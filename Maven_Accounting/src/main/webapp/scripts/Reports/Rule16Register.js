/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function getRule16RegisterDynamicLoad(){
    var panel=Wtf.getCmp("Rule16Register_id");
    if(panel==null){
        panel=new Wtf.account.Rule16RegisterTab({
            title: WtfGlobal.getLocaleText("acc.report.rule16register.title"),//WtfGlobal.getLocaleText("acc.VAT.tabtitle"), //"Stock Valuation Detail Report",
            tabTip:WtfGlobal.getLocaleText("acc.report.rule16register.title"),//WtfGlobal.getLocaleText("acc.VAT.tabtitle"), //"Stock Valuation Detail Report",
            id:'Rule16Register_id',
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

Wtf.account.Rule16RegisterTab=function(config){
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
        width:100,
        listWidth:150
    });
    this.productStore.load();
    
    this.productStore.on("load", function() {
        var record = new Wtf.data.Record({
            productid: "",
            productname: "All Records"
        });
        this.productStore.insert(0, record);
        this.productname.setValue("");
    }, this);
    
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
        id:"rule16registerexport",
        filename: WtfGlobal.getLocaleText("acc.report.rule16register.title"),
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        get:Wtf.autoNum.rule16register,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"printBankBookSummary",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.BankBookSummary,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
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
        scope:this,
        emptyText:WtfGlobal.getLocaleText("acc.field.SelectExciseUnit")
    });
    //    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
    //    this.endDate,"-",this.fetchBtn,this.resetBttn,this.expButton,this.printButton);
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate,"-",WtfGlobal.getLocaleText("acc.lp.companyunitlayout"),this.exciseUnit,"-",WtfGlobal.getLocaleText("acc.productList.gridProduct"),this.productname,"-",this.fetchBtn,this.resetBttn,this.expButton); 
    
    this.record=new Wtf.data.Record.create([
    {
        name:"particularsofinvoiceboe"
    },

    {
        name:"owninvoiceno"
    },

    {
        name:"partyinvoiceno"
    },

    {
        name:"descriptionofgoods"
    },

    {
        name:"quantityrejected"
    },

    {
        name:"reasonforrejection"
    },

    {
        name:"uom"
    },

    {
        name:"amountofcreditavailed"
    }
    ]);  
    
    this.store=new Wtf.data.Store({
        reader:new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"  
        },this.record),
        url: "ACCCombineReports/getRule16Register.do"
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
                o.params.productid= this.productname.getValue();
                o.params.exciseunit= this.exciseUnit.getValue();
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
                o.params.productid= this.productname.getValue();
                o.params.exciseunit= this.exciseUnit.getValue();
            }
        }else{
            o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            o.params.productid= this.productname.getValue();
            o.params.exciseunit= this.exciseUnit.getValue();
        }
    },this);
    
    WtfGlobal.setAjaxTimeOut();
    this.store.load({
        params: {
            start:0,
            limit:30
        }
    });
    
    this.store.on('datachanged',function(){
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },this);
    this.summary = new Wtf.grid.GroupSummary();
    this.GridSummary = new Wtf.ux.grid.GridSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    //    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
        store:this.store,
        sm:this.sm,
        border:false,
        layout:'fit',
        plugins:this.GridSummary,
        loadMask : true,
        viewConfig:{
            forceFit:true, 
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        columns:[this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.report.rule16register.ParticularsofInvoiceBOEotherApprovedDocandDate"),//'Particulars of Invoice/BOE other Approved Doc and Date',
            dataIndex: 'particularsofinvoiceboe',
            pdfwidth:200
        }, {
            header:WtfGlobal.getLocaleText("acc.report.rule16register.OwnRejectionInvoiceNoDate"),//'Own Rejection Invoice No. /Date',
            dataIndex: 'owninvoiceno',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rule16register.PartyRejectionInvoiceNoDate"),//'Party\'s Rejection Invoice No. /Date',//
            dataIndex: 'partyinvoiceno',
            pdfwidth:200,
            width: 100
        }, {
            header:WtfGlobal.getLocaleText("acc.report.rule16register.Descriptionofgoods"),//'Description of goods',//
            dataIndex: 'descriptionofgoods',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rule16register.UOM"),//'UOM',//
            dataIndex: 'uom',
            pdfwidth:200,
            width: 150
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rule16register.QuantityRejected"),//'Quantity Rejected',//
            fixed: true,
            dataIndex: 'quantityrejected',
            pdfwidth:200,
            width: 150,
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                return "Total = " +value;
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.report.rule16register.ReasonforRejection"),//'Reason for Rejection',//
            fixed: true,
            dataIndex: 'reasonforrejection',
            pdfwidth:200,
            width: 150
        },{
            header: WtfGlobal.getLocaleText("acc.report.rule16register.AmountofcreditAvailed"),//'Amount of credit Availed',
            dataIndex: 'amountofcreditavailed',
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
    
    Wtf.account.Rule16RegisterTab.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.Rule16RegisterTab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.Rule16RegisterTab.superclass.onRender.call(this,config);   
    },        
    fetchData:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        var exciseUnit = this.exciseUnit.getValue();
        if(sDate > eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
                stdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                productid :this.productname.getValue(),
                exciseunit:exciseUnit
            }
        });
    },
    handleResetClickNew:function(){
        this.quickPanelSearch.reset();
        this.startDate.reset();
        this.endDate.reset();
        this.productname.reset();
        this.exciseUnit.reset();
        this.fetchData();   
    }
});