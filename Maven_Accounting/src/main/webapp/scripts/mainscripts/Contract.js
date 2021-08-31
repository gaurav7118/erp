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
Wtf.account.NewContract = function(config){
    this.SOIndex = -1;
    this.isCustomer=true;
    this.isNormalContract=config.isNormalContract?config.isNormalContract:false;// contract which is not a lease contract
    this.moduleid=config.moduleid;
    this.externalcurrencyrate=0;
    this.oldval="";this.val="";this.pronamearr=[];
    this.isEdit=(config.isEdit)?config.isEdit:false;
    this.record=config.record;
    this.dataLoaded=false;
    this.contractID="";
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.serviceRecCount=0;
    this.serviceOrgArr=[];
    this.deletedArr=[];
    this.isTermValue=false;
    this.custUPermType=Wtf.UPerm.customer;
    this.custPermType=Wtf.Perm.customer;
    /*
     * Assign the value to the optimized flag as per System preferences.
     */
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
    Wtf.apply(this,config);
    Wtf.account.NewContract.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.NewContract, Wtf.Panel, {
     loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record.data;
            this.contractForm.getForm().loadRecord(this.record);
                        
           this.contractID=data.cid;
           /*
            * In Edit and view case set value to the customer combo-No need to load store 
            */
           var temp_rec = WtfGlobal.searchRecord(this.customerAccStore, data.accid, this.custmerCmb.valueField);
           if(temp_rec){
             this.custmerCmb.setValue(data.accid);
           }else{
             this.custmerCmb.setValForRemoteStore(data.accid, data.accname, data.hasAccess);
           }
           this.SOStore.on("load", function(){
               if(this.custmerCmb.getValue() == data.accid) {
                   var record = new Wtf.data.Record({
                   billid: data.salesorderid,
                   billno: data.salesorder
                });
                this.SOStore.insert(0, record);
                this.fromSO.setValue(data.salesorderid);
                
                if(data.salesorderid && data.salesorderid != ""){
                    this.productGrid.isLinkedFromTransaction = true;
                }
                }
           }, this);
           this.SOStore.load({params:{currencyfilterfortrans:parseInt(data.currencyid)}});
           this.productStore.load({params:{bills: data.salesorderid}});
           
           this.leaseStatus.setValue(data.leasestatus);
           this.Currency.store.on('load',function(){
               this.Currency.setValue(data.currencyid);
           },this);
           this.serviceRecCount=this.agreedservices.getValue();
           if(this.agreedservices.getValue()>0){
                this.serviceStore.removeAll();
              this.serviceStore.load({
                        params:{
                             contractid:data.cid
                        }
                    });
           }
           this.serviceStore.on('load',function(store,record){
               for(var k=0;k<record.length;k++){
                   this.serviceOrgArr.push(record[k].data.id);
               }
           },this);
           
//           if(this.isNormalContract){
               
               this.productGrid.getStore().on('load',function(store, recArr){
                    var amount=0;
                    for(var i=0;i<this.productGrid.getStore().getCount();i++){
                        if(this.productGrid.getStore().getAt(i).data.amount && this.productGrid.getStore().getAt(i).data.amount != "")
                            amount+=Number(this.productGrid.getStore().getAt(i).data.amount);   //ERP-29307
                        }
                    this.leaseAmount.setValue(amount);
                    if(data.salesorderid ==="") {
                        this.productGrid.addBlank(store);
                    }
                },this);
               
                if (this.productOptimizedFlag != undefined && this.productOptimizedFlag == Wtf.Show_all_Products) {
                    this.productGrid.productComboStore.on("load",function(){
                        this.loadDetailsGrid();
                    },this);
                           
                    this.productGrid.productComboStore.load();
                
                } else{
                    this.loadDetailsGrid();
                }
//           }
           
           
          this.deletedArr=this.serviceOrgArr;
            Wtf.Ajax.requestEx({
            url:"ACCContract/getContractFiles.do",
                params:{
                    contractid:data.cid
                }
            }, this,this.onSuccessResponse); 
            this.Memo.setValue(data.memo);
            this.dataLoaded=true;
            var fieldArr = this.SOStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id)) {
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(this.record.json[fieldN.name + "_Value"]);
                    }
             }
        if(this.productGrid) {                       // SET FOR CONTARCT GRID AS PARM TO GET INDIVIDUAL PRODUCT PRICE FOR CUSTOMER 
            this.productGrid.affecteduser = data.accid;
            this.productGrid.forCurrency =data.currencyid;
            this.productGrid.billDate=data.startdate;
        }
        }
    },
    
    loadDetailsGrid:function(){
        if(this.isEdit){
            this.loadEditableGrid();
        }
    },
    
    loadEditableGrid:function(){
        this.subGridStoreUrl = "ACCContract/getContractOrderRows.do";
        if(this.productGrid) {                       // SET FOR CONTARCT GRID AS PARM TO GET INDIVIDUAL PRODUCT PRICE FOR CUSTOMER 
            this.productGrid.billDate=this.fromDate.getValue();
         }
//            this.billid=this.record.data.billid;
            this.productGrid.getStore().proxy.conn.url = this.subGridStoreUrl;
            
            this.productGrid.getStore().load({params:{contractid:this.record.data.cid}});
            
    },
    onSuccessResponse:function(response, request){
        
         this.attachedFilesStr=""
                var fileStore=response.data;
               for(var i=0;i<response.data.length;i++){
                   var rec=fileStore[i];
                   if(i==0){
                        this.attachedFilesStr=rec.id;
                   }else{
                       this.attachedFilesStr+=","+rec.id;
                   }
               }
                
    },
    initComponent: function(config){
        Wtf.account.NewContract.superclass.initComponent.call(this,config);
        this.createStore();
        this.createColumnModel();
        this.createFields();
        this.createForm();
        
        this.saveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
        hidden:this.isViewTemplate,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.save),
        handler: function(){
            this.saveOnlyFlag = 1;
            this.save();
        }
        });
        
        this.attachFilesBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.Lease.addAttach"),
        scope:this,
        handler:this.showContractAttachWindow,
        iconCls :getButtonIconCls(Wtf.etype.save)
        });
        
        this.maintenanceScheduleBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.maintenance.schedule"),//Maintenance Schedule
        tooltip: WtfGlobal.getLocaleText("acc.maintenance.scheduleTT"),
        hidden:false,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.add),
        handler: this.maintenanceScheduleButtonHandler 
        });
        
        this.btnArr=[];
        this.btnArr.push(this.saveBttn); 
        if(!this.isEdit){
            this.savencreateBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            scope: this,
            handler: function(){
                this.saveOnlyFlag = 2;
                this.save();
            },
            iconCls: 'pwnd save'
        });
            this.btnArr.push(this.savencreateBttn);  
        }
        this.btnArr.push(this.attachFilesBttn);  
        this.btnArr.push(this.maintenanceScheduleBttn);  
        
        
        
        
        this.newPanel=new Wtf.Panel({
        autoScroll:true,
        bodyStyle:' background: none repeat scroll 0 0 #DFE8F6;',
        region : 'center',
        items:[this.contractForm,this.productGrid] ,
        bbar:this.btnArr/*[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.save),
            id: "save" + config.heplmodeid + this.id,
            handler: function(){
                //this.mailFlag = true;
                //                    if(this.isTemplate){
                //                        if(this.moduleTemplateName.getValue() == ''){
                //                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.field.PleaseEnterTemplateNameFirst")], 1);
                //                            return;
                //                        }
                //                        this.saveTemplate();
                //                    }else{
                this.save();
            //  }
            }
                
        },{
            text:WtfGlobal.getLocaleText("acc.Lease.addAttach"),
            scope:this,
            //                hidden:true,
            handler:this.showContractAttachWindow,
            iconCls :getButtonIconCls(Wtf.etype.save)
        },{
            text: WtfGlobal.getLocaleText("acc.maintenance.schedule"),//Maintenance Schedule
            tooltip: WtfGlobal.getLocaleText("acc.maintenance.scheduleTT"),
            hidden:false,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler: this.maintenanceScheduleButtonHandler 
        }]*/
    });
         this.newPanel.on("resize",function(){
                this.newPanel.doLayout();
        },this);
        
        var ajxUrl = "CommonFunctions/getInvoiceCreationJson.do";
        var params={
            transactiondate:WtfGlobal.convertToGenericDate(new Date()),
            loadtaxstore:true,
            moduleid :this.moduleid,
//            loadpricestore: false,//!(this.isCustBill||this.isExpenseInv),
            loadcurrencystore:true,
            loadtermstore:true
//            loadInventory:this.isCustomer
        }
        Wtf.Ajax.requestEx({url:ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
        
       this.add(this.newPanel);
       this.hideFormFields();
    //        WtfComMsgBox(29,4,true);
     },
    
    successCallback:function(){
        if(this.isEdit)
            this.loadRecord();
    },
    
    failureCallback:function(){
    
    },
    
    hideFormFields: function() {
        if (this.isNormalContract) {
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.salesContract);
        } else {
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.leaseContract);
        }
    },
 hideTransactionFormFields: function(array) {
       if (array) {
           for (var i = 0; i < array.length; i++) {
               var fieldArray = array[i];
               if (Wtf.getCmp(fieldArray.fieldId + this.id)) {
                   if(Wtf.getCmp("termvalue"+this.id)){
                       if(fieldArray.fieldId == "leaseterm"){
                           Wtf.getCmp("termvalue"+this.id).hidden = fieldArray.isHidden;
                       }
                   }
                   if (fieldArray.fieldId == "ShowOnlyOneTime" && ((this.isEdit != undefined ? this.isEdit : false) || (this.copyInv != undefined ? this.copyInv : false))) {
                       continue;
                   }
                   /*
                    *ERP-37436 
                    *In the configue of field the  hiedeLabel flag is true
                    *but in this function flag is set to false.
                    **/
                   if(!fieldArray.fieldId=="termvalue"){
                        Wtf.getCmp(fieldArray.fieldId + this.id).hideLabel = fieldArray.isHidden;
                    }
                   Wtf.getCmp(fieldArray.fieldId + this.id).hidden = fieldArray.isHidden;
                   if (fieldArray.isReadOnly) {
                       Wtf.getCmp(fieldArray.fieldId + this.id).disabled = fieldArray.isReadOnly;
                   }
                   if (fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel != undefined) {
                       Wtf.getCmp(fieldArray.fieldId + this.id).allowBlank = !fieldArray.isUserManadatoryField;
                       var fieldLabel = "";
                       if (fieldArray.fieldLabelText != "" && fieldArray.fieldLabelText != null && fieldArray.fieldLabelText != undefined) {
                           fieldLabel = fieldArray.fieldLabelText + " *";
                       } else {
                           fieldLabel = (Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel) + " *";
                       }
                       Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel = fieldLabel;
                   } else {
                       if (fieldArray.fieldLabelText != null && fieldArray.fieldLabelText != undefined && fieldArray.fieldLabelText != "") {
                           if (fieldArray.isManadatoryField && fieldArray.isFormField)
                               Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel = fieldArray.fieldLabelText + "*";
                           else
                               Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel = fieldArray.fieldLabelText;
                       }
                   }
               }
           }
       }
   },   
    
    createStore:function(){
        
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'hasAccess'},
           {name: 'applydate', type:'date'}

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTax/getTax.do"
//            baseParams:{
//                mode:33,
//                moduleid :this.moduleid
//            }
        });
