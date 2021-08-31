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

function callTDSMasterRatesDynamicLoad(tabid) {//TDS Master Rates, only in case of India
    var panel = Wtf.getCmp(tabid);
    if (panel == null) {
        panel = new Wtf.account.TDSMasterRates({
            layout: "fit",
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.TDSmasterConfig.tabTitle"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.TDSmasterConfig.tabTitleTT"),
            border: false,
            id: tabid,
            iconCls: 'accountingbase masterconfiguration',
            closable: true
        });
    }
    Wtf.getCmp('as').add(panel);
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    return panel;
}


Wtf.account.TDSMasterRates = function (config) {
    Wtf.account.TDSMasterRates.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true,
        'loadMasterGroup': true
    });
}
Wtf.extend(Wtf.account.TDSMasterRates, Wtf.Panel, {
    initComponent: function () {
        Wtf.account.TDSMasterRates.superclass.initComponent.call(this);
        this.getMasterItemGrid();
        this.mainPanel = new Wtf.Panel({
            layout: "border",
            border: false,
            items: [
                this.gridContainer
            ]
        });
        this.add(this.mainPanel);
    },
    getDates: function (start) {
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
    getMasterItemGrid: function () {
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);
        this.exportButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.export"),
            scope: this,
            iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
            filename: "TDS Rates Report",
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
        get: Wtf.autoNum.TDSMasterRateReport
        });
        var Import = [];
        this.ImportButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import'),
            menu: Import
        });

        this.tbararray = new Array();
        this.fetchButton = new Wtf.Toolbar.Button({
            text: 'Fetch',
            scope: this,
            tooltip: 'Fetch Report',
            handler: this.fetchReportHandler,
            iconCls: 'accountingbase fetch'
        });

        this.addNewRateButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddNewRate"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddNewRateTT"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: function(){
                this.addNewRateHandler("create");
            }
        });
        this.editRateButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.TDSMasterRates.EditRates"),
            scope: this,
            disabled:true,
            tooltip: WtfGlobal.getLocaleText("acc.TDSMasterRates.EditRatesTT"),
            iconCls: getButtonIconCls(Wtf.etype.edit),
            handler: function () {
                this.addNewRateHandler("edit");
            }
        });
        this.deleteTDSRatebtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.TDSMasterRates.DeleteTDSRate"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.TDSMasterRates.DeleteTDSRate"),
            disabled: true,
