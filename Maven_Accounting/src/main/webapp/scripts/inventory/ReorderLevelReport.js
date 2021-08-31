function callReorderLevelReport(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventoryreports, Wtf.Perm.inventoryreports.batchwisestocktracking)) {
        var inventoryTab = Wtf.getCmp("reorderlevelreport");
        if(inventoryTab == null){
            inventoryTab = new Wtf.ReorderLevelReportTabPanel({
                title:WtfGlobal.getLocaleText("acc.inventoryList.ReorderLevelReport"),
                id:"reorderlevelreport",
                layout:"fit",
                iconCls:getButtonIconCls(Wtf.etype.inventoryrlr),
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


Wtf.ReorderLevelReportTabPanel = function(config){
    Wtf.ReorderLevelReportTabPanel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.ReorderLevelReportTabPanel, Wtf.Panel, {
    initComponent: function() {
        Wtf.ReorderLevelReportTabPanel.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        var companyDateFormat='Y-m-d'
        Wtf.ReorderLevelReportTabPanel.superclass.onRender.call(this, config);
        
        this.storeCmbRecord = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },

        {
            name: 'description'
        },

        {
            name: 'fullname'
        },

        {
            name: 'abbrev'
        },

        {
            name: 'dmflag'
        }
        ]);


        
        this.strloadurl = 'INVStore/getStoreList.do';
       
        
        this.storeCmbStore = new Wtf.data.Store({
            url:  this.strloadurl,
            baseParams:{
                byStoreExecutive:"true",
                byStoreManager:"true",
                includePickandPackStore:true
            
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : 'Store*',
            hiddenName : 'store',
            store : this.storeCmbStore,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 180,
            triggerAction: 'all',
            emptyText:'Select store...',
            typeAhead:true,
            forceSelection:true,
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
                text:WtfGlobal.getLocaleText("acc.stock.ClicktoResetFilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                this.storeCmbfilter.setValue(this.storeCmbfilter.store.data.items[0].data.store_id);
                if(this.reordelLevelFiltercmb.store.getCount() > 0){
                    this.reordelLevelFiltercmb.setValue(this.reordelLevelFiltercmb.store.getAt(0).get('id'));
                }else{
                    this.reordelLevelFiltercmb.setValue("");
                }
                Wtf.getCmp("Quick"+this.grid.id).setValue("");
                
                this.initloadgridstore(this.storeCmbfilter.getValue(),this.reordelLevelFiltercmb.getValue());
        
            }
        });
        var moduleData = [];
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            moduleData.push(["false", "Above"],["true", "Below"])
        }
       
        this.moduletype = new Wtf.data.SimpleStore({
            fields:["id", "name"],
            data : moduleData
        });
        this.reordelLevelFiltercmb = new Wtf.form.ComboBox({
            hiddenName : 'Reorder Level',
            store : this.moduletype,
            typeAhead:true,
            readOnly: false,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 110,
            triggerAction: 'all',
            emptyText:'Select module...'
        });  
        
        this.moduletype.on('load', function(){
            if(this.moduletype.getCount() > 0){
                this.reordelLevelFiltercmb.setValue(this.moduletype.getAt(0).get('id'));
            }
        }, this)
        
        this.moduletype.loadData(moduleData);
        
        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.common.search"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
                var action = 4//(monthlyreportcheck)? getMultiMonthCheck(this.frmDate, this.toDate, 1) : 4;
                switch(action) {
                    case 4:
                        var format = "Y-m-d";
                        this.initloadgridstore(this.storeCmbfilter.getValue(),this.reordelLevelFiltercmb.getValue());
                        break;
                    default:
                        break;
                }
            }
        });

      
        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },

        {
            "name":"name"
        },
        {
            "name":"itemcode"
        },
        
        {
            "name":"itemId"
        },

        {
            "name":"itemdescription"
        },
        {
            "name":"itemname"  
        },
        {
            "name":"quantity"
        },
        
        {
            "name":"isbelowreorder"
        },
        {
            "name":'stockuom'
        },
        {
            "name":'reorderlevel'
        }
        ]);
        
        this.ds = new Wtf.data.Store({
            url: 'INVStockLevel/getReorderLevelReportList.do',//
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        
        });
        
        this.ds.on('beforeload', function() {
            var currentBaseParams = this.ds.baseParams;
            currentBaseParams.reordelLevelFilter=this.reordelLevelFiltercmb.getValue();
            this.ds.baseParams=currentBaseParams;
        },this);
        this.sm= new Wtf.grid.CheckboxSelectionModel({
            // singleSelect:true
            });
        
        var cmDefaultWidth = 250;
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.KWLRowNumberer(),  //0
            this.sm, //1
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                //sortable:true,
                dataIndex: 'itemcode',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.productname"),
                //sortable:true,
                dataIndex: 'itemname',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            
            {
                header: WtfGlobal.getLocaleText("acc.product.stockUoMLabel"),
                //sortable:true,
                dataIndex: 'stockuom',
                width:cmDefaultWidth,
                pdfwidth:50
            },{
                header: WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header5"),
                //sortable:true,
                dataIndex: 'quantity',
                width:cmDefaultWidth,
                hidden:false,
                pdfwidth:50
            },{
                header: WtfGlobal.getLocaleText("acc.product.reorderLevel"),
                //sortable:true,
                dataIndex: 'reorderlevel',
                width:cmDefaultWidth,
                hidden:false,
                pdfwidth:50
            },{
                header: WtfGlobal.getLocaleText("acc.stockavailability.IsbelowReorderLevel"),
                //sortable:true,
                dataIndex: 'isbelowreorder',
                width:cmDefaultWidth,
                hidden:true,
                pdfwidth:50
            //                renderer:function(value,b,c,d,e,f,g){
            //                    if(value){
            //                        g.getRow(d).style = "background-color :red";
            //                    } 
            //                }
            }
            ]);
            
                
        this.exportBttn = new Wtf.exportButton({
            obj: this,
            //            id: 'stocktransferregisterexportid',
            tooltip: "Export Report", //"Export Report details.",  
            menuItem:{
                csv:true,
                pdf:true,
                xls:true
            },
            get:Wtf.autoNum.ReorderLevelReport,
            label:"Export"

        })
        this.printBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.print"),
            scope: this,
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printTTip")
            },
            //            id:'interstorestockreportprint',
            iconCls:'pwnd printButtonIcon',
            hidden:true,
            disabled:true,
            handler: function(){
                var selected= this.sm.getSelections();
                var cnt = selected.length;
                if(cnt > 0){
                    printout("interstore",this.ds.query("transfernoteno", selected[0].data.transfernoteno));
           
                }else{
                    return;
                }
            // this.Print();
            }
            
        });



        var tbarArray= new Array();
        tbarArray.push( "-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+": ",this.storeCmbfilter,"-",WtfGlobal.getLocaleText("acc.product.reorderLevel")+": ",this.reordelLevelFiltercmb, "-",this.search,"-",this.resetBtn);
        

       
        var bbarArray=new Array();
        bbarArray.push("-",this.exportBttn);

        

        /********************/
        this.grid=new Wtf.KwlEditorGridPanel({
            //            id:"inventEditorGridPanel"+this.id,
            cm:this.cm,
            store:this.ds,
            sm:this.sm,
            viewConfig: {
                forceFit: false,
                   getRowClass: function(record, index) {
                    if(record.data.isbelowreorder){
                            return "occurrenceNo_N td, .occurrenceNo_N tr";
                    }
                   }
            },
            tbar:tbarArray,
            searchLabel:WtfGlobal.getLocaleText("acc.help.title.121"),
            searchLabelSeparator:":",
            searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.SearchbyTransferNoteNoSerialNo"),
            serverSideSearch:true,
            searchField:"transfernoteno",
            clicksToEdit:1,
            displayInfo: true,
            bbar: bbarArray
        });
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        
        this.grid.on("validateedit",this.validateeditFunction,this);
        this.grid.on("statesave",this.statesaveFunction,this);
        
        this.add(this.grid);
        
        this.on("activate",function()
        {
            
            
            this.storeCmbStore.on("load", function(ds, rec, o){
                var storeIdSetPreviously=this.storeCmbfilter.getValue();
                var index =this.storeCmbStore.find('fullname',"ALL");
                if(index == -1 && rec.length > 1){
                    var newRec=new this.storeCmbRecord({
                        store_id:'',
                        fullname:'ALL'
                    });
                    this.storeCmbStore.insert(0,newRec);
                    this.storeCmbfilter.setValue("",true);
                }
                    
                if(storeIdSetPreviously != undefined && storeIdSetPreviously != ""){
                    this.storeCmbfilter.setValue(storeIdSetPreviously, true);
                }
            
                //                this.storeCmbfilter.fireEvent('select');
                    
                this.initloadgridstore(this.storeCmbfilter.getValue(),this.reordelLevelFiltercmb.getValue());
            }, this);
                
                         
            
        //
        },this);
    

    },
    
   
    initloadgridstore:function(storeid,reordelLevelFilter){

        this.ds.baseParams = {
            store:storeid
        
        }
        this.ds.load({
            params:{
    
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss:this.grid.quickSearchTF.getValue(),
                reordelLevelFilter:reordelLevelFilter
            }
        });
    },
   
    getComboRenderer : function(combo){
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1){
                idx = combo.store.find(combo.displayField, value);
            }
            if(idx == -1)
                return value;
            var rec = combo.store.getAt(idx);
            var valueStr = rec.get(combo.displayField);
            return "<div wtf:qtip=\""+valueStr+"\">"+valueStr+"</div>";
        }
    }
});
