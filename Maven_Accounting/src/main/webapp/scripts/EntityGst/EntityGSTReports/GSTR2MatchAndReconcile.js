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
Wtf.account.GSTR2MatchAndReconcile = function(config) {

    this.importRecordStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
        }),
        url: "AccEntityGST/getGSTR2AImportData.do"
    });

    this.intializeGSTR2Fields();

    this.importRecordStore.on('load', this.importRecordStoreOnLoad, this);

    this.importRecordStore.on('beforeload', this.importRecordStoreBeforeLoad, this);

    this.importRecordGridSM = new Wtf.grid.CheckboxSelectionModel({singleSelect: true});

    this.importRecordGrid = new Wtf.grid.GridPanel({
        stripeRows: true,
        store: this.importRecordStore,
        loadMask: true,
        border: false,
        viewConfig: {
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        scrollable: true,
        height: 500,
        autoWidth: true,
        style: 'padding:8px',
        tbar: ['<b>' + WtfGlobal.getLocaleText("acc.bankReconcile.import.msg1") + '</b>'], //Imported Records from File :
        columns: [],
        bbar: this.importRecordGridPaging = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.importRecordStore,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
            plugins: this.importRecordGridPagingSize = new Wtf.common.pPageSize({
                id: "importRecordGridPagingSize_" + this.id
            }),
            items: this.bbarBttnArr
        })
    });

    this.existingRecordStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
        }),
        url: "AccEntityGST/getGSTR2SummaryDetails.do"
    });

    this.existingRecordStore.on('load', this.existingRecordStoreOnLoad, this);

    this.existingRecordStore.on('beforeload', this.existingRecordStoreBeforeLoad, this);

    this.existingRecordGrid = new Wtf.grid.GridPanel({
        stripeRows: true,
        store: this.existingRecordStore,
        height: 500,
        autoWidth: true,
        loadMask: true,
        columns: [],
        border: false,
        style: 'padding:8px',
        scrollable: true,
        tbar: ['<b>' + WtfGlobal.getLocaleText("acc.bankReconcile.import.msg5") + '</b>'], //Records from System :
        viewConfig: {
//            forceFit: true,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        bbar: this.existingRecordGridPaging = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.existingRecordStore,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
            plugins: this.existingRecordGridPagingSize = new Wtf.common.pPageSize({
                id: "existingRecordGridPagingSize_" + this.id
            }),
            items: this.bbarBttnArr
        })
    });

    this.matchButton = document.createElement('img');
    this.matchButton.src = "../../images/MatchRecord.png";
    this.matchButton.style.cursor = "pointer";
    this.matchButton.style.width = "40px";
    this.matchButton.style.height = "48px";
    this.matchButton.onclick = this.matchButtonClicked.createDelegate(this, []);

    this.matchButtonDiv = document.createElement("div");
    this.matchButtonDiv.appendChild(this.matchButton);
    this.matchButtonDiv.style.padding = "180px 0px 0px 0px";
    this.mainPanelBBar = [];
    this.mainPanelBBar.push(WtfGlobal.getLocaleText("acc.accPref.month"), this.startMonth,
            "-", WtfGlobal.getLocaleText("acc.accPref.year"), this.startYear,
            "-", WtfGlobal.getLocaleText("acc.field.TransactionType"), this.typeCombo,
            "-", WtfGlobal.getLocaleText("acc.field.Entity"), this.EntityCombo,
            "-", this.fetchBtn,
            "-", this.uploadBtn);
    this.NorthForm = new Wtf.form.FormPanel({
        layout: 'fit',
        region: 'center',
        autoHeight: true,
        scrollable: true,
        split: true,
        border: false,
        cls: "visibleDisabled",
        bodyStyle: 'overflow: auto;',
        items: [{
                border: false,
                layout: 'column',
                defaults: {border: false},
                items: [{
                        columnWidth: 0.46,
                        items: [this.importRecordGrid]
                    }, {
                        layout: 'form',
                        columnWidth: 0.045,
                        height: 270,
                        style: 'padding:8px',
                        items: [{
                                layout: 'form',
                                heightWidth: 0.5,
                                border: false,
                                items: [{
                                        region: 'center',
                                        border: false,
                                        contentEl: this.matchButtonDiv
                                    }]
                            }]
                    }, {
                        columnWidth: 0.485,
                        items: [this.existingRecordGrid]
                    }]
            }]

    });

    this.mainPanel = new Wtf.Panel({
        border: false,
        region: 'center',
        layout: 'border',
        scrollable: true,
        tbar: this.mainPanelBBar,
        items: [this.NorthForm]
    })

    Wtf.apply(this, {
        items: [{
//                region: 'center',
                layout: 'border',
                border: false,
                items: [this.mainPanel]
            }]
    }, config);
    Wtf.account.GSTR2MatchAndReconcile.superclass.constructor.call(this, config);

