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

function calltopProductsByCustomersDynamicLoad(){
    var panel = Wtf.getCmp("topProductsByCustomersList");
    if(panel==null){
        panel = new Wtf.account.TopAndDormantUsers({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.TopandDormantProductsByCustomers"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.field.ViewTopproductsandDormantproductsByCustomers"),
            id:'topProductsByCustomersList',
            isCustomer:true,
            isProduct:true,
            border:false,
            closable:true,
            iconCls:'accountingbase agedpayable'
        });
        Wtf.getCmp('topProductsByCustomers').add(panel);
    }
    Wtf.getCmp('topProductsByCustomers').setActiveTab(panel);
    panel.doLayout();
}


function calltopCustomersByProductsDynamicLoad() {
    var panel = Wtf.getCmp("topCustomersByProductsList");
    if (panel == null) {
        panel = new Wtf.account.TopAndDormantUsers({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.TopandDormantCustomersByProducts"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.field.ViewTopcustomersandDormantcustomersByProducts"),
            id:'topCustomersByProductsList',
            isCustomer:true,
            isProduct:false,
            border:false,
            //            layout: 'fit',
            iconCls:'accountingbase agedpayable'
        });
        Wtf.getCmp('topCustomersByProducts').add(panel);
    }
    Wtf.getCmp('topCustomersByProducts').setActiveTab(panel);
    Wtf.getCmp('topCustomersByProducts').doLayout();
}


function calltopVendorsByProductsDynamicLoad() {
    var panel = Wtf.getCmp("topVendorsByProductsList");
    if(panel==null){
        panel = new Wtf.account.TopAndDormantUsers({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.TopandDormantVendorsByProducts"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.field.ViewTopvendorsandDormantvendorsByProducts"),
            id:'topVendorsByProductsList',
            isCustomer:false,
            isProduct:false,
            border:false,
            closable:true,
            iconCls:'accountingbase agedpayable'
        });
        Wtf.getCmp('topVendorsByProducts').add(panel);
    }
    Wtf.getCmp('topVendorsByProducts').setActiveTab(panel);
    Wtf.getCmp('topVendorsByProducts').doLayout();
}

//***************************************************************************************
Wtf.account.TopAndDormantUsers=function(config){
    this.isCustomer=config.isCustomer;
    this.isProduct=config.isProduct,
    this.id = config.id!==undefined ? config.id : Wtf.id();
    this.graphrandomno = Math.random();
    this.topUsersRec = new Wtf.data.Record.create([{           
            name:(this.isProduct)?'productname':'custname'
        },{           
            name:(this.isProduct)?'productid':'custid'
        },{
            name:'isCustomer',type:'boolean'
        },{
            name:'quantity'
        }]);
    this.topUsersStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.topUsersRec),
