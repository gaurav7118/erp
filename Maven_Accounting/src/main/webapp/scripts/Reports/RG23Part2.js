/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function getRG23Part2DynamicLoad(){
    var panel=Wtf.getCmp("formrgpart2_id");
    if(panel==null){
        panel=new Wtf.account.RG23Part2({
            title: WtfGlobal.getLocaleText("acc.report.rgformpart2.title"), //'Form R.G. 23A/23C Part II Test',
            tabTip:WtfGlobal.getLocaleText("acc.report.rgformpart2.title"), //'Form R.G. 23A/23C Part II Test',
            id:'formrgpart2_id',
            border:false,
            layout:'fit',
            closable:true,
            iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.RG23Part2=function(config){
    Wtf.apply(this, config);
    
    var buttonArray = new Array();
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.coa.accountSearchText"), // "Search by Document Account Name
        width: 150,
        id:"quickSearch"+this.id
    });
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    
    var unitStore=new Wtf.data.Store({
        url: "ACCInvoice/getCompanyUnit.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.UnitStoreRec)
    });
    unitStore.load();
        
    this.exciseUnit= new Wtf.form.ComboBox({
        store: unitStore,
        typeAhead: true,
        selectOnFocus:true,
        valueField:'id',
        displayField:'name',
        extraComparisionField:'id', 
        extraFields:[],
        hiddenName:"templateunit",
        id:"templateunit"+this.id,
        mode: 'local',
        forceSelection: true,
        width : 150,
        listWidth : 150,
        editable : true,
        triggerAction:'all',
        hirarchical:true,
        emptyText:'Please select excise unit'
    });
    
    
  
    Wtf.natureOfStockItemStore.load(); 
    this.natureOfStockItem= new Wtf.form.FnComboBox({
        name:'natureOfStockItem',
        hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
        hideLabel:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
        anchor:'85%',
        store:Wtf.natureOfStockItemStore,
        valueField:'id',
        displayField:'name',
        emptyText:WtfGlobal.getLocaleText("acc.report.annexure10.selectstockitemtype")
    });

    
    this.productRec = Wtf.data.Record.create([
    {
        name: 'productid'
    },
        
    {
        name: 'productname'
    },
        
    {
        name: 'desc'
    },
        
    {
        name: 'producttype'
    }
    ]);
    
    this.productStore = new Wtf.data.Store({
        url: "ACCProduct/getProductsForCombo.do",
        baseParams: {
            mode: 22,
            onlyProduct:true
            
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.productRec)
    });

    this.productname = new Wtf.form.ComboBox({
        hiddenName: 'productid',
        name: 'productid',
        hidden: this.isCustBill,
        store: this.productStore,
        valueField: 'productid',
        displayField: 'productname',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
        listWidth:150
    });
    
    //    this.natureOfStockItem.on('select',function(){
    //        this.productname.reset();
    ////        this.productStore.removeAll();
    //        this.store.removeAll();
    //        this.productStore.load({
    //            params:{
    //                natureOfStockItem:this.natureOfStockItem.getValue() 
    //            }
    //        });
    //    },this);
    
    
    
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
        id:"exportrg23part2",
        filename: WtfGlobal.getLocaleText("acc.report.rgformpart2.title")+ "_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())            
        },
        get:Wtf.autoNum.rg23Part2,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"printrg23part2",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.rg23Part2,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
        
    //    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
    //    this.endDate,"-",this.fetchBtn,this.resetBttn,this.expButton,this.printButton);
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate,"-",WtfGlobal.getLocaleText("acc.report.annexure10.stockitemtype"),this.natureOfStockItem,"-",WtfGlobal.getLocaleText("acc.lp.companyunitlayout"),this.exciseUnit,"-",this.fetchBtn ,'-',this.resetBttn,'-',this.expButton); //,this.fetchBtn 
    
    this.record=new Wtf.data.Record.create([
    {
        name:'nameofsupplier'
    },

    {
        name:'date', 
        type:'date'
    },

    {
        name:'cenvat'
    },

    {
        name:'additionalduty'
    },

    {
        name:'invoiceno'
    },

    {
        name:'ECCIECRangeCity'
    },

    {
        name:'folioentry'
    },

    {
        name:'product'
        },
        {
            name:'officerinitial'
        },

        {
            name:'remark'
        },

        {
            name:'creditotherduties'
        },

        {
            name:'creditexcise'
        },

        {
            name:'debitotherduties'
        },

        {
            name:'debitexcise'
        },

        {
            name:'cetno'
        },

        {
            name:'invoicedate'
        },

        {
            name:'totalotherduties'
        },

        {
            name:'totalexcise'
        },

        {
            name:'otherduties'
        },

        {
            name:'excise'
    }
    ]);  
    
    this.store=new Wtf.data.Store({
        url:"ACCCombineReports/getRG23PartII.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"count",
            root: "data"
        },this.record)
    });
    
    this.store.on('load',function(store){
        this.quickPanelSearch.StorageChanged(store);
        this.grid.getView().refresh();
    },this);
    this.store.on('beforeload',function(s,o){
        if(this.pP!=undefined){
            if(this.pP.combo.value!="All"){
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
                o.params.natureOfStockItem= this.natureOfStockItem.getValue();
                o.params.exciseUnit= this.exciseUnit.getValue();
                o.params.isExciseInvoice= true;
                o.params.includeAllRec=true;
            }else{
                var count = this.store.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                o.params.limit = count;
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
                o.params.natureOfStockItem= this.natureOfStockItem.getValue();
                o.params.exciseUnit= this.exciseUnit.getValue();
                o.params.isExciseInvoice= true;
                o.params.includeAllRec=true;
            }
        }else{
            o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            o.params.natureOfStockItem= this.natureOfStockItem.getValue();
            o.params.exciseUnit= this.exciseUnit.getValue();
            o.params.isExciseInvoice= true;
            o.params.includeAllRec=true;
            
        }
    },this); 
    
    WtfGlobal.setAjaxTimeOut();
