/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.Date;


/**
 *
 * @author krawler
 */
public class MachineManRatio {
    
    private String ID;
    private Date dateForRatio;
    private double fullMachineTime;
    private double fullManTime;
    private double partMachineTime;
    private double partManTime;
    private Machine machine;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private Company company;

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

    public User getCreatedby() {
        return createdby;
    }

    public void setCreatedby(User createdby) {
        this.createdby = createdby;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public Date getDateForRatio() {
        return dateForRatio;
    }

    public void setDateForRatio(Date dateForRatio) {
        this.dateForRatio = dateForRatio;
    }

    
    public double getFullMachineTime() {
        return fullMachineTime;
    }

    public void setFullMachineTime(double fullMachineTime) {
        this.fullMachineTime = fullMachineTime;
    }

    public double getFullManTime() {
        return fullManTime;
    }

    public void setFullManTime(double fullManTime) {
        this.fullManTime = fullManTime;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public double getPartMachineTime() {
        return partMachineTime;
    }

    public void setPartMachineTime(double partMachineTime) {
        this.partMachineTime = partMachineTime;
    }

    public double getPartManTime() {
        return partManTime;
    }

    public void setPartManTime(double partManTime) {
        this.partManTime = partManTime;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }
    
    
}
