/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.spring.mrp.workcentremanagement.WorkCentre;

/**
 *
 * @author krawler
 */
public class WorkOrderWorkCenterMapping {
    private String id;
    private WorkOrder workorderid;
    private WorkCentre workcentreid;
    

    public static final String POJONAME="WorkOrderWorkCenterMapping";
    public static final String ATTRIBUTENAME="workorderid.id";
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WorkOrder getWorkorderid() {
        return workorderid;
    }

    public void setWorkorderid(WorkOrder workorderid) {
        this.workorderid = workorderid;
    }

    public WorkCentre getWorkcentreid() {
        return workcentreid;
    }

    public void setWorkcentreid(WorkCentre workcentreid) {
        this.workcentreid = workcentreid;
    }
}
