/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
function getServiceTaxReport(type) {
    var reportPanel = Wtf.getCmp('servicetaxreport'+type);
    var title = WtfGlobal.getLocaleText("acc.service.tax.report.title");
    if(type == Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT){
        title = WtfGlobal.getLocaleText("acc.service.tax.bill.date.wise.report.title");
    }
    if(type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT){
        title = WtfGlobal.getLocaleText("acc.service.tax.realisation.date.wise.report.title");
    }
    if(reportPanel == null){
        reportPanel = new Wtf.account.serviceTaxReportTab({
            id :'servicetaxreport'+type,
            border : false,
            title: title, 
            tabTip: title, 
            isCreditAvailedReport:true,
            layout: 'fit',
            closable : true,
            type:type,
            iconCls:'accountingbase agedrecievable',
            isVHT:false
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.serviceTaxReportTab=function(config){
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
            id:"exportServieTypeReports"+this.id,
            filename: this.title,
            params:{ 
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate: this.endDate.getValue()			
            },
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:Wtf.autoNum.inputCreditSummaryReport,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
    this.basisOfCalculation= new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.india.basisofcal"),
        store: Wtf.basisOfCalculationStore,
        name: 'basisOfCalculation',
        id: 'basisOfCalculation' + this.id,
        width: 183,
        listWidth: 183,
        hiddenName: 'basisOfCalculation',
        valueField: 'id',
        hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
        hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
        mode: 'local',
        displayField: 'name',
        forceSelection: true,
        triggerAction: 'all',
        value: "2",
        selectOnFocus: true
    });
    this. natureOfTransaction= new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.india.natureOfTransaction"),
        store: Wtf.natureOfTransactionStore,
        name: 'natureOfTransaction',
        id: 'natureOfTransaction' + this.id,
        width: 183,
        listWidth: 183,
        hiddenName: 'natureOfTransaction',
        valueField: 'id',
        hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
        hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
        mode: 'local',
        displayField: 'name',
        forceSelection: true,
        triggerAction: 'all',
        value: "",
        selectOnFocus: true
    });
    this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            id:"printBankBookSummary",
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            menuItem:{print:true},
            get:Wtf.autoNum.BankBookSummary,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
            hidden:true
        });
        
    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),
    this.endDate,"-",WtfGlobal.getLocaleText("acc.field.india.basisofcal"),this.basisOfCalculation,"-",WtfGlobal.getLocaleText("acc.field.india.natureOfTransaction")+":",this.natureOfTransaction,this.fetchBtn,this.resetBttn,this.expButton,this.printButton);
    
    this.record=new Wtf.data.Record.create([
        {name: 'date'}, 
        {name: 'refno'}, 
        {name: 'partyname'}, 
        {name: 'category'}, 
        {name: 'billamount'}, 
        {name: 'totaltax'}, 
        {name: 'paidamount'}, 
        {name: 'inputcreditutilized'}, 
        {name: 'realisedamount'}, 
        {name: 'realisedassasableamount'}, 
        {name: 'paidoradjustedamount'}, 
        {name: 'totaltaxpayable'}, 
        {name: 'balancetobepaid'}, 
        {name: 'reftype'}, 
        {name: 'receiptno'}, 
        {name: 'balanceinputcredit'} 
    ]);  
    
    this.store=new Wtf.data.Store({
        reader:new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"  
        },this.record),
        url: "ACCCombineReports/getServiceTaxInputCreditSummaryReport.do"
    });
    
    this.store.on('load',function(store){
        this.quickPanelSearch.StorageChanged(store);
    },this);
    
    this.store.on('beforeload',function(s,o){
        if(this.pP!=undefined){
            if(this.pP.combo.value!="All"){
                o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
                o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
                o.params.type= this.type?this.type:0;
                o.params.natureOfTransaction= !Wtf.isEmpty(this.natureOfTransaction)?this.natureOfTransaction.getValue():"";
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
                o.params.type= this.type?this.type:0;
            }
        }else{
            o.params.stdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
            o.params.type= this.type?this.type:0;
            o.params.basisOfCalculation= this.basisOfCalculation.getValue();
            o.params.natureOfTransaction= !Wtf.isEmpty(this.natureOfTransaction)?this.natureOfTransaction.getValue():"";
            
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
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.GridPanel({
         store:this.store,
         sm:this.sm,
         border:false,
         layout:'fit',
         loadMask : true,
         plugins:this.GridSummary,
         viewConfig:{forceFit:true, emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))},
         columns:[this.sm,this.rowNo,
         {
            header :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.service.tax.report.date")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.date") +"</span>" ,
            dataIndex: 'date' ,
            pdfwidth:200,
            summaryRenderer: function (value, m, rec) {
                return "<p align='center'><b>Total</b></p>";
            }
         },{
            header :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.service.tax.report.vch.type")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.vch.type") +"</span>" ,
            dataIndex: 'reftype',
            hidden : (this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT)?false:true,
            pdfwidth:200
         },{
            header :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.service.tax.report.ref")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.ref") +"</span>" ,
            dataIndex: 'refno' ,
            pdfwidth:200
         },{
            header :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.service.tax.report.vch.no")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.vch.no")+"</span>" ,
            dataIndex: 'receiptno' ,
            hidden : (this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT)?false:true,
            pdfwidth:200
         },{
            header :"<span wtf:qtip='"+ ((this.type != Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT && this.type != Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT )?WtfGlobal.getLocaleText("acc.ven.name"):WtfGlobal.getLocaleText("acc.cnList.gridCustomerName"))+"'>"+ ((this.type != Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT && this.type != Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT)?WtfGlobal.getLocaleText("acc.ven.name"):WtfGlobal.getLocaleText("acc.cnList.gridCustomerName")) +"</span>",
            dataIndex: 'partyname',
            pdfwidth:200
         },{
            header :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.service.tax.report.category")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.category") +"</span>" ,
            dataIndex: 'category',
            pdfwidth:200
         },
         {
            header :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.service.tax.report.bill.amount")+"'>"+WtfGlobal.getLocaleText("acc.service.tax.report.bill.amount") +"</span>"  ,
            dataIndex: 'billamount' ,
            pdfwidth:200,
            hidden : (this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT)?true:false,
            align:'right',
            summaryType: 'sum',
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true);  
            },
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },
         {
            header :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.service.tax.report.bill.total.tax")+"'>"+WtfGlobal.getLocaleText("acc.service.tax.report.bill.total.tax") +"</span>" ,
            dataIndex: 'totaltax' ,
            pdfwidth:200,
            hidden : (this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT)?true:false,
            align:'right',
            summaryType: 'sum',
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true); 
            },
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },{
            header:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.service.tax.report.bill.paid.amount")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.bill.paid.amount") +"</span>" , 
            dataIndex: 'paidamount',
            width:100,
            pdfwidth:200,
            align:'right',
            summaryType: 'sum',
            hidden : (this.type == Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.TAX_SERVICE_RECEIVED_REPORT)?true:false,
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true); 
            },
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },{
            header:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.service.tax.report.realized.amount")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.realized.amount") +"</span>" , 
            dataIndex: 'realisedamount',
            width:100,
            pdfwidth:200,
            align:'right',
            summaryType: 'sum',
            hidden : (this.type == Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.TAX_SERVICE_RECEIVED_REPORT)?false:true,
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true); 
            },
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },{
            header:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.service.tax.report.realized.assessable.amount")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.realized.assessable.amount") +"</span>" , 
            dataIndex: 'realisedassasableamount',
            width:100,
            pdfwidth:200,
            align:'right',
            hidden : (this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT)?false:true,
            summaryType: 'sum',
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true); 
            },
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },{
            header:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.service.tax.report.total.tax.payable")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.total.tax.payable") +"</span>" , 
            dataIndex: 'totaltaxpayable',
            width:100,
            pdfwidth:200,
            align:'right',
            hidden : (this.type == Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.TAX_SERVICE_RECEIVED_REPORT)?false:true,
            summaryType: 'sum',
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true);  
            },
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },{
            header:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.service.tax.report.paid.or.adjusted")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.paid.or.adjusted") +"</span>" , 
            dataIndex: 'paidoradjustedamount',
            width:100,
            pdfwidth:200,
            align:'right',
            summaryType: 'sum',
            hidden : (this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT)?false:true,
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true);  
            },
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },{
            header: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.service.tax.report.bill.input.credit")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.bill.input.credit") +"</span>",
            dataIndex: 'inputcreditutilized',
            width:100,
            pdfwidth:200,
            align:'right',
            hidden : (this.type == Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.TAX_SERVICE_RECEIVED_REPORT)?true:false,
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true); 
            },
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },{
            header:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.service.tax.report.bill.balance.credit")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.bill.balance.credit") +"</span>",
            dataIndex: 'balanceinputcredit' ,
            pdfwidth:200,
            hidden : (this.type == Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT || this.type == Wtf.serviceTaxReports.REALISATION_DATE_WISE_REPORT)?true:false,
            align:'right',
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true); 
            },
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
         },{
            header:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.service.tax.report.bill.balance.paid")+"'>"+ WtfGlobal.getLocaleText("acc.service.tax.report.bill.balance.paid") +"</span>",
            dataIndex: 'balancetobepaid' ,
            pdfwidth:200,
            hidden : (this.type == Wtf.serviceTaxReports.BILL_DATE_WISE_REPORT)?false:true,
            align:'right',
            renderer:function(v,m,rec){
                return curRenderr(v,m,rec,true); 
            },
            summaryType: 'sum',
            summaryRenderer: function (value, m, rec) {
                return "<p align='right'><b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b></p>";
            }
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
    
    Wtf.account.serviceTaxReportTab.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.serviceTaxReportTab,Wtf.Panel,{
    onRender:function(config){
      Wtf.account.serviceTaxReportTab.superclass.onRender.call(this,config);   
    },        
    fetchData:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        var basisOfCalculation=this.basisOfCalculation.getValue();
        var natureOfTransaction=!Wtf.isEmpty(this.natureOfTransaction)?this.natureOfTransaction.getValue():"";
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
                basisOfCalculation:basisOfCalculation,
                natureOfTransaction:natureOfTransaction
            }
        });
    },
    handleResetClickNew:function(){
      this.quickPanelSearch.reset();
      this.startDate.reset();
      this.endDate.reset();
      this.natureOfTransaction.setValue(""); // Reset to option All
      this.fetchData();   
    }
});
function curRenderr(val,m,rec,applycurrency){
    if(val && val != 0){
        return WtfGlobal.conventInDecimal(val,WtfGlobal.getCurrencySymbol());
    }
    return "";
}
