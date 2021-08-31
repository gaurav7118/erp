/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



function callQAApprovalReportDynamicLoad() {
    var panel = Wtf.getCmp("qaApprovalItemsMainTab");
    if (panel == null) {
        panel = new Wtf.TabPanel({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.QAApprovalPOItems"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.field.YoucanviewpendingandrejectedQAitemshere"),
            id: 'qaApprovalItemsMainTab',
            closable: true,
            border: false,
            iconCls: 'accountingbase balancesheet',
            activeTab: 0
        });
        Wtf.getCmp('as').add(panel);
        callQAPendingRejectedItems(true);
        callQAPendingRejectedItems(false);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callQAPendingRejectedItems(isPending) {
    var id = (isPending ? 'pending' : 'rejected') + "ItemsRpt"
    var title = (isPending ? WtfGlobal.getLocaleText("acc.field.Pending") : WtfGlobal.getLocaleText("acc.field.Rejected")) + WtfGlobal.getLocaleText("acc.field.ItemsReport");
    var pendingRejectedRpt = Wtf.getCmp(id);
    if (pendingRejectedRpt == null) {
        pendingRejectedRpt = new Wtf.QAPendingRejectedItems({
            id: id,
            isPending: isPending,
            layout: 'fit',
            iconCls: 'accountingbase ledger',
            title: title,
            tabTip: title,
            border: false
        });


        Wtf.getCmp('qaApprovalItemsMainTab').add(pendingRejectedRpt);
    }
}


Wtf.QAPendingRejectedItems = function(config){
    Wtf.QAPendingRejectedItems.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.QAPendingRejectedItems, Wtf.Panel, {
    initComponent: function() {
        Wtf.QAPendingRejectedItems.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.QAPendingRejectedItems.superclass.onRender.call(this, config);
        this.dmflag = 1;        
    this.fromdateVal = new Date();
    this.todateVal = new Date();
    
 this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:true,
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });

        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.common.fetch"),
            iconCls:'accountingbase fetch',
            scope: this,
            handler: function() {
                   this.initloadgridstore();
            }
        });

        this.record = Wtf.data.Record.create([
            {"name":"id"},
            {"name":"billno"},
            {"name":"vendorname"},
            {"name":"date", type:'date'},
            {"name":"duedate", type:'date'},
            {"name":"productcode"},
            {"name":"productid"},
            {"name":"productname"},            
            {"name":"quantity"},            
            {"name":"qastatus"},
            {"name":"qastatusremark"}
        ]);
        
          var grpView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: true,
            enableGroupingMenu: true,
            hideGroupedColumn: false
        });
        
        this.ds = new Wtf.data.GroupingStore({
            sortInfo: {
                field: 'billno',
                direction: "DESC"
            },
            groupField:"billno",            
            baseParams: {
                flag: 53,
                isPending:this.isPending
            },
            url: 'ACCPurchaseOrderCMN/getQAApprovalItems.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
           )
        });

        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        this.cm = new Wtf.grid.ColumnModel([
             new Wtf.grid.RowNumberer(),
            this.sm,
            {
                header: "ID",
                dataIndex: 'id',                
                hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.het.66"),
                sortable:true,
                dataIndex: 'billno',
                hidden:true,
                fixed:true
            },{
                header: WtfGlobal.getLocaleText("acc.field.OrderNoteNo"),
                sortable:true,
                dataIndex: 'billno'
            },{
                header: WtfGlobal.getLocaleText("acc.ven.name"),
                sortable:true,
                dataIndex: 'vendorname'
            },{
                header: WtfGlobal.getLocaleText("acc.inventoryList.date"),
                sortable:true,
                dataIndex: 'date',
                align:'center',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.prList.dueDate"),
                sortable:true,
                dataIndex: 'duedate',
                align:'center',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.field.ProductCode"),
                sortable:true,
                dataIndex: 'productcode'
            },{
                header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),
                sortable:true,
                dataIndex: 'productname'
            }
//            {
//                header: "Available Quantity",
//                dataIndex: 'availabelQuantity',
//                hidden:this.type == 1?false:true,
//                align:"right"
//            }
            ,{
                header: WtfGlobal.getLocaleText("acc.field.OrderedQuantity"),
                dataIndex: 'quantity',
                align:"right",
                sortable:true
            }
//            ,{
//                header: "Issued Quantity",
//                sortable:true,
//                align:"right",
//                dataIndex: 'nwquantity',
//                editor:this.type == 1?new Wtf.form.NumberField({
//                    scope: this,
//                    id:"issuedQuantity",
//                    allowBlank:false,
//                    allowNegative:false,
//                    listeners : {'focus': setZeroToBlank},
//                    validator : function(val){
//                        var re5digit=/^[0-9]*$/;
//                        if(val.search(re5digit) == -1){
//                           return false;
//                        }else{
//                            return true;
//                        }
//                    }
//                }):null                
//            },
//            {
//                header: "Delivered Quantity",
//                align:"right",
//                dataIndex: 'delquantity',
//                scope:this,
//                hidden:(this.type === 2 || this.type === 3)?false:true,
//                editor:this.type == 3?new Wtf.form.NumberField({
//                    scope: this,                    
//                    allowBlank:false,
//                    allowNegative:false,
//                    listeners : {'focus': setZeroToBlank},
//                    validator : function(val){
//                        var re5digit=/^[0-9]*$/;
//                        if(val.search(re5digit) == -1){
//                           return false;
//                        }else{                            
//                            return true;
//                        }
//                    }
//                }):null
//                ,renderer:this.type == 3?function(val, meta, rec, row, col, store){
//                   if(val <= rec.get('nwquantity') && rec.get('status') === "Ready For Collection")
//                        return val;
//                    else if (val < rec.get('nwquantity')){
//                        return val;
//                    }else if(val == 0){
//                        return '';
//                    }else if(val > rec.get('nwquantity')){
//                        return rec.get('nwquantity');                    
//                    }
//                }:function(val, meta, rec){
//                    if (rec.get('status') === "Issued"){
//                        return rec.get('nwquantity');
//                    }else
//                        return val;
//                }
//            },
//            {
//                header: "UoM",
//                sortable:true,
//                dataIndex: 'name'
//            }
            ,{
                header: WtfGlobal.getLocaleText("acc.invoiceList.status"),
                sortable:true,
                dataIndex: 'qastatus',
                renderer:function(value,meta,rec){
                    if(value==0){
                        value="Approved";
                    }else if(value==1){
                        value="Pending QA Approval";
                    }else if(value==2){
                        value="QA Rejected";
                    }
                    return value;
                }
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
                sortable:true,
                dataIndex: 'qastatusremark',
                renderer:function(value,meta,rec){
                    if(value!=""){
                        meta.attr = "Wtf:qtip='"+value+"' Wtf:qtitle='Remark' ";
                    }
                    return value;
                }
        }
    ]);
                   
        this.approveButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.cc.24"),
            scope: this,              
            tooltip: {
                title:WtfGlobal.getLocaleText("acc.cc.24"), 
                text:WtfGlobal.getLocaleText("acc.field.Approveselecteditems")
            },
            iconCls:"accountingbase approveBtn",
            hidden:(this.isPending)?false:true,
            handler:function(){
                this.acceptFunction("approve","",true);
            }
        });		
        
        this.remark="";
        
        this.rejectButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.field.Reject"),
            scope: this,
            tooltip: {
                title:WtfGlobal.getLocaleText("acc.field.Reject"), 
                text:WtfGlobal.getLocaleText("acc.field.Rejectselecteditems")
            },
            iconCls:"accountingbase rejectBtn",
            hidden:(this.isPending)?false:true,
            handler:function(){
                 this.remarkfunction("reject",false);
            }
        });
        

        var tbarArray = [];
        tbarArray.push("-",WtfGlobal.getLocaleText("acc.field.FromDate1"),this.startDate,"-",WtfGlobal.getLocaleText("acc.field.ToDate1"),this.endDate);
        tbarArray.push("-",this.search);
