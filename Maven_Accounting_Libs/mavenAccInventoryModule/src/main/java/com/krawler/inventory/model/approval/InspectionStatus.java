/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval;

/**
 *
 * @author krawler
 */
public enum InspectionStatus {

    ACCEPTABLE(1), NOT_ACCEPTABLE(0);
    private int statusCode;

    private InspectionStatus(int s) {
        statusCode = s;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