//        if(this.readOnly)
            this.taxStore.load();

        this.transTax= new Wtf.form.ExtFnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            displayDescrption:'taxdescription',
            selectOnFocus:true,
            typeAhead: true,
            mode: 'remote',
            minChars:0,
            extraFields: [],
            isTax: true,
            listeners: {
                'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            }
        });
        
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        
        this.personRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }, {
            name: 'acccode'
        },{
            name: 'taxId'
        },{
            name: 'crmAccountId'
        }
        ,{
            name: 'hasAccess'
        }
        ]);
    
        this.customerAccStore = new Wtf.data.Store({
            url: "ACCCustomer/getCustomersForCombo.do",
            baseParams: {
                mode: 2,
                group: 10,
                deleted: false,
                nondeleted: true,
                common: '1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad: false
            }, this.personRec)
        });
        
        if (!this.custVenOptimizedFlag) {
            this.customerAccStore.load();
            this.customerAccStore.on('load', this.onCustomerLoad, this);
        }
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },
        {
            name: 'oldflag'
        }
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:"autocontract",
                isEdit:this.isEdit
            }
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
                this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
          this.currencyStore.load();
         this.currencyStoreCMB = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStoreCMB.load();
         
           
        this.SORec = Wtf.data.Record.create ([
        {
            name:'billid'
        },

        {
            name:'journalentryid'
        },

        {
            name:'entryno'
        },

        {
            name:'billto'
        },

        {
            name:'discount'
        },

        {
            name:'shipto'
        },

        {
            name:'mode'
        },

        {
            name:'billno'
        },

        {
            name:'date', 
            type:'date'
        },

        {
            name:'duedate', 
            type:'date'
        },

        {
            name:'shipdate', 
            type:'date'
        },

        {
            name:'personname'
        },

        {
            name:'creditoraccount'
        },

        {
            name:'personid'
        },

        {
            name:'shipping'
        },

        {
            name:'othercharges'
        },

        {
            name:'taxid'
        },

        {
            name:'discounttotal'
        },

        {
            name:'discountispertotal',
            type:'boolean'
        },

        {
            name:'currencyid'
        },

        {
            name:'amount'
        },
        {
            name:'amountinbase'
        },

        {
            name:'amountdue'
        },

        {
            name:'costcenterid'
        },

        {
            name:'costcenterName'
        },

        {
            name:'memo'
        },

        {
            name:'shipvia'
        },

        {
            name:'fob'
        },

        {
            name:'includeprotax',
            type:'boolean'
        },

        {
            name:'salesPerson'
        },

        {
            name:'agent'
        }
        ]);
        this.SOStoreUrl ="ACCSalesOrderCMN/getSalesOrders.do";
            
    
        this.SOStore = new Wtf.data.Store({
            url:this.SOStoreUrl,
            //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                mode:42,
                orderForContract:true,
                isLeaseFixedAsset:!this.isNormalContract,
                requestModuleid:this.moduleid,
                linkflag:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SORec)
        });
        this.SOStore.on('load',function(s,o){
            if(this.isEdit)
                this.loadRecord();
        },this); 
        var DimensionCustomFielsArray = GlobalDimensionCustomFieldModel[this.moduleid];
        if( DimensionCustomFielsArray){
           for(var cnt = 0;cnt < DimensionCustomFielsArray.length;cnt++){
               if(DimensionCustomFielsArray[cnt] != undefined){
                    var fieldname = DimensionCustomFielsArray[cnt].fieldname;
                    var newField = new Wtf.data.ExtField({
                        name:fieldname.replace(".",""),
        //                   sortDir:'ASC',
                        type:DimensionCustomFielsArray[cnt].fieldtype == 3 ?  'date' : (DimensionCustomFielsArray[cnt].fieldtype == 2?'float':'auto'),
                        dateFormat:DimensionCustomFielsArray[cnt].fieldtype == 3 ? 'time' : undefined
                    });
                    this.SOStore.fields.items.push(newField);
                    this.SOStore.fields.map[fieldname]=newField;
                    this.SOStore.fields.keys.push(fieldname);
               }
               
           }
           this.SOStore.reader = new Wtf.data.KwlJsonReader(this.SOStore.reader.meta, this.SOStore.fields.items);
       }
        this.leasetermStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Day'],['2','Week'],['3','Month'],['4','Year']]
        });
        
        this.leaseStatusRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },
        {
            name: 'name'
        }]
        );

    this.leaseStatusStore = new Wtf.data.SimpleStore({
        fields:[{
            name:'id'
        },{
            name:'name'
        }],
        data:[['1','Active'],['2','Terminate'],['3','Expire'],['4','Renew']]
    });
        
