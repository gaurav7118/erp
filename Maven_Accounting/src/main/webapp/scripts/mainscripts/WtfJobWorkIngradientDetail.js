/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.JobWorkOutProductWindow = function(config) {
    Wtf.apply(this, config);
    this.isfromSubmit = false;
    this.butnArr = new Array();
    this.noteType = WtfGlobal.getLocaleText("acc.JobWorkOut.ingradientdaetils");
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"), //'Submit',
        scope: this,
        hidden: this.readOnly,
        handler: function() {
            if (this.validateJobOrderItems()) {
                this.isfromSubmit = true;
                this.close();
            }
        }
    }, {
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function() {

            this.hide();
        }
    });

    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.JobWorkOutProductWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.JobWorkOutProductWindow, Wtf.Window, {
    onRender: function(config) {
        Wtf.account.JobWorkOutProductWindow.superclass.onRender.call(this, config);

        var title = this.noteType;
        var msg = "";
        if (this.uomName != undefined && this.uomName != "") {
            msg = "<b>" + WtfGlobal.getLocaleText("acc.product.gridProduct") + "</b> : " + this.productName + "<br> <b>" + WtfGlobal.getLocaleText("acc.product.gridQty") + "</b> : " + this.quantity + " " + this.uomName;
        } else {
            msg = "<b>" + WtfGlobal.getLocaleText("acc.product.gridProduct") + "</b> : " + this.productName + "<br> <b>" + WtfGlobal.getLocaleText("acc.product.gridQty") + "</b> : " + this.quantity;
        }
        var isgrid = true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title, msg, "../../images/accounting_image/price-list.gif", isgrid)},
        {
            region: 'center',
            border: false,
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: this.grid
                    // tbar:this.buttonArray
//            ,
//            bbar:this.pagingToolbar
        });
    },
    initComponent: function(config) {
        Wtf.account.JobWorkOutProductWindow.superclass.initComponent.call(this, config);
        //Create Store
        this.createStore();

        //Create Grid
        this.createGrid();
    },
    createStore: function() {
        this.record = new Wtf.data.Record.create([
            {
                "name": "productid"
            },
            {
                "name": "productname"
            },
            {
                "name": "challannumber"
            },
            {
                "name": "istId"
            },
            {
                "name": "balancequantity"
            },
            {
                "name": "quantity"
            },
            {
                "name": "serialDetails"
            },
            {
                "name": "batchQty"
            },
            {
                "name": "stockType"
            },
            {
                "name": "stockTypeName"
            },
            {
                "name": "isWarehouseForProduct"
            },
            {
                "name": "isLocationForProduct"
            },
            {
                "name": "isBatchForProduct"
            },
            {
                "name": "isSerialForProduct"
            },
            {
                "name": "isRowForProduct"
            },
            {
                "name": "isRackForProduct"
            },
            {
                "name": "isBinForProduct"
            },
            {
                "name": "stockType"
            },
            {
                "name": "stockTypeName"
            },
            {
                "name": "location"
            },
            {
                "name": "warehouse"
            },
            {
                "name": "batchdetails"
            },
            {
                "name": "isSKUForProduct"
            }, {
                "name": "assembleqty"
            }
        ]);

        this.store = new Wtf.data.Store({
            url: "ACCGoodsReceiptCMN/getJobWorkOutIngradientDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.record)

        });

    },
    loadStore: function() {
        this.store.load({
            params: {
                joborderdetail: this.joborderdetail,
                productid: this.prodId
            }
        }, this);
    },
    createGrid: function() {
        var cmDefaultWidth = 200;
        this.gridcm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), //"Product Name",
                dataIndex: 'productname',
                width: cmDefaultWidth,
            },
            {
                header: WtfGlobal.getLocaleText("acc.JobWorkOut.challanno"), //"Product Name",
                dataIndex: 'challannumber',
                width: cmDefaultWidth,
            }, {
                header: WtfGlobal.getLocaleText("acc.JobWorkOut.BOMqty"),
                dataIndex: 'assembleqty',
                sortable: false,
                align: "right",
                width: cmDefaultWidth,
                summaryType: 'sum',
                renderer: function(val) {
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                }
            }, {
                header: WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),
                dataIndex: 'balancequantity',
                sortable: false,
                align: "right",
                width: cmDefaultWidth,
                summaryType: 'sum',
                renderer: function(val) {
                    return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                }
            }, {
                header: WtfGlobal.getLocaleText("acc.product.gridQty"),
                dataIndex: "quantity",
                width: cmDefaultWidth,
                editor: this.readOnly ? "" : this.serialQty = new Wtf.form.NumberField({
                    allowNegative: false,
                    decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
                }),
                renderer: function(val) {
                    if (val == "") {
                        return val;
                    } else {
                        return parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) // WtfGlobal.getCurrencyFormatWithoutSymbol(v, Wtf.companyPref.quantityDecimalPrecision)
                    }
                }

            }, {
                header: '',
                dataIndex: "serialrenderer",
                align: 'center',
                renderer: this.serialRenderer.createDelegate(this),
                hidden: (!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory && !Wtf.account.companyAccountPref.isRowCompulsory && !Wtf.account.companyAccountPref.isRackCompulsory && !Wtf.account.companyAccountPref.isBinCompulsory),
                width: 40
            }
        ]);
        this.grid = new Wtf.grid.EditorGridPanel({
            autoScroll: true,
            clicksToEdit: 1,
            height: 500,
            store: this.store,
            cm: this.gridcm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        if (this.joborderdetails != undefined && this.joborderdetails != "") {
            this.LoadConfigurationAfterStoreLoad();
//            this.grid.getView().refresh();
        } else {
            this.loadStore();
        }


        this.store.on('load', function() {
        }, this);
        this.grid.on('rowclick', this.handleRowClick, this);
        this.grid.on('afteredit', this.updateRow, this);

    },
    updateRow: function(obj) {
        if (obj != null) {
            var rec = obj.record;
            if (obj.field == "quantity") {
                if (obj.value > obj.record.get("balancequantity")) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.stockrequest.Quantitycannotbemorethanavailablequantity")], 2);
                    rec.set("quantity", obj.originalValue);
                    return;

                }else{
                    /*
                     * ERP-41374
                       Quantity was going wrong to SerialNoAutopopulateWindow, so if quantity is modified need to change it in batchdetail too.
                    */
                    var batchArray=JSON.parse(obj.record.data.batchdetails)//Convert string into JSON
                    batchArray["0"].quantity=obj.value;//Assign modified value of quantity 
                    obj.record.data.batchdetails = JSON.stringify(batchArray);//Convert JSON to String
                }
            }
        }

    },
    LoadConfigurationAfterStoreLoad: function() {
        var joborderrecord = "";
        if (this.joborderdetails != undefined && this.joborderdetails.length > 1) {
            joborderrecord = eval('(' + this.joborderdetails + ')');
        }
        var recordQuantity = joborderrecord.length;
        if (joborderrecord.length != 0) {
            for (var i = 0; i < recordQuantity; i++) {
                var batchObj = joborderrecord[i];
                this.addGridRec(batchObj);
            }
        }
    },
    addGridRec: function(record) {
        var rec = this.record;
        var size = this.store.getCount();

        var Record = this.store.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {}, blankObj = {};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if (f.name != 'rowid') {
                blankObj[f.name] = '';
            }
        }
        rec = new rec(blankObj);

        rec.beginEdit();
        //   var fields = this.store.fields;
        var Record = this.store.reader.recordType, fields = Record.prototype.fields, fi = fields.items, fl = fields.length;
        for (var x = 0; x < fl; x++) {
            var value = "";
            if (record != undefined && record != "") {
                if (fields.get(x).type == "date") {
                    value = record[fields.get(x).name];
                    if (value != "" && value != undefined)
                        value = new Date(value);
                }
                else {
                    if (record && record[fields.get(x).name] != "" && record[fields.get(x).name] != undefined)
                        value = unescape(record[fields.get(x).name]); //for saving the serial no. its showing %20 in space
                }
                rec.set(fields.get(x).name, value);
            }
        }

        rec.endEdit();
        rec.commit();
        var recordFound = false;

        if (!recordFound) {
            this.store.add(rec);
        }

    },
    handleRowClick: function(grid, rowindex, e) {
        if (e.getTarget(".serialNo-gridrow")) {
            var store = grid.getStore();
            var record = store.getAt(rowindex);
            this.callSerialDetailsWindow(record)
        }
    },
    callSerialDetailsWindow: function(prorec) {
        if (prorec.get("quantity") == "" || prorec.get("quantity") == undefined) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.empty")], 2);
            return false;
        }
        var index = this.grid.getStore().findBy(function(rec) {
            if (rec.data.productid)
                return true;
            else
                return false;
        })
        if (index != -1) {
            this.batchDetailswin = new Wtf.account.SerialNoAutopopulateWindow({
                renderTo: document.body,
                title: WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
                productName: prorec.data.productname,
                quantity: prorec.data.quantity,
                shipquantity: prorec.data.quantity,
                billid: prorec.data.billid,
                defaultLocation: prorec.data.location,
                productid: prorec.data.productid,
                isShippingDO: true,
                moduleid: this.moduleid,
                transactionid: 854, //this.moduleid,
                isDO: this.isCustomer ? true : false,
                documentid: (this.isEdit) ? prorec.data.rowid : "",
                defaultWarehouse: prorec.data.warehouse,
                defaultAvailbaleQty: this.AvailableQuantity,
                batchDetails: prorec.data.batchdetails,
                warrantyperiod: prorec.data.warrantyperiod,
                warrantyperiodsal: prorec.data.warrantyperiodsal,
                isLocationForProduct: prorec.data.isLocationForProduct,
                isWarehouseForProduct: prorec.data.isWarehouseForProduct,
                isRowForProduct: prorec.data.isRowForProduct,
                isRackForProduct: prorec.data.isRackForProduct,
                isBinForProduct: prorec.data.isBinForProduct,
                isBatchForProduct: prorec.data.isBatchForProduct,
                isSKUForProduct: prorec.data.isSKUForProduct,
                isSerialForProduct: prorec.data.isSerialForProduct,
                isShowStockType: (this.isCustomer) ? true : false,
                isEdit: true,
                copyTrans: this.copyTrans,
                readOnly: this.readOnly,
                width: 950,
                height: 400,
                resizable: false,
                modal: true,
                parentObj: this.parentObj,
                parentGrid: this.Grid
            });
            this.batchDetailswin.on("beforeclose", function() {
                this.batchDetails = this.batchDetailswin.getBatchDetails();
                var isfromSubmit = this.batchDetailswin.isfromSubmit;
                if (isfromSubmit) {  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                    prorec.set("batchdetails", this.batchDetails);
                }
            }, this);
            this.batchDetailswin.show();
        }
    },
    serialRenderer: function(v, m, rec) {
        return "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.serial.desc") + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.serial.desc.title") + "' class='" + getButtonIconCls(Wtf.etype.serialgridrow) + "'></div>";
    },
    validateJobOrderItems: function() {
        var store = this.grid.getStore();
        var enteredQuantity = 0;
        var isInvalidRecordFound = false;
        var compareProdId = "";
        var subproductqty = 0;
        var assembleqty = 0;
        var counter = 0;
        var storesize = store.getTotalCount();
        var quantity = this.quantity;
        store.each(function(rec) {
            counter++;
            if ((compareProdId != "" && compareProdId != undefined) && compareProdId == rec.data.productid) {
                /**
                 * Record of same product Id with diff challan no
                 */
                subproductqty = subproductqty + rec.data.quantity;
                assembleqty = rec.data.assembleqty;
            } else if (compareProdId == "" || compareProdId == undefined) {
                /**
                 * If First Record of grid
                 */
                compareProdId = rec.data.productid;
                subproductqty = subproductqty + rec.data.quantity;
                assembleqty = rec.data.assembleqty;
            } else if (compareProdId != rec.data.productid) {
                /**
                 * If product change then calculate total of product
                 */
                compareProdId = rec.data.productid;
                var totalqty = subproductqty / assembleqty;
                assembleqty = rec.data.assembleqty;
                subproductqty = rec.data.quantity;
                /**
                 * Check Total qty match with assemble product qty
                 */
                if (getRoundofValue(totalqty) != getRoundofValue(quantity)) {
                    isInvalidRecordFound = true;

                }
            }
            if (counter == storesize) {
                var totalqty = subproductqty / assembleqty;
                if (getRoundofValue(totalqty) != getRoundofValue(quantity)) {
                    isInvalidRecordFound = true;

                }
            }
        });

        if (isInvalidRecordFound) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")], 2);
            return false;
        }

        return true;
    },
    getJobWorkItemDetails: function() {

        var arr = [];
        var inculelast = true;
        this.store.each(function(rec) {

            if (rec.data.serialno != "") {
                rec.data[CUSTOM_FIELD_KEY] = Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.module).substring(13));
            }
            // var transdate=(this.isEdit?WtfGlobal.convertToGenericDate(this.record.data.date):WtfGlobal.convertToGenericDate(new Date()));
            if (rec.data.quantity != undefined && rec.data.quantity != "") {
                arr.push(this.store.indexOf(rec));
            }

        }, this);
        var jarray = WtfGlobal.getJSONArray(this.grid, inculelast, arr);
        return jarray;
    }
});

