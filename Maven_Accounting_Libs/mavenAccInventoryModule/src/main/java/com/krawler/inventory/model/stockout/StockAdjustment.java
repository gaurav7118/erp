/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.User;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.cyclecount.CycleCount;
import com.krawler.inventory.model.ist.DODQCISTMapping;
import com.krawler.inventory.model.ist.RejectedDODQCISTMapping;
import com.krawler.inventory.model.ist.RepairGRODetailISTMapping;
import com.krawler.inventory.model.ist.RepairWOCDISTMapping;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.store.Store;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class StockAdjustment implements Serializable {

    private String id;
    private String transactionNo;
    private Product product;
    private Store store;
//    private Location location;
    private Packaging packaging;
    private UnitOfMeasure uom;
    private double quantity;
    private double finalQuantity;
    private double pricePerUnit;
    private CostCenter costCenter;
    private String remark;
    private String throughFile;
    private String reason; // Stock Adjustment Reason from master item
    private String stockAdjustmentReason; // Stock Adjustment Reason from master item
    private String memo;
    private Date businessDate;
    private User creator;
    private Date createdOn;
    private User modifier;
    private Date modifiedOn;
    private StockAdjustmentDraft stockAdjDraft;
    private Company company;
    private AdjustmentStatus status;
    private Inventory inventoryRef;
    private String adjustmentType;
    private Set<StockAdjustmentDetail> stockAdjustmentDetail;
    private JournalEntry journalEntry;
    private StockAdjustmentCustomData stockAdjustmentCustomData;
    private StockAdjustmentCustomData stockAdjustmentLineLevelCustomData;
    private TransactionModule transactionModule;
    private JournalEntry inventoryJE;
    private CycleCount cyclecount;
    
    private boolean isJobWorkIn;
    private boolean priceupdated;
    private long creationdate;
    private boolean isdeleted;
    private RepairGRODetailISTMapping rejectedRepairGRODetailISTMapping; /* Stock rejected from repair store */
    private RepairWOCDISTMapping rejectedRepairWOCDetailISTMapping;/* Stock rejected from repair store FOR MRP transaction*/
    /**
     * Approved stock outs from QC store.
     */
    private DODQCISTMapping approvedDODQCISTMapping;
    /**
     * Repaired stock out from repair store.
     */
    private RejectedDODQCISTMapping rejectedApprovedDODQCISTMapping;
    /**
     * Rejected stock out from repair store.
     */
    private RejectedDODQCISTMapping rejectedDODQCISTMapping;
    public StockAdjustment() {
        stockAdjustmentDetail = new HashSet<>();
        transactionModule = TransactionModule.STOCK_ADJUSTMENT;
    }

    public StockAdjustment(Product product, Store store, UnitOfMeasure uom, double quantity, double pricePerUnit, Date businessDate) {
        this();
        this.company = product.getCompany();
        this.product = product;
        this.store = store;
        this.uom = uom;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
        this.businessDate = businessDate;
    }

    public TransactionModule getTransactionModule() {
        return transactionModule;
    }

    public void setTransactionModule(TransactionModule transactionModule) {
        this.transactionModule = transactionModule;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public Set<StockAdjustmentDetail> getStockAdjustmentDetail() {
        return stockAdjustmentDetail;
    }

    public void setStockAdjustmentDetail(Set<StockAdjustmentDetail> stockAdjustmentDetail) {
        this.stockAdjustmentDetail = stockAdjustmentDetail;
    }

    public Inventory getInventoryRef() {
        return inventoryRef;
    }

    public void setInventoryRef(Inventory inventoryRef) {
        this.inventoryRef = inventoryRef;
    }

    public Packaging getPackaging() {
        return packaging;
    }

    public void setPackaging(Packaging packaging) {
        this.packaging = packaging;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public Date getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(Date businessDate) {
        this.businessDate = businessDate;
    }

    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public User getModifier() {
        return modifier;
    }

    public void setModifier(User modifier) {
        this.modifier = modifier;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStockAdjustmentReason() {
        return stockAdjustmentReason;
    }

    public void setStockAdjustmentReason(String stockAdjustmentReason) {
        this.stockAdjustmentReason = stockAdjustmentReason;
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

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    public StockAdjustmentDraft getStockAdjDraft() {
        return stockAdjDraft;
    }

    public void setStockAdjDraft(StockAdjustmentDraft stockAdjDraft) {
        this.stockAdjDraft = stockAdjDraft;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public AdjustmentStatus getStatus() {
        return status;
    }

    public void setStatus(AdjustmentStatus status) {
        this.status = status;
    }

    public double getFinalQuantity() {
        return finalQuantity;
    }

    public void setFinalQuantity(double finalQuantity) {
        this.finalQuantity = finalQuantity;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public StockAdjustmentCustomData getStockAdjustmentCustomData() {
        return stockAdjustmentCustomData;
    }

    public void setStockAdjustmentCustomData(StockAdjustmentCustomData stockAdjustmentCustomData) {
        this.stockAdjustmentCustomData = stockAdjustmentCustomData;
    }

    public StockAdjustmentCustomData getStockAdjustmentLineLevelCustomData() {
        return stockAdjustmentLineLevelCustomData;
    }

    public void setStockAdjustmentLineLevelCustomData(StockAdjustmentCustomData stockAdjustmentLineLevelCustomData) {
        this.stockAdjustmentLineLevelCustomData = stockAdjustmentLineLevelCustomData;
    }

    public boolean isPriceupdated() {               //To identify the StockOut/Sales Price is updated or not in stock adjustment
        return priceupdated;
    }

    public void setPriceupdated(boolean priceupdated) {
        this.priceupdated = priceupdated;
    }

    public long getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(long creationdate) {
        this.creationdate = creationdate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StockAdjustment other = (StockAdjustment) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public JournalEntry getInventoryJE() {
        return inventoryJE;
    }

    public void setInventoryJE(JournalEntry inventoryJE) {
        this.inventoryJE = inventoryJE;
    }

    public boolean isIsJobWorkIn() {
        return isJobWorkIn;
    }

    public void setIsJobWorkIn(boolean isJobWorkIn) {
        this.isJobWorkIn = isJobWorkIn;
    }

    public boolean isIsdeleted() {
        return isdeleted;
    }

    public void setIsdeleted(boolean isdeleted) {
        this.isdeleted = isdeleted;
    }

    public RepairGRODetailISTMapping getRejectedRepairGRODetailISTMapping() {
        return rejectedRepairGRODetailISTMapping;
    }

    public void setRejectedRepairGRODetailISTMapping(RepairGRODetailISTMapping rejectedRepairGRODetailISTMapping) {
        this.rejectedRepairGRODetailISTMapping = rejectedRepairGRODetailISTMapping;
    }

    public DODQCISTMapping getApprovedDODQCISTMapping() {
        return approvedDODQCISTMapping;
    }

    public void setApprovedDODQCISTMapping(DODQCISTMapping approvedDODQCISTMapping) {
        this.approvedDODQCISTMapping = approvedDODQCISTMapping;
    }

    public RejectedDODQCISTMapping getRejectedApprovedDODQCISTMapping() {
        return rejectedApprovedDODQCISTMapping;
    }

    public void setRejectedApprovedDODQCISTMapping(RejectedDODQCISTMapping rejectedApprovedDODQCISTMapping) {
        this.rejectedApprovedDODQCISTMapping = rejectedApprovedDODQCISTMapping;
    }

    public RejectedDODQCISTMapping getRejectedDODQCISTMapping() {
        return rejectedDODQCISTMapping;
    }

    public void setRejectedDODQCISTMapping(RejectedDODQCISTMapping rejectedDODQCISTMapping) {
        this.rejectedDODQCISTMapping = rejectedDODQCISTMapping;
    }

    public CycleCount getCyclecount() {
        return cyclecount;
    }

    public void setCyclecount(CycleCount cyclecount) {
        this.cyclecount = cyclecount;
    }

    public String getThroughFile() {
        return throughFile;
    }
    public void setThroughFile(String throughFile) {
        this.throughFile = throughFile;
    }

    public RepairWOCDISTMapping getRejectedRepairWOCDetailISTMapping() {
        return rejectedRepairWOCDetailISTMapping;
    }

    public void setRejectedRepairWOCDetailISTMapping(RepairWOCDISTMapping rejectedRepairWOCDetailISTMapping) {
        this.rejectedRepairWOCDetailISTMapping = rejectedRepairWOCDetailISTMapping;
    }
    
}
