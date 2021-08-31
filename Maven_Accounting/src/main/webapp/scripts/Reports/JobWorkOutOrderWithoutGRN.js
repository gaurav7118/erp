/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


function getJobWorkOutWithGRNViewDynamicLoad(){
    var reportPanel = Wtf.getCmp('jobworkdetail');
    if(reportPanel == null){
        reportPanel = new Wtf.account.TransactionListPanelViewJobWorkDetails({
            id : 'jobworkdetail',
            border : false,
            title: WtfGlobal.getLocaleText("acc.field.jobWorkDetailReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.jobWorkDetailReport"),
            layout: 'fit',
            iscustreport : true,
            closable : true,
            isCustomer:true,
            isSalesPersonName:true,
            jobWorkStockOut:true,
            type:2,
            label:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),
            iconCls:getButtonIconCls(Wtf.etype.inventoryval)
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

Wtf.account.TransactionListPanelViewJobWorkDetails = function(config) {
    Wtf.apply(this,config);
    Wtf.account.TransactionListPanelViewJobWorkDetails.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.TransactionListPanelViewJobWorkDetails,Wtf.Panel, {
    onRender: function(config) {
        var companyDateFormat='Y-m-d'
        Wtf.interStoreTransformRequest.superclass.onRender.call(this, config);
        this.dmflag = 0;
        
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
        this.frmDate = new Wtf.ExDateFieldQtip({
            emptyText:'From date...',
            readOnly:true,
            width : 100,
            value:WtfGlobal.getDates(true),
            minValue: Wtf.archivalDate,
            name : 'frmdate',
            format: companyDateFormat//Wtf.getDateFormat()
        });
        
        this.todateVal=new Date().getLastDateOfMonth();
        this.todateVal.setDate(new Date().getLastDateOfMonth().getDate());
        
        this.asOfDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.asOf"),  //'As of',
        name:'asofdate',
        id: 'asofdate'+config.id+config.helpmodeid,
        format:companyDateFormat,
        value:new Date(Wtf.serverDate.format('M d, Y')+" 12:00:00 AM")
    });
        this.toDate = new Wtf.ExDateFieldQtip({
            emptyText:'To date...',
            readOnly:true,
            width : 100,
            name : 'todate',
            value:WtfGlobal.getDates(false),
            minValue: Wtf.archivalDate,
            format: companyDateFormat//Wtf.getDateFormat()
        });
        this.storeCmbRecord = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },

        {
            name: 'description'
        },

        {
            name: 'fullname'
        },

        {
            name: 'abbrev'
        },

        {
            name: 'dmflag'
        }
        ]);


        /*********** Chk for store load for hq[PA]******/
        this.strloadurl = 'INVStore/getStoreList.do';
        // this.strloadflag = 7;
        var globalRoleid=true;
        if(this.type == 5){
            if(globalRoleid == 10){
                this.strloadurl = 'INVStore/getStoreList.do';
            }
            else{
                this.strloadurl = 'INVStore/getStoreList.do'
            }
        }
        else{
            this.strloadurl = 'INVStore/getStoreList.do';
        //this.strloadflag = 7 ;
        }
        /*********************/
        this.storeCmbStore = new Wtf.data.Store({
            url:  this.strloadurl,
            baseParams:{
                byStoreExecutive:"true",
                byStoreManager:"true",
                includePickandPackStore:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : 'Store*',
            hiddenName : 'store',
            store : this.storeCmbStore,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 180,
            triggerAction: 'all',
            emptyText:'Select store...',
            typeAhead:true,
            forceSelection:true,
            listWidth:300,
            hidden:true,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });

        this.storeCmbfilter.on("select",function(combo,record,index){
            if(record != undefined ){
                if(record.data.dmflag =="1")
                    this.dmflag = 1;
                else
                    this.dmflag = 0;
            }
        },this);


        this.resetBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stock.ClicktoResetFilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                this.frmDate.setValue(new Date());
                this.toDate.setValue(new Date());
                this.asOfDate.setValue(new Date());
                this.storeCmbfilter.setValue(this.storeCmbfilter.store.data.items[0].data.store_id);
            }
        });
        this.createInvoice = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.jobWork.createinv"),    
            scope: this, 
            tooltip:WtfGlobal.getLocaleText("acc.field.jobWork.createinv"),       
            iconCls: getButtonIconCls(Wtf.etype.add)
        });
        this.createInvoice.on('click',this.createInvoiceFunction,this);
        this.search = new Wtf.Button({
            anchor: '90%',
            text: WtfGlobal.getLocaleText("acc.common.search"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")
            },
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: function() {
                var action = 4//(monthlyreportcheck)? getMultiMonthCheck(this.frmDate, this.toDate, 1) : 4;
                switch(action) {
                    case 4:
                        var format = "Y-m-d";
                        this.initloadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmbfilter.getValue(),this.asOfDate.getValue().format(format));
                        break;
                    default:
                        break;
                }
            }
        });


        this.record = Wtf.data.Record.create([
            {
                "name":"id"
            },

            {
                "name":"name"
            },

            {
                "name":"transfernoteno"
            },
            {
                name:"vendorname"
            },
            {
                name:"jobWorkOrderNo"
            },

        
            {
                "name":"personname"
            },
            {
                "name":"personid"
            },
            {
                "name":"billid"
            },
            {
                "name":"currency"
            },

            {
                "name":"itemcode"
            },
            
            {
                "name":"itemId"
            },

        
            {
                "name":"itemname"  
            },
       

            {
                "name":"balquantity"
            },
       
            {
                "name" : "collectDetails"
            },
        
            {
                "name":"recQuantity"
            },
            {
                "name":"quantity"
            },

       

            {
                "name":"date" 
            },

            {
                "name":"itemname"
            },
            {
                "name":"ageingdays"
            },
            {
                "name":"itemname"
            },
            {
                "name":"productid"
            },
       
      
            {
                "name":"statusId"
            },
            {
                "name":"statusJob"
            },
       
            {
                "name":"memo"
            },{
                "name":"challanno"
            }
            ]);
        var grpView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: true,
            enableGroupingMenu: true,
            hideGroupedColumn: false
        });
        this.ds = new Wtf.data.GroupingStore({
            sortInfo: {
                field: 'jobWorkOrderNo',
                direction: "DESC"
            },
            groupField:"jobWorkOrderNo",
            baseParams: {
                type:this.type,
                jobWorkStockOut:this.jobWorkStockOut

            },
              url: 'INVGoodsTransfer/getInterStockTransferList.do',//
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        });
            
         this.asOfDate.on("change",this.checkDates,this);

        this.sm= new Wtf.grid.CheckboxSelectionModel({
             singleSelect:false
            });
            
        var cmDefaultWidth = 150;
        var colArr = [
        this.sm, //1
        {
            header:WtfGlobal.getLocaleText("acc.jobworkorder.header.jobworkorder")+" No.", //3
            dataIndex: 'jobWorkOrderNo',
            width:cmDefaultWidth,
            pdfwidth:50
        },
        {
            header: WtfGlobal.getLocaleText("acc.stockavailability.TransferNoteNo."),  //3
            dataIndex: 'transfernoteno',
            width:cmDefaultWidth,
            pdfwidth:50
        },
        {
            header:WtfGlobal.getLocaleText("acc.ven.name"),
            dataIndex:'personname',
            width:cmDefaultWidth,
            hidden: this.type == 2 && this.jobWorkStockOut ? false : true
        },
        {
            header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"),
            dataIndex:'itemname',
            width:cmDefaultWidth,
            hidden: this.type == 2 && this.jobWorkStockOut ? false : true
        },
        
        {
            header: WtfGlobal.getLocaleText("acc.pdf.6"),     //8
            dataIndex: 'date',
            width:cmDefaultWidth,
            pdfwidth:50
        },
        
        {
            header: WtfGlobal.getLocaleText("acc.fixed.asset.quantity"),
            dataIndex: 'quantity',
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: function(val){
                return val; 
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.field.ReceivedQuantity"),
            dataIndex: 'recQuantity',
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: function(val){
                return val; 
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header5"),
            dataIndex: 'balquantity',
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: function(val){
                return val; 
            }
        },
        {
            header: WtfGlobal.getLocaleText("acc.GIRO.Status"),
            dataIndex: 'statusJob',
            width:cmDefaultWidth,
            pdfwidth:50
        },
        {
            header: WtfGlobal.getLocaleText("acc.jobwork.ageingdays"),
            dataIndex: 'ageingdays',
            width:cmDefaultWidth,
            pdfwidth:50,
            renderer: function(val){
                return val; 
            }
        },
        
        {
            header: WtfGlobal.getLocaleText("acc.JobWorkOut.challanno"),
            dataIndex: 'challanno',
            align: "right",
            width: cmDefaultWidth,
            pdfwidth: 100,
            hidden: this.type == 2 && this.jobWorkStockOut ? false : true

        }

        ];
            
        this.moduleid = Wtf.Acc_Product_Master_ModuleId;   
        var colArr = WtfGlobal.appendCustomColumn(colArr,GlobalColumnModel[this.moduleid]);//appending line level custom column
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray,this.ds);
        this.cm = new Wtf.grid.ColumnModel(colArr);  
        
        this.exportBttn = new Wtf.exportButton({
            obj: this,
            id: WtfGlobal.getLocaleText("stocktransferregisterexportid"),
            tooltip: WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"), //"Export Report details.",  
            menuItem:{
                csv:true,
                pdf:true,
                xls:true
            },
            get:Wtf.autoNum.StockTransferHistoryRegister,
            label:"Export"
        })
        this.printBttn=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            id: WtfGlobal.getLocaleText("stocktransferregisterexportid"),
            tooltip: WtfGlobal.getLocaleText("acc.cosignmentloan.ExportReport"), //"Export Report details.
            filename: WtfGlobal.getLocaleText("acc.field.jobWorkDetailReport"),
            menuItem:{
                print:true
            },
            get:Wtf.autoNum.StockTransferHistoryRegister
        });

         this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        var tbarArray= [];
        tbarArray.push("-","From Date: ",this.frmDate,"-","As of ",this.asOfDate,"-","To Date: ",this.toDate, "-",this.search,"-",this.createInvoice,"-",this.AdvanceSearchBtn);
       var bbarArray=[];
        bbarArray.push("-",this.exportBttn);
        bbarArray.push("-",this.printBttn);

        this.grid=new Wtf.KwlEditorGridPanel({
            id:"inventEditorGridPanel"+this.id,
            cm:this.cm,
            store:this.ds,
            sm:this.sm,
            plugins:this.expander,
            viewConfig: {
                forceFit: false
            },
            view: grpView,
            tbar:tbarArray,
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchLabelSeparator:":",
            searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.Searchbyjobworkout"),
            serverSideSearch:true,
            searchField:"transfernoteno",
            clicksToEdit:1,
            displayInfo: true,
            bbar: bbarArray
        });
        
    
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleidarray: '30'.split(','),
            advSearch: true,
            parentPanelSearch: this,
             isOnlyGlobalCustomColumn: true,
            moduleid:Wtf.Acc_Product_Master_ModuleId,
            reportid:Wtf.Acc_Product_Master_ModuleId,
            ignoreDefaultFields:true
        });
    
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,
            {
                region: 'center',
                border: false,
                layout: "fit",
                autoScroll: true,
                items: [this.grid]
            }]
        });
    
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        
        this.add(this.leadpan);
        this.on("activate",function()
        {
                this.storeCmbStore.load();
                this.storeCmbStore.on("load", function(ds, rec, o){
                    var storeIdSetPreviously=this.storeCmbfilter.getValue();
                    var index =this.storeCmbStore.find('fullname',"ALL");
                    if(index == -1 && rec.length > 1){
                        var newRec=new this.storeCmbRecord({
                            store_id:'',
                            fullname:'ALL'
                        });
                        this.storeCmbStore.insert(0,newRec);
                        this.storeCmbfilter.setValue("",true);
                    }
                    
                    if(storeIdSetPreviously != undefined && storeIdSetPreviously != ""){
                        this.storeCmbfilter.setValue(storeIdSetPreviously, true);
                    }
            
                    this.storeCmbfilter.fireEvent('select');
                    
                }, this);
                
        //
        },this);
        this.initloadgridstore(this.frmDate.getValue().format('Y-m-d'),this.toDate.getValue().format('Y-m-d'),this.storeCmbfilter.getValue(),this.asOfDate.getValue().format('Y-m-d'));

    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    }, 
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.ds.baseParams = {
            type:this.type,
            frmDate:this.frmDate.getValue().format('Y-m-d'),
            toDate:this.toDate.getValue().format('Y-m-d'),
            asofdate:this.asOfDate.getValue().format('Y-m-d'),
            isJobWorkOutRemain:true,
            jobWorkStockOut:true,
            isJobWorkStockOut:true,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_Product_Master_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria,
            reportId: Wtf.Acc_Product_Master_ModuleId
        }
        this.ds.load({
            params: {
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss:this.grid.quickSearchTF.getValue(),
                type:this.type
            }
        });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.ds.baseParams = {
             type:this.type,
            frmDate:this.frmDate.getValue().format('Y-m-d'),
            toDate:this.toDate.getValue().format('Y-m-d'),
            asofdate:this.asOfDate.getValue().format('Y-m-d'),
            isJobWorkOutRemain:true,
            jobWorkStockOut:true,
            isJobWorkStockOut:true,
            moduleid: Wtf.Acc_Product_Master_ModuleId
        }
        this.ds.load({
            params: {
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss:this.grid.quickSearchTF.getValue(),
                type:this.type
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();


    },

    initloadgridstore:function(frm, to,storeid,asof){

        this.ds.baseParams = {
            type:this.type,
            frmDate:frm,
            toDate:to,
            asofdate:asof,
            isJobWorkOutRemain:true,
            jobWorkStockOut:true,
            isJobWorkStockOut:true
        }
        this.ds.load({
            params:{
    
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss:this.grid.quickSearchTF.getValue()
            }
        });
    },
    createInvoiceFunction:function(){
        var selectArray = eval(this.getSelectedRecords());
        /*
     * Atleast one invoice need to select from invoice report
     */
    
        if(selectArray.length==0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.jobworkoutWithoutgrn.promtmsg")], 2);
            return;
        }
        var cnt=0;
        var isCutomerSame=true;
        var isJobWorkSame=true;
        var customerArr=[];
    
    
        /*
     * Amount due must be greater than zero
     */
    
        /*------ Checking whether selected invoices of same vendor/Customer------*/
        while(cnt< selectArray.length){
            if(selectArray[0].personid!=selectArray[cnt].personid){
                isCutomerSame=false;
                break;
            }
//            customerArr.push(selectArray[cnt].personid);
            customerArr.push(selectArray[cnt].personname);
            cnt++;
        }
        cnt=0;
         while(cnt< selectArray.length){
            if(selectArray[0].jobWorkOrderNo!=selectArray[cnt].jobWorkOrderNo){
                isJobWorkSame=false;
                break;
            }
//            customerArr.push(selectArray[cnt].personid);
            customerArr.push(selectArray[cnt].personname);
            cnt++;
        }
     
        if (!isCutomerSame ) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promtmsgsamecustomerjobwork")], 2);
            return;
        }
        if (!isJobWorkSame ) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.promtmsgsamejobwork")], 2);
            return;
        }
        
        Wtf.Ajax.requestEx({
            url:"ACCCustomerCMN/checkIsVendorAsCustomer.do",
           params:{
               customerArr:customerArr
    
           }
                    
        },this,function(response){
            if(response.success && !response.isCreateCustomer){                       
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.cretecustomerasvendor")], 2); 
                return;
            }else{
                /*-------- If Vendor/Customer different & Amount Due>0---------*/
                if(isCutomerSame ){//If Vendor/Customer Same & Amount Due > 0
                    var winid="invoicewindow";
                    var panel = Wtf.getCmp(winid);
                    if(panel==null){
                        panel = new Wtf.account.TransactionPanel({
                            id : winid,
                            isEdit:false,
                            isCustomer:true,
                            invObj:this,
                            isCreateInvFromJob:true,
                            label:WtfGlobal.getLocaleText("acc.field.CustomerInvoice"),
                            isInvoice:true,
                            doctype:1,
                            moduleid:Wtf.Acc_Invoice_ModuleId,
                            border : false,
                            heplmodeid: 2, //This is help mode id
                            //            layout: 'border',
                            DOSettings:Wtf.account.companyAccountPref.DOSettings,
                            GRSettings:Wtf.account.companyAccountPref.GRSettings,
                            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),Wtf.TAB_TITLE_LENGTH),
                            tabTip:WtfGlobal.getLocaleText("acc.accPref.autoInvoice"),  //'Invoice',
                            closable: true,
                            isWithInvUpdate:Wtf.account.companyAccountPref.withinvupdate,
                            iconCls:'accountingbase invoice',
                            modeName:'autoinvoice',
                            isExciseTab:false,
                            isExicseOpeningbalance:false
                        });
                        panel.on("activate", function(){
                            panel.doLayout();
                            Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
                        }, this);
                        Wtf.getCmp('as').add(panel);
                    }
                    Wtf.getCmp('as').setActiveTab(panel);
                    Wtf.getCmp('as').doLayout();
                }
            } 
        },function(){});
   
        cnt=0;

      
    },
     checkDates : function(dateObj,newVal,oldVal){
        if(this.asOfDate.getValue()<this.frmDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.AsofdateshouldbeinrangeofFromDateandToDate")], 2);  //"As of date should not be less than From Date."
            dateObj.setValue(oldVal);
        }           
        if(this.toDate.getValue()<this.startDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),WtfGlobal.getLocaleText("acc.agedPay.FromdateshouldnotbegreaterthanToDate")], 2);  //"From date should not be greater than To Date."
            dateObj.setValue(oldVal);
        }
    },
    getSelectedRecords: function() {
        var arr = [];
        var selectionArray=this.grid.getSelectionModel().getSelections();
        for( var i=0;i<selectionArray.length;i++){
            /*
         *system is considering \n for next line in customers/vendor address so problem is occurring while getting eval their address
         */
            arr.push(this.ds.indexOf(selectionArray[i]));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncodingNew(this.grid, true, arr);
        return jarray;
    }
   
});