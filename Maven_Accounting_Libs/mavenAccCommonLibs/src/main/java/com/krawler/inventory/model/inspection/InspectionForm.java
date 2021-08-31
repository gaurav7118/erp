/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.inspection;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Ashish Mohite
 */
public class InspectionForm {
    
    private String id;
    private Date inspectionDate;
    private String modelName;
    private String customerName;
    private String department;
    private String consignmentReturnNo;
    private Set<InspectionFormDetails> rows;

    public InspectionForm(){
        rows = new HashSet<>();
    }
    
    public InspectionForm(Date inspectionDate, String modelName, String customerName, String department, String consignmentReturnNo) {
        this.inspectionDate = inspectionDate;
        this.modelName = modelName;
        this.customerName = customerName;
        this.department = department;
        this.consignmentReturnNo = consignmentReturnNo;
    }

    public String getConsignmentReturnNo() {
        return consignmentReturnNo;
    }

    public void setConsignmentReturnNo(String consignmentReturnNo) {
        this.consignmentReturnNo = consignmentReturnNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Date inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Set<InspectionFormDetails> getRows() {
        return rows;
    }

    public void setRows(Set<InspectionFormDetails> rows) {
        this.rows = rows;
    }
    
}
