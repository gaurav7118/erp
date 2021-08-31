Wtf.PurchaseTree = function(config){
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
    
    Wtf.PurchaseTree.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.PurchaseTree, Wtf.tree.TreePanel, {
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
        Wtf.PurchaseTree.superclass.initComponent.call(this);
        treeObj = this;
          function _openFunction(node){
            switch (node.id) {
                case "GRNWithBarcodeScanner":
                    /**
                     *When user click on GRN entry in  Account Payable-Purchases then redirect to  generateorder.jsp page
                     */
                    window.top.location.href = './generateorder.jsp?docType=GR';
                    break;
                case "291":
                    callPurchaseReqList(undefined,true);
                    break;
                case "292":
                    callVendorQuotationList(undefined,true);
                    break;              
                case "293":
                    callPurchaseOrderList(undefined,undefined,undefined,undefined,WtfGlobal.getLocaleText("acc.accPref.autoPO"));
                    break;              
                case "294":
                    callGoodsReceiptOrderList(undefined,undefined,undefined,undefined,WtfGlobal.getLocaleText("acc.field.PurchaseD/OGRN"));
                    break;
                case "295":
                    callPurchaseInvoiceList();
                    break;
                case "2951":
                    callRecPurchaseInvoiceList();
                    break;
                case "296":
                    callPurchaseReturnList(undefined,undefined,WtfGlobal.getLocaleText("acc.dimension.module.18"));
                    break;
                case "297":
                     Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "298":
                     Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "299":
                       callReceiptReportNew(undefined,undefined,undefined,false,true,undefined,false);// 3 - Open Receive Payment Report which is done using Customer Invoices and 1 - This option used for Open Create Payment For Customer Invoices
//                     callPaymentReceiptNew(2,true, undefined,undefined); //recive payment from vendor so isreceipt flag is true 
                    break;
                case "2911":
                       callPaymentReportNew(undefined,undefined,undefined,false,true,undefined,false);
//                  callPaymentReceiptNew(1,false, undefined,undefined) // make paymet to vendor so that 1 is true an it is regarding payment so isrecipt flag is false
                    break;
                case "2912":
                      callSalesDebitNoteDetails(undefined,undefined,undefined,1,false,"VendorDebitNote",WtfGlobal.getLocaleText("acc.module.name.10"));
                    break;
                case "2913":
                        callSalesCreditNoteDetails(undefined,undefined,undefined,4,false,"vendor_Credit_Note",WtfGlobal.getLocaleText("acc.module.name.12"),true);
                    break;
                case "29175":
                    callSalesCreditNoteDetails(undefined, undefined, undefined, 5, false, "vendor_Credit_Note", WtfGlobal.getLocaleText("acc.field.VendorCreditNote"), true, 5);
                    break;
//                case "2914":
//                     callSalesPaymentReport(undefined,undefined,undefined,14,true,"DishonouredCheque",WtfGlobal.getLocaleText("acc.field.DishonouredCheques"))
//                    break;
                case "2915":
                    callSalesJournalEntryDetails(undefined,undefined,undefined,7,2,WtfGlobal.getLocaleText("acc.je.Type2"));//This is for opening Party Journal Entry Report Tab. Parameter 7- JE TYpe ,2-Open Create Party JE Form
                    break;
                case "2916":
                     Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "29171":
                    callSalesDebitNoteDetails(undefined,undefined,undefined,1,false,"DebitNoteagainstVendorInvoice",WtfGlobal.getLocaleText("acc.dn.recDN"));
                    break;
                case "29172":
                     callSalesDebitNoteDetails(undefined,undefined,undefined,3,false,"DebitNoteagainstPaidVendorInvoice",WtfGlobal.getLocaleText("acc.field.DebitNoteagainstPaidVendorInvoice"));
                    break;
//                case "29173":
//                    callSalesPaymentReport(undefined,undefined,undefined,3,1,"AdvancesVendorPayment",WtfGlobal.getLocaleText("acc.field.AdvancesVendors"))
//                    break;
//                case "29174":
//                    callSalesPaymentReport(undefined,undefined,undefined,6,6,"AdvancesCustomerPayment")
//                    break;
                case "300":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "301":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "302":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "303":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "304":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "305":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "306":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "307":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "308":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "309":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "3011":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "3012":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                case "3013":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;
                 case "31111":
                     callVendorQuotationList(undefined,false);
                    break;    
                 case "31112":
                     callPurchaseOrderList(undefined,true);
                    break;    
                 case "31118":
                     callPurchaseOrderList(undefined,true,undefined,undefined,WtfGlobal.getLocaleText("acc.securitygate.title"),undefined,undefined,undefined,undefined,undefined,true);
                    break;    
                 case "31113":
                     callGoodsReceiptList(undefined,undefined,undefined,undefined,undefined,true);
                    break;    
                 case "31114":
                     callGoodsReceiptOrderList(undefined,true);
                    break;    
                 case "31115":
                     callPurchaseReturnList(undefined,true);
                    break;    
                 case "31117":
                     callPurchaseReqList(undefined,false,"","",true);
                    break;    
                 case "31116":
                     callReqForQuotationList(undefined,undefined,true);
                    break;    
                 case "320":
                     callReqForQuotationList(undefined,undefined,undefined);
                    break;    
                 case "312":
                     callCreditNoteDetails(undefined,undefined,undefined,undefined,undefined,undefined,true,4);
                    break;    
                 case "313":
                     if(Wtf.isNewPaymentStructure){
                        callReceiptReportNew(undefined,undefined,undefined,true,true,undefined,false);
                    }
                    break;    
                 case "314":
                     callDebitNoteDetails(undefined,undefined,undefined,undefined,undefined,undefined,true);
                    break;    
                 case "315":
                     if(Wtf.isNewPaymentStructure){
                         callPaymentReportNew(undefined,undefined,undefined,true,true,undefined,false);
                     }
                    break;    
                 case "316":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;    
                 case "317":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;    
                 case "318":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;    
                 case "319":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;    
                 case "3111":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;    
                 case "3112":
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Thisfunctionalityisnotimplementedyet."));
                    break;    
                 case "29311":
                    callGoodsReceiptList();
                    break;    
                 case "29312":
                    callGoodsReceiptList();
                    break;    
                 case "29313":
                     callAgedPayable({withinventory:true});
                     break;
                case "29314":
                    topVendorsByProducts();
                    break;
                case "29315":
                    callColoumnerPurchaseOrSalesReg("PurchaseReg");
                    break;
                case "29316":
                    openAccountStatement(null,"false");
                    break; 
                   /*
                    * For Security Gate Entry Window
                    */
                case "29317":
                    callPurchaseOrderList(undefined,undefined,undefined,undefined,WtfGlobal.getLocaleText("acc.securitygate.title"),undefined,undefined,undefined,undefined,undefined,true);
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
        var arrayPurchaseReg = new Array();
        var arrayListPO = new Array();
       // var arrayMiscellaneous=new Array();
        var entryNode=_createNode(WtfGlobal.getLocaleText("acc.field.Entry"), '29', false, false, 'images/Account_Payable/Entry.png');        
        //arrayList.push(entryNode);
       
        var docPrintNode=_createNode(WtfGlobal.getLocaleText("acc.field.DocumentPrinting"), '30', false, false, 'images/Account_Payable/Document-Printing.png');
//        arrayList.push(docPrintNode);
        var reportNode=_createNode(WtfGlobal.getLocaleText("acc.dash.rep"), '31', false, false, 'images/Account_Payable/Reports.png');
        
       // var MiscellaneousNode=_createNode(WtfGlobal.getLocaleText("acc.navigate.Miscellaneous"), '34', false, false, 'images/Account_Payable/Reports.png');
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.viewpr)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.Indent/Requisition"), '291', false, true, 'images/Account_Payable/Indent-Requisition.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.viewrfq)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.module.23"), '320', false, true, 'images/Account_Payable/Indent-Requisition.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewvendorquotation)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseInwardQuotation"), '292', false, true, 'images/Account_Payable/Purchase-Inward-Quotation-P.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpo)) {
            var PONode=_createNode(WtfGlobal.getLocaleText("acc.accPref.autoPO"), '293', false, false, 'images/Account_Payable/Purchase-Order.png');
            arrayListEntry.push(PONode);
        }
        /*
         * To display Security gate Entry Link on Nevigation Panel
         */
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.securitygate.title"), '29317', false, false, 'images/Account_Payable/Purchase-Order.png'));
            
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.goodsreceiptreport, Wtf.Perm.goodsreceiptreport.viewgr)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseD/OGRN"), '294', false, true, 'images/Account_Payable/Purchase-DO.png'));
        }  
        /**
         * If user has permission of DO and GRN then Following Entry added into Account Payable-Purchases
         */
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.barcodescanner, Wtf.Perm.barcodescanner.DOandGRNWithBarcodeScanner)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.purchase.grnwithbarcodescanner"), 'GRNWithBarcodeScanner', false, true, 'images/Account_Payable/Purchase-DO.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewvendorinvoice)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.agedPay.venInv"), '295', false, true, 'images/Account_Payable/Purchase-Invoice.png'));
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.RecurringPurchaseInvoice"), '2951', false, true, 'images/Accounts_Receivable/Recurring-Sales-Invoice.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasereturn, Wtf.Perm.purchasereturn.viewpret)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.module.18"), '296', false, true, 'images/Account_Payable/Purchase-Return.png'));
        }
        //arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.ProcurementWizard"), '297', false, true, 'images/Account_Payable/Procurement-Wizard.png'));
        //arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.QuotationComparison"), '298', false, true, 'images/Account_Payable/Quotation-Comparison.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreceivepayment, Wtf.Perm.salesreceivepayment.createreceipt)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorReceipt"), '299', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasemakepayment, Wtf.Perm.purchasemakepayment.createpayment)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorPayment"), '2911', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.debitnote, Wtf.Perm.debitnote.viewdn)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorDebitNote"), '2912', false, true, 'images/Account_Payable/Vendor-Debit-Credit-Note.png'));
        }
        if(Wtf.Countryid != Wtf.Country.MALAYSIA && !WtfGlobal.EnableDisable(Wtf.UPerm.creditnote, Wtf.Perm.creditnote.viewcn)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorCreditNote"), '2913', false, true, 'images/Account_Payable/Vendor-Debit-Credit-Note.png'));
        }
        if (Wtf.Countryid == Wtf.Country.MALAYSIA && !WtfGlobal.EnableDisable(Wtf.UPerm.creditnote, Wtf.Perm.creditnote.viewcn)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorCreditNote"), '29175', false, true, 'images/Account_Payable/Vendor-Debit-Credit-Note.png'));
        }
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpayment)) {
//            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.DishonouredCheques"), '2914', false, true, 'images/Account_Payable/Dishonoured-Cheques.png'));
//        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.viewje)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.PartyJournal"), '2915', false, true, 'images/Account_Payable/Party-Journal.png'));
        }
