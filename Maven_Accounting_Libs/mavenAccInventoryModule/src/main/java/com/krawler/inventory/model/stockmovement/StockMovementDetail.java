/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockmovement;

import com.krawler.common.admin.StoreMaster;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.location.Location;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class StockMovementDetail {

    private String id;
    private StockMovement stockMovement;
    private Location location;
    private StoreMaster row;
    private StoreMaster rack;
    private StoreMaster bin;
    private String batchName;
    private String serialNames;
    private double quantity;

    public StockMovementDetail() {
    }

    public StockMovementDetail(StockMovement stockMovement, Location location, String batchName, String serialNames, double quantity) {
        this();
        this.stockMovement = stockMovement;
        this.location = location;
        this.batchName = batchName;
        this.serialNames = serialNames;
        this.quantity = quantity;
    }
    public StockMovementDetail(StockMovement stockMovement, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin,String batchName, String serialNames, double quantity) {
        this(stockMovement, location, batchName, serialNames, quantity);
        this.row = row;
        this.rack = rack;
        this.bin = bin;
    }

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

    public StockMovement getStockMovement() {
        return stockMovement;
    }

    public void setStockMovement(StockMovement stockMovement) {
        this.stockMovement = stockMovement;
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
        if (StringUtil.isNullOrEmpty(serialName)) {
            return;
        }
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
