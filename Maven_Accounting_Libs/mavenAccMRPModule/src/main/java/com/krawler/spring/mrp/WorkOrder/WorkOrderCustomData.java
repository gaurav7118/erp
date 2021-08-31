/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.mrp.WorkOrder;

import com.krawler.common.admin.AccCustomData;

public class WorkOrderCustomData extends AccCustomData{
    
    private String workOrderId;
    private WorkOrder workOrder;
    private String moduleId;

    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
    public static final String POJONAME = "WorkOrderCustomData";
    public static final String DB_WORKORDERID = "workOrder.ID";
}