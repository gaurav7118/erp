<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page contentType="text/html; charset=UTF-8" %> 
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
                <meta name="viewport" content="height=device-height, initial-scale=1.0"/>
		 <title id="acctitle">ERP</title>
<!--              <script type="text/javascript" src="http://www.google.com/jsapi"></script>-->
<!--		<script type="text/javascript" src="../../scripts/googleapi.js"></script>-->
<!--        <script type="text/javascript">
            google.load('visualization', '1', {packages: ['charteditor']});
        </script>-->
        <script type="text/javascript">
            isProdBuild = false;
            /*<![CDATA[*/
			function _r(url) {
				window.top.location.href = url;
			}
            /*]]>*/
        </script>
        <!-- css -->
        <link rel="stylesheet" type="text/css" href="../../lib/resources/css/wtf-all.css"/>
<!--        <link rel="stylesheet" type="text/css" href="../../lib/plugins/column-lock/columnLock.css"/>-->
        <link rel="stylesheet" type="text/css" href="../../style/view.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/ImportWizard.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/portal.css?v=3"/>
        <link rel="stylesheet" type="text/css" href="../../style/dashboardstyles1.css"/>
        <link rel="stylesheet" type="text/css" href="../../style/graphical-dashboard.css"/>
        <link id="theme" rel="stylesheet" type="text/css" />
        
        <!--link rel="stylesheet" type="text/css" href="../../style/taxform.css?v=3"/-->
        <!--[if lte IE 7]>
                    <link rel="stylesheet" type="text/css" href="../../style/ielte6hax.css" />
            <![endif]-->
        <!--[if IE 7]>
                     <link rel="stylesheet" type="text/css" href="../../style/ie7hax.css" />
             <![endif]-->
        <!--[if IE 8]>
                     <link rel="stylesheet" type="text/css" href="../../style/ie8hax.css" />
             <![endif]-->
        <!--[if gte IE 8]>
                     <link rel="stylesheet" type="text/css" href="../../style/ie8hax.css" />
             <![endif]-->
        <!-- /css -->
		<link rel="shortcut icon" href="../../images/favicon.png"/>
    </head>
    <body>
        <div id="loading-mask" style="width:100%;height:100%;background:#c3daf9;position:absolute;z-index:20000;left:0;top:0;">&#160;</div>
        <div id="loading">
            <div class="loading-indicator-init"><img src="../../images/loading.gif" style="width:16px;height:16px; vertical-align:middle" alt="Loading" />&#160;Loading...</div>
        </div>
        <script  src="../../lib/tinymce/tinymce.min.js"></script>      
        <script  src="../../lib/tinymce/tiny_mce_popup.js"></script>      
        <script src="../../lib/amcharts/amcharts.js"></script>
        <script src="../../lib/amcharts/pie.js"></script>
        <script src="../../lib/amcharts/serial.js"></script>
        <script src="../../lib/amcharts/gauge.js"></script>
        <script src="../../lib/amcharts/plugins/export/export.min.js"></script>
        <link rel="stylesheet" href="../../lib/amcharts/plugins/export/export.css" type="text/css" media="all" />
        <script src="../../lib/amcharts/themes/light.js"></script>
        <script src="../../lib/amcharts/themes/black.js"></script>
<!--        <script src="../../lib/amcharts/themes/none.js"></script>-->
        <script src="../../lib/amcharts/themes/dark.js"></script>
        <!-- js -->
<!--        <script type="text/javascript" src="../../scripts/canvg.js"></script>
        <script type="text/javascript" src="../../scripts/rgbcolor.js"></script>
        <script type="text/javascript" src="../../scripts/grChartImg.js"></script>-->
        <script type="text/javascript" src="../../lib/adapter/wtf/wtf-base.js"></script>
        <script type="text/javascript" src="../../lib/wtf-all-debug.js"></script>
        <script type="text/javascript" src="../../scripts/graphicalDashboard/chartPanel.js"></script>
