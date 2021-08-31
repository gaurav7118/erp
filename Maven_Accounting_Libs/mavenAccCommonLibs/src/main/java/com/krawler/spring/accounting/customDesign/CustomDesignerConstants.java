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

import com.krawler.common.util.Constants;
import java.util.*;

/**
 * A place to keep commonly-used constants.
 */
public class CustomDesignerConstants {
//   
//     public static final String RES_data = "data";
    public static final String CustomDesignProductName_fieldTypeId ="101";
    public static final String CustomDesignProductDesc_fieldTypeId ="102";
    public static final String CustomDesignRate_fieldTypeId ="103";
    public static final String CustomDesignQuantity_fieldTypeId ="104";
    public static final String CustomDesignAmount_fieldTypeId ="105";
    public static final String CustomDesignDiscount_fieldTypeId ="106";
    public static final String CustomDesignTax_fieldTypeId ="107";
    public static final String CustomDesignCurrency_fieldTypeId ="108";
    public static final String CustomDesignParticulars_fieldTypeId ="109";
    public static final String CustomDesignActualQuantity_fieldTypeId ="110";
    public static final String CustomDesignDeliveredQuantity_fieldTypeId ="111";
    public static final String CustomDesignRemarks_fieldTypeId ="112";
    public static final String CustomDesignSerialNumber_fieldTypeId ="113";
    public static final String CustomDesignReceivedQuantity_fieldTypeId ="114";
    public static final String CustomDesignPermitNo_fieldTypeId ="115";
    public static final String DOCUMENT_CURRENCY_ID = "DocumentCurrencyId";
    
    /*Exchange Rate Constants*/
    public static final String CustomDesignExchangeRate_fieldTypeId ="199";
    public static final String CustomDesignGSTExchangeRate_fieldTypeId = "GSTExchangeRate";
        
    //Global Level Base Currency Constants
    public static final String BaseCurrencyGlobalTotalAmount_fieldTypeId = "200";
    public static final String BaseCurrencyGlobalSubTotal_fieldTypeId = "201";
    public static final String BaseCurrencyGlobalTotalTax_fieldTypeId = "202";
    public static final String BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId = "CustomDesignExchangeRateSubTotalwithDiscount_fieldTypeId";
    public static final String BaseCurrencyGlobalTermAmount_fieldTypeId = "CustomDesignExchangeRateTermAmount_fieldTypeId";
    
    //Line level Base Currency Constants
    public static final String BaseCurrencyLineItemUnitPrice = "ExchangeRateUnitPrice";
    public static final String BaseCurrencyLineItemSubTotal = "ExchangeRateLineItemSubTotal";
    public static final String BaseCurrencyLineItemAmount = "ExchangeRateLineItemAmount";
    public static final String BaseCurrencyLineItemSubTotalWithDiscount = "ExchangeRateSubTotalWithoutDiscount";
    public static final String BaseCurrencyLineItemTax ="ExchangeRateLineItemTax";
    public static final String BaseCurrencyLineItemDiscount ="ExchangeRateLineItemDiscount";
    
    //Global Level Specific Currency Constants
    public static final String GlobalSpecificCurrencyExchangeRate ="GlobalSpecificCurrencyExchangeRate";
    public static final String GlobalSpecificCurrencyAmount ="GlobalSpecificCurrencyAmount";
    public static final String GlobalSpecificCurrencySubTotal ="GlobalSpecificCurrencySubTotal";
    public static final String GlobalSpecificCurrencySubTotalWithDicount ="GlobalSpecificCurrencySubTotalWithDicount";
    public static final String GlobalSpecificCurrencyTaxAmount ="GlobalSpecificCurrencyTaxAmount";
    public static final String GlobalSpecificCurrencyTermAmount ="GlobalSpecificCurrencyTermAmount";
    
    
    //Line level Specific Currency Constants
    public static final String SpecificCurrencyExchangeRate ="SpecificCurrencyExchangeRate";
    public static final String SpecificCurrencyAmount ="SpecificCurrencyAmount";
    public static final String SpecificCurrencyDiscount ="SpecificCurrencyDiscount";
    public static final String SpecificCurrencySubTotal ="SpecificCurrencySubTotal";
    public static final String SpecificCurrencySubTotalWithDicount ="SpecificCurrencySubTotalWithDicount";
    public static final String SpecificCurrencyTaxAmount ="SpecificCurrencyTaxAmount";
    public static final String SpecificCurrencyUnitPrice ="SpecificCurrencyUnitPrice";
    
    public static final String LineItemSubTotalWithDiscount="SubTotalWithoutDiscount";
    public static final String SubTotalWithoutDiscount = "SubTotalWithoutDiscount";
    public static final String CustomDesignTotalAmount_fieldTypeId ="116";
    public static final String CustomDesignOriginalAmountDueTotal ="OriginalAmountDueTotal";  //ERP-19271
    public static final String CustomDesignSubTotal_fieldTypeId ="117";
    public static final String CustomDesignTotalDiscount_fieldTypeId ="118";
    public static final String CustomDesignSubTotalWithDiscount_fieldTypeId ="subTotalWithDiscount";
    public static final String CustomDesignSubTotalWithTax_fieldTypeId ="subTotalWithTax"; //ERP-25162
    public static final String CustomDesignTotalTax_fieldTypeId ="119";
    public static final String CustomDesignAmountPaid_fieldTypeId ="CustomDesignAmountPaid_fieldTypeId";
    public static final String CustomDesignCurrencySymbol_fieldTypeId ="CustomDesignCurrencySymbol_fieldTypeId";
    public static final String CustomDesignAmountinwords_fieldTypeId ="120";
    public static final String CustomDesign_Amount_in_words_Bahasa_Indonesia ="Amount in words(Bahasa Indonesia)";
    public static final String CustomDesignAmountinwords_withoutCurrency_fieldTypeId ="amount_in_words_without_currency";
    public static final String CustomDesignTotal_lineItemColumn ="Total";
    public static final String CNDN_InvoiceSalesPerson_fieldTypeId ="InvoiceSalesPerson";
    public static final String CustomDesignTotalTermsDescription_fieldTypeId ="121";
    public static final String CustomDesignQuoteRefNumber_fieldTypeId ="123";
    public static final String CustomDesignSORefNumber_fieldTypeId ="124";
    public static final String CustomDesignDORefNumber_fieldTypeId ="125";
    public static final String CustomDesignDORef_Date_fieldTypeId ="DeliveryOrderDate";
    public static final String CustomDesignVendorQuoteRefNumber_fieldTypeId ="126";
    public static final String CustomDesignPORefNumber_fieldTypeId ="127";
    public static final String CSUTOMDESIGNER_PO_REF_DATE = "PORefDate";
    public static final String CustomDesignGRORefNumber_fieldTypeId ="128"; 
    public static final String CustomDesignGRORefDate_fieldTypeId ="GRORefDate"; 
    public static final String CustomDesignInvRefNumber_fieldTypeId ="129"; 
    public static final String CustomDesignVenInvRefNumber_fieldTypeId ="130";
    public static final String CustomDesignVenInvRefDate_fieldTypeId ="venInvRefDate";
    public static final String CustomDesignCustomerVendorBillingAddress_fieldTypeId = "131";
    public static final String CustomDesignCompanyPostText_fieldTypeId = "132";
    public static final String CustomDesignCustomerVendorShippingAddress_fieldTypeId = "133";
    public static final String CustomDesignCreditTerm_fieldTypeId = "134";
    public static final String CustomDesignNetCreditTerm_fieldTypeId = "NetCreditTerm";
    public static final String CustomDesignCompanyUEN_fieldTypeId = "135";
    public static final String CustomDesignCompanyGRN_No_fieldTypeId = "136";
    public static final String CustomDesignTotalTermsName_fieldTypeId ="Terms Name";
    public static final String CustomDesignBaseCurrency_fieldTypeId ="Base Currency";
    public static final String CustomDesignVendorCurrency_fieldTypeId ="Vendor Currency";
    public static final String CustomDesignPORefNo = "PORef";
    public static final String CustomDesignPRRefNo = "PRRef";
    public static final String CustomJobWorkOrderNo = "JobWorkOrderNo";
    public static final String CustomJobWorkInNo = "JobWorkInNo";
    public static final String CustomJobWorkOrderDate = "JobWorkOrderDate";
    public static final String CustomDesignCurrency = "Currency";
    public static final String CustomDesignCurrencyCode = "CurrencyCode";
    public static final String CustomDesignCurrencySymbol = "CurrencySymbol";
    public static final String CustomDesignNetDebitTerm_fieldTypeId = "NetDebitTerm";
    public static final String CustomDesignNetVendorTerm_fieldTypeId = "NetVendorTerm";
    public static final String LineItemSubTotalWithTax="SubTotalWithTax";
    
    //Showing Customer Code and Customer name while print (used to PO link with SO).
    public static final String CustomDesignSOCustomerName ="SOCustomerName";
    public static final String CustomDesignSOCustomerCode ="SOCustomerCode";
    
    public static final String CustomDesignCustomerDeliveryDate = "DeliveryDate";
    public static final String CustomDesignCustomerDeliveryTime = "DeliveryTime";
    public static final String CustomDesignCustomerVehicleNo = "VehicleNo";
    public static final String CustomDesignCustomerDriver = "Driver";
    public static final String CustomDesignCustomerVendorBillingCity_fieldTypeId = "137";
    public static final String CustomDesignCustomerVendorBillingState_fieldTypeId = "138";
    public static final String CustomDesignCustomerVendorBillingCountry_fieldTypeId = "139";
    public static final String CustomDesignCustomerVendorBillingPostalCode_fieldTypeId = "140";
    public static final String CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId = "141";
    public static final String CustomDesignCustomerVendorBillingFaxNo_fieldTypeId = "142";
    public static final String CustomDesignCustomerVendorBillingMobileNo_fieldTypeId = "BillingMobileNo";
    public static final String CustomDesignCustomerVendorBillingEmailID_fieldTypeId = "VendorBillingEmailID";
    public static final String CustomDesignCustomerVendorShippingEmailID_fieldTypeId = "VendorShippingEmailID";
    public static final String CustomDesignCustomerVendorShippingMobileNo_fieldTypeId = "ShippingingMobileNo";
    public static final String CustomDesignCustomerVendorAlice_Name = "Alias_Name";
    
    public static final String CustomDesignCustomerVendorShippingCity_fieldTypeId = "143";
    public static final String CustomDesignCustomerVendorShippingState_fieldTypeId = "144";
    public static final String CustomDesignCustomerVendorShippingCountry_fieldTypeId = "145";
    public static final String CustomDesignCustomerVendorShippingPostalCode_fieldTypeId = "146";
    public static final String CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId = "147";
    public static final String CustomDesignCustomerVendorShippingFaxNo_fieldTypeId = "148";
    
    public static final String CustomDesignCustomerVendorBillingContactPerson_fieldTypeId = "BillingContactPerson";
    public static final String CustomDesignCustomerVendorShippingContactPerson_fieldTypeId = "ShippingContactPerson";
    public static final String CustomDesignCustomerVendorContactBillingPhoneNo_fieldTypeId = "Billingphonenumber";
    public static final String CustomDesignCustomerVendorContactShippingPhoneNo_fieldTypeId = "Shippingphonenumber";
    public static final String CustomDesignCustomerVendorContactBillingDesignation_fieldTypeId = "Billingdesignation";
    public static final String CustomDesignCustomerVendorContactShippingDesignation_fieldTypeId = "Shippingdesignation";
    public static final String CustomerVendor_Total_Address = "CustomerVendor_Total_Address";
    public static final String CustomDesignCustomerVendorQuantity_Total = "TotalQuantity";
    public static final String CustomerVendor_Address_PostalCode = "CustomerVendor_Address_PostalCode";  
    public static final String CustomDesignCustomerVendorBillingAddress2_fieldTypeId = "BillingAddress2";
    public static final String CustomDesignCustomerVendorBillingAddress3_fieldTypeId = "BillingAddress3";
    public static final String CustomDesignCustomerVendorShippingAddress2_fieldTypeId = "ShippingAddress2";
    public static final String CustomDesignCustomerVendorShippingAddress3_fieldTypeId = "ShippingAddress3";
    public static final String CustomDesignCustomerVendorBillingPhoneNo2_fieldTypeId = "BillingPhoneNo2";
    public static final String CustomDesignCustomerVendorBillingPhoneNo3_fieldTypeId = "BillingPhoneNo3";
    public static final String CustomDesignCustomerVendorShippingPhoneNo2_fieldTypeId = "ShippingPhoneNo2";
    public static final String CustomDesignCustomerVendorShippingPhoneNo3_fieldTypeId = "ShippingPhoneNo3";
    public static final String CustomDesignCustomerVendorBillingEmail1_fieldTypeId = "BillingEmail1";
    public static final String CustomDesignCustomerVendorBillingEmail2_fieldTypeId = "BillingEmail2";
    public static final String CustomDesignCustomerVendorBillingEmail3_fieldTypeId = "BillingEmail3";
    public static final String CustomDesignCustomerVendorShippingEmail1_fieldTypeId = "ShippingEmail1";
    public static final String CustomDesignCustomerVendorShippingEmail2_fieldTypeId = "ShippingEmail2";
    public static final String CustomDesignCustomerVendorShippingEmail3_fieldTypeId = "ShippingEmail3";
    public static final String CustomDesignCustomerVendorCompanyEmail3_fieldTypeId = "Company Email";
    public static final String CustomDesignCustomerVendorBillingFaxNo2_fieldTypeId = "BillingFaxNo2";
    public static final String CustomDesignCustomerVendorBillingFaxNo3_fieldTypeId = "BillingFaxNo3";
    public static final String CustomDesignCustomerVendorShippingFaxNo2_fieldTypeId = "ShippingFaxNo2";
    public static final String CustomDesignCustomerVendorShippingFaxNo3_fieldTypeId = "ShippingFaxNo3";
    public static final String CustomDesignCustomerCompanyAddress_fieldTypeId = "CompanyAddress";
    public static final String CustomDesignCompAccPrefBillAddress_fieldTypeId = "CompAccPrefBillAddress";
    public static final String CustomDesignCompAccPrefShipAddress_fieldTypeId = "CompAccPrefShipAddress";
    public static final String CustomDesignCustomerVendorShippingRecipientName_fieldTypeId = "ShippingRecipientName";
    public static final String CustomDesignCustomerVendorBillingRecipientName_fieldTypeId = "BillingRecipientName";
    //ERP-21048
    public static final String CustomDesignCompanyBillingAddressField_fieldTypeId = "Company Billing Address Field";
    public static final String CustomDesignCompanyBillingAddressCity_fieldTypeId = "Company Billing Address City";
    public static final String CustomDesignCompanyBillingAddressState_fieldTypeId = "Company Billing Address State";
    public static final String CustomDesignCompanyBillingAddressCountry_fieldTypeId = "Company Billing Address Country";
    public static final String CustomDesignCompanyBillingAddressPostalCode_fieldTypeId = "Company Billing Address Postal Code";
    public static final String CustomDesignCompanyBillingAddressContactPerson_fieldTypeId = "Company Billing Address Contact Person";
    public static final String CustomDesignCompanyBillingAddressContactPersonNo_fieldTypeId = "Company Billing Address Contact Person No";
    public static final String CustomDesignCompanyBillingAddressPhoneNo_fieldTypeId = "Company Billing Address Phone No";
    public static final String CustomDesignCompanyBillingAddressMobileNo_fieldTypeId = "Company Billing Address Mobile No";
    public static final String CustomDesignCompanyBillingAddressFaxNo_fieldTypeId = "Company Billing Address Fax No";
    public static final String CustomDesignCompanyBillingAddressEmail_fieldTypeId = "Company Billing Address Email";
    public static final String CustomDesignCompanyShippingAddressField_fieldTypeId = "Company Shipping Address Field";
    public static final String CustomDesignCompanyShippingAddressCity_fieldTypeId = "Company Shipping Address City";
    public static final String CustomDesignCompanyShippingAddressState_fieldTypeId = "Company Shipping Address State";
    public static final String CustomDesignCompanyShippingAddressCountry_fieldTypeId = "Company Shipping Address Country";
    public static final String CustomDesignCompanyShippingAddressPostalCode_fieldTypeId = "Company Shipping Address Postal Code";
    public static final String CustomDesignCompanyShippingAddressContactPerson_fieldTypeId = "Company Shipping Address Contact Person";
    public static final String CustomDesignCompanyShippingAddressContactPersonNo_fieldTypeId = "Company Shipping Address Contact Person No";
    public static final String CustomDesignCompanyShippingAddressPhoneNo_fieldTypeId = "Company Shipping Address Phone No";
    public static final String CustomDesignCompanyShippingAddressMobileNo_fieldTypeId = "Company Shipping Address Mobile No";
    public static final String CustomDesignCompanyShippingAddressFaxNo_fieldTypeId = "Company Shipping Address Fax No";
    public static final String CustomDesignCompanyShippingAddressEmail_fieldTypeId = "Company Shipping Address Email";
    
    /******** SOA ***********/
    public static final String CustomDesignSOACustomerName = "CustomerName";
    public static final String CustomDesignSOAVendorName = "VendorName";
    public static final String CustomDesignSOABillDate = "Date";
    public static final String CustomDesignSOATransactionID = "TransactionID";
    public static final String CustomDesignSOATransactionType = "TransactionType";
    public static final String CustomDesignSOAJournalEntry = "JournalEntry";
    public static final String CustomDesignSOAMemo = "Memo";
    public static final String CustomDesignSOADebitAmount = "DebitAmount";
    public static final String CustomDesignSOACreditAmount = "CreditAmount";
    public static final String CustomDesignSOADebitAmountInBase = "DebitAmountInBase";
    public static final String CustomDesignSOACreditAmountInBase = "CreditAmountInBase";
    public static final String CustomDesignSOATotalCreditAmountInBase = "TotalCreditAmountInBase";
    public static final String CustomDesignSOATotalDebitAmountInBase = "TotalDebitAmountInBase";
    public static final String CustomDesignSOABalanceAmountInBase = "BalanceAmountInBase";
    public static final String CustomDesignSOACustVendCurrBalanceAmountInBase = "CustVendCurrBalanceAmountInBase";
    public static final String CustomDesignSOADueDate = "DueDate";
    public static final String CustomDesignSOACurrency = "Currency";
    public static final String CustomDesignSOACurrencyCode = "Currency Code";
    public static final String CustomDesignSOACurrencySymbol = "Currency Symbol";
    public static final String CustomDesignSOAPartialPayment = "PartialPayment";
    public static final String CustomDesignSOAPoRefrence = "PoRefrence";
    public static final String CustomDesignSOAOriginalAmount = "OriginalAmount";
    public static final String CustomDesignSOAOchequeNo = "chequeNo";
    public static final String CustomDesignSOAAccruedBalance = "accruedBalance";
    public static final String CustomDesignSOAAccruedBalanceInBase = "accruedBalanceInBase";
    public static final String CustomDesignSOABanalanceAmount = "TransactionBalanceAmount";
    public static final String CustomDesignSOATotalInvoiceOriginalAmount = "TotalInvoiceOriginalAmount";
    public static final String CustomDesignSOABalanceAmount = "BalanceAmount";
    
    

    public static final String CNDN_InvoiceNo_fieldTypeId = "149";
    public static final String CNDN_InvoiceAmount_fieldTypeId = "150";
    public static final String CNDN_InvoiceTax_fieldTypeId = "151";
    public static final String CNDN_InvoiceAmountDue_fieldTypeId = "152";
    public static final String CNDN_InvoiceEnterAmount_fieldTypeId = "153";
    public static final String CNDN_Account_fieldTypeId = "154";
    public static final String CNDN_AccountAmount_fieldTypeId = "155";
    public static final String CNDN_AccountTax_fieldTypeId = "156";
    public static final String CNDN_AccountTaxAmount_fieldTypeId = "157";
    public static final String CNDN_AccountTotalAmount_fieldTypeId = "158";
    public static final String CNDN_AccountDescription_fieldTypeId = "159";
    public static final String CNDN_INvoiceDates_fieldTypeId = "160";
    public static final String CNDN_AccountCode_fieldTypeId = "161";
    public static final String CNDN_AccountReason_fieldTypeId = "AccountReason";
    public static final String CNDN_AccountAmountExcludeGST_fieldTypeId = "AccountAmountExcludingGST";
    
    public static final String Customer_AccountNo_fieldTypeId = "162";
    
    public static final String CustomDesignCN_InvoiceNo ="CN_INV";   
    public static final String CustomDesignCN_Customeremail="CN_CUSTEMAIL";
    
    public static final String CustomDesignDN_VendorInvoiceNo ="DN_VENINV";   
    public static final String CustomDesignDN_BillTo="DN_BILLTO";
    public static final String CustomDesignDN_Vendoremail="DN_VENEMAIL";
    
    public static final String CustomDesignBandID_body ="1";
    public static final String CustomDesignBandID_footer ="2"; 
    public static final String CustomDesignBandID_header ="3";
    
    public static final String CompanyPreferences_ModuleId = "122";
    
    /*Receive Payment and Make Payment*/
    public static final String PaymentMethod = "163";
    public static final String PaymentAccount = "164";
    //Invoice Details
    public static final String RPMP_CustomerVendorInvoiceNo = "165";
    public static final String RPMP_InvoiceDate = "166";
    public static final String RPMP_DueDate = "167";
    public static final String RPMP_Tax = "168";
    public static final String RPMP_Discount = "169";
    public static final String RPMP_OriginalAmount = "170";
    public static final String Invoice_Original_Amount_Due = "171";
    public static final String Invoice_Exchange_Rate = "172";
    public static final String RPMP_AmountDue = "173";
    public static final String RPMP_EnterPayment = "174";
    public static final String RPMP_ENTER_PAYMENT_WITH_TAX = "EnterPaymentWithTax";
    //JE details
    public static final String JE_ACCOUNT_CODE = "jeAccountCode";
    public static final String JE_ACCOUNT_NO = "jeAccountNo";
    public static final String JE_ACCOUNT_NAME = "jeAccountName";
    public static final String JE_DESCRIPTION = "jeDescription";
    public static final String JE_CREDIT_AMOUNT = "jeCreditAmount";
    public static final String JE_DEBIT_AMOUNT = "jeDebitAmount";
    //Bank Details
    public static final String Chequeno="175";
    public static final String BankName="176";
    public static final String BankDescription="177";
    public static final String ChequeDate="178";
    public static final String Cheque_Payment_Status="179";
    public static final String Cheque_Payment_ClearanceDate="180";
    //payment 2nd option
    public static final String RPMP_CustomerVendorName = "181";
    //payment 3rd option
    public static final String paymentDebit_note="182";
    public static final String paymentDebit_noteAmount="183";
    public static final String paymentDebit_note_AmountDue="184";
    public static final String paymentDebit_note_EnterPayment="185";
    public static final String RPMP_CreditAmount = "CreditAmount";
    public static final String RPMP_DebitAmount = "DebitAmount";
    public static final String RPMP_ACCOUNT_CODE = "AccountCode";
    //Payment 4rth option grid
    public static final String GridType="186";
    public static final String GridAccountName="187";
    public static final String GridAmountinSGD="188";
    public static final String GridDesc="189";
    public static final String GridTax="190";
    public static final String GridTaxAmount="191";
    public static final String GridAmountinTax="192";
    public static final String GridAccountCode="GridAccountCode";
    
