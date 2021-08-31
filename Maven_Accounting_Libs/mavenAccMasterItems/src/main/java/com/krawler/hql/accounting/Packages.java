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
public class Packages {
    
    private String packageid;
    private String packagename;
    private String measurement;
    private double packageweight;
    private Company company;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public void setPackageid(String packageid) {
        this.packageid = packageid;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public void setPackageweight(double packageweight) {
        this.packageweight = packageweight;
    }

    public String getMeasurement() {
        return measurement;
    }

    public String getPackageid() {
        return packageid;
    }

    public String getPackagename() {
        return packagename;
    }

    public double getPackageweight() {
        return packageweight;
    }
 
}
