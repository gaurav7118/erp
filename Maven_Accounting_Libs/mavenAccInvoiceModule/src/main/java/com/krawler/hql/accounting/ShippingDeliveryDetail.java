/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.stockout.ShippingDeliveryOrder;
import com.krawler.inventory.model.stockout.StockAdjustment;

public class ShippingDeliveryDetail {

    private String ID;
    private Product product;
    private double actualQuantity;
    private double shipQuantity;
    private Company company;
    private ShippingDeliveryOrder shippingDeliveryOrder;
    private StockAdjustment stockAdjustment;
    private DeliveryOrderDetail deliveryOrderDetail;

    public DeliveryOrderDetail getDeliveryOrderDetail() {
        return deliveryOrderDetail;
    }

    public void setDeliveryOrderDetail(DeliveryOrderDetail deliveryOrderDetail) {
        this.deliveryOrderDetail = deliveryOrderDetail;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public double getShipQuantity() {
        return shipQuantity;
    }

    public void setShipQuantity(double shipQuantity) {
        this.shipQuantity = shipQuantity;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ShippingDeliveryOrder getShippingDeliveryOrder() {
        return shippingDeliveryOrder;
    }

    public void setShippingDeliveryOrder(ShippingDeliveryOrder shippingDeliveryOrder) {
        this.shippingDeliveryOrder = shippingDeliveryOrder;
    }

    public StockAdjustment getStockAdjustment() {
        return stockAdjustment;
    }

    public void setStockAdjustment(StockAdjustment stockAdjustment) {
        this.stockAdjustment = stockAdjustment;
    }
}