//    this.importRecordGrid.getSelectionModel().on('rowselect', this.onImportRecordGridCellClick, this);
//
//    this.existingRecordGrid.on('cellclick', this.onExistingRecordsGridCellClick, this);
}
Wtf.extend(Wtf.account.GSTR2MatchAndReconcile, Wtf.Panel, {
    onRender: function(config) {
        Wtf.account.GSTR2MatchAndReconcile.superclass.onRender.call(this, config);

    },
    existingRecordStoreOnLoad: function() {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        Wtf.each(this.existingRecordStore.reader.jsonData.columns, function(column) {

            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            else {
                column.renderer = WtfGlobal.deletedRenderer;
            }

            columns.push(column);
        });

        this.existingRecordGrid.getColumnModel().setConfig(columns);

        this.existingRecordGrid.getView().refresh();

        if (this.existingRecordStore.getCount() < 1) {
            this.existingRecordGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.existingRecordGrid.getView().refresh();
        }
    },
    importRecordStoreOnLoad: function() {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        Wtf.each(this.importRecordStore.reader.jsonData.columns, function(column) {

            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            else {
                column.renderer = WtfGlobal.deletedRenderer;
            }

            columns.push(column);
        });

        this.importRecordGrid.getColumnModel().setConfig(columns);

        this.importRecordGrid.getView().refresh();

        if (this.importRecordStore.getCount() < 1) {
            this.importRecordGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.importRecordGrid.getView().refresh();
        }
    },
    existingRecordStoreBeforeLoad: function() {
        var currentBaseParams = this.existingRecordStore.baseParams;
        currentBaseParams.section = this.typeCombo.getValue();
        var entityItem = WtfGlobal.searchRecord(this.EntityComboStore, this.EntityCombo.getValue(), 'id');
        if (entityItem != undefined && entityItem != null) {
            currentBaseParams.entity = entityItem.get("name");
        }
        currentBaseParams.entityid = this.EntityCombo.getValue();
        currentBaseParams.month = this.startMonth.getValue();
        currentBaseParams.year = this.startYear.getValue();
        currentBaseParams.isGSTR2AMatchAndReconcile = true;
        this.existingRecordStore.baseParams = currentBaseParams;
    },
    importRecordStoreBeforeLoad: function() {
        var currentBaseParams = this.importRecordStore.baseParams;
        currentBaseParams.section = this.typeCombo.getValue();
        var entityItem = WtfGlobal.searchRecord(this.EntityComboStore, this.EntityCombo.getValue(), 'id');
        if (entityItem != undefined && entityItem != null) {
            currentBaseParams.entity = entityItem.get("name");
        }
        currentBaseParams.entityid = this.EntityCombo.getValue();
        currentBaseParams.month = this.startMonth.getValue();
        currentBaseParams.year = this.startYear.getValue();
        currentBaseParams.isGSTR2AMatchAndReconcile = true;
        this.importRecordStore.baseParams = currentBaseParams;
    },
    onImportRecordGridCellClick: function(sm, rowIndex, record) {
    },
    onExistingRecordsGridCellClick: function(g, i, j, e) {
    },
    matchButtonClicked: function() {
        if (this.startMonth.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mprp.noMonthIsSelected")], 2);
            return;
        }
        if (this.startYear.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.pleaseSelectYear")], 2);
            return;
        }
        if (this.EntityCombo.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseselectentity")], 2);
            return;
        }
        var panel = Wtf.getCmp('matchAndCompareWindow');
        if (panel == null) {
            panel = new Wtf.account.MatchAndCompareWindow({
                id: 'matchAndCompareWindow',
                title: 'GSTR2A',
                layout: 'fit',
                border: false,
                closable:true,
                parent: this
            });
            panel.setDefaultValues(this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    },
    intializeGSTR2Fields: function() {
        this.typeComboRec = Wtf.data.Record.create([
            {name: 'typeofinvoice'}
        ]);
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'typeofinvoice'}],
            data: [['B2B Invoices - 3, 4A'], ['Credit/Debit Notes Regular - 6C']]
        });

        this.typeCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("Type") + " *",
            hiddenName: 'id',
            name: 'typeofinvoice',
            store: this.typeStore,
            mode: 'local',
            valueField: 'typeofinvoice',
            displayField: 'typeofinvoice',
            emptyText: "Please Select Type",
            width: 180,
            listWidth: 180
        });
        this.uploadBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"), //'Upload',			
            iconCls: getButtonIconCls(Wtf.etype.menuadd),
            scope: this,
            handler: this.uploadJSON.createDelegate(this)
        });

        //********* Entity **************************
        this.EntityComboRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.EntityComboStore = new Wtf.data.Store({
            url: "AccEntityGST/getFieldComboDataForModule.do",
            baseParams: {
                moduleid: Wtf.Acc_EntityGST,
                isMultiEntity: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.EntityComboRec)
        });

        this.EntityCombo = new Wtf.form.ComboBox({
            hiddenName: 'id',
            name: 'id',
            store: this.EntityComboStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectentity"),
            width: 200,
            listWidth: 200
        });
        this.EntityComboStore.load();
        this.EntityComboStore.on('load', function() {
            var count = this.EntityComboStore.getCount();
            if (count > 0) {
                var seqRec = this.EntityComboStore.getAt(0);
                this.EntityCombo.setValue(seqRec.data.id);
            }
        }, this);

        this.fetchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        // to change this with the month & year drop-down list
        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name: 'monthid', type: 'int'}, 'name'],
            data: [[0, "January"], [1, "February"], [2, "March"], [3, "April"], [4, "May"], [5, "June"], [6, 'July'], [7, 'August'], [8, "September"], [9, "October"],
                [10, "November"], [11, "December"]]
        });

        var data = WtfGlobal.getBookBeginningYear(true);

        this.yearStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id', type: 'int'}, 'yearid'],
            data: data
        });

        this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), //'Month',
            emptyText: WtfGlobal.getLocaleText("acc.reval.month"), //'Month',
            name: 'startMonth',
            displayField: 'name',
            forceSelection: true,
            anchor: '95%',
            valueField: 'monthid',
            mode: 'local',
            triggerAction: 'all',
            width: 90,
            selectOnFocus: true
        });

        this.startYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"), //'Year', this.startYear
            emptyText: WtfGlobal.getLocaleText("acc.reval.year"), //'Year', this.startYear
            name: 'startYear',
            displayField: 'yearid',
            anchor: '95%',
            valueField: 'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            width: 90,
            selectOnFocus: true
        });
        
    },
    uploadJSON: function() {
        if (this.startMonth.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mprp.noMonthIsSelected")], 2);
            return;
        }
        if (this.startYear.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.pleaseSelectYear")], 2);
            return;
        }
        if (this.EntityCombo.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseselectentity")], 2);
            return;
        }

        this.fileuploadwin = new Wtf.form.FormPanel({
            url: "AccEntityGST/updateGSTR2JSON.do",
            waitMsgTarget: true,
            fileUpload: true,
            method: 'POST',
            border: false,
            scope: this,
            bodyStyle: 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
            lableWidth: 50,
            items: [this.entity = new Wtf.form.Hidden({
                    name: 'entityid'
                }), this.tName = new Wtf.form.TextField(
                        {
                            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
                            name: 'FilePath',
                            inputType: 'file',
                            width: 200,
                            blankText: WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                            allowBlank: false,
                            msgTarget: 'qtip'
                        })]
        });

        this.upwin = new Wtf.Window(
                {
                    id: 'upfilewin',
                    title: WtfGlobal.getLocaleText("acc.invoiceList.uploadfile"),
                    closable: true,
                    width: 450,
                    height: 120,
                    plain: true,
                    iconCls: 'iconwin',
                    resizable: false,
                    layout: 'fit',
                    scope: this,
                    listeners: {
                        scope: this,
                        close: function() {
                            scope: this;
                            this.fileuploadwin.destroy();
                        }
                    },
                    items: this.fileuploadwin,
                    buttons: [
                        {
                            anchor: '90%',
                            id: 'save',
                            text: WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"),
                            scope: this,
                            handler: this.uploadFileHandler
                        },
                        {
                            anchor: '90%',
                            id: 'close',
                            text: WtfGlobal.getLocaleText("acc.invoiceList.bt.cancel"),
                            handler: this.closeUploadWin,
                            scope: this
                        }]

                });
        this.entity.setValue(this.EntityCombo.getValue());
        this.upwin.show();
    },
    fetchStatement: function() {
        if (this.startMonth.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mprp.noMonthIsSelected")], 2);
            return;
        }
        if (this.startYear.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.pleaseSelectYear")], 2);
            return;
        }
        if (this.EntityCombo.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseselectentity")], 2);
            return;
        }
        if (this.typeCombo.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Please Select Type"], 2);
            return;
        }

        this.existingRecordStore.load({
            params: {
                start: 0,
                limit: (this.existingRecordGridPagingSize.combo == undefined) ? 30 : this.existingRecordGridPagingSize.combo.value
            }
        });
        this.importRecordStore.load({
            params: {
                start: 0,
                limit: (this.importRecordGridPagingSize.combo == undefined) ? 30 : this.importRecordGridPagingSize.combo.value
            }
        });
    },
    closeUploadWin: function() {
        Wtf.getCmp('upfilewin').close();
    },
    uploadFileHandler: function() {
        if (this.fileuploadwin.form.isValid()) {
            Wtf.getCmp('save').disabled = true;
        }
        if (this.fileuploadwin.form.isValid()) {
            this.fileuploadwin.form.submit({
                scope: this,
                params: {
                    year: this.startYear.getValue(),
                    month: this.startMonth.getValue()
                },
                failure: function(frm, action) {
                    this.upwin.close();
                },
                success: function(frm, action) {
                    this.upwin.close();

                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.success"),
                        msg: "File uploaded successfully.",
                        width: 300,
                        buttons: Wtf.MessageBox.OK,
                        animEl: 'mb9'
                    });
                }
            })
        }
    }
});



