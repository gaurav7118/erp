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
var companyid="";
Wtf.namespace("Wtf","Wtf.common","Wtf.account","Wtf.reportBuilder", "Wtf.inventory","Wtf.CustomLayout.DefaultTemplates");
Wtf.req = {
    base: "../../jspfiles/",
    json: "../../json/",
    account: "../../jspfiles/"
};
/* Product Type Master
+--------------------------------------+--------------------+
| id                                   | name               |
+--------------------------------------+--------------------+
| e4611696-515c-102d-8de6-001cc0794cfa | Inventory Assembly |
| d8a50d12-515c-102d-8de6-001cc0794cfa | Inventory Part     |
| f071cf84-515c-102d-8de6-001cc0794cfa | Non-Inventory Part |
| 4efb0286-5627-102d-8de6-001cc0794cfa | Service            |
+--------------------------------------+--------------------+
 * */
Wtf.unqsrno = [];
Wtf.dupsrno = [];
Wtf.gstDimArray = "";

Wtf.GST_CONFIG_TYPE = {
    
    ISFORMULTIENTITY:1,  // isformultientity = T
    MANDETORY_FIELD:2,   // if madetory field like Product Tax Class but isformultientity = F
    ISFORGST:3,          // isforgstrulemapping = 1
    CUSTOM_TO_ENTITY:4,  // if field is custom field for Entity i.e. it is there for module 1200 only 
    HSN_SAC_CODE:5,       // for HSN/SAC Code
    UQC:6
};

Wtf.GST_CONFIG_TYPE_VALUE = "gstconfigtype";

Wtf.producttype = {
    assembly: "e4611696-515c-102d-8de6-001cc0794cfa",
    invpart: "d8a50d12-515c-102d-8de6-001cc0794cfa",
    noninvpart: "f071cf84-515c-102d-8de6-001cc0794cfa",
    service: "4efb0286-5627-102d-8de6-001cc0794cfa",
    inventoryNonSale: "ff8080812f5c78bb012f5cfe7edb000c9cfa",
    customerAssembly: "a6a350c4-7646-11e6-9648-14dda97925bd",
    customerInventory: "a839448c-7646-11e6-9648-14dda97925bd"
};
Wtf.gstSection = {
    B2B: "B2B Invoices (4A,4B,4C,6B,6C)",
    B2CL: "B2C Large Invoices (5A, 5B)",
    B2CS: "B2C Small Invoices",
    CDNR: "Credit / Debit Note (Registered - 9B)",
    CDNUR: "Credit / Debit Note (UnRegistered - 9B)",
    EXPORT: "Export Invoices (6A)",
    AT: "Tax Liability(Advances received) - 11A(1), 11A(2)",
    ATADJ: "Adjustment of Advances - 11B(1), 11B(2)",
    EXEMPT: "Nil Rated Invoices - 8A, 8B, 8C, 8D",
    HSNSummary: "HSN Wise Summary",
    DOCS:"DOCS"
}
Wtf.IS_GST_HISTORY_PRESENT = "isGSTHistoryDataPresent";
Wtf.gstr2Section ={
    GSTR2_B2B: "B2B Invoices - 3, 4A",
    GSTR2_B2B_unregister: "B2BUR Invoices - 4B",
    GSTR2_CDN: "Credit/Debit Notes Regular - 6C",
    GSTR2_ImpServices: "Import of Services - 4C",
    GSTR2_ImpGoods: "Import of Goods - 5",
    GSTR2_CDN_unregister: "Credit/Debit Notes Unregistered - 6C",
    GSTR2_nilRated: "Nil Rated Invoices - 7 - (Summary)",
    GSTR2_AdvancePaid: "Advance Paid -10A - (Summary)",
    GSTR2_AdvanceAdjust: "Adjustment of Advance - 10B - (Summary)"
}
/**
 * 
 * RCM 5K limit not used now 
 * Purchases from URD in India, will be exempt. Without any ceiling. From 13th October 2017 to 31st March 2018
 * ERP-37398
 */
Wtf.isRCMPurchaseURD5KLimit = false;

Wtf.CESSCalculationFieldSet = 'CESSCalculationFieldSet';
Wtf.GST_CESS_TYPE = "cessType";
Wtf.GST_CESS_VALUATION_AMOUNT = "valuationAmount";
Wtf.DEFAULT_TERMID = "defaultTermID";
Wtf.CESSTYPE_NAME = {
    NOT_APPLICABLE: "Not Applicable",
    PERCENTAGES: "Percentage",
    HIGHER_VALUE_OR_CESSPERCENTAGES: "Value per Thousand or CESS % whichever is higher",
    VALUE_AND_CESSPERCENTAGES: "Value per Thousand + CESS %",
    VALUE: "Value per Thousand"
};
/**
 * GST CESS tax Calculation Type
 */
Wtf.CESSTYPE = {
    NOT_APPLICABLE: "699b94c6-c84d-11e7-bd73-c03fd5658531",
    PERCENTAGES: "699b94c6-c84d-11e7-bd73-c03fd5658532",
    HIGHER_VALUE_OR_CESSPERCENTAGES: "699b94c6-c84d-11e7-bd73-c03fd5658533",
    VALUE_AND_CESSPERCENTAGES: "699b94c6-c84d-11e7-bd73-c03fd5658534",
    VALUE: "699b94c6-c84d-11e7-bd73-c03fd5658535"
};
/**
 * Default GST Terms ID
 */
Wtf.GSTTerm = {
    OutputCGST: "00efad22-5f34-11e7-907b-a6006ad3dba0",
    OutputSGST: "00efb45c-5f34-11e7-907b-a6006ad3dba0",
    OutputIGST: "00efb196-5f34-11e7-907b-a6006ad3dba0",
    OutputUTGST: "00efb75e-5f34-11e7-907b-a6006ad3dba0",
    OutputCESS: "00efba42-5f34-11e7-907b-a6006ad3dba0",
    InputCGST: "00efbbfa-5f34-11e7-907b-a6006ad3dba0",
    InputSGST: "00efbf06-5f34-11e7-907b-a6006ad3dba0",
    InputIGST: "00efbd8a-5f34-11e7-907b-a6006ad3dba0",
    InputUTGST: "00efc0aa-5f34-11e7-907b-a6006ad3dba0",
    InputCESS: "00efc226-5f34-11e7-907b-a6006ad3dba0"
};
Wtf.GSTDefaultAccount = {
    OutputCGST: "31ce34bc-5f2b-11e7-907b-a6006ad3dba0",
    OutputSGST: "31ce38f4-5f2b-11e7-907b-a6006ad3dba0",
    OutputIGST: "31ce37a0-5f2b-11e7-907b-a6006ad3dba0",
    OutputUTGST: "31ce39e4-5f2b-11e7-907b-a6006ad3dba0",
    InputCGST: "31ce3ba6-5f2b-11e7-907b-a6006ad3dba0",
    InputSGST: "31ce3d54-5f2b-11e7-907b-a6006ad3dba0",
    InputIGST: "31ce3c78-5f2b-11e7-907b-a6006ad3dba0",
    InputUTGST: "31ce3e30-5f2b-11e7-907b-a6006ad3dba0",
    CESS : "31ce3aca-5f2b-11e7-907b-a6006ad3dba0"
};   
Wtf.PHPReportType={
    PurchaseRelief : "PurchaseReliefSummary",
    SalesRelief :"SalesReliefSummary"
}
Wtf.templateType = {
    pnl : 0,
    balanceSheet : 1,
    trialBalance : 2,
    cashFlow : 3
}   

Wtf.mrpContractName = {
    day:"1",
    year:"4",
    week:"2",
    month:"3"
}
Wtf.mrpComponentType = {
    Component:"1",
    CoProduct:"2",
    Scrap:"3"
}
Wtf.dirtyStore = {
    title: false,
    customerCategory: false,
    vendorCategory: false,
    assetCategory: false,
    assetDepartment: false,
    assetLocation: false,
//    salesPerson: false,
    inventory: false,
    product: false
};
Wtf.registrationTypeValues = {
    NA : WtfGlobal.getLocaleText("acc.excise.duty.type.NA"),
    DEALER : WtfGlobal.getLocaleText("acc.field.dealer"),
    IMPORTER : WtfGlobal.getLocaleText("acc.field.impoter"),
    MANUFACTURER : WtfGlobal.getLocaleText("acc.field.manufacturer")
};

/*Mobile Field Setup*/
Wtf.mobilefield = {
    detailView:"detailView",
    addEditView:"addEditView",
    summaryView:"summaryView"
};

/*Financial Statements moduleids*/
Wtf.financialStatementsModuleIds = {
    tradingProfitAndLoss:101
};

Wtf.Customer={
    Dormant : "Dormant",
    Active : "Active"
};

Wtf.serviceTaxReports = {
    INPUT_CREDIT_SUMMARY_REPORT: 0,
    BILL_DATE_WISE_REPORT: 1,
    REALISATION_DATE_WISE_REPORT: 2,
    TAX_SERVICE_RECEIVED_REPORT: 3
};

Wtf.excise = {
    VALOREM: "valorem",
    FIXED_DUTY: "fixed_duty",
    SPECIFIC: "specific",
    TARIF_VALUE: "tarif_value",
    TRANSACTION_VALUE: "transaction_value",
    MRP: "mrp",
    COMPOUNDED_LEVY_SCHEME: "compounded_levy_scheme",
    DUTY_PRODUCTION_CAPACITY: "duty_production_capacity",
    WEIGHT: "weight",
    LENGTH: "length",
    VOLUME: "volume",
    THICKNESS: "thickness",
    OTHER: "other",
    QUANTITY: "quantity"
};

/*
 * Addres Related Constant
*/
Wtf.ADDRESS = {
    SHIPPING_ADDRESS:"Shipping Address1",
    BILLING_ADDRESS: "Billing Address1"
};

/*--------------------------*/

Wtf.landingCostCategory = {
    SHIPPING_CHARGES: 0,    //"Shipping Charges",
    FREIGHT_COSTS: 1,       //"Freight Costs",
    IMPORT_FEES: 2,          //"Import Fees",
    CUSTOMS_DUTY:3,         // "Customs and Duty",
    TAXES: 4,               //"Taxes",
    INSURANCE:5,            // "Insurance",
    HANDLING_CHARGES:6,      // "Handling Charges"
    NOT_APPLICABLE:8
};
Wtf.landingCostAllocation = {
    QUANTITY: 0,    //"quantity",
    VALUE: 1,       //"value",
    WEIGHT: 2,       //"Weight"
    MANUAL:3,
    CUSTOMDUTY:4
};

Wtf.GSTR2AComparison = {
    All: 0, //"All",
    Matched: 1, //"Matched",
    NotMatched: 2, //"Not Matched"
    MissingInGSTPortal: 3, //Missing In GST Portal
    MissingInDeskera :4 //Missing In Deskera
};
/*-------------------------*/

Wtf.chartType = {
    bar:"bar",
    pie :"pie",
    line :"line"
}

/**
 * 
 * @type typeGST status for company
 */
Wtf.GSTStatus = {
    NEW: 1,    
    OLDNEW: 2,     
    NONE: 3
    
};
Wtf.GSTCustVenStatus = {
    APPLYGST: 0,    
    NOGST: 1,     
    APPLYSOMEGST: 2,
    APPLYGSTONDATE: 3,
    APPLY_IGST: 4
};
/**
 * GSTR JE type
 * @type type
 */
Wtf.GSTJEType={
    None:0,
    TDS:1,
    TCS:2,
    ITC:3
}
Wtf.GSTRJETYPE={
    TDS:"TDS Credit",
    TCS:"TCS Credit",
    ITC:"ITC Reversed"
}
Wtf.GSTITCTYPE = {
    BLOCKEDITC: "ITC is Blocked",
    ITCREVERSAL: "ITC to be Reversed",
    DEFAULT: 'ITC Available in Full'
}
Wtf.GSTITCTYPEID = {
    DEFAULT: 1,
    BLOCKEDITC: 2,
    ITCREVERSAL:3
    
}
/**
 * 
 * add new  Column pref here 
 */
Wtf.columnPref = {
    isGSTCalculationOnShippingAddress : 'isGSTCalculationOnShippingAddress',
    gstamountdigitafterdecimal : 'gstamountdigitafterdecimal'
}
/**
 * //ERP-34970(ERM-534)
 *GST Customer/ Vendor Registration type default ID'S
 */
Wtf.GSTRegMasterDefaultID = {
    Unregistered : 'ac09adba-58b9-11e7-8ead-c03fd5658532',
    Regular : "ac09adba-58b9-11e7-8ead-c03fd5658531",
    Composition : "ac09adba-58b9-11e7-8ead-c03fd5658533",
    Consumer : "ac09adba-58b9-11e7-8ead-c03fd5658534",
    Regular_ECommerce : "420d7db5-7847-11e7-a551-708bcdaa138a",
    Composition_ECommerce : "13670bb7-7847-11e7-a551-708bcdaa138a"
}
/**
 * 
 * @type typeCustomer Vendor Type default Master item id
 */
Wtf.GSTCUSTVENTYPE = {
    Export : "ac09adba-58b9-11e7-8ead-c03fd5658535",
    ExportWOPAY : "e41d586f-8d30-11e7-b941-6045cb6f9ab5",
    Import : "ac09adba-58b9-11e7-8ead-c03fd5658536",
    SEZ : "ac09adba-58b9-11e7-8ead-c03fd5658537",
    SEZWOPAY : "dc9ff578-a05a-11e7-b9d0-6045cb6f9ab5",
    DEEMED_EXPORT : "33c26e6c-9173-11e7-abc4-cec278b6b50a",
    NA :"47d48400-6789-11e7-b99d-14dda97927f2",
    TaxExempt :"c76dbb92-be25-11e7-a8c4-6045cb6f9ab5"
}
/**
 * RCM PI from  Un-Registered vendor Amount limit
 * @type Number
 */
Wtf.INDIA_URD_RCM_PI_AMOUNTLIMIT = 5000;
Wtf.defaultgstamountdigitafterdecimal = 2;
Wtf.GSTType = {
    INDIATYPE: 1,    
    SINGAPORETYPE: 2
};
Wtf.CompanyVATNumber='';//India Country specific variables
Wtf.CompanyCSTNumber='';
Wtf.CompanyPANNumber='';
Wtf.CompanyNPWPNumber='';
Wtf.CompanyServiceTaxRegNumber='';
Wtf.CompanyTANNumber='';
Wtf.CompanyECCNumber='';
// TDS Flow for Indian Company
Wtf.isTDSApplicable=false;
Wtf.isSTApplicable=false;
Wtf.headofficetanno='';
Wtf.commissioneratecode='';
Wtf.commissioneratename='';
Wtf.divisioncode='';
Wtf.rangecode='';
Wtf.isExciseApplicable=false;
Wtf.isGSTApplicable=false;
Wtf.showIndiaCompanyPreferencesTab=false;
Wtf.IndianGST=false;
Wtf.exciseTariffdetails=false;
Wtf.isFirstTimeLoad=true;
Wtf.excisecommissioneratecode='';
Wtf.excisecommissioneratename='';
Wtf.excisedivisioncode='';
Wtf.exciserangecode='';
Wtf.TDSincometaxcircle='';
Wtf.TDSrespperson='';
Wtf.TDSresppersonfathersname='';
Wtf.TDSresppersondesignation='';
Wtf.deductortype='';

Wtf.salesaccountidcompany='';
Wtf.salesretaccountidcompany='';
Wtf.purchaseretaccountidcompany='';
Wtf.purchaseaccountidcompany='';
Wtf.interstatepuracccformid='';
Wtf.interstatepuraccid='';
Wtf.interstatepuraccreturncformid='';
Wtf.interstatepurreturnaccid='';
Wtf.interstatesalesacccformid='';
Wtf.interstatesalesaccid='';
Wtf.interstatesalesaccreturncformid='';
Wtf.interstatesalesreturnaccid='';

//India Compliance Constants
Wtf.LINELEVELTERMTYPE_VAT = 1;
Wtf.LINELEVELTERMTYPE_Excise_DUTY = 2;
Wtf.LINELEVELTERMTYPE_CST = 3;
Wtf.LINELEVELTERMTYPE_SERVICE_TAX = 4;
Wtf.LINELEVELTERMTYPE_SBC = 5;
Wtf.LINELEVELTERMTYPE_KKC = 6;
Wtf.BASIC_EXEMPTION_APPLIED = "4";

//for company specific flags
Wtf.sms_templateflag = 1;
Wtf.senwan_group_templateflag = 2;
Wtf.ferrate_group_templateflag = 3;
Wtf.lsh_templateflag = 4;
Wtf.smsholding_templateflag = 5;
Wtf.pacific_tec_templateflag = 7;
Wtf.sats_templateflag = 8;
Wtf.senwan_tech_templateflag = 10;
Wtf.spaceTec_templateflag = 11;
Wtf.hengGaon_templateflag = 12;
Wtf.BIT_templateflag = 13;
Wtf.TID_templateflag = 14;
Wtf.HCIS_templateflag = 15;
Wtf.BuildMate_templateflag = 16;
Wtf.F1Recreation_templateflag = 17;
Wtf.F1RecreationLeasing_templateflag = 171;
Wtf.sustenir_templateflag = 18;
Wtf.Diamond_Aviation_templateflag = 19;
Wtf.Merlion_templateflag = 20;
Wtf.senwan_tech_short_quotation_flag = 1;   // shortQuoteFlag=1 for short template of senwan tech otherwise it should be 0. [Mayur B]
Wtf.Guan_Chong_templateflag = 21;
Wtf.Guan_ChongBF_templateflag = 2102;
Wtf.Arklife_templateflag = 23;
Wtf.Alfatech_templateFlag = 24;
Wtf.Tony_FiberGlass_templateflag = 27;
Wtf.Armada_Rock_Karunia_Transhipment_templateflag = 25;
Wtf.SBI_templateflag = 28;
Wtf.Monzone_templateflag = 29;
Wtf.BestSafety_templateflag = 30;
Wtf.FascinaWindows_templateflag = 31;
Wtf.RightSpace_templateflag = 32;
Wtf.RightWork_templateflag = 33;
Wtf.Amcoweld_templateflag = 34;
Wtf.KimChey_templateflag = 35;
Wtf.FastenEnterprises_templateflag = 36;
Wtf.FastenHardwareEngineering_templateflag = 37;
Wtf.LandPlus_templateflag = 380;            //http://accounting.deskera.com/a/lpn/
Wtf.LandBank_templateflag = 381;            //http://accounting.deskera.com/a/lbp/
Wtf.LandBest_templateflag = 382;            //http://accounting.deskera.com/a/lbr/
Wtf.LandElHome_templateflag = 383;          //http://accounting.deskera.com/a/leh/
Wtf.LandHub_templateflag = 384;             //http://accounting.deskera.com/a/lhp/
Wtf.LandMax_templateflag = 385;             //http://accounting.deskera.com/a/lmp/
Wtf.LandQuest_templateflag = 386;           //http://accounting.deskera.com/a/lqp/
Wtf.LandSelectReality_templateflag = 387;   //http://accounting.deskera.com/a/lsr/
Wtf.LandVin_templateflag = 388;             //http://accounting.deskera.com/a/lvp/
Wtf.LandPlus_Zenn_templateflag = 389;       //http://accounting.deskera.com/a/zenn/
Wtf.LandPlus_Mobility_templateflag = 390;
Wtf.BakerTilly_templateflag = 39;
Wtf.BakerTilly_templateflag_pcs = 391;
Wtf.PrimePartners_templateflag = 40;
Wtf.Swatow_templateflag = 41;
Wtf.GPlus_templateflag = 47;
Wtf.CleanSolutions_templateflag = 48;
Wtf.GoldBell_templateflag = 49;
Wtf.Sanxing_templateflag = 50;
Wtf.GohYeowSeng_templateflag = 51;
Wtf.hinsitsu_templateflag = 52;
Wtf.tanejaHomes_templateflag = 53;
Wtf.SeqenceFormatMaxLengthForIndianCompany= 16;

Wtf.cash_detail_type = 0;
Wtf.card_detail_type = 1;   // detail types for payment  [Mayur B] //PayDetail.getPaymentMethod().getDetailType()
Wtf.bank_detail_type = 2;
Wtf.ChequeNoIngore= 0;
Wtf.ChequeNoBlock= 1;
Wtf.ChequeNoWarn= 2;
Wtf.ProductDescInTextArea= 0;
Wtf.ProductDescInTextBox= 1;
Wtf.ProductDescInHtmlEditor= 2;
Wtf.ProductSortByName= 0;
Wtf.ProductSortById= 1;
Wtf.productSearchByStartWith= 0;
Wtf.productSearchByAnywhere= 1;
Wtf.CustomerVendorSortByName= 0; //Sort customer by name
Wtf.CustomerVendorSortByCode= 1; //Sort customer by code
Wtf.AccountSortByName= 0; //Sort Account by name
Wtf.AccountSortByCode= 1; //Sort Account by code
Wtf.AccountProducttype=0;
Wtf.AccountProcutdescription=1;
// Manual Journal Entry Types

Wtf.normal_journal_entry = 1;
Wtf.party_journal_entry = 2;
Wtf.fund_transafer_journal_entry = 3;

Wtf.Acc_Invoice_ModuleId = 2;
Wtf.Acc_BillingInvoice_ModuleId = 3;
Wtf.Acc_Cash_Sales_ModuleId = 4;
Wtf.Acc_Billing_Cash_Sales_ModuleId = 5;
Wtf.Acc_Vendor_Invoice_ModuleId = 6;
Wtf.Acc_Vendor_BillingInvoice_ModuleId = 7;
Wtf.Acc_Cash_Purchase_ModuleId = 8;
Wtf.Acc_BillingCash_Purchase_ModuleId = 9;

Wtf.Acc_Debit_Note_ModuleId = 10;
Wtf.Acc_BillingDebit_Note_ModuleId = 11;

Wtf.Acc_Credit_Note_ModuleId = 12;
Wtf.Acc_BillingCredit_Note_ModuleId = 13;

Wtf.Acc_Make_Payment_ModuleId = 14;
Wtf.Acc_BillingMake_Payment_ModuleId = 15;

Wtf.Acc_Receive_Payment_ModuleId = 16;
Wtf.Acc_BillingReceive_Payment_ModuleId = 17;

Wtf.Acc_Purchase_Order_ModuleId = 18;
Wtf.Acc_BillingPurchase_Order_ModuleId = 19;

Wtf.Acc_Sales_Order_ModuleId = 20;
Wtf.Acc_BillingSales_Order_ModuleId = 21;

Wtf.Acc_Customer_Quotation_ModuleId = 22;
Wtf.Acc_Vendor_Quotation_ModuleId = 23;
Wtf.Acc_GENERAL_LEDGER_ModuleId = 24;
Wtf.Acc_Customer_ModuleId = 25;
Wtf.Acc_Customer_ModuleUUID = "09508488-c1d2-102d-b048-001e58a64cb6";
Wtf.Acc_Vendor_ModuleId = 26;
Wtf.Acc_Delivery_Order_ModuleId = 27;
Wtf.Acc_Goods_Receipt_ModuleId = 28;
Wtf.Acc_Sales_Return_ModuleId = 29;
Wtf.Acc_Purchase_Return_ModuleId = 31;
Wtf.Acc_Purchase_Requisition_ModuleId = 32;
Wtf.Acc_RFQ_ModuleId = 33;
Wtf.Acc_PRO_ModuleId = 34;

Wtf.Account_Statement_ModuleId = 34; 
Wtf.Acc_Basic_Template_Id = "ff8080813ff0605a013ff07200670003";
Wtf.No_Template_Id=-99;
Wtf.Acc_Product_Master_ModuleId=30;
Wtf.Acc_EntityGST=1200;
Wtf.Acc_Contract_ModuleId=35;
Wtf.Acc_Lease_Order=36;
Wtf.Acc_Lease_Contract=64;
Wtf.Acc_Lease_Quotation=65;
Wtf.Acc_Lease_DO=67;
Wtf.Acc_Lease_Return=68;
Wtf.Acc_Assembly_Product_Master_ModuleId=91;
Wtf.Acc_Ledger_ModuleId=102;
Wtf.Bank_Reconciliation_ModuleId=124;

Wtf.Consignment_Sales_ModuleId= 201;
Wtf.Consignment_Purchase_ModuleId = 202;

//Assets
Wtf.DEPRECIATION_BASED_ON_FIRST_FINANCIAL_YEAR_DATE = 0;
Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE = 1;
Wtf.Max_Depreciation_Months = 2000;
Wtf.EffectiveFrom_DateOfAcquisiation_NoOfDays = 3;

Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId = 38;
Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId = 39;
Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId = 40;
Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId = 41;
Wtf.Acc_FixedAssets_AssetsGroups_ModuleId = 42;

