//Creating the Product selection Window Component

Wtf.account.PreferredProductSelectionWindow = function(config) {
    this.id = config.id;
    this.isCustomer = config.isCustomer;
    this.moduleid = config.moduleid;
    this.parentCmpID = config.parentCmpID;
    this.heplmodeid = config.heplmodeid;
    this.butnArr = [];
    this.parentCombo = config.parentCombo;
    this.isConsignment = config.isConsignment;
    this.consignmentProductSelectionArray = config.consignmentProductSelectionArray;
    this.isStoreLocationEnable = config.isStoreLocationEnable;
    this.isJobWorkOrderReciever = config.isJobWorkOrderReciever;
    this.isJobWorkInReciever = config.isJobWorkInReciever;
    this.customerid = config.customerid,
    this.Currency = config.Currency,
    this.custWarehouse = config.custWarehouse,
    this.closeflag = config.closeflag
    this.warehouseId = config.warehouseId;
    this.isFromInventorySide = (config.isFromInventorySide != undefined && config.isFromInventorySide != "") ? config.isFromInventorySide : false;
    this.modulename = (config.modulename != undefined && config.modulename != "") ? config.modulename : "";
    this.isWastageApplicable = (config.isWastageApplicable != undefined && config.isWastageApplicable != "") ? config.isWastageApplicable : false;
    this.mappedProducts = config.mappedProducts;
    this.customerid = (config.customerid != undefined) ? config.customerid : "";
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.add"),
        scope: this,
        handler: function() {
            if (this.productgrid.getStore().getCount() > 0) {
                var recs = this.productgrid.getStore().data.items;

                var jsonString = "";
                for (var cnt = 0; cnt < recs.length; cnt++) {
                    jsonString += this.getFilterJson(recs[cnt].data);
                }

                jsonString = jsonString.substring(0, jsonString.length - 1);
                if (jsonString != "") {
                    jsonString = "[" + jsonString + "]"
                }
                this.parentCombo.customJSONString = jsonString;
            }

            this.closeWin();

        }
    }, {
        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler: function() {
            this.closeWin();
        }
    });

    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.PreferredProductSelectionWindow.superclass.constructor.call(this, config);
    this.productgrid.on('cellclick', this.onCellClick, this);
}


