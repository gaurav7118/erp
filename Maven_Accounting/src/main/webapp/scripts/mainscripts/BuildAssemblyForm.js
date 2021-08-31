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
Wtf.account.BuildAssemblyForm = function(config){
    Wtf.apply(this,config);
    /*
     * To fetch batches separately for Customer Assembly Type Products
     */
    this.isForCustomerAssembly = false; 
    /*
     * to fetch product type of selected Assembly product (Normal Assembly/ Customer Assembly)
     */
    this.productType = "";
    Wtf.account.BuildAssemblyForm.superclass.constructor.call(this, config);
    this.isEdit=config.isEdit;
}
Wtf.extend(Wtf.account.BuildAssemblyForm, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.BuildAssemblyForm.superclass.onRender.call(this, config);
        this.isUnbuildAssembly = this.isUnbuildAssembly;
        this.createStores();
        this.createFields();
        this.createAssemblyGrid();
        this.createForm();
        this.ProductSerialJson = [];
        this.subProductJson = [];
        this.currentQuantity=1;
        this.currentLocation="";
        this.currentWarehouse="";
        this.currentBatch="";
        this.initialStoreData;
        this.exportRecord = [];
        Wtf.totalQtyAddedInBuild=0;
        this.add({
            layout:"border",
            border: false,
            items:[{
                region: "center",
                layout: "border",
                border: false,
                items:[this.selectProduct,this.ProductAssemblyGrid,this.BuildForm]
            }],
            bbar:[this.savebtton,this.cancelbtton, this.singleRowPrint]
        });
        this.addEvents({
            'update':true,
            'closed':true
        });
        this.sequenceFormatStore.on('load',this.setSequenceFormat,this);
        this.sequenceFormatCombobox.on('select',this.getNextSequenceNumber,this)
    },

    createStores:function(){//this method create store for sequence format
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'value'},
            {name: 'oldflag'}
        ]);  
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName,
                isEdit:false 
            }
        });
    },
    
    setSequenceFormat:function(config){//this method used to set default sequence format on combo when store loaded
        if(this.sequenceFormatStore.getCount()>0){
            var count=this.sequenceFormatStore.getCount();
            for(var i=0;i<count;i++){
                var seqRec=this.sequenceFormatStore.getAt(i);
                if(seqRec.json.isdefaultformat=="Yes"){
                    this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                    break;
                }
            }
            if(this.sequenceFormatCombobox.getValue()!=""){
                this.getNextSequenceNumber(this.sequenceFormatCombobox);  
            } else{
                this.RefNo.setValue("");
                this.RefNo.disable();
            }
        }
        /*
         * Hiding customer and Jobworkorder fields intially.
         * Done as Hide and hideLabel configs are not working properly
         */
        WtfGlobal.hideFormElement(this.Name);
        WtfGlobal.hideFormElement(this.jobWorkOrderRecCombo);
    },
    
    getNextSequenceNumber:function(combo){
        if(this.isViewMode){//view mode
            this.sequenceFormatCombobox.disable();
            this.RefNo.disable();  
            var index=WtfGlobal.searchRecordIndex(this.sequenceFormatStore,this.record.data.sequenceformatid,"id");
            if(index!=-1){
                this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
            } else {
                this.sequenceFormatCombobox.setValue("NA"); 
            }
        } else{//create new case
            if(combo.getValue()=="NA"){
                this.RefNo.enable();
                this.RefNo.setValue("");
            } else {
                Wtf.Ajax.requestEx({
                    url:"ACCCompanyPref/getNextAutoNumber.do",
                    params:{
                        from:218,
                        sequenceformat:combo.getValue(),
                        oldflag:false
                    }
                }, this,function(resp){
                    if(resp.data=="NA"){
                        this.RefNo.enable();
                        this.RefNo.setValue("");
                    }else {
                        this.currentBatch=resp.data;
                        this.RefNo.setValue(resp.data);
                        this.RefNo.disable();
                    }
                });
            }
        }
    },
    
    createFields:function(){
        this.savebtton= new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                scope:this,
                handler : this.addnNewBOMCode.createDelegate(this),
                //handler:this.saveForm.createDelegate(this),
                iconCls :'pwnd save',
                disabled : this.isViewMode ? true : false
        });
        this.cancelbtton= new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
                scope:this,
                handler:this.closeForm.createDelegate(this),
                iconCls :'pwnd cancel', // issue 30734 icon for cancel is not proper have to change
                disabled : this.isViewMode ? true : false
        });
        this.singleRowPrint = new Wtf.exportButton({//ERM-26 Print Reocrd button
            obj: this,
            id: "printSingleRecord" + this.id,
            iconCls: 'pwnd printButtonIcon',
            text: WtfGlobal.getLocaleText("acc.rem.236"),
            tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record details',
            disabled: true,
            filename: WtfGlobal.getLocaleText("acc.productList.buildAssembly"),
            exportRecord: this.exportRecord,
            isEntrylevel:true,
            menuItem: {rowPrint: true},
            get: Wtf.autoNum.buildAssemblyReport,
            moduleid: Wtf.Build_Assembly_Report_ModuleId
        });
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            id:'sequenceFormatCombobox'+this.heplmodeid+this.id,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",
            valueField:'id',
            displayField:'value',
            store:this.sequenceFormatStore,
            anchor:'70%',
            typeAhead: true,
            forceSelection: true,
            name:'sequenceformat',
            hiddenName:'sequenceformat',
            allowBlank:false
        });
        this.sequenceFormatStore.load();
        
        this.RefNo= new Wtf.form.TextField({
            fieldLabel: this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild.refNo") : WtfGlobal.getLocaleText("acc.build.1")+"*",  //'Build Ref No.*',
            name: 'refno',
            allowBlank:false,
            anchor:'70%',
            readOnly : this.isViewMode ? true : false
        });
          this.costToBuild= new Wtf.form.NumberField({
            fieldLabel: this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild.totalcost") : WtfGlobal.getLocaleText("acc.build.3"),  //'Total Cost To Build',
            name: 'cost',
            value:0,
            cls:"clearStyle",
            readOnly:true,
            hidden:!(Wtf.dispalyUnitPriceAmountInSales && Wtf.dispalyUnitPriceAmountInPurchase),
            hideLabel: !(Wtf.dispalyUnitPriceAmountInSales && Wtf.dispalyUnitPriceAmountInPurchase)
        });
        this.BuildQuantity= new Wtf.form.NumberField({
            fieldLabel: this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild.quantity") : WtfGlobal.getLocaleText("acc.build.2")+"*",  //'Quantity to Unbuild' / 'Quantity to build*',
            name: 'quantity',
            allowBlank:false,
            maxLength:15,
            //allowDecimals:false,
            allowNegative:false,
            anchor:'70%',
            disabled: true,
            readOnly : this.isViewMode ? true : false,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            validator: function(val) {
                if (this.initialConfig.scope.isSerialActivatedForParentProd) {
                    if (val % 1 > 0) { // check if quantity contains frictional value
                        this.initialConfig.scope.BuildQuantity.setValue(val - (val % 1)); //removed friction value and set new value
                        return true;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            },
            scope:this
        }); 
        
        if(!this.isViewMode) {
            this.BuildQuantity.on('blur',this.setCostToBuild,this);
        }
        
        this.memo = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.memo"),  //'Memo',
            name: 'memo',
            height: 50,
            maxLength:255,
            anchor:'70%',
            readOnly : this.isViewMode ? true : false,
            value : this.isUnbuildAssembly ? "Un-Build Assembly Document" : "Build Assembly Document"
        });
      
        this.OnHandQuantity= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.4"),  //'Available Quantity',
            name: 'qoh',
            value:0,
            cls:"clearStyle",
            readOnly:true,
            decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
            hidden: this.isViewMode,
            hideLabel: this.isViewMode
        });
        this.assemblyProductCost= new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.5"),  //'Unit Product Cost',
            name: 'cost',
            value:0,
            cls:"clearStyle",
            readOnly: true,
            hidden: this.isViewMode ||!(Wtf.dispalyUnitPriceAmountInSales && Wtf.dispalyUnitPriceAmountInPurchase),
            hideLabel: this.isViewMode || !(Wtf.dispalyUnitPriceAmountInSales && Wtf.dispalyUnitPriceAmountInPurchase)
        });
        this.desc = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.description"),  //'Description',
            name: 'desc',
            height: 70,
            anchor: '100%',
            cls:"clearStyle",
