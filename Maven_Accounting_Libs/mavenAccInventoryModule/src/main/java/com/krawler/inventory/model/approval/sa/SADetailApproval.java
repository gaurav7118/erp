/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.sa;

import com.krawler.common.admin.User;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.stockout.StockAdjustmentDetail;
import java.util.Date;

/**
 *
 * @author Vipin Gupta
 */
public class SADetailApproval {

    private String id;
    private StockAdjustmentDetail stockAdjustmentDetail;
    private ApprovalStatus approvalStatus;
    private String serialName;
    private double quantity;
    private SAApproval saApproval;
    private InspectionDetail inspectionDetail;
    private User inspector;
    private String remark;
    private boolean movementStatus;// true - Rejected store transferred or stock issue, false - Default value for rejected only
    private Date modifiedOn;
    private Date repairedOn;
    private ApprovalStatus repairStatus;
    private double retQty;
    private String reason;
    private InspectionForm inspectionForm;

    public SADetailApproval() {
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

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public SAApproval getSaApproval() {
        return saApproval;
    }

    public void setSaApproval(SAApproval saApproval) {
        this.saApproval = saApproval;
    }

    public String getSerialName() {
        return serialName;
    }

    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }

    public StockAdjustmentDetail getStockAdjustmentDetail() {
        return stockAdjustmentDetail;
    }

    public void setStockAdjustmentDetail(StockAdjustmentDetail stockAdjustmentDetail) {
        this.stockAdjustmentDetail = stockAdjustmentDetail;
    }

    public InspectionDetail getInspectionDetail() {
        return inspectionDetail;
    }

    public void setInspectionDetail(InspectionDetail inspectionDetail) {
        this.inspectionDetail = inspectionDetail;
    }

    public User getInspector() {
        return inspector;
    }

    public void setInspector(User inspector) {
        this.inspector = inspector;
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
