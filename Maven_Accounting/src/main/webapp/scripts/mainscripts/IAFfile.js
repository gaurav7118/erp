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
Wtf.account.IAFfileWindow = function(config){
    this.reconRec=(config.reconRec==undefined?"":config.reconRec);
    this.isGST = (config.isGST==undefined?false:config.isGST);
    this.iseSubmission = (Wtf.isEmpty(config.iseSubmission)?false:config.iseSubmission);
    
    var title = this.isGST?WtfGlobal.getLocaleText("acc.field.GSTForm5"):(Wtf.account.companyAccountPref.countryid=='137'?WtfGlobal.getLocaleText("acc.field.GSTAuditFileGAF"):Wtf.account.companyAccountPref.countryid=='106'? WtfGlobal.getLocaleText("acc.field.IAFAuditFileIAF"):WtfGlobal.getLocaleText("acc.field.IRASAuditFileIAF")); 
    var buttontext = this.isGST?WtfGlobal.getLocaleText("acc.field.ExportGSTForm5"):Wtf.account.companyAccountPref.countryid=='137'?WtfGlobal.getLocaleText("acc.field.ExportGAFFile"):WtfGlobal.getLocaleText("acc.field.ExportIAFFile");
    if(this.iseSubmission){
        title = "GST Transaction Listing";
        buttontext = "Submit"
    }
     Wtf.apply(this,{
        title: title,
        buttons: [{
            text: buttontext,
            scope: this,
            handler:function(){
                var sDate=this.startDate.getValue();
                var eDate=this.statementDate.getValue();
                if(sDate>eDate){
                    WtfComMsgBox(1,2);
                } else{
                    if(this.iseSubmission){
                        this.submitGSTTransactionListing();
                    }else{
                        this.saveData();
                    }
                }
            }
        },{
            text: WtfGlobal.getLocaleText("acc.common.close"),
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.IAFfileWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.IAFfileWindow, Wtf.Window, {

    onRender: function(config){
        var image="../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.IAFfileWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createForm();
        var htmltext = this.isGST?getTopHtml(WtfGlobal.getLocaleText("acc.field.GSTForm5"),WtfGlobal.getLocaleText("acc.field.GSTForm5PDFFile"),image,false):(Wtf.account.companyAccountPref.countryid=='137'?getTopHtml(WtfGlobal.getLocaleText("acc.field.GSTAuditFileGAF"),WtfGlobal.getLocaleText("acc.field.GSTAuditFileGAF"),image,false):Wtf.account.companyAccountPref.countryid=='106'?getTopHtml(WtfGlobal.getLocaleText("acc.field.IAFAuditFileIAF"),WtfGlobal.getLocaleText("acc.field.IAFAuditFileIAFTextFile"),image,false):getTopHtml(WtfGlobal.getLocaleText("acc.field.IRASAuditFileIAF"),WtfGlobal.getLocaleText("acc.field.IRASAuditFileIAFTextFile"),image,false));
        if (this.iseSubmission) {
            htmltext = getTopHtml("GST Transaction Listing","GST Transaction Listing e-Submission",image,false);
        }
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: htmltext
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
//            layout: 'fit',
            height:100,
            items:this.Form
        });
   },
   createStore:function(){
	   this.gstReportTypes = [['1', this.isGST?'GST Form 5':'IAF Text file'],
	                         ];	
	   this.gstReportTypesStore = new Wtf.data.SimpleStore({
	       fields: ['id', 'name'],
	       data : this.gstReportTypes
	   });
   },
   setAccount:function(){
       var value=(this.reconRec==""?"":this.reconRec.accountid)
       this.Account.setValue(value)
   },
    createForm:function(){
    	this.iaf=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Report"),
            name: 'iaf',
            anchor:'70%',
            maxLength:45,
            value:this.isGST?WtfGlobal.getLocaleText("acc.field.GSTForm5"):Wtf.account.companyAccountPref.countryid=='137'?WtfGlobal.getLocaleText("acc.field.GSTAuditFileGAF"):WtfGlobal.getLocaleText("acc.field.IAFTextfile"),
            scope:this,
            cls:"clearStyle",
            readOnly:true,
            hidden : this.iseSubmission,
            hideLabel :this.iseSubmission
        });
        this.Account= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Report")+"*",
            hiddenName:"accountid",
            anchor:"85%",
            store: this.gstReportTypesStore,
            valueField:'id',
            displayField:'name',
            allowBlank:false,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.field.PleaseselectaReportType"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
            value:'1',
            disabled:true,
            scope:this
        });
       this.startDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.startDate")+'*',
            format:WtfGlobal.getOnlyDateFormat(),
            value:(this.reconRec==""?this.getDates(true):this.reconRec.startdate),
            anchor:'60%',
            name:"startdate",
            allowBlank:false
        });
        this.statementDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.endDate")+'*',
            format:WtfGlobal.getOnlyDateFormat(),
            value:(this.reconRec==""?this.getDates(false):this.reconRec.statementdate),
            anchor:'60%',
            name:"statementdate",
            allowBlank:false
        });
