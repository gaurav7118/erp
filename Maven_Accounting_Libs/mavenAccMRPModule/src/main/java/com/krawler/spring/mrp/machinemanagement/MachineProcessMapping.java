/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.MasterItem;

/**
 *
 * @author krawler
 */
public class MachineProcessMapping {
    private String ID;
    private Machine machineID;
    private MasterItem processID;
    private Company company;

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

    public MasterItem getProcessID() {
        return processID;
    }

    public void setProcessID(MasterItem processID) {
        this.processID = processID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
}
