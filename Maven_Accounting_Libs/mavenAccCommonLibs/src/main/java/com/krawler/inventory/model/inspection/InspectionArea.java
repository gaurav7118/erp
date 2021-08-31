/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.inspection;

/**
 *
 * @author Vipin Gupta
 */
public class InspectionArea {

    private String id;
    private String name;
    private String faults; // semicolon separated
    private String passingValue;
    private InspectionTemplate inspectionTemplate;

    public InspectionArea() {
    }

    public InspectionArea(InspectionTemplate inspectionTemplate, String name, String faults, String passingValue) {
        this.name = name;
        this.faults = faults;
        this.inspectionTemplate = inspectionTemplate;
        this.passingValue = passingValue;
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

    public InspectionTemplate getInspectionTemplate() {
        return inspectionTemplate;
    }

    public void setInspectionTemplate(InspectionTemplate inspectionTemplate) {
        this.inspectionTemplate = inspectionTemplate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getFaultList() {
        return faults.split(";");
    }
}