//        arrayListEntry.push(_createNode('ST Form Purchase', '2916', false, true, 'images/Account_Payable/ST-Form-Purchase.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.debitnote, Wtf.Perm.debitnote.viewdn)) {   
            arrayListAdj.push(_createNode(WtfGlobal.getLocaleText("acc.dn.recDN"), '29171', false, true, 'images/Account_Payable/Debit-Notes-Vendors.png'));
        }
         //   arrayListAdj.push(_createNode(WtfGlobal.getLocaleText("acc.field.DebitNoteagainstPaidVendorInvoice"), '29172', false, true, 'images/Account_Payable/Credit-Notes-Vendors.png'));
     
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpayment)) {
        //    arrayListAdj.push(_createNode(WtfGlobal.getLocaleText("acc.field.AdvancesVendors"), '29173', false, true, 'images/Account_Payable/Advances-Customers.png'));
//            arrayListAdj.push(_createNode('Advances - Customers', '29174', false, true, 'images/Account_Payable/Advances-Customers.png'));
        }
        
        var adjNode=_createNode(WtfGlobal.getLocaleText("acc.field.AdjustmentsofDocuments"), '2917', false, false, 'images/Account_Payable/Adjustment-of-Documents.png');
        if(arrayListAdj.length!=0) {
            arrayListEntry.push(adjNode);
        }
        
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseIndent/Requisition"), '300', false, true, 'images/Account_Payable/Purchase-Indent-Requisition.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseInwardQuotationPrinting"), '301', false, true, 'images/Account_Payable/Purchase-Inward-Quotation.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseOrderPrinting"), '302', false, true, 'images/Account_Payable/Purchase-Order-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseOrderAmendmentPrinting"), '303', false, true, 'images/Account_Payable/Purchase-Order-Amendment.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseD/OGRNPrinting"), '304', false, true, 'images/Account_Payable/Purchase-DO-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseInvoicePrinting"), '305', false, true, 'images/Account_Payable/Purchase-Invoice-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseReturnPrinting"), '306', false, true, 'images/Account_Payable/Purchase-Return-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorPayment"), '307', false, true, 'images/Account_Payable/Vendor-Payment-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorReceipt"), '308', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorDebitNote"), '309', false, true, 'images/Account_Payable/Vendor-Debit-Notes-Printing.png'));
            arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorCreditNote"), '3011', false, true, 'images/Account_Payable/Vendor-Credit-Notes-Printin.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorChequePrinting"), '3012', false, true, 'images/Account_Payable/Vendor-Cheques-Printing.png'));
        arrayDocPrint.push(_createNode(WtfGlobal.getLocaleText("acc.field.PartyJournalPrinting"), '3013', false, true, 'images/Account_Payable/Party-Journal-Printing.png'));
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.viewpr)) {
            arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.Indent/RequisitionReport"), '31117', false, true, 'images/Account_Payable/Indent-Requisition.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorpr, Wtf.Perm.vendorpr.viewrfq)){
            arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.module.231"), '31116', false, true, 'images/Account_Payable/Purchase-Indent-Requisition.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewvendorquotation)) {
            arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorQuotations"), '31111', false, true, 'images/Accounts_Receivable/Vendor-Quotation.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewpo)) {
            arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.autoPO"), '31112', false, true, 'images/Accounts_Receivable/Purchase-Order.png'));
        }
         arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.securitygate.title"), '31118', false, true, 'images/Accounts_Receivable/Purchase-Order.png'));
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewvendorinvoice)) {
            arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.VendorInvoices/CashPurchases"), '31113', false, true, 'images/Accounts_Receivable/Vendor-Invoice-Cash-Purchas.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.goodsreceiptreport, Wtf.Perm.goodsreceiptreport.viewgr)) {
            arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.GoodsReceiptOrder"), '31114', false, true, 'images/Accounts_Receivable/Good-Receipt-Order.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasereturn, Wtf.Perm.purchasereturn.viewpret)){
            arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.dimension.module.18"), '31115', false, true, 'images/Accounts_Receivable/Purchase-Return.png'));
        }
        if(Wtf.UserReporRole.URole.roleid==1){
            arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.TopandDormantVendorsByProducts"), '29314', false, true, 'images/Account_Payable/Top-and-Dormant-Vendors-By-Products.png')); 
        }
        else{
            for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++)
            {
                if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.TopandDormantVendorsByProducts)
                {
                    arrayPurchaseReg.push(_createNode(WtfGlobal.getLocaleText("acc.field.TopandDormantVendorsByProducts"), '29314', false, true, 'images/Account_Payable/Top-and-Dormant-Vendors-By-Products.png'));
                }
            }
        }
       var purchaseregNode=_createNode(WtfGlobal.getLocaleText("acc.field.PurchaseRegisters"), '311', false, false, 'images/Account_Payable/Purchase-Register.png');
        if(arrayPurchaseReg.length!=0) {
            arrayDocReports.push(purchaseregNode);
        }
        //if(arrayMiscellaneous.length!=0) {
           // arrayDocReports.push(MiscellaneousNode);
        //}
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditnote, Wtf.Perm.creditnote.viewcn)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.CreditNoteRegisters"), '312', false, true, 'images/Account_Payable/Credit-Notes-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreceivepayment, Wtf.Perm.salesreceivepayment.viewreceipt)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.ReceiptRegisters"), '313', false, true, 'images/Account_Payable/Receipts-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.debitnote, Wtf.Perm.debitnote.viewdn)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.DebitNoteRegisters"), '314', false, true, 'images/Account_Payable/Debit-Note-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasemakepayment, Wtf.Perm.purchasemakepayment.viewpayment)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.PaymentRegisters"), '315', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.agedpayable, Wtf.Perm.agedpayable.viewagedpayable)) {
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.wtfTrans.agedp"), '29313', false, true, 'images/Account_Payable/aged-payables-icon.png'));
        }