//            hidden:true,
            iconCls: getButtonIconCls(Wtf.etype.menudelete),
            handler: this.DeleteTDSRateHandler
        });
        this.TDSRatesForResibtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.TDSMasterRates.TDSRatesForResi"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.TDSMasterRates.TDSRatesForResitt"),
            iconCls: 'pwnd downloadDoc',
            hidden:true,
            handler: function () {
                Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/TDSRates.do?type=Resident";
            }
        });
        this.TDSRatesForNonResibtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.TDSMasterRates.TDSRatesForNonResi"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.TDSMasterRates.TDSRatesForNonResitt"),
            iconCls: 'pwnd downloadDoc',
            hidden:true,
            handler: function () {
                Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/TDSRates.do?type=NonResident";
            }
        });
        this.startDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.StartDate"),
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true),
            anchor: '60%',
            name: "startdate",
            allowBlank: false
        });

        this.endDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.EndDate"),
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false),
            anchor: '60%',
            name: "enddate",
            allowBlank: false
        });
        this.startDate.on('change', function (field, newval, oldval) {
            if (field.getValue() != '' && this.endDate.getValue() != '') {
                if (field.getValue().getTime() > this.endDate.getValue().getTime()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    field.setValue(oldval);
                }
            }
        }, this);
        this.endDate.on('change', function (field, newval, oldval) {
            if (field.getValue() != '' && this.startDate.getValue() != '') {
                if (field.getValue().getTime() < this.startDate.getValue().getTime()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                    field.setValue(oldval);
                }
            }
        }, this);
        
        this.MasterItemRec = new Wtf.data.Record.create([
            {name: "natureofpayment"},
            {name: "natureofpaymentid"},
            {name: "residentialstatus"},
            {name: "residentialstatusid"},
            {name: "tdsid"},
            {name: "basicexemptionpertransaction"},
            {name: "basicexemptionperannum"},
            {name: "deducteetype"},
            {name: "deducteetypeid"},
            {name: "tdsrate"},
            {name: "tdsratefromdate"},
            {name: "tdsratetodate"},
            {name: "tdsrateifpannotavailable"},
            {name: "fromamount"},
            {name: "toamount"}
        ]);
        this.MasterItemReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'totalCount'
        }, this.MasterItemRec);

        this.MasterItemStore = new Wtf.data.Store({
            url: "ACCVendorPaymentCMN/getTDSMasterRates.do",
            reader: this.MasterItemReader,
            baseParams: {
                mode: 112
            }
        });
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.rem.5") + "Nature Of Payment",
            width: 150,
            id: "quickSearch" + this.id,
            field: 'natureofpayment',
            Store: this.MasterItemStore
        });
        
        this.tbararray.push(this.quickPanelSearch, this.resetBttn);
        this.tbararray.push("From ", this.startDate, "To ", this.endDate, this.fetchButton);
        this.tbararray.push(this.addNewRateButton,this.editRateButton, this.deleteTDSRatebtn);
        //this.tbararray.push(this.TDSRatesForResibtn, this.TDSRatesForNonResibtn);
        
        this.exportButton.params =this.MasterItemStore.baseParams;
        this.bottombtnArr = new Array();
        this.bottombtnArr.push(this.exportButton);
        this.sm = new Wtf.grid.CheckboxSelectionModel();
        this.sm.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
        this.MasterItemColumn = new Wtf.grid.ColumnModel([
            this.sm,
            {
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.NatureOfPayment") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.NatureOfPayment") + "</span>", //"Master Items",//Nature Of Payments
                dataIndex: "natureofpayment",
                width: 150,
                pdfwidth:75,//To Add in Export Window.
                renderer: function (val) {
                    return "<div wtf:qtip=\"" + val + "\" wtf:qtitle='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.NatureOfPayment") + "'>" + val + "</div>";
                }
            }, {
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.ResidentialStatus") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.ResidentialStatus") + "</span>", //Residential Status
                dataIndex: 'residentialstatus',
                pdfwidth:75,//To Add in Export Window.
                width: 150
            }, {
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.BasicExemptionPerTransaction") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.BasicExemptionPerTransaction") + "</span>", //Basic Exemption Per Transaction.
                dataIndex: 'basicexemptionpertransaction',
                pdfwidth:75,//To Add in Export Window.
                hidden:true,
                width: 150
            }, {
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.BasicExemptionPerAnnum") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.BasicExemptionPerAnnum") + "</span>", //Basic Exemption Per Transaction.
                dataIndex: 'basicexemptionperannum',
                pdfwidth:75,//To Add in Export Window.
                width: 150
            }, {
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.DeducteeType") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.DeducteeType") + "</span>", //Deductee Type
                dataIndex: 'deducteetype',
                pdfwidth:75,//To Add in Export Window.
                width: 150
            }, {
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.Rate") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.Rate") + "</span>", //TDS Rate
                dataIndex: 'tdsrate',
                pdfwidth:75,//To Add in Export Window.
                width: 150
            }, {
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.FromDate") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.FromDate") + "</span>", //From Date
                dataIndex: 'tdsratefromdate',
                pdfwidth:75,//To Add in Export Window.
                width: 150
            }, {
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.ToDate") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.ToDate") + "</span>", //To Date
                dataIndex: 'tdsratetodate',
                pdfwidth:75,//To Add in Export Window.
                width: 150
            }
