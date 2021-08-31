/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;

/**
 *
 * @author krawler
 */
public class RevaluationJECustomData {
    private String ID;
    private String customfield;
    private String lineleveldimensions;
    private User userid;
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

    public String getCustomfield() {
        return customfield;
    }

    public void setCustomfield(String customfield) {
        this.customfield = customfield;
    }

    public String getLineleveldimensions() {
        return lineleveldimensions;
    }

    public void setLineleveldimensions(String lineleveldimensions) {
        this.lineleveldimensions = lineleveldimensions;
    }

    public User getUserid() {
        return userid;
    }

    public void setUserid(User userid) {
        this.userid = userid;
    }
}