//        Hidden for action items in ERP-40018
//        if(Wtf.Countryid==Wtf.Country.INDIA) {
//            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.ColoumnerPurchaseRegisters"), '29315', false, true, 'images/Account_Payable/aged-payables-icon.png'));
//        }
        /*
        link node to open Statement of Account for Vendor
         */
        if(Wtf.UserReporRole.URole.roleid==1){
            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.vendorAccountStatement"), '29316', false, true, 'images/Account_Payable/Vendor-Quotation.png'));
        }
        else{
            for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++)
            {
                if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.SOAVendorAccountStatement){
                    arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.vendorAccountStatement"), '29316', false, true, 'images/Account_Payable/Vendor-Quotation.png'));
                }
            }
        }
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasemakepayment, Wtf.Perm.purchasemakepayment.viewpayment)) {
//            arrayDocReports.push(_createNode(WtfGlobal.getLocaleText("acc.field.DishonouredCheques"), '2914', false, true, 'images/Account_Payable/Dishonoured-Cheques.png'));
//        }
//        arrayDocReports.push(_createNode('Party Ledger Vendor', '316', false, true, 'images/Account_Payable/Party-Ledger-Vendor.png'));
//        arrayDocReports.push(_createNode('Outstanding Documents', '317', false, true, 'images/Account_Payable/Outstanding-Documents.png'));
//        arrayDocReports.push(_createNode('MIS Reports', '318', false, true, 'images/Account_Payable/MIS-Reports.png'));
//        arrayDocReports.push(_createNode('Special Reports', '319', false, true, 'images/Account_Payable/Special-Reports.png'));
//        arrayDocReports.push(_createNode('Adjustment Listing', '3111', false, true, 'images/Account_Payable/Adjustment-Listing.png'));
//        arrayDocReports.push(_createNode('Listing of Masters', '3112', false, true, 'images/Account_Payable/Listing-of-Masters.png'));
        if((!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewvendorinvoice))) {
            arrayListPO.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.autoCP"), '29311', false, true, 'images/Account_Payable/Cash-Purchase.png'));
        }
        if((!WtfGlobal.EnableDisable(Wtf.UPerm.vendorinvoice, Wtf.Perm.vendorinvoice.viewvendorinvoice))) {
            arrayListPO.push(_createNode(WtfGlobal.getLocaleText("acc.field.CreditPurchase"), '29312', false, true, 'images/Account_Payable/cash-sales.png'));
        }
        if(arrayListEntry.length!=0){
            arrayList.push(entryNode);
        }
        if(arrayDocReports.length!=0){
            arrayList.push(reportNode);
        }
   
        
//        PONode.appendChild(arrayListPO);
        purchaseregNode.appendChild(arrayPurchaseReg);
        reportNode.appendChild(arrayDocReports);
        adjNode.appendChild(arrayListAdj);
        entryNode.appendChild(arrayListEntry);
        docPrintNode.appendChild(arrayDocPrint);
       // MiscellaneousNode.appendChild(arrayMiscellaneous);
        this.setRootNode(root1);
        root1.appendChild(arrayList);        
    }
});
