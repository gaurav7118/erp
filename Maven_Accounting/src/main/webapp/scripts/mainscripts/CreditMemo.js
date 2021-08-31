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
/*< COMPONENT USED FOR >
 *      1.Credit Note
 *          callCreditNote --- < Create Credit Note >
 *          [isCN:true]
 *      2.Debit Note
 *          callCreditNote --- < Create Debit Note >
 *          [isCN:false]
 */
Wtf.account.TrNotePanel=function(config){
    this.modeName = config.modeName;
    this.masterGroup=config.isCN?5:1;
    this.currencyid=null;
    this.externalcurrencyrate=0;
    this.noteType=config.isCN?'Credit':'Debit';
    this.businessPerson=config.isCN?"Customer":"Vendor";
    this.cntype=config.cntype?config.cntype:"1";//1 - CN for unpaid invoices, 3 - CN for Paid invoices
    this.custPermType=config.isCN?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCN?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.isCustomer=config.isCN;
    this.readOnly=config.readOnly;
    Wtf.account.TrNotePanel.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TrNotePanel,Wtf.Panel,{
    onRender:function(config){
        this.createFields();
        this.extendComponent();
        this.addInvoiceTermGrid(this.isEdit);   
        var centerPanel = new Wtf.Panel({
            region : 'center',
            border : false,
            autoScroll : true,
            bodyStyle:"background:#eeeeee;",
             id:"creditNoteDetails"+this.helpmodeid,
             bbar: [{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                scope:this,
                iconCls :getButtonIconCls(Wtf.etype.save),  
                handler: this.save
            },"->",getHelpButton(this,this.helpmodeid)]
        });
        
     this.blankPanel = new Wtf.Panel({
            region : 'center',
            border : false
        });
        
    this.centerNewPanel = new Wtf.Panel({
        region: "center",
        autoHeight:true,
        items: [this.invGrGrid, this.northForm]
    });
    this.southTermGridPanel = new Wtf.Panel({
        layout: "border",
        height:100,
        items: [this.termgrid,this.blankPanel,this.southCalTemp]
    });
    
    
   this.southNewPanel = new Wtf.Panel({
        region: "south",
//        layout: "fit",
        autoHeight:true,
        autoScroll:true,
        items: [this.pdGrid,this.southTermGridPanel]
    });  
    
    centerPanel.add(this.custForm,this.centerNewPanel,this.southNewPanel);
    this.add(centerPanel);
        this.name.on('select',this.loadGridData,this);
        this.invGrGrid.getSelectionModel().on('rowdeselect',this.blankTplSummery,this);
        this.invGrGrid.getSelectionModel().on('rowselect',this.getSelectedRow,this);
        this.pdGrid.getStore().on('load',this.setType,this);
        this.pdGrid.getSelectionModel().on('rowdeselect',this.checkAmountDue,this);
        this.pdGrid.getSelectionModel().on('rowselect',this.updateSubtotal,this);
        this.pdGrid.on('datachanged',this.updateSubtotal,this);
        this.creationDate.setValue(Wtf.serverDate);
        Wtf.account.TrNotePanel.superclass.onRender.call(this,config);
    },     

    createFields:function(){
        this.costCenterId = "";        
            
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'value'
        },{
            name: 'oldflag'
        }]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName,
                isEdit:this.isEdit
            }
        });
          this.sequenceFormatStore.on('load',function(){
             if(this.sequenceFormatStore.getCount()>0){
                 var seqRec=this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                   
                }
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
             }
         },this);
     this.sequenceFormatStore.load();
         
       this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
        width:240,
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformat',
        hiddenName:'sequenceformat',
        listeners:{
            'select':{
                fn:this.getNextSequenceNumber,
                scope:this
            }
        }
            
    });
        
        this.no=new Wtf.form.TextField({
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN")) +" "+ WtfGlobal.getLocaleText("acc.cn.9") + "*",  //this.noteType+' Note No*',
            name: 'number',
            scope:this,
            maxLength:45,
            anchor:'90%',
            allowBlank:false
        });
        this.setTrNoteNumber();
        this.isCN?chkcustaccload():chkvenaccload();
        this.name=new Wtf.form.ExtFnComboBox({
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            id:"selectCustomer"+this.helpmodeid,
            hiddenName:'accid',
            store:this.isCN?Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'acccode',
            minChars:1,
            scope:this,
            anchor:'90%',
            displayField:'accname',
//            allowBlank:false, //Checked at a time of saving
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven"),  //'Please Select a '+this.businessPerson+'...',
            forceSelection: true,
            hirarchical:true
//            addNewFn:this.callCustomer.createDelegate(this,[false, null,'custwin',this.isCN])
        });
       if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create))
            this.name.addNewFn=this.callCustomer.createDelegate(this,[false, null,'custwin',this.isCN]);
        this.creationDate=new Wtf.form.DateField({
             xtype:'datefield',
             name:'creationdate',
             allowBlank:false,
             anchor:'90%',
             format:WtfGlobal.getOnlyDateFormat(),
             fieldLabel:WtfGlobal.getLocaleText("acc.customer.date") + "*"  //'Creation Date*'
        });
        this.textBoxName=new Wtf.form.TextField({
            fieldLabel:this.businessPerson,
            name:'textname',
            anchor:'90%',
            allowBlank:false,
            readOnly:true
        });
        this.memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo',
            name:'memo',
            height:35,
            anchor:'90%',
            maxLength:2048
        });
        chkcurrencyload();
        Wtf.currencyStore.on("load",function(store){
            if(store.getCount()<=1){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.currency")],2);
            }
         },this);
         
         var currencyHelp = WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.SelectcurrencytodisplayInvoicesforselectedcurrencyand")+ ((this.isCustomer)? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.agedPay.ven")));
        
         this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.currency") + currencyHelp,  //'Currency',
            hiddenName:'currencyid',
            anchor: '90%',
            allowBlank:false,
            store:Wtf.currencyStore,
//            disabled:true,
            valueField:'currencyid',
         //   emptyText:'Please select Currency...',
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true,
            listeners:{
                'select':{
                    fn:this.getCurrencySymbol,
                    scope:this
                }
            }
        });
       