//        this.leaseStatusStore.load(); 
        
         this.serviceRec =new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'servicedate',
            type:'date'
        }
        ]);
        this.serviceStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.serviceRec),
            url: 'ACCContract/getAggredServicesRows.do'
        });
            this.serviceStore.load();
            
            
             this.storeRec = Wtf.data.Record.create([
            {name:'rowid',defValue:null},
            {name:'productname',mapping:(this.isViewCNDN)?'productdetail':null},
            {name:'billid'},    
            {name:'billno'},
            {name:'Cust_billno'},
            {name:'productid'},
            {name:'pid'},
            {name:'desc'},
            {name:'quantity',defValue:1.00},
            {name:'baseuomquantity',defValue:1.00},
            {name:'uomname'},
            {name:'uomid'},
            {name:'baseuomrate',defValue:1.00},
            {name:'copyquantity',mapping:'quantity'},
            {name:'rate',defValue:0},
            {name:'unitPricePerInvoice',defValue:0},
            {name:'rateinbase'},
            {name:'partamount',defValue:0},
            {name:'discamount'},
            {name:'discount'},
            {name:'discountispercent',defValue:1},
            {name:'prdiscount',defValue:0},
            {name:'invstore'},
            {name:'invlocation'},
            {name:'prtaxid'},
            {name:'prtaxname'},
            {name:'prtaxpercent',defValue:0},
            {name:'taxamount',defValue:0},
            {name:'amount',defValue:0},
            {name:'amountwithtax',defValue:0},
            {name:'amountwithouttax',defValue:0},// used this field for Invoice Terms - rate*qty-discount
            {name:'taxpercent'},
            {name:'remark'},
            {name:'transectionno'},
            {name:'remquantity'},
            {name:'remainingquantity'},
            {name:'oldcurrencyrate',defValue:1},
            {name: 'currencysymbol',defValue:this.symbol},
            {name: 'currencyrate',defValue:1},
            {name: 'externalcurrencyrate'},
            {name:'orignalamount'},
            {name:'typeid',defValue:0},
            {name:'isNewRecord',defValue:'1'},
            {name:'producttype'},
            {name:'permit'},
            {name:'linkto'},
            {name:'linkid'},
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'changedQuantity'},
            {name:'approvedcost'},
            {name:'approverremark'},
            {name:'customfield'},
            {name:'gridRemark'},
            {name:'productcustomfield'},
            {name:'accountId'},
            {name:'salesAccountId'},
            {name:'discountAccountId'},
            {name:'rowTaxAmount'},
            {name:'type'},                        
            {name:'shelfLocation'},
            {name:'productcustomfield'},
            {name:'supplierpartnumber'},
            {name:'copybaseuomrate',mapping:'baseuomrate'}  //for handling inventory updation 
        ]);
        var url="ACCSalesOrderCMN/getSalesOrderRows.do";
        this.productStore = new Wtf.data.Store({
            url:url,
            pruneModifiedRecords:true,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
        if(this.fromSO!=undefined && this.fromSO.getValue()!=undefined && this.fromSO.getValue()!="") {   //ERP-29307 : Do not load ProductStore if SO is not linked in Sales Contract
           this.productStore.on("load",function(store,rec,Obj){
            var numberOfPeriods = this.numberOfPeriods.getValue();
            var amount=0;
            for(var i=0;i<this.productStore.getCount();i++){
               amount+=Number(this.productStore.getAt(i).data.amount);
               if(numberOfPeriods>0){
                   var rate = this.productStore.getAt(i).data.rate;
                   var unitPricePerInvoice = rate/numberOfPeriods;
                   this.productStore.getAt(i).set('unitPricePerInvoice', unitPricePerInvoice);
               }
            }
            this.leaseAmount.setValue(amount);
        },this);
      }
    },
    
    createColumnModel:function(){
        this.rowDiscountTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :[[1,'Percentage'],[0,'Flat']]
        });
        this.rowDiscountTypeCmb = new Wtf.form.ComboBox({
            store: this.rowDiscountTypeStore,
            name:'typeid',
            displayField:'name',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=(this.isNote)?new Wtf.grid.CheckboxSelectionModel():new Wtf.grid.RowNumberer();
        var columnArr =[];
       
            columnArr.push(this.rowno);
        columnArr.push({
            dataIndex:'rowid',
            hidelabel:true,
            hidden:true
        },{
            dataIndex:'billid',
            hidelabel:true,
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridProduct"),//"Product",
            width:200,
            dataIndex:'productname'
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridDescription"),//"Description",
             dataIndex:"desc",
             hidden:this.isNote,
             width:250,
             renderer:function(val){
                 var regex = /(<([^>]+)>)/ig;
//                val = val.replace(/(<([^>]+)>)/ig,"");
               // if(val.length<50)
                    return "<div   wtf:qtip=\"<div style='word-wrap: break-word;'>"+val+"</div>\" wtf:qtitle="+WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc")+">"+val+"</div>";      
//                else
//                    return val.substring(0,50)+" ...";   
            }
         });
         
         columnArr.push({
             header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
             dataIndex:"quantity",
             align:'right',
             width:100,
             renderer:this.quantityRenderer
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
            width:100,
            hidden:true,
            dataIndex:'uomname'
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPrice"),// "Unit Price",
             dataIndex: "rate",
             align:'right',
             width:150,
             renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridUnitPricePerInvoice"),// "Unit Price Per Invoice",
             dataIndex: "unitPricePerInvoice",
             align:'right',
             width:170,
             renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:200,
            dataIndex:'discountispercent',
             renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:this.rowDiscountTypeCmb   //(this.isNote||this.readOnly)?"":
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount",
             dataIndex:"prdiscount",
             align:'right',
             renderer:function(v,m,rec){
                 if(rec.data.discountispercent) {
                     v= v + "%";
                 }else {
                     var symbol = WtfGlobal.getCurrencySymbol();
                     if(rec.data['currencysymbol']!=undefined && rec.data['currencysymbol']!=""){
                         symbol = rec.data['currencysymbol'];
                     }
                     
                     v= WtfGlobal.conventInDecimal(v,symbol)
                 }
                 return'<div class="currency">'+v+'</div>';
             }
         },{
             header: WtfGlobal.getLocaleText("acc.invoice.proTax"),//"Product Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             fixed:true,
             width:150,
             hidden:false,
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:""
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
              fixed:true,
             //align:'right',
             width:150,
             editor:this.transTaxAmount,
             hidden:false,
             renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
        },{
             header:this.isRequisition ? (this.editTransaction ? WtfGlobal.getLocaleText("acc.field.BudgetedCost") : WtfGlobal.getLocaleText("acc.field.EstimatedCost") ): this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),//"Current Amount ":"Amount",
             dataIndex:"amount",
             hidden:this.readOnly || this.isRFQ,
             align:'right',
             width:200,
             renderer:this.calAmountWithoutExchangeRate.createDelegate(this)
        });
      
        this.columnmodel=new Wtf.grid.ColumnModel(columnArr);                
    },
    showPONumbersGrid: function (url) {
        this.PONumberSelectionWin = new Wtf.account.PONumberSelectionWindow({
            renderTo: document.body,
            height: 500,
            id: this.id + 'PONumbersSelectionWindowDO',
            width: 600,
            title: 'Document Selection Window',
            layout: 'fit',
            modal: true,
            resizable: false,
            url: url,
            moduleid: this.moduleid,
            invoice: this,
            storeBaseParams: this.SOStore.baseParams,
            storeParams: this.SOStore.lastOptions.params,
            PORec: this.SORec,
            singleSelect: true
        });
        this.PONumberSelectionWin.show();
    },
    createFields:function(){
      
        this.Pname=new Wtf.form.ExtendedTextField({
            fieldLabel: (this.isFixedAsset)?'Asset Group Name*':WtfGlobal.getLocaleText("acc.product.productName"),//'Product Name*',
            name: 'productname',
            disabled:(this.isEdit && !this.isClone)?true:false,
            allowBlank:false,
            anchor:'75%',
            //            regex:/^[\w\s\'\"\.\-]+$/,
            regex:/^[^\"\%\\]+$/,
            invalidText : 'This field should not be blank or should not contain %, ", \\ characters.',
            maxLength:50
        });
        
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
            //        labelSeparator:'',
            //        labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>",
            valueField:'id',
            displayField:'value',
            store:this.sequenceFormatStore,
            disabled:(this.isEdit && !this.isClone?true:false),  
            anchor:'75%',
            typeAhead: true,
            forceSelection: true,
            name:'sequenceformat',
            hiddenName:'sequenceformat',
            id:'sequenceformat' + this.id,
            allowBlank: false,
            listeners:{
                'select':{
                    fn:this.getNextSequenceNumber,
                    scope:this
                }
            }
            
        });
        
        this.PID=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.Lease.ContractID")+"*",//'Contract ID*',
            disabled:(this.isEdit && !this.isClone)?true:false,
            name: 'contractid',
            id:'contractid' + this.id,
            anchor:'75%',
            allowBlank:false,
            maxLength:50
        });       
         this.fromSO = new Wtf.form.ExtFnComboBox({
            fieldLabel: (this.isNormalContract)?WtfGlobal.getLocaleText("acc.accPref.autoSO"):(WtfGlobal.getLocaleText("acc.lease.order")+"*"),//Sales Order'Lease Order*',
            hiddenName:'salesorder',
            name:'salesorder',
            id:'salesorder' + this.id,
            store:this.SOStore,
            valueField:'billid',
            displayField:'billno',
            extraFields:[],
            listWidth:190,
            extraComparisionField:'billno',// type ahead search on billno as well.
            allowBlank: this.isNormalContract,
            hirarchical: true,
            emptyText: (this.isNormalContract)?WtfGlobal.getLocaleText("acc.contract.salesorder"):WtfGlobal.getLocaleText("acc.contract.leaseorder"),//Select a Sales Order ...
            mode: 'local',
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            anchor:'75%',
            triggerAction: 'all',
            scope: this,
            width:100
        });
        if (Wtf.account.companyAccountPref.enableLinkToSelWin && (this.moduleid == Wtf.Acc_Lease_Contract)) {
            this.SOStore.on('load', function () {
                addMoreOptions(this.fromSO, this.SORec)
            }, this);
            this.SOStore.on('datachanged', function () {
                addMoreOptions(this.fromSO, this.SORec)
            }, this);
        }
      
        this.fromSO.on('select',function(obj,rec,num){
            var billid = this.fromSO.getValue();
            this.SOIndex = this.fromSO.selectedIndex;
            if (billid.indexOf("-1") != -1) {
                var url ="ACCSalesOrderCMN/getSalesOrders.do";
                this.fromSO.reset();
                this.fromSO.collapse();
                this.fromSO.clearValue();
                this.showPONumbersGrid(url);
                return;
            }
            var record = this.SOStore.getAt(this.SOStore.find('billid', billid));
            rec = record;
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getFieldParams.do",
                params: {
                    moduleid: this.isNormalContract?Wtf.Acc_Sales_Order_ModuleId:Wtf.Acc_Lease_Order,
                    jeId: rec.data.billid,
                    customcolumn:0,
                    isActivated:1
                }
            }, this,function(responseObj){
                var billid = this.fromSO.getValue();
                var record = this.SOStore.getAt(this.SOStore.find('billid', billid));
                this.tagsFieldset.resetCustomComponents();
                var customFieldArray = this.tagsFieldset.customFieldArray;  //Set Custom Fields
                for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                    var fieldId = customFieldArray[itemcnt].id;
                    for (var itemcnt1 = 0; itemcnt1 < responseObj.data.length; itemcnt1++) {
                        if (Wtf.getCmp(fieldId) != undefined && Wtf.getCmp(fieldId).getXType()!='fieldset' && Wtf.getCmp(fieldId).name == responseObj.data[itemcnt1].fieldname) {
                            if (responseObj.data[itemcnt1].fieldtype == '4' || responseObj.data[itemcnt1].fieldtype == '7' || responseObj.data[itemcnt1].fieldtype == '12') {
                                var ComboValue = responseObj.data[itemcnt1].fieldName;
                                if(ComboValue) {
                                    var ComboValueArrya = ComboValue.split(',');
                                    var ComboValueID = "";
                                    for (var i = 0; i < ComboValueArrya.length; i++) {
                                        var temp_rec = WtfGlobal.searchRecord(Wtf.getCmp(fieldId).store, ComboValueArrya[i], "name");
                                        ComboValueID += temp_rec.data.id + ",";
                                    }
                                    if (ComboValueID.length > 1) {
                                        ComboValueID = ComboValueID.substring(0, ComboValueID.length - 1);
                                    }
                                    Wtf.getCmp(fieldId).setValue(ComboValueID);
                                }
                                break; //unnecessarily looping througth for loop even if value set for custom field
                            } else if(responseObj.data[itemcnt1].fieldtype == '3' && responseObj.data[itemcnt1].fieldData != undefined){
                                var date = new Date(responseObj.data[itemcnt1].fieldData *1);
                                Wtf.getCmp(fieldId).setValue(date);
                                break; //unnecessarily looping througth for loop even if value set for custom field
                            } else{
                                Wtf.getCmp(fieldId).setValue(responseObj.data[itemcnt1].fieldData);
                                break; //unnecessarily looping througth for loop even if value set for custom field
                            }
                                    
                        } else if (Wtf.getCmp(fieldId) != undefined && Wtf.getCmp(fieldId).getXType() == 'fieldset' && responseObj.data[itemcnt1].fieldName != "" && responseObj.data[itemcnt1].fieldName != undefined) {
                            if (responseObj.data[itemcnt1].fieldtype == '12') {    //check list   
                                var ComboValue = responseObj.data[itemcnt1].fieldName;
                                if(ComboValue) {    //this check is for if combo value haven't set yet
                                    var ComboValueArrya = ComboValue.split(',');
                                    var ComboValueID = "";
                                    var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;
                                    for (var i = 0; i < ComboValueArrya.length; i++) {
                                        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                            if (checkListCheckBoxesArray[checkitemcnt].name == ComboValueArrya[i])
                                                if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                                    Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                                }
                                        }
                                    }
                                }
                                break; //unnecessarily looping througth for loop even if value set for custom field
                            }        
                        }
                    }
                }    
                var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Set Custom Dimension
                for (var itemcnt2 = 0; itemcnt2 < customDimensionArray.length; itemcnt2++) {
                    var fieldId1 = customDimensionArray[itemcnt2].id;
                    for (var itemcnt3 = 0; itemcnt3 < responseObj.data.length; itemcnt3++) {
                        if (Wtf.getCmp(fieldId1) != undefined && Wtf.getCmp(fieldId1).name == responseObj.data[itemcnt3].fieldname) {
                            if (responseObj.data[itemcnt3].fieldData != "" && responseObj.data[itemcnt3].fieldData != undefined) {      //to set value or none
                                 var ComboValue=record.data[responseObj.data[itemcnt3].fieldname];
                                 if(ComboValue){
                                      Wtf.getCmp(fieldId1).setValue(ComboValue);
                                      var  parent=  Wtf.getCmp(fieldId1).parentid;
                                      var displayValue = record.json[responseObj.data[itemcnt3].fieldname+ "_linkValue"];
                                       if (parent != undefined && displayValue != undefined && parent.length > 0) {
                                        if (displayValue) {
                                            Wtf.getCmp(fieldId1).setValForChildComboStore(ComboValue, displayValue); // create record and set value
                                        }
                                    }
                                    var  childid= Wtf.getCmp(fieldId1).childid;
                                    if (childid.length > 0) {
                                        var childidArray = childid.split(",");
                                        for (var i = 0; i < childidArray.length; i++) {
                                            var currentBaseParams = Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.baseParams;
                                            currentBaseParams.parentid = ComboValue;
                                            Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.baseParams = currentBaseParams;
                                            Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.load();
                                        }
                                    }
                                 }
                                
                            } else {
                                Wtf.getCmp(fieldId1).setValue("1234");
                            }
                            break; //unnecessarily looping througth for loop even if value set for custom dimension
                        }
                    }
                } 
            }, function(response){
                
                });
            
            
            this.productGrid.getStore().on('load',this.productstoreLoadedFn,this);
            
//            if(this.isNormalContract){
                this.productGrid.isLinkedFromTransaction = true;
                this.productGrid.getStore().removeAll();
                
                this.productGrid.getStore().on('load',this.onProductGridStoreLoad,this);
                this.productGrid.getStore().proxy.conn.url = "ACCSalesOrderCMN/getSalesOrderRows.do";
                this.productGrid.getStore().load({
                    params:{
                        bills: this.fromSO.getValue(),
                        isFromContract:true,
                        isForDOGROLinking:true
                    }
                });
