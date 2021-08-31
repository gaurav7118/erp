/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.labormanagement;

import com.krawler.common.admin.Company;
import com.krawler.spring.mrp.WorkOrder.AccWorkOrderController;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class LabourWorkCentreMapping  implements Comparable<LabourWorkCentreMapping>{

    private String ID;
    private Company company;
    private Labour labour;
    private WorkCentre workCentre;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Labour getLabour() {
        return labour;
    }

    public void setLabour(Labour labour) {
        this.labour = labour;
    }

    public WorkCentre getWorkCentre() {
        return workCentre;
    }

    public void setWorkCentre(WorkCentre workCentre) {
        this.workCentre = workCentre;
    }

    @Override
    public int compareTo(LabourWorkCentreMapping o) {
        int retVaul = 0;
        try {
            retVaul = this.workCentre.getName().compareTo(o.workCentre.getName());
        } catch (Exception ex) {
            Logger.getLogger(AccWorkOrderController.class.getName()).log(Level.SEVERE, "Problem Occurred while sorting LabourWorkCentreMapping Objects using comparable interface. Please check compareTo method of LabourWorkCentreMapping  to find root cause", ex);
        }
        return retVaul;
    }

}
