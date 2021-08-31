/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class PackingInvoiceListJasperTable {
    private String sno;
    private String carton;
    private String sku;
    private String description;
    private String retailprice;
    private double qty;
    private String qtypics;
    private String qtyctn;
    private String department;
    private String totalamount;
  
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getQtyctn() {
        return qtyctn;
    }

    public void setQtyctn(String qtyctn) {
        this.qtyctn = qtyctn;
    }

    public String getQtypics() {
        return qtypics;
    }

    public void setQtypics(String qtypics) {
        this.qtypics = qtypics;
    }

    public String getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }
    
    public String getCarton() {
        return carton;
    }

    public void setCarton(String carton) {
        this.carton = carton;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public String getRetailprice() {
        return retailprice;
    }

    public void setRetailprice(String retailprice) {
        this.retailprice = retailprice;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }
   
}