//        tbarArray.push("-",this.resetBtn);

//        this.exportButton = new Wtf.Button({
//            text:'Export',
//            iconCls:'pwnd exporticon',
//            tooltip: {title:"Export", text:"Export report in csv format"},
//            handler:function(){
//                 this.exportReport(50, "csv");
//            },
//            scope:this
//        });
        var bbarArray= new Array();
        
                if(this.isPending){
                    bbarArray.push("-",this.approveButton);
                    bbarArray.push("-",this.rejectButton);            
                }                

        this.grid=new Wtf.KwlEditorGridPanel({
            id:"QAApprovalItemsGridPanel"+this.id,
            cm:this.cm,
            store:this.ds,
            sm:this.sm,
            viewConfig: {
                forceFit: true,
                emptyText:WtfGlobal.getLocaleText("acc.field.NoRecordToDisplay")
            },
            view: grpView,
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchEmptyText:WtfGlobal.getLocaleText("acc.field.SearchbyOrderNoteNo"),
            searchField:"billno",
            clicksToEdit:1,
            tbar:tbarArray,
            bbar:bbarArray
        });

        var arrId = [];
        arrId.push("delete");
        this.ok=1;
        
//        this.grid.on("afteredit",function(obj){
//            var rec=obj.record;
//            var issuedQuantity=obj.originalValue;
//            if(this.type == 1 && rec.get('status') === "Pending QA Approval" && Wtf.realroles[0]==14 && isIncludeQAapprovalFlow){
//                if(obj.value <= issuedQuantity){
//                    return true;
//                }else if(obj.value == 0){
//                    return true;
//                }else if(obj.value > issuedQuantity){
//                    Wtf.Msg.show({
//                        title:'Info',
//                        msg: 'Entered quantity can not be greater than Issued quantity.',
//                        buttons: Wtf.Msg.OK,
//                        animEl: 'elId',
//                        icon: Wtf.MessageBox.INFO
//                    });
//                    rec.set("nwquantity",issuedQuantity);                    
//                }
//            }else if(obj.field === "delquantity" && this.type === 3 && rec.get('status') === "Ready For Collection"){
//                if(obj.value > issuedQuantity){
//                    Wtf.Msg.show({
//                        title:'Info',
//                        msg: 'Delivered quantity can not be greater than Issued quantity.',
//                        buttons: Wtf.Msg.OK,
//                        animEl: 'elId',
//                        icon: Wtf.MessageBox.INFO
//                    });
//                    rec.set("delquantity",issuedQuantity);
//                    return false;
//                }else
//                    return true;
//            } else
//                return true;
//        },this);
        
        this.grid.on("validateedit",this.validateeditFunction,this);
        this.grid.on("statesave",this.statesaveFunction,this);