//                    
//            }else{
//                this.productStore.load({params:{bills: this.fromSO.getValue(),isForDOGROLinking:true}});
//                    }
            
        },this);
        
          var comboConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.cust")+"*",
            hiddenName: 'customerid',
            id: "customerid" + this.id,
            store: this.customerAccStore,
            valueField: 'accid',
            displayField: 'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            allowBlank: false,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'remote',
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            isCustomer:true,
            anchor:'75%',
            triggerAction: 'all',
            scope: this,
            width:100,
             listeners:{
                'select':{
                    fn:this.updateData,
                    scope:this    
                }
            }
        };
        if (this.custVenOptimizedFlag) {
            comboConfig['ctCls'] = 'optimizedclass';
            comboConfig['hideTrigger'] = true;
        }
        this.custmerCmb = new Wtf.form.ExtFnComboBox(comboConfig);
        
        this.custmerCmb.on('select',function(combo, record, index){
            this.productGrid.getStore().removeAll();
            this.fromSO.clearValue();
            this.fromSO.reset();
            if(this.isNormalContract){
                this.productGrid.addBlankRow();
            }
           this.productGrid.enable();
           if(this.SOIndex >= 0){
                    this.productGrid.isLinkedFromTransaction = true;
                }
                else{
                    this.productGrid.isLinkedFromTransaction = false;
                }
                
        },this);
        if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create)){
            this.custmerCmb.addNewFn=this.addPerson.createDelegate(this,[false,null,"Customerwindow",true],true);
        }
        this.custmerCmb.on('beforeselect', function(combo, record, index) {
            return validateSelection(combo, record, index);
        }, this);
        this.Currency= new Wtf.form.FnComboBox({  
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur")+"*",  //'Currency',
            hiddenName:'currencyid',
            id:"currencyid"+this.id,
            anchor: '75%',
            //width : 240,
            //disabled:true,
            store:this.currencyStoreCMB,
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true,
            listeners:{
                'select':{
                    fn:this.updateFormCurrency,
                    scope:this
                }
            }
        });
        
         this.SOStore.on('beforeload',function(s,o){
                if(!o.params)o.params={};
                var currentBaseParams = this.SOStore.baseParams;
                currentBaseParams.id=this.custmerCmb.getValue();
                if (this.Currency.getValue()) {
                    currentBaseParams.currencyfilterfortrans = this.Currency.getValue();
                }
                this.SOStore.baseParams=currentBaseParams;        
            },this); 
       
         this.Currency.on('change', function(){
            var customer="",currency="";
            if(this.custmerCmb.getValue() != undefined && this.custmerCmb.getValue() != ""){
                customer= this.custmerCmb.getValue();
            }    
            if(this.Currency.getValue() != undefined && this.Currency.getValue() != ""){
                currency= this.Currency.getValue();        
        }                  
        this.custmerCmb.setValue(customer);
        this.Currency.setValue(currency);
        this.SOStore.load();
        this.fromSO.clearValue();
        this.leaseAmount.enable();
        this.leaseAmount.reset();
        this.leaseAmount.disable();
        this.productStore.removeAll();
        if(this.productGrid) {                       // SET FOR CONTARCT GRID AS PARM TO GET INDIVIDUAL PRODUCT PRICE FOR CUSTOMER 
            this.productGrid.forCurrency =currency;
        }
    }, this);
       
//        this.Name.on('select',this.setSalesPerson,this)
        this.contactPerson=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.ContactPerson*"),  //'Contact Person*',
            name: 'contactperson',
            id:'contactperson' + this.id,
            maxLength:100,
            anchor:'75%',
            scope:this,
            allowBlank:false
        });     
        
        this.attachment=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.address.attachment"),  //'File Attachment
            inputType:'file',
            hidden:true,
            hideLabel:true,
            name:'fileattachment',
            maxLength:100,
            anchor:'75%',
            scope:this,
            allowBlank:true
        });     
        
        this.leaseAmount=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            disabled:true,
            allowBlank:this.isOrder,
            maxLength: 10,
            anchor:'75%',
            id:'leaseamount' + this.id,
            fieldLabel:WtfGlobal.getLocaleText("acc.Lease.LeaseAmount"),  //'Lease Amount',
            name:'leaseamount'
        });
        
        this.leaseTermValue= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            anchor: '95%',
            displayField:'name',
            store:this.leasetermStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.Lease.contractterm")+"*",  //'Lease Amount',
            allowBlank:false,
            typeAhead: true,
            forceSelection: true,
            name:'leaseterm',
            hiddenName:'leaseterm',
            id:'leaseterm' + this.id,
            listeners:{
                'select':{
                    fn:this.updateEndDate,
                    scope:this
                }
            }
        });
      
        
        this.fromDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.Lease.contractstartDate")+"*",
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'startdate',
            id:'startdate' + this.id,
            allowBlank:false,
             disabled:this.isEdit?false:true,
            anchor:'75%'
            
        });
        this.fromDate.on('blur',this.updateEndDate,this);        
        this.toDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.Lease.contractendDate")+"*",
            name: 'enddate',
            id:'enddate' + this.id,
            format : WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
             allowBlank:false,
             disabled:this.isEdit?false:true,
            anchor:'75%',
            scope: this,
            fromenddate:true        //fromenddtae  flag is sent to detect whether the function 'this.update' is called on blur event of 'this.toDate' or not.
        });
       this.toDate.on('blur',this.updateEndDate,this);
        this.termvalue=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            allowBlank:this.isOrder,
            id:'termvalue' + this.id,
            maxLength: 10,
//            width:130,
            anchor:'50%',
            hideLabel:true,
            name:'termvalue'
        });
        this.termvalue.on('blur',this.updateEndDate,this)
        this.termvalue.on('change',this.enbleStartEndDate,this)
        this.agreedservices=new Wtf.form.NumberField({
            allowNegative:false,
            allowDecimals:false,
            defaultValue:1,
//            width:'75%',
             anchor:'75%',
            maxLength: 10,
            fieldLabel: WtfGlobal.getLocaleText("acc.Lease.agreedservices"),
            name:'agreedservices'
        });
        
        this.agreedservices.on("blur",this.addservicerec,this);
             
        this.leaseStatus= new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            store:this.leaseStatusStore,
            addNoneRecord: true,
            disabled:this.isEdit,
            hidden:true,
            hideLabel:true,
            anchor:'75%',
//             allowBlank:false,
//            width : 240,
            forceSelection: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.21"),
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectLeaseStatus"),
            name:'leasestatus',
            hiddenName:'leasestatus'  
        });
        
        this.securityDeposite=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            fieldLabel: WtfGlobal.getLocaleText("acc.Lease.securitydeposite"),
            maxLength: 10,
            anchor:'75%',
            id:'securitydeposite'+this.id,
//            hideLabel:true,
            name:'securitydeposite'
        });
        
        this.originalendDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.Lease.orgenddate"),
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'orgenddate',
            id:'orgenddate'+this.id,
            anchor:'75%'
        });
        this.signinDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.Lease.signindate"),
            name: 'signindate',
            id:'signindate'+this.id,
            format : WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            anchor:'75%',
            scope: this
        });
        
        this.moveindate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.Lease.moveindate"),
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'moveindate',
            id:'moveindate'+this.id,
            anchor:'75%'
        });
        this.moveoutdate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.Lease.moveoutdate"),
            name: 'moveoutdate',
            id:'moveoutdate'+this.id,
            format : WtfGlobal.getOnlyDateFormat(),
            maxLength: 255,
            anchor:'75%',
            scope: this
        });
        this.email = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.profile.email"),  //'E-Mail',
            name:'emailid',
            id:'emailid'+this.id,
//            allowBlank:false,
            anchor:'75%',
            validator:WtfGlobal.validateEmail
        });
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.memo"),
            name: 'memo',
            id:'memo'+this.id,
            height:40,
            anchor:'75%',
            maxLength:2048,
            qtip:(this.record==undefined)?' ':this.record.data.memo,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
        });
        
        this.frequencyComboStore = new Wtf.data.SimpleStore({
	    fields: ["id", "name"],
	    data :[["day","Daily"],["week","Weekly"],["month","Monthly"],["other",WtfGlobal.getLocaleText("acc.value.other")]]
	});
        
        this.frequencyCombo = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.contract.invoicefrequency")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.contract.invoicefrequencyhelp")),//Invoice Frequency
            store: this.frequencyComboStore,
            hiddenName:'frequencyType',
            name:'frequencyType',
            id:'frequencyType'+this.id,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            value: "day",
            triggerAction: 'all',
            typeAhead:true,
//            hideLabel: true,
            labelWidth: 5,
            anchor:'75%',
            selectOnFocus:true
        });
        
        this.frequencyCombo.on('select',this.calculatePeriods,this);
        
        this.numberOfPeriods = new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            hideLabel:false,
            allowDecimals:false,
//            value:0,
//            allowBlank:false,
            disabled:true,
            maxLength: 5,
            anchor:'75%',
            fieldLabel:WtfGlobal.getLocaleText("acc.contract.numberOfPeriods")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.contract.invoicenumberofperiodshelp")),//'Number of Periods',
            name:'numberOfPeriods',
            hiddenName:'numberOfPeriods',
            id:"numberOfPeriods"+this.id
        });
        
        this.numberOfPeriods.on('change',this.iterateStore,this);
       
        
        this.servicecm=[new Wtf.grid.RowNumberer(),{
            header: WtfGlobal.getLocaleText("acc.Lease.servicedate"),
            dataIndex: 'servicedate',
            minValue:new Date().clearTime(true),
            editor:this.setDate=new Wtf.form.DateField({
                align:'left',
                allowBlank:false,
                format:WtfGlobal.getOnlyDateFormat()
            }),
            renderer:WtfGlobal.onlyDateLeftRenderer         
        }];
        
        this.setDate.on("blur",this.checkServiceDate,this);
        
        /**
         * Add Follwoing Fields for Auto DO case
         */
        this.sequenceFormatStoreRecDo = new Wtf.data.Record.create([
            {
                name: 'id'
            },
            {
                name: 'value'
            },
            {
                name: 'oldflag'
            }
        ]);
        this.sequenceFormatStoreDo = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRecDo),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: "autodo",
                isEdit: this.isEdit
            }
        });
        this.sequenceFormatStoreDo.on('load', function() {
            if (this.sequenceFormatStoreDo.getCount() > 0) {
                var seqRec = this.sequenceFormatStoreDo.getAt(0)
                this.sequenceFormatComboboxDo.setValue(seqRec.data.id);
                var count = this.sequenceFormatStoreDo.getCount();
                for (var i = 0; i < count; i++) {
                    seqRec = this.sequenceFormatStoreDo.getAt(i)
                    if (seqRec.json.isdefaultformat == "Yes") {
                        this.sequenceFormatComboboxDo.setValue(seqRec.data.id)
                        break;
                    }
                }
                this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);
            }
        }, this);
        this.sequenceFormatStoreDo.load();

        this.sequenceFormatComboboxDo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>", //WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStoreDo,
            width: 240,
            maxLength: 2048,
            typeAhead: true,
            forceSelection: true,
            name: 'sequenceformatDo',
            hiddenName: 'sequenceformatDo',
            hideLabel: this.isNormalContract,
            hidden: this.isNormalContract,
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumberDo,
                    scope: this
                }
            }

        });
        this.sequenceFormatComboboxDo.on("render",function(){
            WtfGlobal.hideFormElement(this);
        });

        this.no = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.DeliveryOrderNumber*.tt") + "'>" + WtfGlobal.getLocaleText("acc.field.DeliveryOrderNumber*") + "</span>",
            name: 'numberDo',
            scope: this,
            maxLength: 45,
            width: 240,
            hiddenName: 'numberDo',
            hideLabel: this.isNormalContract,
            hidden: this.isNormalContract,
            id: "deliveryONo" + this.heplmodeid + this.id,
            allowBlank: true
        });
        this.no.on("render",function(){
            WtfGlobal.hideFormElement(this);
        });
        this.autoGenerateDO = new Wtf.form.Checkbox({
            name: 'autogenerateDO',
            id: "autogenerateDO" + this.heplmodeid + this.id,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.cust.generateDO.tt") + "'>" + WtfGlobal.getLocaleText("acc.cust.generateDO") + "</span>",
            checked: (this.DOSettings != null ? this.DOSettings : false),
            hideLabel: this.isNormalContract,
            hidden: this.isNormalContract,
            cls: 'custcheckbox',
            width: 10,
            checked:false,
            disabled:this.isEdit
        });
        this.autoGenerateDO.on('check', function(o, newval, oldval) {

            if (this.autoGenerateDO.getValue()) {
                this.showDO();
            } else {
                this.hideDO();
            }

        }, this);

        this.dateofdelivery = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.dodate') + '*', //Date Of Delivery
            format: WtfGlobal.getOnlyDateFormat(),
            name: 'dateofdelivery',
            allowBlank: true,
            hideLabel: this.isNormalContract,
            hidden: this.isNormalContract,
            width: 250
        });
        this.dateofdelivery.setValue(new Date());
        this.dateofdelivery.on("render", function() {
            WtfGlobal.hideFormElement(this);
        });
        this.servicegrid = new Wtf.grid.EditorGridPanel({
            layout:'fit',
            clicksToEdit:1,
            store: this.serviceStore,
            height:100,
            autoScroll : true,
            disabledClass:"newtripcmbss",
            cm: new Wtf.grid.ColumnModel(this.servicecm),
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:false,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
         this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm"+this.id,
            compId1:this.id,
            autoHeight: true,
            moduleid: this.moduleid,
            isEdit: this.isEdit,
            record: this.record
        });
        
        // Maintenance schedule button
        
