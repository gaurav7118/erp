/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class SalesPersonMapping {
    private String ID;
    private MasterItem salesperson;
    private Customer customerID;

    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * @return the salesperson
     */
    public MasterItem getSalesperson() {
        return salesperson;
    }

    /**
     * @param salesperson the salesperson to set
     */
    public void setSalesperson(MasterItem salesperson) {
        this.salesperson = salesperson;
    }

    /**
     * @return the customerID
     */
    public Customer getCustomerID() {
        return customerID;
    }

    /**
     * @param customerID the customerID to set
     */
    public void setCustomerID(Customer customerID) {
        this.customerID = customerID;
    }
}
