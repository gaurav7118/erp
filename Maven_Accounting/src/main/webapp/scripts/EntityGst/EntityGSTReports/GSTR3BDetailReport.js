/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

function callGSTR3BDetails(config) {

    var winid = 'GSTR3BDetailedReport' + companyid;
    var GSTSummaryDetailspanel = Wtf.getCmp(winid);
    if (GSTSummaryDetailspanel == null) {
        GSTSummaryDetailspanel = new Wtf.account.GSTR3BDetails({
            layout: "fit",
            title: WtfGlobal.getLocaleText("acc.report.detailedgstr3b.title"),
            tabTip: WtfGlobal.getLocaleText("acc.report.detailedgstr3b.title"),
            section: config.section,
            border: false,
            closable: true,
            entity: config.entity,
            entityid: config.entityid,
            startdate: config.startdate,
            enddate: config.enddate,
            isComputation:false,
            id: winid,
            isShipping:config.isShipping!= undefined?config.isShipping:false // Flag for identification whether GST calculation on Billing Address or Shipping Address

        });
        Wtf.getCmp('as').add(GSTSummaryDetailspanel);
        Wtf.getCmp('as').setActiveTab(GSTSummaryDetailspanel);
    } else {
        /**
         * If section already open and from Summary click to view another section then just update filetrs and relaod data
         */
        GSTSummaryDetailspanel.setSection(config.section, config.entity, config.entityid, config.startdate, config.enddate);
    }

    Wtf.getCmp('as').setActiveTab(GSTSummaryDetailspanel);
    Wtf.getCmp('as').doLayout();
}
function callGSTComputationDetails(config) {
    var winid = 'ComputationDetails' + companyid;
    var GSTSummaryDetailspanel = Wtf.getCmp(winid);
    if (GSTSummaryDetailspanel == null) {
        GSTSummaryDetailspanel = new Wtf.account.GSTR3BDetails({
            layout: "fit",
            title: WtfGlobal.getLocaleText("acc.report.detailedgstcomputation.title"),
            tabTip: WtfGlobal.getLocaleText("acc.report.detailedgstcomputation.title"),
            section: config.section,
            border: false,
            closable: true,
            entity: config.entity,
            entityid: config.entityid,
            startdate: config.startdate,
            enddate: config.enddate,
            isComputation: config.isComputation,
            id: winid,
            isShipping: config.isShipping != undefined ? config.isShipping : false // Flag for identification whether GST calculation on Billing Address or Shipping Address
        });
        Wtf.getCmp('as').add(GSTSummaryDetailspanel);
        Wtf.getCmp('as').setActiveTab(GSTSummaryDetailspanel);
    } else {
        /**
         * If section already open and from Summary click to view another section then just update filetrs and relaod data
         */
        GSTSummaryDetailspanel.setSection(config.section, config.entity, config.entityid, config.startdate, config.enddate);
    }
    Wtf.getCmp('as').setActiveTab(GSTSummaryDetailspanel);
    Wtf.getCmp('as').doLayout();
}
Wtf.account.GSTR3BDetails = function (config) {
    this.arr = [];
    Wtf.apply(this, config);
    /*
     * Create Grid 
     */
    this.createGridGST();
    /*
     * Create Tool Bar Buttons
     */

    this.createToolBar();
    Wtf.account.GSTR3BDetails.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GSTR3BDetails, Wtf.Panel, {
    setSection: function (section, entity, entityid, startdate, enddate) {
        this.section = section;
        this.entity = entity;
        this.entityid = entityid;
        /**
         * Update section name after opended new section.
         */
        if(this.section != undefined && this.section != ''){
            this.TypeCombo.setValue(this.section);
        }
        /**
         * Set Date if already open any details setion and user changed summary panel dates and open another section 
         */
        if (startdate != undefined && startdate != '') {
            this.startDateObj.setValue(new Date(startdate));
        }
        if (enddate != undefined && enddate != '') {
            this.endDateObj.setValue(new Date(enddate));
        }
        this.FetchStatement();

    },
    onRender: function (config) {
        /*
         * create panel to show grid
         */
        this.createPanelDetails();
        this.add(this.newpan);
        /*
         * fetch data in report
         */
        if (!this.section) {
            this.FetchStatement();
        }
        Wtf.account.GSTR3BDetails.superclass.onRender.call(this, config);
    },
    createPanelDetails: function () {
        this.newpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.tbarbtnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        }),
                        items: this.bbarBttnArr
                    })
                }]
        });

    },
    createToolBar: function () {
        this.tbarbtnArr = [];
        this.bbarBttnArr = [];

        //*********** Component for quick search********************

        this.quickPanelSearch = new Wtf.form.TextField({
            emptyText: WtfGlobal.getLocaleText("acc.GSTR3B.quickSearch3B"), // To search, enter 'Invoice Number or Customer Name' and click on 'Fetch' ,
            width: 220,
            listeners: {     
                render : function (c) {
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: WtfGlobal.getLocaleText("acc.GSTR3B.quickSearch3B")
                    })
                }
            }
        });
                    
               
        if (this.isComputation == false) {
            this.tbarbtnArr.push('-', this.quickPanelSearch);
        }
        this.exportBtn = new Wtf.exportButton({
            obj: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),
            filename: this.isComputation ? WtfGlobal.getLocaleText("acc.report.detailedgstcomputation.title") : WtfGlobal.getLocaleText("acc.report.detailedgstr3b.title") + "_v1",
            menuItem: {
                xls: true
            },
            params: {// Added Params to export Detail view data for GST Computation
                isDetailSectionExport : this.isComputation ? true : false
            },
            label: this.isComputation ? WtfGlobal.getLocaleText("acc.report.detailedgstcomputation.title") : WtfGlobal.getLocaleText("acc.report.detailedgstr3b.title"),
            get: this.isComputation ? Wtf.autoNum.GSTRComputationDetailReport : Wtf.autoNum.GSTR3BDetailReport
        });
        this.bbarBttnArr.push('-', this.exportBtn);
        /**
         * Added Start and End Date fields
         */
        this.startDateObj = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: (this.startdate !=undefined && this.startdate != '')? new Date(this.startdate) : WtfGlobal.getDates(true)
        });
        this.endDateObj = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: (this.enddate !=undefined && this.enddate != '')? new Date(this.enddate) : WtfGlobal.getDates(false)
        });
        /**
         * Added Start and end date in toolbar
         */
        this.tbarbtnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDateObj);
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDateObj);  
        
        /**
         * Section combo box for detail view
         */
        this.typeComboRec = Wtf.data.Record.create([
            { 
                name: 'typeofinvoice' 
            }
        ]);
        this.TypeComboStore = new Wtf.data.Store({
            url: this.isComputation?"AccEntityGST/getGSTRComputationReportSectionNames.do":"AccEntityGST/getGSTR3BSummaryReportSectionNames.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.typeComboRec)
        });
        this.TypeCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("Type") + " *",
            hiddenName: 'id',
            name: 'id',
            store: this.TypeComboStore,
            valueField: 'typeofinvoice',
            displayField: 'typeofinvoice',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("Please Select Type"),
            width: 220,
            listWidth: 220,
            extraFields:[],
        });
        /**
         * On section name change set section variable value
         */
        this.TypeCombo.on('select',function(){
            if (this.TypeCombo.getValue() != "") {
                this.section = this.TypeCombo.getValue();
            }
        },this);
        this.TypeComboStore.on('load', function() {
            this.TypeCombo.setValue(this.section);
        }, this);
        this.TypeComboStore.load({
            params:{
                isforstore:true
            }
        });
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.field.TransactionType")+":");
        this.tbarbtnArr.push('-', this.TypeCombo);
        
        //********* From Transaction type **************************

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
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.field.Entity") + "*" + ":");
        this.tbarbtnArr.push('-', this.EntityCombo);
        this.EntityComboStore.on('load', function () {
            this.EntityCombo.setValue(this.entityid);
        }, this);

        var data = [[3, "All"], [0, "Sales/Purchase Invoice"], [1, "Credit/Debit Note"], [2, "Payment/Receipt"]];
        this.delTypeStore = new Wtf.data.SimpleStore({
            fields: [{name: 'typeid', type: 'int'}, 'name'],
            data: data
        });

        this.typeEditor = new Wtf.form.ComboBox({
            store: this.delTypeStore,
            name: 'typeid',
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            value: 3,
            width: 150,
            listWidth: 150,
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true
        });
        if (this.isComputation == false) {
            this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.field.TransactionType"), this.typeEditor);
        }
        
        this.FetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function () {
                this.FetchStatement()
            }
        });
        this.tbarbtnArr.push('-', this.FetchBttn);
