/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function callGSTR1Summary(gstrreporttype) {

    var winid = 'GSTSummary' + gstrreporttype;
    var GSTSummarypanel = Wtf.getCmp(winid);
    if (GSTSummarypanel == null) {
        GSTSummarypanel = new Wtf.account.callGSTR1Summary({
            layout: "fit",
            title: gstrreporttype == 1 ? "GSTR1 Summary" : "GSTR2 Summary",
            tabTip: gstrreporttype == 1 ? "GSTR1 Summary" : "GSTR2 Summary",
            border: false,
            closable: true,
            id: winid,
            gstrreporttype: gstrreporttype,
            isShipping: CompanyPreferenceChecks.getGSTCalCulationType()!=undefined? CompanyPreferenceChecks.getGSTCalCulationType():false // Flag for identification whether GST calculation on Billing Address or Shipping Address
        });
        Wtf.getCmp('as').add(GSTSummarypanel);
    }
    Wtf.getCmp('as').setActiveTab(GSTSummarypanel);
    Wtf.getCmp('as').doLayout();
}
function callGSTRMisMatchSummary(gstrreporttype) {
    var winid = 'GSTSummary' + gstrreporttype;
    var GSTSummarypanel = Wtf.getCmp(winid);
    if (GSTSummarypanel == null) {
        GSTSummarypanel = new Wtf.account.callGSTR1Summary({
            layout: "fit",
            title: "MisMatch Summary",
            tabTip: "MisMatch Summary",
            border: false,
            closable: true,
            id: winid,
            gstrreporttype: gstrreporttype

        });
        Wtf.getCmp('as').add(GSTSummarypanel);
    }
    Wtf.getCmp('as').setActiveTab(GSTSummarypanel);
    Wtf.getCmp('as').doLayout();
}
Wtf.account.callGSTR1Summary = function (config) {
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

    Wtf.account.callGSTR1Summary.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.callGSTR1Summary, Wtf.Panel, {
    onRender: function (config) {
        /*
         * create panel to show grid
         */
        this.createPanel();
        this.add(this.leadpan);
        /*
         * fetch data in report
         */
       // this.fetchStatement();
        Wtf.account.callGSTR1Summary.superclass.onRender.call(this, config);
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
        this.exportBtn = new Wtf.menu.Item({
            text: "Export Xls",
            iconCls: 'pwnd ' + 'exportcsv',
            scope: this,
            handler: function () {
                var otherParameter = "";
                var url = "";
                if (this.gstrreporttype == 2) {
                    url = "AccEntityGST/ExportGSTR2Summary.do"
                    otherParameter = "startdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                            + "&filetype=xls&filename=GSTR2 Report &entity=" + this.EntityCombo.getRawValue() + "&entityid=" + this.EntityCombo.getValue()+"";//&documentType="+this.documentType.getValue()+"&ss="+this.quickPanelSearch.getValue();
                } else  if (this.gstrreporttype == 3) {
                    url = "AccEntityGST/ExportGSTRMisMatchSummary.do"
                    otherParameter = "startdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                            + "&filetype=xls&filename=GST MisMatch Report &entity=" + this.EntityCombo.getRawValue() + "&entityid=" + this.EntityCombo.getValue()+"";//&documentType="+this.documentType.getValue()+"&ss="+this.quickPanelSearch.getValue();
                } else {
                    url = "AccEntityGST/ExportGSTR1Summary.do"
                    otherParameter = "startdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                            + "&filetype=xls&filename=GSTR1 Report &entity=" + this.EntityCombo.getRawValue() + "&entityid=" + this.EntityCombo.getValue()+"";//&documentType="+this.documentType.getValue()+"&ss="+this.quickPanelSearch.getValue();
                }
                /**
                 * Add company level flag to check is Show Vendor address in Purchase document is OFF or ON
                 */
                otherParameter += "&isAddressNotFromVendorMaster=" + ((WtfGlobal.isIndiaCountryAndGSTApplied() && !Wtf.account.companyAccountPref.isAddressFromVendorMaster)? true : false);
                Wtf.get('downloadframe').dom.src = url + "?" + otherParameter;
            }
        });
        this.exportEfiling = new Wtf.menu.Item({
            text: "Export Xls: E-Filing",
            iconCls: 'pwnd ' + 'exportcsv',
            scope: this,
            handler: function () {

                    var otherParameter = "startdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                            + "&filetype=xls&filename=GSTR1 Report &entity=" + this.EntityCombo.getRawValue() + "&entityid=" + this.EntityCombo.getValue()+"";//&documentType="+this.documentType.getValue()+"&ss="+this.quickPanelSearch.getValue();
                    var url = "AccEntityGST/ExportGSTR1Efiling.do"
                    Wtf.get('downloadframe').dom.src = url + "?" + otherParameter;
                }
        });
        if (this.gstrreporttype === 1) {
            this.exportMenu.push(this.exportBtn);
            this.exportMenu.push(this.exportEfiling);
        }else{
            this.exportMenu.push(this.exportBtn);
        }
        this.csvbtn = new Wtf.Action({
       
            iconCls: 'pwnd ' + 'exportcsv',

            text: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.exportToXLSTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToXLSX") + "</span>",
            menu:{
                items:this.exportMenu
            }
            
            
        });
       
        this.mnuBtns.push(this.csvbtn);
        this.bbarBtnArr.push('-', this.mnuBtns);
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.labourList.quickSearch"),
            width: 200,
            id: "quickSearch" + this.id,
            field: 'empid'
        });
        //        this.btnArr.push(this.quickPanelSearch);
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false)
        });
        this.btnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
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
               // this.fetchStatement();
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
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.btnArr.push('-', this.resetBttn);
        this.resetBttn.on('click', this.handleResetClickNew, this);

    },
    createStore: function () {
        this.Store = new Wtf.data.Store({
            url: this.gstrreporttype === 1 ? "AccEntityGST/getGSTR1Summary.do" : this.gstrreporttype ==3 ? "AccEntityGST/getGSTRMisMatchSummary.do" : "AccEntityGST/getGSTR2Summary.do",
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
            viewConfig: {
                forceFit: true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),
                getRowClass: function(record, index) {
                    return 'red-background'
                } 
            }
        });
        this.grid.on('render',function(){
            this.grid.getView().refresh();
        },this);
        
        this.grid.on('cellclick', this.onCellClick, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('loadexception',WtfGlobal.resetAjaxTimeOut(),this);
    },
    handleStoreBeforeLoad: function (store) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.entity = this.EntityCombo.getRawValue();
        currentBaseParams.entityid = this.EntityCombo.getValue();
        currentBaseParams.isShipping = this.isShipping; // Flag for identification whether GST calculation on Billing Address or Shipping Address
        currentBaseParams.isAddressNotFromVendorMaster = (WtfGlobal.isIndiaCountryAndGSTApplied() && !Wtf.account.companyAccountPref.isAddressFromVendorMaster)? true : false;
        this.Store.baseParams = currentBaseParams;
    },
    fetchStatement: function () {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                //                ss: this.quickPanelSearch.getValue(),
                entity: this.EntityCombo.getRawValue(),
                entityid: this.EntityCombo.getValue(),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
    },
    handleStoreOnLoad: function (store) {
       WtfGlobal.resetAjaxTimeOut();
        var columns = [];
//        columns.push(new Wtf.grid.RowNumberer({
//            width: 30
//        }));
    /*
    *  Add view-image for details
    */
       
        Wtf.each(this.Store.reader.jsonData.columns, function (column) {

            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            else {
                column.renderer = WtfGlobal.deletedRenderer;
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
        this.startDate.setValue(this.getDates(true));
        this.endDate.setValue(this.getDates(false));
        this.EntityComboStore.load();
        this.Store.load({
            params: {
                start: 0,
                limit: 30,
                entity: this.EntityCombo.getRawValue(),
                entityid: this.EntityCombo.getValue(),
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                isShipping : this.isShipping
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
    onCellClick: function (g, i, j, e) {
        var event = e;
        if (event.getTarget("img[class='add']")) {
        //    var el=e.getTarget("a");
        //    if(el==null)return; 
        var header = g.getColumnModel().getDataIndex(j);
        var config = {};
        if (header == "view") {
            var formrec = this.grid.getStore().getAt(i);
            var section = formrec.get("typeofinvoice");
            section=section.replace("<b>","");
            section=section.replace("<b>","");
            config.section = section;
            config.entity = this.EntityCombo.getRawValue();
            config.entityid = this.EntityCombo.getValue();
            config.gstrreporttype = this.gstrreporttype;
            config.startDate = this.startDate.getValue();
            config.endDate = this.endDate.getValue();
            config.isShipping= this.isShipping;
            /**
             * Load GSTR1,GSTR2 and MisMatch details report JS dynamically 
             */
            callGSTRDetailedReport(config);

        }

      }
    }
});