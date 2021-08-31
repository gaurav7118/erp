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

Wtf.contractDetailsView = function(config) {
    this.record = config.record;
    this.isNormalContract=config.isNormalContract?config.isNormalContract:false;// contract which is not a lease contract
    Wtf.apply(this, config);
    Wtf.contractDetailsView.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.contractDetailsView, Wtf.Panel, {
    initComponent: function(){
        Wtf.contractDetailsView.superclass.initComponent.call(this);

        this.image1 = document.createElement('img');
        if(Wtf.isGecko)
            this.image1.className = "tabimageG";
        else
            this.image1.className = "tabimage";
        
//============================================= For Contracts  Details Left Data View =======================================================
        
        this.contractStoreRec= Wtf.data.Record.create([
            {name:'cid'},
            {name:'customername'},
            {name:'contractid'},
            {name:'contractExpireyDate'},
            {name:'renewContract'},
            {name:'terminateContract'},
            {name:'tenureDetails'},
            {name:'totalAmount'},
            {name:'currencysymbol'},
            {name:'status'},
            {name:'contractTerm'}
        ]);
        
        this.contractStore = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractStoreRec)
        });
        this.contractStore.load({
            params: {
                contractid: this.record.data.cid
            }
        });
        
        this.contractStore.on('load', function() {
            this.soAmount = this.contractStore.getAt(0).get('totalAmount');
        },this);

        var tpl = new Wtf.XTemplate(
            '<table border="0" width="60%" style="padding-left:3%;padding-top:5%;">',
            '<tpl for=".">',           
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.CustomerName")+' : </td><td class="leadDetailTD">{customername}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.Contract#")+' : </td><td class="leadDetailTD">{contractid}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.ContractExpiryDate")+' : </td><td class="leadDetailTD">{contractExpireyDate:date("m/d/Y")}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.Status")+' : </td><td class="leadDetailTD">{status}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.ContractTerm")+' : </td><td class="leadDetailTD">{contractTerm}</td></tr>',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.RenewContract")+' : </td><td class="leadDetailTD">{renewContract}</td></tr>',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.TerminateContract")+' : </td><td class="leadDetailTD">{terminateContract}</td></tr>',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.TenureDetails")+' : </td><td class="leadDetailTD">{tenureDetails}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.TotalAmount")+' : </td><td class="leadDetailTD">{currencysymbol} {totalAmount}</td></tr>',
            '</tpl></table>'
        );
        
         this.contractCustomStoreRec= Wtf.data.Record.create([
            {name:'fieldlabel'},
            {name:'fieldData'},
            {name:'fieldtype'},
            {name:'fieldName'},
        ]);
        
        this.contractCustomStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getFieldParams.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractCustomStoreRec)
        });
        this.contractCustomStore.load({
            params: {
                    moduleid: this.isNormalContract?Wtf.Acc_Contract_ModuleId: Wtf.Acc_Lease_Contract,//ERP-24144
                    jeId: this.record.data.billid,
                    customcolumn:0,
                    isActivated:1
             }
        });
        
        var Customtpl = new Wtf.XTemplate(
            '<table border="0" width="60%" style="padding-left:5%;">',
            '<tpl for=".">', 
            '<tr><td class="leadDetailTD" style="color:#15428B;">{fieldlabel}: </td><td class="leadDetailTD">{fieldName}</td></tr>',
            '</tpl></table>'
            );
            
            
