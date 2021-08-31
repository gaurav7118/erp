

/**********************Delivery Order Report And Fixed Asset Delivery Order Report*************************/
Wtf.account.BulkInvoicesList = function(config) {

    this.label = config.label;
    this.titlel = config.title != undefined ? config.title : "null";
    this.moduleid = config.moduleid;
    this.isfromReportList=config.isfromReportList;
    this.isCustomer=config.isCustomer;
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: false
    });
    if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
        this.label = WtfGlobal.getLocaleText("acc.accPref.autoSO");
        if (this.isfromReportList) {
            this.saveurl = "ACCInvoice/saveBulkDOFromSO.do";
        } else {
            this.saveurl = "ACCInvoice/saveBulkInvoicesFromSO.do";
        }

    } else if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
        this.label = WtfGlobal.getLocaleText("acc.accPref.autoPO")
        this.saveurl = "ACCGoodsReceipt/saveBulkInvoicesFromPO.do";
    } else {
        this.label = WtfGlobal.getLocaleText("acc.accPref.autoDO");
        this.saveurl = "ACCInvoice/saveBulkInvoice.do";
    }
    this.newRec = Wtf.data.Record.create([
    {
        name: 'billid'
    },


    {
        name: 'entryno'
    },

    {
        name: 'billto'
    },


    {
        name: 'billno'
    },

    {
        name: 'date', 
        type: 'date'
    },

    {
        name: 'shipdate', 
        type: 'date'
    },

    {
        name: 'personname'
    },

    {
        name: 'aliasname'
    },


    {
        name: 'personid'
    },



    {
        name: 'externalcurrencyrate'
    },

    {
        name: 'status'
    },

      {name: 'isprinted'},

    {
        name: 'currencysymbol'
    },

    {
        name: 'currencyid'
    },

    {
        name: 'amount'
    },

    {
        name: 'amountinbase'
    },

    {
        name: 'invoicenumber'
    },


    {
        name: 'taxid'
    }

    ]);
    this.detailRec = Wtf.data.Record.create([
    {
        name: 'billno'
    },

    {
        name: 'details'
    },

    {
        name: 'Isvalid'
    },

    {
        name: 'personname'
    },

    {
        name: 'text'
    }


    ]);
    this.personRec = Wtf.data.Record.create([
    {
        name: 'personid'
    },

    {
        name: 'taxid'
    },

    {
        name:'currency'
    }


    ]);
    this.personStore = new Wtf.data.SimpleStore({
        fields: this.personRec
    });
    this.detailStore = new Wtf.data.SimpleStore({
        fields: this.detailRec
    //        groupField: 'personname',

    });
    this.detailnewStore = new Wtf.data.SimpleStore({
        fields: this.newRec
    });
    this.perColumnModelArr = [];
    this.invColumnModelArr = [];
    this.invColumnModelArr.push(
    {
        header: (this.isCustomer)?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"),
        dataIndex: 'personname',
        width: 100,
        pdfwidth: 75
    },
    {
        header: this.label + " " + WtfGlobal.getLocaleText("acc.cn.9"),
        dataIndex: 'billno',
        width: 80,
        pdfwidth: 75
    }, {
        header: WtfGlobal.getLocaleText("acc.ccReport.tab2"),
        dataIndex: 'details',
        width: 250,
        pdfwidth: 75
    //  renderer: WtfGlobal.linkDeletedRenderer
    }, {
        header: "Status",
        dataIndex: 'Isvalid',
        width: 80,
        pdfwidth: 75,
        renderer: function(val) {
            if (val == "Invalid") {
                return '<span style="color:red;">' + val + '</span>';
            } else {
                return val;
            }
        }
    },
    {
        header: "text",
        dataIndex: 'text',
        width: 80,
        pdfwidth: 75
    }
    );

    this.GridRec = Wtf.data.Record.create([
    {
        name: 'billid'
    },


    {
        name: 'entryno'
    },

    {
        name: 'billto'
    },

    {
        name: 'billno'
    },

    {
        name: 'date', 
        type: 'date'
    },

    {
        name: 'shipdate', 
        type: 'date'
    },

    {
        name: 'personname'
    },

    {
        name: 'aliasname'
    },


    {
        name: 'personid'
    },

    {
        name: 'externalcurrencyrate'
    },

    {
        name: 'status'
    },

    {
        name: 'currencysymbol'
    },

    {
        name: 'currencyid'
    },

    {
        name: 'amount'
    },

    {
        name: 'amountinbase'
    },

    {
        name: 'invoicenumber'
    },

   

    {
        name: 'taxid'
    },
    {
        name: 'termid'
    },
    {
        name: 'agent'
    },
    {
        name: 'supplierinvoiceno'
    }
  
    ]);
    
    if (this.moduleid==Wtf.Acc_Sales_Order_ModuleId){
        this.url="ACCSalesOrderCMN/getSalesOrdersMerged.do";
    }else if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId){
        this.url="ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do";
    }else {
        this.url="ACCInvoiceCMN/getDeliveryOrdersMerged.do";
    }
    
    this.Store = new Wtf.data.GroupingStore({
        url: this.url,
        //        url: Wtf.req.account + this.businessPerson + 'Manager.jsp',
        //                remoteSort: true,
        baseParams: {
            deleted: false,
            nondeleted: false,
            pendingapproval: false,
            bulkInv: true,
            companyids: companyids,
            isfavourite: false,
            gcurrencyid: gcurrencyid,
            userid: loginid,
            cashonly: false,
            isUnInvoiced: true,
            creditonly: false,
            isfromReportList:this.isfromReportList
        }, 
        sortInfo: {
            field: 'billno',
            direction: 'ASC'
        },
        groupField: 'personname',
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, this.GridRec)
    });



    this.Store.on('beforeload', function(s, o) {
        if (!o.params)
            o.params = {};
        var currentBaseParams = this.Store.baseParams;
        this.Store.baseParams = currentBaseParams;

    }, this);
    this.gridColumnModelArr = [];
    this.gridColumnModelArr.push(this.sm,
    {
        hidden: true,
        header: "",
        dataIndex: 'billid',
        hideable: false
    }, {
        header: this.label + " " + WtfGlobal.getLocaleText("acc.cn.9"),
        dataIndex: 'billno',
        width: 150,
        pdfwidth: 75,
        sortable: true
    //renderer:(config.isQuotation||config.isOrder)?"":WtfGlobal.linkDeletedRenderer
    // renderer: WtfGlobal.linkDeletedRenderer
    }, {
        header: this.label + " " + WtfGlobal.getLocaleText("acc.inventoryList.date"),
        dataIndex: 'date',
        align: 'center',
        width: 150,
        pdfwidth: 80,
        sortable: true,
        renderer: WtfGlobal.onlyDateDeletedRenderer
    }, {
        header: WtfGlobal.getLocaleText("acc.invoiceList.shipdate"),
        dataIndex: 'shipdate',
        renderer: WtfGlobal.onlyDateDeletedRenderer,
        hidden: true
    }, {
        header:(this.isCustomer)?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"), //this.businessPerson,
        width: 150,
        pdfwidth: 75,
        renderer: WtfGlobal.deletedRenderer,
        sortable: true,
        dataIndex: 'personname'
    }, {
        header: WtfGlobal.getLocaleText("acc.cust.aliasname"),
        width: 150,
        pdfwidth: 75,
        hidden: true,
        renderer: WtfGlobal.deletedRenderer,
        dataIndex: 'aliasname',
        sortable: true
    }, {
        header: WtfGlobal.getLocaleText("acc.invoiceList.totAmt"), // "Total Amount",
        align: 'right',
        dataIndex: 'amount',
        width: 150,
        pdfwidth: 75,
        hidden: true,
        hidden:true,
        renderer: WtfGlobal.withoutRateCurrencyDeletedSymbol
    }, {
        header: WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome") + " (" + WtfGlobal.getCurrencyName() + ")", //"Total Amount (In Home Currency)",
        align: 'right',
        dataIndex: 'amountinbase',
        width: 150,
        pdfwidth: 75,
        hidecurrency: true,
        renderer: WtfGlobal.currencyDeletedRenderer,
        hidden: this.isOrder || !Wtf.account.companyAccountPref.unitPriceConfiguration  //hiden column 
    } 
    );
    this.custVendCategoryRec = Wtf.data.Record.create([
    {
        name: 'id'
    },

    {
        name: 'name'
    },
    ]);
    this.custVendCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams: {
            mode: 112,
            groupid: 7
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.custVendCategoryRec)
    });

    this.submit = new Wtf.Toolbar.Button({
        text: "Submit",
        tooltip: 'Submit',
        id: 'submit',
        scope: this
    //        handler: this.submitAction,
    });
    this.cancel = new Wtf.Toolbar.Button({
        text: "Cancel",
        tooltip: 'Cancel',
        id: 'Cancel',
        scope: this
    });

    this.createIndInv = new Wtf.Toolbar.Button({
        text: this.isfromReportList ? (this.moduleid==Wtf.Acc_Sales_Order_ModuleId ? WtfGlobal.getLocaleText("Create Individual Delivey Order") : WtfGlobal.getLocaleText("Create Individual Goods Receipt")) : WtfGlobal.getLocaleText("acc.common.createIndivdualInvoices"),
        tooltip: WtfGlobal.getLocaleText("acc.common.createIndivdualInvoices"),
        id: 'individualInv',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.add),
        handler: this.createIndividaulOrBulkInvoice,
        disabled: true
    });
    this.createBulkInv = new Wtf.Toolbar.Button({
        text: this.isfromReportList ? (this.moduleid==Wtf.Acc_Sales_Order_ModuleId ? WtfGlobal.getLocaleText("acc.common.bulkDO") : WtfGlobal.getLocaleText("Create Bulk Goods Receipt(s)")) : WtfGlobal.getLocaleText("acc.common.createBulkInvoices"),
        tooltip: WtfGlobal.getLocaleText("acc.common.createBulkInvoices"),
        id: 'bulkInv',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.add),
        handler: this.createIndividaulOrBulkInvoice,
        disabled: true
    });

    this.custVendCategory = new Wtf.form.ComboBox({
        fieldLabel: this.isCustomer?WtfGlobal.getLocaleText("acc.masterConfig.7"):WtfGlobal.getLocaleText("acc.masterConfig.8"),
        hiddenName: 'id',
        name: 'id',
        store: this.custVendCategoryStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        typeAhead: true,
        triggerAction: 'all',
        hideLabel: true,
        emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectacategory"),
        width: 100,
        listWidth: 150
    });

    this.custVendCategoryStore.on("load", function() {
        var record = new Wtf.data.Record({
            id: "All",
            name: "All Records"
        });
        this.custVendCategoryStore.insert(0, record);
        this.custVendCategory.setValue("All");
    }, this);

    this.custVendCategory.on("select", function(cmb, rec, ind) {
        this.person = "";
        this.filtercustid = rec.data.id;
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.customerCategoryid = this.filtercustid;
        this.Store.baseParams = currentBaseParams;
    }, this);

    this.custVendCategoryStore.load();
    this.submitBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.fetch"),
        tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"),
        id: 'submitRec' + this.id,
        scope: this,
        iconCls: 'accountingbase fetch',
        disabled: false
    });
    this.submitBttn.on("click", this.submitHandler, this);
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClick, this);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.rem.5") + " " + this.label,
        width: 150,
        // id: "quickSearch" + config.helpmodeid + config.id,
        field: 'billno',
        Store: this.Store
    })

    this.sm.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
    this.loadStore();
    this.tbar1 = new Array();
    //  this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr, GlobalColumnModelForReports[this.moduleid], true);
    this.grid = new Wtf.grid.GridPanel({
        //stripeRows :true,
        store: this.Store,
        id: "gridmsg1",
        height: 200,
        border: false,
        sm: this.sm,
        //tbar: this.tbar2,
        // disabled:this.readOnly,
        disabledClass: "newtripcmbss",
        layout: 'fit',
        loadMask: true,
        plugins: [Wtf.ux.grid.plugins.GroupCheckboxSelection],
        // emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
        view: new Wtf.grid.GroupingView({
            forceFit: true,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }),
        //plugins: this.expander,
        // viewConfig:this.gridView1,
        cm: new Wtf.grid.ColumnModel(this.gridColumnModelArr)
    });

    this.tbar1.push(this.quickPanelSearch, "-", this.resetBttn);
    var label = this.isCustomer?WtfGlobal.getLocaleText("acc.masterConfig.7"):WtfGlobal.getLocaleText("acc.masterConfig.8");
    this.tbar1.push("-");
    this.tbar1.push(label, this.custVendCategory);
    this.tbar1.push("-");
    this.tbar1.push(this.submitBttn);
    this.tbar1.push("-");
    this.tbar1.push(this.createIndInv);
    this.tbar1.push("-");
    this.tbar1.push(this.createBulkInv);
    this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [
        {
            region: 'center',
            border: false,
            layout: "fit",
            tbar: this.tbar1,
            items: [this.grid],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP = new Wtf.common.pPageSize({
                    id: "pPageSize_" + this.id
                    })
            // items:bottombtnArr
            })
        }]
    });
    
    this.addEvents({
        "loadMainReportTab": true
    })
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        items: [this.leadpan]
    });

    //    
    Wtf.account.BulkInvoicesList.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.BulkInvoicesList, Wtf.Panel, {
    //  
    individualSubmit: function() {
        var data = [];
        var arr = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.json="";
        for (var i = 0; i < this.recArr.length; i++) {
            this.json += "{\"personid\":\"" +  this.recArr[i].data.personid + "\",";
            //            this.json += "\"ordertaxamount\":\"" +  this.recArr[i].data.ordertaxamount + "\",";
            this.json += "\"billid\":\"" + this.recArr[i].data.billid + "\"},";
            }
          
        this.json = this.json.substr(0, this.json.length - 1);
        //        this.json = "{\"data\":[" + this.json + "]}";
        this.json = "[" + this.json + "]";
        //        for (var i = 0; i < this.recArr.length; i++) {
        //            json+="personid:"
        ////            var record = new this.GridRec({
        ////                personid: this.recArr[i].data.personid,
        ////                billid: this.recArr[i].data.billid
        ////            });
        //            arr.push(record);
        //        }
        //        data = WtfGlobal.getJSONArray(this.grid, false, arr);
        var comboVal = this.sequenceFormatCombobox.getValue();

        if (comboVal == "NA" || comboVal == "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Please Select Valid Sequence Format"], 2);
            return;
        }
        var transaction="";
        if(this.isfromReportList){
          transaction ="Delivery Order(s)"  
        }else{
            transaction="Invoices";
        }
        Wtf.MessageBox.confirm("Confirm", "Are you sure you want to create "+transaction+"?", function(btn) {
            if (btn == 'yes') {
                this.win.close();
              /*while creating individual invoices from SO 
               */
                if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
                    if (this.isfromReportList) {
                        this.saveurl = "ACCInvoice/saveBulkDOFromSO.do";
                    } else {
                        this.saveurl = "ACCInvoice/saveBulkInvoicesFromSO.do";
                    }

                } else if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
                    this.saveurl = "ACCGoodsReceipt/saveBulkInvoicesFromPO.do";
                } else {
                    this.saveurl = "ACCInvoice/saveBulkInvoice.do";
                }
                Wtf.Ajax.requestEx({
                    url: this.saveurl,
                    method:'POST',
                    params: {
                        data: this.json ,
                        sequenceformatInvoice: comboVal,
                        bulkInvoices: false
                    }
                }, this, this.genSuccessResponse, this.genFailureResponse);

            } else if (btn == 'no') {
                return;
            }
        }, this);
    },
    submitAction: function() {
        var data = [];
        var arr = [];
        var flag = 0;
        var chk = 0;
        var sequenceformatInvoice = "";
        var moduleName="";
        this.recArr = this.grid.getSelectionModel().getSelections();
        //        data = WtfGlobal.getJSONArray(this.grid, true, arr);
        var comboVal = this.sequenceFormatCombobox.getValue();
        //        if (response.data && response.data.length > 0) {
        //                sequenceformatInvoice = response.data[0].id;
        //            }
        if (comboVal == "NA" || comboVal == "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Please Select Valid Sequence Format"], 2);
            return;
        }
        this.win.close();
        this.personStore.removeAll();
        this.detailStore.removeAll();
        
        if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
           moduleName='Sales Order(s)'; 
        } else if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
            moduleName='Purchase Order(s)';
        } else {
            moduleName='Delivery Order(s)';
        }
        
       
        /*  Iterating on all selected records from grid*/
        for (i = 0; i < this.recArr.length; i++) {
            var val = this.recArr[i].data.personid;//Customer ID

           
          /*Checking whether record is present in "personStore" with Customer ID*/
            var index = this.personStore.findBy(
                function(record, id) {
                    if (record.get('personid') === val) {
                        return true;  // a record with this data exists
                    }
                    return false;  // there is no record in the store with this data
                });
           
          
                /*Preparing rec per customer*/
            if (index == -1) {
                this.detailRec = {
                    personid: this.recArr[i].data.personid,
                    taxid: this.recArr[i].data.taxid,
                    currency:this.recArr[i].data.currencyid,
                    termdetails:this.recArr[i].json.termdetails
                }
                //                this.personStore.insert(0, record);
                var record = new this.personStore.recordType(this.detailRec, i);
                this.personStore.add(record);
            }

        }
        
        
        var arr = [];
        var check = [], invalidbillNos = [], validBillNos = [],validCurrency =[],invalidCurrency = [] , invalidtermApplied=[] , includingGSTArr = [];
        if (this.recArr.length != 1) {
           
            /*  If only one record has been selected per customer
             * Then All records will valid
             */
            if (this.personStore.data.items.length == this.recArr.length) {
                for (var j = 0; j < this.recArr.length; j++) {
                    this.detailRec = {
                        billno: this.recArr[j].data.billno,
                        details: '',
                        Isvalid: 'Valid',
                        personname: this.recArr[j].data.personname,
                        text:this.isfromReportList ? 'Delivery Order(s) of following ' +moduleName +' will be created' : 'Invoices of following ' +moduleName +' will be created'
                    }

                    var record = new this.detailStore.recordType(this.detailRec, count);
                    this.detailStore.add(record);
                    count++;
                }
            }
        }


        /* It means selected records have atleast one customer for which have more than one records
         * i.e atleast one customer have more than one records  
         */
       
        if (this.personStore.data.items.length != this.recArr.length) {
          
            var isTermDetail = false;
             var isIncludingGST = false;
             var recordPerCustomer=0;
            for (var i = 0; i < this.personStore.data.items.length; i++) {
              
                isTermDetail = false;
                isIncludingGST=false;
                recordPerCustomer=0;

                /*Iterating on all records */
                for (var j = 0; j < this.recArr.length; j++) {

                    /* If same customer rec */
                    if (this.personStore.data.items[i].data.personid == this.recArr[j].data.personid) {
                        
                        recordPerCustomer++;

                        if (this.personStore.data.items[i].data.taxid != this.recArr[j].data.taxid) {
                            if (invalidbillNos.indexOf(this.personStore.data.items[i].data.personid) == -1)
                                invalidbillNos.push(this.personStore.data.items[i].data.personid);
                        }
                        if (this.personStore.data.items[i].data.currency != this.recArr[j].data.currencyid) {
                            if (invalidCurrency.indexOf(this.personStore.data.items[i].data.personid) == -1 && invalidbillNos.indexOf(this.personStore.data.items[i].data.personid) == -1)
                                invalidCurrency.push(this.personStore.data.items[i].data.personid);
                        }
                        /*  Checking if any SO of same customer have term*/
                        if (this.recArr[j].json.termdetails != "") {
                            isTermDetail = true;
                         

                        }
                         /*  Checking if any SO of same customer have Including GST true*/
                         if (this.recArr[j].json.gstIncluded) {
                            isIncludingGST = true;
                        

                        }

                    }
                    
                    /* If both applied then We are showing invalid messages for Term */
                    if (isIncludingGST && isTermDetail) {
                        /* Preparing invalid array for term applied */
                        if (j == this.recArr.length - 1 && isTermDetail && recordPerCustomer > 1) {

                            invalidtermApplied.push(this.personStore.data.items[i].data.personid);

                        }
                    } else {
                        /* Preparing invalid array for "Invoice term" applied */
                        if (j == this.recArr.length - 1 && isTermDetail && recordPerCustomer > 1) {

                            invalidtermApplied.push(this.personStore.data.items[i].data.personid);

                        }

                        /* Preparing invalid array for "Including GST" option true */
                        if (j == this.recArr.length - 1 && isIncludingGST && recordPerCustomer > 1) {
                            includingGSTArr.push(this.personStore.data.items[i].data.personid);
                        }
                    }

                }
            }
        }
        

        /*  Iterating on valid records which have same Currency/Tax  */

        if (this.personStore.data.items.length != this.recArr.length) {
            for (var i = 0; i < this.personStore.data.items.length; i++) {
                for (var j = 0; j < this.recArr.length; j++) {
                    if (this.personStore.data.items[i].data.personid == this.recArr[j].data.personid){
                        
                        if(this.personStore.data.items[i].data.currency == this.recArr[j].data.currencyid){
                            if ((validCurrency.indexOf(this.personStore.data.items[i].data.personid) == -1))
                                if (invalidCurrency.indexOf(this.personStore.data.items[i].data.personid) == -1 && invalidbillNos.indexOf(this.personStore.data.items[i].data.personid) == -1) {
                                    validCurrency.push(this.personStore.data.items[i].data.personid);
                                }   
                        }
                        if(this.personStore.data.items[i].data.taxid == this.recArr[j].data.taxid) {
                            if ((validBillNos.indexOf(this.personStore.data.items[i].data.personid) == -1))
                                if (invalidbillNos.indexOf(this.personStore.data.items[i].data.personid) == -1 && invalidCurrency.indexOf(this.personStore.data.items[i].data.personid) == -1) {
                                    validBillNos.push(this.personStore.data.items[i].data.personid);
                                }    

                        }                        
                    }
                }
            }
        }
        
        /*  Written a logic if "Invoice Terms" have been applied for any customer 
         * 
         * then remove the record from valid as well as from invalid array 
         */
        var indexof = "";
        for (var term = 0; term < invalidtermApplied.length; term++) {
            indexof = invalidbillNos.indexOf(invalidtermApplied[term]);
            if (indexof != -1) {
                invalidbillNos.splice(indexof, 1);

            }
            indexof = invalidCurrency.indexOf(invalidtermApplied[term]);
            if (indexof != -1) {
                invalidCurrency.splice(indexof, 1);
            }
            indexof = validBillNos.indexOf(invalidtermApplied[term]);
            if (indexof != -1) {
                validBillNos.splice(indexof, 1);
            }
            indexof = validCurrency.indexOf(invalidtermApplied[term]);
            if (indexof != -1) {
                validCurrency.splice(indexof, 1);
            }
        }
        
        
        /*  Written a logic if "Including GST" have been applied for any customer 
         * 
         * then remove the record from valid as well as invalid array 
         */
        var indexof = "";
        for (var term = 0; term < includingGSTArr.length; term++) {
            indexof = invalidbillNos.indexOf(includingGSTArr[term]);
            if (indexof != -1) {
                invalidbillNos.splice(indexof, 1);

            }
            indexof = invalidCurrency.indexOf(includingGSTArr[term]);
            if (indexof != -1) {
                invalidCurrency.splice(indexof, 1);
            }
            indexof = validBillNos.indexOf(includingGSTArr[term]);
            if (indexof != -1) {
                validBillNos.splice(indexof, 1);
            }
            indexof = validCurrency.indexOf(includingGSTArr[term]);
            if (indexof != -1) {
                validCurrency.splice(indexof, 1);
            }
        }
       
        /*  Preparing rec for only one record selected from grid*/
        if (this.recArr.length == 1) {
            this.detailRec = {
                billno: this.recArr[0].data.billno,
                details: '',
                Isvalid: 'Valid',
                personname: this.recArr[0].data.personname,
                text:this.isfromReportList ? 'Delivery Order(s) of following ' +moduleName +' will be created' : 'Invoices of following '+moduleName + ' will be created'
            }
            var record = new this.detailStore.recordType(this.detailRec, count);
            this.detailStore.add(record);
            count++;
        }

        /*  Preparing Validation messages for valid records*/
        var count = this.detailStore.getCount.length;
        for (var i = 0; i < validBillNos.length; i++) {
            for (var j = 0; j < this.recArr.length; j++) {
                if (validBillNos[i] == this.recArr[j].data.personid) {
                    this.detailRec = {
                        billno: this.recArr[j].data.billno,
                        details: '',
                        Isvalid: 'Valid',
                        personname: this.recArr[j].data.personname,
                        text: this.isfromReportList ? 'Delivery Order(s) of following ' +moduleName +' will be created' : 'Invoices of following ' +moduleName+' will be created'
                    }
                    var record = new this.detailStore.recordType(this.detailRec, count);
                    this.detailStore.add(record);
                    count++;
                }
            }

        }
        
        if (this.recArr.length != 1 || this.personStore.data.items.length != this.recArr.length) {
            for (var j = 0; j < validBillNos.length; j++) {
                for (var i = 0; i < this.recArr.length; i++) {
                    if (validBillNos[j] == this.recArr[i].data.personid) {
                        arr.push(this.Store.indexOf(this.recArr[i]));
                    }
                }
            }
        }


        if (this.recArr.length == 1 || this.personStore.data.items.length == this.recArr.length) {
            for (var i = 0; i < this.recArr.length; i++) {
                arr.push(this.Store.indexOf(this.recArr[i]));
            }
        }


        data = WtfGlobal.getJSONArray(this.grid, true, arr);

        for (var i = 0; i < invalidbillNos.length; i++) {
            for (var j = 0; j < this.recArr.length; j++) {
                if (invalidbillNos[i] == this.recArr[j].data.personid) {
                    this.detailRec = {
                        billno: this.recArr[j].data.billno,
                        details: '<span style="color:red;">' + 'This record should have same global level tax as of other <br>'+moduleName+' of same'+(this.isCustomer?' customer.':' vendor.') + '</span>',
                        Isvalid: 'Invalid',
                        personname: this.recArr[j].data.personname,
                        text: this.isfromReportList ? 'Delivery Order(s) of following ' +moduleName +' will not be created' : 'Invoices of following '+ moduleName +' will not be created'

                    }
                    //            this.detailStore.insert(this.invoice.PO.store.getCount(), j);
                    var record = new this.detailStore.recordType(this.detailRec, count);
                    this.detailStore.add(record);
                    count++;
                }
            }

        }
        
        /* Preparing invalid rec if "Including GST" is true*/
        for (var i = 0; i < includingGSTArr.length; i++) {
            for (var j = 0; j < this.recArr.length; j++) {
                if (includingGSTArr[i] == this.recArr[j].data.personid) {
                    this.detailRec = {
                        billno: this.recArr[j].data.billno,
                        details: '<span style="color:red;">' + 'This record should not have "Including GST" true as of other <br>' + moduleName + ' of same'+(this.isCustomer?' customer.':' vendor.') + '</span>',
                        Isvalid: 'Invalid',
                        personname: this.recArr[j].data.personname,
                        text: this.isfromReportList ? 'Delivery Order(s) of following ' +moduleName +' will not be created' : 'Invoices of following ' + moduleName + ' will not be created'

                    }
                   
                    var record = new this.detailStore.recordType(this.detailRec, count);
                    this.detailStore.add(record);
                    count++;
                }
            }

        }
        
        
        /* Preparing invalid rec if "Invoice Terms" is applied*/
        for (var i = 0; i < invalidtermApplied.length; i++) {
            for (var j = 0; j < this.recArr.length; j++) {
                if (invalidtermApplied[i] == this.recArr[j].data.personid) {
                    this.detailRec = {
                        billno: this.recArr[j].data.billno,
                        details: '<span style="color:red;">' + 'This record should not have "Invoice terms" as of other <br>' + moduleName + ' of same'+(this.isCustomer?' customer.':' vendor.') + '</span>',
                        Isvalid: 'Invalid',
                        personname: this.recArr[j].data.personname,
                        text: this.isfromReportList ? 'Delivery Order(s) of following ' +moduleName +' will not be created' : 'Invoices of following ' + moduleName + ' will not be created'

                    }
     
                    var record = new this.detailStore.recordType(this.detailRec, count);
                    this.detailStore.add(record);
                    count++;
                }
            }

        }
        
              
        for (var i = 0; i < invalidCurrency.length; i++) {
            for (var j = 0; j < this.recArr.length; j++) {
                if (invalidCurrency[i] == this.recArr[j].data.personid) {
                    this.detailRec = {
                        billno: this.recArr[j].data.billno,
                        details: '<span style="color:red;">' + 'This record should have same Currency as of other <br>' + moduleName + ' of same'+(this.isCustomer?' customer.':' vendor.') + '</span>',
                        Isvalid: 'Invalid',
                        personname: this.recArr[j].data.personname,
                        text: this.isfromReportList ? 'Delivery Order(s) of following ' +moduleName +' will not be created' : 'Invoices of following '+ moduleName+ ' will not be created'

                    }
                    //            this.detailStore.insert(this.invoice.PO.store.getCount(), j);
                    var record = new this.detailStore.recordType(this.detailRec, count);
                    this.detailStore.add(record);
                    count++;
                }
            }

        }
        
        /*Here iterating on store of valid & invalid records */
        for (var i = 0; i < this.detailStore.getCount(); i++) {
            var val = this.detailStore.data.items[i].data.Isvalid;
            val = val.replace(/(<([^>]+)>)/ig, "");
            if (val == "Invalid") {
                chk++;
            }

        }
        
       /*  If all records are invalid*/
        if (chk == this.detailStore.getCount()) {
            flag = 1;
        }

        this.detailStoreGroup = new Wtf.data.GroupingStore({
            fields: this.detailRec,
            groupField: 'text'
        });
        this.detailStoreGroup.removeAll();
        this.detailStore.each(function(rec) {
            this.detailStoreGroup.add(rec);
        }, this);
        Wtf.ValidateInvoiceRecords(this.detailStoreGroup, this.invColumnModelArr, data, comboVal, flag, this.Grid, this.moduleid,this.isfromReportList);//passing parameter "moduleid" to identify from which module it is called
    },
    closeAction: function() {
        this.win.close();
    },
    createIndividaulOrBulkInvoice: function(btn) {
      
        /* -------------- Bulk DO can be made only from Outstanding Blocked SO(s)----------*/
        if (this.isfromReportList) {
            var isBlockAll = true;
            var blockArr = "";
            this.recArr = this.grid.getSelectionModel().getSelections();
            for (var i = 0; i < this.recArr.length; i++) {

                if (this.recArr[i].json.islockQuantityflag) {
                    blockArr += this.recArr[i].data.billno + ", ";

                } else {
                    isBlockAll = false;
                }

            }

            if (blockArr.length > 0) {
                blockArr = blockArr.substring(0, blockArr.length - 2);
            }

            if (!isBlockAll && blockArr.length > 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "You can  make bulk DO(s) Only for selected blocked SO(s)" + " " + "<b>" + blockArr + "</b>" + ". So please select only blocked SO(s)."], 2);
                return;
            } else if (!isBlockAll) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Only select blocked SO(s) to create bulk DO(s)"], 2);
                return;
            }
        }
        
        var buttonId = btn.id;
        this.submit = new Wtf.Toolbar.Button({
            text: "Submit",
            tooltip: 'Submit',
            id: 'submit',
            scope: this,
            handler: buttonId == 'bulkInv' ? this.submitAction : this.individualSubmit
        });
        this.cancel = new Wtf.Toolbar.Button({
            text: "Cancel",
            tooltip: 'Cancel',
            id: 'Cancel',
            scope: this,
            handler: this.closeAction
        });
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },
        {
            name: 'value'
        }
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: this.isCustomer?(this.isfromReportList ? "autodo" :"autoinvoice"):"autogoodsreceipt"
            //                isEdit: this.copyInv ? false : this.isEdit
            }
        });
        this.sequenceFormatStore.load();
       this.sequenceFormatStore.on('load', this.removeNASequenceFormat, this)
        

        this.sequenceFormatCombobox = new Wtf.form.ComboBox({
            //        labelSeparator:'',
            labelWidth: 100,
            triggerAction: 'all',
            mode: 'local',
            id: 'sequenceFormatCombobox',
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            disabled: false,
            width: 200,
            //typeAhead: true,
            forceSelection: true,
            emptyText: 'Select Sequence Format',
            name: 'sequenceformat',
            //            hiddenName: 'sequenceformat',
            fieldLabel: 'Select Sequence Format',
            allowBlank: false


        });
        var descWindow = Wtf.getCmp(this.id + 'DescWindow')
        if (descWindow == null) {
            this.win = new Wtf.Window
            ({
                width: 400,
                height: 130,
                title: "Select Sequence Format",
                id: 'sequenceFormatWindow',
                bodyBorder: false,
                layout: 'fit',
                items: [
                {
                    xtype: 'panel',
                    layout: 'form',
                    //autoScroll: true,
                    border: false,
                    method: 'POST',
                    scope: this,
                    width: 400,
                    labelWidth: 150,
                    region: 'center',
                    bodyStyle: 'background:#F1F1F1;padding-left:10px;padding-top:30px;padding-bottom:10px;',
                    items: [this.sequenceFormatCombobox]
                    }

                ],
                closable: true,
                resizable: false,
                modal: true,
                buttons: [this.submit, this.cancel]
            });
        }
        this.win.show();
        
    },
    
    
  removeNASequenceFormat:function(){
    
    /*  Removing "NA" sequence format because there is no use of NA here */
    for(var sequenceFormatCount=0; sequenceFormatCount<this.sequenceFormatStore.getCount() ; sequenceFormatCount++){
       if( this.sequenceFormatStore.getAt(sequenceFormatCount).data.id=="NA"){
           this.sequenceFormatStore.remove(this.sequenceFormatStore.getAt(sequenceFormatCount))
           break;
       }
    }
  },
    showWindow: function() {

    },
    genSuccessResponse: function (response, request) {
       Wtf.getCmp('gridmsg1').getStore().load({
            params: {
                start: 0, 
                limit: 30
            }
        });
    WtfComMsgBox(["Success", response.msg], response.success * 2 + 1);
//        this.fireEvent('loadMainReportTab', this);
    }, 
