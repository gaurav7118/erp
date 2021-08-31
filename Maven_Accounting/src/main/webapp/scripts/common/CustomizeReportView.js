
Wtf.CustomizeReportView = function(config) {
    this.grid = config.grid;
    Wtf.apply(this, config);

    Wtf.CustomizeReportView.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.CustomizeReportView, Wtf.Window, {
//    iconCls: 'importIcon',
    width: 750,
    height: 570,
    modal: true,
    layout: "border",
    id: 'CustomizeReportView',
    closable: true,
    initComponent: function(config) {


        Wtf.CustomizeReportView.superclass.initComponent.call(this, config);

        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: false
        });

        this.okButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.msgbox.ok"), //"Import Data",
            scope: this,
            minWidth: 80,
            handler: this.okAction
        });
        this.cancelButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
            scope: this,
            minWidth: 80,
            handler: function() {
                Wtf.getCmp('CustomizeReportView').close();
//          Wtf.getCmp('sequenceFormatWindow').show();
            }
        });
        this.buttons = [this.okButton, this.cancelButton];
    },
    onRender: function(config) {
        Wtf.CustomizeReportView.superclass.onRender.call(this, config);

        this.northMessage = "<ul style='list-style-type:disc;padding-left:15px;'>" +
                "<li>" + WtfGlobal.getLocaleText("acc.field.CustomizeReportViewbymodule") + "</li>";
//      
        this.createEditor();
        this.grid.on('rowclick', this.handleRowClick, this);
        this.add(this.northPanel = new Wtf.Panel({
            region: 'north',
            height: 70,
            border: false,
            bodyStyle: 'background:white;padding:7px',
            html: getImportTopHtml(WtfGlobal.getLocaleText("acc.field.CustomizeReportView"), this.northMessage + "</ul>", "../../images/accounting_image/role-assign.gif", true, "0px", "2px 0px 0px 10px")
        }));
        
        var moduleArray=[];
        if (this.modules != undefined) {
            var modules = this.modules.split(",");
            for (var i = 0; i < modules.length; i++) {
                switch (modules[i]) {
                    case "" + Wtf.Acc_Customer_ModuleId:
                        moduleArray.push([Wtf.Acc_Customer_ModuleId, "Customer"]);
                        break;
                    case "" + Wtf.Acc_Vendor_ModuleId:
                        moduleArray.push([Wtf.Acc_Vendor_ModuleId, "Vendor"]);
                        break;
                    case "" + Wtf.Acc_Goods_Receipt_ModuleId:
                        moduleArray.push([Wtf.Acc_Goods_Receipt_ModuleId, "Goods Receipt"]);
                        break;
                    case "" + Wtf.Acc_Delivery_Order_ModuleId:
                        moduleArray.push([Wtf.Acc_Delivery_Order_ModuleId, "Delivery Order"]);
                        break;
                    case "" + Wtf.Acc_Sales_Return_ModuleId:
                        moduleArray.push([Wtf.Acc_Sales_Return_ModuleId, "Sales Return"]);
                        break;
                    case "" + Wtf.Acc_Purchase_Return_ModuleId:
                        moduleArray.push([Wtf.Acc_Purchase_Return_ModuleId, "Purchase Return"]);
                        break;
                    case "" + Wtf.Acc_Product_Master_ModuleId:
                        moduleArray.push([Wtf.Acc_Product_Master_ModuleId, "Product"]);
                        break;
                    case "" + Wtf.Acc_Invoice_ModuleId:
                        moduleArray.push([Wtf.Acc_Invoice_ModuleId, "Invoice"]);
                        break;
                    case "" + Wtf.Acc_Credit_Note_ModuleId:
                        moduleArray.push([Wtf.Acc_Credit_Note_ModuleId, "Credit Note"]);
                        break;
                    case "" + Wtf.Acc_Receive_Payment_ModuleId:
                        moduleArray.push([Wtf.Acc_Receive_Payment_ModuleId, "Payment Received"]);
                        break;
                    case "" + Wtf.Acc_Debit_Note_ModuleId:
                        moduleArray.push([Wtf.Acc_Debit_Note_ModuleId, "Debit Note"]);
                        break;
                    case "" + Wtf.Inventory_Stock_Adjustment_ModuleId:
                        moduleArray.push([Wtf.Inventory_Stock_Adjustment_ModuleId, "Stock Adjustment"]);
                        break;
                    case "" + Wtf.Acc_InterStore_ModuleId:
                        moduleArray.push([Wtf.Acc_InterStore_ModuleId, "Inter Store"]);
                        break;
                    case "" + Wtf.Acc_GENERAL_LEDGER_ModuleId:
                        moduleArray.push([Wtf.Acc_GENERAL_LEDGER_ModuleId, "Journal Entry"]);
                        break;
                    case "" + Wtf.Acc_Vendor_Invoice_ModuleId:
                        moduleArray.push([Wtf.Acc_Vendor_Invoice_ModuleId, "Vendor Invoice"]);
                        break;
                    case "" + Wtf.Acc_Sales_Order_ModuleId:
                        moduleArray.push([Wtf.Acc_Sales_Order_ModuleId, "Sales Order"]);
                        break;
                    case "" + Wtf.Acc_Customer_Quotation_ModuleId:
                        moduleArray.push([Wtf.Acc_Customer_Quotation_ModuleId, "Customer Quotation"]);
                        break;
                    case "" + Wtf.Acc_Make_Payment_ModuleId:
                        moduleArray.push([Wtf.Acc_Make_Payment_ModuleId, "Payment Made"]);
                        break;
                    case "" + Wtf.Acc_Vendor_Quotation_ModuleId:
                        moduleArray.push([Wtf.Acc_Vendor_Quotation_ModuleId, "Vendor Quotation"]);
                        break;
                    case "" + Wtf.Acc_Purchase_Order_ModuleId:
                        moduleArray.push([Wtf.Acc_Purchase_Order_ModuleId, "Purchase Order"]);
                        break;
                    case "" + Wtf.labourMaster:
                        moduleArray.push([Wtf.labourMaster, "Labour"]);
                        break;
                    case "" + Wtf.MRP_Work_Centre_ModuleID:
                        moduleArray.push([Wtf.MRP_Work_Centre_ModuleID, WtfGlobal.getLocaleText("acc.up.68")]);
                        break;
                    case "" + Wtf.MACHINE_MANAGEMENT_MODULE_ID:
                        moduleArray.push([Wtf.MACHINE_MANAGEMENT_MODULE_ID, "Machine Master"]);
                        break;
                    case "" + Wtf.MRP_Work_Order_ModuleID:
                        moduleArray.push([Wtf.MRP_Work_Order_ModuleID, "Work Order"]);
                        break;
                    case "" + Wtf.MRP_MASTER_CONTRACT_MODULE_ID:
                        moduleArray.push([Wtf.MRP_MASTER_CONTRACT_MODULE_ID, "Master Contract"]);
                        break;
                    case "" + Wtf.MRP_Route_Code_ModuleID:
                        moduleArray.push([Wtf.MRP_Route_Code_ModuleID, "Routing Template"]);
                        break;
                    case "" + Wtf.MRP_Job_Work_ModuleID:
                        moduleArray.push([Wtf.MRP_Job_Work_ModuleID, "Job Work"]);
                        break;
                    case "" + Wtf.Acc_FixedAssets_Details_ModuleId:
                        moduleArray.push([Wtf.Acc_FixedAssets_Details_ModuleId, "Asset Depreciaton Details"]);
                        break;
                }
            }
        }
        this.moduleStore = new Wtf.data.SimpleStore({
            fields: ['moduleid', 'module'],
            data: moduleArray
        });
        this.detailRec = Wtf.data.Record.create([
            {name: 'fields'},
            {name: 'headerName'},
            {name: 'columnWidth'}


        ]);
        this.Store = new Wtf.data.SimpleStore({
            fields: this.detailRec
//        groupField: 'personname',

        });

        this.fieldRec = new Wtf.data.Record.create([
            {name: "id"},
            {name: "fieldname"},
            {name: "fieldlabel"}
        ]);
         this.module = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.selectmodule.combo") + " ",
            store: this.moduleStore,
            valueField: 'moduleid',
            displayField: 'module',
            mode: 'local',
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.selectmodule.combo"),
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: false,
            width: 200,
            forceSelection: true,
            scope: this
        });
        if(this.moduleid==undefined || this.moduleid==''){
            this.moduleid=Wtf.Acc_Invoice_ModuleId;
        } 
        this.fieldStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.fieldRec),

