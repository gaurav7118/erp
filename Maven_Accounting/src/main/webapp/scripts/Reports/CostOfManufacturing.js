/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function callCostOfManufacturingReport(){

    var panel = Wtf.getCmp("CostOfManufacturingReport");
    if(panel==null){
        panel = new Wtf.account.CostOfManufacturingReport({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.CostOfManufacturingReport.Report"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.CostOfManufacturingReport.Reporttooltip"),
            id:'CostOfManufacturingReport',
            border:false,
            searchJson:"",
            filterConjuctionCrit:"",
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}


/*******************************************************************************
 *             Cost of Manufacturing Report
 *******************************************************************************/

Wtf.account.CostOfManufacturingReport = function(config) {
    
    this.id = config.id;
    this.btnArr = [];
    this.calculation_based_on=0;//Cost of Product
    
    Wtf.apply(this, config);

    this.createGrid();
    
    this.createReportButtons();
    
    this.addButtonInArray();
    
    this.Store.on('beforeload', this.setFitleringParametersBeforeLoadingStore, this);
    this.Store.on('load', this.handleStoreOnLoad, this);
    this.Store.on('loadexception', this.callLoadException, this);
    
    Wtf.account.CostOfManufacturingReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.CostOfManufacturingReport, Wtf.Panel, {
    onRender: function(config) {

        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    tbar: this.btnArr,
                    items:[this.grid],
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
//                                searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        }),
                        items : ["-",this.exportButton, this.printButton]
                    })
                }
            ]
        });
        this.add(this.leadpan);
        
        this.loadStore();
                
        Wtf.account.CostOfManufacturingReport.superclass.onRender.call(this, config);
    },
    handleStoreOnLoad: function(store) {
        WtfGlobal.resetAjaxTimeOut();
        this.loadMask.hide();

        var columns = [];
        this.Store.sort("customfield","ASC");
        columns.push(this.sm);
        this.rowNo = new Wtf.grid.RowNumberer();
        columns.push(this.rowNo);
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "cost_of_product") {
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
                column.header  = column.header+" ("+WtfGlobal.getCurrencySymbol()+")";
            }else if(column.ispercentage){
                column.renderer = WtfGlobal.percentageRenderer;
            }else if ((column.dataIndex == "total_expenses" || column.dataIndex == "total_cost_of_product")) {// && column.calculationBasedOn==0
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
                column.header  = column.header+" ("+WtfGlobal.getCurrencySymbol()+")";
            } else if (column.applycurrency && column.applycurrency == true) {
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
                column.header  = column.header+" ("+WtfGlobal.getCurrencySymbol()+")";
            } 
            
            
            if (column.summaryRenderer) {
                column.summaryRenderer = eval('(' + column.summaryRenderer + ')');
            }
            columns.push(column);
        });
        
        var Arr = [];
        Wtf.each(this.Store.reader.jsonData.metaData.fields, function(column) {
            Arr.push(column);
        });
        
        this.groupStore.removeAll();
        this.groupStore.fields = Arr;
        this.grid.getColumnModel().setConfig(columns);
        this.groupStore.add(this.Store.getRange(0, (this.Store.data.items.length - 1)));
        this.grid.getView().refresh();
        
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton){
                this.exportButton.enable();
                
                this.exportButton.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()); 
                this.exportButton.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                this.exportButton.params.productIds = this.productCombo.getValue()==""?"All":this.productCombo.getValue();
                this.exportButton.params.calculationBasedOn = this.calculationBasedOnCombo.getValue();
                this.exportButton.params.dimension = this.CMDimension.getValue();
                this.exportButton.params.customfieldname = this.CMDimension.getRawValue();
                this.exportButton.params.customValues = this.CMValue.getRawValue();
            }
            if(this.printButton){
                this.printButton.enable();
                                
                this.printButton.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()); 
                this.printButton.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                this.printButton.params.productIds = this.productCombo.getValue()==""?"All":this.productCombo.getValue();
                this.printButton.params.calculationBasedOn = this.calculationBasedOnCombo.getValue();
                this.printButton.params.dimension = this.CMDimension.getValue();
                this.printButton.params.customfieldname = this.CMDimension.getRawValue();
                this.printButton.params.customValues = this.CMValue.getRawValue();
            }
        }
        
//        this.costCategoryExpenseStore.load();
//        this.costCategoryExpenseStore.on('load', this.handleCostCategoryExpenseStoreOnLoad, this);

        Wtf.MessageBox.hide();
    },
