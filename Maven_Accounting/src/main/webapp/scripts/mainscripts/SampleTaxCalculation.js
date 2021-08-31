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
Wtf.account.sampleTaxCalculation=function(config){
    this.summary = new Wtf.ux.grid.GridSummary();
        
    this.isSales=config.isSales||false;
    this.ReportType=this.isSales?"Sales":"Purchase";
    this.reportid = (config.isSales ? Wtf.autoNum.EntityBasedSalesTaxReport : Wtf.autoNum.EntityBasedPurchaseTaxReport);
    this.prodRec = new Wtf.data.Record.create([{
        name:'taxcode'
    },{
        name:'taxname'
    },{
        name:'totalsale'
    },{
        name:'nontaxablesale'
    },{
        name:'taxablesale'
    },{
        name:'taxrate'
    },{
        name:'taxcollected'
    },{
        name:'taxpayable'
    },{
        name:'taxamount'
    }]);
    this.prodStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.prodRec),
        //        url: Wtf.req.account+"reporthandler.jsp",
        url : "ACCReports/getCalculatedTax.do",
        baseParams:{
            issales:this.isSales,
            isForTaxReport:Wtf.account.companyAccountPref.countryid=='137'?true:false,
            mode:2
        }
    });
    this.rowNo=new Wtf.KWLRowNumberer();

    this.prodStore.on('load',this.storeloaded,this);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.prodStore,
        border:false,
        layout:'fit',
        viewConfig:{
//            forceFit:true,
            emptyText: '<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>',
            deferEmptyText: false,
            enableRowBody: true
        },
        forceFit:true,
        loadMask : true,
        columns:[this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxName"),  //'Tax Name',
            dataIndex:'taxname',
            renderer:WtfGlobal.deletedRenderer,
            width:200,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxCode"),  //'Tax Code',
            dataIndex:'taxcode',
            renderer:WtfGlobal.deletedRenderer,
            width:200,
            pdfwidth:100
        },{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxRate"),  //"Tax Rate",
            dataIndex:'taxrate',
            renderer:function(v){return "<div style='float:right'>"+v+"%</div>";},
            width:100,
            pdfwidth:100
        },{
            header:(!this.isSales?WtfGlobal.getLocaleText("acc.taxReport.totalPurchase"):WtfGlobal.getLocaleText("acc.taxReport.totalSale")) + " ("+WtfGlobal.getCurrencyName()+")",
            dataIndex:'totalsale',
            renderer:WtfGlobal.currencyRendererDeletedSymbol,
            hidecurrency : true,
            width:200,
            pdfwidth:100
        //        },{
        //            header:"Non-Taxable Sales",
        //            dataIndex:'nontaxablesale',
        //            renderer:WtfGlobal.currencyRendererDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.taxReport.taxAmount")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Tax Amount",
            dataIndex:'taxamount',
            renderer:WtfGlobal.currencyRendererDeletedSymbol,
            hidecurrency : true,
            width:200,
            pdfwidth:100
        //        },{
        //            header:"Taxable Sales",
        //            dataIndex:'taxablesale',
        //            renderer:WtfGlobal.currencyRendererDeletedSymbol
//        },{
//            header:WtfGlobal.getLocaleText("acc.taxReport.taxRate"),  //"Tax Rate",
//            dataIndex:'taxrate',
//            renderer:function(v){return "<div style='float:right'>"+v+"%</div>";},
//            pdfwidth:100
//        },{
//            header: (this.isSales?WtfGlobal.getLocaleText("acc.taxReport.taxCollected"):WtfGlobal.getLocaleText("acc.taxReport.taxPaid"))+ " ("+WtfGlobal.getCurrencyName()+")",  //"Tax Collected":"Tax Paid"),
//            dataIndex:'taxcollected',
//            renderer:WtfGlobal.currencyRendererDeletedSymbol,
//            hidecurrency : true,
//            pdfwidth:100
//        },{
//            header:(this.isSales?WtfGlobal.getLocaleText("acc.taxReport.recievable"):WtfGlobal.getLocaleText("acc.taxReport.payable"))+ " ("+WtfGlobal.getCurrencyName()+")",   //" Tax Payable":"Tax Receivable"),
//            dataIndex:'taxpayable',
//            renderer:WtfGlobal.currencyRendererDeletedSymbol,
//            hidecurrency : true,
//            pdfwidth:100
        }]
    });
    
    this.grid.on("afterlayout",function(g,l){
        /**
         * Auto Load to be removed.
         */
//      this.prodStore.load({
//            params: {
//                start:0,
//                limit:this.pP.combo.value
//            }
//        });
        /**
         * Applying Empty Text Required.
         */
        this.grid.getView().applyEmptyText();
    },this);
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: Wtf.MultiEntityReportsModuleIdArray.split(','),
        reportid:this.reportid,
        advSearch: false,
        parentPanelSearch: this,
        ignoreDefaultFields: true,
        isAvoidRedundent: true,
        isMultiEntity: (Wtf.Countryid == Wtf.Country.SINGAPORE) ? false : true,   // flag to fetch only multi entity dimension in advance search.
        hideRememberSerch:true
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.fromDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:WtfGlobal.getDates(true)
    });
    this.toDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    var btnArr=[];
    
