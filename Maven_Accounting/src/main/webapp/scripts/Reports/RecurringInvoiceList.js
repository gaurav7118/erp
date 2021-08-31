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

function CallOutstandingOrdersReportDynamicLoad(){
    var reportPanel=Wtf.getCmp('Outstanding_Orders_Report');
    if(reportPanel==null){
        reportPanel = new Wtf.RecuringInvoiceList({
            id :"Outstanding_Orders_Report",
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.navigate.OutstandingOrdersReport"), Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.navigate.OutstandingOrdersReport"),
            layout: 'fit',
            //            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //"Invoice",
            isOrder:false,
            closable: true,
            iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}


//******************************************************************************************************
Wtf.RecuringInvoiceList = function(config) {
    Wtf.apply(this, config);
    this.msgLmt = 30;
    this.invID=null;
     this.expandRec = Wtf.data.Record.create ([
        {name:'productname'},
        {name:'productdetail'},
        {name:'prdiscount'},
        {name:'discountispercent'},
        {name:'amount'},
        {name:'productid'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'partamount'},
        {name:'quantity'},
        {name:'unitname'},
        {name:'uomname'},
        {name:'rate'},
        {name:'rateinbase'},
        {name:'externalcurrencyrate'},
        {name:'prtaxpercent'},
        {name:'rowTaxAmount'},
        {name:'orderrate'},
        {name:'desc', convert:WtfGlobal.shortString},
        {name:'productmoved'},
        {name:'currencysymbol'},
        {name:'currencyrate'},
        {name: 'type'},
        {name: 'pid'},
        {name:'carryin'},
        {name:'approverremark'},
        {name:'permit'},
        {name:'linkto'},
        {name:'customfield'},
        {name:'balanceQuantity'}
    ]);
       this.expandStore = new Wtf.data.Store({
        url:"ACCInvoiceCMN/getInvoiceRows.do",
        baseParams:{
            mode:17,
            dtype : 'report'//Display type report/transaction, used for quotation
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.RecurringInvoiceRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'rowproductname'},
        {name:'rowquantity'},
        {name:'rowrate'},
        {name:'rowprdiscount'},
        {name:'rowprtaxpercent'},
        {name:'excluded'},
        {name:'action'},
        {name:'includeprotax',type:'boolean'},
        {name:'createdby'},
        {name:'createdbyid'},
        {name:'NoOfpost'}, 
        {name:'NoOfRemainpost'},  
        {name:'templateid'},
        {name:'templatename'},
        {name:'amountwithouttax'},
        {name:'amountwithouttaxinbase'},
        {name:'commission'},
        {name:'commissioninbase'},
        {name:'amountDueStatus'},
        {name:'salesPerson'},
        {name:'agent'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'approvalstatus'},
        {name:'approvalstatusint', type:'int', defaultValue:-1},
        {name:'isfavourite'},
        {name:'othervendoremails'},
        {name:'termdetails'},
        {name:'approvestatuslevel'},// for requisition
        {name:'posttext'},
        {name:'isOpeningBalanceTransaction'},
        {name:'isNormalTransaction'},
        {name:'isreval'},
        {name:'isprinted'},
        {name:'validdate', type:'date'},
        {name:'cashtransaction',type:'boolean'},
        {name:'landedInvoiceNumber'},
        {name:'billingAddress'},
        {name:'billingCountry'},
        {name:'billingState'},
        {name:'billingPostal'},
        {name:'billingEmail'},
        {name:'billingFax'},
        {name:'billingMobile'},
        {name:'billingPhone'},
        {name:'billingContactPerson'},
        {name:'billingContactPersonNumber'},
        {name:'billingContactPersonDesignation'},
        {name:'billingWebsite'},
        {name:'billingCounty'},
        {name:'billingCity'},
        {name:'billingAddressType'},
        {name:'shippingAddress'},
        {name:'shippingCountry'},
        {name:'shippingState'},
        {name:'shippingCounty'},
        {name:'shippingCity'},
        {name:'shippingEmail'},
        {name:'shippingFax'},
        {name:'shippingMobile'},
        {name:'shippingPhone'},
        {name:'shippingPostal'},
        {name:'shippingContactPersonNumber'},
        {name:'shippingContactPersonDesignation'},
        {name:'shippingWebsite'},
        {name:'shippingContactPerson'},
        {name:'shippingAddressType'}  
    ]); 
   
     this.RecurringInvoiceStore = new Wtf.data.Store({
           url:"ACCInvoiceCMN/getRecurringInvoices.do",
           reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.RecurringInvoiceRec)
        });
     this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false                          //this.isRequisition ? false : true,
     });
    this.expander = new Wtf.grid.RowExpander({});
    this.gridColumnModelArr=[];
    this.gridColumnModelArr.push(this.sm,this.expander,{
            hidden:true,
            dataIndex:'billid'
        },{
            header: WtfGlobal.getLocaleText("acc.common.referenceInvoice"),
            dataIndex:'billno',
            width:150,
            pdfwidth:75,
            renderer:function(v,m,rec) {
                 if(rec.data.excluded){
                     v="<span class='deletedlink'><del>"+v+"</del></span>";
                 }else if(rec.data.isOpeningBalanceTransaction && !rec.data.isNormalTransaction){
                     v=v;
                 }else{
                     v= "<a class='jumplink' href='#'>"+v+"</a>";
                 }
           return v;
        }
        },{
            header: WtfGlobal.getLocaleText("acc.het.12"),
            dataIndex:'date',
            align:'center',
            width:150,
            pdfwidth:80,
            renderer:function(v,m,rec) {
                        if(!v) return v;
                        if(rec!=undefined&&rec.data.excluded)
                            v='<del>'+v.format(WtfGlobal.getOnlyDateFormat())+'</del>';
                        else
                                v = v.format(WtfGlobal.getOnlyDateFormat());
                        v='<div class="datecls">'+v+'</div>';
                        return v;
                    }
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.cust"),  //this.businessPerson,
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.excludedRenderer,
            dataIndex:'personname'
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.discount"),  //"Discount",
            dataIndex:'discount',
            align:'right',
            width:150,
            pdfwidth:75,
            renderer:function(value,m,rec) {
                        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                        var v=parseFloat(value);
                        if(isNaN(v)) return value;
                        if(rec.data.excluded)
                            v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
                        else
                                v=WtfGlobal.conventInDecimal(v,symbol);
                        v=  '<div class="currency">'+v+'</div>';
                        return v;
                    }
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.taxName"),  //"Tax Name",
            dataIndex:'taxname',
            width:150,
            pdfwidth:75,
            renderer:WtfGlobal.excludedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.taxAmt"),  //"Tax Amount",
            dataIndex:'taxamount',
            align:'right',
            width:150,
            pdfwidth:75,
            renderer:function(value,m,rec) {
                        var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                        var v=parseFloat(value);
                        if(isNaN(v)) return value;
                        if(rec.data.excluded)
                            v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
                        else
                                v=WtfGlobal.conventInDecimal(v,symbol);
                        v=  '<div class="currency">'+v+'</div>';
                        return v;
                    }
       },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),  //"Total Amount",
            align:'right',
            dataIndex:'amount',
            width:150,
            pdfwidth:75,
            renderer:function(value,m,rec) {
                    var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                    var v=parseFloat(value);
                    if(isNaN(v)) return value;
                    if(rec.data.excluded)
                        v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
                    else
                            v=WtfGlobal.conventInDecimal(v,symbol);
                    v=  '<div class="currency">'+v+'</div>';
                    return v;
                }
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome") + " ("+WtfGlobal.getCurrencyName()+")",  //"Total Amount (In Home Currency)",
            align:'right',
            dataIndex:'amountinbase',
            width:150,
            pdfwidth:75,
            hidecurrency : true,