genFailureResponse: function(response) {
    WtfGlobal.resetAjaxTimeOut();
    Wtf.MessageBox.hide();
    var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
    if (response.msg)
        msg = response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
},
storeloaded: function(store) {
    //      this.hideLoading();
    this.quickPanelSearch.StorageChanged(store);
},
enableDisableButtons: function() {
    var arr = this.grid.getSelectionModel().getSelections();
    if (arr.length == 0) {
        this.createIndInv.setDisabled(true);
        this.createBulkInv.setDisabled(true);
    } else {
        this.createIndInv.setDisabled(false);
        this.createBulkInv.setDisabled(false);
    }


},
loadStore: function() {
    this.Store.load({
        params: {
            start: 0,
            limit : (this.pP!=undefined && this.pP.combo!=undefined) ? this.pP.combo.value:30,
            ss: this.quickPanelSearch.getValue()
        }
    });
    this.Store.on('load', this.storeloaded, this);
},
submitHandler: function() {
    this.loadStore();
},
handleResetClick: function() {
    if (this.quickPanelSearch.getValue()) {
        this.quickPanelSearch.reset();
        this.loadStore();
        this.Store.on('load', this.storeloaded, this);
    }
}
});
/*-------------------- Function to show Validate Windows -----------------*/

Wtf.ValidateInvoiceRecords = function(store, invColumnModelArr, data, comboVal, flag, Grid,moduleid,isfromReportList) {


    var validateWindow = Wtf.getCmp("IWValidationWindow");
    if (!validateWindow) {
        new Wtf.ValidationWindow({
            title: WtfGlobal.getLocaleText("acc.common.valInvoiceAnalysis"), //"Validation Analysis Report",
            store: store,
            invColumnModelArr: invColumnModelArr,
            data: data,
            comboVal: comboVal,
            flag: flag,
            gridBulk: Grid,
            moduleid:moduleid,
            isfromReportList:isfromReportList
        }).show();
    } else {
        validateWindow.show();
    }
}

