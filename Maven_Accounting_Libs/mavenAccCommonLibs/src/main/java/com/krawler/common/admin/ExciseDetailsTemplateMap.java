/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import com.krawler.common.admin.Company;
import com.krawler.inventory.model.store.Store;

/**
 *
 * @author krawler
 */
public class ExciseDetailsTemplateMap {
    private String id;
//    private String manufacturerType;
    private String registrationType;
    private String unitname;
    private String ECCNo; 
    private Company companyid;
    private Store warehouseid;

    public Store getWarehouseid() {
        return warehouseid;
    }

    public void setWarehouseid(Store warehouseid) {
        this.warehouseid = warehouseid;
    }
    
    public String getECCNo() {
        return ECCNo;
    }

    public void setECCNo(String ECCNo) {
        this.ECCNo = ECCNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

//    public String getManufacturerType() {
//        return manufacturerType;
//    }
//
//    public void setManufacturerType(String manufacturerType) {
//        this.manufacturerType = manufacturerType;
//    }

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public String getUnitname() {
        return unitname;
    }

    public void setUnitname(String unitname) {
        this.unitname = unitname;
    }

    public Company getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Company companyid) {
        this.companyid = companyid;
    }
    
}


