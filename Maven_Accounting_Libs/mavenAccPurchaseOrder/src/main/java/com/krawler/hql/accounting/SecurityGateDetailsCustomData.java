/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class SecurityGateDetailsCustomData extends AccCustomData{
    private String sgeDetailID;
    private SecurityGateDetails securityGateDetails;
    private String moduleId;

    public String getSgeDetailID() {
        return sgeDetailID;
    }

    public void setSgeDetailID(String sgeDetailID) {
        this.sgeDetailID = sgeDetailID;
    }

    public SecurityGateDetails getSecurityGateDetails() {
        return securityGateDetails;
    }

    public void setSecurityGateDetails(SecurityGateDetails securityGateDetails) {
        this.securityGateDetails = securityGateDetails;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
