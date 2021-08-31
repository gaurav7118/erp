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
function monthlySalesReportDynamicLoad(params) {
    var consolidateFlag = params.consolidateFlag || false;
    var withinventory = params.withinventory || false;
    var panel = Wtf.getCmp('monthlySalesReport');
    if (params.isCustomWidgetReport) {
        /*
         *Implementation to add this report in custom widget report. 
         * */
        panel = new Wtf.account.MonthlySalesReport({
            border: false,
            helpmodeid: 96,
            consolidateFlag: consolidateFlag,
            withinventory: withinventory,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            receivable: true,
            monthlysalesreport: true,
            isCustomWidgetReport: params.isCustomWidgetReport
        });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    } else {
        if (panel == null) {
            panel = new Wtf.account.MonthlySalesReport({
                id: 'monthlySalesReport',
                border: false,
                helpmodeid: 96, //ERP-26724
                consolidateFlag: consolidateFlag,
                withinventory: withinventory,
                layout: 'fit',
                iconCls: 'accountingbase agedrecievable',
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.sales.tabTitle"), Wtf.TAB_TITLE_LENGTH),
                tabTip: WtfGlobal.getLocaleText("acc.sales.tabTitle"), //Monthly Sales Report
                receivable: true,
                monthlysalesreport: true,
                closable: true
            });
            Wtf.getCmp('as').add(panel);
            panel.on('salesinvoices', MonthlySalesInvoicesList);
        }

        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
}



function callMonthlySalesByProductDynamicLoad(params) {
    /*
     * get id for panel from params. If not present assign default id.
     */
    var mainPanelId = params.id || "MonthlySalesByProductReport";
    var isCustomWidgetReport = params.isCustomWidgetReport || false; // Flag for widget view
    params.mainPanelId = mainPanelId;
    var mainPanel = Wtf.getCmp(mainPanelId);
    
    if (mainPanel == null) {
        mainPanel = new Wtf.TabPanel({
            id: mainPanelId,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.MonthlySalesByProduct"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.field.MonthlySalesByProduct"), //Monthly Sales By Product Report
            closable: true,
            iconCls: 'accountingbase coa',
            activeTab: 0
        });
        callMonthlySalesByProductSubTab(params);
        callMonthlySalesByProductSubjectToGSTReport(params);
        if (!isCustomWidgetReport) {
            /*
             * If not widget view then add this panel to main tab panel.
             */
            Wtf.getCmp('as').add(mainPanel);
        }
    }
    if (isCustomWidgetReport && params.callbackFn) {
        /*
         *call callback function to add this report to widget.
         **/
        params.callbackFn.call(this, mainPanel);
    } else {
        Wtf.getCmp('as').setActiveTab(mainPanel);
        mainPanel.setActiveTab(Wtf.getCmp('MonthlySalesByProductReportSubTab'));
        Wtf.getCmp('as').doLayout();
    }
}

function callMonthlySalesByProductSubTab(params) {
    var mainPanelId = params.mainPanelId;
    var consolidateFlag = params.consolidateFlag || false;
    var withinventory = params.withinventory || false;
    var isCustomWidgetReport = params.isCustomWidgetReport || false;
    
    var panelId = mainPanelId +'SubTab'; //Id for sub tab
    var panel = Wtf.getCmp(panelId);
    if (panel == null) {
        panel = new Wtf.account.MonthlySalesReport({
            id: panelId,
            border: false,
            helpmodeid: 97, //ERP-26724
            consolidateFlag: consolidateFlag,
            withinventory: withinventory,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.MonthlySalesByProduct"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.field.MonthlySalesByProduct"), //Monthly Sales By Product Report
            receivable: true,
            monthlysalesreport: true,
            isBasedOnProduct: true,
            closable: false,
            isCustomWidgetReport : isCustomWidgetReport
        });
        Wtf.getCmp(mainPanelId).add(panel);
        panel.on('salesinvoices', MonthlySalesInvoicesList);
    }
    Wtf.getCmp(mainPanelId).setActiveTab(panel);
    Wtf.getCmp(mainPanelId).doLayout();
}

function callMonthlySalesByProductSubjectToGSTReport(params) {
    if(Wtf.Countryid !== (Wtf.Country.SINGAPORE+"")){
        return;
    }
    var mainPanelId = params.mainPanelId;
    var consolidateFlag = params.consolidateFlag || false;
    var withinventory = params.withinventory || false;
    var isCustomWidgetReport = params.isCustomWidgetReport || false;
    
    var panelId = mainPanelId +'SubjectToGSTReport'; //Id for sub tab
    var panel = Wtf.getCmp(panelId);
    if (panel == null) {
        panel = new Wtf.account.monthlySalesByProductSubjectToGSTReport({
            id: panelId,
            border: false,
            helpmodeid: 28,
            consolidateFlag: consolidateFlag,
            withinventory: withinventory,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.monthlySalesByProductSubjectToGSTReport"),Wtf.TAB_TITLE_LENGTH), // "Monthly Sales by Product Subject to GST Report",
            tabTip: WtfGlobal.getLocaleText("acc.field.monthlySalesByProductSubjectToGSTReport"),
            receivable: true,
            monthlysalesreport: true,
            isBasedOnProduct: true,
            closable: false,
            isCustomWidgetReport: isCustomWidgetReport
        });
        Wtf.getCmp(mainPanelId).add(panel);
        panel.on('salesinvoices',MonthlySalesInvoicesList);
    }
    Wtf.getCmp(mainPanelId).doLayout();
}



