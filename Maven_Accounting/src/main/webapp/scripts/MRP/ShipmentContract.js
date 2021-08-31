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

Wtf.account.ShipmentContract=function(config){
    
    this.id=config.id;
    this.modeName=config.modeName;
    this.isShipmentContract=config.isShipmentContract?config.isShipmentContract:false;
    this.customerName=config.customerName;
    this.isEdit = config.isEdit!=undefined ? config.isEdit : false;
    this.record = config.record;
    
    Wtf.apply(this, config);

    Wtf.account.ShipmentContract.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.account.ShipmentContract,Wtf.Panel,{
    autoScroll: true,
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:false,
    closable : false,
    autoHeight:true,
    initComponent:function(config){
        Wtf.account.ShipmentContract.superclass.initComponent.call(this,config);
        
//        //Create stores required for fields
//        this.createStores();
        
        //Load stores required for fields
        this.loadStores();
        
//        //Create fields required in panels
//        this.createFields();
        
        
        
//        //Create panels
//        this.createPanels();
        
        //Create Product grid
        this.createProductGrid();
        
    },
    
    onRender:function(config){
        
        this.centerPanel=new Wtf.Panel({
            region:'center',
            border:false,
            scope:this,
//            width:"100%",
            autoHeight:true,
            id:'centerPanel'+this.id,
            items:[
//                this.shipmentContractPanel,
                this.ProductGrid
            ]
        });
//        this.shipmentContractPanel.doLayout();
//        this.GridPanel.doLayout();
        this.centerPanel.doLayout();
        
        this.add(this.centerPanel);
        
        Wtf.account.ShipmentContract.superclass.onRender.call(this, config);
    },
    
//    createStores:function(){
//        this.Rec = Wtf.data.Record.create([
//            {name: 'id'},
//            {name: 'name'}
//        ]);
//        
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
//        
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
//        
//        this.TypeStore = new Wtf.data.SimpleStore({
//            fields:[{
//                name:'id'
//            },{
//                name:'value'
//            }],
//            data:[['1','Yes'],['2','No']]
//        });
//        
//    },
    
    loadStores: function(){
        
    },
    
//    createFields:function(){
//        //Create left panel fields
//        this.createLeftContractDetailsFields();
//        
//        //Create right panel fields
//        this.createRightContractDetailsFields();  
//    },
//    
//    createLeftContractDetailsFields: function(){
//        this.deliveryMode = new Wtf.form.ExtFnComboBox({            
//            triggerAction:'all',
//            mode: 'local',
//            id:'deliverymode'+this.id,
//            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.deliverymode")+"*",
//            valueField:'id',
//            displayField:'name',
//            store:this.deliveryModeStore,
//            width:240,
//            typeAhead: true,
//            forceSelection: true,
//            name:'deliverymode',
//            hiddenName:'deliverymode',
//            addNoneRecord: true,
//            extraFields:[],
//            allowBlank:false
//        });
//        this.deliveryMode.addNewFn=this.addDeliveryMode.createDelegate(this);
//        
//        this.totalNoOfUnit = new Wtf.form.NumberField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.totalnoofunit")+"*",
//            name: 'totalnoofunit',
//            id:"totalnoofunit"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this,
//            allowBlank:false
//        });
//        
//        this.totalQuantity = new Wtf.form.NumberField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.totalquantity"),
//            name: 'totalquantity',
//            id:"totalquantity"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.shippingPeriodFrom = new Wtf.form.DateField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.shippingperiodfrom")+"*",
//            name: 'shippingperiodfrom',
//            id:"shippingperiodfrom"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this,
//            format:WtfGlobal.getOnlyDateFormat(),
//            allowBlank:false
//        });
//
//        this.shippingPeriodTo = new Wtf.form.DateField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.shippingperiodto")+"*",
//            name: 'shippingperiodto',
//            id:"shippingperiodto"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this,
//            format:WtfGlobal.getOnlyDateFormat(),
//            allowBlank:false
//        });
//        
//        this.partialShipmentAllowed = new Wtf.form.ComboBox({            
//            triggerAction:'all',
//            mode: 'local',
//            id:'partialshipmentallowed'+this.id,
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.partialshipmentallowed"),
//            valueField:'id',
//            displayField:'value',
//            store:this.TypeStore,
//            width:240,
//            typeAhead: true,
//            forceSelection: true,
//            name:'partialshipmentallowed',
//            hiddenName:'partialshipmentallowed'
////            listeners:{
////                'select':{
////                    scope:this
////                }
////            }
//        });
//        
//        this.shipmentStatus = new Wtf.form.ExtFnComboBox({            
//            triggerAction:'all',
//            mode: 'local',
//            id:'shipmentstatus'+this.id,
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.shipmentstatus"),
//            valueField:'id',
//            displayField:'name',
//            store:this.shipmentStatusStore,
//            width:240,
//            typeAhead: true,
//            forceSelection: true,
//            name:'shipmentstatus',
//            hiddenName:'shipmentstatus',
//            addNoneRecord: true,
//            extraFields:[]
//        });
//        this.shipmentStatus.addNewFn=this.addShipmentStatus.createDelegate(this);
//        
//        this.shippingAgent = new Wtf.form.TextField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.shippingagent"),
//            name: 'shippingagent',
//            id:"shippingagent"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.loadingPortCountry = new Wtf.form.TextField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.loadingportcountry"),
//            name: 'loadingportcountry',
//            id:"loadingportcountry"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.loadingPort = new Wtf.form.TextField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.loadingport"),
//            name: 'loadingport',
//            id:"loadingport"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.transShipmentAllowed = new Wtf.form.ComboBox({            
//            triggerAction:'all',
//            mode: 'local',
//            id:'transshipmentallowed'+this.id,
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.transshipmentallowed"),
//            valueField:'id',
//            displayField:'value',
//            store:this.TypeStore,
//            width:240,
//            typeAhead: true,
//            forceSelection: true,
//            name:'transshipmentallowed',
//            hiddenName:'transshipmentallowed',
//            listeners:{
//                'select':{
//                    scope:this
//                }
//            }
//        });
//        
//    },
//    
//    createRightContractDetailsFields: function(){
//        this.dischargePortCountry = new Wtf.form.TextField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.dischargeportcountry"),
//            name: 'dischargeportcountry',
//            id:"dischargeportcountry"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.dischargePort = new Wtf.form.TextField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.dischargeport"),
//            name: 'dischargeport',
//            id:"dischargeport"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.finalDestination = new Wtf.form.TextField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.finaldestination"),
//            name: 'finaldestination',
//            id:"finaldestination"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.postalCode = new Wtf.form.TextField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.postalcode"),
//            name: 'postalcode',
//            id:"postalcode"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.budgetFreightCost = new Wtf.form.NumberField({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.budgetfreightcost"),
//            name: 'budgetfreightcost',
//            id:"budgetfreightcost"+this.id,
//            hidden:false,
//            width : 240,
//            maxLength:50,
//            scope:this
//        });
//        
//        this.shipmentcontratremarks = new Wtf.form.TextArea({
//            fieldLabel:WtfGlobal.getLocaleText("acc.mastercontract.remarks"),
//            name: 'shipmentcontratremarks',
//            id:"shipmentcontratremarks"+this.id,
//            height:50,
//            width : 240,
//            maxLength:2048,
//            listeners: {
//                render: function(c){
//                    Wtf.QuickTips.register({
//                        target: c.getEl(),
//                        text: c.qtip
//                    });
//                }
//            }
//        });
//        
//    },
//    
//    createPanels: function(){
//        this.leftShipmentContractPanel = new Wtf.Panel({
//            columnWidth:0.45,
//            id:"leftShipmentContractPanel"+this.id,
//            layout:"form",
//            scope:this,
//            labelWidth:150,
//            bodyStyle: {
//                margin: "20px"
//            },
//            border:false,
//            items:[
//                this.deliveryMode, this.totalNoOfUnit, this.totalQuantity, this.shippingPeriodFrom, this.shippingPeriodTo, this.partialShipmentAllowed, 
//                this.shipmentStatus, this.shippingAgent, this.loadingPortCountry, this.loadingPort, this.transShipmentAllowed
//            ]
//        });
//        
//        this.rightShipmentContractPanel = new Wtf.Panel({
//            columnWidth:0.45,
//            id:"rightShipmentContractPanel"+this.id,
//            layout:"form",
//            labelWidth:150,
//            scope:this,
//            border:false,
//            bodyStyle: {
//                margin: "20px"
//            },
//            items:[
//                this.dischargePortCountry, this.dischargePort, this.finalDestination, this.postalCode, this.budgetFreightCost, this.shipmentcontratremarks
//            ]
//        });
//        
//        this.shipmentContractPanel = new Wtf.form.FormPanel({
//            layout:"column",
//            id:"shipmentContractPanel"+this.id,
//            border:false,
//            scope:this,
//            bodyStyle: {
//                background:"#f1f1f1 none repeat scroll 0 0",
//                margin: "10px",
//                borderColor:"#99bbe8",
//                border:"1px solid #99bbe8"
//            },
//            items: [
//                this.leftShipmentContractPanel,
//                this.rightShipmentContractPanel
//            ]
//        });
//        
//        
//    },
    
    createProductGrid: function(){
        this.ProductGrid=new Wtf.account.MRPProductDetailsGrid({
            layout:"fit",
            id:this.id+"editproductdetailsgrid",
            bodyBorder:true,
            border:false,
            bodyStyle:'padding:10px',
            height:300,
            isInitialQuatiy:false, 
            excluseDateFilters:true,
            productid:(this.record!=null?this.record.data['productid']:null),
            rendermode:"productform",
            isShipmentContract:this.isShipmentContract,
            customerName:this.customerName,
            isEdit: this.isEdit,
            record: this.record
        });

    },
    
    addDeliveryMode: function(){
        addMasterItemWindow('44');
    },
    
    addShipmentStatus: function(){
        addMasterItemWindow('43');
    }
});