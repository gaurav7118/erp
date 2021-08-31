/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

function getProductGrid() {
    return new Wtf.account.MRPProductDetailsGrid();
}

Wtf.account.MRPProductDetailsGrid = function (config){
    this.bodyBorder=config.bodyBorder;
    this.currentQty=config.currentQuantity;
    this.totalQty=config.totalquantity;
    this.parent=config.parent;
    this.isContractDetails=config.isContractDetails?config.isContractDetails:false;
    this.isShipmentContract=config.isShipmentContract?config.isShipmentContract:false;
    this.isPackagingContract=config.isPackagingContract?config.isPackagingContract:false;
    this.affecteduser = "";
    this.forCurrency="";
    this.billDate=new Date();
    this.UomSchemaType=Wtf.account.companyAccountPref.UomSchemaType;
    this.currentAddressDetailrec="";
    this.customerName=config.customerName;
    this.isEdit = config.isEdit ? config.isEdit : false;
    this.moduleId =Wtf.MRP_MASTER_CONTRACT_MODULE_ID;
    
    Wtf.apply(this, config);
    this.productOptimizedFlag = Wtf.account.companyAccountPref.productOptimizedFlag;
    Wtf.account.MRPProductDetailsGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.account.MRPProductDetailsGrid,Wtf.Panel,{
    initComponent:function (){
        this.addEvents({
            'updatedcost':true,
            'updatedbuilds':true,
            'updatedqty':true
        });

        Wtf.account.MRPProductDetailsGrid.superclass.initComponent.call(this);
        this.maxbuilds = 0;
        this.totalcost = 0;
        this.assemblyProductJson = [];
        this.globalProductCount = Wtf.productStore.getCount();
        
        this.createEditors();
        
       this.productRec = Wtf.data.Record.create ([
            {name:'pid'},
            {name:'rowid'},
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'uomid'},
            {name:'uomname'},
            {name:'baseuomname'},
            {name:'baseuomid'},
            {name:'skauomname'},
            {name:'skauomid'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'purchaseaccountid'},
            {name:'salesaccountid'},
            {name:'purchaseretaccountid'},
            {name:'salesretaccountid'},
            {name:'reorderquantity'},
            {name:'quantity'},
            {name:'baseuomquantity'},
            {name:'baseuomrate', defValue: 1.00},
            {name:'rate'},
            {name:'discamount'},
            //Shipment Contract record start
            {name:'deliverymode'},
            {name:'totalnoofunit'},
            {name:'totalquantity'},
            {name:'shippingperiodfrom'},
            {name:'shippingperiodto'},
            {name:'partialshipmentallowed'},
            {name:'shipmentstatus'},
            {name:'shippingagent'},
            {name:'loadingportcountry'},
            {name:'loadingport'},
            {name:'transshipmentallowed'},
            {name:'dischargeportcountry'},
            {name:'dischargeport'},
            {name:'finaldestination'},
            {name:'postalcode'},
            {name:'budgetfreightcost'},
            {name:'shipmentcontratremarks'},
            //Shipping Address
            {name:'shippingaddrscombo'},
            {name:'shippingaliasname'},
            {name:'shippingaddress'},
            {name:'shippingcounty'},
            {name:'shippingcity'},
            {name:'shippingstate'},
            {name:'shippingcountry'},
            {name:'shippingpostalcode'},
            {name:'shippingphone'},
            {name:'shippingmobile'},
            {name:'shippingfax'},
            {name:'shippingemail'},
            {name:'shippingrecipientname'},
            {name:'shippingcontactperson'},
            {name:'shippingcontactpersonnumber'},
            {name:'shippingcontactcersondesignation'},
            {name:'shippingwebsite'},
            {name:'shippingroute'},
            //Shipment Contract record end
            //Packaging Contract record start
            {name:'unitweightvalue'},
            {name:'unitweight'},
            {name:'packagingtype'},
            {name:'certificaterequirement'},
            {name:'certificate'},
            {name:'shippingmarksdetails'},
            {name:'shipmentmode'},
            {name:'percontainerload'},
            {name:'palletmaterial'},
            {name:'packagingprofiletype'},
            {name:'marking'},
            {name:'drumorbagdetails'},
            {name:'drumorbagsize'},
            {name:'numberoflayers'},
            {name:'heatingpad'},
            {name:'palletloadcontainer'},
            //Packaging Contract record end
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'producttype'},
            {name:'type'},
            {name:'purchaseprice'},
            {name: 'isparentproduct'},
            {name:'isAsset',type:'boolean'},
            {name: 'isStopPurchase',type:'boolean'},
            {name: 'isLocationForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name:'saleprice'},
            {name:'isRecyclable'},
            {name:'recycleQuantity'},
            {name: 'leaf'},
            {name: 'level'},
            {name: 'isWastageApplicable'},
            {name:'location'},
            {name:'warehouse'}
        ]);
        
        this.gridStore = new Wtf.data.Store({
            url:"ACCContractMaster/getMasterContractRows.do",
            baseParams:{mode:25},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
        
        if (this.productOptimizedFlag == Wtf.Show_all_Products) {
            this.productStore = new Wtf.data.Store({
                url: "ACCProduct/getProductsForCombo.do",
                baseParams: {mode: 22,excludeParent:true, loadPrice: true,productid:this.productid},
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.productRec)
            });
            this.productStore.load();
        } else {
            this.productStore = Wtf.productStoreOptimized;
        }  
//        if(this.globalProductCount>0){
//            this.cloneProductList();
//        }
        if (Wtf.account.companyAccountPref.productOptimizedFlag == Wtf.Show_all_Products){
            chkproductload(); // to be load on show all product option
        }
        
        this.productId= new Wtf.form.TextField({
            name:'pid'
        });
        
        this.TypeStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id'
                }, {
                    name: 'value'
                }],
            data: [['1', 'Yes'], ['2', 'No']]
        });

        
        this.certificateRequirementEditor = new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            id:'certificaterequirement'+this.id,
            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.partialshipmentallowed"),
            valueField:'id',
            displayField:'value',
            store:this.TypeStore,
            width:240,
            typeAhead: true,
            forceSelection: true,
            name:'certificaterequirement',
            hiddenName:'certificaterequirement'
        });
        
        
