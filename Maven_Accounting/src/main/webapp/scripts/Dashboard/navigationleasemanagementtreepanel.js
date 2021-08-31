Wtf.LeaseManagement = function(config){
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
    
    Wtf.LeaseManagement.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.LeaseManagement, Wtf.tree.TreePanel, {
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
        Wtf.LeaseManagement.superclass.initComponent.call(this);
        treeObj = this;
        
        function _openFunction(node){
            switch (node.id) {
                case "Lease_0":
                    callLeaseQuotation(false, null, null, false,true);
                    break;
                case "Lease_1":
                    callFixedAssetLeaseSalesOrder(false,null,null, false,null,false,false,false,true);
                    break;
                case "Lease_2":
                    callFixedAssetDeliveryOrder(false,null,null,false,true);
                    break;              
                case "Lease_3":
                    callFixedAssetInvoice(false,null,null,false,true);
                    break;              
                case "Lease_4":
                    callSalesOrderList(false,false,false,null,true);
                    break;
                case "Lease_5":
                    var params={};
                    params.consolidateFlag=false;
                    params.filterAppend=false;
                    params.reportbtnshwFlag=false;
                    params.pendingapproval=false;
                    params.isFixedAsset=false;
                    params.isLeaseFixedAsset=true;
                    callDeliveryOrderList(params);
                    break;
                case "Lease_6":
                    callInvoiceList(null,null,false,false,undefined, undefined,false,false,true);
                    break;
                case "Lease_7":
                      callContractOrder(false,null, "contract");
                    break;
                case "Lease_8":
                     callContractOrderReport(undefined,undefined);
                    break;
                case "Lease_9":
                     callQuotationList(false,false,true);
                    break;
                case "Lease_10":
                     callLeaseSalesReturn(false,null,null,true);
                    break;
                case "Lease_11":
                     callSalesReturnList(false,false,true);
                    break;
                case "Lease_12":
                     callProductReplacementReport();
                    break;
                case "Lease_13":
                     callProductMaintenanceReport();
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
      //  
        
        var docPrintNode=_createNode('Document Printing', '2', false, true, 'images/customers-vendor.png');
//        arrayList.push(docPrintNode);
        
        var reportNode=_createNode(WtfGlobal.getLocaleText("acc.dash.rep"), '3', false, false, 'images/Account_Payable/Reports.png');
        //arrayList.push(reportNode);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.createlqt)) { 
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lease.quoation.create"), 'Lease_0', false, true, 'images/Masters/Profit-&-Loss-layout.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.createlor)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lease.order.create"), 'Lease_1', false, true, 'images/Account_Payable/Indent-Requisition.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leasecontract, Wtf.Perm.leasecontract.createlcont)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.contract"), 'Lease_7', false, true, 'images/Statutory/iras-audit-file.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.createldo)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lease.DO.create"), 'Lease_2', false, true, 'images/Account_Payable/Purchase-Invoice.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.createlinv)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lease.customer.invoice.create"), 'Lease_3', false, true, 'images/Account_Payable/cash-sales.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.createlret)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lease.sales.return.create"), 'Lease_10', false, true, 'images/System/preferences.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.veiwlqt)) { 
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.lease.quotation.report"), 'Lease_9', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.viewlor)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.lease.order.report"), 'Lease_4', false, true, 'images/Account_Payable/Receipts-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leasecontract, Wtf.Perm.leasecontract.veiwlcont)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.accPref.contractRegister"), 'Lease_8', false, true, 'images/Statutory/breakup-of-gst-boxes.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.viewldo)){
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.lease.delivery.order.report"), 'Lease_5', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.viewlinv)){
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.lease.customer.invoice.report"), 'Lease_6', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.leaseorder, Wtf.Perm.leaseorder.viewlret)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.lease.sales.return.report"), 'Lease_11', false, true, 'images/System/configuration.png'));
        }
        if(Wtf.UserReporRole.URole.roleid==1){
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.contract.product.replacement.report"), 'Lease_12', false, true, 'images/Statutory/gst-form-5.png'));
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.contract.product.maintenance.report"), 'Lease_13', false, true, 'images/Statutory/iras-audit-file.png'));
        } else{
            for(var userpermcount=0;userpermcount<Wtf.UserReportPerm.length;userpermcount++)
            {
                if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.productreplacementreport)
                {
                    arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.contract.product.replacement.report"), 'Lease_12', false, true, 'images/Statutory/gst-form-5.png'));
                }
                else if(Wtf.UserReportPerm[userpermcount]==Wtf.ReportListName.productmaintenancereport) 
                {
                    arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.contract.product.maintenance.report"), 'Lease_13', false, true, 'images/Statutory/iras-audit-file.png'));
                }
               
            }
        }
        if(arrayListEntry.length!=0){
          arrayList.push(entryNode);  
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
