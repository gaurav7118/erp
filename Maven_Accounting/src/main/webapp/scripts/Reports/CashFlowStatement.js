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
function callCashFlowStatementDynamicLoad(consolidateFlag){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
    var panelID = "cashFlow";
    panelID = consolidateFlag?panelID+'Merged':panelID;
    var panel = Wtf.getCmp(panelID);
    if(panel==null){
        panel = new Wtf.account.CashFlowStatement({
            id : panelID,
            border : false,
            layout: 'fit',
            consolidateFlag:consolidateFlag,
            tabTip:WtfGlobal.getLocaleText("acc.dashboard.TT.cashFlowStatement"),
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.dashboard.cashFlowWorkSheet"), Wtf.TAB_TITLE_LENGTH),  //'Cash Flow Statement',
            closable: true,
            iconCls:'accountingbase receivepayment'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callWeeklyCashFlowStatementDynamicLoad(consolidateFlag){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
    var panelID = "weeklyCashFlow";
    panelID = consolidateFlag?panelID+'Merged':panelID;
    var panel = Wtf.getCmp(panelID);
    if(panel==null){
        panel = new Wtf.account.WeeklyCashFlowStatementNew({
            id : panelID,
            border: false,
            closable: true,
            //            helpmodeid:28,
            withinventory:false,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: WtfGlobal.getLocaleText("ac.report.WeeklyUnpaidInvoiceCashFlow"),  //'Weekly Cash Flow Statement',
            tabTip:WtfGlobal.getLocaleText("ac.report.WeeklyUnpaidInvoiceCashFlowTlp"),
            receivable:true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.CashFlowStatement = function(config){
	
    this.createGrid();
    //	this.fetchData();

    config.layout='border';
    
    Wtf.apply(this,{
        autoScroll:true,
        border:false,
        defaults:{
            border:false,
            bodyStyle:"background-color:white;"
        },
        items:[this.Grid,{
            layout:'fit',
            region:'west',
            width:'20%'
        },{
            layout:'fit',
            region:'east',
            width:'20%'
        }],
        tbar:[WtfGlobal.getLocaleText("acc.field.AccountMasterType"),this.paymentType,WtfGlobal.getLocaleText("acc.field.PaymentAccount"),this.pmtMethod,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,{
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.ra.fetch"),
            tooltip:WtfGlobal.getLocaleText("acc.nee.8"),
            iconCls:'accountingbase fetch',
            //                    tooltip:"Select a time period to view corresponding ratio analysis.",
            scope:this,
            handler:this.fetchData
        },this.btnArr,this.printbtn]
                
    },config);
	
    Wtf.account.CashFlowStatement.superclass.constructor.call(this, config);
    
},

Wtf.extend(Wtf.account.CashFlowStatement, Wtf.Panel, {

    onRender: function(config) {
	
        Wtf.account.CashFlowStatement.superclass.onRender.call(this, config);
	    
        Wtf.Ajax.requestEx({
            url : "ACCReports/getCashFlow.do",
            params: {
                cashFlowReport : true,
                paymentType:this.paymentType.getValue(),
                accountID:"ALL",
                consolidateFlag:this.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        }, this, this.successCallback);
	    
    },

    successCallback:function(response){
        if(response.success){
            this.Grid.store.loadData(response.data);
            this.doLayout();
        }
        if(this.loadMask1!=undefined)
            this.loadMask1.hide();
    },

    fetchData:function(){
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.loadMask1 = new Wtf.LoadMask("cashFlow", {
            msg: WtfGlobal.getLocaleText("acc.msgbox.50"), 
            msgCls: "x-mask-loading acc-cashFlow-form-mask"
        });
        this.loadMask1.show();
        Wtf.Ajax.requestEx({
            url : "ACCReports/getCashFlow.do",
            params: {
                cashFlowReport : true,
                paymentType:this.paymentType.getValue(),
                accountID:this.pmtMethod.getValue(),
                consolidateFlag:this.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        }, this, this.successCallback);
    },
    handleResetClickNew:function(){
        this.paymentType.reset();
        this.accStore.load();
        this.startDate.reset();
        this.endDate.reset();
        this.fetchData();
    },
           
    createGrid:function(){
        var rec = new Wtf.data.Record.create([
        {
            name: 'name'
        },

        {
            name: 'desc'
        },

        {
            name: 'value'
        },

        {
            name: 'fmt'
        }
        ]);
		 
        var Store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                },rec)
        });
		
        this.Grid = new Wtf.grid.GridPanel({
            autoScroll:true,
            store: Store,
            layout:'fit',
            width:'60%',
            region:'center',
            columns: [{
                header:'<font size=2px ><b>'+WtfGlobal.getLocaleText("acc.report.2")+'</b>',
                dataIndex:'name',
                renderer:this.formatName
            },{
                header:'<font size=2px ><b>'+WtfGlobal.getLocaleText("acc.ra.value")+'</b>',
                align:'right',
                dataIndex:'value',
                renderer:this.opBalRenderer
            }],
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
		
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)
        });
		
        Store.on('load', function() {
            if(Store.getCount()<1) {
                this.Grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.Grid.getView().refresh();
            }
        }, this);
	    
	    
        this.csvbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportcsv',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
            scope: this,
            handler:function(){
                this.exportWithTemplate("csv")
            }
        });
        this.xlsbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportcsv',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
            scope: this,
            handler:function(){
                this.exportWithTemplate("xls")
            }
        });
        this.printbtn=new Wtf.Action({
            iconCls:'pwnd printButtonIcon',
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print Report Details.',
            text : WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            scope: this,
            handler:function(){
                this.printcashFlow();
            }
        });
	    
        this.pdfbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportpdf',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
            scope: this,
            handler:function(){
                this.exportPdfTemplate()
            }
        });
	     
        this.mnuBtns=[];

        this.mnuBtns.push(this.csvbtn);
        this.mnuBtns.push(this.xlsbtn);
        this.mnuBtns.push(this.pdfbtn);
        this.btnArr=[]; 
        this.btnArr.push( this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: this.isSummary,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        }));
        this.resetBttn.on('click',this.handleResetClickNew,this);
        this.btnArr.push(this.expButton=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
            scope: this,
            menu:this.mnuBtns
        }));
                        
            
        this.paymentTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'name'
            },{
                name:'value'
            }],
            data:[[WtfGlobal.getLocaleText("coa.masterType.cash"),2],[WtfGlobal.getLocaleText("coa.masterType.bank"),3]] //DATA CONTAINS PAYMENT METHOD TYPES. 
        });
          
        this.paymentType=new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.PaymentMethod"),  //'Payment Method',            
            name:'paymenttype',
            store:this.paymentTypeStore,
            valueField:'value',
            displayField:'name',
            mode: 'local',
            width:150,
            hiddenName:'paymenttype',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectPaymentMethodType"),
            allowBlank:false,
            value:2,          // dfault Account Master type is cash
            forceSelection:true,
            triggerAction:'all'
        });		
      
        this.accRec=new Wtf.data.Record.create([
        {
            name: 'accountid',
            mapping:'accid'
        },

        {
            name: 'accountname',
            mapping:'accname'
        },

        {
            name: 'acccode'
        }
        ]);
        this.accStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec),
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,                
                nondeleted:true,
                ignorecustomers:true,  
                ignorevendors:true
            }
        });
        
        this.accStore.load({
            params:{                           
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
                ignoreGLAccounts:true
            }
        });  
        this.accStore.on("load", function(store){
            var storeNewRecord=new this.accRec({
                accountid:'ALL',
                accountname:'ALL',
                acccode:''
            });
            this.pmtMethod.store.insert(0,storeNewRecord);
            this.pmtMethod.setValue("ALL");
        },this);
    
        this.pmtMethod= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.mp.payMethod"),
            name:"pmtmethod",
            store:this.accStore, 
            extraComparisionField:'acccode',
            minChars:1,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accountid',
            displayField:'accountname',
            allowBlank:false,
            //emptyText:(config.isReceipt?WtfGlobal.getLocaleText("acc.rp.recaacc"):WtfGlobal.getLocaleText("acc.mp.selpayacc")),
            anchor:'90%',
            mode: 'local',
            typeAheadDelay:30000,
            // disabled:true,
            triggerAction: 'all',     
            typeAhead: true,
            forceSelection: true//,
        });
                        
        this.paymentType.on("select",function(c,rec,ind){
            var baseparam = {};
            if(rec.data.value==2){ //Cash Account
                baseparam.ignoreBankAccounts=true
                baseparam.ignoreGSTAccounts=true
                baseparam.ignoreGLAccounts=true 
            }else if(rec.data.value==3){ //Bank Account
                baseparam.ignoreCashAccounts=true
                baseparam.ignoreGSTAccounts=true
                baseparam.ignoreGLAccounts=true 
            }
                
            this.accStore.load({
                params:baseparam
            });          
        },this);        
    },

    formatName:function(val, m, rec){
        if(rec.data.desc && rec.data.fmt != "title" && rec.data.fmt != "total"){
            return '<div><font size=2px >'+val+'</font></div><div class="grid-row-desc">'+rec.data.desc+'</div>';
        }else if(rec.data.fmt == "total"){
            return '<div><font size=2px ><b>'+val+'</b></font><div class="grid-row-desc">'+rec.data.desc+'</div>';
        }else if(rec.data.fmt == "title"){
            return '<div align=right><font size=2px ><b>'+val+'</b></font><div class="grid-row-desc">'+rec.data.desc+'</div>';
        }
        return val;
    },

    opBalRenderer:function(val,m,rec){
        if(rec.data.fmt != "title"){
            if(rec.data.fmt != "total")
                return WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec);
            else
                return '<font size=2px ><b>'+WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec)+'</b>';
        }
    },

    printcashFlow:function(){
        var exportUrl;
        var startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        var header = "lname,lvalue";
        var title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.ra.value");
        exportUrl = "ACCReports/exportCashFlow.do";
        var align = "none,none";
        var url = exportUrl+"?consolidateFlag="+this.consolidateFlag+"&companyids="+companyids+"&gcurrencyid="+gcurrencyid+"&userid="+loginid+"&name="+WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement")+"&filetype=print&startdate="+startdate+"&enddate="+enddate+"&accountid="
        +"&header="+header+"&title="+title+"&width=150&get=27&align="+align+"&paymentType="+this.paymentType.getValue()+"&accountID="+this.pmtMethod.getValue();
        url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
        window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
    },    
    
    exportWithTemplate:function(type){
        var exportUrl;
        var startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        var fileName;
        var header = WtfGlobal.getLocaleText("acc.field.lname,lvalue");
        var title = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.ra.value");
        
        exportUrl = "ACCReports/exportCashFlow.do";
        fileName = WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement")+"_v1";
        

        var align = "none,none";
        var url = exportUrl+"?consolidateFlag="+this.consolidateFlag+"&companyids="+companyids+"&gcurrencyid="+gcurrencyid+"&userid="+loginid+"&filename="+encodeURIComponent(fileName)+"&filetype="+type+"&startdate="+startdate+"&enddate="+enddate+"&accountid="
        +"&header="+header+"&title="+title+"&width=150&get=27&align="+align+"&paymentType="+this.paymentType.getValue()+"&accountID="+this.pmtMethod.getValue();
        Wtf.get('downloadframe').dom.src = url;
    },
    
    exportPdfTemplate:function(){
        var get;
        var fileName;
        var jsonGrid;
        var exportUrl;
        var startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        exportUrl = "ACCReports/exportCashFlow.do";
        fileName = WtfGlobal.getLocaleText("acc.dashboard.cashFlowStatement");
        get = 29;
        jsonGrid = "{data:[{'header':'lname','title':'"+WtfGlobal.getLocaleText("acc.report.2")+"','width':'200','align':''},"+
        "{'header':'lvalue','title':'"+WtfGlobal.getLocaleText("acc.ra.value")+"','width':'100','align':'currency'},";

        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        var url = exportUrl+"?consolidateFlag="+this.consolidateFlag+"&companyids="+companyids+"&gcurrencyid="+gcurrencyid+"&userid="+loginid+"&filename="+encodeURIComponent(fileName)+"&config="+configstr+"&filetype=pdf&startdate="+startdate+"&enddate="+enddate+"&accountid="
        +"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&paymentType="+this.paymentType.getValue()+"&accountID="+this.pmtMethod.getValue();
        Wtf.get('downloadframe').dom.src = url;
    },
    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    createReport:function(){
	
    } 

});