//        this.Currency.on("select", this.loadGridDataWithCurrency, this);
    },
   extendComponent:function(){
    this.ExternalCurrencyRate=new Wtf.form.NumberField({
        allowNegative:false,
        hidden:this.isOrder,
        hideLabel:this.isOrder,
        readOnly:true,
        emptyText:WtfGlobal.getLocaleText("acc.field.SystemgeneratedRateApplied"),
        maxLength: 10,
        anchor:'70%',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ExternalCurrencyRate"),
        name:'externalcurrencyrate',
         listeners:{
            'change':{
                fn:this.updateFormCurrency,
                scope:this
            }
        }
    });
    
     this.tplSummary=new Wtf.XTemplate(
                '<div class="currency-view">',
                '<table width="100%">',
                '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
                '<tr><td><b>+ Term: </b></td><td align=right>{termtotal}</td></tr>',
                '</table>',
                '<hr class="templineview">',
                '<hr class="templineview">',
                '<table width="100%">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmt")+' </b></td><td align=right>{aftertaxamt}</td></tr>',
                '</table>',
                '<table width="100%">',
                '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase")+' </b></td><td align=right>{totalAmtInBase}</td></tr>',
                '</table>',
                '<hr class="templineview">',
                 '</table>',
                '<hr class="templineview">',
                '</tpl>',
                '<hr class="templineview">',
                '</div>'
            );
        this.southCalTemp=new Wtf.Panel({
            region:'east',
            width:300,
            border:false,
            baseCls:'tempbackgroundview',
            html:this.tplSummary.apply({subtotal:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),termtotal:WtfGlobal.currencyRenderer(0)})
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
       this.custForm=new Wtf.form.FormPanel({
            region : "north",
            height:70,
            id:"northForm"+this.id,
            border:false,
            disabledClass:"newtripcmbss",
            disable:this.readOnly,
            style:'padding:10px 0px 0px 10px',
            layout:'form',          
            labelWidth:90,
               items:[{
                layout:'column',
                border:false,
                defaults:{border:false,layout:'form',columnWidth:0.25},
                items:[{layout:'form',
                            columnWidth:0.43,
                    items:[this.name]
                },{layout:'form',
                            columnWidth:0.43,
                    items:[this.Currency]
//                },{layout:'form',
//                            columnWidth:0.33,
//                    items:[this.ExternalCurrencyRate]

                }]
            }, this.tagsFieldset]
       });this.invGrGrid = new Wtf.account.OSDetailGrid({
//            region:'north',
            layout : "fit",
            height:175,
//            style:'padding:20px 0px 20px 0px',
            isNote:true,
            disabledClass:"newtripcmbss",
            disable:this.readOnly,
            border:false,
            sm:new Wtf.grid.CheckboxSelectionModel({singleSelect:true}),
            isReceipt:this.isCN,
            viewConfig:{emptyText:"<div class='grid-empty-text'>"+(this.isCN?WtfGlobal.getLocaleText("acc.rem.121"):WtfGlobal.getLocaleText("acc.rem.122"))+"</div>"},
            forceFit:true,
            closable: true
        });
//        if(this.isCustBill){
//            this.pdGrid=new Wtf.account.BillingProductDetailsGrid({
////                region:'center',
//                layout:'fit',
//                height:325,
//                title:WtfGlobal.getLocaleText("acc.invoiceList.expand.pListNonInv"),  //"Product List",
//                autoScroll:true,
//                border:false,
//                disabledClass:"newtripcmbss",
//                disable:this.readOnly,
//                viewConfig:{forceFit:true},
//                isCustomer:this.isCustomer,
//                isCustBill:this.isCustBill,
//                sm:new Wtf.grid.CheckboxSelectionModel(),
//                isNote:true,
//                isCN:this.isCN,
//                currencyid:this.Currency.getValue(),
//                forceFit:true,
//                loadMask : true
//            });
//        }else
        {
//            if(Optimized_CompanyIds.indexOf(companyid)!= -1){   // optimized for lowercase
//                this.pdGrid=new Wtf.account.ProductDetailsGridOptimized({
//    //                region:'center',
//                    title:WtfGlobal.getLocaleText("acc.cnList.prodList"),  //"Product List",
//                    autoScroll:true,
//                    border:false,
//                    height:325,
//                    disabledClass:"newtripcmbss",
//                    disable:this.readOnly,
//                    sm:new Wtf.grid.CheckboxSelectionModel(),
//                    isNote:true,
//                    isCN:this.isCN,
//                    layout:'fit',
//                    viewConfig:{forceFit:true},
//                    loadMask : true
//                });
//            }else
            {
                this.pdGrid=new Wtf.account.ProductDetailsGrid({
    //                region:'center',
                    title:WtfGlobal.getLocaleText("acc.cnList.prodList"),  //"Product List",
                    autoScroll:true,
                    disabledClass:"newtripcmbss",
                    disable:this.readOnly,
                    border:false,
                    height:325,
                    sm:new Wtf.grid.CheckboxSelectionModel(),
                    isNote:true,
                    isCN:this.isCN,
                    layout:'fit',
                    viewConfig:{forceFit:true},
                    loadMask : true
                });
            }
        }

        this.northForm=new Wtf.form.FormPanel({
            region : "center",
            autoHeight:true,
            border:false,
            layout:'form',
            style: "padding:10px",
            labelWidth:90,
            disabledClass:"newtripcmbss",
            disable:this.readOnly,
            items:[{
                layout:'column',
                border:false,
                defaults:{border:false,layout:'form',columnWidth:0.32},
                items:[{
                    items:[this.sequenceFormatCombobox,this.no]
                },{
                    items:[this.creationDate]
//                },{
//                    items:[this.textBoxName]
                },{
                    items:[this.memo]
                }]
            }]
        });

    },
    callCustomer:function(isEdit,rec,winid,isCustomer){
         callBusinessContactWindow(isEdit, rec, winid, isCustomer);
         var tabid=isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
         Wtf.getCmp(tabid).on('update', function(){
            this.isCN?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
        }, this);
    },

//    updateData:function(){
//        var customer=this.name.getValue();
//        Wtf.Ajax.requestEx({
//            url:"ACC"+this.businessPerson+"/getAddress.do",
////            url:Wtf.req.account+this.businessPerson+'Manager.jsp',
//            params:{
//                mode:4,
//                customerid:customer
//            }
//        }, this,this.setCurrency);
//    },
//    setCurrency:function(response){
//        if(response.success){
//            this.Currency.setValue(response.currencyid);
//            this.setInvoiceCurrencySymbol() ;
//        }
//    },
   
    getCurrencySymbol:function(){
//        Wtf.currencyStore.clearFilter(true)
//         var index=null;
//        this.currencyStore.clearFilter(true)
//        var FIND = this.Currency.getValue();
//        index=this.currencyStore.findBy( function(rec){
//             var parentname=rec.data['currencyid'];
//            if(parentname==FIND)
//                return true;
//             else
//                return false
//            })
//
// //      var rec =Wtf.currencyStore.find('currencyid',this.Currency.getValue());
//       this.currencyid=this.Currency.getValue();
//       if(index>=0)
//            var symbol=  Wtf.currencyStore.getAt(rec).data['symbol'];
       
       this.invGrGrid.getStore().load({
            params:{
                accid:this.name.getValue(),
                mode:(this.isCustBill?16:12), 
                currencyfilterfortrans : this.Currency.getValue(),
                notlinkCNFromInvoiceFlag:this.isCN?true:false// to filter opening balance invoice in case of CN. opening balance invoice will not be shown in CI List for credit note creation except in CN otherwise case.
                }
            });
       var currentRec=WtfGlobal.searchRecord(this.Currency.store, this.Currency.getValue(), 'currencyid');
       this.symbol=currentRec!=null?currentRec.get('symbol'):"";       
       var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
       this.tplSummary.overwrite(this.southCalTemp.body,{
                subtotal:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),
                termtotal:calTermTotal,
                aftertaxamt:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),
                totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),
                amountdue:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol())
            });
