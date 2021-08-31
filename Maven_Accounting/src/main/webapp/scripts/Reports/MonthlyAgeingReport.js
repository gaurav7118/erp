

function callMonthlyAgedRecievableSummary(withinventory,custVendorID){
    var MonthlyAgedRecievablepanel=Wtf.getCmp("MonthlyAgedRecievableSummary");
    if(MonthlyAgedRecievablepanel==null){
        callMonthlyAgedRecievable();
        MonthlyAgedRecievablepanel = new Wtf.account.MonthlyAgedDetail({
            id: 'MonthlyAgedRecievableSummary',
            border: false,
            helpmodeid:93,
            isSummary:true,
            withinventory:withinventory,
            custVendorID:custVendorID,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: WtfGlobal.getLocaleText("acc.agedPay.summaryView"),  //'Summary View',
            tabTip:WtfGlobal.getLocaleText("acc.rem.182"),
            receivable:true
        });
        Wtf.getCmp('mainMonthlyAgedRecievable').add(MonthlyAgedRecievablepanel);
    }
    Wtf.getCmp('mainMonthlyAgedRecievable').setActiveTab(MonthlyAgedRecievablepanel);
    Wtf.getCmp('mainMonthlyAgedRecievable').doLayout();
}

function callMonthlyAgedRecievableReport(withinventory,custVendorID){
    var MonthlyAgedRecievable=Wtf.getCmp("MonthlyAgedRecievable");
    if(MonthlyAgedRecievable==null){
        callMonthlyAgedRecievable();
        MonthlyAgedRecievable = new Wtf.account.MonthlyAgedDetail({
            id: 'MonthlyAgedRecievable',
            border: false,
            helpmodeid:94,
            withinventory:withinventory,
            custVendorID:custVendorID,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: WtfGlobal.getLocaleText("acc.agedPay.reportView"),  //'Report View',
            tabTip:WtfGlobal.getLocaleText("acc.rem.183"),
            receivable:true
        });
        Wtf.getCmp('mainMonthlyAgedRecievable').add(MonthlyAgedRecievable);
        MonthlyAgedRecievable.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('mainMonthlyAgedRecievable').doLayout();
}

function callMonthlyAgedRecievableDynamicLoad(withinventory,custVendorID){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.agedreceivable, Wtf.Perm.agedreceivable.viewagedreceivable)) {
        var panel = Wtf.getCmp("mainMonthlyAgedRecievable");
        if(panel==null){
            panel = new Wtf.TabPanel({
                title:WtfGlobal.getLocaleText("acc.field.monthlyagingcustomerreport"),
                tabTip:WtfGlobal.getLocaleText("acc.field.viewmonthlyagingcustomerreport"),   //'View Monthhly Aged Receivable',
                withinventory:withinventory,
                custVendorID:custVendorID,
                id:'mainMonthlyAgedRecievable',
                closable:true,
                border:false,
                iconCls:'accountingbase agedrecievable',
                activeTab:0
            });
            Wtf.getCmp('as').add(panel);
            callMonthlyAgedRecievableSummary(withinventory,custVendorID);
            callMonthlyAgedRecievableReport(withinventory,custVendorID);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.wtfTrans.agedrr"));
}
//**************************************************************************************************************
function callMonthlyAgedPayableSummary(withinventory,custVendorID){
    var MonthlyAgedPayablepanel=Wtf.getCmp("MonthlyAgedPayableSummary");
    if(MonthlyAgedPayablepanel==null){
        callMonthlyAgedPayable();
        MonthlyAgedPayablepanel = new Wtf.account.MonthlyAgedDetail({
            id: 'MonthlyAgedPayableSummary',
            border: false,
            helpmodeid:28,
            isSummary:true,
            withinventory:withinventory,
            custVendorID:custVendorID,
            layout: 'fit',
            iconCls: 'accountingbase agedPayable',
            title: WtfGlobal.getLocaleText("acc.agedPay.summaryView"),  //'Summary View',
            tabTip:WtfGlobal.getLocaleText("acc.rem.180"),
            receivable:false
        });
        Wtf.getCmp('mainMonthlyAgedPayable').add(MonthlyAgedPayablepanel);
    }
    Wtf.getCmp('mainMonthlyAgedPayable').setActiveTab(MonthlyAgedPayablepanel);
    Wtf.getCmp('mainMonthlyAgedPayable').doLayout();
}

function callMonthlyAgedPayableReport(withinventory,custVendorID){
    var MonthlyAgedPayable=Wtf.getCmp("MonthlyAgedPayable");
    if(MonthlyAgedPayable==null){
        callMonthlyAgedPayable();
        MonthlyAgedPayable = new Wtf.account.MonthlyAgedDetail({
            id: 'MonthlyAgedPayable',
            border: false,
            helpmodeid:83,
            withinventory:withinventory,
            custVendorID:custVendorID,
            layout: 'fit',
            iconCls: 'accountingbase agedPayable',
            title: WtfGlobal.getLocaleText("acc.agedPay.reportView"),  //'Report View',
            tabTip:WtfGlobal.getLocaleText("acc.rem.181"),
            receivable:false
        });
        Wtf.getCmp('mainMonthlyAgedPayable').add(MonthlyAgedPayable);
        MonthlyAgedPayable.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('mainMonthlyAgedPayable').doLayout();
}


function callMonthlyAgedPayableDynamicLoad(withinventory,custVendorID){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.agedreceivable, Wtf.Perm.agedreceivable.viewagedreceivable)) {
        var panel = Wtf.getCmp("mainMonthlyAgedPayable");
        if(panel==null){
            panel = new Wtf.TabPanel({
                title:WtfGlobal.getLocaleText("acc.field.monthlyagingcVendorreport"),
                tabTip:WtfGlobal.getLocaleText("acc.field.viewmonthlyagingVendorreport"),
                withinventory:withinventory,
                custVendorID:custVendorID,
                id:'mainMonthlyAgedPayable',
                closable:true,
                border:false,
                iconCls:'accountingbase agedPayable',
                activeTab:0
            });
            Wtf.getCmp('as').add(panel);
            callMonthlyAgedPayableSummary(withinventory,custVendorID);
            callMonthlyAgedPayableReport(withinventory,custVendorID);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
    else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.wtfTrans.agedrr"));
}

//********************************************************************************************************************************

Wtf.account.MonthlyAgedDetail=function(config){
    this.receivable=config.receivable||false;
    this.withinventory=config.withinventory||false;
    this.reportWithoutAging=config.reportWithoutAging||false;
    this.isSummary=config.isSummary||false;
    this.summary = new Wtf.ux.grid.GridSummary({});
    this.uPermType=(this.receivable?Wtf.UPerm.agedreceivable:Wtf.UPerm.agedpayable);
    this.permType=(this.receivable?Wtf.Perm.agedreceivable:Wtf.Perm.agedpayable);
    this.exportPermType=(this.receivable?this.permType.exportdataagedreceivable:this.permType.exportdataagedpayable);
    this.printPermType=(this.receivable?this.permType.printagedreceivable:this.permType.printagedpayable);
    this.chartPermType=(this.receivable?this.permType.chartagedreceivable:this.permType.chartagedpayable);
    this.custVendorID=config.custVendorID;
    
    this.AgedRec = new Wtf.data.Record.create([{
        name:'billid'
    },{
        name:'journalentryid'
    },{
        name:'entryno'
    },{
        name:'billno'
    },{
        name:'noteid'
    },{
        name:'noteno'
    },{
        name:'date', 
        type:'date'
    },{
        name:'duedate', 
        type:'date'
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
        name:'opening'
    },{
        name:'amountdue_0'
    },{
        name:'amountdue_1'
    },{
        name:'amountdue_2'
    },{
        name:'amountdue_3'
    },{
        name:'amountdue_4'
    },{
        name:'amountdue_5'
    },{
        name:'amountdue_6'
    },{
        name:'amountdue_7'
    },{
        name:'amountdue_8'
    },{
        name:'amountdue_9'
    },{
        name:'amountdue_10'
    },{
        name:'amountdue_11'
    },{
        name:'amountdue_12'
    },{
        name:'amountdue_13'
    },{
        name:'amountdue_14'
    },{
        name:'amountdue_15'
    },{
        name:'amountdue_16'
    },{
        name:'amountdue_17'
    },{
        name:'amountdue_18'
    },{
        name:'total',
        type: 'float'
    },{
        name:'memo'
    },{
        name:'totalinbase'
    },{
        name: 'currencysymbol'
    },{
        name: 'termname'
    },{
        name: 'withoutinventory', 
        type:'boolean'
    }, {
        name:'type'
    },{
        name:'fixedAssetInvoice'
    },{
        name:'fixedAssetLeaseInvoice'
    },{
        name:'isLeaseFixedAsset'
    },{
        name:'cntype'
    },{
        name:'lasteditedby'
    },{
        name:'gTaxId'
    },{
        name:'includeprotax'
    }
    ]);
    
    this.AgedStoreUrl = "";
    this.AgedStoreSummaryUrl = "";
    if(this.receivable){
        this.AgedStoreSummaryUrl = "ACCInvoiceCMN/getMonthlyCustomerAgedReceivable.do";
        this.AgedStoreUrl =  "ACCInvoiceCMN/getMonthlyCustomerAgedReceivable.do";
        this.expGet = 222;
        this.expSummGet = 222;
    }else{
        this.AgedStoreUrl = "ACCGoodsReceiptCMN/getMonthlyVendorAgedPayable.do";
        this.AgedStoreSummaryUrl = "ACCGoodsReceiptCMN/getMonthlyVendorAgedPayable.do";
        this.expGet = Wtf.autoNum.MonthlyVendorAgedPayable;
        this.expSummGet = Wtf.autoNum.MonthlyVendorAgedPayable;
    }
    this.AgedStore =this.isSummary? new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.AgedRec),
        groupField:'personname', 
        sortInfo: {
            field: 'personname',
            direction: "DESC"
        },
        url: this.AgedStoreSummaryUrl,
        baseParams:{
            mode:(this.isSummary?18:(this.withinventory?12:16)),
            creditonly:true,
            withinventory:this.withinventory,
            ignorezero:true,
            isdistributive:this.typeEditor != undefined?this.typeEditor.getValue():true,
            datefilter : this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
            nondeleted:true,
            isAged:true
        }
    }):new Wtf.ux.grid.MultiGroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.AgedRec),
        groupField:['personname','type'],
        sortInfo: {
            field: 'billno',
            direction: "ASC"
        },
        url: this.AgedStoreUrl,
        baseParams:{
            mode:(this.withinventory?12:16),
            creditonly:true,
            withinventory:this.withinventory,
            datefilter : this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
            ignorezero:true,
            nondeleted:true,
            isAged:true
        }
    });

    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'typeid',
            type:"boolean"
        }, 'name'],
        data :[[true,WtfGlobal.getLocaleText("acc.rem.127")],[false,WtfGlobal.getLocaleText("acc.rem.128")]]
    });

    this.dateFilterStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.agedPay.dueDate1-30")],[2,WtfGlobal.getLocaleText("acc.agedPay.dueDate0-30")],[1,WtfGlobal.getLocaleText("acc.agedPay.invoiceDate1-30")],[3,WtfGlobal.getLocaleText("acc.agedPay.invoiceDate0-30")]]
    });
    this.dateFilter = new Wtf.form.ComboBox({
        store: this.dateFilterStore,
        name:'datefilter',
        displayField:'name',
        value:0,
        width:150,
        valueField:'id',
        mode: 'local',
        triggerAction: 'all'
    });

    this.rowNo=new Wtf.KWLRowNumberer();
    this.chkselModel = new Wtf.grid.CheckboxSelectionModel({
        singleSelect : false
    });
    this.rowselModel = new Wtf.grid.RowSelectionModel();
    this.selModel = this.isSummary?this.chkselModel:this.chkselModel;
    this.selModel.on('selectionchange', this.handleRowSelect, this);
    
    var columnArr = [];
    columnArr.push(this.selModel);
    columnArr.push(this.rowNo);
    if(!this.isSummary){
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.DocumentNumber"),//(this.receivable?WtfGlobal.getLocaleText("acc.agedPay.inv"): WtfGlobal.getLocaleText("acc.agedPay.venInv"))+" "+WtfGlobal.getLocaleText("acc.agedPay.number"),
            hidden:this.isSummary,
            dataIndex:'billno',
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridJEno"),  //"Journal Entry Number",
            dataIndex:'entryno',
            hidden:this.isSummary,
            width:150,
            pdfwidth:100,
            sortable: true,
            groupable: true,
            groupRenderer: function(v){
                return v
            },
            renderer:WtfGlobal.linkRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridDate"),  //"Bill Date",
            dataIndex:'date',
            width:150,
            pdfwidth:100,
            align:'center',
            groupRenderer:this.groupDateRender.createDelegate(this),
            hidden:this.isSummary,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridDueDate"),  //"Due Date",
            dataIndex:'duedate',
            sortable: true,
            width:150,
            pdfwidth:100,
            groupable: true,
            hidden:this.isSummary,
            align:'center',
            groupRenderer: this.groupDateRender.createDelegate(this),
            renderer:WtfGlobal.onlyDateRenderer
        });
    }
    columnArr.push({
        header:(this.receivable?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven"))+"/"+ WtfGlobal.getLocaleText("acc.agedPay.accName"),
        dataIndex:'personname',
        width:150,
        pdfwidth:150,
        sortable: true,
        groupable: true,
        summaryRenderer:function(){
            return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
        }
    },{
        header:WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),  //"Currency Symbol",
        dataIndex:'currencyname',
        align:'center',
        width:150,
        pdfwidth:120
    });
    if(!this.isSummary){
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.field.TransactionType"),
            dataIndex:'type',
            align:'center',
            pdfwidth:120,
            groupable: true,
            hidden:true
        });
    }
    
    columnArr.push({
        header:'<span align=center><b>'+WtfGlobal.getLocaleText("acc.field.OpeningAmountDue")+'</b></span>',
        dataIndex:'opening',
        align:'right',
        pdfwidth:120,
        groupable: true,
        style: 'text-align:right',
        renderer:WtfGlobal.withoutRateCurrencySymbol
    });
    
    for(var i=0; i<18; i++){
        columnArr.push({
            hidden: false,
            header:"",
            dataIndex:'amountdue_'+i,
            width:80,
            pdfwidth:120,
            align:'right',
            style: 'text-align:right',
            renderer:WtfGlobal.withoutRateCurrencySymbol
        });
    }

    if(!this.isSummary){
        columnArr.push({
            header:this.isSummary?'<b>'+WtfGlobal.getLocaleText("acc.common.total")+'</b>':Wtf.account.companyAccountPref.descriptionType,  //"Memo",      
            align:'right',
            width:150,
            pdfwidth:150,
            dataIndex:'memo',
            pdfrenderer:"rowcurrency",
            renderer:this.isSummary?this.totalRender.createDelegate(this):""
        },{
            header:WtfGlobal.getLocaleText("acc.field.Term"),
            dataIndex:'termname',
            sortable: true,
            pdfwidth:100,
            groupable: true,
            hidden:this.isSummary,
            align:'center'
        });
    }
    if(this.isSummary){
        columnArr.push({
            header:this.isSummary?'<b>'+WtfGlobal.getLocaleText("acc.common.total")+'</b>':Wtf.account.companyAccountPref.descriptionType,  //"Memo",      
            align:'right',
            width:150,
            pdfwidth:150,
            dataIndex:"total",
            pdfrenderer:"rowcurrency",
            renderer:this.isSummary?WtfGlobal.currencyRenderer:""
        },{
            header:WtfGlobal.getLocaleText("acc.common.total") +" "+ WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Memo",
            hidden : !this.isSummary,
            align:'right',
            width:150,
            pdfwidth:150,
            dataIndex:"totalinbase",
            pdfrenderer:"rowcurrency",
            summaryType:'sum',
            hidecurrency : true,
            summaryRenderer: function(value,m,rec){
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value)
                return '<b>'+retVal+'</b>';
            },
            renderer:WtfGlobal.currencyRenderer
        } );
    }
    if(!this.isSummary){
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.agedPay.gridAmtDueHomeCurrency")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Amount Due (In Home Currency)",
            dataIndex:'amountdueinbase',
            align:'right',
            pdfwidth:100,
            hidden:this.isSummary,
            summaryType:'sum',
            width:150,
            hidecurrency : true,
            summaryRenderer: function(value,m,rec){
                var retVal = WtfGlobal.withoutRateCurrencySymbol(value)
                return '<b>'+retVal+'</b>';
            },
            renderer:WtfGlobal.currencyRenderer
        });   
    }
    
    this.cm= new Wtf.grid.ColumnModel(columnArr);
    
    this.groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: false,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal: false,
        isGroupTotal: false,
        hideGroupedColumn:false,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //ERP-28938
        groupTextTpl: '{group} '    
    });
    this.tbar3 = new Array();
    this.grid = this.isSummary?new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.AgedStore,
        cm:this.cm,
        sm: this.selModel,
        ctCls : 'agedview' ,
        border:false,
        layout:'fit',
        tbar:this.tbar3,
        view:new Wtf.grid.GridView({
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //ERP-28938
            deferEmptyText: false,
            forceFit:false            
        }),
        loadMask : true
    }):new Wtf.ux.grid.MultiGroupingGrid({
        stripeRows :true,
        store:this.AgedStore,
        border:false,
        ctCls:'agedsummary',
        view: this.groupView,
        plugins: [this.summary],
        cm:this.cm,
        sm: this.selModel,
        tbar:this.tbar3,
        loadMask : true
    });
    
    this.grid.on("render",function(){
        this.grid.getView().applyEmptyText(); 
    },this);
    
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        hidden:this.isSummary,
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,       
        hidden:this.isSummary,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.receivable?2:6,
        advSearch: false
    });
    
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.email=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.email"),
        tooltip :WtfGlobal.getLocaleText("acc.MailWin.sendMail"),
        scope: this,
        handler:this.sendMail,
        iconCls: "accountingbase financialreport"
    });
        
    this.asOfDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.asOf"),  //'As of',
        name:'asofdate',
        id: 'asofdate'+this.id+config.helpmodeid,
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:true,
        value:new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM")
    });
    
    // to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'monthid',
            type:'int'
        }, 'name'],
        data :[[0,"January"],[1,"February"],[2,"March"],[3,"April"],[4,"May"],[5,"June"],[6,"July"],[7,"August"],[8,"September"],[9,"October"],
        [10,"November"],[11,"December"]]
    });

    var data=WtfGlobal.getBookBeginningYear(true);
    
    this.yearStore= new Wtf.data.SimpleStore({
        fields: [{
            name:'id',
            type:'int'
        }, 'yearid'],
        data :data
    });

    this.startMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
        name:'startMonth',
        displayField:'name',
        forceSelection: true,
        anchor:'95%',
        valueField:'name',
        mode: 'local',
        width:90,
        triggerAction: 'all',
        selectOnFocus:true
    });  

    this.startYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
        name:'startYear',
        displayField:'yearid',
        anchor:'95%',
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        width:90,
        selectOnFocus:true
    });  

    this.endMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
        name:'endMonth',
        displayField:'name',
        forceSelection: true,
        anchor:'95%',
        valueField:'name',
        mode: 'local',
        triggerAction: 'all',
        width:90,
        selectOnFocus:true
    }); 

    this.endYear = new Wtf.form.ComboBox({
        store: this.yearStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
        name:'endYear',
        displayField:'yearid',
        anchor:'95%',
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        width:90,
        selectOnFocus:true
    });      

    this.asOfDate.on("change",this.checkDates,this);
    this.expButton=new Wtf.exportButton({
        obj:this,
        id: "exportReports"+config.helpmodeid,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.exportTT"),  //'Export report details',
        disabled :true,
        filename:this.receivable==true?WtfGlobal.getLocaleText("acc.field.monthlyagingcustomerreport")+"_v1":WtfGlobal.getLocaleText("acc.field.monthlyagingcVendorreport")+"_v1",
        params:{
            stdate:this.startMonth.getValue() + ", " + this.startYear.getValue(),
            enddate:this.endMonth.getValue() + ", " + this.endYear.getValue(),
            accountid:this.accountID||config.accountID,
            curdate: this.endMonth.getValue() + ", " + this.endYear.getValue(),
            agedDetailsFlag : true,
            datefilter : this.dateFilter.getValue()
        },
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        get:this.isSummary?this.expSummGet:this.expGet
    })
    this.printButton = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //'Print report details',
        disabled: true,
        filename: this.receivable == true ? WtfGlobal.getLocaleText("acc.field.monthlyagingcustomerreport") : WtfGlobal.getLocaleText("acc.field.monthlyagingcVendorreport"),
        label: (this.isSalesCommissionStmt) ? WtfGlobal.getLocaleText("acc.field.SalesCommissionStatement") : config.isCustomer ? (config.isOrder ? WtfGlobal.getLocaleText("acc.accPref.autoSO") : WtfGlobal.getLocaleText("acc.accPref.autoInvoice")) : (config.isOrder ? WtfGlobal.getLocaleText("acc.accPref.autoPO") : WtfGlobal.getLocaleText("acc.agedPay.venInv")),
        params: {
            stdate: this.startMonth.getValue() + ", " + this.startYear.getValue(),
            enddate: this.endMonth.getValue() + ", " + this.endYear.getValue(),
            accountid: this.accountID || config.accountID,
            curdate: this.endMonth.getValue() + ", " + this.endYear.getValue(),
            agedDetailsFlag: true,
            datefilter: this.dateFilter.getValue()
        },
        menuItem: {print: true},
        get: this.isSummary ? this.expSummGet : this.expGet
    })
         
    this.personRec = new Wtf.data.Record.create([
    {
        name: 'accid'
    }, {
        name: 'accname'
    }, {
        name: 'acccode'
    }
    ]);
    
    this.customerAccStore =  new Wtf.data.Store({   //Customer/vendor multi selection Combo
        url:this.receivable?"ACCCustomer/getCustomersForCombo.do":"ACCVendor/getVendorsForCombo.do",
        baseParams:{
            deleted:false,
            nondeleted:true,
            combineData:-1
        },
        reader: new  Wtf.data.KwlJsonReader({
            root: "data"
        },this.personRec)
    });

    this.customerAccStore.on("load", function(store){
        var storeNewRecord=new this.personRec({
            accname:'All',
            accid:'All',
            acccode:''
        });
        this.Name.store.insert( 0,storeNewRecord);
        if((this.custVendorID==undefined ||this.custVendorID=="")){
            this.Name.setValue("All");   
        }else{
            this.Name.setValue(this.custVendorID); 
        }         
    },this);
    this.customerAccStore.load();
   
    this.CustomerComboconfig = {
        store: this.customerAccStore,
        valueField:'accid',
        hideLabel:true,
        displayField:'accname',
        emptyText:this.receivable?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") ,
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
    this.Name = new Wtf.common.Select(Wtf.applyIf({
        multiSelect:true,
        fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*' ,
        forceSelection:true,    
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        width:200
    },this.CustomerComboconfig));

    this.Name.on('select',function(combo,personRec){
        if(personRec.get('accid')=='All'){
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){
            combo.clearValue();
            combo.setValue(personRec.get('accid'));
        }
    } , this);
    var btnArr=[];
    var bottombtnArr=[];
    btnArr.push(
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.agedPay.search"), 
            id:"quickSearch"+config.helpmodeid,
            width: 200,
            hidden:this.isSummary,
            field: 'personname'
        }),
        this.resetBttn);
    btnArr.push(this.Name,'-',
        WtfGlobal.getLocaleText("acc.common.asOf"),this.asOfDate,"-",
        WtfGlobal.getLocaleText("acc.common.from"),
        this.startMonth, this.startYear,
        WtfGlobal.getLocaleText("acc.common.to"),
        this.endMonth, this.endYear,
//        WtfGlobal.getLocaleText("acc.field.On"),this.dateFilter);
        WtfGlobal.getLocaleText("acc.field.AgedOn"),this.dateFilter);
    btnArr.push(
    {
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
        iconCls:'accountingbase fetch',
        scope:this,
        tooltip:this.receivable?WtfGlobal.getLocaleText("acc.agedReceive.view"):WtfGlobal.getLocaleText("acc.agedPay.view"),  //"Select a date to view Aged Receivable.":"Select a date to view Aged Payable.",
        handler:this.fetchAgedData
    });
    this.ReportWithoutAging = new Wtf.Toolbar.Button({
        text : this.receivable?WtfGlobal.getLocaleText("acc.agedPay.totalagedReceivable"):WtfGlobal.getLocaleText("acc.agedPay.totalagedPayable"),
        iconCls: 'accountingbase agedrecievable',
        tooltip : (this.receivable?WtfGlobal.getLocaleText("acc.agedPay.totalagedReceivable.tooltip"):WtfGlobal.getLocaleText("acc.agedPay.totalagedPayable.tooltip")),
        id : 'agingreportbutt'+this.id,
        scope : this,
        hidden : !this.isSummary ||this.reportWithoutAging,
        handler : this.getReportWithoutAging
    });
    this.tbar3.push(this.AdvanceSearchBtn);
    if(!this.isSummary) {
        this.tbar3.push(this.email);  
    }
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        bottombtnArr.push('-', this.expButton);
    }
    bottombtnArr.push('-', this.printButton);
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.helpmodeid!=null){
        btnArr.push("->");
        btnArr.push(getHelpButton(this,config.helpmodeid));
    }
  
    this.leadpan = new Wtf.Panel({
        border:false,
        layout : "border",
        items:[this.objsearchComponent
        , {
            region: 'center',
            layout: 'fit',
            border: false,
            tbar:btnArr,
            items: [this.grid],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.AgedStore,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                //            displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                items:bottombtnArr
            })
        }]
    });
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });

    Wtf.account.MonthlyAgedDetail.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.AgedStore.on("beforeload", function(s,o) {      
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }      
        o.params.curdate=this.endMonth.getValue() + ", " + this.endYear.getValue();
        o.params.startdate=this.startMonth.getValue() + ", " + this.startYear.getValue();
        o.params.asofdate=WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue());
        o.params.enddate=this.endMonth.getValue() + ", " + this.endYear.getValue();
        o.params.custVendorID=(this.custVendorID==undefined || this.custVendorID=="")?this.Name.getValue():this.custVendorID;
        s.baseParams.datefilter=this.dateFilter.getValue();
        s.baseParams.isSummary=this.isSummary;
        s.custVendorID=(this.custVendorID==undefined || this.custVendorID=="")?this.Name.getValue():this.custVendorID;
    },this);
    this.AgedStore.on('load',this.storeloaded,this);
