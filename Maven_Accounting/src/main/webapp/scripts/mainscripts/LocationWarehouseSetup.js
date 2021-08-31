Wtf.account.LocationWarehouseSetup = function(config){
    Wtf.apply(this, config);
    Wtf.account.LocationWarehouseSetup.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.LocationWarehouseSetup, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.LocationWarehouseSetup.superclass.onRender.call(this, config);
        
        //create Grid
        if(this.getallcids == undefined){
           this.getallcids=false;                      //For accessing all customers related to warehouse. 
        }
        this.createMasterGrid();
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [ {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.masterGrid],
                tbar: this.btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.gridStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                })
            }]
        }); 
        this.add(this.leadpan);
    },
    
       AddMasterItem:function (isEdit){
        var editParent="";
        var groupName=(this.transType=='location')?"Location":((this.transType=='departments')?'Department':"Warehouse");

        var recArray = this.masterGrid.getSelectionModel().getSelections();                
        if(isEdit && (this.masterGrid.getSelectionModel().hasSelection()==false||this.masterGrid.getSelectionModel().getCount()>1)){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
          var rec = this.masterGrid.getSelectionModel().getSelected();
            this.customerComboRec = Wtf.data.Record.create ([
                {name:'accid'},
                {name:'id'},
                {name:'title'},
                {name:'accname'},
                {name:'accname'},
                {name:'acccode'},
                {name:'accnamecode'},
                {name:'address'},
                {name:'baddress2'},
                {name:'baddress3'},
                {name:'personname',mapping:'accname'},
                {name:'personemail',mapping:'email'},
                {name:'personid',mapping:'id'},
                {name:'company'},
                {name:'email'},
                {name:'contactno'},
                {name:'contactno2'},
                {name:'customerid'},
                {name:'shippingaddress'},
                {name:'shippingaddress2'},
                {name:'shippingaddress3'},
                {name: 'currencyname'},
                {name: 'currencyid'},
                {name: 'istaxeligible'},
                {name: 'contactperson'},
                {name: 'amountdue'},
                {name: 'mappingaccid'},
                {name: 'sequenceformat'},   
                {name: 'billingAddress1'},
                {name: 'billingAddress2'},

            ]);
            this.customerComboStore=new Wtf.data.Store({
            url: "ACCCustomer/getCustomersIdNameForCombo.do",
//            url: "ACCCustomerCMN/getAllCustomerWarehouse.do",
            baseParams:{      
                isEdit:isEdit
            },
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'totalCount',
                root: "data"
            },this.customerComboRec)
        });
        this.customerComboStore.on('beforeload',function(store, rec){
             WtfGlobal.setAjaxTimeOut();
        },this);
        this.customerComboStore.on('loadexception',function(store, rec){
             WtfGlobal.resetAjaxTimeOut();
        },this);
            this.customerComboStore.load();
             this.addMasterItemForm = new Wtf.form.FormPanel({
                waitMsgTarget: true,
                border: false,
                region: 'center',
                layout:'form',
                bodyStyle: "background: transparent;",
                border:false,
                style: "background: transparent;padding:10px;",
                labelWidth: 107,
                items: [this.CustomerCombo =  new Wtf.common.Select({
                fieldLabel:WtfGlobal.getLocaleText("acc.customerList.gridCustomer"),
                name:"customer",
                hiddenName:'customer',
                store:this.customerComboStore,
                width : 200,
                multiSelect:true,
                //style: "margin-left:4px;",
                allowBlank:false,
                valueField:'accid',
                displayField:'accname',
//                value: (isEdit)?rec.data['customerid']:"",
                mode: 'local',
                triggerAction:'all',
                forceSelection:true
            }),{
                        xtype: 'textfield',
                        fieldLabel: ((isEdit ? WtfGlobal.getLocaleText("acc.common.edit") + ' ' : WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew") + ' ') + groupName),
                        name: "masteritem",
//                        msgTarget: 'under',
                        //style: "margin-left:4px;",
                        width: 200,
                        maxLength:500,
//                        validator:(!this.paidToFlag)?Wtf.ValidateCustomColumnName:"",
                        validator:Wtf.ValidatePaidReceiveName,
                        value:(isEdit)?rec.data['name']:'',
                        allowBlank: false,
                        id: "masteritemname"
                    }]
            });     
             
            this.customerComboStore.on('load',function(s,o){
                WtfGlobal.resetAjaxTimeOut();
                if(isEdit){
                    this.CustomerCombo.setValue(rec.data['customerids']);
                }
            },this);

            this.saveLocationBtn = new Wtf.Button({
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                handler: function(button) {
                    var itemName=Wtf.getCmp("masteritemname").getValue();
                    var transactionName="warehouse";
                        var customerid=this.CustomerCombo.getValue();
                    if (this.addMasterItemForm.form.isValid()) {
                         this.saveMasterGroupItem("ok",itemName,isEdit,(isEdit?rec.data['id']:""),customerid,transactionName,(isEdit?rec.data['customerids']:""),(isEdit?rec.data['warehouse']:"")) 
                    } else {
                        return false;
                    }
                }
            });
            
            this.addMasterItemWindow = new Wtf.Window({
                modal: true,
                title: groupName,
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
                        items: [this.addMasterItemForm]
                    }],
                buttons: [this.saveLocationBtn, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope: this,
                        handler: function() {
                            this.addMasterItemWindow.close();
                        }
                    }]
            });

            this.addMasterItemWindow.show();

