Wtf.account.openingBalanceWindow = function(config){
    
    this.shouldBushinessPersonGridUpdate = false;
    Wtf.apply(this,{
        buttons:[this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeOpenBalanceWin.createDelegate(this)
            })]
    },config);
    Wtf.account.openingBalanceWindow.superclass.constructor.call(this, config);
    
    this.addEvents({
        'updateBushinessPersonGrid':true//event will be fire for updating flag to refresh customer/vendor grid.
    });
}

Wtf.extend(Wtf.account.openingBalanceWindow, Wtf.Window,{
    onRender:function(config){
        Wtf.account.openingBalanceWindow.superclass.onRender.call(this,config);
        
        //create account information form
        this.createAccountInformationForm();
        //create Forms Tab Panel.
        this.createFormsTabPanel();
        // load customer Information
        this.setAccountInfo();
        
        //adding region
        
        this.add(this.northPanel= new Wtf.Panel({
            region:'north',
            bodyStyle:'background:#f1f1f1;border-bottom:1px solid #bfbfbf;',
            height:100,
            border:false,
            items:[this.accountInformationForm]
        }),this.centerPanel = new Wtf.Panel({
            region:'center',
            layout:'fit',
            border:false,
            items:[this.formsTabPanel]
        })
    );
    },
    
    setAccountInfo:function(){
        if(this.accRec!=null && this.accRec!=undefined){
            this.accountCode.setValue(this.accRec.get('acccode'));
            this.accountName.setValue(this.accRec.get('accname'));
        }
    },
    
    createAccountInformationForm:function(){
        this.accountInformationForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            height:100,
            tbar : this.formTbar(),
            bodyStyle:'margin:40px 10px 10px 30px',
//            labelWidth:100,
            items:[
                {
                    layout:'column',
                    border:false,
                    items:[{
                        columnWidth:'.45',
                        layout:'form',
                        border:false,
                        items:[
                            this.accountCode = new Wtf.form.TextField({
                                fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.dimension.module.14"):WtfGlobal.getLocaleText("acc.dimension.module.15"))+' '+WtfGlobal.getLocaleText("acc.field.Code"),
                                readOnly:true,
                                name:'accountCodeInOpenBalance',
                                width:150
                            })
                        ]
                    },{
                        columnWidth:'.45',
                        layout:'form',
                        border:false,
                        items:[
                            this.accountName = new Wtf.form.TextField({
                                fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.dimension.module.14"):WtfGlobal.getLocaleText("acc.dimension.module.15"))+' '+WtfGlobal.getLocaleText("acc.customerList.gridName"),
                                name:'accountNameInOpenBalance',
                                readOnly:true,
                                width:150
                            })
                        ]
                  }]
             }
            ]
        });
    },
    
    createFormsTabPanel:function(){
        this.createTabs();
        this.formsTabPanel = new Wtf.TabPanel({
            autoScroll: true,
//            id:'formsTabPanelId',
            bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
            border:false,
//            closable : false,
            items:[this.invoiceTab,this.paymentTab,this.creditNoteTab,this.debitNoteTab,this.poTab]
        });
        // Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
        if(Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isExciseApplicable && (Wtf.account.companyAccountPref.registrationType == Wtf.registrationTypeValues.MANUFACTURER ||Wtf.account.companyAccountPref.registrationType == Wtf.registrationTypeValues.DEALER)){
            this.formsTabPanel.add(this.exciseInvoiceTab); 
        }
        this.formsTabPanel.on('tabchange',function(tabPanel,panel){
            panel.gridStore.reload({params:{start:0,limit:30}});
        },this);
        this.formsTabPanel.setActiveTab(this.invoiceTab);
        this.formsTabPanel. doLayout();
    },
    
    createTabs:function(){
        this.invoiceTab = new Wtf.account.openingBalanceTransactionListPanel({
            title:this.isCustomer?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv"),
            isInvoice:true,
            isActivateFlag : this.isActivateFlag,
            isCustomer:this.isCustomer,
            accountId:this.accRec.get('accid'),
            border:false
        });
        
        this.invoiceTab.on('openingBalanceUpdated',this.updateCustVenGrid,this);
        
        // Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
        this.exciseInvoiceTab = new Wtf.account.openingBalanceTransactionListPanel({
            title:this.isCustomer?WtfGlobal.getLocaleText("acc.field.india.excise.invoice"):WtfGlobal.getLocaleText("acc.field.india.excise.invoice.purchase"),
            isExciseInvoice:true,
            isActivateFlag : this.isActivateFlag,
            isCustomer:this.isCustomer,
            accountId:this.accRec.get('accid'),
            border:false
        });
        
        this.exciseInvoiceTab.on('openingBalanceUpdated',this.updateCustVenGrid,this);
        this.paymentTab = new Wtf.account.openingBalanceTransactionListPanel({
            title:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.recievePay"):WtfGlobal.getLocaleText("acc.invoiceList.mP"),
            isActivateFlag : this.isActivateFlag,
            isCustomer:this.isCustomer,
            isPayment:true,
            accountId:this.accRec.get('accid'),
            border:false
        });
        
        this.paymentTab.on('openingBalanceUpdated',this.updateCustVenGrid,this);
        
        this.creditNoteTab = new Wtf.account.openingBalanceTransactionListPanel({
            title:WtfGlobal.getLocaleText("acc.accPref.autoCN"),
            isActivateFlag : this.isActivateFlag,
            isCustomer:this.isCustomer,
            isCreditNote:true,
            accountId:this.accRec.get('accid'),
            border:false
        });
        this.creditNoteTab.on('openingBalanceUpdated',this.updateCustVenGrid,this);
        
        this.debitNoteTab = new Wtf.account.openingBalanceTransactionListPanel({
            title:WtfGlobal.getLocaleText("acc.accPref.autoDN"),
            isActivateFlag : this.isActivateFlag,
            isCustomer:this.isCustomer,
            isDebitNote:true,
            accountId:this.accRec.get('accid'),
            border:false
        });
        
        this.debitNoteTab.on('openingBalanceUpdated',this.updateCustVenGrid,this);
        
        
        this.poTab = new Wtf.account.openingBalanceTransactionListPanel({
            title:WtfGlobal.getLocaleText("acc.field.Order"),
            isActivateFlag : this.isActivateFlag,
            isCustomer:this.isCustomer,
            isOrder:true,
            accountId:this.accRec.get('accid'),
            border:false
        });
        
    },
    
    
    closeOpenBalanceWin:function(){
        if(this.shouldBushinessPersonGridUpdate){
            this.fireEvent('updateBushinessPersonGrid',this);
        }
        this.close();
    },
    
    updateCustVenGrid:function(){
        this.shouldBushinessPersonGridUpdate = true;
    },
    
    formTbar : function(){
      var formTBar = [];
      var str = " Selected ";
      str += this.isCustomer?WtfGlobal.getLocaleText("acc.dimension.module.14"):WtfGlobal.getLocaleText("acc.dimension.module.15");
      str += " is ";
      str += this.isActivateFlag ? Wtf.Customer.Active : Wtf.Customer.Dormant;
      formTBar.push("->",str); 
      return formTBar;
    }
});


