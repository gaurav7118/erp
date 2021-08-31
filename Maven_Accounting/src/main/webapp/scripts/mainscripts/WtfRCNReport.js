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
Wtf.account.WtfRCNReport = function(config){
	
    this.createGrid();  
    config.layout='fit'; 
    this.accid="All";
     this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.dnList.searchText") + ' '+WtfGlobal.getLocaleText("acc.je.msg2"),  //'Quick Search by JE Number',
        id:"quickSearch"+config.helpmodeid, //+this.id,
        width: 200,
        field:"lctrno"
     });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset Search Results',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
     this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.form.DateField({
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
          
       this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'acccode'},
            {name:'accountpersontype'},
            {name:'mappedaccountid'},
            {name:'masterTypeValue'},
            {name:'groupname'}
        ]);

    this.cusvenAccStore = new Wtf.data.Store({//Customer/vendor multi selection Combo
        url:"ACCAccountCMN/getAccountsForCombo.do",
        baseParams: {
            deleted: false,
            nondeleted: true,
            ignoreCashAccounts:true,
            ignoreGSTAccounts:true,
            ignoreGLAccounts:true
           
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.accRec)
    });

    this.cusvenAccStore.on("load", function(store) {
        var storeNewRecord = new this.accRec({
            accountname: 'All',
            accountid: 'All'
        });
        this.Name.store.insert(0, storeNewRecord);
        this.Name.setValue("All");
    }, this);
    this.cusvenAccStore.load();
    
    this.VendorComboconfig = {     
        store: this.cusvenAccStore,
        valueField: 'accountid',
        hideLabel: true,
        displayField: 'accountname',
        emptyText:WtfGlobal.getLocaleText("acc.inv.ven"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus: true,
        triggerAction: 'all',
        scope: this
    };
    this.Name = new Wtf.common.Select(Wtf.applyIf({
        multiSelect: false,
        fieldLabel: WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*',
        forceSelection: true,
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
        listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 250,
        extraComparisionField: 'acccode', // type ahead search on acccode as well.
        width: 240
    }, this.VendorComboconfig));
    
     this.Name.on('select', function(combo, personRec) {
        if (personRec.get('accountid') == 'All') {
            this.accid=personRec.get('accountid'),
            combo.clearValue();
            combo.setValue('All');
            this.loadJEStore();
        } else {
            this.accid=personRec.get('accountid'),
            combo.clearValue();
            combo.setValue(personRec.get('accountid'));
            this.loadJEStore();
        }
    }, this);
    
    this.csvselectedbtn=new Wtf.Toolbar.Button({
            iconCls:'pwnd '+'exportcsv',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
            scope: this,
            handler:function(){
                this.exportWithTemplate("csv",true)
            }
        });

    
    Wtf.apply(this,{
        width: 3000,
        height: '100%',
        autoScroll:true,
        defaults:{
            border:false,
            bodyStyle:"background-color:white;overflow: auto;"
        },
        items:[this.Grid],
        tbar:[WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate,this.submitBttn,WtfGlobal.getLocaleText("acc.het.90"),this.Name,this.csvselectedbtn ]
    },config);
	
    Wtf.account.TransactionListPanelViewCashFlowStatementAsPerCOA.superclass.constructor.call(this, config);
    
},

