// ------------------- Stock Adjustment Report


function markoutList1(stockAdjustmentID,isJobWorkInReciever){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockadjustment, Wtf.Perm.stockadjustment.viewstockadj)) {
        var mainTabId = Wtf.getCmp("as");
        var MarkoutTab = isJobWorkInReciever?Wtf.getCmp("jobWorkInrecieverListTab"):Wtf.getCmp("markoutListMainCmp");
        if(MarkoutTab == null){
            MarkoutTab = new Wtf.markoutTab({
                layout:"fit",
                title:isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobWorkIn.vendorjobworkinreg"):WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustmentRegister"),
                closable:true,
                tabTip:isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobWorkIn.vendorjobworkinreg"):WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustmentRegister"),
                border:false,
                iconCls:getButtonIconCls(Wtf.etype.inventorysrep),
                stockAdjustmentID: stockAdjustmentID,
                isJobWorkInReciever:isJobWorkInReciever,
                id:isJobWorkInReciever?"jobWorkInrecieverListTab":"markoutListMainCmp"
            });
            mainTabId.add(MarkoutTab);
        }
        mainTabId.setActiveTab(MarkoutTab);
        mainTabId.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.markoutTab = function (config){
    Wtf.apply(this,config);
    Wtf.markoutTab.superclass.constructor.call(this);
}

Wtf.extend(Wtf.markoutTab,Wtf.Panel,{
    onRender:function (config) {
        this.stockAdjustmentID=config.stockAdjustmentID;
        Wtf.markoutTab.superclass.onRender.call(this,config);
        this.getTabpanel1(this.stockAdjustmentID);
        this.add(this.tabPanel);
    },
    getTabpanel1:function (stockAdjustmentID){
        this.markoutDetailTab(stockAdjustmentID);
        if (!this.isJobWorkInReciever ) {
            this.markoutSummaryTab(stockAdjustmentID);
        }
        this.itemsarr = [];
        this.itemsarr.push(this.markoutDetail);
        if (!this.isJobWorkInReciever ) {
            this.itemsarr.push(this.markoutSummary);
        }
        
        this.tabPanel = new Wtf.TabPanel({
            activeTab:0,
            //            id:"stockrepairePan",
            items:this.itemsarr 
        });
    },
    markoutDetailTab:function (stockAdjustmentID){
        this.markoutDetail =new Wtf.markoutList({
            id:"markoutListCmp",
            layout:'fit',
            title:this.isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobWorkIn.vendorjobworkinreg"):WtfGlobal.getLocaleText("acc.stockavailability.StockAdjustmentDetail"),
            closable:false,
            isJobWorkInReciever:this.isJobWorkInReciever,
            type:1,
            id:this.isJobWorkInReciever?"JowbWorkInRecieverListDetailCmp":"markoutListDetailCmp",
            tabTip:this.isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobWorkIn.vendorjobworkinreg"):WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustmentRegister"), 
            iconCls:getButtonIconCls(Wtf.etype.inventoryisar),
            border:false,
            stockAdjustmentID: stockAdjustmentID
        });
    },
    markoutSummaryTab:function (stockAdjustmentID){
        this.markoutSummary =new Wtf.markoutList({
            id:"markoutListCmp",
            layout:'fit',
            title:WtfGlobal.getLocaleText("acc.stockavailability.StockAdjustmentSummary"),
            closable:false,
            id:"markoutListSummaryCmp",
            type:2,
            tabTip:WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustmentRegister"), 
            iconCls:getButtonIconCls(Wtf.etype.inventoryisar),
            border:false,
            stockAdjustmentID: stockAdjustmentID
        });
    }
});

function markoutList(stockAdjustmentID){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockadjustment, Wtf.Perm.stockadjustment.viewstockadj)) {
        var demo=Wtf.getCmp("markoutListCmp")
        var main=Wtf.getCmp("as");
        if(demo==null){
            demo =new Wtf.markoutList({
                id:"markoutListCmp",
                layout:'fit',
                title:WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustmentRegister"),
                closable:true,
                tabTip:WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustmentRegister"), 
                iconCls:getButtonIconCls(Wtf.etype.inventoryisar),
                border:false,
                stockAdjustmentID: stockAdjustmentID
            })
            main.add(demo);
        }
        main.setActiveTab(demo);
        main.doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.markoutList = function(config){
    Wtf.apply(this, config);
    this.reportType = config.type;
    Wtf.markoutList.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.markoutList, Wtf.Panel, {
    onRender: function(config) {
        Wtf.markoutList.superclass.onRender.call(this, config);

        this.status = 0;
        if(true == 5 || true == 17 || true == 18) {
            this.dmflag = 0;
        } else {
            this.dmflag = 1;
        }
        var companyDateFormat='Y-m-d';
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
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
            name : 'todate',
            value:WtfGlobal.getDates(false),
            minValue: Wtf.archivalDate,
            format: companyDateFormat//Wtf.getDateFormat()
        });
        
        //ERP-27973 : Date Validation
        this.frmDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.toDate.getValue()!=''){
                if(field.getValue().getTime()>this.toDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    field.setValue(oldval);                    
                }
            }
        },this);
        
        this.toDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.frmDate.getValue()!=''){
                if(field.getValue().getTime()<this.frmDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                    field.setValue(oldval);
                }
            }
        },this);
        
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
        ]);

        
        this.exportBttn=new Wtf.Button({
            text:'Export',
            tooltip: {
                text:"Export report in csv format"
            },
            scope: this,
            id:'interstorestockreportexport',
            iconCls:getButtonIconCls(Wtf.etype.exportfile),
            handler: function(){
                this.exportReport(57, "csv");
            }
        });
        
        this.createnew=new Wtf.Action({
            text:WtfGlobal.getLocaleText('acc.jobWorkIn.addnewjobworkin'),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.jobWorkIn.addnewjobworkin")
            },
            scope: this,
            id:'addjobWorkIn',
            iconCls:getButtonIconCls(Wtf.etype.exportfile),
            handler: function(){
                this.openForm(false);
            }
        });
        
        this.edit=new Wtf.Action({
            text:WtfGlobal.getLocaleText('acc.jobWorkIn.editjobworkin'),
            tooltip: {
                text:WtfGlobal.getLocaleText('acc.jobWorkIn.editjobworkin')
            },
            scope: this,
            id:'editJobWorkIn',
            iconCls:getButtonIconCls(Wtf.etype.exportfile),
            handler: function(){
                this.openForm(true);
            }
        });
        
        this.delete1=new Wtf.Action({
            text:WtfGlobal.getLocaleText('acc.jobWorkIn.deletejobworkin'),
            tooltip: {
                text:WtfGlobal.getLocaleText('acc.jobWorkIn.deletejobworkin')
            },
            scope: this,
            id:'deleteJobWorkIn',
            iconCls:getButtonIconCls(Wtf.etype.exportfile),
            handler: function(){
//                this.exportReport(57, "csv");
            }
        });
        
        this.addEditDelArr = [];
        this.addEditDelArr.push(this.createnew);
