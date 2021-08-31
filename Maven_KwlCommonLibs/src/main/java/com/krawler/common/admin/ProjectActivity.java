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
package com.krawler.common.admin;

/**
 *
 * @author krawler-user
 */
public class ProjectActivity {

    private String activityID;
    private String activityName;
    private String displayActivityName;
    private ProjectFeature feature;
    private ProjectActivity parent;
    private int orderNo;
//Constants added for hide/show icons for dashboardWidget for activitylist permissioncode
    // Account Manager
    public static final long  Create_Journal_Entry = 128;
    public static final long  View_Coa=2;
    public static final long  View_Reconciliation=1;
    // Master
    public static final long  View_Product=2;    
    public static final long  View_Customer=2; 
    public static final long  View_Vendor=2; 
    public static final long  View_Customdesign=128;
    public static final long  View_Masterconfig=1;
    // Administration
    public static final long  View_User_Administration=1;
    public static final long  View_Audit_Trail=1;
    public static final long  View_Import_Log=1;
    public static final long  View_CustomLayout=64;
    public static final long  View_AccountPref=1;
    //Purchase Management
    public static final long  View_Journal_Entry=256;
    public static final long  View_PurchaseInvoice_CashPurchase=524288;
    public static final long  View_Debit_Note=4;
    public static final long  View_Payment_Made=4;
    public static final long  View_Purchase_Order=512;
    public static final long  View_Goods_Receipts=2;
    public static final long  View_Purchase_Return=2;
    public static final long  View_Vendor_Quotation=2;
    public static final long  View_Purchase_Requisition=2;
    public static final long  View_Delivery_Planner=1;
    //Sales Management
    public static final long  View_SalesInvoice_CashSales=2097152;
    public static final long  View_Credit_Note=4;
    public static final long  View_Payment_Received=4;
    public static final long  View_Sales_Order=1024;
    public static final long  View_Delivery_order=2;
    public static final long  View_Sales_Return=2;
    public static final long  View_Customer_Quotation=2;
    //Financial Reports
    public static final long View_Trial_Balance=1;
    public static final long View_Ledger=8;
    public static final long View_Trading_profitLoss=64;
    public static final long View_Balance_Sheet=512;
    public static final long View_Cash_Book=4096;
    public static final long View_Bank_Book=32768;
    public static final long View_Aged_Payable= 1;
    public static final long View_Aged_Receivable= 1;
    public static final long View_Quantitative_Analysis= 1;
    
    public String getActivityID() {
        return activityID;
    }

    public void setActivityID(String activityID) {
        this.activityID = activityID;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getDisplayActivityName() {
        return displayActivityName;
    }

    public void setDisplayActivityName(String displayActivityName) {
        this.displayActivityName = displayActivityName;
    }

    public ProjectFeature getFeature() {
        return feature;
    }

    public void setFeature(ProjectFeature feature) {
        this.feature = feature;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }

    public ProjectActivity getParent() {
        return parent;
    }

    public void setParent(ProjectActivity parent) {
        this.parent = parent;
    }
}
