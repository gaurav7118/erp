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
/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function callGSTRMatchAndReconsile(section) {

    var winid = 'MatchAndReconcile'+ section;
    var GSTSummaryDetailspanel = Wtf.getCmp(winid);

    GSTSummaryDetailspanel = new Wtf.account.GSTRMatchAndReconsile({
        layout: "fit",
        title:"Match And Reconcile",
        tabTip:"Match And Reconcile",
        section : section,
        border: false,
        closable: true,
        id: winid

    });
    Wtf.getCmp('as').add(GSTSummaryDetailspanel);

    Wtf.getCmp('as').setActiveTab(GSTSummaryDetailspanel);
//    Wtf.getCmp('as').doLayout();
}

Wtf.account.GSTRMatchAndReconsile = function (config) {
    this.arr = [];
    Wtf.apply(this, config);
    /*
     * Create Tool Bar Buttons
     */
    this.createRecord();
    this.createStore();
    this.createExpandStore();
    this.createGSTMatchToolBar();
   this.createGSTR2T2Bar();

    
    /*
     * Create Grid 
     */  this.createGridGSTMatch();


    Wtf.account.GSTRMatchAndReconsile.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GSTRMatchAndReconsile, Wtf.Panel, {
    onRender: function (config) {
        /*
         * create panel to show grid
         */
        this.createPanelDetails();
        this.add(this.newpan);
        /*
         * fetch data in report
         */
        this.FetchStatement();
        Wtf.account.GSTRMatchAndReconsile.superclass.onRender.call(this, config);
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
                        //searchField: this.quickPanelSearch,
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
    createGSTMatchToolBar: function () {
        this.tbarbtnArr = [];
        this.bbarBttnArr = [];

//        this.btnArr.push(this.quickPanelSearch);
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(false)
        });
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate);
        this.tbarbtnArr.push('-', WtfGlobal.getLocaleText("acc.common.to"), this.endDate);
        this.FetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.FetchStatement
        });
        this.tbarbtnArr.push('-', this.FetchBttn);
        this.ResetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.Resetbutton),
            disabled: false
        });
        this.tbarbtnArr.push('-', this.ResetBttn);
        this.ResetBttn.on('click', this.handleResetClickNew, this);
        this.invoiceRadio = new Wtf.form.Radio({
            fieldLabel: "Invoice", //Flow Diagram View
            name: 'Invoice',
            checked :Wtf.account.GSTRMatchAndReconsile=='0',
        })
        this.tbarbtnArr.push('-', this.invoiceRadio);
        this.tbarbtnArr.push(WtfGlobal.getLocaleText("acc.field.Invoice"));
        this.invoiceRadio.on('check',this.createGSTR2T2Bar,this);
        

