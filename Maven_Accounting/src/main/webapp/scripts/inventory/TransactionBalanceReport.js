function transactionBalanceReport(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.materialinoutregister)) {
        var mainTabId = Wtf.getCmp("as");
        var newTab = Wtf.getCmp("TransactionBalanceReportTab");
        if(newTab == null){
            newTab = new Wtf.TransactionBalanceReport({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.lp.materialinoutregister"),
                closable:true,
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventorymior),
                id:"TransactionBalanceReportTab"
            });
            mainTabId.add(newTab);
        }
        mainTabId.setActiveTab(newTab);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.TransactionBalanceReport = function(config){
    Wtf.TransactionBalanceReport.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.TransactionBalanceReport, Wtf.Panel, {
    initComponent: function() {
        Wtf.TransactionBalanceReport.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.TransactionBalanceReport.superclass.onRender.call(this, config);
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
        var companyDateFormat='Y-m-d'
        this.dmflag = 1;
        this.frmDate = new Wtf.form.DateField({
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
        
        this.toDate = new Wtf.form.DateField({
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
//                isActive:true,   //ERP-40021 :To get all Stores.
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
            emptyText:WtfGlobal.getLocaleText("acc.je.Selectstore"),
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        // allowBlank:false
        });
        
        var storeNewRecord = new this.storeCmbRecord({
                store_id: '',
                abbrev:'',
                description:'',
                fullname: 'All'
                
            });
            
            this.storeCmbStore.on("load", function(store){
                this.storeCmb.store.insert( 0,storeNewRecord);
                this.storeCmb.setValue(""); 
            },this);
       
        this.InOutStore = new Wtf.data.SimpleStore({
            fields:["id", "name"],
            data : [["ALL", "ALL"],["IN", "IN"],["OUT", "OUT"],["OPENING", "Opening Balance"]]
        });
        this.InOutCmb = new Wtf.form.ComboBox({
            hiddenName : 'inOutFilter',
            store : this.InOutStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 120,
            value:"ALL",
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.approval.selectTransaction")+"..."
        });      
        
        this.searchFieldStore = new Wtf.data.SimpleStore({
            fields:["id", "name"],
            data : [["ALL", "ALL"],["SERIAL", "Serial"]]
        });
        this.searchFieldSelectionCmb = new Wtf.form.ComboBox({
            hiddenName : 'inOutFilter',
            store : this.searchFieldStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 120,
            value:"ALL",
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.stockavailability.SelectFieldtoSearch")
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
                var action = 4;//getMultiMonthCheck(this.frmDate, this.toDate, 0);
              //  if(this.storeCmb.getValue() != ""){
                    switch(action) {
                        case 4:
                            var format = 'Y-m-d';
                            this.loadGrid(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmb.getValue(),this.InOutCmb.getValue(),this.searchFieldSelectionCmb.getValue());
                            break;
                        case 6:
                            multiMonthConfirmBox(45, this);
                            break;
                        default:
                            break;
                    }
               // }
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
                this.InOutCmb.setValue("ALL");
                this.searchFieldStore.setValue("ALL");
                Wtf.getCmp("Quick"+this.grid.id).setValue("");
                var format = 'Y-m-d';
                this.loadGrid(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmb.getValue(),this.InOutCmb.getValue(),this.searchFieldSelectionCmb.getValue());
            }
        });

        this.reportType = 1;
        this.detailBtn = new Wtf.Button({
            anchor : '90%',
            text: 'View Details',
            //disabled:true,
            scope:this,
            handler:function(){
                this.reportType = 2;
                this.grid. reconfigure(this.ds1, this.cm);
                this.loadGrid(this.frmDate.getValue().format(Wtf.getDateFormat()), this.toDate.getValue().format(Wtf.getDateFormat()), this.storeCmb.getValue());
                Wtf.getCmp("paggintoolbar"+this.grid.id).bind(this.ds1);
            }
        });

        this.summaryBtn = new Wtf.Button({
            anchor : '90%',
            text: 'View Summary',
            //disabled:true,
            scope:this,
            handler:function(){
                this.reportType = 1;
                this.grid. reconfigure(this.ds, this.cm);
                this.loadGrid(this.frmDate.getValue().format(Wtf.getDateFormat()), this.toDate.getValue().format(Wtf.getDateFormat()), this.storeCmb.getValue());
                Wtf.getCmp("paggintoolbar"+this.grid.id).bind(this.ds);
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
            name:"ccpartno"
        },

        {
            name:"orderuom"
        },

        {
            name:"orderquantity"
        },

        {
            name:"amount"
        },

        {
            name:"avgCost"
        },

        {
            name:"idrAmount"
        },

        {
            name:"receiptquantity"
        },

        {
            name:"soldquantity"
        },

        {
            name:"vendor"
        },
        {
            name:"assemble"
        },

        {
            name:"ownership"
        },

        {
            name:"orderingcategory"
        },

        {
            name:"usage"
        },

        {
            name:"orderno"
        },

        {
            name:"vendorinvice"
        },
        {
            name:"remark"
        },

        {
            name:'module'
        },
        {
            name:'moduleName'
        },
        {
            name:'type'
        },

        {
            name:'costcenter'
        },

        {
            name:'costcenterDescription'
        },

        {
            name:'projectnumber'
        },

        {
            name:'exchangeRate'
        },

        {
            name:'date'
        },
        {
            name:"isBatchForProduct"
        },
        {
            name:"isSerialForProduct"
        },
        {
            name:"isRowForProduct"
        },
        {
            name:"isRackForProduct"
        },
        {
            name:"isBinForProduct"
        },
        {
            name:"stockDetails"
        },
        {
            name:'store'
        },
        {
            name:'locationName'
        }
        ]);

        this.ds = new Wtf.data.GroupingStore({
            url: 'INVGoodsTransfer/getStockMovementList.do',
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
       
        this.ds1 = new Wtf.data.Store({
            url: 'INVGoodsTransfer/getStockMovementList.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: 'count'
            },
            this.record),
            sortInfo:{
                field: 'itemcode', 
                direction: "ASC"
            },
            groupField:["itemcode"]
        });
        
        WtfGlobal.setAjaxTimeOut();
        this.ds.load({
            params:{
                start:0,
                limit:10,//Wtf.companyPref.recperpage,
                frmDate: this.frmDate.getValue().format(WtfGlobal.getDateFormat()),
                toDate: this.toDate.getValue().format(WtfGlobal.getDateFormat())
            }
        });
        this.ds.on("load",function(){
            WtfGlobal.resetAjaxTimeOut();
        },this);

        this.sm= new Wtf.grid.RowSelectionModel({
            // singleSelect:true
        });
        
        this.tmplt =new Wtf.XTemplate(
            '<table cellspacing="1" cellpadding="0" style="margin-top:15px;width:100%;margin-bottom:40px;position:relative" border="0">',
            
            '<tr>',
            '<th style="padding-left:50px"><h2><b>No.</b></h2></th>',
            '<th ><h2><b>Location</b></h2></th>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // batch
            '<th><h2><b>Row</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // batch
            '<th><h2><b>Rack</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // batch
            '<th><h2><b>Bin</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
            '<th><h2><b>Batch</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  // serial 
            '<th><h2><b>Serials</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<th><h2><b>Quantity</b></h2></th>',
            '</tr>',
     
            '<tr><span  class="gridLine" style="width:94%;margin-left:45px;position: relative;top: 33px;"></span></tr>',
            
            '<tpl for="stockDetails">',
            '<tr>',
            '<td style="padding-left:50px"><p>{#}</p></td>',
            
            '<td ><p>{locationName}</p></td>',
            
            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // Row
            '<td ><p>{rowName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // Rack
            '<td ><p>{rackName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // Bin
            '<td ><p>{binName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
            '<td ><p>{batchName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  //  serial 
            '<td ><p>{serialNames}</p></td>',
            '</tpl>',
            
            '<tpl if="this.getQuantityDecimalPreciedValue(quantity)">', 
            '<td ><p>{[this.getQuantityDecimalPreciedValue(values.quantity)]}</p></td>',
            '</tpl>',
             
            '</tr>',
            '</tpl>',
            '</table>',
            {  
                isTrue: function(isSerialForProduct){
                    return isSerialForProduct;
                },
                getQuantityDecimalPreciedValue : function (v){
                    //alert(parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                    return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                }
            }
            );    
        
        this.expander = new Wtf.grid.RowExpander({
            tpl :this.tmplt,
            renderer : function(v, p, record){
                // var isBatchForProduct=record.get("isBatchForProduct");
                //  var isSerialForProduct=record.get("isSerialForProduct");
                if(record.get("stockDetails").length>0){ //means has stock detail data
                    return  '<div class="x-grid3-row-expander">&#160;</div>'
                }else{
                    // return '&#160;' 
                    return ''// '<div class="x-grid3-row-expander">&#160;</div>'
                }
            }
           
        });
        
        var cmDefaultWidth = 106;
        this.cm = new Wtf.grid.ColumnModel([
//            new Wtf.KWLRowNumberer(), //0
            this.expander, //1
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: "itemcode",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.field.ProductName"),
                dataIndex: "itemname",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.common.stores"),
                dataIndex: "store",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.common.location"),
                dataIndex: "locationName",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.stockavailability.PartNo"),
                dataIndex: "ccpartno",
                pdfwidth:100,
                hidden: true
            },{
                header: WtfGlobal.getLocaleText("acc.masterConfig.19"),
                dataIndex: "orderingcategory",
                pdfwidth:100,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.field.CostCenter"),
                dataIndex: "costcenter",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.stockavailability.CostCenterDescription"),
                dataIndex: "costcenterDescription",
                pdfwidth:100,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.stock.ProjectNo"),
                dataIndex: "projectnumber",
                pdfwidth:100,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.field.TransactionDate"),
                dataIndex: "date",
                pdfwidth:100,
                width:cmDefaultWidth
            },
            {
                header: WtfGlobal.getLocaleText("acc.vppl.TransactionModule"),
                dataIndex: "moduleName",
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Reference"),
                dataIndex: "orderno",
                pdfwidth:100,
                width:cmDefaultWidth
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.VendorInvoice"),
                dataIndex: "vendorinvice",
                pdfwidth:100,
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.Remarks"),
                dataIndex: "remark",
                width:cmDefaultWidth,
                pdfwidth:100,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>";
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.exportdetails.custven"),
                dataIndex: "vendor",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.stockavailability.AssembleProduct"),
                dataIndex: "assemble",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.coa.gridAccType"),
                dataIndex: "type",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
                dataIndex: "orderuom",
                width:cmDefaultWidth,
                pdfwidth:50,
                summaryRenderer: function(){
                    return "<b>Balance :</b>";
                }
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridQty"),
                align:"right",
                dataIndex: "orderquantity",
                summaryType : 'sum',
                width:cmDefaultWidth,
                pdfwidth:50,
                renderer:function(v,m,r){
                   return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                },
                summaryRenderer:function(v){
                   return "<b>"+parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</b>"
                }
                
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice")+" (" +WtfGlobal.getCurrencySymbol()+")",
                align:"right",
                dataIndex: "avgCost",
                width:cmDefaultWidth,
                pdfwidth:50,
                renderer:function(v,m,r){
                    return parseFloat(getRoundofValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoice.gridAmount")+" (" +WtfGlobal.getCurrencySymbol()+")",
                align:"right",
                dataIndex: "amount",
                summaryType : 'sum',
                width:cmDefaultWidth,
                pdfwidth:50,
                renderer:function(v,m,r){
                    //return v//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                    return parseFloat(getRoundofValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
                }
                ,
                summaryRenderer:function(v){
//                    return "<b>"+v.toFixed(2)+"</b>"//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                    return "<b>"+parseFloat(getRoundofValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)+"</b>"
                }
            }
            //            ,
            //            {
            //                header: "IDR Exchange Rate",
            //                align:"right",
            //                hidden:true,
            //                dataIndex: "exchangeRate",
            //                renderer:function(v,m,r){
            //                    return v//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
            //                }
            //            },{
            //                header: "Amount",
            //                align:"right",
            //                hidden:true,
            //                dataIndex: "idrAmount",
            //                summaryType : 'sum',
            //                renderer:function(v,m,r){
            //                    return v//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
            //                }
            //                ,
            //                summaryRenderer:function(v){
            //                    return "<b>"+v+"</b>"//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
            //                }
            //            }
            ]);

        this.exportButton=new Wtf.exportButton({
            obj:this,
            id:'inouttransactionexport',
            tooltip:WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"),  //"Export Report details.",  
            params:{
                name: "Material IN OUT Report"
            },
            menuItem:{
                csv:true,
                pdf:true,
                xls:true
            },
            get:Wtf.autoNum.TransactionInOutReport,
            label:"Export"
 
        })
        var tbarArray= new Array();

        tbarArray.push("-",WtfGlobal.getLocaleText("acc.stockavailability.SearchOnField")+": ",this.searchFieldSelectionCmb,"-",WtfGlobal.getLocaleText("acc.common.from")+": ",this.frmDate,"-",WtfGlobal.getLocaleText("acc.common.to")+": ",this.toDate,"-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+": ", this.storeCmb,"-",WtfGlobal.getLocaleText("acc.CNDNList.expand.Type")+": ", this.InOutCmb,"-",
            this.search,"-",this.resetBtn);
                
        var bbarArray = [this.exportButton]
        this.summary = new Wtf.grid.GroupSummary();        
        this.gridSummary = new Wtf.grid.GridSummary();
        var grpView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: true,
            enableGroupingMenu: true
//            hideGroupedColumn: true
        });
        this.grid=new Wtf.KwlEditorGridPanel({
            cm:this.cm,
            store:this.ds,
            displayInfo:true,
            qsWidth:200,
            sm:this.sm,
            loadMask:true,
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchLabelSeparator:":",
            searchEmptyText:WtfGlobal.getLocaleText("acc.field.QuickSearchStockReport"),
            serverSideSearch:true,
            searchField:"itemcode",
            view:grpView,
            tbar:tbarArray,
            bbar:bbarArray,
            displayInfo:false,
            plugins:[this.expander,this.summary]//, this.gridSummary]
        });
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=10
        },this);
        this.grid.pageSize = 10;

        this.add(this.grid);
        
        this.storeCmbStore.load();
       
        var monthlyreportcheck=false
        var action = (monthlyreportcheck)? getMultiMonthCheck(this.frmDate, this.toDate, 0) : 4;
        switch(action) {
            case 4:
                var format = "Y-m-d H:i:s";
                this.loadGrid(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmb.getValue(),this.InOutCmb.getValue(),this.searchFieldSelectionCmb.getValue());
                break;
            case 6:
                multiMonthConfirmBox(45, this);
                break;
            default:
                break;
        }
    
    },

    loadGrid: function(frm, to, storeid,transactionType,searchField){
        this.grid.getStore().baseParams = {
            fromDate:frm,
            toDate:to,
            storeid: storeid,
            transactionType:transactionType,
            fieldToSearch : searchField
        }
       // if(storeid != ""){
            WtfGlobal.setAjaxTimeOut();
            this.grid.getStore().load({
                params:{
                    start:0,
                    limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                    ss: this.grid.quickSearchTF.getRawValue()
                }
            });
       // }
       // else return;
    }

});
