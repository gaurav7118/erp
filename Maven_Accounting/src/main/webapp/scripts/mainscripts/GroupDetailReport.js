/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Wtf.account.GroupDetailReport = function(config) {
    Wtf.apply(this,config);
    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.id = config.id;
    this.accountID = config.accountID;
    this.stDate = config.stDate;
    this.enDate = config.enDate;
    this.currencyFilterConfig = config.currencyFilterConfig;
    this.isConfigLoaded=false;
    this.excludePreviousYearConfig = config.excludePreviousYearConfig;
    this.moduleid= (config.isDefaultGL) ? Wtf.ACC_GENERAL_LEDGER_REPORT_MODULEID : Wtf.ACC_GROUP_DETAIL_REPORT_MODULEID;  // Used to Store grid config.
    this.FinalStatementRec = new Wtf.data.Record.create([
        {name: 'accountname'},
        {name: 'accountcode'},
        {name: 'aliascode'},
        {name: 'accountid'},
        {name: 'openingbalanceinaccountcurrency'},
        {name: 'openingBalanceType'},
        {name: 'openingamount'},
        {name: 'periodamount'},
        {name: 'endingamount'},
        {name: 'amount'},
        {name: 'preopeningamount'},
        {name: 'preperiodamount'},
        {name: 'preamount'},
        {name: 'accountflag'},
        {name: 'isdebit', type: 'boolean'},
        {name: 'level'},
        {name: 'fmt'},
        {name: 'leaf'},
        {name: 'creationDate', type: 'date'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'accountgroupname'},
        {name: 'currencyCode'},
        {name: 'deleted', type: 'boolean'},
        {name: 'totalFlagAccountsWithchild'}
    ]);
    this.accRec = Wtf.data.Record.create([
        {name: 'accountname', mapping: 'accname'},
        {name: 'accountid', mapping: 'accid'},
        {name: 'currencyid', mapping: 'currencyid'},
        {name: 'acccode'},
        {name: 'groupname'}
    ]);

    this.accStore = new Wtf.data.Store({
        url: "ACCAccountCMN/getAccountsForCombo.do",
        baseParams: {
            mode: 2,
            ignorecustomers: true,
            ignorevendors: true,
            nondeleted: true,
            headerAdded: true,
            controlAccounts: true,
            childAccountsFlag : true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.accRec)
    });

    this.accStore.on('load', function() {
        this.showGroupDetailsReport(config);
    }, this);
    this.accStore.load();
    
    this.gridStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.FinalStatementRec),
        url: "ACCReports/getGroupDetailReport.do"
    });
    
    this.expander = new Wtf.grid.RowExpander({
        renderer: function(v, p, record) {
            var val= '<div></div>';
            if (record.get("leaf")&& record.get("level")!=0) {
                val = '<div class="x-grid3-row-expander"></div>';
            }
            return val;
        },
        expandOnEnter: false,
        expandOnDblClick: false
    });
    this.createExpanderStore();
    this.expander.on("expand",this.onRowexpand,this);
    
    this.grid = new Wtf.grid.HirarchicalGridPanel({
        autoScroll: true,
        store: this.gridStore,
        hirarchyColNumber: 1,
        columns: [],
        plugins:[ this.expander],
        border: false,
        loadMask: true,
        viewConfig: {
//            forceFit: true,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec') + "<br>" + WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn'))
        }
    });
    this.grid.on("render", this.handleOnRendererEvent,this);
    this.grid.on('rowclick', this.onRowClickLGrid, this);
    this.grid.on('gridconfigloaded', function() {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    this.grid.getSelectionModel().on('selectionchange', function(selModel) {
        var selectedRecs = selModel.getSelections();
        if(selectedRecs.length > 0){
            this.tLedgerReportBtn.enable();
        }else{
            this.tLedgerReportBtn.disable();
        }
    }, this);
    
//    this.getMyConfig();

    this.gridStore.on('beforeload', function(s, o) {
        var accountIds = this.MultiSelectAccCombo.getValue(true);
        WtfGlobal.setAjaxTimeOut();
        if (!o.params)
            o.params = {};
        o.params.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        o.params.typeid = this.balPLTypeCombo.getValue();
        o.params.accountIds = accountIds.join(",");
        o.params.excludePreviousYear = this.excludeTypeCmb.getValue();
        o.params.periodView = true;
        o.params.mastertypeid = this.accountMasterTypeCombo.getValue();
        o.params.currencyIds = (this.MultiSelectCurrencyCombo) ? this.MultiSelectCurrencyCombo.getValue() : "";
        o.params.deleted = this.deleted;
        o.params.nondeleted = this.nondeleted;
        o.params.acctypes = this.typeEditor.getValue();
        o.params.accountTransactionType = this.accountTransactionType;
        o.params.showAccountsInGroup = this.accountViewTypeCombo.getValue();
    }, this);
    this.gridStore.on("load", function() {
        WtfGlobal.resetAjaxTimeOut();
        this.handleStoreOnLoad();
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
//        if(this.accountViewTypeCombo.getValue() == true){
            for (var i = 0; i < this.gridStore.data.length; i++) {
                this.grid.collapseRow(this.grid.getView().getRow(i));
            }
//        }
    }, this);
    this.gridStore.on('loadexception', function(){
        WtfGlobal.resetAjaxTimeOut();
    }, this);

     this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'stdate',
        format: WtfGlobal.getOnlyDateFormat(),
        maxValue : this.getDates(false),
        value: this.getDates(true)
    });
    
    this.startDate.on("change",function(){
        this.endDate.minValue = this.startDate.getValue();
    },this);
    
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"),
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'enddate',
        minValue : this.getDates(true),
        value: this.getDates(false)
    });
    this.endDate.on("change",function(){
        this.startDate.maxValue = this.endDate.getValue();
    },this);
    var btnArr = [];
    var btnArr1 = [];
    var bbarBtnArr = [];
    
    var accountViewStore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'isgrouped', 
            type: 'boolean'
        }, 'name'],
        data: [
        [true, "Account Group"],
        [false, "Without Account Group"]
        ]
    });

    this.accountViewTypeCombo = new Wtf.form.ComboBox({//Show accounts as grouped or normal.
        store: accountViewStore,
        name: 'accountviewtype',
        displayField: 'name',
        valueField: 'isgrouped',
        mode: 'local',
        value: !this.isDefaultGL,
        width: 100,
        listWidth: 200,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });
    btnArr.push("Group Accounts on",this.accountViewTypeCombo,"-");
    
    this.MSComboconfig = {
        hiddenName: 'accountmulselectcombo',
        store: this.accStore,
        valueField: 'accountid',
        hideLabel: false,
        hidden: false,
        displayField: 'accountname',
        emptyText: WtfGlobal.getLocaleText("acc.fxexposure.all"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus: true,
        triggerAction: 'all',
        scope: this
    };
    this.MultiSelectAccCombo = new Wtf.common.Select(Wtf.applyIf({
        id: 'mulaccountcombo' + this.id,
        multiSelect: true,
        fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectAccounts"),
        forceSelection: true,
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'],
        extraComparisionField: 'acccode', // type ahead search on acccode as well.
        listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 500 : 400,
        width: 125
    }, this.MSComboconfig));
    
    this.MultiSelectAccCombo.on("select",function(combo,rec,index){
        var parent = rec.json.parentid;
        var valueArr = this.MultiSelectAccCombo.valueArray;
        /*
         *Check if parent of account is selected then remove current selected accountid from selected value array.
         */
        if(this.indexOfParent(parent) > -1){
            valueArr.remove(rec.data.accountid);
        }
        /*
         *Check if child of account is selected then remove that selected accountid from selected value array.
         */
        if(rec.json.childArr){
            for(var i=0; i<rec.json.childArr.length;i++ ){
                var childIndex = this.indexOfChild(rec.json.childArr[i]);
                if(childIndex > -1){
                    valueArr.remove(valueArr[childIndex]);
                }
            }
        }
    },this);
    btnArr.push(WtfGlobal.getLocaleText("acc.1099.selAcc"));
    btnArr.push(" ");
    btnArr.push(this.MultiSelectAccCombo);
    this.balPLTypeStore = new Wtf.data.SimpleStore({
        fields: [{name: 'typeid', type: 'int'}, 'name'],
        data: [
            [0, "All"],
            [1, "Balance Sheet"],
            [2, "Profit & Loss"]
        ]
    });

    this.balPLTypeCombo = new Wtf.form.ComboBox({//All/Balance Sheet/Profit & Loss
        store: this.balPLTypeStore,
        name: 'typeid',
        displayField: 'name',
        valueField: 'typeid',
        mode: 'local',
        value: 0,
        width: 100,
        listWidth: 200,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });
    btnArr.push(WtfGlobal.getLocaleText("acc.product.gridType"))
    btnArr.push(this.balPLTypeCombo);
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{
                name: 'name'
            }, {
                name: 'value',
                type: 'boolean'
            }],
        data: [['All', false], ['Exclude Previous Year Balance', true]]
    });

    this.excludeTypeCmb = new Wtf.form.ComboBox({
        labelSeparator: '',
        labelWidth: 0,
        triggerAction: 'all',
        mode: 'local',
        valueField: 'value',
        displayField: 'name',
        store: this.typeStore,
        value: true,
        width: 200,
        disabledClass: "newtripcmbss",
        name: 'excludePreviousYear',
        hiddenName: 'excludePreviousYear'
    });
    btnArr.push(this.excludeTypeCmb);

    this.accountMasterTypeStore = new Wtf.data.SimpleStore({
        fields: [{name: 'mastertypeid', type: 'int'}, 'name'],
        data: [
            [0, "All"],
            [1, "Exclude Bank Accounts"],
            [2, "Exclude Cash Accounts"]
        ]
    });

    this.accountMasterTypeCombo = new Wtf.form.ComboBox({//All/Exclude Bank Accounts/Exclude Cash Accounts
        store: this.accountMasterTypeStore,
        name: 'accountmastertype',
        displayField: 'name',
        valueField: 'mastertypeid',
        mode: 'local',
        value: 0,
        width: 100,
        listWidth: 200,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });
    btnArr.push(WtfGlobal.getLocaleText("acc.field.AccountType"))
    btnArr.push(this.accountMasterTypeCombo);
    btnArr1.push(
            WtfGlobal.getLocaleText("acc.common.from"), this.startDate,
            WtfGlobal.getLocaleText("acc.common.to"), this.endDate
            );

    btnArr1.push('-', {
        xtype: 'button',
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls: 'accountingbase fetch',
        tooltip:WtfGlobal.getLocaleText("acc.cc.27"),
                scope: this,
        handler: this.fetchStatement
    });
    btnArr1.push('-', this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        hidden: this.isSummary,
        tooltip: WtfGlobal.getLocaleText("acc.coa.resetTT"), //'Allows you to add a new search account name by clearing existing search account names.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false,
        handler: this.resetFilters
    }));
    this.expButton = new Wtf.exportButton({
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),
        filename: this.isDefaultGL?"General Ledger Report" + "_v1":"Group Detail Report" + "_v1",
        menuItem: {
            csv: true,
            detailedCSV :true,
            summarySubMenu: true,
            rowPdf: false,
            xls: true,
            subMenu : true,
            detailedXls : true,
            isexportGLConfiguredData:true
        },
        params:{
//            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//            stdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
//            typeid : this.balPLTypeCombo.getValue(),
//            excludePreviousYear: this.excludeTypeCmb.getValue(),
//            accountIds : this.MultiSelectAccCombo.getValue(),
//            mastertypeid : this.accountMasterTypeCombo.getValue(),
            periodView : true,
            exportThreadFlagLedger : Wtf.account.companyAccountPref.downloadglprocessflag,
            isGroupDetailReport:true,
//            showAccountsInGroup : this.accountViewTypeCombo.getValue(),
            includeExcludeChildBalances : false
        },
        label: this.isDefaultGL?"General Ledger Report":"Group Detail Report",
        get: Wtf.autoNum.GroupDetailReport
    });

    this.printbtn = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        filename: this.isDefaultGL?"General Ledger Report":"Group Detail Report",
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //"Print Report details.",   
        menuItem: {
            print: true
        },
        params:{
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            typeid : this.balPLTypeCombo.getValue(),
            excludePreviousYear: this.excludeTypeCmb.getValue(),
            mastertypeid : this.accountMasterTypeCombo.getValue(),
            accountIds : this.MultiSelectAccCombo.getValue(),
            periodView : true  
        },
        label: this.isDefaultGL?"General Ledger Report":"Group Detail Report",
        get: Wtf.autoNum.GroupDetailReport
    });
    bbarBtnArr.push(this.expButton,"-"," ");

    bbarBtnArr.push(this.printbtn," ");
    
    this.subLedgerBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.subLedger"),  //'Sub Ledger',
        tooltip:WtfGlobal.getLocaleText("acc.field.subLedger"),
        scope:this,
        iconCls:'pwnd exportpdf',
        handler:this.subLedgerReport
    });
    
    bbarBtnArr.push("-",this.subLedgerBtn," ");
    
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function() {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });

    btnArr1.push('-', this.expandCollpseButton);
    
    this.tLedgerReportBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.LedgerTReport"),
        tooltip: "Please select account to show ledger report.",
        iconCls: 'accountingbase fetch',
        scope: this,
        disabled : true,
        handler: function() {
            var rec = this.grid.getSelections();
            var acc = [];
            for (var record = 0; record < this.grid.getSelections().length; record++) {
                var accountid = rec[record].data.accountid.replace("Other","").trim();
                if(rec[record].data.accountflag && acc.indexOf(accountid) == -1){
                    
                    acc.push(accountid);
                }
            }
            if(rec.length > 0 && acc.length == 0){
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.SelectAccountToShowLedger"));
            }else{
                callLedger(acc.join(","));
            }
        }
    }); 
    
    bbarBtnArr.push('-', this.tLedgerReportBtn);
    
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    this.configureExportCustomData = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.configure.button.title"),
        tooltip: WtfGlobal.getLocaleText("acc.configure.button.title"),