//        this.invGrGrid.getStore().on('load',this.updateData,this) 
//       return symbol;
    },
    setInvoiceCurrencySymbol:function(){
        this.pdGrid.getStore().removeAll();
        var rec =WtfGlobal.searchRecordIndex(Wtf.currencyStore, this.Currency.getValue(), 'currencyid');
        if(rec>=0){
            var symbol=  Wtf.currencyStore.getAt(rec).data['symbol'];
            this.invGrGrid.setCurrencyid(this.Currency.getValue(),symbol,rec);
       }
    },
    setProductCurrencySymbol:function(store){
       var rec =WtfGlobal.searchRecordIndex(Wtf.currencyStore, this.Currency.getValue(), 'currencyid');
       if(rec>=0){
            var rate=  Wtf.currencyStore.getAt(rec).data['exchangerate'];
            var symbol=  Wtf.currencyStore.getAt(rec).data['symbol'];
            this.pdGrid.setCurrencyid(this.currencyid,rate,symbol,rec);
       }
    },

    setType:function(store){
        store.each(function(rec){
            rec.set('typeid',0);
            rec.set('discamount',0);
        },this);
        this.setProductCurrencySymbol(store);
    },
    loadGridData:function(a,rec){
        var recData = rec.data;
        this.pdGrid.getStore().removeAll();
//        this.invGrGrid.getStore().load({params:{accid:recData.accid,mode:(this.isCustBill?16:12)}});
        this.Currency.setValue(recData.currencyid);
        this.invGrGrid.getStore().proxy.conn.url = this.isCN ? (this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getInvoices.do") : (this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getGoodsReceipts.do");
        this.invGrGrid.getStore().load({
        params:{
            accid:recData.accid,
            mode:(this.isCustBill?16:12), 
            currencyfilterfortrans : recData.currencyid, 
            fullPaidFlag : this.cntype==3?true:false, 
            ignorezero : this.cntype==3?false:true,
            notlinkCNFromInvoiceFlag:this.isCN?true:false// to filter opening balance invoice in case of CN. opening balance invoice will not be shown in CI List for credit note creation except in CN otherwise case.
            }
        });
        this.invGrGrid.getStore().on('load',this.updateData,this) 
        var val
        if(this.isCN)
            val=(Wtf.customerAccStore.getAt(Wtf.customerAccStore.find('accid',recData.accid))).data['accname'];
        else
            val=(Wtf.vendorAccStore.getAt(Wtf.vendorAccStore.find('accid',recData.accid))).data['accname'];
        this.textBoxName.setValue(val);
        
       var currentRec=WtfGlobal.searchRecord(this.Currency.store, this.Currency.getValue(), 'currencyid');
       this.symbol=currentRec!=null?currentRec.get('symbol'):"";       
       var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
       this.tplSummary.overwrite(this.southCalTemp.body,{
                subtotal:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),
                termtotal:calTermTotal,
                aftertaxamt:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),
                totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),
                amountdue:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol())
            });
    },
     checkAmountDue:function(a,val){
        var  pdamountdue=0;
        var selModel=this.invGrGrid.getSelectionModel();
        var len=this.invGrGrid.getStore().getCount();
        for(var i=0;i<len;i++){
            pdamountdue=0;
            if(selModel.isSelected(i)){
                var invid=this.invGrGrid.getStore().getAt(i).data['billid'];
                var invamountdue= this.invGrGrid.getStore().getAt(i).data['amountdue'];
                this.invdiscount= this.invGrGrid.getStore().getAt(i).data['discount'] - this.invGrGrid.getStore().getAt(i).data['deductDiscount'];
                var FIND =invid;
               
                var pdindex=this.pdGrid.getStore().findBy( function(rec){
                    var billid=rec.data['billid'];
               
                     if(billid==FIND){
                        pdamountdue=+rec.data['discamount'];
                        return true;
                    }
                     else
                       return false

                })
                if(invamountdue<pdamountdue && this.invdiscount != 0)
                    return false;
                if(invamountdue<pdamountdue)
                    return true;
            }
        }
        this.updateSubtotal();
        return false;
    },
    checkAmount:function(){//Used to check sum of entered amount is not greater than invoice paid amount for cn type = 3
        var  pdamountdue=0;
        var selModel=this.invGrGrid.getSelectionModel();
        var len=this.invGrGrid.getStore().getCount();
        for(var i=0;i<len;i++){
            pdamountdue=0;
            if(selModel.isSelected(i)){
                var invid=this.invGrGrid.getStore().getAt(i).data['billid'];
                var invamount= this.invGrGrid.getStore().getAt(i).data['amount'];
                this.invdiscount= this.invGrGrid.getStore().getAt(i).data['discount'] - this.invGrGrid.getStore().getAt(i).data['deductDiscount'];
                var FIND =invid;
               
                var pdindex=this.pdGrid.getStore().findBy( function(rec){
                    var billid=rec.data['billid'];
               
                     if(billid==FIND){
                        pdamountdue=+rec.data['discamount'];
                        return true;
                    }
                     else
                       return false

                })
                if(invamount<pdamountdue && this.invdiscount != 0)
                    return false;
                if(invamount<pdamountdue)
                    return true;
            }
        }

        return false;
    },
    getSelectedRow:function(a,index,rec){
        var arr=[];
        this.costCenterId = "";
        var selModel=this.invGrGrid.getSelectionModel();
        if(rec.data.externalcurrencyrate!=undefined)
            this.externalcurrencyrate=rec.data.externalcurrencyrate;
     //   this.ExternalCurrencyRate.setValue(this.externalcurrencyrate==0?"":this.externalcurrencyrate);
        var len=this.invGrGrid.getStore().getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
                arr.push(this.invGrGrid.getStore().getAt(i).data['billid']);
                this.costCenterId = this.invGrGrid.getStore().getAt(i).data['costcenterid'];
            }
        }
        if(arr.length==0){
            this.pdGrid.getStore().removeAll();
            return;
        }
        var url = ((this.isCN)?(this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":'ACCInvoiceCMN/getInvoiceRows.do'):(this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceiptRows.do":'ACCGoodsReceiptCMN/getGoodsReceiptRows.do'))
        this.pdGrid.getStore().proxy.conn.url = url;
        this.pdGrid.getStore().load({params:{
           bills:arr,
           mode:(this.isCustBill?17:14)
       }})
      this.termStore.reload();
    },
    save:function(){
        this.no.setValue(this.no.getValue().trim());
        if(this.name.getValue()==""){
            this.name.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
            return;
        }
         var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(this.northForm.getForm().isValid()  && this.name.getValue()!="" && isValidCustomFields){
            var details=this.pdGrid.getCMProductDetails();
            if(details=="[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cn.8")], 2);
                return;
            }
            if(details=="Error"){               
                return;
            }
            var store=this.pdGrid.getStore();
            if(this.cntype == "3") {
                var checkamount=this.checkAmount();
                 if(checkamount){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cn.10")], 2);
                    return;
                }
            } else {
                var checkamountdue=this.checkAmountDue();
                 if(checkamountdue){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cn.3")], 2);
                    return;
                }
            }
            var iszero=this.pdGrid.isAmountzero(store);
            if(iszero){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (this.isCN?WtfGlobal.getLocaleText("acc.cn.6"):WtfGlobal.getLocaleText("acc.cn.7"))], 2);
                return;
            }
            
            if(!this.isCustBill && (Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel)){
                
                var validstore=WtfGlobal.isValidInventoryInfo(this.pdGrid.getStore(),'invstore');
                if(!validstore){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselectvalidinventorystore")], 2);
                    return;
                }
                
                var validloc=WtfGlobal.isValidInventoryInfo(this.pdGrid.getStore(),'invlocation');
                if(!validloc){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselectvalidinventorylocation")], 2);
                    return;
                }
            }
            
            if(this.invGrGrid.getSelectionModel().getSelected().data.date>this.creationDate.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (this.isCN?WtfGlobal.getLocaleText("acc.cn.5"):WtfGlobal.getLocaleText("acc.cn.4"))], 2);
                return;
            }

            var includetax=false;
            
            if(this.invdiscount > 0) {
            
		            Wtf.Msg.show({
		                title: WtfGlobal.getLocaleText("acc.nee.64"),  //'Enter Discount Value',
		                width:400,
		                msg: WtfGlobal.getLocaleText("acc.nee.65"),  //"Invoice contains Discount. Please Enter Amount of Discount you want to consider for the Note.",
		                value:this.invdiscount,
		                buttons:{ok:WtfGlobal.getLocaleText("acc.common.saveBtn"),cancel:WtfGlobal.getLocaleText("acc.common.cancelBtn")},
		                prompt:true,
		                scope:this,
		                fn:function(btn, txt) {if(btn=="ok"){
		                	
		                	if(!isNaN(txt) && txt <= this.invdiscount)
		                		this.totalInvoiceDiscount = txt;
		                	else
		                		this.totalInvoiceDiscount = 0;
		            
		            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.cn.at"),WtfGlobal.getLocaleText("acc.cn.1"),function(btn){
		                if(btn=="yes") {includetax=true;}
		
		            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.cn.2"),function(btn){
		                if(btn!="yes") {return;}
		                this.msg= WtfComMsgBox(27,4,true);
		                var rec=this.northForm.getForm().getValues();
		                rec.productdetails=details;
                                var custFieldArr=this.tagsFieldset.createFieldValuesArray();
                                if (custFieldArr.length > 0)
                                    rec.customfield = JSON.stringify(custFieldArr);
		                rec.includetax=includetax;
		                rec.externalcurrencyrate=this.externalcurrencyrate;
		                rec.exchangeratefortransaction=this.externalcurrencyrate;
		                rec.currencyid=this.Currency.getValue()
		                rec.mode=this.isCN?(this.isCustBill?61:26):(this.isCustBill?61:27);
		                rec.creationdate=WtfGlobal.convertToGenericDate(this.creationDate.getValue());
		                rec.accid=this.name.getValue();
                                if(this.invGrGrid.getSelectionModel().getSelected())
                                    rec.accountid=this.invGrGrid.getSelectionModel().getSelected().data.accountid;
		                
		                rec.costCenterId=this.costCenterId;
		                rec.totalInvoiceDiscount=this.totalInvoiceDiscount;
                                rec.cntype=this.cntype;
                                rec.invoicetermsmap = this.getInvoiceTermDetails();
                                rec.number = this.no.getValue();
                                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
		                this.ajxUrl = "";
		                if(this.isCN){
		                    this.ajxUrl = this.isCustBill?"ACCCreditNote/saveBillingCreditNote.do":"ACCCreditNote/saveCreditNote.do";
		                }else{
		                    this.ajxUrl = this.isCustBill?"ACCDebitNote/saveBillingDebitNote.do":"ACCDebitNote/saveDebitNote.do";
		                }
		                Wtf.Ajax.requestEx({
		                    url:this.ajxUrl,
		//                    url: Wtf.req.account+(this.isCN?'CustomerManager.jsp':'VendorManager.jsp'),
		                    params: rec
		                },this,this.genSuccessResponse,this.genFailureResponse);
		            },this);
		             },this);
		                
		                }}});
            
            } else {
            	
		            	this.totalInvoiceDiscount = 0;
			            
			            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.cn.at"),WtfGlobal.getLocaleText("acc.cn.1"),function(btn){
			                if(btn=="yes") {includetax=true;}
			
			            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.cn.2"),function(btn){
			                if(btn!="yes") {return;}
			                this.msg= WtfComMsgBox(27,4,true);
			                var rec=this.northForm.getForm().getValues();
			                rec.productdetails=details;
                                        var custFieldArr = this.tagsFieldset.createFieldValuesArray();
                                        if (custFieldArr.length > 0)
                                            rec.customfield = JSON.stringify(custFieldArr);
			                rec.includetax=includetax;
			                rec.externalcurrencyrate=this.externalcurrencyrate;
                                        rec.exchangeratefortransaction=this.externalcurrencyrate;
			                rec.currencyid=this.Currency.getValue()
			                rec.mode=this.isCN?(this.isCustBill?61:26):(this.isCustBill?61:27);
			                rec.creationdate=WtfGlobal.convertToGenericDate(this.creationDate.getValue());
			                rec.accid=this.name.getValue();
                                        if(this.invGrGrid.getSelectionModel().getSelected())
                                            rec.accountid=this.invGrGrid.getSelectionModel().getSelected().data.accountid;
			                rec.costCenterId=this.costCenterId;
			                rec.totalInvoiceDiscount=this.totalInvoiceDiscount;
                                        rec.cntype=this.cntype;
                                        rec.invoicetermsmap = this.getInvoiceTermDetails();
                                        rec.number = this.no.getValue();
                                        var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                                        rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
			                this.ajxUrl = "";
			                if(this.isCN){
			                    this.ajxUrl = this.isCustBill?"ACCCreditNote/saveBillingCreditNote.do":"ACCCreditNote/saveCreditNote.do";
			                }else{
			                    this.ajxUrl = this.isCustBill?"ACCDebitNote/saveBillingDebitNote.do":"ACCDebitNote/saveDebitNote.do";
			                }
			                Wtf.Ajax.requestEx({
			                    url:this.ajxUrl,
			//                    url: Wtf.req.account+(this.isCN?'CustomerManager.jsp':'VendorManager.jsp'),
			                    params: rec
			                },this,this.genSuccessResponse,this.genFailureResponse);
			            },this);
			             },this);
			                
            }
            
        }else
            WtfComMsgBox(2,2);
    },
    genSuccessResponse:function(response){
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
        if(response.success){
            this.northForm.getForm().reset();
            this.invGrGrid.getStore().removeAll();
            this.pdGrid.getStore().removeAll();
            this.name.setValue("");
            this.sequenceFormatStore.load();
//            this.invGrGrid.getView().emptyText="<div class='grid-empty-text'>Please select the account name.</div>"; //BUG FIXED:16231
            this.invGrGrid.getView().refresh();
            Wtf.dirtyStore.product = true; //To reload product list on activate Product List.
            
             var customFieldArray = this.tagsFieldset.customFieldArray;
            for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                var fieldId = customFieldArray[itemcnt].id
                if (Wtf.getCmp(fieldId) != undefined) {
                       Wtf.getCmp(fieldId).clearValue();
                }
            }  
            this.blankTplSummery();
        }
        if(response.reloadInventory){
            Wtf.productStore.reload();
            Wtf.productStoreSales.reload();
        }
        this.setTrNoteNumber();
    },
    getCurrencySymbolExchange:function(){
        var index=null;
//        this.Currency.store.clearFilter(true); //ERP-9962
        var FIND = this.Currency.getValue();
        if(FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index=this.Currency.store.findBy( function(rec){
             var parentname=rec.data['currencyid'];
            if(parentname==FIND)
                return true;
             else
                return false
            });
       this.currencyid=this.Currency.getValue();
       return index;
    },
    getExchangeRate:function(){
        var index=this.getCurrencySymbolExchange();
        var rate=this.externalcurrencyrate;
         var revExchangeRate = 0;
        if(index>=0){
            var exchangeRate = this.Currency.store.getAt(index).data.exchangerate;
            if(this.externalcurrencyrate>0) {
                exchangeRate = this.externalcurrencyrate;
            }
            revExchangeRate = 1/(exchangeRate);
            revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        }
        return revExchangeRate;
    },
    updateSubtotal:function(){
        var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
        var totalSubtotal=0;
        var count=this.pdGrid.getStore().getCount();
        var store = this.pdGrid.getStore();
        var selectedRows=this.pdGrid.getSelectionModel().selections;
        for(var i=0;i<selectedRows.length;i++){
            var total=selectedRows.items[i].data.discamount;
            totalSubtotal+=total;
        }
        var totalWithTerms=totalSubtotal+this.findTermsTotal();

        var totalWithTermsInBase=totalSubtotal+this.findTermsTotal();
        if(this.getExchangeRate()!=0)
        {
            totalWithTermsInBase*=this.getExchangeRate();
        }
        this.tplSummary.overwrite(this.southCalTemp.body,{
                subtotal:WtfGlobal.addCurrencySymbolOnly(totalSubtotal,this.symbol),
                termtotal:calTermTotal,
                aftertaxamt:WtfGlobal.addCurrencySymbolOnly(totalWithTerms,this.symbol),
                totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(totalWithTermsInBase,WtfGlobal.getCurrencySymbol()),
                amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())
            });

    },
    blankTplSummery:function(){
        var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
         this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),termtotal:calTermTotal,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(0,WtfGlobal.getCurrencySymbol())});
         
    },
    genFailureResponse:function(response){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    getNextSequenceNumber:function(a,val){
       if(!(a.getValue()=="NA")){
        WtfGlobal.hideFormElement(this.no);
        this.setTrNoteNumber(true); 
        var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumber,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                WtfGlobal.showFormElement(this.no);
                this.no.reset();
                this.no.enable();
            }else {
                this.no.setValue(resp.data);
                this.no.disable();
                WtfGlobal.hideFormElement(this.no);
            }
            
        });
       } else {
           WtfGlobal.showFormElement(this.no);
           this.no.reset();
           this.no.enable();
       }
    },
    setTrNoteNumber:function(isSelectNoFromCombo){
        var format="";var temp2="";
        var val=this.isCN*1+this.isCustBill*10;
        switch(val){
            case 0:format=Wtf.account.companyAccountPref.autodebitnote;
                temp2=Wtf.autoNum.DebitNote;
                break;
            case 1:format=Wtf.account.companyAccountPref.autocreditmemo;
                temp2=Wtf.autoNum.CreditNote;
                break;
            case 10:format=Wtf.account.companyAccountPref.autobillingdebitnote;
                temp2=Wtf.autoNum.BillingDebitNote;
                break;
            case 11:format=Wtf.account.companyAccountPref.autobillingcreditmemo;
                temp2=Wtf.autoNum.BillingCreditNote;
                break;
        }
        if(isSelectNoFromCombo){
            this.fromnumber = temp2;
        } else if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.no.setValue(resp.data)}, this);
        }
    },setTermValues : function(termDetails)  {
        if(termDetails!=""&&termDetails!=null&&termDetails!=undefined){
        var detailArr = eval(termDetails);
        for(var cnt=0; cnt<detailArr.length; cnt++ ){
            var jObj = detailArr[cnt];
            
            var record = this.termStore.queryBy(function(record){
                return (record.get('id') == jObj.id);
            }, this).items[0];
            if(record) {
                record.set('termamount',jObj.termamount);
                record.set('termpercentage',jObj.termpercentage);
            }
        }
        }
    },

    addInvoiceTermGrid : function() {
        this.termcm=[{
            header: WtfGlobal.getLocaleText("acc.field.Term"),
            dataIndex: 'term'
        },{
            header: WtfGlobal.getLocaleText("acc.field.Percentage"),
            dataIndex: 'termpercentage',
            editor:new Wtf.form.NumberField({
                xtype : "numberfield", 
                maxLength : 15,
                allowNegative : false,
                minValue : 0,
                maxValue: 100,
                regexText:Wtf.MaxLengthText+"15"
            })
        },{
            header: WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
            dataIndex: 'termamount',
            renderer : function(val, meta, rec) {
                if(typeof val=='number' && val>=0 && rec.data.sign==0) {
                    rec.set('termamount',val*(-1));
                    return val*(-1)
                } else 
                    return val;
            },
            editor:new Wtf.form.NumberField({
                xtype : "numberfield", 
                maxLength : 15,
                allowNegative : true,
                regexText:Wtf.MaxLengthText+"15"
            })
        }];
        
        this.termRec =new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'term'},
        {name: 'glaccount'},
        {name: 'sign'
        },{name: 'formula'
        },{name: 'formulaids'
        },{name: 'termamount'
        },{name: 'termpercentage'
        }
        ]);
        this.termStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.termRec),
            url: 'ACCAccount/getInvoiceTermsSales.do',
            baseParams:{
                isSalesOrPurchase:this.isCustomer?true:false
        }
        });
            this.termStore.load();
            this.termStore.on('load',this.closeTermGrid,this);
        this.termgrid = new Wtf.grid.EditorGridPanel({
            region:'west',
            layout:'fit',
            clicksToEdit:1,
            store: this.termStore,
            height:100,
            width:700,
            autoScroll : true,
            disabledClass:"newtripcmbss",
            disable:this.readOnly,
            style:'padding-top:10px;',
            cm: new Wtf.grid.ColumnModel(this.termcm),
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.termgrid.on('afteredit',this.updateSubtotalOnTermChange,this);        
    },
    
    closeTermGrid : function(obj){
        var store = this.termgrid.store;
            if(this.termStore.data.length==0)
            {
                this.termgrid.hide();
            }
            
    }, calProdSubtotalWithoutDiscount:function(){
        var subtotal=0;
        var count=this.pdGrid.getStore().getCount();
        var store = this.pdGrid.getStore();
        var selectedRows=this.pdGrid.getSelectionModel().selections;
        for(var i=0;i<selectedRows.length;i++){
            var total=selectedRows.items[i].data.discamount;
            subtotal+=total;
        }
        return subtotal;
    }, 
    updateSubtotalOnTermChange : function(obj) {
        var recdata=obj.record.data;
        var store = obj.grid.store;
        var subtotal = this.calProdSubtotalWithoutDiscount();
        var formula = recdata.formulaids.split(",");
        var termtotal = 0;
        for(var cnt=0; cnt<formula.length; cnt++){
            if(formula[cnt]=='Basic') {
                termtotal +=(subtotal);
            }
            var record = store.queryBy(function(record){
                return (record.get('id') == formula[cnt]);
            }, this).items[0];
            if(record && (typeof record.data.termamount=='number')) {
//                termtotal +=(record.data.termamount*(record.data.sign==1 ? 1 : -1 ));
                  termtotal +=(record.data.termamount);
            }
        }
        if(obj.field=="termpercentage"){
            var opmod = recdata.sign==0 ? -1 : 1;
            var this_termTotal = ((Math.abs(termtotal) * recdata.termpercentage*1) / 100)*opmod;
            obj.record.set('termamount',this_termTotal);
        } 
        this.updateSubtotal();
    },
    
    findTermsTotal : function() {
        var termTotal = 0;
        if(this.termgrid) {
            var store = this.termgrid.store;
            var totalCnt = store.getCount();
            for(var cnt=0; cnt<totalCnt; cnt++) {
                var lineAmt = store.getAt(cnt).data.termamount;
                if(typeof lineAmt=='number')
                    termTotal += lineAmt;
            }
        }
        return termTotal;
    },
    
    getInvoiceTermDetails : function() {
        var arr=[];
        if(this.termgrid) {
            var store = this.termgrid.store;
            store.each(function(rec){
                var lineAmt = rec.data.termamount;
                if(typeof lineAmt=='number' && lineAmt !=0) {
                    arr.push(store.indexOf(rec));
                }            
            }, this);
            return WtfGlobal.getJSONArray(this.termgrid,true,arr)
        }
    }
}); 
//Credit Note / Debit Note Window

