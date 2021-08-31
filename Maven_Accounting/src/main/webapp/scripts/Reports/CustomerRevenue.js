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
/*Component used for Customer Revenue Report
 */

function getCustRevenueViewReport(){
    var reportPanel = Wtf.getCmp('custrevenue');
    if(reportPanel == null){
        reportPanel = new Wtf.account.CustomerRevenue({
            id : 'custrevenue',
            border : false,
            moduleid:2,
            title: WtfGlobal.getLocaleText("acc.navigate.CustomerRevenue"),
            tabTip: WtfGlobal.getLocaleText("acc.navigate.CustomerRevenueReport"),
            layout: 'fit',
            iscustreport : true,
            closable : true,
            isCustomer:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //"Invoice",
            iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(reportPanel);
    }

    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.CustomerRevenue=function(config){
    this.businessPerson='Customer';
    Wtf.apply(this, config);
    
    
    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'currencysymbol'},
        {name:'customerid'},
        {name:'currencyid'},
        {name:'productid'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'amount'},
        {name:'customername'},
        {name:'incash', type:'boolean'},
        {name:'rowproductname'},
        {name:'rowquantity'},
        {name:'rowrate'},
        {name:'rowprdiscount'},
        {name:'rowprtaxpercent'},
        {name:'amountinbase',type: 'float'},
        {name:'amountinbasewithtax'},
        {name:'vessel'},
         {
            name: 'mergedCategoryData'
        }, {
            name: 'mergedResourceData'
        }, {
            name: 'totalcategorycost'
        }, {
            name: 'categoryName'
        },{
            name: 'totalinvamt'
        },{
        },{
            name: 'invamt'
        },{
        },{
            name: 'invtaxamount'
        },{
        },
        {name:'fixedAssetInvoice'},
        {name:'fixedAssetLeaseInvoice'},
        {name: "personid"},
        {name: "personname"},
        {name: "duedate"},
        {name: "termname"}
        
    ]);
    this.StoreUrl = "ACCInvoiceCMN/getCustomerRevenue.do";
    
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
    
    this.productRec = Wtf.data.Record.create ([
        {name:'productid'},
        {name:'productname'},
        {name:'desc'},
        {name: 'producttype'}
    ]);
     this.startDate.on('change',function(field,newval,oldval){
        if(field.getValue()!='' && this.endDate.getValue()!=''){
            if(field.getValue().getTime()>this.endDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                field.setValue(oldval);                    
            }
        }
    },this);
        
    this.endDate.on('change',function(field,newval,oldval){
        if(field.getValue()!='' && this.startDate.getValue()!=''){
            if(field.getValue().getTime()<this.startDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                field.setValue(oldval);
            }
        }
    },this);
    this.productStore = new Wtf.data.Store({
        url:"ACCProduct/getProductsForCombo.do",
        baseParams:{mode:22},
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.productRec)
    });
    
    this.ProductComboconfig = {
            hiddenName:"productid",         
            store: this.productStore,
            valueField:'productid',
            hideLabel:true,
            hidden:this.iscustreport,
            displayField:'productname',
            emptyText:'Please select a product',
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };

    this.productname = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
         forceSelection:true,         
         width:240
    },this.ProductComboconfig));
        
     this.usersRec = new Wtf.data.Record.create([
        {name: 'id', mapping:'userid'},
        {name: 'name', mapping:'username'},
        {name: 'fname'},
        {name: 'lname'},
        {name: 'image'},
        {name: 'emailid'},
        {name: 'lastlogin',type: 'date'},
        {name: 'aboutuser'},
        {name: 'address'},
        {name: 'contactno'},
        {name: 'rolename'},
        {name: 'roleid'}
    ]);
    
    if(this.isSalesPersonName){
        this.userds = Wtf.salesPersonStore;
    }else{
        this.userds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            },this.usersRec),
            url : "ProfileHandler/getAllUserDetails.do",
            baseParams:{
                mode:11
            }
        });
    }
    
    this.SalesPersonComboconfig = {
            hiddenName:"userid",         
            store: this.userds,
            valueField:'id',
            hideLabel:true,
            hidden : !this.isSalesPersonName,
            displayField:'name',
            emptyText:'Please select a sales person',
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };

    this.salesPersonName = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         id:'salesbysalesperson'+this.id,
         fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.salesbyperson") + '*' ,
         forceSelection:true,         
         width:240
    },this.SalesPersonComboconfig));
    
    this.personRec = new Wtf.data.Record.create ([
        {
            name:'accid'
        },{
            name:'accname'
        },{
            name:'acccode'
        },{
            name: 'termdays'
        },{
            name: 'billto'
        },{
            name: 'currencysymbol'
        },{
            name: 'currencyname'
        },{
            name: 'currencyid'
        },{
            name:'deleted'
        }
    ]);

    this.customerAccStore =  new Wtf.data.Store({
        url:"ACCCustomer/getCustomersForCombo.do",
        baseParams:{
            mode:2,
            group:10,
            deleted:false,
            nondeleted:true,
            common:'1'
        },
        reader: new  Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad:false
        },this.personRec)
    });
        
    this.CustomerComboconfig = {
        hiddenName:this.businessPerson.toLowerCase(),         
        store: this.customerAccStore,
        valueField:'accid',
        hideLabel:true,
        hidden : this.isSalesPersonName ? this.isSalesPersonName : !this.iscustreport,
        displayField:'accname',
        emptyText:WtfGlobal.getLocaleText("acc.inv.cus"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };

    this.Name = new Wtf.common.Select(Wtf.applyIf({
         multiSelect:true,
         fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.cust") + '*' ,
         forceSelection:true,    
         extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
         extraComparisionField:'acccode',// type ahead search on acccode as well.
         listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
         width:240
    },this.CustomerComboconfig));
       
    this.Name.on('select',function(combo,custRec,index){ //multiselection in case of all 
        if(custRec.get('accid')=='All'){  // case when "all" selected after some records
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){   //case when other customers selected after "All"
            combo.clearValue();
            combo.setValue(custRec.get('accid'));
        }
    } , this);
        
    var storeNewRecord=new this.personRec({
        accname:'All',
        accid:'All',
        acccode:'All'
    });    
    this.customerAccStore.on('load', function(store){
        store.insert( 0,storeNewRecord);    
        this.Name.setValue(store.getAt(0).data.accid);
        this.loaddata();
    }, this);
        
        this.productStore.on('load', function(store){
            if(store.getCount()>0){
                    this.productname.setValue(store.getAt(0).data.productid);
                this.productid = null;
                this.loaddata();
            }
            
        }, this);
        
        this.userds.on('load', function(store){
            if(store.getCount()>0){
                this.salesPersonName.setValue(store.getAt(0).data.id);
                this.userid = null;
                this.loaddata();
            }            
        }, this);        
        
        if(this.isSalesPersonName){
            this.userds.load();
        } else if(this.iscustreport) {
            this.customerAccStore.load();
        } else {
            this.productStore.load();
        }
    
    this.Store = new Wtf.data.GroupingStore({
        url:this.StoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec),
         groupField:'customername', 
         sortInfo: {field: 'customername',direction: "ASC"}
    });
    
    
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid+config.id,
        valueField:'typeid',
        mode: 'local',
        defaultValue:0,
        width:160,
        listWidth:160,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    
  
        
    this.Store.on('beforeload', function(){
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 
        this.Store.baseParams = {
            prodfiltercustid:this.Name.getValue()=="All"?"":this.Name.getValue(),
            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        
    }, this);
    
    this.Store.on('load', function(){
        WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;    
    }, this);
    
    
     this.gridColumnModelArr=[];
        this.gridColumnModelArr.push({
            hidden:true,
            dataIndex:'billid'
        },{
            header: WtfGlobal.getLocaleText("acc.cust.name"), //Customer Name
            pdfwidth:80,
            dataIndex:'customername'
           
        },
//        {
//            header:"Type of vessel",
//            pdfwidth:80,
//            dataIndex:'vessel'
//        },
        {
            header:WtfGlobal.getLocaleText("acc.cust.name"),//Customer Name
            pdfwidth:80,
            dataIndex:'customername',
            hidden:true
            
        },{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
            pdfwidth:80,
            dataIndex:'billno',
            renderer : WtfGlobal.linkDeletedRenderer
            
            
        },{
            header:this.label+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
            dataIndex:'date',
            align:'center',
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            summaryRenderer:function(a,b,c){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
        },{
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            hidden:true,
            pdfwidth:85
        },{
            header:WtfGlobal.getLocaleText("acc.field.AmountInDocumentCurrency"),
            dataIndex:'invamt',
            align:'right',
            pdfwidth:100,
            pdfrenderer : "rowcurrency",
            renderer : WtfGlobal.withoutRateCurrencyDeletedSymbol       
        },{
            header:WtfGlobal.getLocaleText("acc.field.AmountInBaseCurrency"),
            dataIndex:'amountinbase',
            align:'right',
            pdfwidth:100,
            summaryType:'sum',
            pdfrenderer : "currency",
            renderer : WtfGlobal.currencyRenderer,
            hidecurrency : true,
            summaryRenderer: function(value, m, rec) {
              var retVal = WtfGlobal.currencySummaryRenderer(value, m, rec)
              return retVal;
            }
        });
 
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid =new Wtf.grid.GridPanel({
        id: 'mGrid' + this.pid,
        store:this.Store,
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr),
        cls: 'colWrap',
        border:false,
        layout:'fit',
        loadMask:true,
        view: new Wtf.grid.GroupingView({
            forceFit:true,
            startCollapsed :true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }),
        plugins:[gridSummary],
        sm: new Wtf.grid.RowSelectionModel({
            singleSelect: true
        })
    });
    var colModelArray = GlobalDimensionModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.Store);
    this.Store.load();
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.moduleid,
        advSearch: false,
        ismultiselect: true,
        reportid: Wtf.autoNum.CustomerRevenueReport
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.grid.on('cellclick',this.onCellClick, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
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
    
    this.exportButton=new Wtf.exportButton({
            obj:this,
            filename:WtfGlobal.getLocaleText("acc.navigate.CustomerRevenue")+"_v1",
            id:"exportReports"+this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            params:{
                prodfiltercustid:this.Name.getValue()=="All"?"":this.Name.getValue(),
                startdate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                enddate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
            },
            disabled :true,
            scope : this,
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:Wtf.autoNum.CustomerRevenueReport
    });
    
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
        label:WtfGlobal.getLocaleText("acc.navigate.CustomerRevenueReport"),
        menuItem:{
            print:true
        },        
        params:{
            name: "Customer Revenue Report ",
            prodfiltercustid:this.Name.getValue()=="All"?"":this.Name.getValue(),
            startdate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
            enddate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
        },
        get:Wtf.autoNum.CustomerRevenueReport
    });
    /*
     * Provided button to expand or collapse all row details. 
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
     
         this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [this.objsearchComponent
        , {
            region: 'center',
            layout: 'fit',
            border: false,
            items: [this.grid],
                tbar: [WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, '-', WtfGlobal.getLocaleText("acc.field.SelectCustomer"), this.productname, this.Name, this.salesPersonName, '-', {
                text : WtfGlobal.getLocaleText("acc.common.fetch"),
                iconCls:'accountingbase fetch',
                scope : this,
                handler : this.loaddata
                            }, '-', this.resetBttn, '-', this.AdvanceSearchBtn, this.exportButton, '-', this.printButton, '-', this.expandCollpseButton],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
        }]
    }); 
    
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[this.leadpan]
   
    });
    

    Wtf.account.CustomerRevenue.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.CustomerRevenue,Wtf.Panel,{
  
  hideLoading:function(){
      Wtf.MessageBox.hide();
  },
  
  loaddata : function(){
      var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        if(sDate > eDate){
            WtfComMsgBox(1,2);
            return;
        }
      if(this.isSalesPersonName) {
            if(this.salesPersonName.getValue() == ''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),'Please select a sales person from drop down.'], 2);
                this.Store.removeAll();
                return;
            }  
      } else if(this.iscustreport) {
            if(this.Name.getValue() == ''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),'Please select a customer from drop down.'], 2);
                this.Store.removeAll();
                return;
            }
        } else if(!this.iscustreport) {
            if(this.productname.getValue() == ''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),'Please select a product from drop down.'], 2);
                this.Store.removeAll();
                return;
            }
        }
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params : {
             start : 0,
             limit : this.pP ? this.pP.combo.getValue():30
            }
        });
        this.exportButton.enable();
        var newFilterParamsForExport = {
            prodfiltercustid:this.Name.getValue()=="All"?"":this.Name.getValue(),
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
        };
        this.exportButton.setParams(newFilterParamsForExport);
        this.printButton.setParams(newFilterParamsForExport);
        
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
        if(json!=""){
            var jsonArray=JSON.parse(json).root;
            for(var i=0;i<jsonArray.length;i++){
                var colName=jsonArray[i].refdbname;
                 for(var j=0;j<this.gridColumnModelArr.length;j++){
                      var configName=this.gridColumnModelArr[j].dbname;
                      if(configName!=undefined && configName==colName){
                           var gridPanel = this.grid.colModel.config[j];
                if (gridPanel != undefined){
                    gridPanel.hidden = false;
                    gridPanel.width = 200;
                }
                      }
           
            }
        }
          this.grid.getView().refresh(true);
        }
       
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: filterConjuctionCriteria,
            reportid: Wtf.autoNum.CustomerRevenueReport
        }
        this.Store.load({
            params: {
//                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: 30
            }
        });
    },

    clearStoreFilter: function() {
          if(this.searchJson!=""){
            var jsonArray=JSON.parse(this.searchJson).root;
            for(var i=0;i<jsonArray.length;i++){
                var colName=jsonArray[0].refdbname;
                 for(var j=0;j<this.gridColumnModelArr.length;j++){
                      var configName=this.gridColumnModelArr[j].dbname;
                      if(configName!=undefined && configName==colName){
                           var gridPanel = this.grid.colModel.config[j];
                if (gridPanel != undefined){
                    gridPanel.hidden = true;
//                    gridPanel.width = 200;
                }
                      }
           
            }
        }
          this.grid.getView().refresh(true);
        }
        this.searchJson = "";
        this.filterConjuctionCrit = "";
//        this.Store.baseParams = {
//            flag: 1,
//            searchJson: this.searchJson,
//            moduleid: this.moduleid,
//            filterConjuctionCriteria: this.filterConjuctionCrit
//        }
        this.Store.load({
            params: {
//                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: 30
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
    
    onCellClick:function(g,i,j,e){
        var formrec = this.grid.getStore().getAt(i);
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="billno"){
            var incash=formrec.get("incash");
            if(incash){
//                callViewCashReceipt(formrec, 'ViewInvoice');
                viewSIDocumentTab(formrec.json); //get and set data from billid in view tab
            }else {
                if(formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                    callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
                } else if(formrec.data.isConsignment){
                    callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
                }else{
//                    callViewInvoice(formrec, 'ViewCashReceipt');
                    viewSIDocumentTab(formrec.json); //get and set data from billid in view tab
                }
            }
        }
//        else if(header=="customername"){
//            openAccountStatement(formrec.data.customerid, "true");
//        }
    },
     handleResetClickNew:function(){ 

//           this.quickPanelSearch.reset();
           this.startDate.setValue(WtfGlobal.getDates(true));
           this.endDate.setValue(WtfGlobal.getDates(false));
            if(this.isSalesPersonName) {
               this.salesPersonName.setValue(this.userds.getAt(0).data.id);
            } else if(this.iscustreport) {
                this.Name.setValue(this.customerAccStore.getAt(0).data.accid);
            } else if(!this.iscustreport) {
                this.productname.setValue(this.productStore.getAt(0).data.productid);
            }

         this.Store.load();
       
    },
    /*
     * ExpandCollapse button handler
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
});
