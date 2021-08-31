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
Wtf.ux.KWLGridView = function(config){
    Wtf.apply(this, config);
    Wtf.ux.KWLGridView.superclass.constructor.call(this);
};
Wtf.extend(Wtf.ux.KWLGridView, Wtf.grid.GridView,{
    initTemplates : function(){
        var ts = this.templates || {};
        if(!ts.master){
            ts.master = new Wtf.Template(
                '<div class="x-grid3" hidefocus="true">',
                '<div class="x-grid3-viewport">',
                '<div class="x-grid3-header"><div class="x-grid3-header-inner"><div class="x-grid3-header-offset">{header}</div></div><div class="x-clear"></div></div>',
                '<div class="x-grid3-scroller"><div class="x-grid3-body">{body}</div><a href="#" class="x-grid3-focus" tabIndex="-1"></a></div>',
                "</div>",
                '<div class="x-grid3-resize-marker"> </div>',
                '<div class="x-grid3-resize-proxy"> </div>',
                "</div>"
                );
        }

        if(!ts.header){
            ts.header = new Wtf.Template(
                '<table border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
                '<thead><tr class="x-grid3-hd-row">{cells}</tr></thead>',
                "</table>"
                );
        }

        if(!ts.hcell){
            ts.hcell = new Wtf.Template(
                '<td class="x-grid3-hd x-grid3-cell x-grid3-td-{id}" style="{style}"><div ' +
                'Wtf:qtip="{tip}" {attr} class="x-grid3-hd-inner x-grid3-hd-{id}" unselectable="on" style="{istyle}">', this.grid.enableHdMenu ? '<a class="x-grid3-hd-btn" href="#"></a>' : '',
                '{value}<img class="x-grid3-sort-icon" src="', Wtf.BLANK_IMAGE_URL, '" />',
                "</div></td>"
                );
        }

        if(!ts.body){
            ts.body = new Wtf.Template('{rows}');
        }

        if(!ts.row){
            ts.row = new Wtf.Template(
                '<div class="x-grid3-row {alt}" style="{tstyle}"><table class="x-grid3-row-table" border="0" cellspacing="0" cellpadding="0" style="{tstyle}">',
                '<tbody><tr>{cells}</tr>',
                (this.enableRowBody ? '<tr class="x-grid3-row-body-tr" style="{bodyStyle}"><td colspan="{cols}" class="x-grid3-body-cell" tabIndex="0" hidefocus="on"><div class="x-grid3-row-body">{body}</div></td></tr>' : ''),
                '</tbody></table></div>'
                );
        }

        if(!ts.cell){
            ts.cell = new Wtf.Template(
                '<td class="x-grid3-col x-grid3-cell x-grid3-td-{id} {css}" style="{style}" tabIndex="0" {cellAttr}>',
                '<div class="x-grid3-cell-inner x-grid3-col-{id}" unselectable="on" {attr}>{value}</div>',
                "</td>"
                );
        }

        for(var k in ts){
            var t = ts[k];
            if(t && typeof t.compile == 'function' && !t.compiled){
                t.disableFormats = true;
                t.compile();
            }
        }

        this.templates = ts;

        this.tdClass = 'x-grid3-cell';
        this.cellSelector = 'td.x-grid3-cell';
        this.hdCls = 'x-grid3-hd';
        this.rowSelector = 'div.x-grid3-row';
        this.colRe = new RegExp("x-grid3-td-([^\\s]+)", "");
    },
    // private
    renderHeaders : function(){
        var cm = this.cm, ts = this.templates;
        var ct = ts.hcell;

        var cb = [], sb = [], p = {};

        for(var i = 0, len = cm.getColumnCount(); i < len; i++){
            p.id = cm.getColumnId(i);
            p.value = cm.getColumnHeader(i) || "";
            p.style = this.getColumnStyle(i, true);
            p.tip = cm.config[i].tip;
            if(cm.config[i].align == 'right'){
                p.istyle = 'padding-right:16px';
            }
            cb[cb.length] = ct.apply(p);
        }
        return ts.header.apply({
            cells: cb.join(""),
            tstyle:'width:'+this.getTotalWidth()+';'
        });
    }
});


/*
 *Plugin to display title on collapsible bar
 **/
Wtf.ux.collapsedPanelTitlePlugin = function(){
    this.init = function(p) {
        if (p.collapsible){
            var r = p.region;
            if ((r == 'north') || (r == 'south')){
                p.on ('render', function(){
                    var ct = p.ownerCt;
                    ct.on ('afterlayout', function(){
                        if (ct.layout[r].collapsedEl){
                            p.collapsedTitleEl = ct.layout[r].collapsedEl.createChild ({
                                tag: 'div',
                                cls: 'x-panel-header x-unselectable ',
                                style: 'padding:0px 3px 4px 5px;',
                                html:"<div class='x-panel-header-text'>"+ p.collapsibletitle+"</div>"
                            });
                        }
                    }, false, {
                        single:true
                    });
                    p.on ('collapse', function(){
                        if (ct.layout[r].collapsedEl && !p.collapsedTitleEl){
                            p.collapsedTitleEl = ct.layout[r].collapsedEl.createChild ({
                                tag: 'div',
                                cls: 'x-panel-header x-unselectable x-panel-header-text',
                                style: 'padding:0px 3px 4px 5px;',
                                html: "<span>"+p.collapsibletitle+"</span>"
                            });
                        }
                    }, false, {
                        single:true
                    });
                });
            }
            else if ((r == 'east') || (r == 'west')){
                var html = "";
                if(p.id == 'navigationpanel'){
                    html = "<img id='quickview' src='../../images/quick-view.gif' />";
                }
                else if(p.id == 'qickviewimage'){
                    html = "<img id='quickview' src='../../images/view-image-link.gif' />";
                }
                p.on ('render', function(){
                    var ct = p.ownerCt;
                    ct.on ('afterlayout', function(){
                        if (ct.layout[r].collapsedEl){
                            p.collapsedTitleEl = ct.layout[r].collapsedEl.createChild ({
                                tag: 'div',
                                style: 'padding:15px 3px 4px 0px;border:0px;',
                                html:html
                            });
                            
                        }
                        Wtf.QuickTips.register({
                            target:  Wtf.get('wtf-gen60'),
                            trackMouse: true,
                            text: WtfGlobal.getLocaleText("acc.field.Clicktoexpand")
                        });
                        Wtf.QuickTips.enable();
                    }, false, {
                        single:true
                    });
                    p.on ('collapse', function(){
                        if (ct.layout[r].collapsedEl && !p.collapsedTitleEl){
                            p.collapsedTitleEl = ct.layout[r].collapsedEl.createChild ({
                                tag: 'div',
                                style: 'padding:15px 3px 4px 0px;border:0px;',
                                html:html
                            });
                            
                        }
                    }, false, {
                        single:true
                    });
                });
            }
        }
    };
}


//Creating the Product selection Window Component

Wtf.account.ProductSelectionWindow = function(config){
    this.id=config.id;
    this.isCustomer = config.isCustomer;
    this.moduleid=config.moduleid;
    this.parentCmpID=config.parentCmpID;
    this.heplmodeid=config.heplmodeid;
    this.butnArr=[];
    this.invoiceGrid=config.invoiceGrid;
    this.isConsignment =config.isConsignment;
    this.consignmentProductSelectionArray=config.consignmentProductSelectionArray;
    this.isStoreLocationEnable=config.isStoreLocationEnable;
    this.isJobWorkOrderReciever = config.isJobWorkOrderReciever;
    this.isJobWorkInReciever = config.isJobWorkInReciever;
    this.customerid = config.customerid,
    this.Currency=config.Currency,
    this.custWarehouse=config.custWarehouse,
    this.closeflag =config.closeflag 
    this.warehouseId=config.warehouseId;
    this.isFromInventorySide=(config.isFromInventorySide != undefined && config.isFromInventorySide != "" ) ? config.isFromInventorySide : false;
    this.isForAdvance=(config.isForAdvance != undefined && config.isForAdvance != "" ) ? config.isForAdvance : false;
    this.modulename=(config.modulename != undefined && config.modulename != "" ) ? config.modulename : "";
    this.isWastageApplicable=(config.isWastageApplicable != undefined && config.isWastageApplicable != "" ) ? config.isWastageApplicable : false;
    this.butnArr.push({
        text : WtfGlobal.getLocaleText("acc.common.add"),
        scope : this,
        handler : function() {
            //ERP-9780, ERP-9828, ERP-9830,ERP-9851, ERP-9855        
            if(this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId){                   //this is for consignment invoice product selection grid
                var recs=this.productgrid.getSelectionModel().getSelections();
                var bRow=this.invoiceGrid.getStore().getAt(this.invoiceGrid.getStore().getCount()-1)
                this.invoiceGrid.getStore().remove(bRow);
                this.invoiceGrid.getStore().add(recs);
                this.invoiceGrid.getStore().fireEvent('load',this);
                this.closeWin();
            }else if(this.isForAdvance==false){
                if(this.isFromInventorySide){
                    this.invoiceGrid.productComboStore=Wtf.productStore;
                }
                if(Wtf.account.companyAccountPref.productOptimizedFlag!=undefined) {
                    this.invoiceGrid.productComboStore.on("load", function() {
                        this.loadProductInGridOptimized();

                    }, this);
                        
                this.invoiceGrid.productComboStore.baseParams.query = "";
                    if(!this.isConsignment){
                        this.invoiceGrid.productComboStore.proxy.conn.url = "ACCProductCMN/getProductsForComboOptimised.do";
                    }
                    this.invoiceGrid.productComboStore.on('loadexception',function(){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasereselectproduct")], 2);
                    },this);
                    if (this.isIndividualProductPrice) {
                        this.invoiceGrid.productComboStore.load({
                            params : {
                                ids : this.productSelectionArray,
                                query : "",
                                affecteduser: this.affecteduser,
                                forCurrency:this.forCurrency,
                                currency: this.currency,
        //                        quantity: this.quantity,
                                transactiondate : this.transactiondate,
                                carryin : this.carryin,
                                getSOPOflag : this.getSOPOflag,
                                startdate : this.startdate,
                                enddate : this.enddate,
                                isIndividualProductPrice:this.isIndividualProductPrice,
                                skipRichTextArea:true
                            }
                        });
                    }else{
                        this.invoiceGrid.productComboStore.load({
                            params : {
                                ids : this.productSelectionArray,
                                query : ""
                            }
                        });
                    }
               } else {
                    if (this.moduleid == Wtf.Acc_Stock_Adjustment_ModuleId || this.moduleid == Wtf.Acc_Stock_Request_ModuleId || this.moduleid == Wtf.Inventory_ModuleId|| this.moduleid == Wtf.Acc_InterStore_ModuleId || this.moduleid == Wtf.Acc_InterLocation_ModuleId) {
                         
                        this.invoiceGrid.productComboStore.on("load", function () {
                            this.loadProductInGridOptimized();

                        }, this);
                        this.invoiceGrid.productComboStore.load({
                            params: {
                                ids: this.productSelectionArray,
                                query: ""
                            }
                        });
                        
                    } else {
                        this.loadProductInGridOptimized();
                    }
                }
                    
        }else if(this.isForAdvance==true){
            this.isSubmitBtnClicked=true;
            this.closeWin();
        }
    }
    }, {
        text : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope : this,
        handler : function() {
            this.closeWin();
        } 
    });
    
    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.ProductSelectionWindow.superclass.constructor.call(this, config);
    this.productgrid.on('cellclick',this.onCellClick, this);
}




