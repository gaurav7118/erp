/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.consignment;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.store.Store;
import java.util.Date;
import java.util.Set;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.authHandler.authHandler;
import static com.krawler.spring.authHandler.authHandler.getGlobalDateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class Consignment {

    private String id;
    private String transactionNo;
    private Store store;
    private Product product;
    private UnitOfMeasure uom;
    private double returnQuantity;
    private ApprovalStatus approvalStatus;
    private Date fromDate;
    private Date toDate;
    private Date createdOn;
    private Company company;
    private Customer customer;
    private String documentid;
    private String moduleRefId;
    private Set<ConsignmentApprovalDetails> consignmentApprovalDetails;
    private double  unitPrice;
    private CostCenter costcenter;

    public Consignment()  {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            this.createdOn = sdf.parse(sdf.format(new Date()));
        } catch (ParseException ex) {
            Logger.getLogger(Consignment.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(double returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
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

    public Set<ConsignmentApprovalDetails> getConsignmentApprovalDetails() {
        return consignmentApprovalDetails;
    }

    public void setConsignmentApprovalDetails(Set<ConsignmentApprovalDetails> consignmentApprovalDetails) {
        this.consignmentApprovalDetails = consignmentApprovalDetails;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getDocumentid() {
        return documentid;
    }

    public void setDocumentid(String documentid) {
        this.documentid = documentid;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getModuleRefId() {
        return moduleRefId;
    }

    public void setModuleRefId(String moduleRefId) {
        this.moduleRefId = moduleRefId;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public CostCenter getCostcenter() {
        return costcenter;
    }

    public void setCostcenter(CostCenter costcenter) {
        this.costcenter = costcenter;
    }
}