Wtf.account.CreditNoteWindow = function(config){
    this.modeName = config.modeName;
    this.value="1",
    this.isCN=config.isCN;
    this.isCustBill=config.isCustBill;
    this.moduleid = config.moduleid;
    this.cntype = config.cntype;
    this.isReverseCNDN = (this.cntype==4?true:false);
    if(this.isCN) {
        if(this.isReverseCNDN) {
            this.customerFlag = false;
        } else {
            this.customerFlag = true;
        }
    } else {
        if(this.isReverseCNDN) {
            this.customerFlag = true;
        } else {
            this.customerFlag = false;
        }
    }
    this.businessPerson=(this.customerFlag?"Customer":"Vendor");
    this.externalcurrencyrate=0;
    this.noteType=config.isCN?'Credit Note':'Debit Note';
    this.custPermType=config.isCN?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCN?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.isCustomer=config.isCN;
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });

    Wtf.apply(this,{
         buttons: this.butnArr
    },config);
    Wtf.account.CreditNoteWindow.superclass.constructor.call(this, config);
//     this.addEvents({
//        'update':true
//    });
}

Wtf.extend(Wtf.account.CreditNoteWindow, Wtf.Window, {
    draggable:false,
    onRender: function(config){
        Wtf.account.CreditNoteWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        this.createForm();
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.store);
        this.addGridRec();
       var title=this.noteType;//this.isCN?WtfGlobal.getLocaleText("acc.cn.payType"):WtfGlobal.getLocaleText("acc.dn.payType");
       var msg=this.title;//this.isCN?WtfGlobal.getLocaleText("acc.cn.sel"):WtfGlobal.getLocaleText("acc.dn.sel");
       var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        }, this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                id: 'centerpan'+this.id,
                autoScroll:true,
                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'fit',
                items:[this.TypeForm,this.grid]
            }))
    },
    createDisplayGrid:function(){
        this.accRec = Wtf.data.Record.create([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        },

        {
            name:'acccode'
        },

        {
            name:'groupname'
        }
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
//                group:[1,2,3,4,5,6,7,8,11,12,14,15,19,20,21,22],
              ignorecustomers:true,  
              ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.accountStore.load();
        var mulDebitCMArr = [];      
        mulDebitCMArr.push({
            header:WtfGlobal.getLocaleText("acc.invoice.gridaccount"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.217") : WtfGlobal.getLocaleText("acc.rem.30"),  //"Debit Account",
            dataIndex:'accountid',
            width:150,
            hidden : this.isAdvPayment,
            editor: this.cmbAccount=new Wtf.form.ExtFnComboBox({
                hiddenName:'accountid',
                store:this.accountStore,
                minChars:1,
                listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
                valueField:'accountid',
                displayField:'accountname',
                forceSelection:true,
                hirarchical:true,
                extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                mode: 'local',
                typeAheadDelay:30000,
                extraComparisionField:'acccode'
            }),
            renderer:Wtf.comboBoxRenderer(this.cmbAccount)
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridAmount"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.218") : WtfGlobal.getLocaleText("acc.rem.31"),  //"Debit Amount",
            dataIndex:'dramount',
            width:100,
            align:'right',
            summaryType:'sum',
            editor:new Wtf.form.NumberField({
                allowBlank: false,
                allowNegative:false
            })
        },{
            header:WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
            dataIndex:"description",
            width:100,
            editor:this.Description=new Wtf.form.TextArea({
                maxLength:255,
                xtype:'textarea'
            })
        });
        mulDebitCMArr = WtfGlobal.appendCustomColumn(mulDebitCMArr,GlobalColumnModel[this.moduleid]);
         mulDebitCMArr.push({
            header: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            dataIndex:"delete",
            align:'center',
            renderer:function(){
                return "<div class='pwnd delete-gridrow' > </div>";
            }
        });
        this.mulDebitCM = new Wtf.grid.ColumnModel(mulDebitCMArr);
//        this.mulDebitCM= new Wtf.grid.ColumnModel([ ]);
       
       
        this.ccRec = new Wtf.data.Record.create([
        {
            name: 'accountid'
        },

        {
            name: 'dramount'
        },
        {
            name:'customfield'
        },
        {
            name: 'description'
        }
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.ccRec)
        });
       
        this.grid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
//            layout:'fit',
            clicksToEdit:1,
            height:130,
            width:'97%',
            store: this.store,
            cm: this.mulDebitCM,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('afteredit',this.addGridRec,this);
        this.grid.on('beforeedit',this.checkrecord,this);
        
       
    },
   
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    getAccountDetailsDetails:function(){
        var arr=[];
        this.store.each(function(rec){
              rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
              arr.push(this.store.indexOf(rec));
        }, this);
        var jarray=WtfGlobal.getJSONArray(this.grid,false,arr);
        return jarray;
    },

    processRow:function(grid,rowindex,e){        
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
               
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addGridRec();
                }
            }, this);
        }
    },
    checkrecord:function(obj){
        if(this.istax){
            var idx = this.grid.getStore().find("taxid", obj.record.data["taxid"]);
            if(idx>=0)
                obj.cancel=true;
        }
    },
    addGridRec:function(e){ 
        var size=this.store.getCount();
        
        var rec= this.accRec;
        var size=this.store.getCount();
        if(size>0){
            var lastRec=this.store.getAt(size-1);
            if(lastRec.get('accountid') == ''){
                lastRec.set('accountid', '');
                return;
            }
            if(lastRec.get('dramount') == ''){
                lastRec.set('dramount', '');
                return;
            }
        } 
        rec = new rec({});
        rec.beginEdit();
        var fields=this.store.fields;
        for(var x=0;x<fields.items.length;x++){
            var value="";
            rec.set(fields.get(x).name, value);
        }      
        rec.endEdit();
        rec.commit();
        this.store.add(rec);
    },
    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.grid,false,arr);
    },        
    createForm:function(){              
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
//            compId:this.id,
            autoHeight: true,
            autoWidth:true,
            parentcompId:this.id,
            moduleid:this.moduleid,
            isWindow:true,
            widthVal:90,
            isEdit: false,//this.isEdit,
            record: undefined//this.record
        });
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
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName
            }
        });
          this.sequenceFormatStore.on('load',function(){
            if(this.sequenceFormatStore.getCount()>0){
                var seqRec=this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                   
                }
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
             }
         },this);
     this.sequenceFormatStore.load();
     
     this.sequenceFormatCombobox = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            valueField:'id',
            displayField:'value',
            store:this.sequenceFormatStore,
            disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
