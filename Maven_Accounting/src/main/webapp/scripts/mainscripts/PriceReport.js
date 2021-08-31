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

//****************************************************************************************************
// Product Management -> Price List Report -> General Price List Report
//****************************************************************************************************

Wtf.account.PriceReport = function(config) {
    this.createTBar();
    
    this.Record = new Wtf.data.Record.create([
        {name: 'priceid'},
        {name: 'carryin', type:'boolean'},
        {name: 'price'},
        {name: 'applydate', type:'date'},
        {name: 'currency'},
        {name: 'productid'},
        {name: 'productuuid'},
        {name: 'uomname'},
        {name: 'initialPrice'},
        {name: 'uomid'},
        {name: 'currencyid'},
        {name: 'productName'},
        {name: 'productDesc'}
    ]);

    this.Store = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"
        }, this.Record),
        url: "ACCProduct/getProductPrice.do",
        remoteSort: true,
        baseParams: {
            mode: 12,
            productPriceinMultipleCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
            productid: ""
        },
        sortInfo: {
            field: 'productid',
            direction: 'ASC'
        },
        groupField : 'productid'
    });
    WtfComMsgBox(29,4,true);

    this.sm = new Wtf.grid.CheckboxSelectionModel();

    this.gridcm = new Wtf.grid.ColumnModel([this.sm,
        {
            header: WtfGlobal.getLocaleText("acc.product.gridProductID"), // "Product ID",
            dataIndex: 'productid'
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
            dataIndex: 'productName',
            pdfwidth:150,
            sortable: true,
            renderer: function(val,m,rec) {
                 if(val){
                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<a class='hirarchical' wtf:qtip='" + val + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rec.data.productuuid + "\")'>" + val + "</a>";
                }
            }
        },{
            header: WtfGlobal.getLocaleText("acc.productList.gridProductDescription"), // "Product Description",
            dataIndex: 'productDesc',
            sortable: true,
            pdfwidth:150,
            renderer: function(val,m,rec) {
                if(val){
                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.productList.gridProductDescription") + "'>" + val + "</div>";
                }    
            }
        },{
            header: WtfGlobal.getLocaleText("acc.rem.74"), // "Price Type",
            dataIndex: 'carryin',
            sortable: true,
            pdfwidth:150,
            renderer: this.CarryInRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridCurrency"), // 'Currency',
            dataIndex: 'currency',
            pdfwidth:150,
            hidden: !Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
            align: 'right'
        },{
            header: WtfGlobal.getLocaleText("acc.rem.75"), // 'Price',
            dataIndex: 'price',
            pdfwidth:150,
            align: 'right',
            sortable: true,
            renderer: function(v,m,rec){
                if((!Wtf.dispalyUnitPriceAmountInSales || !Wtf.dispalyUnitPriceAmountInPurchase)) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else{
                    if(Wtf.account.companyAccountPref.productPriceinMultipleCurrency){
                        return WtfGlobal.withoutCurrencyUnitPriceRenderer(v, m, rec);
                    } else{
//                        return WtfGlobal.currencyRenderer(v,m,rec)
                        return WtfGlobal.withoutCurrencyUnitPriceRenderer(v,false,rec);
                    }
                }
            }
        },{
            header: WtfGlobal.getLocaleText("acc.rem.73"), // "Date Modified",
            dataIndex: 'applydate',
            align: 'center',
            pdfwidth:150,
            sortable: true,
            renderer: WtfGlobal.onlyDateRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.masterprice.uomname"), // 'uom',
            dataIndex: 'uomname',
            pdfwidth:150,
            align: 'right'
        },{
            header: WtfGlobal.getLocaleText("acc.productList.gridInitialPurchasePrice"), // 'Initial Price',
            dataIndex: 'initialPrice',
            pdfwidth:50,
            renderer: function (v, m, rec) {    //To show "Yes/No" in UI for "true/false" value
                if (!v)
                    return "No";
                if (rec.data.initialPrice) {
                    v = 'Yes';
                }
                return v;
            }
        }
    ]);
    this.bBarBtnArr=[];
    
    this.exportButtonProdPrice= new Wtf.exportButton({
        obj: this,
        id: 'generalpricelistxport',
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //"Export Report details.",  
        params: {name: WtfGlobal.getLocaleText("acc.productList.pricetab")},
        menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
        get:Wtf.autoNum.GeneralPriceListReport,//what is this?
        filename: WtfGlobal.getLocaleText("acc.productList.pricetab") + "_v1",
        label: WtfGlobal.getLocaleText("acc.productList.pricetab")

    }),

    this.bBarBtnArr.push('-', this.exportButtonProdPrice);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows: true,
        layout: 'fit',
        store: this.Store,
        cm: this.gridcm,
        border: false,
        loadMask: true,
        sm: this.sm,
        view : new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: true,
            enableGroupingMenu: true,
            hideGroupedColumn: true
        }),
        viewConfig: {
            forceFit: true,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        tbar: this.tBarArr,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
            plugins: this.pP = new Wtf.common.pPageSize(),
            items: this.bBarBtnArr
        })
    });
    this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
    this.Store.on('load',this.hideMsg,this);
    this.Store.on('datachanged', this.handleStoreDataChanged, this);
    this.sm.on("selectionchange",this.enableDisableButtons,this);
    Wtf.account.PriceReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.PriceReport, Wtf.Panel, {
    hideMsg: function(store) {
        if (this.Store.getCount() == 0) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    
    onRender: function(config) {
        this.add(this.grid);
        this.fetchStatement();
        Wtf.account.PriceReport.superclass.onRender.call(this,config);
    },

    CarryInRenderer: function(carryIn) {
        var temptxt = "";
        if (carryIn == false) {
            temptxt = WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"); // "Sale Price";
        } else {
            temptxt = WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"); // "Purchase Price";
        }
        return temptxt;
    },
    
    createTBar: function() {
        this.arrEDSingleS = []; // Enable/Disable button's indexes on single select
        this.arrEDMultiS = []; // Enable/Disable button's indexes on multi select
        this.tBarArr = [];
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.priceReport.QuickSearchEmptyText"), // "Search by Product ID, Product Name...",
            width: 200,
            hidden: false,
            field: 'productid'
        });
        this.tBarArr.push(this.quickPanelSearch);
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: false,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        this.tBarArr.push(this.resetBttn);
        
        this.productRec = Wtf.data.Record.create ([
            {name: 'productid'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);
   
        this.productStore = new Wtf.data.Store({
//            url: "ACCProduct/getProductsForCombo.do",
            url:"ACCProductCMN/getProductsIdNameforCombo.do",
            baseParams: {
                mode: 22,
                excludeParent:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        var baseParamsforCombo = this.productStore.baseParams;
        var configforCombo = {
            multiSelect: true,
            listWidth: Wtf.ProductComboListWidth
        }
        this.productCombo =CommonERPComponent.createProductMultiselectPagingComboBox(200,300,30,this,baseParamsforCombo,configforCombo);
        var record = new this.productRec({
                productid: "All",
                pid: "",
                type: "",
                productname: "All"
            });
        this.productCombo.store.insert(0, record);
        this.productCombo.setValue("All");
        this.tBarArr.push('-', WtfGlobal.getLocaleText("acc.field.SelectProduct"), this.productCombo);
        
        this.productCombo.store.on("load", function(store) {
            WtfGlobal.resetAjaxTimeOut();
            var record = new this.productRec({
                productid: "All",
                pid: "",
                type: "",
                productname: "All"
            });
            this.productCombo.store.insert(0, record);
        }, this);
        
        this.productCombo.on('select',function(combo, productRec) {
            if (productRec.get('productid') == 'All') {
                combo.setValue("");
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(productRec.get('productid'));
            }
        }, this);
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        this.tBarArr.push('-', this.fetchBttn);
        
        this.addPrice = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.TDSMasterRates.Add"), //'Add',
            id: 'addPrice' + this.id,
            scope:this,
            tooltip: WtfGlobal.getLocaleText("acc.productList.addNewPriceTT"), //{text:"Click here to add new price (Purchase Price & Sales Price) by selecting an available product.",dtext:"Select a product to add price.", etext:"Add price to the selected product."},
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function(){
                if(!Wtf.dispalyUnitPriceAmountInSales || !Wtf.dispalyUnitPriceAmountInPurchase){
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.pricelist.addAlert"));
                }else{
                    var handleAdd=this.handleEditPrice.createDelegate(this, [false]);
                    handleAdd();
                }
            }
        });
        this.editPrice = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.edit"), // "Edit",
            tooltip : WtfGlobal.getLocaleText("acc.common.editTT"), // "Edit Selected Record.",
            id: 'editPrice' + this.id,
            scope: this,
            iconCls : getButtonIconCls(Wtf.etype.edit),
            disabled: true,
//            handler: this.handleEditPrice.createDelegate(this, [true])
            handler: function(){
                if(!Wtf.dispalyUnitPriceAmountInSales || !Wtf.dispalyUnitPriceAmountInPurchase){
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.pricelist.editAlert"));
                }else{
                    var handleEdit=this.handleEditPrice.createDelegate(this, [true]);
                    handleEdit();
                }
            }
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice)) {
            this.tBarArr.push('-', this.addPrice);
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.editprice)) {
            this.tBarArr.push('-', this.editPrice);
            this.arrEDSingleS.push(this.tBarArr.length - 1);
        }
        
        this.deletePrice = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.delete"), // "Delete",
            tooltip : WtfGlobal.getLocaleText("acc.fixed.asset.delete.selected"), // "Delete Selected Record(s)",
            id: 'deletePrice' + this.id,
            scope: this,
            iconCls : getButtonIconCls(Wtf.etype.deletebutton),
            disabled: true,
            handler: this.handleDeletePrice
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.deleteprice)) {
            this.tBarArr.push('-', this.deletePrice);
            this.arrEDMultiS.push(this.tBarArr.length - 1);
        }
        
        this.helpText = "<p>Initial purchase price can be used only for opening stock.\n\
            Initial purchase price can be added using product creation form and opening stock import. Initial purchase price won't get populated in any of the purchase transaction(s).</p>";
        this.helpButton = new Wtf.Toolbar.Button({
            scope: this,
            iconCls: 'helpButton',
            tooltip: {text: WtfGlobal.getLocaleText("acc.rem.2")}, //{text:'Get started by clicking here!'},
            mode: id,
            handler: function (e, target, panel) {
                var tmp = e.getEl().getXY();
                var we = new Wtf.CostAndSellingPricehelpDetails();
                we.showHelpWindow(tmp[0], tmp[1], WtfGlobal.getLocaleText("acc.productList.pricetab"), this.helpText);
            }
        });
        this.tBarArr.push('->', this.helpButton);
    },
    
    handleStoreBeforeLoad: function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.productid = this.productCombo.getValue();
        this.Store.baseParams = currentBaseParams;
    },
    
    handleStoreDataChanged: function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    
    enableDisableButtons: function() {
//        WtfGlobal.enableDisableBtnArr(this.tBarArr, this.grid, this.arrEDSingleS, this.arrEDMultiS);
        var multi = !this.grid.getSelectionModel().hasSelection();
        var single = (this.grid.getSelectionModel().getCount() != 1);
        var initialPrice = false;
        this.selectedRecArr = this.grid.getSelectionModel().getSelections();
        for (var i = 0; i < this.selectedRecArr.length; i++) {
            var record = this.selectedRecArr[i];
            if (record.data['initialPrice']!=undefined && record.data['initialPrice']!= ""  && record.data['initialPrice'] == true) {
                initialPrice = true;
            }
        }
        for (var i = 0; i < this.arrEDMultiS.length; i++) {
            if (!initialPrice) {
                this.tBarArr[this.arrEDMultiS[i]].setDisabled(multi);
                WtfGlobal.setTip(this.tBarArr[this.arrEDMultiS[i]]);
            } else {
                this.tBarArr[this.arrEDMultiS[i]].setDisabled(true);
            }
        }
        for (i = 0; i < this.arrEDSingleS.length; i++) {
            if (this.tBarArr[this.arrEDSingleS[i]] != undefined) {
                if (!initialPrice) {
                    this.tBarArr[this.arrEDSingleS[i]].setDisabled(single);
                    WtfGlobal.setTip(this.tBarArr[this.arrEDSingleS[i]]);
                } else {
                    this.tBarArr[this.arrEDSingleS[i]].setDisabled(true);
                }
            }
        }
    },
    
    handleEditPrice: function(isEdit) {
        var priceRec = this.grid.getSelectionModel().getSelected();
        callMasterPricelistWindow("", isEdit, priceRec); // call the function for mass update of price
        Wtf.getCmp("pricewindow").on('update',function() {
            (function() {
                this.Store.load();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        },this);
    },
    
    handleDeletePrice: function() {
        if (this.grid.getSelectionModel().hasSelection() == false) {
            WtfComMsgBox(34,2);
            return;
        }
        var data = [];
        var arr = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid, this.recArr, true, 0, 2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.delete.selected.price.entry"), function(btn) {
            if (btn != "yes") {
                for (var i=0; i<this.recArr.length; i++) {
                    var ind = this.Store.indexOf(this.recArr[i])
                    var num = ind%2;
                    WtfGlobal.highLightRowColor(this.grid, this.recArr[i], false, num, 2, true);
                }
                return;
            }
            for (i=0; i<this.recArr.length; i++) {
                arr.push(this.Store.indexOf(this.recArr[i]));
            }
            data = WtfGlobal.getJSONArray(this.grid, true, arr);
            
            var idData = "";
            for (var i=0; i<this.recArr.length; i++) {
                var rec = this.recArr[i];
                idData += "{\"priceid\":\""+rec.get('priceid')+"\"},";
            }
            if (idData.length > 1) {
                idData = idData.substring(0, idData.length-1);
            }
            data = "[" + idData + "]";
            
            Wtf.Ajax.requestEx({
                url: "ACCProduct/deletePriceList.do",
                params: {
                    data: data
                }
            },this, this.genDeleteSuccessResponse, this.genDeleteFailureResponse);
        },this);
    },
    
    genDeleteSuccessResponse: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.pricingBandMaster"),response.msg],response.success*2+1);
        for (var i=0; i<this.recArr.length; i++) {
            var ind = this.Store.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i], false, num, 2, true);
        }
        if (response.success) {
            (function() {
                this.Store.load();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },
    
    genDeleteFailureResponse: function(response) {
        for (var i=0; i<this.recArr.length; i++) {
            var ind = this.Store.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.grid, this.recArr[i], false, num, 2, true);
        }
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg],2);
    }
});


