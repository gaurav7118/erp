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

/*
 *
 *Component for selecting custom layout for P&L and Balance Sheet Report.
 *
 **/

Wtf.account.selectCustomLayout = function (config){
    Wtf.apply(this,config);
    Wtf.account.selectCustomLayout.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.field.ViewReport"),//WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:this.viewCustomReport
        }, {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.account.selectCustomLayout,Wtf.Window,{
    layout:"border",
    modal:true,
    title:WtfGlobal.getLocaleText("acc.field.ViewCustomLayout"), 
    width:450,
    height:230,
    resizable:false,
    iconCls: "pwnd favwinIcon",
    initComponent:function (){
        Wtf.account.selectCustomLayout.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.AddEditForm);
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.field.ViewCustomLayoutReport"),WtfGlobal.getLocaleText("acc.field.ViewCustomLayoutReport")+".","../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
        });
    },
    GetAddEditForm:function (){        
    
        this.typeRec = new Wtf.data.Record.create([
        {
            name: 'id',
            type: 'string'
        },
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'status',
            type:'int'
        },{
            name: 'templatetitle',
            type: 'string'
        },
        {
            name: 'templateheading',// For India Country only.
            type: 'string'
        },
        {
            name: 'templatetype',
            type:'int'
        }
        ]);
        this.typeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.typeRec),
            url :'ACCAccount/getPnLTemplates.do',
            baseParams:{
                templatetype:this.templatetype
            }
        });
        this.typeStore.load();
        
        this.typeCombo= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.TemplateName*"),
            hiddenName:'id',
            name:'id',
            store:this.typeStore,
            valueField:'id',
            displayField:'name',
            disableOnField: "deleted",
            width:200,
            mode: 'local',
            lastQuery : '',
            //disableKeyFilter:true,
            triggerAction:'all',
            typeAhead: true,
            hirarchical:true,
            forceSelection:true
        });
        
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            defaults:{width:200,allowBlank:false},
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:35px",
            items:[                    
                    this.typeCombo
                ]
        });
    },
    viewCustomReport : function(){
        if(this.typeCombo.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.PleaseselectTemplateName")],2);
            return;
        }
        this.close();
        
        var rec = WtfGlobal.searchRecord(this.typeStore, this.typeCombo.getValue(), "id");        
        var id = this.typeCombo.getValue();
        var templateName = this.typeCombo.getRawValue();
        var templatetitle = rec.data['templatetitle'];
        var panel = Wtf.getCmp("customLayout"+id);            
        if(panel==null){
              panel = new Wtf.account.TradingCustomLayout({
                        id : "customLayout"+id,
                        consolidateFlag:false,
                        title:Wtf.util.Format.ellipsis(templatetitle!=''?templatetitle:templateName,Wtf.TAB_TITLE_LENGTH),
                        tabTip:templatetitle!=''?templatetitle:templateName,  //'Trading & Profit/Loss',
                        topTitle:'<center><font size=4>'+Wtf.util.Format.ellipsis(templatetitle!=''?templatetitle:templateName,Wtf.TAB_TITLE_LENGTH)+'</font></center>',                                
                        moduleid:101, //Added module id for Tading Profit and loss search report
                        searchJson: "",
                        filterConjuctionCrit:"",
                        templateid : id,
                        templatetype : this.templatetype,
                        templatetitle:templatetitle!=''?templatetitle:templateName,
                        statementType:this.templatetype==0?'TradingAndProfitLoss':'BalanceSheet',
                        border : false,
                        closable: true,
                        layout: 'fit',
                        iconCls:'accountingbase financialreport'
              });
            Wtf.getCmp('as').add(panel);
            panel.on('account',viewGroupDetailReport);
        }
        Wtf.getCmp('as').setActiveTab(panel);

        Wtf.getCmp('as').doLayout();
                
    }
});

/*
 *
 *Component for creating custom layout for P&L and Balance Sheet Report.
 *
 **/

Wtf.account.MappingComp = function (config){
    Wtf.apply(this,config);
    this.addEvents({
        "mappingsaved" : true
    });
    Wtf.account.MappingComp.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.field.SaveAndConfigure"),//WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:this.saveData,
            hidden:this.isCopy
        },{
            text:WtfGlobal.getLocaleText("acc.field.CopyTemplate"),//Copy Template
            scope:this,
            handler:this.copyData,
            hidden:!this.isCopy
        }, {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.account.MappingComp,Wtf.Window,{
    layout:"border",
    modal:true,
    title:WtfGlobal.getLocaleText("acc.field.CreateCustomLayout"), 
    width:450,
    height:320,
    resizable:false,
    iconCls: "pwnd favwinIcon",
    initComponent:function (){
        Wtf.account.MappingComp.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);
        this.add(this.AddEditForm);
    },
    GetNorthPanel:function (){
        var northHeader = this.title;
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(northHeader , northHeader+".","../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
        });
    },
    GetAddEditForm:function (){
        this.templatename = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.TemplateName*"),
            width:200,
            maxLength:50,
            disabled : (this.mode == "view"),
            scope:this,
            allowBlank:false,
            validator:Wtf.ValidateCustomItemName,
            value : (this.mode == "edit" || this.mode == "view")? this.templateName : ((this.isEdit)?this.selectedRec.data.name: ""),
            invalidText : 'This field should not be blank or should not contain /^[\w\s\'\"\-\/\;]+$/ characters.'
        });
        this.templatetitle = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.TemplateTitle")+"*",
            width:200,
            maxLength:100,
            allowBlank:false,
            disabled : (this.mode == "view"),
            scope:this,
            validator:Wtf.ValidateCustomItemName,
    //        allowBlank:false,
            value : (this.mode == "edit" || this.mode == "view")? this.templatetitle : ((this.isEdit)?this.selectedRec.data.templatetitle: ""),
           invalidText : 'This field should not be blank or should not contain /^[\w\s\'\"\<\>\=\-\/]+$/ characters.'
           });
        this.templateheading = new Wtf.form.TextField({
                fieldLabel:WtfGlobal.getLocaleText("acc.field.TemplateHeadings"),
                width:200,
                maxLength:250,
                disabled : (this.mode == "view"),
                hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                scope:this,
                value : (this.mode == "edit" || this.mode == "view")? this.templateheading : ""
            });
        this.typeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'name'
            },{
                name:'id'
            }],
            data:[['Profit & Loss',Wtf.templateType.pnl + ""],['Balance Sheet',Wtf.templateType.balanceSheet + ""], ['Trial Balance',Wtf.templateType.trialBalance + ""],['Cash Flow Statement',Wtf.templateType.cashFlow + ""]]
        });
        var defaulttemplate=Wtf.templateType.pnl + "";
        if(this.filterTemplate!=undefined){ 
            defaulttemplate=this.filterTemplate;
            for(var i=0;i<this.typeStore.getCount();i++){
                if(this.typeStore.getAt(i).data.id !=this.filterTemplate){
                    this.typeStore.remove(this.typeStore.getAt(i))
                    i--;
                }
            }
        }
        this.typeCombo= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            disabled : (this.mode == "edit" || this.mode == "view" || this.isCopy || this.isEdit),
            displayField:'name',
            store:this.typeStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.TemplateType*"),
            allowBlank:false,
            width:200,
            typeAhead: true,
            forceSelection: true,
//            value : (this.mode == "edit" || this.mode == "view")? this.templatetype : defaulttemplate
            value : (this.mode == "edit" || this.mode == "view")? this.templatetype :((this.isCopy || this.isEdit)?this.selectedRec.data.templatetype: defaulttemplate)

        });
        
        this.typeCombo.on("select",function(){
            if(this.typeCombo.getValue() == Wtf.templateType.trialBalance || this.typeCombo.getValue() == Wtf.templateType.cashFlow){
                WtfGlobal.hideFormElement(this.setAsDefault);
            }else{
                WtfGlobal.showFormElement(this.setAsDefault);
            }
        },this);
        
        this.setAsDefault = new Wtf.form.Checkbox({
            name:'setAsDefault',
            fieldLabel:"Set As Default", 
            checked:((this.isEdit) ? this.selectedRec.data.isdefault: false),
            hideLabel :(this.isEdit && (this.selectedRec.data.templatetype == Wtf.templateType.trialBalance || this.selectedRec.data.templatetype == Wtf.templateType.cashFlow)) ? true : false,
            hidden :(this.isEdit && (this.selectedRec.data.templatetype == Wtf.templateType.trialBalance || this.selectedRec.data.templatetype == Wtf.templateType.cashFlow)) ? true : false,
            width: 10
        });   
        
        this.countryCombo = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.203")+"*",
            allowBlank:!Wtf.companyAccountPref_isAdminSubdomain,
            hidden: !Wtf.companyAccountPref_isAdminSubdomain,
            hideLabel: !Wtf.companyAccountPref_isAdminSubdomain,
            store: Wtf.countryStore,
            displayField:'name',
            valueField:'id',
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            emptyText: WtfGlobal.getLocaleText("acc.rem.203"),
            forceSelection: !Wtf.companyAccountPref_isAdminSubdomain,
            disabled: this.isCopy
        });
        if(this.isCopy){
            this.countryCombo.setValue(this.selectedRec.data.countryid);
        }
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            defaults:{width:200},
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:35px",
            items:[
                    this.templatename,
                    this.templatetitle,
                    this.typeCombo,
                    this.setAsDefault,
                    this.templateheading,
                    this.countryCombo
                ]
        });
        var index = this.typeStore.find('id', this.typeCombo.getValue());
        var defaultheading="";
        if(index > -1){
            defaultheading = this.typeStore.getAt(index).data.name;
        }
        this.templateheading.setValue(defaultheading);
    },
    saveData : function(){
        if(!this.AddEditForm.form.isValid())
        {
            return;
        } 
       
        if(this.templatename.getValue().trim() == "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Templatenamefieldshouldnotbeblank")],2);
            return;
        }
        if(this.typeCombo.getValue() === "" || this.typeCombo.getValue() == undefined) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Templatetypefieldshouldnotbeblank")],2);
            return;
        }
        var validateDefault = true;
        if(this.replaceDefault){
            validateDefault = !this.replaceDefault;
            this.replaceDefault = false;
        }
        
        Wtf.Ajax.requestEx({
            url:"ACCAccountCMN/saveAccountMapPnL.do",
            params: {
                templateid : (this.mode== "edit" || this.mode== "view") ? this.templateid : ((this.isEdit) ? this.selectedRec.data.id : ""),
                templatename : this.templatename.getValue(),
                templatetitle : this.templatetitle.getValue(),
                templatetype : this.typeCombo.getValue(),
                templateheading : this.templateheading.getValue(),
                validateDefault : validateDefault,
                isDefault : this.setAsDefault.getValue(),
                countryid:this.countryCombo.getValue()
            }
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    copyData:function(){
        if (!this.AddEditForm.form.isValid()){
            return;
        }
        if (this.templatename.getValue().trim() == "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Templatenamefieldshouldnotbeblank")], 2);
            return;
        }
        if (this.typeCombo.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Templatetypefieldshouldnotbeblank")], 2);
            return;
        }
        var validateDefault = true;
        if(this.replaceDefault){
            validateDefault = !this.replaceDefault;
            this.replaceDefault = false;
        }
        Wtf.Ajax.requestEx({
            url: "ACCReports/copyCustomLayout.do",
            params: {
                copytemplateid: this.selectedRec.data.id,
                templatename: this.templatename.getValue(),
                templatetitle: this.templatetitle.getValue(),
                templatetype: this.typeCombo.getValue(),
                templateheading: this.templateheading.getValue(),
                validateDefault : validateDefault,
                isDefault : this.setAsDefault.getValue(),
                countryid: this.countryCombo.getValue()
            }
        }, this, function(response) {
            var res = response;
            if (res.success) {
                var templateid = response.templateid;
                var templatename = this.templatename.getValue();
                var templatetitle = this.templatetitle.getValue();
                var templateheading = this.templateheading.getValue();
                var templatetype = this.typeCombo.getValue();
                var isDefaultTemplate = this.setAsDefault.getValue();
                
                if(!isDefaultTemplate && this.selectedRec.data.isdefault){
                    // if reseting default template removing value from default template json
                    Wtf.CustomLayout.DefaultTemplates[templatetype] = null;
                }else if(isDefaultTemplate){
                    // Updating value for default template 
                    Wtf.CustomLayout.DefaultTemplates[templatetype] = {
                        id : templateid,
                        name : templatename,
                        templatetitle : templatetitle,
                        templateheadings : templateheading,
                        templatetype : templatetype,
                        isdefault : isDefaultTemplate
                    }
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.template.report.copied.success")], 3);
                this.fireEvent("copiedtemplate");
                this.close();
            }else if(response.defaultPresent) {
                response.msg = WtfGlobal.getLocaleText("acc.customLayout.DefaultPresent");
                Wtf.MessageBox.show({
                    title : "Alert",
                    msg : response.msg,
                    width : 520,
                    closable : false,
                    fn : function(btnText){
                        if(btnText){
                            if(btnText == "yes"){
                                this.replaceDefault = true;
                            }else{
                                this.setAsDefault.setValue(false);
                            } 
                            this.copyData();
                        }
                    }.createDelegate(this),
                    buttons: Wtf.MessageBox.YESNO,
                    animEl: 'mb9',
                    icon: Wtf.MessageBox.INFO
                });
        
            }else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), response.msg], 2);   
            }
        }, this.genFailureResponse);
    },
    genSuccessResponse:function(response){
        if(response.success){
            if(response.duplicate == 1) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.DuplicateTemplateName"), response.msg],3);
            } else if(response.defaultPresent == 1) {
                response.msg = WtfGlobal.getLocaleText("acc.customLayout.DefaultPresent");
                
                Wtf.MessageBox.show({
                    title : "Alert",
                    msg : response.msg,
                    width : 520,
                    closable : false,
                    fn : function(btnText){
                        if(btnText){
                            if(btnText == "yes"){
                                this.replaceDefault = true;
                            }else{
                                this.setAsDefault.setValue(false);
                            } 
                            this.saveData();
                        }
                    }.createDelegate(this),
                    buttons: Wtf.MessageBox.YESNO,
                    animEl: 'mb9',
                    icon: Wtf.MessageBox.INFO
                });
        
            } else {
                this.fireEvent("mappingsaved");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg],3);
                
                var templateid = response.templateid;
                var templatename = this.templatename.getValue();
                var templatetitle = this.templatetitle.getValue();
                var templateheading = this.templateheading.getValue();
                var templatetype = this.typeCombo.getValue();
                var countryid = this.countryCombo.getValue();
                var isDefaultTemplate = this.setAsDefault.getValue();
                
                this.close();
                
                if(!isDefaultTemplate && this.isEdit && this.selectedRec.data.isdefault){
                    // if reseting default template removing value from default template json
                    Wtf.CustomLayout.DefaultTemplates[templatetype] = null;
                }else if(isDefaultTemplate){
                    // Updating value for default template 
                    Wtf.CustomLayout.DefaultTemplates[templatetype] = {
                        id : templateid,
                        name : templatename,
                        templatetitle : templatetitle,
                        templateheadings : templateheading,
                        templatetype : templatetype,
                        isdefault : isDefaultTemplate
                    }
                }
                
                if(this.isEdit){
                   return; 
                }
                
                var window = new Wtf.account.MappingCompGrouping({
                    closable: true,
                    templateid : templateid,
                    mode : 'Add',
                    templateName : templatename,
                    templatetype : templatetype,
                    templatetitle : templatetitle,
                    countryid : countryid
                });
