/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

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
public class CycleCountDetail {

    private String id;
    private CycleCount cycleCount;
    private Location location;
    private StoreMaster row;
    private StoreMaster rack;
    private StoreMaster bin;
    private String batchName;
    private String actualSerials;
    private String systemSerials;
    private String actualSerialsSku;
    private String systemSerialsSku;
    private double actualQuantity;
    private double systemQuantity;

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public CycleCount getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(CycleCount cycleCount) {
        this.cycleCount = cycleCount;
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

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getActualSerials() {
        return actualSerials;
    }

    public void setActualSerials(String actualSerials) {
        this.actualSerials = actualSerials;
    }

    public String getSystemSerials() {
        return systemSerials;
    }

    public void setSystemSerials(String systemSerials) {
        this.systemSerials = systemSerials;
    }

    public double getSystemQuantity() {
        return systemQuantity;
    }

    public void setSystemQuantity(double systemQuantity) {
        this.systemQuantity = systemQuantity;
    }

    public double getQtyVariance() {
        return actualQuantity - systemQuantity;
    }

    public String getActualSerialsSku() {
        return actualSerialsSku;
    }

    public void setActualSerialsSku(String actualSerialsSku) {
        this.actualSerialsSku = actualSerialsSku;
    }

    public String getSystemSerialsSku() {
        return systemSerialsSku;
    }

    public void setSystemSerialsSku(String systemSerialsSku) {
        this.systemSerialsSku = systemSerialsSku;
    }
    
    public double getAddedQuantityVariance() {
        double addedQuantity = actualQuantity - systemQuantity;    
        if (cycleCount.getProduct().isIsSerialForProduct()){
            String serials = getAddedSerialVariance();
            if(!StringUtil.isNullOrEmpty(serials)){
                addedQuantity = serials.split(",").length;
            }else{
                addedQuantity = 0;
            }
        }
        if(addedQuantity < 0){
            addedQuantity = 0;
        }
        return addedQuantity;
    }
    public double getRemovedQuantityVariance() {
        double removedQuantity = systemQuantity - actualQuantity;
        if (cycleCount.getProduct().isIsSerialForProduct()){
            String serials = getRemovedSerialVariance();
            if(!StringUtil.isNullOrEmpty(serials)){
                removedQuantity = serials.split(",").length;
            }else{
                removedQuantity = 0;
            }
        }
        if(removedQuantity < 0){
            removedQuantity = 0;
        }
        return removedQuantity;
    }

    public String getAddedSerialVariance() {
        String serialVariance = "";
        if (cycleCount.getProduct().isIsSerialForProduct() && !StringUtil.isNullOrEmpty(actualSerials)) {
            Set<String> countedSerialSet = new HashSet<>(Arrays.asList(actualSerials.split(",")));
            if (!StringUtil.isNullOrEmpty(systemSerials)) {
                Set<String> systemSerialSet = new HashSet<>(Arrays.asList(systemSerials.split(",")));
                countedSerialSet.removeAll(systemSerialSet);
            }
            for (String serial : countedSerialSet) {
                if (StringUtil.isNullOrEmpty(serialVariance)) {
                    serialVariance = serial;
                } else {
                    serialVariance += "," + serial;
                }
            }
        }
        return serialVariance;
    }
    public String getRemovedSerialVariance() {
        String serialVariance = "";
        if (cycleCount.getProduct().isIsSerialForProduct() &&  !StringUtil.isNullOrEmpty(systemSerials)) {
            Set<String> systemSerialSet = new HashSet<>(Arrays.asList(systemSerials.split(",")));
            if (!StringUtil.isNullOrEmpty(actualSerials)) {
                Set<String> countedSerialSet = new HashSet<>(Arrays.asList(actualSerials.split(",")));
                systemSerialSet.removeAll(countedSerialSet);
            }
            for (String serial : systemSerialSet) {
                if (StringUtil.isNullOrEmpty(serialVariance)) {
                    serialVariance = serial;
                } else {
                    serialVariance += "," + serial;
                }
            }
        }
        return serialVariance;
    }
}