//****************************************************************************************************
// Product Management -> Price List Report -> Vendor Price List Report/Customer Price List Report
//****************************************************************************************************

Wtf.account.PriceReportCustVen = function(config) {
    Wtf.apply(this, config);
    this.createTBar();
    
    this.Record = new Wtf.data.Record.create([
        {name: 'affecteduser'},
        {name: 'priceid'},
        {name: 'carryin', type:'boolean'},
        {name: 'price'},
        {name: 'currency'},
        {name: 'applydate', type:'date'},
        {name: 'productid'},
        {name: 'productuuid'},
        {name: 'currencyid'},
        {name: 'affecteduserid'},
        {name: 'affectedusercode'},
        {name: 'productName'},
        {name: 'productDesc'},
        {name: 'pricrinstockoum'},
        {name: 'stockoum'},
        {name: 'uomname'},
        {name: 'uomid'},
    ]);

    this.Store = new Wtf.ux.grid.MultiGroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"
        },this.Record),
        url: "ACCReports/getProductPriceCustVen.do",
        remoteSort: true,
        baseParams: {
            mode: 12,
            productPriceinMultipleCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
            carryin: !this.isCust
        },
        sortInfo: {
            field: 'affecteduser',
            direction: 'ASC'
        },
        groupField : ['affecteduser','productid']
    });
    var colModelArray = GlobalColumnModelForReports[Wtf.Acc_Product_Master_ModuleId];
    WtfGlobal.updateStoreConfig(colModelArray, this.Store);  
    var groupView = new Wtf.ux.grid.MultiGroupingView({
        forceFit: true,
        showGroupName: true,
        enableNoGroups: false,
        isGrandTotal: false,
        isGroupTotal: false,
        hideGroupedColumn: true,
        emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
        groupTextTpl: '{text}: {gvalue}'
    });
    
    WtfComMsgBox(29,4,true);
    
    this.sm = new Wtf.grid.CheckboxSelectionModel();


    var columnArr=[];
    columnArr.push(this.sm,{
            header: WtfGlobal.getLocaleText("acc.product.gridProductID"), // "Product ID",
            dataIndex: 'productid'
        },{
            header: this.isCust ? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.invoice.vendor"), // "Customer" : "Vendor",
            pdfwidth:150,
            dataIndex: 'affecteduser'
        },{
            header: this.isCust ? WtfGlobal.getLocaleText("acc.common.customer.code") : WtfGlobal.getLocaleText("acc.common.vendor.code"), // "Customer Code" : "Vendor Code",
            pdfwidth:150,
            dataIndex: 'affectedusercode'
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
            dataIndex: 'productName',
            pdfwidth:150,
            width:150,
            sortable: true,
            renderer: function(val,m,rec) {
                if(val){
                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<a class='hirarchical' wtf:qtip='" + val + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rec.data.productuuid + "\")'>" + val + "</a>";
                }
            }
        },{
            header: WtfGlobal.getLocaleText("acc.productList.gridProductDescription"), // "Product Description",
            dataIndex: 'productDesc',
            pdfwidth:150,
            width:150,
            sortable: true,
            renderer: function(val,m,rec) {
                if(val){
                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.productList.gridProductDescription") + "'>" + val + "</div>";
                }
            }
        },{
            header: WtfGlobal.getLocaleText("acc.rem.74"), // "Price Type",
            dataIndex: 'carryin',
            pdfwidth:150,
            width:150,
            sortable: true,
            renderer: this.CarryInRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridCurrency"), // "Currency",
            hidden: !Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
            dataIndex: 'currency',
            pdfwidth:150,
            width:150,
            align: 'right'
        },{
            header: WtfGlobal.getLocaleText("acc.UOM.StockUOM"), // "Currency",
            hidden: !Wtf.account.companyAccountPref.productPriceinMultipleCurrency,
            dataIndex: 'stockoum',
            pdfwidth:150,
            width:150,
            align: 'right'
        },{
            header: WtfGlobal.getLocaleText("acc.rem.75"), // 'Price',
            dataIndex: 'price',
            pdfwidth:150,
            width:150,
            sortable: true,
            align:'right',
            renderer: function(v,m,rec){
                if(((this.isCust && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCust && !Wtf.dispalyUnitPriceAmountInPurchase))) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else{
                    if(Wtf.account.companyAccountPref.productPriceinMultipleCurrency){
                        return WtfGlobal.withoutCurrencyUnitPriceRenderer(v, m, rec);
                    } else{
                        //return WtfGlobal.currencyRenderer(v,m,rec)
                        return WtfGlobal.withoutCurrencyUnitPriceRenderer(v,false,rec);
                    }
                }
            }
        },{
            header: WtfGlobal.getLocaleText("acc.rem.73"), // "Date Modified",
            dataIndex: 'applydate',
            align: 'center',
            pdfwidth:150,
            width:150,
            sortable: true,
            renderer: WtfGlobal.onlyDateRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.masterprice.uomname"), // "uom",
            dataIndex: 'uomname',
            pdfwidth:150,
            width:150,
            align: 'right'
        });
        
    columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForReports[Wtf.Acc_Product_Master_ModuleId],true);
    this.gridcm = new Wtf.grid.ColumnModel(columnArr);
     this.bBarBtnArr=[];
    
    this.exportButtonProdPrice = new Wtf.exportButton({
        obj: this,
        id: 'generalpricelistxport12',
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //"Export Report details.",  
        params: {name: WtfGlobal.getLocaleText("acc.productList.pricetab")},
        menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
        get:Wtf.autoNum.VendorCustomerPriceListReport,
        filename: this.id+ " " +WtfGlobal.getLocaleText("acc.productList.pricetab") + "_v1",
        label: WtfGlobal.getLocaleText("acc.productList.pricetab")

    }),

    this.bBarBtnArr.push('-', this.exportButtonProdPrice);
    
    this.grid = new Wtf.ux.grid.MultiGroupingGrid({
        stripeRows: true,
        layout: 'fit',
        cls : 'colWrap',
        view: groupView,
        store: this.Store,
        cm: this.gridcm,
        tbar:this.tBarArSsecond,
        border: false,
        loadMask: true,
        sm: this.sm,
        viewConfig: {
            forceFit: true,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }

    });
    this.getMyConfig();
    this.setCustomColumnHidden();
    this.Store.on('load',this.hideMsg,this);
    this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
    this.Store.on('datachanged', this.handleStoreDataChanged, this);
    this.sm.on("selectionchange",this.enableDisableButtons,this);
    this.grid.on('render', function () {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    Wtf.account.PriceReportCustVen.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.PriceReportCustVen, Wtf.Panel, {
    hideMsg: function(store) {
        if (this.Store.getCount() == 0) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        Wtf.MessageBox.hide();
        /**
         * Reconfigure Column model based on grid config
         */
        var cm = this.grid.getColumnModel();
        this.grid.reconfigure(store, cm);
        this.quickPanelSearch.StorageChanged(store);
    },
    /**
     * Initially Set Hidden true for custom Column 
     */
    setCustomColumnHidden:function(){
      for (var k = 0; k < this.grid.colModel.config.length; k++) {
            if (this.grid.colModel.config[k].dataIndex.indexOf('Custom_') != -1) {
                this.grid.colModel.config[k].hidden = true;
            }
        }
    },
    /**
     * Get Config for Grid Column Model
     */
    getMyConfig: function() {
        WtfGlobal.getGridConfig (this.grid, Wtf.autoNum.VendorCustomerPriceReport, false, false);
        
        var statusForCrossLinkage = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
    },
    /**
     * Save Grid Config State
     */
    saveMyStateHandler: function(grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.autoNum.VendorCustomerPriceReport, grid.gridConfigId, false);
    },
    

    onRender: function(config) {
        
        Wtf.account.PriceReportCustVen.superclass.onRender.call(this,config);
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            autoScroll: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.tBarArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize(),
                        items: this.bBarBtnArr
                    })
                }]
        });
        this.add(this.leadpan);
