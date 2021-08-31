/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
function callCompareAgedAndBSReport(){
    var panel = Wtf.getCmp("callCompareAgedAndBSReportTab");
    if(!panel){// If panel is already not open
        panel= new Wtf.account.compareAgedAndBSReportTab({
            id:'callCompareAgedAndBSReportTab',
            layout:'fit',
            border: false,
            tabTip :'Compare Aging and Balance Sheet',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            title:'Compare Aging and Balance Sheet',
            closable:true
        })
        Wtf.getCmp('as').add(panel);
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    } else{
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
    }
}

Wtf.account.compareAgedAndBSReportTab = function (config){
    Wtf.apply(this, config);
    Wtf.account.compareAgedAndBSReportTab.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.compareAgedAndBSReportTab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.compareAgedAndBSReportTab.superclass.onRender.call(this, config);
        this.add({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid,
            tbar:this.buttonArray
        });
        
        this.store.on('load',function(store){
            WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
        },this);
        this.store.on('loadexception',function(store,rec,option){
            Wtf.MessageBox.hide();
            WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
        },this);
        this.store.on('beforeload',this.onBeforeStoreLoad,this);
    },
    initComponent:function(config){
        Wtf.account.compareAgedAndBSReportTab.superclass.initComponent.call(this, config);
        //create combo stores
        this.createComboStore();
        
        //Create Button
        this.createButton();
         
        //below method create 
        this.createStoreAndGrid();
        
    },

    createComboStore:function(){
        this.reportFilterStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data :[[0,'Aged Receivable'],[1,'Aged Payable']]
        });
    },
    createStoreAndGrid:function(){
        var recordsArray = new Wtf.data.Record.create([{
            name :'asondate',
            type :'date'
        },{
            name :'agingamount'
        },{
            name :'balancesheetamount'
        },{
            name :'amountdifference'
        }]);
    
        var columnArray =[];
        this.rowNo=new Wtf.grid.RowNumberer()
        columnArray.push(this.rowNo, 
        {
            header :'As On Date',
            dataIndex: 'asondate',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header :'Aged Amount',
            dataIndex: 'agingamount',
            width:200,
            pdfwidth:150
        },{
            header :'Balance Sheet Amount',
            dataIndex: 'balancesheetamount',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer 
        },{
            header :"Amount Difference",
            dataIndex: 'amountdifference',
            width:200,
            pdfwidth:150
        });

        this.createStore(recordsArray); 
        this.createGrid(columnArray); 
    },
    createStore:function(recordsArray){
        this.store=new Wtf.data.Store({
            reader:new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"  
            },recordsArray),
            url: "ACCReports/compareAgedAndbalanceSheetReport.do",
            baseParams:{
                enddate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                startdate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
    },
    onBeforeStoreLoad:function(store,obj){
        WtfGlobal.setAjaxTimeOutFor30Minutes(); 
        Wtf.Ajax.timeout = 18000000;//300 minutes
        if(!obj.params){
            obj.params={};
        }
        var baseParams = this.store.baseParams;
        var startDate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
        baseParams.enddate= enddate;
        baseParams.startdate= startDate;
        if(this.productCategory){
            baseParams.categoryid = this.productCategory.getValue(); 
        }
        this.store.baseParams=baseParams;
    },
    createGrid:function(columnArray){
        this.gridcm= new Wtf.grid.ColumnModel(columnArray);
        this.grid = new Wtf.grid.GridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:'consolidationstockreportgrid',
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            stripeRows : true,
            viewConfig: {
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
        },this);
    },
    createButton:function(){
        this.buttonArray = new Array();
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'startdate',
            id: 'stdate'+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"), //'To',  
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)
        });
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.conslodation.fetchdataonfilter"), //'Fetch',
            id: 'fetchButton' + this.id,
            scope: this,
            iconCls:'accountingbase fetch',
            handler: this.fetchData.createDelegate(this)
        });
        
        this.agedReportType = new Wtf.form.ComboBox({
            fieldLabel: 'Aged Report',
            store: this.reportFilterStore,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            triggerAction: 'all',
            typeAhead:true,
            width:200,
            selectOnFocus:true,
            value:1
        });
        this.interval=new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.till"),  //'Till',
            maxLength:2,
            width:30,
            allowDecimal:false,
            allowBlank:true,
            minValue:1,
            name:'duration',
            value:30
        });
        this.buttonArray.push('Report',this.agedReportType,'From',this.startDate,'To',this.endDate,'Interval Of days',this.interval,this.fetchBttn);
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
        WtfGlobal.setAjaxTimeOutFor30Minutes();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
        this.store.load({
            params: {
                //variable section required for Balance Sheet Data
                startDate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                //variable section required for Aged Report
                asofdate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                creditonly :true,
                curdate	:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                datefilter : 1,
                duration : 30,
                ignorezero : true,
                isAged : true,
                isParentChild :false,
                isdistributive:true,
                limit :30,
                moduleIDForFetchingGroupi:0,
                nondeleted:true,
                //variable section of this Report
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                agedreporttype : this.agedReportType.getValue(),
                interval : this.interval.getValue()
            }
        });
    }
});