//                window.on("mappingsaved", this.loadStore, this);
                window.show();
            }
        }
    },

    genFailureResponse:function(response){    	
        var msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});

/*
 *
 * Component to sync default layout for P&L and Balance Sheet Report.
 *
 **/

Wtf.account.SyncDefaultLayoutComp = function (config){
    Wtf.apply(this,config);
    Wtf.account.SyncDefaultLayoutComp.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.custom.layout.synclayout"), //"Sync Layout"
            scope:this,
            handler:this.saveData
        },{
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.account.SyncDefaultLayoutComp,Wtf.Window,{
    layout:"border",
    modal:true,
    title:WtfGlobal.getLocaleText("acc.custom.layout.synclayout"), 
    width:450,
    height:250,
    resizable:false,
    iconCls: "pwnd favwinIcon",
    initComponent:function (){
        Wtf.account.SyncDefaultLayoutComp.superclass.initComponent.call(this);
        
        this.GetNorthPanel();
        this.GetSyncLayoutForm();
        
        this.add(this.northPanel);
        this.add(this.SyncLayoutForm);
    },
    GetNorthPanel:function (){
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(WtfGlobal.getLocaleText("acc.custom.layout.synclayout"),WtfGlobal.getLocaleText("acc.custom.layout.synclayout.html")+".","../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
        });
    },
    GetSyncLayoutForm:function (){
        this.subdomainRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.subdomainStore = new Wtf.data.Store({
            url : "kwlCommonTables/getSubdomainListFromCountry.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.subdomainRec),
            baseParams:{
                countryid:this.countryid
            }
        });
        this.subdomainStore.load();
        
        this.subdomainCombo = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Subdomain*") ,
            allowBlank:false,
            store: this.subdomainStore,
            displayField:'name',
            valueField:'id',
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            emptyText: WtfGlobal.getLocaleText("acc.custom.layout.subdomain.msg"),
            forceSelection: true,
            scope:this
        });
        
        this.SyncLayoutForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            defaults:{width:200},
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:35px",
            items:[
                this.subdomainCombo
            ]
        });
    },
    saveData : function(){
        if (!this.SyncLayoutForm.form.isValid()){
            return;
        }
        
        Wtf.Ajax.requestEx({
            url:"ACCAccountCMN/syncDefaultCustomLayout.do",
            params: {
                subdomainlist : this.subdomainCombo.getValue(),
                countryid: this.countryid,
                synctemplateid: this.synctemplateid
            }
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    genSuccessResponse:function(response){
        if(response.success){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg],3);
            this.close();
        }
    },
    genFailureResponse:function(response){    	
        var msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});

/*
 *
 *Component used for configure custom layout for P&L and Balance Sheet
 *
 **/


Wtf.account.MappingCompGrouping = function(config) {
    
    Wtf.apply(this, config);
    this.selectedGroupId = "";
    
//    this.addEvents({
//        "mappingsaved" : true
//    });
    
    /*Group grid*/
    this.groupgridrec = new Wtf.data.Record.create([
    {
        name: 'groupid'
    },
    {
        name: 'sequence'
    },
    {
        name: 'groupname'
    },
    {
        name: 'nature'
    },
    {
        name: 'parentid'
    },
    {
        name: 'parentname'
    },
    {
        name: 'showtotal'
    },
    {
        name: 'showchild'
    },
    {
        name: 'showchildacc'
    },{
        name: 'excludeChildBalances'
    },{
        name: 'addBlankRowBefore'
    }
    ]);
    this.groupgridstore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.groupgridrec),
        url:"ACCAccount/getLayoutGroups.do",
        baseParams:{
            mode:2,
            //nature:[1, 2],
            nondeleted:true,
            templateid:this.templateid
        } 
    });
    
    this.parentGroupStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.groupgridrec),
        url:"ACCAccount/getLayoutGroups.do",
        baseParams:{
            mode:2,
            //nature:[1, 2],
            nondeleted:true,
            templateid:this.templateid
        } 
    });
    
    this.groupgridstore.on('load', function(store, rec, o){
        var recs = this.groupgridstore.getRange();
        /*
         *Map to maintain child count of group
         *key :- groupid
         *value :- number of childs(count)
         */
        this.groupChildCountMap = {};
        
        /*
         *Map to maintain information of parent child group.
         *key :- groupid
         *value :- parentid
         */
        this.childParentMap = {};
        for(var i = 0 ; i < recs.length ; i++){
            var groupRecData = recs[i].data;
            if(groupRecData.parentid != ""){
                var count = 1;
                if(this.groupChildCountMap.hasOwnProperty(groupRecData.parentid)){
                    /*
                     *If parent already have some child groups then update child count of that parent group.
                     */
                    count += this.groupChildCountMap[groupRecData.parentid];
                }
                var parent = groupRecData.parentid;
                /*
                 *If parent of group also have some parents then update child count of those parents.
                 **/
                while (this.childParentMap.hasOwnProperty(parent)) {
                    this.groupChildCountMap[this.childParentMap[parent]] += 1;
                    parent = this.childParentMap[parent];
                }
                
                this.childParentMap[groupRecData.groupid] = groupRecData.parentid;
                this.groupChildCountMap[groupRecData.parentid] = count;
            }
            
            /*
             Disable Opening stock,closing stock ,diff in opening balance,net pnl from type combo if already used.
              */
            if(groupRecData.nature == 6 || groupRecData.nature == 7 || groupRecData.nature == 8 || groupRecData.nature == 9){
                this.enableDisableGroup(groupRecData,true);
            }
        }
        this.typeCombo.store.loadData(this.typeDataArr);
        Wtf.Ajax.requestEx({
            url:"ACCAccount/getNextCustomLayoutSequence.do",
            params: {
                templateid : this.templateid
            }
        },this,function(response){
            this.nextSequence = response.nextSequence;
            if(response.nextSequence){
                this.sequence.setValue(response.nextSequence);
                this.loadParentGroupStore(rec);
            } else {
                this.sequence.setValue("");
            }
        },function(response){
            this.sequence.setValue("");
        });

    }, this);
        
    var groupgridCm = new Wtf.grid.ColumnModel([{            
        header: WtfGlobal.getLocaleText("acc.field.Sequence"),
        width : 30,
        align : 'center',
        dataIndex: 'sequence',
        renderer : function(value, metadata, record){
            if(record.data.nature == 5){
                return '<b>'+value+'</b>';
            } else {
                return value;
            }
            
        }
    },{
        header: WtfGlobal.getLocaleText("acc.coa.gridType"),
        dataIndex: 'groupname',
        renderer : function(value, metadata, record){
            if(record.data.nature == 5){
                return '<b>'+"<div wtf:qtip=\""+value+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.coa.gridName")+"'>"+value+"</div>"+'</b>';
            } else {
                return "<div wtf:qtip=\""+value+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.coa.gridName")+"'>"+value+"</div>";
            }
        }
    }, {
        header : WtfGlobal.getLocaleText("acc.invoice.gridAction"),
        dataIndex: '',
        width : 30,
        renderer : function(){
            return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
        }
    }]);

    this.gridGroupSM = new Wtf.grid.RowSelectionModel({
        singleSelect:true
    });
    
    this.groupGrid = new Wtf.grid.GridPanel({
        sm:this.gridGroupSM,
        layout : 'fit',
        store: this.groupgridstore,
        cm: groupgridCm,
        border : false,
        loadMask : true,
        viewConfig : {
            forceFit : true
        }
    });
    
    this.groupGrid.on("rowclick", this.handleRowClick, this);
    
    
    /*Group grid end*/
    
    /*------------Left side mapping start--------------*/
    
    this.columnRecAsset = new Wtf.data.Record.create([
    {
        name: 'accid'
    },
    {
        name: 'accname'
    },
    {
        name: 'acccode'
    },
    {
        name: 'naturename'
    },
    {
        name: 'groupname'
    }
    ]);
    
    this.columnDsAsset = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.columnRecAsset),
        url: Wtf.companyAccountPref_isAdminSubdomain ? "ACCAccount/getDefaultAccount.do" : "ACCAccountCMN/getAccountsForCombo.do",
        baseParams:{
            mode:2,
            country:this.countryid,
//            ignoreGLAccounts:true,
//            ignoreCashAccounts:true,
//            ignoreBankAccounts:true,
//            ignoreGSTAccounts:true,
            ignorecustomers:true,  
            ignorevendors:true,
            //nature:[1, 2],
            nondeleted:true,
            headerAdded:true,
            ignoreTransactionFlag:false,
            controlAccounts:true
        },
        sortInfo : {
            field : 'accname',
            direction : 'ASC'
        },
        groupField : 'groupname'        
    });    
        
    var csvHeaderCm = new Wtf.grid.ColumnModel([{
        header: WtfGlobal.getLocaleText("acc.invoice.gridaccount"),
        dataIndex: 'accname'
    },{
        header: WtfGlobal.getLocaleText("acc.coa.accCode"),
        dataIndex: 'acccode'
    },{
        header: WtfGlobal.getLocaleText("acc.setupWizard.accType"),
        dataIndex: 'groupname'
    }]);

    this.summary = new Wtf.grid.GroupSummary({});
    this.headerGridAsset= new Wtf.grid.GridPanel({
        ddGroup:"mapHeader",
        enableDragDrop : (this.mode != "view")? true : false,
        sm:new Wtf.grid.RowSelectionModel({
            singleSelect:true
        }),
        height:200,
        layout : 'fit',
        store: this.columnDsAsset,
        cm: csvHeaderCm,
//        border : false,
        loadMask : true,
        //        view:new Wtf.grid.GridView({
        //            forceFit:true
        //        //                emptyText:emptyGridText
        //        }),
        view : new Wtf.grid.GroupingView({
            forceFit : true,
            showGroupName : true,
            enableGroupingMenu : true,
            hideGroupedColumn : true
        }),
        plugins:[this.summary]
    });


    //Mapped CSV Header Grid
    this.mappedAssetheaders="";
    
    this.mappedAssetHeaderDs = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.columnRecAsset),
        url:"ACCAccount/getAccountsForLayoutGroup.do",
        baseParams:{
            templateid : this.templateid,
            isincome : 0
        },
        sortInfo : {
            field : 'accname',
            direction : 'ASC'
        },
        groupField : 'groupname'
    });
    
    var mappedAssetHeaderCm = new Wtf.grid.ColumnModel([{
        header: WtfGlobal.getLocaleText("acc.field.MappedAccount"),  //"Mapped Headers",
        dataIndex: 'accname'
    },{
        header: WtfGlobal.getLocaleText("acc.coa.accCode"),
        dataIndex: 'acccode'
    },{
        header: WtfGlobal.getLocaleText("acc.setupWizard.accType"),
        dataIndex: 'groupname'
    }]);
    this.mappedAssetHeaderGrid= new Wtf.grid.GridPanel({
        ddGroup:"restoreHeader",
        enableDragDrop : (this.mode != "view")? true : false,
        sm:new Wtf.grid.RowSelectionModel({
            singleSelect:true
        }),
        store: this.mappedAssetHeaderDs,
        cm: mappedAssetHeaderCm,
        height:200,
        layout : 'fit',
//        border : false,
        loadMask : true,
        //        view:new Wtf.grid.GridView({
        //            forceFit:true,
        //            emptyText:"Drag and Drop Expense/Asset Account here"
        //        })
        view : new Wtf.grid.GroupingView({
            forceFit : true,
            showGroupName : true,
            enableGroupingMenu : true,
            hideGroupedColumn : true
        }),
        plugins:[new Wtf.grid.GroupSummary({})]
    });
    
    this.mappedAssetHeaderDs.on("load", function(){
        this.filterMainStore(this.columnDsAsset, this.mappedAssetHeaderDs);
    }, this);
        
//    this.columnDsAsset.load();   

    /*------------Left side mapping End----------*/
    
    
    
    
    this.tempaltename = new Wtf.form.TextField({
        fieldLabel:WtfGlobal.getLocaleText("acc.field.GroupText*"),
        width:200,
        maxLength:250,
        disabled : (this.mode == "view"),
        scope:this,
        allowBlank:false,
        //        value : "ABC"
        value : (this.mode == "edit" || this.mode == "view")? this.templateName : ""
    });
    
    this.sequence = new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.field.Sequence*"),
        width:200,
        maxLength:50,
        disabled : (this.mode == "view"),
        scope:this,
        allowBlank:false,
        allowNegative: false
    });
    
    //    this.tempaltename.setValue(this.templateName);
