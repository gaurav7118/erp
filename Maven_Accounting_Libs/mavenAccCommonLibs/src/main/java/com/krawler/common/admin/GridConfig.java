/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author krawler
 */
public class GridConfig {
    private String cid;
    private String rules;
    private String state;
    private String moduleid;
    private int deleteflag;
    private long updatedOn;
    private User user;
    private boolean isDocumentEntryForm;//Ture: when entry form grid config saved & false: when Report Grid config is saved saved.
                                    //Example: for invoice entry product grid  it will be true but for Invoice report it will be false
    private Company company;

//    private boolean isNewConfigSaved;

    public boolean isIsDocumentEntryForm() {
        return isDocumentEntryForm;
    }

    public void setIsDocumentEntryForm(boolean isDocumentEntryForm) {
        this.isDocumentEntryForm = isDocumentEntryForm;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setDeleteflag(int deleteflag) {
        this.deleteflag = deleteflag;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCid() {
        return cid;
    }

    public String getRules() {
        return rules;
    }

    public String getModuleid() {
        return moduleid;
    }

    public void setModuleid(String moduleid) {
        this.moduleid = moduleid;
    }

    public String getState() {
        return state;
    }

    public int getDeleteflag() {
        return deleteflag;
    }

    public User getUser() {
        return user;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
//    public boolean isIsNewConfigSaved() {
//        return isNewConfigSaved;
//    }
//
//    public void setIsNewConfigSaved(boolean isNewConfigSaved) {
//        this.isNewConfigSaved = isNewConfigSaved;
//    }
}