//        url: Wtf.req.account+"reporthandler.jsp",
        url : (this.isProduct)?"ACCReports/getProductsByUsers.do":"ACCReports/getUsersByProducts.do",
        baseParams:{
            mode:1,            
            isTopCustomers:true,
            isCustomer:this.isCustomer,
            isProduct:this.isProduct
        }
    });
    this.typestore = new Wtf.data.SimpleStore({
           fields:['id','type'],
           data: [
           ['0','Quantity Sold'],
           ['1','Revenue']
           ]
       });
    this.typeCombo=new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.addTpe.header1"), //WtfGlobal.getLocaleText("hrms.common.Gender")+"*",
        hiddenName:'type',
        store:this.typestore,
        displayField:'type',
        valueField:'id',
        forceSelection: true,
        selectOnFocus:true,
        triggerAction: 'all',
        typeAhead:true,
        mode: 'local',
        value : '0',
        width:220,
        allowBlank:false
    });
    this.typeCombo.on("select",function(combo, record, index){
        if(record.data.id=='0') {
            // Quantity Columns
            var colIndex =this.topUsersGrid.getColumnModel().getIndexById(this.id+"topqtycol");
            this.topUsersGrid.getColumnModel().setHidden(colIndex,false) ;//Quantity
            colIndex =this.dormantUsersGrid.getColumnModel().getIndexById(this.id+"dormantqtycol");
            this.dormantUsersGrid.getColumnModel().setHidden(colIndex,false) ;//Quantity
            // Amount Columns
            colIndex =this.topUsersGrid.getColumnModel().getIndexById(this.id+"topamountcol");
            this.topUsersGrid.getColumnModel().setHidden(colIndex,true) 
            colIndex =this.dormantUsersGrid.getColumnModel().getIndexById(this.id+"dormantamountcol");
            this.dormantUsersGrid.getColumnModel().setHidden(colIndex,true) ;
        } else {
            // Quantity Columns
            var colIndex =this.topUsersGrid.getColumnModel().getIndexById(this.id+"topqtycol");
            this.topUsersGrid.getColumnModel().setHidden(colIndex,true) ;//Quantity
            colIndex =this.dormantUsersGrid.getColumnModel().getIndexById(this.id+"dormantqtycol");
            this.dormantUsersGrid.getColumnModel().setHidden(colIndex,true) ;//Quantity
            // Amount Columns
            colIndex =this.topUsersGrid.getColumnModel().getIndexById(this.id+"topamountcol");
            this.topUsersGrid.getColumnModel().setHidden(colIndex,false) 
            colIndex =this.dormantUsersGrid.getColumnModel().getIndexById(this.id+"dormantamountcol");
            this.dormantUsersGrid.getColumnModel().setHidden(colIndex,false) ;
        }
        this.fetchAgedData();
//        this.topUsersStore.load();
//        this.dormantUsersStore.load();
    },this)
    this.rowNo=new Wtf.KWLRowNumberer();
//    this.topUsersStore.on('datachanged', function() {
//        var p = this.pP.combo.value;
////        this.quickPanelSearch.setPage(p);
//    }, this);
    
