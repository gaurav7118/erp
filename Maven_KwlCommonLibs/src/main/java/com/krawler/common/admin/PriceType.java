package com.krawler.common.admin;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler-user
 */
public class PriceType {

    private String ID;
    private String value;
    private int type;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