//        this.addEditDelArr.push(this.edit);
//        this.addEditDelArr.push(this.delete);
        
        this.addEditDelMenu=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText('acc.jobWorkIn.managejobworkin'),
            tooltip: {
                text:WtfGlobal.getLocaleText('acc.jobWorkIn.managejobworkin')
            },
            scope: this,
            id:'ManageJobWorkIn',
            iconCls:getButtonIconCls(Wtf.etype.exportfile),
            menu:this.addEditDelArr
        });
        
        this.importTitle = "Stockout";
        this.ImportButton= new Wtf.Button({
            text: "Import "+this.importTitle,
            scope: this,
            tooltip: {
                text:"Import "+this.importTitle
            },
            handler: function(){
            //                this.importItemForm =  new Wtf.importForm({
            //                    title:"Import "+this.importTitle,
            //                    importFor: "Markout",
            //                    width:500,
            //                    showStore:true,
            //                    modal:true,
            //                    height: 330,
            //                    layout:"fit"
            //              });
            //               this.importItemForm.on("importsuccess",function(){
            //               });
            //this.importItemForm.show();
            },
            iconCls: "importIcon"
        });
        this.storeCmbStore = new Wtf.data.Store({
            url: 'INVStore/getStoreList.do',
            //            baseParams:{
            //                flag:7,
            //                dsmanager:1
            //            },
            baseParams:({
                byStoreManager:"true",
                byStoreExecutive:"true",
                includePickandPackStore:true
            }),
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : 'Store*',
            hiddenName : 'store',
            store : this.storeCmbStore,
            editable: true,
            typeAhead: true,
            forceSelection: true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width: 150,
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
        
        
        /* Remove Stock OUT and Sales From Job Receiver
         * 
         */
        if (this.isJobWorkInReciever) {
            this.AdjustmentTypeStore = new Wtf.data.SimpleStore({
                fields: ['adjustmentType', 'ajdustmentTypeName'],
                data: [['', 'All'], ['Stock IN', 'Stock IN']],
                pruneModifiedRecords: true
            });
        } else {
            this.AdjustmentTypeStore = new Wtf.data.SimpleStore({
                fields: ['adjustmentType', 'ajdustmentTypeName'],
                data: [['', 'All'], ['Stock IN', 'Stock IN'], ['Stock Out', 'Stock Out'], ['Stock Sales', 'Stock Sales']],
                pruneModifiedRecords: true
            });
        }
        
        this.adjustmentTypeFilter = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.AdjustmentTypeStore,
            displayField:"ajdustmentTypeName",
            valueField:"adjustmentType",
            allowBlank:false,
            editable:false,
            width:80
        });
        this.markoutTypeStore = new Wtf.data.SimpleStore({
            fields: ['id', 'type'],
            data: [['', 'All'],
            ['markout', 'Stockout'],
            ['emeal', 'Employee Meal'],
            ['sampling', 'Sampling']]
        });
        this.markoutTypeCmb = new Wtf.form.ComboBox({
            fieldLabel: 'Stockout Type*',
            store: this.markoutTypeStore,
            editable: true,
            displayField: 'type',
            valueField: 'id',
            mode: 'local',
            width: 150,
            triggerAction: 'all',
            emptyText: 'Select Stockout type...',
            value: '',
            forceSelection:true,
            typeAhead:true
        });
        
        this.reasonRec = new Wtf.data.Record.create([
        {
            name: "id"
        },

        {
            name: "name"
        },

        {
            name: "defaultMasterItem"
        }
        ]);
      
        this.reasonRecReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"
        },this.reasonRec);
        
        this.reasonStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                groupid: 31,
                mode: 112
            },
            reader: this.reasonRecReader
        });
        
        this.reasonStore.on("load", function(ds, rec, o) {
            var newRec = new this.storeCmbRecord({
                id: '',
                name: 'ALL'
            });
            this.reasonStore.insert(0, newRec);
            this.adjReasonCombo.setValue('');
        }, this);
        
        this.reasonStore.load();
        
        this.adjReasonCombo = new Wtf.form.FnComboBox({
            fieldLabel: 'Adjustment Reason*',
            hiddenName: 'reason',
            store: this.reasonStore,
            //            id: 'reasoncomboId' + this.id,
            width: 150,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true,
            allowBlank: false
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
                this.toDate.setValue(WtfGlobal.getDates(false));
                if(true != 5)
                    // this.storeCmbfilter.setValue("");
                    Wtf.getCmp("Quick"+this.grid.id).setValue("");
                this.markoutTypeCmb.setValue("");
                this.status = this.status==1?0:1;
                this.storeCmbfilter.setValue(this.storeCmbfilter.store.data.items[0].data.store_id);
                this.adjustmentTypeFilter.setValue(this.adjustmentTypeFilter.store.data.items[0].data.adjustmentType);
                this.adjReasonCombo.setValue(""),
                //                this.adjustmentTypeFilter.setValue("");
                this.loadgridstore(this.frmDate.getValue().format('Y-m-d'), this.toDate.getValue().format('Y-m-d'));
            }
        });
       
       
     this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function() {
                if (this.reportType == 1) {
                    if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
                        this.expandButtonClicked = true;
                    }
                    this.expandCollapseGrid();
//                    if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
//                        this.expander.expandAll();
//                        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
//                    } else {
//                        this.expander.collapseAll();
//                        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
//                    }
//                } 
                }
            }
    });
        
        var monthlyreportcheck=false;
        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.common.search"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
                var action = (monthlyreportcheck)? getMultiMonthCheck(this.frmDate, this.toDate, 0) : 4;
                switch(action) {
                    case 4:
                        this.status = this.status == 1 ? 0 : 1;
                        var format = "Y-m-d H:i:s";
                        this.loadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format));
                        break;
                    case 6:
                        multiMonthConfirmBox(45, this);
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

        {name:'inventoryjeid'},
        {name:'inventoryentryno'},
        {
            "name":"store_id"
        },

        {
            "name":"storeAbbr"
        },

        {
            "name":"storeDesc"
        },

        {
            "name":"productCode"
        },

        {
            "name":"productName"
        },
        {
            "name":"productDescription"
        },

        {
            "name":"quantity"
        },
        {
            "name":"packaging"
        },
        {
            "name":"jobworkorderno"
        },
        {
            "name":"jobworkorderid"
        },
        {
            "name":"customerid"
        },
        {
            "name":"customername"
        },

        {
            "name":"uomName"
        },
        {
            "name":"createdBy"
        },
        {
            "name":"markouttype"
        },

        {
            "name":"reason"
        },

        {
            "name":"costcenter"
        },

        {
            "name":"cost"
        },

        {
            "name":"amount"
        },

        {
            "name":"date" 
        //            "type":"date", //Formatting it from Java side only.
        //            "format":"Y-m-d"
        },

        {
            "name":"type"
        },
        {
            "name":"createdon"
        },

        {
            "name":"adjustmentType"
        },
        {
            "name":"remark"
        },

        {
            "name":"partnumber"
        },

        {
            "name":"seqNumber"
        },
        {
            "name":"locationname"
        },
        {
            "name":"locationid"
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
        },{
            name:"deleted"
        },
        {
            "name":"stockDetails"
        },{
            "name":"memo"
        },
        {
            "name":"adjustmentreason"
        },{
            "name":"adjustmentreasonid"
        },
        {
            "name":"reasonid"
        },
        {
            "name":"ccnumber"
        },
        {
                "name":"throughFile"
        }
        ]);
        
        
        var grpView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: true,
            enableGroupingMenu: true,
            hideGroupedColumn: false
        });
        this.ds = new Wtf.data.Store({
            //            sortInfo: {
            //                field: 'seqNumber',
            //                direction: "ASC"
            //            },
            //            groupField:"storeAbbr",
            baseParams: {
                type:this.reportType
           
            },
            url: 'INVStockAdjustment/getStockAdjustmentList.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        //            remoteSort: true,
        //            remoteGroup: true
        });
        
      
        this.ds.on("load",function(store,rec,opt){
            this.grid.loadMask = false;
             WtfGlobal.resetAjaxTimeOut();
        },this);

        this.ds.on("beforeload",function(){
            this.ds.baseParams = {
                frmDate: this.frmDate.getValue().format('Y-m-d'),
                toDate: this.toDate.getValue().format('Y-m-d'),
                storeid: this.storeCmbfilter.getValue(),
                adjustmentType: this.adjustmentTypeFilter.getValue(),
                markouttype: "markout",
                type:this.reportType,
                summaryFlag: this.status == 1,
                dmflag:this.dmflag,
                adjustmentReason: this.adjReasonCombo.getValue(),
                stockAdjustmentID: (this.stockAdjustmentID != undefined) ? this.stockAdjustmentID : "",
                isJobWorkInReciever:this.isJobWorkInReciever
            }
             WtfGlobal.setAjaxTimeOut();
        },
        this);
        var integrationFeatureFor=true;

        Wtf.grid.GroupSummary.Calculations['cost'] = function(v, record) {
            var amt=record.data.amount;
            if(amt==null || amt==undefined)
                amt=0;
            return Math.round((v + amt)*100)/100;
        }
        Wtf.grid.GroupSummary.Calculations['quantity'] = function(v, record) {
            var qty=record.data.quantity;
            if(qty==null || qty==undefined)
                qty=0;
            return Math.round((v + qty)*100)/100;
        }
        
        
        
        /*      this.tmplt =new Wtf.XTemplate(
            '<table cellspacing="1" cellpadding="0" style="margin-top:15px;width:100%;margin-bottom:40px;position:relative" border="0">',
            '<tr>',
            '<th style="padding-left:50px"><h2><b>No.</b></h2></th>',
            '<th ><h2><b>Location</b></h2></th>',
            '<th ><h2><b>Batch</b></h2></th>',
            '<th ><h2><b>Serials</b></h2></th>',
            '<th><h2><b>Qunatity</b></h2></th>',
            '</tr>',
            '<tr><span  class="gridLine" style="width:94%;margin-left:45px;position: relative;top: 33px;"></span></tr>',
            '<tpl for="stockDetails">',
            '<tr>',
            '<td style="padding-left:50px"><p>{#}</p></td>',
            '<td ><p>{locationName}</p></td>',
            '<td ><p>{batchName}</p></td>',
            '<td ><p>{serialNames}</p></td>',
            '<td ><p>{quantity}</p></td>',
            '</tr>',
            '</tpl>',
            '</table>'
            );
         */             
                