//    handleCostCategoryExpenseStoreOnLoad: function(store) {
//        var columns = [];
//        columns.push(this.sm);
//        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
//            if(store){
//                for(var i=0 ; i<store.length ; i++){
//                    if(column.dataIndex == store.data[i].id){
//                        column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
//                        column.header  = column.header+" ("+WtfGlobal.getCurrencyName()+")";
//                    }
//                }
//            }
//            columns.push(column);
//        });
//        
//        this.grid.getColumnModel().setConfig(columns);
//        this.grid.getView().refresh();
//    },
    loadStore: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                pagingFlag: true
            }
        });

    },
    fetchStatement: function() {
        if (!this.checkDates()) {
            return;
        }
        
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                pagingFlag: true,
                productIds :this.productCombo.getValue()==""?"All":this.productCombo.getValue(),
                calculationBasedOn :this.calculationBasedOnCombo.getValue()
            }
        });
    },
    createGrid: function() {
        
        this.productRec = Wtf.data.Record.create ([
            {name: 'productid'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);
   
        this.productStore = new Wtf.data.Store({
            url:"ACCProductCMN/getProductsIdNameforCombo.do",
            baseParams: {
                mode: 22,
                excludeParent:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        
        this.productComboConfig = {
            hiddenName: "productid",        
            store: this.productStore,
            valueField: 'productid',
            hideLabel: true,
            displayField: 'productname',
            emptyText: WtfGlobal.getLocaleText("acc.prod.comboEmptytext"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this
        };

        this.productCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
            forceSelection: true,
//            extraFields: ['pid','type'],
//            extraComparisionField: 'pid', // type ahead search on product id as well.
            listWidth: Wtf.ProductComboListWidth,
            width: 150
        },this.productComboConfig));
        
        this.productCombo.on('blur', function(field){
            if(this.productCombo.getValue() == undefined || this.productCombo.getValue() == ''){
                if(this.productStore.getCount()>0){
                    this.productCombo.setValue(this.productStore.getAt(0).data.productid);
                }
            }
        }, this);
        
        this.productStore.on('load', function(store){
            var storeNewRecord=new this.productRec({
                productid:'All',
                pid:'All',
                type:'',
                productname:'All',
                desc:'',
                producttype:''
            });
            store.insert(0, storeNewRecord);
            if(store.getCount()>0){
                this.productCombo.setValue(store.getAt(0).data.productid);
            }
        }, this);
        this.productStore.load();
        
        
  
        
        
        
        this.calculationBasedOnStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data :[['0',WtfGlobal.getLocaleText("acc.CostOfManufacturingReport.header3")],
                ['1',WtfGlobal.getLocaleText("acc.CostOfManufacturingReport.header2")]
            ]
        });

        this.calculationBasedOnCombo = new Wtf.form.ComboBox({
            store: this.calculationBasedOnStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.CostOfManufacturingReport.calculationBasedOn"),
            name:'calculationBasedOnCombo',
            displayField:'name',
            value:'0',
            editable:false,
            allowBlank:false,
            valueField:'id',
            mode: 'local',
            triggerAction: 'all'
        });
        
        this.calculationBasedOnCombo.on('select', function(a,b,c){
            this.calculation_based_on = this.calculationBasedOnCombo.getValue();
        }, this);
        
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : 'Loading...'
        });

        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        
        this.Store = new Wtf.data.Store({
            url: "ACCOtherReports/getCostOfManufacturing.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            })
//            groupField: "name",
//            sortInfo: {
//                field: 'name', 
//                direction: "ASC"
//            }
        });
        
        this.groupStore = new Wtf.data.GroupingStore({
            groupField: ['customfield']
        });
        /**
         * 1st Option
         */
        this.CMDimensionRec = Wtf.data.Record.create([
            {name: 'fieldid'},
            {name: 'fieldlabel'},
        ]);
        this.CMDimensionStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getFieldParamsForCombo.do",
            baseParams: {
                moduleidarray: Wtf.Acc_Product_Master_ModuleId,
                fieldtype:7,
                iscustomfield:1,
                isActivated: 1
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.CMDimensionRec)
        });
        this.CMDimension = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.CustomField*"),
