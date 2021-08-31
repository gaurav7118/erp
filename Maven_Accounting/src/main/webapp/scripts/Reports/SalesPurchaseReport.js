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

function getSalesPurchaseTabViewDynamicLoad(searchStr,filterAppend){
    if(!(WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showpurchaseprice)) || !(WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showsalesprice))){
        var reportPanel = Wtf.getCmp('SalesPurchase');
        if(reportPanel == null){
            reportPanel = new Wtf.account.TransactionListPanelViewSalesPurchase({
                id : 'SalesPurchase',
                border : false,
                title: WtfGlobal.getLocaleText("acc.field.StockSalesandPurchase"), //Sales and Purchase Report
                tabTip: WtfGlobal.getLocaleText("acc.field.StockSalesandPurchase"), //Sales and Purchase Report
                layout: 'fit',
                iscustreport : true,
                closable : true,
                isCustomer:true,
                isSalesPersonName:true,
                label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),
                iconCls:getButtonIconCls(Wtf.etype.salespurchase),
                searchJson:searchStr,
                filterAppend:filterAppend
            });
            Wtf.getCmp('as').add(reportPanel);
        }
        Wtf.getCmp('as').setActiveTab(reportPanel);
        showAdvanceSearch(reportPanel,searchStr, filterAppend);
        Wtf.getCmp('as').doLayout();
    }
    else{
         WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+WtfGlobal.getLocaleText("acc.field.StockSalesandPurchase"));
    }
}