function MonthlySalesInvoicesListDynamicLoad(jid,consolidateFlag,reportbtnshwFlag,customerid,monthyear){
    var panel = Wtf.getCmp("MonthlySalesInvoicesList");
    if(panel!=null){
        Wtf.getCmp('as').remove(panel);
        //panel.destroy();
        panel=null;
    }
    if(panel==null){
        panel = new Wtf.account.MonthlySalesInvoicesPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.invoiceList.SalesInvoices"),Wtf.TAB_TITLE_LENGTH) ,//Monthly Sales Invoices
            tabTip:WtfGlobal.getLocaleText("acc.field.YoucanviewMonthlySalesInvoicesListfromhere"),
            id:'MonthlySalesInvoicesList',
            border:false,
            customerid:customerid,
            monthyear:monthyear,
            //            customColArr :customCol,
            moduleId:Wtf.Acc_Invoice_ModuleId,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function MonthlySalesInvoicesListByProductDynamicLoad(productid,consolidateFlag,reportbtnshwFlag,customerid,monthyear){
    var panel = Wtf.getCmp("MonthlySalesInvoicesListByProduct");
    if(panel!=null){
        Wtf.getCmp('as').remove(panel);
        panel=null;
    }
    if(panel==null){
        panel = new Wtf.account.MonthlySalesInvoicesPanel({
            title:WtfGlobal.getLocaleText("acc.invoiceList.SalesInvoicesByProduct"),//Monthly Sales Invoices
            tabTip:WtfGlobal.getLocaleText("acc.field.YoucanviewMonthlySalesInvoicesbyproductListfromhere"),
            id:'MonthlySalesInvoicesListByProduct',
            border:false,
            productid:productid,
            monthyear:monthyear,
            moduleId:Wtf.Acc_Invoice_ModuleId,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            isBasedOnProduct: true,
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
//************************************************8****************************************************
Wtf.account.MonthlySalesReport=function(config){
	this.receivable=config.receivable||false;
	this.isCustomWidgetReport=config.isCustomWidgetReport||false;
	this.withinventory=config.withinventory||false;
	this.isSummary=config.isSummary||false;
        this.isBasedOnProduct = config.isBasedOnProduct != undefined ? config.isBasedOnProduct : false ;
	this.summary = new Wtf.ux.grid.GridSummary();
	// this.summary = new Wtf.grid.GroupSummary({});
    this.expander = new Wtf.grid.RowExpander({});

	// to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name:'monthid',type:'int'}, 'name'],
            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
                [10,'November'],[11,'December']]
    });

    var data=WtfGlobal.getBookBeginningYear(true);
    
    this.yearStore= new Wtf.data.SimpleStore({
            fields: [{name:'id',type:'int'}, 'yearid'],
            data :data
    });

    this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'startMonth',
            displayField:'name',
            forceSelection: true,
            width:90,
            valueField:'name',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
    });  

    this.startYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'startYear',
            width:90,
            displayField:'yearid',
//            anchor:'95%',
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
    });  

    this.endMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'endMonth',
            displayField:'name',
            forceSelection: true,
            width:90,
            valueField:'name',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
    }); 

    this.endYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'endYear',
            displayField:'yearid',
            width:90,
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
    }); 
    
    /*
     Added with GST and without GST Filter in Monthly Sales by Product and Monthly Sales Report
    */
    this.gstView = new Wtf.data.SimpleStore({
        fields: [{name: 'typeid', type: 'int'}, 'name'],
        data: [[0, WtfGlobal.getLocaleText("acc.field.withgst")], [1, WtfGlobal.getLocaleText("acc.field.withoutgst")]]
    });

    this.gstViewCombo = new Wtf.form.ComboBox({
        store: this.gstView,
        name: 'viewGstCombo',
        displayField: 'name',
        valueField: 'typeid',
        mode: 'local',
        value:(this.isBasedOnProduct? 1:0),
        width: 150,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });
    
    this.gstViewCombo.on('select', this.fetchMonthlySalesReport, this);
    if (config.sMonth!=null && config.sMonth!= "")
        this.startMonth.setValue(config.sMonth);

    if (config.sYear!=null && config.sYear!= "")
        this.startYear.setValue(config.sYear);

    if (config.eMonth!=null && config.eMonth!= "")
        this.endMonth.setValue(config.eMonth);   

    if (config.eYear!=null && config.eYear!= "")
        this.endYear.setValue(config.eYear);

	this.uPermType=Wtf.UPerm.invoice;
	this.permType=Wtf.Perm.invoice;
	this.exportPermType=(this.receivable?this.permType.exportdataagedreceivable:this.permType.exportdataagedpayable);
	this.printPermType=(this.receivable?this.permType.printagedreceivable:this.permType.printagedpayable);
	this.chartPermType=(this.receivable?this.permType.chartagedreceivable:this.permType.chartagedpayable);
		
	this.MonthlySalesRecord = new Wtf.data.Record.create([
	{name: 'customerid'},
	{name: 'customername'},
	{name: 'customercode'},
        {name: 'productid'},
        {name: 'productname'},
        {name: 'pid'},
        {name: 'salesAccountName'},
	{name: 'amount_0'},
        {name: 'amount_1'},
        {name: 'amount_2'},
        {name: 'amount_3'},
        {name: 'amount_4'},
        {name: 'amount_5'},
        {name: 'amount_6'},
        {name: 'amount_7'},
        {name: 'amount_8'},
        {name: 'amount_9'},
        {name: 'amount_10'},
        {name: 'amount_11'},
        {name: 'amount_12'},
        {name: 'amount_13'},
        {name: 'amount_14'},
        {name: 'amount_15'},
        {name: 'amount_16'},
        {name: 'amount_17'},
        {name: 'amount_18'},
        {name: 'amount_19'},
        {name: 'amount_20'},
        {name: 'amount_21'},
        {name: 'amount_22'},
        {name: 'amountinbase'}
	]);
	this.RemoteSort= true;
        this.expGet = Wtf.autoNum.MonthlySales;

    this.MonthlySalesStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                remoteSort: this.RemoteSort,
                totalProperty:'count'
            },this.MonthlySalesRecord),
            url: this.isBasedOnProduct ? "ACCInvoiceCMN/getMonthlySalesByProductReport.do" :"ACCInvoiceCMN/getMonthlySalesReport.do",
            remoteSort: this.RemoteSort,
            baseParams:{
				mode: 18, // TODO - to review this
				creditonly:false,
				// nondeleted:false,
				// deleted:false,
				nondeleted:true,
				// deleted:false,
 				getRepeateInvoice: false,
              	consolidateFlag:config.consolidateFlag,
              	companyids:companyids,
              	gcurrencyid:gcurrencyid,
              	userid:loginid				
            },
            sortInfo : {
                field :'customername',
                direction : 'ASC'
            }
    });
    if (this.isCustomWidgetReport) {
        this.store = this.MonthlySalesStore;
    }

	this.typeStore = new Wtf.data.SimpleStore({
		fields: [{name:'typeid',type:"boolean"}, 'name'],
		data :[[true,WtfGlobal.getLocaleText("acc.rem.127")],[false,WtfGlobal.getLocaleText("acc.rem.128")]]
	});

	this.typeEditor = new Wtf.form.ComboBox({
		store: this.typeStore,
		name:'isdistributive',
		displayField:'name',
		value:true,
		anchor:"50%",
		valueField:'typeid',
		mode: 'local',
		triggerAction: 'all'
	});

	this.rowNo=new Wtf.KWLRowNumberer();

 	var columnArr = [];  
        
    var columnWidth = 120;
    var pdfWidth = 80;
    columnArr.push(this.rowNo, {
        header: this.isBasedOnProduct ? WtfGlobal.getLocaleText("acc.productList.gridProductID") : WtfGlobal.getLocaleText("acc.common.customer.code"), // Customer Name
        dataIndex: this.isBasedOnProduct ? 'pid' : 'customercode',
        align:'right',
        width:120,
        pdfwidth:110,
        hidden:true
    });

    columnArr.push( {
        header: this.isBasedOnProduct ? WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"): WtfGlobal.getLocaleText("acc.cust.name"), // Customer Name
        dataIndex: this.isBasedOnProduct ? 'productname' :'customername',
        align:'left',
        width:120,
        pdfwidth:110,
        hidden:false
        //sortable: this.RemoteSort
    }); 	
    
 
    if (this.isBasedOnProduct) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.productList.gridProductID"), // Product ID
            dataIndex: 'pid',
            align: 'left',
            width: 120,
            pdfwidth: 110,
            hidden: false
        });
    
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.product.salesAccount"), // "Sales Account",
            dataIndex: "salesAccountName",
            align: 'left',
            width: 120,
            pdfwidth: 110,
            hidden: false
        });
    }
    
    for (var i = 0; i < 22; i++) {
        columnArr.push({
            hidden: false,
            dataIndex: 'amount_' + i,
           renderer: this.formatMoney,
//            renderer:WtfGlobal.linkDeletedRenderer,
            width: columnWidth,
//            resizable: false,
            pdfwidth:110,
            align:'right'
//            style: 'text-align:left'
        });
    }
        columnArr.push({
            header:"<div><b>Total"+ "(" + WtfGlobal.getCurrencyName() + ")<b><div>", 
            dataIndex: "amountinbase",
            align: 'right',
            renderer: WtfGlobal.currencyRenderer,
            width: 150,
            pdfwidth: 110,
            hidden:false
        });


    // column model
    this.gridcm = new Wtf.grid.ColumnModel(columnArr);    

    this.grid = new Wtf.grid.GridPanel({
//        stripeRows :true,
        store:this.MonthlySalesStore,
        cm: this.gridcm,
        ctCls : 'monthlySalesreport',
        border:false,
        plugins:[this.expander],
        layout:'fit',
        viewConfig: {
//            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //ERP-28938
            deferEmptyText: false
        },
        loadMask : true
    });
	
	this.resetBttn=new Wtf.Toolbar.Button({
		text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
		hidden:this.isCustomWidgetReport,
		tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
		id: 'btnRec' + this.id,
		scope: this,
		iconCls :getButtonIconCls(Wtf.etype.resetbutton),
		disabled :false
	});

	this.chart=new Wtf.Toolbar.Button({
		text:WtfGlobal.getLocaleText("acc.common.chart"),  //'Chart',
		tooltip :WtfGlobal.getLocaleText("acc.graphTT.view")+' '+WtfGlobal.getLocaleText("acc.graphTT.monthlySalesReport"),
		id: 'chartRec'+config.helpmodeid,// + this.id,
		scope: this,
		handler:this.getChart,
		iconCls :(Wtf.isChrome?'accountingbase chartChrome':'accountingbase chart')

	});

    // get date from month & year drop-down lists
    if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
        var temp=new Date();
        var year1=temp.getFullYear();
        this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
        this.startYear.setValue(year1);
        this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
        this.endYear.setValue(year1);
    }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
        }      

        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox(1,2);
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
	
	this.expButton=new Wtf.exportButton({
		obj:this,
                filename:this.isBasedOnProduct ? "MonthlySalesByProductReport_v1":"Monthly_Sales_Report_v1",
		text:WtfGlobal.getLocaleText("acc.common.export"),
		tooltip :WtfGlobal.getLocaleText("acc.sales.exportTT"),  //'Export report details',
		disabled :true,
                hidden:this.isCustomWidgetReport,
		params:{ 
 			stdate: this.sDate,
                        enddate: this.eDate,			
			accountid:this.accountID||config.accountID,
			isdistributive:this.typeEditor.getValue(),
                        isBasedOnProduct : this.isBasedOnProduct
		},
		menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
		get:this.expGet
	})
	this.printButton=new Wtf.exportButton({
		obj:this,
		text:WtfGlobal.getLocaleText("acc.common.print"),
		tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  //'Print report details',
		disabled :true,
                hidden:this.isCustomWidgetReport,
                filename : this.isBasedOnProduct ? WtfGlobal.getLocaleText("acc.field.MonthlySalesByProduct"): WtfGlobal.getLocaleText("acc.wtfTrans.monthlySalesReport"),
		params:{ 	
			accountid:this.accountID||config.accountID,
 			stdate: this.sDate,
                        enddate: this.eDate,				
			isdistributive:this.typeEditor.getValue(),
			name: WtfGlobal.getLocaleText("acc.wtfTrans.monthlySalesReport"),
                        isBasedOnProduct : this.isBasedOnProduct
		},
		label: WtfGlobal.getLocaleText("acc.sales.tabTitle"),
		menuItem:{print:true},
		get:this.expGet
	})
    this.chartButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.chart"),
        iconCls: "accountingbase chart",
        scope: this,
        hidden : this.isCustomWidgetReport,
        menu: [
            {
                xtype: "button",
                text: WtfGlobal.getLocaleText("acc.common.BarChart"),
                iconCls: "x-tool-barchartwizard",
                scope: this,
                handler: function() {
                    var params = {} 
                    params.chartType = Wtf.chartType.bar;
                    this.showChart(params)
                }
            },
            {
                xtype: "button",
                text: WtfGlobal.getLocaleText("acc.common.PieChart"),
                iconCls: "x-tool-piechartwizard",
                scope: this,
                handler: function() {
                    var params = {} 
                    params.chartType = Wtf.chartType.pie;
                    this.showChart(params)
                }
            }
        ]
    })
	var btnArr=[];
	btnArr.push(
			this.quickPanelSearch = new Wtf.KWLTagSearch({
				emptyText: this.isBasedOnProduct ? WtfGlobal.getLocaleText("acc.saleByItem.search"): WtfGlobal.getLocaleText("acc.sales.searchcustomer"), // Search by Customer Name
						id:"quickSearch"+config.helpmodeid,
						width: 200,
						hidden:this.isCustomWidgetReport,
						field: 'customername'
			}),this.resetBttn);


	btnArr.push('-',WtfGlobal.getLocaleText("acc.common.from"),
		// this.startDate
		this.startMonth, this.startYear
		);
                    
	btnArr.push('-',WtfGlobal.getLocaleText("acc.common.to"),
		// this.endDate
		this.endMonth, this.endYear
		);
        
        if (this.isBasedOnProduct) {
            this.productTypeRec = Wtf.data.Record.create ([
                {name: 'id'},
                {name: 'name'}
            ]);
    
        this.productTypeStore = new Wtf.data.Store({
            url: "ACCProduct/getProductTypes.do",
            baseParams: {
                mode: 24,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productTypeRec)
        });
        
        this.productType = new Wtf.form.ComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.productType.tt") + "'>" + WtfGlobal.getLocaleText("acc.invReport.type") + "</span>",
            hiddenName: 'producttype',
            id: 'producttype'+this.id,
            store: this.productTypeStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true,
            width: 100
        });
        this.productTypeStore.load();
    
        this.productTypeStore.on("load", function() {
            var record = new Wtf.data.Record({
                id: "",
                name: "All Records"
            });
            this.productTypeStore.insert(0, record);
            this.productType.setValue("");
        }, this);
    
        btnArr.push('-', "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.product.productType.tt") + "'>" + WtfGlobal.getLocaleText("acc.invReport.type") + "</span>", this.productType);
    }
    

	btnArr.push("-",{
			xtype:'button',
			text:WtfGlobal.getLocaleText("acc.sales.fetch"),  //'Fetch',
			iconCls:'accountingbase fetch',
			scope:this,
			tooltip:WtfGlobal.getLocaleText("acc.sales.view"),  //"Select a date to view Monthly Sales Report
			handler:this.fetchMonthlySalesReport
		});
	
