/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.common.admin.StoreMaster;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.location.Location;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class ISTDetail {

    private String id;
    private InterStoreTransferRequest istRequest;
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
    private double qaApproved;
    private double qaRejected;
    private double approvedQtyFromRepairStore;
    private double rejectedQtyFromRepairStore;
    private InspectionForm inspectionForm;
    private String expdate;
    
    public InterStoreTransferRequest getIstRequest() {
        return istRequest;
    }

    public void setIstRequest(InterStoreTransferRequest istRequest) {
        this.istRequest = istRequest;
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

    public InspectionForm getInspectionForm() {
        return inspectionForm;
    }

    public void setInspectionForm(InspectionForm inspectionForm) {
        this.inspectionForm = inspectionForm;
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

    public ISTDetail getReturnStockDetail() {
        ISTDetail istd = null;
        double returnQty = getReturnQuantity();
        if (returnQty > 0) {
            istd = new ISTDetail();
            istd.setIssuedLocation(issuedLocation);
            istd.setDeliveredLocation(issuedLocation);
            istd.setIssuedRow(issuedRow);
            istd.setDeliveredRow(issuedRow);
            istd.setIssuedRack(issuedRack);
            istd.setDeliveredRack(issuedRack);
            istd.setIssuedBin(issuedBin);
            istd.setDeliveredBin(issuedBin);
            istd.setBatchName(batchName);

            String returnSerialNames = getReturnSerialNames();
            istd.setDeliveredSerialNames(returnSerialNames);
            istd.setIssuedSerialNames(returnSerialNames);

            istd.setIssuedQuantity(returnQty);
            istd.setDeliveredQuantity(returnQty);
        }
        return istd;
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

    public double getQaApproved() {
        return qaApproved;
    }

    public void setQaApproved(double qaApproved) {
        this.qaApproved = qaApproved;
    }

    public double getQaRejected() {
        return qaRejected;
    }

    public void setQaRejected(double qaRejected) {
        this.qaRejected = qaRejected;
    }

    public double getApprovedQtyFromRepairStore() {
        return approvedQtyFromRepairStore;
    }

    public void setApprovedQtyFromRepairStore(double approvedQtyFromRepairStore) {
        this.approvedQtyFromRepairStore = approvedQtyFromRepairStore;
    }

    public double getRejectedQtyFromRepairStore() {
        return rejectedQtyFromRepairStore;
    }

    public void setRejectedQtyFromRepairStore(double rejectedQtyFromRepairStore) {
        this.rejectedQtyFromRepairStore = rejectedQtyFromRepairStore;
    }
    
     public String getExpdate() {
        return expdate;
    }

    public void setExpdate(String expdate) {
        this.expdate = expdate;
    }
}
