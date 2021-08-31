/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

public class ContractCustomData extends AccCustomData {

    private String contractID;
    private Contract contract;
    private String moduleId;

    public Contract getcontract() {
        return contract;
    }

    public void setcontract(Contract contract) {
        this.contract = contract;
    }

    public String getcontractID() {
        return contractID;
    }

    public void setcontractID(String contractID) {
        this.contractID = contractID;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
