/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class StockAgeing {

    String prodid = "";
    String prodcode = "";
    String prodname = "";
    String unit = "";
    String date = "";
    String documentno = "";
    String documentname = "";
    Double quantity1 = 0.0;
    Double quantity2 = 0.0;
    Double quantity3 = 0.0;
    Double amount1 = 0.0;
    Double amount2 = 0.0;
    Double amount3 = 0.0;
    String header1 = "";
    String header2 = "";
    String header3 = "";
    String currency = "";
    String decimalFormatForQuantity = "";
    String decimalFormatForAmount = "";

    public String getDecimalFormatForQuantity() {
        return decimalFormatForQuantity;
    }

    public void setDecimalFormatForQuantity(String decimalFormatForQuantity) {
        this.decimalFormatForQuantity = decimalFormatForQuantity;
    }

    public String getDecimalFormatForAmount() {
        return decimalFormatForAmount;
    }

    public void setDecimalFormatForAmount(String decimalFormatForAmount) {
        this.decimalFormatForAmount = decimalFormatForAmount;
    }
    
    public String getProdid() {
        return prodid;
    }

    public void setProdid(String prodid) {
        this.prodid = prodid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getAmount1() {
        return amount1;
    }

    public void setAmount1(Double amount1) {
        this.amount1 = amount1;
    }

    public Double getAmount2() {
        return amount2;
    }

    public void setAmount2(Double amount2) {
        this.amount2 = amount2;
    }

    public Double getAmount3() {
        return amount3;
    }

    public void setAmount3(Double amount3) {
        this.amount3 = amount3;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocumentname() {
        return documentname;
    }

    public void setDocumentname(String documentname) {
        this.documentname = documentname;
    }

    public String getDocumentno() {
        return documentno;
    }

    public void setDocumentno(String documentno) {
        this.documentno = documentno;
    }

    public String getHeader1() {
        return header1;
    }

    public void setHeader1(String header1) {
        this.header1 = header1;
    }

    public String getHeader2() {
        return header2;
    }

    public void setHeader2(String header2) {
        this.header2 = header2;
    }

    public String getHeader3() {
        return header3;
    }

    public void setHeader3(String header3) {
        this.header3 = header3;
    }

    public String getProdcode() {
        return prodcode;
    }

    public void setProdcode(String prodcode) {
        this.prodcode = prodcode;
    }

    public String getProdname() {
        return prodname;
    }

    public void setProdname(String prodname) {
        this.prodname = prodname;
    }

    public Double getQuantity1() {
        return quantity1;
    }

    public void setQuantity1(Double quantity1) {
        this.quantity1 = quantity1;
    }

    public Double getQuantity2() {
        return quantity2;
    }

    public void setQuantity2(Double quantity2) {
        this.quantity2 = quantity2;
    }

    public Double getQuantity3() {
        return quantity3;
    }

    public void setQuantity3(Double quantity3) {
        this.quantity3 = quantity3;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
