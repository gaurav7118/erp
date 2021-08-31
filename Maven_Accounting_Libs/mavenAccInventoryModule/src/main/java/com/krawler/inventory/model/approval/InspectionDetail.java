/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval;

import com.krawler.common.admin.Company;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class InspectionDetail {
    
    private String id;
    private String referenceNo;
    private String customerPONo;
    private String hospital;
    private String department;
    private String modelname;
    private Set<InspectionCriteriaDetail> inspectionCriteriaDetailSet;
    private Company company;

    public InspectionDetail() {
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getCustomerPONo() {
        return customerPONo;
    }

    public void setCustomerPONo(String customerPONo) {
        this.customerPONo = customerPONo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<InspectionCriteriaDetail> getInspectionCriteriaDetailSet() {
        return inspectionCriteriaDetailSet;
    }

    public void setInspectionCriteriaDetailSet(Set<InspectionCriteriaDetail> inspectionCriteriaDetailSet) {
        this.inspectionCriteriaDetailSet = inspectionCriteriaDetailSet;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getModelname() {
        return modelname;
    }

    public void setModelname(String modelname) {
        this.modelname = modelname;
    }
    

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }
    
}
