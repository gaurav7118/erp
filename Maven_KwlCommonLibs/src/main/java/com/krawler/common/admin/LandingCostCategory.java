/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;


/**
 *
 * @author krawler
 */
public class LandingCostCategory {
    
 private String id;
 private String lccName;
 private int lcallocationid;
 private Company company;

   
 
 
 //Gatter & Setter  Methode

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getLccName() {
        return lccName;
    }

    public void setLccName(String lccName) {
        this.lccName = lccName;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getLcallocationid() {
        return lcallocationid;
    }

    public void setLcallocationid(int lcallocationid) {
        this.lcallocationid = lcallocationid;
    }
}
