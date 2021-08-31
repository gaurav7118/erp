/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function getDailyStockRegisterDynamicLoad(){
    var panel=Wtf.getCmp("dailystockregister");
    if(panel==null){
        panel=new Wtf.account.DailyStockRegister({
            title: 'Daily Stock Register',
            tabTip:'Daily Stock Register',
            id:'dailystockregister',
            border:false,
            layout:'fit',
            closable:true,
            isDailyStockRegister : true,
            iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.DailyStockRegister=function(config){
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
        {name: 'productid'},
        {name: 'productname'},
        {name: 'desc'},
        {name: 'producttype'}
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
        listWidth:150,
        width:150
    });
    
    this.productStore.load();
    
    this.unitStore=new Wtf.data.Store({
        url: "ACCInvoice/getCompanyUnit.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.UnitStoreRec)
    });
    this.unitStore.load();
        
    this.exciseUnit= new Wtf.form.ComboBox({
        store: this.unitStore,
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
        hidden:this.isPlaSummary?true:false,
        hideLabel:this.isPlaSummary?true:false
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
        id:"exportrg23part1",
        filename: WtfGlobal.getLocaleText("acc.dsrreport.title") + "_v1",
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
        get:Wtf.autoNum.dailyStockReport,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"printrg23part1",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.BankBookSummary,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
        
    //    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
    //    this.endDate,"-",this.fetchBtn,this.resetBttn,this.expButton,this.printButton);
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate,"-",WtfGlobal.getLocaleText("acc.lp.companyunitlayout"),this.exciseUnit,"-","Stock Item :",this.productname,"-",this.fetchBtn ,this.resetBttn,'-',this.expButton) ;
    
    this.record=new Wtf.data.Record.create([
        {name:'pid'},
        {name:'productDesc'},
        {name:'transactionDate'},
        {name:'transactionNumber'},
        {name:'openingBalnace'},
        {name:'quantityManufactured'},
        {name:'totalQuantity'},
//        {name:'issuedForProduction'},
//        {name:'issuedForRepacking'},
        {name:'issuedForHomeClearance'},
        {name:'issuedUnderBond'},
        {name:'uom'},
        {name:'uomsales'},
        {name:'hsncode'},
        {name:'looseNoOfReals'},
        {name:'storeClosingbalance'},
        {name:'finishedRoomClosingbalance'},
        {name:'educationCess'},
        {name:'totalAmountOFTransaction'},
        {name:'additionalExciseDutyAmount'},
        {name:'additionalExciseDutyRate'},
        {name:'exciseDutyAmount'},
        {name:'exciseDutyRate'},
        {name:'quantity'},
        {name:'purpose'},
        {name:'exportUnderClaimForRebateAMOUNT'},
        {name:'exportUnderClaimForRebateQTY'},
        {name:'valueOfHomeClearanceStock'},
        {name:'sign'},
        {name:'issuedUnderLUT'}
    ]);  
    
    this.store=new Wtf.data.Store({
        url:"ACCCombineReports/getDailyStockRegister.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.record),     
//        groupField:"pid",
        sortInfo: {field: 'pid',direction: "ASC"}
    });
    this.store.load();
    
    this.store.on('load',function(store){
        this.quickPanelSearch.StorageChanged(store);
        this.grid.getView().refresh();
    },this);
    
    this.store.on('beforeload',function(s,o){
        if(this.pP!=undefined){
            if(this.pP.combo.value!="All"){
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
                o.params.isStockLedger=true;
                o.params.isactivatelandedinvamt=false;
                o.params.isprovalreport=true;
                o.params.rg23part1=true;
                o.params.productId=this.productname.getValue();
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
                o.params.isStockLedger=true;
                o.params.isactivatelandedinvamt=false;
                o.params.isprovalreport=true;
                o.params.isprovalreport=true;
                o.params.rg23part1=true;
                o.params.productId=this.productname.getValue();
            }
        }else{
            o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            o.params.isStockLedger=true;
            o.params.isactivatelandedinvamt=false;
            o.params.isprovalreport=true;
            o.params.rg23part1=true;
            o.params.productId=this.productname.getValue();
            
        }
    },this);
    
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
//        layout:'fit',
//        plugins:this.GridSummary,
        loadMask : true,
        viewConfig:{
//            forceFit:true, 
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        columns:[this.rowNo,{
            header: WtfGlobal.getLocaleText("acc.het.330"),
            pdfwidth:200,
            dataIndex: 'transactionDate'
        }, {
            header: WtfGlobal.getLocaleText("acc.dsrreport.column.doctno"),
            dataIndex: 'transactionNumber',
            pdfwidth:200,
            width: 150,
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                return "<b>Total</b>";
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalance")+" ",
            dataIndex: 'openingBalnace',
            pdfwidth:200,
            width: 100,
            align:'right'
        }, 
        {
            header: WtfGlobal.getLocaleText("acc.dsrreport.column.quantityman"),
            fixed: true,
            dataIndex: 'quantityManufactured',
            pdfwidth:200,
            width: 150,
            align:'right',
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,"")+"</b></p>";
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
            fixed: true,
            pdfwidth:200,
            dataIndex: 'uom',
            width: 70
        },
        {
            header: WtfGlobal.getLocaleText("acc.mastercontract.totalquantity"),
            dataIndex: 'totalQuantity',
            pdfwidth:200,
            width: 150,
            align:'right'
        },
//        {
//            header: WtfGlobal.getLocaleText("acc.dsrreport.column.issued.prod"),
//            dataIndex: 'issuedForProduction',
//            pdfwidth:200,
//            width: 150
//        },
//        {
//            header: WtfGlobal.getLocaleText("acc.dsrreport.column.issued.repack"),
//            dataIndex: 'issuedForRepacking',
//            pdfwidth:200,
//            width: 150
//        },
        {
            header: WtfGlobal.getLocaleText("acc.dsrreport.column.issued.home.clearance"),
            dataIndex: 'issuedForHomeClearance',
            pdfwidth:200,
            width: 150,
            align:'right',
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,"")+"</b></p>";
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
            fixed: true,
            pdfwidth:200,
            dataIndex: 'uomsales',
            width: 70
        },
        {
            header: WtfGlobal.getLocaleText("Value of Home Clearance Stock (Amount)"),
            pdfwidth:300,
            dataIndex: 'valueOfHomeClearanceStock',
            align:'right',
            width: 70
        },
        {
            header: WtfGlobal.getLocaleText("For Export Under Claim For Rebate (Qty)"),
            pdfwidth:300,
            dataIndex: 'exportUnderClaimForRebateQTY',
            align:'center',
            width: 70
        },
        {
            header: WtfGlobal.getLocaleText("For Export Under Claim For Rebate (Amount)"),
            pdfwidth:300,
            align:'right',
            dataIndex: 'exportUnderClaimForRebateAMOUNT',
            width: 70
        },
        {
            header: WtfGlobal.getLocaleText("acc.dsrreport.column.issued.export.bond"),
            dataIndex: 'issuedUnderBond',
            pdfwidth:300,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("acc.dsrreport.column.issued.export.lut"),
            dataIndex: 'issuedUnderLUT',
            pdfwidth:300,
            width: 150
        },
//        {
//            header: WtfGlobal.getLocaleText("acc.dsrreport.column.hsn.code"),
//            dataIndex: 'hsncode',
//            pdfwidth:200,
//            width: 150
//        },
        {
            header: WtfGlobal.getLocaleText("For Other Purpose Rooms: Purpose"),
            dataIndex: 'purpose',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Quantity"),
            dataIndex: 'quantity',
            pdfwidth:200,
            align:'center',
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Excise Duty Rate"),
            dataIndex: 'exciseDutyRate',
            pdfwidth:200,
            align:'center',
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Excise Duty Amount"),
            dataIndex: 'exciseDutyAmount',
            align:'right',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Additional Excise Duty Rate"),
            dataIndex: 'additionalExciseDutyRate',
            align:'center',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Additional Excise Duty Amount"),
            dataIndex: 'additionalExciseDutyAmount',
            align:'right',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Amount"),
            dataIndex: 'totalAmountOFTransaction',
            align:'right',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("E. Cess"),
            dataIndex: 'educationCess',
            align:'right',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("H. E. Cess"),
            dataIndex: 'educationCess',
            align:'right',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("In Finished Room: Closing Balance"),
            dataIndex: 'finishedRoomClosingbalance',
            pdfwidth:200,
            align:'right',
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Store Closing Balance"),
//            dataIndex: 'storeClosingbalance',
            dataIndex: 'totalQuantity',
            align:'right',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Loose No of Reels"),
            dataIndex: 'looseNoOfReals',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Bill / Daily production Report date"),
            dataIndex: 'transactionDate',
            pdfwidth:200,
            width: 150
        },
        {
            header: WtfGlobal.getLocaleText("Sign of Assessee or His agent"),
            dataIndex: 'sign',
            align:'center',
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
    
    Wtf.account.DailyStockRegister.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.DailyStockRegister,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.DailyStockRegister.superclass.onRender.call(this,config);   
    },        
    fetchData:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
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
                isStockLedger:true,
                isactivatelandedinvamt:false,
                isprovalreport:true,
                rg23part1:true,
                exciseunit:this.exciseUnit.getValue(),
                productId:this.productname.getValue()
            }
        });
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
        this.productname.reset();
        this.exciseUnit.reset();
        this.fetchData();   
    }
});