//        this.add(this.grid);
        this.fetchStatement();

    },

    CarryInRenderer: function(carryIn) {
        var temptxt = "";
        if (carryIn == false) {
            temptxt = WtfGlobal.getLocaleText("acc.productList.gridSalesPrice"); // "Sale Price";
        } else {
            temptxt = WtfGlobal.getLocaleText("acc.productList.gridPurchasePrice"); // "Purchase Price";
        }
        return temptxt;
    },
    
    createTBar: function() {
        this.arrEDSingleS = []; // Enable/Disable button's indexes on single select
        this.arrEDMultiS = []; // Enable/Disable button's indexes on multi select
        this.tBarArr = [];
        this.tBarArSsecond = [];
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.priceReport.QuickSearchEmptyText"), // "Search by Product ID, Product Name...",
            width: 200,
            hidden: false,
            field: 'productid'
        });
        this.tBarArr.push(this.quickPanelSearch);
        
                this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: false,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        this.tBarArr.push(this.resetBttn);
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false)
        });
        this.tBarArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.tBarArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
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
            }, this.productTypeRec)
        });
        this.producttype = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.productType"),//'Product Type*',
            hiddenName: 'producttype',
            store: this.productTypeStore,
            disabledClass: "newtripcmbss",
            width: 150,
            valueField: 'id',
            displayField: 'name',
            forceSelection: true
        });
        this.productTypeStore.load();
        this.productTypeStore.on("load", function(store) {
            var record = new this.productRec({
                id: "All",
                name: "All"
            });
            this.producttype.store.insert(0, record);
            this.producttype.setValue("All");
        }, this);
        this.tBarArr.push('-', WtfGlobal.getLocaleText("acc.productList.gridProductType"), this.producttype);
        this.productRec = Wtf.data.Record.create ([
            {name: 'productid'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'producttype'}
        ]);
        
        this.productStore = new Wtf.data.Store({
//            url: "ACCProduct/getProductsForCombo.do",
            url:"ACCProductCMN/getProductsIdNameforCombo.do",
            baseParams: {
                mode: 22,
                excludeParent:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        var baseParamsforCombo = this.productStore.baseParams;
        var configforCombo = {
            multiSelect: true,
            listWidth: Wtf.ProductComboListWidth
        }
        this.productCombo =CommonERPComponent.createProductMultiselectPagingComboBox(200,300,30,this,baseParamsforCombo,configforCombo);
        /*Initial Value All*/
        var record = new this.productRec({
            productid: "All",
            pid: "",
            type: "",
            productname: "All"
        });
        this.productCombo.store.insert(0, record);
        this.productCombo.setValue("All");
        this.tBarArr.push('-', WtfGlobal.getLocaleText("acc.field.SelectProduct"), this.productCombo);
        
        this.productCombo.store.on("load", function(store) {
            WtfGlobal.resetAjaxTimeOut();
            var record = new this.productRec({
                productid: "All",
                pid: "",
                type: "",
                productname: "All"
            });
            this.productCombo.store.insert(0, record);
        }, this);
        
        this.productCombo.on('select',function(combo, productRec) {
            if (productRec.get('productid') == 'All') {
                combo.setValue("");
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(productRec.get('productid'));
            }
        }, this);
        
        this.custVenRec = Wtf.data.Record.create([
            {name:'accountname', mapping:'accname'},
            {name:'accountid', mapping:'accid'},
            {name:'acccode'}
        ]);
        this.custVenStore = new Wtf.data.Store({
            url: this.isCust? "ACCCustomer/getCustomersForCombo.do" : "ACCVendor/getVendorsForCombo.do",
            baseParams: {
                mode: 2,
                group: this.isCust? 10 : 13,
                deleted: false,
                nondeleted: true,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                combineData: this.isCust? 1 : -1 // Send For Seprate Request
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data"
            },this.custVenRec)
        });
        
        var custVenComboLabel = this.isCust? WtfGlobal.getLocaleText("acc.field.SelectCustomer") : WtfGlobal.getLocaleText("acc.field.SelectVendor");
        this.custVenMSComboconfig = {
            hiddenName: 'accountmulselectcombo',         
            store: this.custVenStore,
            valueField: 'accountid',
            hideLabel: false,
            hidden: false,
            displayField: 'accountname',
            emptyText: this.isCust? WtfGlobal.getLocaleText("acc.inv.cus") : WtfGlobal.getLocaleText("acc.inv.ven"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            triggerAction: 'all',
            scope: this
        };
        
        this.custVenCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            forceSelection: true,   
            extraFields: Wtf.account.companyAccountPref.accountsWithCode?['acccode'] : [],
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            listWidth: Wtf.account.companyAccountPref.accountsWithCode?350:250,
            width: 150
        },this.custVenMSComboconfig));
            
        this.custVenStore.on("load", function(store) {
            var storeNewRecord = new this.custVenRec({
                accountname: 'All',
                accountid: 'All',
                acccode: ''
            });
            this.custVenCombo.store.insert( 0, storeNewRecord);
            this.custVenCombo.setValue("All");
        },this);         
        this.custVenStore.load();
        
        this.custVenCombo.on('select',function(combo, rec) {
            if (rec.get('accountid') == 'All') {
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {
                combo.clearValue();
                combo.setValue(rec.get('accountid'));
            }
        }, this);
        this.tBarArr.push('-', (custVenComboLabel + "(s) : "), this.custVenCombo);
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
         this.ValiditydateFilterStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data: [[0, WtfGlobal.getLocaleText("acc.customerList.gridCreationDate")], [1, WtfGlobal.getLocaleText("acc.pricelist.ValidityDate")]]
        });
        this.ValiditydateFilter = new Wtf.form.ComboBox({
            store: this.ValiditydateFilterStore,
            name: 'datefilter',
            displayField: 'name',
            value: 0,
            width: 100,
            valueField: 'id',
            mode: 'local',
            triggerAction: 'all'
        });
        this.tBarArSsecond.push(WtfGlobal.getLocaleText("acc.rem.72")+" "+WtfGlobal.getLocaleText("acc.field.On"),'&nbsp;',this.ValiditydateFilter);
        this.tBarArSsecond.push('-', this.fetchBttn);
         
