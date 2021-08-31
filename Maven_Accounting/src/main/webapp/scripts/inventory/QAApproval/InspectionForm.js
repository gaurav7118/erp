
function inspectionTab(record,module,store,apprlID,isJobWorkOrder, config){
    //    alert(record.data["status"]);
    this.approve = new Wtf.Button({
        text: 'Approve',
        iconCls:'pwnd save',
        scope:this,
        hidden:record.data["status"]=="PENDING"?false:true,
        handler:function(){
            this.inspectionwin.saveData("Approve");
        }
    });
    this.rejectbtn = new Wtf.Button({
        text: 'Reject',
        iconCls:'pwnd save',
        scope:this,
        hidden:record.data["status"]=="PENDING"?false:true,
        handler:function(){
            this.inspectionwin.saveData("Reject");
        }
    });
        
    this.cancelBtn = new Wtf.Button({
        text: 'Cancel',
        //        iconCls:getButtonIconCls(Wtf.etype.menudelete),
        scope:this,
        handler:function(){
            if(this.inspectionwin != null || this.inspectionwin.close() != undefined){
                this.inspectionwin.close();  
            }
                    
        }
    });
    this.printBtn = new Wtf.Button({
        text: 'Print',
        hidden: config ? config.isTempSave : false,//if call from SO then hide button
        //        iconCls:getButtonIconCls(Wtf.etype.menudelete),
        scope:this,
        handler:function(){
        
            var inspectionDate=this.inspectionwin.inspectionDate.getValue().format("Y-m-d");
            var modelNo=this.inspectionwin.modelName.getValue();
            var insepector= this.inspectionwin.inspector.getValue();
            var refNo= this.inspectionwin.refNo.getValue();
            var consignmentReturnNo =this.inspectionwin.consignmentReturnNo.getValue();
            var batchNo=this.inspectionwin.batchNo.getValue();
            var serialNo=this.inspectionwin.serialNo.getValue();
            var department=this.inspectionwin.department.getValue();
            var description=this.inspectionwin.description.getValue();
            var customerName=this.inspectionwin.customerName.getValue();
       
            var mainDivStartHtml="<div style='margin-top:20px'>";
        
            var detailTableHtml="<table border=0 cellpadding='2' cellspacing='0' style='width:90%'> "+
            "<tr><td><b>Inspection Date : </b>"+inspectionDate+"</td><td><b>Ref. No. : </b>"+refNo+"</td></tr>"+
            "<tr><td><b>Model Name : </b>"+modelNo+"</td><td><b>"+WtfGlobal.getLocaleText("acc.field.consignmentreturnno")+". : </b>"+consignmentReturnNo+"</td></tr>"+
            "<tr><td><b>Description : </b>"+description+"</td><td><b>Inspector : </b>"+insepector+"</td></tr>"+
            "<tr><td><b>Customer Name </b>: "+customerName+"</td><td><b>Batch No. : </b>"+batchNo+"</td></tr>"+
            "<tr><td><b>Department : </b>"+department+"</td><td><b>Serial No. : </b>"+serialNo+"</td></tr>"+
            "</table>";
            
            /** 
             * if This is Stock Out Approval window ,  MRP and MRP QA is Activated then add passing value and actual value column in Print PDF case.
             **/

            var fillTableHtml=  "<br/><br/><table border=1 cellpadding='2' cellspacing='0' style='width:90%'>";
            if(((config.isStockOutApproval) && (Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP))){
                fillTableHtml +="<tr><th>Inspection Area</th><th>Status</th><th>Faults</th><th>Passing Value</th><th>Actual Value</th></tr>";            
            }else{
                fillTableHtml +="<tr><th>Inspection Area</th><th>Status</th><th>Faults</th></tr>";
            } 
            
            this.inspectionwin.itemlistStore.each(function(rec){
                if(((config.isStockOutApproval) && (Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP))){                    
                    fillTableHtml +="<tr><td>"+rec.get('areaName')+"&nbsp;</td><td>"+rec.get('status')+"&nbsp;</td><td>"+rec.get('faults')+"&nbsp;</td><td>"+rec.get('passingValue')+"&nbsp;</td><td>"+rec.get('actualValue')+"&nbsp;</td></tr>"                    
                }else{                    
                    fillTableHtml +="<tr><td>"+rec.get('areaName')+"&nbsp;</td><td>"+rec.get('status')+"&nbsp;</td><td>"+rec.get('faults')+"&nbsp;</td></tr>"                    
                }
            }, this)
            
            fillTableHtml += "</table>";
                        
            var mainDivEndHtml="</div>";
        
            var disp_setting="toolbar=yes,location=no,";
            disp_setting+="directories=yes,menubar=yes,";
            disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";
            var content_value ="";
            var docprint=window.open("","",disp_setting);
            docprint.document.open();
            docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
            docprint.document.write('"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">');
            docprint.document.write('<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
            docprint.document.write('<head><title></title>');
            docprint.document.write('<style type="text/css">body{ margin:0px;');
            docprint.document.write('font-family:verdana,Arial;color:#000;');
            docprint.document.write('font-family:Verdana, Geneva, sans-serif; font-size:12px;}');
            docprint.document.write('a{color:#000;text-decoration:none;} </style>');
            docprint.document.write('</head><body onLoad="self.print()"><center>');
            docprint.document.write(mainDivStartHtml+ detailTableHtml + fillTableHtml + mainDivEndHtml);
            docprint.document.write('</center></body></html>');
            docprint.document.close();
        //        docprint.focus();
        //        docprint.close();
      
        }
    });
    /**
     * Save button for saving Inspection Form
     */
    this.saveBtn = new Wtf.Button({
        text: WtfGlobal.getLocaleText('acc.common.saveBtn'),
        scope: this,
        hidden: config ? config.isTempSave : false,//if call from SO then hide button
        handler: function(){
            var inspectionDate = this.inspectionwin.inspectionDate.getValue().format("Y-m-d");
            var modelName = this.inspectionwin.modelName.getValue();
            var consignmentReturnNo = this.inspectionwin.consignmentReturnNo.getValue();
            var department = this.inspectionwin.department.getValue();
            var customerName = this.inspectionwin.customerName.getValue();
            var recordId = this.inspectionwin.records.data.id;
            var module = this.inspectionwin.module;
            
            var inspectionAreaDetailsArray = [];
            
            this.inspectionwin.itemlistStore.each(function(rec){
                var areaId = rec.get('areaId');
                var areaName = rec.get('areaName');
                var status = rec.get('status');
                var faults = rec.get('faults');
                var passingValue = rec.get('passingValue');
                var actualValue = rec.get('actualValue');
                
                if(areaName != ""){
                    var inspectionAreaDetailsObj = {};
                    inspectionAreaDetailsObj["areaId"] = areaId;
                    inspectionAreaDetailsObj["areaName"] = areaName;
                    inspectionAreaDetailsObj["status"] = status;
                    inspectionAreaDetailsObj["faults"] = faults;
                    inspectionAreaDetailsObj["passingValue"] = passingValue;
                    inspectionAreaDetailsObj["actualValue"] = actualValue;
                    
                    inspectionAreaDetailsArray[inspectionAreaDetailsArray.length] = inspectionAreaDetailsObj;
                }
            }, this)
            
            Wtf.Ajax.requestEx({
                url:"INVApproval/saveInspectionForm.do",
                params: {
                    recordId : recordId,
                    module : module,
                    inspectionDate : inspectionDate,
                    modelName : modelName,
                    consignmentReturnNo : consignmentReturnNo,
                    department : department,
                    customerName : customerName,
                    inspectionAreaDetails : JSON.stringify(inspectionAreaDetailsArray)
                }
            },
            this,
            function(result, req){
                var msg=result.msg;
                if(result.success){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),msg],0);
                }
            },
            function(result, req){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.failure"), WtfGlobal.getLocaleText("acc.common.error.occurred")],3);
                return false;
            });
        }
    });
    /**
     * Submit button for saving inspection form details as temporary
     */
    this.submitBtn = new Wtf.Button({
        text: WtfGlobal.getLocaleText('acc.common.submit'),
        scope: this,
        hidden: !(config ? config.isTempSave : false),
        handler: function(){
//            var inspectionDate = this.inspectionwin.inspectionDate.getValue().format("Y-m-d");
//            var modelName = this.inspectionwin.modelName.getValue();
//            var consignmentReturnNo = this.inspectionwin.consignmentReturnNo.getValue();
//            var department = this.inspectionwin.department.getValue();
//            var customerName = this.inspectionwin.customerName.getValue();
//            var recordId = this.inspectionwin.records.data.id;
//            var module = this.inspectionwin.module;
//            
            var inspectionAreaDetailsArray = [];
            //Create json of inspection form details
            this.inspectionwin.itemlistStore.each(function(rec){
                var areaId = rec.get('areaId');
                var areaName = rec.get('areaName');
                var status = rec.get('status');
                var faults = rec.get('faults');
                var passingValue = rec.get('passingValue');
                var actualValue = rec.get('actualValue');
                
                if(areaName != ""){
                    var inspectionAreaDetailsObj = {};
                    inspectionAreaDetailsObj["areaId"] = areaId;
                    inspectionAreaDetailsObj["areaName"] = areaName;
                    inspectionAreaDetailsObj["status"] = status;
                    inspectionAreaDetailsObj["faults"] = faults;
                    inspectionAreaDetailsObj["passingValue"] = passingValue;
                    inspectionAreaDetailsObj["actualValue"] = actualValue;
                    
                    inspectionAreaDetailsArray[inspectionAreaDetailsArray.length] = inspectionAreaDetailsObj;
                }
            }, this);
            //set inspection form details json in product grid store
            if(this.inspectionwin.rowindex != undefined){
                this.inspectionwin.productgrid.getStore().getAt(this.inspectionwin.rowindex).data.inspectionAreaDetails = JSON.stringify(inspectionAreaDetailsArray);
            }
            this.inspectionwin.close();
        }
    });
    
    this.inspectionwin = new Wtf.AutoCreateConfigWin1({
        title:"QA Inspection Form",
        layout : 'fit',
        closable : true,
        modal : true,
        width : 800,
        scope:this,
        records:record,
        productgrid:config ? config.productgrid : "",//pass product grid for accessing SO product details
        isTempSave:config ? config.isTempSave : false,//flag for identification of call from SO
        isStockOutApproval:config ? config.isStockOutApproval : false,//flag for identification of call from StockOutApproval.js
        rowindex:config ? config.rowindex : "",//index of row from product grid
        str:store,
        module:module,
        isJobWorkOrder:isJobWorkOrder,
        apprlID:apprlID,
        height: (config ? config.isTempSave : false) ? 500 : 700,//adjust height
        autoScroll:true,
        resizable :false,
        border:false,
        buttons:[
        //        this.approve,this.rejectbtn,
        this.saveBtn, this.submitBtn, this.printBtn,this.cancelBtn
        ]
    }); 
    this.inspectionwin.show();
}

