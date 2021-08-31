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
public class Templatepnl {

    private String ID;
    private String name;
    private int templateid;
    private String templatetitle;
    private String templateheading; //For India Country Only 
    private int templatetype;
    private int status;
    private Company company;
    private boolean deleted;
    private boolean dontshowmsg; // Flag to show pop up message having information of unmapped accounts.
    private boolean defaultTemplate; // Flag to set cuatom layout as default pnl or bs report.

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isDontshowmsg() {
        return dontshowmsg;
    }

    public void setDontshowmsg(boolean dontshowmsg) {
        this.dontshowmsg = dontshowmsg;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getTemplateid() {
        return templateid;
    }

    public void setTemplateid(int templateid) {
        this.templateid = templateid;
    }

    public String getTemplatetitle() {
        return templatetitle;
    }

    public void setTemplatetitle(String templatetitle) {
        this.templatetitle = templatetitle;
    }

    public int getTemplatetype() {
        return templatetype;
    }

    public void setTemplatetype(int templatetype) {
        this.templatetype = templatetype;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getTemplateheading() {
        return templateheading;
    }
    public void setTemplateheading(String templateheading) {
        this.templateheading = templateheading;
    }

    public boolean isDefaultTemplate() {
        return defaultTemplate;
    }

    public void setDefaultTemplate(boolean defaultTemplate) {
        this.defaultTemplate = defaultTemplate;
    }

}
