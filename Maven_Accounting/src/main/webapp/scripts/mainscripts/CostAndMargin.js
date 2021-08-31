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

Wtf.account.costAndMargin = function(config) {
    
    this.gridStore = config.gridStore;
    this.productComboStore = config.productComboStore;
    this.exchangeRate = config.exchangeRate;
    this.currencySymbol = WtfGlobal.getCurrencySymbol();
    this.forCurrency = (config.parentObj != undefined && config.parentObj.Currency != undefined) ? config.parentObj.Currency.getValue() : WtfGlobal.getCurrencyID();
    this.totalproductQuntity = 0;
    this.totalproductAmount = 0;
    this.totalproductCost = 0;
    this.totalproductMargin = 0;
    this.totalprodcutMarginPercent = 0;
    this.finalproductQuntity = 0;
    this.finalproductAmount = 0;
    this.finalproductCost = 0;
    this.finalproductMargin = 0;
    this.finalproductMarginPercent = 0;
    
    this.createCostAndMarginGrid();
    this.productMarginTplSummary();
    
//========================================================= For Applying Config to the Window =================================================================
   
    Wtf.apply(this, {
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.msgbox.ok"), // 'OK',
            scope: this,
            handler: this.closeWin.createDelegate(this)
        }]
    }, config);
  
    Wtf.account.costAndMargin.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.costAndMargin, Wtf.Window, {
    
//========================================================== For On Rendering logic of Window ==================================================================        
    
    onRender: function(config) {
       
        Wtf.account.costAndMargin.superclass.onRender.call(this, config);
        
        var arr = [];
        var ids = [];
        this.gridStore.each(function(rec) {
            if (rec.data.productid !== undefined && rec.data.productid !== "" && rec.data.quantity !== undefined && rec.data.quantity !== "" && rec.data.rate  !== undefined && rec.data.rate !== "") {
                ids.push(rec.data.productid);
            }
        }, this);
        
        if (ids.length > 0) {
            this.productComboStore.load({
                params: {
                    forCurrency: this.forCurrency,
                    ids: ids
                }
            });
        }
        
        this.productComboStore.on("load", function() {
            var arr = [];
            this.gridStore.each(function(rec) {
                if (rec.data!==null && rec.data.productid !== undefined && rec.data.productid !== "" && rec.data.quantity !== undefined && rec.data.quantity !== "" && rec.data.rate !== undefined && rec.data.rate !== "") {
                    
                    var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, rec.data.productid, 'productid');
                
                    if (productComboRecIndex >= 0) {
                        var proRecord = this.productComboStore.getAt(productComboRecIndex);

                        var productCode = proRecord.data.pid;
                        var productname = proRecord.data.productname;
                        var desc = rec.data.desc;
                        var productQuantity = rec.data.quantity;
                        var amount = rec.data.rate * rec.data.quantity;
                        if( this.exchangeRate !== null && this.exchangeRate !== "" && this.exchangeRate !== 0 && this.exchangeRate !== 1){
                            amount = rec.data.rate * this.exchangeRate * rec.data.quantity;
                        }
                        var marginExchangeRate = rec.data.marginExchangeRate;
                        var cost = 0;
                        if (Wtf.account.companyAccountPref.activateProfitMargin && rec.data.vendorunitcost != "" && rec.data.vendorunitcost != undefined) {
                            cost = rec.data.totalcost;
                        } else if (this.parentObj && !this.parentObj.isEdit && ((this.parentObj.moduleid == Wtf.Acc_Sales_Order_ModuleId && this.parentObj.fromLinkCombo.getValue() == 4) || (this.parentObj.moduleid == Wtf.Acc_Customer_Quotation_ModuleId && this.parentObj.fromPO.getValue() == true))) { // for SO/CQ link to PO/VQ take PO/VQ Unit Price as Product Cost
                            cost = (rec.data.orderrate == 0 || rec.data.orderrate == "" || rec.data.orderrate == undefined)? 0 : rec.data.orderrate * rec.data.quantity;
                            cost = WtfGlobal.getOneCurrencyToOther(cost, marginExchangeRate, 0);
                        } else if (this.parentObj && (this.parentObj.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.parentObj.moduleid == Wtf.Acc_Invoice_ModuleId || this.parentObj.moduleid == Wtf.Acc_Customer_Quotation_ModuleId) && rec.data.marginCost != 0 && rec.data.marginCost != "" && rec.data.marginCost != undefined) { // for SO link to CQ linked to VQ take VQ Unit Price as Product Cost
                            cost = (rec.data.marginCost == 0 || rec.data.marginCost == "" || rec.data.marginCost == undefined)? 0 : rec.data.marginCost * rec.data.quantity;
                            cost = WtfGlobal.getOneCurrencyToOther(cost, marginExchangeRate, 0);
                        } else {
                            cost = (proRecord.data.purchaseprice == 0 || proRecord.data.purchaseprice == "" || proRecord.data.purchaseprice == undefined)? 0 : (proRecord.data.purchaseprice) * rec.data.quantity;
                        }
                        var margin = amount - cost;
                        var marginPercentage = 0;
                        if(amount !== 0){
                            marginPercentage = (amount != 0) ? ((margin / amount) * 100) : 100;
                        }else{
                            marginPercentage = "NA"
                        }

                        //calculate total margin amount for product excluding service type product
                        if (rec.data.type !== 'Service') {
                            this.totalproductQuntity = this.totalproductQuntity + productQuantity;
                            this.totalproductAmount = this.totalproductAmount + amount;
                            this.totalproductCost = this.totalproductCost + cost;
                            this.totalproductMargin = this.totalproductMargin + margin;
                        }
                        
                        //calculate total margin amount for product + service
                        this.finalproductQuntity = this.finalproductQuntity + productQuantity;
                        this.finalproductAmount = this.finalproductAmount + amount;
                        this.finalproductCost = this.finalproductCost + cost;
                        this.finalproductMargin = this.finalproductMargin + margin;
                        
                        var currencysymbol = WtfGlobal.getCurrencySymbol();
                        
//                        if (rec.data.currencysymbol != undefined) {
//                            this.currencySymbol = rec.data.currencysymbol;
//                        }
                        arr.push([productCode,productname,desc,productQuantity, amount, cost, margin, marginPercentage, currencysymbol]);
                    }
                }
            }, this);
            this.calculateMarginPercentage();
            this.costAndMarginGridStore.loadData(arr);

            this.costAndMarginGrid.reconfigure(this.costAndMarginGridStore,this.costAndMarginGridCM);
        }, this);
        

        this.costAndMarginGridStore.loadData(arr);
        
        this.add(this.costAndMarginGrid);
        this.add(this.productMarginTpl);
        
    },
    