//            , {
//                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TDSMasterRates.TDSRateIfPanNotAvailable") + "'>" + WtfGlobal.getLocaleText("acc.TDSMasterRates.TDSRateIfPanNotAvailable") + "</span>", //TDS Rate if PAN is not given
//                dataIndex: 'tdsrateifpannotavailable',
//                pdfwidth:75,//To Add in Export Window.
//                width: 150
//            }
        ]);
        this.grid = new Wtf.grid.GridPanel({
            cls: 'vline-on',
            store: this.MasterItemStore,
            autoScroll: true,
            border: false,
            layout: 'fit',
            split: true,
            sm: this.sm,
            cm: this.MasterItemColumn,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.gridContainer = new Wtf.Panel({
            region: "center",
            layout: 'fit',
            autoScroll: true,
            border: false,
            bodyStyle: {"background-color": 'white'},
            items: [this.grid],
            tbar: this.tbararray,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.MasterItemStore,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                autoWidth: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                items: this.bottombtnArr
            })
        });
        this.storeLoad();
        this.MasterItemStore.on('beforeload', function (s, o) {
            var currentBaseParams = this.MasterItemStore.baseParams;
            currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
            currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
            this.MasterItemStore.baseParams = currentBaseParams;
        }, this);
        
    },
    storeLoad: function () {
        this.MasterItemStore.load({
            params: {
                start: 0,
                limit: (this.pP.combo != undefined) ? this.pP.combo.value : 30,
                ss: this.quickPanelSearch.getValue(),
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())
            }
        });
    },
    enableDisableButtons: function () {
        if(this.sm.getCount()==1){
            this.editRateButton.enable();
        } else {
            this.editRateButton.disable();
        }
        if (this.sm.getCount() >= 1) {
            this.deleteTDSRatebtn.enable();
        } else {
            this.deleteTDSRatebtn.disable();
        }
    },
    handleResetClick: function () {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.storeLoad();
        } else {
            this.startDate.setValue(WtfGlobal.getDates(true));
            this.endDate.setValue(WtfGlobal.getDates(false));
            this.storeLoad();
        }
    },
    fetchReportHandler: function () {
        this.storeLoad();
    },
    addNewRateHandler: function (mode) {
        this.forMode=mode;
        this.AddTDSRateFormPanel();
        
        if(this.forMode=='edit'){
            this.setValuesForEdit();
        }
        
        this.AddTDSRatebtn = new Wtf.Button({
            text: this.forMode == 'edit'?WtfGlobal.getLocaleText("acc.common.update"):WtfGlobal.getLocaleText("acc.TDSMasterRates.Add"),
            scope: this,
            handler: function(){
                this.AddTDSRateHandler();
            }
        });
        this.cancelbtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.TDSMasterRates.Cancel"),
            scope: this,
            handler: function () {
                this.createTemplateWin.close();
            }
        });
        this.createTemplateWin = new Wtf.Window({
            title: "TDS Rates", //config != undefined?WtfGlobal.getLocaleText("acc.field.cloneTerm"):WtfGlobal.getLocaleText("acc.field.DefineTerm"),
            closable: true,
            modal: true,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            height: 380,
            autoScroll: true,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body,
            items: [{
                    region: 'center',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    autoScroll: true,
                    items: this.addTDSRateinfo
                }],
            buttons: [this.AddTDSRatebtn, this.cancelbtn]
        });
        this.createTemplateWin.show();
    },
    setValuesForEdit: function () {
        if (this.grid.getSelectionModel().getSelections().length == 1) {
            var rectoEdit = this.grid.getSelectionModel().getSelections()[0].data;
            if (rectoEdit.residentialstatusid == 0) {
                this.residentialstatus0.checked = true;
                this.residentialstatus1.checked = false;
            } else {
                this.residentialstatus0.checked = false;
                this.residentialstatus1.checked = true;
            }
//            this.AddBasicExeRatePerTransaction.setValue(rectoEdit.basicexemptionpertransaction);
            this.AddBasicExeRatePerAnnum.setValue(rectoEdit.basicexemptionperannum);
//            this.AdddeducteeType.setValue(rectoEdit.deducteetypeid);
            this.AddTDSRate.setValue(rectoEdit.tdsrate);
            this.AddTDSFromDate.setValue(rectoEdit.tdsratefromdate);
//            this.AddnatureOfPayment.setValue(rectoEdit.natureofpaymentid);
            this.AddTDSToDate.setValue(rectoEdit.tdsratetodate);
        }
    },
    DeleteTDSRateHandler: function () {
        var data = [];
        this.formrec = this.grid.getSelectionModel().getSelections();
        var selectedRecordCount = this.formrec.length;
        for (var i = 0; i < selectedRecordCount; i++) {
            var recData = this.formrec[i].data;
            var rowObject = new Object();
            rowObject['tdsid'] = recData.tdsid;
            data.push(rowObject);
        }
        var confmsg = "";
        confmsg += WtfGlobal.getLocaleText("acc.rem.238");
        var url = "";
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), confmsg, function (btn) {
            if (btn != "yes")
                return;
            url = "ACCVendorPaymentCMN/deleteTDSMasterRates.do";
            Wtf.Ajax.requestEx({
                url: url,
                params: {
                    data: JSON.stringify(data)
                }
            }, this, this.genDeleteSuccessResponse, this.genDeleteFailureResponse);
        }, this)
    },
    genDeleteSuccessResponse: function (response) {
        if (response.success) {
            var msg = WtfGlobal.getLocaleText("acc.repeated.allrecordaredeleted");
            if (response.msg)
                msg = response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.delete"), msg], 0);
            this.storeLoad();
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("TDS Master Rates"), response.msg], 0);
        }
    },
    genDeleteFailureResponse: function (response) {
        this.enable();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("Failed"), msg], 1);
    },
    AddTDSRateFormPanel: function () {      
        var natureofPaymentRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'salespersoncode'},
        ]);
        this.natureofPaymentStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, natureofPaymentRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                groupid: 33,
                mode: 112,
                moduleIds: ""
            },
            sortInfo: {
                field: 'salespersoncode',
                direction: 'ASC'
            }
        });
        this.natureofPaymentStore.load();
        this.AddnatureOfPayment = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: false,
            forceSelection: true,
            extraFields: ['salespersoncode'],
            extraComparisionField: 'salespersoncode',
            extraComparisionFieldArray: ['name', 'salespersoncode'],
            listWidth: 400,
            disabled: this.isCustomer,
            anchor: '80%'
        }, {
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddNatureOfPayment") + "*", //'Nature of payment',
            hiddenName: 'addnatureofpayment',
            name: 'addnatureofpayment',
            store: this.natureofPaymentStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            allowBlank: false,
            emptyText: 'Select Nature of Payment',
            typeAhead: true,
            triggerAction: 'all',
            scope: this
        }));

        this.residentialstatus0 = new Wtf.form.Radio({
            boxLabel: WtfGlobal.getLocaleText("acc.vendor.Resident.text"),
            id: "residential" + this.id,
            name: 'addresidentialstatus',
            checked: true,
            autoWidth: true,
            height: 20,
            labelSeparator: '',
            labelWidth: 0
        });
        this.residentialstatus1 = new Wtf.form.Radio({
            boxLabel: WtfGlobal.getLocaleText("acc.vendor.NonResident.text"),
            id: "nonresidential" + this.id,
            name: 'addresidentialstatus',
            autoWidth: true,
            height: 20,
            labelSeparator: ''
        });
        this.AddresidentialFieldset = new Wtf.form.FieldSet({
            xtype: 'fieldset',
            autoHeight: true,
            style: 'border: none; margin-left: -10px; margin-bottom: -20px;',
            items: [{
                xtype: 'radiogroup',
                flex: 8,
                columns: 1,
                id: 'residentialstatus_radiogroup' + this.id,
                fieldLabel: WtfGlobal.getLocaleText('acc.TDSMasterRates.AddResidentialStatus'),
                items: [
                this.residentialstatus0,
                this.residentialstatus1
                ]
            }]
        });
        this.AddBasicExeRatePerTransaction = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddBasicExemptionPerTransaction") + "*",
            name: 'addbasicexemptionpertransaction',
            anchor: '80%',
            invalidText: WtfGlobal.getLocaleText("acc.TDSMasterRates.value.invalid"),
            allowDecimals: true,
            allowNegative: false,
            allowBlank: false,
            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.AddBasicExeRatePerAnnum = new Wtf.form.NumberField({
            anchor: '80%',
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddBasicExemptionPerAnnum") + "*",
            name: 'addbasicexemptionperannum',
            invalidText: WtfGlobal.getLocaleText("acc.TDSMasterRates.value.invalid"),
            allowDecimals: true,
            allowNegative: false,
            allowBlank: false,
            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.deducteeTypeCodeStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data: [
                ['0', 'Corporate'], ['1', 'Non-Corporate']
            ]
        });
        this.AdddeducteeTypeCode = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddDeducteeType") + "*", //'Deductee Type', 
            anchor: '80%',
            store: this.deducteeTypeCodeStore,
            valueField: 'id',
            allowBlank: false,
            displayField: 'name'
        });
        this.deducteeTypeStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 34
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, Wtf.deducteeTypeRec)
        });
        this.deducteeTypeStore.load();
        
        this.natureofPaymentStore.on('load', function () {
            if (this.forMode == 'edit' && this.grid.getSelectionModel().getSelections().length==1) {
                var rectoEdit = this.grid.getSelectionModel().getSelections()[0].data;
                this.AddnatureOfPayment.setValue(rectoEdit.natureofpaymentid);
            }
        }, this);
        
        this.deducteeTypeStore.on("load", function () {
            var rec = this.deducteeTypeStore.getAt(this.deducteeTypeStore.find("name", "Unknown"));
            this.deducteeTypeStore.remove(rec);
            if (this.forMode == 'edit' && this.grid.getSelectionModel().getSelections().length==1) {
                var rectoEdit = this.grid.getSelectionModel().getSelections()[0].data;
                this.AdddeducteeType.setValue(rectoEdit.deducteetypeid);
            }
        }, this);    
        
        this.AdddeducteeType = new Wtf.common.Select(Wtf.apply({
            multiSelect:this.forMode=='edit'?false:true,
            forceSelection: true,
            listWidth: 400,
            anchor: '80%'
        }, {
            name:'deducteetype',
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddDeducteeType"), //'Deductee Type', 
            hiddenName: 'deducteetype',
            anchor: '80%',
            emptyText:'Select a deductee type',
            valueField:'id',
            mode: 'local',
            allowBlank: false,
            displayField:'name',
            id: 'deducteetype' + this.id,
            store: this.deducteeTypeStore,
            typeAhead: true,
            triggerAction: 'all',
            scope: this
        }));

        this.AddTDSRate = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddTDSRate") + "*",
            name: 'addtdsrate',
            anchor: '80%',
            invalidText: 'Alphabets and numbers only',
            allowDecimals: true,
            allowNegative: false,
            allowBlank: false,
            maxValue: 99,
            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.AddTDSFromDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddTDSFROMDate") + "*",
            anchor: '80%',
            format: WtfGlobal.getOnlyDateFormat(),
            value: new Date(),
            allowBlank: false
        });
        this.AddTDSToDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddTDSTODate") + "*",
            anchor: '80%',
            format: WtfGlobal.getOnlyDateFormat(),
            value: new Date(),
            allowBlank: false
        });
