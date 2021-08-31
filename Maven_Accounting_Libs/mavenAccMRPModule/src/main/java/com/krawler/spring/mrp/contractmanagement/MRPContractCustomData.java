/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.admin.AccCustomData;

public class MRPContractCustomData extends AccCustomData{
    
    private String contractId;
    private MRPContract MRPContract;
    private String moduleId;

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public MRPContract getMRPContract() {
        return MRPContract;
    }

    public void setMRPContract(MRPContract MRPContract) {
        this.MRPContract = MRPContract;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