//	if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
		btnArr.push(this.expButton);
//	if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType))
		btnArr.push(this.printButton);
		btnArr.push(this.chartButton);
                
                /*
                 * Added filter combo in button array
                 */
                btnArr.push("->");
                if(!this.isCustomWidgetReport){
		btnArr.push("&nbsp;View", this.gstViewCombo);
                }
	this.resetBttn.on('click',this.handleResetClick,this);
	if(config.helpmodeid!=null){
            if(!this.isCustomWidgetReport){
		btnArr.push(getHelpButton(this,config.helpmodeid));
	}
	}
	Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
	items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.MonthlySalesStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.sales.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });

        Wtf.account.MonthlySalesReport.superclass.constructor.call(this,config);
        this.addEvents({
            'salesinvoices':true
        });
        this.grid.view.refresh.defer(1, this.grid.view);    //ERP-28938
        this.grid.on('cellclick',this.onCellClick, this);
        this.MonthlySalesStore.on("beforeload", function(s,o) {
            this.getStoreBaseParams(o);
            WtfGlobal.setAjaxTimeOut();
        },this);

	this.MonthlySalesStore.on("load",this.storeloaded,this);
	
//	this.MonthlySalesStore.load({       //ERP-28938
//		params:{
//			start:0,			
//			isdistributive:this.typeEditor.getValue(),
//			limit:30,
//			creditonly:true
//		}
//	});

    this.MonthlySalesStore.on("load", function(store){
    	
	// get month count from the first element
        var store = this.grid.getStore();
        
//        var monthArray = store.data.items[0].json["months"]; 
        var monthArray = [];
        if (this.isBasedOnProduct && store.data && store.data.items) {
            /**
             * this.isBasedOnProduct for Monthly Sales by Product.
             */
            monthArray = store.data.items[0].json.months;
        } else if (store.reader.jsonData && store.reader.jsonData.months) {
            // for Monthly Sales Report.
            monthArray = store.reader.jsonData.months;
        }
        var monthCount = monthArray.length;

        var monthToHideStart;
        if (this.isBasedOnProduct) {
            monthToHideStart = 5;
        } else {
            monthToHideStart = 3;
        }
        for(var i=0; i<monthArray.length; i++){
            if(monthArray[i]["monthname"]=='Total'){//Removed Link Form Total Column
               this.grid.getColumnModel().setRenderer((i+monthToHideStart),WtfGlobal.currencyRenderer);
               this.grid.getView().refresh();
            }else{
               this.grid.getColumnModel().setRenderer((i+monthToHideStart),this.formatMoney);
            }
            this.grid.getColumnModel().setColumnHeader((i+monthToHideStart), '<div><b>'+monthArray[i]["monthname"]+'</b></div>') ;        
        }   
    
        var columnCount =  this.grid.getColumnModel().getColumnCount();

        // show those months with data
        for(var i=monthToHideStart; i<(monthToHideStart+monthCount); i++){
            this.grid.getColumnModel().setHidden(i, false) ;
        }        

        // hide those months without data
        for(var i=(monthCount+monthToHideStart); i<columnCount-1; i++){
            this.grid.getColumnModel().setHidden(i,true) ;
        } 
        
        if(store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();

            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();      
            
        } //else
    
        WtfGlobal.resetAjaxTimeOut();
    },this);
    
	this.MonthlySalesStore.on('datachanged', function() {
            var rec = "";
            var p = 30;
            var index = this.MonthlySalesStore.find("customername", "total");
            if (index != -1) {
                rec = this.MonthlySalesStore.getAt(index);
                this.MonthlySalesStore.remove(rec);
            }
            if (rec != undefined && rec != "") {
                this.MonthlySalesStore.add(rec);
            }
            this.quickPanelSearch.setPage(p);
	}, this);

}

