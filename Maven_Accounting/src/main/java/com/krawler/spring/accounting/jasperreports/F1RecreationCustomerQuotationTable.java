/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class F1RecreationCustomerQuotationTable {

    String srno;
    String productname = "";
    String productdesc = "";
    String quantity = "";
    String unitprice = "";
    String totalprice = "";
    String ImgPath = "";

    public void setImgPath(String ImgPath) {
        this.ImgPath = ImgPath;
    }

    public String getImgPath() {
        return this.ImgPath;
    }

    public void setSrno(String srno) {
        this.srno = srno;
    }

    public String getSrno() {
        return this.srno;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getProductname() {
        return this.productname;
    }

    public void setProductdesc(String productdesc) {
        this.productdesc = productdesc;
    }

    public String getProductdesc() {
        return this.productdesc;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getQuantity() {
        return this.quantity;
    }

    public void setUnitprice(String unitprice) {
        this.unitprice = unitprice;
    }

    public String getUnitprice() {
        return this.unitprice;
    }

    public void setTotalprice(String totalprice) {
        this.totalprice = totalprice;
    }

    public String getTotalprice() {
        return this.totalprice;
    }
}
