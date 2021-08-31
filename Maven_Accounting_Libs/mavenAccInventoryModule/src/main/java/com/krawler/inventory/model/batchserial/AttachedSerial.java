/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.batchserial;

import com.krawler.common.admin.NewBatchSerial;

/**
 *
 * @author Vipin Gupta
 */
public class AttachedSerial {
    
    private String id;
    private NewBatchSerial serial;
    private AttachedBatch attachedBatch;

    public AttachedSerial() {
    }

    public AttachedSerial(NewBatchSerial serial, AttachedBatch attachedBatch) {
        this.serial = serial;
        this.attachedBatch = attachedBatch;
    }

    public AttachedBatch getAttachedBatch() {
        return attachedBatch;
    }

    public void setAttachedBatch(AttachedBatch attachedBatch) {
        this.attachedBatch = attachedBatch;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NewBatchSerial getSerial() {
        return serial;
    }

    public void setSerial(NewBatchSerial serial) {
        this.serial = serial;
    }
    
}