//    if(!Wtf.isEmpty(this.natureOfStockItem.getValue())){
        this.store.load({
            params: {
                start:0,
                limit:30
            }
        });
//    }
    
    this.store.on('datachanged',function(){
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },this);
    this.summary = new Wtf.grid.GroupSummary();
    this.GridSummary = new Wtf.ux.grid.GridSummary({});
    this.rowNo=new Wtf.grid.RowNumberer('Sr. No.');
    //    this.sm = new Wtf.grid.CheckboxSelectionModel();
    var pluginsRows=[];
    pluginsRows.push({
//                header:"<div style='display:none'><b>"+""+"</b></div>",
                header:"",
                align:'center',
                hidden: true,
                colspan:1
            },{
                header:"(A)",
                align:'center',
                hidden: true,
                colspan:2
            },{
                header:WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance"),
                align:'center',
                hidden: true,
                colspan:2
            },{
                header:WtfGlobal.getLocaleText("acc.report.rgformpart2.BED.fresh.credit.allowed.title"),
                align:'center',
                hidden: true,
                colspan:3
            },{
                header:WtfGlobal.getLocaleText("acc.report.rgformpart2.BED.fresh.credit.available.title"),
                align:'center',
                hidden: true,
                colspan:2
            },{
                header:WtfGlobal.getLocaleText("acc.report.rgformpart2.BED.total.credit.available.title"),
                align:'center',
                hidden: true,
                colspan:2
            },{
                header:WtfGlobal.getLocaleText("acc.common.debit"),
                align:'center',
                hidden: true,
                colspan:4
            },{
                header:WtfGlobal.getLocaleText("acc.report.rgformpart2.BED.balance.credit.title"),
                align:'center',
                hidden: true,
                colspan:2
            },{
                header:"",
                align:'center',
                hidden: true,
                colspan:1
            },{
                header:"",
                align:'center',
                hidden: true,
                colspan:1
            }
            
    
);
    this.grid = new Wtf.grid.GridPanel({
        store:this.store,
        sm:this.sm,
        border:false,
//        layout:'fit',
//        plugins:this.GridSummary,
        plugins:[new Wtf.GroupHeaderGrid({
                    rows: [pluginsRows]
                })],
        loadMask : true,
        viewConfig:{
//            forceFit:true, 
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        columns:[this.rowNo,{
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.NameofSupplier"),//'Name of Supplier'
            dataIndex: 'nameofsupplier',
            pdfwidth:200,
            width: 100
        }, {
            header: WtfGlobal.getLocaleText("acc.het.330"),//'Date',
            dataIndex: 'date',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:200,
            width: 70
        },{
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.CENVATOpeningBalanceofDutyinCredit")+" ("+WtfGlobal.getCurrencySymbol()+")",//'CENVAT (Opening Balance of Duty in Credit)',
            dataIndex: 'cenvat',
            width: 100,
            pdfwidth:200,
            align:'right'
//            renderer: this.QuantityRender.createDelegate(this)
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.AdditionalDutiesOpeningBalanceofDutyinCredit"),//'Additional Duties (Opening Balance of Duty in Credit)',
            dataIndex: 'additionalduty',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.InvoiceNoBillofEntryNoDateFreshCreditAllowed"),//'Invoice No/Bill of Entry No. & Date (Fresh Credit Allowed)',
            dataIndex: 'invoiceno',
            pdfwidth:200,
            width: 150
        }, {
            header:WtfGlobal.getLocaleText("acc.report.rgformpart2.C.E.RegNoIEC"),//'C.E.RegNo/IEC Code No. of this Supplier & Range/Customs house from where received (Fresh Credit Allowed)',
            dataIndex: 'ECCIECRangeCity',
            width: 150,
            pdfwidth:200,
            renderer: function(val){
                return "<div wtf:qtip=\"" + val + "\">" + val + "</div>";
//                return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.report.rgformpart2.C.E.RegNoIEC") + "'>" + val + "</div>";
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.FolioEntryNo"),//'Folio & Entry No. in Part I (Fresh Credit Allowed)',//WtfGlobal.getLocaleText("acc.report.rgformpart1.QuantityReceiptofInput"),
            dataIndex: 'folioentry',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.BED.fresh.credit.allowed"),
            dataIndex: 'excise',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.OD.fresh.credit.allowed"),
            dataIndex: 'otherduties',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.BED.total.credit.available"),
            dataIndex: 'totalexcise',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.OD.total.credit.available"),
            dataIndex: 'totalotherduties',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.invoice.number"),
            dataIndex: 'invoice_date',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.cet.number"),
            dataIndex: 'cetno',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.BED.total.debit.available"),
            dataIndex: 'debitexcise',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.OD.total.debit.available"),
            dataIndex: 'debitotherduties',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.BED.balance.credit.available"),
            dataIndex: 'creditexcise',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.OD.balance.credit.available"),
            dataIndex: 'creditotherduties',
            align:'right',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.remarks.field"),
            align:'center',
            dataIndex: 'remark',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rgformpart2.central.excise.officer"),
            align:'center',
            dataIndex: 'officerinitial',
            pdfwidth:200,
            width: 150
        }
        ]
    });
    
    Wtf.apply(this,{
        items:[{
            region:'center',
            layout:'fit',
            border:false,
            items:this.grid
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
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
    },this);
    
    Wtf.account.RG23Part2.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.RG23Part2,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.RG23Part2.superclass.onRender.call(this,config);   
    },        
    fetchData:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        if(sDate > eDate){
            WtfComMsgBox(1,2);
            return;
        }
//        if(!Wtf.isEmpty(this.natureOfStockItem.getValue())){  
            this.store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value,
                    stdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                    natureOfStockItem :Wtf.isEmpty(this.natureOfStockItem.getValue()) ? "":this.natureOfStockItem.getValue(),
                    exciseUnit :Wtf.isEmpty(this.exciseUnit.getValue()) ? "":this.exciseUnit.getValue(),
                    isExciseInvoice:true,
                    includeAllRec:true
                }
            });
//        }else{
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),'Please select stock item type'], 2);
//        }
    },
    QuantityRender: function(v,m,rec){
        var val = WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec)
        if(rec.data.transactionNumber==""){
            return '<b>'+val+'</b>';
        }else{
            return val;
        }
    },
    handleResetClickNew:function(){
        this.quickPanelSearch.reset();
        this.startDate.reset();
        this.endDate.reset();
        this.natureOfStockItem.reset();
        this.exciseUnit.reset();
        this.fetchData();   
        this.store.removeAll();
    }
});


