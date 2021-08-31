Wtf.Reports = function(config){
    this.nodeHash = {};
    
    Wtf.Reports.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.Reports, Wtf.tree.TreePanel, {
    autoWidth: true,
    autoHeight: true,
    rootVisible: false,
    border:false,
    autoScroll: true,
    animate: Wtf.enableFx,
    enableDD: false,
    hlDrop: Wtf.enableFx,
    
    
    initComponent: function(){
        Wtf.Reports.superclass.initComponent.call(this);
        treeObj = this;
        function _openFunction(node){
            switch (node.id) {
                case "01":
                    loadReportPerm();
                    break;
                case "02":
                    callWidgetTab();
                    break;
                case "03":
                    loadCreateCustomReportPerm();
                    break;
                case "04":
                    callCustomReportGrid();
                    break;
                case "05":
                    callsetExchangeRateWindow();
                    break;
                case "06":
                    callProductExportDetails();
                    break;
                case "07":
                    callDemoTransactionsImportWin();
                    break;
                case "08":
                    loadCustomWidgetReportList();
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
        
        arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.ReportList"), '01', false, true, 'images/Reports/report-list.png'));
        arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.dashboard.widgets"), '02', false, true, 'images/Reports/widgests.png'));
        arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.CreateCustomReportList"), '03', false, true, 'images/Reports/custom-reports.png'));
        arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomReports"), '04', false, true, 'images/Reports/saved-search-report.png'));
        
//        if (!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.viewclayout)) {
//            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.CustomLayouts"), '08', false, true, 'images/Reports/custom-layout.png'));
//        }
        
        if(Wtf.SetExchageRate_For_MaleshianCompany){
            arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.SetExchangeRate"), '05', false, true, 'images/Reports/widgests.png'));
        }
        arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.exportLog"), '06', false, true, 'images/Reports/export-details-reports.png'));
        arrayList.push(_createNode(WtfGlobal.getLocaleText("acc.field.ImportJustCommodityTransactions"), '07', false, true, 'images/Reports/import-just-commodity-transactions.png'));
        arrayList.push(_createNode("Custom Widget Reports", '08', false, true, 'images/Reports/saved-search-report.png'));
        
        this.setRootNode(root1);
        root1.appendChild(arrayList);
    }
});