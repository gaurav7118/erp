Wtf.ChequeSequenceFormat = function(config){
    Wtf.apply(this,config);
    this.addEvents({
        'setAutoNumbers':true
    });
    Wtf.ChequeSequenceFormat.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:this.saveSequenceNumber 
            },
            {
                text: WtfGlobal.getLocaleText("acc.common.reset"), //"Reset",
                scope: this,
                handler: this.resetFormat
            }, {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.ChequeSequenceFormat,Wtf.Window,{
    layout:"border",
    modal:true,
    title:WtfGlobal.getLocaleText("acc.field.AddSequenceFormat"),
    id: 'chequesequenceformatlinkforaccounting',
    width: 650, //SDP-416
    height: 585,
    resizable: false,
    bodyStyle:"background-color:#f1f1f1",
    iconCls: "pwnd deskeralogoposition",
    onRender:function(config){
        Wtf.ChequeSequenceFormat.superclass.onRender.call(this,config);
//        Wtf.Ajax.requestEx({
//            url : "ACCCompanyPref/getChequeNumberSequenceFormat.do"
//        },this,
//        function(req,res){
//            var restext=req;
//            if(restext.success){
//                if(this.numberofdigit)
//                    this.numberofdigit.setValue(restext.chequeDigitNumbers);
//                if(this.startfrom)
//                    this.startfrom.setValue(restext.chequeNoStartFrom);
//                if(this.showleadingzero)
//                    this.showleadingzero.setValue(restext.isShowLeadingZero);
//            }
//        },
//        function(req){
//        });
        
    },
    initComponent:function (){
        Wtf.ChequeSequenceFormat.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);        
        this.add(this.centerPanel);
        this.add(this.AddEditForm);
    },
    
    GetNorthPanel:function(){
        this.sequenceFormatRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'value'},
            {name: 'bankName'},
            {name: 'numberofdigit'},
            {name: 'startfrom'},
            {name: 'showleadingzero'},
            {name: 'isChecked'},
            {name: 'chequeEndNumber'},
            {name: 'accid'},
            {name: 'isdefault'},
            {name: 'prefix'},
            {name: 'suffix'},
            {name: 'resetcounter'}, 
            {name: 'showdateinprefix'},
            {name: 'selecteddateformat'},
            {name: 'showdateafterprefix'},
            {name: 'oldflag'},
            {name: 'selecteddateformatafterprefix'},
            {name: 'showdateaftersuffix'},
            {name: 'dateformataftersuffix'}
         ]);
         
         this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
                
            },this.sequenceFormatRec),
            url : "ACCCompanyPref/getChequeSequenceFormatStore.do"
         });
         this.sequenceFormatStore.load();
         
         this.sequenceCM= new Wtf.grid.ColumnModel([
             {
                header: WtfGlobal.getLocaleText("acc.label.bank"),
                dataIndex: 'bankName'
            }, {
                header: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
                dataIndex: 'value'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.showDateInPrefix"),
                dataIndex: 'showdateinprefix'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.showDateAfterPrefix"), //Show Date After Prefix
                dataIndex: 'showdateafterprefix'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.showDateAfterSuffix"),
                dataIndex: 'showdateaftersuffix'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.counterreset"),
                dataIndex: 'resetcounter'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.Prefix"),
                dataIndex: 'prefix'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.Suffix"),
                dataIndex: 'suffix'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.NumberofDigit"),
                dataIndex: 'numberofdigit'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.StartFrom"),
                dataIndex: 'startfrom'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.ShowLeadingZero"),
                dataIndex: 'showleadingzero',
                renderer: function (val) {
                    var retVal = val;
                    if (val) {
                        retVal = 'Yes';
                    } else {
                        retVal = 'No';
                    }
                    return retVal;
                }
            }, {
                header: WtfGlobal.getLocaleText("acc.field.isActivated"),
                dataIndex: 'isChecked',
                renderer: function (val) {
                    return val ? "Yes" : "No";
                }
            }, {
                header: WtfGlobal.getLocaleText("acc.designerTemplate.isDefault"),
                dataIndex: 'isdefault',
                renderer: function (val) {
                    return val ? "Yes" : "No";
                }
            }, {
                header: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
                align: 'center',
                renderer: function () {
//                      return "<div class='pwnd deleteSequenceNo'> </div>";
                    return "<div class='pwnd delete-gridrow' > </div>";
                }
            }
        ]);
        
        this.sequencenoGrid = new Wtf.grid.GridPanel({
          store: this.sequenceFormatStore,
          cm:this.sequenceCM,
          height:280,
          autoScroll:true, //SDP-416
          viewConfig:{
              forceFit:true,
              emptyText:WtfGlobal.getLocaleText("acc.field.Norecordfound")
          }
      });
      
      this.sequencenoGrid.on("cellclick", this.deleteSequence, this);
      
      this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.field.AddSequenceFormat"),WtfGlobal.getLocaleText("acc.field.AddSequenceFormatfor")+"Cheque",'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
        
        this.centerPanel = new Wtf.Panel({
            region:"center",
            layout: 'fit',
            items:[this.sequencenoGrid]
        })
    },
    
    GetAddEditForm:function(){
        
        this.accRec = Wtf.data.Record.create([
            {name:'accname'},
            {name:'accid'},
            {name:'acccode'},
            {name:'groupname'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                ignoreCashAccounts:true,
                ignoreGLAccounts:true,
                ignoreGSTAccounts:true,
                ignorecustomers:true,
                ignorevendors:true,
                mode:2,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
        this.accountStore.load();
        
        this.bankAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.label.bank.account"),
            store:this.accountStore,
            emptyText:'Please select an account',
            width:200,
            name:'bankaccount',
            anchor:'85%',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAhead: true,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
            typeAheadDelay:30000,
            valueField:'accid',
            displayField:'accname',
            forceSelection: true,
            allowBlank: false
        });
        
        
        this.numberofdigit = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.NumberofDigit*"),
            name:'numberofdigit',
            allowNegative:false,
            allowBlank:false,
            width:200,
            allowDecimals:false,
            minValue:1,
            maxValue:10,
            emptyText:WtfGlobal.getLocaleText("acc.field.Pleaseenternumberofdigit")
        });
        
        this.startfrom = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.StartFrom"),
            name:'startfrom',
            allowNegative:false,
            maxLength:15,
            width:200,