Wtf.Asset_Maintenance_ModuleId=43;
Wtf.Customer_Address_ModuleId=44;
Wtf.vendor_Address_ModuleId=45;
Wtf.Transaction_Address_ModuleId=46;
Wtf.DeliveryOrderDetail_ModuleId=47;
Wtf.InvoiceDetail_ModuleId=48;
Wtf.Acc_ConsignmentRequest_ModuleId=50;
Wtf.Acc_ConsignmentDeliveryOrder_ModuleId=51;
Wtf.Acc_ConsignmentInvoice_ModuleId=52;
Wtf.Acc_ConsignmentSalesReturn_ModuleId=53;
Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId=57;
Wtf.Acc_Consignment_GoodsReceipt_ModuleId=58;
Wtf.Acc_ConsignmentPurchaseReturn_ModuleId=59;
Wtf.Acc_ConsignmentVendorRequest_ModuleId=63;
Wtf.blankSearchKey="fe62f955-6f8c-11e8-b3eb-6045cb6f9ada";
Wtf.blankSearchText="Blank";

//Delivery Planner_ModuleID
Wtf.Delivery_Planner_ModuleId=804;
Wtf.Delivery_Planner_Announcement_ModuleId=1804;

Wtf.Acc_Packing_List_ModuleId = 54;
Wtf.Acc_Packing_List_Lc_ModuleId = 55;
Wtf.Acc_Packing_List_NonLc_ModuleId = 56;

Wtf.Acc_Packing_ModuleId = 554;
Wtf.Acc_Packing_List_Lc_ModuleId = 654;
Wtf.Acc_Packing_List_NonLc_ModuleId = 754;
Wtf.Acc_Shipping_ModuleId = 854;

Wtf.Acc_Customer_AccountStatement_ModuleId = 60 ;
Wtf.Acc_Vendor_AccountStatement_ModuleId = 61;
//Wtf.Acc_SOA_CustomerAccountStatement_ModuleId = 62;
//Wtf.Acc_SOA_VendorAccountStatement_ModuleId = 63;

// Aged Module IDs : Start
Wtf.Acc_AgedReceivables_Summary_ModuleId = 843;
Wtf.Acc_AgedPayables_Summary_ModuleId = 842;
Wtf.Acc_AgedReceivables_ReportView_ModuleId = 213;
Wtf.Acc_AgedPayables_ReportView_ModuleId = 844;
// Aged Module IDs : End

Wtf.SerialWindow_ModuleId =79;
Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId = 87;
Wtf.Acc_FixedAssets_RFQ_ModuleId = 88;
Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId = 89;
Wtf.Acc_FixedAssets_Purchase_Order_ModuleId = 90;
Wtf.Acc_FixedAssets_Purchase_Return_ModuleId = 96;
Wtf.Inventory_ModuleId =92;//Stock Issue
Wtf.Inventory_Stock_Adjustment_ModuleId = 95;;//Stock Adjustment
Wtf.Inventory_Stock_Repair_ModuleId = 247;;//Stock Adjustment
Wtf.LEASE_INVOICE_MODULEID =93;
Wtf.Acc_Security_Gate_Entry_ModuleId =1116;
Wtf.Acc_Multi_Entity_Dimension_ModuleId = 1200; //Multi Entity Module
Wtf.Build_Assembly_Report_ModuleId = 133;//Build Assembly Entry
Wtf.BOM_Wise_Stock_Report = 1340; //Bom Wise Stock Report 

Wtf.TrialBalance_Moduleid =95;
Wtf.SalesCommisionStmt_Moduleid =97;
Wtf.Acc_FixedAssets_Sales_Return_ModuleId = 98;
Wtf.Acc_FixedAssets_Details_ModuleId = 121;
Wtf.Acc_FixedAssets_SummaryReport_ModuleId = 122;

Wtf.ACC_GENERAL_LEDGER_REPORT_MODULEID = 350;    //Used for Grid Config setting
Wtf.ACC_GROUP_DETAIL_REPORT_MODULEID=351;       //Used for Grid Config setting  

Wtf.Acc_Sales_Report_Master_ModuleId=1250;

//used for barcode generation code

Wtf.GOODS_PENDING_ORDER = 3;
Wtf.FULFILLED_ORDER = 2;
Wtf.STORE_ORDER = 1;

Wtf.BarcodeGenerator_SerialId=1;
Wtf.BarcodeGenerator_ProductId=2;
Wtf.BarcodeGenerator_SKUField=3;
Wtf.BarcodeGenerator_Barcode=4;
Wtf.BarcodeGenerator_BatchID=5;
Wtf.BarcodeGenerator_NULL=0;
Wtf.Currency_ExchangeRate_Module_Id=123;
Wtf.Tax_Currency_ExchangeRate_Module_Id=126;

//Indian Compliance Excise Duty Invoice
Wtf.Acc_ExciseDuty_Invoice=330;
Wtf.Acc_ExciseDuty_Purchase_Invoice=331;
Wtf.UnkownDeducteeTypeReportID = 7;
Wtf.PANNotAvailableReportID = 8;
Wtf.NatureOfPaymentWiseReportID = 9;
Wtf.VATComputationReportID = 11;
//Loan Management
Wtf.LONE_MANAGEMENT_MODULEID =121;

Wtf.Product_Batch_Expiry_Date_Report_Module_Id=301;
Wtf.INDEX_MOBILE_TRANSACTIONS =50;
Wtf.INDEX_SOs_FOR_INVOICING =16;
Wtf.RECORD_Mobile_Application = 1;
Wtf.RECORD_WEB_Application = 0;

//Recurring Reports ModuleId
Wtf.Acc_Recurring_SalesInvoice_ModuleId = 470;               //SDP-10659 Column order not getting saved in recurring invoice tab
Wtf.Acc_Recurring_PurchaseInvoice_ModuleId = 471;            //SDP-10659 Column order not getting saved in recurring invoice tab
Wtf.Acc_Recurring_SalesOrder_ModuleId = 472;                 //SDP-10659 Column order not getting saved in recurring invoice tab
Wtf.Acc_Recurring_JE_ModuleId = 473;                         //SDP-10659 Column order not getting saved in recurring invoice tab
Wtf.Acc_Recurring_MakePayment_ModuleId = 474;                //SDP-10659 Column order not getting saved in recurring invoice tab

//Inventory Reports Moduleid
Wtf.Acc_Stock_Adjustment_ModuleId="95";
Wtf.Acc_Stock_Request_ModuleId="1001";
Wtf.Acc_InterStore_ModuleId="1002";
Wtf.Acc_InterLocation_ModuleId="1003";
Wtf.Acc_CycleCount_ModuleId=1004;
Wtf.recordbillid="";
Wtf.OrderNoteNo="";

//Expense Grid Config settings
Wtf.Expense_Grid_ModuleId = 601;
Wtf.Product_Selection_Window_Grid_Id = 602;
Wtf.Serial_No_Window_Grid_Id = 603;
Wtf.Batch_No_Window_Grid_Id = 604;
Wtf.Stock_Valuation_Report_Grid_Id = 605;
Wtf.InterStore_Form_Grid_Id = 606;

//Product Load type flag    
Wtf.Show_all_Products=0;
Wtf.Products_on_type_ahead=1;
Wtf.Products_on_Submit=2;
Wtf.MaxPageSizeLimit = 100;
Wtf.ProductCombopageSize = 30;
Wtf.CustomerCombopageSize = 30;
Wtf.VendorCombopageSize = 30;
//defaulr Mail sender
Wtf.companyMail=0;
Wtf.UserMail=1;

Wtf.MasterConfig_Quality_Parameter=55;
Wtf.MasterConfig_Quality_Group=56;
Wtf.MasterConfig_IndustryCode=59;
Wtf.MasterConfig_BankName=2;
Wtf.MasterConfig_GAFFileVersion='1.0';

Wtf.GSTProdCategory="Product Tax Class";
Wtf.applieddate="applieddate";
Wtf.GSTHSN_SAC_Code="HSN/SAC Code";
Wtf.BarcodeType_Code_CODE128='CODE128';
Wtf.BarcodeType_Code_CODE39='CODE39';
Wtf.BarcodeType_Code_EAN128='EAN128';
Wtf.BarcodeType_Code_EAN13='EAN13';
Wtf.BarcodeType_Code_EAN8='EAN8';
Wtf.BarcodeType_Code_CODEBAR='CODEBAR';
Wtf.BarcodeType_Code_UPCA='UPCA';
Wtf.BarcodeType_Code_UPCE='UPCE';
Wtf.BarcodeType_Code_POSTNET='POSTNET';
Wtf.BarcodeType_Code_INTERLEAVED2OF5='INTERLEAVED2OF5';
Wtf.BarcodeType_Code_ROYALMAILCUST='ROYALMAILCUST';
Wtf.BarcodeType_Code_USPSINTGNTMAIL='USPSINTGNTMAIL';
Wtf.BarcodeType_Code_DATAMATRICS='DATAMATRICS';

Wtf.BarcodeType_Value_CODE128='Code 128';
Wtf.BarcodeType_Value_CODE39='Code 39';
Wtf.BarcodeType_Value_EAN128='EAN-128';
Wtf.BarcodeType_Value_EAN13='EAN-13';
Wtf.BarcodeType_Value_EAN8='EAN-8';
Wtf.BarcodeType_Value_CODEBAR='CODEBAR';
Wtf.BarcodeType_Value_UPCA='UPC-A';
Wtf.BarcodeType_Value_UPCE='UPC-E';
Wtf.BarcodeType_Value_POSTNET='POSTNET';
Wtf.BarcodeType_Value_INTERLEAVED2OF5='Interleaved 2 of 5';
Wtf.BarcodeType_Value_ROYALMAILCUST='Royal Mail Customer Barcode';
Wtf.BarcodeType_Value_USPSINTGNTMAIL='USPS Intelligent Mail';
Wtf.BarcodeType_Value_DATAMATRICS='DataMatrix';

Wtf.PrintType_Direction_Right='Right';
Wtf.PrintType_Direction_Left='Left';
Wtf.PrintType_Direction_Bottom='Bottom';
Wtf.PrintType_Direction_Top='Top';
Wtf.PrintType_Value_Right='90';
Wtf.PrintType_Value_Left='-90';
Wtf.PrintType_Value_Bottom='0';
Wtf.PrintType_Value_Top='180';

Wtf.location='location';
Wtf.warehouse='warehouse';
Wtf.row='row';
Wtf.rack='rack';
Wtf.bin='bin';
Wtf.UpriceAndAmountDisplayValue='**********';

Wtf.companyAccountPref_custvenloadtype='custvenloadtype';
Wtf.companyAccountPref_withinvupdate='withinvupdate';
Wtf.companyAccountPref_activateProfitMargin='activateProfitMargin';
Wtf.companyAccountPref_memo='memo';
Wtf.companyAccountPref_descriptionType='descriptionType';
Wtf.companyAccountPref_allowZeroUntiPriceForProduct='allowZeroUntiPriceForProduct';
Wtf.companyAccountPref_allowZeroUntiPriceInLeaseModule='allowZeroUntiPriceInLeaseModule';
Wtf.companyAccountPref_termsincludegst='termsincludegst';
Wtf.companyAccountPref_productOptimizedFlag='productOptimizedFlag';
Wtf.companyAccountPref_activeVersioningInPurchaseOrder='activeVersioningInPurchaseOrder';
Wtf.companyAccountPref_autoPopulateMappedProduct='autoPopulateMappedProduct';
Wtf.companyAccountPref_isDuplicateItems='isDuplicateItems';
Wtf.companyAccountPref_countryid='countryid';
Wtf.companyAccountPref_noOfDaysforValidTillField='noOfDaysforValidTillField';
Wtf.companyAccountPref_autoquotation='autoquotation';
Wtf.companyAccountPref_autovenquotation='autovenquotation';
Wtf.companyAccountPref_enableGST='enableGST';
Wtf.companyAccountPref_retainExchangeRate='retainExchangeRate';
Wtf.companyAccountPref_currencyid='currencyid';
Wtf.companyAccountPref_fyfrom='fyfrom';
Wtf.companyAccountPref_bbfrom='bbfrom';
Wtf.companyAccountPref_accountsWithCode='accountsWithCode';
Wtf.companyAccountPref_activateToBlockSpotRate='activateToBlockSpotRate';
Wtf.companyAccountPref_enableLinkToSelWin='enableLinkToSelWin';
Wtf.companyAccountPref_withoutBOM='withoutBOM';
Wtf.companyAccountPref_customerPoReferenceNo='customerPoReferenceNo';
Wtf.companyAccountPref_discountMaster='discountMaster';
Wtf.companyAccountPref_recuringSalesInvoiceMemo='recuringSalesInvoiceMemo';
Wtf.companyAccountPref_displayuom='displayuom';
Wtf.companyAccountPref_advanceSearchInDocumentlinking='advanceSearchInDocumentlinking';
Wtf.companyAccountPref_discountOnPaymentTerms='discountOnPaymentTerms';
Wtf.companyAccountPref_discountInBulkPayment='discountInBulkPayment';
Wtf.companyAccountPref_isPostingDateCheck='isPostingDateCheck';
Wtf.companyAccountPref_differentUOM='differentUOM';

Wtf.HideFormFieldProperty_customerQuotation='customerQuotation';
Wtf.HideFormFieldProperty_vendorQuotation='vendorQuotation';

Wtf.UPerm_customer='customer';
Wtf.UPerm_vendor='vendor';
Wtf.UPerm_invoice='invoice';
Wtf.UPerm_vendorinvoice='vendorinvoice';
Wtf.UPerm_vendorpr='vendorpr';
Wtf.UPerm_creditterm='creditterm'
Wtf.UPerm_tax='tax';

Wtf.Perm_customer='customer';
Wtf.Perm_vendor='vendor';
Wtf.Perm_invoice_createso='invoice_createso';
Wtf.Perm_vendorpr='vendorpr';
Wtf.Perm_vendorinvoice_createpo='vendorinvoice_createpo';
Wtf.Perm_invoice='invoice';
Wtf.Perm_vendorinvoice='vendorinvoice';
Wtf.Perm_invoice_createreceipt='invoice_createreceipt';
Wtf.Perm_vendorinvoice_createpayment='vendorinvoice_createpayment';
Wtf.Perm_creditterm_edit='creditterm_edit';
Wtf.Perm_tax_view='tax_view';
Wtf.companyAccountPref_deliveryPlanner='deliveryPlanner';
Wtf.companyAccountPref_isBatchCompulsory='isBatchCompulsory';
Wtf.companyAccountPref_isLocationCompulsory='isLocationCompulsory';
Wtf.companyAccountPref_isWarehouseCompulsory='isWarehouseCompulsory';
Wtf.companyAccountPref_isRowCompulsory='isRowCompulsory';
Wtf.companyAccountPref_isRackCompulsory='isRackCompulsory';
Wtf.companyAccountPref_isBinCompulsory='isBinCompulsory';
Wtf.companyAccountPref_isSerialCompulsory='isSerialCompulsory';
Wtf.companyAccountPref_shipDateConfiguration='shipDateConfiguration';
Wtf.companyAccountPref_autogoodsreceipt='autogoodsreceipt';
Wtf.companyAccountPref_autoRG23EntryNumber='autoRG23EntryNumber'
Wtf.companyAccountPref_autocashpurchase='autocashpurchase';
Wtf.companyAccountPref_autoinvoice='autoinvoice';
Wtf.companyAccountPref_autocashsales='autocashsales';
Wtf.companyAccountPref_autodo='autodo';
Wtf.companyAccountPref_autogro='autogro';
Wtf.companyAccountPref_jobworkout='autojwo';
Wtf.companyAccountPref_cashaccount='cashaccount';
Wtf.companyAccountPref_custcreditlimit='custcreditlimit';
Wtf.companyAccountPref_negativestock='negativestock';
Wtf.HideFormFieldProperty_customerInvoice='customerInvoice';
Wtf.HideFormFieldProperty_CS='CS';
Wtf.HideFormFieldProperty_vendorInvoice='vendorInvoice';
Wtf.HideFormFieldProperty_CP='CP';
Wtf.Currency_Exchange="Currency Exchange";
Wtf.Tax_Currency_Exchange="Tax Currency Exchange ";
Wtf.Bank_Reconciliation="Bank Reconciliation";
Wtf.DBS_Receiving_Bank_Details = "DBS Receiving Bank Details";
Wtf.Vendor_Quotation_List = "Vendor Quotation";
Wtf.Delivery_Order_List = "Delivery Order";
Wtf.Cash_Sales_List = "Cash Sales";
Wtf.Cash_Purchase_List = "Cash Purchase";
Wtf.Goods_Receipt_Order_List = "GoodsReceiptOrder";
Wtf.Credit_Note_Otherwise = "Credit Note Otherwise";
Wtf.Credit_Note_Against_Vendor = "Credit Note Against Vendor";
Wtf.Inter_Store_Stock_Transfer = "Inter Store Stock Transfer";
Wtf.PURCHASE_RETURN_REMARK_FIELDID='5909f9dc-6e18-11e4-9262-c03fd5632dc7';
Wtf.SALES_RETURN_REMARK_FIELDID='5419f080-6e18-11e4-9262-c03fd5632dc7';   

Wtf.PURCHASE_RETURN_REASON_FIELDID='a80a6246-be7c-11e5-9912-ba0be0483c18';
Wtf.SALES_RETURN_REASON_FIELDID='ce1ccd58-be8a-11e5-9912-ba0be0483c18';   
//Document Designer
Wtf.Subtype_Sales=0;
Wtf.Subtype_Purchase=0;
Wtf.Subtype_Consignment=1;
Wtf.Subtype_Default=0;
Wtf.Subtype_SalesReturn=1;
Wtf.Subtype_PurchaseReturn=1;
Wtf.Subtype_Undercharge = 7;
Wtf.Subtype_Overcharge = 8;
Wtf.Subtype_Inventory=0;
Wtf.Subtype_SOA=0;
Wtf.Subtype_SOI=1;
Wtf.Subtype_SOA_Transaction_Currency=2;
Wtf.Subtype_Lease=2;
Wtf.Subtype_Asset=6;
Wtf.Subtype_Job_Order=3;
Wtf.Subtype_Job_Order_Label=4;
Wtf.Subtype_Opening_Invoice=5; 
Wtf.Subtype_JobWork=9;// subtype for Job Work module 
//QA Approval Subtypes Document Designer
Wtf.Subtype_DeliveryOrder=0;

Wtf.QA_Approval_ID=132;

Wtf.Email_Button_From_Report='20';//option independent upon combo in alert configuration.
Wtf.APPROVAL_EMAIL='22';//option dependent upon combo in alert configuration.
Wtf.REJECTION_EMAIL="23";
//labels    
Wtf.Email_Module_Name_Aged_Recivable="Aged Receivables ";    
Wtf.Email_Module_Name_Aged_Payable="Aged Payables";      
Wtf.Email_Module_Name_Customer = 'Customer';  
Wtf.Email_Module_Name_Vendor= 'Vendor';   
Wtf.Email_Module_Name_Customer_Account_Statement = "Customer_Account_Statement";  
Wtf.Email_Module_Name_Vendor_Account_Statement= 'Vendor_Account_Statement';

Wtf.ADMIN_ROLE_ID='1';
// For Default Master Items
Wtf.WASTAGE_ID = "b0385c02adf611e5bed9eca86bfcd415";

// Default Master Id Deductee Type unknown
Wtf.DeducteeType_Unknown = "79ed9c74-015d-11e6-ba66-14dda9792823";

// Difference in Opening Balance Constant
Wtf.Difference_in_Opening_balances = "Difference in Opening balances";

Wtf.StockInHand = "Stock in Hand" // Stock in hand
Wtf.OpeningStock = "Opening Stock" // Opening Stock
Wtf.AccumulatedProfitAndLoss = "Accumulated Profit/Loss" // Accumulated Profit/Loss

/* Consignment Module Related constant */
Wtf.ERP_CONSIGNMENT_PURCHASE_INVOICE="Consignment Purchase Invoice";

/* Expense Purchase Invoice Constant */
Wtf.EXP_WITH_CASH_PURCHASE_INVOICE="Expense PI With Cash";
Wtf.EXP_WITHOUT_CASH_PURCHASE_INVOICE="Expense PI Without Cash";

/* TAP Return File*/
Wtf.GST_Amount_Claimable = "GST Amount Claimable";
Wtf.GST_Quarterly_Submission=1;
Wtf.GST_Monthly_Submission=0;

var companyid="";
Wtf.Acc_AgedReceivable_CustomizeSummary =103;
Wtf.Acc_AgedPayable_CustomizeSummary =104;

Wtf.Acc_AccountGroup_GL=1;
Wtf.Acc_AccountGroup_Cash=2;
Wtf.Acc_AccountGroup_Bank=3;
Wtf.Acc_AccountGroup_Gst=4;
Wtf.decimalLimiterValue=0.0000000001;// told by Paritosh sir

Wtf.show_just_commodity_software_import_link=false;
Wtf.hide_ibg_company_preference_link=false;
Wtf.show_SetExchageRate_For_MaleshianCompany=false;
Wtf.isNewPaymentStructure = true;
Wtf.ProductComboListWidth = 700;

Wtf.companyAccountPref_isAdminSubdomain=false;

//Inventory valuation type
Wtf.PERPETUAL_VALUATION_METHOD=1;

//MRP module id
Wtf.labourMaster=1101;
Wtf.MRP_Work_Centre_ModuleID=1102;
Wtf.MACHINE_MANAGEMENT_MODULE_ID =1103;
Wtf.MRP_Job_Work_ModuleID=1104;
Wtf.MRP_Work_Order_ModuleID=1105;
Wtf.MRP_MASTER_CONTRACT_MODULE_ID =1106;
Wtf.MRP_Route_Code_ModuleID=1107;
Wtf.MRP_ForeCast_ModuleID=1108;
Wtf.MRP_Job_Work_IN=1109;
Wtf.MRP_Job_Work_ORDER_REC=1114;
Wtf.Job_Work_Out_ORDER_REC=1115;
Wtf.MRP_ROUTING_NONE='None'
Wtf.MRP_ROUTING_TEMPLATE='Routing Template';
Wtf.MRP_ROUTING_CODE='Routing Code';

Wtf.MRP_JOB_WORK_MODULENAME='autojobwork';
Wtf.MRP_Work_Centre_MODULENAME='autoworkcentre';
Wtf.MRP_Work_Order_MODULENAME='autoworkorder';

Wtf.CNDN_TYPE_FOR_MALAYSIA='5';         //ERP-27284 / ERP-28249
//Wtf.CNDN_TYPE_FOR_OVERCHARGE = '6';
Wtf.NoteForUnderCharge = '5';
Wtf.NoteForOvercharge = '6';
Wtf.ZeroRatedTaxAppliedDateForMalasia = "06/01/2018";// Default Format MM/DD/YYYY
Wtf.CapitalGoodsAcquired_TaxNameForMalaysia = "GST(TX-CG)@0%";

Wtf.DISCOUNT_MASTER_MODE='222';         //ERM-68 / ERP-33050

Wtf.ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD='1';         //ERM-177 / ERP-34804
/**
 * Show alert if GST dimension value not present for Specific modules
 */
Wtf.isShowAlertOnDimValueNotPresent = [
    Wtf.Acc_Customer_Quotation_ModuleId, // Customer Quotation
    Wtf.Acc_Vendor_Quotation_ModuleId, // Vendor Quotation
    Wtf.Acc_Purchase_Order_ModuleId, // Purchase Order
    Wtf.Acc_Sales_Order_ModuleId, // Sales Order
    Wtf.Acc_Invoice_ModuleId, // Sales Invoice 
    Wtf.Acc_Cash_Sales_ModuleId, // Cash Sales Invoice 
    Wtf.Acc_Vendor_Invoice_ModuleId, // Purchase Invoice
    Wtf.Acc_Cash_Purchase_ModuleId, // Cash Purchase Invoice
    Wtf.Acc_Delivery_Order_ModuleId, // Delivery Order
    Wtf.Acc_Goods_Receipt_ModuleId, // Goods Receipt
    Wtf.Acc_Sales_Return_ModuleId, // Sales Return
    Wtf.Acc_Purchase_Return_ModuleId, // Purchase Return
    Wtf.Acc_Debit_Note_ModuleId, // Debit Note
    Wtf.Acc_Credit_Note_ModuleId, // Credit Note
    Wtf.Acc_Make_Payment_ModuleId, // Make Payment
    Wtf.Acc_Receive_Payment_ModuleId, // Receive Payment
    Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId, // Asset Sales Invoice
    Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId, // Asset Purchase Invoice
    Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId, // Asset Vendor Quotation
    Wtf.Acc_FixedAssets_Purchase_Order_ModuleId // Asset Purchase Order
];
/**
 * visible isMerchantExporter check for IDNIA country only and below modules
 */
