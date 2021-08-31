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
Wtf.account.TransactionListPanelViewStockReport = function (config){
    Wtf.apply(this,config);
       this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'startdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)

        });
        
        this.productCategoryRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        
        this.productCategoryStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode:112,
                groupid:19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productCategoryRec)
        });
        
        this.CategoryComboConfig = {
            hiddenName: "id",        
            store: this.productCategoryStore,
            valueField: 'id',
            hideLabel: true,
            displayField: 'name',
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this
        };
        this.productCategory = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*' ,
            forceSelection: true,
//            extraFields: ['name'],
            extraComparisionField: 'id', // type ahead search on product id as well.
            listWidth: Wtf.ProductComboListWidth,
            width: 150
        },this.CategoryComboConfig));
        
        this.productCategoryStore.load();
        
        this.productCategoryStore.on("load", function (store) {
        WtfGlobal.resetAjaxTimeOut();
        var record = new this.productCategoryRec({
            id: "All",
            name: "All"

        });
        this.productCategory.store.insert(0, record);
        this.productCategory.setValue("All");
    }, this);

        this.productCategory.on('select', function (combo, productRec) {
        if (productRec.get('id') == 'All') {
            combo.clearValue();
            combo.setValue('All');
        } else if (combo.getValue().indexOf('All') >= 0) {
            combo.clearValue();
            combo.setValue(productRec.get('id'));
        }
    }, this);


    this.productTypeStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data: [['All', 'All'], ['d8a50d12-515c-102d-8de6-001cc0794cfa', 'Inventory Part'], ['e4611696-515c-102d-8de6-001cc0794cfa', 'Inventory Assembly'], ['ff8080812f5c78bb012f5cfe7edb000c9cfa', 'Inventory Non-Sale']]
    });
        
            
    this.productTypeComboConfig = {
        hiddenName: "id",        
        store: this.productTypeStore,
        valueField: 'id',
        hideLabel: false,
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        selectOnFocus: true,
        triggerAction: 'all',
        scope: this
    };
    
        this.productTypeCombo = new Wtf.common.Select(Wtf.applyIf({
        multiSelect: true,
        fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") ,
        forceSelection: true,
        //extraFields: ['name'],
        value:'All',
        extraComparisionField: 'id', // type ahead search on product id as well.
        listWidth: Wtf.ProductComboListWidth,
        width: 150
    }, this.productTypeComboConfig));
   
          this.productTypeCombo.on('select', function (combo, productRec) {
        if (productRec.get('id') == 'All') {
            combo.clearValue;
            combo.setValue('All');
        } else if ( combo.getValue().indexOf('All') >= 0) {
            combo.clearValue();
            combo.setValue(productRec.get('id'));
        }
    }, this);
       this.gridRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'pid'},
            {name:'productname'},
            {name:'productdesc'},
            {name:'productType'},
            {name:'evaluationcost'},
            {name:'openingstockvalue'},
            {name:'openingstockQty'},
            {name:'stockOutQty'},
            {name:'stockInQty'},
            {name:'purchasecost'},
            {name:'avgcost'},
            {name:'lifo'},
            {name:'fifo'},
            {name:'quantity'},
            {name:'valuation'},
            {name:'uom'}
        ]);
        this.gridStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
