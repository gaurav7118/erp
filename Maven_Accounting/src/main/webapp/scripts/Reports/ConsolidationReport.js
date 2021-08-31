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

function callConsolidationReportTab(){
    var panel = Wtf.getCmp("callConsolidationReportTab");
    if(!panel){// If panel is already not open
        Wtf.Ajax.requestEx({
            url:"ACCReports/getMappedCompanies.do",
            params:{
                includeParentCompany:true
            }
        },this,function(response){
            if(response.success){  
                panel= new Wtf.account.consolidationReportTab({
                    id:'callConsolidationReportTab',
                    layout:'fit',
                    border: false,
                    tabTip : WtfGlobal.getLocaleText("acc.conslodation.ConsolidationReport"),
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.conslodation.ConsolidationReport"),Wtf.TAB_TITLE_LENGTH),
                    closable:true,
                    subdomainArray:response.data
                })
                Wtf.getCmp('as').add(panel);
               
                Wtf.getCmp('as').setActiveTab(panel);
                Wtf.getCmp('as').doLayout();
            } else{
            
            }
        },function(response){

        });
    } else{
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
}

Wtf.account.consolidationReportTab = function (config){
    Wtf.apply(this, config);
    Wtf.account.consolidationReportTab.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.consolidationReportTab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.consolidationReportTab.superclass.onRender.call(this, config);
        this.add({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid,
            tbar:this.buttonArray,
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.store,           
                displayInfo: true,
                searchField: this.quickPanelSearch,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
        });
    },
    initComponent:function(config){
        Wtf.account.consolidationReportTab.superclass.initComponent.call(this, config);
        //Create Button
         this.createButton();
         
        //below method create 
          this.createStoreAndGrid();
    },

    createStoreAndGrid:function(){
        var recordsArray = [];
        var columnArray =[];
        recordsArray.push('accountid', 'acccode','accountname');
        this.rowNo=new Wtf.grid.RowNumberer()
        columnArray.push(this.rowNo, 
        {
            header :'',
            hidden : true,
            dataIndex: 'accountid',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer
        },{
            header :WtfGlobal.getLocaleText("acc.conslodation.accountcode"),
            dataIndex: 'acccode',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer  
        },{
            header :WtfGlobal.getLocaleText("acc.balanceSheet.particulars"),
            dataIndex: 'accountname',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer 
        });
        for(var index=0;index<this.subdomainArray.length ;index++){
            var subdomainRecord=this.subdomainArray[index];
            if(subdomainRecord!="" && subdomainRecord!=undefined){
                var subdomainname = subdomainRecord.subdomain;
                var companyname = subdomainRecord.companyname;
                recordsArray.push(subdomainname+'_openingamount', subdomainname+'_periodamount',subdomainname+'_endingamount');
                columnArray.push({
                    header :companyname+" "+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+")",
                    dataIndex: subdomainname+'_openingamount',
                    width:200,
                    align:'right',
                    pdfwidth:150,
                    renderer:WtfGlobal.withoutRateCurrencySymbol
                },{
                    header :companyname+" "+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")",
                    dataIndex: subdomainname+"_periodamount",
                    width:200,
                    align:'right',
                    pdfwidth:150,
                    renderer:WtfGlobal.withoutRateCurrencySymbol 
                },{
                    header :companyname+" "+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")",
                    dataIndex: subdomainname+"_endingamount",
                    width:200,
                    align:'right',
                    pdfwidth:150,
                    renderer:WtfGlobal.withoutRateCurrencySymbol
                });
            }
        }
        recordsArray.push('totalopeningamount', 'totalperiodamount','totalendingamount');
        columnArray.push({
            header :WtfGlobal.getLocaleText("acc.conslodation.totalopeningamount")+" ("+WtfGlobal.getCurrencyName()+")",
            dataIndex: 'totalopeningamount',
            width:200,
            align:'right',
            pdfwidth:150,
            renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header :WtfGlobal.getLocaleText("acc.conslodation.totalperiodamount")+" ("+WtfGlobal.getCurrencyName()+")",
            dataIndex: "totalperiodamount",
            width:200,
            align:'right',
            pdfwidth:150,
            renderer:WtfGlobal.withoutRateCurrencySymbol 
        },{
            header :WtfGlobal.getLocaleText("acc.conslodation.totalendingamount")+" ("+WtfGlobal.getCurrencyName()+")",
            dataIndex: "totalendingamount",
            width:200,
            align:'right',
            pdfwidth:150,
            renderer:WtfGlobal.withoutRateCurrencySymbol
        });
        this.createStore(recordsArray); 
        this.createGrid(columnArray); 
    },
    createStore:function(recordsArray){
        var record=new Wtf.data.Record.create(recordsArray);  
        this.store=new Wtf.data.Store({
            reader:new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"  
            },record),
            url: "ACCReports/getConsolidationReport.do",
            baseParams:{
                start:0,
                limit:30,
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        }); 
//        this.store.load();
        
        this.store.on('load',function(store){
            this.quickPanelSearch.StorageChanged(store);
        },this);
        this.store.on('beforeload',this.onBeforeStoreLoad,this);
    },
    onBeforeStoreLoad:function(store,obj){
        var startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
        if(this.pP!=undefined){
            if(this.pP.combo.value!="All"){
                obj.params.startdate= startdate;
                obj.params.enddate= enddate;
            }else{
                var count = this.store.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                obj.params.limit = count;
                obj.params.startdate= startdate;
                obj.params.enddate= enddate;
            }
        }else{
            obj.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
            obj.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        }
        if(this.expButton){
            this.expButton.setParams({
                startdate:startdate,
                enddate:enddate
            });
        }
//        if(this.printButton){
//            this.printButton.setParams({
//                startdate:startdate,
//                enddate:enddate
//            });
//        }
    },
    createGrid:function(columnArray){
        this.gridcm= new Wtf.grid.ColumnModel(columnArray);
        this.grid = new Wtf.grid.GridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:(this.currencyhistory?'currencyhistory':'defaultCurrencygrid'),
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            stripeRows : true,
            viewConfig: {
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))
            }
        });
        this.grid.on('render',function(){
            this.grid.getView().applyEmptyText();
        },this);
    },
    createButton:function(){
        this.buttonArray = new Array();
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.coa.accountSearchText"), // "Search by Document Account Name
            width: 150,
            id:"quickSearch"+this.id
        });
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
        this.expButton=new Wtf.exportButton({
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
            id:"exportConsolidationReport"+this.id,
            filename: WtfGlobal.getLocaleText("acc.conslodation.ConsolidationReport")+ "_v1",
            menuItem:{
                csv:true,
                pdf:true,
                rowPdf:false,
                xls:true
            },
            params:{
                startdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false))
            },
            get:Wtf.autoNum.consolidatioReport,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
            isConsolidatedReportExport:true,
            usePostMethod:true
        });
    
//        this.printButton=new Wtf.exportButton({
//            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
//            obj:this,
//            id:"printConsolidationReport"+this.id,
//            filename: WtfGlobal.getLocaleText("acc.conslodation.ConsolidationReport"),
//            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
//            menuItem:{
//                print:true
//            },
//            params:{
//                startdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
//                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false))
//            },
//            get:Wtf.autoNum.consolidatioReport,
//            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
//        });
        this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            // readOnly:true,
            value:this.getDates(true)
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            // readOnly:true,
            value:this.getDates(false)
        });
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
            tooltip: 'Select a subdomian to fetch data.',
            id: 'fetchButton' + this.id,
            scope: this,
            iconCls:'accountingbase fetch',
            handler: this.fetchData.createDelegate(this)
        });
        this.buttonArray.push(this.quickPanelSearch,this.resetBttn,this.startDate,this.endDate,this.fetchBttn,this.expButton);
    },
    getDates:function(start){
        var d=new Date();
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
    handleResetClickNew:function(){
        this.quickPanelSearch.reset();
        this.startDate.reset();
        this.endDate.reset();
        this.fetchData();   
    },
    fetchData:function(){
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                ss: this.quickPanelSearch.getValue()
            }
        });
    }
});