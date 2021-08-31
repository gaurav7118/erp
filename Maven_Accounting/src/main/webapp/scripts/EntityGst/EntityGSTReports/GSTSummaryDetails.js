/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function callGSTR1SummaryDetails(config) {
    
    var winid = 'GSTR1SummaryDetails'+config.entity+config.gstrreporttype;
    var title=config.gstrreporttype==1?"GSTR1-":config.gstrreporttype==3?"MisMatch":"GSTR2-";
    var GSTSummaryDetailspanel = Wtf.getCmp(winid);
    if (GSTSummaryDetailspanel == null) {
        GSTSummaryDetailspanel = new Wtf.account.GSTSummaryDetails({
            layout: "fit",
            title: title+config.entity,
            tabTip: title+config.entity,
            section: config.section,
            border: false,
            closable: true,
            entity:config.entity,
            entityid:config.entityid,
            stdate:config.startDate,
            EndDate:config.endDate,
            id: winid,
            gstrreporttype:config.gstrreporttype,
            isShipping:config.isShipping!= undefined?config.isShipping:false // Flag for identification whether GST calculation on Billing Address or Shipping Address

        });
           Wtf.getCmp('as').add(GSTSummaryDetailspanel);
         Wtf.getCmp('as').setActiveTab(GSTSummaryDetailspanel);
    } else 
        GSTSummaryDetailspanel.setSection(config.section,config.entity,config.entityid,config.startDate,config.endDate);
    
    
        Wtf.getCmp('as').setActiveTab(GSTSummaryDetailspanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.GSTSummaryDetails = function (config) {
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
    Wtf.account.GSTSummaryDetails.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GSTSummaryDetails, Wtf.Panel, {
    setSection : function(section,entity,entityid){
        this.section = section; 
        this.prevSection=section;
        this.entity = entity;
        this.entityid=entityid;
        this.FetchStatement();
        this.TypeCombo.setValue(section);
        this.ss='';
        
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
        Wtf.account.GSTSummaryDetails.superclass.onRender.call(this, config);
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
                    items: [this.grid1],
                    tbar: this.tbarbtnArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store1,
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
       
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: (this.section == Wtf.gstSection.HSNSummary)?WtfGlobal.getLocaleText("acc.gstr1.quickSearchHSN"):WtfGlobal.getLocaleText("acc.gstr1.quickSearch"), //"Search by Invoice Number,Customer Name",
            width: 180,
            Store:this.Store1
        });
        // Quick search is available for GSTR1 and GSTR MisMatch report
        if (this.gstrreporttype === 1 || this.gstrreporttype == 3) {
            this.tbarbtnArr.push('-', this.quickPanelSearch);
        }
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.stdate
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.EndDate
        });
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
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
            width: 200,
            listWidth: 200
        });
        this.EntityComboStore.load();
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.field.Entity") + "*" + ":");
        this.tbarbtnArr.push('-', this.EntityCombo);
        this.EntityComboStore.on('load', function() {
            this.EntityCombo.setValue(this.entityid);
        }, this);
        
        this.typeComboRec = Wtf.data.Record.create([
            {name: 'typeofinvoice'}
        ]);

        this.TypeComboStore = new Wtf.data.Store({
            url: this.gstrreporttype === 1 ? "AccEntityGST/getGSTR1Summary.do" : this.gstrreporttype ==3 ? "AccEntityGST/getGSTRMisMatchSummary.do" : "AccEntityGST/getGSTR2Summary.do",
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
        this.TypeComboStore.on('load', function() {
        this.setSection(this.section, this.entity,this.entityid);
        }, this);
        this.TypeComboStore.load({
            params:{
                isforstore:true
            }
        });
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.field.TransactionType")+":");
        this.tbarbtnArr.push('-', this.TypeCombo);
        
        this.StateComboRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);

        this.stateComboStore = new Wtf.data.Store({
            url: "AccEntityGST/getFieldComboDataForModule.do",
            baseParams: {
                moduleid: Wtf.Acc_EntityGST,
                fieldlable: "State"
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.StateComboRec)
        });

        this.StateCombo = new Wtf.form.ComboBox({
            hiddenName: 'id',
            name: 'id',
            store: this.stateComboStore,
            valueField: 'name',
            displayField: 'name',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectstate"),
            width: 220,
            listWidth: 220
        });
        this.stateComboStore.load();
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.gstr1.placeofsupply")+ ":");
        this.tbarbtnArr.push('-', this.StateCombo);
        
        this.FetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function(){
                this.section=this.TypeCombo.getRawValue(),
                this.quickPanelSearch.emptyText=(this.TypeCombo.getRawValue()== Wtf.gstSection.HSNSummary)?WtfGlobal.getLocaleText("acc.gstr1.quickSearchHSN"):WtfGlobal.getLocaleText("acc.gstr1.quickSearch"),
                this.quickPanelSearch.reset();
                this.FetchStatement()
            }
        });
        this.tbarbtnArr.push('-', this.FetchBttn);
        this.ResetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.tbarbtnArr.push('-', this.ResetBttn);
        this.ResetBttn.on('click', this.handleResetClickNew, this);