//    var dataArr = this.templatetype=='0'?[['Income',3],['Expense',2],['Define Total',5],['Closing Stock Total',7]]:
//            (this.templatetype=='1' ? [['Asset',1],['Liability',0],['Define Total',5],['Opening Stock',6],['Closing Stock Total',7],
//        ['Diff In Opening Balance',8]] :(this.templatetype == '3'?[['All Accounts', 10],['Define Total',5]]: [['Asset',1],['Liability',0],['Expense',2],['Income',3],['Define Total',5],['Diff In Opening Balance',8],['Opening Stock',6],['Closing Stock Total',7]]));
    
    this.typeDataArr = [];
    if (this.templatetype == '0') { // Profit & Loss
        this.typeDataArr = [{name:'Income', id:3}, {name:'Expense',id: 2}, {name:'Define Total', id:5},{name:'Opening Stock', id:6}, {name:'Closing Stock Total', id:7}];
    } else if (this.templatetype == '1') { // Balance Sheet
        this.typeDataArr = [{name:'Asset', id:1}, {name:'Liability', id:0}, {name:'Define Total', id:5}, {name:'Closing Stock Total', id:7},
            {name:'Diff In Opening Balance', id:8}, {name:WtfGlobal.getLocaleText("acc.report.32"), id:9}];
    } else if (this.templatetype == '3') { // Cash Flow Statement
        this.typeDataArr = [{name:'Liability', id:0}, {name:'Asset', id:1}, {name:'Expense', id:2}, {name:'Income', id:3}, {name:'Define Total', id:5}];
    } else {
        this.typeDataArr = [{name:'Asset', id:1}, {name:'Liability', id:0}, {name:'Expense', id:2}, {name:'Income', id:3}, {name:'Define Total', id:5}, {name:'Diff In Opening Balance', id:8},{name:'Closing Stock Total', id:7}];
    }
    
    this.typeStore = new Wtf.data.JsonStore({
        fields:[{
            name:'name'
        },{
            name:'id'
        },{
            name:'isdeactive',type :"boolean"
        }],
        data:this.typeDataArr
    });
    
    this.typeCombo= new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        valueField:'id',
        disabled : (this.mode == "view"),
        displayField:'name',
        store:this.typeStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.coa.gridAccountType")+"*",
        allowBlank:false,
        width:200,
        typeAhead: true,
        forceSelection: true,
        value : (this.templatetype=='0')? 3 : (this.templatetype=='3'?0:1),
        tpl: new Wtf.XTemplate(

                '<tpl for=".">',

                '<div wtf:qtip = "{[values.isdeactive ? "'+WtfGlobal.getLocaleText("acc.common.cannotselectvalue")+'" : values.name]}" class="{[values.isdeactive == true ? "x-combo-list-item disabled-record" : "x-combo-list-item"]}">',

                '<div>{name}</div>',

                '</div>',

                '</tpl>'
                )
    });
    
    this.typeCombo.on("beforeselect",function(combo,rec,index){
        //If deactivated then dont allow to select
        if(rec.data.isdeactive){
            return false;
        }else{
            return true;
        }
    },this);
    
    this.groupgridstore.load();
    
    this.typeCombo.on("render",function(){
         this.columnDsAsset.load({
                params:{
                    nature:this.typeCombo.getValue(),
                    templateid : this.templateid
                }
            });
    },this);
    
    this.sequence.on('change', function(){
//        this.filterParentGroupStore(this.typeCombo.getValue(), this.sequence.getValue());
        if(this.sequence.getValue() > this.nextSequence ){
            this.sequence.setValue(this.nextSequence);
        }
    }, this);
    
    this.cmbParent= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.rem.43"),
        hiddenName:'parentid',
        name:'parentid',
        store:this.parentGroupStore,
        valueField:'groupid',
        displayField:'groupname',
        disableOnField: "deleted",
        width:200,
        mode: 'local',
        lastQuery : '',
        //disableKeyFilter:true,
        triggerAction:'all',
        typeAhead: true,
        hirarchical:true,
        forceSelection:true
    });
    
    this.groupChildCountMap = {};
    this.childParentMap = {};
    this.cmbParent.on('select',function(combo, rec, index){
        // If parent selected then set sequence after parent group
        var count = 1;    
        if(this.groupChildCountMap.hasOwnProperty(rec.data.groupid)){
            /*
             *If parent already have some child groups then update child count of that parent group.
             */
            count +=this.groupChildCountMap[rec.data.groupid];
        }
        this.sequence.setValue(rec.data.sequence + count);
        this.sequence.parentSequence = rec.data.sequence + count;
//        this.sequence.disable();
    },this);
    
    this.subGroup=new Wtf.form.FieldSet({
        title: WtfGlobal.getLocaleText("acc.field.Isasubgroup"),
        checkboxToggle: true,
        autoHeight: true,
        width:330,
     //   hidden:this.isFixedAsset,
        border:false,
        checkboxName: 'subgroup',
        style: 'margin-left:-10px',
        collapsed: true,
        items:[this.cmbParent]
    });
    
    this.subGroup.on("collapse",function(){
        if(this.sequence != undefined){
            // reset sequence if unchecked sub group check box
//            this.sequence.enable();
            if(this.selectedGroupId == undefined || this.selectedGroupId == ""){
                this.sequence.setValue(this.nextSequence);
            }
        }
    },this);
    this.subGroup.on("expand",function(){
        if(this.cmbParent.getValue() != undefined && this.cmbParent.getValue() !=""){
            // reset sequence if unchecked sub group check box
//            this.sequence.disable();
            if((this.selectedGroupId == undefined || this.selectedGroupId == "") && this.sequence.parentSequence!= undefined && this.sequence.parentSequence != ""){
                this.sequence.setValue(this.sequence.parentSequence);
            }
        }
    },this);
    
    this.showTotal= new Wtf.form.Checkbox({
        name:'showtotal',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowTotal"),
        checked:true,
        cls : 'custcheckbox',
        width: 10
    });

    this.showChild= new Wtf.form.Checkbox({
        name:'showchild',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowChild"),
        checked:true,
        cls : 'custcheckbox',
        width: 10
    });
    
    this.showChildAcc= new Wtf.form.Checkbox({
        name:'showchildacc',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ShowChildAccounts"),
        checked:true,
        cls : 'custcheckbox',
        width: 10
    });
    
    this.excludeChildBalances= new Wtf.form.Checkbox({
        name:'excludeChildBalances',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.excludeChildBalances"),
        checked:true,
        cls : 'custcheckbox',
        width: 10
    });
     this.addBlankRowBefore = new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.field.addblankrow"),
        name:'addBlankRowBefore',
        width:200,
        maxLength:50,
        disabled : (this.mode == "view"),
        scope:this,
        allowBlank:false,
        maxValue:10,
        value:0
    });
    
//    this.cmbParent.on('select',this.checkGroup,this);
//    this.subAccount.on('collapse',this.enableDisableCombo.createDelegate(this,[false]),this);
//    this.subAccount.on('expand',this.enableDisableCombo.createDelegate(this,[false]),this);        
        
    this.mappingGridComp = new Wtf.Panel({
        layout : 'column',
        border:false,
        id : 'centerregion',
        bodyStyle : 'background-color:#f1f1f1;padding:2px;',
        items : [{
            columnWidth:.48,
                
            border:false,
            layout:"fit",
            autoScroll:true,
            items:this.headerGridAsset
        },{
            columnWidth:.48,
            border:false,
            layout:"fit",
            autoScroll:true,
            items:this.mappedAssetHeaderGrid
        }]
    });

    /*Component for Rule mapping*/
    this.rulesm = new Wtf.grid.CheckboxSelectionModel();
    this.mastercm = new Wtf.grid.ColumnModel([ this.rulesm,
    {
        header: WtfGlobal.getLocaleText("acc.coa.gridType"),
        dataIndex: 'groupname'
    }]);

    this.masterReader = new Wtf.data.Record.create([
    {
        name: 'groupid'
    },

    {
        name: 'groupname'
    },

    {
        name: 'sequence'
    }
    ]);
    

    this.masterds = new Wtf.data.Store({
        url:"ACCAccount/getLayoutGroupsFortotalgroupmap.do",
        baseParams:{
            templateid:this.templateid
        },
        reader: new Wtf.data.KwlJsonReader({
            root:"data"
        }, this.masterReader)
    });
    
    
    
    this.rulesm1 = new Wtf.grid.RowSelectionModel();
    this.mastercm1 = new Wtf.grid.ColumnModel([ 
    {
        header: WtfGlobal.getLocaleText("acc.coa.gridType"),
        dataIndex: 'groupname'
    }]);

    this.masterReader1 = new Wtf.data.Record.create([
    {
        name: 'groupid'
    },
    {
        name: 'groupname'
    },
    {
        name: 'sequence'
    },
    {
        name: 'ruletype' //1 : Plus , 2 : Minus
    }
    ]);
    



    this.masterds1 = new Wtf.data.Store({
        id:'masterstore',
        url:"ACCAccount/getMappedLayoutGroupsforgrouptotal.do",
        reader: new Wtf.data.KwlJsonReader({
            root:"data"
        }, this.masterReader1)
    });

    this.masterds.on("load", this.afterAvailableAccountStoreLoad, this);

    this.movetoright = document.createElement('img');
    this.movetoright.src = "images/arrowright.gif";
    this.movetoright.style.width = "24px";
    this.movetoright.style.height = "24px";
    this.movetoright.style.margin = "5px 0px 5px 0px";
    this.movetoright.onclick = this.movetorightclicked.createDelegate(this,[]);
    this.movetoleft = document.createElement('img');
    this.movetoleft.src = "images/arrowleft.gif";
    this.movetoleft.style.width = "24px";
    this.movetoleft.style.height = "24px";
    this.movetoleft.style.margin = "5px 0px 5px 0px";
    //this.movetoleft.onclick = this.movetoleftclicked.createDelegate(this,[]);

    this.centerdiv = document.createElement("div");
    this.centerdiv.appendChild(this.movetoright);
    //this.centerdiv.appendChild(this.movetoleft);
    this.centerdiv.style.paddingTop = "135px";
    this.centerdiv.style.textAlign = "center";
    
    this.masterGrid = new Wtf.grid.GridPanel({
        id: 'mastergrid',
        store: this.masterds,
        cm: this.mastercm,
        sm:this.rulesm,
        border: true,
        clicksToEdit: 1,
        viewConfig: {
            forceFit: true
        }
    })
    
    this.masterGrid1 = new Wtf.grid.GridPanel({
        id: 'mastergrid1',
        store: this.masterds1,
        cm: this.mastercm1,
        sm:this.rulesm1,
        stripeRows : true,
        border: true,
        clicksToEdit: 1,
        viewConfig: {
            forceFit: true
        }
    });
    
    this.masterGrid1.on('rowclick', this.toggleplusminus, this);
    
    this.mappingRuleComp = new Wtf.Panel({
        layout : 'border',
        hidden : true,
        bodyStyle : 'background-color:#f1f1f1;padding:5px;',
        tbar : [new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),
        scope: this,
        iconCls:'accountingbase fetch',
        handler : this.resetLayoutGroups
        })
                
        ],
        items : [{
            region : 'west',
            border : false,
            width : 250,
            layout : 'fit',
            items :this.masterGrid
        },{
            region : 'center',
            border : false,
            contentEl : this.centerdiv
        },{
            region : 'east',
            border : false,
            width : 250,
            layout : 'fit',
            items :this.masterGrid1
        }]
    });
    
    this.typeCombo.on("select", function(combo, record, index){
        this.filterParentGroupStore(record.data.id, this.sequence.getValue());
        if(record.data.id==5) {//Defined Total            
            this.masterds.load({
                params:{
                    sequence:this.sequence.getValue()
                }
            });
        }else if(record.data.id==0 || record.data.id==2){
            this.columnDsAsset.on("load",function(){
                this.columnDsAsset.each(function(record){
                    if(record.data.accname=="Closing Stock"){
                        this.columnDsAsset.remove(record);
                    }
                },this);
            },this);
            this.columnDsAsset.load({
                params:{
                    nature:record.data.id,
                    templateid : this.templateid
                }
            });
        }else{
            this.columnDsAsset.load({
                params:{
                    nature:record.data.id,
                    templateid : this.templateid
                }
            });
        }
        this.hideShowFields(record.data.id)
    }, this);
    
    /*Component for rule mapping end*/
            
    Wtf.account.MappingCompGrouping.superclass.constructor.call(this, {
        height : 700,
        width : 950,
        title: WtfGlobal.getLocaleText("acc.field.ConfigureCustomLayout"),
        modal : true,
        layout : 'border',
        buttons : [{
            text : WtfGlobal.getLocaleText("acc.field.CreateNewGroup"),
            scope : this,
            handler : this.resetFields
        },{
            text : WtfGlobal.getLocaleText("acc.common.saveBtn"),
            scope : this,
            hidden : (this.mode == "view"),
            handler : this.saveData
        }, {
            text : WtfGlobal.getLocaleText("acc.common.close"),
            scope : this,
            handler : function() {
                this.close();
            }
        }],
        items :[{
            region : 'north',
            border:false,
            height:70,
            bodyStyle : 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(WtfGlobal.getLocaleText("acc.field.ConfigureCustomLayout"),WtfGlobal.getLocaleText("acc.field.ConfigureCustomLayout")+".","../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
        },{
            region : 'west',
            border:false,
            split : true,
            width:300,
            bodyStyle : 'background-color:#f1f1f1;padding:15px 5px 5px 5px;',
            layout : 'fit',
            items : [this.groupGrid]
            //html : 'West Region'
        },{
            region : 'center',
            layout : 'border',
            bodyStyle : 'background-color:#f1f1f1;padding-left:5px;',
            border : false,
            items : [{
                region : 'north',
                border:false,
                height:300,
                bodyStyle : 'background-color:#f1f1f1;padding:15px 5px 5px 20px;',
                layout : 'form',
                labelWidth: 200,
                items : [
                    this.sequence,
                    this.tempaltename, 
                    this.typeCombo,
                    this.subGroup,
                    this.showTotal,
                    this.showChild,
                    this.showChildAcc,
                    this.excludeChildBalances,
                    this.addBlankRowBefore,
                ]
            }, {
                region : 'center',
//                height : 400,
                id :'centerReg',
                bodyStyle : 'background-color:#f1f1f1',
                layout : 'fit',
                autoScroll:true,
                border:false,
                items : [this.mappingRuleComp, this.mappingGridComp]
            }]
        } ]
    });

    this.mappingGridComp.on("afterlayout",function(){
        
        function rowsDiff(store1,store2){
            return diff=store1.getCount()-store2.getCount();
        }

        function unMapRecAsset(atIndex){
            var headerRec = mappedHeaderStore.getAt(atIndex);
            if(headerRec!==undefined){
                var headerAccCount = headerStore.data.items.length;
                for(var i=0; i<headerAccCount; i++){
                    var rec = headerStore.data.items[i];
                    var mapAccID = headerRec.data.accid;
                    if(rec.data.accid==mapAccID){
                        headerStore.remove(rec);
                        break;
                    }
                }
                mappedHeaderStore.remove(headerRec);

            }
            headerStore.add(headerRec);
            headerStore.sort("accname","ASC");  
            headerStore.sort("isMandatory","DESC");
        }


        headerStore = this.columnDsAsset;
        headerGrid = this.headerGridAsset;

        mappedHeaderStore = this.mappedAssetHeaderDs;
        mappedHeaderGrid = this.mappedAssetHeaderGrid;

        // Drag n drop [ Headers -> Mapped Headers ]
        DropTargetEl =  mappedHeaderGrid.getView().el.dom.childNodes[0].childNodes[1];
        DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
            ddGroup    : 'mapHeader',
            notifyDrop : function(ddSource, e, data){
                function mapHeader(record, index, allItems) {
                    var isExist = false;
                    var headerAccID = record.data.accid;
                    var mapStoreCount = mappedHeaderStore.data.items.length;
                    for(var i=0; i<mapStoreCount; i++){
                        var mappedAccID = mappedHeaderStore.data.items[i].data.accid;
                        if(headerAccID == mappedAccID){
                            isExist = true;
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.already.map")],2);
                            break;
                        }
                    }
                    if(!isExist){
                            var newHeaderRecord = new Wtf.data.Record(record.data);
                            mappedHeaderStore.add(newHeaderRecord);
                            ddSource.grid.store.remove(record);
                        }
                }
                Wtf.each(ddSource.dragData.selections ,mapHeader);
                return(true);
            }
        });

        // Drag n drop [ Mapped Headers -> Headers ]
        DropTargetEl =  headerGrid.getView().el.dom.childNodes[0].childNodes[1];
        DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
            ddGroup    : 'restoreHeader',
            notifyDrop : function(ddSource, e, data){
                function restoreColumn(record, index, allItems) {
                    unMapRecAsset(ddSource.grid.store.indexOf(record));
                }
                Wtf.each(ddSource.dragData.selections ,restoreColumn);
                return(true);
            }
        });


    },this);
}

