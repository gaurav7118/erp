/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class BatchSerialMapping {

    private String id;
    private BatchSerial purchaseSerial;
    private BatchSerial salesSerial;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BatchSerial getPurchaseSerial() {
        return purchaseSerial;
    }

    public void setPurchaseSerial(BatchSerial purchaseSerial) {
        this.purchaseSerial = purchaseSerial;
    }

    public BatchSerial getSalesSerial() {
        return salesSerial;
    }

    public void setSalesSerial(BatchSerial salesSerial) {
        this.salesSerial = salesSerial;
    }
}