//        this.packagingProfileTypeRec = Wtf.data.Record.create([
//            {name: 'id'},
//            {name: 'name'}
//        ]);
//        this.packagingProfileTypeStore = new Wtf.data.Store({
//            url: "ACCMaster/getMasterItems.do",
//            baseParams: {
//                mode: 112,
//                groupid: 48
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            }, this.packagingProfileTypeRec)
//        });
        chkpackagingProfileTypeload();
        
        
        this.packagingProfileTypeEditor = new Wtf.form.FnComboBox({
            name: 'packagingprofiletype',
            store: Wtf.packagingProfileTypeStore,
            typeAhead: true,
            selectOnFocus: true,
            maxHeight:250,
            valueField: 'id',
            displayField: 'name',
            scope: this,
            forceSelection: true,
            triggerAction:'all',
            mode: 'local'
        });
        this.packagingProfileTypeEditor.addNewFn=this.addPackagingProfileType.createDelegate(this);
        
        chkUomload();
        this.uomEditor=new Wtf.form.FnComboBox({
            hiddenName:'uomname',
            triggerAction:'all',
            mode: 'local',
            lastQuery:'',
            name:'uomname',
            store:Wtf.uomStore,//Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
            typeAhead: true,
            selectOnFocus:true,
            valueField:'uomid',
            displayField:'uomname',
            scope:this,
            forceSelection:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.edit))
            this.uomEditor.addNewFn=this.showUom.createDelegate(this);
        
        this.editprice=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        });
        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision: Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT
        });
        
