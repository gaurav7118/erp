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
package com.krawler.common.admin;

import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public class AccountingAddressManager {

    public static JSONObject getTransactionAddressJSON(JSONObject obj, BillingShippingAddresses addresses, boolean isVendorTransaction) throws SessionExpiredException, JSONException {
        obj.put(Constants.BILLING_ADDRESS, addresses == null ? "" : addresses.getBillingAddress());
        obj.put(Constants.BILLING_COUNTY, addresses == null ? "" : addresses.getBillingCounty());
        obj.put(Constants.BILLING_CITY, addresses == null ? "" : addresses.getBillingCity());
        obj.put(Constants.BILLING_CONTACT_PERSON, addresses == null ? "" : addresses.getBillingContactPerson());
        obj.put(Constants.BILLING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getBillingContactPersonNumber());
        obj.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getBillingContactPersonDesignation());
        obj.put(Constants.BILLING_WEBSITE, addresses == null ? "" : addresses.getBillingWebsite());
        obj.put(Constants.BILLING_COUNTRY, addresses == null ? "" : addresses.getBillingCountry());
        obj.put(Constants.BILLING_EMAIL, addresses == null ? "" : addresses.getBillingEmail());
        obj.put(Constants.BILLING_FAX, addresses == null ? "" : addresses.getBillingFax());
        obj.put(Constants.BILLING_MOBILE, addresses == null ? "" : addresses.getBillingMobile());
        obj.put(Constants.BILLING_PHONE, addresses == null ? "" : addresses.getBillingPhone());
        obj.put(Constants.BILLING_RECIPIENT_NAME, addresses == null ? "" : addresses.getBillingRecipientName());
        obj.put(Constants.BILLING_POSTAL, addresses == null ? "" : addresses.getBillingPostal());
        obj.put(Constants.BILLING_STATE, addresses == null ? "" : addresses.getBillingState());
        obj.put(Constants.BILLING_ADDRESS_TYPE, addresses == null ? "" : addresses.getBillingAddressType());
        obj.put(Constants.SHIPPING_ADDRESS, addresses == null ? "" : addresses.getShippingAddress());
        obj.put(Constants.SHIPPING_COUNTY, addresses == null ? "" : addresses.getShippingCounty());
        obj.put(Constants.SHIPPING_CITY, addresses == null ? "" : addresses.getShippingCity());
        obj.put(Constants.SHIPPING_CONTACT_PERSON, addresses == null ? "" : addresses.getShippingContactPerson());
        obj.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getShippingContactPersonNumber());
        obj.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getShippingContactPersonDesignation());
        obj.put(Constants.SHIPPING_WEBSITE, addresses == null ? "" : addresses.getShippingWebsite());
        obj.put(Constants.SHIPPING_COUNTRY, addresses == null ? "" : addresses.getShippingCountry());
        obj.put(Constants.SHIPPING_EMAIL, addresses == null ? "" : addresses.getShippingEmail());
        obj.put(Constants.SHIPPING_FAX, addresses == null ? "" : addresses.getShippingFax());
        obj.put(Constants.SHIPPING_MOBILE, addresses == null ? "" : addresses.getShippingMobile());
        obj.put(Constants.SHIPPING_PHONE, addresses == null ? "" : addresses.getShippingPhone());
        obj.put(Constants.SHIPPING_RECIPIENT_NAME, addresses == null ? "" : addresses.getShippingRecipientName());
        obj.put(Constants.SHIPPING_POSTAL, addresses == null ? "" : addresses.getShippingPostal());
        obj.put(Constants.SHIPPING_STATE, addresses == null ? "" : addresses.getShippingState());
        obj.put(Constants.SHIPPING_ROUTE, addresses == null ? "" : addresses.getShippingRoute());
        obj.put(Constants.SHIPPING_ADDRESS_TYPE, addresses == null ? "" : addresses.getShippingAddressType());
        if (isVendorTransaction) {
            obj.put(Constants.VENDCUST_SHIPPING_ADDRESS, addresses == null ? "" : addresses.getVendcustShippingAddress());
            obj.put(Constants.VENDCUST_SHIPPING_STATE, addresses == null ? "" : addresses.getVendcustShippingState());
            obj.put(Constants.VENDCUST_SHIPPING_COUNTRY, addresses == null ? "" : addresses.getVendcustShippingCountry());
            obj.put(Constants.VENDCUST_SHIPPING_COUNTY, addresses == null ? "" : addresses.getVendcustShippingCounty());
            obj.put(Constants.VENDCUST_SHIPPING_CITY, addresses == null ? "" : addresses.getVendcustShippingCity());
            obj.put(Constants.VENDCUST_SHIPPING_EMAIL, addresses == null ? "" : addresses.getVendcustShippingEmail());
            obj.put(Constants.VENDCUST_SHIPPING_FAX, addresses == null ? "" : addresses.getVendcustShippingFax());
            obj.put(Constants.VENDCUST_SHIPPING_POSTAL, addresses == null ? "" : addresses.getVendcustShippingPostal());
            obj.put(Constants.VENDCUST_SHIPPING_MOBILE, addresses == null ? "" : addresses.getVendcustShippingMobile());
            obj.put(Constants.VENDCUST_SHIPPING_PHONE, addresses == null ? "" : addresses.getVendcustShippingPhone());
            obj.put(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME, addresses == null ? "" : addresses.getVendcustShippingRecipientName());
            obj.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getVendcustShippingContactPersonNumber());
            obj.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getVendcustShippingContactPersonDesignation());
            obj.put(Constants.VENDCUST_SHIPPING_WEBSITE, addresses == null ? "" : addresses.getVendcustShippingWebsite());
            obj.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON, addresses == null ? "" : addresses.getVendcustShippingContactPerson());
            obj.put(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE, addresses == null ? "" : addresses.getVendcustShippingAddressType());
            /**
             * Get Vendor billing address from transaction for IDNIA country 
             * and "Show vendors address in purchase document" is OFF
             */
            obj.put(Constants.VENDOR_BILLING_ADDRESS, addresses == null ? "" : addresses.getVendorBillingAddressForINDIA());
            obj.put(Constants.VENDOR_BILLING_COUNTY, addresses == null ? "" : addresses.getVendorBillingCountyForINDIA());
            obj.put(Constants.VENDOR_BILLING_CITY, addresses == null ? "" : addresses.getVendorBillingCityForINDIA());
            obj.put(Constants.VENDOR_BILLING_CONTACT_PERSON, addresses == null ? "" : addresses.getVendorBillingContactPersonForINDIA());
            obj.put(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getVendorBillingContactPersonNumberForINDIA());
            obj.put(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getVendorBillingContactPersonDesignationForINDIA());
            obj.put(Constants.VENDOR_BILLING_WEBSITE, addresses == null ? "" : addresses.getVendorBillingWebsiteForINDIA());
            obj.put(Constants.VENDOR_BILLING_COUNTRY, addresses == null ? "" : addresses.getVendorBillingCountryForINDIA());
            obj.put(Constants.VENDOR_BILLING_EMAIL, addresses == null ? "" : addresses.getVendorBillingEmailForINDIA());
            obj.put(Constants.VENDOR_BILLING_FAX, addresses == null ? "" : addresses.getVendorBillingFaxForINDIA());
            obj.put(Constants.VENDOR_BILLING_MOBILE, addresses == null ? "" : addresses.getVendorBillingMobileForINDIA());
            obj.put(Constants.VENDOR_BILLING_PHONE, addresses == null ? "" : addresses.getVendorBillingPhoneForINDIA());
            obj.put(Constants.VENDOR_BILLING_RECIPIENT_NAME, addresses == null ? "" : addresses.getVendorBillingRecipientNameForINDIA());
            obj.put(Constants.VENDOR_BILLING_POSTAL, addresses == null ? "" : addresses.getVendorBillingPostalForINDIA());
            obj.put(Constants.VENDOR_BILLING_STATE, addresses == null ? "" : addresses.getVendorBillingStateForINDIA());
            obj.put(Constants.VENDOR_BILLING_ADDRESS_TYPE, addresses == null ? "" : addresses.getVendorBillingAddressTypeForINDIA());
        }
        
        
        /*------While linking dropship Purchase Order in Purchase Invoice----------*/
        if (obj.optBoolean("isdropshipchecked", false)) {

            obj.put(Constants.CUSTOMER_SHIPPING_ADDRESS, addresses == null ? "" : addresses.getCustomerShippingAddress());
            obj.put(Constants.CUSTOMER_SHIPPING_STATE, addresses == null ? "" : addresses.getCustomerShippingState());
            obj.put(Constants.CUSTOMER_SHIPPING_COUNTRY, addresses == null ? "" : addresses.getCustomerShippingCountry());
            obj.put(Constants.CUSTOMER_SHIPPING_COUNTY, addresses == null ? "" : addresses.getCustomerShippingCounty());
            obj.put(Constants.CUSTOMER_SHIPPING_CITY, addresses == null ? "" : addresses.getCustomerShippingCity());
            obj.put(Constants.CUSTOMER_SHIPPING_EMAIL, addresses == null ? "" : addresses.getCustomerShippingEmail());
            obj.put(Constants.CUSTOMER_SHIPPING_FAX, addresses == null ? "" : addresses.getCustomerShippingFax());
            obj.put(Constants.CUSTOMER_SHIPPING_POSTAL, addresses == null ? "" : addresses.getCustomerShippingPostal());
            obj.put(Constants.CUSTOMER_SHIPPING_MOBILE, addresses == null ? "" : addresses.getCustomerShippingMobile());
            obj.put(Constants.CUSTOMER_SHIPPING_PHONE, addresses == null ? "" : addresses.getCustomerShippingPhone());
            obj.put(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, addresses == null ? "" : addresses.getCustomerShippingRecipientName());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getCustomerShippingContactPersonNumber());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getCustomerShippingContactPersonDesignation());
            obj.put(Constants.CUSTOMER_SHIPPING_WEBSITE, addresses == null ? "" : addresses.getCustomerShippingWebsite());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, addresses == null ? "" : addresses.getCustomerShippingContactPerson());
            obj.put(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, addresses == null ? "" : addresses.getCustomerShippingAddressType());
            obj.put(Constants.CUSTOMER_SHIPPING_ROUTE, addresses == null ? "" : addresses.getCustomerShippingRoute());

            /*-------Vendor billing address------------  */
            obj.put(Constants.DropShip_BILLING_ADDRESS, addresses == null ? "" : addresses.getVendorBillingAddress());
            obj.put(Constants.DropShip_BILLING_COUNTY, addresses == null ? "" : addresses.getVendorBillingCounty());
            obj.put(Constants.DropShip_BILLING_CITY, addresses == null ? "" : addresses.getVendorBillingCity());
            obj.put(Constants.DropShip_BILLING_CONTACT_PERSON, addresses == null ? "" : addresses.getVendorBillingContactPerson());
            obj.put(Constants.DropShip_BILLING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getVendorBillingContactPersonNumber());
            obj.put(Constants.DropShip_BILLING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getVendorBillingContactPersonDesignation());
            obj.put(Constants.DropShip_BILLING_WEBSITE, addresses == null ? "" : addresses.getVendorBillingWebsite());
            obj.put(Constants.DropShip_BILLING_COUNTRY, addresses == null ? "" : addresses.getVendorBillingCountry());
            obj.put(Constants.DropShip_BILLING_EMAIL, addresses == null ? "" : addresses.getVendorBillingEmail());
            obj.put(Constants.DropShip_BILLING_FAX, addresses == null ? "" : addresses.getVendorBillingFax());
            obj.put(Constants.DropShip_BILLING_MOBILE, addresses == null ? "" : addresses.getVendorBillingMobile());
            obj.put(Constants.DropShip_BILLING_PHONE, addresses == null ? "" : addresses.getVendorBillingPhone());
            obj.put(Constants.DropShip_BILLING_RECIPIENT_NAME, addresses == null ? "" : addresses.getVendorBillingRecipientName());
            obj.put(Constants.DropShip_BILLING_POSTAL, addresses == null ? "" : addresses.getVendorBillingPostal());
            obj.put(Constants.DropShip_BILLING_STATE, addresses == null ? "" : addresses.getVendorBillingState());
            obj.put(Constants.DropShip_BILLING_ADDRESS_TYPE, addresses == null ? "" : addresses.getVendorBillingAddressType());
        }
        return obj;
    }
    /*
    Below method is used to get Transactional level address when PO created from SO.
    */
    public static JSONObject getTransactionAddressJSONForPOFromSO(JSONObject obj, BillingShippingAddresses addresses, boolean isVendorTransaction) throws SessionExpiredException, JSONException {
        
        obj=getTransactionAddressJSON(obj, addresses,isVendorTransaction);
        
        if (obj.has("isTransactionLevelAddress") && obj.optBoolean("isTransactionLevelAddress",false)) {
            obj.put(Constants.CUSTOMER_SHIPPING_ADDRESS, addresses == null ? "" : addresses.getCustomerShippingAddress());
            obj.put(Constants.CUSTOMER_SHIPPING_STATE, addresses == null ? "" : addresses.getCustomerShippingState());
            obj.put(Constants.CUSTOMER_SHIPPING_COUNTRY, addresses == null ? "" : addresses.getCustomerShippingCountry());
            obj.put(Constants.CUSTOMER_SHIPPING_COUNTY, addresses == null ? "" : addresses.getCustomerShippingCounty());
            obj.put(Constants.CUSTOMER_SHIPPING_CITY, addresses == null ? "" : addresses.getCustomerShippingCity());
            obj.put(Constants.CUSTOMER_SHIPPING_EMAIL, addresses == null ? "" : addresses.getCustomerShippingEmail());
            obj.put(Constants.CUSTOMER_SHIPPING_FAX, addresses == null ? "" : addresses.getCustomerShippingFax());
            obj.put(Constants.CUSTOMER_SHIPPING_POSTAL, addresses == null ? "" : addresses.getCustomerShippingPostal());
            obj.put(Constants.CUSTOMER_SHIPPING_MOBILE, addresses == null ? "" : addresses.getCustomerShippingMobile());
            obj.put(Constants.CUSTOMER_SHIPPING_PHONE, addresses == null ? "" : addresses.getCustomerShippingPhone());
            obj.put(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, addresses == null ? "" : addresses.getCustomerShippingRecipientName());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getCustomerShippingContactPersonNumber());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getCustomerShippingContactPersonDesignation());
            obj.put(Constants.CUSTOMER_SHIPPING_WEBSITE, addresses == null ? "" : addresses.getCustomerShippingWebsite());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, addresses == null ? "" : addresses.getCustomerShippingContactPerson());
            obj.put(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, addresses == null ? "" : addresses.getCustomerShippingAddressType());
            obj.put(Constants.CUSTOMER_SHIPPING_ROUTE, addresses == null ? "" : addresses.getCustomerShippingRoute());
        } else {
            obj.put(Constants.CUSTOMER_SHIPPING_ADDRESS, addresses == null ? "" : addresses.getShippingAddress());
            obj.put(Constants.CUSTOMER_SHIPPING_STATE, addresses == null ? "" : addresses.getShippingState());
            obj.put(Constants.CUSTOMER_SHIPPING_COUNTRY, addresses == null ? "" : addresses.getShippingCountry());
            obj.put(Constants.CUSTOMER_SHIPPING_COUNTY, addresses == null ? "" : addresses.getShippingCounty());
            obj.put(Constants.CUSTOMER_SHIPPING_CITY, addresses == null ? "" : addresses.getShippingCity());
            obj.put(Constants.CUSTOMER_SHIPPING_EMAIL, addresses == null ? "" : addresses.getShippingEmail());
            obj.put(Constants.CUSTOMER_SHIPPING_FAX, addresses == null ? "" : addresses.getShippingFax());
            obj.put(Constants.CUSTOMER_SHIPPING_POSTAL, addresses == null ? "" : addresses.getShippingPostal());
            obj.put(Constants.CUSTOMER_SHIPPING_MOBILE, addresses == null ? "" : addresses.getShippingMobile());
            obj.put(Constants.CUSTOMER_SHIPPING_PHONE, addresses == null ? "" : addresses.getShippingPhone());
            obj.put(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, addresses == null ? "" : addresses.getShippingRecipientName());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getShippingContactPersonNumber());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getShippingContactPersonDesignation());
            obj.put(Constants.CUSTOMER_SHIPPING_WEBSITE, addresses == null ? "" : addresses.getShippingWebsite());
            obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, addresses == null ? "" : addresses.getShippingContactPerson());
            obj.put(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, addresses == null ? "" : addresses.getShippingAddressType());
            obj.put(Constants.CUSTOMER_SHIPPING_ROUTE, addresses == null ? "" : addresses.getShippingRoute());
        }
        return obj;
    }
    
    /* ----Get addresses json for dropship type document----- */
    public static JSONObject getTransactionAddressJSONForDropShipDoc(JSONObject obj, BillingShippingAddresses addresses) throws SessionExpiredException, JSONException {

        /*---Customer shipping address-----------  */
        obj.put(Constants.CUSTOMER_SHIPPING_ADDRESS, addresses == null ? "" : addresses.getCustomerShippingAddress());
        obj.put(Constants.CUSTOMER_SHIPPING_STATE, addresses == null ? "" : addresses.getCustomerShippingState());
        obj.put(Constants.CUSTOMER_SHIPPING_COUNTRY, addresses == null ? "" : addresses.getCustomerShippingCountry());
        obj.put(Constants.CUSTOMER_SHIPPING_COUNTY, addresses == null ? "" : addresses.getCustomerShippingCounty());
        obj.put(Constants.CUSTOMER_SHIPPING_CITY, addresses == null ? "" : addresses.getCustomerShippingCity());
        obj.put(Constants.CUSTOMER_SHIPPING_EMAIL, addresses == null ? "" : addresses.getCustomerShippingEmail());
        obj.put(Constants.CUSTOMER_SHIPPING_FAX, addresses == null ? "" : addresses.getCustomerShippingFax());
        obj.put(Constants.CUSTOMER_SHIPPING_POSTAL, addresses == null ? "" : addresses.getCustomerShippingPostal());
        obj.put(Constants.CUSTOMER_SHIPPING_MOBILE, addresses == null ? "" : addresses.getCustomerShippingMobile());
        obj.put(Constants.CUSTOMER_SHIPPING_PHONE, addresses == null ? "" : addresses.getCustomerShippingPhone());
        obj.put(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, addresses == null ? "" : addresses.getCustomerShippingRecipientName());
        obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getCustomerShippingContactPersonNumber());
        obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getCustomerShippingContactPersonDesignation());
        obj.put(Constants.CUSTOMER_SHIPPING_WEBSITE, addresses == null ? "" : addresses.getCustomerShippingWebsite());
        obj.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, addresses == null ? "" : addresses.getCustomerShippingContactPerson());
        obj.put(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, addresses == null ? "" : addresses.getCustomerShippingAddressType());
        obj.put(Constants.CUSTOMER_SHIPPING_ROUTE, addresses == null ? "" : addresses.getCustomerShippingRoute());

        
        /*-----Company billing address------------- */
        obj.put(Constants.BILLING_ADDRESS, addresses == null ? "" : addresses.getBillingAddress());
        obj.put(Constants.BILLING_COUNTY, addresses == null ? "" : addresses.getBillingCounty());
        obj.put(Constants.BILLING_CITY, addresses == null ? "" : addresses.getBillingCity());
        obj.put(Constants.BILLING_CONTACT_PERSON, addresses == null ? "" : addresses.getBillingContactPerson());
        obj.put(Constants.BILLING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getBillingContactPersonNumber());
        obj.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getBillingContactPersonDesignation());
        obj.put(Constants.BILLING_WEBSITE, addresses == null ? "" : addresses.getBillingWebsite());
        obj.put(Constants.BILLING_COUNTRY, addresses == null ? "" : addresses.getBillingCountry());
        obj.put(Constants.BILLING_EMAIL, addresses == null ? "" : addresses.getBillingEmail());
        obj.put(Constants.BILLING_FAX, addresses == null ? "" : addresses.getBillingFax());
        obj.put(Constants.BILLING_MOBILE, addresses == null ? "" : addresses.getBillingMobile());
        obj.put(Constants.BILLING_PHONE, addresses == null ? "" : addresses.getBillingPhone());
        obj.put(Constants.BILLING_RECIPIENT_NAME, addresses == null ? "" : addresses.getBillingRecipientName());
        obj.put(Constants.BILLING_POSTAL, addresses == null ? "" : addresses.getBillingPostal());
        obj.put(Constants.BILLING_STATE, addresses == null ? "" : addresses.getBillingState());
        obj.put(Constants.BILLING_ADDRESS_TYPE, addresses == null ? "" : addresses.getBillingAddressType());

        
        /*-------Vendor billing address------------  */
        obj.put(Constants.DropShip_BILLING_ADDRESS, addresses == null ? "" : addresses.getVendorBillingAddress());
        obj.put(Constants.DropShip_BILLING_COUNTY, addresses == null ? "" : addresses.getVendorBillingCounty());
        obj.put(Constants.DropShip_BILLING_CITY, addresses == null ? "" : addresses.getVendorBillingCity());
        obj.put(Constants.DropShip_BILLING_CONTACT_PERSON, addresses == null ? "" : addresses.getVendorBillingContactPerson());
        obj.put(Constants.DropShip_BILLING_CONTACT_PERSON_NUMBER, addresses == null ? "" : addresses.getVendorBillingContactPersonNumber());
        obj.put(Constants.DropShip_BILLING_CONTACT_PERSON_DESIGNATION, addresses == null ? "" : addresses.getVendorBillingContactPersonDesignation());
        obj.put(Constants.DropShip_BILLING_WEBSITE, addresses == null ? "" : addresses.getVendorBillingWebsite());
        obj.put(Constants.DropShip_BILLING_COUNTRY, addresses == null ? "" : addresses.getVendorBillingCountry());
        obj.put(Constants.DropShip_BILLING_EMAIL, addresses == null ? "" : addresses.getVendorBillingEmail());
        obj.put(Constants.DropShip_BILLING_FAX, addresses == null ? "" : addresses.getVendorBillingFax());
        obj.put(Constants.DropShip_BILLING_MOBILE, addresses == null ? "" : addresses.getVendorBillingMobile());
        obj.put(Constants.DropShip_BILLING_PHONE, addresses == null ? "" : addresses.getVendorBillingPhone());
        obj.put(Constants.DropShip_BILLING_RECIPIENT_NAME, addresses == null ? "" : addresses.getVendorBillingRecipientName());
        obj.put(Constants.DropShip_BILLING_POSTAL, addresses == null ? "" : addresses.getVendorBillingPostal());
        obj.put(Constants.DropShip_BILLING_STATE, addresses == null ? "" : addresses.getVendorBillingState());
        obj.put(Constants.DropShip_BILLING_ADDRESS_TYPE, addresses == null ? "" : addresses.getVendorBillingAddressType());

        return obj;
    }
    
    
    public static Map<String, Object> getAddressParams(HttpServletRequest request, boolean isVendorTransaction) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        try {
            addressMap.put(Constants.BILLING_ADDRESS, request.getParameter(Constants.BILLING_ADDRESS));
            addressMap.put(Constants.BILLING_COUNTRY, request.getParameter(Constants.BILLING_COUNTRY));
            addressMap.put(Constants.BILLING_STATE, request.getParameter(Constants.BILLING_STATE));
            addressMap.put(Constants.BILLING_COUNTY, request.getParameter(Constants.BILLING_COUNTY));
            addressMap.put(Constants.BILLING_CITY, request.getParameter(Constants.BILLING_CITY));
            addressMap.put(Constants.BILLING_POSTAL, request.getParameter(Constants.BILLING_POSTAL));
            addressMap.put(Constants.BILLING_EMAIL, request.getParameter(Constants.BILLING_EMAIL));
            addressMap.put(Constants.BILLING_FAX, request.getParameter(Constants.BILLING_FAX));
            addressMap.put(Constants.BILLING_MOBILE, request.getParameter(Constants.BILLING_MOBILE));
            addressMap.put(Constants.BILLING_PHONE, request.getParameter(Constants.BILLING_PHONE));
            addressMap.put(Constants.BILLING_RECIPIENT_NAME, request.getParameter(Constants.BILLING_RECIPIENT_NAME));
            addressMap.put(Constants.BILLING_CONTACT_PERSON, request.getParameter(Constants.BILLING_CONTACT_PERSON));
            addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, request.getParameter(Constants.BILLING_CONTACT_PERSON_NUMBER));
            addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, request.getParameter(Constants.BILLING_CONTACT_PERSON_DESIGNATION));
            addressMap.put(Constants.BILLING_WEBSITE, request.getParameter(Constants.BILLING_WEBSITE));
            addressMap.put(Constants.BILLING_ADDRESS_TYPE, request.getParameter(Constants.BILLING_ADDRESS_TYPE));
            addressMap.put(Constants.SHIPPING_ADDRESS, request.getParameter(Constants.SHIPPING_ADDRESS));
            addressMap.put(Constants.SHIPPING_COUNTRY, request.getParameter(Constants.SHIPPING_COUNTRY));
            addressMap.put(Constants.SHIPPING_STATE, request.getParameter(Constants.SHIPPING_STATE));
            addressMap.put(Constants.SHIPPING_COUNTY, request.getParameter(Constants.SHIPPING_COUNTY));
            addressMap.put(Constants.SHIPPING_CITY, request.getParameter(Constants.SHIPPING_CITY));
            addressMap.put(Constants.SHIPPING_EMAIL, request.getParameter(Constants.SHIPPING_EMAIL));
            addressMap.put(Constants.SHIPPING_FAX, request.getParameter(Constants.SHIPPING_FAX));
            addressMap.put(Constants.SHIPPING_MOBILE, request.getParameter(Constants.SHIPPING_MOBILE));
            addressMap.put(Constants.SHIPPING_PHONE, request.getParameter(Constants.SHIPPING_PHONE));
            addressMap.put(Constants.SHIPPING_POSTAL, request.getParameter(Constants.SHIPPING_POSTAL));
            addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, request.getParameter(Constants.SHIPPING_CONTACT_PERSON_NUMBER));
            addressMap.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, request.getParameter(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION));
            addressMap.put(Constants.SHIPPING_WEBSITE, request.getParameter(Constants.SHIPPING_WEBSITE));
            addressMap.put(Constants.SHIPPING_CONTACT_PERSON, request.getParameter(Constants.SHIPPING_CONTACT_PERSON));
            addressMap.put(Constants.SHIPPING_RECIPIENT_NAME, request.getParameter(Constants.SHIPPING_RECIPIENT_NAME));
            addressMap.put(Constants.SHIPPING_ROUTE, request.getParameter(Constants.SHIPPING_ROUTE));
            addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, request.getParameter(Constants.SHIPPING_ADDRESS_TYPE));
            if (isVendorTransaction) {
                addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS, request.getParameter(Constants.VENDCUST_SHIPPING_ADDRESS));
                addressMap.put(Constants.VENDCUST_SHIPPING_STATE, request.getParameter(Constants.VENDCUST_SHIPPING_STATE));
                addressMap.put(Constants.VENDCUST_SHIPPING_COUNTRY, request.getParameter(Constants.VENDCUST_SHIPPING_COUNTRY));
                addressMap.put(Constants.VENDCUST_SHIPPING_COUNTY, request.getParameter(Constants.VENDCUST_SHIPPING_COUNTY));
                addressMap.put(Constants.VENDCUST_SHIPPING_CITY, request.getParameter(Constants.VENDCUST_SHIPPING_CITY));
                addressMap.put(Constants.VENDCUST_SHIPPING_EMAIL, request.getParameter(Constants.VENDCUST_SHIPPING_EMAIL));
                addressMap.put(Constants.VENDCUST_SHIPPING_FAX, request.getParameter(Constants.VENDCUST_SHIPPING_FAX));
                addressMap.put(Constants.VENDCUST_SHIPPING_POSTAL, request.getParameter(Constants.VENDCUST_SHIPPING_POSTAL));
                addressMap.put(Constants.VENDCUST_SHIPPING_MOBILE, request.getParameter(Constants.VENDCUST_SHIPPING_MOBILE));
                addressMap.put(Constants.VENDCUST_SHIPPING_PHONE, request.getParameter(Constants.VENDCUST_SHIPPING_PHONE));
                addressMap.put(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME, request.getParameter(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME));
                addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER, request.getParameter(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER));
                addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION, request.getParameter(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION));
                addressMap.put(Constants.VENDCUST_SHIPPING_WEBSITE, request.getParameter(Constants.VENDCUST_SHIPPING_WEBSITE));
                addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON, request.getParameter(Constants.VENDCUST_SHIPPING_CONTACT_PERSON));
                addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE, request.getParameter(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE));
                /**
                 * For Vendor document save Vendor address for INDIA country
                 * if "Show vendors address in purchase document" flag OFF
                 */
                addressMap.put(Constants.VENDOR_BILLING_ADDRESS, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_ADDRESS)) ? request.getParameter(Constants.VENDOR_BILLING_ADDRESS) : "");
                addressMap.put(Constants.VENDOR_BILLING_COUNTRY, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_COUNTRY)) ? request.getParameter(Constants.VENDOR_BILLING_COUNTRY) : "");
                addressMap.put(Constants.VENDOR_BILLING_STATE, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_STATE)) ? request.getParameter(Constants.VENDOR_BILLING_STATE) : "");
                addressMap.put(Constants.VENDOR_BILLING_COUNTY, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_COUNTY)) ? request.getParameter(Constants.VENDOR_BILLING_COUNTY) : "");
                addressMap.put(Constants.VENDOR_BILLING_CITY, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_CITY)) ? request.getParameter(Constants.VENDOR_BILLING_CITY) : "");
                addressMap.put(Constants.VENDOR_BILLING_POSTAL, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_POSTAL)) ? request.getParameter(Constants.VENDOR_BILLING_POSTAL) : "");
                addressMap.put(Constants.VENDOR_BILLING_EMAIL, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_EMAIL)) ? request.getParameter(Constants.VENDOR_BILLING_EMAIL) : "");
                addressMap.put(Constants.VENDOR_BILLING_FAX, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_FAX)) ? request.getParameter(Constants.VENDOR_BILLING_FAX) : "");
                addressMap.put(Constants.VENDOR_BILLING_MOBILE, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_MOBILE)) ? request.getParameter(Constants.VENDOR_BILLING_MOBILE) : "");
                addressMap.put(Constants.VENDOR_BILLING_PHONE, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_PHONE)) ? request.getParameter(Constants.VENDOR_BILLING_PHONE) : "");
                addressMap.put(Constants.VENDOR_BILLING_RECIPIENT_NAME, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_RECIPIENT_NAME)) ? request.getParameter(Constants.VENDOR_BILLING_RECIPIENT_NAME) : "");
                addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_CONTACT_PERSON)) ? request.getParameter(Constants.VENDOR_BILLING_CONTACT_PERSON) : "");
                addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER)) ? request.getParameter(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER) : "");
                addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION)) ? request.getParameter(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION) : "");
                addressMap.put(Constants.VENDOR_BILLING_WEBSITE, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_WEBSITE)) ? request.getParameter(Constants.VENDOR_BILLING_WEBSITE) : "");
                addressMap.put(Constants.VENDOR_BILLING_ADDRESS_TYPE, !StringUtil.isNullOrEmpty(request.getParameter(Constants.VENDOR_BILLING_ADDRESS_TYPE)) ? request.getParameter(Constants.VENDOR_BILLING_ADDRESS_TYPE) : "");
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
    
    public static Map<String, Object> getAddressParamsJson(JSONObject jobj, boolean isVendorTransaction) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        try {
            addressMap.put(Constants.BILLING_ADDRESS, jobj.optString(Constants.BILLING_ADDRESS,null));
            addressMap.put(Constants.BILLING_COUNTRY, jobj.optString(Constants.BILLING_COUNTRY,null));
            addressMap.put(Constants.BILLING_STATE, jobj.optString(Constants.BILLING_STATE,null));
            addressMap.put(Constants.BILLING_COUNTY, jobj.optString(Constants.BILLING_COUNTY,null));
            addressMap.put(Constants.BILLING_CITY, jobj.optString(Constants.BILLING_CITY,null));
            addressMap.put(Constants.BILLING_POSTAL, jobj.optString(Constants.BILLING_POSTAL,null));
            addressMap.put(Constants.BILLING_EMAIL, jobj.optString(Constants.BILLING_EMAIL,null));
            addressMap.put(Constants.BILLING_FAX, jobj.optString(Constants.BILLING_FAX,null));
            addressMap.put(Constants.BILLING_MOBILE, jobj.optString(Constants.BILLING_MOBILE,null));
            addressMap.put(Constants.BILLING_PHONE, jobj.optString(Constants.BILLING_PHONE,null));
            addressMap.put(Constants.BILLING_RECIPIENT_NAME, jobj.optString(Constants.BILLING_RECIPIENT_NAME,null));
            addressMap.put(Constants.BILLING_CONTACT_PERSON, jobj.optString(Constants.BILLING_CONTACT_PERSON,null));
            addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.BILLING_CONTACT_PERSON_NUMBER,null));
            addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.BILLING_CONTACT_PERSON_DESIGNATION,null));
            addressMap.put(Constants.BILLING_WEBSITE, jobj.optString(Constants.BILLING_WEBSITE,null));
            addressMap.put(Constants.BILLING_ADDRESS_TYPE, jobj.optString(Constants.BILLING_ADDRESS_TYPE,null));
            addressMap.put(Constants.SHIPPING_ADDRESS, jobj.optString(Constants.SHIPPING_ADDRESS,null));
            addressMap.put(Constants.SHIPPING_COUNTRY, jobj.optString(Constants.SHIPPING_COUNTRY,null));
            addressMap.put(Constants.SHIPPING_STATE, jobj.optString(Constants.SHIPPING_STATE,null));
            addressMap.put(Constants.SHIPPING_COUNTY, jobj.optString(Constants.SHIPPING_COUNTY,null));
            addressMap.put(Constants.SHIPPING_CITY, jobj.optString(Constants.SHIPPING_CITY,null));
            addressMap.put(Constants.SHIPPING_EMAIL, jobj.optString(Constants.SHIPPING_EMAIL,null));
            addressMap.put(Constants.SHIPPING_FAX, jobj.optString(Constants.SHIPPING_FAX,null));
            addressMap.put(Constants.SHIPPING_MOBILE, jobj.optString(Constants.SHIPPING_MOBILE,null));
            addressMap.put(Constants.SHIPPING_PHONE, jobj.optString(Constants.SHIPPING_PHONE,null));
            addressMap.put(Constants.SHIPPING_POSTAL, jobj.optString(Constants.SHIPPING_POSTAL,null));
            addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.SHIPPING_CONTACT_PERSON_NUMBER,null));
            addressMap.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION,null));
            addressMap.put(Constants.SHIPPING_WEBSITE, jobj.optString(Constants.SHIPPING_WEBSITE,null));
            addressMap.put(Constants.SHIPPING_CONTACT_PERSON, jobj.optString(Constants.SHIPPING_CONTACT_PERSON,null));
            addressMap.put(Constants.SHIPPING_RECIPIENT_NAME, jobj.optString(Constants.SHIPPING_RECIPIENT_NAME,null));
            addressMap.put(Constants.SHIPPING_ROUTE, jobj.optString(Constants.SHIPPING_ROUTE,null));
            addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, jobj.optString(Constants.SHIPPING_ADDRESS_TYPE,null));
            if (isVendorTransaction) {
                addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS, jobj.optString(Constants.VENDCUST_SHIPPING_ADDRESS,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_STATE, jobj.optString(Constants.VENDCUST_SHIPPING_STATE,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_COUNTRY, jobj.optString(Constants.VENDCUST_SHIPPING_COUNTRY,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_COUNTY, jobj.optString(Constants.VENDCUST_SHIPPING_COUNTY,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_CITY, jobj.optString(Constants.VENDCUST_SHIPPING_CITY,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_EMAIL, jobj.optString(Constants.VENDCUST_SHIPPING_EMAIL,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_FAX, jobj.optString(Constants.VENDCUST_SHIPPING_FAX,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_POSTAL, jobj.optString(Constants.VENDCUST_SHIPPING_POSTAL,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_MOBILE, jobj.optString(Constants.VENDCUST_SHIPPING_MOBILE,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_PHONE, jobj.optString(Constants.VENDCUST_SHIPPING_PHONE,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME, jobj.optString(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_WEBSITE, jobj.optString(Constants.VENDCUST_SHIPPING_WEBSITE,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON, jobj.optString(Constants.VENDCUST_SHIPPING_CONTACT_PERSON,null));
                addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE, jobj.optString(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE,null));
                /**
                 * For Vendor document save Vendor address for INDIA country
                 * if "Show vendors address in purchase document" flag OFF
                 */
                addressMap.put(Constants.VENDOR_BILLING_ADDRESS, jobj.optString(Constants.VENDOR_BILLING_ADDRESS, ""));
                addressMap.put(Constants.VENDOR_BILLING_COUNTRY, jobj.optString(Constants.VENDOR_BILLING_COUNTRY, ""));
                addressMap.put(Constants.VENDOR_BILLING_STATE, jobj.optString(Constants.VENDOR_BILLING_STATE, ""));
                addressMap.put(Constants.VENDOR_BILLING_COUNTY, jobj.optString(Constants.VENDOR_BILLING_COUNTY, ""));
                addressMap.put(Constants.VENDOR_BILLING_CITY, jobj.optString(Constants.VENDOR_BILLING_CITY, ""));
                addressMap.put(Constants.VENDOR_BILLING_POSTAL, jobj.optString(Constants.VENDOR_BILLING_POSTAL, ""));
                addressMap.put(Constants.VENDOR_BILLING_EMAIL, jobj.optString(Constants.VENDOR_BILLING_EMAIL, ""));
                addressMap.put(Constants.VENDOR_BILLING_FAX, jobj.optString(Constants.VENDOR_BILLING_FAX, ""));
                addressMap.put(Constants.VENDOR_BILLING_MOBILE, jobj.optString(Constants.VENDOR_BILLING_MOBILE, ""));
                addressMap.put(Constants.VENDOR_BILLING_PHONE, jobj.optString(Constants.VENDOR_BILLING_PHONE, ""));
                addressMap.put(Constants.VENDOR_BILLING_RECIPIENT_NAME, jobj.optString(Constants.VENDOR_BILLING_RECIPIENT_NAME, ""));
                addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON, jobj.optString(Constants.VENDOR_BILLING_CONTACT_PERSON, ""));
                addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER, ""));
                addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION, ""));
                addressMap.put(Constants.VENDOR_BILLING_WEBSITE, jobj.optString(Constants.VENDOR_BILLING_WEBSITE, ""));
                addressMap.put(Constants.VENDOR_BILLING_ADDRESS_TYPE, jobj.optString(Constants.VENDOR_BILLING_ADDRESS_TYPE, ""));
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
    
    /**
     * Description: This Method is used to get Address details while PO is Created with SO
     * @param jobj
     * @param isVendorTransaction
     * @return 
     */
    
    public static Map<String, Object> getCustomerShippingAddressParamsJson(JSONObject jobj, boolean isVendorTransaction) {
        Map<String, Object> addressMap = new HashMap<>();
        try {
            addressMap = getAddressParamsJson(jobj, isVendorTransaction);
            addressMap.put(Constants.CUSTOMER_SHIPPING_ADDRESS, jobj.optString(Constants.CUSTOMER_SHIPPING_ADDRESS, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_STATE, jobj.optString(Constants.CUSTOMER_SHIPPING_STATE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_COUNTRY, jobj.optString(Constants.CUSTOMER_SHIPPING_COUNTRY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_COUNTY, jobj.optString(Constants.CUSTOMER_SHIPPING_COUNTY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CITY, jobj.optString(Constants.CUSTOMER_SHIPPING_CITY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_EMAIL, jobj.optString(Constants.CUSTOMER_SHIPPING_EMAIL, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_FAX, jobj.optString(Constants.CUSTOMER_SHIPPING_FAX, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_POSTAL, jobj.optString(Constants.CUSTOMER_SHIPPING_POSTAL, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_MOBILE, jobj.optString(Constants.CUSTOMER_SHIPPING_MOBILE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_PHONE, jobj.optString(Constants.CUSTOMER_SHIPPING_PHONE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, jobj.optString(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_WEBSITE, jobj.optString(Constants.CUSTOMER_SHIPPING_WEBSITE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, jobj.optString(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_ROUTE, jobj.optString(Constants.CUSTOMER_SHIPPING_ROUTE, null));
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
    
    public static Map<String, Object> getDefaultVendorCompanyAddressParams(String vendorid, String companyid, AccountingHandlerDAO accountingHandlerDAOobj) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        try {
            HashMap<String, Object> companyAddrRequestParams = new HashMap<String, Object>();
            companyAddrRequestParams.put("companyid", companyid);
            companyAddrRequestParams.put("isDefaultAddress", true);
            KwlReturnObject result = accountingHandlerDAOobj.getCompanyAddressDetails(companyAddrRequestParams);
            List<CompanyAddressDetails>  details= result.getEntityList();
            for (CompanyAddressDetails cad : details) {
                if (cad.isIsBillingAddress()) {
                    addressMap.put(Constants.BILLING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.BILLING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.BILLING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.BILLING_STATE, cad.getState());
                    addressMap.put(Constants.BILLING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.BILLING_CITY, cad.getCity());
                    addressMap.put(Constants.BILLING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.BILLING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.BILLING_FAX, cad.getFax());
                    addressMap.put(Constants.BILLING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.BILLING_PHONE, cad.getPhone());
                    addressMap.put(Constants.BILLING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.BILLING_WEBSITE, cad.getWebsite());
                } else {
                    addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.SHIPPING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.SHIPPING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.SHIPPING_STATE, cad.getState());
                    addressMap.put(Constants.SHIPPING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.SHIPPING_CITY, cad.getCity());
                    addressMap.put(Constants.SHIPPING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.SHIPPING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.SHIPPING_FAX, cad.getFax());
                    addressMap.put(Constants.SHIPPING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.SHIPPING_PHONE, cad.getPhone());
                    addressMap.put(Constants.SHIPPING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.SHIPPING_WEBSITE, cad.getWebsite());
                }
            }

            HashMap<String, Object> vendAddrRequestParams = new HashMap<String, Object>();
            vendAddrRequestParams.put("companyid", companyid);
            vendAddrRequestParams.put("vendorid", vendorid);
            vendAddrRequestParams.put("isDefaultAddress", true);
            vendAddrRequestParams.put("isBillingAddress", false);
            result = accountingHandlerDAOobj.getVendorAddressDetails(vendAddrRequestParams);
            if (!result.getEntityList().isEmpty()) {
                VendorAddressDetails vendAddr = (VendorAddressDetails) result.getEntityList().get(0);
                if (vendAddr != null) {
                    addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE, vendAddr.getAliasName() == null ? "" : vendAddr.getAliasName());
                    addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS, vendAddr.getAddress() == null ? "" : vendAddr.getAddress());
                    addressMap.put(Constants.VENDCUST_SHIPPING_COUNTRY, vendAddr.getCountry() == null ? "" : vendAddr.getCountry());
                    addressMap.put(Constants.VENDCUST_SHIPPING_STATE, vendAddr.getState() == null ? "" : vendAddr.getState());
                    addressMap.put(Constants.VENDCUST_SHIPPING_COUNTY, vendAddr.getCounty() == null ? "" : vendAddr.getCounty());
                    addressMap.put(Constants.VENDCUST_SHIPPING_CITY, vendAddr.getCity() == null ? "" : vendAddr.getCity());
                    addressMap.put(Constants.VENDCUST_SHIPPING_EMAIL, vendAddr.getEmailID() == null ? "" : vendAddr.getEmailID());
                    addressMap.put(Constants.VENDCUST_SHIPPING_FAX, vendAddr.getFax() == null ? "" : vendAddr.getFax());
                    addressMap.put(Constants.VENDCUST_SHIPPING_MOBILE, vendAddr.getMobileNumber() == null ? "" : vendAddr.getMobileNumber());
                    addressMap.put(Constants.VENDCUST_SHIPPING_PHONE, vendAddr.getPhone() == null ? "" : vendAddr.getPhone());
                    addressMap.put(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME, vendAddr.getPhone() == null ? "" : vendAddr.getRecipientName());
                    addressMap.put(Constants.VENDCUST_SHIPPING_POSTAL, vendAddr.getPostalCode() == null ? "" : vendAddr.getPostalCode());
                    addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber() == null ? "" : vendAddr.getContactPersonNumber());
                    addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION, vendAddr.getContactPersonDesignation() == null ? "" : vendAddr.getContactPersonDesignation());
                    addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON, vendAddr.getContactPerson() == null ? "" : vendAddr.getContactPerson());
                    addressMap.put(Constants.VENDCUST_SHIPPING_WEBSITE, vendAddr.getWebsite() == null ? "" : vendAddr.getWebsite());
                }
            }
            /**
             * Get Vendor Default address in separate key's for INDIA country and if not isIsAddressFromVendorMaster
             */
            KwlReturnObject kwlCompanyPref = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company companyObj = null;
            String countryid = "";
            if (kwlCompanyPref != null && kwlCompanyPref.getEntityList() != null && !kwlCompanyPref.getEntityList().isEmpty()) {
                companyObj = (Company) kwlCompanyPref.getEntityList().get(0);
                if (companyObj != null && companyObj.getCountry() != null) {
                    countryid = companyObj.getCountry().getID();
                }
            }
            if (!StringUtil.isNullOrEmpty(countryid) && Integer.valueOf(countryid) == Constants.indian_country_id) {
                KwlReturnObject kwlExtraCompanyPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences extraPreferences = null;
                if (kwlExtraCompanyPref != null && kwlExtraCompanyPref.getEntityList() != null && !kwlExtraCompanyPref.getEntityList().isEmpty()) {
                    extraPreferences = (ExtraCompanyPreferences) kwlExtraCompanyPref.getEntityList().get(0);
                }
                if (extraPreferences != null && !extraPreferences.isIsAddressFromVendorMaster()) {
                    vendAddrRequestParams = new HashMap<String, Object>();
                    vendAddrRequestParams.put("companyid", companyid);
                    vendAddrRequestParams.put("vendorid", vendorid);
                    vendAddrRequestParams.put("isDefaultAddress", true);
                    vendAddrRequestParams.put("isBillingAddress", true);
                    result = accountingHandlerDAOobj.getVendorAddressDetails(vendAddrRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        VendorAddressDetails vendAddr = (VendorAddressDetails) result.getEntityList().get(0);
                        if (vendAddr != null) {
                            addressMap.put(Constants.VENDOR_BILLING_ADDRESS_TYPE, vendAddr.getAliasName() == null ? "" : vendAddr.getAliasName());
                            addressMap.put(Constants.VENDOR_BILLING_ADDRESS, vendAddr.getAddress() == null ? "" : vendAddr.getAddress());
                            addressMap.put(Constants.VENDOR_BILLING_COUNTRY, vendAddr.getCountry() == null ? "" : vendAddr.getCountry());
                            addressMap.put(Constants.VENDOR_BILLING_STATE, vendAddr.getState() == null ? "" : vendAddr.getState());
                            addressMap.put(Constants.VENDOR_BILLING_COUNTY, vendAddr.getCounty() == null ? "" : vendAddr.getCounty());
                            addressMap.put(Constants.VENDOR_BILLING_CITY, vendAddr.getCity() == null ? "" : vendAddr.getCity());
                            addressMap.put(Constants.VENDOR_BILLING_EMAIL, vendAddr.getEmailID() == null ? "" : vendAddr.getEmailID());
                            addressMap.put(Constants.VENDOR_BILLING_FAX, vendAddr.getFax() == null ? "" : vendAddr.getFax());
                            addressMap.put(Constants.VENDOR_BILLING_MOBILE, vendAddr.getMobileNumber() == null ? "" : vendAddr.getMobileNumber());
                            addressMap.put(Constants.VENDOR_BILLING_PHONE, vendAddr.getPhone() == null ? "" : vendAddr.getPhone());
                            addressMap.put(Constants.VENDOR_BILLING_RECIPIENT_NAME, vendAddr.getPhone() == null ? "" : vendAddr.getRecipientName());
                            addressMap.put(Constants.VENDOR_BILLING_POSTAL, vendAddr.getPostalCode() == null ? "" : vendAddr.getPostalCode());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber() == null ? "" : vendAddr.getContactPersonNumber());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION, vendAddr.getContactPersonDesignation() == null ? "" : vendAddr.getContactPersonDesignation());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON, vendAddr.getContactPerson() == null ? "" : vendAddr.getContactPerson());
                            addressMap.put(Constants.VENDOR_BILLING_WEBSITE, vendAddr.getWebsite() == null ? "" : vendAddr.getWebsite());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
    
    
    /* --Get default addresses for dropship type PO-------*/
        public static Map<String, Object> getDefaultVendorBillingAddressParamsForDropShipTypeDoc(String vendorid, String companyid, AccountingHandlerDAO accountingHandlerDAOobj,JSONObject jobj) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        try {
            HashMap<String, Object> companyAddrRequestParams = new HashMap<String, Object>();
            companyAddrRequestParams.put("companyid", companyid);
            companyAddrRequestParams.put("isDefaultAddress", true);
           
            /*----Company Billing Address---------- */
            KwlReturnObject result = accountingHandlerDAOobj.getCompanyAddressDetails(companyAddrRequestParams);
            List<CompanyAddressDetails>  details= result.getEntityList();
            for (CompanyAddressDetails cad : details) {
                if (cad.isIsBillingAddress()) {
                    addressMap.put(Constants.BILLING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.BILLING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.BILLING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.BILLING_STATE, cad.getState());
                    addressMap.put(Constants.BILLING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.BILLING_CITY, cad.getCity());
                    addressMap.put(Constants.BILLING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.BILLING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.BILLING_FAX, cad.getFax());
                    addressMap.put(Constants.BILLING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.BILLING_PHONE, cad.getPhone());
                    addressMap.put(Constants.BILLING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.BILLING_WEBSITE, cad.getWebsite());
                } 
            }

            
            /*----Vendor Billing Address----------- */
            HashMap<String, Object> vendAddrRequestParams = new HashMap<String, Object>();
            vendAddrRequestParams.put("companyid", companyid);
            vendAddrRequestParams.put("vendorid", vendorid);
            vendAddrRequestParams.put("isDefaultAddress", true);
            vendAddrRequestParams.put("isBillingAddress", false);
            result = accountingHandlerDAOobj.getVendorAddressDetails(vendAddrRequestParams);
            if (!result.getEntityList().isEmpty()) {
                VendorAddressDetails vendAddr = (VendorAddressDetails) result.getEntityList().get(0);
                if (vendAddr != null) {
                 addressMap.put(Constants.DropShip_BILLING_ADDRESS_TYPE, vendAddr.getAliasName());
                    addressMap.put(Constants.DropShip_BILLING_ADDRESS, vendAddr.getAddress());
                    addressMap.put(Constants.DropShip_BILLING_COUNTRY, vendAddr.getCountry());
                    addressMap.put(Constants.DropShip_BILLING_STATE, vendAddr.getState());
                    addressMap.put(Constants.DropShip_BILLING_COUNTY, vendAddr.getCounty());
                    addressMap.put(Constants.DropShip_BILLING_CITY, vendAddr.getCity());
                    addressMap.put(Constants.DropShip_BILLING_POSTAL, vendAddr.getPostalCode());
                    addressMap.put(Constants.DropShip_BILLING_EMAIL, vendAddr.getEmailID());
                    addressMap.put(Constants.DropShip_BILLING_FAX, vendAddr.getFax());
                    addressMap.put(Constants.DropShip_BILLING_MOBILE, vendAddr.getMobileNumber());
                    addressMap.put(Constants.DropShip_BILLING_PHONE, vendAddr.getPhone());
                    addressMap.put(Constants.DropShip_BILLING_RECIPIENT_NAME, vendAddr.getRecipientName());
                    addressMap.put(Constants.DropShip_BILLING_CONTACT_PERSON, vendAddr.getContactPerson());
                    addressMap.put(Constants.DropShip_BILLING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber());
                    addressMap.put(Constants.DropShip_BILLING_CONTACT_PERSON_DESIGNATION, vendAddr.getContactPersonDesignation());
                    addressMap.put(Constants.DropShip_BILLING_WEBSITE, vendAddr.getWebsite());
                    /**
                     * Country ID india and drop ship activated and Show vendor address check OFF
                     * then add vendor Billing address in separate fields
                     */
                    KwlReturnObject kwlCompanyPref = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                    Company companyObj = null;
                    String countryid = "";
                    if (kwlCompanyPref != null && kwlCompanyPref.getEntityList() != null && !kwlCompanyPref.getEntityList().isEmpty()) {
                        companyObj = (Company) kwlCompanyPref.getEntityList().get(0);
                        if (companyObj != null && companyObj.getCountry() != null) {
                            countryid = companyObj.getCountry().getID();
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(countryid) && Integer.valueOf(countryid) == Constants.indian_country_id) {
                        KwlReturnObject kwlExtraCompanyPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                        ExtraCompanyPreferences extraPreferences = null;
                        if (kwlExtraCompanyPref != null && kwlExtraCompanyPref.getEntityList() != null && !kwlExtraCompanyPref.getEntityList().isEmpty()) {
                            extraPreferences = (ExtraCompanyPreferences) kwlExtraCompanyPref.getEntityList().get(0);
                        }
                        if (extraPreferences != null && !extraPreferences.isIsAddressFromVendorMaster()) {
                            addressMap.put(Constants.VENDOR_BILLING_ADDRESS_TYPE, vendAddr.getAliasName() == null ? "" : vendAddr.getAliasName());
                            addressMap.put(Constants.VENDOR_BILLING_ADDRESS, vendAddr.getAddress() == null ? "" : vendAddr.getAddress());
                            addressMap.put(Constants.VENDOR_BILLING_COUNTRY, vendAddr.getCountry() == null ? "" : vendAddr.getCountry());
                            addressMap.put(Constants.VENDOR_BILLING_STATE, vendAddr.getState() == null ? "" : vendAddr.getState());
                            addressMap.put(Constants.VENDOR_BILLING_COUNTY, vendAddr.getCounty() == null ? "" : vendAddr.getCounty());
                            addressMap.put(Constants.VENDOR_BILLING_CITY, vendAddr.getCity() == null ? "" : vendAddr.getCity());
                            addressMap.put(Constants.VENDOR_BILLING_POSTAL, vendAddr.getPostalCode() == null ? "" : vendAddr.getPostalCode());
                            addressMap.put(Constants.VENDOR_BILLING_EMAIL, vendAddr.getEmailID() == null ? "" : vendAddr.getEmailID());
                            addressMap.put(Constants.VENDOR_BILLING_FAX, vendAddr.getFax() == null ? "" : vendAddr.getFax());
                            addressMap.put(Constants.VENDOR_BILLING_MOBILE, vendAddr.getMobileNumber() == null ? "" : vendAddr.getMobileNumber());
                            addressMap.put(Constants.VENDOR_BILLING_PHONE, vendAddr.getPhone() == null ? "" : vendAddr.getPhone());
                            addressMap.put(Constants.VENDOR_BILLING_RECIPIENT_NAME, vendAddr.getRecipientName() == null ? "" : vendAddr.getRecipientName());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON, vendAddr.getContactPerson() == null ? "" : vendAddr.getContactPerson());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber() == null ? "" : vendAddr.getContactPersonNumber());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION, vendAddr.getContactPersonDesignation() == null ? "" : vendAddr.getContactPersonDesignation());
                            addressMap.put(Constants.VENDOR_BILLING_WEBSITE, vendAddr.getWebsite() == null ? "" : vendAddr.getWebsite());
                            vendAddrRequestParams = new HashMap<String, Object>();
                            vendAddrRequestParams.put("companyid", companyid);
                            vendAddrRequestParams.put("vendorid", vendorid);
                            vendAddrRequestParams.put("isDefaultAddress", true);
                            vendAddrRequestParams.put("isBillingAddress", false);
                            result = accountingHandlerDAOobj.getVendorAddressDetails(vendAddrRequestParams);
                            if (!result.getEntityList().isEmpty()) {
                                vendAddr = (VendorAddressDetails) result.getEntityList().get(0);
                                if (vendAddr != null) {
                                    addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE, vendAddr.getAliasName() == null ? "" : vendAddr.getAliasName());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS, vendAddr.getAddress() == null ? "" : vendAddr.getAddress());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_COUNTRY, vendAddr.getCountry() == null ? "" : vendAddr.getCountry());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_STATE, vendAddr.getState() == null ? "" : vendAddr.getState());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_COUNTY, vendAddr.getCounty() == null ? "" : vendAddr.getCounty());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_CITY, vendAddr.getCity() == null ? "" : vendAddr.getCity());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_EMAIL, vendAddr.getEmailID() == null ? "" : vendAddr.getEmailID());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_FAX, vendAddr.getFax() == null ? "" : vendAddr.getFax());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_MOBILE, vendAddr.getMobileNumber() == null ? "" : vendAddr.getMobileNumber());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_PHONE, vendAddr.getPhone() == null ? "" : vendAddr.getPhone());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME, vendAddr.getPhone() == null ? "" : vendAddr.getRecipientName());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_POSTAL, vendAddr.getPostalCode() == null ? "" : vendAddr.getPostalCode());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER, vendAddr.getContactPersonNumber() == null ? "" : vendAddr.getContactPersonNumber());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION, vendAddr.getContactPersonDesignation() == null ? "" : vendAddr.getContactPersonDesignation());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON, vendAddr.getContactPerson() == null ? "" : vendAddr.getContactPerson());
                                    addressMap.put(Constants.VENDCUST_SHIPPING_WEBSITE, vendAddr.getWebsite() == null ? "" : vendAddr.getWebsite());
                                }
                            }
                        }
                    }
                }
            }
            
            /*------Customer Shipping Addres------------  */
            addressMap.put(Constants.CUSTOMER_SHIPPING_ADDRESS, jobj.optString(Constants.CUSTOMER_SHIPPING_ADDRESS, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_STATE, jobj.optString(Constants.CUSTOMER_SHIPPING_STATE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_COUNTRY, jobj.optString(Constants.CUSTOMER_SHIPPING_COUNTRY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_COUNTY, jobj.optString(Constants.CUSTOMER_SHIPPING_COUNTY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CITY, jobj.optString(Constants.CUSTOMER_SHIPPING_CITY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_EMAIL, jobj.optString(Constants.CUSTOMER_SHIPPING_EMAIL, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_FAX, jobj.optString(Constants.CUSTOMER_SHIPPING_FAX, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_POSTAL, jobj.optString(Constants.CUSTOMER_SHIPPING_POSTAL, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_MOBILE, jobj.optString(Constants.CUSTOMER_SHIPPING_MOBILE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_PHONE, jobj.optString(Constants.CUSTOMER_SHIPPING_PHONE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, jobj.optString(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_WEBSITE, jobj.optString(Constants.CUSTOMER_SHIPPING_WEBSITE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, jobj.optString(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_ROUTE, jobj.optString(Constants.CUSTOMER_SHIPPING_ROUTE, null));
            
            
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
    
        
        
        
           
        public static Map<String, Object> getVendorBillingAddressParamsForDropShipTypeDoc(JSONObject jobj) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        try {
  
            /*-------Company billing address--------  */
             addressMap.put(Constants.BILLING_ADDRESS, jobj.optString(Constants.BILLING_ADDRESS,null));
            addressMap.put(Constants.BILLING_COUNTRY, jobj.optString(Constants.BILLING_COUNTRY,null));
            addressMap.put(Constants.BILLING_STATE, jobj.optString(Constants.BILLING_STATE,null));
            addressMap.put(Constants.BILLING_COUNTY, jobj.optString(Constants.BILLING_COUNTY,null));
            addressMap.put(Constants.BILLING_CITY, jobj.optString(Constants.BILLING_CITY,null));
            addressMap.put(Constants.BILLING_POSTAL, jobj.optString(Constants.BILLING_POSTAL,null));
            addressMap.put(Constants.BILLING_EMAIL, jobj.optString(Constants.BILLING_EMAIL,null));
            addressMap.put(Constants.BILLING_FAX, jobj.optString(Constants.BILLING_FAX,null));
            addressMap.put(Constants.BILLING_MOBILE, jobj.optString(Constants.BILLING_MOBILE,null));
            addressMap.put(Constants.BILLING_PHONE, jobj.optString(Constants.BILLING_PHONE,null));
            addressMap.put(Constants.BILLING_RECIPIENT_NAME, jobj.optString(Constants.BILLING_RECIPIENT_NAME,null));
            addressMap.put(Constants.BILLING_CONTACT_PERSON, jobj.optString(Constants.BILLING_CONTACT_PERSON,null));
            addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.BILLING_CONTACT_PERSON_NUMBER,null));
            addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.BILLING_CONTACT_PERSON_DESIGNATION,null));
            addressMap.put(Constants.BILLING_WEBSITE, jobj.optString(Constants.BILLING_WEBSITE,null));
            addressMap.put(Constants.BILLING_ADDRESS_TYPE, jobj.optString(Constants.BILLING_ADDRESS_TYPE,null));
           
      

            
        
            /*-------Vendor billing address--------  */
             addressMap.put(Constants.DropShip_BILLING_ADDRESS, jobj.optString(Constants.DropShip_BILLING_ADDRESS,null));
            addressMap.put(Constants.DropShip_BILLING_COUNTRY, jobj.optString(Constants.DropShip_BILLING_COUNTRY,null));
            addressMap.put(Constants.DropShip_BILLING_STATE, jobj.optString(Constants.DropShip_BILLING_STATE,null));
            addressMap.put(Constants.DropShip_BILLING_COUNTY, jobj.optString(Constants.DropShip_BILLING_COUNTY,null));
            addressMap.put(Constants.DropShip_BILLING_CITY, jobj.optString(Constants.DropShip_BILLING_CITY,null));
            addressMap.put(Constants.DropShip_BILLING_POSTAL, jobj.optString(Constants.DropShip_BILLING_POSTAL,null));
            addressMap.put(Constants.DropShip_BILLING_EMAIL, jobj.optString(Constants.DropShip_BILLING_EMAIL,null));
            addressMap.put(Constants.DropShip_BILLING_FAX, jobj.optString(Constants.DropShip_BILLING_FAX,null));
            addressMap.put(Constants.DropShip_BILLING_MOBILE, jobj.optString(Constants.DropShip_BILLING_MOBILE,null));
            addressMap.put(Constants.DropShip_BILLING_PHONE, jobj.optString(Constants.DropShip_BILLING_PHONE,null));
            addressMap.put(Constants.DropShip_BILLING_RECIPIENT_NAME, jobj.optString(Constants.DropShip_BILLING_RECIPIENT_NAME,null));
            addressMap.put(Constants.DropShip_BILLING_CONTACT_PERSON, jobj.optString(Constants.DropShip_BILLING_CONTACT_PERSON,null));
            addressMap.put(Constants.DropShip_BILLING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.DropShip_BILLING_CONTACT_PERSON_NUMBER,null));
            addressMap.put(Constants.DropShip_BILLING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.DropShip_BILLING_CONTACT_PERSON_DESIGNATION,null));
            addressMap.put(Constants.DropShip_BILLING_WEBSITE, jobj.optString(Constants.DropShip_BILLING_WEBSITE,null));
            addressMap.put(Constants.DropShip_BILLING_ADDRESS_TYPE, jobj.optString(Constants.DropShip_BILLING_ADDRESS_TYPE,null));
             
            /**
             * If drop ship document then add vendor billing address in new fields 
             */
            addressMap.put(Constants.VENDOR_BILLING_ADDRESS, jobj.optString(Constants.DropShip_BILLING_ADDRESS,""));
            addressMap.put(Constants.VENDOR_BILLING_COUNTRY, jobj.optString(Constants.DropShip_BILLING_COUNTRY,""));
            addressMap.put(Constants.VENDOR_BILLING_STATE, jobj.optString(Constants.DropShip_BILLING_STATE,""));
            addressMap.put(Constants.VENDOR_BILLING_COUNTY, jobj.optString(Constants.DropShip_BILLING_COUNTY,""));
            addressMap.put(Constants.VENDOR_BILLING_CITY, jobj.optString(Constants.DropShip_BILLING_CITY,""));
            addressMap.put(Constants.VENDOR_BILLING_POSTAL, jobj.optString(Constants.DropShip_BILLING_POSTAL,""));
            addressMap.put(Constants.VENDOR_BILLING_EMAIL, jobj.optString(Constants.DropShip_BILLING_EMAIL,""));
            addressMap.put(Constants.VENDOR_BILLING_FAX, jobj.optString(Constants.DropShip_BILLING_FAX,""));
            addressMap.put(Constants.VENDOR_BILLING_MOBILE, jobj.optString(Constants.DropShip_BILLING_MOBILE,""));
            addressMap.put(Constants.VENDOR_BILLING_PHONE, jobj.optString(Constants.DropShip_BILLING_PHONE,""));
            addressMap.put(Constants.VENDOR_BILLING_RECIPIENT_NAME, jobj.optString(Constants.DropShip_BILLING_RECIPIENT_NAME, ""));
            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON, jobj.optString(Constants.DropShip_BILLING_CONTACT_PERSON, ""));
            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.DropShip_BILLING_CONTACT_PERSON_NUMBER, ""));
            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.DropShip_BILLING_CONTACT_PERSON_DESIGNATION, ""));
            addressMap.put(Constants.VENDOR_BILLING_WEBSITE, jobj.optString(Constants.DropShip_BILLING_WEBSITE, ""));
            addressMap.put(Constants.VENDOR_BILLING_ADDRESS_TYPE, jobj.optString(Constants.DropShip_BILLING_ADDRESS_TYPE, ""));
            
            /*------Customer Shipping Addres------------  */
            addressMap.put(Constants.CUSTOMER_SHIPPING_ADDRESS, jobj.optString(Constants.CUSTOMER_SHIPPING_ADDRESS, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_STATE, jobj.optString(Constants.CUSTOMER_SHIPPING_STATE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_COUNTRY, jobj.optString(Constants.CUSTOMER_SHIPPING_COUNTRY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_COUNTY, jobj.optString(Constants.CUSTOMER_SHIPPING_COUNTY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CITY, jobj.optString(Constants.CUSTOMER_SHIPPING_CITY, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_EMAIL, jobj.optString(Constants.CUSTOMER_SHIPPING_EMAIL, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_FAX, jobj.optString(Constants.CUSTOMER_SHIPPING_FAX, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_POSTAL, jobj.optString(Constants.CUSTOMER_SHIPPING_POSTAL, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_MOBILE, jobj.optString(Constants.CUSTOMER_SHIPPING_MOBILE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_PHONE, jobj.optString(Constants.CUSTOMER_SHIPPING_PHONE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, jobj.optString(Constants.CUSTOMER_SHIPPING_RECIPIENT_NAME, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_NUMBER, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON_DESIGNATION, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_WEBSITE, jobj.optString(Constants.CUSTOMER_SHIPPING_WEBSITE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, jobj.optString(Constants.CUSTOMER_SHIPPING_CONTACT_PERSON, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, jobj.optString(Constants.CUSTOMER_SHIPPING_ADDRESS_TYPE, null));
            addressMap.put(Constants.CUSTOMER_SHIPPING_ROUTE, jobj.optString(Constants.CUSTOMER_SHIPPING_ROUTE, null));
            
            
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
        
        
    
    public static Map<String, Object> getDefaultVendorAddressParams(String vendorid, String companyid, AccountingHandlerDAO accountingHandlerDAOobj) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        try {
            HashMap<String, Object> vendAddrRequestParams = new HashMap<String, Object>();
            vendAddrRequestParams.put("companyid", companyid);
            vendAddrRequestParams.put("vendorid", vendorid);
            vendAddrRequestParams.put("isDefaultAddress", true);           
            KwlReturnObject result = accountingHandlerDAOobj.getVendorAddressDetails(vendAddrRequestParams);
            
            List<VendorAddressDetails>  details= result.getEntityList();
            for (VendorAddressDetails cad : details) {
                if (cad.isIsBillingAddress()) {
                    addressMap.put(Constants.BILLING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.BILLING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.BILLING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.BILLING_STATE, cad.getState());
                    addressMap.put(Constants.BILLING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.BILLING_CITY, cad.getCity());
                    addressMap.put(Constants.BILLING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.BILLING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.BILLING_FAX, cad.getFax());
                    addressMap.put(Constants.BILLING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.BILLING_PHONE, cad.getPhone());
                    addressMap.put(Constants.BILLING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.BILLING_WEBSITE, cad.getWebsite());
                } else {
                    addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.SHIPPING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.SHIPPING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.SHIPPING_STATE, cad.getState());
                    addressMap.put(Constants.SHIPPING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.SHIPPING_CITY, cad.getCity());
                    addressMap.put(Constants.SHIPPING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.SHIPPING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.SHIPPING_FAX, cad.getFax());
                    addressMap.put(Constants.SHIPPING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.SHIPPING_PHONE, cad.getPhone());
                    addressMap.put(Constants.SHIPPING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.SHIPPING_WEBSITE, cad.getWebsite());
                }
                /**
                 * Country ID india and drop ship activated and Show vendor
                 * address check OFF then add vendor Billing address in separate
                 * fields
                 */
                KwlReturnObject kwlCompanyPref = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company companyObj = null;
                String countryid = "";
                if (kwlCompanyPref != null && kwlCompanyPref.getEntityList() != null && !kwlCompanyPref.getEntityList().isEmpty()) {
                    companyObj = (Company) kwlCompanyPref.getEntityList().get(0);
                    if (companyObj != null && companyObj.getCountry() != null) {
                        countryid = companyObj.getCountry().getID();
                    }
                }
                if (!StringUtil.isNullOrEmpty(countryid) && Integer.valueOf(countryid) == Constants.indian_country_id) {
                    KwlReturnObject kwlExtraCompanyPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                    ExtraCompanyPreferences extraPreferences = null;
                    if (kwlExtraCompanyPref != null && kwlExtraCompanyPref.getEntityList() != null && !kwlExtraCompanyPref.getEntityList().isEmpty()) {
                        extraPreferences = (ExtraCompanyPreferences) kwlExtraCompanyPref.getEntityList().get(0);
                    }
                    if (extraPreferences != null && !extraPreferences.isIsAddressFromVendorMaster()) {
                        if (cad.isIsBillingAddress()) {
                            addressMap.put(Constants.VENDOR_BILLING_ADDRESS_TYPE, cad.getAliasName());
                            addressMap.put(Constants.VENDOR_BILLING_ADDRESS, cad.getAddress());
                            addressMap.put(Constants.VENDOR_BILLING_COUNTRY, cad.getCountry());
                            addressMap.put(Constants.VENDOR_BILLING_STATE, cad.getState());
                            addressMap.put(Constants.VENDOR_BILLING_COUNTY, cad.getCounty());
                            addressMap.put(Constants.VENDOR_BILLING_CITY, cad.getCity());
                            addressMap.put(Constants.VENDOR_BILLING_POSTAL, cad.getPostalCode());
                            addressMap.put(Constants.VENDOR_BILLING_EMAIL, cad.getEmailID());
                            addressMap.put(Constants.VENDOR_BILLING_FAX, cad.getFax());
                            addressMap.put(Constants.VENDOR_BILLING_MOBILE, cad.getMobileNumber());
                            addressMap.put(Constants.VENDOR_BILLING_PHONE, cad.getPhone());
                            addressMap.put(Constants.VENDOR_BILLING_RECIPIENT_NAME, cad.getRecipientName());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON, cad.getContactPerson());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                            addressMap.put(Constants.VENDOR_BILLING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                            addressMap.put(Constants.VENDOR_BILLING_WEBSITE, cad.getWebsite());
                        } else {
                            addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE, cad.getAliasName() == null ? "" : cad.getAliasName());
                            addressMap.put(Constants.VENDCUST_SHIPPING_ADDRESS, cad.getAddress() == null ? "" : cad.getAddress());
                            addressMap.put(Constants.VENDCUST_SHIPPING_COUNTRY, cad.getCountry() == null ? "" : cad.getCountry());
                            addressMap.put(Constants.VENDCUST_SHIPPING_STATE, cad.getState() == null ? "" : cad.getState());
                            addressMap.put(Constants.VENDCUST_SHIPPING_COUNTY, cad.getCounty() == null ? "" : cad.getCounty());
                            addressMap.put(Constants.VENDCUST_SHIPPING_CITY, cad.getCity() == null ? "" : cad.getCity());
                            addressMap.put(Constants.VENDCUST_SHIPPING_EMAIL, cad.getEmailID() == null ? "" : cad.getEmailID());
                            addressMap.put(Constants.VENDCUST_SHIPPING_FAX, cad.getFax() == null ? "" : cad.getFax());
                            addressMap.put(Constants.VENDCUST_SHIPPING_MOBILE, cad.getMobileNumber() == null ? "" : cad.getMobileNumber());
                            addressMap.put(Constants.VENDCUST_SHIPPING_PHONE, cad.getPhone() == null ? "" : cad.getPhone());
                            addressMap.put(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME, cad.getPhone() == null ? "" : cad.getRecipientName());
                            addressMap.put(Constants.VENDCUST_SHIPPING_POSTAL, cad.getPostalCode() == null ? "" : cad.getPostalCode());
                            addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber() == null ? "" : cad.getContactPersonNumber());
                            addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation() == null ? "" : cad.getContactPersonDesignation());
                            addressMap.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON, cad.getContactPerson() == null ? "" : cad.getContactPerson());
                            addressMap.put(Constants.VENDCUST_SHIPPING_WEBSITE, cad.getWebsite() == null ? "" : cad.getWebsite());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
    
    public static Map<String, Object> getDefaultCustomerAddressParams(String customerid, String companyid, AccountingHandlerDAO accountingHandlerDAOobj) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        try {
            HashMap<String, Object> custAddrRequestParams = new HashMap<String, Object>();
            custAddrRequestParams.put("companyid", companyid);
            custAddrRequestParams.put("customerid", customerid);
            custAddrRequestParams.put("isDefaultAddress", true);           
            KwlReturnObject result = accountingHandlerDAOobj.getCustomerAddressDetails(custAddrRequestParams);
            
            List<CustomerAddressDetails>  details= result.getEntityList();
            for (CustomerAddressDetails cad : details) {
                if (cad.isIsBillingAddress()) {
                    addressMap.put(Constants.BILLING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.BILLING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.BILLING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.BILLING_STATE, cad.getState());
                    addressMap.put(Constants.BILLING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.BILLING_CITY, cad.getCity());
                    addressMap.put(Constants.BILLING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.BILLING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.BILLING_FAX, cad.getFax());
                    addressMap.put(Constants.BILLING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.BILLING_PHONE, cad.getPhone());
                    addressMap.put(Constants.BILLING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.BILLING_WEBSITE, cad.getWebsite());
                } else {
                    addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.SHIPPING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.SHIPPING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.SHIPPING_STATE, cad.getState());
                    addressMap.put(Constants.SHIPPING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.SHIPPING_CITY, cad.getCity());
                    addressMap.put(Constants.SHIPPING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.SHIPPING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.SHIPPING_FAX, cad.getFax());
                    addressMap.put(Constants.SHIPPING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.SHIPPING_PHONE, cad.getPhone());
                    addressMap.put(Constants.SHIPPING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.SHIPPING_WEBSITE, cad.getWebsite());
                    addressMap.put(Constants.SHIPPING_ROUTE,cad.getShippingRoute());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
  
    public static String getCompanyDefaultBillingAddress(String companyid, AccountingHandlerDAO accountingHandlerDAOobj) {
        String address = "";
        try {
            HashMap<String, Object> companyAddrRequestParams = new HashMap<String, Object>();
            companyAddrRequestParams.put("companyid", companyid);
            companyAddrRequestParams.put("isDefaultAddress", true);
            companyAddrRequestParams.put("isBillingAddress", true);
            KwlReturnObject result = accountingHandlerDAOobj.getCompanyAddressDetails(companyAddrRequestParams);
            if (result!=null && !result.getEntityList().isEmpty()) {
                CompanyAddressDetails details = (CompanyAddressDetails) result.getEntityList().get(0);
                if (details!=null) {
                    String addr = StringUtil.isNullOrEmpty(details.getAddress()) ? "" : details.getAddress();
                    String county = StringUtil.isNullOrEmpty(details.getCounty()) ? "" : ", " + details.getCounty();
                    String city = StringUtil.isNullOrEmpty(details.getCity()) ? "" : ", " + details.getCity();
                    String state = StringUtil.isNullOrEmpty(details.getState()) ? "" : ", " + details.getState();
                    String country = StringUtil.isNullOrEmpty(details.getCountry()) ? "" : ", " + details.getCountry();
                    String postalcode = StringUtil.isNullOrEmpty(details.getPostalCode()) ? "" : " " + details.getPostalCode();
                    String email = StringUtil.isNullOrEmpty(details.getEmailID()) ? "" : "\nEmail : " + details.getEmailID();
                    String website = StringUtil.isNullOrEmpty(details.getWebsite()) ? "" : "\nWebsite : " + details.getWebsite();
                    String phone = StringUtil.isNullOrEmpty(details.getPhone()) ? "" : "\nPhone : " + details.getPhone();
                    String fax = StringUtil.isNullOrEmpty(details.getFax()) ? "" : StringUtil.isNullOrEmpty(phone) ? "\nFax : " + details.getFax() : ", Fax : " + details.getFax();
                    String contractpersonno = StringUtil.isNullOrEmpty(details.getContactPersonNumber()) ? "" : "\nContact Person No : " + details.getContactPersonNumber();
                    String contractpersondesignation = StringUtil.isNullOrEmpty(details.getContactPersonDesignation()) ? "" : "\nContact Person Designation : " + details.getContactPersonDesignation();
                    String mobile = StringUtil.isNullOrEmpty(details.getMobileNumber()) ? "" : "\nMobile : " + details.getMobileNumber();
                    String attn = StringUtil.isNullOrEmpty(details.getContactPerson()) ? "" : "\nAttn. : " + details.getContactPerson();
                    address = addr + county + city + state + country + postalcode + email + website + phone + fax + mobile + contractpersonno + contractpersondesignation + attn;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }
    public static String getCompanyDefaultBillingAddressFasten(String companyid, AccountingHandlerDAO accountingHandlerDAOobj) {//Fasten Hardware
        String address = "";
        try {
            HashMap<String, Object> companyAddrRequestParams = new HashMap<String, Object>();
            companyAddrRequestParams.put("companyid", companyid);
            companyAddrRequestParams.put("isDefaultAddress", true);
            companyAddrRequestParams.put("isBillingAddress", true);
            KwlReturnObject result = accountingHandlerDAOobj.getCompanyAddressDetails(companyAddrRequestParams);
            if (result!=null && !result.getEntityList().isEmpty()) {
                CompanyAddressDetails details = (CompanyAddressDetails) result.getEntityList().get(0);
                if (details!=null) {
                    String addr = StringUtil.isNullOrEmpty(details.getAddress()) ? "" : details.getAddress();
                    String county = StringUtil.isNullOrEmpty(details.getCounty()) ? "" : ", " + details.getCounty();
                    String city = StringUtil.isNullOrEmpty(details.getCity()) ? "" : ", " + details.getCity();
                    String state = StringUtil.isNullOrEmpty(details.getState()) ? "" : ", " + details.getState();
                    String country = StringUtil.isNullOrEmpty(details.getCountry()) ? "" : ", " + details.getCountry();
                    String postalcode = StringUtil.isNullOrEmpty(details.getPostalCode()) ? "" : " " + details.getPostalCode();
                    String phone = StringUtil.isNullOrEmpty(details.getPhone()) ? "" : "\nTel : " + details.getPhone();
                    String fax = StringUtil.isNullOrEmpty(details.getFax()) ? "" : "\nFax : " + details.getFax();
                    String email = StringUtil.isNullOrEmpty(details.getEmailID()) ? "" : "\nEmail : " + details.getEmailID();

                    address = addr + county + city + state + country + postalcode +phone+ fax +email;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }
    public static String getCompanyDefaultShippingAddress(String companyid, AccountingHandlerDAO accountingHandlerDAOobj) {
        String address = "";
        try {
            HashMap<String, Object> companyAddrRequestParams = new HashMap<String, Object>();
            companyAddrRequestParams.put("companyid", companyid);
            companyAddrRequestParams.put("isDefaultAddress", true);
            companyAddrRequestParams.put("isBillingAddress", false);
            KwlReturnObject result = accountingHandlerDAOobj.getCompanyAddressDetails(companyAddrRequestParams);
            if (result != null && !result.getEntityList().isEmpty()) {
                CompanyAddressDetails details = (CompanyAddressDetails) result.getEntityList().get(0);
                if (details != null) {
                    String addr = StringUtil.isNullOrEmpty(details.getAddress()) ? "" : details.getAddress();
                    String county = StringUtil.isNullOrEmpty(details.getCounty()) ? "" : ", " + details.getCounty();
                    String city = StringUtil.isNullOrEmpty(details.getCity()) ? "" : ", " + details.getCity();
                    String state = StringUtil.isNullOrEmpty(details.getState()) ? "" : ", " + details.getState();
                    String country = StringUtil.isNullOrEmpty(details.getCountry()) ? "" : ", " + details.getCountry();
                    String postalcode = StringUtil.isNullOrEmpty(details.getPostalCode()) ? "" : " " + details.getPostalCode();
                    String email = StringUtil.isNullOrEmpty(details.getEmailID()) ? "" : "\nEmail : " + details.getEmailID();
                    String website = StringUtil.isNullOrEmpty(details.getWebsite()) ? "" : "\nWebsite : " + details.getWebsite();
                    String phone = StringUtil.isNullOrEmpty(details.getPhone()) ? "" : "\nPhone : " + details.getPhone();
                    String fax = StringUtil.isNullOrEmpty(details.getFax()) ? "" : StringUtil.isNullOrEmpty(phone) ? "\nFax : " + details.getFax() : ", Fax : " + details.getFax();
                    String contractpersonno = StringUtil.isNullOrEmpty(details.getContactPersonNumber()) ? "" : "\nContact Person No : " + details.getContactPersonNumber();
                    String contractpersondesignation = StringUtil.isNullOrEmpty(details.getContactPersonDesignation()) ? "" : "\nContact Person Designation : " + details.getContactPersonDesignation();
                    String mobile = StringUtil.isNullOrEmpty(details.getMobileNumber()) ? "" : "\nMobile : " + details.getMobileNumber();
                    String attn = StringUtil.isNullOrEmpty(details.getContactPerson()) ? "" : "\nAttn. : " + details.getContactPerson();
                    address = addr + county + city + state + country + postalcode + email + website + phone + fax + mobile + contractpersonno + contractpersondesignation + attn;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }
     
     public static Map<String, Object> getDefaultCustomerAddress(Map<String,Object> rparams, AccountingHandlerDAO accountingHandlerDAOobj,JSONObject paramJobj) {
        HashMap<String, Object> addressMap = new HashMap<String, Object>();
        HashMap<String, Object> custAddrRequestParams = new HashMap<String, Object>();
        String companyid= rparams.get(Constants.companyKey)!=null?(String)rparams.get(Constants.companyKey):paramJobj.optString(Constants.companyKey);
        custAddrRequestParams.put("companyid", companyid);
        String isBillingAddress="false";

         try {
             if (rparams.containsKey("isBillingAddress") && rparams.get("isBillingAddress") != null) {
                 isBillingAddress = rparams.get("isBillingAddress").toString();
                 custAddrRequestParams.put("isBillingAddress", isBillingAddress);
             }

            if (rparams.containsKey("customerid") && rparams.get("customerid") != null) {
                String customerid = rparams.get("customerid").toString();
                custAddrRequestParams.put("customerid", customerid);
            }
            
            custAddrRequestParams.put("isDefaultAddress", true);
            KwlReturnObject result = accountingHandlerDAOobj.getCustomerAddressDetails(custAddrRequestParams);

            List<CustomerAddressDetails> details = result.getEntityList();
            for (CustomerAddressDetails cad : details) {
                if (cad.isIsBillingAddress()) {
                    addressMap.put(Constants.BILLING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.BILLING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.BILLING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.BILLING_STATE, cad.getState());
                    addressMap.put(Constants.BILLING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.BILLING_CITY, cad.getCity());
                    addressMap.put(Constants.BILLING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.BILLING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.BILLING_FAX, cad.getFax());
                    addressMap.put(Constants.BILLING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.BILLING_PHONE, cad.getPhone());
                    addressMap.put(Constants.BILLING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.BILLING_WEBSITE, cad.getWebsite());
                } else {
                    addressMap.put(Constants.SHIPPING_ADDRESS_TYPE, cad.getAliasName());
                    addressMap.put(Constants.SHIPPING_ADDRESS, cad.getAddress());
                    addressMap.put(Constants.SHIPPING_COUNTRY, cad.getCountry());
                    addressMap.put(Constants.SHIPPING_STATE, cad.getState());
                    addressMap.put(Constants.SHIPPING_COUNTY, cad.getCounty());
                    addressMap.put(Constants.SHIPPING_CITY, cad.getCity());
                    addressMap.put(Constants.SHIPPING_POSTAL, cad.getPostalCode());
                    addressMap.put(Constants.SHIPPING_EMAIL, cad.getEmailID());
                    addressMap.put(Constants.SHIPPING_FAX, cad.getFax());
                    addressMap.put(Constants.SHIPPING_MOBILE, cad.getMobileNumber());
                    addressMap.put(Constants.SHIPPING_PHONE, cad.getPhone());
                    addressMap.put(Constants.SHIPPING_RECIPIENT_NAME, cad.getRecipientName());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON, cad.getContactPerson());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, cad.getContactPersonNumber());
                    addressMap.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, cad.getContactPersonDesignation());
                    addressMap.put(Constants.SHIPPING_WEBSITE, cad.getWebsite());
                    addressMap.put(Constants.SHIPPING_ROUTE, cad.getShippingRoute());
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccountingAddressManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return addressMap;
    }
    
}