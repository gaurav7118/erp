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

/**
 *
 * @author Pandurang
 */



function CustomColumnSummaryReportListDynamicLoad(){//Summary Report

    var panel = Wtf.getCmp("CustomColumnSummaryReport");
    if(panel==null){
        panel = new Wtf.account.CustomColumnSummaryReport({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.CustomColumnSummaryReport.Report"),Wtf.TAB_TITLE_LENGTH) ,//"Finance Details"
            tabTip:WtfGlobal.getLocaleText("acc.CustomColumnSummaryReport.Reporttooltip"),
            id:'CustomColumnSummaryReport',
            border:false,
            moduleId:102,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            closable: true

        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}

function CustomColumnDetailReportListDynamicLoad(){  //Detail Report

    var panel = Wtf.getCmp("CustomColumnDetailReport");
    if(panel==null){
        panel = new Wtf.account.CustomColumnDetailReport({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.CustomColumnDetailReport.Report"),Wtf.TAB_TITLE_LENGTH) ,//"Finance Details"
            tabTip:WtfGlobal.getLocaleText("acc.CustomColumnDetailReport.Reporttooltip"),
            id:'CustomColumnDetailReport',
            border:false,
            isFromSummaryReport:false,
            searchJson:"",
            filterConjuctionCrit:"",
            moduleId:102,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            closable: true

        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}
/*******************************************************************************
 *             Custom Column Summary Report
 *******************************************************************************/
Wtf.account.CustomColumnSummaryReport=function(config){
    Wtf.apply(this, config);
    this.moduleid=102;             //config.moduleId;
   this.combostore = new Wtf.data.SimpleStore({
        fields: [
        {
            name: 'header'
        },

        {
            name: 'name'
        },
        {
            name: 'xtype'
        },
        {
            name: 'cname'
        },
        {
            name: 'iscustomcolumn'
        },
        {
            name: 'dbname'
        },
        {
            name:'sheetEditor'
        },
        {
            name:'fieldtype'
        },
        {
            name: 'refdbname'
        },
        {
            name: 'fieldid'
        },{
            name:'iscustomcolumndata'
        },{
            name:'moduleid'
        },{
            name: 'isdefaultfield'
        }]
    });

    this.columnCombo = new Wtf.form.ComboBox({
        store : this.combostore,
        editable:false,
        typeAhead: true,
        selectOnFocus:true,
        displayField:'header',
        valueField : 'fieldid',
        triggerAction: 'all',
        emptyText : WtfGlobal.getLocaleText("acc.responsealert.msg.12"),//'Select a Search Field to search',
        mode:'local'
    })

    this.columnCombo.on("select",function(){
        this.fetchBtn.enable();
    },this);
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.cm=new Wtf.grid.ColumnModel([this.sm,this.rowNo,{
        header: WtfGlobal.getLocaleText("acc.customreport.header.ProjectCode"),//"Column",
        pdfwidth:180,
        dataIndex:'header'
//    },{
//        header: WtfGlobal.getLocaleText("acc.advancesearch.search1txt"),
//        dataIndex:'searchText',
//        hidden:true

    },{
//    {
//        header: WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt"),//"Search Text",
//        dataIndex:'id'
//    },{
//        header: WtfGlobal.getLocaleText("acc.DELETEBUTTON"),//"Delete",
//        dataIndex:'delField',
//        renderer : function(val, cell, row, rowIndex, colIndex, ds) {
//            return "<div class='pwnd delete-gridrow' > </div>";
//        }
//    },{
            header:WtfGlobal.getLocaleText("acc.customreport.header.netprofit")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Total Amount (In Home Currency)",
            align:'right',
            dataIndex:'amount',
            pdfwidth:140,
            //hidecurrency : true,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol

    }     
    ]);

    this.searchRecord = Wtf.data.Record.create([{
        name: 'column'
    },{
        name: 'searchText'
    },{
        name: 'header'
    },{
        name: 'amount'
    },{
        name: 'dbname'
    },{
        name: 'name'
    },
    {
        name: 'id'
    },{
        name: 'xtype'
    },{
        name: 'refdbname'
    },{
        name: 'iscustomcolumn'
    },{
        name:'xfield'
    },{
        name:'fieldtype'
    },{
        name:'iscustomcolumndata'
    },{
        name:'isfrmpmproduct'
    },{ 
        name:'isdefaultfield'
    }]);
         
    this.GridJsonReader = new Wtf.data.JsonReader({
        root: "data",
        totalProperty: 'count'
    }, this.searchRecord);

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
    this.StoreUrl =  "ACCOtherReports/getCustomColumnSummaryReport.do";
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        baseParams:{
                 mode: 2,
                 isSummaryReport:true,
                 flag: 1
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.searchRecord)
    });
    
    this.Store.on('beforeload', function(){
        var startDate="",endDate="";
         

        startDate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
        endDate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        this.Store.baseParams = {
            deleted:false,
            nondeleted:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            startdate : startDate,
            enddate : endDate,
            searchJson:this.filterJson,
            mode: 2,
            flag: 1,
            fieldid:this.columnCombo.getValue(),
            filterConjuctionCriteria: "AND",
            moduleid: 102
        
        }
          if(this.pP!=undefined &&this.pP.combo!=undefined){
            if(this.pP.combo.value=="All"){
                var count
                if(this.store!=undefined){
                     count = this.store.getTotalCount()
                }
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
            }
        }
    }, this);
  
  this.Store.on('load',function(){
        this.showDetails.enable();
        this.exportButton.enable();
         //params:{start:0,limit:30}
    },this);  
      
//    this.pagingToolbar = new Wtf.PagingSearchToolbar({
//        pageSize: 15,
//        id: "pagingtoolbar" + this.id,
//        store: this.Store,
////        searchField: this.quickPanelSearch,
//        displayInfo: true,
//        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
//        plugins: this.pP = new Wtf.common.pPageSize({
//            id : "pPageSize_"+this.id
//            })
//    });
        
     var gridSummary = new Wtf.ux.grid.GridSummary;
    this.grid = new Wtf.grid.GridPanel({ 
        store: this.Store,
        cm:this.cm,
        stripeRows: true,
        autoScroll : true,
        border:false,
        loadMask : true,
//        plugins: [gridSummary],
        clicksToEdit:1,
        viewConfig: {
            forceFit:false,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
        
    });
     this.showDetails = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.customreport.detailsReport"),  //"Set price",
            tooltip:WtfGlobal.getLocaleText("acc.customreport.detailsReportTooltip"),  
             handler:function (){
                if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.customreport.header.selectrecord")],2);
                    return;
                }else{
                    this.showDetaliReportView(this.grid.getSelectionModel().getSelected().data["id"],this.grid,this.columnCombo);
                } 


                },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            disabled:true
//            hidden:true
        });  
    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        filename:"Custom Column Summary Report_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,xls:true
        },
        params:{
            name:"Custom Column Summary Report",
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())

        },
        get:Wtf.autoNum.ProjectCountrySummaryReport
    });

      
    this.fetchBtn=new Wtf.Toolbar.Button({
            text : WtfGlobal.getLocaleText("acc.ra.fetch"),
            iconCls:'accountingbase fetch',
            scope : this,
            disabled:true,
            handler :function (){
            if (this.startDate.getValue() > this.endDate.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
                return;
            }
            var record = WtfGlobal.searchRecord(this.columnCombo.store, this.columnCombo.getValue(), 'fieldid');
            this.displayFieldData(this.columnCombo, record)

        }
    });            
    var buttonArray = new Array();
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), this.endDate);   
    buttonArray.push(this.resetBttn);
   buttonArray.push(this.text1=new Wtf.Toolbar.TextItem(WtfGlobal.getLocaleText("acc.advancesearch.searchfield")+": "),this.columnCombo);

               
    buttonArray.push('-',this.fetchBtn,'-',this.showDetails,'-',this.exportButton);  //, '-', this.printButton
    this.getComboData();
    
