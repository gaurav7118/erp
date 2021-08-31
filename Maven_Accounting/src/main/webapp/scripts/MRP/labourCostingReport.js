/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.labourCostingReport = function (config) {
    Wtf.apply(this, config);
    this.createFitlerCombos();
    this.creategrid();
    this.createTBar();
    Wtf.labourCostingReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.labourCostingReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.labourCostingReport.superclass.onRender.call(this, config);
        this.createPanel();
        this.add(this.labourCostingpanel);
        this.Store.load();
    },
    createPanel: function () {
        var htmlDesc="";
        if (this.isMRPProfitablityReport) {
            var htmlDesc = getTopHtml(WtfGlobal.getLocaleText("acc.mrp.workorderprofilibility"), WtfGlobal.getLocaleText("mrp.workorderprofilibility.northpanel.description"), '../../images/accounitngperiodgrid.jpg', true, '0px 0px 0px 0px');
        } else {
            htmlDesc = getTopHtml(WtfGlobal.getLocaleText("mrp.costingreport.northpanel.title"), WtfGlobal.getLocaleText("mrp.costingreport.northpanel.description"), '../../images/accounitngperiodgrid.jpg', true, '0px 0px 0px 0px');
        }
        this.northPanel = new Wtf.Panel({
            region: "north",
            height: 75,
            border: false,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: htmlDesc
        });

        this.labourCostingpanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'north',
                    layout: 'fit',
                    height: 75,
                    border: false,
                    items: [this.northPanel]
                }, {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.buttonsArr,
                    bbar: [this.bottomToolbar]
                }]
        });
    },
    createTBar: function () {
        this.buttonsArr = [];
        this.bottomToolbar = [];
        this.buttonsArr.push(WtfGlobal.getLocaleText("acc.Workorder.WorkOrder"));
        this.buttonsArr.push(this.workorderCombo);

        this.buttonsArr.push(WtfGlobal.getLocaleText("mrp.costingreport.costingtype.title"));
        this.buttonsArr.push(this.costTypeCombo);

        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this
        })
        this.fetchBttn.on('click', function () {
            this.isWorkOrderSelected();
        }, this);
        this.buttonsArr.push(this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);
        this.buttonsArr.push('-', this.resetBttn);
        this.expButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.agedPay.exportTT"), //'Export report details',
            filename: WtfGlobal.getLocaleText("mrp.labour.costing.reporttitle"),
            menuItem: {pdf:true,csv: true, xls: true},
            get: Wtf.autoNum.MrpCostingReport
        });
        this.bottomToolbar.push(this.expButton);
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


        this.workorderCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.workCenter"),
            store: this.workOrderStore,
            valueField: 'workorderid',
            displayField: 'workordername',
            mode: 'remote',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: 'Please select work order',
            width: 200,
            value: '',
            extraFields: ['wocode'],
            mode:'remote'
        });


        this.costType = new Wtf.data.SimpleStore({
            fields: [{name: 'typeid', type: 'int'}, 'name'],
            data: [[0, 'All'], [1, 'Labour'], [2, 'Machine'], [3, 'Material']]
        });

        this.costTypeCombo = new Wtf.form.ComboBox({
            store: this.costType,
            name: 'costtype',
            displayField: 'name',
            valueField: 'typeid',
            emptyText: 'Please select costing Type',
            mode: 'local',
            value: 0,
            width: 150,
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true
        });

    },
    creategrid: function () {
        this.gridRec = Wtf.data.Record.create([
            {name: 'costtype'}, //projectname- is treated as work order  name
            {name:'sonumber'},
            {name: 'name'},
            {name: 'workorder'},
            {name: 'taskname'},
            {name: 'hours'},
            {name: 'cost'},
            {name: 'materialname'},
            {name: 'Description'},
            {name: 'uom'},
            {name: 'quantity'},
        ]);
        this.Store = new Wtf.ux.grid.MultiGroupingStore({
            url: 'ACCWorkOrder/getWorkOrderCostingReport.do',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.gridRec),
            groupField: ['costtype']
        });

        this.sm = new Wtf.grid.CheckboxSelectionModel({});


        this.ColumnArr = [];

        this.ColumnArr.push(this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        }));
        this.ColumnArr.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        this.ColumnArr.push({
            header: '',
            dataIndex: 'costtype',
            width: 150,
            pdfwidth: 150,
            hidden: true
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.mrp.salesorder"),
            dataIndex: 'sonumber',
            width: 150,
            pdfwidth: 150,
            hidden: this.isMRPProfitablityReport ? false : true
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.mrp.field.labourname"),
            dataIndex: 'name',
            width: 150,
            pdfwidth: 150
        });

        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.costingreport.header.materialid"),
            dataIndex: 'materialname',
            width: 150,
            pdfwidth: 150
        });

        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.Workorder.WorkOrder"),
            dataIndex: 'workorder',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.resourceanalysis.columns.taskname"),
            dataIndex: 'taskname',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("mrp.costingreport.header.hours"),
            dataIndex: 'hours',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header2"),
            dataIndex: 'Description',
            width: 150,
            pdfwidth: 150
        });

        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
            dataIndex: 'uom',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.gridQty"),
            dataIndex: 'quantity',
            width: 150,
            pdfwidth: 150
        });
        this.ColumnArr.push({
            header: WtfGlobal.getLocaleText({key:"mrp.costingreport.header.cost", params:[WtfGlobal.getCurrencySymbol()]}),
            dataIndex: 'cost',
            width: 150,
            pdfwidth: 150,
            align: 'right',
            renderer: WtfGlobal.currencyRenderer
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
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            cm: this.cm,
            layout: 'fit',
            border: false,
            stripeRows: true,
            sm: this.sm,
            forceFit: true,
            view: new Wtf.grid.GroupingView({
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")) 
            })
        });
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
    },
    handleStoreOnLoad: function () {
        this.grid.getView().refresh(true);
        this.expButton.setParams({
            woid: this.workorderCombo.getValue()
        });
    },
    fetchStatement: function () {
        this.Store.load({
            params: {
                woid: this.workorderCombo.getValue(),
                costingType: this.costTypeCombo.getValue()
            }
        });
    },
    handleStoreBeforeLoad: function () {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.woid = this.workorderCombo.getValue();
        currentBaseParams.costingType = this.costTypeCombo.getValue();
        currentBaseParams.isMRPProfitablityReport = this.isMRPProfitablityReport;
        this.Store.baseParams = currentBaseParams;

    },
    handleResetClick: function () {
        this.workorderCombo.reset();
        this.costTypeCombo.setValue(0);
        this.fetchStatement();

    },
    isWorkOrderSelected: function () {
        var workOrder = this.workorderCombo.getValue();
        if (workOrder === undefined || workOrder === '') {
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("mrp.costingreport.pleaseselectworkorderalert.msg"));
            return;
        }
        this.fetchStatement();
    }
});