<!--        <script type="text/javascript" src="../../scripts/common/columnLock.js"></script>-->
        <script type="text/javascript" src="../../scripts/common/WtfThemeCombo.js"></script>
        
        <script type="text/javascript" src="../../scripts/common/WtfKWLJsonReader.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfServerDateField.js"></script>
        <script type="text/javascript" src="../../scripts/WtfLibOverride.js"></script>
        <script type="text/javascript" src="../../props/wtf-lang-locale.js"></script>
        <script type="text/javascript" src="../../props/msgs/messages.js"></script>        
        <script type="text/javascript" src="../../scripts/WtfGlobal.js"></script>
        <script type="text/javascript" src="../../scripts/WtfWidgetComponent.js"></script>
        <script type="text/javascript" src="../../scripts/amchart.js"></script>
        <script type="text/javascript" src="../../scripts/WtfCustomPanel.js"></script>
        <script type="text/javascript" src="../../scripts/WtfSettings.js"></script>
        <script type="text/javascript" src="../../scripts/integration/IntegrationConstants.js?v=8"></script>
        <script type="text/javascript" src="../../scripts/core/ScriptLoader.js"></script>
        <script type="text/javascript" src="../../scripts/common/ModuleScripts.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfReportGrid.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AccountRevaluation.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfCreateCustomFields.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/attributeComponent.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfUpdateProfile.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/WtfChannel.js"></script>
        <script type="text/javascript" src="../../scripts/WtfMain-ex.js"></script>
        <script type="text/javascript" src="../../scripts/core/WtfBindBase.js"></script>
        <script type="text/javascript" src="../../scripts/core/WtfBind.js"></script>
        <script type="text/javascript" src="../../scripts/common/CommonERPComponent.js"></script>
        <script type="text/javascript" src="../../scripts/common/BufferView.js"></script>
        <script type="text/javascript" src="../../scripts/common/DetailPanel.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfMasterStores.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfAddComment.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfGetDocsAndCommentList.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfPaging.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/pPageSize.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfPagingPlugin.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/KwlEditorGrid.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/QuickSearch.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/KWLTagSearch.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/KWLLocalSearch.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfComboBox.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfTextField.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/groupHeaderPlugin.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfAdvHtmlEditor.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfRichTextArea.js"></script>
<!--        <script src="../../scripts/amcharts_v3.js"></script>-->
<!--        <script src="../../scripts/serial_v3.js"></script>-->
        <script type="text/javascript" src="../../scripts/common/WtfExtComboBox.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/RadioSelectionModel.js?v=3"></script>
