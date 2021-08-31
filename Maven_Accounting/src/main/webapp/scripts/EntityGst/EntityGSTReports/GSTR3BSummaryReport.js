/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function callGSTR3BSummaryReport(gstrreporttype) {

    var winid = 'GSTR3BSummaryReport' + gstrreporttype;
    var GSTSummarypanel = Wtf.getCmp(winid);
    if (GSTSummarypanel == null) {
        GSTSummarypanel = new Wtf.account.GSTR3BSummaryReport({
            layout: "fit",
            title: "GSTR3B",
            tabTip: "GSTR3B-Summary Report",
            border: false,
            closable: true,
            id: winid,
            isShipping: CompanyPreferenceChecks.getGSTCalCulationType()!=undefined? CompanyPreferenceChecks.getGSTCalCulationType():false // Flag for identification whether GST calculation on Billing Address or Shipping Address

        });
        Wtf.getCmp('as').add(GSTSummarypanel);
    }
    Wtf.getCmp('as').setActiveTab(GSTSummarypanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.GSTR3BSummaryReport = function (config) {
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

    Wtf.account.GSTR3BSummaryReport.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.GSTR3BSummaryReport, Wtf.Panel, {
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
        Wtf.account.GSTR3BSummaryReport.superclass.onRender.call(this, config);
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
        this.exportBtnArr = [];
        this.exportMenu = [];  
        this.mnuBtns = [];
        this.exportBtn = new Wtf.exportButton({
            obj: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),
            filename: "GSTR3B Report" + "_v1",
            menuItem: {
                xls: true
            },
            label: "GSTR3B Report",
            get: Wtf.autoNum.GSTR3BReport
        });
        this.exportDetailedBtn = new Wtf.Action({
            iconCls: 'pwnd ' + 'exportcsv',
            text: WtfGlobal.getLocaleText("acc.report.detailedgstr3b.title"),
            tooltip: WtfGlobal.getLocaleText("acc.report.detailedgstr3b.title"),
            scope: this,
            handler: function () {
                var url = "AccEntityGST/exportGSTR3BSummaryDetails.do?isFromSummaryView=true";
                var parameters = "&entity=" + this.EntityCombo.getRawValue();
                parameters += "&startdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                parameters += "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                parameters += "&entityid=" + this.EntityCombo.getValue();
                parameters += "&isShipping=" + this.isShipping;
                Wtf.get('downloadframe').dom.src = url+parameters;
            }
        });
        this.bbarBtnArr.push('-', this.exportBtn);
        this.bbarBtnArr.push('-', this.exportDetailedBtn);
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.labourList.quickSearch"),
            width: 200,
            id: "quickSearch" + this.id,
            field: 'empid'
        });
        /**
         * Add start and End date filter 
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
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
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
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.field.Entity") + "*" + ":");
        this.btnArr.push('-', this.EntityCombo);
        this.EntityComboStore.on('load', function () {
            var count = this.EntityComboStore.getCount();
            if (count > 0) {
                var seqRec = this.EntityComboStore.getAt(0);
                this.EntityCombo.setValue(seqRec.data.id);
            }
        }, this);
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function () {
                if (this.EntityCombo.getRawValue() === "") {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gstr1.validationEntity")], 2);
                } else {
                    this.fetchStatement();
                }
            }
        });
        this.btnArr.push('-', this.fetchBttn);

    },
    createStore: function () {
        this.Store = new Wtf.data.Store({
            url: "AccEntityGST/getGSTR3BSummaryReport.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
    },
    createGrid: function () {
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            loadMask: true,
            autoScroll : true, // Added auto scroll property to add scroll on small screen
            viewConfig: {
               // forceFit: true, // Removed forcefit
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),
                getRowClass: function(record, index) {
                    return 'red-background';
                } 
            }
        });
         this.grid.on('render',function(){
            this.grid.getView().refresh();
        },this);
        this.grid.on('cellclick', this.onCellClick, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('loadexception',WtfGlobal.resetAjaxTimeOut(),this);
    },
    handleStoreBeforeLoad: function (store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.entity = this.EntityCombo.getRawValue();
        currentBaseParams.entityid = this.EntityCombo.getValue();
        currentBaseParams.isShipping = this.isShipping;
        currentBaseParams.isAddressNotFromVendorMaster = (WtfGlobal.isIndiaCountryAndGSTApplied() && !Wtf.account.companyAccountPref.isAddressFromVendorMaster)? true : false;
        this.Store.baseParams = currentBaseParams;
    },
    fetchStatement: function () {
        if (this.startDate.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mprp.noMonthIsSelected")], 2);
            return;
        }
        if (this.endDate.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.pleaseSelectYear")], 2);
            return;
        }
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                entity: this.EntityCombo.getRawValue(),
                entityid: this.EntityCombo.getValue()
            }
        });
    },
    handleStoreOnLoad: function (store) {
        WtfGlobal.resetAjaxTimeOut()
        var columns = [];
//        columns.push(new Wtf.grid.RowNumberer({
//            width: 30
//        }));

        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
            if (column.dataIndex == "view") {
                column.renderer = function(value, css, record, row, column, store){
                    if (!record.data.enableViewDetail) {
                        return "";
                    } else {
                        return "<img id='AcceptImg' class='add'  style='height:18px; width:18px;' src='images/report.gif' title='View Report '></img>";
                    }
                };
//                column.hidden = true;
            } else if (column.dataIndex === "taxableamt" || column.dataIndex === "igst" || column.dataIndex === "cgst" || column.dataIndex === "sgst" || column.dataIndex === "csgst" || column.dataIndex === "totaltax") {
                column.renderer = function (value, isCheckCenterAlign, rec) {
                    var isCenterAlign = (isCheckCenterAlign == undefined ? false : isCheckCenterAlign[0]);
                    var v = parseFloat(value);
                    if (isNaN(v))
                        return value;
                    v = WtfGlobal.conventInDecimal(v, WtfGlobal.getCurrencySymbol());
                    if (rec.data.deleted)
                        v = '<del>' + v + '</del>';

                    v = '<div class="currency">' + v + '</div>';
                    if (rec.data.showBold) {

                        v = "<b>" + v + "</b>";
                    }
                    if (isCenterAlign)
                        v = '<div>' + v + '</div>';
                    return v;
                }
            } else {
                column.renderer = function(value, css, record, row, column, store){
                    if (!record.data.showBold) {
                        return value;
                    } else {
                        return "<b>" + value + "</b>";
                    }
                };;
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    handleResetClickNew: function ()
    {
        this.quickPanelSearch.reset();
        this.Store.load({
            params: {
                start: 0,
                limit: 30,
                entity: this.EntityCombo.getRawValue(),
                entityid: this.EntityCombo.getValue()
            }
        })
    },
    onCellClick: function (g, i, j, e) {
         var event = e;
        if (event.getTarget("img[class='add']")) {
        //    var el=e.getTarget("a");
        //    if(el==null)return; 
        var header = g.getColumnModel().getDataIndex(j);
        var config = {};
        if (header == "view") {
            var formrec = this.grid.getStore().getAt(i);
            var section = formrec.get("particulars");
            config.section = section;
            config.entity = this.EntityCombo.getRawValue();
            config.entityid = this.EntityCombo.getValue();
            config.gstrreporttype = this.gstrreporttype;
            config.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
            config.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
            config.isShipping= this.isShipping; // Flag for identification whether GST calculation on Billing Address or Shipping Address
            /**
             * Load GSTR3B detailed reoprt JS file dynamically
             */
            callGSTR3BAndComputationDetailedReport(config, 2);
        }
        }

    }, 
   currencyDeletedRenderer: function(value,isCheckCenterAlign,rec) {
        var isCenterAlign=(isCheckCenterAlign==undefined?false:isCheckCenterAlign[0]);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
            v= WtfGlobal.conventInDecimal(v,WtfGlobal.getCurrencySymbol());
            if(rec.data.deleted)
                v='<del>'+v+'</del>';
            
            v = '<div class="currency">' + v + '</div>';
             if(rec.data.showBold){
                        
                        v ="<b>" + v+ "</b>";
             }
            if(isCenterAlign)
                  v= '<div>'+v+'</div>';
         return v;
    }
});