//        this.tmplt =new Wtf.XTemplate(
//            '<table cellspacing="1" cellpadding="0" style="margin-top:15px;width:100%;margin-bottom:40px;position:relative" border="0">',
//            
//            '<tr>',
//            '<th style="padding-left:50px"><h2><b>No.</b></h2></th>',
//            '<th ><h2><b>Location</b></h2></th>',
//            
//            '<tpl for="parent">',
//            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // batch
//            '<th><h2><b>Row</b></h2></th>',
//            '</tpl>',
//            '</tpl>',
//            
//            '<tpl for="parent">',
//            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // batch
//            '<th><h2><b>Rack</b></h2></th>',
//            '</tpl>',
//            '</tpl>',
//            
//            '<tpl for="parent">',
//            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // batch
//            '<th><h2><b>Bin</b></h2></th>',
//            '</tpl>',
//            '</tpl>',
//            
//            '<tpl for="parent">',
//            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
//            '<th><h2><b>Batch/Challan No</b></h2></th>',
//            '</tpl>',
//            '</tpl>',
//            
//            '<tpl for="parent">',
//            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  // serial 
//            '<th><h2><b>Serials</b></h2></th>',
//            '</tpl>',
//            '</tpl>',
//            
//            '<th><h2><b>Quantity</b></h2></th>',
//            '</tr>',
//     
//            '<tr><span  class="gridLine" style="width:94%;margin-left:45px;position: relative;top: 33px;"></span></tr>',
//            
//            '<tpl for="stockDetails">',
//            '<tr>',
//            '<td style="padding-left:50px"><p>{#}</p></td>',
//            
//            '<td ><p>{locationName}</p></td>',
//            
//            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // Row
//            '<td ><p>{rowName}</p></td>',
//            '</tpl>',
//            
//            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // Rack
//            '<td ><p>{rackName}</p></td>',
//            '</tpl>',
//            
//            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // Bin
//            '<td ><p>{binName}</p></td>',
//            '</tpl>',
//            
//            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
//            '<td ><p>{batchName}</p></td>',
//            '</tpl>',
//            
//            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  //  serial 
//            '<td ><p>{serialNames}</p></td>',
//            '</tpl>',
//            
//          
//            '<td ><p>{quantity}</p></td>',
//            
//            '</tr>',
//            '</tpl>',
//            '</table>',
//            {  
//                isTrue: function(isSerialForProduct){
//                    return isSerialForProduct;
//                }
//            }
//            );    
            
//        if(this.reportType==2){
            this.expandRec = Wtf.data.Record.create ([
            {
                "name":"id"
            },
            {
                "name":"productCode"
            },
            {
                "name":"productid"
            },

            {
                "name":"productName"
            },
            {
                "name":"productDescription"
            },

            {
                "name":"quantity"
            },
            {
                "name":"packaging"
            },

            {
                "name":"uomName"
            },
        
            {
                "name":"markouttype"
            },

            {
                "name":"reason"
            },

            {
                "name":"costcenter"
            },

            {
                "name":"cost"
            },

            {
                "name":"amount"
            },
        
            {
                "name":"type"
            },
        
            {
                "name":"adjustmentType"
            },
            {
                "name":"remark"
            },

            {
                "name":"partnumber"
            },

            {
                "name":"seqNumber"
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
                "name":"reasonid"
            },
            {
                "name":"ccnumber"
            }
            ]);
            this.expandStoreUrl = 'INVStockAdjustment/getStockAdjustmentRows.do';
    
            this.expandStore = new Wtf.data.Store({
                url:this.expandStoreUrl,
                baseParams:{
                    mode:14,
                    dtype : 'report', // Display type report/transaction, used for quotation
                    isNormalContract:this.isNormalContract
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.expandRec)
            });
    
            this.expander = new Wtf.grid.RowExpander({});
    
            this.expandStore.on('load',this.fillExpanderBody,this);
            this.expander.on("expand",this.onRowexpand,this);
