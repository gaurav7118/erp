function callCustomerReceivedReportDynamicLoad() {
    var panel = Wtf.getCmp("customerReceivedReport");
    if (panel == null) {
        panel = new Wtf.account.CustomerReceivedReport({
            title: WtfGlobal.getLocaleText("acc.common.customerReceivedReport"),
            tabTip: WtfGlobal.getLocaleText("acc.common.customerReceivedReport"),
            id: "customerReceivedReport",
            iconCls:'accountingbase coa',
            layout: 'fit',
            moduleid : Wtf.Acc_Receive_Payment_ModuleId,
            closable: true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


Wtf.account.CustomerReceivedReport=function(config){
    this.id = config.id;
    this.moduleid = config.moduleid;
    
    Wtf.apply(this, config);    
    this.createTBar();
    this.createDisplayGrid();
    
    
    Wtf.account.CustomerReceivedReport.superclass.constructor.call(this, config);

}
Wtf.extend(Wtf.account.CustomerReceivedReport,Wtf.Panel,{
    
    onRender: function(config) {
        
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.store,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id: "pPageSize_" + this.id
                    })
                })
            }]
        });
        this.add(this.leadpan);
        this.fetchRecords();
        Wtf.account.CustomerReceivedReport.superclass.onRender.call(this, config);
    },
    createDisplayGrid: function() {
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            });
        this.rowNo=new Wtf.KWLRowNumberer();
        var columnArray = [];
        columnArray.push(this.rowNo,
        {
            header: WtfGlobal.getLocaleText("acc.cust.name"),
            dataIndex: 'customerName',
            pdfwidth:75,
            sortable:true
        }, 
        {
            header: WtfGlobal.getLocaleText("acc.agedPay.gridIno"),
            dataIndex: 'invoiceNumber',
            pdfwidth:75,
            sortable:true,
            renderer:WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.rem.34"),
            dataIndex: 'invoiceDate',
            pdfwidth:75,
            align:'center',
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.het.178"),
            dataIndex: 'receiptNumber',
            pdfwidth:75,
            sortable:true,
            renderer:WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.agedPay.gridJEno"),
            dataIndex: 'receiptJENumber',
            pdfwidth:75,
            sortable:true,
            renderer:WtfGlobal.linkDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.het.179"),
            dataIndex: 'receiptDate',
            align:'center',
            pdfwidth:75,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },
        {
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            hidden:true,
            sortable:true,
            pdfwidth:85
        },
        {
            header: WtfGlobal.getLocaleText("acc.prList.amtRec"), 
            dataIndex: 'amountReceived',
            align:'right',
            pdfwidth:75,
            sortable:true,
            renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
        },
        {
            header: WtfGlobal.getLocaleText("acc.field.description"),
            dataIndex: 'description',
            pdfwidth:75,
            sortable:true,
            renderer: function(value) {
                value = value.replace(/\'/g, "&#39;");
                value = value.replace(/\"/g, "&#34");
                return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
            }
        }, 
        {
            header: WtfGlobal.getLocaleText("acc.labour.resourcecost.name"),
            dataIndex: 'resourceName',
            pdfwidth:75,
            sortable:true
        }
        );
        
        this.Rec = Wtf.data.Record.create([
        {
            name: 'customerName'
        },

        {
            name: 'invoiceDate',
            type: 'date'
        },

        {
            name: 'receiptDate',
            type: 'date'
        },

        {
            name: 'amountReceived',
            type:'float'
        },

        {
            name: 'amountdue',
            type:'float'
        },

        {
            name: 'invoiceNumber'
        },

        {
            name: 'receiptNumber'
        },

        {
            name: 'receiptJENumber'
        },

        {
            name: 'description'
        },

        {
            name: 'currencyname'
        },

        {
            name: 'currencyid'
        },

        {
            name: 'resourceName'
        },
        {
            name :'invoiceId'
        },
        {
            name :'billid'
        },
        {
            name : 'jeid'
        },
        {
            name : 'jedate'
        },
        {
            name: 'currencysymboltransaction'
        },
        {
            name :'currencycode'
        }
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.Rec),
            url:"ACCInvoiceCMN/getPaymentDetailsForInvoice.do",
            baseParams: {
                deleted: false,
                nondeleted: true
            }
        });
        
//        columnArray = WtfGlobal.appendCustomColumn(columnArray,GlobalColumnModel[this.moduleid]);
        columnArray = WtfGlobal.appendCustomColumnForReport(columnArray,GlobalColumnModel[Wtf.Acc_Invoice_ModuleId]);
        this.cm = new Wtf.grid.ColumnModel(columnArray );    
//        var  columnArray1  =  GlobalColumnModel[this.moduleid];
        var  columnArray1  =  GlobalColumnModel[Wtf.Acc_Invoice_ModuleId];
        WtfGlobal.updateStoreConfig(columnArray1, this.store);
//        WtfGlobal.updateStoreConfig(columnArray, this.store);
        
        this.store.on("loadexception", function() {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.mp.unableToLoadData")],1);
        }, this)
        this.store.on('load',this.storeLoaded,this);
        this.store.on('beforeload',function(store,option){
            var currentBaseParams = this.store.baseParams;
            currentBaseParams.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
            currentBaseParams.enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());  
            currentBaseParams.moduleid=this.moduleid;
            this.store.baseParams=currentBaseParams;
            
//            this.exportButton.setParams({
//            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
//            deleted: false,
//            nondeleted: true
//         });
        },this);
        this.store.on('datachanged', function(){        
            var p = this.pP?this.pP.combo.value:30;        
        }, this);        
        this.grid = new Wtf.grid.GridPanel({
            store: this.store,
            autoScroll: true,
            cm: this.cm,
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.grid.on('cellclick',this.onCellClick, this);
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        var formrec = this.grid.getStore().getAt(i);
        if(dataindex == "invoiceNumber"){
            var billid=formrec.data['invoiceId'];
            viewTransactionTemplate1("Sales Invoice", formrec,false,billid);                    
        }else if(dataindex=='receiptNumber'){
            viewTransactionTemplate("Payment Received", formrec);                    
        } else if(dataindex = 'receiptJENumber'){
            this.callJEReportAndExpandJE(formrec,e);
        }
    },
    callJEReportAndExpandJE:function(formrec,e){
        var jeid=formrec.data['jeid']; 
        var jestartdate=this.startDate.getValue();
        var jeentrydate=this.endDate.getValue();
        if(e.target.getAttribute('jedate')!=undefined && e.target.getAttribute('jedate')!="") {  
            jeentrydate= new Date(e.target.getAttribute('jedate'));
            jestartdate= new Date(e.target.getAttribute('jedate'));
            jestartdate = new Date(jestartdate.setDate(jeentrydate.getDate()-1));
            jeentrydate = new Date(jeentrydate.setDate(jeentrydate.getDate()+1));
        }
        this.fireEvent('journalentry',jeid,true,this.consolidateFlag,null,null,null,jestartdate, jeentrydate);
    },
    createTBar: function() {
        this.btnArr = [];
        this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'startdate',
            style:"margin-left: 15px;",
            format:WtfGlobal.getOnlyDateFormat(),
            value:WtfGlobal.getDates(true)
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            style:"margin-left: 15px;",
            name:'enddate',
            value:WtfGlobal.getDates(false)

        });    
        this.fetchBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip:this.isCN?WtfGlobal.getLocaleText("acc.cn.fetchTT"):WtfGlobal.getLocaleText("acc.dn.fetchTT"),  //"Select a time period to view corresponding credit/debit note records.",
            style:"margin-left: 15px;",
            iconCls:'accountingbase fetch',
            scope:this,
            handler:this.fetchRecords                        
        }); 
        this.exportButton=new Wtf.exportButton({
            obj:this,
            id:"exportReports"+this.id,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
//            usePostMethod:true,
            filename:WtfGlobal.getLocaleText("acc.common.customerReceivedReport")+"_v1", 
            menuItem:{csv:true,pdf:true,xls:true},
            get:Wtf.autoNum.CustomerReceivedReport
        })
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"), 
            filename:WtfGlobal.getLocaleText("acc.common.customerReceivedReport"),
            menuItem:{print:true},
            params:{name:WtfGlobal.getLocaleText("acc.common.customerReceivedReport")},
            label:WtfGlobal.getLocaleText("acc.common.customerReceivedReport"),
            get:Wtf.autoNum.CustomerReceivedReport
        })
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.cm,
            moduleid: this.moduleid,
            advSearch: false,
            ignoreDefaultFields: true
        });
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
            scope: this,
            hidden: (this.moduleid == undefined) ? true : false,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',this.fetchBttn,'-',this.exportButton,'-',this.printButton,'-',this.AdvanceSearchBtn);
        
        this.startDate.on('change',function(field,newval,oldval){
        if(field.getValue()!='' && this.endDate.getValue()!=''){
            if(field.getValue().getTime()>this.endDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                field.setValue(oldval);                    
            }
        }
       },this);
        
       this.endDate.on('change',function(field,newval,oldval){
        if(field.getValue()!='' && this.startDate.getValue()!=''){
            if(field.getValue().getTime()<this.startDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                field.setValue(oldval);
            }
        }
      },this);
    
    },
    storeLoaded : function() {
        this.grid.getView().refresh();
    },
    fetchRecords : function(){
        this.store.load({
            params :{
                start: 0,
                limit: (this.pP == undefined)?30:(this.pP.combo == undefined) ? 30 : this.pP.combo.value
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
            moduleid: this.moduleid,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.store.load({params: {ss: "", start: 0, limit: this.pP.combo.value}});
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.store.load({params: {ss: "", start: 0, limit: this.pP.combo.value}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    }
});
    