//            readOnly:true
            readOnly : this.isViewMode ? true : false
        });
        this.productRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'pid'},
            {name:'productname'},
            {name:'desc'},
            {name:'quantity'},
            {name:'producttype'},
            {name:'type'},
            {name: 'location'},
            {name: 'warehouse'},
            {name: 'isLocationForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'isRowForProduct'},
            {name: 'isRackForProduct'},
            {name: 'isBinForProduct'},
            {name: 'isBatchForProduct'},
            {name: 'isSerialForProduct'},
            {name: 'isSKUForProduct'},
            {name:'purchaseprice'}
        ]);
        this.productStore = new Wtf.data.Store({
            url: "ACCProduct/getProductsByType.do",
            baseParams: {
                mode: 28,
                type: "'" + Wtf.producttype.assembly + "','" + Wtf.producttype.customerAssembly + "'",
                isBuild: true //Assembly products , isBuild : true {to fetch both assembly and customer assembly}
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            },this.productRec)
        });
        
        this.productStore.on("load", this.loadForm,this);
        
        var productComboConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.build.6") + "*", //'Assembly Product*',            
            name: 'product',
            store: this.productStore,
            emptyText: "",
            anchor: "95%",
            hideLabel: false,
            valueField: 'productid',
            maxHeight:250,
            displayField: 'pid',
            extraFields: ['productname'],
            disabled: this.isViewMode ? true : false
        }
        
        this.AssemblyProducts = CommonERPComponent.createProductPagingComboBox('auto', 500, Wtf.ProductCombopageSize, this, undefined, false, false, productComboConfig);
        
        /*
         * Customer Field
         */
        this.Name = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.invoiceList.cust.tt") + "'>" + WtfGlobal.getLocaleText("acc.invoiceList.cust") + "</span>",
            id: "customer" + this.id,
            store: Wtf.customerAccRemoteStore,
            valueField: 'accid',
            displayField: 'accname',
            minChars: 1,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 550 : 400,
//            allowBlank: !this.isJobWorkInReciever,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'remote',
//            hidden: true,
//            hideLabel: true,
//            disabled:true,
            typeAhead: true,
            extraComparisionField: 'acccode',
            typeAheadDelay: 30000,
            forceSelection: true,
            selectOnFocus: true,
            isVendor: false,
            isCustomer: true,
            width: 200,
            triggerAction: 'all',
            scope: this
        });
        this.SOStoreRec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'discount'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'duedate', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'creditoraccount'},
            {name:'personid'},
            {name:'shipping'},
            {name:'othercharges'},
            {name:'taxid'},
            {name:'productid'},
            {name:'discounttotal'},
            {name:'isAppliedForTax'},// in Malasian company if DO is applied for tax
            {name:'discountispertotal',type:'boolean'},
            {name:'currencyid'},
            {name:'currencysymbol'},
            {name:'amount'},
            {name:'amountinbase'},
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'lasteditedby'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'includeprotax',type:'boolean'},
            {name:'salesPerson'},
            {name:'islockQuantityflag'},
            {name:'agent'},
            {name:'termdetails'},
            {name:'LineTermdetails'},//Line Level Term Details
            {name:'shiplengthval'},
            {name:'gstIncluded'},
            {name:'quotationtype'},
            {name:'contract'},
            {name:'termid'},
            {name:'externalcurrencyrate'},//    ERP-9886
            {name:'customerporefno'},
            {name:'isexpenseinv'},
            {name: 'billingAddressType'},
            {name: 'billingAddress'},
            {name: 'billingCountry'},
            {name: 'billingState'},
            {name: 'billingPostal'},
            {name: 'billingEmail'},
            {name: 'billingFax'},
            {name: 'billingMobile'},
            {name: 'billingPhone'},
            {name: 'billingContactPerson'},
            {name: 'billingRecipientName'},
            {name: 'billingContactPersonNumber'},
            {name: 'billingContactPersonDesignation'},
            {name: 'billingWebsite'},
            {name: 'billingCounty'},
            {name: 'billingCity'},
            {name: 'shippingAddressType'},
            {name: 'shippingAddress'},
            {name: 'shippingCountry'},
            {name: 'shippingState'},
            {name: 'shippingCounty'},
            {name: 'shippingCity'},
            {name: 'shippingEmail'},
            {name: 'shippingFax'},
            {name: 'shippingMobile'},
            {name: 'shippingPhone'},
            {name: 'shippingPostal'},
            {name: 'shippingContactPersonNumber'},
            {name: 'shippingContactPersonDesignation'},
            {name: 'shippingWebsite'},
            {name: 'shippingRecipientName'},
            {name: 'shippingContactPerson'},
            {name: 'shippingRoute'},
            {name: 'vendcustShippingAddress'},
            {name: 'vendcustShippingCountry'},
            {name: 'vendcustShippingState'},
            {name: 'vendcustShippingCounty'},
            {name: 'vendcustShippingCity'},
            {name: 'vendcustShippingEmail'},
            {name: 'vendcustShippingFax'},
            {name: 'vendcustShippingMobile'},
            {name: 'vendcustShippingPhone'},
            {name: 'vendcustShippingPostal'},
            {name: 'vendcustShippingContactPersonNumber'},
            {name: 'vendcustShippingContactPersonDesignation'},
            {name: 'vendcustShippingWebsite'},
            {name: 'vendcustShippingContactPerson'},
            {name: 'vendcustShippingRecipientName'},
            {name: 'vendcustShippingAddressType'}
        ]);
        this.SOStore = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getSalesOrders.do",
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                isJobWorkOrderReciever:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SOStoreRec)
        });
        /*Job Work Order Field
         * */
        this.jobWorkOrderRecCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"remote",
            typeAhead:true,
            forceSelection:true,
            store:this.SOStore,
            name:"jobworkorderno",
