/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.batchserial;

import com.krawler.inventory.model.ist.InterStoreTransferRequest;

/**
 *
 * @author Vipin Gupta
 */
public class ISTAttachedBatch extends AttachedBatch{
    
    private InterStoreTransferRequest interStoreTransferRequest;

    public ISTAttachedBatch() {
    }

    public ISTAttachedBatch(InterStoreTransferRequest interStoreTransferRequest) {
        this.interStoreTransferRequest = interStoreTransferRequest;
    }

    public InterStoreTransferRequest getInterStoreTransferRequest() {
        return interStoreTransferRequest;
    }

    public void setInterStoreTransferRequest(InterStoreTransferRequest interStoreTransferRequest) {
        this.interStoreTransferRequest = interStoreTransferRequest;
    }
    
}
