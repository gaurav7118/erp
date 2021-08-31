/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval;

/**
 *
 * @author Vipin Gupta
 */
public class InspectionCriteriaDetail {

    private String id;
    private String inspectionArea;
    private Integer acceptable;
    private String faults;
    private InspectionDetail inspectionDetail;

    public InspectionCriteriaDetail() {
        this.acceptable=-1;
    }

//    public Integer isAcceptable() {
//        return acceptable;
//    }

    public Integer getAcceptable() {
        return acceptable;
    }

    public void setAcceptable(Integer acceptable) {
        this.acceptable = acceptable;
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

    public String getInspectionArea() {
        return inspectionArea;
    }

    public void setInspectionArea(String inspectionArea) {
        this.inspectionArea = inspectionArea;
    }

    public InspectionDetail getInspectionDetail() {
        return inspectionDetail;
    }

    public void setInspectionDetail(InspectionDetail inspectionDetail) {
        this.inspectionDetail = inspectionDetail;
    }
}
