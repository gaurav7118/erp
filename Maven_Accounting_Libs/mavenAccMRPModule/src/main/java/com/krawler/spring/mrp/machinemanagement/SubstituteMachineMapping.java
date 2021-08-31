/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class SubstituteMachineMapping {
    private String ID;
    private Machine substituteMachineID;
    private Machine activeMachineID;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Machine getSubstituteMachineID() {
        return substituteMachineID;
    }

    public void setSubstituteMachineID(Machine substituteMachineID) {
        this.substituteMachineID = substituteMachineID;
    }

    public Machine getActiveMachineID() {
        return activeMachineID;
    }

    public void setActiveMachineID(Machine activeMachineID) {
        this.activeMachineID = activeMachineID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
}
