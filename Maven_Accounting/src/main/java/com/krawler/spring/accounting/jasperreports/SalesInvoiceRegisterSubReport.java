/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class SalesInvoiceRegisterSubReport {
    String srno = "";
    String invoiceno = "";
    String donumber = "";
    String custNo = "";
    String custName = "";
    String salesperson = "";
    String docNo = "";
    String invoiceDate = "";
    String doDate = "";
    String dueDate = "";
    String currency = "";
    String prodCode = "";
    String description = "";
    String qty = "";
    String uom = "";
    String rate = "";
    String amount = "";
    String discount = "";
    String taxName = "";
    String basicAmount = "";
    String taxAmount = "";
    String termAmount = "";
    String discountAmount = "";
    String totalAmount = "";
    double basicAmountVal = 0;
    double taxAmountVal = 0;
    double termAmountVal = 0;
    double totalAmountVal = 0;
    double discountAmountVal = 0;

    public String getSalesperson() {
        return salesperson;
    }

    public void setSalesperson(String salesperson) {
        this.salesperson = salesperson;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
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

    public String getBasicAmount() {
        return basicAmount;
    }

    public void setBasicAmount(String basicAmount) {
        this.basicAmount = basicAmount;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getTermAmount() {
        return termAmount;
    }

    public void setTermAmount(String termAmount) {
        this.termAmount = termAmount;
    }
    
    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }    
    
    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTaxName() {
        return taxName;
    }

    public void setTaxName(String taxName) {
        this.taxName = taxName;
    }
    
    public String getSrno() {
        return srno;
    }

    public void setSrno(String srno) {
        this.srno = srno;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProdCode() {
        return prodCode;
    }

    public void setProdCode(String prodCode) {
        this.prodCode = prodCode;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getCustNo() {
        return custNo;
    }

    public void setCustNo(String custNo) {
        this.custNo = custNo;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getDonumber() {
        return donumber;
    }

    public void setDonumber(String donumber) {
        this.donumber = donumber;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getInvoiceno() {
        return invoiceno;
    }

    public void setInvoiceno(String invoiceno) {
        this.invoiceno = invoiceno;
    }
    
    public String getDoDate() {
        return doDate;
    }

    public void setDoDate(String doDate) {
        this.doDate = doDate;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getBasicAmountVal() {
        return basicAmountVal;
    }

    public void setBasicAmountVal(double basicAmountVal) {
        this.basicAmountVal = basicAmountVal;
    }

    public double getTaxAmountVal() {
        return taxAmountVal;
    }

    public void setTaxAmountVal(double taxAmountVal) {
        this.taxAmountVal = taxAmountVal;
    }

    public double getTotalAmountVal() {
        return totalAmountVal;
    }
    
    public double getDiscountAmountVal() {
        return discountAmountVal;
    }

    public void setDiscountAmountVal(double discountAmountVal) {
        this.discountAmountVal = discountAmountVal;
    }

    public double getTermAmountVal() {
        return termAmountVal;
    }

    public void setTermAmountVal(double termAmountVal) {
        this.termAmountVal = termAmountVal;
    }
    
    public void setTotalAmountVal(double totalAmountVal) {
        this.totalAmountVal = totalAmountVal;
    }

}

