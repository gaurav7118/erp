function itemStockLevelByBatchWise(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.batchwisestocktracking)) {
        var inventoryTab = Wtf.getCmp("batchwiseInventoryLevel");
        if(inventoryTab == null){
            inventoryTab = new Wtf.BatchWiseInventoryLevel({
                title:WtfGlobal.getLocaleText("acc.inventoryList.BatchwiseStockTracking"),
                id:"batchwiseInventoryLevel",
                layout:"fit",
                iconCls:getButtonIconCls(Wtf.etype.inventorybst),
                closable:true
            });
            Wtf.getCmp("as").add(inventoryTab);
        }
        Wtf.getCmp("as").setActiveTab(inventoryTab);
        Wtf.getCmp("as").doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}
function itemBatchStockLevelByDate(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.batchwisestocktracking)) {
        var inventoryTab = Wtf.getCmp("datewisebatchInventoryLevel");
        if(inventoryTab == null){
            inventoryTab = new Wtf.DateWiseBatchInventoryLevel({
                title:WtfGlobal.getLocaleText("acc.inventoryList.DatewiseBatchStockTracking"),
                id:"datewisebatchInventoryLevel",
                layout:"fit",
                iconCls:getButtonIconCls(Wtf.etype.inventorydbst),
                closable:true
            });
            Wtf.getCmp("as").add(inventoryTab);
        }
        Wtf.getCmp("as").setActiveTab(inventoryTab);
        Wtf.getCmp("as").doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

function itemStockLevelByStore(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
        var inventoryTab1 = Wtf.getCmp("storewiseInventoryLevel");  
        if(inventoryTab1 == null){
            inventoryTab1 = new Wtf.StoreWiseInventoryLevel({
                title:WtfGlobal.getLocaleText("acc.lp.stockavailabilitybywarehouse"),
                id:"storewiseInventoryLevel",
                layout:"fit",
                iconCls:getButtonIconCls(Wtf.etype.inventorysarbw),
                closable:true
            });
            Wtf.getCmp("as").add(inventoryTab1);
        }   
        Wtf.getCmp("as").setActiveTab(inventoryTab1);
       Wtf.getCmp("as").doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

function itemStockLevelByStoreSummary(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
        var inventoryTab1 = Wtf.getCmp("storewiseInventoryLevelSummary");  
        if(inventoryTab1 == null){
            inventoryTab1 = new Wtf.StoreWiseInventoryLevelSummary({
                title:WtfGlobal.getLocaleText("acc.lp.stockavailabilitybywarehousesummary"),
                id:"storewiseInventoryLevelSummary",
                layout:"fit",
                iconCls:getButtonIconCls(Wtf.etype.inventorysarbw),
                closable:true
            });
            Wtf.getCmp("as").add(inventoryTab1);
        }   
        Wtf.getCmp("as").setActiveTab(inventoryTab1);
        Wtf.getCmp("as").doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

function itemStockLevelBySystemandCustomer(){
    
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
        if(Wtf.account.companyAccountPref.SKUFieldParm){
            var inventoryTab1 = Wtf.getCmp("AssetDetailsreport");  
            if(inventoryTab1 == null){
                inventoryTab1 = new Wtf.AssetDetialTabs({
                    title:"Asset Details Report",
                    id:"AssetDetailsreport",
                    layout:"fit",
                    iconCls:getButtonIconCls(Wtf.etype.inventorysarbw),
                    closable:true
                });
                Wtf.getCmp("as").add(inventoryTab1);
            }   
            Wtf.getCmp("as").setActiveTab(inventoryTab1);
            Wtf.getCmp("as").doLayout();
        }else{
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.common.enableSkuwarnMSG")],0);
        }
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
    
}
function itemStockLevelByDate(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.stockavailabilitybywarehouse)) {
        var inventoryTab1 = Wtf.getCmp("datewiseInventoryLevel");  
        if(inventoryTab1 == null){
            inventoryTab1 = new Wtf.DateWiseInventoryLevel({
                title:WtfGlobal.getLocaleText("acc.inventoryList.DatewiseStockTracking"),
                id:"datewiseInventoryLevel",
                layout:"fit",
                iconCls:getButtonIconCls(Wtf.etype.inventorydst),
                closable:true
            });
            Wtf.getCmp("as").add(inventoryTab1);
        }   
        Wtf.getCmp("as").setActiveTab(inventoryTab1);
        Wtf.getCmp("as").doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}
function updateSerialNames(selectedRec){
    var reportPanel = Wtf.getCmp('updateSerialnames'+selectedRec[0].data.itemid);
    if(reportPanel == null){
        reportPanel = new Wtf.SerialNoWindow({
            id : 'updateSerialnames'+selectedRec[0].data.itemid,
            border : false,
            title: WtfGlobal.getLocaleText("acc.product.serialupdate")+" - "+selectedRec[0].data.itemcode,  //"Receipt Type",
            selectedRec:selectedRec,
            tabTip: WtfGlobal.getLocaleText("acc.product.serialupdate")+" - "+selectedRec[0].data.itemcode,
            layout: 'fit',
            closable : true
        //            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}
Wtf.AssetDetialTabs = function (config){
    Wtf.apply(this,config);
    this.isJobWorkOrder= config.isJobWorkOrder;
    this.isisJobWorkOrderInQA=config.isisJobWorkOrderInQA;
    Wtf.AssetDetialTabs.superclass.constructor.call(this);
}
Wtf.extend(Wtf.AssetDetialTabs,Wtf.Panel,{
    onRender:function (config) {
        Wtf.AssetDetialTabs.superclass.onRender.call(this,config);
        this.getTabpanel();
        this.add(this.tabPanel);
    },
    getTabpanel:function (){
        this.getAseetDetailTab();
//        this.getStockDetailsTab();        
        this.itemsarr = [];       
        this.itemsarr.push(this.assetDtl);
//        this.itemsarr.push(this.stockDtl);

        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            id:"assetdetail",
            items:this.itemsarr
        });
    },
    getAseetDetailTab:function(){
        this.assetDtl =new Wtf.AssetDetailsreport({
//            id:this.isJobWorkOrder?"StockoutQACmpjobworkorder" + 0:"StockoutQACmp"+0,
            layout:'fit',
            title:"Asset Details",
            status:"PENDING",
            iconCls:getButtonIconCls(Wtf.etype.inventoryAllStock),
            border:false,
            type:1
        });
    },
    getStockDetailsTab:function (){
        this.stockDtl =new Wtf.AssetDetailsreport({
//            id:this.isJobWorkOrder?"StockoutQACmpjobworkorder" + 3:"StockoutQACmp"+3,
            layout:'fit',
            title:"Stock Details",
            iconCls:getButtonIconCls(Wtf.etype.inventoryAllStock),
            status:"DONE",
            border:false,
            type:2
        });
    }
});


Wtf.BatchWiseInventoryLevel = function(config){
    Wtf.BatchWiseInventoryLevel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.BatchWiseInventoryLevel, Wtf.Panel, {
    initComponent: function() {
        Wtf.BatchWiseInventoryLevel.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.BatchWiseInventoryLevel.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
        //            params: {
        //                mode: 111,
        //                masterid: masterid,
        //                isShowCustColumn: true
        //            }
        }, this, function (request, response) {
            var customProductField = request.data;
            this.status = 0;
            this.searchJson="";

            this.record = Wtf.data.Record.create([
            {
                "name":"storecode"
            },

            {
                "name":"storedescription"
            },
            {
                "name":"itemid"
            },
            {
                "name":"itemname"
            },
            {
                "name":"itemdescription"
            },
            {
                "name":"itemcode"
            },
            {
                "name":"ccpartnumber"
            },
            {
                "name":"location"
            },
            {
                "name":"rowName"
            },
            {
                "name":"rackName"
            },
            {
                "name":"binName"
            },
            {
                "name":"quantity"
            },
            {
                "name":"batchPricePerUnit"
            },
            {
                "name":"batchName"
            },
            {
                "name":"serials"
            },
            {
                "name":"batchQty"
            },
            {
                "name":"uom"
            },
            {
                "name":"stockType"
            },
            {
                "name":"stockTypeName"
            },
            {
                 "name": "itemasset"
            },
            {
                 "name": "serialexpdate"
            },
            {
              "name" : "batchExpdate"  
            },
            {
              "name" : "isBatchForProduct"  
            },
            {
              "name" : "isSerialForProduct"  
            }
            ]);
            var grpView = new Wtf.grid.GroupingView({
                forceFit: true, 
                showGroupName: true,
                enableGroupingMenu: true,
                hideGroupedColumn: false
            });
            /*************************/

            this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },

            {
                name: 'abbr'
            },
            {
                name: "fullname"
            },
            {
                name: 'description'
            }
            ]);

            this.storeCmbStore = new Wtf.data.Store({
                url:'INVStore/getStoreList.do',
                baseParams:{
                    isActive :true,
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
                fieldLabel : 'Store*',
                hiddenName : 'storeid',
                store : this.storeCmbStore,
                typeAhead:true,
                displayField:'fullname',
                valueField:'store_id',
                mode: 'local',
                width : 125,
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
            var trackStoreLocation=true
            if(trackStoreLocation){
            
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
                    baseParams: {
                        allLoc: true
                    },
                    reader: new Wtf.data.KwlJsonReader({
                        root: 'data'
                    }, this.locCmbRecord)
                });
            
                this.locCmbStore.on("beforeload", function(ds, rec, o){
                    this.locCmbStore.removeAll();
                    this.locCmb.reset(); 
                }, this);
                this.locCmb = new Wtf.form.ComboBox({
                    fieldLabel : 'Location*',
                    hiddenName : 'locationid',
                    store : this.locCmbStore,
                    typeAhead:true,
                    displayField:'name',
                    valueField:'id',
                    mode: 'local',
                    width : 125,
                    triggerAction: 'all',
                    emptyText:'Select location...'
                // allowBlank:false
                });
            }          
               
            this.resetBtn = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
                },
                iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                scope:this,
                handler:function(){
                    this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
                    if(trackStoreLocation){
                        // this.locCmb.setValue(this.locCmb.store.data.items[0].data.id);
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                
                }
            });

            this.search = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.common.search"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
                },
                iconCls : 'accountingbase fetch',
                scope:this,
                handler:function(){
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }   
                }
            });

            this.CreatePOButton= new Wtf.Button({
                text: "Create PO",
                scope: this,
                tooltip: {
                    title:"Create PO", 
                    text:"Create Purchase Order for Selected Item"
                },
                handler: function(){
                    var gSm = this.grid.getSelectionModel();
                    if(gSm.getSelections().length==1){
                        var rec = gSm.getSelected();
                        createPOforItemInvLevel(rec.data.itemid, this.storeCmb.getValue(), rec.data.vendorId);
                    } else {
                        Wtf.MessageBox.show({
                            title:"Create PO",
                            msg:"Please select a item to create Purchase Order.",
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK
                        });
                    }
                },
                iconCls: "pwnd cloneicon caltb"
            });
        
            /***************************/


            this.ds = new Wtf.data.GroupingStore({
                sortInfo: {
                    field: 'itemcode',
                    direction: "ASC"
                },
                groupField:"itemcode",
                url: 'INVStockLevel/getBachWiseStockInventory.do',//
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data',
                    totalProperty:'count'
                },
                this.record)
           
            });
            this.updateStoreConfig(customProductField);
           
            if(trackStoreLocation){       
                this.locCmbStore.on("load", function(ds, rec, o){
                    //                if(rec.length > 0){
                    var newRec=new this.storeCmbRecord({
                        id:'',
                        name:'ALL'
                    })
                    this.locCmbStore.insert(0,newRec);
                    this.locCmb.setValue('');
                //                }
                }, this);
            }

            this.storeCmbStore.on("load", function(ds, rec, o){
                if(rec.length > 0){
                    var newRec=new this.storeCmbRecord({
                        id:'ALL',
                        fullname:'ALL'
                    })
                    this.storeCmbStore.insert(0,newRec);
                    this.storeCmb.setValue('ALL');
                    if(rec.length==1){
                        this.storeCmb.setValue(rec[0].data.store_id);
                    }
                    this.storeCmb.fireEvent('select');
                    if(trackStoreLocation){                 
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                    //this.initloadgridstore(this.storeCmb.getValue());
                    }
                }
            }, this);
            this.storeCmbStore.load();
        
            this.storeCmb.on("select",function(){
                this.locCmbStore.load({
                    params:{
                        storeid:this.storeCmb.getValue()
                    }
                }) 
            },this);
            var sm= new Wtf.grid.CheckboxSelectionModel({
                // singleSelect:true
                });
        
            var integrationFeatureFor=true;
            var cmDefaultWidth = 200;
            var colArr = [
            //sm
            new Wtf.KWLRowNumberer(),
            //this.rowExpander,
           
            
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: 'itemcode',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.ProductName"),
                dataIndex: 'itemname',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridDescription"),
                dataIndex: 'itemdescription',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex: 'storecode',
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
                dataIndex: 'storedescription',
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add"),
                dataIndex: 'location',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inv.loclevel.3"),
                dataIndex: 'rowName',
                width:cmDefaultWidth,
                hidden:Wtf.account.companyAccountPref.isRowCompulsory?false:true,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inv.loclevel.4"),
                dataIndex: 'rackName',
                width:cmDefaultWidth,
                hidden:Wtf.account.companyAccountPref.isRackCompulsory?false:true,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inv.loclevel.5"),
                dataIndex: 'binName',
                width:cmDefaultWidth,
                hidden:Wtf.account.companyAccountPref.isBinCompulsory?false:true,
                pdfwidth:100,
                summaryRenderer:function(v){
                    return "<div style = 'float:right;'><b>Balance :</b></div>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.gridQty"),
                dataIndex: 'quantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                summaryType : 'sum',
                pdfwidth:50,
                renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                },
                summaryRenderer:function(v){
                    return "<b>"+parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
                dataIndex: 'uom',
                width:cmDefaultWidth,
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.lotBatch"),
                dataIndex: 'batchName',
                width:cmDefaultWidth,
                sortable:false,
                hidden:!Wtf.account.companyAccountPref.isBatchCompulsory,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.Serials"),
                dataIndex: 'serials',
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
                hidden:!(Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isBatchCompulsory),
                pdfwidth:100,
                renderer:function(val){
                    val = WtfGlobal.replaceAll(val, ",",  ", ");
                    var tipval = WtfGlobal.replaceAll(val, ",",  "<br>");
                    return "<div wtf:qtip='"+tipval+"'>"+val+"</div>"
                }
                
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.stockType_ReusableOrNonReusable"),
                dataIndex: 'stockTypeName',
                width:cmDefaultWidth,
                align:'center',
                pdfwidth:30,
                renderer:function(val, m ,r){
                    if(r.get('stockType') == "REUSABLE"){
                        return "<div wtf:qtip='Reusable'>"+val+"</div>"
                    }else if(r.get('stockType') == "DISPOSABLE"){
                        return "<div wtf:qtip='Consumable'>"+val+"</div>"
                    }else {
                        return "<div wtf:qtip='Consumable'>"+val+"</div>"
                    }
                }
            }
            ];
            if (customProductField && customProductField.length > 0) {
                for (var ccnt = 0; ccnt < customProductField.length; ccnt++) {
                    colArr.push({
                        header: customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: cmDefaultWidth,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }    
            this.cm = new Wtf.grid.ColumnModel(colArr);    
            this.objsearchComponent=new Wtf.advancedSearchComponent({
                cm:this.cm
            });

            this.AdvanceSearchBtn = new Wtf.Button({
                text : "Advanced Search",
                scope : this,
                tooltip:'Manage search with your preference',
                handler : this.configurAdvancedSearch,
                iconCls :'accountingbase fetch'
            });
            this.exportButton=new Wtf.exportButton({
                obj:this,
                id:'batchwisestockexport',
                tooltip:"Export Report",  //"Export Report details.",  
                params:{
                    name: "Batchwise Stock Tracking"
                },
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true
                },
                get:Wtf.autoNum.BatchwiseStockTrackingReport,
                label:"Export"
            })
            var tbararr = new Array();

            tbararr.push("-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":", this.storeCmb);
            tbararr.push("-",WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add")+":", this.locCmb);
            tbararr.push("-",this.search);
            tbararr.push("-",this.resetBtn);
            
            this.summaryBtn = new Wtf.Button({
                anchor : '90%',
                text: 'View Summary',
                scope:this,
                hidden: (true == 5 || true == 17 || true == 18),
                handler:function(){
                    this.storeCmb.setValue("");
                    if(this.status==0) {
                        this.storeCmb.disable();
                        this.cm.setHidden(2, true);
                        this.summaryBtn.setText("View Details");
                        this.status = 1;
                    } else {
                        this.status = 0;
                        this.storeCmb.enable();
                        this.cm.setHidden(2, false);
                        this.summaryBtn.setText("View Summary");
                    }
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                }
            });
            this.summary = new Wtf.grid.GroupSummary({});
            this.grid=new  Wtf.KwlEditorGridPanel({
                id:"inventoryList",
                region:'center',
                cm:this.cm,
                store:this.ds,
                sm:sm,
                loadMask:true,
                viewConfig: {
                    forceFit: false
                },
                view: grpView,
                autoscroll:true,
                plugins : [this.summary],
                searchLabel:WtfGlobal.getLocaleText("acc.het.1001"),
                displayInfo:true,
                searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.EnterProductIDnamebatchserial"),
                serverSideSearch:true,
                //advanceSearch:true,
                tbar:tbararr,
                bbar:[this.exportButton]
            });

            //If you want to enable button ,if only one record selected ,otherwise disable
            //        var arrId=new Array();
            //        arrId.push("delete");//"deleteIssueBtn" id of button
            //        arrId.push("edit");
            //
            //        enableDisableButton(arrId,this.ds,sm);
            this.innerPanel = new Wtf.Panel({
                layout : 'border',
                bodyStyle :"background-color:transparent;",
                border:false,
                items : [this.grid,this.objsearchComponent]

            });
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);
            this.add(this.innerPanel);
            this.doLayout();
            this.objsearchComponent.advGrid.on("filterStore",this.filterStore, this);
            this.objsearchComponent.advGrid.on("clearStoreFilter",this.clearStoreFilter, this);
        }, function () {

            });
    //        if(trackStoreLocation){            
    //            this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
    //        }else{
    //            this.initloadgridstore(this.storeCmb.getValue());
    //        }    
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
    initloadgridstore:function(storetype,locationtype){

        this.ds.baseParams = {
            store:storetype,
            location:locationtype,
            //summaryFlag: this.status == 1,
            searchJson:this.searchJson
        }
        this.ds.load(
        {
            params: {
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.companyPref.recperpage,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()

            }
        });
    },

    configurAdvancedSearch:function(){
        this.objsearchComponent.show();
        this.doLayout();
    },
    filterStore:function(json){
        this.searchJson=json;
        if(trackStoreLocation){        
        //this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
        }else{
        //this.initloadgridstore(this.storeCmb.getValue());
        }    
    },
    clearStoreFilter:function(){
        this.objsearchComponent.hide();
        this.doLayout();
        this.searchJson="";
        if(trackStoreLocation){        
        // this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
        }else{
        //this.initloadgridstore(this.storeCmb.getValue());
        }
    }
});
Wtf.AssetDetailsreport = function(config){
    Wtf.AssetDetailsreport.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.AssetDetailsreport, Wtf.Panel, {
    initComponent: function() {
        Wtf.AssetDetailsreport.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.AssetDetailsreport.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
        //            params: {
        //                mode: 111,
        //                masterid: masterid,
        //                isShowCustColumn: true
        //            }
        }, this, function (request, response) {
            var customProductField = request.data;
            this.status = 0;
            this.searchJson="";

            this.record = Wtf.data.Record.create([
            {
                "name":"storecode"
            },

            {
                "name":"storedescription"
            },
            {
                "name":"itemid"
            },
            {
                "name":"itemname"
            },
            {
                "name":"itemdescription"
            },
            {
                "name":"itemcode"
            },
            {
                "name":"customer"
            },
            {
                "name":"location"
            },
            {
                "name":"rowName"
            },
            {
                "name":"rackName"
            },
            {
                "name":"binName"
            },
            {
                "name":"quantity"
            },
            {
                "name":"batchname"
            },
            {
                "name":"serialname"
            },
            {
              "name" : "serialid"  
            },
            {
                "name":"batchExpdate"
            },
            {
              "name" : "serialexptodate"  
            },
            {
              "name" : "skufieldValue"  
            },
            {
                "name":"skufield"
            },
            {
                "name":"ConsigneeName"
            },
            {
                "name":"consignmentdn"
            },
            {
                "name":"consignmendate"
            },
            {
                "name":"loanfromdate"
            },
            {
                "name":"loantodate"
            },
            {
                "name":"country"
            },
            {
                "name":"Purposeofloan"
            }
            ]);
            var grpView = new Wtf.grid.GroupingView({
//                forceFit: true,
                showGroupName: false,
                enableGroupingMenu: true,
                hideGroupedColumn: false
            });
            /*************************/

            this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },

            {
                name: 'abbr'
            },
            {
                name: "fullname"
            },
            {
                name: 'description'
            }
            ]);

            this.storeCmbStores = new Wtf.data.Store({
                url:'INVStore/getStoreList.do',
                baseParams:{
                    isActive :true,
                    byStoreExecutive:"true",
                    byStoreManager:"true",
                    assetdtl:true,
                    includePickandPackStore:true
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                },this.storeCmbRecord)
            });
        
            this.storeCmb = new Wtf.form.ComboBox({
                fieldLabel : 'Store*',
                hiddenName : 'storeid',
                store : this.storeCmbStores,
                typeAhead:true,
                displayField:'fullname',
                valueField:'store_id',
                mode: 'local',
                width : 125,
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
            var trackStoreLocation=true
            if(trackStoreLocation){
            
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
                    fieldLabel : 'Location*',
                    hiddenName : 'locationid',
                    store : this.locCmbStore,
                    typeAhead:true,
                    displayField:'name',
                    valueField:'id',
                    mode: 'local',
                    width : 125,
                    triggerAction: 'all',
                    emptyText:'Select location...',
                    listWidth:300,
                    tpl: new Wtf.XTemplate(
                        '<tpl for=".">',
                        '<div wtf:qtip = "{[values.name]}" class="x-combo-list-item">',
                        '<div>{name}</div>',
                        '</div>',
                        '</tpl>')
                // allowBlank:false
                });
            }          
               
            this.resetBtn = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
                },
                iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                scope:this,
                handler:function(){
                    this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
                    if(trackStoreLocation){
                        // this.locCmb.setValue(this.locCmb.store.data.items[0].data.id);
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                
                }
            });

            this.search = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.common.search"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
                },
                iconCls : 'accountingbase fetch',
                scope:this,
                handler:function(){
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }   
                }
            });
            
            this.UpdateSerialDetailsBtn = new Wtf.Toolbar.Button({
                text:this.type==1? WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateSerialDetails.title"):WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateBatchDetails.title"),
                tooltip: WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateSerialDetails.ttip"),
                scope: this,
                disabled: true,
//                hidden:this.type==2,
                iconCls: getButtonIconCls(Wtf.etype.copy),
                handler: this.UpdateAssetSerialDetailsHandler
            });
            
            this.CreatePOButton= new Wtf.Button({
                text: "Create PO",
                scope: this,
                tooltip: {
                    title:"Create PO", 
                    text:"Create Purchase Order for Selected Item"
                },
                handler: function(){
                    var gSm = this.grid.getSelectionModel();
                    if(gSm.getSelections().length==1){
                        var rec = gSm.getSelected();
                        createPOforItemInvLevel(rec.data.itemid, this.storeCmb.getValue(), rec.data.vendorId);
                    } else {
                        Wtf.MessageBox.show({
                            title:"Create PO",
                            msg:"Please select a item to create Purchase Order.",
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK
                        });
                    }
                },
                iconCls: "pwnd cloneicon caltb"
            });
        
            /***************************/


            this.ds = new Wtf.data.GroupingStore({
                sortInfo: {
                    field: 'itemcode',
                    direction: "ASC"
                },
//                groupField:"itemcode",
                url: 'INVStockLevel/getAssetDetailswithStock.do',//
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data',
                    totalProperty:'count'
                },
                this.record)
           
            });
            this.updateStoreConfig(customProductField);
           
            if(trackStoreLocation){       
                this.locCmbStore.on("load", function(ds, rec, o){
                    //                if(rec.length > 0){
                    var newRec=new this.storeCmbRecord({
                        id:'',
                        name:'ALL'
                    })
                    this.locCmbStore.insert(0,newRec);
                    this.locCmb.setValue('');
                //                }
                }, this);
            }

            this.storeCmbStores.on("load", function(ds, rec, o){
                if(rec.length > 0){
                    var newRec=new this.storeCmbRecord({
                        id:'ALL',
                        fullname:'ALL'
                    })
                    this.storeCmbStores.insert(0,newRec);
                    this.storeCmb.setValue('ALL');
                    if(rec.length==1){
                        this.storeCmb.setValue(rec[0].data.store_id);
                    }
                    this.storeCmb.fireEvent('select');
                    if(trackStoreLocation){                 
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                    //this.initloadgridstore(this.storeCmb.getValue());
                    }
                }
            }, this);
            this.storeCmbStores.load();
        
            this.storeCmb.on("select",function(){
                this.locCmbStore.load({
                    params:{
                        storeid:this.storeCmb.getValue()
                    }
                }) 
            },this);
            var sm= new Wtf.grid.CheckboxSelectionModel({
                 singleSelect:true
                });
          sm.on("selectionchange",this.enableDisablePostUnpostButtons.createDelegate(this),this);
            var integrationFeatureFor=true;
            var cmDefaultWidth = 100;
            var colArr = [
            new Wtf.KWLRowNumberer(),
            sm,
            //this.rowExpander,
           
            
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: 'itemcode',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.ProductName"),
                dataIndex: 'itemname',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridDescription"),
                dataIndex: 'itemdescription',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex: 'storecode',
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
                dataIndex: 'storedescription',
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add"),
                dataIndex: 'location',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.gridQty"),
                dataIndex: 'quantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                summaryType : 'sum',
                pdfwidth:50,
                renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                },
                summaryRenderer:function(v){
                    return "<b>"+parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
             
            {
                header: WtfGlobal.getLocaleText("acc.field.lotBatch"),
                dataIndex: 'batchname',
                width:cmDefaultWidth,
                sortable:false,
//                hidden:this.type==2,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.ExpiryDate"),
                dataIndex:'serialexptodate',   //check whether to display in Asset Details Tab or Stock Details Tab
                width:cmDefaultWidth,
                sortable:false,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.Serials"),
                dataIndex: 'serialname',
                width:cmDefaultWidth,
                sortable:false,
//                 hidden:this.type==2,
                pdfwidth:50
                
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.Assets"),
                dataIndex: 'skufield',
                width:cmDefaultWidth,
                sortable:false,
//                hidden:this.type==2,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.ConsigneeName"),
                dataIndex: 'ConsigneeName',
                width:cmDefaultWidth,
                sortable:false,
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.consignmentSalesManagementFlag)
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.ConsignmentDn"),
                dataIndex: 'consignmentdn',
                width:cmDefaultWidth,
                sortable:false,
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.consignmentSalesManagementFlag)
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.ConsignmenDate"),
                dataIndex: 'consignmendate',
                width:cmDefaultWidth,
                sortable:false,
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.consignmentSalesManagementFlag)
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.LoanFromDate"),
                dataIndex: 'loanfromdate',
                width:cmDefaultWidth,
                sortable:false,
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.consignmentSalesManagementFlag)
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.LoandueDate"),
                dataIndex: 'loantodate',
                width:cmDefaultWidth,
                sortable:false,
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.consignmentSalesManagementFlag)
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.Country"),
                dataIndex: 'country',
                width:cmDefaultWidth,
                sortable:false,
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.consignmentSalesManagementFlag)
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.Purposeofloan"),
                dataIndex: 'Purposeofloan',
                width:cmDefaultWidth,
                sortable:false,
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.consignmentSalesManagementFlag)
            }
        ];
            
            if(customProductField && customProductField.length>0) {
                for(var ccnt=0; ccnt<customProductField.length; ccnt++) {
                    colArr.push({
                        header : customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: 100,
                        hidden:(customProductField[ccnt].dataindex=="Custom_Material Group")?false:true,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }
            
            this.cm = new Wtf.grid.ColumnModel(colArr);    
            this.objsearchComponent=new Wtf.advancedSearchComponent({
                cm:this.cm
            });

            this.AdvanceSearchBtn = new Wtf.Button({
                text : "Advanced Search",
                scope : this,
                tooltip:'Manage search with your preference',
                handler : this.configurAdvancedSearch,
                iconCls :'accountingbase fetch'
            });
            this.exportButton=new Wtf.exportButton({
                obj:this,
                id:'assetdetail'+this.id,
                tooltip:"Export Report",  //"Export Report details.",  
                params:{
                    name: "Batchwise Stock Tracking"
                },
                menuItem:{
                    csv:true,
                    pdf:false,
                    xls:true
                },
                get:Wtf.autoNum.inventoryAllStock,
                label:"Export"
            })
            var tbararr = new Array();

            tbararr.push("-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":", this.storeCmb);
            tbararr.push("-",WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add")+":", this.locCmb);
            tbararr.push("-",this.search);
            tbararr.push("-", this.UpdateSerialDetailsBtn)
            tbararr.push("-",this.resetBtn);
            
            this.summaryBtn = new Wtf.Button({
                anchor : '90%',
                text: 'View Summary',
                scope:this,
                hidden: (true == 5 || true == 17 || true == 18),
                handler:function(){
                    this.storeCmb.setValue("");
                    if(this.status==0) {
                        this.storeCmb.disable();
                        this.cm.setHidden(2, true);
                        this.summaryBtn.setText("View Details");
                        this.status = 1;
                    } else {
                        this.status = 0;
                        this.storeCmb.enable();
                        this.cm.setHidden(2, false);
                        this.summaryBtn.setText("View Summary");
                    }
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                }
            });
            this.summary = new Wtf.grid.GroupSummary({});
            this.grid=new  Wtf.KwlEditorGridPanel({
                id:"assetdetaillist",
                region:'center',
                cm:this.cm,
                store:this.ds,
                sm:sm,
                loadMask:true,
                viewConfig: {
                    forceFit: false
                },
                view: grpView,
                autoScroll:true,
//                autoWidth:true,
//                plugins : [this.summary],
//                searchLabel:WtfGlobal.getLocaleText("acc.het.1001"),
                displayInfo:true,
                searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.EnterProductIDnamebatchserial"),
                serverSideSearch:true,
                //advanceSearch:true,
                tbar:tbararr,
                bbar:[this.exportButton]
            });

            this.innerPanel = new Wtf.Panel({
                layout : 'border',
                bodyStyle :"background-color:transparent;",
                border:false,
                items : [this.grid,this.objsearchComponent]

            });
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);
            this.add(this.innerPanel);
            this.doLayout();
//            this.objsearchComponent.advGrid.on("filterStore",this.filterStore, this);
//            this.objsearchComponent.advGrid.on("clearStoreFilter",this.clearStoreFilter, this);
            
            this.on("activate",function(){
                this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
            },this);
        }, function () {
    });
  
    },
    enableDisablePostUnpostButtons: function () {
        var rec = this.grid.getSelectionModel().getSelected();
        if (rec !== undefined&&(rec.data.isBatchForProduct=="T"||rec.data.isSerialForProduct=="T")) {
            this.UpdateSerialDetailsBtn.enable();
        } else {
            this.UpdateSerialDetailsBtn.disable();
        }
    },
     UpdateAssetSerialDetailsHandler:function(){
  
//               rec.applydate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
        var rec = this.grid.getSelectionModel().getSelected();
        this.skufieldValue=rec.data.skufieldValue;
        this.serialid=rec.data.serialid;
        this.serialexptodate=rec.data.serialexptodate;
        this.batchExpdate=rec.data.batchExpdate;
        this.batchName=rec.data.batchname;
        this.itemcode=rec.data.itemcode;
        this.isSerialForProduct=rec.data.isSerialForProduct;
        
        this.updateAssetDetail = new Wtf.UpdateAssetSerialDetails({
           title:this.type==1?WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateSerialDetails.title"):WtfGlobal.getLocaleText("acc.assetdetailreport.tbarbutton.UpdateBatchDetails.title"),
            layout:'border',
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            height:200,
            assetdetailreport:this,
            skufieldValue : this.skufieldValue,
            serialid:this.serialid,
            serialexptodate:  this.serialexptodate,
            batchExpdate:  this.batchExpdate,
            type:this.type,
            itemcode:this.itemcode,
            batchName:this.batchName,
            isSerialForProduct:this.isSerialForProduct,
            width:400
        });
     this.updateAssetDetail.on('updateexpirydateandassetid',function(){
         alert('Sharad');
         
     }, this);
        this.updateAssetDetail.show();   
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
    
    initloadgridstore:function(storetype,locationtype){

        this.ds.baseParams = {
            store:this.storeCmb.getValue(),
            location:this.locCmb.getValue(),
            type:this.type,
            searchJson:this.searchJson
        }
        this.ds.load(
        {
            params: {
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.companyPref.recperpage,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()

            }
        });
    },

    configurAdvancedSearch:function(){
        this.objsearchComponent.show();
        this.doLayout();
    },
    filterStore:function(json){
        this.searchJson=json;   
    },
    clearStoreFilter:function(){
        this.objsearchComponent.hide();
        this.doLayout();
        this.searchJson="";
    }
});
Wtf.DateWiseBatchInventoryLevel = function(config){
    Wtf.DateWiseBatchInventoryLevel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.DateWiseBatchInventoryLevel, Wtf.Panel, {
    initComponent: function() {
        Wtf.DateWiseBatchInventoryLevel.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.DateWiseBatchInventoryLevel.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
        //            params: {
        //                mode: 111,
        //                masterid: masterid,
        //                isShowCustColumn: true
        //            }
        }, this, function (request, response) {
            var customProductField = request.data;
            this.status = 0;
            this.searchJson="";

            this.record = Wtf.data.Record.create([
            {
                "name":"storecode"
            },

            {
                "name":"storedescription"
            },
            {
                "name":"itemid"
            },
            {
                "name":"itemname"
            },
            {
                "name":"itemdescription"
            },
            {
                "name":"itemcode"
            },
            {
                "name":"ccpartnumber"
            },
            {
                "name":"location"
            },
            {
                "name":"rowName"
            },
            {
                "name":"rackName"
            },
            {
                "name":"binName"
            },
            {
                "name":"quantity"
            },
            {
                "name":"batchPricePerUnit"
            },
            {
                "name":"batchName"
            },
            {
                "name":"serials"
            },
            {
                "name":"batchQty"
            },
            {
                "name":"uom"
            },
            {
                "name":"stockType"
            },
            {
                "name":"stockTypeName"
            }
            ]);
            var grpView = new Wtf.grid.GroupingView({
                forceFit: true, 
                showGroupName: true,
                enableGroupingMenu: true,
                hideGroupedColumn: false
            });
            /*************************/

            this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },

            {
                name: 'abbr'
            },
            {
                name: "fullname"
            },
            {
                name: 'description'
            }
            ]);

            this.storeCmbStore = new Wtf.data.Store({
                url:'INVStore/getStoreList.do',
                baseParams:{
                    isActive :true,
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
                fieldLabel : 'Store*',
                hiddenName : 'storeid',
                store : this.storeCmbStore,
                typeAhead:true,
                displayField:'fullname',
                valueField:'store_id',
                mode: 'local',
                width : 125,
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
            var trackStoreLocation=true
            if(trackStoreLocation){
            
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
                    fieldLabel : 'Location*',
                    hiddenName : 'locationid',
                    store : this.locCmbStore,
                    typeAhead:true,
                    displayField:'name',
                    valueField:'id',
                    mode: 'local',
                    width : 125,
                    triggerAction: 'all',
                    emptyText:'Select location...',
                    listWidth:300,
                    tpl: new Wtf.XTemplate(
                        '<tpl for=".">',
                        '<div wtf:qtip = "{[values.name]}" class="x-combo-list-item">',
                        '<div>{name}</div>',
                        '</div>',
                        '</tpl>')
                // allowBlank:false
                });
            }          
            this.dateFilter = new Wtf.form.DateField({
                fieldLabel:'Date',
                hiddenName: 'ondate',
                value: new Date(),
                format: 'Y-m-d'
            })   
            this.resetBtn = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
                },
                iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                scope:this,
                handler:function(){
                    this.dateFilter.reset();
                    this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
                    if(trackStoreLocation){
                        // this.locCmb.setValue(this.locCmb.store.data.items[0].data.id);
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                
                }
            });

            this.search = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.common.search"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
                },
                iconCls : 'accountingbase fetch',
                scope:this,
                handler:function(){
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }   
                }
            });

            this.CreatePOButton= new Wtf.Button({
                text: "Create PO",
                scope: this,
                tooltip: {
                    title:"Create PO", 
                    text:"Create Purchase Order for Selected Item"
                },
                handler: function(){
                    var gSm = this.grid.getSelectionModel();
                    if(gSm.getSelections().length==1){
                        var rec = gSm.getSelected();
                        createPOforItemInvLevel(rec.data.itemid, this.storeCmb.getValue(), rec.data.vendorId);
                    } else {
                        Wtf.MessageBox.show({
                            title:"Create PO",
                            msg:"Please select a item to create Purchase Order.",
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK
                        });
                    }
                },
                iconCls: "pwnd cloneicon caltb"
            });
        
            /***************************/


            this.ds = new Wtf.data.GroupingStore({
                sortInfo: {
                    field: 'itemcode',
                    direction: "ASC"
                },
                groupField:"itemcode",
                url: 'INVStockLevel/getDateWiseBatchStockInventory.do',//
                //                url: 'INVStockLevel/getBachWiseStockInventory.do',//
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data',
                    totalProperty:'count'
                },
                this.record)
           
            });
            this.updateStoreConfig(customProductField);
           
            if(trackStoreLocation){       
                this.locCmbStore.on("load", function(ds, rec, o){
                    //                if(rec.length > 0){
                    var newRec=new this.storeCmbRecord({
                        id:'',
                        name:'ALL'
                    })
                    this.locCmbStore.insert(0,newRec);
                    this.locCmb.setValue('');
                //                }
                }, this);
            }

            this.storeCmbStore.on("load", function(ds, rec, o){
                if(rec.length > 0){
                    this.storeCmb.setValue(rec[0].data.store_id);
                    this.storeCmb.fireEvent('select');
                    if(trackStoreLocation){                 
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                    //this.initloadgridstore(this.storeCmb.getValue());
                    }
                }
            }, this);
            this.storeCmbStore.load();
        
            this.storeCmb.on("select",function(){
                this.locCmbStore.load({
                    params:{
                        storeid:this.storeCmb.getValue()
                    }
                }) 
            },this);
            var sm= new Wtf.grid.CheckboxSelectionModel({
                // singleSelect:true
                });
        
            var integrationFeatureFor=true;
            var cmDefaultWidth = 200;
            var colArr = [
            //sm
            new Wtf.KWLRowNumberer(),
            //this.rowExpander,
           
            
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: 'itemcode',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
                dataIndex: 'itemname',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Description"),
                dataIndex: 'itemdescription',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex: 'storecode',
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
                dataIndex: 'storedescription',
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add"),
                dataIndex: 'location',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorysetup.row"),
                dataIndex: 'rowName',
                width:cmDefaultWidth,
                hidden:Wtf.account.companyAccountPref.isRowCompulsory?false:true,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorysetup.rack"),
                dataIndex: 'rackName',
                width:cmDefaultWidth,
                hidden:Wtf.account.companyAccountPref.isRackCompulsory?false:true,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.inventorysetup.bin"),
                dataIndex: 'binName',
                width:cmDefaultWidth,
                hidden:Wtf.account.companyAccountPref.isBinCompulsory?false:true,
                pdfwidth:100,
                summaryRenderer:function(v){
                    return "<div style = 'float:right;'><b>Balance :</b></div>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.gridQty"),
                dataIndex: 'quantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                summaryType : 'sum',
                pdfwidth:50,
                renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                },
                summaryRenderer:function(v){
                    return "<b>"+parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.report.rule16register.UOM"),
                dataIndex: 'uom',
                width:cmDefaultWidth,
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.lotBatch"),
                dataIndex: 'batchName',
                width:cmDefaultWidth,
                sortable:false,
                hidden:!Wtf.account.companyAccountPref.isBatchCompulsory,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockrequest.Serials"),
                dataIndex: 'serials',
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
                header: WtfGlobal.getLocaleText("acc.field.stockType_ReusableOrNonReusable"),
                dataIndex: 'stockTypeName',
                width:cmDefaultWidth,
                align:'center',
                pdfwidth:30,
                renderer:function(val, m ,r){
                    if(r.get('stockType') == "REUSABLE"){
                        return "<div wtf:qtip='Reusable'>"+val+"</div>"
                    }else if(r.get('stockType') == "DISPOSABLE"){
                        return "<div wtf:qtip='Consumable'>"+val+"</div>"
                    }else {
                        return "<div wtf:qtip='Consumable'>"+val+"</div>"
                    }
                }
            }
            ];
            if (customProductField && customProductField.length > 0) {
                for (var ccnt = 0; ccnt < customProductField.length; ccnt++) {
                    colArr.push({
                        header: customProductField[ccnt].columnname,
                        dataIndex: customProductField[ccnt].dataindex,
                        width: cmDefaultWidth,
                        pdfwidth: 50,
                        align: 'center'
                    })
                }
            }    
            this.cm = new Wtf.grid.ColumnModel(colArr);    
            this.objsearchComponent=new Wtf.advancedSearchComponent({
                cm:this.cm
            });

            this.AdvanceSearchBtn = new Wtf.Button({
                text : WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"),
                scope : this,
                tooltip:'Manage search with your preference',
                handler : this.configurAdvancedSearch,
                iconCls :'accountingbase fetch'
            });
            this.exportButton=new Wtf.exportButton({
                obj:this,
                tooltip:WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"),  //"Export Report details.",  
                params:{
                    name: "Datewise Batch Stock Tracking"
                },
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true
                },
                get:Wtf.autoNum.DateWiseBatchStockTrackingReport,
                label:"Export"
            })
            var tbararr = new Array();

            tbararr.push("-",WtfGlobal.getLocaleText("acc.pdf.6")+":", this.dateFilter);
            tbararr.push("-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":", this.storeCmb);
            tbararr.push("-",WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add")+":", this.locCmb);
            tbararr.push("-",this.search);
            tbararr.push("-",this.resetBtn);
            
            this.summaryBtn = new Wtf.Button({
                anchor : '90%',
                text: 'View Summary',
                scope:this,
                hidden: (true == 5 || true == 17 || true == 18),
                handler:function(){
                    this.storeCmb.setValue("");
                    if(this.status==0) {
                        this.storeCmb.disable();
                        this.cm.setHidden(2, true);
                        this.summaryBtn.setText("View Details");
                        this.status = 1;
                    } else {
                        this.status = 0;
                        this.storeCmb.enable();
                        this.cm.setHidden(2, false);
                        this.summaryBtn.setText("View Summary");
                    }
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                }
            });
            this.summary = new Wtf.grid.GroupSummary({});
            this.grid=new  Wtf.KwlEditorGridPanel({
                region:'center',
                cm:this.cm,
                store:this.ds,
                sm:sm,
                loadMask:true,
                viewConfig: {
                    forceFit: false
                },
                view: grpView,
                autoscroll:true,
                plugins : [this.summary],
                searchLabel:WtfGlobal.getLocaleText("acc.field.QuickSearch"),
                nopaging: true,
                displayInfo:true,
                searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.EnterProductIDnamebatchserial"),
                serverSideSearch:true,
                //advanceSearch:true,
                tbar:tbararr,
                bbar:[this.exportButton]
            });

            //If you want to enable button ,if only one record selected ,otherwise disable
            //        var arrId=new Array();
            //        arrId.push("delete");//"deleteIssueBtn" id of button
            //        arrId.push("edit");
            //
            //        enableDisableButton(arrId,this.ds,sm);
            this.innerPanel = new Wtf.Panel({
                layout : 'border',
                bodyStyle :"background-color:transparent;",
                border:false,
                items : [this.grid,this.objsearchComponent]

            });
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);
            this.add(this.innerPanel);
            this.doLayout();
            this.objsearchComponent.advGrid.on("filterStore",this.filterStore, this);
            this.objsearchComponent.advGrid.on("clearStoreFilter",this.clearStoreFilter, this);
        }, function () {

            });
    //        if(trackStoreLocation){            
    //            this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
    //        }else{
    //            this.initloadgridstore(this.storeCmb.getValue());
    //        }    
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
    initloadgridstore:function(storetype,locationtype){

        this.ds.baseParams = {
            ondate: this.dateFilter.getValue().format("Y-m-d"),
            store:storetype,
            location:locationtype,
            //summaryFlag: this.status == 1,
            searchJson:this.searchJson
        }
        this.ds.load(
        {
            params: {
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.companyPref.recperpage,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()

            }
        });
    },

    configurAdvancedSearch:function(){
        this.objsearchComponent.show();
        this.doLayout();
    },
    filterStore:function(json){
        this.searchJson=json;
        if(trackStoreLocation){        
        //this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
        }else{
        //this.initloadgridstore(this.storeCmb.getValue());
        }    
    },
    clearStoreFilter:function(){
        this.objsearchComponent.hide();
        this.doLayout();
        this.searchJson="";
        if(trackStoreLocation){        
        // this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
        }else{
        //this.initloadgridstore(this.storeCmb.getValue());
        }
    }
});



