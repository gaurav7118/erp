/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.Company;
import java.util.Date;

public class MachineCost{
    private String ID;
    private Date effectivedate;
    private String machinecost;
    private Company company;
    private Machine machine;

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

    public Date getEffectivedate() {
        return effectivedate;
    }

    public void setEffectivedate(Date effectivedate) {
        this.effectivedate = effectivedate;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public String getMachinecost() {
        return machinecost;
    }

    public void setMachinecost(String machinecost) {
        this.machinecost = machinecost;
    }
}