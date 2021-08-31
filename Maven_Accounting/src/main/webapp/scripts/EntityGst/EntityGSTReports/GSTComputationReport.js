/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function callGSTComputationReport(gstrreporttype) {

    var winid = 'GSTComputation'+gstrreporttype;
    var GSTSummarypanel = Wtf.getCmp(winid);
    if (GSTSummarypanel == null) {
        GSTSummarypanel = new Wtf.account.callGSTComputationReport({
            layout: "fit",
            title: gstrreporttype!=undefined?WtfGlobal.getLocaleText("acc.GSTR3B.POSTitle"):WtfGlobal.getLocaleText("acc.GSTR3B.Title"),
            tabTip: gstrreporttype!=undefined?WtfGlobal.getLocaleText("acc.GSTR3B.POSTitle"):WtfGlobal.getLocaleText("acc.GSTR3B.Title"),
            border: false,
            closable: true,
            id: winid,
            gstrreporttype:gstrreporttype,
            isShipping: CompanyPreferenceChecks.getGSTCalCulationType()!=undefined? CompanyPreferenceChecks.getGSTCalCulationType():false // Flag for identification whether GST calculation on Billing Address or Shipping Address

        });
        Wtf.getCmp('as').add(GSTSummarypanel);
    }
    Wtf.getCmp('as').setActiveTab(GSTSummarypanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.callGSTComputationReport = function (config) {
    this.arr = [];
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createStore();
    this.createTBar();
    /*
     * Create Grid 
     */
    this.createGrid();

    Wtf.account.callGSTComputationReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.callGSTComputationReport, Wtf.Panel, {
    onRender: function (config) {
        /*
         * create panel to show grid
         */
        this.createPanel();
        this.add(this.leadpan);
        /*
         * fetch data in report
         */
//        this.fetchStatement();
        Wtf.account.callGSTComputationReport.superclass.onRender.call(this, config);
    },
    createPanel: function () {
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.btnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        }),
                        items: this.bbarBtnArr
                    })
                }]
        });
    },
    createTBar: function () {
        this.btnArr = [];
        this.bbarBtnArr = [];
        this.exportButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.report.detailedgstcomputation.summarytitle"),
            tooltip: WtfGlobal.getLocaleText("acc.report.detailedgstcomputation.summarytitle"),
            scope: this,
            id: "GSTR1Summary" + this.id,
            name: "GSTR1Summary",
            iconCls: 'pwnd ' + 'exportcsv',
            handler: function() {
                /**
                 * Changes month and year to start and end date
                 */
                var otherParameter = "startdate=" +WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                        + "&filetype=xls&filename=GST Computation Summary Report&entity="+this.EntityCombo.getRawValue()+"&entityid="+this.EntityCombo.getValue()+"&reportType="+this.gstrreporttype+"";//&documentType="+this.documentType.getValue()+"&ss="+this.quickPanelSearch.getValue();
                var url = "AccEntityGST/ExportGSTRComputationReport.do"
                Wtf.get('downloadframe').dom.src = url + "?" + otherParameter;
            }
        });
        this.exportDetailedBtn = new Wtf.Action({
            iconCls: 'pwnd ' + 'exportcsv',
            text: WtfGlobal.getLocaleText("acc.report.detailedgstcomputation.detailtitle"),
            tooltip: WtfGlobal.getLocaleText("acc.report.detailedgstcomputation.detailtitle"),
            scope: this,
            handler: function () {
                 /**
                 * Changes month and year to start and end date
                 */
                var otherParameter = "startdate=" + WtfGlobal.convertToGenericEndDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                        + "&filetype=xls&filename=GST Computation Detail Report&isdetailreport=true&entity="+this.EntityCombo.getRawValue()+"&entityid="+this.EntityCombo.getValue()+"\
                        &reportType="+this.gstrreporttype+"";
                var url = "AccEntityGST/ExportGSTRComputationReport.do"
                Wtf.get('downloadframe').dom.src = url + "?" + otherParameter;
            }
        });
       this.bbarBtnArr.push('-', this.exportButton);
       this.bbarBtnArr.push('-', this.exportDetailedBtn);
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.labourList.quickSearch"),
            width: 200,
            id: "quickSearch" + this.id,
            field: 'empid'
        });
         /**
          * Changed month and year to start and end date
          */
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(false)
        });
        /**
         * Added Start and End date in toolbar
         */
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });


    

        //********* From Entity **************************
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
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.field.Entity") + "*" + ":");
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
            width: 240,
            listWidth: 240
        });
        this.EntityComboStore.load();
        this.btnArr.push('-', this.EntityCombo);
        this.btnArr.push('-', this.fetchBttn);
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.btnArr.push('-', this.resetBttn);
        this.EntityComboStore.on('load', function () {
            var count = this.EntityComboStore.getCount();
            if (count > 0) {
                var seqRec = this.EntityComboStore.getAt(0);
                this.EntityCombo.setValue(seqRec.data.id);
                //this.fetchStatement();
            }
        }, this);
        this.expandCollpseButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.Expand"),
            tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
            iconCls: 'pwnd toggleButtonIcon',
            scope: this,
            handler: function () {
                this.expandCollapseGrid(this.expandCollpseButton.getText());
            }
        });

    this.btnArr.push('-', this.expandCollpseButton);
    },
    createStore: function() {
        this.Store = new Wtf.data.Store({
            url: "AccEntityGST/getGSTRComputationReport.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
    },
    createGrid: function() {

        var columnArr = [];
        columnArr.push({
             header: "",
            dataIndex: '',
            width: 200,
            align: 'left',
            hidden: true,
            pdfwidth: 200
        });
        this.grid = new Wtf.grid.HirarchicalGridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: columnArr,
            border: false,
            loadMask: true,
            hirarchyColNumber: 0,
            viewConfig: {
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))
            }
        });
        this.grid.on('render', function() {
            this.grid.getView().getRowClass = this.getRowClass.createDelegate(this, [this.grid], 1);
            this.grid.getView().refresh();
            //this.expandCollapseGrid("Expand");
        }, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.grid.on('cellclick', this.onCellClick, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('loadexception',WtfGlobal.resetAjaxTimeOut(),this);
    },
    getRowClass: function(record, grid) {
        var colorCss = "";
        switch (record.data["fmt"]) {
            case "T":
                colorCss = " grey-background";
                break;
            case "B":
                colorCss = " red-background";
                break;
            case "H":
                colorCss = " header-background";
                break;
            case "A":
                colorCss = " darkyellow-background";
                break;
        }
        return grid.getRowClass() + colorCss;
    },
    handleStoreBeforeLoad: function(store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.entity = this.EntityCombo.getRawValue();
        currentBaseParams.entityid = this.EntityCombo.getValue(),
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate= WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.reportType = this.gstrreporttype!=undefined?this.gstrreporttype:"";
        currentBaseParams.isShipping = this.isShipping; // Flag for identification whether GST calculation on Billing Address or Shipping Address
        this.Store.baseParams = currentBaseParams;
    },
    fetchStatement: function () {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
//                ss: this.quickPanelSearch.getValue(),
                entity: this.EntityCombo.getRawValue(),
                entityid:this.EntityCombo.getValue(),
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
    },
    onCellClick: function(g, i, j, e) {
        var event = e;
        if (event.getTarget("img[class='add']")) {
            var header = g.getColumnModel().getDataIndex(j);
            var config = {};
            if (header == "view") {
                var formrec = this.grid.getStore().getAt(i);
                var section = formrec.get("typeofsales");
                config.section = section;
                config.entity = this.EntityCombo.getRawValue();
                config.entityid = this.EntityCombo.getValue();
                config.gstrreporttype = this.gstrreporttype;
                config.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                config.enddate= WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                config.isComputation = true;
                config.isShipping = this.isShipping; // Flag for identification whether GST calculation on Billing Address or Shipping Address
                /**
                 * Load GST computation detailed reoprt JS file dynamically
                 */
                callGSTR3BAndComputationDetailedReport(config, 1);
            }
        }

    }, 
    handleStoreOnLoad: function (store) {
        WtfGlobal.resetAjaxTimeOut();
        var columns = [];
//        columns.push(new Wtf.grid.RowNumberer({
//            width: 30
//        }));

        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex === "heading") {
                column.renderer = this.formatAccountName.createDelegate(this.grid);
            } else if (column.dataIndex == "view") {
                column.renderer = function(value, css, record, row, column, store) {
                    if (!record.data.enableViewDetail) {
                        return "";
                    } else {
                        return "<img id='AcceptImg' class='add'  style='height:18px; width:18px;' src='images/report.gif' title='View Report '></img>";
                    }
                };
//                column.hidden = true;
            } else
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

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }

    },
    formatAccountName: function(val, m, rec, i, j, s) {
        var fmtVal = 0;
        var fmtVal = (rec.data['invoiceid'] == undefined || rec.data['invoiceid'] == '') ? val : "<a  class='jumplink' href='#' onClick='javascript:invRecLinkNew(\"" + rec.data['invoiceid'] + "\",\"" + true + "\")'>" + val + "</a>";
        if (val) {
            if (rec.data['leaf'] == true) {
                fmtVal = "<div style='margin-left:" + (rec.data['level'] * 20) + "px;padding-left:20px'>" + fmtVal + "</div>";
            } else {
                fmtVal = "<div class='" + this.expandercss + "' style='margin-left:"
                        + (rec.data['level'] * 20) + "px;width:20px'><div style='margin-left:20px'><b>" + fmtVal + "</b></div></div>";
            }
        }
        return fmtVal;
    },
    handleResetClickNew: function()
    {
        this.quickPanelSearch.reset();
        this.Store.load({
            params: {
                start: 0,
                limit: 30,
                entity: this.EntityCombo.getRawValue(),
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        })
    },
    getDates: function (start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
     expandCollapseGrid: function(btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            for (var i = 0; i < this.grid.getStore().data.length; i++) {
                this.grid.collapseRow(this.grid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            for (var i = 0; i < this.grid.getStore().data.length; i++) {
                   this.grid.expandRow(this.grid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },

});