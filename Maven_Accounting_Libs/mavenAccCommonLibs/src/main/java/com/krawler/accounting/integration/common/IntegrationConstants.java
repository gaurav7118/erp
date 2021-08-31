/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.common;

import com.krawler.accounting.integration.ups.UpsIntegrationService;
import com.krawler.accounting.integration.avalara.AvalaraIntegrationService;
import com.krawler.accounting.integration.iras.IRASIntegrationService;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public class IntegrationConstants {

    /**
     * Integration Constants (common) - START
     */
    public static final String integrationPartyIdKey = "integrationPartyId";
    public static final String integrationPartyNameKey = "integrationPartyName";
    public static final String integrationPartyHiddenNameKey = "integrationPartyHiddenName";
    public static final String integrationGlobalSettingsJson = "integrationGlobalSettingsJson";
    public static final String integrationConfigJson = "integrationConfigJson";
    public static final String integrationPartiesData = "integrationPartiesData";
    public static final String skipRequestJsonProcessing = "skipRequestJsonProcessing";
    public static final String integrationDetails = "integrationDetails";
    public static final String integrationOperationIdKey = "integrationOperationId";
    public static final String restServiceUrl = "restServiceUrl";
    public static final String parentRecordID = "parentRecordID";
    public static final String userName = "userName";
    public static final String passKey = "passKey";
    public static final String accountNumber = "accountNumber";
    public static final String accountCode = "acccode";
    public static final String licenseKey = "licenseKey";
    public static final String configJson = "configJson";
    public static final String credentialsData = "credentialsData";
    public static final String error = "error";
    public static final String code = "code";
    public static final String response = "response";
    public static final String responseCode = "responseCode";
    public static final int responseCode_299 = 299;//All response with code less than 299 are to be considered as success
    public static final String content_type = "Content-type";
    public static final String accept = "accept";
    public static final String doOutput = "doOutput";
    /**
     * Integration Constants (common) - END
     */

    /**
     * UPS Integration Constants - START
     */
    public static final int integrationPartyId_UPS = 1;
    public static final String upsIntegration = "upsIntegration";
    public static final String ups_costEstimation = "ups_costEstimation";
    public static final String ups_shipping = "ups_shipping";
    public static final String ups_labelRecovery = "ups_labelRecovery";
//    public static final String userName = "kapil.gupta";
//    public static final String passKey = "upsinteg-1";
//    public static final String accessKey = "5D1FE1011E89A5CC";
//    public static final String accountNumber = "22FR99";                              //this field is used as ShipperNumber in request JSON
//    public static final String freightURL = "https://wwwcie.ups.com/rest/FreightShip";//testing URL for ground freight
//    public static final String freightURL = "https://onlinetools.ups.com/rest/FreightShip";//production URL for ground freight
    public static final String shipURL = "https://wwwcie.ups.com/rest/Ship";//testing URL for shipping
//    public static final String shipURL = "https://onlinetools.ups.com/rest/Ship";//production URL for shipping
    public static final String costEstimationURL = "https://wwwcie.ups.com/rest/Ship";//testing URL for shipping cost estimation (Ship-Confirm request)
//    public static final String costEstimationURL = "https://onlinetools.ups.com/rest/Ship";//production URL for shipping cost estimation (Ship-Confirm request)
    public static final String labelURL = "https://wwwcie.ups.com/rest/LBRecovery";//testing URL for shipping label
//    public static final String labelURL = "https://onlinetools.ups.com/rest/LBRecovery";//production URL for shipping label
    public static final String shipContext = "/rest/Ship";//URL context for shipping requests
    public static final String labelRecoveryContext = "/rest/LBRecovery";//URL context for shipping label recovery requests
    public static final String shipFrom_contactPersonName = "shipFrom_contactPersonName";
    public static final String shipFrom_Name = "shipFrom_Name";
    public static final String shipFrom_AddressLine = "shipFrom_AddressLine";
    public static final String shipFrom_City = "shipFrom_City";
    public static final String shipFrom_StateProvinceCode = "shipFrom_StateProvinceCode";
    public static final String shipFrom_PostalCode = "shipFrom_PostalCode";
    public static final String shipFrom_CountryCode = "shipFrom_CountryCode";
    public static final String shipFrom_PhoneNumber = "shipFrom_PhoneNumber";
    public static final String shipFrom_IsResidentialAddress = "shipFrom_IsResidentialAddress";
    public static final String shipper_contactPersonName = "shipper_contactPersonName";
    public static final String shipper_Name = "shipper_Name";
    public static final String shipper_AddressLine = "shipper_AddressLine";
    public static final String shipper_City = "shipper_City";
    public static final String shipper_StateProvinceCode = "shipper_StateProvinceCode";
    public static final String shipper_PostalCode = "shipper_PostalCode";
    public static final String shipper_CountryCode = "shipper_CountryCode";
    public static final String shipper_PhoneNumber = "shipper_PhoneNumber";
    public static final String shipper_IsResidentialAddress = "shipper_IsResidentialAddress";
    public static final String shipTo_contactPersonName = "shipTo_contactPersonName";
    public static final String shipTo_Name = "shipTo_Name";
    public static final String shipTo_AddressLine = "shipTo_AddressLine";
    public static final String shipTo_City = "shipTo_City";
    public static final String shipTo_StateProvinceCode = "shipTo_StateProvinceCode";
    public static final String shipTo_PostalCode = "shipTo_PostalCode";
    public static final String shipTo_CountryCode = "shipTo_CountryCode";
    public static final String shipTo_PhoneNumber = "shipTo_PhoneNumber";
    public static final String shipTo_IsResidentialAddress = "shipTo_IsResidentialAddress";
    public static final String shipmentBillingOption = "shipmentBillingOption";
    public static final String billingAccountNumber = "billingAccountNumber";
    public static final String billingAccountPostal = "billingAccountPostal";
    public static final String billingAccountCountry = "billingAccountCountry";
    public static final String serviceType = "serviceType";
    public static final String shipmentDetails = "shipmentDetails";
    public static final String emailNotificationDetails = "emailNotificationDetails";
    public static final String TrackingNumber = "TrackingNumber";
    public static final String trackingNumber = "trackingNumber";
    public static final String ShippingLabel = "ShippingLabel";
    public static final String shippingLabel = "shippingLabel";
    public static final String packageDetails = "packageDetails";
    public static final String packageDimensions = "packageDimensions";
    public static final String packagingType = "packagingType";
    public static final String packageWeight = "packageWeight";
    public static final String packageNumber = "packageNumber";
    public static final String additionalHandling = "additionalHandling";
    public static final String declaredValue = "declaredValue";
    public static final String deliveryConfirmationType = "deliveryConfirmationType";
    public static final String emailAddress1 = "emailAddress1";
    public static final String emailAddress2 = "emailAddress2";
    public static final String fromEmailAddress = "fromEmailAddress";
    public static final String fromEmailName = "fromEmailName";
    public static final String undeliverableEMailAddress = "undeliverableEMailAddress";
    public static final String emailMemo = "emailMemo";
    public static final String totalShippingCost = "totalShippingCost";
    public static final String shippedWithUPS = "shippedWithUPS";
    public static final String IN = "IN";
    public static final String LBS = "LBS";
    public static final String USD = "USD";
    public static final String salesOrderCostEstimationFlag = "salesOrderCostEstimationFlag";
    /**
     * UPS Integration Constants - END
     */

    /**
     * Avalara Integration Constants - START
     */
    public static final int integrationPartyId_AVALARA = 2;
    public static final String avalara_addressValidation = "avalara_addressValidation";
    public static final String avalara_cancelTax = "avalara_cancelTax";
    public static final String avalara_changeDocCode = "avalara_changeDocCode";
    public static final String avalara_createItems = "avalara_createItems";
    public static final String avalara_createOrAdjustTransaction = "avalara_createOrAdjustTransaction";
    public static final String avalara_credentialsValidation = "avalara_credentialsValidation";
    public static final String avalara_deleteItem = "avalara_deleteItem";
    public static final String avalara_getTransaction = "avalara_getTransaction";
    public static final String avalara_updateItem = "avalara_updateItem";
    public static final String avalaraIntegration = "avalaraIntegration";
    public static final String avataxExemptionCode = "AvaTax Exemption Code";//Field label for AvaTax Exemption Code dimension
    public static final String avalaraDocCode = "avalaraDocCode";
    public static final String avalaraExemptionCode = "avalaraExemptionCode";
    public static final String commit = "commit";
    public static final String avalaraProductTaxCode = "avalaraProductTaxCode";
    public static final String avalaraTaxDetails = "avalaraTaxDetails";
    public static final String taxOverrideDocId = "taxOverrideDocId";
    public static final String taxOverrideDocModuleId = "taxOverrideDocModuleId";
    public static final String taxOverrideType = "taxOverrideType";
    public static final String taxOverrideDate = "taxOverrideDate";
    public static final String taxOverrideReason = "taxOverrideReason";
    public static final String taxCommitting = "taxCommitting";
    public static final String taxCalculation = "taxCalculation";
    public static final String shipToAddressForAvalara = "shipToAddressForAvalara";
    public static final String addressesForValidationWithAvalara = "addressesForValidationWithAvalara";

    /**
     * API Version and Contexts for REST service URL for API v2.0 (url_version2)
     * is stored with Avalara Account credentials in database as we have a field
     * for IntegrationService URL in credentials window We use URL from database
     * which is provided by user in Avalara Integration Settings window in
     * System Controls
     */
    public static final String api_version2 = "/api/v2/";//version context for API v2.0
    public static final String companies = "companies";//context path for 'companies' API methods
    public static final String items = "items";//context path for 'items' API methods
    public static final String transactions = "transactions";//context path for 'transactions' API methods
    public static final String types = "types";//context path for 'types' API methods
    public static final String createoradjust = "createoradjust";//context path for 'createoradjust' API methods
    public static final String changecode = "changecode";//context path for 'changecode 'API methods
    public static final String addresses = "addresses";//context path for 'addresses' API methods
    public static final String void_context = "void";//context for void method
    public static final String resolve = "resolve";//context for addresses validation method

    /**
     * Client and UID headers reuqired by Avalara REST service on each call
     * These are provided by Avalara and must not change unless new values are
     * provided by them These values are for Identification of calls from
     * Deskera ERP Consequently, these are not client specific Same values are
     * to be set for call from each client
     */
    public static final String x_Avalara_Client = "Deskera ERP; 1.0; REST; v2";//request header required by Avalara REST service
    public static final String x_Avalara_UID = "a0o33000004WR6i";//request header required by Avalara REST service, the value is provided by Avalara
    /**
     * Avalara Integration Constants - END
     */

        /**
     * IRAS Integration Constants - START
     */
    public static final int integrationPartyId_IRAS = 7;
    public static final String irasIntegration = "irasIntegration";
    public static final String iras_X_IBM_Client_ID="9c43491e-8fe6-4055-af1b-b4b44a5570e5";//Sandbox credential, pulled from DB
    public static final String iras_X_IBM_Client_Secret="xN3kT1rY5cC0qP0uK6nG6dR8hX4tG3yX4jB8wL0lJ3gD3gA2vI";//Sandbox credential, pulled from DB
    public static final String iras_TransactionListing_Operation="iras_TransactionListing_Operation";
    public static final String iras_GSTForm5Submission_Operation="iras_GSTForm5Submission_Operation";
    public static final String iras_SingPassAuthCodeGeneration_Operation="iras_SingPassAuthCodeGeneration_Operation";
    public static final String iras_TokenGeneration_Operation="iras_TokenGeneration_Operation";
    
    public static final String iras_Integration_Common_Url="https://apisandbox.iras.gov.sg/iras/sb";//Sandbox credential, pulled from DB
    public static final String iras_TransactionListing_context_UA="/gst/submitTransactionListing_UA";//Sandbox credential. For Unauthenticated testing
    public static final String iras_TransactionListing_context="/gst/submitTransactionListing";
    public static final String iras_GSTForm5Submission_context_UA="/gst/submitF5Return_UA";//Sandbox credential. For Unauthenticated testing
    public static final String iras_GSTForm5Submission_context="/gst/submitF5Return";
    public static final String iras_SingPassAuthCodeGeneration_context="/Authentication/SingPassAuth";
    public static final String iras_TokenGeneration_context="/Authentication/SingPassToken";
    public static final String iras_SingPassAuthCode_Scope="GSTReturnsSub+GSTTransListSub";
    public static final String iras_SingPassAuthCode_callback_url="http://sandbox.deskera.com/irasdemo/callback.jsp";//Sandbox credential
    
    /**
     * IRAS Integration Constants - END
     */
    
    public static final Map<Integer, IntegrationService> integrationServiceMap = new HashMap();

    static {
        integrationServiceMap.put(integrationPartyId_UPS, new UpsIntegrationService());
        integrationServiceMap.put(integrationPartyId_AVALARA, new AvalaraIntegrationService());
        integrationServiceMap.put(integrationPartyId_IRAS, new IRASIntegrationService());
    }
    
     /*
     DBS integration Constant
    */
     public static final String BANK_ID = "bankid";
     public static final String BANK_ACCOUNT_NAME = "bankaccountname";
     public static final String BANK_ACCOUNT_NUMBER = "bankaccountnumber";
     public static final String BANK_DETAILS = "bankaccountdetails";
     public static final String DESKERA_ACCOUNT_ID = "deskeraaccount";
     public static final String DESKERA_ACCOUNT_NAME = "deskeraaccountname";
    
}
