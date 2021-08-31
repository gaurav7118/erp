/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.mrp.jobwork;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.hql.accounting.Product;
import java.util.Date;

public class ForecastProductMapping{
    private String ID;
    private Company company;
    private ForecastTemplate forecastTemplate;

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

    public ForecastTemplate getForecastTemplate() {
        return forecastTemplate;
    }

    public void setForecastTemplate(ForecastTemplate forecastTemplate) {
        this.forecastTemplate = forecastTemplate;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
    private Product product;
}