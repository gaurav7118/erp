/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Wtf.ConsignmentStockManagment = function(config){
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
    
    Wtf.ConsignmentStockManagment.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ConsignmentStockManagment, Wtf.tree.TreePanel, {
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
        Wtf.ConsignmentStockManagment.superclass.initComponent.call(this);
        treeObj = this;
        
        function _openFunction(node){
            switch (node.id) {
                case "Lease_0":
                      callConsignmentRequest(false, null, null, false,true,false,false,false,true,true);
                    break;
                case "Lease_1":
                    callConsignmentDeliveryOrder(false,null,null, false,false,true);
                    break;
                case "Lease_2":
                    callConsignmentInvoice(false,null,'consigninv',false,false,true);
                    break;              
                case "Lease_3":
                   callConsignmentSalesReturn(false,null,null,false,true);
                    break;           
                case "107":
                    qaApprovalTab();
                    break;
                case "210":
                    stockRepaire();
                    break;
                case "Lease_9":
                    callConsignmentRequestReport(false,false,false,null,false,true);
                    break;
                case "Lease_5":
                    callConsignmentDeliveryOrderList(false,null,false,null,false, false,false,false,true);
                     break;
                case "Lease_6":
                    callConsignmentInvoiceList(null,null,false,false,undefined, undefined,false,false,false,true);
                    break;
                case "Lease_11":
                     callConsignmentSalesReturnList(false,false,false,null,true);
                    break;
                case "Lease_12":
                    getConsignmentLoanTabView();
                    break;
                case "Lease_13":
                    getConsignmentLoanOutstandingTabView();
                    break;
                case "Lease_14":
                    getStockByCustomerStore();
                    break;
//                case "Lease_15":
//                    getConsignmentQAReport();
//                    break;
                case "Lease_16":
                    getStockRequestOnLoanReport();
                    break;
                    
                case "Lease_17":
                    getConsignmentReturnListTabView();
                    break;
            }
        }
        
        function _createNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon){
            var treeNode= new Wtf.tree.TreeNode({
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
        var arrayListReport = new Array();
        
        var entryNode=_createNode(WtfGlobal.getLocaleText("acc.field.Entry"), '1', false, false, 'images/chart-of-accounts.gif');
       
        var docPrintNode=_createNode('Document Printing', '2', false, true, 'images/customers-vendor.png');
//        arrayList.push(docPrintNode);
        
        var reportNode=_createNode(WtfGlobal.getLocaleText("acc.dash.rep"), '3', false, false, 'images/Account_Payable/Reports.png');
      
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.createsalesconreq)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.consignment.order.create"), 'Lease_0', false, true, 'images/Masters/Profit-&-Loss-layout.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.createsalescondo)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.DO.create"), 'Lease_1', false, true, 'images/Account_Payable/Purchase-Invoice.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.createsalesconinv)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.customer.invoice.create"), 'Lease_2', false, true, 'images/Account_Payable/cash-sales.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.createsalesconret)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.sales.return.create"), 'Lease_3', false, true, 'images/System/preferences.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.up.47"), '107', false, false, 'images/inventory/qa-approval-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.up.56"), '210', false, false, 'images/inventory/stock-repair-icon.png'));
        }
        if(arrayListEntry.length!=0){
            arrayList.push(entryNode);
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewsalesconreq)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.order.report"), 'Lease_9', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewsalescondo)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.delivery.order.report"), 'Lease_5', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewsalesconinv)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.customer.invoice.report"), 'Lease_6', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewsalesconret)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.sales.return.report"), 'Lease_11', false, true, 'images/System/configuration.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentloan)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.loanReport"), 'Lease_12', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentloan)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("Consignment Return Detail Report"), 'Lease_17', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentoutloan)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.loadOutstandingReport"), 'Lease_13', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentcustomerwarehouse)) {
             arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.StockAvailabilityByCustomerWarehouse"), 'Lease_14', false, true, 'images/inventory/stock-availability-by-warehouse.png'));
        }
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
//            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.QAReport"), 'Lease_15', false, true, 'images/inventory/qa-approval-icon.png'));
//        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentloan)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.requestOnLoanReport"), 'Lease_16', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(arrayListReport.length!=0){
            arrayList.push(reportNode);
        }
        
        this.setRootNode(root1);
        entryNode.appendChild(arrayListEntry);
        reportNode.appendChild(arrayListReport);
        root1.appendChild(arrayList);
    }
});


