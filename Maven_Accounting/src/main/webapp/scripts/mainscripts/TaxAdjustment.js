Wtf.account.TaxAdjustment = function(config){
    this.isCustomer = (config.isCustomer)?config.isCustomer:false;
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.saveTransactionForm.createDelegate(this)
            }),this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeTransactionForm.createDelegate(this)
            })]
    },config);
    Wtf.account.TaxAdjustment.superclass.constructor.call(this, config);
    
    this.addEvents({
        'datasaved':true
    });
}

Wtf.extend(Wtf.account.TaxAdjustment, Wtf.Window,{
    onRender:function(config){
        Wtf.account.TaxAdjustment.superclass.onRender.call(this,config);
        var image="../../images/accounting_image/calendar.jpg";
        
        this.createFields();
        this.createTransactionForm();
        this.setValues();
        
        
        // adding form
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(this.title,'Please Fill Form Information',image)
        }, this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                id: 'centerpan'+this.id,
                autoScroll:true,
                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'border',
                items:[this.transactionInfoForm]
            })
        );
        
    },
    
    setValues:function(){
        if(this.isEdit){
            this.Number.setValue(this.record.get('documentNo'));
            this.billDate.setValue(this.record.get('documentDate'));
            this.amount.setValue(this.record.get('amount'));
            this.gstAmount.setValue(this.record.get('gstAmount'));
            this.reason.setValue(this.record.get('reason'));
        }
    },
    
    closeTransactionForm:function(){
        this.close();
    },
    
    saveTransactionForm:function(){
        if(this.transactionInfoForm.getForm().isValid()){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
            if(btn!="yes") {
                return;
            }
            var rec = this.transactionInfoForm.getForm().getValues();
            rec.documentNo=this.Number.getValue();
            rec.transactionAmount=this.amount.getValue();
            rec.gstAmount=this.gstAmount.getValue();
            rec.tax=this.Tax.getValue();
            rec.reason=this.reason.getValue();
            rec.isInputAdjustment=!this.isCustomer;
            
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            if(this.isEdit){
                rec.documentId = this.record.get('documentId');
            }

            this.url = "ACCInvoice/saveTaxAdjustment.do";
            
            Wtf.Ajax.requestEx({
                url:this.url,
                //                    url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    }else{
         WtfComMsgBox(2, 2);
    }
    },
    
    createFields:function(){
        //
        
        this.Number=new Wtf.form.TextField({
            fieldLabel:'Document No.',
            name: 'documentNo',
            disabled:this.isEdit,
            disabledClass:"newtripcmbss",
            //            anchor:'50%',
            width : 220,
            maxLength:50,
            scope:this,
            allowBlank:false,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        });
        
        this.billDate= new Wtf.form.DateField({
            fieldLabel:'Document Date',
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            value:Wtf.serverDate,
//            maxValue:this.getDates(true),
//            anchor:'50%',
            width : 220,
//            listeners:{
//                'change':{
//                    fn:this.updateDueDate,
//                    scope:this
//                }
//            },
            allowBlank:false 
        });
        
        this.amount=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            value:0,
            hideLabel:false,
            allowBlank:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            maxLength: 10,
            width:220,
            fieldLabel:WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
            name:'transactionAmount',
            listeners:{
                'change':{
                    fn:this.updateGSTAmount,
                    scope:this
                }
            }
        });
        
        this.gstAmount=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            value:0,
            hideLabel:false,
            disabled:true,
            allowBlank:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            maxLength: 10,
            width:220,
            fieldLabel:'GST Amount',
            name:'gstAmount'
//            listeners:{
//                'change':{
//                    fn:this.updateSubtotal,
//                    scope:this
//                }
//            }
        });
        
        
        //
        
        
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'}
        ]);
        
        //
        
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
            url : "ACCInvoice/getAdjustmentTax.do",
            baseParams:{
                isInputTax:!this.isCustomer
            }
        });
        
        this.taxStore.on('load',function(){
            if(this.isEdit){
                this.Tax.setValue(this.record.get('tax'));
            }
        },this);
        
        this.taxStore.load();
        
        
        this.Tax= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
//            disabled:!this.isEdit,
            hiddenName:'tax',
            width:220,
            store:this.taxStore,
//            hideLabel:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
//            hidden:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            allowBlank:false,
            listeners:{
                'select':{
                    fn:this.updateGSTAmount,
                    scope:this
                }
            },
            selectOnFocus:true
        });
        
        
        //
        
        Wtf.reasonStore.on('load',function(){
            if(this.isEdit){
                this.reason.setValue(this.record.get('reason'));
            }
        },this);
        
        Wtf.reasonStore.load();
        
        this.reason= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            allowBlank:true,
            store:Wtf.reasonStore,
            addNoneRecord: true,
//            anchor: '94%',
            width : 220,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: 'Reason',
            emptyText: 'Select Reason',
            name:'reason',
            hiddenName:'reason'
        });
        
        this.reason.addNewFn=this.addReason.createDelegate(this);
        
    },
    
    addReason:function(){
        addMasterItemWindow('29');
        Wtf.getCmp('masterconfiguration').on('update', function(){Wtf.reasonStore.reload();}, this);
    },
    
    getDates:function(start){
        var d=Wtf.serverDate;
        //        if(this.statementType=='BalanceSheet'&&start)
        //             return new Date('January 1, 1970 00:00:00 AM');
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.bbfrom)
            monthDateStr=Wtf.account.companyAccountPref.bbfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd.add(Date.YEAR, 0).add(Date.DAY, -1);
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
    createTransactionForm:function(){
        
        this.transactionInfoForm=new Wtf.form.FormPanel({
            region:'center',
            autoHeight:true,
            id:"formPanel"+this.id,
            labelWidth:150,
            border:false,
            bodyStyle: "background: transparent; padding: 20px;",
            defaultType: 'textfield',
            items:[this.Number,this.billDate,this.amount,this.Tax,this.gstAmount,this.reason]
        })
    },
    
    genSuccessResponse:function(response, request){
        if(response.success){
            WtfComMsgBox([this.title,response.msg],response.success*2+1);
            this.fireEvent('datasaved',this);
            this.close();
        }else {
            WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
        }
    },

    genFailureResponse:function(response){
        //        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    updateGSTAmount:function(){
        var amount = this.amount.getValue();
        if(amount==null || amount==undefined || amount==''){
            amount = 0;
        }
        var taxId = this.Tax.getValue();
        
        var taxRec = WtfGlobal.searchRecord(this.taxStore,taxId,'prtaxid');
        
        var taxPercent = 0;
        
        if(taxRec != undefined || taxRec != null){
            taxPercent = taxRec.get('percent');
        }
        
        var gstAmt = amount*taxPercent/100;
        
        this.gstAmount.setValue(gstAmt);
    }
});