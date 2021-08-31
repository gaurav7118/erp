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
function callSetUpWizardOnScriptLoad(){
    new Wtf.Window({
        title: WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"), //"Getting Started Wizard",
        id : 'welcomeSetUpWizard',
        closable: false,
        iconCls:'accountingbase',
        modal: true,
        width: 690,
        height: 280,
        resizable: false,
        buttonAlign: "right",
        renderTo: document.body,
        html:"<div style='font-size:12px;padding:50px;'>"+
        WtfGlobal.getLocaleText("acc.setupWizard.dear")+" "+(_fullName.split(" ")[0])+",<br/><br/>"+
        WtfGlobal.getLocaleText("acc.setupWizard.text1")+"<br/><br/>"+
        WtfGlobal.getLocaleText("acc.setupWizard.text2")+
        "</div>",
        buttons: [
        {
            text: WtfGlobal.getLocaleText("acc.setupWizard.msg2"),  //"Skip Setup",
            tooltip:WtfGlobal.getLocaleText("acc.setupWizard.msg2"),
            hidden:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ||Wtf.account.companyAccountPref.countryid==Wtf.Country.US),
            handler: function(){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"), WtfGlobal.getLocaleText("acc.setupWizard.defaultsetting.msg"),function(btn){
                    if(btn=="yes") {
                        saveDefaultCompanySetUp();
                    }
                });
            }
        },
        {
            text: WtfGlobal.getLocaleText("acc.common.continueBtn"),  //"Continue",
            handler: function(){
                callSetUpWizardWindow();
                Wtf.getCmp("welcomeSetUpWizard").close();
            }
        }
        ]
    }).show();
    return;
}
function saveDefaultCompanySetUp() {

    var windowsize = Wtf.getBody().getViewSize();
    
    this.companyDetailsFieldSet = new Wtf.form.FieldSet({
        border:false,
        bodyBorder:false,
        autoHeight: true,
        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.setupWizard.compaydetails")+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.compaydetails")+"</span>" 
    });
    this.accountGroupFieldSet = new Wtf.form.FieldSet({
        collapsible: true,
        autoHeight: true,
        border:false,
        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.setupWizard.note38")+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.note38")+"</span>" 
    });
    this.accountFieldSet = new Wtf.form.FieldSet({
        collapsible: true,
        autoHeight: true,
        border:false,
        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.setupWizard.note33")+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.note33")+"</span>" 
    });
    this.defaultExchangeRateFieldSet = new Wtf.form.FieldSet({
        collapsible: true,
        autoHeight: true,
        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.setupWizard.surrexrates")+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.surrexrates")+"</span>" 
    });
    this.DefaultGSTTaxFieldSet = new Wtf.form.FieldSet({
        collapsible: true,
        autoHeight: true,
        border:false,
        title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.setupWizard.taxDetails")+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.taxDetails")+"</span>" 
    });
    
    this.defaultSetUpWin = new Wtf.Window({
        title: WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"),
        id: 'defaultcompanysetupwin',
        closable: false,
        iconCls: 'accountingbase',
        modal: true,
        bodyStyle: "background:#f1f1f1;padding:5px",
        width: windowsize.width - 900,
        height: windowsize.height - 200,
        resizable: false,
        buttonAlign: "right",
        autoScroll: true,
        renderTo: document.body,
        border:false,
        items:[this.companyDetailsFieldSet,this.accountGroupFieldSet,this.accountFieldSet,this.defaultExchangeRateFieldSet,this.DefaultGSTTaxFieldSet],
        buttons: [
            {
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                tooltip: WtfGlobal.getLocaleText("acc.setupWizard.msg2"),
                hidden:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ||Wtf.account.companyAccountPref.countryid==Wtf.Country.US),
                handler: function() {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.setupWizard.note23"), function(btn) {
                        if (btn == "yes") {
                            var currencyDatails = "";
                            var ToDateForExchangeRate = "";
                            var date = new Date();
                            var FinancialStartDate = new Date(date.getFullYear(), 0, 1);

                            for (var i = 0; i < Wtf.defaultCurrencyExchangeDet.length; i++) {
                                if (Wtf.defaultCurrencyExchangeDet[i].tocurrencyid == Wtf.defaultCurrencyExchangeDet[i].fromcurrencyid) {
                                    Wtf.defaultCurrencyExchangeDet[i].todate = new Date(5000, 12, 31);
                                    continue;
                                }
                                var toDate = FinancialStartDate;                  // To Date for Exchange Rate
                                ToDateForExchangeRate = toDate.add(Date.DAY, 30);
                                Wtf.defaultCurrencyExchangeDet[i].applydate = FinancialStartDate;
                                Wtf.defaultCurrencyExchangeDet[i].todate = ToDateForExchangeRate;
                            }

                            if (Wtf.defaultCurrencyExchangeDet.length > 0) {
                                for (var i = 0; i < Wtf.defaultCurrencyExchangeDet.length; i++) {
                                    currencyDatails += "{erid:\"" + Wtf.defaultCurrencyExchangeDet[i].id + "\"," +
                                            "tocurrency:\"" + Wtf.defaultCurrencyExchangeDet[i].tocurrency + "\"," +
                                            "tocurrencyid:\"" + Wtf.defaultCurrencyExchangeDet[i].tocurrencyid + "\"," +
                                            "fromcurrencyid:\"" + Wtf.defaultCurrencyExchangeDet[i].fromcurrencyid + "\"," +
                                            "exchangerate:\"" + encodeURI(Wtf.defaultCurrencyExchangeDet[i].exchangerate) + "\"," +
                                            "foreigntobaseexchangerate:\"" + encodeURI(Wtf.defaultCurrencyExchangeDet[i].foreigntobaseexchangerate) + "\"," +
                                            "applydate:\"" + WtfGlobal.convertToGenericDate(new Date(Wtf.defaultCurrencyExchangeDet[i].applydate).clearTime()) + "\"," +
                                            "todate:\"" + WtfGlobal.convertToGenericEndDate(new Date(Wtf.defaultCurrencyExchangeDet[i].todate).clearTime()) + "\"},";
                                }
                                if (currencyDatails.length > 0) {
                                    currencyDatails = currencyDatails.substring(0, currencyDatails.length - 1);
                                }
                                currencyDatails = "[" + currencyDatails + "]";

                                var setUpData = "companyTypeId:\"defaultaccount\",addDefaultAccount:\"Yes\",addDefaultAccountType:\"Yes\",withInventory:\"Yes\",";
                                setUpData += "yearStartDate:\"" + WtfGlobal.convertToGenericDate(FinancialStartDate) + "\",";
                                setUpData += "bookStartDate:\"" + WtfGlobal.convertToGenericDate(FinancialStartDate) + "\",";
                                setUpData += "countryid:\"" + Wtf.account.companyAccountPref.countryid + "\",";
                                setUpData += "stateid:\"" + Wtf.account.companyAccountPref.stateid + "\",";
                                setUpData += "currencyid:\"" + Wtf.account.companyAccountPref.currid + "\",";
                                setUpData += "currencyDetails:" + currencyDatails + ",";
                                setUpData += "taxDetails:[],bankDetails:[],lockDetails:[],withTax1099:\"No\"";
                                setUpData = "{" + setUpData + "}";

                                WtfGlobal.setAjaxTimeOut();
                                Wtf.Ajax.requestEx({
                                    url: "ACCCompanySetup/DefaultCompanySetUp.do",
                                    params: {
                                        data: setUpData
                                    }
                                },
                                this,
                                        function(response) {
                                            if (response.success) {
                                                Wtf.getCmp("tabdashboard").load({
                                                    url: "ACCDashboard/getDashboardData.do",
                                                    params: {
                                                        refresh: false,
                                                        start: 0,
                                                        limit: 10
                                                    },
                                                    scripts: true
                                                });
                                                if (Wtf.getCmp("defaultcompanysetupwin")) {
                                                    Wtf.getCmp("defaultcompanysetupwin").close();
                                                }
                                                getCompanyAccPref();
                                            }else{
                                                if (response.msg) {
                                                    var message = response.msg;
                                                    var title = WtfGlobal.getLocaleText("acc.common.alert");
                                                    Wtf.MessageBox.show({
                                                        title: title,
                                                        msg: message,
                                                        buttons: Wtf.MessageBox.OK,
                                                        icon: Wtf.MessageBox.ERROR
                                                    });

                                                }
                                                 WtfGlobal.resetAjaxTimeOut();
                                            }
                                           
                                        },
                                        function(response) {
                                            WtfGlobal.resetAjaxTimeOut();
                                        });

                                if (Wtf.getCmp("welcomeSetUpWizard")) {
                                    Wtf.getCmp("welcomeSetUpWizard").close();
                                }
                            }

                        }
                    }, this);
                }
            },
            {
                text: "Cancel",
                scope: this,
                handler: function() {
                    this.defaultSetUpWin.close();
                }
            }
        ]
    });
    this.defaultSetUpWin.show();
    this.companyDetailsFieldSet.body.dom.innerHTML = getCompanyDetails();
    this.accountGroupFieldSet.body.dom.innerHTML = getDefaultAccountGroupDetails();
    this.accountFieldSet.body.dom.innerHTML = getDefaultAccountDetails();
    this.defaultExchangeRateFieldSet.body.dom.innerHTML = getDefaultExchangeRateDetails();
    this.DefaultGSTTaxFieldSet.body.dom.innerHTML = getDefaultGSTTaxDetails();
}
function getCompanyDetails() {
    var date = new Date();
    var DefaultFinancialStartDate = new Date(date.getFullYear(), 0, 1);
    var previwHTML =
            "<div style='font-size:11px;'>" +
            "<div>" + WtfGlobal.getLocaleText("acc.setupWizard.note13") + "</div><br/>" +
            "<div>" + WtfGlobal.getLocaleText("acc.setupWizard.note14") + "</div><br/>" + "</div>"

    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText("acc.rem.213") + "</b></span>: " + Wtf.account.companyAccountPref.countryname + "</div>";
    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText("acc.rem.214") + "</b></span>: " + Wtf.account.companyAccountPref.currencyname + "</div>";
    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText("acc.setupWizard.FinYrDate") + "</b></span>: " + DefaultFinancialStartDate.format(WtfGlobal.getOnlyDateFormat()) + "</div>";
    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText("acc.setupWizard.BookBeginingDate") + "</b></span>: " + DefaultFinancialStartDate.format(WtfGlobal.getOnlyDateFormat()) + "</div>";
    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText("acc.setupWizard.indType") + "</b></span>: " + "Others" + "</div>";

    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText("acc.setupWizard.invPref") + "</b></span>: ";
    previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note34");  //"With Inventory"; bydefault inventory will be on,user can change it if he wants
    previwHTML += "</div>";
    if (Wtf.account.companyAccountPref.countryid == "244") {
        previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText("acc.setupWizard.tax1099Pref") + "</b></span>: ";
//        if (Wtf.account.companyAccountPref.countryid == "244") {              // ERP-22346
//            previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note36");  //"With Tax 1099";
//        } else {
            previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note37");  //"Without Tax 1099";
//        }
        previwHTML += "</div>";
    }
    previwHTML += "</div>";
    return previwHTML;
}

function getDefaultAccountGroupDetails() {

    var previwHTML =
            "<div style='font-size:11px;'>" + "</div>"
    var rowStyle = "width: 20%; display: block; float: left; overflow:hidden; padding-left:3px;";
    var rowHeaderStyle = rowStyle + " border-bottom: 1px solid #E8E8E8;";

    var defaultAccountGroupDetails = "";

    if (Wtf.defaultAccountGroups.length > 0) {
        defaultAccountGroupDetails = "<span style='" + rowStyle + " width: 10px;'>&nbsp;</span>" +
                "<span style='" + rowHeaderStyle + " width: 40px;'>" +"<b>"+ WtfGlobal.getLocaleText("acc.setupWizard.sno")+"</b>" + "</span>" +
                "<span style='" + rowHeaderStyle + "'>" + "<b>"+WtfGlobal.getLocaleText("acc.coa.gridAccountType")+"</b>"  + "</span>" +
                "<span style='" + rowHeaderStyle + "'>" + "<b>"+WtfGlobal.getLocaleText("acc.accPref.nature")+"</b>"  + "</span>" +
                "<span style='" + rowHeaderStyle + "'>" + "<b>"+WtfGlobal.getLocaleText("acc.coa.gridAffectsGrossProfit") +"</b>" + "</span>" +
                "<br style='clear:both'/>";

        for (var i = 0; i < Wtf.defaultAccountGroups.length; i++) {
            defaultAccountGroupDetails += "<span style='" + rowStyle + " width: 10px;'>&nbsp;</span>" +
                    "<span style='" + rowStyle + " width: 40px;'>" + (i + 1) + ".&nbsp;</span>" +
                    "<span style='" + rowStyle + " '>" + Wtf.defaultAccountGroups[i].groupname + "&nbsp;</span>" +
                    "<span style='" + rowStyle + " '>" + Wtf.defaultAccountGroups[i].naturename + "&nbsp;</span>" +
                    "<span style='" + rowStyle + " '>" + Wtf.defaultAccountGroups[i].affectgp + "&nbsp;</span>" +
                    "<br style='clear:both'/>";

        }
        defaultAccountGroupDetails = "<br/><div style='padding-left:40px'>" + defaultAccountGroupDetails + "</div>";
    } else {
        defaultAccountGroupDetails = " " + WtfGlobal.getLocaleText("acc.setupWizard.note12");
    }
    previwHTML += "<div class='accDefSWdata'>" + defaultAccountGroupDetails + "</div>";
    previwHTML += "</div>";
    return previwHTML;
}
function getDefaultAccountDetails(){
    
    var previwHTML =
            "<div style='font-size:11px;'>" + "</div>"
    
    var accountrowStyle1 = "width: 20%; display: block; float: left; overflow:hidden; padding-left:90px;";
    var rowStyle = "width: 20%; display: block; float: left; overflow:hidden; padding-left:3px;";
    var rowHeaderStyle = rowStyle + " border-bottom: 1px solid #E8E8E8;";
    var defaultAccountDetails = "";
    
    if (Wtf.defaultAccountDetails.length > 0) {

        defaultAccountDetails = "<span style='" + rowStyle + " width: 10px;'>&nbsp;</span>" +
                "<span style='" + rowHeaderStyle + " width: 40px;'>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.sno") +"</b>"+ "</span>" +
                "<span style='" + rowHeaderStyle + "'>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.accName")+"</b>" + "</span>" +
                "<span style='" + accountrowStyle1 + "'>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.accType") +"</b>"+ "</span>" +
                "<br style='clear:both'/>";

        for (var i = 0; i < Wtf.defaultAccountDetails.length; i++) {
            defaultAccountDetails += "<span style='" + rowStyle + " width: 10px;'>&nbsp;</span>" +
                    "<span style='" + rowStyle + " width: 40px;'>" + (i + 1) + ".&nbsp;</span>" +
                    "<span style='" + rowStyle + " width:30%'>" + Wtf.defaultAccountDetails[i].name + "&nbsp;</span>" +
                    "<span style='" + rowStyle + " width:30%'>" + Wtf.defaultAccountDetails[i].groupname + "&nbsp;</span>" +
                    "<br style='clear:both'/>";

        }

        defaultAccountDetails = "<br/><div style='padding-left:40px'>" + defaultAccountDetails + "</div>";

    } else {
        defaultAccountDetails = " " + WtfGlobal.getLocaleText("acc.setupWizard.note12");
    }
    previwHTML += "<div class='accDefSWdata'>" + defaultAccountDetails + "</div>";
    previwHTML += "</div>";

    return previwHTML;
}
function getDefaultExchangeRateDetails() {

    var rowStyle = "width: 20%; display: block; float: left; overflow:hidden; padding-left:3px;";
    var rowHeaderStyle = rowStyle + " border-bottom: 1px solid #E8E8E8;";

    var previwHTML =
            "<div style='font-size:11px;'>" + "</div>"


    var currencyDatails = "";
    if (Wtf.defaultCurrencyExchangeDet.length > 0) {

        currencyDatails = "<span style='" + rowHeaderStyle + " width: 10px;'>&nbsp;</span>" +
                "<span style='" + rowHeaderStyle + " width: 40px;'>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.sno")+"</b>" + "</span>" +
                "<span style='" + rowHeaderStyle + "'>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.cur") +"</b>"+ "</span>" +
                "<span style='" + rowHeaderStyle + "'>" +"<b>"+ WtfGlobal.getLocaleText("acc.setupWizard.basetoforeignexRate") +"</b>"+ "</span>" +
                "<span style='" + rowHeaderStyle + "'>" +"<b>"+ WtfGlobal.getLocaleText("acc.setupWizard.foreigntobaseexRate") +"</b>"+ "</span>" +
                "<span style='" + rowHeaderStyle + " width:30%'>" +"<b>"+ WtfGlobal.getLocaleText("acc.setupWizard.conv") +"</b>"+ "</span>" +
                "<span style='" + rowHeaderStyle + " width:15%'>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.appDate")+"</b>" + "</span>" +
                "<br style='clear:both'/>";

        var srNo = 0;
        for (var i = 0; i < Wtf.defaultCurrencyExchangeDet.length; i++) {

            if (Wtf.defaultCurrencyExchangeDet[i].tocurrencyid == Wtf.defaultCurrencyExchangeDet[i].fromcurrencyid) {
                continue;
            }
            currencyDatails += "<span style='" + rowStyle + " width: 10px;'>&nbsp;</span>" +
                    "<span style='" + rowStyle + " width: 40px;'>" + (srNo + 1) + ".&nbsp;</span>" +
                    "<span style='" + rowStyle + "'>" + Wtf.defaultCurrencyExchangeDet[i].tocurrency + "&nbsp;</span>" +
                    "<span style='" + rowStyle + "'>" + Wtf.defaultCurrencyExchangeDet[i].exchangerate + "&nbsp;</span>" +
                    "<span style='" + rowStyle + "'>" + Wtf.defaultCurrencyExchangeDet[i].foreigntobaseexchangerate + "&nbsp;</span>" +
                    "<span style='" + rowStyle + " width:30%'>" + "1 " + Wtf.defaultCurrencyExchangeDet[i].fromcurrency + " = " + Wtf.defaultCurrencyExchangeDet[i].exchangerate + " " + Wtf.defaultCurrencyExchangeDet[i].tocurrency + "&nbsp;</span>" +
                    "<span style='" + rowStyle + " width:15%'>" + new Date(Wtf.defaultCurrencyExchangeDet[i].applydate).format(WtfGlobal.getOnlyDateFormat()) + "&nbsp;</span>" +
                    "<br style='clear:both'/>";
            srNo++;
        }
        currencyDatails = "<br/><div style='padding-left:40px'>" + currencyDatails + "</div>";
    } else {
        currencyDatails = " " + WtfGlobal.getLocaleText("acc.setupWizard.note12");
    }
    previwHTML += "<div class='accDefSWdata'>" + currencyDatails + "</div>";

    previwHTML += "</div>";

    return previwHTML;

}
function getDefaultGSTTaxDetails() {
    
    var rowStyle = "width: 20%; display: block; float: left; overflow:hidden; padding-left:3px;";
    var rowHeaderStyle = rowStyle + " border-bottom: 1px solid #E8E8E8;";

    var previwHTML =
            "<div style='font-size:11px;'>" + "</div>"
    var taxDatails = "";
    if (Wtf.account.companyAccountPref.countryid == "137" || Wtf.account.companyAccountPref.countryid == "203" || Wtf.account.companyAccountPref.countryid == "106") {
        taxDatails = "<span style='" + rowHeaderStyle + " width: 10px;'>&nbsp;</span>" +
                "<span style='" + rowHeaderStyle + " width: 40px;'>" +"<b>"+ WtfGlobal.getLocaleText("acc.setupWizard.sno") +"</b>"+ "</span>" +
                "<span style='" + rowHeaderStyle + " '>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.taxnam") +"</b>"+ "</span>" +
                "<span style='" + rowHeaderStyle + " '>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.gridTaxDescription")+"</b>" + "</span>" +
                "<span style='" + rowHeaderStyle + " width: 17%;'>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.taxcod") +"</b>"+ "</span>" +
                "<span style='" + rowHeaderStyle + " width: 12%;'>" + "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.per") +"</b>" +"</span>" +
                "<span style='" + rowHeaderStyle + " width: 15%;'>" +"<b>"+ WtfGlobal.getLocaleText("acc.setupWizard.appDate") +"</b>"+ "</span>" +
                "<br style='clear:both'/>";

        for (i = 0; i < Wtf.defaultGSTTaxDetails.length; i++) {
            taxDatails += "<span style='" + rowStyle + " width: 10px;'>&nbsp;</span>" +
                    "<span style='" + rowStyle + " width: 40px;'>" + (i + 1) + ".&nbsp;</span>" +
                    "<span style='" + rowStyle + " '>" + Wtf.defaultGSTTaxDetails[i].taxname + "&nbsp;</span>" +
                    "<span style='" + rowStyle + " '>" + Wtf.defaultGSTTaxDetails[i].taxdescription + "&nbsp;</span>" +
                    "<span style='" + rowStyle + " width: 17%;'>" + Wtf.defaultGSTTaxDetails[i].taxcode + "&nbsp;</span>" +
                    "<span style='" + rowStyle + " width: 12%;'>" + Wtf.defaultGSTTaxDetails[i].percent + "%&nbsp;</span>" +
                    "<span style='" + rowStyle + " width: 15%;'>" + new Date(Wtf.defaultGSTTaxDetails[i].applydate).format(WtfGlobal.getOnlyDateFormat()) + "&nbsp;</span>" +
                    "<br style='clear:both'/>";
        }
        taxDatails = "<br/><div style='padding-left:40px'>" + taxDatails + "</div>";
    } else {
        taxDatails = WtfGlobal.getLocaleText("acc.setupWizard.note12");  //" Details not provided, can be entered later.";
    }
    previwHTML += "<div class='accDefSWdata'>" + taxDatails + "</div>";
    previwHTML += "</div>";
    return previwHTML;
}