    //Card Holder Name
    public static final String CardNo = "193";
    public static final String CardHolderName = "194";
    public static final String Card_Reference_Number = "195";
    public static final String Card_Type = "196";
    public static final String Card_ExpiryDate = "197";
    
    public static final String Main_Vendor_Name = "198";
    public static final String RPMP_InvoiceTotalDiscount = "RPMP_InvoiceTotalDiscount";
    public static final String accountgriddetails="ACC_GRID_DETAILS";
    public static final String RPMP_AdvanceAmount="RP_MP_AdvanceAmount";
    public static final String CustomerVendor_Term="Customer_Vendor_Term";
    public static final String CustomerVendor_Term_Days="Customer_Vendor_Term_Days";
    public static final String CustomerVendor_AccountCode="CustomerVendor_AccountCode";
    public static final String CustomerVendor_AccCode="CustomerVendor_AccCode";
    public static final String CustomerVendor_AccName="CustomerVendor_AccName";
    public static final String SrNo="SrNo";
    public static final String InvoiceAmountDue="InvoiceAmountDue";
    public static final String AmountDueInWords="AmountDueInWords";
    public static final String InvoicePaymentReceived="InvoicePaymentReceived";
    public static final String PaymentReceivedInWords="PaymentReceivedInWords";
    public static final String Include_GST="Include_GST";
    
    public static final String RPMP_DocumentType = "RPMP_DocumentType";
    public static final String RPMP_DocumentNumber = "RPMP_DocumentNumber";
    public static final String RPMP_Description = "RPMP_Description";
    public static final String RPMP_OriginalAmountDue = "RPMP_OriginalAmountDue";
    public static final String RPMP_ExchangeRate = "RPMP_ExchangeRate";
    public static final String RPMP_TaxAmount = "RPMP_TaxAmount";
    
    public static final String Common_Invoiceno_Accname_creditdebitno_advance="Common_Invoiceno_Accname_creditdebitno_advance";
    public static final String Common_EnterPayment="Common_EnterPayment";
    public static final String DateFormat_RemovingTime="MMMM d, yyyy";
    public static final String CustomerVendor_Code="CustomerVendor_Code";
    public static final String CustomerVendor_VAT_TIN_NO="CustomerVendor_VAT_TIN_NO";
    public static final String CustomerVendor_CST_TIN_NO="Customer_CST_TIN_NO";
    public static final String CustomerVendor_ECC_NO="Customer_Excise_Control_Code";
    public static final String CUSTOMER_SERVICE_TAX_REG_NO="CUSTOMER_SERVICE_TAX_REG_NO";
    
    public static final String CUSTOMER_PAN_NO = "CustomerPanNumber";
    public static final String CustomerVendor_PAN_STATAUS="CustomerVendor_PAN_STATUS";
    public static final String CustomerVendor_DEDUCTEE_TYPE="CustomerVendor_DEDUCTEE_TYPE";
    public static final String CustomerVendor_DEDUCTEE_CODE="CustomerVendor_DEDUCTEE_CODE";
    public static final String CustomerVendor_DEFAULT_NATURE_OF_PAYMENT="CustomerVendor_DEFAULT_NATURE_OF_PAYMENT";
    public static final String CustomerVendor_RESIDENTIAL_STATUS="CustomerVendor_RESIDENTIAL_STATUS";
    public static final String CustomerVendor_CST_REG_DATE="CustomerVendor_CST_REG_DATE";
    public static final String CustomerVendor_VAT_REG_DATE="CustomerVendor_VAT_REG_DATE";
    public static final String CustomerVendor_VAT_DEALER_TYPE="CustomerVendor_VAT_DEALER_TYPE";
    public static final String CustomerVendor_IMPORTER_ECC_NO="CustomerVendor_IMPORTER_ECC_NO";
    public static final String CustomerVendor_IEC_NO="CustomerVendor_IEC_NO";
    public static final String CustomerVendor_RANGE_CODE="CustomerVendor_RANGE_CODE";
    public static final String CustomerVendor_DIVISION_CODE="CustomerVendor_DIVISION_CODE";
    public static final String CustomerVendor_COMMISSIONERATE_CODE="CustomerVendor_COMMISSIONERATE_CODE";
    public static final String CustomerVendor_TDS_PAYABLE_ACCOUNT="CustomerVendor_TDS_PAYABLE_ACCOUNT";
    public static final String CustomerVendor_TDS_HIGHER_RATE="CustomerVendor_TDS_HIGHER_RATE";
    public static final String CustomerVendor_INTERSTATEPARTY="CustomerVendor_INTERSTATEPARTY";
    public static final String CustomerVendor_C_FORM_APPLICABLE="CustomerVendor_C_FORM_APPLICABLE";
    public static final String CustomerVendor_TYPE_OF_MANUFATURER="CustomerVendor_TYPE_OF_MANUFATURER";
    public static final String CustomerVendor_TYPE_OF_SALES="CustomerVendor_TYPE_OF_SALES";
    public static final String TDS_AMOUNT="TDS_AMOUNT";
    public static final String TDS_RATE="TDS_RATE";
    public static final String OTHER_CHARGES="OTHER_CHARGES";
    
    public static final String Company_VAT_TIN_NO="Company_VAT_TIN_NO";
    public static final String Company_CST_TIN_NO="Company_CST_TIN_NO";
    public static final String Company_ECC_NO="Company_Excise_Control_Code";
    public static final String COMPANY_SERVICE_TAX_REG_NO="COMPANY_SERVICE_TAX_REG_NO";
    public static final String Total_Quantity_UOM="Total_Quantity_UOM";
    public static final String CustomerVendor_MappingSalesPerson="CustomerVendor_MappingSalesPerson";
    public static final String Recommended_Retail_Priceid="17";
    public static final String BillTo="BillTo";
    public static final String ShipTo="ShipTo";
    public static final String CUSTOMER_TITLE="CustomerTitle";
    public static final String VENDOR_TITLE="VendorTitle";
    public static final String CUSTOMER_OR_VENDOR_TITLE="CustomerOrVendorTitle";
    public static final String PartNumber="PartNumber";
    public static final String SupplierPartNumber="SupplierPartNumber";
    public static final String CustomerPartNumber="CustomerPartNumber";
    
    
    //same for all the modules
    public static final String SrNO="0";
    public static final String ProductName="1";
    public static final String ProductDescription="2";
    public static final String Rate="3";
    public static final String QuantitywithUOM="4";
    public static final String Amount="5";
    public static final String CGST = "CGST";
    public static final String SGST = "SGST";
    public static final String UTGST = "UTGST";
    public static final String IGST = "IGST";
    public static final String CESS = "CESS";
    public static final String CGSTPERCENT="CGSTPERCENT";
    public static final String SGSTPERCENT="SGSTPERCENT";
    public static final String IGSTPERCENT="IGSTPERCENT";
    public static final String UTGSTPERCENT="UTGSTPERCENT";
    public static final String CGSTAMOUNT="CGSTAMOUNT";
    public static final String SGSTAMOUNT="SGSTAMOUNT";
    public static final String IGSTAMOUNT="IGSTAMOUNT";
    public static final String UTGSTAMOUNT="UTGSTAMOUNT";
    public static final String CESSPERCENT="CESSPERCENT";
    public static final String CESSAMOUNT="CESSAMOUNT";
    public static final String GSTAmountInWords = "GST Amount In Words";
    public static final String AMOUNT_BEFORE_TAX = "Amount_Before_Tax";

    public static final String ProductAvailableQuantity="ProductAvailableQuantity";
    
    //Invoice Constants
    public static final String IN_Discount="6";
    public static final String IN_Tax="7";
    public static final String IN_Currency="8";
    public static final String IN_ProductCode="9";
    public static final String IN_Quantity="10";
    public static final String IN_UOM="11";
    public static final String IN_Loc="12";
    public static final String IN_ProductTax="13";
    public static final String IN_TaxAmount="14";
    public static final String DISPLAY_UOM = "displayUOM"; 
    //DO Constants
    
    public static final String DO_ProductCode="4";
    public static final String DO_Loc="6";
    public static final String DO_ActualQuantityWithUOM="9";
    public static final String DO_DeliveredQuantityWithUOM="10";
    public static final String DO_Remarks="11";
    public static final String DO_SerialNumber="12";
    public static final String DO_CISONo="13";
    public static final String DO_ActualQuantity="14";
    public static final String DO_DeliveredQuantity="15";
    public static final String DO_UOM="16";
    public static final String DO_RRP="17";
    public static final String DO_ProductTax="DO_ProductTax";
    public static final String DO_Discount="DO_Discount";
    
    //DOGROLineMap_GR Constants
    public static final String GR_ProductCode="4";
    public static final String GR_Loc="6";
    public static final String GR_ActualQuantityWithUOM="9";
    public static final String GR_ReceivedQuantityWithUOM="10";
    public static final String GR_Remarks="11";
    public static final String GR_SerialNumber="12";
    public static final String GR_VIPONo="13";
    public static final String GR_ReceivedQuantity="14";
    public static final String GR_UOM="15";
    public static final String GR_ActualQuantity="16";
    public static final String GR_Discount="GR_Discount";
    //added for DO annd GRN
    public static final String Warehouse="warehouse";
    
    //InvoiceProductLineMap_PO constants
    public static final String PO_PermitNo="9";
    public static final String PO_ProductCode="10";
    public static final String PO_Quantity="11";
    public static final String PO_UOM="12";
    public static final String SUBTOTAL="SUBTOTAL";
    public static final String Discountname="Discountname";
    public static final String PartialAmount="PartialAmount";
    public static final String SerialNumber="SerialNumber";
    public static final String SerialNumberExp="SerialNumberExp";
    public static final String BatchNumber="BatchNumber";
    public static final String BatchQuantity="BatchQuantity";
    public static final String HSCode="HSCode";
    public static final String HSN_SAC_CODE="HSN_SAC_CODE";
    public static final String BatchNumberExp="BatchNumberExp";
    public static final String BaseQty="BaseQty";
    public static final String BaseQtyWithUOM="BaseQtyWithUOM";
    public static final String ManufacturingDate="ManufacturingDate";
    public static final String ManufacturingDate_Batch="ManufacturingDateBatch";
    public static final String ManufacturingDate_Serial="ManufacturingDateSerial";
    public static final String AMOUNT_BEFORE_PARTIAL_PAYMENT="AmountBeforePartialAmount";
    public static final String SUBTOTAL_BEFORE_PARTIAL_PAYMENT="SubTotalBeforePartialAmount";
    public static final String SUBTOTAL_AND_TAX_BEFORE_PARTIAL_PAYMENT="SubTotalAndTaxBeforePartialAmount";
    public static final String TOTAL_BEFORE_PARTIAL_PAYMENT="TotalBeforePartialAmount";
    public static final String TOTAL_TAX_BEFORE_PARTIAL_PAYMENT="TotalTaxBeforePartialAmount";
    public static final String REMAINING_BALANCE_DUE = "RemainingBalanceDue";
    
    public static final String TAXABLE_VALUE = "Taxable Value";
    /*
     * Balance Qty and Balance Qty with UOM
     */
    public static final String BalanceQty="BalanceQty";
    public static final String BalanceQtyWithUOM="BalanceQtyWithUOM";
    
//    PO
    public static final String PURCHASEREQCREATOR = "PURCHASEREQCREATOR";
    public static final String CUSTOMER_SHIP_TO = "customerShipTo";
    public static final String CUSTOMER_SHIPPING_ADDRESS = "customerShippingAddress";
    public static final String CUSTOMER_SHIPPING_COUNTRY = "customerShippingCountry";
    public static final String CUSTOMER_SHIPPING_STATE = "customerShippingState";
    public static final String CUSTOMER_SHIPPING_CITY = "customerShippingCity";
    public static final String CUSTOMER_SHIPPING_POSTAL = "customerShippingPostal";
    public static final String CUSTOMER_SHIPPING_EMAIL = "customerShippingEmail";
    public static final String CUSTOMER_SHIPPING_FAX = "customerShippingFax";
    public static final String CUSTOMER_SHIPPING_MOBILE = "customerShippingMobile";
    public static final String CUSTOMER_SHIPPING_PHONE = "customerShippingPhone";
    public static final String CUSTOMER_SHIPPING_CONTACT_PERSON_NO = "customerShippingContactPersonNumber";
    public static final String CUSTOMER_SHIPPING_CONTACT_PERSON = "customerShippingContactPerson";
    
    //Sales Return
     public static final String SR_ProductTax="SR_ProductTax";
     public static final String SR_ActualQuantity="SR_ActualQuantity";
     public static final String SR_ReturnQuantity="SR_ReturnQuantity";
     public static final String SR_ActualQuantitywithUOM="SR_ActualQuantitywithUOM";
     public static final String SR_ReturnQuantitywithUOM="SR_ReturnQuantitywithUOM";
     public static final String SR_SerialNumber="SR_SerialNumber";
     public static final String SR_Reason="SR_Reason";
     public static final String SR_Batch_SubQty="SR_Batch_SubQty";
     public static final String SR_Batch_SubQty_UOM="SR_Batch_SubQty_UOM";
     public static final String SR_Batch_Exp_Date="SR_Batch_Exp_Date";
     public static final String SR_Serial_Exp_Date="SR_Serial_Exp_Date";
     public static final String SR_Asset="SR_Asset";
     public static final String SR_Remark="SR_Remark";
     public static final String SR_LinkTo="SR_LinkTo";
     public static final String Bank_AccountCode="Bank_AccountCode";
     public static final String Bank_AccountNumber="Bank_AccountNumber";
     public static final String CR_memo="CR_memo";
     public static final String CR_SalesPerson="CR_SalesPerson";
     public static final String CR_fromdate="CR_fromdate";
     public static final String CR_todate="CR_todate";
     public static final String Approvedby="Approvedby";
     public static final String NoOfDays="NoOfDays";
     public static final String Tabletype="globaltable";
     public static final String RepeatTabletype="globaltablerepeat";
     public static final String RPMP_TaxCode = "RPMP_TaxCode";
     public static final String AppendRequestType= "AppendRequestType";
     public static final String Link_No ="Link_No";
     public static final String No_Of_Items ="No_Of_Items";
     public static final String No_Of_Items_label ="No of Items";
     public static final String AccountSubTotal = "AccountSubTotal";
     public static final String AccountTotalTax = "AccountTotalTax";
     public static final String AccountTotalAmount = "AccountTotalAmount";
     public static final String InvoiceSubTotal = "InvoiceSubTotal";
     public static final String InvoiceTotalTax = "InvoiceTotalTax";
     public static final String InvoiceTotalAmount = "InvoiceTotalAmount";
     public static final String Acc_ConsignmentRequest_ModuleId ="50";
     public static final String CustomDesignConsignmentRequestType ="ConsignmentRequestType";
     public static final String Consocreatedby ="Consocreatedby";//COnsignment SO Requested by
     public static final String Createdby ="Createdby";//COnsignment SO Requested by
     public static final String CustomDesignCompanyPreText_fieldTypeId = "Company_pretext";
     public static final String TotalAmountWithoutTerm = "TotalAmountWithoutTerm";
     public static final String BaseCurrency = "BaseCurrency_";
     public static final String TotalQuantity = "TotalQuantity";
     public static final String TOTAL_DELIVERED_QUANTITY = "TotalDeliveredQuantity";
     public static final String TOTAL_DELIVERED_QUANTITY_UOM = "TotalDeliveredQuantityWithUOM";
     public static final String roundingDifference = "roundingDifference"; //ERP-25876
    public static final String SalesOrderDate = "SalesOrderDate";
    public static final String InvoiceDate = "InvoiceDate";
    public static final String RemitPaymentTo = "RemitPaymentTo";
    public static final String Poreferencenumber = "Poreferencenumber";
    public static final String CustomDesignSummaryTermsValue_fieldTypeId ="CustomDesignSummaryTermsValue_fieldTypeId";
    public static final String CustomDesignTemplate_Print ="CustomDesignTemplatePrint";
    public static final String CustomDesignTemplate_ItemsNo ="CustomDesignTemplateItemsNo";
    public static final String VendorBillTo = "VendorBillTo";
    public static final String VendorShipTo = "VendorShipTo";
    public static final String PageNumberField ="PageNumberField";
    public static final String CurrentDateField ="CurrentDateField";
    public static final String EndDateField ="End Date";
    public static final String StartDateField ="Start Date";
    public static final String AsofDateField ="As of Date";
    public static final String CurrentMonthField ="CurrentMonth";
    public static final String CurrentYearField ="CurrentYear";
    public static final String AllDimensions ="AllDimensions";
    public static final String imageTag ="imageTag";
    public static final String DimensionLabel="#DimensionLabel#";
    public static final String DimensionValue="#DimensionValue#";
    public static final String DimensionKeyValuePair= "<span style=\"display:table-row\"><span style=\"width:35%;display:table-cell;\">#DimensionLabel#</span> <span style=\"width:5%;display:table-cell;\">:</span> <span style=\"width:50%;display:table-cell;\">#DimensionValue#</span></span>";
    public static final String Posttext ="#Post Text#";
    /*Inventory*/
    public static final String Packaging ="Packaging";
    public static final String UOM ="UOM";
    public static final String Quantity ="Quantity";
    public static final String ISSUED_QUANTITY ="IssuedQuantity";
    public static final String DELIVERED_QUANTITY ="DeliveredQuantity";
    public static final String Location ="Packaging";
    public static final String CostCenter ="CostCenter";
    public static final String CostCenterName ="CostCenterName";
    public static final String ProjectNo ="ProjectNo";
    public static final String FromStore ="FromStore";
    public static final String FromStoreCode ="FromStoreCode";
    public static final String FromStoreDesc ="FromStoreDesc";
    public static final String ToStore ="ToStore";
    public static final String ToStoreCode ="ToStoreCode";
    public static final String ToStoreDesc ="ToStoreDesc";
    public static final String FromLocName ="FromLocName";
    public static final String ToLocName ="ToLocName";
    public static final String OrderStatus ="OrderStatus";
    public static final String TOTALIN ="TotalIN";
    public static final String TOTALOUT ="TotalOUT";
    public static final String TOTAL_IN_QTY ="TotalINQty";
    public static final String TOTAL_OUT_QTY ="TotalOUTQty";
    public static final String CollectLocation ="CollectLocation";
    public static final String VendorTransactionalShipTo ="VendorTransactionalShipTo";
    public static final String AllGloballevelDimensions ="AllGloballevelDimensions";
    public static final String AllGloballevelCustomfields ="AllGloballevelCustomfields";
    public static final String CustomFieldLabel = "#CustomFieldLabel#";
    public static final String CustomFieldValue = "#CustomFieldValue#";
    public static final String CustomFieldKeyValuePair = "<span style=\"display:table-row\"><span style=\"width:35%;display:table-cell;\">#CustomFieldLabel#</span> <span style=\"width:5%;display:table-cell;\">:</span> <span style=\"width:50%;display:table-cell;\">#CustomFieldValue#</span></span>";
    public static final String AllTerms ="AllTerms";
    public static final String AllTermNames ="AllTermNames";
    public static final String AllTermSigns ="AllTermsign";
    public static final String AllLineLevelTermSigns ="AllLineLevelTermsign";
    public static final String AllTermAmounts ="AllTermAmounts";
    public static final String AllTermsLabel = "#AllTermsLabel#";
    public static final String AllTermsValue = "#AllTermsValue#";
    public static final String AllTermsKeyValuePair = "<span style=\"display:table-row\"><span style=\"width:35%;display:table-cell;\">#AllTermsLabel#</span> <span style=\"width:5%;display:table-cell;\">:</span> <span style=\"width:50%;display:table-cell;\">#AllTermsValue#</span></span>";
    
    public static final String DimensionOrder = "dimensionorder";
    public static final String FieldLabel = "fieldlabel";
    public static final String FieldName = "fieldname";
    public static final String CustomFieldOrder = "customfieldorder";
    public static final String isCustomfield = "isCustomfield";
    public static final String fieldid = "fieldid";
    public static final String islineitem = "islineitem";
    public static final String json = "json";
    public static final String CustomLabel = "customlabel";
    public static final String AllLinelevelCustomFields ="AllLinelevelCustomFields";
    public static final String OrderQuantity ="OrderQuantity";
    public static final String AdjustmentType ="AdjustmentType";
    public static final String PerUnitPrice ="PerUnitPrice";
    public static final String SI_ORDER_QUANTITY = "SI_Order_Quantity";