//            hideLabel:true,
//            hidden:true,
//            disabled:true,
            displayField:"billno",
            valueField:"billid",
            fieldLabel:WtfGlobal.getLocaleText("acc.jobWorkOrder.vendorjobworkorder"),
//            allowBlank:!this.isJobWorkInReciever,
            width:200,
            disabled:true,
            parent:this
        });
        
        this.SOStore.on('beforeload',function(s,o){
            if(!o.params) {
                o.params={};
            }
            var currentBaseParams = this.SOStore.baseParams;
            currentBaseParams.id=this.Name.getValue();
            this.SOStore.baseParams=currentBaseParams;        
        },this); 
        
        this.Name.on("change",function(){
            this.jobWorkOrderRecCombo.enable();
            this.jobWorkOrderRecCombo.reset();
            this.SOStore.load();
        },this);
        this.billDate = new Wtf.form.DateField({
            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.he.12") +"'>"+' '+(this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuildassembly.gridBuildDate") : WtfGlobal.getLocaleText("acc.buildassembly.gridBuildDate")) +"</span>",
            id:"invoiceDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'assemblybilldate',
            anchor:"95%",
            allowBlank:false,
            disabled : this.isViewMode ? true : false
        });
        this.bomRec = new Wtf.data.Record.create([
            {name: 'bomid'},
            {name: 'bomCode'},
            {name: 'bomName'}
        ]);
        
        this.bomStore = new Wtf.data.Store({
            url: "ACCProduct/getBOMDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.bomRec)
        });
        
        this.bomCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.mrp.field.bomcode"),
            name: 'bomid',
            store: this.bomStore,
            valueField: 'bomid',
            displayField: 'bomCode',
            allowBlank: false,
            mode: 'remote',
            typeAhead: true,
            disabled : this.isViewMode ? true : false,
            anchor:"95%",
            triggerAction: 'all',
            listWidth: 300,
            extraFields: ['bomName'],
            addNoneRecord: false
        });
        this.selectedProductID="";
        this.AssemblyProducts.on("change",function(cmb,newval,oldval){
            if(newval!=oldval){
                this.selectedProductID = newval;
                this.bomStore.load({
                    params: {
                        productid: newval
                    }
                }, this);
                this.ProductAssemblyGrid.itemsgrid.getStore().removeAll();
                this.bomCombo.reset();
                var prec = this.productStore.getAt(this.productStore.find("productid",newval));
                if (prec) {
                    var ptype = prec.data.producttype;
                    this.productType = ptype;
                    /*
                     * Handled Customer and Job Work Order Field Hide And Show
                     * on Selecting Product 
                     * if producttype is Customer Assembly, then showing both the Fields
                     * else Hiding both the Fields
                     */
                    if (ptype === Wtf.producttype.customerAssembly) {
                        WtfGlobal.showFormElement(this.Name);
                        WtfGlobal.showFormElement(this.jobWorkOrderRecCombo);
                        this.isForCustomerAssembly = true;
                    } else {
                        this.isForCustomerAssembly = false;
                        WtfGlobal.hideFormElement(this.Name);
                        WtfGlobal.hideFormElement(this.jobWorkOrderRecCombo);
                    }
                    
                    /*
                     * Reseting both Customer and Job work order fields on change event of Product combo
                     */
                    this.Name.reset();
                    this.jobWorkOrderRecCombo.reset();

                }
                return;
                this.ProductAssemblyGrid.setProduct(newval);
                this.desc.setValue(prec.data.desc);
                this.OnHandQuantity.setValue(prec.data.quantity);
                this.assemblyProductCost.setValue(prec.data.purchaseprice);
//                this.costToBuild.setValue(getRoundedAmountValue(prec.data.purchaseprice)); 
                this.BuildQuantity.enable();
                this.BuildQuantity.setValue(1);
                if(prec.data.isSerialForProduct){
                    this.isSerialActivatedForParentProd=true; 
                }else{
                    this.isSerialActivatedForParentProd=false; 
                }
//                if(!prec.data.isSerialForProduct){
                      this.setProductCost();
//                }
                }
            
            if (this.ProductAssemblyGrid != undefined) {
                this.ProductAssemblyGrid.updateCostinAssemblyGrid();
            }
        },this);
        this.bomCombo.on("change",function(cmb,newval,oldval){
            if(newval!=oldval){
                this.ProductAssemblyGrid.originalStore.removeAll();
                this.ProductAssemblyGrid.setProductWithBOM(this.selectedProductID,newval);
                var prec = this.productStore.getAt(this.productStore.find("productid",this.selectedProductID));
                this.desc.setValue(prec.data.desc);
                this.OnHandQuantity.setValue(prec.data.quantity);
                this.assemblyProductCost.setValue(prec.data.purchaseprice);
//                this.costToBuild.setValue(getRoundedAmountValue(prec.data.purchaseprice)); 
                this.BuildQuantity.enable();
                this.BuildQuantity.setValue(1);
                if(prec.data.isSerialForProduct){
                    this.isSerialActivatedForParentProd=true; 
                }else{
                    this.isSerialActivatedForParentProd=false; 
                }
//                if(!prec.data.isSerialForProduct){
                      this.setProductCost();
//                }
                }
            
            if (this.ProductAssemblyGrid != undefined) {
                this.ProductAssemblyGrid.updateCostinAssemblyGrid();
            }
        },this);
        this.bomStore.on('beforeload', function(s, o) {
            if (!o.params)
                o.params = {};
            var currentBaseParams = this.bomStore.baseParams;
            currentBaseParams.productid =this.selectedProductID;
            this.bomStore.baseParams = currentBaseParams;
        }, this);
        this.AssemblyProducts.on("select",function(cmb,record,index){
            if (record.data.isSerialForProduct) {
                this.isSerialActivatedForParentProd = true;
            } else {
                this.isSerialActivatedForParentProd = false;    
            }
            if (this.ProductAssemblyGrid != undefined) {
                this.ProductAssemblyGrid.updateCostinAssemblyGrid();
            }
        },this);
                
       // Wtf.productStore.on("load",this.reLoadProductStore,this);
       if(Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_type_ahead || Wtf.account.companyAccountPref.productOptimizedFlag== Wtf.Products_on_Submit|| Wtf.productStore.getCount()>0){
           this.reLoadProductStore();
       }else{
           Wtf.productStore.on("load",this.reLoadProductStore,this);
       }
    },
    
    setCostToBuild : function(){
        if((this.OnHandQuantity.getValue()<this.BuildQuantity.getValue()) && this.isUnbuildAssembly){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),(WtfGlobal.getLocaleText("acc.unbuild.quantityexceed"))+' '+WtfGlobal.getLocaleText("acc.nee.54_1")+' '+WtfGlobal.getLocaleText("acc.field.is")+(this.OnHandQuantity.getValue())+'. '+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")], 2);
                        this.BuildQuantity.setValue(1);
                        this.setProductCost();
                        //alert("Quantity given in Unbuild Assembly are exceeding the quantity available.Maximum available Quantity is ");
                        return true;
        }
            if(this.BuildQuantity.getValue() == '0'){
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"), (this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild1") : WtfGlobal.getLocaleText("acc.build.7")));
                    this.BuildQuantity.setValue(1);
                    this.setProductCost();