//        this.maintenanceScheduleButton = new Wtf.Button({
//            text: WtfGlobal.getLocaleText("acc.maintenance.schedule"),//Maintenance Schedule
//            tooltip: WtfGlobal.getLocaleText("acc.maintenance.scheduleTT"),
//            hidden:false,
//            scope: this,
//            handler: this.maintenanceScheduleButtonHandler
//        }); 
        
        
        
//         this.customFieldset = new Wtf.form.FieldSet({
//            title: WtfGlobal.getLocaleText("acc.Lease.maintancesheduleservice"),
//            autoHeight: true,
//            width:'97%',
//            border: false,
//            items:[this.maintenanceScheduleButton]
//             
//        });
       
//        if(this.isNormalContract){
            this.productGrid = new Wtf.account.ContractProductDetailsGrid({
                height: 300,//region:'center',//Bug Fixed: 14871[SK]
                cls:'gridFormat',
                layout:'fit',
                parentCmpID:this.id,
                moduleid: this.moduleid,
                id:this.id+"editproductdetailsgridcontract",
                isCash:false,
                viewConfig:{forceFit:false},
                record:this.record,
                isQuotation:false,
                isQuotationFromPR : false,
                isCustomer:true,
                currencyid:this.currencyid,
                disabledClass:"newtripcmbss",
                disabled:false,
                fromPO:true,//this.isOrder,
                fromOrder:true,
                isEdit:this.isEdit,
                isFromGrORDO:false,
                isOrder:true,
                isInvoice:false,
                forceFit:false,
                editTransaction: this.isEdit,
                loadMask : true,
                readOnly:false,
                viewGoodReceipt: false,
                isLinkedFromTransaction : this.isLinkedFromTransaction,
                parentObj :this,
                isNormalContract:this.isNormalContract
            });
            
            this.productGrid.on('datachanged',this.updateSubtotal,this);
            
            
//        }else{// if lease contract
//            this.productGrid = new Wtf.grid.GridPanel({
//                layout:'fit',
//                store: this.productStore,
//                height:300,
//                autoScroll : true,
//                baseCls:'gridFormFormat',
//                cm:this.columnmodel,
//                border : false,
//                loadMask : true,
//                //            tbar : this.calInvoiceTermBtn,
//                viewConfig: {
//                    forceFit:false,
//                    emptyText:WtfGlobal.getLocaleText("acc.common.norec")
//                }
//            });
//        }
        