Wtf.extend(Wtf.account.PreferredProductSelectionWindow, Wtf.Window, {
    onRender: function(config) {
        Wtf.account.PreferredProductSelectionWindow.superclass.onRender.call(this, config);

        this.createDisplayGrid();
        this.add(this.productgrid);
        this.on("show", function() {
            var arrID = this.parentCombo.getValue().split(',');
            this.productStoreGrid = this.productStoreGrid                    // for consignment invoice we use simple store and for Product selection window we use Grouping Store
            this.productStoreGrid.on("beforeload", function() {
                var categoryid = this.typeEditor.getValue();
                if (categoryid == 'All') {
                    categoryid = 'All'
                } else {
                    categoryid = categoryid;
                }
                this.productStoreGrid.baseParams = {
                    moduleid: this.moduleid, //ERP-9835
                    limit: this.pPageSizeObj.combo.value,
                    ss: this.localSearch.getValue(),
                    type: this.isJobWorkInReciever ? Wtf.producttype.customerInventory : (this.isJobWorkOrderReciever ? Wtf.producttype.customerAssembly : ((Wtf.getCmp("isMaintenanceOrder" + this.heplmodeid + this.parentCmpID) && Wtf.getCmp("isMaintenanceOrder" + this.heplmodeid + this.parentCmpID).getValue()) ? (Wtf.getCmp("maintenanceNumberCombo" + this.heplmodeid + this.parentCmpID).getValue() != "" ? Wtf.producttype.service : "") : '')), // ERP-11098 [SJ]
                    categoryid: categoryid,
//                    ids:this.isConsignment && this.moduleid ==Wtf.Acc_ConsignmentInvoice_ModuleId?null:this.productList.getValue(),
                    excludeParent: true,
                    excludeProductIds: this.consignmentProductSelectionArray != undefined ? this.consignmentProductSelectionArray.toString() : "",
                    isWastageApplicable: this.isWastageApplicable,
                    warehouseid: this.warehouseId,
                    isStoreLocationEnable: this.isStoreLocationEnable,
                    isConsignment: this.isConsignment && this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId,
                    customerid: this.customerid,
                    isCustomer: this.isCustomer,
                    Currency: this.Currency,
                    custWarehouse: this.custWarehouse,
                    closeflag: this.closeflag,
                    ids : arrID
                }
                if (this.pPageSizeObj != undefined) {
                    if (this.pPageSizeObj.combo.value == "All") {
                        var count = this.productStoreGrid.getTotalCount();
                        var rem = count % 5;
                        if (rem == 0) {
                            count = count;
                        } else {
                            count = count + (5 - rem);
                        }
                        this.productStoreGrid.baseParams.limit = count;
                    }
                }
            }, this);

            this.productStoreGrid.load({
                params: {
                    start: 0,
                    limit: this.pPageSizeObj.combo.value,
                    ids: arrID
                }
            }, this);


        }, this);

        this.productStoreGrid.on("load", function() {

            this.productStoreGrid.each(function(rec) {
                var jString = "";
                if (this.parentCombo && this.parentCombo.customJSONString != "" && this.parentCombo.customJSONString != undefined) {
                    jString = this.parentCombo.customJSONString;
                    jString = eval(jString);

                    if (jString != "" && jString != undefined && jString.length >= 0) {
                        for (var length = 0; length <= jString.length; length++) {
                            var JSON = jString[length];
                            for (var key in JSON) {
                                if (JSON.hasOwnProperty(key) && key == rec.data.productid) {
                                    this.setCustomData(rec, JSON[key]);
                                }
                            }

                        }

                    }

                } else if (rec.data['jsonstring'] != undefined && rec.data['jsonstring'] != "") {
                    jString = eval(rec.data['jsonstring']);
                    this.setCustomData(rec, jString);
                }

            }, this);


        }, this);

    },
    createDisplayGrid: function() {
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
        this.productGridRec = Wtf.data.Record.create([
            {
                name: 'productid'
            },
            {
                name: 'productname'
            },
            {
                name: 'desc'
            },
            {
                name: 'uomid'
            },
            {
                name: 'uomname'
            },
            {
                name: "isBatchForProduct"
            },
            {
                name: "isSerialForProduct"
            },
            {
                name: "isRowForProduct"
            },
            {
                name: "isRackForProduct"
            },
            {
                name: "isBinForProduct"
            },
            {
                name: 'packaging'
            },
            {
                name: 'packagingid'
            },
            {
                name: 'warehouse'
            },
            {
                name: 'orderinguomname'
            },
            {
                name: 'transferinguomname'
            },
            {
                name: 'orderinguomid'
            },
            {
                name: 'transferinguomid'
            },
            {
                name: 'orderToStockUOMFactor'
            },
            {
                name: 'transferToStockUOMFactor'
            },
            {
                name: 'ismultipleuom'
            },
            {
                name: 'uomschematype'
            },
            {
                name: 'quantity'
            },
            {
                name: 'baseuomquantity',
                defValue: 1.00
            },
            {
                name: 'amount',
                defValue: 0
            },
            {
                name: 'purchaseprice'
            },
            {
                name: 'saleprice'
            },
            {
                name: 'producttype'
            },
            {
                name: 'type'
            },
            {
                name: 'initialsalesprice'
            },
            {
                name: 'initialquantity',
                mapping: 'initialquantity'
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
                name: 'temp'
            },
            {
                name: 'currencysymbol'
            },
            {
                name: 'rate'
            }, {
                name: 'uom'
            },
            {
                name: 'isActiveItem'
            },
            {
                name: 'venconsignuomquantity'
            },
            {
                name: 'rowid',
                defValue: null
            },
            {
                name: 'billid'
            },
            {
                name: 'billno'
            },
            {
                name: 'Cust_billno'
            },
            {
                name: 'quantity'
            },
            {
                name: 'baseuomquantity',
                defValue: 1.00
            },
            {
                name: 'baseuomname'
            },
            {
                name: 'stockuom'
            },
            {
                name: 'caseuom'
            },
            {
                name: 'inneruom'
            },
            {
                name: 'caseuomvalue'
            },
            {
                name: 'inneruomvalue'
            },
            {
                name: 'baseuomrate',
                defValue: 1.00
            },
            {
                name: 'copyquantity',
                mapping: 'quantity'
            },
            {
                name: 'rate',
                defValue: 0
            },
            {
                name: 'rateinbase'
            },
            {
                name: 'partamount',
                defValue: 0
            },
            {
                name: 'discamount'
            },
            {
                name: 'discount'
            },
            {
                name: 'discountispercent',
                defValue: 1
            },
            {
                name: 'prdiscount',
                defValue: 0
            },
            {
                name: 'invstore'
            },
            {
                name: 'invlocation'
            },
            {
                name: 'prtaxid'
            },
            {
                name: 'prtaxname'
            },
            {
                name: 'prtaxpercent',
                defValue: 0
            },
            {
                name: 'taxamount',
                defValue: 0
            },
            {
                name: 'amount',
                defValue: 0
            },
            {
                name: 'lineleveltermamount',
                defValue: 0
            },
            {
                name: 'amountwithtax',
                defValue: 0
            },
            {
                name: 'amountwithouttax',
                defValue: 0
            }, // used this field for Invoice Terms - rate*qty-discount

            {
                name: 'taxpercent'
            },
            {
                name: 'remark'
            },
            {
                name: 'transectionno'
            },
            {
                name: 'remquantity'
            },
            {
                name: 'remainingquantity'
            },
            {
                name: 'oldcurrencyrate',
                defValue: 1
            },
            {
                name: 'currencysymbol',
                defValue: this.symbol
            },
            {
                name: 'currencyrate',
                defValue: 1
            },
            {
                name: 'externalcurrencyrate'
            },
            {
                name: 'orignalamount'
            },
            {
                name: 'typeid',
                defValue: 0
            },
            {
                name: 'isNewRecord',
                defValue: '1'
            },
            {
                name: 'producttype'
            },
            {
                name: 'permit'
            },
            {
                name: 'linkto'
            },
            {
                name: 'linkid'
            },
            {
                name: 'linktype'
            },
            {
                name: 'savedrowid'
            },
            {
                name: 'originalTransactionRowid'
            },
            {
                name: 'changedQuantity'
            },
            {
                name: 'approvedcost'
            },
            {
                name: 'approverremark'
            },
            {
                name: 'customfield'
            },
            {
                name: 'gridRemark'
            },
            {
                name: 'productcustomfield'
            },
            {
                name: 'accountId'
            },
            {
                name: 'salesAccountId'
            },
            {
                name: 'batchdetails'
            },
            {
                name: 'discountAccountId'
            },
            {
                name: 'rowTaxAmount'
            },
            {
                name: 'type'
            },
            {
                name: 'shelfLocation'
            },
            {
                name: 'productcustomfield'
            },
            {
                name: 'supplierpartnumber'
            },
            {
                name: 'assetDetails'
            },
            {
                name: 'profitLossAmt'
            },
            {
                name: 'copybaseuomrate',
                mapping: 'baseuomrate'
            }, //for handling inventory updation 

            {
                name: 'pid'
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
                isForSequence: true
            },
            {
                name: 'hasAccess'
            },
            {
                name: "defaultlocqty"
            },
            {
                name: "deflocation"
            },
            {
                name: "hsncode"
            },
            {
                name: 'balancequantity'
            },
            {
                name: 'lockquantity'
            },
            {
                name: 'jsonstring'
            }

        ]);


        this.productStoreGrid = new Wtf.data.GroupingStore({
            //    url:Wtf.req.account+'CompanyManager.jsp',
            url: "ACCProductCMN/getProductsForSelectionGrid.do",
            baseParams: {
                common: '1'

            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, this.productGridRec),
            groupField: "category",
            sortInfo: {
                field: 'temp',
                direction: "ASC"
            }
        });

        this.productRec = Wtf.data.Record.create([
            {
                name: 'productid'
            },
            {
                name: 'pid'
            },
            {
                name: 'type'
            },
            {
                name: 'productname'
            }

        ]);
        this.productStore = new Wtf.data.Store({
            url: "ACCProductCMN/getProductsForCombo.do",
            baseParams: {
                excludeParent: true,
                module_name: "PRODUCT_CATEGORY"
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productRec)
        });
        this.productStore.load({
            params: {
                customerid: this.customerid
            }
        });
        this.productList = new Wtf.common.Select(Wtf.apply({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.product.productName"), //"Product Name*" ,
            extraFields: ['pid', 'type'],
            extraComparisionField: 'pid', // type ahead search on product id as well.
            listWidth: Wtf.ProductComboListWidth,
            forceSelection: true
        }, {
            name: "productlist",
            id: "productlist",
            store: this.productStore,
            valueField: 'productid',
            displayField: 'productname',
            emptyText: WtfGlobal.getLocaleText("acc.prod.comboEmptytext"), //"Please Select Product",
            anchor: '85%',
            mode: 'local',
            selectOnFocus: true,
            //            allowBlank:false,
            triggerAction: 'all',
            typeAhead: true,
            scope: this
        }));
        var ProductcolumnArr = [];

        ProductcolumnArr.push(
//                this.smProductGrid,
                new Wtf.grid.RowNumberer({
                    width: 40
                }),
        {
            header: WtfGlobal.getLocaleText("acc.product.gridProductID"),
            sortable: true,
            dataIndex: "pid",
            width: 110
        }, {
            header: WtfGlobal.getLocaleText("acc.field.ProductName"),
            sortable: true,
            dataIndex: "productname",
            width: 150,
            renderer: function(val, m, rec) {
                return "<a class='jumplink' wtf:qtip='" + val + "' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\"" + rec.data.productid + "\")'>" + val + "</a>";
            }
        }, {
            header: "Product Description",
            sortable: true,
            dataIndex: "desc",
            width: 150,
            renderer: function(val) {
                val = val.replace(/(<([^>]+)>)/ig, "");
                return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.productList.gridProductDescription") + "'>" + val + "</div>";
            }
        })
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.viewpricelist)) {
            ProductcolumnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.UnitPurchasePrice"),
                sortable: true,
                hidden: true,
                dataIndex: "purchaseprice",
                renderer: function(v, metadata, record) {
                    if (!Wtf.dispalyUnitPriceAmountInPurchase) {
                        return Wtf.UpriceAndAmountDisplayValue;
                    } else {
                        return WtfGlobal.withCurrencyUnitPriceRenderer(v, false, record);
                    }
                },
                width: 110
            }, {
                header: WtfGlobal.getLocaleText("acc.field.UnitSalesPrice"),
                sortable: true,
                hidden: true,
                dataIndex: "saleprice",
                renderer: function(v, metadata, record) {
                    if (!Wtf.dispalyUnitPriceAmountInSales) {
                        return Wtf.UpriceAndAmountDisplayValue;
                    } else {
                        return WtfGlobal.withCurrencyUnitPriceRenderer(v, false, record);
                    }
                },
                width: 110
            })
        }
        ProductcolumnArr.push({
            header: this.isConsignment && this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId ? WtfGlobal.getLocaleText("acc.invoice.gridQty") : WtfGlobal.getLocaleText("acc.field.AvailableQuantity"),
            align: 'right',
            dataIndex: "quantity",
            hidden: true,
            renderer: function(val, m, rec) {
                val = (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                return'<div style="margin-right: 15px;">' + val + '</div>';
            },
            width: 110

        }, {
            header: WtfGlobal.getLocaleText("acc.productList.gridLockQuantity"), //"BLock Quantity
            dataIndex: 'lockquantity',
            align: 'right',
            hidden: true,
            renderer: WtfGlobal.blockQtyrenderer,
            pdfwidth: 75

        }, {
            header: WtfGlobal.getLocaleText("acc.productList.gridBalQty"), //"Balance Quantity",
            dataIndex: "balancequantity",
            align: 'right',
            hidden: true,
            renderer: WtfGlobal.balanceQtyrenderer,
            pdfwidth: 75
        }, {
            header: WtfGlobal.getLocaleText("acc.field.VendorAvailableQuantity"),
            align: 'right',
            dataIndex: "venconsignuomquantity",
            hidden: true,
            renderer: function(val, m, rec) {
                val = (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                return'<div style="margin-right: 15px;">' + val + '</div>';
            },
            width: 110

        },
        {
            header: WtfGlobal.getLocaleText("acc.cust.Productcategory"), //"Category",
            dataIndex: 'category',
            hidden: true,
            fixed: true,
            renderer: WtfGlobal.deletedRenderer
        }, {
            header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
            align: 'center',
            hidden: true,
            dataIndex: this.isConsignment && this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId ? "uom" : "uomname"
        }, {
            header: WtfGlobal.getLocaleText("acc.product.packaging"),
            align: 'center',
            hidden: true,
            dataIndex: "packaging"
        }, {
            header: WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),
            align: 'center',
            hidden: true,
            dataIndex: "baseuomquantity",
            renderer: function(val, m, rec) {
                val = (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                return'<div style="margin-right: 15px;">' + val + '</div>';
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),
            align: 'center',
            hidden: true,
            dataIndex: "rate",
            renderer: WtfGlobal.withCurrencyUnitPriceRenderer
        }, {
            header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"),
            align: 'center',
            hidden: true,
            dataIndex: "amount"
        }
        )

        ProductcolumnArr = WtfGlobal.appendCustomColumn(ProductcolumnArr, GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId], undefined, undefined, false, false);
        var columnArray = [];
        columnArray = GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId];
        WtfGlobal.updateStoreConfig(columnArray, this.productStoreGrid);


        this.columnCm = new Wtf.grid.ColumnModel(ProductcolumnArr);
        this.localSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.productList.searchText"), //'Search by Product Name',
            width: 130,
            field: 'productname',
            hidden: true,
            Store: this.productStoreGrid
        });

        this.productCategoryRecord = Wtf.data.Record.create([
            {
                name: 'id'
            },
            {
                name: 'name'
            }
        ]);
        this.productCategoryStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.productCategoryRecord)
        });

        this.typeEditor = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.Productcategory"),
            store: this.productCategoryStore,
            displayField: 'name',
            valueField: 'id',
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true
        });
        this.productCategoryStore.load();
        this.productCategoryStore.on('load', this.setValue, this);
        var tbarArray = new Array();
        if (this.isConsignment && this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId) {
            tbarArray.push(this.localSearch, "-", WtfGlobal.getLocaleText("acc.product.productName"), this.productList, {
                xtype: 'button',
                text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
                iconCls: 'accountingbase fetch',
                scope: this,
                handler: this.loadTypeStore
            })
        } else {
            tbarArray.push(this.localSearch, "-", WtfGlobal.getLocaleText("acc.cust.Productcategory"), this.typeEditor, {
                xtype: 'button',
                text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
                iconCls: 'accountingbase fetch',
                scope: this,
                handler: this.loadTypeStore
            })
        }

        this.productgrid = new Wtf.grid.EditorGridPanel({
            store: this.productStoreGrid,
//            sm: this.smProductGrid,
            cm: this.columnCm,
            clicksToEdit: 1,
            border: false,
            loadMask: true,
            view: !(this.isConsignment && this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId) ? new Wtf.grid.GroupingView({
                forceFit: false
            }) : new Wtf.grid.GridView({
                forceFit: false
            }),
            layout: 'fit',
            modal: true,
            tbar: tbarArray,
            bbar: this.pag = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                border: false,
                id: "paggintoolbar_ProductGrid" + this.id,
                store: this.productStoreGrid,
                searchField: this.localSearch,
                scope: this,
                plugins: this.pPageSizeObj = new Wtf.common.pPageSize({
                    id: "pPageSize_ProductGrid_" + this.id
                }),
                autoWidth: true,
                displayInfo: true//,
            })
        });

    },
    getAvailableQtyForProductInWarehouse: function(productId, warehouseId, rowIndexToSetRecInGrid, recordClicked, i) {
        if (warehouseId != undefined && warehouseId != "" && productId != undefined && productId != "") {
            Wtf.Ajax.requestEx({
                url: "INVStockLevel/getAvailableQtyByStoreProduct.do",
                params: {
                    fromStoreId: warehouseId,
                    productId: productId
                }
            }, this,
                    function(action, response) {
                        if (action.success == true) {
                            var availQty = action.data[0].availableQty;

                            recordClicked.set("avaquantity", availQty);
                        } else {
                            WtfComMsgBox(["Error", "Error occurred while fetching available quantity."], 0);
                            return 0;
                        }

                    },
                    function() {
                        WtfComMsgBox(["Error", "Error occurred while fetching available quantity."], 0);
                        return 0;
                    });
        } else {
            recordClicked.set("avaquantity", 0);
        }
        recordClicked.set("productid", productId);
        recordClicked.set("pid", this.productSelectionIdArray[i]);
        recordClicked.set("itemdescription", this.productSelectionDescArray[i]);
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
    getDefaultLocationQty: function(productId, warehouseId, recordClicked, i) {

        Wtf.Ajax.requestEx({
            url: "INVStockLevel/getStoreProductWiseDetailList.do",
            params: {
                storeId: warehouseId,
                productId: productId,
                defaultloc: true
            }
        }, this,
                function(res, action) {
                    if (res.success == true) {
                        var defaultlocqty = res.data[0].defaultlocqty;
                        var deflocation = res.data[0].deflocation;
                        recordClicked.set("defaultlocqty", defaultlocqty);
                        recordClicked.set("deflocation", deflocation);
                    } else {
                        WtfComMsgBox(["Error", "Error occurred while fetching data."], 0);
                        return;
                    }

                },
                function() {
                    WtfComMsgBox(["Error", "Error occurred while processing"], 1);
                }
        );
    },
    setValue: function() {
        var record = new Wtf.data.Record({
            name: 'All',
            id: 'All'
        });
        var index = this.productCategoryStore.find('name', 'All');
        if (index == -1) {
            this.productCategoryStore.insert(0, record);
            this.typeEditor.setValue("All");
        }

    },
    loadTypeStore: function(a, rec) {
        var categoryid = this.typeEditor.getValue();
        if (categoryid == 'All') {
            this.productStoreGrid.load({
                params: {
                    start: 0,
                    limit: this.pPageSizeObj.combo.value,
                    moduleid: this.moduleid,
                    ss: this.localSearch.getValue(),
                    ids: this.productList.getValue(),
                    categoryid: categoryid
                }
            });
        }
        else {
            this.productStoreGrid.load({
                params: {
                    start: 0,
                    limit: this.pPageSizeObj.combo.value,
                    categoryid: categoryid,
                    ss: this.localSearch.getValue(),
                    ids: this.productList.getValue()
                }
            });
        }
    },
    closeWin: function() { /*this.fireEvent('update',this,this.value);*/
        this.close();
    },
    beforeProductSelect: function(sm, index) {
        var rec = this.productStoreGrid.getAt(index);
        if (this.isConsignment && this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId) {
            return true;
        } else {
            if (!rec.data.isActiveItem) {
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
    getFilterJson: function(record) {
        var GlobalcolumnModel = GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId];
        var filterJson = "";
        var preJsonString = '{"' + record['productid'] + '":[';
        var postJsonString = ']},';
        if (GlobalcolumnModel) {
            for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                var fieldname = GlobalcolumnModel[cnt].fieldname;
                var xtype = GlobalcolumnModel[cnt].fieldtype;
                if (record[fieldname] != "" && record[fieldname] != undefined) {
                    if (xtype == 3) {
                        var date = WtfGlobal.onlyDateRendererTZ(record[fieldname]);
                        filterJson += '{"' + GlobalcolumnModel[cnt].fieldid + '":"' + date + '"},';
                    } else {
                        filterJson += '{"' + GlobalcolumnModel[cnt].fieldid + '":"' + record[fieldname] + '"},';
                    }

                }
            }

            if (filterJson != "") {
                filterJson = filterJson.substring(0, filterJson.length - 1);
                filterJson = preJsonString + filterJson + postJsonString;
            }
            return filterJson;
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
    setCustomData: function(rec, jString) {
        if (jString != "" && jString != undefined && jString.length >= 0) {
//            jString = eval(jString); //
            for (var length = 0; length <= jString.length; length++) {
                var JSON = jString[length];
                for (var key in JSON) {
                    if (JSON.hasOwnProperty(key)) {
                        var fieldname = WtfGlobal.arraySearch(GlobalColumnModel[Wtf.Acc_Product_Master_ModuleId], key);
                        rec.set(fieldname, JSON[key]);
                    }
                }

            }

        }
    }

});