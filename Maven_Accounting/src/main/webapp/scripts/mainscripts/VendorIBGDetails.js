
Wtf.account.VendorIBGDetails = function(config){
    
    this.isFromReceivingBankDetailsGrid = (config.isFromReceivingBankDetailsGrid)?config.isFromReceivingBankDetailsGrid:false;
    this.isEdit = (config.isEdit)?config.isEdit:false;
    this.isView = (config.isView != undefined) ? config.isView : false;
    this.isCustomer = config.isCustomer;
    this.customerId = config.customerId;
    this.editRec= config.ibgReceivingDetails;
    this.editRecCIMBbank = config.CIMBbank==undefined || config.CIMBbank==null || config.CIMBbank=='' ? false:config.CIMBbank;
    this.editRecDBSbank = config.DBSbank == undefined || config.DBSbank == null || config.DBSbank == '' ? false : config.DBSbank;
    this.editRecUOBbank = config.UOBBank == undefined || config.UOBBank == null || config.UOBBank == '' ? false : config.UOBBank;
    this.editRecOCBCBank = (config.OCBCBank == undefined || config.OCBCBank == null || config.OCBCBank == '') ? false : config.OCBCBank;
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.saveTransactionForm.createDelegate(this),
                    hidden: this.isView
            }),this.closeButton = new Wtf.Toolbar.Button({
                    text: this.isView ? WtfGlobal.getLocaleText("acc.common.close") : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeTransactionForm.createDelegate(this)
            })]
    },config);
    
    Wtf.account.VendorIBGDetails.superclass.constructor.call(this, config);
    
    this.addEvents({
        'datasaved':true,
        'canceled':true
    });
    
}

