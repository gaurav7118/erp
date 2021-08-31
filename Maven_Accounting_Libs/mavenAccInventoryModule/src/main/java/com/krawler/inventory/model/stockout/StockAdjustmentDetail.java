/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout;

import com.krawler.common.admin.StoreMaster;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.location.Location;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class StockAdjustmentDetail {

    private String id;
    private StockAdjustment stockAdjustment;
    private Location location;
    private StoreMaster row;
    private StoreMaster rack;
    private StoreMaster bin;
    private String batchName;
    private SalesOrder jobworkorder;  // Job Work Order No to track usage of Job work Order in Kob Work IN
    private String serialNames;
    private double quantity;
    private String finalSerialNames;
    private double finalQuantity;
    private InspectionForm inspectionForm;

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

    public StockAdjustment getStockAdjustment() {
        return stockAdjustment;
    }

    public void setStockAdjustment(StockAdjustment stockAdjustment) {
        this.stockAdjustment = stockAdjustment;
    }

    public double getFinalQuantity() {
        return finalQuantity;
    }

    public void setFinalQuantity(double finalQuantity) {
        this.finalQuantity = finalQuantity;
    }

    public String getFinalSerialNames() {
        return finalSerialNames;
    }

    public void setFinalSerialNames(String finalSerialNames) {
        this.finalSerialNames = finalSerialNames;
    }

    public double getReturnQuantity() {
        return quantity - finalQuantity;
    }

    public SalesOrder getJobworkorder() {
        return jobworkorder;
    }

    public void setJobworkorder(SalesOrder jobworkorder) {
        this.jobworkorder = jobworkorder;
    }

    public InspectionForm getInspectionForm() {
        return inspectionForm;
    }

    public void setInspectionForm(InspectionForm inspectionForm) {
        this.inspectionForm = inspectionForm;
    }
    
    public void addFinalSerialName(String finalSerialName) {
        if (StringUtil.isNullOrEmpty(finalSerialName)) {
            return;
        }
        if (StringUtil.isNullOrEmpty(this.finalSerialNames)) {
            this.finalSerialNames = finalSerialName;
        } else {
            Set<String> serialSet = new HashSet<String>(Arrays.asList(this.finalSerialNames.split(",")));
            if (!serialSet.contains(finalSerialName)) {
                this.finalSerialNames += "," + finalSerialName;
            }
        }
    }

    public String getReturnSerialNames() {
        String returnSerialNames = null;
        if (!StringUtil.isNullOrEmpty(this.serialNames)) {
            if (StringUtil.isNullOrEmpty(this.finalSerialNames)) {
                returnSerialNames = this.serialNames;
            } else {
                String[] iSerialNames = this.serialNames.split(",");
                String[] cSerialNames = this.finalSerialNames.split(",");
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

    public void removeSerialNameFromFinal(String serialName) {
        if (StringUtil.isNullOrEmpty(serialName) || StringUtil.isNullOrEmpty(finalSerialNames)) {
            return;
        } else {
            Set<String> serialSet = new HashSet<String>(Arrays.asList(finalSerialNames.split(",")));
            if (serialSet.contains(serialName)) {
                serialSet.remove(serialName);
            }
            finalSerialNames = "";
            for (String sn : serialSet) {
                if (StringUtil.isNullOrEmpty(finalSerialNames)) {
                    finalSerialNames = sn;
                } else {
                    finalSerialNames += "," + sn;
                }
            }
        }
    }
}
