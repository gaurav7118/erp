/*This Class used for Customer Contact Detail 
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class CustomerAddressDetails extends AddressDetails{
    private String customerID; //since customer object is not acessible here due to maven dependency. so saving customer id only in table
    private String shippingRoute;
    public CustomerAddressDetails(){
        super();
    }
    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }
    
    public String getShippingRoute() {
        return shippingRoute;
    }

    public void setShippingRoute(String shippingRoute) {
        this.shippingRoute = shippingRoute;
    }
}
