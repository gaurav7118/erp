/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.booking;

import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;

/**
 *
 * @author Vipin Gupta
 */
public class StockBookingDetail {
    
    private String id;
    private Store store;
    private Location location;
    private String batchName;
    private String serialNames;
    private double quantity;
    private StockBooking stockBooking;

    
    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getSerialNames() {
        return serialNames;
    }

    public void setSerialNames(String serialNames) {
        this.serialNames = serialNames;
    }

    public StockBooking getStockBooking() {
        return stockBooking;
    }

    public void setStockBooking(StockBooking stockBooking) {
        this.stockBooking = stockBooking;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }
    
}