//        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.exportCustomDataWindowHandler();
        }
    });
    btnArr1.push('-', this.AdvanceSearchBtn,this.configureExportCustomData);
    
    this.currencyRec = Wtf.data.Record.create ([                                
    {
        name:'currencyname',  
        mapping:'name'
    },

    {
        name:'currencyid',    
        mapping:'currencyid'
    },

    {
        name:'accountid'
    },

    {
        name:'acccode'
    },

    {
        name:'groupname'
    }
    ]);
                
    this.currencyStore = new Wtf.data.Store({
        url : "ACCCurrency/getCurrency.do",
        baseParams:{
            mode:2,
            nondeleted:true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.currencyRec)
    });
                
    this.currencyStore.load();                
                
    this.MSCurrencyComboconfig = {
        hiddenName:'currencymulselectcombo',         
        store: this.currencyStore,
        valueField:'currencyid',
        hideLabel:false,
        hidden : false,
        displayField:'currencyname',
        emptyText: WtfGlobal.getLocaleText("acc.fxexposure.all"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
                    
    this.MultiSelectCurrencyCombo = new Wtf.common.Select(Wtf.applyIf({      //Currency
        id:'mulcurrencycombo'+this.id,
        multiSelect:true,
        fieldLabel: WtfGlobal.getLocaleText("acc.fxexposure.currency") ,
        forceSelection:true,  
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
        width:100
    },this.MSCurrencyComboconfig));    

    btnArr.push("-",WtfGlobal.getLocaleText("acc.customerList.gridCurrency"),' ', this.MultiSelectCurrencyCombo," ");
               
    this.delTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[
            [0, WtfGlobal.getLocaleText("acc.rem.105")],
            [1, WtfGlobal.getLocaleText("acc.rem.106")],
            [2, WtfGlobal.getLocaleText("acc.rem.107")],
            [3, WtfGlobal.getLocaleText("acc.rem.216")],
            [Wtf.Acc_Make_Payment_ModuleId, WtfGlobal.getLocaleText("acc.accPref.autoMP")+" "+WtfGlobal.getLocaleText("acc.repeated.repeatedrecords")],
            [Wtf.Acc_Receive_Payment_ModuleId, WtfGlobal.getLocaleText("acc.accPref.autoRP")+" "+WtfGlobal.getLocaleText("acc.repeated.repeatedrecords")]
        ]
    });
    
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.delTypeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.id,
        valueField:'typeid',
        mode: 'local',
        value:0,
        width:110,        
        listWidth:210,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    this.typeEditor.on('select',this.loadTypeStore,this);
    
    btnArr1.push('->',WtfGlobal.getLocaleText("acc.common.view"),' ',this.typeEditor);
    
    this.createAdvanceSearchGrid();
    
    
    var FirstTopToolaBr = new Wtf.Toolbar(btnArr);
    this.wrapperPanel = new Wtf.Panel({
        border: false,
        layout: "border",
        scope: this,
        items: [this.objsearchComponent,
            this.westPanel = new Wtf.Panel({
                width: '100%',
                region: 'center',
                layout: 'fit',
                border: false,
                items: this.grid
            })],
        tbar: btnArr1,
        bbar : bbarBtnArr
    });
    
    Wtf.apply(this, {
        defaults: {border: false, bodyStyle: "background-color:white;"},
        saperate: true,
        statementType: "Trading",
        tbar:FirstTopToolaBr,
        items: this.wrapperPanel
    }, config);
    Wtf.account.GroupDetailReport.superclass.constructor.call(this, config);
    this.addEvents({
        'account': true
    });
     this.on('activate', function() {
        if (this.MultiSelectAccCombo != undefined) {
            this.doLayout();
            this.MultiSelectAccCombo.syncSize();
            this.MultiSelectAccCombo.setWidth(125);
        }
        if (this.MultiSelectCurrencyCombo != undefined) {
            this.doLayout();
            this.MultiSelectCurrencyCombo.syncSize();
            this.MultiSelectCurrencyCombo.setWidth(100);
        }
    }, this);
    WtfGlobal.getReportMenu(FirstTopToolaBr, Wtf.Account_Statement_ModuleId, WtfGlobal.getModuleName(Wtf.Account_Statement_ModuleId));
}

