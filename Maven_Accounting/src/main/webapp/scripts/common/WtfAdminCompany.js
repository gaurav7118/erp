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
Wtf.common.AdminCompany = function(config){
    Wtf.apply(this, config);
    Wtf.common.AdminCompany.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.common.AdminCompany,Wtf.Panel,{
    onRender: function(config) {
        Wtf.common.AdminCompany.superclass.onRender.call(this, config);
        chktimezoneload();
        chkcountryload();
        chkstateload();
        this.companyRecTmp = new Wtf.data.Record.create([
            {name: 'featureid'},
            {name: 'featurename'},
            {name: 'subscriptiondate'},
            {name: 'expdate'},
            {name: "subscribed"}
        ]);
        this.dataReader = new Wtf.data.KwlJsonReader({
            root: "data"
        },this.companyRecTmp);
        this.groupingView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: false,
            enableGroupingMenu: false,
            hideGroupedColumn: false
        });
        this.subscriptionds = new Wtf.data.GroupingStore({
            reader: this.dataReader,
            url: 'admin.jsp?action=0&mode=11&companyid='+ companyid,//mode=0 is user
            method : 'GET',
            sortInfo: {
                field: "subscribed",
                direction: "asc"
            }
        });
        //this.subscriptionds.load();
        this.holidaysRec = Wtf.data.Record.create([
            { name: "day", mapping: 'holiday', type:'date'},
            { name: 'description', mapping: 'description'}
        ]);
        var subscriptionRec = Wtf.data.Record.create([
            { name: "modulename", mapping: 'modulename' },
            { name: 'status', mapping: 'status'}
        ]);
        this.holidaysStore = new Wtf.data.Store({
            url: Wtf.req.base+"UserManager.jsp",
//            id: "companyHolidays",
            autoLoad: true,
            baseParams: {
                mode: 19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, this.holidaysRec)
        });
        this.subscriptionStore = new Wtf.data.Store({
            url: Wtf.req.json+"subs.json",
            autoLoad: true,
            baseParams: {
                action: 0,
                mode: 23,
                companyid: companyid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, subscriptionRec)
        });
        this.subscriptionStore.load();
        this.holidaysStore.on("load", function(){
            new Wtf.form.DateField({
                renderTo: 'holidayDatePick',
                allowBlank:false,
                format: WtfGlobal.getDateFormat(),
                emptyText : WtfGlobal.getLocaleText("acc.field.SelectDate"),
                id: 'holidayDateField'
            });
        });
//        this.notifystore = null;
//        this.notificationReq();
        var seperator = {
            border: false,
            html: '<hr style = "width: 75%;margin-left: 10px">'
        };
        var defConf = {ctCls: 'fieldContainerClass',labelStyle: 'font-size: 11px; text-align: right;'};
        this.companyDetailsPanel = new Wtf.form.FormPanel({
            id: 'companyDetailsForm',
            url:Wtf.req.base+"UserManager.jsp",
            fileUpload: true,
            baseParams:{mode:21},
            cls: 'adminFormPanel',
            autoScroll: true,
            border: false,
            items: [{
                layout: 'column',
                border: false,
                items:[{
                    columnWidth: 0.49,
                    border: false,
                    items: [({
                        id:'compfieldset',
                        xtype : 'fieldset',
                        disabledClass: 'companyFieldSet-disable',
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText("acc.pdf.15"),
                        defaultType: 'textfield',
                        autoHeight: true,
                        items:[{
                            labelStyle: 'font-size: 11px; text-align: right;',
                            name: 'logo',
                            id: 'logoFileDialog',
                            inputType: "file",
                            ctCls: 'fieldContainerClass',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Logo"),
                            width: 200
                        },{
                            xtype : 'panel',
                            border: false,
                            id:'compLogoinfo',
                            html:"Recommended image size 130 X 25"
                        },{
                            xtype : 'panel',
                            border: false,
                            html: "<img id='displaycompanylogo' style='margin-left: 120px; margin-top: 20px;' src = ''>"
                        }]
                    }), seperator, {
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        defaults : defConf,
                        title: WtfGlobal.getLocaleText("acc.field.CompanyDetails"),
                        defaultType: 'textfield',
                        autoHeight: true,
                        items:[{
                            id: 'nameField',
                            name: 'companyname',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Name"),
                            allowBlank:false,
                            maxLength : 100,
                            width: 200
                        },{
                            id: 'domainField',
                            name: 'domainname',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Subdomain"),
                            allowBlank:false,
                            invalidText :'Alphabets and numbers only, 1-32 characters',
                            maxLength: 32,
                            minLength: 1,
                            validator: WtfGlobal.validateUserid,
                            width: 200
                        },{
                            xtype : 'textarea',
                            id: 'addressField',
                            name: 'address',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Address"),
                            maxLength : 1024,
                            height: 80,
                            width: 200
                        },{
                            id: 'cityField',
                            name: 'city',
                            maxLength : 50,
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.City"),
                            width: 200
                        },{
                            id: 'stateField',
                            name: 'state',
                            maxLength : 50,
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.State"),
                            width: 200
                        },{
                            xtype:'numberfield',
                            id: 'zipField',
                            name: 'zip',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.ZipCode"),
                            maxLength : 50,
                            width: 200
                        },{
                            xtype:'combo',
//                            typeAhead: true,
                            editable: false,
                            selectOnFocus:true,
                            emptyText:WtfGlobal.getLocaleText("acc.field.Selectacountry"),
                            autoCreate:{tag: "input", type: "text", size: "24", autocomplete: "on"},
                            fieldLabel : WtfGlobal.getLocaleText("acc.field.Country"),
                            hiddenName : 'country',
                            store : Wtf.countryStore,
                            displayField:'name',
                            valueField:'id',
                            mode: 'local',
                            triggerAction: 'all',
                            id: 'countryField',
                            allowBlank:false,
                            width: 200
                        }]
                    }, seperator ,{
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        defaultType: 'textfield',
                        title: WtfGlobal.getLocaleText("acc.field.CompanyInformation"),
                        defaults : defConf,
                        autoHeight: true,
                        items:[/*{
                            id: 'maxUserField',
                            name: 'maxusers',
                            disabled: true,
                            fieldLabel: 'Users  ',
                            width: 200
                        },*/{
                            id: 'maxProjectField',
                            disabled: true,
                            name: 'maxprojects',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Projects"),
                            width: 200
                        }]
                    }, seperator ,{
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText("acc.profile.timeZone"),
                        defaults : defConf,
                        autoHeight: true,
                        items:[{
                            xtype:'combo',
                            allowBlank: false,
                            emptyText:WtfGlobal.getLocaleText("acc.field.Selectatimezone"),
                            typeAhead: true,
                            forceSelection: true,
                            triggerAction: 'all',
                            id: 'timezoneField',
                            store:Wtf.timezoneStore ,
                            displayField: "name",
                            valueField:'id',
                            mode: 'local',
                            editable: false,
                            hiddenName: 'timezone',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectTime-Zone"),
                            height: 80,
                            width: 200
                        }]
                    }, seperator, {
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
                        defaults : defConf,
                        autoHeight: true,
                        items:[this.currencyfield = new Wtf.form.ComboBox({
                            allowBlank: false,
                            emptyText:WtfGlobal.getLocaleText("acc.field.Selectcurrency"),
                            typeAhead: true,
                            forceSelection: true,
                            triggerAction: 'all',
                            id: 'currencyField',
                            store:this.currencystore,
                            displayField: "currencyname",
                            valueField:'currencyid',
                            mode: 'local',
                            editable: false,
                            hiddenName: 'currency',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectCurrency"),
                            height: 80,
                            width: 200
                        })]
                    }]
                },{
                    columnWidth: 0.49,
                    border: false,
                    items:[
                        {
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        title: WtfGlobal.getLocaleText("acc.field.ContactInformation"),
                        defaultType: 'textfield',
                        defaults : defConf,
                        autoHeight: true,
                        items:[{
                            id: 'phoneField',
                            validator: this.validateContact,
                            name: 'phone',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.PhoneNumber"),
                            maxLength : 16,
                            width: 200
                        },{
                            id: 'faxField',
                            validator: this.validateContact,
                            name: 'fax',
                            maxLength : 50,
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.FaxNumber"),
                            width: 200
                        },{
                            id: 'websiteField',
                            vtype: 'url',
                            name: 'website',
                            fieldLabel: WtfGlobal.getLocaleText("acc.field.Website"),
                            maxLength : 50,
                            width: 200
                        },{
                            id: 'emailField',
                            name: 'mail',
                            validator:WtfGlobal.validateEmail,
                            maxLength : 50,
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.EmailAddress"),
                            invalidText:WtfGlobal.getLocaleText("acc.field.PleaseenteravalidEmailAddress"),
                            width: 200
                        }]
                    }, seperator ,{
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        id: "CompanyHolidays",
                        title: WtfGlobal.getLocaleText("acc.field.CompanyHolidays"),
                        autoHeight: true,
                        items:[{
                             layout:'form',
                             border:false,
                             items:[{
                                border:false,
                                html:'<span style="display: none;font-size: 10px;color:red;" id="addInfoMsg">please click update to make changes permanent</span>'
                            }]
                        },{
                            xtype : 'hidden',
                            hidden: true,
                            id: "CompanyHolidaysHidden",
                            name: "holidays"
                        },{
                            xtype:"panel",
                            layout:'form',
                            autoDestroy: true,
//                            id: "CompanyAddHolidayPanel",
                            border: false,
                            id: 'newHolidayPanel',
                            html: '<div style="display: block; padding-top:5px;" id="addHoliday">' +
                                '<input type="text" style="float: left;" id="holidayDesc" maxlength="512">' +
                                '<div id="holidayDatePick" style="float: left; width: 110px; padding-left:5px;"></div>' +
                                '<img src="../../images/check16.png" class="holidayDelete" onclick= \'addHoliday("' + this.id + '")\' title=\'Add Holiday\'></div>'
                        },{
                            xtype: 'dataview',
                            id: 'holidaysDataView',
                            itemSelector: "CompanyHolidays",
                            tpl: new Wtf.XTemplate('<div class="listpanelcontent"><tpl for=".">{[this.f(values)]}</tpl></div>', {
                                f: function(val){
                                    return "<div id='div_"+val.day+"'><span class='holidaySpan holidayspanwidth'>" + val.description + "</span>" +
                                        "<span class='holidaySpan holidayspanwidth'>" + val.day.format(WtfGlobal.getDateFormat())+ "</span>" +
                                        "<img src='../../images/Delete.png' class='holidayDelete' onclick=\"deleteHoliday(this,'" + this.scope.id + "')\" id='del_"+val.day+"' title='Delete Holiday'>"+
                                        "<div><span class='holidayDiv'></span></div></div>";
                                },
                                scope: this
                            }),
                            store: this.holidaysStore,
                            emptyText: '<span class="holidayspan"> There are no public holidays set for this company </span>'
                        }]
                    }/*,{
                        border: false,
                        html: '<hr style = "width: 75%;margin-left: 10px">'
                    },{
                        xtype : 'hidden',
                        hidden: true,
                        id: "notifyconfHidden",
                        name: "notifyconf"
                    },{
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        id: "notification"+this.id,
                        title: 'Notification Settings',
                        autoHeight: true,
                        items:[{
                             layout:'form',
                             border:false,
                             items:[{
                                border:false,
                                html:'<span style="display: none;font-size: 10px;color:red;" id="addInfoMsg">please click update to make changes permanent</span>'
                            }]
                        },{
                            xtype:"panel",
                            autoScroll:false,
                            id : 'notifyheader'+this.id,
                            autoDestroy: true,
                            border: false//,
//                            html: "<div class='listpanelcontent' style ='font-weight:bold;'>"+
//                                    "<span class='holidaySpan' style = 'width:150px;'></span>" +
//                                    "<span class='holidaySpan' style = 'width:90px;'></span></div>"
                        },{
                            xtype: 'dataview',
                            id: 'notificationDataView'+this.id,
                            itemSelector: "CompanyHolidays",
                            tpl: new Wtf.XTemplate('<div class="listpanelcontent"><tpl for=".">{[this.f(values)]}</tpl></div>', {
                                f: function(val){
                                    var rethtml = "<br/><br/><div id='div_"+val.nid+"'>"+
                                        "<span class='holidaySpan' style = 'width:110px;'>" + val.name + "</span>";
                                        for(var fieldcnt = 0; fieldcnt <  Wtf.getCmp(this.scope.id).notifytype.length; fieldcnt++) {
                                            var fieldname = Wtf.getCmp(this.scope.id).notifytype[fieldcnt].typeid;
                                            rethtml += "<span class='holidaySpan' style = 'width:90px;'><input type='Checkbox' class='checkboxclick' "+(val[fieldname] == true? "checked" : "")
                                                +" ONCLICK=onNotifyChange(this,'"+this.scope.id+"','notify_"+val.nid+"_"+fieldname+"') id = 'notify_"+val.nid+"_"+fieldname+"'/></span>";
                                        }
                                        rethtml += "</div>";
                                        return rethtml;
                                },
                                scope: this
                            }),
                            store: this.notifystore,
                            emptyText: '<span class="holidayspan">There are no public holidays set for this company</span>'
                        }]
                    }*/, seperator ,{
                        xtype : 'fieldset',
                        cls: "companyFieldSet",
                        id: "compnyModuleSubscription",
                        title: WtfGlobal.getLocaleText("acc.field.ModuleSubscription"),
                        autoHeight: true,
                        items:[{
                            xtype: 'dataview',
                            itemSelector: "compnyModuleSubscription",
                            tpl: new Wtf.XTemplate('<div class="listpanelcontent"><tpl for=".">{[this.f(values)]}</tpl></div>', {
                                f: function(val){
                                    var id = "contDiv_" + val.modulename;
                                    var rethtml = "<div class = 'sibContDiv WAIT' id='" + id + "'><div class='subscriptionDiv'>"
                                        + val.modulename + "</div> <div class = 'statusDiv'>" + val.status + "</div>";
                                    if(val.status == "Subscribed"){
                                        rethtml = "<div class = 'sibContDiv SUB' id='" + id + "'><div class='subscriptionDiv'>" + val.modulename +
                                            "</div><div class = 'statusDiv'>" + val.status +
                                            "</div><a href='#' onclick=\"subscribeModule('" + id + "')\" class='sublink'>Unscribe</a>";
                                    } else if(val.status == "Unsubscribed"){
                                        rethtml = "<div class = 'sibContDiv UNS' id='" + id + "'><div class='subscriptionDiv'>" + val.modulename +
                                            "</div><div class = 'statusDiv'>" + val.status +
                                            "</div><a href='#' onclick=\"subscribeModule('" + id + "')\" class='sublink'>Subscribe</a>";
                                    }
                                    return rethtml + "</div>";
                                },
                                scope: this
                            }),
                            store: this.subscriptionStore,
                            emptyText: '<span class="holidayspan">Getting subscription data.</span>'
                        }]
                    }]
                }]
            }]
        });
//        if(WtfGlobal.EnableDisable(Wtf.UPerm.Company, Wtf.Perm.Company.ChangeCompanyLogo)){
//            Wtf.getCmp("compfieldset").disable();
//        }
        Wtf.getCmp("nameField").on("change", function(){
            Wtf.getCmp("nameField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("nameField").getValue()));
        }, this);
        Wtf.getCmp("addressField").on("change", function(){
            Wtf.getCmp("addressField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("addressField").getValue()));
        }, this);
        Wtf.getCmp("cityField").on("change", function(){
            Wtf.getCmp("cityField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("cityField").getValue()));
        }, this);
        Wtf.getCmp("stateField").on("change", function(){
            Wtf.getCmp("stateField").setValue(WtfGlobal.HTMLStripper(Wtf.getCmp("stateField").getValue()));
        }, this);
        Wtf.getCmp("websiteField").on("change", function(){
            var validateURL = WtfGlobal.HTMLStripper(Wtf.getCmp("websiteField").getValue());
            if(validateURL!="") {
                if(validateURL.indexOf("http://") != 0 && validateURL.indexOf("https://") != 0 && validateURL.indexOf("ftp://") != 0)
                    validateURL = "http://"+validateURL;
            }
            Wtf.getCmp("websiteField").setValue(validateURL);
        }, this);
        var btnArr=[];
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.companyadmin, Wtf.Perm.companyadmin.edit))
        btnArr.push({
            text: "Update",
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.save),
            handler: this.updateCompany
          //  iconCls: "pwnd updatecompanydetails"
        });
        var detailPanel = new Wtf.Panel({
            layout: "border",
            border: false,
            bodyStyle: "background-color: #ffffff;",
            bbar:btnArr,
            items: [{
                border: false,
                region: 'center',
                autoScroll: true,
                items: [ this.companyDetailsPanel ]
            }/*,{
                border: false,
                height: 200,
                region: 'south',
                items: [new Wtf.grid.GridPanel({
                    id:'subscriptiongrid'+this.id,
                    store: this.subscriptionds,
                    cm: this.gridcm,
                    border : false,
                    loadMask : true,
                    view: this.groupingView,
                    enableColumnHide: false
               })]
            }*/]
        })
        this.add(detailPanel);
        Wtf.Ajax.requestEx({
            url:Wtf.req.base+"UserManager.jsp",
            params:{
                mode:18
            }},
            this,
            function(request, response){
                var res = request;
                if(res && res.data){
                    this.doLayout();
                    this.fillData(res.data[0]);
                    var hdate = Wtf.getCmp("holidayDateField");
                    if(hdate)
                        hdate.destroy();
                } else {
                    //msgBoxShow(17, 1);
                    this.companyDetailsPanel.disable();
                }
                bHasChanged=true;
            },
            function(){
                bHasChanged=false;
                //msgBoxShow(17, 1);
                this.companyDetailsPanel.disable();
            }
        );
    },
    deleteHoliday: function(day){
        Wtf.get("addInfoMsg").dom.style.display = "block";
        var rec = this.holidaysStore.getAt(this.holidaysStore.find("day", day));
        this.holidaysStore.remove(rec);
        Wtf.getCmp('holidaysDataView').refresh();
    },
    addHoliday: function(){
        var desc = Wtf.get("holidayDesc").dom.value;
        desc = desc.substring(0,510);
        desc = WtfGlobal.HTMLStripper(desc);
        if(desc.trim().length>0) {
            var day = Wtf.getCmp("holidayDateField").getValue();
            if(day) {
                if(this.searchInHolilist(day.format('Y-m-d'))) {
                    var rec = new this.holidaysRec({
                        "day": day,
                        "description": desc
                    });
                    this.holidaysStore.insert(this.holidaysStore.getCount(), rec);
                    Wtf.getCmp('holidaysDataView').refresh();
                    Wtf.get("addInfoMsg").dom.style.display = "block";
                    Wtf.get("holidayDesc").dom.value = "";
                    Wtf.getCmp("holidayDateField").reset();
                }
                else
                     WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Date '"+day.format('Y-m-d')+WtfGlobal.getLocaleText("acc.field.isalreadymarkedasaholiday.")],2);
            } else
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasegiveavaliddate")],2);
        } else
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleasegiverequireddata")],2);
    },

    searchInHolilist : function(newDate) {
        for(var cnt =0 ; cnt<this.holidaysStore.getCount();cnt++) {
            if(this.holidaysStore.getAt(cnt).data.day == newDate)
                return false;
        }
        return true;
    },
    validateContact: function(value) {
        var regex = "^\\+?(\\d{1,4})(-| )?(\\d{1,4})(-| )?(\\d{4,10})$";
        if(value.match(regex) != null)
            return true;
        else {
            this.invalidText = WtfGlobal.getLocaleText("acc.field.Pleaseenteravalidnumber");
            return false;
        }
    },
    updateCompany: function(){
        var arr=[];
        for(var k = 0; k < this.holidaysStore.getCount(); k++){
            var rec=this.holidaysStore.getAt(k);
            arr.push('{day:"'+WtfGlobal.convertToGenericDate(rec.data['day'])+'",description:"'+rec.data['description']+'"}');
        }
        Wtf.getCmp("CompanyHolidaysHidden").setValue('['+arr.join(',')+']');

        Wtf.getCmp("nameField").validate();
        Wtf.getCmp("domainField").validate()
        if(Wtf.getCmp("nameField").isValid() && Wtf.getCmp("domainField").isValid()) {
            if(this.companyDetailsPanel.getForm().isValid()==false) return;
            this.companyDetailsPanel.getForm().submit({
                scope: this,
                success: function(result,action){
                     var resultObj = eval('('+action.response.responseText+')');
                     Wtf.get("addInfoMsg").dom.style.display = "none";
                     document.getElementById('companyLogo').src = "../../images/store/?company=true&"+Math.random();
                     document.getElementById('displaycompanylogo').src = "../../images/store/?company=true&"+Math.random();
                     Wtf.CurrencySymbol = this.currencystore.getAt(this.currencystore.find('currencyid',this.currencyfield.getValue())).data.htmlcode;
                     this.genSuccessResponse(resultObj);
                },
                failure: function(frm, action){
                    var resObj = eval( "(" + action.response.responseText + ")" );
                    this.genFailureResponse(resObj);
                }
            });
        }
    },

    genSuccessResponse:function(response){
        if(response.success==true)
            updatePreferences();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.CompanyManagement"),response.msg],response.success*2+1);
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    resetAll: function(){
        //alert("reserAll");
    },

    fillData: function(resObj){
       // var imagePath = (resObj.image!="")?resObj.image:'../../images/deskeralogo.png';
      //  Wtf.get('domainField').dom.parentNode.innerHTML+="<span style='color:gray !important;font-size:11px !important;'>.deskera.com</span><br><span style='color:gray !important;font-size:10px !important;font-style:italic !important;'>Letters and numbers only — no spaces.<span>";
        Wtf.getCmp("timezoneField").setValue(resObj.timezone);
        Wtf.getCmp("nameField").setValue(resObj.companyname);
        Wtf.getCmp("addressField").setValue(resObj.address);
        Wtf.getCmp("cityField").setValue(resObj.city);
        Wtf.getCmp("stateField").setValue(resObj.state);
        Wtf.getCmp("zipField").setValue(resObj.zip);
        Wtf.getCmp("countryField").setValue(resObj.country);
        Wtf.getCmp("phoneField").setValue(resObj.phone);
        Wtf.getCmp("faxField").setValue(resObj.fax);
//        Wtf.getCmp("maxUserField").setValue(resObj.maxusers);
        Wtf.getCmp("websiteField").setValue(resObj.website);
        Wtf.getCmp("emailField").setValue(resObj.emailid);
        Wtf.getCmp("maxProjectField").setValue(resObj.maxprojects);
        Wtf.getCmp("currencyField").setValue(resObj.currency);
        Wtf.getCmp("domainField").setValue(resObj.subdomain);
        document.getElementById('displaycompanylogo').src = "../../images/store/?company=true&"+Math.random();
    }});

