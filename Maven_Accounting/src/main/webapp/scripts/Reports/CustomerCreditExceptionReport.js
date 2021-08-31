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

function callCustomerCreditExceptionDynamicLoad(){
    var reportPanel = Wtf.getCmp('customercreditexception');
    if(reportPanel == null){
        reportPanel = new Wtf.CustomerCreditExceptions({
            id : 'customercreditexception',
            border : false,
            title: WtfGlobal.getLocaleText("acc.field.CustomerCreditExceptionReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.CustomerCreditExceptionReport"),
            layout: 'fit',
            iscustreport : true,
            closable : true,
            isCustomer:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //"Invoice",
            iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(reportPanel);
    }

    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();

}
//*********************************************************************************************
Wtf.CustomerCreditExceptions = function(config){
    this.isCustBill=config.isCustBill;
    this.GridRec = Wtf.data.Record.create ([
        {name:'accid'},
        {name:'openbalance'},
        {name:'id'},
        {name:'title'},
        {name:'accname'},
        {name:'acccode'},        
        {name:'personname',mapping:'accname'},
        {name:'personid',mapping:'id'},
        {name:'taxeligible',type:'boolean'},
        {name:'overseas',type:'boolean'},
        {name:'mapcustomervendor',type:'boolean'},
        {name:'taxidnumber'},
        {name:'company'},    
        {name:'contactno2'},                 
        {name:'pdm'},
        {name:'pdmname'},
        {name:'parentid'},
        {name:'parentname'},
        {name:'bankaccountno'},
        {name:'termid'},
        {name:'termname'},
        {name:'other'},
        {name: 'leaf'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'istaxeligible'},
        {name: 'deleted'},
        {name: 'creationDate' },//type:'date'}, //This date is handled on JAVA side & sent as String only
        {name: 'categoryid'},
        {name: 'taxno'},
        {name: 'level'},
        {name: 'contactperson'},
        {name: 'amountdue'},
        {name: 'country'},
        {name: 'limit'},
        {name:'fixedAssetInvoice'},
        {name:'fixedAssetLeaseInvoice'},
        {name:'billingAddress'},
        {name:'billingEmailID'},
        {name:'billingMobileNumber'},
        {name:'shippingAddress'} 
    ]);

//    this.StoreUrl = "ACC" + (this.isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices") + ".do";
    this.StoreUrl = "ACCCustomerCMN/getCustomerCreditExceptions.do";
    
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        baseParams: {
          consolidateFlag:config.consolidateFlag,
          companyids:companyids,
          gcurrencyid:gcurrencyid,
          userid:loginid
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
 //    this.Store.on('load',this.hideLoading, this);
//    this.Store.on('loadexception',this.hideLoading, this);
    this.Store.load();
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.customerList.search"),  //'Quick Search By Invoice No.',
        width: 200,
        field: 'billno',
        Store:this.Store
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        handler: this.handleResetClick,
        disabled: false
    });
    
    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
    
    this.expander = new Wtf.grid.RowExpander({});
    this.sm = new Wtf.grid.RowSelectionModel({singleSelect: true});
    this.gridView1 = config.consolidateFlag?new Wtf.grid.GroupingView({
            forceFit:false,
            showGroupName: true,
            enableNoGroups:false, // REQUIRED!
            hideGroupedColumn: true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }):{
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        };
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        border:false,
        sm:this.sm,
        layout:'fit',
        loadMask:true,
        plugins: this.expander,
        viewConfig:this.gridView1,
        columns:[
            this.expander,
            {
            header:WtfGlobal.getLocaleText("acc.stockLedgerCust.Code"),  //"Customer Code,
            dataIndex:'acccode',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //"Name",
            dataIndex:'accname',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Address"),  //"Address",
            dataIndex:'billingAddress',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridEmail"),  //"Email",
            dataIndex:'billingEmailID',
            pdfwidth:110,
            renderer:WtfGlobal.renderDeletedEmailTo
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridContactNo"),  //"Contact No",
            dataIndex:'billingMobileNumber',
            pdfwidth:75,
            renderer:WtfGlobal.deletedRenderer    
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridShippingAddress"),  //"Shipping Address",
            dataIndex:'shippingAddress',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75,
            hidden: false
         },{
            header: WtfGlobal.getLocaleText("acc.customerList.gridCreationDate"),  //"Creation Date",
            dataIndex: "creationDate",
             //This date is handled on JAVA side & sent as String only
//            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:150
         },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.gridCreditTerm"):WtfGlobal.getLocaleText("acc.customerList.gridDebitTerm")),  //"Credit":"Debit")+" Term",
            dataIndex:'termname',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:125
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.cust.creditLimit"):WtfGlobal.getLocaleText("acc.cust.debitLimit")),  //"Credit":"Debit")+" limit",
            dataIndex:'limit',
            renderer:WtfGlobal.currencyRenderer,
            pdfwidth:100   
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridAmountDue"),  //"Amount Due",
            dataIndex:'amountdue',
            align: 'right',
            pdfwidth:75,
            pdfrenderer:"rowcurrency",
            renderer:WtfGlobal.currencyRenderer
        }]
        });

    this.expandRec = Wtf.data.Record.create ([
        {name:'parentInvoiceId'},
        {name:'invoiceId'},
        {name:'invoiceNo'}
    ]);
    this.expandStoreUrl = "ACCCustomerCMN/getCustomerCreditExceptionInvoices.do";
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expandStore.on("beforeload", function(store){
        this.expandStoreUrl = "ACCCustomerCMN/getCustomerCreditExceptionInvoices.do";
        store.proxy.conn.url = this.expandStoreUrl;
    }, this);
    

    this.grid.on('cellclick',this.onRowClick, this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    this.exportButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.export"),
        obj:this,
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),
        filename:WtfGlobal.getLocaleText("acc.field.CustomerCreditExceptionReport")+"_v1",
        params:{
            name: "Customer_Credit_Limit_Exceed"
        },
        get:Wtf.autoNum.CustomerCreditExceed,
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        }
    });
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
        filename:WtfGlobal.getLocaleText("acc.field.CustomerCreditExceptionReport"),
        params:{
            name: "Customer_Credit_Limit_Exceed"
        },
        label:WtfGlobal.getLocaleText("acc.field.Customer_Credit_Limit_Exceed"),
        get:Wtf.autoNum.CustomerCreditExceed,
        menuItem:{
            print:true
        }
    });
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        bodyStyle : "background-color:#ffffff;padding-right:10px;",
        items:[this.grid],
        tbar : [this.quickPanelSearch,this.resetBttn,this.exportButton,this.printButton],
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
           })
        })
    });
    Wtf.CustomerCreditExceptions.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.CustomerCreditExceptions, Wtf.Panel,{
    //hideLoading:function(){  Wtf.MessageBox.hide(); },
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.withInvMode = record.data.withoutinventory;
        this.expandStore.load({params:{customer:record.data['accid']}});
    },
    expandRow:function(){
        Wtf.MessageBox.hide();
    },

    fillExpanderBody:function(){
        var disHtml = "";
        var header = "";

        if(this.expandStore.getCount()==0){
            header = "<span style='color:#15428B;display:block;'>"+WtfGlobal.getLocaleText("acc.invList.repeated.invgen")+"</span>";   // No any invoices generated till now
        } else {
            var blkStyle = "display:block;float:left;width:150px;Height:14px;"
            header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.invList.repeated.genInv")+"</span>";  //Generated Invoices
            for(var i=0;i<this.expandStore.getCount();i++){
                var rec=this.expandStore.getAt(i);
                header += "<span style="+blkStyle+">"+
                            "<a class='jumplink' onclick=\"jumpToTemplate('"+rec.data.invoiceId+"',"+this.withInvMode+")\">"+rec.data.invoiceNo+"</a>"+
                        "</span>";
            }
        }
        disHtml += "<div style='width:95%;margin-left:3%;'>" + header + "<br/></div>";
        this.expanderBody.innerHTML = disHtml;
    },

    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.Store.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
            this.Store.on('load',this.storeloaded,this);
        }
    },
    storeloaded:function(store){
     //   Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    repeateInvoice:function(){
        var formrec= this.grid.getSelectionModel().getSelected();
        var withoutinventory= this.grid.getSelectionModel().getSelected().data.withoutinventory;
        callRepeatedInvoicesWindow(withoutinventory, formrec);
    },
    onRowClick:function(g,i,j,e){
        e.stopEvent();
//        var el=e.getTarget("a");
//        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="billno"){
            var formrec = this.grid.getSelectionModel().getSelected();
            this.withInvMode = formrec.data.withoutinventory;
            var incash=formrec.get("incash");
            
            if(incash &&!this.withInvMode)
                callViewCashReceipt(formrec, 'ViewInvoice');
//            else if(incash)
//                callViewBillingCashReceipt(formrec,null, 'ViewBillingCSInvoice',true);
//            else if(this.withInvMode){
//                callViewBillingInvoice(formrec,null, 'ViewBillingInvoice',false);
//            }
            else {
                if(formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                    callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
                }  else if(formrec.data.isConsignment){
                    callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
                }else{
                    callViewInvoice(formrec, 'ViewCashReceipt');
                }
            }
        }
    }
});