Wtf.isMerchantExporterVisible = [
    Wtf.Acc_Customer_Quotation_ModuleId, // Customer Quotation
    Wtf.Acc_Vendor_Quotation_ModuleId, // Vendor Quotation
    Wtf.Acc_Purchase_Order_ModuleId, // Purchase Order
    Wtf.Acc_Sales_Order_ModuleId, // Sales Order
    Wtf.Acc_Invoice_ModuleId, // Sales Invoice 
    Wtf.Acc_Cash_Sales_ModuleId, // Cash Sales Invoice 
    Wtf.Acc_Vendor_Invoice_ModuleId, // Purchase Invoice
    Wtf.Acc_Cash_Purchase_ModuleId, // Cash Purchase Invoice
    Wtf.Acc_Delivery_Order_ModuleId, // Delivery Order
    Wtf.Acc_Goods_Receipt_ModuleId, // Goods Receipt
]
/**
 * ERP-39530(ERM-1108)
 * For E-Way related fileds gstconfigtype is 8 
 */
Wtf.EWAYFIELDS_GSTCONFIGTYPE = "8";
Wtf.EWAY_BILL_IMPORT_MODULEID = 1300;
Wtf.EWAY_BILL_IMPORT_MODULENAME = "E-Way Bill details";
Wtf.EWAYField_Transport_Mode = "Custom_Transport Mode";
Wtf.EWAYField_Vehicle_No = "Custom_Vehicle No";
Wtf.EWAYField_Transporter_Id="Custom_Transporter ID";// Allow E-way Transporter Doc No field blank in DO, GRN, PR & SR module. ERP-39530
Wtf.EWAYField_Transporter_Doc_No="Custom_Transporter Doc No";// Allow E-way Transporter Doc No field blank in DO, GRN, PR & SR module. ERP-39530
Wtf.EWAYField_Transportation_date="Custom_Transportation Date";// Allow E-way Transporter Doc No field blank in DO, GRN, PR & SR module. ERP-39530
Wtf.EWAYField_Vehicle_Type="Custom_Vehicle Type";// Allow E-way Transporter Doc No field blank in DO, GRN, PR & SR module. ERP-39530
Wtf.EWAYField_Bill_No="Custom_E-Way Bill No";//E-WAY Bill No should have max length 12.
Wtf.EWAYField_Bill_No_maxlength=12;//E-WAY Bill No should have max length 12.
Wtf.CUSTOM_PIN_CODE = "Custom_Pin Code";//Pin Code should have max length field Name
Wtf.CUSTOM_PIN_CODE_maxlength = 6;//Pin Code should have max length 6.
Wtf.EWAYField_Supply_type ='Custom_Supply Type';
Wtf.EWAYField_Sub_Type='Custom_Sub Type';
Wtf.EWAYField_Document_Type='Custom_Document Type';
Wtf.EWAYField_Dispatch_State='Custom_Dispatch State';
Wtf.EWAYField_Ship_To_State='Custom_Ship To State';
Wtf.EWAYField_Distance_level='Custom_Distance level (Km)';
Wtf.GSTCustom_Entity='Entity';
Wtf.Custom_GSTIN='Custom_GSTIN';
Wtf.EWAYField_Transporter_Name='Custom_Transporter Name';
/**
 * E-Way unit fields activated in below modules (Module id's where E-Way Fields Present- 27, 28, 31, 29, 2, 6)
 */
 Wtf.EwayUnitDimCustmFieldsActivatedModules = [
    Wtf.Acc_Invoice_ModuleId, // Sales Invoice 
    Wtf.Acc_Cash_Sales_ModuleId, // Cash Sales Invoice 
    Wtf.Acc_Vendor_Invoice_ModuleId, // Purchase Invoice
    Wtf.Acc_Cash_Purchase_ModuleId, // Cash Purchase Invoice
    Wtf.Acc_Delivery_Order_ModuleId, // Delivery Order
    Wtf.Acc_Goods_Receipt_ModuleId, // Goods Receipt
    Wtf.Acc_Purchase_Return_ModuleId, // Purchase Return
    Wtf.Acc_Sales_Return_ModuleId, // Sales Return
]
Wtf.RECURRINGINVOICES = {
    ALL: 1,
    ACTIVATED: 2,
    DEACTIVATED: 3
}
Wtf.RECURRINGINVOICESMEMO = {
    DATEDON: 1,
    ORIGANALINVOICE: 2,
    DATEDONORIGANALINVOICE: 3
}

Wtf.MasterItems={
    SALESPERSON:"15",
    WOSTATUS:"50"
    
}
Wtf.WODefaultStatus={
    planned:"3493e865-1e3a-11e6-8206-14dda97927f2",
    inprocess:"4c3f913b-1e3a-11e6-8206-14dda97927f2",
    released:"4074b92a-1e3a-11e6-8206-14dda97927f2",
    built:"5c8e70af-1e3a-11e6-8206-14dda97927f2",
    closed:"6b5b8ee9-1e3a-11e6-8206-14dda97927f2"
}
Wtf.massUpdateCAGridTypes = {
    asReqQty : 0,
    asMinPerOfReqQty : 1,
    custBlockQty: 2,
    minPercent: 3
}
Wtf.template={
    VAT_INVOICE:"ee753683-2e98-460f-97c2-4b1a761215d7",
    VAT_INVOICE_MH:"ce73d7e3-94be-4b9c-8a9a-25af82b2c602",
    VAT_DO:"fb9b3a4b-4515-4657-bd3c-338745ebbe2b",
    SERVICE_TAX_INVOICE:"c28d0edb-af85-4ffb-a646-74855159c159",
    RETAIL_INVOICE:"0febd77d-ebca-47e4-a530-b8085485e298",
    RETAIL_INVOICE_MH:"f198576f-040a-4087-bc5e-4d4e406b2b7c",
    RULE_11_MANUFACTURER:"933214e8-ca6e-4aff-90e5-6b73a65f7967",
    DEFAULT_TEMPLATE_INVOICE:"d9cf6243-3a1b-42d3-827f-432433cs7675",
    DEFAULT_TEMPLATE_PO:"fc437d31-0195-492e-848a-0183633ce72fd",
    DEFAULT_TEMPLATE_PI:"fc437d31-0195-492e-48a-0145433c25fd",
    DEFAULT_TEMPLATE_CQ:"996d7111-64a6-4b3e-8b28-02d3a254f66740a"
}
Wtf.StateName = {
    GUJARAT:'3',
    MAHARASHTRA:'4',
    DELHI:'1'
}
Wtf.CustomFieldType={
    SingleSelectDropdown:4,
    MultiSelectDropdown:7,
    CheckList:12
}
Wtf.Currency = {
    SGD : 6
}
Wtf.CurrencyName ={
    MYR : 'Malaysian Ringgit (MYR)',
    SGD : 'SG Dollar(SGD)'
}
Wtf.CurrencySymbols ={
    MYR : 'MYR',
    SGD : 'SGD'
}
Wtf.Country = {
    SINGAPORE : 203,
    MALAYSIA : 137,
    INDONESIA : 106,
    INDIA:105,
    US:244,
    PHILIPPINES:182
}
//Multi Level rules applied upon constants
Wtf.ApprovalRules_AppliedUpon={
    All_Conditions:0,
    Total_Amount:1,
    Journal_Entry_Creator:2,
    Profit_Margin_Amount:3,
    Specific_Products:4,
    Specific_Products_Discount:5,
    Specific_Products_category:6,
    SO_CREDIT_LIMIT:7               //ERM-396
}
Wtf.CountryID = {
    SINGAPORE: "203",
    MALAYSIA: "137",
    INDONESIA: "106",
    INDIA:"105"
}

Wtf.CompanyID = {
    CHKL: "95d79cb9-3efa-49e6-b00e-9df20164cdbd",
    Marubishi : "5b207d19-a091-4815-bc81-3175ae7bd6c6"
}

Wtf.WidgetGlobalParams = {
    CONSOLIDATE_FLAG : "$consolidateFlag$",
    COMPANY_ID : "$companyids$",
    GLOBAL_CURRENCY_ID : "$gcurrencyid$",
    LOGIN_ID : "$loginid$",
    FIN_START_DATE : "$financialstartdate$",
    FIN_END_DATE : "$financialenddate$",
    CURRENT_DATE : "$currentdate$"
}
Wtf.ReportID = {
    DailySalesReport : "Daily_Sales_Report",
    DailyBookingsReport : "Daily_Bookings_Report",
    MonthlyBookingsReport : "Monthly_Bookings_Report",
    YearlyBookingsReport : "Yearly_Bookings_Report",
    MonthlyTradingAndProfitLoss : "Monthly_Trading_&_Profit/Loss",
    YearlyTradingAndProfitLoss : "Yearly_Trading_&_Profit/Loss",
    MonthlySalesReport : "Monthly_Sales_Report",
    MonthlySalesByProduct : "Monthly_Sales_By_Product"
}

Wtf.MasterFormAccount = {
    CustomerAccounts : "customerAccounts",
    VendorAccounts : "vendorAccounts",
    ProductPurchaseAccounts : "productPurchaseAccounts",
    ProductSalesAccounts : "productSalesAccounts"
}