//            disabled:true,            
//            allowBlank:true,
            allowDecimals:false,
            emptyText:WtfGlobal.getLocaleText("acc.field.Pleaseenterstartvalue")
        });
        
        this.chequeEndNumber = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.chequesequenceformate.ChequeEndNumber"),
            name: 'chequeendnumber',
            allowNegative: false,
            allowBlank: false,
            width: 200,
            allowDecimals: false,
            minValue: 1,
//            maxValue:10,
            emptyText: WtfGlobal.getLocaleText("acc.chequesequenceformate.PleaseenterChequeEndNumber")
        });
        
        this.showleadingzero = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowLeadingZero"),
            name:'showleadingzero',
            width:200,
//            disabled:true,
            checked:true
        });
        
        this.isCheckedCheckBox = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.isActivated"),
            name: 'isChecked',
            checked: false
        });
        
        this.isdefault = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.designerTemplate.isDefault"),
            name: 'isdefault',
            checked: false
        });
        this.formatStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'formatid',
                type:'string'
            }, 'name'],
            data :[['YYYY','YYYY'],['YYYYMM','YYYYMM'],['YYYYMMDD','YYYYMMDD'], ['YY','YY'], ['YYMM','YYMM'], ['YYMMDD','YYMMDD'], ['empty','']]
        });
        this.showDateinPrefix = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.showDateInPrefix"),
            name:'showdateinprefix',
            checked:false
        });
        this.selectDateFormat = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.selectDateFormat"),
            name:'showdateinprefix',
            store:this.formatStore,
            forceSelection: true,
            valueField:'formatid',
            displayField:'name',