Wtf.account.MatchAndCompareWindow = function(config) {
    Wtf.apply(this, config);
    this.intializeGSTR2AFields();
    Wtf.account.MatchAndCompareWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.MatchAndCompareWindow, Wtf.Panel, {
    onRender: function(config) {
        Wtf.account.MatchAndCompareWindow.superclass.onRender.call(this, config);
        this.createComparisonWindowFields();
        this.createDisplayGrid();
        this.mainPanelBBar = [];
        this.mainPanelBBar.push(WtfGlobal.getLocaleText("acc.accPref.month"), this.startMonth,
                "-", WtfGlobal.getLocaleText("acc.accPref.year"), this.startYear,
                "-", WtfGlobal.getLocaleText("acc.field.TransactionType"), this.typeCombo,
                "-", WtfGlobal.getLocaleText("acc.field.Entity"), this.EntityCombo,
                 "-", this.comparisonCombo,"-", this.fetchBtn);
        this.mainPanelBBar.push("->", this.helpButton);
                
        this.add({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            tbar:this.mainPanelBBar ,
            items:[this.grid]
        });
//        this.add(this.grid);
    },
    createComparisonWindowFields: function() {
        this.comparisonFilter = new Wtf.data.SimpleStore({
            fields: [{name: 'id', type: 'int'}, 'name'],
            data: [[Wtf.GSTR2AComparison.All, "All"], 
                [Wtf.GSTR2AComparison.Matched, "Matched"],
                [Wtf.GSTR2AComparison.NotMatched, "Not Matched"],
                [Wtf.GSTR2AComparison.MissingInGSTPortal, "Missing In GST Portal"],
                [Wtf.GSTR2AComparison.MissingInDeskera, "Missing In Deskera"]]
        });

        this.comparisonCombo = new Wtf.form.ComboBox({
            store: this.comparisonFilter,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), //'Month',
            emptyText: WtfGlobal.getLocaleText("acc.reval.month"), //'Month',
            name: 'comparison',
            displayField: 'name',
            forceSelection: true,
            anchor: '95%',
            value: 0,
            valueField: 'id',
            mode: 'local',
            triggerAction: 'all',
            width: 200,
            selectOnFocus: true
        });
        
        this.matchedItem = new Wtf.Toolbar.TextItem(""); 
        this.matchedItem.getEl().innerHTML ="<div style=\"width:10px; height:10px; background:Lime; float:left; margin-right: 5px;\"></div> <b>Matched</b>"; 
        this.notMatchItem = new Wtf.Toolbar.TextItem(""); 
        this.notMatchItem.getEl().innerHTML ="<div style=\"width:10px; height:10px; background:OrangeRed; float:left; margin-right: 5px;\"></div><b> Not Matched</b>"; 
        this.missingInGSTPortal = new Wtf.Toolbar.TextItem(""); 
        this.missingInGSTPortal.getEl().innerHTML ="<div style=\"width:10px; height:10px; background:DarkOrange; float:left; margin-right: 5px;\"></div> <b>Missing In GST Portal</b>"; 
        this.missingInDeskera = new Wtf.Toolbar.TextItem(""); 
        this.missingInDeskera.getEl().innerHTML ="<div style=\"width:10px; height:10px; background:CornflowerBlue ; float:left; margin-right: 5px;\"></div> <b>Missing In Deskera</b>"; 
        
        this.comparisonCombo.on("change", this.comparisonStatusOnChange, this);
        
        this.addbtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.add"), 
            tooltip: WtfGlobal.getLocaleText("acc.common.add"),
            scope: this,
            hidden: this.typeCombo.getValue() != "B2B Invoices",
            iconCls: getButtonIconCls(Wtf.etype.add),
            handler: this.addPurchaseInvoice.createDelegate(this, [false])
        });
        
        this.editBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.edit"), 
            tooltip: WtfGlobal.getLocaleText("acc.common.edit"),
            scope: this,
            disabled: true,
            hidden: this.typeCombo.getValue() != "B2B Invoices",
            iconCls: getButtonIconCls(Wtf.etype.edit),
            handler: this.addPurchaseInvoice.createDelegate(this, [true])
        });
        
        this.expButton = new Wtf.exportButton({
            obj: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),
            filename: "GSTR2A Comparison" + "_v1",
            menuItem: {
                csv: true,
                xls: true
            },
            label: "GSTR2A Comparison",
            get: Wtf.autoNum.GSTR2MatchAndReconcile
        });
        
        this.acceptBtn = new Wtf.Action({
            text: "Accept",
            tooltip: "Accept",
//            style: "margin-left: 6px;",
            scope: this,
            disabled: true,
            handler: this.updateTransactionFlag.createDelegate(this, ["A"])
        });

        this.rejectBtn = new Wtf.Action({
            text: "Reject",
            tooltip: "Reject",
//            style: "margin-left: 6px;",
            scope: this,
            disabled: true,
            handler: this.updateTransactionFlag.createDelegate(this, ["R"])
        });
        
        this.modifyBtn = new Wtf.Action({
            text: "Modify",
            tooltip: "Modify",
//            style: "margin-left: 6px;",
            scope: this,
            disabled: true,
            handler: this.updateTransactionFlag.createDelegate(this, ["M"])
        });

        this.exportOfflineJSON = new Wtf.Toolbar.Button({
            text: "Export Offline JSON", 
            tooltip: "Export offline JSON file which needs to be uploaded on GSTN portal. This file will include transaction(s) which has been marked as Accept, Modify, Reject,Add to Portal.", 
            scope: this,
            iconCls: 'pwnd exportItem',
            handler: this.exportJSON.createDelegate(this)
        });
        
        this.pendingBtn = new Wtf.Action({
            text: "Pending", 
            tooltip: "Pending", 
//            style: "margin-left: 6px;",
            scope: this,
            disabled: true,
            handler: this.updateTransactionFlag.createDelegate(this, ["P"])
        });
        
        this.addToGSTNPortal = new Wtf.Action({
            text: "Add to Portal", 
            tooltip: "Add to Portal", 
//            style: "margin-left: 6px;",
            scope: this,
            disabled: true,
            handler: this.updateTransactionFlag.createDelegate(this, ["G"])
        });
        var flagBtnArray = [];
        flagBtnArray.push(this.acceptBtn);
        flagBtnArray.push(this.modifyBtn);
        flagBtnArray.push(this.rejectBtn);
        flagBtnArray.push(this.addToGSTNPortal);
        flagBtnArray.push(this.pendingBtn);
        this.transactionFlagMenu = new Wtf.Toolbar.Button({
            text: "Update Status",
            scope: this,
            iconCls: 'accountingbase pricelistbutton',
            tooltip: "Update transaction(s) status flag, which needs to be uploaded on GSTN portal. ",
            menu: flagBtnArray
        });
       
    },
    exportJSON: function () {
        var url = "AccEntityGST/exportGSTR2Json.do?";
        url += "&entityid=" + this.EntityCombo.getValue();
        url += "&month=" + this.startMonth.getValue();
        url += "&year=" + this.startYear.getValue();
        Wtf.get('downloadframe').dom.src = url;
    },
    createDisplayGrid: function() {
        this.matchAndCompareStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }),
            url: "AccEntityGST/getGSTR2AComparisonData.do"
        });

        this.matchAndCompareStore.on('load', this.matchAndCompareStoreOnLoad, this);

        this.matchAndCompareStore.on('beforeload', this.matchAndCompareStoreBeforeLoad, this);

        this.tbarArr = [];
        this.tbarArr.push(this.addbtn, this.editBtn, "-", this.transactionFlagMenu, "-", this.expButton, "-", this.exportOfflineJSON, "-", this.matchedItem, this.notMatchItem, this.missingInGSTPortal, this.missingInDeskera,"-",this.helpButton1);
        
        this.grid = new Wtf.grid.GridPanel({
            stripeRows: true,
            loadMask: true,
            store: this.matchAndCompareStore,
            border: false,
            viewConfig: {
//            forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            tbar: this.tbarArr,
            columns: [],
            bbar: this.importRecordGridPaging = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.matchAndCompareStore,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                plugins: this.importRecordGridPagingSize = new Wtf.common.pPageSize({
                    id: "importRecordGridPagingSize_" + this.id
                }),
                    items: this.bbarBttnArr
            })
        });
        this.grid.getSelectionModel().on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
    },
    closeWin: function() {
        this.close();
    },
    enableDisableButtons: function () {
        if (this.grid.getSelectionModel().getCount() == 1 && this.editBtn != undefined) {
            if (this.grid.getSelectionModel().getSelected().data['status'] === "Not Matched") {
                this.editBtn.enable();
            }
        } else {
            this.editBtn.disable();
        }
    },
