/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.batchserial;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.NewProductBatch;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class AttachedBatch {

    private String id;
    private NewProductBatch batch;
    private Set<AttachedSerial> attachedSerials;
    private double quantity;
    private Company company;

    public AttachedBatch() {
        this.attachedSerials = new HashSet<AttachedSerial>();
    }

    public AttachedBatch(NewProductBatch batch, double quantity, Company company) {
        this();
        this.batch = batch;
        this.quantity = quantity;
        this.company = company;
    }

    public AttachedBatch(NewProductBatch batch, Set<AttachedSerial> attachedSerials, double quantity, Company company) {
        this(batch, quantity, company);
        this.attachedSerials = attachedSerials;
    }

    public NewProductBatch getBatch() {
        return batch;
    }

    public void setBatch(NewProductBatch batch) {
        this.batch = batch;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<AttachedSerial> getAttachedSerials() {
        return attachedSerials;
    }

    public void setAttachedSerials(Set<AttachedSerial> attachedSerials) {
        this.attachedSerials = attachedSerials;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
