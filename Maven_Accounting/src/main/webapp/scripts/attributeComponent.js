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
Wtf.attributeComponent = function(config){
    Wtf.apply(this, config);
    Wtf.attributeComponent.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.attributeComponent, Wtf.Panel, {
	labelWidth:80,
	layout:'form',
	border: false,
	defaults:{
		anchor:'98%'
	},
    initComponent: function() {
        Wtf.attributeComponent.superclass.initComponent.call(this);

        this.addEvents({
            "closeform": true,
            "setObject":true
        });
        
        
                this.count = 0;
        Wtf.Ajax.requestEx({
            url: "ACCCommon/getFieldParams.do",
            params: {
                moduleid:this.recmoduleid,
               invoiceId:this.recinvoiceId
            }
            },this,
        function(responseObj) {
            this.fireEvent("setObject",responseObj);
            this.sdate = "0";
            this.edate = "0";

            if(responseObj.data !='' && responseObj.data !=null){
                this.count = responseObj.data.length;
                var campaignTypeFlag = false;
                var moveToLeadFlag = false;
                for(var i = 0; i < responseObj.data.length; i++) {

                    if(responseObj.data[i].fieldtype=="1"){   // TextField

                        if(this.callFrom=="MoveLead"){  // Create Lead form from Move Lead from View Campaign

                            if(!moveToLeadFlag){
                                this.addMoveToLeadTextField();
                                moveToLeadFlag = true;
                            }
                            if(!(responseObj.data[i].recordname=="lastname") && !(responseObj.data[i].recordname=="firstname") && !(responseObj.data[i].recordname=="email")){
                                this.addTextField(responseObj.data[i]);
                            } 
                                
                        } else {
                        	var props = {};
                        	if(responseObj.data[i].recordname=="email"){
                        		props.vtype='email';
                        	}
                            this.addTextField(responseObj.data[i],props);

                            if(this.configType=="Campaign" && responseObj.data[i].recordname=="campaignname"){
                                this.addCampaignTypeComboBox(campaignTypeFlag);
                            }
                        }

                    } else if(responseObj.data[i].fieldtype=="2"){  // NumberField

                        this.addNumberField(responseObj.data[i]);

                    } else if(responseObj.data[i].fieldtype=="3"){  // DateField

                        this.addDateField(responseObj.data[i]);

                    } else if(responseObj.data[i].fieldtype=="4" || responseObj.data[i].fieldtype=="7") {  // ComboBox Or Multi-Select Combo Box

                        if(responseObj.data[i].comboname!=undefined && responseObj.data[i].comboname[0]=="Campaign Type"){
                            campaignTypeFlag = true;
                        }else {
                            this.addComboBox(responseObj.data[i]);
                        }

                    } else if(responseObj.data[i].fieldtype=="5"){  // TimeField

                        this.addTimeField(responseObj.data[i]);

                    } else if(responseObj.data[i].fieldtype=="6"){ // CheckBox

                        this.addCheckBox(responseObj.data[i]);

                    } else if(responseObj.data[i].fieldtype=="8"){ // Reference Module

                        this.addReferenceComboBox(responseObj.data[i]);

                    }
                    Wtf.getCmp(this.id).doLayout();
                }

            }
            this.fireEvent("closeform", this.id);
        },
        function() {
            this.fireEvent("closeform", this.id);
        }
        );
        
    },

    onRender: function(config){
        Wtf.attributeComponent.superclass.onRender.call(this, config);

    },
    validateSelection : function(combo,record,index){
        return record.get('hasAccess' );
    },
    isValidate: function() {
        var flag = 0;
        for(var i = 0; i < this.count; i++) {
            if(Wtf.getCmp(this.id+'text'+i) && Wtf.getCmp(this.id+'text'+i).validate() == false) {
                flag = 1;
                break;
            }
        }
        if(flag == 1) {
            return false;
        } else {
            return true;
        }
    },
    addTextField : function(responseObjData, props){

        this.text =new Wtf.ux.TextField(Wtf.applyIf(props||{},{
            id:responseObjData.recordname+this.id,
            fieldLabel: (responseObjData.allowblank==0)?responseObjData.fieldname+"*":responseObjData.fieldname,
            name: responseObjData.fieldname,
            scope: this,
            //anchor:this.attributeAnchor,
            //style:this.attributeStyle,
            maxLength:(responseObjData.maxlength!="0"|| responseObjData.maxlength!=null)? responseObjData.maxlength : Number.MAX_VALUE,
            allowBlank:responseObjData.allowblank
        }));

        Wtf.getCmp(this.id).add(this.text);

    },
    // Kuldeep Singh : Add NumberField
    addNumberField : function(responseObjData){

        Wtf.getCmp(this.id).add(new Wtf.form.NumberField({
            id:responseObjData.recordname+this.id,
            fieldLabel: (responseObjData.allowblank==0)?responseObjData.fieldname+"*":responseObjData.fieldname,
            name: responseObjData.fieldname,
            maxLength:15,
            scope: this,
            allowBlank:responseObjData.allowblank
        }));
    },
    // Kuldeep Singh : Add DateField
    addDateField : function(responseObjData){

        var recname = responseObjData.recordname;
        var datevalue=undefined;
        if(recname.replace(" ","").toLowerCase()=='startdate'){ // Current Date for start date and end date
            this.sdate = recname;
            datevalue = new Date();
        }
        if(recname.replace(" ","").toLowerCase()=='enddate'){
            this.edate = recname;
        }
        Wtf.getCmp(this.id).add(new Wtf.form.DateField({
            id:responseObjData.recordname+this.id,
            readOnly:true,
            emptyText:WtfGlobal.getLocaleText("acc.field.--SelectDate--"),
            recordname:responseObjData.recordname,
            fieldLabel: (responseObjData.allowblank==0)?responseObjData.fieldname+"*":responseObjData.fieldname,
            name: responseObjData.fieldname,
            allowBlank:responseObjData.allowblank,
            renderer:WtfGlobal.onlyDateRendererTZ,
            offset:Wtf.pref.tzoffset,
            value:datevalue
        }));
    },

    cloneRemoteComboStore : function(store) {
        return new Wtf.data.Store( {
            proxy : store.proxy,
            reader : store.reader,
            baseParams : store.baseParams,
            autoLoad : store.autoLoad
        });
    },
    // Kuldeep Singh : Add ComboBox or Multi-Select Combobox
    addComboBox : function(responseObjData){

        var storedata =[];
        if(responseObjData.masterdata!=null){
            for(var ctr =0;ctr<responseObjData.masterdata.length;ctr++){
                var storerecord = [];
                storerecord.push(responseObjData.masterdata[ctr].id);
                storerecord.push(responseObjData.masterdata[ctr].data);
                storedata.push(storerecord);
            }
        }
        var url = "";
        var baseParams = {};
        var displayField = 'name';
        var valueField = 'id';
        var createStore = true;
        if(responseObjData.customflag){
            url = 'crm/common/fieldmanager/getCustomCombodata.do';
            baseParams = {
                mode:2,
                flag:1,
                fieldid:responseObjData.pojoname
            }
        }else if(responseObjData.comboname!=null) {
            if(responseObjData.comboname=="Lead Type") {
                valueField = 'mainid';
            }
            /*
             *  useed remote drop down so created new store here
             */
            url = 'Common/CRMManager/getComboData.do';
            baseParams = {
                comboname:responseObjData.comboname,
                common:'1'
            };
        } else if(responseObjData.masterconfigid!=null) {
            var masterconfigid = parseInt(responseObjData.masterconfigid);
            switch(masterconfigid) {
                case 1:
                    url = "Common/User/getOwner.do";
                    baseParams = {
                        module : responseObjData.module[0],
                        common:'1'
                    }
                    break;
                case 2:
                    url = Wtf.req.springBase+"Product/action/getProductname.do";
                    break;
                case 3:
                    this.ruleTypeStore = this.cloneRemoteComboStore(Wtf.parentaccountstoreSearch);
                    createStore = false;
                    break;
                case 4:
                    this.ruleTypeStore = this.cloneRemoteComboStore(Wtf.contactStoreSearch);
                    createStore = false;
                    break;
            }
        }
        baseParams.hierarchy = true;
        if(createStore) {
        this.ruleTypeStore=new Wtf.data.Store({
            url: url,
            baseParams: baseParams,
            reader: new Wtf.data.KwlJsonReader({
                root:'data'
            }, Wtf.ComboReader),
            autoLoad:false
        });
        }

        if(responseObjData.fieldtype=="4") {
            this.ruleTypeCombo = new Wtf.form.ComboBox({
                id:responseObjData.recordname+this.id,
                triggerAction: 'all',
                store:this.ruleTypeStore,
                mode:'remote',
                displayField:displayField,
                fieldLabel: (responseObjData.allowblank==0)?responseObjData.fieldname+"*":responseObjData.fieldname,
                valueField:valueField,
                name:responseObjData.fieldname,
                hiddenName :responseObjData.fieldname,
                editable : createStore ? false : true,
                minChars : 2,
                triggerClass : createStore ? "" : "dttriggerForTeamLead",
                emptyText: (createStore ? WtfGlobal.getLocaleText("acc.field.Select"): WtfGlobal.getLocaleText("acc.field.Search"))+WtfGlobal.getLocaleText("acc.field.arecord"),
                allowBlank:responseObjData.allowblank,
                tpl: Wtf.comboTemplate ,
                listeners : {
                    "beforeselect": this.validateSelection,
                     scope : this
                }
            });
            Wtf.getCmp(this.id).add(this.ruleTypeCombo);
        } else if(responseObjData.fieldtype==7) {
            Wtf.getCmp(this.id).add(new Wtf.common.Select(Wtf.applyIf({
                multiSelect:true,
                forceSelection:true
            },{
                id:responseObjData.fieldlabel+this.id,
                triggerAction: 'all',
                store:this.ruleTypeStore,
                mode:'remote',
                displayField:'name',
                fieldLabel: (responseObjData.isessential==0)?responseObjData.fieldname+"*":responseObjData.fieldname,
                valueField:'id',
                hiddenName :responseObjData.fieldname,
                name: responseObjData.fieldname,
                scope: this,
                allowBlank:responseObjData.isessential,
                tpl: Wtf.comboTemplate ,
                listeners : {
                    "beforeselect": this.validateSelection,
                     scope : this
                }
            })));
        }

    },
    // Kuldeep Singh : Add TimeField
    addTimeField : function(responseObjData){

        Wtf.getCmp(this.id).add(new Wtf.form.TimeField({
            id:responseObjData.recordname+this.id,
            fieldLabel: (responseObjData.allowblank==0)?responseObjData.fieldname+"*":responseObjData.fieldname,
            name: responseObjData.fieldname,
            format:WtfGlobal.getLoginUserTimeFormat(),
            value:WtfGlobal.setDefaultValueTimefield(),
            allowBlank:responseObjData.allowblank

        }));

    },
    // Kuldeep Singh : Add Checkbox
    addCheckBox : function(responseObjData){

        Wtf.getCmp(this.id).add(new Wtf.form.Checkbox({
            id:responseObjData.recordname+this.id,
            fieldLabel: (responseObjData.allowblank==0)?responseObjData.fieldname+" *":responseObjData.fieldname,
            name: responseObjData.fieldname,
            checked :(this.refid.length==0)?false:responseObjData.configdata[0],
            scope: this
        }));
    },
    // Kuldeep Singh : Add Reference ComboBox
    addReferenceComboBox : function(responseObjData){

        var displayField = 'name';
        var valueField = 'id';
        this.refStore = new Wtf.data.Store({
            url: 'Common/CRMManager/getComboData.do',
            baseParams:{
                comboname:responseObjData.comboname,
                common:'1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root:'data'
            },  Wtf.ComboReader),
            autoLoad:false
        });

        this.refCombo = new Wtf.form.ComboBox({
            id:responseObjData.recordname+this.id,
            triggerAction: 'all',
            store:this.refStore,
            mode:'remote',
            displayField:displayField,
            fieldLabel: (responseObjData.allowblank==0)?responseObjData.fieldname+"*":responseObjData.fieldname,
            valueField:valueField,
            name:responseObjData.fieldname,
            hiddenName :responseObjData.fieldname,
            editable :false,
            allowBlank:responseObjData.allowblank,
            tpl: Wtf.comboTemplate ,
            listeners : {
                "beforeselect": this.validateSelection,
                 scope : this
            }
        });
        Wtf.getCmp(this.id).add(this.refCombo);
    },
    // Kuldeep Singh : Add Campaign Type ComboBox
    addCampaignTypeComboBox : function (campaignTypeFlag){

        var url = 'Common/CRMManager/getComboData.do';
        var baseParams = {};

        var displayField = 'name';
        var valueField = 'id';

        baseParams = {
            comboname:["Campaign Type"],
            common:'1'
        };

        baseParams.hierarchy = true;
        this.ruleTypeStore=new Wtf.data.Store({
            url: url,
            baseParams: baseParams,
            reader: new Wtf.data.KwlJsonReader({
                root:'data'
            }, Wtf.ComboReader),
            autoLoad:false


        });

        this.ruleTypeCombo = new Wtf.form.ComboBox({
            id:"quick_insert_campaign_typeid"+this.id,
            triggerAction: 'all',
            store:this.ruleTypeStore,
            mode:'remote',
            //anchor:"98%",
           // style:"width:105%",
            //listWidth:130,
            displayField:displayField,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Type*"),
            valueField:valueField,
            name:"Type",
            hiddenName :"Type",
            editable :false,
            allowBlank:false,
            tpl: Wtf.comboTemplate ,
            listeners : {
                "beforeselect": this.validateSelection,
                 scope : this
            }
        });
        Wtf.getCmp(this.id).add(this.ruleTypeCombo);

        Wtf.getCmp(this.id).doLayout();

        this.setEmailCampaignAsDefault(this.ruleTypeStore);

    },
    setEmailCampaignAsDefault:function(CampaignStore) {
        CampaignStore.on("load",function(store){
            var i = store.find("mainid","b0e71040-b46d-4fc0-bfe3-1fccca96016f");
            if(i>=0){
                var rec = store.getAt(i);
                Wtf.getCmp("quick_insert_campaign_typeid"+this.id).setValue(rec.get("id"));
            }

        },this);
        CampaignStore.load();
    },
    addMoveToLeadTextField : function(){

        this.text =new Wtf.ux.TextField({
            id:Wtf.move_to_lead_fname+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.FirstName"),
            name: "First Name",
            scope: this,
            //anchor:"98%",
            maxLength:255
        });

        Wtf.getCmp(this.id).add(this.text);

        this.text1 =new Wtf.ux.TextField({
            id:Wtf.move_to_lead_lname+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.LastName*"),
            name: "Last Name",
            scope: this,
            //anchor:"98%",
            maxLength:255,
            allowBlank:false
        });

        Wtf.getCmp(this.id).add(this.text1);

        this.text2 =new Wtf.ux.TextField({
            id:Wtf.move_to_lead_email+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.common.email"),
            name: "Email",
            scope: this,
            //anchor:"98%",
            vtype:'email',
            maxLength:100,
            allowBlank:false
        });

        Wtf.getCmp(this.id).add(this.text2);

    }
});
