/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.workOrderStockDetailsReport = function(config) {
    this.arr = [];
    this.moduleId = Wtf.MRP_Work_Order_ModuleID;
    Wtf.apply(this, config);
    
    /*
     * Create Grid 
     */
    this.createGrid();
    /*
     * Create Store 
     */
    this.creatStore();
    /*
     * Create Tool Bar Buttons
     */
    this.createTBar();
    /*
     * Create Panel
     */
    this.createPanel();
    
    Wtf.account.workOrderStockDetailsReport.superclass.constructor.call(this, config);
    
}
Wtf.extend(Wtf.account.workOrderStockDetailsReport, Wtf.Panel, {
    closable:true,
    onRender: function(config) {
        /*
         * create panel to show grid
         */
        
        this.add(this.leadpan);
        /*
         * fetch data in report
         */
        this.fetchStatement();
        Wtf.account.workOrderStockDetailsReport.superclass.onRender.call(this, config);
    },
    createPanel: function() {
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
                tbar: this.toolbarPanel,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        //                            id: "pPageSize_" + this.id
                        }),
                    items: this.bbarBtnArr
                })
            }]
        });
    },
    createTBar: function() {
        this.btnArr = [];        
        this.bbarBtnArr = [];
        this.isCustomer =true;
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("mrp.workorder.report.quicksearch"), // "Search by Work Order ID...",
            width: 200,
            id:"quicksearch"  +this.id,
            hidden: false
        });
        this.btnArr.push(this.quickPanelSearch);
        
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.btnArr.push('-', this.resetBttn);
        this.resetBttn.on('click', this.handleResetClickNew, this);
        
        //Customer
        
        this.CustomizedName = CommonERPComponent.createCustomerMultiselectPagingComboBox(this, this.customerAccStore, {});
        
        this.customerAccStore.on("load", function(store){
            if (this.CustomizedName && this.CustomizedName.el.getValue() == "" && this.customerAccStore.lastOptions.params.start == 0) {
                var storeNewRecord = new this.personRec({
                    accid:'All',
                    accname:'All',
                    acccode:''
                });
                this.CustomizedName.store.insert(0, storeNewRecord);
            }
        },this);

        this.CustomizedName.on('select',function(combo,personRec){
            if(personRec.get('accid')=='All'){
                combo.setValue('All');
            }else if(combo.getValue().indexOf('All')>=0){
                combo.setValue(personRec.get('accid'));
            }
        } , this);
       
        this.btnArr.push('-',this.CustomizedName);

        //Customer END
        
        this.btnArr.push('-',WtfGlobal.getLocaleText("acc.common.from"));
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'stdate' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: WtfGlobal.getDates(true)
        })
        this.btnArr.push(this.startDate);

        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'enddate' + this.id,
            value: WtfGlobal.getDates(false)
        })
        this.btnArr.push(this.endDate);
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch', 
            scope: this,
            handler: this.fetchStatement
        });
        this.btnArr.push('-', this.fetchBttn);
       
        var firsttbar = new Wtf.Toolbar(this.btnArr);


        this.toolbarPanel = new Wtf.Panel({
            border: false,
            items: [firsttbar]
        });
              
        this.exportButton = new Wtf.exportButton({
            obj: this,
            id: "exportReports" + this.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), // 'Export report details',
            filename: "Work Order List" + "_v1",
            scope: this,
            menuItem: {
                csv: true,
                pdf: true,
                xls: true
            },
            get: 1105
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.workorder, Wtf.Perm.workorder.exportwo)) {
            this.bbarBtnArr.push('-', this.exportButton);
        }
        this.exportButton.on("click", function() {
            this.exportButton.setParams({
                type: this.transactionType.getValue(),
                productId: this.productId,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
        }, this);
        //Document Designer print button
        this.singleRowPrint = new Wtf.exportButton({
            obj:this,
            id:"printSingleRecord"+this.id,
            iconCls: 'pwnd printButtonIcon',
            text: WtfGlobal.getLocaleText("acc.rem.236"),
            tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
            disabled :true,
            hidden:this.isSalesCommissionStmt,
            menuItem:{
                rowPrint:(this.isSalesCommissionStmt)?false:true
                },
            get:Wtf.autoNum.exportMRPWorkOrder,
            moduleid:this.moduleId
        });
        //put print button in bar
        this.bbarBtnArr.push('-', this.singleRowPrint);
      
    },
    creatStore: function() {
         this.isCustomer =true;
        this.personRec = new Wtf.data.Record.create ([
        {name: 'accid'},
        {name: 'accname'},
        {name: 'acccode'},
        {name: 'groupname'},
        {name: 'hasAccess'}
        ]);

        this.customerAccStore =  new Wtf.data.Store({
            url:this.isCustomer?"ACCCustomer/getCustomersIdNameForCombo.do":"ACCVendor/getVendorsIdNameForCombo.do",
            baseParams:{    
                deleted:false,
                nondeleted:true,
                combineData:this.isCustomer?1:-1  //Send For Seprate Request
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            },this.personRec)
        });
       
        
        
        
    },
    createGrid: function() {
         this.expandRec = Wtf.data.Record.create ([
        {name:'productid'},
        {name:'productname'},
        {name:'producttype'},
        {name:'requiredquantity'},
        {name:'blockedquantity'},
        {name:'consumptionquantity'},
        {name:'wastagequantity'},
        {name:'recyclequantity'},
        {name:'returnedquantity'},
        {name:'bomtype'},
        {name:'producedquantity'},
        {name:'uom'},
        {name:'unitprice'},
        {name:'totalamount'},
        {name:'id'}

    ]);
        this.expandStore = new Wtf.data.Store({
            url:"ACCWorkOrder/getWorkOrderExpanderDetails.do",
            baseParams:{
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.expandRec)
        });
        this.expandStore.on('load',this.fillExpanderBody,this);
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            });
        this.sm.on('selectionchange',this.enableDisableButtons,this);
        this.Store = new Wtf.data.Store({
            url: "ACCWorkOrder/getWorkOrderDataandColumnModel.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
       
        this.expander = new Wtf.grid.RowExpander({});
        this.expander.on("expand",this.onRowexpand,this);
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.Store,
            columns: [],
            border: false,
            loadMask: true,
            plugins:[this.expander],
            sm: this.sm,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.grid.flag = 0;
        this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
    },

    handleStoreBeforeLoad: function () {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        currentBaseParams.isWOStockDetailsReport=true;
        this.Store.baseParams = currentBaseParams;
    },
    fetchStatement: function() {
        if(this.startDate.getValue()>this.endDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;
        }
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value,
                ss: this.quickPanelSearch.getValue(),
                customerComboValue:this.CustomizedName.getValue()=="All"?"":this.CustomizedName.getValue()
            }
        });
    },
    onRowexpand: function(scope, record) {
        this.expandStore.load({
            params:{
                id:record.data.id,
                isWOStockDetailsReport:true
            }
        });
    },
    handleStoreOnLoad: function() {
        var columns = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            //            singleSelect: true
            });
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        columns.push(this.sm);
        columns.push(this.expander);

        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "entryno") {
                column.renderer = WtfGlobal.linkDeletedRenderer
            }else{
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
        this.quickPanelSearch.StorageChanged(this.Store);
    },
    handleResetClickNew: function (){
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },  
    enableDisableButtons: function () {
        var selectionModel = this.grid.getSelectionModel();
        if (selectionModel.hasSelection() && selectionModel.getCount() == 1) {
            //enable print button after selection
            if (this.singleRowPrint) {
                this.singleRowPrint.enable();
            }
            
        }else if (selectionModel.getCount() == 0) {
            //disable print button after selection
            if (this.singleRowPrint) {
                this.singleRowPrint.disable();
            }
        }
      

    },
    fillExpanderBody:function(){
        
        this.custArr = [];
        
     
        var StockDetailsHeaderArray = "";
        var StockDetailsHeader = "";

        var widthInPercent = 0;
     

        var prevBillid = "";
        var sameParent = false;
    
        for (var i = 0; i < this.expandStore.getCount(); i++) {
            var rec = this.expandStore.getAt(i);


            StockDetailsHeaderArray = this.getStockDetailsHeader(rec);
            StockDetailsHeader = StockDetailsHeaderArray[0];
            widthInPercent = StockDetailsHeaderArray[1];
           
        
            
           
            var currentBillid = rec.data['id'];
            if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                prevBillid = currentBillid;
                sameParent = false;
                var StockDetailsHeaderData = "";
             
                this.StockCount = 1;
                this.invCnt = 1;
            } else {
                sameParent = true;
            }


            StockDetailsHeaderData = this.getStockDetailsHeaderData(rec, sameParent, widthInPercent);

            var moreIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('id') === rec.data['id']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
            if (moreIndex != -1) {
                var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));

                var disHtmlStockDetailsHeader = "<div class='expanderContainer' style='width:180%; padding-left: 0.50%;' >" + StockDetailsHeader + StockDetailsHeaderData + "</div>";
