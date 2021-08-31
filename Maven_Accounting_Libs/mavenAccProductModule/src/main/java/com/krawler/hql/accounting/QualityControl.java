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
public class QualityControl {
    private String ID;
    private Company company;
    private Product product;
    private BOMDetail bom;
    private MasterItem qcgroup;
    private MasterItem qcparameter;
    private int qcvalue;
    private String qcdescription;
    private boolean deleteflag;
    private UnitOfMeasure qcuom;
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public BOMDetail getBom() {
        return bom;
    }

    public void setBom(BOMDetail bom) {
        this.bom = bom;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isDeleteflag() {
        return deleteflag;
    }

    public void setDeleteflag(boolean deleteflag) {
        this.deleteflag = deleteflag;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getQcdescription() {
        return qcdescription;
    }

    public void setQcdescription(String qcdescription) {
        this.qcdescription = qcdescription;
    }

    public MasterItem getQcgroup() {
        return qcgroup;
    }

    public void setQcgroup(MasterItem qcgroup) {
        this.qcgroup = qcgroup;
    }

    public MasterItem getQcparameter() {
        return qcparameter;
    }

    public void setQcparameter(MasterItem qcparameter) {
        this.qcparameter = qcparameter;
    }

    public int getQcvalue() {
        return qcvalue;
    }

    public void setQcvalue(int qcvalue) {
        this.qcvalue = qcvalue;
    }

    public UnitOfMeasure getQcuom() {
        return qcuom;
    }
    public void setQcuom(UnitOfMeasure qcuom) {
        this.qcuom = qcuom;
    }
}
