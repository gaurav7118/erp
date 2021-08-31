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

Wtf.account.Ledger=function(config){
    this.summary = new Wtf.grid.GroupSummary({});
    this.accRec = Wtf.data.Record.create ([
        {name:'accountname',mapping:'accname'},
        {name:'accountid',mapping:'accid'},
        {name:'currencyid',mapping:'currencyid'},
        {name:'acccode'},
        {name:'groupname'}
    ]);
    this.accountID = config.accountID;
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=this.permType.exportdataledger;
    this.printPermType=this.permType.printledger;
    this.tabOpenFlag = true;
    this.accStore = new Wtf.data.Store({
        //        url: Wtf.req.account+'CompanyManager.jsp',
        url : "ACCAccountCMN/getAccountsForCombo.do",
        baseParams:{
            mode:2,
             ignorecustomers:true,  
             ignorevendors:true,
            nondeleted:true,
            headerAdded:true,
            ignoreTransactionFlag:false
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });
   
    this.LedgerRec = new Wtf.data.Record.create([
                {name: 'd_date',type:'date'},
                {name: 'd_accountname'},
                {name: 'd_acccode'},
                {name: 'd_entryno'},
                {name: 'd_journalentryid'},
                {name: 'd_amount'},
                {name: 'd_transactionID'},
                {name: 'd_transactionDetails'},
                {name: 'c_date',type:'date'},
                {name: 'c_accountname'},
                {name: 'c_acccode'},
                {name: 'c_entryno'},
                {name: 'c_journalentryid'},
                {name: 'c_amount'},
                {name: 'c_transactionID'},
                {name: 'c_transactionDetails'},
                {name: 'accountid'},
                {name: 'accountname'},
                {name: 'accCode'},
                {name: 'accCodeName'},
                {name: 'billid'},
                {name: 'type'},
                {name: 'noteid'},
                {name: 'c_amountAccountCurrency'},
                {name: 'd_amountAccountCurrency'},
                {name: 'currencysymbol'},
                {name: 'netbalance'},
                {name: 'isnetbalance'},
                {name:'d_transactionDetailsForExpander'},
                {name:'c_transactionDetailsForExpander'}    
        ]);
            
    this.LedgerStore = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.LedgerRec),
        //                url: Wtf.req.account+'CompanyManager.jsp',
        url:"ACCReports/getLedger.do",
        baseParams:{
            mode:61,
            withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
            consolidateFlag:config.consolidateFlag,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            ledgerReport:true,
            periodView: config.periodView
        },               
        sortInfo : {
            field : 'accCodeName',
            direction : 'ASC'
        },
        groupField : 'accCodeName'
    });

    this.rowNo=new Wtf.grid.RowNumberer();
    this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,
    {
        header: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //"Date",
        dataIndex: 'd_date',
        align:'center',
        width:70,
        pdfwidth:110,
        renderer:this.callDateAndNetBalaceRenderer 
    },
    {
        header: WtfGlobal.getLocaleText("acc.coa.accCode"),  //"Account",
        dataIndex: 'accCode',
        align:'center',
        width:40,
        pdfwidth:110,
        hidden:true
    },
    {
        header: WtfGlobal.getLocaleText("acc.ledger.gridAccount"),  //"Account",
        dataIndex: 'accCodeName',
        align:'center',
        width:40,
        pdfwidth:110,
        hidden:true
    },           
    {
        header: WtfGlobal.getLocaleText("acc.coa.accCode"),  //"Account Code",
        dataIndex: 'd_acccode',
        // renderer:this.formatAccountName,
        width:50,
        pdfwidth:100
    // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.accName"),  //"Account Name",
        dataIndex: 'd_accountname',
        renderer:this.formatAccountName,
        width:50,
        pdfwidth:100,
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'd_entryno',
        width:40,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        pdfwidth:100,
        renderer:WtfGlobal.linkRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.product.description"),
        dataIndex: 'd_transactionDetailsForExpander',
        width:30,
        pdfwidth:100,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        renderer:function(value,meta){
            meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Description' ";
            return value;
        }
    },{
        header: WtfGlobal.getLocaleText("acc.field.TranID"),
        dataIndex: 'd_transactionID',
        width:30,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        pdfwidth:100,
        renderer:config.consolidateFlag?"":WtfGlobal.linkRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.gridDebitAmount")+ " ("+WtfGlobal.getCurrencySymbol()+")",  //"Debit Amount",
        dataIndex: 'd_amount',
        align:'right',
        renderer:WtfGlobal.currencyRenderer,
        width:40,
        summaryType:'sum',
        pdfwidth:125,
        hidecurrency : true,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.field.DebitAmountinAccountCurrency"),//WtfGlobal.getLocaleText("acc.ledger.debitAmountAccountCcy"),
        dataIndex: 'd_amountAccountCurrency',
        id:'d_amountAccountCurrency',
        align:'right',
        width:40,
        pdfwidth:100,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        renderer:this.accountCurrencyRenderer,
        pdfrenderer : "rowcurrency"
    },{
        width:1,
        renderer:this.showSeperator
    },{
        header: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //"Date",
        dataIndex: 'c_date',
        align:'center',
        width:30,
        renderer:WtfGlobal.onlyDateRenderer
    },
    {
        header: WtfGlobal.getLocaleText("acc.coa.accCode"),  //"Account Code",
        dataIndex: 'c_acccode',
        // renderer:this.formatAccountName,
        width:50
    //                pdfwidth:100
    // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.accName"),  //"Account Name",
        dataIndex: 'c_accountname',
        renderer:this.formatAccountName,
        width:50,
        //                pdfwidth:100,
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'c_entryno',
        width:40,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        renderer:WtfGlobal.linkRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.product.description"),
        dataIndex: 'c_transactionDetailsForExpander',
        width:40,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        renderer:function(value,meta){
            meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Description' ";
            return value;
        }
    },{
        header: WtfGlobal.getLocaleText("acc.field.TranID"),
        dataIndex: 'c_transactionID',
        width:30,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        renderer:config.consolidateFlag?"":WtfGlobal.linkRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.ledger.gridCreditAmount")+ " ("+WtfGlobal.getCurrencySymbol()+")",  //"Credit Amount",
        align:'right',
        dataIndex: 'c_amount',
        renderer:WtfGlobal.currencyRenderer,
        width:40,
        summaryType:'sum',
        pdfwidth:125,
        hidecurrency : true,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.field.CreditAmountinAccountCurrency"),//WtfGlobal.getLocaleText("acc.ledger.creditAmountAccountCcy"),  //"Account",,
        dataIndex: 'c_amountAccountCurrency',
        id:'c_amountAccountCurrency',
        align:'right',
        width:40,
        pdfwidth:100,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        renderer:this.accountCurrencyRenderer,
        pdfrenderer : "rowcurrency"
    }]);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store: this.LedgerStore,
        cm: this.gridcm,
        border : false,
        loadMask : true,
        view : new Wtf.grid.GroupingView({
            forceFit : true,
            showGroupName : true,
            enableGroupingMenu : true,
            hideGroupedColumn : false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }),
        plugins:[this.summary],
