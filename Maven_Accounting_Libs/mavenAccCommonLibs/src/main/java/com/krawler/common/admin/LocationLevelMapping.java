/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class LocationLevelMapping {
    private String ID;
    private String newLevelNm;
    private LocationLevel llevelid;
    private Company company;
    private String parent;
    private boolean activate;
    
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

    public LocationLevel getLlevelid() {
        return llevelid;
    }

    public void setLlevelid(LocationLevel llevelid) {
        this.llevelid = llevelid;
    }



    public String getNewLevelNm() {
        return newLevelNm;
    }

    public void setNewLevelNm(String newLevelNm) {
        this.newLevelNm = newLevelNm;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
    
}
