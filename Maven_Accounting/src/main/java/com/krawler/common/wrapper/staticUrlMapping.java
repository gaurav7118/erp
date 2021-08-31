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

package com.krawler.common.wrapper;

import java.util.Arrays;
import java.util.HashMap;

public class staticUrlMapping {

    public final static HashMap<String,Object> staticurlmap = new HashMap<String, Object>();

    public staticUrlMapping() {

                staticurlmap.put("/ACCCompanyPref/*.do","accCompanyPreferencesController");
                staticurlmap.put("/ACCAccount/*.do","accAccountController");
                staticurlmap.put("/ACCAccountCMN/*.do","accAccountControllerCMN");
                staticurlmap.put("/ACCCustomer/*.do","accCustomerController");
                staticurlmap.put("/ACCCustomerCMN/*.do","accCustomerControllerCMN");
                staticurlmap.put("/ACCVendor/*.do","accVendorController");
                staticurlmap.put("/ACCVendorCMN/*.do","accVendorControllerCMN");
                staticurlmap.put("/ACCInvoice/*.do","accInvoiceController");
                staticurlmap.put("/ACCInvoiceCMN/*.do","accInvoiceControllerCMN");
                staticurlmap.put("/ACCRepeateInvoice/*.do","accRepeateInvoice");
                staticurlmap.put("/ACCSalesOrder/*.do","accSalesOrderController");
                staticurlmap.put("/ACCSalesOrderCMN/*.do","accSalesOrderControllerCMN");
                staticurlmap.put("/ACCCreditNote/*.do","accCreditNoteController");
                staticurlmap.put("/ACCReceipt/*.do","accReceiptController");
                staticurlmap.put("/ACCReceiptCMN/*.do","accReceiptControllerCMN");
                staticurlmap.put("/ACCJournal/*.do","accJEController");
                staticurlmap.put("/ACCJournalCMN/*.do","accJEControllerCMN");
                staticurlmap.put("/ACCPurchaseOrder/*.do","accPOController");
                staticurlmap.put("/ACCPurchaseOrderCMN/*.do","accPOControllerCMN");
                staticurlmap.put("/ACCGoodsReceipt/*.do","accGoodsReceiptController");
                staticurlmap.put("/ACCGoodsReceiptCMN/*.do","accGoodsReceiptControllerCMN");
                staticurlmap.put("/ACCDebitNote/*.do","accDebitNoteController");
                staticurlmap.put("/ACCVendorPayment/*.do","accVendorPaymentController");
                staticurlmap.put("/ACCVendorPaymentCMN/*.do","accVendorPaymentControllerCMN");
                staticurlmap.put("/ACCProduct/*.do","accProductcontroller");
                staticurlmap.put("/ACCProductCMN/*.do","accProductControllerCMN");
                staticurlmap.put("/ACCUoM/*.do","accUomController");
                staticurlmap.put("/ACCTax/*.do","accTaxcontroller");
                staticurlmap.put("/ACCMaster/*.do","accMasterItemsController");
                staticurlmap.put("/ACCCurrency/*.do","accCurrencycontroller");
                staticurlmap.put("/ACCTerm/*.do","accTermcontroller");
                staticurlmap.put("/ACCPaymentMethods/*.do","accPaymentController");
                staticurlmap.put("/ACCDepreciation/*.do","accDepreciationController");
                staticurlmap.put("/ACCReconciliation/*.do","accBankReconciliationController");
                staticurlmap.put("/ACCReports/*.do","accReportsController");
                staticurlmap.put("/ACCRevalReports/*.do","accReevaluationReportController");
                staticurlmap.put("/ACCOtherReports/*.do","accOtherReportsController"); 
                staticurlmap.put("/ACCCombineReports/*.do","accReportsCombineController");
                staticurlmap.put("/ACCDashboard/*.do","accDashboardController");
                staticurlmap.put("/CommonFunctions/*.do","commonFunctions");
                staticurlmap.put("/ACCChart/*.do","accChartControllerCMN");
                staticurlmap.put("/ACCExportRecord/*.do","accExportRecordController");
                staticurlmap.put("/ACCExportInvoice/*.do","accExportInvoiceController");
                staticurlmap.put("/ImportRecords/*.do","importcontroller");
                staticurlmap.put("/ACCCommon/*.do","accCommonController");
                staticurlmap.put("/ACCCompanySetup/*.do","newCompanySetupController");
                staticurlmap.put("/kwlCommonTables/*.do","kwlCommonTablesController");
                staticurlmap.put("/ProfileHandler/*.do","profileHandlerController");
                staticurlmap.put("/PermissionHandler/*.do","permissionHandlercontroller");
                staticurlmap.put("/AuthHandler/*.do","authHandlercontroller");
                staticurlmap.put("/CompanyDetails/*.do","companyDetailsController");
		staticurlmap.put("/ACCAudit/*.do","accAuditTrailCMN");
		staticurlmap.put("/ExportPDF/*.do","exportPdfTemplateController");
                staticurlmap.put("/EditHelp/*.do","EditHelpController");
                staticurlmap.put("/CostCenter/*.do","accCostCenterController");
                staticurlmap.put("/ACCExportPrintCMN/*.do","accExportInventoryTemplateController");
                staticurlmap.put("/MailNotify/*.do","AccMailNotifyController");
                staticurlmap.put("/accPeriodSettings/*.do","accPeriodSettingsController");
                staticurlmap.put("/ACCFieldSetup/*.do","accFieldSetupController");
    }
}