//                    this.costToBuild.setValue(this.assemblyProductCost.getValue());
            }else{ //check whether product is available or not to build the product when we enter the quntity to build
                var flag=false;
                var confirmForQtyExceed=false;
                for(var j=0;j<this.ProductAssemblyGrid.gridStore.getCount();j++){
                    var prodID = this.ProductAssemblyGrid.gridStore.getAt(j).data['productid'];
                    if(prodID=="" || prodID==undefined){
                        continue;
                    }
                    var producttype = this.ProductAssemblyGrid.gridStore.getAt(j).data['producttype'];
                    if(producttype!="4efb0286-5627-102d-8de6-001cc0794cfa" && !this.isUnbuildAssembly){//service
                        var onhand = this.ProductAssemblyGrid.gridStore.getAt(j).data['onhand'];  //available quantity of individual product
                        var quantityneeded = this.ProductAssemblyGrid.gridStore.getAt(j).data['quantity'];// individual product quantity needed to build product 
                        var lockquantity = this.ProductAssemblyGrid.gridStore.getAt(j).data['lockquantity'];//available quantity of individual product  // individual product lock quantity needed to build product 
                        var productName = this.ProductAssemblyGrid.gridStore.getAt(j).data['productname'];
                        var totalquantity=this.BuildQuantity.getValue(); 
                        
                        var isWarehouseForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isWarehouseForProduct'];
                        var isLocationForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isLocationForProduct'];
                        var isRowForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isRowForProduct'];
                        var isRackForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isRackForProduct'];
                        var isBinForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isBinForProduct'];
                        var iswarlocrowrackbinAnyActivated= (isWarehouseForProduct || isLocationForProduct || isRowForProduct || isRackForProduct || isBinForProduct);
                        var isBatchForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isBatchForProduct'];
                        var isSerialForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isSerialForProduct'];
                        if((totalquantity*quantityneeded)>(onhand))  //(total quantity to build*quantity needed)=required quantity <=(available quantity of indivisual product)
                        {
                            if (iswarlocrowrackbinAnyActivated && !Wtf.account.companyAccountPref.isnegativestockforlocwar) {
                                flag = true;
                                break;
                            } else if (iswarlocrowrackbinAnyActivated && Wtf.account.companyAccountPref.isnegativestockforlocwar && (isBatchForProduct || isSerialForProduct)) { // product that has WLRRB enabled & BS disabled 
                                flag = true;
                                break;
                            } else if (!iswarlocrowrackbinAnyActivated && !isBatchForProduct && !isSerialForProduct) { // product that has everything disabled (WLRRBBS propertly)
                                confirmForQtyExceed=true;
                                if(flag){
                                    break;
                                }
                            }else if (iswarlocrowrackbinAnyActivated && Wtf.account.companyAccountPref.isnegativestockforlocwar ) { 
                                confirmForQtyExceed=true;
                                if(flag){
                                    break;
                                }
                            }
                        }
                    }
                }
                if ((flag && confirmForQtyExceed)) {    //ERP-29035
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"), (this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild.quantityexceed") : WtfGlobal.getLocaleText("acc.field.QuantitygiveninBAareexceedingthequantityavailable")) + '<br>' + WtfGlobal.getLocaleText("acc.nee.54") + ' ' + productName + ' ' + WtfGlobal.getLocaleText("acc.field.is") + (onhand) + '.<br><b>' + WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed") + '</b>'], 2);
                    this.BuildQuantity.setValue(1);
                    this.setProductCost();
                    return false;
                } else if(confirmForQtyExceed && !flag){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), (this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild.quantityexceed") : WtfGlobal.getLocaleText("acc.field.QuantitygiveninBAareexceedingthequantityavailable")) + '<br>' + WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed") + '</center>', function(btn) {
                        if (btn == "yes") {
                            this.setCostToBuildsub();
                        } else {
                            this.BuildQuantity.setValue(1);
                            this.setProductCost();
                            return false;
                        }
                    }, this);
                }else{
                    this.setCostToBuildsub();
                }
                     Wtf.unqsrno.length=0;
            }
        
    },
    setCostToBuildsub:function()
    {
        /*
         * If producttype = Customer Assembly, and Customer and Job work order fields are not selected/Empty
         * then, showing message to select both and return
         */
        if (this.productType === Wtf.producttype.customerAssembly && !this.jobWorkOrderRecCombo.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.jobworkorder.buildAssembly.warning")],2);   //Batch and serial no details are not valid.
            return;
        }
        var value=this.BuildQuantity.getValue() * this.assemblyProductCost.getValue();
//        this.costToBuild.setValue(parseFloat(getRoundedAmountValue(value)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
        //if(Wtf.account.companyAccountPref.showprodserial== true) 
        var assemblyProduct=this.AssemblyProducts.getValue();
        var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productStore, assemblyProduct, 'productid');
        if(productComboRecIndex >=0){
            var proRecord = this.productStore.getAt(productComboRecIndex);
            var isAnyOfWLRRBBSEnable=false; 
            isAnyOfWLRRBBSEnable=proRecord.data.isLocationForProduct || proRecord.data.isWarehouseForProduct || proRecord.data.isRowForProduct || proRecord.data.isRackForProduct || proRecord.data.isBinForProduct || proRecord.data.isBatchForProduct || proRecord.data.isSerialForProduct;
            if(!isAnyOfWLRRBBSEnable){ // if Assembly product does not contain any of these then check for BOM product in order to check whether WLBS window is to be opened.
                for(var j=0;j<this.ProductAssemblyGrid.gridStore.getCount();j++){
                    var prodID = this.ProductAssemblyGrid.gridStore.getAt(j).data['productid'];
                    if(prodID=="" || prodID==undefined){
                        continue;
                    }
                    var producttype = this.ProductAssemblyGrid.gridStore.getAt(j).data['producttype'];
                    if(producttype!="4efb0286-5627-102d-8de6-001cc0794cfa" && !this.isUnbuildAssembly){//service
                        var isWarehouseForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isWarehouseForProduct'];
                        var isLocationForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isLocationForProduct'];
                        var isRowForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isRowForProduct'];
                        var isRackForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isRackForProduct'];
                        var isBinForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isBinForProduct'];
                        var isBatchForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isBatchForProduct'];
                        var isSerialForProduct = this.ProductAssemblyGrid.gridStore.getAt(j).data['isSerialForProduct'];
                        isAnyOfWLRRBBSEnable= (isWarehouseForProduct || isLocationForProduct || isRowForProduct || isRackForProduct || isBinForProduct || isBatchForProduct || isSerialForProduct);
                        if(isAnyOfWLRRBBSEnable){
                            break;
                        }
                    }
                }
            }
            if(proRecord.data.type!='Service' && proRecord.data.type!='Non-Inventory Part'){  //serial no for only inventory type of product
               if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory  || Wtf.account.companyAccountPref.isRowCompulsory  || Wtf.account.companyAccountPref.isRackCompulsory  || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                    if(isAnyOfWLRRBBSEnable) 
                    {
                        this.callSerialNoWindow(this.BuildQuantity.getValue());
                    }
                }
              }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.batchserial.funforInventoryitems")],2);   //Batch and serial no details are not valid.
                return;
            }
        }
