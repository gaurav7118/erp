/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.WOShortfallReport = function (config) {
    Wtf.apply(this, config);
    this.arr = [];
    this.creategrid();
    this.createTBar();

    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: 4,
        advSearch: false
    });

    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    Wtf.WOShortfallReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.WOShortfallReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.WOShortfallReport.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.panel);
        this.fetchStatement();
        
        //ERP-30730 : Start Date Validation
        this.startDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.endDate.getValue()!=''){
                if(field.getValue().getTime()>this.endDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    field.setValue(oldval);                    
                }
            }
        },this);  
        //ERP-30730 : End Date Validation
        this.endDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.startDate.getValue()!=''){
                if(field.getValue().getTime()<this.startDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                    field.setValue(oldval);
                }
            }
        },this);
    },
    createPanel: function () {
        this.panel = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent, {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.buttonsArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearchh,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                        items: [this.bottomToolbar]
                    })
                }]
        });
    },
    createTBar: function () {
        this.buttonsArr = [];
        this.bottomToolbar = [];
        this.quickPanelSearchh = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.productList.searchText"),
            field: 'productId',
            width: 200,
            Store: this.Store
        })
        this.buttonsArr.push(this.quickPanelSearchh);

        this.buttonsArr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'stdate' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        })
        this.buttonsArr.push(this.startDate);

        this.buttonsArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate' + this.id,
            value: WtfGlobal.getDates(false)
        })
        this.buttonsArr.push(this.endDate);
                
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), 
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"),
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        })

        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), 
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), 
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        })
        this.resetBttn.on('click', this.handleResetClick, this);
        
        this.buttonsArr.push(this.resetBttn);
        this.buttonsArr.push(this.fetchBttn);


        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), 
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), 
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        })
        this.buttonsArr.push(this.AdvanceSearchBtn);

//        this.customReportViewBtn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
//            scope: this,
//            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
//            handler: this.customizeView,
//            iconCls: 'accountingbase fetch'
//        });
//        this.buttonsArr.push('->', this.customReportViewBtn);

        this.expButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.agedPay.exportTT"), 
            filename: WtfGlobal.getLocaleText("mrp.qualitycontrolreport.registerreport.tab.title")+"_v1",
            params: {stdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                ss: this.quickPanelSearchh.getValue() != undefined ? this.quickPanelSearchh.getValue() : ''
            },
            menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
            get: Wtf.autoNum.mrpqcreport
        });
        this.genPOPRBtn= new Wtf.Toolbar.Button({
            text: Wtf.account.companyAccountPref.autoGenPurchaseType == 0? "Generate Purchase Order" : "Generate Purchase Requsition",
            tooltip: Wtf.account.companyAccountPref.autoGenPurchaseType == 0? "Generate Purchase Order" : "Generate Purchase Requsition",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            disabled:true,
            handler: this.createPOfromShortFall
        });
        this.bottomToolbar.push(this.genPOPRBtn);

    },
  
//    customizeView: function () {
//        this.customizeViewWin = new Wtf.CustomizeReportView({
//            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
//            parentPanel: this,
//            iconCls: getButtonIconCls(Wtf.etype.deskera),
//            grid: this.grid,
//            reportId: Wtf.MRP_Job_Work_ModuleID,
//            modules: "" + Wtf.MRP_Job_Work_ModuleID
//        });
//        this.customizeViewWin.show();
//    },
    
    creategrid: function () {
        
        this.expandRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'woid'},
            {name: 'woname'},
            {name: 'quantity'},
        ]);
        this.expandStore = new Wtf.data.Store({
            url:"ACCWorkOrder/getShortFallProductsReport.do",
            baseParams:{
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.expandRec)
        });
        this.expandStore.on('load',this.fillExpanderBody,this);
        this.Store = new Wtf.data.Store({
            url: "ACCWorkOrder/getShortFallProductsReport.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });

        this.sm = new Wtf.grid.CheckboxSelectionModel({});

        this.sm.on('selectionchange', this.enableDisableButtons, this);
        this.expander = new Wtf.grid.RowExpander({});
        this.expander.on("expand",this.onRowexpand,this);
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            columns: [],
            border: false,
            stripeRows :true,
            loadMask: true,
            plugins:[this.expander],
            sm: this.sm,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);

        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: this.isOrder ? Wtf.Acc_Sales_Order_ModuleId : Wtf.Acc_Invoice_ModuleId,
            advSearch: false
        });

        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);


    },
    fetchStatement: function () {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                ss:this.quickPanelSearchh.getValue() != undefined ? this.quickPanelSearchh.getValue() : '' // Passed ss parameter on click of fetch button.
            }
        });
    },
    
 
    handleStoreBeforeLoad: function () {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.Store.baseParams = currentBaseParams;

    },
    handleStoreOnLoad: function () {
        var columns = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        columns.push(this.sm);
        columns.push(this.expander);

        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
            columns.push(column);
        },this);
