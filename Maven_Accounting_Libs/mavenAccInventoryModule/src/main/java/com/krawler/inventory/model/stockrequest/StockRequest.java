/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.User;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stock.StockCustomData;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.store.Store;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
 public class StockRequest {

    private String id;
    private String transactionNo;
    private Product product;
    private Store fromStore;
    private Store toStore;
    private Packaging packaging;
    private UnitOfMeasure uom;
    private RequestStatus status;
    private String remark;
    private String returnReason;
    private String projectNumber;
    private CostCenter costCenter;
    private double orderedQty;
    private double issuedQty;
    private double deliveredQty;
    private Company company;
    private Date businessDate;
    private Date requestedOn;
    private Date issuedOn;
    private Date collectedOn;
    private Date modifiedOn;
    private User requestedBy;
    private User issuedBy;
    private User collectedBy;
    private User approvedBy;
    private boolean returnRequest;
    private Set<StockRequestDetail> stockRequestDetails = new HashSet<StockRequestDetail>();
    private StockCustomData stockCustomData;
    private TransactionModule module;
    private long issueddate;
    private long collecteddate;
    private long modifieddate;
    private StockCustomData stockLineLevelCustomData;//Line Level Custom data
    private int istemplate;
    private String parentID;

    public int getIstemplate() {
        return istemplate;
    }

    public void setIstemplate(int istemplate) {
        this.istemplate = istemplate;
    }

    public StockRequest() {
        returnRequest = false;
        this.stockRequestDetails = new HashSet<StockRequestDetail>();
    }

    public StockRequest(Product product, Store fromStore, Store toStore, UnitOfMeasure uom, double orderedQty) {
        this();
        this.product = product;
        this.fromStore = fromStore;
        this.toStore = toStore;
        this.uom = uom;
        this.orderedQty = orderedQty;
        this.company = product.getCompany();
    }

    public Set<StockRequestDetail> getStockRequestDetails() {
        return stockRequestDetails;
    }

    public void setStockRequestDetails(Set<StockRequestDetail> stockRequestDetails) {
        this.stockRequestDetails = stockRequestDetails;
    }

    public Packaging getPackaging() {
        return packaging;
    }

    public void setPackaging(Packaging packaging) {
        this.packaging = packaging;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public User getCollectedBy() {
        return collectedBy;
    }

    public void setCollectedBy(User collectedBy) {
        this.collectedBy = collectedBy;
    }

    public Date getCollectedOn() {
        return collectedOn;
    }

    public void setCollectedOn(Date collectedOn) {
        this.collectedOn = collectedOn;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public Store getFromStore() {
        return fromStore;
    }

    public void setFromStore(Store fromStore) {
        this.fromStore = fromStore;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(User issuedBy) {
        this.issuedBy = issuedBy;
    }

    public Date getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(Date issuedOn) {
        this.issuedOn = issuedOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Date getRequestedOn() {
        return requestedOn;
    }

    public void setRequestedOn(Date requestedOn) {
        this.requestedOn = requestedOn;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Store getToStore() {
        return toStore;
    }

    public void setToStore(Store toStore) {
        this.toStore = toStore;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public double getDeliveredQty() {
        return deliveredQty;
    }

    public void setDeliveredQty(double deliveredQty) {
        this.deliveredQty = deliveredQty;
    }

    public double getIssuedQty() {
        return issuedQty;
    }

    public void setIssuedQty(double issuedQty) {
        this.issuedQty = issuedQty;
    }

    public double getOrderedQty() {
        return orderedQty;
    }

    public void setOrderedQty(double orderedQty) {
        this.orderedQty = orderedQty;
    }

    public String getProjectNumber() {
        return projectNumber;
    }

    public void setProjectNumber(String projectNumber) {
        this.projectNumber = projectNumber;
    }

    public Date getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(Date businessDate) {
        this.businessDate = businessDate;
    }

    public StockCustomData getStockCustomData() {
        return stockCustomData;
    }

    public void setStockCustomData(StockCustomData stockCustomData) {
        this.stockCustomData = stockCustomData;
    }

    public boolean isReturnRequest() {
        return returnRequest;
    }

    public void setReturnRequest(boolean returnRequest) {
        this.returnRequest = returnRequest;
    }

    public TransactionModule getModule() {
        return module;
    }
    
    public void setModule(TransactionModule module) {
        this.module = module;
    }

    public long getCollecteddate() {
        return collecteddate;
    }

    public void setCollecteddate(long collecteddate) {
        this.collecteddate = collecteddate;
    }

    public long getIssueddate() {
        return issueddate;
    }

    public void setIssueddate(long issueddate) {
        this.issueddate = issueddate;
    }

    public long getModifieddate() {
        return modifieddate;
    }

    public void setModifieddate(long modifieddate) {
        this.modifieddate = modifieddate;
    }

    public StockCustomData getStockLineLevelCustomData() {
        return stockLineLevelCustomData;
    }

    public void setStockLineLevelCustomData(StockCustomData stockLineLevelCustomData) {
        this.stockLineLevelCustomData = stockLineLevelCustomData;
    }
    
    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }
}