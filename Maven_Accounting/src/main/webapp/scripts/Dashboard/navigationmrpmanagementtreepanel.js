Wtf.MRPManagement = function(config){
    this.nodeHash = {};
    this.createStores();
    Wtf.MRPManagement.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.MRPManagement, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
    border:false,
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    
    createStores: function () {
        /*
         * ERP-40324 : Load Work Order Status store while at the time of MRP Component Activation and used it in WorkOrderEntryForm.js
         */
        Wtf.WOStatusStore.load();
    },
    
    initComponent: function(){
        Wtf.MRPManagement.superclass.initComponent.call(this);
        treeObj = this;
        
        function _openFunction(node){
            switch (node.id) { 
            //Master
                case "MRP_MASTER_PRODUCTMASTER":
                    callProductDetails();
                    break;
                case "MRP_MASTER_WORKCENTERMASTER":
                    workCentreMaster(false);
                    break;
                case "MRP_MASTER_LABOURMASTER":
                    callLabourDetails();
                    break;
                case "MRP_MASTER_MACHINEMASTER":
                    callMachineMasterList();
                    break;
//                case "MRP_MACHINE_MAN_RATIO":
//                    callMachinManRatio();
//                    break;
                case "MRP_MASTER_ROUTINGMASTER":
                    callRoutingMasterList();
                    break;

            //Entry
                case "MRP_ENTRY_MASTERCONTRACT":
                    callContractMasterDetails();
                    break;
                case "MRP_ENTRY_SALESORDER":
                    callSalesOrderList(undefined,false,false,false,false,"","",true);// True to identify the MRP Order
                    break;
                case "MRP_ENTRY_SALESCONTRACT":
                    callContractOrderReport(undefined,true,true,undefined);
                    break;
                case "MRP_ENTRY_WORKORDER":
                    callMRPWorkOrderReport("MRPWorkOrderReportEntry",false);
                    break;
                case "MRP_ENTRY_WORKCENTER":
                    workcentreList();
                    break;
                case "MRP_ENTRY_JOBWORKOUT":
//                    jobOrderReportTab();
                    callPurchaseOrderList(undefined,undefined,undefined,undefined,WtfGlobal.getLocaleText("acc.accPref.autoJOBWORKOUT"),"","",true); // True to identify MRP JOB Work OUT
                    break;
                case "MRP_ENTRY_JOBWORKIN":
                      callGoodsReceiptList(null,null,false,undefined, undefined,false,false,false,true);
                      
//                    JObWorkIn();
                    break;
                case "MRP_ENTRY_MACHINE_BREAKDOWN":
                    //callMachineBreakDown();
                    break;
                case "MRP_ENTRY_MACHINE_MAINTENANCE_SCHEDULE":
                    jobOrderReportTab();
                    break;
                case "MRP_WOWithBarcodeScanner":
                    /**
                     * When user click on wo entry in manufacturing then redirect to startworkorder.jsp page
                     */
                    window.top.location.href = './startworkorder.jsp';
                    break;
                    
            //Reports
                case "MRP_REPORT_MASTERCONTRACT":
                    var isReport = true;
                    callContractMasterDetails(isReport);
                    break;
                case "MRP_REPORT_SALESORDER":                    
                    callSalesOrderList(undefined,true,false,false,false,"","",true);// True to identify the MRP Order
                    break;
                case "MRP_REPORT_SALESCONTRACT":
                    callContractOrderReport(undefined,undefined,true,true);
                    break;
                case "MRP_REPORT_WORKORDER":
                    callMRPWorkOrderReport("MRPWorkOrderReportList",true);
                    break;
                case "MRP_REPORT_WORKCENTER":
                    isReport = true;
                     workCentreMaster(isReport);
                    break;
                case "MRP_REPORT_JOBWORK":
                    isReport = true;
                    jobOrderReportTab(isReport);
                    break;
               case "MRP_TASK_PROGRESS":
                    callTaskProgressList();
                    break;     
                case "MRP_REJECTED_ITEMS":
                    reajectedItemListInMRP();
                    break;
                case "MRP_ENTRY_JOBWORK":
                    jobOrderReportTab();
                    break;
                case "MRP_QCcontrolP_ITEMS":
                    MRPQualityCOntrolReport();
                    break;
                case "MRP_FORECASTTEMPLATE":
                    callForecastDetails();
                    break;
                case "MRP_WOShortfallReport":
                    callWOShortfallReport();
                    break;      
                case "mrplabourCosting":
                    mrpLabourCosting();
                    break;  
               case "mrpWorkOrderProfitability":
                    mrpWorkOrderProfitabilityReport();
                    break;
               case "workorderstockdetailsreport":   
                    workOrderStockDetailsReport("workorderstockdetailsreport",true);
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
            
            treeNode.on("beforechildrenrendered",function(node){
                WtfModuleWiseScripts.loadScripts(Wtf.ModuleScriptGr.mrp);
            },this);
            
            return treeNode;
        }

        var root1 = new Wtf.tree.AsyncTreeNode({
            text: '',
            expanded: true
        });           
        var arrayList = new Array();
        var arrayListmaster = new Array();//Master
        var arrayListEntry = new Array();//Entry
        var arrayListReport = new Array();//Reports
        
        var masterNode=_createNode('Master', '1', false, false, 'images/chart-of-accounts.gif');
        var entryNode=_createNode('Entry', '2', false, false, 'images/Accounts_Receivable/Entry.png');
        var reportNode=_createNode('Reports', '3', false, false, 'images/Account_Payable/Reports.png');
           
        //Master
        arrayListmaster.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.productmaster"), 'MRP_MASTER_PRODUCTMASTER', false, false, 'images/Masters/Products.png'));
        arrayListmaster.push(_createNode(WtfGlobal.getLocaleText("acc.labour.labouradministration"), 'MRP_MASTER_LABOURMASTER', false, false, 'images/MRP/labourMaster.png'));
        arrayListmaster.push(_createNode(WtfGlobal.getLocaleText("acc.field.machinemaster.title"), 'MRP_MASTER_MACHINEMASTER', false, false, 'images/MRP/machineMaster.png'));
        arrayListmaster.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.workcentermaster"), 'MRP_MASTER_WORKCENTERMASTER', false, false, 'images/MRP/workCentre.png'));
//        arrayListmaster.push(_createNode(WtfGlobal.getLocaleText("acc.machineManReport.title"), 'MRP_MACHINE_MAN_RATIO', false, false, 'images/inventory/machine-master-icon.png'));
        arrayListmaster.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.field.routingmaster.title"), 'MRP_MASTER_ROUTINGMASTER', false, false, 'images/MRP/routingMaster.png'));
        
        
        //Entry
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.mastercontract.title"), 'MRP_ENTRY_MASTERCONTRACT', false, false, 'images/Account_Payable/Payment-Register.png'));
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.salesorder"), 'MRP_ENTRY_SALESORDER', false, false, 'images/Accounts_Receivable/Sale-Order.png'));
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.salescontract"), 'MRP_ENTRY_SALESCONTRACT', false, false, 'images/Statutory/iras-audit-file.png'));
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.workorder"), 'MRP_ENTRY_WORKORDER', false, false, 'images/MRP/workOrder.png'));
        /**
         * When user has permission of work order then following entry is added to manufacturing
         */
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.barcodescanner, Wtf.Perm.barcodescanner.StartWorkOrderWithBarcodeScanner)) {
            arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.wowithbarcodescanner"), 'MRP_WOWithBarcodeScanner', false, false, 'images/MRP/workOrder.png'));
        }
        arrayListEntry.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.jobwork"), 'MRP_ENTRY_JOBWORK', false, false, 'images/jobwork.png'));
       
        
        //Reports 
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.mastercontract.title")+' Registers', 'MRP_REPORT_MASTERCONTRACT', false, false, 'images/Account_Payable/Payment-Register.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.salesorder")+' Registers', 'MRP_REPORT_SALESORDER', false, false, 'images/Accounts_Receivable/Sale-Order.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.salescontract")+' Registers', 'MRP_REPORT_SALESCONTRACT', false, false, 'images/Statutory/breakup-of-gst-boxes.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.workorder")+' Registers', 'MRP_REPORT_WORKORDER', false, false, 'images/MRP/workOrder.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.workorderstockdetailsreport"), 'workorderstockdetailsreport', false, false, 'images/MRP/workOrder.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("mrp.workorder.entry.workcentre")+' Registers', 'MRP_REPORT_WORKCENTER', false, false, 'images/MRP/workCentre.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.jobwork")+' Registers', 'MRP_REPORT_JOBWORK', false, false, 'images/jobwork.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.taskProgress")+' Registers', 'MRP_TASK_PROGRESS', false, false, 'images/Masters/report-scheduler.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("mrp.rejecteditems.report.title")+' Registers', 'MRP_REJECTED_ITEMS', false, false, 'images/MRP/rejectedItemList.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("mrp.qualitycontrolreport.register.title"), 'MRP_QCcontrolP_ITEMS', false, false, 'images/Account_Payable/Payment-Register.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("mrp.woshortfallproduct.register.title"), 'MRP_WOShortfallReport', false, false, 'images/Account_Payable/Payment-Register.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("mrp.forecase.create"), 'MRP_FORECASTTEMPLATE', false, false, 'images/MRP/forecasting-icon.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("mrp.labour.costing.reporttitle"), 'mrplabourCosting', false, false, 'images/MRP/costing.png'));
        arrayListReport.push(_createNode(WtfGlobal.getLocaleText("acc.mrp.workorderprofilibilityreport"), 'mrpWorkOrderProfitability', false, false, 'images/MRP/costing.png'));
        

        if(arrayListmaster.length!=0){
            arrayList.push(masterNode);  
        }
        if(arrayListEntry.length!=0){
            arrayList.push(entryNode);  
        }
        if(arrayListReport.length!=0){
            arrayList.push(reportNode);    
        }
        
        this.setRootNode(root1);
        masterNode.appendChild(arrayListmaster);
        entryNode.appendChild(arrayListEntry);
        reportNode.appendChild(arrayListReport);
        root1.appendChild(arrayList);
    }
});
