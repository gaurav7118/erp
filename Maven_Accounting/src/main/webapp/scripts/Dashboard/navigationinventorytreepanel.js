Wtf.InventoryTree = function(config){
    this.nodeHash = {};
    var tree;
    var outbox;
    var drafts;
    var deleteditems;
    var starreditems;
    var temptreenode;
    var folders;
    var nodeid;
    var treeObj;
    var composeMail;
    
    Wtf.InventoryTree.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.InventoryTree, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
    //    id: 'folderview',
    border:false,
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    
    
    initComponent: function(){
        Wtf.InventoryTree.superclass.initComponent.call(this);
        
        treeObj = this;
        
        function _openFunction(node){
            switch (node.id) {
                case "3":
                    inventoryConfig();
                    break;
                case "101":
                    markoutallTab();
                    break;
                case "102":
                    callCycleCountForm();
                    break;
                case "103":
                    goodsOrderTab();
                    break;
                case "104":
                    goodsIssueTab();
                    break;
                case "105":
                    interStoreTransfers();
                    break;
                case "106":
                    interLocationTransfers();
                    break;
                case "107":
                    qaApprovalTab();
                    break;
                case "108":
                    interStoreTransfersApproval();
                    break;
                case "109":
                    stockRequestTransfersApproval();
                    break;
                case "110":
                    stockBookingTab();
                    break;
                case "111":
                    consignmentApproval();
                    break;
                case "112":
                    importProductStock();
                    break;
                case "113":
                    callCycleCountCalendar();
                    break;
                case "201":
                    getStockValuationDetailsReport();
                    break;
                case "202":
                    getStockAgeingTabView();
                    break;
                case "203":
                    itemStockLevelByStore();
                    break;
                case "204":
                    transactionBalanceReport();
                    break;
                case "205":
                    itemStockLevelByBatchWise();
                    break;
                case "206":
                    interStoreTransfersReport();
                    break;
                case "207":
                    markoutList1();
                    break;
                case "208":
                    callProductThresholdReport();
                    break;
                case "209":
                    stockMovementReport();
                    break;
                case "210":
                    stockRepaire();
                    break;
                case "211":
                    interLocationTransfersReport();
                    break;
                case "212":
                    itemStockLevelByDate();
                    break;
                case "213":
                    itemBatchStockLevelByDate();
                    break;
                case "214":
                    callReorderLevelReport();
                    break;
                case "215":
                    getStockLedgerTabView();
                    break;
                case "216":
                    showStockStatusReportTab();
                    break;
//                case "217":
//                    getStockValuationDetailsReport();
//                    break;
                case "218":
                    getLocationSummaryReportTabView();
                    break;
                case "219":
                    StockReportOnDimension();
                    break;
                case "220":
                    getStockReportTab();
                    break;
                case "221":
                    callCycleCountReport();
                    break;
                case "222":
                    itemStockLevelBySystemandCustomer();
                    break;
                case "223":
                    StockSummaryReport();
                    break;
                case "224":
                    itemStockLevelByStoreSummary();
                    break;     
                case "225":
                    callProductQuantityDetailsReport();//Sending argument false when calling from incentory
                    break;     
            }
        }
        function _createNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon){
            var treeNode=new Wtf.tree.TreeNode({
                text: nodeText,
                id: nodeID,
                cls:'paddingclass',
                allowDrag: canDrag,
                leaf: isLeaf,
                icon: nodeIcon
            });
            treeNode.on("click",function(node){
                _openFunction(node);
            },this);
            return treeNode;
        }
        var root1 = new Wtf.tree.AsyncTreeNode({
            text: '',
            expanded: true
        });           
        var arrayList = new Array();
        var arrayListEntry = new Array();
        //        var arrayDocPrint = new Array();
        var arrayDocReports = new Array();
        //        var arrayVoucPay = new Array();
        var entryNode=_createNode(WtfGlobal.getLocaleText("acc.field.Entry"), '1', false, false, 'images/General_Ledger/Entry.png');
        // arrayList.push(entryNode);

        var reportsNode=_createNode(WtfGlobal.getLocaleText("acc.dash.rep"), '2', false, false, 'images/General_Ledger/Reports.png');
        // arrayList.push(reportsNode);
        
       
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockadjustment, Wtf.Perm.stockadjustment.createstockadj)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustment"), '101', false, true, 'images/inventory/stock-adjustment-icon.png'));
        }
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.createstockreq)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.invmodule.2"), '103', false, true, 'images/inventory/stock-request-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.issuenote, Wtf.Perm.issuenote.createissuenote)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.invmodule.1"), '104', false, true, 'images/inventory/stock-issue-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.createistreq)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.invmodule.4"), '105', false, true, 'images/inventory/inter-store-stock-transfer-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.interlocationstocktransfer, Wtf.Perm.interlocationstocktransfer.createinterlocationtransferreq)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.invmodule.5"), '106', false, true, 'images/inventory/inter-location-stock-transfer-icon.png'));
        }