function callSetUpWizardWindow(){
    var panel = Wtf.getCmp("SetUpWizard");
    if(!panel){
        new Wtf.account.setUpWizard({
            id : 'SetUpWizard',
            border : false,
            //            closeAction:'hide',
            layout: 'fit',
            title:WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"), //'Getting Started Wizard',
            closable: false,
            iconCls:'accountingbase',
            modal: true,
            width: 800,
            height: 650,
            resizable: false,
            buttonAlign: "right",
            renderTo: document.body
        }).show();
        Wtf.getCmp("SetUpWizard").on("setup",function(win){
            Wtf.getCmp("tabdashboard").load({
                url:"ACCDashboard/getDashboardData.do",
                params:{
                    refresh:false,
                    start : 0,
                    limit : 10
                },
                scripts: true
            });
            getCompanyAccPref();
        },this);
    } else {
        panel.show();
    }
}

function activateWizardCard(card){
    var imgLink = document.getElementById("accWizardLinkImage"+card);
    if(imgLink.className.indexOf("img-visited") != -1) {
        var wizard = Wtf.getCmp("SetUpWizard");
        if(!wizard.isLinksClosed) {
            var lay = wizard.cardPanel.getLayout();
            var i = lay.activeItem.id.split('card-')[1];
            var current = parseInt(i);
            wizard.navigate(current, parseInt(card));
        }
    }
}
function aWizardAddCustomer(){
    var wizard = Wtf.getCmp("SetUpWizard");
    if(wizard){
        wizard.close();
    }
    callCustomerDetails(null,true,true);
}
function aWizardAddVendor(){
    var wizard = Wtf.getCmp("SetUpWizard");
    if(wizard){
        wizard.close();
    }
    callVendorDetails(null,true,true);
}
function aWizardForCompanyPref(){
    var wizard = Wtf.getCmp("SetUpWizard");
    if(wizard){
        wizard.close();
    }
    callAccountPref();
}
function copyDefaultExchangeRates(id){
    Wtf.getCmp(id).getStore().proxy.conn.url = "ACCCurrency/getDefaultCurrencyExchange.do";
    Wtf.getCmp(id).getStore().load({params:{currencyid:Wtf.getCmp('currencyrecid').getValue()}});
    if(Wtf.getCmp('move-next')){//ERP-5116
        Wtf.getCmp('move-next').setDisabled(false);//enabling next button
    }
}
Wtf.account.setUpWizard = function (config){
    this.hide1099=(WtfGlobal.getCurrencyID()!=usCurrencyID);
    this.buttons = [
                {
                    id: 'exit-setup',
                    text: WtfGlobal.getLocaleText("acc.setupWizard.msg2"),  //'Skip Setup',
                    scope:this,
                    hidden:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ||Wtf.account.companyAccountPref.countryid==Wtf.Country.US),
                    handler: function(){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.setupWizard.tabTitle"), WtfGlobal.getLocaleText("acc.setupWizard.defaultsetting.msg"),function(btn){
                            if(btn=="yes") {
                             saveDefaultCompanySetUp();
                                this.close();
                            }
                        },this);
                    }
                },
//                '->',
                {
                    id: 'move-prev',
                    text: WtfGlobal.getLocaleText("acc.setupWizard.previous"),  //'<< Previous',
                    handler: this.navHandler.createDelegate(this, [-1]),
                    hidden: true
                },
                {
                    id: 'move-next',
                    text: WtfGlobal.getLocaleText("acc.setupWizard.next"),  //'Next >>',
                    handler: this.navHandler.createDelegate(this, [1])
                }
            ]
    Wtf.apply(this,config);
    Wtf.account.setUpWizard.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.setUpWizard, Wtf.Window,{
    initComponent:function (){
        this.addEvents({
            'setup':true
        });
        Wtf.account.setUpWizard.superclass.initComponent.call(this);

        this.companyTypeID = "defaultaccount";      // Default Company type id
        this.isLinksClosed = false;                 // Enable/Disable processbar image link ==>false:enable, true:disable
        this.isTaxStoreRemoveFlag = false;                 // Enable/Disable processbar image link ==>false:enable, true:disable
        this.FinancialStartDate = new Date();       // Default financial start date
        var toDate = this.FinancialStartDate;                  // To Date for Exchange Rate
        this.ToDateForExchangeRate = toDate.add(Date.DAY, 30); // To Date for Exchange Rate
        this.BookStartDate = new Date();            // Default Book begining date
        this.getAllCards();

        this.cardsArr = [
					this.card0 = new Wtf.Panel({
					    id: 'card-0',
					    linkName: WtfGlobal.getLocaleText("acc.nee.16"),  //"Set Country, Currency & Financial Year",  "Select Industry Type",
					    border: false,
					    layout:"fit",
					    bodyStyle: 'padding:20px 100px 20px 100px',
					    items: this.countryCurrencyBody
					}),
                    this.card1 = new Wtf.Panel({
                        id: 'card-1',
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step1"),  //"Select Industry Type",
                        border: false,
                        layout:"fit",
                        bodyStyle: 'padding:20px 100px 20px 100px',
                        items: this.companyTypeBody
                    }),
                     this.card2 = new Wtf.Panel({
                        id: 'card-2',
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step21"),  //""Select Account Type"
                        bodyStyle: 'padding:10px 100px',
                        border: false,
                        layout:"fit",
                        items: this.accountBody
                    }),
                    this.card3 = new Wtf.Panel({
                        id: 'card-3',
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step2"),  //"View Account list",
                        border: false,
                        layout: "fit",
                        bodyStyle: 'padding:10px 100px',
                        items: this.defaultAccountBody
                    }),
                    this.card4 = new Wtf.Panel({
                        id: 'card-4',
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step3"),  //"Set Currency Exchange Rate",
                        border: false,
                        bodyStyle: 'padding:10px 100px',
                        layout:"fit",
//                        items: this.currencyGridPanel = new Wtf.Panel({
//                            layout: "fit",
//                            border: true,
//                            height: 250,
                            items: this.currencyBody
//                        })
                    }),
//                    this.card3 = new Wtf.Panel({
//                        id: 'card-4',
//                        border: false,
//                        layout:"fit",
//                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step4"),  //"Set Financial Year",
//                        bodyStyle: 'padding:50px 80px 50px 80px',
//                        items : this.financialYearBody
//                    }),
                    this.card5 = new Wtf.Panel({
                        id: 'card-5',
                        border: false,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step5"),  //"Set Tax Details",
                        layout: "fit",
                        bodyStyle: 'padding:10px 50px 30px 50px',
                        items: this.taxBody
                    }),
                    this.card6 = new Wtf.Panel({
                        id: 'card-6',
                        border: false,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step6"),  //"Set Bank Details",
                        layout: "fit",
                        bodyStyle: 'padding:30px 50px 30px 50px',
                        items: this.bankBody

                    }),
                    this.card7 = new Wtf.Panel({
                        id: 'card-7',
                        border: false,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step7"),  //"Set Inventory Preferences",
                        bodyStyle: 'padding:10px 300px 0 10px;overflow-y:scroll;overflow-x:hidden;',
                        items: this.inventoryPreferencesForm
                    }),
                     this.card8 = new Wtf.Panel({
                        id: 'card-8',
                        border: false,
                        hidden: (Wtf.account.companyAccountPref.countryid != '244')?true:false,   //this.hide1099,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step8"),  //"Set Tax 1099 Preferences",
                        bodyStyle: 'padding:100px 150px 0 150px',
                        items: this.tax1099PreferencesForm
                    }),
                    this.card9 = new Wtf.Panel({
                        id: 'card-9',

                        border: false,
                        linkName: WtfGlobal.getLocaleText("acc.setupWizard.step9"),  //"Confirm Setup",
                        layout: "fit",
                        autoScroll: true,
                        html: this.previewTemplate=new Wtf.XTemplate(
                                 "<div>"+WtfGlobal.getLocaleText("acc.setupWizard.note13")+"</div><br/>"
                            )
                    }),
                    this.card10 = new Wtf.Panel({
                        id: 'card-10',
//                    this.card8 = new Wtf.Panel({
//                        id: 'card-8',
                        border: false,
                        layout: "fit",
                        html: "<div style='padding-left:50px;font-size:12px'><br/><br/>"+
                                "<h1>"+WtfGlobal.getLocaleText("acc.setupWizard.note19")+"</h1><br/><br/>" +
                                "<p>"+WtfGlobal.getLocaleText("acc.setupWizard.note18")+"</p><br/><br/>" +
                                "<p>"+WtfGlobal.getLocaleText("acc.setupWizard.note20")+"</p>" +
                                "<p class='listpanelcontent' style='padding:0' >"+WtfGlobal.getLocaleText("acc.setupWizard.or") +
                                    "<br/><a wtf:qtip='Create Customer' href='#' onclick='aWizardAddCustomer()'>"+WtfGlobal.getLocaleText("acc.setupWizard.note21")+"</a>" +
                                    "<br/><a wtf:qtip='Create Vendor' href='#' onclick='aWizardAddVendor()'>"+WtfGlobal.getLocaleText("acc.setupWizard.note22")+"</a>" +
                                    "<br/><a wtf:qtip='Set System Preferences' href='#' onclick='aWizardForCompanyPref()'>"+WtfGlobal.getLocaleText("acc.setupWizard.note42")+"</a>" +
                                "</p>" +

                                "</div>"
                    })];
        this.cardPanel = new Wtf.Panel({
            region: "center",
            bodyStyle: 'background:#f1f1f1;font-size:12px;padding:10px',
            layout: "card",
            border: false,
            activeItem: 0,
            items: this.cardsArr
        });

        this.topLinkPanelHTML = this.createLinkPanel();
        this.northPanel = new Wtf.Panel({
            region: "north",
            height: 130,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: this.topLinkPanelHTML
        });

        this.wrapperPanel = new Wtf.Panel({
           layout: "border",
           items: [
               this.northPanel,
               this.cardPanel
           ]
        });

    },

    onRender: function(config){
        this.add(this.wrapperPanel);
        Wtf.account.setUpWizard.superclass.onRender.call(this, config);
    },

    navHandler: function(direction) {
//        var tax = (Wtf.getCmp('countryreccombo').getValue() != '244')?true:false;  // ERP-22346
       var lay = this.cardPanel.getLayout();
       var i = lay.activeItem.id.split('card-')[1];
       var current = parseInt(i);
       if(current==0 && direction==1){// On Set Country and currency page and clicked on next. - If country is changed from 
           this.isTaxStoreRemoveFlag=false;
           this.onCountrySelect();
       }
       var next = current + direction;
       /*
        For Indian company Setup set tax will be skipped  
         */
      if (next == 5&&this.country.getValue()=="105") {
            next += direction
        }
        if (next == 8) {
            next += direction
        }
       this.navigate(current, next);
    },

    navigate: function(current, next){
        if(this.beforeActivateCard(next)) {
            if(this.country.getValue() == "106" && next == 1 && this.npwp != "" && !this.npwp.isValid()){
            //validation for npwp no. only for Indonesia company. It is not compulsory but if you enter then enter it in required format.
                return;
            }
            if(this.country.getValue() == "105" && next == 1 && this.pan != "" && !this.pan.isValid()){
            //validation for pan no. only for India company. It is not compulsory but if you enter then enter it in required format.
                return;
            }
            var lay = this.cardPanel.getLayout();
            var last = this.cardsArr.length-1;

            Wtf.getCmp('move-prev').setVisible(!next==0);
            
            if(next==4 && Wtf.getCmp('wizardCurrencyGrid').getStore().getCount()== 0){//ERP-5116
                Wtf.getCmp('move-next').setDisabled(true);
                /**
                 * Need to refresh Currency grid ,becuase some time GRID empty text not visible.
                 * Currency grid empty text - load Currency rate link.
                 */
                Wtf.getCmp('wizardCurrencyGrid').getView().refresh();
            }else{
                Wtf.getCmp('move-next').setDisabled(false);
            }
            if(next==6 && this.country.getValue()== Wtf.Country.INDIA && Wtf.getCmp('wizardBankGrid').getStore().getCount()== 0){
                if(this.bankBody && this.bankBody.items && this.bankBody.items.items && this.bankBody.items.items[2] && this.bankBody.items.items[2].items && this.bankBody.items.items[2].items.items){
                    this.bankBody.items.items[2].items.items[0].setVisible(true);
                    this.bankBody.items.items[2].items.items[1].setVisible(false);
                }
            }else if(next==6 && this.country.getValue()!= Wtf.Country.INDIA) {
                if(this.bankBody && this.bankBody.items && this.bankBody.items.items && this.bankBody.items.items[2] && this.bankBody.items.items[2].items && this.bankBody.items.items[2].items.items){
                    this.bankBody.items.items[2].items.items[0].setVisible(true);
                    this.bankBody.items.items[2].items.items[1].setVisible(false);
                }
            }
            this.isLinksClosed = false;
            this.updateLinkState(current,"visited");
            if(next<last) {
                lay.setActiveItem(next);
                this.onActivateCard(next);
                Wtf.getCmp('move-next').setText(next==last-1?WtfGlobal.getLocaleText("acc.common.saveBtn"):WtfGlobal.getLocaleText("acc.setupWizard.next"));
            } else if(next==last){ //Save
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.setupWizard.note23"),function(btn){
                    if(btn=="yes") {
                        this.saveSetup(lay, next);
                    }
                },this);
            } else if(next>last){ //Finish
                this.close();
            }

            if((next==0 && !this.typesm.hasSelection())
                || (next==7)){
                Wtf.getCmp('move-next').setDisabled(false);
                this.isLinksClosed = true;
            }
            if((next==0 && !this.typesm.hasSelection())
                || (next==8 && this.tax1099PreferencesForm.getForm().getValues().withTax1099==undefined)){
                Wtf.getCmp('move-next').setDisabled(true);
                this.isLinksClosed = true;
            }
            if(next==5)
                this.taxCode.setValue("");
            this.taxName.setValue("");
            this.taxDescription.setValue("");
            this.taxPercent.setValue("");
            if(this.gstcmbAccount){
                this.gstcmbAccount.setValue("");
            }
            if(this.TaxTypeCombo){
                this.TaxTypeCombo.setValue(0);
            }
            this.taxForm.getForm().clearInvalid();
            if(next==6)
                this.bankName.setValue("");
            this.ifsccode.setValue("");
            this.micrcode.setValue("");
            this.accountName.setValue("");
            this.bankAccountNo.setValue("");
            this.bankBalance.setValue(0);
            this.bankForm.getForm().clearInvalid();
            this.hideColumnOfBankRpt=new Array();
            this.hideColumnOfBankRpt.push('micrcode');    
            this.hideColumnOfBankRpt.push('ifsccode');    
            this.hideColumnOfBankRpt.push('bankbranchname');    
            this.hideColumnOfBankRpt.push('bankbranchaddress');    
            this.hideColumnOfBankRpt.push('branchstate');    
            this.hideColumnOfBankRpt.push('bsrcode');    
            this.hideColumnOfBankRpt.push('pincode');
            for(var i=0;i<this.hideColumnOfBankRpt.length;i++){
                var rowtaxindex=this.bankGrid.getColumnModel().findColumnIndex(this.hideColumnOfBankRpt[i]);
                this.bankGrid.getColumnModel().setHidden( rowtaxindex, this.country.getValue()!=Wtf.Country.INDIA) ;
            }  
            this.bankGrid.getView().refresh();
            if(this.country.getValue()!=Wtf.Country.INDIA){ 
                WtfGlobal.hideFormElement(this.ifsccode);
                WtfGlobal.hideFormElement(this.micrcode);
                this.mailingDetailsSetup.hide();
            }else{
                WtfGlobal.showFormElement(this.ifsccode);
                WtfGlobal.showFormElement(this.micrcode);
                this.mailingDetailsSetup.show();
            }
        }
    },

    saveSetup: function(lay, next){
         this.loadingMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.49")
        });
        this.loadingMask.show();
        this.isLinksClosed = true;
        Wtf.getCmp('move-prev').setDisabled(true);
        Wtf.getCmp('move-next').setDisabled(true);

        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url: "ACCCompanySetup/SetupCompany.do",
            params:{
                data:this.getSetUpData()
            }
        },
        this,
        function(response){
             this.loadingMask.hide();
            if(response.success){
                lay.setActiveItem(next);
                this.onActivateCard(next);
                Wtf.getCmp('move-prev').setVisible(false);
                Wtf.getCmp('exit-setup').setVisible(false);
                Wtf.getCmp('move-next').setText(WtfGlobal.getLocaleText("acc.setupWizard.finish"));
                Wtf.getCmp('move-next').setDisabled(false);
                this.isLinksClosed = true;
                WtfGlobal.resetAjaxTimeOut();
                this.fireEvent("setup");

                Wtf.pref.Currencyid = response.currency.Currencyid;
                Wtf.pref.CurrencyName = response.currency.CurrencyName;
                Wtf.pref.CurrencySymbol = response.currency.CurrencySymbol;
                Wtf.pref.Currency = response.currency.Currency;
            } else {
                this.isLinksClosed = false;
                Wtf.getCmp('move-prev').setDisabled(false);
                Wtf.getCmp('move-next').setDisabled(false);
                var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                if(response.msg)msg="Failed to save company setup<br/>"+response.msg;
                this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),msg,Wtf.MessageBox.ERROR);
                WtfGlobal.resetAjaxTimeOut();
            }
        },
        function(response){
            this.isLinksClosed = false;
            Wtf.getCmp('move-prev').setDisabled(false);
            Wtf.getCmp('move-next').setDisabled(false);
            var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),msg,Wtf.MessageBox.ERROR);
            WtfGlobal.resetAjaxTimeOut();
        });
    },

    createLinkPanel: function(){
        var links = new Array();
        for(var i=0; i<this.cardsArr.length; i++){
            if(this.cardsArr[i].linkName!=undefined && this.cardsArr[i].linkName!=""){
                links.push(this.cardsArr[i].linkName);
            }
        }
        var topLinkPanelHTML = "<div style=\"padding:5px\">&nbsp;</div>";
        var totalLinks = links.length;
        var totalWidth = this.width-15;
        var linkWidth = Math.floor(totalWidth/totalLinks);
        for(i=0; i<totalLinks; i++){
            topLinkPanelHTML += "<div id='accWizardLink"+i+"' class='accWizardLink' style=\"width:"+linkWidth+"px;\">" +
                                    "<div class='awl-image-bg'>" +
                                       "<div id='accWizardLinkImage"+i+"' class='awl-image"+(i==0?' img-current':'')+"' onclick=\"activateWizardCard('"+i+"');\">&nbsp;</div>" +
                                    "</div>" +
                                    "<div style=\"height:5px\"></div>" +
                                    "<div id='accWizardLinkText"+i+"' class='awl-text"+(i==0?' text-current':'')+"'>" +links[i] + "</div>" +
                                "</div>";
        }
        return topLinkPanelHTML;
    },

    beforeActivateCard: function(card){
        var process = true;
        if(card==1){ // set default company type as Others/None
            if(!this.typesm.hasSelection()){
//                this.showMessage("Alert","Please select <b>Others/None</b> as your Industry Type");
//                process = false;
                var defaultIndex = this.typeStore.find("id", "defaultaccount");
                this.typesm.selectRow(defaultIndex);
            }
        } else if(card==5){
            var fyval= this.getBeginingYearDate(true);
            var bbval= this.getBookBeginingDate();
            process = fyval & bbval;
        }
        return process;
    },

    onActivateCard: function(card){
        this.updateLinkState(card,"current");
        Wtf.getCmp("card-"+card).doLayout();
        if(card==1){
//            if(this.typesm.hasSelection()){
//                var rec = this.typesm.getSelected();
//                if(this.companyTypeID != rec.data.id){
//                    this.companyTypeID = rec.data.id;
//                    this.defaultAccountStore.load({params:{companyType:this.companyTypeID,country:this.country.getValue(),state:this.state.getValue()}});
//                }
//            } else {
//                this.defaultAccountStore.load({params:{companyType:this.companyTypeID,country:this.country.getValue(),state:this.state.getValue()}});
//            }
        } else if (card==2){
                if(this.typesm.hasSelection()){
                var rec = this.typesm.getSelected();
                if(this.companyTypeID != rec.data.id){
                    this.companyTypeID = rec.data.id;
                    this.defaultAccountStore.load({params:{companyType:this.companyTypeID,country:this.country.getValue(),state:this.state.getValue()}});
                }
            } else {
                this.defaultAccountStore.load({params:{companyType:this.companyTypeID,country:this.country.getValue(),state:this.state.getValue()}});
            }
//            this.card2.doLayout();
//            alert(this.defaultAccountForm.getForm().getValues());
        } else if (card==3){

        }else if (card==4){
//           this.currencyGrid.store.load({params:{currencyid:Wtf.getCmp('currencyrecid').getValue()}});//to refresh the grid with new financial year change
        } 
        else if (card==5){
            this.taxApplyDate.setValue(this.FinancialStartDate);
            /*var selectedCountry=Wtf.getCmp('countryreccombo').getValue();//LOADING TAX STORE ACCORDING TO SELECTED COUNTRY WITH CHANGED FINANCIAL YEAR DATE
            if(selectedCountry==203 || selectedCountry==137 || selectedCountry==106){//Singapore || Malasiya || Indonesion
                if(selectedCountry==137||selectedCountry==106){//Malasiya
                    this.taxStore.load({
                        params:{
                            countryid:selectedCountry,
                            financialYrStartDate:WtfGlobal.convertToGenericDate(this.FinancialStartDate)
                        }
                    });
                }else{
                    this.taxStore.load();//Singapore
                }
            }*/
        } else if (card==6){
            this.bankApplyDate.setValue(this.FinancialStartDate);
        } else if (card==7){
//            alert(this.getBankDetails());
        } else if (card==8){
////            alert(this.getBankDetails());
        }else if (card==9){
//            alert(this.getPreviewTemplate());
            if(this.typesm.hasSelection()){
                rec = this.typesm.getSelected();
                if(this.companyTypeID != rec.data.id){
                    this.companyTypeID = rec.data.id;
                }
            }
            this.card9.body.dom.innerHTML = this.getPreviewTemplate();
        }
    },

    updateLinkState: function(card, state) {
        var ImgLink = document.getElementById("accWizardLinkImage"+card);
        if(ImgLink){
            var ImgLinkClass = ImgLink.className;
            var textLink = document.getElementById("accWizardLinkText"+card);
            var textLinkClass = textLink.className;

            if(state=="visited"){
                ImgLinkClass = ImgLinkClass.replace("img-current", "");
                ImgLinkClass += " img-visited";
                textLinkClass = textLinkClass.replace("text-current", "");
                textLinkClass += " text-visited";
            } else if(state=="current") {
                ImgLinkClass = ImgLinkClass.replace("img-visited", "");
                ImgLinkClass += " img-current";
                textLinkClass = textLinkClass.replace("text-visited", "");
                textLinkClass += " text-current";
            } else {
                ImgLinkClass = ImgLinkClass.replace("img-visited", "");
                ImgLinkClass = ImgLinkClass.replace("img-current", "");
                textLinkClass = textLinkClass.replace("text-visited", "");
                textLinkClass = textLinkClass.replace("text-current", "");
            }
            ImgLink.className = ImgLinkClass;
            textLink.className = textLinkClass;
        }
    },


    showMessage: function(title, message, icon) {
        Wtf.MessageBox.show({
            title: title,
            msg: message,
            buttons: Wtf.MessageBox.OK,
            icon: icon==undefined ? Wtf.MessageBox.INFO : icon
        });
    },

    getAllCards: function() {

        /*--------Load default GST Taxes for singapur country only------------*/
        this.taxRec = new Wtf.data.Record.create([
            {name: 'taxid'},
            {name: 'taxname'},
            {name: 'taxtype', type:'int'},
            {name: 'taxdescription'},
            {name: 'taxcode'},
            {name: 'gstaccountname'},
            {name: 'gstaccountid'},
            {name: 'salestaxaccountname'},
            {name: 'salestaxaccountid'},
            {name: 'mastertypevalue'},
            {name: 'percent', type:'float'},
            {name: 'applydate', type:'date'},
            {name: 'isEditing'}
        ]);

        this.taxStore = new Wtf.data.Store({
        url : "ACCTax/getDefaultGSTTax.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            baseParams:{},
            totalProperty:"count"
        },this.taxRec)
    });
    
    /*
     * Added countryid of United States(244) for loading Default taxes
     */
   if(Wtf.account.companyAccountPref.countryid==203 || Wtf.account.companyAccountPref.countryid==137 || Wtf.account.companyAccountPref.countryid==106|| Wtf.account.companyAccountPref.countryid==244){//For Indonesia,Singapore,United states and Malasiya
        this.taxStore.load({
            params:{
                countryid:Wtf.account.companyAccountPref.countryid,
                financialYrStartDate:WtfGlobal.convertToGenericDate(this.FinancialStartDate)
            }
        });
    }