/*                         
 *  Synch Customer window                          
 *                         
 */
Wtf.account.customerSeqFormatWindow = function(config){
    this.isCustomer=config.isCustomer;
    this.grid=config.grid;
    this.loadingMask = new Wtf.LoadMask(document.body,{
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.field.Sync"),   //'Submit',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
                 this.fireEvent('cancel',this);
                 var panel = this.parentId;
                 this.close();
                 if(panel!=null){
                         Wtf.getCmp('as').remove(panel);
                         //panel.destroy();
                         panel=null;                 
                    }
            
                }   
    });

//    if(!this.isAccPref) {
//        this.butnArr.push({
//            text: 'Cancel',
//            scope: this,
//            handler:this.closeWin
//        });
//    }

    Wtf.apply(this,{
         buttons: this.butnArr
    },config);
    Wtf.account.customerSeqFormatWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
//        'cancel':true
    });
}

Wtf.extend(Wtf.account.customerSeqFormatWindow, Wtf.Window, {
    
    draggable:false,
    onRender: function(config){
        Wtf.account.customerSeqFormatWindow.superclass.onRender.call(this, config);
        this.createForm();
       var title=WtfGlobal.getLocaleText("acc.field.SyncCustomer.sequenceFormat");  //this.isAccPref?"Preferences":((this.personwin?"Account ":this.transectionName)+' Type');
       var msg=WtfGlobal.getLocaleText("acc.field.SyncCustomer.sequenceFormat.sel");  //this.isAccPref?"Select Preferences":('Select '+(this.personwin?"Account ":this.transectionName)+' type.');
       var isgrid=(this.isAccPref ?true:false);
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
        });
    },


    createForm:function(){
       this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'value'}
        ]);
        
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName
            }
        });
        
        this.sequenceFormatStoreVenCus = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:"autocustomerid",//reverse mode is given for create as venor and create as customer1
                isAllowNA:false
            }
        });
        this.sequenceFormatStoreVenCus.load();
        this.sequenceFormatComboVenCus = new Wtf.form.ComboBox({            
             triggerAction:'all',
             mode: 'local',
             fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"*",
             valueField:'id',
             displayField:'value',
             store:this.sequenceFormatStoreVenCus,  
             width:160,
             typeAhead: true,
             forceSelection: true,
             allowBlank:false,
             name:'sequenceformatvencus',
             hiddenName:'sequenceformatvencus'
//             disabled:true
        });
        
