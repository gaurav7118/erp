/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class PricingBandmappingWithVolumeDisc {
    
    private String ID;
    private PricingBandMaster pricebandid;
    private PricingBandMaster volumediscountid;
    

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * @return the pricebandid
     */
    public PricingBandMaster getPricebandid() {
        return pricebandid;
    }

    /**
     * @param pricebandid the pricebandid to set
     */
    public void setPricebandid(PricingBandMaster pricebandid) {
        this.pricebandid = pricebandid;
    }

    /**
     * @return the volumediscountid
     */
    public PricingBandMaster getVolumediscountid() {
        return volumediscountid;
    }

    /**
     * @param volumediscountid the volumediscountid to set
     */
    public void setVolumediscountid(PricingBandMaster volumediscountid) {
        this.volumediscountid = volumediscountid;
    }

   
    
}