Wtf.extend(Wtf.account.VendorIBGDetails, Wtf.Window,{
    onRender:function(config){
        Wtf.account.VendorIBGDetails.superclass.onRender.call(this,config);
        var image="../../images/accounting_image/calendar.jpg";
        
        this.createAndLoadStores();
        this.createFields();
        this.createForm();
        
        if(this.isEdit){
            this.setDataBeforeEdit()
        }else if (this.isCustomer && !this.isEdit) {
            this.UOBEndToEndID.setValue(Wtf.account.companyAccountPref.uobendtoendid);//SDP-6506
            this.UOBPurposeCode.setValue(Wtf.account.companyAccountPref.uobpurposecode);
        }
        if (this.OCBCSendRemittanceAdviceVia && this.OCBCSendRemittanceAdviceVia.getValue() == "E") {
            this.OCBCRemittanceAdviceSendDetails.validator = WtfGlobal.validateEmail;
        }
           
        // adding form
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(this.title,WtfGlobal.getLocaleText("acc.vendorIBG.FillIBGDetails"),image)
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
    
    setDataBeforeEdit:function(){
        if(this.isEdit && this.editRec){
            this.bankNumber.setValue(this.editRec.get('receivingBankCode'));
            this.bankName.setValue(this.editRec.get('receivingBankName'));
            this.branchNumber.setValue(this.editRec.get('receivingBranchCode'));
            this.receivingAccNumber.setValue(this.editRec.get('receivingAccountNumber'));
            this.receivingAccName.setValue(this.editRec.get('receivingAccountName'));
            this.collectionAccNo.setValue(this.editRec.get('collectionAccNo'));
            this.collectionAccName.setValue(this.editRec.get('collectionAccName'));
            this.giroBICCode.setValue(this.editRec.get('giroBICCode'));
            this.refNumber.setValue(this.editRec.get('refNumber'));
            this.emailForGiro.setValue(this.editRec.get('emailForGiro'));
            this.DBSbank.fireEvent('check',this.DBSbank,this);
            this.CIMBbank.fireEvent('check',this.CIMBbank,this);
            this.UOBbank.fireEvent('check',this.UOBbank,this);
            this.OCBCBank.fireEvent('check',this.OCBCBank,this);
            if(this.isCustomer){
                var rec = this.editRec.data;
                this.setDataInUOBFields(rec);
            }
            if(this.editRecOCBCBank){
                this.setDataInOCBCFields(this.editRec);
            }
        }
    },
    
    createForm:function(){
        this.transactionInfoForm=new Wtf.form.FormPanel({
            region:'center',
            autoHeight:true,
            labelWidth:200,
            border:false,
            bodyStyle: "background: transparent; padding: 20px;",
            defaultType: 'textfield',
            items:[this.ibgBankSetting,this.dbsBankFields,this.cimbBankFields,this.UOBBankFields,this.OCBCBankFields]
        });
    },
    
    createFields:function(){
         
        this.ibgBankSetting = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.ibg.receivingbank"),
            id:this.id+'ibgreceivingbanksetting',
            autoHeight : true,
            width: 480,
            items:[
            this.DBSbank = new Wtf.form.Checkbox({
                fieldLabel:WtfGlobal.getLocaleText("acc.field.developmentBankOfSingapore"), // "Development Bank Of Singapore",
                id:'ibgbank'+'dbs'+this.id,
                name:'DBSbank',
                cls : 'custcheckbox',
                hidden : this.isCustomer,
                hideLabel : this.isCustomer,
                checked : this.isCustomer?false:((this.isEdit&&this.editRec)?this.editRecDBSbank:true),
                disabled:this.isEdit
            }),
            this.CIMBbank = new Wtf.form.Checkbox({
                fieldLabel:WtfGlobal.getLocaleText("acc.name.cimbbank"), // Commerce International Merchant Bankers
                id:'ibgbank'+'cimb'+this.id,
                name:'CIMBbank',
                cls : 'custcheckbox',
                hidden : this.isCustomer,
                hideLabel : this.isCustomer,
                checked: this.isCustomer?false:((this.isEdit&&this.editRec)?this.editRecCIMBbank:true),
                disabled:this.isEdit
            }),    
            this.UOBbank = new Wtf.form.Checkbox({
                fieldLabel:WtfGlobal.getLocaleText("acc.field.unitedOverseasBank"), // Commerce International Merchant Bankers
                id:'ibgbank'+'uob'+this.id,
                name:'UOBbank',
                cls : 'custcheckbox',
                hidden : !this.isCustomer,
                hideLabel : !this.isCustomer,
                checked: !this.isCustomer?false:((this.isEdit&&this.editRec)?this.editRecUOBbank:true),
                disabled:this.isEdit
            }),
            this.OCBCBank = new Wtf.form.Checkbox({
                fieldLabel: WtfGlobal.getLocaleText("acc.obbcBank.fullname"), //Oversea-Chinese Banking Corporation
                id: 'ibgbankocbc' + this.id,
                name: 'OCBCBank',
                cls: 'custcheckbox',
                hidden: this.isCustomer,
                hideLabel: this.isCustomer,
                checked: this.isCustomer ? false : ((this.isEdit && this.editRec) ? this.editRecOCBCBank : true),
                disabled: this.isEdit
            })    
            ]
        }); 
        this.dbsBankFields = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.name.dbsbanksetting"),
            id:this.id+'dbsfields',
            autoHeight : true,
            width: 480,
            hidden:true,
            defaults:{
                    disabled:this.isCustomer
                },
            items:[
            this.bankNumber=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.ibg.bank.receiving.info"),
                name: 'banknumber',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"banknumber"+this.id,
                width : 220,
                minLength:4,
                maxLength:4,
                allowNegative:false,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.vendorIBG.PleaseEnterBankNumberCode")
            }),
            this.bankName=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.ibg.bank.receiving.name"),
                name: 'bankname',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"bankname"+this.id,
                width : 220,
                maxLength:250,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.vendorIBG.PleaseEnterBankName")
            }),
            this.branchNumber=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.ibg.branch.receiving.info"),
                name: 'branchnumber',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"branchnumber"+this.id,
                width : 220,
                maxLength:3,
                maskRe: /[0-9.]/, 
                regex:/^[0-9\b]+$/,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.vendorIBG.PleaseEnterBranchNumberCode")
            }),
            this.receivingAccNumber=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.ibg.bank.receiving.acc.number"),
                name: 'receivingaccountnumber',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"receivingaccountnumber"+this.id,
                width : 220,
                maxLength:11,
                regex:/[\d]/,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.vendorIBG.PleaseEnterAccountNumber")
            }),
            this.receivingAccName=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.ibg.bank.receiving.acc.name"),
                name: 'receivingaccountname',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"receivingaccountname"+this.id,
                width : 220,
                maxLength:20,
                scope:this,
                allowBlank:false,
                emptyText: WtfGlobal.getLocaleText("acc.vendorIBG.PleaseEnterAccountName")
            })
            ]
        });
        this.bankNumber.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.bankName.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.branchNumber.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.receivingAccNumber.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.receivingAccName.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.cimbBankFields = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.name.cimbbanksetting"),
            id:this.id+'cimbfields',
            autoHeight : true,
            width: 480,
            hidden:true,
            defaults:{
                    disabled:this.isCustomer
                },
            items:[
            this.collectionAccNo=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.cimb.collectionAccNo")+'*',
                name: 'collectionAccNo',
                disabledClass:"newtripcmbss",
                id:"collectionAccNo"+this.id,
                width : 220,
                maxLength:34,
                allowNegative:false,
                scope:this,
                allowBlank:false
//                maskRe: /[a-zA-Z0-9]/
            }),
        
            this.collectionAccName=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.cimb.collectionAccName")+'*',
                name: 'collectionAccName',
                disabledClass:"newtripcmbss",
                id:"collectionAccName"+this.id,
                width : 220,
                maxLength:140,
                scope:this,
                allowBlank:false
