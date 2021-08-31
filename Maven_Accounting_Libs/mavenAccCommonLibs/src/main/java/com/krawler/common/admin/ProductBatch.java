/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class ProductBatch {

    private String id;
    private String name;
    private Date mfgdate;
    private Date expdate;
    private double quantity;
    private double balance;
    private InventoryLocation location;
    private InventoryWarehouse warehouse;
    private String product;
    private String asset;
    private Company company;
    private boolean isopening;
    private boolean ispurchase;
    private int transactiontype;

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getExpdate() {
        return expdate;
    }

    public void setExpdate(Date expdate) {
        this.expdate = expdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InventoryLocation getLocation() {
        return location;
    }

    public void setLocation(InventoryLocation location) {
        this.location = location;
    }

    public Date getMfgdate() {
        return mfgdate;
    }

    public void setMfgdate(Date mfgdate) {
        this.mfgdate = mfgdate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public InventoryWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(InventoryWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    public boolean isIsopening() {
        return isopening;
    }

    public void setIsopening(boolean isopening) {
        this.isopening = isopening;
    }

    public boolean isIspurchase() {
        return ispurchase;
    }

    public void setIspurchase(boolean ispurchase) {
        this.ispurchase = ispurchase;
    }

    public int getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(int transactiontype) {
        this.transactiontype = transactiontype;
    }
}
