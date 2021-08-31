/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 * Used to Store INDIA GST Tax CESS Calculation Type 
 * CESS Calculation Type as Below
 * 1) Not Applicable. 
 * 2) Percentage
 * 3) Value per Thousand or CESS % whichever is higher
 * 4) Value per Thousand + CESS %
 * 5) Value per Thousand 
 * 
 * @author Rahul A. Bhawar
 */
public class GSTCessRuleType {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
