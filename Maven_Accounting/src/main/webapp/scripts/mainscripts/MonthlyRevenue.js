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
Wtf.account.MonthlyRevenue=function(config){
    this.summary = new Wtf.grid.GroupSummary({});
    this.expander = new Wtf.grid.RowExpander({});
    this.accRec = Wtf.data.Record.create ([
        {name:'accountname',mapping:'accname'},
        {name:'accountid',mapping:'accid'},
        {name:'currencyid',mapping:'currencyid'},
        {name:'acccode'}
    ]);

    // to change this with the month & year drop-down list
    this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name:'monthid',type:'int'}, 'name'],
            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
                [10,'November'],[11,'December']]
    });
    
    this.natureStore = new Wtf.data.SimpleStore({
            fields: [{name:'id',type:'int'}, 'name'],
            data :[[3,'Income'],[2,'Expense'],[0,'All']]
    });
    
    this.natureCombo = new Wtf.form.ComboBox({
            store: this.natureStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.nature"),  //'Nature',
            name:'nature',
            displayField:'name',
            hidden:true,//ERP-529
//            hideLabel:true,
            forceSelection: true,
            anchor:'95%',
            valueField:'id',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true            
    });
    this.natureCombo.setValue(3);
    this.natureCombo.on('change', function(){
        
        this.accStore.load({
        params:{
            nature : this.natureCombo.getValue()==0?[2,3]:this.natureCombo.getValue()
        }
    });
    }, this);
    var data=this.getBookBeginningYear(true);
    
    this.yearStore= new Wtf.data.SimpleStore({
            fields: [{name:'id',type:'int'}, 'yearid'],
            data :data
    });

    this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'startMonth',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            width:150,
            valueField:'name',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
    });  

    this.startYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'startYear',
            displayField:'yearid',
            anchor:'95%',
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            width:100,
            triggerAction: 'all',
            selectOnFocus:true
    });  

    this.endMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'endMonth',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'name',
            mode: 'local',
            width:150,
            triggerAction: 'all',
            selectOnFocus:true
    }); 

    this.endYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'endYear',
            displayField:'yearid',
            anchor:'95%',
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            width:100,
            triggerAction: 'all',
            selectOnFocus:true
    });       

    if (config.sMonth!=null && config.sMonth!= "")
        this.startMonth.setValue(config.sMonth);

    if (config.sYear!=null && config.sYear!= "")
        this.startYear.setValue(config.sYear);

    if (config.eMonth!=null && config.eMonth!= "")
        this.endMonth.setValue(config.eMonth);   

    if (config.eYear!=null && config.eYear!= "")
        this.endYear.setValue(config.eYear);

    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.exportPermType=this.permType.exportdataledger;
    this.printPermType=this.permType.printledger;

    this.accStore = new Wtf.data.Store({
        url : "ACCAccountCMN/getAccountsForCombo.do",
        baseParams:{
            mode:2,
             ignorecustomers:true,  
             ignorevendors:true,
             nondeleted:true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });

    // the data structure for the record
    this.LedgerRec = new Wtf.data.Record.create([
        {name: 'accCode'},
        {name: 'accountcode'},
        {name: 'acccode'},
        {name: 'accCodeName'},
        {name: 'accountname'},
        {name: 'accountid'},      
        {name: 'amount_0'},
        {name: 'amount_1'},
        {name: 'amount_2'},
        {name: 'amount_3'},
        {name: 'amount_4'},
        {name: 'amount_5'},
        {name: 'amount_6'},
        {name: 'amount_7'},
        {name: 'amount_8'},
        {name: 'amount_9'},
        {name: 'amount_10'},
        {name: 'amount_11'},
        {name: 'amount_12'},
        {name: 'amount_13'},
        {name: 'amount_14'},
        {name: 'amount_15'},
        {name: 'amount_16'},
        {name: 'amount_17'}
    ]);

    this.LedgerStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.LedgerRec),
            url:"ACCReports/getMonthlyRevenue.do",
            baseParams:{
                mode:61, // TODO - check what it is meant for
                creditonly:true,
                ignorezero:true,
                nondeleted:true                
            }
    });

    this.rowNo=new Wtf.grid.RowNumberer();

    var columnArr = [];
    
    var columnWidth = 80;
    var pdfWidth = 80;     
    columnArr.push(this.rowNo);
    columnArr.push( {
        header: WtfGlobal.getLocaleText("acc.coa.accCode"), // "Account Code", 
        dataIndex: 'accountcode',
        align:'center',
        width:120,
        pdfwidth:110,
        hidden:false
    });  

    columnArr.push( {
        header: WtfGlobal.getLocaleText("acc.coa.gridAccountName"), // "Account Name", 
        dataIndex: 'accountname',
        align:'center',
        width:120,
        pdfwidth:110,
        hidden:false
    });  
    var dynamicArrayIndex = columnArr.length;
    for(var i=0; i<18; i++){
       columnArr.push({
            hidden: false,
                    dataIndex: 'amount_'+i,      
                    renderer:this.formatMoney,  
                    width: columnWidth,
                    pdfwidth: pdfWidth,
                    align:'center',
                    style: 'text-align:right'
        });
    };    

    // column model
    this.gridcm= new Wtf.grid.ColumnModel(columnArr);

    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.LedgerStore,
        cm:this.gridcm,
        ctCls : 'monthlyRevenueReport',
        border:false,
        plugins:[this.expander],
        layout:'fit',
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        loadMask : true
    });
    
    this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
            this.grid.getView().refresh();
    },this);
    
    this.LedgerStore.on('beforeload', function(s,o) {
         if(this.pP.combo!=undefined){
            if(this.pP.combo.value=="All"){
                var count = this.LedgerStore.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                s.paramNames.limit = count;
                s.baseParams.stdate=this.sDate;
                s.baseParams.enddate=this.eDate;
                s.baseParams.nature=this.natureCombo.getValue();
                s.baseParams.accountid = 'All';
            }else{
                s.baseParams.stdate=this.sDate;
                s.baseParams.enddate=this.eDate;
                s.baseParams.nature=this.natureCombo.getValue();
                s.baseParams.accountid = 'All';
            }
        }
    },this);    
    
    // looad data into the store
    this.LedgerStore.on("load", function(store){

        // even if there is no records, we still need to show the month names
        // get month count from the first element
        var store = this.grid.getStore();

        // var monthArray = store.months; // data.items[0].json["months"];
        if(store.data.items[0].json["months"]!=undefined){
            var monthArray = store.data.items[0].json["months"];
            var monthCount = monthArray.length;

            for(var i=0; i<monthArray.length; i++){  
                this.grid.getColumnModel().setColumnHeader((i+dynamicArrayIndex), '<div align=left><b>'+monthArray[i]["monthname"]+'</b></div>') ;        
            }   

            var columnCount =  this.grid.getColumnModel().getColumnCount();

            // show those months with data
            for(var i=dynamicArrayIndex; i<(dynamicArrayIndex+monthCount); i++){
                this.grid.getColumnModel().setHidden(i, false) ;
            }        

            // hide those months without data
            for(var i=(monthCount+dynamicArrayIndex); i<columnCount; i++){
                this.grid.getColumnModel().setHidden(i,true) ;
            }
        }
        // as the first entry is always for storing the month names
        if(store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();

            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();//}
            if(this.printButton)this.printButton.enable();//}                    
        }
    },this);

    // the account drop down list            
    this.cmbAccount=new Wtf.form.ExtFnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
        id:'accountIdForCombo' ,
        name:'accountid',
        store:this.accStore,
        valueField:'accountid',
        displayField:'accountname',
        mode: 'local',
        typeAheadDelay:30000,
        minChars:1,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        width:150,
        hiddenName:'accountid',
        emptyText:'Select Account',
        allowBlank:false,
        forceSelection:true,
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
        triggerAction:'all'
    });
      this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);   
    var btnArr=[];

    btnArr.push(
//ERP-529         WtfGlobal.getLocaleText("acc.accPref.nature"),this.natureCombo,WtfGlobal.getLocaleText("acc.ledger.accName"),this.cmbAccount,
            this.natureCombo,WtfGlobal.getLocaleText("acc.ledger.accName"),this.cmbAccount,
            WtfGlobal.getLocaleText("acc.common.from"),
            this.startMonth, this.startYear,
            WtfGlobal.getLocaleText("acc.common.to"),
            this.endMonth, this.endYear,
            '-',{
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"),  //"Select a time period to view corresponding ledger records.",
            iconCls:'accountingbase fetch',
            scope:this,
            handler:this.onClick
        },'-',this.resetBttn
    );

    // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
        }      

        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox(1,2);
            return;            
        }

        var months;
        months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
        months -= startMonthDate.getMonth();
        months += endMonthDate.getMonth();
        if (months<0)
            months=0;

        if (months>18){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported") ], 2);
            return;            
        }    
    /////    

    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push("-",this.expButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.export"),
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),
            disabled :true,
            filename: WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle"),
            params:{
                // stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                //      enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                    stdate: this.sDate,
                    enddate: this.eDate,                
                    accountid:this.accountID||config.accountID,
                    name: WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle")
            },
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:Wtf.autoNum.MonthlyRevenue
        }));
    }

    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        btnArr.push("-",this.printButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.print"),
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
            disabled :true,
            params:{
                    // stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                    // enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                    stdate: this.sDate,
                    enddate: this.eDate,                       
                    accountid:this.accountID||config.accountID,
                    name: WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle")
            },
            filename: WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle"),
            label: WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle"),
            menuItem:{print:true},
            get:Wtf.autoNum.MonthlyRevenue
        }));
    }

    
    this.grid.on('cellclick',this.onCellClick, this);

    this.accStore.on('load',function(){
           var storeNewRecord=new this.accRec({
            accountname:'All',
            accountid:'All'
        });
        this.cmbAccount.store.insert( 0,storeNewRecord);
           
      this.showLedger(config.accountID,this.startMonth.getValue(),this.startYear.getValue(), this.endMonth.getValue(), this.endYear.getValue());

    },this);

    this.accStore.load({
        params:{
            nature : this.natureCombo.getValue()
        }
    });

    Wtf.apply(this,{
        items:[{
            layout:'border',
            border:false,
            scope:this,
            items:[{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid
            }],
            tbar:btnArr,
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.LedgerStore,           
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
                plugins: this.pP = new Wtf.common.pPageSize({
                   id : "pPageSize_"+this.id
                })
            })
        }]
    },config);
    
    Wtf.account.MonthlyRevenue.superclass.constructor.call(this, config);

    this.addEvents({
       'journalentry':true
    });
    
} // end of Wtf.account.MonthlyRevenue