this.leadpan = new Wtf.Panel({
    layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items:[
            {
            region:'center',
            layout:'fit',
            border:false,
            items:[this.grid],
            tbar :buttonArray,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
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
    
    Wtf.account.CustomColumnSummaryReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.CustomColumnSummaryReport,Wtf.Panel,{
  
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
    displayFieldData:function(combo,record){
        if(record == '')
            var recXtype = "textfield";
        else
            recXtype=record.get('xtype');
        if (recXtype == "None"){
            record.set('xtype','textfield');
        }

        this.doLayout();
        var iscustomcolumn,fieldtype,refdbname,dbname,iscustomcolumndata,isfrmpmproduct,isdefaultfield=false;
        if(record!=''){
             iscustomcolumn=record.get('iscustomcolumn');
             iscustomcolumndata=record.get('iscustomcolumndata');
             isfrmpmproduct=record.get('moduleid')=='30';
             fieldtype=record.get('fieldtype');
             refdbname = record.get('refdbname');
             dbname=record.get('dbname');
             isdefaultfield=record.get('isdefaultfield');
        }
        
          
        
        
        iscustomcolumn = iscustomcolumn?iscustomcolumn:"";
        if (recXtype == "textfield" || recXtype == 'Text' || recXtype =='textarea'){
            this.searchText = new Wtf.form.TextField({
                anchor: '95%',
                maxLength: 100,
                width:125,
                fromproduct:isfrmpmproduct,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname,
                isdefaultfield:isdefaultfield
            });
        }

        if (recXtype == "numberfield" || recXtype == 'Number(Integer)' || recXtype == 'Number(Float)'){
            this.searchText = new Wtf.form.NumberField({
                anchor: '95%',
                maxLength: 100,
                width:125,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname
            });
            this.searchTextTo = new Wtf.form.NumberField({
                anchor: '95%',
                maxLength: 100,
                width:125,
                iscustomcolumn:iscustomcolumn,
                iscustomcolumndata:iscustomcolumndata,
                fromproduct:isfrmpmproduct,
                fieldtype:fieldtype,
                dbname:dbname,
                refdbname:refdbname
            });
        }



        if ((recXtype == "combo" || recXtype == "Combobox" || recXtype == "select" || recXtype == "fieldset") &&  record!=''){
  
          this.filterJson='{"root":[';
            var xtype=record.data.xtype;
            if(xtype==undefined)
                xtype = 'combo';
         var ComboRecord=WtfGlobal.searchRecord(combo.store, combo.getValue(), 'fieldid');
         
         var header="",interval=false,isinterval=false;
         if(ComboRecord!=null){
             header=ComboRecord.get("header");
             isdefaultfield=ComboRecord.get("isdefaultfield");
         }
         this.filterJson+='{ "column":"'+combo.getValue()+'","refdbname":"'+record.data.refdbname+'","xfield":"'+record.data.refdbname+'","iscustomcolumn":"'+record.data.iscustomcolumn+'","iscustomcolumndata":"'
             +record.data.iscustomcolumndata+'","isinterval":"'+isinterval+'","interval":"'+interval+'","isbefore":"'+record.data.isbefore+'","isfrmpmproduct":"false","fieldtype":"'+record.data.fieldtype+'","searchText":"","columnheader":"'
             +encodeURIComponent(header)+'","search":"","xtype":"'+xtype+'","isdefaultfield":"'+isdefaultfield+'","combosearch":""},';
          
        this.filterJson=this.filterJson.substring(0,this.filterJson.length-1);
        this.filterJson+="]}";
        
        var filterConjuctionCriteria = "AND";
            this.Store.load({
                params: {
                    mode: 2,
                    flag: 1,
                    fieldid: record.data.fieldid,
                    filterConjuctionCriteria: filterConjuctionCriteria,
                    moduleid:102,
                    searchJson:this.filterJson,
                    start:0,
                    limit:this.pP.combo.value
                }
            });
  
            this.displayField=combo.getValue();



        }

    },

    getComboData: function() {
        if (!this.myData) {
            var mainArray = [];
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getFieldParams.do",
                params: {
                    moduleid: 102,
                    jeId: (this.record) ? this.record.data.journalentryid : "",
                    isAdvanceSearch:true,
                    iscustomdimension:this.dimensionBasedComparisionReport,
                    isActivated:1,
                    isCustomDetailReport:true,
                    isAvoidRedundent:true
                }
            }, this,
                    function(responseObj) {
                        this.sdate = "0";
                        this.edate = "0";

                        if (responseObj.data != '' && responseObj.data != null) {
                            for (var i = 0; i < responseObj.data.length; i++) {
                                var tmpArray = [];
                                responseObj.data[i].fieldData
                                var header = headerCheck(WtfGlobal.HTMLStripper(responseObj.data[i].fieldlabel));
                                header = header.replace("*", "");
                                header = header.trim();
                                tmpArray.push(header);
                                responseObj.data[i].column_number
                                tmpArray.push(responseObj.data[i].column_number);
                                tmpArray.push(WtfGlobal.getXType(responseObj.data[i].fieldtype));
                                tmpArray.push(responseObj.data[i].column_number);
                                tmpArray.push(true);
                                tmpArray.push(responseObj.data[i].column_number);
                                tmpArray.push(responseObj.data[i].xfield);
                                tmpArray.push(responseObj.data[i].fieldtype);
                                tmpArray.push(responseObj.data[i].column_number);
                                tmpArray.push(responseObj.data[i].fieldid);
                                tmpArray.push(responseObj.data[i].iscustomcolumn);
                                tmpArray.push(responseObj.data[i].moduleid);
                                tmpArray.push(responseObj.data[i].isdefaultfield);
                                var recXtype=WtfGlobal.getXType(responseObj.data[i].fieldtype);
                                if (recXtype == "combo" || recXtype == "Combobox" || recXtype == "select" ){//|| recXtype == "fieldset"
                                    mainArray.push(tmpArray)
                                }
                                
                            }
                            this.myData = mainArray;
//                            if (this.advSearch)
                                this.combostore.loadData(this.myData);

                        }

                    },
                    function() {
                    }
            );
        }
    },
     showDetaliReportView: function(recordid,grid,combo) {
         
         var gridrecord=WtfGlobal.searchRecord(grid.store, recordid, 'id');
         var isdefaultfield="";//false
         var record=WtfGlobal.searchRecord(combo.store, combo.getValue(), 'fieldid');
         var filterJson='{"data":[';
         var header="",interval=false,isinterval=false;
         this.combovalArr=[];
         this.combovalArr.push(gridrecord.data.header);
         var recXtype = "select";//record.data.xtype
         if(record!=null){
             header=record.get("header");
             //isdefaultfield=record.get("isdefaultfield");
         }
         filterJson+='{ "column":"'+encodeURIComponent(header)+'","dbname":"'+combo.getValue()+'","refdbname":"'+record.data.refdbname+'","xfield":"'+record.data.refdbname+'","iscustomcolumn":"'+record.data.iscustomcolumn+'","iscustomcolumndata":"'
             +record.data.iscustomcolumndata+'","isinterval":"'+isinterval+'","interval":"'+interval+'","isbefore":"false","isfrmpmproduct":"false","fieldtype":"'+record.data.fieldtype+'","searchText":"'+gridrecord.data.id+'"\n\
        ,"xtype":"'+recXtype+'","isdefaultfield":"'+isdefaultfield+'","id":"\''+this.combovalArr+'\'","combosearch":"'+gridrecord.data.header+'"},';
        //,"columnheader":"'+encodeURIComponent(header)+'","search":"'+gridrecord.data.id+'"  
        filterJson=filterJson.substring(0,filterJson.length-1);
        filterJson+="]}";
         var panel = Wtf.getCmp("CustomColumnDetailReport");
         if(panel!=null){
            Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null;
         }
        var filterAppend="AND";
        if(panel==null){
            panel = new Wtf.account.CustomColumnDetailReport({
                title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.CustomColumnDetailReport.Report"),Wtf.TAB_TITLE_LENGTH) ,//"Finance Details"
                tabTip:WtfGlobal.getLocaleText("acc.CustomColumnDetailReport.Reporttooltip"),
                id:'CustomColumnDetailReport',
                border:false,
                isFromSummaryReport:true,
                searchJson:filterJson,
                filterConjuctionCrit:filterAppend,
                checkRecXtype:true,
                moduleId:102,
                layout: 'fit',
                iconCls: 'accountingbase agedrecievable',
                closable: true

            });
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,filterJson, filterAppend);
    },
    handleResetClickNew:function()
    {
       var  startDate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
       var  endDate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
       this.startDate.reset();
       this.endDate.reset();
       this.columnCombo.reset();
       this.fetchBtn.disable();
        this.Store.load( {
            deleted:false,
            nondeleted:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            startdate : startDate,
            enddate : endDate,
            start: 0, 
            limit:30
        })
        
    }
  
});


/*******************************************************************************
 *             Custom Column Details Report
 *******************************************************************************/

Wtf.account.CustomColumnDetailReport=function(config){
    Wtf.apply(this, config);
    this.isFromSummaryReport=config.isFromSummaryReport;
    this.checkRecXtype=config.checkRecXtype;
    this.moduleid=102;       
   this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'entryno'},
        {name:'companyid'},
        {name:'currencysymbol'},
        {name:'currencyid'},
        {name:'billno'},
        {name:'personname'},
        {name:'group'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'amount'},
        {name:'amountinbase'},
        {name:'amountdueinbase'},
        {name:'amountdue'},
        {name:'externalcurrencyrate'},
        {name:'withoutinventory',type:'boolean'},
        {name:'description'},
        {name:'customer_vendor'}
    ]);
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
     this.StoreUrl =  "ACCOtherReports/getCustomColumnDetailReport.do" ;
    this.customColumnDetailLoadMask = new Wtf.LoadMask(document.body,{
        msg : WtfGlobal.getLocaleText("acc.msgbox.527")
    });
    this.Store = new Wtf.data.GroupingStore({
        url:this.StoreUrl,
        baseParams:{
            deleted:false,
            nondeleted:false,
            CashAndInvoice:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        },
        sortInfo : {
            field : 'group',
            direction : 'ASC'
        },
        groupField : 'group',
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
   
    this.Store.on('loadexception',this.hideLoading, this);
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.Store,
//        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
    });
        
    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
