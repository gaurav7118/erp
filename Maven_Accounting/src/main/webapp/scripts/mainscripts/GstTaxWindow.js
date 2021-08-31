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
Wtf.account.GstTaxWindow = function(config){
    this.reconRec=(config.reconRec==undefined?"":config.reconRec);
     Wtf.apply(this,{
        title: WtfGlobal.getLocaleText("acc.taxReport.GSTReport"),
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.field.GenerateReport"),
            scope: this,
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.close"),
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.GstTaxWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.GstTaxWindow, Wtf.Window, {

    onRender: function(config){
        var image="../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.GstTaxWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createForm();
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml('GST Report','GST Report Filter',image,false)
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
	   this.gstReportTypes = [['1', 'GST Transaction Report (Detailed)'],['2', 'GST Transaction Report (Summarised)'],
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
                items:[this.Account,this.startDate,this.statementDate]
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
	    	var url = "ACCReports/exportGSTReport.do?stdate="+WtfGlobal.convertToGenericDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericDate(this.statementDate.getValue())+"&reportType="+this.Account.getValue()+"&withoutinventory="+Wtf.account.companyAccountPref.withoutinventory;
	    	Wtf.get('downloadframe').dom.src = url;
    	}
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
