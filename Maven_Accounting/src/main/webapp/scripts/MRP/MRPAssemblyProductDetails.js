/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.MRPAssemblyProductDetails = function(config) {
    this.id = config.id;
    this.isCAComponent=config.isCAComponent;
    this.readOnly = config.readOnly != undefined ? config.readOnly : false;
    Wtf.apply(this, config);
    Wtf.account.MRPAssemblyProductDetails.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.MRPAssemblyProductDetails, Wtf.Panel, {
    border: false,
    layout: 'border',
//    autoScroll: true,
    height: 720,
//    plugins: new Wtf.ux.collapsedPanelTitlePlugin(),
//    collapsibletitle: "BOM Details",
//    title: "BOM Details",
//    cls : 'southcollapse',
//    collapsible: true,
//    collapsed: false,
    onRender: function(config) {
        this.createWestPanel();
        if (this.isCAComponent) {
            this.createCAGrid();
        } else {
            this.createGrid();
            this.createFields();
            this.createForm();
            this.createGridOfBOMSaved();
        }
        
        this.itemsArr = [];
        if (!this.isCAComponent) {
            this.itemsArr = [this.bomCreationForm,this.AssemblyGrid, this.createdBOMGrid]
        } else {
            this.itemsArr = [this.CAGrid]
        }
        this.add(this.westPanel, {
            region: 'center',
            border: false,
//            autoScroll:true,
            layout:this.isCAComponent?"fit":"",
//            layout:"fit",
            items: this.itemsArr
        });
        Wtf.account.MRPAssemblyProductDetails.superclass.onRender.call(this, config);
    },
    createGrid: function() {

        this.AssemblyGrid = new Wtf.account.productAssemblyGrid({
            layout: "fit",
            bodyBorder: true,
//            disabled: this.readOnly,
            readOnly: this.readOnly,
            region: 'center',
            hidden: false,
            border: false,
            bodyStyle: 'padding:10px',
            height: 400,
            productForm:true,
            isInitialQuatiy: false,
            excluseDateFilters: true,
            gridtitle: WtfGlobal.getLocaleText("acc.product.gridBillofMaterials") + " " + WtfGlobal.getLocaleText("acc.field.AP.note"), //"Bill Of Materials",
            productid: (this.record != null ? this.record.data['productid'] : null),
            rendermode: "productform"
        });
    },
    createFields: function() {
        this.bomCode = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.bomcode"), //'BOM Code*',
            name: 'bomCode',
            anchor: '95%',
            width: 200,
            readOnly: this.readOnly,
            maxLength: 50
        });
        this.bomName = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.bomname"), //'BOM Name',
            name: 'bomName',
            anchor: '95%',
            width: 200,
            readOnly: this.readOnly,
            maxLength: 50
        });
        this.parentRec = new Wtf.data.Record.create([{
                name: 'parentid', mapping: 'productid'
            }, {
                name: 'parentname', mapping: 'productname'
            }, {
                name: 'leaf', type: 'boolean'
            }, {
                name: 'level', type: 'int'
            }, {
                name: "productid"
            }, {
                name: 'pid', mapping: 'pid'
            }]);
        this.parentStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.parentRec),
            url: "ACCProduct/getProductsOptimized.do",
            baseParams: {
                mode: 22,
                includeParent: true,
                showallproduct: 2, //this is for to fetch non deleted products in parent products combo
                productid: (this.record != null && !this.isClone ? this.record.data['productid'] : null)
            }
        });
        this.linkProductCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.linkproductname"),
            anchor: '95%',
            hiddenName: 'parentid',
            store: this.parentStore,
            width: 200,
            valueField: 'productid',
            displayField: 'productname',
            extraFields: ['pid', 'productname'],
            readOnly: this.readOnly,
            disabled: this.readOnly,
            maxHeight: 250,
            isProductCombo: true,
            listWidth: 300,
            mode: 'remote',
            minChars: 2,
            extraComparisionField: 'pid',
            typeAhead: true,
            forceSelection: true,
            hirarchical: true,
            isParentCombo: true,
            triggerAction: 'all'
        });
        this.workCenter = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machine.workCenter"), //'Work Center',
            width: 200,
            name: 'workcenter',
            readOnly: this.readOnly,
            anchor: '95%',
            maxLength: 50
        });
        this.alternateBOMCode = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.alternatebomcode"), //"Alternate BOM Code"
            name: 'alternatebomcode',
            width: 200,
            readOnly: this.readOnly,
            anchor: '95%',
            maxLength: 50
        });
        this.linkAlternateBOM = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.linkalternatebom"),
            anchor: '95%',
            hiddenName: 'productid',
            store: this.parentStore,
            valueField: 'productid',
            displayField: 'productname',
            disabled: this.readOnly,
            readOnly: this.readOnly,
            extraFields: ['pid', 'productname'],
            width: 200,
            maxHeight: 250,
            isProductCombo: true,
            listWidth: 300,
            mode: 'local',
            minChars: 2,
            extraComparisionField: 'pid',
            typeAhead: true,
            forceSelection: true,
            hirarchical: true,
            isParentCombo: true,
            triggerAction: 'all'
        });
        this.subbomcode = new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.parentbomcode"), //'Parent BOM Code',
            name: 'subbomcode',
            width: 200,
            anchor: '95%',
            readOnly: this.readOnly,
            maxLength: 50
        });
        this.subbom = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.mrp.field.isasubbom"),
            checkboxToggle: true,
            autoHeight: true,
            autoWidth: true,
            width: 400,
            checkboxName: 'subbom',
            style: 'margin-right:30px',
            scope: this,
            collapsed: true,
            items: [this.subbomcode]
        });
        this.isDefaultBOM = new Wtf.form.Checkbox({
            name: 'isdefaultbom',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.mrp.field.isdefaultbom") + "'>" + WtfGlobal.getLocaleText("acc.mrp.field.isdefaultbom") + "</span>",
            disabled: this.readOnly,
            checked: false,
            scope: this,
            cls: 'custcheckbox',
            width: 10
        });
    },
    createForm: function() {
        this.bomCreationForm = new Wtf.form.FormPanel({
            region: 'north',
//            autoHeight: true,
//            height: 100,
            disabledClass: "newtripcmbss",
            border: false,
            scope: this,
            items: [
                {
                    layout: 'form',
                    defaults: {
                        border: false, labelWidth: 150, width: 200

                    },
                    baseCls: 'northFormFormat',
                    cls: "visibleDisabled",
                    items: [this.bomCode, this.bomName, this.isDefaultBOM

//                        {
//                            layout: 'column',
//                            defaults: {
//                                border: false
//                            },
//                            items:[
//                                {
//                                    layout: 'form',
//                                    columnWidth: 0.55,
//                                    items: [
//                                        this.bomCode,
//                                        this.bomName,
//                                        this.linkProductCombo,
//                                        this.workCenter
//                                    ]
//                                },{
//                                    layout: 'form',
//                                    columnWidth: 0.45,
//                                    items: [
//                                        this.linkAlternateBOM,
//                                        this.alternateBOMCode,
//                                        this.subBOM
//                                    ]
//                                }
//                            ]
//                        }

                    ]
                }
            ]
        });
    },
    createWestPanel: function(arr) {

        if (!this.isCAComponent) {
            this.bomStore = new Wtf.data.SimpleStore({
                fields: ['bomCode', 'bomName', "isdefaultbom", "bomAssemblyDetails"],
                data: []
            });
            
            this.bomCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
                mode: 'local',
                valueField: 'bomCode',
                displayField: 'bomName',
                store: this.bomStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.product.BOM.select.BOM"),
                toolTip: WtfGlobal.getLocaleText("acc.product.BOM.select.BOM"),
                emptyText: WtfGlobal.getLocaleText("acc.product.BOM.select.BOM"),
                hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag),
                hidden: !(Wtf.account.companyAccountPref.activateMRPManagementFlag),
                width: 130,
                typeAhead: true,
                forceSelection: true
            });
            this.bomCombo.on("change", function(combo, newvalue, oldvalue) {
                var recordIndex = combo.store.findBy(
                    function(record, id) {
                        if (record.get('bomCode') === combo.value) {
                            return true;
                        }
                        return false;
                    }
                    );
                if (recordIndex != -1) {
                    var rec = combo.store.getAt(recordIndex);
                    this.loadBOMDetails(rec.data.bomid);
                }
            }, this);
            
        }
        if (this.isCAComponent) {
            if (this.westPanel) {
                this.westPanel.remove(this.westBOMTab);
            }
        }
        this.westBOMTab = new Wtf.tree.TreePanel({
            region: 'west',
//            autoScroll: true,
            width: 240,
            height: 680,
//            enableDD: true,
            border: false,
            containerScroll: true,
            dropConfig: {appendOnly: true}
        });
        var text="";
        var id = "";
        var productType = "";
        var productid = "";
        if (this.isCAComponent) {
            if (arr) {
                text = arr[4];
                id = arr[0];
                productType = arr[1];
                productid = arr[0];
            } else {
                text = this.record != undefined && this.record.data != undefined ? this.record.data.productname : '';
                id = this.record != undefined && this.record.data != undefined ? this.record.data.productid : 'root';
                productType = this.record != undefined && this.record.data != undefined ? this.record.data.type : "";
                productid = this.record != undefined && this.record.data != undefined ? this.record.data.type : "";
            }
        } else {
            text = this.record != undefined && this.record.data != undefined ? this.record.data.productname : '';
            id = this.record != undefined && this.record.data != undefined ? this.record.data.productid : 'root';
            productType = this.record != undefined && this.record.data != undefined ? this.record.data.type : "";
            productid = this.record != undefined && this.record.data != undefined ? this.record.data.type : "";
        }
        this.rootNode = new Wtf.tree.TreeNode({
            text: text,
            id: id,
            productType: productType,
            draggable: false, // disable root node dragging
            productid: productid,
            expanded: true,
            listeners: {
                scope: this,
                click: this.showDetails.createDelegate(this)
            }
        });
        this.westBOMTab.setRootNode(this.rootNode);
