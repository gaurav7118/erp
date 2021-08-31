/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.ups;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.accounting.integration.common.IntegrationUtil;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public class UpsIntegrationUtil extends IntegrationUtil {

    public Map<String, String> createHeadersMap() {
        Map<String, String> headersMap = new HashMap();
        headersMap.put(IntegrationConstants.content_type, "application/json");
        headersMap.put(IntegrationConstants.accept, "text/json");
        return headersMap;
    }

    public Map<String, Object> createOtherReqPropertiesMap() {
        Map<String, Object> otherReqPropertiesMap = new HashMap();
        otherReqPropertiesMap.put(IntegrationConstants.doOutput, true);
        return otherReqPropertiesMap;
    }

    /**
     * This method is used to create request body for Label-recovery request
     */
    public JSONObject getPayloadForLabelRecovery(JSONObject paramsjobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        returnJobj.put("LabelRecoveryRequest", getLabelRecoveryJson(paramsjobj));
        returnJobj.put("UPSSecurity", getUPSSecurityJson(integrationAccountDetails));
        return returnJobj;
    }

    /**
     * Method to create 'LabelRecoveryRequest' JSON which is required by
     * label-recovery request
     */
    private JSONObject getLabelRecoveryJson(JSONObject paramsjobj) throws JSONException {
        JSONObject LabelRecoveryRequest = new JSONObject();                 //LabelRecoveryRequest

        JSONObject LabelSpecification = new JSONObject();                   //LabelSpecification
        LabelSpecification.put("HTTPUserAgent", "Mozilla/4.5");
        LabelSpecification.put("LabelImageFormatCode", "GIF");

        JSONObject Translate = new JSONObject();                            //Translate
        Translate.put("DialectCode", "GB");
        Translate.put("LanguageCode", "eng");
        Translate.put("Code", "01");

        LabelRecoveryRequest.put("LabelSpecification", LabelSpecification);
        LabelRecoveryRequest.put("TrackingNumber", (String) paramsjobj.optString(IntegrationConstants.TrackingNumber));
        LabelRecoveryRequest.put("Translate", Translate);
        return LabelRecoveryRequest;
    }

    /**
     * This method is used to create request body for Shipment request
     */
    public JSONObject getPayloadForShipping(JSONObject paramsjobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        JSONObject ShipmentRequest = new JSONObject();                      //ShipmentRequest
        ShipmentRequest.put("Request", getRequestOptionsJson(paramsjobj));
        ShipmentRequest.put("Shipment", getShipmentJson(paramsjobj,integrationAccountDetails));
        ShipmentRequest.put("LabelSpecification", getLabelSpecificationJson(paramsjobj));

        returnJobj.put("UPSSecurity", getUPSSecurityJson(integrationAccountDetails));
        returnJobj.put("ShipmentRequest", ShipmentRequest);
        return returnJobj;
    }

    /**
     * This method creates request body for Cost Estimation request
     */
    public JSONObject getPayloadForCostEstimation(JSONObject paramsjobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        JSONObject ShipConfirmRequest = new JSONObject();                   //ShipConfirmRequest
        ShipConfirmRequest.put("Request", getRequestOptionsJson(paramsjobj));
        ShipConfirmRequest.put("Shipment", getShipmentJson(paramsjobj, integrationAccountDetails));
        ShipConfirmRequest.put("LabelSpecification", getLabelSpecificationJson(paramsjobj));

        returnJobj.put("UPSSecurity", getUPSSecurityJson(integrationAccountDetails));
        returnJobj.put("ShipConfirmRequest", ShipConfirmRequest);
        return returnJobj;
    }

    /**
     * Method to create 'Shipment' JSON which is required by shipment request
     * and cost estimation request
     */
    private JSONObject getShipmentJson(JSONObject paramsjobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject Shipment = new JSONObject();                             //Shipment
        String shipmentDetailsStr = paramsjobj.optString(IntegrationConstants.shipmentDetails);
        JSONObject shipmentDetailsJobj = new JSONObject(shipmentDetailsStr);
        Shipment.put("ShipFrom", getShipFromAddressJson(shipmentDetailsJobj));
        Shipment.put("ShipTo", getShipToAddressJson(shipmentDetailsJobj));
        Shipment.put("Shipper", getShipperAddressJson(shipmentDetailsJobj,integrationAccountDetails));
        Shipment.put("PaymentInformation", getPaymentInformationJson(shipmentDetailsJobj, integrationAccountDetails));
        if (!StringUtil.isNullOrEmpty(paramsjobj.optString(IntegrationConstants.emailNotificationDetails))) {
            //Email notification details, added to request only if Email Notification Details are provided by user
            Shipment.put("ShipmentServiceOptions", getShipmentServiceOptionsJson(paramsjobj));
        }
        Shipment.put("Service", getServiceJson(shipmentDetailsJobj));
        Shipment.put("Package", getPackageDetailsJson(paramsjobj));
        return Shipment;
    }

    /**
     * Create 'RequestOption' JSON which is used in shipment request and cost
     * estimation request
     */
    private JSONObject getRequestOptionsJson(JSONObject paramsjobj) throws JSONException {
        JSONObject Request = new JSONObject();                              //Request

        JSONObject TransactionReference = new JSONObject();                 //TransactionReference
        TransactionReference.put("CustomerContext", "Your Customer Context");

        Request.put("RequestOption", "validate");
        Request.put("TransactionReference", TransactionReference);
        return Request;
    }

    /**
     * Create 'LabelSpecification' JSON which is used in shipment request and
     * cost estimation request
     */
    private JSONObject getLabelSpecificationJson(JSONObject paramsjobj) throws JSONException {
        JSONObject LabelSpecification = new JSONObject();                   //Shipping Lable details
        JSONObject LabelImageFormat = new JSONObject();
        LabelImageFormat.put("Code", "GIF");

        LabelSpecification.put("LabelImageFormat", LabelImageFormat);
        LabelSpecification.put("HTTPUserAgent", "Mozilla/4.5");
        return LabelSpecification;
    }

    /**
     * Create 'UPSSecurity' JSON which used in shipment request, cost estimation
     * request, and label recovery request This JSON contains authentication
     * details for the REST call
     */
    private JSONObject getUPSSecurityJson(JSONObject integrationAccountDetails) throws JSONException {
        JSONObject UPSSecurity = new JSONObject();                          //UPSSecurity
        JSONObject UsernameToken = new JSONObject();                        //UsernameToken
        UsernameToken.put("Username", integrationAccountDetails.optString(IntegrationConstants.userName));
        UsernameToken.put("Password", integrationAccountDetails.optString(IntegrationConstants.passKey));

        JSONObject ServiceAccessToken = new JSONObject();                   //ServiceAccessToken
        ServiceAccessToken.put("AccessLicenseNumber", integrationAccountDetails.optString(IntegrationConstants.licenseKey));

        UPSSecurity.put("UsernameToken", UsernameToken);
        UPSSecurity.put("ServiceAccessToken", ServiceAccessToken);
        return UPSSecurity;
    }

    /**
     * Create 'Package' JSON which is used in 'Shipment' JSON used in shipment
     * request and cost estimation request This JSONArray contains all details
     * about packages in the shipment
     */
    private JSONArray getPackageDetailsJson(JSONObject paramsjobj) throws JSONException {
        JSONArray Package = new JSONArray();            //JSONArray to contain packages' details
        String packageDetailsStr = paramsjobj.optString(IntegrationConstants.packageDetails);
        JSONArray packageDetailsJarr = new JSONArray(packageDetailsStr);
        for (int i = 0; i < packageDetailsJarr.length(); i++) {
            JSONObject packageDetailsJobj = packageDetailsJarr.optJSONObject(i);
            String packageDimensions = packageDetailsJobj.optString(IntegrationConstants.packageDimensions);
            /**
             * Replace '*' with ',' and then split on ',' Can't split on '*'
             * directly as '*' is a meta character
             */
            String[] packageDimArr = packageDimensions.replace("*", ",").split(",");
            JSONObject PackageJobj = new JSONObject();

            JSONObject Packaging = new JSONObject();
            Packaging.put("Code", packageDetailsJobj.optString(IntegrationConstants.packagingType));
            PackageJobj.put("Packaging", Packaging);

            JSONObject Dimensions = new JSONObject();                           //Dimensions of Package
            JSONObject Dimensions_UnitOfMeasurement = new JSONObject();         //UnitOfMeasurement
            Dimensions_UnitOfMeasurement.put("Code", IntegrationConstants.IN);
            Dimensions.put("UnitOfMeasurement", Dimensions_UnitOfMeasurement);
            Dimensions.put("Length", packageDimArr[0].trim());
            Dimensions.put("Width", packageDimArr[1].trim());
            Dimensions.put("Height", packageDimArr[2].trim());
            PackageJobj.put("Dimensions", Dimensions);

            JSONObject PackageWeight = new JSONObject();                        //Weight
            JSONObject Weight_UnitOfMeasurement = new JSONObject();             //UnitOfMeasurement
            Weight_UnitOfMeasurement.put("Code", IntegrationConstants.LBS);
            PackageWeight.put("UnitOfMeasurement", Weight_UnitOfMeasurement);
            PackageWeight.put("Weight", packageDetailsJobj.optString(IntegrationConstants.packageWeight).trim());
            PackageJobj.put("PackageWeight", PackageWeight);

            if (!StringUtil.isNullOrEmpty(packageDetailsJobj.optString(IntegrationConstants.packageNumber).trim())) {
                JSONObject ReferenceNumber = new JSONObject();
                ReferenceNumber.put("Value", StringUtil.decodeString(packageDetailsJobj.optString(IntegrationConstants.packageNumber)));
                PackageJobj.put("ReferenceNumber", ReferenceNumber);
            }

            if (!StringUtil.isNullOrEmpty(packageDetailsJobj.optString(IntegrationConstants.additionalHandling)) && StringUtil.equal(packageDetailsJobj.optString(IntegrationConstants.additionalHandling), "1")) {
                PackageJobj.put("AdditionalHandlingIndicator", "");
            }
            if (!StringUtil.isNullOrEmpty(packageDetailsJobj.optString(IntegrationConstants.declaredValue).trim())) {                          //Declared Value
                JSONObject DeclaredValue = new JSONObject();
                DeclaredValue.put("CurrencyCode", IntegrationConstants.USD);
                DeclaredValue.put("MonetaryValue", packageDetailsJobj.optString(IntegrationConstants.declaredValue));

                JSONObject PackageServiceOptions = new JSONObject();
                PackageServiceOptions.put("DeclaredValue", DeclaredValue);

                PackageJobj.put("PackageServiceOptions", PackageServiceOptions);
            }

            JSONObject DeliveryConfirmation = new JSONObject();         //Delivery Confirmation Type
            DeliveryConfirmation.put("DCISType", packageDetailsJobj.optString(IntegrationConstants.deliveryConfirmationType));
            PackageJobj.put("DeliveryConfirmation", DeliveryConfirmation);

            Package.put(PackageJobj);
        }
        return Package;
    }

    /**
     * Create 'Service' JSON which is used in shipment request and cost
     * estimation request Contains service code for the shipping service to be
     * used in the shipment
     */
    private JSONObject getServiceJson(JSONObject paramsjobj) throws JSONException {
        JSONObject Service = new JSONObject();                              //Service
        Service.put("Code", paramsjobj.optString(IntegrationConstants.serviceType));
        return Service;
    }

    /**
     * Create Shipper's address JSON
     */
    private JSONObject getShipperAddressJson(JSONObject paramsjobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject Shipper = new JSONObject();                                //shipper
        JSONObject shipperAddress = new JSONObject();                         //Address
        shipperAddress.put("AddressLine", paramsjobj.optString(IntegrationConstants.shipFrom_AddressLine) != null ? (paramsjobj.optString(IntegrationConstants.shipFrom_AddressLine)).replaceAll("\n", " ") : "");
        shipperAddress.put("City", paramsjobj.optString(IntegrationConstants.shipFrom_City));
        shipperAddress.put("StateProvinceCode", paramsjobj.optString(IntegrationConstants.shipFrom_StateProvinceCode));
        shipperAddress.put("PostalCode", paramsjobj.optString(IntegrationConstants.shipFrom_PostalCode));
        shipperAddress.put("CountryCode", paramsjobj.optString(IntegrationConstants.shipFrom_CountryCode));
        if (paramsjobj.has(IntegrationConstants.shipFrom_IsResidentialAddress)) {           //Indicator for Residential Address
            shipperAddress.put("ResidentialAddressIndicator", "");
        }

        JSONObject shipperPhone = new JSONObject();                           //Phone
        shipperPhone.put("Number", paramsjobj.optString(IntegrationConstants.shipFrom_PhoneNumber));

        Shipper.put("Name", paramsjobj.optString(IntegrationConstants.shipFrom_Name));
        Shipper.put("Address", shipperAddress);
        Shipper.put("ShipperNumber", integrationAccountDetails.optString(IntegrationConstants.accountNumber));
        Shipper.put("AttentionName", paramsjobj.optString(IntegrationConstants.shipFrom_contactPersonName));
        Shipper.put("Phone", shipperPhone);
        return Shipper;
    }

    /**
     * Create ShipTo address (receiver's address) JSON
     */
    private JSONObject getShipToAddressJson(JSONObject paramsjobj) throws JSONException {
        JSONObject ShipTo = new JSONObject();                               //ShipTo
        JSONObject shipToPhone = new JSONObject();                        //Phone
        shipToPhone.put("Number", paramsjobj.optString(IntegrationConstants.shipTo_PhoneNumber));

        JSONObject shipToAddress = new JSONObject();                        //Address
        shipToAddress.put("AddressLine", paramsjobj.optString(IntegrationConstants.shipTo_AddressLine) != null ? (paramsjobj.optString(IntegrationConstants.shipTo_AddressLine)).replaceAll("\n", " ") : "");
        shipToAddress.put("City", paramsjobj.optString(IntegrationConstants.shipTo_City));
        shipToAddress.put("StateProvinceCode", paramsjobj.optString(IntegrationConstants.shipTo_StateProvinceCode));
        shipToAddress.put("PostalCode", paramsjobj.optString(IntegrationConstants.shipTo_PostalCode));
        shipToAddress.put("CountryCode", paramsjobj.optString(IntegrationConstants.shipTo_CountryCode));
        if (paramsjobj.has(IntegrationConstants.shipTo_IsResidentialAddress)) {     //Indicator for Residential Address
            shipToAddress.put("ResidentialAddressIndicator", "");
        }

        ShipTo.put("Name", paramsjobj.optString(IntegrationConstants.shipTo_Name));
        ShipTo.put("Address", shipToAddress);
        ShipTo.put("Phone", shipToPhone);
        ShipTo.put("AttentionName", paramsjobj.optString(IntegrationConstants.shipTo_contactPersonName));
        return ShipTo;
    }

    /**
     * create ShipFrom address (pick-up address) JSON
     */
    private JSONObject getShipFromAddressJson(JSONObject paramsjobj) throws JSONException {
        JSONObject ShipFrom = new JSONObject();                             //ShipFrom

        JSONObject shipFromAddress = new JSONObject();                      //Address
        shipFromAddress.put("AddressLine", paramsjobj.optString(IntegrationConstants.shipFrom_AddressLine) != null ? (paramsjobj.optString(IntegrationConstants.shipFrom_AddressLine)).replaceAll("\n", " ") : "");
        shipFromAddress.put("City", paramsjobj.optString(IntegrationConstants.shipFrom_City));
        shipFromAddress.put("StateProvinceCode", paramsjobj.optString(IntegrationConstants.shipFrom_StateProvinceCode));
        shipFromAddress.put("PostalCode", paramsjobj.optString(IntegrationConstants.shipFrom_PostalCode));
        shipFromAddress.put("CountryCode", paramsjobj.optString(IntegrationConstants.shipFrom_CountryCode));
        if (paramsjobj.has(IntegrationConstants.shipFrom_IsResidentialAddress)) {          //Indicator for Residential Address
            shipFromAddress.put("ResidentialAddressIndicator", "");
        }

        JSONObject shipFromPhone = new JSONObject();                        //Phone
        shipFromPhone.put("Number", paramsjobj.optString(IntegrationConstants.shipFrom_PhoneNumber));

        ShipFrom.put("Name", paramsjobj.optString(IntegrationConstants.shipFrom_Name));
        ShipFrom.put("Address", shipFromAddress);
        ShipFrom.put("AttentionName", paramsjobj.optString(IntegrationConstants.shipFrom_contactPersonName));
        ShipFrom.put("Phone", shipFromPhone);

        return ShipFrom;
    }

    /**
     * Create 'PaymentInformation' JSOn
     */
    private JSONObject getPaymentInformationJson(JSONObject paramsjobj, JSONObject integrationAccountDetails) throws JSONException {
        JSONObject PaymentInformation = new JSONObject();                   //PaymentInformation

        JSONObject ShipmentCharge = new JSONObject();
        ShipmentCharge.put("Type", "01");//"01"-> Transportation, "02"-> Duties And Taxes
        if (paramsjobj.has(IntegrationConstants.shipmentBillingOption)) {                  //Billing details
            String shipmentBillingOption = paramsjobj.optString(IntegrationConstants.shipmentBillingOption);
            if (StringUtil.equal(shipmentBillingOption, "01")) {//Bill Shipper
                JSONObject BillShipper = new JSONObject();
                BillShipper.put("AccountNumber", integrationAccountDetails.optString(IntegrationConstants.accountNumber));
                ShipmentCharge.put("BillShipper", BillShipper);
            } else if (StringUtil.equal(shipmentBillingOption, "02")) {//Bill Receiver
                JSONObject BillReceiver = new JSONObject();
                BillReceiver.put("AccountNumber", paramsjobj.optString(IntegrationConstants.billingAccountNumber));
                JSONObject Address = new JSONObject();
                Address.put("PostalCode", paramsjobj.optString(IntegrationConstants.billingAccountPostal));
                BillReceiver.put("Address", Address);
                ShipmentCharge.put("BillReceiver", BillReceiver);
            } else if (StringUtil.equal(shipmentBillingOption, "03")) {//Bill Third Party
                JSONObject BillThirdParty = new JSONObject();
                BillThirdParty.put("AccountNumber", paramsjobj.optString(IntegrationConstants.billingAccountNumber));
                JSONObject Address = new JSONObject();
                Address.put("PostalCode", paramsjobj.optString(IntegrationConstants.billingAccountPostal));
                Address.put("CountryCode", paramsjobj.optString(IntegrationConstants.billingAccountCountry));
                BillThirdParty.put("Address", Address);
                ShipmentCharge.put("BillThirdParty", BillThirdParty);
            }
        }
        PaymentInformation.put("ShipmentCharge", ShipmentCharge);

        return PaymentInformation;
    }

    /**
     * Create 'ShipmentServiceOptions' JSON Contains details for Email
     * notifications on the shipment's movements
     */
    private JSONObject getShipmentServiceOptionsJson(JSONObject paramsjobj) throws JSONException {
        JSONObject ShipmentServiceOptions = new JSONObject();
        
        String emailNotificationDetailsStr = paramsjobj.optString(IntegrationConstants.emailNotificationDetails);
        if (!StringUtil.isNullOrEmpty(emailNotificationDetailsStr)) {
            JSONObject emailDetailsJobj = new JSONObject(emailNotificationDetailsStr);

            String[] EmailAddress = null;
            if (StringUtil.isNullOrEmpty(emailDetailsJobj.optString(IntegrationConstants.emailAddress2))) {
                EmailAddress = new String[]{emailDetailsJobj.optString(IntegrationConstants.emailAddress1)};
            } else {
                EmailAddress = new String[]{emailDetailsJobj.optString(IntegrationConstants.emailAddress1), emailDetailsJobj.optString(IntegrationConstants.emailAddress2)};
            }

            JSONArray Notification = new JSONArray();                           //Details for Email Notification
            JSONObject Email = new JSONObject();
            Email.put("EMailAddress", EmailAddress);
            Email.put("FromEMailAddress", emailDetailsJobj.optString(IntegrationConstants.fromEmailAddress));
            if (!StringUtil.isNullOrEmpty(emailDetailsJobj.optString(IntegrationConstants.undeliverableEMailAddress))) {
                Email.put("UndeliverableEMailAddress", emailDetailsJobj.optString(IntegrationConstants.undeliverableEMailAddress));
            }
            if (!StringUtil.isNullOrEmpty(emailDetailsJobj.optString(IntegrationConstants.fromEmailName))) {
                Email.put("FromName", emailDetailsJobj.optString(IntegrationConstants.fromEmailName));
            }
            if (!StringUtil.isNullOrEmpty(emailDetailsJobj.optString(IntegrationConstants.emailMemo))) {
                Email.put("Memo", emailDetailsJobj.optString(IntegrationConstants.emailMemo));
            }
            JSONObject NotificationJobj = new JSONObject();
            NotificationJobj.put("EMail", Email);
            NotificationJobj.put("NotificationCode", "6");//6 - Quantum View Ship Notification
            Notification.put(NotificationJobj);

            Email = new JSONObject();
            Email.put("EMailAddress", EmailAddress);
            Email.put("FromEMailAddress", emailDetailsJobj.optString(IntegrationConstants.fromEmailAddress));
            NotificationJobj = new JSONObject();
            NotificationJobj.put("EMail", Email);
            NotificationJobj.put("NotificationCode", "7");//7 - Quantum View Exception Notification
            Notification.put(NotificationJobj);

            Email = new JSONObject();
            Email.put("EMailAddress", EmailAddress);
            Email.put("FromEMailAddress", emailDetailsJobj.optString(IntegrationConstants.fromEmailAddress));
            NotificationJobj = new JSONObject();
            NotificationJobj.put("EMail", Email);
            NotificationJobj.put("NotificationCode", "8");//8 - Quantum View Delivery Notification
            Notification.put(NotificationJobj);

            ShipmentServiceOptions.put("Notification", Notification);
        }

        return ShipmentServiceOptions;
    }
}
