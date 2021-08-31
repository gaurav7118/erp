Wtf.account.WIPCPAccountSettingsWindow = function(config){
    
    this.cpAccountTypeValue = "";
    this.cpAccountPrefixValue = "";
    this.wipAccountPrefixValue = "";
    this.wipAccountTypeValue = "";
    
    
    
    Wtf.apply(this,{
        //        constrainHeader :true,		// 19991
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            scope: this,
            hidden:WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.savewipandcp),
            handler:this.saveForm
        }, {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:function(){
                this.close();
            }
        }]
    },config);
    
    Wtf.account.WIPCPAccountSettingsWindow.superclass.constructor.call(this,config);
    
}

Wtf.extend(Wtf.account.WIPCPAccountSettingsWindow,Wtf.Window,{
    onRender:function(config){
        Wtf.account.WIPCPAccountSettingsWindow.superclass.onRender.call(this,config);
        this.loadData();
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.title,'Set information for WIP/CP Accounts.',"../../images/accounting_image/Chart-of-Accounts.gif")
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.accForm

        });
    },
    
    createForm:function(){
        
        
        var groupRec=new Wtf.data.Record.create([
        {
            name: 'groupid'
        },

        {
            name: 'groupname'
        },

        {
            name: 'nature'
        },

        {
            name: 'mastergroupid'
        },

        {
            name: 'naturename'
        },

        {
            name: 'leaf',
            type:'boolean'
        },

        {
            name: 'level', 
            type:'int'
        }
        ]);
        
        
        var wipGroupStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },groupRec),
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getGroups.do",
            baseParams:{
                mode:1,
                ignorevendors:false,
                ignorecustomers:false,
                nature:[0,1]
            }
        });
        
        var cpGroupStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },groupRec),
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccount/getGroups.do",
            baseParams:{
                mode:1,
                ignorevendors:false,
                ignorecustomers:false,
                nature:[2,3]
            }
        });
        
        cpGroupStore.on('load',function(){
            this.wipText.setValue(this.wipAccountPrefixValue);
            this.cpText.setValue(this.cpAccountPrefixValue);
            this.wipAccGroup.setValue(this.wipAccountTypeValue);
            this.cpAccGroup.setValue(this.cpAccountTypeValue);
        },this);
        
        wipGroupStore.load();
        cpGroupStore.load();
        
        this.wipAccGroup=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.18")+'*',
            hiddenName:'groupid',
            name:'groupid',
            store:wipGroupStore,
            valueField:'groupid',
            displayField:'groupname',
            typeAhead: true,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectAccountType"),
            forceSelection: true,
            width:150,
            allowBlank: false,
            mode: 'local',
            disableKeyFilter:true,
            lastQuery : '',
            hirarchical:true,
            triggerAction:'all'
        //            addNewFn:this.showGroupWindow.createDelegate(this)
        });
        
        
        this.cpAccGroup=new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.18")+'*',
            hiddenName:'groupid',
            name:'groupid',
            store:cpGroupStore,
            valueField:'groupid',
            displayField:'groupname',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectAccountType"),
            typeAhead: true,
            forceSelection: true,
            width:150,
            allowBlank: false,
            mode: 'local',
            disableKeyFilter:true,
            lastQuery : '',
            hirarchical:true,
            triggerAction:'all'
        //            addNewFn:this.showGroupWindow.createDelegate(this)
        });
        
        
        this.wipText = new Wtf.form.ExtendedTextField({
            id:'wiptextfieldID',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.AccountPrefix"),
            name:'wipaccountname',            
            maxLength: 50,
            allowBlank:false,
            width : 150,
            emptyText :WtfGlobal.getLocaleText("acc.field.EnteraWIPAccountName")
        });
        
        this.cpText = new Wtf.form.ExtendedTextField({
            id:'cptextfieldID',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.AccountPrefix"),
            name:'cpaccountname',            
            maxLength: 50,
            allowBlank:false,
            width : 150,
            emptyText :WtfGlobal.getLocaleText("acc.field.EnteraCPAccountName")
        });
        
        this.accForm=new Wtf.form.FormPanel({
            region:'center',
            width:400,
            autoHeight:true,
            border:false,
            bodyStyle: "background:transparent; padding: 20px 10px 0px 10px",
            style: "background: transparent;padding-left:15px;",
            items:[{
                xtype:'fieldset',
                hidden:false,
                autoHeight:true,
                title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.wipaccount.setting.ttip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.wipaccount.setting")+"</span>",
                id:'wipaccountsettingsID',
                items:[this.wipText,this.wipAccGroup]
            },{
                xtype:'fieldset',
                hidden:false,
                autoHeight:true,
                title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.cpaccount.setting.ttip")+"'>"+WtfGlobal.getLocaleText("acc.accPref.cpaccount.setting")+"</span>",
                id:'cpaccountsettingsID',
                items:[this.cpText,this.cpAccGroup]
            }]
        })
    },
    
    saveForm:function(){
        if(!this.accForm.getForm().isValid()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.Pleasefillappropriatedata")],3);
            return;
        }
            
        var wipText = this.wipText.getValue();
        var cpText = this.cpText.getValue();
        var wipType = this.wipAccGroup.getValue();
        var cpType = this.cpAccGroup.getValue();
        
        
        Wtf.Ajax.requestEx({
            url : "ACCCompanyPref/saveWIPCPAccountsPreferences.do",
            params: {
                wipAccountPrefix:wipText,
                cpAccountPrefix:cpText,
                wipAccountType:wipType,
                cpAccountType:cpType
            }
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    
    genSuccessResponse:function(res,req){
        if(res.success){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.InformationSavedSuccessfully")],3);
            Wtf.account.isCPAndWIPAccountsSET = true;
            this.close();
        }
    },
    genFailureResponse:function(){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.field.Errorwhilesavingdata")],1);
        this.close();
    },
    
    loadData:function(){
        Wtf.Ajax.requestEx({
            url : "ACCCompanyPref/getWIPCPAccountsPreferences.do"
        },this,function(res,req){
            this.cpAccountTypeValue = res.data.cpAccountType;
            this.cpAccountPrefixValue = res.data.cpAccountPrefix;
            this.wipAccountPrefixValue = res.data.wipAccountPrefix;
            this.wipAccountTypeValue = res.data.wipAccountType;
        },function(res,req){
            
        });
    }
});