/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function callSalesAnalysisReportDynamicLoad(isTopCustomers,isTopProducts,isTopAgents,reportName) {
    var panel = Wtf.getCmp("SalesAnalysis"+reportName);
    if (panel == null) {
        panel = new Wtf.account.SalesAnalysisReport({
            title: reportName,
            tabTip: reportName,
            id: "SalesAnalysis"+reportName,
            iconCls:'accountingbase coa',
            layout: 'fit',
            closable: true,
            border: false,
            isTopCustomers: isTopCustomers,
            isTopProducts: isTopProducts,
            isTopAgents: isTopAgents
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.SalesAnalysisReport = function(config) {
    Wtf.apply(this, config);
    this.isTopCustomers = config.isTopCustomers || false;
    this.isTopProducts = config.isTopProducts || false;
    this.isTopAgents = config.isTopAgents || false;
    this.createGrid();
    this.createTBar();
    this.title = config.title;
    Wtf.account.SalesAnalysisReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.SalesAnalysisReport, Wtf.Panel, {
    onRender: function(config) {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.btnArr,
                bbar: this.bBarBtnArr
            }]
        });
        
        this.add(this.leadpan);
        this.fetchStatement();
        
        Wtf.account.SalesAnalysisReport.superclass.onRender.call(this,config);
    },
    
    createTBar: function() {
        this.btnArr = [];
        this.bBarBtnArr = [];
        
        // Create Start Date button
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        
        // Create End Date button
        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)
        });
        
        this.count =new Wtf.form.NumberField({
            fieldLabel: this.isTopCustomers ? WtfGlobal.getLocaleText("acc.header.nooftopcust") : this.isTopProducts ? WtfGlobal.getLocaleText("acc.header.nooftopprod") : WtfGlobal.getLocaleText("acc.header.nooftopagents"),
            maxLength:2,
            width:30,
            allowDecimal:false,
            allowBlank:true,
            minValue:1,
            name:'count',
            value:30
        });
        
        this.btnArr.push("-");
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.btnArr.push(this.startDate);
        this.btnArr.push(" ");
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.btnArr.push(this.endDate);
        this.btnArr.push("-");
        this.btnArr.push(this.isTopCustomers ? WtfGlobal.getLocaleText("acc.header.nooftopcust") : this.isTopProducts ? WtfGlobal.getLocaleText("acc.header.nooftopprod") : WtfGlobal.getLocaleText("acc.header.nooftopagents"));
        this.btnArr.push(this.count);    
            
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement                        
        });
        this.btnArr.push('-', this.fetchBttn);
        
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  // 'Export report details',
            filename: this.title + "_v1",
            disabled: true,
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: Wtf.autoNum.SalesAnalysisReport
        });
        this.bBarBtnArr.push('-', this.exportButton);
        
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                startDate : this.sDate,
                stdate : this.sDate,
                endDate : this.eDate,
                isTopCustomers: this.isTopCustomers,
                isTopProducts: this.isTopProducts,
                isTopAgents: this.isTopAgents,
                countNumber: this.count.getValue()
            });
        },this);
        
        this.printButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.sales.printTT"), // 'Print report details',
            disabled: true,
            filename:  this.title,
            menuItem: {
                print: true
            },
            get: Wtf.autoNum.SalesAnalysisReport
        });
        this.bBarBtnArr.push('-', this.printButton);
        
        this.printButton.on("click", function() {
            this.printButton.setParams({
                startDate : this.sDate,
                stdate : this.sDate,
                endDate : this.eDate,
                isTopCustomers: this.isTopCustomers,
                isTopProducts: this.isTopProducts,
                isTopAgents: this.isTopAgents,
                countNumber: this.count.getValue()
            });
        },this);
    },
    
    createGrid: function() {
        this.Store = new Wtf.data.Store({
            url: "ACCInvoiceCMN/getSalesAnalysisReport.do",
            remoteSort: true,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
            
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            border: false,
            loadMask: true,
            columns: [new Wtf.grid.RowNumberer({
                width: 30,
                pdfwidth:150
            }),{
                header: this.isTopCustomers ? WtfGlobal.getLocaleText("acc.stockLedgerCust.Code") : this.isTopProducts ? WtfGlobal.getLocaleText("acc.field.ProductCode") : WtfGlobal.getLocaleText("acc.header.AgentsCode"),     
                dataIndex:'acccode',
                width: 100,
                pdfwidth:150,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: this.isTopCustomers ? WtfGlobal.getLocaleText("acc.cust.name") : this.isTopProducts ? WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc") : WtfGlobal.getLocaleText("acc.header.AgentName"),     
                dataIndex:'accname',
                width: 150,
                pdfwidth:150,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.product.gridQty"),
                dataIndex:'quantity',
                width: 150,
                pdfwidth:150,
                renderer:WtfGlobal.quantityaRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.addTpe.header2"),
                dataIndex:'value',
                width: 150,
                pdfwidth:150,
                renderer:WtfGlobal.currencyNoncurrencyRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.header.peroftotal"),
                dataIndex:'peroftotal',
                width: 150,
                pdfwidth:150,
                renderer:function(val){
                    return WtfGlobal.conventInDecimal(val,"");
                }
            }],
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
    },

    fetchStatement: function() {
        if(this.count.getValue()>0){
            this.Store.load();
        }else{
            this.Store.removeAll();
        }
    },
    
    handleStoreBeforeLoad: function() {
       WtfGlobal.setAjaxTimeOut()
       var currentBaseParams = this.Store.baseParams;
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
            
        currentBaseParams.isTopCustomers = this.isTopCustomers;
        currentBaseParams.isTopProducts = this.isTopProducts;
        currentBaseParams.isTopAgents = this.isTopAgents;
        currentBaseParams.startdate=this.sDate;
        currentBaseParams.stdate=this.sDate;
        currentBaseParams.enddate=this.eDate;
        currentBaseParams.countNumber = this.count.getValue()
       
        this.exportButton.enable();
        this.printButton.enable();
    },
    
    handleStoreOnLoad: function(store) {
        WtfGlobal.resetAjaxTimeOut();
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
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
    }
});