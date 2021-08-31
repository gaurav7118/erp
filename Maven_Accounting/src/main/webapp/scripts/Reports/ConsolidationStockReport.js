/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function callConsolidationStockReportTab(){
    var panel = Wtf.getCmp("callConsolidationStockReportTab");
    if(!panel){// If panel is already not open
        Wtf.Ajax.requestEx({
            url:"ACCReports/getMappedCompanies.do",
            params:{
                includeParentCompany:true
            }
        },this,function(response){
            if(response.success){  
                panel= new Wtf.account.consolidationStockReportTab({
                    id:'callConsolidationStockReportTab',
                    layout:'fit',
                    border: false,
                    tabTip :WtfGlobal.getLocaleText("acc.consolidation.consolidationstockreporttt"),
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.consolidation.consolidationstockreport"),Wtf.TAB_TITLE_LENGTH),
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

Wtf.account.consolidationStockReportTab = function (config){
    Wtf.apply(this, config);
    Wtf.account.consolidationStockReportTab.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.consolidationStockReportTab,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.consolidationStockReportTab.superclass.onRender.call(this, config);
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
        Wtf.account.consolidationStockReportTab.superclass.initComponent.call(this, config);
        //create combo stores
        this.createComboStore();
        
        //Create Button
         this.createButton();
         
        //below method create 
          this.createStoreAndGrid();
    },

    createComboStore:function(){
        this.productCategoryRecord = Wtf.data.Record.create ([
                {name:'id'},
                {name:'name'}
        ]);
        this.productCategoryStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productCategoryRecord)
        });
    },
    createStoreAndGrid:function(){
        var recordsArray = [];
        var columnArray =[];
        recordsArray.push('productid', 'productcode','productname','producttypename','uomname');
        this.rowNo=new Wtf.grid.RowNumberer()
        columnArray.push(this.rowNo, 
        {
            header :'',
            hidden : true,
            dataIndex: 'productid',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer
        },{
            header :"<b>"+WtfGlobal.getLocaleText("acc.field.ProductCode")+"</b>",
            dataIndex: 'productcode',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer  
        },{
            header :"<b>"+WtfGlobal.getLocaleText("acc.rem.prodName")+"</b>",
            dataIndex: 'productname',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer 
        },{
            header :"<b>"+WtfGlobal.getLocaleText("acc.productList.gridProductType")+"</b>",
            dataIndex: 'producttypename',
            width:200,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer 
        });
        for(var index=0;index<this.subdomainArray.length ;index++){
            var subdomainRecord=this.subdomainArray[index];
            if(subdomainRecord!="" && subdomainRecord!=undefined){
                var subdomainname = subdomainRecord.subdomain;
                var companyname = subdomainRecord.companyname;
                recordsArray.push(subdomainname+'_quantityonhand', subdomainname+'_valuation');
                columnArray.push({
                    header :"<b>["+companyname +"] "+WtfGlobal.getLocaleText("acc.invReport.qty")+"</b>",
                    dataIndex: subdomainname+'_quantityonhand',
                    width:200,
                    align:'right',
                    pdfwidth:150,
                    renderer:this.unitRenderer
                },{
                    header :"<b>["+companyname+"] "+WtfGlobal.getLocaleText("acc.invReport.evaluationprice")+"</b>",
                    dataIndex: subdomainname+"_valuation",
                    width:200,
                    align:'right',
                    pdfwidth:150,
                    renderer:WtfGlobal.withoutRateCurrencySymbol 
                });
            }
        }
        recordsArray.push('totalquantityonhand', 'totalvaluation');
        columnArray.push({
            header :"<b>"+WtfGlobal.getLocaleText("acc.consolidation.stock.totalQuantityonhand")+"</b>",
            dataIndex: 'totalquantityonhand',
            width:200,
            align:'right',
            pdfwidth:150,
            renderer:this.unitRenderer
        },{
            header :"<b>"+WtfGlobal.getLocaleText("acc.consolidation.stock.totalEvaluationAmount")+" ("+WtfGlobal.getCurrencyName()+")"+"</b>",
            dataIndex: "totalvaluation",
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
            url: "ACCProductCMN/getConsolidationStockReport.do",
            sortInfo :{
                field :"productcode",
                direction :"ASC"
            },            
            baseParams:{
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
//        this.store.load();
        
        this.store.on('load',function(store){
            WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
            this.quickPanelSearch.StorageChanged(store);
        },this);
        this.store.on('loadexception',function(store,rec,option){
            Wtf.MessageBox.hide();
            WtfGlobal.resetAjaxTimeOut(); // Function which set time out for 30000 milliseconds i.e. 30 seconds
        },this);
        this.store.on('beforeload',this.onBeforeStoreLoad,this);
    },
    onBeforeStoreLoad:function(store,obj){
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
        if(!obj.params){
            obj.params={};
        }
        var baseParams = this.store.baseParams;
        var enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
        baseParams.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        if(this.productCategory){
            baseParams.categoryid = this.productCategory.getValue(); 
            baseParams.categoryname = this.productCategory.getRawValue(); 
        }
        this.store.baseParams=baseParams;
        if(this.expButton){
            this.expButton.setParams({
                enddate:enddate
            });
        }
//        if(this.printButton){
//            this.printButton.setParams({
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
            id:'consolidationstockreportgrid',
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            stripeRows : true,
            viewConfig: {
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))
            }
        });
        this.grid.on("render", function(grid) {
            this.grid.getView().applyEmptyText();
            WtfGlobal.autoApplyHeaderQtip(grid);
        },this);
    },
    createButton:function(){
        this.buttonArray = new Array();
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.productList.searchText"), // Search by Product Name,Product ID
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
            filename: WtfGlobal.getLocaleText("acc.consolidation.consolidationstockreport")+ "_v1",
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
            get:Wtf.autoNum.consolidationStockReport,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
            isConsolidatedReportExport:true
        });
    
//        this.printButton=new Wtf.exportButton({
//            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
//            obj:this,
//            id:"printConsolidationReport"+this.id,
//            filename: WtfGlobal.getLocaleText("acc.consolidation.consolidationstockreport"),
//            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
//            menuItem:{
//                print:true
//            },
//            params:{
//                startdate:WtfGlobal.convertToGenericStartDate(this.getDates(true)),
//                enddate:WtfGlobal.convertToGenericEndDate(this.getDates(false))
//            },
//            get:Wtf.autoNum.consolidationStockReport,
//            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
//        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.25"),  //As On,
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)
        });
        this.productCategory = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.Productcategory"),
            store: this.productCategoryStore,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            triggerAction: 'all',
            typeAhead:true,
            width:200,
            selectOnFocus:true
        });
        this.productCategoryStore.load();
        this.productCategoryStore.on('load',this.setValue,this);
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.conslodation.fetchdataonfilter"), //'Fetch',
            id: 'fetchButton' + this.id,
            scope: this,
            iconCls:'accountingbase fetch',
            handler: this.fetchData.createDelegate(this)
        });
        
        this.helpText = " <p>"+WtfGlobal.getLocaleText("acc.consolidation.stock.helpmsg1")+"</p>"
                        +"<p>"+WtfGlobal.getLocaleText("acc.consolidation.stock.helpmsg2")+"</p>"
                        +"<ul>"
                        +"<li><b>- "+WtfGlobal.getLocaleText("acc.cust.Productcategory")+" : </b>"+WtfGlobal.getLocaleText("acc.consolidation.stock.helpmsg3")+"</li>"
                        +"<li><b>- "+WtfGlobal.getLocaleText("acc.consolidation.stock.asondate")+" : </b>"+WtfGlobal.getLocaleText("acc.consolidation.stock.helpmsg4")+"</li>"
                        +"<li><b>- "+WtfGlobal.getLocaleText("acc.field.Note")+" : </b>"+WtfGlobal.getLocaleText("acc.consolidation.stock.helpmsg5")+"</li>"
                        +"</ul>";
        this.helpButton = new Wtf.Toolbar.Button({
            scope: this,
            iconCls: 'helpButton',
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.rem.2")
                }, //{text:'Get started by clicking here!'},
            mode: id,
            handler: function(e, target, panel) {
                var tmp = e.getEl().getXY();
                var we = new Wtf.consolidationStockInHand();
                we.showHelpWindow(tmp[0], tmp[1], WtfGlobal.getLocaleText("acc.consolidation.consolidationstockreport"), this.helpText);
            }
        });
    
        this.buttonArray.push(this.quickPanelSearch,this.resetBttn,'-',WtfGlobal.getLocaleText("acc.cust.Productcategory"),this.productCategory,'-',WtfGlobal.getLocaleText("acc.rem.25"),this.endDate,this.fetchBttn,this.expButton,'->',this.helpButton);
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
//        this.startDate.reset();
        this.endDate.reset();
        this.productCategory.setValue("All");
        this.fetchData();   
    },
    fetchData:function(){
        WtfGlobal.setAjaxTimeOut();    // Function which set time out for 900000 milliseconds i.e. 15 minutes
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
//                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                ss: this.quickPanelSearch.getValue()
            }
        });
    },
    unitRenderer:function(value,metadata,record){
        if(value!="" && value!=undefined){
            if(record.data['type'] == "Service"){
            return "N/A";
        }
        var unit=record.data['uomname'];
        value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;
        if(record.data.deleted)
            value='<del>'+value+'</del>';    
        return value;
        } else {
            return "";
        }
    },
    setValue:function(store){
        var record = new Wtf.data.Record({
            name:'All',
            id:'All'
        });
        var index=this.productCategoryStore.find('name','All');
        if(index==-1){
            this.productCategoryStore.insert(0,record);    
            this.productCategory.setValue("All");
        }        
    }
});

