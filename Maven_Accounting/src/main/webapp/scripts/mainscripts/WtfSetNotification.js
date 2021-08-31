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

Wtf.notification = function(config) {
    Wtf.apply(this, config);
    this.recurringruleid="";
    this.groupingView = new Wtf.grid.GroupingView({
        forceFit: true
    });

    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: true
        });
        
    this.cm = new Wtf.grid.ColumnModel([
        {
            header: "",
            anchor:'40%',
            dataIndex: 'id',
            hidden:true
        },{
            header: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName"),
            anchor:'40%',
            dataIndex: 'modulename'
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.dependenton"),
            anchor:'40%',
            dataIndex: 'fieldname'
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.beforeafter"),
            anchor:'40%',
            dataIndex: 'beforeafter',
            renderer:this.typeRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.days"),
            anchor:'40%',
            dataIndex: 'days',
            renderer:this.daysRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.addressee"),
            anchor:'40%',
            dataIndex: 'users',
            renderer:this.addressRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.lp.emailreceiptid"),
            anchor:'40%',
            dataIndex: 'emailids',
//             renderer:this.sendCopyRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.lp.senderId"),
            anchor:'40%',
            dataIndex: 'senderid'
//             renderer:this.sendCopyRenderer
        }]);

    
    this.gridRecord = new Wtf.data.Record.create([{
        name: 'id' 
        },{
        name: 'module'
        },{
        name: 'modulename'
        },{
        name: 'fieldid'
        },{
        name: 'fieldname'
        },{
        name: 'islineitem'
        },{
        name: 'beforeafter'
        },{
        name: 'days'
        },{
        name: 'users' 
        },{
        name: 'userids' 
        },{
        name: 'emailids' 
        },{
        name: 'mailsubject' 
        },{
        name: 'mailcontent' 
        },{
        name: 'isMailToSalesPerson' 
        },{
        name: 'isMailToStoreManager' 
        },{
        name: 'mailToCreator' 
        },{
        name: 'mailtoassignedpersons' 
        },{
        name: 'recurringruleid' 
        },{
        name: 'ismailtoshippingemail' 
        },{
        name: 'templateid' 
        },{
        name: 'isMailToContactPerson' 
        },{
        name: 'senderid' 
        },{
        name: 'hyperlinkText' 
        }
    ]);
       
    this.gridReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:"count",
        remoteGroup:true,
        remoteSort: true
   }, this.gridRecord);
   
   this.gridGroupStore = new Wtf.data.GroupingStore({
    proxy: new Wtf.data.HttpProxy({
        url:"MailNotification/getMailNotificationData.do" 
    }),
    reader: this.gridReader,
    sortInfo: {
        field: 'modulename',
        direction: "ASC"        
    },
    baseParams: {
        companyid:companyid
    },
    groupField:'modulename'
});

this.grid=new Wtf.grid.GridPanel({
    id:'notification'+this.id,
    store: this.gridGroupStore,
    cm: this.cm,
    border: false,
    view: this.groupingView,
    sm: this.sm,
    trackMouseOver: true,
    loadMask: {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")
    }
});


this.userRecord = new Wtf.data.Record.create([{
      name: 'userid' 
      },{
      name: 'fullname'
      },{
      name: 'emailid'    
      },{
      name: 'rolename'    
      },{
      name: 'username'    
      }]);
  
this.userReader = new Wtf.data.KwlJsonReader({
     root: "data",
     totalProperty:"count"
}, this.userRecord);
    
this.userStore=new Wtf.data.Store({
    url:"ProfileHandler/getAllUserDetails.do",
    reader:this.userReader
});
this.userStore.load();

this.NewRuleBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.field.New"),
    iconCls :getButtonIconCls(Wtf.etype.add),
    tooltip :WtfGlobal.getLocaleText("acc.field.NewRule.ttip"),
    id: 'BtnNew1' + this.id
});

this.DeleteBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
    iconCls :getButtonIconCls(Wtf.etype.deletebutton),
    tooltip :WtfGlobal.getLocaleText("acc.field.DeleteRule.ttip"),
    id: 'BtnDel' + this.id,
    scope: this,
    disabled:true
});
this.submitBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.common.submit"),
    iconCls :getButtonIconCls(Wtf.etype.save),
    tooltip :WtfGlobal.getLocaleText("acc.field.SubmitRule.ttip"),
    id: 'BtnSubNew' + this.id,
    scope: this
});

this.EditBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.field.Update"),
    iconCls :getButtonIconCls(Wtf.etype.resetbutton),
    tooltip :WtfGlobal.getLocaleText("acc.field.UpdateRule.ttip"),
    scope: this,
    disabled:true
});
this.moduleStore=new Wtf.data.SimpleStore({
     fields:[{name:"id"},{name:"name"}],
     data:[
         [Wtf.Acc_Purchase_Requisition_ModuleId,"Purchase Requisition"],
         [Wtf.Acc_Vendor_Invoice_ModuleId,"Purchase Invoice"],
         [Wtf.Acc_Invoice_ModuleId,"Sales Invoice"],
         [Wtf.Acc_GENERAL_LEDGER_ModuleId,"Journal Entry"],
         [Wtf.Acc_Sales_Order_ModuleId,"Sales Order"],
         [Wtf.Acc_Purchase_Order_ModuleId,"Purchase Order"],
         [Wtf.Acc_Delivery_Order_ModuleId,"Delivery Order"],
         [Wtf.Acc_Goods_Receipt_ModuleId,"Goods Receipt Order"],
         [Wtf.Acc_Sales_Return_ModuleId,"Sales Return"],
         [Wtf.Acc_Purchase_Return_ModuleId,"Purchase Return"],
         [Wtf.Acc_Customer_ModuleId,"Customer"],
         [Wtf.Acc_Vendor_ModuleId,"Vendor"],
         [Wtf.Acc_Contract_ModuleId,"Contract"],
         [Wtf.Asset_Maintenance_ModuleId,"Asset Maintenance"],
         [Wtf.Acc_Product_Master_ModuleId,"Product"],
         [Wtf.Consignment_Sales_ModuleId,"Consignment Sales"],
         [Wtf.Consignment_Purchase_ModuleId,"Consignment Purchase"],
         [Wtf.Account_Statement_ModuleId,"Account Statement"],
         [Wtf.Acc_Make_Payment_ModuleId,"Make Payment"],
         [Wtf.Acc_Receive_Payment_ModuleId,"Receive Payment"],
         [Wtf.Acc_Customer_Quotation_ModuleId,"Customer Quotation"],
         [Wtf.Acc_Vendor_Quotation_ModuleId,"Vendor Quotation"]
         
         
     ]
});
this.moduleType= new Wtf.form.ComboBox({
     fieldLabel: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName") + '*',
     labelStyle:'width:120px;margin-left: 5px;',
     name:'modules',
     hiddenName:'modules',
     store:this.moduleStore,
     valueField:'id',
     displayField:'name',
     mode: 'local',
     disableKeyFilter:true,
     allowBlank:false,
     triggerAction:'all',
     forceSelection:true,
     typeAhead: true,
     emptyText:WtfGlobal.getLocaleText("acc.field.SelectaModule")
});
this.moduleType.on('select',this.onModuleSelect,this);
 
this.fieldRec = new Wtf.data.Record.create([
            {name: 'fieldid'},
            {name: 'fieldlabel'},
            {name: 'islineitem'}
        ]);
this.fieldds = new Wtf.data.Store({
    reader: new Wtf.data.KwlJsonReader({
        root: "data"
    },this.fieldRec),
    url : "ACCAccountCMN/getGlobalCustomDateFields.do",
    baseParams:{
        moduleid:0
    }
 });         

this.field= new Wtf.form.ComboBox({
     fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.dependenton") + '*',
     labelStyle:'width:120px;margin-left: 5px;',
     name:'fieldid',
     hiddenName:'fieldid',
     store:this.fieldds,
     valueField:'fieldid',
     displayField:'fieldlabel',
     mode: 'local',
     disableKeyFilter:true,
     allowBlank:false,
     triggerAction:'all',
     forceSelection:true,
     typeAhead: true,
     disabled : true,
     emptyText:WtfGlobal.getLocaleText("acc.field.Selectafield")
});

this.field.on("select",this.onFieldSelect, this);
  
this.orderStore=new Wtf.data.SimpleStore({
    fields:[{name:"id"},{name:"name"}],
    data:[[0,"Before Date"],[1,"On Date"],[2,"After Date"]]
});

this.beforeAfter= new Wtf.form.ComboBox({
    fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.beforeafter") + '*',
    labelStyle:'width:120px;margin-left: 5px;',
    name:'beforeafter',
    hiddenName:'beforeafter',
    store:this.orderStore,
    valueField:'id',
    displayField:'name',
    mode: 'local',
    disableKeyFilter:true,
    allowBlank:false,
    triggerAction:'all',
    forceSelection:true,
    typeAhead: true,
    emptyText:WtfGlobal.getLocaleText("acc.field.SelectyourPreference")
});
                
this.days = new Wtf.form.NumberField({
    fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.days") + '*', 
    name:'commission',                
//    width:400,                         
    allowNegative:false,
    allowDecimals:false,
    labelStyle:'width:120px;margin-left: 5px;',
    allowBlank:false,
    minValue : 0,
    maxValue : 365,
    disabled :false,
    emptyText :WtfGlobal.getLocaleText("acc.field.EnterDays")
});

this.repeatNotification = new Wtf.form.Checkbox({
    fieldLabel:WtfGlobal.getLocaleText("acc.mailconfiguration.RepeatNotification"),
    labelStyle:'width:120px;margin-left: 5px;',
    name:'repeatNotification',
    cls : 'custcheckbox',
    width: 10,
    checked: false,
    id:'repeatNotification'
});

this.repeatNotification.on('change',this.callRecurrigReminderWindow,this);
    
this.emailIDs = new Wtf.form.TextField({
    fieldLabel : WtfGlobal.getLocaleText("acc.lp.emailreceiptid"),
    labelStyle:'width:120px;margin-left: 5px;',
    allowBlank: true,
    emptyText : WtfGlobal.getLocaleText("acc.field.Givecomma,aftereachemailaddresstosendmailtoseverals"),
    maxLength:250,
    validator:WtfGlobal.validateMultipleEmail
});
this.senderId = new Wtf.form.TextField({
    fieldLabel : WtfGlobal.getLocaleText("acc.lp.senderId"),
    labelStyle:'width:120px;margin-left: 5px;',
    allowBlank: true,
    emptyText : WtfGlobal.getLocaleText("acc.lp.senderId"),
    maxLength:250,
    validator:WtfGlobal.validateMultipleEmail
});