//-----------------------------------------------Store Wise inventory level-----------------------------------------------------


Wtf.StoreWiseInventoryLevel = function(config){
    Wtf.StoreWiseInventoryLevel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.StoreWiseInventoryLevel, Wtf.Panel, {
    initComponent: function() {
        Wtf.StoreWiseInventoryLevel.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.StoreWiseInventoryLevel.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
        //            params: {
        //                mode: 111,
        //                masterid: masterid,
        //                isShowCustColumn: true
        //            }
        }, this, function (request, response) {
            var customProductField = request.data;
            this.status = 0;
            this.searchJson="";

            this.record = Wtf.data.Record.create([
            {
                "name":"storecode"
            },

            {
                "name":"storedescription"
            },
            {
                "name":"itemid"
            },
            {
                "name":"itemdescription"
            },
            {
                "name":"itemcode"
            },
            {
                "name":"itemname"
            },
            {
                "name":"ccpartnumber"
            },
            {
                "name":"shelflocation"
            },
            {
                "name":"quantity"
            },
            {
                "name":"blockquantity"
            },
            {
                "name":"repairquantity"
            },
            {
                "name":"qaquantity"
            },
            {
                "name":"batchPricePerUnit"
            },
            {
                "name":"batchNo"
            },
            {
                "name":"batchQty"
            },
            {
                "name":"microskey1"
            },
            {
                "name":"microskey2"
            },
            {
                "name":"wincorkey"
            },
            {
                "name":"posdescription"
            },
            {
                "name":"uom"
            },
            {
                "name":"dio"
            },{
                "name":"vendorId"
            },
            {
                "name":"isBatchForProduct"
            },
            {
                "name":"isSerialForProduct"
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
                "name":"stockDetails"
            },
            {
                "name":"stockType"
            },
            {
                "name":"stockTypeName"
            }

            ]);
            //        var grpView = new Wtf.grid.GroupingView({
            //            forceFit: true,
            //            showGroupName: true,
            //            enableGroupingMenu: true,
            //            hideGroupedColumn: true
            //        });
            /*************************/

            this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },

            {
                name: 'abbr'
            },
            {
                name: "fullname"
            },

            {
                name: 'description'
            }
            ]);

            this.storeCmbStore = new Wtf.data.Store({
                url:  this.fromMachineShop == true ? 'INVStore/getStoreList.do': 'INVStore/getStoreList.do',
                baseParams:{
//                    isActive :true,   //ERP-40021 :To get all Stores.
                    byStoreManager:'true',
                    byStoreExecutive:'true',
                    includeQAAndRepairStore:true,
                    includePickandPackStore:true
                //flag:this.fromMachineShop == true ? 4 : 7,
                // storeTypes: this.fromMachineShop == true ? "1":null,//warehouse
                // fromMachineShop: this.fromMachineShop
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                },this.storeCmbRecord)
            });
        
            this.storeCmb = new Wtf.form.ComboBox({
                fieldLabel : 'Store*',
                hiddenName : 'storeid',
                store : this.storeCmbStore,
                typeAhead:true,
                displayField:'fullname',
                valueField:'store_id',
                mode: 'local',
                width : 125,
                triggerAction: 'all',
                emptyText:'Select store...',
                listWidth:300,
                tpl: new Wtf.XTemplate(
                    '<tpl for=".">',
                    '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                    '<div>{fullname}</div>',
                    '</div>',
                    '</tpl>')
            // allowBlank:false
            });
            
            var trackStoreLocation=true;
        
            if(trackStoreLocation){
            
                this.locCmbRecord = new Wtf.data.Record.create([
                {
                    name: 'id'
                },        

                {
                    name: 'name'
                }]);

                this.locCmbStore = new Wtf.data.Store({
                    url:  'INVStore/getStoreLocations.do',
                    reader: new Wtf.data.KwlJsonReader({
                        root: 'data'
                    },this.locCmbRecord)
                });

                this.locCmbStore.on("beforeload", function(ds, rec, o){
                    this.locCmbStore.removeAll();
                    this.locCmb.reset(); 
                }, this);
            

                this.locCmb = new Wtf.form.ComboBox({
                    hiddenName : 'locationid',
                    store : this.locCmbStore,
                    typeAhead:true,
                    displayField:'name',
                    valueField:'id',
                    mode: 'local',
                    width : 125,
                    triggerAction: 'all',
                    listWidth:300,
                    tpl: new Wtf.XTemplate(
                        '<tpl for=".">',
                        '<div wtf:qtip = "{[values.name]}" class="x-combo-list-item">',
                        '<div>{name}</div>',
                        '</div>',
                        '</tpl>')
                //emptyText:'Select location...',
                //allowBlank:false
                });
            }  
            this.productTypeRec = Wtf.data.Record.create([
            {
                name: 'id'
            },

            {
                name: 'name'
            }
            ]);
            this.productTypeStore=new Wtf.data.Store({
                url: "ACCProduct/getProductTypes.do",
                baseParams:{
                    mode:24,
                    common:'1'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.productTypeRec)
            });
           
            this.productTypeStore.load();
            this.producttype= new Wtf.form.FnComboBox({
                id:'producttypecmb',
                store:this.productTypeStore,
                anchor:'70%',
                valueField:'id',
                displayField:'name',
                forceSelection: true
            });
       
            
            
            this.productTypeStore.on("load", function(ds, rec, o) {
                if(this.id=="storewiseInventoryLevel"){
                    var newRec = new this.productTypeRec({
                        id: 'All',
                        name: 'All'
                    });
                    this.productTypeStore.insert(0, newRec);
                
                    //"4efb0286-5627-102d-8de6-001cc0794cfa" - service type, "f071cf84-515c-102d-8de6-001cc0794cfa"  - non-inventory type
                    var deleteRecArr=["4efb0286-5627-102d-8de6-001cc0794cfa","f071cf84-515c-102d-8de6-001cc0794cfa"]; 
                
                    for (var x = 0; x < deleteRecArr.length; x++) {
                        var idx = this.productTypeStore.find(this.producttype.valueField, deleteRecArr[x], 0, false);
                        if (idx != -1) {
                            this.productTypeStore.remove(this.productTypeStore.getAt(idx));
                        }
                    }
                
                
                    this.producttype.setValue('All');
                }
            }, this);
            
            
            
            this.storeCmbStore.load();    
            this.storeCmb.on("select",function(){
                this.locCmbStore.load({
                    params:{
                        storeid:this.storeCmb.getValue()
                    }
                }) 
            
            },this);
        
            this.itemTypeRec = new Wtf.data.Record.create([
            {
                name:'id'
            },

            {
                name:'name'
            }
            ]);

            
            this.resetBtn = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
                },
                iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                scope:this,
                handler:function(){
                    this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
                    this.producttype.setValue(this.producttype.store.data.items[0].data.id);
                    this.storeCmb.fireEvent('select');
                    Wtf.getCmp("Quick"+this.grid.id).setValue("");
                    if(trackStoreLocation){
                        // this.locCmb.setValue(this.locCmb.store.data.items[0].data.id);
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                
                }
            });

            this.search = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.common.search"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
                },
                iconCls : 'accountingbase fetch',
                scope:this,
                handler:function(){
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }   
                }
            });
          
            this.updateSerial = new Wtf.Button({
                text:WtfGlobal.getLocaleText("acc.product.serialupdate"),
                tooltip:WtfGlobal.getLocaleText("acc.product.serialupdate"),
                disabled: false,
                iconCls :getButtonIconCls(Wtf.etype.add),
                scope: this,
                handler:function() {
                    var selectedRec=this.grid.getSelectionModel().getSelections();
                    if(selectedRec.length==0){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.34")],0);
                        return;
                    }
                    if(selectedRec[0].data.isSerialForProduct){
                        this.updateSerialFun(selectedRec);
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.serialnumbernotsetforproduct")],0);
                        return;
                    }
                }
            });
            this.CreatePOButton= new Wtf.Button({
                text: "Create PO",
                scope: this,
                tooltip: {
                    title:"Create PO", 
                    text:"Create Purchase Order for Selected Item"
                },
                handler: function(){
                    var gSm = this.grid.getSelectionModel();
                    if(gSm.getSelections().length==1){
                        var rec = gSm.getSelected();
                        createPOforItemInvLevel(rec.data.itemid, this.storeCmb.getValue(), rec.data.vendorId);
                    } else {
                        Wtf.MessageBox.show({
                            title:"Create PO",
                            msg:"Please select a item to create Purchase Order.",
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK
                        });
                    }
                },
                iconCls: "pwnd cloneicon caltb"
            });
        
            /***************************/


            this.ds = new Wtf.data.Store({
                //            sortInfo: {
                //                field: 'storecode',
                //                direction: "ASC"
                //            },
                // groupField:"storecode",
                //remoteSort: true,
                baseParams: {
                // flag: 18


                },
                url: 'INVStockLevel/getStoreWiseStockInventory.do',//
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data',
                    totalProperty:'count'
                },
                this.record),
                listeners: {
                    'load' :  {
                        fn : function(store,records) {
                                       
                            for(var i=0;i<records.length;i++)
                            {
                                records[i].data.batchPricePerUnit=records[i].data.batchPricePerUnit//WtfGlobal.getCurrencyFormatWithoutSymbol(records[i].data.batchPricePerUnit,Wtf.companyPref.priceDecimalPrecision);
                                records[i].data.batchQty=records[i].data.batchQty//WtfGlobal.getCurrencyFormatWithoutSymbol(records[i].data.batchQty,Wtf.companyPref.quantityDecimalPrecision);
                            }
                       
                        }
                    }          
                }
            });
            
            this.ds.on('load',function(){
                WtfGlobal.resetAjaxTimeOut();
            },this)
            
            this.ds.on('loadexception',function(){
                WtfGlobal.resetAjaxTimeOut();
            },this)
                
            this.updateStoreConfig(customProductField);
           
            if(trackStoreLocation){       
                this.locCmbStore.on("load", function(ds, rec, o){
                    var newRec=new this.locCmbRecord({
                        id:'',
                        name:'ALL'
                    })
                    this.locCmbStore.insert(0,newRec);
                    this.locCmb.setValue('');
                }, this);
            }

            this.storeCmbStore.on("load", function(ds, rec, o){
                if(rec.length > 1){
                    var newRec=new this.storeCmbRecord({
                        store_id:'',
                        fullname:'ALL'
                    })
                    this.storeCmbStore.insert(0,newRec);
                    this.storeCmb.setValue('');
                }else if(rec.length > 0){
                    this.storeCmb.setValue(rec[0].data.store_id, true);
                }
                this.storeCmb.fireEvent('select');
                
                this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
           
            }, this);



            var sm= new Wtf.grid.CheckboxSelectionModel({
                // singleSelect:true
                });
               
            var cmDefaultWidth = 200;
            var integrationFeatureFor=true;

            this.expander = new Wtf.grid.RowExpander({
                tpl:new Wtf.XTemplate(
                    '<table cellspacing="1" cellpadding="0" style="margin-top:15px;width:100%;margin-bottom:40px;position:relative" border="0">',
            
                    '<tr>',
                    '<th style="padding-left:50px"><h2><b>No.</b></h2></th>',
                    '<th ><h2><b>Store</b></h2></th>',
                    '<th ><h2><b>Location</b></h2></th>',
                
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isRowForProduct)==true">',  // Row
                    '<th><h2><b>Row</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
                    
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isRackForProduct)==true">',  // rack
                    '<th><h2><b>Rack</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
                    
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isBinForProduct)==true">',  // bin
                    '<th><h2><b>Bin</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
                    
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isBatchForProduct)==true">',  // batch
                    '<th><h2><b>Batch</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
            
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isSerialForProduct)==false">',  // batch
                    '<th><h2><b>Blocked Quantity</b></h2></th>',
                    '</tpl>',
                    //                    '</tpl>',
            
                    //                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isSerialForProduct)==true">',  // serial 
                    '<th><h2><b>Serials</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
                
            
                    //                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isSerialForProduct)==true">',  // serial 
                    '<th><h2><b>Blocked Serials</b></h2></th>',
                    '</tpl>',
                    //                    '</tpl>',
                
                    '<th ><h2><b>Quantity</b></h2></th>',
                    '</tr>',
                
                    '<tr><span  class="gridLine" style="width:95%;margin-left:45px;position: relative;top: 33px;"></span></tr>',
                
                    '<tpl for="stockDetails">',
                    '<tr>',
                    '<td style="padding-left:50px"><p>{#}</p></td>',
                    '<td ><p>{storeName}</p></td>',
                    '<td ><p>{locationName}</p></td>',
                
                    '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // row
                    '<td ><p>{rowName}</p></td>',
                    '</tpl>',
                    
                    '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // rack
                    '<td ><p>{rackName}</p></td>',
                    '</tpl>',
                    
                    '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // bin
                    '<td ><p>{binName}</p></td>',
                    '</tpl>',
                    
                    '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
                    '<td ><p>{batchName}</p></td>',
                    '</tpl>',
              
                      
                    '<tpl if="this.isTrue(parent.isSerialForProduct)==false">',  // batch
                    '<td ><p>{blockedBatchName}</p></td>',
                    '</tpl>',
              
                    '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  //  serial 
                    '<td ><p>{serialNames}</p></td>',
                    '</tpl>',
               
                    '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  //  blockedSerialNames 
                    '<td ><p>{blockedSerialNames}</p></td>',
                    '</tpl>',
               
                    '<tpl if="this.getQuantityDecimalPreciedValue(values.quantity)">',
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
                            return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        }
                    }
                    )   
                ,
                renderer : function(v, p, record){
//                    if(record.get("stockDetails") != "" && record.get("stockDetails") != undefined){ //means has stock detail data
//                       return  '<div class="x-grid3-row-expander">&#160;</div>'
//                    }else{
//                        //return '&#160;' 
//                        //return  '<div class="x-grid3-row-expander" onclick="showMessageBox(\'Select specific Store and specific Location to view detail.\')">&#160;</div>'
//                        return  '<div class="x-grid3-row-expander" onclick="">&#160;</div>'
//                    }
                     return  '<div class="x-grid3-row-expander">&#160;</div>'
                },
                listeners: {
                    'beforeexpand': function(a,record, body, rowIndex){
//                        var stockDetail=record.get('stockDetails');
//                        if(stockDetail==undefined || stockDetail==""){
//                            showMessageBox('Select specific Store to view details.');
//                            return false;
//                        }
                    },
                    'beforecollapse': function(fieldset){
                    //alert('beforecollapse');
                    }
                }
           
            });
      
            var colArr = [
            //sm
            new Wtf.KWLRowNumberer(),
            this.expander,
            {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex: 'storecode',
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.cosignmentloan.StoreDescription"),
                dataIndex: 'storedescription',
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: 'itemcode',
                width:cmDefaultWidth,
                pdfwidth:100
            // sortable:true,
            //dbname:'sb_itemmaster.itemcode'
            },
            {
                header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.productname"),
                dataIndex: 'itemname',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),
                dataIndex: 'itemdescription',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.je.CoilcraftPartNo"),
                dataIndex: 'ccpartnumber',
                // sortable:true,
                hidden:true// integrationFeatureFor == true ? false: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
                dataIndex: 'shelflocation',
                // sortable:true,
                hidden: true //integrationFeatureFor == true ? false: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.MicrosKey"),
                dataIndex: 'microskey2',
                //  sortable:true,
                dbname:'sb_itemmaster.microskey2',
                hidden:true
            },
            //            {
            //                header: "Wincor Code",
            //                dataIndex: 'wincorkey',
            //                sortable:true,
            //                dbname:'sb_itemmaster.wincorkey'
            //            },
            {
                header: WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header5"),
                dataIndex: 'quantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                dbname:'sb_invetorylist.quantity',
                pdfwidth:50,
                renderer:function(v){
                    return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.BlockedQuantity"),
                dataIndex: 'blockquantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
//                hidden:(Wtf.account.companyAccountPref.consignmentSalesManagementFlag?false:true),
                //                dbname:'sb_invetorylist.quantity',
                pdfwidth:50,
                renderer:function(v){
                    return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.UnderQA"),
                dataIndex: 'qaquantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                summaryType : 'sum',
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.activateQAApprovalFlow),
                renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                },
                summaryRenderer:function(v){
                    return "<b>"+v+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.UnderRepair"),
                dataIndex: 'repairquantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                summaryType : 'sum',
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.activateQAApprovalFlow),
                renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                },
                summaryRenderer:function(v){
                    return "<b>"+v+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.report.rule16register.UOM"),
                dataIndex: 'uom',
                width:cmDefaultWidth,
                pdfwidth:50
            //sortable:true,
            //dbname:'sb_itemconfigdata.name'
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.DIO"),
                dataIndex: 'dio',
                //sortable:false,
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.stockType_ReusableOrNonReusable"),
                dataIndex: 'stockTypeName',
                width:cmDefaultWidth,
                align:'center',
                pdfwidth:30,
                renderer:function(val, m ,r){
                    if(r.get('stockType') == "REUSABLE"){
                        return "<div wtf:qtip='Reusable'>"+val+"</div>"
                    }else if(r.get('stockType') == "DISPOSABLE"){
                        return "<div wtf:qtip='Consumable'>"+val+"</div>"
                    }else {
                        return "<div wtf:qtip='Consumable'>"+val+"</div>"
                    }
                }
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
            this.objsearchComponent=new Wtf.advancedSearchComponent({
                cm:this.cm
            });

            this.AdvanceSearchBtn = new Wtf.Button({
                text : "Advanced Search",
                scope : this,
                tooltip:'Manage search with your preference',
                handler : this.configurAdvancedSearch,
                iconCls :'accountingbase fetch'
            });

            var tbararr = new Array();
            tbararr.push("-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":",this.storeCmb);
            if(trackStoreLocation){
                tbararr.push("-",WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add")+":",this.locCmb);
            }
            tbararr.push("-",WtfGlobal.getLocaleText("acc.field.StockAvailabilityByWarehouseInvCat"),this.producttype);// Product Type
            tbararr.push("-",this.search);//,"-",this.AdvanceSearchBtn);
            tbararr.push("-",this.resetBtn);
            tbararr.push("-",this.updateSerial);

            this.summaryBtn = new Wtf.Button({
                anchor : '90%',
                text: 'View Summary',
                scope:this,
                hidden: (true == 5 || true == 17 || true == 18),
                handler:function(){
                    this.storeCmb.setValue("");
                    if(this.status==0) {
                        this.storeCmb.disable();
                        this.cm.setHidden(2, true);
                        this.summaryBtn.setText("View Details");
                        this.status = 1;
                    } else {
                        this.status = 0;
                        this.storeCmb.enable();
                        this.cm.setHidden(2, false);
                        this.summaryBtn.setText("View Summary");
                    }
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                }
            });
            this.exportButton=new Wtf.exportButton({
                obj:this,
                id:'storewisestockexport',
                tooltip:"Export Report",  //"Export Report details.",  
                params:{
                    name: "Stock Availability by Warehouse ("+this.storeCmb.getRawValue()+")"
                },
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true
                },
                get:Wtf.autoNum.StoreWiseStockBalanceReport,
                label:"Export"
            })
            this.grid=new  Wtf.KwlEditorGridPanel({
                id:"inventoryList1",
                region:'center',
                cm:this.cm,
                store:this.ds,
                sm:sm,
                loadMask:true,
                autoscroll:true,
                viewConfig: {
                    forceFit: false,
                    emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                },
                //view: grpView,
                plugins : this.expander,
                searchLabel:WtfGlobal.getLocaleText("acc.field.QuickSearch"),
                displayInfo:true,
                searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.EnterProductIDProductNameSerialName"),
                serverSideSearch:true,
                //advanceSearch:true,
                tbar:tbararr,
                bbar:[this.exportButton]
            });

            //If you want to enable button ,if only one record selected ,otherwise disable
            //        var arrId=new Array();
            //        arrId.push("delete");//"deleteIssueBtn" id of button
            //        arrId.push("edit");
            //
            //        enableDisableButton(arrId,this.ds,sm);
            this.innerPanel = new Wtf.Panel({
                layout : 'border',
                bodyStyle :"background-color:transparent;",
                border:false,
                items : [this.grid,this.objsearchComponent]

            });
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);
            this.add(this.innerPanel);
            this.doLayout();
            this.objsearchComponent.advGrid.on("filterStore",this.filterStore, this);
            this.objsearchComponent.advGrid.on("clearStoreFilter",this.clearStoreFilter, this);
            
            this.expander.on('expand',this.onRowexpand,this);
        }, function () {

            });
    },
    onRowexpand:function(scope, record, body){
        Wtf.Ajax.requestEx({
                url: "INVStockLevel/getStockDetailForProduct.do",
                params: {
                    productId:record.get("itemid"),
                    store:this.storeCmb.getValue(),
                    location:this.locCmb.getValue()
                }
            },
            this,
            function(action, response){
                if(action.success == true){
                    var data=action.data;
                    this.expander.tpl.overwrite(body, data);
                }else{
                    WtfComMsgBox(["Error", "Some error has occurred while processing your request."],0);
                }
            },
            function(){
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
    initloadgridstore:function(storetype,locationtype){
        WtfGlobal.setAjaxTimeOut();
        this.ds.baseParams = {
            store:storetype,
            location:locationtype,
            searchJson:this.searchJson,
            inventoryCatType:this.producttype.getValue()
        }
        this.ds.load(
        {
            params: {
                start:0,
                limit:30,//Wtf.companyPref.recperpage,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()

            }
        });
    },

    configurAdvancedSearch:function(){
        this.objsearchComponent.show();
        this.doLayout();
    },
    filterStore:function(json){
        this.searchJson=json;
        if(trackStoreLocation){        
            this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
        }else{
            this.initloadgridstore(this.storeCmb.getValue());
        }    
    },
    clearStoreFilter:function(){
        this.objsearchComponent.hide();
        this.doLayout();
        this.searchJson="";
        if(trackStoreLocation){        
            this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
        }else{
            this.initloadgridstore(this.storeCmb.getValue());
        }
    },
    updateSerialFun:function(selectedRec){
        updateSerialNames(selectedRec);
    }
    
});







//-----------------------------------------------Store Wise inventory level summary-----------------------------------------------------


Wtf.StoreWiseInventoryLevelSummary = function(config){
    Wtf.StoreWiseInventoryLevelSummary.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.StoreWiseInventoryLevelSummary, Wtf.Panel, {
    initComponent: function() {
        Wtf.StoreWiseInventoryLevelSummary.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.StoreWiseInventoryLevelSummary.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
        }, this, function (request, response) {
            this.status = 0;
            this.searchJson="";
        
            this.createProductRecordArray();
            this.createStore();
            this.createColumnModelArray();
            
            this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },

            {
                name: 'abbr'
            },
            {
                name: "fullname"
            },

            {
                name: 'description'
            }
            ]);

            this.storeCmbStore = new Wtf.data.Store({
                url:  'INVStore/getStoreList.do',
                baseParams:{
//                    isActive :true,   //ERP-40021 :To get all Stores.
                    byStoreManager:'true',
                    byStoreExecutive:'true',
                    includeQAAndRepairStore:true,
                    includePickandPackStore:true
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                },this.storeCmbRecord)
            });
        
            this.storeCmb = new Wtf.common.Select({
                multiSelect:true,
                forceSelection:true,
                hideTrigger1:true,
                fieldLabel : 'Store*',
                hiddenName : 'storeid',
                name : 'storeid',
                store : this.storeCmbStore,
                selectOnFocus:true,
                typeAhead:true,
                displayField:'fullname',
                valueField:'store_id',
                mode: 'local',
                width : 125,
                triggerAction: 'all',
                emptyText:'Select store...',
                listWidth:300,
                tpl: new Wtf.XTemplate(
                    '<tpl for=".">',
                    '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                    '<div>{fullname}</div>',
                    '</div>',
                    '</tpl>')
            });
            
            
            this.storeCmbStore.load();    
           
            this.resetBtn = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
                },
                iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                scope:this,
                handler: function () {
                    Wtf.getCmp("Quick" + this.grid.id).setValue("");
                    this.storeCmb.setValue("");
                    this.initloadgridstore(this.storeCmb.getValue());
                }
            });

            this.search = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.common.search"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
                },
                iconCls : 'accountingbase fetch',
                scope:this,
                handler:function(){
                   this.fetchHandler();
                }
            });
          
            this.storeCmbStore.on("load", function(ds, rec, o){
                this.initloadgridstore(this.storeCmb.getValue());
            }, this);

            var tbararr = new Array();
            tbararr.push("-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":",this.storeCmb);
            tbararr.push("-",this.search);
            tbararr.push("-",this.resetBtn);

            this.exportButton=new Wtf.exportButton({
                obj:this,
                id:'storewisestockexport',
                tooltip:"Export Report",  //"Export Report details.",  
                params:{
                    name: "Stock Availability by Warehouse Summary("+this.storeCmb.getRawValue()+")"
                },
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true
                },
                get:Wtf.autoNum.StoreWiseStockBalanceSummeryReport,
                label:"Export"
            })
            this.grid=new  Wtf.KwlEditorGridPanel({
                id:"inventoryList1"+this.id,
                region:'center',
                cm:this.cm,
                store:this.ds,
                sm:this.sm,
                loadMask:true,
                autoscroll:true,
                stripeRows : true,
                viewConfig: {
//                    forceFit: true,
                    emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                },
                //view: grpView,
                //plugins : this.expander,
                searchLabel:WtfGlobal.getLocaleText("acc.field.QuickSearch"),
                displayInfo:true,
                searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.EnterProductIDProductNameSerialName"),
                serverSideSearch:true,
                //advanceSearch:true,
                tbar:tbararr,
                bbar:[this.exportButton]
            });
            this.grid.on('cellclick',this.onCellClick, this);//on cell click call this function
            this.innerPanel = new Wtf.Panel({
                layout : 'border',
                bodyStyle :"background-color:transparent;",
                border:false,
                items : [this.grid,this.objsearchComponent]

            });
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
//                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);
            this.add(this.innerPanel);
            this.doLayout();
          
           // this.expander.on('expand',this.onRowexpand,this);
        }, function () {

            });
    },
    
    initloadgridstore:function(storeIds){
       
        WtfGlobal.setAjaxTimeOut();
        this.ds.baseParams = {
            store:storeIds
        }
        this.ds.load(
        {
            params: {
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss:Wtf.getCmp("Quick"+this.grid.id).getValue()
            }
        });
    },

  
   
    createProductRecordArray:function(){
        
        this.gridRecArray = new Array();
        
        this.gridRecArray.push(
            {name:'storecode'},
            {name:'storedescription'},
            {name:'itemid'},
            {name:'itemdescription'},
            {name:'itemcode'},
            {name:'itemname'},
            {name:'quantity'},
            {name:'blockquantity'},
            {name:'repairquantity'},
            {name:'qaquantity'},
            {name:'batchPricePerUnit'},
            {name:'batchNo'},
            {name:'batchQty'},
            {name:'uom'},
            {name:'isBatchForProduct'},
            {name:'isSerialForProduct'},
            {name:'isRowForProduct'},
            {name:'isRackForProduct'},
            {name:'isBinForProduct'},
            {name:'stockDetails'}
        );
    },
    
    createStore: function () {

        this.gridRec = Wtf.data.Record.create(this.gridRecArray);

        this.ds = new Wtf.data.Store({
            url: 'INVStockLevel/getStoreWiseStockInventorySummary.do', //
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: 'count'
            },
            this.gridRec)
            
        });

        this.ds.on('load', function () {
            WtfGlobal.resetAjaxTimeOut();
        }, this)

        this.ds.on('loadexception', function () {
            WtfGlobal.resetAjaxTimeOut();
        }, this)
    },
    
        
    createColumnModelArray:function(){
        var cmDefaultWidth = 150;
        this.columnArr =[];
        this.columnArr.push(
          new Wtf.KWLRowNumberer(),
          //this.expander,      
          {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: 'itemcode',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.productname"),
                dataIndex: 'itemname',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),
                dataIndex: 'itemdescription',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.report.rule16register.UOM"),
                dataIndex: 'uom',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.TotalAvailableQuantity"),
                dataIndex: 'quantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                pdfwidth:50,
                renderer:function(v){
                    return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.TotalBlockedQuantity"),//ERP-38808
                dataIndex: 'blockquantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
//                hidden:(Wtf.account.companyAccountPref.consignmentSalesManagementFlag?false:true),
                pdfwidth:50,
                renderer:this.formatQuantity
            }
        );
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({});

        this.cm = new Wtf.grid.ColumnModel(this.columnArr);
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.cm
        });

    },
    /*Code added for providing link to show SO details where quantity is blocked
     * ERP-38808*/
    formatQuantity: function(val, m, rec, i, j, s) {
        if (rec.data['type'] == "Service" || rec.data['type'] == "Non-Inventory Part") {
            return "N/A";
        }
        var unit = rec.data['uom'];
        var value = parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        if (isNaN(value)) {
            return val;
        }
        val = WtfGlobal.convertQuantityInDecimalWithLink(value, unit);
        return val;
    },
    onCellClick:function(g,i,j,e){
//        var record = g.getStore().getAt(i);//In report click on RichTextArea to show content
        var el=e.getTarget("a");
        if(el==null) {
            return;
        }
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="blockquantity"){
            this.viewTransaction(g,i,e)
        }
    },
    viewTransaction:function(grid, rowIndex, columnIndex){
        if(this.grid.getSelections().length>1){
            return;
        }
        var formrec=null;
    if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
            WtfComMsgBox(15,2);
            return;
        }
    formrec = this.grid.getStore().getAt(rowIndex);
        var productid=formrec.get('itemid');
            callSalesByProductAgainstSalesOrder(true,productid);
    },
     fetchHandler:function(){
        
        this.createProductRecordArray();// resetting record array
        this.createColumnModelArray();// resetting column array
        
        var selectedStore = this.storeCmb.getValue();
        if(selectedStore.length>0){
            
            var selectedStoreArray = selectedStore.split(",");
            for(var i=0; i<selectedStoreArray.length; i++){
                var storeId = selectedStoreArray[i];
                
                var storeRec = WtfGlobal.searchRecord(this.storeCmbStore, storeId, 'store_id');
                
                if(storeRec){
                    var storeName = storeRec.get('abbr');
                    var storeDescription = storeRec.get('description');
                    
                    this.gridRecArray.push({
                        name:storeId
                    });
                    
                    this.columnArr.push({
                        header:WtfGlobal.getLocaleText("acc.product.gridQty")+" in "+storeName+" ("+ storeDescription +")",
                        dataIndex:storeId,
                        align:'right',
                        renderer:function(v){
                            return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                        },
                        hidden:false,
                        width:150,
                        pdfrenderer:'quantity',
                        pdfwidth:75
                    });
                }
                
            }
        }
        
        this.createStore();
        
        this.grid.reconfigure(this.ds,new Wtf.grid.ColumnModel(this.columnArr));
        
        this.initloadgridstore(this.storeCmb.getValue());
    }
   
});


