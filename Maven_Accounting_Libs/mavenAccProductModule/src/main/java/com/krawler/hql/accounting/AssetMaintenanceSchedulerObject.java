/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class AssetMaintenanceSchedulerObject {

    private String id;
    private String scheduleName;
    private Date startDate;
    private Date endDate;
    private boolean adHoc;
    private int frequency;
    private String frequencyType;
    private int totalEvents;
    private int scheduleDuration;
    private int scheduleStopCondition;// ==0 for adhoc schedule, == 1 for total events, == 2 for schedule end date
    private AssetDetails assetDetails; // if schedule is being created from Asset Scheduler then this field will contain value else null 
    private Company company;
    private String contractId;// if schedule is being created from Contract then this field will contain value else null 
    private int scheduleType;// == 1 if Contract Schedule; == 0 if Asset Maintenance Schedule
    private Set<AssetMaintenanceScheduler> assetMaintenanceSchedulers; // 1= Regular 2=Break down
    private int maintenanceType;

    public int getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(int maintenanceType) {
        this.maintenanceType = maintenanceType;
    }
    public boolean isAdHoc() {
        return adHoc;
    }

    public void setAdHoc(boolean adHoc) {
        this.adHoc = adHoc;
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

//    public Set<AssetMaintenanceAdhocScheduleDetails> getAdhocScheduleDetails() {
//        return adhocScheduleDetails;
//    }
//
//    public void setAdhocScheduleDetails(Set<AssetMaintenanceAdhocScheduleDetails> adhocScheduleDetails) {
//        this.adhocScheduleDetails = adhocScheduleDetails;
//    }

    public Set<AssetMaintenanceScheduler> getAssetMaintenanceSchedulers() {
        return assetMaintenanceSchedulers;
    }

    public void setAssetMaintenanceSchedulers(Set<AssetMaintenanceScheduler> assetMaintenanceSchedulers) {
        this.assetMaintenanceSchedulers = assetMaintenanceSchedulers;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public int getScheduleStopCondition() {
        return scheduleStopCondition;
    }

    public void setScheduleStopCondition(int scheduleStopCondition) {
        this.scheduleStopCondition = scheduleStopCondition;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public int getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(int scheduleType) {
        this.scheduleType = scheduleType;
    }
    
}
