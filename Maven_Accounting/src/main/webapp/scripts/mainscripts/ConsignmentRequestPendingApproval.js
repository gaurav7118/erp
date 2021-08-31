Wtf.account.ConsignmentRequestPendingApproval=function(config){
    Wtf.apply(this, config);
    this.moduleid=config.moduleId;
    this.rejectedRecords=config.rejectedRecords;
    this.label = config.label;
    this.GridRec = Wtf.data.Record.create ([
    {
        name:'billid'
    },{
        name:'billno'
    },{
        name:'rowid'
    },{
        name:'productname'
    },{
        name:'productdetail'
    },{
        name:'prdiscount'
    },{
        name:'discountispercent'
    },{
        name:'amount'
    },{
        name:'productid'
    },{
        name:'accountid'
    },{
        name:'accountname'
    },{
        name:'partamount'
    },{
        name:'quantity'
    }, {
        name:'batchid'
    },{
        name:'newbatchserialid'
    },{
        name:'serialbatchmapid'
    },{
            name:'locationbatchmapid'    
    },{
        name:'warehousename'
    },{
        name:'locationname'
    }, {
        name:'batchname'
    },{
        name:'serialname'
    },{
        name:'status'
    },{
        name:'rejectedby'
    }, {
        name:'createdby'
    },{
        name:'dquantity'
    },{
        name:'unitname'
    },{
        name:'baseuomname'
    },{
        name:'baseuomrate'
    },{
        name:'baseuomquantity'
    }, {
        name:'rate'
    },{
        name:'rateinbase'
    }, {
        name:'externalcurrencyrate'
    }, {
        name:'orderrate'
    },{
        name:'desc', 
        convert:WtfGlobal.shortString
    }, {
        name:'productmoved'
    },{
        name:'currencysymbol'
    },{
        name:'currencyrate'
    },{
        name: 'type'
    },{
        name: 'pid'
    },{
        name: 'partno'
    },{
        name: 'unitname'
    },{
        name:'carryin'
    },{
        name:'remark'
    },{
        name:'approverremark'
    },{
        name:'permit'
    },{
        name:'linkto'
    },{
        name:'customfield'
    },{
        name:'usedflag'
    },{
        name:'balanceQuantity'
    },{
        name:'transectionno'
    },{
        name:'discount'
    },{
        name:'isuserallowtoapprove'
    },{
        name:'memo'/*,convert:this.shortString*/
    },{
        name:'creationdate',
        type:'date'
    },{
        name:'duedate',
        type:'date'
    },{
        name:'totalamount'
    },{
        name:'amountdue',
        mapping:'amountduenonnegative'
    },{
        name:'amountpaid'
    },{
        name: 'isLocationForProduct'
    },{
        name: 'isRowForProduct'
    },{
        name: 'isRackForProduct'
    },{
        name: 'isBinForProduct'
    },{
        name: 'isWarehouseForProduct'
    },{
        name: 'isBatchForProduct'
    },{
        name: 'isSerialForProduct'
    },{
        name: 'location'
    },{
        name: 'warehouse'
    },{
        name: 'movementtype'
    },{
        name: 'movementtypename'
    }
    ,{
        name: 'customerId'
    },
    {
        name: 'customerName'
    }
    ]);
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    this.editBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
        tooltip :WtfGlobal.getLocaleText("acc.invoiceList.editO"),  //acc.invoiceList.editO:-'Allows you to edit Order.':acc.invoiceList.editQ:-'Allows you to edit Quotation.'
        id: 'btnEdit' + this.id,
        scope: this,
        hidden:false,
        iconCls :getButtonIconCls(Wtf.etype.edit),
        disabled :true
    });
    this.editBttn.on('click',this.editOrderTransaction.createDelegate(this,[false]),this);
    this.approveInvoiceBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.cc.24"),
        tooltip : WtfGlobal.getLocaleText("acc.consignment.approvebtn.ttip"), //Issue 31009 - [Pending Approval]Window name should be "Approve Pending Invoice" instead of "Approve Pending Approval". it should also have deskera logo
        id: 'approvepending' + this.id,
        scope: this,
        hidden : this.rejectedRecords,
        iconCls :this.isRequisition ? "accountingbase prapprove" : getButtonIconCls(Wtf.etype.add),
        disabled :true,
        handler : this.approvePendingRequest
    });
    
    this.rejectInvoiceBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Reject"),
        tooltip : WtfGlobal.getLocaleText("acc.consignment.rejectbtn.ttip"),
        id: 'rejectpending' + this.id,
        scope: this,
        hidden : this.rejectedRecords,
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        disabled :true,
        handler : this.handleReject
    });
    
    this.StoreUrl =  "ACCSalesOrderCMN/getPendingApprovalCRDetails.do" ;//this.businessPerson=="Customer" ?: "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    
    this.Store = new Wtf.data.GroupingStore({
        url:this.StoreUrl,
        baseParams:{
            deleted:false,
            nondeleted:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            rejectedrecords:this.rejectedRecords,
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        },
        sortInfo : {
            field : 'billno',
            direction : 'DESC'
        },
        groupField :'billno',
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
  
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbarNew" + this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
        plugins: this.pPNew = new Wtf.common.pPageSize({
            id : "pPageSizeNew_"+this.id
        })
    });
        
      
    this.Store.on('beforeload', function(){
        var startDate="",endDate="",isOpening="";
        startDate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
        endDate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        
        this.Store.baseParams = {
            deleted:false,
            nondeleted:true,
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            rejectedrecords:this.rejectedRecords,
            startdate : startDate,
            enddate : endDate
        //            isOpeningBalanceInvoices:isOpening
        }
        
    }, this);
   
   
    this.summary = new Wtf.grid.GroupSummary({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect : false
    });
    this.gridView1 = new Wtf.grid.GroupingView({
        forceFit:true,
        showGroupName: true,
        enableNoGroups:true, // REQUIRED!
        hideGroupedColumn: false,
        deferEmptyText: false ,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    var columnArr =[];   
    columnArr.push(this.sm,this.rowNo,{
        header:WtfGlobal.getLocaleText("acc.product.gridProductID"),
        dataIndex:'pid',
        pdfwidth:70
    // renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
    },{
           
        pdfwidth:75,
        header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product Name",
        dataIndex: 'productname',
        sortable:true
    },{
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.nee.69"),//"status",
        dataIndex:"createdby",
        sortable:true    
    },{
           
        //        pdfwidth:75,
        header: WtfGlobal.getLocaleText("acc.MailWin.consomsg7"),//"Product Name",
        dataIndex: 'billno',
        hidden:true
    //        sortable:true
    },{
          
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),//"Description",
        dataIndex:"desc",
        sortable:true
    },{
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),//"Warehouse",
        dataIndex:"warehousename",
        sortable:true
    },{
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"),//"Location",
        dataIndex:"locationname",
        sortable:true
    },{
        pdfwidth:75,
        header:"Customer",
        dataIndex:"customerName",
//        hidden:this.rejectedRecords,
        sortable:true
    },{
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),//"Batch",
        dataIndex:"batchname",
        sortable:true,
        hidden:true
    },{
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.inventorysetup.serial"),//"Serial",
        dataIndex:"serialname",
        sortable:true,
        hidden:true
    },{
          
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
        dataIndex:"quantity",
        sortable:true
    },{
           
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),//"UOM",
        width:150,
        dataIndex:'baseuomname',
        sortable:true
    },{
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.contract.product.replacement.Status"),//"status",
        dataIndex:"status",
        sortable:true    
    },{
        pdfwidth:75,
        header:WtfGlobal.getLocaleText("acc.contract.product.replacement.Rejected"),//"status",
        dataIndex:"rejectedby",
        hidden : !this.rejectedRecords,
        sortable:true    
    }
);         
    
        
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.grid = new Wtf.grid.GridPanel({
        //id:"gridmsg"+this.id,
        stripeRows :true,
        store:this.Store,
        //tbar : this.tbar2,
        sm:this.sm,
        border:false,
        viewConfig: this.gridView1,
        forceFit:true,
        layout:'fit',
        loadMask:true,
        plugins: [gridSummary],
        cm:new Wtf.grid.ColumnModel(columnArr)
    });
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClickNew,this);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" "+WtfGlobal.getLocaleText("acc.consignment.order"),
        width: 150,
        //      id:"quickSearch"+config.helpmodeid+config.id,
        field: 'billno',
        Store:this.Store
    })
    
    var buttonArray = new Array();
    buttonArray.push(this.quickPanelSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), this.endDate);   
    //  buttonArray.push('-',this.invoiceType);
    buttonArray.push('-', {
        text : WtfGlobal.getLocaleText("acc.ra.fetch"),
        iconCls:'accountingbase fetch',
        scope : this,
        handler : this.loaddata
    });
    buttonArray.push('-',this.resetBttn);
    if(this.rejectedRecords!=undefined && this.rejectedRecords==false){
        buttonArray.push('-',this.editBttn);
    }
    buttonArray.push('-',this.approveInvoiceBttn, '-', this.rejectInvoiceBttn);     //    buttonArray.push('-',this.resetBttn,'-',this.exportButton, '-', this.printButton);     //
    this.Store.load({
        params : {
            start:0,
            limit:(this.pP!=undefined) ? this.pP.combo.value : 30              
        }
    });
    this.leadpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items:[              //this.objsearchComponent,
        {
            region:'center',
            layout:'fit',
            border:false,
            items:[this.grid],
            tbar : buttonArray,
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
        }]
       
    });
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[ this.leadpan]
    });   
    Wtf.account.ConsignmentRequestPendingApproval.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.ConsignmentRequestPendingApproval,Wtf.Panel,{
  
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    loaddata : function(){
     
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params : {
                start:0,
                limit:(this.pP!=undefined) ? this.pP.combo.value : 30              
            }
        });
    },
    
    enableDisableButtons:function(){        
        this.editBttn.enable();
        this.approveInvoiceBttn.enable();   
        this.rejectInvoiceBttn.enable();
                      
    },                
              
                      
    handleResetClickNew:function(){ 

        this.quickPanelSearch.reset();
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));

        this.Store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
       
    },
    
    ApproveRejectQuantity: function(recArr,msg,isApproveQuantity){
        this.QuantityGridRec = Wtf.data.Record.create ([
        {
            name:'billid'
        },{
            name:'billno'
        },{
            name:'rowid'
        },{
            name:'productname'
        },{
            name:'productdetail'
        },{
            name:'prdiscount'
        },{
            name:'discountispercent'
        },{
            name:'amount'
        },{
            name:'productid'
        },{
            name:'accountid'
        },{
            name:'accountname'
        },{
            name:'partamount'
        },{
            name:'quantity'
        },{
            name:'finalquantity', mapping:'quantity'
        }, {
            name:'batchid'
        },{
            name:'newbatchserialid'
        },{
            name:'serialbatchmapid'
        },{
            name:'locationbatchmapid'
        },{
            name:'warehousename'
        },{
            name:'locationname'
        }, {
            name:'batchname'
        },{
            name:'serialname'
        },{
            name:'status'
        },{
            name:'rejectedby'
        }, {
            name:'createdby'
        },{
            name:'dquantity'
        },{
            name:'unitname'
        },{
            name:'baseuomname'
        },{
            name:'baseuomrate'
        },{
            name:'baseuomquantity'
        }, {
            name:'rate'
        },{
            name:'rateinbase'
        }, {
            name:'externalcurrencyrate'
        }, {
            name:'orderrate'
        },{
            name:'desc', 
            convert:WtfGlobal.shortString
        }, {
            name:'productmoved'
        },{
            name:'currencysymbol'
        },{
            name:'currencyrate'
        },{
            name: 'type'
        },{
            name: 'pid'
        },{
            name: 'partno'
        },{
            name: 'unitname'
        },{
            name:'carryin'
        },{
            name:'remark'
        },{
            name:'approverremark'
        },{
            name:'permit'
        },{
            name:'linkto'
        },{
            name:'customfield'
        },{
            name:'usedflag'
        },{
            name:'balanceQuantity'
        },{
            name:'transectionno'
        },{
            name:'discount'
        },{
            name:'isuserallowtoapprove'
        },{
            name:'memo'/*,convert:this.shortString*/
        },{
            name:'creationdate',
            type:'date'
        },{
            name:'duedate',
            type:'date'
        },{
            name:'totalamount'
        },{
            name:'amountdue',
            mapping:'amountduenonnegative'
        },{
            name:'amountpaid'
        },{
            name: 'isLocationForProduct'
        },{
            name: 'isRowForProduct'
        },{
            name: 'isRackForProduct'
        },{
            name: 'isBinForProduct'
        },{
            name: 'isWarehouseForProduct'
        },{
            name: 'isBatchForProduct'
        },{
            name: 'isSerialForProduct'
        },{
            name: 'location'
        },{
            name: 'warehouse'
        },{
            name: 'movementtype'
        },{
            name: 'movementtypename'
        }
    ]);
    
         this.QuantityStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
            },this.QuantityGridRec)
        });
        
             for(var i=0;i<this.recArr.length;i++){
                 var rec = this.recArr[i];
           
                if(rec.data.productid!="" && rec.data.isuserallowtoapprove!=undefined && rec.data.isuserallowtoapprove==true){
                    rec.data.finalquantity=rec.data.quantity;
                    this.QuantityStore.add(rec);
                
                }
             } 
        var cm = new Wtf.grid.ColumnModel([this.rowNo,{
                header:this.label+" "+WtfGlobal.getLocaleText("acc.cn.9"),
                dataIndex:'billno',
                pdfwidth:70
                // renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.product.gridProductID"),
                dataIndex:'pid',
                pdfwidth:70
                // renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
            },{

                pdfwidth:75,
                header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),//"Product Name",
                dataIndex: 'productname',
                sortable:true
//            },{
//                pdfwidth:75,
//                header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),//"Warehouse",
//                dataIndex:"warehousename",
//                sortable:true
//            },{
//                pdfwidth:75,
//                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"),//"Location",
//                dataIndex:"locationname",
//                sortable:true
            },{
                pdfwidth:75,
                header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),//"Batch",
                dataIndex:"batchname",
                sortable:true,
                hidden:true
            },{
                pdfwidth:75,
                header:WtfGlobal.getLocaleText("acc.inventorysetup.serial"),//"Serial",
                dataIndex:"serialname",
                sortable:true,
                hidden:true
            },{

                pdfwidth:75,
                //header:WtfGlobal.getLocaleText("acc.invoice.gridQty"),//"Quantity",
                //dataIndex:"quantity",
                sortable:true,
                 header: isApproveQuantity?"Approve Quantity":"Reject Quantity",
                dataIndex: 'finalquantity',
                minValue:0,
                allowNegative:false,
                editor : new Wtf.form.NumberField({
                    allowDecimals:false,
                    allowNegative:false
                })
            }
        ]);
        
        this.approveRejectGrid = new Wtf.grid.EditorGridPanel({
            clicksToEdit:1,
            autoScroll:true,
            autoWidth:true,
            store: this.QuantityStore,
            cm:cm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:'No Record to display'
            }
        });  
         