this.isMailToSalesPerson = new Wtf.form.Checkbox({
    fieldLabel:WtfGlobal.getLocaleText("acc.field.isMailToSalesPerson"),
    labelStyle:'width:120px;margin-left: 5px;',
    name:'ismailtosalesperson',
    cls : 'custcheckbox',
    width: 10,
    id:'ismailtosalespersonid'
});

this.isMailToContactPerson = new Wtf.form.Checkbox({
    fieldLabel:"Is Mail To Contact Person",
    labelStyle:'width:120px;margin-left: 5px;',
    name:'ismailtocontactperson',
    cls : 'custcheckbox',
    width: 10,
    id:'ismailtocontactpersonid'
});

this.SendMailToStoreManager = new Wtf.form.Checkbox({
    fieldLabel: WtfGlobal.getLocaleText("acc.field.isMailToStoreManager"),
    labelStyle: 'width:120px;margin-left: 5px;margin-top: 7px',
    name: 'ismailtostoremanager',
    cls: 'custcheckbox',
    id: 'ismailtostoremanagerid'
});

this.isMailToAssignedPerson = new Wtf.form.Checkbox({
    fieldLabel:WtfGlobal.getLocaleText("acc.field.isMailToAssignedPerson"),//Is Mail To Assigned Person
    labelStyle:'width:120px;margin-left: 5px;',
    name:'ismailtoassignedperson',
    cls : 'custcheckbox',
    width: 10,
    id:'ismailtoassignedpersonid'
});
this.SendMailToCreator = new Wtf.form.Checkbox({
    fieldLabel:WtfGlobal.getLocaleText("acc.field.Creator"),
   labelStyle:'width:120px;margin-left: 5px;margin-top: 7px',
    name:'ismailtocreator',
    cls : 'addresseecheckbox',
    disabled:true,
    id:'ismailtocreator'
});

this.SendMailToShippingEmail = new Wtf.form.Checkbox({
    fieldLabel: WtfGlobal.getLocaleText("acc.field.sendMailToShippingEmail")+WtfGlobal.addLabelHelp("<ul style='list-style-type:disc; margin-left:10px;'><li>If check is ON then mail will be send to transaction shipping address email.</li><li>If check is OFF then mail will be send to transaction billing address email.</li></ul>"),//Send Mail To Shipping Address Email
    labelStyle: 'width:120px;margin-left: 5px;margin-top: 7px',
    name: 'sendmailtoshippingemail',
    cls: 'custcheckbox',
    id: 'sendmailtoshippingemailid'
});

this.SendmailToselectedPersons = new Wtf.form.Checkbox({
    fieldLabel:WtfGlobal.getLocaleText("acc.field.addresseeByUser"),//Is Mail To Assigned Person
   labelStyle:'width:120px;margin-left: 5px;margin-top: 7px;',
    name:'ismailtoselectedusers',
    cls : 'addresseecheckbox',
    disabled:true,
    id:'ismailtoselectedusers'
});
this.SendmailToselectedPersons.on('change', this.enabledisableAddresseeCombo, this);


    this.sendMailToFieldSet = new Wtf.form.FieldSet({
        anchor: '90%',
        height: 90,
        hidden:true,
        cls: 'fieldSetWidth', //fieldset was not taking width so i have written new css class
        title: WtfGlobal.getLocaleText("acc.fields.mailconfiguration.sendmailto.fieldset"),
        items: [
            this.SendMailToCreator, this.SendmailToselectedPersons
        ]
    });
        
                            
this.selectedmailsubject=false;
this.mailSubject = new Wtf.form.TextField({
    fieldLabel : WtfGlobal.getLocaleText("acc.field.MailSubject"),
    allowBlank: true,
    maxLength:250
//    height:'35px',
});
 this.mailSubject.on('focus',this.setselectedflag,this);
 
this.Message=new Wtf.newHTMLEditor({
    name:"message",
    allowBlank:false,
    fieldLabel:WtfGlobal.getLocaleText("acc.field.MailBody"),
//    xtype:'htmleditor',
    height: 160,
    border: false,
    autoScroll : true
 });
 this.Message.on('activate',this.enablebuttonatcursor,this);
 
this.users=new Wtf.common.Select({
        labelStyle:'width:120px;margin-left: 5px;',
        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.addressee") + '*',
        multiSelect:true,
        forceSelection:true,
        name:'users',
        xtype:'select',
        hiddenName:"users",
        valueField:'userid',
        displayField:'fullname',
        store:this.userStore,
        emptyText:WtfGlobal.getLocaleText("acc.field.SelectAddressee"),
        mode: 'local',
        triggerAction:'all',
        typeAhead: true,
        scope:this
//        allowBlank:false
});
    this.templateRecord = new Wtf.data.Record.create([
    {
        name: 'templateid'
    },

    {
        name: 'templatename'
    }
    ]);
    this.templateStore = new Wtf.data.Store({
        record : this.templateRecord
    });
    this.templateCombo = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.sendDDtemplate.email.select.template")+"*",
        emptyText:"Select Template",
        valueField:'templateid',
        displayField:'templatename',
        store:this.templateStore,
        width:200,
        scope:this,
        labelStyle:'width:120px;margin-left: 5px;',
//        hidden: true,
//        hideLabel:true,
        typeAhead: true,
        forceSelection: true,
        name:'templateid',
        hiddenName:'templateid',
        allowBlank:(Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink) ?false:true
    });
    
    this.hyperlinkText = new Wtf.form.TextField({
        fieldLabel : WtfGlobal.getLocaleText("acc.field.hyperlinkText")+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.hyperlinkText.helptext")),
        labelStyle: 'width:120px;margin-left: 5px;',
        allowBlank: true,
        emptyText : WtfGlobal.getLocaleText("acc.field.hyperlinkText"),
        maxLength: 250
    });
    this.templateCombo.on("select",this.onTemplateComboSelect, this);
    
    this.mailsubjectjson=[]; 
    this.mailbodyjson=[];
    this.selectFieldsStore = new Wtf.data.SimpleStore({
        fields: ['id','label','dbcolumnname','reftablename','reftablefk','reftabledatacolumn','dummyvalue','xtype','customfield'],
        data : [
        ["","","","","","","","",""]
        ]
    });    
 
 /*Select Placeholders*/
    this.selectField = new Wtf.form.ExtFnComboBox({
        fieldLabel:"Select Placeholder Fields",
        labelStyle:'margin-left: 85px;width:150px;',
        name:'id',
        width:380,
        ctCls:'selectfield',
        displayField:'label',
        valueField:'id',
        mode: 'local',
        store:this.selectFieldsStore,
        disabled:true,
        emptyText:WtfGlobal.getLocaleText("acc.field.Selectafield"),
        selectOnFocus:true,
        triggerAction: 'all',
        extraFields: "",
        mailnotification:true,
        listWidth: 500
    });
    
     /*Insert Button*/
    this.insertbutton= new Wtf.Button({
        border:false,
        minWidth:100,
        disabled:true,
        text:WtfGlobal.getLocaleText("acc.template.insertbtn"),//"Insert",
        scope:this,
        hidden:true,
        handler:function(){
            this.insertFieldinHtmlEditor();
            this.selectField.reset();
        }
    });