//        viewConfig: {
//            forceFit:true
//        },
        tbar:[WtfGlobal.getLocaleText("acc.trial.debit"),'->',WtfGlobal.getLocaleText("acc.trial.credit")]
    });    
    this.grid.on("render",function(){
        this.grid.getView().applyEmptyText(); 
    },this);
    this.LedgerStore.on("beforeload", function(store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
    }, this);
    this.LedgerStore.on("loadexception", function(store) {
        WtfGlobal.resetAjaxTimeOut();
    }, this);
    this.LedgerStore.on("load", function(store){
        WtfGlobal.resetAjaxTimeOut();
        if(store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();

           // if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.disable();
        }else{

            //    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermTypee)){
           // if(this.expButton)this.expButton.enable();
            // if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
            if(this.printButton)this.printButton.enable();//}
        }
    },this);
    
     this.MSComboconfig = {
                store: this.accStore,
                valueField:'accountid',
                hideLabel:true,
                displayField:'accountname',
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,
                triggerAction:'all',
                scope:this
            };
             this.cmbAccount = new Wtf.common.Select(Wtf.applyIf({
               name:'accountid',
               multiSelect:true,
               emptyText:WtfGlobal.getLocaleText("acc.field.SelectAccount"),
               fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
               forceSelection:true,         
               extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
               extraComparisionField:'acccode',// type ahead search on acccode as well.
               listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
               hidden: this.accountID == "Difference in Opening balances",
               width:200
             },this.MSComboconfig));      
 
    this.cmbAccount.on('select',function(combo,accRec,index){ //multiselection in case of all 
            if(accRec.get('accountid')=='All'){  //case of multiple record after all
                combo.clearValue();
                combo.setValue('All');
            }else 
                if(combo.getValue().indexOf('All')>=0){  // case of all after record
                combo.clearValue();
                combo.setValue(accRec.get('accountid'));
            }
        } , this);       
        
    this.interCompanyTypeRec = Wtf.data.Record.create ([
                {name:'id'},
                {name:'name'}
    ]);

    this.InterCompanyTypeStore=new Wtf.data.Store({
        url:"ACCMaster/getMasterItems.do",
        baseParams:{
            mode:112,
            groupid:14,
            common:'1'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.interCompanyTypeRec)
    });
           
    this.cmbInterCompanyType=new Wtf.form.ExtFnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
        //                id:'accountIdForCombo'+config.id ,
        name:'intercompanytypeid',
        store:this.InterCompanyTypeStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        width:150,
        listWidth:400,
        hiddenName:'intercompanytypeid',
        emptyText:WtfGlobal.getLocaleText("acc.field.SelectCompanyType"),
        allowBlank:false,
        forceSelection:true,
        extraFields:[],
        triggerAction:'all'
    });
           
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        value:this.getDates(true)
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
       // readOnly:true,
        name:'enddate',
        value:this.getDates(false)
    });           
    if(config.stDate!="" && config.stDate!=undefined){
        this.startDate.setValue(config.stDate);
    }
    if(config.enDate!="" && config.enDate!=undefined){
        this.endDate.setValue(config.enDate);
    }
           
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: config.moduleid,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        //        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        //        hidden:(this.moduleid==undefined)?true:false,       
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });       
    
     this.balPLTypeStore = new Wtf.data.SimpleStore({
                    fields: [{name:'typeid',  type:'int'}, 'name'],
                    data :[
                                [0, "All"],
                                [1, "Balance Sheet"],
                                [2, "Profit & Loss"]                   
                    ]
                });
    
    this.balPLTypeCombo = new Wtf.form.ComboBox({     //All/Balance Sheet/Profit & Loss
        store: this.balPLTypeStore,
        name:'typeid',
        displayField:'name',
//        id:'typeid',
        valueField:'typeid',
        mode: 'local',
        value:0,
        width:100,
        listWidth:200,
        triggerAction: 'all',
        typeAhead:true,
        hidden: this.accountID == Wtf.Difference_in_Opening_balances,
        selectOnFocus:true
    });    
      
    this.typeStore = new Wtf.data.SimpleStore({
                    fields:[{
                        name:'name'
                    },{
                        name:'value',
                        type:'boolean'
                    }],
                    data:[['All',false],['Exclude Previous Year Balance',true]]
                });
        
    this.excludeTypeCmb= new Wtf.form.ComboBox({
        labelSeparator:'',
        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        valueField:'value',
        displayField:'name',
        store:this.typeStore,
        value:true,
        width:200,
        disabledClass:"newtripcmbss",
        name:'excludePreviousYear',
        hiddenName:'excludePreviousYear',
        hidden: this.accountID == Wtf.Difference_in_Opening_balances
    });  
    
     this.balPLTypeCombo.on("select",function(){                    
        if(this.balPLTypeCombo.getValue()==2 ||this.balPLTypeCombo.getValue()==0){
            this.excludeTypeCmb.enable();
            this.excludeTypeCmb.setValue(true);
        }else{
            this.excludeTypeCmb.disable();
            this.excludeTypeCmb.setValue(false)
        }
    },this);
    
    var btnArr=[];
    
    
    if(config.intercompanyFlag) {
        btnArr.push(WtfGlobal.getLocaleText("acc.cust.intercompanytype"),this.cmbInterCompanyType);
    }
    btnArr.push(
        this.accountID == Wtf.Difference_in_Opening_balances ?"":WtfGlobal.getLocaleText("acc.ledger.accName"),
        this.cmbAccount,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,
        WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',this.AdvanceSearchBtn
        );
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push("-",this.expButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.export"),
            obj:this,
            filename:WtfGlobal.getLocaleText("acc.financialStatements"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),
      //      disabled :true,
                params:{stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                accountid:this.accountID||config.accountID,
                periodView: config.periodView,
                name: "Ledger"
            },
                    menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:117
        }));
  

    this.expButton.on("click",function(){
        if(this.cmbAccount.getValue() == "")
        {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.noaccselected")], 2);
            this.expButton.hideMenu();
        } else{//setting the parameters without loading the store of the grid.
             this.expButton.setParams({
                accountid:this.cmbAccount.getValue(),
                stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")+"-"+this.cmbAccount.getRawValue(),
                balPLId:this.balPLTypeCombo.getValue(),
                excludePreviousYear: this.excludeTypeCmb.getValue(),
                periodView : config.periodView
            });
        }
    },this);
    } 
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        btnArr.push("-",this.printButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
            filename:WtfGlobal.getLocaleText("acc.dashboard.consolidateLedgerReport"),
            disabled :true,
                params:{stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                accountid:this.accountID||config.accountID,
                name: WtfGlobal.getLocaleText("acc.ledger.tabTitle"),
                balPLId:this.balPLTypeCombo.getValue(),
                periodView: config.periodView,
                excludePreviousYear: this.excludeTypeCmb.getValue()
            },
            label:WtfGlobal.getLocaleText("acc.ledger.tabTitle"),
                    menuItem:{print:true},
            get:117
        }));
    }
    
    var secondBtnArr=[];
            
    if(this.accountID != Wtf.Difference_in_Opening_balances){
        secondBtnArr.push(WtfGlobal.getLocaleText("acc.product.gridType"),'-',this.balPLTypeCombo,
            this.excludeTypeCmb,
            '-',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"),  //"Select a time period to view corresponding ledger records.",
                iconCls:'accountingbase fetch',
                scope:this,
                handler:this.onClick
            });
    }
            
        var firstbar = new Wtf.Toolbar(btnArr);    
        var secondtbar = new Wtf.Toolbar(secondBtnArr);
            
        this.toolbarPanel = new Wtf.Panel({
            items:[firstbar,secondtbar]
        });
        
    Wtf.apply(this,{
        items:[{
            layout:'border',
            border:false,
            scope:this,
            items:[this.objsearchComponent,{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid
            }],
            tbar:this.toolbarPanel
        }]
    },config)

    Wtf.account.Ledger.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    this.grid.on('cellclick',this.onCellClick, this);
    this.accStore.on('load',function(){this.showLedger(config.accountID,this.startDate.getValue(),this.endDate.getValue());},this);
    this.accStore.on("load", function(store){
        var storeNewRecord=new this.accRec({
            accountname:'All',
            accountid:'All',
            acccode:''
        });
        this.cmbAccount.store.insert( 0,storeNewRecord);
        if(config.intercompanyFlag){
            this.cmbAccount.setValue("All");
            if(this.tabOpenFlag){
                this.tabOpenFlag = false;
                this.onClick();
            }
        }
    },this);
//    this.accStore.on("load", function(store){
//        var storeNewRecord=new this.accRec({
//            accountname:'None',
//            accountid:'None'
//        });
//        this.cmbAccount.store.insert( 0,storeNewRecord);
//            this.cmbAccount.setValue("None");
//    },this);

    if(config.intercompanyFlag) {       
        this.InterCompanyTypeStore.on("load", function(store){
            var storeNewRecord=new this.accRec({
                name:'All',
                id:'All'
            });
            this.cmbInterCompanyType.store.insert( 0,storeNewRecord);
            this.cmbInterCompanyType.setValue("All");           
            this.accStore.load({params:{
                    intercompanytypeid:"All",
                    intercompanyflag:true
            }});
        },this);

    this.InterCompanyTypeStore.load();
       
    this.cmbInterCompanyType.on("select", function() {           
        this.LedgerStore.removeAll();
        this.grid.getView().emptyText=WtfGlobal.getLocaleText("acc.field.ClickonFetchbuttontoviewrecords");
        this.grid.getView().refresh();
            this.accStore.load({params:{
                intercompanytypeid:this.cmbInterCompanyType.getValue(),
                intercompanyflag:true
            }});
    }, this);
    this.cmbAccount.on("select", function() {
        this.LedgerStore.removeAll();
        this.grid.getView().emptyText=WtfGlobal.getLocaleText("acc.field.ClickonFetchbuttontoviewrecords");
        this.grid.getView().refresh();
        }, this);
    } else {
        this.accStore.load();
    }
}

Wtf.extend( Wtf.account.Ledger,Wtf.Panel,{
    formatAccountName:function(val,m,rec){
         if(val=="Total"){return "<b>"+val+"</b>";}
         else{return val}
    },
    
    onClick:function(){
        if(this.cmbAccount.getValue()==""){
            this.cmbAccount.setValue("All");
        }
        this.accountID=this.cmbAccount.getValue();
        this.fetchLedger();
        this.accStore.findBy( function(rec){
            if(rec.data['accountid'] == this.accountID) {
                if(rec.data['currencyid'] == Wtf.pref.Currencyid) {
                    this.gridcm.setHidden(this.gridcm.getIndexById("d_amountAccountCurrency"), true);
                    this.gridcm.setHidden(this.gridcm.getIndexById("c_amountAccountCurrency"), true);
                } else {
                    if(this.accountID == Wtf.Difference_in_Opening_balances){
                        this.gridcm.setHidden(this.gridcm.getIndexById("d_amountAccountCurrency"), true);
                        this.gridcm.setHidden(this.gridcm.getIndexById("c_amountAccountCurrency"), true);
                    }else{
                        this.gridcm.setHidden(this.gridcm.getIndexById("d_amountAccountCurrency"), false);
                        this.gridcm.setHidden(this.gridcm.getIndexById("c_amountAccountCurrency"), false);
                    }
                }
            }
        },this)
    },
    
    fetchLedger:function(){
            WtfGlobal.setAjaxTimeOut();
            var sDate=this.startDate.getValue();
            var eDate=this.endDate.getValue();
       
            if(sDate>eDate){
                WtfComMsgBox(1,2);
                return;
            }
            if(this.startDate != undefined || this.startDate != null){
                this.sDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
            }else{
                this.sDate=WtfGlobal.convertToGenericStartDate(this.getDates(true));
            }
            if(this.endDate != undefined || this.endDate != null){
                this.eDate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
            }else{
                this.eDate=WtfGlobal.convertToGenericEndDate(this.getDates(false));
            }
            if(this.accountID&&this.accountID.length>0){
                this.LedgerStore.load({
                    params:{
                        accountid:this.accountID,
                        stdate:this.sDate,   
                        enddate:this.eDate,
                        searchJson: this.searchJson,
                        filterConjuctionCriteria: this.filterConjuctionCrit,
                        intercompanytypeid:this.cmbInterCompanyType.getValue(),
                        intercompanyflag:this.intercompanyFlag?true:false,
                        excludePreviousYear: this.excludeTypeCmb.getValue(),
                        balPLId:this.balPLTypeCombo.getValue(),
                        isFromTledgerReport:true
                    }
                });
                if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                    this.expButton.setParams({
                        accountid:this.accountID,
                        stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                        name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")+"-"+this.cmbAccount.getRawValue(),
                        balPLId:this.balPLTypeCombo.getValue(),
                        excludePreviousYear: this.excludeTypeCmb.getValue(),
                        isFromTledgerReport:true
                    });
                }
                if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                    this.printButton.setParams({
                        accountid:this.accountID,                   
                        stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                        name: WtfGlobal.getLocaleText("acc.ledger.tabTitle")+"-"+this.cmbAccount.getRawValue(),
                        balPLId:this.balPLTypeCombo.getValue(),
                        excludePreviousYear: this.excludeTypeCmb.getValue(),
                        isFromTledgerReport:true
                    });
                }
            }
    },

    showLedger:function(accid,startDate,endDate){
        var account= "";
        if (accid != "" && accid != undefined) {
            if(accid == Wtf.Difference_in_Opening_balances){            
                var storeNewRecord=new this.accRec({
                    accountname:'Difference in Opening balances',
                    accountid:'Difference in Opening balances'
                });
                this.cmbAccount.store.insert( 0,storeNewRecord);
                account = Wtf.Difference_in_Opening_balances;
            }else{
                var accarr = accid.split(",");
                account = accarr[0];
          
            }
        }
        var i=this.accStore.find("accountid",account);
        if(i>=0){
            this.cmbAccount.setValue(accid);
            if(startDate!="" && startDate!=undefined){
                this.startDate.setValue(startDate);
            }
            if(endDate!="" && endDate!=undefined){
                this.endDate.setValue(endDate);
            }
            if(accid!='None'){   
                this.accountID=accid;
                this.onClick();
            }
        }
    },

    onRender:function(config){
        Wtf.account.Ledger.superclass.onRender.call(this,config);
    },

    callDateAndNetBalaceRenderer:function(v,m,rec){  
        var result="";
        if(rec.data.isnetbalance==true){
            result="<b>Net Balance</b>"+ WtfGlobal.currencyRenderer(rec.data.netbalance);
            return result;
        } else{
            result=WtfGlobal.onlyDateRenderer(v); 
            return result;  
        }      
    },
    
    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.LedgerStore.getAt(i);
        var jid=rec.data['d_journalentryid'];
        if(!jid||jid.length<=0)
            jid=rec.data['c_journalentryid'];
        this.fireEvent('journalentry',jid,true,this.consolidateFlag);
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="c_entryno" || header=="d_entryno"){
            var rec=this.LedgerStore.getAt(i);
            var jid=rec.data['d_journalentryid'];
            if(!jid||jid.length<=0)
                jid=rec.data['c_journalentryid'];
            this.fireEvent('journalentry',jid,true,this.consolidateFlag);
        } else if(header=="d_transactionID" ||header=="c_transactionID"){
            var formrec = this.LedgerStore.getAt(i);
            var type=formrec.data['type'];
            viewTransactionTemplate(type, formrec);
        }
    },

    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    dateRenderer:function(v){
        if(v) return v.format('Y M d');
        return "";
    },

    showSeperator:function(){
        return '<div style="margin:-5px"><img src="../../images/header.gif"></div>';
    },

    accountCurrencyRenderer:function(val,m,rec){ //added forigin currency symbol for  Amount in Account Currency
        if (!(val==="")) {
            var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);        
            var v=parseFloat(val);
            if(isNaN(v)) return val;
            if(rec.data.deleted)
                v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
            else
                    v=WtfGlobal.conventInDecimal(v,symbol);

            return '<div class="currency">'+v+'</div>';
        } else {
            return "";
        }
    },
    
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
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
        this.onClick();
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.fetchLedger();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },   
    
    storeLoad: function() {
        this.fetchLedger();
        this.doLayout();
    }   
});