//                maskRe: /[a-zA-Z0-9]/
            }),
            this.giroBICCode=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.cimb.giroBICCode")+'*',
                name: 'giroBICCode',
                disabledClass:"newtripcmbss",
                id:"giroBICCode"+this.id,
                width : 220,
                maxLength:11,
                minLength:11,
                allowNegative:false,
                scope:this,
                allowBlank:false
//                maskRe: /[a-zA-Z0-9]/  // Not working on chrome. Hence handled on "chnege" event (ERP-20421)
            }),
            this.refNumber=new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.cimb.refNo"),
                name: 'refNumber',
                disabledClass:"newtripcmbss",
                id:"refNumber"+this.id,
                width : 220,
                maxLength:35,
                allowNegative:false,
                scope:this
//                maskRe: /[a-zA-Z0-9]/
            }),
            
            this.emailForGiro = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.email"),
                name:'emailForGiro',
                maxLength:100,
                width : 220,
                validator:WtfGlobal.validateEmail
            })
            ]
        })
        this.collectionAccNo.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.collectionAccName.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.giroBICCode.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.collectionAccNo.on('change',function(obj){
            obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\.\,\'\"\;\:\<\>\!\~\`\_]/g, ""));
        },this);
        this.collectionAccName.on('change',function(obj){
            obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\.\,\'\"\;\:\<\>\!\~\`\_]/g, ""));
        },this);        
        this.giroBICCode.on('change',function(obj){
            obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\.\,\'\"\;\:\<\>\!\~\`\_]/g, ""));
        },this);
        this.refNumber.on('change',function(obj){
            obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\.\,\'\"\;\:\<\>\!\~\`\_]/g, ""));
        },this);        
        this.refNumber.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        
        this.UOBBankFields = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.name.uobbanksetting"),
            id:this.id+'uobfields',
            autoHeight : true,
            width: 480,
            hidden:true,
             defaults:{
                    disabled:!this.isCustomer
                },
            items:[
            this.customerBankAccountType = new Wtf.form.ComboBox({
                        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.61")+'*',
                        store: this.customerBankAccountTypeStore,
                        name:'customerbankaccounttype',
                        hiddenName:'customerbankaccounttype',
                        displayField:'name',
                        id:'customerbankaccounttype'+this.id,
                        valueField:'id',
                        mode: 'local',
                        width:220,
                        triggerAction: 'all',
                        forceSelection : true,
                        allowBlank : false
                    }),    
            this.bankNameForUOB = new Wtf.form.ComboBox({
                        fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.bankReconcile.account") + "'>" + WtfGlobal.getLocaleText("acc.bankReconcile.account") + "* </span>"                       ,
                        store: this.bankNameStore,
                        name:'banknameforuob',
                        hiddenName:'banknameforuob',
                        displayField:'name',
                        id:'banknameforuob'+this.id,
                        valueField:'id',
                        mode: 'local',
                        width:220,
                        triggerAction: 'all',
                        forceSelection : true,
                        allowBlank : false
                    }),               
            this.UOBBICCode=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.receivingBICCode.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.receivingBICCode") + "* </span>",
                name: 'uobbiccode',
                hidenName: 'uobbiccode',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"uobbiccode"+this.id,
                width : 220,
                maxLength:11,
                allowNegative:false,
                readOnly:true,
                scope:this,
                allowBlank:false
            }),
            this.bankCode=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.masterCoonfig.bankCode.qtip") + "'>" + WtfGlobal.getLocaleText("acc.masterCoonfig.bankCode") + "</span>",
                name: 'bankcode',
                hidenName: 'bankcode',
                disabledClass:"newtripcmbss",
                id:"bankcode"+this.id,
                readOnly:true,
                width : 220,
                maxLength:20,
                scope:this
            }),
            this.branchCode=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.masterCoonfig.branchCode.qtip") + "'>" + WtfGlobal.getLocaleText("acc.vendorIBG.BranchCode") + " </span>",
                name: 'branchcode',
                hidenName: 'branchcode',
                disabledClass:"newtripcmbss",
