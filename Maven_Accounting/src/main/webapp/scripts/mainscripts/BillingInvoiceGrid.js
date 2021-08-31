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
Wtf.account.BillingProductDetailsGrid=function(config){
    this.isCustomer=config.isCustomer;
    this.isCustBill=config.isCustBill;
    this.id=config.id;
    this.fromPO=false;
    this.moduleid = config.moduleid;
    this.editTransaction=config.editTransaction;
    this.readOnly=config.readOnly;
    this.isOrder=config.isOrder;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    if(config.isNote!=undefined)
        this.isNote=config.isNote;
    else
        this.isNote=false;
    this.isCN=config.isCN;
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[config.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    Wtf.account.BillingProductDetailsGrid.superclass.constructor.call(this,config);
    this.addEvents({
         'datachanged':true
    });
}
Wtf.extend(Wtf.account.BillingProductDetailsGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    layout:'fit',
    autoScroll:true,
    disabledClass:"newtripcmbss",
    viewConfig:{forceFit:true},
    forceFit:true,
    loadMask:true,
    onRender:function(config){
         Wtf.account.BillingProductDetailsGrid.superclass.onRender.call(this,config);
         this.on('render',this.addBlankRow,this);
         this.on('afteredit',this.updateRow,this);        
         this.on('rowclick',this.handleRowClick,this);
         if(!this.isNote && !this.readOnly && !this.editTransaction){
	         if(this.getColumnModel().getColumnById(this.id+"prtaxid").hidden == undefined && this.getColumnModel().getColumnById(this.id+"taxamount").hidden == undefined){
	        	 this.getColumnModel().setHidden(9, true);				// 21241   If statement added bcos could not use the event destroy for column model
	        	 this.getColumnModel().setHidden(10, true);							// and also could not call the createColumnModel() method from onRender
	         }
         }
     },
     createStore:function(){
        this.storeRec = Wtf.data.Record.create([
            {name:'rowid',defValue:null},
            {name:'productdetail'},
            {name:'creditoraccount'},
            {name:'billid'},
            {name:'billno'},
            {name:'quantity',defValue:1},
            {name:'rate',defValue:0},
            {name:'discamount'},
            {name:'discount'},
            {name:'prdiscount',defValue:0},
            {name:'prtaxid'},
            {name:'prtaxname'},
            {name:'prtaxpercent'},
            {name:'taxamount',defValue:0},
            {name:'calamount',defValue:0},
            {name:'discountamount',defValue:0},
            {name: 'currencysymbol',defValue:this.symbol},
            {name:'taxpercent',defValue:0},
            {name:'totalamount',defValue:0},
            {name:'discountispercent',defValue:1},
            {name:'amount',defValue:0},
            {name:'transectionno'},
            {name:'remquantity'},
            {name:'remainingquantity'},
            {name:'orignalamount'},
            {name:'typeid',defValue:0},
            {name:'isNewRecord',defValue:"1"},
            {name:'linkto'},
            {name:'linkid'},
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'customfield'},
            {name:'rowTaxAmount'},
            {name:'gridRemark'},
            {name:'accountId'}
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
              url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
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
        this.allAccountRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'acccode'},
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'level'},
            {name: 'leaf'},
            {name: 'openbalance'},
            {name: 'parentid'},
            {name: 'parentname'}
        ]);
        this.allAccountStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.allAccountRec),
    //        url: Wtf.req.account +'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
               ignoreCashAccounts:true,
               ignoreBankAccounts:true,
               ignoreGSTAccounts:true,  
               ignorecustomers:true,  
               ignorevendors:true,
               nondeleted:true
            }
        });
        this.allAccountStore.load();
    },
    createComboEditor:function(){
//        this.typeStore = new Wtf.data.SimpleStore({
//            fields: [{name:'typeid',type:'int'}, 'name'],
//            data :[[0,'Normal'],[1,'Defective']]
//        });

        this.noteTypeRec = new Wtf.data.Record.create([
           {name: 'typeid'},
           {name: 'name'},
        ]);
        this.typeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.noteTypeRec),
//            url: Wtf.req.account + 'CompanyManager.jsp',
            url: "ACCCreditNote/getNoteType.do",
            baseParams:{
                mode:31
            }
        });
        this.typeStore.load();
        this.typeEditor = new Wtf.form.ComboBox({
            store: this.typeStore,
            name:'typeid',
            displayField:'name',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            listeners: {
            afterrender: function(combo) {
                var recordSelected = combo.getStore().getAt(0);                     
                combo.setValue(recordSelected.get("typeid"));
            }
            }
        });
        
        this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'acccode'},
            {name:'groupname'}