//        this.AddTDSRateIFPanNotAvailable = new Wtf.form.NumberField({
//            fieldLabel: WtfGlobal.getLocaleText("acc.TDSMasterRates.AddTDSRateIfPANNOTAvailable") + "*",
//            name: 'addtdsrateifpannotavailable',
//            anchor: '80%',
//            invalidText: 'Alphabets and numbers only',
//            allowDecimals: true,
//            allowNegative: false,
//            allowBlank: false,
//            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
//        });
        this.addTDSRateinfo = new Wtf.form.FormPanel({
            url: 'ACCVendorPayment/AddTDSRate.do',
            region: 'center',
            bodyStyle: "background: transparent;",
            border: false,
            labelWidth: 140,
            autoHeight: true,
            autoWidth: true,
            style: "background: transparent;padding:10px;",
            items: [this.AddnatureOfPayment, this.AddresidentialFieldset, this.AddBasicExeRatePerAnnum,
                this.AdddeducteeType, this.AddTDSRate, this.AddTDSFromDate, this.AddTDSToDate] //, this.AddTDSRateIFPanNotAvailable]
        });       
    },
    AddTDSRateHandler: function () {
        var id =""
        if (this.AddTDSFromDate.getValue() > this.AddTDSToDate.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;
        }
//        if(this.AddBasicExeRatePerTransaction.getValue() > this.AddBasicExeRatePerAnnum.getValue()){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.TDSMasterRates.basic.excemption.less.annum") ], 2);
//            return false;
//        }
        if(!Wtf.isEmpty(this.forMode) && this.forMode=='edit'){
            id = this.grid.getSelectionModel().getSelections()[0].data.tdsid;
        }
        // Resident status is default.
        var addresidentialstatus=0;
        if(this.residentialstatus1.getValue()){
            // If user selects Non Resident
            addresidentialstatus=1;
        }
        this.AddTDSRatebtn.disable();
        this.addTDSRateinfo.getForm().submit({
            scope: this,
            params: {
                addtdsfromdate: WtfGlobal.convertToGenericDate(this.AddTDSFromDate.getValue()),
                addtdstodate: WtfGlobal.convertToGenericDate(this.AddTDSToDate.getValue()),
                adddeducteetype: this.AdddeducteeType.getValue(),
                addresidentialstatus:addresidentialstatus,
                id:id
            },
            success: function (result, action) {
                this.AddTDSRatebtn.enable();
                var resultObj = eval('(' + action.response.responseText + ')');
                if (resultObj.data.success) {
                    if(this.forMode == 'edit'){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.TDSupdatedsuccessfully")], 0);
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.Termaddedsuccessfully")], 0);
                    }
                    this.createTemplateWin.close();
                    this.grid.store.load({
                        params: {
                            start: 0,
                            limit: (this.pP.combo != undefined) ? this.pP.combo.value : 30,
                            ss: this.quickPanelSearch.getValue(),
                            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                            enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue())
                        }
                    });
                } else {
                    if (resultObj.data.msg)
                        msg = resultObj.data.msg;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                }
            },
            failure: function (frm, action) {
                this.AddTDSRatebtn.enable();
                var resObj = eval("(" + action.response.responseText + ")");
            }
        });
    }

});
