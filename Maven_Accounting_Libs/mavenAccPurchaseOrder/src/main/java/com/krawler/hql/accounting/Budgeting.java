/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;

/**
 *
 * @author krawler
 */
public class Budgeting {
    
    public final static String FREQUENCY_TYPE_MONTHLY = "0";
    public final static String FREQUENCY_TYPE_BIMONTHLY = "1";
    public final static String FREQUENCY_TYPE_QUARTERLY = "2";
    public final static String FREQUENCY_TYPE_HALF_YEARLY = "3";
    public final static String FREQUENCY_TYPE_YEARLY = "4";

    private String ID;
    private MasterItem department;
    private Product product;
    private MasterItem productCategory;
    private String frequencyType; // 0 -> Monthly, 1 -> Bi-Monthly, 2 -> Quarterly, 3 -> Half Yearly, 4 -> Yearly
    private String frequencyColumn; // if frequencyType == 0 || 1 || 2|| 3 then only frequencyColumn is added else having null
    private double amount;
    private String year; // if frequencyType == 4 then only year is added else having null
    private Company company;
    private FieldComboData dimensionValue; // for budgeting on dimensions

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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public MasterItem getDepartment() {
        return department;
    }

    public void setDepartment(MasterItem department) {
        this.department = department;
    }

    public String getFrequencyColumn() {
        return frequencyColumn;
    }

    public void setFrequencyColumn(String frequencyColumn) {
        this.frequencyColumn = frequencyColumn;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public MasterItem getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(MasterItem productCategory) {
        this.productCategory = productCategory;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public FieldComboData getDimensionValue() {
        return dimensionValue;
    }

    public void setDimensionValue(FieldComboData dimensionValue) {
        this.dimensionValue = dimensionValue;
    }
}