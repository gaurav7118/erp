/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stock;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.authHandler.authHandler;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class Stock {

    private String id;
    private Product product;
    private Store store;
    private Location location;
    private StoreMaster row;
    private StoreMaster rack;
    private StoreMaster bin;
    private long batchNo;
    private double quantity;
    private double pricePerUnit;
    private Date createdOn;
    private Date modifiedOn;
    private Company company;
    private String batchName;
    private String serialNames;

    public Stock(Product product, Store store, Location location, String batchName, double quantity) {
        this();
        this.company = product.getCompany();
        this.product = product;
        this.store = store;
        this.quantity = quantity;
        this.batchName = batchName;
        this.location = location;
    }
    public Stock(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName, double quantity) {
        this(product, store, location, batchName, quantity);
        this.row = row;
        this.rack = rack;
        this.bin = bin;
    }

    public Stock() {
        batchName = "";
        serialNames = "";
    }

    public String getSerialNames() {
        return serialNames;
    }

    public void setSerialNames(String serialNames) {
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

    public long getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(long batchNo) {
        this.batchNo = batchNo;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
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

    public void addSerialName(String serialName) {
        if (!StringUtil.isNullOrEmpty(serialName)) {
            if (StringUtil.isNullOrEmpty(this.serialNames)) {
                this.serialNames = serialName;
            } else {
                Set<String> serialSet = new HashSet<String>(Arrays.asList(this.serialNames.split(",")));
                if (!serialSet.contains(serialName)) {
                    this.serialNames += "," + serialName;
                }
            }
        }
    }

    public void removeSerialName(String serialName) {
        if (!StringUtil.isNullOrEmpty(serialName) && !StringUtil.isNullOrEmpty(this.serialNames)) {
            Set<String> serialSet = new HashSet<String>(Arrays.asList(this.serialNames.split(",")));
            if (serialSet.contains(serialName)) {
                serialSet.remove(serialName);
            }
            this.serialNames = "";
            for (String sn : serialSet) {
                if (StringUtil.isNullOrEmpty(this.serialNames)) {
                    this.serialNames = sn;
                } else {
                    this.serialNames += "," + sn;
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Stock other = (Stock) obj;
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
