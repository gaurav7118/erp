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

import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class ProjectFeature {

    private String featureID;
    private String featureName;
    private String displayFeatureName;
    private Set<ProjectActivity> activities;
    private Set<UserPermission> permissions;
    private int orderNo;
    public static String campaignFName = "Campaign";
    public static String leadFName = "Lead";
    public static String accountFName = "Account";
    public static String contactFName = "Contact";
    public static String opportunityFName = "Opportunity";
    public static String caseFName = "Case";
    public static String activityFName = "Activity";
    public static String productFName = "Product";
    public static String targetFName = "TargetModule";
    //featurelist index to get permission code
    public static final String Account_Management_and_Journal_Entry="coa";
    public static final String Purchase_Management="vendorinvoice";
    public static final String Sales_Management = "invoice";
    public static final String Customer = "customer";
    public static final String Vendor = "vendor";
    public static final String Product = "product";
    public static final String Financial_Statements  ="fstatement";
    public static final String Goods_Receipt_Reports="goodsreceiptreport";
    public static final String Delivery_Order_Reports="deliveryreport";
    public static final String Sales_Return_Reports="salesreturn";
    public static final String Purchase_Return_Reports="purchasereturn";
    public static final String Receive_Payment="purchasemakepayment";
    public static final String Make_Payment ="salesreceivepayment";
    public static final String Credit_Note ="creditnote";
    public static final String Debit_Note ="debitnote";
    public static final String Bank_Reconciliation="bankreconciliation";
    public static final String Quantative_Analysis ="qanalysis";
    public static final String Account_Preferences="accpref"; 
    public static final String Master_Configuration="masterconfig";
    public static final String Audit_Trail="audittrail";
    public static final String User_Administration="useradmin";
    public static final String Import_Log="importlog";
    public static final String Purchase_Requision ="vendorpr";
    public static final String Delivery_Planner="dplanner";
    public static final String Aged_Payable="agedpayable";
    public static final String Aged_Receivable="agedreceivable";
    
    public String getDisplayFeatureName() {
        return displayFeatureName;
    }

    public void setDisplayFeatureName(String displayFeatureName) {
        this.displayFeatureName = displayFeatureName;
    }

    public String getFeatureID() {
        return featureID;
    }

    public void setFeatureID(String featureID) {
        this.featureID = featureID;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public Set<ProjectActivity> getActivities() {
        return activities;
    }

    public void setActivities(Set<ProjectActivity> activities) {
        this.activities = activities;
    }

    public Set<UserPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<UserPermission> permissions) {
        this.permissions = permissions;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(int orderNo) {
        this.orderNo = orderNo;
    }
}