//        this.ds.on("load",function(s,r){
//            this.editButton.disable();
//            this.deleteButton.disable();
//            this.processButton.disable();
//            this.quanarr = [];
//            var i;
//           for(i=0 ; i<s.getCount() ; i++){
//               this.quanarr.push(s.data.items[i].data.quantity);
//           }
//      
//        },this);
        this.add(this.grid);
        this.initloadgridstore();
        this.ds.on('load',function(){
            if(this.ds.data.length>0){
                this.approveButton.setDisabled(false);
                this.rejectButton.setDisabled(false);                
            }else{
                this.approveButton.setDisabled(true);
                this.rejectButton.setDisabled(true);
                this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.field.NoRecordToDisplay"));
                this.grid.getView().refresh();
            }            
        },this);        
    },
    add1:function(){
    },


    initloadgridstore:function(){
        this.ds.baseParams = {            
            isPending:this.isPending,
            startdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }
        this.ds.load({
                params:{
                    start:0,
                    limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize
                }
            });
    },
    loadgridstore:function(frm, to){

        this.ds.baseParams = {
            flag: 53,
            isPending:this.isPending,
            frmDate:frm,
            toDate:to,
            pendingStatusFlag:(this.type == 1 && Wtf.realroles[0]==14 && isIncludeQAapprovalFlow)?true:false,
            storeid:this.storeCmbfilter.getValue(),
            dmflag:this.dmflag,
            subcat: this.subcategoryCmb.getValue()
        }
        this.ds.load({
                params:{

                    start:0,
                    limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize
                }
            });
    },
    remarkfunction:function(type,isApproved){
        var selected = this.sm.getSelections();
        if(selected.length>0){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttorejectselecteditems"), function(btn){
                if(btn == 'yes') { 
                    this.addEditWin = new Wtf.Window({
                        title : WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
                        modal : true,
                        //                iconCls : 'iconwin',
                        minWidth:75,
                        width : 400,
                        height: 250,
                        resizable :false,
                        id:"rmrkwindow",
                        buttonAlign : 'right',
                        layout : 'border',
                        items :[{
                            region : 'north',
                            height : 75,
                            border : false,
                            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                            html : getTopHtml(WtfGlobal.getLocaleText("acc.field.Pleaseenterremark"),WtfGlobal.getLocaleText("acc.field.Fillfollowinginformation"),'images/createuser.png')/*upload52.gif')*/
                        },{
                            region : 'center',
                            border : false,
                            bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 20px 20px 20px;',
                            layout : 'fit',
                            items : [{
                                border : false,
                                bodyStyle : 'background:transparent;',
                                layout : "fit",
                                items : [
                                this.winForm = new Wtf.form.FormPanel({
                                    url: "jspfiles/inventory/pettyCash.jsp?flag=2&",
                                    waitMsgTarget: true,
                                    method : 'POST',
                                    border : false,
                                    bodyStyle : 'font-size:10px;',
                                    labelWidth : 110,
                                    items : [
                                    this.reqTypeName = new Wtf.form.TextArea({
                                        fieldLabel:WtfGlobal.getLocaleText("acc.field.EnterRemark"),
                                        name:'type',
                                        width:200,
                                        allowBlank:false,
                                        regex:Wtf.validateAddress,
                                        maxLength:200
                                    })
                                    ]
                                })
                                ]
                            }]
                        }],
                        buttons :[{
                            text : WtfGlobal.getLocaleText("acc.common.submit"),
                            //                    iconCls:'pwnd ReasonSubmiticon caltb',
                            scope : this,
                            handler: function(){     
                                var str=this.reqTypeName.getValue();
                                //                        str = str.replace("\'","");
                                //                        if(isRejectByQA!=undefined && isRejectByQA==true){  /*Keep issued quantity as it is if qa reject goods order*/
                                //                            for (var i=0;i<selected.length;i++){
                                //                                selected[i].data.nwquantity=selected[i].data.issuequantity;
                                //                            }
                                //                        }    
                                this.acceptItems(selected, str, type,isApproved);
                                
                                Wtf.getCmp('rmrkwindow').close();
                            }
                        },{
                            text : WtfGlobal.getLocaleText("acc.msgbox.cancel"),
                            scope : this,
                            //                    iconCls:'pwnd rejecticon caltb',
                            minWidth:75,
                            handler : function() {
                                Wtf.getCmp('rmrkwindow').close();
                            }
                        }]
                    }).show();
                }else if(btn == 'no') {
                    return;
                }
            },this);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.import.msg13")],0);            
        }
    },        
    acceptFunction:function(type,remark,isApproved){
        var finalremark = remark;
        var selected = this.sm.getSelections();

        if(selected.length>0){            
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.msgbox.CONFIRMTITLE"),WtfGlobal.getLocaleText("acc.field.Areyousureyouwantto")+type+WtfGlobal.getLocaleText("acc.field.selecteditems"), function(btn){
                if(btn == 'yes') {                    
                   this.acceptItems(selected, finalremark, type,isApproved);
                }else if(btn == 'no') {
                    return;
               }
            },this);
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.import.msg13")],0);            
        }
    },
    acceptItems : function(selected, finalremark, text,isApproved){
                    var jsondata = "[";
                    var sep="";
                    for (var i=0;i<selected.length;i++){                        
                        jsondata+="{'quantity':'"+selected[i].get("quantity") +"',";
                        jsondata+="'status':'"+selected[i].get("qastatus") +"',";
                        jsondata+="'id':'"+selected[i].get("id") +"'},";
                    }
                    var trmLen = jsondata.length - 1;
                    jsondata=jsondata.substr(0,trmLen)+"]";
                    var finalStr = jsondata;
                    this.sendGoodsRequest(text, finalStr,isApproved,finalremark);
    },
    
    sendGoodsRequest:function(text, finalStr,isApproved,remark){
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/updateQAApprovalItems.do",
            params: {
                flag: 54,
                isPending:this.isPending,
                qastatusremark:remark,
                isApproved:isApproved,
                jsondata:finalStr
            }
        },
        this,
        function(result) {
            if(result.success) {
                text = text;
                var msg="";
                if(text==="approve"){
                    msg = WtfGlobal.getLocaleText("acc.field.Selecteditemsare")+text+WtfGlobal.getLocaleText("acc.field.dbyQAsuccessfully");   
                }else{
                    msg = WtfGlobal.getLocaleText("acc.field.Selecteditemsare")+text+WtfGlobal.getLocaleText("acc.field.edbyQAsuccessfully");   
                }                
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), msg ], 0);
                this.ds.reload();
