/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class PackingDoListDetail {
    private String ID;
    private int srno;
    private Product product;
    private double actualQuantity;
    private double deliveredQuantity;
    private String description;
    private String remark;
    private Company company;
    private PackingDoList packingDoList;
    private Set<ItemPackingDetail> packingdetails;
    private Set<ShipingDoDetails> shipingdodetails;

    public Set<ShipingDoDetails> getShipingdodetails() {
        return shipingdodetails;
    }

    public void setShipingdodetails(Set<ShipingDoDetails> shipingdodetails) {
        this.shipingdodetails = shipingdodetails;
    }
    
    public Set<ItemPackingDetail> getPackingdetails() {
        return packingdetails;
    }

    public void setPackingdetails(Set<ItemPackingDetail> packingdetails) {
        this.packingdetails = packingdetails;
    }

    private DeliveryOrder deliveryOrder;

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
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

    public void setDeliveredQuantity(double deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPackingDoList(PackingDoList packingDoList) {
        this.packingDoList = packingDoList;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setSrno(int srno) {
        this.srno = srno;
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

    public double getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public String getDescription() {
        return description;
    }

    public PackingDoList getPackingDoList() {
        return packingDoList;
    }

    public Product getProduct() {
        return product;
    }

    public String getRemark() {
        return remark;
    }

    public int getSrno() {
        return srno;
    }
}