//            hidden:this.showDateinPrefix.getValue(),
//            hidelabel:this.showDateinPrefix.getValue(),
            disabled:true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
            
        });
        //Show date after prefix
        this.showdateafterprefix = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.showDateAfterPrefix"),
            name:'showdateafterprefix',
            checked:false
        });
        this.selectDateFormatAfterPrefix = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.selectDateFormat"),
            name:'showdateafterprefix',
            store:this.formatStore,
            forceSelection: true,
            valueField:'formatid',
            displayField:'name',
            disabled:true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
            
        });
        
        this.showDateAfterSuffix = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.showDateAfterSuffix"),
            name:'showdatefftersuffix',
            checked:false
        });
        
        this.selectDateFormatForSuffix = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.selectDateFormatSuffix"),
            name:'selectdateformatforsuffix',
            store:this.formatStore,
            forceSelection: true,
            valueField:'formatid',
            displayField:'name',
            disabled:true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.showDateinPrefix.on('check',function(obj,isChecked){
            if(isChecked){
                this.selectDateFormat.reset();
                this.selectDateFormat.enable();
                this.resetCounter.enable();
            }else{
                this.selectDateFormat.reset();
                this.selectDateFormat.clearValue();
                this.selectDateFormat.disable();
                if(!this.showDateAfterSuffix.getValue()){//when both prefix and suffix get delected then 
                    this.resetCounter.setValue(false);
                    this.resetCounter.disable();
                }
            }

        },this); 
        
        this.showDateAfterSuffix.on('check',function(obj,isChecked){
            if(isChecked){
                this.selectDateFormatForSuffix.reset();
                this.selectDateFormatForSuffix.enable();
                this.resetCounter.enable();
            }else{
                this.selectDateFormatForSuffix.reset();
                this.selectDateFormatForSuffix.clearValue();
                this.selectDateFormatForSuffix.disable();
                if(!this.showDateinPrefix.getValue()){//when both prefix and suffix get delected then 
                    this.resetCounter.setValue(false);
                    this.resetCounter.disable();
                }
            }
        },this); 
        
        this.showdateafterprefix.on('check',function(obj,isChecked){
            if(isChecked){
                this.selectDateFormatAfterPrefix.reset();
                this.selectDateFormatAfterPrefix.enable();
                this.resetCounter.enable();
            }else{
                this.selectDateFormatAfterPrefix.reset();
                this.selectDateFormatAfterPrefix.clearValue();
                this.selectDateFormatAfterPrefix.disable();
                if(!this.showDateAfterSuffix.getValue() && !this.showDateinPrefix.getValue()){//when both prefix and suffix get detected then 
                    this.resetCounter.setValue(false);
                    this.resetCounter.disable();
                }
            }

        },this); 
        
        this.resetCounter = new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.counterreset") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.counterresethelpmessage")),
            name:'resetcounter',
            checked:false,
            disabled:true 
        });
        
        this.prefix = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Prefix"),
            name:'prefix',
            maxLength:20,
            emptyText:WtfGlobal.getLocaleText("acc.field.PleaseenterPrefixvalue")
        });
        this.suffix = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Suffix"),
            name:'suffix',
            maxLength:20,
            emptyText:WtfGlobal.getLocaleText("acc.field.PleaseenterSuffixvalue")
        });
        var notemsg = new Wtf.XTemplate(
            "<div> &nbsp;</div>",
            '<tpl>',
            "<div class='openingOrderText'><br><br><b>Note</b> : System Can have only one sequence format for one Bank Account, Old Sequence Format will be updated if you are making a new Sequence Format for same bank account</div>",
            '</tpl>'
        );
        
        var notemsgPanel = new Wtf.Panel({
            border:false,
//            bodyStyle:'padding-top:50px',
            html:notemsg.apply()
        })
        
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"south",
            border:false,
            height:315,
            autoScroll:true,
            labelWidth: 160, //SDP-416
            defaults:{                
                width:250 //SDP-416
            },
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:15px;",
            items:[this.bankAccount,this.showDateinPrefix,this.selectDateFormat,this.showdateafterprefix,this.selectDateFormatAfterPrefix,this.showDateAfterSuffix,this.selectDateFormatForSuffix,this.resetCounter,this.prefix,this.suffix,this.numberofdigit,this.startfrom,this.chequeEndNumber,this.showleadingzero,this.isdefault,this.isCheckedCheckBox,notemsgPanel]
        });
        this.isEdit=false;
    },
    
    deleteSequence:function(gd, ri, ci, e) {
        var event = e;
        if(event.target.className == "pwnd delete-gridrow") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedsequenceformat"),function(btn){
            if(btn=="yes") {
            var size=gd.getStore().getCount(); 
            if(size<1){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.msgbox.5")],0);
                return;
            }
            var sequenceformat = gd.getStore().getAt(ri).data.value;
            var id = gd.getStore().getAt(ri).data.id;
            Wtf.Ajax.requestEx({
//                        url:Wtf.req.base+'UserManager.jsp',
                        url : "ACCCompanyPref/deleteChequeSequenceFormat.do",
                        params:{
                            sequenceformat:sequenceformat,
                            id:id
                        }
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.sequenceFormatStore.remove(this.sequenceFormatStore.getAt(ri));
                            var count=this.sequenceFormatStore.getCount();
                            var i=0;
                            var remainingFormats="";
                            while(i<count){
                                if(remainingFormats!=""){
                                    remainingFormats+=","+this.sequenceFormatStore.getAt(i).data.value;
                                }else{
                                    remainingFormats+=this.sequenceFormatStore.getAt(i).data.value;
                                }
                                i++ ;
                            }
                            this.fireEvent('setAutoNumbers',remainingFormats, false);
//                            this.close();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.sequence.format.delete")],0);                            
                        } else 
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.sequence.format.deletefailure")],1);

                    },
                    function(req){
                        var restext=req;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.sequence.format.deletefailure")],1);
                    });
        }
            }, this),
                    this.resetFormat();
        } else {
            this.editForm(gd, ri)
        }
   },
    
    saveSequenceNumber:function(){
        if(!this.AddEditForm.form.isValid()){
           return;
         }else if (this.startfrom.getValue() > this.chequeEndNumber.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.chequesequenceformate.startfromalert")], 0);
        } else {
            var showDateInPrefix=this.showDateinPrefix.getValue();
         if(showDateInPrefix!=undefined && showDateInPrefix==true && this.selectDateFormat.getValue()==''){
            this.showDateinPrefix.markInvalid();
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.selectanyDateFormat")],0);
             return;
         }
         
         var showdateafterprefix=this.showdateafterprefix.getValue();
         if(showdateafterprefix!=undefined && showdateafterprefix==true && this.selectDateFormatAfterPrefix.getValue()==''){
            this.showdateafterprefix.markInvalid();
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.selectanyDateFormat")],0);
             return;
         }
         
        var showDateAfterSuffix=this.showDateAfterSuffix.getValue();
        if(showDateAfterSuffix!=undefined && showDateAfterSuffix==true && this.selectDateFormatForSuffix.getValue()==''){
            this.selectDateFormatForSuffix.markInvalid();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.selectanyDateFormatforsuffix")],0);
            return;
        }
            
            this.isChecked = this.isCheckedCheckBox.getValue();
            if(this.isEdit){
                var ri=this.sequencenoGrid.getSelectionModel().getSelections();
                var id=ri[0].data.id;
                this.saveFormat(true,id);
            }
            else{
                this.saveFormat(false);
            }
        }
    },
    
    
    saveFormat:function(isEdit,id){
        var param={
            prefix: this.prefix.getValue(),
            suffix: this.suffix.getValue(),
            showdateinprefix: this.showDateinPrefix.getValue(),
            showdateafterprefix: this.showdateafterprefix.getValue(),
            selecteddateformat: this.selectDateFormat.getValue(),
            selecteddateformatafterprefix: this.selectDateFormatAfterPrefix.getValue(),
            showdateaftersuffix: this.showDateAfterSuffix.getValue(),
            selectedsuffixdateformat: this.selectDateFormatForSuffix.getValue(),
            resetcounter : this.resetCounter.getValue(),
            numberofdigit : this.numberofdigit.getValue(),
            startfrom : this.startfrom.getValue(),
            showleadingzero : this.showleadingzero.getValue(),
            bankAccountId:this.bankAccount.getValue(),
            isChecked: this.isChecked,
            chequeEndNumber : this.chequeEndNumber.getValue(),
            isSequenceFormat:true,
            isdefault:this.isdefault.getValue(),
            id:id,
            isEdit:isEdit
        }
        
        Wtf.Ajax.requestEx({
            url : "ACCCompanyPref/saveChequeSequenceFormat.do",
            params:param
        },this,
        function(req,res){
            var restext=req;
            if(restext.success){
                if(restext.isduplicate){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ThisSequenceFormatalreadyexistsPlease")],0);                               
                }else{
                    this.fireEvent('setAutoNumbers',restext.name, true);
                    this.close();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.sequence.format.save")],0);                            
                }
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.companypreferences.cannotEditSequenceFormat")],2);
            }

        },
        function(req){
            var restext=req;
            if(restext.msg !=""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.sequence.format.failure")],1);
            }
        });
    },
     editForm: function (gd, ri) {
        this.prefix.setValue(gd.getStore().getAt(ri).data.prefix);
        this.suffix.setValue(gd.getStore().getAt(ri).data.suffix);
        var isshowdateinprefix = (gd.getStore().getAt(ri).data.showdateinprefix == "Yes") ? true : false;
        this.showDateinPrefix.setValue(isshowdateinprefix);
        this.selectDateFormat.setValue(gd.getStore().getAt(ri).data.selecteddateformat);  
        var isshowdateafterprefix = (gd.getStore().getAt(ri).data.showdateafterprefix == "Yes")?true:false;
        this.showdateafterprefix.setValue(isshowdateafterprefix);   
        this.selectDateFormatAfterPrefix.setValue(gd.getStore().getAt(ri).data.selecteddateformatafterprefix);
        var showdateaftersuffix = (gd.getStore().getAt(ri).data.showdateaftersuffix == "Yes")?true:false;
        this.showDateAfterSuffix.setValue(showdateaftersuffix);
        this.selectDateFormatForSuffix.setValue(gd.getStore().getAt(ri).data.dateformataftersuffix);
        var resetCounter = (gd.getStore().getAt(ri).data.resetcounter == "Yes")?true:false;
        this.resetCounter.setValue(resetCounter);
        if(isshowdateinprefix || showdateaftersuffix){//if date suffix or prefix availble in this case user can reset counter
            this.resetCounter.enable();
        } else {
            this.resetCounter.disable();
        }
        this.bankAccount.setValue(gd.getStore().getAt(ri).data.accid);
        this.numberofdigit.setValue(gd.getStore().getAt(ri).data.numberofdigit);
        this.chequeEndNumber.setValue(gd.getStore().getAt(ri).data.chequeEndNumber);
        this.startfrom.setValue(gd.getStore().getAt(ri).data.startfrom);
        var showleadingzero = (gd.getStore().getAt(ri).data.showleadingzero == true) ? true : false;
        this.showleadingzero.setValue(showleadingzero);
        var isChecked = gd.getStore().getAt(ri).data.isChecked;
        this.isCheckedCheckBox.setValue(isChecked);
        var isdefault = gd.getStore().getAt(ri).data.isdefault;
        this.isdefault.setValue(isdefault);
        this.disableComponent();
        this.isEdit = true;
    },
    
    disableComponent:function(){
        this.bankAccount.disable();
        this.numberofdigit.disable();
        this.chequeEndNumber.disable();
        this.startfrom.disable();
        this.showleadingzero.disable();
        this.prefix.disable();
        this.suffix.disable();
        this.numberofdigit.disable();
        this.showleadingzero.disable();      
        this.showDateinPrefix.disable();
        this.showdateafterprefix.disable();
        this.selectDateFormat.disable();      
        this.selectDateFormatAfterPrefix.disable();
        this.showDateAfterSuffix.disable();
        this.selectDateFormatForSuffix.disable();
    },    
    resetFormat: function () {
        this.prefix.enable();
        this.suffix.enable();
        this.numberofdigit.enable();
        this.showDateinPrefix.enable();
        this.showdateafterprefix.enable();
        this.showDateAfterSuffix.enable();
        this.prefix.reset();
        this.suffix.reset();
        this.numberofdigit.reset();
        this.showDateinPrefix.reset();
        this.showdateafterprefix.reset();
        this.showDateAfterSuffix.reset();
        this.selectDateFormat.clearValue();//set to Empty Record
        this.selectDateFormatAfterPrefix.clearValue();
        this.selectDateFormatForSuffix.clearValue();
        this.resetCounter.setValue(false);
        this.resetCounter.disable();
        this.numberofdigit.enable();
        this.startfrom.enable();
        this.showleadingzero.enable();
        this.showleadingzero.reset();
        this.isCheckedCheckBox.reset();
        this.numberofdigit.reset();
        this.startfrom.reset();
        this.showleadingzero.reset();
        this.bankAccount.enable();
        this.bankAccount.reset();
        this.chequeEndNumber.enable();
        this.chequeEndNumber.reset();
        this.AddEditForm.enable();
        this.isEdit = false;

    }
})