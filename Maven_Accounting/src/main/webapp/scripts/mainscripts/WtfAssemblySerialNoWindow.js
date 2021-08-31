/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.AssemblySerialNoWindow = function(config){
    this.modeName = config.modeName;
    this.value="1",
    //this.batchName="";
    this.modifiedStore = config.modifiedStore;
    this.parent=[];
    this.levelid=[];
    this.parentObj=config.parentObj;
    this.levelNm=[];
    /*
     *ERP-40741
     *@this.locationChangeForCloseWO - This flag used to identify whether user has clicked to change location or not. 
     *If yes then system will fetch those locations which are associated with selected warehouse. It will be true for close WO call.
     *@this.isCloseWO : This flag used to identify call for Close WO.
     */
    this.locationChangeForCloseWO = false;  
    this.isCloseWO=config.isCloseWO;
    
    this.serialNumberCMArr=[];
    this.isCN=config.isCN;
    this.isUnbuildAssembly = config.isUnbuildAssembly;  //Check for Unbuild-Assembly
    this.isForCustomerAssembly = config.isForCustomerAssembly;
    this.jobworkorderid = config.jobworkorderid;
    this.isCustBill=config.isCustBill;
    this.moduleid = config.moduleid;
    this.currentQuantity=config.currentQuantity;
    this.totalquantity=config.totalquantity;
    this.cntype = config.cntype;
    this.productType=config.type;
    this.bomid = config.bomid;
    this.isReverseCNDN = (this.cntype==4?true:false);
    if(this.isCN) {
        if(this.isReverseCNDN) {
            this.customerFlag = false;
        } else {
            this.customerFlag = true;
        }
    } else {
        if(this.isReverseCNDN) {
            this.customerFlag = true;
        } else {
            this.customerFlag = false;
        }
    }
    this.businessPerson=(this.customerFlag?"Customer":"Vendor");
    this.externalcurrencyrate=0;
    this.noteType=WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber");
    this.custPermType=config.isCN?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCN?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.isCustomer=config.isCN;
    this.isLocationForProduct=config.isLocationForProduct;  //product level batch option
    this.isWarehouseForProduct=config.isWarehouseForProduct;  //product level batch option
    this.isBatchForProduct=config.isBatchForProduct;  //product level batch option
    this.isSerialForProduct=config.isSerialForProduct; //product level serial option
    this.isSKUForProduct=config.isSKUForProduct; //product level SKU option
    this.productAssemblyGrid=config.productAssemblyGrid;
    this.costToBuild=config.costToBuild;
    this.BuildQuantity=config.buildQuantity;
    this.isFinishGood=config.isFinishGood;
    this.assemblyProductCost=config.assemblyProductCost;
    this.isfromProductAssembly = true;
    this.butnArr = new Array();
    this.submit=false;
    this.subProductJson=config.subProductJson;
    this.setValuesToMultipleRec=false;
    //    if((config.isLocationForProduct && config.isWarehouseForProduct && !this.isSerialForProduct) || (config.isLocationForProduct&& config.isWarehouseForProduct && this.isBatchForProduct && !this.isSerialForProduct)){
    //         this.setValuesToMultipleRec=true;
    //    }
    this.sm1 = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false,
        hidden:!this.setValuesToMultipleRec
    }); 
    this.serialNumberCMArr.push(this.sm1);
    
    this.previousButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.productserial.prevBtnText"), //'Previous',
        scope: this,
        minWidth: 80,
        hidden : true,
        //        hidden: (Wtf.totalQtyAddedInBuild == 0),
        handler: function () {
            this.fireEvent('beforclosewin', this);
            //            this.currentQuantity--;
            this.close();
        }
    });
    
    this.submitButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        minWidth: 80,
        hidden:!(Wtf.totalQtyAddedInBuild == this.totalquantity),
        handler: function() {
            this.submit=true;
            this.fireEvent('beforclosewin',this);
            if(this.validateAllDetails() && this.validateQuantityDetail()){
                this.currentQuantity= this.totalquantity- Wtf.totalQtyAddedInBuild;
                this.close();
            }
        }
    });
    
    this.cancelButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        minWidth: 80,
        handler: function() {
            if(this.isFinishGood == undefined){
                var issubmitted = true;//this to check whether once submiited.
                for(var i=0;i<this.totalquantity;i++){ 
                    if(this.parentObj.ProductSerialJson[i] == undefined && this.parentObj.ProductSerialJson[i] == null){
                        issubmitted = false;
                    }
                }
                if(!issubmitted){
                    this.subProductJson=[];
                    this.parentObj.ProductSerialJson=[];
                    this.parentObj.batchDetails=[];
                }
            }
            Wtf.totalQtyAddedInBuild=0;
            this.hide();
        }
    });
    
    this.nextButton = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.productserial.nextBtnText"),   //'Next',
        scope: this,
        minWidth: 80,
        hidden:(Wtf.totalQtyAddedInBuild == this.totalquantity),
        handler: function() {
            this.fireEvent('beforclosewin',this);
            if(this.validateAllDetails() && this.validateQuantityDetail()){
                this.currentQuantity= this.totalquantity- Wtf.totalQtyAddedInBuild;
                this.close();
            }
        }
    }); 

    this.butnArr.push(this.previousButton,this.submitButton,this.nextButton,this.cancelButton); 

    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.AssemblySerialNoWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'beforclosewin':true
    });
}