Wtf.ValidationWindow = function(config) {
    Wtf.IWValidationWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.ValidationWindow, Wtf.Window, {
    iconCls: 'importIcon',
    width: 750,
    height: 570,
    modal: true,
    layout: "border",
    id: 'ValidationWindow',
    closable: false,
    initComponent: function(config) {
        Wtf.ValidationWindow.superclass.initComponent.call(this, config);
        this.okButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.msgbox.ok"), //"Import Data",
            scope: this,
            minWidth: 80,
            handler: this.okAction
        });
        this.cancelButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
            scope: this,
            minWidth: 80,
            handler: function() {
                Wtf.getCmp('ValidationWindow').close();
            //          Wtf.getCmp('sequenceFormatWindow').show();
            }
        });
        this.buttons = [this.okButton, this.cancelButton];
    },
    onRender: function(config) {
        Wtf.IWValidationWindow.superclass.onRender.call(this, config);

        this.northMessage = "<ul style='list-style-type:disc;padding-left:15px;'>" +
        "<li>" +this.isfromReportList ? WtfGlobal.getLocaleText("acc.import.deliveryOrderOk") : WtfGlobal.getLocaleText("acc.import.invoiceOk") + "</li>";
        if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            if (this.isfromReportList) {
               this.headermsg = WtfGlobal.getLocaleText("acc.common.listInvalidOrValidRecordforSelectedSOForDO");
            } else {
                this.headermsg = WtfGlobal.getLocaleText("acc.common.listInvalidOrValidRecordforSelectedSO");
            }
        } else if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
            this.headermsg = WtfGlobal.getLocaleText("acc.common.listInvalidOrValidRecordforSelectedPO");
            this.northMessage = "<ul style='list-style-type:disc;padding-left:15px;'>" +
            "<li>"+ WtfGlobal.getLocaleText("acc.import.purchaseinvoiceOk") + "</li>";
        } else {
            this.headermsg = WtfGlobal.getLocaleText("acc.common.listInvalidOrValidRecord");
        }
        
        this.add(this.northPanel = new Wtf.Panel({
            region: 'north',
            height: 70,
            border: false,
            bodyStyle: 'background:white;padding:7px',
            html: getImportTopHtml(this.headermsg , this.northMessage + "</ul>", "../../images/import.png", true, "0px", "2px 0px 0px 10px")
        }));
        if (this.flag == 1) {
            this.okButton.setDisabled(true);
        }
        //        this.gridView = new Wtf.ux.grid.BufferView({
        //            scrollDelay: false,
        //            autoFill: true
        //        });
        this.Grid = new Wtf.grid.GridPanel({
            store: this.store,
            // sm: this.sm,
            border: true,
            loadMask: true,
            //            view: this.gridView,

            view: new Wtf.grid.GroupingView({
                forceFit: true,
                showGroupName: false,
                enableGroupingMenu: true,
                groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ?"' + WtfGlobal.getLocaleText("acc.item.plural") + '":"' + WtfGlobal.getLocaleText("acc.item") + '"]})', //"Items" : "Item"]})',
                hideGroupedColumn: true,
                emptyText: "<div class='grid-empty-text'>" + WtfGlobal.getLocaleText("acc.common.norec") + "</div>"
            }),
            cm: new Wtf.grid.ColumnModel(this.invColumnModelArr)
        //            bbar: this
        });


        this.add({
            region: 'center',
            layout: 'fit',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:white;padding:7px',
            items: this.Grid
        });

    },
    change: function(val) {
        if (val > 0) {
            return '<span style="color:green;">' + val + '</span>';
        } else if (val < 0) {
            return '<span style="color:red;">' + val + '</span>';
        }
        return val;
    },
    // example of custom renderer function
    pctChange: function(val) {
        if (val > 0) {
            return '<span style="color:green;">' + val + '%</span>';
        } else if (val < 0) {
            return '<span style="color:red;">' + val + '%</span>';
        }
        return val;
    },
    okAction: function() {

        
        var transaction = "";
        if (this.isfromReportList) {
            transaction = "Delivery Order(s)"
        } else {
            transaction = "Invoices";
        }


        if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) {
            if (this.isfromReportList) {
                this.bulksaveurl = "ACCInvoice/saveBulkDOFromSO.do";
            } else {
                this.bulksaveurl = "ACCInvoice/saveBulkInvoicesFromSO.do";
            }

        } else if (this.moduleid == Wtf.Acc_Purchase_Order_ModuleId) {
            this.bulksaveurl = "ACCGoodsReceipt/saveBulkInvoicesFromPO.do";
        } else {
            this.bulksaveurl = "ACCInvoice/saveBulkInvoice.do";
        }

        Wtf.MessageBox.confirm("Confirm", "Are you sure you want to create "+transaction+ "?", function(btn) {
            var obj = this;
            if (btn == 'yes') {
                Wtf.getCmp('ValidationWindow').close();
                //obj.win.close();
                Wtf.Ajax.requestEx({
                    url:this.bulksaveurl,
                    params: {
                        data: this.data,
                        sequenceformatInvoice: this.comboVal,
                        bulkInvoices: true
                    }
                }, this, this.genSuccessResponse, this.genFailureResponse);

            } else if (btn == 'no') {
                return;
            }
        }, this);


    },
    genSuccessResponse: function(response, request) {
        Wtf.getCmp('gridmsg1').getStore().load({
            params: {
                start: 0, 
                limit: 30
            }
        });
    WtfComMsgBox(["Success", response.msg], response.success * 2 + 1);
}, 
genFailureResponse: function(response) {
    WtfGlobal.resetAjaxTimeOut();
    Wtf.MessageBox.hide();
    var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
    if (response.msg)
        msg = response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
}

});