<!--        <script type="text/javascript" src="../../scripts/common/select.js?v=3"></script>-->
        <script type="text/javascript" src="../../scripts/common/WtfPagingMemProxy.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfNotify.js"></script>
        <!--from dashboard-->
        <script type="text/javascript" src="../../scripts/common/WtfUploadFile.js?v=3"></script>        
        <script type="text/javascript" src="../../scripts/alerts/ComAlert.js?v=7"></script>        
        <!--from dashboard-->
        <script type="text/javascript" src="../../scripts/mainscripts/CompanyPreferencesChecks.js?v=8"></script>
        <script type="text/javascript" src="../../scripts/graph/googleGraphRequestHandler.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/RowExpander.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfGridSummary.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/GroupCheckboxSelection.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/UpdateWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ClosablePanel.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TaxWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CurrencyExchange.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TaxCurrencyExchange.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/HTMLEditor.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfDocListView.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/MailWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReconciliationWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReconciliationReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReconciliationHistoryReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ImportBankReconciliation.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/SelectTransactionTypeWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReconciliationDetails.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/HirarchicalGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/COAReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/GroupReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/InventoryReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfDocListWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReceiptReportNew.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReceiptWin.js?v=7"></script>
        <!--script type="text/javascript" src="../../scripts/mainscripts/CurrencyExposureReport.js?v=7"></script-->
        <script type="text/javascript" src="../../scripts/mainscripts/FrequentLedger.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/JournalEntry.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CustomerInformation.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/JournalEntryDetails.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CreditMemoDetails.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ExportInterface.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/builder.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/reportForm.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/selectTemplateWin.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/common/MultiUpload.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/newReportForm.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/selectNewTemplateWin.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/EmailTemplateEditor.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/CustomReport.js"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/widgetReport.js"></script>
        <script type="text/javascript" src="../../scripts/reportBuilder/widgetReportList.js"></script>

        <script type="text/javascript" src="../../scripts/integration/IntegrationSettings.js?v=8"></script>
        <script type="text/javascript" src="../../scripts/integration/UpsIntegration.js"></script>
        <script type="text/javascript" src="../../scripts/integration/AvalaraIntegration.js?v=8"></script>
        <script type="text/javascript" src="../../scripts/integration/DbsIntegration.js?v=8"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CompanyPreferences.js?v=8"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/companyAddressManager.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/transectionTemplate.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CustomerDetails.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ProductDetails.js?v=7"></script>        
        <script type="text/javascript" src="../../scripts/mainscripts/BOMStock.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AssemblyReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CycleCount.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CreditMemo.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Inventory.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ProductAssemblyGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ProductForm.js?v=7"></script> <!--  INV_ACC_MERGE  -->
        <script type="text/javascript" src="../../scripts/mainscripts/UomSchema.js?v=7"></script> <!--  INV_ACC_MERGE  -->
        <script type="text/javascript" src="../../scripts/mainscripts/UomSchemaTypeReport.js?v=7"></script> <!--  INV_ACC_MERGE  -->
        <script type="text/javascript" src="../../scripts/Reports/SalesCommissionSchema.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ProductFormGroup.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/BuildAssemblyForm.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ProductValuation.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/COA.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Group.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/common/advanceSearch.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Invoice.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/NewInvoice.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PurchaseRequisition.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/DeliveryOrder.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/BulkInvoicesList.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/BulkPayment.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/BulkPaymentGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/BulkReceivePayment.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Contract.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Select.js?v=7"></script>          
        <script type="text/javascript" src="../../scripts/mainscripts/SelectPaging.js?v=7"></script>          
        <script type="text/javascript" src="../../scripts/mainscripts/HelpMessage.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/InvoiceList.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/RepeatedInvoice.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/RepeatedJE.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ExpenseInvoiceGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/IBGEntryReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/DeliveryOrderGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/SerialSelectWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfSerialNoWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfSerialNoAutopopulateWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfAssemblySerialNoWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfDiscountDetailsWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/InvoiceGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/NewInvoiceGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/OSDetailGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PayMethodPanelNew.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReceiptWinNew.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AdvancePayment.js?v=7"></script> 
        <script type="text/javascript" src="../../scripts/mainscripts/PaymentEntry.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReceiptEntry.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ReceiptDetail.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/EntityGSTReports/GSTR2MatchAndReconcile.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/InvoiceInfo.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/NoteInfo.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AccountInfo.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/GSTCodeInfo.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/OSDetailGridNew.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Pricelist.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AgedDetails.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CompareAgedAndBalanceSheet.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Tax1099Report.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TrialBalance.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Ledger.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PurchaseSalesReportMaster.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/MasterFormLimitedAccounts.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/BudgetVsCostReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/PaymentWindow.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/ForecastingWidgetReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfJobWorkIngradientDetail.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfFinalStatement.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PayMethodPanel.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/common/MyQuickSearch.js?v=7"></script>