Wtf.extend( Wtf.account.MonthlySalesReport,Wtf.Panel,{  
    getStoreBaseParams: function (o) {
        if(o.params == undefined){
            o.params = {};
        }
        o.params.stdate = this.sDate;
        o.params.enddate = this.eDate;
        o.params.gstfilterflag = this.gstViewCombo.getValue();// 0 - means with  GST and 1- means without GST
        o.params.isFromMonthySalesReport = true;  //sending this value to identify the java side to called Monthly Sales Report or Monthly Sales by Product.
        o.params.ss = this.quickPanelSearch.getValue();
        o.params.productTypeID = this.isBasedOnProduct ? this.productType.getValue() : "";
    },
    showChart: function (param) {
        var chartPanelID;
        var title;
        var tabTipParams = [];
        if(this.isBasedOnProduct) {
            chartPanelID = (param.chartType == Wtf.chartType.bar) ? "monthlySalesByProductBarChart" : "monthlySalesByProductPieChart";
            title = (param.chartType == Wtf.chartType.bar) ? WtfGlobal.getLocaleText("acc.common.MonthlySalesByProduct.BarChart") : WtfGlobal.getLocaleText("acc.common.MonthlySalesByProduct.PieChart");
            tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.MonthlySalesByProduct"));
        } else {
            chartPanelID = (param.chartType == Wtf.chartType.bar) ? "monthlySalesBarChart" : "monthlySalesPieChart";
            title = (param.chartType == Wtf.chartType.bar) ? WtfGlobal.getLocaleText("acc.common.MonthlySales.BarChart") : WtfGlobal.getLocaleText("acc.common.MonthlySales.PieChart");
            tabTipParams.push(WtfGlobal.getLocaleText("acc.common.TT.MonthlySales"));
        }
        var chartParams = (param.chartType == Wtf.chartType.bar) ? this.getBarChartParams() : this.getPieChartParams();
        chartParams.id = chartPanelID;
        chartParams.title = title;
        chartParams.url = (param.chartType == Wtf.chartType.bar) ? this.getBarChartUrl() : this.getPieChartUrl();
        chartParams.chartConfig = (param.chartType == Wtf.chartType.bar) ? this.getBarChartConfig(chartParams) : this.getPieChartConfig(chartParams);
        chartParams.tabTipParams = tabTipParams;
        var chart = Wtf.getCmp(chartPanelID);
        if (chart) {
            Wtf.getCmp('as').remove(chart, true);
        }
        chart = getReportChartPanel(chartParams);
        Wtf.getCmp('as').add(chart);

        Wtf.getCmp('as').setActiveTab(chart);
        Wtf.getCmp('as').doLayout();
    },
    getBarChartParams: function () {
        var chartParams = {
            params :{}
        }
        this.getStoreBaseParams(chartParams);
        chartParams.params.chartType = Wtf.chartType.bar;
        return chartParams;
    },
    getPieChartParams: function () {
        var chartParams = {
            params :{}
        }
        this.getStoreBaseParams(chartParams);
        chartParams.params.chartType =  Wtf.chartType.pie;
        return chartParams;
    },
    getBarChartUrl: function () {
        return this.grid.store.url;
    },
    getPieChartUrl: function () {
        return this.grid.store.url;
    },
    getBarChartConfig :function(chartParams){
        var params = {};
        params.titleField = "monthname";
        params.valueField = "amountinbase";
        params.chartColor = (this.isBasedOnProduct) ? "#FF5757" : "#009987";
        params.textColor = (this.isBasedOnProduct) ? "#307D7E" : "#990012";
        params.valueTitle =  "Total Sales ( "+Wtf.pref.CurrencySymbol+" )";
        params.title = (this.isBasedOnProduct) ? "Monthly Sales By Product Report" : "Monthly Sales Report";
        params.unit = Wtf.pref.CurrencySymbol;
        
        if(chartParams.params.stdate && chartParams.params.enddate){
            params.subTitle = "From : " + chartParams.params.stdate +"\nTo : " + chartParams.params.enddate;
        }
        return getBarChartConfig(params);
    },
    getPieChartConfig :function(chartParams){
        
        var params = {};
        params.titleField = (this.isBasedOnProduct) ? "productname" : "customername";
        params.valueField = "amountinbase";
        params.chartColor = (this.isBasedOnProduct) ? "#1F45FC" : "#7D1B7E";
//        params.textColor = (this.isBasedOnProduct) ? "#00ADB5" : "#00ADB5";
        params.title = (this.isBasedOnProduct) ? "Monthly Sales By Product Report Against Customer" : "Monthly Sales Report Against Customer";
        params.unit = Wtf.pref.CurrencySymbol;
        
        if(chartParams.params.stdate && chartParams.params.enddate){
            params.subTitle = "From : " + chartParams.params.stdate +"\nTo : " + chartParams.params.enddate;
        }
        
        return getPieChartConfig(params);
    },

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
			this.MonthlySalesStore.load({
				params: {
					start:0,					
					isdistributive:this.typeEditor.getValue(),
					limit:this.pP.combo.value,
					aged:true,
					creditonly:true
				}
			});
		}
	},
        
	storeloaded:function(store){
		if(store.getCount()==0){
			if(this.expButton)this.expButton.disable();
			if(this.printButton)this.printButton.disable();
		}else{
			if(this.expButton)this.expButton.enable();
			if(this.printButton)this.printButton.enable();
		}
		this.quickPanelSearch.StorageChanged(store);
	},

	formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRendererWithLink(val);