//            url : "ACCProductCMN/getStockLedger.do",
              url: "ACCProductCMN/getStockValuation.do",
            baseParams:{mode:29,isprovalreport:true,isFromStockReport:true},
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"totalCount"
            },this.gridRec)
        });
        
        
         this.GrandTotalReport=new Wtf.XTemplate(// to display the grand total Ref ERP-8925
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.pagedtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{pagedTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.grandtotalinbase")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{grandTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '</table>'+
        '</div>',            
                                 
        '</div>'
        );
            
    this.GrandTotalReportTPL=new Wtf.Panel({
        id:this.isSummary?'GrandTotalSummaryTPL'+this.id:'GrandTotalReportTPL'+this.id,
        border:false,
        width:310,
        baseCls:'tempbackgroundview',
        html:this.GrandTotalReport.apply({
            pagedTotal:WtfGlobal.currencyRenderer(0),
            grandTotal:WtfGlobal.currencyRenderer(0)                    
        })
    });
    
        
        this.bbar1 = new Array();

        this.bbar1.push("->",this.GrandTotalReportTPL);
        this.gridStore.on('datachanged', function(store) {
            var grandTotalInBaseCurrency= 0;  
           var pageTotalInBaseCurrency= 0;  
        if(store.data.length>0){
            var recordindex=store.data.length-1;
            if (store.getAt(recordindex).json.pagetotal != undefined && store.getAt(recordindex).json.grandTotalInBase != undefined) {
                pageTotalInBaseCurrency = parseFloat(grandTotalInBaseCurrency) + parseFloat(store.getAt(recordindex).json.pagetotal);
                grandTotalInBaseCurrency = parseFloat(grandTotalInBaseCurrency) + parseFloat(store.getAt(recordindex).json.grandTotalInBase);
            }
        }
         this.GrandTotalReport.overwrite(this.GrandTotalReportTPL.body,{
                 pagedTotal:WtfGlobal.conventInDecimal(pageTotalInBaseCurrency,WtfGlobal.getCurrencySymbol()),
                 grandTotal:WtfGlobal.conventInDecimal(grandTotalInBaseCurrency,WtfGlobal.getCurrencySymbol())
            });
//            for(var i=0;i <= recordindex;i++){
//                if(store.getAt(i).data.evaluationcost!=undefined && store.getAt(i).data.evaluationcost!=""){
//                    grandTotalInBaseCurrency=parseFloat(grandTotalInBaseCurrency) +  parseFloat(store.getAt(i).data.evaluationcost);
//                }
//            }
//            this.totalValuationValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(grandTotalInBaseCurrency,WtfGlobal.getCurrencySymbol()) +"</B>"; 
        }, this);
        
        this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
        this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,1));
        if(this.sDate>this.eDate){
            WtfComMsgBox(2,2);
            return;
        }

        this.expButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            filename : WtfGlobal.getLocaleText("acc.dashboard.StockReport")+"_v1",
            hidden : this.isCustomWidgetReport,
            params:{
                startdate:this.sdate,
                enddate:this.edate,
                exportInventoryValuation:true,
                productCategoryid : this.productCategory.getValue(),
                productType : this.productTypeCombo.getValue(),
                companyids : companyids,
                gcurrencyid : gcurrencyid,
                userid : loginid,
                get:555,
                searchJson:this.searchJson==undefined?"":this.searchJson,
                filterConjuctionCriteria: this.filterConjuctionCrit==undefined?"":this.filterConjuctionCrit
            },
            menuItem:{xls:true,csv:true,pdf:true,rowPdf:false},//ERP-22196
            get: Wtf.autoNum.inventoryValuation
        });

         this.exportselRec = new Wtf.exportButton({
            obj: this,
            hidden : this.isCustomWidgetReport,
            id: "selproductlistexport",
            iconCls: 'pwnd exportpdfsingle',
            get: Wtf.autoNum.inventoryValuation,
            text: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"), // + " "+ singlePDFtext,
            tooltip: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"), //'Export selected record(s)'
            filename : WtfGlobal.getLocaleText("acc.dashboard.StockReport")+"_v1",
            disabled: true,
            params: {
                startdate:this.sdate,
                enddate:this.edate,
                exportInventoryValuation:true,
                productCategoryid : this.productCategory.getValue(),
                productType : this.productTypeCombo.getValue(),
                companyids : companyids,
                gcurrencyid : gcurrencyid,
                userid : loginid,
                get:555,
                selproductIds: [],
                totalProducts:0,
                searchJson:this.searchJson==undefined?"":this.searchJson,
                filterConjuctionCriteria: this.filterConjuctionCrit==undefined?"":this.filterConjuctionCrit
            },
            menuItem: {
                xls: true,
                pdf:true
            }
        });
        
        this.exportselRec.on('click', function () {
            var selectionArr =  this.sm.getSelections();
            var idsArray = [];
            for (var i = 0; i < this.sm.getCount(); i++) {
                idsArray.push(selectionArr[i].data['productid']);
            }
            this.exportselRec.setParams({
                selproductIds : idsArray,
                totalProducts:idsArray.length
            });
        }, this);
        
        this.sm = new Wtf.grid.CheckboxSelectionModel();
        this.gridcm = new Wtf.grid.ColumnModel([
           this.sm,
           new Wtf.KWLRowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.saleByItem.gridProductID"),  //"Product ID",
                dataIndex:'pid',
                title:'pid',
                pdfwidth :120,
                width: 100,
                align:'none'
                
            },{
                header:WtfGlobal.getLocaleText("acc.saleByItem.gridProduct"),  //"Product",
                dataIndex:'productname',
                title:'productname',
                pdfwidth :120,
                width: 100,
                align:'none'
                
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.type"),  //"Product Type",
                dataIndex:'productType',
                title: 'productType',
                pdfwidth :120,
                width: 100,
                align:'none',
                renderer: function(val){
                    return val;
                }
            },{
                header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),  //"Description",
                dataIndex:'productdesc',
                title:'productdesc',
                pdfwidth :120,
                width: 100,
                align:'none',                
                renderer : function(val) {
                    return "<div wtf:qtip=\'"+val+"\' wtf:qtitle='"+WtfGlobal.getLocaleText("acc.invReport.desc")+"'>"+val+"</div>";
                }
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.curPurchaseCost"),  //"Last Purchase Price",
                dataIndex:'purchasecost',
                title:'purchasecost',
                hidden:true,
                pdfwidth :120,
                width: 100,
                align:'currency', 
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.openingStock"),  //"Oening Stock (UOM)",
                dataIndex:'openingstockQty',
                title:'openingstockQty',
                pdfwidth :120,
                width: 100,
                align:'right',
                 renderer:function(val){
                   return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);  
                 }
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.openingvalue"),  //" Opening value (In Base Currency)",
                dataIndex:'openingstockvalue',
                title:'openingstockvalue',
                pdfwidth :120,
                width: 100,
                align:'currency', 
                renderer:WtfGlobal.currencyRenderer     
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.avgPurchaseCost"),  //"Avg. Purchase Cost",
                dataIndex:'avgcost',
                title:'avgcost',
                hidden:true,
                pdfwidth :120,
                width: 100,
                align:'currency',
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.stockIN"),  //"Stock IN",
                dataIndex:'stockInQty',
                title:'stockInQty',
                pdfwidth :120,
                width: 100,
                align:'right',
                 renderer:function(val){
                   return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);  
                 }
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.stockOUT"),  //"Stock OUT",
                dataIndex:'stockOutQty',
                title:'stockOutQty',
                pdfwidth :120,
                width: 100,
                align:'right',
                renderer:function(val){
                   return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);  
                 }
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.qty"),  //"Quantity On Hand",
                dataIndex:'quantity',
                title:'quantity',
                pdfwidth :120,
                width: 100,
                align:'right',
