/*This Class used for Vendor Contact Detail 
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class VendorAddressDetails extends AddressDetails{
    
    private String vendorID; //since customer object is not acessible here due to maven dependency. so saving customer id only in table  

    public VendorAddressDetails(){
        super();
    }
    public String getVendorID() {
        return vendorID;
    }

    public void setVendorID(String vendorID) {
        this.vendorID = vendorID;
    }    
}
