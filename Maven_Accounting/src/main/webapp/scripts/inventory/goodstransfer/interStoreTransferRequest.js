
Wtf.interStoreTransformRequest = function(config){
    Wtf.apply(this,config);
    Wtf.interStoreTransformRequest.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.interStoreTransformRequest, Wtf.Panel, {
    initComponent: function() {
        Wtf.interStoreTransformRequest.superclass.initComponent.call(this);
    },
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
            //this.strloadflag = 57 ;
            }
            else{
                this.strloadurl = 'INVStore/getStoreList.do'
            //this.strloadflag = 7;
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
            //                flag:this.strloadflag,
            //                dsmanager:1
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
                this.storeCmbfilter.setValue(this.storeCmbfilter.store.data.items[0].data.store_id);
            //                this.storeCmbfilter.setValue("");
            //this.loadGrid("", "");
            //this.loadgridstore(this.frmDate.getValue().format('Y-m-d'), this.toDate.getValue().format('Y-m-d'));
            }
        });
        var monthlyreportcheck=false;
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
                        this.initloadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmbfilter.getValue());
                        break;
                    default:
                        break;
                }
            }
        });

        //var archivalDate=Date.parseDate(Wtf.archivalDate,"Y-m-d").add(Date.MONTH, 1);
        //        this.monthCal = new Wtf.MonthField({
        //            fieldLabel:'Month*',
        //            width : 200,
        //            minValue: archivalDate.format('F Y'),
        //            value: getSysMonth(),
        //            format: 'F Y'
        //        });

        //this.monthCal.on("change", this.loadgrid, this);

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
            "name":"fromStoreId"
        },

        {
            "name":"toStoreId"
        },

        {
            "name":"fromstorename"
        },
        {
            "name":"fromstoreadd"
        },
        {
            "name":"fromstorefax"
        },
        {
            "name":"fromstorephno"
        },

        {
            "name":"tostorename"
        },
        {
            "name":"tostoreadd"
        },
        {
            "name":"tostorefax"
        },
        {
            "name":"tostorephno"
        },

        {
            "name":"itemcode"
        },
        {
            "name":"ccpartnumber"
        },
        {
            "name":"itemId"
        },

        {
            "name":"itemdescription"
        },
        {
            "name":"itemname"  
        },
        {
            "name":"uomname"
        },

        {
            "name":"quantity"
        },
        {
            "name" : "issueDetails"
        },
        {
            "name" : "collectDetails"
        },
        {
            "name":"defaultStoreName"
        },
        {
            "name":"defaultStoreId"
        },
        {
            "name":"defaultLocName"
        },
        {
            "name":"defaultLocId"
        },
        {
            "name":"defaultCollectionLocId"
        },
        {
            "name":"defaultCollectionLocName"
        },
        {
            "name":"acceptedqty"
        },

        {
            "name":"status"
        },

        {
            "name":"date" 
        //            type:"date", 
        //            format:"Y-m-d"
        },

        {
            "name":"remark"
        },

        {
            "name":"keyfield"
        },

        {
            "name":"createdby"
        },

        {
            "name":"closeornot"
        },
        {
            "name":'orderinguomname'
        },
        
        {
            "name":'transferinguomname'
        },
        
        {
            "name":'stockuomname'
        },
        {
            "name":"transferToStockUOMFactor"
        },

        {
            "name":"packaging"
        },
        {
            "name":"isBatchForProduct"
        },

        {
            "name":"isSerialForProduct"
        },
        {
            name:"isRowForProduct"
        },
        {
            name:"isRackForProduct"
        },
        {
            name:"isBinForProduct"
        },
        {
            "name":"stockDetails"
        },
        {
            "name":"stockDetailsForAccept"
        },
        {
            "name":"costcenter"
        },
        {
            "name":"approvedBy"
        },
        {
            "name":"rejectedBy"
        },
        {
            "name":"statusId"
        },
        {
            "name":"hscode"
        },
        {
            "name":"memo"
        },{
            "name":"challanno"
        },
        {
            "name": "isQAEnable"
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
                field: 'transfernoteno',
                direction: "DESC"
            },
            groupField:"transfernoteno",
            baseParams: {
                //                flag: 24,
                type:this.reportType,//changes variable name 'type' to 'reportType' because 'type' already used while export data.
                jobWorkStockOut:this.jobWorkStockOut
            //salesmonth:this.monthCal.getValue().format('m Y')

            },
            url: 'INVGoodsTransfer/getInterStockTransferList.do',//
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        //            remoteSort: true,
        //            remoteGroup: true
        });
        //  this.ds.load();
        this.ds.addListener("beforeload",function(store,options){
            // Wtf.Ajax.timeout = 120000;
            },this);

        this.ds.on("load",function(store,rec,opt){
            // Wtf.Ajax.timeout = 30000;
            },this);

        this.sm= new Wtf.grid.CheckboxSelectionModel({
            // singleSelect:true
            });
        
        var tableHeaderForBatch='<th ><h2><b>Batch</b></h2></th>';
        var tableHeaderForIssuedSerials='<th ><h2><b>Issued Serials</b></h2></th>';
        var tableHeaderForCollectedSerials='<th ><h2><b>Collected Serials</b></h2></th>';
        var tableDataForBatch='<td ><p>{batchName}</p></td>';
        var tableDataForIssuedSerials='<td ><p>{issuedSerials}</p></td>';
        var tableDataForCollectedSerials='<td ><p>{collectedSerials}</p></td></tr>';
             
       
     
        this.tmplt =new Wtf.XTemplate(
            '<table cellspacing="1" cellpadding="0" style="margin-top:15px;width:100%;margin-bottom:40px;position:relative" border="0">',
            
            '<tr>',
            
            '<th style="padding-left:50px"><h2><b>No.</b></h2></th>',
            '<th ><h2><b>Issued Location</b></h2></th>',
            '<th ><h2><b>Collected Location</b></h2></th>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // row
            '<th><h2><b>Issued Row</b></h2></th>',
            '<th><h2><b>Collected Row</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // rack
            '<th><h2><b>Issued Rack</b></h2></th>',
            '<th><h2><b>Collected Rack</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // bin
            '<th><h2><b>Issued Bin</b></h2></th>',
            '<th><h2><b>Collected Bin</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
            '<th><h2><b>Batch</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<tpl for="parent">',
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  //  serial 
            '<th><h2><b>Issued Serials</b></h2></th>',
            '<th><h2><b>Collected Serials</b></h2></th>',
            '</tpl>',
            '</tpl>',
            
            '<th><h2><b>Issued Quantity</b></h2></th>',
            '<th ><h2><b>Collected Quantity</b></h2></th>',
            
            '</tr>',
     
            '<tr><span  class="gridLine" style="width:94%;margin-left:45px;position: relative;top: 33px;"></span></tr>',
            
            
            '<tpl for="stockDetails">',
            '<tr>',
            '<td style="padding-left:50px"><p>{#}</p></td>',
            
            '<td ><p>{issuedLocationName}</p></td>',
            '<td ><p>{collectedLocationName}</p></td>',
            
            '<tpl if="this.isTrue(parent.isRowForProduct)==true">',  // ROW
            '<td ><p>{issuedRowName}</p></td>',
            '<td ><p>{collectedRowName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isRackForProduct)==true">',  // Rack
            '<td ><p>{issuedRackName}</p></td>',
            '<td ><p>{collectedRackName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isBinForProduct)==true">',  // Bin
            '<td ><p>{issuedBinName}</p></td>',
            '<td ><p>{collectedBinName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isBatchForProduct)==true">',  // batch
            '<td ><p>{batchName}</p></td>',
            '</tpl>',
            
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  // serial 
            '<td style="word-wrap:break-word;"><p>{issuedSerials}</p></td>',
            '<td style="word-wrap:break-word;"><p>{collectedSerials}</p></td>',
            '</tpl>',
            
            '<td ><p>{issuedQuantity}</p></td>',
            '<td ><p>{collectedQuantity}</p></td>',
            
            '</tr>',
            '</tpl>',
            '</table>',
            {  
                isTrue: function(isSerialForProduct){
                    return isSerialForProduct;
                }
            }
            );    
            
               
        this.expander = new Wtf.grid.RowExpander({
            tpl :this.tmplt,
            renderer : function(v, p, record){
                var isBatchForProduct=record.get("isBatchForProduct");
                var isSerialForProduct=record.get("isSerialForProduct");
                if(record.get("stockDetails").length>0){ //means has stock detail data
                    return  '<div class="x-grid3-row-expander">&#160;</div>'
                }else{
                    //return '&#160;' 
                    return  '<div class="x-grid3-row-expander">&#160;</div>'
                }
            }
           
        });
        
        
        
        var trackStoreLocation=true;
        var integrationFeatureFor=true;
        var cmDefaultWidth = 100;
       var colArr = [
            new Wtf.KWLRowNumberer(),  //0
            this.sm, //1
            this.expander,  //2
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.TransferNoteNo."),  //3
                //sortable:true,
                dataIndex: 'transfernoteno',
                width:cmDefaultWidth,
                pdfwidth:50
            //                hidden:true,
            //                fixed:true
            },
            {
                header: WtfGlobal.getLocaleText("0acc.stockavailability.TransferNoteNo"), //4
                //sortable:true,
                dataIndex: 'keyfield',
                hidden:true,
                fixed:true,
                groupRenderer:function(v,u,r,ri,ci,ds){
                    return ds.data.items[ri].get('transfernoteno');
                // return v;
                }
            },
            {
                header:WtfGlobal.getLocaleText("acc.ven.name"),
                dataIndex:'vendorname',
                width:cmDefaultWidth,
                //changes variable name 'type' to 'reportType' because 'type' already used while export data.
                hidden: this.reportType == 2 && this.jobWorkStockOut ? false : true
            },
            {
                header:WtfGlobal.getLocaleText("acc.JobWorkOut.PurcahseOrder")+" No",
                dataIndex:'jobWorkOrderNo',
                width:cmDefaultWidth,
                //changes variable name 'type' to 'reportType' because 'type' already used while export data.
                hidden: this.reportType == 2 && this.jobWorkStockOut ? false : true
            },
            {
                header: WtfGlobal.getLocaleText("acc.nee.69"),   //5
                //sortable:true,
                dataIndex: 'createdby',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.stock.FromStore"),     //6
                //sortable:true,
                dataIndex: 'fromstorename',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.stock.ToStore"),         //7
                //sortable:true,
                dataIndex: 'tostorename',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.pdf.6"),     //8
                //sortable:true,
                dataIndex: 'date',
                width:cmDefaultWidth,
                pdfwidth:50
            //renderer: Wtf.util.Format.dateRenderer(companyDateFormat)
            },
            {
                header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                //sortable:true,
                dataIndex: 'itemcode',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("mrp.qcreport.gridheader.productname"),
                //sortable:true,
                dataIndex: 'itemname',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.je.CoilcraftPartNo"),
                //sortable:true,
                dataIndex: 'ccpartnumber',
                hidden:true //integrationFeatureFor == true ? false: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.common.costCenter"),
                //sortable:true,
                dataIndex: 'costcenter',
                width:cmDefaultWidth,
                pdfwidth:50
            //hidden:true //integrationFeatureFor == true ? false: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.report.rule16register.UOM"),
                //sortable:true,
                dataIndex: 'name',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.product.packaging"),
                //sortable:true,
                dataIndex: 'packaging',
                width:cmDefaultWidth,
                hidden:false,
                pdfwidth:50
            },
            {
            header: WtfGlobal.getLocaleText("acc.stock.IssuedQuantity"),
            //sortable:false,
            dataIndex: 'quantity',
            width:cmDefaultWidth,
            pdfwidth:50,
            //changes variable name 'type' to 'reportType' because 'type' already used while export data.
                editor:this.reportType == 1?new Wtf.form.NumberField({
                    scope: this,
                allowBlank:false,
                    allowNegative:false,
                    decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
                    listeners : {
                        'focus': setZeroToBlank
                }
                }):null,
                renderer: function(val){
                    return val; //WtfGlobal.getCurrencyFormatWithoutSymbol(val, Wtf.companyPref.quantityDecimalPrecision);
                }
            },
            {
                header: '',
                dataIndex:"collectDetails",
                renderer: this.serialRenderer.createDelegate(this),
                hidden:!(this.reportType === 1),//changes variable name 'type' to 'reportType' because 'type' already used while export data.
                width:40
            },
            {
                header: WtfGlobal.getLocaleText("acc.stockavailability.CollectedQuantity"),
                dataIndex: 'acceptedqty',
                width:cmDefaultWidth,
                pdfwidth:50,
                //                editor:this.type == 1?new Wtf.form.NumberField({
                //                    scope: this,
                //                    allowBlank:false,
                //                    allowNegative:false,
                //                    listeners : {
                //                       // 'focus': setZeroToBlank
                //                    }
                //                }):null,
                renderer: function(val){
                    return val; 
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.GIRO.Status"),
                //sortable:true,
                dataIndex: 'status',
                width:cmDefaultWidth,
                pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.assetworkorder.Remark"),
                //sortable:true,
                dataIndex: 'remark',
                width:cmDefaultWidth,
                pdfwidth:100,
                renderer:function(value,meta,rec){
                    if(value!=""){
                        meta.attr = "Wtf:qtip='"+ value +"' Wtf:qtitle='Remark' ";
                    }
                    return value;
                }
            },{
            header: WtfGlobal.getLocaleText("acc.repeated.Gridcol3")+"/ " +WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
            dataIndex: 'memo',
//            groupable: true,
//            hidden:this.type==1,
            width:cmDefaultWidth,
            pdfwidth:50
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.Approvedby"),  
                dataIndex:'approvedBy',
                width:cmDefaultWidth,
                align:"right",
                pdfwidth:100,
                hidden:this.reportType==2?false:true//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            //                renderer: function(v,m,rec){
            //                    if(rec.get('statusId') != 2){
            //                        return v;
            //                    }else return "";
            //                }
                
               
            },
            {
                header: WtfGlobal.getLocaleText("acc.contract.product.replacement.Rejected"),  
                dataIndex:'rejectedBy',
                align:"right",
                width:cmDefaultWidth,
                pdfwidth:100,
                hidden:this.reportType==2?false:true//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            //                renderer: function(v,m,rec){
            //                    if(rec.get('statusId') === 2){
            //                        return v;
            //                    }else return "";
            //                }
                
            },
            {
                header: WtfGlobal.getLocaleText("acc.JobWorkOut.challanno"),
                dataIndex: 'challanno',
                align: "right",
                width: cmDefaultWidth,
                pdfwidth: 100,
                //changes variable name 'type' to 'reportType' because 'type' already used while export data.
                hidden: this.reportType == 2 && this.jobWorkStockOut ? false : true

            }

            ];
            
        this.moduleid = Wtf.Acc_InterStore_ModuleId;    
        colArr = WtfGlobal.appendCustomColumn(colArr,GlobalColumnModelForReports[this.moduleid],true, undefined, true);//appending global level custom column
        var colModelArray = GlobalColumnModelForReports[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.ds);
        colArr = WtfGlobal.appendCustomColumn(colArr,GlobalColumnModel[this.moduleid]);//appending line level custom column
        colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray,this.ds);
        this.cm = new Wtf.grid.ColumnModel(colArr);  
        
        if(this.reportType==2){ //type 2 = history tab,//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            this.cm.setHidden(2,false);  //if history tab then show expander
            this.cm.setHidden(1,true);   //if history tab then hide checkbox 
        //    this.cm.setHidden(17,false);  //if history tab then show collected qty
        }else{
            this.cm.setHidden(2,true);
            this.cm.setHidden(1,false);
           // this.cm.setHidden(17,false);
          //   this.cm.setHidden(18,true);
            this.cm.setHidden(19,true);   //if incoming & outgoing request tab then not show collected quantity
        } 
            
        this.rejectButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.field.Reject"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stockavailability.ClicktoRejectIncomingRequest")
            },
            id:'reject',
            handler:function(){
                this.remarkfunction("reject");
            },
            scope:this
        });
        this.addButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.add"),
            iconCls:'pwnd addicon caltb',
            id:"add",
            handler:this.add1,
            scope:this
        });
        this.deleteButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.delete"),
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            id:"delete",
            handler:this.deleteHandler,
            scope:this
        });
        this.editButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.stockavailability.Accept"),
            iconCls:getButtonIconCls(Wtf.etype.add),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stockavailability.ClicktoacceptIncomingRequest")
            },
            id:"accept",
            //handler:this.acceptFunction,
            handler:function(){
                
                var recs = this.grid.getSelections();
//                if(selected.length==1){
//                    if(selected[0].get("remark")=="Stock Returned"&&selected[0].get("status")=="Returned"){
//                        this.processReturnRequest(selected);
//                    }else{
//                        this.showStockDetailWindow(selected[0])
//                    }
//                }
//                else 
                    if(recs.length >= 1){
                    if(!this.isValidRequestSelection(recs)){
                        WtfComMsgBox(["Alert", "You can select multiple requests of same order only."],3);
                        return false;
                    }
                    if(recs[0].get("remark")=="Stock Returned"&&recs[0].get("status")=="Returned"){
                        if(recs.length==1){
                            this.processReturnRequest(recs);
                        }else{
                            WtfComMsgBox(["Alert", "You can select only one record for return request."],3);
                            return false;
                        }
                    }else if(this.isValidDetails(recs)){
                        this.validateDetailsAndSubmit(recs);
                    }else{
                        var confirmMsg = "Do you want to receive stock in a single location for selected requests ? <br>";
                        Wtf.MessageBox.confirm("Confirm",confirmMsg, function(btn){
                            if(btn == 'yes') {
                                this.selectDefaultLocation(recs,true);
                            }else{
                                this.validateDetailsAndSubmit(recs);
                            }
                        },this);
                    }
                }else{
                    WtfComMsgBox(["Alert", "Please select a record."],3);
                    return;
                }
                    
            //   this.remarkfunction("accept");
            // this.acceptFunction("accept","");
            },
            scope:this
        });
                
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

      /*
       * ERM-718
       * Add Import fuctionality for stock transfer(Import Button)
       */
      var extraConfig = {}; 
        extraConfig.url= "INVGoodsTransfer/importInterStoreTransferRequest.do";
        var menuArray= Wtf.importMenuArray(this, Wtf.Inter_Store_Stock_Transfer, this.ds, "", extraConfig);
         this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu:menuArray
            
        });
        
        this.printBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.print"),
            scope: this,
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printTTip")
            },
            id:'interstorestockreportprint',
            iconCls:'pwnd printButtonIcon',
            hidden:false,
            disabled:true,
            handler: function(){
                var selected= this.sm.getSelections();
                var cnt = selected.length;
                if (cnt > 0) {
                    printout("interstore", this.ds.query("transfernoteno", selected[0].data.transfernoteno));
                }else{
                    return;
                }
                // this.Print();
            }
            
        });
        
        this.ruleNo11NButton=new Wtf.Button({
            text:"Export Rule No 11",
            scope: this,
            id:'ruleNo11NButton',
            iconCls:'pwnd printButtonIcon',
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            disabled:true,
            handler: function(){
                var selected= this.sm.getSelections();
                var productIds="";
                for(var i=0;i<selected.length;i++){
                    productIds+=selected[i].data.itemId+",";
                }
                var id=selected[0].data.transfernoteno;
                var type=this.reportType;//changes variable name 'type' to 'reportType' because 'type' already used while export data.
                Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportRuleNo11Jasper.do?transactionNo="+id+"&type="+type+"&productIds="+productIds;
            }
        });
        /*Print Record Button*/
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        
        var colModArray=[];
        if(this.jobWorkStockOut){
            //get job work out stock transfer module templates
            colModArray = GlobalCustomTemplateList[Wtf.autoNum.JobWorkOutStockTransferModuleID];
        } else{
            colModArray = GlobalCustomTemplateList[Wtf.Acc_InterStore_ModuleId];
        }
        
        var isTflag=colModArray!=undefined && colModArray.length>0?true:false;
        if(isTflag){
            for (var count = 0; count < colModArray.length; count++) {
                var id1=colModArray[count].templateid;
                var name1=colModArray[count].templatename;           
                Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                    iconCls: 'pwnd printButtonIcon',
                    text: name1,
                    id: id1
                }); 
            }           
        }else{
            Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                iconCls: 'pwnd printButtonIcon',
                text:WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinCustomDesigner"),
                id: Wtf.No_Template_Id
            });
        }
        Wtf.menu.MenuMgr.get("printmenu" + this.id).on('itemclick',function(item) {
            this.printRecordTemplate('print',item);
        }, this);
        
        this.singleRowPrint = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.rem.236"),
            hidden:(WtfGlobal.EnableDisable(Wtf.UPerm.stockrequest, Wtf.Perm.stockrequest.printstockreq)),
            iconCls:'pwnd printButtonIcon',
            tooltip:WtfGlobal.getLocaleText("acc.rem.236.single"),
            scope:this,
            disabled:true,
            menu:this.printMenu
        })

        var tbarArray= new Array();
        if(this.reportType == 2 || this.type == 5){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            tbarArray.push("-","From Date: ",this.frmDate,"-","To Date: ",this.toDate, "-","Store: ",this.storeCmbfilter, "-",this.search,"-",this.resetBtn);
        }else if(this.reportType == 1 ||this.reportType == 3){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
        //tbarArray.push("-","Month: ",this.monthCal);
        }
        this.jobWorkStockOutBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.JobWorkOut.addStockTransfer"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            tooltip: WtfGlobal.getLocaleText("acc.JobWorkOut.addStockTransfer"),
            disabled: false
        });
        this.jobWorkStockOutBtn.on('click', function() {
            this.addJobWotkStockOut();
        }, this);    
        /**************date *****/


        //  this.frmDate.on('blur',function(){
        //           if(!(this.frmDate.getValue()==""&&this.frmDate.getValue()!=null)&&
        //			   !(this.toDate.getValue()==""&&this.toDate.getValue()!=null))
        //           {
        //              if(this.frmDate.getValue()>this.toDate.getValue()){
        //                  Wtf.MessageBox.show({
        //                                title: 'Invalid Date',
        //                                msg: 'Report from date cannot be higher than report upto date',
        //                                buttons: Wtf.MessageBox.OK,
        //                                animEl: 'ok',
        //                                icon: Wtf.MessageBox.INFO
        //                            });
        //                  this.frmDate.setValue("");
        //                  return;
        //                } else {
        //                    //calc duration
        //                    var frm = this.frmDate.getValue().format(Wtf.getDateFormat());
        //                    var to = this.toDate.getValue().format(Wtf.getDateFormat());
        //                    if((frm!=""&&to!="")){
        //						this.loadgridstore(frm, to);
        //                    }
        //                    else return;
        //                }
        //            }
        //        },
        //        this);

        //        this.toDate.on('blur',function(){
        //           if(!(this.frmDate.getValue()==""&&this.frmDate.getValue()!=null)&&
        //               !(this.toDate.getValue()==""&&this.toDate.getValue()!=null)
        //             )
        //              {
        //                if(this.frmDate.getValue()>this.toDate.getValue()){
        //                    Wtf.MessageBox.show({
        //                                title: 'Invalid Date',
        //                                msg: 'Report upto date cannot be before report from date',
        //                                buttons: Wtf.MessageBox.OK,
        //                                animEl: 'ok',
        //                                icon: Wtf.MessageBox.INFO
        //                            });
        //                    this.toDate.setValue("");
        //                    return;
        //                 } else {
        //                    //calc duration
        //                    var frm = this.frmDate.getValue().format(Wtf.getDateFormat());
        //                    var to = this.toDate.getValue().format(Wtf.getDateFormat());
        //                    if((frm!=""&&to!="")){
        //						this.loadgridstore(frm, to);
        //                    }
        //                    else return;
        //                 }
        //              }
        //        },this);
        //
        var bbarArray=new Array();
        if(this.reportType == 1){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.acceptrejectistreq)) {
                bbarArray.push("-",this.editButton);
                bbarArray.push("-",this.rejectButton);
                bbarArray.push("-",this.printBttn);
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.printistreq)) {
                bbarArray.push("-",this.printBttn);
            }
        }
        if(this.reportType == 2){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            bbarArray.push("-",this.importBtn);
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.exportisteq)) {
                bbarArray.push("-",this.exportBttn);
                bbarArray.push("-",this.printBttn);
            }
              if(this.jobWorkStockOut && this.reportType == 2){ //changes variable name 'type' to 'reportType' because 'type' already used while export data.               //bustton to add job workout stock transfer
                tbarArray.push(this.jobWorkStockOutBtn);
            }
        }
        if(this.reportType == 3 || this.reportType == 2){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.createistreq) && Wtf.account.companyAccountPref.deleteTransaction){
               // bbarArray.push(this.deleteButton);
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.exportisteq)) {
                bbarArray.push(this.printBttn);
            }
        }
         bbarArray.push(this.ruleNo11NButton);
         bbarArray.push(this.singleRowPrint);

        /********************/
        this.grid=new Wtf.KwlEditorGridPanel({
            id:"inventEditorGridPanel"+this.id,
            cm:this.cm,
            store:this.ds,
            sm:this.sm,
            plugins:this.expander,
            // stripeRows : true,
            viewConfig: {
                forceFit: false
            },
            view: grpView,
            tbar:tbarArray,
            searchLabel:WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchLabelSeparator:":",
            searchEmptyText:WtfGlobal.getLocaleText("acc.stockavailability.SearchbyTransferNoteNoSerialNo"),
            serverSideSearch:true,
            searchField:"transfernoteno",
            clicksToEdit:1,
            displayInfo: true,
            bbar: bbarArray
        });
        //"-",this.addButton,
        //If you want to enable button ,if only one record selected ,otherwise disable
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        
//         this.grid.on("beforeedit",function(){
//             return false;
//         },this);
        var arrId=new Array();
        arrId.push("reject");//"deleteIssueBtn" id of button
        //        arrId.push("accept");

        //enableDisableButton(arrId,this.ds,this.sm);
        this.ok=1;
        //        this.grid.on("cellclick",this.cellClickFunction,this)
        //        this.grid.on("afteredit",this.afterEditFunction,this)
        this.grid.on("validateedit",this.validateeditFunction,this);
        this.grid.on("statesave",this.statesaveFunction,this);
        this.grid.on("cellclick", this.onCellClick, this);
        this.sm.on("selectionchange",function(){
            var selected = this.sm.getSelections();
            if(selected.length>0){
                var test = 0;
                var selTransactionNo = selected[0].get("transfernoteno");
                var different=false;
                for(var i=0;i<selected.length;i++){                     
                    if(selected[i].get("closeornot") == true){
                        test = 1;
                    }    
                    if(selected[i].get("transfernoteno") != selTransactionNo){
                        different = true;
                    }       
                }
                //                 var test = selected.find("closeornot",true);
                if(test == 0){
                    this.editButton.enable();
                    this.rejectButton.enable();
                } else {
                    this.editButton.disable();
                    this.rejectButton.disable();
                }
                if(selected[0].get("remark")=="Stock Returned"&&selected[0].get("status")=="Returned"){
                    this.rejectButton.disable();
                }
                if(different == true){
                    this.printBttn.disable();
                    this.ruleNo11NButton.disable();
                    this.singleRowPrint.disable();
                }else{
                    this.printBttn.enable();
                    this.ruleNo11NButton.enable();
                    this.singleRowPrint.enable();
                }
               
            }else{
                this.printBttn.disable(); 
                this.ruleNo11NButton.disable(); 
                this.singleRowPrint.disable();
            }

        },this);
        this.quanarr = new Array();
        this.ds.on("load",function(s,r){
            this.editButton.disable();
            this.rejectButton.disable();
            this.quanarr =  new Array();
            var i;
            for(i=0 ; i<s.getCount() ; i++){
                this.quanarr.push(s.data.items[i].data.quantity);
            }
            
        // Wtf.getCmp('interstoretransReq').enable();
        },this);
        this.add(this.grid);
        if(this.type == 5){
            this.storeCmbStore.load();
            this.storeCmbStore.on("load", function(ds, rec, o){
                if(rec.length > 0){
                    this.storeCmbfilter.setValue(rec[0].data.id, true);
                    if(rec[0].data.dmflag =="1")
                        this.dmflag = 1;
                    else
                        this.dmflag = 0;
                    var action = (monthlyreportcheck)? getMultiMonthCheck(this.frmDate, this.toDate, 1) : 4;
                    switch(action) {
                        case 4:
                            var format = "Y-m-d H:i:s";
                            this.initloadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format), this.storeCmbfilter.getValue());
                            break;
                        default:
                            break;
                    }
                //this.initloadgridstore(this.frmDate.getValue().format(Wtf.getDateFormat()), this.toDate.getValue().format(Wtf.getDateFormat()),rec[0].data.id);
                }

            }, this);
        }
        this.on("activate",function()
        {
            if(this.reportType ==2 ){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
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
                    this.initloadgridstore(this.frmDate.getValue().format('Y-m-d'),this.toDate.getValue().format('Y-m-d'),this.storeCmbfilter.getValue());
                    
                }, this);
                
            }else if(this.type == 5){
                if(this.storeCmbfilter.getValue('store_id') != ""){
                    this.initloadgridstore(this.frmDate.getValue().format(Wtf.getDateFormat()), this.toDate.getValue().format(Wtf.getDateFormat()),this.storeCmbfilter.getValue('id'));
                }
                          
            }else {
                //                this.ds.load({
                //                    params:{
                //                        start:0,
                //                        limit:30
                ////                        salesmonth:this.monthCal.getValue().format('m Y')
                //                    }
                //                });
                this.ds.load({
                    params:{
                        start:0,
                        limit:30
                    //                        salesmonth:this.monthCal.getValue().format('m Y')
                    }
                });
            }
        //
        },this);
    //        this.storeCmbfilter.on("select", function(cmb, rec, index){
    //
    //           this.loadgridstore(this.frmDate.getValue().format(Wtf.getDateFormat()), this.toDate.getValue().format(Wtf.getDateFormat()));
    //
    //		}, this);

    },
    add1:function(){

    // alert("accept");
    },
    addJobWotkStockOut:function(){
      var obj={};
      obj.isJobWorkStockOut=true;
      interStoreTransfers(obj);
    },
    serialRenderer:function(v,m,rec){
        if((this.reportType ===1 && rec.get('statusId') === 0)){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
        }else{
            return "";
        }
        
    },
    onCellClick :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name

        var linedata = [];//disable custom data editing on cell click
        linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModel[Wtf.Acc_InterStore_ModuleId]);
        for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
            if (linedata[lineFieldCount].dataIndex == fieldName) {
                return false;
            }
        }
         if (this.reportType == 1 && fieldName == 'quantity' && record.data.statusId == 3){
            return false;
        }
        if(e.getTarget('.serialNo-gridrow')){
            if(fieldName == "collectDetails"){
                if(record.data.quantity==0){
                    WtfComMsgBox(["Warning", "Please fill collect quantity."],0);
                    return false;
                }
                this.showStockDetailWindowForCollect(record);
            }
        }
        
    },
    isValidRequestSelection: function(selectedRecs){
        var transNo = null;
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            if(i==0){
                transNo = rec.get('transfernoteno');
            }else{
                if(transNo !== rec.get('transfernoteno')){
                    valid = false;
                    break;
                }
            }
//            if(rec.get('statusId') !== 0){ // 0 is for In Transit
//                valid = false;
//                break;
//            }
        }
        return valid;
    },
    isValidDetails: function(selectedRecs){
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            var detailQty = 0;
            var fillQty = 0;
            detailQty = rec.get('stockDetailsCollectQuantity');
            fillQty=rec.get("quantity");
            if(!detailQty || detailQty <= 0 || detailQty !== fillQty){
                valid = false;
            }
            var issuedquantity = 0;
            if(rec.data.stockDetails!=undefined && rec.data.stockDetails!=null && rec.data.stockDetails.length>0){
                for (var j = 0; j < rec.data.stockDetails.length; j++) {
                    issuedquantity = issuedquantity + rec.data.stockDetails[j].issuedQuantity;
                }
            }          
            if (rec.data.quantity != issuedquantity) { // validating case of partial inter store transfer
                    valid = true;
                    break;                
            }
        }
        return valid;
    },
    selectDefaultLocation: function(selectedRecs,alltoDfLocation){
        var rec = selectedRecs[0];
        
        this.win = new Wtf.DefaultLoationCollectWin({
            id: "defaultlocationcollectwin",
            border : false,
            title : "Select Collect Location",
            layout : 'fit',
            closable: true,
            width:450,
            height:300,
            modal:true,
            storeId:rec.get('toStoreId'),
            defaultLocationId:rec.get('defaultCollectionLocId'),
            resizable:false
        });
        this.win.on('locationSelected', function(locationId, locationName){
            for(var i=0; i<selectedRecs.length; i++){
                var r= selectedRecs[i];
                r.set("defaultCollectionLocId", locationId);
                r.set("defaultCollectionLocName", locationName);
            }
            this.collectInDefaultLocation(selectedRecs,alltoDfLocation);
        },this);
        this.win.show();
        
    },
    collectInDefaultLocation: function(selectedRecs){
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            var collectQty=rec.get("quantity"); 
            var detailQty=rec.get("stockDetailsCollectQuantity"); 
            if(!detailQty ||detailQty <= 0 || detailQty !== collectQty ){
                var transferToStockUOMFactor=rec.get("transferToStockUOMFactor");
                var availableQty=rec.get("defaultAvailQty"); // in primary uom
                var isBatchForProduct=rec.get("isBatchForProduct");
                var isSerialForProduct=rec.get("isSerialForProduct");
                var isRowForProduct=rec.get("isRowForProduct");
                var isRackForProduct=rec.get("isRackForProduct");
                var isBinForProduct=rec.get("isBinForProduct");
                var defaultCollectLocId=rec.get("defaultCollectionLocId");
                var defaultCollectLocName=rec.get("defaultCollectionLocName");
                var issuedStockArray=rec.get("stockDetails");
//                var isDefaultAllowed = !(isBatchForProduct || isSerialForProduct || isRowForProduct || isRackForProduct || isBinForProduct);
                var isDefaultAllowed = !(isRowForProduct || isRackForProduct || isBinForProduct);//in IST case batch and serial are allowed for auto select
                if(!isDefaultAllowed){
                    WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                    valid = false;
                    continue;
                }else{
                    
                    if(defaultCollectLocId){
                        var collectedData = [];
                        for(var idx=0; idx < issuedStockArray.length; idx++){
                            var convertedCollectedQtyInPrimaryQty = transferToStockUOMFactor * collectQty; // collectqty in primary uom
                            var issuedData = issuedStockArray[idx];
                            var qty=issuedData.issuedQuantity
                            var data = {
//                                fromLocationId: issuedData.issuedLocationId,
                                rowId: "",
                                rackId: "",
                                binId: "",
                                batchName: issuedData.batchName,
                                detailId: issuedData.id,
                                locationId: defaultCollectLocId,
                                toLocationName: defaultCollectLocName,
                                serialNames:issuedData.issuedSerials,
                                quantity:issuedData.issuedQuantity,
                                autoSelection:true
                            }
                            if(convertedCollectedQtyInPrimaryQty == 0){
                                break;
                            }else{
//                                if(convertedCollectedQtyInPrimaryQty > availableQty){
//                                    valid = false;
//                                }
//                                data.quantity = convertedCollectedQtyInPrimaryQty;
                                convertedCollectedQtyInPrimaryQty = 0;
                                collectedData.push(data);
                            }
                        }
                        rec.set("stockDetailsForAccept", "");
                        rec.set("stockDetailsCollectQuantity","");
                        rec.set("stockDetailsForAccept", collectedData);
                        rec.set("stockDetailsCollectQuantity",collectQty);
                    }else{
                        WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                        valid = false;
                        continue;
                    }
                }
            }
        }
        if(!valid){
            WtfComMsgBox(["Warning", "Please fill valid details for highlighted records"], 1);
            this.grid.getSelectionModel().clearSelections();
            return false;
        }else{
            this.acceptFunction("accept",true);
        }
    },
    
    showStockDetailWindowForCollect : function (record){

        var orderId=record.get("id");
        var itemId=record.get("itemId");
        var itemCode=record.get("itemcode");
        var quantity=record.get("quantity");
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isRackEnable = record.get("isRackForProduct");
        var isRowEnable = record.get("isRowForProduct");
        var isBinEnable = record.get("isBinForProduct");
        var issuedStoreId = record.get("fromStoreId");
        var issuedStoreName = record.get("fromstorename");
        var collectStoreId = record.get("toStoreId");
        var collectStoreName = record.get("tostorename");
        var transferToStockUOMFactor=record.get("transferToStockUOMFactor")
        var transferingUomName=record.get("transferinguomname");
        var stockUOMName=record.get("stockuomname");
        var maxQtyAllowed=parseFloat(getRoundofValue(transferToStockUOMFactor * quantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        
         
        
        var winTitle = WtfGlobal.getLocaleText("acc.stockrequest.StockDetailforStockTransfer");
        var winDetail = String.format(WtfGlobal.getLocaleText("acc.stockrequest.SelectStockdetailsforstocktransfer")+'<br> <b>'+WtfGlobal.getLocaleText("acc.product.gridProduct")+":"+'</b> {0}<br> <b>'+WtfGlobal.getLocaleText("acc.stockrequest.IssuanceStore")+" :"+'</b> {1}<br><b>'+WtfGlobal.getLocaleText("acc.stockrequest.CollectionStore")+" :"+'</b> {2}<br> <b>'+WtfGlobal.getLocaleText("acc.fixed.asset.quantity")+" :"+'</b> {3} {4} ( {5} {6} )', itemCode, issuedStoreName, collectStoreName, quantity, transferingUomName, maxQtyAllowed, stockUOMName);
        
        var detailWin = new Wtf.StockTransferDetailWin({
            WinTitle : winTitle,
            WinDetail: winDetail,
            TotalTransferQuantity: maxQtyAllowed,
            ProductId:itemId,
            FromStoreId: issuedStoreId,
            ToStoreId: collectStoreId,
            isBatchForProduct: isBatchEnable,
            isSerialForProduct : isSerialEnable,
            isRowForProduct: isRowEnable,
            isRackForProduct: isRackEnable,
            isBinForProduct: isBinEnable,
            StockDetailArray:record.get("stockDetailsForAccept"),
            GridStoreURL:"INVGoodsTransfer/getISTIssuedDetailList.do",
            moduleid:Wtf.Acc_InterStore_ModuleId,
            type:this.reportType,//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            GridStoreExtraParams: {
                requestId : orderId
            },
            DataIndexMapping:{
                detailId : 'detailId',
                toLocationId:"locationId",
                toRowId:"rowId",
                toRackId:"rackId",
                toBinId:"binId",
                serials:"serialNames"
            },
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                handler:function (){
                    if(detailWin.validateSelectedDetails()){
                        var detailArray = detailWin.getSelectedDetails();

                        record.set("stockDetailsForAccept",'');
                        record.set("stockDetailsForAccept",detailArray);
                        record.set("stockDetailsCollectQuantity",quantity);
                        detailWin.close();
                    }else{
                        return;
                    }
                },
                scope:this
            },{
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                handler:function (){
                    detailWin.close();
                },
                scope:this
            }]
        })
        detailWin.show();
    },
    
    validateDetailsAndSubmit: function(selectedRecs){
        var valid = true;
        for(var i=0; i<selectedRecs.length; i++){
            var rec = selectedRecs[i];
            var detailQty = 0;
            var fillQty = 0;
            if(this.reportType == 1){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
               detailQty = rec.get('stockDetailsCollectQuantity')
               fillQty = rec.get('quantity');
            }
            if(!detailQty ||detailQty <= 0 || detailQty !== fillQty ){
                WtfGlobal.highLightRowColor(this.grid,rec,true,0,2,true);
                valid = false;
            }
        }
        if(!valid){
            WtfComMsgBox(["Alert", "Please fill valid details for highlighted records"], 1);
            this.grid.getSelectionModel().clearSelections();
            return false;
        }else{
            if(this.reportType == 1){//changes variable name 'type' to 'reportType' because 'type' already used while export data.
                this.acceptFunction("accept","");
            }
        }
    },
    deleteHandler: function(){
        var recs = this.sm.getSelections();
        if(recs.length == 0){
            WtfComMsgBox(["Alert", "Please select at least one record to delete"], 0);
            return false
        }
        var requestIds = [];
        var requestString = "";
        var notDeletingRequests = "";
        var invalidRec  = false;
        for(var i=0 ; i< recs.length; i++){
            var rec = recs[i];
            requestIds.push(rec.get('id'));
            if(rec.get('status') == "In Transit" || rec.get('status') =="Accepted"){
                requestString += "<br><b>"+(i+1)+").</b> ";
                requestString += "Request No: <b>"+rec.get('transfernoteno')+"</b>, ";
                requestString += "Product: <b>"+rec.get('itemcode')+"</b>, ";
                requestString += "Between Store: <b>"+rec.get('fromstorename')+"</b> to <b>"+rec.get('tostorename')+"</b>";
            }else{
                invalidRec = true;
                notDeletingRequests += "<br><b>"+(i+1)+").</b> ";
                notDeletingRequests += "Request No: <b>"+rec.get('transfernoteno')+"</b>, ";
                notDeletingRequests += "Product: <b>"+rec.get('itemcode')+"</b>, ";
                notDeletingRequests += "Between Store: <b>"+rec.get('fromstorename')+"</b> to <b>"+rec.get('tostorename')+"</b>";
            }
            
        }
        if(invalidRec){
            WtfComMsgBox(["Warning", "You can delete only In-Transit Requests. Please unselect following records.<br>"+notDeletingRequests], 1);
            return false
        }
        var confirmMsg = "Are you sure want to delete following request(s)? <br> "+requestString;
        Wtf.MessageBox.confirm("Confirm",confirmMsg, function(btn){
            if(btn == 'yes') {
                WtfGlobal.setAjaxTimeOut();
                Wtf.Ajax.requestEx({
                    url: "INVGoodsTransfer/deleteInterStoreTransferRequest.do",
                    params: {
                        requestIds: requestIds.toString()
                    }
                },
                this,
                function(result, req){
                    WtfGlobal.resetAjaxTimeOut();
                    if(result.success) {
                        var msg = result.msg;
                        WtfComMsgBox(["Success", msg], 0);
                
                        this.ds.reload();
                    }else if(result.msg){
                      WtfComMsgBox(["Inter Store Transfer",result.msg],result.success*2+2);

                    }else{
                        WtfComMsgBox(["Failure", result.msg], 1);
                    }
            
            
                }, function(){
                    WtfGlobal.resetAjaxTimeOut();
                    WtfComMsgBox(["Error", "Error occurred while processing your request "], 1);
                });
            }
        }, this);
    },
    loadgrid : function(){
        //        if(!this.monthCal.isValid()){
        //            //var archivalDate=Date.parseDate(Wtf.archivalDate,"Y-m-d").add(Date.MONTH, 1);
        //            WtfComMsgBox(["Info", "The selected month field should be equal to or greater than "+archivalDate.format('F Y')], 1);
        //            return;
        //        }

        this.ds.baseParams = {
            // flag: 24,
            type:this.reportType//changes variable name 'type' to 'reportType' because 'type' already used while export data.
        //salesmonth:this.monthCal.getValue().format('m Y')
        }
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
            //salesmonth:this.monthCal.getValue().format('m Y')
            }
        });
    
    },

    initloadgridstore:function(frm, to,storeid){
        //changes variable name 'type' to 'reportType' because 'type' already used while export data.
        this.ds.baseParams = {
            //flag: 24,
            type:this.reportType,
            frmDate:this.reportType === 2?frm:'',
            toDate:this.reportType === 2?to:'',
            storeid:storeid,
            jobWorkStockOut:this.jobWorkStockOut
        // dmflag:this.dmflag
        }
        this.ds.load({
            params:{
    
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss:this.grid.quickSearchTF.getValue()
            }
        });
    },
    loadgridstore:function(frm, to){
        //changes variable name 'type' to 'reportType' because 'type' already used while export data.
        this.ds.baseParams = {
            // flag: 24,
            type:this.reportType,
            frmDate:this.reportType === 2?frm:'',
            toDate:this.reportType === 2?to:'',
            storeid:this.storeCmbfilter.getValue(),
            jobWorkStockOut:this.jobWorkStockOut
        //dmflag:this.dmflag
        }
        this.ds.load({
            params:{
                        
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
                ss:this.grid.quickSearchTF.getValue()
            }
        });
    },
    remarkfunction:function(type){
        var selected = this.sm.getSelections();

        if(selected.length>0){
            this.addEditWin = new Wtf.Window({
                title : "Remark",
                modal : true,
                iconCls : 'iconwin',
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
                    html : getTopHtml("Please Enter Remark","Fill following information",'images/createuser.png')/*upload52.gif')*/
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
                            //url: "jspfiles/inventory/pettyCash.jsp?flag=2&",
                            waitMsgTarget: true,
                            method : 'POST',
                            border : false,
                            bodyStyle : 'font-size:10px;',
                            labelWidth : 110,
                            items : [
                            this.reqTypeName = new Wtf.form.TextArea({
                                fieldLabel:'Enter Remark*',
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
                    text : 'Submit',
                    id:'interstoretransReq',
                    tooltip: {
                        text:"Click to Submit"
                    },
//                    iconCls:'pwnd ReasonSubmiticon caltb',
                    scope : this,
                    handler: function(){
                        if(this.reqTypeName.getValue().trim() == ""){
                            WtfComMsgBox(["Alert","Please enter remark"], 0);
                        }else {
                            //      Wtf.getCmp('interstoretransReq').disabled=true;
                            //            Wtf.getCmp('interstoretransReq').disable();
                            this.acceptFunction(type,this.NewlineRemove(this.reqTypeName.getValue()));
                            //     Wtf.getCmp('interstoretransReq').disabled=false;
                            Wtf.getCmp('rmrkwindow').close();
                        }

                    }
                },{
                    text : 'Cancel',
                    tooltip: {
                        text:"Click to Cancel"
                    },
//                    iconCls:'pwnd rejecticon caltb',
                    scope : this,
                    minWidth:75,
                    //id: 'frmChangePassCancelBtn',
                    handler : function() {
                        Wtf.getCmp('rmrkwindow').close();
                    }
                }]
            }).show();
        }else{
            WtfComMsgBox(["Alert","Please select a record."], 0);
        }
    },
    exportReport: function(reportid, exportType){
        //this.limit = this.pg.pageSize;
        var recordCnt = this.grid.store.getTotalCount();
        if(recordCnt == 0)
        {
            //  WtfComMsgBox(["Error", "No records to export"], 0,1);
            return;
        }
        var repColModel = this.grid.getColumnModel();
        var numCols = this.grid.getColumnModel().getColumnCount();
        var colHeader = "[";
        for(var i = 1;i<numCols;i++){ // skip row numberer
            if(!(repColModel.isHidden(i))){
                colHeader += "{\"displayField\":\""+repColModel.getColumnHeader(i)+"\",";
                colHeader += "\"valueField\":\""+repColModel.getDataIndex(i)+"\"},";
            }
        }

        colHeader = colHeader.substr(0,colHeader.length-1)+"]";
        var url =  "ExportDataServlet.jsp?" +"&mode=" + reportid +
        "&colHeader=" + colHeader+
        "&storeid=" + this.storeCmbfilter.getValue()+
        "&type="+this.reportType+//changes variable name 'type' to 'reportType' because 'type' already used while export data.
        "&reportname=" + this.title +
        "&exporttype=" + exportType +                                         
        "&frmDate=" + this.frmDate.getValue().format(Wtf.getDateFormat())+
        "&toDate=" + this.toDate.getValue().format(Wtf.getDateFormat()) +
        "&dmflag="+ this.dmflag+
        "&ss="+ this.grid.quickSearchTF.getValue();
        //changes variable name 'type' to 'reportType' because 'type' already used while export data.                                
        if(this.reportType==1 || this.reportType==3){
            url =  "ExportDataServlet.jsp?" +"&mode=" + reportid +
            "&colHeader=" + colHeader+
            //"&storeid=" + this.storeCmbfilter.getValue()+
            "&type="+this.reportType+//changes variable name 'type' to 'reportType' because 'type' already used while export data.
            "&reportname=" + this.title +
            "&exporttype=" + exportType +
            "&salesmonth=" + this.monthCal.getValue().format('m Y') +
            //"&frmDate=" + this.frmDate.getValue().format(Wtf.getDateFormat())+
            //"&toDate=" + this.toDate.getValue().format(Wtf.getDateFormat()) +
            "&ss="+ this.grid.quickSearchTF.getValue();
        }
        setDldUrl(url);
    }
    ,
    //    Print:function(){
    //        var selected= this.grid.getSelectionModel().getSelections();
    //        var cnt = selected.length;
    //        var arr=[];
    //        var transdate;
    //        for(var i=0;i<cnt;i++){
    //            var jObj = {}
    //            transdate=selected[i].get("date");
    //            jObj.quantity=selected[i].get("quantity");
    //            jObj.fromstoreid=selected[i].get("fromstore");
    //            jObj.tostoreid=selected[i].get("tostore");
    //            jObj.fromlocationid=selected[i].get("fromlocationid");
    //            jObj.tolocationid=selected[i].get("tolocationid");
    //            jObj.remark=selected[i].get("remark");
    //            jObj.itemid=selected[i].get("itemid");
    //            jObj.uom=selected[i].get("uomname");
    //            jObj.id=selected[i].get("id");      
    //            arr.push(jObj);
    //        }
    //         var str = "jspfiles/inventory/printOut.jsp?transid=''";
    //                        str += "&transdate="+transdate;
    //                        str += "&type=4&flag=48&start=0&limit=0";
    //                        window.open(str, "mywindow","menubar=1,resizable=1,scrollbars=1");
    //    },
    acceptFunction:function(type,alltoDfLocation){
        
        var selected = this.sm.getSelections();

        if(selected.length>0){
            var tmp="";
            if(type=="reject")
            {
                tmp=" all ";
            }
            else
            {
                tmp=" selected ";
            }

            Wtf.MessageBox.confirm("Confirm","Are you sure you want to "+type+tmp+"items?", function(btn){
                if(btn == 'yes') {
                    this.el.mask('Submitting...', 'loadingMask');
                    var jsondata = "";
                    var sep="";
                    var transdate="";
                    var finalStr="";
                    var requestIdArray = new Array();
     
                    if( type=='reject'){
                        for (var i=0;i<selected.length;i++){
               
                            requestIdArray.push(selected[i].get("id"));
                        }
              
                        finalStr += requestIdArray.toString();
                    }else{
                        for (var i=0;i<selected.length;i++){
                            
                            var jObj = {};
                            transdate=selected[i].get("date");
                            jObj.id=selected[i].get("id");
                            jObj.quantity=selected[i].get("quantity");
                            jObj.stockDetails=selected[i].get("stockDetailsForAccept");
                            
                            jsondata += JSON.stringify(jObj)+",";
                            var trmLen = jsondata.length - 1;
                            var finalStr = jsondata.substr(0,trmLen);
                        
                        }
                    }
                    this.sendInterStoreTransfer(type, finalStr, transdate,alltoDfLocation);
                }else if(btn == 'no') {
                    this.el.unmask('Submitting...', 'loadingMask');
                    return;
                }
            },this);
        }else{
            WtfComMsgBox(["Alert","Please select a record"], 0);
        }


    //          Wtf.Ajax.requestEx({
    //            url:"jspfiles/inventory/store.jsp",
    //            params: {
    //                flag:30,
    //                jsondata:jsondata
    //            }
    //        }, this,
    //        function(response){
    //            WtfComMsgBox(["Success","Data Accept Successfully"],0);
    //            Wtf.getCmp("as").remove(this,true);
    //        },
    //        function(response){
    //            WtfComMsgBox(["Error","Error in connecting to server"],1);
    //            Wtf.getCmp("as").remove(this,true);
    //        })
    //        alert(arr[0].data.quantity);
    //        this.ds.commitChanges();
    //        alert("Accept");
    },
    
    sendInterStoreTransfer:function(type, finalStr, transdate,alltoDfLocation){
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url: type =='accept'?"INVGoodsTransfer/acceptInterStoreTransferRequest.do":"INVGoodsTransfer/rejectInterStoreTransferRequest.do",
            params: {
                // flag: 30,
                type: type,
                jsondata: finalStr,
                qaApprove:false,
                remark: type =='accept'? "" :this.NewlineRemove(this.reqTypeName.getValue()),
                colltoDfLocation:alltoDfLocation
            //transdate: transdate
            }
        },
        this,
        function(result, req){
            WtfGlobal.resetAjaxTimeOut();
            // var retstatus = eval('(' +result+ ')');
            if(result.success) {
                var msg = result.msg;
                this.el.unmask('Submitting...', 'loadingMask');
                WtfComMsgBox(["Success", msg], 0);
                if(Wtf.getCmp('interstockcollectionwindow') != undefined){
                    Wtf.getCmp('interstockcollectionwindow').close();
                }
            }else{
                this.el.unmask('Submitting...', 'loadingMask');
                WtfComMsgBox(["Failure", result.msg], 1);
            }
            this.ds.reload();
            
        }, function(){
            WtfGlobal.resetAjaxTimeOut();
            WtfComMsgBox(["Error", "Error occurred while processing"], 1);
        });
    },
    processReturnRequest:function(selected){
        var reqId=selected[0].get("id");
                if(Wtf.account.companyAccountPref.activateQAApprovalFlow && Wtf.account.inventoryPref.interStoreQA && selected[0].data.isQAEnable){
                    Wtf.Msg.show({
                        title:"Confirm",
                        msg: "Do You want this Returned item(s) to go through QA Inspection Process ?",
                        buttons: Wtf.Msg.YESNOCANCEL,  // used YESNOCANCEL as on closing window it set default btn value to no 
                        scope:this,
                        fn: function(btn){
                            if(btn == 'yes') {
                                Wtf.Ajax.requestEx({
                                    url: "INVGoodsTransfer/getISTStockDetail.do",
                                    params: {
                                        orderId:reqId
                                    }
                                },
                                this,
                                function(result) {
                                    if(result.success) {
                                        var serialArr = [];
                                        var dataArr=result.data;
                                        for(var i=0 ; i< dataArr.length; i++){
                                            var data = dataArr[i];
                                            var serials = data.issuedSerials.split(",");
                                            for(var j=0 ; j< serials.length ; j++){
                                                if(serials[j] != undefined && serials[j] != "" && serials[j] != null){
                                                    serialArr.push(serials[j]);
                                                }
                                            }
                                        }
                                        if(serialArr.length > 0){
                                            this.openSerialSelectionWindow(reqId, serialArr);
                                        }else{
                                            this.acceptReturnRequest(reqId,true,"");
                                        }

                                    }else{
                                        WtfComMsgBox(["Error","Error occurred while processing"],1);
                                    }
                                }, function(){
                                    //                        this.loadMask.hide();
                                    WtfComMsgBox(["Error","Error occurred while processing"],1);
                                });

                            }
                            if(btn == 'no') {
                                this.acceptReturnRequest(reqId,false,"");
                            }
                        }
                    });
                }else{
                    this.acceptReturnRequest(reqId,false,"");
                }
    },
    openSerialSelectionWindow: function(returnRequestId, serialArr){
        this.winTitle="Select serial for approval";
        this.winDescriptionTitle= "Select serial for approval";
        this.winDescription="Select serial to send for apprval" ;
        var gridStore = new Wtf.data.SimpleStore({
            fields:['serial']
        });
        if(serialArr != '' && serialArr != null && serialArr != undefined){
            var serialCmbData = [];
            for(var i=0 ; i<serialArr.length ; i++){
                serialCmbData.push([serialArr[i]])
            }
            gridStore.loadData(serialCmbData)
        }
        var sm = new Wtf.grid.CheckboxSelectionModel({
            width:25
        });
        var cm = new Wtf.grid.ColumnModel([
            sm,
            new Wtf.grid.RowNumberer(),
            {
                header:"Serial",
                dataIndex:"serial"
            }]);
        
        this.serialGrid=new Wtf.grid.GridPanel({
            region: 'center',
            border: false,
            store: gridStore,
            cm: cm,
            sm:sm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true
            }
        })
        
        
        var serialSelectionWindow = new Wtf.Window({
            id:'approvalserialselectionwindowid',
            title : this.winTitle,
            modal : true,
            scope:this,
            iconCls : 'iconwin',
            minWidth:100,
            width : 300,
            height: 300,
            resizable :true,
            scrollable:true,
            buttonAlign : 'right',
            layout : 'border',
            items :[{
                region : 'center',
                border : false,
                //                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 0px 0px 0px;',
                layout : 'fit',
                items : [this.serialGrid]
            }],
            buttons :[{
                text : 'Submit',
                iconCls:'pwnd ReasonSubmiticon caltb',
                scope : this,
                handler: function(){  
                    var recs = this.serialGrid.getSelectionModel().getSelections();
                    var serialArr = []
                    for(var i=0 ; i<recs.length ; i++){
                        serialArr.push(recs[i].get('serial'));
                    }
                    Wtf.getCmp('approvalserialselectionwindowid').close();
                    this.acceptReturnRequest(returnRequestId,true,serialArr);
                }
            },{
                text : 'Cancel',
                iconCls:'pwnd rejecticon caltb',
                minWidth:75,
                scope:this,
                handler : function() {
                    Wtf.getCmp('approvalserialselectionwindowid').close();
                }
            }]
        }).show();
    },
    acceptReturnRequest:function(reqId,qaApprove,serialArr){
        Wtf.Ajax.requestEx({
            url:"INVGoodsTransfer/acceptISTReturnRequest.do",
            params: {
                requestId :reqId,
                qaApprove:qaApprove,
                serialNames:serialArr.toString()
            }
        },this,
        function(res,action){
            if(res.success==true){
                WtfComMsgBox(["Success", res.msg],0);
            }else{
                WtfComMsgBox(["Error", "Error occurred while fetching data."],0);
                return;
            }
            this.ds.reload();
        },
        function() {
            WtfComMsgBox(["Error", "Error occurred while processing"],1);
        }
        );   
    },
    statesaveFunction:function(){
    //alert("state Save");
    },
    validateeditFunction:function(e){
        //alert("Validate function");
     
        if(e.value > this.quanarr[e.row] || e.value < 0){
            Wtf.Msg.alert("Warning","Quantity can't be more than "+this.quanarr[e.row] + ".");
            return false;
        }
        if(e.value=="" || e.value==0)
        {
            Wtf.Msg.alert("Warning","Quantity can't be empty or 0.")
            return false;
        }

    },
    
    NewlineRemove : function(str){
        // str = Wtf.util.Format.stripScripts(str);
        if (str)
            return str.replace(/\n/g, ' ');
        else
            return str;
    },
    printRecordTemplate:function(printflg,item){
        var recordbillid="";
        var selected= this.sm.getSelections();
        var cnt = selected.length;
        var transfernoteno="";
        for(var i=0;i<cnt;i++){
            transfernoteno=selected[i].get("transfernoteno");
            recordbillid=selected[i].json.id;
        }
        var params= "myflag=order&order&transactiono="+transfernoteno+"&moduleid="+Wtf.Acc_InterStore_ModuleId+"&templateid="+item.id+"&recordids="+recordbillid+"&filetype="+printflg;  
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "ACCExportPrintCMN/exportSingleInterStoreTransfer.do";
        var inputs =params.split('&');
        for(var i=0;i<inputs.length;i++){
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = KV_pair[1];
            mapForm.appendChild(mapInput); 
        }
        document.body.appendChild(mapForm);
        mapForm.submit();
        var myWindow = window.open("", "mywindow","menubar=1,resizable=1,scrollbars=1");
        var div =  myWindow.document.createElement("div");
        div.innerHTML =  WtfGlobal.getLocaleText("acc.field.LoadingMask");
        myWindow.document.body.appendChild(div);
        mapForm.remove();
    },
    showStockDetailWindow : function (record){
        var orderId=record.get("id");
        var itemId=record.get("itemId");
        var itemCode=record.get("itemcode");
        var quantity=record.get("quantity");
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isRackEnable = record.get("isRackForProduct");
        var isRowEnable = record.get("isRowForProduct");
        var isBinEnable = record.get("isBinForProduct");
        var transferToStockUOMFactor=record.get("transferToStockUOMFactor");
        var transferUOMName=record.get("transferinguomname");
        var stockUOMName=record.get("stockuomname");
        var fromStoreId=record.get("fromStoreId");
        var toStoreId=record.get("toStoreId");
        var fromStoreName=record.get("fromstorename");
        var toStoreName=record.get("tostorename");
        var maxQtyAllowed=transferToStockUOMFactor * quantity;
        
        var winTitle = "Stock Detail for Stock Transfer";
        var winDetail = String.format('Select Stock details for stock transfer  <br> <b>Product :</b> {0}<br> <b>Issuance Store :</b> {1}<br> <b>Collection Store :</b> {2}<br> <b>Quantity :</b> {3} {4} ( {5} {6} )', itemCode, fromStoreName, toStoreName, quantity, transferUOMName, maxQtyAllowed, stockUOMName);
        
        this.detailWin = new Wtf.StockTransferDetailWin({
            WinTitle : winTitle,
            WinDetail: winDetail,
            TotalTransferQuantity: maxQtyAllowed,
            ProductId:itemId,
            FromStoreId: fromStoreId,
            ToStoreId: toStoreId,
            isBatchForProduct: isBatchEnable,
            isSerialForProduct : isSerialEnable,
            isRowForProduct: isRowEnable,
            isRackForProduct: isRackEnable,
            isBinForProduct: isBinEnable,
            GridStoreURL:"INVGoodsTransfer/getISTIssuedDetailList.do",         
            GridStoreExtraParams: {
                requestId : orderId
            },
            StockDetailArray:record.get("stockDetails"),
            DataIndexMapping:{
                detailId: 'detailId',
                toLocationId:"locationId",
                toRowId:"rowId",
                toRackId:"rackId",
                toBinId:"binId",
                serials:"serialNames"
            },
            buttons:[{
                text:"Save",
                handler:function (){
                    if(this.detailWin.validateSelectedDetails()){
                        var detailArray = this.detailWin.getSelectedDetails();

                        record.set("stockDetailsForAccept","");
                        record.set("stockDetailsForAccept",detailArray);
                        
                        this.acceptFunction("accept","");
                        this.detailWin.close();
                    }else{
                        return;
                    }
                },
                scope:this
            },{
                text:"Cancel",
                handler:function (){
                    this.detailWin.close();
                },
                scope:this
            }]
        })
        this.detailWin.show();
    },
    interstockcollectionwindow : function(grid){
        
        var selected=grid.getSelections();

        this.orderId=selected[0].get("id");
        this.transferNoteNo= selected[0].get("transfernoteno");
        this.itemId1= selected[0].get("itemId");
        this.itemcode= selected[0].get("itemcode");
        this.isBatchForProduct=selected[0].get("isBatchForProduct");
        this.isSerialForProduct=selected[0].get("isSerialForProduct");
        this.fromStoreId=selected[0].get("fromStoreId");
        this.toStoreId=selected[0].get("toStoreId");
        this.currentRowNo=grid.store.indexOf(selected[0]);
        this.fromStoreName=selected[0].get("fromstorename");
        this.toStoreName=selected[0].get("tostorename");
        this.quantity=selected[0].get("quantity");
        this.transferToStockUOMFactor=selected[0].get("transferToStockUOMFactor");
        this.transferUOMName=selected[0].get("transferinguomname");
        this.stockUOMName=selected[0].get("stockuomname");
        this.qtyToBeFilled=this.quantity*this.transferToStockUOMFactor;
        
        this.locCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },        

        {
            name: 'name'
        }]);

        this.locCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
        
        this.locCmbStore.load({
            params:{
                storeid:this.toStoreId
            }
        });
            
        this.locCmb = new Wtf.form.ComboBox({
            fieldLabel : 'To Location*',
            hiddenName : 'tolocationid',
            store : this.locCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select location...'
        });
        
        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            decimalPrecision:4,//Wtf.companyPref.quantityDecimalPrecision,
            allowNegative:false
        })
        
        this.serialCmbStore = new Wtf.data.SimpleStore({
            fields:['serialnoid','serial'], 
            pruneModifiedRecords:true
        });
   
        this.serialCmb =new Wtf.common.Select({
            fieldLabel:"<span wtf:qtip='"+"Serial" +"'>"+"Serial"+"</span>",
            hiddenName:'serialid',
            name:'serialid',
            store : this.serialCmbStore,
            xtype:'select',
            valueField:'serial',
            displayField:'serial',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            mode: 'local',
            triggerAction:'all',
            typeAhead: true,
            emptyText:'Select Serial...'
        }); 
      
        this.collectionCm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:"Order ID",
                dataIndex:"id",
                hidden :true
            },
            {
                header:"Order Note No.",
                dataIndex:"transfernoteno",
                hidden :true
            },
            {
                header:"Product ID",
                dataIndex:"itemcode",
                hidden :true
            },
            {
                header:"Stock Detail ID",
                dataIndex:"stockdetailid",
                hidden:true
            },
            {
                header:"IssuedLocationID",
                dataIndex:"tolocationid",
                hidden:true
            },
            {
                header:"Issued from Location",
                dataIndex:"tolocationname",
                hidden:false
            },
            {
                header:"Issued Quantity",
                dataIndex:"issuedQty",
                hidden:false
            },
            {
                header:"Batch",
                dataIndex:"batch",
                // editor:this.batchCmb,
                hidden : (this.isSerialForProduct==true) ? false :true
            //renderer:this.getComboRenderer(this.batchCmb)
            },
            {
                header:"Collect to Location*",
                dataIndex:"fromLocationId",
                editor:this.locCmb,
                renderer:this.getComboRenderer(this.locCmb)
            },
            {
                header:"Collect Quantity*",
                dataIndex:"collectQty",
                editor:this.quantityeditor
            },
            {
                header:"isBatchEnable",
                dataIndex:"isBatchForProduct",
                hidden:true
            },
            {
                header:"isSerialEnable",
                dataIndex:"isSerialForProduct",
                hidden:true
            },
            {
                header:"Issued Serials",
                dataIndex:"serial",
                hidden:true
            //editor:this.serialCmb
            //renderer:this.getComboRenderer(this.serialCmb)
            },
            {
                header:"Serial",
                dataIndex:"selectedSerials",
                editor:this.serialCmb,
                hidden : (this.isBatchForProduct==true) ? false :true
            //renderer:this.getComboRenderer(this.serialCmb)
            },
            
            ]);
      
        this.collectionGridStore = new Wtf.data.SimpleStore({
            fields:['id','transfernoteno','itemid','itemcode','stockdetailid','issuedQty','tolocationid','tolocationname','isBatchForProduct',
            'isSerialForProduct','batch','serial','selectedSerials','fromLocationId','collectQty'], 
            pruneModifiedRecords:true
        });  //fromLocationId = collect locationid  ,tolocationid = issue locationid
           
    
        //   fields:['id','transfernoteno','itemid','itemcode','stockdetailid''issuedQty','tolocationid','tolocationname','isBatchForProduct',
        //           'isSerialForProduct','batch','serial','selectedSerials','fromLocationId','collectQty'], 
        
    
    
        //now get issued stock detail(its location,issued batch,serials and qty)
           
        Wtf.Ajax.requestEx({
            url:"INVGoodsTransfer/getISTStockDetail.do",
            params: {
                orderId : this.orderId
            }
        },this,
        function(res,action){
            if(res.success==true){
                var totalRec=res.data.length;
  
                this.collectionGridStoreArr=[];
                this.collectionGridStore.removeAll(); 
        
                for(var i=0;i<totalRec;i++){
                            
                    this.collectionGridStoreArr.push([this.orderId,this.transferNoteNo,this.itemId1,this.itemcode,res.data[i].id,
                        res.data[i].issuedQuantity,res.data[i].issuedLocationId,res.data[i].issuedLocationName,this.isBatchForProduct,
                        this.isSerialForProduct,res.data[i].issuedBatch,res.data[i].issuedSerials,"","",0]);
                            
                }
                this.collectionGridStore.loadData(this.collectionGridStoreArr);
                 
            }else{
                WtfComMsgBox(["Error", "Error occurred while fetching data."],0);
                return;
            }
                
        },
        function() {
            WtfComMsgBox(["Error", "Error occurred while processing"],1);
        }
        );   
   
  
        
        this.interstockCollectionGrid = new Wtf.grid.EditorGridPanel({
            cm:this.collectionCm,
            region:"center",
            id:"editorgrid2sd",
            autoScroll:true,
            store:this.collectionGridStore,
            viewConfig:{
                forceFit:true,
                emptyText:"No Data to Show."
            },
            clicksToEdit:1
        });
        
        
        this.winTitle="Select Quantity,Location"+(this.isBatchForProduct ? ",Batch" : "")+(this.isSerialForProduct ? ",Serial" : "");
        this.winDescriptionTitle= this.winTitle+" for following item<br/>";
        this.winDescription="<b>Order Note No. : </b>"+this.transferNoteNo+"<br/>"
        +"<b>Product Code : </b>"+this.itemcode+"<br/>"
        +"<b>From Store : </b>"+this.fromStoreName+"<br/>"
        +"<b>To Store  : </b>"+this.toStoreName+"<br/>"
        +"<b>Quantity : </b>"+ this.quantity +" "+this.transferUOMName+" ( "+ this.qtyToBeFilled +" "+ this.stockUOMName +" )<br/>" ;
       
        
      
        this.collectWindow = new Wtf.Window({
            title : this.winTitle,
            modal : true,
            iconCls : 'iconwin',
            minWidth:75,
            width : 950,
            height: 500,
            resizable :true,
            scrollable:true,
            id:"interstockcollectionwindow",
            buttonAlign : 'right',
            layout : 'border',
            items :[{
                region : 'north',
                height : 122,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml(this.winDescriptionTitle,this.winDescription,'images/accounting_image/add-Product.gif')/*upload52.gif')*/
            },{
                region : 'center',
                border : false,
                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:0px 0px 0px 0px;',
                layout : 'fit',
                items : [{
                    border : false,
                    bodyStyle : 'background:transparent;',
                    layout : "border",
                    items : [
                    {
                        region : 'center',
                        layout : 'fit',
                        border : false,
                        items: this.interstockCollectionGrid 
                    }
                       
                    ]
                }]
            }],
            buttons :[{
                text : 'Accept',
                iconCls:'pwnd ReasonSubmiticon caltb',
                scope : this,
                handler: function(){  
                   
                    var isValid=this.validateFilledData();
                    
                    if(isValid==true){
                        var jsonData=this.makeJSONData();
                        var rec=grid.getStore().getAt(this.currentRowNo);
                        rec.set("stockDetailsForAccept",jsonData);
                        //                        Wtf.getCmp('interstockcollectionwindow').close();
                        this.acceptFunction("accept","");
                    }
                }
            },{
                text : 'Cancel',
                scope : this,
                iconCls:'pwnd rejecticon caltb',
                minWidth:75,
                handler : function() {
                    Wtf.getCmp('interstockcollectionwindow').close();
                }
            }]
        }).show();
        
        Wtf.getCmp("interstockcollectionwindow").doLayout();
            
        this.interstockCollectionGrid.on("beforeedit",this.collectBeforeEdit,this);
        this.interstockCollectionGrid.on("afteredit",this.collectAfterEdit,this);
    },
      
    collectBeforeEdit :function(e){
        
        var rec=e.record;
        
        if(e.record.data.isBatchForProduct == false && (e.field =='batch')) {
            return false;
        }
        if(e.record.data.isSerialForProduct == false && (e.field =='selectedSerials')) {
            return false;
        }
        if(e.field =='selectedSerials' &&  e.record.data.collectQty==0) {
            WtfComMsgBox(["Warning", "Please Fill quantity first."],0);
            return false;
        }
        if(e.field =='selectedSerials' &&  e.record.data.collectQty > 0) {
            
            this.serialStoreArr=[];
            
            var issuedSerials=rec.data.serial;
            var SerialArr=[];
            if(issuedSerials != "" && issuedSerials != undefined){
                SerialArr=issuedSerials.split(",");
                SerialArr.sort();
                for(var i=0;i<SerialArr.length;i++){
                    this.serialStoreArr.push([SerialArr[i],SerialArr[i]]);
                }
                this.serialCmbStore.loadData(this.serialStoreArr);
            }
        }
            
    },
    
    collectAfterEdit :function(e){
        if(e.field =='collectQty') {
            if(e.record.data.collectQty > e.record.data.issuedQty){
            //                var rec=e.record;
            //                rec.set("collectQty",e.record.data.issuedQty);
            //                return false;
            }
            
            if(e.record.data.collectQty==0 && e.record.data.isSerialForProduct == true){  //if  edited afterwards case
                var rec=e.record;
                rec.set("selectedSerials","");
                return false;
            }
            
            var totalRec=this.interstockCollectionGrid.getStore().getTotalCount();
            var enteredTotalQty=0;
            
            for(var i=0; i < totalRec;i++){
                var currentRec=this.interstockCollectionGrid.getStore().getAt(i);
                enteredTotalQty += currentRec.get("collectQty");
            }
            if(enteredTotalQty > this.qtyToBeFilled){
                WtfComMsgBox(["Warning", "Entered total quantity cannot be greater than total Collected quantity."],0);
                var record=e.record;
                record.set("collectQty",0);
                return false;
            }
        }
        
        if(e.field =='selectedSerials' && (e.record.data.selectedSerials !="" && e.record.data.selectedSerials !=undefined)) {
            var rowRec=e.record;
            var maxSerialSelectionAllowed=rowRec.data.collectQty;
            var selectedSerialList=e.record.data.selectedSerials;
            var separatedSerialArr=selectedSerialList.split(",");
            if(separatedSerialArr.length > maxSerialSelectionAllowed){
                rowRec.set("selectedSerials","");
                //                WtfComMsgBox(["Warning", "You can select maximum "+maxSerialSelectionAllowed+" Serials from list."],0);
                WtfComMsgBox(["Warning", "Quantity and selected serial numbers count must be same"],0);
                return false;
            }
        
        }
    },
    validateFilledData : function(){
        var recs=this.interstockCollectionGrid.getStore().getModifiedRecords();
        var isSerialForProduct=false;
        var quantity=0;
        var serial="";
        var locationid="";   
        var totalQty=0;
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                isSerialForProduct=this.isSerialForProduct; 
                quantity=recs[k].get("collectQty");
                serial=recs[k].get("selectedSerials");
                locationid=recs[k].get("fromLocationId");
                 
                totalQty += quantity;  
                
                if(locationid == undefined || locationid == ""){
                    WtfComMsgBox(["Warning", "Please select Location."],0);
                    return false;
                }         
                
                if(locationid != undefined && locationid != "" && quantity==0){
                    WtfComMsgBox(["Warning", "Please enter quantity."],0);
                    return false;
                }
                
                if(quantity>0){ // ie.case for -ve qty .
                    if(isSerialForProduct==true){
                        var serialsArr=[];
                        if(serial !=undefined && serial != ""){
                            serialsArr=serial.split(",");
                        }
                        if(serialsArr.length != quantity && quantity!=0){
                            WtfComMsgBox(["Warning", "Please Select "+quantity+" serials as per quantity."],0);
                            return false;
                        }
                    }
                   
                }
            }
            if(totalQty != this.qtyToBeFilled){
                WtfComMsgBox(["Warning", "Please fill total <b>"+this.qtyToBeFilled+" </b> Quantity."],0);
                return false;
            }
            this.quantity=totalQty;
            return true;
            
        }else{
            WtfComMsgBox(["Warning", "Please Select  Location."],0);
            return false;
        }
        
    },
    makeJSONData : function(){
        var recs=this.interstockCollectionGrid.getStore().getModifiedRecords();
        var jArray=[]; 
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
                var jsondata = {};
                var detailId=recs[k].get("stockdetailid");
                var batch=recs[k].get("batch");
                var locationid=recs[k].get("fromLocationId");  
                var quantity=recs[k].get("collectQty");
                var serial=recs[k].get("selectedSerials");
                
                if(quantity != 0){
                    jsondata.detailId=detailId;
                    jsondata.locationId=locationid;
                    jsondata.batchName=batch;
                    jsondata.serialNames=serial;
                    jsondata.quantity=quantity;
                    jArray.push(jsondata);
                }
            }
        }
        return jArray;
    },
    getComboRenderer : function(combo){
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1){
                idx = combo.store.find(combo.displayField, value);
            }
            if(idx == -1)
                return value;
            var rec = combo.store.getAt(idx);
            var valueStr = rec.get(combo.displayField);
            return "<div wtf:qtip=\""+valueStr+"\">"+valueStr+"</div>";
        }
    }
});
