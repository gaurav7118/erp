/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class SenwanGroupPurchaseOrderTable {

        String shippingmethod="";
	String shippingterms="";
	String deliverydate="";

    public String getDeliverydate() {
        return deliverydate;
    }

    public void setDeliverydate(String deliverydate) {
        this.deliverydate = deliverydate;
    }

    public String getShippingmethod() {
        return shippingmethod;
    }

    public void setShippingmethod(String shippingmethod) {
        this.shippingmethod = shippingmethod;
    }

    public String getShippingterms() {
        return shippingterms;
    }

    public void setShippingterms(String shippingterms) {
        this.shippingterms = shippingterms;
    }
  
}