//------------------- Country and currency ---------------------------//

    	this.currencystorerec = Wtf.data.Record.create ([
    	                                        {name: 'currencyid'},
    	                                        {name: 'name'},
    	                                ]);
    	                                this.currencyrecStore=new Wtf.data.Store({
    	                                    url: "ACCCurrency/getCurrency.do",
    	                                    reader: new Wtf.data.KwlJsonReader({
    	                                        root: "data"
    	                                    },this.currencystorerec)
    	                                });
    	this.currencyrecStore.load();
    	Wtf.countryStore.load();

    	this.country = new Wtf.form.ComboBox({
            store: Wtf.countryStore,
//            name:'taxStoreid',
            width:160,
            id:'countryreccombo',
            listWidth:150,
            labelWidth:80,
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.200"),
            displayField:'name',
            valueField:'id',
            value:Wtf.account.companyAccountPref.countryid,
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            disabled: Wtf.defaultReferralKeyflag,
            emptyText: WtfGlobal.getLocaleText("acc.rem.203"),
            selectOnFocus:true,
            forceSelection: true,
            listeners: {
                scope: this,
                select : function (combo, record, index) {
                     this.accStore.load();
                    if(record.data.id ==  Wtf.Country.INDIA || record.data.id == Wtf.Country.US) {
                        this.isMultiEntityCheckbox.setValue(true);
                        this.isMultiEntityCheckbox.setDisabled(true);
                    } else {
                        this.isMultiEntityCheckbox.setValue(false);
                        this.isMultiEntityCheckbox.setDisabled(false);
                    }
                }
            } // If India OR US country is selected then, this check will be automatically selected and disabled
        });
    	this.state = new Wtf.form.ComboBox({
            store: Wtf.stateStore,
            width:160,
            id:'statereccombo',
            listWidth:150,
            labelWidth:80,
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.253"),
            displayField:'name',
            valueField:'id',
            value:Wtf.account.companyAccountPref.state,
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            emptyText: WtfGlobal.getLocaleText("acc.rem.254"),
            selectOnFocus:true,
            forceSelection: true
        });


    	this.currency = new Wtf.form.ComboBox({
            store: this.currencyrecStore,
            id:'currencyrecid',
            width:160,
            listWidth:150,
            labelWidth:80,
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.201"),
            displayField:'name',
            valueField:'currencyid',
            value:Wtf.account.companyAccountPref.currid,
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            disabled: Wtf.defaultReferralKeyflag,
            emptyText: WtfGlobal.getLocaleText("acc.rem.204"),
            selectOnFocus:true,
            forceSelection: true
        });
        
        this.vat =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.vat"),
            id:"setupwiz"+"vat",
            width : 160,
            maxLength:11,
            regex:/\d{10}[a-z | A-Z | 0-9 ]{1}/, 
            invalidText :'The value in this field is invalid',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenter") +" "+WtfGlobal.getLocaleText("acc.setupwizard.vat")
        });      
        this.cst =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.cst"),
            id:"setupwiz"+"cst",
            width : 160,
            maxLength:11,
            regex:/\d{10}[a-z | A-Z | 0-9 ]{1}/, 
            invalidText :'The value in this field is invalid',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenter") +" "+WtfGlobal.getLocaleText("acc.setupwizard.cst")
        });      
        this.pan =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.pan"),
            id:"setupwiz"+"pan",
            width : 160,
            maxLength:10,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum",
            regex:/[A-Z]{5}\d{4}[A-Z]/, //[a-z]{3}[cphfatblj][a-z]\d{4}[a-z]/i,
            regexText:'Invalid PAN eg."AAAAA1234A"',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenter") +" "+WtfGlobal.getLocaleText("acc.setupwizard.pan")
        });      
        //In back end, saving npwp no. as pan no.
        this.npwp =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.npwp"),
            id:"setupwiz"+"npwp",
            width : 160,
            maxLength: 20,
            invalidText :'Numbers only',
            regex:/\d{2}\.\d{3}\.\d{3}\.\d{1}[-.]\d{3}\.\d{3}/,
            regexText:'Invalid NPWP No. (eg."01.567.505.1-056.000")',
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenter") +" "+WtfGlobal.getLocaleText("acc.setupwizard.npwp")
        });      
        this.service =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.service"),
            id:"setupwiz"+"service",
            width : 160,
            maxLength:15,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum",
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenter") +" "+WtfGlobal.getLocaleText("acc.setupwizard.service")
        });      
        this.tan =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.tan"),
            id:"setupwiz"+"tan",
            width : 160,
            maxLength:10,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum",
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenter") +" "+WtfGlobal.getLocaleText("acc.setupwizard.tan")
        });      
        this.ecc =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.ecc"),
            id:"setupwiz"+"ecc",
            width : 160,
            maxLength:15,
            invalidText :'Alphabets and numbers only',
            vtype : "alphanum",
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenter") +" "+WtfGlobal.getLocaleText("acc.setupwizard.ecc")
        });      
        this.registrationTypeCombo=new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.india.ExciseDutyRegistrationType") +"*",
            store:Wtf.registrationTypeStore,
            name:'registrationType',
            id:'registrationType'+this.id,
            width:145,
            listWidth:145,
            labelWidth:80,
            valueField:'id',
            mode:'local',
            displayField:'name',
            forceSelection: true,
            triggerAction: 'all',
            allowBlank:false,
            value:Wtf.registrationType,
            selectOnFocus:true,
            emptyText:WtfGlobal.getLocaleText("acc.setupwizard.india.SelectExciseDutyRegistrationType")
        });
        this.GSTIN =new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin')+WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.india.vecdorcustomer.column.gstininfo")),
            width : 160,
            name:'GSTIN',
            value:Wtf.GSTIN,
            regex:/[A-Z | 0-9]{10}\d{3}[A-Z]{2}/,
            hidden:true,
            hideLabel:true,
            invalidText:"Invalid GSTIN",
            emptyText: "Please enter "+WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin')
        });
        this.onlyBaseCurrency=new Wtf.form.Checkbox({                               
            fieldLabel:WtfGlobal.getLocaleText("acc.field.showonlybasecurrency"),
            labelStyle:'width: 160px;',
            style:'margin-top: 5px; position: relative;',
            name:'onlyBaseCurrency',             
            checked:Wtf.account.companyAccountPref.onlyBaseCurrency
        });
        
    	this.currencyrecStore.on('load',function(){
            this.currency.setValue(Wtf.account.companyAccountPref.currid);
            WtfGlobal.hideFormElement(this.enableGST);
            this.gstcmbAccount.allowBlank=true;
            WtfGlobal.hideFormElement(this.gstcmbAccount);
        },this);
    	Wtf.countryStore.on('load',function(){
            this.country.setValue(Wtf.account.companyAccountPref.countryid);
            if(this.country.getValue()!=Wtf.Country.INDIA){
                WtfGlobal.hideFormElement(this.state);
            }else{
                Wtf.stateStore.load({
                    params:{
                        countryid: this.country.getValue()!= undefined ? this.country.getValue() :"" 
                    }
                });
                /**
                 * Old INDIA Compliance fields visible on new company setup
                 * Please check more details ERP-35391
                 */
                WtfGlobal.hideFormElement(this.state);
                this.IndiaCountrySpecificNumbers.hide();
            }
        },this);
        
    	Wtf.stateStore.on('load',function(){this.state.setValue(Wtf.account.companyAccountPref.state);},this);
    	this.currency.on('select',function(){
    		if(Wtf.getCmp('wizardCurrencyGrid').getStore().getCount() != 0) {
    			copyDefaultExchangeRates('wizardCurrencyGrid');
    		}
    	},this);

    	this.country.on('select',function(combo,selRec){
            this.isTaxStoreRemoveFlag=true;
            if(selRec.data && selRec.data.id && selRec.data.id==Wtf.Country.INDIA){
                this.IndiaCountrySpecificNumbers.hide();
                WtfGlobal.showFormElement(this.ifsccode);
                WtfGlobal.showFormElement(this.micrcode);
                Wtf.stateStore.load({
                    params:{
                        countryid: this.country.getValue()!= undefined ? this.country.getValue() :"" 
                    }
                });
            } else{
                WtfGlobal.hideFormElement(this.ifsccode);
                WtfGlobal.hideFormElement(this.micrcode);
                WtfGlobal.hideFormElement(this.state);
                this.IndiaCountrySpecificNumbers.hide();
            }
            if(selRec.data && selRec.data.id && selRec.data.id==Wtf.Country.INDONESIA){
                this.IndonesiaCountrySpecificNumbers.show();
            }else{
                this.IndonesiaCountrySpecificNumbers.hide();
            }
            this.onCountrySelect();
    	},this);
        
        this.state.on('select',function(combo,selRec){
            var selectedCountry=Wtf.getCmp('countryreccombo').getValue();
            var selectedState=selRec.data.id;
            this.taxStore.load({
                params:{
                    countryid:selectedCountry,
                    stateid:selectedState,
                    financialYrStartDate:WtfGlobal.convertToGenericDate(this.FinancialStartDate)
                }
            });
    	},this);

//    	this.countryCurrencyForm = new Wtf.form.FormPanel({
//            border: false,
//            region: "center",
//            layout: "form",
//            autoScroll: true,
//            style: "background:#f1f1f1;",
//            defaults: {labelWidth:180,border:false},
//            items : [
//                new Wtf.form.FieldSet({
//                        cls: "wiz-card3-fieldset",
//                        width: 400,
//                        autoHeight:true,
//                        title:"Country and Currency Settings",  //'Financial Year Settings',
//                        items:[this.country, this.currency]
//
//
//                ]
//        });




//    	this.countryCurrencyBody = new Wtf.Panel({
//            border: false,
//            layout: "border",
//            items: [
//                this.countryCurrencyForm,
//                new Wtf.Panel({
//                    region:"south",
//                    heigth:20,
//                    border:false,
//                    style: "background:#f1f1f1;padding-top:2px",
//                    html:"<div style='font-size: 12px; padding-top:2px;'>"+
//                    "Note: The following 2 points will be applicable when you set Country and Currency from Setup Wizard <br/>"+
//                    " - Country cannot be changed for Accounting application through apps <br/>"+
//                    " - Currency cannot be changed for Accounting application apps <br/>"+
//                        "</div>"
//                }),
//                this.financialYearBody
//                ]
//        });









//------------------- Country and currency ---------------------------//

//------------------- Card 0 ---------------------------//
        this.typeRec = Wtf.data.Record.create ([
                {name: 'id'},
                {name: 'name'},
                {name: 'details'}
        ]);
        this.typeStore=new Wtf.data.Store({
            url: "ACCCommon/getCompanyTypes.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.typeRec)
        });
        this.typeStore.load();
        this.typeStore.on("load",function(store, recs, option){
            if(this.typeStore.getCount()>0){
                var defaultIndex = this.typeStore.find("id", this.companyTypeID);
                this.typesm.selectRow(defaultIndex);
//                Wtf.getCmp('move-next').setDisabled(true);
//                this.isLinksClosed = true;
            }
        },this);

        this.typesm = new Wtf.grid.RadioSelectionModel();//{singleSelect:true});
        this.typecm = new Wtf.grid.ColumnModel([
            this.typesm,
            {
                header: WtfGlobal.getLocaleText("acc.setupWizard.indType"),  //"Industry Type",
                sortable:true,
                dataIndex: 'name'
            }
        ]);
        this.typesm.on("selectionchange",function(sm){
            if(this.typesm.hasSelection()){
//                this.companyTypeID = this.typesm.getSelected().data.id;
                Wtf.getCmp('move-next').setDisabled(false);
                this.isLinksClosed = false;
            } else {
                Wtf.getCmp('move-next').setDisabled(true);
                this.isLinksClosed = true;
            }
        },this);
        this.companyTypeGrid = new Wtf.grid.GridPanel({
            region:"center",
            cm:this.typecm,
            store:this.typeStore,
//            height:350,
            sm:this.typesm,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("acc.setupWizard.note24")  //"Industry types are not available"
            }),
            loadMask:true
        });


        this.companyTypeBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.companyTypeGrid,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.text3")+"</div>"
                })
                ]
        });

//        this.CompanyTypeGridBody = new Wtf.Panel({
//            border: false,
//            layout: "border",
//            items: [
//                this.companyTypeGrid,
//                new Wtf.Panel({
//                    region: "south",
//                    heigth: 100,
//                    border: false,
//                    layout: "form",
//                    style: "background:#f1f1f1;padding:15px 0 0 0",
//                    title: "Note",
//                    items: new Wtf.form.FieldSet({
//                            border: false,
//                            title: "Note",
//                            autoHeight: true,
//
//                            items: new Wtf.Panel({
//                                border: false,
//                                html: "If your industry type is different then above shown list, select \"Others/None\""
//                            })
//                        })
//                })
//                ]
//        });

