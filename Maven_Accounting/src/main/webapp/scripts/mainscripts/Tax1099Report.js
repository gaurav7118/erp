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
/* <COMPONENT USED FOR>

 * 1. 1099 Report
 *      call1099Report() --- < View 1099 Report>
 *
 */
Wtf.account.Tax1099DetailReport=function(config){
    this.isexpenseinv=true;
    this.withinventory=true;
    this.only1099Acc=true;
    this.only1099Vend=true;
    this.isSummary=config.isSummary||false;
    this.taxPermType=Wtf.Perm.tax;
    this.taxPermType=Wtf.Perm.view
    this.GRRec = new Wtf.data.Record.create([{
            name:'billid'
        },{
            name:'journalentryid'
        },{
            name:'entryno'
        },{
            name:'billno'
        },{
            name:'date', type:'date'
        },{
            name:'personname'
        },{
            name:'amountdueinbase'
        },{
             name:'amountdue'  
        },{
            name:'total'    
        },{
            name:'total'
        },{
            name:'memo'
        },{
            name: 'currencysymbol'
        },{
            name: 'amount'
        },{
            name: 'accountnames'
         }
    ]);

    this.GRStoreUrl = "ACCGoodsReceiptCMN/getGoodsReceipts.do";
    this.expGet = 21;

    this.GRStore =new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.GRRec),
        groupField:'personname',
        sortInfo: {field: 'personname',direction: "DESC"},
        url: this.GRStoreUrl
    });
    this.rowNo=new Wtf.KWLRowNumberer();
    this.summary = new Wtf.ux.grid.GridSummary();
    this.cm= new Wtf.grid.ColumnModel([this.rowNo,{       
            header:WtfGlobal.getLocaleText("acc.1099.gridVIno"),  //"Vendor Invoice Number",
            dataIndex:'billno'
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridJEentry"),  //"Journal Entry Number",
            dataIndex:'entryno',
            pdfwidth:100,
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v},
            renderer:WtfGlobal.linkRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridbillDate"),  //"Bill Date",
            dataIndex:'date',
             pdfwidth:100,
            align:'center',
            groupRenderer:this.groupDateRender.createDelegate(this),
            renderer:WtfGlobal.onlyDateRenderer        
        },{
            header:WtfGlobal.getLocaleText("acc.1099.venAcc"),  //"Vendor Account Name",
            dataIndex:'personname',
            hidden:this.isexpenseinv,
            pdfwidth:150,
            sortable: true,
            groupable: true
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridAccName"),  //"Account Name",
            dataIndex:'accountnames',
            pdfwidth:150,
            sortable: true,
            groupable: true
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridTotalAmt"),  //'Total Amount',
            align:'right',
            pdfwidth:150,
            hidden: !this.isexpenseinv,
            dataIndex:"amount",
            pdfrenderer:"rowcurrency",
            renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridAmtDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align:'right',
            pdfwidth:120,
            renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",            
            align:'right',
            pdfwidth:150,
            dataIndex:'memo',
            pdfrenderer:"rowcurrency"
    }]);

    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.GRStore,
        cm:this.cm,
        border:false,
        plugins:[this.summary],
        layout:'fit',
        view:new Wtf.grid.GroupingView({
            forceFit:true
       }),
        loadMask : true
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.vendTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'boolean'}, 'name'],
        data :[[true,'Only 1099 Vendors'],[false,"All Vendors"]]
    });
    this.vendTypeEditor = new Wtf.form.ComboBox({
        store: this.vendTypeStore,
        name:'vendtype',
        displayField:'name',
        id:'viewvendtype'+config.helpmodeid,
        valueField:'typeid',
        mode: 'local',
        value:true,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });

    this.accTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'boolean'}, 'name'],
        data :[[true,'Only 1099 Accounts'],[false,"All Accounts"]]
    });
    this.accTypeEditor = new Wtf.form.ComboBox({
        store: this.accTypeStore,
        name:'acctype',
        displayField:'name',
        id:'viewacctype'+config.helpmodeid,
        valueField:'typeid',
        mode: 'local',
        value:true,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    this.curDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.till"),  //'Till',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        id: 'dueDate'+config.helpmodeid,
        value:new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM")
    });
