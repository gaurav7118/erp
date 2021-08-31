/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.purchaseSalesReportMaster = function(config) {
    this.arr = [];
    Wtf.apply(this, config);
    this.isReport = config.isReport ? config.isReport : false;
    this.moduleid = Wtf.Acc_Sales_Report_Master_ModuleId;
    this.isproductcombo = false;
    this.iscustomercombo = false;
   
    /*------Function to create toolbar button ---------*/
    this.createTbar();
    
   
    /*----------Create record for Grid ------------ */
    this.createrecord();
    
    /*----------Create column model for Grid -------------------- */
    
    this.createcolumnmodel();
    
    /*------------Function to create Grid  -----------------*/
    this.creategrid();
   
    
    Wtf.account.purchaseSalesReportMaster.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.purchaseSalesReportMaster, Wtf.Panel, {
    
    
        onRender: function(config) {
       Wtf.account.purchaseSalesReportMaster.superclass.onRender.call(this, config);

      
         this.createPanel();
       
        this.pageLimit = new Wtf.forumpPageSize({
            ftree: this.grid
        });
   
        this.add(this.purchaseSalesReportMasterPanel);
        //this.fetchStatement();
        this.grid.on('render', function () {
            this.getMyConfig();
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.grid.on('statesave', this.saveMyStateHandler, this);
            }, this);
        }, this);
        
    },
    
    
        createPanel: function() {
        this.purchaseSalesReportMasterPanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.grid],
                    tbar: this.panelbtnarr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                       // searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id,
                            changePageSize: function (value) {
                                var pt = this.pagingToolbar;
                                if (this.combo.getValue() != "All") {
                                    value = parseInt(value) || parseInt(this.combo.getValue());
                                    value = (value > 0) ? value : 1;
                                    this.pagingToolbar.pageSize = value;
                                    this.pagingToolbar.doLoad(Math.floor(this.pagingToolbar.cursor / this.pagingToolbar.pageSize) * this.pagingToolbar.pageSize);
                                } else {
                                    this.pagingToolbar.pageSize = this.getRoundCount(pt.store.getTotalCount());
                                    if (this.pagingToolbar.pageSize <= 0) {
                                        this.pagingToolbar.pageSize = 5;    //handled when quick search and any other filter is applied.
                                    }
                                    this.pagingToolbar.doLoad(Math.floor(this.pagingToolbar.cursor / this.pagingToolbar.pageSize) * this.pagingToolbar.pageSize);
                                    var store = pt.store;
                                    store.fireEvent('datachanged', store);
                                    if (this.storeSortFlag) {
                                        store.remoteSort = false;
                                    }

                                    pt.first.setDisabled(true);
                                    pt.prev.setDisabled(true);
                                    pt.next.setDisabled(true);
                                    pt.last.setDisabled(true);
                                    pt.updateInfo();
                                }
                                if (!this.recordsLimit) {
                                    this.updateStore();
                                }
                                this.combo.collapse();
                            }
                        }),
                        items: [this.exportButton, this.printButton],
                        updateInfo : function(){
                            if(this.displayEl){
                                var count = this.store.getCount();
                                var msg = count == 0 ?
                                    this.emptyMsg :
                                    String.format(
                                        this.displayMsg,
                                        this.cursor+1, this.cursor+count-1, this.store.getTotalCount()
                                    );
                                this.displayEl.update(msg);
                            }
                        }
                    })
                }]
        });
    },
    
    
    
     createTbar:function(){
       
        this.btnArr = [];
        this.panelbtnarr = [];
        
        
//        this.panelbtnarr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
//            emptyText: WtfGlobal.getLocaleText("acc.salesmaster.searchtext"), 
//            width: 200,
//            id: "quickSearch" + this.id,
//            field: 'salesmastersearch'
//        }));
        
        
               
          this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        value:this.getDates(true)
    });
    
     this.panelbtnarr.push(WtfGlobal.getLocaleText("acc.common.from"));
        this.panelbtnarr.push(this.startDate,'-');
    
    
 
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
       value:this.getDates(false)
    });
    
       this.panelbtnarr.push(WtfGlobal.getLocaleText("acc.common.to"));
        this.panelbtnarr.push(this.endDate,'-');
        
        this.fetchBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"), // 'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetch"), // "Select a time period to view corresponding transactions.",
            style: "margin-left: 6px;",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.fetchStatement
        });
        
        this.panelbtnarr.push(this.fetchBttn);
        
 
        

        this.exportButton = new Wtf.exportButton({
            obj: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"),  
//          params: {name: WtfGlobal.getLocaleText("acc.prod.filename")},
            disabled :true,   
            menuItem: {csv: true, pdf: true, rowPdf: false, xls: true},
            get: Wtf.autoNum.Sales_Report_Master,
            filename: WtfGlobal.getLocaleText("acc.field.salesReport") + "_v1",
            text: WtfGlobal.getLocaleText("acc.common.export")
        });
        this.printButton = new Wtf.exportButton({
            obj: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.printTT"),  
//          params: {name: WtfGlobal.getLocaleText("acc.prod.filename")},
            disabled :true,
            menuItem: {print: true},
            get: Wtf.autoNum.Sales_Report_Master,
            filename: WtfGlobal.getLocaleText("acc.field.salesReport"),
            text:WtfGlobal.getLocaleText("acc.common.print"),
        });
       
             
        
        /*-----Drop down to show all Custom fields related to Product module----------*/
        
        this.productCustomFieldRec = Wtf.data.Record.create([
                {name: 'fieldname'},
                {name: 'fieldid'},
                {name:'columnnumber'},
                {name:'fieldtype'}
            
            ]);

        
        
        this.productCustomFieldStore = new Wtf.data.Store({
        url :"ACCAccountCMN/getCustomFieldAsPerModuleId.do",

        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
        },this.productCustomFieldRec)
    });
 
        
           this.productCustomFieldStore.load({
                params : {
                  moduleid : Wtf.Acc_Product_Master_ModuleId
                }
            });
        
        this.MSComboconfig = {
            store: this.productCustomFieldStore,
            valueField: 'fieldid',
            displayField: 'fieldname',
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: true,
            triggerAction: 'all',
            scope: this
        };
        
        
           this.productCustomFieldCombo = new Wtf.common.Select(Wtf.applyIf({
             multiSelect:true,
             fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,
             id:"productCustom"+this.heplmodeid+this.id ,
             forceSelection:true,
             emptyText:'Select Custom fields to be displayed as column',
            
             width:240,
             listeners:{
                'select': function(){
                    this.isproductcombo = true;
                },
                'unselect':function(){
                    this.iscustomercombo = true;
                },
                scope:this
            }
             
        },this.MSComboconfig));
            
   
         var cmbAccountlabel = WtfGlobal.getLocaleText("Product Custom Field(s)");
        this.panelbtnarr.push(cmbAccountlabel,this.productCustomFieldCombo,'-')
    
         this.productCustomFieldCombo.on("clearval",function(){
            
             for(var i=0; i<this.grid.getColumnModel().config.length;  i++) {
                 var data = this.grid.getColumnModel().config[i].dataIndex; 
                 if(data != "" && data.startsWith("Custom_") && data.endsWith("_product")) {
                     var record = WtfGlobal.searchRecord(this.productCustomFieldStore,this.grid.getColumnModel().config[i].fieldid,"fieldid");
                     var rowtaxindex = this.grid.getColumnModel().findColumnIndex("Custom_" + record.data.fieldname + "_product");
                     this.grid.getColumnModel().setHidden(rowtaxindex, true);
                  }
                
            }
            this.isproductcombo = true;
        },this);
    
    
    /*------Combo for Customer Custom field ------------ */
    
       this.customerCustomFieldRec = Wtf.data.Record.create([
                { name: 'fieldname'},
                {name: 'fieldid'},
                {name:'columnnumber'},
                {name:'fieldtype'}
            
            ]);

        
        
        this.customerCustomFieldStore = new Wtf.data.Store({
        url :"ACCAccountCMN/getCustomFieldAsPerModuleId.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: "totalCount"
        },this.customerCustomFieldRec)
    });
        
        
        this.customerCustomFieldStore.load({
            params: {
                moduleid: Wtf.Acc_Customer_ModuleId
            }
        });
        
        this.CustomerMSComboconfig = {
            store: this.customerCustomFieldStore,
            valueField: 'fieldid',
            displayField: 'fieldname',
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: true,
            triggerAction: 'all',
            scope: this
        };
        
        
           this.customerCustomFieldCombo = new Wtf.common.Select(Wtf.applyIf({
             multiSelect:true,
             fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,
             id:"customerCustom"+this.heplmodeid+this.id ,
             forceSelection:true,
             emptyText:'Select Custom fields to be displayed as column',
            
             width:240,
              listeners:{
                'select': function(){
                    this.iscustomercombo = true;
                },
                'unselect':function(){
                    this.iscustomercombo = true;
                },
                scope:this
            }
        },this.CustomerMSComboconfig));
            
   
       var customercmbAccountlabel = WtfGlobal.getLocaleText("Customer Custom Field(s)");
        this.panelbtnarr.push(customercmbAccountlabel,this.customerCustomFieldCombo)
        
         this.customerCustomFieldCombo.on("clearval",function(){
            
             for(var i=0; i<this.grid.getColumnModel().config.length;  i++) {
                 var data = this.grid.getColumnModel().config[i].dataIndex; 
                 if(data != "" && data.startsWith("Custom_") && data.endsWith("_customer")) {
                     var record = WtfGlobal.searchRecord(this.customerCustomFieldStore,this.grid.getColumnModel().config[i].fieldid,"fieldid");//this.customerCustomFieldStore.getAt(index);
                     var rowtaxindex = this.grid.getColumnModel().findColumnIndex("Custom_" + record.data.fieldname + "_customer");
                     this.grid.getColumnModel().setHidden(rowtaxindex, true);
                  }
                
            }
            this.iscustomercombo = true;
        },this);
    
    
    
    },
    
    createrecord: function() {


        this.purchaseSalesReportMasterRec = new Wtf.data.Record.create([{
            name :'documenttype'   
            }, {
                name: 'documentno'
            }, {
                name: 'billid'
            }, {
                name: 'documentdate'
//                type: 'date'
            }, {
                name: 'invoicedono'
            }, {
                name: 'doid'
            }, {
                name: 'invoicedodate',
                type:'date'
            }, {
                name: 'invoicesono'
            }, {
                name: 'soid'
            }, {
                name: 'invoicesodate',
                type:'date'
            }, {
                name: 'linkedid'
            }, {
                name: 'linkedno'
            }, {
                name: 'linkeddate'
//                type:'date'
            }, {
//                name: 'cnid'
//            }, {
//                name: 'cnno'
//            }, {
//                name: 'cndate',
//                type:'date'
//            }, {
                name: 'shippingterms'
            }, {
                name: 'customercode'
            }, {
                name: 'customername'
            }, {
                name: 'customeraliasname'
            }, {
                name: 'customercategory'
            }, {
                name: 'customerindustry'
            }, {
                name: 'customercurrency'
            }, {
                name: 'salesmancode'
            }, {
                name: 'productid'
            }, {
                name: 'pid'
            }, {
                name: 'productname'
            }, {
                name: 'productdescription'
            }, {
                name: 'productuom'
            }, {
                name: 'producvendor'
            }, {
                name: 'productbatch'
            }, {
                name: 'productserial'
            }, {
                name: 'invoiceamount'
            }, {
                name: 'invoiceamountinbase'
            }, {
                name: 'invoicesalesqty'
            }, {
                name: 'salesrevenueinbasecurrency'
            }, {
                name: 'salesrevenue'
            }, {
                name: 'invoiceproductcost'
            }, {
                name: 'unitsellingprice'
            }, {
                name: 'invoiceproductcost'
            }, {
                name: 'invoicesalesmargin'
            }, {
                name: 'invoiceproductbatch'
            }, {
                name: 'salesgl'
            },{
                name:'currencysymboltransaction'
            },{
                name:'excludinggstamount'
            },{
                name:'excludinggstamountinbase'
            },{
                name:'groupcol',
                type:'string'
            }, {
                name: 'totalinvoiceproductcost'
            }]);
    },
    
    createcolumnmodel: function() {

        this.gridColumnModelArray = [];

        this.gridColumnModelArray.push(new Wtf.grid.RowNumberer(), {
            hidden: true,
            header: "",
            dataIndex: 'billid'
        }, {
            header: "Document Type",
            dataIndex: 'documenttype',
            width:100,
            sortable: true,
            pdfwidth: 50,
            align: 'left'
        }, {
            header: "Document No",
            dataIndex: 'documentno',
            width:80,
            sortable: true,
            pdfwidth: 50,
            align: 'left',
            renderer: function(val, m, rec) {
                val = val.replace(/(<([^>]+)>)/ig, "");
                if (rec.data.deleted) {
                    val = '<del>' + val + '</del>';
                }
                if(rec.data.documenttype === "Invoice"){
                    return "<a class='jumplink' wtf:qtip='" + val + "' href='#'  onClick='javascript:viewSIDocumentTab({billid:\""+rec.data.billid+"\"})'>" + val + "</a>";
                } else{
                    return "<a class='jumplink' wtf:qtip='" + val + "' href='#'  onClick='javascript:viewCNDocumentTab({noteid:\""+rec.data.billid+"\"})'>" + val + "</a>";
                }
            }
            //summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}
        }, {
            header: 'Document Date',
            align: 'center',
            dataIndex: 'documentdate',
            sortable: true,
//            renderer: WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:50
        } ,{
            header: WtfGlobal.getLocaleText("acc.invoiceList.totAmt"), 
            dataIndex: 'invoiceamount',
                
            pdfwidth: 50,
            align: 'right',
            renderer: WtfGlobal.withoutRateCurrencySymbolTransaction
        }, {
            header: WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome") + " ("+WtfGlobal.getCurrencyName()+")", 
            dataIndex: 'invoiceamountinbase',
                  
            pdfwidth: 50,
            align: 'right',
            renderer :  WtfGlobal.withoutRateCurrencySymbol,
        }, {
            header: WtfGlobal.getLocaleText("acc.reportGrid.header.totalAmountBeforeGST"), 
            dataIndex: 'excludinggstamount',
            pdfwidth: 50,
            align: 'right',
            renderer :  WtfGlobal.withoutRateCurrencySymbolTransaction
        }, {
            header: WtfGlobal.getLocaleText("acc.reportGrid.header.totalAmountInBaseBeforeGST") + " ("+WtfGlobal.getCurrencyName()+")", 
            dataIndex: 'excludinggstamountinbase',
            pdfwidth: 50,
            align: 'right',
            renderer :  WtfGlobal.withoutRateCurrencySymbol,
//        },{
//            header: WtfGlobal.getLocaleText("Sales D/O No."),
//            dataIndex: 'invoicedono',
//            pdfwidth: 50,
//            align: 'left',
//            renderer: function(val, m, rec) {
//                var doid = rec.data.doid.split(",");
//                var valArr = val.split(",");
//                var link = "";
//                for(var ind = 0; ind < doid.length; ind++){
//                    valArr[ind] = valArr[ind].replace(/(<([^>]+)>)/ig, "");
//                if (rec.data.deleted) {
//                        valArr[ind] = '<del>' + valArr[ind] + '</del>';
//                }
//                    link += "<a class='jumplink' wtf:qtip='" + valArr[ind] + "' href='#'  onClick='javascript:viewDODocumentTab({billid:\""+doid[ind]+"\"})'>" + valArr[ind] + "</a>";
//            }
//                return link;
////                return "<a class='jumplink' wtf:qtip='" + val + "' href='#'  onClick='javascript:viewDODocumentTab({billid:\""+rec.data.doid+"\"})'>" + val + "</a>";
//            }
//        }, {
//            header: 'Sales D/O Date',
//            align: 'center',
//            dataIndex: 'invoicedodate',
//            width: 100,
//            renderer: WtfGlobal.onlyDateDeletedRenderer

        }, {
            header: WtfGlobal.getLocaleText("Linked DO/SR/CN No"),
            dataIndex: 'linkedno',
            pdfwidth: 100,
            width: 200,
            align: 'left',
            renderer: function(val, m, rec) {
                var link = "";
                if(rec.data.documenttype === "Invoice"){
                    var linkedid = rec.data.linkedid.split("!##");
                    var linkedno = val.split("!##");
                    for(var cnt = 0; cnt < linkedid.length; cnt++){
                        var subLinkedId = linkedid[cnt].split(",");
                        var valArr = linkedno[cnt].split(",");
                        var doLink = "";
                        var srLink = "";
                        var cnLink = "";
                        for(var ind = 0; ind < subLinkedId.length; ind++){
                            if(subLinkedId[ind] != "null"){
                                valArr[ind] = valArr[ind].replace(/(<([^>]+)>)/ig, "");
                                if (rec.data.deleted) {
                                    valArr[ind] = '<del>' + valArr[ind] + '</del>';
                                }
                                if(cnt == 0){
                                    doLink += "<a class='jumplink' style='margin : -2px 5px; float : none;' wtf:qtip='" + valArr[ind] + "' href='#'  onClick='javascript:viewDODocumentTab({billid:\""+subLinkedId[ind]+"\"})'>" + valArr[ind] + "</a>";
                                } else if (cnt == 1){
                                    srLink += "<a class='jumplink' style='margin : -2px 5px; float : none;' wtf:qtip='" + valArr[ind] + "' href='#'  onClick='javascript:viewSRDocumentTab({billid:\""+subLinkedId[ind]+"\"})'>" + valArr[ind] + "</a>";
                                } else if(cnt == 2){
                                    cnLink += "<a class='jumplink' style='margin : -2px 5px; float : none;' wtf:qtip='" + valArr[ind] + "' href='#'  onClick='javascript:viewCNDocumentTab({noteid:\""+subLinkedId[ind]+"\"})'>" + valArr[ind] + "</a>";
                                }
                                if(doLink != "" && ind + 1 < subLinkedId.length){
                                    doLink += ",";
                                }
                                if(srLink != "" && ind + 1 < subLinkedId.length){
                                    srLink += ",";
                                }
                                if(cnLink != "" && ind + 1 < subLinkedId.length){
                                    cnLink += ",";
                                }
                            }
                        }
                        link += doLink != "" ? doLink + "/" : "";
                        link += srLink != "" ? srLink + "/" : "";
                        link += cnLink != "" ? cnLink + "" : "";
                    }
                    link = link.endsWith("/") ? link.substr(0, link.length - 1) : link;
                } else{
                    link += "<a class='jumplink' style='margin : -2px 5px; float : none;' wtf:qtip='" + val + "' href='#'  onClick='javascript:viewSRDocumentTab({billid:\""+rec.data.linkedid+"\"})'>" + val + "</a>";
                }
                return link;
            }
        }, {
            header: WtfGlobal.getLocaleText("Linked DO/SR/CN Date"),
            align: 'center',
            dataIndex: 'linkeddate',
            width: 100,
            pdfwidth: 100
//            renderer: WtfGlobal.onlyDateDeletedRenderer
        }, 
//        {
//            header: "Sales S/O No.",
//            dataIndex: 'invoicesono',
//            width: 100,
//            pdfwidth: 50,
//            align: 'left',
//            renderer: function(val, m, rec) {
//                val = val.replace(/(<([^>]+)>)/ig, "");
//                if (rec.data.deleted) {
//                    val = '<del>' + val + '</del>';
//                }
//                return "<a class='jumplink' wtf:qtip='" + val + "' href='#'  onClick='javascript:viewSODocumentTab({billid:\""+rec.data.soid+"\"})'>" + val + "</a>";
//            }
//        }, 
//        {
//            header: 'Sales S/O Date',
//            align: 'center',
//            dataIndex: 'invoicesodate',
//            width: 100,
//            renderer: WtfGlobal.onlyDateDeletedRenderer
//        }, 
        {
            header: "Customer Code",
            dataIndex: 'customercode',
            width: 100,
         
            pdfwidth: 50,
            align: 'left',
        }, {
            header: "Customer Name",
            dataIndex: 'customername',
            width: 100,
            sortable: true,
            pdfwidth: 50,
            align: 'left',
        }, {
            header: "Customer Alias Name",
            dataIndex: 'customeraliasname',
            width: 100,
          
            pdfwidth: 50,
            align: 'left',
        }, {
            header: "Customer Category",
            dataIndex: 'customercategory',
            width: 100,
          
            pdfwidth: 50,
            align: 'left',
        }, {
            header: "Customer Currency",
            dataIndex: 'customercurrency',
            width: 100,
           
            pdfwidth: 50,
            align: 'left',
        }, {
            header: "Salesman Code",
            dataIndex: 'salesmancode',
            width: 100,
            
            pdfwidth: 50,
            align: 'left',
        }, {
            header: WtfGlobal.getLocaleText("acc.productList.gridProductID"),
            dataIndex: 'productid',
            align: 'left',
            pdfwidth: 50,
           
            renderer: function(val, m, rec) {   // ERP-13247 [SJ]
                val = val.replace(/(<([^>]+)>)/ig, "");
                if (rec.data.deleted) {
                    val = '<del>' + val + '</del>';
                }
                return "<a class='jumplink' wtf:qtip='" + val + "' href='#'  onClick='javascript:Wtf.onCellClickProductDetails(\""+rec.data.pid+"\","+this.isFixedAsset+")'>" + val + "</a>";
            }
        }, {
            header: "Product Name",
            dataIndex: 'productname',
            align: 'left',
            pdfwidth: 50,
          
        }, {
            header: WtfGlobal.getLocaleText("acc.productList.gridProductDescription"), // Product Description",
            dataIndex: 'productdescription',
            align:'left',
           
            renderer: function(val, m, rec) {
                if (rec.data.deleted)
                    val = '<del>' + val + '</del>';
                return "<div wtf:qtip='" + val + "' wtf:qtitle='" + WtfGlobal.getLocaleText("acc.productList.gridProductDescription") + "'>" + val + "</div>";
            },
            pdfwidth: 50,
        }, {
            header: WtfGlobal.getLocaleText("acc.masterConfig.uom"),
            dataIndex: 'productuom',
            hidden: true,
            pdfwidth: 50,
            align:'left',
            renderer: WtfGlobal.deletedRenderer
        },{
            header: "Product Batch Name",
            dataIndex: 'productbatch',
            align: 'left',
            pdfwidth: 50,
            
        }, {
            header: "Product Serial No",
            dataIndex: 'productserial',
            align: 'left',
            pdfwidth: 50,
           
        },{
            header: "Invoice Sales Qty",
            dataIndex: 'invoicesalesqty',
            align: 'right',
            renderer: this.unitRenderer,
            pdfwidth: 50,
        }, {
            header: "Invoice Sales Revenue in Base Currency", // "Initial Sales Price",
            align: 'right',
            hidden: true,
            dataIndex: 'salesrevenueinbasecurrency',
            pdfwidth: 50,
//            pdfrenderer: 'unitpricecurrency',
            renderer: function(v, metadata, record) {
                if (!Wtf.dispalyUnitPriceAmountInSales) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    return WtfGlobal.withoutRateCurrencySymbol(v, false, record);
                }
            }
        }, {
            header: "Invoice Sales Revenue in Source Currency", // "Initial Sales Price",
            align: 'right',
            hidden: true,
            dataIndex: 'salesrevenue',
            pdfwidth: 50,
//            pdfrenderer: 'unitpricecurrency',
            renderer: function(v, metadata, record) {
                if (!Wtf.dispalyUnitPriceAmountInSales) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    return WtfGlobal.withoutRateCurrencySymbolTransaction(v, false, record);
                }
            }
        }, {
            header: "Unit Selling Price", // "Unit Selling price",
            align: 'right',
            dataIndex: 'unitsellingprice',
            pdfwidth: 50,
//            pdfrenderer: 'unitpricecurrency',
            renderer: function(v, metadata, record) {
                if (!Wtf.dispalyUnitPriceAmountInSales) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    return WtfGlobal.withoutRateCurrencySymbolTransaction(v, false, record);
                }
            }
        }, {
            header: "Invoice Product Unit Cost", // "Initial Sales Price",
            align: 'right',
            dataIndex: 'invoiceproductcost',
            pdfwidth: 50,
//            pdfrenderer: 'unitpricecurrency',
            renderer: function(v, metadata, record) {
                if (!Wtf.dispalyUnitPriceAmountInSales) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    return WtfGlobal.withoutRateCurrencySymbol(v, false, record);
                }
            }
        }, {
            header: "Total Invoice Product Cost",
            align: 'right',
            dataIndex: 'totalinvoiceproductcost',
            pdfwidth: 50,
            renderer: function(v, metadata, record) {
                if (!Wtf.dispalyUnitPriceAmountInSales) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else if (record.data['type'] == "Inventory Non-Sale") {
                    return "N/A";
                } else {
                    return WtfGlobal.withoutRateCurrencySymbol(v, false, record);
                }
            }
        },
        {
            header: "Sales GL",
            dataIndex: 'salesgl',
            align: 'left',
            pdfwidth: 50,
        }, {
            header: "",
            dataIndex: 'groupcol',
            align: 'left',
            pdfwidth: 50,
            hidden: true,
            hideable: false
        })
    },
    
        creategrid: function() {
            
        this.msgLmt = 30;

        this.Store = new Wtf.data.GroupingStore({
        url: "ACCInvoiceCMN/getSalesReportMasterData.do",
        groupField :"groupcol",
        sortInfo :{
            field : 'groupcol',
            direction : 'ASC'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.purchaseSalesReportMasterRec)
    });
       


        this.sm = new Wtf.grid.CheckboxSelectionModel({
//            singleSelect: true
        });
        this.sm.on('selectionchange',this.enableDisableButtons,this);
       
//        this.summary = new Wtf.grid.GroupSummary({});
         
        var productColumnArray = GlobalColumnModelForReports[Wtf.Acc_Product_Master_ModuleId] != undefined ? JSON.clone(GlobalColumnModelForReports[Wtf.Acc_Product_Master_ModuleId]) : [];
        for(var cnt = 0;cnt < productColumnArray.length;cnt++){
            productColumnArray[cnt].fieldname = productColumnArray[cnt].fieldname + "_product";
        }
        var customerColumnArray = GlobalColumnModelForReports[Wtf.Acc_Customer_ModuleId] != undefined ? JSON.clone(GlobalColumnModelForReports[Wtf.Acc_Customer_ModuleId]) : [];
        for(var cnt = 0;cnt < customerColumnArray.length;cnt++){
            customerColumnArray[cnt].fieldname = customerColumnArray[cnt].fieldname + "_customer";
        }
        
        this.gridColumnModelArray = WtfGlobal.appendCustomColumn(this.gridColumnModelArray,productColumnArray,true,undefined,undefined,undefined,undefined,undefined,true);  
        this.gridColumnModelArray = WtfGlobal.appendCustomColumn(this.gridColumnModelArray,customerColumnArray,true,undefined,undefined,undefined,undefined,undefined,true);    
//        this.gridColumnModelArray = WtfGlobal.appendCustomColumn(this.gridColumnModelArray,GlobalColumnModelForReports[Wtf.Acc_Product_Master_ModuleId],true,undefined,undefined,undefined,undefined,undefined,true);  
//        this.gridColumnModelArray = WtfGlobal.appendCustomColumn(this.gridColumnModelArray,GlobalColumnModelForReports[Wtf.Acc_Customer_ModuleId],true,undefined,undefined,undefined,undefined,undefined,true);    
          
        this.grid = new Wtf.grid.GridPanel({
            store: this.Store,
            loadMask: true,
//            id: "gridmsg" + this.id,
            border: false,
//            plugins: [this.summary],
            layout: 'fit',
            trackMouseOver: true,
            columns: this.gridColumnModelArray,
            view : new Wtf.grid.CustomGroupingView({
                forceFit : false,
                enableGroupingMenu : false,
                hideGroupedColumn : true,
                emptyText : WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }),
            forceFit: false
        });
            
        WtfGlobal.updateStoreConfig(productColumnArray, this.Store);
        WtfGlobal.updateStoreConfig(customerColumnArray, this.Store);
        
        /*------Need to check this code requirement ---------*/
       // this.grid.on('cellclick',this.afteredit,this);
       this.Store.on('beforeload', this.handleStoreBeforeLoad, this);
       this.Store.on('load', this.handleStoreOnLoad, this);
       this.Store.on('loadexception', function(store) {
         WtfGlobal.resetAjaxTimeOut(); 
        this.grid.getView().refresh();
    }, this);
       this.grid.on('afterlayout', this.handleAfterLayoutOfGrid, this);

  
    },
    
   
    /*
     * 
     * Moved the code of fetchStatement function to handleStoreBeforeLoad as it is need to call from multiple places
     */
       fetchStatement: function() {
        this.Store.load({
            params: {
               start: 0,
               limit: (this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
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
    
    handleStoreBeforeLoad :  function(){
        WtfGlobal.setAjaxTimeOut();
        var productCustomFieldArray= new Array();
        var productCustomDataIndexArray= new Array();
        var customerCustomFieldArray= new Array();
        var customerCustomDataIndexArray= new Array();
        var productCustomDataTypeArray= new Array();
        var customerCustomDataTypeArray= new Array();
        var i = 0;
        var index;
        var productbillids=[];
        var customerbillids=[];
        var customerCustomDataTypeArray= new Array();
        for(i=0; i<this.grid.getColumnModel().config.length;  i++) {
            var data = this.grid.getColumnModel().config[i].dataIndex;
            if(data != "" && data.startsWith("Custom_") && data.endsWith("_product") && !this.isproductcombo) {
                if(this.productCustomFieldCombo.valueArray.length <= 0 && !this.grid.getColumnModel().config[i].hidden) {
                    index = this.productCustomFieldStore.find("fieldid", this.grid.getColumnModel().config[i].fieldid);
                    //this.productCustomFieldCombo.setValue(this.grid.getColumnModel().config[i].fieldid);
                    productbillids.push(this.grid.getColumnModel().config[i].fieldid);
                    var record = this.productCustomFieldStore.getAt(index);
                    productCustomFieldArray.push(record.data.columnnumber);
                    productCustomDataIndexArray.push("Custom_" + record.data.fieldname);
                    productCustomDataTypeArray.push(record.data.fieldtype);
                    var rowtaxindex = this.grid.getColumnModel().findColumnIndex("Custom_" + record.data.fieldname + "_product");
                    this.grid.getColumnModel().setHidden(rowtaxindex, false);
                }
            } else if(data != "" && data.startsWith("Custom_") && data.endsWith("_customer") && !this.iscustomercombo) { 
                if (this.productCustomFieldCombo.valueArray.length <= 0  && !this.grid.getColumnModel().config[i].hidden) {
                    index = this.customerCustomFieldStore.find("fieldid", this.grid.getColumnModel().config[i].fieldid);
                    //this.customerCustomFieldCombo.setValue(this.grid.getColumnModel().config[i].fieldid);
                    customerbillids.push(this.grid.getColumnModel().config[i].fieldid);
                    var record = this.customerCustomFieldStore.getAt(index);
                    customerCustomFieldArray.push(record.data.columnnumber);
                    customerCustomDataIndexArray.push("Custom_" + record.data.fieldname);
                    customerCustomDataTypeArray.push(record.data.fieldtype);
                    var rowtaxindex = this.grid.getColumnModel().findColumnIndex("Custom_" + record.data.fieldname + "_customer");
                    this.grid.getColumnModel().setHidden(rowtaxindex, false);
                }
            }
        }
        if(this.productCustomFieldCombo.valueArray.length <= 0 && !this.isproductcombo) {
            this.productCustomFieldCombo.setValue(productbillids);
        }
        if(this.customerCustomFieldCombo.valueArray.length <= 0 && !this.iscustomercombo) {
            this.customerCustomFieldCombo.setValue(customerbillids);
        }
        
        if (this.productCustomFieldCombo.valueArray.length > 0) {

            for (i = 0; i < this.productCustomFieldCombo.valueArray.length; i++) {

                index = this.productCustomFieldStore.find("fieldid", this.productCustomFieldCombo.valueArray[i]);
                var record = this.productCustomFieldStore.getAt(index);
                 productCustomFieldArray.push(record.data.columnnumber);
                 productCustomDataIndexArray.push("Custom_"+record.data.fieldname);
                 productCustomDataTypeArray.push(record.data.fieldtype);
                 
                 var rowtaxindex = this.grid.getColumnModel().findColumnIndex("Custom_" + record.data.fieldname + "_product");
                 this.grid.getColumnModel().setHidden(rowtaxindex, false);
            }

        }
        
        
        if (this.customerCustomFieldCombo.valueArray.length > 0) {

            for (i = 0; i < this.customerCustomFieldCombo.valueArray.length; i++) {

                index = this.customerCustomFieldStore.find("fieldid", this.customerCustomFieldCombo.valueArray[i]);
                var record = this.customerCustomFieldStore.getAt(index);
                customerCustomFieldArray.push(record.data.columnnumber);
                customerCustomDataIndexArray.push("Custom_" + record.data.fieldname);
                customerCustomDataTypeArray.push(record.data.fieldtype);
                
                var rowtaxindex = this.grid.getColumnModel().findColumnIndex("Custom_" + record.data.fieldname + "_customer");
                this.grid.getColumnModel().setHidden(rowtaxindex, false);
                
            }

        } 
        /*
         * The below parameters are sent now in both the cases i.e. on fetch button and on paging
         */
        this.Store.baseParams = {
            startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()), 
            enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
               productCustomFieldArray:JSON.stringify(productCustomFieldArray),
               productCustomDataIndexArray:JSON.stringify(productCustomDataIndexArray),
               productCustomDataTypeArray :JSON.stringify(productCustomDataTypeArray),
               customerCustomFieldArray:JSON.stringify(customerCustomFieldArray),
               customerCustomDataIndexArray:JSON.stringify(customerCustomDataIndexArray),
               customerCustomDataTypeArray :JSON.stringify(customerCustomDataTypeArray)
            }
    },
    
    handleAfterLayoutOfGrid: function() {

        /*
         * Applying Empty Text Required.
         */
        this.grid.getView().applyEmptyText();
    },
   
    handleStoreOnLoad: function() {
        WtfGlobal.resetAjaxTimeOut(); 
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        if(this.Store.getCount()==0){
            if(this.exportButton){
                this.exportButton.disable();
            }
            if(this.printButton){
                this.printButton.disable();
            }
        }else{
            if(this.exportButton){
                this.exportButton.enable();
            }
            if(this.printButton){
                this.printButton.enable();
            }
        }

    },
    getMyConfig: function () {
        WtfGlobal.getGridConfig (this.grid, this.moduleid, false, false);
    },
   
    saveMyStateHandler: function (grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    }
   
});