//                 summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                 renderer:function(val){
                   return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);  
                 }
            },{
            header:WtfGlobal.getLocaleText("acc.cc.2"), // uom,
            dataIndex:"uom",
            pdfwidth:80
        },
            {
                header:WtfGlobal.getLocaleText("acc.invReport.evaluationprice"),  //"Evaluation (In Base Currency)",
                dataIndex:'evaluationcost',
                title:'evaluationcost',
                pdfwidth :120,
                width: 100,
                align:'currency', 
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.lifo"),  //"LIFO Valuation",
                dataIndex:'lifo',
                title:'lifo',
                pdfwidth :120,
                hidden:true,
                width: 100,
                align:'currency',
                summaryType:'sum',
                summaryRenderer:this.opBalRenderer,
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.fifo"),  //"FIFO Valuation",
                dataIndex:'fifo',
                title:'fifo',
                pdfwidth :120,
                hidden:true,
                width: 100,
                align:'currency',
                summaryType:'sum',
                summaryRenderer:this.opBalRenderer,
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.WTavg"),  //"Weighted Avg. Valuation",
                dataIndex:'valuation',
                title:'valuation',
                pdfwidth :120,
                hidden:true,
                width: 100,
                align:'currency',
                summaryType:'sum',
                summaryRenderer:this.opBalRenderer,
                renderer:WtfGlobal.currencyRenderer
            }]);
        this.summary = new Wtf.ux.grid.GridSummary();
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.field.QuickSearchStockReport"),
            width: this.isCustomWidgetReport ? 150 : 200,
            id: "quickSearch" + this.id,
            Store: this.gridStore
        });
      this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        hidden : this.isCustomWidgetReport,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
        this.grid = new Wtf.grid.GridPanel({
            layout:'fit',
            region:"center",
            store: this.gridStore,
            cm:this.gridcm,
            sm:this.sm,
            border : false,
            loadMask : true,
//            plugins:[this.summary],
            viewConfig: {
               // forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn'))
            },
//            tbar:[this.quickPanelSearch,
//                WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-','-', {
//                xtype:'button',
//                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
//                iconCls:'accountingbase fetch',
//                tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),  //"Select a time period to view corresponding records.",
//                scope:this,
//                handler:this.fetchStatement
//            }],
            bbar:this.bbar1 
        });
        
    this.grid.on("render",function(){
        this.grid.getView().applyEmptyText(); 
    },this);


        this.gridStore.on('beforeload',function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = this.gridStore.baseParams;
            currentBaseParams.startdate=this.sdate;
            currentBaseParams.enddate=this.edate;
            currentBaseParams.ss = this.quickPanelSearch.getValue();
            currentBaseParams.limit = (this.pP.combo == undefined ? 30 : this.pP.combo.value);
            currentBaseParams.productCategoryid = this.productCategory.getValue();
            currentBaseParams.productType = this.productTypeCombo.getValue();
            if (this.pP.combo != undefined) {
                if (this.pP.combo.value == "All") {
                    var count = this.gridStore.getTotalCount();
                    var rem = count % 5;
                    if (rem == 0) {
                        count = count;
                    } else {
                        count = count + (5 - rem);
                    }
                    currentBaseParams.limit = count;
                }
            }
            this.gridStore.baseParams = currentBaseParams;
            WtfGlobal.setAjaxTimeOut();
        }, this);

        this.gridStore.on('load', function() {
            if(this.gridStore.getCount()<1) {
                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        this.sm.on("selectionchange",function(){
            var selectionArr =  this.sm.getSelections();
            if (selectionArr.length >= 1) {
                this.exportselRec.enable();
            } else{
                this.exportselRec.disable(); 
            }
        },this);
         this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        hidden : this.isCustomWidgetReport,
        iconCls: "advanceSearchButton"
    });
        this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: '30,27,28,29,31'.split(','),
        advSearch: false,
        isAvoidRedundent:true,
        reportid:Wtf.autoNum.inventoryValuation        //Used for remember search
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    Wtf.account.TransactionListPanelViewStockReport.superclass.constructor.call(this);
}

