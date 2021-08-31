Wtf.account.UOMschemaMaster = function(config){
    Wtf.apply(this, config);
    Wtf.account.UOMschemaMaster.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.UOMschemaMaster, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.UOMschemaMaster.superclass.onRender.call(this, config);
        
        //create Grid
        
        this.creategrid();
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [ {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.Store,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                        items: this.bottomArr
                })
            }]
        }); 
        this.add(this.leadpan);
    },
    
    updateGrid:function(){
        this.Store.reload();
    },
    
    creategrid:function(){
        
        this.btnArr = [];
        this.bottomArr = [];
      
        
        this.createNew = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.schema.addschema"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.schema.addschema.tooltip"),
            iconCls :getButtonIconCls(Wtf.etype.add),
//             disabled :true,
//             hidden:true, 
            handler:function (){
                this.AddUOMSchemaType(false);
             }
        });
        
        this.editButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.schema.Editschema"),//'Edit',
            scope:this,
            disabled:true,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:WtfGlobal.getLocaleText("acc.schema.Editschema.tooltip"),
             handler:function (){
               this.AddUOMSchemaType(true);
            }
        });
        
        this.deleteButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),//'Delete',
            scope:this,
            disabled:true,
            iconCls :getButtonIconCls(Wtf.etype.menudelete), 
            tooltip:WtfGlobal.getLocaleText("acc.fixed.asset.delete.selected"),//'Delete to selected record',
            handler:function (){
                this.DeleteUOMSchemaType();
            }
        });
        this.ConfigureUOMSchema = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.UOM.ConfigureUOMSchema"), //             WtfGlobal.getLocaleText("acc.schema.addschema"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.schema.addschema.tooltip"),
            iconCls :getButtonIconCls(Wtf.etype.add),
             disabled :true,
