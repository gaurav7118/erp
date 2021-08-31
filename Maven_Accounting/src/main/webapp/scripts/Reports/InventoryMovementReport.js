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
function getInventoryMovementDetailsReportDynamicLoad(){
    var panel = Wtf.getCmp("newInventoryMovementReport");
    if(panel==null){
        panel = new Wtf.account.InventoryMovementReport({
            id : 'newInventoryMovementReport',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.InventoryMovementDetailsReport.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.InventoryMovementDetailsReport.tabTitleTT"),
            topTitle:'<center><font size=4>' + WtfGlobal.getLocaleText("acc.InventoryMovementDetailsReport.tabTitle") + '</font></center>',
            statementType:'BalanceSheet',
            border : false,
            closable: true,
            isDetailReport:true,
            layout: 'fit',
            filename:WtfGlobal.getLocaleText("acc.InventoryMovementDetailsReport.tabTitle")+"_v1"
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.westPanel.setWidth(panel.getInnerWidth()/2);
        panel.doLayout();
    });
}
function getInventoryMovementSummaryReportDynamicLoad(){
    var panel = Wtf.getCmp("newInventoryMovementSummaryReport");
    if(panel==null){
        panel = new Wtf.account.InventoryMovementReport({
            id : 'newInventoryMovementSummaryReport',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.InventoryMovementSummaryReport.tabTitle"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.InventoryMovementSummaryReport.tabTitleTT"),
            topTitle:'<center><font size=4>' + WtfGlobal.getLocaleText("acc.InventoryMovementSummaryReport.tabTitle") + '</font></center>',
            statementType:'BalanceSheet',
            border : false,
            closable: true,
            layout: 'fit',
            filename:WtfGlobal.getLocaleText("acc.InventoryMovementSummaryReport.tabTitle")+"_v1"
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.westPanel.setWidth(panel.getInnerWidth()/2);
        panel.doLayout();
    });
}
//*****************************************************************************************************


Wtf.account.InventoryMovementReport=function(config){
    Wtf.apply(this,config);
    this.isDetailReport=config.isDetailReport;
    var rowno=0;
    this.noOfMonths=0;
    this.expander = new Wtf.grid.RowExpander({ 
        renderer: function(v, p, record) {
                if (rowno<(record.store.data.items.length-1)) {
                    p.cellAttr = 'rowspan="2"';
                    rowno++;
                    return '<div class="x-grid3-row-expander"></div>';
                } else {
                    p.id = '';
                    return '&#160;';
                }
              }
            });
        
    // to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'monthid',
            type:'int'
        }, 'name'],
        data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
        [10,'November'],[11,'December']]
    });

    var data=WtfGlobal.getBookBeginningYear(true);
    
    this.yearStore= new Wtf.data.SimpleStore({
        fields: [{
            name:'id',
            type:'int'
        }, 'yearid'],
        data :data
    });

    this.startMonth = new Wtf.form.ComboBox({
        store: this.monthStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
        name:'startMonth',
        displayField:'name',
        forceSelection: true,
        anchor:'95%',
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
        displayField:'yearid',
        anchor:'95%',
        width:80,
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
        anchor:'95%',
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
        anchor:'95%',
         width:80,
        valueField:'yearid',
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true
    });       

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
//to add location criteria
this.locTypeRec = new Wtf.data.Record.create([
        {name:"levelId"},
        {name:"levelName"},
    ]);
    
    this.locTypeReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.locTypeRec);

    this.locTypeStore = new Wtf.data.Store({
        url:"ACCMaster/getLevelsCombo.do",
        reader:this.locTypeReader
    });
    this.locTypeStore.load();
    this.locTypeEditor = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
        valueField:'levelId',
        displayField:'levelName',
        store:this.locTypeStore,
        anchor:'90%',
        width:150,
        emptyText:WtfGlobal.getLocaleText("acc.stockValuationDetail.LocEmptyText"),
        listWidth:200,
        value:this.type,
        typeAhead: true,
        forceSelection: true,
        name:'locationtp'
    });   
  
    
    this.locItemRec = new Wtf.data.Record.create([
        {name:"id"},
        {name:"name"},
    ]);
   
    this.locItemReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.locItemRec);
    
    this.locItemStore = new Wtf.data.Store({
        url:"ACCMaster/getLocItems.do",
        reader:this.locItemReader
    });
    
    this.locItemEditor = new Wtf.form.ComboBox({
        triggerAction:'all',
        id:'locItemEditor',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
        valueField:'id',
        displayField:'name',
        disabled:true,
        store:this.locItemStore,
        width:100,
         listWidth:200,
        anchor:'90%',
        emptyText:WtfGlobal.getLocaleText("acc.ra.value"),
        value:this.record ? this.record.data.locationid : '',
        typeAhead: true,
        forceSelection: true,
        name:'locitems'
    });

      this.locTypeEditor.on("select",function(){
        var itemBox=Wtf.getCmp('locItemEditor') 
        itemBox.enable();
        var levelid=this.getValue();
        var transType=(levelid==1 ? 'location' :(levelid==2 ? 'warehouse': (levelid==3 ? 'row' :(levelid==4 ? 'rack' : 'bin'))));
        var store=Wtf.getCmp('locItemEditor').store;
        store.load({
            params:{
                levelid:levelid,
                transType:transType
            }
        })
    });  
    
    this.locItemStore.on("load",function(){
        var record = new Wtf.data.Record({
            id: "",
            name: "All Records"
        });
        this.insert(0, record);
    });
    this.locTypeStore.on("load",function(){
        var record = new Wtf.data.Record({
            id: "",
            name: "All Records"
        });
        this.insert(0, record);
//        this.locItemEditor.setValue("");
    });   
  ////      



    this.BuildAssemblyRecord = new Wtf.data.Record.create([
    {
        name: 'productid'
    },{
        name: 'prodid'
    },
    {
        name: 'productname'
    },{
        name: 'uom'
    },
    {
        name: 'amount_0'
    },

    {
        name: 'qty_0'
    },

    {
        name: 'amount_1'
    },

    {
        name: 'qty_1'
    },

    {
        name: 'amount_2'
    },

    {
        name: 'qty_2'
    },

    {
        name: 'amount_3'
    },

    {
        name: 'qty_3'
    },

    {
        name: 'amount_4'
    },

    {
        name: 'qty_4'
    },

    {
        name: 'amount_5'
    },

    {
        name: 'qty_5'
    },

    {
        name: 'amount_6'
    },

    {
        name: 'qty_6'
    },

    {
        name: 'amount_7'
    },

    {
        name: 'qty_7'
    },

    {
        name: 'amount_8'
    },

    {
        name: 'qty_8'
    },

    {
        name: 'amount_9'
    },

    {
        name: 'qty_9'
    },

    {
        name: 'amount_10'
    },

    {
        name: 'qty_10'
    },

    {
        name: 'amount_11'
    },

    {
        name: 'qty_11'
    },

    {
        name: 'amount_12'
    },

    {
        name: 'qty_12'
    },

    {
        name: 'amount_13'
    },

    {
        name: 'qty_13'
    },

    {
        name: 'amount_14'
    },

    {
        name: 'qty_14'
    },

    {
        name: 'amount_15'
    },

    {
        name: 'qty_15'
    },

    {
        name: 'amount_16'
    },

    {
        name: 'qty_16'
    },

    {
        name: 'amount_17'
    },

    {
        name: 'qty_17'
    },

    {
        name: 'amount_18'
    },

    {
        name: 'qty_18'
    },

    {
        name: 'amount_19'
    },

    {
        name: 'qty_19'
    },

    {
        name: 'amount_20'
    },

    {
        name: 'qty_20'
    },

    {
        name: 'amount_21'
    },

    {
        name: 'qty_21'
    },

    {
        name: 'amount_22'
    },

    {
        name: 'qty_22'
    },
    ]);
	
    this.expGet = Wtf.autoNum.InventoryMovementReport;

    this.BuildAssemblyStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.BuildAssemblyRecord),
        url: "ACCReports/getInventoryMovementReport.do",     
        baseParams:{
            mode: 18, // TODO - to review this
            companyids:companyids
        }
    });

    this.expandRec = Wtf.data.Record.create ([
    {
        name: 'prodid'
    },
    {
        name: 'productname'
    },

    {
        name: 'amount_0'
    },

    {
        name: 'qty_0'
    },

    {
        name: 'amount_1'
    },

    {
        name: 'qty_1'
    },

    {
        name: 'amount_2'
    },

    {
        name: 'qty_2'
    },

    {
        name: 'amount_3'
    },

    {
        name: 'qty_3'
    },

    {
        name: 'amount_4'
    },

    {
        name: 'qty_4'
    },

    {
        name: 'amount_5'
    },

    {
        name: 'qty_5'
    },

    {
        name: 'amount_6'
    },

    {
        name: 'qty_6'
    },

    {
        name: 'amount_7'
    },

    {
        name: 'qty_7'
    },

    {
        name: 'amount_8'
    },

    {
        name: 'qty_8'
    },

    {
        name: 'amount_9'
    },

    {
        name: 'qty_9'
    },

    {
        name: 'amount_10'
    },

    {
        name: 'qty_10'
    },

    {
        name: 'amount_11'
    },

    {
        name: 'qty_11'
    },

    {
        name: 'amount_12'
    },

    {
        name: 'qty_12'
    },

    {
        name: 'amount_13'
    },

    {
        name: 'qty_13'
    },

    {
        name: 'amount_14'
    },

    {
        name: 'qty_14'
    },

    {
        name: 'amount_15'
    },

    {
        name: 'qty_15'
    },

    {
        name: 'amount_16'
    },

    {
        name: 'qty_16'
    },

    {
        name: 'amount_17'
    },

    {
        name: 'qty_17'
    },

    {
        name: 'amount_18'
    },

    {
        name: 'qty_18'
    },

    {
        name: 'amount_19'
    },

    {
        name: 'qty_19'
    },

    {
        name: 'amount_20'
    },

    {
        name: 'qty_20'
    },

    {
        name: 'amount_21'
    },

    {
        name: 'qty_21'
    },

    {
        name: 'amount_22'
    },

    {
        name: 'qty_22'
    }
    ]);
    this.expandStore = new Wtf.data.Store({
        url:"ACCReports/getBuildAssemblyDetails.do",
        baseParams:{
            mode:25
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    this.expandStore.on('load',this.fillExpanderBodyNew,this);

    this.expander.on("expand",this.onRowexpand,this);
    this.rowNo=new Wtf.grid.RowNumberer();

    var columnArr = [];  

    var columnWidth = 100;
    var pdfWidth = 80;
    
    if(this.isDetailReport){
        columnArr.push(this.expander);
    }else{
    columnArr.push({
        header: '',   //blank column added as in export interface columns are shown from 1st index instead of 0th
        align:'right',
        width:120,
        pdfwidth:110,
        hidden:true
    })}
    columnArr.push({
        header: WtfGlobal.getLocaleText("acc.product.gridProductID"), // Product ID
        dataIndex: 'prodid',
        align:'right',
        width:120,
        pdfwidth:110
    }); columnArr.push({
        header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"), // UOM
        dataIndex: 'uom',
        align:'right',
        width:120,
        pdfwidth:110
    });
    columnArr.push({
        header: WtfGlobal.getLocaleText("acc.product.gridProductID"), // Product ID
        dataIndex: 'productid',
        align:'right',
        width:120,
        pdfwidth:110,
        hidden:true
    });

    columnArr.push( {
        header: WtfGlobal.getLocaleText("acc.product.gridProduct"), // Product
        dataIndex: 'productname',
        align:'right',
        width:120,
        pdfwidth:110,
        hidden:false
    }); 	

    for (var i = 0; i < 22; i++) {
        columnArr.push({
            hidden: false,
            dataIndex: 'qty_' + i,
            header:'Quantity',
            width: columnWidth+5,
            resizable: false,
            pdfwidth:110,
            align:'right',
            style: 'text-align:left'
        });
        columnArr.push({
            hidden: false,
            dataIndex: 'amount_' + i,
            header:'Cost'+i,
            renderer: this.formatMoney,
            //            renderer:WtfGlobal.linkDeletedRenderer,
            width: columnWidth,
            resizable: false,
            pdfwidth:110,
            align:'right',
            style: 'text-align:left'
        });
        
    }
     

    // column model
    this.gridcm = new Wtf.grid.ColumnModel(columnArr);    

    this.grid = new Wtf.grid.GridPanel({
        store:this.BuildAssemblyStore,
        cm: this.gridcm,
        border:false,
        plugins:this.isDetailReport ? [this.expander] : '',
        layout:'fit',
        viewConfig: {
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        loadMask : true
    });
	
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        hidden:false,
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });



    /////
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
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported") ], 2);
        return;            
    }    
   
    this.printButton=new Wtf.exportButton({
        obj:this,
        filename:this.isDetailReport ? WtfGlobal.getLocaleText("acc.InventoryMovementDetailsReport.tabTitle") : WtfGlobal.getLocaleText("acc.InventoryMovementSummaryReport.tabTitle"),
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  //'Print report details',
        disabled :true,
        params:{ 	
            accountid:this.accountID||config.accountID,
            stdate: this.sDate,
            enddate: this.eDate,				
            name: WtfGlobal.getLocaleText("acc.wtfTrans.monthlySalesReport")
        },
        label: WtfGlobal.getLocaleText("acc.sales.tabTitle"),
        menuItem:{
            print:true
        },
        get:this.expGet
    })
    var btnArr=[];
    btnArr.push(
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.InventoryMovementReport.searchtext"), // Search by Product Name/Id
            id:"quickSearch"+config.helpmodeid,
            width: 200,
            hidden:false,
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
     
    this.expButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        filename:this.filename,
        params:{ 
            stdate: this.sDate,
            enddate: this.eDate,
            isinventoryDetails : this.isDetailReport ? this.isDetailReport : false,
            noOfMonths:this.noOfMonths
        },
        menuItem:{
            csv:true,
            pdf:this.isDetailReport ? false : true, //ERP-13359 : Hide Export PDF option from Inventory Movement Details Reeport
            rowPdf:false,
            xls:true
        },
        get:this.expGet
    })
    //    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))
      
    var btnArr1=[];
    btnArr1.push( '-', WtfGlobal.getLocaleText("acc.field.Select"),this.locTypeEditor," ",this.locItemEditor);
    btnArr1.push("-",{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.sales.fetch"),  //'Fetch',
        iconCls:'accountingbase fetch',
        scope:this,
        tooltip:WtfGlobal.getLocaleText("acc.InventoryMovementDetailReport.view"),  //"Select a date to Inventory Movement Report
        handler:this.fetchInventoryMovementReport
    });
    btnArr1.push(this.expButton);
    //if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType))
        btnArr1.push(this.printButton);
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.helpmodeid!=null){
        btnArr1.push("->");
        btnArr1.push(getHelpButton(this,config.helpmodeid));
    }
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items: [{
            layout: 'fit',
            border: false,
            tbar:btnArr1,
            items:[this.grid]
        }],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.BuildAssemblyStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.1099.noresult"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });
    
    Wtf.account.InventoryMovementReport.superclass.constructor.call(this,config);
