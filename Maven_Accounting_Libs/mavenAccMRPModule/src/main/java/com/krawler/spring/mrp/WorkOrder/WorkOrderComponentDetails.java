/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.hql.accounting.Product;

/**
 *
 * @author krawler
 */
public class WorkOrderComponentDetails {

    private String ID;
    private Product product;
    private Product parentProduct;
    private WorkOrder workOrder;
    private double requiredQuantity;
    private double availableQuantity;
    private double blockQuantity;
    private double rejectedQuantity; // saves rejected ites at task level
    private double wastedQuantity; // saves wasted ites at task level
    private double recycledQuantity; // saves recycled ites at task level
    private double blockQuantityUsed;
    private double initialPurchasePrice;
    private double minpercent;
    private String consumptionDetails;
    private boolean blockQtyUsed;
    private boolean blockedFromCA;   
    private double producedQuantity;
    private Inventory inventory;
    private Inventory inventoryProduced;
    private Inventory inventoryReturnedOut;
    private Inventory inventoryReturnedIn;
    private Inventory inventoryWasteOut;
    private Inventory inventoryWasteIn;
    private Inventory inventoryRecycleIn;
    private Inventory inventoryRecycleOut;
    private String taskId;
    private String taskName;
    private JournalEntryDetail inventoryJEdetail;
    private double returnQuantity;
    private String blockDetails;

    public JournalEntryDetail getInventoryJEdetail() {
        return inventoryJEdetail;
    }

    public void setInventoryJEdetail(JournalEntryDetail inventoryJEdetail) {
        this.inventoryJEdetail = inventoryJEdetail;
    }
    
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public void setWorkOrder(WorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(double availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public double getBlockQuantity() {
        return blockQuantity;
    }

    public void setBlockQuantity(double blockQuantity) {
        this.blockQuantity = blockQuantity;
    }

    public double getInitialPurchasePrice() {
        return initialPurchasePrice;
    }

    public void setInitialPurchasePrice(double initialPurchasePrice) {
        this.initialPurchasePrice = initialPurchasePrice;
    }

    public double getMinpercent() {
        return minpercent;
    }

    public void setMinpercent(double minpercent) {
        this.minpercent = minpercent;
    }

    public Product getParentProduct() {
        return parentProduct;
    }

    public void setParentProduct(Product parentProduct) {
        this.parentProduct = parentProduct;
    }

    public double getBlockQuantityUsed() {
        return blockQuantityUsed;
    }

    public void setBlockQuantityUsed(double blockQuantityUsed) {
        this.blockQuantityUsed = blockQuantityUsed;
    }

    public String getConsumptionDetails() {
        return consumptionDetails;
    }

    public void setConsumptionDetails(String consumptionDetails) {
        this.consumptionDetails = consumptionDetails;
    }

    public boolean isBlockQtyUsed() {
        return blockQtyUsed;
    }

    public void setBlockQtyUsed(boolean blockQtyUsed) {
        this.blockQtyUsed = blockQtyUsed;
    }

    public double getProducedQuantity() {
        return producedQuantity;
    }

    public void setProducedQuantity(double producedQuantity) {
        this.producedQuantity = producedQuantity;
    }

    public double getRejectedQuantity() {
        return rejectedQuantity;
    }

    public void setRejectedQuantity(double rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
    }

    public double getWastedQuantity() {
        return wastedQuantity;
    }

    public void setWastedQuantity(double wastedQuantity) {
        this.wastedQuantity = wastedQuantity;
    }

    public double getRecycledQuantity() {
        return recycledQuantity;
    }

    public void setRecycledQuantity(double recycledQuantity) {
        this.recycledQuantity = recycledQuantity;
    }
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    public double getReturnQuantity() {
        return returnQuantity;
    }

    public Inventory getInventoryProduced() {
        return inventoryProduced;
    }

    public void setInventoryProduced(Inventory inventoryProduced) {
        this.inventoryProduced = inventoryProduced;
    }
    
    
    public Inventory getInventoryReturnedOut() {
        return inventoryReturnedOut;
    }

    public void setInventoryReturnedOut(Inventory inventoryReturnedOut) {
        this.inventoryReturnedOut = inventoryReturnedOut;
    }

    public Inventory getInventoryReturnedIn() {
        return inventoryReturnedIn;
    }

    public void setInventoryReturnedIn(Inventory inventoryReturnedIn) {
        this.inventoryReturnedIn = inventoryReturnedIn;
    }
    
    
    public Inventory getInventoryWasteOut() {
        return inventoryWasteOut;
    }

    public void setInventoryWasteOut(Inventory inventoryWasteOut) {
        this.inventoryWasteOut = inventoryWasteOut;
    }

    public Inventory getInventoryWasteIn() {
        return inventoryWasteIn;
    }

    public void setInventoryWasteIn(Inventory inventoryWasteIn) {
        this.inventoryWasteIn = inventoryWasteIn;
    }

    public Inventory getInventoryRecycleIn() {
        return inventoryRecycleIn;
    }

    public void setInventoryRecycleIn(Inventory inventoryRecycleIn) {
        this.inventoryRecycleIn = inventoryRecycleIn;
    }
    
    public Inventory getInventoryRecycleOut() {
        return inventoryRecycleOut;
    }

    public void setInventoryRecycleOut(Inventory inventoryRecycleOut) {
        this.inventoryRecycleOut = inventoryRecycleOut;
    }
    
    public void setReturnQuantity(double returnQuantity) {
        this.returnQuantity = returnQuantity;
    }
    
    public String getBlockDetails() {
        return blockDetails;
    }

    public void setBlockDetails(String blockDetails) {
        this.blockDetails = blockDetails;
    }
       
    public boolean isBlockedFromCA() {
        return blockedFromCA;
    }

    public void setBlockedFromCA(boolean blockedFromCA) {
        this.blockedFromCA = blockedFromCA;
    }
    
    public static final String POJONAME = "WorkOrderComponentDetails";
    public static final String DB_WORKORDERID = "workOrder.ID";
    public static final String BATCH_DETAILS = "batchdetails";
    public static final String PRODUCE_DETAILS = "producedqtydetails";
    public static final String WASTE_DETAILS = "wasteqtydetails";
    public static final String PARAM_PRODUCED_QUANTITY = "producedquantity";
    public static final String PARAM_ACTUAL_QUANTITY = "actualquantity";
    public static final String PARAM_ID = "id";
    public static final String PARAM_REQUIRED_QUANTITY = "requiredquantity";
    public static final String PARAM_AVAILABLE_QUANTITY = "availablequantity";

    public static final String PARAM_REJECTED_QUANTITY = "rejectedQuantity";
    public static final String PARAM_WASTE_QUANTITY = "wasteQuantity";
    public static final String PARAM_RECYCLE_QUANTITY = "recycleQuantity";

    public static final String PARAM_BLOCKED_QUANTITY = "blockquantity";
    public static final String PARAM_PURCHASE_PRICE = "purchaseprice";
    public static final String PARAM_PRODUCT_DESC = "desc";
    public static final String PARAM_PRODUCT_TYPE_ID = "producttype";
    public static final String PARAM_PRODUCT_TYPE_NAME = "type";
}
