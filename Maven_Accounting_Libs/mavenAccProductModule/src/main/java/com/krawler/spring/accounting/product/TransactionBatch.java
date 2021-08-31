/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.product;

import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public class TransactionBatch {

    public static final int DocType_OPENING = -1;
    public static final int DocType_INITIAL = 0;
    public static final int DocType_ASSEMBLY_MAIN = 6;
    public static final int DocType_GRN = 1;
    public static final int DocType_PURCHASE_RETURN = 2;
    public static final int DocType_DO = 3;
    public static final int DocType_SALES_RETURN = 4;
    public static final int DocType_ASSEMBLY_SUB = 5;
    public static final int DocType_SA_IN = 7; // Stock Adjustment IN
    public static final int DocType_SA_OUT = 8; // Stock Adjustment Out/Sales
    public static final int DocType_SR_ISSUE = 9; // Stock request Issue
    public static final int DocType_SR_COLLECT = 10; // Stock request Collect
    public static final int DocType_IN_ISSUE = 11; // Issue Note Issue
    public static final int DocType_IN_COLLECT = 12; // Issue Note Collect
    public static final int DocType_IST_ISSUE = 13; // Inter Store Transfer Issue
    public static final int DocType_IST_COLLECT = 14; // Inter Store Transfer Collect
    public static final int DocType_ILT_ISSUE = 15; // Inter Location Transfer Issue
    public static final int DocType_ILT_COLLECT = 16;// Inter Location Transfer Collect
    public static final int DocType_WO_IN = 17; // work order assembly item IN
    public static final int DocType_WO_OUT = 18; // work order inventory part item OUT
    private double price;
    private double quantity;
    private double quantityDue;
    private double removedQty; // out
    private boolean outEntry; // out
    private boolean opening;
    private double withoutlanded; // ERM-447 rate of product withoutlandedcost
    private int batchNo;
    private String warehouseId;
    private String locationId;
    private String rowId;
    private String rackId;
    private String binId;
    private String batchId;
    private String serialId;
    private String key;
    private String transactionId;
    private int docType;
    private String transactionNo;
    private Date transactionDate;
    private String personCode;
    private String personName;
    private String billid;
    private Integer srNo;
    private Long createdon;
    private String assemblyProductID;
    private String remark;
    private String stockUOMID;
    private String costCenterID;
    private String memo;
    /* IN transaction details for OUT transaction 
     String - IN transaction detail ID,
     List[0] - Quantity,
     List[1] - Rate, 
     List[2] - Amount i.e. Quantity * Rate */
    private Map<String, List> inTransactionQtyAmountMap;
    private boolean periodTransaction; // transaction that fall in selected period
    /* OUT transaction details for IN transaction 
     String - IN transaction detail ID,
     List[0] - Quantity,
     List[1] - Rate, 
     List[2] - Amount i.e. Quantity * Rate */
    private Map<String, List<Double>> outTransactionQtyAmountMap;
    
    /**
     * <code>shouldUpdatePrice</code> is used to check whether the price for
     * purchase return transaction should be update according to valuation or
     * not. We are using this flag to differentiate the purchase return
     * transaction with delivery order.
     */
    private boolean shouldUpdatePrice;

    private boolean advanceSearchTransaction;
    private String companyid;
    
    /**
     * ERM-447 extraJSON fields to get landed invoice cost and category values.
     */

    private JSONObject extraJSON;

    public JSONObject getExtraJSON() {
        return extraJSON;
    }

    public void setExtraJSON(JSONObject extraJSON) {
        this.extraJSON = extraJSON;
    }

    public double getWithoutlanded() {
        return withoutlanded;
    }

    public void setWithoutlanded(double withoutlanded) {
        this.withoutlanded = withoutlanded;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public Map<String, List<Double>> getOutTransactionQtyAmountMap() {
        return outTransactionQtyAmountMap;
    }

    public void setOutTransactionQtyAmountMap(Map<String, List<Double>> outTransactionQtyAmountMap) {
        this.outTransactionQtyAmountMap = outTransactionQtyAmountMap;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getPersonCode() {
        return personCode;
    }

    public void setPersonCode(String personCode) {
        this.personCode = personCode;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public Integer getSrNo() {
        return srNo;
    }

    public void setSrNo(Integer srNo) {
        this.srNo = srNo;
    }

    public Long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(Long createdon) {
        this.createdon = createdon;
    }

    public TransactionBatch(double price, double quantity, boolean opening, int batchNo, String batchId, String serialId) {
        this.outEntry = false;
        this.price = price;
        this.quantity = quantity;
        this.opening = opening;
        this.batchNo = batchNo;
        this.batchId = batchId;
        this.serialId = serialId;
        this.shouldUpdatePrice = true;
    }

    public TransactionBatch(int docType, String transactionId, String key, double price, double quantity, boolean opening, int batchNo) {
        this(price, quantity, opening, batchNo, "", "");
        this.key = key;
        this.transactionId = transactionId;
        this.docType = docType;
        this.shouldUpdatePrice = true;
    }

    public TransactionBatch(int docType, String transactionId, String key, double price, double quantity, boolean opening, int batchNo, String personCode, String personName, String transactionNo, Date tranDate, String billid, Integer srNo, Long createdon, boolean isPeriodTransaction) {
        this(price, quantity, opening, batchNo, "", "");
        this.key = key;
        this.transactionId = transactionId;
        this.docType = docType;
        this.personCode = personCode;
        this.personName = personName;
        this.transactionNo = transactionNo;
        this.transactionDate = tranDate;
        this.billid = billid;
        this.srNo = srNo;
        this.createdon = createdon;
        this.periodTransaction = isPeriodTransaction;
        this.shouldUpdatePrice = true;
    }

    public boolean isPeriodTransaction() {
        return periodTransaction;
    }

    public void setPeriodTransaction(boolean periodTransaction) {
        this.periodTransaction = periodTransaction;
    }

    public int getDocType() {
        return docType;
    }

    public void setDocType(int docType) {
        this.docType = docType;
    }

    public double getQuantityDue() {
        return quantityDue;
    }

    public void setQuantityDue(double quantityDue) {
        this.quantityDue = quantityDue;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public boolean isOpening() {
        return opening;
    }

    public void setOpening(boolean opening) {
        this.opening = opening;
    }

    public int getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(int batchNo) {
        this.batchNo = batchNo;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public boolean isOutEntry() {
        return outEntry;
    }

    public void setOutEntry(boolean outEntry) {
        this.outEntry = outEntry;
    }

    public double getRemovedQty() {
        return removedQty;
    }

    public void setRemovedQty(double removedQty) {
        this.removedQty = removedQty;
    }

    public String getBatchId() {
        return batchId;
    }

    public String getBinId() {
        return binId;
    }

    public void setBinId(String binId) {
        this.binId = binId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getRackId() {
        return rackId;
    }

    public void setRackId(String rackId) {
        this.rackId = rackId;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public double getAvailableQty() {
        return quantity - removedQty;
    }

    public String getBillid() {
        return billid;
    }

    public void setBillid(String billid) {
        this.billid = billid;
    }
    
    public double getAmount() {
        return quantity * price;
    }
    public boolean isPartialStorageContains(Map<String, String> partialStorageDetail) {
        boolean contains = true;
        if (partialStorageDetail != null && !partialStorageDetail.isEmpty()) {
            String pWhId = partialStorageDetail.get("warehouseId");
            String pLocId = partialStorageDetail.get("locationId");
            String pRowId = partialStorageDetail.get("rowId");
            String pRackId = partialStorageDetail.get("rackId");
            String pBinId = partialStorageDetail.get("binId");
            String pBatchId = partialStorageDetail.get("batchName");
            String pSerialId = partialStorageDetail.get("serialName");
            if (pWhId != null && !pWhId.equals(warehouseId)) {
                contains = false;
            } else if (pLocId != null && !pLocId.equals(locationId)) {
                contains = false;
            } else if (pRowId != null && !pRowId.equals(rowId)) {
                contains = false;
            } else if (pRackId != null && !pRackId.equals(rackId)) {
                contains = false;
            } else if (pBinId != null && !pBinId.equals(binId)) {
                contains = false;
            } else if (pBatchId != null && !pBatchId.equals(batchId)) {
                contains = false;
            } else if (pSerialId != null && !pSerialId.equals(serialId)) {
                contains = false;
            }
        }
        return contains;
    }

    public String getPartialStorageKey(PriceValuationStack.StorageFilter[] storageCombinations) {
        StringBuilder key = new StringBuilder();
        for (PriceValuationStack.StorageFilter combination : storageCombinations) {
            switch (combination) {
                case WAREHOUSE:
                    if (!StringUtil.isNullOrEmpty(warehouseId)) {
                        key.append(warehouseId);
                    }
                    break;
                case LOCATION:
                    if (!StringUtil.isNullOrEmpty(locationId)) {
                        key.append(locationId);
                    }
                    break;
                case ROW:
                    if (!StringUtil.isNullOrEmpty(rowId)) {
                        key.append(rowId);
                    }
                    break;
                case RACK:
                    if (!StringUtil.isNullOrEmpty(rackId)) {
                        key.append(rackId);
                    }
                    break;
                case BIN:
                    if (!StringUtil.isNullOrEmpty(binId)) {
                        key.append(binId);
                    }
                    break;
                case BATCH:
                    if (!StringUtil.isNullOrEmpty(batchId)) {
                        key.append(batchId);
                    }
                    break;
                case SERIAL:
                    if (!StringUtil.isNullOrEmpty(serialId)) {
                        key.append(serialId);
                    }
                    break;
            }
        }
        return key.toString();
    }


    public Map<String, List> getInTransactionQtyAmountMap() {
        return inTransactionQtyAmountMap;
    }

    public void setInTransactionQtyAmountMap(Map<String, List> inTransactionQtyAmountMap) {
        this.inTransactionQtyAmountMap = inTransactionQtyAmountMap;
    }

    public String getAssemblyProductID() {
        return assemblyProductID;
    }

    public void setAssemblyProductID(String assemblyProductID) {
        this.assemblyProductID = assemblyProductID;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getStockUOMID() {
        return stockUOMID;
    }

    public void setStockUOMID(String stockUOMID) {
        this.stockUOMID = stockUOMID;
    }

    public String getCostCenterID() {
        return costCenterID;
    }

    public void setCostCenterID(String costCenterID) {
        this.costCenterID = costCenterID;
    }
    
    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean isShouldUpdatePrice() {
        return shouldUpdatePrice;
    }

    public void setShouldUpdatePrice(boolean shouldUpdatePrice) {
        this.shouldUpdatePrice = shouldUpdatePrice;
    }

    public boolean isAdvanceSearchTransaction() {
        return advanceSearchTransaction;
}

    public void setAdvanceSearchTransaction(boolean advanceSearchTransaction) {
        this.advanceSearchTransaction = advanceSearchTransaction;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }
}