<!--        <script type="text/javascript" src="../../scripts/mainscripts/WtfSetApproval.js?v=7"></script>-->
        <script type="text/javascript" src="../../scripts/mainscripts/WtfSetNotification.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfTransactionManager.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/callGSTRuleSetup.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/EntityGSTReports/callGSTRuleReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Sales/SalesTransactions.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Sales/DOApprovalRules.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Masters/masterTransactionManager.js?v=7"></script>     
        <script type="text/javascript" src="../../scripts/General_Ledger/General_LedgerTransactionManager.js?v=7"></script>     
        <script type="text/javascript" src="../../scripts/Purchases/PurchaseTransaction.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/common/WtfCalculator.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfGridView.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/PreferredProductSelectionWindow.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfAuditTrail.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/importInterface.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/documentImportInterface.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/MasterConfiguration.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/InventorySetup.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PriceReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/SuggestedReorder.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ratioanalysis.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/SetUpWizard.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/SampleTaxCalculation.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/GSTeSubmissionDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Tax1099Data.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AssetRemove.js"></script>
        <script type="text/javascript" src="../../scripts/common/UserManagement.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Masters/MasterPriceList.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/GSTCalculation.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/GSTHistoryData.js?v=7"></script>
        <!--<script type="text/javascript" src="../../scripts/EntityGst/GSTR1Report.js?v=7"></script>-->
        <script type="text/javascript" src="../../scripts/EntityGst/EntityGSTReports/GSTSummary.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/EntityGSTReports/GSTComputationReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/EntityGSTReports/GSTR3BSummaryReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/EntityGSTReports/GSTR3BDetailReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/EntityGSTReports/GSTSummaryDetails.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/EntityGst/GSTRMatchAndReconcile.js?v=7"></script>
        <!-- Philippines Statutory Compliance JS -->
        <script type="text/javascript" src="../../scripts/PhilippinesCompliance/PHPReliefReports.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationmastertreepanel.js?v=3"></script>        
        <script type="text/javascript" src="../../scripts/Dashboard/navigationsystemtreepanel.js?v=3"></script>        
        <script type="text/javascript" src="../../scripts/Dashboard/navigationGLCashBankTreepanel.js?v=3"></script>        
        <script type="text/javascript" src="../../scripts/Dashboard/navigationpurchasetreepanel.js?v=3"></script>        
        <script type="text/javascript" src="../../scripts/Dashboard/navigationfixedassetstreepanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationleasemanagementtreepanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationloanmanagementtreepanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationconsignmenttreepanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationPurchaseConsignmentTreepanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationMiscellaneousTreepanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationsalestreepanel.js?v=3"></script>        
        <script type="text/javascript" src="../../scripts/Dashboard/navigationstatutorytreepanel.js?v=3"></script>   
        <script type="text/javascript" src="../../scripts/Dashboard/navigationmrpmanagementtreepanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationJobWorkTreePanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationJobWorkOutTreePanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/Dashboard/navigationreportstreepanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/PhilippinesCompliance/phpVATReport.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/IndonesiaCompliance/indonesiaVATReport.js?v=3"></script>

        
        <script type="text/javascript" src="../../scripts/common/WtfMultiGroupingPanel.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfMultiGroupingStore.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfMultiGroupingView.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/common/WtfGridSummary.js?v=3"></script>      
        <script type="text/javascript" src="../../scripts/common/WtfServerDateField.js?v=3"></script> 

        <script type="text/javascript" src="../../scripts/mainscripts/GstTaxWindow.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/IAFfile.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfNewTrading.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/linkInvoice.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/MonthlyRevenue.js"></script>
        <script type="text/javascript" src="../../scripts/designer/WtfDesignerDocTemplateList.js"></script>
        <script type="text/javascript" src="../../scripts/common/WtfCustomizeView.js"></script>
        <script type="text/javascript" src="../../scripts/common/FormDetailsWindow.js"></script>
        <script type="text/javascript" src="../../scripts/common/CustomizeReportView.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CustomFieldsHistory.js"></script>
        <script type="text/javascript" src="../../scripts/common/radio_checkbox_grouping.js"></script>
        <script type="text/javascript" src="../../scripts/common/ExportCustomdataInFinancialReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/GSTForm5DetailedView.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/projectstatus.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WipCpAccountSettings.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/OpeningBalance.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/OpeningBalanceforAccount.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/openingBalanceTransactionTab.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TransactionOpeningBalenceForm.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/NoteAgainstInvoiceWindow.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AddressDetail.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/OpenSoPo.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AssetMaintenanceScheduler.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AssetMaintenanceSchedulerReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AssetMaintenanceWorkOrder.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WorkOrderProductDetailsGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AdHocSchedulerGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/MaintenanceSchedulers.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AssetMaintenanceWorkOrderReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/StockReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ChequeSequenceFormatWindow.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ChequeDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CashFlowStatementAsPerCOA.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfMalasianGST.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/VendorIBGDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/VendorIBGDetailsGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/IBGBankDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ContractReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/contractDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/customerContractDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ContractActivityPanel.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ContractProductDetailsGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WtfRCNReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AllowZeroQuantityForProduct.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/AssetCompanyPreferenceSetting.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/BudgetingOnDeptCompanyPrefSetting.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/BadDebtInvoiceList.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ProductExportDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CommonExportDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WriteOffInvoicesWindow.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WrittenOffInvoicesReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/InvoiceMonthWise.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/LinkAdvancePayment.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WriteOffPaymentsWindow.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WrittenOffPaymentsReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/LoanDisbursementInfo.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/IBGFileForUOB.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/GiroFileGenerationHistory.js"></script>
        <!--Fixed Assets and Lease Management-->
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetSalesReturn.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetSalesReturnGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetPurchaseRequisition.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetPurchaseRequisitionGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/fixedassetopeningwindow.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetReport.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/UpdateAssetSerialDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetGroup.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetDepreciation.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetInvoice.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetInvoiceGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetDeliveryOrder.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetDeliveryOrderGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CustomerReceivedReport.js"></script>
         <script type="text/javascript" src="../../scripts/mainscripts/LandingCostItemReport.js"></script>
        <link rel='stylesheet' type='text/css' href='fullcalendar-2.1.1/fullcalendar.css' />
        <script type="text/javascript" src="fullcalendar-2.1.1/lib/jquery.min.js"></script>
        <script type="text/javascript" src="fullcalendar-2.1.1/lib/jquery-ui.custom.min.js"></script>
        <script src='lib/moment.min.js'></script>
        <script type='text/javascript' src='fullcalendar-2.1.1/fullcalendar.js'></script>
        <script type='text/javascript' src='fullcalendar-2.1.1/fullcalendar.min.js'></script>
        <link rel='stylesheet' type='text/css' href='fullcalendar-2.1.1/fullcalendar.print.css' media='print' />
        <script type='text/javascript' src='../../scripts/Calendar.js'></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PackingDoList.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PackDeliveryorder.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ShipDeliveryorder.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/StoreWisecustomerStock.js"></script>

        <!--inventory-->
        <script type="text/javascript" src="../../scripts/Dashboard/navigationinventorytreepanel.js?v=3"></script>  
        <script type="text/javascript" src="../../scripts/inventory/process/MachineMaster.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/process/ProcessMaster.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/cycleCount/WtfCycleCountModule.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/cycleCount/WtfCycleCountReport.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/cycleCount/automatedCycleCount.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/cycleCount/CCStockDetailFormWin.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/goodstransfer/GoodsTransferTab.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/goodstransfer/order.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/goodstransfer/interStoreStockTransferTab.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/goodstransfer/interStoreTransferRequest.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/goodstransfer/interstoreTransfer.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/goodstransfer/interLocationTransfer.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/goodstransfer/goodIssue.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/goodstransfer/GoodsOrderTransfer.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/stockadjustment/AddEditMaster.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/stockadjustment/makoutallTab.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/stockadjustment/markout.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/stockadjustment/markoutList.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/threshold/ProductThresholdForm.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/threshold/ThresholdReport.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/StockDetailFormWin.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/TransactionBalanceReport.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/StockMovementReport.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/StockSummaryReport.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/inventoryLevel.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/InventoryConfiguration.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/SequenceFormat.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/stockacknowledgement/Receipt.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/store/storeMasterGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/inventory/store/exchangeRecordGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/inventory/InventorySettings.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/QAApproval/StockoutApproval.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/QAApproval/InterstoreApproval.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/QAApproval/InspectionForm.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/QAApproval/InspectionTemplate.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/store/DefaultWarehouse.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/inventory/LocationMaster.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/ReorderLevelReport.js"></script>
        <script type="text/javascript" src="../../scripts/inventory/ActivateDeactivateInventory.js"></script>
        
        <!-- Consignment Module JS-->
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentRequestApproval.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentRequestPendingApproval.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentStockInvoice.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentStockInvoiceGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentStockDeliveryOrder.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentStockDeliveryorderGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentStockSalesReturn.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentStockSalesReturnGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ConsignmentLoan.js"></script>
        
        <script type="text/javascript" src="../../scripts/mainscripts/ManageEligibilityRules.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Disbursement.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/DisbursementReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/LocationWarehouseSetup.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CreditNoteDebitNoteInvoice.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CreditNoteDebitNoteInvoiceGrid.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PricingBandMaster.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TaxableDeliveryOrderList.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TaxAdjustment.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TaxAdjustmentGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/UserGroup.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/VersionList.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/PriceListVolumeDiscount.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/Budgeting.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ProductQuantityDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CostAndMargin.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/SetPriceListForBandWindow.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/BadDebtReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/GSTTaxes.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TDSPaymentWindow.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/TermSelGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/dealerExciseDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/supplierExciseDetails.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/SupplierExciseGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/DealerExciseGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/RepeatedPayment.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetDepreciationDetailsGrid.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/FixedAssetSummeryReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/WastageCalculation.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/GroupDetailReport.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/CloseBookCheckList.js"></script>
        
        <!--Report JS Sccripts-->
        <script type="text/javascript" src="../../scripts/Reports/WtfMonthlyCustomLayout.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/TDSMasterRates.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/IndiaCompanyPreferences.js"></script>
        <script type="text/javascript" src="../../scripts/common/ReportScripts.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CustomerRevenue.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/BankBookSummayReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesComissionReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/CashFlowStatement.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CostCenterDetailsReport.js"></script> 
        <script type="text/javascript" src="../../scripts/Reports/CreditNoteWithAccountDetail.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CustomDetailSummaryReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/CustomerCreditExceptionReport.js?v=7"></script>  
        <script type="text/javascript" src="../../scripts/Reports/WtfPartyLedger.js"></script>
         <script type="text/javascript" src="../../scripts/Reports/CustomerVendorLedger.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/SerialNoDetails.js"></script> 
         <script type="text/javascript" src="../../scripts/Reports/WtfDimensionBasedProfitAndLoss.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/WtfDimensionBasedTrialBalance.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/DimensionsReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/DriversTrackingReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/FinanceReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ForeignCurrencyExposure.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/InactiveCustomerList.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/InventoryMovementReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/VATandVHTReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/Rule16Register.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/DVATForm31Report.js"></script>
        <script type="text/javascript" src="../../scripts/mainscripts/ColoumnerPurchaseReg.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/RG23Part1.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/RG23Part2.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/Annexure10Report.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/TDSChallanControlReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/dailyStockReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/vatRegister.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/plaReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/serviceTaxReports.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/AccLinkDataReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/MissingAutoSequenceNumber.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/WtfMonthlyBalanceSheet.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/MonthlyAgeingReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/WtfNewMonthlyTrading.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/YearlyTradingAndPLReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/MonthlySalesByProductSubjectToGSTReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/MonthlySalesReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/SMTPAuthentication.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/DailySalesReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/BookingsReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/Widgets.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/ExciseComputationReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/ServiceTaxComputationReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesPurhcaseAnnexureReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/VATAndCSTCalculationReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/RecurringInvoiceList.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/MaintenanceReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ProductReplacementList.js"></script> 
        <script type="text/javascript" src="../../scripts/Reports/QAPendingRejectedItems.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/RevenueRecognitionReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesByServiceProductDetailReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/salesPersonCommissionDimensionReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesByItem.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesByItemDetail.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesProductCategoryDetailReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/DayEndCollectionReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/JobWorkOutOrderWithoutGRN.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/GSTSalesTaxLiabilityReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesCommissionOnDimension.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/CheckInCheckOutReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/ChallanWiseReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/JobWorkInAgedReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/JWProductSummaryReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/CustomerSummaryMonthlySales.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/CustomDetailReport.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/WtfPendingApproval.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/StockAgeing.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/StockLedger.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/StockReportOnDimension.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/StockStatus.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/StockValuationDetailReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/LocationSummary.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/TopAndDormantUsers.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/TradingAndProfitLossWithBudget.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesCommissionStatementForPaymentTerm.js?v=7"></script>
        <script type="text/javascript" src="../../scripts/Reports/MonthlyCommissionOfSalesPerson.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/salesCommissionDetailReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/salesCommissionproductDetailReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/DeliveryPlanner.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/WtfAccountForecasting.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/VehicleDeliverySummaryReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesPurchaseReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CommonReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/PriceVarianceReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/VendorProductPriceListReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/customervendorregistry.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/GstFormGenerationHistory.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CreateConsolidationReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ConsolidationReportGeneration.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ConsolidationReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ConsolidationProfitAndLoss.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ConsolidationBalanceSheet.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ConsolidationStockReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ConsolidationCustomBalanceSheet.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ConsolidationCustomPNL.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CostOfManufacturing.js"></script>
        
        <script type="text/javascript" src="../../scripts/Reports/TaxPeriodTab.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/TaxPeriodGrid.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CreateTaxPeriodWindow.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ProductTransactionDetail.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/accountingPeriodTab.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/AccountingPeriodGrid.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CheckColumnComponent.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CreateAccountingPeriodWindow.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/PriceListBandReport.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/ProductBrandDiscount.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/CostAndSellingPriceOfItemsToCustomer.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/MonthwiseGeneralLedger.js"></script>
        <script type="text/javascript" src="../../scripts/Reports/SalesAnalysis.js"></script>


        <script type="text/javascript" src="../../scripts/MRP/WorkCentreMasterReport.js?v=7"></script> <!--  INV_ACC_MERGE  -->
        <script type="text/javascript" src="../../scripts/MRP/WorkcentreMasterForm.js?v=7"></script> <!--  INV_ACC_MERGE  -->

        <script type="text/javascript" src="../../scripts/MRP/JobOrderReport.js?v=7"></script> <!--  INV_ACC_MERGE  -->
        <script type="text/javascript" src="../../scripts/MRP/WorkCentreList.js?v=7"></script> <!--  INV_ACC_MERGE  -->
        <script type="text/javascript" src="../../scripts/Dashboard/navigationmrpmanagementtreepanel.js?v=3"></script>

        
        <script type="text/javascript" src="../../scripts/MRP/workorder.js?v=3"></script>
        <script type="text/javascript" src="../../scripts/MRP/workOrderEntryForm.js"></script>
          <script type="text/javascript" src="../../scripts/MRP/workOrderReport.js"></script>
          <script type="text/javascript" src="../../scripts/MRP/WorkOrderStockDetailsReport.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/JobOrderEntryForm.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/JobWorkOrderGrid.js"></script>
        <!--<script type="text/javascript" src="../../scripts/MRP/RoutingMasterList.js"></script>-->
        <script type="text/javascript" src="../../scripts/MRP/MRPAssemblyProductDetails.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/RoutingMasterList.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/RoutingTemplateMaster.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/MachineMasterList.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/MachineMaster.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/MRPTransactionManager.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/MasterContract.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/RejectedItemListMRP.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/MRPQCReport.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/ResourceCost.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/TaskProgressReport.js"></script>
        <script type="text/javascript" src="../../scripts/MRP/labourCostingReport.js"></script>
        <!--<script type="text/javascript" src="../../scripts/MRP/RoutingTemplateMasterGrid.js"></script>-->
        <script type="text/javascript" src="../../scripts/Reports/TestMailWindow.js"></script>
        <script type="text/javascript" src="../../scripts/graphicalDashboard/dashboardManager.js"></script>
        <script type="text/javascript" src="../../scripts/graphicalDashboard/createDashboard.js"></script>
        <script type="text/javascript" src="../../scripts/graphicalDashboard/showDashboard.js"></script>
        <script type="text/javascript" src="../../scripts/graphicalDashboard/dashboard.js"></script>
        <script type="text/javascript">
			/*<![CDATA[*/
			PostProcessLoad = function () {
				setTimeout(function () {
					Wtf.get('loading').remove();
					Wtf.get('loading-mask').fadeOut({remove: true});
				}, 250);
				Wtf.EventManager.un(window, "load", PostProcessLoad);
			}
			Wtf.EventManager.on(window, "load", PostProcessLoad);
			/*]]>*/
		</script>
		<script type="text/javascript">
			var is_chrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;
			if (is_chrome) {

                document.write('<link rel="stylesheet" href="../../style/webkit.css" type="text/css" />');
            }
            if (navigator.userAgent.indexOf("MSIE 10") > -1) {
                document.write('<link rel="stylesheet" href="../../style/ie10hax.css" type="text/css" />');
            }
            if (navigator.userAgent.indexOf("Safari") > -1) {
                document.write('<link rel="stylesheet" href="../../style/webkit.css" type="text/css" />');
            }
        </script>
        <!-- /js -->
        <!-- html -->
            <img id="dummyCompanyLogo" style="display:none;" src="<%=getServletContext().getInitParameter("platformURLProtocolNeutral")%>b/<%=com.krawler.common.util.URLUtil.getDomainName(request)%>/images/store/?company=true" alt="logo"/>
        <div id="header" style="position: relative;">
