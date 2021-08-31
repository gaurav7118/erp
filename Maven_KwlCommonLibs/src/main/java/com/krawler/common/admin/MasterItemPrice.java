package com.krawler.common.admin;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler-user
 */
public class MasterItemPrice {

    private String ID;
    private String value;
    private double price;
    private PriceType type;
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

    public Company getCompany() {
        return company;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public PriceType getType() {
        return type;
    }

    public void setType(PriceType type) {
        this.type = type;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