//    this.expButton=new Wtf.exportButton({
//        obj:this,
//        tooltip :'Export report details',
//        disabled :true,
//        params:{ stdate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
//             only1099Acc:this.only1099Acc,
//             only1099Vend:this.only1099Vend,
//             enddate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
//             accountid:this.accountID||config.accountID,
//             curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue())
//        },
//        menuItem:{csv:true,pdf:true,rowPdf:false},
//        get:this.expGet
//    })
//    this.printButton=new Wtf.exportButton({
//        obj:this,
//        tooltip :'Print report details',
//        disabled :true,
//        params:{ stdate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
//             only1099Acc:this.only1099Acc,
//             only1099Vend:this.only1099Vend,
//             enddate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
//             accountid:this.accountID||config.accountID,
//             curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue()),
//             name: "1099 Report"
//        },
//        lable: "1099 Report",
//        menuItem:{print:true},
//        get:this.expGet
//    })
    var btnArr=[];
    btnArr.push(
       this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.1099.search"), //'Search by Vendor Account Name',
            id:"quickSearch"+config.helpmodeid,
            width: 200,
            field: 'personname'
        }),
        this.resetBttn);
        btnArr.push('-',WtfGlobal.getLocaleText("acc.common.till"),this.curDate);

            if(this.isexpenseinv)
            btnArr.push("-",this.vendTypeEditor);
                if(this.isexpenseinv)
            btnArr.push("-",this.accTypeEditor);
        btnArr.push("-",{ 
            xtype:'button',
             text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.1099.view"),  //"Select a date to view 1099 Report.",
            handler:this.loadStore
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax1099, Wtf.Perm.tax1099.tax1099assigntax)){
	        btnArr.push("-",{
	            xtype:'button',
	            text:WtfGlobal.getLocaleText("acc.1099.taxCategory"),  //'Assign Tax Category',
	            iconCls:'accountingbase fetch',
	            scope:this,
	            tooltip:WtfGlobal.getLocaleText("acc.1099.taxCategoryTT"),  //'Assign Tax Category',
	            handler:this.assignTaxCategory
	        });
        }
      
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.helpmodeid!=null){
        btnArr.push("->");
        btnArr.push(getHelpButton(this,config.helpmodeid));
    }
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.GRStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
            emptyMsg:WtfGlobal.getLocaleText("acc.common.norec"),  // "No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });

    Wtf.account.Tax1099DetailReport.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.GRStore.on("beforeload", function(s,o) {
       s.baseParams={
        creditonly:true,
        for1099Report:true,
        withinventory:this.withinventory,
        only1099Acc:this.accTypeEditor.getValue(),
        ignorezero:!this.isexpenseinv,
        only1099Vend:this.vendTypeEditor.getValue(),
        curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue()),
        nondeleted:true}
    },this);
    this.GRStore.on('load',this.storeloaded,this);
    this.GRStore.load({
        params:{
            start:0,
            limit:30
        }
    });
    this.GRStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
    this.grid.on('cellclick',this.onCellClick, this);
}

Wtf.extend( Wtf.account.Tax1099DetailReport,Wtf.Panel,{
    assignTaxCategory:function(){
        callTax1099Window(this.id+"tax1099winid")
        Wtf.getCmp(this.id+"tax1099winid").on('update',function(){this.loadStore();},this);
    },
    
    loadStore:function(){
        this.GRStore.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    },

    groupDateRender:function(v){
       return v.format(WtfGlobal.getOnlyDateFormat())
    },

    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },

    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.GRStore.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true);
        }
    },

    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.GRStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    }
});
/////////////////////////////////////////////////////////////////////////////

