/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.stocktransfer;

import com.krawler.common.admin.User;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.inspection.InspectionForm;
import java.util.Date;

/**
 *
 * @author Vipin Gupta
 */
public class StockTransferDetailApproval {

    private String id;
    private String stockTransferDetailId;
    private ApprovalStatus approvalStatus;
    private String serialName;
    private double quantity;
    private StockTransferApproval stockTransferApproval;
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

    public StockTransferDetailApproval() {
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

    public String getSerialName() {
        return serialName;
    }

    public void setSerialName(String serialName) {
        this.serialName = serialName;
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

    public StockTransferApproval getStockTransferApproval() {
        return stockTransferApproval;
    }

    public void setStockTransferApproval(StockTransferApproval stockTransferApproval) {
        this.stockTransferApproval = stockTransferApproval;
    }

    public String getStockTransferDetailId() {
        return stockTransferDetailId;
    }

    public void setStockTransferDetailId(String stockTransferDetailId) {
        this.stockTransferDetailId = stockTransferDetailId;
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