//  this.topUsersStore.on('load',this.storeloaded,this);

    var btnArr=[];
    
     this.fromDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'stdate',
       // readOnly:true, 
        value:WtfGlobal.getDates(true)
    });
    
    this.toDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
   //     readOnly:true,
        value:WtfGlobal.getDates(false)
    });
    
    this.topUsersStore.on('beforeload',function(s,o){
        if(!o.params)o.params={};
//        var currentBaseParams = this.Store.baseParams;
//        currentBaseParams.deleted=this.deleted;
//        currentBaseParams.nondeleted=this.nondeleted;
//        currentBaseParams.costCenterId = this.costCenter.getValue();
//        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
//        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
//        this.Store.baseParams=currentBaseParams;

        var currentBaseParams = this.topUsersStore.baseParams;
        currentBaseParams.startdate =  WtfGlobal.convertToGenericDate(this.fromDate.getValue());
        currentBaseParams.enddate =  WtfGlobal.convertToGenericDate(this.toDate.getValue());
        if(this.isProduct){
            currentBaseParams.accid=this.cmbAccount.getValue();
        }else{
            currentBaseParams.productid=this.productCombo.getValue();
        }                
        currentBaseParams.type = this.typeCombo.getValue();
        this.topUsersStore.baseParams=currentBaseParams;
    },this);
    
    this.sizeStore = new Wtf.data.SimpleStore({
            fields:[{name:'value'}],
            data:[[10],[20],[50],[100]]
        });
        
      this.sizeCombo= new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'value',
            store:this.sizeStore,            
            value:10,
            width:100,
            typeAhead: true,
            forceSelection: true,
            name:'size',
            hiddenName:'size'            
        });
        
    this.productRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'uomid'},
            {name:'uomname'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'purchaseaccountid'},
            {name:'salesaccountid'},
            {name:'purchaseretaccountid'},
            {name:'salesretaccountid'},
            {name:'reorderquantity'},
            {name:'quantity'},
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name:'leaf'},
            {name:'currencysymbol'},
            {name:'currencyrate'},
            {name:'level'}
        ]);
        this.productStore = new Wtf.data.Store({
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{mode:22},
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productRec)
        });
       
        this.productStore.on('beforeload',function(){
                WtfGlobal.setAjaxTimeOut();
        }, this);
        this.productStore.on('load',function(){
                WtfGlobal.resetAjaxTimeOut();
        },this);
    this.productStore.on('load',function(){
        this.productStore.insert(0,new Wtf.data.Record({
            productname:'All',
            productid:'All'
        }));
    },this);
        this.productStore.on('loadexception',function(){
                WtfGlobal.resetAjaxTimeOut();
        },this);
        if(!this.isProduct){
            this.productStore.load();
        }
        

    this.productCombo= new Wtf.form.ComboBox({            
            hidden:this.isProduct,
            hideLabel:this.isProduct,
            triggerAction:'all',
            mode: 'local',
            selectOnFocus:true,
            valueField:'productid',
            displayField:'productname',
            store:this.productStore,                                                 
            width:200,
            typeAhead: true,
            forceSelection: true,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectProduct"),
            allowBlank:false,        
            name:'productname',
            hiddenName:'productname'            
        });
        
     this.productStore.on("load", function(){
        if(this.productCombo.getValue()==""){
            if(this.productStore.data.length>0){
                this.productCombo.setValue(this.productStore.data.items[0].data.productid);
                 this.topUsersStore.load({
                    params:{
                        start:0,
                        productid:this.productCombo.getValue(),
                        limit:30
                    }
                });
                this.dormantUsersStore.load({
                    params:{
                        start:0,
                        productid:this.productCombo.getValue(),
                        limit:30                    
                    }
                });   
            }
        }                    
    }, this);

    this.custRec = Wtf.data.Record.create ([{
        name:'accountname',
        mapping:'accname'
    },{
        name:'accountid',
        mapping:'accid'
    },{
        name:'acccode'
    }]);

    this.custStore = new Wtf.data.Store({
        url : this.isCustomer? "ACCCustomer/getCustomersForCombo.do": "ACCVendor/getVendorsForCombo.do",
        baseParams:{
            nondeleted:true,
            combineData:this.isCustomer?1:-1  //Send For Seprate Request
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.custRec)
    });    
    
    if(this.isProduct){
       this.custStore.load(); 
    }        

     this.cmbAccount=new Wtf.form.ExtFnComboBox({    
        hidden:!this.isProduct,
        hideLabel:!this.isProduct,
        name:'accountid',
        store:this.custStore,
        valueField:'accountid',
        displayField:'accountname',
        minChars:1,
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
        typeAheadDelay:30000,
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        mode: 'local',
        width:200,
        hiddenName:'accountid',
        emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectCustomer"): WtfGlobal.getLocaleText("acc.field.SelectVendor"),
        allowBlank:false,
        forceSelection:true,
        triggerAction:'all'
    });
    
    this.custStore.on("load", function(){
        if(this.cmbAccount.getValue()==""){
            if(this.custStore.data.length>0){
                this.cmbAccount.setValue(this.custStore.data.items[0].data.accountid);
                  this.topUsersStore.load({
                    params:{
                        start:0,
                        accid:this.cmbAccount.getValue(),
                        limit:30
                    }
                });
                this.dormantUsersStore.load({
                    params:{
                        start:0,
                        accid:this.cmbAccount.getValue(),
                        limit:30
                    }
                }); 
            }
        }                    
    }, this);
               
    btnArr.push(
      
    );
       
//    btnArr.push(this.exportbtn = new Wtf.exportButton({
//        obj:this,
//        disabled:true,
//        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
//        id:"exportSalebyItemSummary",
//        menuItem:{csv:true,pdf:true,rowPdf:false},
//        get:913,
//        label:"Sale by Item Summary Report"
//    }));
//    
//    btnArr.push(this.printButton=new Wtf.exportButton({
//        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
//        obj:this,
//        disabled:true,
//        id:"printSalebyItemSummary",
//        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
//        menuItem:{print:true},
//        get:913,
//        label:"Sale by Item Summary Report"
//    }));

    this.topUsersGrid = new Wtf.grid.GridPanel({        
        title:(this.isProduct)?WtfGlobal.getLocaleText("acc.field.TopProducts"):(this.isCustomer)?WtfGlobal.getLocaleText("acc.field.TopCustomers"):WtfGlobal.getLocaleText("acc.field.TopVendors"),
        stripeRows :true,
        store:this.topUsersStore,
        border:false,        
        layout:'fit',
        viewConfig:{forceFit:true, emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))},
