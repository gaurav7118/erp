/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

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

function getSerialNoTabViewDynamicLoad(){
    var reportPanel = Wtf.getCmp('serialnodetail');
    if(reportPanel == null){
        reportPanel = new Wtf.account.TransactionListPanelViewSerialNoDetails({
            id : 'serialnodetail',
            border : false,
            title: WtfGlobal.getLocaleText("acc.field.SerialNoDetailReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.SerialNoDetailReport"),
            layout: 'fit',
            iscustreport : true,
            closable : true,
            isCustomer:true,
            isSalesPersonName:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}
function getProductExpiryTabViewDynamicLoad(){
    var reportPanel = Wtf.getCmp('productexpirydetail');
    if(reportPanel == null){
        reportPanel = new Wtf.account.TransactionListPanelViewSerialNoDetails({
            id : 'productexpirydetail',
            border : false,
            title: WtfGlobal.getLocaleText("acc.field.ProductExpiryDetailReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.ProductExpiryDetailReport"),
            layout: 'fit',
            iscustreport : true,
            closable : true,
            isCustomer:false,
            ispurchase:true,
            isSalesPersonName:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

//*****************************************************************************

Wtf.account.TransactionListPanelViewSerialNoDetails = function(config) {
    Wtf.apply(this, config);
    
    this.custRec = Wtf.data.Record.create ([{
        name:'accountname',
        mapping:'accname'
    },{
        name:'accountid',
        mapping:'accid'
    },{
        name:'acccode'
    }]);
    this.custStore =  new Wtf.data.Store({
        url:this.isCustomer?"ACCCustomer/getCustomersForCombo.do":"ACCVendor/getVendorsForCombo.do",
        baseParams:{
            mode:2,
            group:this.isCustomer?10:13,
            deleted:false,
            nondeleted:true,
            combineData:this.isCustomer?1:-1  //Send For Seprate Request
        },
        reader: new  Wtf.data.KwlJsonReader({
            root: "data"
        },this.custRec)
    });

    this.GridRec = Wtf.data.Record.create([
    {
        name:'customer'
    },

    {
        name:'pid'
    },

    {
        name:'prodname'
    },

    {
        name:'transactionDate', 
        type:'date'
    },

    {
        name:'transactionNumber'
    },

    {
        name:'contractnumber'
    },

    {
        name:'cid'
    },

    {
        name:'transid'
    },

    {
        name:'personCode'
    },

    {
        name:'personName'
    },

    {
        name:'quantity'
    },

    {
        name: 'type'
    },

    {
        name: 'accId'
    },

    {
        name: 'accName'
    },

    {
        name: 'batchdetails'
    },

    {
        name: 'isFixedAsset'
    },

    {
        name: 'isLeaseFixedAsset'
    },

    {
        name: 'transactionType'
    },

    {
        name: 'batch'
    },

    {
        name: 'location'
    },
    {
        name: 'assetid'
    },

    {
        name: 'warehouse'
    },
    {
        name: 'expdate'
    }
    ]);
    
    this.SerialNoStore = new Wtf.data.GroupingStore({
        url:"ACCCustomer/getCustomerTransactionDetail.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec),
        sortInfo: {
            field: 'transactionDate',
            direction: "ASC"
        }
    });
    
   
    var groupView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: true,
        emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>',
        groupTextTpl: '{group} '
    });
    
   
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.expander = new Wtf.grid.RowExpander({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false       
    });
    this.gridColumnModelArr=[];
    this.gridColumnModelArr.push(this.sm,this.expander,
    {
        header:this.isCustomer?WtfGlobal.getLocaleText("acc.dimension.module.14"):WtfGlobal.getLocaleText("acc.invoiceList.ven"), // "Customer
        dataIndex:"accName",
        align:'left',
        width:150,
        pdfwidth:75,
        sortable:true
    },{
        header:this.isCustomer?WtfGlobal.getLocaleText("acc.stockLedgerCust.Code"):WtfGlobal.getLocaleText("acc.stockLedgerven.Code"), // "Code",
        dataIndex:"personCode",
        renderer:WtfGlobal.deletedRenderer,
        align:'center',
        width:150,
        pdfwidth:75
    },{
        header:WtfGlobal.getLocaleText("acc.productList.gridProductID"), // "Product ID",
        dataIndex:"pid",
        //hidden: true,
        fixed: true,
        renderer:WtfGlobal.deletedRenderer,
        width:150,
         pdfwidth:75,
         sortable:true
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // product nae
        dataIndex:"prodname",
        align:'center',
        renderer : function(val) {
            val = val.replace(/(<([^>]+)>)/ig,"");
            return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
        },
        width:150,
        pdfwidth:75,
        sortable:true
    },{
        header: WtfGlobal.getLocaleText("acc.inventorysetup.batch"),  //Batch
        dataIndex:"batch",
        align:'center',
        width:150,
        pdfwidth:85,
        sortable:true
    },{
        header: (WtfGlobal.getLocaleText("acc.inventorysetup.batch")+" "+ WtfGlobal.getLocaleText("acc.field.ExpiryDate")),  //Batch Expiry Date
        dataIndex:"expdate",
        align:'center',
        width:150,
        pdfwidth:85,
        sortable:true
    },{ 
        
        header: WtfGlobal.getLocaleText("acc.inventorysetup.locationmaster"),//location
        dataIndex:"location",
        align:'center',
        width:150,
        pdfwidth:85,
        sortable:true
    },{
        
        header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),//ware
        dataIndex:"warehouse",
        align:'center',
        width:150,
        pdfwidth:85,
        sortable:true  
    },{
        header:WtfGlobal.getLocaleText("acc.field.DocumentNo"), // "Document No
        dataIndex:"transactionNumber",
        renderer:WtfGlobal.deletedRenderer,
        align:'center',
        width:150,
        pdfwidth:55,
        sortable:true
    },{
        header: WtfGlobal.getLocaleText("acc.field.DocumentType"), // document type user
        dataIndex:"transactionType",
        align:'left',
        width:150,
        pdfwidth:85,
        sortable:true
    },{
        header:WtfGlobal.getLocaleText("acc.field.TransactionDate"), // "Date",
        dataIndex:'transactionDate',
        renderer:WtfGlobal.onlyDateDeletedRenderer,
        width:150,
        pdfwidth:75,
        sortable:true
    },{
        
        header: WtfGlobal.getLocaleText("acc.inventoryList.qty"),  //quantity
        dataIndex:"quantity",
        align:'center',
        width:150,
        pdfwidth:85,
        sortable:true
      },{
        header:WtfGlobal.getLocaleText("acc.Lease.ContractID"), // contractnumber
        dataIndex:"contractnumber",
        renderer:WtfGlobal.deletedRenderer,
        align:'center',
        width:150,
        pdfwidth:55,
        renderer:WtfGlobal.linkDeletedRenderer,  
        sortable:true
    });
        
     this.fetchBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.fetchStatement                        
    });
    
    /*
     * Provided button to expand or collapse all row details. 
     * We display S.No.,SerialNo.,Exp.FromDate,Exp.EndDate,DDD24.
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
//        readOnly:true,
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
//        readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    
    this.grid = new Wtf.grid.GridPanel({  
        stripeRows :true,
        trackMouseOver: true,
        id:"serialexpirydetail"+config.helpmodeid,
        store:this.SerialNoStore,
        cm: new Wtf.grid.ColumnModel(this.gridColumnModelArr),
        sm:this.sm,
        plugins:this.expander,
        border:false,
        tbar: [WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, this.fetchBttn, '-', this.expandCollpseButton],
        layout:'fit',
        viewConfig:{
            forceFit: false,
            view:groupView,
            emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>'
        },
        loadMask:true
    });
    
    this.getMyConfig();
    this.expander.on("expand",fillExpanderBody,this);
    this.grid.on('cellclick',this.onCellClick, this);
    this.grid.on('render', function() {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    
    function fillExpanderBody(scope, record, body){
        var disHtml = "";
        var arr=[];
        var header="";
        this.expanderBody=body;   
        var productTypeText = this.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pTypeNonInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");
        var batchdetails=record.data.batchdetails;
         if(batchdetails!=undefined && batchdetails!=[] && batchdetails!="[]"){
            arr=[// WtfGlobal.getLocaleText("acc.inventorysetup.batch"),//batch,
            (record.data.assetid!="")? WtfGlobal.getLocaleText("acc.fixed.asset.id"):'',// assetid
            WtfGlobal.getLocaleText("acc.field.SerialNo"),//Serial No,
            WtfGlobal.getLocaleText("acc.field.Exp.FromDate"),//exp from date,
            WtfGlobal.getLocaleText("acc.field.Exp.EndDate")//exp end date,
//            this.isCustomer? WtfGlobal.getLocaleText("acc.field.Exp.VenEndDate"):WtfGlobal.getLocaleText("acc.field.Exp.cusEndDate"),// vendor exp end date,
       
            ];
            var custArr = [];
            custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.SerialWindow_ModuleId]);
            var arrayLength = arr.length;
            for (i = 0; i < custArr.length; i++) {
                if (custArr[i].header != undefined)
                    arr[arrayLength + i] = custArr[i].header;
            }
            var count = 0;
            for (var i = 0; i < arr.length; i++) {
                if (arr[i] != "") {
                    count++;
                }
            }
            count = count + 2;
            var widthInPercent = 100 / count;
            var gridHeaderText = WtfGlobal.getLocaleText("acc.batch.srlist");
            header = "<span class='gridHeader'>" + gridHeaderText + "</span>";   //Product List
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
          
            var arrayLength=arr.length;
            var arrCounter=0;
      
            for (var arrI = 0; arrI < arr.length; arrI++) {
                if (arr[arrI] != undefined)
                    var action = this.isCustomer ? WtfGlobal.getLocaleText("acc.field.Exp.VenEndDate") : WtfGlobal.getLocaleText("acc.field.Exp.cusEndDate")
                if (arr[arrI] == action) {              // large header hence require more width
                    header += "<span class='headerRow' style='width: 14% ! important;'>" + arr[arrI] + "</span>";
                } else {
                    header += "<span class='headerRow' style='width: " + widthInPercent + "% ! important;'>" + arr[arrI] + "</span>";
                }

            }

        header += "<span class='gridLine'></span>";
        var quantity=record.data.quantity   ;   
       
            for(var storeCount=0;storeCount<quantity;storeCount++){
                var rec=record;
                var productname=this.withInvMode?rec.data['productdetail']: rec.data['prodname'];
                var  assetID=(record.data.assetid!="")?rec.data['assetid']:"";
        
            var batchdetails=record.data.batchdetails;
                var batchrecords="";
            if((batchdetails!=undefined || batchdetails!=[]) && batchdetails.length>1){
                    batchrecords= eval('(' + batchdetails + ')');
                    var batchObj=batchrecords[storeCount];
                    if(batchObj!=undefined) {
                        var serialno=batchObj.serialno;
                        var batch= batchObj.batch;
                        var expstart="",expend="";
                        if(batchObj.expstart!=undefined && batchObj.expstart!=""){
                         expstart= new Date(batchObj.expstart);
                        }
                        if(batchObj.expend!=undefined && batchObj.expend!=""){
                         expend=  new Date(batchObj.expend)
                        }
//                        var vendorExpDate="";
//                        if(batchObj.vendorExpDate!=undefined && batchObj.vendorExpDate!="")
//                        { 
//                            vendorExpDate=batchObj.vendorExpDate;
//                        }else{
//                            vendorExpDate="";
//                        }
                                                //Column : S.No.
                        header += "<span class='gridNo'>"+(storeCount+1) +".&nbsp;&nbsp;</span>";

                        //asset id
                        if(record.data.assetid!="")
                            header += "<span class='gridRow'  wtf:qtip='"+assetID+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(assetID,15)+"</span>";
                        //Column : serial no
                        header += "<span class='gridRow'  wtf:qtip='"+serialno+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(serialno,15)+"</span>";

                        //Columnexp start 
                          header += "<span class='gridRow'  wtf:qtip='"+(expstart==""?"":expstart.format(WtfGlobal.getOnlyDateFormat()))+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis((expstart==""?"":expstart.format(WtfGlobal.getOnlyDateFormat())),15)+"</span>";   // ERP-12776 [SJ]
                        //Column : exp end
                        header += "<span class='gridRow'  wtf:qtip='"+(expend==""?"":expend.format(WtfGlobal.getOnlyDateFormat()))+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis((expend==""?"":expend.format(WtfGlobal.getOnlyDateFormat())),15)+"</span>";// ERP-12776 [SJ]
                        //Column :vendor exp end
            
//                            header += "<span class='gridRow'  wtf:qtip='" + vendorExpDate + "' style='width: 14% ! important;'>" + Wtf.util.Format.ellipsis((vendorExpDate!=""?vendorExpDate:"-"), 15) + "</span>";                   
                        // custom fields
                        for (var j = 0; j < custArr.length; j++) {
                            if (batchObj[custArr[j].dataIndex] != undefined && batchObj[custArr[j].dataIndex] != "null" && batchObj[custArr[j].dataIndex] != "")
                                header += "<span class='gridRow'style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + batchObj[custArr[j].dataIndex] + "'>" + Wtf.util.Format.ellipsis(batchObj[custArr[j].dataIndex], 15) + "&nbsp;</span>";
                            else
                                header += "<span class='gridRow' style='width:" + widthInPercent + "% ! important;'>&nbsp;&nbsp;</span>";
                        }
                    }
                }

                header +="<br>";
            }
         }else{
            header += "<span class='headerRow'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</span>"
        }
            disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>"; //}
            this.expanderBody.innerHTML = disHtml;
    //  }
        }
    this.SerialNoStore.on("datachanged", function(store){
        if(store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
        }
    },this);
     
    this.CustomerMSComboconfig={
        hiddenName:'accountmulselectcombo',         
        store: this.custStore,
        valueField:'accountid',
        hideLabel:false,
        hidden : false,
        displayField:'accountname',
        emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") ,
        mode: 'local',
        typeAhead: true,
        selectOnFocus:true,
        triggerAction:'all',
        scope:this
    };
        
    this.cmbAccount = new Wtf.common.Select(Wtf.applyIf({
        multiSelect:true,
        fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',,
        forceSelection:true,   
        extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
        extraComparisionField:'acccode',// type ahead search on acccode as well.
        listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
        hidden:this.ispurchase?true:false,
        width:240
    },this.CustomerMSComboconfig));
            
    this.custStore.on("load", function(store){
        var storeNewRecord=new this.custRec({
            accountname:'All',
            accountid:'All',
            acccode:''
        });
        this.cmbAccount.store.insert( 0,storeNewRecord);
        this.cmbAccount.setValue("All");
    },this);         
    this.custStore.load();
    
    this.cmbAccount.on('select',function(combo,custRec,index){ //multiselection in case of all 
        if(custRec.get('accountid')=='All'){  //case of multiple record after all
            combo.clearValue();
            combo.setValue('All');
        }else if(combo.getValue().indexOf('All')>=0){  // case of all after record
            combo.clearValue();
            combo.setValue(custRec.get('accountid'));
        }
    } , this);
    
    this.DateStore = new Wtf.data.SimpleStore({
        fields:[{
            name:'name'
        },{
            name:'value',
            type:'boolean'
        }],
        data:[['Document Date',false],['Document Exp. Date',true]]
    });
    this.dateType= new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        valueField:'value',
        displayField:'name',
       // disabled:this.isEdit?false:true,
        store:this.DateStore,
        id: "DateType"+this.heplmodeid+this.id,
        fieldLabel:WtfGlobal.getLocaleText("acc.serialNoDetail.Date"),  // date type
        allowBlank:this.isOrder,
        value:false,
         width:150,
        typeAhead: true,
        forceSelection: true,
        name:'dateType'
       });
       
    this.StockCmbStore = new Wtf.data.SimpleStore({
        fields: [{
                name: 'name'
            }, {
                name: 'value'
            }],
        data: [['All Stock', 'all'], ['In-Hand Stock', 'inhand']]
    });
    this.StockCmb = new Wtf.form.ComboBox({
        triggerAction: 'all',
        mode: 'local',
        valueField: 'value',
        displayField: 'name',
        hidden:this.ispurchase?false:true,
        store: this.StockCmbStore,
        id: "Stock" + this.heplmodeid + this.id,
        fieldLabel: WtfGlobal.getLocaleText("acc.serialNoDetail.Date"), // date type
        value: 'all',
        width: 150,
        typeAhead: true,
        forceSelection: true,
        name: 'stocktype'
    });
       
    this.SerialNoStore.on('beforeload', function() {
//        this.exportButton.enable()
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        this.CustomerID=this.cmbAccount.getValue();
        this.stockType=this.StockCmb.getValue();
        this.dateVal=this.dateType.getValue();
        this.isPurchase=this.ispurchase;
        this.ss=(this.quickPanelSearch.getValue()==undefined)?"":this.quickPanelSearch.getValue();
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        var fromdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        
        
        var currentBaseParams =this.SerialNoStore.baseParams;
        currentBaseParams.customerID=this.CustomerID;
        currentBaseParams.startdate=fromdate;
        currentBaseParams.enddate=todate;
        currentBaseParams.dateType=this.dateVal;
        currentBaseParams.isPurchase=this.isPurchase;
        currentBaseParams.ss=this.ss;
        currentBaseParams.fetchOnHandData=(this.stockType == "inhand" ? true : false);
        this.SerialNoStore.baseParams=currentBaseParams;
        WtfGlobal.setAjaxTimeOut();
    },this);
    
    this.SerialNoStore.on('load', function(store) {
        WtfGlobal.resetAjaxTimeOut();
        if(this.SerialNoStore.getCount() < 1) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    }, this);
    
    this.SerialNoStore.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
        this.expander.resumeEvents('expand');
    }, this);
    

    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.serialNoDetail.QuickSearchEmptyText"), // "Search by Document no, Description, Code, Party / Cost Center ...",
        width: 300,
        id:"quickSearch"+config.helpmodeid,
        field: 'transactionNumber'
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
    

   
//    this.exportButton=new Wtf.exportButton({
//        obj:this,
//        id:"exportReports"+this.id,
//        text: WtfGlobal.getLocaleText("acc.common.export"),
//        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
//        disabled :true,
//        hidden:true,
//        scope : this,
//        menuItem:{
//            csv:true,
//            pdf:true,
//            rowPdf:false
//        },
//        params:{
//            enddate :  WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
//            startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//            //            start:0,
//            //            limit:this.pP.combo.value,
//            isStockLedger : true
//        },
//        get:917
//    });
    
    Wtf.account.TransactionListPanelViewSerialNoDetails.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewSerialNoDetails,Wtf.Panel, {
    onRender: function(config){
        this.SerialNoStore.load({
            params:{
                start:0,
                limit:30
            }
        });
       
       
       var bottombtnArr=[];

        bottombtnArr.push('-', 
        this.exportButton=new Wtf.exportButton({
                obj:this,
                filename:(this.isCustomer==true?WtfGlobal.getLocaleText("acc.field.SerialNoDetailReport")+"_v1":WtfGlobal.getLocaleText("acc.field.ProductExpiryDetailReport")+"_v1"),
                id:"exportReports"+this.id,
                text: WtfGlobal.getLocaleText("acc.common.export"),
                tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
                disabled :true,
                scope : this,
                menuItem:{
                    csv:true,
                    pdf:true,
                    xls:true

                },
                params:{
                    enddate :  WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                    startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                    start:0,
                    limit : this.pP!=undefined?this.pP.combo.value:"",
                    customerID:this.cmbAccount.getValue(),
                    dateType:this.dateType.getValue(),
                    isPurchase:this.ispurchase,
                    ss : (this.quickPanelSearch.getValue()==undefined)?"":this.quickPanelSearch.getValue(),
                    fetchOnHandData : (this.StockCmb.getValue() == "inhand" ? true : false)
                },
                get:Wtf.autoNum.vendorproductexpiry
            }));

          bottombtnArr.push('-', this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            menuItem:{print:true},
             params:{
                    enddate :  WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                    startdate : WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                    start:0,
                    limit : this.pP!=undefined?this.pP.combo.value:"",
                    customerID:this.cmbAccount.getValue(),
                     dateType:this.dateType.getValue(),
                   isPurchase:this.ispurchase,
                   ss : (this.quickPanelSearch.getValue()==undefined)?"":this.quickPanelSearch.getValue()
                    
                },
            get:Wtf.autoNum.vendorproductexpiry
          }));

        this.leadpan = new Wtf.Panel({
            layout: 'fit',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: [this.quickPanelSearch, this.resetBttn, '-',this.cmbAccount,'-',this.dateType,'-',this.StockCmb],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.SerialNoStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    items:bottombtnArr
                })
            }]
        }); 
        this.add(this.leadpan);
        
        Wtf.account.TransactionListPanelViewSerialNoDetails.superclass.onRender.call(this,config);
    },

    getMyConfig : function(){
        WtfGlobal.getGridConfig (this.grid, Wtf.Product_Batch_Expiry_Date_Report_Module_Id, false, false);
    },
    saveMyStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Product_Batch_Expiry_Date_Report_Module_Id, grid.gridConfigId, false);
    },
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement:function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        this.CustomerID=this.cmbAccount.getValue();
        this.dateVal=this.dateType.getValue();
        
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        var fromdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        
        this.SerialNoStore.load({
            params: {
                startdate:fromdate,
                enddate:todate,
                start:0,
                customerID:this.CustomerID,
                dateType:this.dateVal,
                limit:this.pP.combo.value
            }
        });
    },
    onCellClick:function(g,i,j,e){
        var formrec = this.grid.getStore().getAt(i);
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="contractnumber"){
            var rec = this.sm.getSelected();
            if(rec.data.cid !=""){
                callContractDetails(rec);  
            }
        }
    },
    
    /*
     * ExpandCollapse button handler
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollapseGrid: function (btntext) {
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            for (var i = 0; i < this.grid.getStore().data.length; i++) {
                this.expander.collapseRow(i)
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            for (var i = 0; i < this.grid.getStore().data.length; i++) {
                this.expander.expandRow(i)
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
 
});