    public static final String CustomerVendoraccountcode ="CustomerVendoraccountcode";
    public static final String CustomerVendorCode ="CustomerVendorCode";
    public static final String Updatedby = "Updatedby";
    public static final String Basecurrencyname = "Basecurrencyname";
    public static final String Basecurrencycode = "Basecurrencycode";
    public static final String Basecurrencysymbol = "Basecurrencysymbol";
    public static final int globaltablefieldtype = 12;
    public static final int summerytablefieldtype = 11;
    public static final int selectfieldfieldtype = 2;
    public static final int dataelementfieldtype = 17;
    public static final String SummaryTaxPercent = "SumaryTaxPercent";
    public static final String BaseCurrencyAccountAmount = "BaseCurrencyAccountAmount";
    public static final String BaseCurrencyAccountTaxAmount = "BaseCurrencyAccountTaxAmount";
    public static final String BaseCurrencyAccountAmountwithTax = "BaseCurrencyAccountAmountwithTax";
    public static final String BaseCurrencyAccountSubTotal = "BaseCurrencyAccountSubTotal";
    public static final String BaseCurrencyAccountTotalTax = "BaseCurrencyAccountTotalTax";
    public static final String BaseCurrencyAccountTotalAmount = "BaseCurrencyAccountTotalAmount";
    public static final String CustomerVendorBillingAddress = "CustomerVendorBillingAddress";
    public static final String CustomerVendorShippingAddress = "CustomerVendorShippingAddress";
    public static final String FromStoreTotalAddress = "FromStoreTotalAddress";
    public static final String ToStoreTotalAddress = "ToStoreTotalAddress";
    public static final String FromStoreAddress = "FromStoreAddress";
    public static final String ToStoreAddress = "ToStoreAddress";
    public static final String FromStoreContactNo= "FromStoreContactNo";
    public static final String ToStoreContactNo = "ToStoreContactNo";
    public static final String FromStoreFaxNo = "FromStoreFaxNo";
    public static final String ToStoreFaxNo = "ToStoreFaxNo";

    public static final String LinkedSalesPerson ="SalesPerson";
    public static final String LinkedSalesPersonDesignation ="SalesPersonDesignation";
    public static final String GSTNumber ="GSTNumber";
    public static final String CUSTOMER_VENDOR_GSTIN_NUMBER ="GSTIN_Number";// constant for GSTIN Number in Customer or Vendor Master
    public static final String CustomDesign_ENTITY_GSTIN_NUMBER ="Entity_GSTIN_Number";// constant for Entity GSTIN Number in master config
    public static final String LinkedReferenceDate ="LinkedReferenceDate";
    public static final String CustomerModuleid ="25";
    public static final String VendorModuleid ="26";
    public static final String SalesReturnNumber ="SalesReturnNumber";
    public static final String PurchaseReturnNumber ="PurchaseReturnNumber";
    public static final String PurchaseRequisitionNumber ="PurchaseRequisitionNumber";
    public static final String PurchaseRequisitionDate ="PurchaseRequisitionDate";
    public static final String InvoiceTax = "InvoiceTax";
    public static final String StatementOfAccTotal ="total";
    public static final String StatementOfAcccurrency = "Currency";
    public static final String StatementOfAccInterval = "interval";
    public static final String StatementOfAccAccruedBalance = "accruedbalance";
    public static final String RRP="RRP";
    public static final String CurrentUserFirstName="CurrentUserFirstName";
    public static final String CurrentUserLastName="CurrentUserLastName";
    public static final String CurrentUserEmail="CurrentUserEmail";
    public static final String CurrentUserFullName="CurrentUserFullName";
    public static final String CurrentUserAddress="CurrentUserAddress";
    public static final String CurrentUserContactNumber="CurrentUserContactNumber";
    public static final String CompanyModuleid = "1";
    public static final String CompanyPANNumber = "CompanyPANNumber";
    public static final String serviceTaxNumber = "serviceTaxNumber";
    public static final String totalAmountForServiceTaxInvoice = "TotalAmountForServiceTaxInvoice";
    public static final String swacchaBharatCessForServiceTaxInvoice = "swacchaBharatCessForServiceTaxInvoice";
    public static final String serviceTaxRateForServiceTaxInvoice = "serviceTaxRateForServiceTaxInvoice";
    public static final String AdditionalDescription = "AdditionalDescription";
    public static final String LineLevelTax = "LineLevelTax";
    public static final String LineLevelTaxAmount = "LineLevelTaxAmount";
    public static final String LineLevelTaxPercent = "LineLevelTaxPercent";
    public static final String AllLineLevelTax = "AllLineLevelTax";
    public static final String AllLineLevelTaxAmount = "AllLineLevelTaxAmount";
    public static final String AllLineLevelTaxPercent = "AllLineLevelTaxPercent";
    public static final String AllLineLevelTaxBasic = "AllLineLevelTaxBasic";
    public static final String CompanyBankIFSCCode = "CompanyBankIFSCCode";
    public static final String CompanyBankAccountNumber = "CompanyBankAccountNumber";
    public static final String solinking = "solinking";
    public static final String linking = "linking";
    public static final String polinking = "polinking";
    public static final String invoicelinking = "invoicelinking";
    public static final String salesreturnlinking = "salesreturnlinking";
    public static final String BaseCurrencyInvoiceSubTotal = "BaseCurrencyInvoiceSubTotal";
    public static final String BaseCurrencyInvoiceTotalTax = "BaseCurrencyInvoiceTotalTax";
    public static final String BaseCurrencyInvoiceTotal = "BaseCurrencyInvoiceTotal";
    public static final String BaseCurrencyInvoiceAmount = "BaseCurrencyInvoiceAmount";
    public static final String BaseCurrencyInvoiceTaxAmount = "BaseCurrencyInvoiceTaxAmount";
    public static final String BaseCurrencyEnterAmount = "BaseCurrencyEnterAmount";
    public static final String ProductBarcode = "ProductBarcode";
    public static final String JobWorkChallanDate = "JobWorkChallanDate";
    public static final String JobWorkBalanceQty = "JobWorkBalanceQty";
    public static final String JobWorkConsumeQty = "JobWorkConsumeQty";
    public static final String CURRENCY_EXCHANGE_RATE = "CurrencyExchangeRate";
    public static final String EXCISE_IN_WORDS="ExciseInWords";
    public static final String RANGE_CODE_COMPANY="RangeCodeCompany";
    public static final String DIVISION_CODE_COMPANY="DivisionCodeCompany";
    public static final String EDUCATION_CESS_IN_WORDS="EducationCessInWords";
    public static final String H_CESS_IN_WORDS="HCessInWords";
    public static final String COMMISSIONERATE_CODE_COMPANY="CommissionerateCodeCompany";
    public static final String INVOICE_CREATION_DATE="invoicecreationdate";
    public static final String INVOICE_UPDATION_DATE="invoiceupdationdate";
    public static final String RATEINCLUDINGGST="rateincludinggst";
    public static final String CONSIGNEE_NAME="consignee_name";
    public static final String CONSIGNEE_ADDRESS="consignee_address";
    public static final String CONSIGNEE_EXCISE_REGN_NO="consignee_excise_regn_number";
    public static final String BUYER_EXCISE_REGN_NO="buyer_excise_regn_number";
    public static final String CONSIGNEE_RANGE_CODE="consignee_range_code";
    public static final String CONSIGNEE_DIVISION_CODE="consignee_division_code";
    public static final String CONSIGNEE_COMMISSIONERATE_CODE="consignee_commissionerate_code";
    public static final String VAT_AMOUNT_IN_WORDS="vat_amount_in_words";
    public static final String CUSTOMER_EXCISE_RANGE_CODE="customer_excise_range_code";
    public static final String CUSTOMER_EXCISE_TYPE_OF_MANUFACTURER="customer_excise_type_of_manufacturer";
    public static final String CUSTOMER_EXCISE_TYPE_OF_SALES="customer_excise_type_of_sales";
    public static final String CUSTOMER_EXCISE_IMPORTER_ECC_NUMBER="customer_excise_importer_ecc_number";
    public static final String CUSTOMER_EXCISE_IEC_NUMBER="customer_excise_iec_number";
    public static final String CUSTOMER_EXCISE_DIVISION_CODE="customer_excise_division_code";
    public static final String CUSTOMER_EXCISE_COMMISSIONERATE_CODE="customer_excise_commissionerate_code";
    public static final String INVOICE_DATE_WITH_TIME="invoice_date_with_time";
    public static final String RPMP_TAXNAME = "RPMP_taxname";
    public static final String TAXNAME = "taxname";
    public static final String ISFORMULA = "isformula";
    public static final String TOTALAMOUNT_WITHBANCKCHARGE ="totalamountwithbankcharges";
    public static final String PRODUCTNETWEIGHT ="productnetweight";
    public static final String LOT_SIZE ="lotsize";
    public static final String LINEEXCHANGERATE ="lineexchangerate";
    public static final String CustomDesignSOAAmountDue ="soaamountdue";
    public static final String IS_MULTIPLE_TRANSACTION = "ismultipletransaction";
    public static final String SUPPLIER_INVOICE_NO = "supplierinvoiceno";
    public static final String DOCUMENTSTATUS = "Document_Status";
    public static final String PARTIAL = "Partial";
    public static final String FULL = "Full";
    
    //Currency Symbol/Code index
    public static final String CustomDesignCurrencySymbolIndex ="0";
    public static final String CustomDesignCurrencyCodeIndex ="1";
    public static final String CustomDesignBaseCurrencySymbolIndex ="2";
    public static final String CustomDesignBaseCurrencyCodeIndex ="3";
    
    
    //Lease
    public static final String REPLACEMENT_NUMBER ="replacementnumber";
    
    // Pick and Pack
    public static final String NO_OF_PACKAGE ="noofpackage";
    public static final String PACKAGE_NO ="packageno";
    public static final String PACK_QUANTITY ="packquantity";
    public static final String PACK_LOCATION ="packlocation";
    public static final String PACK_CURRENT_WAREHOUSE ="packcurrentwarehouse";
    public static final String SHIPMENT_TRACKING_NO ="upsshipmentnumber";
    public static final String QUANTITY_PER_PACKAGE ="Quantity per Package";
    public static final String GROSS_WEIGHT ="Gross Weight";
    public static final String TOTAL_GROSS_WEIGHT ="Total Gross Weight";
    public static final String PACKAGE_MEASUREMENT ="Package Measurement";
    public static final String PACKAGE_NAME ="Package Name";
    
    //modules subtype
    public static final String ASSET ="6";
    public static final String UNDERCHARGE_SUBTYPE = "7";
    public static final String OVERCHARGE_SUBTYPE = "8";
    public static final String JOB_WORK ="9";
    
    //Email Notification
    public static final String TEMPLATE_HYPERLINK_IN_EMAIL ="TemplateHyperlinkInEmail";
    
    public static final String PRODUCT_CATEGORY = "Product Category";
    public static final String PRODUCT_ID = "Product ID";
    public static final String BILLING_ADDRESS = "Billing Address";
    public static final String SHIPPING_ADDRESS = "Shipping Address";
    
    //Delivery Order
    //Total No Of Batches
    public static final String CountOfBatches = "Count Of Batches";
    
    //USED to separate value Global and line level
    public static final String VALUE_SEPARATOR = "!##";
    
    //Posttext for identification of custom fields from which they belongs. (Used in CN with sales return subtype and DN with purchase return subtype template)
    public static final String CUSTOM_POST_TEXT_CN = "_CN";
    public static final String CUSTOM_POST_TEXT_DN = "_DN";
    
    public static final int RICH_TEXT_AREA = 15;
    public static final String UNIT_PRICE_AND_AMOUNT_AS_STARS = "***";
    
    public static final String PO_Status ="PO_Status";
    public static final String SO_Status ="SO_Status";
    
    //Show Amount In Words Constants
    public static final int IN_Document_Currency = 1;
    public static final int IN_Base_Currency = 2;
   
    public static TreeMap<String, String> CustomDesignInvoicetoOtherMap = new TreeMap<String, String>();    
    static{
        CustomDesignInvoicetoOtherMap.put("Customer",CustomerModuleid);
//        CustomDesignInvoicetoOtherMap.put("Tax","41");
        CustomDesignInvoicetoOtherMap.put("Company",CompanyModuleid);
        CustomDesignInvoicetoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
//        CustomDesignInvoicetoOtherMap.put("Consignment Invoice", String.valueOf(Constants.Acc_ConsignmentInvoice_ModuleId));
    }
    
    public static TreeMap<String, String> CustomDesignVendorInvoicetoOtherMap = new TreeMap<String, String>();
    static{
        CustomDesignVendorInvoicetoOtherMap.put("Vendor",VendorModuleid);
//        CustomDesignVendorInvoicetoOtherMap.put("Tax","41");
        CustomDesignVendorInvoicetoOtherMap.put("Company",CompanyModuleid);
        CustomDesignVendorInvoicetoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
    }
    
    public static TreeMap<String, String> CustomDesignSalesOrdertoOtherMap = new TreeMap<String, String>();    
    static{
        CustomDesignSalesOrdertoOtherMap.put("Customer", CustomerModuleid);
//        CustomDesignSalesOrdertoOtherMap.put("Tax","41");
        CustomDesignSalesOrdertoOtherMap.put("Company", CompanyModuleid);
        CustomDesignSalesOrdertoOtherMap.put("Company Preference", CompanyPreferences_ModuleId);
        CustomDesignSalesOrdertoOtherMap.put("Consignment Request", String.valueOf(Constants.Acc_ConsignmentRequest_ModuleId));
    }
    
    public static TreeMap<String, String> CustomDesignPurchaseOrdertoOtherMap = new TreeMap<String, String>();
    static{
        CustomDesignPurchaseOrdertoOtherMap.put("Vendor",VendorModuleid);
//        CustomDesignPurchaseOrdertoOtherMap.put("Tax","41");
        CustomDesignPurchaseOrdertoOtherMap.put("Company",CompanyModuleid);
        CustomDesignPurchaseOrdertoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
    }
    
    public static TreeMap<String, String> CustomDesignQuotationtoOtherMap = new TreeMap<String, String>();    
    static{
        CustomDesignQuotationtoOtherMap.put("Customer",CustomerModuleid);
//        CustomDesignQuotationtoOtherMap.put("Tax","41");
        CustomDesignQuotationtoOtherMap.put("Company",CompanyModuleid);
        CustomDesignQuotationtoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
    }
    
    public static TreeMap<String, String> CustomDesignVendorQuotationtoOtherMap = new TreeMap<String, String>();
    static{
        CustomDesignVendorQuotationtoOtherMap.put("Vendor",VendorModuleid);
//        CustomDesignVendorQuotationtoOtherMap.put("Tax","41");
        CustomDesignVendorQuotationtoOtherMap.put("Company",CompanyModuleid);
        CustomDesignVendorQuotationtoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
    }
    
    public static TreeMap<String, String> CustomDesignDeliveryOrdertoOtherMap = new TreeMap<String, String>();
    static{
        CustomDesignDeliveryOrdertoOtherMap.put("Customer",CustomerModuleid);
        CustomDesignDeliveryOrdertoOtherMap.put("Company",CompanyModuleid);
        CustomDesignDeliveryOrdertoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
        CustomDesignDeliveryOrdertoOtherMap.put("Consignment Delivery Order", String.valueOf(Constants.Acc_ConsignmentDeliveryOrder_ModuleId));
    }
    
    public static TreeMap<String, String> CustomDesignGoodsReceiptOrdertoOtherMap = new TreeMap<String, String>();
    static{
        CustomDesignGoodsReceiptOrdertoOtherMap.put("Vendor",VendorModuleid);
        CustomDesignGoodsReceiptOrdertoOtherMap.put("Company",CompanyModuleid);
        CustomDesignGoodsReceiptOrdertoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
    }
    
    public static TreeMap<String, String> CustomDesignPaymenttoOtherMap = new TreeMap<String, String>();
    static{
//        CustomDesignPaymenttoOtherMap.put("Company","42");
        CustomDesignPaymenttoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
        CustomDesignPaymenttoOtherMap.put("Customer", CustomerModuleid);
        CustomDesignPaymenttoOtherMap.put("Vendor",VendorModuleid);
    }
    
    public static TreeMap<String, String> CustomDesignReceipttoOtherMap = new TreeMap<String, String>();
    static{
//        CustomDesignReceipttoOtherMap.put("Company","42");
        CustomDesignReceipttoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
        CustomDesignReceipttoOtherMap.put("Customer",CustomerModuleid);
        CustomDesignReceipttoOtherMap.put("Vendor", VendorModuleid);
    }
    
    public static TreeMap<String, String> ConsignmentDOtoOtherMap = new TreeMap<String, String>();

    static {
        ConsignmentDOtoOtherMap.put("Consignment Request", Acc_ConsignmentRequest_ModuleId);
    }
    
    public static TreeMap<String, String> CustomDesignSalesReturntoOtherMap = new TreeMap<String, String>();

    static {
        CustomDesignSalesReturntoOtherMap.put("Company",CompanyModuleid);
        CustomDesignSalesReturntoOtherMap.put("Company Preference", CompanyPreferences_ModuleId);
        CustomDesignSalesReturntoOtherMap.put("Customer",CustomerModuleid);
        CustomDesignSalesReturntoOtherMap.put("Consignment Sales Return",String.valueOf(Constants.Acc_ConsignmentSalesReturn_ModuleId));
    }
    
    public static TreeMap<String, String> CustomDesignPurchaseReturntoOtherMap = new TreeMap<String, String>();
    static{
        CustomDesignPurchaseReturntoOtherMap.put("Company",CompanyModuleid);
        CustomDesignPurchaseReturntoOtherMap.put("Company Preference",CompanyPreferences_ModuleId);
        CustomDesignPurchaseReturntoOtherMap.put("Vendor", VendorModuleid);
    }
    
    public static TreeMap<String, String> CustomDesignOtherCommonMap = new TreeMap<String, String>();    
    static{
        CustomDesignOtherCommonMap.put("Customer",CustomerModuleid);
        CustomDesignOtherCommonMap.put("Vendor",VendorModuleid);
    }
    