//        Wtf.productStore.on("load", this.cloneProductList, this);
        if (this.productOptimizedFlag == Wtf.Show_all_Products) {
            this.productEditor = new Wtf.form.ExtFnComboBox({
                name: 'productname',
                store: this.productStore,
                typeAhead: true,
                selectOnFocus: true,
                maxHeight:250,
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField: 'productid',
                isProductCombo: true,
                displayField: 'productname',
                extraFields: ['pid','type'],
                extraComparisionField: 'pid', // type ahead search on acccode as well.
                listWidth: 400,
                scope: this,
//                disabled:this.isShipmentContract || this.isPackagingContract,
                forceSelection: true
            });
        } else {
            this.productEditor = new Wtf.form.ExtFnComboBox({
                name: 'productname',
                store: this.productStore, //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                selectOnFocus: true,
                isProductCombo: true,
                maxHeight:250,
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField: 'productid', //productid
                displayField: 'productname',
                extraFields: ['pid','type'],
                listWidth: 450,
                extraComparisionField: 'pid', // type ahead search on acccode as well.
                mode: 'remote',
                //editable:false,
                hideTrigger: true,
                scope: this,
                emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
                triggerAction: 'all',
                editable: true,
                minChars: 2,
                hirarchical: true,
                hideAddButton: true, //Added this Flag to hide AddNew  Button  
                // addNewFn:this.openProductWindow.createDelegate(this),
                forceSelection: true
            });
        } 
       this.productEditor.addNewFn=this.openProductWindow.createDelegate(this);
       
       this.transDiscount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });

        var columnArr = new Array();
        
        columnArr.push(new Wtf.grid.RowNumberer());
            
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product",//
            dataIndex:'productid',
            width:250,
            renderer:this.getComboNameRenderer(this.productEditor),//method used because it sets the productname in shipment and packaging tabs
            editor:(this.isShipmentContract || this.isPackagingContract)?"":this.productEditor
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),//"Description",//
            dataIndex:'desc',
            width:250,
            renderer : function(val) {
                return "<div wtf:qtip=\""+val+"\" wtf:qtitle="+WtfGlobal.getLocaleText("acc.product.gridDesc")+">"+val+"</div>";
            },
            editor:this.readOnly?"":this.remark
        });

        if(this.isContractDetails){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.product.gridQty"),
                align:'right',
                width:100,
                dataIndex:'quantity',
                editor:this.readOnly?"":new Wtf.form.NumberField({allowNegative:false, allowDecimals:true,decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL}),
                renderer:function(val){
                    return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?" ":parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                }
            });
            columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModel[Wtf.MRP_MASTER_CONTRACT_MODULE_ID]);
        }

        if(this.isShipmentContract){
            columnArr.push({
                header: '',
                align:'center',
                renderer: this.ShippingAddressRenderer.createDelegate(this),
                dataIndex:'shippingaddresswindow',
                id:this.id+'shippingaddresswindow',
                width:40
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
                width:150,
                dataIndex:'uomid',
                renderer:this.readOnly?"":Wtf.comboBoxRendererwithClearFilter(this.uomEditor),
                editor:(this.readOnly||this.isViewTemplate)?"":this.uomEditor
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),//Base UOM Rate
                dataIndex:"baseuomrate",
                align:'left',
                width:150,
                renderer:this.conversionFactorRenderer(this.productStore,"productid","uomname",this.gridStore),
                editor:(this.isViewTemplate||this.readOnly||this.UomSchemaType==Wtf.PackegingSchema)?"":(Wtf.account.companyAccountPref.UomSchemaType===0  && Wtf.account.companyAccountPref.isBaseUOMRateEdit) ?this.transBaseuomrate : ""     //Does allow to user to change conversion factor
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridQtyInBase"),//Base UOM Quantity
                dataIndex:"baseuomquantity",
                align:'right',
                width:150,
                renderer:this.storeRenderer(this.productStore,"productid","uomname",this.gridStore)
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),// "Unit Price",
                dataIndex: "rate",
                align:'right',
                width:150,
                renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
                editor:(this.readOnly||this.isViewTemplate)?"":this.editprice,
                editable:true
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridAmount"),
                dataIndex:'discamount',
                align:'right',
                width:200,
                renderer:WtfGlobal.withoutRateCurrencySymbol,
                editor:""
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.deliverymode"),
                dataIndex:"deliverymode",
                width:150,
                editor:this.deliveryModeEditor,
                renderer:Wtf.comboBoxRendererwithClearFilter(this.deliveryModeEditor)
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.totalnoofunit"),
                dataIndex:"totalnoofunit",
                width:150,
                align:'center',
                editor:this.totalNoOfUnitEditor
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.totalquantity"),
                dataIndex:"totalquantity",
                width:150,
                align:'center',
                editor:this.totalQuantityEditor
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.shippingperiodfrom"),
                dataIndex:"shippingperiodfrom",
                width:150,
                editor:this.shippingPeriodFromEditor,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.shippingperiodto"),
                dataIndex:"shippingperiodto",
                width:150,
                editor:this.shippingPeriodToEditor,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.partialshipmentallowed"),
                dataIndex:"partialshipmentallowed",
                width:150,
                editor:this.partialShipmentAllowedEditor,
                renderer:Wtf.comboBoxRenderer(this.partialShipmentAllowedEditor)
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.shipmentstatus"),
                dataIndex:"shipmentstatus",
                width:150,
                editor:this.shipmentStatusEditor,
                renderer:Wtf.comboBoxRendererwithClearFilter(this.shipmentStatusEditor)
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.shippingagent"),
                dataIndex:"shippingagent",
                width:150,
                editor:new Wtf.form.TextField({
                    name:'shippingagent'
                })
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.loadingportcountry"),
                dataIndex:"loadingportcountry",
                width:150,
                editor:new Wtf.form.TextField({
                    name:'loadingportcountry'
                })
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.loadingport"),
                dataIndex:"loadingport",
                width:150,
                editor:new Wtf.form.TextField({
                    name:'loadingport'
                })
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.transshipmentallowed"),
                dataIndex:"transshipmentallowed",
                width:150,
                editor:this.transShipmentAllowedEditor,
                renderer:Wtf.comboBoxRenderer(this.transShipmentAllowedEditor)
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.dischargeportcountry"),
                dataIndex:"dischargeportcountry",
                width:150,
                editor:this.dischargePortCountryEditor
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.dischargeport"),
                dataIndex:"dischargeport",
                width:150,
                editor:this.dischargePortEditor
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.finaldestination"),
                dataIndex:"finaldestination",
                width:150,
                editor:this.finalDestinationEditor
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.postalcode"),
                dataIndex:"postalcode",
                width:150,
                editor:this.postalCodeEditor
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.budgetfreightcost"),
                dataIndex:"budgetfreightcost",
                width:150,
                editor:this.budgetFreightCostEditor
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.remarks"),
                dataIndex:"shipmentcontratremarks",
                width:150,
                editor:this.shipmentcontratremarksEditor
            });
        }
        
        this.unitweightvalueEditor = new Wtf.form.TextField({
            name:'unitweightvalue'
        });
        
        if(this.isPackagingContract){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.mastercontract.unitweightvalue"),
                dataIndex:"unitweightvalue",
                width:150,
                editor:this.unitweightvalueEditor
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.unitweight"),
                dataIndex:"unitweight",
                width:150,
                editor:new Wtf.form.TextField({
                    name:'unitweight'
                })
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.packagingtype"),
                dataIndex:"packagingtype",
                width:150,
                editor:new Wtf.form.TextField({
                    name:'packagingtype'
                })
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.certificaterequirement"),
                dataIndex:"certificaterequirement",
                width:150,
                renderer:Wtf.comboBoxRenderer(this.certificateRequirementEditor),
                editor:this.certificateRequirementEditor
            },/*{
                header:WtfGlobal.getLocaleText("acc.mastercontract.certificate"),
                dataIndex:"certificate",
                width:150
            },*/{
                header:WtfGlobal.getLocaleText("acc.mastercontract.shippingmarksdetails"),
                dataIndex:"shippingmarksdetails",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.shipmentmode"),
                dataIndex:"shipmentmode",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.percontainerload"),
                dataIndex:"percontainerload",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.palletmaterial"),
                dataIndex:"palletmaterial",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.packagingprofileyype"),
                dataIndex:"packagingprofiletype",
                width:150,
                editor:this.packagingProfileTypeEditor,
                renderer:Wtf.comboBoxRendererwithClearFilter(this.packagingProfileTypeEditor)
                
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.marking"),
                dataIndex:"marking",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.drumorbagdetails"),
                dataIndex:"drumorbagdetails",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.drumorbagsize"),
                dataIndex:"drumorbagsize",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.numberoflayers"),
                dataIndex:"numberoflayers",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.heatingpad"),
                dataIndex:"heatingpad",
                width:150,
                editor:new Wtf.form.TextField()
            },{
                header:WtfGlobal.getLocaleText("acc.mastercontract.palletloadcontainer"),
                dataIndex:"palletloadcontainer",
                width:150,
                editor:new Wtf.form.TextField()
            });
        }
        if(!(this.isShipmentContract || this.isPackagingContract)){
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.product.gridAction"),//"Action",//
                align:'center',
                hidden:this.isBuildAssemblyFlag,
                renderer: this.deleteRenderer.createDelegate(this)    
            });
        }
        
        this.gridcm = new Wtf.grid.ColumnModel(columnArr);

    
        var colModelArray;
        colModelArray = GlobalColumnModel[this.moduleId];
        WtfGlobal.updateStoreConfig(colModelArray, this.gridStore);

        this.itemsgrid = new Wtf.grid.EditorGridPanel({
            layout:'fit',
            region:"center",
            clicksToEdit:1,
            autoScroll:true,
            store: this.gridStore,
            cm:this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
//                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });

        this.wraperPanel = new Wtf.Panel({
            layout:"border",
            border : this.bodyBorder==true?true:false,
            items:[
                this.itemsgrid
            ]
        });

        if(this.isEdit && this.isContractDetails){
            this.gridStore.load({
                params:{
                    mrpcontractid: this.record.data.id
                }
            });
            this.gridStore.on('load', this.gridStoreOnLoad, this);
        }
        
        this.itemsgrid.on('afteredit',this.updateRecord,this);
        this.itemsgrid.on('validateedit',this.validateRecord,this);
        this.itemsgrid.on('beforeedit',this.beforeEditRecord,this);
        if (!this.readOnly) {
            this.itemsgrid.on('rowclick',this.handleRowClick,this);
        }
        this.add(this.wraperPanel);
        if(this.rendermode=="productform"){
             this.addBlankRecord();
        }
    },
    openProductWindow:function(){
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.product, Wtf.Perm.product.create)){
            callProductWindow(false, null, "productWin");
            Wtf.getCmp("productWin").on("update",function(obj,productid){this.productID=productid;},this);
        }
        else{
              WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.creating")+" "+WtfGlobal.getLocaleText("acc.create.products")); 
        }
    },
    gridStoreOnLoad: function(){
        this.addBlankRecord();
        
        this.activateMasterContractTabs();
    },
    activateMasterContractTabs: function(){
        var MasterContractTab = Wtf.getCmp('MasterContractTab');
        if(MasterContractTab && MasterContractTab.tabPanel){
            //To fire "activate" event for all tabs
            MasterContractTab.tabPanel.setActiveTab(0);
            MasterContractTab.tabPanel.setActiveTab(1);
            MasterContractTab.tabPanel.setActiveTab(2);
            MasterContractTab.tabPanel.setActiveTab(3);
            MasterContractTab.tabPanel.setActiveTab(4);
            MasterContractTab.tabPanel.setActiveTab(5);
            MasterContractTab.tabPanel.setActiveTab(0);
        }
    },
    addBlankRecord:function(){

        var Record = this.gridStore.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {}, blankObj = {};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if (f.name != 'rowid') {
                blankObj[f.name] = '';
                if (!Wtf.isEmpty(f.defValue))
                    blankObj[f.name] = f.convert((typeof f.defValue == "function" ? f.defValue.call() : f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        this.gridStore.add(newrec);
    },

    updateRecord: function(e) {
        if (e.field == "productid") {
            if (e.row == this.gridStore.getCount() - 1) {
                this.addBlankRecord();
            }

            var productrec = this.productStore.getAt(this.productStore.find('productid', e.value));
            Wtf.Ajax.requestEx({
                url:"ACCProduct/getIndividualProductPrice.do",
                params: {
                    productid: e.value,
                    affecteduser: this.affecteduser,
                    forCurrency: Wtf.account.companyAccountPref.productPriceinMultipleCurrency ? this.forCurrency : "",
                    quantity: 0,
                    transactiondate : WtfGlobal.convertToGenericDate(this.billDate),
                    carryin : false
                }
            }, this, function(response) {
                var datewiseprice = response.data[0]
                e.record.set("pid", productrec.data["pid"]);
                e.record.set("productname", productrec.data["productname"]);
                e.record.set("desc", productrec.data["desc"]);
                e.record.set("purchaseprice", datewiseprice.purchaseprice);
                e.record.set("saleprice", datewiseprice.saleprice);
                e.record.set("type", productrec.data["type"]);
                e.record.set("producttype", productrec.data["producttype"]);
                e.record.set("isSerialForProduct", productrec.data["isSerialForProduct"]);
                e.record.set("quantity", e.record.data.quantity);
                e.record.set("uomid", productrec.data["uomid"]);
                e.record.set("rate", datewiseprice.price);
                if (productrec.data["baseuomrate"] != "") {
                    e.record.set("baseuomrate", productrec.data["baseuomrate"]);
                } else {
                    e.record.set("baseuomrate", 1);
                }
                if (productrec.data["baseuomquantity"] != "") {
                    e.record.set("baseuomquantity", productrec.data["baseuomquantity"]);
                } else {
                    e.record.set("baseuomquantity", 1);
                }
                e.record.set("discamount", datewiseprice.price * productrec.data["quantity"]);

//                this.updateSubtotal();
            }, function() {

            });
        }
        if(e.field == "quantity" || e.field == "rate"){
            this.updateSubtotal();
        }
        if(e.field == "baseuomrate" ){
            var baseuomrate = e.value;
            e.record.set("baseuomquantity",baseuomrate * e.record.data.quantity );
        }
    },
    
    beforeEditRecord:function(e){
        if(this.disableEdit) {
            return false;
        }
    },

    validateRecord:function(e){
        if(e.field=="productid"){
            var productRecIndex = WtfGlobal.searchRecordIndex(this.gridStore, e.value, 'productid');
            //if(this.gridStore.find("productid",e.value)>=0){
            if(productRecIndex>=0){
                var productrec=this.productStore.getAt(this.productStore.find('productid',e.value));
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+productrec.data['productname']], 2);
                e.cancel=true;
            }        
        }
    },
    updateSubtotal:function(){
        var price = 0;
        var storeCount=this.rendermode=="productform"?this.gridStore.getCount()-1:this.gridStore.getCount();
        for(var i=0;i<storeCount;i++){
            var rec=this.gridStore.getAt(i);
            var discamount=0
            price = (rec.data['rate']=== undefined || rec.data['rate']=="")?0:rec.data['rate'];
            var Qty=(rec.data['quantity']=== undefined || rec.data['quantity']=="")?0:rec.data['quantity'];
              discamount = price * Qty;
              rec.set("discamount",discamount);
        }
    },   
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    deleteRenderer:function(v,m,rec){
        
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
        
    },
    handleRowClick:function(grid,rowindex,e){
        if(this.disableEdit) {
            return false;
        }
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                store.remove(store.getAt(rowindex));//remove record from Contract Details product store
                
                var MasterContractTab = Wtf.getCmp('MasterContractTab');
                if(MasterContractTab.ShipmentContractForm.ProductGrid){
                    var shipmentContractProductStore = MasterContractTab.ShipmentContractForm.ProductGrid.gridStore;
                    shipmentContractProductStore.remove(shipmentContractProductStore.getAt(rowindex));//remove record from Shipment Contract product store
                }
                if(MasterContractTab.PackagingContractForm.ProductGrid){
                    var packagingContractProductStore = MasterContractTab.PackagingContractForm.ProductGrid.gridStore;
                    packagingContractProductStore.remove(packagingContractProductStore.getAt(rowindex));//remove record from Packaging Contract product store
                }
        
                grid.getView().refresh();
                if(rowindex==total-1){
                    this.addBlankRecord();
                }
            }, this);
        }else if(e.getTarget(".serialNo-gridrow")){
            callAddressWindow(this.customerName, this.gridStore.data, this.isEdit);
            Wtf.getCmp('shippingAddressWindow').on('update',function(config){
                this.currentAddressDetailrec=config.currentaddress;
            },this);
        }
    },

    getComboNameRenderer : function(combo){
        return function(value,metadata,record,row,col,store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store,value,combo.valueField);
            var fieldIndex = "productname";
            if(idx == -1) {
                if(record.data["productname"] && record.data[fieldIndex].length>0) {
                    return record.data[fieldIndex];
                }
                else
                    return "";
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get(combo.displayField);
            record.set("productid", value);
            record.set("productname", displayField);
            return displayField;
        }
    },

    createEditors: function(){
        this.TypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'value'
            }],
            data:[['1','Yes'],['2','No']]
        });
        
        
        this.Rec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);        
