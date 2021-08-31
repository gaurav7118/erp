/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class DoDetails {
    private String ID;
    private Product product;
    private double actualQuantity;
    private double packQuantity;
    private Company company;
    private Packing packing;
    private PackingDetail packingDetails;
    private DeliveryOrder deliveryOrder;

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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }
    
    public PackingDetail getPackingDetails() {
        return packingDetails;
    }

    public void setPackingDetails(PackingDetail packingDetails) {
        this.packingDetails = packingDetails;
    }

    public double getPackQuantity() {
        return packQuantity;
    }

    public void setPackQuantity(double packQuantity) {
        this.packQuantity = packQuantity;
    }

    public Packing getPacking() {
        return packing;
    }

    public void setPacking(Packing packing) {
        this.packing = packing;
    }
    
}