//////////////////////////////////////////////////////////////////////////////////////////////////////////




Wtf.DateWiseInventoryLevel = function(config){
    Wtf.DateWiseInventoryLevel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.DateWiseInventoryLevel, Wtf.Panel, {
    initComponent: function() {
        Wtf.DateWiseInventoryLevel.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.DateWiseInventoryLevel.superclass.onRender.call(this, config);
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getProductCustomFieldsToShow.do"
        //            params: {
        //                mode: 111,
        //                masterid: masterid,
        //                isShowCustColumn: true
        //            }
        }, this, function (request, response) {
            var customProductField = request.data;
            this.status = 0;
            this.searchJson="";

            this.record = Wtf.data.Record.create([
            {
                "name":"storecode"
            },

            {
                "name":"storedescription"
            },
            {
                "name":"itemid"
            },
            {
                "name":"itemdescription"
            },
            {
                "name":"itemcode"
            },
            {
                "name":"itemname"
            },
            {
                "name":"ccpartnumber"
            },
            {
                "name":"shelflocation"
            },
            {
                "name":"quantity"
            },
            {
                "name":"repairquantity"
            },
            {
                "name":"qaquantity"
            },
            {
                "name":"batchPricePerUnit"
            },
            {
                "name":"batchNo"
            },
            {
                "name":"batchQty"
            },
            {
                "name":"microskey1"
            },
            {
                "name":"microskey2"
            },
            {
                "name":"wincorkey"
            },
            {
                "name":"posdescription"
            },
            {
                "name":"uom"
            },
            {
                "name":"dio"
            },{
                "name":"vendorId"
            },
            {
                "name":"isBatchForProduct"
            },
            {
                "name":"isSerialForProduct"
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
                "name":"stockDetails"
            },
            {
                "name":"stockType"
            },
            {
                "name":"stockTypeName"
            }

            ]);
            //        var grpView = new Wtf.grid.GroupingView({
            //            forceFit: true,
            //            showGroupName: true,
            //            enableGroupingMenu: true,
            //            hideGroupedColumn: true
            //        });
            /*************************/

            this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },

            {
                name: 'abbr'
            },
            {
                name: "fullname"
            },

            {
                name: 'description'
            }
            ]);

            this.storeCmbStore = new Wtf.data.Store({
                url:  this.fromMachineShop == true ? 'INVStore/getStoreList.do': 'INVStore/getStoreList.do',
                baseParams:{
                    isActive :true,
                    byStoreManager:'true',
                    byStoreExecutive:'true',
                    includeQAAndRepairStore:true,
                    includePickandPackStore:true
                //flag:this.fromMachineShop == true ? 4 : 7,
                // storeTypes: this.fromMachineShop == true ? "1":null,//warehouse
                // fromMachineShop: this.fromMachineShop
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                },this.storeCmbRecord)
            });
        
            this.storeCmb = new Wtf.form.ComboBox({
                fieldLabel : 'Store*',
                hiddenName : 'storeid',
                store : this.storeCmbStore,
                typeAhead:true,
                displayField:'fullname',
                valueField:'store_id',
                mode: 'local',
                width : 125,
                triggerAction: 'all',
                emptyText:'Select store...',
                listWidth:300,
                tpl: new Wtf.XTemplate(
                    '<tpl for=".">',
                    '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                    '<div>{fullname}</div>',
                    '</div>',
                    '</tpl>')
            // allowBlank:false
            });
            
            this.dateFilter = new Wtf.form.DateField({
                fieldLabel:'Date',
                hiddenName: 'ondate',
                value: new Date(),
                format: 'Y-m-d'
            })
        
            var trackStoreLocation=true;
        
            if(trackStoreLocation){
            
                this.locCmbRecord = new Wtf.data.Record.create([
                {
                    name: 'id'
                },        

                {
                    name: 'name'
                }]);

                this.locCmbStore = new Wtf.data.Store({
                    url:  'INVStore/getStoreLocations.do',
                    reader: new Wtf.data.KwlJsonReader({
                        root: 'data'
                    },this.locCmbRecord)
                });

                this.locCmbStore.on("beforeload", function(ds, rec, o){
                    this.locCmbStore.removeAll();
                    this.locCmb.reset(); 
                }, this);
            

                this.locCmb = new Wtf.form.ComboBox({
                    hiddenName : 'locationid',
                    store : this.locCmbStore,
                    typeAhead:true,
                    displayField:'name',
                    valueField:'id',
                    mode: 'local',
                    width : 125,
                    triggerAction: 'all'
                //emptyText:'Select location...',
                //allowBlank:false
                });
            }   
        
       
            this.storeCmbStore.load();
        
            this.storeCmb.on("select",function(){
                this.locCmbStore.load({
                    params:{
                        storeid:this.storeCmb.getValue()
                    }
                }) 
            
            },this);
        
            this.itemTypeRec = new Wtf.data.Record.create([
            {
                name:'id'
            },

            {
                name:'name'
            }
            ]);

            
            this.resetBtn = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
                },
                iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                scope:this,
                handler:function(){
                    this.dateFilter.reset();
                    this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
                    this.storeCmb.fireEvent('select');
                    if(trackStoreLocation){
                        // this.locCmb.setValue(this.locCmb.store.data.items[0].data.id);
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                
                }
            });

            this.search = new Wtf.Button({
                anchor : '90%',
                text: WtfGlobal.getLocaleText("acc.common.search"),
                tooltip: {
                    text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
                },
                iconCls : 'accountingbase fetch',
                scope:this,
                handler:function(){
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }   
                }
            });
          
            this.CreatePOButton= new Wtf.Button({
                text: "Create PO",
                scope: this,
                tooltip: {
                    title:"Create PO", 
                    text:"Create Purchase Order for Selected Item"
                },
                handler: function(){
                    var gSm = this.grid.getSelectionModel();
                    if(gSm.getSelections().length==1){
                        var rec = gSm.getSelected();
                        createPOforItemInvLevel(rec.data.itemid, this.storeCmb.getValue(), rec.data.vendorId);
                    } else {
                        Wtf.MessageBox.show({
                            title:"Create PO",
                            msg:"Please select a item to create Purchase Order.",
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK
                        });
                    }
                },
                iconCls: "pwnd cloneicon caltb"
            });
        
            /***************************/


            this.ds = new Wtf.data.Store({
                //            sortInfo: {
                //                field: 'storecode',
                //                direction: "ASC"
                //            },
                // groupField:"storecode",
                //remoteSort: true,
                baseParams: {
                // flag: 18


                },
                url: 'INVStockLevel/getDateWiseStockInventory.do',//
                //                url: 'INVStockLevel/getStoreWiseStockInventory.do',//
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data',
                    totalProperty:'count'
                },
                this.record),
                listeners: {
                    'load' :  {
                        fn : function(store,records) {
                                       
                            for(var i=0;i<records.length;i++)
                            {
                                records[i].data.batchPricePerUnit=records[i].data.batchPricePerUnit//WtfGlobal.getCurrencyFormatWithoutSymbol(records[i].data.batchPricePerUnit,Wtf.companyPref.priceDecimalPrecision);
                                records[i].data.batchQty=records[i].data.batchQty//WtfGlobal.getCurrencyFormatWithoutSymbol(records[i].data.batchQty,Wtf.companyPref.quantityDecimalPrecision);
                            }
                       
                        }
                    }          
                }
            });
            
            this.ds.on('load',function(){
                WtfGlobal.resetAjaxTimeOut();
            },this)
            
            this.ds.on('loadexception',function(){
                WtfGlobal.resetAjaxTimeOut();
            },this)
            
            this.updateStoreConfig(customProductField);
           
            if(trackStoreLocation){       
                this.locCmbStore.on("load", function(ds, rec, o){
                    var newRec=new this.locCmbRecord({
                        id:'',
                        name:'ALL'
                    })
                    this.locCmbStore.insert(0,newRec);
                    this.locCmb.setValue('');
                }, this);
            }

            this.storeCmbStore.on("load", function(ds, rec, o){
                if(rec.length > 1){
                    var newRec=new this.storeCmbRecord({
                        store_id:'',
                        fullname:'ALL'
                    })
                    this.storeCmbStore.insert(0,newRec);
                    this.storeCmb.setValue('');
                }else if(rec.length > 0){
                    this.storeCmb.setValue(rec[0].data.store_id, true);
                }
                this.storeCmb.fireEvent('select');
                
                this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
           
            }, this);



            var sm= new Wtf.grid.CheckboxSelectionModel({
                // singleSelect:true
                });
               
            var cmDefaultWidth = 200;
            var integrationFeatureFor=true;
        

            this.expander = new Wtf.grid.RowExpander({
                tpl:new Wtf.XTemplate(
                    '<table cellspacing="1" cellpadding="0" style="margin-top:15px;width:100%;margin-bottom:40px;position:relative" border="0">',
            
                    '<tr>',
                    '<th style="padding-left:50px"><h2><b>No.</b></h2></th>',
                    '<th ><h2><b>Store</b></h2></th>',
                    '<th ><h2><b>Location</b></h2></th>',
                 
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isRowForProduct)==true">',  // Row
                    '<th><h2><b>Row</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
                    
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isRackForProduct)==true">',  // rack
                    '<th><h2><b>Rack</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
                    
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isBinForProduct)==true">',  // bin
                    '<th><h2><b>Bin</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
                    
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isBatchForProduct)==true">',  // batch
                    '<th><h2><b>Batch</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
            
