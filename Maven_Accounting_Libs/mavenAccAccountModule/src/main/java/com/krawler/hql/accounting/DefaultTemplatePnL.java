/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Country;

/**
 *
 * @author krawler
 */
public class DefaultTemplatePnL {

    private String ID;
    private String name;
    private int templateid;
    private String templatetitle;
    private int templatetype;
    private int status;
    private Country country;
    private boolean deleted;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