//============================================= For Contracts  Details Right Data View =======================================================            
            
        this.contractStore2Rec= Wtf.data.Record.create([
            {name:'cid'},
//            {name:'contract'},
            {name:'to'},
            {name:'from', type:'date'},
            {name:'status'},
            {name:'contractTerm'},
            {name:'lastRenewedDate'},
            {name:'originalEndDate', type:'date'},
            {name:'signInDate', type:'date'},
            {name:'moveInDate', type:'date'},
            {name:'moveOutDate', type:'date'}
        ]);
        
        this.contractStore2 = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractOtherDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractStore2Rec)
        });
        this.contractStore2.load({
            params: {
                contractid: this.record.data.cid
            }
        });

        var tpl2 = new Wtf.XTemplate(
            '<table border="0" width="60%" style="padding-left:5%;padding-top:5%;">',
            '<tpl for=".">',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">Contract#: </td><td class="leadDetailTD">{cid}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.From")+' : </td><td class="leadDetailTD">{from:date("m/d/Y")}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.To")+' : </td><td class="leadDetailTD">{to:date("m/d/Y")}</td></tr>',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.Status")+' : </td><td class="leadDetailTD">{status}</td></tr>',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.ContractTerm")+' : </td><td class="leadDetailTD">{contractTerm}</td></tr>',
//            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.LastRenewedDate")+' : </td><td class="leadDetailTD">{lastRenewedDate:date("m/d/Y")}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.OriginalEndDate")+' : </td><td class="leadDetailTD">{originalEndDate:date("m/d/Y")}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.SignInDate")+' : </td><td class="leadDetailTD">{signInDate:date("m/d/Y")}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.MoveInDate")+' : </td><td class="leadDetailTD">{moveInDate:date("m/d/Y")}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.MoveOutDate")+' : </td><td class="leadDetailTD">{moveOutDate:date("m/d/Y")}</td></tr>',
            '</tpl></table>'
        );
        
        
//============================================= For Contracts Normal Invoices Details =======================================================

        this.contractStore5 = new Wtf.data.SimpleStore({
            fields: ['id','contractDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl5 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:6%;">',
            '<tpl for=".">',           
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.NormalInvoiceDetails")+' : </td><td class="leadDetailTD">{contractDetails}</td></tr>',
            '</tpl></table>'           
        );
                
        this.quickSearchTF = new Wtf.KWLTagSearch({
            width: 220,
            emptyText:"",
            id:'quick8', // In use,do not delete.
            Store:this.EditorStore,
            parentGridObj: this
        });
        this.toolbarItems = [];
        this.toolbarItems.push(this.quickSearchTF);
       
        this.contractDetailsRec = Wtf.data.Record.create([
            {name:'cid'},
            {name:'documentID'},
            {name:'documentNumber'},
            {name:'description'},
            {name:'date', type:'date'},
            {name:'amount'}
        ]);
        this.contractDetailsStore = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractNormalInvoiceDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractDetailsRec)
        });
        this.contractDetailsStore.load({
            params: {
                contractid: this.record.data.cid,
                isNormalContract:this.isNormalContract
            }
        });
        
        this.contractDetailsStore.on('load', function() {
            this.totalAmount = 0;
            var i;
            for(i=0; i < this.contractDetailsStore.getCount(); i++) {
                this.totalAmount += this.contractDetailsStore.getAt(i).get('amount');
            }
            this.soAmount = this.soAmount - this.totalAmount;
            
            var dataValue=[[this.record.data.cid, this.soAmount, this.record.data.currencysymbol]];
            this.contractStore3.loadData(dataValue);
            
        },this);
        
        this.contractDetailsGrid = new Wtf.grid.GridPanel({
            store:this.contractDetailsStore,
            id:"gridmsgsd",
            border:false,
            layout:'fit',
            height:200,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText: ""
            },
            forceFit:true,
            columns:[
                {
                    hidden:true,
                    dataIndex:'cid'
                },{
                    header:WtfGlobal.getLocaleText("acc.invoice.gridInvNo"), // 'Invoice No.',
                    dataIndex:'documentNumber'
                },{
                    header:(this.isNormalContract==true)?WtfGlobal.getLocaleText("acc.het.203"):WtfGlobal.getLocaleText("acc.contractActivityPanel.Description"), // 'Description',
                    dataIndex:'description'
                },{
                    header:WtfGlobal.getLocaleText("acc.stockLedger.Date"), // 'Date',
                    dataIndex:'date',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // 'Amount',
                    dataIndex:'amount',
                    align:'right',
                    renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
                }]
        });
        
        
        
        
