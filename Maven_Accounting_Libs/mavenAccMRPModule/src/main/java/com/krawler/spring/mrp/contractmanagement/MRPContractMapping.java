/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

/**
 *
 * @author krawler
 */
public class MRPContractMapping {

    private String ID;
    private MRPContract mrpcontract;
    private String parentcontractid;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public MRPContract getMrpcontract() {
        return mrpcontract;
    }

    public void setMrpcontract(MRPContract mrpcontract) {
        this.mrpcontract = mrpcontract;
    }

    public String getParentcontractid() {
        return parentcontractid;
    }

    public void setParentcontractid(String parentcontractid) {
        this.parentcontractid = parentcontractid;
    }
}