//        this.quickPanelSearch.setPage(p);
    }, this);
      
    this.Store.on('load',function(){
        this.hideLoading();
        this.exportButton.enable()
         //params:{start:0,limit:30}
    },this);  
      
    this.Store.on('beforeload', function(){
        this.customColumnDetailLoadMask.show();
        WtfGlobal.setAjaxTimeOut();
        var startDate="",endDate="";
        startDate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
        endDate= WtfGlobal.convertToGenericDate(this.endDate.getValue());     
        this.Store.baseParams = {
            deleted:false,
            nondeleted:false,
            CashAndInvoice:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            startdate : startDate,
            enddate : endDate,
            filterConjuctionCriteria:this.filterConjuctionCrit==undefined?"":this.filterConjuctionCrit,
            searchJson:this.searchJson==undefined?"":this.searchJson
            
        }
        if(this.pP!=undefined &&this.pP.combo!=undefined){
            if(this.pP.combo.value=="All"){
                var count
                if(this.store!=undefined){
                     count = this.store.getTotalCount()
                }
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
            }
        }
        
    }, this);
   
    this.summary = new Wtf.grid.GroupSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.gridView1 = new Wtf.grid.GroupingView({
        forceFit:false,
        showGroupName: false,
        enableNoGroups:true, // REQUIRED!
        hideGroupedColumn: true,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    var columnArr =[];   
    columnArr.push(this.sm,this.rowNo,{
        header:WtfGlobal.getLocaleText("acc.reval.transaction"),   //+WtfGlobal.getLocaleText("acc.cn.9"),
        dataIndex:'billno',
        width:150,
        pdfwidth:80
       // renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
    },{
//        header:WtfGlobal.getLocaleText("acc.invoiceList.cust"),       //(config.isCustomer? :WtfGlobal.getLocaleText("acc.invoiceList.ven")),  //this.businessPerson,
//        pdfwidth:75,
//        renderer:WtfGlobal.deletedRenderer,
        dataIndex:'group',
        width:150,
        hidden:true,
        header:"Group"
//        sortable:true
    },{
        header:WtfGlobal.getLocaleText("acc.je.tabTitle"),       //(config.isCustomer? :WtfGlobal.getLocaleText("acc.invoiceList.ven")),  //this.businessPerson,
        pdfwidth:85,
        width:150,
        renderer:WtfGlobal.deletedRenderer,
        dataIndex:'entryno',
        summaryRenderer:function(){
            return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
        },
        sortable:true
    });         
//    columnArr = WtfGlobal.appendCustomColumn(columnArr,this.customColArr,true);
    columnArr.push({ 
            header:WtfGlobal.getLocaleText("acc.field.TransactionDate"),  //"Invoice  Date
            dataIndex:'date',
            width:150,
            align:'center',
            pdfwidth:100,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
//        },{
//            header:WtfGlobal.getLocaleText("acc.invoiceList.due"),  //"Due Date",
//            dataIndex:'duedate',
//            align:'center',
//            pdfwidth:80,
//            renderer:WtfGlobal.onlyDateDeletedRenderer,
//              summaryRenderer:function(){
//            return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
//            }
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridAmt")+ " ("+WtfGlobal.getCurrencyName()+")",  //"Total Amount (In Home Currency)",
            align:'right',
            dataIndex:'amountinbase',
            pdfwidth:100,
            width:150,
            summaryType: 'sum',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            //hidecurrency : true,
            summaryRenderer: function(value, m, rec) {
                if (value != 0) {
                    var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
                    return retVal;
                } else {
                    return '';
                }
            }

    },
    { 
        header:WtfGlobal.getLocaleText("acc.je.desc"),  //Description
        dataIndex:'description',
        width:150,
        align:'center',
        pdfwidth:100,
        sortable:true
    },
    { 
        header:WtfGlobal.getLocaleText("acc.customreport.header.customer_vendor_name"),  //Customer / Vendor Name
        dataIndex:'customer_vendor',
        width:150,
        align:'center',
        pdfwidth:100,
        sortable:true
    }
    );  
        
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
                else
                    this.showAdvanceSearch.defer(10, this);
            },
            scope: this
        }
    });
     this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        isCustomDetailReport:true,
        checkRecXtype:this.checkRecXtype,
        hideRememberSerch:true,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
     this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);

    this.exportButton=new Wtf.exportButton({
        obj:this,
        filename:WtfGlobal.getLocaleText("acc.CustomColumnDetailReport.Report")+"_v1",
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,xls:true
        },
        params:{
            name:"Custom Column Detail Report",
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())

        },
        get:Wtf.autoNum.ProjectCountryDetailReport
    });
        this.printButton = new Wtf.exportButton({
        obj: this,
        text: WtfGlobal.getLocaleText("acc.common.print"),
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"),
        filename:WtfGlobal.getLocaleText("acc.CustomColumnDetailReport.Report"),
        menuItem: {print: true},
        get:Wtf.autoNum.ProjectCountryDetailReport,
        params: {
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }
    });
