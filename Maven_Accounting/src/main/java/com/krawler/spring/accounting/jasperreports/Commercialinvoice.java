/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class Commercialinvoice {

    String custname = "";
    String custadd = "";
    String precarriageby = "";
    String oceanvessel = "";
    String shipfrom = "";
    String etd = "";
    String bl = "";
    String eta = "";
    String destination = "";
    String shippingterms = "";
    String invoiceno = "";
    String lcno = "";
    String date = "";
    String totalinword = "";
    String memo = "";

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getTotalinword() {
        return totalinword;
    }

    public void setTotalinword(String totalinword) {
        this.totalinword = totalinword;
    }
    
    public String getBl() {
        return bl;
    }

    public void setBl(String bl) {
        this.bl = bl;
    }

    public String getCustadd() {
        return custadd;
    }

    public void setCustadd(String custadd) {
        this.custadd = custadd;
    }

    public String getCustname() {
        return custname;
    }

    public void setCustname(String custname) {
        this.custname = custname;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public String getEtd() {
        return etd;
    }

    public void setEtd(String etd) {
        this.etd = etd;
    }

    public String getInvoiceno() {
        return invoiceno;
    }

    public void setInvoiceno(String invoiceno) {
        this.invoiceno = invoiceno;
    }

    public String getLcno() {
        return lcno;
    }

    public void setLcno(String lcno) {
        this.lcno = lcno;
    }

    public String getOceanvessel() {
        return oceanvessel;
    }

    public void setOceanvessel(String oceanvessel) {
        this.oceanvessel = oceanvessel;
    }

    public String getPrecarriageby() {
        return precarriageby;
    }

    public void setPrecarriageby(String precarriageby) {
        this.precarriageby = precarriageby;
    }

    public String getShipfrom() {
        return shipfrom;
    }

    public void setShipfrom(String shipfrom) {
        this.shipfrom = shipfrom;
    }

    public String getShippingterms() {
        return shippingterms;
    }

    public void setShippingterms(String shippingterms) {
        this.shippingterms = shippingterms;
    }
}
