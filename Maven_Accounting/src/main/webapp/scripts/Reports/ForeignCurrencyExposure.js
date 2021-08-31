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


function callForeignCurrencyExposureDynamicLoad(consolidateFlag){
    consolidateFlag = (consolidateFlag!=undefined || consolidateFlag!=null)?consolidateFlag:false;
    var panelID = "foreignCurrencyExposure";
    panelID = consolidateFlag?panelID+'Merged':panelID;
    var panel = Wtf.getCmp(panelID);
    if(panel==null){
        panel = new Wtf.ForeignCurrencyExposureReport({
            id : panelID,
            consolidateFlag:consolidateFlag,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.fxexposure.link"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.fxexposure.link"),  //'Foreign Currency Exposure Report',
            border: false,
            closable: true,
            layout: 'fit',
            foreignCurrencyGainAndLoss:false,
            iconCls:'accountingbase receivepayment',
            isCustBill:false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function callForeignCurrencyGainAndLossTabViewDynamicLoad(consolidateFlag){
    consolidateFlag = (consolidateFlag!=undefined || consolidateFlag!=null)?consolidateFlag:false;
    var panelID = "foreignCurrencyGainAndLoss";
    var panel = Wtf.getCmp(panelID);
    if(panel==null){
        panel = new Wtf.ForeignCurrencyExposureReport({
            id : panelID,
            consolidateFlag:consolidateFlag,
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.foreignCurrency.gainloss.report"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.foreignCurrency.gainloss.report.tt"),  //'Click to view Foreign Currency Gain And Loss Report',
            border: false,
            closable: true,
            layout: 'fit',
            foreignCurrencyGainAndLoss:true,
            iconCls:'accountingbase receivepayment',
            isCustBill:false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

Wtf.ForeignCurrencyExposureReport = function(config){
    // this.summary = new Wtf.ux.grid.GridSummary();
    this.summary = new Wtf.grid.GroupSummary({});
    this.isCustBill=config.isCustBill;
    this.isCustomerOptionSelected=false;
    this.optionSelected=2; // Used for foreign Gain & Loss Report (2-All,0-Customer,1-Vendor)
    this.foreignCurrencyGainAndLoss = (config.foreignCurrencyGainAndLoss!=null && config.foreignCurrencyGainAndLoss!=undefined)?config.foreignCurrencyGainAndLoss:false;
    this.businessPerson=(this.isCustomerOptionSelected?'Customer':'Vendor');
    this.heplmodeid = config.heplmodeid; // ??
    
    this.customerid = null;
    this.vendorid = null;
    this.documentTypeId = null;
    this.currencyid = null;

    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'invoiceno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        // {name:'customer'},
        {name:'currencycode'},
        {name:'oldcurrencyratetobase'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
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
        {name:'childCount'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'templateid'},
        {name:'templatename'},
        {name:'status'},
        {name:'withoutinventory',type:'boolean'},
        {name:'amountinbase1'},
        {name:'netgainloss1'},
        {name:'netgainlosspercent1'},
        {name:'amountinbase2'},
        {name:'netgainloss2'},
        {name:'netgainlosspercent2'}
    ]);
    if(this.foreignCurrencyGainAndLoss){
        this.GridRec = Wtf.data.Record.create ([
        {name:'accid'},
        {name:'personname'},
        {name:'invoicedate'},
        {name:'no'},
        {name:'amountinbase'},
        {name:'invoiceCurrencyCode'},
        {name:'exchangerate'},
        {name:'amount'},
        {name:'paymentdate'},
        {name:'paymentno'},
        {name:'amountdue'},
        {name:'paymentamountinbase'},
        {name:'paymentCurrencyCode'},
        {name:'paymentrate'},
        {name:'paymentTransactionExchangeRate'},
        {name:'paymentamount'},
        {name:'differenceAmount', type: 'float'},
        {name:'differenceRate'}
    ]);    
    }
    if (this.isCustomerOptionSelected)
        this.StoreUrl = "ACCInvoiceCMN/getForeignCurrencyExposure.do";
    else
        this.StoreUrl = "ACCGoodsReceiptCMN/getForeignCurrencyExposure.do";
    if (this.foreignCurrencyGainAndLoss){
        this.StoreUrl = "ACCInvoiceCMN/getForeignCurrencyGainAndLoss.do";
    }
    // if(config.consolidateFlag){
        this.Store = new Wtf.data.GroupingStore({
            url:this.StoreUrl,
            baseParams: {
              getRepeateInvoice: false,
              creditonly: true,
              ignorezero: true,
              consolidateFlag:config.consolidateFlag,
              companyids:companyids,
              gcurrencyid:gcurrencyid,
              userid:loginid
            },
            sortInfo : {
                field : 'personname',
                direction : 'ASC'
            },
            groupField : 'personname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
    // } 

    // else {
    //     this.Store = new Wtf.data.Store({
    //         url:this.StoreUrl,
    //         baseParams: {
    //           getRepeateInvoice: false,
    //           consolidateFlag:config.consolidateFlag,
    //           companyids:companyids,
    //           gcurrencyid:gcurrencyid,
    //           userid:loginid
    //         },
    //         reader: new Wtf.data.KwlJsonReader({
    //             root: "data",
    //             totalProperty:'count'
    //         },this.GridRec)
    //     });
    // }
//    this.Store.load();

    //////////////// THE BUTTONS & INPUT FIELDS TOOLBAR
    var btnArr=[];
//    var topBtnArr=[];
//    topBtnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
//        emptyText:WtfGlobal.getLocaleText("acc.fxexposure.quicksearch"),  //'Quick Search By Invoice No or Customer / Vendor Name'
//        width: 200,
//        field: 'billno',
//        Store:this.Store
//    }));
//
//    topBtnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
//        text: WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
//        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
//        scope: this,
//        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
//        handler: this.handleResetClick,
//        disabled: false
//    }));

    var customerVendorSelectionArr = new Array();
    if(this.foreignCurrencyGainAndLoss){
        customerVendorSelectionArr.push([2,WtfGlobal.getLocaleText("acc.ledger.accAllTransactions")],[0,WtfGlobal.getLocaleText("acc.fxexposure.customer")], [1,WtfGlobal.getLocaleText("acc.fxexposure.vendor")] );
    }else{
        customerVendorSelectionArr.push([0,WtfGlobal.getLocaleText("acc.fxexposure.customer")], [1,WtfGlobal.getLocaleText("acc.fxexposure.vendor")] );
    }

     this.customerVendorSelectionStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :customerVendorSelectionArr
    });    

    this.customerVendorSelectionStore.on("load", function(){
        // to insert a blank value with 'All' to be the first option
//        var allRec = new Wtf.data.Record({
//           name : WtfGlobal.getLocaleText("acc.fxexposure.selectcusvendor"), // "Select Customer / Vendor",
//           typeid: ""            
//        });
//
//        if(this.customerVendorSelectionStore.getAt(0)!="")
//            this.customerVendorSelectionStore.insert(0,allRec);
    },this);     

    this.comboCustomerVendorSelection = new Wtf.form.ComboBox({
            name:'typeid',
            triggerAction:'all',
            hideLabel:false,
            hidden:false,
            mode: 'local',
            valueField:'typeid',
            displayField:'name',
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectAccount"),
            disabled:false,
            store:this.customerVendorSelectionStore,                        
//            allowBlank:false,            
            typeAhead: true, 
            width:150,
            forceSelection: true,                        
            selectOnFocus:true,      
            value:2,  //this.foreignCurrencyGainAndLoss?2:0,
            scope:this,
            listeners:{
                'select':{
                    fn:this.loadCustomerVendorSelectionStore,
                    scope:this
                }
            }
        });
//    if (this.isCustomerOptionSelected)
//        this.comboCustomerVendorSelection.setValue(0);
//    else
//        this.comboCustomerVendorSelection.setValue(1);
    
//    if(!this.foreignCurrencyGainAndLoss){
//        btnArr.push(this.comboCustomerVendorSelection);    
//    }

    // being used for both customer & vendor drop-down lists
    this.personRec = new Wtf.data.Record.create ([
        {
            name:'accid'
        },{
            name:'accname'
        },{
            name: 'termdays'
        },{
            name: 'billto'
        },{
            name: 'currencysymbol'
        },{
            name: 'currencyname'
        },{
            name: 'currencyid'
        },{
            name:'deleted'
        },{
            name:'acccode'
        },{
            name:'hasAccess'
        }
        ]);    
    
    if (this.isCustomerOptionSelected){
        this.customerAccStoreUrl = "ACCCustomer/getCustomersForCombo.do";    
    }else{
        this.customerAccStoreUrl = "ACCVendor/getVendorsForCombo.do";
    }
    
    this.customerAccStore =  new Wtf.data.Store({
        url:this.customerAccStoreUrl,
        baseParams:{
            mode:2,
            group:[10, 13],
            deleted:false,
            nondeleted:true,
            common:'1'
        },reader: new  Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad:false
        },this.personRec)
    });    
    
    this.customerAccStore.on("load", function(){
        // to insert a blank value with 'All' to be the first option
        var allRec = new Wtf.data.Record({
           accname : "All",
           accid: "",
           termdays: "",
           billto: "",
           currencyid: "",
           currencysymbol: "",
           currencyname: "",
           deleted: "",
           acccode:""
        });

        if(this.customerAccStore.getAt(0)!="")
            this.customerAccStore.insert(0,allRec);
    },this);    

//    this.customerAccStore.load();       

    this.comboCustomerVendor= new Wtf.form.ExtFnComboBox({
            fieldLabel:this.isCustomerOptionSelected?WtfGlobal.getLocaleText("acc.fxexposure.customer"):WtfGlobal.getLocaleText("acc.fxexposure.vendor"),
            hiddenName:this.businessPerson.toLowerCase(),
            store: this.customerAccStore,
            valueField:'accid',
            emptyText: '',
            disabled:true,
            displayField:'accname',
//            allowBlank:false,
            hirarchical:true,
//            emptyText:this.isCustomerOptionSelected?WtfGlobal.getLocaleText("acc.fxexposure.customer"):WtfGlobal.getLocaleText("acc.fxexposure.vendor") ,
            mode: 'local',
            typeAhead: true,
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            minChars:1,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            forceSelection: true,
            selectOnFocus:true,
            width : 150,
            triggerAction:'all',
            scope:this,
            listeners:{
                'select':{
                    fn:this.loadCustomerVendorStore,
                    scope:this
                }
            }
           
    });
//    if(!this.foreignCurrencyGainAndLoss){
//        btnArr.push('  ',this.comboCustomerVendor);
//    }
//    var documentTypeArr = new Array();
// documentTypeArr.push([0,WtfGlobal.getLocaleText("acc.inv.invOption")], [1,WtfGlobal.getLocaleText("acc.inv.cnOption")], [2,WtfGlobal.getLocaleText("acc.inv.dnOption")], [4,WtfGlobal.getLocaleText("acc.inv.adOption")]  );
//    documentTypeArr.push([0,WtfGlobal.getLocaleText("acc.fxexposure.invOption")]);
//
//    this.documentTypeStore = new Wtf.data.SimpleStore({
//        fields: [{name:'typeid',type:'int'}, 'name'],
//        data :documentTypeArr
//    });        
//
//    this.comboDocumentType = new Wtf.form.ComboBox({
//            name:'typeid',
//            triggerAction:'all',
//            hideLabel:false,
//            hidden:false,
//            mode: 'local',
//            valueField:'typeid',
//            displayField:'name',
//            disabled:false,
//            store:this.documentTypeStore,                        
//            emptyText: this.documentTypeStore.getAt(0).name,
//            allowBlank:false,            
//            typeAhead: true, 
//            width:150,
//            forceSelection: true,                        
//            selectOnFocus:true,           
//            scope:this,
//            listeners:{
//                'select':{
//                    fn:this.loadDocumentTypeStore,
//                    scope:this
//                }
//            }
//        }); 
//
//    this.comboDocumentType.setValue(documentTypeArr[0].typeid);
//    this.comboDocumentType.setValue(0);
//
//    btnArr.push(this.comboDocumentType);    

    // Currency combo box being used for both customer & vendor drop-down lists
    this.currencyRec = new Wtf.data.Record.create ([{name:'name'},{name:'currencyid'}]);               
    
    this.currencyStore =  new Wtf.data.Store({
        url:"ACCCurrency/getCurrency.do",
        baseParams:{
            mode:2,
            group:[10, 13],
            deleted:false,
            nondeleted:true,
            common:'1'
        },
        reader: new  Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.currencyRec)
    });    

    // to insert a blank value with 'All' to be the first option
    var allCurrencyRec = new Wtf.data.Record({name : 'All Currencies', currencyid: ""});

    this.currencyStore.on("load", function(){
        if(this.currencyStore.getAt(0)!=""){
            this.currencyStore.insert(0,allCurrencyRec);
            // this.documentTypeStore.setAt(0, 0);
        }
        for(var i=0; i<this.currencyStore.data.length; i++){
            if (this.currencyStore.getAt(i).data.currencyid == gcurrencyid){
                this.currencyStore.remove(this.currencyStore.getAt(i));
                break;
            }
        }
    },this);    
    if(!this.foreignCurrencyGainAndLoss){
        this.currencyStore.load();     
    }

    this.comboCurrency= new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.fxexposure.currency"), //  "Currency",
            hiddenName: WtfGlobal.getLocaleText("acc.fxexposure.currency"), // "Currency",
            store: this.currencyStore,
            valueField:'currencyid',
            displayField:'name',
            disabled:true,
//            allowBlank:false,
            hirarchical:true,
            emptyText: allCurrencyRec.name,
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            width : 150,
            triggerAction:'all',
            scope: this,
            listeners: {
                'select': {
                    fn: this.loadCurrencyStore,
                    scope: this
                }
            }           
    });
    this.comboCurrency.setValue(allCurrencyRec.currencyid);
//    this.comboCurrency.setValue("");
//    if(!this.foreignCurrencyGainAndLoss){
//        btnArr.push('  ',this.comboCurrency);  
//    }
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

//    btnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
//    btnArr.push(this.startDate);
//    btnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
//    btnArr.push(this.endDate);      
        this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
   
    this.submitBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),
        // tooltip :"Select a time period to view corresponding records.",  
        id: 'submitRec' + this.id,
        scope: this,
        iconCls:'accountingbase fetch',
        disabled :!this.foreignCurrencyGainAndLoss
    });
    this.submitBttn.on("click", this.submitHandler, this);
    /*
     * Provided button to expand or collapse all row details. 
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
//    btnArr.push('  ',this.submitBttn);
//    if(!this.foreignCurrencyGainAndLoss){
//       btnArr.push(' ',this.resetBttn);
//    }



//    btnArr.push("->");

    this.minExchangeRateTxt=new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.fxexposure.minrate")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.basetocurrencyexchangerate.help")), // 'Min Exchange Rate',
            name: 'minrate',
            id: "minrate",
            disabled : true,
            width : 50,
            maxLength:50,
            scope:this
    });
    if(!this.foreignCurrencyGainAndLoss){
        btnArr.push(WtfGlobal.getLocaleText("acc.fxexposure.minrate")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.basetocurrencyexchangerate.help")));
        btnArr.push(this.minExchangeRateTxt);
    }
    this.maxExchangeRateTxt=new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.fxexposure.maxrate")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.basetocurrencyexchangerate.help")), // 'Max Exchange Rate',
            name: 'maxrate',
            id: "maxrate",
            disabled : true,
            width : 50,
            maxLength:50,
            scope:this
    });
    if(!this.foreignCurrencyGainAndLoss){
        btnArr.push(WtfGlobal.getLocaleText("acc.fxexposure.maxrate")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.basetocurrencyexchangerate.help")));
        btnArr.push(this.maxExchangeRateTxt);   
    }
    
    

    this.compareBtn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.compare"),
        tooltip : WtfGlobal.getLocaleText("acc.fxexposure.compareTT"), // "Compare net gain/loss based on exchange rates entered.",  
        id: 'compareRec' + this.id,
        scope: this,
        iconCls:'accountingbase compareRate',
        disabled :true
    });
    this.compareBtn.on("click", this.compareHandler, this);
    if(!this.foreignCurrencyGainAndLoss){
        btnArr.push('  ',this.compareBtn);
    }
    
    this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        filename :this.foreignCurrencyGainAndLoss?WtfGlobal.getLocaleText("acc.foreignCurrency.gainloss.report")+"_v1":WtfGlobal.getLocaleText("acc.fxexposure.link")+"_v1",
        menuItem:{
            xls:true,
            pdf:true,
            csv:true,
            rowPdf:false
        },
        get:this.foreignCurrencyGainAndLoss?246:Wtf.autoNum.ForeignCurrencyExposure
    });
   
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
        label:WtfGlobal.getLocaleText("acc.foreignCurrency.gainloss.report"),
        disabled :true,
        filename : this.foreignCurrencyGainAndLoss?WtfGlobal.getLocaleText("acc.foreignCurrency.gainloss.report"):WtfGlobal.getLocaleText("acc.fxexposure.link"),
        menuItem:{
            print:true
        },
        get:this.foreignCurrencyGainAndLoss?246:Wtf.autoNum.ForeignCurrencyExposure,
        params:{
            name: this.foreignCurrencyGainAndLoss?WtfGlobal.getLocaleText("acc.foreignCurrency.gainloss.report"):WtfGlobal.getLocaleText("acc.fxexposure.link")
        }
    });
//    if(this.foreignCurrencyGainAndLoss){
        btnArr.push('-',this.exportButton);
        btnArr.push('-',this.printButton);
//    }
    this.printButton.on('click', function() {
        this.printButton.get = this.foreignCurrencyGainAndLoss ? (246) : (this.isCustomerOptionSelected ? Wtf.autoNum.ForeignCurrencyExposureCustomer : Wtf.autoNum.ForeignCurrencyExposure);
    }, this);
    this.exportButton.on('click', function() {
        this.exportButton.get = this.foreignCurrencyGainAndLoss ? (246) : (this.isCustomerOptionSelected ? Wtf.autoNum.ForeignCurrencyExposureCustomer : Wtf.autoNum.ForeignCurrencyExposure);
    }, this);
    this.expander = new Wtf.grid.RowExpander({});
    this.sm = new Wtf.grid.RowSelectionModel({singleSelect: true});
    // this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);   

    this.rowNo=new Wtf.grid.RowNumberer();
    this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,
            {
                header: this.getComboDisplay(this.comboCustomerVendorSelection, this.customerVendorSelectionStore), // WtfGlobal.getLocaleText("acc.fxexposure.customer"),  //"Customer",
                width:150,
                pdfwidth:150,
                renderer:WtfGlobal.deletedRenderer,
                dataIndex:'personname',
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
            },{
                header: WtfGlobal.getLocaleText("acc.prList.invNo"),
                dataIndex:'billno',
                width:150,
                pdfwidth:150,
                renderer:config.consolidateFlag?WtfGlobal.deletedRenderer:WtfGlobal.linkDeletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.fxexposure.currency"), // "Currency",  // WtfGlobal.getLocaleText("acc.inv.ccy"), // "Currency",  
                dataIndex:'currencycode',
                width:150,
                pdfwidth:150,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.fxexposure.totalamt"), // "Total Invoiced Amount", // WtfGlobal.getLocaleText("acc.inv.totalInvAmt"), //"Total Invoiced Amount",  
                dataIndex:'amount',
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                width:150,
                pdfwidth:150,
                hidden:false
            },{
                header: WtfGlobal.getLocaleText("acc.field.AppliedExchangeRate"), // "Applied Exchange Rate",  // WtfGlobal.getLocaleText("acc.inv.txrateBaseAmt"), // "Transaction Exchange Rate (To Base currency)",  
                dataIndex:'oldcurrencyratetobase',
                width:150,
                pdfwidth:150,
                hidden:false
            }, {
                header: WtfGlobal.getLocaleText("acc.fxexposure.totalamtinbase"), // "Total Invoice Amount in Base Currency (As of Transaction Date)",  // WtfGlobal.getLocaleText("acc.inv.txrateBaseAmt"), // "Transaction Exchange Rate (To Base currency)",  
                dataIndex:'amountinbase',
                renderer:WtfGlobal.currencyDeletedRenderer,
                width:150,
                pdfwidth:150,
                hidden:false
            }, {
                header: WtfGlobal.getLocaleText("acc.fxexposure.totalamtinbasemin"), // "Total Invoice Amount in Base Currency (Min Rate)",  // WtfGlobal.getLocaleText("acc.inv.txrateBaseAmt"), // "Transaction Exchange Rate (To Base currency)",  
                dataIndex:'amountinbase1',
                renderer:WtfGlobal.currencyRenderer,
                width:150,
                summaryType:'sum',
                pdfwidth:150,
                hidecurrency : true,
                summaryRenderer:WtfGlobal.currencySummaryRenderer
                // renderer:WtfGlobal.currencyDeletedRenderer,
                // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                // width:80,
                // pdfwidth:150,
                // hidden:false                
            }, {
                header: WtfGlobal.getLocaleText("acc.fxexposure.netgainlossmin"), // "Net Gain / Loss (Min Rate)",  // WtfGlobal.getLocaleText("acc.inv.txrateBaseAmt"), // "Transaction Exchange Rate (To Base currency)",  
                dataIndex:'netgainloss1',
                renderer:WtfGlobal.currencyRenderer,
                width:150,
                summaryType:'sum',
                pdfwidth:150,
                hidecurrency : true,
                summaryRenderer:WtfGlobal.currencySummaryRenderer
                // renderer:WtfGlobal.currencyDeletedRenderer,
                // summaryType:'sum',
                // summaryRenderer:WtfGlobal.currencySummaryRenderer,                
                // width:80,
                // pdfwidth:150,
                // hidden:false
            }, {
                header: WtfGlobal.getLocaleText("acc.fxexposure.netgainlosspercentmin"), // "Net Gain / Loss % (Min Rate)",  // WtfGlobal.getLocaleText("acc.inv.txrateBaseAmt"), // "Transaction Exchange Rate (To Base currency)",  
                dataIndex:'netgainlosspercent1',            
                renderer:function(v,m,rec){
                    if (v != "" && v<0){
                        v= v + "%";                
                        return'<div class="currency" style="color:red">'+v+'</div>';

                    }else if (v != "" && v>=0){
                        v= v + "%";                
                        return'<div class="currency">'+v+'</div>';
                    }                                        
                },
                width:140,
                pdfwidth:150,
                hidden:false
            }, {
                header: WtfGlobal.getLocaleText("acc.fxexposure.totalamtinbasemax"), // "Total Invoice Amount in Base Currency (Max Rate)",  // WtfGlobal.getLocaleText("acc.inv.txrateBaseAmt"), // "Transaction Exchange Rate (To Base currency)",  
                dataIndex:'amountinbase2',
                renderer:WtfGlobal.currencyRenderer,
                width:150,
                summaryType:'sum',
                pdfwidth:150,
                hidecurrency : true,
                summaryRenderer:WtfGlobal.currencySummaryRenderer
                // renderer:WtfGlobal.currencyDeletedRenderer,
                // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'},
                // width:80,
                // pdfwidth:150,
                // hidden:false                 
            }, {
                header: WtfGlobal.getLocaleText("acc.fxexposure.netgainlossmax"), //  "Net Gain / Loss (Max Rate)",  // WtfGlobal.getLocaleText("acc.inv.txrateBaseAmt"), // "Transaction Exchange Rate (To Base currency)",  
                dataIndex:'netgainloss2',
                renderer:WtfGlobal.currencyRenderer,
                width:150,
                summaryType:'sum',
                pdfwidth:150,
                hidecurrency : true,
                summaryRenderer:WtfGlobal.currencySummaryRenderer
                // renderer:WtfGlobal.currencyDeletedRenderer,
                // summaryType:'sum',
                // summaryRenderer:WtfGlobal.currencySummaryRenderer,                
                // width:80,
                // pdfwidth:150,
                // hidden:false
            }, {
                header: WtfGlobal.getLocaleText("acc.fxexposure.netgainlosspercentmax"), // "Net Gain / Loss % (Max Rate)",  // WtfGlobal.getLocaleText("acc.inv.txrateBaseAmt"), // "Transaction Exchange Rate (To Base currency)",  
                dataIndex:'netgainlosspercent2',
                renderer:function(v,m,rec){
                    if (v != "" && v<0){
                        v= v + "%";                
                        return '<div class="currency" style="color:red">'+v+'</div>';

                    }else if (v != "" && v>=0){
                        v= v + "%";                
                        return '<div class="currency">'+v+'</div>';
                    }                                        
                },
                width:150,
                pdfwidth:150,
                hidden:false
            }]);        
    if(this.foreignCurrencyGainAndLoss){
     this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,
            {
                header:  WtfGlobal.getLocaleText("acc.trial.acc"),  //"Account",
                width:150,
                pdfwidth:150,
                renderer:WtfGlobal.deletedRenderer,
                dataIndex:'personname',
                summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
            },{
                header:WtfGlobal.getLocaleText("acc.reval.evaldate"),//date
                dataIndex:'invoicedate',
                sortable:true,
                align:'center',
                width:150,
                pdfwidth:80
                //renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.common.reference"),
                dataIndex:'no',
                sortable:true,
                width:150,
                pdfwidth:150,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.common.orignalAmount"), 
                dataIndex:'amount',
                renderer:function(val,m,rec){
                    var symbol=((rec==undefined||rec.data.invoiceCurrencyCode==null||rec.data['invoiceCurrencyCode']==undefined||rec.data['invoiceCurrencyCode']=="")?WtfGlobal.getCurrencySymbol():rec.data['invoiceCurrencyCode']);
                    var v=parseFloat(val);
                    if(isNaN(v)) return val;
                    if(rec.data.deleted)
                        v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
                    else
                        v=WtfGlobal.conventInDecimal(v,symbol);
                    v=  '<div class="currency">'+v+'</div>';
                    return v;
                },
                width:150,
                pdfwidth:150,
//                summaryType:'sum',
                hidden:false
            },{
                header: WtfGlobal.getLocaleText("acc.currency.exRate"), //"Exchange Rate"
                dataIndex:'exchangerate',
                width:100,
                pdfwidth:150,
                hidden:false
            },{
                header:  WtfGlobal.getLocaleText("acc.fxexposure.totalamtinbase"), 
                dataIndex:'amountinbase',
                renderer:WtfGlobal.currencyDeletedRenderer,
                width:150,
//                summaryType:'sum',
                pdfwidth:150,
                hidden:false
            },{
                header:WtfGlobal.getLocaleText("acc.reval.evaldate"),
                dataIndex:'paymentdate',
                align:'center',
                width:150,
                pdfwidth:80
//                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.common.reference.payments/receipts"),
                dataIndex:'paymentno',
                width:150,
                pdfwidth:150,
                sortable:true,
                renderer:WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.field.OriginalAmountDue"), 
                dataIndex:'amountdue',
                renderer:function(val,m,rec){
                    var symbol=((rec==undefined||rec.data.invoiceCurrencyCode==null||rec.data['invoiceCurrencyCode']==undefined||rec.data['invoiceCurrencyCode']=="")?WtfGlobal.getCurrencySymbol():rec.data['invoiceCurrencyCode']);
                    var v=parseFloat(val);
                    if(isNaN(v)) return val;
                    if(rec.data.deleted)
                        v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
                    else
                        v=WtfGlobal.conventInDecimal(v,symbol);
                    v=  '<div class="currency">'+v+'</div>';
                    return v;
                },
                width:150,
                pdfwidth:150,
//                summaryType:'sum',
                hidden:false
            },{
                header: WtfGlobal.getLocaleText("acc.currency.exRate"), 
                dataIndex:'paymentTransactionExchangeRate',
                width:150,
                pdfwidth:150,
                hidden:false
            },{
                header: WtfGlobal.getLocaleText("acc.fxexposure.totalamtinbase"), 
                dataIndex:'paymentamountinbase',
                renderer:WtfGlobal.currencyDeletedRenderer,
                width:150,
                pdfwidth:150,
//                summaryType:'sum',
                hidden:false
            }/*,{
                header: "Amount Paid/Received",
                dataIndex:'paymentamount',
                renderer:function(val,m,rec){
                    var symbol=((rec==undefined||rec.data.paymentCurrencyCode==null||rec.data['paymentCurrencyCode']==undefined||rec.data['paymentCurrencyCode']=="")?WtfGlobal.getCurrencySymbol():rec.data['paymentCurrencyCode']);
                    var v=parseFloat(val);
                    if(isNaN(v)) return val;
                    if(rec.data.deleted)
                        v='<del>'+WtfGlobal.conventInDecimal(v,symbol)+'</del>';
                    else
                        v=WtfGlobal.conventInDecimal(v,symbol);
                    v=  '<div class="currency">'+v+'</div>';
                    return v;
                },
                width:100,
                pdfwidth:150,
//                summaryType:'sum',
                hidden:false
            }*/,{
                header: WtfGlobal.getLocaleText("acc.common.difference.exchangeRate"),//Difference (of Exchange Rate)
                dataIndex:'differenceRate',
                renderer: function(val){
                    return "<span font-color='red'>"+(val)+"</span>";
                },
                width:150,
//                summaryType:'sum',
                pdfwidth:150,
                hidden:false
            },{
                header: WtfGlobal.getLocaleText("acc.common.gainloss.amount"),//Gain/Loss Amount
                dataIndex:'differenceAmount',
                renderer: function(val){
                    return "<span font-color='red'>"+WtfGlobal.currencyRenderer(val)+"</span>";
//                    return "<span font-color='red'>"+(val)+"</span>";
                },
                width:150,
//                summaryType:'sum',
                pdfwidth:150,
                hidden:false,
                summaryType: 'sum',
                summaryRenderer:WtfGlobal.currencySummaryRenderer
            }]); 
    }
    this.gridView1 = config.consolidateFlag?new Wtf.grid.GroupingView({
            forceFit:true,
            showGroupName: true,
            enableNoGroups:false, // REQUIRED!
            hideGroupedColumn: true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }):{
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        };    
//  var plugins=[];
//  if(!this.foreignCurrencyGainAndLoss){
//      plugins.push(this.summary);
//  }
  this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        border:false,
        sm:this.sm,
        cm: this.gridcm,
        loadMask : true,  
        //        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
        layout:'fit',
        // plugins: this.expander,
        plugins:[this.summary],
        // viewConfig:this.gridView1,
        view : new Wtf.grid.GroupingView({
            startCollapsed :true,
            forceFit : false,
            showGroupName : true,
            enableGroupingMenu : true,
            hideGroupedColumn : false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }),       
        tbar:btnArr,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
                })
    });

        this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
            this.grid.getView().refresh();
        },this);
        
       
    
    this.items=[this.grid];

    this.expandRec = Wtf.data.Record.create ([
        {name:'parentInvoiceId'},
        {name:'invoiceId'},
        {name:'invoiceNo'}
    ]);

    this.expandStoreUrl = "ACCInvoice/getInvoiceRepeateDetails.do";
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expandStore.on("beforeload", function(store){
        this.expandStoreUrl = "ACCInvoice/" + (this.withInvMode?"getBillingInvoiceRepeateDetails":"getInvoiceRepeateDetails") + ".do";
        store.proxy.conn.url = this.expandStoreUrl;
    }, this);    

    this.customerAccStore.on('load', function(store){
        if(store.getCount()>0){
            this.comboCustomerVendor.setValue(store.getAt(0).data.accid);                
            // this.customerId = null;
//            this.loadStore();
        }            
    }, this);         
    if (!this.foreignCurrencyGainAndLoss || this.foreignCurrencyGainAndLoss==undefined){
    this.Store.on('beforeload',function(s,o){
        this.Store.removeAll();
        if(!o.params)o.params={};
        o.params.cashonly=this.cashonly;
        o.params.creditonly=true;
        o.params.ignorezero=true;

        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.deleted= false; // this.deleted;
        currentBaseParams.nondeleted=true; // this.nondeleted;
        currentBaseParams.cashonly= this.cashonly;
        currentBaseParams.creditonly=true;
        currentBaseParams.ignorezero=true;
        currentBaseParams.currencyid=this.currencyid;
        currentBaseParams.customerid=this.customerid;
        currentBaseParams.vendorid=this.vendorid;
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        currentBaseParams.minrate = this.minExchangeRateTxt.getValue();
        currentBaseParams.maxrate = this.maxExchangeRateTxt.getValue();
        if(currentBaseParams.archieve == 3){
            currentBaseParams.isfavourite=true;
        }else{
            currentBaseParams.isfavourite=this.isfavourite;
        }
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
    }, this);
    }else{
          this.Store.on('beforeload',function(s,o){
            this.Store.removeAll();
            if(!o.params)o.params={};
            var currentBaseParams = this.Store.baseParams;
            currentBaseParams.deleted= false; // this.deleted;
            currentBaseParams.nondeleted=true; // this.nondeleted;
            currentBaseParams.customerid=this.customerid;
            currentBaseParams.vendorid=this.vendorid;
            currentBaseParams.optionSelected=this.optionSelected;
            currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
            currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
            this.expandButtonClicked = false;        
          }, this);
    }
    if(this.foreignCurrencyGainAndLoss){
        this.Store.load({params:{start:0,limit:30}});
    }
    this.currencyStore.on('load', this.hideLoading, this);

    this.Store.on('load',this.expandRow, this);
    this.Store.on('load',this.hideLoading, this);
    this.Store.on('loadexception',this.hideLoading, this);

    this.grid.on('cellclick',this.onRowClick, this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
//    this.tbar = topBtnArr;

    this.tbar1 = [];
    this.tbar1.push(this.comboCustomerVendorSelection);    


    if(!this.foreignCurrencyGainAndLoss){
        this.tbar1.push('  ',this.comboCurrency);  
    }
    
    this.tbar1.push('  ',this.comboCustomerVendor);
    
    this.tbar1.push(WtfGlobal.getLocaleText("acc.common.from"));
    this.tbar1.push(this.startDate);
    
    this.tbar1.push(WtfGlobal.getLocaleText("acc.common.to"));
    this.tbar1.push(this.endDate); 
    
    this.tbar1.push('  ',this.submitBttn);
    this.tbar1.push('-', this.expandCollpseButton);
    
    if(!this.foreignCurrencyGainAndLoss){
        this.tbar1.push(' ',this.resetBttn);
    }
    this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        items:[
        {
            region:'center',
            layout:'fit',
            border:false,
            tbar : this.tbar1,
            items:[this.grid]
        }]
    });
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });
    
    Wtf.apply(this,config);
    Wtf.ForeignCurrencyExposureReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.ForeignCurrencyExposureReport, Wtf.Panel,{
    onRender: function(config) {
	Wtf.ForeignCurrencyExposureReport.superclass.onRender.call(this, config);
    },
    
    hideLoading:function(){
      Wtf.MessageBox.hide();
    },

    getComboDisplay : function(combo, store) {
        var value = combo.getValue();
        var valueField = combo.valueField;
        var returnValue = WtfGlobal.getLocaleText("acc.field.Vendor/Customer");
        var record;
        store.each(function(r){
            if(r.data[valueField] == value){
                record = r;
                return false;
            }
        });
        if(value != ""){
            returnValue = record ? record.get(combo.displayField) : null;
        }
        return returnValue;
    },

    submitHandler : function(){
        
        if(this.startDate.getValue()>this.endDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
            return;
        }
        
      // this.loadStore();
      this.Store.load({
            params : {
                start:0,
                limit:this.pP ? this.pP.combo.getValue():30,
                isCompare: true
//                minrate: this.minExchangeRateTxt.getValue(),
//                maxrate: this.maxExchangeRateTxt.getValue()                            
            }
        });
    },

    compareHandler : function(){
        if (this.comboCurrency.getValue() == "" || this.comboCurrency.getValue() == "0"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"), WtfGlobal.getLocaleText("acc.fxexposure.selectcurrencywarning")], 2); // "Please select one currency"
            this.comboCurrency.selectOnFocus = true;
            return;
        }

        if (this.minExchangeRateTxt.getValue() == "" || this.maxExchangeRateTxt.getValue() == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"), WtfGlobal.getLocaleText("acc.fxexposure.ratewarning")], 2); // "Please select one currency"
            this.minExchangeRateTxt.selectOnFocus = true;
            return;
        }
        

        this.Store.load({
            params : {
                start:0,
                limit:15,
                isCompare: true,
                minrate: this.minExchangeRateTxt.getValue(),
                maxrate: this.maxExchangeRateTxt.getValue()                            
            }
        });
    },

    loadStore: function(){
        this.Store.load({
            params : {
                start:0,
                limit:this.pP ? this.pP.combo.getValue():30,
                isCompare: false
            }
        });
    },

    loadCustomerVendorSelectionStore:function(a,rec){
        this.comboCustomerVendor.enable();
        this.comboCurrency.enable();
        this.compareBtn.enable();
        this.submitBttn.enable();
        this.comboCurrency.setValue("");
        this.currencyid = "";
        var selectedVendorCustomerOption = rec.data.typeid;
        if(this.foreignCurrencyGainAndLoss){
            this.customerid = "";            
            this.vendorid = "";            
        }
        if (selectedVendorCustomerOption==0){
            this.optionSelected=0;
            this.isCustomerOptionSelected = true;
            if (this.foreignCurrencyGainAndLoss){
                this.Store.proxy.conn.url = "ACCInvoiceCMN/getForeignCurrencyGainAndLoss.do";
            }else{
                this.Store.proxy.conn.url = "ACCInvoiceCMN/getForeignCurrencyExposure.do";
            }
            this.customerAccStore.proxy.conn.url = "ACCCustomer/getCustomersForCombo.do";
            this.gridcm.config[1].header = WtfGlobal.getLocaleText("acc.agedReceive.gridCustomer");
        }else if (selectedVendorCustomerOption == 1){
            this.isCustomerOptionSelected = false;
            this.optionSelected=1;
            if (this.foreignCurrencyGainAndLoss){
                this.Store.proxy.conn.url = "ACCInvoiceCMN/getForeignCurrencyGainAndLoss.do";
            }else{
                this.Store.proxy.conn.url = "ACCGoodsReceiptCMN/getForeignCurrencyExposure.do";
            }
            this.customerAccStore.proxy.conn.url = "ACCVendor/getVendorsForCombo.do";
            this.gridcm.config[1].header = WtfGlobal.getLocaleText("acc.up.4");
        }else{
              if(this.foreignCurrencyGainAndLoss){
                this.optionSelected=2;
                this.comboCustomerVendor.disable();
            } else {
                this.optionSelected=2;
                this.comboCustomerVendor.disable();
                this.comboCurrency.disable();
            }
        }        

        // this.reconfigure(this.Store,this.gridcm);
        // this.grid.reconfigureGrid(this.gridcm);
        this.grid.reconfigure(this.Store, this.gridcm);
        this.grid.getView().refresh(true);        

        this.customerAccStore.load();

        this.Store.on('load',this.storeloaded,this);

        this.loadStore();

        WtfComMsgBox(29,4,true);        
    },

    loadCustomerVendorStore:function(a,rec){
        
        if(this.minExchangeRateTxt != undefined || this.minExchangeRateTxt != null){
            this.minExchangeRateTxt.value = "";
        }
        
        if(this.maxExchangeRateTxt != undefined || this.maxExchangeRateTxt != null){
            this.maxExchangeRateTxt.value = "";
        }

        if (this.isCustomerOptionSelected){
            this.customerid = rec.data.accid;            
        }else {
            this.vendorid = rec.data.accid;            
        }

        this.Store.load({
            params : {
                start:0,
                limit:this.pP ? this.pP.combo.getValue():30,
                isCompare: true,
                minrate: this.minExchangeRateTxt.getValue(),
                maxrate: this.maxExchangeRateTxt.getValue(),
                customerid: this.customerid,
                vendorid : this.vendorid
            }
        });

        WtfComMsgBox(29,4,true);        
    },

    loadDocumentTypeStore:function(a,rec){
        this.documentTypeId = rec.data.typeid;

        // TODO - to get the result based on the selected document type

        this.Store.on('load',this.storeloaded,this);

        this.loadStore();

        WtfComMsgBox(29,4,true);        
    },   

    loadCurrencyStore:function(a,rec){
        
        if(this.comboCurrency.getValue() == ""){
            if(this.minExchangeRateTxt != undefined || this.minExchangeRateTxt != null){
                this.minExchangeRateTxt.value = "";
                this.minExchangeRateTxt.disable();
            }
        
            if(this.maxExchangeRateTxt != undefined || this.maxExchangeRateTxt != null){
                this.maxExchangeRateTxt.value = "";
                this.maxExchangeRateTxt.disable();
            }
        }else{
            if(this.minExchangeRateTxt != undefined || this.minExchangeRateTxt != null){
                this.minExchangeRateTxt.enable();
            }
        
            if(this.maxExchangeRateTxt != undefined || this.maxExchangeRateTxt != null){
                this.maxExchangeRateTxt.enable();
            }
        }

        
        this.currencyid = rec.data.currencyid;

        // TODO - to get the result based on the selected currency

        this.Store.on('load',this.storeloaded,this);
        
        this.loadStore();

        WtfComMsgBox(29,4,true);        
    },            

    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.withInvMode = record.data.withoutinventory;
        this.expandStore.load({params:{parentid:record.data['billid']}});
    },

    expandRow:function(){
//        if(this.foreignCurrencyGainAndLoss){
            if(this.Store!=undefined && this.Store.getCount()>0){
                this.exportButton.enable();
                this.printButton.enable();
            } else{
                this.exportButton.disable();
                this.printButton.enable();
            }   
//        }
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

//    handleResetClick:function(){
//        if(this.quickPanelSearch.getValue()){
//            this.quickPanelSearch.reset();
////            this.comboCurrency.setValue(allCurrencyRec.currencyid);
//            // this.minExchangeRateTxt.setValue("");
//            // this.maxExchangeRateTxt.setValue("");
//
//            this.comboCurrency.reset();
//            this.minExchangeRateTxt.value = "";
//            this.maxExchangeRateTxt.value = "";
//
//            this.Store.load({
//                params: {
//                    start:0,
//                    limit:this.pP.combo.value
//                }
//            });
//            this.Store.on('load',this.storeloaded,this);
//        }
//    },
    storeloaded:function(store){
        if(store.getTotalCount() == 0){
            this.minExchangeRateTxt.value = "";
            this.maxExchangeRateTxt.value = "";
            this.minExchangeRateTxt.disable();
            this.maxExchangeRateTxt.disable();
            this.compareBtn.disable();
        }else{
            this.minExchangeRateTxt.enable();
            this.maxExchangeRateTxt.enable();
            this.compareBtn.enable();
        }
        
//        this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(this.grid);
//        },this);
        
     //   Wtf.MessageBox.hide();
//        this.quickPanelSearch.StorageChanged(store);
    },
    // enableDisableButtons:function(){
    //     if(this.sm.getCount()==1){
    //         this.editBttn.enable();
    //     } else {
    //         this.editBttn.disable();
    //     }
    // },
    handleResetClick:function(){
        this.comboCustomerVendorSelection.reset();
        this.comboCustomerVendorSelection.clearValue();
        this.comboCustomerVendor.reset();
        this.endDate.reset();
        this.startDate.reset();
        this.comboCurrency.reset();
        this.Store.removeAll();
        this.submitBttn.disable();
    },
    repeateInvoice:function(){
        var formrec= this.grid.getSelectionModel().getSelected();
        var withoutinventory= this.grid.getSelectionModel().getSelected().data.withoutinventory;
        callRepeatedInvoicesWindow(withoutinventory, formrec);
    },
    onRowClick:function(g,i,j,e){
        e.stopEvent();
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
            else{ 
                if(formrec.data.fixedAssetInvoice||formrec.data.fixedAssetLeaseInvoice){
                    callViewFixedAssetInvoice(formrec, formrec.data.billid+'Invoice',false,undefined,false,formrec.data.fixedAssetInvoice,formrec.data.fixedAssetLeaseInvoice);
                } else if(formrec.data.isConsignment){
                    callViewConsignmentInvoice(true,formrec,formrec.data.billid+'ConsignmentInvoice',false,false,true);
                }else{
                    callViewInvoice(formrec, 'ViewCashReceipt');
                }
            }
        }
    },
    /*
     * ExpandCollapse button handler
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.grid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.grid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
});

function jumpToTemplate(billid, isCustBill){
    Wtf.Ajax.requestEx({
        url: "ACC" + ("InvoiceCMN/getInvoices") + ".do",
        params: {billid:billid}
        },this,
        function(response){
            var rec = response;
            rec.data = response.data[0];
            var incash=rec.data.incash;
            if(incash &&!isCustBill)
                callViewCashReceipt(rec, 'ViewInvoice');
//            else if(incash)
//                callViewBillingCashReceipt(rec,null, 'ViewBillingCSInvoice',true);
//            else if(isCustBill)
//                callViewBillingInvoice(rec,null, 'ViewBillingInvoice',false);
            else
                callViewInvoice(rec, 'ViewCashReceipt');
        },
        function(response){

        });
}
