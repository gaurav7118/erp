/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


function callDimensionsReportDynamicLoad(){
    var panel=Wtf.getCmp("dimensionreportpanela");
    if(panel==null){
        panel=new Wtf.account.DimensionsReportPanel({
            title:WtfGlobal.getLocaleText("acc.dimensionsReport.title"),
            tabTip:WtfGlobal.getLocaleText("acc.dimensionsReport.ttp"),
            id:'dimensionreportpanela',
            border:false,
            layout:'fit',
            closable:true
        //           iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
//*************************************************************************************

Wtf.account.DimensionsReportPanel=function(config){
    Wtf.apply(this, config);
    
    var buttonArray = new Array();
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.dimensionsReport.search"), // "Search by Document Account No
        width: 150,
        id:"quickSearch"+this.id
    });
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'startdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    
    this.fetchBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',           
        scope: this,
        id:'fetchBtn'+this.id,
        tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"), 
        handler: this.fetchData,
        iconCls:'accountingbase fetch'
    });
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
    this.expButton=new Wtf.exportButton({
        obj:this,
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
        id:"exportDimensionReport",
        filename:WtfGlobal.getLocaleText("acc.dimensionsReport.title")+"_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,xls:true
        },
        get:Wtf.autoNum.DimesionReport,
        label:WtfGlobal.getLocaleText("acc.dimensionsReport.title")
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"printBankBookSummary",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.DimesionReport,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
//        hidden:true
    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        //        id: 'advanced3', // In use, Do not delete
        scope: this,
        //        hidden:(this.moduleid !=undefined || (this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt) )?false:true ,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate,"-",this.fetchBtn,this.resetBttn,this.expButton,this.printButton,this.AdvanceSearchBtn);
    
    this.record=new Wtf.data.Record.create([
    {
        name:"date"
    },

    {
        name:"documentno"
    },

    {
        name:"documenttype"
    },

    {
        name:"description"
    },

    {
        name:"amount"
    },{
        name:"currencysymbol"
    }
      
    ]);  
    
    this.store=new Wtf.data.Store({
        reader:new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"  
        },this.record),
        url: "ACCReports/getDimensionsReport.do",
        params:{
            startdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }
    });
    
    this.store.on('load',function(store){
        this.quickPanelSearch.StorageChanged(store);
    },this);
    
    this.store.on('beforeload',function(s,o){
        o.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
        o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
    },this);
    
    WtfGlobal.setAjaxTimeOut();
    this.store.load({
        params: {
            startdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            start:0,
            limit:30
        }
    });
    
    this.store.on('datachanged',function(){
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },this);
    
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    
    this.grid = new Wtf.grid.GridPanel({
        store:this.store,
        sm:this.sm,
        border:false,
        layout:'fit',
        loadMask : true,
        viewConfig:{
            forceFit:true, 
            emptyText:WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.")
            },
        columns:[this.sm,this.rowNo,{
            header :WtfGlobal.getLocaleText("acc.bankBook.transNo"), //'Document No.',
            renderer:WtfGlobal.deletedRenderer,
            dataIndex: 'documentno',
            pdfwidth:200
        },{
            header :WtfGlobal.getLocaleText("acc.inventoryList.date"), //Date
            dataIndex: 'date' ,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.field.DocumentType"),  //" Document Type",
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            dataIndex: 'documenttype',
            width:100,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.contractActivityPanel.Description"),  //"Description",
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            dataIndex: 'description',
            width:100,
            pdfwidth:200
        },{
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            hidden:true,
            pdfwidth:85
        },{
            header: WtfGlobal.getLocaleText("acc.commission.Amount"),  //" Amount",
            dataIndex: 'amount',
            width:100,
            pdfwidth:200,
            align:'center',
            renderer:WtfGlobal.currencyRendererDeletedSymbol
        }]
    });
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: 102,//(this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt ==true ) ?  Wtf.Acc_Invoice_ModuleId:this.moduleid,
        advSearch: false,
        hideRememberSerch:true,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    Wtf.apply(this,{
        items:[{
            region:'center',
            layout:'border',
            border:false,
            items:[this.objsearchComponent,
            {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid]
            }]
        }],
        tbar:buttonArray,
        bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.store,           
            displayInfo: true,
            searchField: this.quickPanelSearch,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });
    
    Wtf.account.DimensionsReportPanel.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.DimensionsReportPanel,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.DimensionsReportPanel.superclass.onRender.call(this,config);   
    },        
    fetchData:function(){
        if(this.startDate.getValue() > this.endDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
            return;
        }
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
            }
        });
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
        this.store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: (this.isSalesCommissionStmt != undefined && this.isSalesCommissionStmt ==true ) ?  Wtf.Acc_Invoice_ModuleId:this.moduleid,
            isFixedAsset:this.isFixedAsset,
            isLeaseFixedAsset:this.isLeaseFixedAsset,
            filterConjuctionCriteria: filterConjuctionCriteria,
            pendingapproval: false //this.pendingapproval
        }
        this.store.load({
            params: {
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value,
                pagingFlag:true
            }
        });
},
  clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: this.moduleId,
                filterConjuctionCriteria: this.filterConjuctionCrit,
//                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },

handleResetClickNew:function(){
    this.quickPanelSearch.reset();
    this.startDate.reset();
    this.endDate.reset();
    this.fetchData();   
}
});