//    this.AgedStore.load({         //ERP-28938
//        params:{
//            start:0,
//            datefilter : this.dateFilter.getValue(),
//            custVendorID:(this.custVendorID==undefined || this.custVendorID=="")?this.Name.getValue():this.custVendorID,
//            startdate : this.startMonth.getValue() + ", " + this.startYear.getValue(),           
//            asofdate : WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()), 
//            enddate: this.endMonth.getValue() + ", " + this.endYear.getValue(),
//            limit:30,
//            creditonly:true,
//            isAged:true,
//            isSummary:this.isSummary
//        }
//    });
    
    this.AgedStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
    this.grid.view.refresh.defer(1, this.grid.view); 
    this.grid.on('cellclick',this.onCellClick, this);
}

Wtf.extend( Wtf.account.MonthlyAgedDetail,Wtf.Panel,{ 
    sumBaseAmount:function(dataindex,v,m,rec){       
        if(!this.isSummary){
            v=rec.data[dataindex];
            return WtfGlobal.withoutRateCurrencySymbol(v,m,rec)
        }
        return "";
    }, 
    groupDateRender:function(v){
        return v.format(WtfGlobal.getOnlyDateFormat())
    },
    
    totalRender:function(v,m,rec){
        var val=WtfGlobal.withoutRateCurrencySymbol(v,m,rec);
        return "<b>"+val+"</b>"
    },
    
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.AgedStore.load({
                params: {
                    start:0,
                    datefilter : this.dateFilter.getValue(),
                    limit:this.pP.combo.value,
                    aged:true,
                    creditonly:true,
                    isAged:true,
                    startdate : this.startMonth.getValue() + ", " + this.startYear.getValue(),
                    enddate:this.endMonth.getValue() + ", " + this.endYear.getValue(),
                    asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue())
                }
            });
        }
    },
  
    storeloaded:function(store){
        WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
        if(store.getCount()==0){
            if(this.expButton)this.expButton.disable();
            if(this.printButton){this.printButton.disable();}
        }else{
            if(this.expButton)this.expButton.enable();
             if(this.printButton){this.printButton.enable();}
        }
        this.quickPanelSearch.StorageChanged(store);
        var monthArray = store.data.items[store.data.length-1].json["months"];
        for(var i=0; i<monthArray.length; i++){            
            if(this.isSummary){
                this.grid.getColumnModel().setColumnHeader((i+5), '<span align=left><b>'+monthArray[i]["monthname"]+'</b></span>') ;            
            }else{
                this.grid.getColumnModel().setColumnHeader((i+10), '<span align=left><b>'+monthArray[i]["monthname"]+'</b></span>') ;            
            }
        }
        var monthCount = monthArray.length;
        var tempcnt;
        if(this.isSummary){
            monthCount+=5;
            tempcnt=5;
        }else{
            monthCount+=10;
            tempcnt=10;
        }
        this.pagingToolbar.store.remove(this.pagingToolbar.store.getAt(this.pagingToolbar.store.getCount()-1));
        this.pagingToolbar.updateInfo();
        // show those months with data
        for(var i=1; i<(monthCount); i++){
            this.grid.getColumnModel().setHidden(i, false) ;
        }  
        for(var i=(monthCount); i<(parseInt(tempcnt)+18); i++){
            this.grid.getColumnModel().setHidden(i,true) ;
        } 
       // store.remove(store.getAt(store.data.length-1));
        this.grid.getView().refresh();
        this.grid.loadMask.hide();
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.AgedStore.getAt(i).data['journalentryid'];
            var startDateValue=this.AgedStore.getAt(i).data['date'];    //bill date value for 'From' field in JE tab
            var endDateValue=this.AgedStore.getAt(i).data['date'];  //bill date value for 'To' field in JE tab
            this.fireEvent('journalentry',accid,true,undefined,undefined,undefined,undefined,startDateValue,endDateValue); //journalentry tab opens with 'startDateValue' as 'From' value and 'endDateValue' as 'To' value
        }
        if(header=="billno"){
            var formrec = this.AgedStore.getAt(i);
            var type=formrec.data['type'];
            var withoutinventoryFlag = formrec.data.withoutinventory;
//            if(type=="Credit Note" && withoutinventoryFlag){
//                callViewBillingCreditNote(formrec, 'ViewcreditNote')         
//            }else 
//            if(type=="Credit Note" && !withoutinventoryFlag){
//                if(this.receivable){
//                    if (formrec.data.cntype == Wtf.NoteForOvercharge) {
//                        var winid = 'creditnoteForOverchargeView' + formrec.get("noteno");
//                        callEditNoteForOvercharge(winid, formrec, true, true, true, true);
//                    } else {
//                        callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,formrec.get('cntype'),formrec, null); 
//                    }
//                } else{
//                    if (formrec.data.cntype == Wtf.CNDN_TYPE_FOR_MALAYSIA) {
//                        callViewCreditNoteGst(true, formrec, false, false, true);
//                    } else {
//                        callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,4,formrec, null); //Credit Note Against Vendor need to send cntype 4
//                    }
//                }     
//            }
////            else if(type=="Debit Note" && withoutinventoryFlag){
////                callViewBillingDebitNote(formrec, 'ViewDebitNote')
////            }
//            else if(type=="Debit Note" && !withoutinventoryFlag){
//                if(this.receivable){
//                    if (formrec.data.cntype == Wtf.CNDN_TYPE_FOR_MALAYSIA) {
//                        callViewCreditNoteGst(true, formrec, false, false, false);
//                    } else {
//                        callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,4,formrec, null);;  //Debit Note Against Customer need to send cntype 4
//                    }
//                }else{
//                    if (formrec.data.cntype == Wtf.CNDN_TYPE_FOR_MALAYSIA) {
//                        callViewCreditNoteGst(true, formrec, false, false, false);
//                    } else if (formrec.data.cntype == Wtf.NoteForOvercharge) {
//                        var winid = 'debitnoteForOverchargeView' + formrec.get("noteno");
//                        callEditNoteForOvercharge(winid, formrec, true, true, false, true);
//                    } else {
//                        callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,formrec.get('cntype'),formrec, null);   
//                    }
//                }
//            }else{
                viewTransactionTemplate(type, formrec);   
//            }
        }
    },
    
    checkDates : function(dateObj,newVal,oldVal){            
        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue(); 
        if(this.asOfDate.getValue()<this.sDate){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.AsofdateshouldbeinrangeofFromDateandToDate")], 2);  //"As of date should be in range of From Date and To Date."
            dateObj.setValue(oldVal);
        }           
        if(this.eDate.getValue()<this.sDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.FromdateshouldnotbegreaterthanToDate")], 2);  //"From date should not be greater than To Date."
            dateObj.setValue(oldVal);
        }
    },
        
    viewTransection:function(){						// Function for viewing the invoice details from the invoice list  Neeraj
        var formrec=null;
        formrec = this.grid.getSelectionModel().getSelected();
        var type=formrec.data['type'];
        var withoutinventoryFlag = formrec.data.withoutinventory;
        var isExpensiveInv = formrec.get("isexpenseinv");
           
//        if(type=="Customer Invoice" && withoutinventoryFlag) {
//            callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
//        } else 
        if(type=="Customer Invoice" && !withoutinventoryFlag){
            if(formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
            } else if(formrec.data.isConsignment){
                callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
            }else{
                callViewInvoice(formrec, 'ViewCashReceipt');
            }
        } 
//        else if(type=="Vendor Invoice" && withoutinventoryFlag) {
//            callViewBillingGoodsReceipt(formrec,null, 'ViewBillingInvoice',false);
//        } 
        else if(type=="Vendor Invoice" && !withoutinventoryFlag){
            if(formrec.data.fixedAssetInvoice){
                callViewFixedAssetGoodsReceipt(formrec, formrec.data.billid+'GoodsReceipt',false,formrec.data.isExpensiveInv,undefined,false,formrec.data.fixedAssetInvoice);
            } else{
                callViewGoodsReceipt(formrec, 'ViewGoodsReceipt',formrec.get("isexpenseinv"));
            }
        } 
//        else if(type == "Payment Received" && withoutinventoryFlag) {
//            callViewBillPayment(formrec, 'ViewBillingReceivePayment',true)
//        } 
        else if(type == "Payment Received"&& !withoutinventoryFlag) {
            if(Wtf.isNewPaymentStructure) {
                callViewPaymentNew(formrec, 'ViewReceivePayment',true);
            }
        } 
//        else if(type == "Payment Made" && withoutinventoryFlag) {
//            callViewBillPayment(formrec, 'ViewBillingPaymentMade',false)
//        } 
        else if(type == "Payment Made" && !withoutinventoryFlag) {
            if(Wtf.isNewPaymentStructure) {
                callViewPaymentNew(formrec, 'ViewPaymentMade',false);
            }
        } 
//        else if(type == "Credit Note" && withoutInventory) {
//            callViewBillingCreditNote(formrec, 'ViewcreditNote')
//        } 
        else if(type == "Credit Note" && !withoutInventory) {
            callViewCreditNote("ViewcreditNote" + formrec.get("noteno"), true,true,formrec.get('cntype'),formrec, null);
        }
//        else if(type == "Debit Note" && withoutInventory) {
//            callViewBillingDebitNote(formrec, 'ViewDebitNote')
//        } 
        else if(type == "Debit Note" && !withoutInventory) {
            callViewDebitNote("ViewDebitNote" + formrec.get("noteno"), true,false,formrec.get('cntype'),formrec, null);
        }
    },
    
    fetchAgedData:function(){ 
        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return;
        }
        if (this.startYear.getValue() > this.endYear.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;            
        }
        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;            
        }
        var months;
        months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
        months -= startMonthDate.getMonth();
        months += endMonthDate.getMonth();
        if (months<0)
            months=0;
        if (months>18){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported") ], 2);
            return;            
        }
  
        this.AgedStore.load({
            params:{
                startdate : this.startMonth.getValue() + ", " + this.startYear.getValue(),
                enddate:this.endMonth.getValue() + ", " + this.endYear.getValue(),
                asofdate:WtfGlobal.convertToGenericEndDate(this.asOfDate.getValue()),
                datefilter : this.dateFilter.getValue(),
                custVendorID:this.Name.getValue(),
                start:0,
                limit:this.pP.combo.value,
                creditonly:true,
                isAged:true
            }
        });
 
        this.expButton.setParams({
            stdate:this.startMonth.getValue() + ", " + this.startYear.getValue(),
            startdate:this.startMonth.getValue() + ", " + this.startYear.getValue(),
            accountid:this.accountID,
            enddate:this.endMonth.getValue() + ", " + this.endYear.getValue(),
            curdate: this.endMonth.getValue() + ", " + this.endYear.getValue(),
            datefilter : this.dateFilter.getValue()
        });
        this.pagingToolbar.store.remove(this.pagingToolbar.store.getAt(this.pagingToolbar.store.getCount()-1));
        this.pagingToolbar.updateInfo();
    }, 
    
    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.AgedStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
        
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.AgedStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.receivable?2:6,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.AgedStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.AgedStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.receivable?2:6,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.AgedStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
            }
        });
    },
    
    handleRowSelect : function(sm){
        var oldAccountId = "";
        var selectedRecArr = this.grid.getSelectionModel().getSelections();
        var distictVendorsExists = false;
        for(var i = 0; i<selectedRecArr.length ; i++){
            if(selectedRecArr[i].data.personid !== selectedRecArr[0].data.personid){
                distictVendorsExists = true;
                break;
            }
        }
        if(distictVendorsExists){
            this.email.disable();
        }else{
            this.email.enable();
        }
    },
    
    sendMail : function(){
        var selectedRec = null;
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(15,2);
            return;
        }
        selectedRec = this.grid.getSelectionModel().getSelections();
        var mode = 0;
        var configstr = this.getTemplateConfig();
        var fileType = 'pdf';
        var get = 24;
        var pdfStore = this.filPdfStore();
        var gridConfig = this.genJsonForPdf(pdfStore);
        var deleted = false;
        var nondeleted = true;
        var exportUrl = "ACCInvoiceCMN/exportInvoices.do";
        var gridParams = this.grid.getStore().baseParams;
        var isCustomer = (this.receivable)?true:false;
        var label = WtfGlobal.getLocaleText("acc.field.Aged")+((this.receivable)?WtfGlobal.getLocaleText("acc.field.Receivables")+" ":WtfGlobal.getLocaleText("acc.field.Payables"));
        var fileName = label;
        var filterParams="";
        callEmailForMultipleRecords('newwinid', selectedRec, label, mode, isCustomer, configstr, fileName, fileType, get, gridConfig,deleted,nondeleted,filterParams);
    },
    
    filPdfStore:function(){
        this.pdfStore = new Wtf.data.Store({});
        var column = this.grid.getColumnModel();
        var k=1;
        for(var i=1 ; i<column.getColumnCount() ; i++) { // skip row numberer
            if(column.isHidden(i)==true||column.getColumnHeader(i)==""||column.getDataIndex(i)=="" || column.getDataIndex(i)=='amountdue1' || column.getDataIndex(i)=='amountdue2' || column.getDataIndex(i)=='amountdue3' || column.getDataIndex(i)=='amountdue4'|| column.getDataIndex(i)=='amountdue5' || column.getDataIndex(i)=='amountdue6' || column.getDataIndex(i)=='amountdue7' || column.getDataIndex(i)=='amountdue8'){
                continue;
            }
            else{
                if( column.config[i].pdfwidth!=undefined) {
                    var format="";
                    var title;
                    if(column.getRenderer(i)==WtfGlobal.currencyRenderer || column.getRenderer(i)==WtfGlobal.currencyDeletedRenderer ||column.getRenderer(i)==WtfGlobal.currencySummaryRenderer || column.getRenderer(i)==WtfGlobal.currencyRendererDeletedSymbol || column.getRenderer(i)==WtfGlobal.globalCurrencySymbolforDebit || column.getRenderer(i)==WtfGlobal.globalCurrencySymbolforCredit) {
                        format= (column.config[i].hidecurrency) ? "withoutcurrency" :  'currency';
                    } else if(column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbol || column.getRenderer(i)==WtfGlobal.withoutRateCurrencyDeletedSymbol || column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbolforDebit || column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbolforCredit) {
                        format= (column.config[i].hidecurrency) ? "withoutrowcurrency" : 'rowcurrency';
                    } else if(column.getRenderer(i)==WtfGlobal.onlyDateRenderer || column.getRenderer(i)==WtfGlobal.onlyDateDeletedRenderer) {
                        format='date';
                    } else {
                        if(column.config[i].pdfrenderer!=undefined) {
                            format= (column.config[i].hidecurrency) ? "" : column.config[i].pdfrenderer;
                        }
                    }

                    if(column.config[i].title==undefined)
                        title=column.config[i].dataIndex;
                    else
                        title=column.config[i].title;
                    this.newPdfRec = new Wtf.data.Record({
                        header : title,
                        title : column.config[i].header,
                        width : column.config[i].pdfwidth,
                        align : format,
                        index : k
                    });
                    this.pdfStore.insert(this.pdfStore.getCount(), this.newPdfRec);
                    k++;
                }
            }
        }
        return this.pdfStore;
    },
    
    getTemplateConfig : function(){
        var title = WtfGlobal.getLocaleText("acc.field.Aged")+((this.receivable)?WtfGlobal.getLocaleText("acc.field.Receivables"):WtfGlobal.getLocaleText("acc.field.Payables"));
        var config = '{"landscape":"true","pageBorder":"true","gridBorder":"true","title":'+title+',"subtitles":"","headNote":"Aged Report","showLogo":"true","headDate":"true","footDate":"false","footPager":"false","headPager":"true","footNote":"","textColor":"000000","bgColor":"FFFFFF"}';
        
        return config;
    },
    
    genJsonForPdf:function (pdfStore){
        var jsondata = [];
        for(var i=0;i<pdfStore.getCount();i++) {
            var recData = pdfStore.getAt(i).data;
            if(recData.align=="right" && recData.title.indexOf("(")!=-1) {
                recData.title=recData.title.substring(0,recData.title.indexOf("(")-1);
            }
            var temp = {
                header:recData.header,
                title:encodeURIComponent(recData.title),
                width:recData.width,
                align:recData.align
            };
            jsondata.push(temp)
        }

        return Wtf.encode({
            data:jsondata
        });
    }
});