Wtf.extend(Wtf.account.ProductSelectionWindow, Wtf.Window, {

    onRender: function(config){
     
     
        Wtf.account.ProductSelectionWindow.superclass.onRender.call(this, config);
        
        this.createDisplayGrid();
        this.add(this.productgrid);
        this.on("show", function() {
           this.productStoreGrid=this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId?this.consignmentProductStoreGrid:this.productStoreGrid                    // for consignment invoice we use simple store and for Product selection window we use Grouping Store
            this.productStoreGrid.on("beforeload", function(){
                var categoryid = this.typeEditor.getValue();
                if(categoryid=='All') {
                    categoryid='All'
                } else {
                    categoryid=categoryid;
                }
                this.productStoreGrid.baseParams = {
                    moduleid:this.moduleid, //ERP-9835
                    limit :this.pPageSizeObj.combo.value,
                    ss : this.localSearch.getValue(),
                    type:this.isJobWorkInReciever?Wtf.producttype.customerInventory:(this.isJobWorkOrderReciever?(this.isCustomer?Wtf.producttype.customerAssembly:Wtf.producttype.assembly):((Wtf.getCmp("isMaintenanceOrder"+this.heplmodeid+this.parentCmpID) && Wtf.getCmp("isMaintenanceOrder"+this.heplmodeid+this.parentCmpID).getValue())?(Wtf.getCmp("maintenanceNumberCombo"+this.heplmodeid+this.parentCmpID).getValue()!=""?Wtf.producttype.service:""):'')),// ERP-11098 [SJ]
                    categoryid:categoryid,
//                    ids:this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId?null:this.productList.getValue(),
                    excludeParent:true,
                    excludeProductIds:this.consignmentProductSelectionArray!=undefined?this.consignmentProductSelectionArray.toString():"",
                    isWastageApplicable:this.isWastageApplicable,
                    warehouseid:this.warehouseId ? this.warehouseId : this.storeCmb.getValue(),
                    locationid : this.locCmb.getValue(),
                    isStoreLocationEnable:this.isStoreLocationEnable,
                    isConsignment:this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId,
                    customerid : this.customerid,
                    Currency:this.Currency,
                    custWarehouse:this.custWarehouse,
                    closeflag :this.closeflag 
                }
                if(this.pPageSizeObj!=undefined){
                    if(this.pPageSizeObj.combo.value=="All"){
                        var count = this.productStoreGrid.getTotalCount();
                        var rem = count % 5;
                        if(rem == 0){
                            count = count;
                        }else{
                            count = count + (5 - rem);
                        }
                        this.productStoreGrid.baseParams.limit = count;
                    }
                }
            }, this); 
        
            this.productStoreGrid.load({
                params : {
                    start : 0,
                    limit :this.pPageSizeObj.combo.value
                }
            },this);
        
        },this);
        
        
        
        if(this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId){
            this.consignmentProductStoreGrid.on("load", function() {
                var i=0;
                for(i=0; i<this.productSelectionArray.length; i++) {
                
                    var index = this.productStoreGrid.find('productid',this.productSelectionArray[i]);
                    if(index != -1) {
                        this.smProductGrid.selectRow(index, true);
                    }
                }
            }, this);
        }else{
        this.productStoreGrid.on("load", function() {
                var i=0;
                for(i=0; i<this.productSelectionArray.length; i++) {
                
                    var index = this.productStoreGrid.find('productid',this.productSelectionArray[i]);
                    if(index != -1) {
                        this.smProductGrid.selectRow(index, true);
                    }
                }
            }, this);
        }
        
        
        this.smProductGrid.on("rowselect", function(selectionModel, rowIndex, record) {
            //var productid = record.data.pid;
            var productid = record.data.productid;
           
            var i = 0;
            for(i=0; i<this.productSelectionArray.length; i++){
                if(productid == this.productSelectionArray[i]) {
                    break;
                }
            }
            
            if(i==this.productSelectionArray.length) {
                this.productSelectionArray.push(record.data.productid);
                this.productSelectionIdArray.push(record.data.pid);
                if (this.isFromInventorySide) {
                    this.productSelectionDescArray.push(record.data.desc);
                    this.productSelectionNameArray.push(record.data.productname);
                    this.productSelectionUOMNameArray.push(record.data.uomname);
                    this.productSelectionUOMIdArray.push(record.data.uomid);
                    this.productSelectionPackagingArray.push(record.data.packaging);
                    this.productSelectionPackagingIdArray.push(record.data.packagingid);
                    this.productSelectionWarehouseArray.push(record.data.warehouse);
                    this.isBatch.push(record.data.isBatchForProduct);
                    this.isSerial.push(record.data.isSerialForProduct);
                    this.isSKU.push(record.data.isSKUForProduct);
                    this.isRow.push(record.data.isRowForProduct);
                    this.isRack.push(record.data.isRackForProduct);
                    this.isBin.push(record.data.isBinForProduct);
                    this.productOrderUOMNameArray.push(record.data.orderinguomname);
                    this.productOrderUOMIdArray.push(record.data.orderinguomid);
                    this.productTransferUOMNameArray.push(record.data.transferinguomname);
                    this.productTransferUOMIdArray.push(record.data.transferinguomid);
                    this.productOrderToStockUOMFactorArray.push(record.data.orderToStockUOMFactor);
                    this.productTransferToStockUOMFactorArray.push(record.data.transferToStockUOMFactor);
                    this.productIsMultiUoMArray.push(record.data.ismultipleuom);
                    this.productUoMSchemaTypeArray.push(record.data.uomschematype); 
                    this.productUnitPriceArray.push(record.data.purchaseprice); 
                }
            }
            
            
        }, this);
        
        this.smProductGrid.on("rowdeselect", function(selectionModel, rowIndex, record) {
            var productid = record.data.productid;
            var i = 0;
            for(i=0;i<this.productSelectionArray.length; i++){
                if(productid == this.productSelectionArray[i]) {
                    break;
                }
            }
            
            this.productSelectionArray.splice(i, 1);
            this.productSelectionIdArray.splice(i, 1);
            
            if (this.isFromInventorySide) {
                this.productSelectionDescArray.splice(i, 1);
                this.productSelectionNameArray.splice(i, 1);
                this.productSelectionUOMNameArray.splice(i, 1);
                this.productSelectionUOMIdArray.splice(i, 1);
                this.productSelectionPackagingArray.splice(i, 1);
                this.productSelectionPackagingIdArray.splice(i, 1);
                this.productSelectionWarehouseArray.splice(i, 1);
                this.isBatch.splice(i, 1);
                this.isSerial.splice(i, 1);
                this.isSKU.splice(i, 1);
                this.isRow.splice(i, 1);
                this.isRack.splice(i, 1);
                this.isBin.splice(i, 1);
                this.productOrderUOMNameArray.splice(i, 1);
                this.productOrderUOMIdArray.splice(i, 1);
                this.productTransferUOMNameArray.splice(i, 1);
                this.productTransferUOMIdArray.splice(i, 1);
                this.productOrderToStockUOMFactorArray.splice(i, 1);
                this.productTransferToStockUOMFactorArray.splice(i, 1);
                this.productIsMultiUoMArray.splice(i, 1);
                this.productUoMSchemaTypeArray.splice(i, 1); 
                this.productUnitPriceArray.splice(i, 1); 
            }
        }, this);
        
        this.smProductGrid.on('beforerowselect',this.beforeProductSelect,this);
        
        WtfGlobal.getGridConfig(this.productgrid, Wtf.Product_Selection_Window_Grid_Id + "_" + this.moduleid, false, false);
        
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.productgrid.on('statesave', this.saveGridStateHandler, this);
        }, this);
    },
    
    createDisplayGrid:function(){
        this.isProductLoad = true;
        this.productSelectionArray = [];
        this.productSelectionIdArray = [];
        
        //following array is used in inventory side module
        if (this.isFromInventorySide) {
            this.productSelectionDescArray = [];
            this.productSelectionNameArray = [];
            this.productSelectionUOMNameArray = [];
            this.productSelectionUOMIdArray = [];
            this.productSelectionPackagingArray = [];
            this.productSelectionPackagingIdArray = [];
            this.productSelectionWarehouseArray = [];
            this.isBatch = [];
            this.isSerial = [];
            this.isSKU = [];
            this.isRow = [];
            this.isRack = [];
            this.isBin = [];
            this.productOrderUOMNameArray = [];
            this.productOrderUOMIdArray = [];
            this.productTransferUOMNameArray = [];
            this.productTransferUOMIdArray = [];
            this.productOrderToStockUOMFactorArray = [];
            this.productTransferToStockUOMFactorArray = [];
            this.productIsMultiUoMArray = [];
            this.productUoMSchemaTypeArray = [];
            this.productUnitPriceArray = [];
        }
        this.productGridRec = Wtf.data.Record.create ([
        {
            name:'productid'
        },

        {
            name:'productname'
        },

        {
            name:'desc'
        },
        
        {
            name:'uomid'
        },
        {
            name:'uomname'
        },
        {
            name:"isBatchForProduct"
        },
        {
            name:"isSerialForProduct"
        },
        {
            name:"isRowForProduct"
        },
        {
            name:"isRackForProduct"
        },
        {
            name:"isBinForProduct"
        },
        {
            name:'packaging'
        },
        {
            name:'packagingid'
        },
        {
            name:'warehouse'
        },
        {
            name:'orderinguomname'
        },
        {
            name:'transferinguomname'
        },
        {
            name:'orderinguomid'
        },
        {
            name:'transferinguomid'
        },
        {
            name:'orderToStockUOMFactor'
        },
        {
            name:'transferToStockUOMFactor'
        },
        {
            name:'ismultipleuom'
        },
        {
            name:'uomschematype'
        },
        
        {
            name:'quantity'
        },
        
        {
            name:'baseuomquantity'
//            defValue:1.00
        },
        {
            name:'amount',
            defValue:0
        },

        {
            name:'purchaseprice'
        },

        {
            name:'saleprice'
        },

        {
            name: 'producttype'
        },

        {
            name: 'type'
        },

        {
            name:'initialsalesprice'
        },

        {
            name: 'initialquantity',
            mapping:'initialquantity'
        },

        {
            name: 'initialprice'
        },

        {
            name: 'pid'
        },
        {
            name: 'category'
        },
        {
            name:'temp'
        },
        {
            name:'currencysymbol'
        },
        {
            name:'rate'
        },{
            name:'uom'
        },
        {
            name:'isActiveItem'
        },
        {
            name:'venconsignuomquantity'
        },
        {
            name:'rowid',
            defValue:null
        },
            

        {
            name:'billid'
        },

        {
            name:'billno'
        },

        {
            name:'Cust_billno'
        },
            
            

        {
            name:'quantity'
        },

        {
            name:'baseuomquantity'
//            defValue:1.00
        },
            

        {
            name:'baseuomname'
        },
            

        {
            name:'stockuom'
        },

        {
            name:'caseuom'
        },

        {
            name:'inneruom'
        },

        {
            name:'caseuomvalue'
        },

        {
            name:'inneruomvalue'
        },

        {
            name:'baseuomrate',
            defValue:1.00
        },

        {
            name:'copyquantity',
            mapping:'quantity'
        },

        {
            name:'rate',
            defValue:0
        },

        {
            name:'rateinbase'
        },

        {
            name:'partamount',
            defValue:0
        },

        {
            name:'discamount'
        },

        {
            name:'discount'
        },

        {
            name:'discountispercent',
            defValue:1
        },

        {
            name:'prdiscount',
            defValue:0
        },

        {
            name:'invstore'
        },

        {
            name:'invlocation'
        },

        {
            name:'prtaxid'
        },

        {
            name:'prtaxname'
        },

        {
            name:'prtaxpercent',
            defValue:0
        },

        {
            name:'taxamount',
            defValue:0
        },

        {
            name:'amount',
            defValue:0
        },
        {
            name:'lineleveltermamount',
            defValue:0  
        },
        {
            name:'amountwithtax',
            defValue:0
        },

        {
            name:'amountwithouttax',
            defValue:0
        },// used this field for Invoice Terms - rate*qty-discount

        {
            name:'taxpercent'
        },

        {
            name:'remark'
        },

        {
            name:'transectionno'
        },

        {
            name:'remquantity'
        },

        {
            name:'remainingquantity'
        },

        {
            name:'oldcurrencyrate',
            defValue:1
        },

        {
            name: 'currencysymbol',
            defValue:this.symbol
            },

            {
            name: 'currencyrate',
            defValue:1
        },

        {
            name: 'externalcurrencyrate'
        },

        {
            name:'orignalamount'
        },

        {
            name:'typeid',
            defValue:0
        },

        {
            name:'isNewRecord',
            defValue:'1'
        },

        {
            name:'producttype'
        },

        {
            name:'permit'
        },

        {
            name:'linkto'
        },

        {
            name:'linkid'
        },

        {
            name:'linktype'
        },

        {
            name:'savedrowid'
        },

        {
            name:'originalTransactionRowid'
        },

        {
            name:'changedQuantity'
        },

        {
            name:'approvedcost'
        },

        {
            name:'approverremark'
        },

        {
            name:'customfield'
        },

        {
            name:'gridRemark'
        },

        {
            name:'productcustomfield'
        },

        {
            name:'accountId'
        },

        {
            name:'salesAccountId'
        },

        {
            name:'batchdetails'
        },

        {
            name:'discountAccountId'
        },

        {
            name:'rowTaxAmount'
        },

        {
            name:'type'
        },                        

        {
            name:'shelfLocation'
        },

        {
            name:'productcustomfield'
        },

        {
            name:'supplierpartnumber'
        },

        {
            name:'assetDetails'
        },

        {
            name:'profitLossAmt'
        },

        {
            name:'copybaseuomrate',
            mapping:'baseuomrate'
        },  //for handling inventory updation 

        {
            name:'pid'
        },

        {
            name: 'isLocationForProduct'
        },

        {
            name: 'isWarehouseForProduct'
        },

        {
            name: 'isSKUForProduct'
        },

        {
            name: 'isAsset'
        },

        {
            name: 'location'
        },

        {
            name: 'warehouse'
        },

        {
            name: 'srno', 
            isForSequence:true
        },

        {
            name:'hasAccess'
        },
        {
            name:"defaultlocqty"
        },
        {
            name:"deflocation"
        },
        {
            name:"hsncode"
        },
        {
            name: 'balancequantity'
        },
        {
          name:'lockquantity' 
        },
        {
            name:'rcmapplicable'
        },
        {
            name:'gstCurrencyRate',
            defValue:0.0
        },
        {
            name:'minorderingquantity',
            defValue:0.0
        },
        {
            name:'maxorderingquantity',
            defValue:0.0
        }              
        ]);
        var prodUrl="ACCProductCMN/getProductsForSelectionGrid.do";
        
        if(!this.isConsignment && (this.moduleid ==Wtf.Acc_Vendor_Invoice_ModuleId||this.moduleid ==Wtf.Acc_Invoice_ModuleId)){        
            prodUrl="ACCProductCMN/getProductsForSelectionGridOptimised.do";
        }
        
        this.productStoreGrid = new Wtf.data.GroupingStore({
            //    url:Wtf.req.account+'CompanyManager.jsp',
            url:prodUrl,
            baseParams:{
                common:'1'
                              
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            },this.productGridRec),
            groupField:"category",
            sortInfo: {
                field: 'temp',
                direction: "ASC"
            }
        });