//        this.deliveryModeStore = new Wtf.data.Store({
//            url: "ACCMaster/getMasterItems.do",
//            baseParams: {
//                mode: 112,
//                groupid: 44
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            }, this.Rec)
//        });
//        this.deliveryModeStore.load();
        
        chkdeliveryModeStoreload();
        this.deliveryModeEditor = new Wtf.form.FnComboBox({            
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            store:Wtf.deliveryModeStore,
            width:240,
            typeAhead: true,
            forceSelection: true,
            name:'deliverymode',
            hiddenName:'deliverymode'
        });
        this.deliveryModeEditor.addNewFn=this.addDeliveryMode.createDelegate(this);
        
        this.totalNoOfUnitEditor = new Wtf.form.NumberField({
            name: 'totalnoofunit',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this,
            allowBlank:false
        });
        
        this.totalQuantityEditor = new Wtf.form.NumberField({
            name: 'totalquantity',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this
        });
        
        this.shippingPeriodFromEditor = new Wtf.form.DateField({
            name: 'shippingperiodfrom',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this,
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:false
        });

        this.shippingPeriodToEditor = new Wtf.form.DateField({
            name: 'shippingperiodto',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this,
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:false
        });
        
        
        this.partialShipmentAllowedEditor = new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'value',
            store:this.TypeStore,
            width:240,
            typeAhead: true,
            forceSelection: true,
            name:'partialshipmentallowed',
            hiddenName:'partialshipmentallowed'
        });
        
        
