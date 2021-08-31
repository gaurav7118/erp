/* 
 * Component used to configure the custome data  for export
 */

Wtf.account.ExportCustomdataInFinancialReport = function (config) {
    this.expObj = config.expObj;
    Wtf.apply(this, {
        layout: 'border',
        buttons: [this.saveButton = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                minWidth: 50,
                scope: this,
                handler: this.saveForm.createDelegate(this)
            }), this.closeButton = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                minWidth: 50,
                scope: this,
                handler: this.closeOpenWin.createDelegate(this)
            })]
    }, config);

    Wtf.account.ExportCustomdataInFinancialReport.superclass.constructor.call(this, config);

}

Wtf.extend(Wtf.account.ExportCustomdataInFinancialReport, Wtf.Window, {
    onRender: function (config) {
        Wtf.account.ExportCustomdataInFinancialReport.superclass.onRender.call(this, config);
        /**
         * method to create to form
         */
        this.creategrid();
//        this.createForm();
//        this.createmoduleWiseEditorGrid();
        this.add(
                {
                    region: 'north',
                    height: 100,
                    border: false,
                    id: 'resolveConflictNorth_panel_Overrite',
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.configure.window.header"), WtfGlobal.getLocaleText("acc.configure.window.header"), "../../images/accounting_image/role-assign.gif",true)

                }
        );
        this.centralPanel = new Wtf.Panel({
            waitMsgTarget: true,
            border: false,
            region: "center",
            bodyStyle: "background: transparent;",
            style: "background:transparent; padding:10px;",
//            autoScroll : true,
            items: [this.moduleHeaderGrid]

        });
        this.add(this.centralPanel);
  },
    creategrid: function () {

        this.moduleHeaderRec = new Wtf.data.Record.create([
            {name: "id"},
            {name: "title"},
            {name: "fieldType"},
            {name: "module"},
            {name: "header"},
            {name: "iscd"},
            {name: "customcolumn"},
            {name: "forexpander"},
            {name: "isdd"},
            {name: "isconfigureCustomdata"},
            {name: "groupinfo"},
            {name: "isManadatoryField"},
            {name: "isshowinview"}
        ]);

        this.moduleHeaderReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"
        }, this.moduleHeaderRec);

        this.moduleHeaderStore = new Wtf.data.GroupingStore({
            url: "ACCReports/getExportConfiguredCustomData.do",
            reader: this.moduleHeaderReader,
            baseParams: {
            },
            groupField: "fieldType",
            sortInfo: {field: 'fieldType', direction: "ASC"}
        });
        this.moduleHeaderStore.load();
//        }
        var isShowInExport = new Wtf.grid.CheckColumnCustomized({
            header: WtfGlobal.getLocaleText("Show in Export(Detailed)"),
            dataIndex: 'isconfigureCustomdata',
            align:'center',
            width: 50,
            renderer : this.checkColumnRenderer
        });
        var isShowInView = new Wtf.grid.CheckColumnCustomized({
            header: WtfGlobal.getLocaleText("Show in View"),
            dataIndex: 'isshowinview',
            align:'center',
            width: 50,
            renderer : this.checkColumnRenderer
        });
//        var isShowInExport = new Wtf.grid.RowSelectionModel({});
        this.moduleHeaderColumn = new Wtf.grid.ColumnModel([
            {
                header: WtfGlobal.getLocaleText("acc.customfield.customtype"), //"Custom Type",
                hidden: true,
                fixed: true,
                dataIndex: "fieldType"

            }, {
                header: WtfGlobal.getLocaleText("acc.customfield.fieldname"), //"Header",
                dataIndex: "title",
                groupRenderer: WtfGlobal.nameRenderer
            }, {
                header: WtfGlobal.getLocaleText("acc.field.Modules"),
                dataIndex: "module",
                groupRenderer: WtfGlobal.nameRenderer,
                renderer: function (val) {
                    return "<div wtf:qtip=\"" + val + "\">" + val + "</div>";
                }

            }, isShowInExport,isShowInView]);
        this.groupingView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: true,
            groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ?"' + WtfGlobal.getLocaleText("acc.item.plural") + '":"' + WtfGlobal.getLocaleText("acc.item") + '"]})', //"Items" : "Item"]})',
            hideGroupedColumn: true,
            emptyText: "<div class='grid-empty-text'>" + WtfGlobal.getLocaleText("acc.common.norec") + "</div>"
        });
        this.moduleHeaderGrid = new Wtf.grid.EditorGridPanel({
            store: this.moduleHeaderStore,
            height: 360,
//            sm : isShowInExport,
//            region:"center",
//            sm:sm,
            loadMask: true,
            plugins: [isShowInExport,isShowInView],
            cm: this.moduleHeaderColumn,
            view: this.groupingView
        });

    },
    closeOpenWin: function () {
        this.close();
    },
    checkColumnRenderer: function (v, p, record) {
        var qtip = ''
        if (record.data.isManadatoryField) {
            p.css += "x-item-disabled";
            qtip += "Mandatory Field";
        } else {
            p.css += ' x-grid3-check-col-td';
        }
        return '<div class="x-grid3-check-col' + (v ? '-on' : '') + ' x-grid3-cc-' + this.id + '" wtf:qtip="' + qtip + '">&#160;</div>';
    },
    /**
     *  
     * Save method 
     */
    saveForm: function () {
        var records = this.moduleHeaderGrid.getStore();
        var dataArr = new Array();
        var id = "";
        records.each(function (record) {
            var isconfigureCustomdata = record.get('isconfigureCustomdata') != "" && record.get('isconfigureCustomdata') != undefined ? record.get('isconfigureCustomdata') : false;
            var isShowInView = record.get('isshowinview') != "" && record.get('isshowinview') != undefined ? record.get('isshowinview') : false;
            var jObject = {};
            jObject.title = record.get('title');
            jObject.header = record.get('header');
            jObject.isconfigureCustomdata = isconfigureCustomdata;//Hide/Show flag for Export
            jObject.isshowinview = isShowInView;//Hide/Show flag for View 
            id = record.get('id')
            dataArr.push(jObject);
        });
        Wtf.Ajax.requestEx({
            url: "ACCReports/saveGLConfiguration.do",
            params: {
                data: JSON.stringify(dataArr),
                id: id
            }
        }, this,
                function (req, res) {
                    var restext = req;
                    if (restext.success) {
                        restext.msg=restext.msg+WtfGlobal.getLocaleText("acc.configure.window.save.note");
                        this.close();
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), restext.msg], 3);
                        WtfGlobal.exportReportInThread(this.expObj);

                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), restext.msg], 1);
                    }

                });
    }

})