//        if(this.moduleid==Wtf.Acc_Receive_Payment_ModuleId){
            this.LineLevelcolModelArray=GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId];
            WtfGlobal.updateStoreConfig(this.LineLevelcolModelArray, this.productStoreGrid);
//        }
            
        if(this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId){                  // for consignment invoice we use simple store and for Product selection window we use Grouping Store
            this.consignmentProductStoreGrid = new Wtf.data.Store({
                //    url:Wtf.req.account+'CompanyManager.jsp',
                url:"ACCInvoiceCMN/getAllUninvoicedConsignmentDetails.do",
            
               
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty: 'totalCount'
                },this.productGridRec),
                sortInfo: {
                    field: 'temp',
                    direction: "ASC"
                }
            });
        }
        
        
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
                excludeParent:true, 
                module_name : "PRODUCT_CATEGORY"
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        this.productList = new Wtf.common.Select(Wtf.apply({
            multiSelect:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.product.productName"), //"Product Name*" ,
            extraFields:['pid','type'],
            extraComparisionField:'pid',// type ahead search on product id as well.
            extraComparisionFieldArray:['pid','productname'], //search on both pid and name
            isProductCombo: true,
            listWidth:Wtf.ProductComboListWidth,
            forceSelection:true
        },{
            name:"productlist",
            id:"productlist",
            store: this.productStore,
            valueField:'productid',
            displayField:'productname',
            emptyText:WtfGlobal.getLocaleText("acc.prod.comboEmptytext"), //"Please Select Product",
            extraComparisionField:'pid',
            extraComparisionFieldArray:['pid','productname'], //search on both pid and name
            isProductCombo: true,
            anchor:'85%',
            mode: 'local',
            selectOnFocus:true,
            //            allowBlank:false,
            triggerAction:'all',
            typeAhead: true,
            scope:this
        }));
        this.smProductGrid = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: false
        });
        var ProductcolumnArr = [];
        
        ProductcolumnArr.push(
            this.smProductGrid,
            new Wtf.grid.RowNumberer({
                width:40
            }),
            {
                header: WtfGlobal.getLocaleText("acc.product.productid"),
                sortable:true,
                dataIndex: "pid",
                width:110
            },{
                header:WtfGlobal.getLocaleText("acc.field.ProductName"),
                sortable:true,
                dataIndex:"productname",
                width:150,
               renderer:function(val,m,rec) {     
                 return "<a class='jumplink' wtf:qtip='"+val+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+rec.data.productid+"\")'>"+val+"</a>";
               }
            },{
                header:"Product Description",
                sortable:true,
                dataIndex:"desc",
                width:150,
                renderer : function(val) {
                    val = val.replace(/(<([^>]+)>)/ig,"");
                    return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridProductDescription")+"'>"+val+"</div>";
                }
             })
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.viewpricelist)){   
                ProductcolumnArr.push({
                header:WtfGlobal.getLocaleText("acc.field.UnitPurchasePrice"),
                sortable:true,
                hidden: this.isCustomer || this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId ,
                dataIndex:"purchaseprice",
//                renderer:function(val,m,rec){
//                    val=(parseFloat(getRoundofValue(val)).toFixed(Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);              
//                    return'<div class="currency">'+val+'</div>';
//                },
               renderer: function(v,metadata,record) {
                   if(!Wtf.dispalyUnitPriceAmountInPurchase){
                       return Wtf.UpriceAndAmountDisplayValue;
                   } else{
                       return WtfGlobal.withCurrencyUnitPriceRenderer(v,false,record);
                   }
                },
                width:110
            },{
                header:WtfGlobal.getLocaleText("acc.field.UnitSalesPrice"),
                sortable:true,
                hidden: !this.isCustomer,
                dataIndex:"saleprice",
//                renderer:function(val,m,rec){
//                    val=(parseFloat(getRoundofValue(val)).toFixed(Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);              
//                    return'<div class="currency">'+val+'</div>';
//                },
                renderer: function(v,metadata,record) {
                    if(!Wtf.dispalyUnitPriceAmountInSales){
                       return Wtf.UpriceAndAmountDisplayValue;
                    } else{
                       return WtfGlobal.withCurrencyUnitPriceRenderer(v,false,record);
                    }
                },
                width:110
            })}
         ProductcolumnArr.push({
                header:this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId? WtfGlobal.getLocaleText("acc.invoice.gridQty"):WtfGlobal.getLocaleText("acc.field.AvailableQuantity"),
                align:'right',
                dataIndex: "quantity",
                renderer:function(val,m,rec){
                    if(val!="NA"){
                    val=(parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                }
                    return'<div style="margin-right: 15px;">'+val+'</div>';
                },
                width:110
                
            },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLockQuantity"),//"BLock Quantity
            dataIndex:'lockquantity',
            align:'right',           
            //renderer:WtfGlobal.blockQtyrenderer,
            pdfwidth: 75
            
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridBalQty"),//"Balance Quantity",
            dataIndex:"balancequantity",
            align:'right',
            //renderer:WtfGlobal.balanceQtyrenderer,
            pdfwidth:75
        },{
                header: WtfGlobal.getLocaleText("acc.field.VendorAvailableQuantity"),
                align:'right',
                dataIndex: "venconsignuomquantity",
                //hidden:this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId, 
                hidden:Wtf.account.companyAccountPref.consignmentSalesManagementFlag || !(this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId)?false:true,
                renderer:function(val,m,rec){
                    if(val=="NA"){
                        return '<div style="margin-right: 15px;">'+val+'</div>';
                    }else{
                        val=(parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        return'<div style="margin-right: 15px;">'+val+'</div>';
                    }
            },
                width:110
                
            },
            {
                header:WtfGlobal.getLocaleText("acc.cust.Productcategory"),  //"Category",
                dataIndex:'category',
                hidden: true,
                fixed: true,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
                align:'center',
                hidden: !(this.isFromInventorySide || this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId), 
                dataIndex: this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId?"uom":"uomname"
            },{
                header: WtfGlobal.getLocaleText("acc.product.packaging"),
                align:'center',
                 hidden: !this.isFromInventorySide, 
                dataIndex: "packaging"
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase") ,
                align:'center',
                 hidden: !this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId, 
                dataIndex: "baseuomquantity",
                renderer:function(val,m,rec){
                    if(val=="NA"){
                        return '<div style="margin-right: 15px;">'+val+'</div>';
                    }else{
                        val=(parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        return'<div style="margin-right: 15px;">'+val+'</div>';
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),
                align:'center',
                 hidden: !(this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId), 
                dataIndex: "rate",
                renderer:WtfGlobal.withCurrencyUnitPriceRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridAmount"),
                align:'center',
                hidden: true, 
                dataIndex: "amount"
            }
             )
//     if(this.moduleid==Wtf.Acc_Receive_Payment_ModuleId){
         ProductcolumnArr = WtfGlobal.appendCustomColumn(ProductcolumnArr,this.LineLevelcolModelArray,true,undefined,undefined,undefined,Wtf.Acc_Product_Master_ModuleId);
//     }
        this.columnCm = new Wtf.grid.ColumnModel(ProductcolumnArr);
        this.localSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.productList.searchText"),//'Search by Product Name',
            width: 130,
            field: 'productname',
            Store:this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId?this.consignmentProductStoreGrid:this.productStoreGrid
        });
         
        this.productCategoryRecord = Wtf.data.Record.create ([
        {
            name:'id'
        },

        {
            name:'name'
        }
        ]);
        this.productCategoryStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productCategoryRecord)
        });
     
        this.typeEditor = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.Productcategory"),
            store: this.productCategoryStore,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true,
            width:100
        });
        this.productCategoryStore.load();
        this.productCategoryStore.on('load',this.setValue,this);
          
        this.storeCmbRecord = new Wtf.data.Record.create([
            {
                name: 'store_id'
            },
            {
                name: 'abbr'
            },
            {
                name: "fullname"
            },
            {
                name: 'description'
            }
        ]);

        this.storeCmbStore = new Wtf.data.Store({
            url: 'INVStore/getStoreList.do',
            baseParams: {
                isActive: true,
                byStoreManager: 'true',
                byStoreExecutive: 'true',
                includeQAAndRepairStore: true,
                includePickandPackStore: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, this.storeCmbRecord)
        });

        this.storeCmb = new Wtf.form.ComboBox({
            fieldLabel: 'Store*',
            hiddenName: 'storeid',
            store: this.storeCmbStore,
            typeAhead: true,
            displayField: 'fullname',
            valueField: 'store_id',
            mode: 'local',
            width: 125,
            triggerAction: 'all',
            emptyText: 'Select store...',
            listWidth: 300,
            tpl: new Wtf.XTemplate(
                    '<tpl for=".">',
                    '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                    '<div>{fullname}</div>',
                    '</div>',
                    '</tpl>')
                    // allowBlank:false
        });
        this.storeCmbStore.load();
        this.storeCmb.on("select", function () {
            this.locCmbStore.load({
                params: {
                    storeid: this.storeCmb.getValue()
                }
            })

        }, this);
        this.storeCmbStore.on("load", function (ds, rec, o) {
            var newRec = new this.storeCmbRecord({
                store_id: '',
                fullname: 'ALL'
            })
            this.storeCmbStore.insert(0, newRec);
            this.storeCmb.setValue('');
            this.storeCmb.fireEvent('select');
        }, this);
        this.locCmbRecord = new Wtf.data.Record.create([
            {
                name: 'id'
            },
            {
                name: 'name'
            }]);

        this.locCmbStore = new Wtf.data.Store({
            url: 'INVStore/getStoreLocations.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, this.locCmbRecord)
        });

        this.locCmbStore.on("beforeload", function (ds, rec, o) {
            this.locCmbStore.removeAll();
            this.locCmb.reset();
        }, this);


        this.locCmb = new Wtf.form.ComboBox({
            hiddenName: 'locationid',
            store: this.locCmbStore,
            typeAhead: true,
            displayField: 'name',
            valueField: 'id',
            mode: 'local',
            width: 125,
            triggerAction: 'all'
                    //emptyText:'Select location...',
                    //allowBlank:false
        });
        this.locCmbStore.on("load", function (ds, rec, o) {
            var newRec = new this.locCmbRecord({
                id: '',
                name: 'ALL'
            })
            this.locCmbStore.insert(0, newRec);
            this.locCmb.setValue('');
        }, this);
         var tbarArray= new Array();
         if(this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId){
             this.productStore.load();
             tbarArray.push(this.localSearch,"-",WtfGlobal.getLocaleText("acc.product.productName"),this.productList,{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                iconCls:'accountingbase fetch',
                scope:this,
                handler:this.loadTypeStore
            })
         } else {
            tbarArray.push(this.localSearch, "-", WtfGlobal.getLocaleText("acc.cust.Productcategory"), this.typeEditor);
            if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId ||
                    this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid === Wtf.Acc_Vendor_Quotation_ModuleId ||
                    this.moduleid == Wtf.Acc_Delivery_Order_ModuleId || this.moduleid === Wtf.Acc_Goods_Receipt_ModuleId || this.moduleid == Wtf.Acc_Sales_Return_ModuleId || this.moduleid == Wtf.Acc_Purchase_Return_ModuleId) {
                    tbarArray.push(WtfGlobal.getLocaleText("acc.jobworkin.create.Store"), this.storeCmb, WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"), this.locCmb);
            }
            tbarArray.push({
                xtype: 'button',
                text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
                iconCls: 'accountingbase fetch',
                scope: this,
                handler: this.loadTypeStore
            })
        }
        
        this.productgrid = new Wtf.grid.GridPanel({
            store: (this.isConsignment && this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId)?this.consignmentProductStoreGrid:this.productStoreGrid,
            sm:this.smProductGrid,
            cm: this.columnCm,
            border : false,
            loadMask : true,
            view: !(this.isConsignment && this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId)?new Wtf.grid.GroupingView({
                forceFit:false
            }):new Wtf.grid.GridView({
                
                forceFit:false
            }),
            layout : 'fit',
            modal : true,
            tbar : tbarArray,
            bbar: this.pag=new Wtf.PagingSearchToolbar({
                pageSize: 30,
                border : false,
                id : "paggintoolbar_ProductGrid"+this.id,
                store: this.productStoreGrid,
                searchField: this.localSearch,
                scope:this,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                id : "pPageSize_ProductGrid_"+this.id
                }),
                autoWidth : true,
                displayInfo:true//,
            }) 
        });
        
         
        
        
    }, 
    getAvailableQtyForProductInWarehouse: function (productId, warehouseId,rowIndexToSetRecInGrid,recordClicked, i) {
        if (warehouseId != undefined && warehouseId != "" && productId != undefined && productId != "") {
            Wtf.Ajax.requestEx({
                url: "INVStockLevel/getAvailableQtyByStoreProduct.do",
                params: {
                    fromStoreId: warehouseId,
                    productId: productId
                }
            }, this,
                    function (action, response) {
                        if (action.success == true) {
                            var availQty = action.data[0].availableQty;

                            recordClicked.set("avaquantity", availQty);
                        } else {
                            WtfComMsgBox(["Error", "Error occurred while fetching available quantity."], 0);
                            return 0;
                        }

                    },
                    function () {
                        WtfComMsgBox(["Error", "Error occurred while fetching available quantity."], 0);
                        return 0;
                    });
        } else {
            recordClicked.set("avaquantity", 0);
        }
        recordClicked.set("productid", productId);
        recordClicked.set("pid", this.productSelectionIdArray[i]);
        recordClicked.set("itemdescription", this.productSelectionDescArray[i]);
        recordClicked.set("desc", this.productSelectionDescArray[i]);
        recordClicked.set("productname", this.productSelectionNameArray[i]);
        recordClicked.set("uomname", this.productOrderUOMNameArray[i]);
        recordClicked.set("uomid", this.productOrderUOMIdArray[i]);
        recordClicked.set("packaging", this.productSelectionPackagingArray[i]);
        recordClicked.set("packagingid", this.productSelectionPackagingIdArray[i]);
        recordClicked.set("warehouse", warehouseId);
        recordClicked.set("confactor", 1);
        recordClicked.set("ismultipleuom", this.productIsMultiUoMArray[i]);
        recordClicked.set("uomschematype", this.productUoMSchemaTypeArray[i]);
        recordClicked.set("stockuomname", this.productSelectionUOMNameArray[i]);
        recordClicked.set("orderinguomname", this.productOrderUOMNameArray[i]);
        recordClicked.set("transferinguomname", this.productTransferUOMNameArray[i]);
        recordClicked.set("transferToStockUOMFactor", this.productTransferToStockUOMFactorArray[i]);
        recordClicked.set("orderToStockUOMFactor", this.productOrderToStockUOMFactorArray[i]);
        
        this.parentCmpID.checkAndRemoveDuplicateProductFromGrid(rowIndexToSetRecInGrid);
    },
    
    getDefaultLocationQty : function(productId, warehouseId,recordClicked, i){
         
            Wtf.Ajax.requestEx({
            url:"INVStockLevel/getStoreProductWiseDetailList.do",
            params: {
                storeId: warehouseId,
                productId: productId,
                defaultloc:true
            }
        },this,
        function(res,action){
            if(res.success==true){
                var defaultlocqty=res.data[0].defaultlocqty;
                var deflocation=res.data[0].deflocation;
                recordClicked.set("defaultlocqty", defaultlocqty);
                recordClicked.set("deflocation", deflocation);
            }else{
                WtfComMsgBox(["Error", "Error occurred while fetching data."],0);
                return;
            }
                
        },
        function() {
            WtfComMsgBox(["Error", "Error occurred while processing"],1);
        }
        ); 
    },
    setValue:function(){
        var record = new Wtf.data.Record({
            name:'All',
            id:'All'
        });
        var index=this.productCategoryStore.find('name','All');
        if(index==-1){
            this.productCategoryStore.insert(0,record);    
            this.typeEditor.setValue("All");
        }        
        
    },
    loadTypeStore:function(a,rec){
        var categoryid = this.typeEditor.getValue();
        if(categoryid=='All') {
            this.productStoreGrid.load({
                params: {
                    start : 0,
                    limit : this.pPageSizeObj.combo.value,
                    moduleid:this.moduleid, 
                    ss :this.localSearch.getValue(),
                    ids:this.productList.getValue(),
                    categoryid:categoryid
                }
            });
        }
        else {
            this.productStoreGrid.load({
                params:{
                    start : 0,
                    limit : this.pPageSizeObj.combo.value,
                    categoryid:categoryid,
                    ss :this.localSearch.getValue(),
                    ids:this.productList.getValue()
                }
            });
        }
    },
    loadProductInGridOptimized : function() {
        if(this.isProductLoad){
            for(var i=0; i< this.productSelectionArray.length; i++) {
                var rowindex = this.invoiceGrid.getStore().getCount() - 1;
                if(this.isFromInventorySide){
                    this.parentCmpID.addRec();
                }else{
                    this.invoiceGrid.addBlankRow();
                }
                
                var recordClicked = this.invoiceGrid.getStore().getAt(rowindex);
                var productId=this.productSelectionArray[i];
                
                if (this.isFromInventorySide) {
                    if (this.moduleid == Wtf.Acc_Stock_Request_ModuleId && this.modulename == "GOODS_REQUEST") {
                        var warehouseId = this.productSelectionWarehouseArray[i];
                        this.getAvailableQtyForProductInWarehouse(productId, warehouseId,rowindex,recordClicked, i); // rowindex is index of row of grid where to set data
                    } else {
                        recordClicked.set("productid", productId);
                        recordClicked.set("pid", this.productSelectionIdArray[i]);
                        recordClicked.set("isBatchForProduct", this.isBatch[i]);
                        recordClicked.set("isSerialForProduct", this.isSerial[i]);
                        recordClicked.set("isSKUForProduct", this.isSKU[i]);
                        recordClicked.set("isRowForProduct", this.isRow[i]);
                        recordClicked.set("isRackForProduct", this.isRack[i]);
                        recordClicked.set("isBinForProduct", this.isBin[i]);
                        recordClicked.set("packaging", this.productSelectionPackagingArray[i]);
                        recordClicked.set("packagingid", this.productSelectionPackagingIdArray[i]);
                        recordClicked.set("ismultipleuom", this.productIsMultiUoMArray[i]);
                        recordClicked.set("uomschematype", this.productUoMSchemaTypeArray[i]);
                        recordClicked.set("desc", this.productSelectionDescArray[i]);
                        recordClicked.set("amount", this.productUnitPriceArray[i]);
                        recordClicked.set("stockuomname", this.productSelectionUOMNameArray[i]);
                        recordClicked.set("orderinguomname", this.productOrderUOMNameArray[i]);
                        recordClicked.set("transferinguomname", this.productTransferUOMNameArray[i]);
                        recordClicked.set("transferToStockUOMFactor",this.productTransferToStockUOMFactorArray[i]);
                        recordClicked.set("orderToStockUOMFactor",this.productOrderToStockUOMFactorArray[i]);
                        recordClicked.set("desc", this.productSelectionNameArray[i]);
                        recordClicked.set("itemdescription", this.productSelectionDescArray[i]);
                        if (this.moduleid == Wtf.Acc_Stock_Adjustment_ModuleId && this.modulename == "STOCK_ADJUSTMENT") {
                            recordClicked.set("desc", this.productSelectionDescArray[i]);
                            recordClicked.set("productname", this.productSelectionNameArray[i]);
                            recordClicked.set("uomname", this.productSelectionUOMNameArray[i]);
                            recordClicked.set("uomid", this.productSelectionUOMIdArray[i]);
                        } else if (this.moduleid == Wtf.Acc_Stock_Request_ModuleId && this.modulename == "GOODS_ISSUE") {
                            recordClicked.set("itemdescription", this.productSelectionNameArray[i]);
                            recordClicked.set("desc", this.productSelectionDescArray[i]);
                            recordClicked.set("uom", this.productOrderUOMNameArray[i]);
                            recordClicked.set("uomname", this.productOrderUOMNameArray[i]);
                            recordClicked.set("uomid", this.productOrderUOMIdArray[i]);
                            recordClicked.set("confactor", 1);

                        } else if (this.moduleid == Wtf.Acc_InterStore_ModuleId || this.moduleid == Wtf.Acc_InterLocation_ModuleId) {
                            recordClicked.set("itemdescription", this.productSelectionNameArray[i]); //field for product name is 'itemdescription' in InterStore and InterLocation JS files hence assigning name to it
                            recordClicked.set("desc", this.productSelectionDescArray[i]);
                            recordClicked.set("uomname", this.productTransferUOMNameArray[i]);
                            recordClicked.set("uomid", this.productTransferUOMIdArray[i]);
                            recordClicked.set("confactor", 1);
                            if(!(this.isBatch[i] || this.isSerial[i])){
                                this.getDefaultLocationQty(productId, this.warehouseId,recordClicked,i);
                            }
                        }
                    }
                    if (this.moduleid != Wtf.Acc_Stock_Adjustment_ModuleId && this.modulename != "STOCK_ADJUSTMENT") {
                        this.parentCmpID.checkAndRemoveDuplicateProductFromGrid(rowindex);
                    }
                    
                } else {
                    recordClicked.set("productid", productId);
                    recordClicked.set("pid", this.productSelectionIdArray[i]);
                }
                var productComboStoreData = this.invoiceGrid.productComboStore.data.items[i].data;
                /**
                 * passing same record and its data by comparing
                 * clicked record and record in store. 
                 */
                for (var j = 0; j < this.invoiceGrid.productComboStore.data.items.length; j++) {
                    var individualProductPriceData = this.invoiceGrid.productComboStore.data.items[j].data;
                    if ((individualProductPriceData != undefined && individualProductPriceData != "") && (individualProductPriceData.individualproductprice != undefined  && individualProductPriceData.individualproductprice != "")) {
                        if (individualProductPriceData.individualproductprice[0].productid === recordClicked.data.productid) {
                            productComboStoreData = individualProductPriceData;
                            break;
                        }
                    }
                }
                var isAddProductsFromWindow = true;
                recordClicked.set("lineleveltermamount", 0);
                var obj = {};
                if(Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_Submit) {
                    obj = {
                        record : recordClicked,
                        field : 'pid',
                        value : this.productSelectionIdArray[i],
                        grid:this.invoiceGrid,
                        individualproductprice: productComboStoreData.individualproductprice,
                        discountData: productComboStoreData.discountData,
                        isAddProductsFromWindow: isAddProductsFromWindow
                    }
                } else {
                    obj = {
                        record : recordClicked,
                        field : 'productid',
                        value : this.productSelectionArray[i],
                        grid:this.invoiceGrid,
                        individualproductprice: productComboStoreData.individualproductprice,
                        discountData: productComboStoreData.discountData,
                        isAddProductsFromWindow: isAddProductsFromWindow
                    }
                }
                                
                //                            this.callupdateRowonProductLoad(obj);
                if(!this.isFromInventorySide){
                    this.invoiceGrid.updateRow(obj);
                }
                
                rowindex++;
            }
            //ERP-8199 : Code to reset all data after loading selected products in GRID
            this.productSelectionArray.splice(0, (this.productSelectionArray.length - 1));
            this.isProductLoad = false;
            this.closeWin();
        }
    },
    closeWin:function(){ /*this.fireEvent('update',this,this.value);*/
        this.close();
    },
    beforeProductSelect:function(sm,index){
        var rec = this.productStoreGrid.getAt(index);
        if(this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId){
            return true;
        }else{
            if(!rec.data.isActiveItem){
                return false;
            }
        }
    },

    onCellClick: function(g, i, j, e) {
        var el = e.getTarget("a");
        if (el == null) {
            return;
        }
        var header = g.getColumnModel().getDataIndex(j);
       /* When clicked on "Block Quantity" link*/
        if (header == "lockquantity") {
            this.viewTransection(g, i, e)
        }
    },
    viewTransection: function(grid, rowIndex, columnIndex) {
        if (this.productgrid.getSelections().length > 1) {
            return;
        }
        var formrec = null;
        if (rowIndex < 0 && this.productgrid.getStore().getAt(rowIndex) == undefined || this.productgrid.getStore().getAt(rowIndex) == null) {
            WtfComMsgBox(15, 2);
            return;
        }
        formrec = this.productgrid.getStore().getAt(rowIndex);
        var productid = formrec.get('productid');
       /* Closing window to show "Sales Order report by block quantity" tab */
        this.close();
        callSalesByProductAgainstSalesOrder(true, productid);
    },
    saveGridStateHandler: function (grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Product_Selection_Window_Grid_Id + "_" + this.moduleid, grid.gridConfigId, false);
    }
   
});

