/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class SatTaxInvoice {

    String gstregno = "";
    String documentno = "";
    String date = "";
    String comregno = "";
    String accno = "";
    String terms = "";
    String name = "";
    String add = "";
    String subtotal = "";
    String taxpercent = "";
    String taxamount = "";
    String totalamount = "";
    double totalamountValue = 0;
    String raisedby = "";
    String currency = "";
    String cust = "";
    String credit = "";
    String vesselname = "";
    String shiplength = "";
    String phone = "";
    String contactperson = "";
    String hp = "";
    String fax = "";
      public String getShiplength() {
        return shiplength;
    }

    public void setShiplength(String shiplength) {
        this.shiplength = shiplength;
    }
     public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
     public String getContactperson() {
        return contactperson;
    }

    public void setContactperson(String contactperson) {
        this.contactperson = contactperson;
    }
     public String gethp() {
        return hp;
    }

    public void setHp(String hp) {
        this.hp = hp;
    }
   public String getFax() {
        return vesselname;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }
    
    public String getVesselname() {
        return vesselname;
    }

    public void setVesselname(String vesselname) {
        this.vesselname = vesselname;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getCust() {
        return cust;
    }

    public void setCust(String cust) {
        this.cust = cust;
    }

    public String getAccno() {
        return accno;
    }

    public void setAccno(String accno) {
        this.accno = accno;
    }

    public String getAdd() {
        return add;
    }

    public void setAdd(String add) {
        this.add = add;
    }

    public String getComregno() {
        return comregno;
    }

    public void setComregno(String comregno) {
        this.comregno = comregno;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDocumentno() {
        return documentno;
    }

    public void setDocumentno(String documentno) {
        this.documentno = documentno;
    }

    public String getGstregno() {
        return gstregno;
    }

    public void setGstregno(String gstregno) {
        this.gstregno = gstregno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRaisedby() {
        return raisedby;
    }

    public void setRaisedby(String raisedby) {
        this.raisedby = raisedby;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(String subtotal) {
        this.subtotal = subtotal;
    }

    public String getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(String taxamount) {
        this.taxamount = taxamount;
    }

    public String getTaxpercent() {
        return taxpercent;
    }

    public void setTaxpercent(String taxpercent) {
        this.taxpercent = taxpercent;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }

    public String getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(String totalamount) {
        this.totalamount = totalamount;
    }

    public double getTotalamountValue() {
        return totalamountValue;
}

    public void setTotalamountValue(double totalamountValue) {
        this.totalamountValue = totalamountValue;
    }
}
