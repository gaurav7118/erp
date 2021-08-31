/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

import com.krawler.common.admin.StoreMaster;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;

/**
 *
 * @author Vipin Gupta
 */
public class MovedSerialMailDetail {
    
    private Product product;
    private Store store;
    private Location location;
    private StoreMaster row;
    private StoreMaster rack;
    private StoreMaster bin;
    private String batchName;
    private String serialNames;

    public MovedSerialMailDetail() {
    }

    public MovedSerialMailDetail(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, String serialNames) {
        this.product = product;
        this.store = store;
        this.location = location;
        this.row = row;
        this.rack = rack;
        this.bin = bin;
        this.batchName = batchName;
        this.serialNames = serialNames;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getSerialNames() {
        return serialNames;
    }

    public void setSerialNames(String serialNames) {
        this.serialNames = serialNames;
    }

    public StoreMaster getBin() {
        return bin;
    }

    public void setBin(StoreMaster bin) {
        this.bin = bin;
    }

    public StoreMaster getRack() {
        return rack;
    }

    public void setRack(StoreMaster rack) {
        this.rack = rack;
    }

    public StoreMaster getRow() {
        return row;
    }

    public void setRow(StoreMaster row) {
        this.row = row;
    }
    
    
}
