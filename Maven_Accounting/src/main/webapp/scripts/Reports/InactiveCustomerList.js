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

function callInactiveCustomerListDynamicLoad(personlinkid,openperson,withinventory){
    var panel = Wtf.getCmp("InactiveCustomerList");
    if(panel==null){
        panel = new Wtf.account.InactiveCustomerList({
            id : 'InactiveCustomerList',
            withinventory:withinventory,
            border : false,
            openperson:openperson,
            personlinkid:personlinkid,
            moduleId:Wtf.Acc_Customer_ModuleId,
            layout: 'fit',
            closable:true,
            isCustomer:true,
            title:WtfGlobal.getLocaleText("acc.rem.14.1"),  //'Customer List',
            tabTip: WtfGlobal.getLocaleText("acc.erp.viewinactivecustomerlist"),
            iconCls :getButtonIconCls(Wtf.etype.customer)
        });

        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

//*****************************************************************************************
Wtf.account.InactiveCustomerList=function(config){
    this.withinventory=config.withinventory||false;
    this.summary = new Wtf.ux.grid.GridSummary();
    this.personlinkid=config.personlinkid;
    this.perAccID=null;
    this.openperson=config.openperson;
    this.isEdit=false;
    this.recArr=[];
    this.isAdd=false;
    this.nondeleted=false;
    this.deleted=false;
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.uPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.moduleId=config.moduleId;
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    this.submitBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),
        tooltip :WtfGlobal.getLocaleText("acc.invReport.fetchTT"),  
        id: 'submitRec' + this.id,
        scope: this,
        iconCls:'accountingbase fetch',
        disabled :false
    });
    
    this.submitBttn.on("click", this.submitHandler, this);
    this.GridRec = Wtf.data.Record.create ([
        {name:'accid'},
        {name:'openbalance'},
        {name:'id'},
        {name:'title'},
        {name:'accname'},
        {name:'acccode'},
        {name:'accnamecode'},
        {name:'personname',mapping:'accname'},
        {name:'personemail',mapping:'email'},
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
        {name: 'mappedSalesPersonId'},
        {name:'other'},
        {name: 'leaf'},
        {name: 'currencysymbol'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'istaxeligible'},
        {name: 'deleted'},
        {name: 'creationDate' ,type:'date'},
        {name: 'categoryid'},
        {name:'intercompany',type:'boolean'},
        {name: 'intercompanytypeid'},
        {name: 'taxno'},
        {name: 'level'},
        {name: 'contactperson'},
        {name: 'amountdue'},
        {name: 'mappingaccid'},
        {name: 'country'},
        {name: 'limit'},
        {name: 'billingAddress'},
        {name: 'billingMobileNumber'},
        {name: 'billingEmailID'},
        {name: 'billingPhone'},
        {name: 'shippingAddress'}
    ]);
    this.msgLmt = 30;
    this.jReader = new Wtf.data.KwlJsonReader({
        totalProperty: 'totalCount',
        root: "data"
    }, this.GridRec);
    this.Store = new Wtf.data.Store({
        title:this.businessPerson+" Information",
        url : "ACCCustomerCMN/getInactiveCustomers.do",
        baseParams:{
                 mode:2,
                 startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue())
            },
        reader: this.jReader
    });
    var glblStore=(config.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore);
    glblStore.on('beforeload',function(){this.Store.reload();},this);    
    this.Store.on('load',this.hideMsg,this);
    this.Store.on('loadexception',this.hideMsg,this);

    this.Store.load({
        params: {
            start: 0,
            startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
        }
    });
     this.Store.on('beforeload', function(){
        this.Store.baseParams = {
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue())
        }
        
    }, this);
    WtfComMsgBox(29,4,true);
    this.btnArr=[];
    this.bbarBtnArr=[];
    this.btnArrEDSingleS=[]; // Enable/Disable button's indexes on single select
    this.btnArrEDMultiS=[]; // Enable/Disable button's indexes on multi select
      
        this.quickSearchTF = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.field.SearchbyCustomerNameandCustomerID"),
            width: 150,
            field: 'accname',
            Store:this.Store
        });
        this.btnArr.push(this.quickSearchTF);
        this.btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            hidden:this.isSummary,
            tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search '+this.businessPerson+' name'+' by clearing existing search  '+this.businessPerson+' name'+'.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        }));
        this.resetBttn.on('click',this.handleResetClick,this);
        this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.btnArr.push(this.startDate);
        this.btnArr.push(this.submitBttn);
  
    this.grid = new Wtf.grid.GridPanel({
        store:this.Store,
        border:false,
        layout:'fit',
        plugins:[this.summary],
        viewConfig: {
            forceFit: false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        forceFit:false,
        loadMask:true,
        columns:[{
            header:'',
            dataIndex:'accnamecode',
            hidden:true
        }
            ,{
            header:this.businessPerson+WtfGlobal.getLocaleText("acc.field.Code"),
            dataIndex:'acccode',//dataIndex:'accnamecode',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //"Name",
            dataIndex:'accname',//dataIndex:'accnamecode',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Address"),  //"Billing Address",
            dataIndex:'billingAddress',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Email"),  //"Billing Email",
            dataIndex:'billingEmailID',
            pdfwidth:110,
            renderer:WtfGlobal.renderDeletedEmailTo
        },{
            header:WtfGlobal.getLocaleText("acc.address.Billing")+" "+WtfGlobal.getLocaleText("acc.address.Mobile"),  //"Billing Mobile",
            dataIndex:'billingMobileNumber',
            pdfwidth:75,
            renderer:WtfGlobal.renderDeletedContactToSkype
        },{
            header:config.isCustomer?WtfGlobal.getLocaleText("acc.field.CustomerUEN"):WtfGlobal.getLocaleText("acc.field.VendorUEN"),
            dataIndex:'contactno2',
            hidden : (Wtf.account.companyAccountPref.countryid != '203'),
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalance"),  //"Opening Balance",
            dataIndex:'openbalance',
            align:'right',
            hidden:true,
            renderer:this.opBalRenderer,
            pdfwidth:75,
            pdfrenderer:"rowcurrency"				// Opening Balance to be displayed in customer/vendor currency in CSV and pdf Export
        },{
            header: WtfGlobal.getLocaleText("acc.customerList.gridCreationDate"),  //"Creation Date",
            dataIndex: "creationDate",
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.customerList.gridOpeningBalanceType"),  //'Opening Balance Type',
            dataIndex: 'openbalance',
            pdfwidth:75,
            hidden:true,
//            summaryType:'sum',
//            summaryRenderer:this.balTypeRenderer,
            renderer:this.balTypeRenderer
         },{
            header :WtfGlobal.getLocaleText("acc.customerList.gridCurrency"),  //'Currency',
            pdfwidth:75,
            dataIndex: 'currencyname',
            hidden:true,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:(Wtf.account.companyAccountPref.countryid != '203')?WtfGlobal.getLocaleText("acc.customerList.gridOtherInfo"):WtfGlobal.getLocaleText("acc.field.GSTNumber"),  //"Other Information",
            dataIndex:'other',
            renderer:WtfGlobal.deletedRenderer,
            hidden: !Wtf.account.companyAccountPref.withouttax1099,
            pdfwidth:50,
            pdfrenderer:"rowcurrency"
        },
        {
            header:WtfGlobal.getLocaleText("acc.customerList.gridShippingAddress"),  //"Shipping Address",
            dataIndex:'shippingAddress',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:75,
            hidden: !config.isCustomer

        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridTaxIDno"),  //"Tax ID Number",
            dataIndex:'taxidnumber',
            renderer:WtfGlobal.deletedRenderer,
            hidden:config.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
            pdfwidth:125
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.grid1099TaxEligible"),  //" 1099 Tax Eligible",
            dataIndex:'istaxeligible',
            hidden:config.isCustomer||Wtf.account.companyAccountPref.withouttax1099,
            pdfwidth:125
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.gridCreditTerm"):WtfGlobal.getLocaleText("acc.customerList.gridDebitTerm")),  //"Credit":"Debit")+" Term",
            dataIndex:'termname',
            renderer:WtfGlobal.deletedRenderer,
            pdfwidth:125//,
//            hidden: true
        },{
            header:(config.isCustomer?WtfGlobal.getLocaleText("acc.cust.creditLimit"):WtfGlobal.getLocaleText("acc.cust.debitLimit")),  //"Credit":"Debit")+" limit",
            dataIndex:'limit',
            renderer:this.opBalRenderer,
            pdfwidth:100,
            hidden: true
        }]
    });
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
    },this);
    this.pageLimit = new Wtf.forumpPageSize({
        ftree:this.grid
    });
 
    this.pToolBar = new Wtf.PagingSearchToolbar({
        id: 'pgTbar' + this.id,
        pageSize: this.msgLmt,
        store: this.Store,
        searchField: this.quickSearchTF,
        displayInfo: true,
        plugins: this.pageLimit,
        items : this.bbarBtnArr
    });
    this.Store.on("load", this.setPageSize, this);
    this.Store.on('datachanged', function() {
        var p = this.pageLimit.combo.value;
        this.quickSearchTF.setPage(p);
    }, this);
     