Wtf.notification.superclass.constructor.call(this,{
    autoDestroy:true,
    border: false,
    layout :'border',
    items:[{
        title : WtfGlobal.getLocaleText("acc.field.Rule"),
        paging : false,
        autoLoad : false,
        region:"north",
        height:450,
        bodyStyle : "background:#f0f0f0;",
        border: false,
        layout:"fit",
        items: [{
                    border:false,
                    layout:'column',
                    bodyStyle:'padding:13px 13px 13px 13px',
                    labelWidth:140,
                    autoScroll: true,
                    items: [{
                        layout:'form',
                        columnWidth:0.30,
                        border:false,
                        labelWidth:120,
                        defaults : {
                         anchor:'90%'
                        },
                        items:[this.moduleType,this.field,this.beforeAfter,this.days,this.sendMailToFieldSet,this.users,this.templateCombo,this.hyperlinkText, this.repeatNotification,this.isMailToSalesPerson,this.isMailToContactPerson,this.SendMailToStoreManager,this.isMailToAssignedPerson,this.SendMailToShippingEmail, this.emailIDs,this.senderId, {
                            xtype: 'panel',
                            border: false,
                            cls: 'emailfieldInfo',
                            html: WtfGlobal.getLocaleText("acc.field.Givecomma,aftereachemailaddresstosendmailtoseverals")
                        }]
                    },{
                        layout:'form',
                        columnWidth:0.70,
                        border:false,
                        labelWidth:80,
                        defaults : {
                            anchor:'99%'
                        },
                    items:[this.mailSubject, this.Message,
                    {
                        layout:'column',
                        border:false,
                        defaults:{
                            border:false
                        },
                        items:[ {
                            layout:'form',
                            border:false,
                            width:'650px',
                            items:this.selectField
                        },{
                            layout:'form',
                            border:false,
                            bodyStyle:"padding-left:10px;margin-top:10px;", 
                            items:[this.insertbutton]
                        }]
                    }]
                }],
                bbar:[this.NewRuleBttn,this.DeleteBttn,this.submitBttn,this.EditBttn]
            }]
        },{
            title : WtfGlobal.getLocaleText("acc.field.NotificationRules"),
            paging : false,
            autoLoad : false,
            region:'center',
            layout:'fit',
            items:this.grid,
            bbar:this.pg = new Wtf.PagingSearchToolbar({
                id: 'pgTbarModule' + this.id,
                pageSize: 15,
                store: this.gridGroupStore,
                displayInfo: true,
//                displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP3 = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
      }]
 });     
 

this.gridGroupStore.on("datachanged",function() {
    var p = this.pP3.combo.value;
}, this);

    this.gridGroupStore.on('beforeload', function(s, o) {
        if (this.pP3 != undefined && this.pP3.combo!=undefined) {
            if (this.pP3.combo.value == "All") {
                var count = this.gridGroupStore.getTotalCount();
                var rem = count % 5;
                if (rem == 0) {
                    count = count;
                } else {
                    count = count + (5 - rem);
                }
                o.params.limit = count;
            }
        }
    }, this);

this.gridGroupStore.on("load",function(store){
    var p = this.pP3.combo.value;
    this.loadMask1.hide();
},this);    

this.gridGroupStore.on('loadexception',function(){
                   this.loadMask1.hide();
     },this);
     
 Wtf.getCmp('notification'+this.id).on("render",function(){
            this.loadMask1 = new Wtf.LoadMask(Wtf.getCmp('notification'+this.id).el.dom, Wtf.apply(this.loadMask1));
            this.loadMask1.show();
 },this);    

this.on('render',this.handleRender,this);
this.on('show',this.handleshow,this); 
this.grid.on("rowclick", this.rowClickHandle, this);
this.beforeAfter.on('select',this.showDayField,this);
this.submitBttn.on('click',this.submit,this);
this.NewRuleBttn.on('click',this.NewRule,this);
this.EditBttn.on('click',this.editData,this);
this.DeleteBttn.on('click',this.deleteData,this);

this.selectField.on("render", function() {//on render show and hide the component
    WtfGlobal.hideFormElement(this.selectField);
}, this);

}

Wtf.extend(Wtf.notification, Wtf.Panel,{
    handleshow:function(){
       this.items.items[0].ownerCt.doLayout();
    },
    
    onModuleSelect : function() {
        this.field.setValue('');
        this.fieldds.baseParams['moduleid'] = this.moduleType.getValue();
        this.fieldds.load();
        this.field.enable();
        this.days.enable();
        this.beforeAfter.setValue("");
        this.beforeAfter.clearValue();
        this.field.clearValue();
        this.emailIDs.setValue("");
        this.senderId.setValue("");
        this.mailSubject.setValue("");
        this.Message.setValue("");
        this.days.reset();
        this.users.reset();
        this.disablebuttons();//Disabling the buttons when modulename button is clicked.
        this.SendMailToCreator.reset();
        this.SendmailToselectedPersons.reset();
        this.SendMailToCreator.disable();
        this.SendmailToselectedPersons.disable();
        this.isMailToSalesPerson.reset();
        this.SendMailToStoreManager.reset();
        this.SendMailToShippingEmail.reset();
        this.sendMailToFieldSet.hide();
        this.isMailToContactPerson.reset();
        this.hyperlinkText.reset();
        WtfGlobal.hideFormElement(this.isMailToSalesPerson);
        WtfGlobal.hideFormElement(this.isMailToContactPerson);
        WtfGlobal.hideFormElement(this.SendMailToStoreManager);
        WtfGlobal.hideFormElement(this.SendMailToShippingEmail);
        WtfGlobal.hideFormElement(this.hyperlinkText);
            
        this.isMailToAssignedPerson.reset();
        WtfGlobal.hideFormElement(this.isMailToAssignedPerson);
        if(this.moduleType.getValue() == Wtf.Acc_Contract_ModuleId || this.moduleType.getValue() == Wtf.Consignment_Sales_ModuleId 
                || this.moduleType.getValue() == Wtf.Consignment_Purchase_ModuleId) {
            WtfGlobal.showFormElement(this.isMailToSalesPerson);
            this.isMailToSalesPerson.reset();
            WtfGlobal.showFormElement(this.isMailToContactPerson);
            this.isMailToContactPerson.reset();
        } else if(this.moduleType.getValue() == Wtf.Asset_Maintenance_ModuleId) {
            WtfGlobal.showFormElement(this.isMailToAssignedPerson);
            this.isMailToAssignedPerson.reset();
        }
        if(this.moduleType.getValue() == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleType.getValue() == Wtf.Acc_Invoice_ModuleId){
            WtfGlobal.showFormElement(this.repeatNotification);   
        } else {
            this.repeatNotification.setValue(false);
            WtfGlobal.hideFormElement(this.repeatNotification);   
        }
        var moduleid = (this.moduleType && this.moduleType.getValue())?this.moduleType.getValue():"";
        var allowDDTemplate = (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Vendor_Invoice_ModuleId 
                            || moduleid == Wtf.Acc_Sales_Order_ModuleId || moduleid == Wtf.Acc_Purchase_Order_ModuleId  
                            || moduleid == Wtf.Acc_Delivery_Order_ModuleId || moduleid == Wtf.Acc_Goods_Receipt_ModuleId
                            || moduleid == Wtf.Acc_Sales_Return_ModuleId || moduleid == Wtf.Acc_Purchase_Return_ModuleId
                            || moduleid == Wtf.Acc_Make_Payment_ModuleId || moduleid == Wtf.Acc_Receive_Payment_ModuleId
                            || moduleid == Wtf.Acc_Customer_Quotation_ModuleId || moduleid == Wtf.Acc_Vendor_Quotation_ModuleId
                            ||moduleid == Wtf.Acc_Purchase_Requisition_ModuleId) 
                            && (Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink)?true:false;
        if(allowDDTemplate){
            this.templateStore.removeAll();
            this.templateCombo.reset();
            WtfGlobal.showFormElement(this.templateCombo);   
            var colModArray = GlobalCustomTemplateList[this.moduleType.getValue()];
            var isTflag=colModArray!=undefined && colModArray.length>0?true:false;
            if(isTflag){
                var none  = new this.templateRecord({
                    templateid :"None",
                    templatename : "None"
                });
                this.templateStore.add(none);
                for (var count = 0; count < colModArray.length; count++) {
                    var id1=colModArray[count].templateid;
                    var name1=colModArray[count].templatename;
                    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && !addTemplate(id1)){
                        continue;
                    }
                    var record1  = new this.templateRecord({
                        templateid :id1,
                        templatename : name1
                    });
                    this.templateStore.add(record1);
                    if(colModArray.length == 1){
                        this.templateCombo.setValue(id1);
                        this.hyperlinkText.setValue(name1);
                    }
                }
            }
        } else{
            if(this.templateCombo){
                this.templateStore.removeAll();
                this.templateCombo.reset();
                this.templateCombo.allowBlank= true;
                WtfGlobal.hideFormElement(this.templateCombo);   
            }   
        }
    },
    enablebuttons: function() {
        this.selectField.enable();
        this.insertbutton.enable();
    },
    disablebuttons: function() {
        this.selectField.disable();
        this.insertbutton.disable();
    },
    enablebuttonatcursor : function() {//Enable select placeholder field
        var fieldVal = this.field.getValue();
        if ((this.moduleid == Wtf.Acc_Purchase_Order_ModuleId ||
                this.moduleid == Wtf.Acc_Sales_Order_ModuleId ||
                this.moduleid == Wtf.Acc_Make_Payment_ModuleId ||
                this.moduleid == Wtf.Acc_Receive_Payment_ModuleId ||
                this.moduleid == Wtf.Acc_Invoice_ModuleId ||
                this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ||
                this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId ||
                this.moduleid == Wtf.Acc_Delivery_Order_ModuleId||
                this.moduleid==Wtf.Acc_Sales_Return_ModuleId || 
                this.moduleid==Wtf.Acc_Purchase_Return_ModuleId ||
                this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId || 
                this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId ||
                this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId) && fieldVal === Wtf.Email_Button_From_Report) {
            this.selectedmailsubject = false;
            this.enablebuttons();
        }
    }, 
    setselectedflag : function() {
         var fieldVal = this.field.getValue();
       if((this.moduleid== Wtf.Acc_Purchase_Order_ModuleId ||
           this.moduleid == Wtf.Acc_Sales_Order_ModuleId ||
           this.moduleid == Wtf.Acc_Make_Payment_ModuleId ||
           this.moduleid == Wtf.Acc_Receive_Payment_ModuleId ||
           this.moduleid == Wtf.Acc_Invoice_ModuleId ||
           this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ||
           this.moduleid == Wtf.Acc_Delivery_Order_ModuleId ||
           this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId ||
           this.moduleid==Wtf.Acc_Sales_Return_ModuleId || 
           this.moduleid==Wtf.Acc_Purchase_Return_ModuleId ||
           this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId || 
           this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId ||
           this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId) && fieldVal === Wtf.Email_Button_From_Report){
            this.selectedmailsubject=true;
            this.enablebuttons();
            this.Message.activated=false;
        }
    }, 
    
    createjson : function(obj,bodyflag) {
        var jsonobj=[];
        if(bodyflag){//for mailbody section
            var arr = obj.match(/\{PLACEHOLDER:(.*?)}/g);
            if(arr){
                for(var j=0;j<arr.length;j++){
                    var jsonData={};
                    var matches = arr[j].replace(/\{|\}/gi, '').split(":");
                    jsonData['placeholder'] = matches[1];
                    var rec =  WtfGlobal.searchRecord(this.selectFieldsStore, matches[1], 'id');
                    if (rec) {
                        var recdata = rec.data;
                        jsonData['reftablename'] = recdata.reftablename;
                        jsonData['reftablefk'] = recdata.reftablefk;
                        jsonData['reftabledatacolumn'] = recdata.reftabledatacolumn;
                        jsonData['dbcolumnname'] = recdata.dbcolumnname;
                        jsonData['fieldid'] = recdata.id;
                        jsonData['label'] = recdata.label;
                        jsonData['xtype'] = recdata.xtype;
                        jsonData['customfield'] = recdata.customfield;
                        jsonData["label"] = recdata.label;
                        jsonobj.push(jsonData);
                    }//end of rec
                } //end of for loop
            }//end of array
        }else{//for subject section
             var arr = obj.split("#");
            if(arr){
                for(var j=0;j<arr.length;j++){
                    var jsonData={};
                    var rec = WtfGlobal.searchRecord(this.selectFieldsStore, arr[j], 'label');
                    if (rec) {
                        var recdata = rec.data;
                        jsonData['reftablename'] = recdata.reftablename;
                        jsonData['reftablefk'] = recdata.reftablefk;
                        jsonData['reftabledatacolumn'] = recdata.reftabledatacolumn;
                        jsonData['dbcolumnname'] = recdata.dbcolumnname;
                        jsonData['fieldid'] = recdata.id;
                        jsonData['label'] = recdata.label;
                        jsonData['xtype'] = recdata.xtype;
                        jsonData['customfield'] = recdata.customfield;
                        jsonData["label"] = recdata.label;
                        jsonData['placeholder'] = recdata.id;
                        jsonobj.push(jsonData);
                    }//end of rec
                } //end of for loop
            }//end of array
        }
        return jsonobj;
    }, 
    insertFieldinHtmlEditor : function() {
        var fieldid=this.selectField.getValue();
        if(fieldid!='NA'){//If fieldid is not NA
            var rec =  WtfGlobal.searchRecord(this.selectFieldsStore, fieldid, 'id');
            if (rec) {
                var recdata = rec.data;
                var labelvalue=recdata.label;
                if(this.selectedmailsubject){
                    var label="#"+ labelvalue + "#";
                    var mailsubjectvalue=this.mailSubject.getValue()+" "+label;
                    this.mailSubject.setValue(mailsubjectvalue); 
                }else{
                    var label="<span attribute='{PLACEHOLDER:"+fieldid+"}'>#"+ labelvalue + "#</span>";
                    this.Message.insertAtCursor(label);
                }
            }
        }
    },  
    setGlobalStore : function(moduleid) {//Selecting Default Fields
        Wtf.Ajax.requestEx({
            url: "MailNotify/getSelectFieldPlaceholderswithCategories.do",
            method: 'POST',
            params: {
                moduleid: moduleid
            }
        },this, function(response){
            if (response.success) {
                var resData = response.defaultfield.data;
                var arr2 = [];
                for (var i = 0; i < resData.length; i++) {
                    if(resData[i]!=undefined && resData[i].data!=undefined && resData[i]!='undefined' && resData[i].data!='undefined'){
                        for (var j = 0; j < resData[i].data.length; j++) {
                            var data = resData[i].data[j];
                            var header="";
                            if(data.id=='NA'){
                                var header = headerCheck(WtfGlobal.HTMLStripper(data.label));
                                header = header.replace("*", "");
                                header = header.trim();
                            }else{
                                header=data.label;
                            }
                            var arr1 = [data.id,header,data.dbcolumnname,data.reftablename,data.reftablefk,data.reftabledatacolumn,data.dummyvalue,data.xtype,data.customfield]
                            arr2.push(arr1);
                        }
                    }
                }
                this.selectFieldsStore.loadData(arr2);
            }
        });
    },       
    onFieldSelect : function() {
        var moduleID = this.moduleType.getValue();
        var record = this.moduleStore.queryBy(function(record){
                return (record.get('id') == moduleID);
            }, this).items[0];
        var moduleData = record.data;
        var moduleName = moduleData.name
        var fieldVal = this.field.getValue();
        this.beforeAfter.enable();
        this.disablebuttons();//Disabling the buttons when dependentupon button is clicked.
        this.moduleid=moduleData.id;
        this.Message.activated=false;
        WtfGlobal.hideFormElement(this.selectField);
        this.insertbutton.hide();
        this.SendMailToShippingEmail.reset();
        this.hyperlinkText.reset();
        WtfGlobal.hideFormElement(this.SendMailToShippingEmail);
        WtfGlobal.hideFormElement(this.hyperlinkText);
        
        if(fieldVal == "18" || fieldVal == "19" || fieldVal == "13" || moduleID == Wtf.Consignment_Sales_ModuleId || moduleID == Wtf.Consignment_Purchase_ModuleId){
            WtfGlobal.showFormElement(this.isMailToSalesPerson);
            this.isMailToSalesPerson.reset();
             WtfGlobal.showFormElement(this.isMailToContactPerson);
            this.isMailToContactPerson.reset();
            if(fieldVal != "35"){
            WtfGlobal.showFormElement(this.SendMailToStoreManager);
            this.SendMailToStoreManager.reset();
        }
        }else{
            WtfGlobal.hideFormElement(this.isMailToSalesPerson); 
            this.isMailToSalesPerson.reset();
            WtfGlobal.hideFormElement(this.isMailToContactPerson); 
            this.isMailToContactPerson.reset();
            WtfGlobal.hideFormElement(this.SendMailToStoreManager); 
            this.SendMailToStoreManager.reset();
        }
        
        if(fieldVal === Wtf.Email_Button_From_Report){
            if(moduleData.id == Wtf.Acc_Purchase_Order_ModuleId||
               moduleData.id == Wtf.Acc_Sales_Order_ModuleId ||
               moduleData.id == Wtf.Acc_Invoice_ModuleId || 
               moduleData.id == Wtf.Acc_Vendor_Invoice_ModuleId ||
               moduleData.id == Wtf.Acc_Delivery_Order_ModuleId ||
               moduleData.id == Wtf.Acc_Goods_Receipt_ModuleId ||
               moduleData.id ==Wtf.Acc_Sales_Return_ModuleId || 
               moduleData.id ==Wtf.Acc_Purchase_Return_ModuleId ||
               moduleData.id ==Wtf.Acc_Customer_Quotation_ModuleId || 
               moduleData.id ==Wtf.Acc_Vendor_Quotation_ModuleId ||
               moduleData.id ==Wtf.Acc_Purchase_Requisition_ModuleId){
               this.emailIDs.getEl().up('.x-form-item').setDisplayed(true);
            } else {
                this.emailIDs.getEl().up('.x-form-item').setDisplayed(false);
            } 
        }
        if(fieldVal === Wtf.Email_Button_From_Report){//20
            this.EnableDisableButton();
            this.users.reset();
            if(moduleData.id == Wtf.Acc_Purchase_Order_ModuleId||
               moduleData.id == Wtf.Acc_Sales_Order_ModuleId ||
               moduleData.id == Wtf.Acc_Make_Payment_ModuleId ||
               moduleData.id == Wtf.Acc_Receive_Payment_ModuleId||
               moduleData.id == Wtf.Acc_Invoice_ModuleId || 
               moduleData.id == Wtf.Acc_Vendor_Invoice_ModuleId ||
               moduleData.id == Wtf.Acc_Delivery_Order_ModuleId ||
               moduleData.id == Wtf.Acc_Goods_Receipt_ModuleId ||
               moduleData.id ==Wtf.Acc_Sales_Return_ModuleId || 
               moduleData.id ==Wtf.Acc_Purchase_Return_ModuleId ||
               moduleData.id ==Wtf.Acc_Customer_Quotation_ModuleId || 
               moduleData.id ==Wtf.Acc_Vendor_Quotation_ModuleId ||
               moduleData.id ==Wtf.Acc_Purchase_Requisition_ModuleId){
                WtfGlobal.showFormElement(this.selectField);
                this.insertbutton.show();
                this.setGlobalStore(moduleData.id);//assigning default values to store
                
               WtfGlobal.showFormElement(this.SendMailToShippingEmail); 
               this.SendMailToShippingEmail.reset();
               
               if(this.templateCombo.lastSelectionText !== "None" && this.templateCombo.lastSelectionText !== "" ){
                    WtfGlobal.showFormElement(this.hyperlinkText);
                    this.hyperlinkText.setValue(this.templateCombo.lastSelectionText);
               }
            }
        }else if(fieldVal === Wtf.APPROVAL_EMAIL || fieldVal === Wtf.REJECTION_EMAIL ){
             this.sendMailToFieldSet.show();
            this.EnableDisableButton();
            this.SendMailToCreator.setValue(false);
            this.SendMailToCreator.setValue(false);
             this.users.reset();
             this.SendMailToCreator.enable();
             this.SendmailToselectedPersons.enable();
            this.SendMailToCreator.show();
            this.SendmailToselectedPersons.show();
             this.emailIDs.setValue("");
        }else{
            this.sendMailToFieldSet.hide();
            this.days.enable();
            this.beforeAfter.enable();
            this.emailIDs.enable();
            this.isMailToAssignedPerson.enable();
            this.isMailToSalesPerson.enable();
             if (fieldVal === '35') {
                this.isMailToContactPerson.enable();
            }
            this.SendMailToStoreManager.enable();
            this.users.enable();
            this.users.reset();
            
            this.SendMailToCreator.setValue(false);
            this.SendmailToselectedPersons.setValue(false);
            
            this.SendMailToCreator.disable();
            this.SendmailToselectedPersons.disable();
        }
        
        if((moduleID == Wtf.Consignment_Sales_ModuleId && fieldVal !='35') || moduleID == Wtf.Consignment_Purchase_ModuleId){
            this.beforeAfter.setValue(1);
            this.beforeAfter.disable();
            this.days.disable();
            this.days.setValue(0);
        }
        
        if(moduleData.id == Wtf.Acc_Invoice_ModuleId || moduleData.id == Wtf.Acc_Vendor_Invoice_ModuleId){
            if(fieldVal === Wtf.Email_Button_From_Report){
                this.repeatNotification.setValue(false);
                WtfGlobal.hideFormElement(this.repeatNotification);   
            } else {
                WtfGlobal.showFormElement(this.repeatNotification);   
            }
        }
        var record = this.fieldds.queryBy(function(record){
                return (record.get('fieldid') == fieldVal);
            }, this).items[0];
        var mailContent = "", mailsubject = WtfGlobal.getLocaleText("acc.field.DeskeraERP")+ record.data.fieldlabel + " " + WtfGlobal.getLocaleText("acc.field.Notification");
        var fieldid='';
        if (fieldVal == Wtf.Email_Button_From_Report) {
            fieldid = Wtf.Email_Button_From_Report;
        } else if (fieldVal == Wtf.APPROVAL_EMAIL) {
            fieldid = Wtf.APPROVAL_EMAIL;
        }else if (fieldVal == Wtf.REJECTION_EMAIL) {
            fieldid =Wtf.REJECTION_EMAIL;
        }
        
        if(record.data.islineitem) {
            mailContent = WtfGlobal.getLocaleText("acc.field.HellobrWehaveenclosedyour")+moduleName+WtfGlobal.getLocaleText("acc.field.detailstodaydated#Date_Today#for")+companyName+WtfGlobal.getLocaleText("acc.field.company")+"<br/><br/>\n\ "+
            WtfGlobal.getLocaleText("acc.field.Foryourkindinformationthe")+record.data.fieldlabel+WtfGlobal.getLocaleText("acc.field.dateofsomeproductsinyour")+moduleName+WtfGlobal.getLocaleText("acc.field.sis#Date_Val#");
            this.Message.setValue(mailContent);
            this.mailSubject.setValue(mailsubject);
        } else {
            if(fieldVal=='1') {
                if (moduleData.id == Wtf.Acc_Invoice_ModuleId) {
                    mailsubject = mailsubject = WtfGlobal.getLocaleText("acc.field.DeskeraERP") +  WtfGlobal.getLocaleText("acc.accPref.autoInvoice") + " " + record.data.fieldlabel + " " + WtfGlobal.getLocaleText("acc.field.Notification");
                    mailContent = WtfGlobal.getLocaleText("acc.field.HellobrbrWehaveenclosedyour") + moduleName + ' ' + WtfGlobal.getLocaleText("acc.field.todaydated#Date_Today#for") + companyName + "." + "<br/><br/>\n\ " +
                            WtfGlobal.getLocaleText("acc.field.Foryourkindinformationthe") + WtfGlobal.getLocaleText("acc.accPref.autoInvoice") + " " + record.data.fieldlabel + ' ' + WtfGlobal.getLocaleText("acc.common.text.for") + ' ' + moduleName + ' ' + WtfGlobal.getLocaleText("acc.field.is#Due_Date_Val#") + "<br/><br/>";

                } else if (moduleData.id == Wtf.Acc_Vendor_Invoice_ModuleId) {
                    mailsubject = mailsubject = WtfGlobal.getLocaleText("acc.field.DeskeraERP") + WtfGlobal.getLocaleText("acc.accPref.autoInvoice") + " "+ record.data.fieldlabel + " " + WtfGlobal.getLocaleText("acc.field.Notification");
                    mailContent = WtfGlobal.getLocaleText("acc.field.HellobrbrWehaveenclosedyour") + moduleName + ' ' + WtfGlobal.getLocaleText("acc.field.todaydated#Date_Today#for") + companyName + "." + "<br/><br/>\n\ " +
                            WtfGlobal.getLocaleText("acc.field.Foryourkindinformationthe") + WtfGlobal.getLocaleText("acc.accPref.autoInvoice") + " " +record.data.fieldlabel + ' ' + WtfGlobal.getLocaleText("acc.common.text.for") + ' ' + moduleName + ' ' + WtfGlobal.getLocaleText("acc.field.is#Due_Date_Val#") + "<br/><br/>";
                } else if (moduleData.id == Wtf.Acc_GENERAL_LEDGER_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.je.Type2");
                } else if (moduleData.id == Wtf.Acc_Sales_Order_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.field.SalesOrderDetails");
                } else if (moduleData.id == Wtf.Acc_Purchase_Order_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.field.PurchaseOrderDetails");
                } else if (moduleData.id == Wtf.Acc_Delivery_Order_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.field.DeliveryOrderDetails");
                } else if (moduleData.id == Wtf.Acc_Goods_Receipt_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.field.GoodsReceiptOrderDetails");
                } else if (moduleData.id == Wtf.Acc_Sales_Return_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.field.SalesReturnDetails");
                } else if (moduleData.id == Wtf.Acc_Purchase_Return_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.field.PurchaseReturnDetails");
                } else if (moduleData.id == Wtf.Acc_Customer_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.field.CustomerDetails");
                } else if (moduleData.id == Wtf.Acc_Vendor_ModuleId) {
                   mailsubject =  WtfGlobal.getLocaleText("acc.field.VendorDetails");
                } 
            }else  if(fieldVal=='10' || fieldVal=='11') {
                mailContent = "Hello,<br/><br/>We have enclosed your "+moduleName+" dated #Date_Today# for "+companyName+" .<br/><br/>\n\
                                    For your kind information, the following Product used for "+moduleName+" is going to Expiry on #Date_Val# <br/><br/>";
            } else  if(fieldVal=='12') {
            mailContent = "Hello,<br/><br/>We have enclosed your "+moduleName+" dated #Date_Today# for "+companyName+".<br/><br/>\n\
                                For your kind information, the "+record.data.fieldlabel+" for "+moduleName+" is #Date_Val# <br/><br/>\n\
                                Following are the Product Serial No(s) which are not sold yet.<br/><br/>";
            } else  if(fieldVal=='13') {
                mailContent = WtfGlobal.getLocaleText("acc.field.contractExpiryMailNotification1")+" "+companyName+".<br/><br/>\n\ "+
                                WtfGlobal.getLocaleText("acc.field.contractExpiryMailNotification2")+"<br/><br/>";
            } else if(fieldVal=='14' || fieldVal=='15'){
                mailContent = WtfGlobal.getLocaleText("acc.field.Hellobrbassetschedule")+' '+WtfGlobal.getLocaleText("acc.field.todaydated#Date_Today#")+"<br/><br/>\n\ "+
                        WtfGlobal.getLocaleText("acc.field.plnote")+record.data.fieldlabel+' '+WtfGlobal.getLocaleText("acc.field.datefor")+moduleName+ ' '+WtfGlobal.getLocaleText("acc.field.is#Date_Val#")+"<br/><br/>";
            }else if(fieldVal=='16'){
                mailsubject= WtfGlobal.getLocaleText("acc.field.DeskeraERP")+ WtfGlobal.getLocaleText("acc.field.agedProductNotification") + " " + WtfGlobal.getLocaleText("acc.field.Notification");
                mailContent = "Hello,<br/><br/>We have enclosed list of  Aged Product(s) for "+companyName+" .<br/><br/>\n\
                                For your kind information,  Following Product(s) are getting aged and are not sold yet.<br/><br/>";      
                this.beforeAfter.setValue(0);
                this.beforeAfter.disable();
            }else if(fieldVal=='17'){ // product expiry date
                mailContent = 'Hello,<br/><br/>We have enclosed list of Product(s) '+WtfGlobal.getLocaleText("acc.field.todaydated#Date_Today#for")+companyName+".<br/><br/>\n\ "+
                ' For your kind information, Following Product(s)  is(are) going to expire .<br/><br/>';     
                
            }else if(fieldVal=='18'){ // product QA Inspection rejection mail format
                mailContent = 'Hello,<br/><br/>We have enclosed list of Product(s) '+WtfGlobal.getLocaleText("acc.field.todaydated#Date_Today#for")+companyName+".<br/><br/>\n\ "+
                ' For your kind information, Following Product(s)  is rejected by QA Approver.<br/><br/>';     
                
            }else if(fieldVal=='19'){ // product QA Inspection approval mail format
               mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has approved Request  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='25'){ // Consignment Sales Request Creation
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has created  a Consignment Request  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='26'){ // Consignment Sales Request edition
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has edited a Consignment Request  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='27'){ // Consignment Sales Request approval
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has approved a Consignment Request  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='28'){ // Consignment Sales DO creation
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has created a Consignment Sales Delivery Order  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='29'){ // Consignment Sales Return Creation
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has created a Consignment Sales Return  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='30'){ // Consignment Purchase Vendor Request Creation
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has created a Purchase Order <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='31'){ // Consignment Purchase Vendor Request edition
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has edited a Purchase Order  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='32'){ // Consignment Purchase GRN creation
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has created a Goods Receipt Note  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='33'){ // Consignment Purchase Invoice creation
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has created a Purchase Invoice  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldVal=='34'){ // Consignment Purchase Return creation
                mailContent = 'Hello,<br/><br/>For your kind information, User <b>' + WtfGlobal.getLocaleText("acc.field.username#User_Name#") + "</b> has created a Purchase Return  <b>" + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            } else if(fieldVal=='35'){ // Consignment Purchase Return creation
                mailContent = 'Hello,<br/><br/>For your kind information Loan Due date is passed for <b>' + WtfGlobal.getLocaleText("acc.field.doc_no#Document_Number#") + "</b>.<br/>";
            }
            else if(fieldid==Wtf.Email_Button_From_Report || fieldid==Wtf.APPROVAL_EMAIL ||fieldid == Wtf.REJECTION_EMAIL){ // to edit email content modulewise.
//                
                Wtf.Ajax.requestEx({
                    url:"MailNotification/getEmailTemplateToEdit.do",
                    params:{
                        fieldid: fieldid,
                        moduleID:this.moduleType.getValue()
                    }
                },this, function(resp){
                    if(resp.success == true) {   
                        this.Message.setValue(resp.message);
                        this.mailSubject.setValue(resp.subject);
                        this.templateid=resp.id;
                        if (fieldid == Wtf.APPROVAL_EMAIL || fieldid == Wtf.REJECTION_EMAIL) {
                            this.SendMailToCreator.setValue(resp.mailToCreator);
                            this.SendmailToselectedPersons.setValue(resp.mailtoassignedpersons);
                            if(resp.mailtoassignedpersons){
                                 this.users.enable();
                                this.users.setValue(resp.userids);
                                 this.emailIDs.setValue(resp.emailids);
                            }
                        }
                    }else if(resp.success==false){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), WtfGlobal.getLocaleText("acc.field.Notificationrulenotsavedsucessfully")], 1);
                    }  
                });
            }else {
                mailContent = WtfGlobal.getLocaleText("acc.field.HellobrbrWehaveenclosedyour")+moduleName+' '+WtfGlobal.getLocaleText("acc.field.todaydated#Date_Today#for")+companyName+"."+"<br/><br/>\n\ "+
                        WtfGlobal.getLocaleText("acc.field.Foryourkindinformationthe")+record.data.fieldlabel+' '+WtfGlobal.getLocaleText("acc.common.text.for")+' '+moduleName+' '+WtfGlobal.getLocaleText("acc.field.is#Date_Val#")+"<br/><br/>";
                this.Message.setValue(mailContent);
                this.mailSubject.setValue(mailsubject);
            }
        }
        this.Message.setValue(mailContent);
        this.mailSubject.setValue(mailsubject);
        
    },
    onTemplateComboSelect:function(){
        if(this.templateCombo.selectedIndex <= 0){
            WtfGlobal.hideFormElement(this.hyperlinkText);
        } else{
            WtfGlobal.showFormElement(this.hyperlinkText);
            this.hyperlinkText.setValue(this.templateCombo.store.getAt(this.templateCombo.selectedIndex).data.templatename);
        }
        
    },
   submit:function(){
       if (this.selectFieldsStore) {//Clearing the filter of store as items are populating less in case of typeahead
           this.selectFieldsStore.clearFilter();
       }
       
       if(this.SendmailToselectedPersons!=undefined && this.SendmailToselectedPersons.getValue()){
           if(this.users!=undefined && this.users.getValue()==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.alert.addressee")], 3); 
                return;
           }
       }
       if(Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink){
            var moduleid = (this.moduleType && this.moduleType.getValue())?this.moduleType.getValue():"";
            var allowDDTemplate = (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Vendor_Invoice_ModuleId 
                || moduleid == Wtf.Acc_Sales_Order_ModuleId || moduleid == Wtf.Acc_Purchase_Order_ModuleId  
                || moduleid == Wtf.Acc_Delivery_Order_ModuleId || moduleid == Wtf.Acc_Goods_Receipt_ModuleId
                || moduleid == Wtf.Acc_Sales_Return_ModuleId || moduleid == Wtf.Acc_Purchase_Return_ModuleId
                || moduleid == Wtf.Acc_Make_Payment_ModuleId || moduleid == Wtf.Acc_Receive_Payment_ModuleId
                || moduleid == Wtf.Acc_Customer_Quotation_ModuleId || moduleid == Wtf.Acc_Vendor_Quotation_ModuleId
                ) && (Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink)?true:false;
           if(allowDDTemplate && this.templateCombo && ( this.templateCombo.getValue()== undefined || this.templateCombo.getValue()== "" )){
               this.templateCombo.markInvalid();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2 ); 
                return;
           }
       }
      if(this.moduleType.isValid() && this.beforeAfter.isValid() && this.days.isValid() && this.users.isValid()){
          
          if(this.field.getValue() == Wtf.Email_Button_From_Report && (this.moduleType.getValue() == Wtf.Acc_Purchase_Order_ModuleId || this.moduleType.getValue() == Wtf.Acc_Sales_Order_ModuleId
                    || this.moduleType.getValue() == Wtf.Acc_Make_Payment_ModuleId || this.moduleType.getValue() == Wtf.Acc_Receive_Payment_ModuleId
                    || this.moduleType.getValue() == Wtf.Acc_Invoice_ModuleId || this.moduleType.getValue() == Wtf.Acc_Vendor_Invoice_ModuleId ||
                    this.moduleType.getValue()==Wtf.Acc_Delivery_Order_ModuleId ||  this.moduleType.getValue()==Wtf.Acc_Goods_Receipt_ModuleId ||
                    this.moduleType.getValue()==Wtf.Acc_Sales_Return_ModuleId || this.moduleType.getValue()==Wtf.Acc_Purchase_Return_ModuleId ||
                    this.moduleType.getValue()==Wtf.Acc_Customer_Quotation_ModuleId || this.moduleType.getValue()==Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleType.getValue()== Wtf.Acc_Purchase_Requisition_ModuleId) ){

                var obj=this.Message.getValue();
                this.mailbodyjson=this.createjson(obj,true);
                obj=this.mailSubject.getValue();
                this.mailsubjectjson=this.createjson(obj,false);
            }
           if(this.beforeAfter.getValue()!=1 && this.days.getValue()===0){
              WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.Pleaseentervaluegreaterthanzero")], 3); 
              return;
           }
           Wtf.Ajax.requestEx({
             url:"MailNotification/saveMailNotification.do",
             params:{
               modules:this.moduleType.getValue(),
               beforeAfter:this.beforeAfter.getValue(),
               days:this.days.getValue(),
               companyid:companyid,
               users:this.users.getValue(),
               fieldid : this.field.getValue(),
               isMailToSalesPerson: this.isMailToSalesPerson.getValue(),
               isMailToContactPerson: this.isMailToContactPerson.getValue(),
               isMailToStoreManager: this.SendMailToStoreManager.getValue(),
               isMailToShippingEmail: this.SendMailToShippingEmail.getValue(),
               isMailToAssignedPerson: this.isMailToAssignedPerson.getValue(),
               emailids : this.emailIDs.getValue(), 
                senderid : this.senderId.getValue(), 
               mailcontent : this.Message.getValue(),
               mailsubject : this.mailSubject.getValue(),
               modulename:this.moduleType.lastSelectionText,
               isSendMailToCreator:this.SendMailToCreator.getValue(),
               isSendMailToAssignee:this.SendmailToselectedPersons.getValue(),
               id:this.templateid,
               recurringruleid:this.recurringruleid,
               mailbodyjson:this.mailbodyjson?Wtf.util.JSON.encode(this.mailbodyjson):"",
               mailsubjectjson:this.mailsubjectjson?Wtf.util.JSON.encode(this.mailsubjectjson):"",
               templateid: (this.templateCombo && this.templateCombo.getValue())?this.templateCombo.getValue():"",
               hyperlinkText : this.hyperlinkText.getValue().trim()
             }
            },this, function(resp){
                 if(resp.success == true) {    
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0); 
                    this.disablebuttons();
                    this.Message.activated=false;
                    WtfGlobal.hideFormElement(this.selectField);
                    this.insertbutton.hide();
                    this.gridGroupStore.load();
                 }else if(resp.success==false){
                     if(resp.duplicate==true){
                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), resp.msg], 3);   
                     }else{
                       WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), resp.msg], 1);   
                     }
                 }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), WtfGlobal.getLocaleText("acc.field.Notificationrulenotsavedsucessfully")], 1);
                 }  
                 this.Message.setValue('');
          },function(resp){
          });          
       }else{
          this.beforeAfter.isValid(); 
          this.users.isValid();
          this.days.isValid()
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
         return;  
       }
       this.NewRule();
    },
    editData:function(){
        if(this.grid.getSelections().length>0){
            var rec = this.grid.getSelectionModel().getSelected();
        }
        if(this.SendmailToselectedPersons!=undefined && this.SendmailToselectedPersons.getValue()){
           if(this.users!=undefined && this.users.getValue()==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.alert.addressee")], 3); 
                return;
           }
       }
        if (this.field.getValue() != Wtf.APPROVAL_EMAIL && this.field.getValue() !=  Wtf.REJECTION_EMAIL ) {
            if (this.field.getValue() != Wtf.Email_Button_From_Report && this.beforeAfter.getValue() != 1 && this.days.getValue() === 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.Pleaseentervaluegreaterthanzero")], 3);
                return;
            }
        }
        var users='';
        
        if(this.field.getValue() == Wtf.APPROVAL_EMAIL ||this.field.getValue()==  Wtf.REJECTION_EMAIL &&  this.SendmailToselectedPersons.getValue()){
            users=this.users.getValue();
        }else if(this.field.getValue() != Wtf.APPROVAL_EMAIL ||this.field.getValue()==  Wtf.REJECTION_EMAIL){
            users=this.users.getValue();
        }
        if(Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink){
            var moduleid = (this.moduleType && this.moduleType.getValue())?this.moduleType.getValue():"";
            var allowDDTemplate = (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Vendor_Invoice_ModuleId 
                || moduleid == Wtf.Acc_Sales_Order_ModuleId || moduleid == Wtf.Acc_Purchase_Order_ModuleId  
                || moduleid == Wtf.Acc_Delivery_Order_ModuleId || moduleid == Wtf.Acc_Goods_Receipt_ModuleId
                || moduleid == Wtf.Acc_Sales_Return_ModuleId || moduleid == Wtf.Acc_Purchase_Return_ModuleId
                || moduleid == Wtf.Acc_Make_Payment_ModuleId || moduleid == Wtf.Acc_Receive_Payment_ModuleId
                || moduleid == Wtf.Acc_Customer_Quotation_ModuleId || moduleid == Wtf.Acc_Vendor_Quotation_ModuleId
                ) && (Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink)?true:false;
           if(allowDDTemplate && this.templateCombo && ( this.templateCombo.getValue()== undefined || this.templateCombo.getValue()== "" )){
                this.templateCombo.markInvalid();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2 ); 
                return;
           }
       }
        
        //ERP-20634
        if (this.selectFieldsStore) {//Clearing the filter of store as items are populating less in case of typeahead
            this.selectFieldsStore.clearFilter();
        }
        if (this.field.getValue() == Wtf.Email_Button_From_Report && (this.moduleType.getValue() == Wtf.Acc_Purchase_Order_ModuleId || this.moduleType.getValue() == Wtf.Acc_Sales_Order_ModuleId
                || this.moduleType.getValue() == Wtf.Acc_Make_Payment_ModuleId || this.moduleType.getValue() == Wtf.Acc_Receive_Payment_ModuleId
                || this.moduleType.getValue() == Wtf.Acc_Invoice_ModuleId || this.moduleType.getValue() == Wtf.Acc_Vendor_Invoice_ModuleId ||
                this.moduleType.getValue() == Wtf.Acc_Delivery_Order_ModuleId|| this.moduleType.getValue() == Wtf.Acc_Goods_Receipt_ModuleId ||
                this.moduleType.getValue()==Wtf.Acc_Sales_Return_ModuleId || this.moduleType.getValue()==Wtf.Acc_Purchase_Return_ModuleId ||
                this.moduleType.getValue()==Wtf.Acc_Customer_Quotation_ModuleId || this.moduleType.getValue()==Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleType.getValue()== Wtf.Acc_Purchase_Requisition_ModuleId)) {

            var obj = this.Message.getValue();
            this.mailbodyjson = this.createjson(obj, true);
            obj = this.mailSubject.getValue();
            this.mailsubjectjson = this.createjson(obj, false);
        }    
        var id=rec.data.id;
        Wtf.Ajax.requestEx({
             url:"MailNotification/editMailNotificationData.do",
            params:{
                id:id,
                modules:this.moduleType.getValue(),
                beforeAfter:this.beforeAfter.getValue(),
                days:this.days.getValue(),
                companyid:companyid,
                users:users,
                fieldid : this.field.getValue(),
                isMailToSalesPerson: this.isMailToSalesPerson.getValue(),
                isMailToStoreManager: this.SendMailToStoreManager.getValue(),
                isMailToShippingEmail: this.SendMailToShippingEmail.getValue(),
                isMailToAssignedPerson: this.isMailToAssignedPerson.getValue(),
                emailids : this.emailIDs.getValue(),
                senderid : this.senderId.getValue(), 
                mailcontent : this.Message.getValue(),
                mailsubject : this.mailSubject.getValue(),
                modulename:this.moduleType.lastSelectionText,
                isSendMailToCreator:this.SendMailToCreator.getValue(),
                isSendMailToAssignee:this.SendmailToselectedPersons.getValue(),
                recurringruleid:this.recurringruleid,
                isMailToContactPerson: this.isMailToContactPerson.getValue(),
                repeatNotification:this.repeatNotification.getValue(),
                mailbodyjson:this.mailbodyjson?Wtf.util.JSON.encode(this.mailbodyjson):"",
                mailsubjectjson:this.mailsubjectjson?Wtf.util.JSON.encode(this.mailsubjectjson):"",
                templateid: (this.templateCombo && this.templateCombo.getValue())?this.templateCombo.getValue():"",
                hyperlinkText: this.hyperlinkText.getValue().trim()
            }
        },this, function(resp){
            if(resp.success == true) {    
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0); 
                this.gridGroupStore.load();
                this.disablebuttons();
                this.Message.activated=false;
                WtfGlobal.hideFormElement(this.selectField);
                this.insertbutton.hide();
            }else if(resp.success==false){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), resp.msg], 1);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), WtfGlobal.getLocaleText("acc.field.Notificationrulenoteditedsucessfully")], 1);
            }       
        },function(resp){
            });
        this.NewRule();
    },
    deleteData:function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedrule"),function(btn){
            if(btn=="yes") {            
             if(this.grid.getSelections().length>0){
               var rec = this.grid.getSelectionModel().getSelected();
             }
             var id=rec.data.id;
             Wtf.Ajax.requestEx({
             url:"MailNotification/deleteMailNotificationData.do",
             params:{
                 id:id,
                 companyid:companyid,
                 modulename:rec.data.modulename
               }
         },this, function(resp){
                 if(resp.success == true) {    
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0); 
                    this.gridGroupStore.load();
                 }else if(resp.success==false){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), resp.msg], 1);
                 }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), WtfGlobal.getLocaleText("acc.field.Notificationrulenotdeletedsucessfully")], 1);
                 }       
          },function(resp){
      });
      this.NewRule();
       }
        },this)
    },
    handleRender:function(panelObj) {
        this.gridGroupStore.load({
            params:{
                start:0,
                limit:15
            }
        });
    },
    transactionTypeRenderer : function(value){
            var record = this.moduleStore.queryBy(function(record){
                return (record.get('id') == value);
            }, this).items[0];
            return record.data.name;
        },
    typeRenderer:function(value,a,record){
        if(record.data.fieldid === Wtf.Email_Button_From_Report ||record.data.fieldid== Wtf.APPROVAL_EMAIL ||record.data.fieldid== Wtf.REJECTION_EMAIL){
            return 'NA'; 
        } else if(value === 0) {
                return WtfGlobal.getLocaleText("acc.field.Beforedate");
                
            } else if(value === 1) {
                return WtfGlobal.getLocaleText("acc.field.Ondate"); 
            }else if(value === 2) {
                return WtfGlobal.getLocaleText("acc.field.Afterdate");        
            }  
       },
    daysRenderer:function(value,a,record){
        if(record.data.fieldid == Wtf.Email_Button_From_Report ||record.data.fieldid== Wtf.APPROVAL_EMAIL ||record.data.fieldid== Wtf.REJECTION_EMAIL){
            return 'NA';  
        }else if(value===1 || value===0){
           return value+" day"; 
        }else{
           return value+" days";  
            
        }  
    },
    addressRenderer:function(value,a,record){
         if(record.data.fieldid == Wtf.Email_Button_From_Report){
            return 'NA';  
        }else{
           return value; 
            
        }  
    },
     sendCopyRenderer:function(value,a,record){
         if(record.data.fieldid == Wtf.Email_Button_From_Report){
            return 'NA';  
        }else{
           return value; 
            
        }  
    },
    showDayField:function(combo,rec,ind){
       if(this.beforeAfter.getValue()===0 || this.beforeAfter.getValue()===2){
           this.days.enable();
           this.days.reset();
       }else{  
           this.days.disable();
           this.days.setValue(0);
       }      
    },
    callRecurrigReminderWindow:function(){
        if(this.repeatNotification.getValue()){
           callNotificationRecurrigReminderWindow(this.recurringruleid,this.moduleType.getValue(),this.moduleType.lastSelectionText); 
            Wtf.getCmp('notificationRecurrigReminderWindow').on('update',function(config){
                this.recurringruleid=config.recurringruleid;
            },this);
        }
    },
    rowClickHandle:function(){
       this.DeleteBttn.enable();
       this.submitBttn.disable();
       this.EditBttn.enable();
       this.NewRuleBttn.enable()
       var recData = this.grid.getSelectionModel().getSelected().data;
       var moduleType=recData.module;
       var beforeAfter=recData.beforeafter;
       var days=recData.days;
       this.moduleType.setValue(moduleType);
       this.beforeAfter.setValue(beforeAfter);
       this.emailIDs.setValue(recData.emailids);
       this.senderId.setValue(recData.senderid);
       this.mailSubject.setValue(recData.mailsubject);
       this.Message.setValue(recData.mailcontent);
       var fieldrec = new this.fieldRec({
            fieldid : recData.fieldid ,
            fieldlabel : recData.fieldname,
            islineitem : recData.islineitem
       })
       this.fieldds.removeAll();
       this.fieldds.add(fieldrec);
       this.field.setValue(recData.fieldid);
       this.field.disable();
       this.users.setValue(recData.userids);

       this.isMailToSalesPerson.reset();
       WtfGlobal.hideFormElement(this.isMailToSalesPerson);
       this.SendMailToStoreManager.reset();
       WtfGlobal.hideFormElement(this.SendMailToStoreManager);
       this.SendMailToShippingEmail.reset();
       WtfGlobal.hideFormElement(this.SendMailToShippingEmail);
       this.hyperlinkText.reset();
       WtfGlobal.hideFormElement(this.hyperlinkText);

       this.isMailToAssignedPerson.reset();
       WtfGlobal.hideFormElement(this.isMailToAssignedPerson);
       
        if (this.moduleType.getValue() == Wtf.Acc_Contract_ModuleId || this.moduleType.getValue() == Wtf.Consignment_Sales_ModuleId || this.moduleType.getValue() == Wtf.Consignment_Purchase_ModuleId || recData.fieldid == "18" || recData.fieldid == "19") {
            WtfGlobal.showFormElement(this.isMailToSalesPerson);
            this.isMailToSalesPerson.setValue(recData.isMailToSalesPerson);
             WtfGlobal.showFormElement(this.isMailToContactPerson);
            this.isMailToContactPerson.setValue(recData.isMailToContactPerson);
            if (this.moduleType.getValue() == Wtf.Consignment_Sales_ModuleId || this.moduleType.getValue() == Wtf.Consignment_Purchase_ModuleId || recData.fieldid == "18" || recData.fieldid == "19") {
                WtfGlobal.showFormElement(this.SendMailToStoreManager);
                this.SendMailToStoreManager.setValue(recData.isMailToStoreManager);
            }
        } else if(this.moduleType.getValue() == Wtf.Asset_Maintenance_ModuleId) {
            WtfGlobal.showFormElement(this.isMailToAssignedPerson);
            this.isMailToAssignedPerson.setValue(recData.isMailToAssignedPerson);
        }
        if (beforeAfter == 1 || this.beforeAfter.getValue() == 1 || this.moduleType.getValue() == Wtf.Consignment_Sales_ModuleId || this.moduleType.getValue() == Wtf.Consignment_Purchase_ModuleId) {
            if (this.moduleType.getValue() == Wtf.Consignment_Sales_ModuleId && recData.fieldid === "35") {
                this.days.enable();
                this.days.setValue(days);
            } else {
                this.days.disable();
                this.days.setValue(0);
            }
       }else{
           this.days.enable(); 
           this.days.setValue(days);
       }
        this.disablebuttons();//Disabling the buttons when dependentupon button is clicked.
        this.Message.activated=false;
        WtfGlobal.hideFormElement(this.selectField);
        this.insertbutton.hide();
        
        if(recData.fieldid === Wtf.Email_Button_From_Report){
           if(recData.module == Wtf.Acc_Purchase_Order_ModuleId || recData.module == Wtf.Acc_Sales_Order_ModuleId || recData.module == Wtf.Acc_Make_Payment_ModuleId || recData.module == Wtf.Acc_Receive_Payment_ModuleId
                    || recData.module == Wtf.Acc_Invoice_ModuleId || recData.module == Wtf.Acc_Vendor_Invoice_ModuleId ||
                    recData.module == Wtf.Acc_Delivery_Order_ModuleId || recData.module == Wtf.Acc_Goods_Receipt_ModuleId ||
                    recData.module ==Wtf.Acc_Sales_Return_ModuleId || recData.module ==Wtf.Acc_Purchase_Return_ModuleId ||
                    recData.module==Wtf.Acc_Customer_Quotation_ModuleId || recData.module==Wtf.Acc_Vendor_Quotation_ModuleId){
                this.emailIDs.getEl().up('.x-form-item').setDisplayed(true);
           } else {
                this.emailIDs.getEl().up('.x-form-item').setDisplayed(false);
           }
        }
       if(recData.fieldid === Wtf.Email_Button_From_Report){//20
           this.EnableDisableButton();
           this.DeleteBttn.disable();
             this.SendMailToCreator.reset();
            this.SendmailToselectedPersons.reset();
            this.SendMailToCreator.hide();
            this.SendmailToselectedPersons.hide();
            if(recData.module == Wtf.Acc_Purchase_Order_ModuleId || recData.module == Wtf.Acc_Sales_Order_ModuleId || recData.module == Wtf.Acc_Make_Payment_ModuleId || recData.module == Wtf.Acc_Receive_Payment_ModuleId
                    || recData.module == Wtf.Acc_Invoice_ModuleId || recData.module == Wtf.Acc_Vendor_Invoice_ModuleId ||
                    recData.module == Wtf.Acc_Delivery_Order_ModuleId || recData.module == Wtf.Acc_Goods_Receipt_ModuleId ||
                    recData.module ==Wtf.Acc_Sales_Return_ModuleId || recData.module ==Wtf.Acc_Purchase_Return_ModuleId ||
                    recData.module==Wtf.Acc_Customer_Quotation_ModuleId || recData.module==Wtf.Acc_Vendor_Quotation_ModuleId || moduleData.id ==Wtf.Acc_Purchase_Requisition_ModuleId){
                this.Message.activated=false;
                this.moduleid=recData.module;
                WtfGlobal.showFormElement(this.selectField);
                this.insertbutton.show();
                this.setGlobalStore(recData.module);//assigning default values to store
                
                WtfGlobal.showFormElement(this.SendMailToShippingEmail);
                this.SendMailToShippingEmail.setValue(recData.ismailtoshippingemail);
                if(recData.templateid != ""){
                    WtfGlobal.showFormElement(this.hyperlinkText);
                    this.hyperlinkText.setValue(recData.hyperlinkText);
                }
            }
            this.sendMailToFieldSet.hide();
       }else if(recData.fieldid === Wtf.APPROVAL_EMAIL ||recData.fieldid === Wtf.REJECTION_EMAIL ){//22
           this.EnableDisableButton();
            this.DeleteBttn.disable();
            this.SendMailToCreator.enable();
            this.SendmailToselectedPersons.enable();
             this.SendMailToCreator.show();
            this.SendmailToselectedPersons.show();
            this.SendMailToCreator.setValue(recData.mailToCreator);
            this.SendmailToselectedPersons.setValue(recData.mailtoassignedpersons);
            if(recData.mailtoassignedpersons){
                this.users.enable();
            }
            this.sendMailToFieldSet.show();
       }else{
           
            this.days.enable();
            this.beforeAfter.enable();
            this.emailIDs.enable();
            this.isMailToAssignedPerson.enable();
            this.isMailToSalesPerson.enable();
            this.SendMailToStoreManager.enable();
            this.users.enable();
            this.DeleteBttn.enable();
            
            this.SendMailToCreator.reset();
            this.SendmailToselectedPersons.reset();
            this.SendMailToCreator.hide();
            this.SendmailToselectedPersons.hide();
            
            
            this.sendMailToFieldSet.hide();
        }
        if((this.moduleType.getValue() == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleType.getValue() == Wtf.Acc_Invoice_ModuleId) && recData.fieldid != Wtf.Email_Button_From_Report){
            WtfGlobal.showFormElement(this.repeatNotification);
            this.recurringruleid=recData.recurringruleid;
            if(recData.recurringruleid!="" && recData.recurringruleid!=undefined){
                this.repeatNotification.setValue(true);
            } else {
                this.repeatNotification.setValue(false);
            }
        } else {
            this.repeatNotification.setValue(false);
            WtfGlobal.hideFormElement(this.repeatNotification);
        }
        var moduleid = (this.moduleType && this.moduleType.getValue())?this.moduleType.getValue():"";
        var allowDDTemplate = (moduleid == Wtf.Acc_Invoice_ModuleId || moduleid == Wtf.Acc_Vendor_Invoice_ModuleId 
                            || moduleid == Wtf.Acc_Sales_Order_ModuleId || moduleid == Wtf.Acc_Purchase_Order_ModuleId  
                            || moduleid == Wtf.Acc_Delivery_Order_ModuleId || moduleid == Wtf.Acc_Goods_Receipt_ModuleId
                            || moduleid == Wtf.Acc_Sales_Return_ModuleId || moduleid == Wtf.Acc_Purchase_Return_ModuleId
                            || moduleid == Wtf.Acc_Make_Payment_ModuleId || moduleid == Wtf.Acc_Receive_Payment_ModuleId
                            || moduleid == Wtf.Acc_Customer_Quotation_ModuleId || moduleid == Wtf.Acc_Vendor_Quotation_ModuleId
                            || moduleid == Wtf.Acc_Purchase_Requisition_ModuleId
                            ) && (Wtf.account.companyAccountPref.activateDDTemplateFlow||Wtf.account.companyAccountPref.activateDDInsertTemplateLink) ?true:false;
       if(allowDDTemplate){
            this.templateStore.removeAll();
            this.templateCombo.reset();
            WtfGlobal.showFormElement(this.templateCombo);   
            var colModArray = GlobalCustomTemplateList[this.moduleType.getValue()];
            var isTflag=colModArray!=undefined && colModArray.length>0?true:false;
            if(isTflag){
                var none  = new this.templateRecord({
                    templateid :"None",
                    templatename : "None"
                });
                this.templateStore.add(none);
                for (var count = 0; count < colModArray.length; count++) {
                    var id1=colModArray[count].templateid;
                    var name1=colModArray[count].templatename;           
                    if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && !addTemplate(id1)){
                        continue;
                    }
                    var record1  = new this.templateRecord({
                        templateid :id1,
                        templatename : name1
                    });
                    this.templateStore.add(record1);

                }
            }
            if(recData && recData.templateid){
                this.templateCombo.setValue(recData.templateid);
            }
        } else{
            if(this.templateCombo){
                this.templateStore.removeAll();
                this.templateCombo.reset();
                this.templateCombo.allowBlank= true;
                WtfGlobal.hideFormElement(this.templateCombo);   
            }
        }
    },
    NewRule:function(){
        this.grid.getSelectionModel().clearSelections();
        this.moduleType.enable();
        this.beforeAfter.enable();
        this.submitBttn.enable();
        this.DeleteBttn.disable();
        this.EditBttn.disable();
        this.field.enable();
        this.days.enable();
        this.moduleType.setValue("");
        this.beforeAfter.setValue("");
        this.moduleType.clearValue();
        this.beforeAfter.clearValue();
        this.field.clearValue();
        this.emailIDs.setValue("");
        this.senderId.setValue("");
        this.mailSubject.setValue("");
        this.Message.setValue("");
        this.days.reset();
        this.users.reset();
        this.isMailToSalesPerson.reset();
        this.SendMailToStoreManager.reset();
        this.SendMailToShippingEmail.reset();
        this.isMailToAssignedPerson.reset();
        this.SendMailToCreator.reset();
        this.SendmailToselectedPersons.reset();
        this.isMailToContactPerson.reset(),
        this.SendMailToCreator.disable();
        this.SendmailToselectedPersons.disable();
        this.recurringruleid="";
        this.repeatNotification.setValue(false);
        WtfGlobal.hideFormElement(this.repeatNotification);
        this.sendMailToFieldSet.hide();
        this.templateCombo.reset();
        this.templateStore.removeAll();
        this.hyperlinkText.reset();
    },
    EnableDisableButton:function(){
        this.field.enable();
        this.days.disable();
        this.beforeAfter.disable();
//        this.emailIDs.disable();
        this.isMailToAssignedPerson.disable();
        this.isMailToSalesPerson.disable();
        this.users.disable();
        
    },
    enabledisableAddresseeCombo: function() {
        if (this.SendmailToselectedPersons.getValue()) {
            this.users.enable();
        } else {
            this.users.setValue("");
            this.users.reset();
            this.users.disable();
        }

    }
});

    //Reminder Window
 function callNotificationRecurrigReminderWindow(recurringruleid,moduleid,modulename){
        var window = Wtf.getCmp('notificationRecurrigReminderWindow');
        if(!window){
            new Wtf.account.notificationRecurrigReminderWindow({
                title:WtfGlobal.getLocaleText("acc.mailconfiguration.RecurringReminderRule"),
                id:'notificationRecurrigReminderWindow',                                   
                iconCls :getButtonIconCls(Wtf.etype.deskera),            
                width:500,
                height:265,
                resizable:false,
                closable: true,
                layout:'fit',
                renderTo: document.body,
                modal: true,
                recurringruleid:recurringruleid,
                constrainHeader :true,
                moduleid:moduleid,
                modulename:modulename
            }).show();        
        }
    }
    