//        this.approveRejectGrid.on('afteredit',this.updateRow,this);
        this.approveRejectGrid.on('validateedit',this.updateRow,this);
        
        this.approveRejectQuantityPanel=new Wtf.Panel({
            border: false,
            region: 'center',
            autoScroll:true,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.approveRejectGrid]
        })

        this.approveRejectQuantityWin = new Wtf.Window({
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            title: isApproveQuantity?'Approve Quantity':'Reject Quantity',
            buttonAlign: 'right',
            width: 600,
            height:400,
            layout:'fit',
            scope: this,
            items:this.approveRejectQuantityPanel,
            buttons: [{
                text: 'Save',
                scope: this,
                handler: function(){
//                    this.saveReusableDetails(rec,detail,incash);
//                    getDetails();
                    if(isApproveQuantity){
                        this.approvePendingRequestCallAjax(this.getDetails(),msg);
                    }else{
                        this.handleRejectCallAjax(this.getDetails(),msg);
                    }
                    
                    this.approveRejectQuantityWin.close();
                }   
            },{
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.approveRejectQuantityWin.close();
                }
            }]
        });
        this.approveRejectQuantityWin.show();
    },
    refreshGridView : function (panel){
        this.grid.view.refresh.defer(1,this.grid.view);
    },
     getDetails:function(){
        this.QuantityStore.each(function(rec){
            if(rec.data.rowid==undefined){
                rec.data.rowid='';
                    
            }
        },this);
        var arr=[];
        this.QuantityStore.each(function(rec){
            arr.push(this.QuantityStore.indexOf(rec));
        }, this);
        var jarray=WtfGlobal.getJSONArray(this.approveRejectGrid,true,arr);      
        return jarray;
    },
    updateRow:function(obj){
        if(obj!=null){
//            this.productComboStore.clearFilter(); // Issue 22189
            var rec=obj.record;
            var quantity = rec.get("quantity");
            if(obj.field=="finalquantity"){
                if(rec.get("serialbatchmapid")!="" && rec.get("serialbatchmapid")!= undefined){
                    obj.cancel=true;  
                }
                else if(obj.value > quantity){
                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),"Quantity can't be grater than actual Quantity."], 2);
                       obj.cancel=true;  
                 }
                else if(obj.value == 0){
                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),"Quantity can't be zero."], 2);
                       obj.cancel=true;  
                 }
            }
        }
    },    
    approvePendingRequest : function(){
        //         if(this.grid.getSelectionModel().)
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        var idData = "",nonaccessserials="";
        for(var i=0;i<this.recArr.length;i++){
            var rec = this.recArr[i];
            if(rec.data.isuserallowtoapprove!=undefined && rec.data.isuserallowtoapprove==true){
                idData += "{\"billid\":\""+rec.get('billid')+"\",\"serialbatchmapid\":\""+rec.get('serialbatchmapid')+"\"},";
            }else{
                nonaccessserials +=nonaccessserials.length>1? (","+rec.get('serialname')):rec.get('serialname');
            }
            
        }
        if(idData.length>1){
            idData=idData.substring(0,idData.length-1);
        }
        data="["+idData+"]";
        var msg=nonaccessserials.length>1 ? " except serials  "+nonaccessserials:"";
        if(idData.length>1){
            this.ApproveRejectQuantity(this.recArr,msg,true);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.approval.requestorapprovePerm")], 2);
        }
        
        
    },
    approvePendingRequestCallAjax: function(data,msg){
        this.loadMask = new Wtf.LoadMask(document.body, {
            msg: 'Saving...'
        });
        if(data.length>1){//formRecord.data.isuserallowtoapprove!=undefined && formRecord.data.isuserallowtoapprove==true       
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected") +" "+ this.label + msg +  "?", function(btn) {//nonaccessbillno.length>1 ? (" except orders "+nonaccessbillno+"?"):
                if (btn == "yes") {
                     this.loadMask.show();
                    var URL = "",winTitle="";
                    URL="ACCSalesOrder/approveConsignmentRequest.do";
          
                
                    Wtf.Ajax.requestEx({
                        url: URL,
                        params: {
                            data:data
                        }
                    }, this, this.genSuccessRespApproveTransaction, this.genFailureRespApproveTransaction);
                }
            }, this)
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.approval.requestorapprovePerm")], 2);
        }
    },
    genSuccessRespApproveTransaction: function(response) {
         this.loadMask.hide();
        //        this.remarkWindow.close();
        if(response.success){
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
                this.loaddata();
            }, this);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
        }
    },
    
    genFailureRespApproveTransaction: function(response) {
         this.loadMask.hide();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")], 2);
    }, 
    
    handleReject:function(){
           
        //        var formRecord = this.grid.getSelectionModel().getSelected();
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        var idData = "",nonaccessserials="";
        for(var i=0;i<this.recArr.length;i++){
            var rec = this.recArr[i];
            if(rec.data.isuserallowtoapprove!=undefined && rec.data.isuserallowtoapprove==true){
                idData += "{\"billid\":\""+rec.get('billid')+"\",\"serialbatchmapid\":\""+rec.get('serialbatchmapid')+"\",\"sodetailid\":\""+rec.get('rowid')+"\"},";
            }else{
                nonaccessserials +=nonaccessserials.length>1? (","+rec.get('serialname')):rec.get('serialname');
            }
            
        }
        if(idData.length>1){
            idData=idData.substring(0,idData.length-1);
        }
        data="["+idData+"]";
        //       if(formRecord.data.isuserallowtoapprove!=undefined && formRecord.data.isuserallowtoapprove==true){
        var msg=nonaccessserials.length>1 ? " except serials  "+nonaccessserials:"";
        if(idData.length>1){
            this.ApproveRejectQuantity(this.recArr,msg,false);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.approval.requestorRejectPerm")], 2);
        }
         
    },
    handleRejectCallAjax: function(data,msg){
        if(data.length>1){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttorejectselected")+" "+ this.label + msg + "?",function(btn){//+this.label
                if(btn=="yes") {
                    this.ajxUrl = "ACCSalesOrder/rejectConsignmentRequest.do";
                    Wtf.Ajax.requestEx({
                        url:this.ajxUrl,
                        params:{
                            data:data
                        }
                    },this,this.genSuccessResponseReject,this.genFailureResponseReject);
                }
            },this);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.approval.requestorRejectPerm")], 2);
        }
    },
    editOrderTransaction:function(copyInv){			// Editing Sales and Purchase Order with Inventory and Without Inventory
        var formRecord = null;
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(15,2);
            return;
        } else if(this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Please select only one record to edit."], 2);
            return;
        } 
        formRecord = this.grid.getSelectionModel().getSelected();
        var label=copyInv?WtfGlobal.getLocaleText("acc.common.copy"):WtfGlobal.getLocaleText("acc.product.edit");
        var findtablabel=copyInv?WtfGlobal.getLocaleText("acc.product.edit"):WtfGlobal.getLocaleText("acc.common.copy");
        var billid=formRecord.data.billid;
        label=label+billid;
        findtablabel=findtablabel+billid;
        this.withInvMode = formRecord.get("withoutinventory");
        var isrequesthasapprovedorders=formRecord.data.isrequesthasapprovedorders;
        if(isrequesthasapprovedorders!=undefined && isrequesthasapprovedorders==true){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.approval.requestoreditcheck")], 2);
        }else{
            this.SORec = Wtf.data.Record.create ([
            {
                name:'billid'
            },

            {
                name:'journalentryid'
            },

            {
                name:'entryno'
            },

            {
                name:'billto'
            },

            {
                name:'companyid'
            },

            {
                name:'companyname'
            },

            {
                name:'discount'
            },

            {
                name:'currencysymbol'
            },

            {
                name:'orderamount'
            },

            {
                name:'isexpenseinv'
            },

            {
                name:'currencyid'
            },

            {
                name:'shipto'
            },

            {
                name:'mode'
            },

            {
                name:'billno'
            },

            {
                name:'date', 
                type:'date'
            },

            {
                name:'duedate', 
                type:'date'
            },

            {
                name:'shipdate', 
                type:'date'
            },

            {
                name:'personname'
            },

            {
                name:'aliasname'
            },

            {
                name:'personemail'
            },

            {
                name:'personid'
            },

            {
                name:'shipping'
            },

            {
                name:'othercharges'
            },

            {
                name:'partialinv',
                type:'boolean'
            },

            {
                name:'includeprotax',
                type:'boolean'
            },

            {
                name:'amount'
            },

            {
                name:'amountbeforegst'
            },

            {
                name:'amountdue'
            },

            {
                name:'termdays'
            },

            {
                name:'termid'
            },

            {
                name:'termname'
            },

            {
                name:'incash',
                type:'boolean'
            },

            {
                name:'taxamount'
            },

            {
                name:'taxid'
            },

            {
                name:'orderamountwithTax'
            },

            {
                name:'taxincluded',
                type:'boolean'
            },

            {
                name:'taxname'
            },

            {
                name:'deleted'
            },

            {
                name:'termamount'
            },

            {
                name:'amountinbase'
            },

            {
                name:'memo'
            },

            {
                name:'createdby'
            },

            {
                name:'createdbyid'
            },

            {
                name:'externalcurrencyrate'
            },

            {
                name:'ispercentdiscount'
            },

            {
                name:'discountval'
            },

            {
                name:'crdraccid'
            },

            {
                name:'creditDays'
            },

            {
                name:'isRepeated'
            },

            {
                name:'porefno'
            },

            {
                name:'costcenterid'
            },

            {
                name:'costcenterName'
            },

            {
                name:'interval'
            },

            {
                name:'intervalType'
            },

            {
                name:'NoOfpost'
            }, 

            {
                name:'NoOfRemainpost'
            },  

            {
                name:'templateid'
            },

            {
                name:'templatename'
            },

            {
                name:'startDate', 
                type:'date'
            },

            {
                name:'nextDate', 
                type:'date'
            },

            {
                name:'expireDate', 
                type:'date'
            },

            {
                name:'repeateid'
            },

            {
                name:'status'
            },

            {
                name:'amountwithouttax'
            },

            {
                name:'amountwithouttaxinbase'
            },

            {
                name:'commission'
            },

            {
                name:'commissioninbase'
            },

            {
                name:'amountDueStatus'
            },

            {
                name:'salesPerson'
            },

            {
                name:'agent'
            },

            {
                name:'shipvia'
            },

            {
                name:'fob'
            },

            {
                name:'approvalstatus'
            },

            {
                name:'approvalstatusinfo'
            },

            {
                name:'approvalstatusint', 
                type:'int', 
                defaultValue:-1
            },

            {
                name:'archieve', 
                type:'int'
            },

            {
                name:'withoutinventory',
                type:'boolean'
            },

            {
                name:'isfavourite'
            },

            {
                name:'isCapitalGoodsAcquired'
            },

            {
                name:'isRetailPurchase'
            },

            {
                name:'importService'
            },

            {
                name:'othervendoremails'
            },

            {
                name:'termdetails'
            },

            {
                name:'approvestatuslevel'
            },// for requisition

            {
                name:'isrequesteditable'
            },// for Consignment Request Approval

            {
                name:'posttext'
            },

            {
                name:'isOpeningBalanceTransaction'
            },

            {
                name:'isNormalTransaction'
            },

            {
                name:'isreval'
            },

            {
                name:'islockQuantityflag'
            },

            {
                name:'isprinted'
            },

            {
                name:'validdate', 
                type:'date'
            },

            {
                name:'cashtransaction',
                type:'boolean'
            },

            {
                name:'shiplengthval'
            },

            {
                name:'invoicetype'
            },

            {
                name:'landedInvoiceID'
            },

            {
                name:'landedInvoiceNumber'
            },

            {
                name:'termdays'
            },

            {
                name:'billingAddress'
            },

            {
                name:'billingCountry'
            },

            {
                name:'billingState'
            },

            {
                name:'billingPostal'
            },

            {
                name:'billingEmail'
            },

            {
                name:'billingFax'
            },

            {
                name:'billingMobile'
            },

            {
                name:'billingPhone'
            },

            {
                name:'billingContactPerson'
            },

            {
                name:'billingContactPersonNumber'
            },

            {
                name:'billingContactPersonDesignation'
            },
            {
                name:'billingWebsite'
            },
            {
                name:'billingCounty'
            },
            {
                name:'billingCity'
            },

            {
                name:'billingAddressType'
            },

            {
                name:'shippingAddress'
            },

            {
                name:'shippingCountry'
            },

            {
                name:'shippingState'
            },

            {
                name:'shippingCounty'
            },
            
            {
                name:'shippingCity'
            },

            {
                name:'shippingEmail'
            },

            {
                name:'shippingFax'
            },

            {
                name:'shippingMobile'
            },

            {
                name:'shippingPhone'
            },

            {
                name:'shippingPostal'
            },

            {
                name:'shippingContactPersonNumber'
            },
            {
                name:'shippingContactPersonDesignation'
            },
            {
                name:'shippingWebsite'
            },
            {
                name:'shippingContactPerson'
            },

            {
                name:'shippingRoute'
            },

            {
                name:'shippingAddressType'
            },

            {
                name:'sequenceformatid'
            },

            {
                name:'gstIncluded'
            },

            {
                name:'lasteditedby'
            },

            {
                name:'salespersonname'
            },

            {
                name:'isConsignment'
            },

            {
                name:'custWarehouse'
            },

            {
                name:'movementtype'
            },

            {
                name:'deliveryTime'
            },

            {
                name:'getFullShippingAddress'
            },

            {
                name:'selfBilledInvoice'
            },

            {
                name:'RMCDApprovalNo'
            },

            {
                name:'fixedAssetInvoice'
            },

            {
                name:'fixedAssetLeaseInvoice'
            },

            //Below Fields are used only for Cash Sales and Purchase.

            {
                name:'methodid'
            },

            {
                name:'paymentname'
            },

            {
                name:'detailtype'
            },

            {
                name:'cardno'
            },

            {
                name:'nameoncard'
            },

            {
                name:'cardexpirydate', 
                type:'date'
            },

            {
                name:'cardtype'
            },

            {
                name:'cardrefno'
            },

            {
                name:'chequeno'
            },

            {
                name:'bankname'
            },

            {
                name:'shippingterm'
            },

            {
                name:'chequedate', 
                type:'date'
            },

            {
                name:'chequedescription'
            },

            {
                name:'termsincludegst'
            },

            {
                name:'attachment'
            },

            {
                name:'fromdate',
                type:'date'
            },

            {
                name:'todate',
                type:'date'
            },

            {
                name:'customerporefno'
            },

            {
                name:'totalprofitmargin'
            },

             {
                name:'totalprofitmarginpercent'
            },
            {name:'requestWarehouse'},
            {name:'requestLocation'}
            ]);
            this.StoreUrl = "ACCSalesOrderCMN/getSalesOrdersMerged.do";
            this.RequestStore = new Wtf.data.Store({
                url:this.StoreUrl,
                baseParams:{
                    deleted:false,
                    nondeleted:false,
                    cashonly:false,
                    creditonly:false,
                    CashAndInvoice:true,
                    salesPersonFilterFlag:true,
                    isFixedAsset:false,
                    isLeaseFixedAsset:false,
                    isConsignment:true,
                    editSOfromPendingOrder:true,
                    consolidateFlag:false,
                    billid:billid,
                    companyids:companyids,
                    gcurrencyid:gcurrencyid,
                    userid:loginid,
                    isfavourite:false,
                    isprinted:false
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty:'count'
                },this.SORec)
            });
            this.RequestStore.load();
            this.RequestStore.on('load',function(){
                var rec=this.RequestStore.getAt(0);
                if(rec.data.isrequesteditable!=undefined && rec.data.isrequesteditable!=true){
                    var win=null;
                    if(billid!=undefined && billid!=null && billid!=""){
                        win=billid+"_"+'consignmentreuest';
                    }
                    callConsignmentRequest(true,rec,win, copyInv,null,false,false,false,true,true);
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.approval.pendingordereditcheck")],2);
                }
            },this)
            
        }
                    
    	
    },
    genSuccessResponseReject:function(response){
        WtfComMsgBox([this.label,response.msg],response.success*2+1);
        if(response.success){
            (function(){
                this.loaddata();
            }).defer(WtfGlobal.gridReloadDelay(),this);

        }
    },
    genFailureResponseReject:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg) {
            msg=response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }

});
