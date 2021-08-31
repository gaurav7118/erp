/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.RoutingTemplateMasterGrid = function(config) {
    this.id = config.id;
    Wtf.apply(this, config);
    this.createEditors();
    this.columnModel();
    this.createStore();
    this.addEvents({
        'datachanged': true
    });
    Wtf.account.RoutingTemplateMasterGrid.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.RoutingTemplateMasterGrid, Wtf.grid.EditorGridPanel, {
    clicksToEdit: 1,
    stripeRows: true,
    disabledClass: "newtripcmbss",
    layout: 'fit',
    autoScroll: true,
//    viewConfig: {forceFit: true},
    loadMask: true,
    onRender: function(config) {
        this.on('validateedit', this.checkRow, this);
        this.on('afteredit', this.updateRow, this);
        this.on('rowclick', this.handleRowClick, this);
        Wtf.account.RoutingTemplateMasterGrid.superclass.onRender.call(this, config);
    },
    createEditors: function() {
        this.processRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.processStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 36
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.processRec)
        });
//        this.processStore.load();

        this.process = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.process"),
            triggerAction: 'all',
            mode: 'local',
            valueField: 'id',
            listWidth: 150,
            anchor: '75%',
            displayField: 'name',
            store: this.processStore,
            typeAhead: true,
            forceSelection: true,
            allowBlank: false,
            name: 'process',
            hiddenName: 'processtype',
            extraFields: [],
            addNoneRecord: false
        });
        this.routeCode = new Wtf.form.TextField({
            name: 'routecode',
            hidden: false,
            width: 150,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'starttime',
            format: WtfGlobal.getOnlyDateFormat()
        });

        this.endDate = new Wtf.ExDateFieldQtip({
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'endtime'
        });

        this.duration = new Wtf.form.NumberField({
            maxLength: 2,
            width: 30,
            allowDecimal: false,
            allowBlank: false,
            minValue: 1,
            name: 'duration',
            readOnly: true
        });
        this.waitingperiod = new Wtf.form.NumberField({
            maxLength: 2,
            width: 30,
            allowDecimal: false,
            allowBlank: false,
            minValue: 1,
            name: 'waitingperiod'
        });

        this.wcRecord = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.wcStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode: 112,
                groupid: 37
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.wcRecord)
        });
//        this.wcStore.load();
        this.wcCombo = new Wtf.form.ExtFnComboBox({
            name: 'workcenter',
            store: this.wcStore,
            valueField: 'id',
            displayField: 'name',
            allowBlank: false,
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            anchor: '75%',
            listWidth: 300,
            extraFields: [],
            addNoneRecord: false
        });
        this.status = new Wtf.form.TextField({
            name: 'status',
            hidden: false,
            width: 300,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });
        this.predecessors = new Wtf.form.TextField({
            name: 'predecessors',
            hidden: false,
            width: 300,
            maxLength: 50,
            scope: this,
            allowBlank: false,
            maskRe: /[1-9]/,
            regEx: /[1-9]/
        });
        this.predecessorstype = new Wtf.form.TextField({
            name: 'predecessorstype',
            hidden: false,
            width: 300,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });

        this.machineMasterRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'machinename'},
            {name: 'machineid'}
        ]);

        this.machineStore = new Wtf.data.Store({
            url: "ACCMachineMasterCMN/getMachineMasterDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.machineMasterRec)
        });
        this.machineCombo = new Wtf.form.ExtFnComboBox({
            name: 'machinename',
            store: this.machineStore,
            valueField: 'id',
            displayField: 'machinename',
            allowBlank: false,
            mode: 'local',
            typeAhead: true,
            width: 200,
            triggerAction: 'all',
            listWidth: 300,
            extraFields: [],
            addNoneRecord: false
        });
