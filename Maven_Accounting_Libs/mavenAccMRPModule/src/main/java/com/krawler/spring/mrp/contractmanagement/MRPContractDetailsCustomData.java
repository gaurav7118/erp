/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.admin.AccCustomData;

public class MRPContractDetailsCustomData extends AccCustomData{
    
    private String contractDetailsId;
    private MRPContractDetails contractDetails;
    private String moduleId;

    public String getContractDetailsId() {
        return contractDetailsId;
    }

    public void setContractDetailsId(String contractDetailsId) {
        this.contractDetailsId = contractDetailsId;
    }

    public MRPContractDetails getContractDetails() {
        return contractDetails;
    }

    public void setContractDetails(MRPContractDetails contractDetails) {
        this.contractDetails = contractDetails;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}