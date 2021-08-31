/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.spring.mrp.labormanagement.Labour;

/**
 *
 * @author krawler
 */
public class WorkOrderLabourMapping {
    
    private String id;
    private WorkOrder workorderid;
    private Labour labourid;

    public static final String POJONAME="WorkOrderLabourMapping";
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

    public Labour getLabourid() {
        return labourid;
    }

    public void setLabourid(Labour labourid) {
        this.labourid = labourid;
    }
    
}