//    this.addEvents({
//        'salesinvoices':true
//    });
      
    this.BuildAssemblyStore.on("beforeload", function(s,o) {
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
        this.noOfMonths=months;
        o.params.stdate = this.sDate;
        o.params.enddate = this.eDate;
        this.expButton.params.stdate=this.sDate;
        this.expButton.params.enddate= this.eDate;
        this.expButton.params.isinventoryDetails = this.isDetailReport ?  this.isDetailReport : false;
        this.expButton.params.type=this.locTypeEditor.getValue();
        this.expButton.params.ss=this.quickPanelSearch.getValue();
        this.expButton.params.locationid =this.locItemEditor.getValue();
        this.expButton.params.noOfMonths=this.noOfMonths;
        rowno=0;
    },this);

    this.BuildAssemblyStore.on("load",this.storeloaded,this);
	
    this.BuildAssemblyStore.load({
        params:{
            start:0,			
            limit:30,
            creditonly:true
        }
    });

    this.BuildAssemblyStore.on("load", function(store){
    	
        // get month count from the first element
        var store = this.grid.getStore();

        var monthArray = store.data.items[0].json["months"];
        var monthCount = monthArray.length;
//        var monthToHideStart = 3;
//        if(this.isDetailReport) 
         var monthToHideStart = 5;
        var colno=0;
        for(var i=0; i<monthArray.length ; i++){
//            if(monthArray[i]["monthname"]=='Total'){//Removed Link Form Total Column
//                this.grid.getColumnModel().setRenderer((i+monthToHideStart),WtfGlobal.currencyRenderer);
//                this.grid.getView().refresh();
//            }
            this.grid.getColumnModel().setColumnHeader((colno+monthToHideStart), '<div><b>'+monthArray[i]["monthname"]+'</b>'+'<b> Quantity'+'</b></div>') ;       
            colno++;
            this.grid.getColumnModel().setColumnHeader((colno+monthToHideStart), '<div><b>'+monthArray[i]["monthname"]+'</b>'+'<b> Cost'+'</b></div>') ;        
            colno++;
        }   

        var columnCount =  this.grid.getColumnModel().getColumnCount();

        // show those months with data
        for(var i=monthToHideStart; i<(monthToHideStart+(monthCount*2)); i++){
            this.grid.getColumnModel().setHidden(i, false) ;
        }        

        // hide those months without data
        for(var i=((monthCount*2)+monthToHideStart); i<columnCount; i++){
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
    },this);
}