//            renderer:WtfGlobal.currencyDeletedRenderer     
            renderer:function(value,m,rec) {
                    var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                    var v=parseFloat(value);
                    if(isNaN(v)) return value;
                    if(rec.data.excluded)
                        v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
                    else
                            v=WtfGlobal.conventInDecimal(v,symbol);
                    v=  '<div class="currency">'+v+'</div>';
                    return v;
                }
        },{
            header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",
            dataIndex:'memo',
            hidden:this.isSalesCommissionStmt,
            renderer:function(value){
                var res = "<span class='gridRow' style='width:200px;'  wtf:qtip='"+value+"'>"+Wtf.util.Format.ellipsis(value,60)+"</span>";
                return res;
            },
            width:150,
            pdfwidth:100
        },{
            header: WtfGlobal.getLocaleText("acc.common.action"),
            dataIndex:'action',
            hidden:this.isSalesCommissionStmt,
            width:150,
            pdfwidth:100
        });
   
       this.onDate=new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectDate") + " :",
            format:WtfGlobal.getOnlyDateFormat(),
            name:'onDate' + this.id,
            value:Wtf.serverDate
        });
          
        this.submitBtn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.fetch"),
            tooltip : WtfGlobal.getLocaleText("acc.invReport.fetchTT"),  
            id: 'submitRec' + this.id,
            scope: this,
            iconCls:'accountingbase fetch',
            handler:this.FetchInvoices
        });
        this.GenerateBtn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.generateInvoice"),
            tooltip :"Generate Invoice Now",
            id: 'btnGenerateInv' + this.id,
            scope: this,
            disabled :true,
            iconCls :getButtonIconCls(Wtf.etype.copy),
            handler:this.GenerateInvoice
        });
        this.ExcludeBtn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.excludeInvoice"),
            tooltip: WtfGlobal.getLocaleText("acc.common.excludeInvoice.tt"),
            id: 'btnExcludeInv' + this.id,
            disabled :true,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.ExcludeInvoice
        });
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    this.RecurringInvoiceStore.on('load',this.expandRow, this);
    this.RecurringInvoiceGrid = new Wtf.grid.GridPanel({
        store: this.RecurringInvoiceStore,
        cm: new Wtf.grid.ColumnModel(this.gridColumnModelArr),
        sm:this.sm,
        viewConfig:{
            forceFit:false,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            
        },
        plugins:[this.expander],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: this.msgLmt,
            id: "pagingtoolbar" + this.id,
            store: this.RecurringInvoiceStore,
            displayInfo: true,
//            displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id,
                ftree:this.grid
            })
        })
    });
     this.sm.on("selectionchange",function(sm){
         if(sm.getSelected() !=undefined){
            if(!sm.getSelected().data.excluded && this.sm.getCount()==1){
                this.ExcludeBtn.enable();
                this.GenerateBtn.enable();
            }else{
                this.ExcludeBtn.disable();
                this.GenerateBtn.disable();
            }
         }else{
             this.ExcludeBtn.disable();
             this.GenerateBtn.disable();
         }
     },this);
     
     this.RecurringInvoiceStore.on('beforeload', function(){
        this.RecurringInvoiceStore.baseParams = {
            InvoicesOnDate : WtfGlobal.convertToGenericDate(this.onDate.getValue())
             
        }
        
    }, this);
 
    this.RecurringInvoiceStore.load();
    Wtf.RecuringInvoiceList.superclass.constructor.call(this, {
        border: false,  
        layout: 'border',
        items:[{
            id:'List_Of_Outstanding_Orders',
            margins: '0 5 5 5',
            region: 'center',
            layout: 'fit',
            title: WtfGlobal.getLocaleText("acc.common.listOf.outstandingOrders"),
            split: true,
            border: false,
            items: this.RecurringInvoiceGrid,
        tbar: [WtfGlobal.getLocaleText("acc.field.SelectDate") + " : ",this.onDate,this.submitBtn,this.GenerateBtn,this.ExcludeBtn]
//            bbar: []
        }]
    });
}

