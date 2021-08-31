/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.vendor;

import com.krawler.common.admin.VendorAddresses;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Vendor;

/**
 *
 * @author krawler
 */
public class accVendorHandler {
    public static String getVendorAddress(Vendor vendor,boolean isbillingAddr) {
        String address = "";
        try {
            if (vendor != null) {
                VendorAddresses vendorAddresses = vendor.getVendorAddresses();
                if (vendorAddresses != null) {
                    if(isbillingAddr){
                        String addr = StringUtil.isNullOrEmpty(vendorAddresses.getBillingAddress1()) ? "" : vendorAddresses.getBillingAddress1();
                        String city = StringUtil.isNullOrEmpty(vendorAddresses.getBillingCity1()) ? "" : ", "+vendorAddresses.getBillingCity1();
                        String state = StringUtil.isNullOrEmpty(vendorAddresses.getBillingState1()) ? "" :", "+ vendorAddresses.getBillingState1();
                        String country = StringUtil.isNullOrEmpty(vendorAddresses.getBillingCountry1()) ? "" :", "+ vendorAddresses.getBillingCountry1();
                        String postalcode = StringUtil.isNullOrEmpty(vendorAddresses.getBillingPostal1()) ? "" :" "+ vendorAddresses.getBillingPostal1();
                        String email = StringUtil.isNullOrEmpty(vendorAddresses.getBillingEmail1()) ? "" :"\nEmail : "+ vendorAddresses.getBillingEmail1();
                        String phone = StringUtil.isNullOrEmpty(vendorAddresses.getBillingPhone1()) ? "" :"\nPhone : "+ vendorAddresses.getBillingPhone1();
                        String mobile = StringUtil.isNullOrEmpty(vendorAddresses.getBillingMobile1()) ? "" :"\nMobile : "+ vendorAddresses.getBillingMobile1();
                        String fax = StringUtil.isNullOrEmpty(vendorAddresses.getBillingFax1()) ? "" :", Fax : "+ vendorAddresses.getBillingFax1();
                        address = addr + city + state + country + postalcode + email+ phone + mobile + fax;   
                    } else{
                        String addr = StringUtil.isNullOrEmpty(vendorAddresses.getShippingAddress1()) ? "" : vendorAddresses.getShippingAddress1();
                        String city = StringUtil.isNullOrEmpty(vendorAddresses.getShippingCity1()) ? "" : ", "+vendorAddresses.getShippingCity1();
                        String state = StringUtil.isNullOrEmpty(vendorAddresses.getShippingState1()) ? "" :", "+ vendorAddresses.getShippingState1();
                        String country = StringUtil.isNullOrEmpty(vendorAddresses.getShippingCountry1()) ? "" :", "+ vendorAddresses.getShippingCountry1();
                        String postalcode = StringUtil.isNullOrEmpty(vendorAddresses.getShippingPostal1()) ? "" :" "+ vendorAddresses.getShippingPostal1();
                        String email = StringUtil.isNullOrEmpty(vendorAddresses.getShippingEmail1()) ? "" :"\nEmail : "+ vendorAddresses.getShippingEmail1();
                        String phone = StringUtil.isNullOrEmpty(vendorAddresses.getShippingPhone1()) ? "" :"\nPhone : "+ vendorAddresses.getShippingPhone1();
                        String mobile = StringUtil.isNullOrEmpty(vendorAddresses.getShippingMobile1()) ? "" :"\nMobile : "+ vendorAddresses.getShippingMobile1();
                        String fax = StringUtil.isNullOrEmpty(vendorAddresses.getShippingFax1()) ? "" :", Fax : "+ vendorAddresses.getShippingFax1();
                        address = addr + city + state + country + postalcode + email+ phone + mobile + fax;   
                    }
                } else {//For old vendor
                    String addr=StringUtil.isNullOrEmpty(vendor.getAddress()) ? "" : vendor.getAddress();
                    String email=StringUtil.isNullOrEmpty(vendor.getEmail())?"":"\nEmail : "+vendor.getEmail();
                    String phone = StringUtil.isNullOrEmpty(vendor.getContactNumber()) ? "" :"\nContact No : "+ vendor.getContactNumber();
                    String fax = StringUtil.isNullOrEmpty(vendor.getFax()) ? "" :", Fax : "+ vendor.getFax();
                    address = addr+email+phone+fax;
                    
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
    
    public static String getVendorAddressForSenwanTec(Vendor vendor,boolean isbillingAddr) {
        String address = "";
        try {
            if (vendor != null) {
                VendorAddresses vendorAddresses = vendor.getVendorAddresses();
                if (vendorAddresses != null) {
                    if(isbillingAddr){
                        String addr = StringUtil.isNullOrEmpty(vendorAddresses.getBillingAddress1()) ? "" : vendorAddresses.getBillingAddress1();
                        String city = StringUtil.isNullOrEmpty(vendorAddresses.getBillingCity1()) ? "" : ", "+vendorAddresses.getBillingCity1();
                        String state = StringUtil.isNullOrEmpty(vendorAddresses.getBillingState1()) ? "" :", "+ vendorAddresses.getBillingState1();
                        String country = StringUtil.isNullOrEmpty(vendorAddresses.getBillingCountry1()) ? "" :", "+ vendorAddresses.getBillingCountry1();
                        address = addr + city + state + country;   
                    } else{
                        String addr = StringUtil.isNullOrEmpty(vendorAddresses.getShippingAddress1()) ? "" : vendorAddresses.getShippingAddress1();
                        String city = StringUtil.isNullOrEmpty(vendorAddresses.getShippingCity1()) ? "" : ", "+vendorAddresses.getShippingCity1();
                        String state = StringUtil.isNullOrEmpty(vendorAddresses.getShippingState1()) ? "" :", "+ vendorAddresses.getShippingState1();
                        String country = StringUtil.isNullOrEmpty(vendorAddresses.getShippingCountry1()) ? "" :", "+ vendorAddresses.getShippingCountry1();
                        address = addr + city + state + country;   
                    }
                } else {//For old vendor
                    String addr=StringUtil.isNullOrEmpty(vendor.getAddress()) ? "" : vendor.getAddress();
                    address = addr;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
    public static String getTotalVendorAddress(Vendor vendor,boolean isbillingAddr) {
        String address = "";
        try {
            if (vendor != null) {
                VendorAddresses vendorAddresses = vendor.getVendorAddresses();
                if (vendorAddresses != null) {
                    if(isbillingAddr){
                        String addr = StringUtil.isNullOrEmpty(vendorAddresses.getBillingAddress1()) ? "" : vendorAddresses.getBillingAddress1();
                        String city = StringUtil.isNullOrEmpty(vendorAddresses.getBillingCity1()) ? "" : ", "+vendorAddresses.getBillingCity1();
                        String state = StringUtil.isNullOrEmpty(vendorAddresses.getBillingState1()) ? "" :", "+ vendorAddresses.getBillingState1();
                        String country = StringUtil.isNullOrEmpty(vendorAddresses.getBillingCountry1()) ? "" :", "+ vendorAddresses.getBillingCountry1();
                        String postalcode = StringUtil.isNullOrEmpty(vendorAddresses.getBillingPostal1()) ? "." :"-"+ vendorAddresses.getBillingPostal1();
                        address = addr + city + state + country + postalcode;   
                    } else{
                        String addr = StringUtil.isNullOrEmpty(vendorAddresses.getShippingAddress1()) ? "" : vendorAddresses.getShippingAddress1();
                        String city = StringUtil.isNullOrEmpty(vendorAddresses.getShippingCity1()) ? "" : ", "+vendorAddresses.getShippingCity1();
                        String state = StringUtil.isNullOrEmpty(vendorAddresses.getShippingState1()) ? "" :", "+ vendorAddresses.getShippingState1();
                        String country = StringUtil.isNullOrEmpty(vendorAddresses.getShippingCountry1()) ? "" :", "+ vendorAddresses.getShippingCountry1();
                        String postalcode = StringUtil.isNullOrEmpty(vendorAddresses.getBillingPostal1()) ? "." :"-"+ vendorAddresses.getBillingPostal1();
                        address = addr + city + state + country + postalcode;   
                    }
                } else {//For old vendor
                    String addr=StringUtil.isNullOrEmpty(vendor.getAddress()) ? "" : vendor.getAddress();
                    address = addr;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
}
