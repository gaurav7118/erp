/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.store.Store;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class CycleCount {

    private String id;
    private String transactionNo;
    private Product product;
    private Store store;
    private Date businessDate;
    private double systemQty;
    private Packaging packaging;
    private double casingUomCount;
    private double innerUomCount;
    private double stockUomCount;
    private Date createdOn;
    private User createdBy;
    private Company company;
    private String remark;
    private CycleCountStatus status;
    private Set<CycleCountDetail> cycleCountDetails;
    private boolean extraItem;
    private CycleCountCustomData cycleCountCustomData;

    public CycleCountCustomData getCycleCountCustomData() {
        return cycleCountCustomData;
    }

    public void setCycleCountCustomData(CycleCountCustomData cycleCountCustomData) {
        this.cycleCountCustomData = cycleCountCustomData;
    }

    public CycleCount() {
        cycleCountDetails = new HashSet<>();
        status = CycleCountStatus.DONE;
    }

    public CycleCount(Product product, Store store, Date businessDate, Packaging packaging, double casingUomCount, double innerUomCount, double stockUomCount) {
        this();
        this.company = product.getCompany();
        this.product = product;
        this.store = store;
        this.businessDate = businessDate;
        this.packaging = packaging;
        this.casingUomCount = casingUomCount;
        this.innerUomCount = innerUomCount;
        this.stockUomCount = stockUomCount;
    }

    public Set<CycleCountDetail> getCycleCountDetails() {
        return cycleCountDetails;
    }

    public void setCycleCountDetails(Set<CycleCountDetail> cycleCountDetails) {
        this.cycleCountDetails = cycleCountDetails;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public Date getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(Date businessDate) {
        this.businessDate = businessDate;
    }

    public double getCasingUomCount() {
        return casingUomCount;
    }

    public void setCasingUomCount(double casingUomCount) {
        this.casingUomCount = casingUomCount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getInnerUomCount() {
        return innerUomCount;
    }

    public void setInnerUomCount(double innerUomCount) {
        this.innerUomCount = innerUomCount;
    }

    public Packaging getPackaging() {
        return packaging;
    }

    public void setPackaging(Packaging packaging) {
        this.packaging = packaging;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        company = product.getCompany();
    }

    public CycleCountStatus getStatus() {
        return status;
    }

    public void setStatus(CycleCountStatus status) {
        this.status = status;
    }

    public double getStockUomCount() {
        return stockUomCount;
    }

    public void setStockUomCount(double stockUomCount) {
        this.stockUomCount = stockUomCount;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public double getSystemQty() {
        return systemQty;
    }

    public void setSystemQty(double systemQty) {
        this.systemQty = systemQty;
    }

    public boolean isExtraItem() {
        return extraItem;
    }

    public void setExtraItem(boolean extraItem) {
        this.extraItem = extraItem;
    }

    public double getActualQty() {
        double expectedQty = 0;
        if (this.packaging != null) {
            expectedQty += packaging.getQuantityInStockUoM(this.packaging.getCasingUoM(), this.casingUomCount);
            expectedQty += packaging.getQuantityInStockUoM(this.packaging.getInnerUoM(), this.innerUomCount);
            expectedQty += stockUomCount;
        } else {
            expectedQty = casingUomCount + innerUomCount + stockUomCount;
        }
        return expectedQty;
    }

    public double getQtyVariance() {
        double expectedQty = getActualQty();
        return expectedQty - systemQty;
    }
}