Wtf.extend(Wtf.account.WtfRCNReport, Wtf.Panel, {

    onRender: function(config) {
       
        Wtf.account.WtfRCNReport.superclass.onRender.call(this, config);
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url : "ACCOtherReports/getRCNReportData.do",
            params: {
                consolidateFlag:true,
                accname:this.accid
            }
        }, this, this.successCallback);
	    
    },

    successCallback:function(response){
        if(response.success){
            this.Grid.store.loadData(response.data);
            this.doLayout();
        }
    },
    
   createGrid:function(){
       
        this.RCNReportRec = new Wtf.data.Record.create([
        {
            name: 'bank'
        },

        {
            name: 'ref'
        },

        {
            name: 'supplier'
        },

        {
            name: 'lctrno'
        },

        {
            name: 'ttdate'
        },

        {
            name: 'amount_euro'
        },

        {
            name: 'amount_usd'
        },

        {
            name: 'inteedton'
        },

        {
            name: 'price'
        },

        {
            name: 'bankcharges'
        },

        {
            name: 'bankint'
        },

        {
            name: 'amount'
        },

        {
            name: 'paidon'
        },        

        {
            name: 'date'
        },

        {
            name: 'invoiceno1'
        },

        {
            name: 'customer'
        },

        {
            name: 'fcls'
        },

        {
            name: 'tons'
        },

        {
            name: 'price1'
        },

        {
            name: 'less2per'
        },

        {
            name: 'freight'
        },

        {
            name: 'calcamt'
        },

        {
            name: 'claimscn'
        },

        {
            name: 'amount_usd1'
        },

        {
            name: 'freight_usd'
        },

        {
            name: 'insurchg'
        },

        {
            name: 'usdbankcharge'
        },

        {
            name: 'paidon1'
        },

        {
            name: 'bankint_usd'
        },

        {
            name: 'shortageplusor'
        },

        {
            name: 'claims'
        },
        
        {
            name: 'format'
        }
        ]);
		 
        this.RCNreportStroe = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                },this.RCNReportRec)
        });		                  
        
        this.Grid = new Wtf.grid.GridPanel({
            store: this.RCNreportStroe,
            autoScroll:true,
            hirarchyColNumber:0,
            loadMask : true,
            columns: [{
                header:'<font size=2px ><b>Bank</b>',
                dataIndex:'bank',
                width:300,
                renderer:this.formatbank
            },{
                header:'<font size=2px ><b>Ref</b>',
                dataIndex:'ref',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>Supplier</b>',
                align:'left',
                dataIndex:'supplier',
                width:300,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>LC/TR NO</b>',
                align:'left',
                dataIndex:'lctrno',
                width:150,
                renderer:this.formatlctrno
            },{
                header:'<font size=2px ><b>TT Date</b>',
                align:'left',
                width:150,
                dataIndex:'ttdate',
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>AMOUNT EURO</b>',
                align:'right',
                dataIndex:'amount_euro',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>AMOUNT USD</b>',
                align:'right',
                dataIndex:'amount_usd',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>INTED TON</b>',
                align:'right',
                dataIndex:'inteedton',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>PRICE</b>',
                align:'right',
                dataIndex:'price',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>BANK CHARGES</b>',
                align:'right',
                dataIndex:'bankcharges',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>BANK INTEREST</b>',
                align:'right',
                dataIndex:'bankint',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>AMOUNT</b>',
                align:'right',
                dataIndex:'amount',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>PAID ON</b>',
                align:'left',
                dataIndex:'paidon',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>DATE</b>',
                align:'left',
                dataIndex:'date',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>INVOICE NO</b>',
                align:'left',
                dataIndex:'invoiceno1',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>CUSTOMER</b>',
                align:'left',
                dataIndex:'customer',
                width:300,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>flcs</b>',
                align:'left',
                dataIndex:'fcls',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>TONS</b>',
                align:'right',
                dataIndex:'tons',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>PRICE</b>',
                align:'right',
                dataIndex:'price1',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>Less 2%</b>',
                align:'right',
                dataIndex:'less2per',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>Freight(add)/ Less</b>',
                align:'left',
                dataIndex:'freight',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>Calc Amt</b>',
                align:'right',
                dataIndex:'calcamt',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>Claims CN</b>',
                align:'right',
                dataIndex:'claimscn',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>Amount USD</b>',
                align:'right',
                dataIndex:'amount_usd1',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>FREIGHT USD</b>',
                align:'right',
                dataIndex:'freight_usd',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>INSUR CHARGES</b>',
                align:'right',
                dataIndex:'insurchg',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>USD BANK CHARGES</b>',
                align:'right',
                dataIndex:'usdbankcharge',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>PAID ON</b>',
                align:'left',
                dataIndex:'paidon1',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>BANK INTEREST USD</b>',
                align:'right',
                dataIndex:'bankint_usd',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>SHORTAGE PLUS OR</b>',
                align:'left',
                dataIndex:'shortageplusor',
                width:150,
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>CLAIMS</b>',
                align:'right',
                dataIndex:'claims',
                width:150,
                renderer:this.formatName
            }],     
            border : false,            
            viewConfig: {
                forceFit:false
            }
        });
            
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        }); 
        
       this.RCNreportStroe.on('beforeload', function(){
            this.loadMask.show();
        }, this);
        
        this.RCNreportStroe.on('loadexception', function(){
            this.loadMask.hide();
        }, this);
        
        this.RCNreportStroe.on('load', function() {
            if(Store.getCount()<1) {
                this.Grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.Grid.getView().refresh();
                this.loadMask.hide();
            }
        }, this);	     
    },

    formatbank:function(val, m, rec){
        if(rec.data.format == "title" &&rec.data.bank!=""){
            return '<div align=left><b>'+rec.data.bank+'</b></div>';
        }
        return val;
    },
    
    formatlctrno:function(val, m, rec){
       if(rec.data.format == "title"&&rec.data.lctrno!=""){
            return '<div align=left><b>'+rec.data.lctrno+'</b></div>';
        }
        return val;
    },
    
    formatNo:function(val, m, rec){
        if(rec.data.no && rec.data.format != "title" && rec.data.format != "total" &&rec.data.format!="maintitle"){
            return '<div align=left>'+rec.data.no+'</div>';
        }else if(rec.data.format == "total"){
            return '<div align=right><b>'+rec.data.no+'</b></div>';
        }else if(rec.data.format == "title"){
            return '<div align=center><b>'+rec.data.no+'</b></div>';
        }else if(rec.data.format == "maintitle"){
            return '<div align=center><font size=3px ><u><b>'+rec.data.no+'</b><u></font>';
        }
        return val;
    },
    
    opBalRenderer:function(val,m,rec){
        if(rec.data.format != "total" ){
            return WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec);
        }else if(rec.data.format == "title"){
            return '<font size=2px ><b>'+WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec)+'</b>';
        }else{
            return '<b>'+WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec)+'</b>';
        }	
    },
    
      submitHandler : function(){
         if(this.startDate.getValue()>this.endDate.getValue()){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
             return;
         }
         this.loadJEStore();
     },
     
    loadJEStore:function(){
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url : "ACCOtherReports/getRCNReportData.do",
            params: {
                consolidateFlag:true, 
                ss:this.quickPanelSearch.getValue(),
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                accname:this.accid
            }
        }, this, this.successCallback);
	    
    },
    
    storeloaded:function(store){
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
    },
    
     exportWithTemplate:function(type,SelectedExportFlag,Landscape_Orientation){
        var exportUrl = "ACCOtherReports/exportRCNReport.do";
        var nonDeleted = this.nondeleted==undefined ? false : this.nondeleted;
        var Landscape_Orientation = Landscape_Orientation==undefined ? false : Landscape_Orientation;
        var deleted = this.deleted==undefined ? false : this.deleted;
        
        var linkid = this.entryID==undefined ? '' : this.entryID;
        var fileName = "RCN Report";
        var name = "RCN Report";
        var header = "bank,ref,supplier,lctrno,ttdate,amount_euro,amount_usd,inteedton,price,bankcharges,bankint,amount,paidon,date,invoiceno1,customer,fcls,tons,price1,less2per,freight,calcamt,claimscn,amount_usd1,freight_usd,insurchg,usdbankcharge,paidon1,bankint_usd,shortageplusor,claims";
        var title = "Bank,Ref,Supplier,LC TR/No,TT Date,Amount Euro,Amount USD,INTED TON,PRICE,Bank Charges, Bank Int, Amount,Paid On,Date,Invoice No,Customer,fcls,TONS,PRICE,Less 2%,FREIGHT,Calc Amt,Claims CN,Amount USD,FREIGHT USD,Insur Chg,USD Bank Charge,Paid On,Bank Int USD,SHORTAGE PLUS OR,Claims";
        var align = "none,none,none,none,none,none,none,none,none,none,none,none,none,date,none,none,none,none,none,none,none,none,none,none,none,none,none,date,none,none,none";
        var withoutinventory = Wtf.account.companyAccountPref.withoutinventory;
        var cashtype = this.cashtype;
        var startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        var searchJsonString = "";
        if(this.searchJson!= null && this.searchJson!= undefined && this.searchJson!= ""){
            searchJsonString = "&searchJson="+this.searchJson+"&flag=1&moduleid="+Wtf.Acc_GENERAL_LEDGER_ModuleId+"&filterConjuctionCriteria="+this.filterConjuctionCrit;
        }
        if(this.consolidateFlag) { 
            var url = exportUrl+"&companyids="+companyids+"&gcurrencyid="+gcurrencyid+"&userid="+loginid+"&filename="+encodeURIComponent(fileName)+ "&name="+encodeURIComponent(name) + "&cashtype=" + encodeURIComponent(cashtype) + "&withoutinventory=" +withoutinventory + "&filetype="+type+"&nondeleted="+nonDeleted+"&deleted="+deleted+"&costCenterId="+costCenterId+"&selectedIds="+selectedIds+"&startdate="+startdate+"&enddate="+enddate+"&accid="+this.accname+"&linkid="+linkid+"&header="+header+"&title="+encodeURIComponent(title)+"&width=150&get=27&align="+align+searchJsonString+"&Landscape_Orientation="+Landscape_Orientation;
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+ "&name="+encodeURIComponent(name)+ "&withoutinventory=" +withoutinventory + "&filetype="+type+"&startdate="+startdate+"&enddate="+enddate+"&accname="+this.accid+"&linkid="+linkid+"&header="+header+"&title="+encodeURIComponent(title)+"&width=150&get=27&align="+align+searchJsonString+"&Landscape_Orientation="+Landscape_Orientation;
        }        

        Wtf.get('downloadframe').dom.src = url;
    },
    
    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.storeloaded();
        }
    } 

});