//        if(rec.data['fmt']){
//            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
//        }
//        else if(rec.data["level"]==0&&rec.data["customername"]!="")
//            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
//             fmtVal= "<a class='jumplink' href='#'>"+fmtVal+"</a>";
        return fmtVal;
    },

    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },

    // populate month & year drop-down list
    // SON REFACTOR - to move this function to Global / common class   	

    fetchMonthlySalesReport:function(){    
        if (this.startYear.getValue() > this.endYear.getValue() ){
            WtfComMsgBox(1,2);
            return;            
        }
        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return;
        }      

        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox(1,2);
            return;            
        }

        var months;
        months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
        months -= startMonthDate.getMonth();
        months += endMonthDate.getMonth();
        if (months<0)
            months=0;

        if (months>18){
            WtfComMsgBox(["Alert",'Only maximum 18 months are supported!' ], 2);
            return;            
        }  

        this.MonthlySalesStore.load({
            params:{
                isdistributive:this.typeEditor.getValue(),
                start:0,
                limit:this.pP.combo.value,
                creditonly:true
            }
        });

        this.expButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            isdistributive:this.typeEditor.getValue()
        });

        this.printButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            isdistributive:this.typeEditor.getValue(),
            name:this.isBasedOnProduct ? "Monthly Sales By Product Report" :"Monthly Sales Report"
        })
    },
        
   	onRowClick:function(g,i,e){
		e.stopEvent();
		var el=e.getTarget("a");
		if(el==null)return;
		var accid=this.MonthlySalesStore.getAt(i).data['accountid'];
		this.fireEvent('account',accid);
	},
        
        onCellClick:function(g,i,j,e){
            e.stopEvent();
            var el=e.getTarget("a");
            if(el==null)return;
            var header=g.getColumnModel().getDataIndex(j);
            if(header!=="customerid" ||header!=="customername" || header != "productid"  || header != "productname"){
                if(this.isBasedOnProduct){
                    var accid=this.MonthlySalesStore.getAt(i).data['accountid'];
                    var productid=this.MonthlySalesStore.getAt(i).data['productid'];
                    var monthyear=g.getColumnModel().config[j].header;
                    monthyear=monthyear.replace('<div><b>', '');
                    monthyear=monthyear.replace('</b></div>', '');
                    monthyear=monthyear.trim();
                    MonthlySalesInvoicesListByProduct(productid,true,this.consolidateFlag,customerid,monthyear);
                }else{
                    var accid=this.MonthlySalesStore.getAt(i).data['accountid'];
                    var customerid=this.MonthlySalesStore.getAt(i).data['customerid'];
                    var monthyear=g.getColumnModel().config[j].header;
                    monthyear=monthyear.replace('<div><b>', '');
                    monthyear=monthyear.replace('</b></div>', '');
                    monthyear=monthyear.trim();
                    this.fireEvent('salesinvoices',accid,true,this.consolidateFlag,customerid,monthyear);
                }
            }
        },
        
	getChart:function(){
		var chartid="monthlysalesreportchartid";
		var swf1="../../scripts/graph/krwcolumn/krwcolumn/krwcolumn.swf";
		var id1=this.receivable?"receivableid":"payableid"
		var dataflag1=this.receivable?"ACCInvoiceCMN/getAgedReceivableChart":"ACCGoodsReceiptCMN/getAccountPayableChart";
		var mainid=this.receivable?"mainAgedRecievable":"mainAgedPayable";
		var xmlpath1= this.receivable?'../../scripts/graph/krwcolumn/examples/AgesReceivable/agedreceivable_settings.xml':'../../scripts/graph/krwcolumn/examples/AgesPayable/agedpayable_settings.xml';
		var id2=this.receivable?"piereceivableid":"piepayableid"
		var swf2="../../scripts/graph/krwcolumn/krwpie/krwpie.swf";
		var dataflag2=this.receivable?"ACCInvoiceCMN/getAgedReceivablePie":"ACCGoodsReceiptCMN/getAgedReceivablePie";
		var xmlpath2= this.receivable?'../../scripts/graph/krwcolumn/examples/AgesReceivable/pieagedreceivable_settings.xml':'../../scripts/graph/krwcolumn/examples/AgesPayable/pieagedpayable_settings.xml';
		globalAgedChart(chartid,id1,swf1,dataflag1,mainid,xmlpath1,id2,swf2,dataflag2,xmlpath2,this.withinventory,true,false);
	}
});
/********************************************************************************
 *                Monthly Sales Invoices Report
 ********************************************************************************/