//        this.loadBOMDetails();
        var arr = []
        if (this.isCAComponent) {
            arr = [this.westBOMTab];
        } else {
            arr = [this.bomCombo, this.westBOMTab]
        }
        
        
        if (this.westPanel) {
            if (this.isCAComponent) {
                this.westPanel.add(this.westBOMTab);
                this.westPanel.doLayout();
            } 
        } else {
            this.westPanel = new Wtf.form.FormPanel({
                region: 'west',
                border: false,
                width: 240,
                height: 480,
                autoScroll:true,
                hidden: !Wtf.account.companyAccountPref.activateMRPManagementFlag,
                items: arr
            });
        }

    },
    loadBOMDetails: function(bomid,pid) {
        if ((((this.record != undefined && this.record.data != undefined ? this.record.data.productname : '') != "")||(pid!=undefined)) && Wtf.account.companyAccountPref.activateMRPManagementFlag) {
            var params = {
                productid: (this.record && this.record.data && this.record.data.productid)?this.record.data.productid:(pid?pid:"")
            };
            if (bomid != undefined && bomid != "") {
                params.bomid = bomid;
            }
            Wtf.Ajax.requestEx({
                url: "ACCProductCMN/getProductRecipes.do",
                params: params
            }, this, function(response) {
                if (response != undefined && response.data != undefined) {
                    this.rootNode.eachChild(function(child) {//remove each child from root node
                        this.rootNode.removeChild(child);
                    }, this);
                    for (var i = 0; i < response.data.length; i++) {
                        var record = response.data[i];
                        var treeNode = new Wtf.tree.TreeNode({
                            text: record.text,
                            id: (record.id != undefined && record.id != "") ? record.id : record.productid,
                            cls: 'paddingclass',
                            leaf: false,
                            bomid: record.bomid,
                            expanded: true,
                            productType: record.producttype,
                            productid: record.productid,
                            listeners: {
                                scope: this,
                                click: this.showDetails.createDelegate(this)
                            }
                        });
                        if (record.parentid && this.westBOMTab.getNodeById(record.parentid) != undefined) {
                            this.westBOMTab.getNodeById(record.parentid).appendChild(treeNode);
                        } else {
                            this.rootNode.appendChild(treeNode);
                        }
                    }
                    this.rootNode.expand();
                }
            }, function(response) {

            });
        }
    },
    // for creating bom grid for saved bom records
    createGridOfBOMSaved: function() {

        this.gridRecord = new Wtf.data.Record.create([
            {name: 'bomCode'},
            {name: 'bomid'},
            {name: 'bomName'},
            {name: 'linkProductId'},
            {name: 'linkProductName'},
            {name: 'workCenter'},
            {name: 'linkAlternateBOMId'},
            {name: 'linkAlternateBOMName'},
            {name: 'alternateBOMCode'},
            {name: 'isdefaultbom'},
            {name: 'subBOMCode'},
            {name: 'bomAssemblyDetails'}
        ]);

        this.gridReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "count",
            remoteGroup: true,
            remoteSort: true
        }, this.gridRecord);

        this.createdBOMStore = new Wtf.data.Store({
            url: "ACCProduct/getBOMDetails.do",
            reader: this.gridReader
        });
        this.createdBOMStore.load({
            params: {
                productid: (this.record && this.record.data && this.record.data.productid)?this.record.data.productid:""
            }
        }, this);
        this.comboLoaded = false;
        this.createdBOMStore.on("load", function() {
            this.createdBOMStore.each(function(rec) {
                if (!this.comboLoaded) {
                    this.bomCombo.store.add(rec);
                }
                if (rec != undefined && rec != null && rec.data['isdefaultbom']) {
                    this.bomCreationForm.getForm().loadRecord(rec);
                    // remove all details in grid
//                        this.AssemblyGrid.itemsgrid.getStore().removeAll();
                    // load bom details in bom grid
//                        this.loadSavedBOMAssemblyDetails(rec);
                    // for updating subtotal of bom grid
                    this.AssemblyGrid.updateSubtotal();
                    this.AssemblyGrid.updateCostinAssemblyGrid();
                    if (!this.comboLoaded) {
                        this.bomCombo.setValue(rec.data['bomCode']);
                        this.loadBOMDetails(rec.data['bomid']);
                    }
                }
            }, this);
            this.comboLoaded = true;
        }, this);
        
        this.sm = new Wtf.grid.RowSelectionModel({
            singleSelect: true
        });
        this.cm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText('acc.field.bomCode'), // "BOM Code",
                dataIndex: 'bomCode',
                width: 150
            }, {
                header: WtfGlobal.getLocaleText('acc.field.bomName'), // "BOM Name",
                dataIndex: 'bomName',
                width: 150
            }, {
                header: WtfGlobal.getLocaleText('acc.mrp.field.isdefaultbom'), // "Work Center",
                dataIndex: 'isdefaultbom',
                width: 150
            }, {
                header: WtfGlobal.getLocaleText('acc.field.linkProductName'), // "Link Product Name",
                dataIndex: 'linkProductName',
                width: 150,
                hidden: true
            }, {
                header: WtfGlobal.getLocaleText('acc.field.workCenter'), // "Work Center",
                dataIndex: 'workCenter',
                width: 150,
                hidden: true
            }, {
                header: WtfGlobal.getLocaleText('acc.field.linkAlternateBOM'), // "Link Alternate BOM",
                dataIndex: 'linkAlternateBOMName',
                width: 150,
                hidden: true
            }, {
                header: WtfGlobal.getLocaleText('acc.field.alternateBOMCode'), // "Alternate BOM Code",
                dataIndex: 'alternateBOMCode',
                width: 150,
                hidden: true
            }, {
                header: WtfGlobal.getLocaleText('acc.field.subBOMCode'), // "Sub BOM Code",
                dataIndex: 'subBOMCode',
                width: 150,
                hidden: true
            }
        ]);

        this.createdBOMGrid = new Wtf.grid.GridPanel({
            id: 'createdBOMGrid' + this.id,
            store: this.createdBOMStore,
            hidden: !Wtf.account.companyAccountPref.activateMRPManagementFlag,
            cm: this.cm,
            border: false,
            sm: this.sm,
            trackMouseOver: true,
            loadMask: {
                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
            },
            height: 200,
            style: 'margin-left:10px; margin-right:10px',
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.createdBOMGrid.on('render', function() {
            this.createdBOMGrid.view.refresh.defer(1, this.createdBOMGrid.view); /* Refresh the empty text of grid without load the store */
        }, this);

        this.createdBOMGrid.on("rowclick", this.rowClickHandle, this);
    },
    // grid row click handler
    rowClickHandle: function(grid, rowindex, e) {
        var rec = this.createdBOMGrid.getSelectionModel().getSelected();
        if (rec != undefined && rec != null) {
            this.bomCreationForm.getForm().loadRecord(rec);
            if (rec.data.subBOMCode && this.subBOM.collapsed == true) {
                this.subBOM.toggleCollapse();
            }
            // remove all details in grid
            this.AssemblyGrid.itemsgrid.getStore().removeAll();
            // load bom details in bom grid
            this.loadSavedBOMAssemblyDetails(rec);
            // for updating subtotal of bom grid
            this.AssemblyGrid.updateSubtotal();
        }
    },
    loadSavedBOMAssemblyDetails: function(record) {
        var bomDetailRecords = "";
        if (record.data.bomAssemblyDetails != undefined && record.data.bomAssemblyDetails.length > 1) {
            bomDetailRecords = eval('(' + record.data.bomAssemblyDetails + ')');
        }
        var recordQuantity = bomDetailRecords.length;

        if (recordQuantity != 0) {
            for (var i = 0; i < recordQuantity; i++) {
                var bomDetailRecord = bomDetailRecords[i];
                var rec = new this.AssemblyGrid.gridRec(bomDetailRecord);
                rec.beginEdit();
                var fields = this.AssemblyGrid.gridStore.fields;

                for (var x = 0; x < fields.items.length; x++) {
                    var value = bomDetailRecord[fields.get(x).name];
                    if (fields.get(x).name == 'type' && value && value != '') {
                        value = decodeURI(value);
                    }
                    if (fields.get(x).name == 'productname' && value && value != '') {
                        value = decodeURI(value);
                    }
                    if (fields.get(x).name == 'desc' && value && value != '') {
                        value = decodeURI(value);
                    }
                    rec.set(fields.get(x).name, value);
                }

                rec.endEdit();
                rec.commit();
                this.AssemblyGrid.gridStore.add(rec);
            }
        }
//        this.AssemblyGrid.addBlankRecord(); // to add blank record at end of the grid
    },
    showDetails: function(node, event) {
        if (node != undefined && node.attributes != undefined && node.attributes.productType == "Inventory Assembly") {
            if (this.isCAComponent) {
                if (this.scope.quantity.getValue()) {
                    this.CAStore.load({
                        params: {
                            productid: node.attributes.productid,
                            mrproductquantity:this.scope.quantity.getValue(),
                            bomdetailid:this.scope.materialIDCombo.getValue()
                        }
                    }, this);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please, provide some quantity"], 2);
                }
            } else {
                this.AssemblyGrid.setProductWithDefaultBOM(node.attributes.productid);
                this.createdBOMStore.load({
                    params: {
                        productid: node.attributes.productid
                    }
                }, this);
            }
        } else {
            return;
        }
//        if (node != undefined && node.attributes!=undefined && node.attributes.productType == "Inventory Assembly") {
//             var recordArray=new Array();
//             this.AssemblyGrid.gridStore.removeAll();
//             for(var i=0;i<this.gridData.length;i++){
//                 var rec=this.gridData[i];
//                 if(rec.parentid!=undefined && rec.parentid!="" && rec.parentid==node.attributes.productid){
//                    this.AssemblyGrid.gridStore.insert(this.AssemblyGrid.gridStore.getCount(), new this.AssemblyGrid.gridRec(rec)); 
//                 }
//             }
//        } else {
//            return;
//        }
    },
     createCAGrid: function() {
        this.createCAGridBBar();     // call Function to create Bottom bar fo component Availability Grid
        this.CAGridRec = Wtf.data.Record.create ([
        {
            name:'id'
        },   
        {
            name:'wodetailid'
        },
        {
            name:'productid'
        },
        {
            name:'pid'
        },
        {
            name:'productname'
        },

        {
            name:'desc'
        },

        {
            name:'type'
        },

        {
            name:'purchaseprice'
        },

        {
            name:'availablequantity'
        },

        {
            name:'requiredquantity'
        },

        {
            name:'shortfallquantity'
        },
        {
            name:'outstandingquantity'
        },
        {
            name:'blockquantity'
        },

        {
            name:'orderquantity'
        },

        {
            name:'genpo'
        },
        {
            name:'genpocheck'
        },
        {
            name:'level'
        }
        ,
        {
            name:'location'
        }
        ,
        {
            name:'warehouse'
        }
        ,
        {
            name:'POCode'
        }
        ,
        {
            name:'minpercentquantity'  
        }
        ,
        {
            name:'parentproductid'
        }
        ,
        {
            name:'batchdetails'
        }
        ,
        {
            name:'serialwindow'
        }
        ,
        {
            name:'isLocationForProduct'
        }
        ,
        {
            name:'isWarehouseForProduct'
        }
        ,
        {
            name:'isBatchForProduct'
        }
        ,
        {
            name:'isSerialForProduct'
        }
        ,
        {
            name:'isRowForProduct'
        }
        ,
        {
            name:'isRackForProduct'
        }
        ,
        {
            name:'isBinForProduct'
        },
        {
            name:'availQtyofdefaultlocwarehouse'    //ERP-37246 : Available Quantity at Default Warehouse and/or location
        }
        ]);
        var url="ACCProduct/getAssemblyItems.do";//this url
        if (this.woScope != undefined && this.woScope.isEdit) {
            url = "ACCWorkOrder/getWorkOrderComponentDetails.do";
        }
        this.CAStore = new Wtf.data.Store({
            url:url,
            baseParams:{
                isForCompAvailablity:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.CAGridRec)
        });             
        this.CAsm = new Wtf.grid.CheckboxSelectionModel({
//            singleSelect : true // Assigning multiselect to selection model
        });
        this.CAsm.on('selectionchange',this.enableDisableCAGridBBarBtns,this);    // On changing selections of CA grid, calling bbar buttons enable, disable function
        this.CAcm = [];
        this.CAcm.push(new Wtf.grid.RowNumberer(),this.CAsm);
        /*If isCAComponent is true then only Product ID column added*/
        if(this.isCAComponent){
            /*Added new column to grid as Product ID*/
            this.CAcm.push(
                {
                    header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.product.threshold.grid.productID")+"\">"+WtfGlobal.getLocaleText("acc.product.threshold.grid.productID")+"<div>",
                    dataIndex:'pid',
                    align:'left',
                    pdfwidth:150,
                    width:150
                }
            );
        }
            
        this.CAcm.push(
            {    // added tooltip to header
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.product.threshold.grid.productname")+"\">"+WtfGlobal.getLocaleText("acc.product.threshold.grid.productname")+"<div>",
                dataIndex:'productname',
                align:'left',
                pdfwidth:150,
                width:150,
                renderer:function(v,m,rec) {
                    var cnt = rec.data.level;
                    var newVal = "";
                    for(var index = 0; index < cnt; index++) {
                        newVal += "&emsp;"
                    }
                    newVal += v;
                    if (rec.data.type == "Inventory Assembly") {
                        newVal = "<b>" + newVal + "</b>";
                    }
                    return newVal;
                }
            },
            {
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header2")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header2")+"<div>",
                dataIndex:'desc',
                align:'left',
                width:150,
                pdfwidth:150
            },
            {
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header3")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header3")+"<div>",
                dataIndex:'type',
                align:'left',
                width:150,
                pdfwidth:150
            },
            
            {
                dataIndex:'availablequantity',
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header5")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header5")+"<div>",
                align:'left',
                width:150,
                pdfwidth:150,
                renderer: this.unitRenderer
            },
            {
                dataIndex:'outstandingquantity',
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header10")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header10")+"<div>",
                align:'left',
                width:150,
                pdfwidth:150,
                renderer: this.unitRenderer
            },
            {
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header6")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header6")+"<div>",
                dataIndex:'requiredquantity',
                align:'left',
                width:150,
                pdfwidth:150,
                renderer: this.unitRenderer
            },
            {
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header11")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header11")+"<div>",
                dataIndex:'blockquantity',
                align:'left',
                width:150,
                pdfwidth:150,
                renderer: this.unitRenderer,           
                editor:new Wtf.form.NumberField({
                        allowBlank: false,
                        allowNegative: false,
                        decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
                    })
            },
            {   
                header: '',
                align:'center',
                renderer: this.serialRenderer.createDelegate(this),
                dataIndex:'serialwindow',
                id:this.id+'serialwindow',
                width:40
            },
            {
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header12")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header12")+"<div>",
                dataIndex:'minpercentquantity',
                align:'left',
                width:150,
                pdfwidth:150,
                renderer: this.unitRenderer,     
                editor:new Wtf.form.NumberField({
                        allowBlank: false,
                        allowNegative: false,
                        decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
                    })
            },
            {   
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header7")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header7")+"<div>",
                dataIndex:'shortfallquantity',
                align:'left',
                width:150,
                pdfwidth:150,
                renderer: this.unitRenderer
            },
            {
                header:"<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header8")+"\">"+WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header8")+"<div>",
                dataIndex:'orderquantity',
                align:'left',
                width:150,
                pdfwidth:150, 
                renderer: this.unitReorderRenderer
            }
            
        );
        var genpoheader = "";
        if (Wtf.account.companyAccountPref.autoGenPurchaseType == 0) {  // checking flag for generating PO or PR
           genpoheader = WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header13");
        } else {
           genpoheader = WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header14");
        }
        var genPOPR= new Wtf.CheckColumnComponent({   
            dataIndex: 'genpocheck',
            header:"<div  wtf:qtip=\""+genpoheader+"\">"+genpoheader+"<div>",
            width: 200,
            align:'center',
            scope:this
        });
        this.pluginArr = [];
        this.pluginArr.push(genPOPR);
        this.CAcm.push(genPOPR);
        this.CAGrid = new  Wtf.grid.EditorGridPanel({
            layout: 'fit',
            region: "center",
            height:300,
            bodyStyle: 'padding-left:10px',//
            store: this.CAStore,
            columns: this.CAcm,
            autoScroll:true,
            border: false,
            loadMask: true,
            bbar:this.cabtnarr,
            sm: this.CAsm,
         // forceFit:true,
            viewConfig: {
//                forceFit: true,
            getRowClass: function(r) {
                
                /* 
                 * DISABLED flag true only if:
                 * work order is Started.
                 * Product is Assembly Product.
                 * Block quantity not 0 for Inventory part.
                
                 * If Disabled flag is true for Product in Component Availability window then:
                 * Disable that complete product ROW. 
                 */
                  if (r.json.Disabled){                       
                        return 'x-item-disabled';
                    }else{ 
                        return '';
                    }
            },
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.CAGrid.on("afteredit",function(e){
            
            var rec = e.record;
            if(e.field=="blockquantity"){
                var availQuantity = e.record.data.availablequantity;      
                var originalBlockValue = parseFloat((getRoundofValue(e.originalValue)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                var blockQuantity = parseFloat((getRoundofValue(e.record.data.blockquantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                var requiredquantity = parseFloat((getRoundofValue(e.record.data.requiredquantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                var minpercentquantity = parseFloat((getRoundofValue(e.record.data.minpercentquantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                var minrequiredquantity= parseFloat(getRoundofValue((getRoundofValue((requiredquantity * minpercentquantity)))/100).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                var outstandingquantity = e.record.data.outstandingquantity;
                /**
                 * skip 0 Block quantity from validation
                 */
                if (blockQuantity != 0 && blockQuantity > requiredquantity) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.blockQuantity.err.msg1")], 2);
                    rec.set("blockquantity",originalBlockValue);
                }else if(blockQuantity != 0 && blockQuantity < minrequiredquantity){
                    if(minpercentquantity==100){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.blockQuantity.err.lessthanrequired")], 2);
                        rec.set("blockquantity",originalBlockValue);
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.blockQuantity.err.lessthanminimumrequired")], 2);
                        rec.set("blockquantity",originalBlockValue);
                    }
                }else {
                    if (availQuantity < blockQuantity) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.blockQuantity.err.msg")], 2);
                        rec.set("blockquantity",0.0);
                    }
                }  
            }
            /**
             * applied vaidation for minimum percent quantity to keep in between range of 100
             */
            if (e.field == "minpercentquantity") {
                if (rec.data.minpercentquantity <= 0 || rec.data.minpercentquantity > 100) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.minpercentquantity.err.msg")], 2);
                    rec.set("minpercentquantity", e.originalValue);
                } else {
                    rec.set("blockquantity", 0.0);
                }
            }
                
        },this);
        this.CAGrid.on("cellclick",function(grid,rowIndex,columnIndex,e){
            var record = grid.getStore().getAt(rowIndex);  // Get the Record
            var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
            var data=record.get(fieldName);
            if(fieldName=="blockquantity")
            {
                   
              var dataBlockquantity = parseFloat(getRoundofValue(record.get(fieldName))).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
              record.set("blockquantity",dataBlockquantity);
                   
            }
            var sfquantity = parseInt(record.get("shortfallquantity"));
            if (fieldName == "genpocheck") {
                record.set("genpocheck", !data);
            }
            
        },this);
        this.CAGrid.on('rowclick',this.handleRowClick,this);  // Calling function handleRowClick When row click event is fired.
        if (this.woScope != undefined && this.woScope.isEdit && this.woScope.record!=undefined) {
            this.CAStore.load({
                params: {
                    bills: this.woScope.record.data.id,
                    isFromWO:true
                }
            },this);     
            var arr = [];
            arr.push(this.woScope.record.data.productid);
            arr.push(this.woScope.record.data.type);
            arr.push(this.woScope.record.data.productid);
            arr.push(this.woScope.record.data.pid);
            arr.push(this.woScope.record.data.productname);
            this.createWestPanel(arr);
            this.loadBOMDetails(this.woScope.record.data.materialid, this.woScope.record.data.productid);
        }
    },
    unitRenderer: function (value, metadata, record) {
        if (record.data['type'] == "Service") {
            return "N/A";
        }
        value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        return value;
    },
      unitReorderRenderer: function (value, metadata, record) {
        if (record.data['type'] == "Service") {
            return "N/A";
        }
        value = parseFloat(getRoundofValue(record.data['orderquantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) +"(" + parseFloat(getRoundofValue(record.data['shortfallquantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) +")";
        return value;
    },
    /*
     * Purpose: Creating Component Availability Bottom Bar Grid
     * Date: Jun 14 2016
     * Author: Sayed Kausar Ali
     */
    createCAGridBBar: function() {
        this.cabtnarr = [];
        // ---------------Refresh Button-------------------
         this.refreshBttn = new Wtf.Toolbar.Button({      
            text: WtfGlobal.getLocaleText("acc.field.ResetAccount"), 
            tooltip: WtfGlobal.getLocaleText("acc.field.ResetAccount"),
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler: function(){
                this.refreshCAGrid(true);              // On Click Calling Function to refresh a Grid
            }
        });
        // ---------------- As Required Quantity Button ----------------
        this.setToReqBtn = new Wtf.Action({       
            text: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty.setToReqQty"),
            id:'ReqBlockQtyBtn' + this.id,
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty.setToReqQty"),
            handler: function() {
                this.UpdateMassCAGrid(Wtf.massUpdateCAGridTypes.asReqQty); // On Click Calling Fucntion to Update Block quantity with type = 0
            },
            iconCls: getButtonIconCls(Wtf.etype.copy)
        });
        // ---------------- As Minimum Percent of Requried Quantity Button ----------------
        this.setToMinPerReqQty = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty.setToMinPerReqQty"),
            id:'MinPerReqQtyBtn' + this.id,
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty.setToMinPerReqQty"),
            handler: function() {
                this.UpdateMassCAGrid(Wtf.massUpdateCAGridTypes.asMinPerOfReqQty);    // On Click Calling Fucntion to Update Block quantity with type = 1
            },
            iconCls: getButtonIconCls(Wtf.etype.copy)
        });
        // ----------------Custom Block Quantity Button ----------------
        this.customBlockBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty.customBlockQty"),
            id:'customBlockQtyBtn' + this.id,
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty.customBlockQty"),
            handler: function() {
                this.callCAGridCustomMassUpdateWin(this,Wtf.massUpdateCAGridTypes.custBlockQty); // On Click Calling Fucntion to open window for fetching custom value of block quantity with type = 2
            },
            iconCls: getButtonIconCls(Wtf.etype.copy)
        });
        
        // ---------------- Creating Block Quantity Menu----------------
        this.blockQtyBtnArr = [];    
        // -------------- Pushing Buttons in Block Quantity Menu -------------
        this.blockQtyBtnArr.push(this.setToReqBtn);
        this.blockQtyBtnArr.push(this.setToMinPerReqQty);
        /**
         * will be added as funtionality is fully functional
         */
//        this.blockQtyBtnArr.push(this.customBlockBtn);
        
        // -------------- Block Quantity Menu Button: Contains Buttons to mass Update Block Quanity Column of CA Grid --------------
        this.blockQtyBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty"),
            id:'blockQtyBtn' + this.id,
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty"),
            menu:this.blockQtyBtnArr,                                       // assigning Menu to this Button
            iconCls: getButtonIconCls(Wtf.etype.copy)
        });
        // -------------- Minimum Percent Button: Button to mass Update Minimum percent Column of CA Grid  --------------
        this.minPercentBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.minPercent"),
            id:'minPercentBtn' + this.id,
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.minPercent"),
            handler: function() {
                this.callCAGridCustomMassUpdateWin(this,Wtf.massUpdateCAGridTypes.minPercent); // On Click Calling Fucntion to open window for fetching custom value of block quantity with type = 3
            },
            iconCls: getButtonIconCls(Wtf.etype.copy)
        });
        
        // ------------------- Creating Mass Update Menu ---------------
        this.massUpdateArr = [];
        // ---------------- Pushing Buttons in Mass Update Menu -----------
        this.massUpdateArr.push(this.blockQtyBtn);
        this.massUpdateArr.push(this.minPercentBtn);
        
        // ------------------ Mass Update Menu Button --------------------
        this.massUpdateMenu = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu"),
            scope: this,
            disabled:true,
            id:'massUpdateMenu' + this.id,
            tooltip: WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu"),
            iconCls: getButtonIconCls(Wtf.etype.inventorydst),
            menu: this.massUpdateArr                             // Assigning menu to button
        });
        this.cabtnarr.push(this.refreshBttn);
        this.cabtnarr.push('-',this.massUpdateMenu);
    },
    POPRRenderer: function(v) {
        if (v=="yes") {
            if (Wtf.account.companyAccountPref.autoGenPurchaseType == 0) {
                return "<a href='#'>Generate PO</a>";
            } else {
                return "<a href='#'>Generate PR</a>";
            }
        }
    },
//    getCAJSON: function() {
//        var jsonstring = "";
//        this.CAStore.each(function(rec) {
//            var batchdetails = "";
//            if (rec.data.batchdetails) {
//                batchdetails = rec.data.batchdetails;
//            }
//            jsonstring += "{productid:\"" + rec.data['productid'] + "\"," +
//                    "initialpurchaseprice:" + rec.data['purchaseprice'] + "," + "requiredquantity:" + rec.data['requiredquantity'] + "," + "availablequantity:" + rec.data['availablequantity'] ;
//                jsonstring+=  "," + "blockquantity:" + rec.data['blockquantity'];
//                jsonstring+=  "," + "minpercentquantity:" + rec.data['minpercentquantity'];     // adding percent quantity and parent product id to save json
//                jsonstring+=  "," + "batchdetails:" + batchdetails;     // adding percent quantity and parent product id to save json
//                jsonstring+=  "," + "parentproductid:\"" + rec.data['parentproductid'] +"\"";
//            jsonstring += "},";
//        }, this);
//        jsonstring = jsonstring.substr(0, jsonstring.length - 1);
//        return jsonstring;
//    },
    getCAJSON: function() {
        var jsonstring = "";
        var jsonObj = {};
        var jsonArr = [];
        var arr = [];
        var finaltotal=0;
        /**
         * Calculate Initial Purchase price of Co-product or Scrap product Based on Coponent type product(%Rate of (Sum of component type product price)).
         * This will work only if MRP is Activate and mrpProductComponentType is Activate.
         * Initial Purchase price of Component product same as previous.
         **/
        if((Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType))
        {
        
            this.CAStore.each(function(rec) {
            
                if(rec.json.componentType==1)
                {

                    finaltotal+=(rec.data.purchaseprice*rec.data.blockquantity);
                
                }
            
            }, this);
        }
        
        this.CAStore.each(function(rec) {
            var batchdetails = "";
            jsonObj = rec.data;
           
            
           if((Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType))
           {
                if(rec.json.componentType==2 || rec.json.componentType==3)
                {
                    var cstotal=(rec.json.crate/100)*finaltotal;
                    jsonObj.initialpurchaseprice = cstotal;
                
                
                }else{
                    jsonObj.initialpurchaseprice = rec.data.purchaseprice;
            
                }  
          
            }else{
                jsonObj.initialpurchaseprice = rec.data.purchaseprice;
                
            }  
            /* If Disabled flag is true for Product in Component Availability window then:
             * don't pass DISABLED product information to JAVA side for updation. 
             */
                
            if(rec.json.Disabled==undefined || rec.json.Disabled=="" || rec.json.Disabled==false){   
                    jsonArr.push(jsonObj);
            }
        }, this);
        jsonstring = JSON.stringify(jsonArr);
        return jsonstring;
    },
    
    /*
     * Purpose: To refresh Component Avaiability Grid
     * Date: Jun 14 2016
     * Author: Sayed Kausar Ali
     * Extra Note: CAStore has two URLs
     *              (a) From ProductController called when we create a new Work Order and when a Work order product or BOM is Changed on EDIT
     *              (b) From WorkOrderController called On EDIT
     */
    refreshCAGrid: function(isRefreshButton) {
        if(this.CAStore.getCount()==0 || isRefreshButton){
            if (this.woScope.isBOMChanged) { // if BOM is Changed i.e. on creating new WO or Changing BOM on EDIT
                this.CAStore.load({          // setting params as required in ProductController Function
                    params: {
                        productid: this.scope.productNameCombo.getValue(),
                        mrproductquantity:this.scope.quantity.getValue(),
                        bomdetailid:this.scope.materialIDCombo.getValue()
                    }
                }, this);

            } else {
                this.CAStore.load({       // Else setting Params as required in WorkOrderController Function
                    params: {
                        bills: this.woScope.record.data.id,
                        isFromWO:true
                    }
                },this);
            }
        }
    },
    
    /*
     * Purpose: Creating Window for fetching custom values of Block Quantity and Minimum Percent
     * Date: Jun 14 2016
     * Author: Sayed Kausar Ali
     */
    callCAGridCustomMassUpdateWin: function(obj,type) {
        var textLabel = "";
        var maxVal;
        // ----- Setting field Label  and max Value for the number field depending on type. If type = 3 (minPercent) maxValue is 100 else default
        if (type == Wtf.massUpdateCAGridTypes.minPercent) {
            textLabel = WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.minPercent");
            maxVal = 100;
        }if (type == Wtf.massUpdateCAGridTypes.custBlockQty) {
            textLabel = WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty");
            maxVal = Number.MAX_VALUE;
        }
        
        // ----------- Number Field to Fetch values -----------
        this.custValText = new Wtf.form.NumberField({
            fieldLabel:textLabel,
            maxValue:maxVal,
            name: 'custValText',
            id:"custValText"+this.id,
            hidden:false,
            allowBlank:false
        });  
        
//        // On Changing value if type = 3 (MinPercent) then value should not be greater than 100.
//        this.custValText.on('change',function(){
//            var val = this.custValText.getValue();
//            if (type == Wtf.massUpdateCAGridTypes.minPercent ) {
//                if (val > 100) {
//                    this.custValText.setValue(100); // Setting Value to 100 if it gets greater than 100
//                }
//            }
//        },this);
        
        // ------------- Just a simple Panel: contains that number Field ---------------
        this.CAGridCustomMassUpdatePanel = new Wtf.form.FormPanel({
            border:false,
            lableWidth : 50,
             bodyStyle : 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
            items:[this.custValText]
        })
        
        //-------------- Mass Update Window contains above Panel ---------------
        this.CAGridCustomMassUpdateWin = new Wtf.Window({
            name:'CAGridCustomMassUpdateWin',
            modal: true,
            title: "Enter " + textLabel,
            buttonAlign: 'right',
            width: 370,
            height:120,
            layout:'fit',
            CAscope: obj,
            draggable:false,
            scope:this,
            resizable:false,
            items:[this.CAGridCustomMassUpdatePanel],
            buttons: [{
                text: 'Submit',
                scope: this,
                handler: function() {
                    this.UpdateMassCAGrid(type);      //  On Click Calling Fucntion to Update Block quantity or Min percent depending on type passed to this Function
                }
            },
            {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.CAGridCustomMassUpdateWin.close();  // Closing this Window
                }
            }]
        });
        
        this.CAGridCustomMassUpdateWin.show();
    },
    
    /*
     * Purpose: To Mass Update CA Grid.
     * Date: Jun 14 2016
     * Author: Sayed Kausar Ali
     */
    UpdateMassCAGrid:function(type) {
        var count = this.CAGrid.getSelections().length;  // Fetching Grid Selections Count
        if ( count > 0) {                                // Checking if count is greater than Zero
            for (var index = 0; index< count; index++ ) {
                var rec = this.CAGrid.getSelections()[index]; // Fetching Grid Selections
                if (type === Wtf.massUpdateCAGridTypes.minPercent) {  
                    var val = this.custValText.getValue();
                    if (val > 100 || val < 0) {                                // If Value is greater than 100 showing error msg
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.minPercent.invalidMsg")], 2);
                        if (val < 0) {
                            this.custValText.setValue(0);                         // setting number field value to 0
                        } else {
                            this.custValText.setValue(100);                         // setting number field value to 100 
                        }
                        break;
                    } else {
                        rec.set("minpercentquantity",val);         // updating Min Percent for selected records
                        if (this.CAGridCustomMassUpdateWin) {
                            this.CAGridCustomMassUpdateWin.close();                                  
                        }
                    }
                } else if (type === Wtf.massUpdateCAGridTypes.asReqQty) {
                    rec.set("blockquantity",rec.data.requiredquantity);                // updating Block quantity as per Required Qty
                } else if (type === Wtf.massUpdateCAGridTypes.custBlockQty) {
                    var val = this.custValText.getValue();
                    if (val < 0) { 
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty.invalidMsg2")], 2);
                        this.custValText.setValue(0);
                        break;
                    } else {
                        rec.set("blockquantity",this.custValText.getValue());              // updating Block Quantity as per custom qty provided in number field
                        if (this.CAGridCustomMassUpdateWin) {
                            this.CAGridCustomMassUpdateWin.close();                                  
                        }
                    }
                } else if (type === Wtf.massUpdateCAGridTypes.asMinPerOfReqQty) {
                    var reqQty = rec.data.requiredquantity;
                    var percent = rec.data.minpercentquantity;
                    rec.set("blockquantity",(reqQty * (percent / 100)));     // updating Block quantity as per min percent of Required Qty i.e if minpercent is 50 and required is 10 t
                }
                rec.commit();                                                          // Commiting that record
                this.CAGrid.getView().refresh();                                       // refreshing the view of Grid
            }
            if (type !== Wtf.massUpdateCAGridTypes.minPercent) {
                this.validateBlockQty();
            }
        }
    },
    
    /*
     * Purpose: To enable Disable buttons on Grid selection change
     * Date: Jun 14 2016
     * Author: Sayed Kausar Ali
     */
    enableDisableCAGridBBarBtns: function() {
        var selectionModel = this.CAGrid.getSelectionModel();                   // Fetching Selection Model
        if (selectionModel.hasSelection() && selectionModel.getCount() == 1) {  // if 1 rec is selected
            if (this.massUpdateMenu) {
                this.massUpdateMenu.enable();                                   // enabling Mass Update Button
            }
        } else if(selectionModel.getCount() > 1) {                              // If more than 1 rec is selected 
            if (this.massUpdateMenu) {
                this.massUpdateMenu.enable();                                   // enabling Mass Update Button
            }
        } else if (selectionModel.getCount() == 0) {                            // If No rec is Selected
            if (this.massUpdateMenu) {
                this.massUpdateMenu.disable();                                  // disabling Mass Update Button
            }
        }    
    },
    
    /*
     * Purpose: To Validate Block Quantity on Mass Update
     * Date: Jun 14 2016
     * Author: Sayed kausar Ali
     */
    validateBlockQty: function() {
        var isBlockQtyValid = true;                                             // Flag to check if any block quantity is invalid
        var invalidProds = "";
        var count = this.CAGrid.getSelections().length;                         // fetching selction count
        if ( count > 0) {                                                       // checking if count is greater than 0
            for (var index = 0; index< count; index++ ) {
                var rec = this.CAGrid.getSelections()[index];                   // fetching record
                var blockQty = rec.data.blockquantity;
                var availQty = rec.data.availablequantity;
                if (availQty < blockQty) {                                      // if available qty is less than block qty
                    isBlockQtyValid = false;                                    // setting flag to false
                    invalidProds += " <b>" + rec.data.productname + "</b>&emsp;";// adding product name to msg String
                    rec.set("blockquantity",0);                                 // Setting Block Qty to Zero for that particular record
                    rec.commit();
                }
            }
        }
        
        // If flag is fasle : showing a message to inform user about that.
        if (!isBlockQtyValid) {
            var msg = WtfGlobal.getLocaleText("acc.mrp.WO.CA.massUpdateMenu.blockQty.invalidMsg");
            msg += invalidProds;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        }
    },
    serialRenderer:function(v,m,rec){
//        var batchDetailArr = [];
//        var isBatchPresent= false;
//        if (rec.data.batchdetails) {
//            isBatchPresent = true;
//            batchDetailArr = eval(rec.data.batchdetails);
//        }
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title.workOrder")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },    
    handleRowClick:function(grid,rowindex,e){
        var store=grid.getStore();
        var record = store.getAt(rowindex); 
        
        /*
         * For DISABLED product:
         * do'not allow to do MASS update.
         * After Row click display proper message and deselect that row.
         * Mass update option Remain DISABLED for DISABLED product.
         */
        
        if((record.json.Disabled) && (record.json.producttype==Wtf.producttype.assembly))
        {
                this.CAsm.deselectRow(rowindex);
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mrp.cannotchangeassembly.alert")],2);   
                return;
        }else{
            if(record.json.Disabled){
                 this.CAsm.deselectRow(rowindex);
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mrp.cannotchangetaskcomplete.alert")],2);   
                 return;
            }
        }
        
        if((Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.mrpProductComponentType) && (record.json.componentType==2 || record.json.componentType==3))
        {       
                 /**  
                   * if product is co-product or scrap not allow to edit the row.  
                   *  
                   *  */
                 this.CAsm.deselectRow(rowindex);
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mrp.coproductscrap.alert")],2);   
                 return;
                
        }
        else
        {
            if(e.getTarget(".serialNo-gridrow")){
                var productid = record.get('productid');
                record.Wtrantype=20;//transaction type 20 for work order
                if(record.data.type!='Service' && record.data.type!='Non-Inventory Part'){  
                        if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){    
                            if(record.data.isLocationForProduct || record.data.isWarehouseForProduct || record.data.isBatchForProduct || record.data.isSerialForProduct  || record.data.isRowForProduct || record.data.isRackForProduct  || record.data.isBinForProduct) {
                                this.callSerialNoWindow(record);
                            } else {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.Functinality")],2);   //Batch and serial no details are not valid.
                                return;
                            }
                        }
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")],2);   //Batch and serial no details are not valid.
                    return;
                }
            }
        }
    },
    callSerialNoWindow:function(obj){//if autogenerate flag is true then show serial no
        
            var deliveredprodquantity = obj.data.blockquantity;
            deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;

            var prorec = obj;
            var islocationavailble=false;
            var productsDefaultLocation="";
            if(prorec.data.isLocationForProduct && prorec.data.location!="" && prorec.data.location!=undefined){
                islocationavailble=true;
                productsDefaultLocation=prorec.data.location;
            }else if(!prorec.data.isLocationForProduct){
                islocationavailble=true;
            }
        
            var iswarehouseavailble=false;
            var productsDefaultWarehouse="";
            if(prorec.data.isWarehouseForProduct && prorec.data.warehouse && prorec.data.warehouse!=undefined){
                iswarehouseavailble=true;
                productsDefaultWarehouse=prorec.data.warehouse;
            }else if(!prorec.data.isWarehouseForProduct){
                iswarehouseavailble=true;
            }
            var filterJson='[';
            var urlinfo="";
            if (Wtf.account.companyAccountPref.activateMRPManagementFlag && this.isCAComponent && this.woScope.isEdit) {   //ERP-41405 : If MRP activated and call is from Component Availability
                urlinfo = "ACCInvoice/getBatchRemainingQuantityForMultipleRecords.do";
                var blockquantity = 0;
                if (prorec.data.producttype == Wtf.PRODUCT_TYPE_ASSEMBLY) {
                    blockquantity = prorec.json.blockquantity;
                } else {
                    blockquantity = prorec.data.blockquantity;
                }
                if (prorec.get("blockdetails")) {
                        var blockDetails = prorec.get("blockdetails");
                        blockDetails = eval(blockDetails);
                        for (var i = 0; i < blockDetails.length; i++) {
                            filterJson += '{"location":"' + blockDetails[i].location + '","warehouse":"' + blockDetails[i].warehouse + '","productid":"' + blockDetails[i].productid + '","documentid":"","purchasebatchid":"' + blockDetails[i].purchasebatchid + '","quantity":"' + blockquantity + '"},';
                        }
                } else {
                    //ERP-41405 - Send the entire details through filter JSON. For those which do not have value, keept it as empty.
                    filterJson += '{"location":"' + prorec.data.location + '","warehouse":"' + prorec.data.warehouse + '","productid":"' + prorec.data.productid + '","quantity":"' + blockquantity
                            + '","documentid":"","purchasebatchid":"","balance":"","batch":"","batchname":"","bin":"","customfield":"","documentbatchid":"","expdate":"' + ""
                            + '","expend":"","expstart":"","id":"","isreadyonly":"","purchaseserialid":"","rack":"","row":"","reusablecount":"","isserialusedinDO":"' + ""
                            + '","lockquantity":"","mfgdate":"","modified":"","packlocation":"","packwarehouse":"","serialno":"","serialnoid":"","skufield":"' + ""
                            + '","stocktype":"","wastageQuantity":"","wastageQuantityType":"","attachment":"","attachmentids":"","avlquantity":""},';
                }
                filterJson = filterJson.substring(0, filterJson.length - 1);
                filterJson += "]";
            } else {    //ERP-41405 : Regular Flow
                urlinfo = "ACCInvoice/getBatchRemainingQuantity.do";
                filterJson += '{"location":"' + productsDefaultLocation + '","warehouse":"' + productsDefaultWarehouse + '","productid":"' + prorec.data.productid + '","documentid":"","purchasebatchid":""},';
                filterJson = filterJson.substring(0, filterJson.length - 1);
                filterJson += "]";
            }
            
            if( islocationavailble || iswarehouseavailble ){ //if salesside and either default location and warehouse  then checkit
                Wtf.Ajax.requestEx({
                    url: urlinfo,
                    params: {
                        batchdetails:(this.woScope.isEdit)?((obj.data.batchdetails=="[]" || obj.data.batchdetails=="")?filterJson:obj.data.batchdetails):filterJson,
                        transType:20,
                        isEdit:this.woScope.isEdit
                    }
                },this,function(res,req){
                    if (Wtf.account.companyAccountPref.activateMRPManagementFlag && this.isCAComponent && this.woScope.isEdit) {   //ERP-41405 : If MRP activated and call is from Component Availability
                        if (res != undefined && res.data != undefined && res.data != "") {
                        var result = res;
                        var batchdetailsarr = '[';
                        for (var k = 0; k < result.data.length; k++) {
                        //ERP-40524 - Form the batchdetail array with correct data and send it to WL Batch window.
                            batchdetailsarr += '{"balance":"' + result.data[k].balance + '","batch":"' + result.data[k].batch + '","batchname":"' + result.data[k].batchname + '","bin":"' + result.data[k].bin + '","customfield":"' + result.data[k].customfield + '","documentbatchid":"' + result.data[k].documentbatchid + '","expdate":"' + result.data[k].expdate
                                + '","expend":"' + result.data[k].expend + '","expstart":"' + result.data[k].expstart + '","id":"' + result.data[k].id + '","isreadyonly":"' + result.data[k].isreadyonly
                                + '","isserialusedinDO":"' + result.data[k].isserialusedinDO + '","lockquantity":"' + result.data[k].lockquantity + '","mfgdate":"' + result.data[k].mfgdate + '","modified":"' + result.data[k].modified
                                + '","packlocation":"' + result.data[k].packlocation + '","packwarehouse":"' + result.data[k].packwarehouse + '","purchaseserialid":"' + result.data[k].purchaseserialid + '","reusablecount":"' + result.data[k].reusablecount
                                + '","rack":"' + result.data[k].rack + '","row":"' + result.data[k].row + '","serialno":"' + result.data[k].serialno + '","serialnoid":"' + result.data[k].serialnoid
                                + '","skufield":"' + result.data[k].skufield + '","stocktype":"' + result.data[k].stocktype + '","wastageQuantity":"' + result.data[k].wastageQuantity
                                + '","wastageQuantityType":"' + result.data[k].wastageQuantityType + '","attachment":"' + result.data[k].attachment + '","attachmentids":"' + result.data[k].attachmentids
                                + '","avlquantity":"' + result.data[k].avlquantity + '", "location":"' + result.data[k].location + '","warehouse":"' + result.data[k].warehouse
                                + '","productid":"' + result.data[k].productid + '","documentid":"' + result.data[k].documentid + '","purchasebatchid":"' + result.data[k].purchasebatchid
                                + '","quantity":"' + result.data[k].quantity + '"},';
                        }
                        batchdetailsarr = batchdetailsarr.substring(0, batchdetailsarr.length - 1);
                        batchdetailsarr += "]";
                        prorec.data.temporarybatchdetails = batchdetailsarr;//ERP-40524- Use temporary varible @temporarybatchdetails to send batch details to WL Batch window.                            
                        this.CallSerialnoDetailsWindow(prorec);
                    }
                    } else {
                        this.AvailableQuantity = res.quantity;
                        this.CallSerialnoDetailsWindow(obj);
                    }
                    return;
                },function(res,req){
                    return false;
                });
            }else{
                this.CallSerialnoDetailsWindow(obj);
            }
    },
    /**
     * to adjust balance quantity if consumed on currently on UI side.
     */
    adjustAvailableQuantityForMultipleProducts: function (productid) {
        var records = this.CAGrid.getStore().getRange();
        if (records) {
            for (var i = 0; i < records.length; i++) {
                /**
                 * take quantity of batchdetails only if product matches
                 */
                if (records[i].get("productid") == productid) {
                    var batchdetails = records[i].get("batchdetails");
                    if (batchdetails && batchdetails !== '[]') {
                        var batchDetailsArr = eval(batchdetails);
                        for (var element = 0; element < batchDetailsArr.length; element++) {
                            var value = batchDetailsArr[element].quantity;
                            this.AvailableQuantity -= parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        }
                    }
                }
            }
        }
    },
    CallSerialnoDetailsWindow:function(obj){   

        var deliveredprodquantity = obj.data.blockquantity;
        deliveredprodquantity = (deliveredprodquantity == "NaN" || deliveredprodquantity == undefined || deliveredprodquantity == null)?0:deliveredprodquantity;

        if(deliveredprodquantity<=0){
            WtfComMsgBox(["Info","Block Quantity should be greater than zero. "], 2);
            return false;
        }
        /**
         * adjust balance quantity if consumed on currently on UI side.
         */
        this.adjustAvailableQuantityForMultipleProducts(obj.get("productid"));
        var prorec = obj; 
        var parentGridRecords = this.CAGrid.getStore().getRange();
        this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:prorec.data.productname,
            isCAComponent:this.isCAComponent,
            uomName:prorec.data.uomname,   
            quantity:obj.data.blockquantity,
            defaultLocation:prorec.data.location, 
            productid:prorec.data.productid,
            isSales:this.isCustomer,
            isSales:true,
            moduleid:(obj.Wtrantype)?obj.Wtrantype:(this.isCustomer?Wtf.Acc_Delivery_Order_ModuleId:Wtf.Acc_Goods_Receipt_ModuleId),
            transType:this.isCustomer?Wtf.Acc_Delivery_Order_ModuleId:Wtf.Acc_Goods_Receipt_ModuleId,
            defaultAvailbaleQty:this.AvailableQuantity,
            isEdit:this.woScope.isEdit,
            isDO:this.isCustomer?true:false,
            defaultWarehouse:prorec.data.warehouse,
            batchDetails : (Wtf.account.companyAccountPref.activateMRPManagementFlag && this.isCAComponent && this.woScope.isEdit) ? (prorec.data.temporarybatchdetails!=undefined ? prorec.data.temporarybatchdetails : prorec.data.batchdetails) : prorec.data.batchdetails,  //ERP-41405 : Differentiated MRP CA call and Regular call
            warrantyperiod:prorec.data.warrantyperiod,   
            warrantyperiodsal:prorec.data.warrantyperiodsal,   
            isLocationForProduct:prorec.data.isLocationForProduct,
            isWarehouseForProduct:prorec.data.isWarehouseForProduct,
            isRowForProduct:prorec.data.isRowForProduct,
            isRackForProduct:prorec.data.isRackForProduct,
            isBinForProduct:prorec.data.isBinForProduct,
            isBatchForProduct:prorec.data.isBatchForProduct,
            isSerialForProduct:prorec.data.isSerialForProduct,
            copyTrans:this.copyInv,                    
            isFromWO:true,
            transactionid:(this.isCustomer)?4:5,        
            width:950,
            readOnly:this.readOnly,                   
            height:400,
            resizable : false,
            parentGridRecords: parentGridRecords,
            modal : true
        });
        this.batchDetailswin.on("beforeclose",function(){
            this.batchDetails=this.batchDetailswin.getBatchDetails();
            var isfromSubmit=this.batchDetailswin.isfromSubmit;
            if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                obj.set("batchdetails",this.batchDetails);
            }
        },this);
        this.batchDetailswin.show();
    }
});
