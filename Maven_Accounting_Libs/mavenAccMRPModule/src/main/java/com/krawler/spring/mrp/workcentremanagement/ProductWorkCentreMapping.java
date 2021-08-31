/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.Product;
import com.krawler.spring.mrp.machinemanagement.Machine;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;

/**
 *
 * @author krawler
 */
public class ProductWorkCentreMapping {
    private String ID;
    private Product productid;
    private Company companyid;
    private WorkCentre workCenterID;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Company companyid) {
        this.companyid = companyid;
    }

    public Product getProductid() {
        return productid;
    }

    public void setProductid(Product productid) {
        this.productid = productid;
    }

    public WorkCentre getWorkCenterID() {
        return workCenterID;
    }

    public void setWorkCenterID(WorkCentre workCenterID) {
        this.workCenterID = workCenterID;
    }

}
