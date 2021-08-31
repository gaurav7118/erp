/*
 * Folowing Component is used for Setting  Budget options at company level From Company Preferences.
 */

Wtf.account.BudgetSetting = function(config){
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
            minWidth: 50,
            scope: this,
            handler: this.saveForm.createDelegate(this)
        }),this.closeButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
            minWidth: 50,
            scope: this,
            handler: this.closeOpenWin.createDelegate(this)
        })]
    },config);
    
    Wtf.account.BudgetSetting.superclass.constructor.call(this, config);
    
}

Wtf.extend(Wtf.account.BudgetSetting, Wtf.Window,{
    
    onRender:function(config){
        Wtf.account.BudgetSetting.superclass.onRender.call(this,config);
        
        this.createBudgetSettingForm();
        
        
        this.add({
            region: 'north',
            height: 10,
            border: false,
            bodyStyle: 'margin-top:20px',
            baseCls:'bckgroundcolor',
            items:[this.budgetSettingForm]
        },{
            region: 'center',
            border: false,
            //                autoScroll:true,
            layout:'fit',
            baseCls:'bckgroundcolor',
            items:[this.budgetmethodSettingForm]
        }
        );
        
    },
    
    createBudgetSettingForm:function(){
        
        this.BudgetActivation= new Wtf.form.Checkbox({
            name:'BudgetActivation',
            id:'BudgetActivation'+this.id,
            hiddeName:'BudgetActivation',
            // labelSeparator:'', 
            fieldLabel:WtfGlobal.getLocaleText("acc.field.activateBudgetingonDepartment"),
            checked:Wtf.account.companyAccountPref.activatebudgetingforPR,          
            cls : 'custcheckbox',
            width: 10
        });
        
        
        this.BudgetActivation.on('check', this.BudgetActivationChanged,this);
        
        // create radio button group for budget
        
        this.department = new Wtf.form.Radio({
            name:'assetacquisition',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'0',
            width:300,
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetType==0),
            boxLabel:WtfGlobal.getLocaleText("acc.field.department") //'Department'
        });
        
        this.deptandProd = new Wtf.form.Radio({
            name:'assetacquisition',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'1',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetType==1),
            boxLabel:WtfGlobal.getLocaleText("acc.field.departmentandprod")//'Department and Product'
        });
        
        this.deptProdandCategory = new Wtf.form.Radio({
            name:'assetacquisition',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'2',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetType==2),
//            boxLabel:WtfGlobal.getLocaleText("acc.field.departmentandprodcategory") //'Department and Product Category'
            hidden: true
        });
        
        
        
        this.budgetingbasedon = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoHeight:true,
            anchor:'100%',
            title:WtfGlobal.getLocaleText("acc.field.BudgetingBasedOn"),
            defaults:{
                border:false
            },
            items:[this.department,this.deptandProd,this.deptProdandCategory]
        });
        //create radio group for frequency type
        this.monthly = new Wtf.form.Radio({
            name:'frequency',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'0',
            width:300,
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetFreqType==0),
            boxLabel:WtfGlobal.getLocaleText("acc.field.monthly") //'Monthly'
        });
        
        this.bimonthly = new Wtf.form.Radio({
            name:'frequency',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'1',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetFreqType==1),
            boxLabel:WtfGlobal.getLocaleText("acc.field.biMonthly") //'Bi-Monthly'
        });
        
        this.quarterly = new Wtf.form.Radio({
            name:'frequency',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'2',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetFreqType==2),
            boxLabel:WtfGlobal.getLocaleText("acc.field.quarterly") //'Quarterly'
        });
        this.halfyearly = new Wtf.form.Radio({
            name:'frequency',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'3',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetFreqType==3),
            boxLabel:WtfGlobal.getLocaleText("acc.field.halfYearly") //'Half Yearly'
        });
        this.yearly = new Wtf.form.Radio({
            name:'frequency',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'4',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetFreqType==4),
            boxLabel:WtfGlobal.getLocaleText("acc.field.yearly") //'Yearly'
        });
        this.budgetingfrequency = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoHeight:true,
            anchor:'100%',
            title:WtfGlobal.getLocaleText("acc.field.BudgetingFrequencyType"),//'Budgeting Frequency Type',
            defaults:{
                border:false
            },
            items:[this.monthly,this.bimonthly,this.quarterly,this.halfyearly, this.yearly]
        });
        //warn or Block for minimum Budgeting 
       
        this.ignore = new Wtf.form.Radio({
            name:'warnblock',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'0',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetwarnblock==0),
            boxLabel:WtfGlobal.getLocaleText("acc.field.ignore") //'Ignore'
        });
        this.warn = new Wtf.form.Radio({
            name:'warnblock',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'1',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetwarnblock==1),
            boxLabel:WtfGlobal.getLocaleText("acc.field.warn") //'Warn'
        });
        this.block = new Wtf.form.Radio({
            name:'warnblock',
            labelAlign : 'left',
            disabled:(!Wtf.account.companyAccountPref.activatebudgetingforPR),
            inputValue :'2',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.budgetwarnblock==2),
            boxLabel:WtfGlobal.getLocaleText("acc.field.block") //'Block'
        });
        this.warnBlock = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.field.MinimumBudgetSettings"),//'Minimum Budget Setting',
            defaults:{
                border:false
            },
            items:[this.ignore,this.warn,this.block]
        });
       
        
        this.budgetingMethod = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoWidth:true,
            autoHeight:true,
            anchor:'100%',
            title:WtfGlobal.getLocaleText("acc.field.BudgetingMethod"),//'Budgeting Method',
            defaults:{
                border:false
            },
            items:[this.budgetingbasedon,this.budgetingfrequency,this.warnBlock]
        });
        
        
        this.budgetSettingForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            autoHeight:true,
            labelWidth:300,
            anchor:'100%',
            bodyStyle:'margin:10px',
            items:[this.BudgetActivation]
        });
        
        this.budgetmethodSettingForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            height:400,
            labelWidth:0,
            anchor:'100%',
            bodyStyle:'margin:10px',
            items:[this.budgetingMethod]
        });
        
    },
    
    
    closeOpenWin:function(){
        this.close();
    },
    
    saveForm:function(){
        //        this.saveButton.disable();
        
        var budgetingType = 0;
        var frequencyType = 0;
        var warnblocktype = 0;
        
        //Budget type
        if (this.department.getValue()){
            budgetingType = 0;
        } else if(this.deptandProd.getValue()){
            budgetingType = 1;
        } else if(this.deptProdandCategory.getValue()){
            budgetingType = 2;
        }
        //Frequency Type
        if (this.monthly.getValue()){
            frequencyType = 0;
        } else if(this.bimonthly.getValue()){
            frequencyType = 1;
        } else if(this.quarterly.getValue()){
            frequencyType = 2;
        }else if(this.halfyearly.getValue()){
            frequencyType = 3;
        }else if(this.yearly.getValue()){
            frequencyType = 4;
        }
        //warnblock type this.ignore,this.warn,this.block
        if (this.ignore.getValue()){
            warnblocktype = 0;
        } else if(this.warn.getValue()){
            warnblocktype = 1;
        } else if(this.block.getValue()){
            warnblocktype = 2;
        } 
        
        //        var depreciationCalculationMethod
        
        Wtf.Ajax.requestEx({
            url : "ACCCompanyPref/saveBudgetSetting.do",
            params:{
                activatebudgetingforPR:this.BudgetActivation.getValue(),
                budgetType:budgetingType,
                budgetFreqType:frequencyType,
                budgetwarnblock:warnblocktype
                
            }
        },this,
        function(req,res){
            var restext=req;
            if(restext.success){
                Wtf.account.companyAccountPref.activatebudgetingforPR = this.BudgetActivation.getValue();
                Wtf.account.companyAccountPref.budgetType=budgetingType;
                Wtf.account.companyAccountPref.budgetwarnblock=warnblocktype;
                Wtf.account.companyAccountPref.budgetFreqType=frequencyType;
                
                
              
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),restext.msg],3);
                this.close();
            } else
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);

        },
        function(response){
            this.saveButton.enable();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });
    },
    
    BudgetActivationChanged:function(c,val){
        if(val){
            this.department.enable();
            this.deptandProd.enable();
            this.deptProdandCategory.enable();
            this.monthly.enable();
            this.bimonthly.enable();
            this.quarterly.enable();
            this.halfyearly.enable();
            this.yearly.enable();
            this.ignore.enable();
            this.warn.enable();
            this.block.enable();
        }else{
            this.department.disable();
            this.deptandProd.disable();
            this.deptProdandCategory.disable();
            this.monthly.disable();
            this.bimonthly.disable();
            this.quarterly.disable();
            this.halfyearly.disable();
            this.yearly.disable();
            this.ignore.disable();
            this.warn.disable();
            this.block.disable();
        }
    }
})