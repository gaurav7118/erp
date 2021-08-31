Wtf.account.FAOpeningWindow = function(config){
    Wtf.apply(this,{
        buttons:[this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeFAOpenWin.createDelegate(this)
            })]
    },config);
    
    Wtf.account.FAOpeningWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.FAOpeningWindow, Wtf.Window,{
    onRender:function(config){
        Wtf.account.FAOpeningWindow.superclass.onRender.call(this,config);
        
        this.createFixedAssetInfoForm();
     
        //create Grid
        
        this.createDocumentInfoGrid();
        
        // set Asset Info
        
        this.setAssetInfo();
        
        this.add({
            region: 'north',
            height: 100,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            items:[this.fixedAssetInfoForm]
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.documentGridContainerPanel]

        }
        );        
    },
    
    setAssetInfo: function(){
        if(this.assetRec){
            this.assetId.setValue(this.assetRec.get('pid'));
            this.assetName.setValue(this.assetRec.get('productname'));
        }
    },
    
    createFixedAssetInfoForm:function(){
        this.fixedAssetInfoForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            height:100,
            bodyStyle:'margin:40px 10px 10px 30px',
//            labelWidth:100,
            items:[
                {
                    layout:'column',
                    border:false,
                    items:[{
                        columnWidth:'.45',
                        layout:'form',
                        border:false,
                        labelWidth:85,
                        items:[
                            this.assetId = new Wtf.form.TextField({
                                fieldLabel:WtfGlobal.getLocaleText("erp.fixedasset.assetgroupid"),
                                readOnly:true,
                                name:'assetId',
                                width:150
                            })
                        ]
                    },{
                        columnWidth:'.45',
                        layout:'form',
                        border:false,
                        labelWidth:110,
                        items:[
                            this.assetName = new Wtf.form.TextField({
                                fieldLabel:WtfGlobal.getLocaleText("erp.fixedasset.assetgroupname"),
                                name:'assetName',
                                readOnly:true,
                                width:150
                            })
                        ]
                  }]
             }
            ]
        });
    },
    
    createDocumentInfoGrid:function(){
        this.gridRec = new Wtf.data.Record.create([
            {name:'documentId'},
            {name:'documentNo'},
            {name:'documentDate',type:'date'},
            {name:'quantity'},
            {name:'rate'},
//            {name:'value'},        // ERP-16629: WDV field should be optional during asset creation
            {name:'assetdetails'}
        ]);
        
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.gridRec);
        
        var gridStoreUrl = "ACCProduct/getAssetOpenings.do";
        
        this.gridStore = new Wtf.data.Store({
            url:gridStoreUrl,
            baseParams:{
                productId:this.assetRec.get('productid')
            },
            reader:this.gridStoreReader
        });
        
        this.loadMask = new Wtf.LoadMask(document.body,{
                    msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        
        this.gridStore.on('loadexception',function(){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);
        
         this.gridStore.on('beforeload',function(){
            this.loadMask.show();
        },this);

        this.gridStore.on('load',function(){
            this.loadMask.hide();
        },this);
        
        this.gridStore.load({params:{start:0,limit:30}});
        Wtf.FixedAssetStore.load();  // load the store because when we create new fixed asset group then while giving opening it is not availble in combo 
        //
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:false
        });
        
        this.colModel = new Wtf.grid.ColumnModel([this.sm,{
                header:WtfGlobal.getLocaleText("acc.field.DocumentNo"),
                dataIndex:'documentNo',
                align:'center'
            },{
                header:WtfGlobal.getLocaleText("acc.fixed.asset.date"),
                dataIndex:'documentDate',
                align:'center',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.fixed.asset.quantity"),
                dataIndex:'quantity',
                align:'center',
                renderer:this.quantityRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.fixed.asset.rate"),
                dataIndex:'rate',
                align:'center',
                renderer:WtfGlobal.currencyRendererSymbol
            },/* // ERP-16629: WDV field should be optional during asset creation
            {
                header:'WDV',
                dataIndex:'value',
                align:'center',
                renderer:WtfGlobal.currencyRendererSymbol
        } */]);
    //
        this.documentInfoGrid = new Wtf.grid.GridPanel({
            cm:this.colModel,
            store:this.gridStore,
            sm:this.sm,
            stripeRows :true,
            border:false,
            viewConfig:{
                emptyText:'<center>'+WtfGlobal.getLocaleText("erp.emptytext.norectodisplay")+'</center>',
                forceFit:true
            }
        });
         this.documentInfoGrid.getSelectionModel().on('selectionchange',this.enableDisableButtons,this);
         // Tbar Item Creation
    
        var buttonArr = [];
        this.createNewButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.create.title"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.create.title"),
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler:this.createNewHandler.createDelegate(this)
        });

        this.editButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),
            scope:this,
            //hidden:true,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:WtfGlobal.getLocaleText("acc.fixedasset.editopeningtransaction"),
            handler:this.editHandler.createDelegate(this)
        });

        this.deleteButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.deleteHandler.createDelegate(this)
        });
        
        buttonArr.push(this.createNewButton,this.editButton,this.deleteButton);
        
        // creating container panel
    
        this.documentGridContainerPanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            items:[
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.documentInfoGrid],
                    tbar:buttonArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.gridStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                    })
                }
            ]
        });         
    },
    
    quantityRenderer:function(val,m,rec){
        return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    },
    
    createNewHandler:function(){
        this.openingForm = new Wtf.account.FAOpeningForm({
            title:WtfGlobal.getLocaleText("acc.field.OpeningForm"),
            layout:'border',
            id:'createFAOpeningFormId',
            assetRec:this.assetRec,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            height:400,
            width:750,
            modal:true
        });
        
        this.openingForm.on('datasaved',this.reloadStore,this)
        
        this.openingForm.show();
    },
    
    deleteHandler : function(){        
        var selectedRecordArray = this.documentInfoGrid.getSelectionModel().getSelections();
        if(selectedRecordArray.length ==  0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        
        var documentIds = "";
        var record = "";
        for(var i = 0; i <  selectedRecordArray.length; i++){
            record = selectedRecordArray[i];
            var documentId = record.get('documentId');
            documentIds +=  documentId + ",";
        }
                
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.block"), WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttodeletetoselectedRecord"),function(btn){
             if(btn=="yes") {
                Wtf.Ajax.requestEx({
                    url: "ACCProduct/deleteFixedAssetOpeningDocuments.do",
                    params: {
                        documentId:documentIds
                    }
                },this,this.genSuccessResponseAsset,this.genFailureResponseAsset);            
            }
        }, this)
    },
    
    genSuccessResponseAsset : function(response){        
        if(response.success){
            WtfComMsgBox([this.title,response.msg],response.success*2+1);
            this.reloadStore();
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        }
    },
    
    genFailureResponseAsset : function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg){
            msg=response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
        
    reloadStore:function(){
        this.gridStore.reload();
    },
    
    editHandler:function(){
       	var formRecord = null;
        var record=" ";
    	if(this.documentInfoGrid.getSelectionModel().hasSelection()==false||this.documentInfoGrid.getSelectionModel().getCount()>1){
            WtfComMsgBox(118,2);
            return;
    	}
    	formRecord = this.documentInfoGrid.getSelectionModel().getSelections();
        record = formRecord[0];
        
         this.openingForm = new Wtf.account.FAOpeningForm({
            title: WtfGlobal.getLocaleText("acc.field.OpeningForm"),
            layout:'border',
            id:'editopeningFormId'+this.id,
            assetRec:this.assetRec,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            height:400,
            isEdit:true,
            record:record,
            width:750,
            modal:true
        });
        
        this.openingForm.on('datasaved',this.reloadStore,this)
        this.openingForm.show();      
    },
    enableDisableButtons: function() {
        if (this.documentInfoGrid.getSelectionModel().hasSelection() == false || this.documentInfoGrid.getSelectionModel().getCount() > 1) {
            if (this.editButton)
                this.editButton.disable();
            return;
        } else {
            if (this.editButton)
                this.editButton.enable();
            if (this.deleteButton)
                this.deleteButton.enable();
            return;
        }
    },
    
    closeFAOpenWin:function(){
        this.close();
    }
})


