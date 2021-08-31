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


function callRevenueRecognitionReportDynamicLoad(){
    var callRevenueRecognitionReportPanel=Wtf.getCmp("RevenueRecognitionReport");
    if(callRevenueRecognitionReportPanel==null){

        callRevenueRecognitionReportPanel = new Wtf.RevenueRecognitionReport({
            id : 'RevenueRecognitionReport',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.RevenueRecognitionReport.tabTitle"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.RevenueRecognitionReport.tabTitleTT"),
            closable: true,
            iconCls:'accountingbase financialreport'
        });
        Wtf.getCmp('as').add(callRevenueRecognitionReportPanel);
    }
    Wtf.getCmp('as').setActiveTab(callRevenueRecognitionReportPanel);
    Wtf.getCmp('as').doLayout();

}

Wtf.RevenueRecognitionReport = function(config) {
    this.msgLmt = 30;
    this.BalanceSummary=0;
    this.RevenueRecognitionReportRec = new Wtf.data.Record.create([{           
        name:'Revenue'
    },{
        name:'Balance'
    }]);
    this.jReader = new Wtf.data.KwlJsonReader({
        totalProperty: 'totalCount',
        root: "data"
    }, this.RevenueRecognitionReportRec);

    this.RevenueRecognitionReportStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.RevenueRecognitionReportRec),
        url : 'ACCReports/RevenueRecognitionReport.do',
            
        baseParams:{
            accountIds:Wtf.account.companyAccountPref.salesAccount+","+Wtf.account.companyAccountPref.salesRevenueRecognitionAccount,
            acctypes:0,
            balPLId:0,
            deleted:false,
            group:12,
            ignore:true,
            isGeneralLedger:true,
            nondeleted:false
        }
        
    });
    this.RevenueRecognitionReportCM = new Wtf.grid.ColumnModel([
        {
            header:"<b>" +WtfGlobal.getLocaleText("acc.RevenueRecognitionReport.gridRevenue") + "</b>",
            width: 200,
            align:'left',
            dataIndex: 'Revenue'
        }, {
            header:"<div align=right><b>"+WtfGlobal.getLocaleText("acc.RevenueRecognitionReport.gridBalance")+" ("+WtfGlobal.getCurrencyName()+") </b></div>",
            renderer:WtfGlobal.currencyDeletedRenderer,
            width: 200,
            align:'right',
            dataIndex: 'Balance'
        }]);
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    
    this.RevenueRecognitionReportGrid = new Wtf.grid.GridPanel({
        store:  this.RevenueRecognitionReportStore,
        cm: this.RevenueRecognitionReportCM,
        width:600,
        viewConfig: {
            emptyText:"<b><font size='5'>" +WtfGlobal.getLocaleText("acc.common.norec.RevenueReport") + "</font></b>",
            forceFit:true
        }
       
    });
     this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    this.RevenueRecognitionReportStore.load();
    this.FetchButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.agedPay.fetch"),
        tooltip: WtfGlobal.getLocaleText("acc.field.FetchRevenue"),
        disabled: false,
        iconCls:'accountingbase fetch',
        scope: this,
        handler:this.FetchRevenue

    });
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[
        this.westPanel = new Wtf.Panel({
            width:'50%',
            region:'center',
            layout:'fit',
            border:false,
            items:this.RevenueRecognitionReportGrid
        }),
        {
            layout:'fit',
            region:'west',
            width:'25%'
        },{
            layout:'fit',
            region:'east',
            width:'25%'
        }]
       
    });
     Wtf.apply(this,{
        defaults:{
            border:false,
            bodyStyle:"background-color:white ;"
        },
        saperate:true,
        items:this.wrapperPanel,
        tbar: [WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,this.FetchButton,this.resetBttn]
    },config);    
    
    Wtf.RevenueRecognitionReport.superclass.constructor.call(this,config);

}

Wtf.extend(Wtf.RevenueRecognitionReport, Wtf.Panel, {
    onRender: function(config) {
        Wtf.RevenueRecognitionReport.superclass.onRender.call(this, config);
        this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue().add(Date.DAY,0));
        this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue().add(Date.DAY,1));
        this.RevenueRecognitionReportStore.load( {
            startDate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            endDate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
        });
    },

    FetchRevenue: function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();

        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return;
        }
        var loadingMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
         
        this.RevenueRecognitionReportStore.on('beforeload', function(){
            loadingMask.show();
        }, this);
        this.RevenueRecognitionReportStore.on('loadexception', function(){
            loadingMask.hide();
        }, this);
        this.RevenueRecognitionReportStore.on('load', function(){
            loadingMask.hide();
        }, this);
    
        this.sdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
        this.edate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
        if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.RevenueRecognitionReportStore.load({
            params:{
                startDate:this.sdate,
                endDate:this.edate
            }
        });
        
    },
    
    handleResetClick:function(){
        this.startDate.reset();
        this.endDate.reset();
        this.RevenueRecognitionReportStore.load();
    }
});

