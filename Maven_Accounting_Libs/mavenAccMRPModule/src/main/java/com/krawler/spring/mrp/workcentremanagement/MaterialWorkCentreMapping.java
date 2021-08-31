/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.BOMDetail;
import com.krawler.hql.accounting.Product;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;

/**
 *
 * @author krawler
 */
public class MaterialWorkCentreMapping {
    private String ID;
    private BOMDetail bomid;
    private Company companyid;
    private WorkCentre workCenterID;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public BOMDetail getBomid() {
        return bomid;
    }

    public void setBomid(BOMDetail bomid) {
        this.bomid = bomid;
    }

    public Company getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Company companyid) {
        this.companyid = companyid;
    }

    public WorkCentre getWorkCenterID() {
        return workCenterID;
    }

    public void setWorkCenterID(WorkCentre workCenterID) {
        this.workCenterID = workCenterID;
    }
    
    
}
