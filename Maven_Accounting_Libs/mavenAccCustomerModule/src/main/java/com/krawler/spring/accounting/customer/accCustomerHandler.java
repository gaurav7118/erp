/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customer;

import com.krawler.common.admin.CustomerAddresses;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Customer;

/**
 *
 * @author krawler
 */
public class accCustomerHandler {

    public static String getCustomerAddress(Customer customer, boolean isbillingAddr) {
        String address = "";
        try {
            if (customer != null) {
                CustomerAddresses customerAddresses = customer.getCustomerAddresses();
                if (customerAddresses != null) {
                    if (isbillingAddr) {
                        String addr = StringUtil.isNullOrEmpty(customerAddresses.getBillingAddress1()) ? "" : customerAddresses.getBillingAddress1();
                        String city = StringUtil.isNullOrEmpty(customerAddresses.getBillingCity1()) ? "" : ", " + customerAddresses.getBillingCity1();
                        String state = StringUtil.isNullOrEmpty(customerAddresses.getBillingState1()) ? "" : ", " + customerAddresses.getBillingState1();
                        String country = StringUtil.isNullOrEmpty(customerAddresses.getBillingCountry1()) ? "" : ", " + customerAddresses.getBillingCountry1();
                        String postalcode = StringUtil.isNullOrEmpty(customerAddresses.getBillingPostal1()) ? "" : " " + customerAddresses.getBillingPostal1();
                        String email = StringUtil.isNullOrEmpty(customerAddresses.getBillingEmail1()) ? "" : "\nEmail : " + customerAddresses.getBillingEmail1();
                        String phone = StringUtil.isNullOrEmpty(customerAddresses.getBillingPhone1()) ? "" : "\nPhone : " + customerAddresses.getBillingPhone1();
                        String mobile = StringUtil.isNullOrEmpty(customerAddresses.getBillingMobile1()) ? "" : "\nMobile : " + customerAddresses.getBillingMobile1();
                        String fax = StringUtil.isNullOrEmpty(customerAddresses.getBillingFax1()) ? "" : ", Fax : " + customerAddresses.getBillingFax1();
                        address = addr + city + state + country + postalcode + email + phone + mobile + fax;
                    } else {
                        String addr = StringUtil.isNullOrEmpty(customerAddresses.getShippingAddress1()) ? "" : customerAddresses.getShippingAddress1();
                        String city = StringUtil.isNullOrEmpty(customerAddresses.getShippingCity1()) ? "" : ", " + customerAddresses.getShippingCity1();
                        String state = StringUtil.isNullOrEmpty(customerAddresses.getShippingState1()) ? "" : ", " + customerAddresses.getShippingState1();
                        String country = StringUtil.isNullOrEmpty(customerAddresses.getShippingCountry1()) ? "" : ", " + customerAddresses.getShippingCountry1();
                        String postalcode = StringUtil.isNullOrEmpty(customerAddresses.getShippingPostal1()) ? "" : " " + customerAddresses.getShippingPostal1();
                        String email = StringUtil.isNullOrEmpty(customerAddresses.getShippingEmail1()) ? "" : "\nEmail : " + customerAddresses.getShippingEmail1();
                        String phone = StringUtil.isNullOrEmpty(customerAddresses.getShippingPhone1()) ? "" : "\nPhone : " + customerAddresses.getShippingPhone1();
                        String mobile = StringUtil.isNullOrEmpty(customerAddresses.getShippingMobile1()) ? "" : "\nMobile : " + customerAddresses.getShippingMobile1();
                        String fax = StringUtil.isNullOrEmpty(customerAddresses.getShippingFax1()) ? "" : ", Fax : " + customerAddresses.getShippingFax1();
                        address = addr + city + state + country + postalcode + email + phone + mobile + fax;
                    }
                } else {//For old customer address saved in customer table
                    if (isbillingAddr) {
                        String addr = StringUtil.isNullOrEmpty(customer.getBillingAddress()) ? "" : customer.getBillingAddress();
                        String email = StringUtil.isNullOrEmpty(customer.getEmail()) ? "" : "\nEmail : " + customer.getEmail();
                        String phone = StringUtil.isNullOrEmpty(customer.getContactNumber()) ? "" : "\nContact No : " + customer.getContactNumber();
                        String fax = StringUtil.isNullOrEmpty(customer.getFax()) ? "" : ", Fax : " + customer.getFax();
                        address = addr + email + phone + fax;
                    } else {
                        String addr = StringUtil.isNullOrEmpty(customer.getShippingAddress()) ? "" : customer.getShippingAddress();
                        String email = StringUtil.isNullOrEmpty(customer.getEmail()) ? "" : "\nEmail : " + customer.getEmail();
                        String phone = StringUtil.isNullOrEmpty(customer.getContactNumber()) ? "" : "\nContact No : " + customer.getContactNumber();
                        String fax = StringUtil.isNullOrEmpty(customer.getFax()) ? "" : ", Fax : " + customer.getFax();
                        address = addr + email + phone + fax;
                    }

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }

    public static String getCustomerAddressForSenwanTec(Customer customer, boolean isbillingAddr) {
        String address = "";
        try {
            if (customer != null) {
                CustomerAddresses customerAddresses = customer.getCustomerAddresses();
                if (customerAddresses != null) {
                    if (isbillingAddr) {
                        String addr = StringUtil.isNullOrEmpty(customerAddresses.getBillingAddress1()) ? "" : customerAddresses.getBillingAddress1();
                        String city = StringUtil.isNullOrEmpty(customerAddresses.getBillingCity1()) ? "" : ", " + customerAddresses.getBillingCity1();
                        String state = StringUtil.isNullOrEmpty(customerAddresses.getBillingState1()) ? "" : ", " + customerAddresses.getBillingState1();
                        String country = StringUtil.isNullOrEmpty(customerAddresses.getBillingCountry1()) ? "" : ", " + customerAddresses.getBillingCountry1();
                        address = addr + city + state + country;
                    } else {
                        String addr = StringUtil.isNullOrEmpty(customerAddresses.getShippingAddress1()) ? "" : customerAddresses.getShippingAddress1();
                        String city = StringUtil.isNullOrEmpty(customerAddresses.getShippingCity1()) ? "" : ", " + customerAddresses.getShippingCity1();
                        String state = StringUtil.isNullOrEmpty(customerAddresses.getShippingState1()) ? "" : ", " + customerAddresses.getShippingState1();
                        String country = StringUtil.isNullOrEmpty(customerAddresses.getShippingCountry1()) ? "" : ", " + customerAddresses.getShippingCountry1();
                        address = addr + city + state + country;
                    }
                } else {//For old customer address saved in customer table
                    if (isbillingAddr) {
                        String addr = StringUtil.isNullOrEmpty(customer.getBillingAddress()) ? "" : customer.getBillingAddress();
                        address = addr;
                    } else {
                        String addr = StringUtil.isNullOrEmpty(customer.getShippingAddress()) ? "" : customer.getShippingAddress();
                        address = addr;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
    public static String getTotalCustomerAddress(Customer customer, boolean isbillingAddr) {
        String address = "";
        try {
            if (customer != null) {
                CustomerAddresses customerAddresses = customer.getCustomerAddresses();
                if (customerAddresses != null) {
                    if (isbillingAddr) {
                        String addr = StringUtil.isNullOrEmpty(customerAddresses.getBillingAddress1()) ? "" : customerAddresses.getBillingAddress1();
                        String city = StringUtil.isNullOrEmpty(customerAddresses.getBillingCity1()) ? "" : ", " + customerAddresses.getBillingCity1();
                        String state = StringUtil.isNullOrEmpty(customerAddresses.getBillingState1()) ? "" : ", " + customerAddresses.getBillingState1();
                        String country = StringUtil.isNullOrEmpty(customerAddresses.getBillingCountry1()) ? "" : ", " + customerAddresses.getBillingCountry1();
                        String postalcode = StringUtil.isNullOrEmpty(customerAddresses.getShippingPostal1()) ? "." : "-" + customerAddresses.getShippingPostal1();
                        address = addr + city + state + country + postalcode;
                    } else {
                        String addr = StringUtil.isNullOrEmpty(customerAddresses.getShippingAddress1()) ? "" : customerAddresses.getShippingAddress1();
                        String city = StringUtil.isNullOrEmpty(customerAddresses.getShippingCity1()) ? "" : ", " + customerAddresses.getShippingCity1();
                        String state = StringUtil.isNullOrEmpty(customerAddresses.getShippingState1()) ? "" : ", " + customerAddresses.getShippingState1();
                        String country = StringUtil.isNullOrEmpty(customerAddresses.getShippingCountry1()) ? "" : ", " + customerAddresses.getShippingCountry1();
                        String postalcode = StringUtil.isNullOrEmpty(customerAddresses.getShippingPostal1()) ? "." : "-" + customerAddresses.getShippingPostal1();
                        address = addr + city + state + country + postalcode;
                        
                      
                    }
                } else {//For old customer address saved in customer table
                    if (isbillingAddr) {
                        String addr = StringUtil.isNullOrEmpty(customer.getBillingAddress()) ? "" : customer.getBillingAddress();
                        address = addr;
                    } else {
                        String addr = StringUtil.isNullOrEmpty(customer.getShippingAddress()) ? "" : customer.getShippingAddress();
                        address = addr;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return address;
    }
}