//***********************************************************************************
Wtf.account.TransactionListPanelViewSalesPurchase = function(config) {
    Wtf.apply(this, config);
     
    this.GridRec = Wtf.data.Record.create([
    {
        name:'pid'
    },

    {
        name:'productDesc'
    },

    {
        name:'transactionDate', 
        type:'date'
    },

    {
        name:'transactionNumber'
    },

    {
        name:'personCode'
    },

    {
        name:'personName'
    },

    {
        name:'receivedqty'
    },

    {
        name:'deliveredqty'
    },

    {
        name:'stockRate'
    },

    {
        name:'value'
    },

    {
        name:'balance'
    },
    {
        name:'uomName'
    },
    {
        name:'currencysymbol'
    },
    {
        name:'currencycode'
    }
    ]);
    
    this.SalesPurchaseStore = new Wtf.data.GroupingStore({
        url:"ACCProductCMN/getStockLedger.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec),     
        groupField:"pid",
        sortInfo: {
            field: 'pid',
            direction: "ASC"
        }
    });
    this.isActivateLandedInvoiceAmt=false;
    this.SalesPurchaseStore.on('beforeload', function() {
        this.isActivateLandedInvoiceAmt=false;
        var currentBaseParams = this.SalesPurchaseStore.baseParams;
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.docType=this.salesPurchaseCombo.getValue(),
        currentBaseParams.customerId=this.customer.getValue(),
        currentBaseParams.productId=this.productList.getValue(),
        currentBaseParams.isStockLedger=false,
        currentBaseParams.isSalesPurchaseReport=true,
        this.SalesPurchaseStore.baseParams=currentBaseParams; 
        this.exportButton.enable()
        this.printButton.enable()
                 
    },this);
    WtfGlobal.setAjaxTimeOut();
    this.SalesPurchaseStore.on('load', function(store) {
        if(this.SalesPurchaseStore.getCount() < 1) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
        Wtf.MessageBox.hide();
    }, this);
    
    this.SalesPurchaseStore.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
    }, this);
    

    this.grid = new Wtf.grid.GridPanel({    
        store:this.SalesPurchaseStore,
        border:false,
        layout:'fit',
        view: new Wtf.grid.GroupingView({
            startCollapsed :true,
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: true,
            hideGroupedColumn: true
        }),
        loadMask:true,
        columns:[
        {
            header:"",
            dataIndex:"pid",
            hidden : true
        },
        {
            header:WtfGlobal.getLocaleText("acc.productList.gridProductID"), // "Product ID",
            dataIndex:"pid",
            pdfwidth:75,
            fixed: true,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridDescription"), // "Description",
            dataIndex:"productDesc",
            renderer : function(val) {
                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
            },
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Date"), // "Date",
            dataIndex:'transactionDate',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.DocumentNo"), // "Document #",
            dataIndex:"transactionNumber",
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Code"), // "Code",
            dataIndex:"personCode",
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.partyORcostCenter"), // "Party / Cost Center",
            dataIndex:"personName",
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Received"), // "Received",
            dataIndex:"receivedqty",
            renderer:function(v, m, rec){
                var val = (rec.data["receivedqty"]!="" && rec.data["receivedqty"]!=undefined)?rec.data["receivedqty"]:""
                if(val!="" && val!=undefined){
                    var value=(parseFloat(getRoundofValue(rec.data["receivedqty"])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(rec.data["receivedqty"])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    var uomname = rec.data["uomName"];
                    return value+" "+uomname;
                }                         
            },
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Delivered"), // "Delivered",
            dataIndex:"deliveredqty",
            renderer:function(v, m, rec){
                var val = (rec.data["deliveredqty"]!="" && rec.data["deliveredqty"]!=undefined)?rec.data["deliveredqty"]:""
                if(val!="" && val!=undefined){
                    var value=(parseFloat(getRoundofValue(rec.data["deliveredqty"])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(rec.data["deliveredqty"])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    var uomname = rec.data["uomName"];
                    return value+" "+uomname;
                }                         
            },
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            hidden:true,
            pdfwidth:85
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),//"Unit Price"
            dataIndex:"stockRate",
            renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol,
            //                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,         // To show rate upto 3 decimal
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),//"Total Amount"
            dataIndex:"value",
            //                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:75
        }
        ]
    });
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.stockLedger.QuickSearchEmptyText"), // "Search by Document no, Description, Code, Party / Cost Center ...",
        width: 200,
        id:"quickSearch"+config.helpmodeid,
        field: 'transactionNumber'
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
        //    readOnly:true,
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
        //  readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    
    var cusven=[];
    var Data = [];
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showpurchaseprice) && !WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showsalesprice)){
        Data.push([0,'All Documents'],[1,'Purchase Documents'],[2,'Sales Documents'])
        cusven.push([0,'Customer'],[1,'Vendor'],[2,'All'])
    }else if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showpurchaseprice)){
        Data.push([1,'Purchase Documents'])
        cusven.push([1,'Vendor'],[2,'All'])
    }else if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showsalesprice)){
        Data.push([2,'Sales Documents'])
        cusven.push([0,'Customer'],[2,'All'])
    }
     
    this.cusvenStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data :cusven
    });
    
    this.salesPurchaseComboStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data :Data
    });

    this.cusvenCombo = new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.salesPurchase"),//"Sales Purchase",
        store: this.cusvenStore,  
        scope: this,
        readOnly :true,
        valueField: 'id',
        displayField: 'name',
        forceSelection: true,
        width:150,
        disabled:(WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showpurchaseprice) && WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.showsalesprice)),
        emptyText:WtfGlobal.getLocaleText("acc.inv.cusven"),//'Select Customer/Vendor',
        mode: 'local',
        triggerAction: 'all',
        value:this.cusvenStore.getAt(this.cusvenStore.getTotalCount()-1).data.id
    });
    var url;
    if(this.cusvenCombo.getValue()==0 && this.cusvenCombo.getValue()!=undefined){
        url="ACCCustomer/getCustomersForCombo.do";
    }
    if(this.cusvenCombo.getValue()==1 && this.cusvenCombo.getValue()!=undefined){
        url="ACCVendor/getVendorsForCombo.do";
    }
    var baseParamArray={
        deleted:false,
        nondeleted:true,
        combineData:-1  //Send For Seprate Request
    };
    this.cusRec = new Wtf.data.Record.create([
    {
        name: 'accid'
    }, {
        name: 'accname'
    }, {
        name: 'acccode'
    }
    ]);   
    this.customerAccStore =  new Wtf.data.Store({   
        url:url,
        proxy: new Wtf.data.HttpProxy({
            url: url
        }),
        baseParams:baseParamArray,
        reader: new  Wtf.data.KwlJsonReader({
            root: "data"
        },this.cusRec)
    });
    
    
    var storeNewRecord = new this.cusRec({
        accname: 'All',
        accid: ''
    });
    
    this.customerAccStore.on("load", function(store){
        this.customer.store.insert( 0,storeNewRecord);
        this.customer.setValue("");   
    },this);
    //       this.customerAccStore.load();
    
    this.cusvenCombo.on("select",function(combo){
        if(combo.getValue()==0){
            this.customer.enable();
            url="ACCCustomer/getCustomersForCombo.do";
        }
        if(combo.getValue()==1){
            this.customer.enable();
            url="ACCVendor/getVendorsForCombo.do";
        }
        this.customerAccStore.proxy.conn.url=url;
        if(combo.getValue()!=2){
            this.customerAccStore.load({
                url:url
            });
        }else{
            this.customer.disable();
            this.customerAccStore.removeAll()
        }
        if (this.customer) {
            this.customer.store.insert(0, storeNewRecord);
            this.customer.setValue("");
        }
    },this);
    
    this.CustomerComboconfig = {
        //hiddenName:this.businessPerson.toLowerCase(),         
        store: this.customerAccStore,
        valueField:'accid',
        hideLabel:true,
        displayField:'accname',
        emptyText: WtfGlobal.getLocaleText("acc.common.select"), //'Select',
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this,
        value:'',
        disabled:true
    };
    
    this.customer = new Wtf.common.Select(Wtf.applyIf({
        multiSelect:false,
        fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*' ,
        forceSelection:true,    
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        listWidth:200,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        width:200
    },this.CustomerComboconfig));
    this.customer.store.insert(0, storeNewRecord);
    this.customer.setValue("");   
    // Product
    this.productRec = Wtf.data.Record.create ([
    {
        name:'productid'
    },

    {
        name:'pid'
    },

    {
        name:'type'
    },

    {
        name:'productname'
    }
    
    ]);
    
    this.productStore = new Wtf.data.Store({
        url:"ACCProductCMN/getProductsForCombo.do",
        baseParams:{
            module_name : "PRODUCT_CATEGORY"
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.productRec)
    });
    this.productStore.load();
        
    this.productList = new Wtf.common.Select(Wtf.apply({
        multiSelect:false,
        fieldLabel:WtfGlobal.getLocaleText("acc.product.productName"), //"Product Name*" ,
        extraFields:['pid'],
        extraComparisionField:'pid',// type ahead search on product id as well.
        listWidth:Wtf.ProductComboListWidth,
        forceSelection:true
    },{
        name:"productlist",
        id:"productlist",
        store: this.productStore,
        valueField:'productid',
        displayField:'productname',
        emptyText:WtfGlobal.getLocaleText("acc.prod.comboEmptytext"), //"Please Select Product",
        anchor:'85%',
        mode: 'local',
        selectOnFocus:true,
        allowBlank:true,
        triggerAction:'all',
        typeAhead: true,
        scope:this,
        value:'0'
    }));
    this.productStore.on("load", function(store) {
        var storeNewRecord = new this.productRec({
            productid: '',
            pid: '',
            productname: 'All'
        });
        this.productList.store.insert(0, storeNewRecord);
        this.productList.setValue("");
    }, this);
    //product
    this.salesPurchaseCombo = new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.salesPurchase"),//"Sales Purchase",
        store: this.salesPurchaseComboStore,  
        scope: this,
        readOnly :true,
        valueField: 'id',
        displayField: 'name',
        forceSelection: true,
        width:150,
        emptyText:WtfGlobal.getLocaleText("acc.mp.selectDocumentType"),//'Select Document Type',
        mode: 'local',
        triggerAction: 'all',
        value:this.salesPurchaseComboStore.getAt(0).data.id
    });

    this.fetchBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.select.preferences.tosearch.records"),//'Select/Input your preferences to search desired records',
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.fetchStatement                        
    });
    //    this.isActivateLandedInvAmt=new Wtf.form.Checkbox({
    //        fieldLabel:WtfGlobal.getLocaleText("acc.field.StockLedgerreportIncludeLanInvAmt"),
    //        name:'includelandedinvamt'
    //    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
        this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        filename:WtfGlobal.getLocaleText("acc.field.StockSalesandPurchase")+"_v1",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        params:{
            enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            isExport:true,
            //            start:0,
            //            limit:this.pP.combo.value,
            isStockLedger : false,
            isSalesPurchaseReport : true,
            searchJson:this.searchJson==undefined?"":this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit==undefined?"":this.filterConjuctionCrit,
            docType:this.salesPurchaseCombo.getValue()
        },
        get:Wtf.autoNum.StockLedger
    });
    this.exportButton.on("click",function(){
        this.exportButton.setParams({
            startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            isStockLedger : false,
            isSalesPurchaseReport : true,
            isExport:true,
            searchJson:this.searchJson==undefined?"":this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit==undefined?"":this.filterConjuctionCrit
        });
    },this);
    
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  //'Print report details',
        disabled :true,
        params:{ 	
            stdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate:  WtfGlobal.convertToGenericDate(this.endDate.getValue()),		
            name: WtfGlobal.getLocaleText("acc.field.StockLedger"),
            isStockLedger : false,
            isSalesPurchaseReport : true,
            isExport:true 
        },
        label: WtfGlobal.getLocaleText("acc.field.StockLedger"),
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.StockLedger
    })
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

    this.tbar1 = new Array();
    this.tbar1.push(this.resetBttn, this.exportButton, this.printButton, '-', this.expandCollpseButton);
    this.grid = new Wtf.grid.GridPanel({    
        store:this.SalesPurchaseStore,
        border:false,
        layout:'fit',
        tbar:this.tbar1,
        view: new Wtf.grid.GroupingView({
            startCollapsed :true,
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: true,
            hideGroupedColumn: true
        }),
        loadMask:true,
        columns:[
        {
            header:"",
            dataIndex:"pid",
            hidden : true
        },
        {
            header:WtfGlobal.getLocaleText("acc.productList.gridProductID"), // "Product ID",
            dataIndex:"pid",
            pdfwidth:75,
            fixed: true,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridDescription"), // "Description",
            dataIndex:"productDesc",
            renderer : function(val) {
                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
            },
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Date"), // "Date",
            dataIndex:'transactionDate',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.DocumentNo"), // "Document #",
            dataIndex:"transactionNumber",
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Code"), // "Code",
            dataIndex:"personCode",
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.partyORcostCenter"), // "Party / Cost Center",
            dataIndex:"personName",
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Received"), // "Received",
            dataIndex:"receivedqty",
            renderer:function(v, m, rec){
                var val = (rec.data["receivedqty"]!="" && rec.data["receivedqty"]!=undefined)?rec.data["receivedqty"]:""
                if(val!="" && val!=undefined){
                    var value=(parseFloat(getRoundofValue(rec.data["receivedqty"])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(rec.data["receivedqty"])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    var uomname = rec.data["uomName"];
                    return value+" "+uomname;
                }                         
            },
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.stockLedger.Delivered"), // "Delivered",
            dataIndex:"deliveredqty",
            renderer:function(v, m, rec){
                var val = (rec.data["deliveredqty"]!="" && rec.data["deliveredqty"]!=undefined)?rec.data["deliveredqty"]:""
                if(val!="" && val!=undefined){
                    var value=(parseFloat(getRoundofValue(rec.data["deliveredqty"])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(rec.data["deliveredqty"])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    var uomname = rec.data["uomName"];
                    return value+" "+uomname;
                }                         
            },
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            hidden:true,
            pdfwidth:85
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),//"Unit Price",
            dataIndex:"stockRate",
            renderer: WtfGlobal.withCurrencyUnitPriceRenderer,
            //                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,         // To show rate upto 3 decimal
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),//"Total Amount",
            dataIndex:"value",
            //                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:75
        }
        ]
    });
    this.grid.on('render',
        function(){
            new Wtf.Toolbar({
                renderTo: this.grid.tbar,
                items:  this.tbar1
            });
        },this);
        
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: '30,27,28,29,31'.split(','),
        advSearch: false,
        isAvoidRedundent:true,
        reportid:Wtf.autoNum.SalesPurchaseReport        //Used for remember search
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    Wtf.account.TransactionListPanelViewSalesPurchase.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewSalesPurchase,Wtf.Panel, {
    onRender: function(config){
        this.SalesPurchaseStore.load({
            params:{
                start:0,
                limit:30,
                isprovalreport:true
            }
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
                tbar: [this.quickPanelSearch, this.AdvanceSearchBtn, '-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate,this.salesPurchaseCombo,this.productList,this.cusvenCombo,this.customer, this.fetchBttn],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.SalesPurchaseStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    })
                })
            }]
        }); 
        this.add(this.leadpan);
        
        Wtf.account.TransactionListPanelViewSalesPurchase.superclass.onRender.call(this,config);
    },
    
    handleResetClick: function () {
        this.productList.setValue('');
        this.cusvenCombo.setValue(this.cusvenStore.getAt(this.cusvenStore.getTotalCount()-1).data.id);
        this.customer.setValue('');
        this.customer.disable();
        this.salesPurchaseCombo.setValue(this.salesPurchaseComboStore.getAt(0).data.id);
        this.startDate.reset();
        this.endDate.reset();
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.fetchStatement();
    },
    
    fetchStatement:function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
                 
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        var fromdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        
        this.SalesPurchaseStore.load({
            params: {
                startdate:fromdate,
                enddate:todate,
                start:0,
                ss:this.quickPanelSearch.getValue(),
                limit:this.pP.combo.value,
                isprovalreport:true,
                docType:this.salesPurchaseCombo.getValue()
            }
        });
    },
    QuantityRender: function(v,m,rec){
        var val = WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec)
        if(rec.data.transactionNumber==""){
            return '<b>'+val+'</b>';
        }else{
            return val;
        }
    },
    unitRenderer: function(value,metadata,record) {
        if(value != '') {
            value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
        return value;
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
        this.SalesPurchaseStore.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.SalesPurchaseStore.load({
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
        this.SalesPurchaseStore.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.SalesPurchaseStore.load({
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


