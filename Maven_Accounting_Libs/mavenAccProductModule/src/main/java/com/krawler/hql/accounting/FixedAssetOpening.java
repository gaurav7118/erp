package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;
import java.util.Set;

public class FixedAssetOpening {

    private String id;
    private String documentNumber;
    private Date creationDate;
    private double quantity;
    private double rate;
    private double wdv;
    private Company company;
    private Product product;// parent table for asset
    private Inventory inventory;

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getWdv() {
        return wdv;
    }

    public void setWdv(double wdv) {
        this.wdv = wdv;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
