function StockSummaryReport(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockmovementregister)) {
        var mainTabId = Wtf.getCmp("as");
        var newTab = Wtf.getCmp("StockSummaryReportTab");
        if(newTab == null){
            newTab = new Wtf.StockSummaryReport({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.lp.stocksummaryreport"),
                closable:true,
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventorysmr),
                id:"StockSummaryReportTab"
            });
            mainTabId.add(newTab);
        }
        mainTabId.setActiveTab(newTab);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.StockSummaryReport = function(config){
    Wtf.StockSummaryReport.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.StockSummaryReport, Wtf.Panel, {
    initComponent: function() {
        Wtf.StockSummaryReport.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.TransactionBalanceReport.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
        //            params: {
        //                mode: 111,
        //                masterid: masterid,
        //                isShowCustColumn: true
        //            }
        }, this, function (request, response) {
            var customProductField = request.data;
            this.fromdateVal = new Date().getFirstDateOfMonth();
            this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
            var companyDateFormat='Y-m-d'
            this.dmflag = 1;
            this.frmDate = new Wtf.ExDateFieldQtip({
                emptyText:'From date...',
                readOnly:true,
                width : 100,
                value:WtfGlobal.getDates(true),
                name : 'frmdate',
                minValue: Wtf.archivalDate,
                format: companyDateFormat//Wtf.getDateFormat()
            });

            this.todateVal=new Date().getLastDateOfMonth();
            this.todateVal.setDate(new Date().getLastDateOfMonth().getDate());

            this.toDate = new Wtf.ExDateFieldQtip({
                emptyText:'To date...',
                readOnly:true,
                width : 100,
                minValue: Wtf.archivalDate,
                name : 'todate',
                value:WtfGlobal.getDates(false),
                format: companyDateFormat//Wtf.getDateFormat()
            });

            this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },

            {
                name: 'abbrev'
            },

            {
                name: 'description'
            },

            {
                name: 'fullname'
            }
            ]);

            this.storeCmbStore = new Wtf.data.Store({
                url:  'INVStore/getStoreList.do',
                baseParams:{
//                    isActive:true,    //ERP-40021 :To get all Stores.
                    byStoreExecutive:"true",
                    byStoreManager:"true",
                    includeQAAndRepairStore:true,
                    includePickandPackStore:true
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                },this.storeCmbRecord)
            });


            this.storeCmb = new Wtf.form.ComboBox({
                hiddenName : 'storeid',
                store : this.storeCmbStore,
                typeAhead:true,
                displayField:'fullname',
                valueField:'store_id',
                mode: 'local',
                width : 120,
                triggerAction: 'all',
                emptyText:'Select Store...',
                listWidth:300,
                tpl: new Wtf.XTemplate(
                    '<tpl for=".">',
                    '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                    '<div>{fullname}</div>',
                    '</div>',
                    '</tpl>')
            });