//        this.addPrice = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.TDSMasterRates.Add"), //'Add',
//            id: 'addPrice' + this.id,
//            scope:this,
//            tooltip: WtfGlobal.getLocaleText("acc.productList.addNewPriceTT"), //{text:"Click here to add new price (Purchase Price & Sales Price) by selecting an available product.",dtext:"Select a product to add price.", etext:"Add price to the selected product."},
//            iconCls: getButtonIconCls(Wtf.etype.add),
//            handler: this.handleEditPrice.createDelegate(this, [false])
//        });
//        if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice)) {
//            this.tBarArr.push('-', this.addPrice);
//        }
        this.editPrice = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.edit"), // "Edit",
            tooltip : WtfGlobal.getLocaleText("acc.common.editTT"), // "Edit Selected Record.",
            id: 'editPrice' + this.id,
            scope: this,
            iconCls : getButtonIconCls(Wtf.etype.edit),
            disabled: true,
//            handler: this.handleEditPrice.createDelegate(this, [true])
            handler: function(){
                if(!Wtf.dispalyUnitPriceAmountInSales || !Wtf.dispalyUnitPriceAmountInPurchase){
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.pricelist.editAlert"));
                }else{
                    var handleEdit=this.handleEditPrice.createDelegate(this, [true]);
                    handleEdit();
                }
            }
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.editprice)) {
            this.tBarArSsecond.push('&nbsp;','-', this.editPrice);
            this.arrEDSingleS.push(this.tBarArSsecond.length - 1);
        }

        this.deletePrice = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.delete"), // "Delete",
            tooltip : WtfGlobal.getLocaleText("acc.fixed.asset.delete.selected"), // "Delete Selected Record(s)",
            id: 'deletePrice' + this.id,
            scope: this,
            iconCls : getButtonIconCls(Wtf.etype.deletebutton),
            disabled: true,
            handler: this.handleDeletePrice
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.deleteprice)) {
            this.tBarArSsecond.push('&nbsp;','-', this.deletePrice);
            this.arrEDMultiS.push(this.tBarArSsecond.length - 1);
        }
    },
    
    handleStoreBeforeLoad: function(store, options) {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.productid = this.productCombo.getValue();
        currentBaseParams.affectuserid = this.custVenCombo.getValue();
        currentBaseParams.ss= this.quickPanelSearch.getValue();
        currentBaseParams.isvaliditydate = this.ValiditydateFilter ? (this.ValiditydateFilter.getValue()==0 ? false : true) : false ;
        var sortInfo;
        if (store.sortInfo instanceof Array) {
            sortInfo = store.sortInfo[0];
            currentBaseParams.field = sortInfo.field;
            currentBaseParams.direction = sortInfo.direction;
        } else if (store.sortInfo instanceof Object) {
            sortInfo = store.sortInfo;
            currentBaseParams.field = sortInfo.field;
            currentBaseParams.direction = sortInfo.direction;
        }
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.producttype = this.producttype.getValue();
        this.Store.baseParams = currentBaseParams;
        this.exportButtonProdPrice.params = currentBaseParams;
        this.exportButtonProdPrice.setParams({
            productid :this.productCombo.getValue(),
            affectuserid : this.custVenCombo.getValue(),
            ss: this.quickPanelSearch.getValue()==undefined?"":this.quickPanelSearch.getValue()
        });
    },
    getDates: function(start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    }, 
    handleStoreDataChanged: function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
        }
        this.startDate.setValue(this.getDates(true));
        this.endDate.setValue(this.getDates(false));
        this.fetchStatement();
    },
    
    fetchStatement: function() {
        var isvalidityDate = this.ValiditydateFilter ? (this.ValiditydateFilter.getValue()==0 ? false : true) : false ;
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                producttype:this.producttype.getValue(),
                isvaliditydate : isvalidityDate
            }
        });
    },
    
    enableDisableButtons: function() {
        WtfGlobal.enableDisableBtnArr(this.tBarArSsecond, this.grid, this.arrEDSingleS, this.arrEDMultiS);
    },
    
    handleEditPrice: function(isEdit) {
        var priceRec = this.grid.getSelectionModel().getSelected();
        var pricePersonType = this.isCust ? "Customer" : "Vendor"
        callMasterPricelistWindow(pricePersonType, isEdit, priceRec); // call the function for mass update of price
        Wtf.getCmp("pricewindow").on('update',function() {
            (function() {
                this.Store.load();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        },this);
    },
    
    handleDeletePrice: function() {
        if (this.grid.getSelectionModel().hasSelection() == false) {
            WtfComMsgBox(34,2);
            return;
        }
        var data = [];
        var arr = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid, this.recArr, true, 0, 2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.delete.selected.price.entry"), function(btn) {
            if (btn != "yes") {
                for (var i=0; i<this.recArr.length; i++) {
                    var ind = this.Store.indexOf(this.recArr[i])
                    var num = ind%2;
                    WtfGlobal.highLightRowColor(this.grid, this.recArr[i], false, num, 2, true);
                }
                return;
            }
            for (i=0; i<this.recArr.length; i++) {
                arr.push(this.Store.indexOf(this.recArr[i]));
            }
            data = WtfGlobal.getJSONArray(this.grid, true, arr);
            
            var idData = "";
            for (var i=0; i<this.recArr.length; i++) {
                var rec = this.recArr[i];
                idData += "{\"priceid\":\""+rec.get('priceid')+"\"},";
            }
            if (idData.length > 1) {
                idData = idData.substring(0, idData.length-1);
            }
            data = "[" + idData + "]";
            
            Wtf.Ajax.requestEx({
                url: "ACCProduct/deletePriceList.do",
                params: {
                    data: data
                }
            },this, this.genDeleteSuccessResponse, this.genDeleteFailureResponse);
        },this);
    },
    
    genDeleteSuccessResponse: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.pricingBandMaster"),response.msg],response.success*2+1);
        for (var i=0; i<this.recArr.length; i++) {
            var ind = this.Store.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i], false, num, 2, true);
        }
        if (response.success) {
            (function() {
                this.Store.load();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }
    },
    
    genDeleteFailureResponse: function(response) {
        for (var i=0; i<this.recArr.length; i++) {
            var ind = this.Store.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.grid, this.recArr[i], false, num, 2, true);
        }
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg],2);
    }
});





