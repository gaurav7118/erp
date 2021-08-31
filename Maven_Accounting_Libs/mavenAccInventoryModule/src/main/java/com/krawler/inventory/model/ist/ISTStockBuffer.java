/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.inventory.model.location.Location;

/**
 *
 * @author Vipin Gupta
 */
public class ISTStockBuffer {
    
    private String id;
    private ISTDetail istd;
    private Location location;
    private double quantity;
    private double pricePerUnit;
    private long batchNo;

    public ISTStockBuffer() {
    }
    
    public ISTStockBuffer(ISTDetail istd, Location location, double quantity, double pricePerUnit, long batchNo) {
        this.istd = istd;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.batchNo = batchNo;
        this.location = location;
    }

    public ISTDetail getIstd() {
        return istd;
    }

    public void setIstd(ISTDetail istd) {
        this.istd = istd;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(long batchNo) {
        this.batchNo = batchNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    
    
}
