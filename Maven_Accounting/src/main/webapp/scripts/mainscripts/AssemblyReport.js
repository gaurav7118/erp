/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
Wtf.common.AssemblyProductReport = function(config){
    Wtf.common.AssemblyProductReport.superclass.constructor.call(this,config);
    this.isUnbuildAssembly = config.isUnbuildAssembly;
};

Wtf.extend(Wtf.common.AssemblyProductReport,Wtf.Panel,{
    onRender : function(config){
        var btnArr=[], bottomArr=[] ;
        Wtf.common.AssemblyProductReport.superclass.onRender.call(this,config);
       
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
                name:'productrefno'
            },
            {
                name:'memo'
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
                name: 'journalentryid'
            },
            {
                name: 'entryno'
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
                name: 'billid' //ERM-26 To use generic function added billid in Build Assembly Report Store
            },
            {
                name: 'bomCode'
            }
        ]);
    
    
        this.auditReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"totalCount"
        }, this.auditRecord);
    
        this.store = new Wtf.data.Store({
            proxy: new Wtf.data.HttpProxy({
                url:"ACCProduct/getAssemblyProductsWithSerials.do",
                baseParams:{isUnbuildAssembly:this.isUnbuildAssembly}
            }),
            reader: this.auditReader
        });
        this.expander = new Wtf.grid.RowExpander({});
        this.sm = new Wtf.grid.CheckboxSelectionModel({
    	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>',    // For IE 7 the all select option not available
        multiSelect:true
        });
        this.sm.on("rowselect", function (sm) {//ERM-26 Print button row select enable
            if (sm.hasSelection()) {
                if(this.singleRowPrint){
                    this.singleRowPrint.enable();
                }
            }
        }, this);
        this.sm.on("rowdeselect", function (sm) {//ERM-26 Print button row deselect disable
            if (!sm.hasSelection()) {
                if(this.singleRowPrint){
                    this.singleRowPrint.disable();
                }
            }
        }, this);
        this.cmodel = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),this.sm,this.expander,
        {
            header: this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.product.unbuild.assembly") : WtfGlobal.getLocaleText("acc.product.assembly"),
            tip:this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.product.unbuild.assembly"): WtfGlobal.getLocaleText("acc.product.assembly"),
            width: 240,
            pdfwidth:80,
            dataIndex: 'productname'
        },{
            header: this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.product.unbuild.assemblyrefno") : WtfGlobal.getLocaleText("acc.product.assemblyrefno"),
            tip:this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.product.unbuild.assemblyrefno") : WtfGlobal.getLocaleText("acc.product.assemblyrefno"),
            width: 160,
            pdfwidth:80,
            align:'center',
            dataIndex: 'productrefno'
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.jeno"), // "Journal Entry No",
            dataIndex: 'entryno',
            width: 150,
            pdfwidth: 80,
            renderer: WtfGlobal.linkDeletedRenderer
        },{
            header: this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuildassembly.gridBuildDate") : WtfGlobal.getLocaleText("acc.buildassembly.gridBuildDate"),
            tip:this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuildassembly.gridBuildDate") : WtfGlobal.getLocaleText("acc.buildassembly.gridBuildDate"),
            dataIndex:'entrydate',
            align:'center',
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.product.unbuild.assemblyquantity") : WtfGlobal.getLocaleText("acc.product.assemblyquantity"),
            tip:this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.product.unbuild.assemblyquantity") : WtfGlobal.getLocaleText("acc.product.assemblyquantity"),
            width: 120,
            pdfwidth:80,
            dataIndex: 'quantity',
             renderer:function(val){
                       return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?" ":parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    }
        },{
            header: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
            tip: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
            width: 100,
            pdfwidth:80,
            renderer : function (val){
                if(!val){
                    return val;
                }else{
                    return "<span class='gridRow'  wtf:qtip='"+val+"'>"+Wtf.util.Format.ellipsis(val,15)+"&nbsp;</span>"
                }
            },
            dataIndex: 'warehouse'
        },{
            header: WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"),
            tip: WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"),
            width: 100,
            pdfwidth:80,
            renderer : function (val){
                if(!val){
                    return val;
                }else{
                    return "<span class='gridRow'  wtf:qtip='"+val+"'>"+Wtf.util.Format.ellipsis(val,15)+"&nbsp;</span>"
                }
            },
            dataIndex: 'location'
        },{
            header: WtfGlobal.getLocaleText("acc.assetdepriciation.grid.BatchName"),
            tip: WtfGlobal.getLocaleText("acc.assetdepriciation.grid.BatchName"),
            width: 100,
            pdfwidth:80,
            renderer : function (val){
                if(!val){
                    return val;
                }else{
                    return "<span class='gridRow'  wtf:qtip='"+val+"'>"+Wtf.util.Format.ellipsis(val,15)+"&nbsp;</span>"
                }
            },
            dataIndex: 'batch'
        },{
            header: WtfGlobal.getLocaleText("acc.field.SerialNo"),
            tip: WtfGlobal.getLocaleText("acc.field.SerialNo"),
            width: 100,
            pdfwidth:80,
            renderer : function (val){
                if(!val){
                    return val;
                }else{
                    return "<span class='gridRow'  wtf:qtip='"+val+"'>"+Wtf.util.Format.ellipsis(val,15)+"&nbsp;</span>"
                }
            },
            dataIndex: 'serial'
        },{
            header: WtfGlobal.getLocaleText("acc.product.description"),
            tip: WtfGlobal.getLocaleText("acc.product.description"),
            align:'right',
            width: 100,
            pdfwidth:80,
            dataIndex: 'description',
            groupable:true
        },{
            header: WtfGlobal.getLocaleText("acc.common.memo"),
            tip: WtfGlobal.getLocaleText("acc.common.memo"),
            align:'right',
            pdfwidth:80,
            width: 100,
            dataIndex: 'memo'
        },{
            header: WtfGlobal.getLocaleText("acc.field.bomCode"),
            tip: WtfGlobal.getLocaleText("acc.field.bomCode"),
            align:'right',
            pdfwidth:80,
            width: 100,
            dataIndex: 'bomCode'
        }]);
        
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
            name:'purchaseprice'
        },

        {
            name:'saleprice'
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
        this.expandStore = new Wtf.data.Store({
            //            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getProductBatchDetails.do",
            baseParams:{
                mode:25
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gridRec)
        });
        
    this.productRec = Wtf.data.Record.create ([
            {
                name:'productid'
            },
            {
                name:'pid'
            },
            {
                name:'type'
            },
            {
                name:'productname'
            },

            {
                name:'desc'
            },

            {
                name: 'producttype'
            }
            ]);
    
        this.productStore = new Wtf.data.Store({
            //            url:Wtf.req.account+'CompanyManager.jsp',
            url: "ACCProduct/getProductsByType.do",
            baseParams:{
                mode:28,
                type:Wtf.producttype.assembly
                },//Assembly products 
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        var baseParamsforCombo = this.productStore.baseParams;
        var configforCombo = {
            multiSelect: true,
            listWidth: Wtf.ProductComboListWidth
        }
        this.productname = CommonERPComponent.createProductMultiselectPagingComboBox(200, 300, 30, this, baseParamsforCombo, configforCombo);  
//    this.ProductComboconfig = {
//            hiddenName:"productid",         
//            store: this.productStore,
//            valueField:'productid',
//            hideLabel:true,
//            hidden:this.iscustreport,
//            displayField:'productname',
//            emptyText:WtfGlobal.getLocaleText("acc.msgbox.17"),
//            mode: 'local',
//            typeAhead: true,
//            selectOnFocus:true,
//            triggerAction:'all',
//            scope:this
//        };
////    this.productname = new Wtf.common.Select(Wtf.applyIf({
////         multiSelect:true,
////         fieldLabel:WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
////         forceSelection:true,
////         width:240
////    },this.ProductComboconfig));
//        this.productStore.load();
//
//    this.productname = new Wtf.common.Select(Wtf.applyIf({
//         multiSelect:true,
//         fieldLabel:WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
//         extraFields:['pid','type'],
//         extraComparisionField:'pid',// type ahead search on product id as well.
//         listWidth:Wtf.ProductComboListWidth, 
//         forceSelection:true,         
//         width:240
//    },this.ProductComboconfig));
        
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
        this.expandStore.on('load',this.fillExpanderBody,this);
//        this.expandStore.on('loadexception',this.fillExpanderBody,this);
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

        this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            tooltip :WtfGlobal.getLocaleText("acc.auditTrail.resetTip"),  //'Reset Search Result.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        
        
        this.searchBttn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
//            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            scope: this,
            id:"search"+this.helpmodeid,
            handler: this.searchHandler,
            iconCls:'accountingbase fetch'
        });
        
        var searchFieldStore = new Wtf.data.SimpleStore({
            fields: ["id", "name","toolTip"],
            data: [["OTHERS", "OTHERS", WtfGlobal.getLocaleText("acc.productList.searchText")],  ["BATCH", "Batch Name", "Search on Batch Name"],["SERIAL", WtfGlobal.getLocaleText("acc.field.SerialNo"), "Search on Serial No"]]
        });
        this.searchFieldSelectionCmb = new Wtf.form.ExtFnComboBox({
            hiddenName: 'searchOnField',
            store: searchFieldStore,
            typeAhead: true,
            displayField: 'name',
            extraFields: ['toolTip'],
            searchOnField:true,
            valueField: 'id',
            mode: 'local',
            listWidth:500,
            width: 100,
            value: "OTHERS",
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.stockavailability.SelectFieldtoSearch")
        });
        this.searchFieldSelectionCmb.on("select",function(){
            if( this.fT.getValue() != undefined && this.fT.getValue() != "" ){
                this.store.reload();
            }
        },this);
    
        this.fT = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.productList.searchText") +","+WtfGlobal.getLocaleText("acc.field.SerialNo")+","+WtfGlobal.getLocaleText("acc.assetdepriciation.grid.BatchName"),//'Search by Product Name',
            width: 130,
            id:"quicksearch"+this.helpmodeid,
            field: 'productname',
            Store:this.store
        });
        //        new Wtf.form.TextField({
        //            fieldLabel : WtfGlobal.getLocaleText("acc.auditTrail.con"),  //"Contains",
        //            emptyText : WtfGlobal.getLocaleText("acc.productList.searchText"),  //Search by Product Name,
        //            width : 200
        //        });
                            
        this.reader = new Wtf.data.JsonReader({
            root: 'data',
            fields: [{
                name: 'id'
            }, {
                name: 'name'
            }]
        });
      this.exportButton=new Wtf.exportButton({
            obj:this,
            id:"exportReports"+this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            filename: WtfGlobal.getLocaleText("acc.dashboard.assemblyreport")+"_v1",
            disabled :true,
            scope : this,
            hidden:this.isUnbuildAssembly,
            menuItem:{csv:true,pdf:true,rowPdf:false},
            get:Wtf.autoNum.AssemblyExport
    });
     btnArr.push(new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.productList.buildAssembly"),//'Build Assembly',
            tooltip: WtfGlobal.getLocaleText("acc.productList.buildAssemblyTT"),  //{text:"Click here to build stock of an assembly product by using Inventory Assembly."},
            scope:this,
            hidden:this.isUnbuildAssembly,
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler:function(){
                callBuildAssemblyForm();
            }
        }));
    this.editButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),
            scope:this,
            hidden:true,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:WtfGlobal.getLocaleText("acc.field.EditTransaction"),
            handler:this.editHandler.createDelegate(this)
        });
    
    this.deleteButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.delete"),
            scope:this,
            hidden:(this.isOrder || this.isUnbuildAssembly),
            tooltip:WtfGlobal.getLocaleText("acc.field.DeleteTransaction"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.deleteHandler.createDelegate(this)
        });
     this.exportButton.enable();
        var innerPanel = new Wtf.Panel({
            border : false,
            layout:'fit',
            items:[this.grid],
            tbar: [ this.fT,"-",WtfGlobal.getLocaleText("acc.stockavailability.SearchOnField") + ": ",this.searchFieldSelectionCmb,"-",this.productname,                    
            this.searchBttn, 
            this.resetBttn, this.exportButton,
            btnArr,this.editButton,this.deleteButton,"->",
            getHelpButton(this,this.helpmodeid)
            ],
            bbar: new Wtf.PagingSearchToolbar({
                id: 'pgTbar' + this.id,
                searchField: this.fT,
                pageSize: 30,
                store: this.store,
                displayInfo: true,
                //                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({}),
                items: ["-",
                    this.singleRowPrint = new Wtf.exportButton({//ERM-26 Print Reocrd button
                        obj: this,
                        id: "printSingleRecord" + this.id,
                        iconCls: 'pwnd printButtonIcon',
                        text: WtfGlobal.getLocaleText("acc.rem.236"),
                        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record details',
                        disabled: true,
                        filename: WtfGlobal.getLocaleText("acc.productList.buildAssembly"),
                        menuItem: {rowPrint: true},
                        get: Wtf.autoNum.buildAssemblyReport,
                        moduleid: Wtf.Build_Assembly_Report_ModuleId
                    })
                ]
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
            this.store.baseParams.searchOnField = this.searchFieldSelectionCmb.getValue();
            this.store.baseParams.ss = this.fT.getValue();
            this.store.baseParams.productid = this.productname.getValue();
        }, this);
        this.store.on('datachanged', function() {
            var p = this.pP.combo.value;
            this.fT.setPage(p);
        }, this);
        this.add(innerPanel);
        this.store.baseParams = {
            mode:201,
            isUnbuildAssembly:this.isUnbuildAssembly,
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
    handleResetClick:function(){
        this.fT.reset();
        this.productname.reset();
        this.searchFieldSelectionCmb.reset();
        this.store.baseParams = {
            mode:201,
            isUnbuildAssembly:this.isUnbuildAssembly,
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
    
    onCellClick: function(grid, rowIndex, columnIndex, eventObject) {
        eventObject.stopEvent();
        var el = eventObject.getTarget("a");
        if (el == null) {
            return;
        }
        var header = grid.getColumnModel().getDataIndex(columnIndex);
        if (header == "entryno") {
            var journalentryid = this.store.getAt(rowIndex).data['journalentryid'];
            this.fireEvent('journalentry',journalentryid,true, null,null,null,null);
        }
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
//        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.product.assembly.comp")+"</span>";   
//        header += "<span class='gridNo' style='font-weight:bold;'>S.No.</span>";
//        for(var i=0;i<arr.length;i++){
//            header += "<span class='headerRow'>" + arr[i] + "</span>";
//        }
//        header += "<span class='gridLine'></span>";
        
        
        
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
            
        var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.productname);
        if(isInvalidProductsSelected){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
            return;
        }
    
    
    this.store.removeAll();
    this.store.baseParams = {
        mode:201,
        isUnbuildAssembly:this.isUnbuildAssembly,
        search:this.fT.getValue(),
        productid :  this.productname.getValue()
    };
    this.store.load({
        params: {
            start:0,
            limit:this.pP.combo.value
        }
    });
},
    editHandler:function(){
        var buildForm = Wtf.getCmp("buildAssemblyForm");
        var selectedRecordArray = this.grid.getSelectionModel().getSelections();
        if(selectedRecordArray.length>1 || selectedRecordArray.length<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        var record = "";
        if(selectedRecordArray.length == 1){
            record = selectedRecordArray[0];
        }
        if(buildForm==null){
            buildForm = new Wtf.account.BuildAssemblyForm({
                title:'Edit'+ WtfGlobal.getLocaleText("acc.productList.buildAssembly"),  //"Build Assembly",
                tabTip: WtfGlobal.getLocaleText("acc.productList.buildAssembly"),  //"Build Assembly",
                id:"buildAssemblyForm",
                prodbuildid:record.data.productid,
                iconCls :getButtonIconCls(Wtf.etype.buildassemly),
                layout:'fit',
                record:record,
                isEdit:true,
                closable:true,
                border:false
            });
            buildForm.on("closed",function(buildForm){
                Wtf.getCmp('as').remove(buildForm)
            },this);
            buildForm.on("activate",function(panel){
                panel.doLayout();
            },this);
            Wtf.getCmp('as').add(buildForm);
        }
        Wtf.getCmp('as').setActiveTab(buildForm);
        Wtf.getCmp('as').doLayout();
    },
    deleteHandler:function(){
        var selectedRecordArray = this.grid.getSelectionModel().getSelections();
        if(selectedRecordArray.length<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.rem.238"),function(btn){
            if(btn =="yes") {
                var productids=[],productrefno=[],mainproductids=[],assmbledProdQty=[];
                var deleteUrl = "INVGoodsTransfer/deleteProductBuildAssembly.do";  //ACCProduct/deleteProductBuildAssembly.do
                for(var count=0;count<selectedRecordArray.length;count++)
                {
                    productids.push(selectedRecordArray[count].data.productid);
                    productrefno.push(selectedRecordArray[count].data.productrefno);
                    mainproductids.push(selectedRecordArray[count].data.mainproductid);
                    assmbledProdQty.push(selectedRecordArray[count].data.quantity);
                }               
                Wtf.Ajax.requestEx({
                    url:deleteUrl,
                    params:{
                        productids:productids,
                        productrefno:productrefno,
                        product:mainproductids,
                        assmbledProdQty:assmbledProdQty
                    }
                },this,function(res,req){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.productList.buildAssemblyReport"),res.msg],res.success*2+1);
                    Wtf.getCmp('buildreportgridid').store.reload();
                },function(res,req){
                    var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
                    if(res.msg)msg=res.msg;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                });
            }
        });
    }
    
});