//     
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
    },
    onRowexpand: function(scope, record) {
        this.expandStore.load({
            params:{
                productid:record.data.id,
                isForExpander:true
                }
            });
    },
    handleResetClick: function () {
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));
        if (this.quickPanelSearchh.getValue()) {
            this.quickPanelSearchh.reset();
        } 
        this.fetchStatement();

    },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
    },
    fillExpanderBody: function() {
        var disHtml = "";
        this.custArr = [];
        var previd = "";
        var sameParent = false;
        var sfheader = this.sfheader();
        
        for (var i = 0; i < this.expandStore.getCount(); i++) {
            var header = "";
            var rec = this.expandStore.getAt(i);
            
            var currentid = rec.data['id'];
            if (previd != currentid) {             
                previd = currentid;
                sameParent = false;
            } else {
                sameParent = true;
            }
            
            header = this.getExpanderData(rec, sameParent, sfheader[1], sfheader[2]);
            var moreIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('id') === rec.data['id']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
            if (moreIndex != -1) {
//            if (true) {
                var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                disHtml = "<div class='expanderContainer1'>" + sfheader[0] + header + "</div>";
                body.innerHTML = disHtml;
                if (this.expandButtonClicked) {
                    this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                    this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                }
            }
        }
    },
    getExpanderData: function (rec, sameParent, minWidth, widthInPercent) {
        if (!sameParent) {
            this.Repeatheader = "";
            this.serialNumber = 0;
        }
        this.Repeatheader += "<div style='width: 100%;min-width:" + minWidth + "px'>";
        var woid = rec.data['woid'];
        var woName = rec.data['woname'];
        var quantity = rec.data['quantity'];

        this.Repeatheader += "<span class='gridNo'>" + (++this.serialNumber) + ".</span>";

        //Column : M.data['labourname'];achine Name
        /*
         * Removed hyperlink reason ticket - ERP-35160
         */
        this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + woid + "' style='width: " + widthInPercent + "% ! important;' wtf:qtip='" + woid + "'>" +Wtf.util.Format.ellipsis(woid, 20) + "&nbsp;</span>";


        this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + woName + "' style='width: " + widthInPercent + "% ! important;' wtf:qtip='" + woName + "'>" + Wtf.util.Format.ellipsis(woName, 20) + "&nbsp;</span>";


        this.Repeatheader += "<span class='gridRow'  wtf:qtip='" + quantity + "' style='width: " + widthInPercent + "% ! important;' wtf:qtip='" + quantity + "'>" + Wtf.util.Format.ellipsis(quantity, 10) + "&nbsp;</span>";
        this.Repeatheader += "<br>";
        this.Repeatheader += "</div>";
        return this.Repeatheader;

    },
    sfheader: function () {
        var arr = [];
        var headerArray = [];
        arr=["Work Order ID", "Work Order Name","Quantity","           "];
        var header = "<span class='gridHeader'>Other Details</span>";   //Account List
       
        var arrayLength=arr.length;
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        var widthInPercent=100/count;
        var minWidth = count*100 + 40;
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";  
        headerArray.push(header);
        headerArray.push(minWidth);
        headerArray.push(widthInPercent);
        return headerArray;
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: 3,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },
    createPOfromShortFall:function(){   
        var gridDataArr = this.grid.getSelectionModel().getSelections();
        var savedRec = {}
        savedRec.data = []
        var productidstr = "";
        for (var cnt =0; cnt < gridDataArr.length; cnt++) {//get product id of all select product of shortfall report 
                productidstr += gridDataArr[cnt].data.id+",";
        }
        
        savedRec.data.productidstr =productidstr;
        if (Wtf.account.companyAccountPref.autoGenPurchaseType == 0) {// To create Purchase order
             callEditPurchaseOrder(true,savedRec,undefined,false,this,null,false,undefined,false,false,false,false,false,true);
        } else {// To create Purchase requisition
            callPurchaseReq(false, savedRec,undefined, false,this,false,false,false,true);
        }
        
    },
    enableDisableButtons: function () {
        var selectionModel = this.grid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() > 0) {
            this.genPOPRBtn.enable();
        } 
    }
});