this.bbarBtnArr.push('-');


       if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata)) {
       this.bbarBtnArr.push(this.exportButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.exportCustomer"):WtfGlobal.getLocaleText("acc.vendorList.exportVendor")),  //"Export "+this.businessPerson+" details",  //.toLowerCase()+" details",
            disabled :true,
            filename: WtfGlobal.getLocaleText("acc.rem.14.1")+"_v1",
            id:(config.isCustomer?"exportCustomerLists6":"exportVendorLists7"),
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            params:{
                   startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                   inactiveCustomer:true
            },
            get:(config.isCustomer?113:114),
            label:this.businessPerson.toLowerCase(),
            isInactiveCustomerListReport : true
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.print)) {
        this.bbarBtnArr.push(this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            id:(config.isCustomer?"printCustomer6":"printVendor7"),
            tooltip:(config.isCustomer?WtfGlobal.getLocaleText("acc.customerList.printCustomer"):WtfGlobal.getLocaleText("acc.vendorList.printVendor")),  //"Print "+this.businessPerson+" details",   //.toLowerCase()+" details",
            disabled :true,
            filename: WtfGlobal.getLocaleText("acc.rem.14.1"),
            params:{
                name:config.isCustomer?WtfGlobal.getLocaleText("acc.rem.14"):WtfGlobal.getLocaleText("acc.vendorList.tab"),
                startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                inactiveCustomer:true
            },
            menuItem:{print:true},
            get:(config.isCustomer?113:114),
            label:this.businessPerson.toLowerCase()
        }));
    }
     this.btnArr.push("->");
     Wtf.account.InactiveCustomerList.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.InactiveCustomerList,Wtf.Panel,{
 
    submitHandler : function(){
        this.loadStore();
    },
    loadStore:function(){
       this.Store.load({
           params : {
               start : 0,
               startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
               limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt,
               ss : this.quickSearchTF.getValue()
           }
       });
    },
    handleResetClick:function(){
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false))
        if(this.quickSearchTF.getValue()){
            this.quickSearchTF.reset();
            this.Store.load({
            params: {
                start: 0,
                 startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
            }
        });
        }
    },
    setPageSize: function(store, rec, opt){
        var count = 0;
        for (var i = 0; i < store.getCount(); i++) {
            if (rec[i].data['level'] == 0 && (rec[i].data['parentid'] == "" || rec[i].data['parentid'] == undefined))
                count++;
        }
        this.pageLimit.totalSize = this.jReader.jsonData['totalCount'];
    },
   
   calllinkRowColor:function(id){
        var index=this.Store.find('id',id );
         var rec=this.Store.getAt(index);
         if(index>=0)
            WtfGlobal.highLightRowColor(this.grid,rec,true,0,0);
   },
   

   hideMsg: function(store){
        if(this.Store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();            
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
         Wtf.MessageBox.hide();
         if(this.personlinkid!=undefined)
         this.calllinkRowColor(this.personlinkid);
    },

    onRender: function(config){
     this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.btnArr,
                bbar: this.pToolBar
            }]
        }); 
       this.add(this.leadpan);
        if(this.openperson){
            callBusinessContactWindow(false, null, 'bcwin', this.isCustomer);
            var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
            Wtf.getCmp(tabid).on('update',this.updateGrid,this);
        }
        Wtf.account.InactiveCustomerList.superclass.onRender.call(this,config);
    },
     updateGrid: function(obj,perAccID){
        this.perAccID=perAccID;
        this.Store.reload();
        this.isAdd=true;
        this.Store.on('load',this.colorRow,this)
    },

    colorRow: function(store){
        if(this.isAdd && (!this.isEdit)){
            this.recArr=[];
            if(store.find('accid',this.perAccID) != -1) {
                this.recArr.push(store.getAt(store.find('accid',this.perAccID)));
                WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,0);
            }
            this.isAdd=false;
        }
    },

  
 genSuccessResponse:function(response){
       WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        if(response.success){
            (function(){
            WtfGlobal.loadpersonacc(this.isCustomer);//this.Store.reload();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        }

    },

    genFailureResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.Store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
   
    
 
    opBalRenderer:function(val,m,rec){
    	return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=val==0?"N/A":val>0?"Debit":"Credit";
        return WtfGlobal.deletedRenderer(val,m,rec)
    }
    
});