//-------------------------------------------------------------------------------------------------------------------
//---------------CUSTOMER AND VENDOR ACCOUNT STATEMENT REPORT-------------------------------------------------------------------

Wtf.account.cvLedger=function(config){
    this.moduleid=config.moduleid;
    this.reportid=config.reportid;
    this.gridConfigId = "";
    this.summary = new Wtf.grid.GroupSummary({});
    this.isCustomer = config.isCustomer;
    
    Wtf.grid.GroupSummary.Calculations['last'] = function(v, record, colName){
        var result = 0;
        var val = record.data[colName];
        var count = record.store.getCount();
        var rowIdx = record.store.find("referid",record.data.referid);

        if(rowIdx == (count-1)){
            result = val;
        }else{
//            var nextRec = record.store.getAt(rowIdx + 1);
//            if(record.data["type"] != nextRec.data["type"])
                result = val;
        }

        return result;
    };

    Wtf.grid.GroupSummary.Calculations['debitSum'] = function(v, record, colName){
        if(record.data[colName] && record.data["type"] != "Cash Sale")
            return v + record.data[colName];
        else
            return v;
    };
    Wtf.grid.GroupSummary.Calculations['creditSum'] = function(v, record, colName){
        if(record.data[colName] && record.data["type"] != "Cash Purchase")
            return v + record.data[colName];
        else
            return v;
    };

    this.custRec = Wtf.data.Record.create ([{
        name:'accountname',
        mapping:'accname'
    },{
        name:'accountid',
        mapping:'accid'
    },{
        name:'acccode'
    }]);

    this.custStore = new Wtf.data.Store({
        url : this.isCustomer? "ACCCustomer/getCustomersForCombo.do": "ACCVendor/getVendorsForCombo.do",
        baseParams:{
            nondeleted:true,
            combineData:this.isCustomer?1:-1  //Send For Seprate Request
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
        },this.custRec)
    });

    this.LedgerRec = new Wtf.data.Record.create([{
        name: 'jeEntryDate',
        type:'date'
    },{
        name: 'invoiceId'
    },{
        name: 'invoiceNumber'
    },{
        name: 'supplierinvoiceno'
    },{
        name: 'jeId'
    },{
        name: 'memo'
    },{
        name: 'currencyid'
    },{
        name: 'currencysymbol'
    },{
        name: 'jeEntryNumber'
    },{
        name: 'jeEntryExternalCurrencyRate'
    },{
        name: 'ExternalCurrencyRate'
    },{
        name: 'debitAmountInBase'
    },{
        name: 'creditAmountInBase'
    },{
        name: 'balanceAmountInBase'
    },{
        name: 'debitAmount'
    },{
        name: 'creditAmount'
    },{
        name: 'tdsAmount'
    },{
        name: 'balanceAmount'
    },{
        name: 'type'
    },{
        name: 'accId'
    },{
        name: 'accName'
    },{
        name: 'billid'
    },{
        name: 'noteid'
    },{
        name: 'categoryName'
    },{
        name: 'referid'
     },{
        name: 'billingEmail'
    },{
        name: 'currencycode'
    },{
        name: 'accCode'
    },{
        name:'isLeaseFixedAsset'
    },{
        name : 'documentStatus'
    }]);
   
    this.LedgerStore = new Wtf.ux.grid.MultiGroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.LedgerRec),
        remoteSort: true,
        url:"ACCReports/"+ (this.isCustomer ? "getCustomerLedger": "getVendorLedger") +  ".do",       
        groupField: ['accCode', 'type']
    });
   
    var groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: false,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal:false,
        isGroupTotal:false,
        hideGroupedColumn: false,
        emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.field.soareport.selectdate')),
        groupTextTpl: '{group} '
    });
   
    var gridSummary = new Wtf.ux.grid.GridSummary({});

    this.rowNo=new Wtf.grid.RowNumberer();
    var gridColumns = [];
    gridColumns.push(this.rowNo,{
        header: (this.isCustomer?WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.agedPay.ven")) + WtfGlobal.getLocaleText("Code"),
        dataIndex: 'accCode',
        align:'center',
        width:80,
        pdfwidth:80,
        sortable : true,
        hideable: false
    },{
        header: (this.isCustomer?WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.agedPay.ven")) + WtfGlobal.getLocaleText("acc.userAdmin.name"),
        dataIndex: 'accName',
        align:'center',
        width:80,
        pdfwidth:80,
        sortable : true
    },{
        header: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //"Date",
        dataIndex: 'jeEntryDate',
        align:'center',
        width:150,
        pdfwidth:110,
        sortable : true,
        renderer:WtfGlobal.onlyDateRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.field.TransactionID"),
        dataIndex: 'invoiceNumber',
        width:100,
        align:'center',
        pdfwidth:150,
        sortable : true,
        renderer:WtfGlobal.linkRenderer

    });
    
    if(!this.isCustomer && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
        gridColumns.push({
            header: WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNumber"),
            dataIndex: 'supplierinvoiceno',
            width:100,
            pdfwidth:100
        });
    }
    
    gridColumns.push({
        header: WtfGlobal.getLocaleText("acc.field.TransactionType"),
        dataIndex: 'type',
        align:'center',
        width:150,
        pdfwidth:80,
        sortable : true
    },{
        header: WtfGlobal.getLocaleText("acc.je.tabTitle"),
        dataIndex: 'jeEntryNumber',
        align:'center',
        width:150,
        pdfwidth:100,
        renderer:WtfGlobal.linkRenderer
    },{
        header: Wtf.account.companyAccountPref.descriptionType,
        dataIndex: 'memo',
        align:'center',
        width:180,
        pdfwidth:150,
        summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header: WtfGlobal.getLocaleText("acc.setupWizard.curEx"),  
        dataIndex: 'ExternalCurrencyRate',
        align:'right',
        width:180,
        pdfwidth:125//,
    },{
        header: WtfGlobal.getLocaleText("acc.je.debitAmt"), //"Debit Amount",
        dataIndex: 'debitAmount',
        align:'right',
        renderer:WtfGlobal.withoutRateCurrencySymbolforDebit,
        width:180,
        pdfwidth:125//,
    //        summaryType: 'sum',
    //        summaryRenderer: function(value, m, rec) {
    //            if (value != 0) {
    //                var retVal = WtfGlobal.currencySummaryRendererSymbol(value, m, rec)
    //                return retVal;
    //            } else {
    //                return '';
    //            }
    //        }
    }, {
        header: WtfGlobal.getLocaleText("acc.je.creditAmt"),
        align:'right',
        dataIndex: 'creditAmount',
        renderer:WtfGlobal.withoutRateCurrencySymbolforCredit,
        width:180,
        pdfwidth:125
    //summaryType:'sum',
    //        summaryRenderer: function(value, m, rec) {
    //            if (value != 0) {
    //                var retVal = WtfGlobal.currencySummaryRendererSymbol(value, m, rec)
    //                 return retVal;
    //            } else {
    //                return '';
    //            }
    //        }
    },{
        header: WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency")+ " ("+WtfGlobal.getCurrencyName()+")", //"Debit Amount",
        dataIndex: 'debitAmountInBase',
        align:'right',
        renderer:WtfGlobal.globalCurrencySymbolforDebit,
        width:180,
        pdfwidth:125,
        summaryType: 'sum',
        hidecurrency : true,
        summaryRenderer: function(value, m, rec) {
            if (value != 0) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            } else {
                return '';
            }
        }
    },{
        header: WtfGlobal.getLocaleText("acc.cust.currency"), //"Currency",
        dataIndex: 'currencycode',
        align:'right',
        renderer:WtfGlobal.globalCurrencySymbolforDebit,
        width:80,
        pdfwidth:125
   },{
        header: WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency") + " ("+WtfGlobal.getCurrencyName()+")",
        align:'right',
        dataIndex: 'creditAmountInBase',
        renderer:WtfGlobal.globalCurrencySymbolforCredit,
        width:180,
        pdfwidth:125,
        summaryType:'sum',
        hidecurrency : true,
        summaryRenderer: function(value, m, rec) {
            if (value != 0) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            } else {
                return '';
            }
        }
    });
    if(!this.isCustomer && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
        gridColumns.push({
            header: WtfGlobal.getLocaleText("acc.invoice.gridTDSAmt"),
            renderer:WtfGlobal.currencyRenderer,
            align:'right',
            id: 'tdsamount',
            dataIndex: 'tdsAmount',
            width:180,
            pdfwidth:125
        });
    }
        gridColumns.push({
        header: WtfGlobal.getLocaleText("acc.field.BalanceAmountinbasecurrency") + " ("+WtfGlobal.getCurrencyName()+")",
        align:'right',
        dataIndex: 'balanceAmountInBase',
        renderer:WtfGlobal.currencyRenderer,
        width:180,
        pdfwidth:125,
        summaryType: 'last',
        hidecurrency : true,
        summaryRenderer: function(value, m, rec) {
            if (value != 0) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            } else {
                return '';
            }
        }
    });
    
    /*---------Document Status column calulated as per amountdue of "as of date"----------*/
    gridColumns.push({
        header: WtfGlobal.getLocaleText("acc.field.documentstatus"),
        dataIndex: 'documentStatus',
        align: 'center',
        width: 150,
        pdfwidth: 80,
        sortable: true,
        hidden : (this.reportid==undefined),//Hidden for combined Account statement tab
        renderer:WtfGlobal.stringValueRenderer
    });
        
    this.gridcm= new Wtf.grid.ColumnModel(gridColumns);
    this.grid = new Wtf.ux.grid.MultiGroupingGrid({
        store: this.LedgerStore,
        cm: this.gridcm,
        plugins:[gridSummary],
        border : false,
        //        loadMask : true,
        view: groupView, 
        bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.LedgerStore,
            //searchField: this.cmbAccount.getValue(),
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id: "pPageSize_" + this.id
            })
        })
    });
    this.LedgerStore.on("datachanged", function(store){
        if(store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
//            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.disable();
        }else{
//            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
        Wtf.MessageBox.hide();
        if (this.SOALoadMask != undefined) {
            this.SOALoadMask.hide();
        }
        //After selecting All from paging, remoteSort becomes false. But for SOA we need remote sorting true even in All paging.
        this.LedgerStore.remoteSort = true;
    },this);
     
    this.LedgerStore.on("beforeload", function(s) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        if (this.grid.el != undefined) {
            this.SOALoadMask = new Wtf.LoadMask(this.grid.el.dom, {
                msg: "Loading..."
            });
            this.SOALoadMask.show();
        }
        var isPostDatedCheque=null;
        var index=this.typeEditorForCheque.getValue();
        if(index==1){
            isPostDatedCheque=true;
        }else{
            isPostDatedCheque=false;
        }
        var idParamName = this.isCustomer ? "customerid":"vendorid";
        s.baseParams.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        s.baseParams.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        s.baseParams.asofdate=WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue());
        s.baseParams.invoiceAmountDueFilter=this.cmbInvoiceType.getValue();
        s.baseParams.withoutinventory = Wtf.account.companyAccountPref.withoutinventory;
        s.baseParams.customerIds = this.accountID;
        s.baseParams.vendorIds = this.accountID;
        s.baseParams.isPostDatedCheque=isPostDatedCheque;
        s.baseParams.idparamname = idParamName;
        s.baseParams.isSortedOnCreationDate = this.groupCombo.getValue()==1?true:false;
        s.baseParams.reportid = this.reportid;
        if(this.grid.getStore().getSortState()!=undefined){
            s.baseParams.sort=this.grid.getStore().getSortState().field;
            s.baseParams.dir=this.grid.getStore().getSortState().direction;
        }
        if(this.pP.combo!=undefined){
            if(this.pP.combo.value=="All"){
                var count = this.LedgerStore.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                s.paramNames.limit = count;
            }
        }        
    }, this); 
      this.LedgerStore.on('loadexception', function() {
        WtfGlobal.resetAjaxTimeOut();
        if (this.SOALoadMask != undefined) {
            this.SOALoadMask.hide();
        }
        this.grid.getView().refresh();
    }, this);
     
    this.CustomerMSComboconfig={
            hiddenName:'accountmulselectcombo',         
            store: this.custStore,
            valueField:'accountid',
            hideLabel:false,
            hidden : false,
            displayField:'accountname',
//            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") ,
            mode: 'remote',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };
        
        this.cmbAccount = new Wtf.common.SelectPaging(Wtf.applyIf({
                multiSelect:true,
                fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',,
                forceSelection:true,   
                extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                extraComparisionField:'acccode',// type ahead search on acccode as well.
                listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
                pageSize:Wtf.ProductCombopageSize,
                width:200
            },this.CustomerMSComboconfig));
        
        this.custStore.on("load", function(store){
            var storeNewRecord=new this.custRec({
                 accountname:'All',
                 accountid:'All',
                 acccode:'All'
            });
//            this.cmbAccount.store.insert( 0,storeNewRecord);
//            if (this.cmbAccount.getValue() == undefined || this.cmbAccount.getValue() == "") {
//                this.cmbAccount.setValue("All");
//            }
            WtfGlobal.resetAjaxTimeOut();
        }, this);
    this.custStore.on("beforeload", function(store){
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
    },this);
    if(!(this.reportid == Wtf.autoNum.SOA_CustomerAccountStatement || this.reportid == Wtf.autoNum.SOA_VendorAccountStatement)) {
        this.custStore.load();
    }
    
    this.cmbAccount.on('select',function(combo,custRec,index){ //multiselection in case of all 
        if(custRec.get('accountid')=='All'){  //case of multiple record after all
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){  // case of all after record
            combo.clearValue();
            combo.setValue(custRec.get('accountid'));
        }
    } , this);
    
     this.invoiceTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:"boolean"}, 'name'],
        data :[[true,WtfGlobal.getLocaleText("acc.ledger.accAllTransactions")],[false,WtfGlobal.getLocaleText("acc.ledger.accOutStandingTransaction")]]
    });
      this.cmbInvoiceType=new Wtf.form.ComboBox({
       // fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accInvoice"),  //'Select Invoice',
        name:'invoicetype',
        store:this.invoiceTypeStore,
        valueField:'typeid',
        displayField:'name',
        value:true,
        mode: 'local',
        width:120,
        hiddenName:'invoicetype',
        emptyText:WtfGlobal.getLocaleText("acc.ledger.accSelectTransactions"),
        triggerAction:'all'
      
    });
    this.groupComboStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data:[[0,"Group by Transaction Type"],[1,"Sort by Date"]]
    });
    this.groupCombo = new Wtf.form.ComboBox({
        store: this.groupComboStore,
        name:'groupCombo',
        displayField:'name',
        value:0,
        width:200,
        valueField:'id',
        mode: 'local',
        triggerAction: 'all'
    });
    this.groupCombo.on('select',this.groupComboChange,this);
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat()
     //   readOnly:true,