Wtf.extend( Wtf.account.InventoryMovementReport,Wtf.Panel,{  
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
        this.endMonth.reset();
        this.startYear.reset();
        this.startMonth.reset();
        this.endYear.reset();
        this.locItemEditor.reset();
        this.locTypeEditor.reset();
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
        }
       this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
       this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();
          this.BuildAssemblyStore.load({
                params: {
                    start:0,					
                    limit:this.pP.combo.value,
                    aged:true,
                    creditonly:true,
                    ss:this.quickPanelSearch.getValue()
                }
            });
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
        var fmtVal=WtfGlobal.currencyRenderer(val);
        return fmtVal;
    },

    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },

//    getBookBeginningYear:function(isfirst){
//        var ffyear;
//        if(isfirst){
//            var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
//            ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)
//            ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()
//        }
//        else{
//            var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
//            ffyear=new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime()
//        }
//
//        var data=[];
//        var newrec;
//        if(ffyear==null||ffyear=="NaN"){
//            ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)
//        }
//        var year=ffyear.getFullYear();
//        var temp=new Date();
//        var year1=temp.getFullYear();
//        data.push([0,year1]);
//        var i=1;
//        while(year1>=year){
//            data.push([i,--year1]);
//            i++;
//        }
//        if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
//            data.push([1,year+1]);
//            newrec = new Wtf.data.Record({
//                id:1,
//                yearid:year+1
//            });
//        }
//        return data;
//    },     	

    fetchInventoryMovementReport:function(){

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
        this.noOfMonths=months;
        if (months>18){
            WtfComMsgBox(["Alert",'Only maximum 18 months are supported!' ], 2);
            return;            
        }  

        this.BuildAssemblyStore.load({
            params:{
                start:0,
                limit:this.pP.combo.value,
                type:this.locTypeEditor.getValue(),
                ss:this.quickPanelSearch.getValue(),
                locationid :this.locItemEditor.getValue()
            }
        });

        this.expButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate			
        });

        this.printButton.setParams({
            stdate: this.sDate,
            enddate: this.eDate,			
            name: "Inventory Movement Report"
        })
    },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.expandStore.load({
            params:{
                productid:record.data.productid,
                stdate: this.sDate,
                enddate: this.eDate
            }
        });
    },