//============================================= For Contracts Renew Details =======================================================

        this.contractStore9 = new Wtf.data.SimpleStore({
            fields: ['id','contractDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl9 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:6%;">',
            '<tpl for=".">',           
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.field.contractRenewDetails")+' : </td><td class="leadDetailTD">{contractDetails}</td></tr>',
            '</tpl></table>'           
        );
       
        this.contractRenewDetailsRec = Wtf.data.Record.create([
            {name:'cid'},
            {name:'startdate', type:'date'},
            {name:'enddate', type:'date'}
        ]);
        this.contractRenewDetailsStore = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractRenewDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractRenewDetailsRec)
        });
        this.contractRenewDetailsStore.load({
            params: {
                contractid: this.record.data.cid
            }
        });
        
        this.contractRenewDetailsGrid = new Wtf.grid.GridPanel({
            store:this.contractRenewDetailsStore,
            id:"gridmsgsd",
            border:false,
            layout:'fit',
            height:200,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText: ""
            },
            forceFit:true,
            columns:[
                {
                    hidden:true,
                    dataIndex:'cid'
                },{
                    header:WtfGlobal.getLocaleText("acc.Lease.contractstartDate"), // 'Contract Start Date.',
                    dataIndex:'startdate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.Lease.contractendDate"), // 'Contract End Date',
                    dataIndex:'enddate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                }]
        });
        
        
        
//============================================= For Contracts Outstanding Amount Details =======================================================

        this.contractStore3 = new Wtf.data.SimpleStore({
            fields: ['cid','outstandingAmount','currencysymbol'],
            data : [
                ["","",""]
            ]
        });

        var tpl3 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:50%;padding-top:1%;">',
            '<tpl for=".">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.OutstandingAmount")+' : </td><td class="leadDetailTD">{currencysymbol} {outstandingAmount}</td></tr>',
            '</tpl></table>'
        );
            
        
        
//============================================= For Contracts Replacement Invoices Details =======================================================

        this.contractStore6 = new Wtf.data.SimpleStore({
            fields: ['id','replaceInvDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl6 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:6%;">',
            '<tpl for=".">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.ReplacementInvoiceDetails")+' : </td><td class="leadDetailTD">{replaceInvDetails}</td></tr>',
            '</tpl></table>'
        );
        
        this.contractReplacementInvoiceRec = Wtf.data.Record.create([
            {name:'cid'},
            {name:'documentID'},
            {name:'documentNumber'},
            {name:'description'},
            {name:'date', type:'date'},
            {name:'amount'}
        ]);
        this.contractReplacementInvoiceStore = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractReplacementInvoiceDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractReplacementInvoiceRec)
        });
        this.contractReplacementInvoiceStore.load({
            params: {
                contractid: this.record.data.cid
            }
        });
        
        this.contractReplacementInvoiceGrid = new Wtf.grid.GridPanel({
            store:this.contractReplacementInvoiceStore,
            border:false,
            layout:'fit',
            height:200,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText: ""
            },
            forceFit:true,
            columns:[
                {
                    hidden:true,
                    dataIndex:'cid'
                },{
                    header:WtfGlobal.getLocaleText("acc.invoice.gridInvNo"), // 'Invoice No.',
                    dataIndex:'documentNumber'
                },{
                    header:(this.isNormalContract==true)?WtfGlobal.getLocaleText("acc.het.203"):WtfGlobal.getLocaleText("acc.contractActivityPanel.Description"), // 'Description',
                    dataIndex:'description'
                },{
                    header:WtfGlobal.getLocaleText("acc.stockLedger.Date"), // 'Date',
                    dataIndex:'date',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // 'Amount',
                    dataIndex:'amount',
                    align:'right',
                    renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
                }]
        });
        
        
        
