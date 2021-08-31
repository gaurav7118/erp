/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.SerialNoWindow = function(config){
    this.modeName = config.modeName;
    this.isCAComponent= config.isCAComponent;//Component Availability Component
    this.isUnbuildAssembly = config.isUnbuildAssembly;
    this.mainassemblyproduct = config.mainassemblyproduct;
    this.barcodetype = config.barcodetype;
    this.productcode = config.productcode;
    this.isBatchBarcode = (Wtf.account.companyAccountPref.generateBarcodeParm && this.barcodetype==Wtf.BarcodeGenerator_BatchID) ? true : false;
    //this.refno=config.refno;
    this.value="1";
    this.parent=[];
    this.child=[];
    this.levelid=[];
    this.levelNm=[];
    this.serialNumberCMArr=[];
    this.isCN=config.isCN;
    this.isCustBill=config.isCustBill;
    this.isForCustomerAssembly=config.isForCustomerAssembly,
    this.jobworkorderid=config.jobworkorderid,
    this.isJobworkOrder=config.isJobworkOrder,
    this.bomid=config.bomid,
    this.moduleid = config.moduleid;
    config.quantity = (config.quantity=="NaN" || config.quantity=="") ? 0:parseFloat(config.quantity).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    this.isfromProductAssembly = (config.isfromProductAssembly)?config.isfromProductAssembly:false;
    this.isSalesOrder = (config.isSalesOrder)?config.isSalesOrder:false;
    this.isForRepairStoreOnly = (config.isForRepairStoreOnly)?config.isForRepairStoreOnly:false; //ERM-691 flag passed for displaying repair stores only in the QA approval flow
    this.includeQAAndRepairStore= (config.includeQAAndRepairStore)?config.includeQAAndRepairStore:false; 
    this.totalStoreCount=7;//ERP-10478 //Total no. of stores to be loaded while rendering the window. Increase the count if more stores are added to handle load mask.
    this.loadedStoreCount=0;
    this.module=Wtf.SerialWindow_ModuleId ;
    this.cntype = config.cntype;
    this.productType=config.type;
    this.isOnlyBatchForProduct=config.isOnlyBatchForProduct;
    this.allowUserToEditQuantity=config.allowUserToEditQuantity;            //If this flag is true than we allow user to edit Qty in batch and serial window this flag is only used when  Variable Purchase/Sales UOM conversion rate check is enable in company preferences ERM-319
    this.productUomid=config.productUomid;
    this.selectedProductUomid=config.selectedProductUomid;
    this.isReverseCNDN = (this.cntype==4?true:false);
    this.parentGridRecords=config.parentGridRecords;
    if(this.isCN) {
        if(this.isReverseCNDN) {
            this.customerFlag = false;
        } else {
            this.customerFlag = true;
        }
    }else {
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
    this.isfromSubmit=false;
    this.fromPO = (config.fromPO)?config.fromPO:false;
    this.butnArr = new Array();
    this.transactionid=config.transactionid;
    this.remainingQuantity=0;
    this.isBatchForProduct=config.isBatchForProduct;  //product level batch option
    this.isSerialForProduct=config.isSerialForProduct; //product level serial option
    this.isSKUForProduct=config.isSKUForProduct; //product level sku option
    this.linkflag=config.linkflag;
    this.isEdit=config.isEdit;
    this.isItemReusable=config.isItemReusable ;
    this.copyTrans=config.copyTrans;
    this.billid=config.billid;
    this.workorderid=config.workorderid;
    this.movmentType=config.movmentType;
    this.requestWarehouse=config.requestWarehouse;
    this.requestLocation=config.requestLocation;
    this.isLeaseFixedAsset=config.isLeaseFixedAsset;
    this.responseBatch=false;
    this.recordLength=0;
    this.AvailableQuantity=0;
    this.defaultAvailbaleQty=config.defaultAvailbaleQty;
    this.customerID=config.customerID;
    this.isForCustomer=(config.isForCustomer)?config.isForCustomer:false;
    this.isConsignment=(config.isConsignment)?config.isConsignment:false,
    this.isForconsignment=(config.isForconsignment)?config.isForconsignment:false,
    this.isblokedinso=(config.isblokedinso)?config.isblokedinso:false;
    this.documentid=config.documentid;
    this.islinkedFromLeaseSo=(config.islinkedFromLeaseSo)?config.islinkedFromLeaseSo:false;
    this.isLinkedFromSO=config.isLinkedFromSO;
    this.isLinkedFromCI=config.isLinkedFromCI;
    this.isLinkedFromGR=config.isLinkedFromGR;
    this.isLinkedFromPI=config.isLinkedFromPI;
    this.isLinkedFromDO=config.isLinkedFromDO;
    this.isLinkedFromSI=config.isLinkedFromSI;
    this.readOnly=config.readOnly;
    this.storeLoadCount=0;
    this.setValuesToMultipleRec=false;
    this.linkedFrom=config.linkedFrom;
    this.docrowid=config.docrowid;
    this.linkedFrom=config.linkedFrom;
    this.isWastageApplicable = (config.isWastageApplicable) ? config.isWastageApplicable : false;
    this.isShowStockType=(config.isShowStockType)?config.isShowStockType:false,
    //    if((config.isLocationForProduct && config.isWarehouseForProduct && !this.isSerialForProduct) || (config.isLocationForProduct&& config.isWarehouseForProduct && this.isBatchForProduct && !this.isSerialForProduct)){
    //         this.setValuesToMultipleRec=true;
    //    }
    this.sm1 = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false,
        hidden:!this.setValuesToMultipleRec
    }); 
    this.serialNumberCMArr.push(this.sm1);
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        hidden:this.readOnly,
        handler: function() {
            if(this.validateBatchSerialDetails()){
                    this.close();
                }
            for(var p = 0;p < this.store.data.items.length -1;p++){
                var rowObject = new Object();
                rowObject['srnoid'] = this.store.data.items[p].data.serialnoid;
                Wtf.unqsrno.push(rowObject);
                                       
            }
            for(var q = 0;q < this.store.data.items.length -1;q++){
                var rowObject = new Object();
                rowObject['srnoid'] = this.store.data.items[q].data.serialnoid;
                Wtf.dupsrno.push(rowObject);
                                       
            }            
        }
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            //On Cancel removed items which are added in duplicate store
            if(!this.readOnly){
                for(var p = 0;p < this.store.data.items.length -1;p++){
                    var rowObject = new Object();
                    rowObject['srnoid'] = this.store.data.items[p].data.serialnoid;
                    Wtf.unqsrno.pop(rowObject);

                }
                for(var q = 0;q < this.store.data.items.length -1;q++){
                    var rowObject = new Object();
                    rowObject['srnoid'] = this.store.data.items[q].data.serialnoid;
                    Wtf.dupsrno.pop(rowObject);

                }         
            }
            this.hide();
        }
    });

    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.SerialNoWindow.superclass.constructor.call(this, config);
    this.grid.addEvents({
        'afterColModelCreated': true
    });
}

Wtf.extend(Wtf.account.SerialNoWindow, Wtf.Window, {

    onRender: function(config){
        Wtf.account.SerialNoWindow.superclass.onRender.call(this, config);
        
        this.loadMask = new Wtf.LoadMask(document.body, {msg: WtfGlobal.getLocaleText("acc.msgbox.50")});
        this.loadMask.show();
        
        this.createDisplayGrid();
        //this.expend.setValue(Wtf.serverDate);
        //        var batchrecords="";
        //        if(this.batchDetails!=undefined && this.batchDetails.length>1){
        //            batchrecords= eval('(' + this.batchDetails + ')');
        //        }
        //        this.locationStore.on("load",function(){
        //            for(var i=0;i<this.quantity;i++){
        //                if(batchrecords.length>0 && i<=batchrecords.length){
        //                    var batchObj=batchrecords[i];
        //                    this.addGridRec(batchObj,(i==0));
        //                }else{
        //                    this.addGridRec(undefined,(i==0));
        //                } 
        //            }
        //        },this);
        
        //        if(this.setValuesToMultipleRec==true){
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
        },
        {
            name: 'locationid'
        },
        {
            name: 'location'
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
            baseParams:{isActive:true},  //ERP-40021 :To get only active Locations.
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
        var title=this.noteType;//this.isCN?WtfGlobal.getLocaleText("acc.cn.payType"):WtfGlobal.getLocaleText("acc.dn.payType");
        //var msg="<b>Product</b> : "+this.productName+"<br> <b>Quantity</b> : "+this.quantity
        var msg="";
        if(this.uomName!=undefined && this.uomName!=""){
            msg="<b>"+WtfGlobal.getLocaleText("acc.product.gridProduct")+"</b> : "+this.productName+"<br> <b>"+WtfGlobal.getLocaleText("acc.product.gridQty")+"</b> : "+this.quantity+" "+this.uomName;
        }else{
            msg="<b>"+WtfGlobal.getLocaleText("acc.product.gridProduct")+"</b> : "+this.productName+"<br> <b>"+WtfGlobal.getLocaleText("acc.product.gridQty")+"</b> : "+this.quantity;
        }
         
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
            layout: 'fit',
            items:[this.grid]
        }));
        if(this.setValuesToMultipleRec!=true){
            this.wrapperNorth.hide();
        }
        
        this.grid.on('afterColModelCreated', this.getGridConfig, this);
        
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveGridStateHandler, this);
        }, this);
        
        this.getLevelMapping();
    },
    getLevelMapping:function(){
        Wtf.Ajax.requestEx({
            url:"ACCMaster/getLevels.do"
        },this,function(res){
            for(var i=0;i<res.data.length;i++){
                this.parent.push(res.data[i].parent)
                this.levelNm.push(res.data[i].newLevelName);
                this.levelid.push(i+1);
            }
            this.createSerialNoCmArr();
            this.loadedStoreCount++;
            this.checkLoadMask();
            this.grid.fireEvent('afterColModelCreated', this.grid);
        },this.genFailureResponse);
    },
    genFailureResponse: function(response) {
        this.loadedStoreCount++;
        this.checkLoadMask();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 1);
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
                    //                    header:"Rack",
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
                    //                    header:"Bin",
                    header:colNm[i],
                    width:200,
                    hidden:!this.isBinForProduct, //if without batch then hidden batch column
                    dataIndex:'bin',
                    renderer:Wtf.comboBoxRenderer(this.binCb),
                    editor:this.readOnly?"":this.binCb
                });
            }
        }
        if ((this.moduleid == Wtf.Acc_Delivery_Order_ModuleId && !this.isSalesOrder) && Wtf.account.companyAccountPref.pickpackship && !Wtf.account.companyAccountPref.interloconpick) {
            this.serialNumberCMArr.push({
                header: 'Pack Wareshouse',
                width: 200,
                hidden: Wtf.account.companyAccountPref.interloconpick, //if without batch then hidden batch column
                dataIndex: 'packwarehouse',
                renderer: Wtf.comboBoxRenderer(this.packWarehouse),
                editor: this.readOnly ? "" : this.packWarehouse
            });
        }
        if ((this.moduleid == Wtf.Acc_Delivery_Order_ModuleId && !this.isSalesOrder)  && Wtf.account.companyAccountPref.pickpackship && Wtf.account.companyAccountPref.interloconpick) {
            this.serialNumberCMArr.push({
                header: 'Pack Location',
                width: 200,
                hidden: !Wtf.account.companyAccountPref.interloconpick, //if without batch then hidden batch column
                dataIndex: 'packlocation',
                renderer: Wtf.comboBoxRenderer(this.packLocation),
                editor: this.readOnly ? "" : this.packLocation
            });
        }

       
       
        this.serialNumberCMArr.push(
        {
            header:(Wtf.jobWorkInFlowFlag!= undefined && Wtf.jobWorkInFlowFlag==true && (this.productType=="Job Work Assembly" || this.productType=="Job Work Inventory"))? "Challan No":WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
            dataIndex:'batch',
            width:100,
            hidden:!this.isBatchForProduct, //if without batch then hidden batch column
            editor:this.readOnly?"":this.barcodeBatchName
        });
        var isvalidModule = (this.moduleid == Wtf.Acc_Delivery_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId) ? true : false;
        if (this.isBatchForProduct && isvalidModule){
            this.serialNumberCMArr.push({//Add Button to get the Batch Window, if scanner is not able to scan the Batch No.
                header: WtfGlobal.getLocaleText("acc.common.add"),
                dataIndex: 'batchid',
                align: 'center',
                width: 40,
                hidden : (this.readOnly!=undefined && this.readOnly) ? true : false,
                renderer: this.addBatchNo.createDelegate(this)
            });
        }        
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
        
        /*
         *  ERP-32353- Add Attach Document and Attachments column in batch serial window for GR,DO,SI.
         */
        var showAttachmentCol = false;
        if (this.parentObj != undefined && this.parentObj != '' && this.parentObj != null) {
            if (this.parentObj.moduleid == Wtf.Acc_Cash_Purchase_ModuleId ||this.parentObj.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId ||this.parentObj.moduleid == Wtf.Acc_Goods_Receipt_ModuleId || this.parentObj.moduleid == Wtf.Acc_Invoice_ModuleId || this.parentObj.moduleid == Wtf.Acc_Delivery_Order_ModuleId){
                showAttachmentCol=true;
            }
        }

        if (showAttachmentCol) {
            this.serialNumberCMArr.push({
                header: WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"), //"Attach Documents",
                dataIndex: 'attachdoc',
                id: "attachdoc" + this.heplmodeid + this.id,
                width: 150,
                hidden: !this.isBatchForProduct, //if without batch then hidden batch mfgdate column
                align: 'center',
                renderer: function(val) {
                    return "<div style='height:16px;width:16px;'><div class='pwndbar1 uploadDoc' style='cursor:pointer' wtf:qtitle='"
                            + WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments")
                            + "' wtf:qtip='"
                            + WtfGlobal.getLocaleText("acc.invoiceList.clickToAttachDocuments")
                            + "'>&nbsp;</div></div>";
                }
            }, {
                header: WtfGlobal.getLocaleText("acc.invoiceList.attachments"), //"Attachments",
                dataIndex: 'attachment',
                hidden: !this.isBatchForProduct, //if without batch then hidden batch mfgdate column
                id: "attachment" + this.heplmodeid + this.id,
                width: 150,
                renderer: Wtf.DownloadLink.createDelegate(this)
            });
        }
        
        this.serialNumberCMArr.push({
            //            header:WtfGlobal.getLocaleText("acc.product.sku"),         
            header:"Stock From",
            dataIndex:'stocktype',
            width:100,
            hidden:(this.isShowStockType&&Wtf.account.companyAccountPref.consignmentSalesManagementFlag)?false:true,
            renderer:Wtf.comboBoxRenderer(this.stockTypeCombo),
            editor:this.readOnly?"":this.stockTypeCombo
        });
        
        this.serialNumberCMArr.push({
            header:this.isCAComponent?WtfGlobal.getLocaleText("acc.productList.gridBalQty"):WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),/* MRP: update label as Balance quantity in component availability window(batch serial window) */
            dataIndex:"avlquantity",
            //tooltip : this.isCAComponent ? WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header5.formula.tooltip") : "",  //ERP-41405 : Balance Quantity = Balance Quantity + Current WO Block Quantity.
            hidden:(!this.isSales) || (this.isSales && this.isSerialForProduct) || (this.isConsignment && this.moduleid==Wtf.Acc_ConsignmentSalesReturn_ModuleId),
            width:100
        }); 
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.product.gridQty"),
            dataIndex:"quantity",
            hidden:(!this.isLocationForProduct && !this.isWarehouseForProduct && !this.isBatchForProduct && this.isSerialForProduct && !this.isRowForProduct && !this.isRackForProduct && !this.isBinForProduct),
            width:100,
            editor:(this.readOnly || this.isForRepairStoreOnly)?"":this.serialQty=new Wtf.form.NumberField({    //ERM-691 isForRepairStoreOnly flag to disable quantity editing
                allowNegative: false,
                decimalPrecision:Wtf.QUANTITY_DIGIT_AFTER_DECIMAL
            })
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
            header:WtfGlobal.getLocaleText("acc.common.usedCount"),
            dataIndex:'reusablecount',
            width:100,
            minValue:0,
            allowNegative:false,
            hidden:(!this.isSerialForProduct) || (!(this.moduleid==51) && !(this.moduleid==53) && !(this.moduleid==29)) , //Reusable Count for how many times the Product is used
            editor:this.readOnly?"":new Wtf.form.NumberField({
                allowDecimals:false,
                allowNegative:false,
                name:'reusablecount'
            
            })
        });
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.field.ValidFrom"), /*Sets the label with Warranty Valid From*/
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

            header:WtfGlobal.getLocaleText("acc.field.ExpiresOn"),  /*Sets the label with Warranty Expires On*/

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
            hidden:!(Wtf.account.companyAccountPref.SKUFieldParm && this.isSKUForProduct),
            editor:this.readOnly?"":new Wtf.form.TextField({
                name:'skufield'
            }) 
        });
        
        if (this.isWastageApplicable && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
            this.wastageQuantityTypeStore = new Wtf.data.SimpleStore({
                fields: [{name:'typeid', type:'int'}, 'name'],
                data :[[1,'Percentage'], [0,'Flat']]
            });
            
            this.wastageQuantityType = new Wtf.form.ComboBox({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.wastageType"), // 'Wastage Quantity Type',
                store: this.wastageQuantityTypeStore,
                width: 220,
                name: 'wastageQuantityType',
                displayField: 'name',
                valueField: 'typeid',
                mode: 'local',
                triggerAction: 'all',
                selectOnFocus: true,
                allowBlank: false
            });
            
            this.wastageQuantity = new Wtf.form.NumberField({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.wastageQuantity"), // 'Wastage Quantity',
                name: 'wastageQuantity',
                allowNegative: false,
                defaultValue: 0,
                allowBlank: false,
                maxLength: 10,
                width: 220,
                decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
            });
            
            this.serialNumberCMArr.push({
                header: WtfGlobal.getLocaleText("acc.field.wastageType"), // "Wastage Quantity Type",
                dataIndex: 'wastageQuantityType',
                align: 'left',
                width: 130,
                renderer: Wtf.comboBoxRenderer(this.wastageQuantityType),
                editor: this.readOnly ? "" : this.wastageQuantityType
            });
            
            this.serialNumberCMArr.push({
                header: WtfGlobal.getLocaleText("acc.field.wastageQuantity"), // "Wastage Quantity",
                dataIndex: 'wastageQuantity',
                align: 'right',
                width: 110,
                editor: this.readOnly ? "" : this.wastageQuantity
            });
        }
        
        if(this.isSerialForProduct){
            this.serialNumberCMArr = WtfGlobal.appendCustomColumn(this.serialNumberCMArr,GlobalColumnModel[this.module],undefined,undefined,this.readOnly);
        } 
        var CustomtotalStoreCount=0;
        var CustomloadedStoreCount=0;
       
        for(var j=0; j<this.serialNumberCMArr.length;j++){
            if(this.serialNumberCMArr[j].dataIndex.indexOf('Custom_') != -1 &&(this.serialNumberCMArr[j].fieldtype ===4 || this.serialNumberCMArr[j].fieldtype ===7) ){
                    CustomtotalStoreCount++;
                    this.serialNumberCMArr[j].editor.store.on('load',function(){
                        CustomloadedStoreCount++;
                        if(CustomtotalStoreCount === CustomloadedStoreCount && this.grid != undefined){
                            if (this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId || this.moduleid == Wtf.Acc_Delivery_Order_ModuleId ||
                                    this.moduleid == Wtf.Acc_Sales_Return_ModuleId || this.moduleid == Wtf.Acc_Purchase_Return_ModuleId ||
                                    this.moduleid == Wtf.Acc_ConsignmentRequest_ModuleId || this.moduleid == Wtf.Acc_ConsignmentDeliveryOrder_ModuleId ||
                                    this.moduleid == Wtf.Acc_ConsignmentInvoice_ModuleId || this.moduleid == Wtf.Acc_ConsignmentSalesReturn_ModuleId ||
                                    this.moduleid == Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId || this.moduleid == Wtf.Acc_Consignment_GoodsReceipt_ModuleId || this.moduleid == Wtf.Acc_ConsignmentPurchaseReturn_ModuleId || this.moduleid == Wtf.Acc_ConsignmentVendorRequest_ModuleId ||
                                    this.moduleid == Wtf.Acc_Lease_DO || this.moduleid == Wtf.Acc_Lease_Return ) {
                                    this.populateCustomFieldValue(this.grid);
                                /*
                                 * Commenting this line as it is no longer required. We return false on cellclick to disable custom field.
                                 */
//                                this.disableCustomFieldOfGrid();     
                            }
                            this.grid.getView().refresh(); 
                        }
                },this)
               }
              
            }
       
        this.serialNumberCMArr.push({
            header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
            align:'center',
            width:40,
            hidden:this.readOnly,
            renderer: this.deleteRenderer.createDelegate(this)
        }
        );
        for(var childCntr=0;childCntr<this.parent.length;childCntr++){
            for(var parentCntr=0;parentCntr<this.parent.length;parentCntr++){
                if((childCntr+1)==this.parent[parentCntr]){
                    this.child.push(parentCntr+1);
                    break;
                }
            }
            if(parentCntr==this.parent.length){
                this.child.push(0);
            }
        }
        this.serialNumberCM=new Wtf.grid.ColumnModel(this.serialNumberCMArr);
        this.grid.reconfigure(this.grid.getStore(),this.serialNumberCM);
        //            this.grid.getView().forceFit=true;
        this.grid.getView().refresh(); 
        