//        viewConfig:{
//            forceFit:true,
//            enableRowBody: true/*,
//            getRowClass: this.changeRowColor.createDelegate(this)*/
//        },
        forceFit:true,
        loadMask : true,
        columns:[this.rowNo,{
            header:(this.isProduct)?WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"):(this.isCustomer)?WtfGlobal.getLocaleText("acc.saleByItem.gridCustName"):WtfGlobal.getLocaleText("acc.1099.gridVname"),  //' Customer Name',
            dataIndex:(this.isProduct)?'productname':'custname',
            renderer:(this.isProduct)?"":this.linkRenderer,
            width:500,
            pdfwidth:500
        },{
            header:(this.isCustomer)?WtfGlobal.getLocaleText("acc.saleByItem.gridQtySold"):WtfGlobal.getLocaleText("acc.field.QuantityPurchased"),  //"Quantity Sold",
            dataIndex:'quantity',
            align:'right',  
            id:this.id+"topqtycol",
            width:150,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
            dataIndex:'quantity',
            align:'right',  
            hidden : true,
            id:this.id+"topamountcol",
            width:150,
            pdfwidth:150
        }
    ],
         bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.topUsersStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
        })
    });
    
    this.dormantUsersRec = new Wtf.data.Record.create([{           
            name:(this.isProduct)?'productname':'custname'
        },{           
            name:(this.isProduct)?'productid':'custid'
        },{
            name:'isCustomer',type:'boolean'
        },{
            name:'quantity'
        }]);
    
    this.dormantUsersStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.dormantUsersRec),