//            http://localhost:8080/Accounting/a/rahulerp37/ACCAccountCMN/getFieldParams.do

            url: "ACCAccountCMN/getFieldParams.do",
            baseParams: {
                customcolumn: 0,
                isActivated: 1,
                isOpeningTransaction: false,
//                moduleid: this.moduleid,
                reportid:this.reportId
                
            }
        });

        this.field = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.selectfield.combo") + " ",
            store: this.fieldStore,
            valueField: 'fieldlabel',
            displayField: 'fieldlabel',
            mode: 'local',
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.selectfield.combo"),
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: false,
            width: 200,
            forceSelection: true,
            scope: this
        });



        this.module.on('select', this.onModuleChange, this);

        this.addButton = new Wtf.Button({
            text:  WtfGlobal.getLocaleText("acc.common.add"),
            scope: this,
            handler: this.addFunction
        })

        this.selectFieldset = new Wtf.form.FieldSet({
//            title: WtfGlobal.getLocaleText("acc.import.masterPref1"), 
            autoHeight: true,
            border: false,
            width: '100%',
//            hidden:this.isGroupImport,
            cls: "import-Wiz-fieldset",
//            defaultType: 'radio',
            layout: 'form',
            items: [{layout: 'column',
                    border: false,
                    labelWidth: 130,
                    defaults: {
                        border: false
                    }, items: [{
                            layout: 'form',
                            columnWidth: 0.45,
                            items: this.module
                        }, {
                            columnWidth: 0.45,
//                            labelWidth: 40,
                            layout: 'form',
                            items: this.field
                        },
                        {columnWidth: 0.10,
//                            labelWidth: 40,
                            layout: 'form',
                            items: this.addButton
                        },
                    ]
                }]
        });

        this.add({
            region: 'center',
            layout: 'border',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:white;padding:7px',
            items: [
                {
                    region: "north",
                    height: 60,
                    border: false,
                    items: [this.selectFieldset]
                },
                this.grid
            ]

        });
    }, 
    onModuleChange: function() {
        this.isReturnAllFields = false;
        this.setModuleId();
        this.fieldStore.load({
            params: {
                moduleid: this.moduleid,
                isReturnAllFields: this.isReturnAllFields
            }
        });
        this.customHeaderStore.load({
            params: {
                moduleid: this.moduleid
            }
        });
    },
    handleRowClick: function(grid, rowindex, e) {
        if (e.getTarget(".delete-gridrow")) {
            var record=this.customHeaderStore.data.items[rowindex].data;
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn) {
                if (btn != "yes")
                    return;
                
                Wtf.Ajax.requestEx({
                    url: "ACCOtherReports/deleteCustomizeReportColumn.do",
                    params: {
                        id:record.id,
                        reportId: this.reportId
                    }
                }, this);
                this.customHeaderStore.reload();
            }, this);
        }


    },
    createEditor: function() {
        this.groupingView1 = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: true,
            hideGroupedColumn: true
        });

        this.customHeaderRecord = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'headerName'},
            {name: 'fieldname'},
            {name: 'fieldDataIndex'},
            {name: 'action'}
        ]);


        this.customHeaderReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count"
        }, this.customHeaderRecord);
        
        this.customHeaderStore = new Wtf.data.Store({
            url: "ACCOtherReports/getCustomizeReportViewMappingField.do",
            baseParams: {
                reportId: this.reportId
            },
            reader: this.customHeaderReader
        });