Wtf.consolidationStockInHand = Wtf.extend(Wtf.Component, {
    tplMarkup: ['<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">' +
                '<div class="fcue-inner">' +
                '<div class="fcue-t"></div>' +
                '<div class="fcue-content">' +
                '<a onclick="closeCue();" href="#" id="fcue-close"></a>' +
                '<div class="ft ftnux"><p>' +
                '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span>' +
                '<p></p>' +
                '<span id="titledesc">sssdd</span>' +
                '<div id="helpBttnContainerDiv"><p></p>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '<div class="fcue-b">' +
                '<div></div>' +
                '</div>' +
                '<div class="fcue-pnt fcue-pnt-t-r">' +
                '</div>' +
                '</div>'],
    id: 'widgethelpdialog',
    initComponent: function(config) {
        Wtf.consolidationStockInHand.superclass.initComponent.call(this, config);
    },
    showHelpWindow: function(x, y, title, desc) {
        if (document.getElementById('fcue-360-mask'))
            document.getElementById('fcue-360-mask').style.display = "block";
        this.tpl = new Wtf.Template(this.tplMarkup[0]);
        this.tpl.append(document.body, {});
        document.getElementById('titlehelp').innerHTML = title;
        document.getElementById('titledesc').innerHTML = desc;
        Wtf.get('fcue-360').setXY([x - 330, y + 30]);
        document.getElementById('fcue-360').style.visibility = "visible";
    }
});