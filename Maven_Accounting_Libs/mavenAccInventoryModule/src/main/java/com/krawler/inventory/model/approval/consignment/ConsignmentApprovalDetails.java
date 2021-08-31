/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.consignment;

import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.admin.User;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.location.Location;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class ConsignmentApprovalDetails {

    private String id;
    private Location location;
    private String serialName;
    private String batchName;
    private double quantity;
    private NewProductBatch batch;
    private NewBatchSerial purchaseSerialId;
    private ApprovalStatus approvalStatus;
    private InspectionDetail inspectionDTL;
    private Consignment consignment;
    private String remark;
    private boolean movementStatus;
    private Date modifiedOn;
    private Date repairedOn;
    private User inspector;
    private ApprovalStatus repairStatus;
    private double retQty;
    private String reason;
    private InspectionForm inspectionForm;

    public ConsignmentApprovalDetails() {
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InspectionDetail getInspectionDTL() {
        return inspectionDTL;
    }

    public void setInspectionDTL(InspectionDetail inspectionDTL) {
        this.inspectionDTL = inspectionDTL;
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

    public String getSerialName() {
        return serialName;
    }

    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }

    public Consignment getConsignment() {
        return consignment;
    }

    public void setConsignment(Consignment consignment) {
        this.consignment = consignment;
    }

    public NewProductBatch getBatch() {
        return batch;
    }

    public void setBatch(NewProductBatch batch) {
        this.batch = batch;
    }

    public NewBatchSerial getPurchaseSerialId() {
        return purchaseSerialId;
    }

    public void setPurchaseSerialId(NewBatchSerial purchaseSerialId) {
        this.purchaseSerialId = purchaseSerialId;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isMovementStatus() {
        return movementStatus;
    }

    public void setMovementStatus(boolean movementStatus) {
        this.movementStatus = movementStatus;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public User getInspector() {
        return inspector;
    }

    public void setInspector(User inspector) {
        this.inspector = inspector;
    }

    public ApprovalStatus getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(ApprovalStatus repairStatus) {
        this.repairStatus = repairStatus;
    }

    public double getRetQty() {
        return retQty;
    }

    public void setRetQty(double retQty) {
        this.retQty = retQty;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getRepairedOn() {
        return repairedOn;
    }

    public void setRepairedOn(Date repairedOn) {
        this.repairedOn = repairedOn;
    }
    
    public InspectionForm getInspectionForm() {
        return inspectionForm;
    }

    public void setInspectionForm(InspectionForm inspectionForm) {
        this.inspectionForm = inspectionForm;
    }
   
}