//    this.fromDate.on('change',function(field,newval,oldval){
//        if(field.getValue()!='' && this.toDate.getValue()!=''){
//            if(field.getValue().getTime()>this.toDate.getValue().getTime()){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
//                field.setValue(oldval);                    
//            }
//        }
//    },this);
//        
//    this.toDate.on('change',function(field,newval,oldval){
//        if(field.getValue()!='' && this.fromDate.getValue()!=''){
//            if(field.getValue().getTime()<this.fromDate.getValue().getTime()){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
//                field.setValue(oldval);
//            }
//        }
//    },this);  
    
    this.taxYearStore = new Wtf.data.SimpleStore({
        fields: [{name: 'id'}, {name: 'name'},{name:'entrydate'},{name:'enddate'}]
    });
  
    this.taxPeriodStore = new Wtf.data.SimpleStore({
         fields: [{name: 'id'}, {name: 'name'},{name:'entrydate'},{name:'enddate'}]
    });

    this.TaxYear  = new Wtf.form.ComboBox({
        displayField:'name',
        valueField:'id',
        name:'id',
        mode: 'local',
        store:this.taxYearStore,
        emptyText:WtfGlobal.getLocaleText("acc.field.Selecttaxyear"),
        triggerAction: 'all',
//        allowBlank: false,
        typeAhead:true,
        width: 150,
        listWidth: 400,
        listeners:{
            scope:this,
            beforequery: function() {
                this.TaxYear.clearValue();// it clears the combo but value is still there.
                this.TaxYear.reset();//this reset the value to blank
                this.setTaxYear(Wtf.TaxAccountingPeriods.YEAR);    
            },
            select: function(combo, record, index) {
                this.setTaxPeriod(); 
                this.fromDate.setValue(record.data.entrydate);
                this.toDate.setValue(record.data.enddate);
            }
        }
    });

    this.TaxPeriod = new Wtf.form.ComboBox({
        displayField:'name',
        valueField:'id',
        store:this.taxPeriodStore,
        name:'id',
        mode: 'local',
        emptyText:WtfGlobal.getLocaleText("acc.field.Selectataxperiod"),
        triggerAction: 'all',
//        allowBlank: false,
        typeAhead:true,
        width: 150,
        listWidth: 400,
        listeners:{
            scope:this,
            beforequery: function() {
                if(this.TaxYear.getValue()==""){
                    this.TaxPeriod.clearValue();
                    this.taxPeriodStore.loadData("");
                }
            },
            select: function(combo, record, index) {
                this.fromDate.setValue(record.data.entrydate);
                this.toDate.setValue(record.data.enddate);
            }
        }
    });
    
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.taxReport.search"),  //'Search by Tax Name',
        width: 200,
        field: 'taxname'
    }),this.resetBttn,WtfGlobal.getLocaleText("acc.common.taxyear"),this.TaxYear,'-',WtfGlobal.getLocaleText("acc.common.taxperiod"),this.TaxPeriod,'-','From :',this.fromDate,'-','To :',this.toDate,'-',{
        xtype:'button',
        text:'Fetch',
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.loadStore
    },
    this.exportButton=new Wtf.exportButton({
        obj:this,
        disabled:true,
        tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
        id:(this.isSales?"exportsalestaxreport":"exportpurchasetaxreport"),
        params:{
            name:this.isSales?WtfGlobal.getLocaleText("acc.taxReport.salesTax"):WtfGlobal.getLocaleText("acc.taxReport.purchaseTax"),
            issales:this.isSales
        },
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        get:911,
        label:this.isSales?WtfGlobal.getLocaleText("acc.taxReport.salesReport"):WtfGlobal.getLocaleText("acc.taxReport.purchaseReport"),
        filename:this.isSales?WtfGlobal.getLocaleText("acc.taxReport.salesReport")+"_v1":WtfGlobal.getLocaleText("acc.taxReport.purchaseReport")+"_v1"
    }),
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        disabled:true,
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",
        filename : WtfGlobal.getLocaleText("acc.taxReport.taxReport"),
        params:{
            name:this.isSales?WtfGlobal.getLocaleText("acc.taxReport.salesTax"):WtfGlobal.getLocaleText("acc.taxReport.purchaseTax"),
            issales:this.isSales
        },
        menuItem:{
            print:true
        },
        get:911,
        label:this.isSales?WtfGlobal.getLocaleText("acc.taxReport.salesReport"):WtfGlobal.getLocaleText("acc.taxReport.purchaseReport")
    }),
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton",
        hidden: !(Wtf.account.companyAccountPref.isMultiEntity || Wtf.Countryid == Wtf.Country.SINGAPORE)
    })
    );
   
    Wtf.apply(this, {
        border: false,
        layout: "border",
        items: [this.objsearchComponent,
            {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.prodStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id: "pPageSize_" + this.id
                    })
                })
            }]
    });
    
    this.resetBttn.on('click',this.handleResetClick,this);
    Wtf.account.sampleTaxCalculation.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.prodStore.on("beforeload", function(s,o) {
        WtfGlobal.setAjaxTimeOut();
        o.params.stdate= WtfGlobal.convertToGenericDate(this.fromDate.getValue());
        o.params.enddate= WtfGlobal.convertToGenericDate(this.toDate.getValue());
    },this);
    
    this.prodStore.on("loadexception",function(){
       WtfGlobal.resetAjaxTimeOut(); 
    });
    this.prodStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
//    this.prodStore.load({
//        params:{
//            start:0,
//            limit:30
//        }
//    });
        }
    
