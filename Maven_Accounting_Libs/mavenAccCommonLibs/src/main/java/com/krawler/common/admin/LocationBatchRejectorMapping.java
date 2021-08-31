/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author Pandurang
 */
public class LocationBatchRejectorMapping {
    private String ID;
    private User rejectedby;
    private double rejectedQuntity;
    private LocationBatchDocumentMapping locationDocumentMapping;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public LocationBatchDocumentMapping getLocationDocumentMapping() {
        return locationDocumentMapping;
    }

    public void setLocationDocumentMapping(LocationBatchDocumentMapping locationDocumentMapping) {
        this.locationDocumentMapping = locationDocumentMapping;
    }

    public double getRejectedQuntity() {
        return rejectedQuntity;
    }

    public void setRejectedQuntity(double rejectedQuntity) {
        this.rejectedQuntity = rejectedQuntity;
    }

    public User getRejectedby() {
        return rejectedby;
    }

    public void setRejectedby(User rejectedby) {
        this.rejectedby = rejectedby;
    }
    
    
}
