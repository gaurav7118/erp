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

Wtf.account.MasterContract = function(config) {
    this.id = config.id;
    this.modeName = config.modeName;
    this.buttonArray = new Array();
    this.isEdit = config.isEdit!=undefined ? config.isEdit : false;
    this.record = config.record;
    this.contractid = '';
     
    //Create Buttons required for bbar
    this.createBBarButtons();

    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar: this.buttonArray
    });

    Wtf.account.MasterContract.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.account.MasterContract, Wtf.account.ClosablePanel, {
    autoScroll: true,
    bodyStyle: {background: "#DFE8F6 none repeat scroll 0 0"},
    border: 'false',
    closable: true,
    initComponent: function(config) {
        Wtf.account.MasterContract.superclass.initComponent.call(this, config);

        this.ContractDetailsForm = new Wtf.account.ContractDetails({
            title: "Contract Details",
            id: "mrpcontractdetailsform" + this.id,
            border: false,
            disabledClass: "newtripcmbss",
            closable: false,
            isContractDetails: true,
            isEdit: this.isEdit,
            record: this.record
        });
        this.ContractDetailsForm.on('render', this.ContractDetailsFormOnRender, this);
        this.ContractDetailsForm.on("activate", this.doLayoutOfAllTabs, this);

        this.ShipmentContractForm = new Wtf.account.ShipmentContract({
            title: "Shipment Contract",
            id: "mrpshipmentcontract" + this.id,//Do not channge as this is used somewhere
            border: false,
            closable: false,
            isShipmentContract: true,
            customerName:this.customerName,
            isEdit: this.isEdit
        });
        this.ShipmentContractForm.on("activate", this.doLayoutTabs, this);

        this.BillingContractForm = new Wtf.account.BillingContract({
            title: "Billing Contract",
            id: "mrpbillingcontract" + this.id,//Do not channge as this is used somewhere
            border: false,
            closable: false,
            customerName: this.customerName,
            isEdit: this.isEdit,
            record: this.record
        });
        this.BillingContractForm.on('render', this.BillingContractFormOnRender, this);
        this.BillingContractForm.on("activate", this.doLayoutTabs, this);

        this.PaymentTermsForm = new Wtf.account.PaymentTerms({
            title: "Payment Terms",
            id: "mrppaymentterms" + this.id,
            border: false,
            closable: false,
            isEdit: this.isEdit,
            record: this.record
        });
        this.PaymentTermsForm.on('render', this.PaymentTermsFormOnRender, this);
        this.PaymentTermsForm.on("activate", this.doLayoutOfAllTabs, this);

        this.PackagingContractForm = new Wtf.account.PackagingContract({
            title: "Packaging Contract",
            id: "mrppackagingcontract" + this.id,//Do not channge as this is used somewhere
            border: false,
            closable: false,
            isPackagingContract: true
        });
        this.PackagingContractForm.on("activate", this.doLayoutTabs, this);

        this.DocumentRequiredForm = new Wtf.account.DocumentRequired({
            title: "Document Required",
            id: "mrpDocumentRequired" + this.id,
            border: false,
            closable: false,
            isEdit: this.isEdit,
            record: this.record
        });
        this.DocumentRequiredForm.on('render', this.DocumentRequiredFormOnRender, this);
        this.DocumentRequiredForm.on("activate", this.doLayoutOfAllTabs, this);
        
        this.tabPanel = new Wtf.TabPanel({
            id: "mrpmastercontractpanel" + this.id,
            activeTab: 0,
            border: false,
            items: [this.ContractDetailsForm, this.ShipmentContractForm, this.BillingContractForm, this.PaymentTermsForm, this.PackagingContractForm, this.DocumentRequiredForm]
        });
        this.tabPanel.doLayout();
        this.tabPanel.on('beforeclose', this.askToClose,this);
        this.tabPanel.on('render', this.tabPanelOnRender, this);
    },
    onRender: function(config) {
        this.isClosable=false;
        this.add(this.tabPanel);
        
        Wtf.account.MasterContract.superclass.onRender.call(this, config);
    },
    tabPanelOnRender: function(){
        if(this.ContractDetailsForm.ProductGrid){
            //Set 1st tab panel i.e. Contract Details Tab active. (ERP-23573) - Form validation creating problem
            this.ContractDetailsForm.ProductGrid.activateMasterContractTabs();
        }
    },
    ContractDetailsFormOnRender: function(){
        this.ContractDetailsForm.loadData();
    },
    PaymentTermsFormOnRender: function(){
        this.PaymentTermsForm.loadData();
    },
    BillingContractFormOnRender: function(){
        this.BillingContractForm.loadData();
    },
    DocumentRequiredFormOnRender: function(){
        this.DocumentRequiredForm.loadData();
    },
    doLayoutOfAllTabs: function(panel) {
        this.tabPanel.doLayout();
        this.ContractDetailsForm.doLayout();
        if(this.ContractDetailsForm.ProductGrid){
            this.ContractDetailsForm.ProductGrid.doLayout();
        }
        this.ShipmentContractForm.doLayout();
        if(this.ShipmentContractForm.ProductGrid){
            this.ShipmentContractForm.ProductGrid.doLayout();
        }
        this.BillingContractForm.doLayout();
        this.PaymentTermsForm.doLayout();
        this.PackagingContractForm.doLayout();
        if(this.PackagingContractForm.ProductGrid){
            this.PackagingContractForm.ProductGrid.doLayout();
        }
        this.DocumentRequiredForm.doLayout();

        this.customerName = this.ContractDetailsForm.Customer.getValue();
        if (this.customerName != undefined && this.customerName !=''){
            this.DocumentRequiredForm.customerName.setValue(this.customerName);
        }
    },
    doLayoutTabs:function(panel){
        this.doLayoutOfAllTabs(panel);
        if(panel.id=='mrpshipmentcontractMasterContractTab' || panel.id=='mrppackagingcontractMasterContractTab'){
            this.addShipmentAndPackagingGridRecords();
        }else if(panel.id=='mrpbillingcontractMasterContractTab'){
            this.activateBillingContractTab();
        }
    },
    askToClose:function(){
        if(this.isClosable!==true){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.msgbox.51"),  //this.closeMsg,
                width:500,
                buttons: Wtf.MessageBox.YESNO,
                animEl: 'mb9',
                fn:function(btn){
                    if(btn!="yes"){return;
                    }else{
                        if(Wtf.dupsrno!=undefined){
                            Wtf.dupsrno.length=0;
                        }
                    }
                    this.ownerCt.remove(this);
                },
                scope:this,
                icon: Wtf.MessageBox.QUESTION
            });
        }
            return this.isClosable;
    },
    activateBillingContractTab: function(){
//        this.BillingContractForm.customerName = this.customerName;
        this.BillingContractForm.billingAddrsStore.load({
            params:{
                isBillingAddress:true,
                customerid: this.customerName
            }
        });
    },
    addShipmentAndPackagingGridRecords: function() {
        var temp = this.ContractDetailsForm.ProductGrid.gridStore.data;
        //Copy products from Contract Details grid to Shipment Contract grid
        this.copyProductsToShipppmentContract(temp);
        //Copy products from Contract Details grid to Packaging Contract grid
        this.copyProductsToPackagingContract(temp);
    },
    copyProductsToShipppmentContract: function(temp){

        this.ShipmentContractForm.ProductGrid.customerName = this.customerName;

        var productid = '';
        var shipmentdatacount;
        var rec;
        var index =  this.ShipmentContractForm.ProductGrid.gridStore.findBy(function(record) {
            if(record.get('productid')=='' || record.get('productid')==undefined){
                return true;
            }else{
                return false;
            }
        });

        var shipmentdata = this.ShipmentContractForm.ProductGrid.gridStore.data;
        if(index != -1){
            shipmentdatacount = shipmentdata.length-1;
        }else{
            shipmentdatacount = shipmentdata.length;
        }
        if(shipmentdatacount>0){
            for (var i = 0; i < temp.length; i++) {
                productid = temp.items[i].data.productid;

                var productFlag = this.checkIfProductExists(shipmentdata, productid);
                if(!productFlag && !(productid=='' || productid==undefined)){
                    rec = this.createShipmentProductRec(temp, i);
                    this.ShipmentContractForm.ProductGrid.gridStore.add(rec);
                }
            }
        }else{
            this.ShipmentContractForm.ProductGrid.gridStore.removeAll();
            for (var i = 0; i < temp.length; i++) {
                productid = temp.items[i].data.productid;
                if(!(productid=='' || productid==undefined)){
                    rec = this.createShipmentProductRec(temp, i);
                    this.ShipmentContractForm.ProductGrid.gridStore.add(rec);
                }
            }
        }
    },
    copyProductsToPackagingContract: function(temp){
        var productid = '';
        var packagingdatacount;
        var rec;
        var index =  this.PackagingContractForm.ProductGrid.gridStore.findBy(function(record) {
            if(record.get('productid')=='' || record.get('productid')==undefined){
                return true;
            }else{
                return false;
            }
        });

        var packagingdata = this.PackagingContractForm.ProductGrid.gridStore.data;
        if(index != -1){
            packagingdatacount = packagingdata.length-1;
        }else{
            packagingdatacount = packagingdata.length;
        }
        if(packagingdatacount>0){
            for (var i = 0; i < temp.length; i++) {
                productid = temp.items[i].data.productid;

                var productFlag = this.checkIfProductExists(packagingdata, productid);
                if(!productFlag && !(productid=='' || productid==undefined)){
                    rec = this.createPackagingProductRec(temp, i);
                    this.PackagingContractForm.ProductGrid.gridStore.add(rec);
                }
            }
        }else{
            this.PackagingContractForm.ProductGrid.gridStore.removeAll();
            for (var i = 0; i < temp.length; i++) {
                productid = temp.items[i].data.productid;
                if(!(productid=='' || productid==undefined)){
                    rec = this.createPackagingProductRec(temp, i);
                    this.PackagingContractForm.ProductGrid.gridStore.add(rec);
                }
            }
        }
    },
    createShipmentProductRec: function(temp, i){
        var quantity = temp.items[i].data.quantity;
        var rec;
        if(this.isEdit){
            var shippingperiodfrom = temp.items[i].data.shippingperiodfrom;
            var shippingfrom = '';
            if(shippingperiodfrom!='' && shippingperiodfrom!=undefined){
                shippingfrom = new Date(shippingperiodfrom);
            }
            var shippingperiodto = temp.items[i].data.shippingperiodto;
            var shippingto = '';
            if(shippingperiodto!='' && shippingperiodto!=undefined){
                shippingto = new Date(shippingperiodto);
            }
            rec = new this.ContractDetailsForm.ProductGrid.productRec({
                pid: temp.items[i].data.pid,
                productid: temp.items[i].data.productid,
                productname: temp.items[i].data.productname,
                desc: temp.items[i].data.desc,
                uomid: temp.items[i].data.uomid,
                baseuomrate: temp.items[i].data.baseuomrate,
                baseuomquantity: (quantity!=""&& quantity>0 && temp.items[i].data.baseuomrate!==undefined)?(temp.items[i].data.baseuomrate)*(temp.items[i].data.quantity):temp.items[i].data.baseuomquantity,//quantity get mutiplied with baseuomquantity when it non zero and empty
                rate: temp.items[i].data.rate,
                discamount: temp.items[i].data.discamount,
                deliverymode: temp.items[i].data.deliverymode,
                totalnoofunit: temp.items[i].data.totalnoofunit,
                totalquantity: temp.items[i].data.totalquantity,
                baseuomname: temp.items[i].data.baseuomname,
                baseuomid: temp.items[i].data.baseuomid,
                quantity: quantity ? quantity : 0.0,
                shippingperiodfrom: shippingfrom,
                shippingperiodto: shippingto,
                partialshipmentallowed: temp.items[i].data.partialshipmentallowed,
                shipmentstatus: temp.items[i].data.shipmentstatus,
                shippingagent: temp.items[i].data.shippingagent,
                loadingportcountry: temp.items[i].data.loadingportcountry,
                loadingport: temp.items[i].data.loadingport,
                transshipmentallowed: temp.items[i].data.transshipmentallowed,
                dischargeportcountry: temp.items[i].data.dischargeportcountry,
                dischargeport: temp.items[i].data.dischargeport,
                finaldestination: temp.items[i].data.finaldestination,
                postalcode: temp.items[i].data.postalcode,
                budgetfreightcost: temp.items[i].data.budgetfreightcost,
                shipmentcontratremarks: temp.items[i].data.shipmentcontratremarks,
                //Shipping Address
                shippingaddrscombo: temp.items[i].data.shippingaddrscombo,
                shippingaliasname: temp.items[i].data.shippingaliasname,
                shippingaddress: temp.items[i].data.shippingaddress,
                shippingcity: temp.items[i].data.shippingcity,
                shippingstate: temp.items[i].data.shippingstate,
                shippingcountry: temp.items[i].data.shippingcountry,
                shippingpostalcode: temp.items[i].data.shippingpostalcode,
                shippingphone: temp.items[i].data.shippingphone,
                shippingmobile: temp.items[i].data.shippingmobile,
                shippingfax: temp.items[i].data.shippingfax,
                shippingemail: temp.items[i].data.shippingemail,
                shippingrecipientname: temp.items[i].data.shippingrecipientname,
                shippingcontactperson: temp.items[i].data.shippingcontactperson,
                shippingcontactpersonnumber: temp.items[i].data.shippingcontactpersonnumber,
                shippingcontactcersondesignation: temp.items[i].data.shippingcontactcersondesignation,
                shippingwebsite: temp.items[i].data.shippingwebsite,
                shippingroute: temp.items[i].data.shippingroute
            });
        }else{
            rec = new this.ContractDetailsForm.ProductGrid.productRec({
                pid: temp.items[i].data.pid,
                productid: temp.items[i].data.productid,
                productname: temp.items[i].data.productname,
                desc: temp.items[i].data.desc,
                uomid: temp.items[i].data.uomid,
                baseuomrate: temp.items[i].data.baseuomrate,
                baseuomquantity: (quantity!=""&& quantity>0 && temp.items[i].data.baseuomrate!==undefined)?(temp.items[i].data.baseuomrate)*(temp.items[i].data.quantity):temp.items[i].data.baseuomquantity,//quantity get mutiplied with baseuomquantity when it non zero and empty
                rate: temp.items[i].data.rate,
                quantity: quantity ? quantity : 0.0,
                discamount: temp.items[i].data.discamount,
                deliverymode:"",
                totalnoofunit:"",
                totalquantity:"",
                shippingperiodfrom:"",
                shippingperiodto:"",
                partialshipmentallowed:"",
                shipmentstatus:"",
                shippingagent:"",
                loadingportcountry:"",
                loadingport:"",
                transshipmentallowed:"",
                dischargeportcountry:"",
                dischargeport:"",
                finaldestination:"",
                postalcode:"",
                budgetfreightcost:"",
                shipmentcontratremarks:""
            });
        }

        return rec;
    },
    createPackagingProductRec: function(temp, i){
        var rec;
        if(this.isEdit){
            rec = new this.ContractDetailsForm.ProductGrid.productRec({
                pid: temp.items[i].data.pid,
                productid: temp.items[i].data.productid,
                productname: temp.items[i].data.productname,
                desc: temp.items[i].data.desc,
                unitweightvalue: temp.items[i].data.unitweightvalue,
                unitweight: temp.items[i].data.unitweight,
                packagingtype: temp.items[i].data.packagingtype,
                certificaterequirement: temp.items[i].data.certificaterequirement,
                certificate: temp.items[i].data.certificate,
                shippingmarksdetails: temp.items[i].data.shippingmarksdetails,
                shipmentmode: temp.items[i].data.shipmentmode,
                percontainerload: temp.items[i].data.percontainerload,
                palletmaterial: temp.items[i].data.palletmaterial,
                packagingprofiletype: temp.items[i].data.packagingprofiletype,
                marking: temp.items[i].data.marking,
                drumorbagdetails: temp.items[i].data.drumorbagdetails,
                drumorbagsize: temp.items[i].data.drumorbagsize,
                numberoflayers: temp.items[i].data.numberoflayers,
                heatingpad: temp.items[i].data.heatingpad,
                palletloadcontainer: temp.items[i].data.palletloadcontainer
            });
        }else{
            rec = new this.ContractDetailsForm.ProductGrid.productRec({
                pid: temp.items[i].data.pid,
                productid: temp.items[i].data.productid,
                productname: temp.items[i].data.productname,
                desc: temp.items[i].data.desc,
                unitweightvalue:"",
                unitweight:"",
                packagingtype:"",
                certificaterequirement:"",
                certificate:"",
                shippingmarksdetails:"",
                shipmentmode:"",
                percontainerload:"",
                palletmaterial:"",
                packagingprofiletype:"",
                marking:"",
                drumorbagdetails:"",
                drumorbagsize:"",
                numberoflayers:"",
                heatingpad:"",
                palletloadcontainer:""
            });
        }

        return rec;
    },
    checkIfProductExists: function(data, productid){
        var datacount = data.length;
        for(var i = 0; i < datacount; i++){
            if(productid == data.items[i].data.productid || productid=='' || productid==undefined){
                return true;
            }
        }
        return false;
    },
    createBBarButtons: function() {
        this.saveBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            id: "save" + this.id,
            scope: this,
            handler: function() {
                this.saveAndCreateNewFlag = false;
                this.save();
            },
            iconCls: 'pwnd save'
        });
        this.buttonArray.push(this.saveBttn);
        
        this.savencreateBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            id: "savencreate" + this.id,
            scope: this,
            handler: function() {
                this.saveAndCreateNewFlag = true;// This flag is used to differentiate between Save button and Save and Create New button
                this.save();
            },
            iconCls: 'pwnd save'
        });
        if (!this.isEdit) {
            this.buttonArray.push(this.savencreateBttn);
        }
    },
    validMandatory: function() {
//
//        var contractDetailsItems = this.ContractDetailsForm.contractDetailsPanel.getForm().items;
//        for (var j = 0; j < contractDetailsItems.length; j++) {
//            if (contractDetailsItems.itemAt(j).allowBlank == false && (contractDetailsItems.itemAt(j).getValue() === "" || contractDetailsItems.itemAt(j).getValue() == undefined) && contractDetailsItems.itemAt(j).hidden == false) {
//                contractDetailsItems.itemAt(j).markInvalid();
//                var title = "Contract Details"
//                this.ContractDetailsForm.setTitle('<font color="red">' + title + '</font>');
//
//            }
//        }
////       var lengthTemp = this.DocumentRequiredForm.DocumentRequired.initialConfig.items[0].items[0].items.length;
//        var documentRequiredItems = this.DocumentRequiredForm.DocumentRequired.initialConfig.items[0].items[0].items[0].items;
//        for (var j = 0; j < documentRequiredItems.length; j++) {
//            if (documentRequiredItems[j].allowBlank == false && (documentRequiredItems[j].getValue() === "" || documentRequiredItems[j].getValue() == undefined) && documentRequiredItems[j].hidden == false) {
////                    documentRequiredItems.itemAt(j).markInvalid();
//                var title1 = "Document Required"
//                this.DocumentRequiredForm.setTitle('<font color="red">' + title1 + '</font>');
//
//            }
//        }


    },
    getContractDetails: function(){
        var jsonstring="";
        var jsonarray=new Array();

        var contractDetailsProductGrid = this.ContractDetailsForm.ProductGrid.gridStore.data;//Get Contract Details product grid data
        var contractDetailsProductGridCount = contractDetailsProductGrid.length-1;

        this.addShipmentAndPackagingGridRecords();//used to copy the contratc detail grid's data to shipment and packaging grid. 
        var shipmentContractProductGrid = this.ShipmentContractForm.ProductGrid.gridStore.data;//Get Shipment Contract product grid data

        var packagingContractProductGrid = this.PackagingContractForm.ProductGrid.gridStore.data;//Get Packaging Contract product grid data

        if(contractDetailsProductGridCount>0){
            for(var cnt=0 ; cnt<contractDetailsProductGridCount ; cnt++){
                var contractDetailsRec = contractDetailsProductGrid.items[cnt].data;
                var arr = [];
                    arr = Wtf.decode(WtfGlobal.getCustomColumnData(contractDetailsRec, Wtf.MRP_MASTER_CONTRACT_MODULE_ID).substring(13));
                    if(arr.length>0)
                        contractDetailsRec.customfield =arr;
                        
                var shipmentContractRec = shipmentContractProductGrid.items[cnt].data;
                var packagingContractRec = packagingContractProductGrid.items[cnt].data;

                var quantity=(contractDetailsRec.quantity==""||contractDetailsRec.quantity==undefined)?0:contractDetailsRec.quantity;
                jsonstring = {
                    //Contract details data
                    productid:contractDetailsRec.productid,
                    quantity:quantity,
                    desc:contractDetailsRec.desc,
                    customfield:contractDetailsRec.customfield,
                    //Shipment contract data
                    uomid:shipmentContractRec.uomid,
                    baseuomrate:shipmentContractRec.baseuomrate,
                    baseuomquantity:shipmentContractRec.baseuomquantity,
                    rate:shipmentContractRec.rate,
                    discamount:shipmentContractRec.discamount,
                    deliverymode:shipmentContractRec.deliverymode,
                    totalnoofunit:shipmentContractRec.totalnoofunit,
                    totalquantity:shipmentContractRec.totalquantity,
                    shippingperiodfrom:(shipmentContractRec.shippingperiodfrom!='' && shipmentContractRec.shippingperiodfrom!=undefined) ? WtfGlobal.convertToGenericDate(shipmentContractRec.shippingperiodfrom) : '',
                    shippingperiodto:(shipmentContractRec.shippingperiodto!='' && shipmentContractRec.shippingperiodto!=undefined) ? WtfGlobal.convertToGenericDate(shipmentContractRec.shippingperiodto) : '',
                    partialshipmentallowed:shipmentContractRec.partialshipmentallowed,
                    shipmentstatus:shipmentContractRec.shipmentstatus,
                    shippingagent:shipmentContractRec.shippingagent,
                    loadingportcountry:shipmentContractRec.loadingportcountry,
                    loadingport:shipmentContractRec.loadingport,
                    transshipmentallowed:shipmentContractRec.transshipmentallowed,
                    dischargeportcountry:shipmentContractRec.dischargeportcountry,
                    dischargeport:shipmentContractRec.dischargeport,
                    finaldestination:shipmentContractRec.finaldestination,
                    postalcode:shipmentContractRec.postalcode,
                    budgetfreightcost:shipmentContractRec.budgetfreightcost,
                    shipmentcontratremarks:shipmentContractRec.shipmentcontratremarks,
                    shippingaddress: this.getShippingAddress(),
                    //Packaging contract data
                    unitweightvalue:packagingContractRec.unitweightvalue,
                    unitweight:packagingContractRec.unitweight,
                    packagingtype:packagingContractRec.packagingtype,
                    certificaterequirement:packagingContractRec.certificaterequirement,
                    certificate:packagingContractRec.certificate,
                    shippingmarksdetails:packagingContractRec.shippingmarksdetails,
                    shipmentmode:packagingContractRec.shipmentmode,
                    percontainerload:packagingContractRec.percontainerload,
                    palletmaterial:packagingContractRec.palletmaterial,
                    packagingprofiletype:packagingContractRec.packagingprofiletype,
                    marking:packagingContractRec.marking,
                    drumorbagdetails:packagingContractRec.drumorbagdetails,
                    drumorbagsize:packagingContractRec.drumorbagsize,
                    numberoflayers:packagingContractRec.numberoflayers,
                    heatingpad:packagingContractRec.heatingpad,
                    palletloadcontainer:packagingContractRec.palletloadcontainer
                };
                jsonarray.push(jsonstring);
            }
        }

        return jsonarray;
    },
    getShippingAddress: function(){
        var shippingAddress = new Array();
        if(this.ShipmentContractForm.ProductGrid && this.ShipmentContractForm.ProductGrid.currentAddressDetailrec){
            var currentAddressDetailrec = this.ShipmentContractForm.ProductGrid.currentAddressDetailrec;
            shippingAddress.push(currentAddressDetailrec);
        }
        return shippingAddress;
    },
    save: function() {
        this.saveData();
    },
    getInvalidFields: function() {
        var tabs = new Array();
        var a = new Array();
        var b = new Array();
        b.push("Please fill the following fields.");
        b.toString().replace(/[*,]/gi, '')
        var cnt = 0;
        a.push("<div style = 'margin-left:50px'>");
        tabs.push(this.ContractDetailsForm.contractDetailsPanel.getForm().items, this.BillingContractForm.BillingContractForm.getForm().items, this.PaymentTermsForm.PaymentTermsForm.getForm().items, this.DocumentRequiredForm.DocumentRequired.getForm().items)
        for (var i = 0; i < tabs.length; i++) {
            var isMandatoryFlag = false;
            var fields = tabs[i].items;
            for (var j = 0; j < fields.length; j++) {
                if (fields[j].allowBlank == false && (fields[j].getValue() === "" || fields[j].getValue() == undefined) && fields[j].hidden == false) {
                    cnt++;
                    isMandatoryFlag = true;
                    fields[j].markInvalid();
                    a.push('<br />' + cnt + '.' + fields[j].fieldLabel);
                }
            }
//            if (isMandatoryFlag && i == 0) {
//                var title = this.ContractDetailsForm.title;
//                title = title.replace(/<(?:.|\n)*?>/gm, '');//regex used to remove unwanted HTML code
//                this.ContractDetailsForm.setTitle('<font color="red">' + title + '</font>');
//            }else{
//                this.ContractDetailsForm.setTitle(title);
//            }
//            if (isMandatoryFlag && i == 1) {
//                var title = this.BillingContractForm.title;
//                title = title.replace(/<(?:.|\n)*?>/gm, '');
//                this.BillingContractForm.setTitle('<font color="red">' + title + '</font>');
//            }else{
//                this.BillingContractForm.setTitle(title);
//            }
//            if (isMandatoryFlag && i == 2) {
//                var title = this.PaymentTermsForm.title;
//                title = title.replace(/<(?:.|\n)*?>/gm, '');
//                this.PaymentTermsForm.setTitle('<font color="red">' + title + '</font>');
//            }else{
//                this.PaymentTermsForm.setTitle(title);
//            }
        }
        //custom fields validation
        var invalidCustomFieldsArray = this.ContractDetailsForm.tagsFieldset.getInvalidCustomFields();
        for (var i = 0; i < invalidCustomFieldsArray.length; i++) {
            cnt++;
            var field=invalidCustomFieldsArray[i];
            var n = field.fieldLabel.indexOf('*');
            var label = field.fieldLabel.substring(0, n); 
            a.push('<br />' + cnt + '.' + label+'</span>');
        }
        a.push("</div>");
        
        var contractDetailsProductGrid = this.ContractDetailsForm.ProductGrid.gridStore.data;//Get Contract Details product grid data
        var contractDetailsProductGridCount = contractDetailsProductGrid.length-1;
        
        this.validflag = false;
        
        if (a.length > 2) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), b.toString().replace(/[*,]/gi, '').concat(a.toString().replace(/[*,]/gi, ''))], 2)
        } else if(contractDetailsProductGridCount < 1){
            WtfComMsgBox(33, 2);//Pop up message if product(s) are not selected.
        } else if(contractDetailsProductGridCount > 0) {
            var ctr=0;
            for(ctr=0 ; ctr<contractDetailsProductGridCount ; ctr++){
                var contractDetailsRec = contractDetailsProductGrid.items[ctr].data;
                if(contractDetailsRec.quantity==""||contractDetailsRec.quantity==undefined||contractDetailsRec.quantity<0){
                    //If product(s) are selected then quanity should be > 0
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+" "+contractDetailsRec.productname+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    break;
                }
            }
            if(ctr==contractDetailsProductGridCount){
                this.validflag = true;
            }
        } else {
            this.validflag = true;
        }
    },
    saveData: function() {
        this.getInvalidFields();
        if(this.validflag){
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.savdat"),
            msg: WtfGlobal.getLocaleText("acc.je.msg1"),
            scope: this,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width: 300,
            fn: function(btn) {
                if (btn != "yes") {
                    this.enableSaveButton();
                    return;
                }
                
                //Contract Details data
                var contractDetailsJsonObject = this.ContractDetailsForm.contractDetailsPanel.getForm().getValues();
                contractDetailsJsonObject.customer = this.ContractDetailsForm.Customer.getValue();
                contractDetailsJsonObject.contractterm = this.ContractDetailsForm.contractTerm.getValue();
                var creationDate = this.ContractDetailsForm.creationDate.getValue();
                if(creationDate!='' && creationDate!=undefined){
                    contractDetailsJsonObject.creationdate = WtfGlobal.convertToGenericDate(this.ContractDetailsForm.creationDate.getValue());
                }else{
                    contractDetailsJsonObject.creationdate = '';
                }
                var fromDate = this.ContractDetailsForm.fromDate.getValue();
                if(fromDate!='' && fromDate!=undefined){
                    contractDetailsJsonObject.contractstartdate = WtfGlobal.convertToGenericDate(this.ContractDetailsForm.fromDate.getValue());
                }else{
                    contractDetailsJsonObject.contractstartdate = '';
                }
                var toDate = this.ContractDetailsForm.toDate.getValue();
                if(toDate!='' && toDate!=undefined){
                    contractDetailsJsonObject.contractenddate = WtfGlobal.convertToGenericDate(this.ContractDetailsForm.toDate.getValue());
                }else{
                    contractDetailsJsonObject.contractenddate = '';
                }
                var seqFormatRec = WtfGlobal.searchRecord(this.ContractDetailsForm.sequenceFormatStore, this.ContractDetailsForm.sequenceFormatCombobox.getValue(), 'id');
                contractDetailsJsonObject.seqformat_oldflag = seqFormatRec != null ? seqFormatRec.get('oldflag') : true;
                contractDetailsJsonObject.parentcontractid = this.ContractDetailsForm.parentContractId.getValue();
                
                //Get values for diabled fields
                WtfGlobal.onFormSumbitGetDisableFieldValues(this.ContractDetailsForm.contractDetailsPanel.getForm().items, contractDetailsJsonObject);
                
                var contractdetailsarray = [];
                var contractdetailsdata = {};
                contractdetailsarray.push(contractDetailsJsonObject);
                contractdetailsdata.contractDetailsJsonObject  = contractdetailsarray;
                
                //Billing Contract data
                var billingcontractarray = [];
                var billingcontractdata = {};
                if(this.BillingContractForm.BillingContractForm.getForm().getValues()){
                    var billingContractFormValues = this.BillingContractForm.BillingContractForm.getForm().getValues();

                    billingcontractarray.push(billingContractFormValues);
                    billingcontractdata.billingContractObject = billingcontractarray;
                }
                
                //Payment Tems data
                var paymenttermsdataarray = [];
                var paymenttermsdata = {};
                if(this.PaymentTermsForm.PaymentTermsForm.getForm().getValues()){
                    var paymentTermsValues = this.PaymentTermsForm.PaymentTermsForm.getForm().getValues();
                    paymentTermsValues.paymentmethodname = this.PaymentTermsForm.paymentMethodName.getValue();
                    paymentTermsValues.paymenttermname = this.PaymentTermsForm.paymentTermName.getValue();
                    
                    paymentTermsValues.paymenttermdate = WtfGlobal.convertToGenericDate(this.PaymentTermsForm.paymentTermDate.getValue());
                    
                    paymenttermsdataarray.push(paymentTermsValues);
                    paymenttermsdata.paymentTermsObject = paymenttermsdataarray;
                }
                
                //Document Required data
                var documentrequireddataarray = [];
                var documentrequireddata = {};
                if(this.DocumentRequiredForm.DocumentRequired.getForm().getValues()){
                    var documentRequiredValues = this.DocumentRequiredForm.DocumentRequired.getForm().getValues();
                    var dateOfAggrement = this.DocumentRequiredForm.dateOfAggrement.getValue();
                    if(dateOfAggrement!='' && dateOfAggrement!=undefined){
                        documentRequiredValues.dateOfAggrement = WtfGlobal.convertToGenericDate(this.DocumentRequiredForm.dateOfAggrement.getValue());
                    }else{
                        documentRequiredValues.dateOfAggrement = '';
                    }
                    documentRequiredValues.countryAggrement = this.DocumentRequiredForm.countryAggrement.getValue();
                    if(this.DocumentRequiredForm.uploadForm){
                        documentRequiredValues.savedFilesMappingId = this.DocumentRequiredForm.uploadForm.savedFilesMappingId;
                    }
                    
                    documentrequireddataarray.push(documentRequiredValues);
                    documentrequireddata.documentRequiredObject = documentrequireddataarray;
                }
                
                //Product grid data
                var details = this.getContractDetails();
                var detailsdata = {};
                detailsdata.detailsObject = details;
                

                if(this.isEdit && this.record){
                    this.mastercontractid = this.record.data.id;
                }
                //Global custom data
                var custFieldArr = this.ContractDetailsForm.tagsFieldset.createFieldValuesArray();
                
                WtfComMsgBox(27, 4, true);
                var ajxUrl = "ACCContractMasterCMN/saveMasterContract.do";
                Wtf.Ajax.requestEx({
                    url: ajxUrl,
                    params: {
                        contractdetailsdata:JSON.stringify(contractdetailsdata),
                        billingcontractdata:JSON.stringify(billingcontractdata),
                        paymenttermsdata:JSON.stringify(paymenttermsdata),
                        documentrequireddata:JSON.stringify(documentrequireddata),
                        detailsdata:JSON.stringify(detailsdata),
                        customfield: JSON.stringify(custFieldArr),
                        mastercontractid: this.mastercontractid,
                        isEdit:this.isEdit
                        }
                    }, this, this.genSuccessResponse, this.genFailureResponse);
                }
            }, this);
        }
    },
    genSuccessResponse: function(response, request) {
        Wtf.MessageBox.hide();
        if (response.success) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                msg:this.isEdit ? WtfGlobal.getLocaleText("acc.mastercontract.update.msg") : WtfGlobal.getLocaleText("acc.mastercontract.save.msg"),
                width: 450,
                scope: {
                    scopeObj: this
                },
                buttons: Wtf.MessageBox.OK,
                fn: function(btn ,text, option) {
                    this.scopeObj.refreshReportGrid();
                },
                animEl: 'mb9',
                icon: Wtf.MessageBox.INFO
            });
        }
        this.isClosable=true;
        if (this.saveAndCreateNewFlag) {
            this.enableSaveButton();
            this.resetComponents();
            

        } else {
            this.disableComponent();
            this.disableSaveButton();
            
        }
    },
    refreshReportGrid: function(){
        var comp = null;
        comp = Wtf.getCmp('contractmasterlistEntry');
        if(comp){
            comp.fireEvent('mastercontractupdate');
        }  
    },
    resetComponents: function() {
        //Reset form fields & product grid
        if (this.ContractDetailsForm.contractDetailsPanel && this.ContractDetailsForm.ProductGrid) {
            this.ContractDetailsForm.contractDetailsPanel.getForm().reset();
            if (this.ContractDetailsForm.tagsFieldset) {
                this.ContractDetailsForm.tagsFieldset.resetCustomComponents();
            }
            this.ContractDetailsForm.ProductGrid.gridStore.removeAll();
        }
        if(this.ShipmentContractForm.ProductGrid){
            this.ShipmentContractForm.ProductGrid.gridStore.removeAll();
        }
        if(this.PackagingContractForm.ProductGrid){
            this.PackagingContractForm.ProductGrid.gridStore.removeAll();
        }
        if (this.BillingContractForm.BillingContractForm) {
            this.BillingContractForm.BillingContractForm.getForm().reset();
        }
        if (this.PaymentTermsForm.PaymentTermsForm) {
            this.PaymentTermsForm.PaymentTermsForm.getForm().reset();
        }
        if (this.DocumentRequiredForm.DocumentRequired) {
            this.DocumentRequiredForm.DocumentRequired.getForm().reset();
        }
        
        //Set new value for sequence
        this.setSequenceFormatForCreateNewCase();// when form is reset on 'save and create new' case, default sequence format will be set to combobox again.
    },
    setSequenceFormatForCreateNewCase:function(){
        if(this.ContractDetailsForm){
            var seqRec = this.ContractDetailsForm.sequenceFormatStore.getAt(0)
            this.ContractDetailsForm.sequenceFormatCombobox.setValue(seqRec.data.id);
            var count = this.ContractDetailsForm.sequenceFormatStore.getCount();
            for (var i = 0; i < count; i++) {
                seqRec = this.ContractDetailsForm.sequenceFormatStore.getAt(i)
                if (seqRec.json.isdefaultformat == "Yes") {
                    this.ContractDetailsForm.sequenceFormatCombobox.setValue(seqRec.data.id)
                    break;
                }
            }
            this.ContractDetailsForm.getNextSequenceNumber(this.ContractDetailsForm.sequenceFormatCombobox); 

            if(this.ContractDetailsForm.ProductGrid){
                //Set 1st table panel i.e. COntract Details Tab active for save & create new case
                this.ContractDetailsForm.ProductGrid.gridStoreOnLoad();
            }
        }
    },
    disableComponent: function() {
        if (this.ContractDetailsForm && this.ContractDetailsForm.contractDetailsPanel && this.ContractDetailsForm.ProductGrid) {
            this.ContractDetailsForm.contractDetailsPanel.getForm().items.each(function(itm) {
                itm.setDisabled(true)
            });
            this.ContractDetailsForm.disable();
//            this.ContractDetailsForm.ProductGrid.items.each(function(itm){itm.setDisabled(true)});
            var columnModel = this.ContractDetailsForm.ProductGrid.itemsgrid.getColumnModel();
            var noOfColumns = this.ContractDetailsForm.ProductGrid.itemsgrid.getColumnModel().config.length;
            for (var i = 0; i < noOfColumns; i++) {
                columnModel.setEditable(i, false);
            }
            this.ContractDetailsForm.ProductGrid.purgeListeners();
        }
        if (this.ShipmentContractForm && this.ShipmentContractForm.ProductGrid) {
            var columnModel = this.ShipmentContractForm.ProductGrid.itemsgrid.getColumnModel();
            var noOfColumns = this.ShipmentContractForm.ProductGrid.itemsgrid.getColumnModel().config.length;
            for (var i = 0; i < noOfColumns; i++) {
                columnModel.setEditable(i, false);
            }
            this.ShipmentContractForm.ProductGrid.purgeListeners();
        }
        if (this.BillingContractForm && this.BillingContractForm.BillingContractForm) {
            this.BillingContractForm.BillingContractForm.disable();
        }
        if (this.PaymentTermsForm && this.PaymentTermsForm.PaymentTermsForm) {
            this.PaymentTermsForm.PaymentTermsForm.disable();
        }
        if (this.PackagingContractForm && this.PackagingContractForm.ProductGrid) {
            var columnModel = this.PackagingContractForm.ProductGrid.itemsgrid.getColumnModel();
            var noOfColumns = this.PackagingContractForm.ProductGrid.itemsgrid.getColumnModel().config.length;
            for (var i = 0; i < noOfColumns; i++) {
                columnModel.setEditable(i, false);
            }
            this.PackagingContractForm.ProductGrid.purgeListeners();
        }
        if (this.DocumentRequiredForm && this.DocumentRequiredForm.DocumentRequired) {
            this.DocumentRequiredForm.DocumentRequired.disable();
        }
    },
    genFailureResponse: function(response) {
        Wtf.MessageBox.hide();
        this.enableSaveButton();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);

    },
    enableSaveButton: function() {
        this.saveBttn.enable();
        this.savencreateBttn.enable();
    },
    disableSaveButton: function() {
        this.saveBttn.disable();
        this.savencreateBttn.disable();
    }
});