Wtf.account.MonthlySalesInvoicesPanel=function(config){
    Wtf.apply(this, config);
    this.isBasedOnProduct = config.isBasedOnProduct;
    this.moduleid=config.moduleId;
    this.customerid=config.customerid;
    this.productid = config.productid;
    this.monthyear=config.monthyear;
    this.startDateRec=Wtf.serverDate.clearTime();
    this.getDates();
   this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'entryno'},
        {name:'companyid'},
        {name:'currencysymbol'},
        {name:'currencyid'},
        {name:'billno'},
        {name:'personname'},
        {name: 'productname'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'amount'},
        {name:'amountinbase'},
        {name:'amountdueinbase'},
        {name:'amountdue'},
        {name:'externalcurrencyrate'},
        {name:'withoutinventory',type:'boolean'},
    ]);
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:this.startDateRec.getFirstDateOfMonth()
       
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:this.startDateRec.getLastDateOfMonth()
        
    });
    this.fetchbtn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.sales.fetch"),  //'Fetch',
        iconCls:'accountingbase fetch',
        scope:this,
        tooltip:WtfGlobal.getLocaleText("acc.sales.view"),  //"Select a date to view Monthly Sales Report
        handler:this.fetchMonthlySalesReport
    });
    
    this.StoreUrl = this.isBasedOnProduct? "ACCInvoiceCMN/getInvoicesByProduct.do" : "ACCInvoiceCMN/getInvoicesMerged.do" ;//this.businessPerson=="Customer" ?: "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    
    this.Store = new Wtf.data.GroupingStore({
        url:this.StoreUrl,
        baseParams:{
            deleted:false,
            nondeleted:true,
            cashonly:false,
            creditonly:false,
            CashAndInvoice:true,
//            onlyOutsatnding:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            customerid:this.customerid,
            productid : this.productid,
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            start: 0,
            limit: 30,
            isMonthlySalesInvoice: true   //ERP-20971
        },
        sortInfo : {
            field : this.isBasedOnProduct ? 'productname' :'personname',
            direction : 'ASC'
        },
        groupField : this.isBasedOnProduct ? 'productname' : 'personname',
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
  
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
    });
     
   this.Store.load();  
    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
      
    this.Store.on('load',function(){
        this.exportButton.enable()
         //params:{start:0,limit:30}
    },this);  
      
    this.Store.on('beforeload', function(){
        this.Store.baseParams = {
            deleted:false,
            nondeleted:true,
            cashonly:false,
            creditonly:false,
//            onlyOutsatnding:true,
            CashAndInvoice:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            customerid:this.customerid,
            productid: this.productid,
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            isMonthlySalesInvoice: true   //ERP-20971
        }
        
    }, this);
   
    this.summary = new Wtf.grid.GroupSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.gridView1 = new Wtf.grid.GroupingView({
        forceFit:true,
        showGroupName: true,
        enableNoGroups:true, // REQUIRED!
        hideGroupedColumn: false,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    var columnArr =[];   
    columnArr.push(this.sm,this.rowNo,{ 
        
        header:WtfGlobal.getLocaleText("acc.field.Invoice")+WtfGlobal.getLocaleText("acc.cn.9"),
        dataIndex:'billno',
        pdfwidth:70
       // renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
    },{
        header: this.isBasedOnProduct ? WtfGlobal.getLocaleText("acc.contract.product.name") :WtfGlobal.getLocaleText("acc.invoiceList.cust"),       //(config.isCustomer? :WtfGlobal.getLocaleText("acc.invoiceList.ven")),  //this.businessPerson,
        pdfwidth:75,
        renderer:WtfGlobal.deletedRenderer,
        dataIndex: this.isBasedOnProduct ? 'productname' :'personname',
        sortable:true,
        summaryRenderer:function(){
            return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
        }
       },{  
        header:WtfGlobal.getLocaleText("acc.rem.34"),  //"Invoice  Date
        dataIndex:'date',
        width:50,
        align:'center',
        pdfwidth:80,
        sortable:true,
        renderer:WtfGlobal.onlyDateDeletedRenderer
   
      }
        );         
//    columnArr = WtfGlobal.appendCustomColumn(columnArr,this.customColArr,true);
    columnArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.due"),  //"Due Date",
            dataIndex:'duedate',
            align:'center',
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        
        },{
            header:WtfGlobal.getLocaleText("acc.mp.amtDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align:'right',
            pdfwidth:70,
            hidden:this.isBasedOnProduct,
            //pdfrenderer : "rowcurrency",
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol            //currencyRendererDeletedSymbol
         },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),   //+ " ("+WtfGlobal.getCurrencyName()+")",  //"Total Amount (In Home Currency)",
            align:'right',
            dataIndex:'amount',
            pdfwidth:80,
            //hidecurrency : true,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
        header:WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase")+ " ("+WtfGlobal.getCurrencyName()+")",        //"Amount in Base Currency",
        dataIndex:'amountinbase',
        align:'right',
        pdfwidth:80,
        //pdfrenderer : "rowcurrency",
        summaryType:'sum',
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer, 
        summaryRenderer: function(value, m, rec) {
            if (value != 0) {
                var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                return retVal;
            } else {
                return '';
            }
        }
       
            
    });  
        
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.grid.GridPanel({
        //id:"gridmsg"+this.id,
        stripeRows :true,
        store:this.Store,
        //tbar : this.tbar2,
        sm:this.sm,
        border:false,
        viewConfig: this.gridView1,
        forceFit:true,
        layout:'fit',
        plugins: [gridSummary],
       // loadMask : true,  
        cm:new Wtf.grid.ColumnModel(columnArr),
        listeners: {
            "reloadexternalgrid": function() {
                if (!this.searchparam)
                    this.loaddata.defer(10, this);
//                else
                    //this.showAdvanceSearch.defer(10, this);
            },
            scope: this
        }
    });
