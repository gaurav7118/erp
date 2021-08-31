/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.RejectedItemListReport = function (config) {
    Wtf.apply(this, config);
    this.arr = [];
    this.creategrid();
    this.createTBar();


    Wtf.RejectedItemListReport.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.RejectedItemListReport, Wtf.Panel, {
    onRender: function (config) {
        Wtf.RejectedItemListReport.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.jobOrderpanel);
        this.fetchStatement();
        
        //ERP-35176 : Start Date Validation
        this.startDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.endDate.getValue()!=''){
                if(field.getValue().getTime()>this.endDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    field.setValue(oldval);                    
                }
            }
        },this);  
        //ERP-35176 : End Date Validation
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
        this.jobOrderpanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [ {
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
            emptyText: WtfGlobal.getLocaleText("acc.rejecteditemreport.quicksearch.searchText"), //ERP-35176 : Search by Product Name,Product ID, WO ID, WO Name
            field: 'productid',
            width: 200,
            Store: this.Store
        })
        this.buttonsArr.push(this.quickPanelSearchh);
        
        //ERP-35176 : From and To Date Filter
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


       


        this.expButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.agedPay.exportTT"), 
            filename: WtfGlobal.getLocaleText("mrp.rejjecteditemlistreport.exportTT"),
            params: {
                ss: this.quickPanelSearchh.getValue() != undefined ? this.quickPanelSearchh.getValue() : ''
            },
            menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
            get: Wtf.autoNum.exportmrprejecteditemlist
        });
        this.bottomToolbar.push(this.expButton)

    },
    creategrid: function () {

        this.Store = new Wtf.data.Store({
            url: "ACCWorkOrder/getRejectedItemsList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });

        this.sm = new Wtf.grid.CheckboxSelectionModel({});

        this.sm.on('selectionchange', this.enableDisableButtons, this);

        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            columns: [],
            border: false,
            stripeRows :true,
            loadMask: true,
            sm: this.sm,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
        this.Store.on('load', this.handleStoreOnLoad, this);

     


    },
    fetchStatement: function () {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                startdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                ss: this.quickPanelSearchh.getValue() != undefined ? this.quickPanelSearchh.getValue() : ''
            }
        });
    },
    
 
    handleStoreBeforeLoad: function () {
        var currentBaseParams = this.Store.baseParams;
       
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
    handleResetClick: function () {
     
        if (this.quickPanelSearchh.getValue()) {
            this.quickPanelSearchh.reset();
        } else {
            this.fetchStatement();
        }

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
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: 3,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
      
        this.doLayout();


    }
});


//****************************graphs to be opened from reports Node*******************************

Wtf.productWiseComponentAvailability = function (config) {
    Wtf.apply(this, config);
    Wtf.productWiseComponentAvailability.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.productWiseComponentAvailability, Wtf.Panel, {
    onRender: function (config) {
        Wtf.productWiseComponentAvailability.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.northPanel);
    },
    createPanel: function () {
         this.northPanel = new Wtf.Panel({
            region:"center",
            height:700,
            weidth:500,
            autoScroll:true,
            border:false,
            html: "<div style='height:700px;'  id=mrpwostatus ><img  src=../../images/Product-wise.png alt='' /></div>"
        });
    }
});



Wtf.workOrderWiseComponentAvailability = function (config) {
    Wtf.apply(this, config);
    Wtf.workOrderWiseComponentAvailability.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.workOrderWiseComponentAvailability, Wtf.Panel, {
    onRender: function (config) {
        Wtf.workOrderWiseComponentAvailability.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.northPanel);
    },
    createPanel: function () {
         this.northPanel = new Wtf.Panel({
            region:"center",
            height:700,
            weidth:500,
            autoScroll:true,
            border:false,
             html: "<div style='height:700px;'  id=mrpwostatus ><img  src=../../images/Work-Order-wise.png alt='' /></div>"
        });
    }
});


Wtf.workorderTasksStatusReportNavigation = function (config) {
    Wtf.apply(this, config);
    Wtf.workorderTasksStatusReportNavigation.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.workorderTasksStatusReportNavigation, Wtf.Panel, {
    onRender: function (config) {
        Wtf.workorderTasksStatusReportNavigation.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.northPanel);
    },
    createPanel: function () {
         this.northPanel = new Wtf.Panel({
            region:"center",
            height:700,
            autoScroll:true,
            weidth:500,
            border:false,
             html: "<div style='height:700px;'  id=mrpwostatus ><img  src=../../images/Task-Status-Report_mod.png alt='' /></div>"
        });
    }
});




Wtf.workorderstatusReportgraphviewmod = function (config) {
    Wtf.apply(this, config);
    Wtf.workorderstatusReportgraphviewmod.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.workorderstatusReportgraphviewmod, Wtf.Panel, {
    onRender: function (config) {
        Wtf.workorderstatusReportgraphviewmod.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.northPanel);
    },
    createPanel: function () {
         this.northPanel = new Wtf.Panel({
            region:"center",
            height:700,
            weidth:500,
            autoScroll:true,
            border:false,
             html: "<div style='height:700px;'  id=mrpwostatus ><img  src=../../images/Work-Order-Status-Report_mod.png alt='' /></div>"
        });
    }
});

Wtf.vendorInfoReportGraph = function (config) {
    Wtf.apply(this, config);
    Wtf.vendorInfoReportGraph.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.vendorInfoReportGraph, Wtf.Panel, {
    onRender: function (config) {
        Wtf.vendorInfoReportGraph.superclass.onRender.call(this, config);

        this.createPanel();

        this.add(this.northPanel);
    },
    createPanel: function () {
         this.northPanel = new Wtf.Panel({
            region:"center",
            height:700,
            weidth:500,
             autoScroll:true,
            border:false,
             html: "<div style='height:700px;'  id=mrpwostatus ><img  src=../../images/vendor-information.png alt='' /></div>"
        });
    }
});
