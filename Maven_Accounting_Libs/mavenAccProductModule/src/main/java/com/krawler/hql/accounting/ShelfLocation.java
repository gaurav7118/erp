/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author Atul
 */
public class ShelfLocation {

    private String id;
    private String shelfLocationValue;
    private Company company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getShelfLocationValue() {
        return shelfLocationValue;
    }

    public void setShelfLocationValue(String shelfLocationValue) {
        this.shelfLocationValue = shelfLocationValue;
    }
}
