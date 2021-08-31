/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class ReturnSerialMapping {

    private String id;
    private BatchSerial maptoserial;
    private BatchSerial mapserial;
    private int returntype; //This is Type we can identify Which Returnis done   1- Sales return ,2 Purchase Return

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BatchSerial getMapserial() {
        return mapserial;
    }

    public void setMapserial(BatchSerial mapserial) {
        this.mapserial = mapserial;
    }

    public BatchSerial getMaptoserial() {
        return maptoserial;
    }

    public void setMaptoserial(BatchSerial maptoserial) {
        this.maptoserial = maptoserial;
    }

    public int getReturntype() {
        return returntype;
    }

    public void setReturntype(int returntype) {
        this.returntype = returntype;
    }
}