//        }
    },

      saveMasterGroupItem: function(btn, txt, isEdit, id,customerid, transactionName,customerids,warehouse){
        var callUrl = "ACCMaster/saveLocations.do";
        if(transactionName=='warehouse')
            callUrl = "ACCCustomerCMN/saveCustomerWarehouses.do";
        else if(this.transType=='departments')
            callUrl = "ACCMaster/saveDepartmentItem.do";
       var groupName=(this.transType=='location')?"Location":((this.transType=='departments')?'Department':"Warehouse");
       if(btn=="ok"){
           if(txt.replace(/\s+/g, '')!=""){
                this.saveLocationBtn.disable();
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                     url:callUrl,
                    params: {
                        mode:114,
                        name:txt,
                        id:id,
                        customerid:customerid,
                        customerids:customerids,
                        warehouse:warehouse,
                        isEdit:isEdit,
                        groupName:groupName,
                        isForCustomer:true
                    }
                },this,this.genSuccessResponse,this.genFailureResponse);
           }else{
               Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),  //'Master Configuration',
                    msg: WtfGlobal.getLocaleText("acc.field.Pleaseenternew")+this.masterStore.getAt(this.masterStore.find('id',masterid)).data['name'],
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO,
                    width: 300,
                    scope: this,
                    fn: function(){
                        if(btn=="ok"){
                            this.AddMasterItem(isEdit,masterid,outer);
                        }
                    }
                 });

           }
        } else if(outer){
           this.destroy();
       }
   },
     DeleteMasterItem:function (){
          if(this.masterGrid.getSelectionModel().hasSelection()){
           var arrID=[];
           var arrName=[];
//           var groupName=(this.transType=='location')?"Location":((this.transType=='departments')?'Department':"Warehouse");
           var groupName="warehouse";
           var rec = this.masterGrid.getSelectionModel().getSelections();
           for(var i=0;i<this.masterGrid.getSelectionModel().getCount();i++){
                arrID.push(rec[i].data['id']);
                arrName.push(rec[i].data['name'])
           }
       }
       
        var callUrl = "";
//        callUrl = "ACCMaster/deleteLocations.do";
        if(groupName=='warehouse')
            callUrl = "ACCCustomerCMN/deleteCustomerWarehouses.do";
        if(this.transType=='departments')
            callUrl = "ACCMaster/deleteDepartmentItem.do";
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
                        url:callUrl,
                        params: {
                                mode:116,
                                ids:arrID,
                                name:arrName,
                                groupName:groupName
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);
                }
              
            }

        });
    },
    
    genSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.MasterConfiguration"),response.msg],response.success*2+1);
            if(response.success){
                (function(){
                    this.gridStore.reload();                
                }).defer(WtfGlobal.gridReloadDelay(),this);
            }
             if(this.addMasterItemWindow) 
            this.addMasterItemWindow.close();
        }
        else{
            if(response.isused){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg]);
            }
        }        
    },
    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    updateGrid:function(){
        this.gridStore.reload();
    },
    
    createMasterGrid:function(){
        
        this.btnArr = [];
      
        
        this.createNewLocButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.Consignment.addlocation"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.Consignment.addlocation"),
            iconCls :getButtonIconCls(Wtf.etype.add),
             disabled :true,
             hidden:true, 
            handler:function (){
                this.createNewHandler(true);
             }
        });
        this.createNewwarehouseButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.Consignment.addwarehouse"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.Consignment.addwarehouse"),
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler:function (){
               this.AddMasterItem(false);
             }
        });
        
        this.editButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.Consignment.Editwarehouse"),//'Edit',
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:WtfGlobal.getLocaleText("acc.Consignment.Editwarehouse"),
             handler:function (){
               this.AddMasterItem(true);
            }
        });
        
        this.deleteButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),//'Delete',
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.menudelete), 
            tooltip:WtfGlobal.getLocaleText("acc.fixed.asset.delete.selected"),//'Delete to selected record',
            handler:function (){
                this.DeleteMasterItem();
            }
        });
            this.masterItemSearch = new Wtf.MyQuickSearch({
            field: 'name',
            hidden:true,
            emptyText:WtfGlobal.getLocaleText("acc.masterConfig.search1"),  //'Search by Master Item...',
            width: 150
         });

        this.btnArr.push(this.masterItemSearch,this.createNewLocButton,this.createNewwarehouseButton,this.editButton,this.deleteButton);
       
        this.gridRec = Wtf.data.Record.create ([
                {name:'id'},
                {name:'customer'},
                {name:'name'},
                {name:'title'},
                {name:'accname'},
                {name:'acccode'},
                {name:'accnamecode'},
                {name:'customerName'},
                {name:'mapingid'},
                {name:'customerids'},
                {name:'warehouse'},
                {name:'customerid'}
       ]);
        
        
        this.msgLmt = 30;
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        }, this.gridRec);
        
        this.gridStore = new Wtf.data.Store({
//            url:"ACCCustomerCMN/getCustomerWarehouses.do",
            url:"ACCCustomerCMN/getAllCustomerWarehouse.do",
            baseParams:{
                mode:22,
                isForCustomer:true,
                getallcids:this.getallcids
            },
            reader: this.gridStoreReader
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
        
        this.gridStore.load();
        
         this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
        
           
        this.colModel = new Wtf.grid.ColumnModel([this.sm,{
             header:WtfGlobal.getLocaleText("warehouse.customer.customerCode"),
            dataIndex:'accnamecode',//dataIndex:'accnamecode',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),  //"warehouse",
            dataIndex:'name',//dataIndex:'accnamecode',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            pdfwidth:75
        },{
          header:WtfGlobal.getLocaleText("acc.customerList.gridName"),  //" customer Name",
            dataIndex:'customerName',
            renderer:WtfGlobal.deletedRenderer,
            sortable: true,
            pdfwidth:75
         }]);
        
           this.masterGrid = new Wtf.grid.GridPanel({
            cm:this.colModel,
            store:this.gridStore,
            sm:this.sm,
            stripeRows :true,
            border:false,
            viewConfig:{
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
                forceFit:true
            }
        });
         this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);

        
    },
     enableDisableButtons:function() {
        var rec = this.sm.getSelected();
        if(this.sm.getCount() == 1) {
              this.editButton.enable();
              this.deleteButton.enable();
            
        } else {
              this.editButton.disable();
              this.deleteButton.disable();
        }
          
    }
});
