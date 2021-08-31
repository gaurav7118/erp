Wtf.linkReport = function(conf){
    Wtf.apply(this, conf);
    Wtf.linkReport.superclass.constructor.call(this, conf);
    this.moduleid=conf.moduleid,
    this.transactionno=conf.transactionno
}

Wtf.extend(Wtf.linkReport, Wtf.Panel, {
    closable:true,
    border:false,
    iconCls:'writtenOffSalesInvoiceIcon',
    layout:'border',
    initComponent:function(){
        this.title = (this.module && this.module == 1)?Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.link.data.sales.report"),Wtf.TAB_TITLE_LENGTH) :Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.link.data.purchase.report"),Wtf.TAB_TITLE_LENGTH);
        this.tabTip = (this.module && this.module == 1)?WtfGlobal.getLocaleText("acc.link.data.sales.report"):WtfGlobal.getLocaleText("acc.link.data.purchase.report");
        Wtf.linkReport.superclass.initComponent.call(this);
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'startdatelinkreport',
            id:'startdatelinkreport'+this.id,
            format:WtfGlobal.getOnlyDateFormat()
        });

        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddatelinkreport',
            id:'enddatelinkreport'+this.id
        });
        this.searchBttn = new Wtf.Button({ 
            text: WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            scope: this,
            id:"searchlinkreport"+this.id,
            name:"searchlinkreport",
            iconCls:'accountingbase fetch',
            handler:function(){
                this.sdate=this.startDate.getValue();
                this.edate=this.endDate.getValue();
                if(this.sdate > this.edate){
                    WtfComMsgBox(1,2);
                    return false;
                }
                this.loadData();
            }
        });
        /*
         * Hard coded header because of custom html report
         */
        var headerS="po,cq,so,do,ci,rp,dn,gl";
        var headerP="so,pr,rfq,vq,po,grn,pi,pay,cn,gl";
        var titleS="Purchase Order,Customer Quotation,Sales Order,Delivery Order,CS/Invoice/Return,Receipt/CN,DN,GL";
        var titleP="Sales Order,Purchase Requisition,RFQ,Vendor Quotation,Purchase Order,GRN,CP/Invoice/Return,Payment/DN,CN,GL";
        var widthS="150,150,150,150,150,150,150,150";
        var widthP="150,150,150,150,150,150,150,150,150,150";
        var alignS="none,none,none,none,none,none,none,none";
        var alignP="none,none,none,none,none,none,none,none,none,none";
        var iscustomer=this.id == "LinkSales" ? true : false;
        this.exportButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.report.annexure2A.Exportinxls"),
            tooltip: WtfGlobal.getLocaleText("acc.agedPay.exportTT"),
            scope: this,
            id:"searchlinkreport"+this.id,
            name:"searchlinkreport",
            iconCls:'pwnd '+'exportcsv',
                  handler: function(){
                      if(this.id == "LinkSales"){
                    var otherParameter = "startdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                            +"&filetype=xls&filename=Linking Information&isCustomer="+iscustomer+"&header="+headerS+"&title="+titleS+"&width="+widthS
                    +"&align="+alignS+"&documentType="+this.documentType.getValue()+"&ss="+this.quickPanelSearch.getValue();
                    var url = "ACCLinkData/exportMonthWiseLinkingSalesReport.do"
                    Wtf.get('downloadframe').dom.src = url+"?"+otherParameter; 
                      }else{
                    var otherParameter = "startdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                            +"&filetype=xls&filename=Linking Information&isCustomer="+iscustomer+"&header="+headerP+"&title="+titleP+"&width="+widthP
                    +"&align="+alignP+"&documentType="+this.documentType.getValue()+"&ss="+this.quickPanelSearch.getValue();
                    var url = "ACCLinkData/exportMonthWiseLinkingPurchaseReport.do"
                    Wtf.get('downloadframe').dom.src = url+"?"+otherParameter; 
                          
                          
            }
            }
        });

        var documentTypeStoreRec = new Array();
        if (this.id == "LinkSales") {
            documentTypeStoreRec.push([Wtf.Acc_Sales_Order_ModuleId, 'Sales Order']);
            documentTypeStoreRec.push([Wtf.Acc_Invoice_ModuleId, 'Sales Invoice']);
            documentTypeStoreRec.push([Wtf.Acc_Delivery_Order_ModuleId, 'Delivery Order']);
            documentTypeStoreRec.push([Wtf.Acc_Customer_Quotation_ModuleId, 'Customer Quotation']);
            documentTypeStoreRec.push([Wtf.Acc_Sales_Return_ModuleId, 'Sales Return']);
            documentTypeStoreRec.push([Wtf.Acc_Credit_Note_ModuleId, 'Credit Note']);
            documentTypeStoreRec.push([Wtf.Acc_Receive_Payment_ModuleId, 'Receive Payment']);

        } else {
            documentTypeStoreRec.push([Wtf.Acc_Purchase_Order_ModuleId, 'Purchase Order']);
            documentTypeStoreRec.push([Wtf.Acc_Vendor_Invoice_ModuleId, 'Purchase Invoice']);
            documentTypeStoreRec.push([Wtf.Acc_Purchase_Requisition_ModuleId, 'Purchase Requisition']);
            documentTypeStoreRec.push([Wtf.Acc_RFQ_ModuleId, 'RFQ']);
            documentTypeStoreRec.push([Wtf.Acc_Vendor_Quotation_ModuleId, 'Vendor Quotation']);
            documentTypeStoreRec.push([Wtf.Acc_Purchase_Return_ModuleId, 'Purchase Return']);
            documentTypeStoreRec.push([Wtf.Acc_Debit_Note_ModuleId, 'Debit Note']);
            documentTypeStoreRec.push([Wtf.Acc_Make_Payment_ModuleId, 'Make Payment']);
            documentTypeStoreRec.push([Wtf.Acc_Goods_Receipt_ModuleId, 'Goods Receipt']);
        } 
        
        
        
        this.documentTypeStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'id'
                }, {
                    name: 'name'
                }],
            data: documentTypeStoreRec
        });
        
       this.documentType= new Wtf.form.ComboBox({
            triggerAction:'all',
            name:"documnetTypeCombo",
            hideLabel:false,
            hidden:false,
            mode: 'local',
            valueField:'id',
            displayField:'name',
            store:this.documentTypeStore,                        
            emptyText : WtfGlobal.getLocaleText("Select Document Type"),
            id:'fromLinkComboId'+this.heplmodeid+this.id,
            typeAhead: true,            
            width:135,
            forceSelection: true,                        
            selectOnFocus:true,           
            scope:this,
            listeners:{
                'select':{
                    fn:this.enableNumber,
                    scope:this
                }
            }
        });
        
        this.documentType.on('select', function() {
            if (this.quickPanelSearch.getValue() != undefined && this.quickPanelSearch.getValue() != "") {
                this.loadData();
      
            }
              
        }, this);
        
        
        this.quickPanelSearch = new Wtf.form.TextField({
            name: 'documentno',
            width: 175,
            emptyText: WtfGlobal.getLocaleText('Search by Document No'),
            disabled:true
        })
        this.timer = new Wtf.util.DelayedTask(this.callKeyUp);
        
        var tbar = new Wtf.Toolbar({
            items:[
                WtfGlobal.getLocaleText("acc.common.from")+" :",this.startDate,"  ","-", WtfGlobal.getLocaleText("acc.common.to")+": ",this.endDate,"       ",this.searchBttn, " Document Type :", this.documentType, "  ", "Document No:",this.quickPanelSearch,this.exportButton
           ] 
        });
        
        var documentType = "";
        /* This will execute if we click on "Related Transaction(s)" button 
         * on report to see all related transactions
         */
        if (this.moduleid != undefined && this.transactionno != undefined) {    
            this.enableNumber,
            this.documentType.setValue(this.moduleid);
            this.quickPanelSearch.setValue(this.transactionno);
        }
        
        
        var html =""; 
        if(this.module && this.module==1){
            html = "<table border=0 style=\"width:98%; border-spacing: 0; border-collapse: collapse;\">"+
                    "<thead> "+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.module.name.18")+"</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.module.name.22")+"</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.module.name.20")+"</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.module.name.27")+"</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.common.cs.InvoiceReturn")+"</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.common.receipt/CN")+"</th>"+
                    "<th class = \"tableHeaderlink\">DN</th>"+
                    "<th class = \"tableHeaderlink\">GL</th></thead></table>";
        } else{
            html = "<table border=0 style=\"width:100%; border-spacing: 0; border-collapse: collapse;\">"+
                    "<thead>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.module.name.20")+"</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.module.name.32")+"</th>"+
                    "<th class = \"tableHeaderlink\">RFQ</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.module.name.23")+"</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.module.name.18")+"</th>"+
                    "<th class = \"tableHeaderlink\">GRN</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.common.cp.InvoiceReturn")+"</th>"+
                    "<th class = \"tableHeaderlink\">"+WtfGlobal.getLocaleText("acc.common.paymentDN")+"</th>"+
                    "<th class = \"tableHeaderlink\">CN</th>"+
                    "<th class = \"tableHeaderlink\">GL</th></thead></table>";
        }
        this.notepanel=new Wtf.Panel({
            id: 'notesouthpanel',
            region: 'north',
            border: false,
            autoScroll: true,
            scope: this,
            hidden: true,
            html: "<span ><b>Note</b>: <span style='color:red;'>Date Filter has been disabled As you have applied search Document Number Filter.</span></span>"
        });
        
        this.mainPanel=new Wtf.Panel({
            id: 'northPanel' + this.id,
            region: 'north',
            border: false,
            scope: this,
            html: html,
            height: 70,
            items: [
                this.notepanel
            ],
            tbar: tbar

        });
        this.add(this.mainPanel);
        this.linkInnerPanel = new Wtf.Panel ({
           border:false, 
           bodyStyle: 'background:white;',
           autoScroll:true,
           scope:this,
           html:this.html
        });
        this.linkPanel = new Wtf.Panel({
            region:'center',
            id:"linkPanel",
            bodyStyle: 'background:white;',
            border:false,
            layout:"fit",
            autoScroll:true,
            scope:this,
            items:[this.linkInnerPanel]
            
        });
        
        this.add(this.linkPanel);
    },
    onRender: function(conf){
        Wtf.linkReport.superclass.onRender.call(this, conf);
        var date = new Date();
        var firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
        var lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0);
        this.startDate.setValue(firstDay);
        this.endDate.setValue(lastDay);
        this.el.dom.onkeyup = this.onKeyUp.createDelegate(this);
        this.loadData();
        
    }
    ,
    loadData: function() {
        if (this.quickPanelSearch.getValue() != undefined && this.quickPanelSearch.getValue() != "") {
            this.startDate.disable();
            this.endDate.disable();
            this.quickPanelSearch.enable();

        } else {
            this.startDate.enable();
            this.endDate.enable();
            this.documentType.setValue("");
            this.mainPanel.setHeight(70);
            this.quickPanelSearch.disable();
        }

        WtfComMsgBox(27, 4, true);
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url: (this.module && this.module == 1) ? "ACCLinkData/getMonthWiseLinkingSalesReport.do" : "ACCLinkData/getMonthWiseLinkingReport.do",
            params: {
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                isCustomer: this.id == "LinkSales" ? true : false,
                documentType: this.documentType.getValue(),
                ss: this.quickPanelSearch.getValue()
            }
        },
        this,
                function(responseObj) {
                    Wtf.MessageBox.hide();
                    WtfGlobal.resetAjaxTimeOut();
                    if (responseObj.success == true) {
                        var table = document.getElementById((this.module && this.module == 1) ? 'linkedsalestable' : 'linkedpurchasetable');
                        if (table) {
                            var parentPanel = table.parentNode;
//                            if (parentPanel) {
//                                parentPanel.removeChild(table);
//                                parentPanel.innerHTML = responseObj.html;
//                            }   

                            this.linkPanel.remove(this.linkInnerPanel);
                            this.linkInnerPanel = new Wtf.Panel({
                                border: false,
                                bodyStyle: 'background:white;',
                                autoScroll: true,
                                scope: this,
                                html: responseObj.html
                            });
                            this.linkPanel.add(this.linkInnerPanel);
                            this.linkPanel.doLayout();
                            
                        }
                    }
                },
                function() {
                    Wtf.MessageBox.hide();
                });

    },

    /* Call when clicking on "Related transaction(s)" button from transactions report & 
     * 
     * linking information tab is already exist 
     */
    showLinkingOfSelectedTransaction: function(moduleid, transactionno) {
        this.enableNumber,
        this.documentType.setValue(moduleid);
        this.quickPanelSearch.setValue(transactionno);
        this.loadData();
    },
   
    /* Call when showing "Link Information Report" for Purchase & sales side & 
     * 
     * linking information tab is already exist 
     */
    showLinkingOfAllTransactions: function() {
        this.documentType.setValue("");
        this.quickPanelSearch.setValue("");
        this.startDate.reset();
        this.endDate.reset();
        this.loadData();
    },
    enableNumber: function() {
        this.quickPanelSearch.enable();
    },
  
    onKeyUp: function(e) {

        if (this.documentType.getValue() != "") {
            this.timer.cancel();
            this.timer.delay(1000, this.callKeyUp, this);

        }else{
            
        }
    },
    
    callKeyUp: function() {

        if (this.quickPanelSearch.getValue() != undefined && this.quickPanelSearch.getValue() != "") {
            var panel = Wtf.getCmp("notesouthpanel");
            if (panel != null) {
                panel.show();
                this.mainPanel.setHeight(80);
            }
            this.startDate.disable();
            this.endDate.disable();
            this.quickPanelSearch.enable();
        } else {
            var panel = Wtf.getCmp("notesouthpanel");
            if (panel != null) {
                panel.hide();
            }
            this.startDate.enable();
            this.endDate.enable();
            this.documentType.setValue("");
            this.mainPanel.setHeight(70);
            this.quickPanelSearch.disable();
        }
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:(this.module && this.module==1)?"ACCLinkData/getMonthWiseLinkingSalesReport.do":"ACCLinkData/getMonthWiseLinkingReport.do",
            params: {
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                isCustomer: this.id == "LinkSales" ? true : false,
                documentType: this.documentType.getValue(),
                ss: this.quickPanelSearch.getValue()
            }
        },
        this,
                function(responseObj) {
                    Wtf.MessageBox.hide();
                    WtfGlobal.resetAjaxTimeOut();
                    if (responseObj.success == true) {
                        var skapanle = Wtf.getCmp("northPanelLinkPurchase");
                        var table = document.getElementById((this.module && this.module == 1) ? 'linkedsalestable' : 'linkedpurchasetable');
                        if (table) {
                            var parentPanel = table.parentNode;
//                            if (parentPanel) {
//                                parentPanel.removeChild(table);
//                                parentPanel.innerHTML = responseObj.html;
////                                skapanle.html = responseObj.html;
//                            }
//                            
                            this.linkPanel.remove(this.linkInnerPanel);
                            this.linkInnerPanel = new Wtf.Panel({
                                border: false,
                                bodyStyle: 'background:white;',
                                autoScroll: true,
                                scope: this,
                                html: responseObj.html
                            });
                            this.linkPanel.add(this.linkInnerPanel);
                            this.linkPanel.doLayout();
                        }
                    }
                });
                
              this.doLayout();

    }  
   
});


function linkPurchaseReportTabDynamicLoad(module,moduleid,transactionno){
    var id = (module==1) ? "LinkSales":"LinkPurchase";
    var panel = Wtf.getCmp(id);
    if(panel==null){
        panel = new Wtf.linkReport({
            id:id,
            module:module,
            moduleid:moduleid,
            transactionno:transactionno,
            html: "<div id = "+( (module==1)?"linkedsalestable":"linkedpurchasetable" )+"><table > <tr><td>&nbsp;</td></tr></table></div>"
        });
        Wtf.getCmp('as').add(panel);
    }else{
       /* Calling Function to load data in report for linking of that particular transaction*/
        if (moduleid != undefined && transactionno != undefined) {
            panel.showLinkingOfSelectedTransaction(moduleid, transactionno);
        } else {
            /* Calling Function  to load data in report for all linking transaction in date range*/
            panel.showLinkingOfAllTransactions();
        }
        
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