//        if (this.isCAComponent) {  //ERP-41405 : Set Tooltip to Grid's Header based on its dataindex.          
//            this.grid.getColumnModel().setColumnTooltip(this.grid.colModel.findColumnIndex("avlquantity"),WtfGlobal.getLocaleText("acc.mrp.wo.ca.grid.header5.formula.tooltip")); 
//            this.grid.getView().updateHeaders();
//        }        
    },
    addBatchNo:function(){//ERM-304
        return "<div class='pwnd add-gridrow' wtf:qtip=\"Click to select Batch Number(s).\"></div>";
    },
    createDisplayGrid:function(){
        this.sm = new Wtf.grid.CheckboxSelectionModel({    //my ch
            //            singleSelect :true
            });
        this.srnocm= new Wtf.grid.ColumnModel([this.sm,new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.userAdmin.name"),
            width:200,
            dataIndex:'serialno'
        },{
            header:WtfGlobal.getLocaleText("acc.field.Exp.FromDate"),
            dataIndex:'expstart',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            format:WtfGlobal.getOnlyDateFormat()
        },{
            header:WtfGlobal.getLocaleText("acc.field.Exp.EndDate"),
            dataIndex:'expend',
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            //          header:WtfGlobal.getLocaleText("acc.product.sku"), 
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
            name: 'purchasebatchid'
        },{
            name: 'purchaseserialid'
        },{
            name: 'packwarehouse'
        },{
            name: 'packlocation'
        },{
            name:'skufield',
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm
        },
        {
            name:'warehouseid',
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm
        },
        {
            name:'warehousename',
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm
        },
        {
            name:'locationid',
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm
        },
        {
            name:'locationname',
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm
        }
        ]);
        var url="";
        if(this.isUnbuildAssembly){
            url="ACCMaster/getUsedSerialsForAssembly.do"    //ERP-23242
        } else {
            url="ACCMaster/getNewSerials.do"
        }
        this.srnostore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.srnoRec),
            url:url,
            baseParams:{
                mainproduct : this.mainassemblyproduct,     //ERP-23242
                isUnbuildAssembly : this.isUnbuildAssembly, //ERP-23242
                //refno:this.refno,
                productid:this.productid,
                duplicatecheck:true,
                isEdit:this.isEdit,
                linkflag:this.linkflag,
                documentid:(this.isEdit  || (this.linkflag && this.isConsignment))?this.documentid:"",
                moduleid:this.moduleid,
                copyTrans:this.copyTrans,
                billid:this.billid,
                isblokedinso:this.isblokedinso
            }
        });
        
        this.srnostore.load();  //loadded all the record to chechk the functionality of duplicate Serial name
        
        this.srnostore.on("load",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        this.srnostore.on("loadexception",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        
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
            name: 'batchid' //Batch Id for Add Batch Button
        },
        {
            name: 'batchname'
        },
        {
            name: 'stocktype',defValue:3
        },,
        {
            name: 'packwarehouse'
        },
        {
            name: 'packlocation'
        }
        ]);
        this.batchNostore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.batchNoRec),
            url:"ACCMaster/getNewBatches.do",
            baseParams:{
                productid:this.productid,
                jobworkorderid:this.jobworkorderid // to fetch those batches, which are used in Stock adjustment which are linked to job work order having id in jobworkorderid
            }
        });
        
        //        this.batchNostore.load();  //loadded all the record to chechk the functionality of duplicate batch name
       
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
            baseParams:{isActive:true},  //ERP-40021 :To get only active Locations.
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
        },
         {name: 'warehouse'}
        ]);
        this.wareHouseReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.wareHouseRec);

        var warehouseStoreUrl = (this.isConsignReturn || this.isForCustomer)?"ACCCustomerCMN/getAllCustomerWarehouse.do":"ACCMaster/getWarehouseItems.do";
        this.wareHouseStore = new Wtf.data.Store({
            //            url:"ACCMaster/getWarehouseItems.do",
            url:warehouseStoreUrl,
            reader:this.wareHouseReader,
            baseParams:{
                customerid:this.customerID,
                isForCustomer:this.isForCustomer,
                movementtypeid:(this.movmentType!=undefined && this.movmentType !="")?this.movmentType:"",
                warehouseid:this.warehouseid,
                isActive:true,  //ERP-40021 :To get only active Locations.
                isRepairStoreOnly:this.isForRepairStoreOnly  //ERM-691 for repair stores only 
            }
        });
       
        this.wareHouseStore.load();
        //        this.wareHouseStore.on("load",function(){
        //            this.grid.getView().refresh();
        //        },this);
        this.locationStore.load();
        var firstTime=true;
        this.wareHouseStore.on("load",function(){ 
            this.loadedStoreCount++;
            this.checkLoadMask();
            this.grid.getView().refresh();
            this.storeLoadCount=this.storeLoadCount+1;
            if(this.storeLoadCount==2){//For check that Warehouse and location Store Load
                //this.storeLoadCount=0;
                this.LoadConfigurationAfterStoreLoad();
                firstTime=false;
            }
        },this);
        this.wareHouseStore.on("loadexception",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        this.locationStore.on("load",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
            this.storeLoadCount=this.storeLoadCount+1;
            if(this.storeLoadCount==2){
                //this.storeLoadCount=0;
                this.LoadConfigurationAfterStoreLoad();
                firstTime=false;
            }
        },this);
        this.locationStore.on("loadexception",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        
        this.locationEditor = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.locationStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'location',
            hiddenName:'location'

        });
        this.barcodeBatchName = new Wtf.form.TextField({
            name: 'batch',
            editable :  false
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
        this.rowStore.on("load",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        this.rowStore.on("loadexception",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        
        this.rackStore = new Wtf.data.Store({
            url:"ACCMaster/getStoreMasters.do",
            reader:this.levelReader,
            baseParams:{
                transType:'rack'
            }
        });
        this.rackStore.load();
        this.rackStore.on("load",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        this.rackStore.on("loadexception",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        
        this.binStore = new Wtf.data.Store({
            url:"ACCMaster/getStoreMasters.do",
            reader:this.levelReader,
            baseParams:{
                transType:'bin'
            }
        });
        this.binStore.load();
        this.binStore.on("load",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        this.binStore.on("loadexception",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        
        this.rowCb = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            lastQuery:'',
            displayField:'name',
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
            store:this.rackStore,
            anchor:'90%',
            lastQuery:'',
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
            valueField:this.isForCustomer?'warehouse':'id',
            displayField:'name',
            store:this.wareHouseStore,
            anchor:'90%',
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            name:'warehouse',
            hiddenName:'warehouse'

        });
        
        this.packWarehouseRec = new Wtf.data.Record.create([
        {
            name:"packwarehouse"
        },

        {
            name:"abbr"
        },

        {
            name:"fullname"
        },
        ]);
        
        this.packWarehouseReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.packWarehouseRec);
        
        this.packWarehouseStore = new Wtf.data.Store({
            url :"ACCMaster/getPackingStore.do",
            reader:this.packWarehouseReader
        });
       
        this.packWarehouseStore.load({
            params:{
                isActive : "true",
                isLocation:false
            }
        });

        this.packWarehouse = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:'Pack Warehouse',
            valueField:'packwarehouse',
            displayField:'fullname',
            store:this.packWarehouseStore,
            anchor:'90%',
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            name:'packwarehouse',
            value:Wtf.account.companyAccountPref.packingstore,
            hiddenName:'packwarehouse',
            hidden:Wtf.account.companyAccountPref.interloconpick
        //            disabled: true
        });
        
        this.packLocationRec = new Wtf.data.Record.create([
        {
            name:"packlocation"
        },

        {
            name:"abbr"
        },

        {
            name:"fullname"
        },
        ]);
        
        this.packLocationReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.packLocationRec);
        
        this.packLocationStore = new Wtf.data.Store({
            url :"ACCMaster/getPackingStore.do",
            reader:this.packLocationReader
        });
       
        this.packLocationStore.load({
            params:{
                isActive : "true",
                isLocation:true
            }
        });

        this.packLocation = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:'Pack Location',
            valueField:'packlocation',
            displayField:'fullname',
            lastQuery:'',
            store:this.packLocationStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'packlocation',
            value:Wtf.account.companyAccountPref.packinglocation,
            hiddenName:'packlocation',
            hidden:!Wtf.account.companyAccountPref.interloconpick
        //            disabled: true
        });