/* Window to open link Number*/

Wtf.account.PONumberSelectionWindow = function(config){
    this.id=config.id;
    this.moduleid="";
    this.Currency=config.Currency,
    this.inputValue=config.inputValue;
    if(config.inputValue=='5'&&this.moduleid==Wtf.Acc_Debit_Note_ModuleId){
    this.moduleid= Wtf.Acc_Debit_Note_ModuleId;   
    }else if(config.inputValue=='5'&&this.moduleid==Wtf.Acc_Credit_Note_ModuleId){
    this.moduleid= Wtf.Acc_Credit_Note_ModuleId;
    }else{
        this.moduleid= config.moduleid
    }
    this.PORec=config.PORec;
    this.doNotPopulateLineLevelData = config.doNotPopulateLineLevelData!=undefined?false:config.doNotPopulateLineLevelData; // do not populate invoice data
    this.butnArr=[];
    this.invoice=config.invoice;
    this.storeUrl=config.url;
    this.columnHeader=config.columnHeader?config.columnHeader:""; 
    this.storeBaseParams=config.storeBaseParams;
    this.storeParams=config.storeParams
    this.singleSelect=config.singleSelect
    this.fromLinkComboValue=config.fromLinkComboValue;
    this.butnArr.push({
        text :  WtfGlobal.getLocaleText("acc.common.add"),
        scope : this,
        handler : function() {
            var arr = this.PONumbersGrid.getSelectionModel().getSelections();
            var billids=[];
            // remove more 
            if (this.moduleid == Wtf.Acc_Lease_Contract) {
                var moreIndex = this.invoice.fromSO.store.findBy(
                    function (record, id) {
                        if (record.get('billid') === '-1') {
                            return true;  // a record with this data exists
                        }
                        return false;  // there is no record in the store with this data
                    });
                if (moreIndex != -1) {
                    var moreRecord = this.invoice.fromSO.store.getAt(moreIndex);
                    this.invoice.fromSO.store.remove(moreRecord);
                }
                for (var cnt = 0; cnt < arr.length; cnt++) {
                    var idx = this.invoice.fromSO.store.findBy(
                        function (record, id) {
                            if (record.get('billid') === arr[cnt].data.billid) {
                                return true;  // a record with this data exists
                            }
                            return false;  // there is no record in the store with this data
                        });
                    if (idx == -1) {
                        this.invoice.fromSO.store.insert(this.invoice.fromSO.store.getCount(), arr[cnt]);
                    }
                    billids.push(arr[cnt].data.billid);
                }
                if(Wtf.account.companyAccountPref.enableLinkToSelWin){
                    this.invoice.fromSO.store.insert(this.invoice.fromSO.store.getCount(), new this.PORec({
                        billno: "<a class='moreLink' href=#>More</a>",
                        billid: '-1'
                    }));
                }
                this.invoice.fromSO.setValue(billids);
                this.invoice.fromSO.fireEvent("select", this.invoice.fromSO);
            } else {
                var moreIndex = this.invoice.PO.store.findBy(
                    function (record, id) {
                        if (record.get('billid') === '-1') {
                            return true;  // a record with this data exists
                        }
                        return false;  // there is no record in the store with this data
                    });
                if (moreIndex != -1) {
                    var moreRecord = this.invoice.PO.store.getAt(moreIndex);
                    this.invoice.PO.store.remove(moreRecord);
                }
                for (var cnt = 0; cnt < arr.length; cnt++) {
                    var idx = this.invoice.PO.store.findBy(
                        function (record, id) {
                            if (record.get('billid') === arr[cnt].data.billid) {
                                return true;  // a record with this data exists
                            }
                            return false;  // there is no record in the store with this data
                        });
                    if (idx == -1) {
                        this.invoice.PO.store.insert(this.invoice.PO.store.getCount(), arr[cnt]);
                    }
                    billids.push(arr[cnt].data.billid);
                }
                if(Wtf.account.companyAccountPref.enableLinkToSelWin){
                    this.invoice.PO.store.insert(this.invoice.PO.store.getCount(), new this.PORec({
                        billno: "<a class='moreLink' href=#>More</a>",
                        billid: '-1'
                    }));
                }
                this.invoice.PO.setValue(billids);
                if(this.moduleid == Wtf.Acc_RFQ_ModuleId){
                    this.invoice.populateGridData();
                    }
                else if (!this.doNotPopulateLineLevelData) {
                    this.invoice.populateData();
                }
            }
           
            this.invoice.doLayout();
            this.closeWin();
        }
    }, {
        text :  WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope : this,
        handler : function() {
            this.closeWin();
        } 
    });
    
    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.PONumberSelectionWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.PONumberSelectionWindow, Wtf.Window, {

    onRender: function(config){
        Wtf.account.PONumberSelectionWindow.superclass.onRender.call(this, config);
        
        if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && this.fromLinkComboValue == 0 && Wtf.account.companyAccountPref.columnPref.advanceSearchInDocumentlinking) {
                var extraparam = this.storeParams;
                var panel = new Wtf.account.TransactionListPanel({
                    id: this.id + 'PONumbersSelectionWindowDO',
                    border: false,
                    isOrder: true,
                    consolidateFlag: false,
                    isCustomer: false,
                    reportbtnshwFlag: undefined,
                    moduleId: Wtf.Acc_Purchase_Order_ModuleId,
                    isCustBill: false,
                    linkedWithModuleId: Wtf.Acc_Vendor_Invoice_ModuleId,
                    pendingapproval: undefined,
                    outstandingreportflag: undefined,
                    person: extraparam.id,
                    isConsignment: undefined,
                    isMRPJOBWORKOUT: undefined,
                    isSecurityGateEntry: undefined,
                    isfromReportList: undefined, //true if outstanding report is clicked from Report List
                    extraFilters: extraparam,
                    label: "Purchase order",
                    helpmodeid: 17,
                    layout: 'fit',
                    closable: true,
                    searchJson: undefined,
                    filterConjuctionCrit: undefined,
                    isJobWorkOrderReciever: undefined,
                    isfromsearchwin: true,
                    closeflag: this.storeBaseParams.closeflag,
                    scope: this
                },this);
                this.PONumbersGrid = panel.grid;
                this.PONumbersStore = this.PONumbersGrid.getStore();
                this.add(panel);
        } else {
            this.createDisplayGrid();
            this.add(this.PONumbersGrid);
            this.PONumbersStore.on('beforeload', function () {
                WtfGlobal.setAjaxTimeOut();
                var currentBaseParams = this.PONumbersStore.baseParams;
                currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   //For UI Report  //ERP-8487
                currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                currentBaseParams.ss = this.localSearch.getValue();
            if(this.pPageSizeObj.combo!=undefined){
                if(this.pPageSizeObj.combo.value=="All"){                   // Sending the limit for 'All' by calculating the nearest value of total count in multiple of 5
                        var count = this.PONumbersStore.getTotalCount();
                        var rem = count % 5;
                    if(rem == 0){
                            count = count;
                    }else{
                            count = count + (5 - rem);
                        }
                        currentBaseParams.limit = count;
                    } else {
                        currentBaseParams.limit = this.pPageSizeObj.combo.value;
                    }
                }

            if(this.storeParams){                
                this.PONumbersStore.baseParams =this.extend(currentBaseParams,this.storeParams);  
                }
            else{
                this.PONumbersStore.baseParams =currentBaseParams;
                }

            }, this);
        this.PONumbersStore.on('load',function(){
                WtfGlobal.resetAjaxTimeOut();
        },this);
        this.PONumbersStore.on('loadexception',function(){
                WtfGlobal.resetAjaxTimeOut();
        },this);
            /*
             * When clicked on more option then Custom/Dimension value set in Product selection Grid Store because we are inserting selected record in Main POStore
             */
//        if (Wtf.account.companyAccountPref.enableLinkToSelWin) {
            var DimensionCustomFielsArray = GlobalDimensionCustomFieldModel[this.moduleid];
            if (DimensionCustomFielsArray) {
                for (var cnt = 0; cnt < DimensionCustomFielsArray.length; cnt++) {
                    if (DimensionCustomFielsArray[cnt] != undefined) {
                        var fieldname = DimensionCustomFielsArray[cnt].fieldname;
                        var newField = new Wtf.data.ExtField({
                            name: fieldname.replace(".", ""),
                            type: DimensionCustomFielsArray[cnt].fieldtype == 3 ? 'date' : (DimensionCustomFielsArray[cnt].fieldtype == 2 ? 'float' : 'auto'),
                            dateFormat: DimensionCustomFielsArray[cnt].fieldtype == 3 ? 'time' : undefined
                        });
                        this.PONumbersStore.fields.items.push(newField);
                        this.PONumbersStore.fields.map[fieldname] = newField;
                        this.PONumbersStore.fields.keys.push(fieldname);
                    }

                }
                this.PONumbersStore.reader = new Wtf.data.KwlJsonReader(this.PONumbersStore.reader.meta, this.PONumbersStore.fields.items);
            }
//        }

        this.on("show", function() {
            if(this.storeParams){
                this.storeParams.start=0;
                this.storeParams.limit=this.pPageSizeObj.combo.value;
                }
//            this.PONumbersStore.load(
//            {
//                params :this.storeParams?this.storeParams:{
//                    start:0,
//                    limit:this.pPageSizeObj.combo.value
//                }
//            },this);
        },this);
      }
    },
    createDisplayGrid:function(){
        this.productSelectionArray = [];
        
        this.PONumbersStore = new Wtf.data.Store({
            url:this.storeUrl,
            remoteSort:true,
            baseParams:{
                mode:this.storeBaseParams.mode,
                closeflag: this.storeBaseParams.closeflag,
                requestModuleid:this.storeBaseParams.requestModuleid,   
                isGrid:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PORec)
        });
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:this.singleSelect?this.singleSelect:false
        });
      
        var columnArr =[];
        this.expander = new Wtf.grid.RowExpander({});
        columnArr.push(this.sm, this.expander,{
            header: WtfGlobal.getLocaleText("acc.bankBook.transNo"),
            sortable: true,
            dataIndex: "billno"
        });
        if (!(this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId && this.invoice != undefined && this.invoice.fromLinkCombo != undefined && this.invoice.fromLinkCombo.getValue() == 5)) {
            columnArr.push({
                header: this.checkPerson() ? WtfGlobal.getLocaleText("acc.contractDetails.CustomerName"):WtfGlobal.getLocaleText("acc.ven.name"),
                sortable: true,
                hidden: this.moduleid == Wtf.Acc_RFQ_ModuleId ? true : false, 
                dataIndex: "personname"
            });
        }
        columnArr.push({
            header: this.columnHeader + " "+WtfGlobal.getLocaleText("acc.bankBook.date"),
            sortable: true,
            dataIndex: 'date',
            renderer: WtfGlobal.onlyDateRenderer
        }, {
            header: WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),
            dataIndex: 'amount',
            renderer: WtfGlobal.currencyRendererSymbol
        }, {
            header: WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome"),
            dataIndex: 'amountinbase',
            renderer: WtfGlobal.currencyDeletedRenderer
        });
        this.columnCm = new Wtf.grid.ColumnModel(columnArr);
        
        this.localSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.customerregistry.searchtext")+","+(this.invoice.isCustomer?WtfGlobal.getLocaleText("acc.contractDetails.CustomerName"): WtfGlobal.getLocaleText("acc.ven.name")),//'Search by Document Number, (Vendor Name or Customer Name'),
            width: 200,
            field: 'billno',
            Store:this.PONumbersStore
        });
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'stdate' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });

        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate' + this.id,
            value: WtfGlobal.getDates(false)
        });
        this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        this.expandRec = Wtf.data.Record.create ([
        {
            name:'productname'
        },

        {
            name:'productdetail'
        },

        {
            name:'prdiscount'
        },

        {
            name:'discountispercent'
        },

        {
            name:'amount'
        },

        {
            name:'productid'
        },

        {
            name:'partamount'
        },

        {
            name:'quantity'
        },

        {
            name:'unitname'
        },

        {
            name:'rate'
        },

        {
            name:'rateIncludingGst'
        },

        {
            name:'israteIncludingGst'
        },

        {
            name:'rowTaxAmount'
        },
        {
            name:'recTermAmount'
        },

        {
            name:'orderrate'
        },

        {
            name:'currencysymbol'
        },

        {
            name: 'pid'
        }
        ]);
        //    
        var url;
        var isVendorJobWorkOrder = false;
        if(this.inputValue!='5'){
            if(this.moduleid == Wtf.Acc_RFQ_ModuleId){
                    url = "ACCPurchaseOrderCMN/getRequisitionRows.do"; 
            }
            if(this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId){
                if(this.fromLinkComboValue == 5){
                    url = "ACCPurchaseOrderCMN/getRequisitionRows.do"; 
                }
            }else if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId){
                if(this.fromLinkComboValue == 2){
                    url ="ACCPurchaseOrderCMN/getQuotationRows.do";
                    
                }else if (this.fromLinkComboValue == 0) {
                    url = "ACCSalesOrderCMN/getSalesOrderRows.do";
                }else if (this.fromLinkComboValue == 5) {
                    url = "ACCPurchaseOrderCMN/getRequisitionRows.do";
                }
            }else if(this.moduleid==Wtf.Acc_Goods_Receipt_ModuleId){
                if(this.fromLinkComboValue == 0 ){
                    url = "ACCPurchaseOrderCMN/getPurchaseOrderRows.do"; 
                }else if(this.fromLinkComboValue == 1 ){
                    url ="ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
                }else if(this.fromLinkComboValue == 3 ){
                /*
                 * 3 is used for security gate entry in goods receipt
                 */
                    url = "ACCPurchaseOrderCMN/getSecurityGateEntryRows.do"; 
                }   
            }else if(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId){
                if(this.fromLinkComboValue == 0 ){
                    url = "ACCPurchaseOrderCMN/getPurchaseOrderRows.do"; 
                }else if(this.fromLinkComboValue == 1){
                    url ="ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
                }else if(this.fromLinkComboValue == 2){
                    url ="ACCPurchaseOrderCMN/getQuotationRows.do";
                }
            }else if(this.moduleid==Wtf.Acc_Sales_Order_ModuleId){
                if(this.fromLinkComboValue == 4 ){
                    url = "ACCPurchaseOrderCMN/getPurchaseOrderRows.do"; 
                }else if(this.fromLinkComboValue == 2){
                    url ="ACCSalesOrderCMN/getQuotationRows.do";
                }else if(this.fromLinkComboValue == 10){
                    url ="ACCContractMasterCMN/getMasterContractRows.do";
                }
            }else if(this.moduleid==Wtf.Acc_Delivery_Order_ModuleId){
                if(this.fromLinkComboValue == 0 ){
                    url = "ACCSalesOrderCMN/getSalesOrderRows.do";
                }else if(this.fromLinkComboValue == 1){
                    url = "ACCInvoiceCMN/getInvoiceRows.do";
                } else if (this.fromLinkComboValue == 2) {// Job Work In
                    url = "ACCSalesOrderCMN/getSalesOrderRows.do";
                    isVendorJobWorkOrder = true;
                }
            }else if(this.moduleid==Wtf.Acc_Invoice_ModuleId){
                if(this.fromLinkComboValue == 0 ){
                    url = "ACCSalesOrderCMN/getSalesOrderRows.do";
                }else if(this.fromLinkComboValue == 1 ){
                    url ="ACCInvoiceCMN/getDeliveryOrderRows.do";
                }else if(this.fromLinkComboValue == 2 ){
                    url ="ACCSalesOrderCMN/getQuotationRows.do";
                }
            }else if(this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId){
                url ="ACCPurchaseOrderCMN/getQuotationRows.do";
            }else if(this.moduleid==Wtf.Acc_Purchase_Return_ModuleId){
                if(this.fromLinkComboValue == 0 ){
                    url = "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";  ;
                }else if(this.fromLinkComboValue == 1){
                    url = "ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
                }
            }else if(this.moduleid==Wtf.Acc_Sales_Return_ModuleId){
                if(this.fromLinkComboValue == 0 ){
                    url = "ACCInvoiceCMN/getDeliveryOrderRows.do"  ;
                }else if(this.fromLinkComboValue == 1){
                    url = "ACCInvoiceCMN/getInvoiceRows.do";
                }
            }else if(this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId){
                url = "ACCPurchaseOrderCMN/getPurchaseOrderRows.do"; 
            }
        }else{
            if(this.moduleid== Wtf.Acc_Credit_Note_ModuleId){
                url = "ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
            }else{
                url = "ACCInvoiceCMN/getInvoiceRows.do";
            }
        }
        this.expandStore = new Wtf.data.Store({
            url:url,
            baseParams: {
                isVendorJobWorkOrder: isVendorJobWorkOrder
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.expandRec)
        }); 
    
        this.expandStore.on('load',this.fillExpanderBody,this);
        this.expander.on("expand",this.onRowexpand,this);
        this.resetBttn.on('click',this.handleResetClick,this);
        this.PONumbersGrid = new Wtf.grid.GridPanel({
            store: this.PONumbersStore,
            sm:this.sm,
            cm: this.columnCm,
            border : false,
            enableColumnHide:false,
            loadMask : true,
            plugins:this.expander,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec') + "<br>" +WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn')) 
            },
            layout : 'fit',
            tbar : [this.localSearch,'-',WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
                text : WtfGlobal.getLocaleText("acc.agedPay.fetch"),
                iconCls:'accountingbase fetch',
                scope : this,
                handler : this.loadData
            },'-',this.resetBttn],
            bbar: this.pag=new Wtf.PagingSearchToolbar({
                pageSize: 30,
                border : false,
                id : "paggintoolbar_PONumbersGrid_"+this.id,
                store: this.PONumbersStore,
                searchField: this.localSearch,
                scope:this,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_PONumbersGrid_"+this.id
                }),
                autoWidth : true,
                displayInfo:true//,
            })
        });
        this.PONumbersGrid.on('render',function(){
            this.PONumbersGrid.getView().refresh();
        },this)
    },
    onRowexpand:function(scope, record, body){
        var colModelArray = [];
        colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
        colModelArray = [];
        colModelArray = GlobalColumnModelForProduct[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
        this.expanderBody=body;
        this.expandStore.load({
            params:{
                bills:record.data.billid
            }
        });
    },
    fillExpanderBody:function(){
        var disHtml = "";
        var arr=[];
        var israteincludegst = this.expandStore.getCount()>0 && this.expandStore.getAt(0).data.israteIncludingGst ? this.expandStore.getAt(0).data.israteIncludingGst : false;
        arr=[(WtfGlobal.getLocaleText("acc.invoiceList.expand.PID")),//PID for Inventory
        (WtfGlobal.getLocaleText("acc.invoiceList.expand.pName")),//Product Details or Product Name
        (WtfGlobal.getLocaleText("acc.invoiceList.expand.qty")),//Quantity,
        (WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice")),//Unit Price
        ];
        if(!this.moduleid == Wtf.Acc_RFQ_ModuleId){
            arr.push(
                (WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc")),//Discount
                (WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"))//Tax
                );
        }
        arr.push((WtfGlobal.getLocaleText("acc.invoiceList.expand.amt")));//Amount
        
        var gridHeaderText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
        var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        count++; // from grid no
        var widthInPercent=100/count;
        var minWidth = count*100;
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var arrI=0;arrI<arr.length;arrI++){
            if(arr[arrI]!=undefined)
                header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[arrI] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";  
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
           
           
        for(var storeCount=0;storeCount<this.expandStore.getCount();storeCount++){
            var rec=this.expandStore.getAt(storeCount);
            var productname=this.withInvMode?rec.data['productdetail']: rec.data['productname'];

            //Column : S.No.
            header += "<span class='gridNo'>"+(storeCount+1)+".</span>";
            var pid=rec.data['pid'];
            header += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(pid,10)+"</span>"; 
            //Column : Product Name
            header += "<span class='gridRow'  wtf:qtip='"+productname+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(productname,10)+"</span>"; 
                
            //Quantity
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+parseFloat(getRoundofValue(rec.data['quantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+rec.data['unitname']+"</span>";
            //Unit Price
            var rate = rec.data.rate;
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.withCurrencyUnitPriceRenderer(rate,true,rec)+"</span>";
            //Discount
            if(!this.moduleid == Wtf.Acc_RFQ_ModuleId){
                if(rec.data.discountispercent == 0){
                    header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
                } else {
                    header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
                }
            
                //Tax
                var tax;
                if(rec.data['rowTaxAmount']==""){
                    tax=0;
                }else{
                    tax=rec.data['rowTaxAmount'];
                }
                if(rec.data['recTermAmount'] !="" && rec.data['recTermAmount'] != undefined ){
                    tax += rec.data['recTermAmount'];
                }
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(tax,rec.data['currencysymbol'],[true])+"</span>";
            }                               
            //Amount
            var  amount=0;
            amount=rec.data['quantity']*rate;
            if(rec.data['partamount'] != 0){
                amount = amount * (rec.data['partamount'] /100);
            }
                   
            var discount = 0;
            if(rec.data.prdiscount > 0) {
                if(rec.data.discountispercent == 0){
                    discount = rec.data.prdiscount;
                } else {
                    discount = (amount * rec.data.prdiscount) / 100;
                }
            }
       
            amount=(amount-discount);
            if(!israteincludegst) {
                amount+=rec.data['rowTaxAmount'];//(amount*rec.data['prtaxpercent']/100);
                if(rec.data['recTermAmount'] !="" && rec.data['recTermAmount'] != undefined ){
                    amount += rec.data['recTermAmount'];
                }
            }
            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true])+"</span>";
                 
            header +="<br>";
        }
        if(this.expandStore.getCount()==0){
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
            header += "<span class='headerRow'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</span>"
        }
        header += "</div>";
        disHtml += "<div class='expanderContainer1'style='margin-left: 7%;width:1200px' >" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    },
    handleResetClick:function(){
        if(this.localSearch.getValue()){
            this.localSearch.reset();
        }
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));
        this.loadData();
    },
    checkPerson:function(){
        var flag=true;
        if(this.invoice.isCustomer){
            if(this.columnHeader=="Purchase Order"){
                flag=false;
            }
        }else{
            if(this.columnHeader !="Sales Order"){
                flag=false;
            }
        }
        return flag;
    },
    loadData:function(){
        if (this.startDate.getValue() > this.endDate.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;
        }
        this.PONumbersStore.load({
            params: {
                start : 0,
                limit :this.pPageSizeObj.combo.value,
                ss : this.localSearch.getValue()
            }
        });
    },
    closeWin:function(){ /*this.fireEvent('update',this,this.value);*/
        this.close();
    },
    extend: function(obj, src) {
        for (var key in src) {
            if(key!='start' && key!='limit' && key!='startdate' && key!='enddate'&& key!='ss'){
                if (src.hasOwnProperty(key)) obj[key] = src[key];
            }
        }
        return obj;
        }
});

Wtf.account.SyncAllFromLMSWindow = function(config){
	
    
    this.dimension = new Wtf.form.Checkbox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.LMSDimension"), //Select Dimension
        name: 'selectDimension',
        cls: 'checkboxtopPosition',
        autoWidth: true
    //checked: Wtf.account.companyAccountPref.showVendorUpdate
    });
    this.product = new Wtf.form.Checkbox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.LMSProduct"), //Select Product
        name: 'selectProduct',
        cls: 'checkboxtopPosition',
        autoWidth: true
    // checked: Wtf.account.companyAccountPref.showCustomerUpdate
    });
    this.customer = new Wtf.form.Checkbox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.LMSCustomer"), //Select Customer
        name: 'selectCustomer',
        cls: 'checkboxtopPosition',
        autoWidth: true
    //checked: Wtf.account.companyAccountPref.showProductUpdate
    });
    this.invoice = new Wtf.form.Checkbox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.LMSInvoice"), //Select Invoice
        name: 'selectInvoice',
        cls: 'checkboxtopPosition',
        autoWidth: true
    //checked: Wtf.account.companyAccountPref.showProductUpdate
    });
    this.receipt = new Wtf.form.Checkbox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.LMSReceipt"), //Select Receipt
        name: 'selectReceipt',
        cls: 'checkboxtopPosition',
        autoWidth: true
    //checked: Wtf.account.companyAccountPref.showProductUpdate
    });
                
    Wtf.apply(this,{
        border:false,
        items: [{
            xtype: 'fieldset',
            autoHeight: true,
            title: WtfGlobal.getLocaleText("acc.field.SyncAllFromLMSFielsetSelect"), //Sync All From LMS
            title:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.SyncAllFromLMSFielsetSelect") + "'>" + WtfGlobal.getLocaleText("acc.field.SyncAllFromLMSFielsetSelect") + "</span>",
            defaults: {
                anchor: '50%',
                maxLength: 50,
                validator: this.validateFormat
            },
            items: [this.dimension, this.product, this.customer, this.invoice,this.receipt]
        }],
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.SynchfromLMS"),
            scope: this,
            handler: function () {
                this.syncAllFromLMS(this);
            }
        }, {
            text: WtfGlobal.getLocaleText("acc.msgbox.cancel"),
            scope: this,
            handler: function () {
                this.close();
            }
        }]
                
    },config);
	
    Wtf.account.SyncAllFromLMSWindow.superclass.constructor.call(this, config);
    
},