//------------------- Card 1 <View Account List>---------------------------//

        this.defaultAccountRec = Wtf.data.Record.create ([
                {name: 'id'},
                {name: 'name'},
                {name: 'groupname'},
                {name: 'companytype'}
        ]);
        this.defaultAccountStore=new Wtf.data.Store({
            url: "ACCAccount/getDefaultAccount.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.defaultAccountRec)
        });
        this.defaultAccountStore.load({params:{companyType:this.companyTypeID,country:Wtf.account.companyAccountPref.countryid,state:this.state.getValue()}});
        Wtf.grid.RowNumberer.prototype.width= 29;
        this.defaultAccountcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.setupWizard.accName"),  //"Account Name",
                sortable:true,
                dataIndex: 'name',
                regex:Wtf.specialChar
            },
            {
                header: WtfGlobal.getLocaleText("acc.coa.gridAccountType"),  //"Group Type",
                sortable:true,
                dataIndex: 'groupname'
            }
        ]);
        this.defaultAccountsGrid = new Wtf.grid.GridPanel({
            region: "center",
            border: true,
            cm:this.defaultAccountcm,
            store:this.defaultAccountStore,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("acc.setupWizard.note25")  //"Account list is not available"
            }),
            loadMask:true
        });

        this.defaultAccountForm = new Wtf.form.FormPanel({
            region: "south",
            heigth: 120,
            border: false,
            style: "background:#f1f1f1;padding:15px 0 0 0",
            labelWidth:10,
            items : new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.setupWizard.question1"),  //'Do you want to use Account List shown above?',
                autoHeight: true,
                border: false,
                cls: "wiz-card6-fieldset",
                defaultType: 'radio',
                items: [
                    this.withAccList = new Wtf.form.Radio({
                        checked: true,
                        fieldLabel: '',
                        labelSeparator: '',
                        boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.listSelect1"), //'Yes, I will start with these accounts and will edit them later.',
                        name: 'addDefaultAccount',
                        inputValue: "Yes"
                    }),
                    this.withOutAcclist = new Wtf.form.Radio({
                        ctCls: "fieldset-item",
                        fieldLabel: '',
                        labelSeparator: '',
                        boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.listSelect2"), //'No, I will create my own accounts later.',
                        name: 'addDefaultAccount',
                        inputValue: "No"
                    })]
                 
                 })
        });

        this.defaultAccountBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.defaultAccountsGrid,
                this.defaultAccountForm
                ]
        });
//------------------- Card 2 <Select account types>---------------------------//
            this.accRec = new Wtf.data.Record.create([
        {
            name: 'groupid'
        },{
            name: 'groupname'
        },{
            name: 'nature'
        },{
            name: 'affectgp'
        },{
            name: 'parentid'
        },{
            name: 'isMasterGroupD'
        }
        ]);
        this.accStore = new Wtf.data.Store({
             baseParams:{
              defaultgroup:true
             },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.accRec),
            url:"ACCAccount/getGroups.do"
        });

        /*
         Groups will be loaded according to country
          */
       this.accStore.on('beforeload',function(){
           this.accStore.baseParams.country =this.country.getValue();
        }, this);
  
        this.accCm= new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
           {
                header:WtfGlobal.getLocaleText("acc.coa.gridAccountType"),  //"Group Name",
                dataIndex:'groupname',
                autoWidth : true
            },{
                header:WtfGlobal.getLocaleText("acc.coa.gridNature"),  //"Nature",
                dataIndex:'nature',
                width:110,
                align:'right',
                renderer:this.natureRenderer

            },{
                header:WtfGlobal.getLocaleText("acc.coa.gridAffectsGrossProfit"),  //"affectgp",
                width:200,
                dataIndex:'affectgp',
                renderer:WtfGlobal.boolRenderer("Yes", "No")
            },{
                header:WtfGlobal.getLocaleText("acc.rem.43"),  //"parentid",
                width:200,
                hidden:true,
                dataIndex:'parentid'

            }]);
        this.accgrid = new Wtf.grid.EditorGridPanel({
            id:"wizardaccGrid",
            region:"center",
            autoScroll:true,
            clicksToEdit:1,
            store: this.accStore,
            cm: this.accCm,
            loadMask : true,
            border: true,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("account.common.nodatadisplay")
            })
        });
        this.defaultAcc = new Wtf.form.FormPanel({
            region: "south",
            heigth: 120,
            border: false,
            style: "background:#f1f1f1;padding:15px 0 0 0",
            labelWidth:10,
            items : new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.setupWizard.question11"),  //'Do you want to use Default Group Type as shown above?',
                autoHeight: true,
                border: false,
                cls: "wiz-card6-fieldset",
                defaultType: 'radio',
                items: [
                this.withAccType= new Wtf.form.Radio({
                    checked: true,
                    hideLabel:true,
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.listSelect11"),  //'Yes, I will start with these accounts and will edit them later.',
                    name: 'addDefaultAccountType',
                    inputValue: "Yes"
                }),        
                this.withOutAccType= new Wtf.form.Radio({
                    ctCls:"fieldset-item",
                    hideLabel:true,
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.listSelect12"),  //'No, I will create my own accounts later.',
                    name: 'addDefaultAccountType',
                    inputValue: "No"
                })]
            })
        });
        this.withOutAccType.on("change",function(){
            if((this.defaultAcc.getForm().getValues().addDefaultAccountType)=="No"){
                this.withOutAcclist.setValue(true);
                this.withAccList.setValue(false);
                this.withAccList.disable();
                this.withOutAcclist.disable();
                 this.defaultAccountStore.load();
                this.defaultAccountsGrid.getView().refresh();
            } 
        },this);
        this.withAccType.on("change",function(){
            if((this.defaultAcc.getForm().getValues().addDefaultAccountType)=="Yes"){
                this.defaultAccountBody.enable();
                this.withOutAcclist.setValue(false);
                this.withAccList.setValue(true);
                this.withAccList.enable();
                this.withOutAcclist.enable();
                this.defaultAccountStore.load({params:{companyType:this.companyTypeID,country:Wtf.account.companyAccountPref.countryid,state:this.state.getValue()}});
                this.defaultAccountsGrid.getView().refresh();
            }
        },this);
        this.accountBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
            this.accgrid,
            this.defaultAcc
            ]
        });

//------------------- Card 2 <Set Currency Exchange Rate>---------------------------//
        this.currencyRec = new Wtf.data.Record.create([
            {
                name: 'id'
            },{
                name: 'applydate', type:'date'
            },{
                name: 'fromcurrency'
            },{
                name: 'tocurrency'
            },{
                name: 'exchangerate', type:'float'
            },{
                name: 'tocurrencyid'
            },{
                name: 'fromcurrencyid'
            },{
                name: 'companyid'
            },{
                name: 'todate', type:'date'
            },{
                name: 'foreigntobaseexchangerate', type:'float'
            }
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url: "ACCCurrency/getCurrencyExchangeList.do"
        });
        this.currencyStore.load();
        this.currencyStore.on("load",function(store){
            this.currencyStore.filterBy(function(rec){
                if(rec.data.tocurrencyid==rec.data.fromcurrencyid){
                    rec.data.todate = new Date(5000,12,31);
                    return false
                }
                this.FinancialStartDate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
                var toDate = this.FinancialStartDate;                  // To Date for Exchange Rate
                this.ToDateForExchangeRate = toDate.add(Date.DAY, 30);
                rec.data.applydate = this.FinancialStartDate;
                rec.data.todate = this.ToDateForExchangeRate;
                return true;
            },this);
        },this);
        
        this.ToDateEditor = new Wtf.form.DateField({
            name:'todate',
            format:WtfGlobal.getOnlyDateFormat()
        })
        
        this.currencyCm= new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridForCurrency"),  //"Foreign Currency",
                dataIndex:'tocurrency',
                autoWidth : true
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.basetoforeignexRate"),  //"Base to Foreign Exchange Rate",
                dataIndex:'exchangerate',
                width:110,
                align:'right',
                editor:this.exchangeRate=new Wtf.form.NumberField({
                    allowBlank: false,
                    decimalPrecision:16,
                    allowNegative: false,
                    minValue:0
                })
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.foreigntobaseexRate"),  //"Foreign to Base Exchange Rate",
                dataIndex:'foreigntobaseexchangerate',
                width:110,
                align:'right',
                editor:this.foreignToBaseExchangeRate=new Wtf.form.NumberField({
                    allowBlank: false,
                    decimalPrecision:16,
                    allowNegative: false,
                    minValue:0
                })
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridViewConv"),  //"View Conversion",
                width:200,
                dataIndex:'fromcurrency',
                renderer: function(a,b,c){
                    return "1 "+a+" = "+c.data.exchangerate+" "+c.data.tocurrency;
                }
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.applyDate"),  //"Applied Date",
                id: 'applydate',
                dataIndex: 'applydate',
                width:100,
                renderer:(WtfGlobal.onlyDateRenderer), 
                minValue:new Date().clearTime(true),
                editor:new Wtf.form.DateField({
                    name:'applydate',
                    format:WtfGlobal.getOnlyDateFormat()
                })
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.ToDate"),  //"To Date",
                id: 'todate',
                dataIndex: 'todate',
                width:100,
                hidden : true,
                renderer:(WtfGlobal.onlyDateRenderer),
                minValue:new Date().clearTime(true),
                editor:new Wtf.form.DateField({
                    name:'todate',
                    format:WtfGlobal.getOnlyDateFormat()
                })
            }            
        ]);
        this.currencyGrid = new Wtf.grid.EditorGridPanel({
            id:"wizardCurrencyGrid",
            region:"center",
            autoScroll:true,
            clicksToEdit:1,
            store: this.currencyStore,
            cm: this.currencyCm,
            loadMask : true,
            border: true,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:"<div style=\"text-align:center\"><a style=\"font-size:12px;font-weight:normal;\" onclick=\"javascript: copyDefaultExchangeRates('wizardCurrencyGrid')\" href='#' class='tbar-link-text'>"+WtfGlobal.getLocaleText("acc.setupWizard.note2")+"</a></div>"
            })
        });
        this.currencyGrid.on('afteredit',this.updateRow,this);
        
        this.activateToDateforExchangeRates=new Wtf.form.Checkbox({
            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.ActivateToDateforExchangeRates") + "'>" + WtfGlobal.getLocaleText("acc.field.ActivateToDateforExchangeRates") + "</span>",
            labelStyle: 'width: 280px;',
            name:'activateToDateforExchangeRates',
            checked:Wtf.account.companyAccountPref.activateToDateforExchangeRates
        });
        
        this.activateToDateforExchangeRates.on('check',function(o,newval,oldval){
            if(this.activateToDateforExchangeRates.getValue()){
                var fieldlabeltext = WtfGlobal.getLocaleText("acc.setupWizard.FromDate");
                var index=this.currencyGrid.getColumnModel().getIndexById('applydate');
                this.currencyGrid.getColumnModel().setColumnHeader(index,fieldlabeltext) ;
                
                index=this.currencyGrid.getColumnModel().getIndexById('todate');
                this.currencyGrid.getColumnModel().setHidden(index,false) ;                
            }else{
                var fieldlabeltext = WtfGlobal.getLocaleText("acc.setupWizard.applyDate");
                var index=this.currencyGrid.getColumnModel().getIndexById('applydate');
                this.currencyGrid.getColumnModel().setColumnHeader(index,fieldlabeltext) ;
                
                index=this.currencyGrid.getColumnModel().getIndexById('todate');
                this.currencyGrid.getColumnModel().setHidden(index,true) ;  
            }
        },this);
        
        this.activateToDateforExchangeRatesForm = new Wtf.form.FormPanel({
            region: "north",
            heigth: 120,
            border: false,
            labelWidth:10,
            layout: "form",
            autoScroll: true,
            style: "background:#f1f1f1;padding-top:2px",
            defaults: {labelWidth:130,border:false},
            items :              
                new Wtf.form.FieldSet({
                        cls: "wiz-card0-fieldset",
                        width: 350,
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.field.ExchangeRateSettings"),  //'Exchange Rate Settings',
                        items:[this.activateToDateforExchangeRates]
                })         
        });
        
        this.currencyBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.activateToDateforExchangeRatesForm,  
//                new Wtf.Panel({
//                    region:"north",
//                    heigth:50,
//                    border:false,
//                    style: "background:#f1f1f1;padding-top:2px;padding-bottom:5px",
//                    html:"<div style='font-size: 12px;'>"+
////                            "<b>"+WtfGlobal.getLocaleText("acc.setupWizard.homeCurrency")+" "+WtfGlobal.getCurrencyName()+".</b>&nbsp;"+
////                            "<a onclick=\"javascript: copyDefaultExchangeRates('wizardCurrencyGrid')\" href='#' class='tbar-link-text'>Download Latest Exchange Rates</a><br/>"+
//                            "</div>"
//                }),
                this.currencyGrid,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.note1")+"</div>"
                })
                ]
        });
//------------------- Card 3 <Set Financial Year>---------------------------//
        this.daysStore = new Wtf.data.SimpleStore({
            fields: [{name:'daysid',type:'int'}, 'name'],
            data :[[1,'1'],[2,'2'],[3,'3'],[4,'4'],[5,'5'],[6,'6'],[7,'7'],[8,'8'],[9,'9'],[10,'10'],
                [11,'11'],[12,'12'],[13,'13'],[14,'14'],[15,'15'],[16,'16'],[17,'17'],[18,'18'],[19,'19'],[20,'20'],
                [21,'21'],[22,'22'],[23,'23'],[24,'24'],[25,'25'],[26,'26'],[27,'27'],[28,'28'],[29,'29'],[30,'30'],[31,'31']]
        });
        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name:'monthid',type:'int'}, 'name'],
            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
                [10,'November'],[11,'December']]
        });
        this.fdays = new Wtf.form.ComboBox({
            store:  this.daysStore,
            width: 150,
            listWidth: 150,
            name:'daysid',
            displayField:'name',
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.FinYrDate"),  //'Financial Year Date',
            valueField:'daysid',
            mode: 'local',
            anchor:'95%',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus:true,
            allowBlank:false,
            value:1
        });
        this.fmonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            width: 150,
            listWidth: 150,
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.month"),  //'Month',
            name:'monthid',
            displayField:'name',
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus:true,
            allowBlank:false,
            value:0
        });
        this.bdays = new Wtf.form.ComboBox({
            store:  this.daysStore,
            width: 150,
            listWidth: 150,
            name:'daysid',
            displayField:'name',
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.BookBeginingDate"),  //'Book Beginning Date',
            valueField:'daysid',
            mode: 'local',
            anchor:'95%',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus:true,
            allowBlank:false,
            value:1
        });
        this.bmonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            width: 150,
            listWidth: 150,
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.month"),  //'Month',
            name:'monthid',
            displayField:'name',
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus:true,
            allowBlank:false,
            value:0
          });
         
        //for financial year date-Neeraj D
        var currentTime = new Date();
        var now = currentTime.getFullYear()+1;
        var years = [];
        var y = 2000;
        while(y<=now+2){
            years.push([y]);
            y++;
        }
        this.storeThn = new Wtf.data.SimpleStore({
            fields: [ 'financialyears' ],        
            data: years
        });
        this.finanyear = new Wtf.form.ComboBox({
            store: this.storeThn,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'yearid',
            xtype: 'combo',
            displayField:'financialyears',
            anchor:'95%',
            valueField:'financialyears',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            listClass: 'x-combo-list-small',
            typeAhead: false,
            allowBlank: false,
            value:currentTime.getFullYear(),
            listeners:{
                scope:this,
                beforequery: function() {
                    this.finanyear.store.loadData(years);
                },
                select: function(combo, record, index) {
                    var y1=combo.getValue(); 
                    this.comparefinancialyears(false);
                    this.finanyear.collapse();
                    if (this.bankStore && this.bankStore != undefined && this.bankStore.getCount() > 0 || (this.taxStore && this.taxStore != undefined && this.taxStore.getCount() > 0)) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.bankDetailsconfirmmsg"), function(btn) {
                            if (btn != "yes") {
                                return;
                            }
                            else {
                                this.FinancialStartDate = new Date(this.finanyear.getValue(), this.fmonth.getValue(), this.fdays.getValue());
                                if (this.bankStore.getCount() > 0) {
                                    this.bankStore.filterBy(function(rec) {
                                        rec.data.applydate = this.FinancialStartDate;
                                        return true;
                                    }, this);
                                }
                                if (this.taxStore.getCount() > 0) {
                                    this.taxStore.filterBy(function(rec) {
                                        rec.data.applydate = this.FinancialStartDate;
                                        this.taxApplyDate.setValue(this.FinancialStartDate);
                                        return true;
                                    }, this);
                                }
                            }
                        }, this);
                    }
                }
            }
        });
//
        this.byear = new Wtf.form.ComboBox({
            store: this.storeThn,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'yearid',
            displayField:'financialyears',
            anchor:'95%',
            valueField:'financialyears',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            listClass: 'x-combo-list-small',
            typeAhead: false,
            allowBlank: false,
            value:currentTime.getFullYear(),
            listeners:{
                scope:this,
                beforequery: function() {
                    var y1=this.finanyear.getValue();
                    var currentTime1 = new Date();
                    var now1 = currentTime1.getFullYear()+1;
                    var years1 = [];
                    while(y1<=now1){
                        years1.push([y1]);
                        y1++;
                    }
                    this.byear.store.loadData(years1);
                }
            }
        });
         
        this.lockRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'islock'},
        ]);
        this.lockds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.lockRec),
            url : "ACCCompanyPref/getYearLock.do",
            baseParams:{
                mode:94
            }
        });
        this.lockds.load();
//        this.lockds.on('load',this.setLockYearValues,this);

        this.financialYearForm = new Wtf.form.FormPanel({
            border: false,
            region: "center",
            layout: "form",
            autoScroll: true,
            style: "background:#f1f1f1; padding-top:3.5cm;",
            defaults: {labelWidth:200,border:false},
            items : [
                new Wtf.form.FieldSet({
//                        xtype:'fieldset',
                        cls: "wiz-card3-fieldset",
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.setupWizard.FYsetting"),  //'Financial Year Settings',
//                        defaults:{
//                            format:WtfGlobal.getOnlyDateFormat(),
//                            allowBlank:false,
//                            border:false
//                        },
                        items:[{
                            layout:"column",
                            border:false,
                            labelWidth:130,
                            defaults:{border:false},
                            items:[{
//                                columnWidth: .5,
                                width:"54%",
                                layout:'form',
                                items:this.fdays
                            },{
//                                columnWidth: .5,
                                width:"38%",
                                cls:"right-column",
                                labelWidth:50,
                                layout:'form',
                                items:this.fmonth
                            }]
                        },{
                            layout:'column',
                            cls:"fieldset3-item",
                            border:false,
                            labelWidth:130,
                            defaults:{border:false},
                            items:[{
                                layout:'form',
//                                columnWidth:0.5,
                                width:"54%",
                                items:this.bdays
                            },{
//                                columnWidth:0.5,
                                width:"38%",
                                cls:"right-column",
                                labelWidth:50,
                                layout:'form',
                                items:this.bmonth
                            }]
                        }]
                    })
                ]
        });
