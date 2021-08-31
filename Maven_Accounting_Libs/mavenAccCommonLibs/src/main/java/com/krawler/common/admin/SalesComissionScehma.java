/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class SalesComissionScehma {

    private String ID;
    private double lowerlimit;
    private double upperlimit;
    private double percentageType;
    private double amount;
    private Company company;
    private String schemaItem;
    private String categoryid;//for Storing product category
    private String productId;
    private int commissiontype;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getLowerlimit() {
        return lowerlimit;
    }

    public void setLowerlimit(double lowerlimit) {
        this.lowerlimit = lowerlimit;
    }

    public double getPercentageType() {
        return percentageType;
    }

    public void setPercentageType(double percentageType) {
        this.percentageType = percentageType;
    }

    public double getUpperlimit() {
        return upperlimit;
    }

    public void setUpperlimit(double upperlimit) {
        this.upperlimit = upperlimit;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getSchemaItem() {
        return schemaItem;
    }

    public void setSchemaItem(String schemaItem) {
        this.schemaItem = schemaItem;
    }

    public int getCommissiontype() {
        return commissiontype;
    }

    public void setCommissiontype(int commissiontype) {
        this.commissiontype = commissiontype;
    }

    public String getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