//        if(!proRecord.data.isSerialForProduct){
            this.setProductCost();
//        }
    },
    setProductCost:function(){
        var assemblyProduct=this.AssemblyProducts.getValue();
        var bomJson=this.ProductAssemblyGrid.getAssemblyJson();
        Wtf.Ajax.requestEx({
            url: "ACCReports/getPriceCalculationForAsseblyProduct.do",    
            params:{
                productid:assemblyProduct,
                buildquantity:this.BuildQuantity.getValue(),
                bomjson :bomJson,
                bomdetailid:this.bomCombo.getValue()
            }
        },this,function(res,req){
//            var results = eval("(" + trimStr(res.responseText) + ")");
            var totalprice=0;
            var buildQty=this.BuildQuantity.getValue();
            if(res){
                var valuationArray=res.valuationArray;
                for(var i=0;i<valuationArray.length;i++){
                    var recObj=valuationArray[i];
                    var GridRec=this.ProductAssemblyGrid.gridStore.find("productid",recObj.productid);
//                    this.ProductAssemblyGrid.gridStore.getAt(0).data.purchaseprice=recObj.buildcost;
                    var rec=this.ProductAssemblyGrid.gridStore.getAt(GridRec);
//                    rec.set("purchaseprice",recObj.buildcost);
//                    var formatedBuildCost = getRoundedAmountValue(recObj.buildcost*rec.data.quantity);/*SDP-1599*/
                    var formatedBuildCost = getRoundedAmountValue(recObj.buildcost);/*SDP-1599*/
                    totalprice+=(formatedBuildCost*buildQty);
                }
                this.costToBuild.setValue(getRoundedAmountValue(totalprice));
//                this.ProductAssemblyGrid.getView().refresh(true);
                if (this.ProductAssemblyGrid != undefined) {
                    this.ProductAssemblyGrid.updateCostinAssemblyGrid();
                }
            }
                
        });
    },
    callSerialNoWindow:function(currentQty){
            var assemblyProduct=this.AssemblyProducts.getValue();
            var bomid = this.bomCombo.getValue();
        var index=this.productStore.findBy(function(rec){
            if(rec.data.productid==assemblyProduct)
                return true;
            else
                return false;
        })
        var prorec=this.productStore.getAt(index); 
        var ttlQty=this.BuildQuantity.getValue();
        var location=prorec.data.location;
        var warehouse=prorec.data.warehouse;
        var batch="";
        if(!this.isUnbuildAssembly){    //In case of Unbuild assembly, Ref.No would not be Batch ID.
            batch=(!prorec.data.isBatchForProduct ? "" : this.RefNo.getValue());
        }
        
//        if(currentQty>1){
//        location=this.currentLocation;
//        warehouse=this.currentWarehouse;
////        batch=this.currentBatch;
//        }
        Wtf.dupsrno = []//Need to initialize this variable when component get called. ERP-25459
        this.batchDetailswin=new Wtf.account.AssemblySerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),//   title:'Select Warehouse, Batch & Serial Number(s)',
            productName:prorec.data.productname,
            quantity:1,
            type:prorec.data.type,
            currentQuantity:currentQty,
            totalquantity:ttlQty,
            defaultLocation:location,
            productid:prorec.data.productid,
            bomid : bomid,
            isSales:false,