Wtf.IBGBanks={
    DBSBank :1,
    CIMBBank :2,
    UOBBank :3,
    OCBCBank:4
}
Wtf.IBGBanks.MAXAmount = {
    UOBBank : 9999999999999.99
}
Wtf.MaxLimitForFastProcessingMode = 50000;
Wtf.GridStateSaveDelayTimeout = 1500;
Wtf.BadDebtProcessingType={
    Months:0,
    Days:1
},
Wtf.ValidateCustomColumnName = function(colname){
    var patt = new RegExp(/^[\w\s\'\-\/]+$/);       //SDP-12364 : "\ Removed
    return patt.test(colname);
}

Wtf.ValidatePaidReceiveName = function(colname){
    var patt = new RegExp(/^[\w\@\$\%\#\(\)\^\&\*\s\'\"\-\+\.\/]+$/);
    return patt.test(colname);
}
Wtf.ValidateCustomItemName = function(colname){
    var patt = new RegExp(/^[\w\s\'\"\&\-\/\;]+$/);
    return patt.test(colname);
}
Wtf.ValidateAssetId = function(colname){  // Not allow %,+ 
    var patt = new RegExp(/^[^,%+><]*$/);
    return patt.test(colname.trim());
}

Wtf.ValidateJournalEntryNo = function(colname){  // Not Allow %+*!@$^=?:;`~".'
    var patt = new RegExp(/^[^%+*!@$^=?:;`~".'&]*$/);
    return patt.test(colname.trim());
}

Wtf.ValidateTariffName = function(colname){
    var patt = new RegExp(/^[\w\s\'\"\<\>\=\-\/]+$/);
    return patt.test(colname);
}
Wtf.ValidateInterValTime = function(colname){
    var patt = new RegExp(/^[\[0-9]\:;]+$/);
    return patt.test(colname);
}
/*
 * @type RegExp
 *Checks whether html tags are present or not in string. 
 */
Wtf.ValidateHTMLinString = function(stringObj){
    if(!stringObj){
        return false;
    }
    var patt = new RegExp(Wtf.HTMLRegex);
    return patt.test(stringObj);
}
Wtf.ComboReader = new Wtf.data.Record.create([
    {
        name: 'id',
        type: 'string'
    },
    {
        name: 'name',
        type: 'string'
    },
    {
        name: 'mainid',
        type: 'string'
    },
    {
        name: 'hasAccess'
    },
    {
        name: 'itemdescription'
    },
{name: 'level', type: 'int'},
{name: 'leaf', type: 'boolean'},
{name: 'parentid'},
{name: 'parentname'},
{name: 'companyid'},
{name: 'distributedopeningbalanace'},
{name: 'debitType'},
{name: 'field_id'},
{name: 'currencysymbol'},
{name:'isshipping',type:'boolean',defValue:true}
]);

Wtf.comboTemplate = new Wtf.XTemplate('<tpl for="."><div wtf:qtip="{[values.hasAccess === false ? "You cannot assign Archived records" : "" ]}" class="{[values.hasAccess === false ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}">',
        '{name}',
        '</div></tpl>');
        
Wtf.TAB_TITLE_LENGTH = 19;
Wtf.CUSTOM_PANEL_TITLE_LENGTH = 13;
Wtf.QUANTITY_DIGIT_AFTER_DECIMAL=4;//This is temparary value for assignment actual value store in compaccpreferences table
Wtf.AMOUNT_DIGIT_AFTER_DECIMAL=2;//This is temparary value for assignment actual value store in compaccpreferences table
Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL=2;//This is temparary value for assignment actual value store in compaccpreferences table
Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT=6;//This is temparary value for assignment actual value store in compaccpreferences table
Wtf.EXCHANGERATE_DIGIT_AFTER_DECIMAL_FOR_IMPORT= 14
Wtf.EXCHANGERATE_DIGIT_AFTER_DECIMAL_FOR_TRANSACTION= 16
Wtf.Round_Off_Number=Math.pow(10,16);
Wtf.After_Decimal=10;
Wtf.BLANK_IMAGE_URL = "../../lib/resources/images/default/s.gif";
Wtf.DEFAULT_USER_URL = "../../images/defaultuser.png";
Wtf.ValidateMailPatt = /^([a-zA-Z0-9_\-\.+]+)@(([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,10}|[0-9]{1,3})$/;   //SDP-1764
Wtf.ValidateUserid = /^\w+$/;
Wtf.ValidateUserName = /^[\w\s\'\"\.\-]+$/;
Wtf.validateHeadTitle = /^[\w\s\'\"\.\-\,\~\!\@\$\^\*\(\)\{\}\[\])]+$/;
Wtf.DomainPatt = /[ab]\/([^\/]*)\/(.*)/;
Wtf.PhoneRegex= /^([^-])(\(?\+?[0-9]*\)?)?[\.0-9_\- \(\)]*$/;
Wtf.specialCharacters = /[\[\\\^\$\.\|\?\*\+\(\)\{\}]/g;
Wtf.HTMLRegex = /(<([^>]+)>)/ig

/*
*Regex to avoid special characters.
*/
Wtf.avoidSpecialCharacters = /^[^!@#$%^&*_+\=\[\]{};':"\\|,.<>\/?~]/;

Wtf.productNameCommaMaskRe = /^[^,]*$/;
Wtf.specialChar = "";  ///^[a-z A-Z 0-9 ]{1,50}$/;
Wtf.term={
    VAT:1,
    Excise:2,
    CST:3,
    Service_Tax:4,
    Swachh_Bharat_Cess:5,
    Krishi_Kalyan_Cess:6,
    Others:0,
    GST:7
};
Wtf.DNOP={ // default Nature of Purchase // DefaultMasterItem ids
    Manufacturer_Depot:"69558bd3-0aa5-11e6-8c43-14dda97927ea",
    First_Stage_Dealer:"70cee34e-0aa5-11e6-8c43-14dda97927ea",
    From_Agent_of_Manufacturer:"7f678160-0aa5-11e6-8c43-14dda97927ea",
    Purchase_from_Impoter:"850d0810-0aa5-11e6-8c43-14dda97927ea",
    Second_Stage_Dealer:"91b18dba-0aa5-11e6-8c43-14dda97927ea",
    Manufacturer:"da497c6d-6eb4-11e6-8964-14dda97927fc",
    Agent_of_Manufacturer:"7ecaeb3d-6eb4-11e6-8964-14dda97927fc"
};
Wtf.etype = {
    user: 0,
    comm: 1,
    proj: 2,
    home: 3,
    docs: 4,
    cal: 5,
    forum: 6,
    pmessage: 7,
    pplan: 8,
    adminpanel: 9,
    todo: 10,
    search: 11,
    deskera:12,
    exportfile:13,
    exportcsv:14,
    exportpdf:15,
    save:16,
    resetbutton:17,
    add:18,
    edit:19,
    deletebutton:20,
    customer:21,
    audittrail:22,
    permission:23,
    deletegridrow:24,
    menuadd:25,
    menuedit:26,
    menudelete:27,
    addproduct:28,
    buildassemly:29,
    inventoryval:30,
    cyclecount:31,
    addcyclecount:32,
    approvecyclecount:33,
    countcyclecount:34,
    cyclecountreport:35,
    reorderreport:36,
    ratioreport:37,
    addcyclecounttab:38,
    approvecyclecounttab:39,
    countcyclecounttab:40,
    cyclecountreporttab:41,
    salesbyitem:42,
    salesbyitemsummary:43,
    salesbyitemdetil:44,
    copy:45,
    sync:46,
    menuclone:47,
    serialgridrow:48,    
    terminate:49,
    renew:50,
    srcontract:51,
    salesopen:52,
    recycleQuantity:53,
    doDetails:54,
    packingDetails:55,
    stockvaluationSummary:56,
    activate:57,
    deactivate:58,
    activatedeactivate:59,
    syncmenuItem:60,
    inventorysa:61,
    inventorysr:62,
    inventorysi:63,
    inventoryist:64,
    inventoryilst:65,
    inventoryqa:66,
    inventorysrep:67,
    inventorysarbw:68,
    inventorybst:69,
    inventorydst:70,
    inventorydbst:71,
    inventorymior:72,
    inventorysmr:73,
    inventoryistd:74,
    inventoryisar:75,
    inventoryptr:76,
    inventoryiltd:77,
    inventoryrlr:78,
    wastageQuantity: 79,
    salespurchase: 80,
    addtdsgrid:81,
    termCalcWindow:82,
    jobwork:83,
    exciseDetailWindow:84,
    supplierDetailWindow:85,
    inventoryAllStock:86,
    mrpcosting:87,
    jobworkdetails:88,
    discountdetails:89,
    tdswinexpencegrid:90,
    tdswinproductgrid:91,  //For Tax Window  SDP-10875
    deletegridrow1:92,
    inspectiontemplategridrow:93//for inspection template edit icon
};

Wtf.autoNum={
    JournalEntry:0,
    SalesOrder:1,
    Invoice:2,
    CreditNote:3,
    Receipt:4,
    PurchaseOrder:5,
    GoodsReceipt:6,
    DebitNote:7,
    Payment:8,
    CashSale:9,
    CashPurchase:10,
    BillingInvoice:11,
    BillingReceipt:12,
    BillingCashSale:13,
    BillingCashPurchase:14,
    BillingGoodsReceipt:15,
    BillingPayment:16,
    BillingSalesOrder:17,
    BillingPurchaseOrder:18,
    BillingCreditNote:19,
    BillingDebitNote:20,
    AgedPayableWithInv:21,
    AgedPayableWithOutInv:22,
    VendorAgedPayable:23,
    ExportInvoices:24,//Aged Receivable with inventory
    getBillingInvoices:25,//Aged Receivable with inventory
    CustomerAgedReceivable:26,
    BalanceSheet:27,
    DefaultBalanceSheetReportId:845,//Used for Normal balancesheet.
    BalanceSheetPeriodView:846,
    TradingPnl:28,
    RatioAnalysis:29,
    JobWorkOut:1115,//Module ID for Job Work Out 
    RequestForQuotation:33,
    Contract:35,
    Quotation:50,
    CustomerAccountStatement:51,
    VendorAccountStatement:52,
    CustomerAccountLedger:66,
    VendorAccountLedger:67,
    DeliveryOrder:53,
    GoodsReceiptOrder:54,
    ExportCustomizedReceivableSummary:55,
    ExportCustomizedPayablesSummary:56,
    Venquotation:57,
    Requisition:58,
    RFQ : 59,
    CustomerCreditExceed : 60,
    SalesReturn : 61,
    CustomPnlBs : 62,
    profitAndLossMonthlyCustomLayout : 68,//Monthly Profit And Loss Custom Layout with Monthly Budget
    balanceSheetMonthlyCustomLayout : 69,//Monthly Balance Sheet Layout
    dimensionBasedMonthlyPLCustomLayout : 70,//Dimension Based Monthly Profit And Loss Layout
    customer:113,
    vendor:114,
    MonthlyRevenue:119,
    MonthlySales:125,
    StockStatus:126,
    MonthlyTradingPnl:123,
    PurchaseReturn : 63,
    ProjectStatus : 127,
    customer:64,
    vendor:65,
    AccountForecast:197,
    product:198,
    BankBookSummary:201,
    GroupExport:202,
    MonthlyBalanceSheet:203,
    StockLedger:204,
    StockAgeing:205,
    StockReport:253,
    StockValuationDetails:206,
    StockValuationSummary:2060,//Constant for stock valuation summary export button
    AssemblyExport:207 ,
    assetWorkOrderExport:208,
    PackingDoList:209,
    ProjectCountryDetailReport:210,
    ProjectCountrySummaryReport:211,
    PaymentTermSalesCommissionReport:212,
    CustomerQuoationVersion:213,
    CustomerRevenueReport:214,
    InventoryMovementReport:215,
    salesBadDebtClaimReport:216,
    salesBadDebtRecoveredReport:217,
    purchaseBadDebtClaimReport:218,
    purchaseBadDebtRecoveredReport:219,
    TaxesReport:220,
    DimesionReport:221,
    MonthlyCustomerAgedReceivable:222,
    VATReport:223,
    WHTReport:224,
    FixedAssetReport:225,
    TransactionInOutReport:226,
    SalesBadDebtReleifAdjustment: 227,
    PurchaseBadDebtReleifAdjustment: 228,
    StockMovementReport: 229,
    TradingAndProfitLossWithBudget:230,
    MonthlyVendorAgedPayable:231,
    StoreWiseStockBalanceReport:232,
    BatchwiseStockTrackingReport:233,
    salesCommissionDetailReport: 234,
    loanreport: 235,
    StockAdjustmentRegister: 236,
    StockTransferHistoryRegister: 237,
    FullfilledOrdersRegister: 238,
    FixedAssetDepreciation: 239,
    AccountsReEvaluationReport: 240,
    pricingBand: 241,
    StockAvailabilityByCustomerWarehouseReport:242,
    agedSummaryBasedOnSalesPerson:243,
    agedDetailBasedOnSalesPerson:244,
    StockRepairReport:247,
    InterLocationTransferReport:248,
    DateWiseStockTrackingReport:249,
    DateWiseBatchStockTrackingReport:250,
    StockQAReport:251,
    VendorQuotationVersion:252,
    DisposedAssetsReport:253,
    weeklyCashFlow:444,
    inventoryValuation:555,
    vendorproductexpiry:666,
    GeneralLedger : 777,
    BankReconcilation:771,    
    Dimension_Based_TradingAndProfitLoss : 772,
    Dimension_Based_BalanceSheet  : 773,
    LMSJournalEntry:774,
    Dimension_Based_TrialBalance : 775,
    Sales_By_Customer_Against_SO : 776,
    SO_By_ProductReport :  300,
    PO_By_ProductReport :  301,
    ContractReport :  778,
    AssetMaintenanceSchedulerReport :  779,
    productReplacementReport:800,
    productMaintenanceReport:801,
    AssetDepreciationReport:802,
    ReorderLevelReport:803,
    DeliveryPlanner:804,
    StockRequestOnLoanReport:805,
    MonthlyCommissionOfSalesPerson: 806,
    MonthlySalesByProductSubjectToGSTReport: 807,
    agedDetailsBasedOnDimension : 808,
    agedDetailsBasedOnDimensionDetailed : 809,
    salesByProductCategoryDetail :810,
    SalesByServiceProductDetailReport: 811,
    CustomerPartyLedgerSummary: 812,
    CustomerPartyLedgerDetails: 813,
    VendorPartyLedgerSummary: 814, 
    VendorPartyLedgerDetails: 815,
    agedpayablereportBasedOnDimension :816,
    agedpayabledetailedreportBasedOnDimension :817,
    StockRepairPendingReport:818,
    salesPersonCommissionDimensionReport: 819,
    LoanDisbursementReport: 820,
    RepaymentScheduleReport: 821,
    CreditNoteWithAccountDetail:822,
    StockReportOnDimension:823,
    paymentTermSalesCommissionDetailReport: 824,
    SalesPurchaseReport:825,
    AgedReceivableDetailReport:826,
    dayEndCollectionReport:827,
    priceVarianceReport: 828,
    productPriceReportCustVen:829,
    customerRegistryReport:830,
    vendorRegistryReport:831,
    customLineDetailsReport: 832,
    priceListBandReport: 833,
    CycleCountReport: 834,
    ProductSummary: 835,
    LabourList: 836,
    dimensionBasedProfitLoss:837,
    dimensionBasedBalanceSheet:838,
    dimensionBasedTrialBalance:839,
    machineMasterReport:840,
    contractMasterReport:841,
    exporMRPJobWork:1104,
    exportMRPWorkCentre:1102,
    exportMRPWorkOrder:1105,
    DailySalesReport:1106,
    exportRoutingTemplate:1107,
    assetGroupExport:1110,
    driverTrackingExport:1111,
    plaReportExport:1112,
    plaSummaryReportExport:1113,
    creditAvailedReportExport:1114,
    driverTrackingExport:1111,
    annexure10Report:1115,
    rg23Part2:1116,
    rg23Part1:1117,
    dailyStockReport:1118,
    annexure2AReport:1119,
    rule16register:1122,
    exportmrprejecteditemlist:1121,
    DailyBookingsReport:1124,
    MonthlyBookingsReport:1125,
    YearlyBookingsReport:1126,
    costandsellingpriceforanitemstocustomer:1123,
    inputCreditSummaryReport:1127,
    mrpqcreport:1128,
    consolidationGenerationReport:1129,
    consolidatioReport:1130,
    ForeignCurrencyExposure:1131,
    TDSChallanControlReport:1132,
    ForecastTemplate:1133,
    Dealer_Excise_RG23DEntry_No:1134,
    workordertaskstatusexport:1135,
    monthwiseGeneralLedgerReport: 1136,
    SalesAnalysisReport: 1137,
    columnPurchaseRegisterReprot: 1138,
    accountrevaluationReprot: 1139,
    ForeignCurrencyExposureCustomer:1140,
    AssetSummeryReport:1141,
    EntityBasedGSTForm03:1142,
    EntityBasedGSTAuditFile:1143,
    EntityBasedGSTTabReturnFile:1144,
    EntityBasedSalesTaxReport:1145,
    EntityBasedPurchaseTaxReport:1146,
    EntityBasedGSTReport:1147,
    DVATForm31:1148,
    TradingAndProfitLoss:1149,
    BalanceSheetReportId:1150,      // Added BalanceSheetReportId:1141 because (BalanceSheet:27) 27 is already Module id of DO
    CashFlowStatement:1151,
    TrialBalance:1152,
    GroupDetailReport:1153,
    unknownDeducteeTypeReport:1154,
    PANNotAvailableReport:1155,
    consolidatioProfitAndLossReport:1156,
    consolidatioBalanceSheetReport:1157,
    customerMonthlySalesbyProduct:1158,  //Constant for customer summary report
    ChallanReport:1159,
    LinkingInformationSalesReport:1160,
    LinkingInformationPurchaseReport:1161,
    AssetGroupReport:1162,
    DimensionBasedMonthlyTradingPL:1163, // For Dimension Based Monthly PL
    inventoryAllStock:1164,
    DisposedAssetReport:1165,
    StockStatusReportId:1166, //It's used in backed (Constants.java) for status report id.
    DimensionBasedMonthlyBS:1200, // For Dimension Based Monthly BS
    TDSMasterRateReport:1201, // For India Country Only.
    VATComputationReport:1202,
    VATAndCSTCalculationReport:1203,
    consolidationStockReport:1204,
    CustomerReceivedReport : 1205,
    VATPurchaseRegister:1206, // For India Country Only.
    VATSalesRegister:1207, // For India Country Only.
    ServiceTaxCreditRegister : 1208,
    MrpCostingReport : 1209,
    StockSummaryReport : 1210,
    SalesCommissionSchemaReport:1211,
    JobToDateProfitReport : 1212,
    PurchaseByVendorReport:1213,
    GeneralPriceListReport:1214,
    VendorCustomerPriceListReport:1215,
    columnSalesRegisterReport: 1216,
    ShippingDOReport:1217,
    ConsignmentReturnList: 1218,
    VendorCustomerPriceReport:1219,
    NatureOfPaymentWiseReport:1220,
    BankReconcilationHistoryDetails:1221,
    checkInandCheckOut:1222,
    IncidentCasesReport:1223,
    landedcostreport:1224,
    securityGateEntry:1225,
    jwproductsummary:1226,
    QAApprovalReport:132,
    buildAssemblyReport:133,
    PackingReport: 134,
    SOA_CustomerAccountStatement:1227,
    SOA_VendorAccountStatement:1228,
    gstSalesTaxLiabilityReport: 135,
    StoreWiseStockBalanceSummeryReport:1229,
    consolidationCustomBalanceSheet:1230,
    Sales_Report_Master:1231,
    Purchase_Report_Master:1232,
    CostOfManufacturingReport:1330,
    GSTR2MatchAndReconcile:1331,
    GSTR3BReport:1332,
    BudgetVsCost:1333,
    ActualVsBudget:1334,
    GSTR3BDetailReport:1335,
    GSTRComputationDetailReport:1435, // GST Computation report invdividual section export to excel
    consolidationCustomPNL:1336,
    GoodsPendingOrdersRegister: 1337,
    JobWorkStockInModuleID: 1338, //Job Work Stock In module id
    JobWorkOutStockTransferModuleID: 1339, //Job Work Out Stock Transfer module id
    BOMAssemblyExport: 1340, //BOM wise Stock Report Export
    salesCommissionproductDetailReport: 1341,
    putchasesReliefReport:1342 //Purchases Relief Report ERP-41505
};

Wtf.NA_UOM_DEFAULTMEASUREOFUOM_ID = '9d4f64ee-f5dc-11e7-a316-708bcdaa138a';


/*
 * 
 * @type String
 * Module IDs array for advance search based on Multi Entity
 */
Wtf.MultiEntityReportsModuleIdArray = '' + Wtf.Acc_Invoice_ModuleId + ',' + Wtf.Acc_Vendor_Invoice_ModuleId + ',' + Wtf.Acc_Receive_Payment_ModuleId + ',' + Wtf.Acc_Debit_Note_ModuleId + ',' + Wtf.Acc_Credit_Note_ModuleId + ',' + Wtf.Acc_Make_Payment_ModuleId + ',' + Wtf.Acc_GENERAL_LEDGER_ModuleId + ',' + Wtf.Acc_Delivery_Order_ModuleId+'';
Wtf.MalaysianGSTForm03Taxes = ["GST(TX)","GST(TX-CG)","GST(TX-ES)","GST(TX-IES)","GST(TX-RE)","GST(IM)","GST(IS)","GST(AJP)","GST(SR)","GST(ZRL)","GST(ZDA)","GST(ZRE)","GST(DS)","GST(ES)","GST(IES)","GST(RS)","GST(AJS)","GST(TX-FRS)","GST(SR-MS)","GST(SR-JWS)","GST(NTX)"];

var SATSCOMPANY_ID="bfb1057a-38d0-49ae-bb18-99f87eb658a3";
Wtf.ExportMolueName={
   product :'Product',
   customer:'Customer',
   vendor:'Vendor',
   Group:'Group',
   Account: 'Accounts',
   Fixed_Asset_Group: 'Fixed Asset Group'
};

function getModuleNameByModuleNumber(modulenumber) {
    var modulename = "";
    switch (modulenumber) {

        case Wtf.Acc_Customer_ModuleId:
            modulename = '--------[ Customer Master] Dimension(s)---------';
            break;

        case Wtf.Acc_Invoice_ModuleId:
            modulename = '--------[ Invoice/Cash sales ] Dimension(s)-----';
            break;
        case Wtf.Acc_Credit_Note_ModuleId:
            modulename = '--------[ Credit Note ] Dimension(s)---------';
            break;
        case Wtf.Acc_Debit_Note_ModuleId:
            modulename = '--------[ Debit Note ] Dimension(s)---------';
            break;
        case Wtf.Acc_Receive_Payment_ModuleId:
            modulename = '--------[ Receipt ] Dimension(s)---------';
            break;
        default :
            modulename = '';

    }
            return  modulename;
}


/*these module name used while importing data. 
 * Here module given name should be matched with the modulename given in table modules
 */

Wtf.OpeningModuleName={
   openingSalesInvoice :'Opening Sales Invoice',
   openingPrchaseInvoice:'Opening Purchase Invoice',
   openingReceipt:'Opening Receipt',
   openingPayment:'Opening Payment',
   openingVendorCreditNote:'Opening Vendor Credit Note',
   openingCustomerCreditNote:'Opening Customer Credit Note',
   openingVendorDebitNote:'Opening Vendor Debit Note',
   openingCustomerDebitNote:'Opening Customer Debit Note',
   openingCustomerSalesOrder:'Sales Order',
   openingVendorPurchaseOrder:'Purchase Order'
};

Wtf.ChannelName={
   // For Invoice Modules 
   CIAndCSReport :'/CustomerInvoiceAndCashSalesReport/gridAutoRefresh',
   VIAndCPReport:'/VendorInvoiceAndCashPurchaseReport/gridAutoRefresh',
   
   SalesOrderReport : '/SalesOrderReport/gridAutoRefresh',
   PurchaseOrderReport : '/PurchaseOrderReport/gridAutoRefresh',
   
   SalesQuotationReport : '/SalesQuotationReport/gridAutoRefresh',
   PurchaseQuotationReport : '/PurchaseQuotationReport/gridAutoRefresh',
   
   DeliveryOrderReport : '/DeliveryOrderReport/gridAutoRefresh',
   GoodsReceiptReport : '/GoodsReceiptReport/gridAutoRefresh',    
   
   CreditNoteReport :'/CreditNoteReport/gridAutoRefresh',
   DebitNoteReport:'/DebitNoteReport/gridAutoRefresh',
   
   SalesReturnReport :'/SalesReturnReport/gridAutoRefresh',
   PurchaseReturnReport:'/PurchaseReturnReport/gridAutoRefresh',
      
   // For Fixed Asset Modules 
   FixedAssetAIList:'/FixedAssetAcquiredInvoiceList/gridAutoRefresh',
   FixedAssetDIList :'/FixedAssetDisposalInvoiceList/gridAutoRefresh',
   FixedAssetReceiptList :'/FixedAssetReceiptList/gridAutoRefresh',
   FixedAssetDeliveryList :'/FixedAssetDeliveryList/gridAutoRefresh',
   FixedAssetPurchaseOrderList :'/FixedAssetPurchaseOrderList/gridAutoRefresh',
   
   // For Lease Modules
   LeaseQuotationReport : '/LeaseQuotationReport/gridAutoRefresh',
   LeaseOrderReport : '/LeaseOrderReport/gridAutoRefresh',
   LeaseInvoiceList  : '/LeaseInvoiceList/gridAutoRefresh',
   LeaseDeliveryOrderReport : '/LeaseDeliveryOrderReport/gridAutoRefresh',
   
   // For ConsignMent Modules
   ConsignmentPurchaseReturnReport:'/ConsignmentPurchaseReturnReport/gridAutoRefresh',
   //For Product Grid
   ProductAndServicesReport:'/ProductsDetails/gridAutoRefresh',
   //For Account Group Report
   AccountGroupReport:'/AccountGroupReport/gridAutoRefresh'
};
Wtf.ReportListName={
    //For Userpermission in NavigationPanel 
    BankBookSummaryReport :'Bank_Book_Summary_Report',
    CashFlowStatement :'Cash_Flow_WorkSheet',
    DefaultCustomerList: 'Default_Customer_List',
    DimensionsReport :'Dimensions_Report' ,
    MonthlyTradingProfitLoss:'Monthly_Trading_&_Profit/Loss',
    MonthlyBalanceSheet:'Monthly Balance Sheet',
    TopandDormantCustomersByProducts:'Top_and_Dormant_Customers_By_Products' ,
    TopandDormantProductsByCustomers :'Top_and_Dormant_Products_By_Customers',
    TopandDormantVendorsByProducts:'Top_and_Dormant_Vendors_By_Products',
    MonthlyRevenue : 'Monthly_Revenue'  ,
    SalesByCustomer: 'Sales_By_Customer',                    
    SalesByItemReport: 'Sales_By_Item_Report',                
    SalesByProduct :'Sales_By_Product',
    CustomerRevenueReport :'Customer_Revenue_Report',
    InactivecustomerList:'Inactive_customer_List',
    SalesBySalesPerson :'Sales_By_Sales_Person',
    MonthlySalesRegister:'Monthly_Sales_Report',
    OutstandingOrdersReport:'Outstanding_Orders_Report',
    Gstreport :'view_gst_report',
    StockAgeing:'Stock_Ageing',
    StockReport:'Stock_Report',
    StockValuation:'Stock_Valuation_Detail_Report',
    IrasAudifile :'Export IAF Text File',
    SalesReplacementReport:'Sales_Replacement_Report',
    SalesMaintenanceReport:'Sales_Maintenance_Report',
    Contract:'Contract',
    CostCenterReport:'Cost_Center_Report',
    productreplacementreport:'Product_Replacement_Report',
    productmaintenancereport:'Product_Maintenance_Report',
    TaxReport : 'Tax_Report',
    StockLedger:'Stock_Ledger',
    StockStatus:'Stock_Status_Report',
    Stockvaluation:'Stock_Valuation_Summary_Report',
    Stockreport:'Stock_Report',
    SOACustomerAccountStatement:'SOA_Customer_Account_Statement',
    SOAVendorAccountStatement:'SOA_Vendor_Account_Statement',
    VATOutReportIndonesia : 'PPN_Keluaran_Output_VAT'
};
Wtf.account.nature={
    Liability:0,
    Asset:1,
    Expences:2,
    Income:3
};
Wtf.masterTypeValueOfAccount={
    GLTypeAccount:1,
    CashTypeAccount:2,
    BankTypeAccount:3,
    GSTTypeAccount:4
    
}
Wtf.controlCases={
   IGNORE :0,
   BLOCK:1,
   WARN:2
};

Wtf.appID={
    PM:1,
    CRM:2,
    ERP:3,
    HRMS:4,
    eUnivercity:5, //LMS
    eLeave:6,
    inventory:7,
    eClaim:8,
    eTraining:9 
}
var bHasChanged = false;
Wtf.Perm = {};
Wtf.UPerm = {};
Wtf.UserReportPerm = {};
Wtf.UserReporRole ={};
this.countryRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'}
]);
this.stateRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'}
]);
this.timezoneRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'}
]);

Wtf.currencyRec = new Wtf.data.Record.create([
    {name: 'currencyid',mapping:'tocurrencyid'},
    {name: 'symbol'},
    {name: 'currencyname',mapping:'tocurrency'},
    {name: 'exchangerate'},
    {name: 'htmlcode'},
    {name: 'id',mapping:'tocurrencyid'},
    {name: 'name',mapping:'tocurrency'},
    {name: 'erdid',mapping:'id'},
    {name: 'companyid'},
    {name: 'fromcurrencyid'},
    {name: 'fromcurrency'},
    {name: 'currencycode'},
    {name: 'ismaxnearestexchangerate'}
]);
Wtf.countryStore = new Wtf.data.Store({
//    url:Wtf.req.base+"UserManager.jsp",
    url : "kwlCommonTables/getAllCountries.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },this.countryRec),
    baseParams:{
        mode:20,
        common:'1'
    }
});
Wtf.stateStore = new Wtf.data.Store({
//    url:Wtf.req.base+"UserManager.jsp",
    url : "kwlCommonTables/getAllStates.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },this.stateRec),
    baseParams:{
        mode:20,
        common:'1'
    }
});
Wtf.currencyStore = new Wtf.data.Store({
//    url:Wtf.req.base+"CompanyManager.jsp",
    url:"ACCCurrency/getCurrencyExchange.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.currencyRec),
    baseParams:{
        mode:201,
        common:'1'
    },
    autoLoad:false
});
Wtf.timezoneStore = new Wtf.data.Store({
//    url:Wtf.req.base+"UserManager.jsp",
    url:"kwlCommonTables/getAllTimeZones.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },this.timezoneRec),
    baseParams:{
        mode:16,
        common:'1'
    },
    autoLoad:false
});

Wtf.personRec = new Wtf.data.Record.create ([
        {name:'accid'},
        {name:'accname'},
        {name:'vattinno'},        
        {name:'csttinno'},        
        {name:'gstin'},     
        {name:'GSTINRegistrationTypeId'}, 
        {name:'CustVenTypeDefaultMstrID'},  
        {name:'GSTINRegTypeDefaultMstrID'},     //ERP-34970(ERM-534)   
        {name:'CustomerVendorTypeId'},
        {name:'GSTINRegistrationTypeName'},        
        {name:'CustomerVendorTypeName'},
        {name:'panno'},        
        {name:'vendorbranch'},        
        {name:'npwp'},        
        {name:'servicetaxno'},        
        {name:'tanno'},        
        {name:'eccno'},        
        {name:'acccode'},
//        {name: 'level'},
        {name:'openbalance'},
        {name:'id'},
        {name:'title'},
        {name:'aliasname'},
        {name:'accnamecode'},
        {name:'address'},
        {name:'baddress2'},
        {name:'baddress3'},
        {name:'personname',mapping:'accname'},
        {name:'personemail',mapping:'email'},
        {name:'personid',mapping:'id'},
        {name:'taxeligible',type:'boolean'},
        {name:'overseas',type:'boolean'},
        {name:'mapcustomervendor',type:'boolean'},
        {name:'taxidnumber'},
        {name:'taxcode'},
        {name:'company'},        
        {name:'uenno'},        
        {name:'pdm'},
        {name:'isPermOrOnetime'},
        {name:'pdmname'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'bankaccountno'},
        {name:'termid'},
        {name:'termname'},
        {name: 'isavailableonlytosalespersons'},
        {name: 'isvendoravailabletoagent'},
        {name: 'agentsmappedwithvendor'},
        {name: 'salesPersonAgent'},
        {name: 'mappedSalesPersonId'},
        {name: 'mappedReceivedFromId'},
        {name: 'mappedPaidToId'},
        {name: 'mappedMultiSalesPersonId'},//For multi-select sales person combobox
        {name: 'defaultagentmappingid'},
        {name:'other'},
        {name: 'leaf'},
        {name: 'istaxeligible'},
        {name: 'creationDate' ,type:'date'},
        {name: 'categoryid'},
        {name:'intercompany',type:'boolean'},
        {name:'isTDSapplicableoncust',type:'boolean'},
        {name:'isTDSapplicableonvendor',type:'boolean'},
        {name:'isVendorUsedInTDSTransactions',type:'boolean'},
        {name: 'intercompanytypeid'},
        {name: 'taxno'},
        {name: 'level'},
        {name: 'contactperson'},
        {name: 'amountdue'},
        {name: 'mappingaccid'},
        {name: 'mappingvenaccid'},
        {name: 'mappingcusaccid'},
        {name: 'country'},
        {name: 'limit'},
        {name: 'sequenceformat'},   
        {name: 'addressDetails'},
        {name: 'billingAddress'},
        {name: 'billingContactPerson'},
        {name: 'billingContactPersonNumber'},
        {name: 'billingContactPersonDesignation'},
        {name: 'billingMobileNumber'},
        {name: 'billingEmailID'},
        {name: 'shippingAddress'},
        {name: 'shippingState'},        
        {name: 'productname'},
        {name: 'prodname'},
        {name: 'productid'},
        {name: 'isIBGActivated'},
        {name: 'isactivate'},
        {name: 'employmentStatus'},
        {name: 'employerName'},
        {name: 'companyAddress'},
        {name: 'occupationAndYears'},
        {name: 'monthlyIncome'},
        {name: 'noofActiveCreditLoans'},
        {name: 'exceededamount'},
        {name: 'companyRegistrationNumber'},
        {name: 'gstRegistrationNumber'},
        {name: 'paymentCriteria'},
        {name: 'pricingBandID'},
        {name: 'mappingcusaccid'},
        {name: 'mappingvenaccid'}, 
        {name: 'gstVerifiedDate',type:'date'},
        {name: 'seztodate',type:'date'},
        {name: 'sezfromdate',type:'date'},
        {name: 'synchedfromotherapp'} ,                    //set to non-editable customer code i.e. synced from CRM
        {name: "ibgReceivingDetails"},    
        {name: "isPropagatedPersonalDetails"} ,
        {name :'DBSbank'},
        {name :'CIMBbank'},
        {name :'itno'},
        {name :'panStatusId'},
        {name :'natureOfPayment'},
        {name :'natureOfPaymentname'},
        {name :'tdsInterestPayableAccount'},
        {name :'istdsInterestPayableAccountisUsed'},
        {name :'deducteeTypeId'},
        {name :'deducteeCode'},
        {name: 'dealertype'},
        {name: 'vatregdate',type:'date'},
        {name: 'cstregdate',type:'date'},
        {name: 'defaultnatureofpurchase'},
        {name: 'importereccno'},
        {name: 'iecno'},
        {name: 'range'},
        {name: 'division'},
        {name: 'commissionerate'},
        {name: 'vehicleNo'},
        {name: 'vehicleNoID'},
        {name: 'driver'},
        {name: 'driverID'},
        {name: 'termdays'},
        {name: 'termid'},
        {name: 'accountid'},
        {name: 'masterSalesPerson'},
        {name: 'masterSalesPersonName'},
        {name: 'masterReceivedForm'},
        {name: 'masteragent'},
        {name: 'masteragentname'},
        {name: 'billto'},
        {name: 'currencysymbol'},     //ERP-26588    SDP-3679 Amount defined under Credit Sales limit getting mapped to Amount Due
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name:'deleted'},
        {name:'mappedAccountTaxId'},
        {name: 'taxId'},
        {name: 'rmcdApprovalNumber'},
        {name: 'groupname'},
        {name: 'selfBilledFromDate',type:'date'},
        {name: 'selfBilledToDate',type:'date'},
        {name:'id',mapping:'accid'},
        {name:'name',mapping:'accname'},
        {name:'billingEmail'},
        {name:'hasAccess'},
        {name:'isLoanClear'},
        {name:'isLoanApply'},
        {name:'deducteetype'},
        {name:'deducteetypename'},
        {name:'residentialstatus'},
        {name:'interstateparty'},
        {name:'isInterstatepartyEditable'},
        {name:'isUsedInTransactions'},
        {name:'isVendorUsedInTDSTransactions'},
        {name:'cformapplicable'},
        {name:'gtaapplicable'},
        {name:'defaultnatureofpurchase'},
        {name: 'manufacturertype'},
        {name: 'deliveryDate'},
        {name: 'deliveryTime'},
        {name: 'commissionerate'},
        {name: 'division'},
        {name: 'range'},
        {name: 'iecnumber'},
        {name: 'addressExciseBuyer'},
        {name: 'billingState'},
        {name: 'dtaaApplicable'},
        {name: 'dtaaFromDate',type:'date'},
        {name: 'dtaaToDate',type:'date'},
        {name: 'dtaaSpecialRate'},
        {name: 'higherTDSRate'},
        {name: 'lowerRate'},
        {name: 'nonLowerDedutionApplicable'},
        {name: 'deductionReason'},
        {name: 'certificateNo'},
        {name: 'deductionFromDate',type:'date'},
        {name: 'deductionToDate',type:'date'},
        {name: 'referenceNumberNo'},
        {name: 'minPriceValueForVendor'},
         {name:'currentAddressDetailrec'},
         {name:'addressMappingRec'},       
         {name:'uniqueCase'},
         {name: 'considerExemptLimit'},
         {name: 'pricingBandName'},
         {name : 'paymentmethod'},
         {name: 'controlaccountcode'},
        {name: 'controlaccountname'}
        
        
]);
Wtf.customerAccStore =  new Wtf.data.Store({
//    url:Wtf.req.account+'CustomerManager.jsp',
    url:"ACCCustomer/getCustomersForCombo.do",
    baseParams:{
         mode:2,
         group:10,
         deleted:false,
         nondeleted:true,
        common:'1'
    },
    reader: new  Wtf.data.KwlJsonReader({
        root: "data",
        autoLoad:false
    },Wtf.personRec)
});
Wtf.customerAccRemoteStore =  new Wtf.data.Store({
    //    url:Wtf.req.account+'CustomerManager.jsp',
    url:"ACCCustomer/getCustomersForCombo.do",
    baseParams:{
         mode:2,
         group:10,
         deleted:false,
         nondeleted:true,
        common:'1'
    },
    reader: new  Wtf.data.KwlJsonReader({
        root: "data",
        autoLoad:false
    },Wtf.personRec)
});

Wtf.vendorAccStore =  new Wtf.data.Store({
//    url:Wtf.req.account+'VendorManager.jsp',
    url:"ACCVendor/getVendorsForCombo.do",
    baseParams:{
         mode:2,
         group:13,
         deleted:false,
         nondeleted:true,
        common:'1'
    },
    reader: new  Wtf.data.KwlJsonReader({
        root: "data",
        autoLoad:false
    },Wtf.personRec)
});
Wtf.vendorAccRemoteStore =  new Wtf.data.Store({
//    url:Wtf.req.account+'VendorManager.jsp',
    url:"ACCVendor/getVendorsForCombo.do",
    baseParams:{
         mode:2,
         group:13,
         deleted:false,
         nondeleted:true,
        common:'1'
    },
    reader: new  Wtf.data.KwlJsonReader({
        root: "data",
        autoLoad:false
    },Wtf.personRec)
});

Wtf.uomRec = Wtf.data.Record.create ([
    {name:'uomid'},
    {name:'uomname'},
    {name: 'precision'}
]);
Wtf.uomStore=new Wtf.data.Store({
    url: "ACCUoM/getUnitOfMeasure.do",
    baseParams:{
        mode:31,
        common:'1'
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.uomRec)
});


    
   
 Wtf.RecDevMode = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);        
        Wtf.deliveryModeStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 44
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, Wtf.RecDevMode)
        });
        
        
   Wtf.shipmentStatusStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 43
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, Wtf.RecDevMode)
        });

 Wtf.packagingProfileTypeStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams: {
            mode: 112,
            groupid: 48
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, Wtf.RecDevMode)
    });
                
         
Wtf.reportingUOMStore=new Wtf.data.Store({
    url: "ACCUoM/getUnitOfMeasure.do",
    baseParams:{
        mode:31,
        common:'1'
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.uomRec)
});
Wtf.productTypeRec = Wtf.data.Record.create ([
    {name: 'id'},
    {name: 'name'}
]);
Wtf.productTypeStore=new Wtf.data.Store({
    url: "ACCProduct/getProductTypes.do",
    baseParams:{
        mode:24,
        common:'1'
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productTypeRec)
});

Wtf.salesAccRec = Wtf.data.Record.create ([
    {name: 'accid'},
    {name: 'accname'},
    {name: 'acccode'},
    {name: 'hasAccess'},
    {name: 'id', mapping: 'accid'},
    {name: 'name', mapping: 'accname'}
]);
Wtf.salesAccStore=new Wtf.data.Store({
    url:"ACCAccountCMN/getAccountsForCombo.do",
    baseParams:{
         mode:2,
         ignoreCashAccounts:true,
         ignoreBankAccounts:true,
         ignoreGSTAccounts:true,  
         ignorecustomers:true,  
         ignorevendors:true,
         common:'1',
         nondeleted:true
//         nature:[3]
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesAccRec)
});
Wtf.accountStore=new Wtf.data.Store({
    url:"ACCAccountCMN/getAccountsForCombo.do",
    baseParams:{
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true,
                controlAccounts:true
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesAccRec)
});
Wtf.termRec = new Wtf.data.Record.create([
        {name: 'termid'},
        {name: 'termname'},
        {name: 'termdays'},
        {name: 'isdefaultcreditterm'},
        {name: 'id', mapping: 'termid'},
        {name: 'name', mapping: 'termname'}
]);

Wtf.termds = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.termRec),
//    url: Wtf.req.account + 'CompanyManager.jsp',
    url : "ACCTerm/getTerm.do",
    baseParams:{
        mode:91,
        common:'1'
    }
 });
Wtf.accGrpoup = new Wtf.data.Record.create([
    {name: 'groupid'},
    {name: 'groupname'},
    {name: 'id', mapping: 'groupid'},
    {name: 'name', mapping: 'groupname'}
]);

Wtf.accGrpoupStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.accGrpoup),
    url: "ACCAccount/getGroups.do",
    baseParams: {
        mode: 1,
        ignorevendors: false,
        ignorecustomers: false
    }
});
Wtf.GridRecTitle = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);

Wtf.TitleStore=new Wtf.data.Store({
//   url:Wtf.req.account+'CompanyManager.jsp',
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,   
        groupid:6,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.GridRecTitle)
});
//For Line Level Terms
Wtf.LineTermsMasterRecord = Wtf.data.Record.create ([
   {name:"masterid", mapping: 'id'},
   {name:"name"},
   {name: 'groupid'},//value to identify group
   {name: 'activated'},
   {name: 'defaultMasterItem'},
   {name: 'typeid',mapping: 'code'}
]);
Wtf.LineTermsMasterStore=new Wtf.data.Store({
//   url:Wtf.req.account+'CompanyManager.jsp',
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,   
        groupid:37,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.LineTermsMasterRecord)
});

Wtf.ShippingRouteRec=new Wtf.data.Record.create([
 {name:'id'},
 {name:'name'}  
]);
Wtf.ShippingRouteStore=new Wtf.data.Store({
  url:"ACCMaster/getMasterItems.do",
  baseParams:{
    mode:112,   
    groupid:28,
    common:'1'
  },
  reader: new Wtf.data.KwlJsonReader({
        root: "data"
  },Wtf.ShippingRouteRec)      
});

Wtf.custCategoryRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);

Wtf.CustomerCategoryStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:7,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.custCategoryRec)
});

Wtf.interCompanyTypeRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);

Wtf.InterCompanyTypeStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:14,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.interCompanyTypeRec)
});

Wtf.registrationTypeStore = new Wtf.data.SimpleStore({
    fields: [{
        name:'id'
    },{
        name:'name'
    }],
    data:[['0',WtfGlobal.getLocaleText("acc.excise.duty.type.NA")],['1',WtfGlobal.getLocaleText("acc.field.dealer")], ['2',WtfGlobal.getLocaleText("acc.field.impoter")], ['3',WtfGlobal.getLocaleText("acc.field.manufacturer")]] //    1-Dealer 2-Importer 3-Manufacturer 
});
Wtf.basisOfCalculationStore = new Wtf.data.SimpleStore({
    fields: [{
        name:'id'
    },{
        name:'name'
    }],
    data:[['',"ALL"],['1',"Cash/Realization"], ['2',"Accrual"]]  //ERP-26691
});
Wtf.natureOfTransactionStore = new Wtf.data.SimpleStore({// Import on service store > Report - Service Tax - Input Credit Summary (Indian Compliance)
    fields: [{
        name:'id'
    },{
        name:'name'
    }],
    data:[['',"ALL"],['1',"Import on Services"]]  //ERP-26691
});
Wtf.transactionTypeStore = new Wtf.data.SimpleStore({//Static store for field Transaction Type for Sales module (ERP-31477)
    fields: ['id', 'name'],
    data: [["Cash", "Cash"],
        ["Credit", "Credit"],
    ],
    autoLoad: true
});
Wtf.exciseMethodStore = new Wtf.data.SimpleStore({
    fields: ['id', 'name'],
    data: [[Wtf.excise.VALOREM, 'Ad Valorem Method'], [Wtf.excise.QUANTITY, 'Quantity'], [Wtf.excise.SPECIFIC, 'Specific basis'], [Wtf.excise.MRP, 'MRP (Maximum Retail Price)']]
});
Wtf.VATMethodStore = new Wtf.data.SimpleStore({
    fields: ['id', 'name'],
    data: [[Wtf.excise.VALOREM, 'Ad Valorem Method'], [Wtf.excise.QUANTITY, 'Quantity'],[Wtf.excise.MRP, 'MRP (Maximum Retail Price)']]
});
Wtf.manufactureTypeStore = new Wtf.data.SimpleStore({
    fields: [{
        name:'id'
    },{
        name:'name'
    }],
    data:[['1','Regular'], ['2','Small Scale Industries(SSI)']] 
});
Wtf.deducteeTypeRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'},
    {name:'defaultMasterItem'},
    {name:'typeofdeducteetype'},
    
]);

Wtf.deducteeTypeStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:34
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.deducteeTypeRec)
});
Wtf.natureOfStockItemRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);

Wtf.natureOfStockItemStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:52
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.natureOfStockItemRec)
});
Wtf.defaultNatureOfPurchaseRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'},
    {name:'defaultMasterItem'}
]);

Wtf.defaultNatureOfPurchaseStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:47
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.defaultNatureOfPurchaseRec)
});
Wtf.vatCommodityRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'},
    {name:'vatcommoditycode'},
    {name:'vatscheduleno'},
    {name:'vatscheduleserialno'},
    {name:'vatnotes'}
]);

Wtf.vatCommodityStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:42
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.vatCommodityRec)
});

Wtf.vedCategoryRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);
Wtf.VendorCategoryStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:8,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.vedCategoryRec)
});

Wtf.assetCategoryRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);
Wtf.AssetCategoryStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:9,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.assetCategoryRec)
});

Wtf.assetDepartmentRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);

Wtf.AssetDepartmentStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:13,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.assetDepartmentRec)
});

Wtf.assetLocationRec = Wtf.data.Record.create ([
    {name:'id'},
    {name:'name'}
]);

Wtf.AssetLocationStore=new Wtf.data.Store({
   url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:12,
        common:'1'
     },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.assetLocationRec)
});

Wtf.taxRec = new Wtf.data.Record.create([
   {name: 'taxid'},
   {name: 'taxname'},
   {name: 'percent',type:'float'},
   {name: 'taxcode'},
   {name: 'accountid'},
   {name: 'accountname'},
   {name: 'applydate', type:'date'},
    {name: 'id', mapping: 'taxid'},
    {name: 'name', mapping: 'taxname'}

]);

Wtf.taxStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.taxRec),
//    url: Wtf.req.account + 'CompanyManager.jsp',
    url : "ACCTax/getTax.do",
    baseParams:{
        mode:33,
        common:'1'
    }
});

Wtf.CostCenterRec = Wtf.data.Record.create ([
    {name: 'id'},
    {name: 'ccid'},
    {name: 'name'},
    {name: 'description'}
]);
Wtf.CostCenterStore=new Wtf.data.Store({
    url: "CostCenter/getCostCenter.do?forCombo=report",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.CostCenterRec)
});
Wtf.FormCostCenterStore=new Wtf.data.Store({
    url: "CostCenter/getCostCenter.do?forCombo=form",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.CostCenterRec),
    baseParams:{
        common:'1'
    }
});
Wtf.UnitStoreRec = Wtf.data.Record.create ([   // ERP-27117 :Provide Feature to Edit Excise Unit Window Showing in Grid
    {name: 'id'},
    {name: 'name'},
    {name: 'registrationType'},
    {name: 'ECCNo'},
    {name: 'warehouse'}
]);
Wtf.FormUnitStore=new Wtf.data.Store({
    url: "ACCInvoice/getCompanyUnit.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.UnitStoreRec)
});
Wtf.LineLevelCostCenterRec = Wtf.data.Record.create ([
    {name: 'id', defaultValue: ''},
    {name: 'ccid'},
    {name: 'name'},
    {name: 'description'}
]);
Wtf.LineLevelCostCenterStore=new Wtf.data.Store({
    url: "CostCenter/getCostCenter.do?forCombo=form",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.LineLevelCostCenterRec),
    baseParams:{
        common:'1'
    }
});

Wtf.FormCostCenterStore.addEvents({
    'costcenterloaded' : true
});

Wtf.FormCostCenterStore.on("load", function(){
    Wtf.FormCostCenterStore.fireEvent('costcenterloaded');
});

Wtf.DORec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    }]
);

Wtf.DOStatusStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DORec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:10
    }
});

Wtf.MPPaidToRec=new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'},
    {name: 'isIbgActivItematedForPaidTo', type:'boolean'}
]);

Wtf.MPPaidToStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.MPPaidToRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:17
    }
});

Wtf.RPReceivedFromRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    }]
);

Wtf.RPReceivedFromStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.MPPaidToRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:18
    }
});

Wtf.ProductCategoryRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    }]
);

Wtf.ProductCategoryStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.ProductCategoryRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:19
    }
});

Wtf.agentRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    },
    {
            name:'hasAccess'
    }]
);

Wtf.agentStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.agentRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:20
    }
});

Wtf.agentStore.on("load", function() {
    var record = new Wtf.data.Record({
        id: '',
        name: 'None'
    });
    Wtf.agentStore.insert(0, record);
}, this);

Wtf.salesPersonRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    },{
        name: 'userid'
    },
     {
            name:'hasAccess'
    }
    ]
);
     
Wtf.salesPersonStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesPersonRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:15
    }
});
Wtf.dostatusStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.salesPersonRec),
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 10
    }
});
Wtf.salesPersonStore.on("load", function() {
    var record = new Wtf.data.Record({
        id: '',
        name: 'None'
    });
    Wtf.salesPersonStore.insert(0, record);
}, this);

Wtf.DocNoRec = new Wtf.data.Record.create([
    {
        name:'id', mapping:'billid'
    },
    {
        name:'name', mapping:'billno'
    }]
);
Wtf.SONoStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DocNoRec),
    url:"ACCLinkData/getLinkedSONo.do",
    baseParams:{
        isAdvanceSearch:true
    }
});
Wtf.DONoStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DocNoRec),
    url:"ACCLinkData/getLinkedDONo.do",
    baseParams:{
        isAdvanceSearch:true
    }
});
Wtf.CQNoStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DocNoRec),
    url:"ACCLinkData/getLinkedCQNo.do",
    baseParams:{
        isAdvanceSearch:true
    }
});
Wtf.storeRec = new Wtf.data.Record.create([
{
    name:'id', 
    mapping:'store_id'
},
{
    name:'name', 
    mapping:'fullname'
}]
);
Wtf.Stores=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.storeRec),
    url:"INVStore/getStoreList.do",
    baseParams:{
        isAdvanceSearch:true,
        isActive : true,
        storeTypes : "1"
    }
});

Wtf.SINoStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DocNoRec),
    url:"ACCLinkData/getLinkedSINo.do",
    baseParams:{
        isAdvanceSearch:true
    }
});
Wtf.VQNoStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DocNoRec),
    url:"ACCLinkData/getLinkedVQNo.do",
    baseParams:{
        isAdvanceSearch:true
    }
});
Wtf.PONoStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DocNoRec),
    url:"ACCLinkData/getLinkedPONo.do",
    baseParams:{
        isAdvanceSearch:true
    }
});
Wtf.GRNoStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DocNoRec),
    url:"ACCLinkData/getLinkedGRNo.do",
    baseParams:{
        isAdvanceSearch:true
    }
});
Wtf.PINoStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.DocNoRec),
    url:"ACCLinkData/getLinkedPINo.do",
    baseParams:{
        isAdvanceSearch:true
    }
});
Wtf.PRNoStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.DocNoRec),
    url: "ACCLinkData/getLinkedPRNo.do",
    baseParams: {
        isAdvanceSearch: true
    }
});
Wtf.DNStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.DocNoRec),
    url: "ACCLinkData/getLinkedDebitNoteNo.do",
    baseParams: {
        isAdvanceSearch: true
    }
});
Wtf.CNStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.DocNoRec),
    url: "ACCLinkData/getLinkedCreditNoteNo.do",
    baseParams: {
        isAdvanceSearch: true
    }
});
Wtf.RFQNoStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.DocNoRec),
    url: "ACCLinkData/getLinkedRFQNo.do",
    baseParams: {
        isAdvanceSearch: true
    }
});
Wtf.salesPersonFilteredByCustomer=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesPersonRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:15
    }
});
Wtf.customerBankAccountTypeStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesPersonRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:61
    }
});
Wtf.movmentTypeStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesPersonRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:30 //Gruop  id for movement Type
    }
});

Wtf.LoanTypeStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesPersonRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:32 //Gruop  id for Loan Type
    }
});
Wtf.LoanCategoryStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesPersonRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:58 //Gruop  id for Loan Type
    }
});
Wtf.departmentRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    }]
);

Wtf.departmentStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.departmentRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:13
    }
});
Wtf.commonRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'},
    {name: 'defaultMasterItem'}
]);
Wtf.materialRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'},
    {name: 'pid'},
    {name: 'productname'}
]);

Wtf.workCentreStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec),
    url: "ACCWorkCentreCMN/getWorkCentreForCombo.do"
});
Wtf.keySkillStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec),
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 54
    }
});

Wtf.processStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 36
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});
Wtf.purchaseAccRec = Wtf.data.Record.create([
    {name: 'id', mapping: 'accid'},
    {name: 'name', mapping: 'accname'},
    {name: 'acccode'},
    {name: 'hasAccess'},
]);
Wtf.purchaseAccStore = new Wtf.data.Store({
    url: "ACCAccountCMN/getAccountsForCombo.do",
    baseParams: {
        mode: 2,
        ignorecustomers: true,
        ignorevendors: true,
        nondeleted: true,
        controlAccounts: true
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.purchaseAccRec)
});
Wtf.workCenterTypeStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 38
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});
Wtf.wcproductRec = Wtf.data.Record.create([
    {name: 'id', mapping: 'productid'},
    {name: 'pid'},
    {name: 'type'},
    {name: 'name', mapping: 'productname'},
    {name: 'desc'},
    {name: 'producttype'},
]);

Wtf.wcproductStore = new Wtf.data.Store({
    url: "ACCProduct/getProductsForCombo.do",
    baseParams: {
        mode: 22,
        onlyProduct: true,
        isFixedAsset: false,
//            type: Wtf.producttype.assembly,
        includeBothFixedAssetAndProductFlag: false
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.wcproductRec)
});
Wtf.materialStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        totalProperty: 'count',
        root: "data"
    }, Wtf.materialRec),
    url: "ACCProductCMN/getBOMforCombo.do"
});
Wtf.labourRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name', mapping: 'empcode'}
]);
Wtf.labourStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        totalProperty: 'count',
        root: "data"
    }, Wtf.labourRec),
    url: "ACCLabourCMN/getLabourForCombo.do"
});
Wtf.machineRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name', mapping: 'machineid'}
]);
Wtf.machineStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        totalProperty: 'count',
        root: "data"
    }, Wtf.machineRec),
    url: "ACCMachineMaster/getMachinesForCombo.do"
});
Wtf.workCenterLocationStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 51
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});
Wtf.workTypeStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 41
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});
Wtf.workCenterManagerStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 40
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});
Wtf.routingRec = Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name', mapping: 'routingtname'},
]);
Wtf.routingStore = new Wtf.data.Store({
    url: "ACCRoutingManagement/getRoutingTemplates.do",
    baseParams: {
        isforcombo: true,
        bomid: '',
        routingmastertype: 0
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.routingRec)
});

Wtf.WOStatusStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 50
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});
Wtf.workOrderTypeStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 49
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});

Wtf.productStoreJobWork = new Wtf.data.Store({
    url: "ACCProduct/getProductsForCombo.do",
    baseParams: {
        mode: 22,
        onlyProduct: true,
        isFixedAsset: false,
        includeBothFixedAssetAndProductFlag: false
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.wcproductRec)
});

Wtf.workOrderRec = Wtf.data.Record.create([
    {name: 'id', mapping: 'workorderid'},
    {name: 'name', mapping: 'workordername'},
]);

Wtf.workOrderStore = new Wtf.data.Store({
    url: "ACCJobWorkController/getWorkOrdersForCombo.do",
    baseParams: {
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.workOrderRec)
});
Wtf.sellerTypeStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 35
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});
Wtf.contractStatusStore = new Wtf.data.Store({
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 45
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.commonRec)
});

Wtf.parentContractIdRec = Wtf.data.Record.create([
    {name: 'id', mapping: 'billid'},
    {name: 'name', mapping: 'billno'},
]);
        
Wtf.parentContractIdStore = new Wtf.data.Store({
    url: "ACCContractMaster/getMasterContracts.do",
    baseParams: {
        mode: 42,
        closeflag: true,
        nondeleted: true,
        onlyApprovedRecords: true,
        rfqlinkflag: true
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty: 'count'
    }, Wtf.parentContractIdRec)
});
    
Wtf.assignedtoRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    }]
);

Wtf.assignedToStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.salesPersonRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:23
    }
});

Wtf.reasonRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    }]
);

Wtf.reasonStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.reasonRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:29
    }
});

Wtf.workOrderStatusRec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    }]
);

Wtf.workOrderStatusStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.workOrderStatusRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:24
    }
});

Wtf.vehicleRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'},
    {name: 'driverID'}
]);

Wtf.vehicleStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.vehicleRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams: {
        mode:112,
        groupid: 25
    }
});

Wtf.driverRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'}
]);

Wtf.driverStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.driverRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 26
    }
});

Wtf.tripRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'}
]);

Wtf.tripStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.tripRec),
    url:"ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 27
    }
});

Wtf.GRORec=new Wtf.data.Record.create([
    {
        name: 'id'
    },
    {
        name: 'name'
    }]
);

Wtf.GROStatusStore=new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.GRORec),
    url:"ACCMaster/getMasterItems.do",
    baseParams:{
        mode:112,
        groupid:11
    }
});
Wtf.pmtRec = new Wtf.data.Record.create([
    {name: 'id', mapping: 'methodid'},
    {name: 'name',mapping: 'methodname'},
    {name: 'accountid'},
    {name: 'acccurrency'},
    {name: 'accountname'},
    {name: 'isIBGBankAccount',
        type: 'boolean'},
    {name: 'isdefault'},
    {name: 'detailtype',
        type: 'int'},
    {name: 'acccustminbudget'},
    {name: 'autopopulate'},
]);
Wtf.pmtStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.pmtRec),
    url: "ACCPaymentMethods/getPaymentMethods.do"
});
Wtf.inventoryStoreRec = Wtf.data.Record.create([
       {
        name:"id"
    },

    {
        name:"name"
    },

    {
        name: 'parentid'
    },
    {
        name: 'location'
    },

    {
        name: 'parentname'
    }
]);

Wtf.inventoryStore = new Wtf.data.Store({
   url:"ACCMaster/getWarehouseItems.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.inventoryStoreRec)
});

Wtf.inventoryLocRec = Wtf.data.Record.create([
    {
        name:"id"
    },

    {
        name:"name"
    },

    {
        name: 'parentid'
    },

    {
        name: 'parentname'
    }
]);

Wtf.inventoryLocation = new Wtf.data.Store({
   url:"ACCMaster/getLocationItems.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.inventoryLocRec)
});
  
Wtf.productRec = Wtf.data.Record.create ([
    {name:'productid'},
    {name:'productname'},
    {name:'desc'},
    {name:'uomid'},
    {name:'uomname'},
    {name:'displayUoMid'},
    {name:'displayUoMName'},
    {name:'displayuomrate'},
    {name:'displayuomvalue'},
    {name:'inspectionTemplate'},
    {name:'salesuomname'},
    {name:'purchaseuomname'},
    {name:'salesuom'},
    {name:'purchaseuom'},
    {name:'stocksalesuomvalue'},
    {name:'stockpurchaseuomvalue'},
    {name:'stockuom'},
    {name:'caseuom'},
    {name:'inneruom'},
    {name:'caseuomvalue'},
    {name:'inneruomvalue'},
    {name:'parentid'},
    {name:'parentname'},
    {name:'purchaseaccountid'},
    {name:'productpurchaseaccountid'},
    {name:'productaccountid'},
    {name:'productsalesaccountid'},
    {name:'salesaccountid'},
    {name:'purchaseretaccountid'},
    {name:'salesretaccountid'},
    {name:'reorderquantity'},
    {name:'quantity'},
    {name:'lockquantity'},
    {name:'warrantyperiod'},
    {name:'warrantyperiodsal'},
    {name:'reorderlevel'},
    {name:'leadtime'},
    {name:'purchaseprice'},
    {name:'saleprice'},
    {name: 'leaf'},
    {name: 'currencysymbol'},
    {name: 'currencyrate'},
    {name: 'producttype'},
    {name: 'type'},
    {name: 'syncable'},
    {name: 'multiuom'},
    {name: 'blockLooseSell'},
    {name: 'pocountinselecteduom'},
    {name: 'socountinselecteduom'},
    {name: 'availableQtyInSelectedUOM'},
    {name:'initialsalesprice'},
    {name: 'initialquantity',mapping:'initialquantity'},
    {name: 'initialprice'},
    {name: 'ccountinterval'},
    {name: 'ccounttolerance'},
    {name: 'productweight'},
    {name: 'vendor'},
    {name: 'pid'},
    {name: 'level'},
    {name: 'salesacctaxcode'},
    {name: 'purchaseacctaxcode'},
    {name: 'timeintervalChk'},
    {name: 'addshiplentheithqty'},
    {name: 'timeinterval'},
    {name: 'parentDependentType'},
    {name: 'dependentType'},
    {name: 'dependentTypeNo'},
    {name: 'dependentTypeQty'},
    {name: 'hourtimeinterval'},
    {name: 'noofqtyvalue'},
    {name: 'noofquqntity'},
    {name:'minorderingquantity', mapping:'minorderingquantity'},
    {name:'maxorderingquantity', mapping:'maxorderingquantity'},
    {name: 'isparentproduct'},
    {name: 'pocount'},
    {name: 'location'},
    {name: 'defaultbomcode'},
    {name: 'defaultbomid'},
    {name:'uomschematypeid'},
    {name: 'warehouse'},
    {name: 'socount'},
    {name:'supplierpartnumber'},
    {name:'depreciationRate'},
    {name:'depreciationMethod'},
    {name:'depreciationCostLimit'},
    {name:'depreciationGL'},
    {name:'provisionGL'},
    {name:'assetSaleGL'},
    {name:'depreciationGLAccount'},
    {name:'depreciationProvisionGLAccount'},
    {name:'isAsset',type:'boolean'},
    {name: 'isStopPurchase',type:'boolean'},
    {name:'shelfLocation'},
    {name: 'isLocationForProduct'},
    {name: 'isWarehouseForProduct'},
    {name: 'isBatchForProduct'},
    {name: 'isSerialForProduct'},
    {name: 'isSKUForProduct'},
    {name: 'isRecyclable'},
    {name: 'recycleQuantity'},
    {name: 'venconsignuomquantity'},
    {name: 'consignquantity'},
    {name: 'isRowForProduct'},
    {name: 'isRackForProduct'},
    {name: 'isBinForProduct'},
    {name:'linkto'},
    {name:'linkid'},
    {name:'linktype'},
    {name:'savedrowid'},
    {name: 'isAutoAssembly'},
    {name: 'sicount'}, // outstanding SI product quantity count,
    {name: 'purchaseaccountname'},
    {name: 'purchaseretaccountname'},
    {name: 'salesaccountname'},
    {name: 'salesretaccountname'},
    {name: 'purchaseuom'},
    {name: 'salesuom'},
    {name: 'shippingtype'},
    {name: 'licensecode'},
    {name: 'additionaldesc'},
    {name: 'additionalfreetext'},
    {name: 'barcode'},
    {name: 'itemgroup'},
    {name: 'recTermAmount'},
    {name: 'OtherTermNonTaxableAmount'},
    {name: 'LineTermdetails'},
    {name: 'dealerExciseTerms'},
    {name: 'uncheckedTermdetails'},
    {name: 'isWastageApplicable', type: 'boolean'},
    {name:'opensocount'},
    {name:'openpocount'},
    {name:'reservestock'},
    {name: 'isActive'},
    {name: 'hasAccess'},
    {name:'compairwithUOM'},
    {name:'valuationType'},
    {name:'valuationTypeVAT'},
    {name:'compairwithUOMVAT'},
    {name:'productMRP'},
    {name:'quantityInReportingUOM'},
    {name:'quantityInReportingUOMVAT'},
    {name:'reortingUOMExcise'},
    {name:'reortingUOMSchemaExcise'},
    {name:'reportingUOMVAT'},
    {name:'reportingUOMSchemaVAT'},
    {name: 'productweightperstockuom'},
    {name: 'productweightincludingpakagingperstockuom'},
    {name:'productvolumeperstockuom'},
    {name:'productvolumeincludingpakagingperstockuom'},
    {name: 'quantity'},
    {name: 'hsncode'},
    {name: 'jsonString'}, //ERP-30963
    {name: 'individualproductprice'},
    {name: 'discountData'},
    {name: 'barcodetype'} ,  //ERM-304
    {name :'purchasetaxId'},
    {name :'isQAEnable'},
    {name: 'salestaxId'},
    {name:'itctype'}
    
]);
Wtf.FixedAssetStore = new Wtf.data.Store({
    url:"ACCProductCMN/getProductsForCombo.do",
    baseParams:{mode:22,common:'1',loadPrice:true,isFixedAsset:true},
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productRec)
});

Wtf.FixedAssetStoreOptimized = new Wtf.data.Store({
    url:"ACCProductCMN/getProductsForCombo.do",
    autoLoad:false,
    baseParams:{mode:22,common:'1',loadPrice:true,isFixedAsset:true},
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productRec)
});
//Wtf.FixedAssetStore.load();
Wtf.usersRec = new Wtf.data.Record.create([
    {name: 'userid'},
    {name: 'username'},
    {name: 'fname'},
    {name: 'lname'},
    {name: 'image'},
    {name: 'emailid'},
    {name: 'lastlogin',type: 'date'},
    {name: 'aboutuser'},
    {name: 'address'},
    {name: 'contactno'},
    {name: 'rolename'},
    {name: 'roleid'}
]);

Wtf.userds = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        totalProperty: 'count',
        root: "data"
    },Wtf.usersRec),
    url : "ProfileHandler/getAllUserDetails.do",
    baseParams:{
        mode:11
    }
});

Wtf.locationRec = new Wtf.data.Record.create([
    {name:"id"},
    {name:"name"},
    {name: 'parentid'},
    {name: 'parentname'}
]);

Wtf.locationReader = new Wtf.data.KwlJsonReader({
    root:"data"
},Wtf.locationRec);

Wtf.locationStore = new Wtf.data.Store({
        url:"ACCMaster/getLocationItems.do",
    reader:Wtf.locationReader
});

Wtf.detartmentRec = new Wtf.data.Record.create([
    {name:"id"},
    {name:"name"}
]);

Wtf.detartmentReader = new Wtf.data.KwlJsonReader({
    root:"data"
},Wtf.detartmentRec);

Wtf.detartmentStore = new Wtf.data.Store({
        url:"ACCMaster/getDepartments.do",
    reader:Wtf.detartmentReader
});


Wtf.productStore = new Wtf.data.Store({
//    url:Wtf.req.account+'CompanyManager.jsp',
    url:"ACCProductCMN/getProductsForCombo.do",
    baseParams:{
        mode:22,
        common:'1',
        excludeParent:true
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productRec)
});
Wtf.productStoreSales = new Wtf.data.Store({
        url:"ACCProductCMN/getProductsForCombo.do",
        baseParams:{
        	loadInventory:true
            },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });
Wtf.FixedAssetAndProductLeaseStore = new Wtf.data.Store({
    url:"ACCProductCMN/getProductsForCombo.do",
    baseParams:{
        loadInventory:true,
        includeBothFixedAssetAndProductFlag:true,
        excludeParent:true
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productRec)
});
Wtf.FixedAssetAndProductLeaseStoreOptimized = new Wtf.data.Store({
    url:"ACCProductCMN/getProductsForCombo.do",
    autoLoad:false,
    baseParams:{
        loadInventory:true,
        includeBothFixedAssetAndProductFlag:true,
        excludeParent:true
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productRec)
});
Wtf.productStoreOptimized = new Wtf.data.Store({
//    url:Wtf.req.account+'CompanyManager.jsp',
    url:"ACCProductCMN/getProductsForCombo.do",
    baseParams:{mode:22,common:'1',loadPrice:true,onlyProduct:true,excludeParent:true},
    autoLoad:false,
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },Wtf.productRec)
});

/*
    * IBG Transaction Code as given in DBS PDF File
    * 
    "20" - Sundry Credit
    "21" - Standing Instruction Credit
    "22" - Salary Credit
    "23" - Dividend Credit
    "24" - Inward Remittance Credit
    "25" - Bill Proceeds Credit
    "30" - Direct Debit <<-- currently our system does not support this transaction
    */

Wtf.ibgTransactionCodeStore = new Wtf.data.SimpleStore({
        fields: [
            {name:'ibgCode'}
        ],
        data :[
            ['20'],
            ['21'],
            ['22'],
            ['23'],
            ['24'],
            ['25'],
            ['30']
        ]
    });
    
Wtf.ibgBanksStore = new Wtf.data.SimpleStore({
        fields: [
            {name:'id'},
            {name:'name'}
        ],
        data :[
            [1,'Development Bank Of Singapore'],
            [2,'Commerce International Merchant Bankers']
        ]
    });
    
Wtf.cimbPurposeCodeStore = new Wtf.data.SimpleStore({
        fields: [{name: "id"}, {name: "name"}],
        data: [[1, "COLL"], [2, "COMC"],[3,"SALA"]]
});
Wtf.productStoreSalesOptimized = new Wtf.data.Store({
        url:"ACCProductCMN/getProductsForCombo.do",
        autoLoad:false,
        baseParams:{
        	loadInventory:true,
                onlyProduct:true,
                excludeParent:true
            },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });    


function loadGlobalStores(){

	Wtf.delTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.rem.105")],[1,WtfGlobal.getLocaleText("acc.rem.106")],[2,WtfGlobal.getLocaleText("acc.rem.107")]]
    });
    Wtf.activeDormantStore = new Wtf.data.SimpleStore({
        fields: [{name: 'typeid', type: 'int'}, 'name'],
        data: [[-1, WtfGlobal.getLocaleText("acc.vendor.All")], [1, WtfGlobal.getLocaleText("acc.vendor.activeVendor")], [2, WtfGlobal.getLocaleText("acc.vendor.dormantVendor")]]
    });
    Wtf.activeDormantCustomerStore = new Wtf.data.SimpleStore({
        fields: [{name: 'typeid', type: 'int'}, 'name'],
        data: [[-1, WtfGlobal.getLocaleText("acc.vendor.All")], [1, WtfGlobal.getLocaleText("acc.customer.activeCustomer")], [2, WtfGlobal.getLocaleText("acc.customer.dormantCustomer")]]
    });

	Wtf.intervalTypeStore = new Wtf.data.SimpleStore({
	    fields: ["id", "name"], //Dont't Change id field values [SK]
	    data :[["day",WtfGlobal.getLocaleText("acc.rem.108")],["week",WtfGlobal.getLocaleText("acc.rem.109")],["month",WtfGlobal.getLocaleText("acc.fixedAssetList.month")]]
	});
            this.usersRec = new Wtf.data.Record.create([
            {name: 'userid'},
            {name: 'username'},
            {name: 'fname'},
            {name: 'lname'},
            {name: 'image'},
            {name: 'emailid'},
            {name: 'lastlogin',type: 'date'},
            {name: 'aboutuser'},
            {name: 'address'},
            {name: 'contactno'},
            {name: 'rolename'},
            {name: 'roleid'},
            {name: 'id',mapping:'userid'},
            {name: 'name',mapping:'fullname'}
        ]);
                Wtf.userds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            },this.usersRec),
            url : "ProfileHandler/getAllUserDetails.do",
            baseParams:{
                mode:11
            }
        });
        Wtf.userds.load();

}


Wtf.grid.CheckColumn = function(config){
    Wtf.apply(this, config);
    if(!this.id)
        this.id = Wtf.id();
    this.renderer = this.renderer.createDelegate(this);
};
Wtf.grid.CheckColumn.prototype ={
    fyear:0,
    byear:0,
    fdate:0,
    bdate:0,
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    onMouseDown : function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            this.grid.fireEvent("afteredit",{
                grid:this.grid,
                record:record,
                field:this.dataIndex,
                value:!record.data[this.dataIndex],
                originalValue:record.data[this.dataIndex],
                row:index,
                column:0//Not known
            });
        }
    },
    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td';
        return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
    }
};

Wtf.grid.CheckColumnCreditTerm = function(config){
    Wtf.apply(this, config);
    if(!this.id)
        this.id = Wtf.id();
    this.renderer = this.renderer.createDelegate(this);
};
Wtf.grid.CheckColumnCreditTerm.prototype ={
    fyear:0,
    byear:0,
    fdate:0,
    bdate:0,
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    onMouseDown : function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            var flag = record.data[this.dataIndex];
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            for(var i=0 ; i<this.grid.store.getCount()-1 ; i++){
                if(i!=index){
                    record = this.grid.store.getAt(i);
                    if(flag==false){
                        record.set(this.dataIndex, false);
                    }
                }
            }
        }
    },
    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td';
        return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
    }
};

Wtf.grid.CheckColumnCustomized = function(config){
    Wtf.apply(this, config);
    if(!this.id)
        this.id = Wtf.id();
    this.renderer = this.renderer.createDelegate(this);
};
Wtf.grid.CheckColumnCustomized.prototype ={
    fyear:0,
    byear:0,
    fdate:0,
    bdate:0,
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    onMouseDown : function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            
            if(Wtf.account.companyAccountPref.countryid == Wtf.CountryID.MALAYSIA && record.data.fieldDataIndex=="appliedGst" && (this.dataIndex =="isreadonlycol" ||this.dataIndex =="hidecol")){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.customfield.lineitem.isHideOrRead.alert")], 2);
                return;
            }
            /*
             *An alert when user try to change the value of child fields.
             **/
            if(record.data.parentid){
                var header="";
                for(var i=0;i<this.grid.store.data.length;i++){
                    if(this.grid.store.getAt(i).data.id == record.data.parentid){
                        header = this.grid.store.getAt(i).data.fieldname;
                        break;                    
                    }
                }
                var msg = WtfGlobal.getLocaleText("acc.checkboxitem.childFieldNotEditable")+"<br>"+WtfGlobal.getLocaleText("acc.checkboxitem.childField")+" <b>"+header+"</b>.<br>" +WtfGlobal.getLocaleText("acc.checkboxitem.parentField")+" <b>"+header+"</b>."
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),msg], 2);
                return; 
            }            
            if(record.data.fieldDataIndex=="includeprotax" && (this.dataIndex =="isUserManadatoryField")){
                for(var i=0;i<this.grid.store.data.length;i++){
                    if(this.grid.store.getAt(i).data.fieldDataIndex == "includetax" && this.grid.store.getAt(i).data.isUserManadatoryField){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.customfield.defaultitem.taxesMandatory")], 2);
                        return
                    }
                }
            }
            if(record.data.fieldDataIndex=="includetax" && (this.dataIndex =="isUserManadatoryField")){
                for(var i=0;i<this.grid.store.data.length;i++){
                    if(this.grid.store.getAt(i).data.fieldDataIndex == "includeprotax" && this.grid.store.getAt(i).data.isUserManadatoryField){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.customfield.defaultitem.taxesMandatory")], 2);
                        return
                    }
                }
            }            
            if((record.data.fieldDataIndex=="applyGlobalDiscount" || record.data.fieldDataIndex=="autogenerateDO" || record.data.fieldDataIndex=="includingGST") && (this.dataIndex =="isUserManadatoryField")){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.checkboxitem.isMandatory.alert")], 2);
                return;
            }
            if(record.data.fieldDataIndex=="ShowOnlyOneTime" && (this.dataIndex =="isUserManadatoryField")){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.checkboxitem.isMandatory.alert")], 2);
                return;
            }
            if(record.get('isManadatoryField') && !(this.dataIndex == 'hidefieldfromreport')){
                return;
            }
            if ( record.get('isUserManadatoryField') && (this.dataIndex == "hidecol" || this.dataIndex == "isreadonlycol") ) {
                return;
            }
            if (this.dataIndex == 'isUserManadatoryField' && (record.get('isreadonlycol') || record.get('hidecol'))) {
                return;
            }
            if ((this.dataIndex == 'isreadonlycol' && record.get('hidecol'))||(this.dataIndex == 'hidecol' && record.get('isreadonlycol'))) {
                return;
            }
            if ( record.get('columntype') == 'Line Item(s)' && this.dataIndex == 'isUserManadatoryField' ) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.customfield.lineitem.isMandatory.alert")], 2);
                return;
            }
            if ( record.get('columntype') == 'Report Item(s)' && this.dataIndex == 'isUserManadatoryField' ) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.customfield.reportitem.isMandatory.alert")], 2);
                return;
            }
            if ((record.get('columntype') == 'Custom Field(s)' || record.get('columntype') == 'Dimension Field(s)') && this.dataIndex == 'isUserManadatoryField' ) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.customfield.custom/dimension.isMandatory.alert")], 2);
                return;
            }
            if ((record.get('columntype') == 'Custom Field(s)' || record.get('columntype') == 'Dimension Field(s)') && this.dataIndex == 'hidecol' &&  (Wtf.Acc_Contract_ModuleId==35 || Wtf.Acc_Lease_Contract==64) && !(Wtf.Acc_Invoice_ModuleId==2)) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.customfield.isFormField.custom/dimension.alert")], 2);
                return;
            }
            if ( record.get('columntype') == 'Report Item(s)' && this.dataIndex == 'hidecol' &&  (Wtf.Acc_PRO_ModuleId==34 || Wtf.Acc_Contract_ModuleId==35 || Wtf.Acc_Lease_Contract==64)) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.customfield.lineitem.isFormField.alert")], 2);
                return;
            }
            if ((record.get('isReportField')||record.get('columntype') == 'Custom Field(s)' || record.get('columntype') == 'Dimension Field(s)') && this.dataIndex == 'isreadonlycol' &&  (Wtf.Acc_PRO_ModuleId==34 || Wtf.Acc_Contract_ModuleId==35 || Wtf.Acc_Lease_Contract==64)) {
                return;
            }
            if (!record.get('isReportField') && this.dataIndex == 'hidefieldfromreport' && (Wtf.Acc_PRO_ModuleId == 34 || Wtf.Acc_Contract_ModuleId == 35 || Wtf.Acc_Lease_Contract == 64)) {
                if ((record.get('columntype') == 'Custom Field(s)' || record.get('columntype') == 'Dimension Field(s)')) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"), WtfGlobal.getLocaleText("acc.customfield.isFormField.custom/dimension.report.alert")], 2);
                    return;
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"), WtfGlobal.getLocaleText("acc.customfield.defaultitem.isFormField.alert")], 2);
                    return;
                }
            }
            /*
             *If the value of parent field changed in hide/show window, also changing the value of its child fields
             **/
            for(var i=0;i<this.grid.store.data.length;i++){
                if(this.grid.store.getAt(i).data.parentid == record.data.id){
                    
                    if (this.grid.store.getAt(i).data.isReportField) {
                        this.grid.store.getAt(i).set("hidefieldfromreport", !record.data[this.dataIndex]);
                    } else {
                    this.grid.store.getAt(i).set(this.dataIndex, !record.data[this.dataIndex]);
                    }
                    
                }
            }
            
            /*
             *Setting global level fields 'Apply global Discount', 'Discount' and 'Discount Type'  according to
             *line level 'Discount' field.(This is an unique case handled for line level Discount field)
             **/
            if(record.data.fieldDataIndex=="prdiscount"){
                for(var i=0;i<this.grid.store.data.length;i++){
                    if(this.grid.store.getAt(i).data.fieldDataIndex == "applyGlobalDiscount" || this.grid.store.getAt(i).data.fieldDataIndex == "globaldiscount" || this.grid.store.getAt(i).data.fieldDataIndex == "globalDiscountType" || this.grid.store.getAt(i).data.fieldDataIndex == "globalDiscountType" || this.grid.store.getAt(i).data.fieldDataIndex == "discountispercent" || this.grid.store.getAt(i).data.fieldDataIndex == "discountamount"){
                        this.grid.store.getAt(i).set(this.dataIndex, !record.data[this.dataIndex]);
                        if(this.dataIndex == "hidecol" && this.grid.store.getAt(i).data.fieldDataIndex != "discountispercent" && this.grid.store.getAt(i).data.isreadonlycol){
                            this.grid.store.getAt(i).set("isreadonlycol", record.data[this.dataIndex]);
                        }
                        else if(this.dataIndex == "isreadonlycol" && this.grid.store.getAt(i).data.fieldDataIndex != "discountispercent" && this.grid.store.getAt(i).data.isreadonlycol){
                            this.grid.store.getAt(i).set("hidecol", record.data[this.dataIndex]);
                        }
                }
                }    
               
            }
            /*
             *Setting global level fields 'Apply global Discount', 'Discount' and 'Discount Type'  according to
             *line level 'Discount Type' field.(This is an unique case handled for line level Discount Type field)
             **/
            else if(record.data.fieldDataIndex=="discountispercent" ){
                for(var i=0;i<this.grid.store.data.length;i++){
                    if(this.grid.store.getAt(i).data.fieldDataIndex == "applyGlobalDiscount" || this.grid.store.getAt(i).data.fieldDataIndex == "globaldiscount" || this.grid.store.getAt(i).data.fieldDataIndex == "globalDiscountType" || this.grid.store.getAt(i).data.fieldDataIndex == "globalDiscountType" || this.grid.store.getAt(i).data.fieldDataIndex == "prdiscount" || this.grid.store.getAt(i).data.fieldDataIndex == "discountamount"){
                        this.grid.store.getAt(i).set(this.dataIndex, !record.data[this.dataIndex]);
                        if(this.dataIndex == "hidecol" && this.grid.store.getAt(i).data.fieldDataIndex != "prdiscount"  && this.grid.store.getAt(i).data.isreadonlycol){
                            this.grid.store.getAt(i).set("isreadonlycol", record.data[this.dataIndex]);
                        }
                        else if(this.dataIndex == "isreadonlycol" && this.grid.store.getAt(i).data.fieldDataIndex != "prdiscount"  && this.grid.store.getAt(i).data.isreadonlycol){
                            this.grid.store.getAt(i).set("hidecol", record.data[this.dataIndex]);
                        }
                    }
                }              
            }
            
            
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            this.grid.fireEvent("afteredit",{
                grid:this.grid,
                record:record,
                field:this.dataIndex,
                value:!record.data[this.dataIndex],
                originalValue:record.data[this.dataIndex],
                row:index,
                column:0//Not known
            });
        }
    },
    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td';
        return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
    }
};

function getTopHtml(text, body,img,isgrid,margin){
    if(isgrid===undefined)isgrid=false;
    if(margin===undefined)margin='15px 0px 10px 10px';
     if(img===undefined||img==null) {
        img = '../../images/createuser.png';
    }
     var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                    +"<div style='float:left;height:100%;width:auto;position:relative;'>"
                    +"<img src = "+img+"  class = 'adminWinImg'></img>"
                    +"</div>"
                    +"<div style='float:left;height:100%;width:80%;position:relative;'>"
                    +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
                    +"<div style='font-size:10px;float:left;margin:2mm 0px 3mm 3mm;width:100%;position:relative;'>"+body+"</div>"
                        +(isgrid?"":"<div class='medatory-msg'>"+WtfGlobal.getLocaleText("acc.changePass.reqFields")+"</div>")
                        +"</div>"
                    +"</div>" ;
     return str;
}

function deleteHoliday(obj, admin){
    Wtf.MessageBox.confirm('Confirm', 'Are you sure you would like to delete the holiday?', function(btn){
        if(btn == "yes")
            Wtf.getCmp(admin).deleteHoliday(obj.id.substring(4));
        },
    this);
}

function cancelHoliday(){
    Wtf.get("addHoliday").dom.style.display = 'none';
}
function addHoliday(admin){
    Wtf.getCmp(admin).addHoliday();
}

function showChart(id1,dataflag,swf,xmlpath,persongroup,isagedgraph,withinventory,nondeleted,deleted,year){
	var comp  =Wtf.getCmp(id1);
	if(comp){
		if(comp.rendered){				// Send year field selected in the year combo      Neeraj
            var pid = comp.body.dom.id;
            var personlimit=1;
//            var data= "ACCChart/getTopCustomerChart.do";//&personlimit="+personlimit+"&creditonly=true&persongroup="+persongroup+"&isagedgraph="+isagedgraph+"&withinventory="+withinventory;
            var data= dataflag+".do?personlimit="+personlimit+"&creditonly=true&persongroup="+persongroup+"&isagedgraph="+isagedgraph+"&withinventory="+withinventory+"&nondeleted="+nondeleted+"&deleted="+deleted+"&year="+year;
            createNewChart(swf,'krwpie', '100%', '100%', '8', '#FFFFFF', xmlpath,data, pid);
		}else{
	        comp.on("render", function(){
	            var pid = Wtf.getCmp(id1).body.dom.id;
	            var personlimit=1;
	//            var data= "ACCChart/getTopCustomerChart.do";//&personlimit="+personlimit+"&creditonly=true&persongroup="+persongroup+"&isagedgraph="+isagedgraph+"&withinventory="+withinventory;
	            var data= dataflag+".do?personlimit="+personlimit+"&creditonly=true&persongroup="+persongroup+"&isagedgraph="+isagedgraph+"&withinventory="+withinventory+"&nondeleted="+nondeleted+"&deleted="+deleted;
	            createNewChart(swf,'krwpie', '100%', '100%', '8', '#FFFFFF', xmlpath,data, pid);
	        }, this);
		}
    }
}

function removeDuplicateParameters(parameters){
    var resultStr = "";
    var result= new Array();
    var keyValuesForParameters;
    /* 
     * For Check first parameter is blank in url.
     * checkBlankParameter=false (If first parameter in url is blank) (ERP-12576 for this issue first parameter is blank)
     * checkBlankParameter=true  (If first parameter in url is not blank)
     */
    var checkBlankParameter=true;  
    keyValuesForParameters=parameters.split('&');
    var len=keyValuesForParameters.length;
    for(var i=0;i<len;i++){
        var parameter;
        parameter=keyValuesForParameters[i];
        var len1=result.length;
        var isPresent=false;
        for(var j=0;j<len1;j++){   //checking for duplicate key value pairs for parameters
            if(result[j]==parameter){
                isPresent=true;
                break; 
            }
        }
        if(!isPresent){
            if(keyValuesForParameters[0]==""){
                checkBlankParameter=false;
            }
            if(i!=0 && !checkBlankParameter){  //not inserting first value because it is blank
                result[i-1]=parameter;
                resultStr+="&"+parameter; 
            }
            /*
             *if First Parameter is blank in url
             */
            if(checkBlankParameter){
                result[i]=parameter;
                resultStr+="&"+parameter; 
            }
                
        }
    }
    return resultStr;
}

function globalChart(id,id1,swf,dataflag,mainid,xmlpath,withinventory){
    var reportPanel =Wtf.getCmp(id);
    if(reportPanel==null){
        reportPanel = new Wtf.Panel({
            id: id,
            border : false,
            title : WtfGlobal.getLocaleText("acc.rem.166"),  //"Chart View",
            autoScroll:true,
            layout:'border',
            closable: true,
            style:'padding:50px',
            defaults:{border:false},
            iconCls:(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart'),
            items:[new Wtf.Panel({
                id:"msgid",
                region:"south",
                width:10,
                baseCls:"chartmsg",
                html:"Note: Amount in <span class='currency-view'>"+WtfGlobal.getCurrencySymbol()+"</span>",
                border : false,
                frame:false
            }),new Wtf.Panel({
                id:id1,
                region:"center",
                defaults:{border:false},
                border : false,
                frame:false
            })]
        });
        showChart(id1,dataflag,swf,xmlpath,false,false,withinventory);
        Wtf.getCmp(mainid).add(reportPanel);
    }
    Wtf.getCmp(mainid).setActiveTab(reportPanel);
    Wtf.getCmp(mainid).doLayout();
}

function getBookBeginningYear(){
	var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
    var ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)
    ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()

    var data=[];
    var newrec;
    if(ffyear==null||ffyear=="NaN"){ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)}
    var year=ffyear.getFullYear();
    data.push([0,year])
    if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
        data.push([1,year+1]);
        newrec = new Wtf.data.Record({id:1,yearid:year+1});
    }
    return data;
}
function serialRenderer(){
    return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
}
function viewRenderer(v,m,rec){
    return "<div class='view pwnd view-gridrow'  title='View Asset Details '></div>";
}

function globalAgedChart(id,id1,swf1,dataflag1,mainid,xmlpath1,id2,swf2,dataflag2,xmlpath2,withinventory,nondeleted,deleted){

	var yearComboData = getBookBeginningYear();

	var yearStore= new Wtf.data.SimpleStore({
		fields: [{name:'id',type:'int'}, 'yearid'],
		data:yearComboData
	});

	var reportPanel =Wtf.getCmp(id);
    if(reportPanel==null){
    	var year = new Wtf.form.ComboBox({
    	    store: yearStore,
    	    fieldLabel:'Year',
    	    name:'yearid',
    	    displayField:'yearid',
    	    anchor:'40%',
//    	    valueField:'yearid',
    	    forceSelection: true,
    	    mode: 'local',
    	    triggerAction: 'all',
    	    selectOnFocus:true,
    	    emptyText: "Select a Year......."
    	});
    	year.on('select',function(c){
    		showChart(id1,dataflag1,swf1,xmlpath1,false,true,withinventory,nondeleted,deleted,c.getValue());
    	    showChart(id2,dataflag2,swf2,xmlpath2,true,true,withinventory,nondeleted,deleted,c.getValue());
    	});
        reportPanel = new Wtf.Panel({
            id: id,
            border : false,
            title : WtfGlobal.getLocaleText("acc.rem.166"),  //"Chart View",
            autoScroll:true,
            layout:'border',
            closable: true,
            bodyStyle:'padding:20px',
            defaults:{border:false},
            iconCls:(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart'),
            tbar:[new Wtf.Toolbar.Button({text:"Select Year"}),year],
            items:[new Wtf.Panel({
                id:"msgid"+id1,
                region:"south",
                width:10,
                baseCls:"chartmsg",
                html:"<b>Note:</b> Amount in <span class='currency-view'>"+WtfGlobal.getCurrencySymbol()+"  ("+WtfGlobal.getCurrencyName()+")</span>",
                border : false,
                frame:false
            }),new Wtf.Panel({
                    id:id1,
                    region:"center",
                    defaults:{border:false},
                    border : false,
                frame:false
            }),new Wtf.Panel({
                id:id2,
                region:"east",
                width:600,
                defaults:{border:false},
                border : false,
                frame:false
            })]
        });

        showChart(id1,dataflag1,swf1,xmlpath1,false,true,withinventory,nondeleted,deleted);
        showChart(id2,dataflag2,swf2,xmlpath2,true,true,withinventory,nondeleted,deleted);
        Wtf.getCmp(mainid).add(reportPanel);
    }
    Wtf.getCmp(mainid).setActiveTab(reportPanel);
    Wtf.getCmp(mainid).doLayout();
}

function chkcustaccload(){
    if(!Wtf.StoreMgr.containsKey("customer")){
        Wtf.customerAccStore.load();
        Wtf.StoreMgr.add("customer",Wtf.customerAccStore)
    }
}
function chkvenaccload(){
    if(!Wtf.StoreMgr.containsKey("ven")){
        Wtf.vendorAccStore.load();
        Wtf.StoreMgr.add("ven",Wtf.vendorAccStore)
    }

}
function chklabourdeptload(){
    if(!Wtf.StoreMgr.containsKey("labourdept")){
        Wtf.departmentStore.load();
        Wtf.StoreMgr.add("labourdept",Wtf.departmentStore)
    }
}
function chkworkCentreload(){
    if(!Wtf.StoreMgr.containsKey("workCentre")){
        Wtf.workCentreStore.load();
        Wtf.StoreMgr.add("workCentre",Wtf.workCentreStore)
    }
}
function chkkeySkillload(){
    if(!Wtf.StoreMgr.containsKey("keySkill")){
        Wtf.keySkillStore.load();
        Wtf.StoreMgr.add("keySkill",Wtf.keySkillStore)
    }
}
function chkProcessload(){
    if(!Wtf.StoreMgr.containsKey("Process")){
        Wtf.processStore.load();
        Wtf.StoreMgr.add("Process",Wtf.processStore)
    }
}
function chkvendorload(){
    if(!Wtf.StoreMgr.containsKey("Vendor")){
        Wtf.vendorAccRemoteStore.load();
        Wtf.StoreMgr.add("Vendor",Wtf.vendorAccRemoteStore)
    }
}
function chkPurchaseAccload(){
    if(!Wtf.StoreMgr.containsKey("Purchase")){
        Wtf.purchaseAccStore.load();
        Wtf.StoreMgr.add("Purchase",Wtf.purchaseAccStore)
    }
}
function chkwcTypeload() {
    if (!Wtf.StoreMgr.containsKey("WorkCentreType")) {
        Wtf.workCenterTypeStore.load();
        Wtf.StoreMgr.add("WorkCentreType", Wtf.workCenterTypeStore)
    }
}
function chkwcproductload() {
    if (!Wtf.StoreMgr.containsKey("Product")) {
        Wtf.wcproductStore.load();
        Wtf.StoreMgr.add("Product", Wtf.wcproductStore)
    }
}
function chkMaterialload() {
    if (!Wtf.StoreMgr.containsKey("Material")) {
        Wtf.materialStore.load();
        Wtf.StoreMgr.add("Material", Wtf.materialStore)
    }
}
function chkLabourload() {
    if (!Wtf.StoreMgr.containsKey("Labour")) {
        Wtf.labourStore.load();
        Wtf.StoreMgr.add("Labour", Wtf.labourStore)
    }
}
function chkMachineload() {
    if (!Wtf.StoreMgr.containsKey("Machine")) {
        Wtf.machineStore.load();
        Wtf.StoreMgr.add("Machine", Wtf.machineStore)
    }
}
function chkwcLocationload() {
    if (!Wtf.StoreMgr.containsKey("WorkCentreLocation")) {
        Wtf.workCenterLocationStore.load();
        Wtf.StoreMgr.add("WorkCentreLocation", Wtf.workCenterLocationStore)
    }
}
function chkWarehouseload() {
    if (!Wtf.StoreMgr.containsKey("Warehouse")) {
        Wtf.inventoryStore.load();
        Wtf.StoreMgr.add("Warehouse", Wtf.inventoryStore)
    }
}
function chkworkTypeload() {
    if (!Wtf.StoreMgr.containsKey("workType")) {
        Wtf.workTypeStore.load();
        Wtf.StoreMgr.add("workType", Wtf.workTypeStore)
    }
}
function chkwcManagerload() {
    if (!Wtf.StoreMgr.containsKey("Manager")) {
        Wtf.workCenterManagerStore.load();
        Wtf.StoreMgr.add("Manager", Wtf.workCenterManagerStore)
    }
}
function chkroutingload() {
    if (!Wtf.StoreMgr.containsKey("routing")) {
        Wtf.routingStore.load();
        Wtf.StoreMgr.add("routing", Wtf.routingStore);
    }
}
function chkWOStatusload() {
    if (!Wtf.StoreMgr.containsKey("workOrderStatus")) {
        Wtf.WOStatusStore.load();
        Wtf.StoreMgr.add("workOrderStatus", Wtf.WOStatusStore);
    }
}
function chkWOTypeload() {
    if (!Wtf.StoreMgr.containsKey("workOrderType")) {
        Wtf.workOrderTypeStore.load();
        Wtf.StoreMgr.add("workOrderType", Wtf.workOrderTypeStore);
    }
}
function chklocationload() {
    if (!Wtf.StoreMgr.containsKey("JobWorkLocation")) {
        Wtf.locationStore.load();
        Wtf.StoreMgr.add("JobWorkLocation", Wtf.locationStore);
    }
}
function chkWorkOrderload() {
    if (!Wtf.StoreMgr.containsKey("WorkOrder")) {
        Wtf.workOrderStore.load();
        Wtf.StoreMgr.add("WorkOrder", Wtf.workOrderStore);
    }
}
function chkSellerTypeload() {
    if (!Wtf.StoreMgr.containsKey("SellerType")) {
        Wtf.sellerTypeStore.load();
        Wtf.StoreMgr.add("SellerType", Wtf.sellerTypeStore);
    }
}
function chkContractStatusload() {
    if (!Wtf.StoreMgr.containsKey("ContractStatus")) {
        Wtf.contractStatusStore.load();
        Wtf.StoreMgr.add("ContractStatus", Wtf.contractStatusStore);
    }
}
function chkParentContractload() {
    if (!Wtf.StoreMgr.containsKey("ParentContract")) {
        Wtf.parentContractIdStore.load();
        Wtf.StoreMgr.add("ParentContract", Wtf.parentContractIdStore);
    }
}

function chkStatusload() {
    if (!Wtf.StoreMgr.containsKey("Status")) {
        Wtf.dostatusStore.load();
        Wtf.StoreMgr.add("Status", Wtf.dostatusStore);
    }
}
function chktaxload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("tax"+moduleid)){
        Wtf.taxStore.load(params);
        Wtf.StoreMgr.add("tax"+moduleid,Wtf.taxStore);
    }
}

function chksalesAccountload(){
    if(!Wtf.StoreMgr.containsKey("salesAccount")){
        Wtf.salesAccStore.load();
        Wtf.StoreMgr.add("salesAccount",Wtf.salesAccStore);
    }
}
function chkAccountload(){
    if(!Wtf.StoreMgr.containsKey("Account")){
        Wtf.accountStore.load();
        Wtf.StoreMgr.add("salesAccount",Wtf.accountStore);
    }
}
function chkProductTypeload(){
    if(!Wtf.StoreMgr.containsKey("productType")){
        Wtf.productTypeStore.load();
        Wtf.StoreMgr.add("productType",Wtf.productTypeStore)
    }
}
function chkUomload(){
    if(!Wtf.StoreMgr.containsKey("uom")){
        Wtf.uomStore.load();
        Wtf.StoreMgr.add("uom",Wtf.uomStore)
    }
}
function chkshipmentStatusStoreload(){
    if(!Wtf.StoreMgr.containsKey("shipmentStatusStore")){
        Wtf.shipmentStatusStore.load();
        Wtf.StoreMgr.add("shipmentStatusStore",Wtf.shipmentStatusStore)
    }
}
function chkdeliveryModeStoreload(){
    if(!Wtf.StoreMgr.containsKey("deliverymodemrp")){
        Wtf.deliveryModeStore.load();
        Wtf.StoreMgr.add("deliverymodemrp",Wtf.deliveryModeStore)
    }
}
function chkpackagingProfileTypeload(){
    if(!Wtf.StoreMgr.containsKey("packagingProfileType")){
        Wtf.packagingProfileTypeStore.load();
        Wtf.StoreMgr.add("packagingProfileType",Wtf.packagingProfileTypeStore)
    }
}
function chkReportingUomload(){
    if(!Wtf.StoreMgr.containsKey("reportingUOM")){
        Wtf.reportingUOMStore.load();
        Wtf.StoreMgr.add("reportingUOM",Wtf.reportingUOMStore)
    }
}
function chktermload(){
    if(!Wtf.StoreMgr.containsKey("term")){
        Wtf.termds.load();
        Wtf.StoreMgr.add("term",Wtf.termds)
    }
}
function chkaccgroupload() {
    if (!Wtf.StoreMgr.containsKey("accgrp")) {
        Wtf.accGrpoupStore.load();
        Wtf.StoreMgr.add("accgrp", Wtf.accGrpoupStore)
    }
}
function chktitleload(){
    if(!Wtf.StoreMgr.containsKey("title")){
        Wtf.TitleStore.load();
        Wtf.StoreMgr.add("title",Wtf.TitleStore);
    }
}
function chkShippingRouteload(){
    if(!Wtf.StoreMgr.containsKey("ShippingRoute")){
        Wtf.ShippingRouteStore.load();
        Wtf.StoreMgr.add("ShippingRoute",Wtf.ShippingRouteStore);
    }
}
function chkCustomerCategoryload(){
    if(!Wtf.StoreMgr.containsKey("CustomerCategory")){
        Wtf.CustomerCategoryStore.load();
        Wtf.StoreMgr.add("CustomerCategory",Wtf.CustomerCategoryStore);
    }
}
function chkInterCompanyTypeload(){
    if(!Wtf.StoreMgr.containsKey("InterCompanyType")){
        Wtf.InterCompanyTypeStore.load();
        Wtf.StoreMgr.add("InterCompanyType",Wtf.InterCompanyTypeStore);
    }
}
function chkVendorCategoryload(){
    if(!Wtf.StoreMgr.containsKey("VendorCategory")){
        Wtf.VendorCategoryStore.load();
        Wtf.StoreMgr.add("VendorCategory",Wtf.VendorCategoryStore);
    }
}
function chkCostCenterload(){   
    if(!Wtf.StoreMgr.containsKey("CostCenter")){
        Wtf.CostCenterStore.load();
        Wtf.StoreMgr.add("CostCenter",Wtf.CostCenterStore);
    }
}
function chkFormCostCenterload(){
    if(!Wtf.StoreMgr.containsKey("FormCostCenter")){
        Wtf.FormCostCenterStore.load();
        Wtf.StoreMgr.add("FormCostCenter",Wtf.FormCostCenterStore);
    }
}
function chkLineLevelCostCenterload(){
    if(!Wtf.StoreMgr.containsKey("LineLevelCostCenter")){ 
        Wtf.LineLevelCostCenterStore.load();
        Wtf.StoreMgr.add("LineLevelCostCenter",Wtf.LineLevelCostCenterStore);
    }
}
function chkAssetCategoryload(){
    if(!Wtf.StoreMgr.containsKey("AssetCategory")){
        Wtf.AssetCategoryStore.load();
        Wtf.StoreMgr.add("AssetCategory",Wtf.AssetCategoryStore);
    } else if(Wtf.dirtyStore.assetCategory) {
        Wtf.AssetCategoryStore.reload();
        Wtf.dirtyStore.assetCategory=false;
    }
}

function chkAssetDepartmentload(){
    if(!Wtf.StoreMgr.containsKey("AssetDepartment")){
        Wtf.AssetDepartmentStore.load();
        Wtf.StoreMgr.add("AssetDepartment",Wtf.AssetDepartmentStore);
    } else if(Wtf.dirtyStore.assetDepartment) {
        Wtf.AssetDepartmentStore.reload();
        Wtf.dirtyStore.assetDepartment=false;
    }
}

function chkSalesPersonload(){
    if(!Wtf.StoreMgr.containsKey("SalesPerson")){
        Wtf.salesPersonStore.load();
        Wtf.StoreMgr.add("SalesPerson",Wtf.salesPersonStore);
    } else if(Wtf.dirtyStore.salesPerson) {
        Wtf.salesPersonStore.reload();
        Wtf.dirtyStore.salesPerson=false;
    }
}
function chkAgentload(){
    if(!Wtf.StoreMgr.containsKey("Agent")){
        Wtf.agentStore.load();
        Wtf.StoreMgr.add("Agent",Wtf.agentStore);
    } else if(Wtf.dirtyStore.salesPerson) {
        Wtf.agentStore.reload();
        Wtf.dirtyStore.agentStore=false;
    }
}
function chkSONoload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("SONo"+moduleid)){
        Wtf.SONoStore.load(params);
        Wtf.StoreMgr.add("SONo"+moduleid,Wtf.SONoStore);
    }
}
function chkStoresload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("Stores"+moduleid)){
        Wtf.Stores.load(params);
        Wtf.StoreMgr.add("Stores"+moduleid, Wtf.Stores);
    }
}
function chkDONoload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("DONo"+moduleid)){
        Wtf.DONoStore.load(params);
        Wtf.StoreMgr.add("DONo"+moduleid,Wtf.DONoStore);
    } 
}
function chkCQNoload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("CQNo"+moduleid)){
        Wtf.CQNoStore.load(params);
        Wtf.StoreMgr.add("CQNo"+moduleid,Wtf.CQNoStore);
    } 
}
function chkSINoload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("SINo"+moduleid)){
        Wtf.SINoStore.load(params);
        Wtf.StoreMgr.add("SINo"+moduleid,Wtf.SINoStore);
    } 
}
function chkVQNoload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("VQNo"+moduleid)){
        Wtf.VQNoStore.load(params);
        Wtf.StoreMgr.add("VQNo"+moduleid,Wtf.VQNoStore);
    } 
}
function chkPONoload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("PONo"+moduleid)){
        Wtf.PONoStore.load(params);
        Wtf.StoreMgr.add("PONo"+moduleid,Wtf.PONoStore);
    } 
}
function chkGRNoload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("GRNo"+moduleid)){
//        Wtf.GRNoStore
        Wtf.GRNoStore.load(params);
        Wtf.StoreMgr.add("GRNo"+moduleid,Wtf.GRNoStore);
    } 
}
function chkPINoload(params, moduleid){
    if(!Wtf.StoreMgr.containsKey("PINo"+moduleid)){
        Wtf.PINoStore.load(params);
        Wtf.StoreMgr.add("PINo"+moduleid,Wtf.PINoStore);
    } 
}
function chkPRNoload(params, moduleid) {
    if (!Wtf.StoreMgr.containsKey("PRNo" + moduleid)) {
        Wtf.PRNoStore.load(params);
        Wtf.StoreMgr.add("PRNo" + moduleid, Wtf.PRNoStore);
    }
}
function chkDNload(params, moduleid) {
    if (!Wtf.StoreMgr.containsKey("DN" + moduleid)) {
        Wtf.DNStore.load(params);
        Wtf.StoreMgr.add("DN" + moduleid, Wtf.DNStore);
    }
}
function chkCNload(params, moduleid) {
    if (!Wtf.StoreMgr.containsKey("CN" + moduleid)) {
        Wtf.CNStore.load(params);
        Wtf.StoreMgr.add("CN" + moduleid, Wtf.CNStore);
    }
}
function chkRFQNoload(params, moduleid) {
    if (!Wtf.StoreMgr.containsKey("RFQNo" + moduleid)) {
        Wtf.RFQNoStore.load(params);
        Wtf.StoreMgr.add("RFQNo" + moduleid, Wtf.RFQNoStore);
    }
}
function chkmovementtypeload(){
    if(!Wtf.StoreMgr.containsKey("MovementType")){
        Wtf.movmentTypeStore.load();
        Wtf.StoreMgr.add("MovementType",Wtf.movmentTypeStore);
    } else if(Wtf.dirtyStore.salesPerson) {
        Wtf.movmentTypeStore.reload();
        Wtf.dirtyStore.MovementType=false;
    }
}

function chkloantypeload(){
    if(!Wtf.StoreMgr.containsKey("LoanType")){
        Wtf.LoanTypeStore.load();
        Wtf.StoreMgr.add("MovementType",Wtf.LoanTypeStore);
    } else if(Wtf.dirtyStore.salesPerson) {
       Wtf.LoanTypeStore.reload();
        Wtf.dirtyStore.LoanType=false;
    }
}
function chkloanCategoryLoad(){
    if(!Wtf.StoreMgr.containsKey("LoanCategory")){
        Wtf.LoanCategoryStore.load();
        Wtf.StoreMgr.add("MovementType",Wtf.LoanCategoryStore);
    } 
}

function chkAssignedToload(){
    if(!Wtf.StoreMgr.containsKey("AssignedTo")){
        Wtf.assignedToStore.load();
        Wtf.StoreMgr.add("AssignedTo",Wtf.assignedToStore);
    } else if(Wtf.dirtyStore.AssignedTo) {
        Wtf.assignedToStore.reload();
        Wtf.dirtyStore.AssignedTo=false;
    }
}

function chkAssetLocationload(){
    if(!Wtf.StoreMgr.containsKey("AssetLocation")){
        Wtf.AssetLocationStore.load();
        Wtf.StoreMgr.add("AssetLocation",Wtf.AssetLocationStore);
    } else if(Wtf.dirtyStore.assetLocation) {
        Wtf.AssetLocationStore.reload();
        Wtf.dirtyStore.assetLocation=false;
    }
}

 function chktimezoneload()
 {
     if(!Wtf.StoreMgr.containsKey("timezone")){
            Wtf.timezoneStore.load();
            Wtf.StoreMgr.add("timezone",Wtf.timezoneStore);
        }
 }
 function chkcountryload()
 {
     if(!Wtf.StoreMgr.containsKey("country")){
            Wtf.countryStore.load();
            Wtf.StoreMgr.add("country",Wtf.countryStore);
        }
 }
 function chkstateload()
 {
     if(!Wtf.StoreMgr.containsKey("state")){
            Wtf.stateStore.load();
            Wtf.StoreMgr.add("state",Wtf.stateStore);
        }
 }
  function chkcurrencyload(){
     if(!Wtf.StoreMgr.containsKey("currencystore")){
            Wtf.currencyStore.load();
            Wtf.StoreMgr.add("currencystore",Wtf.currencyStore);
        }
 }
 function chkproductload(){
    if(!Wtf.StoreMgr.containsKey("productstore")){
        if(!(Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_type_ahead)){
            Wtf.productStore.load();
            Wtf.StoreMgr.add("productstore",Wtf.productStore);
        }
        
    }
}
 function chkproductSalesload(){
    if(!Wtf.StoreMgr.containsKey("productstoresales")){ 
        if(!(Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_type_ahead)){
            Wtf.productStoreSales.load();
            Wtf.StoreMgr.add("productstoresales",Wtf.productStoreSales);            
    }
}
}
    function chkinventoryWarehouse(){
        if(!Wtf.StoreMgr.containsKey("inventoryStore")){
            Wtf.inventoryStore.load();
            Wtf.StoreMgr.add("inventoryStore",Wtf.inventoryStore);
        } else if(Wtf.dirtyStore.inventoryStore) {
            Wtf.inventoryStore.reload();
            Wtf.dirtyStore.inventoryStore=false;
        }
    }
    function chkinventoryLocation(){
        if(!Wtf.StoreMgr.containsKey("inventoryLocation")){
            Wtf.inventoryLocation.load();
            Wtf.StoreMgr.add("inventoryLocation",Wtf.inventoryLocation);
        } else if(Wtf.dirtyStore.inventoryLocation) {
            Wtf.inventoryLocation.reload();
            Wtf.dirtyStore.inventoryLocation=false;
        }
    }
function chkUsersload() {
    if (!Wtf.StoreMgr.containsKey("user")) {
        Wtf.userds.load();
        Wtf.StoreMgr.add("user", Wtf.userds);
    }
}
function chkProductBrandload() {
    if(!Wtf.productBrandForView){
        Wtf.productBrandForView = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, Wtf.productBrandRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 53
            }
        });
    }
    if (!Wtf.StoreMgr.containsKey("productBrandForView")) {
        Wtf.productBrandForView.load();
        Wtf.StoreMgr.add("productBrandForView", Wtf.productBrandForView);
    }
}
function chkProductCategoryload() {
    if (!Wtf.StoreMgr.containsKey("productCategory")) {
        Wtf.ProductCategoryStore.load();
        Wtf.ProductCategoryStore.on("load",function(){
            var productCategoryrecordForNoneValue =new Wtf.data.Record({
                id:'None',
                name:'None'
            });
            this.insert(0,productCategoryrecordForNoneValue);
        })
        Wtf.StoreMgr.add("productCategory", Wtf.ProductCategoryStore);
    }
}
function chkPaidToload() {
    if (!Wtf.StoreMgr.containsKey("paidto")) {
        Wtf.MPPaidToStore.load();
        Wtf.StoreMgr.add("paidto", Wtf.MPPaidToStore);
    }
}
function chkReceivedFromload() {
    if (!Wtf.StoreMgr.containsKey("receivefrom")) {
        Wtf.RPReceivedFromStore.load();
        Wtf.StoreMgr.add("receivefrom", Wtf.RPReceivedFromStore);
    }
}
function chkPaymentMethodload() {
    if (!Wtf.StoreMgr.containsKey("method")) {
        Wtf.pmtStore.load();
        Wtf.StoreMgr.add("method", Wtf.pmtStore);
    }
}
function chkLandingCostCategoryload() {
//    if (!Wtf.StoreMgr.containsKey("LandingCostCategory")) {
        Wtf.landingCostCategoryStore.load();
//        Wtf.StoreMgr.add("LandingCostCategory", Wtf.landingCostCategoryStore);
//    }
}
Wtf.apply(Wtf.form.VTypes, {
    daterange : function(val, field) {
        var date = field.parseDate(val);

        if(!date){
            return;
        }
        if (field.startDateField && (!this.dateRangeMax || (date.getTime() != this.dateRangeMax.getTime()))) {
            var start = Wtf.getCmp(field.startDateField);
            start.setMaxValue(date);
            start.validate();
            this.dateRangeMax = date;
        }
        else if (field.endDateField && (!this.dateRangeMin || (date.getTime() != this.dateRangeMin.getTime()))) {
            var end = Wtf.getCmp(field.endDateField);
            end.setMinValue(date);
            end.validate();
            this.dateRangeMin = date;
        }
        /*
         * Always return true since we're only using this vtype to set the
         * min/max allowed values (these are tested for after the vtype test)
         */
        return true;
    },

    password : function(val, field) {
        if (field.initialPassField) {
            var pwd = Wtf.getCmp(field.initialPassField);
            return (val == pwd.getValue());
        }
        return true;
    },

    passwordText : WtfGlobal.getLocaleText("acc.field.Passwordsdonotmatch")
});

Wtf.productDetailsGridIsEmpty = function(grid) {
        if(grid.getStore().getCount()==0) {
            return true;
        } else {
            if(grid.getStore().getCount()>1) {
                return false;
            } else {
                var rec = grid.getStore().getAt(0);
                return (rec.data.productid=="" ? true : false);
            }
        }
}

Wtf.comboBoxRenderer = function(combo) {
    return function(value) {
        var rec = WtfGlobal.searchRecord(combo.store, value, combo.valueField);
        if(rec == undefined || rec == null){
            rec = WtfGlobal.searchRecord(combo.store, value, combo.displayField);
        }
//        var idx = combo.store.find(combo.valueField, value);
//        if(idx == -1){
//            idx = combo.store.find(combo.displayField, value);
//        }
//        var rec = combo.store.getAt(idx);
        return rec!=undefined?rec.data[combo.displayField]:"";
    };
}
/*
 * 
 * @param {type} combo
 * @returns {Function}
 * Rendrer for cess types like ( value per thousand + cess %,Value per Thousand or CESS % whichever is higher etc.) for india ERP-37785
 * 
 */
Wtf.comboBoxRendererForCess = function (combo) {
    return function (value,md, rec, ri, ci, store) {
        var cessType = '';
        var taxtypeName = '';
        cessType = rec!=undefined && rec.data != undefined && rec.data.cessType != undefined ? rec.data.cessType : '';
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && cessType != undefined && cessType != '') {
            if (cessType == Wtf.CESSTYPE.NOT_APPLICABLE) {
                taxtypeName = Wtf.CESSTYPE_NAME.NOT_APPLICABLE;
            } else if (cessType == Wtf.CESSTYPE.PERCENTAGES) {
                taxtypeName = Wtf.CESSTYPE_NAME.PERCENTAGES;
            } else if (cessType == Wtf.CESSTYPE.VALUE_AND_CESSPERCENTAGES) {
                taxtypeName = Wtf.CESSTYPE_NAME.VALUE_AND_CESSPERCENTAGES;
            } else if (cessType == Wtf.CESSTYPE.HIGHER_VALUE_OR_CESSPERCENTAGES) {
                taxtypeName = Wtf.CESSTYPE_NAME.HIGHER_VALUE_OR_CESSPERCENTAGES;
            } else if (cessType == Wtf.CESSTYPE.VALUE) {
                taxtypeName = Wtf.CESSTYPE_NAME.VALUE;
            }
        } else {
            var rec = WtfGlobal.searchRecord(combo.store, value, combo.valueField);
            if (rec == undefined || rec == null) {
                rec = WtfGlobal.searchRecord(combo.store, value, combo.displayField);
            }
            taxtypeName = rec != undefined ? rec.data[combo.displayField] : "";
        }
        md.attr = 'wtf:qtip="'+taxtypeName+'"';       
        return taxtypeName;

    };
},
Wtf.newcomboBoxRenderer = function(combo) {
    return function(value) {
        var rec = WtfGlobal.duplicatesearchRecord(combo.store, value, combo.valueField);
        if(rec == undefined || rec == null){
            rec = WtfGlobal.duplicatesearchRecord(combo.store, value, combo.displayField);
        }
        return rec!=undefined?rec.data[combo.displayField]:"";
    };
}
Wtf.applySequenceRenderer = function(val, cell, row, rowIndex, colIndex, ds) {
        var storecount=ds.data.items.length;
        var index ="";
        if(ds.data.items[0].data.productid!=undefined){
            index = WtfGlobal.searchRecordIndex(ds,"","productid");
        }
        if(ds.data.items[0].data.accountid!=undefined){
            index = WtfGlobal.searchRecordIndex(ds,"","accountid");
        }    
        if(ds.data.items[0].data.termid!=undefined){
            index = WtfGlobal.searchRecordIndex(ds,"","termid");
        }    
        if(ds.data.items[0].data.methodid!=undefined){
            index = WtfGlobal.searchRecordIndex(ds,"","methodid");
        }
        /*---Will execute in case of MP & RP--- */
        if(ds.data.items[0].data.documentid!=undefined){
            index = WtfGlobal.searchRecordIndex(ds,"","documentid");
        } 
     
        if(index == -1){    //for increasing counter in case of blank record not added
            storecount++;
        }
        var str = "";
        if(rowIndex<storecount-2) {
            str +=  '<div class=\'pwndBar2 shiftrowdownIcon\'></div>';
        }
        if(rowIndex>0 && rowIndex!=storecount-1) {
            str += ' <div class=\'pwndBar2 shiftrowupIcon\'></div>';
        }
        return str;
}
Wtf.comboBoxRendererwithClearFilter = function(combo) {
    return function(value) {
         combo.store.clearFilter();
        var idx = combo.store.find(combo.valueField, value);
        if(idx == -1)
            return "";
        var rec = combo.store.getAt(idx);
        return rec.data[combo.displayField];
    };
}
Wtf.MulticomboBoxRenderer = function(combo) {
    return function(value) {
        var idx;
        var rec;
        var valStr = "";
         combo.store.clearFilter();
        if (value != undefined && value != "") {
            var valArray = value.split(",");
            for (var i = 0; i < valArray.length; i++) {
                idx = WtfGlobal.searchRecordIndex(combo.store,valArray[i],combo.valueField);
                if(idx == -1){
                    idx = WtfGlobal.searchRecordIndex(combo.store,valArray[i],combo.displayField);
                }
                if (idx != -1) {
                    rec = combo.store.getAt(idx);
                    if (i == (valArray.length - 1))
                        valStr += rec.get(combo.displayField)
                    else
                        valStr += rec.get(combo.displayField) + ", ";
                }
            }
        }
        return valStr;
    }
}

Wtf.commonWaitMsgBox = function(msg) {
    Wtf.MessageBox.show({
        msg: msg,
        width:290,
        wait:true,
        title:WtfGlobal.getLocaleText("acc.common.load"),  //"Processing your request. Please wait...",
        waitConfig: {interval:200}
    });
}
 Wtf.apply(Wtf.form.ComboBox.prototype, {   
    maxHeight:400
});

Wtf.updateProgress =function() {
    Wtf.MessageBox.hide();
}

function setZeroToBlank(field){
        if(field.getValue()==0){
           field.setValue("");
        }
    }
    
function setDldUrl(u){
    document.getElementById('downloadframe').src = u;
}
function openDldUrl(u){
	window.open (u,'_blank');
}


Wtf.sequenceFormatStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:"count"
                
    },new Wtf.data.Record.create([
    {
        name: 'id'
    },

    {
        name: 'value'
    }
    ])),
    url : "ACCCompanyPref/getSequenceFormatStore.do"
});

function getRoundofValue(val){
      val=Number(val);
      val = val + 1/Math.pow(10,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL+Wtf.After_Decimal);
      var newnumber = Math.round(val*Math.pow(10,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL))/Math.pow(10,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
      return newnumber;
    
}
function getRoundofValueWithValues(val,no){
      val=Number(val);
      val = val + 1/Math.pow(10,no+Wtf.After_Decimal);
      var newnumber = Math.round(val*Math.pow(10,no))/Math.pow(10,no);
      return newnumber;
    
}
function getRoundedAmountValue(val){
      val=Number(val);
      val = val + 1/Math.pow(10,Wtf.AMOUNT_DIGIT_AFTER_DECIMAL+Wtf.After_Decimal); 
      var newnumber = Math.round(val*Math.pow(10,Wtf.AMOUNT_DIGIT_AFTER_DECIMAL))/Math.pow(10,Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
      return newnumber; 
}
function getSequenceFormatWin(obj){
       return new Wtf.form.ComboBox({
//            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'value',
            store:Wtf.sequenceFormatStore,
//            addNoneRecord: true,
            width : 240,
            forceSelection: true,
            selectOnFocus:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
//            emptyText: 'Select Sequence Format...',
            name:'sequenceformat',
            hiddenName:'sequenceformat',
            disabled:(obj.isEdit&&!obj.copyInv&&!obj.isPOfromSO&&!obj.isSOfromPO?true:false),
            listeners:{
                'select':{
                    fn:obj.getNextSequenceNumber,
                    scope:obj
                }
            }            
        });
}

Wtf.form.DateField.override({
    altFormatsdayfirst : "d/m/Y|d-m-y|d-m-Y|d/m|d-m|md|dmy|dmY|d|Y-m-d", 
    parseDate : function(value){
        if(!value || value instanceof Date){
            return value;
        }
        var v = Date.parseDate(value, this.format);
        if(!v && this.altFormatsdayfirst){
            if(!this.altFormatsdayfirstArray){
                this.altFormatsdayfirstArray = this.altFormatsdayfirst.split("|");
            }
            for(var i = 0, len = this.altFormatsdayfirstArray.length; i < len && !v; i++){
                v = Date.parseDate(value, this.altFormatsdayfirstArray[i]);
            }
        }
        return v;
    }    
});

Wtf.data.ExtField = function(config){
    if(typeof config == "string"){
        config = {name: config};
    }
    Wtf.apply(this, config);
    
    if(!this.type){
        this.type = "auto";
    }
    
    var st = Wtf.data.SortTypes;
    
    if(typeof this.sortType == "string"){
        this.sortType = st[this.sortType];
    }
    
    
    if(!this.sortType){
        switch(this.type){
            case "string":
                this.sortType = st.asUCString;
                break;
            case "date":
                this.sortType = st.asDate;
                break;
            default:
                this.sortType = st.none;
        }
    }

    
    var stripRe = /[\$,%]/g;

    
    
    if(!this.convert){
        var cv, dateFormat = this.dateFormat;
        switch(this.type){
            case "":
            case "auto":
            case undefined:
                cv = function(v){return v;};
                break;
            case "string":
                cv = function(v){return (v === undefined || v === null) ? '' : String(v);};
                break;
            case "int":
                cv = function(v){
                    return v !== undefined && v !== null && v !== '' ?
                           parseInt(String(v).replace(stripRe, ""), 10) : '';
                    };
                break;
            case "float":
                cv = function(v){
                    return v !== undefined && v !== null && v !== '' ?
                           parseFloat(String(v).replace(stripRe, ""), 10) : ''; 
                    };
                break;
            case "bool":
            case "boolean":
                cv = function(v){return v === true || v === "true" || v == 1;};
                break;
            case "date":
                cv = function(v){
                    if(!v){
                        return '';
                    }
                    if(v instanceof Date){
                        return v;
                    }
                    if(dateFormat){
                        if(dateFormat == "timestamp"){
                            return new Date(v*1000);
                        }
                        if(dateFormat == "time"){
                            if(isNaN(v)){
                                return new Date(v).format(WtfGlobal.getOnlyDateFormat());
                            }else{
                                return new Date(parseInt(v, 10));
                            }
                        }
                        return Date.parseDate(v, dateFormat);
                    }
                    var parsed = Date.parse(v);
                    return parsed ? new Date(parsed) : null;
                };
             break;
            
        }
        this.convert = cv;
    }
};

Wtf.data.ExtField.prototype = {
    dateFormat: null,
    defaultValue: "",
    mapping: null,
    sortType : null,
    sortDir : "ASC"
}

// Define Store when there is Yearly post method is selected but user want to post by both way Yearly as well as Monthly
Wtf.postOptionStore = new Wtf.data.SimpleStore({
    fields: [{
        name:'postoptionid',
        type:'int'
    }, 'name'],
    data :[[1,"Yearly"],[2,"Monthly"]]
});

//Accounting period and tax period setup

Wtf.TaxAccountingPeriods={
    YEAR :1,
    QUARTER :2,
    MONTHS:3,
    FULLYEAR:4
}

Wtf.getComboNameRenderer = function(combo){
    return function (value, metadata, record, row, col, store) {
        var idx = searchInComboStore(combo.valueField, value, combo.store);
        var fieldIndex = combo.comboFieldDataIndex;
        if (idx == -1) {
            if (record.data[combo.comboFieldDataIndex] && record.data[fieldIndex].length > 0) {
                return record.data[fieldIndex];
            }
            else
                return "";
        }
        var rec = combo.store.getAt(idx);
        var displayField = rec.get(combo.displayField);
        record.set(fieldIndex + "id", value);
        return displayField;
    }
}

function searchInComboStore(valueField, value, store) {
    var index = store.findBy(function(record) {
        if(record.get(valueField)==value)
            return true;
        else
            return false;
    });
    return index;
}

/**
 * hideUnitPriceAmount -> this flag should be passes as true if the user does not have permission to view unit price and amounts in transaction(s)
 * if hideUnitPriceAmount flag's value is passed true then amounts are hidden in the Tooltip which is created in this function
 * @param {type} v
 * @param {type} m
 * @param {type} rec
 * @param {type} hideUnitPriceAmount
 * @returns {String}
 */
function getToolTipOfTermsfun(v, m, rec, hideUnitPriceAmount){
    var table="";
    var termDetails="";
      if(!Wtf.isEmpty(rec.data.LineTermdetails)){
          termDetails=rec.data.LineTermdetails;
      }
    if(!Wtf.isEmpty(termDetails)){
        var symbol = !Wtf.isEmpty(rec.data.currencysymbol)?rec.data.currencysymbol:WtfGlobal.getCurrencySymbol();
        var jsonData = eval(termDetails);//JSON.parse(termDetails);
        if (WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW) {
            table = "<table cellspacing=10>" +
                    "<tr>" +
                    "<td><b>Tax Description</b></td>" +
                    "<td><b>Tax&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b></td>" +
                    "<td><b>Tax Type</b></td>" +
                    "<td><b>Amount</b></td>" +
                    "<td><b>Assessable Value</b></td>" +
                    "<td><b>Tax Amount</b></td>" +
                    "<td><b>Account</b></td>" +
                    "</tr>";
        } else {
            table = "<table cellspacing=10>" +
                    "<tr>" +
                    "<td><b>Tax Description</b></td>" +
                    "<td><b>Tax&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</b></td>" +
                    "<td><b>Tax Type</b></td>" +
                    "<td><b>Amount</b></td>" +
                    "<td><b>Assessable Value</b></td>" +
                    "<td><b>Tax Amount</b></td>" +
                    "<td><b>Account</b></td>" +
                    "<td><b>Purchase Value/ Sale Value</b></td>" +
                    "<td><b>Deduction/ Abatement %</b></td>" +
                    "</tr>";
        }

        //taxvalue
        for (var i = 0; i < jsonData.length; i++) {
            var term=!Wtf.isEmpty(jsonData[i].term)?jsonData[i].term:"";
            var termpercentage=!Wtf.isEmpty(jsonData[i].termpercentage)?jsonData[i].termpercentage:"";
            var termamount=!Wtf.isEmpty(jsonData[i].termamount)?jsonData[i].termamount:"";
            if (hideUnitPriceAmount) {//If user does not have permission to view unit price, then term amount is also hidden from user
                termamount = Wtf.UpriceAndAmountDisplayValue;
            } else if(termamount < 0){
                termamount = symbol +" "+ WtfGlobal.conventInDecimalWithoutSymbol(termamount);
            } else {
                termamount = WtfGlobal.conventInDecimal(termamount,symbol);
            }
            var glaccountname=!Wtf.isEmpty(jsonData[i].glaccountname)?jsonData[i].glaccountname:"";
            var deductionorabatementpercent=!Wtf.isEmpty(jsonData[i].deductionorabatementpercent)?jsonData[i].deductionorabatementpercent:"";
            var purchasevalueorsalevalue=!Wtf.isEmpty(jsonData[i].purchasevalueorsalevalue)?jsonData[i].purchasevalueorsalevalue:"";
            var termtype=!Wtf.isEmpty(jsonData[i].termtype)?jsonData[i].termtype:"";
            var taxtype=!Wtf.isEmpty(jsonData[i].taxtype)?jsonData[i].taxtype:"";
            var assessablevalue=!Wtf.isEmpty(jsonData[i].assessablevalue)?jsonData[i].assessablevalue:"0";
            //If user does not have permission to view unit price, then assessable amount is also hidden from user
            assessablevalue = hideUnitPriceAmount ? Wtf.UpriceAndAmountDisplayValue : WtfGlobal.conventInDecimal(assessablevalue,symbol);
            var taxvalue=!Wtf.isEmpty(jsonData[i].taxvalue)?jsonData[i].taxvalue:"";
            taxvalue=WtfGlobal.gstdecimalRendererWithoutCurrency(taxvalue);
            var termtypeName="";
            var taxtypeName="";
            //Tax Description
            switch(termtype){
                case 1:
                    termtypeName="VAT";
                    break;
                case 2:
                    termtypeName="Excise Duty";
                    break;
                case 3:
                    termtypeName="CST";
                    break;
                case 4:
                    termtypeName="Service Tax";
                    break;
                case 5:
                    termtypeName="Swachh Bharat Cess";
                    break;
                case 6:
                    termtypeName="Krishi Kalyan Cess";
                    break;
                case 0:
                    termtypeName="Others";
                    break;
                default:
                    // For dynamic line level term type need to get from store 
                    var index = Wtf.LineTermsMasterStore.find('typeid',termtype);
                    if(index>=0){
                        termtypeName = Wtf.LineTermsMasterStore.getAt(index).data.name;
                    }else{
                        termtypeName="";
                    }
            } 
            /*
             * 
             * @type String|@arr;jsonData@pro;cessType|String
             * For cess types in tooltip like ( value per thousand + cess %,Value per Thousand or CESS % whichever is higher etc.) for india ERP-37785
             */
            var cessType = '';
            cessType = !Wtf.isEmpty(jsonData[i].cessType) ? jsonData[i].cessType : "";

            if (WtfGlobal.isIndiaCountryAndGSTApplied() && cessType != undefined && cessType != '') {
                if (cessType == Wtf.CESSTYPE.NOT_APPLICABLE) {
                    taxtypeName = Wtf.CESSTYPE_NAME.NOT_APPLICABLE;
                } else if (cessType == Wtf.CESSTYPE.PERCENTAGES) {
                    taxtypeName = Wtf.CESSTYPE_NAME.PERCENTAGES;
                } else if (cessType == Wtf.CESSTYPE.VALUE_AND_CESSPERCENTAGES) {
                    taxtypeName = Wtf.CESSTYPE_NAME.VALUE_AND_CESSPERCENTAGES;
                } else if (cessType == Wtf.CESSTYPE.HIGHER_VALUE_OR_CESSPERCENTAGES) {
                    taxtypeName = Wtf.CESSTYPE_NAME.HIGHER_VALUE_OR_CESSPERCENTAGES;
                } else if (cessType == Wtf.CESSTYPE.VALUE) {
                    taxtypeName = Wtf.CESSTYPE_NAME.VALUE;
                }
            }
            else {
                // Tax Type
                if (taxtype == 1) {
                    taxtypeName = "Percentage";
                } else if (taxtype == 0) {
                    taxtypeName = "Flat";
                } else {
                    taxtypeName = "";
                }
            }       
            table+="<tr> <td valign=top>"+termtypeName+"</td>";
            table+="<td valign=top>"+term+"</td>";
            table+="<td valign=top>"+taxtypeName+"</td>";
            table+="<td valign=top>"+taxvalue+"</td>";
            table+="<td valign=top>"+assessablevalue+"</td>";
            table+="<td valign=top>"+termamount+"</td>";
            table+="<td valign=top>"+glaccountname+"</td>";
            if (WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.OLDNEW) {
                table += "<td valign=top>" + purchasevalueorsalevalue + "</td>";
                table += "<td valign=top>" + deductionorabatementpercent + "</td>";
            }
            table+="</tr>";
        }
        table+="</table>";
        if(WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW){
            return " <div class='" + getButtonIconCls(Wtf.etype.termCalcWindow) + "' wtf:qtip=\"" + table + " \" wtf:qwidth=\"500\"'> </div> ";
        }else{
            return " <div class='" + getButtonIconCls(Wtf.etype.termCalcWindow) + "' wtf:qtip=\"" + table + " \" wtf:qwidth=\"680\"'> </div> ";
        }
        
    }else{
        return "<div class='" + getButtonIconCls(Wtf.etype.termCalcWindow) + "'></div>";
}
}

Wtf.productBrandRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'}
]);

Wtf.productBrandStore = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.productBrandRec),
    url: "ACCMaster/getMasterItems.do",
    baseParams: {
        mode: 112,
        groupid: 53
    }
});
Wtf.mutliEntiryGSTRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'},
    {name: 'fieldid'}
]);
Wtf.mutliEntiryGSTStore = new Wtf.data.Store({
    url: "AccGST/getMultiEntityForCombo.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.mutliEntiryGSTRec)
});

/*------------ Landing Cost Module Default Fields --------------*/
Wtf.landingCostCategoryRec = new Wtf.data.Record.create([
    {name: 'id'},
    {name: 'name'},
    {name: 'allocationtype'},
    {name: 'allocationtypevalue'}
]);
Wtf.landingCostCategoryStore = new Wtf.data.Store({
    url: "kwlCommonTables/getLandingCostCategory.do",
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.landingCostCategoryRec)
});

Wtf.landingCostAllocationTypeDataIndia = [[Wtf.landingCostAllocation.QUANTITY, 'Quantity'], [Wtf.landingCostAllocation.VALUE, 'Value'], [Wtf.landingCostAllocation.WEIGHT, 'Weight'], [Wtf.landingCostAllocation.MANUAL, 'Manual'], [Wtf.landingCostAllocation.CUSTOMDUTY, 'Custom Duty']];

Wtf.landingCostAllocationTypeStore = new Wtf.data.SimpleStore({
    fields: ['id', 'name'],
    data: [[Wtf.landingCostAllocation.QUANTITY, 'Quantity'], [Wtf.landingCostAllocation.VALUE, 'Value'],[Wtf.landingCostAllocation.WEIGHT, 'Weight'],[Wtf.landingCostAllocation.MANUAL, 'Manual']]
});
Wtf.landingCostAllocationTypeStoreIndia = new Wtf.data.SimpleStore({
    fields: ['id', 'name'],
    data: Wtf.landingCostAllocationTypeDataIndia
});
/*-------------------------*/

/*
 *Function to get JSON Object from json array matching paramPair
 */
Array.prototype.getIemtByParam = function(paramPair) {
    var key = Object.keys(paramPair)[0];
    return this.find(function(item){
        return ((item[key] == paramPair[key]) ? true: false)
    });
}
//Stores for limited accounts combo
Wtf.limitedAccRec = Wtf.data.Record.create([
    {name: 'accountid'},
    {name: 'accid', mapping: 'accountid'},
    {name: 'accountname'},
    {name: 'accname', mapping: 'accountname'},
    {name: 'acccode', mapping: 'accountcode'},
    {name: 'groupname'}
]);

Wtf.customerLimitedAccStore = new Wtf.data.Store({
    url: "ACCAccountCMN/getLimitedAccountsForCombo.do",
    baseParams: {
        fieldId: "customerAccounts"
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.limitedAccRec)
});

Wtf.vendorLimitedAccStore = new Wtf.data.Store({
    url: "ACCAccountCMN/getLimitedAccountsForCombo.do",
    baseParams: {
        fieldId : "vendorAccounts"
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.limitedAccRec)
});

Wtf.productPurchaseLimitedAccStore = new Wtf.data.Store({
    url: "ACCAccountCMN/getLimitedAccountsForCombo.do",
    baseParams: {
        fieldId : "productPurchaseAccounts"
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.limitedAccRec)
});

Wtf.productSalesLimitedAccStore = new Wtf.data.Store({
    url: "ACCAccountCMN/getLimitedAccountsForCombo.do",
    baseParams: {
        fieldId : "productSalesAccounts"
    },
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    }, Wtf.limitedAccRec)
});
//function to load all limited account stores
function loadMappedAccountsStore(){
    Wtf.customerLimitedAccStore.load();
    Wtf.vendorLimitedAccStore.load();
    Wtf.productPurchaseLimitedAccStore.load();
    Wtf.productSalesLimitedAccStore.load();
}
//call function to loading all limited account stores
loadMappedAccountsStore();

Wtf.marubishi_Company_Id = "5b207d19-a091-4815-bc81-3175ae7bd6c6";
// Provided check for HSN/SAC Code should not be greater than 8 digits (For India) ERM-1092
Wtf.agedDueDate1to30Filter = 0;
Wtf.agedInvoiceDate1to30Filter = 1;
Wtf.agedDueDate0to30Filter = 2;
Wtf.agedInvoiceDate0to30Filter = 3;

Wtf.HSNMaxLength = 8;
