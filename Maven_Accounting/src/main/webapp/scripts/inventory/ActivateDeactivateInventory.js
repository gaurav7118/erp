


Wtf.ActivateInventoryWizard = function (config){
    Wtf.apply(this,{
        title : "Activate Inventory integration",
        WinTitle: "Activate Inventory Tab",
        WinDetail: "Enable all inventory transactions by activating inventory tab",
        modal : true,
        iconCls : 'iconwin',
        minWidth:75,
        width : 600,
        height: 650,
        resizable :true,
        scrollable:true,
        layout:'fit',
        border:false,
        buttons:[{
            text:'Confirm',
            scope: this,
            handler: function(){
                this.activateInventoryHandler()
            }
        }, {
            text:'Cancel',
            scope: this,
            handler: function(){
                this.close();
            }
        }]
    });
    Wtf.ActivateInventoryWizard.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.ActivateInventoryWizard,Wtf.Window,{
    
    initComponent:function (){
        Wtf.ActivateInventoryWizard.superclass.initComponent.call(this);
        this.activateType = "NONE";
        this.GetNorthPanel();
        this.GetAddEditForm();

        this.mainPanel = new Wtf.Panel({
            layout:'border',
            items:[this.northPanel, this.ActivateWLPanel]
        });
        this.add(this.mainPanel);
        
        this.addEvents({
            activationSuccess : true
        })
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(this.WinTitle, this.WinDetail,'images/accounting_image/price-list.gif', true)
        });
    },
    GetAddEditForm:function (){
        
        this.companyLevelWarehouse = new Wtf.form.Checkbox({
            boxLabel: "Activate Warehouse at company level*",
            hideLabel:true,
            labelSeparator: "",
            checked:true,
            disabled : true,
            name:'activateWLInCompany'
        })
        this.companyLevelLocation = new Wtf.form.Checkbox({
            boxLabel: "Activate Location at company level*",
            hideLabel:true,
            labelSeparator: "",
            checked:true,
            disabled : true,
            name:'activateWLInCompany'
        })
        this.companyLevelBatch = new Wtf.form.Checkbox({
            boxLabel: "Activate Batch at company level",
            hideLabel:true,
            labelSeparator: "",
            checked:true,
            name:'activateWLInCompany'
        })
        this.companyLevelSerial = new Wtf.form.Checkbox({
            boxLabel: "Activate Serial at company level",
            hideLabel:true,
            labelSeparator: "",
            checked:true,
            name:'activateWLInCompany'
        })
        var companyLevelnote = {
            xtype: "panel",
            border:false,
            height: 30,
            html: "<b>Note:</b> <i>By activating inventory, warehouse and location both are activated by default. This is mandatory to use inventory. You can also activate batch and serial in company level.</i>"
        }
        
        var companyFieldSet = new Wtf.form.FieldSet({
            title : "Activate Warehouse and Location for Company",
            height: 160,
            items:[
            this.companyLevelWarehouse,
            this.companyLevelLocation,
            this.companyLevelBatch,
            this.companyLevelSerial,
            companyLevelnote
            ]
        })
        
        
        this.notActivateWLRadio = new Wtf.form.Radio({
            boxLabel: "Do not activate warehouse and location for existing products. I will manage it for new products in future",
            height: 40,
            hideLabel:true,
            labelSeparator: "",
            checked:true,
            name:'activateWLInProduct'
        })
        this.allActivateWLRadio = new Wtf.form.Radio({
            boxLabel: "Activate warehouse and location for all existing products.",
            hideLabel:true,
            labelSeparator: "",
            name:'activateWLInProduct'
        })
        this.selectedActivateWLRadio = new Wtf.form.Radio({
            boxLabel: "Activate warehouse and location for selected existing products.",
            hideLabel:true,
            labelSeparator: "",
            name:'activateWLInProduct'
        })
        var productLevelnote = {
            xtype: "panel",
            border:false,
            height: 30,
            html: "<b>Note:</b> <i>This process will update all previous transactions of these products with default warehouse and default location</i>"
        }

        var gridRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'productid'
        },{
            name: 'productname'
        },{
            name: 'desc'
        }
        ]);

        var gridStore = new Wtf.data.Store({
            url:  'INVActiveDeactive/getInactivedInventoryProducts.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },gridRecord)
        });
        
        gridStore.load();

        var sm = new Wtf.grid.CheckboxSelectionModel({
            width:25
        });
        var cm = new Wtf.grid.ColumnModel([
            sm,
            new Wtf.grid.RowNumberer(),
            {
                header:"Product Code",
                dataIndex:"productid"
            }, {
                header:"Product Name",
                dataIndex:"productname"
            }, {
                header:"Product Description",
                dataIndex:"desc"
            }]);
        
        this.productGrid=new Wtf.grid.GridPanel({
            width:544,
            height:150,
            border: false,
            store: gridStore,
            cm: cm,
            disabled:true,
            sm:sm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true,
                emptyText: 'No any product to activate warehouse or location'
            }
        })
        
        var productFieldSet = new Wtf.form.FieldSet({
            title : "Activate Warehouse and Location for Product",
            height: 300,
            border: false,
            items:[
            this.notActivateWLRadio,
            this.allActivateWLRadio,
            this.selectedActivateWLRadio,
            productLevelnote,
            this.productGrid
            ]
        })
        
        this.ActivateWLPanel = new Wtf.form.FormPanel({
            region: 'center',
            layout: 'border',
            border: false,
            bodyStyle:"background-color:#f1f1f1;padding:10px",
            items:[{
                region:"center",
                border: false,
                items:[
                companyFieldSet,
                productFieldSet
                ]
            }]
        });
        
        this.selectedActivateWLRadio.on('focus', function(comp){
            this.productGrid.enable();
            this.activateType = "SELECTED";
        }, this)
        this.allActivateWLRadio.on('focus', function(comp){
            this.productGrid.disable();
            this.activateType = "ALL";
        }, this)
        this.notActivateWLRadio.on('focus', function(comp){
            this.productGrid.disable();
            this.activateType = "NONE";
        }, this)
        
    },
    isAllSelected: function(){
        var grid = this.productGrid;
        var totalCount = grid.store.getTotalCount();
        var recs = grid.getSelectionModel().getSelections();
        
        return totalCount == recs.length
    },
    getProductArray: function(){
        var recs = this.productGrid.getSelectionModel().getSelections();
        if(recs.length == 0){
            WtfComMsgBox(["Alert", "You need to select product(s) to activate warehouse and location for existing products"], 2);
            return false;
        }
        var productArray = [];
        for(var i=0 ; i<recs.length ; i++){
            var rec = recs[i];
            productArray.push(rec.get('id'))
            
        }
        return productArray;
    },
    activateInventoryHandler : function(){  
        
        //        
        var companyLevel = {
            activateWarehouse : this.companyLevelWarehouse.getValue(),
            activateLocation : this.companyLevelLocation.getValue(),
            activateBatch : this.companyLevelBatch.getValue(),
            activateSerial : this.companyLevelSerial.getValue()
        }
        var productLevel = {
            activateType : this.activateType
        };
        var totalCount = 0;
        if(this.activateType == "ALL"){
            totalCount = this.productGrid.store.getTotalCount();
        }
        if(this.activateType == "SELECTED"){
            if(this.isAllSelected()){
                productLevel.activateType = "ALL"
            }else{
                var productArray = this.getProductArray();
                if(!productArray){
                    return false;
                }
                productLevel.activateType = "SELECTED"
                productLevel.productIds = productArray.toString()
                totalCount = productArray.length;
            }
        }
        
        var setup = {
            companyLevel : companyLevel,
            productLevel : productLevel
        }
        var confMsg = "<p><b>Inventory Activation will take some more time to process. Please make sure no other transaction should be perform during this process."
                + " We will notify you by an email once the activation process is completed.</b></p><br><p style='padding-left: 47px;'>Are you sure you want to proceed for activation?</p>";
        Wtf.MessageBox.show({
            title: 'Confirm',
            msg: confMsg,
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'mb9',
            fn: function(btn) {
                if (btn == "yes") {
                    var loadingMask = new Wtf.LoadMask(document.body, {
                        msg: WtfGlobal.getLocaleText("acc.msgbox.50")
                    });
                    loadingMask.show();
                    Wtf.Ajax.requestEx({
                        method: 'POST',
                        url: 'INVActiveDeactive/activateInventory.do',
                        params: ({
                            setupData: JSON.stringify(setup),
                            totalProducts: totalCount
                        })
                    },
                    this,
                            function(result, req) {
                                loadingMask.hide();
                                if (result.success == true) {
                                    var successMsg = "Inventory Activation process is started, we will notify you with your registered email while activation process is completed.";
                                    WtfComMsgBox(["Alert", successMsg], 0);
                                    this.fireEvent('activationSuccess');

                                } else {
                                    WtfComMsgBox(["Warning", result.msg], 2);
                                    return false;
                                }
                            },
                            function(result, req) {
                                loadingMask.hide();
                                WtfComMsgBox(["Error", "Error occured while processing your request."], 1);
                                return false;
                            }
                    )
                }
            },
            scope: this,
            icon: Wtf.MessageBox.QUESTION,
            width:600
        });
    }
});