    public static TreeMap<String, String> CustomDesignJoinMap = new TreeMap<String, String>();    
    static{
        //Customer Invoice JoinMap
        CustomDesignJoinMap.put("invoice_customer","left join customer on customer.id = invoice.customer ");
        CustomDesignJoinMap.put("invoice_tax","left join tax on tax.id = invoice.tax ");
        CustomDesignJoinMap.put("invoice_company","left join company on company.companyid = invoice.company ");
        CustomDesignJoinMap.put("invoice_compaccpreferences","left join compaccpreferences on compaccpreferences.id = invoice.company ");
        
        //Vendor Invoice JoinMap
        CustomDesignJoinMap.put("goodsreceipt_vendor","left join vendor on vendor.id = goodsreceipt.vendor ");
        CustomDesignJoinMap.put("goodsreceipt_tax","left join tax on tax.id = goodsreceipt.tax ");
        CustomDesignJoinMap.put("goodsreceipt_company","left join company on company.companyid = goodsreceipt.company ");
        CustomDesignJoinMap.put("goodsreceipt_compaccpreferences","left join compaccpreferences on compaccpreferences.id = goodsreceipt.company ");
        
        //Sales Order JoinMap
        CustomDesignJoinMap.put("salesorder_customer","left join customer on customer.id = salesorder.customer ");
        CustomDesignJoinMap.put("salesorder_tax","left join tax on tax.id = salesorder.tax ");
        CustomDesignJoinMap.put("salesorder_company","left join company on company.companyid = salesorder.company ");
        CustomDesignJoinMap.put("salesorder_compaccpreferences","left join compaccpreferences on compaccpreferences.id  = salesorder.company ");
        
        //Purchase Order JoinMap
        CustomDesignJoinMap.put("purchaseorder_vendor","left join vendor on vendor.id = purchaseorder.vendor ");  
        CustomDesignJoinMap.put("purchaseorder_tax","left join tax on tax.id = purchaseorder.tax ");
        CustomDesignJoinMap.put("purchaseorder_company","left join company on company.companyid = purchaseorder.company ");
        CustomDesignJoinMap.put("purchaseorder_compaccpreferences","left join compaccpreferences on compaccpreferences.id = purchaseorder.company ");
        
        //Customer Quotation JoinMap
        CustomDesignJoinMap.put("quotation_customer","left join customer on customer.id = quotation.customer ");
        CustomDesignJoinMap.put("quotation_tax","left join tax on tax.id = quotation.tax ");
        CustomDesignJoinMap.put("quotation_company","left join company on company.companyid = quotation.company ");
        CustomDesignJoinMap.put("quotation_compaccpreferences","left join compaccpreferences on compaccpreferences.id = quotation.company ");
        
        //Vendor Quotation JoinMap
        CustomDesignJoinMap.put("vendorquotation_vendor","left join vendor on vendor.id = vendorquotation.vendor ");
        CustomDesignJoinMap.put("vendorquotation_tax","left join tax on tax.id = vendorquotation.tax ");  
        CustomDesignJoinMap.put("vendorquotation_company","left join company on company.companyid = vendorquotation.company ");
        CustomDesignJoinMap.put("vendorquotation_compaccpreferences","left join compaccpreferences on compaccpreferences.id  = vendorquotation.company ");
        
        //Delivery Order JoinMap
        CustomDesignJoinMap.put("deliveryorder_customer","left join customer on customer.id = deliveryorder.customer ");
        CustomDesignJoinMap.put("deliveryorder_company","left join company on company.companyid = deliveryorder.company ");
        CustomDesignJoinMap.put("deliveryorder_compaccpreferences","left join compaccpreferences on compaccpreferences.id = deliveryorder.company ");
        
        //GR Order JoinMap
        CustomDesignJoinMap.put("grorder_vendor","left join vendor on vendor.id = grorder.vendor ");  
        CustomDesignJoinMap.put("grorder_company","left join company on company.companyid = grorder.company ");
        CustomDesignJoinMap.put("grorder_compaccpreferences","left join compaccpreferences on compaccpreferences.id = grorder.company ");
        
        //Payment JoinMap
        CustomDesignJoinMap.put("payment_company","left join company on company.companyid = payment.company ");
        CustomDesignJoinMap.put("payment_compaccpreferences","left join compaccpreferences on compaccpreferences.id = payment.company ");
        
        //Receipt JoinMap
        CustomDesignJoinMap.put("receipt_company","left join company on company.companyid = receipt.company ");
        CustomDesignJoinMap.put("receipt_compaccpreferences","left join compaccpreferences on compaccpreferences.id  = receipt.company ");
        
        //SalesReturn JoinMap
        CustomDesignJoinMap.put("salesreturn_company","left join company on company.companyid = salesreturn.company ");
        CustomDesignJoinMap.put("salesreturn_compaccpreferences","left join compaccpreferences on compaccpreferences.id  = salesreturn.company ");
        
        //PurchaseReturn JoinMap
        CustomDesignJoinMap.put("purchasereturn_company", "left join company on company.companyid = purchasereturn.company ");
        CustomDesignJoinMap.put("purchasereturn_compaccpreferences", "left join compaccpreferences on compaccpreferences.id  = purchasereturn.company ");
        
        //CreditNote JoinMap
        CustomDesignJoinMap.put("creditnote_company", "left join company on company.companyid = debitnote.company ");
        CustomDesignJoinMap.put("creditnote_compaccpreferences", "left join compaccpreferences on compaccpreferences.id  = creditnote.company ");
        
        //DebitNote JoinMap
        CustomDesignJoinMap.put("debitnote_company", "left join company on company.companyid = debitnote.company ");
        CustomDesignJoinMap.put("debitnote_compaccpreferences", "left join compaccpreferences on compaccpreferences.id  = debitnote.company ");
        
        //Purchase Requisition JoinMap
        CustomDesignJoinMap.put("purchaserequisition_company", "left join company on company.companyid = purchaserequisition.company ");
        CustomDesignJoinMap.put("purchaserequisition_compaccpreferences", "left join compaccpreferences on compaccpreferences.id  = purchaserequisition.company ");
    }  
    
    
    //Multi-Entity fields
    public static final String CustomDesign_MultiEntity_GST_Number = "MultiEntityGSTNumber";
    public static final String CustomDesign_MultiEntity_Tax_Number = "MultiEntityTaxNumber";
    public static final String CustomDesign_MultiEntity_BRN_Number = "MultiEntityBRNNumber";
    public static final String CustomDesign_MultiEntity_MSIC_Code = "MultiEntityMSICCode";
    //Multi-Entity fields map
    public static HashMap<String, String> CustomDesignMultiEntityFieldsMap = new HashMap<String, String>(); 
    static{
        CustomDesignMultiEntityFieldsMap.put(CustomDesignerConstants.CustomDesign_MultiEntity_GST_Number, "{label:'Multi Entity GST No.',xtype:'1'}");
        CustomDesignMultiEntityFieldsMap.put(CustomDesignerConstants.CustomDesign_MultiEntity_Tax_Number, "{label:'Multi Entity Tax No.',xtype:'1'}");
        CustomDesignMultiEntityFieldsMap.put(CustomDesignerConstants.CustomDesign_MultiEntity_BRN_Number, "{label:'Multi Entity BRN No.',xtype:'1'}");
        CustomDesignMultiEntityFieldsMap.put(CustomDesignerConstants.CustomDesign_MultiEntity_MSIC_Code, "{label:'Multi Entity MSIC Code',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignCompanyAddressFieldsMap = new HashMap<String, String>(); 
    static{
        // Billing Address
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompAccPrefBillAddress_fieldTypeId, "{label:'Company Billing Address',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressField_fieldTypeId, "{label:'Company Billing Address Field',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressCity_fieldTypeId, "{label:'Company Billing Address City',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressState_fieldTypeId, "{label:'Company Billing Address State',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressCountry_fieldTypeId, "{label:'Company Billing Address Country',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressPostalCode_fieldTypeId, "{label:'Company Billing Address Postal Code',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressContactPerson_fieldTypeId, "{label:'Company Billing Address Contact Person',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressContactPersonNo_fieldTypeId, "{label:'Company Billing Address Contact Person No',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressPhoneNo_fieldTypeId, "{label:'Company Billing Address Phone No',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressMobileNo_fieldTypeId, "{label:'Company Billing Address Mobile No',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressFaxNo_fieldTypeId, "{label:'Company Billing Address Fax No',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyBillingAddressEmail_fieldTypeId, "{label:'Company Billing Address Email',xtype:'1'}");
        // Shipping Address
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompAccPrefShipAddress_fieldTypeId, "{label:'Company Shipping Address',xtype:'1'}"); 
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressField_fieldTypeId, "{label:'Company Shipping Address Field',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressCity_fieldTypeId, "{label:'Company Shipping Address City',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressState_fieldTypeId, "{label:'Company Shipping Address State',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressCountry_fieldTypeId, "{label:'Company Shipping Address Country',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressPostalCode_fieldTypeId, "{label:'Company Shipping Address Postal Code',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressContactPerson_fieldTypeId, "{label:'Company Shipping Address Contact Person',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressContactPersonNo_fieldTypeId, "{label:'Company Shipping Address Contact Person No',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressPhoneNo_fieldTypeId, "{label:'Company Shipping Address Phone No',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressMobileNo_fieldTypeId, "{label:'Company Shipping Address Mobile No',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressFaxNo_fieldTypeId, "{label:'Company Shipping Address Fax No',xtype:'1'}");
        CustomDesignCompanyAddressFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyShippingAddressEmail_fieldTypeId, "{label:'Company Shipping Address Email',xtype:'1'}");
    }
    public static HashMap<String, String> CustomDesignInvoiceExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignInvoiceExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignInvoiceExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "{label:'Quotation Reference Number',xtype:'1'}");        
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "{label:'Sales Order Reference Number',xtype:'1'}");        
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "{label:'Delivery Order Reference Number',xtype:'1'}"); 
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignDORef_Date_fieldTypeId, "{label:'Delivery Order Reference Date',xtype:'1'}"); 
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer Billing Contact Person',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId,"{label:'Customer Shipping Contact Person',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term,"{label:'Customer Term',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode,"{label:'Customer Account Code',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.InvoiceAmountDue,"{label:'Balance Due',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AmountDueInWords,"{label:'Balance Due In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.PaymentReceivedInWords,"{label:'Payment Received In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.InvoicePaymentReceived,"{label:'Payment Received',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Customer Code',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.Total_Quantity_UOM,"{label:'Total Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,isNumeric:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "{label:'Currency Symbol',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");

        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, "{label:'Exchanged Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, "{label:'Exchanged Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, "{label:'Exchanged Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, "{label:'Exchanged SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true}");        
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, "{label:'Exchanged Term Amount',xtype:'2',isFromUnitPriceAndAmount:true}");        
        
        
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, "{label:'Terms Name',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.TotalAmountWithoutTerm, "{label:'Amount Without Term',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.SalesOrderDate, "{label:'Sales Order Date',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}"); 
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, "{label:'Total Term',xtype:'2',isFromUnitPriceAndAmount:true}");  
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");  
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPreText_fieldTypeId, "{label:'Company PreText',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_Print, "{label:'Printed On',xtype:'3'}");  
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true}");  
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true}"); //ERP-25162
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_ItemsNo, "{label:'Number Of Items',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.Customer_AccountNo_fieldTypeId, "{label:'Customer Account No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllTerms, "{label:'All Terms',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllTermNames, "{label:'All Term Names',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllTermSigns, "{label:'All Term Signs',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTermSigns, "{label:'All Line Level Term Signs',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllTermAmounts, "{label:'All Term Amounts',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.totalAmountForServiceTaxInvoice, "{label:'Total Amount For Service Tax Invoice',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.swacchaBharatCessForServiceTaxInvoice, "{label:'Swachha Bharat Cess @ 0.50%',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.serviceTaxRateForServiceTaxInvoice, "{label:'Service Tax Rate @ 14%',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.roundingDifference, "{label:'Rounding Difference',xtype:'1'}"); //ERP-25876
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CompanyBankIFSCCode, "{label:'Company Bank IFSC Code',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CompanyBankAccountNumber, "{label:'Company Bank Account Number',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_TITLE, "{label:'Customer Title',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.TOTAL_TAX_BEFORE_PARTIAL_PAYMENT, "{label:'Total Tax Before Partial Payment',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.SUBTOTAL_BEFORE_PARTIAL_PAYMENT, "{label:'Sub Total Before Partial Payment',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.SUBTOTAL_AND_TAX_BEFORE_PARTIAL_PAYMENT, "{label:'Sub Total+Tax Before Partial Payment',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.TOTAL_BEFORE_PARTIAL_PAYMENT, "{label:'Total Amount Before Partial Payment',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.REMAINING_BALANCE_DUE, "{label:'Remaining Balance Due',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CurrentUserFullName, "{label:'Current User Full Name',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CurrentUserContactNumber, "{label:'Current User Contact Number',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.INVOICE_CREATION_DATE, "{label:'Invoice Created on',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.INVOICE_UPDATION_DATE, "{label:'Invoice Updated on',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomJobWorkOrderDate, "{label:'Job Work Order Date',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomJobWorkInNo, "{label:'Job Work In No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomJobWorkOrderNo, "{label:'Job Work Order No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.SHIPMENT_TRACKING_NO, "{label:'Shipment Tracking No.',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "{label:'Customer Billing Mobile No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingMobileNo_fieldTypeId, "{label:'Customer Shipping Mobile No',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorAlice_Name, "{label:'Customer Alias Name',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, "{label:'Delivery Date',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, "{label:'Delivery Time',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDriver, "{label:'Delivery Driver',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, "{label:'Delivery Vehicle No.',xtype:'1'}");
        
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, "{label:'Specific Currency Term Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "{label:'Exchange Rate (GST)', xtype:'2',isFromUnitPriceAndAmount:false, defwidth:10, seq:63}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.CountOfBatches, "{label:'Count Of Batches',xtype:'1'}");
        CustomDesignInvoiceExtraFieldsMap.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, "{label:'Amount Before Tax', xtype:'2',isFromUnitPriceAndAmount:true}");
    
    }
    
    public static HashMap<String, String> CustomDesignVendorInvoiceExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignVendorInvoiceExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignVendorInvoiceExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, "{label:'Vendor Quotation Reference Number',xtype:'1'}");        
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, "{label:'Purchase Order Reference Number',xtype:'1'}");        
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, "{label:'Good Receipts Order Reference Number',xtype:'1'}"); 
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Vendor Billing Email',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId,"{label:'Vendor Shipping Contact Person',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term,"{label:'Vendor Term',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode,"{label:'Vendor Account Code',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Vendor Code',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, "{label:'Exchanged Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, "{label:'Exchanged Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, "{label:'Exchanged Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, "{label:'Exchanged SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true}");        
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, "{label:'Exchanged Term Amount',xtype:'2',isFromUnitPriceAndAmount:true}");     
        
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, "{label:'Terms Name',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, "{label:'Total Term',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "{label:'Vendor Billing Mobile No',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_Print, "{label:'Printed On',xtype:'3'}");  
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Debit Term',xtype:'1'}");  
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPreText_fieldTypeId, "{label:'Company PreText',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.VendorTransactionalShipTo, "{label:'Vendor Transactional ShipTo',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllTerms, "{label:'All Terms',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.InvoiceAmountDue,"{label:'Balance Due',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.VENDOR_TITLE, "{label:'Vendor Title',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.INVOICE_CREATION_DATE, "{label:'Invoice Created on',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.INVOICE_UPDATION_DATE, "{label:'Invoice Updated on',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllTermNames, "{label:'All Term Names',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllTermSigns, "{label:'All Term Signs',xtype:'1'}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.AllTermAmounts, "{label:'All Term Amounts',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, "{label:'Specific Currency Term Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "{label:'Exchange Rate (GST)', xtype:'2',isFromUnitPriceAndAmount:false, defwidth:10, seq:62}");
        CustomDesignVendorInvoiceExtraFieldsMap.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, "{label:'Amount Before Tax', xtype:'2',isFromUnitPriceAndAmount:true}");
        
    }
    
    public static HashMap<String, String> CustomDesignDOExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignDOExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignDOExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "{label:'Quotation Reference Number',xtype:'1'}");        
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "{label:'Sales Order Reference Number',xtype:'1'}");        
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignInvRefNumber_fieldTypeId, "{label:'Invoice Reference Number',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer Billing Contact Person',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Customer Shipping Contact Person',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmail1_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmail1_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "{label:'Currency Symbol',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Customer Term',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode, "{label:'Customer Account Code',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Customer Code',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_MappingSalesPerson,"{label:'Map Sales Person',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CR_memo, "{label:'CR Memo',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CR_SalesPerson, "{label:'CRSalesPerson',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CR_fromdate, "{label:'CRFromdate',xtype:'3'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CR_todate, "{label:'CRTodate',xtype:'3'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.Consocreatedby, "{label:'SO Createdby',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "{label:'Customer Billing Mobile No',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingMobileNo_fieldTypeId, "{label:'Customer Shipping Mobile No',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.SalesOrderDate, "{label:'Sales Order Date',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.InvoiceDate, "{label:'Invoice Date',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}"); 
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.TOTAL_DELIVERED_QUANTITY, "{label:'Total Delivered Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.Poreferencenumber, "{label:'PO Reference No',xtype:'1'}"); 
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPreText_fieldTypeId, "{label:'Company PreText',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_ItemsNo, "{label:'Number Of Items',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, "{label:'Total Term',xtype:'2',isFromUnitPriceAndAmount:true}");  
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.TotalAmountWithoutTerm, "{label:'Amount Without Term',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, "{label:'Terms Name',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.AllTerms, "{label:'All Terms',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.Total_Quantity_UOM,"{label:'Total Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,isNumeric:true}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.TOTAL_DELIVERED_QUANTITY_UOM,"{label:'Total Delivered Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,isNumeric:true}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, "{label:'Credit Term',xtype:'1',isNumeric:true}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_TITLE, "{label:'Customer Title',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CurrentUserFullName, "{label:'Current User Full Name',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CurrentUserContactNumber, "{label:'Current User Contact Number',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.NO_OF_PACKAGE, "{label:'No of Package',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.PACKAGE_NO, "{label:'Package No',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.PACK_CURRENT_WAREHOUSE, "{label:'Pack Current Warehouse',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.SHIPMENT_TRACKING_NO, "{label:'Shipment Tracking No.',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CountOfBatches, "{label:'Count Of Batches',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.TOTAL_GROSS_WEIGHT, "{label:'Total Gross Weight',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, "{label:'Delivery Date',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, "{label:'Delivery Time',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDriver, "{label:'Delivery Driver',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, "{label:'Delivery Vehicle No.',xtype:'1'}");
        CustomDesignDOExtraFieldsMap.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, "{label:'Amount Before Tax', xtype:'2',isFromUnitPriceAndAmount:true}");
    }
    
    public static HashMap<String, String> CustomDesignGROExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignGROExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignGROExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, "{label:'Vendor Quotation Reference Number',xtype:'1'}");        
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, "{label:'Purchase Order Reference Number',xtype:'1'}");        
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CSUTOMDESIGNER_PO_REF_DATE, "{label:'Purchase Order Date',xtype:'1'}");        
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVenInvRefNumber_fieldTypeId, "{label:'Vendor Invoice Reference Number',xtype:'1'}"); 
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Vendor Billing Email',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Vendor Shipping Email',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code, "{label:'Vendor Code',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Vendor Term',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, "{label:'Debit Term',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorContactBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Contact Person Number',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorContactShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Contact Person Number',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "{label:'Vendor Billing Mobile No',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.VendorBillTo, "{label:'Vendor Bill To',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.VendorShipTo, "{label:'Vendor Ship To',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Vendor Shipping Contact Person',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.VendorTransactionalShipTo, "{label:'Vendor Transactional ShipTo',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, "{label:'Total Term',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.TotalAmountWithoutTerm, "{label:'Amount Without Term',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, "{label:'Terms Name',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.AllTerms, "{label:'All Terms',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.VENDOR_TITLE, "{label:'Vendor Title',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Vendor CST TIN NO',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignNetDebitTerm_fieldTypeId, "{label:'NET Debit Term',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.CustomDesignNetVendorTerm_fieldTypeId, "{label:'NET Vendor Term',xtype:'1'}");
        CustomDesignGROExtraFieldsMap.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, "{label:'Amount Before Tax', xtype:'2',isFromUnitPriceAndAmount:true}");
    }
    public static HashMap<String, String> CustomDesignCustomerQuotationExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignCustomerQuotationExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignCustomerQuotationExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, "{label:'Credit Term',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Customer Code',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPreText_fieldTypeId, "{label:'Company PreText',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignPORefNo, "{label:'Customer PO Ref No',xtype:'1'}");
        
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, "{label:'Exchanged Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, "{label:'Exchanged Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, "{label:'Exchanged Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, "{label:'Exchanged SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true}");        
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, "{label:'Exchanged Term Amount',xtype:'2',isFromUnitPriceAndAmount:true}");     
         
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, "{label:'Terms Name',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, "{label:'Total Term',xtype:'2',isFromUnitPriceAndAmount:true}");  
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer Billing Contact Person',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Customer Shipping Contact Person',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_ItemsNo, "{label:'Number Of Items',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.AllTerms, "{label:'All Terms',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_TITLE, "{label:'Customer Title',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CurrentUserFullName, "{label:'Current User Full Name',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CurrentUserContactNumber, "{label:'Current User Contact Number',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.AllTermNames, "{label:'All Term Names',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.AllTermSigns, "{label:'All Term Signs',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.AllTermAmounts, "{label:'All Term Amounts',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.REPLACEMENT_NUMBER, "{label:'Replacement Number',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, "{label:'Delivery Date',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, "{label:'Delivery Time',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDriver, "{label:'Delivery Driver',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, "{label:'Delivery Vehicle No.',xtype:'1'}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, "{label:'Specific Currency Term Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        CustomDesignCustomerQuotationExtraFieldsMap.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, "{label:'Amount Before Tax', xtype:'2',isFromUnitPriceAndAmount:true}");
    }
    public static HashMap<String, String> CustomDesignVendorQuotationExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignVendorQuotationExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignVendorQuotationExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Vendor Billing Email',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Vendor Code',xtype:'1'}");  
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "{label:'Currency Symbol',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Vendor Shipping Contact Person',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Vendor Term',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, "{label:'Exchanged Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, "{label:'Exchanged Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, "{label:'Exchanged Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, "{label:'Exchanged SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true}");        
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, "{label:'Exchanged Term Amount',xtype:'2',isFromUnitPriceAndAmount:true}");     
        
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, "{label:'Terms Name',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, "{label:'Total Term',xtype:'2',isFromUnitPriceAndAmount:true}"); 
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "{label:'Vendor Billing Mobile No',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.VendorTransactionalShipTo, "{label:'Vendor Transactional ShipTo',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.AllTerms, "{label:'All Terms',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.VENDOR_TITLE, "{label:'Vendor Title',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Vendor CST TIN NO',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CurrentUserFullName, "{label:'Current User Full Name',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.CurrentUserContactNumber, "{label:'Current User Contact Number',xtype:'1'}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, "{label:'Specific Currency Term Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        CustomDesignVendorQuotationExtraFieldsMap.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, "{label:'Amount Before Tax', xtype:'2',isFromUnitPriceAndAmount:true}");
    
    }
    public static HashMap<String, String> CustomDesignSalesOrderExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignSalesOrderExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignSalesOrderExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer Billing Contact Person',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Customer Shipping Contact Person',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "{label:'Customer Quotation Reference Number',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Customer Term',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode, "{label:'Customer Account Code',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Customer Code',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.NoOfDays, "{label:'No of days',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, "{label:'Exchanged Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, "{label:'Exchanged Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, "{label:'Exchanged Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, "{label:'Exchanged SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true}");        
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, "{label:'Exchanged Term Amount',xtype:'2',isFromUnitPriceAndAmount:true}");     
        
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, "{label:'Terms Name',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, "{label:'Total Term',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_Print, "{label:'Printed On',xtype:'3'}");  
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, "{label:'Purchase Order Reference Number',xtype:'1'}"); 
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CSUTOMDESIGNER_PO_REF_DATE, "{label:'Purchase Order Date',xtype:'1'}"); 
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_ItemsNo, "{label:'Number Of Items',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All GlobalLevel Customfields',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, "{label:'Credit Term Days',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.AllTerms, "{label:'All Terms',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_TITLE, "{label:'Customer Title',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.Total_Quantity_UOM,"{label:'Total Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,isNumeric:true}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, "{label:'Delivery Date',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, "{label:'Delivery Time',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDriver, "{label:'Delivery Driver',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, "{label:'Delivery Vehicle No.',xtype:'1'}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, "{label:'Specific Currency Term Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, "{label:'Amount Before Tax', xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesOrderExtraFieldsMap.put(CustomDesignerConstants.SO_Status, "{label:'Status', xtype:'1'}");
        }
    public static HashMap<String, String> CustomDesignPurchaseOrderExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignPurchaseOrderExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignPurchaseOrderExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "{label:'Vendor Billing Mobile No',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Vendor Billing Email',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Vendor Shipping Email',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "{label:'Sales Order Reference Number',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, "{label:'Vendor Quotation Reference Number',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId,"{label:'Vendor Shipping Contact Person',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term,"{label:'Vendor Term',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode,"{label:'Vendor Account Code',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Vendor Code',xtype:'1'}"); 
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorQuantity_Total,"{label:'Total Quantity',xtype:'1'}"); 
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOCustomerName, "{label:'Sales Order Customer Name',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOCustomerCode, "{label:'Sales Order Customer Code',xtype:'1'}");
        
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, "{label:'Exchanged Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, "{label:'Exchanged Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, "{label:'Exchanged Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, "{label:'Exchanged SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, "{label:'Exchanged Term Amount',xtype:'2',isFromUnitPriceAndAmount:true}");     
        
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignBaseCurrency_fieldTypeId, "{label:'Base Currency',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVendorCurrency_fieldTypeId, "{label:'Vendor Currency',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.Approvedby, "{label:'Approver name',xtype:'1'}"); 
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, "{label:'Terms Name',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, "{label:'Total Term',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_Print, "{label:'Printed On',xtype:'3'}");  
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.VendorTransactionalShipTo, "{label:'Vendor Transactional ShipTo',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.AllTerms, "{label:'All Terms',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CustomDesignPRRefNo, "{label:'PR NO.',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.VENDOR_TITLE, "{label:'Vendor Title',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.AllTermNames, "{label:'All Term Names',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.AllTermSigns, "{label:'All Term Signs',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.AllTermAmounts, "{label:'All Term Amounts',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.PURCHASEREQCREATOR, "{label:'Purchase Requisition Creator',xtype:'1'}");
        
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIP_TO, "{label:'Customer Ship To',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_ADDRESS, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_COUNTRY, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_STATE, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CITY, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_POSTAL, "{label:'Customer Shipping Postal',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_EMAIL, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_FAX, "{label:'Customer Shipping Fax',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_MOBILE, "{label:'Customer Shipping Mobile',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_PHONE, "{label:'Customer Shipping Phone',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CONTACT_PERSON_NO, "{label:'Customer Shipping Contact Person Number',xtype:'1'}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_SHIPPING_CONTACT_PERSON, "{label:'Customer Shipping Contact Person',xtype:'1'}");
        
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, "{label:'Specific Currency Term Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, "{label:'Amount Before Tax', xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseOrderExtraFieldsMap.put(CustomDesignerConstants.PO_Status, "{label:'Status', xtype:'1'}");
    
    }
    
        public static HashMap<String, String> CustomDesignReceivePaymentExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignReceivePaymentExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignReceivePaymentExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Customer Term',xtype:'1'}");

        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.PaymentAccount, "{label:'Payment Account',xtype:'2'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_AdvanceAmount, "{label:'Advance Amount',xtype:'2'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Include_GST, "{label:'GST Tax',xtype:'2'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2'}");
        
        //Customer Invoice Details
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo, "{label:'Customer Invoice(s)',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_InvoiceDate, "{label:'Invoice Dates',xtype:'3'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_DueDate, "{label:'Invoice Due Dates',xtype:'3'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_Tax, "{label:'Invoice Tax',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_Discount, "{label:'Invoice Discount',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_OriginalAmount, "{label:'Invoice Original Amount',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Invoice_Original_Amount_Due, "{label:'Invoice Original Amount Due',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Invoice_Exchange_Rate, "{label:'Invoice Exchange Rate',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_AmountDue, "{label:'Invoice Amount Due',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_EnterPayment, "{label:'Invoice Enter Payment',xtype:'1'}");
        //Bank Details
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Chequeno, "{label:'Cheque No',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.BankName, "{label:'Bank Name',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.ChequeDate, "{label:'Cheque Date',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.BankDescription, "{label:'Bank Description',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Cheque_Payment_Status, "{label:'Payment Status',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "{label:'Cheque Payment Clearance Date',xtype:'1'}");

        //2nd option-Receive payment from Vendor:
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_CustomerVendorName, "{label:'Vendor',xtype:'1'}");
        
        //3rd option-Debit Note
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.paymentDebit_note, "{label:'Debit Note Number',xtype:'1'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.paymentDebit_noteAmount, "{label:'Debit Note Amount',xtype:'1'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.paymentDebit_note_AmountDue, "{label:'Debit Note Amount Due',xtype:'1'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.paymentDebit_note_EnterPayment, "{label:'Debit Note Enter Payment',xtype:'1'}");

        //4rth option-GL Code-Receive payment against GL Code:
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.GridType, "{label:'Grid Type',xtype:'1'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.GridAccountName, "{label:'Account',xtype:'1'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.GridAmountinSGD, "{label:'Account Amount',xtype:'2'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.GridDesc, "{label:'Account Description',xtype:'1'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.GridTax, "{label:'Account Tax',xtype:'1'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.GridTaxAmount, "{label:'Account Tax Amount',xtype:'1'}");
         CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.GridAmountinTax, "{label:'Account Amount with Tax',xtype:'1'}");
         
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No.',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "{label:'Quotation Reference Number',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "{label:'Sales Order Reference Number',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "{label:'Delivery Order Reference Number',xtype:'1'}");
        //Appended Text
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, "{label:'Common Number',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Common_EnterPayment, "{label:'Common EnterPayment',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer Billing Contact Person',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Customer Shipping Contact Person',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, "{label:'Delivery Date',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, "{label:'Delivery Time',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDriver, "{label:'Delivery Driver',xtype:'1'}");
        CustomDesignReceivePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, "{label:'Delivery Vehicle No.',xtype:'1'}");

    }
    
        //New ReceivePayment UI    
    public static HashMap<String, String> CustomDesignReceivePaymentNewExtraFieldsMap = new HashMap<String, String>();

    static {
        CustomDesignReceivePaymentNewExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignReceivePaymentNewExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Total_Address, "{label:'Vendor Total Address',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode, "{label:'Vendor Account Code',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccCode, "{label:'Vendor Acc Code',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccName, "{label:'Vendor Acc Name',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Vendor Term',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Address_PostalCode, "{label:'Vendor Address With Postal Code',xtype:'1'}");

        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.PaymentMethod, "{label:'Payment Method',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.PaymentAccount, "{label:'Payment Account',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Include_GST, "{label:'GST Tax',xtype:'2'}");

        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, "{label:'Total Tax',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignOriginalAmountDueTotal, "{label:'Original Amount Due Total',xtype:'2'}");  //ERP-19271
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountPaid_fieldTypeId, "{label:'Amount Paid',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.TOTALAMOUNT_WITHBANCKCHARGE, "{label:'Total Amount With Bank Charges',xtype:'2'}");

        //common value for all three types
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_DocumentType, "{label:'Document Type',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_DocumentNumber, "{label:'Document Number',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_Description, "{label:'Description',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_OriginalAmountDue, "{label:'Original Amount Due',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_ExchangeRate, "{label:'Exchange Rate',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_AmountDue, "{label:'Amount Due',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_Tax, "{label:'Tax%',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_TaxCode, "{label:'Tax Code',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_TaxAmount, "{label:'Tax Amount',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_EnterPayment, "{label:'Enter Payment',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_TAXNAME, "{label:'Tax Name',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.DOCUMENTSTATUS, "{label:'Document Status',xtype:'1'}");
        
        //1st option-Receive payment from Customer:
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_InvoiceDate, "{label:'Invoice Dates',xtype:'3'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_DueDate, "{label:'Due Dates',xtype:'3'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_Discount, "{label:'Invoice Discount',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_OriginalAmount, "{label:'Invoice Original Amount',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "{label:'Currency Symbol',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_InvoiceTotalDiscount, "{label:'Invoice Total Discount',xtype:'2'}");

        //2nd option-Receive payment from Vendor:
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_CustomerVendorName, "{label:'Customer/ Vendor Name',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode, "{label:'Customer/Vendor Account Code',xtype:'1'}");

        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No.',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "{label:'Quotation Reference Number',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "{label:'Sales Order Reference Number',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "{label:'Delivery Order Reference Number',xtype:'1'}");
        // Against GL code
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, "{label:'Account Code',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_CreditAmount, "{label:'Credit Amount',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_DebitAmount, "{label:'Debit Amount',xtype:'2'}");

        //Bank Details
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Chequeno, "{label:'Cheque No',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.BankName, "{label:'Bank Name',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.ChequeDate, "{label:'Cheque Date',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.BankDescription, "{label:'Bank Description',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Cheque_Payment_Status, "{label:'Payment Status',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "{label:'Cheque Payment Clearance Date',xtype:'1'}");
        //Card Holder Details
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CardNo, "{label:'Card Number',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CardHolderName, "{label:'Card Holder Name',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Card_Reference_Number, "{label:'Card Reference Number',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Card_Type, "{label:'Card Type',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Card_ExpiryDate, "{label:'Card ExpiryDate',xtype:'1'}");
        //Appended Text
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, "{label:'Common Number',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Common_EnterPayment, "{label:'Common EnterPayment',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Bank_AccountCode, "{label:'Bank Account Code',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Bank_AccountNumber, "{label:'Bank Account Number',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer Billing Contact Person',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Customer Shipping Contact Person',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        
        //Company Details
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_OR_VENDOR_TITLE, "{label:'Customer/Vendor Title',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CURRENCY_EXCHANGE_RATE, "{label:'Currency Exchange Rate',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Customer/Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Customer/Vendor CST TIN NO',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignNetCreditTerm_fieldTypeId, "{label:'NET Credit Term',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.GSTAmountInWords, "{label:'GST Amount In Words',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, "{label:'Delivery Date',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, "{label:'Delivery Time',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDriver, "{label:'Delivery Driver',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, "{label:'Delivery Vehicle No.',xtype:'1'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId, "{label:'Discount',xtype:'2'}");
        CustomDesignReceivePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "{label:'Exchange Rate (GST)', xtype:'2', defwidth:10}");

    }
    
      public static HashMap<String, String> CustomDesignMakePaymentExtraFieldsMap = new HashMap<String, String>();
    static {
        //Vendor Addresses Details
        CustomDesignMakePaymentExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignMakePaymentExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Main_Vendor_Name, "{label:'Vendor Name',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode, "{label:'Vendor Account Code',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Vendor Term',xtype:'1'}");
        
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.PaymentMethod, "{label:'Payment Method',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.PaymentAccount, "{label:'Payment Account',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_AdvanceAmount, "{label:'Advance Amount',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Include_GST, "{label:'GST Tax',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2'}");

        //Vendor Invoice Details
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo, "{label:'Vendor Invoice(s)',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_InvoiceDate, "{label:'Invoice Dates',xtype:'3'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_DueDate, "{label:'Due Dates',xtype:'3'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_Tax, "{label:'Invoice Tax',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_Discount, "{label:'Invoice Discount',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_OriginalAmount, "{label:'Invoice Original Amount',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Invoice_Original_Amount_Due, "{label:'Invoice Original Amount Due',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Invoice_Exchange_Rate, "{label:'Invoice Exchange Rate',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_AmountDue, "{label:'Invoice Amount Due',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_EnterPayment, "{label:'Invoice Enter Payment',xtype:'2'}");
        
        //Bank Details
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Chequeno, "{label:'Cheque No',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.BankName, "{label:'Bank Name',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.ChequeDate, "{label:'Cheque Date',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.BankDescription, "{label:'Bank Description',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Cheque_Payment_Status, "{label:'Payment Status',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "{label:'Cheque Payment Clearance Date',xtype:'1'}");
        //Card Holder Details
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CardNo, "{label:'Card Number',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CardHolderName, "{label:'Card Holder Name',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Card_Reference_Number, "{label:'Card Reference Number',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Card_Type, "{label:'Card Type',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Card_ExpiryDate, "{label:'Card ExpiryDate',xtype:'1'}");

        //2nd option-Make payment from Vendor:
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RPMP_CustomerVendorName, "{label:'Customer',xtype:'1'}");

        //3rd option-Debit Note
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.paymentDebit_note, "{label:'Credit Note Number',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.paymentDebit_noteAmount, "{label:'Credit Note Amount',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.paymentDebit_note_AmountDue, "{label:'Credit Note Amount Due',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.paymentDebit_note_EnterPayment, "{label:'Credit Note Enter Payment',xtype:'1'}");

        //4rth option-GL Code-Make payment against GL Code:
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.GridType, "{label:'Grid Type',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.GridAccountName, "{label:'Account',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.GridAmountinSGD, "{label:'Account Amount',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.GridDesc, "{label:'Account Description',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.GridTax, "{label:'Account Tax',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.GridTaxAmount, "{label:'Account Tax Amount',xtype:'2'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.GridAmountinTax, "{label:'Account Amount with Tax',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.GridAccountCode, "{label:'Account Code',xtype:'1'}");

        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No.',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, "{label:'Vendor Quotation Reference Number',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, "{label:'Good Receipts Order Reference Number',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, "{label:'Purchase Order Reference Number',xtype:'1'}");
        //Appended Text
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, "{label:'Common Number',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Common_EnterPayment, "{label:'Common EnterPayment',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Bank_AccountCode, "{label:'Bank Account Code',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Bank_AccountNumber, "{label:'Bank Account Number',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Vendor Shipping Contact Person',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignMakePaymentExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        
    }
    
//New MakePayment UI    
    public static HashMap<String, String> CustomDesignMakePaymentNewExtraFieldsMap = new HashMap<String, String>();

    static {
        //Vendor Addresses Details
        CustomDesignMakePaymentNewExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignMakePaymentNewExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Vendor Billing Email',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Vendor Shipping Email',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Total_Address, "{label:'Vendor Total Address',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Address_PostalCode, "{label:'Vendor Address With Postal Code',xtype:'1'}");
//        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Main_Vendor_Name, "{label:'Vendor Name',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode, "{label:'Vendor Account Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Vendor Term',xtype:'1'}");
        
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.PaymentMethod, "{label:'Payment Method',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.PaymentAccount, "{label:'Payment Account',xtype:'1'}");
//        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Include_GST, "{label:'GST Tax',xtype:'2'}");

        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, "{label:'Total Tax',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignOriginalAmountDueTotal, "{label:'Original Amount Due Total',xtype:'2'}");  //ERP-19271
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountPaid_fieldTypeId, "{label:'Amount Paid',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.TOTALAMOUNT_WITHBANCKCHARGE, "{label:'Total Amount With Bank Charges',xtype:'2'}");

        //common value for all three types
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_DocumentType, "{label:'Document Type',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_DocumentNumber, "{label:'Document Number',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_Description, "{label:'Description',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_OriginalAmountDue, "{label:'Original Amount Due',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_ExchangeRate, "{label:'Exchange Rate',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_AmountDue, "{label:'Amount Due',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_Tax, "{label:'Tax%',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_TaxCode, "{label:'Tax Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_TaxAmount, "{label:'Tax Amount',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_EnterPayment, "{label:'Enter Payment',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_ENTER_PAYMENT_WITH_TAX, "{label:'Enter Payment With Tax',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_TAXNAME, "{label:'Tax Name',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.DOCUMENTSTATUS, "{label:'Document Status',xtype:'1'}");
        
        //Against GL Code
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, "{label:'Account Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_CreditAmount, "{label:'Credit Amount',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_DebitAmount, "{label:'Debit Amount',xtype:'2'}");

        //1st option-Make payment to Vendor:
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_InvoiceDate, "{label:'Invoice Dates',xtype:'3'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_DueDate, "{label:'Due Dates',xtype:'3'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_Discount, "{label:'Invoice Discount',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_OriginalAmount, "{label:'Invoice Original Amount',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "{label:'Currency Symbol',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_InvoiceTotalDiscount, "{label:'Invoice Total Discount',xtype:'2'}");
        
        //2nd option-Make payment to Customer:
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RPMP_CustomerVendorName, "{label:'Customer/ Vendor Name',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode, "{label:'Customer/Vendor Account Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccCode, "{label:'Customer/Vendor Acc Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccName, "{label:'Customer/Vendor Acc Name',xtype:'1'}");

        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No.',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, "{label:'Vendor Quotation Reference Number',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, "{label:'Good Receipts Order Reference Number',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, "{label:'Purchase Order Reference Number',xtype:'1'}");

        //Bank Details
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Chequeno, "{label:'Cheque No',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.BankName, "{label:'Bank Name',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.ChequeDate, "{label:'Cheque Date',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.BankDescription, "{label:'Bank Description',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Cheque_Payment_Status, "{label:'Payment Status',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "{label:'Cheque Payment Clearance Date',xtype:'1'}");
        //Card Holder Details
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CardNo, "{label:'Card Number',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CardHolderName, "{label:'Card Holder Name',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Card_Reference_Number, "{label:'Card Reference Number',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Card_Type, "{label:'Card Type',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Card_ExpiryDate, "{label:'Card ExpiryDate',xtype:'1'}");
        //Appended Text
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, "{label:'Common Number',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Common_EnterPayment, "{label:'Common EnterPayment',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Bank_AccountCode, "{label:'Bank Account Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Bank_AccountNumber, "{label:'Bank Account Number',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Vendor Shipping Contact Person',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        //Company Deatails
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_OR_VENDOR_TITLE, "{label:'Customer/Vendor Title',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.JE_ACCOUNT_CODE, "{label:'JE Account Code',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.JE_ACCOUNT_NO, "{label:'JE Account No',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.JE_ACCOUNT_NAME, "{label:'JE Account Name',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.JE_DESCRIPTION, "{label:'JE Description',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.JE_CREDIT_AMOUNT, "{label:'JE Credit Amount',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.JE_DEBIT_AMOUNT, "{label:'JE Debit Amount',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CURRENCY_EXCHANGE_RATE, "{label:'Currency Exchange Rate',xtype:'2'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Customer/Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Customer/Vendor CST TIN NO',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.SUPPLIER_INVOICE_NO, "{label:'Supplier Invoice No',xtype:'1'}");
        CustomDesignMakePaymentNewExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "{label:'Exchange Rate (GST)', xtype:'2', defwidth:10}");
        
    }
         public static HashMap<String, String> CustomDesignCreditNoteExtraFieldsMap = new HashMap<String, String>(); 
    static {
        //Customer
        CustomDesignCreditNoteExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignCreditNoteExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Customer Term',xtype:'1'}");
         CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmail1_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");
         CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmail1_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");

        //Common Fields for both Customer,Vendor & GL
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No.',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceNo_fieldTypeId, "{label:'Invoice Number',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceAmount_fieldTypeId, "{label:'Invoice Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceTax_fieldTypeId, "{label:'Tax Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceAmountDue_fieldTypeId, "{label:'Invoice Amount Due',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId, "{label:'Enter Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_INvoiceDates_fieldTypeId, "{label:'Invoice Dates',xtype:'3'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.InvoiceSubTotal, "{label:'InvoiceSubTotal',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.InvoiceTotalTax, "{label:'InvoiceTotalTax',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.InvoiceTotalAmount, "{label:'InvoiceTotalAmount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.InvoiceTax, "{label:'Invoice Tax',xtype:'2'}");
        
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountCode_fieldTypeId, "{label:'Account Code',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_Account_fieldTypeId, "{label:'Account Name',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountAmount_fieldTypeId, "{label:'Account Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountTax_fieldTypeId, "{label:'Account Tax',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountTaxAmount_fieldTypeId, "{label:'Account Tax Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountTotalAmount_fieldTypeId, "{label:'Account Total Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountDescription_fieldTypeId, "{label:'Account Description',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountReason_fieldTypeId, "{label:'Account Reason',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountAmountExcludeGST_fieldTypeId, "{label:'Account Amount Excluding GST',xtype:'2'}");
        
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AccountSubTotal, "{label:'AccSubTotal',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AccountTotalTax, "{label:'AccTotalTax',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AccountTotalAmount, "{label:'AccTotalAmount',xtype:'2'}");
        
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.Customer_AccountNo_fieldTypeId, "{label:'Vendor/Customer Account No',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendoraccountcode, "{label:'Vendor/Customer Account Code',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendorCode, "{label:'Vendor/Customer Code',xtype:'1'}");
        //Address
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer/Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");

        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmail1_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Customer/Vendor Shipping Contact Person',xtype:'1'}");
        
        //Other Fields
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, "{label:'Total Tax',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyUEN_fieldTypeId, "{label:'Company UEN No',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyGRN_No_fieldTypeId, "{label:'Company GRN No',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, "{label:'Credit Term',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignNetCreditTerm_fieldTypeId, "{label:'NET Credit Term',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "{label:'Vendor Billing Mobile No',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceSalesPerson_fieldTypeId, "{label:'Invoice Sales Person',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.SR_LinkTo, "{label:'Linked Reference Number',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountAmount, "{label:'Base Currency Account Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountTaxAmount, "{label:'Base Currency Account Tax Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountAmountwithTax, "{label:'Base Currency Account Total Amount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountSubTotal, "{label:'Base Currency AccSubTotal',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountTotalTax, "{label:'Base Currency AccTotalTax',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountTotalAmount, "{label:'Base Currency AccTotalAmount',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.SummaryTaxPercent, "{label:'Account Tax Percent',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyInvoiceSubTotal, "{label:'Base Currency Invoice SubTotal',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyInvoiceTotalTax, "{label:'Base Currency Invoice TotalTax',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyInvoiceTotal, "{label:'Base Currency Invoice Total',xtype:'2'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_OR_VENDOR_TITLE, "{label:'Customer/Vendor Title',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Customer/Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Customer/Vendor CST TIN NO',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CompanyPANNumber, "{label:'Company Pan Number',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTax, "{label:'All Line Level Tax',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTaxAmount, "{label:'All Line Level Tax Amount',xtype:'1',isNumeric:true}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTaxBasic, "{label:'All Line Level Tax Basic',xtype:'1',isNumeric:true}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_PAN_NO, "{label:'Customer/Vendor Pan Number',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, "{label:'Delivery Date',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, "{label:'Delivery Time',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDriver, "{label:'Delivery Driver',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, "{label:'Delivery Vehicle No.',xtype:'1'}");
        CustomDesignCreditNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "{label:'Exchange Rate (GST)', xtype:'2', defwidth:10}");
        
    } 
           public static HashMap<String, String> CustomDesignDebitNoteExtraFieldsMap = new HashMap<String, String>(); 
    static {

        //Vendor
         CustomDesignDebitNoteExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
         CustomDesignDebitNoteExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
         CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignDN_VendorInvoiceNo, "{label:'Vendor Invoice No',xtype:'1'}");
        
        //Customer
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term, "{label:'Customer Term',xtype:'1'}");

        //Common Fields for both Vendor,Customer & GL       
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.SrNo, "{label:'Sr No.',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceNo_fieldTypeId, "{label:'Invoice Number',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceAmount_fieldTypeId, "{label:'Invoice Amount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceTax_fieldTypeId, "{label:'Tax Amount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceAmountDue_fieldTypeId, "{label:'Invoice Amount Due',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId, "{label:'Enter Amount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_INvoiceDates_fieldTypeId, "{label:'Invoice Dates',xtype:'3'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.InvoiceSubTotal, "{label:'InvoiceSubTotal',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.InvoiceTotalTax, "{label:'InvoiceTotalTax',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.InvoiceTotalAmount, "{label:'InvoiceTotalAmount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.InvoiceTax, "{label:'Invoice Tax',xtype:'2'}");

        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountCode_fieldTypeId, "{label:'Account Code',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_Account_fieldTypeId, "{label:'Account Name ',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountAmount_fieldTypeId, "{label:'Account Amount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountTax_fieldTypeId, "{label:'Account Tax',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountTaxAmount_fieldTypeId, "{label:'Account Tax Amount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountTotalAmount_fieldTypeId, "{label:'Account Total Amount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountDescription_fieldTypeId, "{label:'Account Description',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountReason_fieldTypeId, "{label:'Account Reason',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CNDN_AccountAmountExcludeGST_fieldTypeId, "{label:'Account Amount Excluding GST',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AccountSubTotal, "{label:'AccSubTotal',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AccountTotalTax, "{label:'AccTotalTax',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AccountTotalAmount, "{label:'AccTotalAmount',xtype:'2'}");

        //Address
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Billing Address',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Billing City',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Billing State',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:' Billing Country',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Billing Postal Code',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Billing Phone No',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Billing Fax No',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Billing Email',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor/Customer Billing Contact Person',xtype:'1'}");
        
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Shipping Address',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Shipping City',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Shipping State',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Shipping Country',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Shipping Postal code',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Shipping Phone No',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Shipping Fax No',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Shipping Email',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Shipping Contact Person',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Vendor/Customer Shipping Contact Person',xtype:'1'}");
        
        //Other Fields
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Vendor/Customer Code',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, "{label:'Total Tax',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, "{label:'Debit Term',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.Customer_AccountNo_fieldTypeId, "{label:'Vendor/Customer Account No',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyUEN_fieldTypeId, "{label:'Company UEN No',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyGRN_No_fieldTypeId, "{label:'Company GRN No',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingMobileNo_fieldTypeId, "{label:'Vendor Billing Mobile No',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AllDimensions, "{label:'All Line Level Dimensions',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AllLinelevelCustomFields, "{label:'All Line Level Custom Fields',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.SummaryTaxPercent, "{label:'Account Tax Percent',xtype:'1'}");  //ERP-20872
       
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountSubTotal, "{label:'Base Currency AccSubTotal',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountTotalTax, "{label:'Base Currency AccTotalTax',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyAccountTotalAmount, "{label:'Base Currency AccTotalAmount',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyInvoiceSubTotal, "{label:'Base Currency Invoice SubTotal',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyInvoiceTotalTax, "{label:'Base Currency Invoice TotalTax',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyInvoiceTotal, "{label:'Base Currency Invoice Total',xtype:'2'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2'}");
        
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_OR_VENDOR_TITLE, "{label:'Customer/Vendor Title',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Customer/Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Customer/Vendor CST TIN NO',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CompanyPANNumber, "{label:'Company Pan Number',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTax, "{label:'All Line Level Tax',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTaxAmount, "{label:'All Line Level Tax Amount',xtype:'1',isNumeric:true}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTaxBasic, "{label:'All Line Level Tax Basic',xtype:'1',isNumeric:true}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_PAN_NO, "{label:'Customer/Vendor Pan Number',xtype:'1'}");
        CustomDesignDebitNoteExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "{label:'Exchange Rate (GST)', xtype:'2', defwidth:10}");

    }
/*Sales Return*/
        public static HashMap<String, String> CustomDesignSalesReturnExtraFieldsMap = new HashMap<String, String>(); 
    static {
        
        CustomDesignSalesReturnExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignSalesReturnExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.SR_LinkTo, "{label:'Linked Reference Number',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer Billing Contact Person',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId,"{label:'Customer Shipping Contact Person',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingRecipientName_fieldTypeId,"{label:'Customer Shipping Recipient Name',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingRecipientName_fieldTypeId,"{label:'Customer Billing Recipient Name',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term,"{label:'Customer Term',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode,"{label:'Customer Account Code',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCN_InvoiceNo,"{label:'Credit Note Number',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Customer Code',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "{label:'Currency Symbol',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.AppendRequestType, "{label:'Request Type',xtype:'1'}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, "{label:'Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");  
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.Posttext, "{label:'Post Text',xtype:'1'}");
        
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, "{label:'Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, "{label:'Exchanged Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, "{label:'Exchanged Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, "{label:'Exchanged Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, "{label:'Exchanged SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, "{label:'Exchanged Term Amount',xtype:'2',isFromUnitPriceAndAmount:true}");     
        
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorContactBillingDesignation_fieldTypeId, "{label:'Customer Billing Address Contact Person Designation',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorContactShippingDesignation_fieldTypeId, "{label:'Customer Shipping Address Contact Person Designation',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.LinkedSalesPerson, "{label:'Sales Person',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.LinkedSalesPersonDesignation, "{label:'Sales Person Designation',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.LinkedReferenceDate, "{label:'Linked Reference Date',xtype:'1'}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true}"); 
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, "{label:'Sub Total+Tax',xtype:'2',isFromUnitPriceAndAmount:true}");  //ERP-25162
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.SalesReturnNumber, "{label:'Sales Return Number For CN',xtype:'1'}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, "{label:'Total Discount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_TITLE, "{label:'Customer Title',xtype:'1'}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTax, "{label:'All Line Level Tax',xtype:'1'}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTaxAmount, "{label:'All Line Level Tax Amount',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
//        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTaxBasic, "{label:'All Line Level Tax Basic',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.Poreferencenumber, "{label:'PO Reference No',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.Total_Quantity_UOM,"{label:'Total Quantity With UOM',xtype:'1',isFromUnitPriceAndAmount:false,isNumeric:true}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.TAXNAME,"{label:'Tax Name',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.SummaryTaxPercent,"{label:'Tax Percent',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, "{label:'Credit Term',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "{label:'Delivery Order Reference Number',xtype:'1'}"); 
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignDORef_Date_fieldTypeId, "{label:'Delivery Order Reference Date',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, "{label:'Delivery Date',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, "{label:'Delivery Time',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerDriver, "{label:'Delivery Driver',xtype:'1'}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, "{label:'Delivery Vehicle No.',xtype:'1'}");
        
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, "{label:'Specific Currency Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:55}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, "{label:'Specific Currency Exchange Rate',xtype:'2',isFromUnitPriceAndAmount:false,defwidth: 10,seq:57}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, "{label:'Specific Currency SubTotal',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:58}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, "{label:'Specific Currency SubTotal-Discount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:59}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, "{label:'Specific Currency Tax Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:60}");
        CustomDesignSalesReturnExtraFieldsMap.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, "{label:'Specific Currency Term Amount',xtype:'2',isFromUnitPriceAndAmount:true,defwidth: 10,seq:61}");
    
            }
    
    /*Purchase Return*/
        public static HashMap<String, String> CustomDesignPurchaseReturnExtraFieldsMap = new HashMap<String, String>(); 
    static {
        
        CustomDesignPurchaseReturnExtraFieldsMap.putAll(CustomDesignCompanyAddressFieldsMap);
        CustomDesignPurchaseReturnExtraFieldsMap.putAll(CustomDesignMultiEntityFieldsMap);
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.SR_LinkTo, "{label:'Linked Reference Number',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Vendor Billing Email',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Vendor Shipping Email',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId,"{label:'Vendor Shipping Contact Person',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingRecipientName_fieldTypeId,"{label:'Vendor Shipping Recipient Name',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingRecipientName_fieldTypeId,"{label:'Vendor Billing Recipient Name',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term,"{label:'Vendor Term',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_AccountCode,"{label:'Vendor Account Code',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignDN_VendorInvoiceNo,"{label:'Debit Note Number',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Code,"{label:'Vendor Code',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, "{label:'Currency Symbol',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.BillTo, "{label:'Bill To',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.ShipTo, "{label:'Ship To',xtype:'1'}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, "{label:'Sub Total-Disc',xtype:'2',isFromUnitPriceAndAmount:true}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, "{label:'Total Tax',xtype:'2',isFromUnitPriceAndAmount:true}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, "{label:'Total Discount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.RemitPaymentTo, "{label:'Remit Payment To',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.Updatedby, "{label:'Last Updated By',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.Basecurrencyname, "{label:'Base Currency Name',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.Basecurrencycode, "{label:'Base Currency Code',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.Basecurrencysymbol, "{label:'Base Currency Symbol',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.LinkedSalesPerson, "{label:'Agent',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.PurchaseReturnNumber , "{label:'Purchase Return Number For DN',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.VENDOR_TITLE, "{label:'Vendor Title',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Vendor CST TIN NO',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTax, "{label:'All Line Level Tax',xtype:'1'}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTaxAmount, "{label:'All Line Level Tax Amount',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
//        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.AllLineLevelTaxBasic, "{label:'All Line Level Tax Basic',xtype:'1',isFromUnitPriceAndAmount:true,isNumeric:true}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_PAN_NO, "{label:'Vendor Pan Number',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CompanyPANNumber, "{label:'Company Pan Number',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId,"{label:'Purchase Order Reference Number',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CSUTOMDESIGNER_PO_REF_DATE,"{label:'Purchase Order Reference Date',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVenInvRefNumber_fieldTypeId,"{label:'Purchase Invoice Reference Number',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignVenInvRefDate_fieldTypeId,"{label:'Purchase Invoice Reference Date',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId,"{label:'Goods Receipt Reference Number',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.CustomDesignGRORefDate_fieldTypeId,"{label:'Goods Receipt Reference Date',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.TAXNAME,"{label:'Tax Name',xtype:'1'}");
        CustomDesignPurchaseReturnExtraFieldsMap.put(CustomDesignerConstants.SummaryTaxPercent,"{label:'Tax Percent',xtype:'2',isFromUnitPriceAndAmount:false}");
        }
    
    public static HashMap<String, String> CustomDesignStockRequestExtraFieldsMap = new HashMap<String, String>();

    static {

        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.FromStore, "{label:'From Store',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.ToStore, "{label:'To Store',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.OrderStatus, "{label:'Order Status',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'MoD',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.FromStoreTotalAddress, "{label:'From Store Total Address',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.ToStoreTotalAddress, "{label:'To Store Total Address',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.FromStoreAddress, "{label:'From Store Address',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.ToStoreAddress, "{label:'To Store Address',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.FromStoreContactNo, "{label:'From Store ContactNo',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.ToStoreContactNo, "{label:'To Store ContactNo',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.FromStoreFaxNo, "{label:'From Store FaxNo',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.ToStoreFaxNo, "{label:'To Store FaxNo',xtype:'1'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2'}");
        CustomDesignStockRequestExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");
 }
    
    public static HashMap<String, String> CustomDesignStockAdjustmentExtraFieldsMap = new HashMap<String, String>();

    static {

        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.FromStore, "{label:'From Store',xtype:'1'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.FromStoreCode, "{label:'From Store Code',xtype:'1'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.FromStoreDesc, "{label:'From Store Description',xtype:'1'}");
//        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.ToStore, "{label:'To Store',xtype:'1'}");
//        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.ToStoreCode, "{label:'To Store Code',xtype:'1'}");
//        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.ToStoreDesc, "{label:'To Store Description',xtype:'1'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.OrderStatus, "{label:'Order Status',xtype:'1'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'MoD',xtype:'1'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.TOTALIN, "{label:'Total IN',xtype:'2'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.TOTALOUT, "{label:'Total OUT',xtype:'2'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.TOTAL_IN_QTY, "{label:'Total IN Qty',xtype:'2'}");
        CustomDesignStockAdjustmentExtraFieldsMap.put(CustomDesignerConstants.TOTAL_OUT_QTY, "{label:'Total OUT Qty',xtype:'2'}");
    } 
    public static HashMap<String, String> CustomDesignInterStoreTransferExtraFieldsMap = new HashMap<String, String>();

    static {
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.FromStore, "{label:'From Store',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.FromStoreDesc, "{label:'From Store Description',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.FromStoreCode, "{label:'From Store Code',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.ToStore, "{label:'To Store',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.ToStoreCode, "{label:'To Store Code',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.ToStoreDesc, "{label:'To Store Description',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.OrderStatus, "{label:'Order Status',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'MoD',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.FromStoreAddress, "{label:'From Store Address',xtype:'1',defwidth: 10,seq:15}");
        CustomDesignInterStoreTransferExtraFieldsMap.put(CustomDesignerConstants.ToStoreAddress, "{label:'To Store Address',xtype:'1',defwidth: 10,seq:16}");
    }
    
    public static HashMap<String, String> CustomDesignInterLocationstockTransferExtraFieldsMap = new HashMap<String, String>();

    static {
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.FromStore, "{label:'From Store',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.FromStoreCode, "{label:'From Store Code',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.FromStoreDesc, "{label:'From Store Description',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.ToStore, "{label:'To Store',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.ToStoreCode, "{label:'To Store Code',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.ToStoreDesc, "{label:'To Store Description',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.FromLocName, "{label:'From Location',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.ToLocName, "{label:'To Location',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.OrderStatus, "{label:'Order Status',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'MoD',xtype:'1'}");
        CustomDesignInterLocationstockTransferExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignRequestForQuotationExtraFieldsMap = new HashMap<String, String>(); //Request For Quotation ExtraFields

    static {
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignBaseCurrency_fieldTypeId, "{label:'Base Currency',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorQuantity_Total, "{label:'Total Quantity',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Createdby FullName',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'pagenumber'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTemplate_Print, "{label:'Printed On',xtype:'3'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelDimensions, "{label:'All Global Level Dimensions',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.AllGloballevelCustomfields, "{label:'All Global Level Customfields',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.RPMP_CustomerVendorName, "{label:'Vendor Name',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendorCode, "{label:'Vendor Code',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendorBillingAddress, "{label:'Vendor Bill To',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendorShippingAddress, "{label:'Vendor Ship To',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrency, "{label:'Currency',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencyCode, "{label:'Currency Code',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCurrencySymbol, "{label:'Currency Symbol',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term,"{label:'Vendor Term',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomerVendor_Term_Days,"{label:'Vendor Term Days',xtype:'1'}");
        
        /*Vendor Billing and Shipping Address fields*/
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, "{label:'Company Post Text',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Vendor Billing Email',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Vendor Shipping Email',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId,"{label:'Vendor Shipping Contact Person',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingRecipientName_fieldTypeId,"{label:'Vendor Shipping Recipient Name',xtype:'1'}");
        CustomDesignRequestForQuotationExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingRecipientName_fieldTypeId,"{label:'Vendor Billing Recipient Name',xtype:'1'}");
    }
    //Map for Common field of India country
    public static HashMap<String, String> CustomDesignCommonExtraFieldsForIndia = new HashMap<String, String>();

    static {
        CustomDesignCommonExtraFieldsForIndia.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, "{label:'GSTIN Number',xtype:'1'}");
	CustomDesignCommonExtraFieldsForIndia.put(CustomDesignerConstants.CustomDesign_ENTITY_GSTIN_NUMBER, "{label:'Entity GSTIN Number',xtype:'1'}");
    }
    
    // Map for extra summary fields of Overcharged and Undercharged CN
    public static HashMap<String, String> CustomDesignExtraFieldsForOverChargedCN = new HashMap<String, String>();
    static {
        CustomDesignExtraFieldsForOverChargedCN.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, "{label:'Sub Total-Disc',xtype:'2'}");
        CustomDesignExtraFieldsForOverChargedCN.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, "{label:'Sub Total+Tax',xtype:'2'}"); 
        CustomDesignExtraFieldsForOverChargedCN.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, "{label:'Total Discount',xtype:'2'}");
    }
    
    //Map for Common field of Indonesia country
    public static HashMap<String, String> CustomDesignCommonExtraFieldsForIndonesia = new HashMap<String, String>();

    static {
        CustomDesignCommonExtraFieldsForIndonesia.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, "{label:'Amount in words(Bahasa Indonesia)',xtype:'1',isFromUnitPriceAndAmount:true}");
    }
    public static HashMap<String, String> CustomDesignExtraFieldsForVendorIndia = new HashMap<String, String>();

    static {
	CustomDesignExtraFieldsForVendorIndia.putAll(CustomDesignCommonExtraFieldsForIndia);
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Vendor CST TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_ECC_NO,"{label:'Vendor Excise Control Code(ECC No)',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CUSTOMER_PAN_NO, "{label:'Vendor PAN Number',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_PAN_STATAUS, "{label:'Vendor PAN Status',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_DEDUCTEE_TYPE, "{label:'Vendor Deductee Type',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_DEDUCTEE_CODE, "{label:'Vendor Deductee Code',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_DEFAULT_NATURE_OF_PAYMENT, "{label:'Vendor Default Nature Of Payment',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_RESIDENTIAL_STATUS, "{label:'Vendor Residential Status',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_CST_REG_DATE, "{label:'Vendor CST Reg. Date',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_VAT_REG_DATE, "{label:'Vendor VAT Reg. Date',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_VAT_DEALER_TYPE, "{label:'Vendor VAT Dealer Type',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_IMPORTER_ECC_NO, "{label:'Vendor Importer ECC No',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_IEC_NO, "{label:'Vendor IEC Number',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_RANGE_CODE, "{label:'Vendor Excise Range Code',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_DIVISION_CODE, "{label:'Vendor Excise Division Code',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_COMMISSIONERATE_CODE, "{label:'Vendor Excise Commissionerate Code',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_TDS_PAYABLE_ACCOUNT, "{label:'Vendor TDS Payable Account',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_TDS_HIGHER_RATE, "{label:'Vendor TDS Higher Rate',xtype:'2'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_INTERSTATEPARTY, "{label:'Vendor Inter State Party',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_C_FORM_APPLICABLE, "{label:'Vendor C Form Applicable',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_TYPE_OF_MANUFATURER, "{label:'Vendor Type of Manufaturer',xtype:'1'}");
	CustomDesignExtraFieldsForVendorIndia.put(CustomDesignerConstants.CustomerVendor_TYPE_OF_SALES, "{label:'Vendor Type of Sales',xtype:'1'}");
	
    }
    public static HashMap<String, String> CustomDesignExtraFieldsForCustomerIndia = new HashMap<String, String>();

    static {
	CustomDesignExtraFieldsForCustomerIndia.putAll(CustomDesignCommonExtraFieldsForIndia);
	CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CUSTOMER_PAN_NO, "{label:'Customer PAN Number',xtype:'1'}");
	CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_PAN_STATAUS, "{label:'Customer PAN Status',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Customer VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Customer CST TIN NO',xtype:'1'}");
	CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_CST_REG_DATE, "{label:'Customer CST Reg. Date',xtype:'1'}");
	CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_VAT_REG_DATE, "{label:'Customer VAT Reg. Date',xtype:'1'}");
	CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_VAT_DEALER_TYPE, "{label:'Customer VAT Dealer Type',xtype:'1'}");
	CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_INTERSTATEPARTY, "{label:'Customer Inter State Party',xtype:'1'}");
	CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_C_FORM_APPLICABLE, "{label:'Customer C Form Applicable',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CUSTOMER_EXCISE_TYPE_OF_SALES, "{label:'Customer Excise Type Of Sales',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CustomerVendor_ECC_NO,"{label:'Customer Excise Control Code(ECC No)',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CUSTOMER_EXCISE_IMPORTER_ECC_NUMBER, "{label:'Customer Excise Importer ECC No.',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CUSTOMER_EXCISE_IEC_NUMBER, "{label:'Customer Excise IEC No.',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CUSTOMER_EXCISE_RANGE_CODE, "{label:'Customer Excise Range Code',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CUSTOMER_EXCISE_DIVISION_CODE, "{label:'Customer Excise Division Code',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CUSTOMER_EXCISE_COMMISSIONERATE_CODE, "{label:'Customer Excise Commissionerate Code',xtype:'1'}");
	CustomDesignExtraFieldsForCustomerIndia.put(CustomDesignerConstants.CUSTOMER_SERVICE_TAX_REG_NO, "{label:'Customer Service Tax Reg No',xtype:'1'}");
	
    }
    public static HashMap<String, String> CustomDesignExtraFieldsForInvoiceIndia = new HashMap<String, String>();

    static {
        CustomDesignExtraFieldsForInvoiceIndia.putAll(CustomDesignExtraFieldsForCustomerIndia);
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.CONSIGNEE_NAME, "{label:'Consignee Name',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.CONSIGNEE_ADDRESS, "{label:'Consignee Address',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.CONSIGNEE_EXCISE_REGN_NO, "{label:'Consignee Excise Registration Number',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.BUYER_EXCISE_REGN_NO, "{label:'Buyer Excise Registration Number',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.CONSIGNEE_RANGE_CODE, "{label:'Consignee Range Code',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.CONSIGNEE_DIVISION_CODE, "{label:'Consignee Division Code',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.CONSIGNEE_COMMISSIONERATE_CODE, "{label:'Consignee Commissionerate Code',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.CONSIGNEE_COMMISSIONERATE_CODE, "{label:'Consignee Commissionerate Code',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.VAT_AMOUNT_IN_WORDS, "{label:'VAT Amount in Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.INVOICE_DATE_WITH_TIME, "{label:'Invoice Date with Time',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.EXCISE_IN_WORDS, "{label:'Excise In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.EDUCATION_CESS_IN_WORDS, "{label:'Education Cess In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.H_CESS_IN_WORDS, "{label:'H Cess In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.RANGE_CODE_COMPANY, "{label:'Company Excise Range Code',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.DIVISION_CODE_COMPANY, "{label:'Company Excise Division Code',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.COMMISSIONERATE_CODE_COMPANY, "{label:'Company Excise Commessionerate Code',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.Company_ECC_NO,"{label:'Company Excise Control Code(ECC No)',xtype:'1'}");
	CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.CompanyPANNumber, "{label:'Company Pan Number',xtype:'1'}");
        CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.serviceTaxNumber, "{label:'Service Tax No',xtype:'1'}");
	CustomDesignExtraFieldsForInvoiceIndia.put(CustomDesignerConstants.COMPANY_SERVICE_TAX_REG_NO, "{label:'Company Service Tax Reg No',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignExtraFieldsForSalesOrderIndia = new HashMap<String, String>();

    static {
        CustomDesignExtraFieldsForSalesOrderIndia.putAll(CustomDesignExtraFieldsForCustomerIndia);
        CustomDesignExtraFieldsForSalesOrderIndia.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForSalesOrderIndia.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignExtraFieldsForSalesReturnIndia = new HashMap<String, String>();

    static {
        CustomDesignExtraFieldsForSalesReturnIndia.putAll(CustomDesignExtraFieldsForCustomerIndia);
        CustomDesignExtraFieldsForSalesReturnIndia.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForSalesReturnIndia.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForSalesReturnIndia.put(CustomDesignerConstants.CompanyPANNumber, "{label:'Company Pan Number',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignExtraFieldsForDeliveryOrderIndia = new HashMap<String, String>();

    static {
        CustomDesignExtraFieldsForDeliveryOrderIndia.putAll(CustomDesignExtraFieldsForCustomerIndia);
        CustomDesignExtraFieldsForDeliveryOrderIndia.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForDeliveryOrderIndia.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForDeliveryOrderIndia.put(CustomDesignerConstants.Company_ECC_NO,"{label:'Company Excise Control Code(ECC No)',xtype:'1'}");
        CustomDesignExtraFieldsForDeliveryOrderIndia.put(CustomDesignerConstants.CompanyBankIFSCCode, "{label:'Company Bank IFSC Code',xtype:'1'}");
        CustomDesignExtraFieldsForDeliveryOrderIndia.put(CustomDesignerConstants.CompanyBankAccountNumber, "{label:'Company Bank Account Number',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignExtraFieldsForCustomerQuotationIndia = new HashMap<String, String>();

    static {
        CustomDesignExtraFieldsForCustomerQuotationIndia.putAll(CustomDesignExtraFieldsForCustomerIndia);
        CustomDesignExtraFieldsForCustomerQuotationIndia.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForCustomerQuotationIndia.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignExtraFieldsForPurchaseOrderIndia = new HashMap<String, String>();

    static {
        CustomDesignExtraFieldsForPurchaseOrderIndia.putAll(CustomDesignExtraFieldsForVendorIndia);
        CustomDesignExtraFieldsForPurchaseOrderIndia.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForPurchaseOrderIndia.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
    }
    public static HashMap<String, String> CustomDesignExtraFieldsForMakeAndReceivePaymentIndia = new HashMap<String, String>();

    static {
        //GST fields
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.CGSTAMOUNT, "{label:'CGST Amount',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.CGSTPERCENT, "{label:'CGST Percent',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.SGSTAMOUNT, "{label:'SGST Amount',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.SGSTPERCENT, "{label:'SGST Percent',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.IGSTAMOUNT, "{label:'IGST Amount',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.IGSTPERCENT, "{label:'IGST Percent',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.UTGSTAMOUNT, "{label:'UTGST Amount',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.UTGSTPERCENT, "{label:'UTGST Percent',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.CESSAMOUNT, "{label:'CESS Amount',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.CESSPERCENT, "{label:'CESS Percent',xtype:'2'}");
        CustomDesignExtraFieldsForMakeAndReceivePaymentIndia.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignExtraFieldsForCN_DN_India = new HashMap<String, String>();

    static {
        //GST fields
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.CGSTAMOUNT, "{label:'CGST Amount',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.CGSTPERCENT, "{label:'CGST Percent',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.SGSTAMOUNT, "{label:'SGST Amount',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.SGSTPERCENT, "{label:'SGST Percent',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.IGSTAMOUNT, "{label:'IGST Amount',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.IGSTPERCENT, "{label:'IGST Percent',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.UTGSTAMOUNT, "{label:'UTGST Amount',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.UTGSTPERCENT, "{label:'UTGST Percent',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.CESSAMOUNT, "{label:'CESS Amount',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.CESSPERCENT, "{label:'CESS Percent',xtype:'2'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.LineLevelTax, "{label:'Line Level Tax',xtype:'1'}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.LineLevelTaxAmount, "{label:'Line Level Tax Amount',xtype:'1',isNumeric:true}");
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.LineLevelTaxPercent, "{label:'Line Level Tax Percent',xtype:'1',isNumeric:true}");      
        CustomDesignExtraFieldsForCN_DN_India.put(CustomDesignerConstants.HSN_SAC_CODE, "{label:'HSN/SAC Code',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignExtraFieldsForVendorInvoiceIndia = new HashMap<String, String>();

    static {
        CustomDesignExtraFieldsForVendorInvoiceIndia.putAll(CustomDesignExtraFieldsForVendorIndia);
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.TDS_AMOUNT, "{label:'TDS Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
	CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.OTHER_CHARGES, "{label:'Other Charges',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.EXCISE_IN_WORDS, "{label:'Excise In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.EDUCATION_CESS_IN_WORDS, "{label:'Education Cess In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.H_CESS_IN_WORDS, "{label:'H Cess In Words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.RANGE_CODE_COMPANY, "{label:'Company Excise Range Code',xtype:'1'}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.DIVISION_CODE_COMPANY, "{label:'Company Excise Division Code',xtype:'1'}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.COMMISSIONERATE_CODE_COMPANY, "{label:'Company Excise Commessionerate Code',xtype:'1'}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,"{label:'Vendor VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO,"{label:'Vendor CST TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.Company_VAT_TIN_NO,"{label:'Company VAT TIN NO',xtype:'1'}");
        CustomDesignExtraFieldsForVendorInvoiceIndia.put(CustomDesignerConstants.Company_CST_TIN_NO,"{label:'Company CST TIN NO',xtype:'1'}");
    }
    public static HashMap<String, String> CustomDesignExtraFieldsForRequestForQuotationIndia = new HashMap<String, String>();

    static {
        CustomDesignExtraFieldsForRequestForQuotationIndia.putAll(CustomDesignCommonExtraFieldsForIndia);
        CustomDesignExtraFieldsForRequestForQuotationIndia.put(CustomDesignerConstants.CompanyPANNumber, "{label:'Company Pan Number',xtype:'1'}");
    }
    
    public static final String CustomDesignSOACompanyUEN = "Company UEN";
    public static final String CustomDesignSOACompanyGSTNumber = "GST Number";
    public static final String CustomDesignSOACompanyName = "Company Name";
    public static final String CustomDesignSOACompanyBillingAddress = "Company Billing Address";
    public static final String CustomDesignSOACompanyBillingAddressCity = "Company Billing Address City";
    public static final String CustomDesignSOACompanyBillingAddressState = "Company Billing Address State";
    public static final String CustomDesignSOACompanyBillingAddressCountry = "Company Billing Address Country";
    public static final String CustomDesignSOACompanyBillingAddressPostalCode = "Company Billing Address Postal Code";
    public static final String CustomDesignSOACompanyBillingAddressPhone = "Company Billing Address Phone No";
    public static final String CustomDesignSOACompanyBillingAddressMobile = "Company Billing Address Mobile No";
    public static final String CustomDesignSOACompanyBillingAddressFax = "Company Billing Address Fax";
    public static final String CustomDesignSOACompanyBillingAddressEmail = "Company Billing Address Email";
    public static final String CustomDesignSOACompanyBillingAddressContactPerson = "Company Billing Address Contact Person";
    public static final String CustomDesignSOACompanyBillingAddressContactPersonNumber = "Company Billing Address Contact Person No";
    public static final String CustomDesignSOACompanyShippingAddress = "Company Shipping Address";
    public static final String CustomDesignSOACompanyShippingAddressCity = "Company Shipping Address City";
    public static final String CustomDesignSOACompanyShippingAddressState = "Company Shipping Address State";
    public static final String CustomDesignSOACompanyShippingAddressCountry = "Company Shipping Address Country";
    public static final String CustomDesignSOACompanyShippingAddressPostalCode = "Company Shipping Address Postal Code";
    public static final String CustomDesignSOACompanyShippingAddressPhone = "Company Shipping Address Phone No";
    public static final String CustomDesignSOACompanyShippingAddressMobile = "Company Shipping Address Mobile No";
    public static final String CustomDesignSOACompanyShippingAddressFax = "Company Shipping Address Fax";
    public static final String CustomDesignSOACompanyShippingAddressEmail = "Company Shipping Address Email";
    public static final String CustomDesignSOACompanyShippingAddressContactPerson = "Company Shipping Address Contact Person";
    public static final String CustomDesignSOACompanyShippingAddressContactPersonNumber = "Company Shipping Address Contact Person No";
    public static final String CustomDesignSOACompanyRegNo = "Company Reg No";
    public static final String CustomDesignSOA_NET_Credit_Term = "NET Credit Term";
    public static final String CustomDesignSOA_NET_Debit_Term = "NET Debit Term";
    
    
    
    
    public static final String CustomDesignSOACustomerCode = "Customer Code";
    public static final String CustomDesignSOABillTO = "Bill To";
    public static final String CustomDesignSOAShipTo = "Ship To";
    public static final String CustomDesignSOACustomerAlias = "Alias Name";
    public static final String CustomDesignSOACustomerName1 = "Customer Name";
    public static final String CustomDesignSOACustomerGSTIN = "GSTIN Number";
    public static final String CustomDesignSOACustomerBillingAddress = "Billing Address";
    public static final String CustomDesignSOACustomerBillingAddressCity = "Billing Address City";
    public static final String CustomDesignSOACustomerBillingAddressState = "Billing Address State";
    public static final String CustomDesignSOACustomerBillingAddressCountry = "Billing Address Country";
    public static final String CustomDesignSOACustomerBillingAddressPostalCode = "Billing Address PostalCode";
    public static final String CustomDesignSOACustomerBillingAddressPhoneNumber = "Billing Address Phone";
    public static final String CustomDesignSOACustomerBillingAddressMobileNumber = "Billing Address Mobile";
    public static final String CustomDesignSOACustomerBillingAddressFax = "Billing Address Fax";
    public static final String CustomDesignSOACustomerBillingAddressEmail = "Billing Address Email";
    public static final String CustomDesignSOACustomerBillingAddressContactPerson = "Billing Address Contact Person";
    public static final String CustomDesignSOACustomerBillingAddressContactPersonNumber = "Billing Address Contact Person Number";
    
    public static final String CustomDesignSOACustomerShippingAddress = "Shipping Address";
    public static final String CustomDesignSOACustomerShippingAddressCity = "Shipping Address City";
    public static final String CustomDesignSOACustomerShippingAddressState = "Shipping Address State";
    public static final String CustomDesignSOACustomerShippingAddressCountry = "Shipping Address Country";
    public static final String CustomDesignSOACustomerShippingAddressPostalCode = "Shipping Address PostalCode";
    public static final String CustomDesignSOACustomerShippingAddressPhoneNumber = "Shipping Address Phone";
    public static final String CustomDesignSOACustomerShippingAddressMobileNumber = "Shipping Address Mobile";
    public static final String CustomDesignSOACustomerShippingAddressFax = "Shipping Address Fax";
    public static final String CustomDesignSOACustomerShippingAddressEmail = "Shipping Address Email";
    public static final String CustomDesignSOACustomerShippingAddressContactPerson = "Shipping Address Contact Person";
    public static final String CustomDesignSOACustomerShippingAddressContactPersonNumber = "Shipping Address Contact Person Number";
    
    public static final String CustomDesignSOACustomerCurrency = "Currency";
    public static final String CustomDesignSOACustomerCurrencyCode = "Currency Code";
    public static final String CustomDesignSOACustomerCurrencySymbol = "Currency Symbol";
    
    public static final String CustomDesignSOACustomerUEN = "Customer UEN";
    public static final String CustomDesignSOACustomerOpeningBalance = "Customer Opening Balance";
    public static final String CustomDesignSOACustomerGSTNumber = "GST Number";
    public static final String CustomDesignSOACustomerCreditTerm = "Credit Term";
    public static final String CustomDesignSOACustomerCreditSalesLimit = "Credit Sales Limit";
    public static final String CustomDesignSOASalesPersonName = "Sales Person Name";
    public static final String CustomDesignSOASalesPersonCode = "Sales Person Code";
    public static final String CustomDesignSOASalesPersonEmail = "Sales Person Email";
    public static final String CustomDesignSOASalesPersonDesignation = "Sales Person Designation";
    
    public static HashMap<String, String> CustomDesignSOACustomerExtraFieldsMap = new HashMap<String, String>(); 
    
    static {
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerCode, "{label:'Customer Code',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerName1, "{label:'Customer Name',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerAlias, "{label:'Alias Name',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABillTO, "{label:'Bill To',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerGSTIN, "{label:'GSTIN Number',xtype:'1'}");
        
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddress, "{label:'Billing Address',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressCity, "{label:'Billing Address City',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressState, "{label:'Billing Address State',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressCountry, "{label:'Billing Address Country',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressPostalCode, "{label:'Billing Address PostalCode',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressPhoneNumber, "{label:'Billing Address Phone',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressMobileNumber, "{label:'Billing Address Mobile',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressFax, "{label:'Billing Address Fax',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressEmail, "{label:'Billing Address Email',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressContactPerson, "{label:'Billing Address Contact Person',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerBillingAddressContactPersonNumber, "{label:'Billing Address Contact Person Number',xtype:'1'}");
        
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddress, "{label:'Shipping Address',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressCity, "{label:'Shipping Address City',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressState, "{label:'Shipping Address State',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressCountry, "{label:'Shipping Address Country',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressPostalCode, "{label:'Shipping Address PostalCode',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressPhoneNumber, "{label:'Shipping Address Phone',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressMobileNumber, "{label:'Shipping Address Mobile',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressFax, "{label:'Shipping Address Fax',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressEmail, "{label:'Shipping Address Email',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressContactPerson, "{label:'Shipping Address Contact Person',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerShippingAddressContactPersonNumber, "{label:'Shipping Address Contact Person Number',xtype:'1'}");
        
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerUEN, "{label:'Customer UEN',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerOpeningBalance, "{label:'Customer Opening Balance',xtype:'2'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyGSTNumber, "{label:'GST Number',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerCreditTerm, "{label:'Credit Term',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOA_NET_Credit_Term, "{label:'NET Credit Term',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACustomerCreditSalesLimit, "{label:'Credit Sales Limit',xtype:'2'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CurrentDateField, "{label:'Current Date',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.EndDateField, "{label:'End Date',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.StartDateField, "{label:'Start Date',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.AsofDateField, "{label:'As of Date',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CurrentMonthField, "{label:'Current Month',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CurrentYearField, "{label:'Current Year',xtype:'1'}");
        
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyName, "{label:'Company Name',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddress, "{label:'Company Billing Address',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressCity, "{label:'Company Billing Address City',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressState, "{label:'Company Billing Address State',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressCountry, "{label:'Company Billing Address Country',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressPostalCode, "{label:'Company Billing Address Postal Code',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressPhone, "{label:'Company Billing Address Phone No',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressMobile, "{label:'Company Billing Address Mobile No',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressFax, "{label:'Company Billing Address Fax',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressEmail, "{label:'Company Billing Address Email',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressContactPerson, "{label:'Company Billing Address Contact Person',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressContactPersonNumber, "{label:'Company Billing Address Contact Person No',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddress, "{label:'Company Shipping Address',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressCity, "{label:'Company Shipping Address City',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressState, "{label:'Company Shipping Address State',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressCountry, "{label:'Company Shipping Address Country',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressPostalCode, "{label:'Company Shipping Address Postal Code',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressPhone, "{label:'Company Shipping Address Phone No',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressMobile, "{label:'Company Shipping Address Mobile No',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressFax, "{label:'Company Shipping Address Fax',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressEmail, "{label:'Company Shipping Address Email',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressContactPerson, "{label:'Company Shipping Address Contact Person',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressContactPersonNumber, "{label:'Company Shipping Address Contact Person No',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyRegNo, "{label:'Company Reg No',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOASalesPersonName, "{label:'Sales Person Name',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAccruedBalance, "{label:'Accrued Balance',xtype:'2'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAccruedBalanceInBase, "{label:'Accrued Balance In Base',xtype:'2'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATotalCreditAmountInBase, "{label:'Total Credit Amount In Base Currency',xtype:'2'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATotalDebitAmountInBase, "{label:'Total Debit Amount In Base Currency',xtype:'2'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount In Words',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOASalesPersonCode, "{label:'Sales Person Code',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOASalesPersonEmail, "{label:'Sales Person Email',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOASalesPersonDesignation, "{label:'Sales Person Designation',xtype:'1'}");
        CustomDesignSOACustomerExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATotalInvoiceOriginalAmount, "{label:'Total Invoice Amount',xtype:'2'}");
        
    }
    public static final String CustomDesignSOAVendorCode = "Vendor Code";
    public static final String CustomDesignSOAVendorAlias = "Alias Name";
    public static final String CustomDesignSOAVendorName1 = "Vendor Name";
    public static final String CustomDesignSOAVendorGSTIN = "GSTIN Number";
    
    public static final String CustomDesignSOAVendorBillingAddress = "Billing Address";
    public static final String CustomDesignSOAVendorBillingAddressCity = "Billing Address City";
    public static final String CustomDesignSOAVendorBillingAddressState = "Billing Address State";
    public static final String CustomDesignSOAVendorBillingAddressCountry = "Billing Address Country";
    public static final String CustomDesignSOAVendorBillingAddressPostalCode = "Billing Address PostalCode";
    public static final String CustomDesignSOAVendorBillingAddressPhoneNumber = "Billing Address Phone";
    public static final String CustomDesignSOAVendorBillingAddressMobileNumber = "Billing Address Mobile";
    public static final String CustomDesignSOAVendorBillingAddressFax = "Billing Address Fax";
    public static final String CustomDesignSOAVendorBillingAddressEmail = "Billing Address Email";
    public static final String CustomDesignSOAVendorBillingAddressContactPerson = "Billing Address Contact Person";
    public static final String CustomDesignSOAVendorBillingAddressContactPersonNumber = "Billing Address Contact Person Number";
    
    public static final String CustomDesignSOAVendorShippingAddress = "Shipping Address";
    public static final String CustomDesignSOAVendorShippingAddressCity = "Shipping Address City";
    public static final String CustomDesignSOAVendorShippingAddressState = "Shipping Address State";
    public static final String CustomDesignSOAVendorShippingAddressCountry = "Shipping Address Country";
    public static final String CustomDesignSOAVendorShippingAddressPostalCode = "Shipping Address PostalCode";
    public static final String CustomDesignSOAVendorShippingAddressPhoneNumber = "Shipping Address Phone";
    public static final String CustomDesignSOAVendorShippingAddressMobileNumber = "Shipping Address Mobile";
    public static final String CustomDesignSOAVendorShippingAddressFax = "Shipping Address Fax";
    public static final String CustomDesignSOAVendorShippingAddressEmail = "Shipping Address Email";
    public static final String CustomDesignSOAVendorShippingAddressContactPerson = "Shipping Address Contact Person";
    public static final String CustomDesignSOAVendorShippingAddressContactPersonNumber = "Shipping Address Contact Person Number";
    
    public static final String CustomDesignSOAVendorCurrency = "Currency";
    public static final String CustomDesignSOAVendorCurrencyCode = "Currency Code";
    public static final String CustomDesignSOAVendorCurrencySymbol = "Currency Symbol";
    
    public static final String CustomDesignSOAVendorUEN = "Vendor UEN";
    public static final String CustomDesignSOAVendorGSTNumber = "GST Number";
    public static final String CustomDesignSOAVendorDebitTerm = "Debit Term";
    public static final String CustomDesignSOAVendorDebitSalesLimit = "Debit Sales Limit";
    public static final String CustomDesignSOAAgentName = "Agent Name";
    public static final String CustomDesignSOAAgentCode = "Agent Code";
    public static final String CustomDesignSOAAgentEmail = "Agent Email";
    public static final String CustomDesignSOAAgentDesignation = "Agent Designation";
    
    public static HashMap<String, String> CustomDesignSOAVendorExtraFieldsMap = new HashMap<String, String>(); 
    
    static {
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorCode, "{label:'Vendor Code',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorName1, "{label:'Vendor Name',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorAlias, "{label:'Alias Name',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOABillTO, "{label:'Bill To',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAShipTo, "{label:'Ship To',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorGSTIN, "{label:'GSTIN Number',xtype:'1'}");
        
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddress, "{label:'Billing Address',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressCity, "{label:'Billing Address City',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressState, "{label:'Billing Address State',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressCountry, "{label:'Billing Address Country',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressPostalCode, "{label:'Billing Address PostalCode',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressPhoneNumber, "{label:'Billing Address Phone',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressMobileNumber, "{label:'Billing Address Mobile',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressFax, "{label:'Billing Address Fax',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressEmail, "{label:'Billing Address Email',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressContactPerson, "{label:'Billing Address Contact Person',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorBillingAddressContactPersonNumber, "{label:'Billing Address Contact Person Number',xtype:'1'}");
        
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddress, "{label:'Shipping Address',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressCity, "{label:'Shipping Address City',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressState, "{label:'Shipping Address State',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressCountry, "{label:'Shipping Address Country',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressPostalCode, "{label:'Shipping Address PostalCode',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressPhoneNumber, "{label:'Shipping Address Phone',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressMobileNumber, "{label:'Shipping Address Mobile',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressFax, "{label:'Shipping Address Fax',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressEmail, "{label:'Shipping Address Email',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressContactPerson, "{label:'Shipping Address Contact Person',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorShippingAddressContactPersonNumber, "{label:'Shipping Address Contact Person Number',xtype:'1'}");
        
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorUEN, "{label:'Vendor UEN',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyGSTNumber, "{label:'GST Number',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorDebitTerm, "{label:'Debit Term',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOA_NET_Debit_Term, "{label:'NET Debit Term',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAVendorDebitSalesLimit, "{label:'Debit Sales Limit',xtype:'2'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CurrentDateField, "{label:'Current Date',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.EndDateField, "{label:'End Date',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.StartDateField, "{label:'Start Date',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.AsofDateField, "{label:'As of Date',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CurrentMonthField, "{label:'Current Month',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CurrentYearField, "{label:'Current Year',xtype:'1'}");
        
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyName, "{label:'Company Name',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddress, "{label:'Company Billing Address',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressCity, "{label:'Company Billing Address City',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressState, "{label:'Company Billing Address State',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressCountry, "{label:'Company Billing Address Country',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressPostalCode, "{label:'Company Billing Address Postal Code',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressPhone, "{label:'Company Billing Address Phone No',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressMobile, "{label:'Company Billing Address Mobile No',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressFax, "{label:'Company Billing Address Fax',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressEmail, "{label:'Company Billing Address Email',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressContactPerson, "{label:'Company Billing Address Contact Person',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyBillingAddressContactPersonNumber, "{label:'Company Billing Address Contact Person No',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddress, "{label:'Company Shipping Address',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressCity, "{label:'Company Shipping Address City',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressState, "{label:'Company Shipping Address State',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressCountry, "{label:'Company Shipping Address Country',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressPostalCode, "{label:'Company Shipping Address Postal Code',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressPhone, "{label:'Company Shipping Address Phone No',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressMobile, "{label:'Company Shipping Address Mobile No',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressFax, "{label:'Company Shipping Address Fax',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressEmail, "{label:'Company Shipping Address Email',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressContactPerson, "{label:'Company Shipping Address Contact Person',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyShippingAddressContactPersonNumber, "{label:'Company Shipping Address Contact Person No',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACompanyRegNo, "{label:'Company Reg No',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAgentName,"{label:'Agent Name',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAccruedBalance,"{label:'Accrued Balance',xtype:'2'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAccruedBalanceInBase,"{label:'Accrued Balance In Base',xtype:'2'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATotalCreditAmountInBase,"{label:'Total Credit Amount In Base Currency',xtype:'2'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATotalDebitAmountInBase,"{label:'Total Debit Amount In Base Currency',xtype:'2'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId,"{label:'Amount In Words',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAgentCode, "{label:'Agent Code',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAgentEmail, "{label:'Agent Email',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOAAgentDesignation, "{label:'Agent Designation',xtype:'1'}");
        CustomDesignSOAVendorExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOATotalInvoiceOriginalAmount, "{label:'Total Invoice Amount',xtype:'2'}");
        
    }
    
    public static HashMap<String, String> CustomDesignSOACurrencyExtraFieldsMap = new HashMap<String, String>(); 
    static{
        CustomDesignSOACurrencyExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACurrency, "{label:'Currency',xtype:'1'}");
        CustomDesignSOACurrencyExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACurrencyCode, "{label:'Currency Code',xtype:'1'}");
        CustomDesignSOACurrencyExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSOACurrencySymbol, "{label:'Currency Symbol',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignPurchaseRequisitionExtraFieldsMap = new HashMap<String, String>(); //Request For Quotation ExtraFields

    static {
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, "{label:'Total Amount',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2',isFromUnitPriceAndAmount:false}");
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, "{label:'Sub Total',xtype:'2',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, "{label:'Amount in words',xtype:'1',isFromUnitPriceAndAmount:true}");
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.PurchaseRequisitionNumber, "{label:'Purchase Requisition Number',xtype:'1'}");
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.PurchaseRequisitionDate, "{label:'Purchase Requisition Date',xtype:'1'}");
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompAccPrefBillAddress_fieldTypeId, "{label:'Company Billing Address',xtype:'1'}");
        CustomDesignPurchaseRequisitionExtraFieldsMap.put(CustomDesignerConstants.CustomDesignCompAccPrefShipAddress_fieldTypeId, "{label:'Company Shipping Address',xtype:'1'}"); 

    }
    
      public static TreeMap<String, String> CustomDesignCustomerAgeingMap = new TreeMap<String, String>();
    static{
        CustomDesignCustomerAgeingMap.put(CustomDesignerConstants.StatementOfAcccurrency, "{label:'Currency',xtype:'1',seq:0}");
        CustomDesignCustomerAgeingMap.put(CustomDesignerConstants.StatementOfAccTotal, "{label:'Total',xtype:'2',seq:1}");
        CustomDesignCustomerAgeingMap.put(CustomDesignerConstants.StatementOfAccInterval, "{label:'Interval',xtype:'2',seq:2}");
        CustomDesignCustomerAgeingMap.put(CustomDesignerConstants.StatementOfAccAccruedBalance, "{label:'Accrued Balance',xtype:'2',seq:3}");
    }
    
     public static TreeMap<String, String> CustomDesignVendorAgeingMap = new TreeMap<String, String>();
    static{
        CustomDesignVendorAgeingMap.put(CustomDesignerConstants.StatementOfAcccurrency, "{label:'Currency',xtype:'1',seq:0}");
        CustomDesignVendorAgeingMap.put(CustomDesignerConstants.StatementOfAccTotal, "{label:'Total',xtype:'2',seq:1}");
        CustomDesignVendorAgeingMap.put(CustomDesignerConstants.StatementOfAccInterval, "{label:'Interval',xtype:'2',seq:2}");
        CustomDesignVendorAgeingMap.put(CustomDesignerConstants.StatementOfAccAccruedBalance, "{label:'Accrued Balance',xtype:'2',seq:3}");
    }
    
    public static HashMap<String, String> CurrentUserDetailsMap = new HashMap<String, String>();

    static {
        CurrentUserDetailsMap.put(CustomDesignerConstants.CurrentUserFirstName, "{label:'Current User FirstName',xtype:'1'}");
        CurrentUserDetailsMap.put(CustomDesignerConstants.CurrentUserLastName,"{label:'Current User LastName',xtype:'1'}");
        CurrentUserDetailsMap.put(CustomDesignerConstants.CurrentUserEmail,"{label:'Current User Email',xtype:'1'}");
        CurrentUserDetailsMap.put(CustomDesignerConstants.CurrentUserFullName, "{label:'Current User FullName',xtype:'1'}");
        CurrentUserDetailsMap.put(CustomDesignerConstants.CurrentUserAddress,"{label:'Current User Address',xtype:'1'}");
        CurrentUserDetailsMap.put(CustomDesignerConstants.CurrentUserContactNumber, "{label:'Current User Contact Number',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesignConsignmentRequestExtraFieldsMap = new HashMap<String, String>(); 
    static {
        CustomDesignConsignmentRequestExtraFieldsMap.put(CustomDesignerConstants.CustomDesignConsignmentRequestType, "{label:'Request Type',xtype:'1'}");
    }
    
    public static final String documentDesignerprintTemplateUrl = "transaction/printtemplate?request=";
    
    // SO Job Order Flow Map details
    public static final String SALES_ORDER_NO = "SalesOrderNo";
    public static final String SALES_INVOICE_NO = "SalesInvoiceNo";
    public static final String SHIP_DATE = "ShipDate";
    public static final String CUSTOMER_NAME = "CustomerName";
    
    public static HashMap<String, String> CustomDesignCommon_JobOrderFlowExtraFieldsMap = new HashMap<String, String>();
    
    static{
        // Global fields
        CustomDesignCommon_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Created By',xtype:'1'}");
        CustomDesignCommon_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.SHIP_DATE, "{label:'Ship Date',xtype:'3'}");
        CustomDesignCommon_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.CUSTOMER_NAME, "{label:'Customer Name',xtype:'1'}");
        // Product level fields
        CustomDesignCommon_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1'}");
        CustomDesignCommon_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1'}");
        CustomDesignCommon_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.ProductDescription, "{label:'Product Description',xtype:'1'}");
        CustomDesignCommon_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.imageTag, "{label:'Product Image',xtype:'imageTag'}");
        
    }
    
    public static HashMap<String, String> CustomDesignSO_JobOrderFlowExtraFieldsMap = new HashMap<String, String>();
    
    static{
        // Sales Order Global fields
        CustomDesignSO_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.SALES_ORDER_NO, "{label:'Sales Order No',xtype:'1'}");
        CustomDesignSO_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.SalesOrderDate, "{label:'Sales Order Date',xtype:'3'}");
        
    }
    // SI Job Order Flow Map details
    public static HashMap<String, String> CustomDesignSI_JobOrderFlowExtraFieldsMap = new HashMap<String, String>();
    
    static{
        // Sales Invoice Global fields
        CustomDesignSI_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.SALES_INVOICE_NO, "{label:'Sales Invoice No',xtype:'1'}");
        CustomDesignSI_JobOrderFlowExtraFieldsMap.put(CustomDesignerConstants.InvoiceDate, "{label:'Sales Invoice Date',xtype:'3'}");
        
    }
    
    // QA Inspection Form extra fields Map
    public static final String INSPECTION_DATE = "InspectionDate";
    public static final String MODEL_NAME = "ModelName";
    public static final String INSPECTION_DESCRIPTION = "InspectionDescription";
    public static final String INSPECTION_CUSTOMER_NAME = "InspectionCustomerName";
    public static final String DEPARTMENT = "Department";
    public static final String INSPECTION_REF_NO = "InspectionRefNo";
    public static final String CONSIGNMENT_RETURN_NO = "ConsignmentReturnNo";
    public static final String INSPECTOR = "Inspector";
    public static final String INSPECTION_STATUS = "InspectionStatus";
    public static final String DO_QUANTITY = "DOQuantity";
    // QA Inspection Template details
    public static final String INSPECTION_TEMPLATE_AREA="InspectionTemplateArea";
    public static final String INSPECTION_TEMPLATE_STATUS="InspectionTemplateStatus";
    public static final String INSPECTION_TEMPLATE_SPECIFIED_FAULTS="InspectionTemplateSpecifiedFaults";
    public static final String INSPECTION_TEMPLATE_FAULTS="InspectionTemplateFaults";
    
    // DO extra fields Map
    public static HashMap<String, String> CustomDesignDO_QA_Approval_ExtraFieldsMap = new HashMap<String, String>();
    static{
        // DO Global fields
        CustomDesignDO_QA_Approval_ExtraFieldsMap.put(CustomDesignerConstants.Poreferencenumber, "{label:'PO Reference No',xtype:'1'}"); 
        // DO Product level fields
        CustomDesignDO_QA_Approval_ExtraFieldsMap.put(CustomDesignerConstants.DO_QUANTITY, "{label:'DO Quantity',xtype:'2'}"); 
        
    }
    // Stock Adjustment extra fields Map
    public static HashMap<String, String> CustomDesignStockAdjustment_QA_Approval_ExtraFieldsMap = new HashMap<String, String>();
    static{
        // Stock Adjustment Global fields
        // Stock Adjustment Product level fields
        CustomDesignStockAdjustment_QA_Approval_ExtraFieldsMap.put(CustomDesignerConstants.Quantity, "{label:'SA Quantity',xtype:'2'}");
        
    }
    public static HashMap<String, String> CustomDesign_QA_Inspecation_Form_ExtraFieldsMap = new HashMap<String, String>();
    static{
        // QA Inspection Form Global fields
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.INSPECTION_DATE, "{label:'Inspection Date',xtype:'3'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.MODEL_NAME, "{label:'Model Name',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.INSPECTION_DESCRIPTION, "{label:'Inspection Description',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.INSPECTION_CUSTOMER_NAME, "{label:'Inspection Customer Name',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.DEPARTMENT, "{label:'Department',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.INSPECTION_REF_NO, "{label:'Inspection Ref No',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.CONSIGNMENT_RETURN_NO, "{label:'Consignment Return No',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.INSPECTOR, "{label:'Inspector',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch No',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serial No',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.IN_ProductCode, "{label:'Product Code',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.ProductName, "{label:'Product Name',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.Warehouse, "{label:'Warehouse',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.Location, "{label:'Location',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.INSPECTION_STATUS, "{label:'Inspection Status',xtype:'1'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.Quantity, "{label:'Quantity',xtype:'2'}");
        CustomDesign_QA_Inspecation_Form_ExtraFieldsMap.put(CustomDesignerConstants.CustomDesignRemarks_fieldTypeId, "{label:'Remark',xtype:'1'}");
        
    }
    // MRP Work Order
    public static final String WORK_CENTER = "WorkCenter";
    public static final String MACHINES = "Machines";
    public static final String LABOURS = "Labours";
    public static final String WORK_ORDER_STATUS = "WorkOrderStatus";
    public static final String LINK_TO = "LinkNo";
    public static final String LINK_DOCUMENT_NO = "LinkDocumentNo";
    public static final String LINK_DOCUMENT_DATE = "LinkDocumentDate";
    //Details Table fields - Component Availability/ Consumption
    public static final String BALANCE_QUANTITY = "BalanceQuantity";
    public static final String REQUIRED_QUANTITY = "RequiredQuantity";
    public static final String ACTUAL_QUANTITY = "ActualQuantity";
    public static final String REJECTED_QUANTITY = "RejectedQuantity";
    public static final String WASTE_QUANTITY = "WasteQuantity";
    public static final String RECYCLE_QTY = "RecycleQuantity";
    public static final String PRODUCED_QUANTITY = "ProducedQuantity";
    public static final String TOTAL_BLOCK_QUANTITY = "TotalBlockQuantity";
    public static final String BLOCK_QUANTITY = "BlockQuantity";
    public static final String MIN_PERCENT_QUANTITY_REQUIRED = "MinPercentQuantityRequired";
    public static final String BLOCKED_QTY_BY_OTHER_ORDERS = "BlockedQuantityByOtherOrders";
    public static final String SHORTFALL_QUANTITY = "ShortfallQuantity";
    public static final String ORDER_QUANTITY = "OrderQuantity";
    public static final String WAREHOUSE =  "warehouse";
    public static final String LOCATION = "location";
    //Details Table fields - Tasks
    public static final String TASK_NAME = "TaskName";
    public static final String NOTES = "Notes";
    public static final String DURATION = "Duration";
    public static final String START_DATE = "StartDate";
    public static final String END_DATE = "EndDate";
    public static final String PROGRESS = "Progress";
    public static final String SKILLS = "Skills";
    public static final String PROCESSES = "Processes";
    public static final String RESOURCE_NAMES = "ResourceNames";
    public static final String CHECKLIST = "Checklist";
    
    //MRP Work Order global fields
    public static HashMap<String, String> CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap = new HashMap<String, String>();
    static{
        CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap.put(CustomDesignerConstants.WORK_CENTER, "{label:'Work Center',xtype:'1'}");
        CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap.put(CustomDesignerConstants.MACHINES, "{label:'Machine(s)',xtype:'1'}");
        CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap.put(CustomDesignerConstants.LABOURS, "{label:'Labour(s)',xtype:'1'}");
        CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap.put(CustomDesignerConstants.WORK_ORDER_STATUS, "{label:'Work Order Status',xtype:'1'}");
        CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap.put(CustomDesignerConstants.LINK_TO, "{label:'Link To',xtype:'1'}");
        CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap.put(CustomDesignerConstants.LINK_DOCUMENT_NO, "{label:'Link Document No',xtype:'1'}");
        CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap.put(CustomDesignerConstants.LINK_DOCUMENT_DATE, "{label:'Link Document Date',xtype:'1'}");
        CustomDesign_MRP_WORK_ORDER_ExtraFieldsMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch Number',xtype:'1'}");
        
    }
    // Bank Reconciliation module constants
    public static final String DATE = "date";
    public static final String CUSTOMER_VENDOR_NAME = "CustomerVendorName";
    public static final String JOURNAL_ENTRY_NO = "JournalEntryNo";
    public static final String TRANSACTION_ID = "TransactionID";
    public static final String REFERENCE_NO_DESC = "ReferenceNoDesc";
    public static final String MEMO = "Memo";
    public static final String DOCUMENT_CURRENCY_SYMBOL = "DocumentCurrencySymbol";
    public static final String RECEIVED_FROM = "ReceivedFrom";
    public static final String PAID_TO = "PaidTo";
    public static final String DEBIT_AMOUNT_IN_DOCUMENT_CURRENCY = "DebitAmountInDocumentCurrency";
    public static final String DEBIT_AMOUNT_IN_ACCOUNT_CURRENCY = "DebitAmountInAccountCurrency";
    public static final String DEBIT_AMOUNT_IN_BASE_CURRENCY = "DebitAmountInBaseCurrency";
    public static final String CREDIT_AMOUNT_IN_DOCUMENT_CURRENCY = "CreditAmountInDocumentCurrency";
    public static final String CREDIT_AMOUNT_IN_ACCOUNT_CURRENCY = "CreditAmountInAccountCurrency";
    public static final String CREDIT_AMOUNT_IN_BASE_CURRENCY = "CreditAmountInBaseCurrency";
    public static final String OPENING_BALANCE_BANK_BOOK = "OpeningBalanceBankBook";
    public static final String BALANCE_BANK_BOOK = "BalanceBankBook";
    public static final String UNCLEARED_DEPOSITS = "UnclearedDeposits";
    public static final String UNCLEARED_CHECKS = "UnclearedChecks";
    public static final String BALANCE_BANK_STATEMENT = "BalanceBankStatement";
    public static final String FROM_DATE = "FromDate";
    public static final String TO_DATE = "ToDate";
    public static final String BANK_NAME = "BankName";
    
    public static HashMap<String, String> CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap = new HashMap<String, String>();
    static{
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_ACCOUNT_CURRENCY, "{label:'Debit Amount in Account Currency',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.DEBIT_AMOUNT_IN_BASE_CURRENCY, "{label:'Debit Amount in Base Currency',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_ACCOUNT_CURRENCY, "{label:'Credit Amount in Account Currency',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.CREDIT_AMOUNT_IN_BASE_CURRENCY, "{label:'Credit Amount in Base Currency',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.OPENING_BALANCE_BANK_BOOK, "{label:'Opening Balance Bank Book',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.BALANCE_BANK_BOOK, "{label:'Balance Bank Book',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.UNCLEARED_CHECKS, "{label:'Uncleared Checks',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.UNCLEARED_DEPOSITS, "{label:'Uncleared Deposits',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.BALANCE_BANK_STATEMENT, "{label:'Balance Bank Statement',xtype:'2'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.FROM_DATE, "{label:'From Date',xtype:'1'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.TO_DATE, "{label:'To Date',xtype:'1'}");
        CustomDesign_BANK_RECONCILIATION_ExtraFieldsMap.put(CustomDesignerConstants.BANK_NAME, "{label:'Bank Name',xtype:'1'}");
        
    }
    
    //Line fields constants for Build Assembly Module - 133 //ERM-26
    public static final String PRODUCT_TYPE = "productType";
    public static final String QUANTITY_NEEDED = "1331";
    public static final String INVENTORY_QUANTITY = "1332";
    public static final String RECYCLE_QUANTITY = "1333";
    public static final String REMAINING_QUANTITY = "1334";
    public static final String WASTAGE_QUANTITY = "1335";
    
    public static HashMap<String, String> CustomDesign_BUILD_ASSEMBLY_ExtraFieldsMap = new HashMap<String, String>();
    static{
        CustomDesign_BUILD_ASSEMBLY_ExtraFieldsMap.put(CustomDesignerConstants.BatchNumber, "{label:'Batch Number',xtype:'1'}");
        CustomDesign_BUILD_ASSEMBLY_ExtraFieldsMap.put(CustomDesignerConstants.SerialNumber, "{label:'Serial Number',xtype:'1'}");
        CustomDesign_BUILD_ASSEMBLY_ExtraFieldsMap.put(CustomDesignerConstants.BatchNumberExp, "{label:'Expiry date',xtype:'3'}");
        CustomDesign_BUILD_ASSEMBLY_ExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Created By',xtype:'1'}");
    }
    
    public static HashMap<String, String> CustomDesign_Email_Notification_ExtraFieldsMap = new HashMap<String, String>();
    static{
        CustomDesign_Email_Notification_ExtraFieldsMap.put(CustomDesignerConstants.TEMPLATE_HYPERLINK_IN_EMAIL, "{label:'Template Hyperlink In Email',xtype:'1'}");
    }
    /**
     * Maps for Job Work modules
     */
    public static final String VENDOR_NAME = "VendorName";
    public static final String JOB_WORK_IN_ORDER_NO = "JobWorkInOrderNo";
    public static final String JOB_WORK_OUT_ORDER_NO = "JobWorkOutOrderNo";
    public static final String JOB_WORK_CHALLAN_NO = "JobWorkChallanNo";
    
    //Map for Job Work Stock In module
    public static HashMap<String, String> CustomDesignExtraFields_For_JobWorkStockIn = new HashMap<String, String>();
    static{
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CUSTOMER_NAME, "{label:'Customer Name',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomerVendor_AccCode, "{label:'Customer Code',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, "{label:'GSTIN Number',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesign_ENTITY_GSTIN_NUMBER, "{label:'Entity GSTIN Number',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CompanyPANNumber, "{label:'Entity PAN Number',xtype:'1'}");
        //Customer address fields
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Customer Billing Address',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Customer Shipping Address',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Customer Billing City',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Customer Billing State',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Customer Billing Country',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Customer Billing Postal Code',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Customer Billing Phone No',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Customer Billing Fax No',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Customer Billing Email',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Customer Shipping City',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Customer Shipping State',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Customer Shipping Country',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Customer Shipping Postal code',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Customer Shipping Phone No',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Customer Shipping Fax No',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Customer Shipping Email',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Customer Billing Contact Person',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Customer Shipping Contact Person',xtype:'1'}");
        
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.JOB_WORK_IN_ORDER_NO, "{label:'Job Work In Order No.',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockIn.put(CustomDesignerConstants.JOB_WORK_CHALLAN_NO, "{label:'Job Work Delivery Challan No.',xtype:'1'}");
    }
    //Map for Job Work Out Stock Transfer module
    public static HashMap<String, String> CustomDesignExtraFields_For_JobWorkStockOutTransfer = new HashMap<String, String>();
    static{
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.VENDOR_NAME, "{label:'Vendor Name',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomerVendor_AccCode, "{label:'Vendor Code',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, "{label:'GSTIN Number',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesign_ENTITY_GSTIN_NUMBER, "{label:'Entity GSTIN Number',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CompanyPANNumber, "{label:'Entity PAN Number',xtype:'1'}");
        //Vendor address fields
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "{label:'Vendor Billing Address',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "{label:'Vendor Shipping Address',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "{label:'Vendor Billing City',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "{label:'Vendor Billing State',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "{label:'Vendor Billing Country',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "{label:'Vendor Billing Postal Code',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "{label:'Vendor Billing Phone No',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "{label:'Vendor Billing Fax No',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingEmailID_fieldTypeId, "{label:'Vendor Billing Email',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "{label:'Vendor Shipping City',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "{label:'Vendor Shipping State',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "{label:'Vendor Shipping Country',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "{label:'Vendor Shipping Postal code',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "{label:'Vendor Shipping Phone No',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "{label:'Vendor Shipping Fax No',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingEmailID_fieldTypeId, "{label:'Vendor Shipping Email',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "{label:'Vendor Billing Contact Person',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingContactPerson_fieldTypeId, "{label:'Vendor Shipping Contact Person',xtype:'1'}");
        
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.JOB_WORK_OUT_ORDER_NO, "{label:'Job Work Out Order No.',xtype:'1'}");
        CustomDesignExtraFields_For_JobWorkStockOutTransfer.put(CustomDesignerConstants.JOB_WORK_CHALLAN_NO, "{label:'Challan No.',xtype:'1'}");
    }
    
    /*
       TDS Line Level Field Map for Vendor Invoice / Make Payment
    */
    public static final HashMap<String, String> TDS_Field_Map = new HashMap();
    
    static {
        TDS_Field_Map.put(CustomDesignerConstants.TDS_RATE, "{label:'TDS Rate', xtype:'2', defwidth:10}");
        TDS_Field_Map.put(CustomDesignerConstants.TDS_AMOUNT, "{label:'TDS Amount', xtype:'2', defwidth:10}");
    }
}
