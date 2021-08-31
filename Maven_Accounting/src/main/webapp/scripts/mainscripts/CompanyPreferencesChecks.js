/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

CompanyPreferenceChecks = {           
    withoutBOMCheck:function(){
        if(Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.withoutBOM != undefined && Wtf.account.companyAccountPref.columnPref.withoutBOM != null){
            return Wtf.account.companyAccountPref.columnPref.withoutBOM;
        }else{
            return false;
        }
    },
    /*this function check the value of customporefno checkbox*/
    duplicateCustomerPoReferenceNo: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.customerPoReferenceNo != undefined && Wtf.account.companyAccountPref.columnPref.customerPoReferenceNo != null) {
            return Wtf.account.companyAccountPref.columnPref.customerPoReferenceNo;
        } else {
            return false;
        }
    },
   /*this code is for Productcombo description
     * assgin 0 whhen  type is added inthe combo
     * assign 1 when description is add in the combo 
         * Default value is 0*/
    productComboDisplay:function(){
     if (Wtf.account.companyAccountPref.columnPref != null && Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref. columnPref.Productflag!= undefined && Wtf.account.companyAccountPref. columnPref.Productflag!= null) {
            return Wtf.account.companyAccountPref.columnPref.Productflag;
        } else {
            return Wtf.AccountProducttype;
        }
    },
    //Map default Payment Method field to customer.
    mapDefaultPaymentMethod : function(){
          if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.mapDefaultPaymentToCustomer != undefined && Wtf.account.companyAccountPref.columnPref.mapDefaultPaymentToCustomer != null) {
            return Wtf.account.companyAccountPref.columnPref.mapDefaultPaymentToCustomer;
        } else {
            return false;
        }
    },
        /**
         * GST Calculation Based on Check 
         * Default GST calculation Based on Billing address
         */
    getGSTCalCulationType: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.isGSTCalculationOnShippingAddress != undefined && Wtf.account.companyAccountPref.columnPref.isGSTCalculationOnShippingAddress != null) {
              return Wtf.account.companyAccountPref.columnPref.isGSTCalculationOnShippingAddress;
        } else{
              return false;
        }
    },
    displayUOMCheck:function(){
        if(Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.isDisplayUOM != undefined && Wtf.account.companyAccountPref.columnPref.isDisplayUOM != null){
            return Wtf.account.companyAccountPref.columnPref.isDisplayUOM;
        }
    },
    discountMaster: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.discountMaster != undefined && Wtf.account.companyAccountPref.columnPref.discountMaster != null) {
            return Wtf.account.companyAccountPref.columnPref.discountMaster;
        }else{
            return false;
        }
    },
    discountOnPaymentTerms: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.discountOnPaymentTerms != undefined && Wtf.account.companyAccountPref.columnPref.discountOnPaymentTerms != null) {
            return Wtf.account.companyAccountPref.columnPref.discountOnPaymentTerms;
        }else{
            return false;
        }
    },