//        this.fmonth.on('beforeselect',this.getBeginingYearDate,this);
//        this.fdays.on('beforeselect',this.getBeginingYearDate,this);
//        this.bmonth.on('beforeselect',this.getBookBeginingDate,this);
//        this.bdays.on('beforeselect',this.getBookBeginingDate,this);
        this.finanyear.on('change',this.setCurrencyAndTax,this);
        this.fmonth.on('change',this.setCurrencyAndTax,this);
        this.fdays.on('change',this.setCurrencyAndTax,this);
        this.fmonth.on('select',this.getBeginingYearDate,this);
        this.fdays.on('select',this.getBeginingYearDate,this);
        this.bmonth.on('select',this.getBookBeginingDate,this);
        this.bdays.on('select',this.getBookBeginingDate,this);
        this.byear.on('select',this.getBookBeginingDate,this);
        this.getBeginingYearDate();
        this.getBookBeginingDate();


        this.financialYearBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.financialYearForm,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='font-size: 12px; padding-top:2px;'>"+
                    WtfGlobal.getLocaleText("acc.setupWizard.note3")+
                    WtfGlobal.getLocaleText("acc.setupWizard.note4")+
                    WtfGlobal.getLocaleText("acc.setupWizard.note5")+
                        "</div>"
                })
                ]
        });

        var countryCurrencyFormItems = [];
        this.CountryAndCurrency=new Wtf.form.FieldSet({
                        cls: "wiz-card0-fieldset",
                        width: 566,
                        autoHeight:true,
                        title:WtfGlobal.getLocaleText("acc.nee.17"),  //"Country and Currency Settings",  //'Financial Year Settings',
                        items:[this.country,this.state, this.currency , this.onlyBaseCurrency]
                });
                
         countryCurrencyFormItems.push(this.CountryAndCurrency);
        this.IndiaCountrySpecificNumbers=new Wtf.form.FieldSet({
                        cls: "wiz-card0-fieldset",
                        width: 566,
                        autoHeight:true,
                        hidden:this.country.getValue()!= Wtf.Country.INDIA,
                        title:WtfGlobal.getLocaleText("acc.setupwizard.groupname"),  //Company GST Details
                        items:[this.registrationTypeCombo,this.vat,this.cst,this.pan,this.service,this.tan,this.ecc,this.GSTIN]
                });
         this.IndonesiaCountrySpecificNumbers=new Wtf.form.FieldSet({
                        cls: "wiz-card0-fieldset",
                        width: 566,
                        autoHeight:true,
                        hidden:this.country.getValue()!= Wtf.Country.INDONESIA,
                        title:WtfGlobal.getLocaleText("acc.setupwizard.groupname"),  //Company GST Details
                        items:[this.npwp]
                });
        countryCurrencyFormItems.push(this.IndonesiaCountrySpecificNumbers);
        countryCurrencyFormItems.push(this.IndiaCountrySpecificNumbers);
        this.FinancialYearSettings = new Wtf.form.FieldSet({
                  cls: "wiz-card3-fieldset",
                  autoHeight:true,
                  style: "padding-top:10px",
                  title:WtfGlobal.getLocaleText("acc.setupWizard.FYsetting"),  //'Financial Year Settings',
                  items:[{
                      layout:"column",
                      border:false,
                      labelWidth:130,
                      defaults:{border:false},
                      items:[{
                          width:"45%",
                          layout:'form',
                          items:this.fdays
                      },{
                          width:"25%",
                          cls:"right-column",
                          labelWidth:50,
                          layout:'form',
                          items:this.fmonth
                      },{
                           width:"25%",
//                         columnWidth: .5,
                           labelWidth:35,
                           layout:'form',
                           items: this.finanyear
                            }]
                  },{
                      layout:'column',
                      cls:"fieldset3-item",
                      border:false,
                      labelWidth:130,
                      defaults:{border:false},
                      items:[{
                          layout:'form',
                          width:"45%",
                          items:this.bdays
                      },{
                          width:"25%",
                          cls:"right-column",
                          labelWidth:50,
                          layout:'form',
                          items:this.bmonth
                      },{
//                         columnWidth:0.5,
                           width:"25%",
                           labelWidth:35,
                           layout:'form',
                           items:this.byear
                            }]
                  }]
              });
        countryCurrencyFormItems.push(this.FinancialYearSettings);
        
        this.isMultiEntityCheckbox = new Wtf.form.Checkbox({
            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.gst.entity.qtip") + "'>" + WtfGlobal.getLocaleText("acc.gst.activate.multiEntity") + " </span>",
            labelStyle: "width: 280px;",
            name: 'isMultiEntity',
            disabled:(this.country.getValue() == Wtf.Country.INDIA || this.country.getValue() == Wtf.Country.US),
            listeners: {
                'check': {
                    fn: this.multiEntityHandler,
                    scope: this
                }
            },
            checked: Wtf.account.companyAccountPref.isMultiEntity || ((this.country.getValue() == Wtf.Country.INDIA || this.country.getValue() == Wtf.Country.US))
        });
        this.isMultiEntityFieldSet = new Wtf.form.FieldSet({
            cls: "wiz-card0-fieldset",
            autoHeight: true,
            style: "height: 63px;",
            title: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.gst.entity.qtip") + "'>" + WtfGlobal.getLocaleText("acc.gst.multiEntity") + " </span>",
            items: [{
                    border: false,
                    xtype: 'panel',
                    html: '<font color="#555555">' + WtfGlobal.getLocaleText("acc.gst.entity.qtiptxt") + '</font>'
                }, this.isMultiEntityCheckbox]
        });
        countryCurrencyFormItems.push(this.isMultiEntityFieldSet);
        
        this.countryCurrencyForm = new Wtf.form.FormPanel({
            border: false,
            region: "center",
            layout: "form",
            autoScroll: true,
            style: "background:#f1f1f1;padding-top:2px",
            defaults: {labelWidth:170,border:false},
            items :countryCurrencyFormItems
        });


        this.countryCurrencyBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.countryCurrencyForm,
//                this.financialYearForm,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='font-size: 12px; padding-top:2px;'>"+
                    WtfGlobal.getLocaleText("acc.rem.205")+"<br>"+
                    WtfGlobal.getLocaleText("acc.rem.206")+"<br>"+
                    WtfGlobal.getLocaleText("acc.rem.207")+"<br>"+"<br>"+
                    WtfGlobal.getLocaleText("acc.rem.211")+"<br>"+"<br>"+
                    WtfGlobal.getLocaleText("acc.setupWizard.note3")+
                    WtfGlobal.getLocaleText("acc.setupWizard.note4")+
                    WtfGlobal.getLocaleText("acc.setupWizard.note5")+
                        "</div>"
                }),
                ]
        });

//------------------- Card 4 <Set Tax Details>---------------------------//

        this.taxCm= new Wtf.grid.ColumnModel([
//            new Wtf.grid.RowNumberer(),
            {
                width:100,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxName"),  //"Tax Name",
                dataIndex:'taxname'
            },{
                width:220,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxDescription"),  //"Tax Description",
                dataIndex:'taxdescription',
                renderer : function(val,md,rec) {
                        return "<div wtf:qtip=\""
                        + val
                        + "\" wtf:qtitle='"
                        + "'>" + val + "</div>";
                    }
            },{
                width:100,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxCode"),  //"Tax Code",
                dataIndex:'taxcode'
            },{
                width:100,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxAccount"),  //"Tax Account",
                dataIndex:'gstaccountname',
                id:'gstaccountnamecolumnid',
                hidden:true
            },{
                header:WtfGlobal.getLocaleText("acc.coa.gridAccType"),
                dataIndex: 'taxtype',
                pdfwidth:100,
                id:'taxtype1',
                renderer:function(val){
                    if(val==0){
                        return "Both";
                    }else if(val==2){
                        return "Sales";
                    }else if(val==1){
                        return "Purchase";
                    }
                }
            },{
                width:50,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridPercent"),  //"Percent",
                dataIndex:'percent',
                align: "left",
                renderer:function(val){
                    if(typeof val != "number") return "";
                    return val+'%';
                }
            },{
                width:70,
                header: WtfGlobal.getLocaleText("acc.setupWizard.gridTaxApplicable"),  //"Tax Applicable Date",
                dataIndex: 'applydate',
                renderer:function(v){
                    v="";
                    v=Wtf.getCmp('applydatefortax').getValue().format(WtfGlobal.getOnlyDateFormat());
                    return v;
                }

            },{
                width:45,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridEdit"),  //'Edit',
                dataIndex: 'action',
                align: "center",
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.edit)+"' style='height: 20px; width: 16px; cursor:pointer; float:left;'></div>";
                }
            },{
                width:50,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),  //'Delete',
                dataIndex: 'action',
                align: "center",
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"' style='height: 16px; width: 16px; cursor:pointer; float:left; margin-left:5px'></div>";
                }
            }
        ]);
        this.taxGrid = new Wtf.grid.GridPanel({
            region:"center",
            autoScroll:true,
            clicksToEdit:1,
            sm: new Wtf.grid.RowSelectionModel(),
            store: this.taxStore,
            cm: this.taxCm,
            loadMask : true,
            border: false,
            view: new Wtf.grid.GridView({
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("acc.setupWizard.note26")  //"Fill the form to add Tax Details"
            })
        });
        this.taxGrid.on('rowclick',function(grid,rowindex,e){
            if(e.getTarget(".delete-gridrow")){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn){
                    if(btn!="yes") return;
                    var store=grid.getStore();
                    store.remove(store.getAt(rowindex));
                }, this);
            } else if(e.getTarget(".edit")){
                var store=grid.getStore();
                this.taxForm.getForm().loadRecord(store.getAt(rowindex));
                this.TaxTypeCombo.setValue(store.getAt(rowindex).data.taxtype);
//                store.remove(store.getAt(rowindex));
                store.each(function(rec){
                    rec.data.isEditing = 0;
                },this);
                store.getAt(rowindex).data.isEditing = 1;
            }
        },this);
        
        // Account Combo
        
        this.gstaccRec=new Wtf.data.Record.create([
                {name: 'accountid',mapping:'accid'},
                {name: 'accountname',mapping:'accname'}
        ]);
        
        this.gstaccStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gstaccRec),
            //            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
    //            group:[3],
                ignoreGLAccounts:true,  
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
    //            ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            }
        });
        this.gstaccStore.load();
    
        this.gstcmbAccount= new Wtf.form.FnComboBox({
                fieldLabel:'GST Account*',
                hiddenName:'gstaccountid',
                store:this.gstaccStore,
                addNoneRecord: true,
                listWidth:200,
                width:183,
                valueField:'accountid',
                displayField:'accountname',
                emptyText:'Select GST Tax Account',
                forceSelection:true,
                mode: 'local',
                disableKeyFilter:true,
                allowBlank:(this.country.getValue()!=137),
                triggerAction:'all',
                hirarchical:true
            });
        
         var arr = [[0,"Both"],[2,"Sales"],[1,"Purchase"]];
        this.TaxTypeStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'taxtypeid',
                type:'int'
            }, 'name'],
            data :arr
        });
        
        this.TaxTypeCombo= new  Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxType"),  //"Tax type*",
            hiddenName:'taxtypeid',
            store:this.TaxTypeStore,
            valueField:'taxtypeid',
            displayField:'name',
            forceSelection:true,
            mode: 'local',
            listWidth:200,
            anchor:'57.5%',
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            selectOnFocus:true,
            emptyText:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxType.emptytext")//acc.setupWizard.gridTaxType.emptytext
        });
        this.enableGST=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.enable.gst"),
            cls : 'custcheckbox',
            width : 10,
            name:'enableGST',
            listeners:{
                'check':{
                    fn:this.GSTEnableHandler,
                    scope:this
                }
            },
            checked:true
        })
        
        
        this.taxForm = new Wtf.form.FormPanel({
            region: "north",
            border: false,
            style: "background:#f1f1f1;padding:0 15px 15px 15px",
            height: 260,
            labelWidth:160,
            items : [
                // Check box for Malasian Company
                
                this.enableGST,
                
                new Wtf.Panel({
                    border: false,
                    html : "<div style='font-size: 12px;padding-bottom:4px'>"+WtfGlobal.getLocaleText("acc.setupWizard.note7")+"<br/></div>"
                }),
                this.taxId = new Wtf.form.Hidden({
                    hidden:true,
                    name:"taxid"
                }),
                this.taxName = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxName"),  //"Tax Name*",
                    width:200,
                    maxLength:50,
                    allowBlank: false,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterTaxName"),  //"Enter Tax name here",
                    name:"taxname",
                    regex:Wtf.specialChar
                }),
                this.taxDescription = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxDescription"),  //"Tax Description",
                    width:200,
                    maxLength:1000,
                    allowBlank: true,
                    emptyText:  WtfGlobal.getLocaleText("acc.setupWizard.enterTaxDescription"),  //"Enter Tax description here",
                    name:"taxdescription",
                    regex:Wtf.specialChar
                }),
                this.taxCode = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxCode"),  //"Tax Code*",
                    width:200,
                    maxLength:50,
                    allowBlank: false,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterTaxCode"),  //"Enter Tax code here",
                    name:"taxcode",
                    regex:Wtf.specialChar
                }),
                this.taxPercent = new Wtf.form.NumberField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridPercent"),  //"Percent*",
                    width:200,
                    allowBlank: false,
                    maxValue:100,
                    allowNegative:false,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterPercent"),  //"Enter Percent here",
                    maxLength:50,
                    name:"percent"
                }),
                this.gstcmbAccount,
                this.TaxTypeCombo,
                this.taxApplyDate = new Wtf.form.DateField({
                    id:'applydatefortax',
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.gridTaxApplicable"),  //"Tax Applicable Date*",
                    width:183,
                    allowNegative:false,
                    name:'applydate',
                    readOnly:true,
//                    minValue:(this.isMalasianCompany)?this.FinancialStartDate:'',
                    value: this.FinancialStartDate,
                    format:WtfGlobal.getOnlyDateFormat()
                }),
                this.taxSaveButton = new Wtf.Button({
                    text:WtfGlobal.getLocaleText("acc.setupWizard.addToListBtn"),  //"Add to list",
                    scope:this,
                    handler:function() {
                        if(!this.taxForm.getForm().isValid()) {
                            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.setupWizard.note15"));
                        } else {
                            var FIND_TAX = this.taxName.getValue();
                            var editIndex = this.taxStore.find("isEditing","1");
                            var index = this.taxStore.findBy(function(rec){
                                                                var taxname=rec.data['taxname'].trim();
//                                                                taxname=taxname.replace(/\s+/g, '');
                                                                if(taxname==FIND_TAX)// && rec.data.isEditing!=1)
                                                                    return true;
                                                                else
                                                                    return false
        });
                            if(index!=-1 && ((editIndex ==-1) || (editIndex!=-1 && index!=editIndex))){
                                this.showMessage(WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.field.Taxentryfor")+"<b>"+this.taxName.getValue()+"</b>"+WtfGlobal.getLocaleText("acc.field.isavailableintaxdetails"));
                            } else {
                                if(index==-1 && editIndex==-1){
                                    var newrec = new this.taxRec({
                                        taxid:this.taxStore.getCount(),
                                        taxname:this.taxName.getValue(),
                                        taxdescription:this.taxDescription.getValue(),
                                        taxcode:this.taxCode.getValue(),
                                        gstaccountname:this.gstcmbAccount.getRawValue(),
                                        gstaccountid:this.gstcmbAccount.getValue(),
                                        percent:this.taxPercent.getValue(),
                                        taxtype:this.TaxTypeCombo.getValue(),
                                        applydate:this.taxApplyDate.getValue(),
                                        mastertypevalue:Wtf.Acc_AccountGroup_Gst                    //mastertypevale for gsttax
        });
                                    this.taxStore.add(newrec);
                                } else {
                                    var rec = this.taxStore.getAt(editIndex);
                                    rec.data.taxname=this.taxName.getValue();
                                    rec.data.taxdescription=this.taxDescription.getValue();
                                    rec.data.taxcode=this.taxCode.getValue();
                                    rec.data.gstaccountname=this.gstcmbAccount.getRawValue();
                                    rec.data.gstaccountid=this.gstcmbAccount.getValue();
                                    rec.data.percent=this.taxPercent.getValue();
                                    rec.data.taxtype=this.TaxTypeCombo.getValue(),
                                    rec.data.applydate=this.taxApplyDate.getValue();
                                    rec.data.isEditing = 0;
                                    this.taxStore.commitChanges();
                                    this.taxGrid.getView().refresh();
                                     mastertypevalue:Wtf.Acc_AccountGroup_Gst
                                }
                                this.taxForm.getForm().setValues({
                                    taxid:"",
                                    taxname:"",
                                    taxdescription:"",
                                    taxcode:"",
                                    gstaccountname:"",
                                    gstaccountid:"",
                                    percent:"",
                                    applydate:this.FinancialStartDate
                                });
                                this.TaxTypeCombo.clearValue();
                                this.taxForm.getForm().clearInvalid();
                            }
                        }
                    }
                })
            ]
        });

        this.taxBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.taxForm,
                this.taxGrid,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.note6")+"</div>"
                })
                ]
        });