//                var disHtml = "<div class='expanderContainer' style='width:170%'>" + header + headerData + "</div>";
                body.innerHTML = disHtmlStockDetailsHeader ;
                
                if (this.expandButtonClicked) {
                    this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                    this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                }
            }
        }
    },
    getStockDetailsHeader:function(rec){
        //code for account details
        var StockDetailsHeaderArray = [];
        var type = "";
        var AccArr = ["Product Id","Product Name","Product Type","BOM Type","UOM","Required Quantity","Blocked Quantity","Consumption Quantity","Returned Quantity","Wastage Quantity","Recycle Quantity","Produced Quantity","Unit Price ($)","Total Amount ($)"];
       
        var count = 0;
        for (var custArrcount = 0; custArrcount < AccArr.length; custArrcount++) {
            if (AccArr[custArrcount] != "") {
                count++;
            }
        }
        var widthInPercent = 80 / count;
        var minWidth = count * 100 + 40;
        var AccGridHeaderText = "Stock Details";
        var StockDetailsHeader = "<span class='gridHeader'>" + AccGridHeaderText + "</span>";
        StockDetailsHeader += "<span class='gridNo' style='font-weight:bold;'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";    //S.No.
        for (var j = 0; j < AccArr.length; j++) {
            StockDetailsHeader += "<span class='headerRow' style='width:" + widthInPercent + "% ! important;'>" + AccArr[j] + "</span>";
        }
        StockDetailsHeader += "<span class='gridLine' style='width:80%'></span>";

        StockDetailsHeaderArray.push(StockDetailsHeader);
        StockDetailsHeaderArray.push(widthInPercent);
        return StockDetailsHeaderArray;
    },
    getStockDetailsHeaderData:function(accountDetailsRec,sameParent,widthInPercent){
        if (!sameParent || this.StockCount == 1) {
            this.StockDetailsHeader = "";
            this.StockCount = 1;
        }

        this.StockDetailsHeader += "<span class='gridNo' >" + (this.StockCount) + ".</span>";
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" +accountDetailsRec.data['productid'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['productid'], 20) + "</span>";
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec.data['productname'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['productname'], 20) + "</span>";
       
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" +accountDetailsRec.data['producttype'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['producttype'], 20) + "</span>";
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec.data['bomtype'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['bomtype'], 20) + "</span>";
        
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec.data['uom'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['uom'], 20) + "</span>";
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec.data['requiredquantity'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['requiredquantity'], 20) + "</span>";
       
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" +accountDetailsRec.data['blockedquantity'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['blockedquantity'], 20) + "</span>";
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec.data['consumptionquantity'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['consumptionquantity'], 20) + "</span>";
       
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" +accountDetailsRec.data['returnedquantity'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['returnedquantity'], 20) + "</span>";
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" +accountDetailsRec.data['wastagequantity'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['wastagequantity'], 20) + "</span>";
        
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec.data['recyclequantity'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['recyclequantity'], 20) + "</span>";
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" +accountDetailsRec.data['producedquantity'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['producedquantity'], 20) + "</span>";
       
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" +accountDetailsRec.data['unitprice'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['unitprice'], 20) + "</span>";
        this.StockDetailsHeader += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'  wtf:qtip='" + accountDetailsRec.data['totalamount'] + "'>" + Wtf.util.Format.ellipsis(accountDetailsRec.data['totalamount'], 20) + "</span>";
       
        this.StockDetailsHeader += "<br>";
        this.StockCount++;
        return this.StockDetailsHeader;
    }

});
