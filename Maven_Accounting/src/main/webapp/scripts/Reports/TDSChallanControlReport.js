function getTDSChallanControlReport() {
    var reportPanel = Wtf.getCmp('TDSChallanControlReport');
    if(reportPanel == null){
        reportPanel = new Wtf.account.TDSChallanControlTab({
            id :'TDSChallanControlReport',
            border : false,
            title: WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.title"),//'TDS Challan Control Report',
            tabTip: WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.title"),//'TDS Challan Control Report',
            isCreditAvailedReport:true,
            layout: 'fit',
            closable : true,
            iconCls:'accountingbase agedrecievable',
            isVHT:false
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.TDSChallanControlTab=function(config){
    Wtf.apply(this, config);
    
    var buttonArray = new Array();
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.coa.accountSearchText"), 
        width: 150,
        hidden:true,
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
    this.startDate.on('change', function (field, newval, oldval) {
        if (field.getValue() != '' && this.endDate.getValue() != '') {
            if (field.getValue().getTime() > this.endDate.getValue().getTime()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                field.setValue(oldval);
            }
        }
    }, this);
    this.endDate.on('change', function (field, newval, oldval) {
        if (field.getValue() != '' && this.startDate.getValue() != '') {
            if (field.getValue().getTime() < this.startDate.getValue().getTime()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                field.setValue(oldval);
            }
        }
    }, this);
    
    Wtf.natureOfStockItemStore.load();
    this.natureOfStockItem= new Wtf.form.FnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.product.stockitemtype")+"*", 
        name:'natureOfStockItem',
        anchor:'85%',
        store:Wtf.natureOfStockItemStore,
        valueField:'id',
        displayField:'name',
        emptyText:WtfGlobal.getLocaleText("acc.report.annexure10.selectstockitemtype")
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
        id:"tdschallancontrol_export",
        filename: WtfGlobal.getLocaleText("TDS Challan Control Report")+ "_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true,
            detailedXls:true
        },
        params:{
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())            
        },
        get:Wtf.autoNum.TDSChallanControlReport,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        hidden:false
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"tdschallancontrol_print",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.TDSChallanControlReport,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        hidden:true
    });
        
    var unitStore=new Wtf.data.Store({
        url: "ACCInvoice/getCompanyUnit.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.UnitStoreRec)
    });
    unitStore.load();
    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate,"-",this.fetchBtn,this.resetBttn,this.expButton,this.printButton);
    
    this.record=new Wtf.data.Record.create([
        {name: 'date'}, 
        {name: 'natureofpayment'}, 
        {name: 'deducteetype'}, 
        {name: 'bankname'}, 
        {name: 'bsrcode'}, 
        {name: 'cinno'}, 
        {name: 'amount'}, 
        {name: 'paymentid'}, 
        {name: 'tdsRate'} ,
        {name: 'paymentInterestId'} ,
        {name:'TDSPaymentType'},
        {name:'TDSPaymentTypeDesc'}
    ]);  
    this.store=new Wtf.data.Store({
        reader:new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"  
        },this.record),
        url: "ACCCombineReports/getTDSChallanControlReport.do",
        baseParams:{
            isExciseInvoice:true
        }
    });
    
    this.store.on('load',function(store){
        this.quickPanelSearch.StorageChanged(store);
    },this);
    
    this.store.on('beforeload',function(s,o){
        if(this.pP!=undefined){
            if(this.pP.combo.value!="All"){
                o.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            }else{
                var count = this.store.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                o.params.limit = count;
                o.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            }
        }else{
            o.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
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
    this.expandRec = Wtf.data.Record.create ([
        {name:'vendorName'},
        {name:'vendorPanNo'},
        {name:'transactionDate'},
        {name:'transactionDocumentNo'},
        {name:'amountPaid'},
        {name:'paymentid'},
        {name:'tdsRate'},
        {name:'paymentInterestId'},
        {name:'tdsAmount'},
        {name:'tdsInterestAmount'},
        {name:'TDSInterestRate'}
    ]);
   
    this.expandStore = new Wtf.data.Store({
        url:"ACCCombineReports/getIndiaComplianceReportData.do",
        baseParams:{
            reportid:9,
            expanderStoreLoad : true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
   
    this.expander = new Wtf.grid.RowExpander({});
    this.expander.on("expand",this.onRowexpand,this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.summary = new Wtf.grid.GroupSummary();
    this.GridSummary = new Wtf.ux.grid.GridSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
        store:this.store,
        sm:this.sm,
        border:false,
        layout:'fit',
        loadMask : true,
        plugins:[this.GridSummary,this.expander],
        viewConfig:{
            forceFit:true, 
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        columns:[this.rowNo,this.expander,
        {
            header : WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.TDSPaymentDate"),//'TDS Payment Date',//
            dataIndex: 'date' ,
            pdfwidth:200
        },{
            header :WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.NatureofPayment"),//'Nature of Payment',
            dataIndex: 'natureofpayment',
            pdfwidth:200
        },{
            header : WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.DeducteeCode"),//'Deductee Code',
            dataIndex: 'deducteetype' ,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.BankName"),//'Bank Name',
            dataIndex: 'bankname',
            width:100,
            pdfwidth:200,
            align:'center'
        },{
            header : WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.BSRCode"),//'BSR Code',
            dataIndex: 'bsrcode' ,
            pdfwidth:200
        },{
            header : WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.CIN"),//'CIN',
            dataIndex: 'cinno' ,
            pdfwidth:200
        },{
            header: WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.Amount"), //'Amount',//WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.Amount"), 
            dataIndex: 'amount',
            width:100,
            pdfwidth:200,
            align:'right',
            renderer: this.QuantityRender.createDelegate(this)
        },{
            header: WtfGlobal.getLocaleText("acc.report.TDSChallanControlReport.TDSPaymentType"), //'TDS Payment Type',
//            header: "TDS Payment Type",
            dataIndex: 'TDSPaymentTypeDesc',
            width:100,
            pdfwidth:200,
            align:'center',
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
    
    Wtf.account.TDSChallanControlTab.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.TDSChallanControlTab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.TDSChallanControlTab.superclass.onRender.call(this,config);   
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
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                stdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
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
        this.fetchData();   
    },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.expandStore.load({
            params:{
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate   : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                paymentid : (record.data && record.data.paymentid)?record.data.paymentid:((record.data && record.data.paymentInterestId)?record.data.paymentInterestId:"")
            }
        });
        this.SeletedRecord = record;
    },
    fillExpanderBody:function(a,b,c,d){
        var disHtml = "";
        var natureOfPaymentHeader = "";
        var prevBillid = "";
        var sameParent = false;

        for (var i = 0; i < this.expandStore.getCount(); i++) {
            var header = "";
            var rec = this.expandStore.getAt(i);
            
            var currentBillid = rec.data['billid'];
            if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                prevBillid = currentBillid;
                sameParent = false;
            } else {
                sameParent = true;
            }
            
            natureOfPaymentHeader = this.getNaureOfPaymentHeader(rec);              //ProductHeader[0]: HTML text,  ProductHeader[1]: minWidth,  ProductHeader[2]:widthInPercent
            header = this.getNaureOfPaymentExpanderData(rec, sameParent, natureOfPaymentHeader[1], natureOfPaymentHeader[2]);
            var moreIndex = -1;
            if(this.SeletedRecord.data.TDSPaymentType == 1){
                moreIndex=WtfGlobal.searchRecordIndex(this.grid.getStore(), rec.data['paymentid'], 'paymentid');
            }else if (this.SeletedRecord.data.TDSPaymentType == 2){
                moreIndex=WtfGlobal.searchRecordIndex(this.grid.getStore(), rec.data['paymentInterestId'], 'paymentInterestId');
            }else if (this.SeletedRecord.data.TDSPaymentType == 3){
                moreIndex=WtfGlobal.searchRecordIndex(this.grid.getStore(), rec.data['paymentid'], 'paymentid');
            }
            if (moreIndex != -1) {
                var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                disHtml = "<div class='expanderContainer1'>" + natureOfPaymentHeader[0] + header + "</div>";
                body.innerHTML = disHtml;
            }
        }
    },
    getNaureOfPaymentHeader: function(rec){
            var natureOfPaymentHeader = [];
            var arr=[];
            arr=[WtfGlobal.getLocaleText("acc.ven.name"),
                WtfGlobal.getLocaleText("acc.field.VendorPANNO"),
                WtfGlobal.getLocaleText("acc.field.TransactionDate"),
                WtfGlobal.getLocaleText("acc.field.TransactionID"),
                WtfGlobal.getLocaleText("acc.TermSelGrid.TDSAssessableAmount"),
                WtfGlobal.getLocaleText("acc.CommonReport.tdsRate"),
                WtfGlobal.getLocaleText("acc.CommonReport.tdsInterestRate"),//  TDS Interest Rate
                WtfGlobal.getLocaleText("acc.TDSPaymentWindow.TDSAmount"),
                WtfGlobal.getLocaleText("acc.CommonReport.tdsInterestPayment")
           ];
            var gridHeaderText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
            var header = "";  
              
            
            var count=0;
            for(var i=0;i<arr.length;i++){
                if(arr[i] != ""){
                    count++;
                }
            }
            count++; // from grid no
            var widthInPercent=200/count;
            var minWidth = count*200;
            header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
//            for(var arrI=0;arrI<arr.length;arrI++){
//                if(arr[arrI]!=undefined)
//                   header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[arrI] + "</span>";
//            }
            header += "<span class='headerRow' style='width:10% ! important;'>" + WtfGlobal.getLocaleText("acc.ven.name") + "</span>";
            header += "<span class='headerRow' style='width:10% ! important;'>" + WtfGlobal.getLocaleText("acc.field.VendorPANNO") + "</span>";
            header += "<span class='headerRow' style='width:15% ! important;'>" + WtfGlobal.getLocaleText("acc.field.TransactionDate") + "</span>";
            header += "<span class='headerRow' style='width:10% ! important;'>" + WtfGlobal.getLocaleText("acc.field.TransactionID") + "</span>";
            header += "<span class='headerRow' style='width:10% ! important;'>" + WtfGlobal.getLocaleText("acc.TermSelGrid.TDSAssessableAmount") + "</span>";
            
            if(this.SeletedRecord.data.TDSPaymentType == 1 || this.SeletedRecord.data.TDSPaymentType == 3){
                header += "<span class='headerRow' style='width:10% ! important;'>" + WtfGlobal.getLocaleText("acc.CommonReport.tdsRate") + "</span>";
                header += "<span class='headerRow' style='width:10% ! important;'>" + WtfGlobal.getLocaleText("acc.TDSPaymentWindow.TDSAmount") + "</span>";
            }
            if(this.SeletedRecord.data.TDSPaymentType == 2 || this.SeletedRecord.data.TDSPaymentType == 3){
                header += "<span class='headerRow' style='width:10% ! important;'>TDS Interest Rate</span>";
                header += "<span class='headerRow' style='width:10% ! important;'>" + WtfGlobal.getLocaleText("acc.CommonReport.tdsInterestAmt") + "</span>";
            }
            header += "</div><div style='width: 200%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";  
            natureOfPaymentHeader.push(header);
            natureOfPaymentHeader.push(minWidth);
            natureOfPaymentHeader.push(widthInPercent);
            return natureOfPaymentHeader;
    },
    getNaureOfPaymentExpanderData: function(rec,sameParent, minWidth, widthInPercent){
    if (!sameParent) {
        this.Repeatheader = "";
        this.serialNumber = 0;
    }
    this.Repeatheader += "<div style='width: 100%;min-width:" + minWidth + "px'>";  
    this.Repeatheader += "<span class='gridNo'>"+(++this.serialNumber)+".</span>";

    var vendorName=rec.get('vendorName');
    var vendorPanNo=rec.get('vendorPanNo');
    var documentDate=rec.get('transactionDate');
    var TransactionId=rec.get('transactionDocumentNo');
    var amountPaidOrCredited=rec.get('amountPaid');
    var tdsRate=rec.get('tdsRate');
    var TDSAmount=rec.get('tdsAmount');
    var TDSInterestRate=rec.get('TDSInterestRate');
    var tdsInterestAmount=rec.get('tdsInterestAmount');
    this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+vendorName+"' style='width: 10% ! important;'>"+vendorName+"</span>"; 
    this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+vendorPanNo+"' style='width: 10% ! important;'>"+vendorPanNo+"</span>"; 
    this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+documentDate+"' style='width: 15% ! important;'>"+documentDate+"</span>"; 
    this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+TransactionId+"' style='width: 10% ! important;'>"+TransactionId+"</span>"; 
    this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+amountPaidOrCredited+"' style='width: 10% ! important;'>"+amountPaidOrCredited+"</span>";
    if(this.SeletedRecord.data.TDSPaymentType == 1 || this.SeletedRecord.data.TDSPaymentType == 3){
        this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+tdsRate+"' style='width: 10% ! important;'>"+tdsRate+"</span>";
        this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+TDSAmount+"' style='width: 10% ! important;'>"+TDSAmount+"</span>";
    }
    if(this.SeletedRecord.data.TDSPaymentType == 2 || this.SeletedRecord.data.TDSPaymentType == 3){
        this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+TDSInterestRate+"' style='width: 10% ! important;'>"+TDSInterestRate+"</span>";
        this.Repeatheader += "<span class='gridRow'  wtf:qtip='"+tdsInterestAmount+"' style='width: 10% ! important;'>"+tdsInterestAmount+"</span>";  
    }
    this.Repeatheader += "<br>";
    this.Repeatheader += "</div>";
    return this.Repeatheader;
 
    }
});