//            batchDetails:this.ProductSerialJson[currentQty - 1],
            batchDetails:this.ProductSerialJson[0],
            isLocationForProduct:prorec.data.isLocationForProduct,
            isWarehouseForProduct:prorec.data.isWarehouseForProduct,
            isRowForProduct:prorec.data.isRowForProduct,
            isRackForProduct:prorec.data.isRackForProduct,
            isBinForProduct:prorec.data.isBinForProduct,
            isBatchForProduct:prorec.data.isBatchForProduct,
            subProductJson:this.subProductJson,
            isSerialForProduct:prorec.data.isSerialForProduct,
            isSKUForProduct:prorec.data.isSKUForProduct,
            productAssemblyGrid:this.ProductAssemblyGrid,
            costToBuild:this.costToBuild,
            buildQuantity:this.BuildQuantity,
            assemblyProductCost:this.assemblyProductCost,
            /*
             * passing jobworkorderid and  isForCustomerAssembly for fecthing Jobwork related batches only
             */
            jobworkorderid:this.isForCustomerAssembly?this.jobWorkOrderRecCombo.getValue():"",
            isForCustomerAssembly:this.isForCustomerAssembly,
            defaultWarehouse:warehouse,
            defaultBatch:batch,
            modal:true,
            width:950,
            height:500, //ERP-11109 [SJ]
            autoScroll:true, //ERP-11109 [SJ]
            resizable : false,
            closable:false,
            parentObj:this,
            isUnbuildAssembly:this.isUnbuildAssembly,
            modifiedStore : this.ProductAssemblyGrid.gridStore,
            moduleId: Wtf.Build_Assembly_Report_ModuleId
        });
       
        this.batchDetailswin.on("close",function(){
          if (this.ProductAssemblyGrid != undefined) {
            this.ProductAssemblyGrid.updateCostinAssemblyGrid();
          }   
            var qty=this.batchDetailswin.currentQuantity;
          if(qty<=ttlQty && qty!=0){
             this.callSerialNoWindow(qty);
          }
            
            
        },this);
        this.batchDetailswin.on("beforclosewin",function(){
           this.currentQuantity=this.batchDetailswin.currentQuantity;
           this.currentLocation=this.batchDetailswin.currentLocation;
           this.currentWarehouse=this.batchDetailswin.currentWarehouse;
           this.currentBatch=this.batchDetailswin.currentBatch;
            if (this.ProductAssemblyGrid != undefined) {
               this.ProductAssemblyGrid.updateCostinAssemblyGrid();
            }
        },this);
        this.batchDetailswin.on("beforeclose",function(){
            this.batchDetails=this.batchDetailswin.getBatchDetails();
            prorec.set("batchdetails",this.batchDetails);
//            this.ProductSerialJson[this.currentQuantity-1]=this.batchDetailswin.getBatchDetails();
            this.ProductSerialJson[0]=this.batchDetailswin.getBatchDetails();
            if (this.ProductAssemblyGrid != undefined) {
               this.ProductAssemblyGrid.updateCostinAssemblyGrid();
            }
        },this);
        this.batchDetailswin.show();
    
    },

    reLoadProductStore : function(){
        if(Wtf.getCmp(this.id)){ //Load assembly product store if component exists
            this.productStore.reload();
        } else {//Remove event handler if tab does not exists
            Wtf.productStore.un("load",this.reLoadProductStore,this);
        }
    },

    loadForm: function(){
        if(this.productid!=""){
            this.AssemblyProducts.setValue(this.record.data.mainproductid);
            this.ProductAssemblyGrid.setParams('ACCProduct/getProductBatchDetails.do',this.prodbuildid,'25',this.record.data.mainproductid);
            var prec = this.productStore.getAt(this.productStore.find("productid", this.record.data.mainproductid));
            this.desc.setValue(this.record.data.description);
            this.OnHandQuantity.setValue(prec.data.quantity);
            this.assemblyProductCost.setValue(prec.data.purchaseprice);
            this.BuildQuantity.enable();
            this.BuildQuantity.setValue(this.record.data.quantity);
//            this.costToBuild.setValue(prec.data.purchaseprice*this.BuildQuantity.getValue());
//            if(!prec.data.isSerialForProduct){
                this.setProductCost();
//            }
            this.RefNo.setValue(this.record.data.productrefno);
            this.memo.setValue(this.record.data.memo);
            if (this.record != undefined && this.record.data != undefined) {
                this.bomCombo.setValForRemoteStore(this.record.data.bomdetailid, this.record.data.bomCode);
            }
        }
        this.productStore.un("load", this.loadForm,this);
    },

    createAssemblyGrid:function(){
        this.ProductAssemblyGrid = new Wtf.account.productAssemblyGrid({
            layout:"fit",
             cls:'gridFormat',
             region: 'center',
       //                 border: false,
                 //       baseCls: 'bckgroundcolor',
        //                bodyStyle:"padding:20px",
        //    border:false,
            height:250,
            gridtitle:this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild.component") : WtfGlobal.getLocaleText("acc.build.8"), //"Components Needed To Build",
            productid:null,
            isBuildAssemblyFlag:true,
            isInitialQuatiy:true, 
            rendermode:this.isUnbuildAssembly ? "unbuildproduct" : "buildproduct", //To identify the call for BOM Grid either from Assembly Build OR Unbuild Assembly
            readOnly:this.isViewMode,
            parent:this,
            isUnbuildAssembly : this.isUnbuildAssembly,
            moduleId: Wtf.Build_Assembly_Report_ModuleId
        });
    },
    
    createForm:function(){
        this.selectProduct=new Wtf.form.FormPanel({
            region: 'north',
            height: 135,
            border:false,
            items:[{
            layout:'form',
            baseCls:'northFormFormat',
            labelWidth:130,
            items:[{
                layout:'column',
                border:false,
                defaults:{border:false},
                items:[{
                        layout:'form',
                        columnWidth:0.3,
                        border:false,
                        defaultType: 'textfield',
                        defaults:{width:200},
                        items:[this.AssemblyProducts,this.bomCombo ,this.billDate]                     
                    },{
                        layout:"form",
                        border:false,
                        labelWidth:110,
                        columnWidth:0.3,
                        defaultType: 'textfield',
                        defaults:{width:200},
                        items:[this.OnHandQuantity,this.assemblyProductCost,this.Name,this.jobWorkOrderRecCombo]        
                    },{
                        layout:"form",
                        border:false,
                        labelWidth:80,
                        columnWidth:0.25,
                        defaultType: 'textfield',
                        defaults:{width:200},
                        items:[
                            this.desc,
                            {xtype:'hidden',name:'productid'}
                        ]
                    }]
                }]
            }]
       });

       this.selectProduct.on('render',this.setDate,this);
       this.BuildForm=new Wtf.form.FormPanel({
            region: 'south',
            height: 150,
            border: false,
            baseCls: 'bckgroundcolor',
            layout: 'form',
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding:0px 10px 10px 10px",
            labelWidth:120,
            items:[{
                layout:'column',
                border: false,
                defaults:{border:false},
                items:[{
                    layout:'form',
                    columnWidth:0.35,
                    items:[this.BuildQuantity,
                           this.sequenceFormatCombobox,
                           this.RefNo,
                           this.memo]
                	},{
                		labelWidth:110,
                		layout:'form',
                		items:[this.costToBuild]
                	}]
            }]
       });
    },
    setDate: function () {
        if(!this.isViewMode){
//            this.billDate.setValue(Wtf.serverDate);
            this.billDate.setValue(new Date());
        } else {
            var billdate  = new Date(this.record.json.entrydate);   //ERP-21570
            this.billDate.setValue(billdate);
        }
    },
    closeForm:function(){
        this.fireEvent('closed',this);
    },

    saveAndUpdateForm:function(){
        if(this.isEdit){
            Wtf.Ajax.requestEx({
                url:"INVGoodsTransfer/deleteProductBuildAssembly.do", //ACCProduct/deleteProductBuildAssembly.do
                params:{
                    productids:this.prodbuildid,
                    isEdit:this.isEdit,
                    mainproduct:this.AssemblyProducts.getValue(),
                    assmbledProdQty:this.record.data.quantity
                }
            },this,this.saveForm());
        }else{
            this.isEdit=false;
            this.saveForm();
        }
    },

    addnNewBOMCode : function(){
        this.addnewBOM = new Wtf.form.FormPanel({
            id : 'newbompanel'+this.id,
            labelWidth:100,
            frame:true,
            border:false,
            width:320,
            height:100,        
            defaultType:'textfield',
            monitorValid:true,
            items:[this.newBomCode = new Wtf.form.TextArea({
                id : 'bomcode'+this.id,
                fieldLabel:"New BOM Code",
                autoWidth: true,
                height:25,
                allowBlank: false
            }), this.newBomName = new Wtf.form.TextArea({
                id : 'bomname'+this.id,
                fieldLabel:"New BOM Name",
                autoWidth: true,
                height:25,
                allowBlank: false
            })]
        });
        this.createWindow = Wtf.getCmp("bomcodewindow"+this.id); 
        if(this.createWindow==null){        
            this.createWindow = new Wtf.Window({
                frame:true,
                id:"bomcodewindow"+this.id,
                title:'New BOM Details',
                width:330,
                height:150,
                modal:true,
                cope:this,
                items: this.addnewBOM,
                buttons:[{
                    text: "Save",
                    id: 'savenewbom' + this.id,
                    scope: this,
                    handler: function() {
                        if(this.newBomCode.getValue()=="" || this.newBomName.getValue()==""){
                            return false;
                        } else {
                            this.createWindow.close();
                            this.saveForm();
                        }
                    }
                },{
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    id : 'cancelnewbom'+this.id,
                    scope: this,
                    handler: function() {
                        this.createWindow.close();
                    }
                }]
            });
        }
        this.isStoreUpdated = false;
        /*Check whether store is updated or not on the basis of No.of records, Product ID, Quantity, Percentage. If anyone of the thing is changed then we will consider 
            that Store is changed & we will save it as New Bom Formula.
        */
        if(this.ProductAssemblyGrid!=undefined){
            this.orignalStoreData = this.ProductAssemblyGrid.originalStore;
            this.updatedStoreData = this.ProductAssemblyGrid.gridStore;
            if(this.orignalStoreData.getCount()!=this.updatedStoreData.getCount()-1){
                this.isStoreUpdated = true;
            }
            for(var j=0; j<this.updatedStoreData.getCount(); j++){
                var record = this.updatedStoreData.getAt(j);
                var prodid = record.data.productid;
                if(prodid=="" || prodid==undefined){
                    continue;   //Skipt the blank record
                }
                var newInventoryquantiy = record.data.inventoryquantiy;
                var newPercentatge = record.data.percentage;
                var index = this.orignalStoreData.find('productid',prodid);
                if(index<0){    
                    this.isStoreUpdated = true;//This product ID is not available in Original Store Data
                    break;
                } else {    //This product ID is available in Original Store Data. So Check for Quantity & Percentage change
                    var oldRecord = this.orignalStoreData.getAt(index);
                    var oldInventoryquantiy = oldRecord.data.inventoryquantiy;
                    var oldPercenatge = oldRecord.data.percentage;
                    if(newInventoryquantiy!=oldInventoryquantiy){
                        this.isStoreUpdated = true;
                        break;
                    } else if(newPercentatge!=oldPercenatge){
                        this.isStoreUpdated = true;
                        break;
                    }
                }
            }//for
        
            //If Store is updated then Show Window to take new BOM Code from User
            if(this.isStoreUpdated && !this.isUnbuildAssembly){
                this.createWindow.show();
            } else {
                this.saveForm();    //No Change in Store.
            }
        }
    },
    saveForm:function(){
        var obj=this;
        var prodOnHandQuantity ;
        var prodQuantityNeeded ;
        var productname;
        var newOnHandQuantity;
        var negstockflag= false;
        var onhandquantityflag=false;
        var newval=this.BuildQuantity.getValue();
        var oldval=1;  
        if(this.BuildQuantity.getValue()==0){
            this.BuildQuantity.markInvalid(this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild.quantity.errmsg") : WtfGlobal.getLocaleText("acc.build.9"));
            return;
        }
        var invalidProducts = this.checkForDeactivatedProducts();
            if(invalidProducts!=''){
            Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), 
                    msg: WtfGlobal.getLocaleText("acc.buildassembly.followingProductsAreDeactivated")+'</br>'+'<b>'+invalidProducts+'<b>',
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this,
                    scopeObj :this,
                    fn: function(btn){
                        if(btn=="ok"){
                            return;
                        }
                    }
                });
                return;
        } 
        var assembleProduct=this.AssemblyProducts.getValue();
         var prorec=this.productStore.getAt(this.productStore.find('productid',assembleProduct));
          var batchSerialDetail = this.batchDetails;
          //if(Wtf.account.companyAccountPref.showprodserial){    //check wether batch and serial no detail entered or not
           if(Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory){    //check wether batch and serial no detail entered or not
            if(prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isBatchForProduct || prorec.data.isSerialForProduct){ 
                if(batchSerialDetail == undefined || batchSerialDetail == "[]" || batchSerialDetail.length<=0){  //for checking batch and serial detail entered or not
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                    return;
                }
            }
        }
        var selectProdValid = this.selectProduct.getForm().isValid();
        var buildFormValid = this.BuildForm.getForm().isValid();
        if(!selectProdValid || !buildFormValid){
            WtfComMsgBox(2,2);
        }else{
            var rec =obj.ProductAssemblyGrid.gridStore.data.items;
            for(var cnt=0;cnt<rec.length;cnt++){
                prodOnHandQuantity = rec[cnt].data.onhand;
                prodQuantityNeeded=rec[cnt].data.quantity;
                productname=rec[cnt].data.productname;
                var producttype = rec[cnt].data.producttype;
                if(prodOnHandQuantity<0 && producttype!="4efb0286-5627-102d-8de6-001cc0794cfa" && !this.isUnbuildAssembly){    //Skip this check for 'Service' Type of Product. ERP-30031
		//ERP-39359 - Do not check quantity on hand while doing Unbuild assembly. We suppose that, On dissemble of Assembly Product, we are releasing BOM quantity (Not consuming BOM Quantity)
                    onhandquantityflag=true;
                    negstockflag=true;
                    break;
                }else{
//                    if(newval>1){
//                        prodQuantityNeeded = newval*prodQuantityNeeded;
//                    } 
                    newOnHandQuantity=prodOnHandQuantity-prodQuantityNeeded;
                    if(newOnHandQuantity<0 && producttype!="4efb0286-5627-102d-8de6-001cc0794cfa" && !this.isUnbuildAssembly){   //Skip this check for 'Service' Type of Product. ERP-30031
		 //ERP-39359 - Do not check quantity on hand while doing Unbuild assembly. We suppose that, On dissemble of Assembly Product, we are releasing BOM quantity (Not consuming BOM Quantity)
                        negstockflag=true; 
                        break;
                    }
                }
            } 
            if(negstockflag && Wtf.account.companyAccountPref.negativestock!=0){
                if(onhandquantityflag){
                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.nee.75")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+productname+' is '+prodOnHandQuantity+'. So you cannot proceed.'], 2);
                        this.BuildQuantity.setValue(oldval);
                        return;
                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.nee.75")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+productname+' '+WtfGlobal.getLocaleText("acc.field.is")+' '+prodOnHandQuantity+'.'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed") , function(btn){
                            if(btn=="yes"){
                                this.ConfirmQaBuildProduct();
                                return;
                            }else{
                                this.BuildQuantity.setValue(oldval);                               
                                return;
                            }
                        },this);
                    } 
                }else{
                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.nee.75")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+productname+' is '+prodOnHandQuantity+'. So you cannot proceed?'], 2);
                        this.BuildQuantity.setValue(oldval);
                        return;

                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.nee.75")+' '+WtfGlobal.getLocaleText("acc.nee.54")+' '+productname+' '+WtfGlobal.getLocaleText("acc.field.is")+' '+prodOnHandQuantity+'.'+ WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed") , function(btn){
                            if(btn=="yes"){
                                this.ConfirmQaBuildProduct();
                                return;
                            }else{
                                this.BuildQuantity.setValue(oldval);                                    
                                return;
                            }
                        },this); 
                    }
                }
            }else{
                this.ConfirmQaBuildProduct();
            }
        }
    },

 ConfirmQaBuildProduct: function () {
        this.isQAinspection = false;
        if (this.productType === Wtf.producttype.customerAssembly &&  Wtf.account.companyAccountPref.BuildAssemblyApprovalFlow  && !(this.isUnbuildAssembly) ) {
            /*
             * When Qc aprroval flow is activated for Delivery order at that time control comes in this block and sent "isQAinspection" as true to java side.
             */
            Wtf.Msg.show({
                title: WtfGlobal.getLocaleText("acc.common.savdat"),
                closable: false,
                msg: WtfGlobal.getLocaleText("acc.deliveryorder.qainspection.returnmsg"),
                buttons: Wtf.Msg.YESNO,
                scope: this,
                fn: function (btn) {
                    if (btn == "no") {
                        this.isQAinspection = false;
                    } else if (btn == "yes") {
                        this.isQAinspection = true;
                    } else {
                        this.enableSaveButtons();
                        return;
                    }
                   this.buildAssemblyProduct();
                },
                animEl: 'elId',
                icon: Wtf.MessageBox.QUESTION
            });
        }else{
            this.buildAssemblyProduct();
        }
    },
    
    buildAssemblyProduct : function(){
        var warn = WtfGlobal.getLocaleText("acc.build.10");  //"Do you want to save the product(s) assembly?";
        if(this.BuildQuantity.getValue() > this.ProductAssemblyGrid.maxbuilds){
            warn = this.BuildQuantity.getValue()+" "+ (this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.unbuild.quantity") : WtfGlobal.getLocaleText("acc.build.2"));
        //                warn = WtfGlobal.getLocaleText("acc.build.1")+" "+this.BuildQuantity.getValue()+" "+WtfGlobal.getLocaleText("acc.build.2");
        }
//        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),warn,function(btn){
//                if(btn!="yes") {return;}
            WtfComMsgBox(27,4,true);
            var rec=this.BuildForm.getForm().getValues();
            rec.mode=27;
            rec.newbomcode = this.newBomCode!=undefined ? this.newBomCode.getValue() : "";
            rec.newbomname = this.newBomName!=undefined ? this.newBomName.getValue() : "";
            rec.product=this.AssemblyProducts.getValue();
            /*
             * to save Jobworkorderid in product buid
             */
            rec.jobworkorderid=this.jobWorkOrderRecCombo.getValue();
            rec.isForCustomerAssembly=this.isForCustomerAssembly;
            rec.bomdetailid=this.bomCombo.getValue();
            rec.assembly=this.ProductAssemblyGrid.getAssemblyJson();
            rec.applydate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.createdon=WtfGlobal.convertToGenericDate(new Date().clearTime(true));
            rec.ProductSerialJson=this.ProductSerialJson.filter(Boolean).toString();
            rec.description=this.desc.getValue();
            if(this.isUnbuildAssembly){
                rec.isUnbuildAssembly=this.isUnbuildAssembly; 
                rec.assmbledProdQty=(this.BuildQuantity.getValue()!="" && this.BuildQuantity.getValue()!=undefined) ? this.BuildQuantity.getValue() : 1; 
            }
            rec.isBuild = this.isUnbuildAssembly ? false : true;    //To maintain build & unbuild assembly records
            rec.refno=this.RefNo.getValue();
            rec.isEdit=this.isEdit;
            //for delete request
            if(this.isEdit){
             rec.productids=this.prodbuildid;
             rec.assmbledProdQty=this.record.data.quantity;
            }
            
            rec.isQAinspection=this.isQAinspection;
            Wtf.Ajax.requestEx({
                //                    url: Wtf.req.account+'CompanyManager.jsp',
//                url: "ACCProduct/buildProductAssembly.do",
                url: "ACCProductCMN/buildProductAssembly.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse
            );
//        },this);
       // this.savebtton.disable();
    },
    
    genSuccessResponse:function(response){
        if (response.success) {
            WtfComMsgBox([(this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.productList.unBuildAssembly") : WtfGlobal.getLocaleText("acc.productList.buildAssembly")),response.msg],response.success*2+1);
            this.savebtton.disable();
            this.disableComponents();            
        } else {
            WtfComMsgBox([(this.isUnbuildAssembly ? WtfGlobal.getLocaleText("acc.productList.unBuildAssembly") : WtfGlobal.getLocaleText("acc.productList.buildAssembly")),response.msg], 2);
            this.savebtton.enable();
        }
        if (response.id !== "") {
            this.singleRowPrint.enable();
            this.exportRecord["billid"] = response.id;//ERM-26
            if (this.singleRowPrint) {
                this.singleRowPrint.exportRecord = this.exportRecord;
            }
        }
        Wtf.dirtyStore.product = true;
        Wtf.totalQtyAddedInBuild=0;
        if(!(Wtf.account.companyAccountPref.productOptimizedFlag==Wtf.Products_on_type_ahead)){
            Wtf.productStore.load();
            Wtf.productStoreSales.reload();
        }
       Wtf.getCmp('buildreportgridid').store.reload();
        this.fireEvent('closed',this);
    },
    
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.isQAinspection=false;
//        this.fireEvent('closed',this);
    },
    
    disableComponents:function(response){
        if(this.sequenceFormatCombobox) this.sequenceFormatCombobox.disable();
        if(this.RefNo) this.RefNo.disable();
        if(this.costToBuild) this.costToBuild.disable();
        if(this.BuildQuantity) this.BuildQuantity.disable();
        if(this.memo) this.memo.disable();
        if(this.OnHandQuantity) this.OnHandQuantity.disable();
        if(this.assemblyProductCost) this.assemblyProductCost.disable();
        if(this.desc) this.desc.disable();
        if(this.AssemblyProducts) this.AssemblyProducts.disable();
        if(this.billDate) this.billDate.disable();
        if(this.bomCombo) this.bomCombo.disable();
        if(this.ProductAssemblyGrid) this.ProductAssemblyGrid.disableEdit = true;
    },
    checkForDeactivatedProducts:function(){
        var invalidProducts='';
        for(var x=0;x<this.ProductAssemblyGrid.gridStore.getCount();x++){
            var rec = this.ProductAssemblyGrid.gridStore.getAt(x);
            if(rec.data.hasAccess == false){
                invalidProducts += rec.data.productname+', ';
            }
        }
        if(invalidProducts !=''){
            invalidProducts = invalidProducts.substring(0,invalidProducts.length-2);
        }
        return invalidProducts;
    }
});
