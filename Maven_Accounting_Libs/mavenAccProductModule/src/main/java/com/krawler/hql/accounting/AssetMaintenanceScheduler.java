/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class AssetMaintenanceScheduler {

    private String id;
    private Date startDate;
    private Date endDate;
    private Date actualStartDate;
    private Date actualEndDate;
    private boolean adHoc;
    /*
     *If user is edit total event then  isScheduleEdit is true
     */ 
    private boolean isScheduleEdit;
    private int frequency;
    private String frequencyType;
    private int totalEvents;
    private int scheduleDuration;
    private AssetDetails assetDetails;// if schedule is being created from Asset Scheduler then this field will contain value else null 
    private Company company;
    private int scheduleType;// == 1 if Contract Schedule; == 0 if Asset Maintenance Schedule
//    private AssetMaintenanceWorkOrder assetMaintenanceWorkOrder;
    private MasterItem assignedTo;
    private MasterItem status;
    private AssetMaintenanceSchedulerObject assetMaintenanceSchedulerObject;   // 1= Regular 2=Break down
    private int maintenanceType;

    public int getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(int maintenanceType) {
        this.maintenanceType = maintenanceType;
    }
    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public boolean isAdHoc() {
        return adHoc;
    }

    public void setAdHoc(boolean adHoc) {
        this.adHoc = adHoc;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(String frequencyType) {
        this.frequencyType = frequencyType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getScheduleDuration() {
        return scheduleDuration;
    }

    public void setScheduleDuration(int scheduleDuration) {
        this.scheduleDuration = scheduleDuration;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public AssetDetails getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(AssetDetails assetDetails) {
        this.assetDetails = assetDetails;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

//    public AssetMaintenanceWorkOrder getAssetMaintenanceWorkOrder() {
//        return assetMaintenanceWorkOrder;
//    }
//
//    public void setAssetMaintenanceWorkOrder(AssetMaintenanceWorkOrder assetMaintenanceWorkOrder) {
//        this.assetMaintenanceWorkOrder = assetMaintenanceWorkOrder;
//    }

    public MasterItem getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(MasterItem assignedTo) {
        this.assignedTo = assignedTo;
    }

    public MasterItem getStatus() {
        return status;
    }

    public void setStatus(MasterItem status) {
        this.status = status;
    }

    public AssetMaintenanceSchedulerObject getAssetMaintenanceSchedulerObject() {
        return assetMaintenanceSchedulerObject;
    }

    public void setAssetMaintenanceSchedulerObject(AssetMaintenanceSchedulerObject assetMaintenanceSchedulerObject) {
        this.assetMaintenanceSchedulerObject = assetMaintenanceSchedulerObject;
    }

    public int getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(int scheduleType) {
        this.scheduleType = scheduleType;
    }

    /**
     * @return the isScheduleEdit
     */
    public boolean isIsScheduleEdit() {
        return isScheduleEdit;
    }

    /**
     * @param isScheduleEdit the isScheduleEdit to set
     */
    public void setIsScheduleEdit(boolean isScheduleEdit) {
        this.isScheduleEdit = isScheduleEdit;
    }
    
}