//    fillExpanderBody:function(){
//        var disHtml = "";
//        var arr=[];
// 
//        arr=[ WtfGlobal.getLocaleText("acc.build.1"),WtfGlobal.getLocaleText("acc.product.gridQty"),WtfGlobal.getLocaleText("acc.buildassembly.gridBuildDate"),WtfGlobal.getLocaleText("acc.common.memo"),
//        WtfGlobal.getLocaleText("acc.rem.prodDesc.Mixed")];
//        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.InventoryMovementDetailsReport.builddetails")+"</span>";   
//        header += "<span class='gridNo' style='font-weight:bold;'>S.No.</span>";
//        for(var i=0;i<arr.length;i++){
//            header += "<span class='headerRow'>" + arr[i] + "</span>";
//        }
//        header += "<span class='gridLine'></span>";
//        for(var i=0;i<this.expandStore.getCount();i++){
//            var rec=this.expandStore.getAt(i);
//            
//            var description= rec.data['description'];
//            var quantity= rec.data['quantity'];
//            var entrydate= rec.data['entrydate'];
//            var productrefno= rec.data['productrefno'];
//            var memo= rec.data['memo'];
//            quantity= parseFloat(getRoundofValue(quantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
//              
//            header += "<span class='gridNo'>"+(i+1)+".</span>";
//            header += "<span class='gridRow'>"+productrefno+"&nbsp;</span>";
//            header += "<span class='gridRow'>"+quantity+"&nbsp;</span>";
//              header += "<span class='gridRow'>"+entrydate+"</span>";
//            header += "<span class='gridRow'>"+memo+"&nbsp;</span>";
//            header += "<span class='gridRow'  wtf:qtip='"+description+"'>"+Wtf.util.Format.ellipsis(description,15)+"&nbsp;</span>";
//          
//            header +="<br>";
//        }
//        disHtml += "<div class='expanderContainer2' style='width:100%'>" + header + "</div>";
//            
//        this.expanderBody.innerHTML = disHtml;
//    }