//        this.sequenceFormatComboVenCus.on('select',this.getNextSequenceNumberForVenCus,this);
//         this.createASCustOrVenCode = new Wtf.form.TextField({
//            fieldLabel: ((this.businessPerson=="Customer")?WtfGlobal.getLocaleText("acc.invoice.vendor"):WtfGlobal.getLocaleText("acc.invoice.customer"))+" "+WtfGlobal.getLocaleText("acc.field.Code"), 
//            name: 'custorvenacccode',
//            id: this.id+'custorvenacccode',
//            hideLabel : this.isFixedAsset,
//            hidden : this.isFixedAsset,
//            scope:this,
//            disabled:true,
//            width:160,
//            maxLength:50
////            listeners:{
////                scope:this,
////                focus:function(){
////                    this.searchText(this.AccountStore,this.pan,this.id+'custorvenacccode','code',!this.isCustomer);
////                }
////            }
//        });
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:120,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            items:[ this.sequenceFormatComboVenCus  /*this.accountType,this.makeReceivePayType,this.makeReceivePayAgainstNoteType,this.makePaymentsGLCode, this.ContraType,this.BillType*/ ]
       });
//       this.accountType.setValue(true);
    },
//    getNextSequenceNumberForVenCus:function(combo){
//      if(combo.getValue()=="NA"){
//           this.createASCustOrVenCode.reset();
////           this.createASCustOrVenCode.enable();
//              WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please select valid sequence format."],2);
//      }else{
//          Wtf.Ajax.requestEx({
//            url:"ACCCompanyPref/getNextAutoNumber.do",
//            params:{
//                from:Wtf.autoNum.customer,//this.businessPerson=="Customer"?Wtf.autoNum.vendor: reverse mode is given for "create as venor" and "create as customer1"
//                sequenceformat:combo.getValue()
//            }
//        }, this, function(resp){
//            if(resp.data=="NA"){
//                this.createASCustOrVenCode.reset();
//                this.createASCustOrVenCode.enable();
////                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please Select Valid Sequence Format."],2);
//            }else {
//                this.createASCustOrVenCode.setValue(resp.data);  
//                this.createASCustOrVenCode.disable();
//            }
//        });
//      }  
//    },
   saveForm:function(){  
          var isValid = this.TypeForm.getForm().isValid();
        
        if(!isValid){
            WtfComMsgBox(2,2);
        }else{
             var rec=this.TypeForm.getForm().getValues();
//             rec.custorvenacccode=this.createASCustOrVenCode.getValue();
             rec.sequenceformatvencus=this.sequenceFormatComboVenCus.getValue();
             rec.from=Wtf.autoNum.customer;
              Wtf.Ajax.requestEx({
                url:"ACCCustomerCMN/getCustomerFromCRMAccounts.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
            this.loadingMask.show();
            this.close();
        }
//        this.close();
    },
    closeWin:function(){
         this.fireEvent('update',this);this.close();
    },
    genSuccessResponse:function(response){
        this.grid.store.reload();
        this.loadingMask.hide();
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
//        if(response.success){
//        
////            this.disableComponent();
//       }  

    },
    genFailureResponse:function(response){
        this.loadingMask.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
    
}); 