Wtf.extend(Wtf.account.MappingCompGrouping, Wtf.Window,{
//    enableDisableCombo:function(disabled){        
//        if(this.isEdit==true){     
//            if(this.record!=null && !this.record.data.isHeaderAccount && !this.isotherexpense && !this.isFixedAsset && !this.issales && !this.ispurchase){
//                this.accGroup.setDisabled(disabled);
//            }               
//        }else{
//            if(!this.isotherexpense && !this.isFixedAsset && !this.issales && !this.ispurchase){			// issue 20495 fixed
//                this.accGroup.setDisabled(disabled);
//            }
//        }                    
//        this.cmbParent.setValue("");
//        this.checkGroup();        
//    },
    enableDisableGroup : function(recdata , isdeactive){
        for(var index = 0 ; index < this.typeDataArr.length ; index++){
            if(this.typeDataArr[index].id == recdata.nature){
                this.typeDataArr[index].isdeactive = isdeactive ;
            }
        }
    },
    afterAvailableAccountStoreLoad:function(){
        this.masterds1.load({
            params : {
                totalgroupid : this.selectedGroupId
            }
        });
    },
    filterParentGroupStore: function(nature, sequence) {
        this.parentGroupStore.clearFilter(true);
        this.cmbParent.setValue(undefined);
        this.parentGroupStore.filterBy(function(comborec){
            if(nature === 5 && comborec.data.nature != 5 && comborec.data.nature != 6 && comborec.data.nature != 7 && comborec.data.nature != 8 && comborec.data.nature != 9 && comborec.data.sequence < sequence) {//If defined total group selected from group type dropdown then populate all groups except defined total
                return true;
                /*
                 *Don't allow Define Total, Opening stock, Closing Stock, Diff in opening balance ,
                 *Net Profit/Loss in parent drop down when same nature is selected in group drop down.
                 *
                 *For e.g. If I have select Opening stock group from group type combo then don't allow Opening Stock in Parent Group drop down.
                 */
            }else if(comborec.data.nature != 5 && comborec.data.nature != 6 && comborec.data.nature != 7 && comborec.data.nature != 8 && comborec.data.nature != 9 && comborec.data.nature === nature && comborec.data.sequence < sequence){//If group selected from group type dropdown is other than defined total then populate all groups of selected nature.
                return true;
                 /*
                 *Don't allow Define Total, Opening stock, Closing Stock, Diff in opening balance ,
                 *Net Profit/Loss in parent drop down.
                 *
                 **For e.g. If I have select Asset group from group type combo then don't allow Closing Stock,Diff in opening balance,Net Profit/Loss in Parent Group drop down.
                 */
            }else if((nature == 6 || nature == 7 || nature == 8 || nature == 9) && (comborec.data.nature != 3 && comborec.data.nature != 5 && comborec.data.nature != 6 && comborec.data.nature != 7 && comborec.data.nature != 8 && comborec.data.nature != 9)){//If group selected from group type dropdown is other than defined total then populate all groups of selected nature.
                return true;
            }else{
                return false;
            }
        });
    },
    
    loadParentGroupStore: function(records) {
        this.parentGroupStore.removeAll();
        this.parentGroupStore.add(records);
        this.filterParentGroupStore(this.typeCombo.getValue(), this.sequence.getValue());
    },
    
    hideShowFields : function(nature){
        if(nature == 6 || nature == 7 || nature == 8 || nature == 9) { //,['Opening Stock',6],['Closing Stock Total',7],['Diff In Opening Balance',8],['Net Profit/Loss',9]
            this.mappingGridComp.hide();
            this.mappingRuleComp.hide();
//            this.subGroup.hide();
            WtfGlobal.hideFormElement(this.showTotal);
            WtfGlobal.hideFormElement(this.showChild);
            WtfGlobal.hideFormElement(this.showChildAcc);
            WtfGlobal.hideFormElement(this.excludeChildBalances);
        } else if(nature == 5) {//Defined Total
            this.mappingRuleComp.show();
            this.mappingGridComp.hide();
            this.subGroup.show();
            WtfGlobal.hideFormElement(this.showTotal);
            WtfGlobal.hideFormElement(this.showChild);
            WtfGlobal.hideFormElement(this.showChildAcc);
            WtfGlobal.hideFormElement(this.excludeChildBalances);
            this.mappingRuleComp.doLayout();
        } else {
            this.subGroup.show();
            WtfGlobal.showFormElement(this.showTotal);
            WtfGlobal.showFormElement(this.showChild);
            WtfGlobal.showFormElement(this.showChildAcc);
            WtfGlobal.showFormElement(this.excludeChildBalances);
            this.mappingGridComp.show();
            this.mappingRuleComp.hide();
//            this.mappingGridComp.doLayout();  
        }
        
    },
    
    saveData : function(){
//        if(!this.AddEditForm.form.isValid())
//        {
//            return;
//        } 
        
        this.typeStore.loadData(this.typeDataArr);
        if(this.sequence.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Sequencefieldshouldnotbeblank")],2);
            return;
        }
        if(this.tempaltename.getValue().trim() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.GroupTextfieldshouldnotbeblank")],2);
            return;
        }
        if(this.typeCombo.getValue() === "") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.GroupTypefieldshouldnotbeblank")],2);
            return;
        }
              
        var accountsArray = new Array();
        
        if(this.typeCombo.getValue()==5) {
            if(this.masterds1.getCount() == 0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.NoGrouphasbeenselected")],2);
                return;
            }
            for(var i=0; i < this.masterds1.getCount(); i++ ) {            
                if(this.masterds1.getAt(i).data.ruletype != 1 && this.masterds1.getAt(i).data.ruletype != 2 ){
                    if(i==0) {
                        accountsArray.push(this.masterds1.getAt(i).data.groupid + "_NULL");
                    } else {
                        accountsArray.push(this.masterds1.getAt(i).data.groupid + "_" + ((this.masterds1.getAt((i-1)).data.ruletype==1)? "PLUS" : "MINUS"));
                    }
                }
            }
        } else {
            for(var i=0; i < this.mappedAssetHeaderDs.getCount(); i++ ){
                accountsArray.push(this.mappedAssetHeaderDs.getAt(i).data.accid);
            }
        }
        
        var oldSequence;
        if(this.gridGroupSM.getSelected()){
            var recdata = this.gridGroupSM.getSelected().data;
            oldSequence = recdata.sequence;
        }
    
        Wtf.Ajax.requestEx({
            url:"ACCAccountCMN/saveLayoutGroup.do",
            params: {
                groupname : this.tempaltename.getValue(),
                nature : this.typeCombo.getValue(),
                sequence : this.sequence.getValue(),
                oldSequence : oldSequence,
                accountsArray : accountsArray,
                accountsArrayFlag : accountsArray.length>0?true:false,
                groupid : this.selectedGroupId,
                templateid : this.templateid,
                subgroup:(this.subGroup.collapsed?false:true),
                parentid:this.cmbParent.getValue()!=""?this.cmbParent.getValue():"",
                parentname:this.cmbParent.getRawValue()!=""?this.cmbParent.getRawValue():"",
                showtotal:this.showTotal.getValue(),
                showchild:this.showChild.getValue(),
                showchildacc:this.showChildAcc.getValue(),
                excludeChildBalances:this.excludeChildBalances.getValue(),
                addBlankRowBefore:this.addBlankRowBefore.getValue()
            }
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    
    genSuccessResponse:function(response){
        if(response.success){
            if(response.duplicate == 1) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.DuplicateGroupName"), response.msg],3);
            } else {
//                this.fireEvent("mappingsaved");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg],3);
                this.resetFields();
                this.groupgridstore.load();
            }
        }
    },

    genFailureResponse:function(response){
    	
        var msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },
    
    handleRowClick : function(grid,rowindex,e){

        if(e.getTarget(".delete-gridrow")){
            var recdata = this.gridGroupSM.getSelected().data;
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttodeleteselectedGroup"),function(btn){
                if(btn=="yes") {
                    /*
                     Enable deleted group type in type combo.
                      */
                    if(recdata.nature == 6 || recdata.nature == 7 || recdata.nature == 8 || recdata.nature == 9){
                        this.enableDisableGroup(recdata,false);
                        this.typeCombo.store.loadData(this.typeDataArr);
                    }
                    
                    Wtf.Ajax.requestEx({
                        url:"ACCAccount/deleteLayoutGroup.do",
                        params: {
                            templateid : this.templateid,                    
                            sequence : recdata.sequence,                    
                            groupid : recdata.groupid                    
                        }
                    },this,this.genSuccessResponseGroup,this.genFailureResponseGroup);
                    
                }
            }, this);
        } else {            
            this.subGroup.collapse();
            var recdata = this.gridGroupSM.getSelected().data;
            this.filterParentGroupStore(recdata.nature, recdata.sequence);
            this.selectedGroupId = recdata.groupid;
            this.tempaltename.setValue(recdata.groupname); 
            this.showTotal.setValue(recdata.showtotal);
            this.showChild.setValue(recdata.showchild);
            this.showChildAcc.setValue(recdata.showchildacc);
            this.excludeChildBalances.setValue(recdata.excludeChildBalances);
            this.addBlankRowBefore.setValue(recdata.addBlankRowBefore);
            this.typeCombo.setValue(recdata.nature); 
            this.typeCombo.disable();
            this.sequence.setValue(recdata.sequence);
            if(recdata.parentid!=""){
                this.subGroup.expand();
                this.cmbParent.setValue(recdata.parentid);
//                this.sequence.disable();
            } else {
                this.cmbParent.setValue(undefined);
//                this.sequence.enable();
            }
            this.hideShowFields(recdata.nature);
            if(recdata.nature == 5) {
                this.masterds.load({
                    params : {
                        totalgroupid : this.selectedGroupId,
                        sequence:recdata.sequence
                    }
                });                
            } else {
                this.mappedAssetHeaderDs.load({
                    params : {
                        groupid : this.selectedGroupId
                    }
                });
                this.columnDsAsset.load({
                    params : {
                        nature : recdata.nature,
                        templateid : this.templateid
                    }
                });
            }
        }
    },
    
    genSuccessResponseGroup:function(response){
        if(response.success){
            this.resetFields();
            var msg=WtfGlobal.getLocaleText("acc.field.Grouphasbeendeletedsuccessfully");
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),msg],3);
        } else {
            msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        }
    },

    genFailureResponseGroup:function(response){
        var msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },
    
    resetFields : function(){
        this.selectedGroupId = "";
        this.tempaltename.setValue(""); 
        if(this.templatetype=='0'){//P&L
            var nature = 3;//Income
            this.typeCombo.setValue(nature);
        } else {//Balance Sheet
            nature = 1;//Asset
            this.typeCombo.setValue(nature);
        }
        this.subGroup.collapse();
        this.cmbParent.setValue(undefined);
        this.typeCombo.enable();
        this.sequence.setValue("");
//        this.sequence.enable();
        this.sequence.parentSequence = undefined;
        this.hideShowFields(1);
        this.mappedAssetHeaderDs.removeAll();
        this.columnDsAsset.load({
            params : {
                nature : nature
            }
        });
        this.groupgridstore.load();
    },
    
    toggleplusminus : function(grid, index, e){
        if(this.rulesm1.hasSelection()){
            var selected = this.rulesm1.getSelected();
            if(selected.data.ruletype == 1){
                selected.set('ruletype', 2);
                selected.set('groupname', "<div style='text-align:center;'><a href='#' style='text-decoration:none;color:#083772'>Minus</a></div>");
            }else if(selected.data.ruletype == 2) {
                selected.set('ruletype', 1);
                selected.set('groupname', "<div style='text-align:center;'><a href='#' style='text-decoration:none;color:#083772'>Plus</a></div>");
            }
        }
    },
    resetLayoutGroups : function() {        
//        for(var i=0; i < this.masterds1.getCount(); i++){            
//            for(var j=0; j < this.masterds.getCount(); j++){
//                if(this.masterds.getAt(j).data.sequence > this.masterds1.getAt(j).data.sequence){
//                    this.masterds.insert(j,this.masterds1.getAt(i));
//                    this.masterds1.remove(this.masterds1.getAt(i));
//                    break;
//                }
//                
//                if(j==this.masterds.getCount()) {
//                    this.masterds.add(this.masterds1.getAt(i));
//                    this.masterds1.remove(this.masterds1.getAt(i));
//                }
//            }   
//        }
        this.masterds1.removeAll();
        this.masterds.un("load", this.afterAvailableAccountStoreLoad, this);
        this.masterds.load({
            params : {
//                totalgroupid : this.selectedGroupId,
                sequence:this.sequence.getValue()
            }
        });
        var task = new Wtf.util.DelayedTask(function() {
                this.masterds.on("load", this.afterAvailableAccountStoreLoad, this);
        }, this);
        task.delay(100);        
    },
     movetorightclicked : function() {
         if(this.rulesm.hasSelection()) {
            var selected = this.rulesm.getSelections();
            for(var i= 0 ; i < selected.length ; i++) {
                if(this.masterds1.getCount() > 0){
                    var j=0;
                    for(j=0; j < this.masterds1.getCount(); j++){
                        if(this.masterds1.getAt(j).data.sequence > selected[i].data.sequence){
                            if(j==0) {
                                var record = new this.masterReader1({
                                    groupid : "plus" + selected[i].data.groupid,
                                    groupname : "<div style='text-align:center;'><a href='#' style='text-decoration:none;color:#083772'>Plus</a></div>",
                                    sequence : selected[i].data.sequence,
                                    ruletype : 1
                                });
                                this.masterds1.insert(j,record);
                                this.masterds1.insert(j,selected[i]);
                                
                                this.masterds.remove(selected[i]);
                            } else {
                                this.masterds1.insert(j,selected[i]);
                                var record = new this.masterReader1({
                                    groupid : "plus" + selected[i].data.groupid,
                                    groupname : "<div style='text-align:center;'><a href='#' style='text-decoration:none;color:#083772'>Plus</a></div>",
                                    sequence : selected[i].data.sequence,
                                    ruletype : 1
                                });
                                this.masterds1.insert(j,record);
                                this.masterds.remove(selected[i]);
                            }
                            break;
                        }
                    }
                    if(j==this.masterds1.getCount()){
                        var record = new this.masterReader1({
                                groupid : "and" + selected[i].data.groupid,
                                groupname : "<div style='text-align:center;'><a href='#' style='text-decoration:none;color:#083772'>Plus</a></div>",
                                sequence : selected[i].data.sequence,
                                ruletype : 1
                            });
                        this.masterds1.add(record);
                        this.masterds1.add(selected[i]);
                        this.masterds.remove(selected[i]);
                    }
                } else {
                    this.masterds1.insert(0,selected[i]);
                    this.masterds.remove(selected[i]);
                }
            }
        }
    },

    filterMainStore : function(storemain, storemapped){
        for(var i=0; i < storemapped.getCount(); i++){
            var accountid = storemapped.getAt(i).data.accid;
            var index = storemain.find('accid', accountid);
            if(index>-1){
                storemain.remove(storemain.getAt(index));
            }
        }
    }
    
});