//                if(retstatus.printFlag){
//                    var flag = 104;
//                    var orderArray = eval('('+retstatus.processData+')');
//                    for(var count=0 ; count<orderArray.length ; count++){
//                        var str = "jspfiles/inventory/printOut.jsp?flag="+flag+"&data="+ encodeURIComponent(JSON.stringify(orderArray[count])) + "&type=" + type;
//                        window.open(str, "mywindow"+count,"menubar=1,resizable=1,scrollbars=1");
//                    }                                
//                }
            }else{                
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileprocessing")], 1);                      
            }

        }, function(){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileprocessing")], 1);                      
        });
    },
    statesaveFunction:function(){
    },
    validateeditFunction:function(e){
    }
//    exportReport: function(reportid, exportType){
//        var recordCnt = this.grid.store.getTotalCount();
//        if(recordCnt == 0)
//        {
//            msgBoxShow(["Error", "No records to export"], 0,1);
//            return;
//        }
//        var url="";
//        if(this.type==2 || this.type==3 || this.type==1){////fulfilled - Goods Pending - Store orders
//            url =  "ExportDataServlet.jsp?" +"mode=" + reportid +
//            "&reportname=" + this.title +
//            "&exporttype=" + exportType +
//            "&type=" + this.type +
//            "&frmDate=" + this.frmDate.getValue().format(Wtf.getDateFormat())+
//            "&toDate=" + this.toDate.getValue().format(Wtf.getDateFormat()) +
//            "&storeid=" +this.storeCmbfilter.getValue();
//        }else{
//            url =  "ExportDataServlet.jsp?" +"mode=" + reportid +
//            "&reportname=" + this.title +
//            "&exporttype=" + exportType +
//            "&type=" + this.type +
//            "&storeid=" +this.storeCmbfilter.getValue();
//   
//        }
//        setDldUrl(url);
//    }
});