//============================================= For Contracts Maintenance Invoices Details =======================================================

        this.contractStore7 = new Wtf.data.SimpleStore({
            fields: ['id','maintenanceInvDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl7 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:6%;">',
            '<tpl for=".">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.MaintenanceInvoiceDetails")+' : </td><td class="leadDetailTD">{replaceInvDetails}</td></tr>',
            '</tpl></table>'
        );
        
        this.contractMaintenanceInvoiceRec = Wtf.data.Record.create([
            {name:'documentID'},
            {name:'documentNumber'},
            {name:'description'},
            {name:'date', type:'date'},
            {name:'amount'}
        ]);
        this.contractMaintenanceInvoiceStore = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractMaintenanceInvoiceDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractMaintenanceInvoiceRec)
        });
        this.contractMaintenanceInvoiceStore.load({
            params: {
                contractid: this.record.data.cid
            }
        });
        
        this.contractMaintenanceInvoiceGrid = new Wtf.grid.GridPanel({
            store:this.contractMaintenanceInvoiceStore,
            border:false,
            layout:'fit',
            height:200,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText: ""
            },
            forceFit:true,
            columns:[
                {
                    header:WtfGlobal.getLocaleText("acc.invoice.gridInvNo"), // 'Invoice No.',
                    dataIndex:'documentNumber'
                },{
                    header:(this.isNormalContract==true)?WtfGlobal.getLocaleText("acc.het.203"):WtfGlobal.getLocaleText("acc.contractActivityPanel.Description"), // 'Description',
                    dataIndex:'description'
                },{
                    header:WtfGlobal.getLocaleText("acc.stockLedger.Date"), // 'Date',
                    dataIndex:'date',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // 'Amount',
                    dataIndex:'amount',
                    align:'right',
                    renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
                }]
        });
        
        
        
        
        
//============================================= For Contracts Sales Return Details =======================================================

        
        this.contractSalesReturnRec = Wtf.data.Record.create([
            {name:'documentID'},
            {name:'documentNumber'},
            {name:'description'},
            {name:'date', type:'date'},
            {name:'amount'}
        ]);
        this.contractSalesReturnStore = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractSalesReturnDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractSalesReturnRec)
        });
        this.contractSalesReturnStore.load({
            params: {
                contractid: this.record.data.cid
            }
        });
        
        this.contractSalesReturnGrid = new Wtf.grid.GridPanel({
            store:this.contractSalesReturnStore,
            border:false,
            layout:'fit',
            height:200,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText: ""
            },
            forceFit:true,
            columns:[
                {
                    header:WtfGlobal.getLocaleText("acc.MailWin.srmsg7"), // 'Sales Return Number',
                    dataIndex:'documentNumber'
                },{
                    header:(this.isNormalContract==true)?WtfGlobal.getLocaleText("acc.het.203"):WtfGlobal.getLocaleText("acc.contractActivityPanel.Description"), // 'Description',
                    dataIndex:'description'
                },{
                    header:WtfGlobal.getLocaleText("acc.stockLedger.Date"), // 'Date',
                    dataIndex:'date',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.invoice.gridAmount"), // 'Amount',
                    dataIndex:'amount',
                    align:'right',
                    renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
                }]
        });
        
        
        