//        this.customHeaderStore.load();
        for (var i = 0; i < this.parentPanel.arr.length; i++) {
            var record = new this.customHeaderRecord({
                headerName: this.parentPanel.arr[i]
            });
            this.customHeaderStore.add(record);
        }
//        if(this.customHeaderStore.data.items.length==0){
//            this.grid.getView().emptyText='<div class="emptyGridText">' + WtfGlobal.getLocaleText('acc.feild.selectdate') + ' <br></div>'
//        }
        this.cmodel = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.common.columnName"),
                dataIndex: 'id',
                width: 100,
                hidden: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.common.moduleName"),
                dataIndex: 'module',
                width: 100,
                hidden: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.common.columnName"),
                dataIndex: 'headerName',
                width: 500
            }, {
                header: WtfGlobal.getLocaleText("acc.invoice.gridAction"), //"Action",
                align: 'center',
                dataIndex: 'action',
                width: 200,
                renderer: this.deleteRenderer.createDelegate(this)}
        ]);

        this.grid = new Wtf.grid.GridPanel({
            ds: this.customHeaderStore,
            cm: this.cmodel,
            layout: "fit",
            region: "center",
            border: false,
            autoScroll: true,
            height: 500,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

    }, deleteRenderer: function(v, m, rec) {
        return "<div style='margin: auto;' class='" + getButtonIconCls(Wtf.etype.deletegridrow) + "'></div>";
    },
    addFunction: function() {
        var val = this.field.getValue();
        var count = this.customHeaderStore.data.items.length;

        for (var i = 0; i < count; i++) {
            if (val == this.customHeaderStore.data.items[i].data.headerName)
            {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.alert"),
                    msg: WtfGlobal.getLocaleText("acc.customizereportview.duplicatecolumn.msg"),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    width: 400
                });
                return;
            }
        }
        if (val != "") {
            var record = new this.customHeaderRecord({
                id: "",
                headerName: val,
                fieldname:val,
                fieldDataIndex:val
                
            });
            this.customHeaderStore.add(record);
        }
    },
    setModuleId: function() {
        if (this.reportId == Wtf.autoNum.StockReportOnDimension || this.module.getValue()==Wtf.Acc_Customer_ModuleId || this.module.getValue()==Wtf.Acc_Vendor_ModuleId) {
            if(this.reportId == Wtf.autoNum.dayEndCollectionReport){
                this.isReturnAllFields = false;
            }else{
                this.isReturnAllFields = true;
            }
        }
            this.moduleid = this.module.getValue();
      
    },
    okAction: function() {
        // Collect ids of those records
        var idsArray = [];
        var records = this.grid.getStore();
        if(records.data && records.data.length==0){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.alert"),
                msg: WtfGlobal.getLocaleText("acc.customizereportview.selectionAlertMsg"),
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.WARNING,
                width: 400
            });
            return;
        }
        records.each(function(record) {
            var ids = [];
            idsArray.push("{'id'='"+record.get('id')+"','isManadatoryField'='"+false+"','fieldname'='"+record.get('fieldname')+"','fieldDataIndex'='"+record.get('fieldDataIndex')+"','hidecol'='"+false+"','isreadonlycol'='"+false+"','fieldlabeltext'='"+""+"','isUserManadatoryField'='"+false+"'}");
        });
        
        
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/saveCustomizedReportFields.do",
            params: {
                flag: 34,
                moduleid:this.moduleid,
                reportId: this.reportId,
                isFormField:false,
                isLineField:false,
                //                    data: ids,
                data:Wtf.util.JSON.encode(idsArray),
                isOrderCustOrDimFields:false
            }
        }, this, function(action, response){
            if(action.success){
                Wtf.getCmp('CustomizeReportView').close();
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.field.Confirmation"),
                    msg: WtfGlobal.getLocaleText("acc.field.Configurationsavedsuccessfully"),
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    fn:function(btn){
                        //                        this.parentGrid=Wtf.getCmp("gridmsg"+this.parentHelpModeId+this.parentId);
                        if (this.reportId == Wtf.autoNum.AgedReceivableDetailReport || this.reportId == Wtf.autoNum.agedDetailBasedOnSalesPerson || this.reportId == Wtf.autoNum.SO_By_ProductReport) {
                            this.parentPanel.appendGridColumn(this.reportId);
                        } else {
                            this.parentPanel.Store.load({
                                params: {
                                    arr: "",
                                    start: 0,
                                    limit: 30
                                }
                            });
                        }
                    },
                    scope:this,
                    icon: Wtf.MessageBox.INFO
                });
            } else {
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.invoiceList.status"), action.msg);
            }
        },function() {
            });

    }

});