Wtf.extend( Wtf.account.sampleTaxCalculation,Wtf.Panel,{
    loadStore:function(){
          if( this.fromDate.getValue()!='' && this.toDate.getValue()!=''){
            if(this.fromDate.getValue().getTime()>this.toDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                 return;
            }
        }  
        this.prodStore.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },

    storeloaded:function(store){
        WtfGlobal.resetAjaxTimeOut();
        this.quickPanelSearch.StorageChanged(store);
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
    },    
    setTaxYear: function(periodType) {//Populating fields in Sub Periods form.
        Wtf.Ajax.requestEx({
            url: "accPeriodSettings/getParentTaxPeriods.do",
            method: 'POST',
            params: {
                periodtype: periodType
            }
        },this, function(response){
            if (response.success) {
                var resData = response.data;
                var arr2 = [];
                for (var i = 0; i < resData.length; i++) {
                    if(resData[i]!=undefined && resData[i]!='undefined'){
                        var data = resData[i];
                        var arr1 = [data.id,data.name,data.entrydate,data.enddate]
                        arr2.push(arr1);
                    }
                }
                this.taxYearStore.loadData(arr2);
            }
        });
    },
    setTaxPeriod: function() {//Populating fields in Sub Periods form.
        Wtf.Ajax.requestEx({
            url: "accPeriodSettings/getTaxPeriods.do",
            method: 'POST',
            params: {
                subperiodOf:this.TaxYear.getValue()
            }
        },this, function(response){
            if (response.success) {
                var resData = response.data;
                var arr2 = [];
                for (var i = 0; i < resData.length; i++) {
                    if(resData[i]!=undefined && resData[i]!='undefined'){
                        var data = resData[i];
                        var arr1 = [data.id,data.periodname,data.startdate,data.enddate]
                        arr2.push(arr1);
                    }
                }
                this.taxPeriodStore.loadData(arr2);
            }
        });
    },

    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.prodStore.baseParams = this.prodStore.baseParams || {};
        this.prodStore.baseParams.flag = 1;
        this.prodStore.baseParams.iscustomcolumndata= 0;
        this.prodStore.baseParams.searchJson= this.searchJson;
        this.prodStore.baseParams.filterConjuctionCriteria= filterConjuctionCriteria;
        
        this.prodStore.load({
            params: {
                issales:this.isSales,
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.prodStore.baseParams = this.prodStore.baseParams || {};
        this.prodStore.baseParams.flag = 1;
        this.prodStore.baseParams.iscustomcolumndata= 0;
        this.prodStore.baseParams.searchJson= this.searchJson;
        this.prodStore.baseParams.filterConjuctionCriteria= this.filterConjuctionCrit;
        
        this.prodStore.load({
            params: {
                issales:this.isSales,
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    }
});

//************************************* GST Report********************************
Wtf.account.GSTReportTab=function(config){
    this.summary = new Wtf.ux.grid.GridSummary();
    this.isSales=config.isSales||false;
    this.ReportType=this.isSales?"Sales":"Purchase";
    
    var budgetRecord = Wtf.data.Record.create([
    {
        name: 'taxname'
    }, {
        name: 'taxcode'
    },{
        name: 'totalsale'
    },{
        name: 'taxrate'
    }, {
        name: 'taxamount'
    }, {
        name: 'taxcollected'
    }, {
        name: 'taxpayable'
    }, {
        name: 'mergedCategoryData'
    }, {
        name: 'mergedResourceData'
    }, {
        name: 'total'
    }, {
        name: 'totalcategorycost'
    }, {
        name: 'categoryName'
    },{
        name: 'invdate'
    },{
    },{
        name: 'taxamount'
    },{
    },{
        name: 'invno'
    },{
    },{
        name: 'totalinvamt'
    },{
    },{
        name: 'invamt'
    },{
    },{
        name: 'invtaxamount'
    },{
    },{
        name: 'journalEntryNo'
    },{
        name: 'invname'
    }
    ,{
        name: 'currencyid'
    }
    ,{
        name: 'currencysymbol'
    }
    ,{
        name: 'currencyname'
    }
    ,{
        name: 'currencycode'
    }
    ,{
        name: 'gramtexcludingtax'
    },{
        name: 'addAmountFlag'
    }
    ,{
        name: 'totalgramtexcludingtax'
    },{
        name: 'jeid'
    },{
        name: 'jedate'
    },{
        name: 'billid'
    },{
        name: 'type'
    },{
        name: 'noteid'
    },{
        name: 'originalamountincludingtax'
    },{
        name: 'transactionexchangerate'
    },{
        name: 'transactioncurrencysymbol'
    },{
        name: 'originaltaxamount'
    },{
        name: 'jedate'
    },{
        name: 'inputType'
    },{
        name: 'billid'
    },{
        name: 'noteno'
    },{
        name: 'currencyid'
    },{
        name: 'date'
    },{
        name: 'memo'
    },{
        name: 'includeprotax'
    },{
        name: 'lasteditedby'
    },{
        name: 'costcenterid'
    },{
        name: 'agentid'
    },{
        name: 'sequenceformatid'
    },{
        name: 'personid'
    },{
        name: 'personname'
    },{
        name: 'agent'
    },{
        name: 'gTaxId'
    },{
        name: 'notetax'
    },{
        name:'salesPerson'
    },{
        name:'salesPerson'
    },{
        name:'isLeaseFixedAsset'
    }
    ]);
    
    this.groupStore = new Wtf.ux.grid.MultiGroupingStore({
        url : "ACCReports/getGSTReportForGrid.do",
        //            url: 'test.jsp',          
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, budgetRecord),
        autoLoad: false,
        groupField: ['mergedCategoryData', 'mergedResourceData']
    });
    this.groupStore.on('load', function() {
        WtfGlobal.resetAjaxTimeOut();
        if(this.groupStore.data.items.length>0&&this.groupStore.data.items[0].data!=undefined&&this.groupStore.data.items[0].data.currencysymbol!=undefined)
            for(var i=0;i<this.budgetCM.config.length;i++){
                if(this.budgetCM.config[i].dataIndex=="invamt"&&this.groupStore.data.items.length>0&&this.groupStore.data.items[0].data!=undefined&&this.groupStore.data.items[0].data.currencysymbol!=undefined&&this.groupStore.data.items[0].data.currencysymbol!=""){
                    this.budgetCM.setColumnHeader(i, 'Amount With Tax'+ " ("+this.groupStore.data.items[0].data.currencyname+")")
                }
                if(this.budgetCM.config[i].dataIndex=="invtaxamount"&&this.groupStore.data.items.length>0&&this.groupStore.data.items[0].data!=undefined&&this.groupStore.data.items[0].data.currencysymbol!=undefined&&this.groupStore.data.items[0].data.currencysymbol!=""){
                    this.budgetCM.setColumnHeader(i, 'Tax Amount'+ " ("+this.groupStore.data.items[0].data.currencyname+")")
                }
                if(this.budgetCM.config[i].dataIndex=="gramtexcludingtax"&&this.groupStore.data.items.length>0&&this.groupStore.data.items[0].data!=undefined&&this.groupStore.data.items[0].data.currencysymbol!=undefined&&this.groupStore.data.items[0].data.currencysymbol!=""){
                    this.budgetCM.setColumnHeader(i, 'Amount Without Tax'+ " ("+this.groupStore.data.items[0].data.currencyname+")")
                }
            }
        this.storeloaded(this.groupStore);    
        
        mainPanel.loadMask.hide();
        this.grid.getView().refresh();
    }, this);
    this.groupStore.on('loadexception', function() {
        WtfGlobal.resetAjaxTimeOut();
        mainPanel.loadMask.hide();
        this.grid.getView().refresh();
    }, this);

    this.groupStore.on('beforeload', function() {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        mainPanel.loadMask.show();
    }, this);

    this.budgetCM = new Wtf.grid.ColumnModel([
    {
        hidden: true,
        header: WtfGlobal.getLocaleText("acc.bankBook.gridDate"),
        fixed: true,
        dataIndex: 'taxcode',
        pdfwidth:100
    },{
        header: WtfGlobal.getLocaleText("acc.setupWizard.taxnam"),
//        fixed: true,
        dataIndex: 'taxcode',
        width: 120,
        pdfwidth:100
    },{
        header: WtfGlobal.getLocaleText("acc.bankBook.gridDate"),
        //fixed: true,
        dataIndex: 'invdate',
        width: 100,
        pdfwidth:100//,
    //            renderer:function(v){
    //                return (new Date(v).format(WtfGlobal.getOnlyDateFormat()));
    //            }
    },{
        header: WtfGlobal.getLocaleText("acc.field.TransactionID"),
        //fixed: true,
        dataIndex: 'invno',
        width: 150,
        pdfwidth:100,
        renderer:function(value,meta,rec){
            meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Transaction ID' ";
            if(!value || rec.data.type == 'Tax Adjustment') return "<div style= 'margin:-2px 14px'>"+value+"</div>"; 
            value = WtfGlobal.linkRenderer(value,meta,rec)
            return value;
        }
    },{
        header: WtfGlobal.getLocaleText("acc.prList.JEno"),
        //   fixed: true,
        dataIndex: 'journalEntryNo',
        width: 150,
        pdfwidth:100,
        renderer:WtfGlobal.jERendererForGST
    },{
        header: WtfGlobal.getLocaleText("acc.userAdmin.name"),
        // fixed: true,
        dataIndex: 'invname',
        width: 150,
        pdfwidth:100,
           summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
    },{
        header: WtfGlobal.getLocaleText("acc.common.memo"),
        dataIndex:'memo',
        renderer: WtfGlobal.memoRenderer,
        width:150,
        pdfwidth:100
    },{
        /*
         * ERP-28048 
         * GST report will always be in country currency. So chaning the currency sumbo
         */
        header: WtfGlobal.getLocaleText("acc.invoice.amountWithoutTax")+ " ("+(Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE ? Wtf.CurrencyName.SGD : Wtf.CurrencyName.MYR)+")",
        //   fixed: true,
        dataIndex: 'gramtexcludingtax',
        width: 150,
        pdfwidth:100,
        renderer:this.formatMoneyCountryWise,
        summaryType:'sum',
        pdfrenderer:"rowcurrency",
        summaryRenderer: function(value,m,rec){
            var v=parseFloat(value);
            if(isNaN(v)) return value;
            var retVal = WtfGlobal.withoutRateCountryWiseCurrencySymbol(value,m,rec);
            return '<b>'+retVal+'</b>';
        }
    },
    {
        header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount")+ " ("+(Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE ? Wtf.CurrencyName.SGD : Wtf.CurrencyName.MYR)+")",
        // fixed: true,
        dataIndex: 'invtaxamount',
        width: 150,
        pdfwidth:100,
        renderer:this.formatMoneyCountryWise,
        summaryType:'sum',
        pdfrenderer:"rowcurrency",
        summaryRenderer: function(value,m,rec){
            var v=parseFloat(value);
            if(isNaN(v)) return value;
            var retVal = WtfGlobal.withoutRateCountryWiseCurrencySymbol(value,m,rec)
            return '<b>'+retVal+'</b>';
        //                 if(value > 0){
        //                    var retVal = WtfGlobal.withoutRateCurrencySymbol(value,m,rec)
        //                    return '<b>'+retVal+'</b>';
        //                 }else{
        //                     return '';
        //                 }
        }
    },
    {
        header: WtfGlobal.getLocaleText("acc.field.PurchasesSalesValue") + " ("+(Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE ? Wtf.CurrencyName.SGD : Wtf.CurrencyName.MYR)+")",
        //    fixed: true,
        dataIndex: 'invamt',
        width: 150,
        renderer:this.formatMoneyCountryWise,
        summaryType:'sum',
        pdfwidth:100,
        pdfrenderer:"rowcurrency",
        summaryRenderer: function(value,m,rec){
            var v=parseFloat(value);
            if(isNaN(v)) return value;
            var retVal = WtfGlobal.withoutRateCountryWiseCurrencySymbol(value,m,rec)
            return '<b>'+retVal+'</b>';
        //                 if(value >0){
        //                var retVal = WtfGlobal.withoutRateCurrencySymbol(value,m,rec)
        //                return '<b>'+retVal+'</b>';
        //                 }else{
        //                     return '';
        //                 }
        }
    },
    {
        header: WtfGlobal.getLocaleText("acc.setupWizard.curEx"),  
        hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.SINGAPORE,
        dataIndex: 'transactionexchangerate',
        width: 150,            
        pdfwidth:100            
    },
    {
        header: WtfGlobal.getLocaleText("acc.gstreport.amtInTransactionCurrency"),         
        dataIndex: 'originalamountincludingtax',
        hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.SINGAPORE,
        width: 150,            
        pdfwidth:100,
        renderer:this.formatMoneyWithTransactionCurrency,
        pdfrenderer:"rowcurrency"
    },
    {
        header: WtfGlobal.getLocaleText("acc.field.mergedcategory"),
        //  fixed: true,
        dataIndex: 'mergedCategoryData'
    }, {
        header: WtfGlobal.getLocaleText("acc.field.mergedcategory"),
        //    fixed: true,
        dataIndex: 'mergedResourceData'
    //                ,renderer:function(v, m, r){
    //                    return v + '[Total:' + r.get('total') + ']';
    //                }
    }]);

    var groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: false,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal:false,
        isGroupTotal:true,
        hideGroupedColumn: true,
        emptyText: '<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>',
        groupTextTpl: '{group} '
    });
    var gridSummary = new Wtf.grid.GroupSummary({});
    var bottomArray=[];
    this.grid = new Wtf.ux.grid.MultiGroupingGrid({
        //        id: 'mGrid' + this.pid,
        layout:'fit',
        store:this.groupStore,
        cm: this.budgetCM,
        cls: 'colWrap',
        plugins:[gridSummary],
        view: groupView,
        sm: new Wtf.grid.RowSelectionModel({
            singleSelect: true
        })
    });
    
    this.grid.on('cellclick',this.onCellClick, this);
    this.grid.on('columnresize',this.resizeTablayout, this);
    this.grid.on('bodyresize',this.resizeTablayout, this);
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: Wtf.MultiEntityReportsModuleIdArray.split(','),
        reportid:Wtf.autoNum.EntityBasedGSTReport,
        advSearch: false,
        parentPanelSearch: this,
        ignoreDefaultFields: true,
        isAvoidRedundent: true,
        hideRememberSerch : true,
        isMultiEntity : (Wtf.Countryid == Wtf.Country.SINGAPORE) ? false : true  // flag to fetch only multi entity dimension in advance search.
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.startDate=new Wtf.ExDateFieldQtip({
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        // readOnly:true,
        allowBlank:false,
        value:this.getDates(true)
    });
  
    this.endDate=new Wtf.ExDateFieldQtip({
        name:'enddate',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        allowBlank:false,
        value:this.getDates(false)
    });
//    this.startDate.on('change',function(field,newval,oldval){
//        if(field.getValue()!='' && this.endDate.getValue()!=''){
//            if(field.getValue().getTime()>this.endDate.getValue().getTime()){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
//                field.setValue(oldval);                    
//            }
//        }
//    },this);
//        
//    this.endDate.on('change',function(field,newval,oldval){
//        if(field.getValue()!='' && this.startDate.getValue()!=''){
//            if(field.getValue().getTime()<this.startDate.getValue().getTime()){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
//                field.setValue(oldval);
//            }
//        }
//    },this);
    this.fetchButton = new Wtf.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.loadStore
    });
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls:'pwnd toggleButtonIcon',
        scope:this,
        handler: function(){
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton",
        hidden: !(Wtf.account.companyAccountPref.isMultiEntity || Wtf.Countryid == Wtf.Country.SINGAPORE)
    });
    this.exportPdf = new Wtf.Button({
        anchor : '90%',
        text: WtfGlobal.getLocaleText("acc.field.ExportPDF"),
        iconCls:'pwnd exportpdf1',
        handler:this.exportPDT
                
    });
    this.expCsvButton=new Wtf.exportButton({
        obj:this,
        //            disabled:true,
        tooltip :WtfGlobal.getLocaleText("acc.common.Csv.exportTT"),  //'Export report details.',
        id:"exportGSTReport",
            menuItem:{csv:true,pdf:false,rowPdf:false,xls:true},
        get:916,
        filename: WtfGlobal.getLocaleText("acc.taxReport.GSTReport"),
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        disabled:true
    });
    this.expInOutCsvButton=new Wtf.exportButton({
        obj:this,
        tooltip :WtfGlobal.getLocaleText("acc.common.Csv.exportTT"),  //'Export report IN OUT details.',
        id:"exportGSTReport1",
            menuItem:{csv:true,pdf:false,rowPdf:false,xls:true},
            params:{isInOut:true},
        get:916,
        text: WtfGlobal.getLocaleText("acc.field.ExportGSTInOUT"),
        filename: WtfGlobal.getLocaleText("acc.taxReport.GSTReport"),
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        disabled:true
    });

    var btnArray = [];
    btnArray.push(new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text : WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
        scope: this,
        reporttype:1,
        handler:this.exportPDFData
    }))
    btnArray.push(new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text : WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
        scope: this,
        reporttype:11,
        handler:this.exportPDFData
    }))
    var btn=new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text :WtfGlobal.getLocaleText("acc.field.ExportGSTDetailed"),
        scope: this,
        menu: btnArray
    });
            
    this.exportPdfButton = new Wtf.Button({
        id: 'exportButton',
        text: WtfGlobal.getLocaleText("acc.common.exportToPDF"),
        tooltip: WtfGlobal.getLocaleText("acc.common.pdf.exportTT"), //'Export report details.',
        iconCls:'pwnd exportpdf1',     
        scope:this,
        disabled:true,
        menu: {
            scope:this,
            items: [btn , {
                text: WtfGlobal.getLocaleText("acc.field.ExportGSTSummarised"),
                iconCls: 'pwnd exportpdf',
                reporttype:2,
                scope:this,
                handler: this.exportPDFData
            }
            ]
        }

    });
    
    bottomArray.push('-',this.expCsvButton, '-',this.expInOutCsvButton,'-',this.exportPdfButton);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.field.SearchbyTaxNameTransactionIDJournalEntryNoName"),
        width: 200,
        field: 'taxname'
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);

    this.taxYearStore = new Wtf.data.SimpleStore({
        fields: [{name: 'id'}, {name: 'name'},{name:'entrydate'},{name:'enddate'}]
     });
  
    this.taxPeriodStore = new Wtf.data.SimpleStore({
         fields: [{name: 'id'}, {name: 'name'},{name:'entrydate'},{name:'enddate'}]
    });

    this.TaxYear  = new Wtf.form.ComboBox({
        displayField:'name',
        valueField:'id',
        name:'id',
        mode: 'local',
        store:this.taxYearStore,
        emptyText:WtfGlobal.getLocaleText("acc.field.Selecttaxyear"),
        triggerAction: 'all',
//        allowBlank: false,
        typeAhead:true,
        width: 150,
        listWidth: 400,
        listeners:{
            scope:this,
            beforequery: function() {
                this.TaxYear.clearValue();// it clears the combo but value is still there.
                this.TaxYear.reset();//this reset the value to blank
                this.setTaxYear(Wtf.TaxAccountingPeriods.YEAR);    
            },
            select: function(combo, record, index) {
                this.setTaxPeriod(); 
                this.startDate.setValue(record.data.entrydate);
                this.endDate.setValue(record.data.enddate);
            }
        }
    });

    this.TaxPeriod = new Wtf.form.ComboBox({
        displayField:'name',
        valueField:'id',
        store:this.taxPeriodStore,
        name:'id',
        mode: 'local',
        emptyText:WtfGlobal.getLocaleText("acc.field.Selectataxperiod"),
        triggerAction: 'all',
//        allowBlank: false,
        typeAhead:true,
        width: 150,
        listWidth: 400,
        listeners:{
            scope:this,
            beforequery: function() {
                if(this.TaxYear.getValue()==""){
                    this.TaxPeriod.clearValue();
                    this.taxPeriodStore.loadData("");
                }
            },
            select: function(combo, record, index) {
                this.startDate.setValue(record.data.entrydate);
                this.endDate.setValue(record.data.enddate);
            }
        }
    });
    this.tbarArr = [this.quickPanelSearch, this.resetBttn, WtfGlobal.getLocaleText("acc.common.taxyear"), this.TaxYear, WtfGlobal.getLocaleText("acc.common.taxperiod"), this.TaxPeriod,
        WtfGlobal.getLocaleText("acc.common.from"), this.startDate, "-", WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate, "-", this.fetchButton, '-', this.expandCollpseButton, this.AdvanceSearchBtn]
    
    Wtf.apply(this, {
        border: false,
        layout: "border",
        items: [this.objsearchComponent,
            {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.tbarArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.groupStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id: "pPageSize_" + this.id
                    }),
                    items: bottomArray
                })
            }]
    });
    
    Wtf.account.GSTReportTab.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.groupStore.on("beforeload", function(s, o) {    //ERP-9201
        o.params.stdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        o.params.withoutinventory=Wtf.account.companyAccountPref.withoutinventory;
        o.params.ss = this.quickPanelSearch.getValue();
        
    }, this);
    
    /*Strore is not loading when opening the report first time.Data will load after clicking on "Fetch" button */
    