//        this.Account.on('select',this.setOpeningBalance,this)
//        this.startDate.on('change',this.setOpeningBalance,this)
        this.beginingBalance=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.openBalance")+' '+WtfGlobal.getCurrencySymbolForForm(),
            name: 'openingbalance',
            anchor:'70%',
            maxLength:45,
            value:(this.reconRec==""?0:this.reconRec.openingbalance),
            scope:this,
            readOnly:true,
            disabled: true	// Bug No 19950 Fixed 
        });
        this.endingBalance=new Wtf.form.NumberField({
            name:"endingbalance",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.endingBalance")+" "+WtfGlobal.getCurrencySymbolForForm()+"*",
            maxLength:15,
            decimalPrecision:2,
            value:(this.reconRec==""?0:this.reconRec.endingbalance),
            anchor:'70%'
        });
         this.Form=new Wtf.form.FormPanel({
            region:'north',
            height:100, //(Wtf.isIE)?220:190,
            border:false,
            bodyStyle: 'overflow: hidden',
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:100,
                border:false,
                height:100,
                items:[this.iaf,this.startDate,this.statementDate]
           }]
        });
    },
//    setOpeningBalance:function(a,rec){
//        Wtf.Ajax.requestEx({
////            url:Wtf.req.account+'CompanyManager.jsp',
//            url:"ACCReports/getAccountOpeningBalance.do",
//            params:{
//                mode:68,
//                stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
//                accountid:this.Account.getValue()
//
//            }
//        }, this,this.setbalance);
//    },

//    setbalance:function(response){
//        if(response.success)
//            this.beginingBalance.setValue(response.data.openingbalance);
//    },

    closeWin:function(){
         this.fireEvent('cancel',this)
         this.close();
     },

    saveData:function(){
    	if(this.Form.getForm().isValid()) {
            if(this.isGST) {
	    	var url = "ACCReports/exportGSTReport.do?stdate="+WtfGlobal.convertToGenericDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericDate(this.statementDate.getValue())+"&reportType="+"3"+"&withoutinventory="+Wtf.account.companyAccountPref.withoutinventory;
            } else {
                var url = "ACCReports/exportIAFfile.do?stdate="+WtfGlobal.convertToGenericDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericDate(this.statementDate.getValue())+"&reportType="+this.Account.getValue();
            }
	    	Wtf.get('downloadframe').dom.src = url;
    	}
    },
    getIRASTLURL:function(chunkids, companyid, callbackURL, description, form5orTLSubmission){
        Wtf.Ajax.requestEx({
            url: "ACCReports/getIRASRedirectingUrl.do",
            method: 'POST',
            params: {
                chunkids: chunkids,
                companyid: companyid,
                callbackURL: callbackURL,
                description: description,
                flag: form5orTLSubmission
            }
        }, this, function (response) {
            WtfGlobal.resetAjaxTimeOut();
             this.loadingMask.hide();
            if (response.success && !Wtf.isEmpty(response.isRedirectUrl)) {
                    window.open(response.data);
            } else {
                WtfGlobal.resetAjaxTimeOut();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Error occurred during process please try after sometime."], 1);
            }
        });
    },
    submitGSTTransactionListing: function () {
            this.loadingMask = new Wtf.LoadMask(this.id, {
                msg: "Preparing Data..."
            });
            this.loadingMask.show();
            WtfGlobal.setAjaxTimeOutFor30Minutes();
            Wtf.Ajax.requestEx({
                url: "ACCReports/gstTransactionListingSubmission.do",
                method: 'POST',
                params: {
                    stdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    enddate: WtfGlobal.convertToGenericDate(this.statementDate.getValue()),
                    startDate: WtfGlobal.convertToDateOnly(this.startDate.getValue()),
                    endDate: WtfGlobal.convertToDateOnly(this.statementDate.getValue()),
                    reportType: this.Account.getValue(),
                    gstRegNo: Wtf.account.companyAccountPref.gstnumber,
                    taxRefNo: Wtf.account.companyAccountPref.taxNumber
                },
            }, this, function (response) {
                this.loadingMask.hide();
                if (response.success) {
                    this.close();
                    WtfGlobal.resetAjaxTimeOut();
                    Wtf.MessageBox.confirm("GST Transaction Listing", "You will be redirected to IRAS for authentication. ", function (btn) {
                        if (btn != "yes") {
                            return;
                        }
                        var chunkids = response.data.chunkids;
                        var companyid = response.data.companyid;
                        var callbackURL = response.data.callbackUrl;
                        var description = response.data.description;
                        var form5orTLSubmission = response.data.flag;
                        this.loadingMask.show();
                        this.getIRASTLURL(chunkids, companyid, callbackURL, description, form5orTLSubmission);
                    },this);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Error occurred during process please try after sometime."], 1);
                    WtfGlobal.resetAjaxTimeOut();
                }
            });
    },

    genSuccessResponse:function(response){
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
         if(response.success) this.fireEvent('update');
        this.store.load();
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    getDates:function(start){
        var d=new Date();
        if(this.statementType=='BalanceSheet'){
            if(start)
                return new Date('January 1, 1970 00:00:00 AM');
            else
                return d;
        }
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    }
});