//            var storeNewRecord = new this.storeCmbRecord({
//                store_id: '',
//                abbrev:'',
//                description:'',
//                fullname: 'All'
//                
//            });
//    
//            this.storeCmbStore.on("load", function(store){
//                this.storeCmb.store.insert( 0,storeNewRecord);
//                this.storeCmb.setValue(""); 
//            },this);
            
            this.InOutStore = new Wtf.data.SimpleStore({
                fields:["id", "name"],
                data : [["ALL", "ALL"],["IN", "IN"],["OUT", "OUT"]]
            });
            this.InOutCmb = new Wtf.form.ComboBox({
                hiddenName : 'inOutFilter',
                store : this.InOutStore,
                typeAhead:true,
                displayField:'name',
                valueField:'id',
                mode: 'local',
                width : 120,
                triggerAction: 'all',
                emptyText:WtfGlobal.getLocaleText("acc.approval.selectTransaction")+"..."
            });

            this.search = new Wtf.Button({
                anchor: '90%',
                text: WtfGlobal.getLocaleText("acc.common.search"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
                },
                iconCls: 'accountingbase fetch',
                scope: this,
                handler: function() {
                    if(this.storeCmb.getValue() != ""){
                        this.sDate=this.frmDate.getValue();
                        this.eDate=this.toDate.getValue();
                 
                        if(this.sDate > this.eDate){
                            WtfComMsgBox(1,2);
                            return;
                        }
                        var format = 'Y-m-d';
                        this.loadGrid(WtfGlobal.convertToGenericEndDate(this.frmDate.getValue()),  WtfGlobal.convertToGenericEndDate(this.toDate.getValue()), this.storeCmb.getValue());
                    }
                }
            });

            this.resetBtn = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.stock.ClicktoResetFilter")
                },
                iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                scope:this,
                handler:function(){
                    this.frmDate.setValue(this.fromdateVal);
                    this.toDate.setValue(this.todateVal);
                    this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
                    this.vendorCmb.setValue("");
                    this.ownershipCmb.setValue("");
                }
            });

            ///////////////////////////////////////////////////
            this.record = Wtf.data.Record.create([
            {
                name:"storecode"
            },
            {
                name:"storedescription"
            },
            {
                name:"itemcode"
            },
            {
                name:"itemdescription"
            },
            {
                name:"itemname"  
            },
            {
                name:"amount"
            },
            {
                name:"avgcost"
            },
            {
                name:"uom"
            },
            {
                name:"openingqty"
            },
            {
                name:"goodsreceiptorderqty"
            },
            {
                name:"deliveryorderqty"
            },
            {
                name:"stocktransferINqty"
            },
            {
                name:"stocktransferOUTqty"
            },
            {
                name:"stockadjustmentqty"
            },
            {
                name:"balanceqty"
            },
            {
                name:"cyclecountqty"
            },
            {
                name:"varianceqty"
            },
            ]);

            this.ds = new Wtf.data.GroupingStore({
                url: 'ACCProductCMN/getStockSummaryReport.do',
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data',
                    totalProperty:'count'
                },
                this.record),
                sortInfo:{
                    field: 'itemcode',
                    direction: "ASC"
                },
                groupField:['itemcode']
            });
            this.updateStoreConfig(customProductField);
            
            WtfGlobal.setAjaxTimeOutFor30Minutes();
            this.ds.load({
                params:{
                    start:0,
                    limit:30,//Wtf.companyPref.recperpage,
                    frmDate:this.frmDate.getValue().format( WtfGlobal.getDateFormat()),
                    toDate:this.toDate.getValue().format( WtfGlobal.getDateFormat()),
                    enddate:this.toDate.getValue().format( WtfGlobal.getDateFormat())
                    
                }
            });
            
            this.ds.on("load",function(){
                 WtfGlobal.resetAjaxTimeOut();
            },this);
            
            this.sm= new Wtf.grid.RowSelectionModel({
                });

            var cmDefaultWidth = 106;
            var colArr = [
            new Wtf.KWLRowNumberer(), //0
            {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.productID"),
                dataIndex: "itemcode",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.productname"),
                dataIndex: "itemname",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.machinemaster.header.2"),
                dataIndex: "itemdescription",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
                dataIndex: "uom",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: "Opening Qty",
                dataIndex: "openingqty",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.field.GoodsReceiptOrder"),
                dataIndex: "goodsreceiptorderqty",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.field.deliveryOrder(s)"),
                dataIndex: "deliveryorderqty",
                pdfwidth:100,
                width:cmDefaultWidth

            },{
                header: WtfGlobal.getLocaleText("acc.invReport.stockIN"),
                dataIndex: "stocktransferINqty",
                pdfwidth:100,
                width:cmDefaultWidth

            },{
                header: WtfGlobal.getLocaleText("acc.invReport.stockOUT"),
                dataIndex: "stocktransferOUTqty",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustment"),
                dataIndex: "stockadjustmentqty",
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.productList.gridBalQty"),
                dataIndex: "balanceqty",
                pdfwidth:100,
                width:cmDefaultWidth
            },
            {
                header: WtfGlobal.getLocaleText("acc.lp.cyclecount"),
                dataIndex: "cyclecountqty",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.field.Variance"),
                dataIndex: "varianceqty",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.lp.stocksummaryreportunitcost")+"("+WtfGlobal.getCurrencyName()+")",
                dataIndex: "avgcost",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.Amount")+"("+WtfGlobal.getCurrencyName()+")",
                dataIndex: "amount",
                pdfwidth:100,
                width:cmDefaultWidth
            }
            ];
            if(customProductField && customProductField.length>0) {
                for(var ccnt=0; ccnt<customProductField.length; ccnt++) {
                    colArr.push({
                        header : customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: cmDefaultWidth,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }
            this.cm = new Wtf.grid.ColumnModel(colArr);
            this.exportButton = new Wtf.exportButton({
                obj: this,
                id: 'stocksummaryexport',
                tooltip: WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"), //"Export Report details.",  
                menuItem:{
                    csv:true,
                    xls:true
                },
                params:{
                    enddate :  this.toDate.getValue().format( WtfGlobal.getDateFormat())
                },
                get:Wtf.autoNum.StockSummaryReport,
                label:"Export"

            })
            var tbarArray= new Array();

            tbarArray.push("-",WtfGlobal.getLocaleText("acc.common.from")+" : ",this.frmDate,"-",WtfGlobal.getLocaleText("acc.common.to")+": ",this.toDate,"-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+": ", this.storeCmb,"-",//"-","Transaction Type: ", this.InOutCmb,"-",
                this.search,"-",this.resetBtn);

            var bbarArray = [this.exportButton]
            this.summary = new Wtf.grid.GroupSummary();
            this.gridSummary = new Wtf.grid.GridSummary();
            var grpView = new Wtf.grid.GroupingView({
                forceFit: false,
                showGroupName: true,
                enableGroupingMenu: true,
                hideGroupedColumn: false
            });
            this.grid=new Wtf.KwlEditorGridPanel({
                cm:this.cm,
                store:this.ds,
                displayInfo:true,
                qsWidth:200,
                sm:this.sm,
                loadMask:true,
                searchLabel:WtfGlobal.getLocaleText("acc.het.806"),
                searchLabelSeparator:":",
                searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.SearchByProductIDProductNameSerialName"),
                serverSideSearch:true,
                searchField:"itemcode",
//                view:grpView,
                tbar:tbarArray,
                bbar:bbarArray
//                plugins:[this.summary]//, this.gridSummary]
            });
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);

            this.add(this.grid);
            this.grid.doLayout();
            this.doLayout();
            this.storeCmbStore.load();

            var format = "Y-m-d";
            this.loadGrid(WtfGlobal.convertToGenericEndDate(this.frmDate.getValue()),  WtfGlobal.convertToGenericEndDate(this.toDate.getValue()), this.storeCmb.getValue());
        }, function () {

            });

    },
    
    updateStoreConfig : function(customProductField) {
        for (var cnt = 0; cnt < customProductField.length; cnt++) {
            var fieldname = customProductField[cnt].dataindex;
            var newField = new Wtf.data.Field({
                name: fieldname
            });
            this.ds.fields.items.push(newField);
            this.ds.fields.map[fieldname] = newField;
            this.ds.fields.keys.push(fieldname);
        }
        this.ds.reader = new Wtf.data.KwlJsonReader(this.ds.reader.meta, this.ds.fields.items);
    },
    
    loadGrid: function(frm, to, storeid,vendorid,ownershipid, itemcode){
        this.grid.getStore().baseParams = {
            fromDate:frm,
            toDate:to,
            storeid: storeid
        }
        if(storeid != ""){
            WtfGlobal.setAjaxTimeOut();
            this.grid.getStore().load({
                params:{
                    start:0,
                    limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                    ss: this.grid.quickSearchTF.getRawValue()
                }
            });
        }
        else return;
        }

});
