/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.configuration;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.util.BatchType;
import com.krawler.common.util.InventoryCheck;
import java.util.Date;

/**
 *
 * @author Vipin Gupta
 */
public class InventoryConfig {

    private String id;
    private Company company;
    private BatchType stockBatchType;
    private InventoryCheck negativeInventoryCheckType;
    private boolean enableStockRequestApprovalFlow;
    private boolean enableStockAdjustmentApprovalFlow;
    private boolean enableStockoutApprovalFlow;
    private boolean enableISTReturnApprovalFlow;
    private boolean enableSRReturnApprovalFlow;
    private User createdBy;
    private User modifiedBy;
    private Date createdOn;
    private Date modifiedOn;

    public InventoryConfig() {
        this.stockBatchType = BatchType.LIFO;
        this.negativeInventoryCheckType = InventoryCheck.ALLOW;
        this.enableStockAdjustmentApprovalFlow = false;
        this.enableStockRequestApprovalFlow = false;
        this.enableStockoutApprovalFlow = false;
        this.enableISTReturnApprovalFlow = false;
        this.enableSRReturnApprovalFlow = false;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isEnableStockAdjustmentApprovalFlow() {
        return enableStockAdjustmentApprovalFlow;
    }

    public void setEnableStockAdjustmentApprovalFlow(boolean enableStockAdjustmentApprovalFlow) {
        this.enableStockAdjustmentApprovalFlow = enableStockAdjustmentApprovalFlow;
    }

    public boolean isEnableStockRequestApprovalFlow() {
        return enableStockRequestApprovalFlow;
    }

    public void setEnableStockRequestApprovalFlow(boolean enableStockRequestApprovalFlow) {
        this.enableStockRequestApprovalFlow = enableStockRequestApprovalFlow;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InventoryCheck getNegativeInventoryCheckType() {
        return negativeInventoryCheckType;
    }

    public void setNegativeInventoryCheckType(InventoryCheck negativeInventoryCheckType) {
        this.negativeInventoryCheckType = negativeInventoryCheckType;
    }

    public BatchType getStockBatchType() {
        return stockBatchType;
    }

    public void setStockBatchType(BatchType stockBatchType) {
        this.stockBatchType = stockBatchType;
    }

    public boolean isEnableISTReturnApprovalFlow() {
        return enableISTReturnApprovalFlow;
    }

    public void setEnableISTReturnApprovalFlow(boolean enableISTReturnApprovalFlow) {
        this.enableISTReturnApprovalFlow = enableISTReturnApprovalFlow;
    }

    public boolean isEnableStockoutApprovalFlow() {
        return enableStockoutApprovalFlow;
    }

    public void setEnableStockoutApprovalFlow(boolean enableStockoutApprovalFlow) {
        this.enableStockoutApprovalFlow = enableStockoutApprovalFlow;
    }

    public boolean isEnableSRReturnApprovalFlow() {
        return enableSRReturnApprovalFlow;
    }

    public void setEnableSRReturnApprovalFlow(boolean enableSRReturnApprovalFlow) {
        this.enableSRReturnApprovalFlow = enableSRReturnApprovalFlow;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public User getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(User modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }
}
