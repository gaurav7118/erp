/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Wtf.ConsignmentStockManagmentPurchase = function(config){
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
    
    Wtf.ConsignmentStockManagmentPurchase.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ConsignmentStockManagmentPurchase, Wtf.tree.TreePanel, {
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
        Wtf.ConsignmentStockManagmentPurchase.superclass.initComponent.call(this);
        treeObj = this;
        
        function _openFunction(node){
            switch (node.id) {
                case "Consign_0":
                    callConsignmentRequest(false, null, null, false,true,false,false,false,true,false);
                    break;
                case "Consign_1":
                    callConsignmentGoodsReceiptOrder(false,null,null, false,false,true);
                    break;
                case "Consign_2":
                    callConsignmentGoodsReceipt(false,null,'consignmentgr',false,false,true);
                    break;              
                case "Consign_3":
                   callConsignmentPurchaseReturn(false,null,null,false,true);
                    break;              
                case "Consign_9":
                    callVendorConsignmentRequestReport(false,false,false,null,WtfGlobal.getLocaleText("acc.VenConsignment.order.report"),true);
                    break;
                case "Consign_5":
                    callConsignmentGoodsReceiptOrderList(false,null,false,null,false, false,false,false,true);                                                         
                     break;
                case "Consign_6":
                      callGoodsReceiptList(null,null,false,undefined, undefined,false,false,true);
                      break;
                case "Consign_11":
                     callConsignmentPurchaseReturnList(false,false,false,null,true);
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
     
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentpurchase, Wtf.Perm.consignmentpurchase.createpurchaseconreq)){    
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.venconsignment.order.create"), 'Consign_0', false, true, 'images/Masters/Profit-&-Loss-layout.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentpurchase, Wtf.Perm.consignmentpurchase.createpurchasecondo)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.GRN.create"), 'Consign_1', false, true, 'images/Account_Payable/cash-sales.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentpurchase, Wtf.Perm.consignmentpurchase.createpurchaseconinv)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.vendor.invoice.create"), 'Consign_2', false, true, 'images/Account_Payable/Purchase-Invoice.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentpurchase, Wtf.Perm.consignmentpurchase.createpurchaseconret)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.purchase.return.create"), 'Consign_3', false, true, 'images/System/preferences.png'));
        }
        if(arrayListEntry.length!=0){
            arrayList.push(entryNode);
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentpurchase, Wtf.Perm.consignmentpurchase.viewpurchaseconreq)){  
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.VenConsignment.order.report"), 'Consign_9', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentpurchase, Wtf.Perm.consignmentpurchase.viewpurchasecondo)){  
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.VenConsignment.GRN.report"), 'Consign_5', false, true, 'images/Account_Payable/Payment-Register.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentpurchase, Wtf.Perm.consignmentpurchase.viewpurchaseconinv)){  
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.vendor.invoice.report"), 'Consign_6', false, true, 'images/Account_Payable/Vendor-Receipt-Payment.png'));
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentpurchase, Wtf.Perm.consignmentpurchase.viewpurchaseconret)){  
            arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.Consignment.purchaseretturn.report"), 'Consign_11', false, true, 'images/System/configuration.png'));
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


