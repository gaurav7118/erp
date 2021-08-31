Wtf.GLCashBankTree = function(config){
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

    Wtf.GLCashBankTree.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.GLCashBankTree, Wtf.tree.TreePanel, {
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
        Wtf.GLCashBankTree.superclass.initComponent.call(this);
        treeObj = this;

        function _openFunction(node){
            switch (node.id) {
                case "231":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "232":  //ERP-2321 i.e. Since this functinality is implemented now
                    //Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "233":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "234":
                   callJournalEntryDetails(undefined,undefined,undefined,false,undefined,WtfGlobal.getLocaleText("acc.je.tabTitle"));
                    break;
                case "235":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "236":
                   callReconciliationWindow();
                    break;
                case "241":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "242":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "243":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "244":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "251":
                    callFrequentLedger(true,"", WtfGlobal.getLocaleText("acc.WoutI.7"), WtfGlobal.getLocaleText("acc.field.accountingbasecashbook"),undefined,WtfGlobal.getLocaleText("acc.dashboard.consolidateCashBookReport"));
                    break;
                case "252":
                    callFrequentLedger(false,"9",WtfGlobal.getLocaleText("acc.WoutI.8"),WtfGlobal.getLocaleText("acc.field.accountingbasebankbook"),undefined,WtfGlobal.getLocaleText("acc.dashboard.consolidateBankBookReport"));
                    break;
                case "253":
                   callJournalEntryDetails(undefined,undefined,undefined,true,undefined,WtfGlobal.getLocaleText("acc.field.JournalBook"));
                    break;
                case "254":
                    callGeneralLedger();
                    break;
                case "255":
                    TrialBalance();
                    break;
                case "256":
                    NewTradingProfitLoss();
                    break;
                case "257":
                   periodViewBalanceSheet();
                    break;
                case "258":
                    callCashFlowStatement();
                    break;
                case "259":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "260":
                    loadReconcilationDrafts();
                    break;
               case "2511":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "2512":
                    callCOA();
                    break;     
                case "23211":
                    if(Wtf.isNewPaymentStructure) {
                        callPaymentReportforVoucherNew(undefined,undefined,undefined,9,WtfGlobal.getLocaleText("acc.dimension.module.5"));  //function to call payment made report
                    }
                    break;
                case "23212":
                    if(Wtf.isNewPaymentStructure) {
                        callReceiptReportforVoucherNew(undefined,undefined,undefined,9,WtfGlobal.getLocaleText("acc.field.RecivePayment")); //function to call payment recived report
                    } 
//                    else {
//                        callReceiptReportforVoucher(undefined,undefined,undefined,9,WtfGlobal.getLocaleText("acc.field.RecivePayment")); //function to call payment recived report
//                    }
                    break;
                case "23213":
                    callRatioAnalysis();
                    break;          
                case  "23214":
                    callBankBookSummaryReport();
                    break;
                case "23215":
                    monthlyTradingProfitLoss();
                    break;
               case "23216":
                    monthlyBalanceSheet();
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
        var arrayDocPrint = new Array();
        var arrayDocReports = new Array();
        var arrayVoucPay = new Array();
        var entryNode=_createNode(WtfGlobal.getLocaleText("acc.field.Entry"), '23', false, false, 'images/General_Ledger/Entry.png');
        //arrayList.push(entryNode);
        var docPrintNode=_createNode(WtfGlobal.getLocaleText("acc.field.DocumentPrinting"), '24', false, false, 'images/General_Ledger/Document-Printing.png');
//        arrayList.push(docPrintNode);
        var reportsNode=_createNode(WtfGlobal.getLocaleText("acc.dash.rep"), '25', false, false, 'images/General_Ledger/Reports.png');
       // arrayList.push(reportsNode);
    
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasemakepayment, Wtf.Perm.purchasemakepayment.viewpayment)) {
            arrayVoucPay.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.module.5"), '23211', false, true, 'images/General_Ledger/make-payment.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreceivepayment, Wtf.Perm.salesreceivepayment.viewreceipt)) {
            arrayVoucPay.push(_createNode(WtfGlobal.getLocaleText("acc.field.RecivePayment"), '23212', false, true, 'images/General_Ledger/receive-payment.png'));
        }
        var voucherNode=_createNode(WtfGlobal.getLocaleText("acc.field.Voucher"), '232', false, false, 'images/General_Ledger/Voucher.png');
//        arrayListEntry.push(_createNode('Contra Voucher', '231', false, true, 'images/General_Ledger/Contra-Voucher.png'));
        if(arrayVoucPay.length!=0) {
            arrayListEntry.push(voucherNode);
        }
//        arrayListEntry.push(_createNode('Recurring Voucher', '233', false, true, 'images/General_Ledger/Recurring-Voucher.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.je.tabTitle"), '234', false, true, 'images/General_Ledger/Journal.png'));
        }
//        arrayListEntry.push(_createNode('Recurring Journal', '235', false, true, 'images/General_Ledger/Recurring-Journal.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.bankreconciliation, Wtf.Perm.bankreconciliation.view)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"), '236', false, true, 'images/General_Ledger/Bank-Reconciliation.png'));
        }
        
        //for sub part under the voucher section
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VoucherPrinting"), '241', false, true, 'images/General_Ledger/Voucher-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.JournalPrinting"), '242', false, true, 'images/General_Ledger/Journal-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VoucherChequePrinting"), '243', false, true, 'images/General_Ledger/Voucher-Cheque-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.ContraVoucherPrinting"), '244', false, true, 'images/General_Ledger/Contra-Voucher-Printing.png'));
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewcashbook)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateCashBookReport"), '251', false, true, 'images/General_Ledger/Cash-Bank-Reports.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewbankbook)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateBankBookReport"), '252', false, true, 'images/General_Ledger/Cash-Bank-Reports.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.bankreconciliation, Wtf.Perm.bankreconciliation.view)) {
            arrayDocReports.push(_createNode("Bank Reconciliation Drafts", '260', false, true, 'images/General_Ledger/Bank-Reconciliation.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.JournalBook"), '253', false, true, 'images/General_Ledger/Journal-Book.png'));
        }
        /*
         * Addded to show/Hide GL report on  prievileges are set in report list
         */
        var i=0;
        if (Wtf.UserReporRole.URole.roleid == 1) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.master.invoiceterm.glaccount"), '254', false, true, 'images/General_Ledger/General-Ledger.png'));
        } else {
            for (i = 0; i < Wtf.UserReportPerm.length; i++) {
                if (Wtf.UserReportPerm[i] == 'General_Ledger_Report') {
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.master.invoiceterm.glaccount"), '254', false, true, 'images/General_Ledger/General-Ledger.png'));
                }
            }
        }        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtrialbalance)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.trial.tabtitle"), '255', false, true, 'images/General_Ledger/Trial-Balance.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtradingpnl)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.Profit&LossAccount"), '256', false, true, 'images/General_Ledger/Profit-&-Loss-Account.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewbsheet)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateBalanceSheetLink"), '257', false, true, 'images/General_Ledger/Balance-Sheet.png'));
        }
        if(Wtf.UserReporRole.URole.roleid==1) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateCashFlowReport"), '258', false, true, 'images/General_Ledger/Cashflow-Statement.png'));
        }
        else{
              for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++)
            {
                if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.CashFlowStatement)
                {
                     arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.consolidateCashFlowReport"), '258', false, true, 'images/General_Ledger/Cashflow-Statement.png'));
                }
            }
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.qanalysis, Wtf.Perm.qanalysis.view)) {
            //arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.ra.tabTT"), '23213', false, true, 'images/Masters/ratio-analysis-report.png'));	//SDP-10451
        }