//        value:this.getDates(true)
    });
    this.asOfDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.asOf"),  //'As of',
        name:'asofdate',
        format:WtfGlobal.getOnlyDateFormat()
//        value:this.getDates(false)
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        //  readOnly:true,
        name:'enddate'
//        value:this.getDates(false)
    });
    this.startDate.on("change",this.checkDates,this);
    this.asOfDate.on("change",this.checkDates,this);
    this.endDate.on("change",this.checkDates,this);
    this.delTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.ledger.accInclude")],[1,WtfGlobal.getLocaleText("acc.ledger.accExclude")]]
    });
    this.typeEditorForCheque = new Wtf.form.ComboBox({
        store: this.delTypeStore,
        name:'typeid',
        displayField:'name',
        //id:'view'+(config.isCustomer ? "customerid":"vendorid"),    //config.helpmodeid,
        valueField:'typeid',
        mode: 'local',
        value:0,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    
     this.interval=new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.till"),  //'Till',
        maxLength:2,
        width:30,
        allowDecimal:false,
        allowBlank:true,
        minValue:2,
        name:'duration',
        value:this.isCustomer ? Wtf.agedReceivableInterval: Wtf.agedPayableInterval // Ageing Config will be used in SOA Report , default is 30
    });
    
    this.noOfIntervalStore= new Wtf.data.SimpleStore({
        fields: [{name : 'value'}],
        data :[[2], [3], [4], [5], [6], [7], [8], [9], [10]]
    });
    
    this.noOfIntervalCombo = new Wtf.form.ComboBox({
        store: this.noOfIntervalStore,
        name:'noOfInterval',
        displayField:'value',
        value:this.isCustomer ? Wtf.agedReceivableNoOfInterval : Wtf.agedPayableNoOfInterval, // Ageing Config will be used in SOA Report, default is 30
        width:50,
        valueField:'value',
        mode: 'local',
        triggerAction: 'all'
    });
    
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    var moduleArr = Wtf.Acc_Receive_Payment_ModuleId+","+Wtf.Acc_Make_Payment_ModuleId+","+Wtf.Acc_Credit_Note_ModuleId+","+Wtf.Acc_Debit_Note_ModuleId+"";
    if(this.isCustomer){
        moduleArr += ","+Wtf.Acc_Invoice_ModuleId;
    }else{
        moduleArr += ","+Wtf.Acc_Vendor_Invoice_ModuleId;
    }
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: moduleArr.split(','),
        advSearch: false,
        isAvoidRedundent:true,
        customerCustomFieldFlag : this.isCustomer?true:false,
        vendorCustomFieldFlag : this.isCustomer?false:true,
        ignoreDefaultFields :true,
        reportid:this.isCustomer? Wtf.Acc_Customer_AccountStatement_ModuleId : Wtf.Acc_Vendor_AccountStatement_ModuleId
    });
    
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
      this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: this.isSummary,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
       this.resetBttn.on('click',this.handleResetClickNew,this);
       /*
        * Provided button to expand or collapse all row details. 
        */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    
