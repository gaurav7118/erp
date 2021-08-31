/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

import com.krawler.common.admin.Company;
import com.krawler.inventory.model.frequency.Frequency;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class CycleCountCalendar {

    private String id;
    private Date date;
    private Set<Frequency> frequencies;
    private Company company;

    public CycleCountCalendar() {
        this.frequencies = new HashSet<Frequency>();
    }

    public CycleCountCalendar(Company company, Date date, Set<Frequency> frequencies) {
        this();
        this.date = date;
        this.frequencies = frequencies;
        this.company = company;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Set<Frequency> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(Set<Frequency> frequencies) {
        this.frequencies = frequencies;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