//            width:240,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'sequenceformat',
            hiddenName:'sequenceformat',
            listeners:{
                'select':{
                    fn:this.getNextSequenceNumber,
                    scope:this
                }
            }

        });
        this.no=new Wtf.form.TextField({
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN")) +" "+ WtfGlobal.getLocaleText("acc.cn.9") + "*",  //this.noteType+' Note No*',
            name: 'number',
            scope:this,
            maxLength:45,
            anchor:'90%',
            allowBlank:false
        });
        this.setTrNoteNumber();
        this.customerFlag?chkcustaccload():chkvenaccload();
        this.name=new  Wtf.form.ExtFnComboBox({
            fieldLabel:(this.customerFlag?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            id:"selectCustomer"+this.helpmodeid,
            hiddenName:'accid',
            store:this.customerFlag?Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            minChars:1,
            extraComparisionField:'acccode',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth : Wtf.account.companyAccountPref.accountsWithCode?350:240,
            mode: 'local',
            typeAheadDelay:30000,
            scope:this,
            allowBlank:false,
            anchor:'90%',
            displayField:'accname',
//            allowBlank:false, //Checked at a time of saving
            emptyText:this.customerFlag?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven"),  //'Please Select a '+this.businessPerson+'...',
            forceSelection: true,
            hirarchical:true
//            addNewFn:this.callCustomer.createDelegate(this,[false, null,'custwin',this.isCN])
        });
        
       if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create)) {
            this.name.addNewFn=this.callCustomer.createDelegate(this,[false, null,'custwin',this.isCN]);
       }
        this.creationDate=new Wtf.form.DateField({
             xtype:'datefield',
             name:'creationdate',
             allowBlank:false,
             anchor:'90%',
             format:WtfGlobal.getOnlyDateFormat(),
             value:Wtf.serverDate,
             fieldLabel:WtfGlobal.getLocaleText("acc.customer.date") + "*"  //'Creation Date*'
        });        
        this.memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo',
            name:'memo',
            hidden:(this.cntype==2 || this.cntype==4),
            hideLabel:(this.cntype==2 || this.cntype==4),
            height:35,
            anchor:'90%',
            maxLength:2048
        });
        chkcurrencyload();
        Wtf.currencyStore.on("load",function(store){
            if(store.getCount()<=1){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please set Currency Exchange Rates"],2);
            }
         },this);
        
         this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.customer.currency") /*+ currencyHelp*/,  //'Currency',
            hiddenName:'currencyid',
            anchor: '90%',
            allowBlank:false,
            store:Wtf.currencyStore,