//                readOnly:true,
                id:"branchcode"+this.id,
                width : 220,
                maxLength:20,
                scope:this
            }),
            this.UOBReceivingBankAccNumber=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.receivingBankAccNO.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.receivingBankAccNO") + "* </span>",
                name: 'uobreceivingbankaccno',
                hiddenName: 'uobreceivingbankaccno',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"uobreceivingbankaccno"+this.id,
                width : 220,
                maxLength:34,
                scope:this,
                allowBlank:false
            }),
            this.UOBReceivingAccName=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.receivingAccName.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.receivingAccName") + "* </span>",
                name: 'uobreceivingaccname',
                hiddenName: 'uobreceivingaccname',
                disabled:false,
                /*
                 *Regex to avoid special characters for bank account name 
                 */
                maskRe : Wtf.avoidSpecialCharacters,
                disabledClass:"newtripcmbss",
                id:"uobreceivingaccname"+this.id,
                width : 220,
                maxLength:140,
                scope:this,
                allowBlank:false
            }),
            this.UOBCurrency=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.currencyFilterLable.qtip") + "'>" + WtfGlobal.getLocaleText("acc.common.currencyFilterLable") + "* </span>",
                name: 'uobcurrency',
                hiddenName: 'uobcurrency',
                disabled:false,
                disabledClass:"newtripcmbss",
                readOnly : true,
                id:"uobcurrency"+this.id,
                width : 220,
                maxLength:3,
                value : 'SGD',
                scope:this,
                allowBlank:false
            }),
            this.UOBEndToEndID=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.endToEndId.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.endToEndId") + "* </span>",
                name: 'uobendtoendid',
                hiddenName: 'uobendtoendid',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"uobendtoendid"+this.id,
                width : 220,
                maxLength:35,
                scope:this,
                allowBlank:false
            }),
            this.UOBMandateId=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.mandateId.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.mandateId") + "* </span>",
                name: 'uobmandateid',
                hiddenName: 'uobmandateid',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"uobmandateid"+this.id,
                width : 220,
                maxLength:35,
                scope:this,
                allowBlank:false
            }),
            this.UOBPurposeCode=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.cimb.purposeCode.qtip") + "'>" + WtfGlobal.getLocaleText("acc.cimb.purposeCode") + "* </span>",
                name: 'uobpurposecode',
                hiddenName: 'uobpurposecode',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"uobpurposecode"+this.id,
                width : 220,
                maxLength:4,
                scope:this,
                allowBlank:false
            }),
            this.UOBUltimatePayerBeneficiaryName=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.ultimatePayerName.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.ultimatePayerName") + " </span>",
                name: 'uobultimatebeneficiaryname',
                hiddenName: 'uobultimatebeneficiaryname',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"uobultimatebeneficiaryname"+this.id,
                width : 220,
                maxLength:140,
                scope:this,
                allowBlank:true
            }),
            this.UOBCustomerReference=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.customerReference.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.customerReference") + " </span>",
                name: 'uobcustomerreference',
                hiddenName: 'uobcustomerreference',
                disabled:false,
                disabledClass:"newtripcmbss",
                id:"uobcustomerreference"+this.id,
                width : 220,
                maxLength:16,
                scope:this,
                allowBlank:true
            })
            ]
        });
        if (this.isEdit) {
            this.customerBankAccountType.on('change', this.getDataForSelectedBankAccount, this);
        }
        if(this.isCustomer){
            if(this.isEdit && this.editRec){
                this.customerBankAccountTypeStore.on('load',function(){
                    this.customerBankAccountType.setValue(this.editRec.data.customerBankAccountType);
                },this);
                this.bankNameStore.on('load', function () {
                    this.bankNameForUOB.setValue(this.editRec.data.UOBBankName);
                }, this);
            }            
        }
        this.bankNameForUOB.on('select',this.onBankNameSelect,this);
        this.UOBBICCode.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.UOBReceivingBankAccNumber.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.UOBReceivingAccName.on('blur',function(obj){
            var value = obj.getValue() ? obj.getValue().trim():"";
            var patt = new RegExp(/[\~\`\!\@\#\$\%\^\&\*\_\=\<\>\[\]\{\}\\\|\?\/\.\,\"\:\'\;]/g);
            if(patt.test(value)){
                obj.invalid = true;
                obj.markInvalid("Special Characters like <b> ! @ # $ % ^ & * _ + \ = [ ] { } ; ' : \" \ | , . < > / ? ~ </b> are not allowed.");
            }else{
                obj.invalid = false;
                obj.setValue(obj.getValue().trim())
            }
        },this);
        this.UOBEndToEndID.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.UOBMandateId.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.UOBPurposeCode.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.UOBUltimatePayerBeneficiaryName.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.UOBCustomerReference.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.UOBCurrency.on('blur',function(obj){
            obj.setValue(obj.getValue().trim())
        },this);
        this.branchCode.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim())
        }, this);
        this.UOBEndToEndID.on('change',function(obj){
            obj.setValue(obj.getValue().replace(/[\~\`\!\@\#\$\%\^\&\*\_\=\<\>\[\]\{\}\\\|]/g, ""));
        },this);
        this.UOBCustomerReference.on('change',function(obj){
            obj.setValue(obj.getValue().replace(/[\~\`\!\@\#\$\%\^\&\*\_\=\<\>\[\]\{\}\\\|]/g, ""));
        },this);
        
        this.OCBCSendRemittanceStore = new Wtf.data.SimpleStore({
            fields: [{name: "id"}, {name: "name"}],
            data: [["E", "Email"], ["F", "Fax"]]
        });
        this.OCBCBankFields = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.ocbcBank.setting"),
            id: this.id + 'ocbcfields',
            autoHeight: true,
            width: 480,
            hidden: true,
            defaults: {
                disabled: this.isCustomer
            },
            items: [
                this.OCBCBankCode = new Wtf.form.TextField({
                    fieldLabel: "<span wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.ocbcBank.bankCode.qtip") + "\">" + WtfGlobal.getLocaleText("acc.masterCoonfig.bankCode") + "</span>",
                    name: 'ocbcBankCode',
                    hidenName: 'ocbcBankCode',
                    disabledClass: "newtripcmbss",
                    width: 220,
                    maxLength: 11,
                    minLength:11,
                    scope: this,
                    allowBlank:false
                }),
                this.OCBCVendorAccountNumber = new Wtf.form.TextField({
                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.ocbcBank.emplopeeAccountNumber") + "'>" + WtfGlobal.getLocaleText("acc.ocbcBank.emplopeeAccountNumber") + "</span>",
                    name: 'ocbcVendorAccountNumber',
                    hidenName: 'ocbcVendorAccountNumber',
                    disabledClass: "newtripcmbss",
                    width: 220,
                    maxLength: 34,
                    scope: this,
                    allowBlank:false
                }),
                this.OCBCUltimateCreditorName = new Wtf.form.TextField({
                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.ocbcBank.optionalField") + "'>" + WtfGlobal.getLocaleText("acc.ocbcBank.ultimateCreditorName") + "</span>",
                    name: 'ocbcUltimateCreditorName',
                    hidenName: 'ocbcUltimateCreditorName',
                    disabledClass: "newtripcmbss",
                    width: 220,
                    maxLength: 140,
                    scope: this
                }),
                this.OCBCUltimateDebtorName = new Wtf.form.TextField({
                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.ocbcBank.optionalField") + "'>" + WtfGlobal.getLocaleText("acc.ocbcBank.ultimateDebtorName") + "</span>",
                    name: 'ocbcUltimateDebtorName',
                    hidenName: 'ocbcUltimateDebtorName',
                    disabledClass: "newtripcmbss",
                    width: 220,
                    maxLength: 140,
                    scope: this
                }),
                this.OCBCSendRemittanceAdviceVia = new Wtf.form.ComboBox({
                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.ocbcBank.sendRemittanceAdviceVia.qtip") + "'>" + WtfGlobal.getLocaleText("acc.ocbcBank.sendRemittanceAdviceVia") + "</span>",
                    name: 'ocbcSendRemittanceAdviceVia',
                    hidenName: 'ocbcSendRemittanceAdviceVia',
                    store: this.OCBCSendRemittanceStore,
                    value:'E',
                    width: 220,
                    valueField: 'id',
                    displayField: 'name',
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead: true,
                    forceSelection: true
                }),
                this.OCBCRemittanceAdviceSendDetails = new Wtf.form.TextField({
                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.ocbcBank.sendDetails.qtip") + "'>" + WtfGlobal.getLocaleText("acc.ocbcBank.sendDetails++") + "</span>",
                    name: 'ocbcRemittanceAdviceSendDetails',
                    hidenName: 'ocbcRemittanceAdviceSendDetails',
                    disabledClass: "newtripcmbss",
                    width: 220,
                    maxLength: 255,
                    scope: this
                })
            ]});
        
        this.OCBCBankCode.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.OCBCVendorAccountNumber.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.OCBCUltimateCreditorName.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.OCBCUltimateDebtorName.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.OCBCRemittanceAdviceSendDetails.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        
        this.OCBCSendRemittanceAdviceVia.on('change', function (obj, newValue, oldValue) {
            if (newValue == "E") {
                this.OCBCRemittanceAdviceSendDetails.validator = WtfGlobal.validateEmail;
            } else {
                this.OCBCRemittanceAdviceSendDetails.validator = undefined;
            }
            this.OCBCRemittanceAdviceSendDetails.setValue("");
        }, this);
        
         this.DBSbank.on('check',function(cb){
            if(cb.getValue()){
                if(!this.isView){
                    this.enableFieldsetFileds(this.dbsBankFields);
                }
                this.dbsBankFields.show();
            } else {
               this.disableFieldsetComponents(this.dbsBankFields);
               this.dbsBankFields.hide();
            }
        },this);
        this.CIMBbank.on('check',function(cb){
            if(cb.getValue()){
                if(!this.isView){
                    this.enableFieldsetFileds(this.cimbBankFields);
                }
                this.cimbBankFields.show();
            }else {
                this.disableFieldsetComponents(this.cimbBankFields);
                this.cimbBankFields.hide();
            }
        },this);  
        this.UOBbank.on('check',function(cb){
            if(cb.getValue()){
                if(!this.isView){
                    this.enableFieldsetFileds(this.UOBBankFields);
                }
                this.UOBBankFields.show();
            }else {
                this.disableFieldsetComponents(this.UOBBankFields);
                this.UOBBankFields.hide();
            }
        },this);
        this.OCBCBank.on('check',function(cb){
            if(cb.getValue()){
                if(!this.isView){
                    this.enableFieldsetFileds(this.OCBCBankFields);
                }
                this.OCBCBankFields.show();
            }else {
                this.disableFieldsetComponents(this.OCBCBankFields);
                this.OCBCBankFields.hide();
            }
        },this);
        
    },
    
    saveTransactionForm:function(){
        
        if(this.UOBReceivingAccName.invalid){
            WtfComMsgBox(2, 2);
            return;
        }
        
        if(!this.transactionInfoForm.getForm().isValid()){
            WtfComMsgBox(2, 2);
            return;
        }
        if(!this.DBSbank.getValue() && !this.CIMBbank.getValue() && !this.UOBbank.getValue() && !this.OCBCBank.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.ibgdetails.noDataEntered")],2);
            return;
        }
        if(this.UOBbank.getValue()){
            if(this.UOBReceivingAccName.getValue() == this.UOBUltimatePayerBeneficiaryName.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.uob.fieldsShouldHaveDifferentValues")],2);
                return;
            }
        }
        var message = WtfGlobal.getLocaleText("acc.ibg.receiving.save.confirm");
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),message,function(btn){
            if(btn!="yes") {
                return;
            }
            
            var rec = this.transactionInfoForm.getForm().getValues();
            
            if (this.editRec != undefined && this.editRec.data != undefined && this.editRec.data.UOBReceivingBankDetailId != undefined && this.editRec.data.UOBReceivingBankDetailId != "") {
                rec.UOBReceivingBankDetailId = this.editRec.data.UOBReceivingBankDetailId;
            }
            rec.receivingBankCode = this.bankNumber.getValue();
            
            rec.receivingBankName = this.bankName.getValue();
            
            rec.receivingBranchCode = this.branchNumber.getValue();
            
            rec.receivingAccountNumber = this.receivingAccNumber.getValue();
            
            rec.receivingAccountName = this.receivingAccName.getValue();
            
            rec.collectionAccNo = this.collectionAccNo.getValue(); 
            
            rec.collectionAccName = this.collectionAccName.getValue();
            
            rec.giroBICCode = this.giroBICCode.getValue();
            
            rec.refNumber = this.refNumber.getValue();
            
            rec.emailForGiro = this.emailForGiro.getValue();
            
            rec.customerBankAccountType = this.customerBankAccountType.getValue();
            
            rec.UOBBICCode = this.UOBBICCode.getValue();
            
            rec.UOBReceivingBankAccNumber = this.UOBReceivingBankAccNumber.getValue();
            
            rec.UOBReceivingAccName = this.UOBReceivingAccName.getValue();
            
            rec.UOBEndToEndID = this.UOBEndToEndID.getValue();
            
            rec.UOBMandateId = this.UOBMandateId.getValue();
            
            rec.UOBPurposeCode = this.UOBPurposeCode.getValue();
            
            rec.UOBCustomerReference = this.UOBCustomerReference.getValue();
            
            rec.UOBUltimatePayerBeneficiaryName = this.UOBUltimatePayerBeneficiaryName.getValue();
            
            rec.UOBCurrency = this.UOBCurrency.getValue();
            
            rec.bankNameForUOB = this.bankNameForUOB.getValue();
            
            rec.UOBBankCode = this.bankCode.getValue();
            
            rec.UOBBranchCode = this.branchCode.getValue();
            
            rec.DBSbank = this.DBSbank.getValue();
            rec.CIMBbank = this.CIMBbank.getValue();
            rec.UOBbank = this.UOBbank.getValue();
            
            if (this.OCBCBank.getValue()) {
                rec.OCBCBank = this.OCBCBank.getValue();

                if (this.isEdit && this.editRec != undefined) {
                    rec.ocbcIBGDetailId = this.editRec.data.ocbcIBGDetailId;
                }
                rec.ocbcBankCode = this.OCBCBankCode.getValue();
                rec.ocbcVendorAccountNumber = this.OCBCVendorAccountNumber.getValue();
                rec.ocbcUltimateCreditorName = this.OCBCUltimateCreditorName.getValue();
                rec.ocbcUltimateDebtorName = this.OCBCUltimateDebtorName.getValue();
                rec.ocbcSendRemittanceAdviceVia = this.OCBCSendRemittanceAdviceVia.getValue();
                rec.ocbcRemittanceAdviceSendDetails = this.OCBCRemittanceAdviceSendDetails.getValue();
            }
            
            rec.isEdit = this.isEdit;
            if(this.isFromMasterConfiguration){
                rec.masterItem = this.masterItemId;
            }else{
                rec.vendor = this.vendorId;
                rec.customer = this.customerId;
            }
            
            if(this.isEdit && this.editRec != undefined){
                rec.receivingBankDetailId = this.editRec.get('ibgId');
                rec.cimbReceivingBankDetailId = this.editRec.get('cimbReceivingBankDetailId');
            }
            
            // Decide the URL to save data according to Bank.
            if(this.isFromReceivingBankDetailsGrid){                // Edit case.
                var url = '';
                if(this.isEdit){
                    if(this.editRecDBSbank){
                        url = 'ACCVendor/saveIBGReceivingBankDetails.do';
                    } else if(this.editRecCIMBbank) {
                        url = 'ACCVendor/saveCIMBReceivingBankDetails.do';
                    } else if(this.editRecUOBbank){
                        url = 'ACCCustomerCMN/saveUOBReceivingBankDetails.do';
                    }else if(this.editRecOCBCBank){
                        url = 'ACCVendorCMN/saveOCBCReceivingBankDetails.do';
                    }
                    Wtf.Ajax.requestEx({
                        url:url,
                        params: rec
                    },this,this.genSuccessResponse.createDelegate(this),this.genFailureResponse.createDelegate(this));
                } else {
                    if(this.DBSbank.getValue()){
                        url = 'ACCVendor/saveIBGReceivingBankDetails.do';
                        Wtf.Ajax.requestEx({
                            url:url,
                            params: rec
                        },this,this.genSuccessResponse.createDelegate(this),this.genFailureResponse.createDelegate(this));
                    }
                    if(this.CIMBbank.getValue()){
                        url = 'ACCVendor/saveCIMBReceivingBankDetails.do';
                        Wtf.Ajax.requestEx({
                            url:url,
                            params: rec
                        },this,this.genSuccessResponse.createDelegate(this),this.genFailureResponse.createDelegate(this));
                    }
                    if(this.UOBbank.getValue()){
                        url = 'ACCCustomerCMN/saveUOBReceivingBankDetails.do';
                        Wtf.Ajax.requestEx({
                            url:url,
                            params: rec
                        },this,this.genSuccessResponse.createDelegate(this),this.genFailureResponse.createDelegate(this));
                    }
                    if(this.OCBCBank.getValue()){
                        url = 'ACCVendorCMN/saveOCBCReceivingBankDetails.do';
                        Wtf.Ajax.requestEx({
                            url:url,
                            params: rec
                        },this,this.genSuccessResponse.createDelegate(this),this.genFailureResponse.createDelegate(this));
                    }
                }
                
            }else{
                var formJsonObject = '{\"receivingBankDetailId":\"'+(rec.receivingBankDetailId!=undefined?rec.receivingBankDetailId:"")+'\",\"receivingBankCode\":\"'+this.bankNumber.getValue()+'\",\"receivingBankName\":\"'+this.bankName.getValue()+'\",\"receivingBranchCode\":\"'+this.branchNumber.getValue()+'\",\"receivingAccountNumber\":\"'+this.receivingAccNumber.getValue()+'\",\"receivingAccountName\":\"'+this.receivingAccName.getValue()+'\",\"collectionAccNo\":\"'+(rec.collectionAccNo!=undefined?rec.collectionAccNo:"")+'\",\"collectionAccName\":\"'+(rec.collectionAccName!=undefined?rec.collectionAccName:"")+'\",\"giroBICCode\":\"'+(rec.giroBICCode!=undefined?rec.giroBICCode:"")+'\",\"cimbReceivingBankDetailId\":\"'+(rec.cimbReceivingBankDetailId!=undefined?rec.cimbReceivingBankDetailId:"")+'\",\"refNumber\":\"'+(rec.refNumber!=undefined?rec.refNumber:"")+'\"}';

                this.fireEvent('datasaved',this,formJsonObject);
                
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.ibg.receiving.saved")],3);
                
                this.close();
            }                      
                    
        },this);
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
    
    closeTransactionForm:function(){
        this.fireEvent('canceled',this);
        this.close();
    },
    disableFieldsetComponents:function(fieldSet){
        for(var x=0;x<fieldSet.items.length;x++){
            fieldSet.items.itemAt(x).disable();
        }
    },
    enableFieldsetFileds :function(fieldSet){
        for(var x=0;x<fieldSet.items.length;x++){
            fieldSet.items.itemAt(x).enable();
        }
    },
    getDataForSelectedBankAccount: function(obj){
        var bankAccountType =  this.customerBankAccountType.getValue();
        Wtf.Ajax.requestEx({
            url:'ACCCustomerCMN/getUOBReceivingBankDetails.do',
            params: {
                customer : this.customerId,
                isForForm:true,
                customerBankAccountType : bankAccountType
            }
        },this,this.genSuccessResponseOnFormData,this.genFailureResponse.createDelegate(this));
    },
    genSuccessResponseOnFormData : function(response){
        if(response.success){
            if(response.dataExists){
                var data = response.data;
                var rec = data[0];
                this.setDataInUOBFields(rec);
            }
        }
    },
    setDataInUOBFields : function(rec){
        
        this.UOBBICCode.setValue(rec.UOBBICCode);
        this.UOBReceivingBankAccNumber.setValue(rec.UOBReceivingBankAccNumber);
        this.UOBReceivingAccName.setValue(rec.UOBReceivingAccName);
        this.UOBCurrency.setValue(rec.UOBCurrency);
        this.UOBEndToEndID.setValue(rec.UOBEndToEndID);
        this.UOBMandateId.setValue(rec.UOBMandateId);
        this.UOBUltimatePayerBeneficiaryName.setValue(rec.UOBUltimatePayerBeneficiaryName);
        this.UOBPurposeCode.setValue(rec.UOBPurposeCode);
        this.UOBCustomerReference.setValue(rec.UOBCustomerReference);
        this.bankCode.setValue(rec.UOBBankCode);
        this.branchCode.setValue(rec.UOBBranchcode);
    },
    
    setDataInOCBCFields:function(rec){
        var data = rec.data;
        this.OCBCBankCode.setValue(data.ocbcBankCode);
        this.OCBCVendorAccountNumber.setValue(data.ocbcVendorAccountNumber);
        this.OCBCUltimateCreditorName.setValue(data.ocbcUltimateCreditorName);
        this.OCBCUltimateDebtorName.setValue(data.ocbcUltimateDebtorName);
        this.OCBCSendRemittanceAdviceVia.setValue(data.ocbcSendRemittanceAdviceVia);
        this.OCBCRemittanceAdviceSendDetails.setValue(data.ocbcRemittanceAdviceSendDetails);
    },
    createAndLoadStores:function(){
    this.customerBankAccountTypeStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },Wtf.salesPersonRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:61
            }
        });
        if(this.isCustomer){
            this.customerBankAccountTypeStore.load();
        }     
    
    this.bankNameRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'name'
        },{
            name: 'userid'
        },{
            name:'hasAccess'
        },{
            name:'BICCode'
        },{
            name:'bankCode'
        },{
            name:'branchCode'
        }
        ]
    );    
    this.bankNameStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.bankNameRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:2
            }
        });
        if(this.isCustomer){
            this.bankNameStore.load();
        }            
    },
    onBankNameSelect:function(obj){
        var record = WtfGlobal.searchRecord(this.bankNameStore, this.bankNameForUOB.getValue(), "id");
        var BICCode = record.data.BICCode;
        var bankCode = record.data.bankCode;
        var branchCode = record.data.branchCode;
        
        this.UOBBICCode.setValue(BICCode);
        this.bankCode.setValue(bankCode);
        this.branchCode.setValue(branchCode);
    }
});
