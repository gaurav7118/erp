/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.User;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.store.Store;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class InterStoreTransferRequest {

    private String id;
    private String transactionNo;
    private Product product;
    private Store fromStore;
    private Store toStore;
    private Packaging packaging;
    private UnitOfMeasure uom;
    private InterStoreTransferStatus status;
    private String remark;
    private boolean isjobWorkClose;
    private double orderedQty;
    private double acceptedQty;
    private CostCenter costCenter;
    private Company company;
    private Date businessDate;
    private Date createdOn;
    private Date modifiedOn;
    private User createdBy;
    private User modifiedBy;
    private User approvedBy;
    private Set<ISTDetail> istDetails;
    private TransactionModule transactionModule;      //Only Two Types of Modules 1.inter store transfer or 2.inter location Transfer 
    private InterStoreTransferCustomData ISTCustomData;
    private long creationdate;
    private long modifieddate;
    private String memo;
    private InterStoreTransferCustomData ISTLineLevelCustomData;
    private boolean isJobWorkStockTransfer;
    private String purchaseOrderDetail;
    private ChallanNumber challanNumber;
    private GRODetailISTMapping detailISTMapping;
    private RepairGRODetailISTMapping repairGRODetailISTMapping;
    private String parentID;
    private WOCDetailISTMapping wocdISTMapping;
    private RepairWOCDISTMapping repairWOCDISTMapping;

    public String getPurchaseOrderDetail() {
        return purchaseOrderDetail;
    }

    public void setPurchaseOrderDetail(String purchaseOrderDetail) {
        this.purchaseOrderDetail = purchaseOrderDetail;
    }

    public ChallanNumber getChallanNumber() {
        return challanNumber;
    }

    public void setChallanNumber(ChallanNumber challanNumber) {
        this.challanNumber = challanNumber;
    }

    public InterStoreTransferRequest() {
        this.istDetails = new HashSet<ISTDetail>();
        this.transactionModule = TransactionModule.INTER_STORE_TRANSFER;
    }

    public InterStoreTransferRequest(Product product, Store fromStore, Store toStore, UnitOfMeasure uom) {
        this();
        this.product = product;
        this.fromStore = fromStore;
        this.toStore = toStore;
        this.uom = uom;
        this.company = product.getCompany();
    }

    public Set<ISTDetail> getIstDetails() {
        return istDetails;
    }

    public void setIstDetails(Set<ISTDetail> istDetails) {
        this.istDetails = istDetails;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public Packaging getPackaging() {
        return packaging;
    }

    public void setPackaging(Packaging packaging) {
        this.packaging = packaging;
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

    public User getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(User modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public double getOrderedQty() {
        return orderedQty;
    }

    public void setOrderedQty(double orderedQty) {
        this.orderedQty = orderedQty;
    }

    public double getAcceptedQty() {
        return acceptedQty;
    }

    public void setAcceptedQty(double acceptedQty) {
        this.acceptedQty = acceptedQty;
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

    public InterStoreTransferStatus getStatus() {
        return status;
    }

    public void setStatus(InterStoreTransferStatus status) {
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

    public Date getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(Date businessDate) {
        this.businessDate = businessDate;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public TransactionModule getTransactionModule() {
        return transactionModule;
    }

    public void setTransactionModule(TransactionModule transactionModule) {
        this.transactionModule = transactionModule;
    }

    public InterStoreTransferCustomData getISTCustomData() {
        return ISTCustomData;
    }

    public void setISTCustomData(InterStoreTransferCustomData ISTCustomData) {
        this.ISTCustomData = ISTCustomData;
    }

    public long getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(long creationdate) {
        this.creationdate = creationdate;
    }

    public long getModifieddate() {
        return modifieddate;
    }

    public void setModifieddate(long modifieddate) {
        this.modifieddate = modifieddate;
    }

    public InterStoreTransferCustomData getISTLineLevelCustomData() {
        return ISTLineLevelCustomData;
    }

    public void setISTLineLevelCustomData(InterStoreTransferCustomData ISTLineLevelCustomData) {
        this.ISTLineLevelCustomData = ISTLineLevelCustomData;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
    public boolean isIsJobWorkStockTransfer() {
        return isJobWorkStockTransfer;
    }

    public void setIsJobWorkStockTransfer(boolean isJobWorkStockTransfer) {
        this.isJobWorkStockTransfer = isJobWorkStockTransfer;
    }


    public GRODetailISTMapping getDetailISTMapping() {
        return detailISTMapping;
    }

    public void setDetailISTMapping(GRODetailISTMapping detailISTMapping) {
        this.detailISTMapping = detailISTMapping;
    }

    public RepairGRODetailISTMapping getRepairGRODetailISTMapping() {
        return repairGRODetailISTMapping;
    }

    public void setRepairGRODetailISTMapping(RepairGRODetailISTMapping repairGRODetailISTMapping) {
        this.repairGRODetailISTMapping = repairGRODetailISTMapping;
    }

    /**
     * @return the isjobWorkClose
     */
    public boolean isIsjobWorkClose() {
        return isjobWorkClose;
    }

    /**
     * @param isjobWorkClose the isjobWorkClose to set
     */
    public void setIsjobWorkClose(boolean isjobWorkClose) {
        this.isjobWorkClose = isjobWorkClose;
    }

    public String getParentID() {
        return parentID;
}

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public RepairWOCDISTMapping getRepairWOCDISTMapping() {
        return repairWOCDISTMapping;
    }

    public void setRepairWOCDISTMapping(RepairWOCDISTMapping repairWOCDISTMapping) {
        this.repairWOCDISTMapping = repairWOCDISTMapping;
    }

    public WOCDetailISTMapping getWocdISTMapping() {
        return wocdISTMapping;
    }

    public void setWocdISTMapping(WOCDetailISTMapping wocdISTMapping) {
        this.wocdISTMapping = wocdISTMapping;
    }
    
}