/*
 *
 *COMPONENT USED FOR BALANCESHEET AND PROFIT AND LOSS
 *
 **/
Wtf.account.FinalStatement=function(config){
    this.total=[0,0,0,0,0,0,0,0];
    this.leftHeading=(config.statementType=="BalanceSheet"?"Liability":"Debit");
    this.rightHeading=(config.statementType=="BalanceSheet"?"Asset":"Credit");
     this.uPermType=Wtf.UPerm.fstatement;
     this.permType=Wtf.Perm.fstatement;
     this.exportPermType=(config.statementType=="BalanceSheet"?this.permType.exportdatabsheet:this.permType.exportdatatradingpnl);
     this.printPermType=(config.statementType=="BalanceSheet"?this.permType.printbsheet:this.permType.exportdatatradingpnl)
    this.toggle=0; 
    this.summaryL = new Wtf.grid.GridSummary();      //ERP-8702 to show total label in single line
    this.summaryR = new Wtf.grid.GridSummary();
    this.isSelectedCurrencyDiff =false;
    this.isCompareGlobal=false;
    this.periodView=config.periodView != undefined ?config.periodView:false;
    this.FinalStatementRec = new Wtf.data.Record.create([
        {name: 'accountname'},
        {name: 'accountcode'},
        {name: 'accountid'},
        {name: 'amount'},
        {name: 'openingamount'},
        {name: 'periodamount'},
        {name:'endingamount'},
        {name: 'amountInSelectedCurrency'},
        {name: 'preamount'},
        {name: 'accountflag'},
        {name: 'isdebit',type:'boolean'},
        {name: 'level'},
        {name: 'fmt'},
        {name: 'leaf'},
        {name: 'totalFlagAccountsWithchild'}
    ]);
    
    this.statementType=config.statementType||"Trading";


    this.currencyRec = new Wtf.data.Record.create([
                {name: 'currencyid',mapping:'tocurrencyid'},
                {name: 'symbol'},
                {name: 'currencyname',mapping:'tocurrency'},
                {name: 'exchangerate'},
                {name: 'htmlcode'}
            ]);

            this.currencyStoreCMB = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty:"count"
                },this.currencyRec),
        //        url:Wtf.req.account+'CompanyManager.jsp'
                url:"ACCCurrency/getCurrencyExchange.do"
            });


        this.currencyFilter= new Wtf.form.FnComboBox({
                fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
                hiddenName:'currencyid',
                width : 150,
                store:this.currencyStoreCMB,
                valueField:'currencyid',
                allowBlank : false,
                forceSelection: true,
                displayField:'currencyname',
                scope:this,
                selectOnFocus:true
            });

    //        this.currencyFilter.on('select', function(){
    //            this.selectedCurrencyRec= this.currencyStoreCMB.getAt(this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue())!=-1?this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue()):WtfGlobal.getCurrencyID());   
    //            }, this);

    this.lStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"							// Assets on Left side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    this.rStroe = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "right"							//  Liabilities on right side in Balance Sheet          Neeraj
        },this.FinalStatementRec),
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    
   
    this.lGrid = new Wtf.grid.HirarchicalGridPanel({
        plugins:[this.summaryL],
        //stripeRows :true,
        autoScroll:true,
        store: this.lStroe,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            renderer:this.formatAccountName,
            width:150,
            summaryRenderer:function(){return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));}.createDelegate(this)
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.coa.accCode")+'</b>',
            dataIndex:'accountcode',
//            sortable: true,
            hidden:!Wtf.account.companyAccountPref.showaccountcodeinfinancialreport,
//            renderer:this.formatAccountName,
            width:100,
            align: 'center'
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.openingAmount(liability)")+ " ("+WtfGlobal.getCurrencyName()+")":WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)")+ " ("+WtfGlobal.getCurrencyName()+")")+"</b></div>",
            dataIndex:'openingamount',
            renderer:this.formatMoney,
            hidden:((config.statementType!="BalanceSheet" && !this.periodView) || config.consolidateFlag),
            summaryRenderer:this.showLastRec.createDelegate(this,[4])
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.periodAmount(liability)")+ " ("+WtfGlobal.getCurrencyName()+")":WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)")+ " ("+WtfGlobal.getCurrencyName()+")")+"</b></div>",
            dataIndex:'periodamount',
            renderer:this.formatMoney,
            hidden:((config.statementType!="BalanceSheet" && !this.periodView) || config.consolidateFlag),
            summaryRenderer:this.showLastRec.createDelegate(this,[0])
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(liability)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Debit)"))+" ("+WtfGlobal.getCurrencyName()+") </b></div>",
            dataIndex:(config.statementType=="BalanceSheet" && !this.isCompareGlobal && this.periodView)?'endingamount':'amount',            
            renderer:this.formatMoney,
            hidecurrency : true,
            summaryRenderer:(config.statementType=="BalanceSheet" && !this.isCompareGlobal && this.periodView)?this.showLastRec.createDelegate(this,[6]):this.showLastRec.createDelegate(this,[1])
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(liability)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Debit)"))+ " ("+WtfGlobal.getCurrencyName()+") </b></div>",
            dataIndex:'preamount',
            hidden:((config.statementType!="BalanceSheet" && config.statementType!="TradingAndProfitLoss") || config.consolidateFlag),
            renderer:this.formatMoney,
            hidecurrency : true,
            summaryRenderer:this.showLastRec.createDelegate(this,[2])
        },{
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.dnList.gridAmt")+" ("+WtfGlobal.getCurrencyName()+")</b></div>",
            dataIndex:'amountInSelectedCurrency',
//            dataIndex:'amount',
            hidecurrency : true,
            renderer:this.formatMoneyInSelectedCurr.createDelegate(this),
            summaryRenderer:this.showLastRec.createDelegate(this,[4])
        }],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //ERP-28938
            deferEmptyText: false
        }
    });
    this.lStroe.on('load',function(store,rec,option){
        if(rec.length==0){
            this.lGrid.getView().refresh(true); 
        }
    });
    this.lGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.on('rowclick',this.onRowClickLGrid, this);
    this.rGrid = new Wtf.grid.HirarchicalGridPanel({
        plugins:[this.summaryR],
        //stripeRows :true,
        autoScroll:true,
        store: this.rStroe,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            width:150,
            renderer:this.formatAccountName,
            summaryRenderer:function(){return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));}.createDelegate(this)
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.coa.accCode")+'</b>',
            dataIndex:'accountcode',
//            sortable: true,
            hidden:!Wtf.account.companyAccountPref.showaccountcodeinfinancialreport,
//            renderer:this.formatAccountName,
            width:100,
            align: 'center'
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.openingAmount(asset)")+ " ("+WtfGlobal.getCurrencyName()+")":WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)")+ " ("+WtfGlobal.getCurrencyName()+")")+"</b></div>",
            dataIndex:'openingamount',
            renderer:this.formatMoney,
            hidden:((config.statementType!="BalanceSheet" && !this.periodView) || config.consolidateFlag),
            summaryRenderer:this.showLastRec.createDelegate(this,[5])
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.periodAmount(asset)")+ " ("+WtfGlobal.getCurrencyName()+")":WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)")+ " ("+WtfGlobal.getCurrencyName()+")")+"</b></div>",
            dataIndex:'periodamount',
            renderer:this.formatMoney,
            hidden:((config.statementType!="BalanceSheet" && !this.periodView) || config.consolidateFlag),
            summaryRenderer:this.showLastRec.createDelegate(this,[1])
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)")+ " ("+WtfGlobal.getCurrencyName()+")":WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)")+ " ("+WtfGlobal.getCurrencyName()+")")+"</b></div>",
            dataIndex:(config.statementType=="BalanceSheet" && !this.isCompareGlobal  && this.periodView)?'endingamount':'amount',
            renderer:this.formatMoney,
            summaryRenderer:(config.statementType=="BalanceSheet" && !this.isCompareGlobal && this.periodView)?this.showLastRec.createDelegate(this,[7]):this.showLastRec.createDelegate(this,[1])
        },{
            header:"<div align=right><b>"+(config.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)")+ " ("+WtfGlobal.getCurrencyName()+")":WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)")+ " ("+WtfGlobal.getCurrencyName()+")")+"</b></div>",
            dataIndex:'preamount',
            renderer:this.formatMoney,
            hidden:((config.statementType!="BalanceSheet" && config.statementType!="TradingAndProfitLoss") || config.consolidateFlag),
            summaryRenderer:this.showLastRec.createDelegate(this,[3])
        }
        ,{
            header:"<div align=right><b>"+"Amount"+" ("+WtfGlobal.getCurrencyName()+")</b></div>",
            dataIndex:'amountInSelectedCurrency',
//            dataIndex:'amount',
            hidecurrency : true,
            renderer:this.formatMoneyInSelectedCurr.createDelegate(this),
            summaryRenderer:this.showLastRec.createDelegate(this,[9])
        }
    ],
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),    //Left Grid   //ERP-28938
            deferEmptyText: false
        }
    });
    this.rGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.view.refresh.defer(1, this.lGrid.view); 
    this.rGrid.view.refresh.defer(1, this.rGrid.view); 
    this.rGrid.on('rowclick',this.onRowClickRGrid, this);    
    this.lGrid.on('render',function(){
        this.lGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.lGrid],1);
    },this);

    this.rGrid.on('render',function(){
        this.rGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.rGrid],1);
    },this);
    
    this.lStroe.on('load',function(store,rec,option){
        if(rec.length==0){
            this.lGrid.getView().refresh(true); 
        }
    });

//    this.lGrid.getStore().on("load", function(){
//        for(var i=0; i< this.lGrid.getStore().data.length; i++){
//            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));
//
//        }
//    }, this);
//
//    this.rGrid.getStore().on("load", function(){
//        for(var i=0; i< this.rGrid.getStore().data.length; i++){
//            this.rGrid.collapseRow(this.rGrid.getView().getRow(i));
//        }
//    }, this);

    
    var comboReader = new Wtf.data.Record.create([
    {
        name: 'id',
        type: 'string'
    },
    {
        name: 'name',
        type: 'string'
    },
    {
        name: 'templatecode',
        type: 'string'
    }
    ]);
   
    
    this.templateStore = new Wtf.data.Store({
        url :'ACCAccount/getPnLTemplates.do',
        root: 'data',
        remoteSort:true,
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },comboReader),
        baseParams:{
            isdropdown:true
        }
    });
    
    this.templateStore.on("load", this.templateStoreLoad, this);
    
    
    this.templateCombo= new Wtf.form.ComboBox({
        fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.het.101"):WtfGlobal.getLocaleText("acc.field.DebitAccount*")),
        width:100,
        store: this.templateStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        typeAhead: true,
        forceSelection: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    });
    
    var sdateSavedSearch;
    var edateSavedSearch;
    if(config.searchJson != undefined && config.searchJson != ""){
        sdateSavedSearch = JSON.parse(config.searchJson).data[0].sdate;
        edateSavedSearch = JSON.parse(config.searchJson).data[0].edate;
    }
    
this.firstTime=true;
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        width:125,
        //        hidden:config.statementType=='BalanceSheet',
        value:this.getDates(true, sdateSavedSearch)
    });
    this.startPreDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stpredate',
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        width:125,      
        hidden:this.periodView,
