Wtf.SalesTree = function(config){
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
    
    Wtf.SalesTree.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.SalesTree, Wtf.tree.TreePanel, {
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
        Wtf.SalesTree.superclass.initComponent.call(this);
        treeObj = this;
        
        function _openFunction(node){
            switch (node.id) {
                case "261":
                    callQuotationList();
                    break;
                case "262":
                    callSalesOrderList();
                    break;
                case "263":
                    callDeliveryOrderList();
                    break;               
                case "264":
                    callNewInvoiceList(undefined,undefined,undefined,undefined,undefined,undefined,WtfGlobal.getLocaleText("acc.field.SalesInvoice"));
                    break;               
                case "266":
                    callSalesReturnList(undefined,undefined,undefined,WtfGlobal.getLocaleText("acc.accPref.autoSR"));
                    break;
                case "2612":
                    callSalesJournalEntryDetails(undefined,undefined,undefined,7,2,WtfGlobal.getLocaleText("acc.je.Type2"));//This is for opening Party Journal Entry Report Tab. Parameter 7- JE TYpe ,2-Open Create Party JE Form
                    break;
                case "267":
                    callReceiptReportNew(undefined,undefined,undefined,false,true,undefined,true);// 3 - Open Receive Payment Report which is done using Customer Invoices and 1 - This option used for Open Create Payment For Customer Invoices
//                    callPaymentReceiptNew(1,true, undefined,undefined); // recive payment from customer so isreceipt true
                    break;
                case "268":
                    callPaymentReportNew(undefined,undefined,undefined,false,true,undefined,true);
//                    callPaymentReceiptNew(2,false, undefined,undefined);  //2 for make payment against customer and isreceipt false
                    break;
                case "269":
                    callSalesDebitNoteDetails(undefined,undefined,undefined,4,true,"Customer_Debit_Note",WtfGlobal.getLocaleText("acc.module.name.10"),true);
                    break;
                case "26505":
                    callSalesDebitNoteDetails(undefined,undefined,undefined,5,true,"Customer_Debit_Note",WtfGlobal.getLocaleText("acc.field.CustomerDebitNote"),true,5);
                    break;
                case "2611":
                    callSalesCreditNoteDetails(undefined,undefined,undefined,1,true,"customer_Credit_Note",WtfGlobal.getLocaleText("acc.module.name.12"));
                    break;
                case "26121":
                    callSalesCreditNoteDetails(undefined,undefined,undefined,1,true,"Credit_Note_against_Customer_Invoice",WtfGlobal.getLocaleText("acc.cn.recCN"));
                    break;
                case "26122":
                    callSalesCreditNoteDetails(undefined,undefined,undefined,3,true,"Credit_Note_against_Paid_Customer_Invoice",WtfGlobal.getLocaleText("acc.field.CreditNoteagainstPaidCustomerInvoice"));
                    break;
                //                case "26123":
                //                     SalesReceiptReport(undefined,undefined,undefined,3,1,"Advances_Customers",WtfGlobal.getLocaleText("acc.field.AdvancesCustomers"));
                //                    break;     
                //                case "26124":
                //                     SalesReceiptReport(undefined,undefined,undefined,6,6,"Advances_Vendors");
                //                    break;     
                case "28111":
                    callQuotationList(undefined,true);   //caling the function for particular report
                    break;     
                case "28112":
                    callSalesOrderList(undefined,true);
                    break;     
                case "28113":
                    callInvoiceList(undefined,undefined,undefined,undefined,undefined,undefined,true);
                    break;     
                case "28114":
                    var params={};
                    params.reportbtnshwFlag=true;
                    callDeliveryOrderList(params);
                    break;     
                case "28115":
                    callSalesReturnList(undefined,true);
                    break;     
                case "28116":
                    callContractOrder(false,null, "contract");
                    break;     
                case "282":
                    callCreditNoteDetails(undefined,undefined,undefined,undefined,undefined,undefined,true);
                    break;     
                case "283":
                    if(Wtf.isNewPaymentStructure){
                        callReceiptReportNew(undefined,undefined,undefined,true,true,undefined,true);
                    }
                    
                    break;     
                case "284":
                    callDebitNoteDetails(undefined,undefined,undefined,undefined,undefined,undefined,true,4);
                    break;     
                case "285":
                    if(Wtf.isNewPaymentStructure){
                        callPaymentReportNew(undefined,undefined,undefined,true,true,undefined,true);
                    }
                    break;     
                case "286":
                    callJournalEntryDetails(undefined,undefined,undefined,true);
                    break;     
                case "271":
                    callCustomDesigner("id",2);
                    break;     
                case "272":
                    callCustomDesigner("id",2);
                    break;     
                //                case "28":
                //                   Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                //                    break;
                case "265":
                    callRecInvoiceList(undefined,WtfGlobal.getLocaleText("acc.field.RecurringSalesInvoice"));
                    break;
                case "2613":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                //                case "2615":
                //                    SalesReceiptReport(undefined,undefined,undefined,14,true,"Dishonoured_Cheque",WtfGlobal.getLocaleText("acc.field.DishonouredCheques"));
                //                    break;
                case "2616":
                    //callContractOrder(false,null,null,true);
                     callContractOrderReport(undefined,true,true,undefined);
                    break;
                case "272":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "273":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "274":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "275":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "276":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "277":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "278":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "279":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "2711":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "2712":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "287":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "288":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "289":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "2811":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "2812":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "26411":
                    callSalesReceipt();
                    break;
                case "26412":
                    callInvoice();
                    break;
                case "290":
                    callContractOrderReport(undefined,undefined,true,true);
                    break;
                case "291":
                    callProductReplacementReport(null,true,WtfGlobal.getLocaleText("acc.contract.sales.replacement.report"));
                    break;
                case "292":
                    callProductMaintenanceReport(null,true,WtfGlobal.getLocaleText("acc.contract.sales.maintenance.report"));
                    break;
                case "293":
                    callAgedRecievable({withinventory:true});                    
                    break;
                case "294":
                    if(!Wtf.account.companyAccountPref.packingdolist){
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.pickAndPackreportwarning"));
                    }else{
                        callPackingDoListReport();
                    }
                    break;  
                case "295":
                    if (!Wtf.account.companyAccountPref.pickpackship) {
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.pickAndPackreportwarning"));
                    } else {
                        ShippingDOReport();
                    }
                    break; 
                case "26413":
                    getCustRevenueView();
                    break;
                case "26414":
                    getSalesByProdTabView();
                    break;
                case  "26415":
                    callCustomerDefaultReportList(undefined,undefined,undefined);
                    break;
                case  "26416":
//                    monthlyRevenue(undefined,undefined,undefined, undefined,undefined);
                    monthlyRevenue();
                    break;
                case  "26417":
                    monthlySalesReport(undefined,undefined);
                    break;
                case "26418":
                    getSalesByCustTabView();
                    break;
                case "26419":
                    callSaleByItem();
                    break;   
                case "26500":
                    getSalesBySalesPersonTabView();
                    break;  
                case "26501":
                    topProductsByCustomers();
                    break;  
                case "26502":
                    topCustomersByProducts();
                    break;   
                case "26503":
                    callInactiveCustomerList(undefined,undefined,undefined);
                    break;
                case "26504":
                    CallOutstandingOrdersReport();
                    break;  
                case "DOWithBarcodeScanner":
                    /**
                     * When user click on DO entry in Account Receivable-Sales then redirect to generateorder.jsp page
                     */
                    window.top.location.href = './generateorder.jsp?docType=DO';
                    break;
                case "writeOffCustomerInvoicesId":
                    if(Wtf.account.companyAccountPref.writeOffAccount == undefined || Wtf.account.companyAccountPref.writeOffAccount== null|| Wtf.account.companyAccountPref.writeOffAccount==''){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.setWriteOffAccount")], 2);
                        return;
                    }
                    WriteOffInvoicesWindow(true);
                    break;
                case "writeOffCustomerInvoicesReport":
                    WriteOffInvoicesReport(true);
                    break;    
                case "writeOffReceiptsId" :
                    if(Wtf.account.companyAccountPref.receiptWriteOffAccount == undefined || Wtf.account.companyAccountPref.receiptWriteOffAccount== null|| Wtf.account.companyAccountPref.receiptWriteOffAccount==''){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.setWriteOffAccountForReceipt")], 2);
                        return;
                    }
                    WriteOffPaymentsWindow(true);
                    break;
                case "writeOffPaymentsReport":
                    writeOffPaymentsReport(true);
                    break;
                case "ColoumnerSalesReg":
                    callColoumnerPurchaseOrSalesReg("SaleReg");
                    break;
                case "customerAccountStatement":
                    openAccountStatement(null,"true");
                    break;
                case "PackingReport":
                    if (!Wtf.account.companyAccountPref.pickpackship) {
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("erp.pickAndPackreportwarning"));
                    } else {
                        callPackingReport();
                    }
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
        var arrayListAdj = new Array();
        var arrayDocPrint = new Array();
        var arrayDocReports = new Array();
        var arraySalesReg = new Array();
        var arrayListCI = new Array();
        var arrayMiscellaneous=new Array();
        var arrayListWriteOffItems = new Array();
        var entryNode=_createNode(WtfGlobal.getLocaleText("acc.field.Entry"), '26', false, false, 'images/Accounts_Receivable/Entry.png');
        //arrayList.push(entryNode);
        var docprint=_createNode(WtfGlobal.getLocaleText("acc.field.DocumentPrinting"), '27', false, false, 'images/Accounts_Receivable/Document-Printing.png');
        //        arrayList.push(docprint);   
        var reportNode=_createNode(WtfGlobal.getLocaleText("acc.dash.rep"), '28', false, false, 'images/Accounts_Receivable/Reports.png');
        //arrayList.push(reportNode);
        var MiscellaneousNode=_createNode(WtfGlobal.getLocaleText("acc.navigate.Miscellaneous"), '29', false, false, 'images/Accounts_Receivable/Reports.png');
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewquotation)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.ProformaInvoiceQuotation"), '261', false, true, 'images/Accounts_Receivable/Proforma-Invoice.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewso)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.autoSO"), '262', false, true, 'images/Accounts_Receivable/Sale-Order.png'));
        }
        
        //        if(Wtf.account.companyAccountPref.activateSalesContrcatManagement){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leasecontract, Wtf.Perm.leasecontract.veiwscont)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.contract.create"), '2616', false, true, 'images/Statutory/iras-audit-file.png'));
        }
        
        /**
         * If user has permission of DO and GRN then Following Entry added into Account Receivable-Sales
         */
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.barcodescanner, Wtf.Perm.barcodescanner.DOandGRNWithBarcodeScanner)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.sales.dowithbarcodescanner"), 'DOWithBarcodeScanner', false, true, 'images/Accounts_Receivable/Sale-DO.png'));
        }
        //        }
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.deliveryreport, Wtf.Perm.deliveryreport.viewdo)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesD/O"), '263', false, true, 'images/Accounts_Receivable/Sale-DO.png'));
        }
        var CINode=_createNode(WtfGlobal.getLocaleText("acc.field.SalesInvoice"), '264', false, true, 'images/Accounts_Receivable/Sale-Invoice.png');
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewinvoice)) {
            arrayListEntry.push(CINode);
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.RecurringSalesInvoice"), '265', false, true, 'images/Accounts_Receivable/Recurring-Sales-Invoice.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreturn, Wtf.Perm.salesreturn.viewsret)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.autoSR"), '266', false, true, 'images/Accounts_Receivable/Sales-Return.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreceivepayment, Wtf.Perm.salesreceivepayment.createreceipt)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerReceipt"), '267', false, true, 'images/Accounts_Receivable/Customer-Receipt.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasemakepayment, Wtf.Perm.purchasemakepayment.createpayment)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerPayment"), '268', false, true, 'images/Accounts_Receivable/Customer-Receipt.png'));
        }
        if(Wtf.Countryid != Wtf.Country.MALAYSIA&&!WtfGlobal.EnableDisable(Wtf.UPerm.debitnote, Wtf.Perm.debitnote.viewdn)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerDebitNote"), '269', false, true, 'images/Accounts_Receivable/Credit-Notes-Customers.png'));
        }
        if(Wtf.Countryid == Wtf.Country.MALAYSIA && !WtfGlobal.EnableDisable(Wtf.UPerm.debitnote, Wtf.Perm.debitnote.viewdn)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerDebitNote"), '26505', false, true, 'images/Accounts_Receivable/Credit-Notes-Customers.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditnote, Wtf.Perm.creditnote.viewcn)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerCreditNote"), '2611', false, true, 'images/Accounts_Receivable/Credit-Notes-Customers.png'));
        }

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.PartyJournal"), '2612', false, true, 'images/Accounts_Receivable/Party-Journal.png'));
        }
        //        arrayListEntry.push(_createNode('ST Form Sales', '2613', false, true, 'images/Accounts_Receivable/ST-Form-Sales.png'));
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditnote, Wtf.Perm.creditnote.viewcn)) {
            arrayListAdj.push(_createNode(WtfGlobal.getLocaleText("acc.cn.recCN"), '26121', false, true, 'images/Accounts_Receivable/Credit-Notes-Customers.png'));
        }
        //   arrayListAdj.push(_createNode(WtfGlobal.getLocaleText("acc.field.CreditNoteagainstPaidCustomerInvoice"), '26122', false, true, 'images/Accounts_Receivable/Credit-Notes-Customers.png'));
       
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewreceipt)) {
        //   arrayListAdj.push(_createNode(WtfGlobal.getLocaleText("acc.field.AdvancesCustomers"), '26123', false, true, 'images/Accounts_Receivable/Advances-Customers.png'));
        //            arrayListAdj.push(_createNode('Advances - Vendors', '26124', false, true, 'images/Accounts_Receivable/Advances-Customers.png'));
        }
        var adjNode=_createNode(WtfGlobal.getLocaleText("acc.field.AdjustmentsofDocuments"), '2614', false, false, 'images/Accounts_Receivable/Adjustment-of-Documents.png');
      
        //        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewreceipt)) {
        //            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.DishonouredCheques"), '2615', false, true, 'images/Accounts_Receivable/Dishonoured-Cheques.png'));
        //        }
        
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.ProformaInvoiceQuotationPrinting"), '271', false, true, 'images/Accounts_Receivable/Proforma-Invoice-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesOrderPrinting"), '272', false, true, 'images/Accounts_Receivable/Sale-Order-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesD/OPrinting"), '273', false, true, 'images/Accounts_Receivable/Sale-DO-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesInvoicePrinting"), '274', false, true, 'images/Accounts_Receivable/Sale-Invoice-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesReturnPrinting"), '275', false, true, 'images/Accounts_Receivable/Sales-Return-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.fieldcallSaleByItem.CustomerReceipt"), '276', false, true, 'images/Accounts_Receivable/Customer-Receipt-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerPayment"), '277', false, true, 'images/Accounts_Receivable/Customer-Payment-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerCreditNote"), '278', false, true, 'images/Accounts_Receivable/Customer-Credit-Notes-Print.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerDebitNote"), '279', false, true, 'images/Accounts_Receivable/Customer-Debit-Notes-Printi.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerChequePrinting"), '2711', false, true, 'images/Accounts_Receivable/Customer-Cheques-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PartyJournalPrinting"), '2712', false, true, 'images/Accounts_Receivable/Party-Journal-Printing.png'));
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewquotation)) {
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomerQuotations"), '28111', false, true, 'images/Accounts_Receivable/Customer-Quotation.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewso)) {
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.autoSO"), '28112', false, true, 'images/Accounts_Receivable/Sales-Order.png'));
        }
        /* if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewso)) {
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.contract"), '28116', false, true, 'images/Accounts_Receivable/Sales-Order.png'));
        }*/
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leasecontract, Wtf.Perm.leasecontract.veiwscont)){
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.sales.contractRegister"), '290', false, true, 'images/Statutory/breakup-of-gst-boxes.png'));
        }
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewinvoice)) {
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.Invoices/CashSales"), '28113', false, true, 'images/Accounts_Receivable/Invoice-Cash-Sales.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.deliveryreport, Wtf.Perm.deliveryreport.viewdo)) {
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.autoDO"), '28114', false, true, 'images/Accounts_Receivable/Delivery-Order.png'));
            //arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("erp.PackingDoListReport"), '294', false, true, 'images/Accounts_Receivable/Delivery-Order.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.deliveryreport, Wtf.Perm.deliveryreport.viewdo)) {
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("erp.PackingReport"), 'PackingReport', false, true, 'images/Accounts_Receivable/Delivery-Order.png'));
               arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.shipping.shippingdo"), '295', false, true, 'images/Accounts_Receivable/Delivery-Order.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreturn, Wtf.Perm.salesreturn.viewsret)) {
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.autoSR"), '28115', false, true, 'images/Accounts_Receivable/Sales-Return.png'));
        }
        var saleRegNode=_createNode(WtfGlobal.getLocaleText("acc.field.SalesRegisters"), '281', false, false, 'images/Accounts_Receivable/Sale-Registers.png');
        if(Wtf.UserReporRole.URole.roleid==1){
            arrayMiscellaneous.push(_createNode(WtfGlobal.getLocaleText("acc.rem.237"), '26415', false, true, 'images/Accounts_Receivable/Defaulter-Customer-List.png'));
            arrayMiscellaneous.push(_createNode(WtfGlobal.getLocaleText("acc.field.TopandDormantProductsByCustomers"), '26501', false, true, 'images/Accounts_Receivable/Top-and-Dormant-Products-By-Customers.png'));
            arrayMiscellaneous.push(_createNode(WtfGlobal.getLocaleText("acc.field.TopandDormantCustomersByProducts"), '26502', false, true, 'images/Accounts_Receivable/Top-and-Dormant-Customers-By-Products.png'));
            arrayMiscellaneous.push(_createNode(WtfGlobal.getLocaleText("acc.rem.14.1"), '26503', false, true, 'images/Accounts_Receivable/Inactive-Customer-List.png'));
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesByProduct"), '26414', false, true, 'images/Accounts_Receivable/Sales-By-Product.png'));
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.navigate.monthlySalesRegisterCustomers"), '26417', false, true, 'images/Accounts_Receivable/Monthly-Sales-report.png'));
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesByCustomer"), '26418', false, true, 'images/Accounts_Receivable/Sales-By-Cuctomer.png'));
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.salesByItem"), '26419', false, true, 'images/Accounts_Receivable/Sales-By-Item-Report.png'));      
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesBySalesPerson"), '26500', false, true, 'images/Accounts_Receivable/Delivery-Order.png'));      
            arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.navigate.OutstandingOrdersReport"), '26504', false, true, 'images/Accounts_Receivable/Outstanding-Orders-Report.png'));      
            if(arraySalesReg.length!=0) {
                arrayDocReports.push(saleRegNode);
            }
            if(arrayMiscellaneous.length!=0) {
                arrayDocReports.push(MiscellaneousNode);
            }  
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.navigate.CustomerRevenue"), '26413', false, true, 'images/Accounts_Receivable/Customer-Revenue.png'));
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.navigate.monthlyRevenue"), '26416', false, true, 'images/Accounts_Receivable/Monthly-Revenue.png'));
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.contract.sales.replacement.report"), '291', false, true, 'images/Statutory/gst-form-5.png'));
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.contract.sales.maintenance.report"), '292', false, true, 'images/Statutory/iras-audit-file.png'));
         }
        else{
            for(var i=0;i<Wtf.UserReportPerm.length;i++)
            {
                if(Wtf.UserReportPerm[i]==Wtf.ReportListName.DefaultCustomerList)
                {
                    arrayMiscellaneous.push(_createNode(WtfGlobal.getLocaleText("acc.rem.237"), '26415', false, true, 'images/Accounts_Receivable/Defaulter-Customer-List.png'));
                 
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.TopandDormantProductsByCustomers ) 
                {
                    arrayMiscellaneous.push(_createNode(WtfGlobal.getLocaleText("acc.field.TopandDormantProductsByCustomers"), '26501', false, true, 'images/Accounts_Receivable/Top-and-Dormant-Products-By-Customers.png'));
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.TopandDormantCustomersByProducts)
                {
                    arrayMiscellaneous.push(_createNode(WtfGlobal.getLocaleText("acc.field.TopandDormantCustomersByProducts"), '26502', false, true, 'images/Accounts_Receivable/Top-and-Dormant-Customers-By-Products.png'));
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.InactivecustomerList)
                {
                    arrayMiscellaneous.push(_createNode(WtfGlobal.getLocaleText("acc.rem.14.1"), '26503', false, true, 'images/Accounts_Receivable/Inactive-Customer-List.png'));
                }
               
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.SalesByProduct){
                    arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesByProduct"), '26414', false, true, 'images/Accounts_Receivable/Sales-By-Product.png'));
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.MonthlySalesRegister){
                    arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.navigate.monthlySalesRegisterCustomers"), '26417', false, true, 'images/Accounts_Receivable/Monthly-Sales-report.png'));
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.SalesByCustomer){
                    arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesByCustomer"), '26418', false, true, 'images/Accounts_Receivable/Sales-By-Cuctomer.png'));
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.SalesByItemReport){
                    arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.salesByItem"), '26419', false, true, 'images/Accounts_Receivable/Sales-By-Item-Report.png'));      
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.SalesBySalesPerson){
                    arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.SalesBySalesPerson"), '26500', false, true, 'images/Accounts_Receivable/Delivery-Order.png'));      
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.OutstandingOrdersReport){
                    arraySalesReg.push(_createNode(WtfGlobal.getLocaleText("acc.navigate.OutstandingOrdersReport"), '26504', false, true, 'images/Accounts_Receivable/Outstanding-Orders-Report.png'));      
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.CustomerRevenueReport){
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.navigate.CustomerRevenue"), '26413', false, true, 'images/Accounts_Receivable/Customer-Revenue.png'));
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.MonthlyRevenue){
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.navigate.monthlyRevenue"), '26416', false, true, 'images/Accounts_Receivable/Monthly-Revenue.png'));
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.SalesReplacementReport){
                     arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.contract.sales.replacement.report"), '291', false, true, 'images/Statutory/gst-form-5.png'));
                }
                else if(Wtf.UserReportPerm[i]==Wtf.ReportListName.SalesMaintenanceReport){
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.contract.sales.maintenance.report"), '292', false, true, 'images/Statutory/iras-audit-file.png'));
                }
            }
            if(arraySalesReg.length!=0) {
                arrayDocReports.push(saleRegNode);
            }
            if(arrayMiscellaneous.length!=0) {
                arrayDocReports.push(MiscellaneousNode);
            }  
        }
        
        
       
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditnote, Wtf.Perm.creditnote.viewcn)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.CreditNoteRegisters"), '282', false, true, 'images/Accounts_Receivable/Credit-Notes-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreceivepayment, Wtf.Perm.salesreceivepayment.viewreceipt)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.ReceiptRegisters"), '283', false, true, 'images/Accounts_Receivable/Receipts-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.debitnote, Wtf.Perm.debitnote.viewdn)) { 
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.DebitNoteRegisters"), '284', false, true, 'images/Accounts_Receivable/Debit-Note-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasemakepayment, Wtf.Perm.purchasemakepayment.viewpayment)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.PaymentRegisters"), '285', false, true, 'images/Accounts_Receivable/Payment-Register.png'));
        }
        /* As Discussed in ERP-15677 I am removing "Customer Party Ledger" Link
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.PartyLedgerCustomer"), '286', false, true, 'images/Accounts_Receivable/Party-Ledger-Customer.png'));
        } */
        
        //        if(Wtf.account.companyAccountPref.activateSalesContrcatManagement){