//this.objsearchComponent.on("clearStoreFilter", this.clearStoreFilter, this);

    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClickNew,this);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" Invoice",
        width: 150,
        //      id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    })
    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            name:this.isBasedOnProduct ? "Monthly Sales By Product" :"Monthly Sales",
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            isBasedOnProduct : this.isBasedOnProduct,
            productid : this.isBasedOnProduct ? this.productid :""
        },
        get:Wtf.autoNum.Invoice
    });
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
        label:WtfGlobal.getLocaleText("acc.invoiceList.SalesInvoices"),
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.Invoice,
        params:{
            name:this.isBasedOnProduct ? "Monthly Sales By Product" :"Monthly Sales",
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            isBasedOnProduct : this.isBasedOnProduct        
        }
    });
this.leadpan = new Wtf.Panel({
    layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items:[             //this.objsearchComponent,
            {
            region:'center',
            layout:'fit',
            border:false,
            items:[this.grid],
            tbar : [this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), 
            this.endDate,'-',this.fetchbtn,'-',this.exportButton, '-', this.printButton],//,'-'this.AdvanceSearchBtn,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({   
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
             id : "pPageSize_"+this.id
            })
        })
        }]
       
    });
     Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });   
    Wtf.account.MonthlySalesInvoicesPanel.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.MonthlySalesInvoicesPanel,Wtf.Panel,{
  
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    loaddata : function(){
     
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params : {
                start:0,
                limit:15
                            
            }
        });
        this.exportButton.enable();
    },
    fetchMonthlySalesReport:function(){ 
        
        if (this.startDate.getValue() > this.endDate.getValue() ){
            WtfComMsgBox(1,2);
            return;            
        }
        this.Store.load({
            params:{
                start:0,
                limit:this.pP.combo.value,
                creditonly:true
            }
        });
    }, 
    handleResetClickNew:function(){ 

        this.quickPanelSearch.reset();
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));

        this.Store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
       
    },
    
    getDates: function() {
       var monthyearRec;
       var str=this.monthyear;
        monthyearRec=str.split(' ');
        var month=monthyearRec[0];
        var year=monthyearRec[1];
        //    this.customColArr=config.customColArr,
        var monthvalue=0;
        if(this.monthyear!=undefined){
        switch(month){
           case 'Jan':
               monthvalue=0;
               break;
           case 'Feb':
               monthvalue=1;
               break;
           case 'Mar':
               monthvalue=2;
               break;
           case 'Apr':
               monthvalue=3;
               break;
           case 'May':
               monthvalue=4;
               break;
           case 'Jun':
               monthvalue=5;
               break;
           case 'Jul':
               monthvalue=6;
               break;
           case 'Aug':
               monthvalue=7;
               break;
           case 'Sep':
               monthvalue=8;
               break;
           case 'Oct':
               monthvalue=9;
               break;
           case 'Nov':
               monthvalue=10;
               break;
           case 'Dec':
               monthvalue=11;
               break;
           
        }
        
        this.startDateRec.setMonth(monthvalue);
        this.startDateRec.setYear(year);
        } 
    }
   
});
