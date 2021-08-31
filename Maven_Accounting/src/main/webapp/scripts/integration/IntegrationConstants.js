/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This file contains constants from various third party service integrations
 * UPS Integration
 * Avalara Integration
 */

/**
 * ID's of different Integration Parties
 * These ID's are assigned by Deskera as a unique identifier for integration parties
 */
Wtf.integrationPartyId = {
    UPS: 1, //UPS Integration
    IRAS:7, //IRAS Integration
    AVALARA: 2,//Avalara Integration
    DBS: 5//Avalara Integration
};
/**
 * Mapping of bankid with their logo path
 * This mapping is used to get logo of a bank in bank integration
 * @type type
 */
Wtf.bankIdLogoMapping = {};
Wtf.bankIdLogoMapping[Wtf.integrationPartyId.DBS] = "<img src='../../images/dbs_logo.png' style='height:12px'></img>";
/**
 * ID's of different Integration operations
 * These ID's are assigned by Deskera as a unique identifier for integration operations
 */
Wtf.integrationOperationId = {
    ups_costEstimation: "ups_costEstimation", //UPS Cost Estimation
    ups_shipping: "ups_shipping", //UPS Shipping
    ups_labelRecovery: "ups_labelRecovery", //UPS Label Recovery
    //Integration OperationId Constants : Start
    iras_GSTForm5Submission:"iras_GSTForm5Submission",
    iras_TransactionListing:"iras_TransactionListing",
    iras_SingPassAuthCodeGeneration:"iras_SingPassAuthCodeGeneration",
    iras_TokenGeneration:"iras_TokenGeneration",
    //Integration OperationId Constants : End
    avalara_addressValidation: "avalara_addressValidation", //Avalara Address Validation
    avalara_cancelTax: "avalara_cancelTax", //Avalara Cancel(Void) tax
    avalara_changeDocCode: "avalara_changeDocCode", //Avalara Change Doc Code
    avalara_createItems: "avalara_createItems", //Avalara Create Item(s)
    avalara_createOrAdjustTransaction: "avalara_createOrAdjustTransaction", //Avalara Create Or Adjust Transaction (used to calculate and commit tax)
    avalara_credentialsValidation: "avalara_credentialsValidation", //Avalara Credentials Validation
    avalara_deleteItem: "avalara_deleteItem", //Avalara Delete Item
    avalara_getTransaction: "avalara_getTransaction", //Avalara Get Transaction
    avalara_updateItem: "avalara_updateItem" //Avalara Item Update
};
/**
 * ID's of different Integration settings fields
 * An underscore character and integrationPartyId is 
 * appended in these IDs for each integration
 * For example ID of IntegrationCheckbox for UPS integration is "integrationCheck_1"
 */
Wtf.integrationFieldId = {
    integrationCheck: "integrationCheck",
    integrationSettingsBttn: "integrationSettingsBttn",
    integrationSettingsValidationBttn: "integrationSettingsValidationBttn",
    integrationSettingsWindow: "integrationSettingsWindow"
};
/**
 * Constants used in various integrations
 */
