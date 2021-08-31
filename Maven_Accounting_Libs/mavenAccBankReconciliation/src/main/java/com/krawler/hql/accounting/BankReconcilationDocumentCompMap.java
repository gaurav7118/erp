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
public class BankReconcilationDocumentCompMap {

    private String ID;
    private BankReconcilationDocuments document;
    private String reconcileID;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public BankReconcilationDocuments getDocument() {
        return document;
    }

    public void setDocument(BankReconcilationDocuments document) {
        this.document = document;
    }

    public String getReconcileID() {
        return reconcileID;
    }

    public void setReconcileID(String reconcileID) {
        this.reconcileID = reconcileID;
    }

}