Wtf.extend(Wtf.account.GroupDetailReport, Wtf.Panel, {
    indexOfParent : function(parentId){
        var valueArr = this.MultiSelectAccCombo.valueArray;
        var index = valueArr.indexOf(parentId);
        if(parentId == undefined){
            return -1;
        }else if(index > -1){
            return index;
        }else{
            var parentRec = WtfGlobal.searchRecord(this.MultiSelectAccCombo.store,parentId,"accountid");
            if(parentRec == undefined){
                parentRec = {
                    json:{}
                }
            }
            return this.indexOfParent(parentRec.json.parentid);
        }
    },
    indexOfChild : function(childId){
        var valueArr = this.MultiSelectAccCombo.valueArray;
        var index = valueArr.indexOf(childId);
        if(childId == undefined){
            return -1;
        }else if(index > -1){
            return index;
        }else{
            var parentRec = WtfGlobal.searchRecord(this.MultiSelectAccCombo.store,childId,"accountid");
            if(parentRec == undefined){
                parentRec = {
                    json:{
                        childArr:[]
                    }
                }
            }
            for(var i=0; i<parentRec.json.childArr.length;i++ ){
                return this.indexOfChild(parentRec.json.childArr[i]);
            }
        }
    },
    createAdvanceSearchGrid : function(){
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: 102 ,
            reportid:  Wtf.autoNum.GroupDetailReport,
            advSearch: false
        });
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
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
        this.gridStore.baseParams.searchJson= this.searchJson;
        this.gridStore.baseParams.filterConjuctionCriteria = filterConjuctionCriteria;
        this.gridStore.load();
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.gridStore.baseParams.searchJson= this.searchJson;
        this.gridStore.baseParams.filterConjuctionCriteria = this.filterConjuctionCriteria;
        this.gridStore.load();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    exportCustomDataWindowHandler: function () {
        this.ExportCustomDataWindow = new Wtf.account.ExportCustomdataInFinancialReport({
            title: WtfGlobal.getLocaleText("acc.configure.window.title"),
            resizable: false,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            modal: true,
            autoScroll: true,
            width: 800,
            height: 550
//            layout:'border'
        });
        this.ExportCustomDataWindow.show();
    },
    handleStoreOnLoad: function() {
        if (!this.isConfigLoaded) {
            var columns = [];
            columns.push(this.expander);  // Added expander button
            var columnConfig = this.gridStore.reader.jsonData.columns;
            var columnWidth = this.grid.getInnerWidth() / columnConfig.length;
            var column;
            for (var i = 0; i < columnConfig.length; i++) {
                column = columnConfig[i];
                if (column.renderer) {
                    column.renderer = eval(column.renderer);
                }
                column.width = columnWidth;
                if (column.dataIndex == "accountcode" || column.dataIndex == "aliascode" || column.dataIndex == "accountgroupname" || column.dataIndex == "currencyCode") {
                    column.renderer = WtfGlobal.deletedRenderer;
                } else if (column.dataIndex == "accountname") {
                    column.renderer = this.formatAccountName;
                    column.width = (columnWidth * 2);
                    column.summaryRenderer = function() {
                        return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
                    }.createDelegate(this);

                } else if (column.dataIndex == "openingbalanceinaccountcurrency" && column.header == WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType")) {
                    column.renderer = this.balTypeRenderer;
                } else if (column.dataIndex == "openingamount" || column.dataIndex == "periodamount" || column.dataIndex == "endingamount") {
                    column.renderer = this.formatMoney;
                    column.pdfrenderer = "currency";
                    if (column.dataIndex == "endingamount") {
                        column.summaryRenderer = this.showLastRec.createDelegate(this, [0]);
                    }
                } else if (column.dataIndex == "openingbalanceinaccountcurrency") {
                    column.renderer = this.openingBalInAccountCurrency;
                    column.pdfrenderer = "rowcurrency";
                } else if (column.dataIndex == "creationDate") {
                    column.renderer = function(v, m, rec) {
                        if (!v) {
                            return v;
                        }
                        if (v != "" && rec != undefined && rec.data != undefined && rec.data.deleted != undefined && rec.data.deleted) {
                            v = '<del>' + v.format(WtfGlobal.getOnlyDateFormat()) + '</del>';
                        } else if (v != "") {
                            v = v.format(WtfGlobal.getOnlyDateFormat());
                        }
                        v = '<div class="datecls">' + v + '</div>';
                        return v;
                    }
                }
                columns.push(column);
            }
            this.grid.getColumnModel().setConfig(columns);
            /*
             * Get Column index 
             */
            var hirarchyColIndex = this.grid.getColumnModel().findColumnIndex("accountname");
            if (hirarchyColIndex > -1) {
                this.grid.getColumnModel().setRenderer(hirarchyColIndex, this.grid.formatAccountName.createDelegate(this.grid));
            }
            if (this.gridStore.getCount() < 1) {
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            }
            this.grid.getView().refresh();
            this.getMyConfig();
            this.isConfigLoaded = true;
        }
        this.grid.on('gridconfigloaded', function() {
            this.expandCollapseGrid(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }, this);
    },
    openingBalInAccountCurrency: function(val, m, rec) {
        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
        var v = parseFloat(val);
        if (isNaN(v))
            return val;
        if (rec.data.deleted)
            v = '<del>' + WtfGlobal.conventInDecimal(v, symbol) + '</del>';
        else
            v = WtfGlobal.conventInDecimal(v, symbol);
        v = '<div class="currency">' + v + '</div>';
        return v;
    },
    handleOnRendererEvent:function(){
        this.grid.getView().getRowClass = this.getRowClass.createDelegate(this, [this.grid], 1);
        this.grid.getView().applyEmptyText();
        WtfGlobal.autoApplyHeaderQtip(this.grid);
    },
    loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        this.accountTransactionType=undefined;
        var index=rec.data.typeid;
        if(index==1){
            this.deleted=true;
        }else if(index==2){
            this.nondeleted=true;
        }else if(index==Wtf.Acc_Make_Payment_ModuleId || index==Wtf.Acc_Receive_Payment_ModuleId){
            this.accountTransactionType=index;
        }
        this.gridStore.load();
    },
    onRowClickLGrid: function(g, i, e) {
        e.stopEvent();
        var el = e.getTarget("a");
        if (el == null)
            return;
        var accid = this.grid.getStore().getAt(i).data['accountid'];
        this.fireEvent('account', accid, this.startDate.getValue(), this.endDate.getValue());
    },
    onRender: function(config) {
        Wtf.account.GroupDetailReport.superclass.onRender.call(this, config);
    },
    /**
     * show 'Group Details Report' after click on account.
     */
    showGroupDetailsReport:function(config){
        var accid  = config.accountID;
        var stDate = config.stDate;
        var enDate = config.enDate;
        var currencyFilterConfig = config.currencyFilterConfig;
        var excludePreviousYearConfig = config.excludePreviousYearConfig;
        this.accStore.clearFilter();
        var i = this.accStore.find("accountid", accid);
        if (i >= 0) {
            this.MultiSelectAccCombo.setValue(accid);
        }
        this.startDate.maxValue = undefined;
        this.endDate.minValue = undefined;
        if (enDate != null && enDate != undefined && enDate != "") {
            this.endDate.setValue(enDate);
        }
         if (stDate != null && stDate != undefined && stDate != "") {
            this.startDate.setValue(stDate);
        }      
        
        this.startDate.maxValue = this.endDate.getValue();
        this.endDate.minValue = this.startDate.getValue();
            
        if (excludePreviousYearConfig != null && excludePreviousYearConfig != undefined && excludePreviousYearConfig != "") {
            this.excludeTypeCmb.setValue(excludePreviousYearConfig);
        }
        if (currencyFilterConfig != null && currencyFilterConfig != undefined && currencyFilterConfig != "") {
            this.MultiSelectCurrencyCombo.setValue(currencyFilterConfig);
        }
        if (accid != undefined) {
            this.fetchStatement();
        }
    },
    resetFilters: function() {
        if (this.MultiSelectAccCombo) {
            this.MultiSelectAccCombo.reset();
        }
        if (this.startDate) {
            this.startDate.reset();
        }
        if (this.endDate) {
            this.endDate.reset();
        }
        if (this.balPLTypeCombo) {
            this.balPLTypeCombo.reset();
        }
        if (this.MultiSelectAccCombo) {
            this.MultiSelectAccCombo.reset();
        }
        if (this.excludeTypeCmb) {
            this.excludeTypeCmb.reset();
        }
        if (this.accountMasterTypeCombo) {
            this.accountMasterTypeCombo.reset();
        }
        if (this.MultiSelectCurrencyCombo) {
            this.MultiSelectCurrencyCombo.reset();
        }
        if (this.typeEditor) {
            this.typeEditor.reset();
        }
        this.deleted=false;
        this.nondeleted=false;
        this.accountTransactionType=undefined;
        this.gridStore.load();
    },
    expandCollapseGrid: function(btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            for (var i = 0; i < this.grid.getStore().data.length; i++) {
                this.grid.collapseRow(this.grid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            for (var i = 0; i < this.grid.getStore().data.length; i++) {
                   this.grid.expandRow(this.grid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    getRowClass: function(record, grid) {
        var colorCss = "";
        switch (record.data["fmt"]) {
            case "T":
                colorCss = " grey-background";
                break;
            case "B":
                colorCss = " red-background";
                break;
            case "H":
                colorCss = " header-background";
                break;
            case "A":
                colorCss = " darkyellow-background";
                break;
        }
        return grid.getRowClass() + colorCss;
    },
    formatAccountName: function(val, m, rec, i, j, s) {
        var fmtVal = val;
        if (rec.data['fmt']) {
            fmtVal = '<font size=2px ><b>' + fmtVal + '</b></font>';
        }
        else if (rec.data["level"] == 0 && rec.data["accountname"] != "") {
            fmtVal = '<span style="font-weight:bold">' + fmtVal + '</span>';
        }

//        if (rec.data.accountflag) {
//            fmtVal = WtfGlobal.accountLinkRenderer(fmtVal);
//        }
        return WtfGlobal.deletedRenderer(fmtVal,undefined,rec);
    },
    formatMoney: function(val, m, rec, i, j, s) {
        var fmtVal = WtfGlobal.currencyDeletedRenderer(val,undefined,rec);
        if (rec.data['fmt']) {
            fmtVal = '<font size=2px ><b>' + fmtVal + '</b></font>';
        }
        else if (rec.data["level"] == 0 && rec.data["accountname"] != "")
            fmtVal = '<span style="font-weight:bold">' + fmtVal + '</span>';
        return fmtVal;
    },
    fetchStatement: function(isCompare) {
        this.grid.getStore().removeAll();
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
//        if(this.accountViewTypeCombo.getValue()){
//            this.expandCollpseButton.show();
//        }else{
//            this.expandCollpseButton.hide();
//        }
        this.gridStore.load();

    },
    getDates: function(start) {
        var d = Wtf.serverDate;
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom)
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd)
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        if (start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    showLastRec: function(pos) {
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },
    createExpanderStore : function(currencyIds){
        this.expandRec = Wtf.data.Record.create([
            {name: 'd_date',type:'date'},
            {name: 'd_accountname'},
            {name: 'refno'},
            {name: 'exchangeratefortransaction'},
            {name: 'd_acccode'},
            {name: 'd_entryno' ,mapping :'entryno'},
	    {name: 'd_externalcurrencyrate'},
            {name: 'd_journalentryid'},
            {name: 'd_amount'},
            {name: 'd_transactionID'},
            {name: 'd_transactionDetails'},
            {name: 'd_transactionDetailsForExpander'},
            {name: 'c_date',type:'date'},
            {name: 'c_accountname'},
            {name: 'c_acccode'},
            {name: 'c_entryno'},
	    {name: 'c_externalcurrencyrate'},
            {name: 'c_journalentryid'},
            {name: 'c_amount'},
            {name: 'c_transactionID'},
            {name: 'c_transactionDetails'},
            {name: 'c_transactionDetailsForExpander'},
            {name: 'accountid'},
            {name: 'accountname'},
            {name: 'accCode'},
            {name: 'accCodeName'},
            {name: 'billid'},
            {name: 'type',mapping: 'doctype'},
            {name: 'noteid'},
            {name: 'c_amountAccountCurrency'},
            {name: 'd_amountAccountCurrency'},
            {name: 'currencysymbol'},
            {name: 'transactionCurrency'},
            {name: 'transactionAmount'},
            {name: 'transactionSymbol'},
            {name: 'transactionDateString'},
            {name: 'multiEntityData'},
            {name: 'gstCode'},
            {name: 'isconsignment'},
            {name: 'isLeaseFixedAsset'}
            
            
        ]);
        this.expandStore = new Wtf.data.Store({
            url:"ACCCombineReports/getLedgerDetails.do",
            baseParams:{
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                generalLedgerFlag:true,
                isFromExpander:true,
                isOpeningBal:"",
                includeExcludeChildBalances : false
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty : 'count'
            },this.expandRec)
        });
        this.expandStore.on('beforeload', function(){
            WtfGlobal.setAjaxTimeOutFor30Minutes();
        }, this);
        this.expandStore.on('loadexception', function(){
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        this.expandStore.on('load', function(){
            WtfGlobal.resetAjaxTimeOut();
            this.fillExpanderBodyNew();
        }, this);
    },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        if(this.startDate != undefined || this.startDate != null){
            this.sDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        }else{
            this.sDate=WtfGlobal.convertToGenericStartDate(this.getDates(true));
        }
        if(this.endDate != undefined || this.endDate != null){
            this.eDate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());     //General Ledger Report //ERP-8205,8316,8328
        }else{
            this.eDate=WtfGlobal.convertToGenericEndDate(this.getDates(false));
        }
        this.expandStore.load({
            params:{
                    accountid:record.data.accountid.replace("Other ",""),
                    accountopeningamount:record.data.openingamount,//Kapil Sir Changes
                    stdate:this.sDate,
                    startdate:this.sDate,
                    enddate:this.eDate,
                    searchJson: this.searchJson,
                    filterConjuctionCriteria: this.filterConjuctionCrit,
                    excludePreviousYear: this.excludeTypeCmb.getValue(),
                    currencyIds : this.MultiSelectCurrencyCombo ? this.MultiSelectCurrencyCombo.getValue() : "",
                    accountTransactionType : this.accountTransactionType
                }
            });
    },
    fillExpanderBody: function(){
        var disHtml = "";
        var arr=[];
        var startDate=this.startDate.getValue();
        var endDate=this.endDate.getValue();
        var openingBalanceOfAccount = 0, totalDebitAmount=0, totalCreditAmount = 0;
        arr = [WtfGlobal.getLocaleText("acc.inventoryList.date"), WtfGlobal.getLocaleText("acc.coa.gridAccountName"), WtfGlobal.getLocaleText("acc.field.JournalFolio(J/F)"),WtfGlobal.getLocaleText("acc.product.description"),'Exchange Rate ('+WtfGlobal.getCurrencySymbol()+')',WtfGlobal.getLocaleText("acc.je.debitAmt"), WtfGlobal.getLocaleText("acc.je.creditAmt")];
        var gridHeaderText = WtfGlobal.getLocaleText("acc.ccReport.tab3");
        var multiEntityHeader = this.expandStore.reader.jsonData.multiEntityHeader || "Entity";
        var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
        
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventoryList.date")+"'style='width: 6% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.inventoryList.date"),15)+ "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.coa.gridDoubleEntryMovement")+"'style='width: 13% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.coa.gridDoubleEntryMovement"),25) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.JournalFolio(J/F)")+"'style='width: 7% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.JournalFolio(J/F)"),20) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DocumentNO")+"'style='width: 7% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.DocumentNO"),20) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.description")+"'style='width: 9% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.product.description"),40) + "</span>";
        header += "<span class='headerRow' wtf:qtip= 'Exchange Rate ("+WtfGlobal.getCurrencySymbol()+")"+"'style='width: 9% ! important;'>" + Wtf.util.Format.ellipsis('Exchange Rate ('+WtfGlobal.getCurrencySymbol()+')',22) + "&nbsp;</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.je.debitAmt")+"'style='width: 9% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.je.debitAmt"),16) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency")+"'style='width: 9% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency"),16) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.je.creditAmt")+"'style='width: 9% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.je.creditAmt"),16) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency")+"'style='width: 9% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency"),16) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.taxReport.taxCode")+"'style='width: 5% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.taxReport.taxCode"),16) + "</span>";
        if(Wtf.account.companyAccountPref.isMultiEntity){
            header += "<span class='headerRow' wtf:qtip='"+multiEntityHeader+"'style='width: 7% ! important;'>" + Wtf.util.Format.ellipsis(multiEntityHeader,16) + "</span>";
        }
        
        header += "<span style='margin:21px 0 10px;' class='gridLine'></span>";
        var continuedStoreCnt=0;
        for(i=0;i<this.expandStore.getCount();i++){
            header += "<span class='gridExpanderRow'>";
            var rec=this.expandStore.getAt(i);
            if(rec.data['d_accountname'] == 'Opening Balance' || rec.data['d_accountname'] == 'Balance c/f' || rec.data['d_accountname'] == 'Balance b/d' || rec.data['c_accountname'] == 'Opening Balance' || rec.data['c_accountname'] == 'Balance c/f' || rec.data['c_accountname'] == 'Balance b/d'){
                continuedStoreCnt++;
                continue;
            }  
            //Column : Date
            if(rec.data['d_date'] != ''){
                header += "<span class='gridRow' wtf:qtip='"+rec.data['d_date'].format(WtfGlobal.getOnlyDateFormat())+"'style='width: 6% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['d_date'].format(WtfGlobal.getOnlyDateFormat()),15)+"&nbsp;</span>";
            }else{
                header += "<span class='gridRow' wtf:qtip='"+rec.data['c_date'].format(WtfGlobal.getOnlyDateFormat())+"'style='width: 6% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['c_date'].format(WtfGlobal.getOnlyDateFormat()),15)+"&nbsp;</span>";
            }
                
            //Column : Account Name
            if(rec.data['d_accountname'] != ''){
                header += "<span class='gridRow' wtf:qtip='"+rec.data['d_accountname']+"' style='width: 13% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['d_accountname'],20)+"&nbsp;</span>";
            }else{
                header += "<span class='gridRow' wtf:qtip='"+rec.data['c_accountname']+"' style='width: 13% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['c_accountname'],20)+"&nbsp;</span>";
                 
            }
                
            //Column : Journal Folio
            if(rec.data['d_entryno'] != ''){
                var jid=rec.data['d_journalentryid'];
                header += "<a href='#' onClick=Wtf.onCellClick('"+jid+"',"+Wtf.encode(startDate)+","+Wtf.encode(endDate)+") >"+"<span class='gridRow' wtf:qtip='"+rec.data['d_entryno']+"'style='width: 7% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['d_entryno'],18)+"&nbsp;</span></a>";
            }else{
                var jid=rec.data['c_journalentryid'];
                header += "<a href='#' onClick=Wtf.onCellClick('"+jid+"',"+Wtf.encode(startDate)+","+Wtf.encode(endDate)+") >"+"<span class='gridRow' wtf:qtip='"+rec.data['c_entryno']+"'style='width: 7% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['c_entryno'],18)+"&nbsp;</span></a>";
            }
             
            //Column : Document No.
            if (rec.data['c_transactionID'] != '') {
                var c_documentNo = rec.data['c_transactionID'];
                
                var isLeaseFixedAsset = (rec.data["isLeaseFixedAsset"] != "" && rec.data["isLeaseFixedAsset"] != undefined) ? rec.data["isLeaseFixedAsset"] : false;
                var isconsignment = (rec.data["isconsignment"] != "" && rec.data["isconsignment"] != undefined) ? rec.data["isconsignment"] : false;
                var transactionType = rec.data['type'];
                var billid = rec.data['billid'];
                var noteid = rec.data['noteid'];
                var jid = rec.data['c_journalentryid'];
                if (transactionType == '') {
                    header += "<a href='#' onClick=Wtf.onCellClick('" + jid + "'," + Wtf.encode(startDate) + "," + Wtf.encode(endDate) + ") >" + "<span class='gridRow' wtf:qtip='" + rec.data['c_transactionID'] + "'style='width: 7% ! important;'>" + Wtf.util.Format.ellipsis(rec.data['c_transactionID'], 18) + "&nbsp;</span></a>";
                } else {

                    header += "<a href='#' onClick='Wtf.onCellClickofDocumentNo(\"" + c_documentNo + "\",\"" + billid + "\",\"" + noteid + "\",\"" + transactionType + "\",\"" + isconsignment + "\",\"" + isLeaseFixedAsset + "\");' >" + "<span class='gridRow' wtf:qtip='" + rec.data['c_transactionID'] + "'style='width: 7% ! important;'>" + Wtf.util.Format.ellipsis(rec.data['c_transactionID'], 18) + "&nbsp;</span></a>";
                }
               
            } else {
                var d_documentNo = rec.data['d_transactionID'];
                var isLeaseFixedAsset = (rec.data["isLeaseFixedAsset"] != "" && rec.data["isLeaseFixedAsset"] != undefined) ? rec.data["isLeaseFixedAsset"] : false;
                var isconsignment = (rec.data["isconsignment"] != "" && rec.data["isconsignment"] != undefined) ? rec.data["isconsignment"] : false;
                var transactionType = rec.data['type'];
                var billid = rec.data['billid'];
                var noteid = rec.data['noteid'];
                var jid = rec.data['d_journalentryid'];
                if (transactionType == '') {
                    header += "<a href='#' onClick=Wtf.onCellClick('" + jid + "'," + Wtf.encode(startDate) + "," + Wtf.encode(endDate) + ") >" + "<span class='gridRow' wtf:qtip='" + rec.data['d_transactionID'] + "'style='width: 7% ! important;'>" + Wtf.util.Format.ellipsis(rec.data['d_transactionID'], 18) + "&nbsp;</span></a>";
                } else {

                    header += "<a href='#' onClick='Wtf.onCellClickofDocumentNo(\"" + d_documentNo + "\",\"" + billid + "\",\"" + noteid + "\",\"" + transactionType + "\",\"" + isconsignment + "\",\"" + isLeaseFixedAsset + "\");' >" + "<span class='gridRow' wtf:qtip='" + rec.data['d_transactionID'] + "'style='width: 7% ! important;'>" + Wtf.util.Format.ellipsis(rec.data['d_transactionID'], 18) + "&nbsp;</span></a>";
            }
            }

            
            //Column : Description
            if(rec.data['c_transactionDetails'] !=''){
                var value = rec.data['c_transactionDetailsForExpander'];
                value = value.replace(/\'/g, "&#39;");
                header += "<span class='gridRow' wtf:qtip='"+value+"' style='width: 9% ! important;'>"+Wtf.util.Format.ellipsis(value,30)+"&nbsp;</span>";
            }else{
                var value1=rec.data['d_transactionDetailsForExpander'];
                value1 = value1.replace(/\'/g, "&#39;");
                header += "<span class='gridRow' wtf:qtip='"+value1+"' style='width: 9% ! important;'>"+Wtf.util.Format.ellipsis(value1,30)+"&nbsp;</span>";
            }
            //Column : Description
            if(rec.data['transactionCurrency']!=undefined&&rec.data['transactionCurrency'] !=''){
                if(rec.data['transactionCurrency']==WtfGlobal.getCurrencyID()){
                    header += "<span class='gridRow' wtf:qtip='1' style='width: 9% ! important;text-align: center;'>1 &nbsp;</span>";
                }else{
                    var value ;
                    if(rec.data['d_amount'] != '' && (rec.data['d_externalcurrencyrate']!='' && rec.data['d_externalcurrencyrate']>0)){
                        var value = 1 / ((rec.data['d_externalcurrencyrate']) - 0);
                        value = (Math.round(value * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
                    }else if(rec.data['d_amount'] != ''){   //if externalcurrencyrate == 0
                        value = getRoundofValueWithValues((rec.data['d_amount']/rec.data['transactionAmount']), Wtf.CURRENCY_RATE_DIGIT_AFTER_DECIMAL);
                    }else if(rec.data['c_amount'] != '' && (rec.data['c_externalcurrencyrate']!='' && rec.data['c_externalcurrencyrate']>0)){
                        var value = 1 / ((rec.data['c_externalcurrencyrate']) - 0);
                        value = (Math.round(value * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
                    } else if(rec.data['c_amount'] != ''){  //if externalcurrencyrate == 0
                        value = getRoundofValueWithValues((rec.data['c_amount']/rec.data['transactionAmount']), Wtf.CURRENCY_RATE_DIGIT_AFTER_DECIMAL);
            }
                    header += "<span class='gridRow' wtf:qtip='"+rec.data['transactionSymbol']+'('+rec.data['transactionDateString']+') '+value+"'style='width: 9% ! important;text-align: center;'>"+Wtf.util.Format.ellipsis(rec.data['transactionSymbol']+'('+rec.data['transactionDateString']+') '+value,30)+"&nbsp;</span>";
                }
            }
            else{
                header += "<span class='gridRow' wtf:qtip='' style='width: 9% ! important;text-align: center;'>&nbsp;</span>";
            }
              
            //Column : Debit Amount
            if(rec.data['d_amount'] != ''){
                var dAmtWitSymbol=WtfGlobal.conventInDecimal(rec.data['transactionAmount'],rec.data['transactionSymbol']);                          
                header += "<span class=\"gridRow\" wtf:qtip=\""+dAmtWitSymbol+"\"style=\"width: 9% ! important;\">"+dAmtWitSymbol+"&nbsp;</span>";
               
            }else{
                header += "<span class='gridRow'  style='width: 9% ! important;'>&nbsp;</span>";
            }
            //Debit Amount in BASE
            if(rec.data['d_amount'] != ''){
                var dAmtWitSymbol=WtfGlobal.conventInDecimal(rec.data['d_amount'],WtfGlobal.getCurrencySymbol());                          
                header += "<span class=\"gridRow\" wtf:qtip=\""+dAmtWitSymbol+"\"style=\"width: 9% ! important;\">"+dAmtWitSymbol+"&nbsp;</span>";
                totalDebitAmount+=rec.data['d_amount'];
            }else{
                header += "<span class='gridRow'  wtf:qtip='"+rec.data['d_amount']+"'style='width: 9% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['d_amount'],20)+"&nbsp;</span>";
            }
            //Column : Credit Amount
            
            if(rec.data['c_amount'] != ''){
                var cAmtWitSymbol=WtfGlobal.conventInDecimal(rec.data['transactionAmount'],rec.data['transactionSymbol']);                          
                header += "<span class=\"gridRow\" wtf:qtip=\""+cAmtWitSymbol+"\" style=\"width: 9% ! important;\">"+cAmtWitSymbol+"&nbsp;</span>";
            }else{
                header += "<span class='gridRow' style='width: 9% ! important;'>&nbsp;</span>";
            }
            //Credit amount in BASE
            if(rec.data['c_amount'] != ''){
                var cAmtWitSymbol=WtfGlobal.conventInDecimal(rec.data['c_amount'],WtfGlobal.getCurrencySymbol());                
                header += "<span class=\"gridRow\" wtf:qtip=\""+cAmtWitSymbol+"\" style=\"width: 9% ! important;\">"+cAmtWitSymbol+"&nbsp;</span>";
                totalCreditAmount+=rec.data['c_amount'];
            }else{
                header += "<span class='gridRow' wtf:qtip='"+rec.data['c_amount']+"'style='width: 9% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['c_amount'],20)+"&nbsp;</span>";
            }
            //GST Code
            if (rec.data['gstCode'] != '' && rec.data['gstCode'] != undefined) {
                header += "<span class=\"gridRow\" wtf:qtip=\"" + rec.data['gstCode'] + "\"style=\"width: 5% ! important;\">" + Wtf.util.Format.ellipsis(rec.data['gstCode'],20)+ "&nbsp;</span>";
            } else {
                header += "<span class='gridRow'  style='width: 5% ! important;'>&nbsp;</span>";
            }
            if(Wtf.account.companyAccountPref.isMultiEntity){
                if (rec.data['multiEntityData'] != '' && rec.data['multiEntityData'] != undefined) {
                    header += "<span class=\"gridRow\" wtf:qtip=\"" + rec.data['multiEntityData'] + "\"style=\"width: 7% ! important;\">" + Wtf.util.Format.ellipsis(rec.data['multiEntityData'],20) + "&nbsp;</span>";
                } else {
                    header += "<span class='gridRow'  style='width: 7% ! important;'>&nbsp;</span>";
                }
            }
            header +="</span></br>";
        }
        header += "<span class='gridLineBottom'></span>";
        
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.total")+"'style='width: 10% ! important;'>" + WtfGlobal.getLocaleText("acc.common.total") + "</span>";
        header += "<span class='headerRow' style='width: 10% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 10% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 10% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 10% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 9% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.addCurrencySymbolOnly(totalDebitAmount,WtfGlobal.getCurrencySymbol(),[true])+" 'style='width: 9% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(totalDebitAmount,WtfGlobal.getCurrencySymbol(),[true]) + "</span>";
        header += "<span class='headerRow' style='width: 9% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.addCurrencySymbolOnly(totalCreditAmount,WtfGlobal.getCurrencySymbol(),[true])+" 'style='width: 9% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(totalCreditAmount,WtfGlobal.getCurrencySymbol(),[true]) + "</span>";
        header += "<span class='headerRow' style='width: 5% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 9% ! important;'>&nbsp;</span>";
        
        if(this.expandStore.getCount()==0 || this.expandStore.getCount()==continuedStoreCnt){
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
            header += "<span class='headerRow'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</span>"
        }
        disHtml += "<div style='min-width: 970px !important;margin-bottom: 30px;width: 98%; margin-left: 2%;'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    },
    fillExpanderBodyNew: function() {
        var arr = [];
        var startDate = this.startDate.getValue();
        var endDate = this.endDate.getValue();
        var title = "", dataIndex = "", align = "", records = [];
        var openingBalanceOfAccount = 0, totalDebitAmount = 0, totalCreditAmount = 0;
        if (this.expandStore.reader.jsonData != undefined) {
            title = this.expandStore.reader.jsonData.title;
            dataIndex = this.expandStore.reader.jsonData.header;
            align = this.expandStore.reader.jsonData.align;
            records = this.expandStore.reader.jsonData.data;
            totalDebitAmount = this.expandStore.reader.jsonData["d_sumamout"] || 0;
            totalCreditAmount = this.expandStore.reader.jsonData["c_sumamout"]|| 0;
        }

        var gridHeaderText = WtfGlobal.getLocaleText("acc.ccReport.tab3");

        /*If data present create html for expander else show no data to display.*/
        if (records.length > 0) {
            if (title != undefined && typeof title == "string") {
                title = title.split(",");
            }
            if (dataIndex != undefined && typeof dataIndex == "string") {
                dataIndex = dataIndex.split(",");
            }
            if (align != undefined && typeof align == "string") {
                align = align.split(",");
            }

            var arrayLength = title.length + 1;
            var width = (arrayLength * 140) + 250;
            var widthInPercent = (100 / arrayLength);

            /*Header Section*/

            var header = "<span class='gridHeader'>" + gridHeaderText + "</span>";   //Product List
            header += "<div style='display:table !important;width:" + width + "px'>";

            for (var i = 0; i < title.length; i++) {
                header += "<span class='headerRow' style='width:" + widthInPercent + "%;text-align:" + align[i] + ";' wtf:qtip='" + title[i] + "'>" + Wtf.util.Format.ellipsis(title[i], 20) + "&nbsp;</span>";
                arr.push(title[i]);
            }
            header += "</div>";

            //Values Section
            header += "<div style='width:" + width + "px;'><span class='gridLine'></span></div>";
            var recordData;
            for (i = 0; i < records.length; i++) {
                header += " <div style='width:" + width + "px;display:table !important;height: 22px;'>";
                recordData = records[i];
                for (var j = 0; j < dataIndex.length; j++) {
                    var dataIndx = dataIndex[j] || "";
                    var recordvalue = recordData[dataIndx] || "";
                    var alignTo = align[j];
                    if (dataIndx == "c_transactionAmount" || dataIndx == "d_transactionAmount" || dataIndx == "c_amount" || dataIndx == "d_amount" || dataIndx == "balance") {
                        /*
                         * Below IF block is used to showing the transaction as per currency Symbol
                         */
                        if (dataIndx == "c_transactionAmount" && recordvalue != "" && recordData['txnCurrSymbol']!="" && recordData['txnCurrSymbol']!=undefined) {
                            recordvalue = WtfGlobal.conventInDecimal(recordData['c_transactionAmount'], recordData['txnCurrSymbol']);
                        } else if (dataIndx == "d_transactionAmount" && recordvalue != "" && recordData['txnCurrSymbol']!="" && recordData['txnCurrSymbol']!=undefined) {
                            recordvalue = WtfGlobal.conventInDecimal(recordData['d_transactionAmount'], recordData['txnCurrSymbol']);
                        }else {
                            recordvalue = WtfGlobal.currencyRenderer(recordvalue);
                        }
                    } else if (dataIndx == "entryno") {
                        var jid = recordData['d_journalentryid'] || recordData['c_journalentryid'];
                        recordvalue = "<a href='#' onClick=Wtf.onCellClick('" + jid + "'," + Wtf.encode(startDate) + "," + Wtf.encode(endDate) + ")>" + recordvalue + "&nbsp;</a>";
                    } else if (dataIndx == "refno") {
                        var isLeaseFixedAsset = false;
                        var isconsignment = false;
                        var transactionType = recordData['doctype'];
                        if(transactionType=='Consignment Sales Invoice' ||transactionType=='Consignment Purchase Invoice'){
                            isconsignment=true;
                        }else if(transactionType=="Lease Invoice"){
                           isLeaseFixedAsset=true; 
                        }
                        var billid = recordData['billid'];
                        var noteid = recordData['noteid'];
                        var transactionID = recordData['refno'];
                        var jid = recordData['d_journalentryid'] || recordData['c_journalentryid'];
                        if (transactionType == '' || transactionType == undefined) {
                            recordvalue = "<a href='#' onClick=Wtf.onCellClick('" + jid + "'," + Wtf.encode(startDate) + "," + Wtf.encode(endDate) + ")>" + recordvalue + "&nbsp;</a>";
                        } else {
                            recordvalue = "<a href='#' onClick='Wtf.onCellClickofDocumentNo(\"" + transactionID + "\",\"" + billid + "\",\"" + noteid + "\",\"" + transactionType + "\",\"" + isconsignment + "\",\"" + isLeaseFixedAsset + "\");' >" + recordvalue + "</a>";
                        }
                    } else if (dataIndx == "description") {
                        recordvalue = recordvalue.replace(/\'/g, "&#39;");
                    } else if (dataIndx == "c_date" || dataIndx == "d_date") {
                        //Column : Date
                        var transactionDate = "";
                        if (recordData['d_date'] != '') {
                            recordvalue = recordData['d_date'];
                        } else if (recordData['c_date'] != '') {
                            recordvalue = recordData['c_date'];
                        }
                        if (recordvalue != "") {
                            transactionDate = new Date(recordvalue).format(WtfGlobal.getOnlyDateFormat());
                        }
                        recordvalue = transactionDate;
                    } else {
                        if ((recordvalue == undefined || recordvalue == "") && dataIndx.indexOf("d_") == 0) {
                            recordvalue = recordData[dataIndx.replace("d_", "c_")];
                        } else if ((recordvalue == undefined || recordvalue == "") && dataIndx.indexOf("c_") == 0) {
                            recordvalue = recordData[dataIndx.replace("c_", "d_")];
                        }
                    }
                    var tooltipValue = recordvalue.replace(Wtf.HTMLRegex,'').replace(/\"/g, "&#34;").replace(/\'/g, "&#39;");
                    if (!Wtf.ValidateHTMLinString(recordvalue)) {
                        recordvalue = Wtf.util.Format.ellipsis(recordvalue, 20);
                    }
                    
                    header += "<span class='gridRow' wtf:qtip=\""+tooltipValue+"\" style='text-overflow: ellipsis;overflow: hidden;width:" + widthInPercent + "%;text-align:" + alignTo + ";'>" + recordvalue + "&nbsp;</span>";
                }
                header += "</div>";
            }
            header += "<div style='width:" + width + "px;'><span class='gridLine'></span></div>";//Added grid line after the all the data
            header += " <div style='width:" + width + "px;display:table !important;height: 22px;'>";
            header += "<span class='headerRow' wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.total") + "'style='text-overflow: ellipsis;overflow: hidden;width: " + widthInPercent + "%; ! important;'>" + WtfGlobal.getLocaleText("acc.common.total") + "&nbsp;</span>"; //*Total* Header for showing Credit/Debit total amount.

            /*
             * Below loop is used to adjust the total Credit and Debit amount column width
             */
            for (var k = 1; k < title.length; k++) {
                if (title[k] == "Debit Amount in Base Currency (" + Wtf.account.companyAccountPref.currencycode + ")") {
                    totalDebitAmount = WtfGlobal.currencyRenderer(totalDebitAmount);
                    header += "<span class='headerRow' wtf:qtip='" + WtfGlobal.addCurrencySymbolOnly(totalDebitAmount, WtfGlobal.getCurrencySymbol(), [true]) + " 'style='text-align:right;width: " + widthInPercent + "%;! important;'>" + WtfGlobal.addCurrencySymbolOnly(totalDebitAmount, WtfGlobal.getCurrencySymbol(), [true]) + "&nbsp;</span>";
                } else if (title[k] == "Credit Amount in Base Currency (" + Wtf.account.companyAccountPref.currencycode + ")") {
                    totalCreditAmount = WtfGlobal.currencyRenderer(totalCreditAmount);
                    header += "<span class='headerRow' wtf:qtip='" + WtfGlobal.addCurrencySymbolOnly(totalCreditAmount, WtfGlobal.getCurrencySymbol(), [true]) + " 'style='text-align:right;width:" + widthInPercent + "%;! important;'>" + WtfGlobal.addCurrencySymbolOnly(totalCreditAmount, WtfGlobal.getCurrencySymbol(), [true]) + "&nbsp;</span>";
                } else {
                    header += "<span class='headerRow' style='text-overflow: ellipsis;overflow: hidden;width: " + widthInPercent + "%;'>&nbsp;</span>";
                }
            }
            header += "</div>";
        } else {
            header = "<span class='gridHeader'>" + gridHeaderText + "</span>";
            header += "<span class='headerRow'>" + WtfGlobal.getLocaleText("acc.field.Nodatatodisplay") + "</span>"
        }
        
        var disHtml = "<div class='expanderContainer' style='overflow:auto;'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    },
//    fillExpanderGrid: function(){
//        var arr = [];
//        var startDate=this.startDate.getValue();
//        var endDate=this.endDate.getValue();
//        var title = "",dataIndex="",align="",records=[];
//        if(this.expandStore.reader.jsonData !=undefined ){
//            title = this.expandStore.reader.jsonData.title;
//            dataIndex = this.expandStore.reader.jsonData.header;
//            align = this.expandStore.reader.jsonData.align;
//        }
//        
//        if(title !=undefined && typeof title == "string"){
//            title = title.split(",");
//        }
//        if(dataIndex !=undefined && typeof dataIndex == "string"){
//            dataIndex = dataIndex.split(",");
//        }
//        if(align !=undefined && typeof align == "string"){
//            align = align.split(",");
//        }
//        var columnArr = [],fieldsArr = [],columnObj,fieldsObj;
//        
//        for (var i=0;i<title.length;i++){
//            fieldsObj = {};
//            fieldsObj.name = dataIndex[i];
//            fieldsArr.push(fieldsObj);
//            
//            columnObj = {};
//            columnObj.header = title[i].trim();
//            columnObj.dataIndex = dataIndex[i];
////            columnObj.align = align[i];
//            columnObj.width = 160;
//            
//            if(dataIndex[i] == "c_transactionAmount" || dataIndex[i] == "d_transactionAmount" || dataIndex[i] == "c_amount" || dataIndex[i] == "d_amount"){
//                columnObj.renderer = WtfGlobal.currencyRenderer;
//            }
//            columnArr.push(columnObj);
//        }
//        
//        
//        var grid = Wtf.getCmp('testExpanderGrid');
//        if(grid != undefined){
//            var reader = grid.getStore().reader;
//            if(fieldsArr.lenght > 0){
//                var metaData = {
//                    totalProperty : reader.totalProperty,
//                    root : reader.root,
//                    fields : fieldsArr
//                }
//                delete reader.ef;
//                reader.meta = metaData;
//                reader.recordType = Wtf.data.Record.create(metaData.fields);
//                reader.onMetaChange(reader.meta, reader.recordType, "");
//            }
//                
//                
//            grid.reconfigure(grid.getStore(),new  Wtf.grid.ColumnModel(columnArr));
//            grid.getView().refresh(true);
//            grid.doLayout();
//        }
//    },
       
    balTypeRenderer:function(val,m,rec){
        if(rec.data["level"] != 0){
            val=(val==0?WtfGlobal.getLocaleText("acc.field.N/A"):(val>0?WtfGlobal.getLocaleText("acc.common.debit"):WtfGlobal.getLocaleText("acc.common.credit")));
            return WtfGlobal.deletedRenderer(val,m,rec)
       }
        return "";
    },
    
    subLedgerReport:function(){
        this.isGeneralLedger = true;
        new Wtf.account.subLedgerWindow({
            title: WtfGlobal.getLocaleText("acc.field.subLedgerExport"),  
            id: 'subLedgerWindowID',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            parentObj:this,
            closable: true,
            resizable: false,
            modal: true,
            scope :this,
            width: 840,
            height: 550,
            isGroupDetailReport:true
        }).show();
    },
    
    getMyConfig:function (){
        WtfGlobal.getGridConfig (this.grid, this.moduleid, false, true);
        
        var statusForCrossLinkage = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
},
saveMyStateHandler: function (grid,state){
    WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
}   
});