<!--            <div id="headTimezone" class="TimezonePopup" id="wtf-gen442">
                <div class="TimezoneMessage">Please note that your timezone is different from your organization's timezone. Please <a onclick="showPersnProfile()" href="#" style="color:#445566;" class="helplinks">click here</a> to update.</div>
                <div class="TimezoneImage" onclick = "closeTimezonePop()">&nbsp</div>
            </div>
        
            <img id="companyLogo" src="<%=getServletContext().getInitParameter("platformURL")%>b/<%=com.krawler.common.util.URLUtil.getDomainName(request)%>/images/store/?company=true" alt="logo"/>
            <img src="../../images/Deskera-financials-text.png" alt="accounting" style="float:left;margin-left:4px;margin-top:1px;" />
            <div class="userinfo"> 
                <span id="whoami"></span><br /><a id="signout"; href="#" onclick="signOut('signout');"wtf:qtip=''><script></script></a>&nbsp;&nbsp;<a id="changepass"; href="#" onclick="showPersnProfile1();"wtf:qtip=''><script></script></a>&nbsp;&nbsp;<a id="myacc"; href="#" onclick="showPersnProfile();" wtf:qtip=''><script></script></a>&nbsp;&nbsp;<a id="cal"; href="#" onclick="callCalendar();" wtf:qtip=''><script></script></a>
            </div>
            <div id="serchForIco"></div>
            <div id="searchBar"></div>
            <div id="shortcuts" class="shortcuts">
                <div id="menulinks" style="float:right !important;position: relative;">
                    <div id="shortcutmenu6"style="float:left !important;position: relative;"></div>
                    <div id="dash6"style="float: left ! important; margin-top: 3px;">|</div>
                    <div id="shortcutmenu1"style="float:left !important;position: relative;"></div>
                    <div id="dash1"style="float: left ! important; margin-top: 3px;">|</div>
                    <div id="shortcutmenu2"style="float:left !important;position: relative;"></div>
                    <div id="dash2"style="float: left ! important; margin-top: 3px;">|</div>
                      <div id="shortcutmenu3"style="float:left !important;position: relative;"></div>
                                                            <div id="dash3"style="float: left ! important; margin-top: 3px;">|</div>                
                    <div id="shortcutmenu4"style="float:left !important;position: relative;"></div>
                    <div id="dash4"style="float: left ! important; margin-top: 3px;">|</div>
                    <div id="shortcutmenu5"style="float:left !important;position: relative;"></div>
                    </div>
                <div id="signupLink"style="float: right ! important; margin-top: 3px;"></div>
                </div>-->
                    </div>
        <div id='centerdiv'></div>
        <div id="fcue-360-mask" class="wtf-el-mask" style="display:none;z-index:1999999;opacity:0.3;">&nbsp;</div>
        <div style="display:none;">
            <iframe id="downloadframe" name="downloadframe"></iframe>
        </div>
        <form id="designpanelpreview" target="downloadframe"
              method="post" action="CustomDesign/showSamplePreview.do">
            <input type="hidden" name="json" value="[]"/>
            <input type="hidden" name="html" value=""/>
            <input type="hidden" name="moduleid" value="1"/>
        </form>
<!-- /html -->
    </body>
</html>
