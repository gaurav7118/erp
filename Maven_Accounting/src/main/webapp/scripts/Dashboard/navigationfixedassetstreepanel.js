Wtf.FixedAssets = function(config){
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
    
    Wtf.FixedAssets.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.FixedAssets, Wtf.tree.TreePanel, {
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
        Wtf.FixedAssets.superclass.initComponent.call(this);
        treeObj = this;
        
        function _openFunction(node){
            switch (node.id) {
                case "91":
                    // createFixedAsset();
                    callFixedAssetGrid(null,true);
                    break;
                case "9101":
                    callFixedAssetPurchaseReqList(undefined,true,true,undefined);
                    break;
                case "9103":
                    callFixedAssetVendorQuotationList(undefined,undefined,true);
                    break;
                case "9105":
                    callFixedAssetPurchaseOrderList(undefined,undefined,undefined,undefined,undefined,true);
                    break;
                case "911":
                    callFixedAssetGoodsReceipt(false,null,null,null,false,true);
                    break;              
                case "912":
                    callFixedAssetInvoice(false,null,null,true);
                    break;              
                case "913":
                    callFixedAssetGrid(null,false);
                    break;
                case "914":
                    callFixedAssetDetailsGrid(null,false);
                    break;
                case "9145":
                    callFixedAssetDepreciationDetailsGrid();
                    break;
                case "9102":
                    callFixedAssetPurchaseReqList(undefined,undefined,true,true);
                    break;
                case "9104":
                    callFixedAssetVendorQuotationList(undefined,true,true);
                    break;
                case "9106":
                    callFixedAssetPurchaseOrderList(undefined,true,undefined,undefined,undefined,true);
                    break;
                case "915":
                    callGoodsReceiptList(null,null,false,undefined, undefined,false,true);
                    break;
                case "916":
                    callInvoiceList(null,null,false,false,undefined, undefined,false,true);
                    break;
                case "917":
                    callFixedAssetGoodsReceiptDelivery(false,null,null, false,true);
                    break;
                case "918":
                    callFixedAssetDeliveryOrder(false,null,null,true);
                    break;
                case "919":                    
                    var params={};
                    params.consolidateFlag=false;
                    params.reportbtnshwFlag=false;
                    params.pendingapproval=false;
                    params.isFixedAsset=true;                  
                    callDeliveryOrderList(params);
                    break;
                case "9111":
                    callGoodsReceiptOrderList(false,false,true);
                    break;
                case "9112":
                    callFixedAssetDepreciation();
                    break;
                case "9113":
                    callFixedAssetDetailsGridForMaintenanceScheduler('assetAssetDetailsTabForMaintenanceCreate',true,WtfGlobal.getLocaleText("acc.asset.CreateSchedule"),WtfGlobal.getLocaleText("acc.asset.CreateAssetMaintenanceSchedule"),true,true);
                    break;
                case "9114":
                    callFixedAssetDetailsGridForMaintenanceScheduler('assetAssetDetailsTabForMaintenanceUpdate',true,WtfGlobal.getLocaleText("acc.asset.UpdateSchedules"),WtfGlobal.getLocaleText("acc.asset.UpdateAssetMaintenanceSchedules"),true,false,true);
                    break;
                case "9115":
                    callAssetMaintenanceWorkOrderReport(null);
                    break;
                case "9116":
                    callAssetMaintenanceSchedulerReport(null,true);
                    break;     
                case "9117":
                    callFixedAssetDepreciationUnpost(); 
                    break;
                case "9118":
                    callFixedAssetPurchaseReturnList(undefined);
                    break;
                case "9119":
                    callFixedAssetPurchaseReturnList(true);
                    break;
                case "9120":
                    callFixedAssetSalesReturnList(false,true);
                    break;
                case "9121":
                    callFixedAssetSalesReturnList(true,true);
                    break;
                case "9122":
                    callFixedAssetReqForQuotationList(false,undefined);
                    break;
                case "9123":
                    callFixedAssetReqForQuotationList(false,true);
                    break;
                case "9124":
                    callFixedAssetSummeryReport(false,true);
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
        //arrayList.push(entryNode);
        
        //        var maintenanceSchedulerNode=_createNode('Asset Maintenance Scheduler', '4', false, false, 'images/Masters/custom-fields.png');
        //        arrayList.push(maintenanceSchedulerNode);
        
        var docPrintNode=_createNode(WtfGlobal.getLocaleText("acc.field.DocumentPrinting"), '2', false, true, 'images/customers-vendor.png');
        //        arrayList.push(docPrintNode);
        
        var reportNode=_createNode(WtfGlobal.getLocaleText("acc.dash.rep"), '3', false, false, 'images/Account_Payable/Reports.png');
        //arrayList.push(reportNode);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.viewfa)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("erp.FixedAssetGroups"), '91', false, true, 'images/Account_Payable/assets-groups.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.viewapreq)) {
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.createAssetPurchaseRequisition"), '9101', false, true, 'images/Account_Payable/create-asset-purchase-requisition-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.viewarfq)) {
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lp.createarfq"), '9122', false, true, 'images/Account_Payable/create-asset-purchase-requisition-icon.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.viewavq)) {
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.createAssetVendorQuotation"), '9103', false, true, 'images/Account_Payable/create-asset-vendor-quotation.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.viewapo)) {
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.createAssetPurchaseOrder"), '9105', false, true, 'images/Account_Payable/create-asset-purchase-order.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.createacqinv)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.assets.CreateAssetPurchaseInvoice"), '911', false, true, 'images/Account_Payable/Purchase-Invoice.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetsales, Wtf.Perm.assetsales.createdispinv)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.assets.CreateAssetDisposalInvoice"), '912', false, true, 'images/Account_Payable/cash-sales.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.createfagr)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.assets.CreateAssetGoodsReceipt"), '917', false, true, 'images/Account_Payable/cash-sales.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetsales, Wtf.Perm.assetsales.createfado)){
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lp.createfado"), '918', false, true, 'images/General_Ledger/receive-payment.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereturn, Wtf.Perm.assetpurchasereturn.viewapret)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.createAssetPurchaseReturn"), '9118', false, true, 'images/Account_Payable/Purchase-Return.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.assetsalesreturn, Wtf.Perm.assetsalesreturn.viewasret)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.field.createAssetSalesReturn"), '9120', false, true, 'images/Accounts_Receivable/Sales-Return.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.genfadep)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.fixed.asset.generate.depreciation"), '9112', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.unpostdep)) {
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lp.unpostdep"), '9117', false, true, 'images/Accounts_Receivable/Customer-Quotation.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.viewamaint)) {
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lp.createfamain"), '9113', false, true, 'images/Accounts_Receivable/Customer-Credit-Notes-Print.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.updateamaintshedule)) {
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.lp.updateamaintshedule"), '9114', false, true, 'images/Accounts_Receivable/Customer-Receipt-Printing.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.viewfa)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("erp.FixedAssetGroupsreport"), '913', false, true, 'images/Account_Payable/Receipts-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.viewareport)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.assets.AssetDetailsReport"), '914', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.viewadeprreport)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.FixedAssetDepreciationDetailsReport"), '9145', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.viewadeprreport)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.FixedAssetSummeryReport"), '9124', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.viewapreq)) {
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.assetPurchaseRequisitionList"), '9102', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.viewarfq)) {
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.assetRFQList"), '9123', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.viewavq)) {
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.assetVendorQuotationList"), '9104', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.viewapo)) {
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.assetPurchaseOrderList"), '9106', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.viewacqinv)){
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("erp.navigate.AssetAcquiredInvoiceList"), '915', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetsales, Wtf.Perm.assetsales.viewdispinv)){
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("erp.navigate.AssetDisposalInvoiceList"), '916', false, true, 'images/Account_Payable/Vendor-Debit-Credit-Note.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.veiwfagr)){
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("erp.navigate.AssetReceiptList"), '9111', false, true, 'images/General_Ledger/Listing-of-Masters.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.assetsales, Wtf.Perm.assetsales.viewfado)){
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("erp.navigate.AssetDeliveryList"), '919', false, true, 'images/General_Ledger/Voucher-Printing.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereturn, Wtf.Perm.assetpurchasereturn.viewapret)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.assetPurchaseReturnList"), '9119', false, true, 'images/Accounts_Receivable/Purchase-Return.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.assetsalesreturn, Wtf.Perm.assetsalesreturn.viewasret)) {
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.assetSalesReturnList"), '9121', false, true, 'images/Accounts_Receivable/Sales-Return.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.viewmainworkorder)) {
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("erp.navigate.AssetMaintenanceWorkOrders"), '9115', false, true, 'images/Accounts_Receivable/Sales-Order.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.viewamaint)) {
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("erp.navigate.AssetMaintenanceSchedulesReport"), '9116', false, true, 'images/Accounts_Receivable/Customer-Quotation.png'));
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
