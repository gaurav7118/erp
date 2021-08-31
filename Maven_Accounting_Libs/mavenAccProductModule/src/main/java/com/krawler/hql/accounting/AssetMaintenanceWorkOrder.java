/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class AssetMaintenanceWorkOrder {

    private String id;
    private String workOrderNumber;
    private Date workOrderDate;
    private Date startDate;
    private Date endDate;
    private KWLCurrency currency;
    private Company company;
    private MasterItem assignedTo;
    private String remark;
    private AssetMaintenanceScheduler assetMaintenanceScheduler;
    private Set<AssetMaintenanceWorkOrderDetail> maintenanceWorkOrderDetails;

    public MasterItem getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(MasterItem assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getWorkOrderNumber() {
        return workOrderNumber;
    }

    public void setWorkOrderNumber(String workOrderNumber) {
        this.workOrderNumber = workOrderNumber;
    }

    public Set<AssetMaintenanceWorkOrderDetail> getMaintenanceWorkOrderDetails() {
        return maintenanceWorkOrderDetails;
    }

    public void setMaintenanceWorkOrderDetails(Set<AssetMaintenanceWorkOrderDetail> maintenanceWorkOrderDetails) {
        this.maintenanceWorkOrderDetails = maintenanceWorkOrderDetails;
    }

    public AssetMaintenanceScheduler getAssetMaintenanceScheduler() {
        return assetMaintenanceScheduler;
    }

    public void setAssetMaintenanceScheduler(AssetMaintenanceScheduler assetMaintenanceScheduler) {
        this.assetMaintenanceScheduler = assetMaintenanceScheduler;
    }

    public Date getWorkOrderDate() {
        return workOrderDate;
    }

    public void setWorkOrderDate(Date workOrderDate) {
        this.workOrderDate = workOrderDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