//    var agedDateFilterField = " Aged Date Filter";
    var agedDateFilterField = WtfGlobal.getLocaleText("acc.field.AgedOn");
    
    this.agedDateFilterStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name','tooltip'],
        data :[[0,WtfGlobal.getLocaleText("acc.agedPay.dueDate1-30"),WtfGlobal.getLocaleText("acc.agedPay.dueDate1-30tt")],[2,WtfGlobal.getLocaleText("acc.agedPay.dueDate0-30"),WtfGlobal.getLocaleText("acc.agedPay.dueDate0-30tt")],[1,WtfGlobal.getLocaleText("acc.agedPay.invoiceDate1-30"),WtfGlobal.getLocaleText("acc.agedPay.invoiceDate1-30tt")],[3,WtfGlobal.getLocaleText("acc.agedPay.invoiceDate0-30"),WtfGlobal.getLocaleText("acc.agedPay.invoiceDate0-30tt")]]
    });
    
    this.agedDateFilter = new Wtf.form.ComboBox({
        store: this.agedDateFilterStore,
        name:'agedDateFilter',
        displayField:'name',
        valueField:'id',
        mode: 'local',
//        value:0,
        value:this.isCustomer ? Wtf.agedReceivableDateFilter : Wtf.agedPayableDateFilter,
        triggerAction: 'all',
        selectOnFocus:true,
        tpl : new Wtf.XTemplate('<tpl for="."><div class="x-combo-list-item" Wtf:qtip="{tooltip}">{name}</div></tpl>')
    });
       
    var btnArr=[];
    var secondBtnArr=[];
     //60 & 61 Wtf.Acc_Customer_AccountStatement_ModuleId : Wtf.Acc_Vendor_AccountStatement_ModuleId,
    if(this.moduleid === 60 || this.moduleid === 61){
        this.email=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.email"),
            tooltip :WtfGlobal.getLocaleText("acc.MailWin.sendMail"),
            scope: this,
            handler:this.sendMail,
            iconCls: "accountingbase financialreport"
        });
        var cmbAccountlabel = this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectCustomer") : WtfGlobal.getLocaleText("acc.field.SelectVendor");
        btnArr.push(
            cmbAccountlabel,this.cmbAccount,
            WtfGlobal.getLocaleText("acc.ledger.accSelectTransactions"),this.cmbInvoiceType,
            this.AdvanceSearchBtn,
            this.email
            );
            
        secondBtnArr.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,
        WtfGlobal.getLocaleText("acc.common.asOf"),this.asOfDate,
            WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',
            WtfGlobal.getLocaleText("acc.agedPay.interval"),this.interval,
            '-',WtfGlobal.getLocaleText("acc.aged.NoOfIntervals"),this.noOfIntervalCombo,{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                tooltip:WtfGlobal.getLocaleText("acc.field.SelectatimeperiodtoviewcorrespondingAccountStatement"),
                iconCls:'accountingbase fetch',
                scope:this,
                handler:this.onClick
                },
                '-',this.resetBttn,
                '-', this.groupCombo, /*Group BY TRANSACTION / SORT BY DATE  */
                '-', this.expandCollpseButton,
                '-', agedDateFilterField,
                 this.agedDateFilter
          );
              
        var disclaimerBtnArr=[];
        if(this.reportid==Wtf.autoNum.SOA_CustomerAccountStatement || this.reportid==Wtf.autoNum.SOA_VendorAccountStatement){
            disclaimerBtnArr.push(WtfGlobal.getLocaleText("acc.soareport.Disclaimer.text"));
        }
        var secondtbar = new Wtf.Toolbar(secondBtnArr);
        var firsttbar = new Wtf.Toolbar(btnArr);
        var disclaimertbar = new Wtf.Toolbar(disclaimerBtnArr);
            
        this.toolbarPanel = new Wtf.Panel({
            items:[firsttbar,secondtbar,disclaimertbar]
        });
    }else{
        btnArr.push(
            WtfGlobal.getLocaleText("acc.ledger.accName"),this.cmbAccount,
            WtfGlobal.getLocaleText("acc.ledger.accSelectTransactions"),this.cmbInvoiceType,
            WtfGlobal.getLocaleText("acc.common.from"),this.startDate,
            WtfGlobal.getLocaleText("acc.common.to"),this.endDate,
            WtfGlobal.getLocaleText("acc.agedPay.interval"),this.interval,
            '-',{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                tooltip:WtfGlobal.getLocaleText("acc.field.SelectatimeperiodtoviewcorrespondingAccountStatement"),
                iconCls:'accountingbase fetch',
                scope:this,
                handler:this.onClick
            }, '-',this.resetBttn);
    }
    if(this.reportid==undefined || this.reportid==null){
        this.reportid="";
    }
    if(this.isCustomer){
        var filename = WtfGlobal.getLocaleText("acc.field.StatementofAccounts")+"_v1";
        if(this.reportid==Wtf.autoNum.SOA_CustomerAccountStatement){
            filename = "SOA - "+WtfGlobal.getLocaleText("acc.agedPay.cus")+WtfGlobal.getLocaleText("acc.field.AccountStatement")+"_v1";
        }
        this.expButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.export"),
            obj:this,
            filename:filename,
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),
              disabled :true,
            params:{
                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
//                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                interval:this.interval.getValue(),
                statementOfAccountsFlag:true,
                customerIds:this.accountID,
                isCustomerSales:true,
                name: "Customer_Account_Statement",
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                custVendorID:this.accountID,
                datefilter:this.agedDateFilter.getValue(),
                ignorezero:true,
                isdistributive:true,
                isAged:true,
                creditonly:true,
                mode:18,
                nondeleted:true,
                withinventory:true,
                reportWithoutAging:false,
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                curdate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                invoiceAmountDueFilter:this.cmbInvoiceType.value,
                duration:this.interval.getValue(),
                reportid:this.reportid
            },
            menuItem:{csv:true,subMenu:true,subMenu1:true,subMenu2:true,rowPdf:false,xls:true,pdf:((Wtf.templateflag==Wtf.BakerTilly_templateflag_pcs || Wtf.templateflag==Wtf.BakerTilly_templateflag || Wtf.templateflag==Wtf.GoldBell_templateflag )?true:false)},
            get:Wtf.autoNum.CustomerAccountStatement
        });
        this.printButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
            disabled :true,
            filename:filename,
            params:{
                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                customerIds:this.accountID,
                name: "Customer Account Statement",
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory
            },
            label:WtfGlobal.getLocaleText("acc.field.Customer_Account_Statement"),
            menuItem:{print:true},
            get:Wtf.autoNum.CustomerAccountStatement
        });
    }else{
        var filename = WtfGlobal.getLocaleText("acc.field.StatementofAccounts")+"_v1";
        if(this.reportid==Wtf.autoNum.SOA_VendorAccountStatement){
            filename = "SOA - "+WtfGlobal.getLocaleText("acc.invoice.vendor")+WtfGlobal.getLocaleText("acc.field.AccountStatement")+"_v1";
        }
        this.expButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.export"),
            obj:this,
            filename:filename,
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),
            disabled :true,
            params:{
                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
//                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                interval:this.interval.getValue(),
                statementOfAccountsFlag:true,
                vendorIds:this.accountID,
                isCustomerSales:false,
                name: "Vendor_Account_Statement",
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                custVendorID:this.accountID,
                datefilter:this.agedDateFilter.getValue(),
                ignorezero:true,
                isdistributive:true,
                isAged:true,
                creditonly:true,
                mode:18,
                nondeleted:true,
                withinventory:true,
                reportWithoutAging:false,
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                curdate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                invoiceAmountDueFilter:this.cmbInvoiceType.value,
                duration:this.interval.getValue(),
                reportid:this.reportid
            },
            menuItem:{csv:true,subMenu:true,subMenu1:true,subMenu2:true,rowPdf:false,xls:true,pdf:((Wtf.templateflag==Wtf.BakerTilly_templateflag_pcs || Wtf.templateflag==Wtf.BakerTilly_templateflag  || Wtf.templateflag==Wtf.GoldBell_templateflag)?true:false)},
            get:Wtf.autoNum.VendorAccountStatement
        });
        this.printButton=new Wtf.exportButton({
            obj:this,
            filename:filename,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
            disabled :true,
            params:{
                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                vendorIds:this.accountID,
                name: "Vendor Account Statement",
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory
            },
            label:WtfGlobal.getLocaleText("acc.field.Vendor_Account_Statement"),
            menuItem:{print:true},
            get:Wtf.autoNum.VendorAccountStatement
        });
    }
  
    this.reportPanel = new Wtf.account.StatementOfAccountsListPanelView({
        border : false,
        layout: 'fit',
        isCustomer:this.isCustomer,
        helpmodeid: 16,
        label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //"Invoice",
        isOrder:false,
        closable: false,
        iconCls:'accountingbase invoicelist',
        agedDateFilter:this.agedDateFilter.getValue()
    });
    if(this.isCustomer){
        this.singleRowPrint=new Wtf.exportButton({
            obj:this,
            id:"printSingleRecord"+config.helpmodeid+config.id,
            iconCls: 'pwnd printButtonIcon',
            text: WtfGlobal.getLocaleText("acc.rem.236"),
            params:{
                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                customerIds:this.accountID,
                interval:this.interval.getValue(),
                statementOfAccountsFlag:true,
                isCustomerSales:true,
                name: "Customer_Account_Statement",
                custVendorID:this.accountID,
                datefilter:this.agedDateFilter.getValue(),
                ignorezero:true,
                isdistributive:true,
                isAged:true,
                creditonly:true,
                mode:18,
                nondeleted:true,
                withinventory:true,
                reportWithoutAging:false,
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                curdate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                invoiceAmountDueFilter:this.cmbInvoiceType.value,
                duration:this.interval.getValue(),
                reportid:this.reportid
            },
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
            hidden:this.isRequisition || this.isSalesCommissionStmt,
            menuItem:{
                rowPrint:(this.isSalesCommissionStmt)?false:true
                },
            //         get:tranType,
            moduleid:this.moduleid//Wtf.Acc_Customer_AccountStatement_ModuleId,//
        });
    } else {
        this.singleRowPrint=new Wtf.exportButton({
            obj:this,
            id:"printSingleRecord"+config.helpmodeid+config.id,
            iconCls: 'pwnd printButtonIcon',
            text: WtfGlobal.getLocaleText("acc.rem.236"),
            params:{
                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                vendorIds:this.accountID,
                interval:this.interval.getValue(),
                statementOfAccountsFlag:true,
                isCustomerSales:false,
                name: "Vendor_Account_Statement",
                custVendorID:this.accountID,
                datefilter:this.agedDateFilter.getValue(),
                ignorezero:true,
                isdistributive:true,
                isAged:true,
                creditonly:true,
                mode:18,
                nondeleted:true,
                withinventory:true,
                reportWithoutAging:false,
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                curdate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                invoiceAmountDueFilter:this.cmbInvoiceType.value,
                duration:this.interval.getValue(),
                reportid:this.reportid
            },
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
            hidden:this.isRequisition || this.isSalesCommissionStmt,
            menuItem:{
                rowPrint:(this.isSalesCommissionStmt)?false:true
                },
            //         get:tranType,
            moduleid:this.moduleid
        });
    }
    //ERP-29162 : SOA - Need to remove Email button if provided Document designer template
    if(this.singleRowPrint.printMenu.items.length == 1 && this.singleRowPrint.printMenu.items.keys[0] == -99){
        this.email.show();
    } else{
        this.email.hide();
    }
    
    this.southPanel = new Wtf.Panel({
                border: true,
                region: 'south',
                layout: 'fit',
                height:130 ,
                plugins : new Wtf.ux.collapsedPanelTitlePlugin(),
                collapsibletitle : "Ageing Details",
                title : WtfGlobal.getLocaleText("acc.field.AgeingDetails"),
                collapsible: true,
                collapsed: true,
                items : [this.reportPanel]
               
            });            
    btnArr.push("-",this.expButton);
    btnArr.push("-",this.printButton,'-',this.typeEditorForCheque);
    btnArr.push(this.singleRowPrint);
    
    Wtf.apply(this,{
        items:[{
            layout:'border',
            border:false,
            scope:this,
            items:[this.objsearchComponent,{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid
            },this.southPanel],//
            tbar:(this.moduleid === 60 || this.moduleid === 61) ? this.toolbarPanel : btnArr
        }]
    },config)
    this.expButton.on("click", function() {
        this.expButton.setParams({
            searchJson: this.searchJson == undefined ? "" : this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit == undefined ? "" : this.filterConjuctionCrit,
            moduleid: this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId,
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
            customerIds:this.cmbAccount.getValue(),
            custVendorID:this.cmbAccount.getValue(),
            vendorIds: this.cmbAccount.getValue(),
            invoiceAmountDueFilter:this.cmbInvoiceType.value
            
        });
    }, this);
    
    this.singleRowPrint.on("click", function() {
        var isPostDatedCheque;
        var index=this.typeEditorForCheque.getValue();
        if(index==1){
            isPostDatedCheque=true;
        }else{
            isPostDatedCheque=false;
        }
        this.singleRowPrint.setParams({
            searchJson: this.searchJson == undefined ? "" : this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit == undefined ? "" : this.filterConjuctionCrit,
            moduleid: this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId,
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            curdate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            datefilter:this.agedDateFilter.getValue(),//this is used for Fetching aged data based on invoice date
            customerIds:this.accountID,
            custVendorID:this.accountID,
            invoiceType:this.cmbInvoiceType.getValue(),
            name: "Customer Account Statement",
            isPostDatedCheque:isPostDatedCheque,
            invoiceAmountDueFilter:this.cmbInvoiceType.value,
            withoutinventory: Wtf.account.companyAccountPref.withoutinventory
        });
    }, this);
    Wtf.account.Ledger.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    this.grid.addEvents({
        'savemystate' : true
    });
    this.getMyConfig();
    this.grid.on('savemystate',this.saveMyStateHandler,this)
    this.grid.on("columnmove", this.saveMyState, this);
    this.grid.on("columnresize", this.saveMyState, this);
    this.grid.colModel.on("hiddenchange", function(colModel) {
        this.grid.reconfigure(this.grid.store, colModel);
        this.grid.fireEvent("savemystate", this.grid, this.grid.getState());
    }, this);
    this.grid.colModel.on("widthchange", this.saveMyState, this);
    this.grid.colModel.on("configchanged", this.saveMyState, this);
    this.grid.on('cellclick',this.onCellClick, this);
     this.typeEditorForCheque.on('select',this.onClick,this);
