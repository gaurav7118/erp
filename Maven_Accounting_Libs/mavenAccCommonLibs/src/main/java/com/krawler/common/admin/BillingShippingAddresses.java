/*This Class used for storing Address information for following transactions
 * 1.Vendor Invoice
 * 2.Customer Invoice
 * 3.Cash Purchase
 * 4.Cash Sales
 * 5.Purchase Order
 * 6.Sales Order
 * 7.Vendor Quotation
 * 8.Customer Quotation
 * 9.Delivery Order
 * 10.Goods Receipts Order
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class BillingShippingAddresses {

    private String ID;
    private String billingAddress;
    private String billingCountry;
    private String billingState;
    private String billingCounty;
    private String billingCity;
    private String billingPostal;
    private String billingEmail;
    private String billingFax;
    private String billingMobile;
    private String billingPhone;
    private String billingRecipientName;
    private String billingContactPerson;   
    private String billingContactPersonNumber;
    private String billingContactPersonDesignation;
    private String billingWebsite;
    private String billingAddressType;
    private String shippingAddress;
    private String shippingCountry;
    private String shippingState;
    private String shippingCounty;
    private String shippingCity;
    private String shippingEmail;
    private String shippingFax;
    private String shippingMobile;
    private String shippingPhone;
    private String shippingPostal;
    private String shippingContactPersonNumber;
    private String shippingContactPersonDesignation;
    private String shippingWebsite;
    private String shippingRecipientName;
    private String shippingContactPerson;
    private String shippingRoute;    
    private String shippingAddressType;
    private String vendcustShippingAddress;
    private String vendcustShippingCountry;
    private String vendcustShippingState;
    private String vendcustShippingCounty;
    private String vendcustShippingCity;
    private String vendcustShippingEmail;
    private String vendcustShippingFax;
    private String vendcustShippingMobile;
    private String vendcustShippingPhone;
    private String vendcustShippingPostal;
    private String vendcustShippingContactPersonNumber;
    private String vendcustShippingContactPersonDesignation;
    private String vendcustShippingWebsite;
    private String vendcustShippingRecipientName;
    private String vendcustShippingContactPerson;
    private String vendcustShippingAddressType;
    private String customerShippingAddress;
    private String customerShippingCountry;
    private String customerShippingState;
    private String customerShippingCounty;
    private String customerShippingCity;
    private String customerShippingEmail;
    private String customerShippingFax;
    private String customerShippingMobile;
    private String customerShippingPhone;
    private String customerShippingPostal;
    private String customerShippingContactPersonNumber;
    private String customerShippingContactPersonDesignation;
    private String customerShippingWebsite;
    private String customerShippingRecipientName;
    private String customerShippingContactPerson;
    private String customerShippingAddressType;
    private String customerShippingRoute;
   
    /*----
    
     Column contains data for dropship type doc where vendor Billing as well as Company Billing address 
    
     together comes into the picture
    
     Store vendor billing address
    
     ------  */
    
    private String vendorBillingAddress;
    private String vendorBillingCountry;
    private String vendorBillingState;
    private String vendorBillingCounty;
    private String vendorBillingCity;
    private String vendorBillingPostal;
    private String vendorBillingEmail;
    private String vendorBillingFax;
    private String vendorBillingMobile;
    private String vendorBillingPhone;
    private String vendorBillingRecipientName;
    private String vendorBillingContactPerson;
    private String vendorBillingContactPersonNumber;
    private String vendorBillingContactPersonDesignation;
    private String vendorBillingWebsite;
    private String vendorBillingAddressType;
    
    /**
     * Vendor Billing address details if "Show Vendor address in Purchase document" flag is OFF and INDIA Country
     */
    private String vendorBillingAddressForINDIA;
    private String vendorBillingCountryForINDIA;
    private String vendorBillingStateForINDIA;
    private String vendorBillingCountyForINDIA;
    private String vendorBillingCityForINDIA;
    private String vendorBillingPostalForINDIA;
    private String vendorBillingEmailForINDIA;
    private String vendorBillingFaxForINDIA;
    private String vendorBillingMobileForINDIA;
    private String vendorBillingPhoneForINDIA;
    private String vendorBillingRecipientNameForINDIA;
    private String vendorBillingContactPersonForINDIA;
    private String vendorBillingContactPersonNumberForINDIA;
    private String vendorBillingContactPersonDesignationForINDIA;
    private String vendorBillingWebsiteForINDIA;
    private String vendorBillingAddressTypeForINDIA;
    
    private Company company;

    public BillingShippingAddresses() {
        this.billingAddress = "";
        this.billingCountry = "";
        this.billingState = "";
        this.billingCounty = "";
        this.billingCity = "";
        this.billingPostal = "";
        this.billingEmail = "";
        this.billingFax = "";
        this.billingMobile = "";
        this.billingPhone = "";
        this.billingRecipientName = "";
        this.billingContactPerson = "";
        this.billingContactPersonNumber = "";
        this.billingContactPersonDesignation = "";
        this.billingWebsite = "";
        this.billingAddressType = "";
        this.shippingAddress = "";
        this.shippingCountry = "";
        this.shippingState = "";
        this.shippingCounty = "";
        this.shippingCity = "";
        this.shippingEmail = "";
        this.shippingFax = "";
        this.shippingMobile = "";
        this.shippingPhone = "";
        this.shippingPostal = "";
        this.shippingContactPersonNumber = "";
        this.shippingContactPersonDesignation = "";
        this.shippingWebsite = "";
        this.shippingRecipientName = "";
        this.shippingContactPerson = "";
        this.shippingRoute = "";
        this.shippingAddressType = "";
        this.vendcustShippingAddress = "";
        this.vendcustShippingCountry = "";
        this.vendcustShippingState = "";
        this.vendcustShippingCounty = "";
        this.vendcustShippingCity = "";
        this.vendcustShippingEmail = "";
        this.vendcustShippingFax = "";
        this.vendcustShippingMobile = "";
        this.vendcustShippingPhone = "";
        this.vendcustShippingPostal = "";
        this.vendcustShippingContactPersonNumber = "";
        this.vendcustShippingContactPersonDesignation = "";
        this.vendcustShippingWebsite = "";
        this.vendcustShippingRecipientName = "";
        this.vendcustShippingContactPerson = "";
        this.vendcustShippingAddressType = "";
        this.vendorBillingAddress = "";
        this.vendorBillingCountry = "";
        this.vendorBillingState = "";
        this.vendorBillingCounty = "";
        this.vendorBillingCity = "";
        this.vendorBillingPostal = "";
        this.vendorBillingEmail = "";
        this.vendorBillingFax = "";
        this.vendorBillingMobile = "";
        this.vendorBillingPhone = "";
        this.vendorBillingRecipientName = "";
        this.vendorBillingContactPerson = "";
        this.vendorBillingContactPersonNumber = "";
        this.vendorBillingContactPersonDesignation = "";
        this.vendorBillingWebsite = "";
        this.vendorBillingAddressType = "";
        this.vendorBillingAddressForINDIA = "";
        this.vendorBillingCountryForINDIA = "";
        this.vendorBillingStateForINDIA = "";
        this.vendorBillingCountyForINDIA = "";
        this.vendorBillingCityForINDIA = "";
        this.vendorBillingPostalForINDIA = "";
        this.vendorBillingEmailForINDIA = "";
        this.vendorBillingFaxForINDIA = "";
        this.vendorBillingMobileForINDIA = "";
        this.vendorBillingPhoneForINDIA = "";
        this.vendorBillingRecipientNameForINDIA = "";
        this.vendorBillingContactPersonForINDIA = "";
        this.vendorBillingContactPersonNumberForINDIA = "";
        this.vendorBillingContactPersonDesignationForINDIA = "";
        this.vendorBillingWebsiteForINDIA = "";
        this.vendorBillingAddressTypeForINDIA = "";
    }

    public String getVendorBillingAddress() {
        return vendorBillingAddress;
    }

    public void setVendorBillingAddress(String vendorBillingAddress) {
        this.vendorBillingAddress = vendorBillingAddress;
    }

    public String getVendorBillingCountry() {
        return vendorBillingCountry;
    }

    public void setVendorBillingCountry(String vendorBillingCountry) {
        this.vendorBillingCountry = vendorBillingCountry;
    }

    public String getVendorBillingState() {
        return vendorBillingState;
    }

    public void setVendorBillingState(String vendorBillingState) {
        this.vendorBillingState = vendorBillingState;
    }

    public String getVendorBillingCounty() {
        return vendorBillingCounty;
    }

    public void setVendorBillingCounty(String vendorBillingCounty) {
        this.vendorBillingCounty = vendorBillingCounty;
    }

    public String getVendorBillingCity() {
        return vendorBillingCity;
    }

    public void setVendorBillingCity(String vendorBillingCity) {
        this.vendorBillingCity = vendorBillingCity;
    }

    public String getVendorBillingPostal() {
        return vendorBillingPostal;
    }

    public void setVendorBillingPostal(String vendorBillingPostal) {
        this.vendorBillingPostal = vendorBillingPostal;
    }

    public String getVendorBillingEmail() {
        return vendorBillingEmail;
    }

    public void setVendorBillingEmail(String vendorBillingEmail) {
        this.vendorBillingEmail = vendorBillingEmail;
    }

    public String getVendorBillingFax() {
        return vendorBillingFax;
    }

    public void setVendorBillingFax(String vendorBillingFax) {
        this.vendorBillingFax = vendorBillingFax;
    }

    public String getVendorBillingMobile() {
        return vendorBillingMobile;
    }

    public void setVendorBillingMobile(String vendorBillingMobile) {
        this.vendorBillingMobile = vendorBillingMobile;
    }

    public String getVendorBillingPhone() {
        return vendorBillingPhone;
    }

    public void setVendorBillingPhone(String vendorBillingPhone) {
        this.vendorBillingPhone = vendorBillingPhone;
    }

    public String getVendorBillingRecipientName() {
        return vendorBillingRecipientName;
    }

    public void setVendorBillingRecipientName(String vendorBillingRecipientName) {
        this.vendorBillingRecipientName = vendorBillingRecipientName;
    }

    public String getVendorBillingContactPerson() {
        return vendorBillingContactPerson;
    }

    public void setVendorBillingContactPerson(String vendorBillingContactPerson) {
        this.vendorBillingContactPerson = vendorBillingContactPerson;
    }

    public String getVendorBillingContactPersonNumber() {
        return vendorBillingContactPersonNumber;
    }

    public void setVendorBillingContactPersonNumber(String vendorBillingContactPersonNumber) {
        this.vendorBillingContactPersonNumber = vendorBillingContactPersonNumber;
    }

    public String getVendorBillingContactPersonDesignation() {
        return vendorBillingContactPersonDesignation;
    }

    public void setVendorBillingContactPersonDesignation(String vendorBillingContactPersonDesignation) {
        this.vendorBillingContactPersonDesignation = vendorBillingContactPersonDesignation;
    }

    public String getVendorBillingWebsite() {
        return vendorBillingWebsite;
    }

    public void setVendorBillingWebsite(String vendorBillingWebsite) {
        this.vendorBillingWebsite = vendorBillingWebsite;
    }

    public String getVendorBillingAddressType() {
        return vendorBillingAddressType;
    }

    public void setVendorBillingAddressType(String vendorBillingAddressType) {
        this.vendorBillingAddressType = vendorBillingAddressType;
    }

    public String getVendorBillingAddressForINDIA() {
        return vendorBillingAddressForINDIA;
    }

    public void setVendorBillingAddressForINDIA(String vendorBillingAddressForINDIA) {
        this.vendorBillingAddressForINDIA = vendorBillingAddressForINDIA;
    }

    public String getVendorBillingCountryForINDIA() {
        return vendorBillingCountryForINDIA;
    }

    public void setVendorBillingCountryForINDIA(String vendorBillingCountryForINDIA) {
        this.vendorBillingCountryForINDIA = vendorBillingCountryForINDIA;
    }

    public String getVendorBillingStateForINDIA() {
        return vendorBillingStateForINDIA;
    }

    public void setVendorBillingStateForINDIA(String vendorBillingStateForINDIA) {
        this.vendorBillingStateForINDIA = vendorBillingStateForINDIA;
    }

    public String getVendorBillingCountyForINDIA() {
        return vendorBillingCountyForINDIA;
    }

    public void setVendorBillingCountyForINDIA(String vendorBillingCountyForINDIA) {
        this.vendorBillingCountyForINDIA = vendorBillingCountyForINDIA;
    }

    public String getVendorBillingCityForINDIA() {
        return vendorBillingCityForINDIA;
    }

    public void setVendorBillingCityForINDIA(String vendorBillingCityForINDIA) {
        this.vendorBillingCityForINDIA = vendorBillingCityForINDIA;
    }

    public String getVendorBillingPostalForINDIA() {
        return vendorBillingPostalForINDIA;
    }

    public void setVendorBillingPostalForINDIA(String vendorBillingPostalForINDIA) {
        this.vendorBillingPostalForINDIA = vendorBillingPostalForINDIA;
    }

    public String getVendorBillingEmailForINDIA() {
        return vendorBillingEmailForINDIA;
    }

    public void setVendorBillingEmailForINDIA(String vendorBillingEmailForINDIA) {
        this.vendorBillingEmailForINDIA = vendorBillingEmailForINDIA;
    }

    public String getVendorBillingFaxForINDIA() {
        return vendorBillingFaxForINDIA;
    }

    public void setVendorBillingFaxForINDIA(String vendorBillingFaxForINDIA) {
        this.vendorBillingFaxForINDIA = vendorBillingFaxForINDIA;
    }

    public String getVendorBillingMobileForINDIA() {
        return vendorBillingMobileForINDIA;
    }

    public void setVendorBillingMobileForINDIA(String vendorBillingMobileForINDIA) {
        this.vendorBillingMobileForINDIA = vendorBillingMobileForINDIA;
    }

    public String getVendorBillingPhoneForINDIA() {
        return vendorBillingPhoneForINDIA;
    }

    public void setVendorBillingPhoneForINDIA(String vendorBillingPhoneForINDIA) {
        this.vendorBillingPhoneForINDIA = vendorBillingPhoneForINDIA;
    }

    public String getVendorBillingRecipientNameForINDIA() {
        return vendorBillingRecipientNameForINDIA;
    }

    public void setVendorBillingRecipientNameForINDIA(String vendorBillingRecipientNameForINDIA) {
        this.vendorBillingRecipientNameForINDIA = vendorBillingRecipientNameForINDIA;
    }

    public String getVendorBillingContactPersonForINDIA() {
        return vendorBillingContactPersonForINDIA;
    }

    public void setVendorBillingContactPersonForINDIA(String vendorBillingContactPersonForINDIA) {
        this.vendorBillingContactPersonForINDIA = vendorBillingContactPersonForINDIA;
    }

    public String getVendorBillingContactPersonNumberForINDIA() {
        return vendorBillingContactPersonNumberForINDIA;
    }

    public void setVendorBillingContactPersonNumberForINDIA(String vendorBillingContactPersonNumberForINDIA) {
        this.vendorBillingContactPersonNumberForINDIA = vendorBillingContactPersonNumberForINDIA;
    }

    public String getVendorBillingContactPersonDesignationForINDIA() {
        return vendorBillingContactPersonDesignationForINDIA;
    }

    public void setVendorBillingContactPersonDesignationForINDIA(String vendorBillingContactPersonDesignationForINDIA) {
        this.vendorBillingContactPersonDesignationForINDIA = vendorBillingContactPersonDesignationForINDIA;
    }

    public String getVendorBillingWebsiteForINDIA() {
        return vendorBillingWebsiteForINDIA;
    }

    public void setVendorBillingWebsiteForINDIA(String vendorBillingWebsiteForINDIA) {
        this.vendorBillingWebsiteForINDIA = vendorBillingWebsiteForINDIA;
    }

    public String getVendorBillingAddressTypeForINDIA() {
        return vendorBillingAddressTypeForINDIA;
    }

    public void setVendorBillingAddressTypeForINDIA(String vendorBillingAddressTypeForINDIA) {
        this.vendorBillingAddressTypeForINDIA = vendorBillingAddressTypeForINDIA;
    }
    

    public String getBillingWebsite() {
        return billingWebsite;
    }

    public void setBillingWebsite(String billingWebsite) {
        this.billingWebsite = billingWebsite;
    }

    public String getShippingWebsite() {
        return shippingWebsite;
    }

    public void setShippingWebsite(String shippingWebsite) {
        this.shippingWebsite = shippingWebsite;
    }

    public String getVendcustShippingWebsite() {
        return vendcustShippingWebsite;
    }

    public void setVendcustShippingWebsite(String vendcustShippingWebsite) {
        this.vendcustShippingWebsite = vendcustShippingWebsite;
    }
    
    public String getVendcustShippingAddress() {
        return vendcustShippingAddress;
    }

    public void setVendcustShippingAddress(String vendcustShippingAddress) {
        this.vendcustShippingAddress = vendcustShippingAddress;
    }

    public String getVendcustShippingAddressType() {
        return vendcustShippingAddressType;
    }

    public void setVendcustShippingAddressType(String vendcustShippingAddressType) {
        this.vendcustShippingAddressType = vendcustShippingAddressType;
    }

    public String getVendcustShippingCity() {
        return vendcustShippingCity;
    }

    public void setVendcustShippingCity(String vendcustShippingCity) {
        this.vendcustShippingCity = vendcustShippingCity;
    }

    public String getVendcustShippingContactPerson() {
        return vendcustShippingContactPerson;
    }

    public void setVendcustShippingContactPerson(String vendcustShippingContactPerson) {
        this.vendcustShippingContactPerson = vendcustShippingContactPerson;
    }

    public String getVendcustShippingContactPersonNumber() {
        return vendcustShippingContactPersonNumber;
    }

    public void setVendcustShippingContactPersonNumber(String vendcustShippingContactPersonNumber) {
        this.vendcustShippingContactPersonNumber = vendcustShippingContactPersonNumber;
    }
    public String getVendcustShippingContactPersonDesignation() {
        return vendcustShippingContactPersonDesignation;
    }

    public void setVendcustShippingContactPersonDesignation(String vendcustShippingContactPersonDesignation) {
        this.vendcustShippingContactPersonDesignation = vendcustShippingContactPersonDesignation;
    }
    public String getVendcustShippingCountry() {
        return vendcustShippingCountry;
    }

    public void setVendcustShippingCountry(String vendcustShippingCountry) {
        this.vendcustShippingCountry = vendcustShippingCountry;
    }

    public String getVendcustShippingEmail() {
        return vendcustShippingEmail;
    }

    public void setVendcustShippingEmail(String vendcustShippingEmail) {
        this.vendcustShippingEmail = vendcustShippingEmail;
    }

    public String getVendcustShippingFax() {
        return vendcustShippingFax;
    }

    public void setVendcustShippingFax(String vendcustShippingFax) {
        this.vendcustShippingFax = vendcustShippingFax;
    }

    public String getVendcustShippingMobile() {
        return vendcustShippingMobile;
    }

    public void setVendcustShippingMobile(String vendcustShippingMobile) {
        this.vendcustShippingMobile = vendcustShippingMobile;
    }

    public String getVendcustShippingPhone() {
        return vendcustShippingPhone;
    }

    public void setVendcustShippingPhone(String vendcustShippingPhone) {
        this.vendcustShippingPhone = vendcustShippingPhone;
    }

    public String getVendcustShippingPostal() {
        return vendcustShippingPostal;
    }

    public void setVendcustShippingPostal(String vendcustShippingPostal) {
        this.vendcustShippingPostal = vendcustShippingPostal;
    }

    public String getVendcustShippingRecipientName() {
        return vendcustShippingRecipientName;
    }

    public void setVendcustShippingRecipientName(String vendcustShippingRecipientName) {
        this.vendcustShippingRecipientName = vendcustShippingRecipientName;
    }

    public String getVendcustShippingState() {
        return vendcustShippingState;
    }

    public void setVendcustShippingState(String vendcustShippingState) {
        this.vendcustShippingState = vendcustShippingState;
    }
    
    public String getCustomerShippingAddress() {
        return customerShippingAddress;
    }

    public void setCustomerShippingAddress(String customerShippingAddress) {
        this.customerShippingAddress = customerShippingAddress;
    }

    public String getCustomerShippingCountry() {
        return customerShippingCountry;
    }

    public void setCustomerShippingCountry(String customerShippingCountry) {
        this.customerShippingCountry = customerShippingCountry;
    }

    public String getCustomerShippingState() {
        return customerShippingState;
    }

    public void setCustomerShippingState(String customerShippingState) {
        this.customerShippingState = customerShippingState;
    }

    public String getCustomerShippingCity() {
        return customerShippingCity;
    }

    public void setCustomerShippingCity(String customerShippingCity) {
        this.customerShippingCity = customerShippingCity;
    }

    public String getCustomerShippingEmail() {
        return customerShippingEmail;
    }

    public void setCustomerShippingEmail(String customerShippingEmail) {
        this.customerShippingEmail = customerShippingEmail;
    }

    public String getCustomerShippingFax() {
        return customerShippingFax;
    }

    public void setCustomerShippingFax(String customerShippingFax) {
        this.customerShippingFax = customerShippingFax;
    }

    public String getCustomerShippingMobile() {
        return customerShippingMobile;
    }

    public void setCustomerShippingMobile(String customerShippingMobile) {
        this.customerShippingMobile = customerShippingMobile;
    }

    public String getCustomerShippingPhone() {
        return customerShippingPhone;
    }

    public void setCustomerShippingPhone(String customerShippingPhone) {
        this.customerShippingPhone = customerShippingPhone;
    }

    public String getCustomerShippingPostal() {
        return customerShippingPostal;
    }

    public void setCustomerShippingPostal(String customerShippingPostal) {
        this.customerShippingPostal = customerShippingPostal;
    }

    public String getCustomerShippingContactPersonNumber() {
        return customerShippingContactPersonNumber;
    }

    public void setCustomerShippingContactPersonNumber(String customerShippingContactPersonNumber) {
        this.customerShippingContactPersonNumber = customerShippingContactPersonNumber;
    }

    public String getCustomerShippingContactPersonDesignation() {
        return customerShippingContactPersonDesignation;
    }

    public void setCustomerShippingContactPersonDesignation(String customerShippingContactPersonDesignation) {
        this.customerShippingContactPersonDesignation = customerShippingContactPersonDesignation;
    }

    public String getCustomerShippingWebsite() {
        return customerShippingWebsite;
    }

    public void setCustomerShippingWebsite(String customerShippingWebsite) {
        this.customerShippingWebsite = customerShippingWebsite;
    }

    public String getCustomerShippingRecipientName() {
        return customerShippingRecipientName;
    }

    public void setCustomerShippingRecipientName(String customerShippingRecipientName) {
        this.customerShippingRecipientName = customerShippingRecipientName;
    }

    public String getCustomerShippingContactPerson() {
        return customerShippingContactPerson;
    }

    public void setCustomerShippingContactPerson(String customerShippingContactPerson) {
        this.customerShippingContactPerson = customerShippingContactPerson;
    }

    public String getCustomerShippingAddressType() {
        return customerShippingAddressType;
    }

    public void setCustomerShippingAddressType(String customerShippingAddressType) {
        this.customerShippingAddressType = customerShippingAddressType;
    }
    
    
    public String getCustomerShippingRoute() {
        return customerShippingRoute;
    }

    public void setCustomerShippingRoute(String customerShippingRoute) {
        this.customerShippingRoute = customerShippingRoute;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getBillingRecipientName() {
        return billingRecipientName;
    }

    public void setBillingRecipientName(String billingRecipientName) {
        this.billingRecipientName = billingRecipientName;
    }

    public String getShippingRecipientName() {
        return shippingRecipientName;
    }

    public void setShippingRecipientName(String shippingRecipientName) {
        this.shippingRecipientName = shippingRecipientName;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getBillingCity() {
        return billingCity;
    }

    public void setBillingCity(String billingCity) {
        this.billingCity = billingCity;
    }

    public String getBillingContactPerson() {
        return billingContactPerson;
    }

    public void setBillingContactPerson(String billingContactPerson) {
        this.billingContactPerson = billingContactPerson;
    }

    public String getBillingContactPersonNumber() {
        return billingContactPersonNumber;
    }

    public void setBillingContactPersonNumber(String billingContactPersonNumber) {
        this.billingContactPersonNumber = billingContactPersonNumber;
    }

    public String getBillingContactPersonDesignation() {
        return billingContactPersonDesignation;
    }

    public void setBillingContactPersonDesignation(String billingContactPersonDesignation) {
        this.billingContactPersonDesignation = billingContactPersonDesignation;
    }
    public String getBillingCountry() {
        return billingCountry;
    }

    public void setBillingCountry(String billingCountry) {
        this.billingCountry = billingCountry;
    }

    public String getBillingEmail() {
        return billingEmail;
    }

    public void setBillingEmail(String billingEmail) {
        this.billingEmail = billingEmail;
    }

    public String getBillingFax() {
        return billingFax;
    }

    public void setBillingFax(String billingFax) {
        this.billingFax = billingFax;
    }

    public String getBillingMobile() {
        return billingMobile;
    }

    public void setBillingMobile(String billingMobile) {
        this.billingMobile = billingMobile;
    }

    public String getBillingPhone() {
        return billingPhone;
    }

    public void setBillingPhone(String billingPhone) {
        this.billingPhone = billingPhone;
    }

    public String getBillingPostal() {
        return billingPostal;
    }

    public void setBillingPostal(String billingPostal) {
        this.billingPostal = billingPostal;
    }

    public String getBillingState() {
        return billingState;
    }

    public void setBillingState(String billingState) {
        this.billingState = billingState;
    }

    public String getBillingAddressType() {
        return billingAddressType;
    }

    public void setBillingAddressType(String billingAddressType) {
        this.billingAddressType = billingAddressType;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public void setShippingCity(String shippingCity) {
        this.shippingCity = shippingCity;
    }

    public String getShippingContactPerson() {
        return shippingContactPerson;
    }

    public void setShippingContactPerson(String shippingContactPerson) {
        this.shippingContactPerson = shippingContactPerson;
    }

    public String getShippingRoute() {
        return shippingRoute;
    }

    public void setShippingRoute(String shippingRoute) {
        this.shippingRoute = shippingRoute;
    }

    public String getShippingContactPersonNumber() {
        return shippingContactPersonNumber;
    }

    public void setShippingContactPersonNumber(String shippingContactPersonNumber) {
        this.shippingContactPersonNumber = shippingContactPersonNumber;
    }
    public String getShippingContactPersonDesignation() {
        return shippingContactPersonDesignation;
    }

    public void setShippingContactPersonDesignation(String shippingContactPersonDesignation) {
        this.shippingContactPersonDesignation = shippingContactPersonDesignation;
    }
    public String getShippingCountry() {
        return shippingCountry;
    }

    public void setShippingCountry(String shippingCountry) {
        this.shippingCountry = shippingCountry;
    }

    public String getShippingEmail() {
        return shippingEmail;
    }

    public void setShippingEmail(String shippingEmail) {
        this.shippingEmail = shippingEmail;
    }

    public String getShippingFax() {
        return shippingFax;
    }

    public void setShippingFax(String shippingFax) {
        this.shippingFax = shippingFax;
    }

    public String getShippingMobile() {
        return shippingMobile;
    }

    public void setShippingMobile(String shippingMobile) {
        this.shippingMobile = shippingMobile;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public void setShippingPhone(String shippingPhone) {
        this.shippingPhone = shippingPhone;
    }

    public String getShippingPostal() {
        return shippingPostal;
    }

    public void setShippingPostal(String shippingPostal) {
        this.shippingPostal = shippingPostal;
    }

    public String getShippingState() {
        return shippingState;
    }

    public void setShippingState(String shippingState) {
        this.shippingState = shippingState;
    }

    public String getShippingAddressType() {
        return shippingAddressType;
    }

    public void setShippingAddressType(String shippingAddressType) {
        this.shippingAddressType = shippingAddressType;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getBillingCounty() {
        return billingCounty;
    }

    public void setBillingCounty(String billingCounty) {
        this.billingCounty = billingCounty;
    }

    public String getShippingCounty() {
        return shippingCounty;
    }

    public void setShippingCounty(String shippingCounty) {
        this.shippingCounty = shippingCounty;
    }

    public String getVendcustShippingCounty() {
        return vendcustShippingCounty;
    }

    public void setVendcustShippingCounty(String vendcustShippingCounty) {
        this.vendcustShippingCounty = vendcustShippingCounty;
    }

    public String getCustomerShippingCounty() {
        return customerShippingCounty;
    }

    public void setCustomerShippingCounty(String customerShippingCounty) {
        this.customerShippingCounty = customerShippingCounty;
    }
    
    public String getFullShippingAddress() {
        StringBuilder sb = new StringBuilder();
        if (this.shippingAddress != null) {
            sb.append(this.shippingAddress);
        }
        
        if (sb.length() > 0 && this.shippingCounty != null) {
            sb.append(" \n").append(this.shippingCounty);
        } else {
            if(this.shippingCounty != null){
                sb.append(this.shippingCounty);
            }
        }
        
        if (sb.length() > 0 && this.shippingCity != null) {
            sb.append(" \n").append(this.shippingCity);
        } else {
            if(this.shippingCity != null){
                sb.append(this.shippingCity);
            }
        }
        
        if (sb.length() > 0 && this.shippingState != null) {
            sb.append(" \n").append(this.shippingState);
        } else {
            if(this.shippingState != null){
                sb.append(this.shippingState);
            }
        }
        
        if (sb.length() > 0 && this.shippingCountry != null) {
            sb.append(" \n").append(this.shippingCountry);
        } else {
            if(this.shippingCountry != null){
                sb.append(this.shippingCountry);
            }
        }
        
        if (sb.length() > 0 && this.shippingPostal != null) {
            sb.append(" \n").append(this.shippingPostal);
        } else {
            if(this.shippingPostal != null){
                sb.append(this.shippingPostal);
            }
        }
        
        if (sb.length() > 0 && this.shippingPhone != null) {
            sb.append("\n").append(this.shippingPhone);
        } else {
            if(this.shippingPhone != null){
                sb.append(this.shippingPhone);
            }
        }
        
        if (sb.length() > 0 && this.shippingMobile != null) {
            sb.append(" \n").append(this.shippingMobile);
        } else {
            if(this.shippingMobile != null){
                sb.append(this.shippingMobile);
            }
        }
        
        if (sb.length() > 0 && this.shippingFax != null) {
            sb.append(" \n").append(this.shippingFax);
        } else {
            if(this.shippingFax != null){
                sb.append(this.shippingFax);
            }
        }
        
        if (sb.length() > 0 && this.shippingEmail != null) {
            sb.append(" \n").append(this.shippingEmail);
        } else {
            if(this.shippingEmail != null){
                sb.append(this.shippingEmail);
            }
        }
        
        if (sb.length() > 0 && this.shippingContactPerson != null) {
            sb.append(" \n").append(this.shippingContactPerson);
        } else {
            if(this.shippingContactPerson != null){
                sb.append(this.shippingContactPerson);
            }
        }
        
        if (sb.length() > 0 && this.shippingRecipientName != null) {
            sb.append(" \n").append(this.shippingRecipientName);
        } else {
            if(this.shippingRecipientName != null){
                sb.append(this.shippingRecipientName);
            }
        }
        
        if (sb.length() > 0 && this.shippingContactPersonNumber != null) {
            sb.append(" \n").append(this.shippingContactPersonNumber);
        } else {
            if(this.shippingContactPersonNumber != null){
                sb.append(this.shippingContactPersonNumber);
            }
        }
        if (sb.length() > 0 && this.shippingContactPersonDesignation != null) {
            sb.append(" \n").append(this.shippingContactPersonDesignation);
        } else {
            if (this.shippingContactPersonDesignation != null) {
                sb.append(this.shippingContactPersonDesignation);
            }
        }
        return sb.toString().trim();
    }
}
