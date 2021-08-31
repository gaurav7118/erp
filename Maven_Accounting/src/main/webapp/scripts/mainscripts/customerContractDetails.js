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

Wtf.account.CustomerContractDetails = function(config){
    this.record = config.record;
    Wtf.apply(this, config);
    Wtf.account.CustomerContractDetails.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.account.CustomerContractDetails, Wtf.Panel, {
    initComponent: function(){
        Wtf.account.CustomerContractDetails.superclass.initComponent.call(this);

        this.image1 = document.createElement('img');
        if(Wtf.isGecko)
            this.image1.className = "tabimageG";
        else
            this.image1.className = "tabimage";



//================================================== For Customer Details View =========================================================

        this.contractStore = new Wtf.data.SimpleStore({
            fields: ['id','accountname','addresss'],
            data : [
                ["1","krawler","Pune"]
            ]
        });
        
        var dataValue=[["1",this.record.data.accname,this.record.data.billingAddress1]];
        this.contractStore.loadData(dataValue);

        var tpl = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:5%;padding-top:6%;">',
            '<tpl for=".">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.contractDetails.CustomerName")+' : </td><td class="leadDetailTD">{accountname}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.customerContractDetails.AddressDetails")+' : </td><td class="leadDetailTD">{addresss}</td></tr>',
            '</tpl></table>'
        );

        this.contractDataview = new Wtf.DataView({
            store: this.contractStore,
            tpl: tpl,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: WtfGlobal.getLocaleText("acc.common.no.images")
        });
        
        this.quickSearchTF = new Wtf.KWLTagSearch({
            width: 220,
            emptyText:"",
            id:'quick8', // In use,do not delete.
            Store:this.EditorStore,
            parentGridObj: this
        });
        this.toolbarItems = [];
        this.toolbarItems.push(this.quickSearchTF);
        
        
//================================================== For Contract Agreement Details =========================================================

        this.contractStore5 = new Wtf.data.SimpleStore({
            fields: ['id','contractDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl5 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:2%;padding-top:3%;">',
            '<tpl for=".">',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.customerContractDetails.ContractAgreementDetails")+' : </td><td class="leadDetailTD">{contractDetails}</td></tr>',
            '</tpl></table>'          
        );
                
        this.contractStore5view = new Wtf.DataView({
            store: this.contractStore5,
            tpl: tpl5,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: WtfGlobal.getLocaleText("acc.common.no.images")
        });        

        
        this.leaseAgreementRec= Wtf.data.Record.create([
            {name:'contractid'},
            {name:'contactperson'},
            {name:'agreementtype'},
            {name:'fromdate', type:'date'},
            {name:'todate', type:'date'},
            {name:'status'},
            {name:'leaseterm'},
            {name:'lastrenewdate', type:'date'},
            {name:'orgenddate', type:'date'},
            {name:'signindate', type:'date'},
            {name:'moveindate', type:'date'},
            {name:'moveoutdate', type:'date'}
        ]);
        
        this.leaseAgreementStore = new Wtf.data.Store({
            url: "ACCSalesOrderCMN/getCustomerContractsAgreementDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.leaseAgreementRec)
        });
        this.leaseAgreementStore.load({
            params:{
                customerid:this.record.data.accid
            }
        });
        
        this.leaseAgreementGrid = new Wtf.grid.GridPanel({
            store:this.leaseAgreementStore,
            id:"gridmsgsd",
            border:false,
            height:150,
            layout:'fit',
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText: "" 
            },
            forceFit:true,
            columns:[
                {
                    header:WtfGlobal.getLocaleText("acc.Lease.ContractID"), // 'Contract ID',
                    dataIndex:'contractid'
                },{
                    header:WtfGlobal.getLocaleText("acc.address.ContactPerson"), //  'Contact Person',
                    dataIndex:'contactperson'
                },{
//                    header:'Agreement Type',
//                    dataIndex:'agreementtype'
//                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.From"), // 'From',
                    dataIndex:'fromdate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.To"), // 'To',
                    dataIndex:'todate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.Status"), // 'Status',
                    dataIndex:'status'
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.ContractTerm"), // 'Contract Term',
                    dataIndex:'leaseterm'
                },{
//                    header:'Last Renewed Date',
//                    dataIndex:'lastrenewdate',
//                    renderer:WtfGlobal.onlyDateDeletedRenderer
//                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.OriginalEndDate"), // 'Original End Date',
                    dataIndex:'orgenddate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.SignInDate"), // 'Sign In Date',
                    dataIndex:'signindate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.MoveInDate"), // 'Move In Date',
                    dataIndex:'moveindate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.MoveOutDate"), // 'Move Out Date',
                    dataIndex:'moveoutdate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                }]
        });
        
    
//================================================== For Cost Agreement Details =========================================================
    
        this.contractStore6 = new Wtf.data.SimpleStore({
            fields: ['id','contractDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl6 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:2%;padding-top:3%;">',
            '<tpl for=".">',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.customerContractDetails.CostAgreementDetails")+' : </td><td class="leadDetailTD">{contractDetails}</td></tr>',
            '</tpl></table>'          
        );
                
        this.contractStore6view = new Wtf.DataView({
            store: this.contractStore6,
            tpl: tpl6,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: WtfGlobal.getLocaleText("acc.common.no.images")
        });
        
        this.costAgreementRec= Wtf.data.Record.create([
            {name:'contractid'},
            {name:'leaseamount'},
            {name:'securitydepos'},
            {name:'outstandings'},
            {name:'monthlyrent'},
            {name:'currencysymbol'}
        ]);
        
        this.costAgreementStore = new Wtf.data.Store({
            url: "ACCSalesOrderCMN/getCustomerContractCostAgreementDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'totalCount'
            },this.costAgreementRec)
        });
        this.costAgreementStore.load({
            params:{
                customerid:this.record.data.accid
            }
        });
        
        this.costAgreementGrid = new Wtf.grid.GridPanel({
            store:this.costAgreementStore,
            id:"gridmsgsd",
            border:false,
            height:150,
            layout:'fit',
            loadMask:true,
            viewConfig:{
                forceFit:true,
                emptyText: ""
            },
            forceFit:true,
            columns:[
                {
                    header:WtfGlobal.getLocaleText("acc.Lease.ContractID"), // 'Contract ID',
                    dataIndex:'contractid'
                },{
                    header:WtfGlobal.getLocaleText("acc.Lease.LeaseAmount"), // 'Contract Amount',
                    dataIndex:'leaseamount',
                    align:'right',
                    renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
                },{
//                    header:'Security Deposit',
//                    dataIndex:'securitydepos'
//                },{
                    header:WtfGlobal.getLocaleText("acc.contractDetails.OutstandingAmount"), // 'Outstanding Amount',
                    dataIndex:'outstandings',
                    align:'right',
                    renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
//                },{
//                    header:'Monthly Rent',
//                    dataIndex:'monthlyrent'
                }]
                });
        
        
    
//================================================== For Services Agreement Details =========================================================
    
        this.contractStore7 = new Wtf.data.SimpleStore({
            fields: ['id','contractDetails'],
            data : [
                ["1",""]
            ]
        });

        var tpl7 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:2%;padding-top:3%;">',
            '<tpl for=".">',          
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.customerContractDetails.ServicesAgreementDetails")+' : </td><td class="leadDetailTD">{contractDetails}</td></tr>',
            '</tpl></table>'          
        );    
                
        this.contractStore7view = new Wtf.DataView({
            store: this.contractStore7,
            tpl: tpl7,
            autoHeight:true,
            multiSelect: true,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: WtfGlobal.getLocaleText("acc.common.no.images")
        });        
    
        this.servicesAgreementRec= Wtf.data.Record.create([
            {name:'contractid'},
            {name:'agreedservices'},
            {name:'nextservicedate', type:'date'},
            {name:'lastservicedate', type:'date'},
            {name:'oncallservices'},
            {name:'ongoingservices'}
        ]);
   
        this.servicesAgreementStore = new Wtf.data.Store({
            url: "ACCSalesOrderCMN/getCustomerContractsServiceAgreementDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'totalCount'
            },this.servicesAgreementRec)
        });
        this.servicesAgreementStore.load({
            params:{
                customerid:this.record.data.accid
            }
        });
        
        this.servicesAgreementGrid = new Wtf.grid.GridPanel({
            store:this.servicesAgreementStore,
            id:"gridmsgsd",
            border:false,
            height:150,
            layout:'fit',
            loadMask:true,
            viewConfig:{forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.empty")) //Empty
            },
            forceFit:true,
            columns:[
                {
                    header:WtfGlobal.getLocaleText("acc.Lease.ContractID"), // 'Contract ID',
                    dataIndex:'contractid'
                },{
                    header:WtfGlobal.getLocaleText("acc.Lease.agreedservices"), // 'No. of Agreed Services',
                    dataIndex:'agreedservices',
                    align:'right'
                },{
                    header:WtfGlobal.getLocaleText("acc.customerContractDetails.NextServiceDate"), // 'Next Service Date',
                    dataIndex:'nextservicedate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                    header:WtfGlobal.getLocaleText("acc.customerContractDetails.LastServiceDate"), // 'Last Service Date',
                    dataIndex:'lastservicedate',
                    align:'center',
                    renderer:WtfGlobal.onlyDateDeletedRenderer
//                },{
//                    header:'On Call Services If any',
//                    dataIndex:'oncallservices'
//                },{
//                    header:'On Going Services If any',
//                    dataIndex:'ongoingservices'
                }]
        });
    
//=============================================== For Adding All Data View ============================================================
        
        this.add({
            layout: "table",
            layoutConfig: {
                columns: 1
            },
            autoScroll:true,
            bodyStyle:'overflow-y: scroll',
//            tbar:new Wtf.Toolbar(this.toolbarItems),
//            bbar:new Wtf.Toolbar(this.toolItems),
            items:[
                {
                    colspan: 1,
                    width:300,
                    border:false,
                    items:this.contractDataview,
                    bodyStyle:"margin-bottom:25px"
                },{
                    colspan: 1,
                    width:1200,
                    border:false,
                    items:this.contractStore5view
//                   bodyStyle:"margin-left:0px"
                },{
                    colspan: 1,
                    width:1200,
                    border:false,
                    items:this.leaseAgreementGrid,
                    bodyStyle:"margin-left:20px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    border:false,
                    items:this.contractStore6view
//                    bodyStyle:"margin-bottom:50px"
                },{
                    colspan: 1,
                    width:1200,
                    border:false,
                    items:this.costAgreementGrid,
                    bodyStyle:"margin-left:20px;overflow-y: scroll"
                },{
                    colspan: 1,
                    width:1200,
                    border:false,
                    items:this.contractStore7view
//                    bodyStyle:"margin-bottom:50px"
                },{
                    colspan: 1,
                    width:1200,
                    border:false,
                    items:this.servicesAgreementGrid,
                    bodyStyle:"margin-left:20px;overflow-y: scroll"
                }]
        });
            }
});
