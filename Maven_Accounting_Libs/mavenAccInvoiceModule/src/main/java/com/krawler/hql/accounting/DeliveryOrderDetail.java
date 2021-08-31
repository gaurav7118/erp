/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductBatch;
import com.krawler.hql.accounting.DeliveryOrder;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductBuild;

/**
 *
 * @author krawler-user
 */
public class DeliveryOrderDetail {

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
    private DeliveryOrder deliveryOrder;
    private SalesOrderDetail sodetails;
    private InvoiceDetail cidetails;
    private String sourcegoodsReceiptOrderDetailsid; 
    private Inventory inventory;
    private String partno;
    DeliveryOrderDetailCustomData deliveryOrderDetailCustomData;
    private String invstoreid;
    private String invlocid;
    private ProductBatch batch;
    private ProductBuild productbuild;
    private double rowTaxAmount;
    private double rowTermAmount;
    private double OtherTermNonTaxableAmount;
    private Tax tax;
    private double discount;
    private BOMDetail bomcode;  // to save bom of assembly and  customer assembly products
    private int discountispercent;
    private double rate;
    private double rateincludegst; // If Transcation including GST save Unit price including GST
    private boolean isFromVendorConsign;
    private String priceSource;
    private JournalEntryDetail inventoryJEdetail;
    private JournalEntryDetail costOfGoodsSoldJEdetail;
    private boolean isLineItemClosed;//flag for identifying whether dodetail is Closed manually or not value->'F' means not closed
    private String discountJson;                                                //Used to store json of discount masters applied in Price band screen for each row ERM-68;
    private String pricingBandMasterid;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.

    public String getSourcegoodsReceiptOrderDetailsid() {
        return sourcegoodsReceiptOrderDetailsid;
    }

    public void setSourcegoodsReceiptOrderDetailsid(String sourcegoodsReceiptOrderDetailsid) {
        this.sourcegoodsReceiptOrderDetailsid = sourcegoodsReceiptOrderDetailsid;
    }

    public String getDiscountJson() {
        return discountJson;
    }

    public void setDiscountJson(String discountJson) {
        this.discountJson = discountJson;
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

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
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

    public SalesOrderDetail getSodetails() {
        return sodetails;
    }

    public void setSodetails(SalesOrderDetail sodetails) {
        this.sodetails = sodetails;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public InvoiceDetail getCidetails() {
        return cidetails;
    }

    public void setCidetails(InvoiceDetail cidetails) {
        this.cidetails = cidetails;
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

    public DeliveryOrderDetailCustomData getDeliveryOrderDetailCustomData() {
        return deliveryOrderDetailCustomData;
    }

    public void setDeliveryOrderDetailCustomData(DeliveryOrderDetailCustomData deliveryOrderDetailCustomData) {
        this.deliveryOrderDetailCustomData = deliveryOrderDetailCustomData;
    }

    public String getInvstoreid() {
        return invstoreid;
    }

    public void setInvstoreid(String invstoreid) {
        this.invstoreid = invstoreid;
    }

    public String getInvlocid() {
        return invlocid;
    }

    public void setInvlocid(String invlocid) {
        this.invlocid = invlocid;
    }

    public ProductBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductBatch batch) {
        this.batch = batch;
    }

    public double getRate() {
        return rate;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
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
    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
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

    public void setRate(double rate) {
        this.rate = rate;
    }

    public boolean isIsFromVendorConsign() {
        return isFromVendorConsign;
    }

    public void setIsFromVendorConsign(boolean isFromVendorConsign) {
        this.isFromVendorConsign = isFromVendorConsign;
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

    public JournalEntryDetail getCostOfGoodsSoldJEdetail() {
        return costOfGoodsSoldJEdetail;
    }

    public void setCostOfGoodsSoldJEdetail(JournalEntryDetail costOfGoodsSoldJEdetail) {
        this.costOfGoodsSoldJEdetail = costOfGoodsSoldJEdetail;
    }

    public BOMDetail getBomcode() {
        return bomcode;
    }

    public void setBomcode(BOMDetail bomcode) {
        this.bomcode = bomcode;
    }

    public double getRateincludegst() {
        return rateincludegst;
    }

    public void setRateincludegst(double rateincludegst) {
        this.rateincludegst = rateincludegst;
    }    
    
    public ProductBuild getProductbuild() {
        return productbuild;
    }

    public void setProductbuild(ProductBuild productbuild) {
        this.productbuild = productbuild;
    }

    public boolean isIsLineItemClosed() {
        return isLineItemClosed;
    }

    public void setIsLineItemClosed(boolean isLineItemClosed) {
        this.isLineItemClosed = isLineItemClosed;
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