//============================================= For Contracts Normal DO Item Details =======================================================


        this.contractStore4 = new Wtf.data.SimpleStore({
            fields: ['cid','itemsDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl4 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:6%;">',
            '<tpl for=".">',           
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.NormalDOItemsDetails")+' : </td><td class="leadDetailTD">{itemsDetails}</td></tr>',
            '</tpl></table>'           
        );
        
        this.expandNormalDOItemRec = Wtf.data.Record.create ([
            {name:'srid'},
            {name:'srname'},
            {name:'batchname'},
            {name:'warrentyExpireyDate', type:'date'},
            {name:'vendorWarrentyDate'}
        ]);
        this.expandNormalDOItemStoreUrl = "ACCSalesOrderCMN/getContractNormalDOItemDetailsRow.do";

        this.expandNormalDOItemStore = new Wtf.data.Store({
            url:this.expandNormalDOItemStoreUrl,
            baseParams:{
                mode:14,
                dtype : 'report' // Display type report/transaction, used for quotation
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.expandNormalDOItemRec)
        });

        this.expanderNormalDOItem = new Wtf.grid.RowExpander({});

        this.expandNormalDOItemStore.on('load',this.fillNormalDOItemExpanderBody,this);
        this.expanderNormalDOItem.on("expand",this.onNormalDOItemRowexpand,this);
    
    
        this.contractDetailsRec2= Wtf.data.Record.create([
            {name:'pid'},
            {name:'itemName'},
            {name:'doid'},
            {name:'itemCode'},
            {name:'itemDescription'},
            {name:'quantity'},
            {name:'uomname'}
        ]);
        this.contractDetailsStore2 = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractNormalDOItemDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractDetailsRec2)
        });
        this.contractDetailsStore2.load({
            params: {
                contractid: this.record.data.cid,
                isNormalContract:this.isNormalContract
            }
        });
        
        this.contractDetailsGrid2 = new Wtf.grid.GridPanel({
            store:this.contractDetailsStore2,
            id:"gridmsgsd2",
            border:false,
            layout:'fit',
            height:200,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText:""
            },
            plugins:this.expanderNormalDOItem,
            forceFit:true,
            columns:[this.expanderNormalDOItem, {
                    hidden:true,
                    dataIndex:'pid'
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ItemName"), // 'Item Name',
                    dataIndex:'itemName',
                    pdfwidth:35
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ItemCode"), // 'Item Code',
                    dataIndex:'itemCode',
                    pdfwidth:35
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ItemDescription"), // 'Item Description',
                    dataIndex:'itemDescription',
                    pdfwidth:150
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ItemQuantity"), // 'Item Quantity',
                    dataIndex:'quantity',
                    align:'right',
                    renderer:this.unitRenderer,
                    pdfwidth:150
                }]
        });
        
        
        
        
//============================================= For Contracts Replacement DO Item Details =======================================================

        this.contractStore8 = new Wtf.data.SimpleStore({
            fields: ['cid','itemsDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl8 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:6%;">',
            '<tpl for=".">',           
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.ReplacementDOItemsDetails")+' : </td><td class="leadDetailTD">{itemsDetails}</td></tr>',
            '</tpl></table>'           
        );

        this.expandReplacementDOItemRec = Wtf.data.Record.create ([
            {name:'srid'},
            {name:'srname'},
            {name:'batchname'},
            {name:'warrentyExpireyDate', type:'date'},
            {name:'vendorWarrentyDate'}
        ]);
        this.expandReplacementDOItemStoreUrl = "ACCSalesOrderCMN/getContractReplacementDOItemDetailsRow.do";

        this.expandReplacementDOItemStore = new Wtf.data.Store({
            url:this.expandReplacementDOItemStoreUrl,
            baseParams:{
                mode:14,
                dtype : 'report' // Display type report/transaction, used for quotation
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.expandReplacementDOItemRec)
        });

        this.expanderReplacementDOItem = new Wtf.grid.RowExpander({});

        this.expandReplacementDOItemStore.on('load',this.fillReplacementDOItemExpanderBody,this);
        this.expanderReplacementDOItem.on("expand",this.onReplacementDOItemRowexpand,this);
    
    
        this.contractDetailsRec3= Wtf.data.Record.create([
            {name:'pid'},
            {name:'itemName'},
            {name:'productReplacementID'},
            {name:'dateOfReplacement', type:'date'},
            {name:'itemCode'},
            {name:'itemDescription'},
            {name:'quantity'},
            {name:'uomname'}
        ]);
        this.contractDetailsStore3 = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getContractReplacementDOItemDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:"totalCount",
                root: "data"
            },this.contractDetailsRec3)
        });
        this.contractDetailsStore3.load({
            params: {
                contractid: this.record.data.cid
            }
        });
        
        this.contractDetailsGrid3 = new Wtf.grid.GridPanel({
            store:this.contractDetailsStore3,
            id:"gridmsgsd2",
            border:false,
            layout:'fit',
            height:200,
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText:""
            },
            plugins:this.expanderReplacementDOItem,
            forceFit:true,
            columns:[this.expanderReplacementDOItem, {
                    hidden:true,
                    dataIndex:'pid'
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ItemName"), // 'Item Name',
                    dataIndex:'itemName',
                    pdfwidth:35
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.dateOfReplacement"), // 'Date of Replacement',
                    dataIndex:'dateOfReplacement',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer,
                    pdfwidth:35
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ItemCode"), // 'Item Code',
                    dataIndex:'itemCode',
                    pdfwidth:35
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ItemDescription"), // 'Item Description',
                    dataIndex:'itemDescription',
                    pdfwidth:150
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ItemQuantity"), // 'Item Quantity',
                    dataIndex:'quantity',
                    align:'right',
                    renderer:this.unitRenderer,
                    pdfwidth:150
                }]
        });
        
       this.contractCustomDataview = new Wtf.DataView({
            store: this.contractCustomStore,
            tpl: Customtpl,
            autoHeight:true,
            multiSelect: true,
            columnWidth:.50,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap'
//            emptyText: 'No images to display'
        })

