/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.mrp.labormanagement;

import com.krawler.common.admin.Company;
import java.util.Date;

public class ResourceCost {

    private String ID;
    private Date effectivedate;
    private String resourcecost;
    private Company company;
    private Labour labour;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getEffectivedate() {
        return effectivedate;
    }

    public void setEffectivedate(Date effectivedate) {
        this.effectivedate = effectivedate;
    }

    public String getResourcecost() {
        return resourcecost;
    }

    public void setResourcecost(String resourcecost) {
        this.resourcecost = resourcecost;
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
}