//        this.shipmentStatusStore = new Wtf.data.Store({
//            url: "ACCMaster/getMasterItems.do",
//            baseParams: {
//                mode: 112,
//                groupid: 43
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            }, this.Rec)
//        });
//        this.shipmentStatusStore.load();
        chkshipmentStatusStoreload();
        this.shipmentStatusEditor = new Wtf.form.FnComboBox({            
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            store:Wtf.shipmentStatusStore,
            width:240,
            typeAhead: true,
            forceSelection: true,
            name:'shipmentstatus',
            hiddenName:'shipmentstatus'
        });
        this.shipmentStatusEditor.addNewFn=this.addShipmentStatus.createDelegate(this);
        
        this.transShipmentAllowedEditor = new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'value',
            store:this.TypeStore,
            width:240,
            typeAhead: true,
            forceSelection: true,
            name:'transshipmentallowed',
            hiddenName:'transshipmentallowed'
        });
        
        this.dischargePortCountryEditor = new Wtf.form.TextField({
            name: 'dischargeportcountry',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this
        });
        
        this.dischargePortEditor = new Wtf.form.TextField({
            name: 'dischargeport',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this
        });
        
        this.finalDestinationEditor = new Wtf.form.TextField({
            name: 'finaldestination',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this
        });
        
        this.postalCodeEditor = new Wtf.form.TextField({
            name: 'postalcode',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this
        });
        
        this.budgetFreightCostEditor = new Wtf.form.NumberField({
            name: 'budgetfreightcost',
            hidden:false,
            width : 240,
            maxLength:50,
            scope:this
        });
        
        this.shipmentcontratremarksEditor = new Wtf.form.TextArea({
            name: 'shipmentcontratremarks',
            height:50,
            width : 240,
            maxLength:2048,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
        });
        this.remark= new Wtf.form.TextArea({
            name:'remark'
        });
    },
    
    addDeliveryMode: function(){
        addMasterItemWindow('44');
    },
    
    addShipmentStatus: function(){
        addMasterItemWindow('43');
    },
    
    ShippingAddressRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.mastercontract.shippingaddress")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.mastercontract.shippingaddress")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    
    addPackagingProfileType: function(){
        addMasterItemWindow('48');
    },
    
    conversionFactorRenderer:function(store, valueField, displayField,gridStore) {
        return function(value, meta, record) {
            
            if(value != "") {
                value = (parseFloat(getRoundofValue(value)).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT)=="NaN")?parseFloat(0).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT):parseFloat(getRoundofValueWithValues(value,Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT)).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT);
            }
            var idx = Wtf.uomStore.find("uomid", record.data["uomid"]);            
            if(idx == -1)
                return value;
            var uomname = Wtf.uomStore.getAt(idx).data["uomname"];
            if (uomname == "N/A") {
                return value;
            }
           
            var rec="";
            idx = store.find(valueField, record.data[valueField]);
            if(idx == -1){
                idx = gridStore.find(valueField, record.data[valueField]);
                if(idx == -1)
                    return value;
                rec = gridStore.getAt(idx);
                return "1 "+ uomname +" = "+ +value+" "+rec.data["baseuomname"];
            }else{
                 rec = store.getAt(idx);
                 return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
            }  
            
        }
    },
    
storeRenderer:function(store, valueField, displayField,gridStore) {
        return function(value, meta, record) {
            value=(parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            var rec="";
            var idx = store.find(valueField, record.data[valueField]);
            if(idx == -1){
                idx = gridStore.find(valueField, record.data[valueField]);
                if(idx == -1)
                    return value;
                rec = gridStore.getAt(idx);
                return value+" "+rec.data["baseuomname"];
            }else{
                 rec = store.getAt(idx);
                 return value+" "+rec.data[displayField];
            }                          
            
        }
    },
    
    showUom:function(){
       callUOM('uomReportWin');
       Wtf.getCmp('uomReportWin').on('update', function(){
           Wtf.uomStore.reload();
       }, this);
    }
});