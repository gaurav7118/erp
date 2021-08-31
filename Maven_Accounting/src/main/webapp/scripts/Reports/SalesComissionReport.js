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
function SalesCommissionReportDynamicLoad(){

    var panel = Wtf.getCmp("SalesCommissionReport");
    if(panel==null){
        panel = new Wtf.account.SalesCommissionReportPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.masterConfig.salesCommision"),Wtf.TAB_TITLE_LENGTH) ,//"Finance Details"
            tabTip:WtfGlobal.getLocaleText("acc.masterConfig.salesCommisiontooltip1"),
            id:'SalesCommissionReport',
            border:false,
            //customColArr :customCol,
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


function BrandCommissionReportDynamicLoad(){

    var panel = Wtf.getCmp("BrandCommissionReport");
    if(panel==null){
        panel = new Wtf.account.BrandCommissionReportPanel({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.masterConfig.brandCommision"),Wtf.TAB_TITLE_LENGTH) ,//"Finance Details"
            tabTip:WtfGlobal.getLocaleText("acc.masterConfig.brandCommisiontooltip"),
            id:'BrandCommissionReport',
            border:false,
            //customColArr :customCol,
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



Wtf.account.SalesCommissionReportPanel=function(config){
    Wtf.apply(this, config);
    this.moduleid=config.moduleId;
    this.itemRec = Wtf.data.Record.create([
            {name:"id"},
            {name:"name"},
            {name:"modulename"},
            {name:"fieldtype"},
            {name:"parentid"},
            {name:"leaf"},
            {name:"fieldtype"},
            {name:'level',type:'int'}
        ]);
        var baseparam = {
            mode:112,
            groupid:15
            
        };
        this.itemStore = new Wtf.data.Store({
              url:"ACCMaster/getMasterItems.do",
            baseParams:baseparam,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.itemRec)
        }); 
    
        this.MSComboconfig = {
            store: this.itemStore,
            valueField:'id',
            hideLabel:true,
            displayField:'name',
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };
    
        this.itemListCombo = new Wtf.common.Select(Wtf.applyIf({
            name:'itemlist',
            multiSelect:true,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectSalesPerson"), //             WtfGlobal.getLocaleText("acc.field.SelectAccount"),
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.15"),  //'Account Name',
            forceSelection:true,         
    //        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
    //        extraComparisionField:'acccode',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            width:200
        },this.MSComboconfig));      

        this.itemListCombo.on('select',function(combo,accRec,index){ //multiselection in case of all 
            if(accRec.get('id')=='All'){  //case of multiple record after all
                combo.clearValue();
                combo.setValue('All');
            }else 
                if(combo.getValue().indexOf('All')>=0){  // case of all after record
                combo.clearValue();
                combo.setValue(accRec.get('id'));
            }
        } , this);       
    
         this.itemStore.on("load", function() {
            var record1 = new Wtf.data.Record({
                id: "All",
                name: "All"
            });
          this.itemStore.insert(0, record1);
          this.itemListCombo.setValue("All");
    }, this);
        this.itemStore.load();
    
   this.GridRec = Wtf.data.Record.create ([
//        {name:'billid'},
//        {name:'entryno'},
//        {name:'companyid'},
        {name:'currencysymbol'},
        {name:'currencyid'},
//        {name:'billno'},
        {name:'salesPersonID'},
        {name:'name'},
//        {name:'date', type:'date'},
//        {name:'duedate', type:'date'},
        {name:'amount'},
        {name:'value'},
        {name:'actualInvoiceamount'},
        {name:'commissionamount'},
//        {name:'amountdueinbase'},
//        {name:'amountdue'},
//        {name:'externalcurrencyrate'},
//        {name:'withoutinventory',type:'boolean'},
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
     this.StoreUrl =  "ACCInvoiceCMN/SalesCommissionReport.do" ;//this.businessPerson=="Customer" ?: "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    
    this.Store =  new Wtf.data.Store({
        url:this.StoreUrl,
        baseParams:{
            deleted:false,
            nondeleted:true,  
            companyids:companyids,
            commissiontype:1,
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        },
//        sortInfo : {
//            field : 'personname',
//            direction : 'ASC'
//        },
      
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
        
    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
      
    this.Store.on('load',function(){
        this.exportButton.enable()
         //params:{start:0,limit:30}
    },this);  
      
    this.Store.on('beforeload', function(){
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.deleted=false;
        currentBaseParams.nondeleted=true;
        currentBaseParams.companyids=companyids;
        currentBaseParams.commissiontype=1;
        currentBaseParams.itemid=this.itemListCombo.getValue();
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        this.Store.baseParams=currentBaseParams;
    }, this);
   
//    this.summary = new Wtf.grid.GroupSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
   
    var columnArr =[];   
    columnArr.push(this.rowNo,{
        header: WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"),            //WtfGlobal.getLocaleText("acc.invoiceList.cust"),       //(config.isCustomer? :WtfGlobal.getLocaleText("acc.invoiceList.ven")),  //this.businessPerson,
        pdfwidth:300,
        renderer:WtfGlobal.deletedRenderer,
        dataIndex:'name',
         width: 400,
        sortable:true
        },{
        header:WtfGlobal.getLocaleText("acc.field.TotalInvoiceAmount")+"("+WtfGlobal.getCurrencyName()+")",                     //WtfGlobal.getLocaleText("acc.masterconfig.amount")+ " ("+WtfGlobal.getCurrencyName()+")",        //"Amount in Base Currency",
        dataIndex:'actualInvoiceamount',
        align:'right',
        pdfwidth:220,
         width: 220,
        pdfrenderer : "rowcurrency",
        summaryType:'sum',
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer
            
        },{
        header:WtfGlobal.getLocaleText("acc.common.totalCommissionAmount")+"("+WtfGlobal.getCurrencyName()+")",        //"Amount in Base Currency",
        dataIndex:'commissionamount',
        align:'right',
        pdfwidth:180,
        width: 220,
        pdfrenderer : "rowcurrency",
        summaryType:'sum',
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer
            
        });  
        
//    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.grid.GridPanel({
        //id:"gridmsg"+this.id,
        stripeRows :true,
        store:this.Store,
        //tbar : this.tbar2,
        sm:this.sm,
        border:false,
        viewConfig:{
         emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        forceFit:true,
        layout:'fit',
//        plugins: [gridSummary],
        loadMask : true,  
        cm:new Wtf.grid.ColumnModel(columnArr)
       
    });
    
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: Wtf.Acc_Invoice_ModuleId,
        advSearch: false,
        ignoreDefaultFields:true
    });
    
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
            
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
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" "+WtfGlobal.getLocaleText("acc.invoiceList.salesPerson"),
        width: 150,
        //      id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    })
    
    this.viewDetailBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.lp.viewccd"), // 'View Details',
        tooltip: WtfGlobal.getLocaleText("acc.lp.viewccd"), // 'View Details',
        id: 'btnViewDetailBtn' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.reorderreport)
    });
    
    this.viewDetailBtn.on('click', function() {
        getSalesCommissionDetailTabView();
    }, this);
    
    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        filename:WtfGlobal.getLocaleText("acc.salesComissionReport.exportName")+"_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            name:"Sales Commission Report",
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())

        },
        get:920
    });
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
        label:WtfGlobal.getLocaleText("acc.invoiceList.unpaidInvoices"),
        menuItem:{
            print:true
        },
        get:920,
        params:{
            name:"Sales Commission Report",
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }
    });
     this.Store.load();
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
             tbar : [this.quickPanelSearch,'-',WtfGlobal.getLocaleText("acc.common.select.salesPerson"),this.itemListCombo,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), this.endDate,'-',{
            text : WtfGlobal.getLocaleText("acc.ra.fetch"),
            iconCls:'accountingbase fetch',
            scope : this,
            handler : this.loaddata
            },this.AdvanceSearchBtn,'-',this.resetBttn, this.viewDetailBtn, this.exportButton, '-', this.printButton],//
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
   
    Wtf.account.SalesCommissionReportPanel.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.SalesCommissionReportPanel,Wtf.Panel,{
  
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
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_Invoice_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_Invoice_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
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
       
        }
          
});