/*
 * Address Window Start
 */
Wtf.account.shippingAddressWindow = function(config) {

    var buttonArray = new Array();
    this.currentaddress= config.currentaddress!=undefined ? config.currentaddress : "";
    this.customerName=config.customerName;
    this.shippingComboValueBeforeSelect="";
    this.gridData = config.gridData
    this.isEdit = config.isEdit != undefined ? config.isEdit : false;
    
    Wtf.apply(this, config);

    this.closeButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close"),
        minWidth: 50,
        scope: this,
        handler: this.closeWindow.createDelegate(this)
    });

    this.saveButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
        minWidth: 50,
        id: 'savebutton' + this.id,
        scope: this,
        handler: this.saveData.createDelegate(this)
    });

    buttonArray.push(this.saveButton);
    buttonArray.push(this.closeButton);

    Wtf.apply(this, {
        buttons: buttonArray
    });
    Wtf.account.shippingAddressWindow.superclass.constructor.call(this, config);

    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.shippingAddressWindow, Wtf.Window, {
    layout: "border",
    closable: true,
    resizable: false,
    border: false,
    initComponent: function(config) {
        Wtf.account.shippingAddressWindow.superclass.initComponent.call(this, config);

        this.createStore();

        this.createFields();
    },
    onRender: function(config) {
        Wtf.account.shippingAddressWindow.superclass.onRender.call(this, config);

        var centerPanel = new Wtf.Panel({
            id: 'addressdetails' + this.id,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:0px 0px 0px 0px;',
            region: 'center',
            autoScroll: true,
            border: false,
            width: 300,
            items: [
                this.addressDetailForm
            ]
        });

        this.add(centerPanel);

        this.shippingAddrsCombo.on('beforeselect',function(combo){
            this.shippingComboValueBeforeSelect=combo.getValue();
        },this);
        this.shippingAddrsCombo.on('select',this.setShippingAddressDataOnSelect,this);

    },
    createStore: function() {

        this.addrsRec = new Wtf.data.Record.create([
            {name: 'aliasName'},
            {name: 'address'},
            {name: 'county'},
            {name: 'city'},
            {name: 'state'},
            {name: 'country'},
            {name: 'postalCode'},
            {name: 'phone'},
            {name: 'mobileNumber'},
            {name: 'fax'},
            {name: 'emailID'},
            {name: 'contactPerson'},
            {name: 'recipientName'},
            {name: 'contactPersonNumber'},
            {name: 'contactPersonDesignation'},
            {name: 'website'},
            {name: 'shippingRoute'},
            {name: 'isDefaultAddress'},
            {name: 'isBillingAddress'}]);

        this.ShippingAddrsStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.addrsRec),
            url: "ACCCustomer/getAddresses.do",
            baseParams: {
                isBillingAddress: false,
                customerid: this.customerName
            }
        });
        this.ShippingAddrsStore.load();
        this.ShippingAddrsStore.on('load', this.ShippingAddrsStoreOnLoad, this);
    },
    createFields: function() {
        this.shippingAliasName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.mastercontract.aliasname"),
            name: "shippingaliasname",
            id: this.id + 'shippingaliasname',
            anchor: '85%'
        });

        this.shippingAddress = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.customer.address"),
            name: "shippingaddress",
            id: this.id + 'shippingaddress',
            maxLength: 250,
            height: 60,
            allowBlank: false,
            allowNegative: false,
            anchor: '85%'
        });

        this.shippingCounty = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.County"),
            name: "shippingcounty",
            id: this.id + 'shippingcounty',
            anchor: '85%',
            hidden: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US),
            hideLabel: !(Wtf.account.companyAccountPref.countryid == Wtf.Country.US)
        });
        
        this.shippingCity = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.City"),
            name: "shippingcity",
            id: this.id + 'shippingcity',
            anchor: '85%'
        });

        this.shippingState = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.State"),
            name: "shippingstate",
            id: this.id + 'shippingstate',
            anchor: '85%'
        });

        this.shippingCountry = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Country"),
            name: "shippingcountry",
            id: this.id + 'shippingcountry',
            anchor: '85%'
        });

        this.shippingPostalCode = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.PostalCode"),
            name: "shippingpostalcode",
            id: this.id + 'shippingpostalcode',
            anchor: '85%'
        });

        this.shippingPhone = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Phone"),
            name: 'shippingphone',
            id: this.id + 'shippingphone',
            anchor: '85%'
        });

        this.shippingMobile = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.Mobile"),
            name: 'shippingmobile',
            id: this.id + 'shippingmobile',
            anchor: '85%'
        });

        this.shippingFax = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.fax"),
            name: 'shippingfax',
            id: this.id + 'shippingfax',
            anchor: '85%'
        });

        this.shippingEmail = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.email"),
            name: 'shippingemail',
            id: this.id + 'shippingemail',
            allowBlank: true,
            anchor: '85%',
            validator: WtfGlobal.validateMultipleEmail
        });

        this.messagePanelShipping = new Wtf.Panel({
            xtype: 'panel', border: false,
            cls: 'emailfieldInfoInContactDetails',
            html: WtfGlobal.getLocaleText("acc.mail.seperator.comma")
        });

        this.shippingRecipientName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.RecipientName"),
            name: 'shippingrecipientname',
            id: this.id + 'shippingrecipientname',
            anchor: '85%'
        });

        this.shippingContactPerson = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPerson"),
            name: 'shippingcontactperson',
            id: this.id + 'shippingcontactperson',
            anchor: '85%'
        });

        this.shippingContactNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonNumber"),
            name: 'shippingcontactpersonnumber',
            id: this.id + 'shippingcontactpersonnumber',
            anchor: '85%'
        });

        this.shippingContactDesignation = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.ContactPersonDesignation"),
            name: 'shippingcontactcersondesignation',
            id: this.id + 'shippingcontactcersondesignation',
            anchor: '85%'
        });

        this.shippingWebsite = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.address.website"),
            name: 'shippingwebsite',
            id: this.id + 'shippingwebsite',
            anchor: '85%'
        });

        this.shippingRoute = new Wtf.form.TextField({
            fieldLabel: 'Route',
            name: 'shippingroute',
            id: this.id + 'shippingroute',
            anchor: '85%'
        });


        this.shippingAddrsCombo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            name: 'shippingaddrscombo',
            mode: 'local',
            valueField: 'aliasName',
            displayField: 'aliasName',
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectShippingAddress..."),
            store: this.ShippingAddrsStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShippingAddress*"),
            anchor: '85%',
            typeAhead: true,
            forceSelection: true
        });



        this.addressDetailForm = new Wtf.form.FormPanel({
            border: false,
            layout: 'form',
            bodyStyle: 'padding:10px 10px 10px 10px;',
            autoHeight: true,
            autoWidth: true,
            items: [{
                    layout: 'form',
                    baseCls: 'northFormFormat',
                    labelWidth: 110,
                    defaults: {
                        border: false
                    },
                    items: [{
                            layout: 'column',
                            defaults: {
                                border: false
                            },
                            items: [{
                                    layout: 'form',
                                    columnWidth: 0.50,
                                    items: [
                                        this.shippingAddrsCombo, this.shippingAliasName, this.shippingAddress, this.shippingCounty, this.shippingCity, this.shippingState, this.shippingCountry,
                                        this.shippingPostalCode, this.shippingPhone, this.shippingMobile, this.shippingFax
                                    ]
                                }, {
                                    layout: 'form',
                                    columnWidth: 0.50,
                                    items: [
                                        this.shippingEmail, this.messagePanelShipping, this.shippingRecipientName, this.shippingContactPerson, this.shippingContactNumber,
                                        this.shippingContactDesignation, this.shippingWebsite, this.shippingRoute
                                    ]
                                }]
                        }]
                }]
        });
        
        this.addressDetailForm.on('render', this.addressDetailFormOnRender, this);

    },
    addressDetailFormOnRender: function(){
        if(this.isEdit && this.gridData){
            var data = this.gridData.items[0].data
            this.shippingAliasName.setValue(data.shippingaliasname);
            this.shippingAddress.setValue(data.shippingaddress);
            this.shippingCounty.setValue(data.shippingcounty);
            this.shippingCity.setValue(data.shippingcity);
            this.shippingState.setValue(data.shippingstate);
            this.shippingCountry.setValue(data.shippingcountry);
            this.shippingPostalCode.setValue(data.shippingpostalcode);
            this.shippingPhone.setValue(data.shippingphone);
            this.shippingMobile.setValue(data.shippingmobile);
            this.shippingFax.setValue(data.shippingfax);
            this.shippingEmail.setValue(data.shippingemail);
            this.shippingRecipientName.setValue(data.shippingrecipientname);
            this.shippingContactPerson.setValue(data.shippingcontactperson);
            this.shippingContactNumber.setValue(data.shippingcontactpersonnumber);
            this.shippingContactDesignation.setValue(data.shippingcontactcersondesignation);
            this.shippingWebsite.setValue(data.shippingwebsite);
            this.shippingRoute.setValue(data.shippingroute);
        }
    },
    ShippingAddrsStoreOnLoad: function(){
        if(this.isEdit && this.gridData){
            var data = this.gridData.items[0].data
            this.shippingAddrsCombo.setValue(data.shippingaddrscombo);
        }
    }, 
    closeWindow: function() {
        this.close();
    },
    addDefaultShippingAddressInStore: function() {
        var storeRec = new Wtf.data.Record({
            aliasName: "Shipping Address1",
            isBillingAddress: false
        });
        this.ShippingAddrsStore.add(storeRec)
        this.ShippingAddrsStore.commitChanges();
    },
    saveData: function() {
        if(!this.addressDetailForm.getForm().isValid()){
           WtfComMsgBox(2,2);
            return;
        } else{
            this.currentaddress=this.addressDetailForm.getForm().getValues();
//            this.currentaddress.linkedDocumentCombo=this.linkedDocumentCombo.getValue();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.field.Youhavesuccessfullyaddedyouraddressdetail")],3);
            this.fireEvent("update",this);

            this.closeWindow();
        }
    },
    
    setShippingAddressDataOnSelect:function(combo,rec){
        if(combo.getValue()==this.shippingComboValueBeforeSelect){ //If same name selected no need to do any action 
            return;
        }
        if(this.currentaddress!="" && this.currentaddress.shippingAddrsCombo==rec.data.aliasName){
            this.setShippingAddress(this.currentaddress);
        } else{
            this.setDefaultShippingAddress(rec.data);
        }
    },
    
    setShippingAddress:function(addr){
        this.shippingAliasName.setValue(addr.aliasName);
        this.shippingAddress.setValue(addr.shippingAddress);
        this.shippingCounty.setValue(addr.shippingCounty);
        this.shippingCity.setValue(addr.shippingCity);
        this.shippingState.setValue(addr.shippingState);
        this.shippingCountry.setValue(addr.shippingCountry);
        this.shippingPostalCode.setValue(addr.shippingPostal);
        this.shippingPhone.setValue(addr.shippingPhone);
        this.shippingMobile.setValue(addr.shippingMobile);
        this.shippingFax.setValue(addr.shippingFax);
        this.shippingEmail.setValue(addr.shippingEmail);
        this.shippingRecipientName.setValue(addr.shippingRecipientName);
        this.shippingContactPerson.setValue(addr.shippingContactPerson);
        this.shippingContactNumber.setValue(addr.shippingContactPersonNumber);
        this.shippingContactDesignation.setValue(addr.shippingContactPersonDesignation);
        this.shippingWebsite.setValue(addr.shippingWebsite);
        this.shippingRoute.setValue(addr.shippingRoute);
    },
    
    setDefaultShippingAddress:function(addr){
        this.shippingAliasName.setValue(addr.aliasName);
        this.shippingAddress.setValue(addr.address);
        this.shippingCounty.setValue(addr.county);
        this.shippingCity.setValue(addr.city);
        this.shippingState.setValue(addr.state);
        this.shippingCountry.setValue(addr.country);
        this.shippingPostalCode.setValue(addr.postalCode);
        this.shippingPhone.setValue(addr.phone);
        this.shippingMobile.setValue(addr.mobileNumber);
        this.shippingFax.setValue(addr.fax);
        this.shippingEmail.setValue(addr.emailID);
        this.shippingContactPerson.setValue(addr.contactPerson);
        this.shippingRecipientName.setValue(addr.recipientName);
        this.shippingContactNumber.setValue(addr.contactPersonNumber);
        this.shippingContactDesignation.setValue(addr.contactPersonDesignation);
        this.shippingWebsite.setValue(addr.website);
        this.shippingRoute.setValue(addr.shippingRoute);
    }
});

/*
 * Address Window End
 */