Wtf.account.WeeklyCashFlowStatementNew = function(config){
    this.createGrid();
    config.layout='border';
    Wtf.apply(this,{
        autoScroll:true,
        border:false,
        defaults:{
            border:false,
            bodyStyle:"background-color:white;"
        },
        items:[this.grid/*,{layout:'fit',region:'west',width:'20%'},{layout:'fit',region:'east',width:'20%'}*/],
        tbar:[WtfGlobal.getLocaleText("acc.field.AccountMasterType"),this.paymentType,WtfGlobal.getLocaleText("acc.field.PaymentAccount"),this.pmtMethod,WtfGlobal.getLocaleText("acc.common.from"), this.startDate,
        WtfGlobal.getLocaleText("acc.agedPay.intervalinweeks"),this.interval,{
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.ra.fetch"),
            tooltip:WtfGlobal.getLocaleText("acc.nee.8"),
            iconCls:'accountingbase fetch',
            scope:this,
            handler:this.fetchData
        },this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: this.isSummary,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false,
            handler:this.handleResetClickNew
        }),this.expButton,this.printButton]
                
    },config);
    Wtf.account.CashFlowStatement.superclass.constructor.call(this, config);
},

Wtf.extend(Wtf.account.WeeklyCashFlowStatementNew, Wtf.Panel, {
    onRender: function(config) {
        Wtf.account.WeeklyCashFlowStatementNew.superclass.onRender.call(this, config);
        var enddate=this.getEndDateAccordingToInterval();
        this.grid.store.load({
            params: {
                weeklyCashFlowReport: true,
                intervals:this.interval.getValue(),
                paymentType: this.paymentType.getValue(),
                accountID:"ALL",
                consolidateFlag: this.consolidateFlag,
                companyids : companyids,
                gcurrencyid : gcurrencyid,
                userid : loginid,
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:enddate
            }
        });
	    
    },
    fetchData:function(){
        var enddate=this.getEndDateAccordingToInterval();
        this.grid.store.load({
            params: {
                weeklyCashFlowReport: true,
                intervals:this.interval.getValue(),
                paymentType: this.paymentType.getValue(),
                accountID : this.pmtMethod.getValue(),
                consolidateFlag: this.consolidateFlag,
                companyids : companyids,
                gcurrencyid : gcurrencyid,
                userid : loginid,
                startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:enddate
            }
        });
        this.grid.getView().refresh();
    },
    handleResetClickNew:function(){
        this.paymentType.reset();
        this.accStore.load();
        this.startDate.reset();
        this.fetchData();
    },
           
    createGrid:function(){
        var rec = new Wtf.data.Record.create([
            ]);
            
        var targetReader = new Wtf.data.KwlJsonReader({
            root:'data',
            totalProperty:'totalCount'
        },rec);

        this.weeklyCashFlowstore = new Wtf.data.Store({
            url : "ACCReports/getCashFlow.do",
            reader:targetReader
        });
		
        this.weeklyCashFlowstore.on("load",this.createColumnModel,this);
                
        this.grid = new Wtf.grid.GridPanel({
            autoScroll:true,
            store: this.weeklyCashFlowstore,
            layout:'fit',
            width:'60%',
            region:'center',
            columns:[], 
            border : false,
            loadMask : true,
            viewConfig: {
//                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                }
        });
        this.interval = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.agedPay.till"), //'Till',
            maxLength: 2,
            width: 30,
            allowDecimal: false,
            allowBlank: false,
            minValue: 1,
            name: 'duration',
            value: 5,
            readOnly: true
        });

        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
            name: 'startdate',
            format: WtfGlobal.getOnlyDateFormat(),
            value: this.getDates(true)
        });
		
        this.paymentTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'name'
            },{
                name:'value'
            }],
            data:[[WtfGlobal.getLocaleText("coa.masterType.cash"),2],[WtfGlobal.getLocaleText("coa.masterType.bank"),3]] //DATA CONTAINS PAYMENT METHOD TYPES. 
        });
          
        this.paymentType=new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.PaymentMethod"),  //'Payment Method',            
            name:'paymenttype',
            store:this.paymentTypeStore,
            valueField:'value',
            displayField:'name',
            mode: 'local',
            width:150,
            hiddenName:'paymenttype',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectPaymentMethodType"),
            allowBlank:false,
            value:2,          // dfault Account Master type is cash
            forceSelection:true,
            triggerAction:'all'
        });		
    
        this.accRec=new Wtf.data.Record.create([
        {
            name: 'accountid',
            mapping:'accid'
        },

        {
            name: 'accountname',
            mapping:'accname'
        },

        {
            name: 'acccode'
        }
        ]);
        this.accStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec),
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,                
                nondeleted:true,
                ignorecustomers:true,  
                ignorevendors:true
            }
        });
        
        this.accStore.load({
            params:{                           
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
                ignoreGLAccounts:true
            }
        });  
        this.accStore.on("load", function(store){
            var storeNewRecord=new this.accRec({
                accountid:'ALL',
                accountname:'ALL',
                acccode:''
            });
            this.pmtMethod.store.insert(0,storeNewRecord);
            this.pmtMethod.setValue("ALL");
        },this);
    
        this.pmtMethod= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.mp.payMethod"),
            name:"pmtmethod",
            store:this.accStore, 
            extraComparisionField:'acccode',
            minChars:1,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            valueField:'accountid',
            displayField:'accountname',
            allowBlank:false,
            anchor:'90%',
            mode: 'local',
            typeAheadDelay:30000,
            triggerAction: 'all',     
            typeAhead: true,
            forceSelection: true//,
        });
              
        this.paymentType.on("select",function(c,rec,ind){
            var baseparam = {};
            if(rec.data.value==2){ //Cash Account
                baseparam.ignoreBankAccounts=true
                baseparam.ignoreGSTAccounts=true
                baseparam.ignoreGLAccounts=true 
            }else if(rec.data.value==3){ //Bank Account
                baseparam.ignoreCashAccounts=true
                baseparam.ignoreGSTAccounts=true
                baseparam.ignoreGLAccounts=true 
            }
             
            this.accStore.load({
                params:baseparam
            });          
        },this);
        
        this.expButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            filename : WtfGlobal.getLocaleText("acc.dashboard.WeeklyCashFlowStatement")+"_v1",
            params:{
                    stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                    enddate:this.getEndDateAccordingToInterval(),
                    weeklyCashFlowReport: true,
                    intervals:this.interval.getValue(),
                    paymentType: this.paymentType.getValue(),
                    accountid : this.pmtMethod.getValue(),
                    consolidateFlag: this.consolidateFlag,
                    companyids : companyids,
                    gcurrencyid : gcurrencyid,
                    userid : loginid,
                    get:444
                  },
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get: Wtf.autoNum.weeklyCashFlow
        });
        
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            filename : WtfGlobal.getLocaleText("acc.dashboard.WeeklyCashFlowStatement"),
            label:WtfGlobal.getLocaleText("acc.dashboard.WeeklyCashFlowStatement"),
            params:{
                    stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                    enddate:this.getEndDateAccordingToInterval(),
                    weeklyCashFlowReport: true,
                    intervals:this.interval.getValue(),
                    paymentType: this.paymentType.getValue(),
                    accountid : this.pmtMethod.getValue(),
                    consolidateFlag: this.consolidateFlag,
                    companyids : companyids,
                    gcurrencyid : gcurrencyid,
                    userid : loginid,
                    get:444
            },
            menuItem:{print:true},
            get: Wtf.autoNum.weeklyCashFlow
          });
        
    },
    createColumnModel : function(store){
        var columns = [];
        Wtf.each(store.reader.jsonData.columns, function(column){
            if(column.header=='Particulars'){
                column.renderer = this.formatName;
                column.header='<font size=2px ><b>'+WtfGlobal.getLocaleText("acc.report.2")+'</b>';
            }else if(column.header=='Value'){
                column.header='<font size=2px ><b>'+WtfGlobal.getLocaleText("acc.ra.value")+'</b>';
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
            }else{
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
            }
            columns.push(column);
        },this);
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();
    },
    formatName:function(val, m, rec){
        if(rec.data.desc && rec.data.fmt != "title" && rec.data.fmt != "total" ){
            return '<div><font size=2px >'+val+'</font></div><div class="grid-row-desc">'+rec.data.desc+'</div>';
        }else if(rec.data.fmt == "total"){
            return '<div><font size=2px ><b>'+val+'</b></font><div class="grid-row-desc">'+rec.data.desc+'</div>';
        }else if(rec.data.fmt == "title"){
            return '<div><font size=2px ><b>'+val+'</b></font><div class="grid-row-desc">'+rec.data.desc+'</div>';
        }
        return val;
    },

    opBalRenderer:function(val,m,rec){
        if(rec.data.fmt != "title"){
            if(rec.data.fmt != "total")
                return WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec);
            else
                return '<font size=2px ><b>'+WtfGlobal.withoutRateCurrencyDeletedSymbol(val,m,rec)+'</b>';
        }
    },
    getEndDateAccordingToInterval : function(){
        var duration=7;
        var intervals=this.interval.getValue();
        var numberOfDays=(duration * intervals - 1);
        var endDate=this.startDate.getValue().add(Date.DAY, numberOfDays);
        return WtfGlobal.convertToGenericEndDate(endDate);
    },
    getDates: function(start) {
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom)
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd)
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        if (start)
            return fd;

        return fd.getLastDateOfMonth();
    }
});

