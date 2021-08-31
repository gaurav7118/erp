/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest;

import com.krawler.inventory.model.location.Location;

/**
 *
 * @author Vipin Gupta
 */
public class SRStockBuffer {

    private String id;
    private double quantity;
    private double pricePerUnit;
    private long batchNo;
    private Location location;
    private StockRequestDetail stockRequestDetail;

    public SRStockBuffer() {
    }

    public SRStockBuffer(StockRequestDetail stockRequestDetail, Location location, double quantity, double pricePerUnit, long batchNo) {
        this.location = location;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.batchNo = batchNo;
        this.stockRequestDetail = stockRequestDetail;
    }

    public StockRequestDetail getStockRequestDetail() {
        return stockRequestDetail;
    }

    public void setStockRequestDetail(StockRequestDetail stockRequestDetail) {
        this.stockRequestDetail = stockRequestDetail;
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


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SRStockBuffer other = (SRStockBuffer) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
