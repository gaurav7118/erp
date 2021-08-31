/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest;

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
public class StockRequestDetail {

    private String id;
    private StockRequest stockRequest;
    private Location issuedLocation;
    private Location deliveredLocation;
    private StoreMaster issuedRow;
    private StoreMaster deliveredRow;
    private StoreMaster issuedRack;
    private StoreMaster deliveredRack;
    private StoreMaster issuedBin;
    private StoreMaster deliveredBin;
    private String batchName;
    private String issuedSerialNames;
    private String deliveredSerialNames;
    private double issuedQuantity;
    private double deliveredQuantity;

    public StockRequest getStockRequest() {
        return stockRequest;
    }

    public void setStockRequest(StockRequest stockRequest) {
        this.stockRequest = stockRequest;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public Location getDeliveredLocation() {
        return deliveredLocation;
    }

    public void setDeliveredLocation(Location deliveredLocation) {
        this.deliveredLocation = deliveredLocation;
    }

    public StoreMaster getDeliveredBin() {
        return deliveredBin;
    }

    public void setDeliveredBin(StoreMaster deliveredBin) {
        this.deliveredBin = deliveredBin;
    }

    public StoreMaster getDeliveredRack() {
        return deliveredRack;
    }

    public void setDeliveredRack(StoreMaster deliveredRack) {
        this.deliveredRack = deliveredRack;
    }

    public StoreMaster getDeliveredRow() {
        return deliveredRow;
    }

    public void setDeliveredRow(StoreMaster deliveredRow) {
        this.deliveredRow = deliveredRow;
    }

    public StoreMaster getIssuedBin() {
        return issuedBin;
    }

    public void setIssuedBin(StoreMaster issuedBin) {
        this.issuedBin = issuedBin;
    }

    public StoreMaster getIssuedRack() {
        return issuedRack;
    }

    public void setIssuedRack(StoreMaster issuedRack) {
        this.issuedRack = issuedRack;
    }

    public StoreMaster getIssuedRow() {
        return issuedRow;
    }

    public void setIssuedRow(StoreMaster issuedRow) {
        this.issuedRow = issuedRow;
    }

    public double getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(double deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public String getDeliveredSerialNames() {
        return deliveredSerialNames;
    }

    public void setDeliveredSerialNames(String deliveredSerialNames) {
        this.deliveredSerialNames = deliveredSerialNames;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getIssuedLocation() {
        return issuedLocation;
    }

    public void setIssuedLocation(Location issuedLocation) {
        this.issuedLocation = issuedLocation;
    }

    public double getIssuedQuantity() {
        return issuedQuantity;
    }

    public void setIssuedQuantity(double issuedQuantity) {
        this.issuedQuantity = issuedQuantity;
    }

    public String getIssuedSerialNames() {
        return issuedSerialNames;
    }

    public void setIssuedSerialNames(String issuedSerialNames) {
        this.issuedSerialNames = issuedSerialNames;
    }

    public double getReturnQuantity() {
        return this.issuedQuantity - this.deliveredQuantity;
    }

    public void addDeliveredSerialName(String deliveredSerialName) {
        if (StringUtil.isNullOrEmpty(deliveredSerialName)) {
            return;
        }
        if (StringUtil.isNullOrEmpty(this.deliveredSerialNames)) {
            this.deliveredSerialNames = deliveredSerialName;
        } else {
            Set<String> serialSet = new HashSet<String>(Arrays.asList(this.deliveredSerialNames.split(",")));
            if (!serialSet.contains(deliveredSerialName)) {
                this.deliveredSerialNames += "," + deliveredSerialName;
            }
        }
    }

    public String getReturnSerialNames() {
        String returnSerialNames = null;
        if (!StringUtil.isNullOrEmpty(this.issuedSerialNames)) {
            if (StringUtil.isNullOrEmpty(this.deliveredSerialNames)) {
                returnSerialNames = this.issuedSerialNames;
            } else {
                String[] iSerialNames = this.issuedSerialNames.split(",");
                String[] cSerialNames = this.deliveredSerialNames.split(",");
                Set<String> issuedSerialSet = new HashSet<String>(Arrays.asList(iSerialNames));
                Set<String> collectedSerialSet = new HashSet<String>(Arrays.asList(cSerialNames));
                issuedSerialSet.removeAll(collectedSerialSet);
                for (String rSerialName : issuedSerialSet) {
                    if (StringUtil.isNullOrEmpty(returnSerialNames)) {
                        returnSerialNames = rSerialName;
                    } else {
                        returnSerialNames += "," + rSerialName;
                    }
                }
            }
        }
        return returnSerialNames;
    }

    public StockRequestDetail getReturnStockDetail() {
        StockRequestDetail srd = null;
        double returnQty = getReturnQuantity();
        if (returnQty > 0) {
            srd = new StockRequestDetail();
            srd.setIssuedLocation(issuedLocation);
            srd.setDeliveredLocation(issuedLocation);
            srd.setIssuedRow(issuedRow);
            srd.setDeliveredRow(issuedRow);
            srd.setIssuedRack(issuedRack);
            srd.setDeliveredRack(issuedRack);
            srd.setIssuedBin(issuedBin);
            srd.setDeliveredBin(issuedBin);
            srd.setBatchName(batchName);

            String returnSerialNames = getReturnSerialNames();
            srd.setDeliveredSerialNames(returnSerialNames);
            srd.setIssuedSerialNames(returnSerialNames);

            srd.setIssuedQuantity(returnQty);
            srd.setDeliveredQuantity(returnQty);
        }
        return srd;
    }

    public void removeFromDeliveredSerialName(String serialName) {
        if (StringUtil.isNullOrEmpty(serialName) || StringUtil.isNullOrEmpty(deliveredSerialNames)) {
            return;
        } else {
            Set<String> serialSet = new HashSet<String>(Arrays.asList(deliveredSerialNames.split(",")));
            if (serialSet.contains(serialName)) {
                serialSet.remove(serialName);
            }
            deliveredSerialNames = "";
            for (String sn : serialSet) {
                if (StringUtil.isNullOrEmpty(deliveredSerialNames)) {
                    deliveredSerialNames = sn;
                } else {
                    deliveredSerialNames += "," + sn;
                }
            }
        }
    }
}