//        if(Wtf.account.companyAccountPref.activateCycleCount) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.CycleCountCalendar"), '113', false, true, 'images/inventory/cycle-count-icon.png'));
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.CycleCountForm"), '102', false, true, 'images/inventory/cycle-count-icon.png'));
//        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.up.47"), '107', false, false, 'images/inventory/qa-approval-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.up.56"), '210', false, false, 'images/inventory/stock-repair-icon.png'));
        }
        if(arrayListEntry.length!=0){
            arrayList.push(entryNode);
        }
        //        arrayQAApproval.push(_createNode('Inter Store Approval', '108', false, false, 'images/inventory/stock-acknowledgement-icon.png'));
        //        arrayQAApproval.push(_createNode('Stock Request Approval', '109', false, false, 'images/inventory/stock-acknowledgement-icon.png'));
        //        arrayListEntry.push(_createNode('Stock Booking', '110', false, true, 'images/inventory/stock-acknowledgement-icon.png'));
        //        arrayQAApproval.push(_createNode('Consignment Return Approval', '111', false, false, 'images/inventory/stock-acknowledgement-icon.png'));
        //        arrayListEntry.push(_createNode('Import Stock', '112', false, true, 'images/inventory/stock-adjustment-icon.png'));

        if(Wtf.UserReporRole.URole.roleid==1){
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.StockAgeing"), '202', false, true, 'images/inventory/stock-ageing-icon.png'));
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.stockValuationDetail.tabtitle"), '201', false, true, 'images/inventory/stock-valuation-report-icon.png'));
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.StockReport"), '220', false, true, 'images/inventory/stock-valuation-report-icon.png'));
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.StockLedgerreport"), '215', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.stockStatusReport"), '216', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.locationSummary.tabtitle"), '218', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
        }
        else{
            for (var userpermcount = 0; userpermcount < Wtf.UserReportPerm.length; userpermcount++)
            {
                if (Wtf.UserReportPerm[userpermcount] == Wtf.ReportListName.StockAgeing) {
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.StockAgeing"), '202', false, true, 'images/inventory/stock-ageing-icon.png'));
                }
                else if (Wtf.UserReportPerm[userpermcount] == Wtf.ReportListName.StockValuation) {
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.stockValuationDetail.tabtitle"), '201', false, true, 'images/inventory/stock-valuation-report-icon.png'));
                }
                else if (Wtf.UserReportPerm[userpermcount] == Wtf.ReportListName.StockLedger) {
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.StockLedgerreport"), '215', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
                }
                else if (Wtf.UserReportPerm[userpermcount] == Wtf.ReportListName.StockStatus) {
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.stockStatusReport"), '216', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
            }
                else if (Wtf.UserReportPerm[userpermcount] == Wtf.ReportListName.Stockvaluation) {
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.locationSummary.tabtitle"), '218', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
                }
                 else if (Wtf.UserReportPerm[userpermcount] == Wtf.ReportListName.Stockreport) {
        arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.StockReport"), '220', false, true, 'images/inventory/stock-valuation-report-icon.png'));
                }
            }
        }
       
     
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.lp.stockavailabilitybywarehouse"), '203', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.lp.stockavailabilitybywarehousesummary"), '224', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
            arrayDocReports.push(_createNode("Asset Report", '222', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.batchwisestocktracking)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.BatchwiseStockTracking"), '205', false, true, 'images/inventory/batch-wise-stock-tracking-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.DatewiseStockTracking"), '212', false, true, 'images/inventory/datewise-stock-tracking-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.DatewiseBatchStockTracking"), '213', false, true, 'images/inventory/datewise-batch-stock-tracking-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.materialinoutregister)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.lp.materialinoutregister"), '204', false, true, 'images/inventory/material-in-out-register-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockmovementregister)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.lp.stockmovementregister"), '209', false, true, 'images/inventory/stock-movement-register-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockmovementregister)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.lp.stocksummaryreport"), '223', false, true, 'images/inventory/stock-movement-register-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockmovementregister)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.StockMovementReportWithAdvanceSearch"), '219', false, true, 'images/inventory/stock-movement-register-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.viewistreq)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.InterStoreTransferDetails") ,'206', false, true, 'images/inventory/inter-store-transfer-details-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockadjustment, Wtf.Perm.stockadjustment.viewstockadj)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustmentRegister"), '207', false, true, 'images/inventory/stock-adjustment-register-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.viewproductthreshold)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.ProductThresholdReport"), '208', false, true, 'images/inventory/product-threshold-register-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.viewproductthreshold)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.InterLocationTransferDetails"), '211', false, true, 'images/inventory/inter-location-transfer-details-icon.png'));
        }