//        hidden:config.statementType=='BalanceSheet',
        value:this.getPreDates(this.startDate.getValue())
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        width:125,
        name:'enddate',
        value:this.getDates(false, edateSavedSearch)
    });
    this.endPreDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),
        format:WtfGlobal.getOnlyDateFormat(),
        //readOnly:true,
        width:125,
        hidden:this.periodView,
        name:'endpredate',
        value:this.getPreDates(this.endDate.getValue())
    });
    
    this.startDate.on("change",function(){    
     this.startPreDate.setValue(this.startDate.getValue().add(Date.YEAR, -1))
    },this);
    
    this.endDate.on("change",function(){    
     this.endPreDate.setValue(this.endDate.getValue().add(Date.YEAR, -1))
    },this);
    
    this.grid = this.rGrid;
    var mnuBtns=[];
    var csvbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        menu: {        
            items: [
            {
                text: WtfGlobal.getLocaleText("acc.common.exportToCsvTshape"),
                iconCls:'pwnd '+'exportcsv',     
                scope: this,
                handler:function(){
                    this.exportWithTemplate("csv",false)
                }
            },{
                text: WtfGlobal.getLocaleText("acc.common.exportToCsvStandard"),
                iconCls:'pwnd '+'exportcsv',             
                scope: this,
                handler:function(){
                    this.exportWithTemplate("csv",true)
                }
            }
            
            ]
        }
    });
    mnuBtns.push(csvbtn)
       var xlsbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        menu: {        
            items: [
            {
                text: WtfGlobal.getLocaleText("acc.common.exportToCsvTshape"),
                iconCls:'pwnd '+'exportcsv',     
                scope: this,
                handler:function(){
                    this.exportWithTemplate("xls",false)
                }
            },{
                text: WtfGlobal.getLocaleText("acc.common.exportToCsvStandard"),
                iconCls:'pwnd '+'exportcsv',             
                scope: this,
                handler:function(){
                    this.exportWithTemplate("xls",true)
                }
            }
            
            ]
        }
    });
     mnuBtns.push(xlsbtn)
     var pdfbtn=new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
        scope: this,
        menu: {        
            items: [
            {
                text: WtfGlobal.getLocaleText("acc.common.exportToPDFTreeView"),
                iconCls:'pwnd '+'exportpdf',                
                scope: this,
                handler:function(){
                    this.exportPdfTemplate(true)
                }
            },{
                text: WtfGlobal.getLocaleText("acc.common.exportToPDFStandard"),
                iconCls:'pwnd '+'exportpdf',                
                scope: this,
                handler:function(){
                    this.exportPdfTemplate(false)
                }
            },{
                text: WtfGlobal.getLocaleText("acc.common.exportToPDFMonthlyBalanceSheet"),
                iconCls:'pwnd '+'exportpdf',                
                scope: this,
                handler:function(){
                    this.exportPdfTemplate(false,true);
                }
            }
            ]
        }    
    });
    mnuBtns.push(pdfbtn)
    
    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:140,
        displayField:'name',
        valueField:'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead:true,
        value:"",
        selectOnFocus:true,
        forceSelection: true,
        emptyText:WtfGlobal.getLocaleText("acc.rem.9")  //,"Select a Cost Center"
    });

    this.currencyStoreCMB.on('load', function(){
                this.currencyFilter.setValue(WtfGlobal.getCurrencyID());
            }, this);
    this.currencyStoreCMB.load();
    var btnArr=[];
    var btnArr1=[];
       this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden: this.isSummary,
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
       this.resetBttn.on('click',this.handleResetClickNew,this);
       btnArr.push(
        (WtfGlobal.getLocaleText("acc.common.from")),this.startDate,
        (WtfGlobal.getLocaleText("acc.common.to")),this.endDate,
//        'Custom Layout ', this.templateCombo,
        '-',{
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
            iconCls:'accountingbase fetch',
            style:" margin-left: 5px;",
            tooltip:(config.statementType=='BalanceSheet'?WtfGlobal.getLocaleText("acc.cc.26"):WtfGlobal.getLocaleText("acc.cc.27")),
            scope:this,
            handler:this.fetchStatement
        },'-',this.resetBttn
        );
    if(this.statementType=="TradingAndProfitLoss"){
        btnArr.push(WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),this.currencyFilter);
    }
    if(!this.periodView){
        if ((config.statementType == "BalanceSheet" || config.statementType=="TradingAndProfitLoss")  && !config.consolidateFlag) {
            btnArr.push('-', (WtfGlobal.getLocaleText("acc.common.compareWith")),
                (WtfGlobal.getLocaleText("acc.common.from")), this.startPreDate,
                (WtfGlobal.getLocaleText("acc.common.to")), this.endPreDate,{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.compare"),  //'Fetch',
        iconCls:'accountingbase fetch',
        tooltip:WtfGlobal.getLocaleText("acc.cc.balanceSheet.compare.TT"),
        hidden:this.periodView,
        scope:this,
        style:" margin-left: 5px;",
        handler:this.fetchCompareStatement
        }
                );
    }
    }
    
    if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter"){
        btnArr1.push(WtfGlobal.getLocaleText("acc.common.costCenter"),this.costCenter);
    }
     this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        reportid: config.reportid,
        moduleid:config.statementType=="CostCenter"?101:config.moduleid,
        ignoreDefaultFields : true,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
//        id: 'advanced3', // In use, Do not delete
        scope: this,
//        hidden:(this.moduleid==undefined)?true:false,        
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });

    btnArr1.push('-',this.AdvanceSearchBtn);
    
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
        btnArr.push(this.expButton=new Wtf.Button({
    //        iconCls:'pwnd '+'exportcsv',
            text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
//                    disabled :true,
            scope: this,
            menu:mnuBtns
        }));
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
        
        btnArr.push(this.printbtn=new Wtf.Button({
            iconCls:'pwnd printButtonIcon',
            text :WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details.',
//            disabled :true,
            scope: this,
            handler:function(){
                this.exportWithTemplate("print")
            }
        }));
    }

    btnArr1.push(this.ToggleButton=new Wtf.Button({			// Used for toggling assets and liabilities from Left to Right & Vice versa
            text:WtfGlobal.getLocaleText("acc.balanceSheet.toggle"),  //'Toggle',
            iconCls:'pwnd toggleButtonIcon',
            tooltip :(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.toggleTT"):WtfGlobal.getLocaleText("acc.balanceSheet.toggleTT")),
            scope: this,
            handler: this.swapGrids,
            hidden:(config.statementType=="BalanceSheet"?false:true)
        }));
    
//   this.expButton=new Wtf.exportButton({
//        obj:this,
//        id:"exportReports"+config.helpmodeid,
//        tooltip :'Export report details',
//        params:{ stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
//                 enddate:WtfGlobal.convertToGenericDate(this.getDates(false))/*,
//                 accountid:this.accountID||config.accountID*/
//        },
//        menuItem:{csv:true,pdf:true,rowPdf:false},
//        get:27
//    });

    this.expandCollpseButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.Expand"),
            tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
            iconCls:'pwnd toggleButtonIcon',
            scope:this,
            handler: function(){
                this.expandCollapseGrid(this.expandCollpseButton.getText());
            }
        });
        
    btnArr1.push('-', this.expandCollpseButton);
    
    this.customLayoutButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.ViewCustomLayout"),
        tooltip:WtfGlobal.getLocaleText("acc.field.Clicktoviewcustomlayout"),
        iconCls:'accountingbase fetch',
        scope:this,
        handler: function(){
            var window = new Wtf.account.selectCustomLayout({
                closable: true,
                templatetype:config.statementType=="BalanceSheet"?1:0
            });
            window.show();
        }
    });
    
    btnArr1.push('-', this.customLayoutButton);
    
    this.periodViewBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.periodView"),
        tooltip:WtfGlobal.getLocaleText("acc.periodView"),
        iconCls:'pwnd splitViewIcon',
        hidden:(!(config.statementType=="BalanceSheet")|| !this.periodView),
        scope:this,
        handler: function(){
           this.templateid = this.templateCombo.getValue();
           BalanceSheet(config.consolidateFlag,"", this.templateid);
        }
    });
    btnArr1.push('-',this.periodViewBtn);
     
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[this.objsearchComponent,
            this.centerPanel = new Wtf.Panel({
                region:'center',
                layout:'fit',
                width:'49%',
                border:true,
                items:this.statementType=="BalanceSheet"?this.lGrid:this.rGrid
            }),
            this.westPanel = new Wtf.Panel({
                region:'west',
                layout:'fit',
                width:'49%',
                border:false,
                split:true,
                items:this.statementType=="BalanceSheet"?this.rGrid:this.lGrid
            })],
       tbar:btnArr1
    });

    Wtf.apply(this,{
        saperate:true,
        statementType:"Trading",
        items:this.wrapperPanel,
        tbar:btnArr
    },config);

     Wtf.account.FinalStatement.superclass.constructor.call(this,config);
     this.addEvents({
        'account':true
     });
     //this.templateStore.load();     //ERP-29938
}

