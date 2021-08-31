/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.spring.mrp.machinemanagement.Machine;

/**
 *
 * @author krawler
 */
public class WorkOrderMachineMapping {
    
    private String id;
    private WorkOrder workorderid;
    private Machine machineid;
    
    public static final String POJONAME="WorkOrderMachineMapping";
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

    public Machine getMachineid() {
        return machineid;
    }

    public void setMachineid(Machine machineid) {
        this.machineid = machineid;
    }
}