//    this.groupStore.load({
//        params:{
//            isFirstTimeLoad:true,//when clicking on report initially to load. 
//            start:0,
//            limit:30
//        }
//    });
}

Wtf.extend(Wtf.account.GSTReportTab, Wtf.Panel, {
    loadStore: function() {
          if(this.startDate.getValue()!='' && this.endDate.getValue()!=''){
             if(this.startDate.getValue().getTime()>this.endDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                return;
            }
        }  
        this.groupStore.load({
            params: {
               start : 0,
               limit :  this.pP.combo!=undefined?this.pP.combo.value:30,
               ss : this.quickPanelSearch.getValue()
            }
        });
        if((this.startDate.getValue()=="" && this.endDate.getValue()=="")||(this.startDate.getValue()!="" && this.endDate.getValue()=="")||(this.startDate.getValue()=="" && this.endDate.getValue()!="")){
            this.exportPdfButton.disable();
            this.expInOutCsvButton.disable();
            this.expCsvButton.disable();
            this.grid.getView().emptyText='<div class="emptyGridText">' + WtfGlobal.getLocaleText('acc.feild.selectdate') + ' <br></div>'
        }else{
            this.exportPdfButton.enable();
            this.expInOutCsvButton.enable();
            this.expCsvButton.enable();
            if(this.groupStore.getTotalCount.length==0){
                this.grid.getView().emptyText='<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>'
            }
            
        }
    },
    setTaxYear: function(periodType) {//Populating fields in Sub Periods form.
        Wtf.Ajax.requestEx({
            url: "accPeriodSettings/getParentTaxPeriods.do",
            method: 'POST',
            params: {
                periodtype: periodType
            }
        },this, function(response){
            if (response.success) {
                var resData = response.data;
                var arr2 = [];
                for (var i = 0; i < resData.length; i++) {
                    if(resData[i]!=undefined && resData[i]!='undefined'){
                        var data = resData[i];
                        var arr1 = [data.id,data.name,data.entrydate,data.enddate]
                        arr2.push(arr1);
                    }
                }
                this.taxYearStore.loadData(arr2);
            }
        });
    },
    setTaxPeriod: function() {//Populating fields in Sub Periods form.
        Wtf.Ajax.requestEx({
            url: "accPeriodSettings/getTaxPeriods.do",
            method: 'POST',
            params: {
                subperiodOf:this.TaxYear.getValue()
            }
        },this, function(response){
            if (response.success) {
                var resData = response.data;
                var arr2 = [];
                for (var i = 0; i < resData.length; i++) {
                    if(resData[i]!=undefined && resData[i]!='undefined'){
                        var data = resData[i];
                        var arr1 = [data.id,data.periodname,data.startdate,data.enddate]
                        arr2.push(arr1);
                    }
                }
                this.taxPeriodStore.loadData(arr2);
            }
        });
    },
    
    formatMoney:function(val,m,rec,i,j,s){
        //        var fmtVal=WtfGlobal.currencyRenderer(val);
        //        var fmtVal=WtfGlobal.withoutRateCurrencySymbolForGST(val,m,rec);
        var fmtVal=WtfGlobal.withoutRateCurrencySymbolForGSTFM5(val,m,rec);
        return fmtVal;
    },
    /*
     * Added a new formatter for appending the country currency symbol
     */
    formatMoneyCountryWise:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.withoutRateCountryWiseCurrencySymbol(val,m,rec);
        return fmtVal;
    },
    formatMoneyWithTransactionCurrency:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.withoutRateCurrencySymbolForGSTFM5withTransactionCurrency(val,m,rec);
        return fmtVal;
    },
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    }, 
    storeloaded: function(store) {
        this.quickPanelSearch.StorageChanged(store);
        if (store.getCount() == 0) {
            if (this.exportPdfButton)
                this.exportPdfButton.disable();
            if (this.expInOutCsvButton)
                this.expInOutCsvButton.disable();
            if (this.expCsvButton)
                this.expCsvButton.disable();
        } else {
            if (this.exportPdfButton)
                this.exportPdfButton.enable();
            if (this.expInOutCsvButton)
                this.expInOutCsvButton.enable();
            if (this.expCsvButton)
                this.expCsvButton.enable();
        }
    },
    getDates: function(start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    exportPDFData: function(type) { //ERP-9201
        var url = "ACCReports/exportGSTReport.do?stdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue()) + "&reportType=" + type.reporttype + "&withoutinventory=" + Wtf.account.companyAccountPref.withoutinventory+"&taxYearName="+this.TaxYear.getRawValue()+"&taxPeriodName="+this.TaxPeriod.getRawValue();
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityId=" + this.objsearchComponent.advGrid.getSearchJSON().searchText;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
    
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        var formrec = this.grid.getStore().getAt(i);
        if(dataindex == "invno"){
            var type=formrec.data['type'];
            var withoutinventory=formrec.data['withoutinventory'];  
            var billid=formrec.data['billid'];
            if (type == "Debit Note" || type == "Credit Note") {
                billid = formrec.data['noteid'];
            }
            if(type == 'Journal Entry'){
                this.callJEReportAndExpandJE(formrec,e);
            } else {
                if(formrec.data['inputType']=='5'&&type == "Credit Note"){
                     callViewCreditNoteGst(true,formrec,false,false,true);
                }else if(formrec.data['inputType']=='5'&&type == "Debit Note"){
                     callViewCreditNoteGst(true,formrec,false,false,false);
                }else{
                    viewTransactionTemplate1(type, formrec,withoutinventory,billid);            
                }
            }    
        }else if(dataindex=='journalEntryNo'){
            this.callJEReportAndExpandJE(formrec,e);
        }
    },
    
    resizeTablayout:function(){
        var records = this.groupStore.getRange();
        this.groupStore.removeAll();
        this.groupStore.add(records);  
    },
    
    callJEReportAndExpandJE:function(formrec,e){
        var jeid=formrec.data['jeid']; 
        var jestartdate=this.startDate.getValue();
        var jeentrydate=this.endDate.getValue();
        if(e.target.getAttribute('jedate')!=undefined && e.target.getAttribute('jedate')!="") {  // multiple links in single row
            jeentrydate= new Date(e.target.getAttribute('jedate'));
            jestartdate= new Date(e.target.getAttribute('jedate'));
            jestartdate = new Date(jestartdate.setDate(jeentrydate.getDate()-1));
            jeentrydate = new Date(jeentrydate.setDate(jeentrydate.getDate()+1));
        }
        this.fireEvent('journalentry',jeid,true,this.consolidateFlag,null,null,null,jestartdate, jeentrydate);
    },
     expandCollapseGrid : function(btntext){
        if(btntext == WtfGlobal.getLocaleText("acc.field.Collapse")){
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.groupStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.groupStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.groupStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.groupStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    }
});

