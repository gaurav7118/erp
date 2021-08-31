/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

import java.util.Comparator;

/**
 *
 * @author krawler
 */
public class JasperProductTable implements Comparator {

    String code = "";
    String name = "";
    String desc = "";
    String qty = "";
    String uom = "";
    String rate = "";
    String total = "";
    String totalExcludingTax = "";//string
    String totalWithout_Tax_Discount = "";//string
    double totalWithoutTax = 0;//Double type for jasper calculation
    double totalinDouble = 0;
    double rowTaxAmount = 0;
    String custom1 = "";
    String custom2 = "";
    String custom3 = "";
    String custom4 = "";
    String custom5 = "";
    String custom6 = "";
    String srNo = "";
    String amountCurrency = "";
    String productCategory = "";
    int productCategoryID = 0;

    public String getCustom6() {
        return custom6;
    }

    public void setCustom6(String custom6) {
        this.custom6 = custom6;
    }

    public String getCustom4() {
        return custom4;
    }

    public void setCustom4(String custom4) {
        this.custom4 = custom4;
    }

    public String getCustom5() {
        return custom5;
    }

    public void setCustom5(String custom5) {
        this.custom5 = custom5;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustom1() {
        return custom1;
    }

    public void setCustom1(String custom1) {
        this.custom1 = custom1;
    }

    public String getCustom2() {
        return custom2;
    }

    public void setCustom2(String custom2) {
        this.custom2 = custom2;
    }

    public String getCustom3() {
        return custom3;
    }

    public void setCustom3(String custom3) {
        this.custom3 = custom3;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getSrNo() {
        return srNo;
}

    public void setSrNo(String srNo) {
        this.srNo = srNo;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }

    public void setAmountCurrency(String amountCurrency) {
        this.amountCurrency = amountCurrency;
    }

    public double getTotalinDouble() {
        return totalinDouble;
    }

    public void setTotalinDouble(double totalindouble) {
        this.totalinDouble = totalindouble;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public double getTotalWithoutTax() {
        return totalWithoutTax;
    }

    public void setTotalWithoutTax(double totalWithoutTax) {
        this.totalWithoutTax = totalWithoutTax;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public int getProductCategoryID() {
        return productCategoryID;
    }

    public void setProductCategoryID(int productCategoryID) {
        this.productCategoryID = productCategoryID;
    }

    public String getTotalExcludingTax() {
        return totalExcludingTax;
    }

    public void setTotalExcludingTax(String totalExcludingTax) {
        this.totalExcludingTax = totalExcludingTax;
    }
    
     @Override
    public int compare(Object obj1, Object obj2) {
       Integer p1 = ((JasperProductTable) obj1).getProductCategoryID();
       Integer p2 = ((JasperProductTable) obj2).getProductCategoryID();

       if (p1 > p2) {
           return 1;
       } else if (p1 < p2){
           return -1;
       } else {
           return 0;
       }
    }

    public String getTotalWithout_Tax_Discount() {
        return totalWithout_Tax_Discount;
}

    public void setTotalWithout_Tax_Discount(String totalWithout_Tax_Discount) {
        this.totalWithout_Tax_Discount = totalWithout_Tax_Discount;
    }

}
