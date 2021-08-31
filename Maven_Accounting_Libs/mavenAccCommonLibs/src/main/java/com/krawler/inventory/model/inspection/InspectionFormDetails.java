/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.inspection;

/**
 *
 * @author Ashish Mohite
 */
public class InspectionFormDetails {
    
    private String id;
    private InspectionForm inspectionForm;
    private InspectionArea inspectionArea;
    private String inspectionAreaValue;
    private String inspectionStatus;// Status will be text - OK or NG
    private String faults;
    private String passingValue;
    private String actualValue;
    
    public InspectionFormDetails(){
    }
    
    public InspectionFormDetails(InspectionForm inspectionForm, InspectionArea inspectionArea, String inspectionAreaValue, String inspectionStatus, String faults, String passingValue, String actualValue) {
        this.inspectionForm = inspectionForm;
        this.inspectionArea = inspectionArea;
        this.inspectionAreaValue = inspectionAreaValue;
        this.inspectionStatus = inspectionStatus;
        this.faults = faults;
        this.passingValue = passingValue;
        this.actualValue = actualValue;
    }

    public String getPassingValue() {
        return passingValue;
    }

    public void setPassingValue(String passingValue) {
        this.passingValue = passingValue;
    }

    public String getFaults() {
        return faults;
    }

    public void setFaults(String faults) {
        this.faults = faults;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InspectionArea getInspectionArea() {
        return inspectionArea;
    }

    public void setInspectionArea(InspectionArea inspectionArea) {
        this.inspectionArea = inspectionArea;
    }

    public String getInspectionAreaValue() {
        return inspectionAreaValue;
    }

    public void setInspectionAreaValue(String inspectionAreaValue) {
        this.inspectionAreaValue = inspectionAreaValue;
    }

    public InspectionForm getInspectionForm() {
        return inspectionForm;
    }

    public void setInspectionForm(InspectionForm inspectionForm) {
        this.inspectionForm = inspectionForm;
    }

    public String getInspectionStatus() {
        return inspectionStatus;
    }

    public void setInspectionStatus(String inspectionStatus) {
        this.inspectionStatus = inspectionStatus;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }
   
}