// FIXED ASSET OPENING FORM COMPONENT

Wtf.account.FAOpeningForm = function(config){
//    this.assetDetails = [];
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.saveForm.createDelegate(this)
                }),this.closeButton = new Wtf.Toolbar.Button({
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        minWidth: 50,
                        scope: this,
                        handler: this.closeFAOpenForm.createDelegate(this)
                })]
    },config);
    
    Wtf.account.FAOpeningForm.superclass.constructor.call(this, config);
    
    this.addEvents({
        'datasaved':true//event will be fire when data will be saves successfully.
    });
}

Wtf.extend(Wtf.account.FAOpeningForm, Wtf.Window,{
    onRender:function(config){
        Wtf.account.FAOpeningForm.superclass.onRender.call(this,config);
        var image="../../images/accounting_image/calendar.jpg";
        
        if(Wtf.userds)
            Wtf.userds.load();
        if(Wtf.locationStore)
            Wtf.locationStore.load();
        if(Wtf.detartmentStore)
            Wtf.detartmentStore.load();
             
        // create form FIELDS
        
        this.createFormFields();
        
        // create form
        
        this.createForm();
  
        // adding form
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(this.title,WtfGlobal.getLocaleText("acc.field.PleaseFillDocumentInformation"),image)
        }, this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                id: 'centerpan'+this.id,
                autoScroll:true,
                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
//                layout: 'border',
                items:[this.docInfoForm]
            })
        );
        if(this.isEdit){     //edit form
            this.loadRecord();
        }       
    },
    
    closeFAOpenForm:function(){
        this.close();
    },
    
   saveForm:function(){
        this.saveButton.disable();
        this.docNumber.setValue(this.docNumber.getValue().trim());
        if(this.docInfoForm.getForm().isValid()){
            var rec = this.docInfoForm.getForm().getValues();
            
            rec.productId = this.assetRec.get('productid');
            rec.creationDateStr = WtfGlobal.convertToGenericDate(this.docDate.getValue());
            rec.documentNumber = this.docNumber.getValue();
            rec.quantity = this.quantity.getValue();
            rec.rate = this.rate.getValue();
            /* // ERP-16629: WDV field should be optional during asset creation
            rec.wdv = this.wdv.getValue(); */
            rec.assetDetails = this.assetDetails;
            rec.isEdit=this.isEdit;
            var assetDetailArray = eval('(' + this.assetDetails + ')');
             if(this.isEdit){
                var assetarray=eval('(' +this.record.get("assetdetails") + ')');
                rec.documentId=assetarray[0].documentId;
                rec.editdocumentNumber=assetarray[0].documentNo;
            }
            if(assetDetailArray == null || assetDetailArray == undefined){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.PleaseProvideAssetDetails")],0);
                this.saveButton.enable();
                return;
            }
            
            if(this.quantity.getValue()<=0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.PleaseEnteranappropriatequantity")],0);
                this.saveButton.enable();
                return;
            }
            
            if(this.rate.getValue()<=0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.PleaseEnterappropriateRate")],0);
                this.saveButton.enable();
                return;
            }
            
           /*  // ERP-16629: WDV field should be optional during asset creation
            if(this.wdv.getValue()<=0){
                WtfComMsgBox(['Infornation','Please Enter an appropriate WDV Value.'],0);
                return;
            } */
            
            var assetDetailTotalCost = 0;
            /* // ERP-16629: WDV field should be optional during asset creation
            var assetDetailTotalWDV = 0; */
            
            for(var i=0;i<assetDetailArray.length;i++){
               /* // ERP-16629: WDV field should be optional during asset creation
               assetDetailTotalWDV+=parseFloat(assetDetailArray[i].wdv); */
                assetDetailTotalCost+=parseFloat(assetDetailArray[i].cost);
            }
            
            /* // ERP-16629: WDV field should be optional during asset creation
            if(assetDetailTotalWDV != this.wdv.getValue()){
                WtfComMsgBox(['Infornation','WDV entered is not equal to Asset Details total WDV value.'],0);
                return;
            } */
            
            if(getRoundedAmountValue(assetDetailTotalCost) != getRoundedAmountValue(this.rate.getValue())){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.filed.RateenteredisnotequaltoAssetDetailstotalCostvalue")],0);
                this.saveButton.enable();
                return;
            }
            
            if(getRoundedAmountValue(this.quantity.getValue()) != getRoundedAmountValue(assetDetailArray.length)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.field.EntereddoesmatchsetDetails")],2);
                this.saveButton.enable();
                return;
            }           
            
            var url = 'ACCProduct/saveAssetOpenings.do';
            
            Wtf.Ajax.requestEx({
                url:url,
                params:rec
            },this,this.genSuccessResponse,this.genFailureResponse);    
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
            this.saveButton.enable();
            return;  
        }
    },
    
    genSuccessResponse:function(response, request){
        if(response.success){
            WtfComMsgBox([this.title,response.msg],response.success*2+1);
            this.fireEvent('datasaved',this);
            this.close();
        }else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
            this.saveButton.enable();
        }
    },
    
    genFailureResponse:function(response, request){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        this.saveButton.enable();
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },    
    
    createForm:function(){
        
        this.docInfoForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            height:100,
            bodyStyle:'margin:20px 10px 10px 10px',
            labelWidth:70,
            items:[
                {
                    layout:'column',
                    border:false,
                    items:[{
                        columnWidth:'.38',
                        layout:'form',
                        border:false,
                        items:[this.docNumber,this.docDate]
                    },{
                        columnWidth:'.38',
                        layout:'form',
                        border:false,
                        items:[this.quantity,this.rate]
                  },{
                        columnWidth:'.14',
                        layout:'form',
                        border:false,
                        items:[this.assetDetailsButton]
                  }]
             }
            ]
        });
    },
    
    loadRecord:function()
    {
        //this.docInfoForm.getForm().loadRecord(this.record);
        this.docNumber.setValue(this.record.get("documentNo"));
        this.docDate.setValue(this.record.get("documentDate"));
        this.quantity.setValue(this.record.get("quantity"));
        this.rate.setValue(this.record.get("rate"));
        /* // ERP-16629: WDV field should be optional during asset creation
        this.wdv.setValue(this.record.get("value")); */
        this.assetDetails=this.record.get("assetdetails");
    },
    
    createFormFields:function(){
//        a a;
        this.assetDetailsButton1 = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.fixed.asset.details"),
            tooltip: WtfGlobal.getLocaleText("acc.fixed.asset.detailsTT"),
            disabled: false,
            scope: this,
            hidden:true
//            handler: this.wdvChanged
        })
        this.assetDetailsButton2 = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.fixed.asset.details"),
            tooltip: WtfGlobal.getLocaleText("acc.fixed.asset.detailsTT"),
            disabled: false,
            scope: this,
            hidden:true