//     text: 
    matchAndCompareStoreOnLoad: function() {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        Wtf.each(this.matchAndCompareStore.reader.jsonData.columns, function(column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            else {
                column.renderer = WtfGlobal.deletedRenderer;
            }

            columns.push(column);
        }, this);

        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();
        
        if (this.matchAndCompareStore.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
    },
    matchAndCompareStoreBeforeLoad: function() {
        var currentBaseParams = this.matchAndCompareStore.baseParams;
        currentBaseParams.section = this.typeCombo.getValue();
        var entityItem = WtfGlobal.searchRecord(this.EntityComboStore, this.EntityCombo.getValue(), 'id');
        if (entityItem != undefined && entityItem != null) {
            currentBaseParams.entity = entityItem.get("name");
        }
        currentBaseParams.entityid = this.EntityCombo.getValue();
        currentBaseParams.month = this.startMonth.getValue();
        currentBaseParams.year = this.startYear.getValue();
        currentBaseParams.isGSTR2AMatchAndReconcile = true;
        currentBaseParams.comparisonfilter = this.comparisonCombo.getValue();
//        currentBaseParams.start = 0;
//        currentBaseParams.limit = (this.importRecordGridPagingSize == undefined || this.importRecordGridPagingSize.combo == undefined) ? 30 : this.importRecordGridPagingSize.combo.value;
        this.matchAndCompareStore.baseParams = currentBaseParams;
    },
    fetchStatement: function() {
         if (this.startMonth.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mprp.noMonthIsSelected")], 2);
            return;
        }
        if (this.startYear.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.pleaseSelectYear")], 2);
            return;
        }
        if (this.EntityCombo.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseselectentity")], 2);
            return;
        }
        
        this.matchAndCompareStore.load({
            params: {
                start: 0,
                limit: (this.importRecordGridPagingSize.combo == undefined) ? 30 : this.importRecordGridPagingSize.combo.value
            }
        });
    },
    taxableAmountRenderer: function(value, m, rec) {
        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        if (rec.data.deleted)
            v = '<del>' + WtfGlobal.conventInDecimal(v, symbol) + '</del>';
        else
            v = WtfGlobal.conventInDecimal(v, symbol);
        if (rec.data['status'] != undefined && rec.data['status'] == "Not Matched") {
            var gtaxableAmt = parseFloat(rec.data['gtaxableAmt']);
            if (rec.data.deleted)
                gtaxableAmt = '<del>' + WtfGlobal.conventInDecimal(gtaxableAmt, symbol) + '</del>';
            else
                gtaxableAmt = WtfGlobal.conventInDecimal(gtaxableAmt, symbol);

            v = '<div class="currency" wtf:qtip="GST Portal Value:' + gtaxableAmt + '">' + v + '</div>';
        } else {
            v = '<div class="currency">' + v + '</div>';
        }
        return v;
    },
    totalCessRenderer: function(value, m, rec) {
        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        if (rec.data.deleted)
            v = '<del>' + WtfGlobal.conventInDecimal(v, symbol) + '</del>';
        else
            v = WtfGlobal.conventInDecimal(v, symbol);
        if (rec.data['status'] != undefined && rec.data['status'] == "Not Matched") {
            var gcess = parseFloat(rec.data['gcess']);
            if (rec.data.deleted) {
                gcess = '<del>' + WtfGlobal.conventInDecimal(gcess, symbol) + '</del>';
            } else {
                gcess = WtfGlobal.conventInDecimal(gcess, symbol);
            }
            v = '<div class="currency" wtf:qtip="GST Portal Value:' + gcess + '">' + v + '</div>';
        } else {
            v = '<div class="currency">' + v + '</div>';
        }
        return v;
    },
    totalAmountRenderer: function(value, m, rec) {
        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        if (rec.data.deleted)
            v = '<del>' + WtfGlobal.conventInDecimal(v, symbol) + '</del>';
        else
            v = WtfGlobal.conventInDecimal(v, symbol);
        if (rec.data['status'] != undefined && rec.data['status'] == "Not Matched") {
            var gtotalAmt = parseFloat(rec.data['gtotalAmt']);
            if (rec.data.deleted)
                gtotalAmt = '<del>' + WtfGlobal.conventInDecimal(gtotalAmt, symbol) + '</del>';
            else
                gtotalAmt = WtfGlobal.conventInDecimal(gtotalAmt, symbol);

            v = '<div class="currency" wtf:qtip="GST Portal Value:' + gtotalAmt + '">' + v + '</div>';
        } else {
            v = '<div class="currency">' + v + '</div>';
        }

        return v;
    },
    totalTaxRenderer: function(value, m, rec) {
        var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        if (rec.data.deleted)
            v = '<del>' + WtfGlobal.conventInDecimal(v, symbol) + '</del>';
        else
            v = WtfGlobal.conventInDecimal(v, symbol);
        if (rec.data['status'] != undefined && rec.data['status'] == "Not Matched") {
            var gtotalTax = parseFloat(rec.data['gtotalTax']);
            if (rec.data.deleted)
                gtotalTax = '<del>' + WtfGlobal.conventInDecimal(gtotalTax, symbol) + '</del>';
            else
                gtotalTax = WtfGlobal.conventInDecimal(gtotalTax, symbol);

            v = '<div class="currency" wtf:qtip="GST Portal Value:' + gtotalTax + '">' + v + '</div>';
        } else {
            v = '<div class="currency">' + v + '</div>';
        }

        return v;
    },
    comparisonStatusOnChange: function(obj, newValue, oldValue) {
        this.acceptBtn.disable();
        this.modifyBtn.disable();
        this.rejectBtn.disable();
//        this.deleteBtn.disable();
        this.addToGSTNPortal.disable();
        this.pendingBtn.disable();
        switch (newValue) {
            case Wtf.GSTR2AComparison.Matched:
                this.acceptBtn.enable();
                break;
            case Wtf.GSTR2AComparison.NotMatched:
                this.modifyBtn.enable();
                break;
            case Wtf.GSTR2AComparison.MissingInGSTPortal:
                this.addToGSTNPortal.enable();
                this.pendingBtn.enable();
                break;
            case Wtf.GSTR2AComparison.MissingInDeskera:
                this.rejectBtn.enable();
                break;
        }
        
        if (newValue === Wtf.GSTR2AComparison.Matched) {
            this.addbtn.setVisible(true);
        } else {
            this.addbtn.setVisible(false);
        }
    },
    statusRenderer: function (value, m, rec)  {
        if (value === "Matched") {
            value = '<span style="font-weight:bold;background-color:Lime;">' + value + '</span>';
        } else if (value === "Not Matched") {
            value = '<span style="font-weight:bold;background-color:OrangeRed;">' + value + '</span>';
        } else if (value === "Missing In GST Portal") {
            value = '<span style="font-weight:bold;background-color:DarkOrange;">' + value + '</span>';
        } else if (value === "Missing In Deskera") {
            value = '<span style="font-weight:bold;background-color:CornflowerBlue ;">' + value + '</span>';
        }
        return value;
    },
    updateTransactionFlag: function (updateFlag) {
        if (!this.grid.getSelectionModel().hasSelection()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bankReconcile.msg3")], 1);
            return;
        }
        var recordIndex = WtfGlobal.searchRecordIndex(this.comparisonFilter, this.comparisonCombo.getValue(), 'id');
        var nameRecord = this.comparisonFilter.getAt(recordIndex);
        var status = "";
        if (nameRecord != undefined && nameRecord != null) {
            status = nameRecord.data['name'];
        }
        var selectedArr = this.grid.getSelectionModel().getSelections();
        var jsonString = "";
        for (var idx = 0; idx < selectedArr.length; idx++) {
            var record = selectedArr[idx];
            if (status != record.data['status']) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please select record(s) having status as: <b>" + status + "</b>."], 2);    
                return;
            }
            var gstrsubmissionid = (record.data['gstrsubmissionid'] == "" || record.data['gstrsubmissionid'] == undefined) ? "''" : record.data['gstrsubmissionid'];
            var import_gstrsubmissionid = (record.data['import_gstrsubmissionid'] == "" || record.data['import_gstrsubmissionid'] == undefined) ? "''" : record.data['import_gstrsubmissionid'];
            var system_gstrsubmissionid = (record.data['system_gstrsubmissionid'] == "" || record.data['system_gstrsubmissionid'] == undefined) ? "''" : record.data['system_gstrsubmissionid'];
            jsonString += "{gstrsubmissionid:\"" + gstrsubmissionid + "\"," +
                    "import_gstrsubmissionid:" + import_gstrsubmissionid + "," +
                    "system_gstrsubmissionid:" + system_gstrsubmissionid + "},";
        }
        if (jsonString.length > 0) {
            jsonString = jsonString.substr(0, jsonString.length - 1);
        }
        Wtf.Ajax.requestEx({
            url: "AccEntityGST/updateGSTR2TransactionFlag.do",
            params: {
                updateFlag: updateFlag,
                jsonString: jsonString
            }
        }, this, function (res, req) {
            this.matchAndCompareStore.reload();
        });
    },
    addPurchaseInvoice: function(isEdit) {
        if (isEdit) {
            var billid = this.grid.getSelectionModel().getSelected().data['invoiceid'];
            this.GridRec = Wtf.data.Record.create([
                {name: 'billid'},
                {name: 'journalentryid'},
                {name: 'entryno'},
                {name: 'billto'},
                {name: 'companyid'},
                {name: 'companyname'},
                {name: 'discount'},
                {name: 'currencysymbol'},
                {name: 'orderamount'},
                {name: 'isexpenseinv'},
                {name: 'currencyid'},
                {name: 'shipto'},
                {name: 'mode'},
                {name: 'billno'},
                {name: 'rfqno'},
                {name: 'isjobWorkWitoutGrn'},
                {name: 'prno'},
                {name: 'PR_IDS'},
                {name: 'date', type: 'date'},
                {name: 'duedate', type: 'date'},
                {name: 'shipdate', type: 'date'},
                {name: 'personname'},
                {name: 'aliasname'},
                {name: 'personemail'},
                {name: 'personid'},
                {name: 'shipping'},
                {name: 'othercharges'},
                {name: 'partialinv', type: 'boolean'},
                {name: 'includeprotax', type: 'boolean'},
                {name: 'amount'},
                {name: 'amountbeforegst'},
                {name: 'GSTINRegTypeDefaultMstrID'},
                {name: 'amountdue'},
                {name: 'amountdueinbase'},
                {name: 'exchangeratefortransaction'},
                {name: 'termdays'},
                {name: 'termid'},
                {name: 'termname'},
                {name: 'incash', type: 'boolean'},
                {name: 'taxamount'},
                {name: 'OtherTermNonTaxableAmount'}, //Other Charges(Other Term NonTaxable Amount), (For India Country)
                {name: 'taxid'},
                {name: 'orderamountwithTax'},
                {name: 'taxincluded', type: 'boolean'},
                {name: 'taxname'},
                {name: 'deleted'},
                {name: 'hasautogenpickpackdo', type: 'boolean'},
                {name: 'termamount'},
                {name: 'amountinbase'},
                {name: 'memo'},
                {name: 'createdby'},
                {name: 'createdbyid'},
                {name: 'externalcurrencyrate'},
                {name: 'ispercentdiscount'},
                {name: 'discountval'},
                {name: 'crdraccid'},
                {name: 'creditDays'},
                {name: 'isRepeated'},
                {name: 'porefno'},
                {name: 'costcenterid'},
                {name: 'costcenterName'},
                {name: 'interval'},
                {name: 'intervalType'},
                {name: 'NoOfpost'},
                {name: 'NoOfRemainpost'},
                {name: 'templateid'},
                {name: 'templatename'},
                {name: 'startDate', type: 'date'},
                {name: 'nextDate', type: 'date'},
                {name: 'expireDate', type: 'date'},
                {name: 'repeateid'},
                {name: 'status'},
                {name: 'statustype'},
                {name: 'amountwithouttax'},
                {name: 'amountwithouttaxinbase'},
                {name: 'commission'},
                {name: 'commissioninbase'},
                {name: 'amountDueStatus'},
                {name: 'paymentstatus'},
                {name: 'salesPerson'},
                {name: 'agent'},
                {name: 'agentname'},
                {name: 'shipvia'},
                {name: 'fob'},
                {name: 'approvalstatus'},
                {name: 'approvalstatusinfo'},
                {name: 'approvalstatusint', type: 'int', defaultValue: -1},
                {name: 'archieve', type: 'int'},
                {name: 'withoutinventory', type: 'boolean'},
                {name: 'isfavourite'},
                {name: 'isCapitalGoodsAcquired'},
                {name: 'isRetailPurchase'},
                {name: 'importService'},
                {name: 'othervendoremails'},
                {name: 'termdetails'},
                {name: 'approvestatuslevel'}, // for requisition
                {name: 'isrequesteditable'}, // for Consignment Request Approval
                {name: 'posttext'},
                {name: 'isOpeningBalanceTransaction'},
                {name: 'isNormalTransaction'},
                {name: 'isreval'},
                {name: 'islockQuantityflag'},
                {name: 'isprinted'},
                {name: 'isEmailSent'},
                {name: 'validdate', type: 'date'},
                {name: 'cashtransaction', type: 'boolean'},
                {name: 'shiplengthval'},
                {name: 'invoicetype'},
                {name: 'landedInvoiceID'},
                {name: 'landedInvoiceNumber'},
                {name: 'manualLandedCostCategory'},
                {name: 'termdays'},
                {name: 'billingAddress'},
                {name: 'billingCountry'},
                {name: 'billingState'},
                {name: 'billingPostal'},
                {name: 'billingEmail'},
                {name: 'billingFax'},
                {name: 'billingMobile'},
                {name: 'billingPhone'},
                {name: 'billingContactPerson'},
                {name: 'billingRecipientName'},
                {name: 'billingContactPersonNumber'},
                {name: 'billingContactPersonDesignation'},
                {name: 'billingWebsite'},
                {name: 'billingCounty'},
                {name: 'billingCity'},
                {name: 'billingAddressType'},
                {name: 'shippingAddress'},
                {name: 'shippingCountry'},
                {name: 'shippingState'},
                {name: 'shippingCounty'},
                {name: 'shippingCity'},
                {name: 'shippingEmail'},
                {name: 'shippingFax'},
                {name: 'shippingMobile'},
                {name: 'shippingPhone'},
                {name: 'shippingPostal'},
                {name: 'shippingContactPersonNumber'},
                {name: 'shippingContactPersonDesignation'},
                {name: 'shippingWebsite'},
                {name: 'shippingContactPerson'},
                {name: 'shippingRecipientName'},
                {name: 'shippingRoute'},
                {name: 'shippingAddressType'},
                {name: 'vendcustShippingAddress'},
                {name: 'vendcustShippingCountry'},
                {name: 'vendcustShippingState'},
                {name: 'vendcustShippingCounty'},
                {name: 'vendcustShippingCity'},
                {name: 'vendcustShippingEmail'},
                {name: 'vendcustShippingFax'},
                {name: 'vendcustShippingMobile'},
                {name: 'vendcustShippingPhone'},
                {name: 'vendcustShippingPostal'},
                {name: 'vendcustShippingContactPersonNumber'},
                {name: 'vendcustShippingContactPersonDesignation'},
                {name: 'vendcustShippingWebsite'},
                {name: 'vendcustShippingContactPerson'},
                {name: 'vendcustShippingRecipientName'},
                {name: 'vendcustShippingAddressType'},
                {name: 'dropshipbillingAddressType'},
                {name: 'dropshipbillingAddress'},
                {name: 'dropshipbillingCountry'},
                {name: 'dropshipbillingState'},
                {name: 'dropshipbillingPostal'},
                {name: 'dropshipbillingEmail'},
                {name: 'dropshipbillingFax'},
                {name: 'dropshipbillingMobile'},
                {name: 'dropshipbillingPhone'},
                {name: 'dropshipbillingContactPerson'},
                {name: 'dropshipbillingRecipientName'},
                {name: 'dropshipbillingContactPersonNumber'},
                {name: 'dropshipbillingContactPersonDesignation'},
                {name: 'dropshipbillingWebsite'},
                {name: 'dropshipbillingCounty'},
                {name: 'dropshipbillingCity'},
                {name: 'sequenceformatid'},
                {name: 'gstIncluded'},
                {name: 'lasteditedby'},
                {name: 'movementtype'},
                {name: 'movementtypename'},
                {name: 'salespersonname'},
                {name: 'isConsignment'},
                {name: 'custWarehouse'},
                {name: 'custWarehousename'},
                {name: 'requestWarehouse'},
                {name: 'requestWarehousename'},
                {name: 'requestLocationname'},
                {name: 'requestLocation'},
                {name: 'autoapproveflag'},
                {name: 'movementtype'},
                {name: 'deliveryTime'},
                {name: 'getFullShippingAddress'},
                {name: 'selfBilledInvoice'},
                {name: 'RMCDApprovalNo'},
                {name: 'fixedAssetInvoice'},
                {name: 'fixedAssetLeaseInvoice'},
                //Below Fields are used only for Cash Sales and Purchase.
                {name: 'methodid'},
                {name: 'paymentname'},
                {name: 'moduleid'},
                {name: 'detailtype'},
                {name: 'cardno'},
                {name: 'nameoncard'},
                {name: 'cardexpirydate', type: 'date'},
                {name: 'cardtype'},
                {name: 'cardrefno'},
                {name: 'chequeno'},
                {name: 'clearanceDate', type: 'date'},
                {name: 'paymentStatus'},
                {name: 'bankname'},
                {name: 'refname'},
                {name: 'shippingterm'},
                {name: 'chequedate', type: 'date'},
                {name: 'chequedescription'},
                {name: 'termsincludegst'},
                {name: 'attachment'},
                {name: 'fromdate', type: 'date'},
                {name: 'todate', type: 'date'},
                {name: 'customerporefno'},
                {name: 'totalprofitmargin'},
                {name: 'totalprofitmarginpercent'},
                {name: 'isDraft'},
                {name: 'isLinkedTransaction'},
                {name: 'isMRPJOBWORKOUT'},
                {name: 'isJobWorkOrderReciever'},
                {name: 'closeStatus'},
                {name: 'parentinvoiceid'},
                {name: 'isFromPOS'},
                {name: 'isactivate', type: 'boolean'},
                {name: 'approver'},
                {name: 'ispendingapproval', type: 'boolean'},
                {name: 'parentso'},
                {name: 'isWrittenOff'},
                {name: 'isRecovered'},
                {name: 'allowEditingRecurredDocuments', type: 'boolean'},
                {name: 'editedRecurredDocumentsApprover'},
                {name: 'currencycode'},
                {name: 'hasAccess', type: 'boolean'},
                {name: 'isAllowToEdit', type: 'boolean'},
                {name: 'isPaymentStatusCleared', type: 'boolean'},
                {name: 'statusforcrosslinkage'},
                {name: 'statusofpoforrequisition'},
                {name: 'closedmanually'},
                {name: 'assetExciseid'},
                {name: 'exciseDetailid'},
                {name: 'suppliers'},
                {name: 'supplierTINSalesTAXNo'},
                {name: 'supplierExciseRegnNo'},
                {name: 'cstnumber'},
                {name: 'supplierRange'},
                {name: 'supplierCommissionerate'},
                {name: 'supplierAddress'},
                {name: 'supplierImporterExporterCode'},
                {name: 'supplierDivision'},
                {name: 'manufacturername'},
                {name: 'manufacturerExciseRegnNo'},
                {name: 'manufacturerRange'},
                {name: 'manufacturerCommissionerate'},
                {name: 'manufacturerDivision'},
                {name: 'manufacturerAddress'},
                {name: 'manufacturerImporterExporterCode'},
                {name: 'InvoicenoManuFacture'},
                {name: 'InvoiceDateManuFacture'},
                {name: 'supplierState'},
                {name: 'isExciseInvoice'},
                {name: 'isExciseInvoiceWithTemplate'},
                {name: 'defaultnatureofpurchase'},
                {name: 'registrationType'},
                {name: 'populateproducttemplate'},
                {name: 'UnitName'},
                {name: 'ECCNo'},
                {name: 'manufacturertype'},
                {name: 'formtypeid'},
                {name: 'gtaapplicable'},
                {name: 'gstapplicable'},
                {name: 'isInterstateParty'},
                {name: 'excisetypeid'},
                {name: 'formseriesno'},
                {name: 'formno'},
                {name: 'formdate', type: 'date'},
                {name: 'formamount'},
                {name: 'checkformstatus'},
                {name: 'formstatus'},
                {name: 'issupplementary'},
                {name: 'originalInvoice'},
                {name: 'originalInvoiceId'},
                {name: 'driverID'},
                {name: 'vehicleNoID'},
                {name: 'discountinbase'},
                {name: 'tdsrate'},
                {name: 'tdsmasterrateruleid'},
                {name: 'natureOfPayment'},
                {name: 'deducteetype'},
                {name: 'residentialstatus'},
                {name: 'tdsPayableAccount'},
                {name: 'natureOfPaymentname'},
                {name: 'deducteetypename'},
                {name: 'tdsamount'},
                {name: 'totalAmountWithTDS'},
                {name: 'TotalAdvanceTDSAdjustmentAmt'},
                {name: 'AdvancePaymentID'},
                {name: 'AdvancePaymentNumber'},
                {name: 'isTaxPaidTransaction'},
                {name: 'isSupplierLinekd'},
                {name: 'isClaimedTransaction'},
                {name: 'supplierinvoiceno'}, //SDP-4510
                {name: 'importexportdeclarationno'}, //ERM-470
                {name: 'landingCostCategoryCombo'}, //SDP-4510
                {name: 'customerbankaccounttype'},
                {name: 'customerbankaccounttypevalue'},
                {name: 'isGIROFileGeneratedForUOBBank'},
                {name: 'isGIROFileGeneratedForUOBBankForReport'},
                {name: 'paymentMethodUsedForUOB'},
                {name: 'vattinno'},
                {name: 'csttinno'},
                {name: 'panno'},
                {name: 'servicetaxno'},
                {name: 'isjobworkoutrec'},
                {name: 'tanno'},
                {name: 'eccno'},
                {name: 'subtotal'},
                {name: 'productTotalAmount'},
                {name: 'upsTrackingNumbers'}, //Tracking Numbers of shipments created with Pick-Pack-Ship process and UPS REST service
                {name: 'totalShippingCost'}, //total shipping cost estimated by UPS service
                {name: 'customerShippingAddressType'},
                {name: 'customerShippingAddress'},
                {name: 'customerShippingCountry'},
                {name: 'customerShippingState'},
                {name: 'customerShippingCounty'},
                {name: 'customerShippingCity'},
                {name: 'customerShippingEmail'},
                {name: 'customerShippingFax'},
                {name: 'customerShippingMobile'},
                {name: 'customerShippingPhone'},
                {name: 'customerShippingPostal'},
                {name: 'customerShippingContactPersonNumber'},
                {name: 'customerShippingContactPersonDesignation'},
                {name: 'customerShippingWebsite'},
                {name: 'customerShippingRecipientName'},
                {name: 'customerShippingContactPerson'},
                {name: 'customerShippingRoute'},
                {name: 'customeridforshippingaddress'},
                {name: 'purchaseinvoicetype'},
                {name: 'isapplytaxtoterms'},
                {name: 'childCount'},
                {name: 'cashReceived'}, //for Cash Received field in Sales Invoice
                {name: 'isapplytaxtoterms'},
                {name: 'isTDSApplicable'},
                {name: 'personcode'},
                {name: 'purchaseordertype'},
                {name: 'isSOPOBlock'},
                {name: 'isRoundingAdjustmentApplied'},
                {name: 'isFinalLevelApproval'}

            ]);
            this.StoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
            
            this.Store = new Wtf.data.Store({
                url: this.StoreUrl,
                remoteSort: this.RemoteSort,
                baseParams: {
                    deleted: false,
                    billid: billid,
                    gcurrencyid: gcurrencyid,
                    userid: loginid
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    upsErrorJSON: 'upsErrorJSON',
                    totalProperty: 'count'
                }, this.GridRec)
            });
            
            this.Store.load();
            this.Store.on("load", function () {
                var formrec = this.Store.data.items[0];
                var label = WtfGlobal.getLocaleText("acc.product.edit") + billid;
                var isLinkedTransaction = false;
                if ((formrec.data.isLinkedTransaction != undefined && formrec.data.isLinkedTransaction)) {
                    isLinkedTransaction = true;
                }
                var isAllowToEdit = false;
                if (formrec.data.isAllowToEdit != undefined && formrec.data.isAllowToEdit != null && formrec.data.isAllowToEdit != "") { 
                    isAllowToEdit = formrec.data.isAllowToEdit; // true when invoice is created using auto generate DO/GR option and SI/DO/PI/GR is not forward linked to any document
                }
                if (isAllowToEdit) {
                    isLinkedTransaction = false;
                }
                var isSelfBilledInvoice = formrec.get("selfBilledInvoice");
                var incash=formrec.get("incash");
                if (incash) {
                    callEdiCashPurchase(formrec, label + 'PaymentReceipt', false, false, undefined, undefined, isLinkedTransaction, isAllowToEdit);
                } else {
                    callEditGoodsReceipt(formrec, label + 'GoodsReceipt', false, false, undefined, false, isSelfBilledInvoice, isLinkedTransaction, isAllowToEdit);
                }
                this.close();
            }, this);
        } else {
            callSalesOrPurchaseType(false);
            this.close();
        }
    },
    intializeGSTR2AFields: function() {
        this.typeComboRec = Wtf.data.Record.create([
            {name: 'typeofinvoice'}
        ]);
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'typeofinvoice'}],
            data: [['B2B Invoices'], ['CDN Invoices']]
        });

        this.typeCombo = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("Type") + " *",
            hiddenName: 'id',
            name: 'typeofinvoice',
            store: this.typeStore,
            mode: 'local',
            valueField: 'typeofinvoice',
            displayField: 'typeofinvoice',
            emptyText: "Please Select Type",
            width: 150,
            listWidth: 150
        });

        this.typeCombo.on("select", function (cmb, rec, ind) {
            if (this.typeCombo.getValue() == "B2B Invoices") {
                if (this.addbtn) {
                    this.addbtn.setVisible(true);
                }
                if (this.editBtn) {
                    this.editBtn.setVisible(true);
                }
            } else {
                if (this.addbtn) {
                    this.addbtn.setVisible(false);
                }
                if (this.editBtn) {
                    this.editBtn.setVisible(false);
                }
            }
        }, this);
        //********* Entity **************************
        this.EntityComboRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.EntityComboStore = new Wtf.data.Store({
            url: "AccEntityGST/getFieldComboDataForModule.do",
            baseParams: {
                moduleid: Wtf.Acc_EntityGST,
                isMultiEntity: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.EntityComboRec)
        });

        this.EntityCombo = new Wtf.form.ComboBox({
            hiddenName: 'id',
            name: 'id',
            store: this.EntityComboStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectentity"),
            width: 200,
            listWidth: 200
        });
        this.EntityComboStore.load();
        this.EntityComboStore.on('load', function () {
            var count = this.EntityComboStore.getCount();
            if (count > 0) {
                var seqRec = this.EntityComboStore.getAt(0);
                this.EntityCombo.setValue(seqRec.data.id);
            }
        }, this);

        this.fetchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        // to change this with the month & year drop-down list
        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name: 'monthid', type: 'int'}, 'name'],
            data: [[0, "January"], [1, "February"], [2, "March"], [3, "April"], [4, "May"], [5, "June"], [6, 'July'], [7, 'August'], [8, "September"], [9, "October"],
                [10, "November"], [11, "December"]]
        });

        var data = WtfGlobal.getBookBeginningYear(true);

        this.yearStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id', type: 'int'}, 'yearid'],
            data: data
        });

        this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.month"), //'Month',
            emptyText: WtfGlobal.getLocaleText("acc.reval.month"), //'Month',
            name: 'startMonth',
            displayField: 'name',
            forceSelection: true,
            anchor: '95%',
            valueField: 'monthid',
            mode: 'local',
            triggerAction: 'all',
            width: 90,
            selectOnFocus: true
        });

        this.startYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"), //'Year', this.startYear
            emptyText: WtfGlobal.getLocaleText("acc.reval.year"), //'Year', this.startYear
            name: 'startYear',
            displayField: 'yearid',
            anchor: '95%',
            valueField: 'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            width: 90,
            selectOnFocus: true
        });

        /*
         *
         * */
        this.helpText = "<p>Details related to trasaction flag present on offline JSON:\n\
                <ul>\n\
                    <li><b>- Accept : </b> Transaction(s) with <b>Matched</b> status can be accepted. </li>\n\
                    <li><b>- Modify : </b> Transaction(s) with <b>Not Matched</b> status can be modified. It can be used, if there is any correction required on the transaction(s) uploaded by the supplier. </li>\n\
                    <li><b>- Reject : </b> Transaction(s) with <b>Missing In Deskera</b> status can be rejected. </li>\n\
                    <li><b>- Pending : </b> Transaction(s) with <b>Missing In GST Portal</b> status can be selected as Pending.</li>\n\
                    <li><b>- Add to Portal : </b> Transaction(s) with <b>Missing In GST Portal</b> status can be select as Add to Portal. It can be used to upload transaction(s) on GSTN portal. </li>\n\
                </ul>\n\
                </p>";
