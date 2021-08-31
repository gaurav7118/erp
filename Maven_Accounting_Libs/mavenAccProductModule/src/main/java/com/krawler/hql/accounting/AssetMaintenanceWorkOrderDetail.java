/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductBatch;

/**
 *
 * @author krawler
 */
public class AssetMaintenanceWorkOrderDetail {

    private String ID;
    private int srno;
    private Product product;
//    private double actualQuantity;
    private double deliveredQuantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomdeliveredquantity;
    private double baseuomrate;
    private String description;
    private String remark;
    private Company company;
    private Inventory inventory;
    private ProductBatch batch;
    private double rate;
    private AssetMaintenanceWorkOrder assetMaintenanceWorkOrder;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

//    public double getActualQuantity() {
//        return actualQuantity;
//    }
//
//    public void setActualQuantity(double actualQuantity) {
//        this.actualQuantity = actualQuantity;
//    }

    public AssetMaintenanceWorkOrder getAssetMaintenanceWorkOrder() {
        return assetMaintenanceWorkOrder;
    }

    public void setAssetMaintenanceWorkOrder(AssetMaintenanceWorkOrder assetMaintenanceWorkOrder) {
        this.assetMaintenanceWorkOrder = assetMaintenanceWorkOrder;
    }

    public double getBaseuomdeliveredquantity() {
        return baseuomdeliveredquantity;
    }

    public void setBaseuomdeliveredquantity(double baseuomdeliveredquantity) {
        this.baseuomdeliveredquantity = baseuomdeliveredquantity;
    }

    public double getBaseuomquantity() {
        return baseuomquantity;
    }

    public void setBaseuomquantity(double baseuomquantity) {
        this.baseuomquantity = baseuomquantity;
    }

    public double getBaseuomrate() {
        return baseuomrate;
    }

    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }

    public ProductBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductBatch batch) {
        this.batch = batch;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public double getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(double deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }
}