//        arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.contract.sales.replacement.report"), '291', false, true, 'images/Statutory/gst-form-5.png'));
//        arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.contract.sales.maintenance.report"), '292', false, true, 'images/Statutory/iras-audit-file.png'));
//        //        }
        
        //        arrayDocReports.push(_createNode('Outstanding Documents', '287', false, true, 'images/Accounts_Receivable/Outstanding-Documents.png'));
        //        arrayDocReports.push(_createNode('MIS Reports', '288', false, true, 'images/Accounts_Receivable/MIS-Reports.png'));
        //        arrayDocReports.push(_createNode('Special Reports', '289', false, true, 'images/Accounts_Receivable/Special-Reports.png'));
        //        arrayDocReports.push(_createNode('Adjustment Listing', '2811', false, true, 'images/Accounts_Receivable/Adjustment-Listing.png'));
        //        arrayDocReports.push(_createNode('Listing of Masters', '2812', false, true, 'images/Accounts_Receivable/Listing-of-Masters.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.agedreceivable, Wtf.Perm.agedreceivable.viewagedreceivable)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.AgedReceivable"), '293', false, true, 'images/Accounts_Receivable/aged-receivables-icon.png'));
        }
        // var RevenueNode=_createNode(WtfGlobal.getLocaleText("acc.navigate.CustomerRevenue"), '26413', false, true, 'images/Accounts_Receivable/Customer-Revenue.png');
        
        
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.invoice, Wtf.Perm.invoice.viewreceipt)) {
//            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.DishonouredCheques"), '2615', false, true, 'images/Accounts_Receivable/Dishonoured-Cheques.png'));
//        }
//        
        arrayListCI.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.autoCS"), '26411', false, true, 'images/Accounts_Receivable/Sale-Invoice.png'));
        arrayListCI.push(_createNode(WtfGlobal.getLocaleText("acc.field.CreditSales"), '26412', false, true, 'images/Accounts_Receivable/Sale-Invoice.png'));
        
        if(arrayListAdj.length!=0) {
            arrayListEntry.push(adjNode);
        }

       if(!WtfGlobal.EnableDisable(Wtf.UPerm.writeOffInvoice, Wtf.Perm.writeOffInvoice.writeOffEntry)){
            arrayListWriteOffItems.push(_createNode(WtfGlobal.getLocaleText("acc.writeOff.Adj"), 'writeOffCustomerInvoicesId', false, true, 'images/Accounts_Receivable/Write-Off-adjustment.png'));
       }
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.writeOffReceipts, Wtf.Perm.writeOffReceipts.writeOffEntryReceipts)){
            arrayListWriteOffItems.push(_createNode(WtfGlobal.getLocaleText("acc.writeOff.AdjAdvPayment"), 'writeOffReceiptsId', false, true, 'images/Accounts_Receivable/Write-Off-adjustment.png'));
       }
       var writeOffNode= _createNode(WtfGlobal.getLocaleText("acc.rem.158"), 'writeOffNodeId', false, false, 'images/Accounts_Receivable/Write-Off-icon-Small.png');
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.writeOffInvoice, Wtf.Perm.writeOffInvoice.writeOffReport)){
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.writtenOff.SalesInvoice"), 'writeOffCustomerInvoicesReport', false, false, 'images/Accounts_Receivable/Sales-Invoice-Report.png'));
       }
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.writeOffReceipts, Wtf.Perm.writeOffReceipts.writeOffReportReceipts)){
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.writtenOff.ReceiptReport"), 'writeOffPaymentsReport', false, false, 'images/Accounts_Receivable/Sales-Invoice-Report.png'));
       }