//             hidden:true, 
            handler:function (){
                this.ConfigureUOMSchemaWindow();
             }
        });
            this.masterItemSearch = new Wtf.MyQuickSearch({
            field: 'name',
            hidden:true,
            emptyText:WtfGlobal.getLocaleText("acc.masterConfig.search1"),  //'Search by Master Item...',
            width: 150
         });

        this.btnArr.push(this.masterItemSearch,!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.createuom)?this.createNew:"",!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.edituom)?this.editButton:"",!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.deleteuom)?this.deleteButton:"",!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.configuomuom)?this.ConfigureUOMSchema:"");//,this.createNewLocButton,this.createNewwarehouseButton,this.editButton,this.deleteButton
       
        this.moduleName = "UOM Schema";
        var extraConfig = {};
        extraConfig.url= "ACCUoM/importUOMSchemaType.do";
        var extraParams = "";
        var importBtnArr = Wtf.importMenuArray(this, this.moduleName, this.Store, extraParams, extraConfig);
        
        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importBtnArr
        });
        
       if(!WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.importuom)){
            this.bottomArr.push(this.importBtn);
        }
        this.gridRec = Wtf.data.Record.create ([
//                {name:'uomid'},
                {name:'rowid'},
                {name:'uomschematype'},
                {name:'uomid'},
                {name:'uomname'}
                
       ]);
        
        
        this.msgLmt = 30;
        this.StoreReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        }, this.gridRec);
       
        this.Store = new Wtf.data.Store({
            url:"ACCUoM/getUOMType.do",
            baseParams:{
                mode:22,
                doNotShowNAUomName:true
            },
            reader: this.StoreReader
        });
        
               
        this.loadMask = new Wtf.LoadMask(document.body,{
                    msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        
        this.Store.on('loadexception',function(){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);
        
         this.Store.on('beforeload',function(){
            this.loadMask.show();
        },this);
        
        
        this.Store.on('load',function(){
            this.loadMask.hide();
        },this);
        
        this.Store.load();
        
         this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
        
           
        this.colModel = new Wtf.grid.ColumnModel([this.sm,{
             header:WtfGlobal.getLocaleText("acc.UOM.SchemaUOM"),
            dataIndex:'uomschematype',//dataIndex:'accnamecode',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.UOM.StockUOM"),                 //WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),  //"warehouse",
            dataIndex:'uomname',//dataIndex:'accnamecode',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            pdfwidth:75
        }
//        ,{
//          header:'UOM Schema Type',              //WtfGlobal.getLocaleText("acc.customerList.gridName"),  //" customer Name",
//            dataIndex:'uomschematype',
//            renderer:WtfGlobal.deletedRenderer,
//            sortable: true,
//            pdfwidth:75
//         }
     ]);
        
           this.grid = new Wtf.grid.GridPanel({
            cm:this.colModel,
            store:this.Store,
            sm:this.sm,
            stripeRows :true,
            border:false,
            viewConfig:{
                emptyText:'<center>'+WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))+'</center>',
                forceFit:true
            }
        });
         this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);

        
    },
     AddUOMSchemaType:function (isEdit){
//        var recArray = this.grid.getSelectionModel().getSelections();                
        if(isEdit && (this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1)){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
          var rec = this.grid.getSelectionModel().getSelected();

             chkUomload(); 
            this.uomRec = Wtf.data.Record.create([
                {name: 'uomid'},
                {name: 'uomname'},
                {name: 'precision'}
            ]);

            this.uomStore = new Wtf.data.Store({
                url: "ACCUoM/getUnitOfMeasure.do",
                baseParams: {
                    mode: 31,
                    common: '1',
                    doNotShowNAUomName: true
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, this.uomRec)
            });
//            var scope = this;
//            if (Wtf.uomStore.getRange() > 0) {
//                scope.uomStore.data = Wtf.uomStore.data || {};
//            }
            this.uomStore.load();

            this.stockUom=new Wtf.form.FnComboBox({
                name:'uomname',
                store:this.uomStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                selectOnFocus:true,
                fieldLabel:WtfGlobal.getLocaleText("acc.product.stockUoMLabel"),
                valueField:'uomid',
                displayField:'uomname',
                scope:this,
                allowBlank: false,
                width: 200,
                forceSelection:true
            });  
            this.stockUom.addNewFn=this.showUom.createDelegate(this);
            
            this.stockUom.on('select', function(combo, record, index){
                if(isEdit && combo.getValue() != rec.data['uomid']){
                    Wtf.Ajax.requestEx({
                        url:"ACCUoM/getISUOMSchemaConfiguredandUsed.do",   
                        params: {
                            uomschematype:rec.data['uomschematype'],
                            newStockUomid:combo.getValue(),
                            oldStockUomid:rec.data['uomid']
                        }
                    },this,this.genSuccessResponseforEdit,this.genFailureResponseforEdit);
                }
            },this);
             this.addUOMSchemaItemForm = new Wtf.form.FormPanel({
                waitMsgTarget: true,
                border: false,
                region: 'center',
                layout:'form',
                bodyStyle: "background: transparent;",
                style: "background: transparent;padding:20px;",
                labelWidth: 107,
                items: [this.schemaName=new Wtf.form.TextField({
//                        xtype: 'textfield',
                        fieldLabel:WtfGlobal.getLocaleText("acc.schema.name"),       //((isEdit ? WtfGlobal.getLocaleText("acc.common.edit") + ' ' : WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew") + ' ') ),
                        name: "uomschematype",
                        scope:this,
//                        msgTarget: 'under',
//                        style: "margin-left:30px;",
                        width: 200,
                        maxLength:500,
//                        validator:(!this.paidToFlag)?Wtf.ValidateCustomColumnName:"",
                        validator:Wtf.ValidatePaidReceiveName,
                        value:(isEdit)?rec.data['name']:'',
                        allowBlank: false
//                        id: "uomschematype"
                    }),this.stockUom]
            });     
            
            if(isEdit && this.uomStore.getCount()>0){
                this.stockUom.setValue(rec.data['uomid']);
                this.schemaName.setValue(rec.data['uomschematype']);
            }
             
            this.uomStore.on('load',function(s,o){
                if(isEdit){
                    this.stockUom.setValue(rec.data['uomid']);
                    this.schemaName.setValue(rec.data['uomschematype']);
                }
            },this);
            
            this.saveLocationBtn = new Wtf.Button({
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                handler: function(button) {
                    this.schemaName.setValue(this.schemaName.getValue().trim())
                    var schemaName=this.schemaName.getValue();
                    var stockuomid=this.stockUom.getValue();
                    if (this.addUOMSchemaItemForm.form.isValid()) {
                         this.saveLocationBtn.disable();
                         this.saveMasterGroupItem("ok",schemaName,isEdit,(isEdit?rec.data['rowid']:""),stockuomid) 
                    } else {
                        WtfComMsgBox(2, 2);
                    }
                }
            });

            this.addUOMSchemaType = new Wtf.Window({
                modal: true,
                title:  isEdit ? WtfGlobal.getLocaleText("acc.schema.Editschema") :WtfGlobal.getLocaleText("acc.UOM.AddUOMSchemaType"),
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                bodyStyle: 'padding:5px;',
                buttonAlign: 'right',
                width: 425,
//        height: 115,
                scope: this,
                items: [{
                        region: 'center',
                        border: false,
                        bodyStyle: 'background:#f1f1f1;font-size:10px;',
                        autoScroll: true,
                        items: [this.addUOMSchemaItemForm]
                    }],
                buttons: [{
                        text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                        scope: this,
                        handler: function (button) {   
                            this.schemaName.setValue(this.schemaName.getValue().trim());
                            var schemaName=this.schemaName.getValue();
                            var stockuomid=this.stockUom.getValue();
                            if (this.addUOMSchemaItemForm.form.isValid()) {
                                 this.saveLocationBtn.disable();
                                this.saveMasterGroupItem("ok", schemaName, isEdit, (isEdit ? rec.data['rowid'] : ""), stockuomid)
                            } else {
                                WtfComMsgBox(2, 2);
                            }
                        }
                    }, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope: this,
                        handler: function() {
                            this.addUOMSchemaType.close();
                        }
                    }]
            });

            this.addUOMSchemaType.show();

//        }
    },
       genSuccessResponseforEdit:function(response){
        if(response.success){
            if(response.count == 0){
                this.stockUom.setValue(response.newStockUomid);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.uomschema.stockuomupdate")], 2);
                this.stockUom.setValue(response.oldStockUomid);
                return false;
            }
           
        }else if(response.success == false){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg]);
        }    
    },
    genFailureResponseforEdit:function(response){
        if(response.msg)
            var msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    showUom: function () {
        callUOM('uomReportWin');
        Wtf.getCmp('uomReportWin').on('update', function () {
            Wtf.uomStore.reload();
        }, this);
    },
    ConfigureUOMSchemaWindow: function(){
     var selections = this.grid.getSelectionModel().getSelections();
        if (selections.length == 1) {
            var rec = this.grid.getSelectionModel().getSelected();
            this.UomSchemaWindow = new Wtf.account.UomSchemaWindow({
                id: 'uomschemawindow',
                title: WtfGlobal.getLocaleText("acc.schema.schemaDef"),
                border: false,
                uomid:rec.data['uomid'],
                uomschematype:rec.data['uomschematype'],
                uomschematypeid:rec.data['rowid'],
            //    isReceipt: false,
                layout:'border',
                height:Wtf.account.companyAccountPref.activateInventoryTab==true?650:450,
                autoScroll: true
            //    isCustomer: this.businessPerson == 'Customer'
            //    accid: this.accid,
            //    currencyfilterfortrans: this.receiptObject.Currency.getValue(),
            //    personCode: this.personCode,
            //    personName: this.personName
            });
            //    this.UomSchemaWindow.on('beforeclose', function(winObj) {
            //        if(winObj.isSubmitBtnClicked) {
            //            this.addSelectedInvoices(winObj.getSelectedRecords());
            //        }
            //    }, this);
        this.UomSchemaWindow.show(); 
        } 
    },
    saveMasterGroupItem: function(btn, schemaName, isEdit, rowid,stockuomid){   
       if(btn=="ok"){
           if(schemaName.replace(/\s+/g, '')!=""){
                this.saveLocationBtn.disable();
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                     url:"ACCUoM/saveUOMSchemaType.do",
                    params: {
                        schemaName:schemaName,
                        rowid:rowid,
                        stockuomid:stockuomid,
                        isEdit:isEdit
                        
                    }
                },this,this.genSuccessResponse,this.genFailureResponse);
           }
        }
   },
    DeleteUOMSchemaType:function (){
          if(this.grid.getSelectionModel().hasSelection()){
           var arrID=[];
           var arrName=[];
           var rec = this.grid.getSelectionModel().getSelections();
           for(var i=0;i<this.grid.getSelectionModel().getCount();i++){
                arrID.push(rec[i].data['rowid']);
                arrName.push(rec[i].data['uomschematype'])
           }
       }
       
        

        Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.warning"),  //"Warning",
           msg: WtfGlobal.getLocaleText("acc.masterConfig.msg1"),  ///+"<div><b>"+WtfGlobal.getLocaleText("acc.masterConfig.msg1")+"</b></div>",
           width: 380,
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn=="ok"){
                    Wtf.Ajax.requestEx({
                        url:"ACCUoM/DeleteUOMSchemaType.do",
                        params: {
                                ids:arrID,
                                name:arrName
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);
                }
              
            }

        });
    },
     genSuccessResponse:function(response){
        Wtf.Ajax.timeout = 30000;
        if(response.success){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.MasterConfiguration"),response.msg],response.success*2+1);
            if(response.success){
                (function(){
                    this.Store.reload();                
                }).defer(WtfGlobal.gridReloadDelay(),this);
            }
             if(this.addUOMSchemaType) 
            this.addUOMSchemaType.close();
        }
        else{
            if(response.isused || response.success == false){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg]);
            } 
        }        
    },
    genFailureResponse:function(response){
        Wtf.Ajax.timeout = 30000;
        if(response.msg)
            var msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
     enableDisableButtons:function() {
        var rec = this.sm.getSelected();
        if(this.sm.getCount() == 1) {
              this.editButton.enable();
              this.deleteButton.enable();
              this.ConfigureUOMSchema.enable();
            
        } else {
              this.editButton.disable();
              this.deleteButton.disable();
              this.ConfigureUOMSchema.disable();
        }
          
    }
});