//        this.fromSO.on("select",function(){
//            if(this.isNormalContract){
//                this.productGrid.isLinkedFromTransaction = true;
//                this.productGrid.getStore().removeAll();
//                this.productGrid.getStore().on('load',function(store, recArr){
//                   this.productGrid.addBlank(store);
//                   
//                    var amount=0;
//                    for(var i=0;i<this.productGrid.getStore().getCount();i++){
//                        if(this.productGrid.getStore().getAt(i).data.amount && this.productGrid.getStore().getAt(i).data.amount != "")
//                            amount+=this.productGrid.getStore().getAt(i).data.amount;
//                    }
//                    this.leaseAmount.setValue(amount);
//                   
//               },this);
//                this.productGrid.getStore().proxy.conn.url = "ACCSalesOrderCMN/getSalesOrderRows.do";
//                this.productGrid.getStore().load({
//                    params:{
//                        bills: this.fromSO.getValue()
//                        }
//                    });
//                    
//            }else{
//                this.productStore.load({params:{bills: this.fromSO.getValue()}});
//            }
//            
//        },this)
    },
    onCustomerLoad:function(){
        if (this.isEdit) {
            if (this.record) {
                var data = this.record.data;
                this.custmerCmb.setValue(data.accid);
            }
        }
    },
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid='contactDetailCustomerTab';
        Wtf.getCmp(tabid).on('update', function(){
           Wtf.customerAccStore.load();
        }, this);
    },
    /**
     * Hide Auto do fields
     */
    hideDO: function() {
        WtfGlobal.hideFormElement(this.sequenceFormatComboboxDo);
        WtfGlobal.hideFormElement(this.no);
        WtfGlobal.hideFormElement(this.dateofdelivery);
        this.no.allowBlank = true;
        this.dateofdelivery.allowBlank = true;
    },
    showDO: function() {
        WtfGlobal.showFormElement(this.sequenceFormatComboboxDo);
        WtfGlobal.showFormElement(this.no);
        WtfGlobal.showFormElement(this.dateofdelivery);
        this.no.allowBlank = false;
        this.dateofdelivery.allowBlank = false;

    },
    /**
     * Get Sequnce format for Auto DO
     */
    getNextSequenceNumberDo: function(a, val) {
        if (!(a.getValue() == "NA")) {
            this.setTransactionNumberDo(true);
            var rec = WtfGlobal.searchRecord(this.sequenceFormatStoreDo, a.getValue(), 'id');
            var oldflag = rec != null ? rec.get('oldflag') : true;
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: this.fromnumberDo,
                    sequenceformat: a.getValue(),
                    oldflag: oldflag
                }
            }, this, function(resp) {
                if (resp.data == "NA") {
                    this.no.reset();
                    this.no.enable();
                } else {
                    this.no.setValue(resp.data);
                    this.no.disable();
                }

            });
        } else {
            if (!this.readOnly) {
                this.no.reset();
                this.no.enable();
            }
        }
    },
    setTransactionNumberDo: function(isSelectNoFromCombo) {
        var format = this.isCustomer ? Wtf.account.companyAccountPref.autodo : Wtf.account.companyAccountPref.autogro;
        var temp2 = this.isCustomer ? Wtf.autoNum.DeliveryOrder : Wtf.autoNum.GoodsReceiptOrder;
        if (isSelectNoFromCombo) {
            this.fromnumberDo = temp2;
        } else if (format && format.length > 0) {
            WtfGlobal.fetchAutoNumber(temp2, function(resp) {
                if (this.isEdit)
                    this.no.setValue(resp.data)
            }, this);
        }
    }, 
    calculatePeriods : function(){
        
        this.numberOfPeriods.disable();

        var isOtherCase = this.frequencyCombo.getValue() == "other";
        
        var periodCnt = 0;

        if(!isOtherCase){

            var starttime = new Date();

            if(this.fromDate && this.fromDate.getValue()){
                starttime = this.fromDate.getValue();//Date.parseDate(this.fromDate.getValue(), this.fromDate.format);
            }

            var endtime = new Date();

            if(this.toDate && this.toDate.getValue()){
                endtime = this.toDate.getValue();//Date.parseDate(this.toDate.getValue(), this.toDate.format);
            }

            while(starttime<=endtime){
                if(this.frequencyCombo.getValue() == "day"){
                    starttime = starttime.add(Date.DAY,1);
                } else if(this.frequencyCombo.getValue() == "week"){
                    starttime = starttime.add(Date.DAY,7);
                } else if(this.frequencyCombo.getValue() == "month"){
                    starttime = starttime.add(Date.MONTH,1);
                } else if(this.frequencyCombo.getValue() == "year"){
                    starttime = starttime.add(Date.YEAR,1);
                }
                periodCnt++;
            }

            if(periodCnt<=0){
                periodCnt = 1;
            }

            this.numberOfPeriods.setValue(periodCnt);

        }else{
            this.numberOfPeriods.setValue(periodCnt);
            this.numberOfPeriods.enable();
        }

        this.iterateStore();
        return periodCnt;

    },
    
        addservicerec:function(){ 
            var i=0;
            if(this.isEdit){
                var NewCount=this.agreedservices.getValue();
                var NewRecCount=this.serviceRecCount-NewCount;
                var oldCount=this.serviceRecCount;
                if(NewRecCount<0){
                    for(i=0;i<(-(NewRecCount));i++){
                        this.addGridRec();
                    }
                }else if(NewRecCount>0){
                    for(i=0;i<NewRecCount;i++){
//                        var rec=this.serviceStore.getAt(oldCount-1);
//                        this.deletedArr.push(rec.data.id);
                        this.serviceStore.remove(this.serviceStore.getAt(oldCount-1));
                         oldCount--;

                    }
                    
                }
             this.serviceRecCount= NewCount;  
                
            }else{
                this.serviceStore.removeAll();
                for(i=0;i<this.agreedservices.getValue();i++){
                    this.addGridRec();
                }
            }
            
        },
        addGridRec:function(){ 
        var rec= this.serviceRec;
        rec = new rec({});
        rec.beginEdit();
             var fields=this.serviceStore.fields;
            for(var x=0;x<fields.length;x++){
                var value="";
                rec.set(fields.get(x).name, value);   
            }
        rec.endEdit();
        rec.commit();
        this.serviceStore.add(rec);
    },
    
    calculatePeriodDate:function(a,val){
        
    },
     setNextNumber:function(config){
        if(this.sequenceFormatStore.getCount()>0){
        if(this.isEdit && !this.copyInv){ //only edit case
            var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
            if(index!=-1){
                this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                this.sequenceFormatCombobox.disable();
                this.PID.disable();   
            } else {
                this.sequenceFormatCombobox.setValue("NA"); 
                this.sequenceFormatCombobox.disable();
                this.PID.enable();  
            }
        }
        if(!this.isEdit ){   //there is no functionality for copy for Contract hence it will be always new case
            var count=this.sequenceFormatStore.getCount();
            for(var i=0;i<count;i++){
                var seqRec=this.sequenceFormatStore.getAt(i)
                if(seqRec.json.isdefaultformat=="Yes"){
                    this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                    break;
                }
            }
            if(this.sequenceFormatCombobox.getValue()!=""){
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
            } else{
                 this.PID.setValue("");
                 this.PID.disable();
            }
        }
       }
    },
    getNextSequenceNumber:function(a,val){
        
        //        this.setTransactionNumber(true);
        if(!(a.getValue()=="NA")){
            var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag=rec!=null?rec.get('oldflag'):true;
            Wtf.Ajax.requestEx({
                //             url:"ACCProduct/getProductIDAutoNumber.do",
                //             params: {dummyParam:true}
                url:"ACCCompanyPref/getNextAutoNumber.do",
                params:{
                    from:Wtf.Acc_Contract_ModuleId,
                    sequenceformat:a.getValue(),
                    oldflag:oldflag
                }
            }, this,function(resp){
                if(resp.data=="NA"){
                    this.PID.reset();
                    this.PID.enable();
                }else {
                    this.PID.setValue(resp.data);
                    this.PID.disable();
                }
            
            });
        } else {
            this.PID.reset();
            this.PID.enable();
        }
    },
    createForm:function(){
        this.contractForm=new Wtf.form.FormPanel({
            region: 'north',
            autoHeight: true,        
            id:"northForm"+this.id,
            disabledClass:"newtripcmbss",
            // bodyStyle:"padding:10px",
            border:false,
            items:[{
                layout:'form',
                defaults:{
                    border:false
                },
                baseCls:'northFormFormat',
                labelWidth:160,
                items:[{
                    layout:'column',
                    defaults:{
                        border:false
                    },
                    items:[{
                        layout:'form',
                        columnWidth:0.49,
                        items:[{
                            xtype:'hidden',
                            name:'productid', 
                            value:(this.isClone)?"":(this.record==null?"":this.record.data.productid)
                        },
                        this.custmerCmb,this.Currency,
                        this.fromSO, 
                        this.sequenceFormatCombobox,
                        this.PID,
                        this.contactPerson,this.securityDeposite,this.leaseAmount,this.email,this.attachment,
                        this.autoGenerateDO,this.sequenceFormatComboboxDo,this.no,this.dateofdelivery
                        ]
                    },{
                        layout:'form',
                        columnWidth:0.49,
                        items:[{
                            layout:'column',
                            border:false,
                            defaults:{
                                border:false
                            },
                            items:[{
                                layout:'form',
                                width:'50%',
                                items:this.leaseTermValue
                            },{
                                layout:'form',
                                width:'50%',
                                items:this.termvalue
                            }]
                        },this.fromDate, this.toDate,this.frequencyCombo,this.numberOfPeriods,this.leaseStatus,this.originalendDate,this.signinDate,this.moveindate,this.moveoutdate,this.Memo]
                    }]
            },this.tagsFieldset]
            }]
        });
 
    },
    checkServiceDate:function(rec,store){
        if(this.fromDate.getValue()!=undefined && this.fromDate.getValue()!=""){
            if(rec.value!=""){
                var contractDate=this.fromDate.getValue();
                var contractEndDate=this.toDate.getValue();
                var recDate=rec.value;
                if(new Date(Date.parseDate(recDate,WtfGlobal.getOnlyDateFormat())).getTime() < new Date(contractDate).getTime()){
                   rec.setValue("");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.contract.startDate.limit")], 2);
                    return;
                }
                if(new Date(Date.parseDate(recDate,WtfGlobal.getOnlyDateFormat())).getTime() > new Date(contractEndDate).getTime()){
                   rec.setValue("");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.contract.startDate.Endlimit")], 2);
                    return;
                }
            }
            
        }else{
             rec.setValue("");
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.contract.startDate.alert")], 2);
                    return;
        }
     },
     updateEndDate:function(a,val){
        var term=null;        
         var termtype=this.leaseTermValue.getValue();  //['1','Day'],['2','Week'],['3','Month'],['4','Year']
        var initialEndDateEnteredByUser= this.toDate.getValue();
        switch(termtype*1){
            case 1://Day
                if(this.fromDate.getValue()!="" && this.termvalue.getValue()!=""&&isNaN(this.termvalue.getValue())==false){
                term=new Date(this.fromDate.getValue()).add(Date.DAY, this.termvalue.getValue());
                }
                else if(this.fromDate.getValue()!="" &&isNaN(this.fromDate.getValue())==false){
                    term=this.fromDate.getValue();
                }
                break;    
             case 2://Week
                if(this.fromDate.getValue()!="" && this.termvalue.getValue()!=""&&isNaN(this.termvalue.getValue())==false){
                term=new Date(this.fromDate.getValue()).add(Date.DAY,this.termvalue.getValue()*7);
                }
                else if(this.fromDate.getValue()!="" &&isNaN(this.fromDate.getValue())==false){
                    term=this.fromDate.getValue();
                }
                break; 
             case 3://Month
                if(this.fromDate.getValue()!="" && this.termvalue.getValue()!=""&&isNaN(this.termvalue.getValue())==false){
                term=new Date(this.fromDate.getValue()).add(Date.MONTH,this.termvalue.getValue());
                }
                else if(this.fromDate.getValue()!="" &&isNaN(this.fromDate.getValue())==false){
                    term=this.fromDate.getValue();
                }
                break; 
             case 4://Year
                if(this.fromDate.getValue()!="" && this.termvalue.getValue()!=""&&isNaN(this.termvalue.getValue())==false){
                term=new Date(this.fromDate.getValue()).add(Date.YEAR,this.termvalue.getValue());
                }
                else if(this.fromDate.getValue()!="" &&isNaN(this.fromDate.getValue())==false){
                    term=this.fromDate.getValue();
                }
                break; 
        } 
        if(term != null) {
            term.setDate(term.getDate()-1);
            this.contractForm.getForm().setValues({enddate:term});
        } 
        var endDateAfterCalculation = this.toDate.getValue();
        if(a.fromenddate && (initialEndDateEnteredByUser.getTime() != endDateAfterCalculation.getTime())){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.ContractEnddateShouldNotChange")], 3);
        } 
        this.calculatePeriods();
        
        if(this.productGrid) {                       // SET FOR CONTARCT GRID AS PARM TO GET INDIVIDUAL PRODUCT PRICE FOR CUSTOMER 
            this.productGrid.billDate=this.fromDate.getValue();
        }
    },
    
    iterateStore:function(){

        var numberOfPeriods = this.numberOfPeriods.getValue();

        this.productGrid.getStore().each(function(rec){
            var rate = rec.get('rate');
            if(numberOfPeriods>0){
                var unitPricePerInvoice = rate/numberOfPeriods;
                rec.set('unitPricePerInvoice', unitPricePerInvoice);
            }

        },this);
    },
    
    enbleStartEndDate:function(txt,newval,oldval){
        var termValue=this.termvalue.getValue();
        if(termValue!=""){
            this.fromDate.enable();
            this.toDate.enable(); 
         }
         else{
             this.fromDate.disable();
             this.toDate.disable();
         }
    },
     updateData:function(){
        var customer= this.custmerCmb.getValue();
        Wtf.Ajax.requestEx({
            url:"ACCCustomerCMN/getCurrencyInfo.do",
            params:{
                mode:4,
                customerid:customer,
                isBilling : false
            }
        }, this,this.setCurrencyInfo);     
        
        if(this.productGrid) {                       // SET FOR CONTARCT GRID AS PARM TO GET INDIVIDUAL PRODUCT PRICE FOR CUSTOMER 
            this.productGrid.affecteduser = customer;
        }
//        this.SOStore.load();
        this.fromSO.clearValue();
        this.SOIndex = -1;
        this.leaseAmount.enable();
        this.leaseAmount.reset();
        this.leaseAmount.disable();
        this.productStore.removeAll();
        this.tagsFieldset.resetCustomComponents();
        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
        this.tagsFieldset.setValuesForCustomer(moduleid, customer);
    },
    setCurrencyInfo:function(response){
        if(response.success){
            this.externalcurrencyrate=0;
            this.custdatechange=true;
            this.Currency.setValue(response.currencyid);
            this.currencyid=response.currencyid;
            this.symbol = response.currencysymbol;       
            this.custChange=true;
            this.changeCurrencyStore();
            this.currencyStore.load();
            this.SOStore.load();
        }
    },
     changeCurrencyStore:function(pronamearr){
        this.pronamearr=pronamearr;
        var currency=this.Currency.getValue();
        if(this.val=="")this.val=new Date();
        if(currency!=""||this.custChange)
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val),tocurrencyid:this.Currency.getValue()}});
        else
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val)}});
    },
    addBlankRow:function(){
        var Record = this.productStore.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for(var j = 0; j < fl; j++){
            f = fi[j];
            if(f.name!='rowid') {
                blankObj[f.name]='';
                if(!Wtf.isEmpty(f.defValue))
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        this.productStore.add(newrec);
    },
    setTaxAmountAfterSelection:function(rec) {
        
        var quantity = rec.data.dquantity;
        quantity = (quantity == "NaN" || quantity == undefined || quantity == null)?0:quantity;
//        var rec=obj.record;
        var discount = 0;
        var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
        quantity=getRoundofValue(rec.data.dquantity);
        var origionalAmount = getRoundedAmountValue(rate*quantity) ;
//        if(rec.data.partamount != 0){
//            var partamount=getRoundedAmountValue(rec.data.partamount);
//            origionalAmount = getRoundedAmountValue(origionalAmount * (partamount/100));
//        }
        
//        origionalAmount = this.calAmountWithExchangeRate(origionalAmount, rec);
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue(origionalAmount * prdiscount/ 100);
            } else {
                discount = prdiscount;
            }
        }
//        var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
        var val=origionalAmount-discount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=getRoundedAmountValue(taxrec.data.percent);
        }
        var taxamount= getRoundedAmountValue(val*taxpercent/100);
        return taxamount;
        
    },
    setTaxAmountWithotExchangeRate:function(v,m,rec){
        var taxamount= 0;
        if(v)
            taxamount= parseFloat(getRoundedAmountValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1;
        if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == ""){
            taxamount = 0;
        }
        rec.set("taxamount",taxamount);
        return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
    },
    
    calAmountWithoutExchangeRate:function(v,m,rec){
        var origionalAmount = rec.data.rate*rec.data.quantity;
        if(rec.data.partamount != 0){
            origionalAmount = origionalAmount * (rec.data.partamount/100);
        }
        
        var discount = 0;//origionalAmount*rec.data.prdiscount/100   
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount = (origionalAmount * rec.data.prdiscount) / 100;
            } else {
                discount = rec.data.prdiscount;
            }
        }
        
        var val=(origionalAmount)-discount;///rec.data.oldcurrencyrate  
       // rec.set("amountwithouttax",val);
        var taxamount = 0;
        if(rec.data.rowTaxAmount){
            taxamount= rec.data.rowTaxAmount;
        }
        val = parseFloat(val) + parseFloat(taxamount);

        /*  Setting amount in store for the purpose of calculating "Contract Amount"
         * Here "rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));" is not working So
         */
        for (var i = 0; i < rec.store.getCount(); i++) {

            /* Fetching productid from Store*/
            var productid = rec.store.data.items[i].data.productid;

            if (rec.data.productid == productid) {
                rec.store.data.items[i].data.amount = (parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL) * 1);
                break;

            }
        }

        //rec.set("amount",(parseFloat(getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1));
        
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    },
    getProductDetails:function(){
   
        this.productGrid.getStore().each(function(rec){
                if(rec.data.rowid==undefined){
                    rec.data.rowid='';
                    
                }
                if(rec.data.amount==""){
                    rec.data.amount=0;
                    
                }
                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
                rec.data[CUSTOM_FIELD_KEY_PRODUCT]=Wtf.decode(WtfGlobal.getCustomColumnDataForProduct(rec.data, this.moduleid).substring(20));
            },this);
        
        var arr=[];      
        this.productGrid.getStore().each(function(rec){
                arr.push(this.store.indexOf(rec));
        });
        var jarray=[];
        
//        if(this.isNormalContract){
            jarray=WtfGlobal.getJSONArray(this.productGrid,this.productGrid.isLinkedFromTransaction,arr);
//        }else{
//            jarray=WtfGlobal.getJSONArrayForExactLength(this.productGrid,false,arr);
//        }
        
        return jarray;
    },
     getAgreedServiceDetails:function(){
        var arr=[];
        for(var i=0;i<this.servicegrid.getStore().getCount();i++){
            var rec=this.servicegrid.getStore().getAt(i)
             if(rec.data.servicedate!=undefined && rec.data.servicedate!=""){
                rec.set('servicedate', (new Date(rec.data.servicedate).clearTime()));
                arr.push(i);
                this.deletedArr.remove(rec.data.id);
            } else{                
                return false;
            }         
        }        
        
        return WtfGlobal.getJSONArrayForExactLength(this.servicegrid,true,arr);
    }, 
    save:function(){
   
    if(!this.contractForm.getForm().isValid()){
        WtfComMsgBox(2, 2);
            return;
    }
    if(Wtf.isCRMSync){
        var custId = this.custmerCmb.getValue();
        var custRec=WtfGlobal.searchRecord(this.customerAccStore, custId, 'accid');
        if(custRec.get('crmAccountId') == null || custRec.get('crmAccountId') == undefined || custRec.get('crmAccountId') == ''){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.customer.crm.sync")+' '+custRec.get('accname')+' '+WtfGlobal.getLocaleText("acc.customer.sync")], 3);
            return;
        }
    }
    /**
     * Check Batch Serial details if Auto DO case
     */
        if (!this.isNormalContract) {
            var detail = this.productGrid.getProductDetails();
            if (this.autoGenerateDO.getValue() == true) {
//                var jsonBatchDetails = eval(detail);
                var prodLength = this.productGrid.getStore().data.items.length;
                for (var i = 0; i < prodLength; i++) {
                    var prodID = this.productGrid.getStore().getAt(i).data['productid'];
                    var prorec = this.productGrid.productComboStore.getAt(this.productGrid.productComboStore.find('productid', prodID));
                    if (prorec == undefined) {
                        prorec = this.productGrid.getStore().getAt(i);
                    }
                    if (Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory) { //if company level option is on then only check batch and serial details
                        if ((prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct)) {
                            if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part' && !prorec.data.isAsset) {
                                var batchDetail = this.productGrid.getStore().getAt(i).data['batchdetails'];
                                if (batchDetail == undefined || batchDetail == "" || batchDetail == "[]") {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.bsdetail")], 2);   //Batch and serial no details are not valid.
//                                this.enableSaveButtons();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
    // Checking for deactivated products
   var inValidProducts=this.checkForDeActivatedProductsAdded();
        if(inValidProducts!=''){
            inValidProducts = inValidProducts.substring(0, inValidProducts.length-2);
            Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), 
                    msg: WtfGlobal.getLocaleText("acc.common.followingProductsAreDeactivated")+'</br>'+'<b>'+inValidProducts+'<b>',
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
   
    this.fromSO.setValue(this.fromSO.getValue().trim());       
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(this.contractForm.getForm().isValid() && isValidCustomFields ) {
//            if(this.isCustBill){
//                for(var datacount=0;datacount<this.Grid.getStore().getCount();datacount++){
//                    var creditoracc=this.Grid.getStore().getAt(datacount).data['creditoraccount'];                    
//                    if(creditoracc==undefined||creditoracc==""){
//                        if(this.Grid.getStore().getAt(datacount).data['productdetail'].length>0){
//                            var account=(this.isCustomer)?"Credit account":"Debit account";
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselect")+account], 2);
//                            return;
//                        } 
//                    }            
//                }
//            }
                
            var productsCount = this.productGrid.getStore().getCount()-1; 
            if(this.productGrid.isLinkedFromTransaction){
                productsCount = this.productGrid.getStore().getCount();
            }   
            for(var i=0;i<productsCount;i++){// excluding last row
                var quantity=this.productGrid.getStore().getAt(i).data['quantity'];
                var rate=this.productGrid.getStore().getAt(i).data['rate'];
                if(quantity<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+this.productGrid.getStore().getAt(i).data['productname']+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    return;
                } 
                if(rate===""||rate==undefined||rate<0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+this.productGrid.getStore().getAt(i).data['productname']+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                    return;
                }
            }
//            for(i=0;i<this.serviceStore.getCount();i++){
//                var serviceDate=this.serviceStore.getAt(i).data['servicedate'];
//                if(serviceDate==""){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.contract.servicedateempty")], 2);
//                    return;
//                } 
//            }
//          
            
            var count=this.productGrid.getStore().getCount();
            if(count<1){
                WtfComMsgBox(33, 2);
                return;
            }                        
         
           var rec=this.contractForm.getForm().getValues();
           
            if(this.schedulerFormInformation){
                rec.scheduleNumber=this.schedulerFormInformation.scheduleNumber;
                rec.scheduleStartDate=this.schedulerFormInformation.scheduleStartDate;
                rec.hiddenCurrentDate=this.schedulerFormInformation.hiddenCurrentDate;
                rec.scheduleEndDate=this.schedulerFormInformation.scheduleEndDate;
                //                rec.firstScheduleEndDate=WtfGlobal.convertToGenericDate(this.firstScheduleEndDate.getValue());
                rec.isAdHocSchedule=this.schedulerFormInformation.isAdHocSchedule;
                rec.repeatInterval=this.schedulerFormInformation.repeatInterval;
                rec.intervalType=this.schedulerFormInformation.intervalType;
                rec.totalEvents=this.schedulerFormInformation.totalEvents;
                rec.scheduleDuration=this.schedulerFormInformation.scheduleDuration;
                //                rec.assetId=assetDetailId;
                rec.adHocEventDetails=this.schedulerFormInformation.adHocEventDetails;
                rec.totalEventsStopCondition=this.schedulerFormInformation.totalEventsStopCondition;
                rec.endDateStopCondition=this.schedulerFormInformation.endDateStopCondition;
                
                if(this.isEdit){
                    rec.scheduleId=this.schedulerFormInformation.scheduleId;
                }
                
                rec.isScheduleIncluded=true;
                rec.isScheduleBtn=this.schedulerFormInformation.isScheduleBtn;
                
                if(!(new Date(this.schedulerFormInformation.scheduleStartDate).between(this.fromDate.getValue(), this.toDate.getValue()))){// Maintenance Schedule Start date should be between Contract Start Date and Contract End Date
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.scheduledate")], 3);
                    return;
                }
                
            }else{
                
            /*
             * In case of edit if Contract From Date and End Date get Changed but Maintenance Schedule Start Date does not chhanged and it does not lie between Contract Start & End date
             */
            if(this.isEdit &&!(this.record.get('scheduleStartDate') == null || this.record.get('scheduleStartDate') == undefined || this.record.get('scheduleStartDate') == '')){
                if(!(this.record.get('scheduleStartDate').between(this.fromDate.getValue(), this.toDate.getValue()))){// Maintenance Schedule Start date should be between Contract Start Date and Contract End Date
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.contract.scheduledate")], 3);
                    return;
                }
            }
                
                rec.isScheduleIncluded=false;
            }
            
            
            rec.customer=this.custmerCmb.getValue();
            this.ajxurl = ""; 
            this.ajxurl = "ACCInvoice/saveContract.do";
//            var currencychange=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""&&!this.isOrder;
//            var msg=currencychange?WtfGlobal.getLocaleText("acc.field.Currencyrateyouhaveappliedcannotbechanged"):"";           
            var detail = this.getProductDetails();
            var servicedetail = this.getAgreedServiceDetails();
            if(servicedetail==false){
              WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"Please fill the all service dates."],2);   //"Product(s) details are not valid."
              return;  
            }
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
                }
            if(Wtf.account.companyAccountPref.memo== true && (rec.memo==""))    //memo related setting wether option is true
            {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText({
                    key:"acc.common.memoempty",
                    params:[Wtf.account.companyAccountPref.descriptionType]
                }),function(btn){
                    if(btn!="yes") {
                        return;
                    }
//                    this.checkMemo(rec,detail,incash) 
                       this.showConfirmAndSave(rec,detail,servicedetail,false);
                },this);  
            }else {
                  this.showConfirmAndSave(rec,detail,servicedetail,false);
//                this.checkMemo(rec,detail,incash);     
            }

        }else{
            WtfComMsgBox(2, 2);
        }
    },

    showContractAttachWindow: function () {
        openAttachmentsWindow(this);
    },
    showConfirmAndSave: function (rec, detail, servicedetail, incash) {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),this.EditisAutoCreateDO ?  WtfGlobal.getLocaleText("acc.invoice.msg16"):WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
                if(btn!="yes") {return;}
             
                    rec.detail=detail;
                    rec.servicedetail=servicedetail;
                var custFieldArr=this.tagsFieldset.createFieldValuesArray();
                this.msg= WtfComMsgBox(27,4,true);
//                rec.subTotal=this.Grid.calSubtotal()
//                this.applyCurrencySymbol();
//                rec.perdiscount=this.perDiscount.getValue();
//                rec.isOpeningBalanceOrder=this.isOpeningBalanceOrder;
                rec.contactperson=this.contactPerson.getValue();
                rec.leaseAmount=this.leaseAmount.getValue();
                if(rec.leaseAmount == 0 && this.isNormalContract){
                    var amount=0;
                    for(var i=0;i<this.productGrid.getStore().getCount();i++){
                        if(this.productGrid.getStore().getAt(i).data.amount && this.productGrid.getStore().getAt(i).data.amount != "")
                            amount+=Number(this.productGrid.getStore().getAt(i).data.amount);   //ERP-29307
                    }
                    rec.leaseAmount = amount;
                }
                rec.currencyid=this.Currency.getValue();
                rec.customer=this.custmerCmb.getValue();
//                rec.conyractid=this.PID.getValue();
                rec.salesorderno=this.fromSO.getValue();    //ERP-30712-Sales Order ID.
                rec.sono=this.fromSO.getRawValue();     //ERP-30712-Sales Order Number.
                if(!this.isNormalContract){
                    /**
                     * Params in case of Auto DO
                     */
                    rec.linkNumber=this.fromSO.getValue();
                    rec.fromLinkCombo="Lease Order";
                    rec.billdate = WtfGlobal.convertToGenericDate(this.dateofdelivery.getValue());
                    rec.sequenceformatDo=this.sequenceFormatComboboxDo.getValue();
                    rec.isAutoCreateDO = this.autoGenerateDO.getValue();
                }
                rec.leaseTermType=this.leaseTermValue.getValue();
                rec.termvalue=this.termvalue.getValue();
                rec.leaseStatus=this.leaseStatus.getValue();
                rec.securityDeposite=this.securityDeposite.getValue();
                rec.fileidstr= this.attachedFilesStr;
                rec.currencyid=this.Currency.getValue();
                rec.externalcurrencyrate=this.externalcurrencyrate;     
                rec.number=this.PID.getValue();
                rec.emailid=this.email.getValue();
                rec.memo=this.Memo.getValue();
                rec.file=this.attachment.getValue();
                rec.agreedservices=this.agreedservices.getValue();
                rec.isNormalContract=this.isNormalContract;
                rec.numberOfPeriods=this.numberOfPeriods.getValue();
                rec.frequencyType=this.frequencyCombo.getValue();
                if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);



//                rec.linkNumber=(this.PO != undefined && this.PO.getValue()!="")?this.PO.getValue():"";
//                rec.fromLinkCombo=this.fromLinkCombo.getRawValue();
//                rec.duedate=WtfGlobal.convertToGenericDate(this.DueDate.getValue());
//                rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
//                rec.shipdate=WtfGlobal.convertToGenericDate(this.shipDate.getValue());
//                rec.validdate=WtfGlobal.convertToGenericDate(this.validTillDate.getValue());
//                rec.servicecm=WtfGlobal.convertToGenericDate(this.servicecm.getValue());
                rec.originalendDate=WtfGlobal.convertToGenericDate(this.originalendDate.getValue());
                rec.signinDate=WtfGlobal.convertToGenericDate(this.signinDate.getValue());
                rec.moveindate=WtfGlobal.convertToGenericDate(this.moveindate.getValue());
                rec.moveoutdate=WtfGlobal.convertToGenericDate(this.moveoutdate.getValue());
                rec.startdate=WtfGlobal.convertToGenericDate(this.fromDate.getValue());
                rec.enddate=WtfGlobal.convertToGenericDate(this.toDate.getValue());



                if(this.isEdit){
                    rec.deletedServiceDates=this.deletedArr;
                    rec.invoiceid=this.contractID;
                    rec.isEdit=this.isEdit;
                }
                    
                rec.contractid=this.PID.getValue();//this.copyInv?"":
//                rec.doid=this.DeliveryOrderid;
                rec.mode=this.moduleid;
//             
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                rec.savedFilesMappingId = this.savedFilesMappingId;
                  
                Wtf.Ajax.requestEx({
                    url:this.ajxurl,
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);

            },this);
    },
    disableComponent : function(){
        if(this.isNormalContract){
           this.productGrid.purgeListeners();
        }
        if(this.contractForm){
            this.contractForm.disable();
        }
        if(this.saveBttn){
            this.saveBttn.disable();
        }
        if(this.savencreateBttn){
            this.savencreateBttn.disable();
        }
        if(this.attachFilesBttn){
            this.attachFilesBttn.disable();
        }
        if(this.maintenanceScheduleBttn){
            this.maintenanceScheduleBttn.disable();
        }
    },
    genSuccessResponse:function(response, request){
    if(response.success){
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
    } else if (response.isTaxDeactivated) {
        WtfComMsgBox([this.title, "You cannot save " + this.title + " with deacivated tax(s)."], 2);
    } else {
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
    }
        
    if(response.success) {
        if(this.saveOnlyFlag === 1){
            this.isFromSaveAndCreateNewButton=false;
            this.disableComponent();//use of this function - do not reset form
        }else  if(this.saveOnlyFlag === 2){
            
            this.isFromSaveAndCreateNewButton=true;
            this.schedulerFormInformation="";//clear maintenance schedule when after saving data successfully on save and create new button
            this.attachedFilesStr=""//to clear attachment 
            this.attachment.setValue("");
            if(this.maintenanceScheduler){
             this.maintenanceScheduler.fireEvent("update",this);  //for reset of maintenance schedule form in case of save and create new option
            }
            this.leaseAmount.enable();
            this.sequenceFormatStore.load();
            this.contractForm.getForm().reset();
            if(this.numberOfPeriods){
                this.numberOfPeriods.setValue(0);
            }
            this.leaseAmount.disable();
            this.productGrid.getStore().removeAll();
            this.servicegrid.getStore().removeAll();
//            this.lastTransPanel.Store.removeAll();
            this.symbol = WtfGlobal.getCurrencySymbol();
            this.currencyid = WtfGlobal.getCurrencyID();
//            this.loadStore();
//            this.fromPO.disable();
            this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            Wtf.dirtyStore.product = true;
            var customFieldArray = this.tagsFieldset.customFieldArray;  //Reset Custom Fields
            for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                var fieldId = customFieldArray[itemcnt].id
                if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                    Wtf.getCmp(fieldId).reset();
                }
            }    
            var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
            for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                if (Wtf.getCmp(checkfieldId) != undefined) {
                    Wtf.getCmp(checkfieldId).reset();
                }
            } 
            var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Reset Custom Dimension
            for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
                var fieldId1 = customDimensionArray[itemcnt1].id
                if (Wtf.getCmp(fieldId1) != undefined) {
                    Wtf.getCmp(fieldId1).reset();
                }
            } 
            this.amountdue=0;
        }
            
