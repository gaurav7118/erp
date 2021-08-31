/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.threshold;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.store.Store;

/**
 *
 * @author Vipin Gupta
 */
public class ProductThreshold {
    
    private String id;
    private Company company;
    private Product product;
    private Store store;
    private double thresholdLimit;

    public ProductThreshold() {
    }

    public ProductThreshold(Product product, Store store, double thresholdLimit) {
        this.company = product.getCompany();
        this.product = product;
        this.store = store;
        this.thresholdLimit = thresholdLimit;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public double getThresholdLimit() {
        return thresholdLimit;
    }

    public void setThresholdLimit(double thresholdLimit) {
        this.thresholdLimit = thresholdLimit;
    }
    
}