function getReorderAnalysisMainTabViewDynamicLoad(){
    var reportPanel = Wtf.getCmp('reorderAnalysisMainTab');
    if(reportPanel == null){
        reportPanel = new Wtf.account.ReorderAnalysisReportView({
            id : 'reorderAnalysisMainTab',
            border : false,
            title: WtfGlobal.getLocaleText("acc.field.ReorderAnalysisReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.ReorderAnalysisReport"),
            layout: 'fit',
            iscustreport : true,
            closable : true,
            isCustomer:true,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //"Invoice",
            iconCls:'accountingbase invoicelist'
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}


Wtf.account.ReorderAnalysisReportView=function(config){
    this.businessPerson='Customer';
    Wtf.apply(this, config);
    
    this.GridRec = Wtf.data.Record.create ([
    {name:'productid'},
    {name:'productname'},
    {name:'desc'},
    {name:'pid'},
    {name:'vendor'},
    {name:'producttype'},
    {name:'type'},
    {name:'initialsalesprice'},
    {name:'stockprice'},
    {name:'warrantyperiod'},
    {name:'warrantyperiodsal'},
    {name:'uomid'},
    {name:'purchaseuom'},
    {name:'salesuom'},
    {name:'uomname'},
    {name:'purchaseuomname'},
    {name:'salesuomname'},
    {name:'stockpurchaseuomvalue'},
    {name:'stocksalesuomvalue'},
    {name:'parentuuid'},
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
    {name: 'leaf'},
    {name: 'warranty'},
    {name: 'syncable'},
    {name: 'qaenable'},
    {name: 'level'},
    {name: 'initialquantity',mapping:'initialquantity'},
    {name: 'initialprice'},
    {name: 'ccountinterval'},
    {name:'ccounttolerance'},
    {name:'productweight'},
    {name:'reorderdecision'},
    {name:'safetystock'},
    {name:'outstandingpoqty'},
    {name:'minqty'},
    {name:'avgqty'},
    {name:'ordercycleqty'},
    {name:'reorderqty'},
    {name:'partnumber'}
    ]);
    
    this.StoreUrl = "ACCProductCMN/getReorderAnalysisProducts.do";
    
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
   this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    this.Store = new Wtf.data.Store({
        url:this.StoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.GridRec)
    });
            
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.field.QuickSearchByProductDescription"),
        width: 200,
        field: 'productname',
        Store:this.Store
    })
    
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            searchField: this.quickPanelSearch,
            displayInfo: false,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
        });
        
    this.Store.on('datachanged', function() {
                    var p = this.pP.combo.value;
                    this.quickPanelSearch.setPage(p);
        }, this);
        
    this.Store.on('beforeload', function(){
        this.Store.baseParams = {
            ss : this.quickPanelSearch.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }                
    }, this);
    
    this.loaddata();
    
    
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        loadMask : true,
        id:"gridmsg"+config.id,         
        border:false,        
        bbar : this.pagingToolbar,
        layout:'fit',
        viewConfig:{forceFit:true,emptyText:WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.")},
        forceFit:true,
        columns:[new Wtf.grid.RowNumberer(),{
            hidden:true,
            dataIndex:'productid'
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridProductID"),
            dataIndex:'pid',
            width:100
        },{
            header:WtfGlobal.getLocaleText("acc.field.PartNumber"),
            dataIndex:'partnumber',
            width:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"),
            pdfwidth:80,
            dataIndex:'productname',            
            width:150
        },{
            header:WtfGlobal.getLocaleText("acc.product.purchaseUoMLabel"),
            pdfwidth:80,
            hidden:true,
            dataIndex:'purchaseuomname',
            width:80
        },{
            header:WtfGlobal.getLocaleText("acc.product.stockUoMLabel"),
            pdfwidth:70,
            dataIndex:'uomname',
            width:80
        },{
            header:WtfGlobal.getLocaleText("acc.field.StockOnHand"),
            pdfwidth:80,
            dataIndex:'quantity',
            align:'right',
            width:80
        },{
            header:WtfGlobal.getLocaleText("acc.field.OutstandingPOQty"),
            pdfwidth:80,
            dataIndex:'outstandingpoqty',
            align:'right',
            width:80
        },{
            header:WtfGlobal.getLocaleText("acc.field.MinQty"),
            pdfwidth:80,
            dataIndex:'minqty',
            align:'right',
            width:65            
        },{
            header:WtfGlobal.getLocaleText("acc.field.AveQty"),
            pdfwidth:80,
            dataIndex:'avgqty',
            align:'right',            
            width:65,
            renderer : function(value,metadata,record){
                return getRoundofValue(value).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
            }
        },{
            header:WtfGlobal.getLocaleText("acc.field.SafetyStockLevel"),
            pdfwidth:80,
            dataIndex:'safetystock',
            align:'right',
            width:80
        },{
            header:WtfGlobal.getLocaleText("acc.field.OrderCycleQty"),
            pdfwidth:80,
            dataIndex:'ordercycleqty',
            align:'right',
            width:80
        },{
            header:WtfGlobal.getLocaleText("acc.field.Re-OrderQty"),
            pdfwidth:80,
            dataIndex:'reorderqty',
            align:'right',
            width:80
        },        
        {
            header:WtfGlobal.getLocaleText("acc.field.Re-OrderDecision"),
            dataIndex:'reorderdecision',
            align:'center',
            width:100,
            pdfwidth:100,
            renderer : function(value,metadata,record){                
                var safetystock=record.data.safetystock;       
                var quantity=record.data.quantity;
                if(quantity<=safetystock){
                    return "YES";
                }else{
                    return "NO";
                }
            }  
        }
         ]
    });
        
    this.grid.on("render", function(grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
    },this);
    
