/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class BatchSerial {

    private String id;
    private String name;
    private Date expfromdate;
    private Date exptodate;
    private ProductBatch batch;
    private Company company;
    private String product;
    private boolean ispurchase;
    private int transactiontype;

    public ProductBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductBatch batch) {
        this.batch = batch;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getExpfromdate() {
        return expfromdate;
    }

    public void setExpfromdate(Date expfromdate) {
        this.expfromdate = expfromdate;
    }

    public Date getExptodate() {
        return exptodate;
    }

    public void setExptodate(Date exptodate) {
        this.exptodate = exptodate;
    }

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

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public boolean isIspurchase() {
        return ispurchase;
    }

    public void setIspurchase(boolean ispurchase) {
        this.ispurchase = ispurchase;
    }

    public int getTransactiontype() {
        return transactiontype;
    }

    public void setTransactiontype(int transactiontype) {
        this.transactiontype = transactiontype;
    }
}
