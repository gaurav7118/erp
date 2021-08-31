/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class POOtherDetails {

    private String ID;
    private String purchaseOrderId;
    private String poyourref;
    private String podelyterm;
    private String poinvoiceto;
    private String podelydate;
    private String podept;
    private String porequestor;
    private String poproject;
    private String pomerno;
    private Company company;

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getPomerno() {
        return pomerno;
    }

    public void setPomerno(String pomerno) {
        this.pomerno = pomerno;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPoyourref() {
        return poyourref;
    }

    public void setPoyourref(String poyourref) {
        this.poyourref = poyourref;
    }

    public String getPodelyterm() {
        return podelyterm;
    }

    public void setPodelyterm(String podelyterm) {
        this.podelyterm = podelyterm;
    }

    public String getPoinvoiceto() {
        return poinvoiceto;
    }

    public void setPoinvoiceto(String poinvoiceto) {
        this.poinvoiceto = poinvoiceto;
    }

    public String getPodelydate() {
        return podelydate;
    }

    public void setPodelydate(String podelydate) {
        this.podelydate = podelydate;
    }

    public String getPodept() {
        return podept;
    }

    public void setPodept(String podept) {
        this.podept = podept;
    }

    public String getPorequestor() {
        return porequestor;
    }

    public void setPorequestor(String porequestor) {
        this.porequestor = porequestor;
    }

    public String getPoproject() {
        return poproject;
    }

    public void setPoproject(String poproject) {
        this.poproject = poproject;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