//        if(this.isEdit){
//            this.fireEvent("update",this);
//        }
            
    }
},

    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    updateSubtotal:function(a,val){
        this.applyCurrencySymbol();
        var amount = this.productGrid.calSubtotal();
        
        this.leaseAmount.setValue(amount);
        this.iterateStore();
    },
    
    onCurrencyChangeOnly:function(){
        this.productGrid.getStore().removeAll();
        this.productGrid.addBlankRow(); 
    },
    
    updateFormCurrency:function(){
        this.SOStore.load();
        this.onCurrencyChangeOnly();
        this.applyCurrencySymbol();
        
        var amount = this.productGrid.calSubtotal();
        
        this.leaseAmount.setValue(amount);
    },
    
    applyCurrencySymbol:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
        
        if(index>=0){
           rate=(rate==""?this.currencyStore.getAt(index).data.exchangerate:rate);
            this.symbol=  this.currencyStore.getAt(index).data.symbol;
            this.productGrid.setCurrencyid(this.currencyid,rate,this.symbol,index);
//            this.applyTemplate(this.currencyStore,index);
       }
       return this.symbol;
    },
    
    getCurrencySymbol:function(){
        var index=null;
//        this.currencyStore.clearFilter(true); //ERP-9962
        var FIND = this.Currency.getValue();
        if(FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index=this.currencyStore.findBy( function(rec){
             var parentname=rec.data['currencyid'];
            if(parentname==FIND)
                return true;
             else
                return false
            })
       this.currencyid=this.Currency.getValue();
       return index;
    },
    
    productstoreLoadedFn:function(){
        this.iterateStore();
    },
    
    onProductGridStoreLoad:function(store, recArr){
        var amount=0;
        for(var i=0;i<this.productGrid.getStore().getCount();i++){
            if(this.productGrid.getStore().getAt(i).data.amount && this.productGrid.getStore().getAt(i).data.amount != ""){
                amount+=Number(this.productGrid.getStore().getAt(i).data.amount);   //ERP-29307 : Converted String into Number
            }
        }
        this.leaseAmount.setValue(amount);
        this.productGrid.addBlank(store);
    },
    
maintenanceScheduleButtonHandler:function(){
    
    this.maintenanceScheduler = new Wtf.account.AssetMaintenanceSchedule({
        title:WtfGlobal.getLocaleText("acc.maintenance.scheduler"),
        layout:'border',
        //            id:'maintenanceSchedulerId',
        resizable:false,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        modal:true,
        assetRec:this.assetRec,
        isContract:true,
        contractScheduleInformation:this.schedulerFormInformation,
        isFromCreateButton:!this.isEdit,
        isFromCreationForm:true,
        contractStartDate: (this.fromDate)?this.fromDate.getValue():'',
        contractEndDate: (this.toDate)?this.toDate.getValue():'',
        isEdit:this.isEdit,
        scheduleRecord:this.record,
        isFromSaveAndCreateNewButton:this.isFromSaveAndCreateNewButton,
        height:700,
        width:700
    });
        
    this.maintenanceScheduler.on('beforeclose',this.scheduleBeforeCloseHandler,this);
    this.maintenanceScheduler.on('update',function(){
        this.maintenanceScheduler.resetComponent();
    },this);
    
    this.maintenanceScheduler.show();
},

scheduleBeforeCloseHandler:function(panel){
    if(panel.fromSaveButtonClosed){
        this.schedulerFormInformation = panel.schedulerFormInformation;
    }
},
checkForDeActivatedProductsAdded:function(){
    var invalidProducts='';
    if(!this.isEdit){ // Create new and copy case
        invalidProducts = this.checkDeactivatedProductsInGrid();
    }
    return invalidProducts;
   },
    checkDeactivatedProductsInGrid :function(){
        var inValidProducts=''
        var rec = null;
        var productId = null;
        var productRec = null;
        for(var count=0;count<this.productGrid.store.getCount();count++){
            rec = this.productGrid.store.getAt(count);
            productId = rec.data.productid;
            if(productId!= undefined && productId != null && productId != ''){
                if(this.productOptimizedFlag==Wtf.Show_all_Products){
                    productRec = WtfGlobal.searchRecord(this.productGrid.getStore(), productId, "productid");
                } else {
                    productRec = rec;
                }
                if(productRec && (productRec.data.hasAccess === false)){
                    inValidProducts+=productRec.data.productname+', ';
                }
            }
        }
        return inValidProducts; // List of deactivated products
    }
});