Wtf.extend(Wtf.account.AssemblySerialNoWindow, Wtf.Window, {

    onRender: function(config){
        Wtf.account.AssemblySerialNoWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        
        this.wareHouseRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"name"
        },
        {
            name: 'parentid'
        },
        {
            name: 'parentname'
        }
        ]);
        this.wareHouseReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.wareHouseRec);
        this.wareHouseStore = new Wtf.data.Store({
            url:"ACCMaster/getWarehouseItems.do",
            reader:this.wareHouseReader
        });
        this.wareHouseStore.load();

        this.wareHouseCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),
            valueField:'id',
            displayField:'name',
            store:this.wareHouseStore,
            lastQuery:'',
            disabled: true,
            typeAhead: true,
            forceSelection: true,
            name:'warehouse',
            hiddenName:'warehouse',
            width: 250
        });

        //        if(Wtf.account.companyAccountPref.activateInventoryTab){
        this.wareHouseCombo.on('select',function(){
            this.locationStore.load({
                params:{
                    storeid:this.wareHouseCombo.getValue()
                }
            });
            this.locationCombo.enable();
            this.setValuestoSelectedRecords(this.wareHouseCombo.getValue(),'warehouse');
        },this);
        //        }

        this.locationRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"name"
        },
        {
            name: 'parentid'
        },
        {
            name: 'parentname'
        }
        ]);
        this.locationReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.locationRec);
        var locationStoreUrl="ACCMaster/getLocationItems.do"
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            locationStoreUrl="ACCMaster/getLocationItemsFromStore.do";
        }
        this.locationStore = new Wtf.data.Store({
            url:locationStoreUrl,
            reader:this.locationReader
        });
        this.locationStore.load();

        this.locationCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            multiSelect:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.locationStore,
            typeAhead: true,
            //            allowBlank: false,
            disabled:true,
            forceSelection: true,
            hirarchical:true,
            name:'location',
            hiddenName:'location',
            width: 250
        });            

        this.locationCombo.on('select',function(){
                this.setValuestoSelectedRecords(this.locationCombo.getValue(),'location');
        },this);
        
        this.wrapperNorth = new Wtf.Panel({
            region :'north',
            height:40,
            border:false,
            disabledClass:"newtripcmbss",
                defaults:{border:false},
            layout:'form',
            baseCls:'northFormFormat',
            labelWidth:120,
            items:[{
                layout:'column',
                    defaults:{border:false},
                items:[{
                    layout:'form',
                    columnWidth:0.49,
                    items:[this.wareHouseCombo]
                },{
                    layout:'form',
                    columnWidth:0.49,
                    items:[this.locationCombo]
                }]
            }]
        });    
        
        this.createSubProductGrid();
        //this.expend.setValue(Wtf.serverDate);
        var batchrecords="";
        if(this.batchDetails!=undefined && this.batchDetails.length>1){
            batchrecords= eval('(' + this.batchDetails + ')');
        }

        var title=this.noteType;//this.isCN?WtfGlobal.getLocaleText("acc.cn.payType"):WtfGlobal.getLocaleText("acc.dn.payType");
        var msg="<b>Product</b> : "+this.productName+"<br> <b>Quantity</b> :"+this.totalquantity
        var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },this.wrapperNorth);
        //Adding Routing code template in case of only Work order Close with Routing Code create
        if(this.workorderid != undefined && this.workorderid != ""){
            createRCFormAndWindow(this.workorderid,this.isEdit,true,this);
            this.RoutingCodeNorthPanel = new Wtf.Panel({
                region :'north',
                //height:120,
                autoHeight:true,
                border:false,
                disabledClass:"newtripcmbss",
                    defaults:{border:false},
                layout:'form',
                baseCls:'northFormFormat',
                labelWidth:120,
                items:[this.routingcodeform]
            });
                
            this.add(this.RoutingCodeNorthPanel); 
        }
        this.add(this.centerPanel=new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan'+this.id,
            autoScroll:true,            
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            //            layout: 'fit',
            items:[this.grid,this.SubProductAssemblyGrid]
        }));
        if(this.setValuesToMultipleRec!=true){
            this.wrapperNorth.hide();
        }
        this.getLevelMapping();
    },
    getLevelMapping:function(){
        Wtf.Ajax.requestEx({
            url:"ACCMaster/getLevels.do"
        },this,function(res){
            for(var i=0;i<res.data.length;i++){
                this.parent.push(res.data[i].parent);
                this.levelNm.push(res.data[i].newLevelName);
                this.levelid.push(i+1);
            }
            this.createSerialNoCmArr();
        },this.genFailureResponse);  
    },
    createSerialNoCmArr:function(){
         
        var a=[];
        var temp=[];
        var colNm=[];
        var cntr=0;
        var cntr1=0;
        var flag=false;
        for(var i=0;i<this.parent.length;i++){
            if(this.parent[i]!=""){
                flag=true;
            }
        }
        if(flag){
            for(var i=0;i<this.parent.length;i++){
                if(this.parent[i]==0){
                    a[cntr]=this.levelid[i];
                    colNm[cntr]=this.levelNm[i];
                    temp[cntr1]=this.levelid[i];
                    cntr++;
                    cntr1++;
                //                    break;
                }
            }
            while(cntr1>0){
                cntr1--;    
                //            for(var i=0;i<this.parent.length;i++){
                var i=0;  
                while(i<this.parent.length){  
                    if(this.parent[i]==temp[cntr1]){
                        a[cntr]=this.levelid[i];
                        colNm[cntr]=this.levelNm[i];
                        temp[cntr1]=this.levelid[i];
                        cntr++;
                        i=0;
                        if(temp[cntr1]==''){
                            break;
                        }
                    }else{
                        i++;
                    }
                }
            }
        }else{
            for(var i=0;i<this.parent.length;i++){
                a[i]=this.levelid[i];
                colNm[i]=this.levelNm[i];
            }
        }
         
      
        for(var i=0;i<a.length;i++){
            if(a[i]==1){
                this.serialNumberCMArr.push({
                    header:colNm[i],
                    width:200,
                    hidden:!this.isWarehouseForProduct, //if without batch then hidden batch column
                    dataIndex:'warehouse',
                    renderer:Wtf.comboBoxRenderer(this.wareHouseEditor),
                    editor:this.readOnly?"":this.wareHouseEditor
                });
            }
            if(a[i]==2){
                this.serialNumberCMArr.push({
                    header:colNm[i],
                    width:200,
                    hidden:!this.isLocationForProduct, //if without batch then hidden batch column
                    dataIndex:'location',
                    renderer:Wtf.comboBoxRenderer(this.locationEditor),
                    editor:this.readOnly?"":this.locationEditor
                });   
            }
            if(a[i]==3){
                this.serialNumberCMArr.push(
                {
                    header:colNm[i],
                    width:200,
                    hidden:!this.isRowForProduct, //if without batch then hidden batch column
                    dataIndex:'row',
                    renderer:Wtf.comboBoxRenderer(this.rowCb),
                    editor:this.readOnly?"":this.rowCb
                }
                );
            }
            if(a[i]==4){
                this.serialNumberCMArr.push(
                {
                    header:colNm[i],
                    width:200,
                    hidden:!this.isRackForProduct, //if without batch then hidden batch column
                    dataIndex:'rack',
                    renderer:Wtf.comboBoxRenderer(this.rackCb),
                    editor:this.readOnly?"":this.rackCb
                }
                );
            }
            if(a[i]==5){
                this.serialNumberCMArr.push(
                {
                    header:colNm[i],
                    width:200,
                    hidden:!this.isBinForProduct, //if without batch then hidden batch column
                    dataIndex:'bin',
                    renderer:Wtf.comboBoxRenderer(this.binCb),
                    editor:this.readOnly?"":this.binCb
                });
            }
        }
        this.serialNumberCMArr.push(
        {
            isForCustomerAssembly:this.isForCustomerAssembly,
            header:(Wtf.jobWorkInFlowFlag!= undefined && Wtf.jobWorkInFlowFlag==true && this.isForCustomerAssembly==true && (this.productType=="Job Work Assembly" || this.productType=="Job Work Inventory"))? "Challan No":WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
            dataIndex:'batch',
            width:100,
            hidden:!this.isBatchForProduct, //if without batch then hidden batch column
            editor:this.readOnly?"":new Wtf.form.TextField({
                name:'batch'
            
            })
        });
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.field.MfgDate"),
            dataIndex:'mfgdate',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:!this.isBatchForProduct, //if without batch then hidden batch mfgdate column
            editor:this.readOnly?"":this.mfgdate=new Wtf.form.DateField({
                maxLength:255,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        });
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.field.ExpDate"),
            dataIndex:'expdate',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:!this.isBatchForProduct, //if without batch then hidden batch mfg enddate column
            editor:this.readOnly?"":this.expdate=new Wtf.form.DateField({
                maxLength:255,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        });
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),     //Available Quantity
            dataIndex:'availableQty',
            width:100,
            hidden:!this.isUnbuildAssembly, 
            editor:new Wtf.form.NumberField({
                allowBlank : false,
                allowNegative : false, 
                disabled : true
            })
        });
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.product.gridQty"),
            dataIndex:"quantity",
            hidden:(!this.isLocationForProduct && !this.isWarehouseForProduct && !this.isBatchForProduct && !this.isRowForProduct && !this.isRackForProduct && !this.isBinForProduct && this.isSerialForProduct && !this.isRowForProduct && !this.isRackForProduct && !this.isBinForProduct),
            width:100,
            //            editor:this.readOnly?"":this.serialQty=new Wtf.form.NumberField({allowBlank : false,allowNegative : false, disabled : true})
            editor:(this.readOnly?"":this.serialQty=new Wtf.form.NumberField({allowBlank : false,allowNegative : false,readOnly:true}))
        });
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridBalance"),
            dataIndex:"balance",
            hidden:true,
            width:100
        });
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.field.SerialNo"),
            dataIndex:'serialno',
            width:100,
            hidden:!this.isSerialForProduct, //if without batch then hidden batch mfg enddate column
            editor:this.readOnly?"":new Wtf.form.TextField({
                name:'memo'
            
            })
        });
        this.serialNumberCMArr.push({
           header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ValidFrom")+"'>"+WtfGlobal.getLocaleText("acc.field.ValidFrom")+"</span>",
            //   header:WtfGlobal.getLocaleText("acc.field.Exp.FromDate"),
            dataIndex:'expstart',
            width:200,
            hidden:!this.isSerialForProduct,
            renderer:WtfGlobal.onlyDateDeletedRenderer, 
            editor:this.readOnly?"":this.mfgdate=new Wtf.form.DateField({
                maxLength:255,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        });
        this.serialNumberCMArr.push({

            header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ExpiresOn")+"'>"+WtfGlobal.getLocaleText("acc.field.ExpiresOn")+"</span>",
            //     header:WtfGlobal.getLocaleText("acc.field.Exp.EndDate"),
            dataIndex:'expend',
            renderer:WtfGlobal.onlyDateDeletedRenderer, 
            width:200,
            hidden:!this.isSerialForProduct,
            editor:this.readOnly?"":this.mfgdate=new Wtf.form.DateField({
                maxLength:255,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        });
        this.serialNumberCMArr.push({
            //            header:WtfGlobal.getLocaleText("acc.product.sku"),
            header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
            dataIndex:'skufield',
            width:100,
            //            hidden:!this.isSerialForProduct, //if without batch then hidden batch mfg sku column
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
            editor:this.readOnly?"":new Wtf.form.TextField({
                name:'skufield'
            }) 
        });
            
        this.serialNumberCM=new Wtf.grid.ColumnModel(this.serialNumberCMArr);        
        this.grid.reconfigure(this.grid.getStore(),this.serialNumberCM);
        //            this.grid.getView().forceFit=true;
        this.grid.getView().refresh(); 
        
        this.serialQty.on('change', function (field,newval,oldval) {
            this.currentQuantity=newval;
            Wtf.totalQtyAddedInBuild=Wtf.totalQtyAddedInBuild-oldval;
            Wtf.totalQtyAddedInBuild=Wtf.totalQtyAddedInBuild+newval;
            this.SubProductAssemblyGrid.currentQty=newval;
            this.SubProductAssemblyGrid.updateQtyinAssemblyGrid();
            if(Wtf.totalQtyAddedInBuild == this.totalquantity){
                this.submitButton.show();
                this.nextButton.hide();
            }else if(Wtf.totalQtyAddedInBuild != this.totalquantity){
                this.submitButton.hide();
                this.nextButton.show();
            }
        }, this);
           
    },
    createSubProductGrid:function(){
        if(this.batchDetails != undefined && this.batchDetails != ""){
            var serialNoDetails = eval('(' + this.batchDetails + ')')[0].subproduct
        }
        var moduleId=undefined;
        if(this.moduleId == Wtf.Build_Assembly_Report_ModuleId || this.moduleId == Wtf.MRP_Work_Order_ModuleID){
            moduleId = this.moduleId;
        }
        this.SubProductAssemblyGrid = new Wtf.account.productAssemblyGrid({
            layout:"fit",
            cls:'gridFormat',
            region: 'center',
            isHiddenColumn:true,
            border:false,
            height:250,
            serialNoDetails:serialNoDetails,
            currentQuantity:this.currentQuantity,
            totalquantity:this.totalquantity,
            buildAssemblyObj:this,
            isForCustomerAssembly:this.isForCustomerAssembly,
            jobworkorderid:this.jobworkorderid,
            gridtitle:WtfGlobal.getLocaleText("acc.build.8"), //"Components Needed To Build",
            productid:null,
            rendermode:this.isUnbuildAssembly ? "unbuildproduct" : "buildproduct",
            isFinishGood:this.isFinishGood,
            mainassemblyproduct : this.productid, //Parent product in Assembly Product
            isUnbuildAssembly : this.isUnbuildAssembly,
            rendermode:"buildproduct",
            modifiedStore : this.modifiedStore,
            moduleId:moduleId,
            isCloseWO:this.isCloseWO // If work order is closing then isCloseWO flag is true otherwise false
        });
        if(!this.modifiedStore && !this.isUnbuildAssembly){    //In case of BOM Replacement, BOM Formula should not be load in Batch-Serial Window.
            this.SubProductAssemblyGrid.setProduct(this.productid,this.workorderid);
        }        
    },
    createDisplayGrid:function(){
        this.sm = new Wtf.grid.CheckboxSelectionModel({    //my ch
            //            singleSelect :true
            });
        this.srnocm= new Wtf.grid.ColumnModel([this.sm,new Wtf.grid.RowNumberer(),{
            header:"Name",
            width:200,
            dataIndex:'serialno'
        },{
            header:'Exp.From Date',
            dataIndex:'expstart',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            format:WtfGlobal.getOnlyDateFormat()
        },{
            header:'Exp.End Date',
            dataIndex:'expend',
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
            dataIndex:'skufield'
        }]);
       
       
        this.srnoRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'serialno'
        },
        {
            name: 'serialnoid'
        },
        {
            name: 'expstart', 
            type:'date'
        },
        {
            name: 'expend', 
            type:'date'
        },{
            name: 'purchaseserialid'
        },{
            name:'skufield',
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm
        }, {
            name: 'purchaseprice'
        }
        ]);
        this.srnostore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.srnoRec),
            url:"ACCMaster/getNewSerials.do",
            baseParams:{
                productid:this.productid,
                duplicatecheck:true,
                isEdit:this.isEdit,
                fetchPurchasePrice:true,
                copyTrans:this.copyTrans,
                billid:this.billid
            }
        });
        
        this.srnostore.load();  //mych
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect :true,                       
            hidden:this.isSalesCommissionStmt
        });
        this.batchnocm= new Wtf.grid.ColumnModel([this.sm,new Wtf.grid.RowNumberer(),{
            header:"Name",
            width:200,
            dataIndex:'batchname'
        },{
            header:'Mfg Date',
            dataIndex:'mfgdate',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:'Exp Date',
            dataIndex:'expdate',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        }]);
       
       
        this.batchNoRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'warehouse'
        },
        {
            name: 'location'
        },
        {
            name: 'row'
        },
        {
            name: 'rack'
        },
        {
            name: 'bin'
        },
        {
            name: 'mfgdate' , 
            type:'date'
        },
        {
            name: 'expdate', 
            type:'date'
        },
        {
            name: 'batch'
        },
        {
            name: 'batchname'
        }
        ]);
        this.batchNostore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.batchNoRec),
            url:"ACCMaster/getNewBatches.do",
            baseParams:{
                productid:this.productid,
                jobworkorderid:this.jobworkorderid, // to fetch those batches, which are used in Stock adjustment which are linked to job work order having id in jobworkorderid
                isUnbuildAssembly : this.isUnbuildAssembly  //To fetch those Batches only having quantity > 0
            
            }
        });
        
        this.batchNostore.load();  //loadded all the record to chechk the functionality of duplicate batch name
       
        this.locationRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        },

        {
            name: 'parentid'
        },

        {
            name: 'parentname'
        }
        ]);
        this.locationReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.locationRec);
        var locationurl = "ACCMaster/getLocationItems.do";  //Normal Case
        if(this.isUnbuildAssembly || (this.workorderid!=undefined && this.workorderid!="")){
            locationurl="ACCMaster/getLocationItemsFromStore.do";   //Dissamble Case
        }
        this.locationStore = new Wtf.data.Store({
            url:locationurl,
            reader:this.locationReader
        });
       
       
        this.wareHouseRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        },

        {
            name: 'parentid'
        },
        {
            name: 'location'
        },

        {
            name: 'parentname'
        }
        ]);
        this.wareHouseReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.wareHouseRec);

        this.wareHouseStore = new Wtf.data.Store({
            url:"ACCMaster/getWarehouseItems.do",
            reader:this.wareHouseReader
        });
       
        this.wareHouseStore.load();
        this.locationStore.load();        
        this.locationStore.on("load",function(){
            var batchrecords="";
            if(this.batchDetails!=undefined && this.batchDetails.length>1){
                batchrecords= eval('(' + this.batchDetails + ')');
            }
            if(!this.locationChangeForCloseWO){
            if((!this.isEdit || this.copyTrans)  && batchrecords.length==0){
                if((this.isBatchForProduct || this.isLocationForProduct || this.isWarehouseForProduct || this.isRowForProduct  || this.isRackForProduct  || this.isBinForProduct) &&  !this.isSerialForProduct && !this.linkflag){
                    this.recordLength=1; //if only batch option is on then only one batch is added so only one rwo is shown
                }else if(!(this.isBatchForProduct || this.isLocationForProduct || this.isWarehouseForProduct || this.isRowForProduct  || this.isRackForProduct  || this.isBinForProduct || this.isSerialForProduct)){
                    this.recordLength=1; //if LWRRBBS is not enabled then only one rwo is shown
                }else{
                    this.recordLength=this.currentQuantity;
                }
                for(var i=0;i<this.recordLength;i++){
                    if(batchrecords.length>0 && i<=batchrecords.length){
                        var batchObj=batchrecords[i];
                        var isReadOnly=batchObj.isreadyonly;                    
                        this.addGridRec(batchObj,(i==0),isReadOnly);
                    }else{
                        if((this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct  || this.isRowForProduct  || this.isRackForProduct  || this.isBinForProduct) && this.isSerialForProduct && i!=0){
                            this.addGridRec(undefined,(i==0),true);
                        }else{
                                this.addGridRec(undefined,(i==0),false);                                
                            }
                            }
                        } 
                    }
            else{
                var recordQuantity=batchrecords.length;
                if(this.isSerialForProduct)
                {
                    if(this.currentQuantity<batchrecords.length){
                        recordQuantity=this.currentQuantity;   
                    }
                }
                if(batchrecords.length!=0){
                    for(var i=0;i<recordQuantity;i++){
                        var batchObj=batchrecords[i];
                        var isReadOnly=batchObj.isreadyonly;                    
                        this.addGridRec(batchObj,(i==0),isReadOnly);
                    }
                }else{
                    this.recordLength=this.currentQuantity;
                    for(var i=0;i<this.recordLength;i++){
                    
                        if((this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct  || this.isRowForProduct  || this.isRackForProduct  || this.isBinForProduct) && this.isSerialForProduct && i!=0){
                            this.addGridRec(undefined,(i==0),true);
                        }else{
                            this.addGridRec(undefined,(i==0),false);
                        }
        
                    }
                }
               
                
            }
        }
        this.locationChangeForCloseWO = false;
    },this);
        
        this.locationEditor = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            store:this.locationStore,
            lastQuery:'',
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'location',
            hiddenName:'location'

        });        
        this.levelRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        },{
            name: 'parentid'
        }

        ]);
        this.levelReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.levelRec);
        this.rowStore = new Wtf.data.Store({
            url:"ACCMaster/getStoreMasters.do",
            id:'rowStoreId',
            reader:this.levelReader,
            baseParams:{
                transType:'row'
            }
        });
        this.rowStore.load();
        this.rackStore = new Wtf.data.Store({
            url:"ACCMaster/getStoreMasters.do",
            reader:this.levelReader,
            baseParams:{
                transType:'rack'
            }
        });
        this.rackStore.load();
        this.binStore = new Wtf.data.Store({
            url:"ACCMaster/getStoreMasters.do",
            reader:this.levelReader,
            baseParams:{
                transType:'bin'
            }
        });
        this.binStore.load();
        this.rowCb = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.rowStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'row',
            hiddenName:'row'

        });
        this.rackCb = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.rackStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'rack',
            hiddenName:'rack'

        });
        this.binCb = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.binStore ,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'bin',
            hiddenName:'bin'

        });
      
        this.wareHouseEditor = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
            valueField:'id',
            displayField:'name',
            store:this.wareHouseStore,
            anchor:'90%',
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            name:'warehouse',
            hiddenName:'warehouse'

        });
        /*       
        this.serialNumberCM= new Wtf.grid.ColumnModel([{
            header:"Location",
            width:200,
            hidden:!this.isLocationForProduct, //if without batch then hidden batch column
            dataIndex:'location',
            renderer:Wtf.comboBoxRenderer(this.locationEditor),
            editor:this.locationEditor
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
            width:200,
            hidden:!this.isWarehouseForProduct, //if without batch then hidden batch column
            dataIndex:'warehouse',
            renderer:Wtf.comboBoxRenderer(this.wareHouseEditor),
            editor:this.wareHouseEditor
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
            dataIndex:'batch',
            hidden:!this.isBatchForProduct, //if without batch then hidden batch column
            width:100,
            editor:new Wtf.form.TextField({
                name:'batch'
            
            })
        },{
            header:WtfGlobal.getLocaleText("acc.field.MfgDate"),
            dataIndex:'mfgdate',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:!this.isBatchForProduct, //if without batch then hidden batch mfgdate column
            editor:this.mfgdate=new Wtf.form.DateField({
                maxLength:255,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        },{
            header:WtfGlobal.getLocaleText("acc.field.ExpDate"),
            dataIndex:'expdate',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:!this.isBatchForProduct, //if without batch then hidden batch mfgdate column
            editor:this.expdate=new Wtf.form.DateField({
                maxLength:255,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        },{
            header:WtfGlobal.getLocaleText("acc.product.gridQty"),
            dataIndex:"quantity",
            hidden:(!this.isLocationForProduct && !this.isWarehouseForProduct && !this.isBatchForProduct && this.isSerialForProduct),
            width:100
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridBalance"),
            dataIndex:"balance",
            hidden:true,
            width:100
        },{
            header:WtfGlobal.getLocaleText("acc.field.SerialNo"),
            dataIndex:'serialno',
            hidden:!this.isSerialForProduct, //if without batch then hidden batch mfg enddate column
            width:100,
            editor:new Wtf.form.TextField({
                name:'memo'
            
            })
        },{
            header:"<span wtf:qtip='Warranty Expiry From Date'>Warranty Exp. From Date</span>",
            dataIndex:'expstart',
            width:100,
            hidden:!this.isSerialForProduct, //if without batch then hidden batch mfg enddate column
            renderer:WtfGlobal.onlyDateDeletedRenderer, 
            editor:this.mfgdate=new Wtf.form.DateField({
                maxLength:255,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        },{
            header:"<span wtf:qtip='Warranty Expiry End Date'>Warranty Exp. End Date</span>",
            dataIndex:'expend',
            renderer:WtfGlobal.onlyDateDeletedRenderer, 
            width:100,
             hidden:!this.isSerialForProduct, //if without batch then hidden batch mfg enddate column
            editor:this.mfgdate=new Wtf.form.DateField({
                maxLength:255,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield'
            })
        }]);
       */
        //      if(this.isUnbuildAssembly){   //For Selected Warehouse, associated locations will be get here for Parent Product
        //            this.wareHouseEditor.on('select',function(){
        //                this.locationStore.on("beforeload",function(){
        //                    this.locationEditor.store.removeAll(); 
        //                },this)
        //                this.locationEditor.store.load({
        //                    params:{
        //                        storeid:this.wareHouseEditor.getValue()
        //                    }
        //                });
        //            },this);
        //        }      
        this.serialNumberCM=new Wtf.grid.ColumnModel(this.serialNumberCMArr);
       
       
        this.accRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'location'
        },
        {
            name: 'availableQty', 
            defaultValue : 0
        },

        {
            name: 'warehouse'
        },{
            name: 'row'
        },{
            name: 'rack'
        },{
            name: 'bin'
        },{
            name: 'productid'
        },
        {
            name: 'mfgdate' , 
            type:'date'
        },
        {
            name: 'expdate', 
            type:'date'
        },
        {
            name: 'quantity',defValue:1
        },
        {
            name: 'balance'
        },
        {
            name: 'serialno'
        },
        {
            name: 'serialnoid'
        },
        {
            name: 'expstart', 
            type:'date'
        },
        {
            name: 'expend', 
            type:'date'
        },
        {
            name: 'batch'
        },
        {
            name: 'batchname'
        },
        {
            name: 'purchasebatchid'
        },
        {
            name: 'purchaseserialid'
        },
        {
            name: 'isserialusedinDO'
        },
        {
            name: 'isreadyonly'
        },
        {
            name: 'lockquantity'
        },{
            name: 'skufield',
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm
        }
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
        this.wareHouseEditor.on('select', function () {
            if (this.isCloseWO != undefined && this.isCloseWO) {     //ERP-40741 : This code will be execute only when user is closing WO.
                this.locationChangeForCloseWO = true;
                this.locationStore.on("beforeload", function () {
                    this.locationEditor.store.removeAll();
                }, this);
                this.locationEditor.store.load({
                    params: {
                        storeid: this.wareHouseEditor.getValue()
                    }
                });
            }
        }, this);
        
        this.grid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            sm:this.sm1,
            height:230,
            width:'97%',
            store: this.store,
            cm: this.serialNumberCM,
            border : false,
            loadMask : true,
            viewConfig: {
                //                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.sm1.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
        this.sm1.on("beforerowselect", this.checkSelections, this);
        this.grid.on('beforeedit',this.checkrecord,this);
        this.grid.on('cellclick',this.setWOCloseDetails,this);  //ERP-40516 : Location should be filter on already set Warehouse
        this.grid.on('afteredit',this.checkAvailableQty,this);
        this.grid.on('afteredit',this.checkDuplicateSerialName ,this);
        this.grid.on('afteredit',this.checkDuplicateBatchlName ,this); //loadded all the record to chechk the functionality of duplicate batch name        
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },    
    setWOCloseDetails: function (grid, rowIndex, columnIndex, e) {
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        if (fieldName == "location" && record.data != undefined) {
            this.locationChangeForCloseWO = true;   //To avoid adding additional duplicate record in grid.
            this.locationStore.on("beforeload", function () {
                    this.locationEditor.store.removeAll();
                }, this);
            this.locationEditor.store.load({
                params: {
                    storeid: record.data.warehouse
                }
            });
        }
    },
    checkrecord:function(obj){
        
        if((this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct ) && this.isSerialForProduct ){
            if(obj.record.data.isreadyonly && obj.record.data.isreadyonly!="false"){
                if(obj.field=='warehouse' || obj.field=='location' || obj.field=='batch' || obj.field=='mfgdate' || obj.field=='expdate' || obj.field=='quantity' || obj.field=='row' || obj.field=='rack' || obj.field=='bin'){
                    obj.cancel=true;  
                    return;
                }   
            }
        }
        if(obj.record.data.purchasebatchid != "" && obj.record.data.purchasebatchid != undefined && this.isBatchForProduct)
        {
            this.purchasebatchid=obj.record.data.purchasebatchid;
        }
        if(this.store.getCount()>1 && this.isBatchForProduct) //if quantity is changed manually and then to get batchid for serial no directly give batch of first record
        {
            this.purchasebatchid=this.store.getAt(0).data['purchasebatchid'];
        }
            
            
        if(obj.field=="batch" && (!obj.record.data.isreadyonly || obj.record.data.isreadyonly=="false")){
                
            if((obj.record.get("location")=="" || obj.record.get("location")==undefined) && this.isLocationForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.batch.select.location")], 2);
                return false;
            }
            if((obj.record.get("warehouse")=="" || obj.record.get("warehouse")==undefined)  && this.isWarehouseForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.batch.select.warehouse")], 2);
                return false;
            }
        }
        
        if(obj.field=="serialno" && (!obj.record.data.isreadyonly || obj.record.data.isreadyonly=="false")){
            if((obj.record.get("location")=="" || obj.record.get("location")==undefined) && this.isLocationForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocalText("acc.inventorysetup.warning.serial.select.location")], 2);
                return false;
            }
            if((obj.record.get("warehouse")=="" || obj.record.get("warehouse")==undefined) && this.isWarehouseForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.serial.select.warehouse")], 2);
                return false;
            }    
            if((obj.record.get("row")=="" || obj.record.get("row")==undefined) && this.isRowForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.serial.select.row")], 2);
                return false;
            } 
            if((obj.record.get("rack")=="" || obj.record.get("rack")==undefined) && this.isRackForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.serial.select.rack")], 2);
                return false;
            } 
            if((obj.record.get("bin")=="" || obj.record.get("bin")==undefined) && this.isBinForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.serial.select.bin")], 2);
                return false;
            } 
            if(obj.record.get("batch")=="" && this.isBatchForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.batch.empty")], 2);
                return false;
            }
            
        }
        //this.isblokedinso flag for in consignment do which is linked to consig request and we have blocked serial no in that then in do it shouldnt editable
        if((this.isFixedAsset && this.isSales) ||  this.fromPO  || this.isblokedinso)  //in fixed asset deliveryorder serial no shoulnot be editable  
        {
            obj.cancel=true;
        }
        //        else if(obj.column<7 && obj.row!=0){
        //            obj.cancel=true;
        //        }
        else if(this.isSales || this.isUnbuildAssembly){    //Show Batches in Batch Window for Selected Assembly Product
            if(obj!=null){      
             
                if(obj.field=="batch"){
                    obj.cancel=true;
                    this.openBatchWindow(obj);
                    return; 
                
                }else if(obj.field=="serialno"){
                    if(this.isBatchForProduct && (!obj.record.data.isreadyonly || obj.record.data.isreadyonly=="false")){
                        if(obj.record.get("batch")=="" ||obj.record.get("batch")==undefined){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.batch.empty")], 2);
                            return false;
                        }
                    }
                    obj.cancel=true;
                    var expstartdate=null; 
                    var expendate=null;   
                    if(obj.record.get("expstart")=="" ||obj.record.get("expstart")==undefined);
                    {
                        obj.record.set("expstart", Wtf.serverDate.clearTime(true));
                        expstartdate=Wtf.serverDate.clearTime(true);
                    }
                    if(this.warrantyperiodsal=="" || this.warrantyperiodsal==undefined) {
                        obj.record.set("expend", Wtf.serverDate.clearTime(true));
                        expendate=Wtf.serverDate.clearTime(true);
                    }else
                    {
                        expendate = new Date(obj.record.get("expstart")).add(Date.DAY,this.warrantyperiodsal);
                        obj.record.set("expend",expendate.clearTime(true));
                        
                    }
                    this.openSerialWindow(obj,expstartdate,expendate);
                    return;  
                }
            }
        }
        var parentLevelIdIndex="0";
        var dataIndex="";
        
        if(obj.field=="location"){
            parentLevelIdIndex="0"; 
            dataIndex="location";
        }if(obj.field=="warehouse"){
            parentLevelIdIndex="1"; 
            dataIndex="warehouse";
        }else if(obj.field=="row"){
            parentLevelIdIndex="2";
            dataIndex="row";
        }else if(obj.field=="rack"){
            parentLevelIdIndex="3"; 
            dataIndex="rack";
        }else if(obj.field=="bin"){
            parentLevelIdIndex="4"; 
            dataIndex="bin";
        }
        
        
        if(obj.field=="warehouse" || obj.field=="location" || obj.field=="row" || obj.field=="rack" || obj.field=="bin"){
            var parentLevelId=this.parent[parentLevelIdIndex]; 
            var parentval="";
            switch(parentLevelId){
                case '2':
                    parentval=obj.record.data.location;
                    break;
                case '1':
                    parentval=obj.record.data.warehouse;
                    break;
                case '3':
                    parentval=obj.record.data.row;;
                    break;
                case '4':
                    parentval=obj.record.data.rack;
                    break;
                case '5':
                    parentval=obj.record.data.bin;
                    break;
            }
            if(parentval!=''){
                for (var k = 0; k < obj.grid.colModel.config.length; k++) {
                    if(obj.grid.colModel.config[k].editor && obj.grid.colModel.config[k].editor.field.store && obj.grid.colModel.config[k].dataIndex==dataIndex){ 
                        var store = obj.grid.colModel.config[k].editor.field.store;
			var rec = store.getAt(k);
                        if(rec.data.parentid==""){	//ERP-25603
                            continue;
                        }
                        store.clearFilter();
                        store.filterBy(function(rec) {
                            if (parentval == rec.data.parentid)
                                return true
                            else 
                                return false
                        }, this);
                    }
                }
            }
        
        }
        
    },
     
        
    openSerialWindow:function(obj){ 
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            //            singleSelect :true
            });
        this.batchcm= new Wtf.grid.ColumnModel([this.sm,new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),
            width:200,
            dataIndex:'serialno'
        //        },{
        //            header:'Exp.From Date',
        //            dataIndex:'expstart',
        //            width:100,
        //            renderer:WtfGlobal.onlyDateDeletedRenderer
        //        },{
        //            header:'Exp.End Date',
        //            dataIndex:'expend',
        //            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
            dataIndex:'skufield'
        }]);
       
       
        this.batchRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'serialno'
        },
        {
            name: 'serialnoid'
        },
        {
            name: 'expstart', 
            type:'date'
        },
        {
            name: 'expend', 
            type:'date'
        },{
            name: 'purchaseserialid'
        },{
            name: 'purchasebatchid'
        },{
            name:'skufield'
        }
        ]);
        var serialbatch=obj.record.data.purchasebatchid;
        var seriallocation=obj.record.data.location;
        var serialrow=obj.record.data.row;
        var serialrack=obj.record.data.rack;
        var serialbin=obj.record.data.bin;
        var serialwarehouse=obj.record.data.warehouse;
        for(var cnt=obj.row;cnt>=0;cnt--){
            var rowData=this.store.getAt(cnt);
            if(!rowData.data.isreadyonly && rowData.data.isreadyonly!=undefined){
                serialbatch=rowData.data.purchasebatchid;
                seriallocation=rowData.data.location;
                serialrow=rowData.data.row;
                serialrack=rowData.data.rack;
                serialbin=rowData.data.bin;
                serialwarehouse=rowData.data.warehouse;
                break;
            }
        }
         

        this.batchstore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.batchRec),
            url:"ACCMaster/getNewSerials.do",
            baseParams:{
                //batch:obj.record.data.purchasebatchid
                batch:serialbatch,
                transType:this.moduleid,
                location:seriallocation,
                warehouse:serialwarehouse,
                row:serialrow,
                rack:serialrack,
                fetchPurchasePrice:true,
                bin:serialbin,
                transactionid:this.transactionid,
                productid:this.productid,
                isDO:this.isDO,
                isEdit:this.isEdit,
                copyTrans:this.copyTrans,
                billid:this.billid
            }
        });
        
        this.batchstore.load();
       
        this.batchgrid = new Wtf.grid.GridPanel({
            height:210,
            width:'97%',
            store: this.batchstore,
            cm: this.batchcm,
            sm:this.sm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        var batchWin = new Wtf.Window({
            height : 300,
            width : 400,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            title : WtfGlobal.getLocaleText("acc.field.SelectSerialNo"),
            bodyStyle : 'padding:5px;background-color:#ffffff;',
            layout : 'border',
            resizable : false,
            items : [{
                region : 'center',
                border : false,
                autoScroll : true,
                bodyStyle : 'padding:5px;background-color:#f1f1f1;',
                items:[this.batchgrid]
            }],
            buttons : [{
                text : WtfGlobal.getLocaleText("acc.msgbox.ok"),
                scope:this,
                handler : function(){
                    if(this.sm.getCount()==0){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.sr")],2);
                        return false;
                    }else{
                        this.rec = this.sm.getSelections();
                        for(var i=0;i<this.rec.length;i++){
                            if(this.isUnbuildAssembly && this.rec.length > this.totalquantity){ //In case of compare total unbuild quantity with record no.of grid. So we can select serial no.
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.please")+" "+this.quantity+" "+WtfGlobal.getLocaleText("acc.field.srnoq")],2);
                                return false;
                            }else if(this.rec.length>this.quantity && !this.isUnbuildAssembly) //In case of Unbuil Assembly, bypass this exception. We cannnot compare with quantity.
                            {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.please")+" "+this.quantity+" "+WtfGlobal.getLocaleText("acc.field.srnoq")],2);
                                return false;
                            }
                            //Set Expiry start date & Expiry end date   //ERP-23242
                            var expstartdate=null; 
                            var expendate=null;   
                            if(obj.record.get("expstart")=="" ||obj.record.get("expstart")==undefined);
                            {
                                obj.record.set("expstart", Wtf.serverDate.clearTime(true));
                                expstartdate=Wtf.serverDate.clearTime(true);
                            }
                            if(this.warrantyperiodsal=="" || this.warrantyperiodsal==undefined) {
                                obj.record.set("expend", Wtf.serverDate.clearTime(true));
                                expendate=Wtf.serverDate.clearTime(true);
                            }else
                            {
                                expendate = new Date(obj.record.get("expstart")).add(Date.DAY,this.warrantyperiodsal);
                                obj.record.set("expend",expendate.clearTime(true));
                        
                            }
                            this.store.data.items[obj.row+i].data.serialno=this.rec[i].data.serialno
                            this.store.data.items[obj.row+i].data.serialnoid=this.rec[i].data.serialnoid
                            this.store.data.items[obj.row+i].data.expstart=expstartdate
                            this.store.data.items[obj.row+i].data.expend=expendate
                            this.store.data.items[obj.row+i].data.skufield=this.rec[i].data.skufield
                            this.store.data.items[obj.row+i].data.purchaseserialid=this.rec[i].data.purchaseserialid
                            if(!this.isBatchForProduct || this.isUnbuildAssembly){
                                this.store.data.items[obj.row+i].data.purchasebatchid=this.rec[i].data.purchasebatchid  //kept the purchase batch id in case of batch option is not selected 
                            }
                        }
                        this.grid.getView().refresh();
                      
                    }
                    batchWin.close();
                }
            }],
            autoScroll : true,
            modal : true
        });

        batchWin.show();
        
   
   
    },
    openBatchWindow:function(obj){ 
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect :true,                          //this.isRequisition ? false : true,
            hidden:this.isSalesCommissionStmt
        });
        this.batchcm= new Wtf.grid.ColumnModel([this.sm,new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),
            width:200,
            dataIndex:'batchname'
        },{
            header:WtfGlobal.getLocaleText("acc.field.MfgDate"),
            dataIndex:'mfgdate',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.ExpDate"),
            dataIndex:'expdate',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        }]);
       
       
        this.batchRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'warehouse'
        },
        {
            name: 'location'
        },
        {
            name: 'row'
        },
        {
            name: 'rack'
        },
        {
            name: 'bin'
        },
        {
            name: 'mfgdate' , 
            type:'date'
        },
        {
            name: 'expdate', 
            type:'date'
        },
        {
            name: 'batch'
        },
        {
            name: 'batchname'
        },
        {
            name: 'purchaseprice'
        }
        ]);
        this.batchstore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.batchRec),
            url:"ACCMaster/getNewBatches.do",
            baseParams:{
                location:(this.isLocationForProduct)?obj.record.data.location:"",
                warehouse:(this.isWarehouseForProduct)?obj.record.data.warehouse:"",
                row:(this.isRowForProduct)?obj.record.data.row:"",
                rack:(this.isRackForProduct)?obj.record.data.rack:"",
                bin:(this.isBinForProduct)?obj.record.data.bin:"",
                transType:this.moduleid,
                transactionid:this.transactionid,
                productid:this.productid,
                ispurchase:(this.transactionType!=undefined && this.transactionType)?"":true,
                isUnbuildAssembly : this.isUnbuildAssembly  //To fetch those Batches only having quantity > 0
            }
        });
        
        this.batchstore.load();
       
        this.batchgrid = new Wtf.grid.GridPanel({
            height:210,
            width:'97%',
            store: this.batchstore,
            cm: this.batchcm,
            sm:this.sm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        var batchWin = new Wtf.Window({
            height : 300,
            width : 400,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            title : WtfGlobal.getLocaleText("acc.field.SelectBatch"),
            bodyStyle : 'padding:5px;background-color:#ffffff;',
            layout : 'border',
            resizable : false,
            items : [{
                region : 'center',
                border : false,
                autoScroll : true,
                bodyStyle : 'padding:5px;background-color:#f1f1f1;',
                items:[this.batchgrid]
            }],
            buttons : [{
                text : WtfGlobal.getLocaleText("acc.msgbox.ok"),
                scope:this,
                handler : function(){
                    if(this.sm.getCount()==0){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.br")],2);
                        return false;
                    }else{
                        this.rec = this.sm.getSelected();
                        if(this.rec.data.mfgdate!=null && this.rec.data.mfgdate!=""){
                            obj.record.set("mfgdate",this.rec.data.mfgdate.clearTime(true));
                        }else{
                            obj.record.set("mfgdate",'');
                        }
                        if(this.rec.data.expdate!=null && this.rec.data.expdate!=""){
                            obj.record.set("expdate",this.rec.data.expdate.clearTime(true));
                        }else{
                            obj.record.set("expdate",'');
                        }
                        obj.record.set("batch",this.rec.data.batchname);
                        obj.record.set("batchname",this.rec.data.batchname);
                        obj.record.set("purchasebatchid",this.rec.data.batch);
                    //this.batchName = this.rec.data.batchname;
                    }
                    //                    if(this.rec.data.batchname!=obj.value){  //if cuurently selected batch is not equal to another batch den reset serial no details
                    //                      this.store.each(function(rec){
                    //                         rec.set('serialno','')
                    //                        rec.set('expstart','')
                    //                        rec.set('expend','')
                    //                    },this);  
                    //                    }
                    var filterJson='[';
                    filterJson+='{"location":"'+this.rec.data.location+'","warehouse":"'+this.rec.data.warehouse+'","productid":"'+this.rec.data.productid+'","documentid":"'+this.documentid+'","purchasebatchid":"'+this.rec.data.batch+'"},';
                    filterJson=filterJson.substring(0,filterJson.length-1);
                    filterJson+="]";
                    this.checkAvailableQty(obj,filterJson);
                    batchWin.close();
                }
            }],
            autoScroll : true,
            modal : true
        });

        batchWin.show();
        
   
   
    },
    
    checkSelections:function( scope, rowIndex, keepExisting, record){
        if(rowIndex== (this.store.getCount()-1)){
            return false;
        }else{
            return true;
        }

    },
    enableDisableButtons:function(){
        if(this.sm1.getCount()>=1){
            if(this.wareHouseCombo)this.wareHouseCombo.enable();
            if(this.locationCombo)this.locationCombo.enable();
        }else{
            if(this.wareHouseCombo)this.wareHouseCombo.disable();
            if(this.locationCombo)this.locationCombo.disable();
        }
    },
    setValuestoSelectedRecords:function(presentValue,field){                           
        
        this.recArr = this.grid.getSelectionModel().getSelections();
        var arr=[];
        if(field=="warehouse"){
            for(var k=0;k< this.recArr.length;k++){
                var rec=this.recArr[k];
                if(rec.data!=null || rec.data != 'undefined'){  
                    rec.set(field,presentValue);
                    rec.set("avlquantity",0); 
                    rec.set("location",""); 
                    rec.set("batch",""); 
                    rec.set("batchname",""); 
                }
            }
        } else if(field=="location" && ( this.isSales ==false || this.isBatchForProduct)){
            for(var k=0;k< this.recArr.length;k++){
                rec=this.recArr[k];
                if(rec.data!=null || rec.data != 'undefined'){  
                    rec.set(field,presentValue);
                    rec.set("batch",""); 
                    rec.set("batchname",""); 
                }
            }    
        } else if(field=="location" ){
            
            var store=this.grid.getStore();
            var fields=store.fields;
            for(var i=0;i<this.recArr.length;i++){
                rec=this.recArr[i];
                var recarr=[];
                for(var j=0;j<fields.length;j++){
                    var value=rec.data[fields.get(j).name];
                    if(fields.get(j).name=='customfield' || fields.get(j).name==CUSTOM_FIELD_KEY_PRODUCT || fields.get(j).name=='batchdetails' || fields.get(j).name=='assetDetails') {
                        value=Wtf.encode(value);
                    } else {
                        switch(fields.get(j).type){
                            case "auto":if(value!=undefined){value=(value+"").trim();}value=encodeURI(value);value="\""+value+"\"";break;
                            case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
                        }
                    }
                    if(fields.get(j).name=='location'){
                        recarr.push(fields.get(j).name+":"+presentValue);
                    }else{
                        recarr.push(fields.get(j).name+":"+value);
                    }
                    
                }
                recarr.push("modified:"+rec.dirty);
                arr.push("{"+recarr.join(",")+"}");
            }
            arr="["+arr.join(',')+"]";  

            Wtf.Ajax.requestEx({
                url: "ACCInvoice/getBatchRemainingQuantityForMultipleRecords.do",
                params: {
                    batchdetails:arr,
                    transType:this.moduleid,
                    isEdit:this.isEdit
                }
            },this,function(response,req){
                for(var k=0;k< response.data.length;k++){
                    var responseData=response.data[k];
                    if(responseData!=null || responseData != 'undefined'){  
                        for(var j=0;j< this.recArr.length;j++){
                            var rec=this.recArr[j];
                            if(rec.data.warehouse==responseData.warehouse){  
                                rec.set(field,presentValue);
                                rec.set("avlquantity",responseData.avlquantity); 
                       
                            }
                        }
                    }
                }
            },function(res,req){
                return false;
            });
        }
    },
    addGridRec:function(record,addDefault,isReadOnly){ 
        var size=this.store.getCount();
        
        var rec= this.accRec;
        var size=this.store.getCount();
        rec = new rec({});
        rec.beginEdit();
        var fields=this.store.fields;
        for(var x=0;x<fields.length;x++){
            var value="";
            if(record !=undefined && record!=""){
                if(fields.get(x).type=="date"){
                    value=record[fields.get(x).name];
                    if(value!="")
                        value=new Date(value);
                }                    
                else{
                    value=unescape(record[fields.get(x).name]); //for saving the serial no. its showing %20 in space
                }
            }
            if((isReadOnly && isReadOnly!="false")   && (fields.get(x).name=="location" || fields.get(x).name=="warehouse" || fields.get(x).name == "row" || fields.get(x).name == "rack" || fields.get(x).name == "bin" || fields.get(x).name=="mfgdate" || fields.get(x).name=="expdate" || fields.get(x).name=="batch" || fields.get(x).name=="quantity")){
                continue;   
            }
            if(addDefault && (record=="" || record==undefined)){
                if(fields.get(x).name=="location" && (this.defaultLocation !=undefined && this.defaultLocation!="" && this.isLocationForProduct)){
                    if(this.isUnbuildAssembly){
                        rec.set(fields.get(x).name, ""); //In case of unbuild assembly, do not show default value. Available Quantity Problem
                    } else {
                    rec.set(fields.get(x).name, this.defaultLocation); 
                    }
                } else if(fields.get(x).name=="warehouse" && (this.defaultWarehouse !=undefined && this.defaultWarehouse!="" && this.isWarehouseForProduct)){
                    if(this.isUnbuildAssembly){
                        rec.set(fields.get(x).name, ""); 
                    } else {
                    rec.set(fields.get(x).name, this.defaultWarehouse);  
                    }
                } else if(fields.get(x).name=="serialno" && (this.assetId !=undefined && this.assetId!="")){
                    rec.set(fields.get(x).name, this.assetId);  //set  serialno as assetid 
                } else if(fields.get(x).name=="batch" && this.defaultBatch !=undefined && this.defaultBatch!=""){
                    if(this.isUnbuildAssembly){
                        rec.set(fields.get(x).name, ""); 
                    } else {                        
                    rec.set(fields.get(x).name, this.defaultBatch);
                    }
                } else if(fields.get(x).name=="availableQty"){
                    rec.set(fields.get(x).name, "0");
                } else {
                    rec.set(fields.get(x).name, value); 
                }
            }else{
                rec.set(fields.get(x).name, value);   
            }
            if(fields.get(x).name=='quantity'){
                if((!this.isLocationForProduct && !this.isWarehouseForProduct && !this.isBatchForProduct && !this.isRowForProduct && !this.isRackForProduct && !this.isBinForProduct && this.isSerialForProduct)){
                    rec.set(fields.get(x).name, "1");
                }else{
                    if((this.isEdit || this.linkflag) && !this.copyTrans){
                        rec.set(fields.get(x).name, value);
                    }else{
                        if (record != undefined) {
                            rec.set(fields.get(x).name, value);
                        }else {
                            rec.set(fields.get(x).name, this.currentQuantity);
                                Wtf.totalQtyAddedInBuild= Wtf.totalQtyAddedInBuild+this.currentQuantity;
                            }                                    	
				    }
                                }
                
                
            }
            if(fields.get(x).name=="productid"){
                rec.set(fields.get(x).name, this.productid); 
            }
            if(fields.get(x).name=="isreadyonly"){
                rec.set(fields.get(x).name, isReadOnly); 
            }      
        }      
        rec.endEdit();
        rec.commit();
        this.store.add(rec);
        if (Wtf.totalQtyAddedInBuild == this.totalquantity) {
            this.submitButton.show();
            this.nextButton.hide();
        } else if (Wtf.totalQtyAddedInBuild != this.totalquantity) {
            this.submitButton.hide();
            this.nextButton.show();
        }
    },
    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.grid,false,arr);
    },        
    createForm:function(){              
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            //            compId:this.id,
            autoHeight: true,
            autoWidth:true,
            parentcompId:this.id,
            moduleid:this.moduleid,
            isWindow:true,
            widthVal:90,
            isEdit: false,//this.isEdit,
            record: undefined//this.record
        });
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },

        {
            name: 'oldflag'
        }
        ]);
        
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName
            }
        });
       
    },
    checkQuantity:function(store){
        var enteredQty=0;
        var storeQty=0;
        if(this.isFixedAsset)
        {
            storeQty=store.getCount();
        }else{
            storeQty=store.getCount()-1;
        }
        
        for(var i=0;i<storeQty;i++){
            if(store.getAt(i).data.quantity !=undefined && store.getAt(i).data.quantity !=""){
                enteredQty+=parseInt(store.getAt(i).data.quantity);    
            }            
        }
        if(enteredQty!=this.quantity){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")],2);
            return false;
        }
        return true;
        
    },
    validateBatchDetails:function(){
        var store=this.grid.getStore();
        if(!this.checkQuantity(store))
        {
            return false;
        }
        var arr=[];
        var fields=store.fields;
        var len=store.getCount();
        var record=store.getAt(0); // as only 1st record 
        if(this.isSales &&  !this.isFixedAsset && !this.isLeaseFixedAsset &&  (this.isBatchForProduct || this.isLocationForProduct || this.isWarehouseForProduct  || this.isRowForProduct  || this.isRackForProduct  || this.isBinForProduct) && !this.isSerialForProduct){
            Wtf.Ajax.requestEx({
                url: "ACCInvoice/getBatchRemainingQuantity.do",
                params: {
                    batchdetails:this.getBatchDetails(),
                    transType:this.moduleid
                }
            },this,this.genSuccessResponseBatch,this.genFailureResponseBatch);
        }else{
            this.close();
        }
    },
    genSuccessResponseBatch : function(response){
        //        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],7);
        this.remainingQuantity=response.quantity;
        if(response.success){
            if(this.validateBatchSerialDetails()){
                this.isQuantitynotAvialable=false;
                if(this.isSales && this.setValuesToMultipleRec==true && this.isBatchForProduct ==false ){

                    var warehouseArray=[];//created Unique warehouse array
                    this.store.each(function(rec){
                        if(rec.data.warehouse != ""  ){
                            for(var n=0;n< warehouseArray.length ;n++){
                                if(rec.data.warehouse ==warehouseArray[n]){
                                    break;
                                }
                            }
                            if(n== warehouseArray.length){
                                warehouseArray.push(rec.data.warehouse);
                            }   
                        }
                    },this);
                    var locationArray=[];//created Unique location array
                    this.store.each(function(rec){
                        if(rec.data.location != ""  ){
                            for(var n=0;n< locationArray.length ;n++){
                                if(rec.data.location ==locationArray[n]){
                                    break;
                                }
                            }
                            if(n== locationArray.length){
                                locationArray.push(rec.data.location);
                            }

                        }
                    },this);

                    var avlQuantity=0;
                    var requiredQuntity=0;
                    for(var i=0;i<warehouseArray.length;i++){
                        for(var j=0;j<locationArray.length;j++){
                            this.store.each(function(rec){
                                if(rec.data.warehouse == warehouseArray[i] &&  rec.data.location == locationArray[j] ){
                                    avlQuantity=rec.data.avlquantity;
                                    requiredQuntity+=rec.data.quantity;
                                    if(requiredQuntity > avlQuantity){
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.remqty")],2);
                                        this.isQuantitynotAvialable=true;
                                        return false;

                                    }
                                }
                            },this);
                            avlQuantity=0;
                            requiredQuntity=0;
                            if(this.isQuantitynotAvialable){
                                break;
                            }
                        }
                        if(this.isQuantitynotAvialable){
                            break;
                        }
                    }
                } 
                if(this.isQuantitynotAvialable==false){
                    this.close();
                }               
            }
        }
       
    },
    
    genFailureResponseBatch : function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    validateBatchSerialDetails:function(){
        var store=this.grid.getStore();
        var arr=[];
        var fields=store.fields;
        var len=store.getCount()-1; 
        for(var i=0;i<len;i++){
            var rec=store.getAt(i);
            var recarr=[];
            if((this.isBatchForProduct || this.isLocationForProduct || this.isWarehouseForProduct  || this.isRowForProduct  || this.isRackForProduct  || this.isBinForProduct) && !this.isSerialForProduct){
              
                if(rec.data.purchasebatchid!="" && !this.isFixedAsset && !this.isLeaseFixedAsset && this.isSales && !this.linkflag && ! this.isEdit){  //check remaini){
                    if(this.quantity>this.remainingQuantity)
                    {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.remqty")+" "+WtfGlobal.getLocaleText("acc.batch.remqty1")+' '+(this.remainingQuantity)],2);
                        return false;
                    }
                }
            }
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                if(fields.get(j).name=='location' && i==0 && this.isLocationForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.location.empty") + " " + i + 1], 2);
                        return false;
                    }
                }else if(fields.get(j).name=='warehouse' && i==0 && this.isWarehouseForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.warehouse.empty") + " " + i + 1], 2);
                        return false;
                    }
                }
                else if(fields.get(j).name=='row' && i==0 && this.isRowForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.row.empty") + " " + (i + 1)], 2);
                        //alert("warehouse Should not empty. Please check row " +i+1)    
                        return false;
                    }
                }
                else if(fields.get(j).name=='rack' && i==0 && this.isRackForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.rack.empty") +" " +(i+1)], 2);
                        //alert("warehouse Should not empty. Please check row " +i+1)    
                        return false;
                    }
                }
                else if(fields.get(j).name=='bin' && i==0 && this.isBinForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.bin.empty") + " " + (i + 1)], 2);
                        //alert("warehouse Should not empty. Please check row " +i+1)    
                        return false;
                    }
                }else if(fields.get(j).name=='batch' && i==0){    //do not allow to save if batch is not entered
                    //                    if(Wtf.account.companyAccountPref.isBatchCompulsory ||   this.isBatchForProduct) //only check bach is enterrd or not if batch option is true
                    if(this.isBatchForProduct) //only check bach is enterrd or not if batch option is true
                    {
                        if(value=="" || value == undefined){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.batch.empty") + " " + (i + 1)], 2);
                            return false;
                        }
                    }
                
                }else if(fields.get(j).name=='mfgdate' && i==0){
                    
                } else if(fields.get(j).name=='expdate' && i==0){
                    if(value < rec.data[fields.get(j-1).name]){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.MfgDate.greater")+" " +(i+1)], 2);
                        //alert("Mfg Date should be greater then Exp Date. Please check row " +i+1)    
                        return false;
                    }
                } else if(fields.get(j).name=='serialno'){   //do not allow to save if serial no. is not entered
                    if(this.isSerialForProduct) 
                    {
                        if(value=="" || value == undefined){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.serial.empty") + " " + (i + 1)], 2);
                            return false;
                        }
                        if(this.checkduplicateserialno(value,i) && (value!="" || value != undefined)){
                            
                        }else{
                            return false;
                        }
                    }
                }else if(fields.get(j).name=='expstart'){
                    
                }else if(fields.get(j).name=='expend'){
                    if(value < rec.data[fields.get(j-1).name]){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.expiryDate.greater") + " " + (i + 1)], 2);
                        //alert("Exp.From Date should be greater then Exp.End Date. Please check row " +i+1)    
                        return false;
                    }
                }
            }
        }
        var subProuctStorelength=this.SubProductAssemblyGrid.gridStore.getCount();
        var subProductJsonLength=this.SubProductAssemblyGrid.assemblyProductJson.length;
        if(subProuctStorelength!=subProductJsonLength)
        {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.buildassembly.PleaseEnterallvalidDetailsFirst")], 2);
            return false;  
        }
        return true;
   
    },
    validateAllDetails:function(){
        var store=this.grid.getStore();
        var arr=[];
        var fields=store.fields;
        var len=store.getCount();
        if(this.isFixedAsset || this.isfromProductAssembly){
            len=store.getCount();
        }else{
            len=store.getCount()-1; 
        }
        for(var i=0;i<len;i++){
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                if(fields.get(j).name=='availableQty' && i==0 && this.isUnbuildAssembly){
                    var availqty = 0, qty=0;
                    try { availqty = Number(rec.data.availableQty);  qty= Number(rec.data.quantity);} catch (e) {availqty=rec.data.availableQty; qty=rec.data.quantity}
                    if(availqty < qty){ //ERP-39359
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.unbuild.quantitymorethanavailquantity")+ " " + (i + 1)], 2);
                        //alert("Assembly Product's unbuild quantity is greater than available quantity.. Please check row " +i+1)    
                        return false;
                    }
                }else if(fields.get(j).name=='location' && i==0 && this.isLocationForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.location.empty") + " " + (i + 1)], 2);
                        //alert("Location Should not empty. Please check row " +i+1)    
                        return false;
                    }
                }else if(fields.get(j).name=='warehouse' && i==0 && this.isWarehouseForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.warehouse.empty") + " " + (i + 1)], 2);
                        //alert("warehouse Should not empty. Please check row " +i+1)    
                        return false;
                    }
                }   
                else if(fields.get(j).name=='row' && i==0 && this.isRowForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.row.empty") + " " + (i + 1)], 2);
                        //alert("warehouse Should not empty. Please check row " +i+1)    
                        return false;
                    }
                }
                else if(fields.get(j).name=='rack' && i==0 && this.isRackForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.rack.empty") +" "+ (i + 1)], 2);
                        //alert("warehouse Should not empty. Please check row " +i+1)    
                        return false;
                    }
                }
                else if(fields.get(j).name=='bin' && i==0 && this.isBinForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.bin.empty") + " " + (i + 1)], 2);
                        //alert("warehouse Should not empty. Please check row " +i+1)    
                        return false;
                    }
                }
                else if(fields.get(j).name=='batch' && (!rec.data.isreadyonly || rec.data.isreadyonly=="false")){    //do not allow to save if batch is not entered
                    //                    if(Wtf.account.companyAccountPref.isBatchCompulsory ||   this.isBatchForProduct) //only check bach is enterrd or not if batch option is true
                    if(this.isBatchForProduct) //only check bach is enterrd or not if batch option is true
                    {
                        if(value=="" || value == undefined){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.batch.empty") + " " + (i + 1)], 2);
                            return false;
                        }
                        
                        if((this.moduleid=="28" || this.moduleid=="30")){
                            if(this.checkduplicateBatches(value,i) && (value!="" || value != undefined)){
                            
                            }else{
                                return false;
                            }
                        }
                    }
                
                }else if(fields.get(j).name=='mfgdate' && i==0){
            
                } else if(fields.get(j).name=='expdate' && i==0){
                    if(value < rec.data[fields.get(j-1).name]){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.MfgDate.greater") + " " + (i + 1)], 2);
                        //alert("Mfg Date should be greater then Exp Date. Please check row " +i+1)    
                        return false;
                    }
                } else if(fields.get(j).name=='serialno'){   //do not allow to save if serial no. is not entered
                    if(this.isSerialForProduct) 
                    {
                        if(value=="" || value == undefined){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.serial.empty") + " " + (i + 1)], 2);
                            return false;
                        }
                        if(this.checkduplicateserialno(value,i) && (value!="" || value != undefined)){
                            
                        }else{
                            return false;
                        }
                    }
                }else if(fields.get(j).name=='expstart'){
                    
                }else if(fields.get(j).name=='expend'){
                    if(value < rec.data[fields.get(j-1).name]){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.expiryDate.greater") + " " + (i + 1)], 2);
                        //alert("Exp.From Date should be greater then Exp.End Date. Please check row " +i+1)    
                        return false;
                    }
                }
            }
        }
        var subProuctStorelength=this.SubProductAssemblyGrid.gridStore.getCount();
        var subProductJsonLength=this.SubProductAssemblyGrid.assemblyProductJson.length;
        for(var i=0;i<this.SubProductAssemblyGrid.gridStore.data.length;i++){
            var row=this.SubProductAssemblyGrid.gridStore.data.items[i].data;     
            if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                if(row.isBatchForProduct || row.isSerialForProduct || row.isLocationForProduct || row.isWarehouseForProduct || row.isRowForProduct || row.isRackForProduct  || row.isBinForProduct){
                    //                    if(subProuctStorelength!=subProductJsonLength)
                    if(row.batchdetails == undefined || row.batchdetails == null || row.batchdetails == "")
                    {         
                        if(row.componentType == 2  || row.componentType ==3)
                        {
                            return true;
                        }
                        else
                        {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.buildassembly.PleaseEnterallvalidDetailsForBOM")], 2);
                            return false;
                        }
                    }
                }
            }
        }
     
        this.getPriceOnSerial();
        return true;
    },
    /**
     * Validate quantity details before save when location and warehouse autopopulated 
     */
    validateQuantityDetail: function() {
        /**
         * Need to check available qty
         */
        var filterJson = "";
        filterJson = '[';
        var subProuctStorelength = this.SubProductAssemblyGrid.gridStore.getCount();
        var subProductJsonLength = this.SubProductAssemblyGrid.assemblyProductJson.length;
        for (var i = 0; i < this.SubProductAssemblyGrid.gridStore.data.length; i++) {
            var row = this.SubProductAssemblyGrid.gridStore.data.items[i].data;
            if ((row.isLocationForProduct || row.isWarehouseForProduct) && !row.isSerialForProduct && !row.isBatchForProduct && !row.isRowForProduct && !row.isRackForProduct && !row.isBinForProduct) {
                if (row.batchdetails == undefined || row.batchdetails == null || row.batchdetails == "")
                {
                    if(row.componentType == 2  || row.componentType ==3)
                    {
                    return true;        
                            
                    }
                    else
                    {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.buildassembly.PleaseEnterallvalidDetailsForBOM")], 2);
                        return false;
                    }
                } else {
                    this.SubProductAssemblyGrid.assemblyProductJson[i] = row.batchdetails;
                    var batchdetail=eval(row.batchdetails);
                    for(var j=0;j<batchdetail.length;j++){
                          filterJson += '{"location":"' + batchdetail[j].location + '","warehouse":"' + batchdetail[j].warehouse + '","productid":"' + row.productid + '","quantity":"' + batchdetail[j].quantity + '","componentType":"'+ row.componentType +'"},';
                    }
                }
            }
        }
        if (filterJson.length > 1) {
            filterJson = filterJson.substring(0, filterJson.length - 1);
            filterJson += "]";
                    Wtf.Ajax.requestEx({
            url: "ACCInvoice/getBatchRemainingQuantityforAssembly.do",
            params: {
                batchdetails: filterJson,
                transType: 27
            }
        }, this, function(res, req) {
            var invalidrecpresent = res.invalidrecpresent;
            if (invalidrecpresent && !this.isUnbuildAssembly) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.batch.defaultremqty") + "\n" + res.msg], 2);
                return false;
            } else {
                this.closeGrid();
            }
        }, function(res, req) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.buildassembly.PleaseEnterallvalidDetailsForBOM")], 2);
            return false;
        });
        } else {
            this.closeGrid();
        }

    },
    closeGrid: function() {
        this.currentQuantity = this.totalquantity - Wtf.totalQtyAddedInBuild;
        this.close();
    },
    getPriceOnSerial:function(){
        var quantityToBuild=this.isFinishGood==undefined?this.BuildQuantity.getValue():this.buildQuantity;
        for(var i=0;i<this.SubProductAssemblyGrid.gridStore.data.length;i++){
            var batchdetails=eval(this.SubProductAssemblyGrid.gridStore.data.items[i].data.batchdetails);
            if(this.SubProductAssemblyGrid.gridStore.data.items[i].data.isSerialForProduct){
                var batchDetails  = this.SubProductAssemblyGrid.gridStore.data.items[i].data.batchdetails   //We cannot apply length, if batch details get undefined or empty string
                var batchdetailslength = (batchDetails!=undefined && batchDetails!="") ? eval(this.SubProductAssemblyGrid.gridStore.data.items[i].data.batchdetails).length : 0;
                var temoobj={};
                var serialnostring='';
                for(var j=0;j<batchdetailslength;j++){
                    serialnostring+=batchdetails[j].serialnoid+',';
                }
                serialnostring=serialnostring.substr(0,(serialnostring.length-1));
                temoobj['serailno']=serialnostring;
                temoobj['productid']=this.SubProductAssemblyGrid.gridStore.data.items[i].data.productid;
                temoobj['quantityToBuild']=quantityToBuild;
                this.subProductJson.push(temoobj);
            }
        }
        if(this.submit && this.subProductJson!=undefined){
            Wtf.Ajax.requestEx({
                url: "ACCProduct/getPriceOnSerialNo.do",
                params: {
                    serialdetails:Wtf.encode(this.subProductJson)
                }
            },this,function(res,req){
                this.updateTotalCost(res);
                this.subProductJson=[];
            });
        }
        
    },
    updateTotalCost:function(res){
        var totalprice=0;
        var unitprice=0;
        var quantity=0;
        var quantityToBuild=this.isFinishGood==undefined?this.BuildQuantity.getValue():this.buildQuantity;
        var assemblyProductCost=0;
        for(var i=0;i<res.data.length;i++){
            var row=res.data[i];
            var productid=row.productid;
            var price=row.price;
            var index=this.productAssemblyGrid.gridStore.find('productid',productid);
            if(index!=-1){
                this.productAssemblyGrid.gridStore.getAt(index).set('purchaseprice',price);
                quantity=this.productAssemblyGrid.gridStore.getAt(index).get('quantity');
                unitprice=this.productAssemblyGrid.gridStore.getAt(index).get('purchaseprice');
                totalprice+=unitprice*quantity*quantityToBuild;
                assemblyProductCost+=quantity*unitprice;
                this.assemblyProductCost.setValue(assemblyProductCost);
                this.costToBuild.setValue(totalprice);
            }
           
        }
        if (this.parentObj.ProductAssemblyGrid != undefined) {
            this.parentObj.ProductAssemblyGrid.updateCostinAssemblyGrid();
        }
    },
    checkduplicateserialno:function(testValue,excluderow){
        var store=this.grid.getStore();
        var arr=[];
        var fields=store.fields;
        var len=store.getCount();
        for(var i=0;i<len;i++){
            if(excluderow==i)continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                if(fields.get(j).name=='serialno'){
                    if(testValue==value){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.srd")],2);
                        return false;
                    } 
                }
                
               
            }
        }
        return true;
        
    },
    
    checkDuplicateSerialName:function(obj){   //Function to check duplicate serial no present in system[mayur p]
        if(obj!=null){
            if((this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct || this.isRackForProduct || this.isRowForProduct || this.isBinForProduct) && this.isSerialForProduct ){
                if((!obj.record.data.isreadyonly || obj.record.data.isreadyonly=="false") && !this.isFixedAsset && obj.field=="quantity")
                    this.deleteRowonChangeQty(obj);
            }
            var rec=obj.record;
            var rowindex=obj.row;
            var total=obj.grid.store.getCount();
            if(obj.field=="serialno"){
        
                obj.record.set("serialno", obj.record.get("serialno"));
                var FIND = obj.record.get("serialno").trim().toLowerCase(); 
                FIND =FIND.replace(/\s+/g, '');
                var index=this.srnostore.findBy( function(rec){
                    var serialnoname=rec.data['serialno'].trim().toLowerCase();
                    serialnoname=serialnoname.replace(/\s+/g, '');
                    if(serialnoname===FIND && !rec.data.deleted) // Add non-deleted record check
                        return true;
                    else
                        return false
                })
                if(!this.fromProduct)   //for checking the condition of wether call from product creation window
                {
                    if(obj.record.get("expstart")=="" ||obj.record.get("expstart")==undefined);
                    {
                        obj.record.set("expstart", Wtf.serverDate.clearTime(true));  //show todays date
                     
                    }
                    var expend=null;   

                    if(this.warrantyperiod=="" || this.warrantyperiod==undefined) {
                        obj.record.set("expend", Wtf.serverDate.clearTime(true));  //show todays date
                    }
                    else{
                        expend = new Date(obj.record.get("expstart")).add(Date.DAY,this.warrantyperiod);   //for adding warrenty purchase days in todays date 
                        obj.record.set("expend",expend.clearTime(true));
                    } 
                
                }
                if(index>=0){
                    WtfComMsgBox(110,2);
                    obj.record.set("serialno","");
                    obj.record.set("expstart", "");
                    obj.record.set("expend","");
                }
            } else if(obj.field=="location"){
                this.currentLocation=obj.record.get("location");
            }else if(obj.field=="warehouse"){
                this.currentWarehouse=obj.record.get("warehouse");
            }else if(obj.field=="batch"){
                this.currentBatch=obj.record.get("batch");
            }
        }
    },
    deleteRowonChangeQty:function(obj){
        var quantity=obj.record.data.quantity;
        var store=obj.grid.getStore();
        for(var i=store.getCount();i>obj.row+1;i--){
            var record = store.getAt(obj.row+1);
            if(record!=undefined)
                store.remove(record);
        }
        for(var i=0;i<quantity;i++){
            this.addGridRec(undefined,true,(i!=(quantity-1)));
        }
     
    },
    checkDuplicateBatchlName:function(obj){   //Function to check duplicate Batch Name present in system[mayur p]
        if(obj!=null){
            var rec=obj.record;
            /*
             * duplicate batch can be created for build assembly. and not from product creation tab
             * applied fromProduct Check for that. (Discussed with Raj Shah)
             */
        
            if(obj.field=="batch" && obj.record.get("batch") != "" && (this.fromProduct != undefined && !this.fromProduct)){
        
                obj.record.set("batch", obj.record.get("batch"));
                //var FIND = obj.record.get("batch").trim().toLowerCase(); 
                //FIND =FIND.replace(/\s+/g, '');
                /*  var index=this.batchNostore.findBy( function(rec){
                    var batchName=rec.data['batchname'].trim().toLowerCase();
                    batchName=batchName.replace(/\s+/g, '');
                    if(batchName===FIND && !rec.data.deleted) // Add non-deleted record check
                        return true;
                    else
                        return false
                })*/
                
                Wtf.Ajax.requestEx({
                    url:"ACCMaster/getNewBatches.do",
                    params:{    
                        location:(this.isLocationForProduct)?obj.record.data.location:"",
                        warehouse:(this.isWarehouseForProduct)?obj.record.data.warehouse:"",
                        row:(this.isRowForProduct)?obj.record.data.row:"",
                        rack:(this.isRackForProduct)?obj.record.data.rack:"",
                        bin:(this.isBinForProduct)?obj.record.data.bin:"", 
                        checkbatchname:obj.record.get("batch"),
                        productid:this.productid
                
                    }
                },this,function(res,req){
                    if(res.data.length!=0){
                        WtfComMsgBox(112,2);
                        obj.record.set("batch","");
                        return false;
                    }
                 
                },function(res,req){
                    obj.record.set("batch","");
                    return false;
                });
                 
            }
        }
    },
    /*
     *  Get the available quantity on the selction of location, warehouse, batch
    */
    checkAvailableQty:function(obj,batchJson){
        var islocationavailble=false;
        if(this.isLocationForProduct && obj.record.get("location")!="" && obj.record.get("location")!=undefined){
            islocationavailble=true;
        }else if(!this.isLocationForProduct){
            islocationavailble=true;
        }
        
        var iswarehouseavailble=false;
        if(this.isWarehouseForProduct && obj.record.get("warehouse") && obj.record.get("warehouse")!=undefined){
            iswarehouseavailble=true;
        }else if(!this.isWarehouseForProduct){
            iswarehouseavailble=true;
        }
        var isBatchavailble=false;
        if(this.isBatchForProduct && obj.record.get("batch") && obj.record.get("batch")!=undefined){
            isBatchavailble=true;
        }else if(!this.isBatchForProduct){
            isBatchavailble=true;
        }
        var isRackavailble=false;
        if(this.isRackForProduct && obj.record.get("rack") && obj.record.get("rack")!=undefined){
            isRackavailble=true;
        }else if(!this.isRackForProduct){
            isRackavailble=true;
        }
        
        var isRowavailble=false;
        if(this.isRowForProduct && obj.record.get("row") && obj.record.get("row")!=undefined){
            isRowavailble=true;
        }else if(!this.isRowForProduct){
            isRowavailble=true;
        }
        var isBinavailble=false;
        if(this.isBinForProduct && obj.record.get("bin") && obj.record.get("bin")!=undefined){
            isBinavailble=true;
        }else if(!this.isBinForProduct){
            isBinavailble=true;
        }
        if(this.isUnbuildAssembly && islocationavailble && iswarehouseavailble && isBatchavailble && isRackavailble && isRowavailble && isBinavailble && !this.isFixedAsset && !this.isLeaseFixedAsset){
            Wtf.Ajax.requestEx({
                url: "ACCInvoice/getBatchRemainingQuantity.do",
                params: {
                    batchdetails:this.getBatchDetails(),
                    transType:this.moduleid,
                    isEdit:this.isEdit
                }
            },this,function(res,req){
                this.AvailableQuantity=res.quantity;
                obj.record.set("availableQty",this.AvailableQuantity);
                return false
            },function(res,req){
                obj.record.set("availableQty",this.AvailableQuantity);
                return false;
            });
        }
    },
    getBatchDetails:function(){
        this.store.each(function(rec){
            if(rec.data.rowid==undefined){
                rec.data.rowid='';
                    
            }
        },this);
        var arr=[];
        this.store.each(function(rec){
           
            arr.push(this.store.indexOf(rec));
           
        }, this);
        var jarray=WtfGlobal.getJSONArray(this.grid,true,arr);
        var jsonArray=eval(jarray);
        var subProductJson=this.SubProductAssemblyGrid.assemblyProductJson;
        var subProductJsonLength=this.SubProductAssemblyGrid.assemblyProductJson.length;
        var finalJson=[];
        var count=0;
        for(var i=0;i<subProductJsonLength;i++ ){
            if(subProductJson[i]!=undefined && subProductJson[i]!=""){
                finalJson[count]=subProductJson[i];
                count++;
            }
        }
        if(finalJson!=undefined && finalJson.length>0 && jsonArray.length>0){
            var stringJson=JSON.stringify(finalJson)
            stringJson=stringJson.replace("[\"","");
            stringJson=stringJson.replace("\"]","");
            stringJson=stringJson.replace(/\\\"/g,"\"");
            jsonArray[0].subproduct=stringJson;
            
        }
        return JSON.stringify(jsonArray);
    },
    closeWin:function(){ /*this.fireEvent('update',this,this.value);*/
        this.close();
    }   
});  