//                "<p>This report gives us the ability to compare transaction(s) submitted by supplier on GSTN portal and transaction(s) present in Deskera system. </p>\n\
                
        this.helpButton = new Wtf.Toolbar.Button({
            scope: this,
            iconCls: 'helpButton',
            tooltip: {text: WtfGlobal.getLocaleText("acc.rem.2")}, //{text:'Get started by clicking here!'},
            mode: id,
            handler: function (e, target, panel) {
                var tmp = e.getEl().getXY();
                var we = new Wtf.CostAndSellingPricehelpDetails();
                we.showHelpWindow(tmp[0], tmp[1], "GSTR2 Comparison", this.helpText);
            }
        });
        this.helpText1 = "<p>This report gives us the ability to compare transaction(s) submitted by supplier on GSTN portal and transaction(s) present in Deskera system. </p>\n\
               <p>Details related to status:\n\
         <ul>\n\
         <li><b>- Matched : </b> Transaction(s) submitted by supplier on GSTN portal and transaction(s) present in Deskera system is matching. </li>\n\
         <li><b>- Not Matched : </b> Transaction(s) submitted by supplier on GSTN portal and transaction(s) present in Deskera system is not matching.</li>\n\
         <li><b>- Missing In GST Portal : </b> Supplier has not uploaded transaction(s) on GSTN portal.</li>\n\
         <li><b>- Missing In Deskera : </b> Transaction(s) is available on GSTN portal only. User need to make entry into Deskera system. </li>\n\
         </ul>\n\
         </p>";
        this.helpButton1 = new Wtf.Toolbar.Button({
            scope: this,
            iconCls: 'helpButton',
            tooltip: {text: WtfGlobal.getLocaleText("acc.rem.2")}, //{text:'Get started by clicking here!'},
            mode: id,
            handler: function (e, target, panel) {
                var tmp = e.getEl().getXY();
                var we = new Wtf.CostAndSellingPricehelpDetails();
                we.showHelpWindow(tmp[0], tmp[1], "GSTR2 Comparison", this.helpText1);
            }
        });
    },
    setDefaultValues: function (parent) {
        if (parent != undefined) {
            this.typeCombo.setValue(parent.typeCombo.getValue());
            this.EntityCombo.setValue(parent.EntityCombo.getValue());
            this.startYear.setValue(parent.startYear.getValue());
            this.startMonth.setValue(parent.startMonth.getValue());
        }
    }
});

function callGSTR2AComparison() { //GSTR2A Comparison Window
    var panel = Wtf.getCmp('matchAndCompareWindow');
    if (panel == null || panel == undefined) {
        panel = new Wtf.account.MatchAndCompareWindow({
            id: 'matchAndCompareWindow',
            title: 'GSTR2A',
            layout: 'fit',
            closable:true,
            border: false
        });
        
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