Wtf.extend(Wtf.account.SyncAllFromLMSWindow, Wtf.Window, {

    onRender: function(config) {
	
        Wtf.account.SyncAllFromLMSWindow.superclass.onRender.call(this, config);
    },
    syncAllFromLMS:function(rec){
        
        var record=rec;
        this.requestFlag=0;
        this.requestAllowFlag=true;
        this.deleted=false;
        this.nondeleted=false;
        if (record !== undefined && record !== '') {
            if (record.dimension.checked) {
                this.requestFlag++;
            }
            if (record.product.checked) {
                this.requestFlag++;
            } 
            if (record.customer.checked) {
                this.requestFlag++;
            } 
            if (record.invoice.checked) {
                this.requestFlag++;
            } 
            if (record.receipt.checked) {
                this.requestFlag++;
            } 
        }   
        
        
        switch (this.requestFlag) {
            case 1:if (this.product.checked || this.customer.checked || this.invoice.checked || this.receipt.checked) {
                this.requestAllowFlag = false;
            }
                    
            break;
            case 2:if (this.customer.checked || this.invoice.checked || this.receipt.checked) {
                this.requestAllowFlag = false;
            }
                    
            break;
            case 3:
                if (this.invoice.checked || this.receipt.checked) {
                    this.requestAllowFlag = false;
                }
                break;
            case 4:
                if (this.receipt.checked) {
                    this.requestAllowFlag = false;
                }
                break;
            case 5:this.requestAllowFlag=true;
                break;
             
            default:this.requestAllowFlag = false;
        } 
            
        if(this.requestAllowFlag){
                
            this.syncAllDataFromOtherProjects();
                
                
        }else{
                
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please Select Order as Dimension,Product,Customer,Invoice,Receipt etc.."], 2);
            this.requestAllowFlag=true;
        }
        
    },
    syncAllDataFromOtherProjects : function(){
        this.loadMask1 = new Wtf.LoadMask(this.id, {
            msg: WtfGlobal.getLocaleText("acc.msgbox.49"), 
            msgCls: "x-mask-loading acc-customer-form-mask"
        });
        this.loadMask1.show();
        var mapWithFieldTypeArr="2,3,4,5";
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:"CommonFunctions/SyncAllFromLMS.do",
            params: {
                mode:114,
                mapWithFieldType:mapWithFieldTypeArr,
                requestFlag:this.requestFlag,
                deleted:this.deleted,
                nondeleted: this.nondeleted
            }
        },this,function(response){
            WtfGlobal.resetAjaxTimeOut();
            this.loadMask1.hide();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],response.success*2+1);
            this.close();
        },function(){
            this.loadMask1.hide();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });  
    }

});

