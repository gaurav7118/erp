/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function callServiceTaxComputationDynamicLoad() {
    var panel = Wtf.getCmp('ServiceTaxComputationReport');
    if (panel == null) {
        panel = new Wtf.account.ServiceTaxComputationReport({
            id: 'ServiceTaxComputationReport',
            border: false,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.title"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.title"), //Service Tax Computation Report
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
Wtf.account.ServiceTaxComputationReport = function(config) {
    this.loadingMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    this.tbararray = new Array();
    this.fetchButton = new Wtf.Toolbar.Button({
        text: 'Fetch',
        scope: this,
        tooltip: 'Fetch Report',
        handler: this.fetchReportHandler,
        iconCls: 'accountingbase fetch'
    });
    this.startDate = new Wtf.form.DateField({
        fieldLabel: WtfGlobal.getLocaleText("acc.bankReconcile.startDate") + '*',
        format: WtfGlobal.getOnlyDateFormat(),
        value: this.getDates(true),
        anchor: '60%',
        name: "startdate",
        allowBlank: false
    });
    this.tbararray.push("From ", this.startDate);
    this.toDate = new Wtf.form.DateField({
        fieldLabel: WtfGlobal.getLocaleText("acc.bankReconcile.endDate") + '*',
        format: WtfGlobal.getOnlyDateFormat(),
        value: this.getDates(false),
        anchor: '60%',
        name: "enddate",
        allowBlank: false
    });
    this.startDate.on('change', function(field, newval, oldval) {
        if (field.getValue() != '' && this.toDate.getValue() != '') {
            if (field.getValue().getTime() > this.toDate.getValue().getTime()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                field.setValue(oldval);
            }
        }
    }, this);
    this.toDate.on('change', function(field, newval, oldval) {
        if (field.getValue() != '' && this.startDate.getValue() != '') {
            if (field.getValue().getTime() < this.startDate.getValue().getTime()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                field.setValue(oldval);
            }
        }
    }, this);
    this.tbararray.push("To ", this.toDate);
    this.tbararray.push(this.fetchButton);

    this.creditAdjustmentButton = new Wtf.Toolbar.Button({
        text: 'Credit Adjustment',
        scope: this,
        tooltip: 'Credit Adjustment',
        handler: this.creditAdjustmentWindowHandler
    });
    this.tbararray.push(this.creditAdjustmentButton);
//    this.ServiceTaxPaymentBttn = new Wtf.Toolbar.Button({
//        text:WtfGlobal.getLocaleText("acc.serviceTaxcomputationreport.stPayment"),
//        tooltip: WtfGlobal.getLocaleText("acc.serviceTaxcomputationreport.stPayment"),
//        id: 'servicetaxpayment' + this.id,
//        hidden:true,
//        scope: this,
//        handler: this.ServiceTaxPaymentHandler
//    });
//    this.tbararray.push(this.ServiceTaxPaymentBttn);
    this.moduleHeaderRec = new Wtf.data.Record.create([
        {name: "particulars"},
        {name: "taxtype"},
        {name: "accessablevalue"},
        {name: "servicetaxamount"},
        {name: "kkcamount"},
        {name: "sbcamount"},
        {name: "totalamount"},
        {name: "dutyamount"},
        {name: "account"}
    ]);
    this.moduleHeaderReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty: "count"
    }, this.moduleHeaderRec);

    this.moduleHeaderStore = new Wtf.data.GroupingStore({
        url: "ACCInvoiceCMN/getServiceTaxComputationReport.do",
        reader: this.moduleHeaderReader,
        baseParams: {
            enddate: this.getDates(false),
            startdate: this.getDates(true)
        },
        groupField: "taxtype",
        sortInfo: {
            field: 'particulars',
            direction: "ASC"
        }
    });
    this.moduleHeaderStore.on('beforeload', function() {
        this.moduleHeaderStore.baseParams.enddate = WtfGlobal.convertToGenericEndDate(this.toDate.getValue());
        this.moduleHeaderStore.baseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.loadingMask.show();
    }, this);
    this.moduleHeaderStore.on('load', function() {
        this.loadingMask.hide();
        this.updateBalanceServiceTaxPayable();
    }, this);
    this.totalValue = new Wtf.Toolbar.TextItem(""); // total value
    this.bbar1 = new Array();
    this.moduleHeaderStore.on('datachanged', function(store) {
        var grandTotalInBaseCurrency = 0;
        var recordindex = store.data.length - 1;
        for (var i = 0; i <= recordindex; i++) {
            if (store.getAt(i).json.dutyamount != undefined) {
                grandTotalInBaseCurrency = parseFloat(grandTotalInBaseCurrency) + parseFloat(store.getAt(i).json.dutyamount);
            }
        }
        this.totalValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(grandTotalInBaseCurrency, WtfGlobal.getCurrencySymbol()) + "</B>\\t";
    }, this);
    this.moduleHeaderStore.on('loadexception', function() {
        this.loadingMask.hide();
    }, this);
    this.moduleHeaderStore.load();
    this.colSpan = new Wtf.GroupHeaderGrid({
        rows: [[{
                    align: "center",
                    header: "",
                    colspan: 0
                }, {
                    align: "center",
                    header: "",
                    colspan: 0
                }, {
                    align: "center",
                    header: "",
                    colspan: 0
                }, {
                    align: "center",
                    header: "",
                    colspan: 0
                }, {
                    align: "center",
                    header: "Tax Amount",
                    colspan: 4
                },
            ]],
        hierarchicalColMenu: false
    });
    this.moduleHeaderColumn = new Wtf.grid.ColumnModel([
        {
            header: "Catagory Type",
            hidden: true,
            dataIndex: "taxtype",
            summaryType: "min"

        }, {
            header: WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.particulars"),
            dataIndex: "particulars",
            sortable: false,
            width: 300,
            groupRenderer: WtfGlobal.nameRenderer,
            summaryRenderer: function() {
                return '<div class="grid-summary-common"; align="right";>' + WtfGlobal.getLocaleText("acc.common.total") + '</div>'
            }

        }, {
            header: WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.totalAmount"),
            dataIndex: "totalamount",
            sortable: false,
            renderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {//Accessable value column value is to be shown only in case of "A. Input Credit".
                    if (value == 'chkboxval') {
                        return '<input type="checkbox">'
                    } else {
                        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
                        var v = parseFloat(value);
                        if (isNaN(v))
                            return value;
                        v = WtfGlobal.conventInDecimal(v, symbol)
                        return '<div class="currency">' + v + '</div>';
                    }
                } else {
                    return "";
                }
            },
            hidecurrency: true,
            summaryType: 'sum',
            summaryRenderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {//Accessable value column value is to be shown only in case of "A. Input Credit".
                    return WtfGlobal.currencySummaryRenderer(value, m, rec);
                } else {
                    return "";
                }
            }

        }, {
            header: WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.assessableValue"),
            dataIndex: "accessablevalue",
            sortable: false,
            renderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {//Accessable value column value is to be shown only in case of "A. Input Credit".
                    if (value == 'chkboxval') {
                        return '<input type="checkbox">'
                    } else {
                        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
                        var v = parseFloat(value);
                        if (isNaN(v))
                            return value;
                        v = WtfGlobal.conventInDecimal(v, symbol)
                        return '<div class="currency">' + v + '</div>';
                    }
                } else {
                    return "";
                }
            },
            hidecurrency: true,
            summaryType: 'sum',
            summaryRenderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {//Accessable value column value is to be shown only in case of "A. Input Credit".
                    return WtfGlobal.currencySummaryRenderer(value, m, rec);
                } else {
                    return "";
                }
            }

        }, {
            header: WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.ServiceTaxAmount"),
            dataIndex: "servicetaxamount",
            hidden: false,
            renderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {
                    if (value == 'chkboxval') {
                        return '<input type="checkbox">'
                    } else {
                        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
                        var v = parseFloat(value);
                        if (isNaN(v))
                            return value;
                        v = WtfGlobal.conventInDecimal(v, symbol)
                        return '<div class="currency">' + v + '</div>';
                    }
                } else {
                    return "";
                }
            },
            hidecurrency: true,
            summaryType: 'sum',
            summaryRenderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {
                    return WtfGlobal.currencySummaryRenderer(value, m, rec);
                } else {
                    return "";
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.KKCAmount"),
            dataIndex: "kkcamount",
            hidden: false,
            renderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {
                    if (value == 'chkboxval') {
                        return '<input type="checkbox">'
                    } else {
                        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
                        var v = parseFloat(value);
                        if (isNaN(v))
                            return value;
                        v = WtfGlobal.conventInDecimal(v, symbol)
                        return '<div class="currency">' + v + '</div>';
                    }
                } else {
                    return "";
                }
            },
            hidecurrency: true,
            summaryType: 'sum',
            summaryRenderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {
                    return WtfGlobal.currencySummaryRenderer(value, m, rec);
                } else {
                    return "";
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.SBCAmount"),
            dataIndex: "sbcamount",
            hidden: false,
            renderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {
                    if (value == 'chkboxval') {
                        return '<input type="checkbox">'
                    } else {
                        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
                        var v = parseFloat(value);
                        if (isNaN(v))
                            return value;
                        v = WtfGlobal.conventInDecimal(v, symbol)
                        return '<div class="currency">' + v + '</div>';
                    }
                } else {
                    return "";
                }
            },
            hidecurrency: true,
            summaryType: 'sum',
            summaryRenderer: function(value, m, rec) {
                if (rec.data.taxtype == "A. Input Credit" || rec.data.taxtype == "B. Service Tax Payable") {
                    return WtfGlobal.currencySummaryRenderer(value, m, rec);
                } else {
                    return "";
                }
            }
        }, {
            header: WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.totalTaxAmount"),
            dataIndex: "dutyamount",
            hidden: false,
            renderer: function(value, m, rec) {
                if (value == 'chkboxval') {
                    return '<input type="checkbox">'
                } else {
                    var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
                    var v = parseFloat(value);
                    if (isNaN(v))
                        return value;
                    v = WtfGlobal.conventInDecimal(v, symbol)
                    return '<div class="currency">' + v + '</div>';
                }
            },
            hidecurrency: true,
            summaryType: 'sum',
            summaryRenderer: function(value, m, rec) {
                return WtfGlobal.currencySummaryRenderer(value, m, rec);
            }
        }
    ]);
    this.groupingView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: true,
        hideGroupedColumn: true
    });

    var gridSummary = new Wtf.grid.GroupSummary({});
    this.moduleHeaderGrid = new Wtf.grid.GridPanel({
        store: this.moduleHeaderStore,
        border: false,
        height: 500,
        cm: this.moduleHeaderColumn,
        view: this.groupingView,
        plugins: [gridSummary, this.colSpan]
    });
    this.wrapperPanel = new Wtf.Panel({
        border: false,
        layout: "border",
        scope: this,
        items: [
            this.centerPanel = new Wtf.Panel({
                width: '90%',
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.moduleHeaderGrid]
            })
        ]
    });
    Wtf.apply(this, {
        defaults: {
            border: false,
            bodyStyle: "background-color:white;"
        },
        items: this.wrapperPanel,
        tbar: this.tbararray,
        bbar: this.bbar1
    }, config);
    Wtf.account.ServiceTaxComputationReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.ServiceTaxComputationReport, Wtf.Panel, {
    getDates: function(start) {
        var d = new Date();
        if (this.statementType == 'BalanceSheet') {
            if (start)
                return new Date('January 1, 1970 00:00:00 AM');
            else
                return d;
        }
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom)
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd)
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        if (start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    onRender: function(config) {
        Wtf.account.ServiceTaxComputationReport.superclass.onRender.call(this, config);
        this.moduleHeaderStore.load();
    },
    creditAdjustmentWindowHandler: function() {
        var cenvatavailable = 0;
        var dutypayable = 0;
        for (var j = 0; j < (this.moduleHeaderStore.data.items.length - 1); j++) {
            var rec = this.moduleHeaderStore.data.items[j];
            if (rec.data.taxtype == "A. Input Credit") { //|| rec.data.taxtype == "B. Service Tax Payable"){
                cenvatavailable += rec.data.dutyamount;
            }
            if (rec.data.taxtype == "D. Balance Service Tax Payable (B-C)" && rec.data.particulars == "Balance Service Tax Payable (B-C)") {
                dutypayable += rec.data.dutyamount;
            }
        }
        this.rn = new Wtf.grid.RowNumberer();
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.cm = new Wtf.grid.ColumnModel([this.rn, this.sm, {
                header: "Invoice Number",
                width: 100,
                dataIndex: 'documentnumber'
            }, {
                header: "Assessable Value",
                width: 100,
                dataIndex: 'assessablevalue'
            }
            , {
                header: "Service Tax",
                width: 100,
                dataIndex: 'servicetax'
            }
            , {
                header: "KKC",
                width: 100,
                dataIndex: 'kkc'
            }
            , {
                header: "Excise Duty",
                width: 100,
                dataIndex: 'exciseduty'
            }
        ]);

        this.Rec = new Wtf.data.Record.create([
            {name: 'documentnumber'},
            {name: 'documentid'},
            {name: 'currencysymbol'},
            {name: 'currencyid'},
            {name: 'assessablevalue'},
            {name: 'dateofentry'},
            {name: 'servicetax'},
            {name: 'kkc'},
            {name: 'exciseduty'},
            {name: 'moduleid'}
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, this.Rec),
            url: "ACCCombineReports/getCreditAvailedGoodsReceipt.do",
            baseParams: {
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                stdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                excludeJE: true,
                isSTCompReport: true
            }
        });
        this.store.load();

        this.grid = new Wtf.grid.EditorGridPanel({
            clicksToEdit: 1,
            store: this.store,
            height: 420,
            sm: this.sm,
            width: 400,
            scope: this,
            cm: this.cm,
            serivceTaxSelected: 0,
            selectedRecJson: '',
            dutypayable: dutypayable,
            cenvatavailable: cenvatavailable,
            maxAdjustsmentValue: dutypayable < cenvatavailable ? dutypayable : cenvatavailable,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.sm.on('beforerowselect', function(obj, rowIndex, keepExisting, selectedRec) {
            var selections = this.grid.getSelectionModel().getSelections();
            var servicetax = 0.0;
            var finalArr = [];
            for (var i = 0; i < selections.length; i++) {
                var rec = selections[i];
                finalArr.push(rec.data);
                if (rec && rec.data) {
                    servicetax += parseFloat(rec.data.servicetax);
                }
            }
            if (selectedRec && selectedRec.data && selectedRec.data.servicetax) {
                finalArr.push(selectedRec.data);
                servicetax += parseFloat(selectedRec.data.servicetax);
                if (selectedRec.data.kkc) {
                    servicetax += parseFloat(selectedRec.data.kkc);
                }
//                if(selectedRec.data.exciseduty){
//                    servicetax += parseFloat(selectedRec.data.exciseduty);
//                }
            }

            if (this.grid.maxAdjustsmentValue && servicetax <= this.grid.maxAdjustsmentValue) {
                this.grid.serivceTaxSelected = servicetax;
                var element = document.getElementById('cenvatcredit');
                element.innerHTML = "<span style='padding-left:10px'><b>Duty Payable : </b>" + getRoundedAmountValue(this.grid.dutypayable) + "<b></span><span style='padding-left:10px'>CENVAT Available : </b>" + getRoundedAmountValue(this.grid.cenvatavailable) + "<b><span style='padding-left:10px'>Adjustment Amount : </b>" + getRoundedAmountValue(servicetax) + "</span>";
                this.grid.selectedRecJson = finalArr;
                return true;
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.credit.adjust.error.msg")], 2);
                return false;
            }
        }, this);
        this.sm.on('rowdeselect', function(obj, rowIndex, record) {
            var servicetax = 0.0;
            if (record && record.data && record.data.servicetax) {
                servicetax = this.grid.serivceTaxSelected;
                servicetax -= parseFloat(record.data.servicetax);
                if (record.data.kkc) {
                    servicetax -= parseFloat(record.data.kkc);
                }
//                if(record.data.exciseduty){
//                    servicetax -= parseFloat(record.data.exciseduty);
//                }
                if (servicetax > 0) {
                    this.grid.serivceTaxSelected = servicetax;
                }
                var element = document.getElementById('cenvatcredit');
                element.innerHTML = "<span style='padding-left:10px'><b>Duty Payable : </b>" + getRoundedAmountValue(this.grid.dutypayable) + "<b></span><span style='padding-left:10px'>CENVAT Available : </b>" + getRoundedAmountValue(this.grid.cenvatavailable) + "<b><span style='padding-left:10px'>Adjustment Amount : </b>" + getRoundedAmountValue(servicetax) + "</span>";
            }
        }, this);

        var bodyDesign = "Payable From : " + this.getDates(true).format(WtfGlobal.getOnlyDateFormat()) + " To " + this.getDates(false).format(WtfGlobal.getOnlyDateFormat());
        this.creditAdjustmentWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.STCompReport.ServiceTaxAdjustment"),
            height: 400,
            width: 600,
            modal: true,
            resizable: false,
            items: [{
                    region: 'north',
                    height: 75,
                    border: false,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.STCompReport.ServiceTaxAdjustment"), bodyDesign, "../../images/accounting_image/price-list.gif", true)
                }, {
                    region: 'center',
                    border: false,
                    height: 20,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: "<div id = 'cenvatcredit'><span style='padding-left:10px'><b>Duty Payable : </b>" + getRoundedAmountValue(dutypayable) + "<b></span><span style='padding-left:10px'>CENVAT Available : </b></span>" + getRoundedAmountValue(cenvatavailable) + "<b><span style='padding-left:10px'>Adjustment Amount : </b>0</span></div>"
                }, this.southPanel = new Wtf.Panel({
                    border: false,
                    region: 'south',
                    id: 'southpan' + this.id,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                    baseCls: 'bckgroundcolor',
                    layout: 'fit',
                    height: 320,
                    items: [this.grid]
                })
            ],
            buttons: [{
                    text: 'Save',
                    scope: this,
                    handler: function() {
                        var reportRec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.find("particulars", "Service Tax Credit Adjustment"));
                        if (this.grid.serivceTaxSelected) {
                            reportRec.set("dutyamount", this.grid.serivceTaxSelected);
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.exciseComputationReport.creditadjustment.invoicenotselectedmsg")], 2);
                            return false;
                        }
                        this.updateBalanceServiceTaxPayable();
                        this.creditAdjustmentHandler(reportRec.get("dutyamount"));
                        this.creditAdjustmentWin.close();
                    }
                }, {
                    text: 'Close',
                    scope: this,
                    handler: function() {
                        this.creditAdjustmentWin.close();
                    }
                }
            ]
        });
        this.creditAdjustmentWin.show();
    },
    creditAdjustmentHandler: function(adjustamount) {
        Wtf.Ajax.requestEx({// request to fetch json of particular report, by ID
            url: "ACCInvoiceCMN/getIndiaComplianceReportData.do",
            params: {
                reportid: "12", //Service Tax Computation Report: Credit Adjustment
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                CreditAdjustmentflag: true,
                selectedIdJson: JSON.stringify(this.grid.selectedRecJson),
                adjustamount: this.grid.serivceTaxSelected,
                dutypayable: this.grid.dutypayable,
                isSTCompReport: true
            }
        }, this, function(resp, r) {
            if (resp != "") {
                Wtf.MessageBox.alert("Credit Adjustment", resp.msg, this);
            }
        }, function(resp) {
            if (resp != "" && resp.data != "" && resp.data.length > 0) {

            }
        });
    },
    fetchReportHandler: function() {
        this.moduleHeaderStore.load();
    },
    updateBalanceServiceTaxPayable: function() {
        var dutyamount = 0;
        var reportRec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.find("particulars", "Balance Service Tax Payable (B-C)"));
        for (var j = 0; j < (this.moduleHeaderStore.data.items.length - 1); j++) {
            var rec = this.moduleHeaderStore.data.items[j];
            if (rec.data.taxtype == "B. Service Tax Payable") {
                dutyamount += rec.data.dutyamount;
            } else if (rec.data.taxtype == "C. Service Tax Payments / Credit Adjustments") {
                if (rec.data.particulars == "G.A. R. 7 Payments") {
                    continue;
                }
                dutyamount -= rec.data.dutyamount;
            }
        }
        reportRec.set("dutyamount", dutyamount);
    }
});