Wtf.extend( Wtf.account.FinalStatement,Wtf.Panel,{
    collapseGrids : function() {
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

        }
    
        for(var i=0; i< this.rGrid.getStore().data.length; i++){
            this.rGrid.collapseRow(this.rGrid.getView().getRow(i));
        }
    
    },
    
    templateStoreLoad : function(){
        this.templateStore.insert(0, new Wtf.data.Record({
            'id' : '-1', 
            'name' : 'None',
            'templatecode' : "" 
        }));
        if(this.templateid) {
            this.templateCombo.setValue(this.templateid);
        } else {
            this.templateCombo.setValue('-1');
        }
        
        if(this.statementType!="CostCenter"){
            this.fetchStatement();
        }
    },
    onRowClickRGrid:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accName = this.rGrid.getStore().getAt(i).data['accountname'];
        if(accName == Wtf.Difference_in_Opening_balances){
            callLedger(accName, this.startDate.getValue() , this.endDate.getValue(), undefined, undefined, undefined, this.periodView);
        }else{
            var accid=this.rGrid.getStore().getAt(i).data['accountid'];
            var searchJson = (this.objsearchComponent.advGrid != undefined) ? this.objsearchComponent.advGrid.getJsonofStore() : "" ;
            this.fireEvent('account',searchJson,this.filterConjuctionCrit,accid,this.startDate.getValue(),this.endDate.getValue());
        }
    },
    onRowClickLGrid:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var accName = this.lGrid.getStore().getAt(i).data['accountname'];
        if(accName == Wtf.Difference_in_Opening_balances){
            callLedger(accName, this.startDate.getValue() , this.endDate.getValue(),  undefined, undefined, undefined, this.periodView);     
        }else{
            var accid=this.lGrid.getStore().getAt(i).data['accountid'];
            var searchJson = (this.objsearchComponent.advGrid != undefined) ? this.objsearchComponent.advGrid.getJsonofStore() : "" ;
            this.fireEvent('account',searchJson,this.filterConjuctionCrit,accid,this.startDate.getValue(),this.endDate.getValue());
        }
    },
    expandCollapseGrid : function(btntext){
        if(btntext == WtfGlobal.getLocaleText("acc.field.Collapse")){
            for(var i=0; i< this.lGrid.getStore().data.length; i++){
                this.lGrid.collapseRow(this.lGrid.getView().getRow(i));

            }

            for(var i=0; i< this.rGrid.getStore().data.length; i++){
                this.rGrid.collapseRow(this.rGrid.getView().getRow(i));
            }

            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            for(var i=0; i< this.lGrid.getStore().data.length; i++){
                this.lGrid.expandRow(this.lGrid.getView().getRow(i));

            }

            for(var i=0; i< this.rGrid.getStore().data.length; i++){
                this.rGrid.expandRow(this.rGrid.getView().getRow(i));
            }

            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    exportPdfTemplate:function(isAlignment,isJasper){
        var get;
        var fileName;
        var jsonGrid;
        var exportUrl;
        var header;
        var extraParams="";
        var templatecode = '-1';
        var filterConjuctionCriteria="";
        var searchJson="";
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
        if(this.statementType=="BalanceSheet"){
//            title = "Particulars,Amount(Asset),Particulars,Amount(Liability)";
            extraParams += "&isCompareGlobal="+((this.isCompareGlobal!=null&&this.isCompareGlobal!=undefined)?this.isCompareGlobal:false); 
            exportUrl = getExportUrl(27, this.consolidateFlag);
            fileName = "BalanceSheet";
            get = 27;
            toggle = this.toggle;
            searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
                jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'lamount','title':'Amount(Asset)','width':'150','align':'currency'},"+
                            "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
                            "{'header':'ramount','title':'Amount(Liability)','width':'150','align':'currency'}]}";
            header =  WtfGlobal.getLocaleText("acc.balanceSheet");
        }else{
//            title = "Particulars,Amount(Debit),Particulars,Amount(Credit)";
            var currencyFilterRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
            this.isSelectedCurrencyDiff=(this.currencyFilter!=null&&this.currencyFilter!=undefined)?(this.currencyFilter.getValue()!=WtfGlobal.getCurrencyID()):false;
            exportUrl = getExportUrl(28, this.consolidateFlag);
            if(this.statementType=="TradingAndProfitLoss"){
                fileName = "Trading Profit and Loss Statement";
            } else if (this.statementType=="CostCenter"){
                fileName = WtfGlobal.getLocaleText("acc.ccReport.tab2");
            }
            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&reportView="+this.statementType;
            extraParams += "&filterCurrency="+this.currencyFilter.getValue();
            extraParams += "&externalcurrencyrate="+((currencyFilterRec!=null&&currencyFilterRec!=undefined)?currencyFilterRec.data.exchangerate:0); 
            extraParams += "&isCompareGlobal="+((this.isCompareGlobal!=null&&this.isCompareGlobal!=undefined)?this.isCompareGlobal:false); 
            extraParams += "&isSelectedCurrencyDiff="+((this.isSelectedCurrencyDiff!=undefined&&this.isSelectedCurrencyDiff!=null)?this.isSelectedCurrencyDiff:false); 

            get = 28;
            searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
                jsonGrid = "{data:[{'header':'laccountname','title':'Particulars','width':'150','align':''},"+
                "{'header':'lamount','title':'Amount(Debit)','width':'150','align':'currency'},"+
                "{'header':'raccountname','title':'Particulars','width':'150','align':''},"+
                "{'header':'ramount','title':'Amount(Credit)','width':'150','align':'currency'}]}";
            header =  fileName;
        }  
        var configstr = "{%22landscape%22:%22true%22,%22pageBorder%22:%22true%22,%22gridBorder%22:%22true%22,%22title%22:%22Test%20Title%22,%22subtitles%22:%22%22,%22headNote%22:%22Test%20Header%22,%22showLogo%22:%22false%22,%22headDate%22:%22false%22,%22footDate%22:%22true%22,%22footPager%22:%22false%22,%22headPager%22:%22true%22,%22footNote%22:%22Test%20Footer%22,%22textColor%22:%22000000%22,%22bgColor%22:%22FFFFFF%22}"
        if(this.consolidateFlag) {
         var url = exportUrl+"&filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&isJasper="+isJasper+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle
                     +"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&isAlignment="+isAlignment+"&templatecode="+templatecode;
        } else if(this.isCompareGlobal){
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&isJasper="+isJasper+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle
                     +"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&isAlignment="+isAlignment+"&stpredate="+this.presdate+"&endpredate="+this.preedate+"&templatecode="+templatecode+"&periodView="+this.periodView;
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&config="+configstr+"&header"+header+"&isJasper="+isJasper+"&filetype=pdf&stdate="+this.sdate+"&enddate="+this.edate+"&toggle="+this.toggle
                     +"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria+"&get="+get+"&gridconfig="+encodeURIComponent(jsonGrid)+"&isAlignment="+isAlignment+"&templatecode="+templatecode+"&periodView="+this.periodView+"&isFromBalanceSheet="+(this.statementType=="BalanceSheet"); //SDP-13756 : isFromBalanceSheet - Check added to identify export call from Balance Sheet
        }
            Wtf.get('downloadframe').dom.src = url;

        /*new Wtf.selectTempWin({
                type:'pdf',
                get:get,
                stdate:this.sdate,
                enddate:this.edate,
                accountid:"",
                extra:{},
                mode:"",
                paramstring:"",
                filename:fileName,
                storeToload:"",//obj.pdfStore,
                gridConfig : jsonGrid,
                grid:"",
                json:""
            });*/
    },
    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);
        this.startDate.setValue(startDate?startDate:this.getDates(true));
        this.startPreDate.setValue(this.startDate.getValue());
        this.endDate.setValue(endDate?endDate:this.getDates(false));
        this.endPreDate.setValue(this.endDate.getValue());
        this.fetchStatement();
    },
    exportWithTemplate:function(type,csvflag){
        
       
        if(!this.sdate || !this.edate) {
            if(!this.startDate.getValue() || !this.endDate.getValue()){
                WtfComMsgBox(42,2);
                return;
            }    
            this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
            this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        }
       
        type = type?type:"csv";
        var exportUrl;
        var fileName;
        var reportName;
        var header = "laccountname,lamount,raccountname,ramount";
         var title = "";
         var title1 = "";
         var title2 = "";
         var othertitle = "";
         var csvflag = csvflag==undefined? false:csvflag;
         var extraParams = "";
         var templatecode = '-1';
         var filterConjuctionCriteria="";
        var searchJson="";
        var index = this.templateStore.find('id', this.templateCombo.getValue());
        if(index > -1){
            templatecode = this.templateStore.getAt(index).data.templatecode;
        }
        if(this.periodView){
            if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
                title1 = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.coa.accCode")+","+WtfGlobal.getLocaleText("acc.report.openingAsset")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.report.periodAsset")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.report.endingAsset")+ " ("+WtfGlobal.getCurrencyName()+")";
                title2 = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.coa.accCode")+","+WtfGlobal.getLocaleText("acc.report.openingLiability")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.report.periodLiability")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.report.endingLiability")+ " ("+WtfGlobal.getCurrencyName()+")";
            }else{
                title1 = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.openingAsset")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.report.periodAsset")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.report.endingAsset")+ " ("+WtfGlobal.getCurrencyName()+")";
                title2 = WtfGlobal.getLocaleText("acc.report.2")+","+WtfGlobal.getLocaleText("acc.report.openingLiability")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.report.periodLiability")+ " ("+WtfGlobal.getCurrencyName()+")"+","+WtfGlobal.getLocaleText("acc.report.endingLiability")+ " ("+WtfGlobal.getCurrencyName()+")";
            }
        }else if(this.isCompareGlobal){
            if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                title1 = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + WtfGlobal.getLocaleText("acc.report.18") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.18") + " (" + WtfGlobal.getCurrencyName() + ")";
                title2 = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + WtfGlobal.getLocaleText("acc.report.19") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.19") + " (" + WtfGlobal.getCurrencyName() + ")";
            } else {
                title1 = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.18") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.18") + " (" + WtfGlobal.getCurrencyName() + ")";
                title2 = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.19") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.19") + " (" + WtfGlobal.getCurrencyName() + ")";
            }
        } else {
            if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                title1 = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + WtfGlobal.getLocaleText("acc.report.18") + " (" + WtfGlobal.getCurrencyName() + ")";
                title2 = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + WtfGlobal.getLocaleText("acc.report.19") + " (" + WtfGlobal.getCurrencyName() + ")";
            } else {
                title1 = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.18") + " (" + WtfGlobal.getCurrencyName() + ")";
                title2 = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.19") + " (" + WtfGlobal.getCurrencyName() + ")";
            }
        }
        if(this.statementType=="BalanceSheet"){
            if(csvflag){
                    title = this.toggle == 0 ? title1 : title2;
                    othertitle = this.toggle == 0 ? title2 : title1;
                if(this.periodView){ 
                    if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
                        header = "laccountname,laccountcode,lopeningamount,lperiodamount,lendingamount";
                    }else{
                        header = "laccountname,lopeningamount,lperiodamount,lendingamount";
                    }
                } else if(this.isCompareGlobal){
                    if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        header = "laccountname,laccountcode,lamount,lpreamount";
                    } else {
                        header = "laccountname,lamount,lpreamount";
                    }
                } else {
                    if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        header = "laccountname,laccountcode,lamount";
                    } else {
                        header = "laccountname,lamount";
                    }
                }
            }else{
                title = this.toggle == 0? title1+","+title2:title2+","+title1;
                if(this.periodView){
                    if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
                        header = "raccountname,raccountcode,ropeningamount,rperiodamount,rendingamount,laccountname,laccountcode,lopeningamount,lperiodamount,lendingamount";
                    }else{
                        header = "raccountname,ropeningamount,rperiodamount,rendingamount,laccountname,lopeningamount,lperiodamount,lendingamount";
                    }
                } else if(this.isCompareGlobal){
                    if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        header = "raccountname,raccountcode,ramount,rpreamount,laccountname,laccountcode,lamount,lpreamount";
                    } else {
                        header = "raccountname,ramount,rpreamount,laccountname,lamount,lpreamount";
                    }
                } else {
                    if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        header = "raccountname,raccountcode,ramount,laccountname,laccountcode,lamount";
                    } else {
                        header = "raccountname,ramount,laccountname,lamount";
                    }
                }
            }
            exportUrl = getExportUrl(27, this.consolidateFlag);
            fileName = WtfGlobal.getLocaleText("acc.balanceSheet");
            reportName = WtfGlobal.getLocaleText("acc.balanceSheet");
            searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
        }else{
           var currencyFilterRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
            this.isSelectedCurrencyDiff=(this.currencyFilter!=null&&this.currencyFilter!=undefined)?(this.currencyFilter.getValue()!=WtfGlobal.getCurrencyID()):false;
            if(!this.isCompareGlobal&&!this.isSelectedCurrencyDiff){
                if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                    title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + WtfGlobal.getLocaleText("acc.report.3") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.pre.18") + " (" + WtfGlobal.getCurrencyName() + ")";
                } else {
                    title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.3") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.pre.18") + " (" + WtfGlobal.getCurrencyName() + ")";
                }
            }else{
                if(this.isSelectedCurrencyDiff){
                    if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        title = WtfGlobal.getLocaleText("acc.report.2")+ "," + WtfGlobal.getLocaleText("acc.coa.accCode")  + "," + WtfGlobal.getLocaleText("acc.report.3") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.18") + " (" + currencyFilterRec.data.currencyname + ")" + "," + WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.4") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.4") + " (" + currencyFilterRec.data.currencyname + ")";
                    } else {
                        title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.3") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.18") + " (" + currencyFilterRec.data.currencyname + ")" + "," + WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.4") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.4") + " (" + currencyFilterRec.data.currencyname + ")";
                    }
                } else {
                    if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + WtfGlobal.getLocaleText("acc.report.3") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.18") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.4") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.4") + " (" + WtfGlobal.getCurrencyName() + ")";
                    } else {
                        title = WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.3") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.18") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.2") + "," + WtfGlobal.getLocaleText("acc.report.4") + " (" + WtfGlobal.getCurrencyName() + ")" + "," + WtfGlobal.getLocaleText("acc.report.pre.4") + " (" + WtfGlobal.getCurrencyName() + ")";
                    }
                }
            }
            
            exportUrl = getExportUrl(28, this.consolidateFlag);
            if (this.statementType == "TradingAndProfitLoss") {
                if (csvflag) {
                    if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        header = "accountname,accountcode,amount";
                        title = "Particulars " + "," + WtfGlobal.getLocaleText("acc.coa.accCode") + "," + "Amount (SG Dollar (SGD))";
                    } else{
                        header = "accountname,amount";
                        title = "Particulars,Amount (SG Dollar (SGD))";
                    }
                }
                else if (!this.isCompareGlobal && !this.isSelectedCurrencyDiff) {
                    if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                        header = "laccountname,laccountcode,lamount,raccountname,raccountcode,ramount";
                    } else {
                        header = "laccountname,lamount,raccountname,ramount";
                    }
                } else {
                    if (this.isSelectedCurrencyDiff) {
                        if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                            header = "laccountname,laccountcode,lamount,lamountInSelectedCurrency,raccountname,ramount,ramountInSelectedCurrency";
                        } else {
                            header = "laccountname,lamount,lamountInSelectedCurrency,raccountname,ramount,ramountInSelectedCurrency";
                        }
                    } else {
                        if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                            header = "laccountname,laccountcode,lamount,lpreamount,raccountname,ramount,rpreamount";
                        } else {
                            header = "laccountname,lamount,lpreamount,raccountname,ramount,rpreamount";
                        }
                    }
                }
                fileName = WtfGlobal.getLocaleText("acc.P&L.tabTitle");
                reportName = WtfGlobal.getLocaleText("acc.P&L.tabTitle");
            } else if (this.statementType=="CostCenter"){
                if (csvflag) {
                    header = "accountname,amount";
                }
                fileName = WtfGlobal.getLocaleText("acc.ccReport.tab2")+"_v1";
                reportName = WtfGlobal.getLocaleText("acc.ccReport.tabTitle");
            }
            extraParams += "&costcenter="+this.costCenter.getValue();
            extraParams += "&reportView="+this.statementType;
            extraParams += "&filterCurrency="+this.currencyFilter.getValue();
            extraParams += "&externalcurrencyrate="+((currencyFilterRec!=null&&currencyFilterRec!=undefined)?currencyFilterRec.data.exchangerate:0); 
            extraParams += "&isCompareGlobal="+((this.isCompareGlobal!=null&&this.isCompareGlobal!=undefined)?this.isCompareGlobal:false); 
            extraParams += "&isSelectedCurrencyDiff="+((this.isSelectedCurrencyDiff!=undefined&&this.isSelectedCurrencyDiff!=null)?this.isSelectedCurrencyDiff:false); 
    
            searchJson= this.searchJson!=undefined?this.searchJson:"";
            filterConjuctionCriteria= this.filterConjuctionCrit;
        }
        var align ="";
        if(this.periodView){
            if(Wtf.account.companyAccountPref.showaccountcodeinfinancialreport){
                align = "none,none,withoutcurrency,withoutcurrency,withoutcurrency,none,none,withoutcurrency,withoutcurrency,withoutcurrency";
            }else{
                align = "none,withoutcurrency,withoutcurrency,withoutcurrency,none,withoutcurrency,withoutcurrency,withoutcurrency";
            }
        }else if(this.statementType=="CostCenter"){   //ERP-21997
            align = "none,withoutcurrency,none,withoutcurrency";
        } else {
            if (Wtf.account.companyAccountPref.showaccountcodeinfinancialreport) {
                align = "none,none,withoutcurrency,withoutcurrency,none,none,withoutcurrency,withoutcurrency";
            } else {
                align = "none,withoutcurrency,withoutcurrency,none,withoutcurrency,withoutcurrency";
            }
        }
        if(this.consolidateFlag) {
            var url = exportUrl+"&filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&stpredate="+this.presdate+"&endpredate="+this.preedate+"&templatecode="+templatecode+"&othertitle="+encodeURIComponent(othertitle)+"&csvflag="+csvflag+"&periodView="+this.periodView;
        } else if(this.isCompareGlobal) {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&templatecode="+templatecode+"&stpredate="+this.presdate+"&endpredate="+this.preedate+"&othertitle="+encodeURIComponent(othertitle)+"&csvflag="+csvflag+"&periodView="+this.periodView;
        } else {
            url = exportUrl+"?filename="+encodeURIComponent(fileName)+extraParams+"&filetype="+type+"&stdate="+this.sdate+"&enddate="+this.edate+"&nondeleted="+true+"&accountid="+"&searchJson="+searchJson+"&filterConjuctionCriteria="+filterConjuctionCriteria
                            +"&header="+header+"&title="+encodeURIComponent(title)+"&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&toggle="+this.toggle+"&templatecode="+templatecode+"&othertitle="+encodeURIComponent(othertitle)+"&csvflag="+csvflag+"&periodView="+this.periodView+"&isFromBalanceSheet="+(this.statementType=="BalanceSheet"); //SDP-13756 : isFromBalanceSheet - Check added to identify export call from Balance Sheet
        }
        if(type == "print") {
            url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            Wtf.get('downloadframe').dom.src  = url;
        }
