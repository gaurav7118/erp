/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class TDSRate {
    private int id;
    private String natureofpayment;
    private String deducteetype;
    private String residentialstatus;
    private String rate;
    private Date fromdate;
    private Date todate;
    private double fromamount; 
    private double toamount; 
    private double basicexemptionpertransaction; 
    private double basicexemptionperannum; 
    private double tdsrateifpannotavailable;
    private Company company;
    private boolean deleted;

    public String getDeducteetype() {
        return deducteetype;
    }

    public void setDeducteetype(String deducteetype) {
        this.deducteetype = deducteetype;
    }

    public Date getFromdate() {
        return fromdate;
    }

    public void setFromdate(Date fromdate) {
        this.fromdate = fromdate;
    }

    public String getNatureofpayment() {
        return natureofpayment;
    }

    public void setNatureofpayment(String natureofpayment) {
        this.natureofpayment = natureofpayment;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getResidentialstatus() {
        return residentialstatus;
    }

    public void setResidentialstatus(String residentialstatus) {
        this.residentialstatus = residentialstatus;
    }


    public Date getTodate() {
        return todate;
    }

    public void setTodate(Date todate) {
        this.todate = todate;
    }

    public double getFromamount() {
        return fromamount;
    }

    public void setFromamount(double fromamount) {
        this.fromamount = fromamount;
    }

    public double getToamount() {
        return toamount;
    }

    public void setToamount(double toamount) {
        this.toamount = toamount;
    }

    public double getTdsrateifpannotavailable() {
        return tdsrateifpannotavailable;
    }
    public void setTdsrateifpannotavailable(double tdsrateifpannotavailable) {
        this.tdsrateifpannotavailable = tdsrateifpannotavailable;
    }

    public Company getCompany() {
        return company;
    }
    public void setCompany(Company company) {
        this.company = company;
    }

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public boolean isDeleted() {
        return deleted;
    }
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public double getBasicexemptionpertransaction() {
        return basicexemptionpertransaction;
    }
    public void setBasicexemptionpertransaction(double basicexemptionpertransaction) {
        this.basicexemptionpertransaction = basicexemptionpertransaction;
    }

    public double getBasicexemptionperannum() {
        return basicexemptionperannum;
    }
    public void setBasicexemptionperannum(double basicexemptionperannum) {
        this.basicexemptionperannum = basicexemptionperannum;
    }
}