//        this.viewBttn.on('click', this.View, this);


   },
    createGridGST: function () {
        this.Store = new Wtf.data.Store({
            url: this.isComputation?"AccEntityGST/getGSTComputationDetailReport.do":"AccEntityGST/getGSTR3BSummaryDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
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
            }
        });
         this.grid.on('render',function(){
            this.grid.getView().refresh();
        },this);
//        this.grid.on('cellclick', this.onCellClick, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
    },
    handleStoreBeforeLoad: function (Store1) {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDateObj.getValue());
        currentBaseParams.enddate= WtfGlobal.convertToGenericEndDate(this.endDateObj.getValue());
        currentBaseParams.section = this.section;
        currentBaseParams.entity = this.EntityCombo.getValue() != undefined ? this.EntityCombo.getRawValue() : this.entity;
        currentBaseParams.entityid = this.EntityCombo.getValue() != undefined ? this.EntityCombo.getValue() : this.entityid;
        currentBaseParams.transactionType = this.typeEditor.getValue();
        currentBaseParams.ss=this.quickPanelSearch.getValue()!=undefined?this.quickPanelSearch.getValue():this.ss;
        currentBaseParams.isShipping = this.isShipping; // Flag for identification whether GST calculation on Billing Address or Shipping Address
        currentBaseParams.isAddressNotFromVendorMaster = (WtfGlobal.isIndiaCountryAndGSTApplied() && !Wtf.account.companyAccountPref.isAddressFromVendorMaster)? true : false;
        this.Store.baseParams = currentBaseParams;
    },
    FetchStatement: function () {
        WtfGlobal.setAjaxTimeOut();
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                startdate : WtfGlobal.convertToGenericStartDate(this.startDateObj.getValue()),
                enddate : WtfGlobal.convertToGenericEndDate(this.endDateObj.getValue())
            }
        });
    },
    handleStoreOnLoad: function (store) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            else {
                column.renderer = WtfGlobal.deletedRenderer;
            }
            if (column.summaryRenderer) {
                column.summaryRenderer = eval('(' + column.summaryRenderer + ')');
            }

            columns.push(column);
        });
        try {
            this.grid.getColumnModel().setConfig(columns);
            this.grid.getView().refresh();
        } catch (e) {
            clog(e);
        }
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }

    }
});

