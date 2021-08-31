/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.common.admin.AccCustomData;

public class WorkCentreCustomData extends AccCustomData {
    
    private String workCentreId;
    private WorkCentre workCentre;
    private String moduleId;

    public String getWorkCentreId() {
        return workCentreId;
    }

    public void setWorkCentreId(String workCentreId) {
        this.workCentreId = workCentreId;
    }

    public WorkCentre getWorkCentre() {
        return workCentre;
    }

    public void setWorkCentre(WorkCentre workCentre) {
        this.workCentre = workCentre;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