//        url: Wtf.req.account+"reporthandler.jsp",
        url :(this.isProduct)?"ACCReports/getProductsByUsers.do":"ACCReports/getUsersByProducts.do",
        baseParams:{
            mode:1,            
            isTopCustomers:false,
            isCustomer:this.isCustomer,
            isProduct:this.isProduct
        }
    });
    
    this.dormantUsersStore.on('beforeload',function(s,o){
        if(!o.params)o.params={};
//        var currentBaseParams = this.Store.baseParams;
//        currentBaseParams.deleted=this.deleted;
//        currentBaseParams.nondeleted=this.nondeleted;
//        currentBaseParams.costCenterId = this.costCenter.getValue();
//        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
//        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
//        this.Store.baseParams=currentBaseParams;

        var currentBaseParams = this.dormantUsersStore.baseParams;
        currentBaseParams.startdate =  WtfGlobal.convertToGenericDate(this.fromDate.getValue());
        currentBaseParams.enddate =  WtfGlobal.convertToGenericDate(this.toDate.getValue());
        if(this.isProduct){
            currentBaseParams.accid=this.cmbAccount.getValue();
        }else{
            currentBaseParams.productid=this.productCombo.getValue();
        }
        currentBaseParams.type = this.typeCombo.getValue();
        this.dormantUsersStore.baseParams=currentBaseParams;
    },this);
    
     this.dormantUsersGrid = new Wtf.grid.GridPanel({        
        title:(this.isProduct)?WtfGlobal.getLocaleText("acc.field.DormantProducts"):(this.isCustomer)?WtfGlobal.getLocaleText("acc.field.DormantCustomers"):WtfGlobal.getLocaleText("acc.field.DormantVendors"),
        stripeRows :true,
        store:this.dormantUsersStore,
        border:false,        
        //tbar:btnArr,
        layout:'fit',
//        viewConfig:{
//            forceFit:true,
//            enableRowBody: true/*,
//            getRowClass: this.changeRowColor.createDelegate(this)*/
//        },
        forceFit:true,
        loadMask : true,
        columns:[this.rowNo,{
            header:(this.isProduct)?WtfGlobal.getLocaleText("acc.saleByItem.gridProduct"):(this.isCustomer)?WtfGlobal.getLocaleText("acc.saleByItem.gridCustName"):WtfGlobal.getLocaleText("acc.1099.gridVname"),  //' Customer Name',
            dataIndex:(this.isProduct)?'productname':'custname',
            renderer:(this.isProduct)?"":this.linkRenderer,
            width:500,
            pdfwidth:500
        },{
            header:(this.isCustomer)?WtfGlobal.getLocaleText("acc.saleByItem.gridQtySold"):WtfGlobal.getLocaleText("acc.field.QuantityPurchased"),  //"Quantity Sold",
            dataIndex:'quantity',
            align:'right',            
            id:this.id+"dormantqtycol",
            width:150,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.dnList.gridAmt"),
            dataIndex:'quantity',
            align:'right',  
            hidden : true,
            id:this.id+"dormantamountcol",
            width:150,
            pdfwidth:150
        }
        ],
         bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.dormantUsersStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
        })
    });
    
    Wtf.apply(this,{
        border:false,
        layout : "border",       
        items:[this.innerPanel = new Wtf.Panel({
           region : 'north',
           xtype : "Panel",
           layout : "fit",           
           height :25,
           paging : false,
           border : false,
           tbar:[WtfGlobal.getLocaleText("acc.product.gridType"),this.typeCombo,(this.isProduct)?WtfGlobal.getLocaleText("acc.field.SelectCustomer"): WtfGlobal.getLocaleText("acc.field.SelectProduct") ,this.cmbAccount,this.productCombo,'-',WtfGlobal.getLocaleText("acc.common.from"),this.fromDate,WtfGlobal.getLocaleText("acc.common.to"),this.toDate,'-',WtfGlobal.getLocaleText("acc.field.Size"),this.sizeCombo,'-',
        {
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:((this.isProduct)?WtfGlobal.getLocaleText("acc.field.Selectcustomerfromcustomercombo"):WtfGlobal.getLocaleText("acc.field.Selectproductfrompoductcombo"))+" "+ WtfGlobal.getLocaleText("acc.field.and")+" "+ WtfGlobal.getLocaleText("acc.common.filter.tlp"),
            handler:this.fetchAgedData
        },this.chartsBtn = new Wtf.Button({
                text : WtfGlobal.getLocaleText("acc.field.ShowCharts"),iconCls:'accountingbase graphTabIcon',  
                scope : this,
                handler : this.handleChart,
                disabled : false,
                tooltip: WtfGlobal.getLocaleText("acc.field.ShowCharts")
        })]           
       }),{
           region:'center',                                  
           layout:'fit',
           height:400,
           border:false,
           items:[this.topUsersGrid]
        },{
           region:'south',
           height:400,                       
           layout:'fit',
           border:false,
           items:[this.dormantUsersGrid]
        }]
    
    });

    Wtf.account.TopAndDormantUsers.superclass.constructor.call(this,config);
    
  //this.topUsersStore.on("beforeload", function(s,o) {
        //o.params.fromDate= WtfGlobal.convertToGenericDate(this.fromDate.getValue());
        //o.params.toDate= WtfGlobal.convertToGenericDate(this.toDate.getValue());
     //   o.params.product=this.productEditor.getValue();
  
      //  this.fetchAgedData1(false);
  //},this);
}

