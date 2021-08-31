/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductBatch;

/**
 *
 * @author krawler
 */
public class GoodsReceiptOrderDetails {

    private String ID;
    private int srno;
    private Product product;
    private double actualQuantity;
    private double deliveredQuantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomdeliveredquantity;
    private double baseuomrate;
    private String description;
    private String remark;
    private Company company;
    private GoodsReceiptOrder grOrder;
    private GoodsReceiptDetail videtails;
    private PurchaseOrderDetail podetails;
    private String partno;
    private  SecurityGateDetails securitydetails;
    private Inventory inventory;
    private String invstoreid;
    private String shelfLocation;
    private String invlocid;
    private String supplierpartnumber;
    GoodsReceiptOrderDetailsCustomDate goodsReceiptOrderDetailsCustomDate;
    GoodsReceiptOrderProductCustomData groProductcustomdata;
    private ProductBatch batch;
    private double rowTaxAmount;
    private double rowTermAmount;
    private double OtherTermNonTaxableAmount;
    private Tax tax;
    private double discount;
    private int discountispercent;
    private double rate;
    private double rateincludegst; // If Transcation including GST save Unit price including GST
    private String priceSource;
    private JournalEntryDetail inventoryJEdetail; //debit JED
    private JournalEntryDetail purchasesJEDetail; //credit JED
    private BOMDetail bomcode;
    private String sourceDeliveryOrderDetailid;
    private String pricingBandMasterid;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.

    public String getSourceDeliveryOrderDetailid() {
        return sourceDeliveryOrderDetailid;
    }

    public void setSourceDeliveryOrderDetailid(String sourceDeliveryOrderDetailid) {
        this.sourceDeliveryOrderDetailid = sourceDeliveryOrderDetailid;
    }
   
    public BOMDetail getBomcode() {
        return bomcode;
    }

    public void setBomcode(BOMDetail bomcode) {
        this.bomcode = bomcode;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public double getBaseuomquantity() {
        return baseuomquantity;
    }

    public void setBaseuomquantity(double baseuomquantity) {
        this.baseuomquantity = baseuomquantity;
    }

    public double getBaseuomdeliveredquantity() {
        return baseuomdeliveredquantity;
    }

    public void setBaseuomdeliveredquantity(double baseuomdeliveredquantity) {
        this.baseuomdeliveredquantity = baseuomdeliveredquantity;
    }

    public double getBaseuomrate() {
        return baseuomrate;
    }

    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public double getDeliveredQuantity() {
        return deliveredQuantity;
    }

    public void setDeliveredQuantity(double deliveredQuantity) {
        this.deliveredQuantity = deliveredQuantity;
    }

    public GoodsReceiptOrder getGrOrder() {
        return grOrder;
    }

    public void setGrOrder(GoodsReceiptOrder grOrder) {
        this.grOrder = grOrder;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public GoodsReceiptDetail getVidetails() {
        return videtails;
    }

    public void setVidetails(GoodsReceiptDetail videtails) {
        this.videtails = videtails;
    }

    public PurchaseOrderDetail getPodetails() {
        return podetails;
    }

    public void setPodetails(PurchaseOrderDetail podetails) {
        this.podetails = podetails;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getPartno() {
        return partno;
    }

    public void setPartno(String partno) {
        this.partno = partno;
    }

    public GoodsReceiptOrderDetailsCustomDate getGoodsReceiptOrderDetailsCustomDate() {
        return goodsReceiptOrderDetailsCustomDate;
    }

    public void setGoodsReceiptOrderDetailsCustomDate(GoodsReceiptOrderDetailsCustomDate goodsReceiptOrderDetailsCustomDate) {
        this.goodsReceiptOrderDetailsCustomDate = goodsReceiptOrderDetailsCustomDate;
    }

    public GoodsReceiptOrderProductCustomData getGroProductcustomdata() {
        return groProductcustomdata;
    }

    public void setGroProductcustomdata(GoodsReceiptOrderProductCustomData groProductcustomdata) {
        this.groProductcustomdata = groProductcustomdata;
    }
    
    public String getInvstoreid() {
        return invstoreid;
    }

    public void setInvstoreid(String invstoreid) {
        this.invstoreid = invstoreid;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public String getInvlocid() {
        return invlocid;
    }

    public void setInvlocid(String invlocid) {
        this.invlocid = invlocid;
    }

    public String getSupplierpartnumber() {
        return supplierpartnumber;
    }

    public void setSupplierpartnumber(String supplierpartnumber) {
        this.supplierpartnumber = supplierpartnumber;
    }

    public ProductBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductBatch batch) {
        this.batch = batch;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public int getDiscountispercent() {
        return discountispercent;
    }

    public void setDiscountispercent(int discountispercent) {
        this.discountispercent = discountispercent;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }
    public double getRowTermAmount() {
        return rowTermAmount;
    }
    public void setRowTermAmount(double rowTermAmount) {
        this.rowTermAmount = rowTermAmount;
    }
     public double getOtherTermNonTaxableAmount() {
        return OtherTermNonTaxableAmount;
    }

    public void setOtherTermNonTaxableAmount(double OtherTermNonTaxableAmount) {
        this.OtherTermNonTaxableAmount = OtherTermNonTaxableAmount;
    }
    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public double getRate() {
        return rate;
    }

    public double getRateincludegst() {
        return rateincludegst;
    }

    public void setRateincludegst(double rateincludegst) {
        this.rateincludegst = rateincludegst;
    } 
    
    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getPriceSource() {
        return priceSource;
    }

    public void setPriceSource(String priceSource) {
        this.priceSource = priceSource;
    }

    public JournalEntryDetail getInventoryJEdetail() {
        return inventoryJEdetail;
}

    public void setInventoryJEdetail(JournalEntryDetail inventoryJEdetail) {
        this.inventoryJEdetail = inventoryJEdetail;
    }

    public JournalEntryDetail getPurchasesJEDetail() {
        return purchasesJEDetail;
    }

    public void setPurchasesJEDetail(JournalEntryDetail purchasesJEDetail) {
        this.purchasesJEDetail = purchasesJEDetail;
    }

    /**
     * @return the securitydetails
     */
    public SecurityGateDetails getSecuritydetails() {
        return securitydetails;
    }

    /**
     * @param securitydetails the securitydetails to set
     */
    public void setSecuritydetails(SecurityGateDetails securitydetails) {
        this.securitydetails = securitydetails;
    }

    public String getPricingBandMasterid() {
        return pricingBandMasterid;
    }

    public void setPricingBandMasterid(String pricingBandMasterid) {
        this.pricingBandMasterid = pricingBandMasterid;
    }
    
    public boolean isIsUserModifiedTaxAmount() {
        return isUserModifiedTaxAmount;
    }

    public void setIsUserModifiedTaxAmount(boolean isUserModifiedTaxAmount) {
        this.isUserModifiedTaxAmount = isUserModifiedTaxAmount;
    }
    
}
