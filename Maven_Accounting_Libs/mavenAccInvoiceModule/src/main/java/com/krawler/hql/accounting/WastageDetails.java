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

/**
 *
 * @author krawler
 */
public class WastageDetails {

    private String ID;
    private Product product;
    private double quantity;
    private double percentage;
    private double actualQuantity;
    private int wastageQuantityType; // 0 - 'Flat', 1 - 'Percentage'
    private double wastageQuantity;
    private DeliveryOrderDetail deliveryOrderDetail;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getWastageQuantity() {
        return wastageQuantity;
    }

    public void setWastageQuantity(double wastageQuantity) {
        this.wastageQuantity = wastageQuantity;
    }

    public int getWastageQuantityType() {
        return wastageQuantityType;
    }

    public void setWastageQuantityType(int wastageQuantityType) {
        this.wastageQuantityType = wastageQuantityType;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public DeliveryOrderDetail getDeliveryOrderDetail() {
        return deliveryOrderDetail;
    }

    public void setDeliveryOrderDetail(DeliveryOrderDetail deliveryOrderDetail) {
        this.deliveryOrderDetail = deliveryOrderDetail;
    }
}