/***************************************************************************/
//                       Brand Sales Commission
/***************************************************************************/
Wtf.account.BrandCommissionReportPanel=function(config){
    Wtf.apply(this, config);
    this.moduleid=config.moduleId;       
    this.itemRec1 = Wtf.data.Record.create([
            {name:"id"},
            {name:"name"},
            {name:"modulename"},
            {name:"fieldtype"},
//            {name:"parentid"},
//            {name:"leaf"},
//            {name:"fieldtype"},
//            {name:'level',type:'int'}
        ]);
        var baseparam = {
            mode:112,
            groupid:19
            
        };
        this.itemCategoryStore = new Wtf.data.Store({
              url:"ACCMaster/getMasterItems.do",
            baseParams:baseparam,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.itemRec1)
        }); 
    
        this.MSComboconfig = {
            store: this.itemCategoryStore,
            valueField:'id',
            hideLabel:true,
            displayField:'name',
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };
    
        this.itemCategoryListCombo = new Wtf.common.Select(Wtf.applyIf({
            name:'itemlist',
            multiSelect:true,
            emptyText:WtfGlobal.getLocaleText("acc.salescomission.SelectBrandtooltip"), //             WtfGlobal.getLocaleText("acc.field.SelectAccount"),
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.15"),  //'Account Name',
            forceSelection:true,         
    //        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
    //        extraComparisionField:'acccode',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            width:200
        },this.MSComboconfig));      

        this.itemCategoryListCombo.on('select',function(combo,accRec,index){ //multiselection in case of all 
            if(accRec.get('id')=='All'){  //case of multiple record after all
                combo.clearValue();
                combo.setValue('All');
            }else 
                if(combo.getValue().indexOf('All')>=0){  // case of all after record
                combo.clearValue();
                combo.setValue(accRec.get('id'));
            }
        } , this);       
    
         this.itemCategoryStore.on("load", function() {
            var record1 = new Wtf.data.Record({
                id: "All",
                name: "All"
            });
          this.itemCategoryStore.insert(0, record1);
          this.itemCategoryListCombo.setValue("All");
    }, this);
        this.itemCategoryStore.load();
    
   this.GridRec = Wtf.data.Record.create ([
//        {name:'billid'},
//        {name:'entryno'},
//        {name:'companyid'},
        {name:'currencysymbol'},
        {name:'currencyid'},
        {name:'categoryname'},
        {name:'categoryid'},
        {name:'salescompleted'},
        {name:'salesreturn'},
        {name:'netsales'},
        {name:'percentage'},
        {name:'netcommision'},
//        {name:'billno'},
        {name:'salesperson'},
        {name:'salespersonid'},
//        {name:'date', type:'date'},
//        {name:'duedate', type:'date'},
        {name:'amount'},
        {name:'value'},
//        {name:'actualInvoiceamount'},     
//        {name:'externalcurrencyrate'},
//        {name:'withoutinventory',type:'boolean'},
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
     this.StoreUrl =  "ACCInvoiceCMN/BrandCommissionReport.do" ;//this.businessPerson=="Customer" ?: "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    
    this.Store =  new Wtf.data.GroupingStore({
        url:this.StoreUrl,
        baseParams:{
            deleted:false,
            nondeleted:true,  
            companyids:companyids,
            commissiontype:2,
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        },
        sortInfo : {
            field : 'salesperson',
            direction : 'ASC'
        },
        groupField : 'salesperson',
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
             companyids:companyids,
             commissiontype:2,
//             itemid:this.itemListCombo.getValue(),
             categoryid:this.itemCategoryListCombo.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }
        
    }, this);
   
//    this.summary = new Wtf.grid.GroupSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
   
    var columnArr =[];   
    columnArr.push(this.rowNo,{
        header:"Sales Person",  //"Account Name",
        dataIndex: 'salesperson',
        groupable: true,
        hidden:true
       
       },{
        header:WtfGlobal.getLocaleText("acc.commission.ProductCategory"),                    //'Brand',          
        pdfwidth:300,
        renderer:WtfGlobal.deletedRenderer,
        dataIndex:'categoryname',
         width: 400,
        sortable:true
        },{
        header:WtfGlobal.getLocaleText("acc.commission.salesCompleted"),       //'Sales Completed',            
        dataIndex:'salescompleted',
        align:'right',
        pdfwidth:220,
         width: 220,
//        pdfrenderer : "rowcurrency",
        summaryType:'sum',
//        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            hidden:true,
            pdfwidth:85
        },{
        header:WtfGlobal.getLocaleText("acc.commission.salesReturn"),        //'Sales Return',              
        dataIndex:'salesreturn',
        align:'right',
        pdfwidth:220,
         width: 220,
//        pdfrenderer : "rowcurrency",
        summaryType:'sum',
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer
            
        },{
        header:WtfGlobal.getLocaleText("acc.commission.netSales"),       //'Net Sales',              // +"("+WtfGlobal.getCurrencyName()+")",                     //WtfGlobal.getLocaleText("acc.masterconfig.amount")+ " ("+WtfGlobal.getCurrencyName()+")",        //"Amount in Base Currency",
        dataIndex:'netsales',
        align:'right',
        pdfwidth:220,
         width: 220,
//        pdfrenderer : "rowcurrency",
        summaryType:'sum',
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer
            
        },{
        header:WtfGlobal.getLocaleText("acc.commission.percentageCommission"),        //'Percentage Commission',              // +"("+WtfGlobal.getCurrencyName()+")",                     //WtfGlobal.getLocaleText("acc.masterconfig.amount")+ " ("+WtfGlobal.getCurrencyName()+")",        //"Amount in Base Currency",
        dataIndex:'percentage',
        align:'right',
        pdfwidth:220,
         width: 220,
//        pdfrenderer : "rowcurrency",
        summaryType:'sum'
//        hidecurrency : true,
//        renderer:WtfGlobal.currencyDeletedRenderer
            
        },{
        header:WtfGlobal.getLocaleText("acc.commission.netCommission")+"("+WtfGlobal.getCurrencyName()+")", //'Net Commission'       //"Amount in Base Currency",
        dataIndex:'netcommision',
        align:'right',
        pdfwidth:180,
        width: 220,
        pdfrenderer : "rowcurrency",
        summaryType:'sum',
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer
            
        });  
    this.gridView1 = new Wtf.grid.GroupingView({
        forceFit:true,
        showGroupName: true,
        enableNoGroups:true, // REQUIRED!
        hideGroupedColumn: false,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });    
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.grid.GridPanel({
        //id:"gridmsg"+this.id,
        stripeRows :true,
        store:this.Store,
        //tbar : this.tbar2,
        sm:this.sm,
        border:false,
        viewConfig:this.gridView1,        
        forceFit:true,
        layout:'fit',
        plugins: [gridSummary],
        loadMask : true,  
        cm:new Wtf.grid.ColumnModel(columnArr)
       
    });
  

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
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" Sales Person",
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
        filename:WtfGlobal.getLocaleText("acc.masterConfig.salesCommision")+"_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            name:"Brand Commission Report",
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())

        },
        get:921
    });
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
        label:WtfGlobal.getLocaleText("acc.invoiceList.unpaidInvoices"),
        menuItem:{
            print:true
        },
        get:921,
        params:{
            name:"Brand Commission Report",
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }
    });
     this.Store.load();
this.leadpan = new Wtf.Panel({
    layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items:[
            {
            region:'center',
            layout:'fit',
            border:false,
            items:[this.grid],//'Select Sales Person',this.itemListCombo,'-',
             tbar : [WtfGlobal.getLocaleText("acc.salescomission.SelectBrand"),this.itemCategoryListCombo,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), this.endDate,'-',{ //this.quickPanelSearch,
            text : WtfGlobal.getLocaleText("acc.ra.fetch"),
            iconCls:'accountingbase fetch',
            scope : this,
            handler : this.loaddata
            },'-',this.resetBttn,'-',this.exportButton, '-', this.printButton],//,this.resetBttn
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
   
    Wtf.account.BrandCommissionReportPanel.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.BrandCommissionReportPanel,Wtf.Panel,{
  
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
    handleResetClickNew:function(){ 
        
        this.itemCategoryListCombo.reset();
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));

        this.Store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
       
    }

});