Wtf.account.Tax1099SummaryReport=function(config){
    this.isexpenseinv=true;
    this.withinventory=true;
    this.taxPermType=Wtf.Perm.tax;
    this.taxPermType=Wtf.Perm.view
    this.GRRec = new Wtf.data.Record.create([{
            name:'accountid'
        },{
            name:'accountname'
        },{
            name:'amount'
        },{
            name:'personid'
        },{
            name:'personname'
        },{
            name:'categoryname'
        },{
            name:'categoryid'
        },{
            name:'abovethreshold'

        }
    ]);
    this.GRStoreUrl = "ACCJournalCMN/getTax1099JE.do";
    this.expGet = 21;

    this.GRStore =new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.GRRec),
        groupField:'categoryname',
        sortInfo: {field: 'categoryname',direction: "DESC"},
        url: this.GRStoreUrl
    });
    this.rowNo=new Wtf.KWLRowNumberer();
    this.summary = new Wtf.ux.grid.GridSummary();
    this.cm= new Wtf.grid.ColumnModel([this.rowNo,{
          header:WtfGlobal.getLocaleText("acc.1099.gridCategory"),  //"Category Name",
            dataIndex:'categoryname',
            pdfwidth:100,
            sortable: true,
            groupable: true,
            groupRenderer: function(v){return v}
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridVname"),  //"Vendor Name",
            dataIndex:'personname',
            pdfwidth:100,
            sortable: true,
            groupable: true
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridAccName"),  //"Account Name",
            dataIndex:'accountname',
            pdfwidth:150,
            sortable: true,
            groupable: true
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridAmt"),  //'Amount',
            align:'right',
            pdfwidth:150,
            dataIndex:"amount",
            pdfrenderer:"rowcurrency",
            renderer:WtfGlobal.currencyRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridAboveThreshold"),  //"Above Threshold",
            dataIndex:'abovethreshold',
            pdfwidth:150,
            sortable: true,
            groupable: true

    }]);

    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.GRStore,
        cm:this.cm,
        border:false,
        plugins:[this.summary],
        layout:'fit',
        view:new Wtf.grid.GroupingView({
            forceFit:true
       }),
        loadMask : true
    });

    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });

    this.curDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.till"),  //'Till',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        id: 'dueDate'+config.helpmodeid,
        value:new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM")
    });
//    this.expButton=new Wtf.exportButton({
//        obj:this,
//        tooltip :'Export report details',
//        disabled :true,
//        params:{ stdate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
//             enddate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
//             accountid:this.accountID||config.accountID,
//             curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue())
//        },
//        menuItem:{csv:true,pdf:true,rowPdf:false},
//        get:this.expGet
//    })
//    this.printButton=new Wtf.exportButton({
//        obj:this,
//        tooltip :'Print report details',
//        disabled :true,
//        params:{ stdate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
//             enddate:WtfGlobal.convertToGenericDate(new Date(new Date().format('M d, Y')+" 12:00:00 AM")),
//             accountid:this.accountID||config.accountID,
//             curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue()),
//             name: "1099 Report"
//        },
//        lable: "1099 Report",
//        menuItem:{print:true},
//        get:this.expGet
//    })
    var btnArr=[];
    btnArr.push(
       this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.1099.search"),  //'Search by Vendor Account Name',
            id:"quickSearch"+config.helpmodeid,
            width: 200,
            field: 'personname'
        }),
        this.resetBttn);
        btnArr.push('-',WtfGlobal.getLocaleText("acc.common.till"),this.curDate);
        btnArr.push("-",{
            xtype:'button',
             text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.1099.view"),  //"Select a date to view 1099 Report.",
            handler:this.loadStore
        });
    this.resetBttn.on('click',this.handleResetClick,this);
    if(config.helpmodeid!=null){
        btnArr.push("->");
        btnArr.push(getHelpButton(this,config.helpmodeid));
    }
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        tbar:btnArr,
        items:[this.grid],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.GRStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
            emptyMsg: WtfGlobal.getLocaleText("acc.1099.noresult"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });

    Wtf.account.Tax1099SummaryReport.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });

    this.GRStore.on("beforeload", function(s,o) {
        o.params.curdate= WtfGlobal.convertToGenericDate(this.curDate.getValue());
         this.GRStore.baseParams={
            creditonly:true,
            withinventory:this.withinventory,
            ignorezero:!this.isexpenseinv,
            curdate: WtfGlobal.convertToGenericDate(this.curDate.getValue()),

            nondeleted:true}
    },this);
    this.GRStore.on('load',this.storeloaded,this);
    this.GRStore.load({
        params:{
            start:0,
            limit:30
        }
    });
    this.GRStore.on('datachanged', function() {
        var p = 30;
        this.quickPanelSearch.setPage(p);
    }, this);
    this.grid.on('cellclick',this.onCellClick, this);
}

Wtf.extend( Wtf.account.Tax1099SummaryReport,Wtf.Panel,{
     loadStore:function(){
        this.GRStore.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
    },

    groupDateRender:function(v){
       return v.format(WtfGlobal.getOnlyDateFormat())
    },

    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },

    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.GRStore.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true);
        }
    },

    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.GRStore.getAt(i).data['accountid'];
        this.fireEvent('account',accid);
    }
});
