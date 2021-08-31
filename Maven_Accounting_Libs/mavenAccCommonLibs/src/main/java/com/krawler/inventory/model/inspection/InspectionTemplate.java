/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.inspection;

import com.krawler.common.admin.Company;

/**
 *
 * @author Vipin Gupta
 */
public class InspectionTemplate {

    private String id;
    private String name;
    private String description;
    private Company company;

    public InspectionTemplate() {
    }
    
    public InspectionTemplate(String name, Company company) {
        this.name = name;
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