//------------------- Card 5 <Set Bank Details>---------------------------//

        this.bankRec = new Wtf.data.Record.create([
            {name: 'bankid'},
            {name: 'bankname'},
            {name: 'ifsccode'},
            {name: 'micrcode'},
            {name: 'accountname'},
            {name: 'accounttype'},
            {name: 'mastertypevalue'},
            {name: 'accountno'},
            {name: 'accountcode'},
            {name: 'balance'},
            {name: 'applydate'},
            {name: 'bankbranchname'},
            {name: 'bsrcode'},
            {name: 'bankbranchaddress'},
            {name: 'branchstate'},
            {name: 'pincode'},
            {name: 'isEditing'}
        ]);
        this.bankStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.bankRec),
            url:"ACCAccount/getAccounts.do"
        });
        this.bankCm= new Wtf.grid.ColumnModel([
//            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.setupWizard.BankName"),  //"Bank Name",
                dataIndex:'bankname'
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.IFSCCode"),  //"IFSC Code",
                dataIndex:'ifsccode',
                hidden:true
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.MICRCode"),  //"MICR Code",
                dataIndex:'micrcode',
                hidden:true
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.AccountName"),  //"Account Name",
                dataIndex:'accountname'
            }
            ,{
                header:WtfGlobal.getLocaleText("acc.setupWizard.AccountNo"),  //"Account No.",
                dataIndex:'accountno'
            }
            ,{
                header:WtfGlobal.getLocaleText("acc.setupWizard.AccountCode"),  //"Account Code.",
                dataIndex:'accountcode'
            }
            ,{
                header:WtfGlobal.getLocaleText("acc.coa.gridAccType"),  // "Account Type",
                dataIndex: 'accounttype',
                renderer:function(val){
                    if(val==1){
                        return WtfGlobal.getLocaleText("acc.navigate.BalanceSheet")
                    }else{
                        return WtfGlobal.getLocaleText("acc.field.Profit&Loss")
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.openBal"),  //"Opening Balance",
                dataIndex:'balance',
                autoWidth : true,
                align: "right"
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.appDate"),  //"Applicable Date",
                dataIndex: 'applydate',
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.bankBranchName"),  //"Bank Branch Name",
                dataIndex: 'bankbranchname',
                hidden:this.country.getValue()!=Wtf.Country.INDIA
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.bsrCode"),  //"BSR Code",
                dataIndex: 'bsrcode',
                hidden:this.country.getValue()!=Wtf.Country.INDIA,
                fixed:this.country.getValue()!=Wtf.Country.INDIA                
            },{
                header: WtfGlobal.getLocaleText("acc.setupWizard.bankBranchAddress"),  //"Bank Branch Address",
                dataIndex: 'bankbranchaddress',
                hidden:this.country.getValue()!=Wtf.Country.INDIA,
                fixed:this.country.getValue()!=Wtf.Country.INDIA 
            },{
                header: WtfGlobal.getLocaleText("acc.field.State"),  //"State",
                dataIndex: 'branchstate',
                hidden:this.country.getValue()!=Wtf.Country.INDIA,
                fixed:this.country.getValue()!=Wtf.Country.INDIA,
                renderer: function(val,arg1,arg2){
                    var StateDetails = Wtf.stateStore.find('id',val);
                    if(StateDetails!=-1){
                        var StateName = Wtf.stateStore.getAt(StateDetails).data.name;
                        return StateName;
                    }else{
                      return '';
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.setupWizard.pincode"),  //"Pincode",
                dataIndex: 'pincode',
                hidden:this.country.getValue()!=Wtf.Country.INDIA,
                fixed:this.country.getValue()!=Wtf.Country.INDIA 
            },{
                width:45,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridEdit"),  //'Edit',
                dataIndex: '',
                align: "center",
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.edit)+"' style='height: 20px; width: 16px; cursor:pointer; float:left;'></div>";
                }
            },{
                width:55,
                header:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),  //'Delete',
                dataIndex: '',
                align: "center",
                renderer: function(){
                    return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"' style='height: 16px; width: 16px; cursor:pointer; float:left;'></div>"
                }
            }
        ]);
        this.bankGrid = new Wtf.grid.GridPanel({
            id:"wizardBankGrid",
            region:"center",            
            autoScroll:true,
            store: this.bankStore,
            cm: this.bankCm,
            loadMask : true,
            border: false,
            view: new Wtf.grid.GridView({
                forceFit: false,
                emptyText:WtfGlobal.getLocaleText("acc.setupWizard.note28")  //"Fill the form to add Bank Details"
            })
        });
        this.bankGrid.on('rowclick',function(grid,rowindex,e){
            if(e.getTarget(".delete-gridrow")){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.setupWizard.note27"), function(btn){
                    if(btn!="yes") return;
                    var store=grid.getStore();
                    store.remove(store.getAt(rowindex));
                }, this);
            } else if(e.getTarget(".edit")){
                var store=grid.getStore();
                this.bankForm.getForm().loadRecord(store.getAt(rowindex));
//                store.remove(store.getAt(rowindex));
                store.each(function(rec){
                    rec.data.isEditing = 0;
                },this);
                store.getAt(rowindex).data.isEditing = 1;
            }
        },this);
        

        this.accountTypeStore = new Wtf.data.SimpleStore({
            fields : ['id', 'name'],
            data : [
            ['1', WtfGlobal.getLocaleText("acc.navigate.BalanceSheet")]
            ]
        });

        this.bankBranchName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.bankBranchName"),  
            name:'bankbranchname',
            width:180,
            maxLength:30,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.setupWizard.bankBranchName")
            
        });
        this.bankBranchAddress = new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.bankBranchAddress"),  
            name:'bankbranchaddress',
            width:180,
            maxLength:30,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.setupWizard.bankBranchAddress")
        });
        this.bsrCode = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.bsrCode"),  
            name:'bsrcode',
            width:180,
            allowNegative: false,
            maxLength:7,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.setupWizard.bsrCode")+" of Bank Branch"
        });
        this.branchState =new Wtf.form.ComboBox({
            store: Wtf.stateStore,
            width:163,
            id:'branchstate',
            name:'branchstate',
            listWidth:163,
            labelWidth:80,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.State"),
            displayField:'name',
            valueField:'id',
            value:Wtf.account.companyAccountPref.state,
            triggerAction: 'all',
            mode: 'local',
            typeAhead:true,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.field.State"),
            selectOnFocus:true,
            forceSelection: true
        });
        this.branchPincode = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.pincode"),  
            name:'pincode',
            width:180,
            allowNegative: false,
            maxLength:6,
            emptyText: "Enter "+WtfGlobal.getLocaleText("acc.setupWizard.pincode")
        });
        this.rightSideTitle=new Wtf.Panel({
                    border: false,
                    html : "<div style='font-size: 12px;padding-bottom:10px'>"+"Mailing Details"+"<br/></div>"
                })
        this.mailingDetailsSetup=new Wtf.form.FieldSet({
            title: "Mailing Details",  //'Do you want to track Inventory in Deskera Accounting?',
            autoHeight: true,
            cls: "wiz-card6-fieldset",
            border: false,
            items:[this.bankBranchName,this.bsrCode,this.bankBranchAddress,this.branchState,this.branchPincode]
        });
        
        this.bankForm = new Wtf.form.FormPanel({
            height: 150,
            title:WtfGlobal.getLocaleText("acc.nee.18"),
            autoScroll: true,
            border: true,
             style: "background:#f1f1f1;",
            layout:'column',
            region:'north',
            items : [{
                layout:'form',
                columnWidth: .5,
                style: "padding:10px 10px",
                border: false,
                items:[
//                new Wtf.Panel({
//                    border: false,
//                    html : "<div style='font-size: 12px;padding-bottom:10px'>"+WtfGlobal.getLocaleText("acc.nee.18")+"<br/></div>"
//                }),
                this.bankId = new Wtf.form.Hidden({
                    hidden:true,
                    name:"bankid"
                }),
                this.bankName = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.BankName"),  //"Bank Name*",
                    width:180,
                    maxLength:50,
                    allowBlank: false,
                    emptyText:WtfGlobal.getLocaleText("acc.setupWizard.enterBankName"),  //"Enter Bank name here",
                    name:"bankname",
                    regex:Wtf.specialChar
                }),
                this.ifsccode = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.IFSCCode"),  //"IFSC Code",
                    width:180,
                    maxLength:11,
                    emptyText:WtfGlobal.getLocaleText("acc.setupWizard.enterIFSCCode"),  //"Enter IFSC Code here",
                    name:"ifsccode",
                    invalidText :'Alphabets and numbers only example- ANDB0001478 (4 letters + 7 digits)',
                    vtype : "alphanum",
                    regex:/[^\s]{4}\d{7}/
                }),
                this.micrcode = new Wtf.form.NumberField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.MICRCode"),  //"MICR Code",
                    width:180,
                    allowNegative: false,
                    emptyText:WtfGlobal.getLocaleText("acc.setupWizard.enterMICRCode"),  //"Enter MICR Code here",
                    name:"micrcode",
                    maxLength:9,
                    vtype : "alphanum",
                    invalidText :'numbers only, example- 123456789 (9 digits)',
                    regex:/\d{9}/
                }),
                this.accountName = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.AccountName"),  //"Account Name*",
                    width:180,
                    maxLength:50,
                    allowBlank: false,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterAccountName"),  //"Enter Account name here",
                    name:"accountname",
                    regex:Wtf.specialChar
                }),
                this.bankAccountcode = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.AccountCode"),  //"Account Code",
                    width:180,
                    maxLength:30,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterAccountCode"),  //"Enter Account No",
                    //                    allowBlank: false,
                    name:"accountcode",
                    regex:Wtf.specialChar
                }),
                this.bankAccountNo = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.AccountNo"),  //"Account No",
                    width:180,
                    maxLength:30,
                    emptyText: WtfGlobal.getLocaleText("acc.setupWizard.enterAccountNo"),  //"Enter Account No",
                    //                    allowBlank: false,
                    name:"accountno",
                    regex:Wtf.specialChar
                }),

                this.accountTypeCombo= new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText("acc.coa.gridAccType"),
                    name: 'accounttype',
                    forceSelection:true,
                    triggerAction:'all',
                    editable:false,
                    displayField:'name',
                    valueField:'id',
                    store:this.accountTypeStore,
                    mode:'local',
                    value:1,
                    width:165,
                    listWidth:185
                }),
                this.bankBalance= new Wtf.form.NumberField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.openBal"),  //"Opening Balance",
                    width:180,
                    allowBlank: false,
                    minValue:0,
                    allowNegative:false,
                    value:0,
                    maxLength:11,
                    name:"balance"
                }),
                this.bankApplyDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.rem.24")+"*",  //"Applicable Date*",
                    width:163,
                    allowNegative:false,
                    name:'applydate',
                    readOnly:true,
                    value: this.FinancialStartDate,
                    format:WtfGlobal.getOnlyDateFormat()
                })]
            },{
                layout:'form',  
                labelWidth:90,
                columnWidth: .46, 
                style: "padding:10px 0px",
                border: false,
                items:[this.mailingDetailsSetup]
            } ],
        bbar:[              
                new Wtf.Button({
                    text:WtfGlobal.getLocaleText("acc.setupWizard.addToListBtn"),  //"Add to list",
                    scope:this,
                    iconCls:"pwnd add",
                    handler:function() {
                        if(!this.bankForm.getForm().isValid()) {
                            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.setupWizard.note15"));
                        } else {
                            var FIND_BANK = this.bankName.getValue();
                            var editIndex = this.bankStore.find("isEditing","1");
                            var index = this.bankStore.findBy(function(rec){
                                var bankname=rec.data['bankname'].trim();
                                //                                                                        bankname=bankname.replace(/\s+/g, '');
                                if(bankname==FIND_BANK)
                                    return true;
                                else
                                    return false
                            });
                            if(index!=-1 && ((editIndex ==-1) || (editIndex!=-1 && index!=editIndex))){
                                this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Bankentryfor")+"<b>"+this.bankName.getValue()+"</b>"+WtfGlobal.getLocaleText("acc.field.isavailableinbankdetails"));
                            } else {
                                if(index==-1 && editIndex==-1){
                                    var newrec = new this.bankRec({
                                        bankid:this.bankStore.getCount(),
                                        bankname:this.bankName.getValue(),
                                        ifsccode:this.ifsccode.getValue(),
                                        micrcode:this.micrcode.getValue(),
                                        accountname:this.accountName.getValue(),
                                        accountcode:this.bankAccountcode.getValue(),
                                        accountno:this.bankAccountNo.getValue(),
                                        balance:this.bankBalance.getValue(),
                                        applydate:this.bankApplyDate.getValue(),
                                        mastertypevalue:Wtf.Acc_AccountGroup_Bank,                        //for master type value 3 for bank
                                        accounttype:this.accountTypeCombo.getValue(),
                                        bankbranchname:this.bankBranchName.getValue(),               
                                        bsrcode:this.bsrCode.getValue(),               
                                        bankbranchaddress:this.bankBranchAddress.getValue(),               
                                        branchstate:this.branchState.getValue(),               
                                        pincode:this.branchPincode.getValue()                                           
                                        //this.bankBranchName,this.bsrCode,this.bankBranchAddress,this.branchState,this.branchPincode
                                    });
                                    this.bankStore.add(newrec);
                                } else {
                                    var rec = this.bankStore.getAt(editIndex);
                                    rec.data.bankname=this.bankName.getValue();
                                    rec.data.ifsccode=this.ifsccode.getValue();
                                    rec.data.micrcode=this.micrcode.getValue();
                                    rec.data.accountname=this.accountName.getValue();
                                    rec.data.accountno=this.bankAccountNo.getValue();
                                    rec.data.balance=this.bankBalance.getValue();
                                    rec.data.applydate=this.bankApplyDate.getValue();
                                    rec.data.bankbranchname=this.bankBranchName.getValue();
                                    rec.data.bsrcode=this.bsrCode.getValue();
                                    rec.data.bankbranchaddress=this.bankBranchAddress.getValue();
                                    rec.data.branchstate=this.branchState.getValue();
                                    rec.data.pincode=this.branchPincode.getValue();
                                    mastertypevalue:Wtf.Acc_AccountGroup_Bank;                        //for master type value 3 for bank;                
                                    accounttype:this.accountTypeCombo.getValue()
                                    rec.data.isEditing = 0;
                                    this.bankStore.commitChanges();
                                    this.bankGrid.getView().refresh();
                                }

                                this.bankForm.getForm().setValues({
                                    bankid:"",
                                    bankname:"",
                                    ifsccode:"",
                                    micrcode:"",
                                    accountname:"",
                                    accountno:"",
                                    balance:0,
                                    applydate:this.FinancialStartDate,
                                    accounttype:1,
                                    bankbranchname:"",
                                    bsrcode:"",
                                    bankbranchaddress:"",
                                    branchstate:"",
                                    pincode:""
                                });
                                this.bankForm.getForm().clearInvalid();
                            }
                            if(Wtf.getCmp('move-next')){
                                Wtf.getCmp('move-next').setDisabled(false);//enabling next button
                            }
                        }
                    }
                })]
        });
        
        this.bankName.on("change",function(){
            if(this.accountName.getValue()==""){
                this.accountName.setValue(this.bankName.getValue());
            }
        },this);
        this.bankBody = new Wtf.Panel({
            border: false,
            layout: "border",
            items: [
                this.bankForm,
                this.bankGrid,
                new Wtf.Panel({
                    region:"south",
                    heigth:20,
                    border:false,
                    style: "background:#f1f1f1;padding-top:2px",
                    items:[{
                            xtype:'panel',
                            heigth:15,
                            border:false,
                            html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.note8")+"</div>"
                        },
                        {
                            xtype:'panel',
                            heigth:15,
                            border:false,
                            html:"<div style='color: red; font-size: 12px; padding-top:2px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.note8.india")+"</div>"
                        }
                    ]
                })
                ]
        });

//------------------- Card 6 <Set Inventory Preferences>---------------------------//
        this.withInventory= new Wtf.form.Radio({
                    checked: true,
                    fieldLabel: '',
                    readOnly:true,
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note29"),  //"Yes, I want to use Deskera Accounting <b>with</b> Inventory.",
                    name: 'withInventory',
                    inputValue: "Yes"
                }), 
        this.inventorySetup=new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.setupWizard.question2"),  //'Do you want to track Inventory in Deskera Accounting?',
                autoHeight: true,
                cls: "wiz-card6-fieldset",
                border: false,
                defaultType: 'radio',
                items: [
                              
                this.withInvIntegration = new Wtf.form.Checkbox({
                    fieldLabel: '',
                    checked: Wtf.account.companyAccountPref.activateInventoryTab != undefined ? Wtf.account.companyAccountPref.activateInventoryTab : true,
                    labelSeparator: '',
//                    disabled:true,
//                    style: '',
                    boxLabel:WtfGlobal.getLocaleText("acc.setupWizard.note41"),  //"Include <b>Trading</b> Flow.",
                    name: 'activateInventory',
                    id : 'activateinventory'
//                    inputValue: "No"
                }),        
                        this.activatemrpmodule = new Wtf.form.Checkbox({
                            fieldLabel: '',
                            checked:false,
                            labelSeparator: '',
                            boxLabel: WtfGlobal.getLocaleText("acc.mrp.field.activatemrpmodule"), //"Activate MRP Module",
                            name: 'activatemrpmodule',
                            id: 'activatemrpmodule'
                        }),
                this.withInvUpdate = new Wtf.form.Checkbox({
                    fieldLabel: '',
                    checked: true,
                    labelSeparator: '',
                    disabled:true,
//                    style: 'margin-left: 30px',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note29_a"),  //"Include <b>Trading</b> Flow.",
                    name: 'withInvUpdate',
                    id : 'tradingflow',
                    inputValue: "Yes"
                }),  
                        {
                            xtype: 'fieldset',
                            title: "<span wtf:qtip='" +  WtfGlobal.getLocaleText("acc.companypreferences.inventory.valuation.method") + "'>" +  WtfGlobal.getLocaleText("acc.companypreferences.inventory.valuation.method") + "</span>",
                            autoHeight: true,
                            cls: "wiz-card6-fieldset",
                            items: [this.periodicInventory = new Wtf.form.Radio({
                                    checked: true,
                                    boxLabel:  WtfGlobal.getLocaleText("acc.companypreferences.periodic.inventory"),
                                    fieldLabel: "",
                                    labelSeparator: '',
                                    name: 'inventoryvaluationtype'
                                }),
                                this.perpetualInventory = new Wtf.form.Radio({
                                    fieldLabel: "",
                                    labelSeparator: '',
                                    boxLabel:  WtfGlobal.getLocaleText("acc.companypreferences.perpetual.inventory"),
                                    name: 'inventoryvaluationtype'
                                })]

                        }        
//                this.withOutInventory= new Wtf.form.Radio({
//                    ctCls:"fieldset-item",
//                    fieldLabel: '',
//                    labelSeparator: '',
//                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note30"),  //"No, I want to use Deskera Accounting <b>without</b> Inventory.",
//                    name: 'withInventory',
//                    inputValue: "No"
//                })
            ]
            });
        this.inventorySetting=new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.setupWizard.InventorySettingquestion"),  //'Do you want to track Inventory in Deskera Accounting?',
        autoHeight: true,
        cls: "wiz-card6-fieldset",
        border: false,
        defaultType: 'radio',
        items: [
                this.UomScema= new Wtf.form.Radio({
                    checked: true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note39"),  
                    name: 'UomSchemaType'
                }),               
                this.PackagingUom = new Wtf.form.Radio({
                    fieldLabel: '',
                    labelSeparator: '', 
//                    hidden:true,
//                    hideLabel:true, 
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note40"), 
                    name: 'UomSchemaType'                  
                })]
            });
        this.otherSetting=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.field.OtherSetting"),  //'Other Settings',
            autoHeight: true,
            cls: "wiz-card6-fieldset",
            border: false,
            defaultType: 'radio',
            items: [
                this.isLocationCompulsory=new Wtf.form.Checkbox({ //option to check wether Location is compulsory or not
                    fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isLocationCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isLocationCompulsory")+"</span>",
                    name:'isLocationCompulsory',
                    labelStyle: 'width: 250px;',
                    autoWidth:true,
                    checked:Wtf.account.companyAccountPref.isLocationCompulsory
                }),this.isWarehouseCompulsory=new Wtf.form.Checkbox({ //option to check wether Warehouse is compulsory or not
                    fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isWarehouseCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isWarehouseCompulsory")+"</span>",
                    name:'isWarehouseCompulsory',
                    labelStyle: 'width: 250px;',
                    autoWidth:true,
                    checked:Wtf.account.companyAccountPref.isWarehouseCompulsory
                }),this.isSerialCompulsory=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
                    fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isSerialCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isSerialCompulsory")+"</span>",
                    name:'isSerialCompulsory',
                    labelStyle: 'width: 250px;',
                    autoWidth:true,
                    checked:Wtf.account.companyAccountPref.isSerialCompulsory
                }),this.isBatchCompulsory=new Wtf.form.Checkbox({ //option to check wether batch is compulsory or not
                    fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isBatchCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isBatchCompulsory")+"</span>",
                    name:'isBatchCompulsory',
                    labelStyle: 'width: 250px;',
                    autoWidth:true,
                    checked:Wtf.account.companyAccountPref.isBatchCompulsory
                }),this.isRowCompulsory=new Wtf.form.Checkbox({ //option to check wether Row is compulsory or not
                    fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isRowCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isRowCompulsory")+"</span>",
                    name:'isRowCompulsory',
                    labelStyle: 'width: 250px;',
                    autoWidth:true,
                    checked:Wtf.account.companyAccountPref.isRowCompulsory
                }),this.isRackCompulsory=new Wtf.form.Checkbox({ //option to check wether Row is compulsory or not
                    fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isRackCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isRackCompulsory")+"</span>",
                    name:'isRackCompulsory',
                    labelStyle: 'width: 250px;',
                    autoWidth:true,
                    checked:Wtf.account.companyAccountPref.isRackCompulsory
                }),this.isBinCompulsory=new Wtf.form.Checkbox({ //option to check wether Row is compulsory or not
                    fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.isBinCompulsoryTT")+"'>"+WtfGlobal.getLocaleText("acc.field.isBinCompulsory")+"</span>",
                    name:'isBinCompulsory',
                    labelStyle: 'width: 250px;',
                    autoWidth:true,
                    checked:Wtf.account.companyAccountPref.isBinCompulsory
                }),this.productPricingOnBands = new Wtf.form.Checkbox({
                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.activateProductPricingOnBandsToolTip") + "'>" + WtfGlobal.getLocaleText("acc.field.activateProductPricingOnBands") + "</span>", // "Activate Product Pricing On Bands",
                    labelStyle: 'width: 250px;',
                    name: 'productPricingOnBands',
                    autoWidth:true,
                    checked: Wtf.account.companyAccountPref.productPricingOnBands
                }),this.productPricingOnBandsForSales = new Wtf.form.Checkbox({
                    fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.activateProductPricingOnBandsForSales") + "'>" + WtfGlobal.getLocaleText("acc.field.activateProductPricingOnBandsForSales") + "</span>", // "Activate Product Pricing On Bands For Sales",
                    labelStyle: 'width: 250px;',
                    name: 'productPricingOnBandsForSales',
                    autoWidth:true,
                    checked: Wtf.account.companyAccountPref.productPricingOnBandsForSales
                })]
            });            
        this.inventoryPreferencesForm = new Wtf.form.FormPanel({
            border: false,
            labelWidth:20,
            items :[this.inventorySetup,this.inventorySetting,this.otherSetting] 
        });
        this.withInventory.on("check",function(){
            if(Wtf.getCmp("tradingflow").container != undefined) {
                Wtf.getCmp("tradingflow").container.parent().dom.style.display=("block");
            }            
                      //this.withOutInventory.checked=false;
        },this);
        
        this.withInvIntegration.on("check",function(a,b){
            if(b == true){
                this.isLocationCompulsory.setValue(true);
                this.isWarehouseCompulsory.setValue(true);
                this.isLocationCompulsory.disable();
                this.isWarehouseCompulsory.disable();
            }else{
                this.isLocationCompulsory.enable();
                this.isWarehouseCompulsory.enable();
                this.isLocationCompulsory.setValue(false);
                this.isWarehouseCompulsory.setValue(false);
            }
        },this);
        this.activatemrpmodule.on("check", function(obj, checked) {
            if (checked == true) {
                if (!Wtf.isPMSync) {
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.information"),
                        msg: WtfGlobal.getLocaleText("acc.mrp.field.checkactivationofmrpmodule"),
                        scope: this,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.INFO,
                        width: 350,
                        closable:false,
                        fn: function(btn) {
                            if (btn == "ok") {
                                this.activatemrpmodule.setValue(false);
                                this.periodicInventory.setValue(true);
                                this.perpetualInventory.setValue(false);
                                this.periodicInventory.enable();
                                this.perpetualInventory.enable();
                                return;
                            } 
                        }
                    }, this);

                }
                this.periodicInventory.setValue(false);
                this.perpetualInventory.setValue(true);
                this.periodicInventory.disable();
                this.perpetualInventory.disable();
            } else {
                this.periodicInventory.setValue(true);
                this.perpetualInventory.setValue(false);
                this.periodicInventory.enable();
                this.perpetualInventory.enable();
            }
        }, this);
        
        /*this.isLocationCompulsory.on("check",function(a,b){
            if(b == true && this.isWarehouseCompulsory.getValue() ==true){
                this.withInvIntegration.enable();
            }else{
                this.withInvIntegration.setValue(false);
                this.withInvIntegration.disable();
            }
        },this);
        this.isWarehouseCompulsory.on("check",function(a,b){
            if(b == true && this.isLocationCompulsory.getValue() ==true){
                 this.withInvIntegration.enable();
            }else{
                this.withInvIntegration.setValue(false);
                this.withInvIntegration.disable();
            }
        },this);*/