//    this.resetBttn=new Wtf.Toolbar.Button({
//        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
//        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
//        id: 'btnRec' + this.id,
//        scope: this,
//        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
//        disabled :false
//    });
//    this.resetBttn.on('click',this.handleResetClickNew,this);
                
//    this.exportButton=new Wtf.exportButton({
//            obj:this,
//            id:"exportReports"+this.id,
//            text: WtfGlobal.getLocaleText("acc.common.export"),
//            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
//            disabled :true,
//            scope : this,
//            menuItem:{csv:true,pdf:true,rowPdf:false},
//            get:152
//    });
    
//    this.printButton=new Wtf.exportButton({
//                    obj:this,
//                    text:WtfGlobal.getLocaleText("acc.common.print"),
//                    tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),
//                    label:"Sales By " +(this.iscustreport? (this.isSalesPersonName ? 'sales person':'Customer') : 'Product'),
//                    menuItem:{print:true},
//                    get:152,
//                    params:{
//                             name: "Sales By " +(this.iscustreport? (this.isSalesPersonName ? 'sales person':'Customer') : 'Product') + " Report"
//                    }
//                });
                
    Wtf.apply(this,{
        border:false,
        layout : "fit",
         bodyStyle : "background-color:#ffffff;padding-right:10px;",
        items:[this.grid],
        tbar : [this.quickPanelSearch,'From',this.startDate,'To', this.endDate,'-',{
                text : WtfGlobal.getLocaleText("acc.common.fetch"),
                iconCls:'accountingbase fetch',
                scope : this,
                handler : this.loaddata
        },'-',this.resetBttn]
//,'-',this.exportButton, '-', this.printButton    
    });    
    Wtf.account.ReorderAnalysisReportView.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.ReorderAnalysisReportView,Wtf.Panel,{
  
  hideLoading:function(){
      Wtf.MessageBox.hide();
  },
  
  loaddata : function(){
        this.Store.load({
            params : {
                start:0,
                limit:30
                            
            }
        });
    },
    handleResetClick:function(){
        this.quickPanelSearch.reset();
        this.startDate.reset();
        this.endDate.reset();
        this.loaddata();
    },
    unitRenderer:function(value,metadata,record){
    	var unit=record.data['uomname'];
            value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;
        return value;
    }
    
//     handleResetClickNew:function(){ 
//
//           this.quickPanelSearch.reset();
//           this.startDate.setValue(WtfGlobal.getDates(true));
//           this.endDate.setValue(WtfGlobal.getDates(false));
////            if(this.isSalesPersonName) {
////               this.salesPersonName.setValue(this.userds.getAt(0).data.id);
////            } else if(this.iscustreport) {
////                this.Name.setValue(this.customerAccStore.getAt(0).data.accid);
////            } else if(!this.iscustreport) {
////                this.productname.setValue(this.productStore.getAt(0).data.productid);
////            }
//
//         this.Store.load({
//                params: {
//                    start:0,
//                    limit:this.pP.combo.value
//                }
//            });
//       
//    }
});