fillExpanderBodyNew:function(){
        var disHtml = "";
        var arr=[];
        var monthArray = this.expandStore.data.items[0].json["months"];
        var monthCount = monthArray.length;
        var monthToHideStart = 2;
        var colno=0;
        arr[0]='<div><b>Product Id</b></div>';
        arr[1]='<div><b>Product Name</b></div>';
        for(var i=0; i<monthArray.length ; i++){
            arr[colno+monthToHideStart]='<div><b>'+monthArray[i]["monthname"]+'</b>'+'<b> Quantity'+'</b></div>';
            colno++;
            arr[colno+monthToHideStart]='<div><b>'+monthArray[i]["monthname"]+'</b>'+'<b> Cost'+'</b></div>';
            colno++;
        }   
        var widthpercent=100/(colno+monthToHideStart);
        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.InventoryMovementDetailsReport.builddetails")+"</span>";   
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow' style='width:"+widthpercent+"%'>" + arr[i] + "</span>";
        }
        header += "<span class='gridLine'></span>";
        header +="<br>";
        for(var i=0;i<this.expandStore.getCount();i++){
            var rec=this.expandStore.getAt(i);
            var productId= rec.data['prodid'];
            var productName= rec.data['productname'];
            header += "<span class='gridRow' style='width:"+widthpercent+"%'>"+productId+"</span>";
            header += "<span class='gridRow' style='width:"+widthpercent+"%'>"+productName+"</span>";
            for(var j=0; j<monthArray.length ; j++){
                var quantity= rec.data['qty_'+j];
                var cost= rec.data['amount_'+j];
                header += "<span class='gridRow' style='width:"+widthpercent+"%'>"+quantity+"</span>";
                header += "<span class='gridRow' style='width:"+widthpercent+"%'>"+cost+"</span>";
            }
            header +="<br>";
        }
        disHtml += "<div class='expanderContainer2' style='width:100%'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    }
	
});


