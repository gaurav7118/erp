    /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class VendorAgentMapping {
     private String ID;
    private MasterItem agent;
    private Vendor vendorID;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public MasterItem getAgent() {
        return agent;
    }

    public void setAgent(MasterItem agent) {
        this.agent = agent;
    }

    public Vendor getVendorID() {
        return vendorID;
    }

    public void setVendorID(Vendor vendorID) {
        this.vendorID = vendorID;
    }
}
