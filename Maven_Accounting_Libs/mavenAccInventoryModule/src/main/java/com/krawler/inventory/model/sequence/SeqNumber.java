/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence;

/**
 *
 * @author Vipin gupta
 */
public class SeqNumber {
    
    private String id;
    private SeqFormat seqFormat;
    private long serialNumber;

    public SeqNumber() {
    }

    public SeqNumber(SeqFormat seqFormat, long serialNumber) {
        this.seqFormat = seqFormat;
        this.serialNumber = serialNumber;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SeqFormat getSeqFormat() {
        return seqFormat;
    }

    public void setSeqFormat(SeqFormat seqFormat) {
        this.seqFormat = seqFormat;
    }

    public long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    
    
}
