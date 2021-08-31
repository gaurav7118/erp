/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class MasterItemPriceFormula {

    private String ID;
    private double lowerlimitvalue;
    private double upperlimitvalue;
    private double basevalue;
    private double incvalue;
    private PriceType type;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getBasevalue() {
        return basevalue;
    }

    public void setBasevalue(double basevalue) {
        this.basevalue = basevalue;
    }

    public double getIncvalue() {
        return incvalue;
    }

    public void setIncvalue(double incvalue) {
        this.incvalue = incvalue;
    }

    public double getLowerlimitvalue() {
        return lowerlimitvalue;
    }

    public void setLowerlimitvalue(double lowerlimitvalue) {
        this.lowerlimitvalue = lowerlimitvalue;
    }

    public PriceType getType() {
        return type;
    }

    public void setType(PriceType type) {
        this.type = type;
    }

    public double getUpperlimitvalue() {
        return upperlimitvalue;
    }

    public void setUpperlimitvalue(double upperlimitvalue) {
        this.upperlimitvalue = upperlimitvalue;
    }
}