//        arrayDocReports.push(_createNode('MIS Reports', '259', false, true, 'images/General_Ledger/MIS-Reports.png'));
//        arrayDocReports.push(_createNode('Special Reports', '2511', false, true, 'images/General_Ledger/Special-Reports.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.view)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.ListingofMasters"), '2512', false, true, 'images/General_Ledger/Listing-of-Masters.png'));
        }
        var BankBooksummeryNode=_createNode(WtfGlobal.getLocaleText("acc.navigate.bankbooksummary"), '23214', false, true, 'images/General_Ledger/Bank-Book-Summary.png');
        //arrayDocReports.push(BankBooksummeryNode);
        var monthlyTradingProfitLossNode=_createNode(WtfGlobal.getLocaleText("acc.MonthlyP&L.tabTitle"), '23215', false, true, 'images/General_Ledger/Monthly-Trading-Profit_Loss.png');
        //arrayDocReports.push(monthlyTradingProfitLossNode);
 
        var monthlybalancesheet=_createNode(WtfGlobal.getLocaleText("acc.MonthlyBalanceSheet.tabTitle"), '23216', false, true, 'images/General_Ledger/Balance-Sheet.png');
        if(Wtf.UserReporRole.URole.roleid==1){
            arrayDocReports.push(BankBooksummeryNode); 
            arrayDocReports.push(monthlyTradingProfitLossNode);
            arrayDocReports.push(monthlybalancesheet); 
        } else{
            for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++)
            {
                if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.BankBookSummaryReport)
                {
                    arrayDocReports.push(BankBooksummeryNode); 
                }
                else if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.MonthlyTradingProfitLoss && !WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewtradingpnl) ) 
                {
                    arrayDocReports.push(monthlyTradingProfitLossNode);
                }
                else if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.MonthlyBalanceSheet && !WtfGlobal.EnableDisable(Wtf.UPerm.fstatement, Wtf.Perm.fstatement.viewbsheet))
                {
                    arrayDocReports.push(monthlybalancesheet); 
                }
            }
        }
        if(arrayListEntry.length!=0){
            arrayList.push(entryNode);
        }
        if(arrayDocReports.length!=0){
            arrayList.push(reportsNode);
        }
            
        voucherNode.appendChild(arrayVoucPay);
        reportsNode.appendChild(arrayDocReports);
        docPrintNode.appendChild(arrayDocPrint);
        entryNode.appendChild(arrayListEntry);
        this.setRootNode(root1);
        root1.appendChild(arrayList);
    }
});
