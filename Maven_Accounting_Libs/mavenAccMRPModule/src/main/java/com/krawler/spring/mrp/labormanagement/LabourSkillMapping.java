/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.labormanagement;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.spring.mrp.workcentremanagement.WorkCentre;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class LabourSkillMapping {

    private String ID;
    private Company company;
    private Labour labour;
    private MasterItem skill;

    public MasterItem getSkill() {
        return skill;
    }

    public void setSkill(MasterItem skill) {
        this.skill = skill;
    }

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

 

}