//        this.machineStore.load();
        this.notes = new Wtf.form.TextField({
            name: 'predecessorstype',
            hidden: false,
            width: 300,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });

        this.bomRec = Wtf.data.Record.create([
            {name: 'bomid'},
            {name: 'bomname'}
        ]);

        this.bomStore = new Wtf.data.Store({
            url: "ACCRoutingManagement/getBOMDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.bomRec)
        });

        this.bomid = new Wtf.form.ExtFnComboBox({
            name: 'bomid',
            store: this.bomStore,
            valueField: 'bomid',
            displayField: 'bomname',
            allowBlank: false,
            mode: 'local',
            typeAhead: true,
            width: 200,
            triggerAction: 'all',
            listWidth: 300,
            extraFields: [],
            addNoneRecord: false
        });
        this.subBOMStore = new Wtf.data.Store({
            url: "ACCRoutingManagement/getBOMDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.bomRec)
        });

        this.subbomid = new Wtf.form.ExtFnComboBox({
            name: 'subbomid',
            store: this.subBOMStore,
            valueField: 'bomid',
            displayField: 'bomname',
            allowBlank: false,
            mode: 'local',
            typeAhead: true,
            width: 200,
            triggerAction: 'all',
            listWidth: 300,
            extraFields: [],
            addNoneRecord: false
        });

        this.labourRec = Wtf.data.Record.create([
            {name: 'empid'},
            {name: 'empname'}
        ]);

        this.labourStore = new Wtf.data.Store({
            url: "ACCLabourCMN/getLaboursMerge.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.labourRec)
        });
        this.labourCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("mrp.workorder.entry.labourid"),
            name: 'empname',
            store: this.labourStore,
            valueField: 'empid',
            displayField: 'empname',
            allowBlank: false,
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            anchor: '75%',
            width: 200,
            listWidth: 300,
            extraFields: [],
            addNoneRecord: false
        });
        this.labourname = new Wtf.form.TextField({
            name: 'labourname',
            hidden: false,
            width: 300,
            maxLength: 50,
            scope: this,
            readOnly: true
        });
        this.checklist = new Wtf.form.TextField({
            name: 'checklist',
            hidden: false,
            width: 300,
            maxLength: 50,
            scope: this
        });

        this.stagesequence = new Wtf.form.NumberField({
            maxLength: 2,
            width: 30,
            allowDecimal: false,
            allowBlank: false,
            minValue: 1,
            name: 'stagesequence'
        });
    },
    columnModel: function() {
        var columnArr = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: false
        });
        columnArr.push(this.sm);
        columnArr.push(new Wtf.grid.RowNumberer());
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"), //"Sequence",
            align: 'center',
            dataIndex: 'srno',
            width: 60,
            renderer: Wtf.applySequenceRenderer
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.stagename"), //"stage name",
            width: 150,
            align: 'center',
            dataIndex: 'stagename',
            renderer: this.stageRenderer(this.process),
            editor: this.readOnly ? "" : this.process
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.stagesequence"), //"Stage Sequence",
            width: 100,
            align: 'center',
            dataIndex: 'stagesequence',
            editor: this.readOnly ? "" : this.stagesequence
        }, {
            header: WtfGlobal.getLocaleText("mrp.workorder.report.header8"), //"Route Code",
            width: 100,
            align: 'center',
            dataIndex: 'routecode',
            editor: this.readOnly ? "" : this.routeCode
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.starttime"), //"Start Time",
            width: 100,
            align: 'center',
            dataIndex: 'starttime',
            renderer:WtfGlobal.convertToDateOnly,
            editor: this.readOnly ? "" : this.startDate

        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.endtime"), //"End Time",
            width: 100,
            align: 'center',
            dataIndex: 'endtime',
            renderer: WtfGlobal.convertToDateOnly,
            editor: this.readOnly ? "" : this.endDate
        }, {
            header: WtfGlobal.getLocaleText("acc.taskProgressGrid.header2"), //"duration",
            width: 100,
            align: 'center',
            dataIndex: 'duration',
            editor: this.readOnly ? "" : this.duration
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.waitingperiod"), //"Waiting Period",
            width: 100,
            align: 'center',
            dataIndex: 'waitingperiod',
            editor: this.readOnly ? "" : this.waitingperiod
        }, {
            header: WtfGlobal.getLocaleText("acc.machineMasterGrid.workcenterFilter"), //"Work Center",
            width: 100,
            align: 'center',
            dataIndex: 'workcenter',
            renderer: this.wcRenderer(this.wcCombo),
            editor: this.readOnly ? "" : this.wcCombo
        }, {
            header: WtfGlobal.getLocaleText("acc.contractDetails.Status"), //"Status",
            width: 100,
            align: 'center',
            dataIndex: 'status',
            editor: this.readOnly ? "" : this.status
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.predecessors"), //"Predecessors",
            width: 100,
            align: 'center',
            dataIndex: 'predecessors',
            editor: this.readOnly ? "" : this.predecessors
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.predecessors.type"), //"Predecessors Type",
            width: 100,
            align: 'center',
            dataIndex: 'predecessorstype',
            editor: this.readOnly ? "" : this.predecessorstype
        }, {
            header: WtfGlobal.getLocaleText("acc.machineMasterGrid.header2"), //"Machine ID",
            width: 100,
            align: 'center',
            dataIndex: 'machineid',
            renderer: Wtf.comboBoxRenderer(this.machineCombo),
            editor: this.readOnly ? "" : this.machineCombo
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.bom.id"), //"BOM ID",
            width: 100,
            align: 'center',
            dataIndex: 'bomid',
            renderer: this.bomRenderer(this.bomid),
            editor: this.readOnly ? "" : this.bomid
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.subbom.id"), //"Sub BOM ID",
            width: 100,
            align: 'center',
            dataIndex: 'subbomname',
            renderer: this.subBOMRenderer(this.subbomid),
            editor: this.readOnly ? "" : this.subbomid
        }, {
            header: WtfGlobal.getLocaleText("mrp.workorder.entry.labourid"), //"Labour ID",
            width: 100,
            align: 'center',
            dataIndex: 'labourid',
            editor: this.readOnly ? "" : this.labourCombo
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.labourname"), //"Labour Name",
            width: 100,
            align: 'center',
            dataIndex: 'labourname',
            editor: this.readOnly ? "" : this.labourname

        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.notes"), //"Notes",
            width: 100,
            align: 'center',
            dataIndex: 'notes',
            editor: this.readOnly ? "" : this.notes
        }, {
            header: WtfGlobal.getLocaleText("acc.mrp.field.checklist"), //"Check List",
            width: 100,
            align: 'center',
            dataIndex: 'checklist',
            editor: this.readOnly ? "" : this.checklist
        });
        this.cm = new Wtf.grid.ColumnModel(columnArr);

    },
    createStore: function() {
        this.storeRec = Wtf.data.Record.create([
            {name: 'rowid', defValue: null},
            {name: 'srno', isForSequence: true},
            {name: 'stageid'},
            {name: 'stagename'},
            {name: 'stagesequence'},
            {name: 'routecode'},
            {name: 'starttime'},
            {name: 'endtime'},
            {name: 'duration'},
            {name: 'waitingperiod'},
            {name: 'workcenterid'},
            {name: 'workcenter'},
            {name: 'status'},
            {name: 'predecessors'},
            {name: 'predecessorstype'},
            {name: 'machineid'},
            {name: 'bomid'},
            {name: 'bomname'},
            {name: 'subbomid'},
            {name: 'subbomname'},
            {name: 'labourid'},
            {name: 'labourname'},
            {name: 'notes'},
            {name: 'checklist'}

        ]);
        this.store = new Wtf.data.Store({
            url: "ACCRoutingManagement/getRoutingMasterRows.do",
            pruneModifiedRecords: true,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.storeRec)
        });