//        }
//        else{
//            this.expander = new Wtf.grid.RowExpander({
//                tpl :this.tmplt,
//                renderer : function(v, p, record){
//                    // var isBatchForProduct=record.get("isBatchForProduct");
//                    //  var isSerialForProduct=record.get("isSerialForProduct");
//                    if(record.get("stockDetails").length>0){ //means has stock detail data
//                        return  '<div class="x-grid3-row-expander">&#160;</div>'
//                    }else{
//                        //return '&#160;' 
//                        return  '<div class="x-grid3-row-expander">&#160;</div>'
//                    }
//                }
//           
//            });
//        }
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        
        var cmDefaultWidth =this.reportType==1? 100:250;
        
      
        var colArr =[];
        colArr.push(
        new Wtf.KWLRowNumberer(), //0
        this.sm, //1
        this.expander, //2
        {
            header: WtfGlobal.getLocaleText("acc.stockavailability.StockAdjustmentNo"), //3
            dataIndex: 'seqNumber',
            groupable: false,
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer:WtfGlobal.linkDeletedRenderer
        //                fixed:true
        });
        if (this.reportType == 1 && (Wtf.account.companyAccountPref.activateMRPManagementFlag || Wtf.account.companyAccountPref.inventoryValuationType == "1")) { // Summary Tab
            colArr.push({
                header:  WtfGlobal.getLocaleText("acc.field.inventory.je.number"),
                dataIndex: 'inventoryentryno',
                width: 150,
                pdfwidth: 75,
                renderer: WtfGlobal.linkDeletedRenderer
            });
        }
        colArr.push({
            header: WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),//4
            dataIndex: 'storeAbbr',
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
        //                fixed:true
        },
        {
            header: WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add"),//5
            dataIndex: 'locationname',
            width:cmDefaultWidth,
            pdfwidth:100,
            fixed:true,
            hidden:true,
            renderer: WtfGlobal.linkDeletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.nee.69"),//6
            dataIndex: 'createdBy',
            sortable:false,
            width:cmDefaultWidth,
            pdfwidth:100,
            fixed: true,
            renderer: WtfGlobal.linkDeletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.stock.BusinessDate"),//7
            dataIndex: 'date',
            width:cmDefaultWidth,
            pdfwidth:50,
            fixed:true,
            renderer: WtfGlobal.linkDeletedRenderer
        //                renderer: function(v){
        //                    var date = v;
        //                    if(date != undefined && date != ""){
        //                        date = date.format("Y-m-d");
        //                    }
        //                    return date;
        //                }
        },
        {
            header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),//8
            dataIndex: 'productCode',
            groupable: true,
            hidden:this.reportType==2,
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),//9
            dataIndex: 'productName',
            groupable: true,
            hidden:this.reportType==2,
            width:cmDefaultWidth,
            pdfwidth:100,
            renderer: WtfGlobal.linkDeletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),//10
            dataIndex: 'productDescription',
            groupable: true,
            hidden:this.reportType==2,
            width:cmDefaultWidth,
            pdfwidth:100,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.stockavailability.PartNo"),//11
            dataIndex: 'partnumber',
            groupable: false,
            sortable:false,
            hidden:true,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.stock.AdjustmentType"),//12
            dataIndex: 'adjustmentType',
            hidden:this.reportType==2,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
                
        },{
            header: WtfGlobal.getLocaleText("acc.common.costCenter"),//13
            dataIndex: 'costcenter',
            width:cmDefaultWidth,
            hidden:this.reportType==2,
            fixed:true,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoice.gridRemark"),//14
            dataIndex: 'remark',
            width:cmDefaultWidth,
            hidden:this.reportType==2,
            pdfwidth:50,
            fixed:true,
            renderer: WtfGlobal.linkDeletedRenderer
              
        },
        {
            header: WtfGlobal.getLocaleText("acc.product.packaging"),//15
            dataIndex: 'packaging',
            width:cmDefaultWidth,
            hidden:this.reportType==2,
            pdfwidth:50,
            hidden:true,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//16
            dataIndex: 'uomName',
            hidden:this.reportType==2,
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.fixed.asset.quantity"),//17
            dataIndex: 'quantity',
            sortable:false,
            hidden:this.reportType==2,
            align: 'right',
            width:cmDefaultWidth,
            pdfwidth:50,
            summaryType: 'sum',
            renderer:function(val){
                // return WtfGlobal.getCurrencyFormatWithoutSymbol(val, Wtf.companyPref.quantityDecimalPrecision);
                return val;
            },
            summaryRenderer: function(val) {
                //return 'Total Quantity:' + WtfGlobal.getCurrencyFormatWithoutSymbol(val, Wtf.companyPref.quantityDecimalPrecision);
                return 'Total Quantity:' + val;
            }
        },{
            header:WtfGlobal.getLocaleText("acc.stockavailability.AvgUnitPrice")+" (" +WtfGlobal.getCurrencySymbolForForm()+")",//18
            dataIndex: 'cost',
            align: 'right',
            sortable:false,
            width:cmDefaultWidth,
            hidden:this.reportType==2,
            pdfwidth:50,
            fixed:true,
            hidden:this.isJobWorkInReciever,
            renderer:function(v,m,r){
                //return v//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                return parseFloat(getRoundofValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
            }
        },{
            header:WtfGlobal.getLocaleText("acc.masterconfig.amount")+" (" +WtfGlobal.getCurrencySymbolForForm()+")",//19
            dataIndex: 'amount',
            align: 'right',
            sortable:false,
            summaryType: 'sum',
            width:cmDefaultWidth,
            pdfwidth:50,
            hidden:this.isJobWorkInReciever,
            renderer:function(v,m,r){
                //return v//WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                return parseFloat(getRoundofValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
            },
            summaryRenderer: function(val) {
                //return 'Total Quantity:' + WtfGlobal.getCurrencyFormatWithoutSymbol(val, Wtf.companyPref.quantityDecimalPrecision);
                return 'Total Amount:' + parseFloat(getRoundofValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.masterConfig.29"),//20
            dataIndex: 'reason',
            width:cmDefaultWidth,
            hidden:this.reportType==2,
            pdfwidth:50,
            hidden:false,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.stockavailability.ReasonforAdjustment"),//21
            dataIndex: 'adjustmentreason',
            width:cmDefaultWidth,
            pdfwidth:50,
            //hidden:true,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.stockavailability.EnteredFrom"),//22
            dataIndex: 'type',
            //hidden:true,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.common.memo"),//23
            dataIndex: 'memo',
//            groupable: true,
//            hidden:this.type==1,
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.jobworkorder.header.jobworkorder"),// Job Work Order
            dataIndex: 'jobworkorderno',
            width:cmDefaultWidth,
            hidden:!Wtf.account.companyAccountPref.jobWorkOutFlow,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.jobworkorder.header.ccnumber"),// Job Work Order
            dataIndex: 'ccnumber',
            width:cmDefaultWidth,
//            hidden:!this.isJobWorkInReciever,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.inventory.stockadjustment.header.throughFile"),// Job Work Order
            dataIndex: 'throughFile',
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: WtfGlobal.linkDeletedRenderer
        });
        //         this.cm.defaultSortable = true;
        
        this.moduleid = Wtf.Inventory_Stock_Adjustment_ModuleId;    
        colArr = WtfGlobal.appendCustomColumn(colArr,GlobalColumnModelForReports[this.moduleid],true, undefined, true);
        var colModelArray = GlobalColumnModelForReports[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.ds); 
        if (this.reportType == 1) {
            colArr = WtfGlobal.appendCustomColumn(colArr, GlobalColumnModel[this.moduleid]);
            colModelArray = GlobalColumnModel[this.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, this.ds);
        }
        
        this.cm = new Wtf.grid.ColumnModel(colArr);
        
        this.sm.on("selectionchange",function(){//on selecting grid enabling Print Record Button
            var selected = this.sm.getSelections();
            this.singleRowPrint.disable();
            if(selected.length>0){
                this.singleRowPrint.enable();  
            }
        },this);
        
        this.sm.on("rowselect",function (sm,index,record){
            if(record.data.deleted == true)
            {
                this.deleteBtn.disable();
            }
            else
            {
                this.deleteBtn.enable();
            }
        },this);
        
        var tbarArray = new Array();
        if (this.isJobWorkInReciever) {
            tbarArray.push("-" , this.createnew, "-", WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate") + ": ", this.frmDate, "-", WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate") + ": ", this.toDate, "-", WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore") + ": ", this.storeCmbfilter, "-",
                    WtfGlobal.getLocaleText("acc.stock.AdjustmentReason") + ": ", this.adjReasonCombo, "-", this.search, '-', this.resetBtn);
        } else {
            if (this.reportType == 1) {
                tbarArray.push("-", WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate") + ": ", this.frmDate, "-", WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate") + ": ", this.toDate, "-", WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore") + ": ", this.storeCmbfilter, "-", WtfGlobal.getLocaleText("acc.common.PLType") + ": ", this.adjustmentTypeFilter, "-",
                        WtfGlobal.getLocaleText("acc.stock.AdjustmentReason") + ": ", this.adjReasonCombo, "-", this.search, '-', this.resetBtn,'-',this.expandCollpseButton);
            } else {
                tbarArray.push("-", WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate") + ": ", this.frmDate, "-", WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate") + ": ", this.toDate, "-", WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore") + ": ", this.storeCmbfilter, "-",
                        WtfGlobal.getLocaleText("acc.stock.AdjustmentReason") + ": ", this.adjReasonCombo, "-", this.search, '-', this.resetBtn);
            }
        }
        /**************date *****/

        this.storeCmbStore.load();
        
        this.storeCmbStore.on("load", function(ds, rec, o){
            if(rec.length > 1){
                var newRec=new this.storeCmbRecord({
                    store_id:'',
                    fullname:'ALL'
                })
                this.storeCmbStore.insert(0,newRec);
                this.storeCmbfilter.setValue('');
            }else if(rec.length > 0){
                this.storeCmbfilter.setValue(rec[0].data.store_id, true);
            }
            this.storeCmbfilter.fireEvent('select');
            this.status = 1;
            this.initloadgridstore(this.frmDate.getValue().format('Y-m-d'),this.toDate.getValue().format('Y-m-d'),this.storeCmbfilter.getValue());
        }, this);

        //    this.storeCmbfilter.on("select", function(cmb, rec, index) {
        //        this.loadgridstore(this.frmDate.getValue().format(Wtf.getDateFormat()), this.toDate.getValue().format(Wtf.getDateFormat()));
        //    }, this);
        //    this.markoutTypeCmb.on("select", function(cmb, rec, index) {
        //        this.loadgridstore(this.frmDate.getValue().format(Wtf.getDateFormat()), this.toDate.getValue().format(Wtf.getDateFormat()));
        //    }, this);

        //  this.frmDate.on('blur',function(){
        //           if(!(this.frmDate.getValue()==""&&this.frmDate.getValue()!=null)&&
        //			   !(this.toDate.getValue()==""&&this.toDate.getValue()!=null))
        //           {
        //              if(this.frmDate.getValue()>this.toDate.getValue()){
        //                  Wtf.MessageBox.show({
        //                                title: 'Invalid Date',
        //                                msg: 'Report from date cannot be higher than report upto date',
        //                                buttons: Wtf.MessageBox.OK,
        //                                animEl: 'ok',
        //                                icon: Wtf.MessageBox.INFO
        //                            });
        //                  this.frmDate.setValue("");
        //                  return;
        //                } else {
        //                    //calc duration
        //                    var frm = this.frmDate.getValue().format(Wtf.getDateFormat());
        //                    var to = this.toDate.getValue().format(Wtf.getDateFormat());
        //                    if((frm!=""&&to!="")){
        //						this.loadgridstore(frm, to);
        //                    }
        //                    else return;
        //                }
        //            }
        //        },
        //        this);

        //        this.toDate.on('blur',function(){
        //           if(!(this.frmDate.getValue()==""&&this.frmDate.getValue()!=null)&&
        //               !(this.toDate.getValue()==""&&this.toDate.getValue()!=null)
        //             )
        //              {
        //                if(this.frmDate.getValue()>this.toDate.getValue()){
        //                    Wtf.MessageBox.show({
        //                                title: 'Invalid Date',
        //                                msg: 'Report upto date cannot be before report from date',
        //                                buttons: Wtf.MessageBox.OK,
        //                                animEl: 'ok',
        //                                icon: Wtf.MessageBox.INFO
        //                            });
        //                    this.toDate.setValue("");
        //                    return;
        //                 } else {
        //                    //calc duration
        //                    var frm = this.frmDate.getValue().format(Wtf.getDateFormat());
        //                    var to = this.toDate.getValue().format(Wtf.getDateFormat());
        //                    if((frm!=""&&to!="")){
        //						this.loadgridstore(frm, to);
        //                    }
        //                    else return;
        //                 }
        //              }
        //        },this);
        this.reportgenerate = new Wtf.Button({
            text:'Generate Report',
            scope:this,
            hidden: true== 18,
            handler:function(){
                this.generateReport(45);
                showReportStatus();
            }
        });
        this.summaryBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.lp.viewccs"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stockavailability.ClicktoViewStockAdjustmentDetail")
            },
            //            hidden:(Wtf.realroles[0] == 5 || Wtf.realroles[0] == 17 || Wtf.realroles[0] == 18),
            //disabled:true,
            scope:this,
            //            hidden:this.type==2?true:false,
            handler:function(){
               
                this.loadgridstore(this.frmDate.getValue().format('Y-m-d'), this.toDate.getValue().format('Y-m-d'));
                this.storeCmbfilter.setValue('',true);
            }
        });
        
        var deleteBtnArr = [];
         this.deleteBtn = new Wtf.Action({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.stockadjustment.deletetstockadjustment"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stockadjustment.deleteTT")
            },
            iconCls:'accountingbase menu-delete',
            scope:this,
            handler:function(){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.stockadjustment.delete"), function(btn){
                    if(btn == 'yes') {
                        this.deleteSA(false);
                    }else
                        return false;
                },this);
            }
        });
        deleteBtnArr.push(this.deleteBtn);
        
        this.deleteBtnPermanent = new Wtf.Action({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.stockadjustment.deletestockadjustmentpermanently"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stockadjustment.deletepermanentTT")
            },
            iconCls:'accountingbase menu-delete',
            scope:this,
            handler:function(){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.stockadjustment.deletepermanent"), function(btn){
                    if(btn == 'yes') {
                        this.deleteSA(true);
                    }else
                        return false;
                },this);
            }
        });
        deleteBtnArr.push(this.deleteBtnPermanent);
        
        this.deleteBtnMenu = new Wtf.Action({
            text:'Delete',
            scope: this,
            tooltip:'Delete Stock Adjustment Details',
            iconCls:'accountingbase menu-delete',
            menu: deleteBtnArr
        });
        
        this.exportBttn = new Wtf.exportButton({
            obj: this,
            id: 'stockadjustmentregisterexportid'+this.id,
            tooltip: "Export Report", //"Export Report details.", 
            filename:this.isJobWorkInReciever?"Job_Work_In_Register_v1":"Stock_Adjustment_Register_v1",
            menuItem:{
                csv:true,
                pdf:true,
                xls:true,
                detailedXls:(this.reportType == 1)?true:false,
//                isDetailPDF:(this.type == 1)?true:false
            },
            get:Wtf.autoNum.StockAdjustmentRegister,
            reportType: this.reportType,
            label:"Export"
        });
        
        this.printtBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stockavailability.ClicktoprintAdjustmentNote")
            },
            scope: this,
            id:'sadjreportprint',
            iconCls:'pwnd printButtonIcon',
            handler: function(){
                var selected = this.grid.getSelections();
                if(selected.length==1){
                    if(selected[0].get("seqNumber") != "" &&selected[0].get("seqNumber") != undefined){
                        printoutSA(selected[0].get("seqNumber"));
                    }
                }else if(selected.length > 1){
                    WtfComMsgBox(["Alert", "Please select 1 record at a time."],3);
                    return;
                }else{
                    WtfComMsgBox(["Alert", "Please select a record to print."],3);
                    return;
                }
            }
        });
        /*Print Record Button*/
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        
        var colModArray=[];
        if(this.isJobWorkInReciever){
            //get job work stock in module templates
            colModArray = GlobalCustomTemplateList[Wtf.autoNum.JobWorkStockInModuleID];
        } else{
            colModArray = GlobalCustomTemplateList[Wtf.Acc_Stock_Adjustment_ModuleId];
        }
        var isTflag=colModArray!=undefined && colModArray.length>0?true:false;
        if(isTflag){
            for (var count = 0; count < colModArray.length; count++) {
                var id1=colModArray[count].templateid;
                var name1=colModArray[count].templatename;           
                Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                    iconCls: 'pwnd printButtonIcon',
                    text: name1,
                    id: id1
                }); 
            }           
        }else{
            Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                iconCls: 'pwnd printButtonIcon',
                text:WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinCustomDesigner"),
                id: Wtf.No_Template_Id
            });
        }
        Wtf.menu.MenuMgr.get("printmenu" + this.id).on('itemclick',function(item) {
            this.printRecordTemplate('print',item);
        }, this);
        
        this.singleRowPrint = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.rem.236"),
            hidden:(WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.printstockreq)),
            iconCls:'pwnd printButtonIcon',
            tooltip:WtfGlobal.getLocaleText("acc.rem.236.single"),
            scope:this,
            disabled:true,
            menu:this.printMenu
        });
         
        var isIntegrateWithAccounting=true
        this.summary = new Wtf.grid.GroupSummary({});
        
        var bbarArray=new Array();
        if(this.reportType==1){
            if (!this.isJobWorkInReciever) {
                bbarArray.push(this.summaryBtn);
            }
            bbarArray.push("-",this.deleteBtnMenu);
//            bbarArray.push(this.deleteBtn);
//            bbarArray.push(this.deleteBtnPermanent);
        }
        if (!this.isJobWorkInReciever) {
            bbarArray.push("-",this.printtBttn);
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockadjustment, Wtf.Perm.stockadjustment.exportstockadj)) {
            bbarArray.push("-",this.exportBttn);
        }