// extend the Grid Panel to do some other tricks
Wtf.extend( Wtf.account.MonthlyRevenue,Wtf.Panel,{
    
    onRender:function(config){
         Wtf.account.MonthlyRevenue.superclass.onRender.call(this,config);
    },
    
    formatAccountName:function(val,m,rec){
         if(val=="Total"){return "<b>"+val+"</b>";}
         else{return val}
    },
    onClick:function(){
        this.accountID=this.cmbAccount.getValue();
        this.fetchLedger();
        this.accStore.findBy( function(rec){
            },this)
    },

    formatMoney:function(val,m,rec,i,j,s){
        if(val == 0){
            return '--'
        }
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },

    // populate month & year drop-down list
    // SON REFACTOR - to move this function to Global / common class
    getBookBeginningYear:function(isfirst){
        var ffyear;
        if(isfirst){
            var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
            ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)
            ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()
        }
        else{
            var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
            ffyear=new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime()
        }

       var data=[];
        var newrec;
        if(ffyear==null||ffyear=="NaN"){
            ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)
        }
        var year=ffyear.getFullYear();
        var temp=new Date();
        var year1=temp.getFullYear();
        data.push([0,year1]);
        var i=1;
        while(year1>=year){
            data.push([i,--year1]);
            i++;
        }
        if(!(ffyear.getMonth()==0&&ffyear.getDate()==1)){
            data.push([1,year+1]);
            newrec = new Wtf.data.Record({
                id:1,
                yearid:year+1
            });
        }
        return data;
    },     

    fetchLedger:function(){

        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.startYear.getValue() == ""){
            this.startMonth.setValue(this.monthStore.data.items[0].json[1]);
            this.startYear.setValue(this.yearStore.data.items[0].json[1]);
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[1]);   
            this.endYear.setValue(this.yearStore.data.items[0].json[1]);
        }

        this.sDate = this.startMonth.getValue() + ", " + this.startYear.getValue();
        this.eDate = this.endMonth.getValue() + ", " + this.endYear.getValue();

        if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
        }      

        var startMonthDate = new Date(this.startMonth.getValue() + " 01, " + this.startYear.getValue());
        var endMonthDate = new Date(this.endMonth.getValue() + " 01, " + this.endYear.getValue());        

        if (this.startYear.getValue() >= this.endYear.getValue() && startMonthDate.getMonth() > endMonthDate.getMonth()){
            WtfComMsgBox(1,2);
            return;            
        }

        var months;
        months = (endMonthDate.getFullYear() - startMonthDate.getFullYear()) * 12;
        months -= startMonthDate.getMonth();
        months += endMonthDate.getMonth();
        if (months<0)
            months=0;

        if (months>18){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Onlymaximum18monthsaresupported") ], 2);
            return;            
        }   

        if(this.accountID&&this.accountID.length>0){
            this.LedgerStore.load({
                params:{
                    start:0,
                    limit: (this.pP.combo!=undefined) ? this.pP.combo.value:30,
                    accountid:this.accountID,
                    // stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    // enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
                    stdate: this.sDate,
                    enddate: this.eDate,
                    nature:this.natureCombo.getValue()
                }
            });
            if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
                this.expButton.setParams({
                    accountid:this.accountID,
                    // stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    // enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                    stdate: this.sDate,
                    enddate: this.eDate,                    
                    name: WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle")+"-"+this.cmbAccount.getRawValue()
                });
            }
            if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
                this.printButton.setParams({
                    accountid:this.accountID,
                    // stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    // enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                    stdate: this.sDate,
                    enddate: this.eDate,
                    name: WtfGlobal.getLocaleText("acc.monthlyRevenue.tabTitle")+"-"+this.cmbAccount.getRawValue()
                });
            }
        }
    },
    handleResetClick:function(){
        this.accStore.load();
        this.startMonth.reset();
        this.startYear.reset();
        this.endYear.reset();
        this.endMonth.reset();
        this.fetchLedger();
    },

    showLedger:function(accid,startMonth, startYear, endMonth, endYear){
        var i=this.accStore.find("accountid",accid);
        if(i>=0){
            this.cmbAccount.setValue(accid);

            if(startMonth!="" && startMonth!=undefined){                    
                this.startMonth.setValue(startMonth);
            }

            if(startYear!="" && startYear!=undefined){
                this.startYear.setValue(startYear);
            }

            if(endMonth!="" && endMonth!=undefined){                    
                this.endMonth.setValue(endMonth);
            }

            if(endYear!="" && endYear!=undefined){
                this.endYear.setValue(endYear);
            }            

            this.accountID=accid;
            this.onClick();
        }
    },

    onRowClick:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var rec=this.LedgerStore.getAt(i);
        var jid=rec.data['d_journalentryid'];
        if(!jid||jid.length<=0)
            jid=rec.data['c_journalentryid'];
        this.fireEvent('journalentry',jid,true);
    },

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="c_entryno" || header=="d_entryno"){
            var rec=this.LedgerStore.getAt(i);
            var jid=rec.data['d_journalentryid'];
            if(!jid||jid.length<=0)
                jid=rec.data['c_journalentryid'];
            this.fireEvent('journalentry',jid,true);
        } else if(header=="d_transactionID" ||header=="c_transactionID"){
            var formrec = this.LedgerStore.getAt(i);
            var type=formrec.data['type'];
            viewTransactionTemplate(type, formrec);
        }
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

    dateRenderer:function(v){
        if(v) return v.format('Y M d');
        return "";
    },

    showSeperator:function(){
        return '<div style="margin:-5px"><img src="../../images/header.gif"></div>';
    },

    accountCurrencyRenderer:function(val,m,rec){
        if (val!="")
            return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
        else
            return "";
    }
}); // end of Wtf.extend