Wtf.extend(Wtf.RecuringInvoiceList, Wtf.Panel, {
    onRender: function(config) {
        Wtf.RecuringInvoiceList.superclass.onRender.call(this, config);
        var nextDate= new Date(this.onDate.getValue());
        nextDate.setDate(nextDate.getDate()+1);
        this.onDate.setValue(nextDate.format("Y-m-d"));
    },
    FetchInvoices: function() {
            this.RecurringInvoiceStore.load({
           params : {
               start : 0,
               limit : this.pP.combo.value
           }
       });
     },
   GenerateInvoice:function(){
        var formrec = this.RecurringInvoiceGrid.getSelectionModel().getSelected();
        if(formrec != undefined){
            callEditInvoice(formrec, 'Generate Invoice',true,undefined,false,true,this.onDate.getValue());
        }
   },
   ExcludeInvoice:function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.excludeOutstandingOrders")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
            if(btn=="yes"){
                var formrec = this.RecurringInvoiceGrid.getSelectionModel().getSelected();
                if(formrec != undefined){
                    Wtf.Ajax.request({
                        method: 'POST',
                        url: 'ACCInvoiceCMN/excludeInvoice.do',
                        scope: this,
                        params: {
                            invoice: formrec.data.billid,
                            excludeOrGenerate: 0,
                            generatedDate:WtfGlobal.convertToGenericDate(this.onDate.getValue())
                        },
                        success: function(response) {
                            this.RecurringInvoiceStore.load({
                                params : {
                                    start : 0,
                                    limit : this.pP.combo.value
                                }
                            });
                        }
                    });
                }
            }else{
                return true;
            }
        },this); 
     
    },
    onRowexpand:function(scope, record, body){
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.expandStore);
        this.expanderBody=body;
        this.withInvMode = record.data.withoutinventory;
        this.expandStore.load({params:{bills:record.data.billid,isexpenseinv:false}});
    },
    fillExpanderBody:function(){
        var disHtml = "";
        var arr=[];
        var header="";
        var rec;
        var amount;
           arr=[WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"),//PID for Inventory
                WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//Product Details or Product Name
                WtfGlobal.getLocaleText("acc.invoiceList.expand.pType"),//Product Type
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"),//Quantity,
                WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"),//Unit Price
                WtfGlobal.getLocaleText("acc.field.PartialAmount(%)"),
                WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"),//Discount
                WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"),//Tax
                WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"),//Amount
                WtfGlobal.getLocaleText("acc.field.SO/DO/CQNo")];
        	var gridHeaderText = WtfGlobal.getLocaleText("acc.invoiceList.expand.pList");
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";   //Product List
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
            var custArr = [];
            var arrayLength=arr.length;
            for(var custCount=0;custCount<custArr.length;custCount++){
                var headerFlag=false;
                if(custArr[custCount].header != undefined ) {
                    
                for(var j=0;j<this.customizeData.length;j++){
                              if(custArr[custCount].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                                   headerFlag=true; 
                        }
                    }
                    if(!headerFlag){
                        arr[arrayLength]=custArr[custCount].header;
                        arrayLength=arr.length;
                    }
            }
            }
            for(var arrI=0;arrI<arr.length;arrI++){
                if(arr[arrI]!=undefined) 
                   header += "<span class='headerRow' style='width: 7% ! important;'>" + arr[arrI] + "</span>";
            }
            header += "<span class='gridLine'></span>";
            
            for(var storeCount=0;storeCount<this.expandStore.getCount();storeCount++){
                rec=this.expandStore.getAt(storeCount);
                var productname=rec.data['productname'];

                //Column : S.No.
                header += "<span class='gridNo'>"+(storeCount+1)+".</span>";

                    var pid=rec.data['pid'];
                    header += "<span class='gridRow'  wtf:qtip='"+pid+"' style='width: 7% ! important;'>"+Wtf.util.Format.ellipsis(pid,15)+"</span>";

                //Column : Product Name
                header += "<span class='gridRow'  wtf:qtip='"+productname+"' style='width: 7% ! important;'>"+Wtf.util.Format.ellipsis(productname,15)+"</span>";

             
                     var type = "";
                    type = rec.data['type']
                      header += "<span class='gridRow' wtf:qtip='"+type+"' style='width: 7% ! important;'>"+Wtf.util.Format.ellipsis(type,15)+"</span>";
                

                //Quantity
                 header += "<span class='gridRow' style='width: 7% ! important;'>"+parseFloat(getRoundofValue(rec.data['quantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+rec.data['unitname']+"</span>";
              
                //Unit Price
                if(!this.isRFQ) {
                    var rate=rec.data.rate;
                    header += "<span class='gridRow' style='width: 7% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rate,rec.data['currencysymbol'],[true])+"</span>";
                }

                //Partial Amount
                   header += "<span class='gridRow' style='width: 7% ! important;text-align:center;'>"+rec.data['partamount']+"% "+"&nbsp;</span>";
               
                
                //Discount
                    if(rec.data.discountispercent == 0){
                        header += "<span class='gridRow' style='width: 7% ! important;text-align:center;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
                    } else {
                        header += "<span class='gridRow' style='width: 7% ! important;text-align:center;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
                    }

                    
                //Tax
                header += "<span class='gridRow' style='width: 7% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['rowTaxAmount'],rec.data['currencysymbol'],[true])+"</span>";
                
                //Amount
                amount=0;
                    amount=rec.data['quantity']*rate;
                    if(rec.data['partamount'] != 0){
                        amount = amount * (rec.data['partamount'] /100);
                    }
                    
                    var discount = 0;
                    if(rec.data.prdiscount > 0) {
                        if(rec.data.discountispercent == 0){
                            discount = rec.data.prdiscount;
                        } else {
                            discount = (amount * rec.data.prdiscount) / 100;
                        }
                    }
        
                    amount=(amount-discount);
                    amount+=rec.data['rowTaxAmount'];//(amount*rec.data['prtaxpercent']/100);
                    
//                }
                 header += "<span class='gridRow' style='width: 7% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(amount,rec.data['currencysymbol'],[true])+"</span>";
                 header += (this.isCustomer && this.isQuotation && !this.isOrder)?"<span class='gridRow' style='width: 7.5% ! important;text-align:center;'>"+rec.data['linkto']+"&nbsp;</span>":"";                
                
               //Blank Column
               header += "<span class='gridRow' style='width: 7% ! important;'>"+rec.data['productmoved']+"</span>";
               for(var cust=0;cust<custArr.length;cust++){
                   
                headerFlag=false;
                if(custArr[cust].header != undefined ) {
                    
                for(j=0;j<this.customizeData.length;j++){
                              if(custArr[cust].header==this.customizeData[j].fieldDataIndex && this.customizeData[j].hidecol){
                                   headerFlag=true; 
                        }
                    }
                    if(!headerFlag){
                    if(rec.data[custArr[cust].dataIndex]!=undefined && rec.data[custArr[cust].dataIndex]!="null")
                        header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cust].dataIndex]+"' style='width: 7% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cust].dataIndex],15)+"&nbsp;</span>";
                      else
                        header += "<span class='gridRow' style='width: 7% ! important;'>&nbsp;&nbsp;</span>";
                    }
                }  
                   
                   
                   
                } 
               header +="<br>";
        }
        if(this.expandStore.getCount()==0){
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
            header += "<span class='headerRow'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</span>"
        }
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    },
    expandRow:function(){
              var emptyTxt = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                       
            this.RecurringInvoiceGrid.getView().emptyText=emptyTxt;
            this.RecurringInvoiceGrid.getView().refresh();
      
         this.RecurringInvoiceStore.filter('billid',this.invID);
        if(this.exponly && ( this.RecurringInvoiceStore.getCount() !== 0)){
            this.expander.toggleRow(0);
        }
     }
   
});