//        if (!this.isJobWorkInReciever) {
            bbarArray.push("-", this.singleRowPrint);
//        }
        
        var productopeningqtyextraconfig = {}; 
        productopeningqtyextraconfig.url= "INVStockLevel/importStockAdjustment.do";
        var importSAproducts = Wtf.importMenuArray(this, "StockAdjustment", this.ds, "", productopeningqtyextraconfig);
        var importBtnArr = [];
        this.importProductopningBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.importSAproducts"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.importSAproducts"),
            iconCls: (Wtf.isChrome? 'pwnd importProductChrome' : 'pwnd importcsv'),
            menu: importSAproducts
        });
        importBtnArr.push(this.importProductopningBtn);
        
        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importBtnArr
        });
        bbarArray.push("-", this.importBtn);
        
        this.grid=new Wtf.KwlEditorGridPanel({
            id:"markoutList"+this.id,
            cm:this.cm,
            store:this.ds,
            sm:this.sm,
            loadMask:true,
            // stripeRows : true,
            //            viewConfig: {
            //                forceFit: true
            //            },
            //            view: grpView,
            plugins:[this.expander],//this.summary],
            tbar:tbarArray,
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchLabelSeparator:":",
            searchEmptyText: WtfGlobal.getLocaleText("acc.inventory.markout.grid.quicksearch"),
            serverSideSearch:true,
            searchField:"productCode",
            clicksToEdit:2,
            qsWidth:140,
            displayInfo: true,
            displayMsg: 'Displaying  {0} - {1} of {2}',
            emptyMsg: "No results to display",
            bbar:bbarArray
        });
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        
        this.grid.on("beforeedit",function(){
            return false;
        },this);
         
        this.grid.on('cellclick',this.onCellClick, this);
        

        //If you want to enable button ,if only one record selected ,otherwise disable
        //        var arrId=new Array();
        //        arrId.push("delete");//"deleteIssueBtn" id of button
        //        arrId.push("edit");
        //
        //        enableDisableButton(arrId,this.ds,sm);
        this.add(this.grid);
        
    },
    onCellClick:function(g,i,j,e){
        //        this.starCellClickHandler(g,i,j,e);
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if (header == "inventoryentryno") {
            var accid = this.ds.getAt(i).data['inventoryjeid'];
            var startDate,endDate;
            if(this.frmDate !=undefined && this.toDate !=undefined){
                startDate = this.frmDate.getValue();
                endDate = this.toDate.getValue();
            }
            callJournalEntryDetails(accid, true, undefined , undefined , undefined , undefined , startDate, endDate);
        } else  if(header=="seqNumber"){
            this.viewTransection(g,i,e)
        }
    },
    
    onRowexpand:function(scope, record, body){
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray,this.expandStore);
        this.expanderBody=body;
        this.expandStore.load({
            params: {
                transactionno:record.data.seqNumber,
                transactionID:record.data.id,
                type:this.reportType
            }
        });
    },
    
    expandCollapseGrid: function () {
        var arr = "";
        var store = this.grid.getStore();
        if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            for (var i = 0; i < store.data.length; i++) {
                this.expander.collapseRow(i)
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
            store.each(function (rec) {
                if (rec.data.id != "" && rec.data.id != undefined)
                    arr += rec.data.id + ",";
            }, this);
            if (arr.length != 0) {
                var colModelArray = GlobalColumnModel[this.moduleid];
                WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
//        this.expanderBody=body;
                arr = arr.substring(0, arr.length - 1);
                this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
            }
            this.expandStore.load({
                params: {
//                transactionno:'all',
                    transactionID: arr,
                    type: this.reportType
                }
            });
        }
    },
    openForm: function() {
      markoutallTab(true);  
    },
    
    fillExpanderBody: function () {
        if (this.expandStore.getCount() > 0) {
            this.custArr = [];
            this.custArr = WtfGlobal.appendCustomColumn(this.custArr, GlobalColumnModel[this.moduleid]);
            var colModelArray = GlobalColumnModel[this.moduleid];
            WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);

            var arr = [
                "Product ID", // "Product ID"
                "Product Name",
                "Product Description",
                "Cost Center",
                "Adjustment Type",
                "Remark",
                "Uom",
                "Quantity",
                "Unit Price",
                "Amount",
                "Reason"];

            var gridHeaderText = WtfGlobal.getLocaleText("acc.invoiceList.expand.pList"); // "Product List";

            var header = "<span class='gridHeader'>" + gridHeaderText + "</span>"; // "Product List"

            var arrayLength = arr.length;
            for (var custCount = 0; custCount < this.custArr.length; custCount++) {
                if (this.custArr[custCount].header != undefined) {
                    arr[arrayLength] = this.custArr[custCount].header;
                    arrayLength = arr.length;
                }
            }

            var count = 0;
            for (var i = 0; i < arr.length; i++) {
                if (arr[i] != "") {
                    count++;
                }
            }
            var widthInPercent = 100 / (count + 1);
            var minWidth = count * 100;
            header += "<div style='width: 100%;min-width:" + minWidth + "px'>";
            header += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";
            for (i = 0; i < arr.length; i++) {
                header += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + arr[i] + "</span>";
            }
            header += "</div><div style='width: 100%;min-width:" + minWidth + "px'><span class='gridLine'></span></div>";
            header += "<div style='width: 100%;min-width:" + minWidth + "px'>";

            var header1 = header;

            for (i = 0; i < this.expandStore.getCount(); i++) {
                var rec = this.expandStore.getAt(i);
                var header2 = "";
                var reason = "";            // ERP-41156
                if (rec.data['reason'] != null && rec.data['reason'] != undefined) {
                    reason = rec.data['reason'];
                }
                reason = reason.replace(/<\/?[^>]+(>|$)/g, "");   //SDP-9944
                if (reason == "") {
                    reason = '&nbsp';    //If HTML Content is <br> only.
                }
                
                // Column : S.No.
                if (this.expandButtonClicked) {
                    header2 += "<span class='gridNo'>" + 1 + ".</span>";
                } else {
                    header2 += "<span class='gridNo'>" + (i + 1) + ".</span>";
                }
                // Column : Product ID
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;' wtf:qtip='" + rec.data['productCode'] + "'><a class='jumplink' wtf:qtip='" + rec.data['productCode'] + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rec.data['productid'] + "\"," + this.isFixedAsset + ")'>" + Wtf.util.Format.ellipsis(rec.data['productCode'], 10) + "</a></span>";   // ERP-13247 [SJ]
                // Column : Product Name
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;' wtf:qtip='" + rec.data['productName'] + "'><a class='jumplink' wtf:qtip='" + rec.data['productName'] + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rec.data['productid'] + "\"," + this.isFixedAsset + ")'>" + Wtf.util.Format.ellipsis(rec.data['productName'], 10) + "</a></span>";   // ERP-13247 [SJ]
                // Column : Quantity of Product
                //   header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + rec.data['productDescription'] + "'>" + Wtf.util.Format.ellipsis(rec.data['productDescription'], 25) + "&nbsp;</span>";
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'' wtf:qtip='" + rec.data['productDescription'] + "'>&nbsp;" + Wtf.util.Format.ellipsis(rec.data['productDescription'], 20) + "</span>";
                //   header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;"+rec.data['costcenter']+"</span>";
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'' wtf:qtip='" + rec.data['costcenter'] + "'>&nbsp;" + Wtf.util.Format.ellipsis(rec.data['costcenter'], 20) + "</span>";
                //  header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['adjustmentType']+"</span>";
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'' wtf:qtip='" + rec.data['adjustmentType'] + "'>" + Wtf.util.Format.ellipsis(rec.data['adjustmentType'], 20) + "</span>";
                //  header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;"+rec.data['remark']+"</span>";
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'' wtf:qtip='" + rec.data['remark'] + "'>&nbsp;" + Wtf.util.Format.ellipsis(rec.data['remark'], 20) + "</span>";
                //     header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;"+rec.data['uomName']+"</span>";
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'' wtf:qtip='" + rec.data['quantity'] + "'>&nbsp;" + rec.data['quantity'] + " " + Wtf.util.Format.ellipsis(rec.data['uomName'], 20) + "</span>";
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;" + rec.data['quantity'] + " " + rec.data['uomName'] + "</span>";

                // Column : Unit Price of Product
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['cost'], rec.data['currencysymbol'], [true]) + "</span>";
                // Column : Amount
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(rec.data['amount'], rec.data['currencysymbol'], [true]) + "</span>";

                //   header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['reason']+"</span>";
                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>" + Wtf.util.Format.ellipsis(reason, 20) + "</span>";
                for (var cust = 0; cust < this.custArr.length; cust++) {
                    if (this.custArr[cust].header != undefined) {
                        if (rec.data[this.custArr[cust].dataIndex] != undefined && rec.data[this.custArr[cust].dataIndex] != "null" && rec.data[this.custArr[cust].dataIndex] != "") {
                            if (this.custArr[cust].xtype == "datefield") {
                                var linelevel_datefield = WtfGlobal.onlyDateRendererTZ(new Date(rec.data[this.custArr[cust].dataIndex] * 1));
                                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;" + linelevel_datefield + "</span>";
                            } else {
                                header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;" + rec.data[this.custArr[cust].dataIndex] + "</span>";
                            }
                        } else {
                            header2 += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;&nbsp;</span>";
                        }
                    }
                }
                header += header2;
                header += "<br>";

                if (this.expandButtonClicked) {
                    var moreIndex = this.grid.getStore().findBy(
                            function (record, id) {
                                if (record.get('id') === rec.data['id']) {
                                    return true;  // a record with this data exists 
                                }
                                return false;  // there is no record in the store with this data
                            }, this);
                    if (moreIndex != -1) {
                        var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                        var disHtml = "";
                        disHtml = "<div class='expanderContainer' style='width:100%'>" + header1 + header2 + "</div>";
                        body.innerHTML = disHtml;

                        this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                        this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                        header2 = "";

                    }
                }

            }
            if (!this.expandButtonClicked) {
                header += "</div>";
                var disHtml = "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
                this.expanderBody.innerHTML = disHtml;
            }

        } else {
            this.expanderBody.innerHTML = "<div class='expanderContainer' style='width:100%'>" + WtfGlobal.getLocaleText("acc.field.Nodatatodisplay") + "</div>"; // "No data to display"
        }
    },
    viewTransection:function(grid, rowIndex, columnIndex){
        var formrec=null;
        var type=(this.reportType==undefined?1:this.reportType);
        if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
            WtfComMsgBox(15,2);
            return;
        }
        formrec = this.grid.getStore().getAt(rowIndex);
        
        if (type == 1) {
            Wtf.Ajax.requestEx({
                url: 'INVStockAdjustment/getStockAdjustmentList.do',
                params: {
                    frmDate: this.frmDate.getValue().format('Y-m-d'),
                    toDate: this.toDate.getValue().format('Y-m-d'),
                    markouttype: "markout",
                    type: this.reportType,
                    summaryFlag: this.status == 1,
                    stockAdjustmentID: formrec.data.id,
                    isJobWorkInReciever: this.isJobWorkInReciever,
                    isview: true
                }
            },
            this,
            function (res) {
                if (res.data[0] !== undefined) {
                    res.data = res.data[0];
                    callViewmarkout(true, res, res.data.id, type, this.isJobWorkInReciever);
                }
            },
            function (res) {
            });
        } else {
            callViewmarkout(true,formrec,formrec.data.id, type,this.isJobWorkInReciever);
        }
    },

    printRecordTemplate:function(printflg,item){
        var moduleid="";
        var selected= this.sm.getSelections();
        var cnt = selected.length;
        var recordbillid=new Array();
        var transfernoteno=new Array();
        for(var i=0;i<cnt;i++){ //getting all selected records id and transaction no
            transfernoteno[i]=selected[i].get("seqNumber");
            recordbillid[i]=selected[i].json.id;
        }
        var params= "myflag=order&order&transactiono="+transfernoteno+"&moduleid="+Wtf.Acc_Stock_Adjustment_ModuleId+"&templateid="+item.id+"&recordids="+recordbillid+"&filetype="+printflg;  
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "ACCExportPrintCMN/exportSingleStockAdjustment.do";
        var inputs =params.split('&');
        for(var i=0;i<inputs.length;i++){
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = KV_pair[1];
            mapForm.appendChild(mapInput); 
        }
        document.body.appendChild(mapForm);
        mapForm.submit();
        var myWindow = window.open("", "mywindow","menubar=1,resizable=1,scrollbars=1");
        var div =  myWindow.document.createElement("div");
        div.innerHTML = "Loading, Please Wait...";
        myWindow.document.body.appendChild(div);
        mapForm.remove();
    },
    loadgridstore:function(frm, to){
        
        this.ds.baseParams = {
            //flag: 20,
            frmDate: frm,
            toDate: to,
            storeid: this.storeCmbfilter.getValue(),
            adjustmentType: this.adjustmentTypeFilter.getValue(),
            adjustmentreason:this.adjReasonCombo.getValue(),
            markouttype: "markout",
            summaryFlag: this.status == 1,
            dmflag:this.dmflag
        }
        
        if(this.status == 1) {
            //            this.storeCmbfilter.disable();
            this.printtBttn.hide();
            this.summaryBtn.setText("View Details");
            this.status = 0;
            this.cm.setHidden(1, true);
            this.cm.setHidden(2, true);
            this.cm.setHidden(3, true);
            this.cm.setHidden(4, true);
            this.cm.setHidden(5, true);
            this.cm.setHidden(6, true);
            this.cm.setHidden(7, true);
            this.cm.setHidden(12, true);
            this.cm.setHidden(13, true);
            this.cm.setHidden(14, true);//remark
            this.cm.setHidden(18, true);
            this.cm.setHidden(20, true);
            this.cm.setHidden(21, true);
            this.cm.setHidden(23, true);//memo
            var count=this.cm.getColumnCount()
            for (var i=22;i<count;i++){               //hide custom column in summury view
                this.cm.setHidden(i, true);
            }
            this.grid.reconfigure(this.ds, this.cm);
        } else {
            this.printtBttn.show();
            this.summaryBtn.setText("View Summary");
            this.status = 1;
            this.cm.setHidden(1, false);
            this.cm.setHidden(2, false);
//            this.cm.setHidden(3, false);
//            this.cm.setHidden(4, false);
//            this.cm.setHidden(5, false);
//            this.cm.setHidden(6, false);
//            this.cm.setHidden(7, false);
//            this.cm.setHidden(12, false);
//            this.cm.setHidden(13, false);
//            this.cm.setHidden(14, false);
//            this.cm.setHidden(18, false);   
//            this.cm.setHidden(20, false);
            var count=this.cm.getColumnCount();
            for (var j=22;j<count;j++){               //show custom column in detailed  view
                this.cm.setHidden(j, false);
            }
            for (var indexCount = 0; indexCount < count; indexCount++) {
            var tempDataIndex = this.cm.getDataIndex(indexCount);
                switch(tempDataIndex){
                    case 'seqNumber':this.cm.setHidden(indexCount, false); break;
                    case 'inventoryentryno':this.cm.setHidden(indexCount, false); break;
                    case 'storeAbbr':this.cm.setHidden(indexCount, false); break;
                    case 'locationname':this.cm.setHidden(indexCount, true); break;
                    case 'createdBy':this.cm.setHidden(indexCount, false); break; 
                    case 'date':this.cm.setHidden(indexCount, false); break; 
                    case 'adjustmentType':this.cm.setHidden(indexCount, false); break; 
                    case 'costcenter':this.cm.setHidden(indexCount, false); break; 
                    case 'remark':this.cm.setHidden(indexCount, false); break; 
                    case 'cost':this.cm.setHidden(indexCount, false); break; 
                    case 'reason':this.cm.setHidden(indexCount, false); break; 
                    case 'memo':this.cm.setHidden(indexCount, false); break;
                    case 'adjustmentreason': this.cm.setHidden(indexCount, false); break;
                    case 'jobworkorderno': this.cm.setHidden(indexCount, !Wtf.account.companyAccountPref.jobWorkOutFlow); break;
                }
            }
            //            this.cm.setHidden(21, false);
//            this.cm.setHidden(23, false);
//            var index = WtfGlobal.getColIndexByDataIndex(this.cm,"locationname");
//            if (index > -1) {
//                this.cm.setHidden(index, true);
//            } 
                this.grid.reconfigure(this.ds, this.cm);
        }
        
        //        if(this.status == 1){
        this.initloadgridstore(frm,to,this.storeCmbfilter.getValue());
    //        }
        
    },
    initloadgridstore:function(frm, to, storeid) {
        this.ds.baseParams = {
            //flag: 20,
            frmDate: frm,
            toDate: to,
            storeid: storeid,
            markouttype:"markout",
            summaryFlag: this.status == 1,
            dmflag:this.dmflag,
            isJobWorkInReciever:this.isJobWorkInReciever
        }
        this.ds.load({
            params:{
                start: 0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss: Wtf.getCmp("Quick"+this.grid.id).getValue(),
                isJobWorkInReciever:this.isJobWorkInReciever
            }
        });
        
    },
    deleteSA:function(isDeletePermanent){
        var selected = this.grid.getSelections();
         var arr=[];
         var isdeleteValid=false;
        if(selected.length>0){
            var cnt = selected.length;
            for(var j=0;j<cnt;j++){
                 
                if(selected[j].get("id") != "" &&selected[j].get("id") != undefined){
                    if(selected[j].json.transactionModule!= "Cycle Count") //if transaction is from cycle count do not allow the user to delete it
                    {
                        var jObj = {}
                        jObj.said= selected[j].get("id");
                        arr.push(jObj);
                        isdeleteValid=true;
                    }
                    else //if user tries to delete cycle count data then display this message 
                    {
                        isdeleteValid=false;
                        WtfComMsgBox(["Info","You cannot delete the data for Cycle Count,please select only Stock Adjustment data for deletion."],2);
                        return;
                    }
                }
                
            }
            if(isdeleteValid==true){
                Wtf.Ajax.requestEx({
                    url: 'INVStockAdjustment/deletSA.do',
                    params:{
                        reportid:79,
                        reportname:"Stockout Report",
                        said: JSON.stringify(arr),
                        isPermanent:isDeletePermanent
                    }
                },
                this,
                function (result,resp){
                    //                    var resObj=eval('('+result+')')
                    if(result.success== true) {
                        WtfComMsgBox(["Success", result.msg],3);
                        this.initloadgridstore(this.frmDate.getValue().format('Y-m-d'),this.toDate.getValue().format('Y-m-d'),this.storeCmbfilter.getValue());
                    } else {
                        WtfComMsgBox(["Warning", result.msg],2);
                        this.initloadgridstore(this.frmDate.getValue().format('Y-m-d'),this.toDate.getValue().format('Y-m-d'),this.storeCmbfilter.getValue());
                    }
                },
                function (){

                    });
            }
        } else{
            WtfComMsgBox(["Alert", "Please select a record to delete."],3);//ERP-28219
            return;
        }
    },
    exportReport: function(reportid, exportType){
        //this.limit = this.pg.pageSize;
        var recordCnt = this.grid.store.getTotalCount();
        if(recordCnt == 0){
            // msgBoxShow(["Error", "No records to export"], 0,1);
            return;
        }
        var repColModel = this.grid.getColumnModel();
        var numCols = this.grid.getColumnModel().getColumnCount();
        var colHeader = "[";
        for(var i = 1;i<numCols;i++){ // skip row numberer
            if(!(repColModel.isHidden(i))){
                colHeader += "{\"displayField\":\""+repColModel.getColumnHeader(i)+"\",";
                colHeader += "\"valueField\":\""+repColModel.getDataIndex(i)+"\"},";
            }
        }

        colHeader = colHeader.substr(0,colHeader.length-1)+"]";
        
        var summaryFlag = false;
        if(this.status==1)
        {
            summaryFlag = true;
        }
        var url =  "ExportDataServlet.jsp?" +"&mode=" + reportid +
        "&colHeader=" + encodeURIComponent(colHeader)+
        "&storeid=" + this.storeCmbfilter.getValue()+
        "&markouttype="+"markout"+
        //  "&status"+this.statusStoreCombo.getValue()=="All"?"":this.statusStoreCombo.getValue()+
        // "&type="+this.type+
        "&summaryFlag=" +summaryFlag+
        "&reportname=" + this.title +
        "&dmflag="+this.dmflag+
        "&exporttype=" + exportType +
        "&frmDate=" +this.frmDate.getValue().format(WtfGlobal.getDateFormat())+
        "&toDate=" + this.toDate.getValue().format(WtfGlobal.getDateFormat()) +
        "&ss="+ Wtf.getCmp("Quick"+this.grid.id).getValue()+
        "&start=0" +
        "&limit=10000" ;
                                     
        setDldUrl(url);
    },
    //        printdata:function(){
    //        var selected= this.grid.getSelectionModel().getSelections();
    //        var cnt = selected.length;
    //        var arr=[];
    //        for(var i=0;i<cnt;i++){
    //            var jObj = {}
    //            var finalremark = NewlineRemove(selected[i].get("remark"));
    //            jObj.itemid= selected[i].get("id");
    //            jObj.quantity=selected[i].get("quantity");
    //            jObj.uom=selected[i].get("uomid");
    //            jObj.uomDisplayValue= selected[i].get("uom");
    //            jObj.selfloactionName= finalremark;
    //            jObj.remark=finalremark;
    //            arr.push(jObj);
    //        }
    //        var str = "jspfiles/inventory/printOut.jsp?store=" +  this.storeCmbfilter.getValue();
    //                                    str += "&date="+new Date();
    //                                    str += "&mod=Admin";//Date.parseDate(this.countDate.getRawValue(), Wtf.getCompanyDateFormat()).format(Wtf.getDateTimeFormat())
    ////                                    str += "&subcat="+this.subcategoryCmb.getValue();
    //                                    str += "&flag=55";
    ////                                    str += "&typeFlag=1";
    //                                    str+="&action="+this.type;
    //                                    window.open(str, "mywindow","menubar=1,resizable=1,scrollbars=1");
    //    },
    generateReport:function(reportid){
        if(this.frmDate.getValue()>this.toDate.getValue()) {

            msgBoxShow(["Failure", 'Report from date cannot be higher than report upto date'], 1);
            this.toDate.setValue("");
            return;
        } else if((this.frmDate!="" && this.toDate!=""))  {
            var repColModel = this.grid.getColumnModel();
            var numCols = this.grid.getColumnModel().getColumnCount();
            var colHeader = "[";
            for(var i = 1;i<numCols;i++){ // skip row numberer
                if(!(repColModel.isHidden(i))){
                    colHeader += "{\"displayField\":\""+repColModel.getColumnHeader(i)+"\",";
                    colHeader += "\"valueField\":\""+repColModel.getDataIndex(i)+"\"},";
                }
            }

            var summaryFlag = false;
            if(this.status==1)
            {
                summaryFlag = true;
            }

            var fDate="" ,tDate="";
            if(this.frmDate.getValue() !="")
                fDate=this.frmDate.getValue().format(WtfGlobal.getDateFormat())
            if(this.toDate.getValue() !="")
                tDate=this.toDate.getValue().format(WtfGlobal.getDateFormat())
            colHeader = colHeader.substr(0,colHeader.length-1)+"]";
            Wtf.Ajax.requestEx({
                url:"jspfiles/inventory/inventory.jsp",
                params:{
                    flag:109,
                    reportid:79,
                    reportname:"Stockout Report",
                    frmDate: this.frmDate.getValue().format(WtfGlobal.getDateFormat()),
                    toDate: this.toDate.getValue().format(WtfGlobal.getDateFormat()),
                    storeid: this.storeCmbfilter.getValue(),
                    markouttype: "markout",
                    activity :"markout",
                    summaryFlag: summaryFlag,
                    dmflag:this.dmflag,
                    ss: Wtf.getCmp("Quick"+this.grid.id).getValue()
                }
            },
            this,
            function (result,resp){
                var resObj=eval('('+result+')')
                if(resObj.success== true) {
                    msgBoxShow(['Success', resObj.msg], 0,1);
                } else {
                    msgBoxShow(["Info", resObj.msg], 0,1);
                }
            },
            function (){

                });
        }
    }
});