this.packLocationStore.on("load",function(){
    this.packLocation.setValue(Wtf.account.companyAccountPref.packinglocation);
},this);
this.packWarehouseStore.on("load",function(){
    this.packWarehouse.setValue(Wtf.account.companyAccountPref.packingstore);
},this);

 this.packLocationStore.on("load",function(){
            var i = 0;
            if (Wtf.account.companyAccountPref.pickpackship && this.packLocationStore!=null && this.packLocationStore!=undefined && this.packLocationStore.getAt(i)!=null && this.packLocationStore.getAt(i)!=undefined) {
                    var record = this.packLocationStore.getAt(i);
                    if(record!=undefined && record!=null && record.json!=null && record.json!="" && record.json!=undefined){
                    this.parentObj.pickpacklocation = record.json.packinglocationid?record.json.packinglocationid:"";    
                    }                    
                }            
            },this);

        this.stockTypeComboStore = new Wtf.data.SimpleStore({
                fields: [{name:'typeid', type:'int'}, 'name'],
            data :[[1,'System Stock'], [0,'Vendor Stock']]
        });
        
        this.stockTypeCombo = new Wtf.form.ComboBox({
            fieldLabel: "Stock From", // 'Wastage Quantity Type',
            store: this.stockTypeComboStore,
            width: 220,
            name: 'stktype',
            displayField: 'name',
            valueField: 'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus: true,
            allowBlank: false
        });
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            this.wareHouseEditor.on('select',function(){
                this.locationStore.on("beforeload",function(){
                    this.locationEditor.store.removeAll(); 
                },this)
                this.locationEditor.store.load({
                    params:{
                        storeid:this.wareHouseEditor.getValue()
                    }
                });
            },this);
        }       
        this.serialNumberCM=new Wtf.grid.ColumnModel(this.serialNumberCMArr);
        /*    

        var columnArr =[];
     //   columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.module],undefined,undefined,this.readOnly);            
     //   this.serialNumberCM= new Wtf.grid.ColumnModel([{
          columnArr.push({      
            header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
            width:200,
            hidden:!this.isWarehouseForProduct, //if without batch then hidden batch column
            dataIndex:'warehouse',
            renderer:Wtf.comboBoxRenderer(this.wareHouseEditor),
            editor:this.readOnly?"":this.wareHouseEditor
        },{
            header:"Location",
            width:200,
            hidden:!this.isLocationForProduct, //if without batch then hidden batch column
            dataIndex:'location',
            renderer:Wtf.comboBoxRenderer(this.locationEditor),
            editor:this.readOnly?"":this.locationEditor
        },{
            header:"Row",
            width:200,
            hidden:!this.isRowForProduct, //if without batch then hidden batch column
            dataIndex:'row',
            renderer:Wtf.comboBoxRenderer(this.rowCb),
            editor:this.readOnly?"":this.rowCb
        },{
            header:"Rack",
            width:200,
            hidden:!this.isRackForProduct, //if without batch then hidden batch column
            dataIndex:'rack',
            renderer:Wtf.comboBoxRenderer(this.rackCb),
            editor:this.readOnly?"":this.rackCb
        },{
            header:"Bin",
            width:200,
            hidden:!this.isBinForProduct, //if without batch then hidden batch column
            dataIndex:'bin',
            renderer:Wtf.comboBoxRenderer(this.binCb),
            editor:this.readOnly?"":this.binCb
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
            dataIndex:'batch',
            width:100,
            hidden:!this.isBatchForProduct, //if without batch then hidden batch column
            editor:this.readOnly?"":new Wtf.form.TextField({
                name:'batch'
            
            })
        },{
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
        },{
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
        },{
            header:WtfGlobal.getLocaleText("acc.product.gridQty"),
            dataIndex:"quantity",
            hidden:(!this.isLocationForProduct && !this.isWarehouseForProduct && !this.isBatchForProduct && this.isSerialForProduct && !this.isRowForProduct && !this.isRackForProduct && !this.isBinForProduct),
            width:100,
            editor:this.readOnly?"":this.serialQty=new Wtf.form.NumberField({
        })
        },{
            header:WtfGlobal.getLocaleText("acc.saleByItem.gridBalance"),
            dataIndex:"balance",
            hidden:true,
            width:100
        },{
            header:WtfGlobal.getLocaleText("acc.field.SerialNo"),
            dataIndex:'serialno',
            width:100,
           hidden:!this.isSerialForProduct, //if without batch then hidden batch mfg enddate column
            editor:this.readOnly?"":new Wtf.form.TextField({
                name:'memo'
            
            })
        },{
            header:"<span wtf:qtip='Warranty Expiry From Date'>Warranty Exp. From Date</span>",
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
        },{

            header:"<span wtf:qtip='Warranty Expiry End Date'>Warranty Exp. End Date</span>",
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
            },{
            header:WtfGlobal.getLocaleText("acc.product.sku"),
            dataIndex:'skufield',
            width:100,
//            hidden:!this.isSerialForProduct, //if without batch then hidden batch mfg sku column
              hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
            editor:this.readOnly?"":new Wtf.form.TextField({
                name:'skufield'
            }) 
            }
        );    
       columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.module],undefined,undefined,this.readOnly);  
       columnArr.push({
                       header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
               align:'center',
               width:40,
                hidden:this.readOnly,
                renderer: this.deleteRenderer.createDelegate(this)
       });

       this.serialNumberCM=new Wtf.grid.ColumnModel(columnArr);
        */
        this.accRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'location'
        },{
            name: 'row'
        },
        {
            name: 'rack'
        },
        {
            name: 'bin'
        },
        {
            name: 'warehouse'
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
            name: 'reusablecount',defValue:0
        },{
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
        },
        {
            name: 'documentid'
        },
        {
            name:'customfield'
        },
        {
            name:'avlquantity'
        },
        {
            name:'stocktype',defValue:3
        },
        {
            name:'packwarehouse'
        },
        {
            name:'packlocation'
        },
        {
            name: 'skufield',
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm
        },
        {name: 'wastageQuantityType', defValue: 0},
        {name: 'wastageQuantity', defValue: 0},
        {
            name:'attachment'
        },
        {
            name:'attachmentids'
        },
        {
            name:'documentbatchid'
        }
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        var colModelArray = [];
        colModelArray = GlobalColumnModel[this.module];
        if(colModelArray) {
            colModelArray.concat(GlobalColumnModelForProduct[this.module]);
        }
        WtfGlobal.updateStoreConfig(colModelArray, this.store);
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
        this.grid.addEvents({
        'updateAttachmentDetail':true
        });
        this.grid.flag = 0;
        this.grid.isbatch = true;
        this.grid.readOnly = this.readOnly;//sending this flag for view case
        this.sm1.on("selectionchange", this.enableDisableButtons.createDelegate(this), this);
        this.sm1.on("beforerowselect", this.checkSelections, this);
        this.grid.on('beforeedit',this.checkrecord,this);
        this.grid.on('afteredit',this.checkDuplicateSerialName ,this);
        this.grid.on('afteredit',this.checkDuplicateSKUName ,this);
        this.grid.on('afteredit',this.checkDuplicateBatchlName ,this); //loadded all the record to chechk the functionality of duplicate batch name
        this.grid.on('afteredit',this.checkAvailableQty,this); //loadded all the record to chechk the functionality of duplicate batch name
        this.grid.on('rowclick',this.handleRowClick,this);
        this.grid.on('rowclick',Wtf.callGobalDocFunction,this);
        this.grid.on('updateAttachmentDetail',this.setAttachmentDetail,this);
    },
    /**
     * Function to set document ids to batch
     */
    setTransactionId :function(id,batchrindex){
        
        var finalIds=this.store.getAt(batchrindex).get("attachmentids");
        if(finalIds!=undefined && finalIds!='' && finalIds!=null){
            finalIds=finalIds+","+id;
        }else{
           finalIds=id; 
        }
        var idsArray=finalIds.split(',');
        this.store.getAt(batchrindex).set("attachmentids",finalIds);
        this.store.getAt(batchrindex).set("attachment",idsArray.length);
    },
    setAttachmentDetail: function(rowIndex, docid) {
        var count = 0;
        this.attachmentids=this.store.getAt(rowIndex).get("attachmentids");
        if (this.attachmentids !== undefined && this.attachmentids !== '') {
            var attacharr1 = this.attachmentids.split(',');
            this.attachmentids = '';
            for (var cnt = 0; cnt < attacharr1.length; cnt++) {
                if (attacharr1[cnt] === docid) {
                    continue;
                } else {
                    if (cnt === 0 || this.attachmentids === '') {
                        this.attachmentids = attacharr1[cnt];
                    } else {
                        this.attachmentids = this.attachmentids + "," + attacharr1[cnt];
                    }
                    count++;
                }
            }

        } 
       this.store.getAt(rowIndex).set("attachmentids",this.attachmentids);
       this.store.getAt(rowIndex).set("attachment",count);

    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
                if((record.data.isreadyonly || record.data.isreadyonly=="true") && record.data.isreadyonly!="false"){
                    store.remove(store.getAt(rowindex));//this is done for edit case when we updating less quantity than original 
                    for(var cnt=rowindex;cnt>=0;cnt--){// logic- remove that row and update batch quantity for that batch
                        var rowrecord = store.getAt(cnt);
                        if((!rowrecord.data.isreadyonly || rowrecord.data.isreadyonly=="false") && rowrecord.data.isreadyonly!=undefined){
                            rowrecord.set("quantity",(rowrecord.data.quantity*1)-1);
                            break;
                        }
                    }
                    return;
                }
                store.remove(store.getAt(rowindex));
                for(var cnt=rowindex;cnt<total;cnt++){
                    var delrecord = store.getAt(rowindex);
                    if(delrecord!=undefined && (delrecord.data.isreadyonly || delrecord.data.isreadyonly=="true")){
                        store.remove(store.getAt(rowindex));
                    }else{
                        break;
                    }
               
                }
                if(rowindex==total-1){
                    this.addGridRec(undefined,(rowindex==0),false);
                }
            }, this);
            } else if (e.getTarget(".add-gridrow")) {
                    var store = grid.getStore();
                    var batchRec = store.getAt(rowindex);
                    this.obj = [];
                    this.obj["record"] = batchRec;
                    this.obj["isAddBatch"] = true;  //When user clicks on Add Batch Button
                    this.obj["grid"] = grid;
                    this.obj["row"] = rowindex;
                    this.openBatchWindow(this.obj);
        }
    },
    
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },    
    checkrecord:function(obj){
        /*
         * We can select value of custom field only in case of product opening.
         */
        if(this.moduleid != Wtf.Acc_Product_Master_ModuleId && obj.field.search('Custom_')>=0){
            return false;
        }
        if((this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct ) && this.isSerialForProduct ){
            if(obj.record.data.isreadyonly && obj.record.data.isreadyonly!="false"){
                if(obj.field=='warehouse' || obj.field=='location' || obj.field=='batch' || obj.field=='mfgdate' || obj.field=='expdate' || obj.field=='quantity' || obj.field=='row' || obj.field=='rack' || obj.field=='bin'|| obj.field=='stocktype'){
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
        if((obj.record.data.purchasebatchid != "" && obj.record.data.purchasebatchid != undefined&&this.isConsignReturn&&this.linkflag)&&!this.isBatchForProduct) {
            this.purchasebatchid=obj.record.data.purchasebatchid;
        }
        
        var isvalidModule = (this.moduleid == Wtf.Acc_Delivery_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId) ? true : false;
        if(obj!=null && obj.field=="batch" && (!obj.record.data.isreadyonly || obj.record.data.isreadyonly=="false") && !this.isSales && this.isBatchBarcode && isvalidModule) {
                this.openBatchWindow(obj);
                return;
        } else 
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
        
        if(obj.field=="reusablecount"){
            if(this.isItemReusable!="" || this.isItemReusable!=null){
                if(this.isItemReusable==1 || !(this.moduleid==53))
                    return false; 
            }
        }
        
        if(obj.field=="warehouse"  && (this.moduleid == Wtf.Acc_ConsignmentRequest_ModuleId || this.moduleid == Wtf.Acc_ConsignmentDeliveryOrder_ModuleId) && this.requestWarehouse!="" && this.requestWarehouse!=undefined && this.requestLocation!="" && this.requestLocation!=undefined){
            this.wareHouseEditor.store.clearFilter();
            this.wareHouseEditor.store.filterBy(function(rec) {
                if ( this.requestWarehouse == rec.data.id )
                    return true
                else 
                    return false
            }, this);        
        }
        else if( obj.field=="location" && ( this.moduleid == Wtf.Acc_ConsignmentRequest_ModuleId || this.moduleid == Wtf.Acc_ConsignmentDeliveryOrder_ModuleId) && this.requestWarehouse!="" && this.requestWarehouse!=undefined && this.requestLocation!="" && this.requestLocation!=undefined){
            this.locationEditor.store.clearFilter();
            this.locationEditor.store.filterBy(function(rec) {
                if ( this.requestLocation == rec.data.id )
                    return true
                else 
                    return false
            }, this); 
        }else if(obj.field=="location" && ((obj.record.get("warehouse")!="" && obj.record.get("warehouse")!=undefined)  && this.isWarehouseForProduct )){
            this.locationEditor.store.load({
                params:{
                    storeid:obj.record.get("warehouse")
                }
            });
        }
        if(obj.field=="serialno" && (!obj.record.data.isreadyonly || obj.record.data.isreadyonly=="false")){
            if((obj.record.get("location")=="" || obj.record.get("location")==undefined) && this.isLocationForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.serial.select.location")], 2);
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
        
        if(obj.field=="skufield" && (!obj.record.data.isreadyonly || obj.record.data.isreadyonly=="false")){
            if((obj.record.get("location")=="" || obj.record.get("location")==undefined) && this.isLocationForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.warning.serial.select.location")], 2);
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
        //if((this.isFixedAsset && this.isSales) ||  this.fromPO  || this.isblokedinso || this.islinkedFromLeaseSo)  //in fixed asset deliveryorder serial no shoulnot be editable  
        if((this.isFixedAsset && this.isSales) ||  (this.moduleid !== Wtf.Acc_Lease_Return && this.fromPO) ||  this.islinkedFromLeaseSo)  //in fixed asset deliveryorder serial no shoulnot be editable  
        {
            obj.cancel=true;
        }
        //        else if(obj.column<7 && obj.row!=0){
        //            obj.cancel=true;
        //        }
        else if(this.isSales){
            if(obj!=null){      
                
                if(obj.field=="batch"){
                    if(this.isBatchBarcode){
                        obj.cancel=false;
                    } else {
                        obj.cancel=true;
                    }                    
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
//                    //for DO linked with SO case and SO having locked serials passing the sodetailid to get lockedserials
                    if(this.moduleid==Wtf.Acc_Delivery_Order_ModuleId && this.isLinkedFromSO && !this.isEdit){ 
                        this.documentid=obj.record.data.documentid;
                    }
                    var scopeObj =this;
                    var linkRec = scopeObj.lineRec;
                    if(linkRec && linkRec.json && linkRec.json.isLinkedDoInSI){
                        var linkedDoIdsObj = linkRec.json.linkedDoIds;
                        this.documentid = scopeObj.documentid;                        
                        if(linkedDoIdsObj && typeof linkedDoIdsObj == "object"){
                            this.documentid = linkedDoIdsObj[scopeObj.documentid];
                        }                       
                    }
                    this.openSerialWindow(obj,expstartdate,expendate);
                    return; 
                   
                }
            }
        }  
        var parentLevelIdIndex="0";
        var dataIndex="";
        
        if(obj.field=="location"){
            parentLevelIdIndex="1"; 
            dataIndex="location";
        }if(obj.field=="warehouse"){
            parentLevelIdIndex="0"; 
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
        switch(this.child[parentLevelIdIndex]){
            case '1':
                obj.record.set('warehouse','');
                break;
            case '2':
                obj.record.set('location','');
                break;
            case '3':
                obj.record.set('row','');
                break;
            case '4':
                obj.record.set('rack','');
                break;
            case '5':
                obj.record.set('bin','');
                break;
        }
        if(Wtf.account.companyAccountPref.activateInventoryTab && obj.field=="location") return;
            
        if(obj.field=="warehouse" || obj.field=="location" || obj.field=="row" || obj.field=="rack" || obj.field=="bin"){
            var parentLevelId=this.parent[parentLevelIdIndex],parentstring=""; 
            var parentval="";
            switch(parentLevelId){
                case '2':
                    parentval=obj.record.data.location;
                    parentstring = "Location";
                    break;
                case '1':
                    parentval=obj.record.data.warehouse;
                    parentstring = "Warehouse";
                    break;
                case '3':
                    parentval=obj.record.data.row;
                    parentstring = "Row";
                    break;
                case '4':
                    parentval=obj.record.data.rack;
                    parentstring = "Rack";
                    break;
                case '5':
                    parentval=obj.record.data.bin;
                    parentstring = "Bin";
                    break;
            }
            if(parentval!=''){
                for (var k = 0; k < obj.grid.colModel.config.length; k++) {
                    if(obj.grid.colModel.config[k].editor && obj.grid.colModel.config[k].editor.field.store && obj.grid.colModel.config[k].dataIndex==dataIndex){ 
                        var store = obj.grid.colModel.config[k].editor.field.store;
                        store.clearFilter();
                        store.filterBy(function(rec) {
                            if (parentval == rec.data.parentid)
                                return true
                            else 
                                return false
                        }, this);
                    }
                }
            }else if(parentLevelIdIndex !="0" && this.parent[parentLevelIdIndex]!=0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"Please select the Parent:  \""+parentstring+"\"  first."], 2);
                return false
            }
            
        }
           
    },
    
    openSerialWindow:function(obj,expstartdate,expendate){ 
        //      if(true){
        this.serialSelectWindow = new Wtf.account.SerialSelectWindow({
            id: 'serialSelectWindow',
            title:WtfGlobal.getLocaleText("acc.field.SelectSerialNo"),
            border: false,
            obj:obj,
            modal : true,
            moduleid:this.moduleid,
            isConsignment:this.isConsignment,
            isEdit:this.isEdit,
            //refno:this.refno,
            mainproduct : this.mainassemblyproduct,     //ERP-23242
            isUnbuildAssembly : this.isUnbuildAssembly, //ERP-23242
            subproduct : this.productid,                //ERP-23242
            linkflag:this.linkflag,
            documentid:this.documentid,
            isLinkedFromPI:this.isLinkedFromPI,
            customerID:this.customerID,
            transactionid:this.transactionid,
            productid:this.productid,
            isDO:this.isDO,
            copyTrans:this.copyTrans,
            isForconsignment:this.isForconsignment,
            billid:this.billid,
            quantity:this.quantity,
            expstartdate:expstartdate,
            expendate:expendate,
            isBatchForProduct:this.isBatchForProduct,
            loadedStoreCount:this.loadedStoreCount,
            store:this.store,
            grid:this.grid,
            layout:'border',
            docrowid:this.docrowid,
            linkedFrom:this.linkedFrom,
            doc_id:(this.isEdit && this.moduleid==Wtf.Acc_Sales_Return_ModuleId)?obj.record.data.documentid : ""  // edit case (ERP-36896)
            
        });
        
        this.serialSelectWindow.on("beforeclose",function(){
            var rowindex=obj.row;
            var total=obj.grid.store.getCount();
            if(rowindex==total-1 && !this.isFixedAsset){
                this.addNewRowCheck(obj)
            }
        },this);
        this.serialSelectWindow.show(); 
    //      }else{
    //          this.sm = new Wtf.grid.CheckboxSelectionModel({
    //            //            singleSelect :true
    //            });
    //        this.batchcm= new Wtf.grid.ColumnModel([this.sm,new Wtf.grid.RowNumberer(),{
    //            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),
    //            width:200,
    //            dataIndex:'serialno'
    //        },{
    //            header:'Exp.From Date',          //remove the ex date from  batch window
    //            dataIndex:'expstart',
    //            width:100,
    //            renderer:WtfGlobal.onlyDateDeletedRenderer
    //        },{
    //            header:'Exp.End Date',
    //            dataIndex:'expend',
    //            sortable:true,
    //            renderer:WtfGlobal.onlyDateDeletedRenderer
    //        },{
    ////            header: header:WtfGlobal.getLocaleText("acc.product.sku"),      
    //            header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
    //            hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
    //            dataIndex:'skufield'
    //        }]);
    //         
    //       
    //        this.batchRec = new Wtf.data.Record.create([
    //        {
    //            name: 'id'
    //        },{
    //            name: 'serialno'
    //        },
    //        {
    //            name: 'serialnoid'
    //        },
    //        {
    //            name: 'expstart',
    //            type:'date'
    //        },
    //        {
    //            name: 'expend',
    //            type:'date'
    //        },{
    //            name: 'purchaseserialid'
    //        },{
    //            name: 'purchasebatchid'
    //        },{
    //            name:'skufield'
    //        },{
    //            name:'reusablecount'
    //        }
    //        ]);
    //        var serialbatch=obj.record.data.purchasebatchid;
    //        var seriallocation=obj.record.data.location;
    //        var serialwarehouse=obj.record.data.warehouse;
    //        var serialrow=obj.record.data.row;
    //        var serialrack=obj.record.data.rack;
    //        var serialbin=obj.record.data.bin;
    //        for(var cnt=obj.row;cnt>=0;cnt--){
    //            var rowData=this.store.getAt(cnt);
    //            if((!rowData.data.isreadyonly || rowData.data.isreadyonly=="false") && rowData.data.isreadyonly!=undefined){
    //                serialbatch=rowData.data.purchasebatchid;
    //                seriallocation=rowData.data.location;
    //                serialwarehouse=rowData.data.warehouse;
    //                serialrow=rowData.data.row;
    //                serialrack=rowData.data.rack;
    //                serialbin=rowData.data.bin;
    //                break;
    //            }
    //        }
    //        
    //        if(!this.isBatchForProduct){
    //            serialbatch="";
    //        }
    ////        var serialStoreUrl = (this.isConsignReturn)?"ACCInvoiceCMN/getNewSerialsForConsignmentReturn.do":"ACCMaster/getNewSerials.do";
    //        var serialStoreUrl = "ACCMaster/getNewSerials.do";
    ////        var serialStoreUrl = "ACCMaster/getNewSerials.do";
    //        var documentid="";
    //        if(this.moduleid==Wtf.Acc_ConsignmentDeliveryOrder_ModuleId){
    //            documentid=(this.isEdit || (this.linkflag))?this.documentid:"";
    //        }else{
    //            documentid=(this.isEdit || (this.linkflag && this.isConsignment))?this.documentid:"";
    //        }
    //        this.batchstore = new Wtf.data.Store({
    //            reader: new Wtf.data.KwlJsonReader({
    //                root: "data"
    //            },this.batchRec),
    ////            url:"ACCMaster/getNewSerials.do",
    //            url:serialStoreUrl,
    //            baseParams:{
    //                //batch:obj.record.data.purchasebatchid
    //                batch:serialbatch,
    //                transType:this.moduleid,
    //                location:seriallocation,
    //                warehouse:serialwarehouse,
    //                row:serialrow,
    //                rack:serialrack,
    //                bin:serialbin,
    //                linkflag:this.linkflag,
    //                documentid:documentid,
    //                transactionid:this.transactionid,
    //                productid:this.productid,
    //                isDO:this.isDO,
    //                isEdit:this.isEdit,
    //                copyTrans:this.copyTrans,
    //                isConsignment:this.isConsignment,
    //                isForconsignment:this.isForconsignment,
    //                billid:this.billid
    //            }
    //        });
    //        
    //        this.batchstore.load();
    //        
    //        this.batchstore.on("load",function(){
    //            this.loadedStoreCount++;
    //            if(this.isfromProductAssembly){
    //                for(var r=0;r < this.batchstore.data.items.length; r++){
    //                    var index=this.batchstore.findBy(function(rec){
    //                        for(var q = 0; q<Wtf.unqsrno.length;q++){ 
    //                            if(rec.data.serialnoid == Wtf.unqsrno[q].srnoid)
    //                                return true;
    //                        }
    //                    })
    //                    this.batchstore.remove(this.batchstore.getAt(index));
    //                }
    //            }
    //                 
    //            for(var r=0;r < this.batchstore.data.items.length; r++){
    //                    var index=this.batchstore.findBy(function(rec){
    //                        for(var q = 0; q<Wtf.dupsrno.length;q++){ 
    //                            if(rec.data.serialnoid == Wtf.dupsrno[q].srnoid)
    //                                return true;
    //                        }
    //                    })
    //                    this.batchstore.remove(this.batchstore.getAt(index));
    //                }
    //           
    //            this.checkLoadMask();
    //        },this);
    //        this.batchstore.on("loadexception",function(){
    //            this.loadedStoreCount++;
    //            this.checkLoadMask();
    //        },this);
    //       
    //        this.batchgrid = new Wtf.grid.GridPanel({
    //            height:210,
    //            width:'97%',
    //            store: this.batchstore,
    //            cm: this.batchcm,
    //            sm:this.sm,
    //            border : false,
    //            loadMask : true,
    //            viewConfig: {
    //                forceFit:true,
    //                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
    //            }
    //        });
    //        var batchWin = new Wtf.Window({
    //            height : 300,
    //            width : 400,
    //            iconCls :getButtonIconCls(Wtf.etype.deskera),
    //            title : WtfGlobal.getLocaleText("acc.field.SelectSerialNo"),
    //            bodyStyle : 'padding:5px;background-color:#ffffff;',
    //            layout : 'border',
    //            resizable : false,
    //            items : [{
    //                region : 'center',
    //                border : false,
    //                autoScroll : true,
    //                bodyStyle : 'padding:5px;background-color:#f1f1f1;',
    //                items:[this.batchgrid]
    //            }],
    //            buttons : [{
    //                text : WtfGlobal.getLocaleText("acc.msgbox.ok"),
    //                scope:this,
    //                handler : function(){
    //                    if(this.sm.getCount()==0){
    //                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.sr")],2);
    //                        return false;
    //                    }else{
    //                        this.rec = this.sm.getSelections();
    //                        for(var i=0;i<this.rec.length;i++){
    ////                              var rowObject = new Object();
    //                            if(this.rec.length>this.quantity)
    //                            {
    //                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.please")+" "+this.quantity+" "+WtfGlobal.getLocaleText("acc.field.srnoq")],2);
    //                                return false;
    //                            }
    //                            this.store.data.items[obj.row+i].data.serialno=this.rec[i].data.serialno
    //                            this.store.data.items[obj.row+i].data.serialnoid=this.rec[i].data.serialnoid
    //                            this.store.data.items[obj.row+i].data.expstart=this.rec[i].data.expstart
    //                            this.store.data.items[obj.row+i].data.expend=this.rec[i].data.expend
    //                            this.store.data.items[obj.row+i].data.skufield=this.rec[i].data.skufield
    //                            this.store.data.items[obj.row+i].data.purchaseserialid=this.rec[i].data.purchaseserialid
    //                            this.store.data.items[obj.row+i].data.reusablecount=this.rec[i].data.reusablecount
    //                            if(!this.isBatchForProduct){ 
    //                                this.store.data.items[obj.row+i].data.purchasebatchid=this.rec[i].data.purchasebatchid  //kept the purchase batch id in case of batch option is not selected 
    //                            }
    ////                            var rowObject = new Object();
    ////                            rowObject['srnoid'] =this.rec[i].data.serialnoid
    ////                            Wtf.dupsrno.push(rowObject);
    //                        }
    //                        this.grid.getView().refresh();
    //                      
    //                    }
    //                    batchWin.close();
    //                }
    //            }],
    //            autoScroll : true,
    //            modal : true
    //        });
    //
    //        batchWin.show();
    //      }  
        
        
                
    },
    openBatchWindow:function(obj){ 
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect :true,                          //this.isRequisition ? false : true,
            hidden:this.isSalesCommissionStmt
        });
        this.batchcm= new Wtf.grid.ColumnModel([this.sm,new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
            width:200,
            hidden:(this.moduleid==Wtf.Acc_Sales_Return_ModuleId && ((this.isLocationForProduct || this.isWarehouseForProduct)&& this.isBatchForProduct && !this.isSerialForProduct)) ?false:true,
            dataIndex:'warehousename'
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.locationmaster"),
            width:200,
            hidden:(this.moduleid==Wtf.Acc_Sales_Return_ModuleId && ((this.isLocationForProduct || this.isWarehouseForProduct)&& this.isBatchForProduct && !this.isSerialForProduct)) ?false:true,
            dataIndex:'locationname'
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),
            width:200,
            dataIndex: 'batchname',
            sortable: true //ERP-41012
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
            name: 'productid'
        },{
            name:'warehousename'
        },{
            name:'locationname'
        },{
            name:'attachment'
        },{
            name:'attachmentids'
        },{
            name:'barcodebatch'
        }
        ]);
        
        //         var StoreUrl = (this.isConsignReturn)?"ACCInvoiceCMN/getNewBatchForConsignmentReturn.do":"ACCMaster/getNewBatches.do";
        var StoreUrl = "";
        if(this.isUnbuildAssembly){
            StoreUrl="ACCMaster/getUsedBatchesForAssembly.do";
        } else {
            StoreUrl="ACCMaster/getNewBatches.do";
        }
        /*
         * for Customer assembly Products, fetching batch details from different functions. 
         */
        if (this.isForCustomerAssembly) {
            StoreUrl = "INVStockLevel/getBatchDetailsForJobWorkOrder.do";
        }
        //var StoreUrl = "ACCMaster/getNewBatches.do";
        //         var StoreUrl ="ACCMaster/getNewBatches.do";
        this.batchstore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.batchRec),
            url:StoreUrl,
            sortInfo:{field: "batchname", direction: "ASC"},
            baseParams:{   
                productcode : this.productcode,
                barcodetype : this.barcodetype,
                mainproduct : this.mainassemblyproduct,     //ERP-23242
                isUnbuildAssembly : this.isUnbuildAssembly, //ERP-23242
                location:(this.isLocationForProduct)?obj.record.data.location:"",
                warehouse:(this.isWarehouseForProduct)?obj.record.data.warehouse:"",
                row:(this.isRowForProduct)?obj.record.data.row:"",
                rack:(this.isRackForProduct)?obj.record.data.rack:"",
                bin:(this.isBinForProduct)?obj.record.data.bin:"",
                transType:this.moduleid,
                transactionid:this.transactionid,               
                productid:this.productid,
                assetId:this.assetId,
                isConsignment:this.isConsignment,
                isForconsignment:this.isForconsignment,
                isSerialForProduct:this.isSerialForProduct,
                ispurchase:(this.transactionType!=undefined && this.transactionType)?"":true,
                billid:this.billid,
                isEdit:this.isEdit,
                copyTrans:this.copyTrans,
                isJobworkOrder:this.isJobworkOrder, 
                jobworkorderid:this.jobworkorderid, // to fetch those batches, which are used in Stock adjustment which are linked to job work order having id in jobworkorderid
                linkflag:this.linkflag,
                documentid:(this.isEdit  || (this.linkflag && this.isConsignment))?this.documentid:"",
                producttype:this.productType,
                bomid:this.bomid
            }
        });
        this.batchstore.on("load",function(){		//ERP-26608
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        
        this.batchstore.on("loadexception",function(){
            this.loadedStoreCount++;
            this.checkLoadMask();
        },this);
        this.batchstore.load();
        
    var isAddBatchButton = (obj.isAddBatch!=undefined) ? obj.isAddBatch : false;  
    var isvalidModule = (this.moduleid == Wtf.Acc_Delivery_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId) ? true : false;
    if(this.isBatchBarcode && !isAddBatchButton && obj.field=="batch"  && isvalidModule) {
            this.newdowin = new Wtf.Window({
                    title: WtfGlobal.getLocaleText("acc.batch.barcode"),
                    closable: true,
                    iconCls: getButtonIconCls(Wtf.etype.deskera),
                    width: 330,
                    autoHeight: true,
                    modal: true,
                    bodyStyle: "background-color:#f1f1f1;",
                    closable:true,
                    buttonAlign: 'right',
                    items: [new Wtf.Panel({
                    border: false,
                    html: WtfGlobal.getLocaleText("acc.batch.barcode.window.info"),
//                    html: "Scan your barcode here. </br>For valid barcode, batch details will be auto-populate. </br>For invalid barcode, batch details will not be auto-populate.",
                    height: 50,
                    bodyStyle: "background-color:white; padding: 7px; font-size: 11px; height: 0px; border-bottom: 1px solid #bfbfbf;"
                }),
                this.newdoForm = new Wtf.form.FormPanel({
                    labelWidth: 110,
                    border: false,
                    autoHeight: true,
                    bodyStyle: 'padding:10px 5px 3px; ',
                    autoWidth: true,
                    defaultType: 'textfield',
                    items: [this.newdono = new Wtf.form.TextField({
                        fieldLabel: WtfGlobal.getLocaleText("acc.batch.barcode"),
                        allowBlank: false,
                        labelSeparator: '',
                        width: 110,
                        selectOnFocus : true,
                        itemCls: 'nextlinetextfield',
                        name: 'newdono',
                        id: 'newdono'
                    })],
                        buttons: [
                            {
                                text: WtfGlobal.getLocaleText("acc.msgbox.ok"), //"Ok",
                                scope: this,
                                handler: function () {
                                var value = this.newdono.getRawValue();
                                var allowBatchFlag=false; //flag to check whether to allow validation to batch or not
                                //ERM-1242:In case of Generate GRN from Vendor Invoice or GRN Module,Batch can be entered manually
                                if((this.parentObj.autoGenerateDO!=undefined &&this.parentObj.autoGenerateDO!=null && this.parentObj.autoGenerateDO.getValue() && this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) || (this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId)) {
                                    allowBatchFlag=true;
                                }
                                
                                if(!allowBatchFlag){
                                    var index = WtfGlobal.searchRecordIndex(this.batchstore, value, 'batchname');
                                    if (index != -1) {
                                        var batchRec = this.batchstore.getAt(index);
                                        var dataObj = batchRec.data;
                                        this.setBatchWindowDetails(obj, dataObj, 1);
                                        this.newdowin.close();
                                    } else {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.batch.barcodeerror")], 2);
                                        this.newdono.focus(true, 100);
                                    }
                                }else{
                                    //ERM-1242:When users enters batch manually.Not validating batchname and setting in Grid in GRN & Vendor Invoice with auto GRN
                                    this.setBatchForGRNModule(obj,value);
                                }
                            }
                        }
//                            ,{
//                                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
//                                scope: this,
//                                handler: function () {
//                                    this.newdowin.close();
//                                }
//                            }
                        ]
                })]
                });
                this.newdono.on('specialkey', function(field){
                    
                var allowBatchFlag=false;//flag to check whether to allow validation to batch or not
                //ERM-1242:Avoiding batch Store check in case of Generate GRN from Vendor Invoice or GRN Module,Batch can be entered manually
                if((this.parentObj.autoGenerateDO!=undefined &&this.parentObj.autoGenerateDO!=null && this.parentObj.autoGenerateDO.getValue() && this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) || (this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId)) {
                    allowBatchFlag=true;
                }
                if(!allowBatchFlag){
                    if (field.getValue() != ""){
                        var value = this.newdono.getRawValue();
                        var index = WtfGlobal.searchRecordIndex(this.batchstore, value, 'barcodebatch');
                        if (index != - 1){
                            var batchRec = this.batchstore.getAt(index);
                            var dataObj = batchRec.data;
                            this.setBatchWindowDetails(obj, dataObj, 1);
                            this.newdowin.close();
                        } else {
                            field.setValue("");
                            this.newdono.focus(true, 100);
                        }
                    } else {
                        field.setValue("");
                        this.newdono.focus(true, 100);
                    }
                }

            }, this);
                this.newdowin.show();
                this.newdono.focus(true, 100);    //100 has set to show cursor in window for some time period
    } else {
        this.batchgrid = new Wtf.grid.GridPanel({
            height:210,
            width:'97%',
            store: this.batchstore,
            cm: this.batchcm,
            sm:this.sm,
            border : false,
            autoScroll:true,
            loadMask : true,
            viewConfig: {
//                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.batchgrid.on("render", function(grid) {
            this.batchgrid.getView().applyEmptyText();
             new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.batchgrid.on('statesave', this.saveBatchGridStateHandler, this);
                }, this);  
        }, this);
        
        this.getBatchGridConfig();
        
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
                        this.setBatchWindowDetails(obj, this.rec.data, this.sm.getCount());
                        return false;
                    }else{
                        this.rec = this.sm.getSelected();
                        this.setBatchWindowDetails(obj, this.rec.data, this.sm.getCount());
                    }
                    batchWin.close();
                }
            }],
            autoScroll : true,
            modal : true
        });
        batchWin.show(); 
      }   
    },
    //ERP-41083:Called in case of Barcode scanner using batchname only
    setBatchForGRNModule:function(obj,batchname){
        //restrict duplicate batchname in GRN Module if it is activatd in Company Prefrences
        if(Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch){
            Wtf.Ajax.requestEx({
                url:"INVStockLevel/isBatchExists.do",
                params: {
                    productid:this.itemId,
                    batch : batchname
                }
            },
            this,
            function(result, req){
                var title="Error";
                if(result.success){
                                
                    var isBatchExists=result.data.isBatchPresent;
                                
                    if(isBatchExists == true){
                        title="Warning";
                        WtfComMsgBox([title,"The batch you entered  : <b>"+batchname+"</b> already exists."],0);
                        this.newdono.focus(true, 100);
                    }else{
                        //set batchname in Grid
                        obj.record.set("batch", batchname);
                        obj.record.set("batchname",batchname);
                        this.newdowin.close();
                    }
                }
                else if(result.success==false){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.batch.barcodeerror")], 2);
                    this.newdono.focus(true, 100);
                }
            },
            function(result, req){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.batch.barcodeerror")], 2);
                this.newdono.focus(true, 100);
            });
                                  
        }else{//set batch name in Grid
            obj.record.set("batch", batchname);
            obj.record.set("batchname",batchname);
            this.newdowin.close();
        }
    },
    setBatchWindowDetails: function (obj, batchRec, smcount) {
        if (smcount > 0) {
            if (batchRec.mfgdate != undefined && batchRec.mfgdate != "" && batchRec.mfgdate != null) {
                obj.record.set("mfgdate", batchRec.mfgdate.clearTime(true));
            } else {
                obj.record.set("mfgdate", '');
            }
            if (batchRec.expdate != undefined && batchRec.expdate != "" && batchRec.expdate != null) {
                obj.record.set("expdate", batchRec.expdate.clearTime(true));
            } else {
                obj.record.set("expdate", '');
            }
            obj.record.set("batch", batchRec.batchname);
            obj.record.set("batchname", batchRec.batchname);
            obj.record.set("purchasebatchid", batchRec.batch);
            obj.record.set("documentbatchid", batchRec.batch);
            obj.record.set("attachment", batchRec.attachment);
            obj.record.set("attachmentids", batchRec.attachmentids);
//            obj.record.set("location", batchRec.location);
//            obj.record.set("warehouse", batchRec.warehouse);
//            obj.record.set("row", batchRec.row);
//            obj.record.set("rack", batchRec.rack);
//            obj.record.set("bin", batchRec.bin);
            var rowindex = obj.row;
            var total = obj.grid.store.getCount();
            if (rowindex == total - 1 && !this.isFixedAsset) {
                this.addNewRowCheck(obj)
            }
        }
        if (batchRec.batchname != obj.value) {  //if  cuurently  selec ted  batch is not  equal to another batch den reset serial no details
            obj.record.set('serialno', '')
            obj.record.set('expstart', '')
            obj.record.set('expend', '')
        }
        var filterJson = '[';
        filterJson += '{"id":"' + batchRec.id + '","location":"' + batchRec.location + '","warehouse":"' + batchRec.warehouse + '","productid":"' + batchRec.productid + '","documentid":"' + this.documentid + '","purchasebatchid":"' + batchRec.batch + '"},';
        filterJson = filterJson.substring(0, filterJson.length - 1);
        filterJson += "]";
        this.checkAvailableQty(obj, filterJson);
    },
    saveBatchGridStateHandler: function (grid, state) {//To save config details when we hide or show columns
        
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Batch_No_Window_Grid_Id+ "_" + this.moduleid, grid.gridConfigId, false);
    },
    getBatchGridConfig:function(){//To load config details
        WtfGlobal.getGridConfig(this.batchgrid, Wtf.Batch_No_Window_Grid_Id + "_" + this.moduleid, false, false);
    },
    resetGridRow: function (obj) {
        var size = this.store.getCount();
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = this.serialNumberCM.config.length;
        var values = {},blankObj={};
        var refreshRow=false;
        for(var j = 0; j < fl; j++){
            f = this.serialNumberCM.config[j];
            if(obj.field==f.dataIndex && !refreshRow){ 
                refreshRow=true;
                continue;
            }else if(!refreshRow){
                continue;
            }            
            //            f = fi[j];
                   
            if(f.name!='rowid'&& f.dataIndex != "quantity" ) {            //code for set warehouse and location ERP-17329
                if((obj.field === "warehouse" || obj.field === "location") && (f.dataIndex === "warehouse" || f.dataIndex === "location")){
                    var index=obj.field === "warehouse"?this.levelNm.indexOf("Locations"):this.levelNm.indexOf("Warehouse");
                    if(index != -1 && obj.field === "warehouse" && this.isLocationForProduct == true && this.parent[index] != 0 && this.locationEditor.store.getCount()==1){
                       // obj.record.set(f.dataIndex,this.locationEditor.store.getAt(0).data.id);
                    }else if(index != -1 && obj.field === "location" && this.parent[index] != 0 && this.wareHouseEditor.store.getCount()==1){
                        obj.record.set(f.dataIndex,this.wareHouseEditor.store.getAt(0).data.id);
                    }else{
                        obj.record.set(f.dataIndex,"");
                    }
                      
                }else if(f.dataIndex!=undefined && f.dataIndex != "warehouse" && f.dataIndex != "location" && f.dataIndex.indexOf('Custom_') === -1){
                    obj.record.set(f.dataIndex,"");
                }    
            }
        }        
        
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
    addGridRec: function (record, addDefault, isReadOnly) {
        var size = this.store.getCount();

        var rec = this.accRec;
        var size = this.store.getCount();
       
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for(var j = 0; j < fl; j++){
            f = fi[j];
            if(f.name!='rowid') {
                blankObj[f.name]='';
            }
        }
        rec = new rec(blankObj);
       
        rec.beginEdit();
        //   var fields = this.store.fields;
        var Record = this.store.reader.recordType,fields = Record.prototype.fields, fi = fields.items, fl = fields.length;
        for (var x = 0; x < fl; x++) {
            var value = "";
            if (record != undefined && record != "") {
                if (fields.get(x).type == "date") {
                    value = record[fields.get(x).name];
                    if (value != "" && value!=undefined)
                        value = new Date(value);
                }
                else {
                    if(record && record[fields.get(x).name] != "" && record[fields.get(x).name] != undefined)
                        value = unescape(record[fields.get(x).name]); //for saving the serial no. its showing %20 in space
                }
            }
            if ((isReadOnly && isReadOnly != "false") && (fields.get(x).name == "location" || fields.get(x).name == "warehouse" || fields.get(x).name == "row" || fields.get(x).name == "rack" || fields.get(x).name == "bin" || fields.get(x).name == "mfgdate" || fields.get(x).name == "expdate" || fields.get(x).name == "batch" || fields.get(x).name == "quantity")) {
                continue;
            }
            if (addDefault && (record == "" || record == undefined)) {
                if (fields.get(x).name == "location" && (this.defaultLocation != undefined && this.defaultLocation != "" && this.isLocationForProduct && this.store.getCount() <=1 ))
                    rec.set(fields.get(x).name, this.defaultLocation);
                else if (fields.get(x).name == "warehouse" && (this.defaultWarehouse != undefined && this.defaultWarehouse != "" && this.isWarehouseForProduct && this.store.getCount() <=1))
                    rec.set(fields.get(x).name, this.defaultWarehouse);
                else if (fields.get(x).name == "serialno" && (this.assetId != undefined && this.assetId != ""))
                    rec.set(fields.get(x).name, this.assetId);  //set  serialno as assetid                
                else if (fields.get(x).name == "packwarehouse" && Wtf.account.companyAccountPref.pickpackship && !Wtf.account.companyAccountPref.interloconpick && Wtf.account.companyAccountPref.packingstore != undefined){
                    rec.set(fields.get(x).name, Wtf.account.companyAccountPref.packingstore);  //set  packwarehouse
                }
                else if (fields.get(x).name == "packlocation" && Wtf.account.companyAccountPref.pickpackship && Wtf.account.companyAccountPref.interloconpick && Wtf.account.companyAccountPref.packinglocation != undefined){
                    rec.set(fields.get(x).name, Wtf.account.companyAccountPref.packinglocation);  //set  packlocation
                }
                else
                    rec.set(fields.get(x).name, value);                
            } else {
                rec.set(fields.get(x).name, value);
                if (fields.get(x).name == "packwarehouse" && Wtf.account.companyAccountPref.pickpackship && !Wtf.account.companyAccountPref.interloconpick && Wtf.account.companyAccountPref.packingstore != undefined){
                    rec.set(fields.get(x).name, Wtf.account.companyAccountPref.packingstore);  //set  serialno as assetid
                }else if (fields.get(x).name == "packlocation" && Wtf.account.companyAccountPref.pickpackship && Wtf.account.companyAccountPref.interloconpick && Wtf.account.companyAccountPref.packinglocation != undefined){
                    rec.set(fields.get(x).name, Wtf.account.companyAccountPref.packinglocation);  //set  serialno as assetid
                }
//                if(fields.get(x).name == 'location'){
//                    var locValue=value;
//                }
//                if(fields.get(x).name == 'warehouse' && value !='' ){
//                     
//                    this.locationEditor.store.on("beforeload",function(){
//                        this.locationEditor.store.removeAll(); 
//                        this.locationEditor.store.clearFilter();
//                    },this)
//               
//                    this.locationEditor.store.load({
//                        params:{
//                            storeid:value
//                        }
//                    });
//                    this.locationEditor.store.on('load',function(){
//                        this.locationEditor.store.clearFilter();
//                        rec.set('location', locValue);
//                    },this)
//                }
            }
            if (fields.get(x).name == 'quantity') {
                if ((!this.isLocationForProduct && !this.isWarehouseForProduct && !this.isBatchForProduct && !this.isRowForProduct && !this.isRackForProduct && !this.isBinForProduct && this.isSerialForProduct)) {
                    rec.set(fields.get(x).name, "1");
                } else {
                    var remaningQty=0;
                    var modulecondition = this.checkModuleIdisPresent(this.moduleid);
                    this.store.each(function(storeRecord){
                        if((storeRecord.data.quantity != undefined && storeRecord.data.quantity != "") && (( this.isBatchForProduct || ( this.lastRecord != storeRecord)))){
                            /**
                             * Checking whether the current module belongs to the module in which this functionality is implemented and "Variable Purchase/Sales UOM conversion rate" 
                             * check is enabled in company preferences. If yes than checking if we have to allow user to edit Qty in batch serial window as if SO is linked to 
                             * SI or DO in that case we dont have to allow user to edit Qty. ERM-319
                             */
                            if (!CompanyPreferenceChecks.differentUOM() ||  (CompanyPreferenceChecks.differentUOM() && (!modulecondition || (modulecondition && (!Wtf.isEmpty(this.allowUserToEditQuantity) && !this.allowUserToEditQuantity))))) {     
                                if(getRoundofValue((remaningQty + getRoundofValue(storeRecord.data.quantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)) > this.quantity){
                                    if(!this.isSerialForProduct){
                                        rec.set(fields.get(x).name,parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                                    }else{
                                        rec.set(fields.get(x).name,parseInt(this.quantity-remaningQty));
                                    }
//                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")],2);
                                    return false;
                                }
                            }
                            remaningQty = getRoundofValue(remaningQty + getRoundofValueWithValues(storeRecord.data.quantity,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                        }
                    },this);
                    if ((this.isEdit || this.linkflag) && !this.copyTrans && value=="") {
                        if(!this.isSerialForProduct){
                            rec.set(fields.get(x).name,parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                        }else{
                            rec.set(fields.get(x).name,parseInt(this.quantity-remaningQty));
                        }
                    } else {
                        if (record != undefined){
                            if(!this.isSerialForProduct){
                                value = parseFloat(value).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                            }else{
                                value = parseInt(value);
                            }
                            rec.set(fields.get(x).name, (parseFloat(this.quantity)<=parseFloat(value) && !this.isSerialForProduct)? this.quantity:value);
                            }
                        else{
                            if(!this.isSerialForProduct){
                                rec.set(fields.get(x).name,parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                            }else{
                                rec.set(fields.get(x).name,parseInt(this.quantity-remaningQty));
                            }
                        }
                            
                    }
                }


            }
            if (fields.get(x).name == 'avlquantity') {
                if (!this.isSerialForProduct && ((rec.get("location") !=undefined && rec.get("location") !="") || (rec.get("warehouse") !=undefined && rec.get("warehouse") !=""))) {
                    rec.set(fields.get(x).name, this.defaultAvailbaleQty);
                }
                if (record != undefined && record != "" && record.avlquantity!="" && record.avlquantity!=undefined){
                    var avlQty = parseFloat(record.avlquantity).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    rec.set(fields.get(x).name, parseFloat(avlQty));
                    this.AvailableQuantity=parseFloat(avlQty);
                    //ERP-39168 : To display only link DO quantity to Sales Return Serial Window
                    if(this.linkflag && this.moduleid == Wtf.Acc_Sales_Return_ModuleId ){
                        rec.set(fields.get(x).name, this.defaultAvailbaleQty);
                    }
                }
            }
            if (fields.get(x).name == "productid") {
                rec.set(fields.get(x).name, this.productid);
            }
            if (fields.get(x).name == "isreadyonly") {
                rec.set(fields.get(x).name, isReadOnly);
            }
            if (fields.get(x).name == "wastageQuantityType" || fields.get(x).name == "wastageQuantity") {
                rec.set(fields.get(x).name, 0);
                if (record && record[fields.get(x).name] != undefined) {
                    rec.set(fields.get(x).name, record[fields.get(x).name]);
                }
            }
            
            if (record&&fields.get(x).name == "stocktype"&&record[fields.get(x).name]=="0") {
                rec.set(fields.get(x).name, 0);
                if (record && record[fields.get(x).name] != undefined) {
                    rec.set(fields.get(x).name, record[fields.get(x).name]);
                }
            }
            if(fields && fields.get(x).name != undefined && fields.get(x).name == "stocktype" ){
                rec.set(fields.get(x).name, 1);// set by default 'System Stock'.
            }
            if (fields.get(x).name.indexOf('Custom_') != -1 ) {
                if(this.lineRec!=undefined && this.lineRec!=""){
                    var value = this.lineRec.data[fields.get(x).name];
                    var fieldname = fields.get(x).name;
                    
                    var GlobalcolumnModel = GlobalColumnModel[this.moduleid];
                    if (GlobalcolumnModel) {
                        for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                            var fname = GlobalcolumnModel[cnt].fieldname;
                            var fieldtype = GlobalcolumnModel[cnt].fieldtype;
                            if (fieldname == fname) {
                                if (fieldtype == Wtf.CustomFieldType.SingleSelectDropdown || fieldtype == Wtf.CustomFieldType.MultiSelectDropdown) {
                                      this.populateCustomFieldValue(this.grid);
                                } else{
                                     rec.set(fields.get(x).name, value);
                                }
                                
                            }
                           
                        }
                    }
                }
            }
            
        //            for (var key in record) {
        //                if (key.indexOf('Custom') != -1 && record[key] != undefined && record[key] != "null") { // 'Custom' prefixed already used for custom fields/ dimensions
        //                    //  recObj[key] = record[key+"_Value"];
        //                    if(record[key]== "null" ){
        //                        rec.set(key, "");
        //                    }
        //                    rec.set(key, record[key]);
        //                }
        //            }

        }
        for (var key in record) {
            if (key.indexOf('Custom') != -1 && record[key] != undefined) { // 'Custom' prefixed already used for custom fields/ dimensions
                //  recObj[key] = record[key+"_Value"];
                if (record[key] != "null" && record[key] != "" && record[key] != "undefined" && record[key] != "NaN") {
                    rec.set(key, record[key]);
                } else {
                    rec.set(key, "");
                }

            }
        }
        if (record != "" && record != undefined) {
            var data = record.customfield;
            if (data != undefined & data != "") {
                for (var i = 0; i < data.length; i++) {
                    var value = data[i].fieldname;
                    value = data[i][value];
                    value = data[i][value];
                    if (value != "" && value != "undefined" && value != "NaN" && value != "null") {
                        if (data[i].xtype == "3") {
                            value = parseInt(value);
                            value = new Date(value);
                        }
                        rec.set(data[i].fieldname, value);
                    } else {
                        rec.set(data[i].fieldname, "");
                    }
                }
            }
            rec.set("documentbatchid",record.purchasebatchid);
            rec.set("attachment",record.attachment);
        }
        rec.endEdit();
        rec.commit();
        this.store.add(rec);
        if (this.isfromProductAssembly && !this.isSerialForProduct && !this.isBatchForProduct && ((rec.get("location") != undefined && rec.get("location") != "") || (rec.get("warehouse") != undefined && rec.get("warehouse") != ""))) {
            this.includeLastRowInProdAssembly=true;
            Wtf.Ajax.requestEx({
                url: "ACCInvoice/getBatchRemainingQuantity.do",
                params: {
                    batchdetails: this.getBatchDetails(),
                    transType: this.moduleid,
                    isEdit: this.isEdit
                }
            }, this, function (res, req) {
                //                    alert(res.quantity);
                this.AvailableQuantity = res.quantity;
                rec.set("avlquantity", this.AvailableQuantity);
            }, function (res, req) {
                rec.set("avlquantity", this.defaultAvailbaleQty);
            });
        }
    },
    /**
     * Auto populate custom field value from Line level to serial window level.
     */
    populateCustomFieldValue: function(grid) {
        var GlobalcolumnModel = GlobalColumnModel[this.moduleid];
        if (GlobalcolumnModel) {
            for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                var fieldname = GlobalcolumnModel[cnt].fieldname;
                var iscustomfield = GlobalcolumnModel[cnt].iscustomfield;
                var value = this.lineRec.data[fieldname];
                var dropDowntype = false;
                if (value != undefined && value != "") {
                    if (GlobalcolumnModel[cnt].fieldtype == Wtf.CustomFieldType.SingleSelectDropdown || GlobalcolumnModel[cnt].fieldtype == Wtf.CustomFieldType.MultiSelectDropdown) {
                        value = this.getValueForDimension(fieldname, value);
                        dropDowntype = true;
                    }
                    var array = grid.store.data.items;
                    if (array.length > 0) {
                        for (var i = 0; i < array.length - 0; i++) {
                            for (var k = 0; k < grid.colModel.config.length; k++) {
                                if (grid.colModel.config[k].dataIndex == fieldname) {
                                    var gridRecord = grid.store.getAt(i);
                                    if (dropDowntype && grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store) {
                                        var store = grid.colModel.config[k].editor.field.store;
                                        var valArr = value.split(',');
                                        var ComboValueID = "";
                                        for (var index = 0; index < valArr.length; index++) {
                                            var recCustomCombo = WtfGlobal.searchRecord(store, valArr[index], "name");
                                            if (recCustomCombo)
                                                ComboValueID += recCustomCombo.data.id + ',';
                                        }
                                        if (ComboValueID.length > 0)
                                            ComboValueID = ComboValueID.substring(0, ComboValueID.length - 1);
                                        gridRecord.set(fieldname, ComboValueID);
                                    } else {
                                        gridRecord.set(fieldname, value);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    },
    /*
     * Returns value for drop down custom fields from group grid 
     */
    getValueForDimension: function(fieldName, value) {
        var grid = this.parentGrid;
        if (grid) {
            var array = grid.store.data.items;
            if (array.length > 1) {
                for (var i = 0; i < array.length - 1; i++) {
                    for (var k = 0; k < grid.colModel.config.length; k++) {
                        if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store && grid.colModel.config[k].dataIndex == fieldName) {
                            var store = grid.colModel.config[k].editor.field.store;
                            var gridRecord = grid.store.getAt(i);
                            var valArr = value.split(',');
                            var returnData = "";
                            for (var index = 0; index < valArr.length; index++) {
                                var recCustomCombo = WtfGlobal.searchRecord(store, valArr[index], "id");
                                if (recCustomCombo)
                                    returnData += recCustomCombo.data.name + ',';
                            }
                            return returnData;
                        }
                    }
                }
            }
        }
    },
    /*
     * Disable custom fields at serial window level
     */
    disableCustomFieldOfGrid: function() {
        for (var k = 0; k < this.grid.colModel.config.length; k++) {
            if (this.grid.colModel.config[k].dataIndex.indexOf('Custom_') != -1) {
                this.grid.colModel.config[k].editor = '';
            }
        }
    },
    getJSONArray: function (arr) {
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
    LoadConfigurationAfterStoreLoad:function(){
        var batchrecords="";
        if(this.batchDetails!=undefined && this.batchDetails.length>1){
            batchrecords= eval('(' + this.batchDetails + ')');
        }
        if((!this.isEdit || this.copyTrans)  && batchrecords.length==0){
            if((this.isBatchForProduct || this.isLocationForProduct || this.isWarehouseForProduct || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct) &&  !this.isSerialForProduct){
                this.recordLength=1; //if only batch option is on then only one batch is added so only one rwo is shown
            }else if(!(this.isLocationForProduct&&this.isWarehouseForProduct) && this.moduleid != Wtf.Acc_ConsignmentSalesReturn_ModuleId && !this.isSerialForProduct){// && !(this.isBatchForProduct && this.isSerialForProduct)){
                this.recordLength=1;
            }else{
                this.recordLength=this.quantity;
            }
            for(var i=0;i<this.recordLength;i++){
                if(batchrecords.length>0 && i<=batchrecords.length){
                    var batchObj=batchrecords[i];
                    var isReadOnly=batchObj.isreadyonly;                    
                    this.addGridRec(batchObj,(i==0),isReadOnly);
                }else{
                    if((this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct) && this.isSerialForProduct && i!=0){
                        this.addGridRec(undefined,(i==0),true);
                    }else{
                        this.addGridRec(undefined,(i==0),false);
                    }
                }
            } 
        }
        else{
            var recordQuantity=batchrecords.length;
            if(this.isSerialForProduct && this.isConsignment)
            {
                if(this.quantity<batchrecords.length){
                    recordQuantity=this.quantity;   
                }
            }
            if(batchrecords.length!=0){
                for(var i=0;i<recordQuantity;i++){
                    var batchObj=batchrecords[i];
                    var isReadOnly=batchObj.isreadyonly;                    
                    this.addGridRec(batchObj,(i==0),isReadOnly);
                }
            }else{
                if((this.isBatchForProduct || this.isLocationForProduct || this.isWarehouseForProduct  || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct) &&  !this.isSerialForProduct && (!this.linkflag||this.isSales)){
                    this.recordLength=1; //if only batch option is on then only one batch is added so only one rwo is shown
                }else{
                    this.recordLength=this.quantity;
                }
                for(var i=0;i<this.recordLength;i++){
                    
                    if((this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct) && this.isSerialForProduct && i!=0){
                        this.addGridRec(undefined,(i==0),true);
                    }else{
                        this.addGridRec(undefined,(i==0),false);
                    }
                   
                }
            }
               
                
        }
        if(!this.isFixedAsset){// && !this.isfromProductAssembly){  //Do not add extra quantity for batch & serial product
            this.addGridRec(undefined,(i==0),false);
        }
        
    },
    checkQuantity:function(store){
        var enteredQty=0;
        var storeQty=0;
        var cnt=store.getCount()-1;
        if(this.isFixedAsset)
        {
            storeQty=store.getCount();
        }else{
            storeQty=store.getCount()-1;
        }
//        if(!this.isSerialForProduct&&!this.isBatchForProduct&&store.getAt(cnt).data!=undefined){
//            if(store.getAt(cnt).data.warehouse!=""&&store.getAt(cnt).data.location!=""&&store.getAt(cnt).data.warehouse!=undefined&&store.getAt(cnt).data.location!=undefined){
//                storeQty=store.getCount();
//            }
//        }
        
        for(var i=0;i<storeQty;i++){
            if(store.getAt(i).data.quantity !=undefined && store.getAt(i).data.quantity !=""){
                enteredQty+=this.isSerialForProduct?parseInt(store.getAt(i).data.quantity):parseFloat(store.getAt(i).data.quantity);    
            }            
        }
        var modulecondition = this.checkModuleIdisPresent(this.moduleid);
        /**
         * Checking whether the current module belongs to the module in which this functionality is implemented and "Variable Purchase/Sales UOM conversion rate" 
         * check is enabled in company preferences. If yes than checking if we have to allow user to edit Qty in batch serial window as if SO is linked to 
         * SI or DO in that case we dont have to allow user to edit Qty. OR If the selected UOM is different from Stock UOM of product even thanwe dont have 
         * to allow user to edit Qty in batch serial window ERM-319
         */
//        if (!CompanyPreferenceChecks.differentUOM()  ||  (CompanyPreferenceChecks.differentUOM() && !modulecondition)) {                //Checking wether the current module belongs to the module in which this functionality is implemented and "Variable UOM" check is enabled in company preferences. ERM-319
        if (!CompanyPreferenceChecks.differentUOM() ||  (CompanyPreferenceChecks.differentUOM() && ((!modulecondition || (modulecondition && (!Wtf.isEmpty(this.allowUserToEditQuantity) && !this.allowUserToEditQuantity))) || (this.productUomid==this.selectedProductUomid) || (this.isOnlyBatchForProduct!=undefined && !this.isOnlyBatchForProduct)))) {            //Checking wether the current module belongs to the module in which this functionality is implemented and "Variable UOM" check is enabled in company preferences. ERM-319
            if(parseFloat(enteredQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)!= parseFloat(this.quantity).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) && this.moduleid!=Wtf.Acc_Sales_Return_ModuleId){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")],2);
                return false;
            }
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
        this.isfromSubmit=true;
        if(this.isSales &&  !this.isFixedAsset && !this.isLeaseFixedAsset &&  (this.isBatchForProduct || this.isLocationForProduct || this.isWarehouseForProduct  || this.isRowForProduct  || this.isRackForProduct  || this.isBinForProduct) && !this.isSerialForProduct){
            Wtf.Ajax.requestEx({
                url: "ACCInvoice/getBatchRemainingQuantity.do",
                params: {
                    batchdetails:this.getBatchDetails(),
                    transType:this.moduleid,
                    isEdit:this.isEdit,
                    linkflag:this.linkflag,
                    fromSubmit:true,
                    documentid:((this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && this.isEdit)?this.docrowid : ""
                }
            },this,this.genSuccessResponseBatch,this.genFailureResponseBatch);
        }else{
            this.close();
        }
    },
    genSuccessResponseBatch : function(response){
        //        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],7);
        this.remainingQuantity=response.quantity;
        if((this.isBatchForProduct || this.isLocationForProduct || this.isWarehouseForProduct  || this.isRowForProduct  || this.isRackForProduct  || this.isBinForProduct) && !this.isSerialForProduct){
            if(!Wtf.account.companyAccountPref.isnegativestockforlocwar && !this.isFixedAsset && !this.isLeaseFixedAsset && this.isSales && (!this.linkflag ||this.isLinkedFromSO ||this.isLinkedFromCI ||this.isLinkedFromGR || this.isLinkedFromPI ||this.isLinkedFromSI || this.isLinkedFromDO)){  //check remaini){
                if(this.remainingQuantity==0 && this.moduleid!=Wtf.Acc_Sales_Return_ModuleId && !this.isUnbuildAssembly)
                {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.remqty")],2);
                    return false;
                }
            }
        }
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
                            if(requiredQuntity > avlQuantity && this.moduleid!=Wtf.Acc_Sales_Return_ModuleId){
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
        var len=0;
        if(this.isFixedAsset){
            len=store.getCount()
        }else{
            len=store.getCount()-1; 
        }
        /*
         * Commenting below for ERP-40483
         **/
       // if(this.moduleid!=Wtf.Acc_Sales_Return_ModuleId)
            if(len != this.quantity && this.isSerialForProduct){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")],2);
                return false;
            }
        var batch="";
        for(var i=0;i<len;i++){
            var rec=store.getAt(i);
            var readOnlyFlag=rec.data['isreadyonly'];
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                if(fields.get(j).name=='location' && i==0 && this.isLocationForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.location.empty") + " " + (i + 1)], 2);
                        //alert("Location Should not empty. Please check row " +i+1)    
                        return false;
                    }
                    if(this.checkduplicateLocation(value,i) && (value!="" || value != undefined)){
                    }else{
                        return false;
                    }
                }else if(fields.get(j).name=='warehouse' && i==0 && this.isWarehouseForProduct){
                    if(value=="" || value == undefined){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.warehouse.empty") + " " + (i + 1)], 2);
                        //alert("warehouse Should not empty. Please check row " +i+1)    
                        return false;
                    }
                    if(this.checkduplicateWarehouse(value,i) && (value!="" || value != undefined)){
                    }else{
                        return false;
                    }
                }
                
                
              
                  
                  else if(fields.get(j).name=='packwarehouse' && i==0 && this.packWarehouse && this.moduleid==Wtf.Acc_Delivery_Order_ModuleId){
                    if(Wtf.account.companyAccountPref.pickpackship && !Wtf.account.companyAccountPref.interloconpick){
                        if(Wtf.account.companyAccountPref.packingstore==undefined || Wtf.account.companyAccountPref.packingstore==""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.pickpackship.msg4")+" "+"in"+" "+WtfGlobal.getLocaleText("acc.field.SystemControls") ], 2);
                            return false;
                        }else if(value=="" || value == undefined){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.packwarehouse.empty") + " " + (i + 1)], 2);
                            //alert("packwarehouse Should not empty. Please check row " +i+1)    
                            return false;
                        }  
                    }
//                    
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
                }
                else if(fields.get(j).name=='batch' && (!rec.data.isreadyonly || rec.data.isreadyonly=="false")){    //do not allow to save if batch is not entered
                    //                    if(Wtf.account.companyAccountPref.isBatchCompulsory ||   this.isBatchForProduct) //only check bach is enterrd or not if batch option is true
                    if(this.isBatchForProduct) //only check bach is enterrd or not if batch option is true
                    {
                        if(value=="" || value == undefined){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.inventorysetup.alert.batch.empty") + " " + (i + 1)], 2);
                            return false;
                        }
                        
                        //                        if((this.moduleid=="28" || this.moduleid=="30")){
                        if(this.checkduplicateBatches(value,i) && (value!="" || value != undefined)){
                            
                        }else{
                            return false;
                        }
                    //                        }
                    }
                
                }else if(fields.get(j).name=='mfgdate' && i==0){
                    
                } else if((fields.get(j).name=='quantity') && !this.isSerialForProduct){
                    var isvalidModule = (this.moduleid == Wtf.Acc_Delivery_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Goods_Receipt_ModuleId) ? true : false;
                    if((value==0 || value=="0" || value=="" || value == undefined) && isvalidModule){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.batch.qtycannotzero") + " " + (i + 1)+" "+WtfGlobal.getLocaleText("acc.field.batch.zero.row")], 2);
                        return false;
                    }
                } else if(fields.get(j).name=='expdate' && i==0){
                    if(value && value < rec.data[fields.get(j-1).name]){
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
                        if((readOnlyFlag==false || readOnlyFlag== "false")&& this.isBatchForProduct){
                            batch=rec.data['batch'];
                        }
                         
                        if(this.checkduplicateserialno(value,i,batch) && (value!="" || value != undefined)){
                            
                        }else{
                            return false;
                        }
                    }
                }else if(fields.get(j).name=='skufield'){   //do not allow to save if sku. is not entered
                    if(Wtf.account.companyAccountPref.SKUFieldParm && this.isSKUForProduct) 
                    {
                        if(value=="" || value == undefined){
                            var SKULebel= (Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined) ? Wtf.account.companyAccountPref.SKUFieldRename :WtfGlobal.getLocaleText("acc.product.sku");
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), SKULebel+" Should not be empty. Please Enter the "+SKULebel+" First in row " +(i+1)], 2);
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
       if(this.packWarehouse && Wtf.account.companyAccountPref.pickpackship && this.moduleid==Wtf.Acc_Delivery_Order_ModuleId && (this.parentObj.pickpacklocation=="" || this.parentObj.pickpacklocation==undefined)){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.product.packagingwarehouse")],2);
                        return false;
                    }
        this.validateBatchDetails();
    },
    
  checkduplicateserialno:function(testValue,excluderow,batch){
        var store=this.grid.getStore();
        var len=store.getCount()-1;
        var batchname="";
        for(var i=0;i<len;i++){
            var rec=store.getAt(i);
            var readOnlyFlag=rec.data['isreadyonly'];
            if(excluderow==i){
                if(readOnlyFlag==false || readOnlyFlag== "false"){
                    batchname=rec.data['batch'];
                }
                continue;
            }
            if(readOnlyFlag==false || readOnlyFlag== "false"){
                batchname=rec.data['batch'];
            }
            var serialname=rec.data['serialno'];    
            if(testValue==serialname  && batch==batchname){ //&& batchname !=""
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.srd")],2);
                return false;
            }
        }
        return true;
        
    },
    checkduplicateBatches:function(testValue,excluderow){
        var store=this.grid.getStore();
        var currentRowArr=store.getAt(excluderow);
        var fields=store.fields;
        var len=store.getCount();
        for(var i=0;i<len;i++){
            if(excluderow==i)continue;
            var rec=store.getAt(i);
            if (rec.data.batch != undefined &&  currentRowArr.data.batch != undefined && rec.data.batch == currentRowArr.data.batch&&((rec.data.stocktype!=undefined&&rec.data.stocktype == currentRowArr.data.stocktype)||(rec.data.stocktype==undefined&&currentRowArr.data.stocktype==undefined)||(rec.data.stocktype==""&&currentRowArr.data.stocktype==""))) {
                //                if (rec.data.rack == currentRowArr.data.rack && rec.data.row == currentRowArr.data.row && rec.data.serialno == currentRowArr.data.serialno && rec.data.warehouse == currentRowArr.data.warehouse && rec.data.location == currentRowArr.data.location) {
                // If we have to select different serial no.s from same batch, same location & same warehouse then select only once rather than in different rows. 
               if(Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch){ //if batch already exist
                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.batch.duplicatebatchname")], 2);
                        return false;  
               }
               if (rec.data.rack == currentRowArr.data.rack && rec.data.row == currentRowArr.data.row && rec.data.bin == currentRowArr.data.bin && rec.data.warehouse == currentRowArr.data.warehouse && rec.data.location == currentRowArr.data.location) {
                    if (this.isSerialForProduct) {
                        if (currentRowArr.data.serialno == rec.data.serialno) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.batch.duplicatebatchname")], 2);
                            return false;
                        }
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.batch.duplicatebatchname")], 2);
                        return false;
                    }
                }
            }
        }
        return true;
        
    },
    checkduplicateLocation:function(testValue,excluderow){
        var store=this.grid.getStore();
        var currentRowArr=store.getAt(excluderow);
        var fields=store.fields;
        var len=store.getCount();
        for(var i=0;i<len;i++){
            if(excluderow==i)continue;
            var rec=store.getAt(i);
            if (rec.data.location != undefined &&  currentRowArr.data.location != undefined && rec.data.location == currentRowArr.data.location &&((rec.data.stocktype!=undefined&&rec.data.stocktype == currentRowArr.data.stocktype)||((rec.data.stocktype==undefined&&currentRowArr.data.stocktype==undefined)||(rec.data.stocktype==""&&currentRowArr.data.stocktype=="")))) {
                // If we have to select two quantities from same location & same warehouse then select in one row only rather than in two different rows
                if(rec.data.batch == ""){           //case when only batch is not present
                    if (rec.data.rack == currentRowArr.data.rack && rec.data.row == currentRowArr.data.row && rec.data.bin == currentRowArr.data.bin && rec.data.warehouse == currentRowArr.data.warehouse && rec.data.location == currentRowArr.data.location) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.duplicatelocationname")],2);
                        return false;
                    }  
                }
                if(rec.data.batch!=""){  //case when only batch is  present
                    if (rec.data.rack == currentRowArr.data.rack && rec.data.row == currentRowArr.data.row && rec.data.bin == currentRowArr.data.bin && rec.data.warehouse == currentRowArr.data.warehouse && rec.data.location == currentRowArr.data.location && rec.data.batch == currentRowArr.data.batch) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.duplicatelocationname")],2);
                        return false;
                    } 
                }
               
            }
        }
        return true;
    },
    checkduplicateWarehouse:function(testValue,excluderow){
        var store=this.grid.getStore();
        var currentRowArr=store.getAt(excluderow);
        var fields=store.fields;
        var len=store.getCount();
        for(var i=0;i<len;i++){
            if(excluderow==i)continue;
            var rec=store.getAt(i);
            if (rec.data.warehouse != undefined &&  currentRowArr.data.warehouse != undefined && rec.data.warehouse == currentRowArr.data.warehouse && rec.data.batch == "" && rec.data.serialno == "" && rec.data.location == "") {
                // If we have to select two quantities from same warehouse then select in one row only rather than in two different rows
                if (rec.data.rack == currentRowArr.data.rack && rec.data.row == currentRowArr.data.row && rec.data.bin == currentRowArr.data.bin && rec.data.warehouse == currentRowArr.data.warehouse ) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.duplicatewarehousename")],2);
                    return false;
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
            if(obj.field=="reusablecount"){
                return;
            } 
            if(obj.field=="warehouse") 
            {
                if(obj.value!=obj.originalValue){ //if warehouse is changed then clear all the detail
                    obj.record.set("batch","");
                    if(this.assetId != undefined && this.assetId != ""){
                        obj.record.set('serialno',this.assetId)
                    }else{
                        obj.record.set('serialno','')
                    }
                    obj.record.set('expstart','')
                    obj.record.set('expend','')
                    obj.record.set('purchasebatchid','')
                }
                if(this.purchasebatchid != "" && this.purchasebatchid != undefined&&this.isConsignReturn&&this.linkflag&&!this.isBatchForProduct) {
                    obj.record.set('purchasebatchid',this.purchasebatchid);
                }
                      
            }
            if(obj.field=="location") 
            {
                if(obj.value!=obj.originalValue){ //if warehouse is changed then clear all the detail                   
                    obj.record.set('purchasebatchid','')
                }
                if(this.purchasebatchid != "" && this.purchasebatchid != undefined&&this.isConsignReturn&&this.linkflag&&!this.isBatchForProduct) {
                    obj.record.set('purchasebatchid',this.purchasebatchid);
                }
            }
            var checkDuplicate=!this.isEdit ?true :false; 
            /* In Copy case, value for this.isEdit is true. We have to check duplicate serial number in copy case*/
            if (!checkDuplicate && this.copyTrans) {
                checkDuplicate = true;
            }
            if(obj.field=="serialno"){   //check serial name with comma
                var comma = "," ;
                if ((obj.value).match(comma)) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batchserialwindow.enterSerialNameWithoutComma")], 0);
                    rec.set("serialno", "");
                    return false;
                }
            }
            if(obj.field=="serialno" && !this.fromProduct){  //if from product opening at that time dont check for duplicate
                obj.record.set("serialno", obj.record.get("serialno"));
                /* In Copy case, value for this.isEdit is true. We have to check duplicate serial number in copy case*/
                if (!this.copyTrans &&this.isEdit && this.isChangeInExistingData(obj)){
                    checkDuplicate=true;
                }
                if(!this.isBatchForProduct){
                  
                    if(checkDuplicate){
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
                        if(index>=0){
                            WtfComMsgBox(110,2);
                            obj.record.set("serialno","");
                            obj.record.set("expstart", "");
                            obj.record.set("expend","");
                        }
                    }
                }else {
                    if (this.parentObj != undefined && this.parentObj != "" && (checkDuplicate) && this.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId && this.moduleid!=Wtf.Acc_Cash_Purchase_ModuleId && this.moduleid!=Wtf.Acc_Goods_Receipt_ModuleId) {
                        var parentGridStore = this.parentObj.Grid.store;
                        if (parentGridStore.data.length > 0) {
                            var batchrecords = "";
                            var currentSerialName = obj.record.get("serialno").trim().toLowerCase();
                            //                            var currentUniqueKey = obj.record.get("productid") + obj.record.get("batch").trim() + currentSerialName;
                            var currentUniqueKey = obj.record.get("productid") +  currentSerialName;
                            var occurenceCount = 0;

                            for (var x = 0; x < parentGridStore.data.length; x++) {
                                var batchDetails=parentGridStore.data.items[x].get("batchdetails");
                                if (batchDetails != undefined && batchDetails.length > 0) {
                                    batchrecords = eval('(' + batchDetails + ')');
                                }
                                if (batchrecords.length > 0) {
                                    for (var i = 0; i < batchrecords.length; i++) {
                                        //                                        var filledUniqueKey = batchrecords[i].productid + batchrecords[i].batch.trim() + batchrecords[i].serialno.trim().toLowerCase();
                                        var filledUniqueKey = batchrecords[i].productid + batchrecords[i].serialno.trim().toLowerCase();
                                        if (filledUniqueKey == currentUniqueKey) {
                                            occurenceCount++;
                                    }
                                    }
                                    if (occurenceCount > 1) {
                                        WtfComMsgBox(110, 2);
                                        obj.record.set("serialno", "");
                                        obj.record.set("expstart", "");
                                        obj.record.set("expend", "");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
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
            //                if(index>=0){
            //                    WtfComMsgBox(110,2);
            //                    obj.record.set("serialno","");
            //                    obj.record.set("expstart", "");
            //                    obj.record.set("expend","");
            //                }
                 
            }
            if(rowindex==total-1 && !this.isFixedAsset){
                this.addNewRowCheck(obj)
            }
            
            if (obj.field == "wastageQuantityType") {
                rec.set("wastageQuantity", 0);
                return;
            } else if (obj.field == "wastageQuantity") {
                if (rec.data.wastageQuantityType == 1 && rec.data.wastageQuantity > 100) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.wtg.quant.greater.than.100")], 2);
                    rec.set("wastageQuantity", obj.originalValue);
                    return;
                } else if (rec.data.wastageQuantityType == 0 && rec.data.wastageQuantity > rec.data.quantity) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.wtg.quant.greater.than.inv")], 2);
                    rec.set("wastageQuantity", obj.originalValue);
                    return;
                }
            }
      
        }
            this.resetGridRow(obj);
    },
    checkDuplicateSKUName: function (obj) {   //Function to check duplicate SKU name present in system
        if (obj != null && this.isSerialForProduct && this.isSKUForProduct) {
            
            if (obj.field == "skufield" && !this.fromProduct) {  //if from product opening at that time dont check for duplicate
                obj.record.set("skufield", obj.record.get("skufield"));
                
                //this condition is to check in case of same product multiple time in any IN transaction
                if (this.parentObj != undefined && this.parentObj != "") {
                    var parentGridStore = this.parentObj.Grid.store;
                    if (parentGridStore.data.length > 0) {
                        var batchrecords = "";
                        var currentSKUName = obj.record.get("skufield").trim().toLowerCase();
                        //                            var currentUniqueKey = obj.record.get("productid") + obj.record.get("batch").trim() + currentSerialName;
//                        var currentUniqueKey = obj.record.get("productid") + currentSKUName;
                        var currentUniqueKey = currentSKUName;
                        var occurenceCount = 0;

                        for (var x = 0; x < parentGridStore.data.length; x++) {
                            var batchDetails = parentGridStore.data.items[x].get("batchdetails");
                            if (batchDetails != undefined && batchDetails.length > 0) {
                                batchrecords = eval('(' + batchDetails + ')');
                            }
                            if (batchrecords.length > 0) {
                                for (var i = 0; i < batchrecords.length; i++) {
//                                    var filledUniqueKey = batchrecords[i].productid + batchrecords[i].skufield.trim().toLowerCase();
                                    var filledUniqueKey = batchrecords[i].skufield.trim().toLowerCase();
                                    if (filledUniqueKey == currentUniqueKey) {
                                        occurenceCount++;
                                    }
                                }
                                if (occurenceCount > 1) {
                                    WtfComMsgBox(["Alert",currentSKUName+" already added."],3);
                                    obj.record.set("skufield", "");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
    //        this.resetGridRow(obj);
    },
    deleteRowonChangeQty:function(obj){
        var quantity=obj.record.data.quantity;
        if (this.isSerialForProduct == true) {
            var qtyToCheckFractions = obj.record.data.quantity;
            qtyToCheckFractions = String(qtyToCheckFractions);
            var qtyarray = qtyToCheckFractions.split('.');
            quantity= obj.originalValue;
            var fraction = qtyarray[1];
            if (fraction != undefined && fraction.length > 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                obj.record.set("quantity", obj.originalValue);
            }
        }
        var store=obj.grid.getStore();
        for(var i=store.getCount();i>obj.row+1;i--){
            var record = store.getAt(obj.row+1);
            if(record!=undefined)
                store.remove(record);
        }
        
        var remaningQty=0;
        this.store.each(function(storeRecord){
            if((storeRecord.data.quantity != undefined && storeRecord.data.quantity != "")){
                if(getRoundofValue((remaningQty + getRoundofValue(storeRecord.data.quantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL))  > this.quantity){
                    if(!this.isSerialForProduct){
                        obj.record.set('quantity',parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                    }else{
                        obj.record.set('quantity',parseInt(this.quantity-remaningQty));
                    }
                }
                remaningQty = getRoundofValue(remaningQty + getRoundofValueWithValues(storeRecord.data.quantity,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
            }
        },this);
        quantity=obj.record.data.quantity;
        var readOnl=false;
        if(this.quantity<=remaningQty){
            readOnl=true;
        }
        for(var i=0;i<quantity;i++){
            this.addGridRec(undefined,true,((i!=(quantity-1))||readOnl));
        }
     
    },
    addNewRowCheck:function(obj){  
        var batch=obj.record.data.batch;
        var location=obj.record.data.location;
        var warehouse=obj.record.data.warehouse;
        var serialno=obj.record.data.serialno;
        var quantity=obj.record.data.quantity;
      
        if(this.isLocationForProduct)
        {
            if(location ==""){
                return;
            }
        }
        if(this.isWarehouseForProduct)
        {
            if(warehouse ==""){
                return;
            }
        }
        if(this.isBatchForProduct)
        {
            if(batch ==""){
                return;
            }
        }
        if(this.isSerialForProduct)
        {
            if(serialno ==""){
                return;
            }
        }
        
        this.addGridRec(undefined,true,false);
        var remaningQty=0;
        this.lastRecord=this.store.getAt(this.store.getCount() -1);
        this.store.each(function(storeRecord){
            if((storeRecord.data.quantity != undefined && storeRecord.data.quantity != "") && (( this.isBatchForProduct || ( this.lastRecord != storeRecord)))){
                if(getRoundofValue((remaningQty + getRoundofValue(storeRecord.data.quantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL))  > this.quantity){
                    if(!this.isSerialForProduct){
                        obj.record.set('quantity',parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                    }else{
                        obj.record.set('quantity',parseInt(this.quantity-remaningQty));
                    }
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")],2);
                    return false;
                }
                remaningQty = getRoundofValue(remaningQty + getRoundofValueWithValues(storeRecord.data.quantity,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
            }
        },this);
        if(!this.isSerialForProduct){
            this.lastRecord.set('quantity',parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
        }else{
            this.lastRecord.set('quantity',parseInt(this.quantity-remaningQty));
        }
                            
                                
    },
    checkDuplicateBatchlName:function(obj){   //Function to check duplicate Batch Name present in system[mayur p]
        if(obj!=null){
            if(Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch && obj.field== "batch"){ //check wheather batch already exist
                var batch= obj.record.get("batch");
                obj.record.set("batch","");
                Wtf.Ajax.requestEx({
                    url:"INVStockLevel/isBatchExists.do",
                    params: {
                        productid:this.productid,
                        batch : batch,
                        isEdit:this.isEdit,
                        linkflag:this.linkflag,
                        documentid:(this.isEdit  || (this.linkflag && this.isConsignment))?this.documentid:"",
                        moduleid:this.moduleid,
                        copyTrans:this.copyTrans,
                        billid:this.billid
                    }
                },
                    this,
                    function(result, req){

                        var msg=result.msg;
                        var title="Error";
                        if(result.success){
                                
                            var isBatchExists=result.data.isBatchPresent;
                                
                            if(isBatchExists == true){
                                title="Warning";
                                WtfComMsgBox([title,WtfGlobal.getLocaleText("acc.batch.duplicatebatchname")],0);
                                obj.record.set("batch","");
                                return false;
                            }else{
                                obj.record.set("batch",batch);
                            }
                                
                        }
                        else if(result.success==false){
                            title="Error";
                            WtfComMsgBox([title,"Some Error occurred."],0);
                            obj.record.set("batch","");
                            return false;
                        }
                    },
                    function(result, req){
                        WtfComMsgBox(["Failure", "Some Error occurred."],3);
                        obj.record.set("batch","");
                        return false;
                    });
                }    
            

            //execute this for only both batchserial products
            if (this.isBatchForProduct && this.isSerialForProduct)  
            {   //check which field was edited whether quantity or expdate depending on that copy expdates in other fields below
                if (obj.field == "quantity" || obj.field== "expdate" || obj.field == "warehouse") {
                    
                    var editRowNo = obj.row;      // get the current Row number that is being edited
                    var store = obj.grid.getStore();         
                    var batchQty = obj.record.data['quantity'];
                    var record = this.grid.store.getAt(editRowNo); //copy dates only for records below the set quantity field hence check for null quantity
                    if (batchQty != undefined && batchQty > 1)
                    {
                        for (var i = 1; i < batchQty; i++) {
                            var nextRec = store.getAt(editRowNo + i);
                            if (nextRec != undefined)
                            {
                                //set the next record expdates depending on the quantity 
                               nextRec.set('expdate', record.data.expdate);
                            }
                        }
                    }
                }
            }
            if(obj.field=="batch" && (this.fromProduct != undefined && !this.fromProduct)){
                obj.record.set("batch", obj.record.get("batch"));
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
            
            if(obj.field=="serialno"){
                
                var batchName = "";
                if (this.isBatchForProduct && obj.record.get("batch") == "" || obj.record.get("batch") == undefined) {
                    var store = obj.grid.getStore();
                    for (var i = obj.row; i >= 0 ; i--) {
                        var record = store.getAt(i);
                        if (record != undefined) {
                            if (record.data.batch != undefined && record.data.batch != "") {
                                batchName = record.data.batch;
                                break;
                            }
                        }

                    }
                }else{
                    batchName = obj.record.data.batch;
                }
                
                var checkDuplicate=!this.isEdit ?true :false; 
                /* In Copy case, value for this.isEdit is true. We have to check duplicate serial number in copy case*/
                if (!checkDuplicate && this.copyTrans) {
                    checkDuplicate = true;
                }
                if (!this.copyTrans && this.isEdit && this.isChangeInExistingData(obj)) {
                    checkDuplicate = true;
                }
                obj.record.set("serialno", obj.record.get("serialno").trim());
                if(this.productid != undefined && this.productid != null && this.productid != "" && batchName!="" && obj.record.get("serialno")!="" && checkDuplicate){
                    Wtf.Ajax.requestEx({
                        url:"ACCMaster/getNewSerials.do",
                        params:{    
                            checkbatchname:batchName,
                            checkserialname:obj.record.get("serialno"),
                            productid:this.productid,
                            documentid:this.documentid,
                            isEdit:this.isEdit,
                            duplicatecheck:checkDuplicate
                        }
                    },this,function(res,req){
                        if(res.data.length!=0){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.528")], 2);
                            obj.record.set("serialno","");
                            return false;
                        }

                    },function(res,req){
                        obj.record.set("serialno","");
                        return false;
                    });
                }
            }
            
            if(this.isSKUForProduct && obj.field=="skufield"){
                obj.record.set("skufield", obj.record.get("skufield"));
                var batchName = "";
                if (this.isBatchForProduct && obj.record.get("batch") == "" || obj.record.get("batch") == undefined) {
                    var store = obj.grid.getStore();
                    for (var i = obj.row; i >= 0 ; i--) {
                        var record = store.getAt(i);
                        if (record != undefined) {
                            if (record.data.batch != undefined && record.data.batch != "") {
                                batchName = record.data.batch;
                                break;
                            }
                        }
                    }
                }else{
                    batchName = obj.record.data.batch;
                }
                
                var currentSKUName = obj.record.get("skufield").trim().toLowerCase();
                var currentUniqueKey = this.productid + currentSKUName;
                var occurenceCount = 0;
                var store = obj.grid.getStore();
                for (var x = 0; x < store.data.length; x++) {
                    var record = store.getAt(x);
                    var filledUniqueKey = this.productid + record.data.skufield.trim().toLowerCase();
                    if (filledUniqueKey == currentUniqueKey) {
                        occurenceCount++;
                    }
                    if (occurenceCount > 1) {
                        WtfComMsgBox(["Alert", currentSKUName + " already added."], 3);
                        obj.record.set("skufield", "");
                        return false;
                    }
                }
                
                var SKUDisplayName=(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku");
                if(this.productid != undefined && this.productid != null && this.productid != ""){
                   
                    Wtf.Ajax.requestEx({
                        url:"INVStockLevel/isSKUExists.do",
                        params: {
                            productid:this.productid,
                            batch : batchName,
                            sku :obj.record.get("skufield")
                        }
                    },
                    this,
                    function(result, req){

                        var msg=result.msg;
                        var title="Error";
                        if(result.success){
                                
                            var isSKUExists=result.data.isSKUPresent;
                                
                            if(isSKUExists == true){
                                title="Warning";
                                WtfComMsgBox([title,"The "+SKUDisplayName+" you entered  : <b>"+obj.record.get("skufield")+"</b> already exists."],0);
                                obj.record.set("skufield","");
                                return false;
                            }
                                
                        }
                        else if(result.success==false){
                            title="Error";
                            WtfComMsgBox([title,"Some Error occurred."],0);
                            obj.record.set("skufield","");
                            return false;
                        }
                    },
                    function(result, req){
                        WtfComMsgBox(["Failure", "Some Error occurred."],3);
                        obj.record.set("skufield","");
                        return false;
                    });
                    
                    
                }
            }
            
            if(obj.field=="location" || obj.field=="batch"){
                if((this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct || this.isRackForProduct || this.isRowForProduct || this.isBinForProduct) && this.isSerialForProduct && !this.isFixedAsset ){
                    this.deleteRowonChangeQty(obj);
                }
            }
            if(obj.field=="quantity"){
                var quantity=obj.record.get('quantity');
                var remaningQty=0, isUpdatelastRow=true;
                this.lastRecord=this.store.getAt(this.store.getCount() -1);
                var modulecondition = this.checkModuleIdisPresent(this.moduleid);
                this.store.each(function(storeRecord){
                    if(this.lastRecord != storeRecord && (storeRecord.data.quantity != undefined && storeRecord.data.quantity != "")){
                        if (!CompanyPreferenceChecks.differentUOM() ||  (CompanyPreferenceChecks.differentUOM() && ((!modulecondition || (modulecondition && (!Wtf.isEmpty(this.allowUserToEditQuantity) && !this.allowUserToEditQuantity))) || (this.productUomid==this.selectedProductUomid) || (this.isOnlyBatchForProduct!=undefined && !this.isOnlyBatchForProduct)))) {             //Checking wether the current module belongs to the module in which this functionality is implemented and "Variable Purchase/Sales UOM conversion rate" check is enabled in company preferences and the product is of type batch. ERM-319
                            if(getRoundofValue((remaningQty + getRoundofValue(storeRecord.data.quantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)) > this.quantity){
                                 if(!this.isSerialForProduct){
                                    if(obj.record !=  storeRecord && !this.isEdit){
                                        obj.record.set('quantity',parseFloat(this.quantity-(storeRecord.data.quantity * 1)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                                        remaningQty=this.quantity;
                                    }else{
                                         if(this.isEdit){
                                             obj.record.set('quantity',obj.originalValue);
                                             isUpdatelastRow=false;
                                         }else{
                                             obj.record.set('quantity',parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                                             remaningQty=this.quantity;
                                         }
                                    } 
                                }else{
                                    obj.record.set('quantity',parseInt(this.quantity-remaningQty));
                                }
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")],2);
                                return false;
                            }
                        }
                        remaningQty = getRoundofValue(remaningQty + getRoundofValueWithValues(storeRecord.data.quantity,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                    }
                },this);
                
                //If entered quantity is grater than main quantity
                if(this.isSerialForProduct && quantity > this.quantity){
                     WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.inventorysetup.warning.quantity.equal")],2);
                     if(!this.isSerialForProduct){
                        obj.record.set('quantity',parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                    }else{
                        obj.record.set('quantity',parseInt(this.quantity-remaningQty));
                    }
                     return false;
                }
                
                //var value= value = parseFloat(this.quantity-remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL); 
                 /**
                  * Checking wether the current module belongs to the module in which this functionality is implemented and "Variable UOM" check is enabled in company preferences and the product is of type batch. 
                  * if yes then updating the last row so because of this feature if user enters more quantity than the quantity in grid than it displays negative value in quantity column of BatchSerial window
                  * ERM-319
                  * 
                  * @type WtfSerialNoWindowAnonym$4.checkDuplicateBatchlName@call;checkModuleIdisPresent|WtfSerialNoWindowAnonym$4.checkDuplicateBatchlName@call;checkModuleIdisPresent
                  */
                var modulecondition = this.checkModuleIdisPresent(this.moduleid);
//                if (!CompanyPreferenceChecks.differentUOM() ||  (CompanyPreferenceChecks.differentUOM() && ((!modulecondition || (modulecondition && (!Wtf.isEmpty(this.allowUserToEditQuantity) && !this.allowUserToEditQuantity))) || (this.productUomid==this.selectedProductUomid) || (this.isOnlyBatchForProduct!=undefined && !this.isOnlyBatchForProduct)))) { 
                if (!CompanyPreferenceChecks.differentUOM() || (CompanyPreferenceChecks.differentUOM() && !modulecondition)) {
                    if (isUpdatelastRow && !this.isSerialForProduct) {
                        this.lastRecord.set('quantity', parseFloat(this.quantity - remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                    } else if (isUpdatelastRow) {
                        this.lastRecord.set('quantity', parseInt(this.quantity - remaningQty));
                    }
                } else if (CompanyPreferenceChecks.differentUOM() && modulecondition && this.isOnlyBatchForProduct!=undefined && this.isOnlyBatchForProduct) {         
                    if (isUpdatelastRow && !this.isSerialForProduct && parseFloat(this.quantity - remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) > 0.0) {
                        this.lastRecord.set('quantity', parseFloat(this.quantity - remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
                    } else if (isUpdatelastRow && parseFloat(this.quantity - remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) > 0.0) {
                        this.lastRecord.set('quantity', parseInt(this.quantity - remaningQty));
                    } else if (parseFloat(this.quantity - remaningQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) < 0.0) {
                        this.lastRecord.set('quantity', parseInt(0));
                    }
                }
            }
        }
    },
    /**
     * Checking wether the current module belongs to the module in which this functionality is implemented.If yes then setting modulecondition=true ERM-319
     * @type Boolean|Boolean
     */
    checkModuleIdisPresent:function(moduleid){
        var modulecondition = false;
        if (CompanyPreferenceChecks.differentUOM()) {
            var moduleArray = [Wtf.Acc_Sales_Order_ModuleId, Wtf.Acc_Purchase_Order_ModuleId, Wtf.Acc_Goods_Receipt_ModuleId, Wtf.Acc_Sales_Return_ModuleId, Wtf.Acc_Purchase_Return_ModuleId, Wtf.Acc_Delivery_Order_ModuleId, Wtf.Acc_Invoice_ModuleId, Wtf.Acc_Vendor_Invoice_ModuleId,Wtf.Acc_Cash_Purchase_ModuleId,Wtf.Acc_Cash_Sales_ModuleId];
            if (moduleArray.indexOf(moduleid) != -1) {
                modulecondition = true;
            }
        }
        return modulecondition;
    },
    isChangeInExistingData: function(obj) {
        var isChangedData = false;
        if (this.parentObj != undefined && this.parentObj != "") {
            var parentGridStore = this.parentObj.Grid.store;
            if (parentGridStore.data.length > 0) {
                var currentSerialName = obj.record.get("serialno").trim().toLowerCase();
                var currentProductID = obj.record.get("productid");
                /* Get the serial ID */ 
                var currentSerialNoId = obj.record.get("serialnoid"); 
                var existingSerialName = "";
                var existingProductID = "";
                var existingSerialNoId  = "";
                for (var x = 0; x < parentGridStore.data.length; x++) {
                    var batchrecords = "";
                    var batchDetails = parentGridStore.data.items[x].get("batchdetails");
                    if (batchDetails != undefined && batchDetails.length > 0) {
                        batchrecords = eval('(' + batchDetails + ')');
                    }
                    if (batchrecords.length > 0) {
                        var sameSerialFoundInBatchDetails = false;
                        for (var i = 0; i < batchrecords.length; i++) {
                            existingSerialName = batchrecords[i].serialno.trim().toLowerCase();
                            existingProductID = batchrecords[i].productid;
                            existingSerialNoId = batchrecords[i].serialnoid;
                            /* Check if the serial number is present in current record, except current serial ID */
                            if (existingSerialNoId !== currentSerialNoId) {
                                if ((obj.record.data.documentid !== undefined && obj.record.data.documentid !== "" && batchrecords[i].documentid !== undefined && batchrecords[i].documentid !== "") && currentSerialName === existingSerialName && obj.record.data.documentid === batchrecords[i].documentid) {
                                    isChangedData = true;
                                } else if (obj.record.data.documentid === undefined || obj.record.data.documentid === "" || batchrecords[i].documentid === undefined || batchrecords[i].documentid === "") {
                                    isChangedData = true;
                                }
                            }
                            /* Check if the serial name is changed or not, if serial name associated with Id is present in batch details then data is not changed */
                            if (existingSerialNoId === currentSerialNoId && currentSerialName === existingSerialName && existingProductID === currentProductID) {
                                sameSerialFoundInBatchDetails = true;
                            }
                        }
                        /* If user has entered different serial name which is not existing in current batch details then there is change in data.*/
                        if (!sameSerialFoundInBatchDetails && !isChangedData && existingProductID === currentProductID) {
                            isChangedData = true;
                        }
                    }
                }
            }
        }
        return isChangedData;
    },
    /**
     * to adjust balance quantity if consumed on currently on UI side for MRP WO consumption
     */
    adjustAvailableQuantityForMultipleProducts: function (productid) {
        var records = this.parentGridRecords;
        if (records) {
            for (var i = 0; i < records.length; i++) {
                /**
                 * take quantity of batchdetails only if product matches
                 */
                if (records[i].get("productid") == productid) {
                    var batchdetails = records[i].get("batchdetails");
                    if (batchdetails && batchdetails !== '[]') {
                        var batchDetailsArr = eval(batchdetails);
                        for (var element = 0; element < batchDetailsArr.length; element++) {
                            var value = batchDetailsArr[element].quantity;
                            this.AvailableQuantity -= parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                        }
                    }
                }
            }
        }
    },
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
        if (obj != null && obj.field == "quantity") //if quantity field is being edited then creating batchjson to populate available quantity properly
        {
            var filterJson = '[';
            filterJson += '{"location":"' + obj.record.get("location") + '","warehouse":"' + obj.record.get("warehouse") + '","productid":"' + obj.record.get("productid") + '","documentid":"' + obj.record.get("documentid") + '","purchasebatchid":"' + obj.record.get("purchasebatchid") + '"},';
            filterJson = filterJson.substring(0, filterJson.length - 1);
            filterJson += "]";
            batchJson = filterJson;
        }
        if(this.isSales && islocationavailble && iswarehouseavailble && isBatchavailble && isRackavailble && isRowavailble && isBinavailble && !this.isFixedAsset && !this.isLeaseFixedAsset && !this.isSerialForProduct){
            Wtf.Ajax.requestEx({
                url: "ACCInvoice/getBatchRemainingQuantity.do",
                params: {
                    batchdetails:(batchJson!=[] &&batchJson!= "" && batchJson!=undefined)?batchJson:this.getBatchDetails(),
                    transType:this.moduleid,
                    isEdit:this.isEdit,
                    linkflag:this.linkflag,
                    producttype: this.productType,           //Added producttype, batchname and bomid in request for Job work assembly type product
                    batch : obj.record.get("batch"),
                    bomid : this.bomid
                }
            },this,function(res,req){
                this.AvailableQuantity=res.quantity;
                if (Wtf.account.companyAccountPref.activateMRPManagementFlag) {
                    this.adjustAvailableQuantityForMultipleProducts(this.productid);
                }
                obj.record.set("avlquantity",this.AvailableQuantity);
                return false
            });
        }
    },
    getBatchDetails:function(){
        this.store.each(function(rec){
            if(rec.data.rowid==undefined){
                rec.data.rowid='';
                    
            }
        },this);
        this.store.each(function(rec){
            if((rec.data.documentid==undefined||rec.data.documentid=="")&&this.moduleid==53){
                rec.data.documentid=this.documentid;
            }
        },this);
        var arr=[];
        var inculelast=false;
        if(this.isFixedAsset || (this.isfromProductAssembly && this.includeLastRowInProdAssembly))
        {
            inculelast=true;
            this.includeLastRowInProdAssembly=false;
        }
        this.store.each(function(rec){
           
            if (rec.data.serialno != "") {
                rec.data[CUSTOM_FIELD_KEY] = Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.module).substring(13));
            }
            // var transdate=(this.isEdit?WtfGlobal.convertToGenericDate(this.record.data.date):WtfGlobal.convertToGenericDate(new Date()));
            arr.push(this.store.indexOf(rec));
        }, this);
        var jarray=WtfGlobal.getJSONArray(this.grid,inculelast,arr);      
        return jarray;
    },
    
    closeWin:function(){ /*this.fireEvent('update',this,this.value);*/
        this.close();
    },
    checkLoadMask:function(){
        if(this.totalStoreCount==this.loadedStoreCount){
            this.loadMask.hide();
        }
    },
    getGridConfig: function () {
        WtfGlobal.getGridConfig(this.grid, Wtf.Serial_No_Window_Grid_Id + "_" + this.moduleid, false, false);
    },
    saveGridStateHandler: function (grid, state) {
        if (state && state.columns) {
            for (var i = 0; i < state.columns.length; i++) {
                delete state.columns[i].hidden;
            }
        }
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Serial_No_Window_Grid_Id + "_" + this.moduleid, grid.gridConfigId, false);
    }
});  

/*************************************************************************
 *   
 *           Adjust Recycle Quantity Window
 * 
 *************************************************************************/

Wtf.account.RecycleQuanity = function(config) {
    this.title=config.title;
    this.productName=config.productName;
    this.inventoryquantity=config.inventoryquantity==undefined?0:config.inventoryquantity;
    this.grid=config.grid,
    this.productRecycleQuantity=config.recycleQuantity,
    this.rowindex=config.rowindex,
    this.record=config.record;
    this.okBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.submit")  //'Send'
    });
    this.okBtn.on('click', this.handleSend, this);
    this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close")  //'Close'
    });
    this.closeBtn.on('click', this.handleClose, this);

    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.field.AdjustRecycleQuantity"), //"Send Mail",
        buttons: [this.okBtn, this.closeBtn]
    }, config);

    Wtf.account.RecycleQuanity.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.RecycleQuanity, Wtf.Window, {
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 1);
    },
    onRender: function(config) {
        this.createForm();
        var msg="<b>Product</b> : "+this.productName+"<br>     <b>Actual Quantity</b> : "+this.inventoryquantity
        //var msg="<b>"+WtfGlobal.getLocaleText("acc.product.gridProduct")+"</b> : "+this.productName+"<br> <b>"//+WtfGlobal.getLocaleText("acc.product.gridQty")+"</b> : "+this.quantity
        var isgrid=false;
        this.add({
            region: 'center',
            border: false,
            baseCls: 'bckgroundcolor',
            items: this.Form
        });

        this.add({
            region: 'north',
            height: 85,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            //html: getTopHtml(WtfGlobal.getLocaleText("acc.field.AccountsRe-evaluation"), WtfGlobal.getLocaleText("acc.field.SelectDetails"), "../../images/accounting_image/Chart-of-Accounts.gif")
            html: getTopHtml(this.title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        }, this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
            //            layout: 'border',
            items: [this.Form]
        }));
        Wtf.account.RecycleQuanity.superclass.onRender.call(this, config);
    },
    createForm: function() {
        this.inventoryQuantity=new Wtf.form.NumberField({
            allowNegative:false,
            defaultValue:0,
            allowBlank:false,
            maxLength: 10,
            width:200,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            fieldLabel:WtfGlobal.getLocaleText("acc.product.inventory"),  //'Inventory',
            name:'inventoryquantiy',
            id:"inventoryquantiy"+this.heplmodeid+this.id
        //            listeners:{
        //                'change':{
        //                    fn:this.updateSubtotal,
        //                    scope:this
        //                }
        //            }
        });
        this.inventoryQuantity.setValue(this.inventoryquantity);
        this.recyleQuantity=new Wtf.form.NumberField({
            allowNegative:false,
            //            hidden:true,
            defaultValue:0,
            //            hideLabel:true,
            allowBlank:true,
            maxLength: 10,
            width:200,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            fieldLabel:WtfGlobal.getLocaleText("acc.product.RecycleQuantity"),  //'Recycle Quantity',
            name:'recylequantity',
            id:"recylequantity"+this.heplmodeid+this.id,
            listeners:{
                'change':{
                    fn:this.checkLimit,
                    scope:this
                }
            }
        });
        this.availableRecyclableQuantity = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.build.availabeRecyclableQuantity"),  //'Available Recycle Quantity'
            name: 'availablerecyclablequantity',
            cls:"clearStyle",
            readOnly:true
        });
        this.remainingRecycleQuantity=new Wtf.form.NumberField({
            allowNegative:false,
            //            hidden:true,
            defaultValue:0,
            //            hideLabel:true,
            allowBlank:true,
            maxLength: 10,
            width:200,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            fieldLabel:WtfGlobal.getLocaleText("acc.product.RemainingQuantity"),  //'Remaining Quantity',
            name:'remainingquantity',
            id:"remainingquantity"+this.heplmodeid+this.id
        //            listeners:{
        //                'change':{
        //                    fn:this.checkLimit,
        //                    scope:this
        //                }
        //            }
        });
        var inventoryQty=this.record.data.inventoryquantiy==""||this.record.data.inventoryquantiy==undefined?0:this.record.data.inventoryquantiy;
        var recycleQty=this.record.data.recylequantity==""||this.record.data.recylequantity==undefined?0:this.record.data.recylequantity;
        var remainingquantity=this.record.data.remainingquantity==""||this.record.data.remainingquantity==undefined?0:this.record.data.remainingquantity;
        var availablerecyclequantity=Wtf.isEmpty(this.record.data.availablerecylequantity)?0:this.record.data.availablerecylequantity;
        this.inventoryQuantity.setValue(inventoryQty);
        this.recyleQuantity.setValue(recycleQty);
        this.remainingRecycleQuantity.setValue(remainingquantity);
        this.availableRecyclableQuantity.setValue(availablerecyclequantity);
        
        this.recycleQuantity=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.product.inventoryQuantity"),//'Is a subproduct?',
            //            checkboxToggle: true,
            autoHeight: true,
            autoWidth: true,
            //            hidden:this.isFixedAsset,
            width: 380,
            checkboxName: 'subproduct',
            style: 'margin-right:30px',
            //            collapsed: true,
            items:[this.inventoryQuantity,this.recyleQuantity, this.availableRecyclableQuantity]
        });
        this.remaningQuantity=new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.product.remainingQuantity"),//'Is a subproduct?',
            //            checkboxToggle: true,
            autoHeight: true,
            autoWidth: true,
            //            hidden:this.isFixedAsset,
            width: 380,
            checkboxName: 'subproduct',
            style: 'margin-right:30px',
            //            collapsed: true,
            items:[this.remainingRecycleQuantity]
        });
        this.Form = new Wtf.form.FormPanel({
            height: 'auto',
            border: false,
            items: [{
                layout: 'form',
                bodyStyle: "background: transparent; padding: 20px;",
                //                labelWidth:60,
                border: false,
                items: [ this.recycleQuantity,this.remaningQuantity  // this.startdate, this.enddate
                ]
            }]
        });
    },
    handleClose: function() {
        this.fireEvent('cancel', this)
        this.close();
    },
    checkLimit: function(){
        if(this.recyleQuantity.getValue()>this.productRecycleQuantity){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.productList.gridRecycleQuantityLimit")], 2);
            this.recyleQuantity.setValue(0);
            return;
        }
    },   
    handleSend: function(a, b) {
        var valid=this.Form.getForm().isValid();
        if(valid==false){
            WtfComMsgBox(2, 2);
            return;
        }  
        
        var inventoryQuantity=this.inventoryQuantity.getValue();
        var recyleQuantity=this.recyleQuantity.getValue()==""||this.recyleQuantity.getValue()==undefined?0:this.recyleQuantity.getValue();
        var remainingRecycleQuantity=this.remainingRecycleQuantity.getValue()==""||this.remainingRecycleQuantity.getValue()==undefined?0:this.remainingRecycleQuantity.getValue();
        
        if((inventoryQuantity+recyleQuantity)<remainingRecycleQuantity){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.warningRemaningQuantity")], 2);
            return;
        }
        if((inventoryQuantity+recyleQuantity)>this.inventoryquantity){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.warningActualQuantiy")], 2);
            return;
        }
        
              
        var store=this.grid.getStore();
        var record = store.getAt(this.rowindex);
        record.set("inventoryquantiy",inventoryQuantity);
        record.set("recylequantity",recyleQuantity);
        record.set("remainingquantity",remainingRecycleQuantity);
          
        this.close();   

    },
    success: function(response) {
        if (response.success) {
            WtfComMsgBox([this.label, response.msg], 3);
            this.handleClose();
        } else {
            if (response.msg && response.isMsgSizeException) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
            } else {
                WtfComMsgBox([this.label, WtfGlobal.getLocaleText("acc.rem.210")], 3);
            }
            this.sendBtn.enable();
            this.closeBtn.enable();
        }
    },
    failure: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        this.handleClose();
    },
    mailSuccessResponse: function(response) {
        this.close();
    },
    mailFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        this.close();
    }

});

//**********************************************************************************************************
//                   Set Mass Warehouse/Location Component
//**********************************************************************************************************
Wtf.account.SetLocationwarehouseWindow = function(config) {
    this.label="Set Warehouse/Location";
    this.record=config.record;
    this.grid=config.grid;
    this.isCustomer=config.isCustomer;
    this.butnArr = new Array();
    this.isSubmitBtnClicked = false;  
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = true;
            
            this.submitRecords();
        }
    } , {
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = false;
            this.close();
        }
    });
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.SetLocationwarehouseWindow.superclass.constructor.call(this, config);  
}
Wtf.extend(Wtf.account.SetLocationwarehouseWindow, Wtf.Window, {
    height: 275,
    width: 550,
    modal: true,
    iconCls : getButtonIconCls(Wtf.etype.deskera),
    onRender: function(config) {
        Wtf.account.SetLocationwarehouseWindow.superclass.onRender.call(this, config);
        var title=WtfGlobal.getLocaleText("acc.SetWarehouseLocation");
        var msg=WtfGlobal.getLocaleText("acc.invsetup.msg.select.warehouse.loc");
        this.createForm();
        var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        }, {
            border: false,
            region: 'center',
            //            id: 'centerpan'+this.id,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.wrapperNorth
           
        });   
        
    },
    createForm:function(){
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
            fieldLabel:WtfGlobal.getLocaleText("acc.window.Warehouse")+"*",
            valueField:'id',
            displayField:'name',
            store:this.wareHouseStore,
            lastQuery:'',
            //                disabled: true,
            allowBlank:false,
            typeAhead: true,
            forceSelection: true,
            name:'warehouse',
            hiddenName:'warehouse',
            width: 250
        });

        this.wareHouseCombo.on('select',function(){
            this.locationStore.load({
                params:{
                    storeid:this.wareHouseCombo.getValue()
                }
            });
            this.locationCombo.enable();
        },this);

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
            baseParams:{isActive:true},  //ERP-40021 :To get only active Locations.
            reader:this.locationReader
        });
        this.locationStore.load();

        this.locationCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            multiSelect:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12")+"*",
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.locationStore,
            typeAhead: true,
            allowBlank: false,
            disabled:true,
            forceSelection: true,
            hirarchical:true,
            name:'location',
            hiddenName:'location',
            width: 250
        });     
        this.wrapperNorth = new Wtf.form.FormPanel({
            region :'center',
            height:120,
            //                autoHeight: true,
            autoWidth:true,
            border:false,
            //                disabledClass:"newtripcmbss",
            defaults:{
                border:false
            },
            style: "padding-left: 10px;padding-top: 35px;padding-right: 10px;",
            layout:'form',
            baseCls:'bckgroundcolor',
            items:[this.wareHouseCombo,this.locationCombo]
        });   
      
    },
    submitRecords: function(){
        var warehouse=this.wareHouseCombo.getValue();
        var location=this.locationCombo.getValue();
        if(!(this.wrapperNorth.getForm().isValid())){
            WtfComMsgBox(2, 2);
            return false;
        }
        this.recArr = this.grid.getSelectionModel().getSelections();
        for(var k=0;k< this.recArr.length;k++){
            var proRecord=this.recArr[k];
            if(proRecord.data.isLocationForProduct && proRecord.data.isWarehouseForProduct  && !proRecord.data.isSerialForProduct && !proRecord.data.isBatchForProduct){
                //                    var quantity=0;
                //                    if(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId){
                var  quantity=proRecord.data.baseuomquantity;
                //                    }else{
                //                         quantity=proRecord.data.dquantity;
                //                    }
                    
                var filterJson="";
                filterJson='[';
                filterJson+='{"location":"'+location+'","warehouse":"'+warehouse+'","productid":"'+proRecord.data.productid+'","documentid":"","purchasebatchid":"","row":"","rack":"","bin":"","quantity":"'+quantity+'"},';
                filterJson=filterJson.substring(0,filterJson.length-1);
                filterJson+="]";

                proRecord.set("batchdetails",filterJson);
                proRecord.set("isWarehouseLocationsetCopyCase",true);
            }
        }  
        this.close();
        
    }
    
});
  
//**********************************************************************************************************
//                   Choose Serial to Replace in Blocked Case
//**********************************************************************************************************


Wtf.SerialRepalceWindow = function(config) {
    this.label=WtfGlobal.getLocaleText("acc.replaceserialwin.title"); // "Select Serial to Replace";
    this.SerialReplaceArr=config.SerialReplaceArr;
    this.replacebatchdetails="";
    this.serialchangeCount=config.serialchangeCount,
    this.isCustomer=config.isCustomer;
    this.butnArr = new Array();
    this.isSubmitBtnClicked = false;  
    this.serialNumberCMArr=[];
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = true;
            
            this.submitRecords();
        }
    }
    //    , {
    //        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
    //        scope: this,
    //        handler: function() {
    //            this.isSubmitBtnClicked = false;
    //            this.close();
    //        }
    //    }
    );
    this.storeRec = Wtf.data.Record.create([
    {
        name:'serialnoid',
        defValue:null
    },

    {
        name:'productname'
    },

    {
        name:'itemid'
    },

    {
        name:'quantity'
    },

    {
        name:'quantitydue'
    },
    {
        name:'documentid'
    },

    {
        name:'purchasebatchid'
    },

    {
        name:'batchname'
    },

    {
        name:'ispurchase'
    },

    {
        name:'transactiontype'
    },

    {
        name:'lockquantity'
    },

    {
        name:'isForconsignment'
    },

    {
        name:'serialno'
    },

    {
        name: 'isLocationForProduct'
    },

    {
        name: 'isWarehouseForProduct'
    },

    {
        name: 'isBatchForProduct'
    },

    {
        name: 'isSerialForProduct'
    },

    {
        name: 'location'
    },

    {
        name: 'warehouse'
    },
    {
        name: 'stocktype'
    },
    {
        name: 'packwarehouse'
    },
    {
        name: 'packlocation'
    }
      
    ]);
    var url='';  
    this.Store = new Wtf.data.Store({
        url:url,
        reader: new Wtf.data.KwlJsonReader({
            },this.storeRec)
    });
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false
    }); 
    this.serialNumberCMArr.push(this.sm);   
    this.serialNumberCMArr.push({
        header:WtfGlobal.getLocaleText("acc.field.SerialNo"),
        dataIndex:'serialno'
    //            width:100
    });
    this.serialNumberCMArr.push(
    {
        header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
        dataIndex:'batchname'
    //            width:100
    });
    //        this.serialNumberCMArr.push({
    //            header:WtfGlobal.getLocaleText("acc.field.MfgDate"),
    //            dataIndex:'mfgdate',
    //            width:100,
    //            renderer:WtfGlobal.onlyDateDeletedRenderer
    //            
    //        });
    //        this.serialNumberCMArr.push({
    //            header:WtfGlobal.getLocaleText("acc.field.ExpDate"),
    //            dataIndex:'expdate',
    //            width:100,
    //            renderer:WtfGlobal.onlyDateDeletedRenderer
    //            
    //        });
        
        
    this.cm=new Wtf.grid.ColumnModel(this.serialNumberCMArr);
    this.replacegrid = new Wtf.grid.GridPanel({
        height:210,
        width:'97%',
        store: this.Store,
        cm: this.cm,
        sm:this.sm,
        layout:'fit',
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.getLocaleText("acc.common.norec")
        }
    });
        
    this.Store.loadData(this.SerialReplaceArr);
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.SerialRepalceWindow.superclass.constructor.call(this, config);  
    this.addEvents({
        'onsubmit':true
    });
}
Wtf.extend(Wtf.SerialRepalceWindow, Wtf.Window, {
    height: 275,
    width: 550,
    modal: true,
    iconCls : getButtonIconCls(Wtf.etype.deskera),
    onRender: function(config) {
        Wtf.SerialRepalceWindow.superclass.onRender.call(this, config);
        var title=WtfGlobal.getLocaleText("acc.replaceserialwin.title");
        var msg=WtfGlobal.getLocaleText("acc.invsetup.msg.select.serial");
        var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        }, {
            border: false,
            region: 'center',
            //            id: 'centerpan'+this.id,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.replacegrid
           
        });   
        
    },    
    submitRecords: function(){
        this.recArr = this.replacegrid.getSelectionModel().getSelections();
        var arr=[];
        if(this.serialchangeCount==this.recArr.length){
            for(var k=0;k< this.recArr.length;k++){
                arr.push(this.Store.indexOf(this.recArr[k]))
            }
            this.replacebatchdetails=WtfGlobal.getJSONArray(this.replacegrid,true,arr);
            this.fireEvent('onsubmit',this,this.replacebatchdetails);
            this.close();
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.warningreplacedQuantiy")], 2);
            return;
        }
    }
});
