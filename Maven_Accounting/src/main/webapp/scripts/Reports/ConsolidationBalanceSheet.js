/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function callConsolidationBalanceSheetReportTab(){
    var panel = Wtf.getCmp("consolidationBalanceSheetReportTab");
    if(!panel){// If panel is already not open
        Wtf.Ajax.requestEx({
            url:"ACCReports/getMappedCompanies.do",
            params:{
                includeParentCompany:true
            }
        },this,function(response){
            if(response.success){  
                panel= new Wtf.account.consolidationBalanceSheetReportTab({
                    id:'consolidationBalanceSheetReportTab',
                    layout:'fit',
                    border: false,
                    tabTip : WtfGlobal.getLocaleText("acc.consolidation.consolidationbalancesheettt"),
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    title:WtfGlobal.getLocaleText("acc.consolidation.consolidationbalancesheet"),
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

Wtf.account.consolidationBalanceSheetReportTab = function (config){
    Wtf.apply(this, config);
    this.expandercss="x-grid3-row-expanderacc";
    this.expandedcss="x-grid3-row-expandedacc";
    this.collapsedcss="x-grid3-row-collapsedacc";
    Wtf.account.consolidationBalanceSheetReportTab.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.consolidationBalanceSheetReportTab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.consolidationBalanceSheetReportTab.superclass.onRender.call(this, config);
        this.add({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid,
            tbar:this.buttonArray
        });
    },
    initComponent:function(config){
        Wtf.account.consolidationBalanceSheetReportTab.superclass.initComponent.call(this, config);
        //Create Button
         this.createButton();
         
        //below method create 
          this.createStoreAndGrid();
    },

    createStoreAndGrid:function(){
        var recordsArray = [];
        var columnArray =[];
        recordsArray.push('accountid', 'acccode','accountname','level','fmt','leaf','accountflag');
        columnArray.push( 
        {
            header :'',
            hidden : true,
            dataIndex: 'accountid',
            width:200,
            pdfwidth:150
        },{
            header :"<b>"+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+"</b>",
            dataIndex: 'accountname',
            width:200,
            pdfwidth:150,
            renderer:this.formatAccountName 
        });
        for(var index=0;index<this.subdomainArray.length ;index++){
            var subdomainRecord=this.subdomainArray[index];
            if(subdomainRecord!="" && subdomainRecord!=undefined){
                var subdomainname = subdomainRecord.subdomain;
                var companyname = subdomainRecord.companyname;
                recordsArray.push(subdomainname+'_openingamount', subdomainname+'_periodamount',subdomainname+'_endingamount');
                columnArray.push({
                    header :"<b>"+companyname+" "+WtfGlobal.getLocaleText("acc.field.OpeningAmount")+" ("+WtfGlobal.getCurrencyName()+")</b>",
                    dataIndex: subdomainname+'_openingamount',
                    width:200,
                    align:'right',
                    pdfwidth:150,
                    renderer:WtfGlobal.currencyRenderer
                },{
                    header :"<b>"+companyname+" "+WtfGlobal.getLocaleText("acc.field.PeriodAmount")+" ("+WtfGlobal.getCurrencyName()+")</b>",
                    dataIndex: subdomainname+"_periodamount",
                    width:200,
                    align:'right',
                    pdfwidth:150,
                    renderer:WtfGlobal.currencyRenderer 
                },{
                    header :"<b>"+companyname+" "+WtfGlobal.getLocaleText("acc.field.EndingAmount")+" ("+WtfGlobal.getCurrencyName()+")</b>",
                    dataIndex: subdomainname+"_endingamount",
                    width:200,
                    align:'right',
                    pdfwidth:150,
                    renderer:WtfGlobal.currencyRenderer
                });
            }
        }
        recordsArray.push('totalopeningamount', 'totalperiodamount','totalendingamount');
        columnArray.push({
            header :"<b>"+WtfGlobal.getLocaleText("acc.conslodation.totalopeningamount")+" ("+WtfGlobal.getCurrencyName()+")</b>",
            dataIndex: 'totalopeningamount',
            width:200,
            align:'right',
            pdfwidth:150,
            renderer:WtfGlobal.currencyRenderer
        },{
            header :"<b>"+WtfGlobal.getLocaleText("acc.conslodation.totalperiodamount")+" ("+WtfGlobal.getCurrencyName()+")</b>",
            dataIndex: "totalperiodamount",
            width:200,
            align:'right',
            pdfwidth:150,
            renderer:WtfGlobal.currencyRenderer 
        },{
            header :"<b>"+WtfGlobal.getLocaleText("acc.conslodation.totalendingamount")+" ("+WtfGlobal.getCurrencyName()+")</b>",
            dataIndex: "totalendingamount",
            width:200,
            align:'right',
            pdfwidth:150,
            renderer:WtfGlobal.currencyRenderer
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
            url: "ACCReports/getConsolidationBalanceSheetReport.do",
            baseParams:{
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                periodView: true
            }
        }); 
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
//        this.store.load();
//        WtfComMsgBox(29,4,true)
        this.store.on('load',function(store,rec,option){
            Wtf.MessageBox.hide();
            WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
            this.expandCollapseGrid("Collapse");
            if(rec.length==0){
               this.grid.getView().refresh(true); 
            }
        },this);
        this.store.on('loadexception',function(store,rec,option){
            Wtf.MessageBox.hide();
            WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
        },this);
        this.store.on('beforeload',this.onBeforeStoreLoad,this);
    },
    onBeforeStoreLoad:function(store,obj){
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
        var startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
        
        store.baseParams.startdate= startdate;
        store.baseParams.stdate= startdate;
        store.baseParams.enddate= enddate;
        store.baseParams.periodView= true;// putting to get default BS
        
        if(this.expButton){
            this.expButton.setParams({
                stdate:startdate,
                startdate:startdate,
                enddate:enddate
            });
        }
//        if(this.printButton){
//            this.printButton.setParams({
//                startdate:startdate,
//                sttdate:startdate,
//                enddate:enddate
//            });
//        }
    },
    
    createGrid:function(columnArray){
        this.grid = new Wtf.grid.HirarchicalGridPanel({
            autoScroll:true,
            id:'consolidationBalanceSheetgrid'+this.id,
            store: this.store,
            columns: columnArray,
            border : false,
            loadMask : true,
            hirarchyColNumber:0,
            plugins:[new Wtf.grid.GridSummary()],
            viewConfig: {
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))
            }
        });
        this.grid.on('render',function(){
            this.grid.getView().applyEmptyText();
            WtfGlobal.autoApplyHeaderQtip(this.grid)
            this.grid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.grid],1);
        },this);
    },
    createButton:function(){
        this.buttonArray = new Array();
        
        this.expandCollpseButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.Expand"),
            tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
            iconCls:'pwnd toggleButtonIcon',
            scope:this,
            handler: function(){
                this.expandCollapseGrid(this.expandCollpseButton.getText());
            }
        });
        
        this.expButton=new Wtf.exportButton({
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
            id:"exportConsolidationBSReport"+this.id,
            filename: WtfGlobal.getLocaleText("acc.consolidation.consolidationbalancesheet")+ "_v1",
            menuItem:{
                csv:true,
                pdf:true,
                rowPdf:false,
                xls:true
            },
            params:{
                startdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
                periodView: true
            },
            get:Wtf.autoNum.consolidatioBalanceSheetReport,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
            isConsolidatedReportExport:true,
            usePostMethod:true
        });
    
//        this.printButton=new Wtf.exportButton({
//            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
//            obj:this,
//            id:"printConsolidationBSReport"+this.id,
//            filename: WtfGlobal.getLocaleText("acc.consolidation.consolidationbalancesheet"),
//            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
//            menuItem:{
//                print:true
//            },
//            params:{
//                startdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
//                stdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
//                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false)),
//                periodView: true
//            },
//            get:Wtf.autoNum.consolidatioBalanceSheetReport,
//            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
//        });
        this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
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
        this.buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,this.fetchBttn,this.expandCollpseButton,this.expButton);
    },
    expandCollapseGrid : function(btntext){
        if(btntext == WtfGlobal.getLocaleText("acc.field.Collapse")){
            for(var i=0; i< this.grid.getStore().data.length; i++){
                this.grid.collapseRow(this.grid.getView().getRow(i));

            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            for(var i=0; i< this.grid.getStore().data.length; i++){
                this.grid.expandRow(this.grid.getView().getRow(i));

            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
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
    fetchData:function(){
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
        this.store.load({
            params: {
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                stdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
            }
        });
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
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        } else if(rec.data['leaf']==true){
            fmtVal="<div style='margin-left:"+(rec.data['level']*20)+"px;padding-left:20px'>"+fmtVal+"</div>";
        } else {
            fmtVal= "<div class='x-grid3-row-expanderacc' style='margin-left:"
            +(rec.data['level']*20)+"px;width:20px'><div style='margin-left:20px'><b>"+fmtVal+"</b></div></div>";
        }
        return fmtVal; 
    },
    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    }
});