//         this.isLinksClosed = this.inventoryPreferencesForm.getForm().getValues().withInventory==undefined;
//            if(this.withInventory.getValue ==true && Wtf.getCmp('move-next')){
//    Wtf.getCmp('move-next').setDisabled(false);//enabling next button
//}
//        this.withOutInventory.on("check",function(){
//            this.withInvUpdate.reset(true);
//            Wtf.getCmp("tradingflow").container.parent().dom.style.display=("none");
//            this.isLinksClosed = this.inventoryPreferencesForm.getForm().getValues().withInventory==undefined;
//            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
//            this.withInventory.checked=false;
//        },this);
//        this.UomScema.on("check",function(){           
//            this.isLinksClosed = this.inventoryPreferencesForm.getForm().getValues().UomScema==undefined;
//            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
//        },this);
//        this.PackagingUom.on("check",function(){
//            this.isLinksClosed = this.inventoryPreferencesForm.getForm().getValues().PackagingUom==undefined;
//            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
//        },this);
        
       
        //------------------- Card 7 <Set Tax1099 Preferences>---------------------------//
        this.tax1099PreferencesForm = new Wtf.form.FormPanel({
            border: false,
            labelWidth:20,
            items : new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.setupWizard.question3"),  //'Do you want to track Tax 1099 in Deskera Accounting',
                autoHeight: true,
                cls: "wiz-card6-fieldset",
                border: false,
                defaultType: 'radio',
                items: [
                this.withTax1099= new Wtf.form.Radio({
//                    checked: true,
                    fieldLabel: '',
                    labelSeparator: '',
                    checked: true,
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note16"),  //"Yes, I want to use Deskera Accounting <b>with</b> Tax 1099.",
                    name: 'withTax1099',
                    inputValue: "Yes"
                }),
                this.withOutTax1099= new Wtf.form.Radio({
                    ctCls:"fieldset-item",
                    fieldLabel: '',
//                    checked: true,
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.setupWizard.note17"),  //"No, I want to use Deskera Accounting <b>without</b> Tax 1099.",
                    name: 'withTax1099',
                    inputValue: "No"
                })]
            })
        });
        this.withTax1099.on("check",function(){
            this.isLinksClosed = this.tax1099PreferencesForm.getForm().getValues().withTax1099==undefined;
            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
        },this);
        this.withOutTax1099.on("check",function(){
            this.isLinksClosed = this.tax1099PreferencesForm.getForm().getValues().withTax1099==undefined;
            Wtf.getCmp('move-next').setDisabled(this.isLinksClosed);
        },this);
    },
    
    getBeginingYearDate: function(fromCard5,m,rec){
        var fromCard5Flag=(fromCard5=="undefined" || fromCard5=="")?false:fromCard5;
//        var year =  Wtf.serverDate?Wtf.serverDate.getFullYear():new Date().getFullYear();
        var fyear = (this.finanyear.getValue()!=""||this.finanyear.getValue()!=0)?this.finanyear.getValue():new Date().getFullYear();
        var fmonth = (this.fmonth.getValue()==0 || this.fmonth.getValue()!="")?this.fmonth.getValue():new Date().getMonth();
        var fday = (this.fdays.getValue()==0 || this.fdays.getValue()!="")?this.fdays.getValue():new Date().getDay();
        var isvaliddate= this.checkdate(fday, fmonth, fyear);
        if(!isvaliddate){
            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.setupWizard.note31"));
            return false;
        }
        this.comparefinancialyears(fromCard5Flag);
        this.FinancialStartDate = new Date(fyear,fmonth,fday);
        var toDate = this.FinancialStartDate;
        this.ToDateForExchangeRate = toDate.add(Date.DAY, 30);
        /*if(this.taxApplyDate)
            this.taxApplyDate.minValue='';
        if(this.country.getValue()==137){//Malasiya
            this.taxApplyDate.minValue=this.FinancialStartDate;
            this.taxStore.load({
                params:{
                    countryid:this.country.getValue(),
                    financialYrStartDate:WtfGlobal.convertToGenericDate(this.FinancialStartDate)
                }
            });
        }*/
    
    
        return true;
    },
    
    setCurrencyAndTax:function(F,nval,oval){
        if ((this.taxStore.getCount()<= 0) && (this.country.getValue() == 203 || this.country.getValue() == 137 || this.country.getValue() == 106)) {//Singapore || Malasiya || Indonesion
            this.taxApplyDate.setValue(this.FinancialStartDate);
                if (this.country.getValue() == 137 || this.country.getValue() == 106) {//Malasiya
                    this.taxStore.load({
                        params: {
                            countryid: this.country.getValue(),
                            financialYrStartDate: WtfGlobal.convertToGenericDate(this.FinancialStartDate)
                        }
                    });
                } else {
                    this.taxStore.load();//Singapore
                }
            }
        //this.currencyGrid.store.load({params:{currencyid:Wtf.getCmp('currencyrecid').getValue()}});
        if (this.currencyGrid.store.getCount() > 0) {
            this.currencyStore.filterBy(function(rec) {
                if (rec.data.tocurrencyid == rec.data.fromcurrencyid) {
                    rec.data.todate = new Date(5000, 12, 31);
                    return false;
                }
                this.FinancialStartDate = new Date(this.finanyear.getValue(), this.fmonth.getValue(), this.fdays.getValue());
                var toDate = this.FinancialStartDate;                  // To Date for Exchange Rate
                this.ToDateForExchangeRate = toDate.add(Date.DAY, 30);
                rec.data.applydate = this.FinancialStartDate;
                rec.data.todate = this.ToDateForExchangeRate;
                return true;
            }, this);
        }
                return true;
    },
    
    getBookBeginingDate: function(v,m,rec){
//        var year = Wtf.serverDate?Wtf.serverDate.getFullYear():new Date().getFullYear();
        var byear = (this.byear.getValue()!=""||this.byear.getValue()!=0)?this.byear.getValue():new Date().getFullYear();
//        if(this.bmonth.getValue()<this.fmonth.getValue()
//                ||(this.bmonth.getValue()==this.fmonth.getValue()&&this.bdays.getValue()<this.fdays.getValue())){
        if((this.byear.getValue() < this.finanyear.getValue()) || 
            (this.byear.getValue() == this.finanyear.getValue() && this.bmonth.getValue() < this.fmonth.getValue()) || 
            (this.byear.getValue() == this.finanyear.getValue() && this.bmonth.getValue() == this.fmonth.getValue() && this.bdays.getValue() < this.fdays.getValue())){
             byear++;
        }
        this.comparebookbegyears();
        var bmonth = (this.bmonth.getValue()==0 || this.bmonth.getValue()!="")?this.bmonth.getValue():new Date().getMonth();
        var bday = (this.bdays.getValue()==0 || this.bdays.getValue()!="")?this.bdays.getValue():new Date().getDay();
        var isvaliddate= this.checkdate(bday, bmonth, byear);
        if(!isvaliddate){
            this.showMessage(WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.setupWizard.note32"));
            return false;
        }
        this.BookStartDate = new Date(byear,bmonth,bday);
        return true;
    },   
    comparebookbegyears:function(){//when combo is selected of book beginning years

        var fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
        var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
        var originalbdate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);
        if(new Date(fdate)>new Date(bdate)){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.information"),
                msg: WtfGlobal.getLocaleText("acc.common.FinanBookAlert"),
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO
            });
            this.bdays.setValue(this.fdays.getValue());
            this.bmonth.setValue(this.fmonth.getValue());
            this.byear.setValue(this.finanyear.getValue());
            this.bmonth.collapse();
        }
    },
       comparefinancialyears:function(fromCard5){//when combo is selected of financial years
        var fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
        var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
        this.FinancialStartDate =fdate;
        if(!fromCard5){
            this.bdays.setValue(this.fdays.getValue());
            this.bmonth.setValue(this.fmonth.getValue());
            this.byear.setValue(this.finanyear.getValue());
            this.bmonth.collapse();
        }
    },
    
    GSTEnableHandler:function(c,checked){
        var selectedCountry=Wtf.getCmp('countryreccombo').getValue();
        if(selectedCountry == "137"){
            if(checked){
                this.taxStore.load({
                    params:{
                        countryid:selectedCountry,
                        financialYrStartDate:WtfGlobal.convertToGenericDate(this.FinancialStartDate)
                    }
                });
                
                this.taxName.enable();
                this.taxDescription.enable();
                this.taxCode.enable();
                this.taxPercent.enable();
                this.taxApplyDate.enable();
                this.taxSaveButton.enable();
                
                this.taxName.allowBlank=false;
                this.taxCode.allowBlank=false;
                this.taxPercent.allowBlank=false;
                
            }else{
                this.taxStore.removeAll();
                
                this.taxName.allowBlank=true;
                this.taxDescription.allowBlank=true;
                this.taxCode.allowBlank=true;
                this.taxPercent.allowBlank=true;
                
                this.taxName.setValue('');
                this.taxDescription.setValue('');
                this.taxCode.setValue('');
                this.taxPercent.setValue('');
//                this.taxApplyDate.setValue();
                
                this.taxName.disable();
                this.taxDescription.disable();
                this.taxCode.disable();
                this.taxPercent.disable();
                this.taxApplyDate.disable();
                this.taxSaveButton.disable();
            }
            
        }
    },
    
    multiEntityHandler: function (c, checked) {
        if (checked) {
            if (this.country.getValue() != Wtf.Country.INDIA && this.country.getValue() != Wtf.Country.US) {
                // If Country Is India OR US then MessageBox will not be Shown.
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.gst.activate.multiEntity"),
                    msg: WtfGlobal.getLocaleText("acc.gst.entity.activateEntity"),
                    scope: this,
                    buttons: Wtf.MessageBox.YESNO,
                    icon: Wtf.MessageBox.INFO,
                    width: 300,
                    fn: function (btn) {
                        if (btn == "yes") {
                        } else {
                            this.isMultiEntityCheckbox.setValue(false);
                        }
                    }
                }, this);
            }
    }
    },

    checkdate:function(d,m,y){
        var yl=1900; // least year to consider
        var ym=2100; // most year to consider
        if (m<0 || m>11) return(false);
        if (d<1 || d>31) return(false);
        if (y<yl || y>ym) return(false);
        if (m==3 || m==5 || m==8 || m==10)
        if (d==31) return(false);
        if (m==1)
        {
        var b=parseInt(y/4);
        if (isNaN(b)) return(false);
        if (d>29) return(false);
        if (d==29 && ((y/4)!=parseInt(y/4))) return(false);
        }
        return(true);
    },

    getCurrencyDetails: function(){
        var currencyDatails = "";
        this.currencyStore.clearFilter();
        for(var i=0; i<this.currencyStore.getCount(); i++){
            var rec = this.currencyStore.getAt(i);
            currencyDatails += "{erid:\""+rec.data.id+"\"," +
                            "tocurrency:\""+rec.data.tocurrency+"\"," +
                            "tocurrencyid:\""+rec.data.tocurrencyid+"\","+
                            "fromcurrencyid:\""+rec.data.fromcurrencyid+"\","+
                            "exchangerate:\""+encodeURI(rec.data.exchangerate)+"\","+
                            "foreigntobaseexchangerate:\""+encodeURI(rec.data.foreigntobaseexchangerate)+"\","+
                            "applydate:\""+WtfGlobal.convertToGenericDate(rec.data.applydate.clearTime())+"\","+
                            "todate:\""+WtfGlobal.convertToGenericEndDate(rec.data.todate.clearTime())+"\"},";
        }
        if(currencyDatails.length>0) {
            currencyDatails = currencyDatails.substring(0, currencyDatails.length-1);
        }
        currencyDatails = "["+currencyDatails+"]"
        // Filter
        this.currencyStore.filterBy(function(rec){
            if(rec.data.tocurrencyid==rec.data.fromcurrencyid)
                return false
            else
                return true
        },this);
        return currencyDatails;
    },

    getTaxDetails: function(){
        var taxDatails = "";
        for(var i=0; i<this.taxStore.getCount(); i++){
            var rec = this.taxStore.getAt(i);
            taxDatails += "{name:\""+encodeURI(rec.data.taxname)+"\"," +
                            "description:\""+encodeURI(rec.data.taxdescription)+"\"," +
                            "code:\""+encodeURI(rec.data.taxcode)+"\"," +
                            "percent:\""+rec.data.percent+"\","+
                            "mastertypevalue:\""+rec.data.mastertypevalue+"\","+
                            "gstaccountid:\""+rec.data.gstaccountid+"\","+
                            "salestaxaccountid:\""+rec.data.salestaxaccountid+"\","+
                            "taxtype:\""+rec.data.taxtype+"\","+
                            "applydate:\""+WtfGlobal.convertToGenericDate(this.FinancialStartDate)+"\","+
                            "defaulttaxid:\""+rec.data.taxid+"\"},"; // defaultgst table taxid
                       }
        if(taxDatails.length>0) {
            taxDatails = taxDatails.substring(0, taxDatails.length-1);
        }
        taxDatails = "["+taxDatails+"]"
        return taxDatails;
    },