Wtf.extend(Wtf.account.TransactionListPanelViewStockReport,Wtf.Panel,{
     onRender: function(config){
         var btnArr = [];
        btnArr.push(this.quickPanelSearch, this.AdvanceSearchBtn, this.resetBttn, '-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate, '-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);

        if (this.isCustomWidgetReport !== true) {
            btnArr.push('-', WtfGlobal.getLocaleText("acc.cust.Productcategory"), this.productCategory, WtfGlobal.getLocaleText("acc.invReport.type"), this.productTypeCombo);
        }

        btnArr.push({
            xtype: 'button',
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
            iconCls: 'accountingbase fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"), //"Select a time period to view corresponding records.",
            scope: this,
            handler: this.fetchStatement
        });
         
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: btnArr,
                bbar: [this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.gridStore,
    //            searchField: this.quickPanelSearch,
//                displayInfo: true,
                displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
                })
            }),this.expButton,this.exportselRec]
            }]
        }); 
        this.add(this.leadpan);
        //this.fetchStatement(); // To Avoid initial page load request
        
        Wtf.account.TransactionListPanelViewStockReport.superclass.onRender.call(this,config);
    },
    
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    summaryRenderer:function(val){
        return WtfGlobal.currencyRenderer(Math.abs(val));
    },
    fetchStatement:function(){
        this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if(this.startDate.getValue()>this.endDate.getValue()){
            WtfComMsgBox(1,2);
            return;
        }


        this.gridStore.load({params:{
            startdate:this.sdate,
            enddate:this.edate,
            start:0,
            limit:(this.pP.combo==undefined?30:this.pP.combo.value),
            productCategoryid : this.productCategory.getValue(),
            productType : this.productTypeCombo.getValue()
        }})
    },

    getDates:function(start){
        var d=new Date();
        if(this.statementType=='BalanceSheet'){
            if(start){
                return new Date('January 1, 1970 00:00:00 AM');
            }else
                return d;

        }
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
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
        this.gridStore.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.autoNum.inventoryValuation,
            filterConjuctionCriteria: filterConjuctionCriteria,
            isFromStockReport:true
    }
        this.gridStore.load({
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
        this.gridStore.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.autoNum.inventoryValuation,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            isFromStockReport:true
        }
        this.gridStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
        }
});
