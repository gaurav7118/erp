/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
Wtf.navigationJobWorkOutTreePanel = function(config) {
    this.nodeHash = {};
    Wtf.navigationJobWorkOutTreePanel.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.navigationJobWorkOutTreePanel, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
    border: false,
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    initComponent: function() {
        Wtf.navigationJobWorkOutTreePanel.superclass.initComponent.call(this);
        treeObj = this;

        function _openFunction(node) {
            switch (node.id) {
                //Entry
                case "Job_Work_Out_Purchase_Order":
                    var tabTitle = WtfGlobal.getLocaleText("acc.JobWorkOut.PurcahseOrder");
                    callPurchaseOrderList(undefined, undefined, undefined, undefined, tabTitle, undefined, undefined, undefined, undefined, true);
                    break;
                case "Job_Work_Out_Stock_Transfer":
                    var isJobWorkStockOut = true;
                    interStoreTransfersReport(isJobWorkStockOut);
                    break;
                case "qaApproval":
                    /*
                     * qa Approval Tab (passing isJobWorkOrder=true for JOb work Order flow)
                     */
                    var isJobWorkOrder = true;
                    var isisJobWorkOrderInQA = false;
                    qaApprovalTab(isJobWorkOrder, isisJobWorkOrderInQA);
                    break;
                case "qaApprovalout":
                    /*
                     * qa Approval Tab (passing isJobWorkOrder=true for JOb work Order flow)
                     */
                    var isJobWorkOrder = true;
                    var isisJobWorkOrderInQA = true;
                    qaApprovalTab(isJobWorkOrder, isisJobWorkOrderInQA);
                    break;
                case "Job_Work_Out_MASTER_PRODUCTMASTER":
                    callAssemblyReport(false);
                    break;
                case "Job_Work_Out_Invoice":
                    /*
                      isJobWorkoutInvoice is passed as true
                      */
                     var isJobWorkoutInvoice=true;
                    
                    callGoodsReceiptList(undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined,undefined,isJobWorkoutInvoice);   //sending job_Work_out_GRO flag as true
                    break;
                case "Work_Order_Aging_Report":
                    getJobWorkOutWithGRNTabView();  
                    break;

                case "qaApproval":
                    /*
                     * qa Approval Tab (passing isJobWorkOrder=true for JOb work Order flow)
                     */
                    var isJobWorkOrder = true;
                    qaApprovalTab(isJobWorkOrder);
                    break;
                case "stockrepair":
                    /*
                     * qa Approval Tab (passing isJobWorkOrder=true for JOb work Order flow)
                     */
                    stockRepaire();
                    break;
                case "Job_Work_Out_GRN":
                    var job_Work_Out_GRO = true;
                    callGoodsReceiptOrderList(undefined,undefined,undefined,undefined,undefined,undefined,undefined,job_Work_Out_GRO);
                    break;
                case "Job_Work_Out_payemt":
                    callPaymentReportNew();
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

            treeNode.on("click", function(node) {
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
        var reportNode = _createNode('Reports', '3', false, false, 'images/Account_Payable/Reports.png');

        //Entry
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.JobWorkOut.PurcahseOrder"), 'Job_Work_Out_Purchase_Order', false, false, 'images/jobwork.png'));
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.JobWorkOut.StockTransfer"), 'Job_Work_Out_Stock_Transfer', false, false, 'images/jobwork.png'));
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.JobWorkOut.GRN"), 'Job_Work_Out_GRN', false, false, 'images/jobwork.png'));
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.JobWorkOut.Invoice"), 'Job_Work_Out_Invoice', false, false, 'images/jobwork.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.field.jobWorkDetailReport"), 'Work_Order_Aging_Report', false, true, 'images/jobwork.png'));
        
//        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.JobWorkOut.QAApproval"), 'qaApproval', false, false, 'images/inventory/qa-approval-icon.png'));
        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.JobWorkOut.Payment"), 'Job_Work_Out_payemt', false, false, 'images/jobwork.png'));
//        arrayJobWorkReciever.push(_createNode(WtfGlobal.getLocaleText("acc.productList.buildAssembly"), 'MRP_MASTER_PRODUCTMASTER', false, false, 'images/Masters/Products.png'));

        if (arrayJobWorkReciever.length != 0) {
            arrayList.push(entryNode);
        }
        if (arrayListReport.length != 0) {
            arrayList.push(reportNode);
        }


        this.setRootNode(root1);
        entryNode.appendChild(arrayJobWorkReciever);
        reportNode.appendChild(arrayListReport);
        root1.appendChild(arrayList);
    }
});