//        this.viewBttn.on('click', this.View, this);
 

    }, 
   
    createGridGST: function () {
        this.Store1 = this.gstrreporttype == 1 ||this.gstrreporttype == 2 ? new Wtf.data.GroupingStore({
            url: this.gstrreporttype == 1 ? "AccEntityGST/getGSTR1SummaryDetails.do" : this.gstrreporttype == 3 ? "AccEntityGST/getGSTRMisMatchSummaryDetails.do" : "AccEntityGST/getGSTR2SummaryDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }),
            sortInfo: {field: 'placeofsupply', direction: "DESC"}
        }) : new Wtf.data.Store({
            url: this.gstrreporttype == 1 ? "AccEntityGST/getGSTR1SummaryDetails.do" : this.gstrreporttype == 3 ? "AccEntityGST/getGSTRMisMatchSummaryDetails.do" : "AccEntityGST/getGSTR2SummaryDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });  
        this.gridView1 = new Wtf.grid.GroupingView({
        forceFit:true,
//        showGroupName: false,
//        enableNoGroups:true, // REQUIRED!
//        hideGroupedColumn: true,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
            getRowClass: function(record, index) {
                    return 'grey-background'
                } 
    });

    this.grpSummary = new Wtf.grid.GroupSummary({});
    this.gridSummary = new Wtf.ux.grid.GridSummary();
        this.grid1 = this.gstrreporttype == 1 || this.gstrreporttype == 2? new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store1,
            columns: ['placeofsupply'],
            border: false,
            loadMask: true,
            view: this.gridView1,
//            viewConfig: this.viewConfig,
            plugins: [this.grpSummary]
//            viewConfig: {
//                getRowClass: function(record, index) {
//                    return 'header-background'
//                } 
//            }
        }) : this.grid1 = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store1,
            columns: [],
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.grid1.on('cellclick', this.onCellClick, this);
        this.Store1.on('load', this.handleStoreOnLoad, this);
        this.Store1.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store1.on('loadexception',WtfGlobal.resetAjaxTimeOut(),this);
    },
    handleStoreBeforeLoad: function (Store1) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        if (this.gstrreporttype == 1|| this.gstrreporttype == 2) {
            this.Store1.groupBy(undefined);
        }
        var currentBaseParams = this.Store1.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.section = this.section;
        currentBaseParams.statesearch = this.StateCombo.getValue();
        currentBaseParams.entity= this.EntityCombo.getValue()!=undefined?this.EntityCombo. getRawValue():this.entity;
        currentBaseParams.entityid= this.EntityCombo.getValue()!=undefined?this.EntityCombo.getValue():this.entityid;
        currentBaseParams.isShipping = this.isShipping;
        currentBaseParams.ss=this.quickPanelSearch.getValue()!=undefined?this.quickPanelSearch.getValue():this.ss;
        currentBaseParams.isAddressNotFromVendorMaster = (WtfGlobal.isIndiaCountryAndGSTApplied() && !Wtf.account.companyAccountPref.isAddressFromVendorMaster)? true : false;
        this.Store1.baseParams = currentBaseParams;
    },
    FetchStatement: function () {
        this.Store1.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
 
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
    },
    handleStoreOnLoad: function (store) {
        WtfGlobal.resetAjaxTimeOut();
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
      
        Wtf.each(this.Store1.reader.jsonData.columns, function (column) {

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
            this.grid1.getColumnModel().setConfig(columns);
            this.grid1.getView().refresh();
        } catch (e) {
            clog(e);
        }
        if (this.gstrreporttype == 1) {
            if (this.section == Wtf.gstSection.B2B || this.section == Wtf.gstSection.B2CL || this.section == Wtf.gstSection.CDNR ||
                    this.section == Wtf.gstSection.CDNUR || this.section == Wtf.gstSection.EXPORT) {
                this.Store1.sortInfo = {
                    field: 'gstin',
                    direction: "ASC"
                };
                this.Store1.groupBy("gstin");
//            this.grid1.plugins=[this.gridSummary]
            } else if (this.section == Wtf.gstSection.AT || this.section == Wtf.gstSection.ATADJ || this.section == Wtf.gstSection.B2CS) {
                if (columns.length > 1) {
                    this.Store1.sortInfo = {
                        field: 'placeofsupply',
                        direction: "ASC"
                    };
                    this.Store1.groupBy("placeofsupply");
                }
            }else if (this.section == Wtf.gstSection.EXEMPT) {
               this.Store1.sortInfo = {
                    field: 'taxclasstype',
                    direction: "ASC"
                };
                this.Store1.groupBy("taxclasstype");
            }else if (this.section == Wtf.gstSection.HSNSummary) {
               this.Store1.sortInfo = {
                    field: 'hsnno',
                    direction: "ASC"
                };
                this.Store1.groupBy("hsnno");
            }else if (this.section == Wtf.gstSection.DOCS){
                     this.Store1.sortInfo = {
                        field: 'companyname',
                        direction: "ASC"
                    };
                this.Store1.groupBy("companyname");
            }


        } else if (this.gstrreporttype == 2) {
            if (this.section == Wtf.gstr2Section.GSTR2_B2B || this.section == Wtf.gstr2Section.GSTR2_ImpGoods || this.section == Wtf.gstr2Section.GSTR2_CDN || this.section == Wtf.gstr2Section.GSTR2_nilRated || this.section == Wtf.gstr2Section.GSTR2_AdvanceAdjust) {
                this.Store1.sortInfo = {
                    field: 'gstin',
                    direction: "ASC"
                };
                this.Store1.groupBy("gstin");
//            this.grid1.plugins=[this.gridSummary]

            } else if (this.section == Wtf.gstr2Section.GSTR2_B2B_unregister || this.section == Wtf.gstr2Section.GSTR2_ImpServices || this.section == Wtf.gstr2Section.GSTR2_AdvancePaid) {
                if (columns.length > 1) {
                    this.Store1.sortInfo = {
                        field: 'placeofsupply',
                        direction: "ASC"
                    };
                    this.Store1.groupBy("placeofsupply");
                }
            } else if (this.section == Wtf.gstr2Section.GSTR2_CDN_unregister) {
                    if (columns.length > 1) {
                        this.Store1.sortInfo = {
                            field: 'supplierType',
                            direction: "ASC"
                        };
                        this.Store1.groupBy("supplierType");
                    }
                }
      }

        if (this.Store1.getCount() < 1) {
            this.grid1.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid1.getView().refresh();
        }
      
    },
    handleResetClickNew: function ()
    {
        this.StateCombo.reset();
        this.startDate.setValue(this.stdate);
        this.endDate.setValue(this.EndDate);
        this.EntityCombo.setValue(this.entityid);
        this.TypeCombo.setValue(this.prevSection);
        this.section=this.TypeCombo.getRawValue(),
        this.quickPanelSearch.emptyText = (this.TypeCombo.getRawValue() == Wtf.gstSection.HSNSummary) ? WtfGlobal.getLocaleText("acc.gstr1.quickSearchHSN") : WtfGlobal.getLocaleText("acc.gstr1.quickSearch"),
        this.quickPanelSearch.reset();
        this.Store1.load({
            params: {
                start: 0,
                limit: 30,
                section: this.section,
                entity: this.entity,
                entityid:this.entityid,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
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
    }

});