//            disabled:true,
            valueField:'currencyid',
         //   emptyText:'Please select Currency...',
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });
        this.Amount=new Wtf.form.NumberField({
            name:"amount",
            allowBlank:(this.cntype==2 || this.cntype==4),
            fieldLabel:WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
            id:"amount"+this.id,
            maxLength:15,
            hidden:(this.cntype==2 || this.cntype==4),
            hideLabel:(this.cntype==2 || this.cntype==4),
            decimalPrecision:2,
//            disabled:true,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
            anchor:'90%'
        });
        
        this.name.on('select',function(){
            if(this.name.getValue() != "") {
                var rec = WtfGlobal.searchRecord(this.customerFlag?Wtf.customerAccStore:Wtf.vendorAccStore, this.name.getValue(), "accid");
                this.Currency.setValue(rec.data['currencyid']);
            }
        },this);
        
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            autoHeight:true,
            labelWidth:120,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
             items:[this.sequenceFormatCombobox,this.no, this.name, this.Currency, this.creationDate, this.Amount, this.memo, this.tagsFieldset]
       });
//       this.accountType.setValue(true);
   },
   getNextSequenceNumber:function(a,val){
       if(!(a.getValue()=="NA")){
         WtfGlobal.hideFormElement(this.no);  
         this.setTrNoteNumber(true);       
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumber,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                WtfGlobal.showFormElement(this.no);
                this.no.reset();
                this.no.enable();
            }else {
                this.no.setValue(resp.data);
                this.no.disable();
                WtfGlobal.hideFormElement(this.no);
            }
            
        });
       } else {
           WtfGlobal.showFormElement(this.no);
           this.no.reset();
           this.no.enable();
       }
    },
   setTrNoteNumber:function(isSelectNoFromCombo){
        var format="";var temp2="";
        var val=this.isCN*1+this.isCustBill*10;
        switch(val){
            case 0:format=Wtf.account.companyAccountPref.autodebitnote;
                temp2=Wtf.autoNum.DebitNote;
                break;
            case 1:format=Wtf.account.companyAccountPref.autocreditmemo;
                temp2=Wtf.autoNum.CreditNote;
                break;
            case 10:format=Wtf.account.companyAccountPref.autobillingdebitnote;
                temp2=Wtf.autoNum.BillingDebitNote;
                break;
            case 11:format=Wtf.account.companyAccountPref.autobillingcreditmemo;
                temp2=Wtf.autoNum.BillingCreditNote;
                break;
        }
        if(isSelectNoFromCombo){
            this.fromnumber = temp2;
        } else if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.no.setValue(resp.data)}, this);
        }
   },
   
   callCustomer:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
            this.customerFlag?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
        }, this);
   },

   saveForm:function(){
       var isValid = this.TypeForm.form.isValid();
       var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
       if(!isValid || !isValidCustomFields){
            WtfComMsgBox(2,2);
            return;
       }
       var nameStore=this.name.store;
       var selIndex=nameStore.find("accid",this.name.getValue());
       var accountid=nameStore.getAt(selIndex).data.accountid
//        if(this.TypeForm.form.isValid()){
            if(this.isCN){
                this.ajxUrl = this.isCustBill?"ACCCreditNote/saveBillingCreditNote.do":"ACCCreditNote/saveCreditNote.do";
            }else{
                this.ajxUrl = this.isCustBill?"ACCDebitNote/saveBillingDebitNote.do":"ACCDebitNote/saveDebitNote.do";
            }
            var rec = {};
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            if (custFieldArr.length > 0){
                rec.customfield = JSON.stringify(custFieldArr);
            }
            rec.number = this.no.getValue();
            rec.accountid = accountid;
            rec.otherwise = true;
            var details=this.getAccountDetailsDetails();
            if(this.cntype==2 || this.cntype==4){ 
                if(details == undefined || details == "[]"){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg15")],2);   //"Product(s) details are not valid."
                    return;
                }
                var amount=0.0;
                var detailsObject = eval('(' + details + ')');
                for(var jsonObj=0;jsonObj<detailsObject.length;jsonObj++){
                    amount+=parseFloat(detailsObject[jsonObj].dramount);
                }
                rec.amount = amount;
                rec.details = details;
            }
            rec.otherwise = true;
            rec.creationdate = WtfGlobal.convertToGenericDate(this.creationDate.getValue());
            rec.cntype = this.cntype;
            var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
            rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
            this.TypeForm.form.submit({
                   url:this.ajxUrl,
                   params: rec,
//                   params: {
//                        otherwise:true,
//                        creationdate:WtfGlobal.convertToGenericDate(this.creationDate.getValue()),
//                        cntype:this.cntype
//                   },
                   scope:this,
                   waitMsg:WtfGlobal.getLocaleText("acc.field.Submitting"),
                   success:function(frm,action){
                       var resObj = eval("(" + action.response.responseText + ")");
                       var msg = WtfGlobal.getLocaleText("acc.field.Transactionhasbeensavedsuccessfully");
                       if(resObj.data.success) {
                            if(resObj.data.msg)msg=resObj.data.msg;
                       } else {
                           msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                           if(resObj.data.msg)msg=resObj.data.msg;
                       }
                       WtfComMsgBox([this.noteType,msg],resObj.data.success*2+1);
                       this.close();
                   },
                   failure:function(frm,action){
                       var resObj = eval("(" + action.response.responseText + ")");
                       var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                        if(resObj.data.msg)msg=resObj.msg;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                   }
            });
//        }
    },
    closeWin:function(){ /*this.fireEvent('update',this,this.value);*/this.close();}   
}); 
