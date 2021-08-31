/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.inventory.model.cyclecount;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class CycleCountCustomData extends AccCustomData {

    private String ccid;
    private CycleCount cycleCount;
    private String moduleId;

    public CycleCount getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(CycleCount cycleCount) {
        this.cycleCount = cycleCount;
    }

    public String getCcid() {
        return ccid;
    }

    public void setCcid(String ccid) {
        this.ccid = ccid;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