//    discountInBulkPayment: function () {
//        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.discountInBulkPayment != undefined && Wtf.account.companyAccountPref.columnPref.discountInBulkPayment != null) {
//            return Wtf.account.companyAccountPref.columnPref.discountInBulkPayment;
//        }else{
//            return false;
//        }
//    },
    recuringSalesInvoiceMemo: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.recuringSalesInvoiceMemo != undefined && Wtf.account.companyAccountPref.columnPref.recuringSalesInvoiceMemo != null) {
            return Wtf.account.companyAccountPref.columnPref.recuringSalesInvoiceMemo;
        }else{
            return 1;
        }
    },
    productPagingCheck: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.allowProductPagingEditing != undefined && Wtf.account.companyAccountPref.columnPref.allowProductPagingEditing != null) {
            return Wtf.account.companyAccountPref.columnPref.allowProductPagingEditing;
        }else{
            return false;
        }
    },
    restrictDuplicateBatchCheck: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch != undefined && Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch != null) {
            return Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch;
        }else{
            return false;
        }
    },
    customerVendorPagingCheck: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.allowCustomerVendorPagingEditing != undefined && Wtf.account.companyAccountPref.columnPref.allowCustomerVendorPagingEditing != null) {
            return Wtf.account.companyAccountPref.columnPref.allowCustomerVendorPagingEditing;
        }else{
            return false;
        }
    },
    
    statusOfRequisitionForPO: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.statusOfRequisitionForPO != undefined && Wtf.account.companyAccountPref.columnPref.statusOfRequisitionForPO != null) {
            return Wtf.account.companyAccountPref.columnPref.statusOfRequisitionForPO;
        } else {
            return false;
        }
    },
    
    highlightDepreciatedAssets: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.highlightDepreciatedAssets != undefined && Wtf.account.companyAccountPref.columnPref.highlightDepreciatedAssets != null) {
            return Wtf.account.companyAccountPref.columnPref.highlightDepreciatedAssets;
        } else {
            return true;//By default true for all subdomains
        }
    },
    
    getRoundingAdjustmentFlag: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.roundingAdjustmentFlag != undefined && Wtf.account.companyAccountPref.columnPref.roundingAdjustmentFlag != null) {
            return Wtf.account.companyAccountPref.columnPref.roundingAdjustmentFlag;
        } else {
            return false;
        }
    },
    isPostingDateCheck: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.isPostingDateCheck != undefined && Wtf.account.companyAccountPref.columnPref.isPostingDateCheck != null) {
            return Wtf.account.companyAccountPref.columnPref.isPostingDateCheck;
        } else {
            return false;
        }
    },
    
    activateDropShip: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.activatedropship != undefined && Wtf.account.companyAccountPref.columnPref.activatedropship != null) {
            return Wtf.account.companyAccountPref.columnPref.activatedropship;
        } else {
            return false;
        }
    },
    activateEWayBill: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref != '' && Wtf.account.companyAccountPref.columnPref.activateEWayBill != undefined && Wtf.account.companyAccountPref.columnPref.activateEWayBill != null) {
            return Wtf.account.companyAccountPref.columnPref.activateEWayBill;
        } else {
            return false;
        }
    },

    /*---Company preferences check to map tax at product level----  */
      mapTaxesAtProductLevel: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.mapTaxesAtProductLevel != undefined && Wtf.account.companyAccountPref.columnPref.mapTaxesAtProductLevel != null) {
            return Wtf.account.companyAccountPref.columnPref.mapTaxesAtProductLevel;
        } else {
            return false;
        }
    },

    advanceSearchInDocumentlinking: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.advanceSearchInDocumentlinking != undefined && Wtf.account.companyAccountPref.columnPref.advanceSearchInDocumentlinking != null) {
            return Wtf.account.companyAccountPref.columnPref.advanceSearchInDocumentlinking;
        } else {
            return false;
        }
    },
    differentUOM: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.differentUOM != undefined && Wtf.account.companyAccountPref.columnPref.differentUOM != null) {
            return Wtf.account.companyAccountPref.columnPref.differentUOM;
        } else {
            return false;
        }
    },
    PeriodicJE: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.differentUOM != undefined && Wtf.account.companyAccountPref.columnPref.differentUOM != null) {
            return Wtf.account.companyAccountPref.columnPref.PeriodicJE;
        } else {
            return false;
        }
    },
    /*---Company preferences check to enable term amounts in landed cost JE----  */
    isLandedCostTermJE: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.isLandedCostTermJE != undefined && Wtf.account.companyAccountPref.columnPref.isLandedCostTermJE!= null) {
            return Wtf.account.companyAccountPref.columnPref.isLandedCostTermJE;
        } else {
            return false;
        }
    },
    /**
     *special rate check for purchase side in company preferences 
     */
    bandsWithSpecialRateForPurchase: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase != undefined && Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase != null) {
            return Wtf.account.companyAccountPref.columnPref.bandsWithSpecialRateForPurchase;
        } else {
            return false;
        }
    },
    activeVersioningInPurchaseOrder: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.activeVersioningInPurchaseOrder != undefined && Wtf.account.companyAccountPref.columnPref.activeVersioningInPurchaseOrder != null) {
            return Wtf.account.companyAccountPref.columnPref.activeVersioningInPurchaseOrder;
        } else {
            return false;
        }
    },
    /*---Company preferences check to send Pending documents to next level while editing----  */
    sendPendingDocumentsToNextLevel: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.sendPendingDocumentsToNextLevel != undefined && Wtf.account.companyAccountPref.columnPref.sendPendingDocumentsToNextLevel != null) {
            return Wtf.account.companyAccountPref.columnPref.sendPendingDocumentsToNextLevel;
        } else {
            return false;
        }
    },
    autoLoadInvoiceTermTaxes: function(){
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.autoLoadInvoiceTermTaxes != undefined && Wtf.account.companyAccountPref.columnPref.autoLoadInvoiceTermTaxes != null) {
            return Wtf.account.companyAccountPref.columnPref.autoLoadInvoiceTermTaxes;
        } else {
            return false;
        }
    },
    /*this function returns the value of Allow Zero UntiPrice In Lease Module checkbox*/
    allowZeroUntiPriceInLeaseModule: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.allowZeroUntiPriceInLeaseModule != undefined && Wtf.account.companyAccountPref.columnPref.allowZeroUntiPriceInLeaseModule != null) {
            return Wtf.account.companyAccountPref.columnPref.allowZeroUntiPriceInLeaseModule;
        } else {
            return false;
        }
    },
    deductSOBlockedQtyFromValuation: function () {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.deductSOBlockedQtyFromValuation != undefined && Wtf.account.companyAccountPref.columnPref.deductSOBlockedQtyFromValuation != null) {
            return Wtf.account.companyAccountPref.columnPref.deductSOBlockedQtyFromValuation;
        } else {
            return false;
        }
    },
    undeliveredServiceSOOpen: function() {
        if (Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref.undeliveredServiceSOOpen != undefined && Wtf.account.companyAccountPref.columnPref.undeliveredServiceSOOpen != null) {
            return Wtf.account.companyAccountPref.columnPref.undeliveredServiceSOOpen;
        } else {
            return true;//By default true for all subdomains
        }
    }
};

