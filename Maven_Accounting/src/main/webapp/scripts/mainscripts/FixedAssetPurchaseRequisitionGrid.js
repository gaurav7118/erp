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

Wtf.account.FixedAssetPurchaseRequisitionGrid = function(config) {
    this.isFixedAsset = (config.isFixedAsset)? config.isFixedAsset : false;
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset)? config.isLeaseFixedAsset : false;
    this.isLinkedFromReplacementNumber = (config.isLinkedFromReplacementNumber)? config.isLinkedFromReplacementNumber : false;
    this.isLinkedFromCustomerQuotation = (config.isLinkedFromCustomerQuotation)? config.isLinkedFromCustomerQuotation : false;
    this.parentCmpID = config.parentCmpID;
    this.isCustomer = config.isCustomer;
    this.productComboStore = (this.isFixedAsset)?Wtf.FixedAssetStore:(this.isCustomer?((this.isLeaseFixedAsset)?Wtf.FixedAssetAndProductLeaseStore:Wtf.productStoreSales):Wtf.productStore);
    this.currencyid = config.currencyid;
    this.productI = null;
    this.soLinkFlag = null;
    this.updaterowtax = true; // used in conjunction with this.soLinkFlag
    this.id = config.id;
    this.isOrder = config.isOrder;
    this.isFromGrORDO = (config.isFromGrORDO != null || config.isFromGrORDO != undefined)?config.isFromGrORDO:false;
    this.record = config.record;
    this.billDate = new Date();
    this.dateChange = false;
    this.pronamearr = [];
    this.isCashType = config.isCash;
    this.isInvoice = config.isInvoice;
    this.fromPO = config.fromPO;          
    this.linkedFromOtherTransactions = false; // if this transaction is being linked to any other transaction
    this.readOnly = config.readOnly;
    this.copyInv = config.copyInv;
    this.editTransaction = config.editTransaction;
    this.editLinkedTransactionQuantity = Wtf.account.companyAccountPref.editLinkedTransactionQuantity;
    this.editLinkedTransactionPrice = Wtf.account.companyAccountPref.editLinkedTransactionPrice;
    this.isEdit = config.isEdit;
    this.noteTemp = config.noteTemp;
    this.fromOrder = config.fromOrder;
    this.affecteduser = "";
    this.gridConfigId="";
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag
    
    if (config.isNote != undefined) {
        this.isNote = config.isNote;
    } else {
        this.isNote = false;
    }
    
    this.isCN = config.isCN;
    this.moduleid = config.moduleid;
    this.isViewCNDN = config.isViewCNDN;
    this.isQuotation = config.isQuotation;
    this.isRequisition = config.isRequisition;
    this.isQuotationFromPR = config.isQuotationFromPR;
    this.isRFQ = config.isRFQ;
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    if(colModelArray) {
        colModelArray.concat(GlobalColumnModelForProduct[this.moduleid]);
    }
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    
    this.duplicateStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },Wtf.productRec)
    });
    
    Wtf.account.FixedAssetPurchaseRequisitionGrid.superclass.constructor.call(this,config);
    
    this.addEvents({
        'datachanged':true, // Event fired when grid data is changed. Mostly used to calculate subtotal
        'pricestoreload': true,
        'productselect': true, // Event fired to load data for collapsible panel store
        'productdeleted': true, // Event fired to remove data for collapsible panel store
        'customerchangepriceload': true // Event fired when customer is changed
    });
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
}

