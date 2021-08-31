/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function interStoreTransfersApproval(){
    var mainTabId = Wtf.getCmp("as");
    var projectBudget = Wtf.getCmp("interStoreTransformApproval"+0);
    if(projectBudget == null){
        projectBudget = new Wtf.interStoreTransformApproval({
            layout:"fit",
            title:"Inter Store Approval ",
            closable:true,
            module:0,
            border:false,
            id:"interStoreTransformApproval"+0
        });
        mainTabId.add(projectBudget);
    }
    mainTabId.setActiveTab(projectBudget);
    mainTabId.doLayout();
}
function stockRequestTransfersApproval(){
    var mainTabId = Wtf.getCmp("as");
    var projectBudget = Wtf.getCmp("interStoreTransformApproval"+1);
    if(projectBudget == null){
        projectBudget = new Wtf.interStoreTransformApproval({
            layout:"fit",
            title:"Stock Request Approval ",
            closable:true,
            module:1,
            border:false,
            id:"interStoreTransformApproval"+1
        });
        mainTabId.add(projectBudget);
    }
    mainTabId.setActiveTab(projectBudget);
    mainTabId.doLayout();
}


Wtf.interStoreTransformApproval = function(config){
    Wtf.interStoreTransformApproval.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.interStoreTransformApproval, Wtf.Panel, {
    initComponent: function() {
        Wtf.interStoreTransformApproval.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        var companyDateFormat='y-m-d'
        Wtf.interStoreTransformApproval.superclass.onRender.call(this, config);
        //        this.dmflag = 0;
        
        
        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },

        
        {
            "name":"transactionno"
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
            "name":"tostorename"
        },

        {
            "name":"productcode"
        },
        
        {
            "name":"productname"
        },

        {
            "name":"itemdescription"
        },
        {
            "name":"productid"  
        },
        {
            "name":"uomname"
        },

        {
            "name":"quantity"
        },
        
        {
            "name":"status"
        },
        {
            "name":"transactionmodule"
        }

        ]);
     
        this.ds = new Wtf.data.Store({
            baseParams: {
     
            },
            url: 'INVApproval/getISTApprovalList.do',//
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
      
        });
        
        this.ds.on("beforeload",function(){
            this.ds.baseParams = {
                transactionModule:this.module
            }
        },this)
       

        
        this.sm= new Wtf.grid.CheckboxSelectionModel({
       
            });
        
        
        
        var trackStoreLocation=true;
        var integrationFeatureFor=true
        this.cm = new Wtf.grid.ColumnModel([
        //            new Wtf.grid.RowNumberer(),  //0
        //            this.sm, //1
        //            this.expander,  //2
        {
            header: "Transfer Note No.",  //3
            //sortable:true,
            dataIndex: 'transactionno'
        //                hidden:true,
        //                fixed:true
        },
           
           
        {
            header: "From Store",     //6
            //sortable:true,
            dataIndex: 'fromstorename'
        },
        {
            header: "To Store",         //7
            //sortable:true,
            dataIndex: 'tostorename'
        },
           
        {
            header: "Item Code",
            //sortable:true,
            dataIndex: 'productcode'
        },
        {
            header: "Item Name",
            //sortable:true,
            dataIndex: 'productname'
        },
           
            
        {
            header: "UoM",
            //sortable:true,
            dataIndex: 'uomname'
        },
        
        {
            header: "Quantity",
            //sortable:false,
            dataIndex: 'quantity'
               
        },
            
        {
            header: "Inspection Status",
            //sortable:true,
            dataIndex: 'status',
            renderer:function(value){
                if(value=="DONE"){
                    return "<label style = 'color : green;'>DONE</label>";
                }else if(value=="REJECTED"){
                    return "<label style = 'color : red;'>REJECTED</label>";
                }else{
                    return value;
                }
            }
           
        },
        {
            header: "View Detail",//19
            dataIndex: 'type',
            hidden:false,
            renderer: this.serialRenderer.createDelegate(this)
        }

        ]);
            
       
        /********************/
        this.grid=new Wtf.KwlEditorGridPanel({
            id:"inventEditorGridPaneldetails"+this.id,
            cm:this.cm,
            store:this.ds,
            //            sm:this.sm,
           
            viewConfig: {
                forceFit: true
            },
            //            view: grpView,
            //            tbar:tbarArray,
            searchLabel:"Quick Search",
            searchLabelSeparator:":",
            searchEmptyText:"Search by Transfer Note No ",
            serverSideSearch:true,
            searchField:"transfernoteno",
            clicksToEdit:1,
            displayInfo: true
            
        });
        this.add( this.grid);
        this.loadgrid();
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
       
        
        this.grid.on("cellclick",this.cellClickFunction,this)
       
    

    },
    
    loadgrid : function(){
     
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
       
            }
        });
    
    },

    
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip='Show Details', wtf:qtitle='Show Details' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    cellClickFunction :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
                
        if(fieldName != undefined && fieldName == "type"){
            interStoreQADetail(record);
        
          
        }
    }
    
    
});



