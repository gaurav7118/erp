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
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.Projreport_Template;
import com.krawler.common.admin.User;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class BillingSalesOrder {

    private String ID;
    private String salesOrderNumber;
    private boolean autoGenerated;
    private Date dueDate;
    private Date orderDate;
    private Customer customer;
    private String memo;
    private String billTo;
    private String shipTo;
    private Set<BillingSalesOrderDetail> rows;
    private Company company;
    private boolean deleted;
    private CostCenter costcenter;
    private Tax tax;
    private KWLCurrency currency;
    private Date shipdate;
    private String shipvia;
    private String fob;
    private int pendingapproval;
    private boolean perDiscount;
    private double discount;
    private Projreport_Template templateid;
    private boolean favourite;
    private int approvallevel;
    private User approver;
    private int istemplate;
    private String postText;
    private Term term;
    private MasterItem salesperson;
    private int seqnumber;//Only to store integer part of sequence format
    private SequenceFormat seqformat;

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

    public MasterItem getSalesperson() {
        return salesperson;
    }

    public void setSalesperson(MasterItem salesperson) {
        this.salesperson = salesperson;
    }

    public int getIstemplate() {
        return istemplate;
    }

    public void setIstemplate(int istemplate) {
        this.istemplate = istemplate;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    public int getApprovallevel() {
        return approvallevel;
    }

    public void setApprovallevel(int approvallevel) {
        this.approvallevel = approvallevel;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<BillingSalesOrderDetail> getRows() {
        return rows;
    }

    public void setRows(Set<BillingSalesOrderDetail> rows) {
        this.rows = rows;
    }

    public String getSalesOrderNumber() {
        return salesOrderNumber;
    }

    public void setSalesOrderNumber(String salesOrderNumber) {
        this.salesOrderNumber = salesOrderNumber;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public CostCenter getCostcenter() {
        return costcenter;
    }

    public void setCostcenter(CostCenter costcenter) {
        this.costcenter = costcenter;
    }

    public int getPendingapproval() {
        return pendingapproval;
    }

    public void setPendingapproval(int pendingapproval) {
        this.pendingapproval = pendingapproval;
    }

    public String getFob() {
        return fob;
    }

    public void setFob(String fob) {
        this.fob = fob;
    }

    public String getShipvia() {
        return shipvia;
    }

    public void setShipvia(String shipvia) {
        this.shipvia = shipvia;
    }

    public Date getShipdate() {
        return shipdate;
    }

    public void setShipdate(Date shipdate) {
        this.shipdate = shipdate;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public Projreport_Template getTemplateid() {
        return templateid;
    }

    public void setTemplateid(Projreport_Template templateid) {
        this.templateid = templateid;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public String getBillTo() {
        return billTo;
    }

    public void setBillTo(String billTo) {
        this.billTo = billTo;
    }

    public String getShipTo() {
        return shipTo;
    }

    public void setShipTo(String shipTo) {
        this.shipTo = shipTo;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean isPerDiscount() {
        return perDiscount;
    }

    public void setPerDiscount(boolean perDiscount) {
        this.perDiscount = perDiscount;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }
}