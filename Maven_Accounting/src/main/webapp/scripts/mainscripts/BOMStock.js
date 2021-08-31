/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
Wtf.common.BOMStockReport = function(config){
    Wtf.common.BOMStockReport.superclass.constructor.call(this,config);
    
};
Wtf.extend(Wtf.common.BOMStockReport,Wtf.Panel,{
    onRender : function(config){
        var btnArr=[], bottomArr=[] ;
        Wtf.common.BOMStockReport.superclass.onRender.call(this,config);
        this.auditRecord = Wtf.data.Record.create([
            {
                name:'productid'
            },
            {
                name:'productname'
            },
            {
                name:'quantity'
            },
            {
                name:'description'
            },
            {
                name:'mainproductid'
            },
            {
                name:'entrydate',type:'date'
            },
            {
                name: 'warehouse'
            },
            {
                name: 'location'
            },
            {
                name: 'batch'
            },
            {
                name: 'serial'
            },
            {
                name: 'billid' 
            },
            {
                name: 'bomCode'
            },
            {
                name: 'availableQty'
            },
            {
                name: 'consumedQty'
            },
            {
                name: 'buildQty'
            }
        ]);
        this.auditReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"totalCount"
        }, this.auditRecord);
    
    // Store containing records of bom report
        this.store = new Wtf.data.Store({
            proxy: new Wtf.data.HttpProxy({
                url:"ACCProduct/getAssemblyProductsWithBOM.do"
                
            }),
            reader: this.auditReader
        });
        
        this.expander = new Wtf.grid.RowExpander({});
        this.sm = new Wtf.grid.CheckboxSelectionModel({
    	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>',    // For IE 7 the all select option not available
        multiSelect:true
        });
        this.sm.on("rowselect", function (sm) {
            if (sm.hasSelection()) {
                if(this.singleRowPrint){
                    this.singleRowPrint.enable();
                }
            }
        }, this);
        this.sm.on("rowdeselect", function (sm) {
            if (!sm.hasSelection()) {
                if(this.singleRowPrint){
                    this.singleRowPrint.disable();
                }
            }
        }, this);
        // Column Model for main grid of Report
        this.cmodel = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),this.sm,this.expander,
        {
            header: WtfGlobal.getLocaleText("acc.product.gridProductID"),
            tip: WtfGlobal.getLocaleText("acc.product.gridProductID"),
            width: 100,
            pdfwidth:80,
            dataIndex: 'productname'
        },{
            header: WtfGlobal.getLocaleText("acc.product.description"),
            tip: WtfGlobal.getLocaleText("acc.product.description"),
            align:'left',
            width: 100,
            pdfwidth:80,
            dataIndex: 'description',
            groupable:true
        },{
            header: WtfGlobal.getLocaleText("acc.field.bomCode"),
            tip: WtfGlobal.getLocaleText("acc.field.bomCode"),
            align:'center',
            pdfwidth:80,
            width: 100,
            dataIndex: 'bomCode'
        },{
            header: WtfGlobal.getLocaleText("acc.bom.buildQuantity"),
            tip: WtfGlobal.getLocaleText("acc.bom.buildQuantity"),
            width: 120,
            pdfwidth:80,
            dataIndex: 'buildQty',
             renderer:function(val){
                       return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?" ":parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    }
        },{
            header: WtfGlobal.getLocaleText("acc.bom.consumedQuantity"),
            tip: WtfGlobal.getLocaleText("acc.bom.consumedQuantity"),
            width: 120,
            pdfwidth:80,
            dataIndex: 'consumedQty',
             renderer:function(val){
                       return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?" ":parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    }
        },{
            header: WtfGlobal.getLocaleText("acc.bom.availableQuantity"),
            tip: WtfGlobal.getLocaleText("acc.bom.availableQuantity"),
            width: 120,
            pdfwidth:80,
            dataIndex: 'availableQty',
             renderer:function(val){
                       return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?" ":parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    }
        }]);
    
    // Grid for expander
    this.gridRec = Wtf.data.Record.create ([
        {
            name:'id'
        },

        {
            name:'productid'
        },

        {
            name:'productname'
        },

        {
            name:'desc'
        },
        {
            name:'producttype'
        },

        {
            name:'type'
        },

        {
            name:'onhand'
        },

        {
            name:'quantity'
        },
        {
            name:'recyclequantity'
        },
        {
            name:'inventoryquantiy'
        },

        {
            name:'lockquantity'
        },

        {
            name:'total'
        },
        {name: 'wastageInventoryQuantity'},
        {name: 'warehouse'},
        {name: 'location'},
        {name: 'batch'},
        {name: 'serial'},
        {name: 'wastageQuantityType'},
        {name: 'wastageQuantity'}
        ]);
        //Expander Store contains records of stock bom according to warehouse
        this.expandStore = new Wtf.data.Store({
            url:"ACCProduct/getProductBatchDetails.do",
            baseParams:{
                mode:25
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gridRec)
        });

        //Store combo grid
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
            
        //  To fetch store list of company
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
        
        // Store Combo
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
                    '</tpl>'),
               allowBlank:false
            });
        
        this.storeCmbStore.load();
        
        this.storeid = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:WtfGlobal.getLocaleText("acc.jobworkin.create.Store") + '*' ,
         extraFields:['store_id','abbr'],
         extraComparisionField:'store_id',
         listWidth:Wtf.ProductComboListWidth, 
         forceSelection:true,         
         width:240
    },this.storeCmb));
    
    
    this.storeCmbStore.on("load", function(ds, rec, o){
                if(rec.length > 0){
                    var newRec=new this.storeCmbRecord({
                        store_id:'ALL',
                        fullname:'ALL'
                        
                    })
                    this.storeCmbStore.insert(0,newRec);
                    this.storeCmb.setValue('ALL');
                    if(rec.length==1){
                        this.storeCmb.setValue(rec[0].data.store_id);
                    }
                    this.storeCmb.fireEvent('select');
                }
            }, this);
            this.storeCmbStore.load();

        // Main Grid 
        this.grid=new Wtf.grid.GridPanel({
            ds: this.store,
            id:'buildreportgridid'+this.id,
            cm: this.cmodel,
            sm:this.sm,
            border: false,
            plugins:[this.expander],
            viewConfig:{
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                },
            view: new Wtf.ux.KWLGridView({
                forceFit:true
            }),
            trackMouseOver: true,
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            }
        });
        
        this.grid.on("render",function(grid,layout){
            // Applying loading mask for auto loading report
            if(grid.store.requestInProgress && grid.loadMask && grid.bwrap){
                grid.loadMask = new Wtf.LoadMask(grid.bwrap,
                    Wtf.apply({
                        store:grid.store
                        }, grid.loadMask));
                grid.loadMask.onBeforeLoad();
            }   
        });
        //Expander Store
        this.expandStore.on('load',this.fillExpanderBody,this);
        this.expander.on("expand",this.onRowexpand,this);
        this.grid.on('cellclick', this.onCellClick, this);
        this.cmodel.defaultSortable = true;
    
        this.comboReader = new Wtf.data.Record.create([
        {
            name: 'id',
            type: 'string'
        },{
            name: 'name',
            type: 'string'
        }
        ]);

        this.groupRecord = Wtf.data.Record.create([
        {
            name: 'groupname',
            type: 'string'
        },{
            name: 'groupid',
            type: 'string'
        }
        ]);

        this.groupReader = new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.groupRecord);
        
        // Reset button to reset search result
        this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            tooltip :WtfGlobal.getLocaleText("acc.auditTrail.resetTip"),  //'Reset Search Result.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        
        // Search button to fetch records
        this.searchBttn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            scope: this,
            id:"search"+this.helpmodeid,
            handler: this.searchHandler,
            iconCls:'accountingbase fetch'
        });
        

    
        this.fT = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.bomList.searchText"),//'Search by Product Name',
            width: 130,
            id:"quicksearch"+this.helpmodeid,
            field: 'productname',
            Store:this.store
        });
                            
        this.reader = new Wtf.data.JsonReader({
            root: 'data',
            fields: [{
                name: 'id'
            }, {
                name: 'name'
            }]
        });
        
        // Export Button
      this.exportButton=new Wtf.exportButton({
            obj:this,
            id:"exportReports"+this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            filename: WtfGlobal.getLocaleText("acc.productList.bomWiseStockReport")+"_v1",
            disabled :true,
            scope : this,
            hidden:false,
            menuItem:{csv:true,pdf:true,xls:true},
            get:Wtf.autoNum.BOMAssemblyExport
    });
    this.exportButton.enable();
     
     // Panel containing tbar and bbar
        var innerPanel = new Wtf.Panel({
            border : false,
            layout:'fit',
            items:[this.grid],
            tbar: [ "   ",this.fT,"-",WtfGlobal.getLocaleText("acc.jobworkin.create.Store") + ": ",this.storeCmb,
            this.searchBttn,"-", 
            this.resetBttn,"-", this.exportButton,
            btnArr,"->",
            getHelpButton(this,this.helpmodeid)
            ],
            bbar: new Wtf.PagingSearchToolbar({
                id: 'pgTbar' + this.id,
                searchField: this.fT,
                pageSize: 30,
                store: this.store,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id: 'detailspPageSize_' + this.id,
                })
//                items: ["-",
//                    this.singleRowPrint = new Wtf.exportButton({
//                        obj: this,
//                        id: "printSingleRecord" + this.id,
//                        iconCls: 'pwnd printButtonIcon',
//                        text: WtfGlobal.getLocaleText("acc.rem.236"),
//                        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record details',
//                        disabled: true,
//                        filename: WtfGlobal.getLocaleText("acc.productList.buildAssembly"),
//                        menuItem: {rowPrint: true},
//                        get: Wtf.autoNum.BOMAssemblyExport,
//                        moduleid: Wtf.BOM_Wise_Stock_Report
//                    })
//                ]
            })
        });
        
        this.store.on('load', function() {
            this.store.requestInProgress = undefined;
            if(this.store.getCount()<1) {
                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
        }, this);
        
        this.store.on('beforeload', function() {
            this.store.baseParams = this.store.baseParams || {};
//            this.store.baseParams.searchOnField = this.searchFieldSelectionCmb.getValue();
            this.store.baseParams.ss = this.fT.getValue();
           // this.store.baseParams.storeId = this.storeid.getValue();
        }, this);
        
        this.store.on('datachanged', function() {
            var p = this.pP.combo.value;
            this.fT.setPage(p);
        }, this);
        this.add(innerPanel);
        this.store.baseParams = {
            search:this.fT.getValue()
        };
        /*
         *Please remove below flag while removing this.store.load. 
         *This flag is used to apply loading mask for 1st time loading.
         */
        this.store.requestInProgress = true;
        this.store.load({
            params: {
                start:0,
                limit:30
            }
        });
        
    },
    
    // Function to handle reset operation
    handleResetClick:function(){
        this.fT.reset();
        this.storeCmb.setValue(this.storeCmb.store.data.items[0].data.store_id);
        this.store.baseParams = {
            mode:201,
            groupid:'',
            search:''
        };
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    },
    

    
    onRowexpand:function(scope, record, body){
         this.expanderBody=body;
        this.expandStore.load({
            params:{
                productid:record.data.productid
                }
            });
},
    fillExpanderBody:function(){
        var disHtml = "";
        var arr=[];
  
        arr=[WtfGlobal.getLocaleText("acc.invoiceList.expand.pName") ,WtfGlobal.getLocaleText("acc.rem.prodDesc.Mixed"),WtfGlobal.getLocaleText("acc.assembly.cost"),WtfGlobal.getLocaleText("acc.product.gridType"),
            WtfGlobal.getLocaleText("acc.product.gridQtyneeded"),WtfGlobal.getLocaleText("acc.product.inventoryQuantity"),WtfGlobal.getLocaleText("acc.product.recyclequantityused"),WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"),WtfGlobal.getLocaleText("acc.assetdepriciation.grid.BatchName"),WtfGlobal.getLocaleText("acc.field.SerialNo")];
        if (Wtf.account.companyAccountPref.activateWastageCalculation) {
            arr.push(WtfGlobal.getLocaleText("acc.field.wastageQuantity"));
        }
        var arrayLength = arr.length + 1;
        var width = (arrayLength * 140) + 250;
        var widthInPercent = (100 / arrayLength);
        if(this.grid && this.grid.el){
            width = this.grid.el.dom.clientWidth || this.grid.el.dom.offsetWidth; 
        }

        /*Header Section*/

        var header = "<span class='gridHeader'>" + WtfGlobal.getLocaleText("acc.product.assembly.comp") + "</span>";   //Product List
        header += "<div style='display:table !important;width:" + width + "px'>";
        
        header += "<span class='gridNo' style='font-weight:bold;'>S.No.</span>";
        for (var i = 0; i < arr.length; i++) {
            header += "<span class='headerRow' style='width:" + widthInPercent + "%;' wtf:qtip='" + arr[i] + "'>" + Wtf.util.Format.ellipsis(arr[i], 20) + "&nbsp;</span>";
//            arr.push(arr[i]);
        }
        header += "</div>";

        //Values Section
        header += "<div style='width:" + width + "px;'><span class='gridLine' style='height: 0px;'></span></div>";
            
            
        for(var i=0;i<this.expandStore.getCount();i++){
            header += " <div style='width:" + width + "px;display:table !important;height: 22px;'>";
            var rec=this.expandStore.getAt(i);
            var productname= rec.data['productname'];
            var description= rec.data['desc'];
            var type= rec.data['type'];
            var qtyneeded= rec.data['quantity'];
            var recyclequantity= rec.data['recyclequantity'];
            var inventoryquantiy= rec.data['inventoryquantiy'];
            var wastageQuantityType = rec.data['wastageQuantityType'];
            var warehouse = rec.data['warehouse'];
            var location = rec.data['location'];
            var batch = rec.data['batch'];
            var serial = rec.data['serial'];
            var wastageQuantitySubStr = "";
            if (wastageQuantityType == 0) { // For Flat wastage quantity
                wastageQuantity = parseFloat(getRoundofValue(wastageQuantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            } else { // For Percentage wastage quantity
                wastageQuantitySubStr = "%";
            }
            var wastageQuantity = rec.data['wastageQuantity'];
            
            qtyneeded= parseFloat(getRoundofValue(qtyneeded)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            recyclequantity= parseFloat(getRoundofValue(recyclequantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            inventoryquantiy= parseFloat(getRoundofValue(inventoryquantiy)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var purchaseprice= rec.data['purchaseprice'];
            purchaseprice= WtfGlobal.currencyRenderer(purchaseprice,[true]);
            if(!Wtf.dispalyUnitPriceAmountInSales || !Wtf.dispalyUnitPriceAmountInPurchase){
                purchaseprice=Wtf.UpriceAndAmountDisplayValue;
            }
            header += "<span class='gridNo'>"+(i+1)+".</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;'  wtf:qtip='"+productname+"'>"+Wtf.util.Format.ellipsis(productname,15)+"&nbsp;</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;' wtf:qtip='"+description+"'>"+Wtf.util.Format.ellipsis(description,15)+"&nbsp;</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;'>"+purchaseprice+"</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;' wtf:qtip='"+type+"'>"+Wtf.util.Format.ellipsis(type,15)+"&nbsp;</span>";           
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;'>"+qtyneeded+"</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;'>"+inventoryquantiy+"</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;'>"+recyclequantity+"</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;' wtf:qtip='"+warehouse+"'>"+Wtf.util.Format.ellipsis(warehouse,15)+"&nbsp;</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;' wtf:qtip='"+location+"'>"+Wtf.util.Format.ellipsis(location,15)+"&nbsp;</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;' wtf:qtip='"+batch+"'>"+Wtf.util.Format.ellipsis(batch,15)+"&nbsp;</span>";
            header += "<span class='gridRow' style='width:" + widthInPercent + "%;' wtf:qtip='"+serial+"'>"+Wtf.util.Format.ellipsis(serial,15)+"&nbsp;</span>";
            if (Wtf.account.companyAccountPref.activateWastageCalculation) {
                header += "<span class='gridRow' style='width:" + widthInPercent + "%;'>" + wastageQuantity + wastageQuantitySubStr + "</span>";
            }
            
            header += "</div>";
        }
        disHtml += "<div class='expanderContainer' style='width:100%;overflow: auto;'>" + header + "</div>";
    
        this.expanderBody.innerHTML = disHtml;
    },
searchHandler: function() {
    
    //  Check valid Products Selected or Not
            
//        var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.productname);
//        if(isInvalidProductsSelected){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
//            return;
//        }
    
    
    this.store.removeAll();
    this.store.baseParams = {
        mode:201,
        
        search:this.fT.getValue(),
        storeid :  this.storeCmb.getValue()
    };
    this.store.load({
        params: {
            start:0,
            limit:this.pP.combo.value
        }
    });
}
});

