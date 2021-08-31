/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


function callLandingCostItemReportDynamicLoad() {
    var panel = Wtf.getCmp("landingCostItemReport");
    if (panel == null) {
        panel = new Wtf.account.LandingCostItemReport({
            title: "Product Landed Cost Report",
            tabTip: "Product Landed Cost Report",
            id: "landingCostItemReport",
            iconCls:'accountingbase coa',
            layout: 'fit',
            //            moduleid : Wtf.Acc_Receive_Payment_ModuleId,
            closable: true,
            border: false
        });
    }
    Wtf.getCmp('as').add(panel);
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
    return panel;
}
Wtf.account.LandingCostItemReport = function (config) {
    Wtf.account.LandingCostItemReport.superclass.constructor.call(this, config);
     Wtf.Ajax.requestEx({            // request to fetch json of particular report, by ID
        url: "ACCGoodsReceipt/createLandingCostItemConfig.do",
        params: {
            reportId: this.repId
        }
    },this,this.successCallback,this.failureCallback);
}
Wtf.extend(Wtf.account.LandingCostItemReport, Wtf.Panel, {
    initComponent: function () {
        Wtf.account.LandingCostItemReport.superclass.initComponent.call(this);
//        this.getMasterItemGrid();
        
    },
    onRender:function(config){
        Wtf.account.LandingCostItemReport.superclass.onRender.call(this, config);
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
    successCallback:function(resp){
        this.obj = resp.data;
        this.MasterItemRec = new Wtf.data.Record.create(this.createFieldsForGrouping(this.obj.rec));
        this.MasterItemColumn = new Wtf.grid.ColumnModel(this.createColModel(this.obj.columnModel));        
//        this.MasterItemColumn = new Wtf.grid.ColumnModel(this.obj.columnModel);        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.rem.5") + "Nature Of Payment",
            width: 150,
            id: "quickSearch" + this.id,
            field: 'natureofpayment',
            Store: this.MasterItemStore
        });
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);

        this.tbararray = new Array();
        this.fetchButton = new Wtf.Toolbar.Button({
            text: 'Fetch',
            scope: this,
            tooltip: 'Fetch Report',
            handler: this.fetchReportHandler,
            iconCls: 'accountingbase fetch'
        });
        
        this.startDate = new Wtf.form.DateField({
            fieldLabel: "Start Date",
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true),
            anchor: '60%',
            name: "startdate",
            allowBlank: false
        });

        this.endDate = new Wtf.form.DateField({
            fieldLabel: "End Date",
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
        
        this.expButton = new Wtf.exportButton({
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details.',
        filename: "Product Landed Cost Report",
        get: Wtf.autoNum.landedcostreport,        
        menuItem: {
            csv: false,
            pdf: false,
            rowPdf: false,
            xls: true
        },
        params: {
            enddate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            startdate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            get: Wtf.autoNum.landedcostreport
        },        
        label: WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
    
    this.tbararray.push("From ", this.startDate, "To ", this.endDate, this.fetchButton);
    this.tbararray.push("-");
    this.tbararray.push(this.expButton);
//        this.tbararray.push(this.quickPanelSearch, this.resetBttn);
        this.MasterItemReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'totalCount'
        }, this.MasterItemRec);

        this.MasterItemStore = new Wtf.data.GroupingStore({
            url: "ACCGoodsReceipt/getLandingCostItemReport.do",
            reader: this.MasterItemReader,
            sortInfo: {
                field: 'purchaseinvoice',
                direction: 'ASC'
            },
            groupField : 'purchaseinvoice',
            baseParams: {
                startdate :WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())            
            }
        });
        this.MasterItemStore.on("beforeload", function () {
            var currentBaseParams = this.MasterItemStore.baseParams;
            currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
            currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
            this.MasterItemStore.baseParams = currentBaseParams;
        }, this);
        this.gridView1 = new Wtf.grid.GroupingView({
            forceFit:true,
            showGroupName: true,
            enableNoGroups:true, // REQUIRED!
            hideGroupedColumn: false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        });     
        this.grid = new Wtf.grid.GridPanel({
            cls: 'vline-on',
            store: this.MasterItemStore,
            autoScroll: true,
            border: false,
            layout: 'fit',
            split: true,
            sm: this.sm,
            cm: this.MasterItemColumn,
            viewConfig: this.gridView1
        });
        this.gridContainer = new Wtf.Panel({
            region: "center",
            layout: 'fit',
            autoScroll: true,
            border: false,
            bodyStyle: {
                "background-color": 'white'
            },
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
                plugins: this.pP = new Wtf.common.pPageSize({
                    id: "pPageSize_" + this.id
                }),
                items: this.bottombtnArr
            })
        });
        this.storeLoad({params:{
                start:0,
                limit:30
                
        }});
        this.mainPanel = new Wtf.Panel({
            layout: "border",
            border: false,
            items: [
            this.gridContainer
            ]
        });
        this.add(this.mainPanel);
        this.doLayout();
        
        
    },createFieldsForGrouping:function(data){
        var fields = [];
        for(var fieldcnt = 0; fieldcnt < data.length; fieldcnt++) {
            var fObj = {
                'name':data[fieldcnt].dataIndex
            };
            fields.push(fObj);
        }
        return fields;
    },
    createColModel:function(data){
        var colConfig = [];        
        for(var columncnt=0; columncnt<data.length ;columncnt++) {
            var colObj = {};
            colObj['header'] = '<div wtf:qtip="'+data[columncnt].header+'">'+data[columncnt].header+'</div>';
            colObj['dataIndex'] = data[columncnt].dataIndex;
            colObj['width'] = data[columncnt].width;
            colObj['pdfwidth'] = data[columncnt].pdfwidth;// To make available columns in Export Window
            if(data[columncnt].renderer=="WtfGlobal.withoutRateCurrencyDeletedSymbol"){
                colObj['renderer'] = WtfGlobal.withoutRateCurrencyDeletedSymbol;
            }
            //SDP-15731 providing linked expense invoice numbers against each Purchase Invoice
            else if(data[columncnt].renderer=="expenseinvoice"){
                colObj['renderer'] = function (v) {
                    return '<div wtf:qtip="' + v + '">' + v + '</span>';
                };
            }
            colConfig[colConfig.length] = colObj;
        }
         
        return colConfig;
    }
});