//=============================================== For Defining All Data View ============================================================

        this.contractDataview = new Wtf.DataView({
            store: this.contractStore,
            tpl: tpl,
//            autoHeight:true,
            multiSelect: true,
            columnWidth:.50,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
            
        this.contractDataview2 = new Wtf.DataView({
            store: this.contractStore2,
            tpl: tpl2,
//            autoHeight:true,
            multiSelect: true,
            columnWidth:.50,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
            
        this.contractDataview3 = new Wtf.DataView({
            store: this.contractStore3,
            tpl: tpl3,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
       
        this.contractDataview4 = new Wtf.DataView({
            store: this.contractStore4,
            tpl: tpl4,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
       
        this.contractDataview5 = new Wtf.DataView({
            store: this.contractStore5,
            tpl: tpl5,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
        
        this.contractDataview6 = new Wtf.DataView({
            store: this.contractStore6,
            tpl: tpl6,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
        
        this.contractDataview7 = new Wtf.DataView({
            store: this.contractStore7,
            tpl: tpl7,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
        
        this.contractDataview8 = new Wtf.DataView({
            store: this.contractStore8,
            tpl: tpl8,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
        
        this.contractDataview9 = new Wtf.DataView({
            store: this.contractStore9,
            tpl: tpl9,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No images to display'
        })
        


//=============================================== For Defining All Field Sets ============================================================
        
        
        this.fs1=new Wtf.form.FieldSet({
            width:1180,
            autoHeight:true,
//            height:250,
            title:WtfGlobal.getLocaleText("acc.contractDetails.tabTitle"), // 'Contract Details',
            layout:'column',
            items:[
                this.contractDataview,
                this.contractDataview2,
            ]
        });
        this.fs11=new Wtf.form.FieldSet({
            width:1180,
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.field.OtherFields"), // 'Contract Details',
            layout:'column',
            items:[
                this.contractCustomDataview
            ]
        });
        
        this.fs2=new Wtf.form.FieldSet({
            width:1180,
            height:270,
            title:WtfGlobal.getLocaleText("acc.field.contractRenewDetails"), // 'Contract Renew Details',
            items:[
                this.contractRenewDetailsGrid
            ]
        });
        
        this.fs3=new Wtf.form.FieldSet({
            width:1180,
            height:330,
            title:WtfGlobal.getLocaleText("acc.contractDetails.NormalInvoiceDetails"), // 'Invoice Details',
            items:[
                this.contractDetailsGrid,
                this.contractDataview3
            ]
        });
        
        this.fs4=new Wtf.form.FieldSet({
            width:1180,
            height:270,
            title:WtfGlobal.getLocaleText("acc.contractDetails.ReplacementInvoiceDetails"), // 'Replacement Invoice Details',
            items:[
                this.contractReplacementInvoiceGrid
            ]
        });
        
        this.fs5=new Wtf.form.FieldSet({
            width:1180,
            height:270,
            title:WtfGlobal.getLocaleText("acc.contractDetails.MaintenanceInvoiceDetails"), // 'Maintenance Invoice Details',
            items:[
                this.contractMaintenanceInvoiceGrid
            ]
        });
        
        this.fs6=new Wtf.form.FieldSet({
            width:1180,
            height:270,
            title:WtfGlobal.getLocaleText("acc.contractDetails.NormalDOItemsDetails"), // 'Delivery Order Items Details',
            items:[
                this.contractDetailsGrid2
            ]
        });
        
        this.fs7=new Wtf.form.FieldSet({
            width:1180,
            height:270,
            title:WtfGlobal.getLocaleText("acc.contractDetails.ReplacementDOItemsDetails"), // 'Replacement Delivery Order Items Details',
            items:[
                this.contractDetailsGrid3
            ]
        });
        
        this.fs8=new Wtf.form.FieldSet({
            width:1180,
            height:270,
            title:WtfGlobal.getLocaleText("acc.field.SalesReturnDetails"), // 'Sales Return Details',
            items:[
                this.contractSalesReturnGrid
            ]
        });
        
        

//=============================================== For Adding All Data View ============================================================
       
        this.add({
            layout:"table",
            layoutConfig: {
                columns: 1
            },
            autoWidth:true,
            autoScroll:true,
            bodyStyle:'overflow-y: scroll',
//            bbar:new Wtf.Toolbar(this.toolItems),
            items:[
                {
                    colspan: 1,
                    width:1200,
                    height:250,
                    border:false,
                    items:this.fs1,
                    bodyStyle:"margin-left:20px;margin-top:30px;margin-bottom:20px;"
                },{
                    colspan: 1,
                    width:1200,
                    height:250,
                    border:false,
                    items:this.fs11,
                    bodyStyle:"margin-left:20px;margin-top:0px;margin-bottom:10px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    height:300,
                    border:false,
                    items:this.fs2,
                    bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                },
                {
                    colspan: 1,
                    width:1200,
                    height:350,
                    border:false,
                    items:this.fs3,
                    bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    height:300,
                    border:false,
                    items:this.fs4,
                    bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    height:300,
                    border:false,
                    items:this.fs5,
                    bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    height:300,
                    border:false,
                    items:this.fs8,
                    bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    height:300,
                    border:false,
                    items:this.fs6,
                    bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    height:300,
                    border:false,
                    items:this.fs7,
                    bodyStyle:"margin-left:20px;margin-bottom:20px;overflow-y: scroll"
                }]

        });
    },
    
        
//=============================================== For renderere of quantity  ============================================================    

    
    unitRenderer:function(value,metadata,record){
        if(record.data['type'] == "Service"){
            return "N/A";
        }
        var unit=record.data['uomname'];
        value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;
        return value;
    },
    
    
//=============================================== For Normal DO Item Expand View ============================================================    
    
    onNormalDOItemRowexpand:function(scope, record, body){
        this.expanderNormalDOItemBody=body;
        this.expandNormalDOItemStore.load({
            params: {
                pid: record.data.pid,
                doid: record.data.doid,
                contractid: this.record.data.cid
            }
        });
    },
    
    fillNormalDOItemExpanderBody: function() {
        if(this.expandNormalDOItemStore.getCount() > 0) {
            var arr = [
                "Serial Number",
                "Batch",
                "Warranty Expiry Date",
                "Vendor Warranty"
            ];
            
            var gridHeaderText = "Product Details";
        
            var header = "<span class='gridHeader'>"+gridHeaderText+"</span>"; // "Item Details"
        
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>"; // "S.No."
            for(var i=0; i<arr.length; i++){
                header += "<span class='headerRow' style='width: 10% ! important;'>" + arr[i] + "</span>";
            }
            header += "<span class='gridLine'></span>";
        
            for(i=0; i<this.expandNormalDOItemStore.getCount(); i++) {
                var rec=this.expandNormalDOItemStore.getAt(i);
            
                // Column : S.No.
                header += "<div style='width:100%;float:left;'><span class='gridNo'>"+(i+1)+".</span>";            
                // Column : Serial Number
                header += "<span class='gridRow'  wtf:qtip='"+rec.data['srname']+"' style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['srname'],10)+"</span>";
                // Column : Batch
                header += "<span class='gridRow' wtf:qtip='"+rec.data['batchname']+"' style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['batchname'],10)+"</span>";
                // Column : Warrenty Expirey Date
               if(rec.data['warrentyExpireyDate'])
                    header += "<span class='gridRow' wtf:qtip='"+rec.data['warrentyExpireyDate']+"'>"+Wtf.util.Format.ellipsis(rec.data['warrentyExpireyDate'].format(WtfGlobal.getOnlyDateFormat()),10)+"</span>";  
                   
                else
                    header += "<span class='gridRow' ></span>";
                // Column : Vendor Warrenty
                header += "<span class='gridRow' wtf:qtip='"+rec.data['vendorWarrentyDate']+"' style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['vendorWarrentyDate'],10)+"</span>";
            
                header += "</div>";
            }
        
            var disHtml = "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
            this.expanderNormalDOItemBody.innerHTML = disHtml;
        } else {
            this.expanderNormalDOItemBody.innerHTML = "<div class='expanderContainer' style='width:100%'>" + WtfGlobal.getLocaleText("acc.field.Nodatatodisplay") + "</div>"; // "No data to display"
        }
    },
    
    
//=============================================== For Replacement DO Item Expand View ============================================================        
    
    onReplacementDOItemRowexpand:function(scope, record, body){
        this.expanderReplacementDOItemBody=body;
        this.expandReplacementDOItemStore.load({
            params: {
                pid: record.data.pid,
                productReplacementID: record.data.productReplacementID,
                contractid: this.record.data.cid
            }
        });
    },
    
    fillReplacementDOItemExpanderBody: function() {
        if(this.expandReplacementDOItemStore.getCount() > 0) {
            var arr = [
                "Serial Number",
                "Batch",
                "Warranty Expiry Date",
                "Vendor Warranty"
            ];
            
            var gridHeaderText = "Product Details";
        
            var header = "<span class='gridHeader'>"+gridHeaderText+"</span>"; // "Item Details"
        
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>"; // "S.No."
            for(var i=0; i<arr.length; i++){
                header += "<span class='headerRow'>" + arr[i] + "</span>";
            }
            header += "<span class='gridLine'></span>";
        
            for(i=0; i<this.expandReplacementDOItemStore.getCount(); i++) {
                var rec=this.expandReplacementDOItemStore.getAt(i);
            
                // Column : S.No.
                header += "<div style='width:100%;float:left;'><span class='gridNo'>"+(i+1)+".</span>";            
                // Column : Serial Number
                header += "<span class='gridRow' wtf:qtip='"+rec.data['srname']+"'>"+Wtf.util.Format.ellipsis(rec.data['srname'],10)+"</span>";
                // Column : Batch
                header += "<span class='gridRow' wtf:qtip='"+rec.data['batchname']+"'>"+Wtf.util.Format.ellipsis(rec.data['batchname'],10)+"</span>";
                // Column : Warrenty Expirey Date
                if(rec.data['warrentyExpireyDate'])
                    header += "<span class='gridRow' wtf:qtip='"+rec.data['warrentyExpireyDate']+"'>"+Wtf.util.Format.ellipsis(rec.data['warrentyExpireyDate'].format(WtfGlobal.getOnlyDateFormat()),10)+"</span>";  
                   
                else
                    header += "<span class='gridRow' ></span>";
                // Column : Vendor Warrenty
                header += "<span class='gridRow' wtf:qtip='"+rec.data['vendorWarrentyDate']+"'>"+Wtf.util.Format.ellipsis(rec.data['vendorWarrentyDate'],10)+"</span>";
            
                header += "</div>";
            }
        
            var disHtml = "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
            this.expanderReplacementDOItemBody.innerHTML = disHtml;
        } else {
            this.expanderReplacementDOItemBody.innerHTML = "<div class='expanderContainer' style='width:100%'>" + WtfGlobal.getLocaleText("acc.field.Nodatatodisplay") + "</div>"; // "No data to display"
        }
    }
});
