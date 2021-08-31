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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class GroupCompanyProcessMapping implements Serializable{

    private String ID;
    private String sourceCompany;
    private String sourceCompanyId;
    private String sourceModule;
    private String destinationCompany;
    private String destinationCompanyId;
    private String destinationModule;

    
    public static final String SOURCE_COMPANY_SUBDOMAIN = "sourcecompanysubdomain";
    public static final String DESTINATION_COMPANY_SUBDOMAIN = "destinationcompanysubdomain";
    public static final String SOURCE_COMPANYID = "sourcecompanyid";
    public static final String DESTINATION_COMPANYID = "destinationcompanyid";
    public static final String LinkModule_Combo = "fromLinkCombo";
    public static final String LinkModule_Combo_DO = "fromLinkComboAutoDO";
    public static final String linkedTransactionBillid = "linkNumber";
    
    // Module MAPPING Related Constants
    public static final String SOURCE_MODULE = "sourcemodule";
    public static final String DESTINATION_MODULE = "destinationmodule";

    //Tax Mapping Related Constants
    public static final String SOURCE_TAX_CODE = "sourceTaxCode";
    public static final String SOURCE_TAX_ID = "sourceTaxid";
    public static final String DESTINATION_TAX_CODE = "destinationTaxCode";
    public static final String DESTINATION_TAX_ID = "destinationTaxid";
    
    //Term Related Mapping
    public static final String SOURCE_TERM_NAME = "sourceTermName";
    public static final String SOURCE_TERM_ID = "sourceTermid";
    public static final String DESTINATION_TERM_NAME = "destinationTermName";
    public static final String DESTINATION_TERM_ID = "destinationTermid";
    
    //Vendor Customer Related  Mapping
    public static final String SOURCE_MASTER_ID = "sourceMasterid";
    public static final String SOURCE_MASTERCODE = "sourceMasterCode";
    public static final String DESTNATION_MASTERCODE = "destinationMasterCode";
    public static final String DESTNATION_MASTER_ID = "destinationMasterid";
    public static final String IS_SOURCE_CUSTOMER = "isSourceCustomer";
    
    //Transaction Related Mapping
    public static final String SOURCE_TRANSACTIONID = "sourceTransactionId";
    public static final String DESTINATION_TRANSACTIONID = "destinationTransactionId";
    public static final String SOURCE_COMPANY_SUBDOMAIN_UPDATE = "sourcecompanysubdomainupdate";

    //METHOD RELATED Constants
    public static final String SAVE_PURCHASE_ORDER = "savePurchaseOrder";
    public static final String SAVE_PURCHASE_RETURN = "savePurchaseReturn";
    public static final String SAVE_DELIVERY_ORDER = "saveDeliveryOrder";
    public static final String SAVE_GOODSRECEIPT_ORDER = "saveGoodsReceiptOrder";
    public static final String SAVE_PURCHASE_INVOICE = "saveGoodsReceipt";
    public static final String SAVE_MAKE_PAYMENT = "savePayment";
    public static final String DELETE_PURCHASEORDER_PERMANENT = "deletePurchaseOrdersPermanent";
    public static final String DELETE_PURCHASEORDER_TEMPORARY = "deletePurchaseOrders";
    public static final String DELETE_DELIVERYORDER = "deleteDeliveryOrdersPermanent";
    public static final String DELETE_PURCHASEINVOICE = "deleteGoodsReceiptPermanent";
    public static final String DELETE_PURCHASERETURN_PERMANENT = "deletePurchaseReturnPermanent";
    public static final String DELETE_PURCHASERETURN_TEMPORARY = "deletePurchaseReturn";
    public static final String DELETE_GOODSRECEIPTORDER_PERMANENT = "deleteGoodsReceiptOrdersPermanent";
    public static final String DELETE_GOODSRECEIPTORDER_TEMPORARY = "deleteGoodsReceiptOrders";
    public static final String DELETE_MAKEPAYMENT_PERMANENT = "deletePaymentForEdit";
    public static final String DELETE_MAKEPAYMENT_TEMPORARY = "deletePaymentMerged";
    public static final String GENERATE_GRN_From_Multiple_DO = "generateGRNFromMultipleDO";
    public static final String GENERATE_PO_From_Multiple_SO = "generatePOFromMultipleSO";
    
    public static Set<String> MethodSetForAccountPayable = new HashSet();

    static {
        MethodSetForAccountPayable.add(SAVE_PURCHASE_ORDER);
        MethodSetForAccountPayable.add(SAVE_PURCHASE_INVOICE);
        MethodSetForAccountPayable.add(SAVE_PURCHASE_RETURN);
        MethodSetForAccountPayable.add(SAVE_MAKE_PAYMENT);
        MethodSetForAccountPayable.add(SAVE_GOODSRECEIPT_ORDER);
        MethodSetForAccountPayable.add(DELETE_PURCHASEORDER_PERMANENT);
        MethodSetForAccountPayable.add(DELETE_PURCHASEORDER_TEMPORARY);
        MethodSetForAccountPayable.add(DELETE_DELIVERYORDER);
        MethodSetForAccountPayable.add(DELETE_PURCHASEINVOICE);
        MethodSetForAccountPayable.add(DELETE_PURCHASERETURN_PERMANENT);
        MethodSetForAccountPayable.add(DELETE_PURCHASERETURN_TEMPORARY);
        MethodSetForAccountPayable.add(DELETE_GOODSRECEIPTORDER_PERMANENT);
        MethodSetForAccountPayable.add(DELETE_GOODSRECEIPTORDER_TEMPORARY);
        MethodSetForAccountPayable.add(DELETE_MAKEPAYMENT_PERMANENT);
        MethodSetForAccountPayable.add(DELETE_MAKEPAYMENT_TEMPORARY);
        MethodSetForAccountPayable.add(GENERATE_GRN_From_Multiple_DO);
        MethodSetForAccountPayable.add(GENERATE_PO_From_Multiple_SO);

    }
    public static Set<String> ControllerNameSet = new HashSet();

    static {
        ControllerNameSet.add("accPurchaseOrderController");
        ControllerNameSet.add("accGoodsReceiptController");
        ControllerNameSet.add("accGoodsReceiptController");
        ControllerNameSet.add("accGoodsReceiptControllerCMN");
        ControllerNameSet.add("accSalesReturnControllerCMN");
        ControllerNameSet.add("accVendorPaymentControllerNew");
        ControllerNameSet.add("accInvoiceControllerCMN");
        ControllerNameSet.add("accSalesOrderControllerCMN");
    }
        
    public String getDestinationCompanyId() {
        return destinationCompanyId;
    }

    public void setDestinationCompanyId(String destinationCompanyId) {
        this.destinationCompanyId = destinationCompanyId;
    }

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDestinationCompany() {
        return destinationCompany;
    }

    public void setDestinationCompany(String destinationCompany) {
        this.destinationCompany = destinationCompany;
    }

    public String getDestinationModule() {
        return destinationModule;
    }

    public void setDestinationModule(String destinationModule) {
        this.destinationModule = destinationModule;
    }

    public String getSourceCompany() {
        return sourceCompany;
    }

    public void setSourceCompany(String sourceCompany) {
        this.sourceCompany = sourceCompany;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }
}