Wtf.DeactivateInventoryWizard = function (config){
    Wtf.apply(this,{
        title : "Deactivate Inventory integration",
        WinTitle: "Deactivate Inventory Tab",
        WinDetail: "Disable all inventory transactions by deactivating inventory tab",
        modal : true,
        iconCls : 'iconwin',
        minWidth:75,
        width : 600,
        height: 500,
        resizable :true,
        scrollable:true,
        layout:'fit',
        border:false,
        buttons:[{
            text:'Confirm',
            scope: this,
            handler: function(){
                this.activateInventoryHandler()
            }
        }, {
            text:'Cancel',                
            scope: this,
            handler: function(){
                this.close();
            }
        }]
    });
    Wtf.DeactivateInventoryWizard.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.DeactivateInventoryWizard,Wtf.Window,{
    
    initComponent:function (){
        Wtf.DeactivateInventoryWizard.superclass.initComponent.call(this);
        this.activateType = "NONE";
        this.GetNorthPanel();
        this.GetAddEditForm();

        this.mainPanel = new Wtf.Panel({
            layout:'border',
            items:[this.northPanel, this.ActivateWLPanel]
        });
        this.add(this.mainPanel);
        
        this.addEvents({
            deactivationSuccess : true
        })
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(this.WinTitle, this.WinDetail,'images/accounting_image/price-list.gif', true)
        });
    },
    GetAddEditForm:function (){

        
        var gridRecord = new Wtf.data.Record.create([
        {
            name: 'transactionNo'
        },{
            name: 'product'
        },{
            name: 'module'
        }
        ]);

        var gridStore = new Wtf.data.Store({
            url:  'INVActiveDeactive/getAllInTransitTransactions.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty: "count"
            },gridRecord)
        });

        var sm = new Wtf.grid.RowSelectionModel({
            width:25
        });
        var cm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:"Transaction No",
                dataIndex:"transactionNo"
            }, {
                header:"Product ID",
                dataIndex:"product"
            }, {
                header:"Module",
                dataIndex:"module"
            }]);
        
        this.productGrid=new Wtf.grid.GridPanel({
            width:544,
            height:170,
            border: false,
            store: gridStore,
            cm: cm,
            sm:sm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true,
                emptyText: 'No any In-Tansit Transaction found'
            },
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                store: gridStore,
                displayInfo: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg:"No results to display",
                plugins: new Wtf.common.pPageSize()
            })
        })
        gridStore.load({
            params:{
                start : 0,
                limit: this.pagingToolbar.pageSize
            }
        });
        var transactionFieldSet = new Wtf.form.FieldSet({
            title : "Pending In-Transit Transactions",
            height: 200,
            border: false,
            items:[
            this.productGrid
            ]
        })
        
        this.clearTransactionRadio = new Wtf.form.Checkbox({
            boxLabel: "Complete all In-Transit Requests and Deactivate Inventory",
            height: 30,
            hideLabel:true,
            labelSeparator: "",
            checked:true,
            disabled:true,
            name:'deactivateInventory'
        })
        var productLevelnote = {
            xtype: "panel",
            border:false,
            height: 100,
            html: "<p><b>Note:</b>This process will complete all the In-Transit requests of inventory modules"
        +"<ul style='list-style: disc !important; padding-left: 20px; '>"
        +"<li>Pending In-Transit Requests will be cancelled, If some stock is already issued then the issued stock will go back to the issuance store.</li>"
        +"<li>Return Requests will be complete by collecting the stock.</li>"
        +"<li>Pending Requests which are pending for either QA or Repair, those will be auto approved.</li>"
        +"</ul>"
        +"</p>"
        }

        this.ActivateWLPanel = new Wtf.form.FormPanel({
            region: 'center',
            layout: 'border',
            border: false,
            bodyStyle:"background-color:#f1f1f1;padding:10px",
            items:[{
                region:"center",
                border: false,
                items:[
                transactionFieldSet,
                this.clearTransactionRadio,
                productLevelnote,
                ]
            }]
        });
    },
    deactivateInventoryHandler : function(){  
        
        var confMsg = "<p><b>Inventory Dectivation will take some more time to process. Please make sure no other transaction should be perform during this process."
        +" We will notify you by an email once the deactivation process is completed.</b></p><br><p style='padding-left: 47px;'>Are you sure you want to proceed for deactivation?</p>";
        Wtf.MessageBox.show({
            title: 'Confirm',
            msg: confMsg,
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'mb9',
            fn: function(btn) {
                if (btn == "yes") {
                    var loadingMask = new Wtf.LoadMask(document.body, {
                        msg: WtfGlobal.getLocaleText("acc.msgbox.50")
                    });
                    loadingMask.show();
                    Wtf.Ajax.requestEx({
                        method: 'POST',
                        url: 'INVActiveDeactive/deactivateInventory.do'
                    },
                    this,
                            function(result, req) {
                                loadingMask.hide();
                                if (result.success == true) {
                                    var successMsg = "Inventory dectivation process is started, we will notify you with your registered email while deactivation process is completed.";
                                    WtfComMsgBox(["Alert", successMsg], 0);
                                    this.fireEvent('deactivationSuccess');
                                } else {
                                    WtfComMsgBox(["Warning", result.msg], 2);
                                    return false;
                                }
                            },
                            function(result, req) {
                                loadingMask.hide();
                                WtfComMsgBox(["Error", "Error occured while processing your request."], 1);
                                return false;
                            }
                    )
                }
            },
            scope: this,
            icon: Wtf.MessageBox.QUESTION,
            width:600
        });
    }
});