//================================================================= For Creating Grid ============================================================================    
    
    createCostAndMarginGrid: function() {
        
        this.costAndMarginGridStore = new Wtf.data.SimpleStore({
            fields: ['productCode','productname','desc','productQuantity','amount','cost','margin','marginPercentage','currencysymbol'],
            data: []
        });
        
        this.createCostAndMarginGridCM();
        
        this.costAndMarginGrid = new Wtf.grid.GridPanel({
            store: this.costAndMarginGridStore,
            cm: this.costAndMarginGridCM,
            height:200,
            width: 975,
            border: false,
            loadMask: true,
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        
        this.costAndMarginGrid.on('render', function() {
            this.costAndMarginGrid.getView().refresh();
//            this.costAndMarginGrid.view.refresh.defer(1, this.costAndMarginGrid.view); /* alternate way to refresh the empty text of grid without load the store */
        },this);
    },
    
//============================================================= For Creating Grid Column Model  ======================================================================
    
    createCostAndMarginGridCM: function() {
        this.columnArr = [];
        
        this.columnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.ProductCode"),  // "Product Code",
            dataIndex: 'productCode',
            align: 'center',
            width: 150
        },{
           header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product",acc.productList.gridProduct
           dataIndex:'productname',
           align: 'center',
           width: 150
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),// Product Description",
            dataIndex:'desc',
            align: 'center',
            width: 150
         },{
            header: WtfGlobal.getLocaleText("acc.field.productQuantity"),  // "Product Quantity",
            dataIndex: 'productQuantity',
            align: 'right',
            width: 100,
            renderer: this.quantityRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"),  // "Amount",
            dataIndex: 'amount',
            align: 'right',
            width: 100,
            renderer: WtfGlobal.withCurrencyUnitPriceRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.assembly.cost"),  // "Cost",
            dataIndex: 'cost',
            align: 'right',
            width: 100,
            renderer: WtfGlobal.withCurrencyUnitPriceRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.field.margin"),  // "Margin",
            dataIndex: 'margin',
            align: 'right',
            width: 100,
            renderer: WtfGlobal.withCurrencyUnitPriceRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.field.marginPercentage"),  // "Margin %",
            dataIndex: 'marginPercentage',
            align: 'right',
            width: 100,
            renderer: function(v,m,rec) {
                if (v !== "NA") {
                    v = parseFloat(v).toFixed(2);
                    v = v + "%";
                }
                return '<div class="currency">' + v + '</div>';
            }
        });
        
        this.costAndMarginGridCM = new Wtf.grid.ColumnModel(this.columnArr);
    },
    productMarginTplSummary: function () {
        this.productMarginTplSummary = new Wtf.XTemplate(
            '<div style="padding: 10px; border: 1px solid rgb(153, 187, 232);">',
            '<div><hr class="templineview"></div>',
            '<div>',
            '<table width="100%">' +
            '<tr>' +
            '<td style="width:30%;" text-align=left></td>' +
            '<td style="width:7%; text-align:center"><b>' + WtfGlobal.getLocaleText("acc.field.productQuantity") + '</b></td>' +
            '<td style="width:7%; text-align:center"><b>' + WtfGlobal.getLocaleText("acc.invoice.gridAmount") + '</b></td>' +
            '<td style="width:7%; text-align:center"><b>' + WtfGlobal.getLocaleText("acc.assembly.cost") + '</b></td>' +
            '<td style="width:7%; text-align:center"><b>' + WtfGlobal.getLocaleText("acc.field.margin") + '</b></td>' +
            '<td style="width:7%; text-align:center"><b>' + WtfGlobal.getLocaleText("acc.field.marginPercentage") + '</b></td>' +
            '</tr>' +
            '<tr>' +
            '<td style="width:30%;" text-align=left><b>' + WtfGlobal.getLocaleText("acc.field.TotalMarginProductOnly") + ': </b></td>' +
            '<td style="width:7%; text-align:right">{totalproductQuntity}</td>' +
            '<td style="width:7%; text-align:right">{totalproductAmount}</td>' +
            '<td style="width:7%; text-align:right">{totalproductCost}</td>' +
            '<td style="width:7%; text-align:right">{totalproductMargin}</td>' +
            '<td style="width:7%; text-align:right">{totalprodcutMarginPercent}</td>' +
            '</tr>' +
            '</table>' +
            '</div>',
            '<div><hr class="templineview"></div>',
            '<div>',
            '<table width="100%">' +
            '<tr>' +
            '<td style="width:30%; text-align:left"><b>' + WtfGlobal.getLocaleText("acc.field.totalprofitmarginProductService") + ': </b></td>' +
            '<td style="width:7%; text-align:right">{finalproductQuntity}</td>' +
            '<td style="width:7%; text-align:right">{finalproductAmount}</td>' +
            '<td style="width:7%; text-align:right">{finalproductCost}</td>' +
            '<td style="width:7%; text-align:right">{finalproductMargin}</td>' +
            '<td style="width:7%; text-align:right">{finalproductMarginPercent}</td>' +
            '</tr>' +
            '</table>' +
            '</div>',
            '<div><hr class="templineview"></div>',
            '</div>'
        );

        this.productMarginTpl = new Wtf.Panel({
            id: 'productMarginTpl' + this.id,
            border: false,
            width: 973,
            baseCls: 'tempbackgroundview',
            html: this.productMarginTplSummary.apply({
                totalproductQuntity: this.quantityRenderer(0),
                totalproductAmount: this.currencySummaryRenderer(0),
                totalproductCost: this.currencySummaryRenderer(0),
                totalproductMargin: this.currencySummaryRenderer(0),
                totalprodcutMarginPercent: '<div class="currency">NA</div>',
                
                finalproductQuntity: this.quantityRenderer(0),
                finalproductAmount: this.currencySummaryRenderer(0),
                finalproductCost: this.currencySummaryRenderer(0),
                finalproductMargin: this.currencySummaryRenderer(0),
                finalproductMarginPercent:'<div class="currency">NA</div>'
            })
        });

    },
    calculateMarginPercentage: function () {
        //calculate overall margin percentage product excluding service product
        if (this.totalproductAmount !== 0) {
            this.totalproductMargin = this.totalproductAmount - this.totalproductCost;
            this.totalprodcutMarginPercent = (this.totalproductAmount !== 0) ? ((this.totalproductMargin / this.totalproductAmount) * 100) : 0;
            this.totalprodcutMarginPercent = parseFloat(this.totalprodcutMarginPercent).toFixed(2);
        } else {
            this.totalprodcutMarginPercent = "NA";
        }
        //calculate overall margin percentage
        if (this.finalproductAmount !== 0) {
            this.finalproductMargin = this.finalproductAmount - this.finalproductCost;
            this.finalproductMarginPercent = (this.finalproductAmount !== 0) ? ((this.finalproductMargin / this.finalproductAmount) * 100) : 0;
            this.finalproductMarginPercent = parseFloat(this.finalproductMarginPercent).toFixed(2);
        } else {
            this.finalproductMarginPercent = "NA";
        }
        this.UpdateMarginTpl();
    },
    UpdateMarginTpl: function () {
        this.productMarginTplSummary.overwrite(this.productMarginTpl.body, {
            totalproductQuntity:this.quantityRenderer(this.totalproductQuntity),
            totalproductAmount: this.currencySummaryRenderer(this.totalproductAmount),
            totalproductCost: this.currencySummaryRenderer(this.totalproductCost),
            totalproductMargin: this.currencySummaryRenderer(this.totalproductMargin),
            totalprodcutMarginPercent: this.totalproductAmount === 0 ? '<div class="currency">NA</div>' : '<div class="currency">' + this.totalprodcutMarginPercent + '%</div>',
            finalproductQuntity:this.quantityRenderer(this.finalproductQuntity),
            finalproductAmount: this.currencySummaryRenderer(this.finalproductAmount),
            finalproductCost: this.currencySummaryRenderer(this.finalproductCost),
            finalproductMargin: this.currencySummaryRenderer(this.finalproductMargin),
            finalproductMarginPercent: this.finalproductAmount === 0 ? '<div class="currency">NA</div>' : '<div class="currency">' + this.finalproductMarginPercent + '%</div>'
        });
    },
    quantityRenderer: function(val,m,rec) {
        if (val == "") {
            return val;
        } else {
            return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
    },
    
    quantitySummaryRenderer: function(val,m,rec) {
        var v = parseFloat(val);
        if (isNaN(v)) {
            return val;
        }
        return '<div class="grid-summary-common">' + parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + '</div>';
    },
    
    currencySummaryRenderer: function(val,m,rec) {
        var symbol;
        if (Wtf.getCmp("costAndMarginWindow") != undefined) {
            symbol = Wtf.getCmp("costAndMarginWindow").currencySymbol;
        } else {
            symbol = WtfGlobal.getCurrencySymbol();
        }
        var v = parseFloat(val);
        if (isNaN(v)) {
            return val;
        } 
        v = WtfGlobal.conventInDecimal(v,symbol);
        return WtfGlobal.summaryRenderer('<div class="currency">'+v+'</div>');
    },
    calCostInVendorCurrencyToTransactionCurrency: function (val) {
        if (this.exchangeRate !== 0) {
            var returnVal = (val * (1 / this.exchangeRate));
        }
        return returnVal;
    },
    
//================================================================= For Closing Window =============================================================================

    closeWin: function() {
        this.close();
    }
});
