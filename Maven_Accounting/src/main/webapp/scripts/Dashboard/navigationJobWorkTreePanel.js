/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
Wtf.navigationJobWorkTreePanel = function (config) {
    this.nodeHash = {};
    Wtf.navigationJobWorkTreePanel.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.navigationJobWorkTreePanel, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
    border: false,
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    initComponent: function () {
        Wtf.navigationJobWorkTreePanel.superclass.initComponent.call(this);
        treeObj = this;

        function _openFunction(node) {
            switch (node.id) {
                //Entry
                case "MRP_JOB_WORK_ORDER_RECIVER":
//                      callGoodsReceiptList(null,null,false,undefined, undefined,false,false,false,true);
                    callSalesOrderList(undefined, false, false, false, false, "", "", false, true);
                    break;
                case "MRP_JOB_WORK_IN_RECIVER":
                    markoutList1(undefined, true);
//                    markoutallTab(true);
                    break;
                case "qaApproval":
                    /*
                     * qa Approval Tab (passing isJobWorkOrder=true for JOb work Order flow)
                     */
                    var isJobWorkOrder = true;
                    var isisJobWorkOrderInQA=false;
                    qaApprovalTab(isJobWorkOrder, isisJobWorkOrderInQA);
                    break;    
                 case "qaApprovalout":
                    /*
                     * qa Approval Tab (passing isJobWorkOrder=true for JOb work Order flow)
                     */
                    var isJobWorkOrder = true;
                    var isisJobWorkOrderInQA=true;
                    qaApprovalTab(isJobWorkOrder, isisJobWorkOrderInQA);
                    break;        
                case "MRP_MASTER_PRODUCTMASTER":
                      callAssemblyReport(false);
                    break;
                case "jobworkinvoice":
                    callInvoiceList();
                    break;
                case "Challan_Report":
                    ChallanWiseReport();
                    break;
                case "Work_Order_In_Aging_Report":
                    getJobWorkInAgedReport();
                    break;
                case "JWPSR":
                    JWProductSummaryWiseReport();
                    break;
                case "qaApproval":
                    /*
                     * qa Approval Tab (passing isJobWorkOrder=true for JOb work Order flow)
                     */
                    var isJobWorkOrder = true;
                    var isisJobWorkOrderInQA=false;
                    qaApprovalTab(isJobWorkOrder);
                    break;
                case "stockrepair":
                    /*
                     * qa Approval Tab (passing isJobWorkOrder=true for JOb work Order flow)
                     */
                    stockRepaire();
                    break;
                case "MRP_JOB_WORK_OUT_RECIVER":
                    var params={};
                    params.consolidateFlag=false;
                    params.moduleid=Wtf.MRP_Job_Work_OUT_REC;
                    params.reportbtnshwFlag=false;
                    params.isFixedAsset=false;
                    params.isLeaseFixedAsset=false;
                    params.isUnInvoiceDOReport=false;
                    params.isJobWorkOutReciever=true;
                    callDeliveryOrderList(params);
                    break;
                    

            }
        }

        function _createNode(nodeText, nodeID, canDrag, isLeaf, nodeIcon) {
            var treeNode = new Wtf.tree.TreeNode({
                text: nodeText,
                id: nodeID,
                cls: 'paddingclass',
                allowDrag: canDrag,
                leaf: isLeaf,
                icon: nodeIcon
            });

            treeNode.on("click", function (node) {
                _openFunction(node);
            }, this);

            return treeNode;
        }

        var root1 = new Wtf.tree.AsyncTreeNode({
            text: '',
            expanded: true
        });
        var arrayList = new Array();
        var arrayJobWorkReciever = new Array();// Job Work Reciever
         var arrayListReport = new Array();//Reports

        var entryNode = _createNode('Entry', '2', false, false, 'images/Accounts_Receivable/Entry.png');
         var reportNode=_createNode('Reports', '3', false, false, 'images/Account_Payable/Reports.png');

        //Entry
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.jobWorkOrder.vendorjobworkorder"), 'MRP_JOB_WORK_ORDER_RECIVER', false, false, 'images/jobwork.png'));
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.jobWorkIn.vendorjobworkin"), 'MRP_JOB_WORK_IN_RECIVER', false, false, 'images/jobwork.png'));
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
            arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.jobworkin.qaapproval.title"), 'qaApproval', false, false, 'images/inventory/qa-approval-icon.png'));
        }
        
//        if (!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
//            arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.up.56"), 'stockrepair', false, false, 'images/inventory/stock-repair-icon.png'));
//        }
        //Out QA
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
            arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.jobworkout.qaapproval.title"), 'qaApprovalout', false, false, 'images/inventory/qa-approval-icon.png'));
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.qa, Wtf.Perm.qa.viewqa)) {
            arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.up.56"), 'stockrepair', false, false, 'images/inventory/stock-repair-icon.png'));
        }

        ////Out QA
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.productList.buildAssembly"), 'MRP_MASTER_PRODUCTMASTER', false, false, 'images/Masters/Products.png'));
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.jobWorkOut.jobworkdeliveryorder"), 'MRP_JOB_WORK_OUT_RECIVER', false, false, 'images/jobwork.png'));
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.jobwork.invoice.jobworkinvoice.title"), 'jobworkinvoice', false, false, 'images/jobwork.png'));

        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.challanreport.challanreport"), 'Challan_Report', false, false, 'images/jobwork.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.JWProductSummary.report"), 'JWPSR', false, false, 'images/jobwork.png'));
        
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.jobWorkInDetailReport"), 'Work_Order_In_Aging_Report', false, false, 'images/jobwork.png'));

        if (arrayJobWorkReciever.length != 0) {
            arrayList.push(entryNode);
        }
          if(arrayListReport.length!=0){
            arrayList.push(reportNode);    
        }


        this.setRootNode(root1);
        entryNode.appendChild(arrayJobWorkReciever);
        reportNode.appendChild(arrayListReport);
        root1.appendChild(arrayList);
    }
});
