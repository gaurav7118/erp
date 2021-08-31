/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author krawler
 */
public class TaxInvoice {

    String customfield="";
    String custname="";
    String custcode="";
    String salesPerson ="";
    String soldToCustomer ="";
    String soldToAddress ="";
    String deliveredToAddress ="";
    String deliveredToCustomer ="";
    String invoiceNo ="";
    String dONomber ="";
    String paymentTerms ="";
    String amountInWords ="";
    String grandTotal ="";
    String amount ="";
    String gstValue ="";
    String subtotal ="";
    String discount ="";
    String address ="";
    String gstRegNo ="";
    String date ="";
    String duedate ="";
    String shipDate="";
    String name ="";
    String uem ="";
    String amountTotal ="";
    String reportType ="";
    String customerFax="";
    String customerTel="";
    String customerEmail="";
    String printedby="";
    String pONumber="";
    String accNumber="";
    String qtNumber="";
    String contactPerson="";
    String gstpercent="";
    List<Terms> termsList=new ArrayList<Terms>();
    List<TaxInvoiceSubReport> invoicesublist=new ArrayList<TaxInvoiceSubReport>();
    public String getPrintedby() {
        return printedby;
    }

    public void setPrintedby(String printedby) {
        this.printedby = printedby;
    }
   
    public String getDuedate() {
        return duedate;
    }

    public void setDuedate(String duedate) {
        this.duedate = duedate;
    }

    public String getCustomfield() {
        return customfield;
    }

    public void setCustomfield(String customfield) {
        this.customfield = customfield;
    }

    public String getCustcode() {
        return custcode;
    }

    public void setCustcode(String custcode) {
        this.custcode = custcode;
    }

    public String getCustname() {
        return custname;
    }

    public void setCustname(String custname) {
        this.custname = custname;
    } 
    
    public String getUem() {
        return uem;
    }

    public void setUem(String uem) {
        this.uem = uem;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmountInWords() {
        return amountInWords;
    }

    public void setAmountInWords(String amountInWords) {
        this.amountInWords = amountInWords;
    }

    public String getdONomber() {
        return dONomber;
    }

    public void setdONomber(String dONomber) {
        this.dONomber = dONomber;
    }

    public String getDeliveredToAddress() {
        return deliveredToAddress;
    }

    public void setDeliveredToAddress(String deliveredToAddress) {
        this.deliveredToAddress = deliveredToAddress;
    }

    public String getDeliveredToCustomer() {
        return deliveredToCustomer;
    }

    public void setDeliveredToCustomer(String deliveredToCustomer) {
        this.deliveredToCustomer = deliveredToCustomer;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(String grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getGstValue() {
        return gstValue;
    }

    public void setGstValue(String gstValue) {
        this.gstValue = gstValue;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getSalesPerson() {
        return salesPerson;
    }

    public void setSalesPerson(String salesPerson) {
        this.salesPerson = salesPerson;
    }

    public String getSoldToAddress() {
        return soldToAddress;
    }

    public void setSoldToAddress(String soldToAddress) {
        this.soldToAddress = soldToAddress;
    }

    public String getSoldToCustomer() {
        return soldToCustomer;
    }

    public void setSoldToCustomer(String soldToCustomer) {
        this.soldToCustomer = soldToCustomer;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGstRegNo() {
        return gstRegNo;
    }

    public void setGstRegNo(String gstRegNo) {
        this.gstRegNo = gstRegNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(String amountTotal) {
        this.amountTotal = amountTotal;
    }
    
    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    public List<Terms> getTermsList() {
        return termsList;
    }

    public void setTermsList(List<Terms> termsList) {
        this.termsList = termsList;
    }
    public List<TaxInvoiceSubReport> getInvoiceSublist() {
        return invoicesublist;
    }

    public void setInvoiceSublist(List<TaxInvoiceSubReport> invoicesublist) {
        this.invoicesublist = invoicesublist;
    }
     public String getCustomerFax() {
        return customerFax;
    }

    public void setCustomerFax(String customerfax) {
        this.customerFax = customerfax;
    }
      public String getCustomerTel() {
        return customerTel;
    }

    public void setCustomerTel(String customerTel) {
        this.customerTel = customerTel;
    }
    public String getShipDate() {
        return shipDate;
    }

    public void setShipDate(String shipDate) {
        this.shipDate = shipDate;
    }
      public String getCustomerEmail() {
        return customerEmail;
    }
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
    public String getpONumber() {
        return pONumber;
    }
    public void setpONumber(String pONumber) {
        this.pONumber = pONumber;
    }
    public String getaccNumber() {
        return accNumber;
    }
    public void setaccNumber(String accNumber) {
        this.accNumber = accNumber;
    }
    public String getqtNumber() {
        return qtNumber;
    }
    public void setqtNumber(String qtNumber) {
        this.qtNumber = qtNumber;
    }
    public String getContactPerson() {
        return contactPerson;
    }
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }
    public String getGstpercent() {
        return gstpercent;
    }

    public void setGstpercent(String gstpercent) {
        this.gstpercent = gstpercent;
    }

}
