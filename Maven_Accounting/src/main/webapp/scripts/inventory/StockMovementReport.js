function stockMovementReport(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockmovementregister)) {
        var mainTabId = Wtf.getCmp("as");
        var newTab = Wtf.getCmp("StockMovementReportTab");
        if(newTab == null){
            newTab = new Wtf.StockMovementReport({
                layout:"fit",
                title:WtfGlobal.getLocaleText("acc.lp.stockmovementregister"),
                closable:true,
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventorysmr),
                id:"StockMovementReportTab"
            });
            mainTabId.add(newTab);
        }
        mainTabId.setActiveTab(newTab);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.StockMovementReport = function(config){
    Wtf.StockMovementReport.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.StockMovementReport, Wtf.Panel, {
    initComponent: function() {
        Wtf.StockMovementReport.superclass.initComponent.call(this);
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
//                    isActive:true,   //ERP-40021 :To get all Stores.
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
             this.locCmbRecord = new Wtf.data.Record.create([
                {
                    name: 'id'
                },        
                {
                    name: 'name'
                }
                ]);

                this.locCmbStore = new Wtf.data.Store({
                    url: 'INVStore/getStoreLocations.do',
                    reader: new Wtf.data.KwlJsonReader({
                        root: 'data'
                    },this.locCmbRecord)
                });
            
              this.locCmbStore.on("beforeload", function(ds, rec, o){
                    this.locCmbStore.removeAll();
                    this.locCmb.reset(); 
                }, this);
                
            this.locCmb = new Wtf.form.ComboBox({
                fieldLabel: 'Location*',
                hiddenName: 'locationid',
                store: this.locCmbStore,
                typeAhead: true,
                displayField: 'name',
                valueField: 'id',
                mode: 'local',
                width: 125,
                triggerAction: 'all',
                emptyText: 'Select location...',
                tpl: new Wtf.XTemplate(
                        '<tpl for=".">',
                        '<div wtf:qtip = "{[values.name]}" class="x-combo-list-item">',
                        '<div>{name}</div>',
                        '</div>',
                        '</tpl>')
                        // allowBlank:false
            });
            
             this.storeCmb.on("select",function(){
                this.locCmbStore.load({
                    params:{
                        storeid:this.storeCmb.getValue()
                    }
                }) 
            },this);
            

            var storeNewRecord = new this.storeCmbRecord({
                store_id: '',
                abbrev:'',
                description:'',
                fullname: 'All'
                
            });
            
             var locationNewRecord = new this.locCmbRecord({
                id: '',
                name: 'All'
                
            });
    
            this.storeCmbStore.on("load", function(store){
                this.storeCmb.store.insert( 0,storeNewRecord);
                this.storeCmb.setValue(""); 
            },this);
            
             this.locCmbStore.on("load", function(store){
                this.locCmb.store.insert( 0,locationNewRecord);
                this.locCmb.setValue(""); 
            },this);
            
            
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
//                    if(this.storeCmb.getValue() != ""){
                        this.sDate=this.frmDate.getValue();
                        this.eDate=this.toDate.getValue();
                 
                        if(this.sDate > this.eDate){
                            WtfComMsgBox(1,2);
                            return;
                        }
                        var format = 'Y-m-d';
                        this.loadGrid(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmb.getValue(),this.locCmb.getValue());
//                    }
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
                    this.frmDate.setValue(WtfGlobal.getDates(true));
                    this.toDate.setValue(this.todateVal);
                    this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
                    this.locCmb.setValue(this.locCmb.store.data.items[0].data.id);
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
                name:"avgCost"
            },

            {
                name:"vendor"
            },
            {
                name:"orderno"
            },
            {
                name:"remark"
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
                name:'date'
            },
            {
                name:"stockTypeId"
            },
            {
                name:"stockType"
            },
            {
                name:"reusabilityCount"
            },
            {
                name:"qtyIn"
            },
            {
                name:"qtyOut"
            },
            {
                name:"batchName"
            },
            {
                name:"serialNames"
            },
            {
                name:"locationName"
            },
            {
                name:"rowName"
            },
            {
                name:"rackName"
            },
            {
                name:"binName"
            },
            {
                name:"orderuom"
            },
             {
                name:"seriExpdate"
            },
            {
                name:"memo"
            },
            {
                name:"asset"
            },
            {
                 "name": "itemasset"
            },
            {
                 "name": "serialexpdate"
            },
            {
                 "name": "country"
            }

            ]);

            this.ds = new Wtf.data.GroupingStore({
                url: 'INVGoodsTransfer/getDetailedStockMovementList.do',
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
                    toDate:this.toDate.getValue().format( WtfGlobal.getDateFormat())
                }
            });
            
            this.ds.on("load",function(){
                 WtfGlobal.resetAjaxTimeOut();
            },this);
            this.ds.on("beforeload",function(){
                var currentParams = this.ds.baseParams;
                if(this.isProductView){
                    currentParams.productId = this.productId;
                }
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
                header: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
                dataIndex: "storedescription",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add"),
                dataIndex: "locationName",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.inventorysetup.row"),
                dataIndex: "rowName",
                pdfwidth:100,
                hidden:Wtf.account.companyAccountPref.isRowCompulsory?false:true,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.inventorysetup.rack"),
                dataIndex: "rackName",
                pdfwidth:100,
                hidden:Wtf.account.companyAccountPref.isRackCompulsory?false:true,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.inventorysetup.bin"),
                dataIndex: "binName",
                pdfwidth:100,
                hidden:Wtf.account.companyAccountPref.isBinCompulsory?false:true,
                width:cmDefaultWidth

            },{
                header: WtfGlobal.getLocaleText("acc.field.TransactionDate"),
                dataIndex: "date",
                pdfwidth:100,
                width:cmDefaultWidth

            },{
                header: WtfGlobal.getLocaleText("acc.field.CostCenter"),
                dataIndex: "costcenter",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
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
                header: WtfGlobal.getLocaleText("acc.field.Remarks"),
                dataIndex: "remark",
                width:cmDefaultWidth,
                pdfwidth:100,
                renderer: function(v){
                    return "<div wtf:qtip='"+v+"'>"+v+"</div>";
                }
            },
            {
                header: "Memo",
                dataIndex: "memo",
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
                header: WtfGlobal.getLocaleText("acc.masterConfig.type"),
                dataIndex: "type",
                pdfwidth:100,
                width:cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.report.rule16register.UOM"),
                dataIndex: "orderuom",
                width:cmDefaultWidth,
                pdfwidth:50
            },{
                header: WtfGlobal.getLocaleText("acc.vppl.QuantityIN"),
                align:"right",
                dataIndex: "qtyIn",
                summaryType : 'sum',
                width:cmDefaultWidth,
                pdfwidth:50,
                renderer:function(v,m,r){
                    if(r.get('type') == "OUT"){
                        return "";
                    }else{
                        return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    }
                },
                summaryRenderer:function(v){
                    return "<b> IN : "+parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },{
                header: WtfGlobal.getLocaleText("acc.vppl.QuantityOUT"),
                align:"right",
                dataIndex: "qtyOut",
                summaryType : 'sum',
                width:cmDefaultWidth,
                pdfwidth:50,
                renderer:function(v,m,r){
                    if(r.get('type') == "OUT"){
                        return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    }else{
                        return "";
                    }
                },
                summaryRenderer:function(v){
                    return "<b>OUT : "+parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }

            },
            {
                header: WtfGlobal.getLocaleText("acc.field.lotBatch"),
//                align:"right",
                dataIndex: "batchName",
                hidden:Wtf.account.companyAccountPref.isBatchCompulsory?false:true,
                width:cmDefaultWidth,
                pdfwidth:50,
                renderer:function(v,m,r){
                    return v //WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.SerialNo"),
//                align:"right",
                dataIndex: "serialNames",
                width:cmDefaultWidth,
                pdfwidth:50,
                hidden:Wtf.account.companyAccountPref.isSerialCompulsory?false:true,
                renderer:function(val,m,r){
                    val = WtfGlobal.replaceAll(val, ",",  ", ");
                    var tipval = WtfGlobal.replaceAll(val, ",",  "<br>");
                    return "<div wtf:qtip='"+tipval+"'>"+val+"</div>"
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.Asset"),
                dataIndex: 'itemasset',
                width:cmDefaultWidth,
                sortable:false,
                hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
                pdfwidth:100,
                renderer:function(val){
                    val = WtfGlobal.replaceAll(val, ",",  ", ");
                    var tipval = WtfGlobal.replaceAll(val, ",",  "<br>");
                    return "<div wtf:qtip='"+tipval+"'>"+val+"</div>"
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.ExpiryDate"),
                dataIndex: 'serialexpdate',
                width:cmDefaultWidth,
                sortable:false,
                hidden:!Wtf.account.companyAccountPref.isSerialCompulsory,
                pdfwidth:100,
                renderer:function(val){
                    val = WtfGlobal.replaceAll(val, ",",  ", ");
                    var tipval = WtfGlobal.replaceAll(val, ",",  "<br>");
                    return "<div wtf:qtip='"+tipval+"'>"+val+"</div>"
                }
                
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),
                align:"right",
                dataIndex: "avgCost",
                width:cmDefaultWidth,
                hidden:true,
                pdfwidth:50,
                renderer:function(v,m,r){
                    return v //WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"),
                align:"right",
                dataIndex: "amount",
                summaryType : 'sum',
                width:cmDefaultWidth,
                hidden:true,
                pdfwidth:50,
                renderer:function(v,m,r){
                    return v//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                }
                ,
                summaryRenderer:function(v){
                    return "<b>"+v+"</b>"//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.stockType_ReusableOrNonReusable"),
                dataIndex: 'stockType',
                width:cmDefaultWidth,
                align:'center',
                pdfwidth:50,
                renderer:function(val, m ,r){
                    return "<div wtf:qtip='"+r.get('stockTypeId')+"'>"+val+"</div>"
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.vppl.ReusabilityCount"),
                dataIndex: 'reusabilityCount',
                width:cmDefaultWidth,
                pdfwidth:50,
                align:'center',
                hidden:Wtf.account.companyAccountPref.isSerialCompulsory?false:true,
                renderer:function(val,m,r){
                    val = WtfGlobal.replaceAll(val, ",",  ", ");
                    var tipval = WtfGlobal.replaceAll(val, ",",  "<br>");
                    return "<div wtf:qtip='"+tipval+"'>"+val+"</div>"
                }
                
            },{
                header: WtfGlobal.getLocaleText("acc.field.consignee.country"),
                dataIndex: "country",
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
                id: 'stockmovementchexport',
                hidden : this.isProductView,
                tooltip: WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"), //"Export Report details.",  
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true
                },
                get:Wtf.autoNum.StockMovementReport,
                label:"Export"

            })
            var tbarArray= new Array();

            tbarArray.push("-",WtfGlobal.getLocaleText("acc.common.from")+" : ",this.frmDate,"-",WtfGlobal.getLocaleText("acc.common.to")+": ",this.toDate,"-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+": ", this.storeCmb,"-",//"-","Transaction Type: ", this.InOutCmb,"-",
             WtfGlobal.getLocaleText("Location"),this.locCmb,"-",this.search,"-",this.resetBtn);

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
                qsWidth: this.isProductView ? 50 : 200,
                sm:this.sm,
                loadMask:true,
                searchLabel:WtfGlobal.getLocaleText("acc.het.806"),
                searchLabelSeparator:":",
                searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.SearchByProductIDProductNameSerialName"),
                serverSideSearch:true,
                searchField:"itemcode",
                view:grpView,
                tbar:tbarArray,
                bbar:bbarArray,
                plugins:[this.summary]//, this.gridSummary]
            });
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);

            this.add(this.grid);
            this.grid.doLayout();
            this.doLayout();
            this.storeCmbStore.load();
            this.locCmbStore.load();

            var format = "Y-m-d";
            this.loadGrid(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmb.getValue(),this.locCmb.getValue());
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
    
    loadGrid: function(frm, to, storeid,location,vendorid,ownershipid, itemcode){
        this.grid.getStore().baseParams = {
            fromDate:frm,
            toDate:to,
            storeid: storeid,
            locationName:location
        }
//        if(storeid != ""){
            WtfGlobal.setAjaxTimeOut();
            this.grid.getStore().load({
                params:{
                    start:0,
                    limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                    ss: this.grid.quickSearchTF.getRawValue()
                }
            });
//        }
//        else return;
        }

});