Wtf.extend( Wtf.account.TopAndDormantUsers,Wtf.Panel,{
//    decimalRenderer:function(val){
//        return'<div class="currency">'+WtfGlobal.conventInDecimal(val,"")+'%</div>';
//    },
//    handleResetClick:function(){
//        if(this.quickPanelSearch.getValue()){
//            this.quickPanelSearch.reset();
//            this.topUsersStore.load({
//                params: {
//                    start:0,
//                    limit:this.pP.combo.value,
//                    aged:true,
//                    creditonly:true
//                }
//            });
//        }
//    },

//    changeRowColor:function(record){
//        var dueDate = record.data['duedate'];
//        var currentDate=new Date(new Date().format('M d, Y'))
//        if(currentDate>dueDate)
//            return 'red-background';
//        return 'yellow-background';
//    },

    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
        if(store.getCount()==0){
            if(this.exportbtn)this.exportbtn.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportbtn)this.exportbtn.enable();
            if(this.printButton)this.printButton.enable();
        }
    },

    fetchAgedData:function(){       
        if(this.isProduct){
            if(this.cmbAccount.getValue()!=undefined && this.cmbAccount.getValue()!=""){
                this.topUsersStore.load({
                    params:{
                        start:0,
                        accid:this.cmbAccount.getValue(),
                        limit:this.sizeCombo.getValue()                
                    }
                });
                this.dormantUsersStore.load({
                    params:{
                        start:0,
                        accid:this.cmbAccount.getValue(),
                        limit:this.sizeCombo.getValue()                
                    }
                });   
            // this.fetchAgedData1(true);   
            }            
        }else{
            if(this.productCombo.getValue()!=undefined && this.productCombo.getValue()!=""){
                this.topUsersStore.load({
                    params:{
                        start:0,
                        productid:this.productCombo.getValue(),
                        limit:this.sizeCombo.getValue()                
                    }
                });
                this.dormantUsersStore.load({
                    params:{
                        start:0,
                        productid:this.productCombo.getValue(),
                        limit:this.sizeCombo.getValue()                
                    }
                });   
            // this.fetchAgedData1(true);  
            }
        }      
    },
    
    fetchAgedData1:function(limit){
//        this.printButton.setParams({
//        	fromDate:WtfGlobal.convertToGenericDate(this.fromDate.getValue()),
//        	toDate:WtfGlobal.convertToGenericDate(this.toDate.getValue()),
//            start:0,
//            limit:limit?this.pP.combo.value:30,
//            creditonly:true,
//            name: WtfGlobal.getLocaleText("acc.saleByItem.summaryReport"),
//            filetype: 'print'
//        });
//        this.exportbtn.setParams({
//        	fromDate:WtfGlobal.convertToGenericDate(this.fromDate.getValue()),
//        	toDate:WtfGlobal.convertToGenericDate(this.toDate.getValue()),
//            start:0,
//            limit:limit?this.pP.combo.value:30,
//            creditonly:true,
//            name: WtfGlobal.getLocaleText("acc.saleByItem.summaryReport")
//        });
    },
//      getDates:function(start){
//        var d=new Date();
//        var monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
//        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
//        if(d<fd)
//            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
//        if(start)
//            return fd;
//        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
//    },

     linkRenderer:function(value,metadata,record){                  
            return '<a class="jumplink" href="#" onclick="openAccountStatement(\''+record.data.custid+'\',\''+record.data.isCustomer+'\')">'+value+'</a>';                                      
    },
    
    handleChart : function(){
        var panel = Wtf.getCmp('ChartPanel'+this.id);
        if(!panel){
         panel = new Wtf.TopAndDormantUsersChartPanel(this.getGraphPanelObject());
            this.ownerCt.add(panel);
        } else {
            panel.chartTableData();
        }
        this.ownerCt.setActiveTab(panel);
//        Wtf.getCmp('topCustomersByProducts').setActiveTab(panel);
//        Wtf.getCmp('as').doLayout();
    },
    
    getGraphPanelObject: function () {
        return {
                    title : WtfGlobal.getLocaleText("acc.lp.chartagedreceivable"),
                    tabTip : WtfGlobal.getLocaleText("acc.lp.chartagedreceivable"),
                    id : 'ChartPanel'+this.id,
                    closable : true,
                    border : false,
                    autoScroll : true,
                    bodyStyle : 'background:white',
                    parent : this,
                    iconCls:'accountingbase graphTabIcon',
                    rec:this.topUsersGrid.getStore(),
                    rec1:this.dormantUsersGrid.getStore(),
                    isProduct:this.isProduct,
                    isCustomer:this.isCustomer,
                    layout : 'fit'
                };
    }
    
});



//    this.resetBttn=new Wtf.Toolbar.Button({
//        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
//        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
//        id: 'btnRec' + this.id,
//        scope: this,
//        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
//        disabled :false
//    });


//this.resetBttn.on('click',this.handleResetClick,this);