////////////////////////////////////////////// GST FORM 5 HIERARCHICAL GRID
Wtf.account.GSTForm5HierarchyTab=function(config){
    this.summary = new Wtf.ux.grid.GridSummary();
    this.ReportType="GSTForm5";
    
    var budgetRecord = Wtf.data.Record.create([
    {
        name: 'taxname'
    },  {
        name: 'taxamount'
    },  {
        name: 'mergedCategoryData'
    },  {
        name: 'box'
    },  {
        name: 'fmt'
    },  {
        name: 'level'
    },
    {
        name: 'currencyid'
    }
    ,{
        name: 'currencysymbol'
    }
    ,{
        name: 'currencyname'
    }
    ]);

    this.groupStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"                          
        }, budgetRecord),
        baseParams:{
        // nondeleted:true
        },
        url: "ACCReports/getGSTForm5.do"
    });
    
    this.groupStore.on('beforeload',function(){
        WtfGlobal.setAjaxTimeOutFor30Minutes();
    },this);
    this.groupStore.on('load', function() {
        WtfGlobal.resetAjaxTimeOut();
        if (this.groupStore.data.items.length > 0 && this.groupStore.data.items[0].data != undefined && this.groupStore.data.items[0].data.currencysymbol != undefined && this.groupStore.data.items[0].data.currencysymbol != "") {
            for (var i = 0; i < this.grid.getColumnModel().config.length; i++) {
                if (this.grid.getColumnModel().config[i].dataIndex == "taxamount" && this.groupStore.data.items.length > 0 && this.groupStore.data.items[0].data != undefined && this.groupStore.data.items[0].data.currencysymbol != undefined) {
                    this.grid.getColumnModel().setColumnHeader(i, this.groupStore.data.items[0].data.currencyname)
                }
            }
            this.gstForm5eSubmission.enable();
        }
        mainPanel.loadMask.hide();
        this.grid.getView().refresh();
    }, this);
    
    this.groupStore.on('loadexception', function() {
        WtfGlobal.resetAjaxTimeOut();
        mainPanel.loadMask.hide();
        this.grid.getView().refresh();
    }, this);

    //    this.groupStore.on('beforeload', function() {
    //        mainPanel.loadMask.show();
    //    }, this);

    var budgetCM = new Wtf.grid.ColumnModel([
    {
        header: '',
        fixed: true,
        dataIndex: 'mergedCategoryData'
    },{
        header: WtfGlobal.getLocaleText('acc.product.description'),
        fixed: true,
        dataIndex: 'taxname',
        width: 800,
        pdfwidth:800,
        align: 'right'
    },{
        header: WtfGlobal.getLocaleText('acc.dnList.gridAmt'),
        fixed: true,
        dataIndex: 'taxamount',
        width: 100,
        pdfwidth:100,
        renderer: this.formatMoney,
        align:'center'
    },{
        header: WtfGlobal.getLocaleText("acc.field.BOX"),
        fixed: true,
        dataIndex: 'box',
        width: 50,
        pdfwidth:50,
        align:'left'
    }
    ]);

    var groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: true,
        showGroupName: false,
        enableNoGroups: false,
        isGrandTotal:false,
        isGroupTotal:true,
        hideGroupedColumn: true,
        emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>',
        groupTextTpl: '{group} '
    });

    var gridSummary = new Wtf.grid.GroupSummary({});

    this.grid = new Wtf.grid.HirarchicalGridPanel({
        autoScroll:true,
        store: this.groupStore,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.report.2")+'</b>',
            dataIndex:'taxname',
            renderer:this.formatAccountName,
            width: 600
        },{
            header:"<div align=center><b>"+"S$"+"</b></div>",
            dataIndex:'taxamount',
            renderer:this.formatMoney
        },{
            header:"<div align=left><b>"+WtfGlobal.getLocaleText("acc.field.BOX")+"</b></div>",
            dataIndex:'box'
        // renderer:this.formatBox,
        }],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText: '<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>'
        }
    });

    this.grid.on("render", WtfGlobal.autoApplyHeaderQtip);

    this.grid.on('rowclick',this.onRowClickGrid, this);

    this.grid.on('render',function(){
        this.grid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.grid],1);
        this.grid.getView().applyEmptyText();
    // this.expandCollapseGrid("Expand");
    },this);
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: Wtf.MultiEntityReportsModuleIdArray.split(','),
        reportid: this.reportid,
        advSearch: false,
        parentPanelSearch: this,
        ignoreDefaultFields: true,
        isAvoidRedundent: true,
        isMultiEntity: (Wtf.Countryid == Wtf.Country.SINGAPORE) ? false : true,    // flag to fetch only multi entity dimension in advance search.
        hideRememberSerch: true
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    // this.grid.on('load',function(){
    //     // this.expandCollapseGrid("Expand");
    // },this);

    // this.grid.getStore().on("load", function(){
    //     // for(var i=0; i< this.grid.getStore().data.length; i++){
    //     //     this.grid.expandRow(this.grid.getView().getRow(i));
    //     // }
    // }, this);    

    this.startDate=new Wtf.ExDateFieldQtip({
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        // readOnly:true,
        value:this.getDates(true)
    });

    this.endDate=new Wtf.ExDateFieldQtip({
        name:'enddate',
        format:WtfGlobal.getOnlyDateFormat(),
        //    readOnly:true,
        value:this.getDates(false)
    });

