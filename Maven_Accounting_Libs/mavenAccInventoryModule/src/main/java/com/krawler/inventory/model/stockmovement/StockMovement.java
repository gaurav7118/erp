/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockmovement;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.hql.accounting.Vendor;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.authHandler.authHandler;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class StockMovement {

    private String id;
    private Product product;
    private Store store;
    private UnitOfMeasure stockUoM;
    private double quantity;
    private double pricePerUnit;
    private String remark;
    private String transactionNo;
    private Date transactionDate;
    private TransactionType transactionType;
    private TransactionModule transactionModule;
    private String moduleRefId;
    private String moduleRefDetailId;
    private Company company;
    private Vendor vendor;
    private Customer customer;
    private CostCenter costCenter;
    private Set<StockMovementDetail> stockMovementDetails;
    private Date createdOn ;
    private Product assembledProduct ;
    private long autoSeq;                    // maintain sequence of transactions in case of same date 
    private String memo; 
    private int stock_management_flag;

    public StockMovement() {
        this.createdOn = new Date();
        this.stockMovementDetails = new HashSet<>();
    }

    public StockMovement(Product product, Store store,  double quantity, double pricePerUnit, String transactionNo, Date transactionDate, TransactionType transactionType, TransactionModule transactionModule, String moduleRefId) {
        this(product,store,quantity,pricePerUnit,transactionNo,transactionDate,transactionType,transactionModule,moduleRefId,null);
    }
   
    public StockMovement(Product product, Store store,  double quantity, double pricePerUnit, String transactionNo, Date transactionDate, TransactionType transactionType, TransactionModule transactionModule, String moduleRefId ,String moduleRefDetailId) {
        this();
        this.company = product.getCompany();
        this.product = product;
        this.store = store;
        this.quantity = Math.abs(quantity);
        this.pricePerUnit = Math.abs(pricePerUnit);
        this.transactionNo = transactionNo;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.transactionModule = transactionModule;
        this.moduleRefId = moduleRefId;
        this.moduleRefDetailId=moduleRefDetailId;
    }

    public Product getAssembledProduct() {
        return assembledProduct;
    }

    public void setAssembledProduct(Product assembledProduct) {
        this.assembledProduct = assembledProduct;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Set<StockMovementDetail> getStockMovementDetails() {
        return stockMovementDetails;
    }

    public void setStockMovementDetails(Set<StockMovementDetail> stockMovementDetails) {
        this.stockMovementDetails = stockMovementDetails;
    }


    public CostCenter getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(CostCenter costCenter) {
        this.costCenter = costCenter;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public UnitOfMeasure getStockUoM() {
        return stockUoM;
    }

    public void setStockUoM(UnitOfMeasure stockUoM) {
        this.stockUoM = stockUoM;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = Math.abs(pricePerUnit);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleRefId() {
        return moduleRefId;
    }

    public void setModuleRefId(String moduleRefId) {
        this.moduleRefId = moduleRefId;
    }

    public String getModuleRefDetailId() {
        return moduleRefDetailId;
    }

    public void setModuleRefDetailId(String moduleRefDetailId) {
        this.moduleRefDetailId = moduleRefDetailId;
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
        this.quantity = Math.abs(quantity);
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionModule getTransactionModule() {
        return transactionModule;
    }

    public void setTransactionModule(TransactionModule transactionModule) {
        this.transactionModule = transactionModule;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public long getAutoSeq() {
        return autoSeq;
    }

    public void setAutoSeq(long autoSeq) {
        this.autoSeq = autoSeq;
    }
    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
    
    public int getStock_management_flag() {
        return stock_management_flag;
    }

    public void setStock_management_flag(int stock_management_flag) {
        this.stock_management_flag = stock_management_flag;
    }
}
