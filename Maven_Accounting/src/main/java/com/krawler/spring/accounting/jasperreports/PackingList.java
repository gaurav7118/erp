/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class PackingList {

    private String customer;
    private String incoterms;
    private String vessel;
    private String portofloading;
    private String portofdischarge;
    private String billno;
    private String date;
    private String letterofcn;
    private String partialshipment;
    private String transhipment;
    private String beneficiary;
    private String dateoflc;
    private String packing;
    private double totalquantity;
    private double totalgrossweight;
    private double totalmeasurement;

    public String getBeneficiary() {
        return beneficiary;
    }

    public String getBillno() {
        return billno;
    }

    public String getCustomer() {
        return customer;
    }

    public String getDate() {
        return date;
    }

    public String getDateoflc() {
        return dateoflc;
    }

    public String getIncoterms() {
        return incoterms;
    }

    public String getLetterofcn() {
        return letterofcn;
    }

    public String getPacking() {
        return packing;
    }

    public String getPartialshipment() {
        return partialshipment;
    }

    public String getPortofdischarge() {
        return portofdischarge;
    }

    public String getPortofloading() {
        return portofloading;
    }

    public double getTotalgrossweight() {
        return totalgrossweight;
    }

    public double getTotalmeasurement() {
        return totalmeasurement;
    }

    public double getTotalquantity() {
        return totalquantity;
    }

    public String getTranshipment() {
        return transhipment;
    }

    public String getVessel() {
        return vessel;
    }
    
    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    public void setBillno(String billno) {
        this.billno = billno;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setDateoflc(String dateoflc) {
        this.dateoflc = dateoflc;
    }

    public void setIncoterms(String incoterms) {
        this.incoterms = incoterms;
    }

    public void setLetterofcn(String letterofcn) {
        this.letterofcn = letterofcn;
    }

    public void setPacking(String packing) {
        this.packing = packing;
    }

    public void setPartialshipment(String partialshipment) {
        this.partialshipment = partialshipment;
    }

    public void setPortofdischarge(String portofdischarge) {
        this.portofdischarge = portofdischarge;
    }

    public void setPortofloading(String portofloading) {
        this.portofloading = portofloading;
    }

    public void setTotalgrossweight(double totalgrossweight) {
        this.totalgrossweight = totalgrossweight;
    }

    public void setTotalmeasurement(double totalmeasurement) {
        this.totalmeasurement = totalmeasurement;
    }

    public void setTotalquantity(double totalquantity) {
        this.totalquantity = totalquantity;
    }

    public void setTranshipment(String transhipment) {
        this.transhipment = transhipment;
    }

    public void setVessel(String vessel) {
        this.vessel = vessel;
    }
}