//            emptyText: WtfGlobal.getLocaleText("acc.salescommission.dimension.emptyText"),
            hiddenName: 'fieldid',
            name: 'fieldid',
            store: this.CMDimensionStore,
            valueField: 'fieldid',
            displayField: 'fieldlabel',
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            width: 150,
            listWidth: 150
        });

        this.CMDimension.on('select', this.onCMSelection, this);
        /**
         * 2 nd Option
         */

        this.CMValueRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
        ]);
        this.CMValueStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getCustomCombodata.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.CMValueRec)
        });
        
        this.CMValueStore.on("load",function(){
            var rec = new this.CMValueRec({
                id : "all",
                name : "All"
            });
            this.CMValueStore.insert(0,rec);
            this.CMValue.setValue("all");
        },this);
        
        this.CMValue = new Wtf.common.Select({
            fieldLabel: WtfGlobal.getLocaleText("acc.salescommission.dimension"),
            multiSelect:true,
            hiddenName: 'id',
            name: 'id',
            store: this.CMValueStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            width: 150,
            listWidth: 150,
            disabled: true
        });
        
        this.CMValue.on('select',function(combo,rec){
            if(rec.get('id')=='all'){
                combo.clearValue();
                combo.setValue('all');
            }else if(combo.getValue().indexOf('all')>=0){
                combo.clearValue();
                combo.setValue(rec.get('id'));
            }
        }, this);

        this.gridView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: false,
            enableGroupingMenu: true,
            hideGroupedColumn: false,
            emptyText: "<div class='grid-empty-text'>" + WtfGlobal.getLocaleText("acc.common.norec") + "</div>"
        });
        var gridSummary = new Wtf.grid.GroupSummary({});
        
        this.grid = new Wtf.grid.GridPanel({
            store: this.groupStore,
            border: false,
            columns: [{
                dataIndex: "customfield"
            }],
            layout: 'fit',
            sm: this.sm,
            region: "center",
            stripeRows: true,
            viewConfig: this.gridView,
            plugins:[gridSummary],
            view: this.gridView
        });
    },    
        onCMSelection: function() {
        this.CMValue.enable();
        this.CMValueStore.load({
            params: {
                mode: 2,
                flag: 1,
                fieldid: this.CMDimension.getValue()
            }
        })
    },
    createReportButtons: function() {
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), // 'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });

        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), // 'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate',
            value: WtfGlobal.getDates(false)
        });
        
        this.checkDates();
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportButton" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
            filename:  WtfGlobal.getLocaleText("acc.CostOfManufacturingReport.Report"),
            disabled :true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                rowPdf: false,
                xls: true
            },
            params: {
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                productIds : this.productCombo.getValue()==""?"All":this.productCombo.getValue(),
                calculationBasedOn : this.calculationBasedOnCombo.getValue(),
                dimension : this.CMDimension.getValue(),
                customfieldname : this.CMDimension.getRawValue(),
                customValues : this.CMValue.getRawValue()
            },
            get: Wtf.autoNum.CostOfManufacturingReport 
        });

        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            label:"Print",
            menuItem:{
                print:true
            },
            params: {
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                productIds : this.productCombo.getValue()==""?"All":this.productCombo.getValue(),
                calculationBasedOn : this.calculationBasedOnCombo.getValue(),
                dimension : this.CMDimension.getValue(),
                customfieldname : this.CMDimension.getRawValue(),
                customValues : this.CMValue.getRawValue()
            },
            get:Wtf.autoNum.CostOfManufacturingReport
        });
        
        
    },
    checkDates: function(){
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();

        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return false;
        }

        this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return false;
        }
        return true;
    },
    addButtonInArray: function() {
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.btnArr.push(this.startDate);
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.btnArr.push(this.endDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.field.CustomField*"), this.CMDimension);
        this.btnArr.push('-', this.CMValue);
        this.btnArr.push(WtfGlobal.getLocaleText("acc.CostOfManufacturingReport.calculationBasedOn"),this.calculationBasedOnCombo);
        this.btnArr.push("-");
        this.btnArr.push(WtfGlobal.getLocaleText("acc.field.SelectProduct"),this.productCombo);
        this.btnArr.push("-");
        this.btnArr.push(this.fetchBttn);
    },
    setFitleringParametersBeforeLoadingStore: function (s, o) {
        WtfGlobal.setAjaxTimeOut();
        if (!o.params){
            o.params = {};
        }
        o.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()); 
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        o.params.productIds = this.productCombo.getValue()==""?"All":this.productCombo.getValue();
        o.params.calculationBasedOn = this.calculationBasedOnCombo.getValue();
        o.params.dimension = this.CMDimension.getValue();
        o.params.customfieldname = this.CMDimension.getRawValue();
        o.params.customValues = this.CMValue.getRawValue();

        this.loadMask.show();
    },
    callLoadException: function () {
        WtfGlobal.resetAjaxTimeOut();
        this.loadMask.hide();
    }
});