Wtf.AutoCreateConfigWin1 = function (config){
    Wtf.apply(this,config);
    this.isJobWorkOrder = config.isJobWorkOrder;
    Wtf.AutoCreateConfigWin1.superclass.constructor.call(this);
}

Wtf.extend(Wtf.AutoCreateConfigWin1,Wtf.Window,{
    initComponent:function (){
        
        Wtf.AutoCreateConfigWin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        this.GetSouthPanel();
        /**
         * if call from SO then hide global details
         */
        if(!this.isTempSave){
            this.mainPanel = new Wtf.Panel({
                layout:"border",
                items:[
                this.northPanel,
                this.cForm,
                this.southPanel
                ] 
            });
        } else{
            this.mainPanel = new Wtf.Panel({
                layout:"border",
                items:[
//                this.northPanel,
                this.cForm,
                this.southPanel
                ] 
            });
        }
        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        this.inspectionDate = new Wtf.form.DateField({
            fieldLabel: "Inspection Date",
            emptyText: "Select a date...",
            width: 200,
            format: 'Y-m-d l',
            allowBlank: true,
            name: "countingdate1",
            value: new Date(),
            readOnly: false
        });
        this.statusStore = new Wtf.data.SimpleStore({
            fields:["id", "name"],
            data : [["OK", "OK"],["NG", "NG"]]
        });
        this.statusCmb = new Wtf.form.ComboBox({
            hiddenName : 'statusFilter',
            store : this.statusStore,
            typeAhead:true,
            readOnly: false,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 130,
            triggerAction: 'all',
            value: "OK",
            emptyText:'Select status...'
        });      
        this.modelName =new Wtf.form.TextField({
            fieldLabel:"Model Name",
            name:"MODELNAME",
            allowBlank:true,
            width : 200,
            readOnly: false,
            value:this.records.data["productcode"]!=""?this.records.data["productcode"]:""
        });
        this.inspector =new Wtf.form.TextField({
            fieldLabel:"Inspector",
            name:"Inspector",
            allowBlank:true,
            value:this.records.data["supervisor"]!=""?this.records.data["supervisor"]:loginname,
            width : 200,
            readOnly: true,
            editable:false
        });
        this.refNo =new Wtf.form.TextField({
            fieldLabel:"Ref No",
            name:"refno",
            allowBlank:true,
            editable:false,
            width : 200,
            readOnly: true,
            value:this.records.data["transactionno"]!=""?this.records.data["transactionno"]:""
        });
        this.custmerPONo =new Wtf.form.TextField({
            fieldLabel:"Customer PO No",
            name:"pono",
            allowBlank:true,
            width : 200,
            readOnly: false
        });
        this.serialNo =new Wtf.form.TextField({
            fieldLabel:"Serial No",
            name:"serialno",
            allowBlank:true,
            width : 200,
            readOnly: true,
            editable:false,
            value:this.records.data["serialname"]!=""?this.records.data["serialname"]:""
        });
        this.batchNo =new Wtf.form.TextField({
            fieldLabel:"Batch No",
            name:"batchno",
            allowBlank:true,
            width : 200,
            readOnly: true,
            editable:false,
            value:this.records.data["batchname"]!=""?this.records.data["batchname"]:""
        });
        this.department =new Wtf.form.TextField({
            fieldLabel:"Department",
            name:"department",
            allowBlank:true,
            width : 200,
            readOnly: false
        });
        this.description = new Wtf.form.TextField({
            fieldLabel: "Description ",
            readOnly: true,
            editable:false,
            name: "trans mod",
            value:this.records.data["productname"]!=""?this.records.data["productname"]:"",
            width: 200
        });
        this.hospitalName = new Wtf.form.TextField({
            fieldLabel: "Hospital Name ",
            readOnly: false,
            name: "hospitalname",
            value:'',
            width: 200
        });
        this.customerName = new Wtf.form.TextField({
            fieldLabel: "Customer Name ",
            readOnly: false,
            name: "customername",
            value:this.records.data["customer"]!=""?this.records.data["customer"]:"",
            width: 200
        });
        this.consignmentReturnNo = new Wtf.form.TextField({
            fieldLabel: this.isJobWorkOrder?WtfGlobal.getLocaleText("acc.jobworkorder.qaapproval.transactionno"):WtfGlobal.getLocaleText("acc.field.consignmentreturnno"),//"Consignment Return No",
            readOnly: false,
            name: "consignmentreturnno",
            value:this.records.data["transactionno"]!=""?this.records.data["transactionno"]:"",
            width: 200
        });
        var formItems = new Array();
        formItems.push(this.inspectionDate);
        formItems.push(this.modelName);
        formItems.push(this.description);
        formItems.push(this.customerName);
        formItems.push(this.department);
        //        formItems.push(this.hospitalName);
        
        this.northPanel =  new Wtf.form.FormPanel({
            region:"north",
            id: 'newInspectionForm',
            bodyStyle: "background-color:#F6F6F6; margin: 20px 5px 5px 10px;",
            labelWidth:100,
            height: 100,
            border: false,
            layout:"column",
            items:[{
                layout:"form",
                labelWidth:130,
                border: false,
                columnWidth:.5,
                items:formItems
            },{
                layout:"form",
                labelWidth:130,
                border: false,
                columnWidth:.5,
                items: [
                this.refNo,
                this.consignmentReturnNo,
                this.inspector,
                this.batchNo,
                this.serialNo,
                ]
            }]
        });
    
    },
    GetAddEditForm:function (){
        
        this.inspectionheader = new Wtf.form.TextField({
            readOnly: false,
            name: "inspectiontext",
            width: 200
        });
        this.faultsheader = new Wtf.form.TextArea({
            readOnly: false,
            name: "faultstext",
            width: 200
        });
        /**
         * Passing vlaue text field for MRP QA flow
         */
        this.passingValueText = new Wtf.form.TextField({
            readOnly: false,
            name: "passingValueText",
            width: 200
        });
        
        /**
         * Actual value text field for MRP QA flow
         */
        this.actualValueText = new Wtf.form.TextField({
            readOnly: false,
            name: "actualValueText",
            width: 200
        });
        
        
        this.itemlistRecord = new Wtf.data.Record.create([{
            name: 'templateId'
        },{
            name: 'areaId'
        },{
            name: 'areaName'
        },{
            name: 'status'
        },{
            name: 'faults'
        },{
            name: 'passingValue'
        },{
            name: 'actualValue'
        }]);
   
        this.itemlistReader = new Wtf.data.KwlJsonReader({
            root: 'data'
        }, this.itemlistRecord);

        this.itemlistStore = new Wtf.data.Store({
            sortInfo: {
                field: 'areaName',
                direction: "ASC"
            },
            url:'INVTemplate/getInspectionAreaList.do',
            baseParams:{
                templateId:this.records.get("inspectionTemplate") != null ?this.records.get("inspectionTemplate"): null
            },
            reader: this.itemlistReader
        });
        /**
         * If inspection form details are exist for selected row then proceed with that details 
         * otherwise load details from inspection template.
         */
        if(this.productgrid && this.productgrid.store.getAt(this.rowindex).data.inspectionAreaDetails != undefined && this.productgrid.store.getAt(this.rowindex).data.inspectionAreaDetails != ""){
            this.itemlistStore = new Wtf.data.Store({
                url: 'common.jsp',
                fields: ['templateId', 'areaId', 'areaName', 'status', 'faults', 'passingValue','actualValue'],
                data: JSON.parse(this.productgrid.store.getAt(this.rowindex).data.inspectionAreaDetails),
                reader: new Wtf.data.KwlJsonReader({
//                    root: 'data'
                }, this.itemlistRecord)
            });
        }
        this.itemlistStore.on('load', function(){
            var count = this.itemlistStore.getCount();
            if(count == 0){ // by default 10 blank rows in print if no template data
                for(var i=0; i<10 ; i++){
                    this.addNewRow();
                }
            }
        }, this);
        
        // Get Inspection Form with all details
        Wtf.Ajax.requestEx({
            url:"INVApproval/getInspectionForm.do",
            params: {
                recordId : this.records.data.id,
                module : this.module
            }
        },
        this,
        function(result, req){
            if(result.success && result.data.length > 0){ // If Inspection Form present then set and show all details
                this.inspectionDate.setValue(result.data[0].inspectionDate);
                this.modelName.setValue(result.data[0].modelName);
                this.customerName.setValue(result.data[0].customerName);
                this.department.setValue(result.data[0].department);
                this.consignmentReturnNo.setValue(result.data[0].consignmentReturnNo);
                                
                var details = result.data[0].detail;
                
                if(details.length == 0){
                    this.itemlistStore.load();
                } else{
                    for(var ind = 0; ind < details.length; ind++){
                        this.itemlistStore.add(new this.itemlistRecord({
                            templateId : details[ind].templateId,
                            areaId : details[ind].areaId,
                            areaName : details[ind].areaName,
                            status : details[ind].status,
                            faults : details[ind].faults,
                            passingValue : details[ind].passingValue,
                            actualValue :  details[ind].actualValue
                    }));
                    }
                }
            } else{ // If Inspection Form not present then show default inspection template details
                this.itemlistStore.load();
            }
        },
        function(result, req){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.failure"), WtfGlobal.getLocaleText("acc.common.error.occurred")],3);
        });
        
        //        this.filldata();
        this.itemlistCm = new Wtf.grid.ColumnModel([{
            header: "Inspection Area",
            dataIndex: 'areaName',
            editor:this.inspectionheader
        },{
            header: "Status",
            dataIndex: 'status',
            hidden:((this.isTempSave) && (Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP)),
            editor:this.statusCmb
        },{
            header: "Faults",
            dataIndex: 'faults',
            editor:this.faultsheader
        },{            
            /**
             * Column for MRP QA flow
             */
            header: "Passing Value",
            dataIndex: 'passingValue',
            hidden: !((this.isTempSave || this.isStockOutApproval) && (Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP)),
            editor:this.passingValueText 
        },{            
            /**
             * Column for MRP QA flow
             */
            header: "Actual Value",
            dataIndex: 'actualValue',
            hidden: !((this.isStockOutApproval) && (Wtf.account.companyAccountPref.activateMRPManagementFlag && Wtf.account.companyAccountPref.columnPref.isQaApprovalFlowInMRP)),
            editor:this.actualValueText 
        },{
            header:"Action",
            align:'center',
            dataIndex: "lock",
            renderer: function(v,m,rec){
                return "<span class='pwnd delete-gridrow'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>";
            }
        }]);
        this.itemlistCm.defaultSortable = true;
        //        this.addNewRow();
        var addRowBtn = new Wtf.Button({
            text: "Add new Inspection Area",
            iconCls:getButtonIconCls(Wtf.etype.menuadd),
            handler: this.addNewRow,
            scope:this
        })           
        var tbarArr = [];
        tbarArr.push(addRowBtn);
        this.inspectionGrid =  new Wtf.grid.EditorGridPanel({
            store: this.itemlistStore,
            cm: this.itemlistCm,
            height:420,
            layout:'fit',   
            loadMask : true,
            viewConfig: {
                forceFit: true
            },
            clicksToEdit: 1,
            tbar: tbarArr
        });
   
        this.inspectionGrid.on("afteredit", this.gridAfterEdit, this);
        this.inspectionGrid.on('rowclick',this.handleRowClick,this);
        
        this.cForm = new Wtf.form.FormPanel({
            region:"center",
            layout: 'fit',
            height:800,
            border:false,
            scope:this,
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            items: this.inspectionGrid
        });
       
    },
    GetSouthPanel:function(){
        this.southPanel = new Wtf.Panel({
            region:'south',
            height:20,
            border:false,
            html: "<b><div style=\"border:none;\"><li>\n Note: OK = Acceptable  and   NG = Not Acceptable.</li></div> "
        })
    },
    fillData:function(){
        if(this.records.data["status"]!="PENDING"){
            this.modelName.setValue(this.itemlistStore.getAt(0).get("modelname"));
            this.refNo.setValue(this.itemlistStore.getAt(0).get("refno"));
            this.custmerPONo.setValue(this.itemlistStore.getAt(0).get("pono"));
            this.department.setValue(this.itemlistStore.getAt(0).get("department"));
            this.hospitalName.setValue(this.itemlistStore.getAt(0).get("hospital"));
        //            this.addNewRow();
        }
    //        this.addNewRow();
    },
    gridAfterEdit : function(e) {
        e.record.set("edited", true);
        if(e.row == this.itemlistStore.getCount()-1){
        //            this.addNewRow();
        }
       
    },
    addNewRow: function() {
        this.itemlistStore.add(new this.itemlistRecord({
            areaName: '',
            faults: '',
            status: '',
            passingValue:'',
            actualValue:''
        }));
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var total=store.getCount();
//            if(rowindex==total-1){
//                return;
//            }
            Wtf.MessageBox.confirm('Warning', WtfGlobal.getLocaleText("acc.inspection.temp.delete.insp.area")+" ?", function(btn){
                if(btn!="yes") return;
                store.remove(store.getAt(rowindex));
//                this.ArrangeNumberer(rowindex);
            }, this);
        }
    }
    ,
    saveData:function(operation) {
        var modRecs= this.itemlistStore.getModifiedRecords();
        var count=this.itemlistStore.getCount()-1;
        var dataArr=new Array();
        for(var i = 0; i < count; i++) {
            var rec = modRecs[i];
            var jObject={};
            var jArray=[];
            
            jObject.inspection =this.itemlistStore.getAt(i).get("areaName");
            jObject.status = this.itemlistStore.getAt(i).get("status");
            jObject.faults =this.itemlistStore.getAt(i).get("faults");
           
            dataArr.push(jObject);
        }
        var finalStr = JSON.stringify(dataArr);
        var extraData=new Array();
        var jObject1={};
        jObject1.department =this.department.getValue();
        jObject1.hospitalname = this.hospitalName.getValue() ;
        extraData.push(jObject1);
        var finalStr1 = JSON.stringify(extraData);
        Wtf.Ajax.requestEx({
            url:"INVApproval/saveInspectionData.do",
            params: {
                date : this.inspectionDate.getValue(),
                saDetailApprovalId:this.records.data["id"],
                modelname :this.modelName.getValue(),
                description:this.description.getValue(),
                refno: this.refNo.getValue(),
                pono:this.custmerPONo.getValue(),
                serialno:this.serialNo.getValue(),
                jsondata:finalStr,
                extraFields:finalStr1,
                operation:operation,
                module:this.module,
                isapprovalId:this.apprlID
            }
        },
        this,
        function(result, req){
            var msg=result.msg;
            var title="Success";
            if(result.success){
                WtfComMsgBox([title,msg],0);
                this.itemlistStore.removeAll();
                this.addNewRow();
                this.inspectionDate.setValue(new Date());
                this.modelName.setValue("");
                this.description.setValue("");
                this.refNo.setValue("");
                this.custmerPONo.setValue("");
                this.serialNo.setValue("");
                this.close();
            //                this.str.load();
            }
            else if(result.success==false){
                title="Error";
                WtfComMsgBox([title,"Some Error occurred."],0);
                return false;
            }
        },
        function(result, req){
            WtfComMsgBox(["Failure", "Some Error occurred."],3);
            //            this.inspectionwin.close();  
            return false;
        });
    }
});