//       Hidden for action items in ERP-40018
//       if(Wtf.Countryid==Wtf.Country.INDIA) {
//            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.ColoumnerSalesRegisters"), 'ColoumnerSalesReg', false, false, 'images/Accounts_Receivable/Sales-Invoice-Report.png'));
//       }
       if(arrayListWriteOffItems.length != 0){
            arrayListEntry.push(writeOffNode);
       }    
        /*
        link node to open Statement of Account for Customer
         */
        if(Wtf.UserReporRole.URole.roleid==1){
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.customerAccountStatement"), 'customerAccountStatement', false, false, 'images/Accounts_Receivable/Customer-Quotation.png'));
        }
        else{
            for(var i=0;i<Wtf.UserReportPerm.length;i++){
            
                if(Wtf.UserReportPerm[i]==Wtf.ReportListName.SOACustomerAccountStatement){
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.customerAccountStatement"), 'customerAccountStatement', false, false, 'images/Accounts_Receivable/Customer-Quotation.png'));
                }
            }
        }
        
        if (arrayListEntry.length != 0) {
            arrayList.push(entryNode);
        }
        if (arrayDocReports.length != 0) {
           arrayList.push(reportNode);
        }
        CINode.appendChild(arrayListCI);
        saleRegNode.appendChild(arraySalesReg);
        reportNode.appendChild(arrayDocReports);
        adjNode.appendChild(arrayListAdj);
        if(arrayListWriteOffItems.length != 0){
            writeOffNode.appendChild(arrayListWriteOffItems);
        }    
        entryNode.appendChild(arrayListEntry);
        docprint.appendChild(arrayDocPrint);
        MiscellaneousNode.appendChild(arrayMiscellaneous);
        this.setRootNode(root1);
        root1.appendChild(arrayList);
    }
});