//                    '<tpl for="parent">',
                    '<tpl if="this.isTrue(isSerialForProduct)==true">',  // serial 
                    '<th><h2><b>Serials</b></h2></th>',
                    '</tpl>',
//                    '</tpl>',
                
                    '<th ><h2><b>Quantity</b></h2></th>',
                    '</tr>',
                
                    '<tr><span  class="gridLine" style="width:85%;margin-left:45px;position: relative;top: 33px;"></span></tr>',
                
                    '<tpl for="stockDetails">',
                    '<tr>',
                    '<td style="padding-left:50px"><p>{#}</p></td>',
                    '<td ><p>{storeName}</p></td>',
                    '<td ><p>{locationName}</p></td>',
                
                    '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // row
                    '<td ><p>{rowName}</p></td>',
                    '</tpl>',
                    
                    '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // rack
                    '<td ><p>{rackName}</p></td>',
                    '</tpl>',
                    
                    '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // bin
                    '<td ><p>{binName}</p></td>',
                    '</tpl>',
                    
                    '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
                    '<td ><p>{batchName}</p></td>',
                    '</tpl>',
              
                    '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  //  serial 
                    '<td style="word-wrap:break-word;"><p>{serialNames}</p></td>',
                    '</tpl>',
               
                    '<tpl if="this.getQuantityDecimalPreciedValue(values.quantity)">',
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
                            return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        }
                    }
                    )   
                ,
                renderer : function(v, p, record){
//                    if(record.get("stockDetails") != "" && record.get("stockDetails") != undefined){ //means has stock detail data
//                        return  '<div class="x-grid3-row-expander">&#160;</div>'
//                    }else{
//                        //return '&#160;' 
//                        //return  '<div class="x-grid3-row-expander" onclick="showMessageBox(\'Select specific Store and specific Location to view detail.\')">&#160;</div>'
//                        return  '<div class="x-grid3-row-expander" onclick="">&#160;</div>'
//                    }
                    return  '<div class="x-grid3-row-expander" onclick="">&#160;</div>'
                },
                listeners: {
                    'beforeexpand': function(a,record, body, rowIndex){
//                        var stockDetail=record.get('stockDetails');
//                        if(stockDetail==undefined || stockDetail==""){
//                            showMessageBox('Select specific Store to view details.');
//                            return false;
//                        }
                    },
                    'beforecollapse': function(fieldset){
                    //alert('beforecollapse');
                    }
                }
           
            });
      
            var colArr = [
            //sm
            new Wtf.KWLRowNumberer(),
            this.expander,
            {
                header: WtfGlobal.getLocaleText("acc.product.threshold.grid.storecode"),
                dataIndex: 'storecode',
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.cosignmentloan.StoreDescription"),
                dataIndex: 'storedescription',
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex: 'itemcode',
                width:cmDefaultWidth,
                pdfwidth:100
            // sortable:true,
            //dbname:'sb_itemmaster.itemcode'
            },
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
                dataIndex: 'itemname',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),
                dataIndex: 'itemdescription',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.je.CoilcraftPartNo"),
                dataIndex: 'ccpartnumber',
                // sortable:true,
                hidden:true// integrationFeatureFor == true ? false: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
                dataIndex: 'shelflocation',
                // sortable:true,
                hidden: true //integrationFeatureFor == true ? false: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.MicrosKey"),
                dataIndex: 'microskey2',
                //  sortable:true,
                dbname:'sb_itemmaster.microskey2',
                hidden:true
            },
            //            {
            //                header: "Wincor Code",
            //                dataIndex: 'wincorkey',
            //                sortable:true,
            //                dbname:'sb_itemmaster.wincorkey'
            //            },
            {
                header: WtfGlobal.getLocaleText("acc.field.AvailableQuantity"),
                dataIndex: 'quantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                dbname:'sb_invetorylist.quantity',
                pdfwidth:50,
                renderer:function(v){
                    return parseFloat(getRoundofValue(v)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.UnderQA"),
                dataIndex: 'qaquantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                summaryType : 'sum',
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.activateQAApprovalFlow),
                renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                },
                summaryRenderer:function(v){
                    return "<b>"+v+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.UnderRepair"),
                dataIndex: 'repairquantity',
                sortable:false,
                align:"right",
                width:cmDefaultWidth,
                summaryType : 'sum',
                pdfwidth:50,
                hidden:!(Wtf.account.companyAccountPref.activateQAApprovalFlow),
                renderer:function(val){
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)
                },
                summaryRenderer:function(v){
                    return "<b>"+v+"</b>" //WtfGlobal.getCurrencyFormatWithoutSymbol(v,  Wtf.companyPref.quantityDecimalPrecision)
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.report.rule16register.UOM"),
                dataIndex: 'uom',
                width:cmDefaultWidth,
                pdfwidth:50
            //sortable:true,
            //dbname:'sb_itemconfigdata.name'
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.DIO"),
                dataIndex: 'dio',
                //sortable:false,
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.stockType_ReusableOrNonReusable"),
                dataIndex: 'stockTypeName',
                width:cmDefaultWidth,
                align:'center',
                pdfwidth:30,
                renderer:function(val, m ,r){
                    if(r.get('stockType') == "REUSABLE"){
                        return "<div wtf:qtip='Reusable'>"+val+"</div>"
                    }else if(r.get('stockType') == "DISPOSABLE"){
                        return "<div wtf:qtip='Consumable'>"+val+"</div>"
                    }else {
                        return "<div wtf:qtip='Consumable'>"+val+"</div>"
                    }
                }
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
            this.objsearchComponent=new Wtf.advancedSearchComponent({
                cm:this.cm
            });

            this.AdvanceSearchBtn = new Wtf.Button({
                text : "Advanced Search",
                scope : this,
                tooltip:'Manage search with your preference',
                handler : this.configurAdvancedSearch,
                iconCls :'accountingbase fetch'
            });

            var tbararr = new Array();
            tbararr.push("-",WtfGlobal.getLocaleText("acc.het.322")+":",this.dateFilter);
            tbararr.push("-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+":",this.storeCmb);
            if(trackStoreLocation){
                tbararr.push("-",WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add")+":",this.locCmb);
            }
            tbararr.push("-",this.search);//,"-",this.AdvanceSearchBtn);
            tbararr.push("-",this.resetBtn);

            this.summaryBtn = new Wtf.Button({
                anchor : '90%',
                text: 'View Summary',
                scope:this,
                hidden: (true == 5 || true == 17 || true == 18),
                handler:function(){
                    this.storeCmb.setValue("");
                    if(this.status==0) {
                        this.storeCmb.disable();
                        this.cm.setHidden(2, true);
                        this.summaryBtn.setText("View Details");
                        this.status = 1;
                    } else {
                        this.status = 0;
                        this.storeCmb.enable();
                        this.cm.setHidden(2, false);
                        this.summaryBtn.setText("View Summary");
                    }
                    if(trackStoreLocation){                  
                        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
                    }else{
                        this.initloadgridstore(this.storeCmb.getValue());
                    }
                }
            });
            this.exportButton=new Wtf.exportButton({
                obj:this,
                tooltip:"Export Report",  //"Export Report details.",  
                params:{
                    name: "Datewise Stock Tracking ("+this.dateFilter.getRawValue()+")"
                },
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true
                },
                get:Wtf.autoNum.DateWiseStockTrackingReport,
                label:"Export"
            })
            this.grid=new  Wtf.KwlEditorGridPanel({
                region:'center',
                cm:this.cm,
                store:this.ds,
                sm:sm,
                loadMask:true,
                autoscroll:true,
                viewConfig: {
                    forceFit: false
                },
                //view: grpView,
                plugins : this.expander,
                searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
                displayInfo:true,
                searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.EnterProductIDProductNameSerialName"),
                serverSideSearch:true,
                //advanceSearch:true,
                tbar:tbararr,
                bbar:[this.exportButton]
            });

            //If you want to enable button ,if only one record selected ,otherwise disable
            //        var arrId=new Array();
            //        arrId.push("delete");//"deleteIssueBtn" id of button
            //        arrId.push("edit");
            //
            //        enableDisableButton(arrId,this.ds,sm);
            this.innerPanel = new Wtf.Panel({
                layout : 'border',
                bodyStyle :"background-color:transparent;",
                border:false,
                items : [this.grid,this.objsearchComponent]

            });
            Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
            },this);
            this.add(this.innerPanel);
            this.doLayout();
            this.objsearchComponent.advGrid.on("filterStore",this.filterStore, this);
            this.objsearchComponent.advGrid.on("clearStoreFilter",this.clearStoreFilter, this);
            
            this.expander.on('expand',this.onRowexpand,this);
        }, function () {

            });
    },
    onRowexpand:function(scope, record, body){
        Wtf.Ajax.requestEx({
                url: "INVStockLevel/getStockDetailByDateForProduct.do",
                params: {
                    productId:record.get("itemid"),
                    store:this.storeCmb.getValue(),
                    location:this.locCmb.getValue(),
                    ondate: this.dateFilter.getValue().format("Y-m-d")
                }
            },
            this,
            function(action, response){
                if(action.success == true){
                    var data=action.data;
                    this.expander.tpl.overwrite(body, data);
                }else{
                    WtfComMsgBox(["Error", "Some error has occurred while processing your request."],0);
                }
            },
            function(){
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
    initloadgridstore:function(storetype,locationtype){
        WtfGlobal.setAjaxTimeOut();
        this.ds.baseParams = {
            ondate: this.dateFilter.getValue().format("Y-m-d"),
            store:storetype,
            location:locationtype,
            searchJson:this.searchJson
        }
        this.ds.load(
        {
            params: {
                start:0,
                limit:30,//Wtf.companyPref.recperpage,
                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()

            }
        });
    },

    configurAdvancedSearch:function(){
        this.objsearchComponent.show();
        this.doLayout();
    },
    filterStore:function(json){
        this.searchJson=json;
        if(trackStoreLocation){        
            this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
        }else{
            this.initloadgridstore(this.storeCmb.getValue());
        }    
    },
    clearStoreFilter:function(){
        this.objsearchComponent.hide();
        this.doLayout();
        this.searchJson="";
        if(trackStoreLocation){        
            this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
        }else{
            this.initloadgridstore(this.storeCmb.getValue());
        }
    }
    
});
function showMessageBox(msg){
    Wtf.MessageBox.show({
        title:"Info",
        msg: msg,
        icon:Wtf.MessageBox.INFO,
        buttons:Wtf.MessageBox.OK
    });
}



Wtf.SerialNoWindow = function(config) {
    this.selectedRec=config.selectedRec;
    Wtf.apply(this, config);
    this.butnArr = new Array();
    this.saveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        toolTip: WtfGlobal.getLocaleText("acc.rem.175"),
        scope: this,
        handler: this.saveSerialDetails,
        iconCls: 'pwnd save'
    });
    this.butnArr.push(this.saveBttn);
    this.storeRec = Wtf.data.Record.create([
    {
        name:'serialid',
        defValue:null
    },

    {
        name:'productname'
    },

    {
        name:'itemid'
    },

    {
        name:'quantity'
    },

    {
        name:'quantitydue'
    },

    {
        name:'batchid'
    },

    {
        name:'batchname'
    },

    {
        name:'ispurchase'
    },

    {
        name:'transactiontype'
    },

    {
        name:'lockquantity'
    },

    {
        name:'isForconsignment'
    },

    {
        name:'serialname'
    },

    {
        name: 'isLocationForProduct'
    },

    {
        name: 'isWarehouseForProduct'
    },

    {
        name: 'isBatchForProduct'
    },

    {
        name: 'isSerialForProduct'
    },

    {
        name: 'location'
    },

    {
        name: 'warehouse'
    },

    {
        name: 'newserialname',
        defValue:''
    },

    {
        name: 'isserialupdate',
        defValue:false
    }
            
    ]);
    var url='INVStockLevel/getProductwiseSerialList.do';
        
    this.Store = new Wtf.data.Store({
        url:url,
        pruneModifiedRecords:true,
        baseParams:{
            itemid :this.selectedRec[0].data.itemid
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.storeRec)
    });
    //this.Store.load();
        
       
        
    this.storeCmbRecord = new Wtf.data.Record.create([
    {
        name: 'store_id'
    },

    {
        name: 'abbr'
    },
    {
        name: "fullname"
    },

    {
        name: 'description'
    }
    ]);

    this.storeCmbStore = new Wtf.data.Store({
        url: 'INVStore/getStoreList.do',
        baseParams:{
            isActive :true,
            byStoreManager:'true'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },this.storeCmbRecord)
    });
        
    this.storeCmb = new Wtf.form.ComboBox({
        fieldLabel : 'Store*',
        hiddenName : 'storeid',
        store : this.storeCmbStore,
        typeAhead:true,
        displayField:'fullname',
        valueField:'store_id',
        mode: 'local',
        width : 125,
        triggerAction: 'all',
        emptyText:'Select store...'
    // allowBlank:false
    });
        
    var trackStoreLocation=true;
        
    if(trackStoreLocation){
            
        this.locCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },        

        {
            name: 'name'
        }]);

        this.locCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });

        this.locCmbStore.on("beforeload", function(ds, rec, o){
            this.locCmbStore.removeAll();
            this.locCmb.reset(); 
        }, this);
            

        this.locCmb = new Wtf.form.ComboBox({
            hiddenName : 'locationid',
            store : this.locCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 125,
            triggerAction: 'all'
        //emptyText:'Select location...',
        //allowBlank:false
        });
    }   
        
       
    this.storeCmbStore.load();
        
    this.storeCmb.on("select",function(){
        this.locCmbStore.load({
            params:{
                storeid:this.storeCmb.getValue()
            }
        }) 
            
    },this);
        
      
    this.locCmbStore.on("load", function(ds, rec, o){
        var newRec=new this.locCmbRecord({
            id:'',
            name:'ALL'
        })
        this.locCmbStore.insert(0,newRec);
        this.locCmb.setValue('');
    }, this);

    this.storeCmbStore.on("load", function(ds, rec, o){
        var newRec=new this.storeCmbRecord({
            store_id:'',
            fullname:'ALL'
        })
        this.storeCmbStore.insert(0,newRec);
        this.storeCmb.setValue('');
        this.storeCmb.fireEvent('select');
        this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
            
    }, this);
        
    this.resetBtn = new Wtf.Button({
        anchor : '90%',
        text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
        tooltip: {
            text:WtfGlobal.getLocaleText("acc.inventory.QAAproval.Clickheretoresetfilter")
        },
        iconCls:getButtonIconCls(Wtf.etype.resetbutton),
        scope:this,
        handler:function(){
            
            Wtf.getCmp("Quick"+this.updateserialGrid.id).setValue("");
            this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
            this.storeCmb.fireEvent('select');
            if(trackStoreLocation){
                // this.locCmb.setValue(this.locCmb.store.data.items[0].data.id);
                this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
            }else{
                this.initloadgridstore(this.storeCmb.getValue());
            }
                
        }
    });

    this.search = new Wtf.Button({
        anchor : '90%',
        text: 'Search',
        tooltip: {
            text:"Click to Search"
        },
        iconCls : 'accountingbase fetch',
        scope:this,
        handler:function(){
            if(trackStoreLocation){                  
                this.initloadgridstore(this.storeCmb.getValue(),this.locCmb.getValue());
            }else{
                this.initloadgridstore(this.storeCmb.getValue());
            }   
        }
    });
        
    var cm = new Wtf.grid.ColumnModel([
        this.rowNo=new Wtf.grid.RowNumberer(),
        {
            header: "Batch Number",
            dataIndex: "batchname",
            sortable:true
        },
        {
            header: "Existing Serial No",
            dataIndex: "serialname"
        },
        {
            header: "New Serial No",
            dataIndex: 'newserialname',
            editor : new  Wtf.form.TextField({
                name:'newserialname',
                maxLength:50
            })
        }
        ]);
        
    this.itemcode='('+this.selectedRec[0].data.itemcode+')';
    var tbararr=new Array();
    tbararr.push("-");
    tbararr.push("Store:");
    tbararr.push(this.storeCmb);
    tbararr.push("-");
    tbararr.push("Location");
    tbararr.push(this.locCmb);
    tbararr.push("-");
    tbararr.push(this.search);
    tbararr.push("-");
    tbararr.push(this.resetBtn);
        
    this.updateserialGrid = new Wtf.KwlEditorGridPanel({
        clicksToEdit:1,
        autoScroll:true,
        autoWidth:true,
        store: this.Store,
        cm:cm,
        border : false,
        searchLabel:"acc.dnList.searchText",
        displayInfo:true,
        searchEmptyText:WtfGlobal.getLocaleText("Enter batch, serial"),
        serverSideSearch:true,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        tbar:tbararr,
        bbar:[this.butnArr]
    });
        
    Wtf.getCmp("paggintoolbar"+this.updateserialGrid.id).on('beforerender',function(){
        Wtf.getCmp("paggintoolbar"+this.updateserialGrid.id).pageSize=30;
    },this);

    this.updateserialGrid.on('afteredit',this.checkrowforupdate,this);
    this.initloadgridstore();
    
    Wtf.SerialNoWindow.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.SerialNoWindow,Wtf.Panel, {
    onRender: function(config) {
        
        this.add(this.updateserialGrid);
        
        Wtf.SerialNoWindow.superclass.onRender.call(this,config);
    },
    initloadgridstore:function(storetype,locationtype){//majortype,itemtype,

        this.Store.baseParams = {
            store:storetype,
            location:locationtype,
            itemid :this.selectedRec[0].data.itemid
        }
        this.Store.load(
        {
            params: {
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.updateserialGrid.id).pageSize,//30,//Wtf.companyPref.recperpage,
                ss:  Wtf.getCmp("Quick"+this.updateserialGrid.id).getValue()

            }
        });
    },
    getProductDetails:function(){

        var arr=[];
        this.Store.each(function(rec){
            if(rec.data.itemid!=""){
                //                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
                arr.push(this.Store.indexOf(rec));
            }
        },this)
        var jarray=WtfGlobal.getJSONArray(this.updateserialGrid,true,arr);
        return jarray;
    },
    checkrowforupdate: function(obj){
        if(obj!=null){
            var comma=",";
            var rec=obj.record;
            if(obj.field=="newserialname"){
                if ((obj.value).match(comma)) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batchserialwindow.enterSerialNameWithoutComma")], 0);
                    rec.set("newserialname", "");
                    return false;
                }
                if(obj.value==rec.data.serialname){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.serialnumberalreadyexit")], 2); 
                    obj.record.set("newserialname","");
                    obj.record.set("isserialupdate",false);
                } else if((rec.data.quantitydue-rec.data.lockquantity)==0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.serialnumberalreadyused")], 2); 
                    obj.record.set("newserialname","");
                    obj.record.set("isserialupdate",false);
                }else if(this.isSameBatchSerialInGrid(obj.grid, rec.data.batchname, obj.value)){ // grid ,batchname,newserialname
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"This Serial and Batch is already present."], 2); 
                    obj.record.set("newserialname","");
                    obj.record.set("isserialupdate",false);
                }else{
                    var duplicateCount=0;
                    Wtf.Ajax.requestEx({
                        url:"ACCMaster/checkDuplicateSerialforProduct.do",
                        params:{
                            productid:rec.data.itemid,
                            batchid:rec.data.batchid,
                            serialname:obj.value
                        }
                    }, this,function(response){
                        duplicateCount =response.duplicateCount;
                        if(duplicateCount>0){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.product.serialnumberalreadyexit")], 2); 
                            obj.record.set("newserialname","");
                            obj.record.set("isserialupdate",false);
                        }else{
                            obj.record.set("newserialname",obj.value);
                            obj.record.set("isserialupdate",true);
                        } 

                    }, function(){

                        });
                }
                
                
            }
        }
      
    },
    saveSerialDetails: function(){ //rec,detail,incash
        var modRecLength=this.updateserialGrid.getStore().getModifiedRecords().length;
        if(modRecLength > 0){
            var detailsArr=this.getProductDetails();
            this.ajxurl='INVStockLevel/updateSerialNames.do';
            Wtf.Ajax.requestEx({
                url:this.ajxurl,
                params:{
                    detailsArr:detailsArr
                }                    
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },
    genSuccessResponse:function(response, request){
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
        var inventoryTab1 = Wtf.getCmp("storewiseInventoryLevel");  
        if(inventoryTab1!=null){
            inventoryTab1.ds.load({
                params: {
                    start:0,
                    limit:30  //Wtf.companyPref.recperpage,
                //                ss:  Wtf.getCmp("Quick"+this.grid.id).getValue()

                }
            });
        }
    },
    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    isSameBatchSerialInGrid:function(grid,batch,newSerial){
        var totalRecs=grid.store.getTotalCount();
        var duplicateCnt=0;
        for(var i=0 ;i < totalRecs ; i++){
            var rec=grid.store.getAt(i);
             if (rec != null && rec != undefined && rec != "") {
            if(rec.data.batchname == batch && rec.data.newserialname==newSerial){
                duplicateCnt += 1;
            }
        }
        }
        if(duplicateCnt >1){
            return true;
        }else{
            return false;
        }
    }
});