//    this.printButton=new Wtf.exportButton({
//        obj:this,
//        text:WtfGlobal.getLocaleText("acc.common.print"),
//        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
//        label:WtfGlobal.getLocaleText("acc.invoiceList.unpaidInvoices"),
//        menuItem:{
//            print:true
//        },
//        get:Wtf.autoNum.ProjectCountryDetailReport,
//        params:{
//            name:"Project Country Summary Report",
//            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
//                    
//
//        }
//    });
      this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });

    this.fetchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.ra.fetch"),
        iconCls: 'accountingbase fetch',
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"), 
        handler: this.fetchData
    });
    
    var buttonArray = new Array();
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), this.endDate);   //this.quickPanelSearch,
    buttonArray.push('-',this.fetchBtn,this.resetBttn);
    buttonArray.push('-',this.AdvanceSearchBtn,'-',this.exportButton,'-',this.printButton);     //'-',this.resetBttn,, '-', this.printButton
//    if(this.isFromSummaryReport!=undefined && this.isFromSummaryReport){
//        this.Store.load({
//            params: {
//                flag: 1,
//                searchJson: this.filterJson,
//                moduleid: this.moduleId,
//                filterConjuctionCriteria: "AND",
////                ss: this.quickPanelSearch.getValue(), 
//                start: 0, 
//                limit: 30
//                }
//            });
//    } 
    
            
this.leadpan = new Wtf.Panel({
    layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items:[this.objsearchComponent,
            {
            region:'center',
            layout:'fit',
            border:false,
            items:[this.grid],
            tbar : buttonArray,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
//            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
             id : "pPageSize_"+this.id
            })
        })
        }]
       
    });
    this.loaddata();
     Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });   
    Wtf.account.CustomColumnDetailReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.CustomColumnDetailReport,Wtf.Panel,{
  
    hideLoading:function(){
        WtfGlobal.resetAjaxTimeOut();
        if(this.customColumnDetailLoadMask)
                this.customColumnDetailLoadMask.hide();
        Wtf.MessageBox.hide();
    },
    loaddata : function(){
     
//        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
//            this.Store.baseParams.searchJson = "";
//        }
        this.Store.load({
            params : {
                start:0,
                limit:(this.pP.combo!=undefined) ? this.pP.combo.getValue():30             
            }
        });
        this.exportButton.enable();
    },
    
     showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
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

        this.Store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: this.moduleId,
                filterConjuctionCriteria: filterConjuctionCriteria,
//                ss: this.quickPanelSearch.getValue(), 
                deleted: false,
                nondeleted: false,
                CashAndInvoice: true,
                start: 0, 
                limit: this.pP.combo.value
                }
            });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.load({
            params: {
                flag: 1,
                searchJson: this.searchJson,
                moduleid: this.moduleId,
                filterConjuctionCriteria: this.filterConjuctionCrit,
//                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    handleResetClickNew:function()
    {
       var  startDate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
       var  endDate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
       this.startDate.reset();
       this.endDate.reset();
        this.Store.load( {
            deleted:false,
            nondeleted:false,
            CashAndInvoice:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            startdate : startDate,
            enddate : endDate,
            start: 0, 
            limit: 30
        })

    },
    fetchData: function() {
        var sDate = this.startDate.getValue();
        var eDate = this.endDate.getValue();
        if (sDate > eDate) {
            WtfComMsgBox(1, 2);
            return;
        }
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo.value,
                stdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())
            }
        });
    }
});