//<-- ---------------------------------------------Custom Field History Tab ------------------------------------------->



Wtf.account.customFieldHistoryReportForProduct=function(config){
    this.fieldId = "";
    this.Record = new Wtf.data.Record.create([{
        name: 'fieldLabel'
    },{
        name: 'fieldValue'
    },{
        name: 'applyDate',
        type:'date'
    },{
        name: 'creationDate',
        type:'date'
    },{
        name: 'creator'
    }]);

    this.Store = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.Record),
        url: "ACCProduct/getCustomFieldHistoryForProduct.do",
        baseParams:{
            productId:config.productId,
            fieldId:this.fieldId
        }
    });
    
    var tbarArray = [];
    this.customFieldComboStoreRec = new Wtf.data.Record.create ([
            {
                name:'fieldtype'
            },{
                name:'refcolumn_number'
            },{
                name: 'notificationdays'
            },{
                name: 'validationtype'
            },{
                name: 'comboname'
            },{
                name: 'fieldid'
            },{
                name: 'column_number'
            },{
                name:'moduleflag'
            },{
                name:'comboid'
            },{
                name:'isessential'
            },{
                name:'fieldname'
            },{
                name:'fieldlabel'
            },{
                name:'maxlength'
            },{
                name:'iscustomcolumn'
            },{
                name:'sendnotification'
            },{
                name:'iseditable'
            },{
                name:'moduleid'
            },{
                name:'iscustomfield'
            }
        ]);
        
    this.customFieldComboStore =  new Wtf.data.Store({
            url: "ACCAccountCMN/getFieldParams.do",
            baseParams:{
                moduleid: 30,
                isForProductCustomFieldHistoryCombo:true,
                isActivated:1
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },this.customFieldComboStoreRec)
        });
        
        this.customFieldComboStore.load();
        
        this.customFieldCombo = new Wtf.form.ComboBox({
            store : this.customFieldComboStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CustomFields*"),
            typeAhead: true,
            selectOnFocus:true,
            displayField:'fieldlabel',
            valueField : 'fieldid',
            triggerAction: 'all',
            emptyText : WtfGlobal.getLocaleText("acc.field.SelectaField"),
            mode:'local'
        });
        this.customFieldCombo.on('select',this.onSelection,this);
        tbarArray.push(this.customFieldCombo);
    
    //this.Store.load();