function interStoreQADetail(record){
    var demo=Wtf.getCmp("InterStoreQADetailCmp"+record.get("transactionmodule")+record.get("transactionno"))
    var main=Wtf.getCmp("as");
    if(demo==null){
        demo =new Wtf.InterStoreApprovalDetail({
            id:"InterStoreQADetailCmp"+record.get("transactionmodule")+record.get("transactionno"),
            layout:'fit',
            rec:record,
            title:record.get("transactionmodule")=="INTER_STORE_TRANSFER"?record.get("transactionno")+"-Details":record.get("transactionno")+"-Details",
            closable:true,
            border:false
        })
        main.add(demo);
    }
    main.setActiveTab(demo);
    main.doLayout();
}

Wtf.InterStoreApprovalDetail = function(config){
    Wtf.InterStoreApprovalDetail.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.InterStoreApprovalDetail, Wtf.Panel, {
    onRender: function(config) {
        Wtf.InterStoreApprovalDetail.superclass.onRender.call(this, config);

        //        this.status = 0;
       
       
       
        this.record = Wtf.data.Record.create([
        {
            "name":"id"
        },

        {
            "name":"fromlocationid"
        },

        {
            "name":"fromlocationname"
        },
        {
            "name":"tolocationid"
        },

        {
            "name":"tolocationname"
        },

        {
            "name":"productcode"
        },

        {
            "name":"productname"
        },

        {
            "name":"quantity"
        },
        {
            "name":"uomName"
        },
        {
            "name":"batchname"
        },
        {
            "name":"status"
        },
        {
            "name":"serialname"
        },
        {
            "name":"transactionno"
        }
        
        ]);
        
        
      
        this.ds = new Wtf.data.Store({
            url: 'INVApproval/getISTDetailApprovalList.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        });
        
      
        this.ds.on("load",function(store,rec,opt){
            this.grid.loadMask = false;
        },this);

        this.ds.on("beforeload",function(){
            this.ds.baseParams = {
                isapprovalId:this.rec.get('id')
            }
        },
        this);
        var integrationFeatureFor=true;

        this.approveButton = new Wtf.Button({
            text: 'Approve',
            scope: this,
            hidden:this.type==2?true:false,
//                     disabled: true,
            tooltip: {
               
                text:"Approve selected items"
            },
            //            hidden:((Wtf.realroles[0]==14 && isIncludeQAapprovalFlow)||(Wtf.realroles[0]==27 && isIncludeSVapprovalFlow))?false:true, // Only if QA user and QA approval flow or SuperVisor is included then only see this button
            handler:function(){
               this.docuploadhandler();
            }      
        });		
        
        this.remark="";
        
        this.rejectButton = new Wtf.Button({
            text: 'Reject',
            scope: this,
            disabled: true,
            hidden:this.type==2?true:false,
            tooltip: {
                
                text:"Reject selected items"
            },
            //            hidden:((Wtf.realroles[0]==14 && isIncludeQAapprovalFlow)||(Wtf.realroles[0]==27 && isIncludeSVapprovalFlow))?false:true, // Only if QA user and QA approval flow is included then only see this button
            handler:function(){
                if((true)){
                    this.remarkfunction("process",true);
                }else if(true){
                    this.remarkfunction("reject by Supervisor");
                                
                }                 
            }
        });
        
        var bbarArray= new Array();
        bbarArray.push("-",this.approveButton);
        bbarArray.push("-",this.rejectButton);
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
        var cmDefaultWidth = 100;
        this.cm = new Wtf.grid.ColumnModel([
            new Wtf.KWLRowNumberer(),
            this.sm,
            //            this.expander, //1
            {
                header: "From Location", //2
                dataIndex: 'fromlocationname',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header: "To Location", //2
                dataIndex: 'tolocationname',
                groupable: false,
                width:cmDefaultWidth,
                fixed:true
            },
            {
                header: "Item Code",//3
                dataIndex: 'productcode',
                width:cmDefaultWidth,
                fixed:true,
                hidden:true
            },
            {
                header: "item Name",//3
                dataIndex: 'productname',
                width:cmDefaultWidth,
                fixed:true,
                hidden:true
            },
            {
                header: "Batch",//3
                dataIndex: 'batchname',
                width:cmDefaultWidth,
                fixed:true
            },
          
            {
                header: "Serial",//7
                dataIndex: 'serialname',
                groupable: true,
                width:cmDefaultWidth
            },{
                header: "Inspection Status",//8
                dataIndex: 'status',
                groupable: true,
                width:cmDefaultWidth,
                renderer:function(value){
                    if(value=="APPROVED"){
                        return "<label style = 'color : green;'>APPROVED</label>";
                    }else if(value=="REJECTED"){
                        return "<label style = 'color : red;'>REJECTED</label>";
                    }else{
                        return value;
                    }
                }
            
            },
        
            {
                header: "Quantity",//15
                dataIndex: 'quantity',
                sortable:false,
                align: 'right',
                width:cmDefaultWidth,
                summaryType: 'sum',
                renderer:function(val){
                   
                    return val;
                }
         
            },
          
            {
                header: "Inspection Criteria",//19
                dataIndex: 'type',
                hidden:false,
                renderer: this.serialRenderer.createDelegate(this)
            }]);
        // this.cm.defaultSortable = true;
        

        /**************date *****/


               
        this.grid=new Wtf.KwlEditorGridPanel({
            id:"StockoutApprovalDetail"+this.id,
            cm:this.cm,
            sm:this.sm,
            store:this.ds,
            loadMask:true,
            //            tbar:tbarArray,
            viewConfig: {
                forceFit: true
            },
            searchLabel:"Quick Search",
            searchLabelSeparator:":",
            searchEmptyText: "Search by Product id, Product Name",
            serverSideSearch:true,
            searchField:"productCode",
            clicksToEdit:1,
            displayInfo: true,
            displayMsg: 'Displaying  {0} - {1} of {2}',
            emptyMsg: "No results to display",
            bbar:bbarArray
      
        });
        this.grid.on("cellclick",this.cellClick,this);
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        
        this.sm.on("selectionchange",function(){
            var selected = this.sm.getSelections();
            if(selected.length>0 && selected.length == 1){
                if(selected.get("status") == "PENDING"){
                    this.approveButton.enable();
                    this.rejectButton.enable();
                }
            }
        })

        this.add(this.grid);
        this.loadgrid();
        
    },

    loadgrid : function(){
     
        this.ds.load({
            params:{
                start:0,
                limit:Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize//30,//Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize,
       
            }
        });
    
    },
    serialRenderer:function(v,m,rec){
        if(rec.get('status') == 'PENDING'){
            return "<div  wtf:qtip='Show Details', wtf:qtitle='Show Details' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
        }else{
            return "<div  wtf:qtip='Show Details', wtf:qtitle='Show Details' class='View Details'></div>";
        }
    },
    cellClick :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        //        var itemId=record.get("productid");
        //        var itemCode=record.get("pid");
        //        var quantity=record.get("quantity");
        //        var UOMName=record.get("uomname");
        //        var fromStoreId = this.parent.fromstoreCombo.getValue();
        
        if(fieldName != undefined && fieldName == "type"){
            //            alert("view form");
            //            if(fromStoreId == undefined || fromStoreId == ""){
            //                WtfComMsgBox(["Warning", "Please Select Store."],0);
            //                return false;
            //            }
            inspectionTab(record,"interstore",this.ds,this.rec.get('id'));
          
        }
    },
     docuploadhandler : function(e, t) {
//            if (e.target.className != "pwndbar1 uploadDoc")
//                return;
            var selected = this.sm.getSelections();            
//            if (this.grid.flag == 0) {
                this.fileuploadwin = new Wtf.form.FormPanel(
                {                   
                    url : "ACCInvoiceCMN/attachDocuments.do",
                    waitMsgTarget : true,
                    fileUpload : true,
                    method : 'POST',
                    border : false,
                    scope : this,
                    // layout:'fit',
                    bodyStyle : 'background-color:#f1f1f1;font-size:10px;padding:10px 15px;',
                    lableWidth : 50,
                    items : [
                    this.sendInvoiceId = new Wtf.form.Hidden(
                    {
                        name : 'invoiceid'
                    }),
                    this.tName = new Wtf.form.TextField(
                    {
                        fieldLabel : WtfGlobal.getLocaleText("acc.invoiceList.filePath") + '*',
                        //allowBlank : false,
                        name : 'file',
                        inputType : 'file',
                        width : 200,
                        //emptyText:"Select file to upload..",
                        blankText:WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                        allowBlank:false,
                        msgTarget :'qtip'
                    }) ]
                });

                this.upwin = new Wtf.Window(
                {
                    id : 'upfilewin',
                    title : WtfGlobal
                    .getLocaleText("acc.invoiceList.uploadfile"),
                    closable : true,
                    width : 450,
                    height : 120,
                    plain : true,
                    iconCls : 'iconwin',
                    resizable : false,
                    layout : 'fit',
                    scope : this,
                    listeners : {
                        scope : this,

                        close : function() {
                            thisclk = 1;
                                scope: this;
                            this.fileuploadwin.destroy();
                            this.grid.flag = 0
//                              this.upwin.close();
                        }
                    },
                    items : this.fileuploadwin,
                    buttons : [
                    {
                        anchor : '90%',
                        id : 'save',
                        text : WtfGlobal
                        .getLocaleText("acc.invoiceList.bt.upload"),
                        scope : this,
                        handler : this.upfileHandler
                    },
                    {
                        anchor : '90%',
                        id : 'close',
                        text : WtfGlobal
                        .getLocaleText("acc.invoiceList.bt.cancel"),
                        handler : this.close1,
                        scope : this
                    } ]

                });
//                this.sendInvoiceId.setValue(selected[0].get('billid'));
                this.upwin.show();
//                this.grid.flag = 1;
            },
             close1 : function() {
            Wtf.getCmp('upfilewin').close();
            this.grid.flag = 0;
        },

        upfileHandler : function() {
            if (this.fileuploadwin.form.isValid()) {
                Wtf.getCmp('save').disabled = true;
            }
            //var selected = this.sm.getSelections();
           // if (selected[0].get('doccnt') < 3) {
                if (this.fileuploadwin.form.isValid()) {
                    this.fileuploadwin.form.submit({
                        scope : this,
                        failure : function(frm, action) {
                            this.upwin.close();
                            //this.genSaveSuccessResponse(eval('('+action.response.responseText+')'));
			    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), "File not uploaded! File should not be empty.");
                        },
                        success : function(frm, action) {
                            this.upwin.close();                            
                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), "File uploaded successfully.");
                        }
                    })
                }
            
        }
    
});

