/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class SenwanGroupPurchaseOrderSubReport {

		String sno="";
		String code="";
		String currencysymbol="";            
                String qty="";
		String itemno="";
		String desc="";
		String job="";
		String price="";
		String linetotal="";
		String pono="";
                String date="";
		String shippinginfo="";
		String terms="";
		String quotationno="";
		String prn="";
                String certificate = "";
                String aftCond ="";
                String sirialno = "";

    public String getSirialno() {
        return sirialno;
    }

    public void setSirialno(String sirialno) {
        this.sirialno = sirialno;
    }

    public String getAftCond() {
        return aftCond;
    }

    public void setAftCond(String AftCond) {
        this.aftCond = AftCond;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String Certificate) {
        this.certificate = Certificate;
    }

                        


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrn() {
        return prn;
    }

    public void setPrn(String prn) {
        this.prn = prn;
    }

    public String getQuotationno() {
        return quotationno;
    }

    public void setQuotationno(String quotationno) {
        this.quotationno = quotationno;
    }

    public String getShippinginfo() {
        return shippinginfo;
    }

    public void setShippinginfo(String shippinginfo) {
        this.shippinginfo = shippinginfo;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getPono() {
        return pono;
    }

    public void setPono(String pono) {
        this.pono = pono;
    }
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrencysymbol() {
        return currencysymbol;
    }

    public void setCurrencysymbol(String currencysymbol) {
        this.currencysymbol = currencysymbol;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String no) {
        this.sno = no;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getItemno() {
        return itemno;
    }

    public void setItemno(String itemno) {
        this.itemno = itemno;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getLinetotal() {
        return linetotal;
    }

    public void setLinetotal(String linetotal) {
        this.linetotal = linetotal;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }
  
}
