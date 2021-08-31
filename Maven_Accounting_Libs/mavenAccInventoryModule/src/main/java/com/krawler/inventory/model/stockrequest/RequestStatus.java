/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest;

/**
 *
 * @author Vipin Gupta
 */
public enum RequestStatus {
    
    ORDERED ("Ordered"),
    PENDING_APPROVAL ("Pending Request Approval"),
    ISSUED ("Issued"),
    REJECTED ("Rejected"),
    COLLECTED ("Collected"),
    RETURNED ("Returned"),
    RETURN_REQUEST ("Return Request"),
    RETURN_APPROVAL ("Return Approval"),
    DELETED ("Deleted");
    
    private String name;

    private RequestStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
    
}