//    this.custStore.on("load", function(){
////        var allRec = new Wtf.data.Record({
////            accountname : "All",
////            accountid: ""
////        });
////        if(this.custStore.getAt(0)!="")
////            this.custStore.insert(0,allRec);
//    },this);
//    this.custStore.load();
//    this.on("activate", function() {     //Default Data Load---------------------------------
//        this.LedgerStore.load();
//        if (this.LedgerStore.getCount() == 0) {    //ERP-32676 To Check access log for handled exception
//            this.expButton.disable();              //removed default data load bcoz date values are not set
//            this.printButton.disable();            //to the start, end and asof date fields at the time of grid load
//            this.singleRowPrint.disable();         //so it throws date parse exception to handle it default
//        }                                          //data load is removed.
//    }, this);--------------------------------------------------------------------------------
 }

Wtf.extend( Wtf.account.cvLedger,Wtf.Panel,{
    
    onClick:function(){
        this.accountID=this.cmbAccount.getValue();  
        this.fetchLedger();
    },
    sendMail : function(){
        var selectedRec = null;
        var accountid=this.cmbAccount.getValue();
        if(accountid == undefined || accountid == "" || accountid =="All"){
            WtfComMsgBox(119,2);
            return;
        } 
        if(accountid.split(",").length > 1){
            WtfComMsgBox(120,2);
            return;
        }
        this.grid.getSelectionModel().selectAll();	
        selectedRec = this.grid.getSelectionModel().getSelections();
        var filterParams={};
        filterParams.stdate=WtfGlobal.convertToGenericStartDate(this.getDates(true));
        filterParams.interval=this.interval.getValue();
        filterParams.statementOfAccountsFlag=true;
        filterParams.vendorIds=this.accountID;
        filterParams.customerIds=this.accountID;
        filterParams.isCustomerSales=false;
        filterParams.name= this.isCustomer ? Wtf.Email_Module_Name_Customer_Account_Statement : Wtf.Email_Module_Name_Vendor_Account_Statement;
        filterParams.withoutinventory= Wtf.account.companyAccountPref.withoutinventory;
        filterParams.custVendorID=this.accountID;
        filterParams.datefilter=this.agedDateFilter.getValue();
        filterParams.ignorezero=true;
        filterParams.isdistributive=true;
        filterParams.isAged=true;
        filterParams.creditonly=true;
        filterParams.mode=18;
        filterParams.nondeleted=true;
        filterParams.withinventory=true;
        filterParams.reportWithoutAging=false;
        filterParams.startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        filterParams.curdate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        filterParams.asofdate=WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue());
        filterParams.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        filterParams.invoiceAmountDueFilter=this.cmbInvoiceType.value;
        filterParams.duration=this.interval.getValue();
        filterParams.noOfInterval=this.noOfIntervalCombo.getValue();
        filterParams.accountname=this.cmbAccount.getRawValue();
        filterParams.moduleid=this.moduleid;
        var mode = 18;
        var configstr =""; //this.getTemplateConfig();
        var fileType = 'pdf';
        var get = '';
        var pdfStore = "";//this.filPdfStore();
        var gridConfig =""; //this.genJsonForPdf(pdfStore);
        var deleted = false;
        var nondeleted = true;
        var gridParams = "";//this.grid.getStore().baseParams;
        var isCustomer = (this.isCustomer)?true:false;
        var label = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Customer_Account_Statement") : WtfGlobal.getLocaleText("acc.field.Vendor_Account_Statement");
        var fileName = label;
        callEmailForMultipleRecords('newwinid', selectedRec, label, mode, isCustomer, configstr, fileName, fileType, get, gridConfig,deleted,nondeleted,filterParams);
        this.grid.getSelectionModel().clearSelections();
    },
   fetchLedger:function(){
     var sDate=this.startDate.getValue();
     var eDate=this.endDate.getValue();
     var asOfDate=this.asOfDate.getValue();
     if(sDate!='' && eDate!='' && asOfDate!=''){
            
        var isPostDatedCheque=null;
        var index=this.typeEditorForCheque.getValue();
        if(index==1){
               isPostDatedCheque=true;
        }else{
               isPostDatedCheque=false;
        }
            
        if(sDate>eDate){
            WtfComMsgBox(1,2);
            return;
        }
        if(this.accountID == "" || (this.accountID&&this.accountID.length>0)){
            var idParamName = this.isCustomer ? "customerid":"vendorid";
            if(this.isCustomer){
                this.LedgerStore.load({
                    params:{
                        customerIds:this.accountID,
                        stdate:WtfGlobal.convertToGenericStartDate(sDate),
                        enddate:WtfGlobal.convertToGenericEndDate(eDate),
                        asofdate:WtfGlobal.convertToGenericEndDate(asOfDate),//this is used for Fetching aged data based on asofdate
                        withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                        invoiceAmountDueFilter:this.cmbInvoiceType.value,
                        isPostDatedCheque:isPostDatedCheque,
//                        companyids:companyids,//
                        limit : this.pP.combo!=undefined?this.pP.combo.value:30,
                        start:0,
                        datefilter:this.agedDateFilter.getValue(),
                        interval:this.interval.getValue(),
                        noOfInterval:this.noOfIntervalCombo.getValue(),
//                        archieve:0,//	
//                        consolidateFlag:false,//
//                        deleted:false,//
//                        gcurrencyid:1,//
//                        isOutstanding:false,//
//                        isfavourite:false,//
//                        nondeleted:false,//
                        startdate:WtfGlobal.convertToGenericStartDate(sDate),//
//                        userid:loginid,
                        idparamname:idParamName
                    }
                });
                this.expButton.setParams({
                    stdate:WtfGlobal.convertToGenericStartDate(sDate),
                    startdate:WtfGlobal.convertToGenericStartDate(sDate),                    
                    enddate:WtfGlobal.convertToGenericEndDate(eDate),
                    curdate:WtfGlobal.convertToGenericEndDate(eDate),
                    asofdate:WtfGlobal.convertToGenericEndDate(asOfDate),//this is used for Fetching aged data based on asofdate
                    datefilter:this.agedDateFilter.getValue(),//this is used for Fetching aged data based on invoice date
                    customerIds:this.accountID,
                    custVendorID:this.accountID,
                    name: "Customer_Account_Statement",
                    isPostDatedCheque:isPostDatedCheque,
                    invoiceAmountDueFilter:this.cmbInvoiceType.value,
                    withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                    interval:this.interval.getValue(),
                    duration:this.interval.getValue()
                });
                this.printButton.setParams({
                    stdate:WtfGlobal.convertToGenericStartDate(sDate),
                    startdate:WtfGlobal.convertToGenericStartDate(sDate),
                    enddate:WtfGlobal.convertToGenericEndDate(eDate),
                    curdate:WtfGlobal.convertToGenericEndDate(eDate),
                    asofdate:WtfGlobal.convertToGenericEndDate(asOfDate),//this is used for Fetching aged data based on asofdate
                    datefilter:this.agedDateFilter.getValue(),//this is used for Fetching aged data based on invoice date
                    customerIds:this.accountID,
                    custVendorID:this.accountID,
                    invoiceType:this.cmbInvoiceType.getValue(),
                    name: "Customer Account Statement",
                    isPostDatedCheque:isPostDatedCheque,
                    invoiceAmountDueFilter:this.cmbInvoiceType.value,
                    withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                    interval:this.interval.getValue(),
                    duration:this.interval.getValue()
                });
                this.singleRowPrint.setParams({
                    stdate:WtfGlobal.convertToGenericStartDate(sDate),
                    startdate:WtfGlobal.convertToGenericStartDate(sDate),
                    enddate:WtfGlobal.convertToGenericEndDate(eDate),
                    curdate:WtfGlobal.convertToGenericEndDate(eDate),
                    asofdate:WtfGlobal.convertToGenericEndDate(asOfDate),//this is used for Fetching aged data based on asofdate
                    datefilter:this.agedDateFilter.getValue(),//this is used for Fetching aged data based on invoice date
                    customerIds:this.accountID,
                    custVendorID:this.accountID,
                    invoiceType:this.cmbInvoiceType.getValue(),
                    name: "Customer Account Statement",
                    isPostDatedCheque:isPostDatedCheque,
                    invoiceAmountDueFilter:this.cmbInvoiceType.value,
                    withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                    interval:this.interval.getValue(),
                    duration:this.interval.getValue()
                });
            }else{
                this.LedgerStore.load({
                    params:{
                        vendorIds:this.accountID,
                        stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                        asofdate:WtfGlobal.convertToGenericEndDate(asOfDate),//this is used for Fetching aged data based on asofdate
                        withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                        invoiceAmountDueFilter:this.cmbInvoiceType.value,
                        isPostDatedCheque:isPostDatedCheque,
                        startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),//
                        idparamname:idParamName,
                        datefilter:this.agedDateFilter.getValue(),
                        interval:this.interval.getValue(),
                        noOfInterval:this.noOfIntervalCombo.getValue(),
                        limit : this.pP.combo!=undefined?this.pP.combo.value:30,
                        start:0
                    }
                });
                this.expButton.setParams({
                    stdate:WtfGlobal.convertToGenericStartDate(sDate),
                    startdate:WtfGlobal.convertToGenericStartDate(sDate),
                    enddate:WtfGlobal.convertToGenericEndDate(eDate),
                    curdate:WtfGlobal.convertToGenericEndDate(eDate),
                    asofdate:WtfGlobal.convertToGenericEndDate(asOfDate),//this is used for Fetching aged data based on asofdate
                    datefilter:this.agedDateFilter.getValue(),//this is used for Fetching aged data based on invoice date
                    vendorIds:this.accountID,
                    custVendorID:this.accountID,
                    name: "Vendor_Account_Statement",
                    isPostDatedCheque:isPostDatedCheque,
                    invoiceAmountDueFilter:this.cmbInvoiceType.value,
                    withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                    interval:this.interval.getValue(),
                    duration:this.interval.getValue()
                });
                this.printButton.setParams({
                    stdate:WtfGlobal.convertToGenericStartDate(sDate),
                    startdate:WtfGlobal.convertToGenericStartDate(sDate),
                    enddate:WtfGlobal.convertToGenericEndDate(eDate),
                    curdate:WtfGlobal.convertToGenericEndDate(eDate),
                    asofdate:WtfGlobal.convertToGenericEndDate(asOfDate),//this is used for Fetching aged data based on asofdate
                    datefilter:this.agedDateFilter.getValue(),//this is used for Fetching aged data based on invoice date
                    vendorIds:this.accountID,
                    custVendorID:this.accountID,
                    name: "Vendor Account Statement",
                    isPostDatedCheque:isPostDatedCheque,
                    invoiceType:this.cmbInvoiceType.getValue(),
                    invoiceAmountDueFilter:this.cmbInvoiceType.value,
                    withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                    interval:this.interval.getValue(),
                    duration:this.interval.getValue()
                });
                this.singleRowPrint.setParams({
                    stdate:WtfGlobal.convertToGenericStartDate(sDate),
                    startdate:WtfGlobal.convertToGenericStartDate(sDate),
                    enddate:WtfGlobal.convertToGenericEndDate(eDate),
                    curdate:WtfGlobal.convertToGenericEndDate(eDate),
                    asofdate:WtfGlobal.convertToGenericEndDate(asOfDate),//this is used for Fetching aged data based on asofdate
                    datefilter:this.agedDateFilter.getValue(),//this is used for Fetching aged data based on invoice date
                    vendorIds:this.accountID,
                    custVendorID:this.accountID,
                    name: "Vendor Account Statement",
                    isPostDatedCheque:isPostDatedCheque,
                    invoiceType:this.cmbInvoiceType.getValue(),
                    invoiceAmountDueFilter:this.cmbInvoiceType.value,
                    withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                    interval:this.interval.getValue(),
                    duration:this.interval.getValue()
                });
            }
        }
       } else {
            if (sDate == '') {
                WtfComMsgBox(124, 2);
            } else if (eDate == '') {
                WtfComMsgBox(125, 2);
            } else if (asOfDate == '') {
                WtfComMsgBox(126, 2);
            }
        }
    },

    onRender:function(config){
        Wtf.account.Ledger.superclass.onRender.call(this,config);
   },

    onCellClick:function(g,i,j,e){
       var adgingGrid=this.reportPanel.grid;
       var selectedRec = this.LedgerStore.getAt(i);
       var accId=selectedRec.data['accId'];
       var invoiceId=selectedRec.data['invoiceId'];
       var interval=this.interval.getValue();
        adgingGrid.store.load({
            params:{
            custVendorID:accId,
            datefilter:this.agedDateFilter.getValue(),
            ignorezero:true,
            isdistributive:true,
            statementOfAccountsFlag:true,
            isAged:true,
            creditonly:true,
            mode:18,
            nondeleted:true,
            withinventory:true,
            reportWithoutAging:false,
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            curdate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            duration:interval,
            noOfInterval:this.noOfIntervalCombo.getValue(),
            start:0,
            limit:30
        }});
    
        this.columnModelForAging = adgingGrid.getColumnModel();
    
        if(this.agedDateFilter.getValue() == Wtf.agedDueDate0to30Filter || this.agedDateFilter.getValue() == Wtf.agedInvoiceDate0to30Filter)
        {
            var ColumnIndex = adgingGrid.getColumnModel().findColumnIndex("amountdue2");
            this.columnModelForAging.setColumnHeader(ColumnIndex,("0-")+ this.interval.getValue()+" "+WtfGlobal.getLocaleText("acc.agedPay.days"));
        }else{
            var ColumnIndex = adgingGrid.getColumnModel().findColumnIndex("amountdue2");
            this.columnModelForAging.setColumnHeader(ColumnIndex,("1-")+ this.interval.getValue()+" "+WtfGlobal.getLocaleText("acc.agedPay.days"));
        }
        
        for (var noOfInterval = 1; noOfInterval <=9; noOfInterval++) {  // loop will run for (this.noOfIntervalCombo -1) Times as 1-30 is already added
            var ColumnIndex = adgingGrid.getColumnModel().findColumnIndex("amountdue"+ (noOfInterval + 2));
            if (noOfInterval < this.noOfIntervalCombo.getValue()) {
                this.columnModelForAging.setHidden(ColumnIndex,false);
                if (noOfInterval == (this.noOfIntervalCombo.getValue() - 1)) { // To append ">" For Last Column
                    this.columnModelForAging.setColumnHeader(ColumnIndex, ">" + (this.interval.getValue() * (noOfInterval)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + " ");
                } else { // For Column like 31-60 , 61-90 ...
                    this.columnModelForAging.setColumnHeader(ColumnIndex, ((this.interval.getValue() * noOfInterval) + 1) + "-" + (this.interval.getValue() * (noOfInterval + 1)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + " ");
                }
            } else {
                this.columnModelForAging.setHidden(ColumnIndex,true);
            }

        }
        
        adgingGrid.getView().refresh(true);
       
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="jeEntryNumber"){
            var accid=this.LedgerStore.getAt(i).data['jeId'];
            var jeentrydate= new Date(this.LedgerStore.getAt(i).data['jeEntryDate']);
            var jestartdate= new Date(this.LedgerStore.getAt(i).data['jeEntryDate']);
            var jeenddate= new Date(this.LedgerStore.getAt(i).data['jeEntryDate']);
                
//           jestartdate = new Date(jestartdate.setDate(jeentrydate.getDate()-1));     
//           jeenddate = new Date(jeenddate.setDate(jeentrydate.getDate()+1));
            jestartdate = this.startDate.getValue();
            jeenddate = this.endDate.getValue();
            this.fireEvent('journalentry',accid,true,undefined,undefined,undefined,undefined,jestartdate,jeenddate);
        }
        if(header=="invoiceNumber"){
            var formrec = this.LedgerStore.getAt(i);
            var type=formrec.data['type'];
            viewTransactionTemplate(type, formrec);
        }
    },

    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
    saveMyState: function(){
        this.grid.fireEvent("savemystate", this.grid, this.grid.getState());
    },
    
    getMyConfig : function(){
        if(this.reportid==Wtf.autoNum.SOA_CustomerAccountStatement || this.reportid==Wtf.autoNum.SOA_VendorAccountStatement) {
            WtfGlobal.getGridConfig (this.grid, this.reportid, false, false);
        } else {
            WtfGlobal.getGridConfig (this.grid, this.moduleid, false, false);
        }
    },
    
    saveMyStateHandler: function(grid,state){
        if(this.reportid==Wtf.autoNum.SOA_CustomerAccountStatement || this.reportid==Wtf.autoNum.SOA_VendorAccountStatement) {
            WtfGlobal.saveGridStateHandler(this, grid, state, this.reportid, grid.gridConfigId, false);
        } else {
            WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
        }
    },
    
    handleResetClickNew:function()
    {
       this.startDate.reset();
       this.endDate.reset();
       this.asOfDate.reset();
       this.cmbAccount.reset();
       this.cmbAccount.setValue('All');
       this.interval.reset();
       this.groupCombo.reset();
       this.typeEditorForCheque.reset();
       this.cmbInvoiceType.reset();
       this.LedgerStore.removeAll();
       this.expButton.disable();
       this.printButton.disable();
       this.singleRowPrint.disable();
       if (this.LedgerStore.getTotalCount.length == 0) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.field.soareport.selectdate'));
            this.grid.getView().refresh();
            this.pagingToolbar.pageSize = 30;
            this.pP.combo.setValue(this.pagingToolbar.pageSize);
            }
    },
    
    checkDates: function(dateObj, newVal, oldVal) {
        if (this.startDate.getValue() == '' || this.endDate.getValue() == '' || this.asOfDate.getValue() == '') {
            if (this.expButton)
                this.expButton.disable();
            if (this.printButton)
                this.printButton.disable();
            if (this.singleRowPrint)
                this.singleRowPrint.disable();
        }
        if (this.startDate.getValue() != '' && this.endDate.getValue() != '' && this.asOfDate.getValue() != '') {
            var checkDate = false;
            if (this.asOfDate.getValue() < this.startDate.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"), WtfGlobal.getLocaleText("acc.agedPay.AsofdateshouldbeinrangeofFromDateandToDate")], 2);  //"As of date should not be less than From Date."
                dateObj.setValue(oldVal);
//                checkDate = false;
                return;
            } else {
                checkDate = true;
            }
            
            if (this.asOfDate.getValue() < Wtf.account.companyAccountPref.bbfrom) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"), WtfGlobal.getLocaleText("acc.agedPay.AsofdateshouldbebeforeBookBeginningDate")], 2);  //"From date should not be greater than To Date."
                dateObj.setValue(oldVal);
//                checkDate = false;
                return;
            } else {
                checkDate = true;
            }
            
            if (this.endDate.getValue() < this.startDate.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"), WtfGlobal.getLocaleText("acc.agedPay.FromdateshouldnotbegreaterthanToDate")], 2);  //"From date should not be greater than To Date."
                dateObj.setValue(oldVal);
//                checkDate = false;
                return;
            } else {
                checkDate = true;
            }
            
            if (this.endDate.getValue() < Wtf.account.companyAccountPref.bbfrom) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"), WtfGlobal.getLocaleText("acc.agedPay.TodateshouldbebeforeBookBeginningDate")], 2);  //"From date should not be greater than To Date."
                dateObj.setValue(oldVal);
//                checkDate = false;
                return;
            } else {
                checkDate = true;
            }
            if (checkDate) {
                if (this.expButton)
                    this.expButton.enable();
                if (this.printButton)
                    this.printButton.enable();
                if (this.singleRowPrint)
                    this.singleRowPrint.enable();
            }
        } 
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
        this.LedgerStore.baseParams = {
            pendingApproval: this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.LedgerStore.load({
            params: {
//                ss: this.quickPanelSearch.getValue(), 
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30
            }
        });
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.LedgerStore.baseParams = {
            pendingApproval: this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.LedgerStore.load({
            params: {
//                ss: this.quickPanelSearch.getValue(), 
                start: 0,
                limit: this.pP.combo != undefined ? this.pP.combo.value : 30
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    
      groupComboChange: function (combo,newval,oldval) {
       if (combo.getValue() == 1) {                                 /*SORT BY TRANSACTION DATE*/
          this.grid.getStore().groupBy(['accCode'],false);
       } else{                                                      /*GROUP BY TRANSACTION DATE*/
          this.grid.getStore().groupBy(['accCode', 'type'],false);
       }
         this.fetchLedger();
    },
    /*
     * ExpandCollapse button handler
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
});
//Statement Of Accounts Adging View
Wtf.account.StatementOfAccountsListPanelView=function(config){

    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.interval=30;
//    this.id=config.id;
    this.isCustomer=config.isCustomer;
    this.agedDateFilterForGrid =  config.agedDateFilter;
    
    this.GridRec = Wtf.data.Record.create ([
        {
            name:'billid'
        },{
            name:'journalentryid'
        },{
            name:'entryno'
        },{
            name:'billno'
        },{
            name:'date', type:'date'
        },{
            name:'duedate', type:'date'
        },{
            name:'personname'
        },{
            name:'personemail'
        },{
            name:'personid'
        },{
            name: 'currencysymbol'
        },{
            name: 'currencyname'
        },{
            name: 'currencyid'
        },{
            name:'amountdueinbase'
        },{
             name:'amountdue'
        },{
            name:'amountdue1'
        },{
            name:'amountdue2'
        },{
            name:'amountdue3'
        },{
            name:'amountdue4'
        },{
            name:'amountdue5'
        },{
            name:'amountdue6'
        },{
            name:'amountdue7'
        },{
            name:'amountdue8'
        },{
            name:'amountdue9'
        },{
            name:'amountdue10'
        },{
            name:'amountdue11'
        },{
            name:'accruedbalance'
        },{
            name:'total'
        },{
            name:'amountdueinbase1'
        },{
            name:'amountdueinbase2'
        },{
            name:'amountdueinbase3'
        },{
            name:'amountdueinbase4'
        },{
            name:'total'
        },{
            name:'memo'
        },{
            name:'totalinbase'
        },{
            name: 'currencysymbol'
        },{
            name: 'termname'
        },{
            name: 'withoutinventory', type: 'boolean'
        }, {
            name: 'isLeaseFixedAsset'
        }
    ]);
    this.StoreUrl = this.isCustomer?"ACCInvoiceCMN/getCustomerAgedReceivable.do":"ACCGoodsReceiptCMN/getVendorAgedPayable.do";
//    this.StoreUrl = this.isCustomer?"ACCReports/getCustomerLedgerAdging.do":"ACCGoodsReceiptCMN/getVendorAgedPayable.do";
    
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.GridRec)
    });

    this.colModelData = [];
    this.colModelData.push(new Wtf.grid.RowNumberer(),{
            header:this.isCustomer ? WtfGlobal.getLocaleText("acc.agedPay.gridCustomer/AccName") : WtfGlobal.getLocaleText("acc.agedPay.gridVendor/AccName"),
            dataIndex:'personname',
            pdfwidth:75,
            renderer:WtfGlobal.linkDeletedRenderer
        },
        {
            header:WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),  //"Currency Symbol",
            dataIndex:'currencyname',
            align:'center',
             pdfwidth:120
            //renderer:WtfGlobal.deletedRenderer
         },
         {
            header:WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),  //"Currency Symbol",
            dataIndex:'amountdue1',
            align:'center',
            pdfwidth:120,
//            hidden : this.isCustomer ? (Wtf.agedReceivableDateFilter == 2 ? true : false) : (Wtf.agedPayableDateFilter == 2 ? true : false) ,
//            hidden : this.agedDateFilterForGrid == 2 ? true : false,
            renderer:WtfGlobal.withoutRateCurrencySymbol
         },
         {
//            header: this.isCustomer ? (Wtf.agedReceivableDateFilter == 2 ? ("0-")+ this.interval+" "+WtfGlobal.getLocaleText("acc.agedPay.days") : ("1-")+ this.interval+" "+WtfGlobal.getLocaleText("acc.agedPay.days")) : (Wtf.agedPayableDateFilter == 2 ? ("0-")+ this.interval+" "+WtfGlobal.getLocaleText("acc.agedPay.days") : ("1-")+ this.interval+" "+WtfGlobal.getLocaleText("acc.agedPay.days")),
            header: (this.agedDateFilterForGrid == Wtf.agedDueDate0to30Filter || this.agedDateFilterForGrid == Wtf.agedInvoiceDate0to30Filter) ? ("0-")+ this.interval+" "+WtfGlobal.getLocaleText("acc.agedPay.days") : ("1-")+ this.interval+" "+WtfGlobal.getLocaleText("acc.agedPay.days"),
            dataIndex:'amountdue2',
             pdfwidth:120,
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol//,
            
         });
         
        this.totalNoOfColumns = 9  // Use For FOR Loop Iteration.  keeping Value as 9 because  amountdue1 and amountdue2 is already push in ColModelData.
        this.calculateDataIndex = 2 // we have already added 2 columns in column model current and 1-30 so keeping this value as 2
   
        for (var i = 1; i <=this.totalNoOfColumns; i++) {
            this.colModelData.push({
                header: (((this.interval * i) + 1) + "-") + (this.interval * (i + 1)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days"),
                dataIndex: 'amountdue' + (i + this.calculateDataIndex),
                pdfwidth: 120,
                align: 'right',
                renderer: WtfGlobal.withoutRateCurrencySymbol//,
            });
        }
        this.colModelData.push({
            header:'<b>'+WtfGlobal.getLocaleText("acc.common.total")+'</b>',
            align:'right',
            pdfwidth:150,
            dataIndex:"total",
            renderer:WtfGlobal.withoutRateCurrencySymbol//,
          }
          ,{
            header:WtfGlobal.getLocaleText("acc.common.total") + WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Memo",
            hidden : !this.isSummary,
            align:'right',
            pdfwidth:150,
            dataIndex:"totalinbase",
            summaryType:'sum',
            hidecurrency : true,
            summaryRenderer: function(value,m,rec){
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value)
                return '<b>'+retVal+'</b>';
            },
            renderer:WtfGlobal.currencyRenderer
        });
    this.cm = new Wtf.grid.ColumnModel(this.colModelData);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
//        id:this.id,
        border:false,
        layout:'fit',
        autoScroll : true,
        viewConfig:{emptyText:WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.")},
//        forceFit:true,
        cm:this.cm
    });
    this.grid.flag = 0;

    Wtf.apply(this,{
        border:false,
        layout : "fit",
        bodyStyle : "background-color:#ffffff;padding-right:10px;",
        items:[this.grid]
    });
   
    Wtf.account.StatementOfAccountsListPanelView.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.StatementOfAccountsListPanelView,Wtf.Panel,{
  
  hideLoading:function(){Wtf.MessageBox.hide();}
});