//        this.store.load();
        this.store.on('load', this.addGridRec, this);
    },
    addBlankRow: function() {
        var Record = this.store.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {}, blankObj = {};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if (f.name != 'rowid') {
                blankObj[f.name] = '';
                if (!Wtf.isEmpty(f.defValue))
                    blankObj[f.name] = f.convert((typeof f.defValue == "function" ? f.defValue.call() : f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        this.store.add(newrec);
        this.getView().refresh();
    },
    stageRenderer: function(combo) {
        return function(value, metadata, record, row, col, store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store, value, combo.valueField);
            var fieldIndex = "stagename";
            if (idx == -1) {
                if (record.data["stagename"] && record.data[fieldIndex].length > 0) {
                    return record.data[fieldIndex];
                } else {
                    return "";
                }
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get("name");
            record.set("stageid", value);
            record.set("stagename", displayField);
            return displayField;
        }
    },
    wcRenderer: function(combo) {
        return function(value, metadata, record, row, col, store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store, value, combo.valueField);
            var fieldIndex = "workcenter";
            if (idx == -1) {
                if (record.data["workcenter"] && record.data[fieldIndex].length > 0) {
                    return record.data[fieldIndex];
                } else {
                    return "";
                }
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get("name");
            record.set("workcenterid", value);
            record.set("workcenter", displayField);
            return displayField;
        }
    },
    subBOMRenderer: function(combo) {
        return function(value, metadata, record, row, col, store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store, value, combo.valueField);
            var fieldIndex = "subbomname";
            if (idx == -1) {
                if (record.data["subbomname"] && record.data[fieldIndex].length > 0) {
                    return record.data[fieldIndex];
                } else {
                    return "";
                }
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get("bomname");
            record.set("subbomid", value);
            record.set("subbomname", displayField);
            return displayField;
        }
    },
    bomRenderer: function(combo) {
        return function(value, metadata, record, row, col, store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store, value, combo.valueField);
            var fieldIndex = "bomname";
            if (idx == -1) {
                if (record.data["bomname"] && record.data[fieldIndex].length > 0) {
                    return record.data[fieldIndex];
                } else {
                    return "";
                }
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get("bomname");
            record.set("bomid", value);
            record.set("bomname", displayField);
            return displayField;
        }
    },
    checkRow: function(obj) {
        var rec = obj.record;
        if (obj.field == "endtime") {
            if (rec.get('starttime') == "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Please select Start date first."], 2);
                obj.cancel = false;
                return false;
            }
            if (obj.value < rec.get('starttime')) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Start Date should be less or equal to End Date"], 2);
                obj.cancel = false;
                return false;
            }
            if (obj.value != "" && rec.get('starttime') != "") {
                var starttime = rec.get('starttime');
                var endtime = obj.value;
                var diff = new Date(endtime - starttime);
                var days = diff / 1000 / 60 / 60 / 24;
                rec.set("duration", (days + 1));
            }
        } else if (obj.field == "starttime") {
            if (rec.get('endtime') != undefined && rec.get('endtime') !== "" && rec.get('endtime') < obj.value) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Start Date should be less or equal to End Date"], 2);
                obj.cancel = false;
                return false;
            }
            if (obj.value != "" && rec.get('endtime') !== "") {
                var starttime = obj.value;
                var endtime = rec.get('endtime');
                var diff = new Date(endtime - starttime);
                var days = diff / 1000 / 60 / 60 / 24;
                rec.set("duration", (days + 1));
            }
        } else if (obj.field == "predecessors") {
            var isValidPredecessors = false;
            for (var i = 0; i < this.store.getCount(); i++) {
                if (obj.value == this.store.getAt(i).data['stagesequence']) {
                    isValidPredecessors = true;
                }
            }
            if (!isValidPredecessors) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Stage sequence not present."], 2);
                obj.cancel = false;
                return false;
            }
        } else if (obj.field == "stagesequence") {
            if (obj.value > (this.store.getCount() - 1)) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Firstly please define the stage for added stage sequence"], 2);
                obj.cancel = false;
                return false;
            }
        }
    },
    getTotalDuration: function() {
        var duration = 0;
        for (var i = 0; i < this.store.getCount(); i++) {
            duration += this.store.getAt(i).data['duration'];
        }
        return duration;
    },
    updateRow: function(obj) {
        if (obj.field == "endtime") {
            this.fireEvent('datachanged', this);
        } else if (obj.field == "starttime") {
            this.fireEvent('datachanged', this);
        }
        if (this.store.getCount() > 0 && this.store.getAt(this.store.getCount() - 1).data['stageid'].length <= 0) {
            return;
        }
        this.addBlankRow();
    },
    handleRowClick: function(grid, rowindex, e) {
        if (e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid, 0, rowindex);
        }
        if (e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid, 1, rowindex);
        }
    }

});