Wtf.integration = {
    bankid: "bankid",
    avalaraIntegration: "avalaraIntegration",
    upsIntegration: "upsIntegration",
    irasIntegration:"irasIntegration",
    integrationPartyIdKey: "integrationPartyId",
    integrationOperationIdKey: "integrationOperationId",
    integrationPartyNameKey: "integrationPartyName",
    integrationPartyHiddenNameKey: "integrationPartyHiddenName",
    integrationPartiesData: "integrationPartiesData",
    /**
     * Field label for AvaTax Exemption Code dimension
     * Used in Avalara Integration
     */
    avalaraExemptionCode: "AvaTax Exemption Code",
    /**
     * Document types for Avalara Integration
     */
    avalaraSalesInvoice: "SalesInvoice",
    avalaraReturnInvoice: "ReturnInvoice",
    /**
     * Avalara tax override constants
     */
    TaxDate: "TaxDate",
    Return: "Return",
    /**
     * Avalara Admin Console Page link
     * @type Array
     */
    avalaraAdminConsoleLink: "https://admin-avatax.avalara.net/login.aspx",
    /**
     * Other common constants
     */
    None : "None",
    /**
     * JSONArray with different packaging type codes available on UPS
     * Used in UPS integration
     * @type Array
     */
    upsPackagingTypeArr: [
        {id: "01", name: "UPS Letter"},
        {id: "02", name: "Customer Supplied Package"},
        {id: "03", name: "Tube"},
        {id: "04", name: "PAK"},
        {id: "21", name: "UPS Express Box"},
        {id: "24", name: "UPS 25KG Box"},
        {id: "25", name: "UPS 10KG Box"},
        {id: "30", name: "Pallet"},
        {id: "2a", name: "Small Express Box"},
        {id: "2b", name: "Medium Express Box"},
        {id: "2c", name: "Large Express Box"},
        {id: "59", name: "First Class"},
        {id: "56", name: "Flats"},
        {id: "57", name: "Parcels"},
        {id: "58", name: "BPM"},
        {id: "59", name: "First Class"},
        {id: "60", name: "Priority"},
        {id: "61", name: "Machineables"},
        {id: "62", name: "Irregulars"},
        {id: "63", name: "Parcel Post"},
        {id: "64", name: "BPM Parcel"},
        {id: "65", name: "Media Mail"},
        {id: "66", name: "BPM Flat"},
        {id: "67", name: "Standard Flat"}
    ],
    /**
     * JSONArray with different delivery confirmation type codes available on UPS
     * Used in UPS integration
     * @type Array
     */
    upsDeliveryConfirmationTypeArr: [
        {id: "1", name: "No Signature Required"},
        {id: "2", name: "Signature Required"},
        {id: "3", name: "Adult Signature Required"}
    ],
    /**
     * JSONArray with different service type codes available on UPS
     * Used in UPS integration
     * @type Array
     */
    upsServiceTypeArr: [
        {id: "01", name: "Next Day Air"},
        {id: "02", name: "2nd Day Air"},
        {id: "03", name: "Ground"},
        {id: "07", name: "Express"},
        {id: "08", name: "Expedited"},
        {id: "11", name: "UPS Standard"},
        {id: "12", name: "3 Day Select"},
        {id: "13", name: "Next Day Air Saver"},
        {id: "14", name: "UPS Next Day Air Early"},
        {id: "54", name: "Express Plus"},
        {id: "59", name: "2nd Day Air A.M."},
        {id: "65", name: "UPS Saver"},
        {id: "M2", name: "First Class Mail"},
        {id: "M3", name: "Priority Mail"},
        {id: "M4", name: "Expedited Mail Innovations"},
        {id: "M5", name: "Priority Mail Innovations"},
        {id: "M6", name: "Economy Mail Innovations"},
        {id: "M7", name: "Mail Innovations Returns"},
        {id: "70", name: "UPS Access Point Economy"},
        {id: "82", name: "UPS Today Standard"},
        {id: "83", name: "UPS Today Dedicated Courier"},
        {id: "84", name: "UPS Today Intercity"},
        {id: "85", name: "UPS Today Express"},
        {id: "86", name: "UPS Today Express Saver"},
        {id: "96", name: "UPS Worldwide Express Freight"}
    ],
    /**
     * Values of UPS billing options
     */
    upsBillShipperOption: "01",
    upsBillReceiverOption: "02",
    upsBillThirdPartyOption: "03",
    /**
     * JSONArray with different billing options codes available on UPS
     * Used in UPS integration
     * @type Array
     */
    upsShipmentBillingOptionsArr: [
        {id: "01", name: "Bill Shipper"},
        {id: "02", name: "Bill Receiver"},
        {id: "03", name: "Bill Third Party"}
    ],
    /**
     * Keys used for UPS shipment details window fields
     */
    serviceType: "serviceType",
    shipmentBillingOption: "shipmentBillingOption",
    billingAccountNumber: "billingAccountNumber",
    billingAccountPostal: "billingAccountPostal",
    billingAccountCountry: "billingAccountCountry",
    /**
     * Keys used for UPS package details window fields
     */
    packagingType: "packagingType",
    deliveryConfirmationType: "deliveryConfirmationType",
    additionalHandling: "additionalHandling",
    packageNumber: "packageNumber",
    packageWeight: "packageWeight",
    packageDimensions: "packageDimensions",
    declaredValue: "declaredValue",
    /**
     * Keys used for UPS ShipFrom and ShipTo addresses
     */
    shipFrom_contactPersonName: "shipFrom_contactPersonName",
    shipFrom_Name: "shipFrom_Name",
    shipFrom_AddressLine: "shipFrom_AddressLine",
    shipFrom_City: "shipFrom_City",
    shipFrom_StateProvinceCode: "shipFrom_StateProvinceCode",
    shipFrom_PostalCode: "shipFrom_PostalCode",
    shipFrom_CountryCode: "shipFrom_CountryCode",
    shipFrom_PhoneNumber: "shipFrom_PhoneNumber",
    shipFrom_IsResidentialAddress: "shipFrom_IsResidentialAddress",
    shipTo_contactPersonName: "shipTo_contactPersonName",
    shipTo_Name: "shipTo_Name",
    shipTo_AddressLine: "shipTo_AddressLine",
    shipTo_City: "shipTo_City",
    shipTo_StateProvinceCode: "shipTo_StateProvinceCode",
    shipTo_PostalCode: "shipTo_PostalCode",
    shipTo_CountryCode: "shipTo_CountryCode",
    shipTo_PhoneNumber: "shipTo_PhoneNumber",
    shipTo_IsResidentialAddress: "shipTo_IsResidentialAddress",
    /**
     * Keys used for UPS Email notification window fields
     */
    emailAddress1: "emailAddress1",
    emailAddress2: "emailAddress2",
    undeliverableEMailAddress: "undeliverableEMailAddress",
    fromEmailAddress: "fromEmailAddress",
    fromEmailName: "fromEmailName",
    emailMemo: "emailMemo",
    /**
     * Keys used for Avalara integration settings fields
     */
    taxCommitting: "taxCommitting",
    taxCalculation: "taxCalculation",
    taxAccountId: "taxAccountId",
    addressValidation: "addressValidation",
    transactionLogging: "transactionLogging",
    requestTimeout: "requestTimeout",
    /**
     * Keys used for integration cedentials fields
     */
    userName: "userName",
    passKey: "passKey",
    accountNumber: "accountNumber",
    licenseKey: "licenseKey",
    restServiceUrl: "restServiceUrl"
};