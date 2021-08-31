/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.User;

/**
 *
 * @author krawler
 */
public class PurchaseOrderVersionTermMap {
    private String id;
    private PurchaseOrderVersion purchaseOrderVersion;
    private InvoiceTermsSales term;
    private double termamount;
    private double percentage;
    private Integer deleted;
    private Long createdOn;
    private User creator;

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public PurchaseOrderVersion getPurchaseOrderVersion() {
        return purchaseOrderVersion;
    }

    public void setPurchaseOrderVersion(PurchaseOrderVersion purchaseOrderVersion) {
        this.purchaseOrderVersion = purchaseOrderVersion;
    }
    
    public InvoiceTermsSales getTerm() {
        return term;
    }

    public void setTerm(InvoiceTermsSales term) {
        this.term = term;
    }

    public double getTermamount() {
        return termamount;
    }

    public void setTermamount(double termamount) {
        this.termamount = termamount;
    }
}
