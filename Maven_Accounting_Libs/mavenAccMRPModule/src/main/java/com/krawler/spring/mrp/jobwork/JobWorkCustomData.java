/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.mrp.jobwork;

import com.krawler.common.admin.AccCustomData;

public class JobWorkCustomData extends AccCustomData{
    
    private String jobWorkId;
    private JobWork jobWork;
    private String moduleId;

    public String getJobWorkId() {
        return jobWorkId;
    }

    public void setJobWorkId(String jobWorkId) {
        this.jobWorkId = jobWorkId;
    }
    
    public JobWork getJobWork() {
        return jobWork;
    }

    public void setJobWork(JobWork jobWork) {
        this.jobWork = jobWork;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}