//        if(Wtf.account.companyAccountPref.activateCycleCount) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.productList.cycleCountReport"), '221', false, true, 'images/inventory/reorder-level-report-icon.png'));
//        }
        arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.ReorderLevelReport"), '214', false, true, 'images/inventory/reorder-level-report-icon.png'));
        arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.inventoryList.StockDetailsOnUoMBasis"), '225', false, true, 'images/inventory/reorder-level-report-icon.png'));
       
        
//        arrayDocReports.push(_createNode('Stock Valuation Detail Report', '217', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
        
        
        if(arrayDocReports.length!=0){
            arrayList.push(reportsNode);
        }
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.viewinventryconfigration)) {
//            var configNode=_createNode("Configuration", '3', false, false, 'images/System/configuration.png');
//            arrayList.push(configNode);
//        }
        //        arrayDocReports.push(_createNode('Booking History Report', '209', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
        
        
        
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpayment)) {
        //            arrayVoucPay.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.module.5"), '23211', false, true, 'images/General_Ledger/make-payment.png'));
        //        }
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewreceipt)) {
        //            arrayVoucPay.push(_createNode(WtfGlobal.getLocaleText("acc.field.RecivePayment"), '23212', false, true, 'images/General_Ledger/receive-payment.png'));
        //        }
        //        var voucherNode=_createNode(WtfGlobal.getLocaleText("acc.field.Voucher"), '232', false, false, 'images/General_Ledger/Voucher.png');
        ////        arrayListEntry.push(_createNode('Contra Voucher', '231', false, true, 'images/General_Ledger/Contra-Voucher.png'));
        //        if(arrayVoucPay.length!=0) {
        //            arrayListEntry.push(voucherNode);
        //        }
        ////        arrayListEntry.push(_createNode('Recurring Voucher', '233', false, true, 'images/General_Ledger/Recurring-Voucher.png'));
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
        //            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.Journal"), '234', false, true, 'images/General_Ledger/Journal.png'));
        //        }
        ////        arrayListEntry.push(_createNode('Recurring Journal', '235', false, true, 'images/General_Ledger/Recurring-Journal.png'));
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.bankreconciliation, Wtf.Perm.bankreconciliation.view)) {
        //            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"), '236', false, true, 'images/General_Ledger/Bank-Reconciliation.png'));
        //        }
        //        
        //        //for sub part under the voucher section
        //        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VoucherPrinting"), '241', false, true, 'images/General_Ledger/Voucher-Printing.png'));
        //        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.JournalPrinting"), '242', false, true, 'images/General_Ledger/Journal-Printing.png'));
        //        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VoucherChequePrinting"), '243', false, true, 'images/General_Ledger/Voucher-Cheque-Printing.png'));
        //        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.ContraVoucherPrinting"), '244', false, true, 'images/General_Ledger/Contra-Voucher-Printing.png'));
        //        
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewcashbook)) {
        //            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateCashBookReport"), '251', false, true, 'images/General_Ledger/Cash-Bank-Reports.png'));
        //        }
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewbankbook)) {
        //            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateBankBookReport"), '252', false, true, 'images/General_Ledger/Cash-Bank-Reports.png'));
        //        }
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
        //            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.JournalBook"), '253', false, true, 'images/General_Ledger/Journal-Book.png'));
        //        }
        //        arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.master.invoiceterm.glaccount"), '254', false, true, 'images/General_Ledger/General-Ledger.png'));
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtrialbalance)) {
        //            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.trial.tabtitle"), '255', false, true, 'images/General_Ledger/Trial-Balance.png'));
        //        }
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtradingpnl)) {
        //            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.Profit&LossAccount"), '256', false, true, 'images/General_Ledger/Profit-&-Loss-Account.png'));
        //        }
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewbsheet)) {
        //            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateBalanceSheetLink"), '257', false, true, 'images/General_Ledger/Balance-Sheet.png'));
        //        }
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.cashflow, Wtf.Perm.cashflow.view)) {
        //            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateCashFlowReport"), '258', false, true, 'images/General_Ledger/Cashflow-Statement.png'));
        //        }
        ////        arrayDocReports.push(_createNode('MIS Reports', '259', false, true, 'images/General_Ledger/MIS-Reports.png'));
        ////        arrayDocReports.push(_createNode('Special Reports', '2511', false, true, 'images/General_Ledger/Special-Reports.png'));
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.view)) {
        //            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.ListingofMasters"), '2512', false, true, 'images/General_Ledger/Listing-of-Masters.png'));
        //        }
        
        //        voucherNode.appendChild(arrayVoucPay);
        reportsNode.appendChild(arrayDocReports);
        //        docPrintNode.appendChild(arrayDocPrint);
        entryNode.appendChild(arrayListEntry);
        this.setRootNode(root1);
        root1.appendChild(arrayList);
    }
});
