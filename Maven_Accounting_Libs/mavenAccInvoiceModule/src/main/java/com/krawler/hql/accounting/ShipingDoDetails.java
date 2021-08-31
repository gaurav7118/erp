/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class ShipingDoDetails {
    private String ID;
    private Product product;
    private double actualQuantity;
    private double shipQuantity;
    private double shipedQuantity;
    private Company company;
    private PackingDoList packingDoList;
    private DeliveryOrder deliveryOrder;
    private PackingDoListDetail packingDoListDetails;
    
    public double getShipedQuantity() {
        return shipedQuantity;
    }

    public void setShipedQuantity(double shipedQuantity) {
        this.shipedQuantity = shipedQuantity;
    }
    
    public void setID(String ID) {
        this.ID = ID;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public void setPackingDoList(PackingDoList packingDoList) {
        this.packingDoList = packingDoList;
    }

    public void setPackingDoListDetails(PackingDoListDetail packingDoListDetails) {
        this.packingDoListDetails = packingDoListDetails;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setShipQuantity(double shipQuantity) {
        this.shipQuantity = shipQuantity;
    }

    public String getID() {
        return ID;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public Company getCompany() {
        return company;
    }

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public PackingDoList getPackingDoList() {
        return packingDoList;
    }

    public PackingDoListDetail getPackingDoListDetails() {
        return packingDoListDetails;
    }

    public Product getProduct() {
        return product;
    }

    public double getShipQuantity() {
        return shipQuantity;
    }
    
}
