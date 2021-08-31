/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class ServerSpecificOptions {
    
    private int caseId;
    private String deployedAppDetails;

    public static final int Case_CustomDateTypeChange=1;
    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public String getDeployedAppDetails() {
        return deployedAppDetails;
    }

    public void setDeployedAppDetails(String deployedAppDetails) {
        this.deployedAppDetails = deployedAppDetails;
    }
    
    
    
}