//    WtfComMsgBox(29,4,true);

    this.gridcm= new Wtf.grid.ColumnModel([{
        header: WtfGlobal.getLocaleText("acc.field.CustomFieldName"),
        dataIndex: 'fieldLabel'
    },{
        header :WtfGlobal.getLocaleText("acc.field.NewValue"),
        dataIndex: 'fieldValue',
        align:'center'
    },{
        header :WtfGlobal.getLocaleText("acc.field.ModifiedBy"),
        dataIndex: 'creator',
        align:'center'
    },{
        header: WtfGlobal.getLocaleText("acc.setupWizard.applyDate"),
        dataIndex: 'applyDate',
        align:'center',
        renderer: WtfGlobal.onlyDateRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.field.ModifiedOn"),
        dataIndex: 'creationDate',
        align:'center',
        renderer: WtfGlobal.onlyDateRenderer
    }]);
    this.grid= new Wtf.grid.GridPanel({
        stripeRows :true,
        layout:'fit',
        store: this.Store,
        cm: this.gridcm,
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        tbar:tbarArray
    });
    this.Store.on('load',this.hideMsg,this)
    this.Store.on('loadexception',this.hideMsg,this)
    Wtf.account.customFieldHistoryReportForProduct.superclass.constructor.call(this,config);
}

Wtf.extend( Wtf.account.customFieldHistoryReportForProduct,Wtf.Panel,{
     hideMsg: function(){
         if(this.Store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
         }
         Wtf.MessageBox.hide();
    },
    onRender:function(config){
        this.add(this.grid);
        Wtf.account.customFieldHistoryReportForProduct.superclass.onRender.call(this,config);
        
    },
    
    onSelection:function(combo,rec,index){
        this.fieldId = this.customFieldCombo.getValue();
        this.grid.getStore().baseParams.fieldId = this.fieldId;
        this.grid.getStore().load();
    }
    
});