Wtf.extend(Wtf.account.FixedAssetPurchaseRequisitionGrid, Wtf.grid.EditorGridPanel, {
    clicksToEdit: 1,
    stripeRows: true,
    rate: 1,
    symbol: null,
    disabledClass: "newtripcmbss",
    layout: 'fit',
    viewConfig: {forceFit: true},
    forceFit: true,
    loadMask: true,
    
    onRender: function(config) {
        Wtf.account.FixedAssetPurchaseRequisitionGrid.superclass.onRender.call(this,config);
        this.isValidEdit = true;
        
        if (Wtf.userds) {
            Wtf.userds.load();
        }
        
        if (Wtf.locationStore) {
            Wtf.locationStore.load();
        }
        
        if (Wtf.detartmentStore) {
            Wtf.detartmentStore.load();
        }
        WtfGlobal.getGridConfig(this,this.moduleid,true,false);
        this.on('render',this.addBlankRow,this);
        this.on('afteredit',this.updateRow,this);
        this.on('validateedit',this.checkRow,this);
        this.on('rowclick',this.handleRowClick,this);
        this.on('cellclick',this.RitchTextBoxSetting,this);
        this.on('render', function () {
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.on('statesave', this.saveGridStateHandler, this);
            }, this);
        }, this);
        this.on('beforeedit', function(e) {
            if (this.isLeaseFixedAsset) {
                if (this.isInvoice && this.isCustomer) { // you can create lease sales invoice only after selecting LDO
                    if (this.fromPO == undefined || this.fromPO == null || !this.fromPO) {
                        e.cancel = true;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.fixed.asset.invoice.lease") ], 4);
                        return;
                    }
                }
            }
            
            if ((this.isFixedAsset || this.isLeaseFixedAsset) && this.linkedFromOtherTransactions) {
                if (this.store.getCount()-1 == e.row) { // if last row then don't allow edition, and appending row in case of linking'
                    e.cancel = true;
                    return;
                }
            }
            
            if (e.field == "productid" && e.grid.colModel.config[3].dataIndex=="productid") {
                if (Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer && !this.isQuotation) {
                    var store = e.grid.colModel.config[3].editor.field.store;
                    if (store!=undefined && store.data.length>0) {
                        this.duplicateStore.removeAll();
                        this.duplicateStore.add(store.getRange());
                        
                        this.duplicateStore.each(function(record) {
                            if (record.data.isStopPurchase == true) {
                                this.duplicateStore.remove(record);
                            }
                        },this);
                        e.grid.colModel.config[3].editor.field.store=this.duplicateStore;
                    }                
                }
            }
            
            var isRateFieldEditable = true;
            
            if (!this.isValidEdit) { // Fixed Bug[13888]: Overlaping text box on validation alert messages. [on TAB key navigation]
                e.cancel= true;
                this.isValidEdit = true;
            }
            if ((e.field == "rate") && ((e.record.data.isNewRecord =="" && !this.isEdit ) || (e.record.data.linkid !="" && this.isEdit))) { //isNewRecord for nornal records is "1"
                if (this.editLinkedTransactionPrice && (((this.fromPO || (this.isEdit && this.fromPO==false)) && !this.isCustomer && (this.soLinkFlag==false || (this.isEdit && this.soLinkFlag==null ))) || ((this.isOrder || this.isQuotation) && this.isCustomer && (this.soLinkFlag==false || (this.isEdit && this.soLinkFlag==null))) || (!this.isOrder && this.isCustomer && (this.soLinkFlag==false || (this.isEdit && this.soLinkFlag==null)))) && !this.isRequisition ) {
                    e.cancel = true;
                    isRateFieldEditable = false;
                }
            }
            
            if (e.field == "rate" && !this.isFromGrORDO && isRateFieldEditable) { // Product rate will be editable only if  1) it has edit permission set in Account preferences and{ 2) transaction is being created by linking GR OR DO or 3) login user has price amending permission to edit it) .
                if (this.isCustomer) {
                    if (this.isInvoice) {
                        if (!Wtf.productPriceEditPerm.priceEditPerm.invoice) {
                            e.cancel = true;
                        }
                    } else if (this.isCash) {
                        if (!Wtf.productPriceEditPerm.priceEditPerm.invoice) {
                            e.cancel = true;
                        }
                    } else if (this.isOrder && !this.isQuotation) {
                        if (!Wtf.productPriceEditPerm.priceEditPerm.salesOrder) {
                            e.cancel = true;
                        }
                    } else if (this.isQuotation) {
                        if (!Wtf.productPriceEditPerm.priceEditPerm.customerQuotation) {
                            e.cancel = true;
                        }
                    }
                } else {
                    if (this.isInvoice) {
                        if (!Wtf.productPriceEditPerm.priceEditPerm.goodsReceipt) {
                            e.cancel = true;
                        }
                    } else if (this.isCash) {
                        if (!Wtf.productPriceEditPerm.priceEditPerm.goodsReceipt) {
                            e.cancel = true;
                        }
                    } else if (this.isOrder && !this.isQuotation) {
                        if (!Wtf.productPriceEditPerm.priceEditPerm.purchaseOrder) {
                            e.cancel = true;
                        }
                    } else if (this.isQuotation) {
                        if (!Wtf.productPriceEditPerm.priceEditPerm.vendorQuotation) {
                            e.cancel = true;
                        }
                    }
                }
                
                var beforeEditRecord=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
                
                if (beforeEditRecord == undefined || beforeEditRecord == null) {
                    if (this.productOptimizedFlag == undefined || this.productOptimizedFlag == Wtf.Show_all_Products) {
                        e.cancel = true;
                    } else {
                        if (e.record.data == undefined || e.record.data.productid == undefined || e.record.data.productid == "") {
                            e.cancel = true;
                        }
                    }
                }
            } else if (e.field == "desc" && Wtf.account.companyAccountPref.ishtmlproddesc) {
                e.cancel = true;
                if (e.record.data.productid != "") {
                    this.getPostTextEditor(e);
                }
                return; 
            }
            
           var isQuantityFieldEditable = true;
            if((e.field == "quantity")&& ((e.record.data.isNewRecord =="" && !this.isEdit ) ||(e.record.data.linkid !="" && this.isEdit))){//isNewRecord for normal records is "1"
                if(this.editLinkedTransactionQuantity && (((this.fromPO||(this.isEdit && this.fromPO==false)) && !this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null )) )||((this.isOrder||this.isQuotation) && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null)) )||(!this.isOrder && this.isCustomer && (this.soLinkFlag==false||(this.isEdit && this.soLinkFlag==null))))&& !this.isRequisition ){  //|| (this.fromOrder && this.isCustomer)
                    e.cancel = true;
                    isQuantityFieldEditable = false;
                }
            }
            if(e.field == "quantity" && !this.isFromGrORDO && isQuantityFieldEditable){
                var beforeEditRecord=this.productComboStore.getAt(this.productComboStore.find('productid',e.record.data.productid));
                if(beforeEditRecord == undefined || beforeEditRecord == null){
                    if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){  
                        e.cancel = true;
                    }else{
                        if(e.record.data ==undefined || e.record.data.productid == undefined || e.record.data.productid ==""){ 
                            e.cancel = true;
                        }
                    }
                }
            }
            
                Wtf.Ajax.requestEx({
                    url: "ACCProduct/getIndividualProductPrice.do",
                    params: {
                        productid: e.record.data.productid,
                        affecteduser: this.affecteduser,
                        currency: this.parentObj.Currency.getValue(),
                        quantity: e.record.data.quantity,
                        transactiondate: WtfGlobal.convertToGenericDate(this.billDate),
                    carryin: (this.isCustomer) ? false : true
                    }
                }, this, function(response) {
                if (response.data) {
                    for (var i = 1; i < response.data.length; i++) {
                        var dataObj = response.data[i];
                        var key = dataObj.key;
                        var custValue = dataObj[key];
                        for (var k = 0; k < e.grid.colModel.config.length; k++) {
                            if (e.field == key) {
                                var store = e.grid.colModel.config[6].editor.field.store;
                                store.clearFilter();
                                store.filterBy(function(rec) {
                                    var recId = rec.data.id;
                                    if ((custValue.indexOf(recId) !== -1)) {
                                        return true;
                                    } else {
                                        return false;
                            }
                }, this);
                            }  
                        }
                        }
                    }
                                    }, this);
             
        },this);
        
        if (!this.isNote && !this.readOnly) {
            if (this.record == null || this.record == undefined && this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined) {
                this.getColumnModel().setHidden(this.getColumnModel().getIndexById(this.id+"prtaxid"), true); // 21241   If statement added bcos could not use the event destroy for column model
                this.getColumnModel().setHidden(this.getColumnModel().getIndexById(this.id+"taxamount"), true); // and also could not call the createColumnModel() method from onRender
            }
        }
    },
    saveGridStateHandler: function(grid,state){
        if(!this.readOnly){
            WtfGlobal.saveGridStateHandler(this,grid,state,this.moduleid,this.gridConfigId,true);
        }
    }, 
     populateDimensionValueingrid: function(rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    
    getPostTextEditor: function(e) {
        var _tw=new Wtf.EditorWindowQuotation({
            val: e.record.data.desc,
            id: "abcd"
        });
        
    	this.remark.focus.defer(150,true);
        _tw.on("okClicked", function(obj) {
            var postText = obj.getEditorVal().textVal;
            var styleExpression = new RegExp("<style.*?</style>");
            postText=postText.replace(styleExpression,"");
            e.record.set("desc",postText);
        }, this);         
        _tw.show();
    },
    
    createStore: function() {
        this.deleteRec = new Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'productname'},
            {name: 'productquantity'},
            {name: 'productbaseuomrate'},
            {name: 'productbaseuomquantity'},
            {name: 'productuomid'},
            {name: 'productinvstore'},
            {name: 'productinvlocation'},
            {name: 'productrate'}
        ]);
        
        this.deleteStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.deleteRec)
        });
        
        this.priceRec = Wtf.data.Record.create ([
            {name: 'productid'},
            {name: 'productname'},
            {name: 'desc'},
            {name: 'uomid'},
            {name: 'uomname'},
            {name: 'multiuom'},
            {name: 'blockLooseSell'},
            {name: 'parentid'},
            {name: 'parentname'},
            {name: 'purchaseaccountid'},
            {name: 'salesaccountid'},
            {name: 'purchaseretaccountid'},
            {name: 'salespricedatewise'},
            {name: 'purchasepricedatewise'},
            {name: 'salesretaccountid'},
            {name: 'reorderquantity'},
            {name: 'pricedatewise'},
            {name: 'quantity'},
            {name: 'reorderlevel'},
            {name: 'leadtime'},
            {name: 'purchaseprice'},
            {name: 'saleprice'},
            {name: 'leaf'},
            {name: 'type'},
            {name: 'prtaxid'},
            {name: 'taxamount'},
            {name: 'prtaxpercent'},
            {name: 'prtaxname'},
            {name: 'level'},
            {name: 'initialquantity', mapping: 'initialquantity'},
            {name: 'initialprice'},
            {name: 'shelfLocation'},
            {name: 'producttype'}
        ]);
        
        this.priceStore = new Wtf.data.Store({
            url: "ACCProduct/getProductsForCombo.do",
            baseParams: {
                mode:22
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.priceRec)
        });

        this.priceStore.on('load',this.setGridProductValues,this);
        
        this.storeRec = Wtf.data.Record.create([
            {name: 'rowid', defValue: null},
            {name: 'productname', mapping: (this.isViewCNDN)? 'productdetail': null},
            {name: 'billid'},
            {name: 'billno'},
            {name: 'Cust_billno'},
            {name: 'productid'},
            {name: 'desc'},
            {name: 'quantity', defValue: 1.00},
            {name: 'baseuomquantity', defValue: 1.00},
            {name: 'uomname'},
            {name: 'uomid'},
            {name: 'baseuomrate', defValue: 1.00},
            {name: 'copyquantity', mapping: 'quantity'},
            {name: 'rate', defValue: 0},
            {name: 'unitPricePerInvoice', defValue: 0},
            {name: 'rateinbase'},
            {name: 'partamount', defValue: 0},
            {name: 'discamount'},
            {name: 'discount'},
            {name: 'discountispercent', defValue: 1},
            {name: 'prdiscount', defValue: 0},
            {name: 'invstore'},
            {name: 'invlocation'},
            {name: 'prtaxid'},
            {name: 'prtaxname'},
            {name: 'prtaxpercent', defValue: 0},
            {name: 'taxamount', defValue: 0},
            {name: 'amount', defValue: 0},
            {name: 'amountwithtax', defValue: 0},
            {name: 'amountwithouttax', defValue: 0}, // used this field for Invoice Terms - rate*qty-discount
            {name: 'taxpercent'},
            {name: 'remark'},
            {name: 'transectionno'},
            {name: 'remquantity'},
            {name: 'remainingquantity'},
            {name: 'oldcurrencyrate', defValue: 1},
            {name: 'currencysymbol', defValue: this.symbol},
            {name: 'currencyrate', defValue: 1},
            {name: 'externalcurrencyrate'},
            {name: 'orignalamount'},
            {name: 'typeid', defValue: 0},
            {name: 'isNewRecord', defValue: '1'},
            {name: 'producttype'},
            {name: 'permit'},
            {name: 'linkto'},
            {name: 'linkid'},
            {name: 'linktype'},
            {name: 'savedrowid'},
            {name: 'originalTransactionRowid'},
            {name: 'changedQuantity'},
            {name: 'approvedcost'},
            {name: 'approverremark'},
            {name: 'customfield'},
            {name: 'gridRemark'},
            {name: 'productcustomfield'},
            {name: 'accountId'},
            {name: 'salesAccountId'},
            {name: 'discountAccountId'},
            {name: 'rowTaxAmount'},
            {name: 'type'},                        
            {name: 'shelfLocation'},
            {name: 'productcustomfield'},
            {name: 'supplierpartnumber'},
            {name: 'assetDetails'},
            {name: 'profitLossAmt'},
            {name: 'copybaseuomrate', mapping: 'baseuomrate'}, // for handling inventory updation 
            {name: 'priceSource'},
            {name: 'pid'},
            {name: 'srno', isForSequence:true}
        ]);
        
        var url = Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)? 'CustomerManager.jsp' : 'VendorManager.jsp') : ((this.isCN)? 'CustomerManager.jsp' : 'VendorManager.jsp'));
        
        if (this.fromOrder) {
            url = Wtf.req.account+(this.isCustomer? 'CustomerManager.jsp' : 'VendorManager.jsp');
        }
        
        this.store = new Wtf.data.Store({
            url: url,
            pruneModifiedRecords: true,
//            sortInfo:{
//                field:'srno',
//                direction:'ASC'
//            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.storeRec)
        });
        this.store.on('load',this.loadPOProduct,this);
    },
    
    createComboEditor: function() {
        this.poProductRec = Wtf.data.Record.create ([
            {name: 'productid'},
            {name: 'quantity'},
            {name:' prtaxid'}
        ]);
        
        this.poProductStore = new Wtf.data.Store({
            url: this.isCustomer? 'ACCSalesOrderCMN/getSalesOrderRows.do' : 'ACCPurchaseOrderCMN/getPurchaseOrderRows.do',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.poProductRec)
        });
        
        this.productComboStore.on('beforeload',function(s,o) {
            if (!o.params) {
                o.params = {};
            }
            
            var currentBaseParams = this.productComboStore.baseParams;
            currentBaseParams.getSOPOflag = true;
            currentBaseParams.onlyProduct = this.isOrder?false:true
            currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
            currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));         
            this.productComboStore.baseParams = currentBaseParams;        
        },this);
        
        if(this.isCustomer) {
            chkproductSalesload();
        } else {
            chkproductload();                                        
        }
         if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
            this.productEditor = new Wtf.form.ExtFnComboBox({
                name: (this.isLeaseFixedAsset)? 'pid' : 'productname',
                store: this.productComboStore, // Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus: true,
                maxHeight:250,
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField: 'productid',
                displayField: (this.isLeaseFixedAsset)? 'pid' : 'productname',
                extraFields: (this.isLeaseFixedAsset)?['productname','type']:['pid','type'],
                extraComparisionField: 'pid', // type ahead search on acccode as well.
                listWidth: 400,
                lastQuery: '',
                scope: this,
                hirarchical: true,
                forceSelection: true
            });
        }else{
            this.productEditor = new Wtf.form.ExtFnComboBox({
                name: (this.isLeaseFixedAsset)? 'pid' : 'productname',
                store: this.productComboStore, // Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus: true,
                maxHeight:250,
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField: 'productid',
                displayField: (this.isLeaseFixedAsset)? 'pid' : 'productname',
                extraFields: (this.isLeaseFixedAsset)?['productname','type']:['pid','type'],
                extraComparisionField: 'pid', // type ahead search on acccode as well.
                listWidth: 400,
                lastQuery: '',
                scope: this,
                hideTrigger:true,
                mode:'remote',
                hirarchical: true,
                forceSelection: true
            });
        }
        
        this.productEditor.on("blur", function(e,a,b) {
            if (Wtf.account.companyAccountPref.invAccIntegration && !this.isCustomer && !this.isQuotation) {
                e.store = this.productComboStore;
            } 
        }, this);
        
        chkUomload();
        
        this.uomEditor = new Wtf.form.FnComboBox({
            name: 'uomname',
            store: Wtf.uomStore, // Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus: true,
            valueField: 'uomid',
            displayField: 'uomname',
            scope: this,
            forceSelection: true
        });
        
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.edit)) {
            this.uomEditor.addNewFn=this.showUom.createDelegate(this);
        }
        
        this.noteTypeRec = new Wtf.data.Record.create([
            {name: 'typeid'},
            {name: 'name'}
        ]);

        this.typeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.noteTypeRec),
            url: "ACCCreditNote/getNoteType.do",
            baseParams: {
                mode: 31,
                combineData: -1 // Send For Seprate Request
            }
        });
        this.typeStore.load();

        this.typeEditor = new Wtf.form.ComboBox({
            store: this.typeStore,
            name: 'typeid',
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true,
            listeners: {
                afterrender: function(combo) {
                    var recordSelected = combo.getStore().getAt(0);                     
                    combo.setValue(recordSelected.get("typeid"));
                }
            }
        });
        
        this.accRec = Wtf.data.Record.create([
            {name: 'accountname', mapping: 'accname'},
            {name: 'accountid', mapping: 'accid'},
            {name: 'acccode'},
            {name: 'groupname'}
        ]);
        
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams: {
                mode: 2,
               ignorecustomers: true,  
               ignorevendors: true,
                nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
        WtfGlobal.setAjaxTimeOut();
        this.accountStore.load();
        
        this.accountStore.on('load', function(store, rec) {
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        
        this.cmbAccount = new Wtf.form.ExtFnComboBox({
            hiddenName: 'accountid',
            store: this.accountStore,
            minChars: 1,
            valueField: 'accountid',
            displayField: 'accountname',
            forceSelection: true,
            hirarchical: true,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            mode: 'local',
            typeAheadDelay: 30000,
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            listWidth: Wtf.account.companyAccountPref.accountsWithCode? 500 : 400
        });
        
        this.rowDiscountTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid', type:'int'}, 'name'],
            data :[[1,'Percentage'], [0,'Flat']]
        });
        
        this.rowDiscountTypeCmb = new Wtf.form.ComboBox({
            store: this.rowDiscountTypeStore,
            name: 'typeid',
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true
        });
        
        this.inventoryStores = new Wtf.form.ComboBox({
            store: Wtf.inventoryStore,
            name: 'storeid',
            displayField: 'storedescription',
            valueField: 'storeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true
        });
        
        this.inventoryLocation = new Wtf.form.ComboBox({
            store: Wtf.inventoryLocation,
            name: 'locationid',
            displayField: 'locationname',
            valueField: 'locationid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true
        });
        
        this.remark = new Wtf.form.TextArea({
            name:'remark'
        });
        
        this.cndnRemark = new Wtf.form.TextArea({
            name: 'remark'
        });
        
        this.approverremark = new Wtf.form.TextField({
            name: 'approverremark'
        });
        
        this.permiteditor = new Wtf.form.TextField({
            name: 'permit',
            maxLength: 50
        });
        
        this.transDiscount = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue: 0,
            decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        
        this.transTaxAmount = new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue: 0,
            decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        
        this.partAmount = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue: 0,
            decimalPrecision: 2
        });
        
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid', mapping: 'taxid'},
           {name: 'prtaxname', mapping: 'taxname'},
           {name: 'taxdescription'},
           {name: 'percent', type: 'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type: 'date'}
        ]);
        
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.taxRec),
            url: "ACCTax/getTax.do",
            baseParams: {
                mode: 33,
                moduleid: this.moduleid
            }
        });
        
        if (this.readOnly) {
            this.taxStore.load();
        }

        this.transTax = new Wtf.form.FnComboBox({
            hiddenName: 'prtaxid',
            anchor: '100%',
            store: this.taxStore,
            valueField: 'prtaxid',
            forceSelection: true,
            displayField: 'prtaxname',
            scope: this,
            displayDescrption: 'taxdescription',
            selectOnFocus: true
        });
        
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit)) {
            this.transTax.addNewFn = this.addTax.createDelegate(this);
        }
        
        this.transQuantity = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength: 10,
            maskRe: /[0-9]+(\.[0-9]+)?$/,
           // decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
           allowDecimals:false
        });
        
        this.transBaseuomrate = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength: 10
        });
        
        this.editprice = new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,    
            maxLength: 14
        });                        
    },
    
    showUom: function() {
        callUOM('uomReportWin');
        Wtf.getCmp('uomReportWin').on('update', function() {
            Wtf.uomStore.reload();
        }, this);
    },
    
    addTax: function() {
        this.stopEditing();
        var p = callTax("taxwin");
        Wtf.getCmp("taxwin").on('update', function(){this.taxStore.reload();}, this);
    },
    
    loadPriceAfterProduct: function() {
        if (Wtf.getCmp(this.id)) { // Load price store if component exists
            this.loadPriceStore();
        }
    },
    
    loadPriceStore: function(val) {
        this.billDate  = (val==undefined? this.billDate : val);
    },
    
    loadPriceStoreOnly: function(val,pricestore, affecteduser) { // scope related issue
        this.dateChange = true;
        this.billDate = (val==undefined?this.billDate:val);
        affecteduser = (affecteduser == undefined)? "" : affecteduser;
        pricestore.load({
            params: {
                transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                affecteduser : affecteduser
            }
        });
    },
    
    openProductWindow: function() {
        this.stopEditing();
           if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.create)){
            callProductWindow(false, null, "productWin");
            Wtf.getCmp("productWin").on("update",function(obj,productid){this.productID=productid;},this);
        }
        else{
              WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.create.products")); 
        }
    },

    createColumnModel: function() {
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno = (this.isNote)? new Wtf.grid.CheckboxSelectionModel() : new Wtf.grid.RowNumberer();
        var columnArr = [];
        
        if (!this.readOnly) {
            columnArr.push(this.rowno);
        }
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:65,
            align:'center',
            //dataIndex:'srno',
            name:'srno',
            renderer: Wtf.applySequenceRenderer
        });
        columnArr.push({
            dataIndex: 'rowid',
            hidelabel: true,
            hidden: true
        },{
            dataIndex: 'billid',
            hidelabel: true,
            hidden: true
        },{
            header: (this.isLeaseFixedAsset)?WtfGlobal.getLocaleText("acc.product.gridProductID"):WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"), // "Produt ID" : "Asset Group",
            width: 200,
            dataIndex: (this.isLeaseFixedAsset)? (this.readOnly?'pid':'productid') : (this.readOnly?'productname':'productid'),
            renderer:(this.productOptimizedFlag==Wtf.Products_on_type_ahead)?(this.readOnly?"":this.getComboNameRenderer(this.productEditor)):(this.readOnly?"":Wtf.comboBoxRenderer(this.productEditor)),
            editor: (this.isNote||this.readOnly)?"":this.productEditor
        });
        
        if(!this.isFixedAsset){ //ERP-18989
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name",
                dataIndex: 'productname',
                hidden: this.isFixedAsset
            });
        }
        
       columnArr.push({
            header: WtfGlobal.getLocaleText("acc.je.acc"),
            width: 200,
            dataIndex: 'accountId',
            hidden: (!this.isNote ||this.noteTemp),
            renderer: Wtf.comboBoxRenderer(this.cmbAccount),
            editor: this.readOnly?"":this.cmbAccount
        },{
            header: this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),//"Invoice No.":"Vendor Invoice No.",
            width: 150,
            dataIndex: this.noteTemp?'transectionno':'billno',
            hidden: !this.isNote
        },{
             header: this.isLeaseFixedAsset?WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"):WtfGlobal.getLocaleText("erp.field.AssetDesciption"),//"Description",
             dataIndex: "desc",
             hidden: this.isNote,
             width: 250,
             editor: (this.isNote||this.readOnly)?"":this.remark,
             renderer: this.descriptionRenderer
//                     function(val) {
//                 var regex = /(<([^>]+)>)/ig;
//                 val = val.replace(/(<([^>]+)>)/ig,"");
//                 return val;
//             }
         },{
             header: WtfGlobal.getLocaleText("acc.product.supplier"), // "Supplier Part Number",
             dataIndex: "supplierpartnumber",
             hidden: !(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.partNumber && !this.isCustomer && !this.isQuotation && !this.isNote),
             width: 150         
         },{
             header: WtfGlobal.getLocaleText("acc.field.InventoryStore"), 
             dataIndex: 'invstore',
             hidden:! (Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && ((Wtf.account.companyAccountPref.withinvupdate && (!this.isInvoice && !this.isOrder)) || (!Wtf.account.companyAccountPref.withinvupdate && !this.isOrder))),                 
             width: 150,
             renderer: Wtf.comboBoxRenderer(this.inventoryStores),
             editor: (this.readOnly)?"":this.inventoryStores
         },{
             header: WtfGlobal.getLocaleText("acc.field.ShelfLocation"),
             dataIndex: "shelfLocation",
             hidden: !(Wtf.account.companyAccountPref.invAccIntegration &&  this.isOrder && !this.isCustomer && !this.isQuotation),
             width: 250,
             editor: (this.readOnly)?"":new Wtf.form.TextField()    
         },{
             header: WtfGlobal.getLocaleText("acc.field.InventoryLocation"), 
             dataIndex: 'invlocation',
             hidden: !(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel && ((Wtf.account.companyAccountPref.withinvupdate && (!this.isInvoice && !this.isOrder)) || (!Wtf.account.companyAccountPref.withinvupdate && !this.isOrder))),                 
             width: 150,
             renderer: Wtf.comboBoxRenderer(this.inventoryLocation),
             editor: (this.readOnly)?"":this.inventoryLocation
         });
         
         columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModelForProduct[this.moduleid],undefined,undefined,this.readOnly);
         columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
         
         columnArr.push({
             header: WtfGlobal.getLocaleText("acc.field.PermitNo."),
             dataIndex: "permit",
             hidden: true,
             width: 100,
             editor: (this.readOnly)?"":this.permiteditor
         },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridNoteType"), // "Note Type",
            width: 200,
            dataIndex: 'typeid',
            hidden: (!this.isNote ||this.noteTemp),
            renderer: Wtf.comboBoxRenderer(this.typeEditor),
            editor: this.readOnly?"":this.typeEditor
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridQty"), // "Quantity",
             dataIndex: "quantity",
             align: 'right',
             width: 100,
             renderer: this.quantityRenderer,
             editor: (this.isNote||this.readOnly)? "" : this.transQuantity
         },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"), // "UOM",
            width: 100,
            hidden: (this.isFixedAsset ||this.isLeaseFixedAsset),
            dataIndex: this.readOnly?'uomname':'uomid',
            renderer: this.readOnly?"":Wtf.comboBoxRenderer(this.uomEditor),
            editor: (this.isNote||this.readOnly)?"":this.uomEditor
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"), // Base UOM Rate
             dataIndex: "baseuomrate",
             hidden: this.isFixedAsset,
             align: 'left',
             width: 100,
             renderer: this.conversionFactorRenderer(this.productComboStore,"productid","uomname"),
             editor: (this.isNote||this.readOnly)?"":this.transBaseuomrate
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"), // Base UOM Quantity
             dataIndex: "baseuomquantity",
             hidden: this.isFixedAsset,
             align: 'right',
             width: 50,
             renderer: this.storeRenderer(this.productComboStore,"productid","uomname")
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridRemQty"), // "Remaining Quantity",
             dataIndex: "remainingquantity",
             align: 'right',
             hidden: !this.isNote||this.noteTemp,
             width: 150,
             renderer: this.quantityWithUOMRenderer(this.productComboStore,"productid","uomname"),
             editor: (this.isNote||this.readOnly)?"":this.transQuantity
        },{
             header: "<b> "+ WtfGlobal.getLocaleText("acc.invoice.gridEnterQty") +" </b>",//"<b>Enter Quantity</b>",
             dataIndex: "remquantity",
             align: 'right',
             hidden: !this.isNote||this.noteTemp,
             width: 180,
             renderer: this.quantityWithUOMRenderer(this.productComboStore,"productid","uomname"),
             editor: this.readOnly?"":this.transQuantity
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),// "Unit Price",
             dataIndex: "rate",
             align: 'right',
             width: 150,
             renderer: WtfGlobal.withoutRateCurrencySymbol,
             editor: (this.isNote||this.readOnly)?"":this.editprice,
             editable: true,
             hidden: this.noteTemp || this.isRFQ
        },{
             header: WtfGlobal.getLocaleText("acc.field.PartialAmount(%)"),
             dataIndex: "partamount",
             align:'right',
             id: this.id + "partdisc",
             hidden: !this.isNote || this.noteTemp || !this.isCustomer,
             width: 120,
             fixed: true,
             renderer: function(v){return'<div class="currency">'+parseFloat(v).toFixed(2)+'%</div>';},
             editor: this.readOnly?"":this.partAmount
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width: 200,
            dataIndex: 'discountispercent',
            renderer: Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor: (this.isNote||this.readOnly)? "":this.rowDiscountTypeCmb,
            hidden: this.isFixedAsset
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex: "prdiscount",
             align: 'right',
             width: 150,
             renderer: function(v,m,rec) {
                 if (rec.data.discountispercent) {
                     v = v + "%";
                 } else {
                     var symbol = WtfGlobal.getCurrencySymbol();
                     if (rec.data['currencysymbol']!=undefined && rec.data['currencysymbol']!="") {
                         symbol = rec.data['currencysymbol'];
                     }
                     
                     v= WtfGlobal.conventInDecimal(v,symbol)
                 }
                 return'<div class="currency">'+v+'</div>';
             },
             editor: this.readOnly||this.isNote?"":this.transDiscount,
             hidden: this.isFixedAsset
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"), // "Product Tax",
             dataIndex: "prtaxid",
             id: this.id+"prtaxid",
             fixed: true,
             width: 120,
             hidden: !(this.editTransaction||this.readOnly) || this.noteTemp,
             renderer: Wtf.comboBoxRenderer(this.transTax),
             editor: this.readOnly||this.isNote?"":this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"), // "Tax Amount",
             dataIndex: "taxamount",
             id:this.id+"taxamount",
              fixed:true,
             //align:'right',
             width:150,
             editor:this.readOnly?"":this.transTaxAmount,
             hidden:!(this.editTransaction||this.readOnly)|| this.noteTemp, // || !this.isOrder,
             renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.Tax"),//"Tax",
             dataIndex:"taxpercent",
             align:'right',

             hidden:!this.isNote || this.noteTemp,
             width:200,
             renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
             header:WtfGlobal.getLocaleText("acc.field.ProductTaxAmount"),
             dataIndex:"taxamount",
             align:'right',
             hidden:!this.isNote || this.noteTemp,
             width:200,
             renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
             header: this.isQuotationFromPR ? WtfGlobal.getLocaleText("acc.field.BudgetedCost") : (this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridOriginalAmt"):WtfGlobal.getLocaleText("acc.invoice.gridInvAmt")),//"Original Amount":"Invoice Amount",
             dataIndex: "orignalamount",
             align: 'right',
             width: 150,
             hidden: this.isQuotationFromPR ? false : (!(this.isNote) || this.noteTemp),
             renderer: (this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol)
        },{
             header: this.isRequisition ? (WtfGlobal.getLocaleText("acc.field.EstimatedCost")) : this.isNote? WtfGlobal.getLocaleText("acc.invoice.gridCurAmt") : WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // "Current Amount " : "Amount",
             dataIndex: "amount",
             hidden: this.isRFQ,
             align: 'right',
             width: 200,
             renderer: (this.isNote?WtfGlobal.withoutRateCurrencySymbol:this.calAmountWithoutExchangeRate.createDelegate(this))
        },{
             header: (this.readOnly)?WtfGlobal.getLocaleText("acc.invoice.gridAmount"):"<b>"+ WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt") + "</b>",//"Amount":"<b>Enter Amount</b>",
             dataIndex: this.noteTemp?'discount':'discamount',
             align: 'right',
             width: 200,
             hidden: !this.isNote,
             renderer: WtfGlobal.withoutRateCurrencySymbol,
             editor: (this.readOnly)? "" : new Wtf.form.NumberField({
                 allowBlank: false,
                 allowNegative: false
             })
        },{
             header:WtfGlobal.getLocaleText("acc.field.ApproverRemark"),//"Approver Remark",
             dataIndex:"approverremark",
             hidden: (this.isRequisition && this.editTransaction) ? false : true,
             width: 250,
             editor: (this.isRequisition && this.editTransaction)? this.approverremark : ""
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridRemark"),//"Remark",
            width: 200,
            hidden: (!this.isNote ||this.noteTemp),
            dataIndex: 'gridRemark',
            name: 'gridRemark',
            editor: (this.readOnly)? "" : this.cndnRemark
        });
        
        if (!this.isNote && !this.readOnly) {
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align: 'center',
                width: 40,
                hidden: this.readOnly,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        
        if(this.isFixedAsset || (this.isLeaseFixedAsset && !this.isQuotation)) {
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.fixed.asset.view"),
                align: 'center',
                width: 40,
                renderer: this.viewRenderer.createDelegate(this)
            });
        }
        this.cm = new Wtf.grid.ColumnModel(columnArr);                
    },
    
    getComboNameRenderer : function(combo){
        return function(value,metadata,record,row,col,store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store,value,combo.valueField);
            var fieldIndex = "pid";
            if(idx == -1) {
                if(record.data["pid"] && record.data[fieldIndex].length>0) {
                    return record.data[fieldIndex];
                }
                else
                    return "";
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get(combo.displayField);
            record.set("productid", value);
            record.set("pid", displayField);
            return displayField;
        }
    },
    
    conversionFactorRenderer:function(store, valueField, displayField) {
        return function(value, meta, record) {
            var idx = Wtf.uomStore.find("uomid", record.data["uomid"]);            
            if (idx == -1) {
                return value;
            }
            var uomname = Wtf.uomStore.getAt(idx).data["uomname"];
            if (uomname == "N/A") {
                return value;
            }
            idx = store.find(valueField, record.data[valueField]);
            if (idx == -1) {
                return value;
            }
            var rec = store.getAt(idx);
            return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
        }
    },descriptionRenderer: function(val, meta, rec, row, col, store) {
        var regex = /(<([^>]+)>)/ig;
//        val = val.replace(/(<([^>]+)>)/ig, "");
        var tip = val.replace(/"/g, '&rdquo;');
        meta.attr = 'wtf:qtip="' + tip + '"' + '" wtf:qtitle="' + WtfGlobal.getLocaleText("acc.gridproduct.discription") + '"';
        return val;
    },
    RitchTextBoxSetting: function(grid, rowIndex, columnIndex, e) {
        var v = WtfGlobal.RitchTextBoxSetting(grid, rowIndex, columnIndex, e, this.readOnly);
        return v;
    },
    
    quantityWithUOMRenderer: function(store, valueField, displayField) {
        return function(value, meta, record) {
            value = (parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var idx = Wtf.uomStore.find("uomid", record.data["uomid"]);            
            if (idx == -1) {
                return value;
            }
            var uomname = Wtf.uomStore.getAt(idx).data["uomname"];
            return value+" "+uomname;
        }
    },
    
    quantityRenderer: function(val,m,rec) {
        return (val=="NaN"?0:val);  //return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    },
    
    deleteRenderer: function(v,m,rec) {
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    
    viewRenderer: function(v,m,rec) {
        return "<div class='view pwnd view-gridrow' title=" + WtfGlobal.getLocaleText("acc.fixed.asset.detailsTT") + "></div>";
    },
    
    handleRowClick: function(grid,rowindex,e) {
        if(!this.readOnly && e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid,0,rowindex);
        }
        if(!this.readOnly && e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid,1,rowindex);
        }
        if (e.getTarget(".delete-gridrow")) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn) {
                if (btn != "yes") {
                    return;
                }
                var store = grid.getStore();
                var total = store.getCount();
                var record = store.getAt(rowindex);
                if (record.data.copyquantity != undefined) {
                    var deletedData = [];
                    var newRec = new this.deleteRec({
                        productid: record.data.productid,
                        productname: record.data.productname,    
                        productquantity: record.data.quantity, 
                        productbaseuomrate: record.data.baseuomrate,
                        productbaseuomquantity: record.data.baseuomquantity,
                        productuomid: record.data.uomid,
                        productinvstore: record.data.invstore,
                        productinvlocation: record.data.invlocation,
                        productrate: record.data.rate
                    // To do - Need to check this for multi UOM change
                    });
                    deletedData.push(newRec);
                    this.deleteStore.add(deletedData);                            
                }
                store.remove(store.getAt(rowindex));
                if (rowindex==total-1) {
                    this.addBlankRow();
                }
                this.fireEvent('datachanged',this);
                this.fireEvent('productdeleted',this);
            }, this);
        } else if (e.getTarget(".view-gridrow")) {
            var store = grid.getStore(); 
            var total = store.getCount();
            if (rowindex == total-1) {
                return;
            }
            var record = store.getAt(rowindex);
            if (this.isFixedAsset || (this.isLeaseFixedAsset && !this.isQuotation)) {
                var productid = record.get('productid');
                var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                //when creating asset Purchase Requ. with multiple Asset Group then productid found both 'productComboStore' and 'store' in type ahead 
                if(productComboRecIndex==-1){
                    productComboRecIndex=WtfGlobal.searchRecordIndex(store, productid, 'productid');
                    var proRecord = store.getAt(productComboRecIndex);
                } else if(productComboRecIndex >=0){
                    var proRecord = this.productComboStore.getAt(productComboRecIndex);
                }
                if (record.get("quantity") != 0){
                    this.callFixedAssetDetailsWindow(record, proRecord, this.readOnly);
                }
            }
        } else {
            this.fireEvent("productselect", grid.getStore().getAt(rowindex).get("productid"));
        }
    },

    storeRenderer: function(store, valueField, displayField) {
        return function(value, meta, record) {
            value = (parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var idx = store.find(valueField, record.data[valueField]);
            if (idx == -1) {
                return value;
            }
            var rec = store.getAt(idx);
            return value+" "+rec.data[displayField];
        }
    },

    checkRow: function(obj) {
        var rec = obj.record;
        if (obj.field == "uomid") {
            var prorec = null;
            var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
            if (productComboIndex >= 0) {
                prorec = this.productComboStore.getAt(productComboIndex);
                if(prorec.data.type=='Service') {
                    return false;
                } else if (!prorec.data.multiuom) {
                    return false;
                }
            }
        }
        /*Code on product select starts*/        
        if (obj.field == "productid") {
//            var isProductAlreadySelected = false;
//            
//            this.getStore().each(function(recr) {
//                var prodId = recr.get('productid');
//                if (prodId == obj.value) {
//                    isProductAlreadySelected = true;
//                    return false;
//                }
//            },this);
//            
//            var productText = "Product";
//            if (this.isFixedAsset) {
//                productText = "Asset Group";
//            }
//            
//            if (isProductAlreadySelected) {
//                WtfComMsgBox(['Infornation',productText+' Already Selected'],0);
//                obj.record.set('productid','');
//                return false;
//            }
            
            var index = this.productComboStore.findBy(function(rec) {
                if (rec.data.productid == obj.value) {
                    return true;
                } else {
                    return false;
                }
            });
            var prorec = this.productComboStore.getAt(index);
            if (this.editTransaction) { // In Edit Case Check product quantity is greater than available quantity when selecting product                
                var availableQuantity = prorec.data.quantity; 
                var copyquantity = 0;                    
                this.store.each(function(rec) {
                    if (rec.data.productid == prorec.data.productid) {
                        if (rec.data.copyquantity!=undefined && rec.data.copyquantity!=undefined) {
                            copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate);                            
                        }
                    }
                },this);
                
                availableQuantity = availableQuantity + copyquantity;
                
                if (!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&this.store.find("productid",obj.value)>-1 && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder) {
                    var quantity = 0;                 
                    this.store.each(function(rec) {
                        if (rec.data.productid == obj.value) {
                            var ind = this.store.indexOf(rec);
                            if (ind != -1) {
                                if (ind!=obj.row) {                            
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }     
                        }
                    },this);
                    quantity = quantity + (obj.record.data['quantity']*obj.record.data['baseuomrate']);  
                    
                    if (availableQuantity < quantity) {
                        if (Wtf.account.companyAccountPref.negativestock == 1) { // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity+'<br><br><center>So you cannot proceed ?</center>'], 2);
                            obj.cancel = true;   
                        } else if (Wtf.account.companyAccountPref.negativestock == 2) { // Warn Case
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn) {
                                if (btn == "yes") {
                                    obj.cancel=false;
                                } else {
                                    rec.set("quantity",obj.originalValue);
                                    rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                    obj.cancel=true;
                                    return false;
                                }
                            },this); 
                        }
                    }
                } else if (!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&availableQuantity<(obj.record.data['quantity']*obj.record.data['baseuomrate'])&&prorec.data.type!='Service'&&!this.isQuotation&&!this.isOrder) {
                    this.isValidEdit = false;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+availableQuantity], 2);
                    obj.cancel = true;
                } // for new cash sales but not in if or else in this else loop and for edit no case satifies
            } else { // New transaction case... In normal Case Check product quantity is greater than available quantity when selecting product
                if (!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&this.store.find("productid",obj.value)>-1 && prorec.data.type!="Service" && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder) {
                    var quantity = 0;                 
                    this.store.each(function(rec) {
                        if (rec.data.productid == obj.value) {
                            var ind = this.store.indexOf(rec);
                            if (ind != -1) {
                                if (ind != obj.row) {
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }     
                        }
                    },this);
                    quantity = quantity + (obj.record.data['quantity']*obj.record.data['baseuomrate']);
                    quantity = quantity + obj.record.data['quantity'];
                    if (prorec.data['quantity']<quantity) {
                        if (Wtf.account.companyAccountPref.negativestock == 1){ // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']+'<br><br><center>So you cannot proceed ?</center>'], 2);
                            obj.cancel=true;   
                        } else if (Wtf.account.companyAccountPref.negativestock == 2) {     // Warn Case
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIaretheexceedingwishtoproceed")+'</center>' , function(btn){
                                if (btn=="yes") {
                                    obj.cancel=false;
                                } else {
                                    rec.set("quantity",obj.originalValue);
                                    rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                    obj.cancel = true;
                                    return false;
                                }
                            },this); 
                        }
                    }
                // for Invoice and Cash sales in  with Inventory And Without trading Flow  
                } else if (!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&prorec.data['quantity']<(obj.record.data['quantity']*obj.record.data['baseuomrate'])&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder) {
                    this.isValidEdit = false;
                    if (Wtf.account.companyAccountPref.negativestock == 1) { // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+' is '+prorec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                        obj.cancel = true;   
                    } else if (Wtf.account.companyAccountPref.negativestock == 2) { // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                            if (btn == "yes") {
                                obj.cancel = false;
                            } else {
                                rec.set("quantity",obj.originalValue);
                                rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                obj.cancel = true;
                                return false;
                            }
                        },this); 
                    }
                } else if (this.isCash&&this.isCustomer&&prorec.data['quantity']<(obj.record.data['quantity']*obj.record.data['baseuomrate'])&&prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part' &&!this.isQuotation&&!this.isOrder) {
                    this.isValidEdit = false;
                    if (Wtf.account.companyAccountPref.negativestock == 1) { // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantity")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data['productname']+WtfGlobal.getLocaleText("acc.field.is")+' '+prorec.data['quantity']+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                        obj.cancel = true;   
                    } else if (Wtf.account.companyAccountPref.negativestock == 2) { // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn){
                            if (btn == "yes") {
                                obj.cancel = false;
                            } else {
                                rec.set("quantity",obj.originalValue);
                                rec.set("baseuomquantity",obj.originalValue*obj.record.data['baseuomrate']);
                                obj.cancel=true;
                                return false;
                            }
                        },this); 
                    }
                }
            }  
        // EDIT case in without trading flow and with inventory        
        } else if (!Wtf.account.companyAccountPref.withinvupdate&&this.isCustomer&&(obj.field=="quantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder) {
            if (obj.field == "quantity") {
                var originalQuantity = obj.originalValue;
                var newQuantity = obj.value;
                var originalBaseuomrate = obj.record.data['baseuomrate'];
                var newBaseuomrate = obj.record.data['baseuomrate'];
            } else if (obj.field == "baseuomrate") {
                var originalQuantity = obj.record.data['quantity'];
                var newQuantity = obj.record.data['quantity'];
                var originalBaseuomrate = obj.originalValue;
                var newBaseuomrate = obj.value;
            }
            prorec = this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            if (prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                var quantity = 0;
                if (this.editTransaction) { // In Edit Case Check product quantity is greater than available quantity when selecting quantity                                  
                    var copyquantity = 0;                    
                    this.store.each(function(rec) {
                        if (rec.data.productid == prorec.data.productid) {
                            copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate);
                            var ind=this.store.indexOf(rec);
                            if (ind != -1 ){
                                if (ind != obj.row) {                            
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }
                        }
                    },this);
                    quantity = quantity + (newQuantity*newBaseuomrate);
                    availableQuantity = availableQuantity + copyquantity;   
                    if (this.editTransaction&&!this.copyInv&&availableQuantity < quantity) {
                        if (Wtf.account.companyAccountPref.negativestock==1) { // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCS/CIareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+' '+availableQuantity+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                            obj.cancel = true;   
                        } else if (Wtf.account.companyAccountPref.negativestock==2) { // Warn Case
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn) {
                                if (btn=="yes") {
                                    obj.cancel=false;
                                } else {
                                    rec.set("quantity",originalQuantity);
                                    rec.set("baseuomquantity",originalQuantity*originalBaseuomrate);
                                    rec.set("baseuomrate",originalBaseuomrate);
                                    obj.cancel = true;
                                    return false;
                                }
                            },this); 
                        }
                    } // for edit transaction in in without trading flow and with inventory
                } else { // In normal Case Check product quantity is greater than available quantity when selecting quantity                                
                    this.store.each(function(rec) {
                        if (rec.data.productid == prorec.data.productid) {
                            var ind = this.store.indexOf(rec);
                            if (ind != -1) {
                                if (ind != obj.row) {
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }        
                        }
                    },this);
                    quantity = quantity + (newQuantity*newBaseuomrate);
                    if ((!this.editTransaction||this.copyInv) &&  availableQuantity < quantity) {
                        if (Wtf.account.companyAccountPref.negativestock == 1) { // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableSoyou")+'</center>'], 2);
                            obj.cancel = true;
                        } else if (Wtf.account.companyAccountPref.negativestock == 2) { // Warn Case
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn) {
                                if (btn=="yes") {
                                    obj.cancel = false;
                                } else {
                                    rec.set("quantity",originalQuantity);
                                    rec.set("baseuomquantity",originalQuantity*originalBaseuomrate);
                                    rec.set("baseuomrate",originalBaseuomrate);                                  
                                    obj.cancel = true;
                                    return false;
                                }
                            },this); 
                        }
                    }
                }
            }            
        } else if (this.isCash &&this.isCustomer&&(obj.field=="quantity"||obj.field=="baseuomrate")&&obj.record.data['productid'].length>0&&!this.isQuotation&&!this.isOrder) {
            if (obj.field=="quantity") {
                var originalQuantity = obj.originalValue;
                var newQuantity = obj.value;
                var originalBaseuomrate = obj.record.data['baseuomrate'];
                var newBaseuomrate = obj.record.data['baseuomrate'];
            } else if (obj.field=="baseuomrate") {
                var originalQuantity = obj.record.data['quantity'];
                var newQuantity = obj.record.data['quantity'];
                var originalBaseuomrate = obj.originalValue;
                var newBaseuomrate = obj.value;
            }
            prorec = this.productComboStore.getAt(this.productComboStore.find('productid',obj.record.data.productid));
            if (prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part') {
                var availableQuantity = prorec.data.quantity;
                var quantity = 0;
                if (this.editTransaction) { // In Edit Case Check product quantity is greater than available quantity when selecting quantity                                  
                    var copyquantity = 0;                    
                    this.store.each(function(rec) {
                        if (rec.data.productid == prorec.data.productid) {
                            copyquantity = copyquantity + (rec.data.copyquantity*rec.data.baseuomrate);
                            var ind=this.store.indexOf(rec);
                            if (ind != -1) {
                                if (ind!=obj.row) {                            
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }
                        }
                    },this);
                    quantity = quantity + (newQuantity*newBaseuomrate);
                    availableQuantity = availableQuantity + copyquantity;   
                    if (this.editTransaction && !this.copyInv && availableQuantity < quantity) {
                        if (Wtf.account.companyAccountPref.negativestock == 1) { // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.common.stockCI")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+' is '+availableQuantity+'<br><br><center>So you cannot proceed ?</center>'], 2);
                            obj.cancel=true;   
                        } else if (Wtf.account.companyAccountPref.negativestock == 2) { // Warn Case
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn) {
                                if (btn == "yes") {
                                    obj.cancel = false;
                                } else {
                                    rec.set("quantity",originalQuantity);
                                    rec.set("baseuomquantity",originalQuantity*originalBaseuomrate);
                                    rec.set("baseuomrate",originalBaseuomrate);
                                    obj.cancel = true;
                                    return false;
                                }
                            },this); 
                        }
                    } //for edit transaction in in without trading flow and with inventory
                } else { // In normal Case Check product quantity is greater than available quantity when selecting quantity                                
                    this.store.each(function(rec) {
                        if (rec.data.productid == prorec.data.productid) {
                            var ind = this.store.indexOf(rec);
                            if (ind != -1) {
                                if (ind != obj.row) {
                                    quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);
                                }
                            }
                        }
                    },this);
                    quantity = quantity + (newQuantity*newBaseuomrate);                    
                    if ((!this.editTransaction || this.copyInv) &&  availableQuantity < quantity) {
                        if (Wtf.account.companyAccountPref.negativestock == 1) { // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableSoyoucannotproceed")+'</center>'], 2);
                            obj.cancel = true;   
                        } else if (Wtf.account.companyAccountPref.negativestock == 2) { // Warn Case
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCSareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn) {
                                if (btn == "yes") {
                                    obj.cancel = false;
                                } else {
                                    rec.set("quantity",originalQuantity);
                                    rec.set("baseuomquantity",originalQuantity*originalBaseuomrate);
                                    rec.set("baseuomrate",originalBaseuomrate);
                                    obj.cancel = true;
                                    return false;
                                }
                            },this); 
                        }
                    }
                }
            }
        }
        if (this.isNote) {         
            if (obj.field=="typeid" && (obj.value==0)) { // Discount
                rec.set('remquantity',0);
                rec.set('discamount',0);
            }

            if (obj.field=="remquantity") {// Discount
                if (rec.data['typeid'] == 0) {
                    obj.cancel = true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.YoucannotenterquantitywhenDiscountnotetypeisselectedYouneed") ], 2);
                    rec.set('remquantity',0);
                    rec.set('discamount',0);
                } else {
                    if (rec.data['remainingquantity']<obj.value) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productname']+ ' is '+rec.data.remainingquantity], 2);
                        obj.cancel = true;
                        rec.set('remquantity',0);
                        rec.set('discamount',0);
                    } else {
                        var qty = obj.value;
                        var rate = rec.data['rate'];
                        var prDiscount = rec.data['prdiscount'];
                        var prTax = rec.data['prtaxpercent'];
                        //To do - Need to check quantity checks for multi UOM change
                        var prTaxAmount = rec.data['taxamount'];
                        var amt = qty * rate;
                        if (rec.data['partamount'] !=  0) {
                            amt = amt * (rec.data['partamount']/100);
                        }
                        if (prDiscount > 0) {
                            if (rec.data['discountispercent'] == 1) {
                                amt = amt - ((amt * prDiscount) / 100);
                            } else {
                                amt = amt - prDiscount;
                            }
                        }
                            
                        if (prTax > 0) {
                            amt = amt + (prTaxAmount);
                        }
                        rec.set('discamount',amt);
                    }
                }
            }
            if (obj.field == "typeid") {
                if (rec.data['typeid'] == 0) { // Discount
                    rec.set('remquantity',0);
                    rec.set('discamount',0);
                }
            }
            if (obj.field == "discamount") {
                if (rec.data['orignalamount'] < obj.value) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Remainingamountfortheselectedproductis")+' '+WtfGlobal.getCurrencySymbol()+" "+(rec.data['amount'])], 2);
                    obj.cancel = true;
                    rec.set('discamount',0);
                }
            }
        }
    },
    
    addBlankRow: function() {
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if (f.name!='rowid') {
                blankObj[f.name]='';
                if (!Wtf.isEmpty(f.defValue)) {
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
                }
            }
        }
        var newrec = new Record(blankObj);
        this.store.add(newrec);
    },
    
    updateRow: function(obj) {
        if (obj != null) {
            this.productComboStore.clearFilter(); // Issue 22189
            var rec = obj.record;
            if (obj.field=="prdiscount" && (rec.data.discountispercent == 1) && obj.value >100) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Discountcannotbegreaterthan100")], 2);
                rec.set("prdiscount",0);
            } else {
                this.fireEvent('datachanged',this);
            }
            
            if(obj.field=="discountispercent" && obj.value == 1 && (rec.data.prdiscount > 100)){
                
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Percentdiscountcannotbegreaterthan100")], 2);
                rec.set("discountispercent",0);
            } else {
                this.fireEvent('datachanged',this);
            }
            if(obj.field=="baseuomrate"){
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copybaseuomrate-obj.value)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copybaseuomrate)*((rec.data.copyquantity==="")?1:rec.data.copyquantity));
                
                var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                var productuomid = "";
                if(productComboIndex >=0){
                    prorec = this.productComboStore.getAt(productComboIndex);
                    productuomid = prorec.data.uomid;
                    if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                        obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.value);
                    } else {
                        obj.record.set("baseuomrate", 1);
                    }                      
                }
            }
            if(obj.field=="uomid"){
                var prorec = null;
                var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                var productuomid = "";
                if(productComboIndex >=0){
                    prorec = this.productComboStore.getAt(productComboIndex);
                    productuomid = prorec.data.uomid;
                    if(productuomid != obj.value){
                        //To do - Need to take rate from new window
                        //                      this.showPriceWindow.createDelegate(this,[rec, obj],true);
                        obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                    } else {
                        obj.record.set("baseuomrate", 1);
                        obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                    }
                }
            }
            if(obj.field=="productid"){
                var customFieldArr = GlobalColumnModelForProduct[this.moduleid];
                if(customFieldArr !=null && customFieldArr != undefined){
                    for(var k=0;k<customFieldArr.length;k++){
                        rec.set(customFieldArr[k].fieldname,"");
                    }
                }
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.quantity*(-1))*rec.data.baseuomrate);
                else
                    rec.set("changedQuantity",(rec.data.quantity)*rec.data.baseuomrate);
                
                Wtf.Ajax.requestEx({
                    url:"ACCProduct/getIndividualProductPrice.do",
                    params:{
                        productid: obj.value,
                        affecteduser: this.affecteduser,
                        currency: this.parentObj.Currency.getValue(),
                        quantity: obj.record.data.quantity,
                        transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                        carryin : (this.isCustomer)? false : true
                    }
                }, this,function(response){
                    var datewiseprice =response.data[0].price;
                    this.isPriceListBand = response.data[0].isPriceListBand;
                    this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                    this.priceSource = response.data[0].priceSource;
                    this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                    this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                    this.defaultPrice = datewiseprice;
                    
                    obj.record.set("oldcurrencyrate",1);
                    for(var i=1;i<response.data.length;i++){
                        var dataObj=response.data[i];
                        var key=dataObj.key;
                        if (key != undefined) {
                            for(var k=0;k<obj.grid.colModel.config.length;k++){
                                if(obj.grid.colModel.config[k].dataIndex==key){
                                    var store=obj.grid.colModel.config[k].editor.field.store;
                                    if(store)
                                        store.clearFilter();
                                    obj.record.set(key,dataObj[key]);
                                }
                            }
                        }
                    }
                   
                    var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.value, 'productid');
                    var productname = "";
                    var proddescription = "";
                    var productuomid = undefined;
                    var productsuppliernumber = "";
                    var shelfLocation = "";
                    var isAsset = "";
                    var prorec = null;
                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                    var protaxcode = "";
                    if(productComboIndex >=0){
                        prorec = this.productComboStore.getAt(productComboIndex);
                        productname = prorec.data.productname;
                        proddescription = prorec.data.desc;
                        productuomid = prorec.data.uomid;
                        productsuppliernumber= prorec.data.supplierpartnumber;
                        shelfLocation = prorec.data.shelfLocation;
                        protaxcode = prorec.data[acctaxcode];
                        isAsset = prorec.data.isAsset;
                    }
                    obj.record.set("desc",proddescription);
                    obj.record.set("uomid", productuomid);
                    obj.record.set("supplierpartnumber",productsuppliernumber);
                    obj.record.set("shelfLocation",shelfLocation);
                    obj.record.set("productname", productname);
                    obj.record.set("isAsset", isAsset);
                  
                    if (this.isVolumeDisocunt) {
                        if (obj.record.data.quantity != "") {
                            obj.record.set("rate", this.defaultPrice);
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                        } else {
                            obj.record.set("rate", "");
                            obj.record.set("priceSource", "");
                        }
                    } else if (this.isPriceListBand) {
                        if (this.isPriceFromUseDiscount) {
                            if (obj.record.data.quantity != "") {
                                obj.record.set("rate", this.defaultPrice);
                                obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount + ": " + WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                            } else {
                                obj.record.set("rate", "");
                                obj.record.set("priceSource", "");
                            }
                        } else {
                            obj.record.set("rate", this.defaultPrice);
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                        }
                    } else {
                        if(datewiseprice==0){
                            if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.addprice) && !this.isRequisition){//permissions
                                rec.set("productname",productname);
                                //Wtf.Msg.confirm(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+" <b>"+productname+"</b> "+' '+WtfGlobal.getLocaleText("acc.field.isnotsetDoyouwanttosetitnow"),
                                  //  this.showPriceWindow.createDelegate(this,[rec, obj],true), this);
                            //}else{
                             //   WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pricefortheproduct")+" <b>"+productname+"</b>"+' '+WtfGlobal.getLocaleText("acc.field.isnotset")], 2);
                            }    
                        } else {
                            // setting datewise price according to currency exchange rate - 
                        
                            var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
                            var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
                            var modifiedRate;
                            if(rate!=0.0)
                                modifiedRate=(parseFloat(datewiseprice)*parseFloat(rate))/parseFloat(oldcurrencyrate);
                            else
                                modifiedRate=(parseFloat(datewiseprice)/parseFloat(oldcurrencyrate));
                        
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.quantity != "") {
                                    obj.record.set("rate", this.defaultPrice);
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                obj.record.set("rate", modifiedRate);
                                obj.record.set("productname", productname);
                            }
                        }
                    }

                    if(!(obj.soflag)){
                        obj.record.set("baseuomquantity",1);
                        obj.record.set("quantity",1);
                        if(this.parentObj && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() == true) {
                            obj.record.set("prtaxid", protaxcode);
                        } else {
                            obj.record.set("prtaxid", "");
                        }
                        var taxamount = this.setTaxAmountAfterSelection(obj.record);
                        obj.record.set("taxamount",taxamount);

                        this.fireEvent("productselect", obj.value);
                    }
                    
                    this.fireEvent('datachanged',this);
                }, function(){

                    });
            }else if(obj.field=="quantity"){
                rec=obj.record;
                
                if ((!this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBands) || (this.isCustomer && Wtf.account.companyAccountPref.productPricingOnBandsForSales)) {
                    Wtf.Ajax.requestEx({
                        url:"ACCProduct/getIndividualProductPrice.do",
                        params: {
                            productid: obj.record.data.productid,
                            affecteduser: this.affecteduser,
                            forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                            currency: this.parentObj.Currency.getValue(),
                            quantity: obj.record.data.quantity,
                            transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                            carryin : (this.isCustomer)? false : true
                        }
                    }, this,function(response) {
                        var datewiseprice =response.data[0].price;
                        this.isPriceListBand = response.data[0].isPriceListBand;
                        this.isVolumeDisocunt = response.data[0].isVolumeDisocunt;
                        this.priceSource = response.data[0].priceSource;
                        this.isPriceFromUseDiscount = response.data[0].isPriceFromUseDiscount;
                        this.priceSourceUseDiscount = response.data[0].priceSourceUseDiscount;
                        this.defaultPrice = datewiseprice;
                        
                        if (this.isVolumeDisocunt) {
                            obj.record.set("rate", this.defaultPrice);
                            obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSource);
                        } else if (this.isPriceListBand) {
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.quantity != "") {
                                    obj.record.set("rate", this.defaultPrice);
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount + ": " + WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                obj.record.set("rate", this.defaultPrice);
                                obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.pricingBands") + " - " + this.priceSource);
                            }
                        } else {
                            if (this.isPriceFromUseDiscount) {
                                if (obj.record.data.quantity != "") {
                                    obj.record.set("rate", this.defaultPrice);
                                    obj.record.set("priceSource", WtfGlobal.getLocaleText("acc.field.priceListVolumeDiscount") + " - " + this.priceSourceUseDiscount);
                                } else {
                                    obj.record.set("rate", "");
                                    obj.record.set("priceSource", "");
                                }
                            } else {
                                obj.record.set("rate", this.defaultPrice);
                                obj.record.set("priceSource", "");
                            }
                        }
                        this.fireEvent('datachanged',this);
                    }, function(response) {
                        
                        });
                }
                
                if(this.isCustomer)
                    rec.set("changedQuantity",(rec.data.copyquantity-obj.value)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                else
                    rec.set("changedQuantity",(obj.value-rec.data.copyquantity)*((rec.data.copybaseuomrate==="")?1:rec.data.copybaseuomrate));
                
                if(((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && this.fromOrder&&!(this.editTransaction||this.copyInv))||(this.isEdit && rec.data.linkid !="")) {  
                    if(obj.value > rec.data.copyquantity){
                        var msg = "Product Quantity entered in CI is exceeds from original quantity mentioned in SO. "
                        if (this.isCustomer) {
                            if(this.isOrder && !this.isQuotation){
                                msg = "Product Quantity entered in SO is exceeds from original quantity mentioned in PO/CQ. "
                            } else if(this.isQuotation) {
                                msg = "Product Quantity entered in CQ is exceeds from original quantity mentioned in VQ. "
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = "Product Quantity entered in CI is exceeds from original quantity mentioned in SO/DO/CQ. "
                                } else {
                                    msg = "Product Quantity entered in CI is exceeds from original quantity mentioned in CQ/SO. "
                                }
                                
                            }
                            
                        } else {
                            if(this.isOrder){
                                 msg = WtfGlobal.getLocaleText("acc.field.ProductRFQexceedsfromoriginalquantitymentionedinPR");
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = "Product Quantity entered in VI is exceeds from original quantity mentioned in selected PO/GR/VQ. "
                                } else {
                                    msg = "Product Quantity entered in VI is exceeds from original quantity mentioned in PO/GR/VQ. "
                                }
                                
                            }
                            
                        }
                        obj.record.set(obj.field, obj.originalValue);
                        Wtf.MessageBox.alert("Alert",msg,{
                                
                            },this)
                        
                    }else if(obj.value!=rec.data.copyquantity) {
                        var msg = "Product Quantity entered in Invoice is different from original quantity mentioned in SO. Do you want to continue?"
                        if (this.isCustomer) {
                            if(this.isOrder && !this.isQuotation){
                                msg = "Product Quantity entered in SO is different from original quantity mentioned in PO/CQ. Do you want to continue?"
                            } else if(this.isQuotation) {
                                msg = "Product Quantity entered in CQ is different from original quantity mentioned in VQ. Do you want to continue?"
                            }else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = "Product Quantity entered in CI is different from original quantity mentioned in SO/DO/CQ. Do you want to continue?"
                                } else {
                                    msg = "Product Quantity entered in CI is different from original quantity mentioned in CQ/SO. Do you want to continue?"
                                }
                                
                            }
                            
                        } else {
                            if(this.isOrder){
                                msg = WtfGlobal.getLocaleText("acc.field.ProductRFQdifferentPRcontinue");
                            } else {
                                if(Wtf.account.companyAccountPref.withinvupdate){
                                    msg = "Product Quantity entered in VI is different from original quantity mentioned in selected PO/GR/VQ. Do you want to continue?"
                                } else {
                                    msg = "Product Quantity entered in VI is different from original quantity mentioned in VQ/PO. Do you want to continue?"
                                }
                                
                            }
                            
                        }
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"),msg,function(btn){
                            if(btn!="yes") {
                                obj.record.set(obj.field, obj.originalValue);
                                obj.record.set("baseuomquantity",obj.originalValue*obj.record.get("baseuomrate"));
                            }
                        },this)
                    }
                }
                if((obj.record.data["baseuomquantity"])==0){
                    if(obj.record.data.copyquantity!=undefined){                    
                        var deletedData=[];
                        var newRec=new this.deleteRec({
                            productid:obj.record.data.productid,
                            productname:obj.record.data.productname,    
                            productquantity:obj.record.data.copyquantity,
                            productbaseuomrate:obj.record.data.baseuomrate                            
                        });                            
                        deletedData.push(newRec);
                        this.deleteStore.add(deletedData);                            
                    }
                    this.store.remove(obj.record);
                }
                var productComboIndex = WtfGlobal.searchRecordIndex(this.productComboStore, obj.record.get('productid'), 'productid');
                var productuomid = "";
                if(productComboIndex >=0){
                    prorec = this.productComboStore.getAt(productComboIndex);
                    productuomid = prorec.data.uomid;
                    if(obj.record.get("uomid")!=undefined && productuomid != obj.record.get("uomid")){
                        obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                    } else {
                        obj.record.set("baseuomrate", 1);
                        obj.record.set("baseuomquantity", obj.record.get("quantity")*obj.record.get("baseuomrate"));
                    }
                }
                var isnotdecimalvalue=true;
                if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                    if (prorec.data.isSerialForProduct) {
                        var v = obj.record.data.quantity;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub!=undefined && sub.length > 0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                            isnotdecimalvalue=false;
                            obj.record.set("quantity", obj.originalValue);
                            obj.record.set("baseuomquantity", obj.originalValue*obj.record.get("baseuomrate"));
                        }
                    }
                }
                if((this.isFixedAsset || (this.isLeaseFixedAsset && !this.isQuotation)) && isnotdecimalvalue){
                    var productid = rec.get('productid');
                    var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
                    if(productComboRecIndex >=0){
                        var proRecord = this.productComboStore.getAt(productComboRecIndex);
                        if(proRecord.get('isAsset') && obj.record.get("quantity") != 0)
                            this.callFixedAssetDetailsWindow(obj.record,proRecord);
                        }
                      
                    }
                      
                this.fireEvent('datachanged',this);
                }
            
            if(this.isNote){ 
                if(obj.field=="typeid"){
                    if(rec.data['typeid']!=0){
                        rec.set('accountId',rec.data['salesAccountId']);
                    }else{
                        rec.set('accountId',rec.data['discountAccountId']);
                    }
                }
            } 
        }
        if(this.store.getCount()>0&&this.store.getAt(this.store.getCount()-1).data['productid'].length<=0)
            return;
        if(!this.isNote && (!this.soLinkFlag)) {
            this.addBlankRow();            
        }
       
    },
    
    callFixedAssetDetailsWindow: function(record,productRec,readOnly) {
        var quantity = record.get('quantity');
        
        this.FADetailsGrid = new Wtf.account.FADetails({
            title: 'Asset Details',
            quantity: quantity,
            modal: true,
            isCustomer: this.isCustomer,
            isLeaseFixedAsset: this.isLeaseFixedAsset,
            isFixedAsset: this.isFixedAsset,
            isLinkedFromReplacementNumber: this.isLinkedFromReplacementNumber,
            isLinkedFromCustomerQuotation: this.isLinkedFromCustomerQuotation,
            layout: 'border',
            assetRec: productRec,
            lineRec: record,
            assetDetailsArray: record.get('assetDetails'),
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            fromPO: this.fromPO,
            isFromSalesOrder: this.isOrder&&this.isCustomer,
            isEdit: this.isEdit,
            moduleid: this.moduleid,
            isInvoice: true,
            width: 950,
            readOnly: readOnly,
            height: 500,
            resizable: false
        });
        
        this.FADetailsGrid.show();
        
        this.FADetailsGrid.on('beforeclose',function(panel) {
            if (panel.isFromSaveButton) {
                record.set("assetDetails", panel.assetDetails);
                record.set("profitLossAmt", panel.profitLossAmtOnSelling);
            }
        }, this);
    },
    
    calTaxAmount: function(rec) {
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if (rec.data.partamount != 0) {
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        var discount = 0; // origionalAmount*rec.data.prdiscount/100
        if(!(this.isNote||this.readOnly)) {
            origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        }
        
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount = (origionalAmount * rec.data.prdiscount) / 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
//        var discount=origionalAmount*rec.data.prdiscount/100
        var val=(origionalAmount)-discount;
        var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
                taxpercent=taxrec.data.percent;
            }
        return (val*taxpercent/100);

    },
    
    calTaxAmountWithoutExchangeRate: function(rec) {
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if (rec.data.partamount != 0) {
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        var discount = 0;
        
        if (rec.data.prdiscount > 0) {
            if (rec.data.discountispercent == 1){
                discount = (origionalAmount * rec.data.prdiscount) / 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
        var val = (origionalAmount)-discount;
        var taxpercent = 0;
        var index = this.taxStore.find('prtaxid',rec.data.prtaxid);
        if (index >= 0) {
            var taxrec = this.taxStore.getAt(index);
            taxpercent = taxrec.data.percent;
        }
        return (val*taxpercent/100);

    },
    
    setTaxAmountAfterSelection: function(rec) {
        var discount = 0;
        var origionalAmount = rec.data.rate*rec.data.quantity ;
        if(rec.data.partamount != 0){
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount = origionalAmount * rec.data.prdiscount/ 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
        var val=origionalAmount-discount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=taxrec.data.percent;
        }
        var taxamount= (val*taxpercent/100);
        return taxamount;
        
    },
    
    setTaxAmount: function(v,m,rec) {
        var taxamount= this.calTaxAmount(rec);
        rec.set("taxamount",taxamount);
        return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
    },
    
    setTaxAmountWithotExchangeRate: function(v,m,rec) {
        var taxamount= parseFloat(getRoundedAmountValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
        if (rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == "") {
            taxamount = 0;
        }
        rec.set("taxamount",taxamount);
        return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
    },
    
    calAmount: function(v,m,rec) {
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if (rec.data.partamount != 0) {
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        
        var discount = 0; // origionalAmount*rec.data.prdiscount/100
        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        
        if (rec.data.prdiscount > 0) {
            if (rec.data.discountispercent == 1) {
                discount = (origionalAmount * rec.data.prdiscount) / 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
        
        var val = (origionalAmount)-discount; // rec.data.oldcurrencyrate
        
        rec.set("amountwithouttax",val);
        
        var taxamount = 0;
        if (rec.data.taxamount) {
            taxamount = rec.data.taxamount;
        }
        val += taxamount;

        rec.set("amount",val);
        if(this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount=="")) {
            rec.set("orignalamount",val);
        }
        
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    },
    
    calAmountWithoutExchangeRate: function(v,m,rec) {
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if (rec.data.partamount != 0) {
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        
        var discount = 0; // origionalAmount*rec.data.prdiscount/100   
        if (rec.data.prdiscount > 0) {
            if (rec.data.discountispercent == 1) {
                discount = (origionalAmount * rec.data.prdiscount) / 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
        
        var val = (origionalAmount)-discount; // rec.data.oldcurrencyrate  
        rec.set("amountwithouttax",val);
        var taxamount = 0;
        if (!isNaN(rec.data.taxamount)) {
            taxamount = rec.data.taxamount;
        }
        val = parseFloat(val)+parseFloat(taxamount);

        rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
        if (this.isQuotationFromPR && val!==0 && (rec.data.orignalamount==undefined || rec.data.orignalamount=="")) {
            rec.set("orignalamount",val);
        }
        
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    },
    
    calAmountWithExchangeRate: function(value,rec) {
        var rate = ((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
        var oldcurrencyrate = ((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
        var v;
        if (rate!=0.0) {
            v=(parseFloat(value)*parseFloat(rate))/parseFloat(oldcurrencyrate);
        } else {
            v=(parseFloat(value)/parseFloat(oldcurrencyrate));
        }
        if(isNaN(v)) {
            return value;
        }
        
        v = WtfGlobal.conventInDecimalWithoutSymbol(v)
        return v;
    },
    
    calSubtotal: function() {
        var subtotal = 0;
        var total = 0;
        var count=this.store.getCount();
        for (var i=0; i<count; i++) {
            total = parseFloat(this.store.getAt(i).data['amount']);
            subtotal += total;
        }
        return subtotal;
    },
    
    addBlank: function() {
        this.addBlankRow();
    },
    
    setGridDiscValues: function() {
        this.store.each(function(rec) {
            if(!this.editTransaction) {
                rec.set('prdiscount',0)
            }
        },this);
    },
    
    setGridProductValues: function(datachangeflag, custchangeflag) {
        var rate;
        this.pronamearr = [];
        var productid = "";
        
        if (this.store.getCount() > 0) {
            this.store.each(function(record) {
                var recproduct = record.data.productid;
                if (recproduct != undefined && recproduct != "") {
                    productid = productid + recproduct + ",";
                }
            });

            productid = productid.substring(0, (productid.length - 1) );
        
            Wtf.Ajax.requestEx({
                url:"ACCProduct/getIndividualProductPrice.do",
                params: {
                    productid: productid,
                    affecteduser: this.affecteduser,
                    currency: this.parentObj.Currency.getValue(),
                    transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                    carryin : (this.isCustomer)? false : true
                }
            }, this,function(response) {
                var obj = response.data;
                for (var i=0; i < obj.length ; i++) {
                    var datewisepriceResp =obj[i].price;
                    var productidResp =obj[i].productid;
                    var index = this.store.find('productid', productidResp);

                    // Set rate, if not set array value with productname
                    if (index > -1) {
                        var record = this.store.getAt(index);

                        var proindex = this.productComboStore.find('productid', productidResp);
                        var prorec=this.productComboStore.getAt(proindex);
                        if (datewisepriceResp == 0) {
                            this.pronamearr.push(prorec.get('productname'))
                        }
                        record.set("rate",datewisepriceResp);

                        // Set old currency rate
                        if (this.editTransaction) {
                            record.set('oldcurrencyrate',record.get('currencyrate'));
                        }

                        // Case of copy invoice
                        if ((this.copyInv&&prorec.data.quantity<(record.data.quantity*record.data.baseomrate)&&prorec.data.type!="Service")) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+prorec.data.productname+WtfGlobal.getLocaleText("acc.field.is")+' '+prorec.data.quantity], 2);
                            record.set("quantity",0);
                            record.set("baseuomquantity",0);
                        }
                    }
                }
                
                this.fireEvent('pricestoreload', this.pronamearr,this);
            });
        
        } else {
            this.fireEvent('pricestoreload', this.pronamearr,this);
        }
    },
    
    getProductDetails: function() {
        this.store.each(function(rec) { // converting in home currency
            if (rec.data.rowid == undefined) {
                rec.data.rowid = '';
            }
        },this);
        
        var arr = [];
        this.store.each(function(rec) {
            var taxpercent = 0;
            var index = this.taxStore.find('prtaxid',rec.data.prtaxid);
            if (index >= 0) {
               var taxrec = this.taxStore.getAt(index);
                taxpercent = taxrec.data.percent;
                rec.set('taxpercent',taxpercent);
            }
            rec.data[CUSTOM_FIELD_KEY] = Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
            rec.data[CUSTOM_FIELD_KEY_PRODUCT] = Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, this.moduleid).substring(20));
            arr.push(this.store.indexOf(rec));
        }, this);
        
        var jarray = WtfGlobal.getJSONArray(this,false,arr);
        
        return jarray;
    },
    
    checkDetails: function (grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
        return v;
    },
    
    getCMProductDetails: function() {
        var arr = [];
        var selModel = this.getSelectionModel();
        var len = this.productComboStore.getCount();
        for (var i=0; i<len; i++) {
            if (selModel.isSelected(i)) {
                var rec = selModel.getSelected();
                if (rec.data.typeid==2 || rec.data.typeid==3) 
                // To do - Need to check quantity checks for multi UOM change
                if (rec.data.remquantity==0 && rec.data.type!="Non-Inventory Part" && rec.data.type!="Service") {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseenterthequantityofproduct")+rec.data.productname+WtfGlobal.getLocaleText("acc.field.youwanttoreturn") ], 2);
                    return "Error";
                }
            
            rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
            arr.push(i);
            }
            // arr.push(i); moved to above line cos of issue no: 20258
        }
        return WtfGlobal.getJSONArray(this,true,arr);
    },
    
    loadPOProduct: function() {
        if (this.store.getCount() > 0) {
            this.fireEvent("productselect", this.store.getAt(0).get("productid"));
        }
      
        if (this.fromPO)
            this.store.each(function(rec) {
                var taxamount = rec.get('rowTaxAmount'); // this.calTaxAmount(rec);
                rec.set("taxamount",taxamount);
                if (rec.data.rate === "") {
                    var result = this.productComboStore.find('productid',rec.data.productid);
                    if (result >= 0) {
                        var prorec=this.productComboStore.getAt(result);
                        rec.set("rate",prorec.data.initialprice);			
                    }
                }
            },this);
            
            if (this.isCustomer && this.fromOrder && !this.isNote && !this.readOnly && !this.isOrder)
                this.checkSOLinkedProducts();
            if (this.soLinkFlag && this.isOrder ) { // Allow soLinkFlag for Generate SO from PO,Generate PO from SO and Generate CQ using VQ for showing correct prices of products
                this.store.each(function(rec) {
                    this.fireEvent('afteredit', {
                        field: 'productid',
                        value: rec.data.productid,
                        record: rec,
                        soflag: true
                    });                
                },this);
                this.soLinkFlag = false;
            }
    },
    
    checkSOLinkedProducts: function() {
        var msgBox = 0, msg = "";
        if (this.store.data.length) { // Check Qty mentioned in SO/QO is greater than available quantity
            var storeData = [];
            var recordSet = [];
            storeData = this.store.data.items;
            this.store.removeAll();
            for (var count=0; count<storeData.length; count++) {
                var record = storeData[count];
                recordSet[count] = record;
                var quantity = 0;
                this.store.each(function(rec) {
                    if (rec.data.productid == record.data.productid) {
                        quantity = quantity + (rec.data.quantity*rec.data.baseuomrate);                                                     
                    }
                },this);
                quantity = quantity + (record.data.quantity*record.data.baseuomrate);
                var result = this.productComboStore.find('productid',record.data.productid);
                if (result >= 0) {
                    var prorec = this.productComboStore.getAt(result);
                    if (!this.editTransaction && !Wtf.account.companyAccountPref.withinvupdate && prorec.data.type!='Service' && quantity > prorec.data.quantity) {
                        if (msg == "") {
                            msg = record.data.productname+" in "+record.data.billno;
                        } else {
                            msg = msg+","+record.data.productname+" in "+record.data.billno;
                        }
                        msgBox = 1;
                    } else {
                        this.store.add(record);
                    }
                }
            }
        }
        
    	if (!Wtf.account.companyAccountPref.withinvupdate&&msgBox == 1) {
            if (Wtf.account.companyAccountPref.negativestock == 1) { // Block case
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableinstockQtyfor")+' '+msg+'.<br>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                this.store.removeAll(); 
            } else if (Wtf.account.companyAccountPref.negativestock == 2) { // Warn Case
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninCIareexceedingthequantityavailableDoyouwish")+'</center>' , function(btn) {
                    if (btn == "yes") {
                        this.store.removeAll();
                        this.store.add(recordSet);
                        this.addBlankRow();
                    } else {
                        Wtf.getCmp(this.parentCmpID).loadStore(); 
                        return false;
                    }
                },this); 
            }
        }
    },
    
    loadPOGridStore: function(recids, flag, VQtoCQ,linkingFlag, isForInvoice) {
        this.store.load({
            params:{
                bills:recids,
                mode:43,
                closeflag:true,
                dtype: VQtoCQ?"report" : "trans",
                linkingFlag:linkingFlag,
                isForInvoice:isForInvoice
            }
        });
        this.soLinkFlag = flag;
    },
    
    showPriceWindow: function(btn,text,rec, obj) {
        if (btn!="yes") {
            return;
        }
        callPricelistWindow(rec,"pricewindow",!this.isCustomer,this.billDate);
    },

    setCurrencyid: function(currencyid,rate,symbol,rec,store) {
        this.symbol = symbol;
        this.currencyid = currencyid;
        this.rate = rate;
        for (var i=0; i<this.store.getCount(); i++) {
            this.store.getAt(i).set('currencysymbol',this.symbol);
            this.store.getAt(i).set('currencyrate',this.rate);
        }
        this.getView().refresh();
    },
    
    setCurrencyAmount: function(amount) {
        return amount;
    },
    
    isAmountzero: function(store) {
        var amount;
        var selModel = this.getSelectionModel();
        var len = this.productComboStore.getCount();
        for (var i=0; i<len; i++) {
            if (selModel.isSelected(i)) {
                amount = store.getAt(i).data["discamount"];
                if (amount <= 0) {
                    return true;
                }
            }
        }
        return false;
    }
});