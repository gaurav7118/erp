/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Load Dynamic customervendorregistry.js
 */
function  callCustomerRegistryReportDynamicLoad(searchStr, filterAppend){
    var panel = Wtf.getCmp("CustomerRegistryReport");
    if(panel==null){
        panel = new Wtf.account.CustomerVendorRegisteryReport({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.customerregistry.tabTitle")),
            tabTip:WtfGlobal.getLocaleText("acc.customerregistry.tabTitle"),  // "Account Forecast Report",
            id:'CustomerRegistryReport',
            closable:true,
            border:false,
            isCustomer:true,
            fromMaster:false,
            iconCls:'accountingbase vendor',
            activeTab:0,
            searchStr:searchStr
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    showAdvanceSearch(panel,searchStr, filterAppend);
    Wtf.getCmp('as').doLayout();
}

function  callVendorRegistryReportDynamicLoad(searchStr, filterAppend){
    var panel = Wtf.getCmp("VendorRegistryReport");
    if(panel==null){
        panel = new Wtf.account.CustomerVendorRegisteryReport({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.vendorregistry.tabTitle")),
            tabTip:WtfGlobal.getLocaleText("acc.vendorregistry.tabTitle"),  // "Account Forecast Report",
            id:'VendorRegistryReport',
            closable:true,
            border:false,
            isCustomer:false,
            fromMaster:false,
            iconCls:'accountingbase vendor',
            activeTab:0,
            searchStr:searchStr
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    showAdvanceSearch(panel,searchStr, filterAppend);
    Wtf.getCmp('as').doLayout();
}

//*************************************************************************************************************

Wtf.account.CustomerVendorRegisteryReport = function(config) {
    this.arr = [];
    Wtf.apply(this,config);
    if(this.searchStr==undefined){
        this.firstload=true;
    }
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: false
    });
    this.Store = new Wtf.data.Store({
        url: (this.isCustomer)?"ACCCustomerCMN/getCustomerRegistryDetails.do":"ACCVendorCMN/getVendorRegistryDetails.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'totalCount'
        })
    });
    
    this.startDate = new Wtf.ExDateFieldQtip({
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: WtfGlobal.getDates(true)
    });
    
    var colArr = [];
    this.cm = new Wtf.grid.ColumnModel(colArr);
    this.summary = new Wtf.grid.GroupSummary();
    
   
    this.endDate = new Wtf.ExDateFieldQtip({
        name: 'enddate',
        format: WtfGlobal.getOnlyDateFormat(),
        value: WtfGlobal.getDates(false)
    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        hidden: this.isSOPOByProductReport,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    var loadingMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    
    this.Store.on('beforeload',function(s,o){
        WtfGlobal.setAjaxTimeOut();
        if (!this.MultiSelectAccCombo.validate() && !this.firstload) {
            return;
        }
        if(!this.firstload){
            loadingMask.show();
        }
        if(!o.params)o.params={};
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startDate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   
        currentBaseParams.endDate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());  
        currentBaseParams.person = this.firstload?"$":this.MultiSelectAccCombo.getValue();
        currentBaseParams.moduleid=this.moduleCombo.getValue();
        currentBaseParams.reportId=(this.isCustomer)?Wtf.autoNum.customerRegistryReport:Wtf.autoNum.vendorRegistryReport;
        currentBaseParams.status= this.status.getValue();
        this.Store.baseParams=currentBaseParams;
        this.objsearchComponent.person=this.MultiSelectAccCombo.getValue();
        this.objsearchComponent.modules=this.moduleCombo.getValue();
    },this);
    this.fetchButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.loadStore
    });
   
    this.salesModules=[
            [Wtf.Acc_Invoice_ModuleId, "Sales Invoice and Cash Sale"],
            [Wtf.Acc_Customer_Quotation_ModuleId, "Customer Quotation"],
            [Wtf.Acc_Sales_Return_ModuleId, "Sales Return"],
            [Wtf.Acc_Sales_Order_ModuleId, "Sales Order"],
            [Wtf.Acc_Delivery_Order_ModuleId, "Delivery Order"],
            [Wtf.Acc_Credit_Note_ModuleId, "Credit Note"],
            [Wtf.Acc_Debit_Note_ModuleId, "Debit Note"],
            [Wtf.Acc_Receive_Payment_ModuleId, "Payments Received"],
            [Wtf.Acc_Make_Payment_ModuleId, "Payments Made"]];
        
    this.purchaseModules=[
            [Wtf.Acc_Vendor_Invoice_ModuleId, "Purchase Invoice and Cash Purchase"],
            [Wtf.Acc_Vendor_Quotation_ModuleId, "Vendor Quotation"],
            [Wtf.Acc_Purchase_Return_ModuleId, "Purchase Return"],
            [Wtf.Acc_Purchase_Order_ModuleId, "Purchase Order"],
            [Wtf.Acc_Goods_Receipt_ModuleId, "Goods Receipt"],
            [Wtf.Acc_Debit_Note_ModuleId, "Debit Note"],
            [Wtf.Acc_Credit_Note_ModuleId, "Credit Note"],
            [Wtf.Acc_Make_Payment_ModuleId, "Payments Made"],
            [Wtf.Acc_Receive_Payment_ModuleId, "Payments Received"]];
   
    this.moduleStore = new Wtf.data.SimpleStore({
        fields: ["id", "name"],
        data: (this.isCustomer)?this.salesModules:this.purchaseModules
    });
    this.moduleComboconfig = {
        store: this.moduleStore,
        valueField:'id',
        hideLabel:true,
        displayField:'name',
        emptyText:WtfGlobal.getLocaleText("acc.rem.105"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
    
    this.moduleCombo = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         forceSelection:true,    
         listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
         width:200
    },this.moduleComboconfig));
    
    if(this.searchStr!=undefined && this.searchStr!=""){
        var searchStr = eval("(" + decodeURIComponent(this.searchStr) + ")");
        this.personid=searchStr.data[0].personid;
        this.moduleCombo.setValue(searchStr.data[0].moduleids);
    }
    
    this.personRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }, {
            name: 'acccode'
        }
    ]);
   
    this.personStore = new Wtf.data.Store({//Customer/vendor multi selection Combo
        url: (this.isCustomer) ? "ACCCustomer/getCustomersForCombo.do" : "ACCVendor/getVendorsForCombo.do",
        baseParams: {
            deleted: false,
            nondeleted: true,
            combineData: -1  //Send For Seprate Request
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.personRec)
    });

    this.personStore.load();
   
    this.personName ={
        hiddenName:'accountmulselectcombo',
        store: this.personStore,
        displayField: 'accname',
        valueField: 'accid',
        mode: 'local',
        width: 100,
        listWidth: 50,
        defaultValue: 0,
        triggerAction: 'all',
        typeAhead: true,
        emptyText: (this.isCustomer)?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") ,
        selectOnFocus: true,
        scope:this,
        hideLabel:false,
        clearTrigger:true,
        hidden : false
        
    };
    /*
     *A multiselect ComboBox to search customers/vendors by customer/vendor name or customer/vendor code
     **/
    this.MultiSelectAccCombo = new Wtf.common.Select(Wtf.applyIf({
        id:'mulaccountcombo'+this.id,
        multiSelect:true,
        fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccounts") ,
        forceSelection:true,  
        extraFields:['acccode'],
        extraComparisionField:'acccode',
        width:200,
        listWidth:100  
    },this.personName));
    
    this.statuses = [
        ["all", "All"],
        ["Closed", "Closed"],
        ["Open", "Open"],
        ["Expired", "Expired"],
    ];
    
    this.statusStore=new Wtf.data.SimpleStore({
        fields: ["id", "name"],
        data: this.statuses
    });
    this.status = new Wtf.form.ComboBox({
        store: this.statusStore,
        displayField: 'name',
        valueField: 'id',
        mode: 'local',
        width: 100,
        listWidth: 100,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true,
        value:"all"
    });
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.customerregistry.searchtext"),
        width: 150,
        id: "quickSearch" + config.helpmodeid + config.id,
        field: 'billno',
        Store: this.Store
    })
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClick, this);
    
    this.customReportViewBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
        handler: this.customizeView,
        iconCls: 'accountingbase fetch'
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        params:{
            name: "Customer Registry Report",
            startDate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            isExport:true
        },
        filename: (this.isCustomer) ?WtfGlobal.getLocaleText("acc.customerregistry.tabTitle"):WtfGlobal.getLocaleText("acc.vendorregistry.tabTitle"),
        menuItem:{
            print:true
        },
        get: (this.isCustomer) ?Wtf.autoNum.customerRegistryReport:Wtf.autoNum.vendorRegistryReport
    });
    this.exportButton = new Wtf.exportButton({
        obj: this,
        id: "exportReports" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        scope: this,
        filename: (this.isCustomer)?WtfGlobal.getLocaleText("acc.customerregistry.tabTitle")+"_v1":WtfGlobal.getLocaleText("acc.vendorregistry.tabTitle")+"_v1",
        menuItem: {
            csv: true, 
            pdf: true, 
            rowPdf: false, 
            xls: true
        },
        params: {
            name: (this.isCustomer)?WtfGlobal.getLocaleText("acc.customerregistry.tabTitle"):WtfGlobal.getLocaleText("acc.vendorregistry.tabTitle"),
            startDate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            endDate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            isExport:true
        },
        get: (this.isCustomer)?Wtf.autoNum.customerRegistryReport:Wtf.autoNum.vendorRegistryReport
    });
    
    this.exportButton.on("click", function () {
        this.exportButton.setParams({
            person:this.MultiSelectAccCombo.getValue(),
            moduleid:this.moduleCombo.getValue()
        });
    }, this);
    this.bottomBtnArr = [];
    this.bottomBtnArr.push('-', this.exportButton,"-",this.printButton);
    this.Store.on("dataChanged", function(){
            this.loadData();
    }, this);
    
    this.Store.on('load', function() {
        if(this.Store.getTotalCount()==0 && !this.firstload){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
        }
        this.loadData();
        this.firstload=false;
        loadingMask.hide();
    }, this);
    
    this.personStore.on('load', function() {
        if(this.fromMaster || (this.personid!="" && this.personid!=undefined)){
            this.MultiSelectAccCombo.setValue(this.personid);
            this.loadStore();
        }
    }, this);
    this.tbar1 = [];
    this.tbar1.push(this.quickPanelSearch,"-", this.AdvanceSearchBtn,"-", WtfGlobal.getLocaleText("acc.common.from"),this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate,"-",(this.isCustomer)? WtfGlobal.getLocaleText("acc.up.3"):WtfGlobal.getLocaleText("acc.up.4"),this.MultiSelectAccCombo,"-",WtfGlobal.getLocaleText("acc.field.Modules"),this.moduleCombo,"-",WtfGlobal.getLocaleText("acc.cc.8"),this.status,"-", this.fetchButton, "-", this.resetBttn,"->", this.customReportViewBtn );

    this.pag=new Wtf.PagingSearchToolbar({
        pageSize: 30,
        border : false,
        id : "paggintoolbar_ProductGrid"+this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        scope:this,
        plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
            id : "pPageSize_ProductGrid_"+this.id
        }),
        items: this.bottomBtnArr,
        autoWidth : true,
        displayInfo:true//,
    })
    this.grid = new Wtf.grid.GridPanel({
        store: this.Store,
        height: 500,
        scope:this,
        columns: [],
        border: false,
        displayInfo: true,
        disabledClass: "newtripcmbss",
        viewConfig: {
                forceFit:true,
                emptyText:(this.isCustomer)?WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.customer.singleselect")):WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.vendor.singleselect"))
            },
        autoScroll: true
    });
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: (this.isCustomer)?'2,22,29,20,27,12,10,16,14'.split(','):'6,23,31,18,28,10,12,14,16'.split(','),
        advSearch: true,
        parentPanelSearch: this,
        ignoreDefaultFields:true,
        isAvoidRedundent:true,
        reportid:(this.isCustomer)?Wtf.autoNum.customerRegistryReport:Wtf.autoNum.vendorRegistryReport
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [this.objsearchComponent,
        {
            region: 'center',
            border: false,
            layout: "fit",
            autoScroll: true,
            tbar: this.tbar1,
            items: [this.grid],
            bbar:  this.pag
        }]
    });
    
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.leadpan]
    });
    if (this.searchStr == undefined) {
        this.Store.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    }
    Wtf.account.CustomerVendorRegisteryReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.CustomerVendorRegisteryReport, Wtf.Panel, {
    onRender:function(config){
        Wtf.account.CustomerVendorRegisteryReport.superclass.onRender.call(this,config);
    },
    handleResetClick:function(){
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));
        this.moduleCombo.reset();
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
        }
        this.Store.removeAll();
        this.grid.getView().emptyText=(this.isCustomer)?WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.customer.singleselect")):WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.vendor.singleselect"))
        this.MultiSelectAccCombo.reset();
    },
    loadData:function(){
        var columns = [];
        columns.push(new Wtf.KWLRowNumberer({
            width: 30
        }));
        
        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
            if (column.dataIndex == "amount" || column.dataIndex == "amountdue") {
                if (column.summaryRenderer) {
                    column.summaryRenderer = eval('(' + column.summaryRenderer + ')');
                }
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
            }
            if (column.dataIndex == "documentno") {
                column.renderer = WtfGlobal.linkRendererForAll;
            }
            if (column.fieldType == 3) {
                column.renderer =WtfGlobal.onlyDateRendererDateTZ;
            }
            columns.push(column);
        });
        
        var Arr = [];
        Wtf.each(this.Store.reader.jsonData.metaData.fields, function(column) {
            Arr.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getColumnModel().on("configchanged", function(){
            alert("configchanged");
        },this);
        this.grid.getView().refresh();  
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    }, 
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            pendingApproval: this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleCombo.getValue(),
            filterConjuctionCriteria: filterConjuctionCriteria,
            startDate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),   
            endDate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),  
            reportId: (this.isCustomer)?Wtf.autoNum.customerRegistryReport:Wtf.autoNum.vendorRegistryReport
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pPageSizeObj.combo.value
            }
        });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            moduleid: this.moduleCombo.getValue(),
            isFixedAsset: false,
            isLeaseFixedAsset: false,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pPageSizeObj.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
    customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: (this.isCustomer)?Wtf.autoNum.customerRegistryReport:Wtf.autoNum.vendorRegistryReport,
            modules:(this.isCustomer)?'2,22,29,20,27,12,10,16,14':'6,23,31,18,28,10,12,14,16'
        });
        this.customizeViewWin.show();
        var arr = this.arr;
    },
    loadStore: function () {
        if (this.MultiSelectAccCombo.validate()) {
            var sDate = this.startDate.getValue();
            var eDate = this.endDate.getValue();
            if (sDate > eDate) {
                WtfComMsgBox(1, 2);
                return;
            }
            this.Store.load({
                params: {
                    start: 0,
                    person: this.MultiSelectAccCombo.getValue(),
                    moduleid: this.moduleCombo.getValue(),
                    limit: this.pPageSizeObj.combo.value,
                    status: this.status.getValue(),
                    ss: this.quickPanelSearch.getValue()
                }
            });
        }
    }
});
