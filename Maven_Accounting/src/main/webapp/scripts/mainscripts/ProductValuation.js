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
Wtf.account.productValuationGrid = function (config){
    Wtf.apply(this,config);
    Wtf.account.productValuationGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.account.productValuationGrid,Wtf.Panel,{
    initComponent:function (){
       Wtf.account.productValuationGrid.superclass.initComponent.call(this);

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
           // extraFields: ['name'],
            extraComparisionField: 'id', // type ahead search on product id as well.
            listWidth: Wtf.ProductComboListWidth,
            width: 150
        },this.CategoryComboConfig));
        
        this.productCategoryStore.load();
        
        this.productCategoryStore.on("load", function(store) {
            WtfGlobal.resetAjaxTimeOut();
            var record = new this.productCategoryRec({
                id: "All",
                name:"All"
                
            });
            this.productCategory.store.insert(0, record);
            this.productCategory.setValue("All");
        }, this);

        this.productCategory.on('select',function(combo, productRec) {
            if (productRec.get('id') == 'All') {
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(productRec.get('id'));
            }
        }, this);
        
         this.productTypeStore= new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data : [['All','All'],['d8a50d12-515c-102d-8de6-001cc0794cfa','Inventory Part'],['e4611696-515c-102d-8de6-001cc0794cfa','Inventory Assembly'],['ff8080812f5c78bb012f5cfe7edb000c9cfa','Inventory Non-Sale']]
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
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(productRec.get('id'));
            }
        }, this);

       this.gridRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'pid'},
            {name:'productdesc'},
            {name:'productType'},
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
            baseParams:{mode:29,isprovalreport:true,isInventoryValuation:true},
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"totalCount"
            },this.gridRec)
        });
        
        
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
            filename : WtfGlobal.getLocaleText("acc.dashboard.InventoryValuationReport")+"_v1",
            params:{
//                startdate:this.sdate,
                enddate:this.edate,
                exportInventoryValuation:true,
                productCategoryid : this.productCategory.getValue(),
                productType : this.productTypeCombo.getValue(),
                companyids : companyids,
                gcurrencyid : gcurrencyid,
                userid : loginid,
                get:555
            },
            menuItem:{xls:true,pdf:true,rowPdf:false},
            get: Wtf.autoNum.inventoryValuation
        });

        this.gridcm = new Wtf.grid.ColumnModel([
           new Wtf.KWLRowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.saleByItem.gridProduct"),  //"Product",
                dataIndex:'productname',
                title:'productname',
                pdfwidth :120,
                width: 100,
                align:'none'
                
            }, {
                header: WtfGlobal.getLocaleText("acc.productList.gridProductID"),
                dataIndex: 'pid',
                align: 'left',
                pdfwidth: 120,
                sortable: true
            }, {
                header: WtfGlobal.getLocaleText("acc.invReport.type"), //"Product Type",
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
                    return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.invReport.desc")+"'>"+val+"</div>";
                }
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.curPurchaseCost"),  //"Last Purchase Price",
                dataIndex:'purchasecost',
                title:'purchasecost',
                pdfwidth :120,
                width: 100,
                align:'currency', 
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.avgPurchaseCost"),  //"Avg. Purchase Cost",
                dataIndex:'avgcost',
                title:'avgcost',
                pdfwidth :120,
                width: 100,
                align:'currency',
                renderer:WtfGlobal.currencyRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invReport.qty"),  //"Quantity On Hand",
                dataIndex:'quantity',
                title:'quantity',
                pdfwidth :120,
                width: 100,
                align:'right',
                 summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                 renderer:function(val){
                   return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);  
                 }
            },{
            header:WtfGlobal.getLocaleText("acc.product.uom"), // uom,
            dataIndex:"uom",
            pdfwidth:80,
            align:'none',
            title:'uom'
        },{
                header:WtfGlobal.getLocaleText("acc.invReport.lifo"),  //"LIFO Valuation",
                dataIndex:'lifo',
                title:'lifo',
                pdfwidth :120,
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
                width: 100,
                align:'currency',
                summaryType:'sum',
                summaryRenderer:this.opBalRenderer,
                renderer:WtfGlobal.currencyRenderer
            }]);
        this.summary = new Wtf.ux.grid.GridSummary();
        this.grid = new Wtf.grid.GridPanel({
            layout:'fit',
            region:"center",
            store: this.gridStore,
            cm:this.gridcm,
            border : false,
            loadMask : true,
            plugins:[this.summary],
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar:[WtfGlobal.getLocaleText("acc.cust.Productcategory"),this.productCategory,WtfGlobal.getLocaleText("acc.invReport.type"),this.productTypeCombo,//'From',this.startDate,
                  WtfGlobal.getLocaleText("acc.invReport.ason"),this.endDate, {
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                iconCls:'accountingbase fetch',
                tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),  //"Select a time period to view corresponding records.",
                scope:this,
                handler:this.fetchStatement
            }],
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
            }),this.expButton]
        });


        this.gridStore.on('beforeload',function(s,o){
             WtfGlobal.setAjaxTimeOut();
            if(!o.params)o.params={};
            var currentBaseParams = this.gridStore.baseParams;
//            currentBaseParams.startdate=this.sdate;
            currentBaseParams.enddate=this.edate;
            this.gridStore.baseParams=currentBaseParams;
        },this);

        this.gridStore.on('load', function() {
               WtfGlobal.resetAjaxTimeOut();
            if(this.gridStore.getCount()<1) {
                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().refresh();
            }
        }, this);
        
         this.gridStore.on('loadexception', function() {
            WtfGlobal.resetAjaxTimeOut();
        }, this);

//        this.gridStore.load({
//            params:{
//                start:0,
//                limit:15
//            }
//        });
        this.add(this.grid);
        this.fetchStatement();
    },
    summaryRenderer:function(val){
        return WtfGlobal.currencyRenderer(Math.abs(val));
    },
    fetchStatement:function(){
//        this.sDate=this.startDate.getValue();
//        this.eDate=this.endDate.getValue();
        this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if(this.sDate>this.eDate){
            WtfComMsgBox(2,2);
            return;
        }


        this.gridStore.load({params:{
//            startdate:this.sdate,
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
    }
});