//            handler: this.wdvChanged
        })
        this.assetDetailsButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.fixed.asset.details"),
            tooltip: WtfGlobal.getLocaleText("acc.fixed.asset.detailsTT"),
            disabled: false,
            scope: this,
            handler: this.wdvChanged
        })
        
        this.docNumber=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.docNo")+"*",
            name: 'docnumber',
            disabled:false,
            id:"docnumber"+this.id,
            //            anchor:'50%',
            width : 150,
            maxLength:50,
            scope:this,
//            hidden:!this.isInvoice,
//            hideLabel:!this.isInvoice,
            allowBlank:false,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        });
        
        this.docDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.date"),
            id:"docDate"+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'docDate',
//            value:Wtf.serverDate,
            value:WtfGlobal.getOpeningDocumentDate(true), // Set transaction date as before first financial year date.
            maxValue:WtfGlobal.getOpeningDocumentDate(true),//Set transaction date max value as before first financial year date.
//            hidden:!this.isInvoice,
//            hideLabel:!this.isInvoice,
//            anchor:'50%',
            width : 150
        });
        
        this.quantity=new Wtf.form.NumberField({
            allowBlank:false,
             maxLength: 10,
            width:150,
            value:0,
            allowDecimals:false,
            allowNegative:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.quantity"),
            name:'quantity',
            id:"quantity"+this.id
        });
        
        this.rate=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            value:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            hideLabel:false,
            allowBlank:false,
            maxLength: 10,
            width:150,
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.rate"),
            name:'rate',
            id:"rate"+this.id
        });
        
        this.loadMask = new Wtf.LoadMask(document.body,{
                    msg : 'Loading...'
        });
        
        /* // ERP-16629: WDV field should be optional during asset creation
        this.wdv=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:false,
            value:0,
            decimalPrecision:2,
            hideLabel:false,
            allowBlank:false,
            maxLength: 10,
            width:150,
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.wdv"),
            name:'wdv',
            id:"wdv"+this.id
        });
        
        this.wdv.on('change',this.wdvChanged,this); */
    },
       
    wdvChanged:function(obj,newVal, oldVal){
        this.loadMask.show();
        this.PurDate=this.docDate.getValue();
        this.FADetailsGrid=new Wtf.account.FADetails({
            title:WtfGlobal.getLocaleText("acc.fixed.asset.details"),
            quantity:this.quantity.getValue(),
            rate:this.rate.getValue(),
//            wdv:this.wdv.getValue(),
            modal:true,
            layout:'border',
            assetRec:this.assetRec,
            assetDetailsArray:this.assetDetails,
            moduleid:this.moduleid,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width:950,
            isFromOpeningForm:true,
            isFixedAsset:true,
            isEdit:this.isEdit,
            height:500,
            resizable : false,
            billDate:this.PurDate,
        });
        
        this.FADetailsGrid.show();
        
        this.FADetailsGrid.on('beforeclose',this.FixedAssetDetailsGridSaved, this);
        
        this.loadMask.hide();
    },
    
    FixedAssetDetailsGridSaved:function(panel){
        if(panel.isFromSaveButton)
            this.assetDetails = panel.assetDetails;
        var assetDetailsArr = eval(this.assetDetails);
        var rate = 0;
        if (this.rate && assetDetailsArr && assetDetailsArr.length > 0)
        {
            for (var i = 0; i < assetDetailsArr.length; i++)
            {
                if (assetDetailsArr[i] && assetDetailsArr[i].costInForeignCurrency)
                {
                    rate += parseFloat(assetDetailsArr[i].costInForeignCurrency);
                }
            }
            this.rate.setValue(rate.toFixed(Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL));
        }
    }
});