/*
 * Below component is used to Print the 'Auto Save and Print' Default Template
*/
Wtf.account.autoSaveAndPrintTemplateWin = function (config){
    this.invoiceParentObj=config.invoiceParentObj;
    
    /*
     *Create Print and Cancel Button 
    */
    this.cancelPrint = new Wtf.Button({
        id: 'cancelPrint_Btn',
        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler: function() {
            this.close();
        }
    });
    
    this.printButton = new Wtf.Button({
        id: 'printButton_Btn',
        text: WtfGlobal.getLocaleText("acc.common.print"),
        disabled:true,
        scope: this,
        handler: function() {
            this.printMultipleTemplates()
        }
    })
    this.buttons = [this.printButton,this.cancelPrint];
    
    /*
     * Create Grid to shwo the Default Templates
    */
    this.createTemplateGrid();
    Wtf.apply(this,config);
    Wtf.account.autoSaveAndPrintTemplateWin.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.autoSaveAndPrintTemplateWin, Wtf.Window,{
    initComponent:function (){
        Wtf.account.autoSaveAndPrintTemplateWin.superclass.initComponent.call(this);
    },
    onRender: function(config){
        Wtf.account.autoSaveAndPrintTemplateWin.superclass.onRender.call(this, config);
        this.add(this.grid);
    },
    printMultipleTemplates:function(){
        /*
         * Call to print the selcted Template in multiple tabs
         */
        WtfGlobal.callPrintMultipleTemplates(this.arrayOfTemplate,this.invoiceParentObj.parentObj);
        
    },
    createTemplateGrid:function(){
         /*
          * Get all Document Template as per Module Id  
         */ 
        this.templateArray=[];
        this.templateArray=GlobalCustomTemplateList[this.invoiceParentObj.parentObj.moduleid];
        this.finalArray=[];
        for (var i = 0; i < this.templateArray.length; i++) {
            var obj =this.templateArray[i];
            obj.moduleName = WtfGlobal.getModuleName(obj.moduleid);
            this.finalArray.push(obj);
        }
        
        if (this.invoiceParentObj.parentObj.isAutoCreateDO) {
            this.templateArray1=[];
            this.templateArray1=GlobalCustomTemplateList[Wtf.Acc_Delivery_Order_ModuleId];
            for (var i = 0; i < this.templateArray1.length; i++) {
                var obj1 = this.templateArray1[i];
                obj1.moduleName = WtfGlobal.getModuleName(obj1.moduleid);
                obj1.isDefaultTemp = (obj1.isdefaulttemplate==false?'Custom Template':'Default Template');
                this.finalArray.push(obj1);
            }
        }
        this.gridRec = new Wtf.data.Record.create ([
            {name:'moduleid'},
            {name:'moduleName'},
            {name:'templatesubtype'},
            {name:'templatename'},
            {name:'templateid'}
        ]);
        
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            root: "data"
        },this.gridRec);
        
        var templateRecords = {
            data : this.finalArray
        }
        
        this.gridStore = new Wtf.data.GroupingStore({
            groupField:'moduleName', 
            reader:this.gridStoreReader,
            sortInfo: {
                field: 'moduleName',
                direction: "DESC"
            }
        }); 
        
        this.gridStore.removeAll();
         /*
         * Load the template records in the store 
         */
        this.gridStore.loadData(templateRecords);
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            multiSelect:true
        });
        
        var templateGrid = [];
        templateGrid.push(this.sm,{
            header:WtfGlobal.getLocaleText("acc.designerTemplateName"),
            dataIndex:'templatename',
            sortable:true,
            align:'left'
        },{
            header: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName"),
            dataIndex:'moduleName',
            hidden:true,
            align:'left'
        },{
            header: '',
            dataIndex:'templateid',
            hidden:true,
            align:'left'
        });
        
        this.colModel = new Wtf.grid.ColumnModel(templateGrid);
        this.grid = new Wtf.grid.GridPanel({
            stripeRows :true,
            store:this.gridStore,
            cm:this.colModel,
            sm:this.sm,
            border:false,
            layout:'fit',
            view: new Wtf.grid.GroupingView({
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }),
            plugins :[Wtf.ux.grid.plugins.GroupCheckboxSelection]
        });
        
        this.sm.on("selectionchange",this.enableDisablePrintButton,this);
    },
    enableDisablePrintButton: function() {
        this.arrayOfTemplate = this.sm.getSelections();
        if (this.arrayOfTemplate.length == 0) {     // If no records are selected then keep print button disabled       
            this.printButton.disable();
        } else {
            this.printButton.enable();
        }
    }
});