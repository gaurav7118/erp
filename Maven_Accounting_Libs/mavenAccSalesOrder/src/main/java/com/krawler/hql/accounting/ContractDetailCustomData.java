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
public class ContractDetailCustomData extends AccCustomData {

    private String scDetailID;
    private ContractDetail salesContractDetail;
    private String moduleId;

    public String getScDetailID() {
        return scDetailID;
    }

    public void setScDetailID(String scDetailID) {
        this.scDetailID = scDetailID;
    }

    public ContractDetail getSalesContractDetail() {
        return salesContractDetail;
    }

    public void setSalesContractDetail(ContractDetail salesContractDetail) {
        this.salesContractDetail = salesContractDetail;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

}
