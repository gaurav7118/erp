/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class BITPurchaseRequisition {
    String prn = "";
    String date = "";
    String requester = "";
    String customer = "";
    String project = "";
    String purpose = "";
    String supplier = "";
    String manager = "";
    String managerDate = "";
    String managerComment = "";
    String finance = "";
    String financeDate = "";
    String financeComment = "";
    String ceo = "";
    String ceoDate = "";
    String ceoComment = "";
    String accountCode="";
    String currency="";
    String refno="";
    String subtotal="";
    String gstpercent="";
    String gst="";
    String totalamount="";
    String invoiceNo="";
    String custname="";
    String amountTotal="";
    String amountInWords="";
    String reqbyname="";
    String apprbyname="";
    String reqbydate="";
    String apprbydate="";
    String reqbydesign="";
    String apprbydesign="";
    
    public String getReqbyname()
    {
        return reqbyname;
    }
    public void setReqbyname(String reqbyname)
    {
        this.reqbyname=reqbyname;
    }
     public String getApprbyname()
    {
        return apprbyname;
    }
    public void setApprbyname(String apprbyname)
    {
        this.apprbyname=apprbyname;
    }
      public String getReqbydate()
    {
        return reqbydate;
    }
    public void setReqbydate(String reqbydate)
    {
        this.reqbydate=reqbydate;
    }
       public String getApprbydate()
    {
        return apprbydate;
    }
    public void setApprbydate(String apprbydate)
    {
        this.apprbydate=apprbydate;
    }
        public String getReqbydesign()
    {
        return reqbydesign;
    }
    public void setReqbydesign(String reqbydesign)
    {
        this.reqbydesign=reqbydesign;
    }
    public String getApprbydesign()
    {
        return apprbydesign;
    }
    public void setApprbydesign(String apprbydesign)
    {
        this.apprbydesign=apprbydesign;
    }
    public String getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(String amountTotal) {
        this.amountTotal = amountTotal;
    }
    public String getAmountInWords() {
        return amountInWords;
    }

    public void setAmountInWords(String amountInWords) {
        this.amountInWords = amountInWords;
    }

      public String getCustname() {
        return custname;
    }

    public void setCustname(String custname) {
        this.custname = custname;
    } 
     public String getInvoiceNo() {
        return invoiceNo;
    }
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    public String getTotalamount() {
        return totalamount;
    }
    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }
    public String getGst() {
        return gst;
    }
    public void setGst(String gst) {
        this.gst = gst;
    }
    public String getGstpercent() {
        return gstpercent;
    }
    public void setGstpercent(String gstpercent) {
        this.gstpercent = gstpercent;
    }
    public String getSubtotal() {
        return subtotal;
    }
    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }
    public String getRefno() {
        return refno;
    }

    public void setRefno(String refno) {
        this.refno = refno;
    }
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }
    public String getCeo() {
        return ceo;
    }

    public void setCeo(String ceo) {
        this.ceo = ceo;
    }

    public String getCeoComment() {
        return ceoComment;
    }

    public void setCeoComment(String ceoComment) {
        this.ceoComment = ceoComment;
    }

    public String getCeoDate() {
        return ceoDate;
    }

    public void setCeoDate(String ceoDate) {
        this.ceoDate = ceoDate;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFinance() {
        return finance;
    }

    public void setFinance(String finance) {
        this.finance = finance;
    }

    public String getFinanceComment() {
        return financeComment;
    }

    public void setFinanceComment(String financeComment) {
        this.financeComment = financeComment;
    }

    public String getFinanceDate() {
        return financeDate;
    }

    public void setFinanceDate(String financeDate) {
        this.financeDate = financeDate;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public String getManagerComment() {
        return managerComment;
    }

    public void setManagerComment(String managerComment) {
        this.managerComment = managerComment;
    }

    public String getManagerDate() {
        return managerDate;
    }

    public void setManagerDate(String managerDate) {
        this.managerDate = managerDate;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
  }
