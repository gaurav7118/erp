/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

/**
 *
 * @author Vipin Gupta
 */
public enum InterStoreTransferStatus {

    INTRANSIT("In Transit"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    RETURNED("Returned"),
    PENDING_APPROVAL("Pending for Approval"),
    DELETED("Deleted"),
//    RETURN_ACCEPTED("Return Accepted");
    RETURN_ACCEPTED("Accepted");
    private String name;

    private InterStoreTransferStatus(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