//    this.startDate.on('change',function(field,newval,oldval){
//        if(field.getValue()!='' && this.endDate.getValue()!=''){
//            if(field.getValue().getTime()>this.endDate.getValue().getTime()){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
//                field.setValue(oldval);                    
//            }
//        }
//    },this);
//        
//    this.endDate.on('change',function(field,newval,oldval){
//        if(field.getValue()!='' && this.startDate.getValue()!=''){
//            if(field.getValue().getTime()<this.startDate.getValue().getTime()){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
//                field.setValue(oldval);
//            }
//        }
//    },this);
        
    this.fetchButton = new Wtf.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.loadStore
    });
    
    this.gstForm5eSubmission = new Wtf.Button({
        text: 'GST Form 5 e-Submission',
        tooltip: 'To enable button click on fetch the data first',
        disabled: true,
        iconCls: 'accountingbase reportsBtnIcon',
        scope: this,
        hidden:!Wtf.account.companyAccountPref.columnPref.irasIntegration,//Temperory
        handler: function () {
            
                var params = {
                    id: this.id,
                    searchJson: this.searchJson,
                    filterConjuctionCrit: this.filterConjuctionCrit,
                    objsearchComponent: this.objsearchComponent,
                    startDate: this.startDate,
                    endDate: this.endDate,
                    document: document,
                    gstStore: this.groupStore
                };
                callGSTForm5eSubmissionDetailsForm(params);
            
            
        }
    });

    this.detailedView = new Wtf.Button({
        text:WtfGlobal.getLocaleText("acc.field.DetailedView"),
        tooltip: WtfGlobal.getLocaleText("acc.field.ViewGSTForm5indetailedview"),
        iconCls:'advanceSearchButton',
        scope:this,
        handler:this.viewDetailHandler
    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton",
        hidden: !(Wtf.account.companyAccountPref.isMultiEntity || Wtf.Countryid == Wtf.Country.SINGAPORE)
    });
    var mnuBtns=[];
    this.expCsvButton = new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate()
        }
    });
    mnuBtns.push(this.expCsvButton);
    this.xlsCsvButton = new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        handler:function(){
            this.exportWithTemplate("xls")
        }
    });
    mnuBtns.push(this.xlsCsvButton);
    this.exportButton5=new Wtf.Button({
        scope:this,
        id:"exportReports"+config.helpmodeid, //+this.id,
        iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        menu:mnuBtns
    });
    this.exportPdfButton = new Wtf.Button({
        id: 'exportGSTForm5PDF',
        text: WtfGlobal.getLocaleText("acc.common.exportToPDF"),
        tooltip: WtfGlobal.getLocaleText("acc.common.pdf.exportTT"),
        //text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.pdf.exportTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
        iconCls:'pwnd exportpdf1',     
        scope:this,
        reporttype:4,
        handler: this.exportPDFData
    // menu: {
    //     scope:this,
    //     items: [
    //         {
    //             text: 'Export GST Form 5',
    //             iconCls: 'pwnd exportpdf',
    //             reporttype:3,
    //             scope:this,
    //             handler: this.exportPDFData
    //         }
    //     ]
    // }
    });

    this.printButton = new Wtf.Button({
        iconCls:'pwnd printButtonIcon',
        text :WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details.',
        disabled :false,
        hidden :false,
        scope: this,
        handler:function(){
            this.exportWithTemplate("print")
        }
    });    

    this.tbarArr = [WtfGlobal.getLocaleText("acc.common.from"), this.startDate, "-", 
    WtfGlobal.getLocaleText("acc.common.to"), this.endDate, "-", 
    this.fetchButton,'-',this.exportButton5,'-',this.exportPdfButton, '-', this.printButton,'-',this.detailedView,'-',this.AdvanceSearchBtn,'-',this.gstForm5eSubmission];

    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        defaults:{border:false,bodyStyle:"background-color:white;"},
        items:[{layout: 'fit', region: 'north', tbar: this.tbarArr},
        this.westPanel = new Wtf.Panel({
            width:'60%',
            region:'center',
            layout:'fit',
            border:true,
            items:this.grid
        }),
        {layout:'fit',region:'west',width:'20%',border:true},{layout:'fit',region:'east',width:'20%',border:true}]
    });
    Wtf.apply(this, {
        border: false,
        layout: "border",
        saperate: true,
        statementType: "GSTForm5",
        items: [this.objsearchComponent,
            {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.wrapperPanel]
            }]
    },config);
    
    Wtf.account.GSTForm5HierarchyTab.superclass.constructor.call(this,config);

    this.addEvents({
        'journalentry':true
    });

    this.groupStore.on("beforeload", function(s, o) {
        o.params.stdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        o.params.withoutinventory=Wtf.account.companyAccountPref.withoutinventory;
        if(this.pP!=undefined){
            if(this.pP.combo.value=="All"){
                var count = this.groupStore.getTotalCount();
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
    
//    this.groupStore.load({
//        params:{
//            start:0,
//            limit:30
//        }
//    });
}

Wtf.extend(Wtf.account.GSTForm5HierarchyTab, Wtf.Panel, {
    onRowClickGrid:function(g,i,e){
        e.stopEvent();
    },        

    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
        }
        return grid.getRowClass()+colorCss;
    },    

    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=val;
        if(rec.data['fmt']=="B"){
            fmtVal='<span style="font-weight:bold;margin-left:0px;padding-left:20px;" unselectable="on"><font size=2px>'+fmtVal+'</font></span>';
        
        }else if(rec.data['fmt']=="radio"){
            fmtVal = '<span style="margin-left:0px;padding-left:40px;" unselectable="on"><font size=2px>' + 
            '<input type="radio" name="declaration' + m['value']+ '" value="Yes">  Yes   <input type="radio" name="declaration' + m['value']+ '" value="No">   No'
            '</span>';        

        }else if(rec.data['fmt']=="textbox"){
            fmtVal = '<span style="margin-left:0px;padding-left:40px;" unselectable="on"><font size=2px>' + fmtVal +
            '<input type="text" length=50  id="'+rec.json.id+'">'
            '</span>';   
            
        } else if(rec.data["level"]==0&&rec.data["taxname"]!="") {
            fmtVal='<span style="margin-left:0px;padding-left:40px" unselectable="on"><font size=2px>'+fmtVal+'</span>';
        
        } else if(rec.data["level"]==1&&rec.data["taxname"]!="") {
            fmtVal='<span style="margin-right:0px;padding-left:60px" unselectable="on"><font size=2px>'+fmtVal+'</font></span>';
        }
        return fmtVal;
    },    
    
    formatBox:function(val,m,rec,i,j,s){
        var fmtVal=val;
        fmtVal='<span style="padding-left:40px">'+fmtVal+'</span>';
        return fmtVal;
    },  

    formatMoney:function(val,m,rec,i,j,s){
        //        var fmtVal=WtfGlobal.currencyRenderer(val);
        var fmtVal=WtfGlobal.withoutRateCurrencySymbolForGSTFM5(val,m,rec);
        return fmtVal;
    },

    loadStore: function() {
          if(this.startDate.getValue()!='' && this.endDate.getValue()!=''){  
           if(this.startDate.getValue().getTime()>this.endDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                return;
            }
        }  
        this.groupStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    },
    
    viewDetailHandler: function() {
        var startDate = this.startDate.getValue();
        var enddate = this.endDate.getValue();
        GSTForm5DetailedView(startDate,enddate);
    },

    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    }, 

    storeloaded: function(store) {
        this.quickPanelSearch.StorageChanged(store);
        if (store.getCount() == 0) {
            if (this.exportButton)
                this.exportButton.disable();
            if (this.printButton)
                this.printButton.disable();
        } else {
            if (this.exportButton)
                this.exportButton.enable();
            if (this.printButton)
                this.printButton.enable();
        }
    },

    getDates: function(start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    exportWithTemplate:function(type){
        type = type?type:"csv";
        var nameDe=document.getElementById('id0').value;  //name of Declarant
        var id=document.getElementById('id1').value; //Designation Id
        var desg=document.getElementById('id2').value; //Designation
        var per=document.getElementById('id3').value; //Contact person
        var contact=document.getElementById('id4').value;//Contact No
        var header = "taxname,taxamount,box";        
        var title = WtfGlobal.getLocaleText("acc.report.2") + ",SG Dollar(SGD), BOX";            
        var exportUrl = "ACCReports/getGSTForm5Export.do";            
        var fileName =  WtfGlobal.getLocaleText("acc.taxReport.GSTForm5Report");
        var reportName = WtfGlobal.getLocaleText("acc.taxReport.GSTForm5Report")                       
        var align = "none,none,none";

        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+"&filetype="+type
        +"&stdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
        +"&nondeleted="+true+"&header="+header+
        "&title="+encodeURIComponent(title)+
        "&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&singleGrid="+true+"&gstF5DetailReport="+false+"&nameDe="+encodeURIComponent(nameDe)+"&id="+encodeURIComponent(id)+"&desg="+encodeURIComponent(desg)+"&per="+encodeURIComponent(per)+"&contact="+encodeURIComponent(contact);

        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityId=" + this.objsearchComponent.advGrid.getSearchJSON().searchText;
        }
        if(type == "print") {
            url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            Wtf.get('downloadframe').dom.src  = url;
        }
        Wtf.get('downloadframe').dom.src = url;
    },    

    exportPDFData: function(type) {
        var nameDe=document.getElementById('id0').value;  //name of Declarant
        var id=document.getElementById('id1').value; //Designation Id
        var desg=document.getElementById('id2').value; //Designation
        var per=document.getElementById('id3').value; //Contact person
        var contact=document.getElementById('id4').value;//Contact No
//        valOfTextField.push(document.getElementById('id1').value);
        var url = "ACCReports/exportGSTReport.do?stdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue()) + "&reportType=" + type.reporttype + "&withoutinventory=" + Wtf.account.companyAccountPref.withoutinventory +"&nameDe="+encodeURIComponent(nameDe)+"&id="+encodeURIComponent(id)+"&desg="+encodeURIComponent(desg)+"&per="+encodeURIComponent(per)+"&contact="+encodeURIComponent(contact);
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityId=" + this.objsearchComponent.advGrid.getSearchJSON().searchText;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
    callJEReportAndExpandJE:function(formrec,e){
        var jeid=formrec.data['jeid']; 
        var jestartdate=this.startDate.getValue();
        var jeentrydate=this.endDate.getValue();
        if(e.target.getAttribute('jedate')!=undefined && e.target.getAttribute('jedate')!="") {  // multiple links in single row
            jeentrydate= new Date(e.target.getAttribute('jedate'));
            jestartdate= new Date(e.target.getAttribute('jedate'));
            jestartdate = new Date(jestartdate.setDate(jeentrydate.getDate()-1));
            jeentrydate = new Date(jeentrydate.setDate(jeentrydate.getDate()+1));
        }
        this.fireEvent('journalentry',jeid,true,this.consolidateFlag,null,null,null,jestartdate, jeentrydate);
    },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.groupStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.groupStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.groupStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.groupStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    }
});
