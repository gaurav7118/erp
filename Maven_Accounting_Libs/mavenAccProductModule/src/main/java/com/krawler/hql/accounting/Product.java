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

import com.krawler.common.admin.*;
import com.krawler.common.util.ItemReusability;
import com.krawler.common.admin.LandingCostCategory;
import com.krawler.common.util.LicenseType;
import com.krawler.common.util.ValuationMethod;           //INV_ACC_MERGE
import com.krawler.inventory.model.frequency.Frequency;   //INV_ACC_MERGE
import com.krawler.inventory.model.inspection.InspectionArea;
import com.krawler.inventory.model.inspection.InspectionTemplate;
import com.krawler.inventory.model.packaging.Packaging;   //INV_ACC_MERGE
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class Product implements Comparable {

    private String ID;
    private String name;
    private String description;
    private double reorderQuantity;
    private double recycleQuantity;
    private double reorderLevel;
    private double minOrderingQuantity;
    private double maxOrderingQuantity;
    private double availableQuantity;
    private int leadTimeInDays;
    private int QALeadTimeInDays;
    private int warrantyperiod;
    private int warrantyperiodsal;
    private UnitOfMeasure unitOfMeasure;
    private Account salesAccount;
    private Account purchaseAccount;
    private Account salesReturnAccount;
    private Account purchaseReturnAccount;
    private Account interStatePurchaseAccount;
    private Account interStatePurchaseAccountCForm;
    private Account interStatePurchaseReturnAccount;
    private Account interStatePurchaseReturnAccountCForm;
    private Account inputVAT;
    private Account cstVAT;
    private Account cstVATattwo;
    private Account interStateSalesAccount;
    private Account interStateSalesAccountCForm;
    private Account interStateSalesReturnAccount;
    private Account interStateSalesReturnAccountCForm;
    private Account inputVATSales;
    private Account cstVATSales;
    private Account cstVATattwoSales;
    private Account salesRevenueRecognitionAccount;
    private Account itcAccount;  // Used for Blocked ITC ERP-41416
    private Company company;
    private Product parent;
    private boolean syncable;
    private boolean recyclable;
    private boolean multiuom;
    private boolean blockLooseSell;
    private boolean deleted;
    private Set<Product> children;
    private String productid;
    /*
     * For malaysian Company industryCodeId is used
     */
    private String industryCodeId;
    private Producttype producttype;
    private Vendor vendor;
    private int fromInventory; //From Inventory Web Application(1) and From Inventory Web Service(2) default is (0)
    private AccProductCustomData productCustomData;
    private ProductBatch batch;
    private String supplier;
    private String coilcraft;
    private ShelfLocation shelfLocation;
    private String interplant;
    private int seqnumber;//Only to store integer part of sequence format
    private SequenceFormat seqformat;
    private String datePreffixValue;//Only to store Date Preffix part of sequence format
    private String dateSuffixValue;//Only to store Date Sufefix part of sequence format
    private String dateAfterPreffixValue;//Only to store Date After Prefix part of sequence format
    private boolean qaenable;
    private PriceType dependenttype;
    private boolean intervalfield;
    private boolean addshiplentheithqty;
    private int timeinterval;
    private int noofquqntity;
    private String noofqtyvalue;
    private int isImport;
    private boolean asset;
    private boolean depreciable;
    private InventoryLocation location;
    private InventoryWarehouse warehouse;
    private double totalIssueCount;
    private KWLCurrency currency;
    /*
     * depreciationMethod = 1 == straight line method depreciationMethod = 2 ==
     * Double Declining Balance Method depreciationMethod = 3 == Non Depreciable
     */
    private int depreciationMethod;
    private double depreciationRate;
    private int depreciationCostLimit;
    private Account depreciationGLAccount;
    private Account depreciationProvisionGLAccount;
    private Account sellAssetGLAccount;
    private Account writeOffAssetAccount;
    private boolean revenueRecognitionProcess;
    private boolean isBatchForProduct;
    private boolean islocationforproduct;
    private boolean iswarehouseforproduct;
    private boolean isSerialForProduct;
    private boolean isSKUForProduct;
    private long createdon;
    private Date asOfDate;
    private boolean autoAssembly; // used for Auto Build assembly on sales of product
    private double productweight;
    private double productWeightPerStockUom;
    private double productWeightIncludingPakagingPerStockUom;
    private double productVolumePerStockUom;
    private double productVolumeIncludingPakagingPerStockUom;
    private Set<ProductComposition> rows;
    boolean activateProductComposition;
    private int barcodefield;
    private boolean isrowforproduct;
    private boolean israckforproduct;
    private boolean isbinforproduct;
    private UOMschemaType uomSchemaType;
     
    // new properties    //INV_ACC_MERGE
    private String additionalDesc;
    private String barcode;
    private String descInForeign;
    private String itemGroup;
    private String priceList;
    private String shippingType;
    private boolean isActive;
    private boolean isKnittingItem;
    private ItemReusability itemReusability;
    private int reusabilityCount;
    private String licenseCode;
    private LicenseType licenseType;
    
    private String customerCategory;
    private String serviceTaxCode;
//    private double abatementRate;
    private String excisemethodmain;  //Valuvation Type In UI Field (Product Tax Details(Excise Duty) Tab)
    private String excisemethodsubtype;
    private double exciserate;

    private UnitOfMeasure purchaseUOM;
    private double itemPurchaseLength;
    private double itemPurchaseWidth;
    private double itemPurchaseHeight;
    private double itemPurchaseVolume;
    private String purchaseMfg;
    private String catalogNo;
    
    private UnitOfMeasure salesUOM;
    private double itemSalesLength;
    private double itemSalesWidth;
    private double itemSalesHeight;
    private double itemSalesVolume;
    
    private String alternateProduct;
    
    private double itemLength;
    private double itemWidth;
    private double itemHeight;
    private double itemVolume;
    private String itemColor;
    
    private String additionalFreeText;
    
    private ValuationMethod valuationMethod;
    //private double itemCost;
    private String WIPOffset;
    private String InventoryOffset;
    private Packaging packaging;
    private UnitOfMeasure orderingUOM;
    private UnitOfMeasure transferUOM;
    private boolean countable;
    private Set<Frequency> cycleCountFrequencies;
    private InspectionTemplate inspectionTemplate;
    private String HSCode;
    private boolean wastageApplicable;
    private Account wastageAccount;
    private Product substituteProduct; // added field for MRP module
    private double substituteQty; // added field for MRP module
    
    private Product propagatedProductID;// this field is used to save parents companies product id against propagated product in child company.
    
    private MasterItem vatcommoditycode; // this field access Master Item object as vat commodity details are added 
    private boolean vatonmrp;
    private double mrprate;
    private double vatAbatementRate;
    private Date vatAbatementPeriodFromDate;
    private Date vatAbatementPeriodToDate;
    private String vatMethodType;
    private String reportinguomVAT;
    private UOMschemaType reportingSchemaTypeVAT;
    private String tariffName;
    private String HSNCode;
    private String reportinguom;
    private UOMschemaType reportingSchemaType;
    private String natureofStockItem;
    private MasterItem productBrand; // added field for ERP-22344 Discount in Percentage for each customer and brand

    private double openingBalanceAmount; // opening balance of a product in product currency
    private double openingBalanceAmountInBase; // opening balance of product in base currency
    private double exchangerateforopeningbalanceamount;
    private String SAC;
    private Set<LandingCostCategory> lccategoryid; 
    private UnitOfMeasure displayUoM;
    private boolean productCreatedOnAvalara;//Flag to indicate whether or not an item has been created on Avalara corresponding to the product. Used when Avalara Integration is enabled.
    private String purchasetaxid;//Purchase taxid mapped to product
    private String salestaxid;//Sales taxid mapped to product
    private int itcType; // used for ITC type for Indian GST ERP-41416

    public String getPurchasetaxid() {
        return purchasetaxid;
    }

    public void setPurchasetaxid(String purchasetaxid) {
        this.purchasetaxid = purchasetaxid;
    }
   
    
    public String getSalestaxid() {
        return salestaxid;
    }

    public void setSalestaxid(String salestaxid) {
        this.salestaxid = salestaxid;
    }

    public boolean isProductCreatedOnAvalara() {
        return productCreatedOnAvalara;
    }

    public void setProductCreatedOnAvalara(boolean productCreatedOnAvalara) {
        this.productCreatedOnAvalara = productCreatedOnAvalara;
    }
    
    public UnitOfMeasure getDisplayUoM() {
        return displayUoM;
    }

    public void setDisplayUoM(UnitOfMeasure displayUoM) {
        this.displayUoM = displayUoM;
    }
    
    public String getNatureofStockItem() {
        return natureofStockItem;
    }

    public void setNatureofStockItem(String natureofStockItem) {
        this.natureofStockItem = natureofStockItem;
    }
    private Account inventoryAccount; // "Inventory Account" to be used if MRP flow is activated
    private Account costOfGoodsSoldAccount; // "Cost of Goods Sold Account" to be used if MRP flow is activated
    private Account stockAdjustmentAccount; // "Stock Adjustment Account" to be used if MRP flow is activated
    private boolean rcmApplicable;

    public String getReportinguom() {
        return reportinguom;
    }

    public void setReportinguom(String reportinguom) {
        this.reportinguom = reportinguom;
    }


    public String getHSNCode() {
        return HSNCode;
    }

    public void setHSNCode(String HSNCode) {
        this.HSNCode = HSNCode;
    }

    public String getTariffName() {
        return tariffName;
    }

    public void setTariffName(String tariffName) {
        this.tariffName = tariffName;
    }
    
    public String getDatePreffixValue() {
        return datePreffixValue;
    }

    public void setDatePreffixValue(String datePreffixValue) {
        this.datePreffixValue = datePreffixValue;
    }

    public String getDateSuffixValue() {
        return dateSuffixValue;
    }

    public void setDateSuffixValue(String dateSuffixValue) {
        this.dateSuffixValue = dateSuffixValue;
    }
    
    public Product getPropagatedProductID() {
        return propagatedProductID;
    }

    public void setPropagatedProductID(Product propagatedProductID) {
        this.propagatedProductID = propagatedProductID;
    }
    
    public InspectionTemplate getInspectionTemplate() {
        return inspectionTemplate;
    }

    public void setInspectionTemplate(InspectionTemplate inspectionTemplate) {
        this.inspectionTemplate = inspectionTemplate;
    }

    public String getInventoryOffset() {
        return InventoryOffset;
    }

    public void setInventoryOffset(String InventoryOffset) {
        this.InventoryOffset = InventoryOffset;
    }

    public String getWIPOffset() {
        return WIPOffset;
    }

    public void setWIPOffset(String WIPOffset) {
        this.WIPOffset = WIPOffset;
    }

    public String getAdditionalDesc() {
        return additionalDesc;
    }

    public void setAdditionalDesc(String additionalDesc) {
        this.additionalDesc = additionalDesc;
    }

    public String getAdditionalFreeText() {
        return additionalFreeText;
    }

    public void setAdditionalFreeText(String additionalFreeText) {
        this.additionalFreeText = additionalFreeText;
    }

    public String getAlternateProduct() {
        return alternateProduct;
    }

    public void setAlternateProduct(String alternateProduct) {
        this.alternateProduct = alternateProduct;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getCatalogNo() {
        return catalogNo;
    }

    public void setCatalogNo(String catalogNo) {
        this.catalogNo = catalogNo;
    }

    public String getDescInForeign() {
        return descInForeign;
    }

    public void setDescInForeign(String descInForeign) {
        this.descInForeign = descInForeign;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isIsKnittingItem() {
        return isKnittingItem;
    }

    public void setIsKnittingItem(boolean isKnittingItem) {
        this.isKnittingItem = isKnittingItem;
    }

    public ItemReusability getItemReusability() {
        return itemReusability;
    }

    public void setItemReusability(ItemReusability itemReusability) {
        this.itemReusability = itemReusability;
    }
    
    public int getReusabilityCount() {
        return reusabilityCount;
    }

    public void setReusabilityCount(int reusabilityCount) {
        this.reusabilityCount = reusabilityCount;
    }

    public String getLicenseCode() {
        return licenseCode;
    }

    public double getTotalIssueCount() {
        return totalIssueCount;
    }

    public void setTotalIssueCount(double totalIssueCount) {
        this.totalIssueCount = totalIssueCount;
    }

    
    public void setLicenseCode(String licenseCode) {
        this.licenseCode = licenseCode;
    }

    public LicenseType getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(LicenseType licenseType) {
        this.licenseType = licenseType;
    }

    public String getCustomerCategory() {
        return customerCategory;
    }

    public void setCustomerCategory(String customerCategory) {
        this.customerCategory = customerCategory;
    }

//    public double getAbatementRate() {
//        return abatementRate;
//    }
//
//    public void setAbatementRate(double abatementRate) {
//        this.abatementRate = abatementRate;
//    }

    public String getServiceTaxCode() {
        return serviceTaxCode;
    }

    public void setServiceTaxCode(String serviceTaxCode) {
        this.serviceTaxCode = serviceTaxCode;
    }
    public String getItemColor() {
        return itemColor;
    }
    
    public void setItemColor(String itemColor) {
        this.itemColor = itemColor;
    }

//    public double getItemCost() {
//        return itemCost;
//    }
//
//    public void setItemCost(double itemCost) {
//        this.itemCost = itemCost;
//    }

    public String getItemGroup() {
        return itemGroup;
    }

    public void setItemGroup(String itemGroup) {
        this.itemGroup = itemGroup;
    }

    public double getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(double itemHeight) {
        this.itemHeight = itemHeight;
    }

    public double getItemLength() {
        return itemLength;
    }

    public void setItemLength(double itemLength) {
        this.itemLength = itemLength;
    }

    public double getItemPurchaseHeight() {
        return itemPurchaseHeight;
    }

    public void setItemPurchaseHeight(double itemPurchaseHeight) {
        this.itemPurchaseHeight = itemPurchaseHeight;
    }

    public double getItemPurchaseLength() {
        return itemPurchaseLength;
    }

    public void setItemPurchaseLength(double itemPurchaseLength) {
        this.itemPurchaseLength = itemPurchaseLength;
    }

    public double getItemPurchaseVolume() {
        return itemPurchaseVolume;
    }

    public void setItemPurchaseVolume(double itemPurchaseVolume) {
        this.itemPurchaseVolume = itemPurchaseVolume;
    }

    public double getItemPurchaseWidth() {
        return itemPurchaseWidth;
    }

    public void setItemPurchaseWidth(double itemPurchaseWidth) {
        this.itemPurchaseWidth = itemPurchaseWidth;
    }

    public double getItemSalesHeight() {
        return itemSalesHeight;
    }

    public void setItemSalesHeight(double itemSalesHeight) {
        this.itemSalesHeight = itemSalesHeight;
    }

    public double getItemSalesLength() {
        return itemSalesLength;
    }

    public void setItemSalesLength(double itemSalesLength) {
        this.itemSalesLength = itemSalesLength;
    }

    public double getItemSalesVolume() {
        return itemSalesVolume;
    }

    public void setItemSalesVolume(double itemSalesVolume) {
        this.itemSalesVolume = itemSalesVolume;
    }

    public double getItemSalesWidth() {
        return itemSalesWidth;
    }

    public void setItemSalesWidth(double itemSalesWidth) {
        this.itemSalesWidth = itemSalesWidth;
    }

    public double getItemVolume() {
        return itemVolume;
    }

    public void setItemVolume(double itemVolume) {
        this.itemVolume = itemVolume;
    }

    public double getItemWidth() {
        return itemWidth;
    }

    public void setItemWidth(double itemWidth) {
        this.itemWidth = itemWidth;
    }

    public UnitOfMeasure getOrderingUOM() {
        return orderingUOM;
    }

    public void setOrderingUOM(UnitOfMeasure orderingUOM) {
        this.orderingUOM = orderingUOM;
    }

    public Packaging getPackaging() {
        return packaging;
    }

    public void setPackaging(Packaging packaging) {
        this.packaging = packaging;
    }

    public String getPriceList() {
        return priceList;
    }

    public void setPriceList(String priceList) {
        this.priceList = priceList;
    }

    public String getPurchaseMfg() {
        return purchaseMfg;
    }

    public void setPurchaseMfg(String purchaseMfg) {
        this.purchaseMfg = purchaseMfg;
    }

    public UnitOfMeasure getPurchaseUOM() {
        return purchaseUOM;
    }

    public void setPurchaseUOM(UnitOfMeasure purchaseUOM) {
        this.purchaseUOM = purchaseUOM;
    }

    public UnitOfMeasure getSalesUOM() {
        return salesUOM;
    }

    public void setSalesUOM(UnitOfMeasure salesUOM) {
        this.salesUOM = salesUOM;
    }

    public String getShippingType() {
        return shippingType;
    }

    public void setShippingType(String shippingType) {
        this.shippingType = shippingType;
    }

    public UnitOfMeasure getTransferUOM() {
        return transferUOM;
    }

    public void setTransferUOM(UnitOfMeasure transferUOM) {
        this.transferUOM = transferUOM;
    }

    public ValuationMethod getValuationMethod() {
        return valuationMethod;
    }

    public void setValuationMethod(ValuationMethod valuationMethod) {
        this.valuationMethod = valuationMethod;
    }

    public boolean isCountable() {
        return countable;
    }

    public void setCountable(boolean countable) {
        this.countable = countable;
    }

    public Set<Frequency> getCycleCountFrequencies() {
        return cycleCountFrequencies;
    }

    public void setCycleCountFrequencies(Set<Frequency> cycleCountFrequencies) {
        this.cycleCountFrequencies = cycleCountFrequencies;
    }

//  // end new properties
//    
    
    public boolean isActivateProductComposition() {
        return activateProductComposition;
    }

    public void setActivateProductComposition(boolean activateProductComposition) {
        this.activateProductComposition = activateProductComposition;
    }
    
    public Set<ProductComposition> getRows() {
        return rows;
    }

    public void setRows(Set<ProductComposition> rows) {
        this.rows = rows;
    }
    
    public double getProductweight() {
        return productweight;
    }

    public void setProductweight(double productweight) {
        this.productweight = productweight;
    }

    public double getProductWeightIncludingPakagingPerStockUom() {
        return productWeightIncludingPakagingPerStockUom;
    }

    public void setProductWeightIncludingPakagingPerStockUom(double productWeightIncludingPakagingPerStockUom) {
        this.productWeightIncludingPakagingPerStockUom = productWeightIncludingPakagingPerStockUom;
    }

    public double getProductWeightPerStockUom() {
        return productWeightPerStockUom;
    }

    public void setProductWeightPerStockUom(double productWeightPerStockUom) {
        this.productWeightPerStockUom = productWeightPerStockUom;
    }
    
    public int getIsImport() {
        return isImport;
    }

    public void setIsImport(int isImport) {
        this.isImport = isImport;
    }

    public SequenceFormat getSeqformat() {
        return seqformat;
    }

    public void setSeqformat(SequenceFormat seqformat) {
        this.seqformat = seqformat;
    }

    public int getSeqnumber() {
        return seqnumber;
    }

    public void setSeqnumber(int seqnumber) {
        this.seqnumber = seqnumber;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public Producttype getProducttype() {
        return producttype;
    }

    public void setProducttype(Producttype producttype) {
        this.producttype = producttype;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getLeadTimeInDays() {
        return leadTimeInDays;
    }

    public void setLeadTimeInDays(int leadTimeInDays) {
        this.leadTimeInDays = leadTimeInDays;
    }

    public int getQALeadTimeInDays() {
        return QALeadTimeInDays;
    }

    public void setQALeadTimeInDays(int QALeadTimeInDays) {
        this.QALeadTimeInDays = QALeadTimeInDays;
    }

    public int getWarrantyperiod() {
        return warrantyperiod;
    }

    public void setWarrantyperiod(int warrantyperiod) {
        this.warrantyperiod = warrantyperiod;
    }

    public int getWarrantyperiodsal() {
        return warrantyperiodsal;
    }

    public void setWarrantyperiodsal(int warrantyperiodsal) {
        this.warrantyperiodsal = warrantyperiodsal;
    }

    public double getMaxOrderingQuantity() {
        return maxOrderingQuantity;
    }

    public void setMaxOrderingQuantity(double maxOrderingQuantity) {
        this.maxOrderingQuantity = maxOrderingQuantity;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(double availableQuantity) {
        this.availableQuantity = availableQuantity;
    }
    
    
    public double getMinOrderingQuantity() {
        return minOrderingQuantity;
    }

    public void setMinOrderingQuantity(double minOrderingQuantity) {
        this.minOrderingQuantity = minOrderingQuantity;
    }

    public String getName() {
        return name;
    }

    public String getProductName() {
        return getName();
    }
    
    public InventoryLocation getLocation() {
        return location;
    }

    public void setLocation(InventoryLocation location) {
        this.location = location;
    }

    public InventoryWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(InventoryWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(double reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public double getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(double reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }

    public UnitOfMeasure getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(UnitOfMeasure unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Account getPurchaseAccount() {
        return purchaseAccount;
    }

    public void setPurchaseAccount(Account purchaseAccount) {
        this.purchaseAccount = purchaseAccount;
    }

    public Account getSalesAccount() {
        return salesAccount;
    }

    public void setSalesAccount(Account salesAccount) {
        this.salesAccount = salesAccount;
    }

    public Account getPurchaseReturnAccount() {
        return purchaseReturnAccount;
    }

    public void setPurchaseReturnAccount(Account purchaseReturnAccount) {
        this.purchaseReturnAccount = purchaseReturnAccount;
    }

    public Account getSalesReturnAccount() {
        return salesReturnAccount;
    }

    public void setSalesReturnAccount(Account salesReturnAccount) {
        this.salesReturnAccount = salesReturnAccount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Product getParent() {
        return parent;
    }

    public void setParent(Product parent) {
        this.parent = parent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isMultiuom() {
        return multiuom;
    }

    public void setMultiuom(boolean multiuom) {
        this.multiuom = multiuom;
    }

    public boolean isblockLooseSell() {
        return blockLooseSell;
    }

    public void setblockLooseSell(boolean blockLooseSell) {
        this.blockLooseSell = blockLooseSell;
    }

    public boolean isSyncable() {
        return syncable;
    }

    public void setSyncable(boolean syncable) {
        this.syncable = syncable;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Set<Product> getChildren() {
        return children;
    }

    public void setChildren(Set<Product> children) {
        this.children = children;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Product other = (Product) obj;
        if ((this.ID == null) ? (other.ID != null) : !this.ID.equals(other.ID)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.ID != null ? this.ID.hashCode() : 0);
        return hash;
    }

    public int compareTo(Object o) { // Comparison should be based on product ID if product name is same
        if (this.name.compareTo(((Product) o).getName()) == 0) {
            return this.ID.compareTo(((Product) o).getID());
        } else {
            return this.name.compareTo(((Product) o).getName());
        }
    }

    public int getFromInventory() {
        return fromInventory;
    }

    public void setFromInventory(int fromInventory) {
        this.fromInventory = fromInventory;
    }

    public AccProductCustomData getProductCustomData() {
        return productCustomData;
    }

    public void setProductCustomData(AccProductCustomData productCustomData) {
        this.productCustomData = productCustomData;
    }

    public String getCoilcraft() {
        return coilcraft;
    }

    public void setCoilcraft(String coilcraft) {
        this.coilcraft = coilcraft;
    }

    public String getInterplant() {
        return interplant;
    }

    public void setInterplant(String interplant) {
        this.interplant = interplant;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }
    
    public ShelfLocation getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(ShelfLocation shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public boolean isDepreciable() {
        return depreciable;
    }

    public void setDepreciable(boolean depreciable) {
        this.depreciable = depreciable;
    }

    public int getDepreciationCostLimit() {
        return depreciationCostLimit;
    }

    public void setDepreciationCostLimit(int depreciationCostLimit) {
        this.depreciationCostLimit = depreciationCostLimit;
    }

    public Account getDepreciationGLAccount() {
        return depreciationGLAccount;
    }

    public void setDepreciationGLAccount(Account depreciationGLAccount) {
        this.depreciationGLAccount = depreciationGLAccount;
    }

    public int getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(int depreciationMethod) {
        this.depreciationMethod = depreciationMethod;
    }

    public Account getDepreciationProvisionGLAccount() {
        return depreciationProvisionGLAccount;
    }

    public void setDepreciationProvisionGLAccount(Account depreciationProvisionGLAccount) {
        this.depreciationProvisionGLAccount = depreciationProvisionGLAccount;
    }

    public double getDepreciationRate() {
        return depreciationRate;
    }

    public void setDepreciationRate(double depreciationRate) {
        this.depreciationRate = depreciationRate;
    }

    public Account getSellAssetGLAccount() {
        return sellAssetGLAccount;
    }

    public void setSellAssetGLAccount(Account sellAssetGLAccount) {
        this.sellAssetGLAccount = sellAssetGLAccount;
    }

    public boolean isAsset() {
        return asset;
    }

    public void setAsset(boolean asset) {
        this.asset = asset;
    }

    public boolean isQaenable() {
        return qaenable;
    }

    public void setQaenable(boolean qaenable) {
        this.qaenable = qaenable;
    }

    public ProductBatch getBatch() {
        return batch;
    }

    public void setBatch(ProductBatch batch) {
        this.batch = batch;
    }

    public Account getSalesRevenueRecognitionAccount() {
        return salesRevenueRecognitionAccount;
    }

    public void setSalesRevenueRecognitionAccount(Account salesRevenueRecognitionAccount) {
        this.salesRevenueRecognitionAccount = salesRevenueRecognitionAccount;
    }

    public PriceType getDependenttype() {
        return dependenttype;
    }

    public void setDependenttype(PriceType dependenttype) {
        this.dependenttype = dependenttype;
    }

    public boolean isRevenueRecognitionProcess() {
        return revenueRecognitionProcess;
    }

    public void setRevenueRecognitionProcess(boolean revenueRecognitionProcess) {
        this.revenueRecognitionProcess = revenueRecognitionProcess;
    }

    public boolean isIntervalfield() {
        return intervalfield;
    }

    public void setIntervalfield(boolean intervalfield) {
        this.intervalfield = intervalfield;
    }

    public int getTimeinterval() {
        return timeinterval;
    }

    public void setTimeinterval(int timeinterval) {
        this.timeinterval = timeinterval;
    }

    public String getNoofqtyvalue() {
        return noofqtyvalue;
    }

    public void setNoofqtyvalue(String noofqtyvalue) {
        this.noofqtyvalue = noofqtyvalue;
    }

    public int getNoofquqntity() {
        return noofquqntity;
    }

    public void setNoofquqntity(int noofquqntity) {
        this.noofquqntity = noofquqntity;
    }

    public boolean isAddshiplentheithqty() {
        return addshiplentheithqty;
    }

    public void setAddshiplentheithqty(boolean addshiplentheithqty) {
        this.addshiplentheithqty = addshiplentheithqty;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public Date getAsOfDate() {
        return asOfDate;
    }

    public void setAsOfDate(Date asOfDate) {
        this.asOfDate = asOfDate;
    }

    public boolean isIsBatchForProduct() {
        return isBatchForProduct;
    }

    public void setIsBatchForProduct(boolean isBatchForProduct) {
        this.isBatchForProduct = isBatchForProduct;
    }

    public boolean isIsSerialForProduct() {
        return isSerialForProduct;
    }

    public void setIsSerialForProduct(boolean isSerialForProduct) {
        this.isSerialForProduct = isSerialForProduct;
    }

    public boolean isIsSKUForProduct() {
        return isSKUForProduct;
    }

    public void setIsSKUForProduct(boolean isSKUForProduct) {
        this.isSKUForProduct = isSKUForProduct;
    }
    
    public boolean isRecyclable() {
        return recyclable;
    }

    public void setRecyclable(boolean recyclable) {
        this.recyclable = recyclable;
    }

    public double getRecycleQuantity() {
        return recycleQuantity;
    }

    public void setRecycleQuantity(double recycleQuantity) {
        this.recycleQuantity = recycleQuantity;
    }

    public boolean isAutoAssembly() {
        return autoAssembly;
    }

    public void setAutoAssembly(boolean autoAssembly) {
        this.autoAssembly = autoAssembly;
    }

    public boolean isIslocationforproduct() {
        return islocationforproduct;
    }

    public void setIslocationforproduct(boolean islocationforproduct) {
        this.islocationforproduct = islocationforproduct;
    }

    public boolean isIswarehouseforproduct() {
        return iswarehouseforproduct;
    }

    public void setIswarehouseforproduct(boolean iswarehouseforproduct) {
        this.iswarehouseforproduct = iswarehouseforproduct;
    }

    public int getBarcodefield() {
        return barcodefield;
    }

    public void setBarcodefield(int barcodefield) {
        this.barcodefield = barcodefield;
    }

    public boolean isIsbinforproduct() {
        return isbinforproduct;
    }

    public void setIsbinforproduct(boolean isbinforproduct) {
        this.isbinforproduct = isbinforproduct;
    }

    public boolean isIsrackforproduct() {
        return israckforproduct;
    }

    public void setIsrackforproduct(boolean israckforproduct) {
        this.israckforproduct = israckforproduct;
    }

    public boolean isIsrowforproduct() {
        return isrowforproduct;
    }

    public void setIsrowforproduct(boolean isrowforproduct) {
        this.isrowforproduct = isrowforproduct;
    }
    
     public UOMschemaType getUomSchemaType() {
        return uomSchemaType;
    }

    public void setUomSchemaType(UOMschemaType uomSchemaType) {
        this.uomSchemaType = uomSchemaType;
    }

    public String getHSCode() {
        return HSCode;
    }

    public void setHSCode(String HSCode) {
        this.HSCode = HSCode;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public boolean isWastageApplicable() {
        return wastageApplicable;
    }

    public void setWastageApplicable(boolean wastageApplicable) {
        this.wastageApplicable = wastageApplicable;
    }

    public Account getWastageAccount() {
        return wastageAccount;
    }

    public void setWastageAccount(Account wastageAccount) {
        this.wastageAccount = wastageAccount;
    }

    public Product getSubstituteProduct() {
        return substituteProduct;
    }

    public void setSubstituteProduct(Product substituteProduct) {
        this.substituteProduct = substituteProduct;
    }

    public double getSubstituteQty() {
        return substituteQty;
    }

    public void setSubstituteQty(double substituteQty) {
        this.substituteQty = substituteQty;
    }

    public String getExcisemethodmain() {
        return excisemethodmain;
    }

    public void setExcisemethodmain(String excisemethodmain) {
        this.excisemethodmain = excisemethodmain;
    }

    public String getExcisemethodsubtype() {
        return excisemethodsubtype;
    }

    public void setExcisemethodsubtype(String excisemethodsubtype) {
        this.excisemethodsubtype = excisemethodsubtype;
    }

    public double getExciserate() {
        return exciserate;
    }

    public void setExciserate(double exciserate) {
        this.exciserate = exciserate;
    }
     public MasterItem getVatcommoditycode() {
        return vatcommoditycode;
    }

    public void setVatcommoditycode(MasterItem vatcommoditycode) {
        this.vatcommoditycode = vatcommoditycode;
    }

    public double getMrprate() {
        return mrprate;
    }

    public void setMrprate(double mrprate) {
        this.mrprate = mrprate;
    }
    public String getSAC() {
        return SAC;
    }

    public void setSAC(String SAC) {
        this.SAC = SAC;
    }
    public boolean isVatonmrp() {
        return vatonmrp;
    }

    public void setVatonmrp(boolean vatonmrp) {
        this.vatonmrp = vatonmrp;
    }

    public double getVatAbatementRate() {
        return vatAbatementRate;
    }

    public void setVatAbatementRate(double vatAbatementRate) {
        this.vatAbatementRate = vatAbatementRate;
    }

    public Date getVatAbatementPeriodFromDate() {
        return vatAbatementPeriodFromDate;
    }

    public void setVatAbatementPeriodFromDate(Date vatAbatementPeriodFromDate) {
        this.vatAbatementPeriodFromDate = vatAbatementPeriodFromDate;
    }
    
    public Date getVatAbatementPeriodToDate() {
        return vatAbatementPeriodToDate;
    }

    public void setVatAbatementPeriodToDate(Date vatAbatementPeriodToDate) {
        this.vatAbatementPeriodToDate = vatAbatementPeriodToDate;
    }

    public Account getInterStatePurchaseAccount() {
        return interStatePurchaseAccount;
    }

    public void setInterStatePurchaseAccount(Account interStatePurchaseAccount) {
        this.interStatePurchaseAccount = interStatePurchaseAccount;
    }

    public Account getInterStatePurchaseAccountCForm() {
        return interStatePurchaseAccountCForm;
    }

    public void setInterStatePurchaseAccountCForm(Account interStatePurchaseAccountCForm) {
        this.interStatePurchaseAccountCForm = interStatePurchaseAccountCForm;
    }

    public Account getInterStatePurchaseReturnAccount() {
        return interStatePurchaseReturnAccount;
    }

    public void setInterStatePurchaseReturnAccount(Account interStatePurchaseReturnAccount) {
        this.interStatePurchaseReturnAccount = interStatePurchaseReturnAccount;
    }

    public Account getInterStatePurchaseReturnAccountCForm() {
        return interStatePurchaseReturnAccountCForm;
    }

    public void setInterStatePurchaseReturnAccountCForm(Account interStatePurchaseReturnAccountCForm) {
        this.interStatePurchaseReturnAccountCForm = interStatePurchaseReturnAccountCForm;
    }

    public Account getInterStateSalesAccount() {
        return interStateSalesAccount;
    }

    public void setInterStateSalesAccount(Account interStateSalesAccount) {
        this.interStateSalesAccount = interStateSalesAccount;
    }

    public Account getInterStateSalesAccountCForm() {
        return interStateSalesAccountCForm;
    }

    public void setInterStateSalesAccountCForm(Account interStateSalesAccountCForm) {
        this.interStateSalesAccountCForm = interStateSalesAccountCForm;
    }

    public Account getInterStateSalesReturnAccount() {
        return interStateSalesReturnAccount;
    }

    public void setInterStateSalesReturnAccount(Account interStateSalesReturnAccount) {
        this.interStateSalesReturnAccount = interStateSalesReturnAccount;
    }

    public Account getInterStateSalesReturnAccountCForm() {
        return interStateSalesReturnAccountCForm;
    }

    public void setInterStateSalesReturnAccountCForm(Account interStateSalesReturnAccountCForm) {
        this.interStateSalesReturnAccountCForm = interStateSalesReturnAccountCForm;
    }

    public Account getCstVAT() {
        return cstVAT;
    }

    public void setCstVAT(Account cstVAT) {
        this.cstVAT = cstVAT;
    }

    public Account getCstVATSales() {
        return cstVATSales;
    }

    public void setCstVATSales(Account cstVATSales) {
        this.cstVATSales = cstVATSales;
    }

    public Account getCstVATattwo() {
        return cstVATattwo;
    }

    public void setCstVATattwo(Account cstVATattwo) {
        this.cstVATattwo = cstVATattwo;
    }

    public Account getCstVATattwoSales() {
        return cstVATattwoSales;
    }

    public void setCstVATattwoSales(Account cstVATattwoSales) {
        this.cstVATattwoSales = cstVATattwoSales;
    }

    public Account getInputVAT() {
        return inputVAT;
    }

    public void setInputVAT(Account inputVAT) {
        this.inputVAT = inputVAT;
    }

    public Account getInputVATSales() {
        return inputVATSales;
    }

    public void setInputVATSales(Account inputVATSales) {
        this.inputVATSales = inputVATSales;
    }

    public Account getInventoryAccount() {
        return inventoryAccount;
    }
    
    public void setInventoryAccount(Account inventoryAccount) {
        this.inventoryAccount = inventoryAccount;
    }

    public Account getCostOfGoodsSoldAccount() {
        return costOfGoodsSoldAccount;
    }

    public void setCostOfGoodsSoldAccount(Account costOfGoodsSoldAccount) {
        this.costOfGoodsSoldAccount = costOfGoodsSoldAccount;
    }

    public Account getStockAdjustmentAccount() {
        return stockAdjustmentAccount;
    }

    public void setStockAdjustmentAccount(Account stockAdjustmentAccount) {
        this.stockAdjustmentAccount = stockAdjustmentAccount;
    }

    public UOMschemaType getReportingSchemaType() {
        return reportingSchemaType;
    }

    public void setReportingSchemaType(UOMschemaType reportingSchemaType) {
        this.reportingSchemaType = reportingSchemaType;
    }
    

    public double getOpeningBalanceAmount() {
        return openingBalanceAmount;
    }

    public void setOpeningBalanceAmount(double openingBalanceAmount) {
        this.openingBalanceAmount = openingBalanceAmount;
    }

    public double getOpeningBalanceAmountInBase() {
        return openingBalanceAmountInBase;
    }

    public void setOpeningBalanceAmountInBase(double openingBalanceAmountInBase) {
        this.openingBalanceAmountInBase = openingBalanceAmountInBase;
    }
    public MasterItem getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(MasterItem productBrand) {
        this.productBrand = productBrand;
    }

    public double getExchangerateforopeningbalanceamount() {
        return exchangerateforopeningbalanceamount;
    }

    public void setExchangerateforopeningbalanceamount(double exchangerateforopeningbalanceamount) {
        this.exchangerateforopeningbalanceamount = exchangerateforopeningbalanceamount;
    }

    public UOMschemaType getReportingSchemaTypeVAT() {
        return reportingSchemaTypeVAT;
    }

    public void setReportingSchemaTypeVAT(UOMschemaType reportingSchemaTypeVAT) {
        this.reportingSchemaTypeVAT = reportingSchemaTypeVAT;
    }

    public String getReportinguomVAT() {
        return reportinguomVAT;
    }

    public void setReportinguomVAT(String reportinguomVAT) {
        this.reportinguomVAT = reportinguomVAT;
    }

    public String getVatMethodType() {
        return vatMethodType;
    }

    public void setVatMethodType(String vatMethodType) {
        this.vatMethodType = vatMethodType;
    }

    public double getProductVolumePerStockUom() {
        return productVolumePerStockUom;
    }

    public void setProductVolumePerStockUom(double productVolumePerStockUom) {
        this.productVolumePerStockUom = productVolumePerStockUom;
    }

    public double getProductVolumeIncludingPakagingPerStockUom() {
        return productVolumeIncludingPakagingPerStockUom;
    }

    public void setProductVolumeIncludingPakagingPerStockUom(double productVolumeIncludingPakagingPerStockUom) {
        this.productVolumeIncludingPakagingPerStockUom = productVolumeIncludingPakagingPerStockUom;
    }
    
    public String getDateAfterPreffixValue() {
        return dateAfterPreffixValue;
    }

    public void setDateAfterPreffixValue(String dateAfterPreffixValue) {
        this.dateAfterPreffixValue = dateAfterPreffixValue;
    }

    /**
     * @return the industryCodeId
     */
    public String getIndustryCodeId() {
        return industryCodeId;
    }

    /**
     * @param industryCodeId the industryCodeId to set
     */
    public void setIndustryCodeId(String industryCodeId) {
        this.industryCodeId = industryCodeId;
    }

    public boolean isBlockLooseSell() {
        return blockLooseSell;
    }

    public void setBlockLooseSell(boolean blockLooseSell) {
        this.blockLooseSell = blockLooseSell;
    }

    public Set<LandingCostCategory> getLccategoryid() {
        return lccategoryid;
    }

    public void setLccategoryid(Set<LandingCostCategory> lccategoryid) {
        this.lccategoryid = lccategoryid;
    }

    public boolean isRcmApplicable() {
        return rcmApplicable;
    }

    public void setRcmApplicable(boolean rcmApplicable) {
        this.rcmApplicable = rcmApplicable;
    }

    public Account getWriteOffAssetAccount() {
        return writeOffAssetAccount;
    }

    public void setWriteOffAssetAccount(Account writeOffAssetAccount) {
        this.writeOffAssetAccount = writeOffAssetAccount;
    }

    public Account getItcAccount() {
        return itcAccount;
    }

    public void setItcAccount(Account itcAccount) {
        this.itcAccount = itcAccount;
    }

    public int getItcType() {
        return itcType;
    }

    public void setItcType(int itcType) {
        this.itcType = itcType;
    }
    
}