//        Wtf.get('downloadframe').dom.src = url;
    },
    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
        }
        return grid.getRowClass()+colorCss;
    },
    
    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=val;
        if(rec.data['fmt']){
            fmtVal='<font ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="") {
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        }
        
        if(rec.data.accountflag) {
            fmtVal = WtfGlobal.accountLinkRenderer(fmtVal);
        }
        return fmtVal; 
    },

    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    fetchCompareStatement:function(){
        this.currencyFilter.setValue(WtfGlobal.getCurrencyID());
        this.fetchStatement(true);
    },
    formatMoneyInSelectedCurr:function(val,m,rec,i,j,s){
        var currencyRec;
        var symbol;
        if(this.currencyStoreCMB!=undefined){
//            currencyRec=this.currencyStoreCMB.getAt(this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue())!=-1?this.currencyStoreCMB.find('currencyid',this.currencyFilter.getValue()):WtfGlobal.getCurrencyID());
            currencyRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
            symbol=currencyRec!=null?currencyRec.data.symbol:WtfGlobal.getCurrencySymbol();
        }else{
            symbol=WtfGlobal.getCurrencySymbol();
        }
        var v=parseFloat(val);
        if(isNaN(v)){
            v= val;
        }else {
            v= WtfGlobal.conventInDecimal(v,symbol);
        }
        var fmtVal='<div class="currency">'+v+'</div>';
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },
    fetchStatement:function(isCompare){
       this.isCompareGlobal=(isCompare==true)?isCompare:false;
       this.lGrid.getStore().removeAll();
       this.rGrid.getStore().removeAll();
       
       this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
       WtfComMsgBox(29,4,true); //Show loading mask
       this.sDate=this.startDate.getValue();
       this.eDate=this.endDate.getValue();
       
       if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
       }
       var templatecode = '-1';
       var index = this.templateStore.find('id', this.templateCombo.getValue());
       if(index > -1){
           templatecode = this.templateStore.getAt(index).data.templatecode;
       }
       var currencyRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
       this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
       this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.presdateCompare = "";
        this.preedateCompare = "";
        this.presdate = WtfGlobal.convertToGenericStartDate(this.startPreDate.getValue());
        this.preedate = WtfGlobal.convertToGenericEndDate(this.endPreDate.getValue());
        if (isCompare == true) {
            this.presdateCompare = this.presdate;
            this.preedateCompare = this.preedate;
            if ((this.statementType == 'BalanceSheet' || this.statementType=="TradingAndProfitLoss")) {
                var leftGrid = this.lGrid.colModel.config[5];
                var rightGrid = this.rGrid.colModel.config[5];
                var leftGrid1 = this.lGrid.colModel.config[4];
                var rightGrid1 = this.rGrid.colModel.config[4];
                var lopening = this.lGrid.colModel.config[2];
                var lperiod = this.rGrid.colModel.config[3];
                var rperiod = this.lGrid.colModel.config[3];
                var ropening = this.rGrid.colModel.config[2];
                var leftGridSelectedCurr = this.lGrid.colModel.config[6];
                var rightGridSelectedCurr = this.rGrid.colModel.config[6];
                if (rightGrid != undefined){
                    rightGrid.hidden = false;
                    rightGrid1.hidden=false;
                    rightGridSelectedCurr.hidden = true;
                    ropening.hidden=true;
                    rperiod.hidden=true;
                    rightGrid1.dataIndex='amount';
                    rightGrid1.summaryRenderer=this.showLastRec.createDelegate(this,[1]);
                    rightGrid.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+ " ("+WtfGlobal.getCurrencyName()+") </b></b><br/>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                    rightGridSelectedCurr.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+ " ("+WtfGlobal.getCurrencyName()+") </b></b><br/>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                    rightGrid1.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+ " ("+WtfGlobal.getCurrencyName()+") </b></b><br/>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                }                    
                if (leftGrid != undefined){
                    leftGridSelectedCurr.hidden = true;
                    leftGrid.hidden = false;
                    leftGrid1.hidden=false;
                    lopening.hidden=true;
                    lperiod.hidden=true;
                    leftGrid1.dataIndex='amount';
                    leftGrid1.summaryRenderer=this.showLastRec.createDelegate(this,[0]);
                    leftGrid.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(liability)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Debit)"))+ " ("+WtfGlobal.getCurrencyName()+") </b></b><br/>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                    leftGridSelectedCurr.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(liability)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Debit)"))+ " ("+WtfGlobal.getCurrencyName()+") </b></b><br/>("+WtfGlobal.onlyDateRendererForGridHeader(this.startPreDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endPreDate.getValue())+")</div>";
                    leftGrid1.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(liability)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Debit)"))+ " ("+WtfGlobal.getCurrencyName()+") </b></b><br/>("+WtfGlobal.onlyDateRendererForGridHeader(this.startDate.getValue())+" To "+WtfGlobal.onlyDateRendererForGridHeader(this.endDate.getValue())+")</div>";
                }
                this.lGrid.getView().refresh(true);
                this.rGrid.getView().refresh(true);
            }
        } else {
            if ((!this.firstTime && (this.statementType == 'BalanceSheet' || this.statementType=="TradingAndProfitLoss")) || ((this.statementType == 'BalanceSheet' && !this.periodView))) {
                var leftGrid = this.lGrid.colModel.config[5];
                var rightGrid = this.rGrid.colModel.config[5];
                var leftGrid1 = this.lGrid.colModel.config[4];
                var rightGrid1 = this.rGrid.colModel.config[4];
                var lopening = this.lGrid.colModel.config[2];
                var lperiod = this.rGrid.colModel.config[3];
                var rperiod = this.lGrid.colModel.config[3];
                var ropening = this.rGrid.colModel.config[2];
                var leftGridSelectedCurr = this.lGrid.colModel.config[6];
                var rightGridSelectedCurr = this.rGrid.colModel.config[6];
                if (rightGrid != undefined){
                    if(this.currencyFilter.getValue()==WtfGlobal.getCurrencyID()){
                        rightGridSelectedCurr.hidden = true;
                        rightGrid1.hidden=false;
                    }else{
                        rightGridSelectedCurr.hidden = false;
                        rightGrid1.hidden=true;
                    }
                    rightGrid1.dataIndex=(this.statementType=="BalanceSheet" && !this.isCompareGlobal && this.periodView)? 'endingamount':'amount';
                    rightGrid1.summaryRenderer=(this.statementType=="BalanceSheet" && !this.isCompareGlobal && this.periodView)?this.showLastRec.createDelegate(this,[7]):this.showLastRec.createDelegate(this,[1]);                    
                    if(this.periodView){
                        ropening.hidden=false;
                        rperiod.hidden=false;                   
                        rightGrid.hidden = true;    
                    }else{
                        ropening.hidden=true;
                        rperiod.hidden=true;
                        rightGrid.hidden = true;
                    }
                    rightGrid1.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+ " ("+WtfGlobal.getCurrencyName()+")</b></div>";
                    rightGridSelectedCurr.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(asset)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Credit)"))+ " ("+((currencyRec!=null&&currencyRec.data.currencyname!=undefined)?currencyRec.data.currencyname:WtfGlobal.getCurrencyName())+")</b></div>";
                }
                if (leftGrid != undefined){
                    if(this.currencyFilter.getValue()==WtfGlobal.getCurrencyID()){
                        leftGridSelectedCurr.hidden = true;
                        leftGrid1.hidden=false;
                    }else{
                        leftGridSelectedCurr.hidden = false;
                        leftGrid1.hidden=true;
                    }
                    if(this.periodView){
                        lopening.hidden=false;
                        lperiod.hidden=false;
                        leftGrid.hidden = true;
                    }else{
                        lopening.hidden=true;
                        lperiod.hidden=true;          
                        leftGrid.hidden = true;
                    }
                    leftGrid1.dataIndex=(this.statementType=="BalanceSheet" && !this.isCompareGlobal && this.periodView)? 'endingamount':'amount';
                    leftGrid1.summaryRenderer=(this.statementType=="BalanceSheet" && !this.isCompareGlobal && this.periodView)?this.showLastRec.createDelegate(this,[6]):this.showLastRec.createDelegate(this,[0]);               
                    leftGrid1.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(liability)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Debit)"))+ " ("+WtfGlobal.getCurrencyName()+") </b></b></div>";
                    leftGridSelectedCurr.header="<div align=right><b>"+(this.statementType=="BalanceSheet"?WtfGlobal.getLocaleText("acc.balanceSheet.Amount(liability)"):WtfGlobal.getLocaleText("acc.P&L.Amount(Debit)"))+ " ("+((currencyRec!=null&&currencyRec.data.currencyname!=undefined)?currencyRec.data.currencyname:WtfGlobal.getCurrencyName())+") </b></b></div>";
                }
                this.lGrid.getView().refresh(true);
                this.rGrid.getView().refresh(true);
            }
        }
      
         if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        var params={
         //   fordate:WtfGlobal.convertToGenericDate(this.forDate.getValue().add(Date.DAY,1)),
            stdate:this.sdate,
            enddate:this.edate,
            stpredate:this.presdateCompare,
            endpredate:this.preedateCompare,
            templatecode:templatecode,
            periodView:this.periodView
        }
        params.searchJson= this.searchJson,
        params.filterConjuctionCriteria= this.filterConjuctionCrit
        
        
        if(this.consolidateFlag && this.consolidateFlag==true) {
            params.companyids=companyids;
            params.gcurrencyid=gcurrencyid;
            params.userid=loginid;
        }
        
        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';
        if(this.statementType=="Trading") {
            params.mode=63;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getTradingMerged.do":"ACCReports/getTrading.do";
        }
        if(this.statementType=="ProfitAndLoss") {
            params.mode=64;
            params.nondeleted=true;
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getProfitLossMerged.do":"ACCReports/getProfitLoss.do";
        }
        if(this.statementType=="TradingAndProfitLoss" || this.statementType=="CostCenter") {
            var currencyFilterRec=WtfGlobal.searchRecord(this.currencyStoreCMB,this.currencyFilter.getValue(),'currencyid');
            params.mode=65;
            params.nondeleted=true;
            params.costcenter = this.costCenter.getValue();
            params.reportView = this.statementType;
            params.filterCurrency = this.currencyFilter.getValue();
            params.externalcurrencyrate = (currencyFilterRec!=null&&currencyFilterRec!=undefined)?currencyFilterRec.data.exchangerate:0; 
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getTradingAndProfitLossMerged.do":"ACCReports/getTradingAndProfitLoss.do";
        }
        if(this.statementType=="BalanceSheet") {
            params.mode=66;
            params.nondeleted=true;
            
            this.ajxUrl = this.consolidateFlag?"ACCCombineReports/getBalanceSheetMerged.do":"ACCReports/getBalanceSheet.do";
        }
//        this.expButton.setParams({
////                    accountid:this.accountID,
//                    stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//                    enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
//            });
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
    },

    successCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            this.lGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.field.Norecordstodisplay."));
            this.lGrid.getView().applyEmptyText();
            this.rGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.field.Norecordstodisplay."));
            this.rGrid.getView().applyEmptyText();
            
            this.total=(this.periodView)?response.data.periodtotal:response.data.total;
            if (response.data.pretotal != undefined) {
                this.total[2] = response.data.pretotal[0];
                this.total[3] = response.data.pretotal[1];
            } 
            if(response.data.opentotal != undefined){
                this.total[4] = response.data.opentotal[0];
                this.total[5] = response.data.opentotal[1];
            }
            if(response.data.endtotal != undefined){
                this.total[6] = response.data.endtotal[0];
                this.total[7] = response.data.endtotal[1];
            }
            this.lGrid.store.loadData(response.data);
            this.rGrid.store.loadData(response.data);
            this.doLayout();
//            if((this.statementType!="TradingAndProfitLoss" && this.total && this.total[0]==0 && this.total[1]==0)
//                ||(this.statementType=="TradingAndProfitLoss" && this.lGrid.store.getCount()<=3 && this.rGrid.store.getCount()<=3)){
//                    if(this.expButton)this.expButton.disable();
//                    if(this.printbtn)this.printbtn.disable();
//                }else{
//                    if(this.expButton)this.expButton.enable();
//                    if(this.printbtn)this.printbtn.enable();
//            }
            if (this.firstTime && !this.isCompareGlobal && (this.statementType == 'BalanceSheet'|| this.statementType=="TradingAndProfitLoss")) {
                var leftGrid = this.lGrid.colModel.config[5];
                var rightGrid = this.rGrid.colModel.config[5];
                var leftGridSelectedCurr = this.lGrid.colModel.config[6];
                var rightGridSelectedCurr = this.rGrid.colModel.config[6];
                var leftGrid1 = this.lGrid.colModel.config[4];
                var rightGrid1 = this.rGrid.colModel.config[4];
                var lopening = this.lGrid.colModel.config[2];
                var lperiod = this.rGrid.colModel.config[3];
                var rperiod = this.lGrid.colModel.config[3];
                var ropening = this.rGrid.colModel.config[2];               
                if (rightGrid != undefined){
                    if(this.periodView){
                        ropening.hidden=false;
                        rperiod.hidden=false;  
                        rightGrid.hidden = true;
                    }else{
                        ropening.hidden=true;
                        rperiod.hidden=true;
                        rightGrid.hidden = true;
                        rightGrid1.hidden = false;
                    }
                }
                if (leftGrid != undefined){
                    if(this.periodView){
                        lopening.hidden=false;
                        lperiod.hidden=false;
                        leftGrid.hidden = true;  
                    }else{
                        lopening.hidden=true;
                        lperiod.hidden=true;          
                        leftGrid.hidden = true;
                        leftGrid1.hidden = false;
                    }
                }
                if(this.currencyFilter.getValue()==WtfGlobal.getCurrencyID()){
                    leftGridSelectedCurr.hidden = true;
                    rightGridSelectedCurr.hidden = true;
                }else{
                    leftGridSelectedCurr.hidden = false;
                    rightGridSelectedCurr.hidden = false;
                }
                this.lGrid.getView().refresh(true);
                this.rGrid.getView().refresh(true);
            }
            this.firstTime = false;
            this.collapseGrids();
        }
        this.hideLoading();
    },

    failureCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.hideLoading();
    },

    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    
    getDates:function(start, savedSearchDate){ //this Works for both Start date and end date
        if(savedSearchDate != undefined){            
            return new Date(savedSearchDate);
        }
        var d=Wtf.serverDate;
//        if(this.statementType=='BalanceSheet'&&start)
//             return new Date('January 1, 1970 00:00:00 AM');
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
    
    getPreDates:function(start){      
        return start.add(Date.YEAR, -1);
    },

    showLastRec:function(pos){
        return WtfGlobal.currencySummaryRenderer(this.total[pos]);
    },
    
    swapGrids:function(){			// The summaryRenderer doesnt swap when the grids swap. Only the header and grid data swaps.
    	this.Lcm = this.lGrid.getColumnModel();
    	this.Rcm = this.rGrid.getColumnModel();
    	this.lstore1 = this.lGrid.getStore();
    	this.rstore1 = this.rGrid.getStore();
        
        //Swaping Totals from summary.
        var endingDataIndex = (this.statementType=="BalanceSheet" && !this.isCompareGlobal && this.periodView)?'endingamount':'amount';
        var leftOpen,leftPeriod,leftEnd,leftPreTotal;
        var rightOpen,rightPeriod,rightEnd,rightPreTotal;
        for (var indexCount = 0; indexCount < this.Lcm.getColumnCount(); indexCount++) {
            var tempDataIndex = this.Lcm.getDataIndex(indexCount);
            if (tempDataIndex === 'openingamount') {
                leftOpen = this.Lcm.config[indexCount];
            }
            if (tempDataIndex === 'periodamount') {
                leftPeriod = this.Lcm.config[indexCount];
            }
            if (tempDataIndex === endingDataIndex) {
                leftEnd = this.Lcm.config[indexCount];
            }
            if (tempDataIndex === "preamount") {
                leftPreTotal = this.Lcm.config[indexCount];
            }
        }
        for (indexCount = 0; indexCount < this.Rcm.getColumnCount(); indexCount++) {
            tempDataIndex = this.Rcm.getDataIndex(indexCount);
            if (tempDataIndex === 'openingamount') {
                rightOpen = this.Rcm.config[indexCount];
            }
            if (tempDataIndex === 'periodamount') {
                rightPeriod = this.Rcm.config[indexCount];
            }
            if (tempDataIndex === endingDataIndex) {
                rightEnd = this.Rcm.config[indexCount];
            }
            if (tempDataIndex === "preamount") {
                rightPreTotal = this.Rcm.config[indexCount];
            }
        }
        var temp = leftOpen.summaryRenderer;
        leftOpen.summaryRenderer= rightOpen.summaryRenderer;
        rightOpen.summaryRenderer = temp;
        
        temp = leftPeriod.summaryRenderer;
        leftPeriod.summaryRenderer= rightPeriod.summaryRenderer;
        rightPeriod.summaryRenderer = temp;
        
        temp = leftEnd.summaryRenderer;
        leftEnd.summaryRenderer= rightEnd.summaryRenderer;
        rightEnd.summaryRenderer = temp;
        
        temp = leftPreTotal.summaryRenderer;
        leftPreTotal.summaryRenderer= rightPreTotal.summaryRenderer;
        rightPreTotal.summaryRenderer = temp;
        
        this.lGrid.reconfigure(this.rstore1,this.Rcm);
    	this.rGrid.reconfigure(this.lstore1,this.Lcm);
        
    	if(this.toggle==0)
    		this.toggle=1;
    	else
    		this.toggle=0;

        if(this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")){
            this.expandCollapseGrid(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
        
    },
     showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
        
    },
    filterStore: function(json, filterConjuctionCriteria) {
        /**
         * ERP-33751 - Start Date Required for saved Search
         */        
        this.objsearchComponent.advGrid.sdate = this.startDate.getValue(); 
        this.objsearchComponent.advGrid.edate = this.endDate.getValue();
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.fetchStatement();
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.fetchStatement();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    } ,storeLoad: function() {
       this.fetchStatement();
        this.doLayout();


    } ,
    handleResetClickNew:function()
    {this.costCenter.reset();
           this.startDate.reset();
           this.endDate.reset();
           this.fetchStatement();
    }
});