//            {name:'level',type:'int'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.accountStore.load();
    this.cmbAccount=new Wtf.form.ExtFnComboBox({
                    hiddenName:'accountid',
                    store:this.accountStore,
                    minChars:1,
                    valueField:'accountid',
                    displayField:'accountname',
                    forceSelection:true,
                    hirarchical:true,
//                    addNewFn:this.openCOAWindow.createDelegate(this),
                    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                    mode: 'local',
                    typeAheadDelay:30000,
                    extraComparisionField:'acccode',// type ahead search on acccode as well.
                    listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:300
                });
        
        this.productEditor= new Wtf.form.TextField({
             maxLength:250
        });
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'}

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33
            }
        });
        this.taxStore.load();

        this.transTax= new Wtf.form.FnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            selectOnFocus:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit))
            this.transTax.addNewFn=this.addTax.createDelegate(this);
        this.editQuantity=new Wtf.form.NumberField({
            allowNegative: false,
            defaultValue:0,
            maxLength:10,
            decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
        });
        this.editprice=new Wtf.form.NumberField({
            maxLength:10
        });
        this.cndnRemark= new Wtf.form.TextArea({
            name:'remark'
        });
        this.Discount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0//,
            //maxValue:100
        });
        this.creditTo= new Wtf.form.ExtFnComboBox({
            //fieldLabel:(this.isCustomer?"Credit Account*":"Debit Account*"),
            hiddenName:"creditoraccount",
            //anchor:"50%",
            store: this.allAccountStore,
            valueField:'accid',
            displayField:'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:300,
            //hidden:!this.isCustBill,
            //hideLabel:!this.isCustBill,
            itemCls : (!this.isCustBill)?"hidden-from-item":"",
            //allowBlank:!this.isCustBill||this.isOrder,
            hirarchical:true,
            //emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),  //'Select an Account...',
            //mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            //triggerAction:'all',
//            addNewFn: this.addAccount.createDelegate(this,[this.allAccountStore],true),
            scope:this
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.creditTo.addNewFn=this.addAccount.createDelegate(this,[this.allAccountStore],true);   
        
    },
    addAccount: function(store){
        callCOAWindow(false,null,"coaWin",this.isCustomer,false,false,false,false,false,true);
        Wtf.getCmp("coaWin").on('update',function(){store.reload();},this);
    },
    addTax:function(){
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.taxStore.reload();}, this);
    },
    createColumnModel:function(){
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=(this.isNote)?new Wtf.grid.CheckboxSelectionModel():new Wtf.grid.RowNumberer();
        var columnArr =[];
        if(!this.readOnly){
            if(this.isNote) {
                columnArr.push(this.rowno)
            }
        }
        columnArr.push({
            header:this.isCustBill ? WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),
            width:250,
            dataIndex:'productdetail',
            editor:(this.isNote||this.readOnly)?"":this.productEditor
        });
        columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
        columnArr.push({
            header:(this.isCustomer?WtfGlobal.getLocaleText("acc.rem.217"):WtfGlobal.getLocaleText("acc.je.accDebit")),
            width:250,
            dataIndex:'creditoraccount',
            renderer:Wtf.comboBoxRenderer(this.creditTo),
            editor:(this.isNote||this.readOnly)?"":this.creditTo            
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
             header:WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),
             dataIndex:"quantity",
             align:'right',
             width:200,
             editor:(this.isNote||this.readOnly)?"":this.editQuantity,
             renderer:this.quantityRenderer
         },{
             header:WtfGlobal.getLocaleText("acc.inventoryList.remQty"),
             dataIndex:"remainingquantity",
             align:'right',
             hidden:!this.isNote||this.noteTemp,
             width:150,
             editor:(this.isNote||this.readOnly)?"":this.editQuantity,
             renderer:this.quantityRenderer
        },{
             header:"<b>"+WtfGlobal.getLocaleText("acc.invoice.gridEnterQty")+"</b>",
             dataIndex:"remquantity",
            align:'right',
            hidden:!this.isNote||this.noteTemp,
            width:180,
            editor:this.readOnly?"":this.editQuantity,
            renderer:this.quantityRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"),
            dataIndex:"rate",
            align:'right',
            width:200,
            hidden:this.noteTemp,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            editor:(this.isNote||this.readOnly)?"":this.editprice
        },{
            header:WtfGlobal.getLocaleText("acc.field.SubAmount"),
            dataIndex:'totalamount',
            align:'right',
            width:200,
            hidden:this.isNote,
            renderer:this.calTotalAmount.createDelegate(this)
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:200,
            dataIndex:'discountispercent',
            hidden:this.isQuotation?false:(this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:(this.isNote||this.readOnly)?"":this.rowDiscountTypeCmb
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),//"Discount %",
             dataIndex:"prdiscount",
             align:'right',
             width:150,
             hidden:this.noteTemp,
             renderer:function(v,m,rec){
                 if(rec.data.discountispercent) {
                     v= v + "%";
                 } else {
                     var symbol = WtfGlobal.getCurrencySymbol();
                     if(rec.data['currencysymbol']!=undefined && rec.data['currencysymbol']!=""){
                         symbol = rec.data['currencysymbol'];
                     }
                     
                     v= WtfGlobal.conventInDecimal(v,symbol)
                 }
                 return'<div class="currency">'+v+'</div>';
             },
             editor:this.readOnly||this.isNote?"":this.Discount
         }/*,{
            header: "Discount %",
            dataIndex:"prdiscount",
            align:'right',
            hidden:this.isOrder ||this.noteTemp,
            width:200,
            renderer:function(v){return'<div class="currency">'+v+'%</div>';},
            editor:this.readOnly||this.isNote?"":this.Discount
         }*/,{
            header:WtfGlobal.getLocaleText("acc.invoice.gridDiscountAmt"),
            dataIndex:'discountamount',
            align:'right',
            width:200,
            hidden:this.isNote||this.noteTemp,
            renderer:this.calDiscountAmount.createDelegate(this)
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.Tax"),
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             //align:'right',
             width:120,
             fixed:true,
             hidden:!(this.editTransaction||this.readOnly)||this.noteTemp,
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:this.readOnly||this.isNote?"":this.transTax
        },{
             header: WtfGlobal.getLocaleText("acc.invoiceList.taxAmt"),
             dataIndex:"taxamount",
             id:this.id+"taxamount",
             //align:'right',
             width:150,
             hidden:!(this.editTransaction||this.readOnly)||this.noteTemp,
             renderer:this.setTaxAmount.createDelegate(this),
             editor:this.readOnly?"":new Wtf.form.NumberField({
                allowBlank: true,
                allowNegative: false,
                defaultValue:0
            })
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridLineAmt"),
            align:'right',
            dataIndex:"calamount",
            width:200,
            hidden:this.isNote,
            renderer:this.calDiscAmount.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridOriginalAmt"),
            dataIndex:"orignalamount",
            align:'right',
            width:150,
            hidden:!this.isNote||this.noteTemp,
            renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol)
        },{
            header:this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),
            dataIndex:"amount",
            align:'right',
            hidden:!this.isNote||this.noteTemp,
            width:200,
            renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:this.calAmount.createDelegate(this))
    },{
            header:WtfGlobal.getLocaleText("acc.invoice.Tax"),
            dataIndex:"taxpercent",
            align:'right',
            hidden:!this.isNote||this.noteTemp,
            width:200,
            renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
             header:WtfGlobal.getLocaleText("acc.field.ProductTaxAmount"),
             dataIndex:"taxamount",
             align:'right',
             hidden:!this.isNote||this.noteTemp,
             width:200,
             renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header:(this.readOnly)?WtfGlobal.getLocaleText("acc.dnList.gridAmt"):"<b>"+WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt")+"</b>",
            dataIndex:this.noteTemp?'discount':'discamount',
            align:'right',
            width:200,
            hidden:!this.isNote,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
               allowBlank: false,
               allowNegative: false
           })
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridNoteType"),
            width:200,
            dataIndex:'typeid',
            hidden:(!this.isNote ||this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.typeEditor),
            editor:this.readOnly?"":this.typeEditor
        },
        {
            header:WtfGlobal.getLocaleText("acc.invoice.gridRemark"),//"Note Type",
            width:200,
            hidden:(!this.isNote ||this.noteTemp),
            dataIndex:'gridRemark',
            name:'gridRemark',
            editor:(this.readOnly)?"":this.cndnRemark
        });
        if(!this.isNote && !this.readOnly) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.auditTrail.action"),
                align:'center',
                width:30,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);
    },
    quantityRenderer:function(val,m,rec){
      //return parseFloat(val).toFixed(2);  
      return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addBlankRow();
                }
                this.fireEvent('datachanged',this);
            }, this);
        }
    },

    calAmount:function(v,m,rec){
        var val=rec.data['quantity']*rec.data['rate']-(rec.data['rate']*(rec.data['quantity']*rec.data['prdiscount']/100));
        rec.set("amount",val);
        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
        return WtfGlobal.currencyRendererSymbol(val,m,rec);
    },

    addBlankRow:function(){
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
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
        this.store.add(newrec);
    },
  addBlank:function(){
       this.setGridDiscValues();
        this.addBlankRow();
    },
      setGridDiscValues:function(){
            this.store.each(function(rec){
                   if(!this.editTransaction&&!this.fromPO)
                        rec.set('prdiscount',0)
                },this);
    },
    updateRow:function(obj){
        if(obj!=null){
             var rec=obj.record;
             if(obj.field=="prdiscount" && (rec.data.discountispercent == 1)){
                 rec=obj.record;
                 if(obj.value >100){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Discountcannotbegreaterthan100")], 2);
                        rec.set("prdiscount",0);
                  }
                if(obj.value=="")
                     rec.set("prdiscount",0);

             }
            
            if(obj.field=="discountispercent" && obj.value == 1 && (rec.data.prdiscount > 100)){
                
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Percentdiscountcannotbegreaterthan100")], 2);
                rec.set("discountispercent",0);
            } else {
                this.fireEvent('datachanged',this);
            }
            
             if((rec.data.isNewRecord=="" || rec.data.isNewRecord==undefined) && (obj.field=="quantity" || obj.field=="rate") && this.fromOrder&&!(this.editTransaction||this.copyInv)){
                if(obj.value!=obj.originalValue) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.agedPay.alert"),this.isCustomer?WtfGlobal.getLocaleText("acc.field.ProductenteredInvoicedifferentoriginal"):WtfGlobal.getLocaleText("acc.field.ProductQuantityenteredinVendorInvoiceisdifferentfromoriginalquantitymentionedinPODOwantcontinue"),function(btn){
                        if(btn!="yes") {obj.record.set(obj.field, obj.originalValue)}
                    },this)
                }
            }
             else if(obj.field=="prtaxid"){
                rec=obj.record;
                   // alert(obj.field)
                var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
                var val=(rec.data.quantity*rec.data.rate)-discount;
                var taxpercent=0;
               var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
                if(index>=0){
                    var taxrec=this.taxStore.getAt(index);
                    // alert(taxrec.data.toSource())
                    taxpercent=taxrec.data.percent;
                }
                var taxamount= (val*taxpercent/100);
               // alert(taxamount)
                rec.set("taxamount",taxamount);
            }

            if(this.isNote){
                if(obj.field=="typeid"&&(obj.value==0)){
                    rec.set('remquantity',0);
                    rec.set('discamount',0);
                }

            if(obj.field=="remquantity"){
                if(rec.data['typeid']==0){
                    obj.cancel=true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ChangetheNoteTypefirst") ], 2);
                    rec.set('remquantity',0);
                    rec.set('discamount',0);
                }
                else{
                    rec=this.store.getAt(this.store.find('rowid',obj.record.data['rowid']));
                    if(rec.data['remainingquantity']<obj.value){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.nee.54")+' '+rec.data['productdetail']+ WtfGlobal.getLocaleText("acc.field.is")+rec.data.remainingquantity], 2);
                        obj.cancel=true;
                        rec.set('remquantity',0);
                        rec.set('discamount',0);
                    }else{
                        var qty = obj.value;
                        var rate = rec.data['rate'];
                        var prDiscount = rec.data['prdiscount'];
                        var prTax = rec.data['prtaxpercent'];
                        var prTaxAmount = rec.data['taxamount'];
                        var amt = qty * rate;
                        if(prDiscount > 0)
                            amt = amt - ((amt * prDiscount) / 100);
                        if(prTax > 0)
                            amt = amt + (prTaxAmount);//amt + ((amt * prTax) / 100);
                        rec.set('discamount',amt);
                    }
                }
            }
            if(obj.field=="typeid"){
                rec=this.store.getAt(this.store.find('productid',obj.record.data['productid']));
                if(rec.data['typeid']==0){
                    rec.set('remquantity',0);
                    rec.set('discamount',0);
                }
            }
            if(obj.field=="discamount"){
                if(rec.data['orignalamount']<obj.value){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Remainingamountfortheselectedproductis")+WtfGlobal.getCurrencySymbol()+" "+(rec.data['amount'])], 2);
                    obj.cancel=true;
                    rec.set('discamount',0);
                }
            }
        }
        if(obj.field=="prtaxid" || obj.field=="prdiscount" || obj.field=="quantity" || obj.field=="rate" || obj.field=="discountispercent"){
            taxamount = this.setTaxAmountAfterSelection(rec);
            rec.set("taxamount",taxamount);
        }
        }
        this.fireEvent('datachanged',this);
        if(this.store.getCount()>0&&(this.store.getAt(this.store.getCount()-1).data['productdetail'].length<=0||this.store.getAt(this.store.getCount()-1).data['totalamount']==0)){
            return;}
        if(!this.isNote)
            this.addBlankRow();
    },    

    calDiscountAmount:function(v,m,rec){
        var amount = rec.data['rate']*(rec.data['quantity']);
        var discount = rec.data['prdiscount'];
        if(rec.data['prdiscount'] > 0) {
            if(rec.data['discountispercent'] == 1){
                discount = ((amount * discount) / 100);
            }
        }
        rec.set("discountamount",discount);
        
//        var val=(rec.data['rate']*(rec.data['quantity']*rec.data['prdiscount']/100));
        return WtfGlobal.withoutRateCurrencySymbol(discount,m,rec);
    },
    
    setTaxAmountAfterSelection : function(rec){
        var discount = 0;
        if(rec.data.prdiscount > 0) {
            if(rec.data.discountispercent == 1){
                discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
            } else {
                discount = rec.data.prdiscount;
            }
        }
        
        var val=(rec.data.quantity*rec.data.rate)-discount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=taxrec.data.percent;
        }
        var taxamount= (val*taxpercent/100);
        return taxamount;
    },

    calDiscAmount:function(v,m,rec){
        var amount = rec.data['rate']*(rec.data['quantity']);
        var discount = rec.data['prdiscount'];
        if(rec.data['prdiscount'] > 0) {
            if(rec.data['discountispercent'] == 1){
                discount = ((amount * discount) / 100);
            }
        }
        var val=amount - discount;
        
        var taxamount= rec.get('taxamount');//this.calTaxAmount(rec);
        val+=taxamount;
        rec.set("calamount",val);
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    },
     calTaxAmount:function(rec){
//        var discount=rec.data.rate*rec.data.quantity*rec.data.prdiscount/100
//        var val=(rec.data.quantity*rec.data.rate)-discount;
        var amount = rec.data['rate']*(rec.data['quantity']);
        var discount = rec.data['prdiscount'];
        if(rec.data['prdiscount'] > 0) {
            if(rec.data['discountispercent'] == 1){
                discount = ((amount * discount) / 100);
            }
        }
        var val=amount - discount;
        var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
              // alert(taxrec.data.toSource())
                taxpercent=taxrec.data.percent;
            }
        return (val*taxpercent/100);

    },
    setTaxAmount:function(v,m,rec){
        var taxamount = 0;
        if(v){
            taxamount= v;//this.calTaxAmount(rec);
        }
        if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == ""){
           taxamount = 0;
        }
   	rec.set("taxamount",taxamount);
        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
        return WtfGlobal.currencyRendererSymbol(taxamount,m,rec);
    },

     calTotalAmount:function(v,m,rec){
        rec.set("totalamount",rec.data['quantity']*rec.data['rate']);
        var val=rec.data['quantity']*rec.data['rate'];
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    },
    loadPOGridStore:function(recIds){
        this.store.load({params:{bills:recIds,mode:(this.isCustBill?53:43),closeflag:true}});
    },
    calSubtotal:function(){
        var subtotal=0;
        for(var i=0;i<this.store.getCount();i++)
            subtotal+=this.store.getAt(i).data['calamount'];
        return subtotal;
    },
    setCurrencyid:function(cuerencyid,rate,symbol,record){
        this.symbol=symbol;
        this.store.each(function(rec){
            rec.set('currencysymbol',this.symbol)
        },this)
        // this.store.commitChanges();
     },
    getProductDetails:function(){
        this.store.each(function(rec){
            rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
        }, this);
        return WtfGlobal.getJSONArray(this);
    },

    getCMProductDetails:function(){
        var arr=[];
        var selModel=  this.getSelectionModel();
        var len=this.store.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i))
                arr.push(i);
            }
        return WtfGlobal.getJSONArray(this,true,arr);
    },

    isAmountzero:function(store){
        var amount;
        var selModel=  this.getSelectionModel();
        var len=this.store.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
                amount=store.getAt(i).data["discamount"];
                if(amount<=0)
                    return true;
            }
        }
        return false;
    }
});
