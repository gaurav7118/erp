/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.assemblyQA;

import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.admin.StoreMaster;
import com.krawler.hql.accounting.ProductBuild;
import com.krawler.inventory.model.location.Location;
import java.util.Date;

public class AssemblyProductApprovalDetails {

    private String id;
    private Location location;
    private NewProductBatch productBatch;
    private String batchname;
    private NewBatchSerial serial;
    private String serialname;
    private double quantity;
    private ProductBuild prBuild;
    private AssemblyQAStatus approvalStatus;
    private InventoryWarehouse warehouse; //to save warehouse id where approved quantity will be transferred
    private StoreMaster row;
    private StoreMaster rack;
    private StoreMaster bin;

    private Date mfgdate; // for newproductbatch
    private Date expdate;// for newproductbatch

    private Date expfromdate;// for newbatchserial
    private Date exptodate;// for newbatchserial

    private String remark; // for QA
    private String reason;// for repair
    
    private Date inspectionDate;
    private Date reapirDate;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(Date inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public Date getReapirDate() {
        return reapirDate;
    }

    public void setReapirDate(Date reapirDate) {
        this.reapirDate = reapirDate;
    }
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
   
    public Date getMfgdate() {
        return mfgdate;
    }

    public void setMfgdate(Date mfgdate) {
        this.mfgdate = mfgdate;
    }

    public Date getExpdate() {
        return expdate;
    }

    public void setExpdate(Date expdate) {
        this.expdate = expdate;
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

    public String getBatchname() {
        return batchname;
    }

    public void setBatchname(String batchname) {
        this.batchname = batchname;
    }

    public String getSerialname() {
        return serialname;
    }

    public void setSerialname(String serialname) {
        this.serialname = serialname;
    }
   
   
    
    public StoreMaster getRow() {
        return row;
    }

    public void setRow(StoreMaster row) {
        this.row = row;
    }

    public StoreMaster getRack() {
        return rack;
    }

    public void setRack(StoreMaster rack) {
        this.rack = rack;
    }

    public StoreMaster getBin() {
        return bin;
    }

    public void setBin(StoreMaster bin) {
        this.bin = bin;
    }

    public InventoryWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(InventoryWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public NewProductBatch getProductBatch() {
        return productBatch;
    }

    public void setProductBatch(NewProductBatch productBatch) {
        this.productBatch = productBatch;
    }

    public NewBatchSerial getSerial() {
        return serial;
    }

    public void setSerial(NewBatchSerial serial) {
        this.serial = serial;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public ProductBuild getPrBuild() {
        return prBuild;
    }

    public void setPrBuild(ProductBuild prBuild) {
        this.prBuild = prBuild;
    }

    public AssemblyQAStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(AssemblyQAStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
   
}