Wtf.account.notificationRecurrigReminderWindow=function(config){
    Wtf.apply(this,config);
    this.recurringruleid=config.recurringruleid;
    this.moduleid=config.moduleid;
    this.modulename=config.modulename;

    var buttonArray = new Array();
    this.closeButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close"),
        minWidth: 50,
        scope: this,
        handler: function(){
            this.close();
        }
    });
     
    this.saveButton = new Wtf.Toolbar.Button({
        text:  WtfGlobal.getLocaleText("acc.common.saveBtn"),
        minWidth: 50,
        id:'savebutton'+this.id,
        disabled: this.viewGoodReceipt || this.isViewTemplate,
        scope: this,
        handler: this.saveData.createDelegate(this)
    });
    
    buttonArray.push(this.saveButton);
    buttonArray.push(this.closeButton);
    Wtf.apply(this,{
        buttons:buttonArray 
    });
    this.addEvents({
        'update':true
    });
    Wtf.account.notificationRecurrigReminderWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.notificationRecurrigReminderWindow, Wtf.Window ,{
    onRender:function(config){
        Wtf.account.notificationRecurrigReminderWindow.superclass.onRender.call(this,config);
        this.createFields();
        var centerPanel = new Wtf.Panel({
            id:'addresscenterpanel'+this.id,
            bodyStyle:' background: none repeat scroll 0 0 #DFE8F6;',
            layout: 'fit',
            items:this.repeateForm
        });
        this.add(centerPanel);
        if(this.recurringruleid!="" && this.recurringruleid!=undefined){ //if id not vaialble that recurring is not set. so no need to load
          this.loadRecord();
        }
    },
    loadRecord:function(){
        Wtf.Ajax.requestEx({
            url:"MailNotification/getMailNotificationRecurringDetails.do",
            params: {
               recurringruleid:this.recurringruleid 
            }
        },this,function(response){
           if(response.success){
               this.repeatTime.setValue(response.repeatedTime);
               if(response.repeatedTimeType==1){//days
                   this.repeatTimeType.setValue('day');
               } else if(response.repeatedTimeType==2) {//week
                   this.repeatTimeType.setValue('week');
               } else {//month 
                   this.repeatTimeType.setValue('month'); 
               }
                if(response.endType==1){
                   this.neverEndRepeat.setValue(true);
                   this.endInterval.disable();
                }else{
                   this.endRepeatAfterInterval.setValue(true);
                   this.endInterval.setValue(response.endInterval);
                   this.endInterval.enable();
                }
           }
        },function(response){

        });
    },
    createFields:function(){
        this.repeatTime = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.mailconfiguration.RepeatEvery"),
            width: 120,
            allowBlank: false,
            minValue: 1,
            maxValue: 999,
            allowNegative: false,
            maxLength: 100,
            name: "repeatTime"
        });
        this.repeatTimeType = new Wtf.form.ComboBox({
            store: Wtf.intervalTypeStore,
            hiddenName:'repeatTimeType',
            displayField:'name',
            valueField:'id',
            mode: 'local',
            value: "day",
            triggerAction: 'all',
            allowBlank: false,
            typeAhead:true,
            hideLabel: true,
            width: 150,
            selectOnFocus:true
        });
        this.intervalPanel = new Wtf.Panel({
            layout: "column",
            border: false,
            bodyStyle:'padding-top:10px;padding-left:12px',
            items:[
            new Wtf.Panel({
                columnWidth: 0.55,
                layout: "form",
                border: false,
                anchor:'100%',
                items : this.repeatTime
            }),
            new Wtf.Panel({
                columnWidth: 0.45,
                layout: "form",
                border: false,
                anchor:'100%',
                items : this.repeatTimeType
            })]
        });
        
        this.neverEndRepeat=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.mailconfiguration.NeverEnds"),
            name:'repeatEnd',
            checked :true,
            id:'neverEndRepeat'
        });
        
        this.neverEndRepeat.on('change',function(radio,newVal,oldVal){
            if(newVal){
               this.endInterval.setValue("");
               this.endInterval.clearInvalid();
               this.endInterval.disable(); 
            }
        },this);
        
        this.endRepeatAfterInterval=new Wtf.form.Radio({
            fieldLabel:WtfGlobal.getLocaleText("acc.mailconfiguration.EndAfterInterval"),
            name:'repeatEnd',
            checked :false,
            id:'noRevenueRecognition'
        });
        
        this.endRepeatAfterInterval.on('change',function(radio,newVal,oldVal){
            if(newVal){
               this.endInterval.enable(); 
            }
        },this);
        
        this.endInterval = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.mailconfiguration.Intervals"),
            width: 120,
            allowBlank: true,
            minValue: 1,
            maxValue: 999,
            allowNegative: false,
            disabled:true,
            name: "endInterval"
        });
        
        this.endsFiles=new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.mailconfiguration.Ends"),
            autoHeight:true,
            width : 420,
            items:[this.neverEndRepeat,this.endRepeatAfterInterval,this.endInterval]
        });

       this.repeateForm = new Wtf.form.FormPanel({
            bodyStyle: 'background:#f1f1f1;padding-left:10px;padding-left:10px',
            border: false,
            labelWidth:100,
            items:[this.intervalPanel,this.endsFiles]
        });
    },
    saveData:function(){
        var valid = this.repeateForm.getForm().isValid();
        if(this.endRepeatAfterInterval.getValue() && this.endInterval.getValue()==""){
            valid=false;
            this.endInterval.markInvalid();
        }
        if(valid){
            var rec=[];
            rec = this.repeateForm.getForm().getValues();
            rec.recuringruleid=this.recurringruleid;
            rec.repeatTimeType=this.repeatTimeType.getValue()=='day'?1:this.repeatTimeType.getValue()=='week'?2:this.repeatTimeType.getValue()=='month'?3:"";
            rec.endType=this.neverEndRepeat.getValue()?1:this.endRepeatAfterInterval.getValue()?2:"";
            rec.modulename=this.modulename;
            
            Wtf.Ajax.requestEx({
                url:"MailNotification/saveMailNotificationRecurringDetails.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        } else {
            WtfComMsgBox(2,2);
            return; 
        }
        this.saveButton.disable();
    },
    genSuccessResponse:function(response){
        this.recurringruleid=response.recurringruleid;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),WtfGlobal.getLocaleText("acc.field.Youhavesuccessfullysavedyourrecurringdetails")],3);
        this.fireEvent("update",this);
        this.close();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});
