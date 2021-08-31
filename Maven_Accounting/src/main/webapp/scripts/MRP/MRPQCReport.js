/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.QualityControlParametrsListReport = function (config) {
    Wtf.apply(this, config);
    this.arr = [];
    this.creategrid();

    this.createFitlerCombos();

    this.createTBar();


    Wtf.QualityControlParametrsListReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.QualityControlParametrsListReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.QualityControlParametrsListReport.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.jobOrderpanel);
        this.fetchStatement();
    },
    createPanel: function () {
        this.jobOrderpanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.buttonsArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearchh,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                        items: [this.bottomToolbar]
                    })
                }]
        });
    },
    createFitlerCombos: function () {

        this.workOrderRec = Wtf.data.Record.create([
            {name: 'workorderid'},
            {name: 'workordername'},
            {name: 'wocode'},
            {name: 'projectid'}
        ]);

        this.workOrderStore = new Wtf.data.Store({
            url: "ACCJobWorkController/getWorkOrdersForCombo.do",
            baseParams: {
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.workOrderRec)
        });

        this.workOrderStore.on("load", function () {
            var record = new Wtf.data.Record({
                workorderid: "",
                workordername: "All Records",
                wocode: "All Records"
            });
            this.workOrderStore.insert(0, record);
            this.workorderCombo.setValue("");
        }, this);

        this.workOrderStore.load();

        this.statusStore = new Wtf.data.SimpleStore({
            fields: [{name: 'id'}, {name: 'name'}],
            data: [["All", 2], ["Pass", 1], ["Fail", 0]]
        });


        this.workorderCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.workCenter"),
            store: this.workOrderStore,
            valueField: 'workorderid',
            displayField: 'workordername',
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            width: 200,
            value: '',
            extraFields: ['wocode']
        });


        this.statusCombo = new Wtf.form.ComboBox({
            store: this.statusStore,
            fieldLabel: WtfGlobal.getLocaleText('mrp.fieldlabel.workorder.routingmaster.type'), //Routing master Type  
            displayField: 'id',
            forceSelection: true,
            valueField: 'name',
            mode: 'local',
            triggerAction: 'all',
            value: 2,
            width: 180
        });
    },
    createTBar: function () {
        this.buttonsArr = [];
        this.bottomToolbar = [];
        this.quickPanelSearchh = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.qualitycontrolistreport.searchText"),
            field: 'productid',
            width: 200,
            Store: this.Store
        })
        this.buttonsArr.push(this.quickPanelSearchh);

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        })

        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"),
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        })
        this.resetBttn.on('click', this.handleResetClick, this);

        this.buttonsArr.push(this.resetBttn);


        this.buttonsArr.push('-', WtfGlobal.getLocaleText("acc.mrp.workorder"), this.workorderCombo);

        this.buttonsArr.push('-', WtfGlobal.getLocaleText("acc.common.status"), this.statusCombo);

        this.buttonsArr.push(this.fetchBttn);

        this.expButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.agedPay.exportTT"),
            filename: WtfGlobal.getLocaleText("mrp.qualitycontrolreport.registerreport.tab.title") + "_v1",
            params: {
                ss: this.quickPanelSearchh.getValue() != undefined ? this.quickPanelSearchh.getValue() : ''
            },
            menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
            get: Wtf.autoNum.mrpqcreport
        });
        this.bottomToolbar.push(this.expButton)

    },
    creategrid: function () {
        this.gridRec = Wtf.data.Record.create([
            {name: 'projectname'}, //projectname- is treated as work order  name
            {name: 'taskname'},
            {name: 'productname'},
            {name: 'qcgroup'},
            {name: 'qcpname'},
            {name: 'qcstatus'},
            {name: 'qcminvalue'},
            {name: 'qcactval'},
            {name: 'qcdesc'}
        ]);


        this.Store = new Wtf.ux.grid.MultiGroupingStore({
            url: "ACCWorkOrder/getQualityControlParametersList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.gridRec),
            groupField: ['projectname', 'taskname', 'qcgroup']
        });

        this.sm = new Wtf.grid.CheckboxSelectionModel({});

        this.sm.on('selectionchange', this.enableDisableButtons, this);

        this.ColumnArr = [];

        this.ColumnArr.push(this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        }));
        this.ColumnArr.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.workordername"),
            dataIndex: 'projectname',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.projecttaskname"),
            dataIndex: 'taskname',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.productname"),
            dataIndex: 'productname',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.qcgroup"),
            dataIndex: 'qcgroup',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.qcpname"),
            dataIndex: 'qcpname',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.qcstatis"),
            dataIndex: 'qcstatus',
            width: 150,
            pdfwidth: 150,
            renderer: WtfGlobal.qcStatusRenderer
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.minpassvalue"),
            dataIndex: 'qcminvalue',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.actualvalue"),
            dataIndex: 'qcactval',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.desc"),
            dataIndex: 'qcdesc',
            width: 150,
            pdfwidth: 150
        });

        this.groupView = new Wtf.ux.grid.MultiGroupingView({
            forceFit: false,
            showGroupName: false,
            enableNoGroups: false,
            isGrandTotal: false,
            isGroupTotal: false,
            hideGroupedColumn: false,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')),
            groupTextTpl: '{group} '
        });
        this.cm = new Wtf.grid.ColumnModel(this.ColumnArr);
        this.grid = new Wtf.ux.grid.MultiGroupingGrid({
            store: this.Store,
            cm: this.cm,
            border: false,
            stripeRows: true,
            loadMask: true,
            sm: this.sm,
            view: this.groupView
        });

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);

    },
    fetchStatement: function () {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
    handleStoreBeforeLoad: function () {
        var currentBaseParams = this.Store.baseParams;

        var projectid = '';
        var statustype = '';
        if (this.workorderCombo.getValue() && this.workorderCombo.getValue() != "") {
            var rec = WtfGlobal.searchRecord(this.workOrderStore, this.workorderCombo.getValue(), 'workorderid');
            projectid = rec.data.projectid != undefined ? rec.data.projectid : "";
            currentBaseParams.projectid = projectid;
        }else{
            currentBaseParams.projectid = projectid;
        }

        if (this.statusCombo.getValue() != 2) {
            statustype = this.statusCombo.getValue();
            currentBaseParams.statustype = statustype
        }else{
            currentBaseParams.statustype = statustype
        }

        if (this.exportButton) {
            this.exportButton.setParams({
                projectid: projectid,
                statustype: statustype
            });
        }

        this.Store.baseParams = currentBaseParams;

    },
    handleStoreOnLoad: function () {
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
    },
    handleResetClick: function () {
        this.workorderCombo.setValue("");
        this.statusCombo.setValue(2);
        if (this.quickPanelSearchh.getValue()) {
            this.quickPanelSearchh.reset();
        }
        this.fetchStatement();

    }

});