//        this.viewBttn.on('click', this.View, this);
        

    },
    
    createGSTR2T2Bar: function () {
        
        
        this.btnArr = [];
        
        this.subtype = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'remote',
            selectOnFocus: true,
            valueField: 'type',
            displayField: 'type',
            store: this.Store1,
            width: 100,
            scope: this,
            value: '0',
            typeAhead: true,
            forceSelection: true,
            name: 'subtype',
            hidden:false
        });
        
        this.btnArr.push(WtfGlobal.getLocaleText("acc.field.subtype"));
        this.btnArr.push('-', this.subtype);
       
    },
    createStore: function () {
        this.Store1 = new Wtf.data.Store({
            url: "AccEntityGST/getGSTR1SummaryDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
    },
    createRecord: function () {
        this.Record = new Wtf.data.Record.create(
                [{"name": "sno"},
                    {"name": "itemDescription"},
                    {"name": "hsn"},
                    {"name": "qty"},
                    {"name": "rateItem"},
                    {"name": "discount"},
                    {"name": "taxableVal"},
                    {"name": "sgstRate"},
                    {"name": "sgst"},
                    {"name": "cgstRate"},
                    {"name": "cgst"},
                    {"name": "igstRate"},
                    {"name": "igst"},
                    {"name": "total"},
                    {"name": "diffAmt"}
                ]);
    },
    createExpandStore: function () {
        this.expandStore = new Wtf.data.Store({
            url: "AccEntityGST/getGSTRMatchAndReconcile.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.Record)
        });
    },
    createGridGSTMatch: function () {
        this.expander = new Wtf.grid.RowExpander({});
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.grid1 = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store1,
            columns: [],
           tbar:this.btnArr,
            border: false,
            loadMask: true,
            sm: this.sm,
            plugins: this.expander,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.grid1.on('cellclick', this.onCellClick, this);
        this.Store1.on('load', this.handleStoreOnLoad, this);
        this.Store1.on('beforeload', this.handleStoreBeforeLoad, this);
        this.expander.on("expand", this.onRowexpand, this);
        this.expandStore.on('load', this.fillExpanderBody, this);
        
    },
    handleStoreBeforeLoad: function(store) {
        var currentBaseParams = this.Store1.baseParams;
        currentBaseParams.section= this.section;
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
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        columns.push(this.expander);
        columns.push(this.sm);
        Wtf.each(this.Store1.reader.jsonData.columns, function (column) {

            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            else {
                column.renderer = WtfGlobal.deletedRenderer;
            }

            columns.push(column);
        });
        this.grid1.getColumnModel().setConfig(columns);
        this.grid1.getView().refresh();

        if (this.Store1.getCount() < 1) {
            this.grid1.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid1.getView().refresh();
        }
        //  this.quickPanelSearch.StorageChanged(Store1);
    },
    onRowexpand: function (scope, record, body) {

        this.expanderBody = body;
        this.expandStore.load();


    },
    fillExpanderBody: function () {
        if (this.expandStore.getCount() > 0) {
            for (var j = 0; j < this.expandStore.getCount(); j++) {
                var header = "";
                var disHtml = "";
                var arr = [];

                arr = [WtfGlobal.getLocaleText("acc.gstReconcile.sno"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.itemDescription"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.hsn"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.qty"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.rateItem"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.discount"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.taxableVal"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.sgstRate"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.sgst"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.cgstRate"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.cgst"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.igstRate"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.igst"),
                    WtfGlobal.getLocaleText("acc.gstReconcile.total"),
                ];
                var gridHeaderText = WtfGlobal.getLocaleText("acc.aged.amountwithcurrency");
                var header = "<span class='gridHeader'>" + gridHeaderText + "</span>";   //Product List
                var count = 0;
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i] != "") {
                        count++;
                    }
                }
                count++; // from grid no
                var widthInPercent = 100 / count;
                var minWidth = count * 100;
                header += "<div style='width: 100%;min-width:" + minWidth + "px'>";

                for (var arrI = 0; arrI < arr.length; arrI++) {
                    if (arr[arrI] != undefined)
                        header += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + arr[arrI] + "</span>";
                }
                header += "</div><div style='width: 100%;min-width:" + minWidth + "px'><span class='gridLine'></span></div>";
                header += "<div style='width: 100%;min-width:" + minWidth + "px'>";
                for (var storeCount = j; storeCount < j + 1; storeCount++) {
                    var rec = this.expandStore.getAt(storeCount);



                    if (rec.data['sno'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['sno'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['sno'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['itemDescription'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['itemDescription'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['itemDescription'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['hsn'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['hsn'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['hsn'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['qty'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['qty'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['qty'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['rateItem'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['rateItem'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['rateItem'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['discount'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['discount'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['discount'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['taxableVal'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['taxableVal'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['taxableVal'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['sgstRate'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['sgstRate'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['sgstRate'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['sgst'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['sgst'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['sgst'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['cgstRate'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['cgstRate'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['cgstRate'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['cgst'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['cgst'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['cgst'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['igstRate'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['igstRate'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['igstRate'] + "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['igst'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['igst'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['igst']+"<br>"+"<br>"+"<a href=''>Keep Vendor Amt</a>"+ "</span>";
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                    if (rec.data['total'] != undefined) {
                        header += "<span class='gridRow' wtf:qtip='" + rec.data['total'] + "' style='width: " + widthInPercent + "% ! important;'>" + rec.data['total'] +"<br>"+"<font color='red'>"+ rec.data['diffAmt'] +"</font>"+"<br>"+"<a href=''>  Keep My values</a>" +"</span>";
                        
                    } else {
                        header += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'>&nbsp;</span>";
                    }
                  
                    header += "<br>";

                    if (this.expandStore.getCount() == 0) {
                        header = "<span class='gridHeader'>" + gridHeaderText + "</span>";
                        header += "<span class='headerRow'>" + WtfGlobal.getLocaleText("acc.field.Nodatatodisplay") + "</span>"
                    }
                    header += "</div>";
                    disHtml += "<div class='expanderContainer1'>" + header + "</div>";

                    this.expanderBody.innerHTML = disHtml;

                }
            }
        }



    },
    handleResetClickNew: function ()
    {
        // this.quickPanelSearch.reset();
        this.Store1.load({
            params: {
                start: 0,
                limit: 30,
                section: this.section,
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
    },
    onCellClick: function (g, i, j, e) {
        e.stopEvent();
//    var el=e.getTarget("a");
//    if(el==null)return; 
        var header = g.getColumnModel().getDataIndex(j);
        if (header == "section") {
            var formrec = this.grid1.getStore().getAt(i);
            var section = formrec.get("section");
            if (section == "") {
                return;
            }


        }
    }





});


