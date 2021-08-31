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
public class SecurityGateEntryCustomData extends AccCustomData{

    private String sgeID;
    private SecurityGateEntry securityGateEntry;
    private String moduleId;
    
    public String getSgeID() {
        return sgeID;
    }

    public void setSgeID(String sgeID) {
        this.sgeID = sgeID;
    }

    public SecurityGateEntry getSecurityGateEntry() {
        return securityGateEntry;
    }

    public void setSecurityGateEntry(SecurityGateEntry securityGateEntry) {
        this.securityGateEntry = securityGateEntry;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
  
}
