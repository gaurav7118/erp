/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import java.util.Comparator;

/**
 *
 * @author krawler
 */
public class MachineWorkCenterMapping implements Comparable<MachineWorkCenterMapping>{
    private String ID;
    private Machine machineID;
    private Company company;
    private WorkCentre workCenterID;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Machine getMachineID() {
        return machineID;
    }

    public void setMachineID(Machine machineID) {
        this.machineID = machineID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public WorkCentre getWorkCenterID() {
        return workCenterID;
    }

    public void setWorkCenterID(WorkCentre workCenterID) {
        this.workCenterID = workCenterID;
    }

    @Override
    public int compareTo(MachineWorkCenterMapping o) {
       
        return this.workCenterID.getName().compareTo(o.workCenterID.getName());
    }

    
}