getaccGroupDetails: function(){
        var accGroupDetails = "";
        for(var i=0; i<this.accStore.getCount(); i++){
            var rec = this.accStore.getAt(i);
            accGroupDetails += "{groupname:\""+(rec.data.groupname)+"\"," +
                            "nature:\""+(rec.data.nature)+"\"," +
                            "affectgp:\""+rec.data.affectgp+"\","+
                             "groupid:\""+rec.data.groupid+"\","+
                             "isMasterGroup:\""+rec.data.isMasterGroupD+"\","+
                            "parentid:\""+rec.data.parentid+"\"},";
                       }
        if(accGroupDetails.length>0) {
            accGroupDetails = accGroupDetails.substring(0, accGroupDetails.length-1);
        }
        accGroupDetails = "["+accGroupDetails+"]"
        return accGroupDetails;
    },
    getBankDetails: function(){
        var bankDatails = "";
        for(var i=0; i<this.bankStore.getCount(); i++){
            var rec = this.bankStore.getAt(i);
            bankDatails += "{name:\""+encodeURI(rec.data.bankname)+"\"," +
                            "ifsccode:\""+encodeURI(rec.data.ifsccode)+"\"," +
                            "micrcode:\""+encodeURI(rec.data.micrcode)+"\"," +
                            "accountname:\""+encodeURI(rec.data.accountname)+"\"," +
                            "no:\""+rec.data.accountcode+"\"," +
                            "accountno:\""+rec.data.accountno+"\"," +
                            "balance:\""+encodeURI(rec.data.balance)+"\","+
                             "mastertypevalue:\""+rec.data.mastertypevalue+"\","+
                             "accounttype:\""+rec.data.accounttype+"\","+
                             "bankbranchname:\""+rec.data.bankbranchname+"\","+
                             "bankbranchaddress:\""+rec.data.bankbranchaddress+"\","+
                             "branchstate:\""+rec.data.branchstate+"\","+
                             "bsrcode:\""+rec.data.bsrcode+"\","+
                             "pincode:\""+rec.data.pincode+"\","+
                            "applydate:\""+WtfGlobal.convertToGenericDate(rec.data.applydate.clearTime())+"\"},";
        }
        if(bankDatails.length>0) {
            bankDatails = bankDatails.substring(0, bankDatails.length-1);
        }
        bankDatails = "["+bankDatails+"]"
        return bankDatails;
    },

    setLockYearValues:function(){
        this.lockds.each(function(rec){
            var year=rec.data.name;
            var date=new Date(year,this.fmonth.getValue(),this.fdays.getValue());
            date=date.dateFormat(WtfGlobal.getOnlyDateFormat(date));
            rec.set('sdate',date);
            date=new Date(++year,this.fmonth.getValue(),this.fdays.getValue());
            date=date.add(Date.DAY, -1);
            date=date.dateFormat(WtfGlobal.getOnlyDateFormat(date));
            rec.set('edate',date);
          },this)
    },

    getLockYearDetails: function(){
//        this.setLockYearValues();
        var lockDatails = "";
        for(var i=0; i<this.lockds.getCount(); i++){
            var rec = this.lockds.getAt(i);
            lockDatails += "{id:\""+rec.data.id+"\"," +
                            "islock:\""+rec.data.islock+"\","+
                            "name:\""+rec.data.name+"\"},";
        }
        if(lockDatails.length>0) {
            lockDatails = lockDatails.substring(0, lockDatails.length-1);
        }
        lockDatails = "["+lockDatails+"]"
        return lockDatails;
    },

    getSetUpData: function() {
        var setUpData = "";
        var defaultIndex = this.typeStore.find("id", this.companyTypeID);
        var UomSchemaType=0;
        if (this.UomScema.getValue()) {//UOM Schema
            UomSchemaType = 0;
        } else if (this.PackagingUom.getValue()) {//Packaging UOM
            UomSchemaType = 1;
        }
           
        setUpData += "countryid:\""+this.country.getValue()+"\",";
        setUpData += "stateid:\""+this.state.getValue()+"\",";
        setUpData += "vatNumber:\""+this.vat.getValue()+"\",";
        setUpData += "cstNumber:\""+this.cst.getValue()+"\",";
        if(this.country.getValue() == Wtf.Country.INDONESIA){
            //In back end, saving npwp number as pan no.
            setUpData += "panNumber:\""+this.npwp.getValue()+"\",";
        }else{
            setUpData += "panNumber:\""+this.pan.getValue()+"\",";
        }
        setUpData += "serviceTaxRegNumber:\""+this.service.getValue()+"\",";
        setUpData += "tanNumber:\""+this.tan.getValue()+"\",";
        setUpData += "eccNumber:\""+this.ecc.getValue()+"\",";
        if(this.registrationTypeCombo.getValue() != undefined && this.registrationTypeCombo.getValue() != ""){
            setUpData += "registrationType:\""+Wtf.registrationTypeStore.getAt(Wtf.registrationTypeStore.find("id",this.registrationTypeCombo.getValue())).data.name+"\",";
        }
        if(!Wtf.isEmpty(this.GSTIN) && !Wtf.isEmpty(this.GSTIN.getValue())){
            setUpData += "GSTIN:\""+this.GSTIN.getValue()+"\",";
        }
        setUpData += "currencyid:\""+this.currency.getValue()+"\",";
        setUpData += "onlybasecurrencyflag:\""+this.onlyBaseCurrency.getValue()+"\",";
        setUpData += "activateToDateforExchangeRates:\""+this.activateToDateforExchangeRates.getValue()+"\",";
        setUpData += "companyTypeId:\""+this.companyTypeID+"\",";
        setUpData += "companyType:\""+this.typeStore.getAt(defaultIndex).data.name+"\",";
        setUpData += "addDefaultAccountType:\""+this.defaultAcc.getForm().getValues().addDefaultAccountType+"\",";
        setUpData += "addDefaultAccount:\""+this.defaultAccountForm.getForm().getValues().addDefaultAccount+"\",";
        setUpData += "currencyDetails:"+this.getCurrencyDetails()+",";

        setUpData += "yearStartDate:\""+WtfGlobal.convertToGenericDate(this.FinancialStartDate)+"\",";
        setUpData += "bookStartDate:\""+WtfGlobal.convertToGenericDate(this.BookStartDate)+"\",";

        setUpData += "lockDetails:"+this.getLockYearDetails()+",";
        setUpData += "taxDetails:"+this.getTaxDetails()+",";
        setUpData += "accGroupDetails:"+this.getaccGroupDetails()+",";
        setUpData += "bankDetails:"+this.getBankDetails()+",";
        setUpData += "withInventory:\""+this.withInventory.getValue()+"\",";
        setUpData += "activateInventory:\""+this.withInvIntegration.getValue()+"\",";
        setUpData += "withInvUpdate:\""+this.withInvUpdate.getValue()+"\",";
        setUpData += "UomSchemaType:\""+UomSchemaType+"\",";
        setUpData += "isLocationCompulsory:\""+this.isLocationCompulsory.getValue()+"\",";
        setUpData += "isWarehouseCompulsory:\""+this.isWarehouseCompulsory.getValue()+"\",";
        setUpData += "isRowCompulsory:\""+this.isRowCompulsory.getValue()+"\",";
        setUpData += "isRackCompulsory:\""+this.isRackCompulsory.getValue()+"\",";
        setUpData += "isBinCompulsory:\""+this.isBinCompulsory.getValue()+"\",";
        setUpData += "isBatchCompulsory:\""+this.isBatchCompulsory.getValue()+"\",";
        setUpData += "isSerialCompulsory:\""+this.isSerialCompulsory.getValue()+"\",";
        setUpData += "productPricingOnBands:\""+this.productPricingOnBands.getValue()+"\",";
        setUpData += "productPricingOnBandsForSales:\""+this.productPricingOnBandsForSales.getValue()+"\",";
        setUpData += "activatemrpmodule:\"" + this.activatemrpmodule.getValue() + "\",";
        if (this.periodicInventory.getValue()) {
            setUpData += "inventoryvaluationtype:\"" + 0 + "\",";
        } else if (this.perpetualInventory.getValue()) {
            setUpData += "inventoryvaluationtype:\"" + 1 + "\",";
        }
        
        
        if(this.country.getValue() == '137'){// malasian country
            setUpData += "enableGST:"+this.enableGST.getValue()+",";
//            setUpData += "gstEffectiveDate:\""+WtfGlobal.convertToGenericDate(this.FinancialStartDate)+"\",";
        }
        
        setUpData += "isMultiEntity:"+this.isMultiEntityCheckbox.getValue()+",";

        if(document.getElementById("accWizardLinkImage8").className.indexOf("img-visited") != "-1")
        	setUpData += "withTax1099:"+this.tax1099PreferencesForm.getForm().getValues().withTax1099;
        else
        	setUpData += "withTax1099:false";

        setUpData = "{"+setUpData+"}";
        return setUpData;
    },

    getPreviewTemplate: function() {
        var defaultIndex = this.typeStore.find("id", this.companyTypeID);
        var previwHTML =
            "<div style='padding:10px;font-size:11px;'>"+
                "<div>"+WtfGlobal.getLocaleText("acc.setupWizard.note13")+"</div><br/>"+
                "<div>"+WtfGlobal.getLocaleText("acc.setupWizard.note14")+"</div><br/>"+ "</div>"

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.rem.213")+"</b></span>: "+Wtf.countryStore.getAt(Wtf.countryStore.find("id", this.country.getValue())).data.name+"</div>";
                if(this.country.getValue() == Wtf.Country.INDIA) {
                    if(!Wtf.isEmpty(this.GSTIN) && !Wtf.isEmpty(this.GSTIN.getValue())){
                        previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText('acc.india.vecdorcustomer.column.gstin') + "</b></span>: " + this.GSTIN.getValue() + "</div>";
                    }
                }else if(this.country.getValue() == Wtf.Country.INDONESIA) {
                    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>" + WtfGlobal.getLocaleText("acc.setupwizard.npwp") + "</b></span>: " + this.npwp.getValue() + "</div>";
                }
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.rem.214")+"</b></span>: "+this.currencyrecStore.getAt(this.currencyrecStore.find("currencyid", this.currency.getValue())).data.name+"</div>";

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.FinYrDate")+"</b></span>: "+this.FinancialStartDate.format(WtfGlobal.getOnlyDateFormat())+"</div>";
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.BookBeginingDate")+"</b></span>: "+this.BookStartDate.format(WtfGlobal.getOnlyDateFormat())+"</div>";

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.indType")+"</b></span>: "+this.typeStore.getAt(defaultIndex).data.name+"</div>";

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.note38")+"</b></span>: ";
                if(this.defaultAcc.getForm().getValues().addDefaultAccountType=="Yes"){
                    previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.added");  //"Added";
                } else {
                    previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.notAdded");  //"Not Added";
                }
                  previwHTML += "</div>";
                 previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.note33")+"</b></span>: ";
               if(this.defaultAccountForm.getForm().getValues().addDefaultAccount=="Yes"){
                    previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.added");  //"Added";
                } else {
                    previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.notAdded");  //"Not Added";
                } 
                previwHTML += "</div>";

                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.invPref")+"</b></span>: ";
                    if(this.inventoryPreferencesForm.getForm().getValues().activateInventory=="on"){
                        previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note34");  //"With Inventory";
                        if(this.inventoryPreferencesForm.getForm().getValues().withInvUpdate=="Yes") {
                            previwHTML += " + " + WtfGlobal.getLocaleText("acc.setupWizard.note34_a");  //"With Trading Flow";
                        }
                    } else {
                        previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note35");  //"Without Inventory";
                    }
                    previwHTML += "</div>";

                if(document.getElementById("accWizardLinkImage8").className.indexOf("img-visited") != "-1") {
                    previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.tax1099Pref")+"</b></span>: ";
                    if(this.tax1099PreferencesForm.getForm().getValues().withTax1099=="Yes"){
                        previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note36");  //"With Tax 1099";
                    } else {
                        previwHTML += WtfGlobal.getLocaleText("acc.setupWizard.note37");  //"Without Tax 1099";
                    }
                    previwHTML += "</div>";
                }

var rowStyle = "width: 20%; display: block; float: left; overflow:hidden; padding-left:3px;";
var rowHeaderStyle = rowStyle+" border-bottom: 1px solid #E8E8E8;";

                var currencyDatails = "";
                if(this.currencyStore.getCount()>0) {
                    if(this.activateToDateforExchangeRates.getValue()){
                        currencyDatails = "<span style='"+rowHeaderStyle+" width: 10px;'>&nbsp;</span>" +
                                    "<span style='"+rowHeaderStyle+" width: 20px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.sno")+"</span>" +
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.cur")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.basetoforeignexRate")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.foreigntobaseexRate")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width:25%'>"+WtfGlobal.getLocaleText("acc.setupWizard.conv")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width:10%'>"+WtfGlobal.getLocaleText("acc.setupWizard.FromDate")+"</span>" + 
                                    "<span style='"+rowHeaderStyle+" width:10%'>"+WtfGlobal.getLocaleText("acc.setupWizard.ToDate")+"</span>" +
                                    "<br style='clear:both'/>";
                    }else{
                        currencyDatails = "<span style='"+rowHeaderStyle+" width: 10px;'>&nbsp;</span>" +
                                    "<span style='"+rowHeaderStyle+" width: 40px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.sno")+"</span>" +
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.cur")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.basetoforeignexRate")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"'>"+WtfGlobal.getLocaleText("acc.setupWizard.foreigntobaseexRate")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width:30%'>"+WtfGlobal.getLocaleText("acc.setupWizard.conv")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width:15%'>"+WtfGlobal.getLocaleText("acc.setupWizard.appDate")+"</span>"+
                                    "<br style='clear:both'/>";
                    }

                    for(var i=0; i<this.currencyStore.getCount(); i++){
                        var rec = this.currencyStore.getAt(i);
                        if(this.activateToDateforExchangeRates.getValue()){
                            currencyDatails += "<span style='"+rowStyle+" width: 10px;'>&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 20px;'>"+(i+1)+".&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.tocurrency+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.exchangerate+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.foreigntobaseexchangerate+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width:25%'>"+"1 "+rec.data.fromcurrency+" = "+rec.data.exchangerate+" "+rec.data.tocurrency+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width:10%'>"+(rec.data.applydate).format(WtfGlobal.getOnlyDateFormat())+"&nbsp;</span>" + 
                                        "<span style='"+rowStyle+" width:10%'>"+(rec.data.todate).format(WtfGlobal.getOnlyDateFormat())+"&nbsp;</span>" +
                                        "<br style='clear:both'/>" ;
                        }else{
                            currencyDatails += "<span style='"+rowStyle+" width: 10px;'>&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 40px;'>"+(i+1)+".&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.tocurrency+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.exchangerate+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+"'>"+rec.data.foreigntobaseexchangerate+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width:30%'>"+"1 "+rec.data.fromcurrency+" = "+rec.data.exchangerate+" "+rec.data.tocurrency+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width:15%'>"+(rec.data.applydate).format(WtfGlobal.getOnlyDateFormat())+"&nbsp;</span>"+
                                        "<br style='clear:both'/>" ;
                        }
                    }
                    currencyDatails = "<br/><div style='padding-left:40px'>"+currencyDatails+"</div>";
                } else {
                    currencyDatails = " "+WtfGlobal.getLocaleText("acc.setupWizard.note12");
                }
                previwHTML += "<div class='accSWdata'><span style='float:left;width:170px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.surrexrates")+"</b></span>: "+currencyDatails+"</div>";

                var taxDatails = "";
                /*
                For Companies  Except Indian Copany tax details will be showed
                  */
                if(this.taxStore.getCount()>0&& (this.country.getValue() != Wtf.Country.INDIA)) {
                    taxDatails = "<span style='"+rowHeaderStyle+" width: 10px;'>&nbsp;</span>" +
                                    "<span style='"+rowHeaderStyle+" width: 40px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.sno")+"</span>" +
                                    "<span style='"+rowHeaderStyle+" '>"+WtfGlobal.getLocaleText("acc.setupWizard.taxnam")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" '>"+WtfGlobal.getLocaleText("acc.setupWizard.gridTaxDescription")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width: 17%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.taxcod")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width: 12%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.per")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width: 15%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.appDate")+"</span>"+
                                    "<br style='clear:both'/>";

                    for(i=0; i<this.taxStore.getCount(); i++){
                        rec = this.taxStore.getAt(i);
                        taxDatails += "<span style='"+rowStyle+" width: 10px;'>&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 40px;'>"+(i+1)+".&nbsp;</span>" +
                                        "<span style='"+rowStyle+" '>"+rec.data.taxname+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" '>"+rec.data.taxdescription+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 17%;'>"+rec.data.taxcode+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 12%;'>"+rec.data.percent+"%&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 15%;'>"+(this.FinancialStartDate).format(WtfGlobal.getOnlyDateFormat())+"&nbsp;</span>"+
                                        "<br style='clear:both'/>" ;
                    }
                    taxDatails = "<br/><div style='padding-left:40px'>"+taxDatails+"</div>";
                } else {
                    taxDatails = WtfGlobal.getLocaleText("acc.setupWizard.note12");  //" Details not provided, can be entered later.";
                }
                previwHTML += "<div class='accSWdata'><span style='float:left;width:70px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.taxDetails")+"</b></span>: "+taxDatails+"</div>";

                var bankDatails = "";
                if(this.bankStore.getCount()>0) {
                    bankDatails = "<span style='"+rowHeaderStyle+" width: 10px;'>&nbsp;</span>" +
                                    "<span style='"+rowHeaderStyle+" width: 40px;'>"+WtfGlobal.getLocaleText("acc.setupWizard.sno")+"</span>" +
                                    "<span style='"+rowHeaderStyle+" width: 15%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.BankNam")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width: 25%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.IFSCCode")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width: 15%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.MICRCode")+"</span>"+
                                    "<span style='"+rowHeaderStyle+" width: 25%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.AccountNam")+"</span>"+
                                    "<span style='"+rowHeaderStyle+"width: 15%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.AccountNo")+"</span>"+
                                   "<span style='"+rowHeaderStyle+" width: 15%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.openBal")+"</span>"+
                                   "<span style='"+rowHeaderStyle+" width: 15%;'>"+WtfGlobal.getLocaleText("acc.setupWizard.appDate")+"</span>"+
                                   "<br style='clear:both'/>";

                    for(i=0; i<this.bankStore.getCount(); i++){
                        rec = this.bankStore.getAt(i);
                        bankDatails += "<span style='"+rowStyle+" width: 10px;'>&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 40px;'>"+(i+1)+".&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 15%;'>"+rec.data.bankname+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 15%;'>"+rec.data.ifsccode+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 15%;'>"+rec.data.micrcode+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 25%;'>"+rec.data.accountname+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 15%;'>"+rec.data.accountno+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 15%;'>"+rec.data.balance+"&nbsp;</span>" +
                                        "<span style='"+rowStyle+" width: 15%;'>"+(rec.data.applydate).format(WtfGlobal.getOnlyDateFormat())+"&nbsp;</span>" +
                                        "<br style='clear:both'/>" ;
                    }
                    bankDatails = "<br/><div style='padding-left:40px'>"+bankDatails+"</div>";
                } else {
                    bankDatails = WtfGlobal.getLocaleText("acc.setupWizard.note12");  //" Details not provided, can be entered later.";
                }
                previwHTML += "<div class='accSWdata'><span style='float:left;width:80px'><b>"+WtfGlobal.getLocaleText("acc.setupWizard.bankDetails")+"</b></span>: "+bankDatails+"</div>";
        previwHTML += "</div>";

        return previwHTML;
    },
    natureRenderer:function(val){
        switch(val){
            case Wtf.account.nature.Asset:return "Asset";
            case Wtf.account.nature.Liability:return "Liability";
            case Wtf.account.nature.Expences:return "Expenses";
            case Wtf.account.nature.Income:return "Income";
        }
    },
    updateRow:function(obj){
        if(obj!=null){
            var rec=obj.record;
            if(obj.field=="applydate" && (obj.value > rec.data.todate)){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    rec.set("applydate",obj.originalValue);
            } 
            if(obj.field=="todate" && (obj.value < rec.data.applydate)){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                    rec.set("todate",obj.originalValue);
            }             
            if(obj.field=="exchangerate"){
                if(obj.value != obj.originalValue){
                    var revExchangeRate = 1/((rec.get('exchangerate')*1)-0);
                    revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
                    obj.record.set("foreigntobaseexchangerate", revExchangeRate);
                }
            }
            if(obj.field=="foreigntobaseexchangerate"){
                if(obj.value != obj.originalValue){
                    var revExchangeRate = 1/((rec.get('foreigntobaseexchangerate')*1)-0);
                    revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
                    obj.record.set("exchangerate", revExchangeRate);
                }
            }
        }
    },
    onCountrySelect: function(){
        this.defaultAccountStore.load({
            params:{
                companyType:this.companyTypeID,
                country:this.country.getValue(),
                state:this.state.getValue()
            }
        });        
        this.accStore.load({ //only for indian company - ERP-20822
            params:{
                country:this.country.getValue()
            }
        });
        
        var selectedCountry=Wtf.getCmp('countryreccombo').getValue();
        if(selectedCountry != '244') {
            this.updateLinkState("7","none");
        }
        if(this.isTaxStoreRemoveFlag){
        this.taxStore.removeAll();
        }
        WtfGlobal.hideFormElement(this.enableGST);
        this.gstcmbAccount.allowBlank=true;
        WtfGlobal.hideFormElement(this.gstcmbAccount);
        if(this.taxCm){
            var gstaccountnameColIndex = this.taxCm.getIndexById('gstaccountnamecolumnid');
            if(gstaccountnameColIndex>0)
                this.taxCm.setHidden(gstaccountnameColIndex, true);
        }
        this.taxApplyDate.minValue='';
        /*
         * Added countryid of United States(244) for loading Default taxes on combobox select 
         */
        if((this.taxStore.getCount()<= 0 )&& (selectedCountry==203 || selectedCountry==137 || selectedCountry==106 || selectedCountry==244 || selectedCountry == Wtf.Country.PHILIPPINES)){//Singapore || Malasiya || Indonesion || United States||Philippines
            if(selectedCountry==137){//Malasiya
                this.taxApplyDate.minValue=this.FinancialStartDate;
                this.isMalasianCompany=true;
                WtfGlobal.showFormElement(this.enableGST);
                this.gstcmbAccount.allowBlank=false;
                WtfGlobal.showFormElement(this.gstcmbAccount);
                this.gstaccStore.load();
                this.taxStore.load({
                    params:{
                        countryid:selectedCountry,
                        financialYrStartDate:WtfGlobal.convertToGenericDate(this.FinancialStartDate)
                    }
                });

                // hide Gst Account column

                if(this.taxCm){
                    var gstaccountnameColIndex = this.taxCm.getIndexById('gstaccountnamecolumnid');
                    if(gstaccountnameColIndex>0)
                        this.taxCm.setHidden(gstaccountnameColIndex, false);
                }

            }else if(selectedCountry==106 || selectedCountry == 203 || selectedCountry==244 || selectedCountry == Wtf.Country.PHILIPPINES){
                this.taxStore.load({
                    params:{
                        countryid:selectedCountry,
                        financialYrStartDate:WtfGlobal.convertToGenericDate(this.FinancialStartDate)
                    }
                });
            }else{
                this.taxStore.load();
            }
        }
    }
});
