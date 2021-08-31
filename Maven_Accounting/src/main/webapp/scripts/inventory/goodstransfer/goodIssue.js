/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.goodIssue = function (config){
    Wtf.apply(this,config);
    Wtf.goodIssue.superclass.constructor.call(this);
}

Wtf.extend(Wtf.goodIssue,Wtf.Panel,{
    onRender:function (config) {
        Wtf.goodIssue.superclass.onRender.call(this,config);
        this.getForm();
        this.getItemDetail(this.qarejected);
        
        this.isAutofill=false;
        this.zmainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.Form,
            this.ItemDetailGrid
            ],
            bbar:[this.submitBttn,this.cancelBttn,'-',this.savencreateBttn,'-',this.singleRowPrint]
        });

        this.add(this.zmainPanel);
        this.on("activate",function()
        {
            this.doLayout();
        },this);
        
        this.fromStore.on("load", function(ds, rec, o){
            if(rec.length > 0 && this.qarejected != true){
                this.fromstoreCombo.setValue(rec[0].data.store_id, true);
            }

        }, this);
    },

    getForm:function (){
        
        
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+"*",
            hiddenName : 'store',
            store : this.storeCmbStore,
            forceSelection:true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 125,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.je.Selectstore"),
            typeAhead:true
        });

        this.storeRec = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },

        {
            name: 'fullname'
        },

        {
            name: 'analysiscode'
        },

        {
            name: 'abbr'
        },
        {
            name: 'defaultlocationid'
        }
        ]);

        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        this.Store = new Wtf.data.Store({
            url:  'INVStore/getStoreList.do',
            baseParams:{
                isActive:true,
                isFromStockIssue:"true",
                storeTypes:'0,1,2'
            },
            reader:this.storeReader
        });
        this.Store.on("load", function(ds, rec, o){
            if(rec.length > 0){
            //                this.tostoreCombo.setValue(rec[0].data.store_id, true);
           
            }

        }, this);
        this.Store.load({
            //            params:{
            //                flag:14,
            //                issuetostore:"yes"
            //            }
            });
        this.fromStore = new Wtf.data.Store({
            url:  'INVStore/getStoreList.do',
            baseParams:{
                isActive:true,
                storeTypes:'0,1,2', // 0-Warehouse , 2-headqurter
                byStoreManager:"true",
                isFromStockIssue:"true",
                byStoreExecutive:"true"
            },
            reader:this.storeReader
        });
        this.fromStore.on("load", function(ds, rec, o){
            if(rec.length > 0){
            //                this.fromstoreCombo.setValue(rec[0].data.store_id, true);
           
            }

        }, this);
        this.fromStore.load({
            //            params:{
            //                flag:14,
            //                issue:"yes"
            //            }
            });
                        
        this.locCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },        

        {
            name: 'name'
        }]);

        this.locCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            baseParams:{
                configid:60,//here we are taking all locations so passing location id
                flag:25
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
          
        this.locCmbStore.on("beforeload", function(){
            this.locCmbStore.removeAll();
            this.fromLocCmb.reset(); 
        }, this);

        this.tolocCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            baseParams:{
                configid:60,//here we are taking all locations so passing location id
                flag:25
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
        this.tolocCmbStore.on("beforeload", function(ds, rec, o){
            this.tolocCmbStore.removeAll();
            this.toLocCmb.reset(); 
        }, this);
        //            this.locCmbStore.load();

        this.fromLocCmb = new Wtf.form.ComboBox({
            fieldLabel : WtfGlobal.getLocaleText("acc.je.FromLocation*"),
            hiddenName : 'fromlocationid',
            store : this.locCmbStore,
            typeAhead:true,
            //hideLabel:true,
            //hidden:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 150,
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.je.Selectlocation"),
            allowBlank:false
        });

        this.toLocCmb = new Wtf.form.ComboBox({
            fieldLabel : WtfGlobal.getLocaleText("acc.je.ToLocation*"),
            hiddenName : 'tolocationid',
            store : this.tolocCmbStore,
            typeAhead:true,
            //hideLabel:true,
            // hidden:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 150,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.je.Selectlocation"),
            allowBlank:false
        });
       
        this.fromstoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.fromStore,
            displayField:'fullname',
            valueField:'store_id',
            fieldLabel: WtfGlobal.getLocaleText("acc.je.FromStore*"),
            hiddenName:"fromstore",
            allowBlank:false,
            readOnly:true,
            emptyText:WtfGlobal.getLocaleText("acc.je.SelectFromStore"),
            editable:true,
            width : 150,
            parent:this,
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });
        this.fromstoreCombo.on("change",function(){
            this.parent.ItemDetailGrid.EditorStore.removeAll();
            this.parent.ItemDetailGrid.addRec();
            this.parent.checkForSameStoreInCombos();
        });
        this.tostoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.Store,
            displayField:'fullname',
            valueField:'store_id',
            fieldLabel: WtfGlobal.getLocaleText("acc.je.ToStore*"),
            hiddenName:"tostore",
            allowBlank:false,
            emptyText:WtfGlobal.getLocaleText("acc.je.SelectToStore"),
            width : 150,
            editable:true,
            scope:this,
            tpl: new Wtf.XTemplate(
                    '<tpl for=".">',
                    '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                    '<div>{fullname}</div>',
                    '</div>',
                    '</tpl>'),
                    
        });
//        //ERM-894
        this.tostoreCombo.on("change",this.checkForSameStoreInCombos,this);

        this.submitBttn=new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.je.ClicktoSave")
            },
            iconCls: 'pwnd save',
            handler:function (){
                if(!this.documentNumber.getValue()&&this.sequenceFormatNO.getValue()=="NA"){
                    WtfComMsgBox(["Alert", "Please enter valid Document No "],3);
                    return;
                }
                this.saveOnlyFlag = true;
                Wtf.Msg.show({
                    title: WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg: WtfGlobal.getLocaleText("acc.IST.Warning"),
                    buttons: Wtf.Msg.YESNOCANCEL,
                    scope: this,
                    fn: function (btn) {
                        if (btn == 'yes') {
                            this.isAutofill = true;
                            var isValidStoreData = this.isValidStoreData();
                            if (isValidStoreData) {
                                this.fillAvailableQtyBeforeSaveItem();
                            } else {
                                return;
                            }
                        } else if (btn == 'no') {
                            this.SaveItem();
                        } else {
                            return
                        }
                    },
                    icon: Wtf.MessageBox.QUESTION
                });
                
//                Wtf.MessageBox.confirm("Confirm",WtfGlobal.getLocaleText("acc.IST.Warning"), function(btn){
//                    if(btn == 'yes') {     
//                        this.isAutofill=true;
//                        var isValidStoreData=this.isValidStoreData();
//                        if(isValidStoreData){
//                            this.fillAvailableQtyBeforeSaveItem();
//                        }else{
//                           return;
//                        }
//                    }else if(btn == 'no') {
//                        this.SaveItem();
//                    }
//                },this);
            },
            scope:this
        });

        this.cancelBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.je.ClicktoCancel")
            },
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            scope:this,
            handler:function(){
                this.Form.form.reset();
                this.ItemDetailGrid.EditorStore.removeAll();
                this.ItemDetailGrid.addRec();
            }
        });
        
        /*Save and Create New Button*/
        this.savencreateBttn=new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            id: "savencreate" +  this.id, 
            scope: this,
            iconCls: 'pwnd save',
            handler: function(){
                if(!this.documentNumber.getValue()&&this.sequenceFormatNO.getValue()=="NA"){
                    WtfComMsgBox(["Alert", "Please enter valid Document No "],3);
                    return;
                }
                this.saveOnlyFlag = false;
                Wtf.Msg.show({
                    title: WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg: WtfGlobal.getLocaleText("acc.IST.Warning"),
                    buttons: Wtf.Msg.YESNOCANCEL,
                    scope: this,
                    fn: function (btn) {
                        if (btn == 'yes') {
                            this.isAutofill = true;
                            var isValidStoreData = this.isValidStoreData();
                            if (isValidStoreData) {
                                this.fillAvailableQtyBeforeSaveItem();
                            } else {
                                return;
                            }
                        } else if (btn == 'no') {
                            this.SaveItem();
                        } else {
                            return
                        }
                    },
                    icon: Wtf.MessageBox.QUESTION
                });
//                Wtf.MessageBox.confirm("Confirm",WtfGlobal.getLocaleText("acc.IST.Warning"), function(btn){
//                    if(btn == 'yes') {    
//                        this.isAutofill=true;
//                        var isValidStoreData=this.isValidStoreData();
//                        if(isValidStoreData){
//                            this.fillAvailableQtyBeforeSaveItem();
//                        }else{
//                            return;
//                        }
//                    }else if(btn == 'no') {
//                        this.SaveItem();
//                    }
//                },this);
            }
        });

        /*Print Record Button*/
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        var colModArray = GlobalCustomTemplateList[Wtf.Inventory_ModuleId];
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
        });
        this.documentNumber = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.DocumentNo"),
            name:'documentNo',
            maxLength:50,
            width : 200
        });
        this.sequenceFormatNO = new Wtf.SeqFormatCombo({
            seqNumberField : this.documentNumber,
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.sequenceformat")+"*",
            name:"seqFormat",
            moduleId:1,
            allowBlank:false,
            width : 150
        });
        
        this.dateField = new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.date"),
            format:"Y-m-d",
            name:"businessdate",
            allowBlank:false,
            value:new Date(),
            width : 150
        });
        this.MOUTextField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.je.MoD")+"*",
            readOnly : true,
            name:"trans mod",
            width : 150,
            value:_fullName
        });
        this.KeyField = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.je.key"),
            readOnly : true,
            name:"key",
            width : 150,
            hidden:true,
            hideLabel:true
        });
        
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm2" + this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: Wtf.Inventory_ModuleId,
            isEdit: false
        });
        
        //         if(checktabperms(12, 1) == "edit"){
        //             this.MOUTextField.readOnly = false;
        //        }
        this.Form = new Wtf.form.FormPanel({
            region:"north",
            autoHeight:true,
            id:"northForm2" + this.id,
            url: 'INVGoodsTransfer/addIssueNoteRequest.do',
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            disabledClass:"newtripcmbss",
            items:[{
                border:false,
                layout:'form',
                cls:"visibleDisabled",
                items:[{
                    layout:'column',
                    border:false,
                    defaults:{
                        border:false
                    },
                    items:[{
                        columnWidth:0.33,
                        border:false,
                        layout:'form',
                        //                         bodyStyle:'padding:13px 13px 0px 13px',
                        labelWidth:105,
                        items:[
                        this.fromstoreCombo,
                        //this.fromLocCmb,
                        this.sequenceFormatNO,
                        this.documentNumber,
                        this.KeyField

                        ]
                    },
                    {
                        columnWidth:0.33,
                        border:false,
                        layout:'form',
                        //                        bodyStyle:'padding:13px 13px 0px 13px',
                        labelWidth:105,
                        items:[
                        this.tostoreCombo,
                        //this.toLocCmb,
                        this.MOUTextField

                        ]
                    },{
                        columnWidth:0.33,
                        border:false,
                        layout:'form',
                        //                         bodyStyle:'padding:13px 13px 0px 13px',
                        labelWidth:105,
                        items:[
                        this.dateField

                        ]
                    }]
                },this.tagsFieldset
                ]
            }]       
        });
    },
    checkForSameStoreInCombos:function(){ //ERM-894 From store and To Store in Goods Issue must not contain same warehouses
        if (this.fromstoreCombo.getValue() == this.tostoreCombo.getValue()) {
            this.tostoreCombo.reset();
        }
    },
    getItemDetail:function (qarejected){
        this.ItemDetailGrid = new Wtf.goodEditablegrid({
            layout:"fit",
            gridTitle:WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetails"),
            border:false,
            region:"center",
            qarejected:qarejected,
            prodIds: this.prodIds,
            parent:this,
            disabledClass:"newtripcmbss",
            height:300//,
        });
    },
    fillDefualtLocationValues: function() {
        var fromStoreId = this.fromstoreCombo.getValue();
        var toStoreId = this.tostoreCombo.getValue();
        var gridStore = this.ItemDetailGrid.EditorStore;
        var recCount = gridStore.getCount();
        var productIds = "";
        var gridRecArr = [];
        for (var k = 0; k < recCount; k++) {
            var productId = gridStore.getAt(k).get("productid");
            if (productId != undefined && productId != "") {
                if (productIds.length > 0) {
                    productIds = productIds + ("," + productId);
                } else {
                    productIds = productId;
                }
                var arr1 = [productId, k];
                gridRecArr.push(arr1);
            }
        }
        
        if (productIds != undefined && productIds != "" && productIds.length > 0) {
                Wtf.Ajax.requestEx({
                    url: "INVGoodsTransfer/getDefaultLocationDetailForIssueNote.do",
                    params: {
                        fromStoreId: fromStoreId,
                        toStoreId: toStoreId,
                        productIds: productIds
                    }
                }, this,
                        function(res, action) {
                            if (res.success == true && res.data!= undefined && res.data != "") {
                                
                                var jsonData = JSON.parse(res.data);
                                for (var i = 0; i < jsonData.length; i++) {
                                    var jsonObj = jsonData[i];
                                    var productId=jsonObj.productId;
                                    var defultFromLocationID=jsonObj.defultFromLocationID;
                                    var defultToLocationID=jsonObj.defultToLocationID;
                                    var defaultFromLocQty=jsonObj.defaultFromLocQty;
                                    var defultFromLocationName=jsonObj.defultFromLocationName;
                                    var defultToLocationName=jsonObj.defultToLocationName;
                                    for(var grdRc=0; grdRc < gridRecArr.length ; grdRc++){
                                        if(gridRecArr[grdRc][0] == productId){
                                            var rowIndex=gridRecArr[grdRc][1];
                                            gridStore.getAt(rowIndex).set("defaultFromLocQty", defaultFromLocQty);
                                            gridStore.getAt(rowIndex).set("defultFromLocationID", defultFromLocationID);
                                            gridStore.getAt(rowIndex).set("defultToLocationID", defultToLocationID);
                                            gridStore.getAt(rowIndex).set("defultFromLocationName", defultFromLocationName);
                                            gridStore.getAt(rowIndex).set("defultToLocationName", defultToLocationName);
                                        }
                                    }
                                }
                            } else {
                                WtfComMsgBox(["Error", "Error occurred while fetching data."], 0);
                                return;
                            }
                        },
                        function() {
                            WtfComMsgBox(["Error", "Error occurred while processing"], 1);
                        }
                );
            }
    },
    isValidStoreData: function() {
        var fromStoreId = this.fromstoreCombo.getValue();
        var isFromLocEmpty=false;
        var toStoreId = this.tostoreCombo.getValue();
        var isToLocEmpty=false;
        var fromidx = this.fromstoreCombo.store.find(this.fromstoreCombo.valueField,fromStoreId);
        var toidx = this.tostoreCombo.store.find(this.tostoreCombo.valueField,toStoreId);
        if(fromidx != -1){
            var rec = this.fromstoreCombo.store.getAt(fromidx);
            var defaultFromLocId = rec.get("defaultlocationid");
            if(defaultFromLocId == undefined || defaultFromLocId == ""){
                isFromLocEmpty=true;
            }
        }
        if(toidx != -1){
            var rec = this.tostoreCombo.store.getAt(toidx);
            var defaultToLocId = rec.get("defaultlocationid");
            if(defaultToLocId == undefined || defaultToLocId == ""){
                isToLocEmpty=true;
            }
        }
        
        if(isFromLocEmpty && isToLocEmpty){
            WtfComMsgBox(["Alert", "Default location is not set for From Store and To Store. "], 3);
            return false;
        }else if(isFromLocEmpty){
            WtfComMsgBox(["Alert", "Default location is not set for From Store."], 3);
            return false;
        }else if(isToLocEmpty){
            WtfComMsgBox(["Alert", "Default location is not set for To Store."], 3);
            return false;
        }else{
            return true;
        }
    },
    fillAvailableQtyBeforeSaveItem:function(){
        if((this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
            WtfComMsgBox(["Info", "Please Select MoD for Issue Note"], 0);
            return;
        }
        var recCount = this.ItemDetailGrid.EditorStore.getCount();
        if(!this.qarejected){
            recCount -= 1;
        }
        
        if(recCount == 0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stockrequest.PleaseaddItemstotransfer")], 0);
            return;
        }
        var fromStoreId = this.fromstoreCombo.getValue();
        var toStoreId = this.tostoreCombo.getValue();
        
        if(fromStoreId == undefined || toStoreId==undefined || fromStoreId == "" || toStoreId==""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockrequest.PleaseSelectStore")],0);
            return false;
        }
        
        for(var i=0;i<recCount;i++){
            var rec = this.ItemDetailGrid.EditorStore.getAt(i);
            var defaultFromLocqty=rec.get('defaultFromLocQty');
            var defultFromLocationID=rec.get('defultFromLocationID');
            var defultToLocationID=rec.get('defultToLocationID');
            var defultFromLocationName=rec.get('defultFromLocationName');
            var defultToLocationName=rec.get('defultToLocationName');
            var quantity=rec.get('quantity');
             
            var orderToStockUOMFactor=Wtf.account.companyAccountPref.UomSchemaType?rec.get("orderToStockUOMFactor"):rec.get("confactor");
            
            var isRowForProduct=rec.get('isRowForProduct');
            var isRackForProduct=rec.get('isRackForProduct');
            var isBinForProduct=rec.get('isBinForProduct');
            var isBatchForProduct=rec.get('isBatchForProduct');
            var isSerialForProduct=rec.get('isSerialForProduct');
            var stockDetails=rec.get('stockDetails');
            var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchForProduct && !isSerialForProduct;
            var isDefaultAllocationAllowed = !(isRowForProduct || isRackForProduct || isBinForProduct || isBatchForProduct || isSerialForProduct);
            
            if(rec.data.quantity > 0){
                if(stockDetails == "" || stockDetails == undefined){
                    if(isDefaultAllocationAllowed && (isNegativeAllowed || defaultFromLocqty>= (quantity*orderToStockUOMFactor))){
                        var data = {
                            batchName : "",
                            detailId : "",
                            fromBinId : "",
                            fromBinName :"",
                            fromLocationId :defultFromLocationID,
                            fromLocationName: defultFromLocationName,
                            fromRackId :"",
                            fromRackName :"",
                            fromRowId :"",
                            fromRowName :"",
                            manual :"",
                            quantity: (quantity*orderToStockUOMFactor),
                            serialNames :"",
                            toBinId :"",
                            toBinName :"",
                            toLocationId :defultToLocationID,
                            toLocationName: defultToLocationName,
                            toRackId: "",
                            toRackName: "",
                            toRowId :"",
                            toRowName :""
                        };
                        rec.set("stockDetailQuantity",quantity);
                        rec.set("stockDetails",[data]);
                    }else{
                        WtfGlobal.highLightRowColor(this.ItemDetailGrid.EditorGrid,rec,true,0,2,true);
                    }
                }
            }
        }
        this.SaveItem();
    },
    SaveItem:function (){
//        this.submitBttn.disabled=true;
//        this.savencreateBttn.disable();
        var dataArr=new Array();

        if((this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
            WtfComMsgBox(["Info", "Please Select MoD for Goods Order"], 0);
//            this.enableButtons();
            this.MOUTextField.markInvalid("This field is required");
            return;
        }
        var recCount = this.ItemDetailGrid.EditorStore.getCount();
        if(!this.qarejected){
            recCount -= 1;
        }
        if(recCount == 0){
            WtfComMsgBox(["Info", "Please add Item(s) to issue"], 0);
//            this.enableButtons();
            return;
        }
        var modRecs = this.ItemDetailGrid.EditorStore.getModifiedRecords();
        if(modRecs.length == 0){
            WtfComMsgBox(["Info", "Please enter quantity for at least one item."], 0);
//            this.enableButtons();
            return;
        }
        
        for(var i=0;i<recCount;i++){
            var rec = this.ItemDetailGrid.EditorStore.getAt(i);
            if(this.validateRecord(rec)){
                var jObject={};
                jObject.productid = this.ItemDetailGrid.EditorStore.getAt(i).get("productid");
                jObject.transferQuantity = this.ItemDetailGrid.EditorStore.getAt(i).get("quantity");
                jObject.costcenter = this.ItemDetailGrid.EditorStore.getAt(i).get("costcenter");
                jObject.uom = this.ItemDetailGrid.EditorStore.getAt(i).get("uom");
                jObject.packaging = this.ItemDetailGrid.EditorStore.getAt(i).get("packagingid");
                jObject.confactor = this.ItemDetailGrid.EditorStore.getAt(i).get("confactor");
                jObject.remark = this.ItemDetailGrid.EditorStore.getAt(i).get("remark");
                jObject.moduletype = rec.get("moduletype");
                jObject.qaapprovalid = rec.get("qaapprovalid");
                jObject.qaapprovaldetailid = rec.get("qaapprovaldetailid");
                jObject.stockDetails = this.ItemDetailGrid.EditorStore.getAt(i).get("stockDetails");
                
                var linelevelcustomdata = Wtf.decode(WtfGlobal.getCustomColumnData(this.ItemDetailGrid.EditorStore.data.items[i].data, Wtf.Inventory_ModuleId).substring(13));
                if (linelevelcustomdata.length > 0)
                    jObject.linelevelcustomdata = linelevelcustomdata;
                
                dataArr.push(jObject);
                
                var stockDetails=this.ItemDetailGrid.EditorStore.getAt(i).get("stockDetails");
                if ((stockDetails == "" || stockDetails == undefined)) {
                            WtfComMsgBox(["Alert", "Please select stock location detail for item."], 3);
                            WtfGlobal.highLightRowColor(this.ItemDetailGrid.EditorGrid,rec,true,0,2,true);
                            return;
                }
            }else{
//                this.enableButtons();
                Wtf.MessageBox.show({
                    msg:"Please enter valid data for Issue ",
                    icon:Wtf.MessageBox.WARNING,
                    buttons:Wtf.MessageBox.OK,
                    title:"Warning"
                });
                return;
            }
        }
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(!isValidCustomFields){
            return;
        }
        var finalStr = JSON.stringify(dataArr);
        this.sendGoodsIssueRequest(finalStr);
    },
    enableafterSaveButtons:function(enableflag){
        if(enableflag){//save
            this.singleRowPrint.enable();
        }else{
            this.savencreateBttn.enable();
        }
    },
    disableafterSaveButtons:function(disableflag){
        if(disableflag){//save button
            this.savencreateBttn.disable();
            this.cancelBttn.disable();
            this.submitBttn.disable();
        }else{//save and create new button
            this.singleRowPrint.disable();
        }
    },
    enableButtons:function(){
        this.submitBttn.enable();
        this.savencreateBttn.enable();
    },
    printRecordTemplate:function(printflg,item){
        var transactionno = Wtf.OrderNoteNo;
        var recordids = Wtf.recordbillid;
        var isAutoPopulate = item.isAutoPopulate != undefined ? item.isAutoPopulate : false;
        if(isAutoPopulate){
            transactionno = item.transactionno;
            recordids = item.recordid;
        }
        var params= "myflag=order&transactiono="+transactionno+"&moduleid="+Wtf.Inventory_ModuleId+"&templateid="+item.id+"&recordids="+recordids+"&filetype="+printflg+"&isAutoPopulate="+isAutoPopulate;  
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "ACCExportPrintCMN/exportSingleStockRequestIssue.do";
        var inputs =params.split('&');
        for(var i=0;i<inputs.length;i++){
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = KV_pair[1];
            mapForm.appendChild(mapInput); 
        }
        // If is after saving stock issue then autopopulate print with Document Designer template in new window
        if(isAutoPopulate){
            var htmlString = "";
            htmlString += "<div style='float: right; padding-top: 3px; padding-right: 5px;'>"
                + "<button id = 'print' title='Print Note' onclick='window.print();' style='color: rgb(8, 55, 114);' href='#'>Print</button>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>"
                + "<style>@media print {button#print{display:none;}}</style>";
            var disp_setting="toolbar=yes,location=no,";
            disp_setting+="directories=yes,menubar=yes,";
            disp_setting+="scrollbars=yes,width=700, height=600, left=100, top=25";
    
            var docprint=window.open("","mywindow",disp_setting);
            docprint.document.open();
            docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
            docprint.document.write('<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
            docprint.document.write('<head><title></title>');
            docprint.document.write('</head><body onLoad="self.print()"><center>');
            docprint.document.body.appendChild(mapForm);
            docprint.document.write(htmlString);
            docprint.document.write('</center></body></html>');
            mapForm.submit();
            docprint.document.close();
        } else{ // normal Print Record(s) button print
            document.body.appendChild(mapForm);
            mapForm.submit();
            var myWindow = window.open("", "mywindow","menubar=1,resizable=1,scrollbars=1");
            var div =  myWindow.document.createElement("div");
            div.innerHTML = "Loading, Please Wait...";
            myWindow.document.body.appendChild(div);
        }
        mapForm.remove();
    },
    sendGoodsIssueRequest:function(finalStr){
        if(this.sequenceFormatNO.getValue() == "NA" && this.documentNumber.getValue().trim() == "" ){
            WtfComMsgBox(["Info", "Please enter valid Document No."], 0);
            return;
        }
        var loadingMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        if(this.Form.form.isValid()){
            var allowNegInv="";
            if(this.allowNegativeInventory != undefined || this.allowNegativeInventory != ""){
                allowNegInv=this.allowNegativeInventory;
            }
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            var dimencustomfield="";
            if (custFieldArr.length > 0)
                dimencustomfield = JSON.stringify(custFieldArr);

            this.allowNegativeInventory="";
            this.submitBttn.disabled=true;
            this.savencreateBttn.disable();
            loadingMask.show();
            this.Form.form.submit({
                params:{
                    myflag:'issue',
                    jsondata:finalStr,
                    seqFormatId:this.sequenceFormatNO.getValue(),
                    documentNumber:this.documentNumber.getValue(),
                    fromlocation:this.fromLocCmb.getValue(),
                    tolocation:this.toLocCmb.getValue(),
                    allowNegativeInventory: allowNegInv,
                    isqarejected:this.qarejected,
                    customfield:dimencustomfield,
                    UomSchemaType:!Wtf.account.companyAccountPref.UomSchemaType
                },
                scope:this,
                success:function (result,resp){
                    loadingMask.hide();
                    var retstatus = eval('('+resp.response.responseText+')');
                    var seqno=retstatus.transfernoteno;
                    var msg = retstatus.data.msg;
                    var title="Error";
                    if(retstatus.data.success){
                        title="Success";
                        msg=retstatus.data.msg;
                        Wtf.MessageBox.show({
                            msg:msg,
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK,
                            title:title
                        });
                        
                        Wtf.MessageBox.show({
                            msg:msg,
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK,
                            title:title,
                            scope: this,
                            fn: function () {
                                if (retstatus.data.IssueNoteNo != "" && retstatus.data.IssueNoteNo != undefined) {
                                    var itemObj = {
                                        billid: retstatus.data.billid,
                                        billno: retstatus.data.IssueNoteNo
                                    }
                                    printDocumentAfterSave(Wtf.Inventory_ModuleId, itemObj);
                                }
                            }
                        });
                        
                        Wtf.recordbillid=resp.result.data.billid;
                        Wtf.OrderNoteNo=resp.result.data.IssueNoteNo;
                        this.disableafterSaveButtons(this.saveOnlyFlag);
                        this.enableafterSaveButtons(this.saveOnlyFlag);
                        
                        if(this.saveOnlyFlag && retstatus.data.success){//Disabling and enabling after saving
                            this.Form.disable();
                            this.ItemDetailGrid.disable();
                        }else{
                            this.ItemDetailGrid.EditorStore.removeAll();
                            this.ItemDetailGrid.addRec();
                            this.enableButtons();
                            this.fromstoreCombo.setValue(this.fromStore.getAt(0).data.id, true);
                            this.Form.form.reset();
                            this.tagsFieldset.resetCustomComponents();
                            if(Wtf.getCmp("StockoutApprovalDetailReport") !=undefined){
                                Wtf.getCmp("StockoutApprovalDetailReport").getStore().reload(); 
                            }
                        }         
                    }else if(retstatus.data.success==false && (retstatus.data.currentInventoryLevel != undefined && retstatus.data.currentInventoryLevel != "")){
                
                        if(retstatus.data.currentInventoryLevel=="warn"){

                            Wtf.MessageBox.confirm("Confirm",retstatus.data.msg, function(btn){
                                if(btn == 'yes') {        
                                    this.allowNegativeInventory=true;
                                    this.sendGoodsIssueRequest(finalStr);
                                }else if(btn == 'no') {
                                    this.allowNegativeInventory=false;
                                }
                            },this);
                        }
                    
                        if(retstatus.data.currentInventoryLevel=="block"){
                            Wtf.MessageBox.show({
                                msg: retstatus.data.msg,
                                icon:Wtf.MessageBox.INFO,
                                buttons:Wtf.MessageBox.OK,
                                title:"Warning"
                            });
                        }
                        this.enableButtons();
                    }else if(retstatus.data.msg){
                                WtfComMsgBox(["Stock Issue",retstatus.data.msg],retstatus.data.success*2+2);
                                this.enableButtons();

                     }else if(retstatus.data.success==false){
                        title="ERROR";
                        msg=retstatus.data.msg;
                        if(msg == '' || msg == undefined || msg == null){
                            msg = "Error while sending Goods Issue"
                        }
                        
                        Wtf.MessageBox.show({
                            msg:msg,
                            icon:Wtf.MessageBox.ERROR,
                            buttons:Wtf.MessageBox.OK,
                            title:title
                        });
                        this.enableButtons();
                    }
                },
                failure:function (){
                    loadingMask.hide();
                    Wtf.MessageBox.show({
                        msg:"Error while sending Goods Issue",
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK,
                        title:"Error"
                    });
                    this.Form.form.reset();
                    this.ItemDetailGrid.EditorStore.removeAll();
                    this.ItemDetailGrid.addRec();
                    this.submitBttn.disabled=false;
                }
            });
        }else{
            this.submitBttn.disabled=false;
        }  
    },
    validateRecord: function(rec){
       
       
        if(rec.get("productid") == "" || rec.get("productid") == null || rec.get("productid") ==undefined)
        {
            WtfComMsgBox(["Info", "Please enter valid data for Item "], 0);
            return false;
        }
        
        else if( rec.get("quantity") == "" || rec.get("quantity") <= 0){
            WtfComMsgBox(["Info", "Please enter valid data for Quantity "], 0);
            return false;
        }
       
        else{
            return true;
        }
     
    },
    fillRequestedItems: function(productDataArray){
        this.ItemDetailGrid.itemEditorStore.on('load', function(){
            var row = 0;
            for(var i= 0; i< productDataArray.length ; i++){
                var productData = productDataArray[i]
                this.fromstoreCombo.setValue(productData.storeid);
                var added = this.ItemDetailGrid.fillSelectedRecValue(row, productData, (i+1 == productDataArray.length));
                if(added){
                    row++;
                }
            }
        }, this)
    }
    
    
});

//------------------------------------------Editor Grid Component---------------------------------------------------

Wtf.goodEditablegrid = function (config){
    Wtf.apply(this,config);
    Wtf.goodEditablegrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.goodEditablegrid,Wtf.Panel,{
    initComponent:function (){
        Wtf.goodEditablegrid.superclass.initComponent.call(this);
        this.getEditorGrid();
        this.tmpPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            {
                region:"north",
                height:25,
                border:false,
                bodyStyle:"background-color:#f1f1f1;padding:8px",
                html:"<div class='gridTitleClass' style='float:left;'>"+this.gridTitle+"</div><div style = 'float:right; font-size:9px;'>"+WtfGlobal.getLocaleText("acc.field.Note")+" : " +WtfGlobal.getLocaleText("acc.stock.templatenote")+"</div>"
            },
            this.EditorGrid
            ]
        });
        this.add(this.tmpPanel);
    },
    getEditorGrid:function (){
        
        //        this.costCenterRec = new Wtf.data.Record.create([
        //        {
        //            name:"id"
        //        },
        //        {
        //            name:"name"
        //        },
        //        {
        //            name:"ccid"
        //        }
        //        ]);
        //        this.costCenterReader = new Wtf.data.KwlJsonReader({
        //            root:"data",
        //            totalProperty:"count"
        //        },this.costCenterRec);
        //
        //        this.costCenterStore = new Wtf.data.Store({
        //            url:  'CostCenter/getCostCenter.do',
        //            reader:this.costCenterReader
        //        });
        //        this.costCenterStore.load();
        //        this.costCenterCombo = new Wtf.form.ComboBox({
        //            triggerAction:"all",
        //            mode:"local",
        //            typeAhead:true,
        //            forceSelection:true,
        //            store:this.costCenterStore,
        //            displayField:"ccid",
        //            valueField:"id",
        //            width:200
        //        });
        
        this.uomRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"uomid"
        },
        {
            name:"name"
        },
        {
            name:"uomname"
        },
        {
            name:"factor"
        }
        ]);
        this.uomReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.uomRec);
        
        var uomStoreURL = "ACCUoM/getUnitOfMeasure.do";
        if (Wtf.account.companyAccountPref.UomSchemaType) {
            uomStoreURL = 'INVPackaging/getPackagingUOMList.do';
        }
        this.uomStore = new Wtf.data.Store({
//            url:  'INVPackaging/getPackagingUOMList.do',
            url:  uomStoreURL,
            reader:this.uomReader
        });
        chkUomload();
        this.uomCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
//            store:(Wtf.account.companyAccountPref.UomSchemaType)?this.uomStore:Wtf.uomStore,
            store:this.uomStore,
            displayField:Wtf.account.companyAccountPref.UomSchemaType?"name":"uomname",
            valueField:Wtf.account.companyAccountPref.UomSchemaType?"id":"uomid",
            width:200
        });
        this.transBaseuomrate=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            maxLength:10,
            decimalPrecision: Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT
        });
        
        this.packagingRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"name"
        }
        ]);
        this.packagingReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.packagingRec);

        this.packagingStore = new Wtf.data.Store({
            url:  'INVPackaging/getPackagingList.do',
            reader:this.packagingReader
        });
        //        this.packagingStore.load();
        this.packagingCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.packagingStore,
            displayField:"name",
            valueField:"id",
            width:200
        });
        this.packagingCombo.on("change",function(){
            this.uomStore.load({
                params:{
                    packagingId:this.packagingCombo.getValue()
                }
            });
        },this);
     
        chkLineLevelCostCenterload();
        this.costCenterCombo= new Wtf.form.ExtFnComboBox({
            hiddenName:"costcenter",
            //            id:"costcenter"+this.heplmodeid+this.id,
            store: Wtf.LineLevelCostCenterStore,
            valueField:'id',
            displayField:'name',
            extraFields:['ccid','name'],
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            listWidth:100,
            //            width:200,
            scope:this,
            isProductCombo: true,
            maxHeight:250,
            extraComparisionField:'ccid',// type ahead search on acccode as well.
            lastQuery:'',
            hirarchical:true
            
        }); 
        
        this.costCenterCombo.listWidth=250;
     
        this.itemEditorRec = new Wtf.data.Record.create([
        {
            "name":"id"
        },
        {
            name:"productid"
        },

        {
            name:"pid"
        },

        {
            name:"productname"
        },
        {
            name:"packaging"
        },
        {
            name:"packagingid"
        },

        {
            name:"uomid"
        },

        {
            name:"uomname"
        },
        {
            name:'orderinguomname'
        },
        {
            name:'orderinguomid'
        },
        
        {
            name:'transferinguomname'
        },
        
        {
            name:'stockuomname'
        },
        
        {
            name:"purchaseprice"
        },
        {
            name : "desc"  
        },
  
        {
            name:"quantity"
        },
        {
            name:"reason"
        },
        {
            name:"costcenter"
        },
        {
            name:"remark"
        },
        {
            name:"amount"
        },
        {
            name:"date"
        },
        {
            name:"category"
        },
        {
            name:"warehouse"
        },
        {
            name:"isBatchForProduct"
        },

        {
            name:"isSerialForProduct"
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
            name:"orderToStockUOMFactor"
        },
        {
            name:"stockDetails"
        },
        {
            name:"uomschematype"
        },
        {
            name:"ismultipleuom"
        },
        {
            name:"hasAccess"
        },
        {
            name:"defaultFromLocQty"
        },
        {
            name:"defultFromLocationID"
        },
        {
            name:"defultToLocationID"
        },
        {
            name:"defultFromLocationName"
        },
        {
            name:"defultToLocationName"
        }
        ]);
        
        
        this.itemEditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.itemEditorRec);
        
        this.itemEditorStore = new Wtf.data.Store({
           
            baseParams:{
                isStoreLocationEnable:true,
                isInventoryForm:true
            },
            proxy: new Wtf.data.HttpProxy(new Wtf.data.Connection({
                url:"ACCProduct/getProductsForCombo.do",            
                timeout: 300000
            })),
            // pruneModifiedRecords:true,
            reader:this.itemEditorReader
        });
        var loadingMask1 = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        this.itemEditorStore.on('beforeload',function(){
            loadingMask1.show();
        },this)
        this.itemEditorStore.on('load',function(){
            loadingMask1.hide();
        },this)
        
        this.itemCodeEditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.itemEditorRec);
        this.itemCodeEditorStore = new Wtf.data.Store({
            url:"jspfiles/inventory/store.jsp",
            sortInfo: {
                field: 'itemdescription',
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            },
            reader:this.itemCodeEditorReader
        });
        
        this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
        if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==false){
            this.itemCodeEditorCombo=new Wtf.form.ExtFnComboBox({
                name:'itemcode',
                store:this.itemEditorStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus:true,
                valueField:'productid',
                displayField:'productname',
                extraFields:['pid'],
                listWidth:300,
                extraComparisionField:'pid',// type ahead search on acccode as well.
                lastQuery:'',
                //editable:false,
                scope:this,
                hirarchical:true,
                // addNewFn:this.openProductWindow.createDelegate(this),
                forceSelection:true
                   
            });
            this.itemEditorStore.load();
        }
        else{
            if(this.productOptimizedFlag==Wtf.Products_on_type_ahead){
                var baseParams={
                    isStoreLocationEnable:true,
                    isInventoryForm:true
                }
                this.itemCodeEditorCombo =CommonERPComponent.createProductPagingComboBox(100,300,30,this,baseParams,false,true);
            }else{
                this.itemCodeEditorCombo=new Wtf.form.ExtFnComboBox({
                    name:'itemcode',
                    store:this.itemEditorStore,    
                    typeAhead: true,
                    selectOnFocus:true,
                    isProductCombo: true,
                    valueField:'productid',
                    displayField:'pid',
                    extraFields:['productname'],
                    listWidth:300,
                    //listWidth:450,
                    extraComparisionField:'pid',// type ahead search on acccode as well.
                    mode:'remote',
                    //editable:false,
                    hideTrigger:true,
                    scope:this,
                    triggerAction : 'all',
                    editable : true,
                    minChars : 1,
                    hirarchical:true,
                    hideAddButton : true,//Added this Flag to hide AddNew  Button  
                    //addNewFn:this.openProductWindow.createDelegate(this),
                    forceSelection:true
                });
            }

            if (this.prodIds !=undefined && this.prodIds.length > 0) {
                this.itemEditorStore.load({
                    params: {
                        ids: this.prodIds
                    }
                });
            }
        }
        this.itemCodeEditorCombo.on('beforeselect', function(combo, record, index) {
            if(this.productOptimizedFlag==Wtf.Products_on_type_ahead){
                if(record.data!=undefined &&record.data!=null){
                    var rec=record.data;
                    if(rec.productid!=undefined && rec.productid!=null &&rec.productid!="" ) {
                        var productidarray=[];
                        productidarray.push(rec.productid);
                        this.itemEditorStore.load({
                            params:{
                                ids : productidarray
                            }
                        });
                    }
                }
            }
            return validateSelection(combo, record, index);
        }, this);
       
        /*
         *SDP-4553
         *Set Product Id When user scans barcode for product id field.
         *After scanning barcode by barcode reader press Enter or Tab button.
         *So we are handling specialkey event to set product id to product id combo.
         **/
        this.itemCodeEditorCombo.on('specialkey', function(field , e) {
            if(e.keyCode == e.ENTER|| e.keyCode == e.TAB){
                if(field.getRawValue() !="" && (field.getValue()==""|| /(<([^>]+)>)/ig.test(field.value) )){
                    var value = field.getRawValue();
                    //                    e.stopPropagation();
                    if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==Wtf.Show_all_Products){
                        /*
                     *This block will execute when Show all product or product as free text is selected.
                     *In this case we will search pid in itemEditorStore and set value accordingly. 
                     **/
                        var index = WtfGlobal.searchRecordIndex(this.itemEditorStore,value,'pid');
                        if(index!=-1){
                            var prorec=this.itemEditorStore.getAt(index); 
                            var dataObj = prorec.data;
                            setPIDForBarcode(this,dataObj,field,false);
                    
                        }
                    }else{
                        
                        /*
                     *This block will execute when Show product on type ahead is selected.
                     *In this case we will fetch data from backend.
                     **/
                        var params = JSON.clone(this.itemEditorStore.baseParams);
                        params.query = field.getRawValue();
                        params.isForBarcode = true;
                        
                        Wtf.Ajax.requestEx({
                            url: this.itemEditorStore.proxy.conn.url,
                            params:params
                        }, this, function(response) {
                            var prorec = response.data[0];
                            if(prorec){
                                var newrec = new this.itemEditorStore.reader.recordType(prorec);
                                this.itemEditorStore.add(newrec);
                                setPIDForBarcode(this,prorec,field,true);
                            }
                        }, function() {});
                    }
                }
            }
        },this);
        
        this.EditorRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"itemcode"
        },
        {
            name:"partnumber"
        },
        {
            name:"description"
        },

        {
            name:"uom"
        },
        {
            name:"stockuom"
        },
        {
            name:"confactor"
        },
        {
            name:"packaging"
        },
        {
            name:"packagingid"
        },

        {
            name:"quantity"
        },
        {
            name:"uomid"
        },
        {
            name:"uomname"
        },
        {
            name:'orderinguomname'
        },
        
        {
            name:'transferinguomname'
        },
        {
            name:'stockuomname'
        },
        {
            name:"costcenter"
        },
        {
            name:"remark"
        },
        {
            name:"uomtype"
        },
        {
            name:"uomval"
        },
        {
            name:"primaryuomvalue"
        },
        {
            name:"inneruomvalue"
        },
        {
            name:"casinguomvalue"
        },
        {
            name:"selfloactionName"
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
            name:"desc"
        },
        {
            "name":"orderToStockUOMFactor"
        },
        {
            name:"stockDetails"
        },
        {
            name:"qaapprovalid"
        },
        {
            name:"qaapprovaldetailid"
        },
        {
            name:"moduletype"
        },
        {
            name:"uomschematype"
        },
        {
            name:"ismultipleuom"
        },
        {
            name:"defaultFromLocQty"
        },
        {
            name:"defultFromLocationID"
        },
        {
            name:"defultToLocationID"
        },
        {
            name:"defultFromLocationName"
        },
        {
            name:"defultToLocationName"
        }
        ]);

        this.EditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.EditorRec);

        this.EditorStore = new Wtf.data.Store({
            url:"Json/demo.json",
            reader:this.EditorReader
        });
        
        WtfGlobal.updateStoreConfig(GlobalColumnModel[Wtf.Inventory_ModuleId], this.EditorStore);
        this.addRec();
        var cmWidth=175;
        //this.EditorStore.load();
        this.EditorStore.on("load",this.addRec,this);
        var columnArr = new Array();
        columnArr.push(new Wtf.grid.RowNumberer(),
            {//ERP-12415 [SJ]
                header:WtfGlobal.getLocaleText("acc.common.add"),
                align:'center',
                width:30,
                dataIndex:"plusbtn",
                renderer: this.addProductList.createDelegate(this)
            },
            {
                header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                dataIndex:"pid",
                width:cmWidth,
                editor:this.itemCodeEditorCombo
//                renderer:this.getComboRenderer(this.itemCodeEditorCombo)
            },
            {
                header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
                dataIndex:"itemdescription",
                width:cmWidth
            },
            {
                header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),
                width:cmWidth,
                dataIndex:"desc"
                
            },
            {
                header:WtfGlobal.getLocaleText("acc.je.CoilcraftPartNo"),
                dataIndex:"partnumber",
                hidden:true
            },
            
            {
                header:WtfGlobal.getLocaleText("acc.product.packaging"),
                dataIndex:"packaging",
                renderer:this.getComboRenderer(this.packagingCombo),
                editor:Wtf.account.companyAccountPref.UomSchemaType==false?this.packagingCombo:"",
                width:cmWidth,
                hidden:!Wtf.account.companyAccountPref.UomSchemaType?true:false
            },
            {
                header:WtfGlobal.getLocaleText("acc.uomgrid.stocksuom"),
                dataIndex:"uom",
                renderer:this.getComboRenderer(this.uomCombo),
                editor:this.uomCombo,
                width:cmWidth
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),
                dataIndex:"confactor",
                hidden:Wtf.account.companyAccountPref.UomSchemaType?true:false,
                width:150,
                renderer:this.conversionFactorRenderer(this.itemEditorStore,"productid","uomname",this.EditorStore),
                editor:(Wtf.account.companyAccountPref.UomSchemaType===0) ?this.transBaseuomrate : ""     //Does allow to user to change conversion factor
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.CostCenter"),
                dataIndex:"costcenter",
                width:cmWidth,
                renderer:this.getComboRenderer(this.costCenterCombo),
                editor:this.costCenterCombo
            },
            {
                header:WtfGlobal.getLocaleText("acc.je.QuantityinOrderUoM"),
                dataIndex:"quantity",
                width:cmWidth,
                decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
                editor:new Wtf.form.NumberField(
                {
                    scope: this,
                    allowBlank:false,
                    decimalPrecision: Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
                    allowNegative:false,
                    value:0,
                    listeners : {
                        'focus': this.setZeroToBlank
                    }

                //           validator : function(val){
                //               var re5digit=/^[0-9]*$/;
                //               if(val.search(re5digit) == -1){
                //                   return false;
                //               }else{
                //                    return true;
                //               }
                //
                //           }
                }),
                renderer: function(val){
                    return val;
                }
            },
            {
                header:WtfGlobal.getLocaleText("acc.je.isBatchEnable"),
                dataIndex:"isBatchForProduct",
                hidden:true
            },
            {
                header:WtfGlobal.getLocaleText("acc.je.isSerialEnable"),
                dataIndex:"isSerialForProduct",
                hidden:true
            },
            {
                header: '',
                align:'center',
                renderer: this.serialRenderer.createDelegate(this),
                hidden:this.qarejected,// hidden:(!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory),
                width:40
            },
            {
                header:WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
                dataIndex:"remark",
                width:cmWidth,
                editor:new Wtf.form.TextArea({
                    regex:Wtf.validateAddress,
                    maxLength:200
                })
            });
            
            columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModel[Wtf.Inventory_ModuleId]);
            columnArr.push({
                header:WtfGlobal.getLocaleText("mrp.rejecteditems.report.actioncolumn.title"),
                align:'center',
                dataIndex: "lock",
                width:50,
                renderer: function(v,m,rec){
                    return "<span class='pwnd delete-gridrow'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>";
                }
            });

        this.EditorColumn = new Wtf.grid.ColumnModel(columnArr);
        this.addBut = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.add"),
            iconCls:'pwnd addicon'
        });

        this.EditorGrid = new Wtf.grid.EditorGridPanel({
            cm:this.EditorColumn,
            region:"center",
            id:"editorgrid2rtrt",
            autoScroll:true,
            store:this.EditorStore,
            viewConfig:{
                forceFit:false
            },
            clicksToEdit:1,
            scope:this
        });
        this.EditorGrid.on('rowclick',this.handleRowClick,this);
        this.EditorGrid.on("afteredit",this.fillGridValue,this);
        this.EditorGrid.on("cellclick",this.cellClick,this);
        this.EditorGrid.on('populateDimensionValue',this.populateDimensionValueingrid,this);
    //        this.EditorGrid.on("beforeedit",this.loadUom,this);
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
    },
    conversionFactorRenderer:function(store, valueField, displayField,gridStore) {
        return function(value, meta, record) {
            if(value != "") {
                value = (parseFloat(getRoundofValue(value)).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT)=="NaN")?parseFloat(0).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT):parseFloat(getRoundofValueWithValues(value,Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT)).toFixed(Wtf.UOM_CONVERSION_RATE_DECIMAL_DIGIT);
            }
            var idx = Wtf.uomStore.find("uomid", record.data["uom"]);            
            if(idx == -1)
                return value;
            var uomname = Wtf.uomStore.getAt(idx).data["uomname"];
            if (uomname == "N/A") {
                return value;
            }
            
            var rec="";
            idx = store.find(valueField, record.data[valueField]);
            if(idx == -1){
                idx = gridStore.find(valueField, record.data[valueField]);
                if(idx == -1)
                    return value;
                rec = gridStore.getAt(idx);
                return "1 "+ uomname +" = "+ +value+" "+rec.data["stockuom"];
            }else{
                rec = store.getAt(idx);
                return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
            }  
            
        }
    },
    setZeroToBlank : function(field){
        if(field.getValue()==0){
            field.setValue("");
        }
    },
    
    ArrangeNumberer: function(currentRow) {                // use currentRow as no. from which you want to change numbering
        var plannerView = this.EditorGrid.getView();                      // get Grid View
        var length = this.EditorStore.getCount();              // get store count or no. of records upto which you want to change numberer
        for (var i = currentRow; i < length; i++)
            plannerView.getCell(i, 0).firstChild.innerHTML = i + 1;
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var total=store.getCount();
            if(!this.qarejected && rowindex==total-1){
                return;
            }
            Wtf.MessageBox.confirm('Warning', 'Are you sure you want to remove this item?', function(btn){
                if(btn!="yes") return;
                store.remove(store.getAt(rowindex));
                this.ArrangeNumberer(rowindex);
            //                this.fireEvent('datachanged',this);
            }, this);
        }
    },

    addRec:function (){
        var Record = this.EditorStore.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
        var blankObj = {};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if (f.name != 'rowid') {
                blankObj[f.name] = '';
                if (!Wtf.isEmpty(f.defValue))
                    blankObj[f.name] = f.convert((typeof f.defValue == "function" ? f.defValue.call() : f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        this.EditorStore.add(newrec);
    },
    
    fillSelectedRecValue: function(row, productData, isLastRec){
        var added = false;
        
        for(var i=0;i<this.itemEditorStore.getCount();i++){
            if(this.itemEditorStore.getAt(i).get("productid") == productData.productid){
                var rec = this.EditorStore.getAt(row);
                rec.set("productid", productData.productid);
                rec.set("quantity",(productData.quantity/this.itemEditorStore.getAt(i).get("orderToStockUOMFactor")));
                rec.set("pid",this.itemEditorStore.getAt(i).get("pid")); 
                rec.set("itemdescription",this.itemEditorStore.getAt(i).get("productname"));
                rec.set("packaging",this.itemEditorStore.getAt(i).get("packaging"));
                rec.set("packagingid",this.itemEditorStore.getAt(i).get("packagingid"));
                rec.set("uomid",this.itemEditorStore.getAt(i).get("uomid"));
                rec.set("uom",this.itemEditorStore.getAt(i).get("orderinguomname"));
                rec.set("uomname",this.itemEditorStore.getAt(i).get("orderinguomname"));
                rec.set("partnumber",this.itemEditorStore.getAt(i).get("partnumber"));
                rec.set("costcenter",this.itemEditorStore.getAt(i).get("costcenter")); 
                rec.set("warehouse",this.itemEditorStore.getAt(i).get("warehouse")); 
                rec.set("stockDetails",productData.productDetailDataArr);
                rec.set("uomschematype",this.itemEditorStore.getAt(i).get("uomschematype"));
                rec.set("ismultipleuom",this.itemEditorStore.getAt(i).get("ismultipleuom"));
                rec.set("defaultFromLocQty",this.itemEditorStore.getAt(i).get("defaultFromLocQty"));
                rec.set("defultFromLocationID",this.itemEditorStore.getAt(i).get("defultFromLocationID"));
                rec.set("defultToLocationID",this.itemEditorStore.getAt(i).get("defultToLocationID"));
                rec.set("defultFromLocationName",this.itemEditorStore.getAt(i).get("defultFromLocationName"));
                rec.set("defultToLocationName",this.itemEditorStore.getAt(i).get("defultToLocationName"));
                
                added = true;
                
                if(rec.get("uomname") == "" ){
                    this.EditorGrid.getView().getRow(row).style = "background-color :pink";
                }               

            }
           
        }
        var alreadyAdded = false;
        for(i=0;i<this.EditorStore.getCount();i++){
            if(i != row && this.EditorStore.getAt(i).get("productid") == productData.productid){
                alreadyAdded = true;
                this.EditorStore.getAt(row).reject();
                break;
            }
        }
        if(alreadyAdded){
            WtfComMsgBox(["Failure", "Product is already added in grid."],3);
        }else if( !isLastRec && added && row == this.EditorStore.getCount()-1){
            this.addRec();
        }
        return added;
   
    },
    
    fillGridValue:function (e){
       
        for(var i=0;i<this.itemEditorStore.getCount();i++){
            if(this.itemEditorStore.getAt(i).get("productid") == e.value){
                var rec = this.EditorStore.getAt(e.row); 
                rec.set("productid",e.value);
                rec.set("pid",this.itemEditorStore.getAt(i).get("pid")); 
                rec.set("quantity",0);
                rec.set("costcenter",this.itemEditorStore.getAt(i).get("costcenter")); 
                rec.set("itemdescription",this.itemEditorStore.getAt(i).get("productname"));
                rec.set("desc",this.itemEditorStore.getAt(i).get("desc"));
                rec.set("packaging",this.itemEditorStore.getAt(i).get("packaging"));
                rec.set("packagingid",this.itemEditorStore.getAt(i).get("packagingid"));
                if(!Wtf.account.companyAccountPref.UomSchemaType){
                    rec.set("uom",this.itemEditorStore.getAt(i).get("uomname"));
                }else{
                    rec.set("uom",this.itemEditorStore.getAt(i).get("orderinguomname"));
                }
                //                rec.set("uom",this.itemEditorStore.getAt(i).get("orderinguomname"));
                rec.set("uomname",this.itemEditorStore.getAt(i).get("orderinguomname"));
                rec.set("stockuom",this.itemEditorStore.getAt(i).get("uomname"));
                rec.set("orderinguomname",this.itemEditorStore.getAt(i).get("orderinguomname"));
                rec.set("transferinguomname",this.itemEditorStore.getAt(i).get("transferinguomname"));
                rec.set("orderToStockUOMFactor",this.itemEditorStore.getAt(i).get("orderToStockUOMFactor"));
                rec.set("stockuomname",this.itemEditorStore.getAt(i).get("uomname"));
                rec.set("uomid",this.itemEditorStore.getAt(i).get("uomid"));
                rec.set("isBatchForProduct",this.itemEditorStore.getAt(i).get("isBatchForProduct"));
                rec.set("isSerialForProduct",this.itemEditorStore.getAt(i).get("isSerialForProduct"));
                rec.set("isRowForProduct",this.itemEditorStore.getAt(i).get("isRowForProduct"));
                rec.set("isRackForProduct",this.itemEditorStore.getAt(i).get("isRackForProduct"));
                rec.set("isBinForProduct",this.itemEditorStore.getAt(i).get("isBinForProduct"));
                rec.set("stockDetails",this.itemEditorStore.getAt(i).get("stockDetails"));
                rec.set("uomschematype",this.itemEditorStore.getAt(i).get("uomschematype"));
                rec.set("ismultipleuom",this.itemEditorStore.getAt(i).get("ismultipleuom"));
                rec.set("defaultFromLocQty",this.itemEditorStore.getAt(i).get("defaultFromLocQty"));
                rec.set("defultFromLocationID",this.itemEditorStore.getAt(i).get("defultFromLocationID"));
                rec.set("defultToLocationID",this.itemEditorStore.getAt(i).get("defultToLocationID"));
                rec.set("defultFromLocationName",this.itemEditorStore.getAt(i).get("defultFromLocationName"));
                rec.set("defultToLocationName",this.itemEditorStore.getAt(i).get("defultToLocationName"));
                
                if(rec.get("orderinguomname") == "" ){
                    this.EditorGrid.getView().getRow(e.row).style = "background-color :pink";
                }
                //                alert(this.itemEditorStore.getAt(i).get("productid"));
                
                if(!Wtf.account.companyAccountPref.UomSchemaType){
                    this.getProductBaseUOMRate(e.value,this.itemEditorStore.getAt(i).get("uomid"),e.row);
                }else{
                    this.loadPackagingStore(this.itemEditorStore.getAt(i).get("productid"));
                }
            }
        }
        var alreadyAdded = false;
        for(i=0;i<this.EditorStore.getCount();i++){
            if(i != e.row && this.EditorStore.getAt(i).get("productid") == e.value){
                alreadyAdded = true;
                var blankrec = this.EditorStore.getAt(e.row);
                blankrec.reject();
                blankrec.set("productid","");
                blankrec.set("pid","");
                blankrec.set("uomname","");
                blankrec.set("productname","");
                break;
            }
        }
        if(alreadyAdded){
            WtfComMsgBox(["Failure", "Product is already added in grid."],3);
        }else if(e.row == this.EditorStore.getCount()-1){
            this.addRec();
        }
        if(Wtf.account.companyAccountPref.UomSchemaType){
            for(var i=0;i<this.uomStore.getCount();i++){
                var uomStoreRec = this.uomStore.getAt(i);
                if(uomStoreRec.get("id")== e.value){
                    var rec1 = this.EditorStore.getAt(e.row); 
                    rec1.set("orderToStockUOMFactor",uomStoreRec.get("factor"));
                    rec1.set("orderinguomname",uomStoreRec.get("name"));

                }
            }
        }else{
            var idx = this.uomCombo.store.find(this.uomCombo.valueField, e.value);
            if(idx != -1){
                var rec = this.uomCombo.store.getAt(idx);
                var valueStr = rec.get(this.uomCombo.displayField);
                var rec1 = this.EditorStore.getAt(e.row); 
                rec1.set("orderinguomname",valueStr);
            }
        }
        if(e.field =='uom'&&!Wtf.account.companyAccountPref.UomSchemaType) {
             
            this.getProductBaseUOMRate(this.EditorStore.getAt(e.row).get("productid"),this.EditorStore.getAt(e.row).get("uom"),e.row);
        }
        
        this.parent.fillDefualtLocationValues();
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
    },
    loadPackagingStore:function(productid){
    //        this.packagingStore.load({
    //            params:{    
    //                productId:productid
    //            }
    //        },this);
    },
    loadUom:function(e,pId,pack,colindex){
        if(Wtf.account.companyAccountPref.UomSchemaType){
            var rec=e.record;
            {
                this.uomStore.load({
                    params:{
                        packagingId:pack
                    }
                });
            
                this.packagingStore.load({
                    params:{    
                        productId:pId
                    }
                },this);
            }
        }else{
            this.uomStore.load({
                params:{
                    doNotShowNAUomName:true
                }
            });
        }
    },
    getProductBaseUOMRate:function(pid,uomid,rowno){
        //        prorec = this.EditorStore.getAt(productComboIndex);
        if(pid!=undefined&&uomid!=undefined)
        {
            Wtf.Ajax.requestEx({
                url:  'INVPackaging/getUOMSchemaList.do',
                params: {
                    productId:pid,
                    currentuomid:uomid,
                    uomnature:"Stock"
                }
            },
            this,
            function(action, response){
                if(action.success == true){
                    var baseuomrate=action.data[0].baseuomrate;
                    this.EditorStore.getAt(rowno).set("confactor",baseuomrate)
                }else{
                    WtfComMsgBox(["Error", "Some error has occurred."],0);
                    return false;
                }
            },
            function(){
                WtfComMsgBox(["Error", "Some error has occurred."],0);
                return false;
            });  
        }
    },
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    cellClick :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        if(fieldName=="uom"&& !record.get("uomschematype")&&!Wtf.account.companyAccountPref.UomSchemaType){
            return false;
        }
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchEnable && !isSerialEnable;
        var itemId=record.get("productid");
        var packaging=record.get("packagingid");
        var itemCode=record.get("pid");
        var quantity=record.get("quantity");
        var orderToStockUOMFactor=Wtf.account.companyAccountPref.UomSchemaType?record.get("orderToStockUOMFactor"):record.get("confactor");
        var orderUOMName=record.get("orderinguomname");
        var stockUOMName=record.get("stockuomname");
        var fromStoreId = this.parent.fromstoreCombo.getValue();
        var toStoreId = this.parent.tostoreCombo.getValue();
        this.loadUom(e,itemId,packaging,columnIndex);
        if(fieldName == undefined){
            if(fromStoreId == undefined || toStoreId==undefined || fromStoreId == "" || toStoreId==""){
                WtfComMsgBox(["Warning", "Please Select Store."],0);
                return false;
            }
            if(itemId=="" || itemId==undefined){
                WtfComMsgBox(["Warning", "Please Select Product first."],0);
                return false; 
            }
            if(record.data.quantity==0){
                WtfComMsgBox(["Warning", "Please Fill quantity."],0);
                return false;
            }
            if(record.data.quantity > 0){

                var filterJson='[';
                filterJson+='{"warehouse":"'+fromStoreId+'","productid":"'+itemId+'"},';
                filterJson=filterJson.substring(0,filterJson.length-1);
                filterJson+="]";    
                
                Wtf.Ajax.requestEx(
                {
                    url:  'ACCInvoice/getBatchRemainingQuantity.do',
                    params: {
                        batchdetails: filterJson
                    }
                },
                this,
                function(action, response){
                    if(action.success == true){
                        var availQty=action.quantity;
                        var maxAllowed= Math.floor(availQty/orderToStockUOMFactor);
                        
                        if(!isNegativeAllowed && availQty <= 0){
                            WtfComMsgBox(["Warning", "Quantity is not available for selected item."],0);
                            return false;
                        }
                        
                        if(isNegativeAllowed || availQty >= (quantity * orderToStockUOMFactor )){
                            //                            this.showLocationBatchSerialSelectWindow(itemId,itemCode,isBatchEnable,isSerialEnable,quantity,orderToStockUOMFactor,orderUOMName,stockUOMName,rowIndex);
                            this.showStockDetailWindow(record);
                        }else{
                            WtfComMsgBox(["Alert", "Issue quantity cannot be greater than available quantity.<br/>You can issue maximum <b>"+maxAllowed+" </b>quantity."],3);
                            return;
                        }
                        
                    }else{
                        WtfComMsgBox(["Error", "Some error has occurred."],0);
                        return false;
                    }
                    
                },
                function(){
                    WtfComMsgBox(["Error", "Some error has occurred."],0);
                    return false;
                }
                );
               
               
            }
        }else if(e.getTarget(".add-gridrow")){
                this.showProductGrid();
        }
    },
    addProductList:function(){
        return "<div class='pwnd add-gridrow' wtf:qtip=\"Click to add products\"></div>";
    },
     showProductGrid : function() {
       
        this.productSelWin = new Wtf.account.ProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 700,
            title:'Product Selection Window',
            layout : 'fit',
            modal : true,
            resizable : true,
            id:this.id+'ProductSelectionWindow',
            moduleid:Wtf.Acc_Stock_Request_ModuleId,
            modulename:"GOODS_ISSUE",
            invoiceGrid:this.EditorGrid,
            parentCmpID:this,
            isFromInventorySide:true,
            isStoreLocationEnable:true,
            warehouseId:this.parent.fromstoreCombo.getValue(),
            isCustomer : false
        });
        this.productSelWin.show();
        this.productSelWin.on("close",function(){
           this.parent.fillDefualtLocationValues();
        },this);
    },
    checkAndRemoveDuplicateProductFromGrid: function (rowIndexToCheck) {
        var store = this.EditorGrid.getStore();
        var totalRec = store.getCount();
        if (rowIndexToCheck < totalRec) {
            var productToCheck = store.getAt(rowIndexToCheck).get("productid");
            for (var i = 0; i < totalRec; i++) {
                if (i == rowIndexToCheck) {
                    continue;
                }
                var curRowProductId = store.getAt(i).get("productid");
                if (curRowProductId === productToCheck) {
                    store.remove(store.getAt(rowIndexToCheck));
                    this.ArrangeNumberer(rowIndexToCheck);
                    break;
                }
            }
        }
    },
    showStockDetailWindow : function (record){
        var itemId=record.get("productid");
        var itemCode=record.get("pid");
        var quantity=record.get("quantity");
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchEnable && !isSerialEnable;
        var isRackEnable = record.get("isRackForProduct");
        var isRowEnable = record.get("isRowForProduct");
        var isBinEnable = record.get("isBinForProduct");
        var orderToStockUOMFactor=Wtf.account.companyAccountPref.UomSchemaType?record.get("orderToStockUOMFactor"):record.get("confactor");
        var orderingUomName=record.get("orderinguomname");
        var stockUOMName=record.get("uomname");
        var fromStoreId = this.parent.fromstoreCombo.getValue();
        var fromStoreName=this.parent.fromstoreCombo.getRawValue();
        var toStoreId = this.parent.tostoreCombo.getValue();
        var toStoreName=this.parent.tostoreCombo.getRawValue();
        var maxQtyAllowed=orderToStockUOMFactor * quantity;
        
        var winTitle = WtfGlobal.getLocaleText("acc.stockrequest.StockDetailforStockTransfer");
        var winDetail = String.format('Select Stock details for stock transfer  <br> <b>Product :</b> {0}<br> <b>Issuance Store :</b> {1}<br> <b>Collection Store :</b> {2}<br> <b>Quantity :</b> {3} {4} ( {5} {6} )', itemCode, fromStoreName, toStoreName, quantity, orderingUomName, maxQtyAllowed, stockUOMName);
        
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
            isNegativeAllowed: isNegativeAllowed,
            GridStoreURL:"INVStockLevel/getStoreProductWiseDetailList.do",
            StockDetailArray:record.get("stockDetails"),
            moduleid:Wtf.Acc_Stock_Request_ModuleId,
            modulename:"GOODS_ISSUE",
            type:this.type,
            DataIndexMapping:{
                serials:"serialNames"
            },
            buttons:[{
                text:"Save",
                handler:function (){
                    if(this.detailWin.validateSelectedDetails()){
                        var detailArray = this.detailWin.getSelectedDetails();
                        record.set("stockDetails","");
                        record.set("stockDetails",detailArray);
                        record.set("stockDetailQuantity",quantity);
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
    showLocationBatchSerialSelectWindow : function (itemId,itemCode,isBatchForProduct,isSerialForProduct,quantity,orderToStockUOMFactor,orderUOMName,stockUOMName,rowIndex){
        
        this.fromStoreId = this.parent.fromstoreCombo.getValue();
        this.toStoreId = this.parent.tostoreCombo.getValue();
        this.fromStoreName=this.parent.fromstoreCombo.getRawValue();
        this.toStoreName=this.parent.tostoreCombo.getRawValue();
        this.itemId=itemId;
        this.itemcode=itemCode;
        this.isBatchForProduct=isBatchForProduct;
        this.isSerialForProduct=isSerialForProduct;
        this.quantity=quantity;
        this.orderToStockUOMFactor=orderToStockUOMFactor;
        this.orderUOMName=orderUOMName;
        this.stockUOMName=stockUOMName;
        this.currentRowNo=rowIndex;
        this.maxQtyAllowed=orderToStockUOMFactor * quantity;
        
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
        
        this.serialCmbRecord = new Wtf.data.Record.create([
        {
            name: 'serialnoid'
        },        

        {
            name: 'serial'
        }]);

        this.serialCmbStore = new Wtf.data.Store({
            url:  'INVStockLevel/getProductBatchWiseSerialList.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.serialCmbRecord)
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
        
        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            decimalPrecision:4,//Wtf.companyPref.quantityDecimalPrecision,
            allowNegative:false
        })
        
        this.EditorColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:"Product ID",
                dataIndex:"itemcode",
                hidden:true
            },
            {
                header:"From Location ID",
                dataIndex:"fromlocationid",
                hidden:true
            //editor:this.locCmb,
            //renderer:this.getComboRenderer(this.locCmb)
                
            },
            {
                header:"From Location",
                dataIndex:"fromlocationname",
                hidden:false
            //editor:this.locCmb,
            //renderer:this.getComboRenderer(this.locCmb)
                
            },
            {
                header:"Batch",
                dataIndex:"batch",
                hidden : (this.isBatchForProduct==true) ? false :true
                
            },
            {
                header:"Available Quantity",
                dataIndex:"availableQty"
            },
            {
                header:"To Location *",
                dataIndex:"tolocationid",
                hidden:false,
                editor:this.locCmb,
                renderer:this.getComboRenderer(this.locCmb)
                
            },
            {
                header:"Quantity *",
                dataIndex:"quantity",
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
                header:"Serial *",
                dataIndex:"serial",
                editor:this.serialCmb,
                hidden : (this.isSerialForProduct==true) ? false :true,
                renderer:this.getComboRenderer(this.serialCmb)
            }
            
            ]);
        
            
        this.locationGridStore = new Wtf.data.SimpleStore({
            fields:['itemid','itemcode','fromlocationid','fromlocationname','tolocationid','isBatchForProduct',
            'isSerialForProduct','batch','serial','quantity','availableQty'],
            pruneModifiedRecords:true
        });
          
        this.locationGridStore.on('load', function(){
            this.locationSelectionGrid.getView().refresh();
        },this);
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        var callURL = "";
        var caseType="";
        
        if(this.isBatchForProduct == true && this.isSerialForProduct==true){
            callURL="INVStockLevel/getStoreProductWiseLocationBatchList.do?";
            caseType=1;
        }else if(this.isBatchForProduct == true && this.isSerialForProduct==false){
            callURL="";
            caseType=2;
        }else if(this.isBatchForProduct == false && this.isSerialForProduct==true){
            callURL="";
            caseType=3;
        }else{
            callURL="INVStockLevel/getStockByStoreProduct.do";
            caseType=4;
        }
        this.locationGridStoreArr=[];
            
        Wtf.Ajax.requestEx({
            url:callURL,
            params: {
                toStoreId: this.fromStoreId,
                productId: this.itemId
            }
        },this,
        function(res,action){
            if(res.success==true){
                var totalRec=res.data.length;
 
                //     'itemid','itemcode','fromlocationid','fromlocationname','tolocationid','isBatchForProduct',
                //     'isSerialForProduct','batch','serial','quantity','availableQty'
                        
                //dummy casetype 4 data : 
                // {"locationId":"ff80808149c1a2660149c2aa85e20006","locationName":"qwe","availableQty":100,"productId":"PR/ID000012"}

                //dummy casetype 1 data :
                // {"batchName":"mmob","locationId":"ff8080814a2841a9014a28ba32de000c","batchId":"afee1e00-62f5-44a8-950c-977a469ff77a",
                // "locationName":"123","availableQty":2,"productId":"402880094a7066db014a71c4f4cc0005"}                   
               
                if(caseType==1){
                    this.locationGridStore.removeAll();
                    for(var i=0;i<totalRec;i++){
                            
                        this.locationGridStoreArr.push([this.itemId,this.itemcode,res.data[i].locationId,
                            res.data[i].locationName,"",this.isBatchForProduct,this.isSerialForProduct,
                            res.data[i].batchName,"",0,res.data[i].availableQty]);
                        
                    }
                    this.locationGridStore.loadData(this.locationGridStoreArr);
                }
                if(caseType==4){
                    this.locationGridStore.removeAll();
                    for(var i=0;i<totalRec;i++){
                            
                        this.locationGridStoreArr.push([this.itemId,this.itemcode,res.data[i].locationId,
                            res.data[i].locationName,"",this.isBatchForProduct,this.isSerialForProduct,
                            "","",0,res.data[i].availableQty]);
                           
                    }
                    this.locationGridStore.loadData(this.locationGridStoreArr);   
                }
                  
            }else{
                WtfComMsgBox(["Error", "Error occurred while fetching data."],0);
                return;
            }
                
        },
        function() {
            WtfComMsgBox(["Error", "Error occurred while processing"],1);
        }
        );   
        ///////////////////////////////////////////////////////////////////////////////////////////////// 
        
      
        this.locationSelectionGrid = new Wtf.grid.EditorGridPanel({
            cm:this.EditorColumn,
            region:"center",
            id:"editorgrid2sd",
            autoScroll:true,
            store:this.locationGridStore,
            viewConfig:{
                forceFit:true,
                emptyText:"No Data to Show."
            },
            clicksToEdit:1
        });
        
        this.locationSelectionGrid.on("beforeedit",this.beforeEdit,this);
        this.locationSelectionGrid.on("afteredit",this.afterEdit,this);
        
        this.winTitle="Select Quantity,Location"+(this.isBatchForProduct ? ",Batch" : "")+(this.isSerialForProduct ? ",Serial" : "");
        this.winDescriptionTitle= this.winTitle+" for following item<br/>";
        this.winDescription="<b>Product Code : </b>"+this.itemcode+"<br/>"
        +"<b>From Store : </b>"+this.fromStoreName+"<br/>"
        +"<b>To Store : </b>"+this.toStoreName+"<br/>" 
        +"<b>Quantity : </b>"+ this.quantity +" "+this.orderUOMName+" ( "+ this.quantity*this.orderToStockUOMFactor +" "+ this.stockUOMName +" )<br/>" ;
        
        this.locationSelectionWindow = new Wtf.Window({
            title : this.winTitle,
            modal : true,
            iconCls : 'iconwin',
            minWidth:75,
            width : 950,
            height: 500,
            resizable :true,
            scrollable:true,
            id:"locationwindow",
            buttonAlign : 'right',
            layout : 'border',
            items :[{
                region : 'north',
                height : 110,
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
                        items: this.locationSelectionGrid 
                    }
                       
                    ]
                }]
            }],
            buttons :[{
                text : 'Submit',
                iconCls:'pwnd save',
                scope : this,
                handler: function(){  
                    
                    var isValid=this.validateFilledData();
                    
                    if(isValid==true){
                        var jsonData=this.makeJSONData();
                        var rec=this.EditorStore.getAt(this.currentRowNo);
                        rec.set("stockDetails",jsonData);
                        Wtf.getCmp('locationwindow').close();  
                    }
                }
            },{
                text : 'Cancel',
                scope : this,
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                minWidth:75,
                handler : function() {
                    Wtf.getCmp('locationwindow').close();
                }
            }]
        }).show();
        
        Wtf.getCmp("locationwindow").doLayout();
            
        Wtf.getCmp("locationwindow").on("close",function(){
            //this.showAddToItemMasterWin();
            },this);
            
    },
    validateFilledData : function(){
        
        var recs=this.locationSelectionGrid.getStore().getModifiedRecords();
        
        var isSerialForProduct=false;
        var quantity=0;
        var serial="";
        var tolocationid="";   
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                isSerialForProduct=this.isSerialForProduct; 
                quantity=recs[k].get("quantity");
                serial=recs[k].get("serial");
                tolocationid=recs[k].get("tolocationid");
                
                if(quantity > 0 && tolocationid==""){
                    WtfComMsgBox(["Warning", "Please Select To Location."],0);
                    return false;
                }
                
                if(quantity == 0 && tolocationid != "" && tolocationid != undefined){
                    WtfComMsgBox(["Warning", "Please enter quantity for selected To Store."],0);
                    return false;
                }
                
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
            return true;
        }else{
            WtfComMsgBox(["Warning", "Please Select To Location."],0);
            return false;
        }
        
    },
    makeJSONData : function(){
        var recs=this.locationSelectionGrid.getStore().getModifiedRecords();
        var jsondata = "";
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                var batch=recs[k].get("batch");
                var fromlocationid=recs[k].get("fromlocationid");  
                var quantity=recs[k].get("quantity");
                var serial=recs[k].get("serial");
                var tolocationid=recs[k].get("tolocationid");
                
                jsondata += "{'fromLocationId':'" + fromlocationid  + "',";
                jsondata += "'toLocationId':'" + tolocationid + "',";
                jsondata += "'batchName':'" + batch + "',";
                jsondata += "'serialNames':'" + serial + "',";
                jsondata += "'quantity':'" + quantity + "'},";
            }
           
        }
        
        var trmLen = jsondata.length - 1;
        var finalStr = jsondata.substr(0,trmLen);
        return finalStr;
    },
    
    makeJSONDataForStockDetails : function(recs){
        
        var detailArray = [];
        
        if(recs){
                      
            var batch=recs.get("batch");
            var locationid=recs.get("fromlocationid");  
            var quantity=recs.get("quantity");
            var serial=recs.get("serial");
                
               
            var jsondata = {};
            jsondata.fromLocationId= locationid;
            //            jsondata += "'toLocationId':'" + tolocationid + "',";
            jsondata.batchName=batch;
            jsondata.serialNames=serial;
            jsondata.quantity=quantity;
            detailArray.push(jsondata)
            
           
        }
        return detailArray;
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
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var total=store.getCount();
            if(rowindex==total-1){
                return;
            }
            Wtf.MessageBox.confirm('Warning', 'Are you sure you want to remove this item?', function(btn){
                if(btn!="yes") return;
                store.remove(store.getAt(rowindex));
                this.ArrangeNumberer(rowindex);
            //                this.fireEvent('datachanged',this);
            }, this);
        }
    },
    
    beforeEdit :function(e){
        
        var rec=e.record;
        
        if(e.record.data.isBatchForProduct == false && (e.field =='batch')) {
            return false;
        }
        if(e.record.data.isSerialForProduct == false && (e.field =='serial')) {
            return false;
        }
        if(e.field =='serial' &&  e.record.data.quantity==0) {
            WtfComMsgBox(["Warning", "Please Fill quantity first."],0);
            return false;
        }
        if(e.field =='serial' &&  e.record.data.quantity > 0) {
            this.serialCmbStore.load({
                params:{
                    batch:rec.data.batch,
                    productid :rec.data.itemid,
                    storeid: this.fromStoreId,
                    locationid: rec.data.fromlocationid
                }
            });
        }
            
    },
    
    afterEdit :function(e){
        if(e.field =='quantity') {
            if(e.record.data.quantity > e.record.data.availableQty){
                var rec=e.record;
                rec.set("quantity",0);
                return false;
            }
            
            if(e.record.data.quantity==0 && e.record.data.isSerialForProduct == true){  //if  edited afterwards case
                var rec=e.record;
                rec.set("serial","");
                return false;
            }
            
            var totalRec=this.locationGridStore.getTotalCount();
            var totalIssueQty= this.maxQtyAllowed ;
            var enteredTotalQty=0;
            
            for(var i=0; i < totalRec;i++){
                var currentRec=this.locationGridStore.getAt(i);
                enteredTotalQty += currentRec.get("quantity");
            }
            if(enteredTotalQty > totalIssueQty){
                WtfComMsgBox(["Warning", "Entered total quantity cannot be greater than selected quantity."],0);
                var record=e.record;
                record.set("quantity",0);
                return false;
            }
            
        }
        
        if(e.field =='serial' && (e.record.data.serial !="" && e.record.data.serial !=undefined)) {
            var rowRec=e.record;
            var maxSerialSelectionAllowed=rowRec.data.quantity;
            var selectedSerialList=e.record.data.serial;
            var separatedSerialArr=selectedSerialList.split(",");
            if(separatedSerialArr.length > maxSerialSelectionAllowed){
                rowRec.set("serial","");
                //WtfComMsgBox(["Warning", "You can select maximum "+maxSerialSelectionAllowed+" Serials from list."],0);
                WtfComMsgBox(["Warning", "Quantity and selected serial numbers count must be same"],0);
                return false;
            }
        
        }
    },
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(Wtf.Inventory_ModuleId, rec, this.EditorGrid);
    }
    
});


function printoutIssueNote(IssueNoteNo){

    var URL="INVGoodsTransfer/getStockRequestDetailBySequenceNo.do"; 

    var printTitle = "Goods Issue Note";
    var pcase = "Issue";
    var printflg = "printissueNote";
  
    
    Wtf.Ajax.requestEx({
        url:URL,
        params: {
            sequenceNo : IssueNoteNo,
            moduleName : "ISSUE_NOTE"
    }
    },
    this,
    function(result, req){
    
        var msg=result.msg;
        var title="Error";
        if(result.success){
    
            var rs=result.data;

            var cnt = rs.length;
   
            var isBatchDataPresent=false;
            var isSerialDataPresent=false;
            var isLocationDataPresent=false;
            
            for(var i=0;i<cnt;i++){
                
                var saDetail=result.data[i].stockDetails;
                
                for(var sad=0; sad < saDetail.length; sad++){
                    
                    if(saDetail[sad].batchName != undefined && saDetail[sad].batchName != "" && isBatchDataPresent == false){
                        isBatchDataPresent=true;
}

                    if(saDetail[sad].issuedSerials != undefined && saDetail[sad].issuedSerials != "" && isSerialDataPresent == false){
                        isSerialDataPresent=true;
                    }

                    if(saDetail[sad].issuedLocationName != undefined && saDetail[sad].issuedLocationName != "" && isLocationDataPresent == false){
                        isLocationDataPresent=true;
                    }
                }

            }
    
           
            var htmlString = "<html>"
            + "<title>" + printTitle + "</title>"
            + "<head>"
            + "<STYLE TYPE='text/css'>"
            + "<!--"
            + "TD{font-family: Arial; font-size: 10pt;}"
            + "--->"
            + "</STYLE>"
            + "</head>"
            + "<body>"
            + "<h2 align = 'center' style='font-family:arial; padding: 2%;'> " + printTitle + " </h2>"
            + "<div style='font-family:arial;font-size:10pt;text-align: left;'>"
            + "<span style='float:left; margin-left: 3%;'>"
            +  (printflg ==="printorder" || printflg === "printissueNote" || printflg === "interstore"? "" : "<b>Order ID : </b>" + result.data[0].transfernoteno + "<br><br>") 
            + "<b>" + pcase+  " Note No : </b>" + result.data[0].transfernoteno + "<br><br>"
            + (printflg ==="printorder" || printflg === "printissueNote" || printflg === "interstore" ? "<b>Date : </b>" : "<b>Shipping Date : </b>")+ (printflg === "interstore"? result.data[0].date :(printflg ==="printorder" || printflg === "printissueNote" || printflg === "interstore" ?result.data[0].date :new Date(result.data[0].issuedOn.replace(/[-]/gi, '/')).format('Y-m-d')))
            + "</span>"
            // + "<span style='padding: 1%; float: right;'><img src = " + "cnxt" + " style='max-height: 200px; max-width: 200px;'></span>"
            + "</div>"
            + "<div style='margin-top:140px;width: 95%;'>";
            if (printflg ==="accept") {
            //                htmlString += "<span style='margin-left:1%; border-left:solid 1px black;border-bottom:solid 1px black;border-top:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: left;text-align: left;'><b>To : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" +selected[0].get("tostorename")+ "</br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected[0].get("tostoreadd") + "</br>"
            //                + "&nbsp;&nbsp;&nbsp;&nbsp;<b>Tel : </b>" + selected[0].get("tostorephno") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;<b>Fax : </b>" +selected[0].get("tostorefax") + "</br></span>"
            //                + "<span style='border-bottom:solid 1px black;border-top:solid 1px black;border-left:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: left;text-align: left;'><b>From : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected[0].get("fromstorename") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" + selected[0].get("fromstoreadd")+ "</br>"
            //                + "&nbsp;&nbsp;&nbsp;&nbsp;<b>Tel : </b>" + selected[0].get("fromstorephno") + "</br>&nbsp;&nbsp;&nbsp;&nbsp;<b>Fax : </b>" + selected[0].get("fromstorefax") + "</br></span>";
            } else {
                htmlString += "<span style='margin-left:1%; border-left:solid 1px black;border-bottom:solid 1px black;border-top:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: right;text-align: left;'><b>To : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" +result.data[0].tostorename + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" +result.data[0].tostoreadd
                + "</br></span>"
                + "<span style='border-bottom:solid 1px black;border-top:solid 1px black;border-left:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: left;text-align: left;'><b>From : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" + result.data[0].fromstorename + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" + result.data[0].fromstoreadd
                + "</br></span>";
            }
            htmlString += "</div><br/><br style='clear:both'/><br/>";
            var pgbrkstr1 = "<DIV style='page-break-after:always'></DIV>";
            
            if (i != 0) {
                htmlString += "<br/><br/></br>";
            }
            htmlString += "<center>";
            htmlString += "<table cellspacing=0 border=1 cellpadding=2 width='95%'>";
            if (printflg === "printissueNote") {
                htmlString += "<tr>"
                +"<th>S/N</th>"
                +"<th>Product ID</th>"
                +"<th>Product Name</th>"
                //                    +(printflg === "printissueNote" || printflg === "interstore"?"<th> HS Code</th>":"")
                +"<th>Uom</th>"
                +"<th>Quantity</th>";
                // htmlString += (isLocationDataPresent == true ? "<th>Location</th>" : "" );
                htmlString += "<th>From Location</th>";
                //htmlString += (printflg === "interstore" ? "<th>To Location</th>" : "");
                htmlString += (isBatchDataPresent == true ? "<th>Batch No.</th>"  : "" );
                htmlString += (isSerialDataPresent == true ? "<th>Issued Serial No.</th>" : "" );
                //htmlString += (isSerialDataPresent == true &&  printflg === "interstore" ? "<th>Collected Serial No.</th>" : "" );
                htmlString += "<th>Remark</th>";
                
                /*
                 * Line Level Column
                 */
                var linedata = [];
                linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModel[Wtf.Acc_Stock_Request_ModuleId]);
                for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
                    if (linedata[lineFieldCount].header != undefined && linedata[lineFieldCount].header != "") {
                        htmlString += "<th>" + linedata[lineFieldCount].header + "</th>";
                    }
                }
                htmlString += "</tr>";
            }
            var count=1;
            
            for(var i=0;i<cnt;i++){
        
                var saDetail=result.data[i].stockDetails;
                var batchDtl="";
                var issuedSerialDtl="";
                var collectedSerialDtl="";
                var fromLocationDtl="";
                var toLocationDtl="";
                for(var sad=0; sad < saDetail.length; sad++){
                    
                    batchDtl += saDetail[sad].batchName ;
                    batchDtl += (((sad != saDetail.length-1 && saDetail[sad].batchName=="") || sad == saDetail.length-1) ? "" : "<hr/>");
                    
                    issuedSerialDtl+= saDetail[sad].issuedSerials ;
                    issuedSerialDtl+= (((sad != saDetail.length-1 && saDetail[sad].issuedSerials=="") || sad == saDetail.length-1) ? "" : "<hr/>");
            
                    collectedSerialDtl+= saDetail[sad].collectedSerials ;
                    collectedSerialDtl+= (((sad != saDetail.length-1 && saDetail[sad].collectedSerials=="") || sad == saDetail.length-1) ? "" : "<hr/>");
                    
                    fromLocationDtl+= saDetail[sad].issuedLocationName ;
                    fromLocationDtl+= (((sad != saDetail.length-1 && saDetail[sad].issuedLocationName=="") || sad == saDetail.length-1) ? "" : "<hr/>");
            
                    toLocationDtl+= saDetail[sad].collectedLocationName ;
                    toLocationDtl+= (((sad != saDetail.length-1 && saDetail[sad].collectedLocationName=="") || sad == saDetail.length-1) ? "" : "<hr/>");
                }
        
        
                if (printflg === "printorder"  || printflg === "printissueNote"  || printflg === "interstore") {
                    htmlString += "<tr>"
                    +"<td align='center'>" + count + "&nbsp;</td>"
                    +"<td align='center'>" + result.data[i].itemcode + "&nbsp;</td>"
                    +"<td align='center'>" +result.data[i].itemname + "&nbsp;</td>"
//                        +(printflg === "printissueNote" || printflg === "interstore"?"<td align='center'>" + (result.data[i].hscode == undefined ? "": result.data[i].hscode)+ "&nbsp;</td>":"")
                    +"<td align='center'>" +result.data[i].name + "&nbsp;</td>"
                    +"<td align=right>" + result.data[i].nwquantity + "&nbsp;</td>"
                    +"<td align='center'>" + fromLocationDtl + "</td>"+
                    // (printflg === "interstore" ? "<td align='center'>" + toLocationDtl + "</td>" : "" )+
                    (isBatchDataPresent == true ? "<td align='center'>" + batchDtl+ "</td>"  : "" )+
                    (isSerialDataPresent == true ? "<td align='center'>" + issuedSerialDtl + "</td>" : "" ) +
                    //(isSerialDataPresent == true &&  printflg === "interstore" ? "<td align='center'>" + collectedSerialDtl + "</td>" : "" ) +
            
                    "<td align='center'>" + result.data[i].remark + "&nbsp;</td>";
                    
                    /*
                     * Line Level Custom data
                     */
                    for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
                        if (linedata[lineFieldCount].header != undefined && result.data[i][linedata[lineFieldCount].dataIndex] != undefined) {
                            htmlString += "<td>" + result.data[i][linedata[lineFieldCount].dataIndex] + "</td>";
                        } else {
                            htmlString += "<td>" + "" + "</td>";
                        }
                    }
                    htmlString += "</tr>";
                }
                
                count++;
        
            }

            htmlString += "</table>";
            htmlString += "</center><br><br>";
            if (i != cnt - 1) {
                htmlString += pgbrkstr1;
            }

            htmlString += 
            //        "<div>"
            //    + "<span style='width:270px; padding: 3%; float: left;text-align:left'><b>Prepared By </b><br></br>Sign:</br>Name:&nbsp;&nbsp;&nbsp;&nbsp;" +createdBy + "</br></br></span>"
            //    + "<span style='width:270px; padding: 3%; float: right;text-align:left'><b>Collected By </b></br></br>Sign:</br>Name:&nbsp;&nbsp;&nbsp;&nbsp;" + collectedBy + "</br></br></span>"
            //    + "</div><br style='clear:both'/>"
            "<div style='float: right; padding-top: 3px; padding-right: 5px;'>"
            + "<button id = 'print' title='Print Note' onclick='window.print();' style='color: rgb(8, 55, 114);' href='#'>Print</button>"
            + "</div>"
            + "</div>"
            + "</body>"
            + "</html>";
            +"<style>@media print {button#print{display:none;}}</style>";
            var disp_setting="toolbar=yes,location=no,";
            disp_setting+="directories=yes,menubar=yes,";
            disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";

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
            docprint.document.write(htmlString);
            docprint.document.write('</center></body></html>');
            docprint.document.close();
        }
        else if(result.success==false){
            title="Error";
            WtfComMsgBox([title,"Some Error occurred."],0);
            return false;
        }
    },
    function(result, req){
        WtfComMsgBox(["Failure", "Some Error occurred."],3);
        return false;
    });

   
}

function printDocumentAfterSave(moduleid, itemObj) {
    var billid = itemObj.billid;
    var billno = itemObj.billno;
    var templateCount = 0;
    if (GlobalCustomTemplateList[moduleid]) {
        templateCount = GlobalCustomTemplateList[moduleid].length;
    }
    var templateid = "";
    var templates = [];
    
    if (templateCount == 1) {
        var template = GlobalCustomTemplateList[moduleid][0];
        templateid = template.templateid;
    } else if (templateCount > 1) {
        templates = GlobalCustomTemplateList[moduleid];
    }
    
    if (templateCount == 1) { // If only one template present then print directly with that template
        var paramObj = {
            id: templateid,
            transactionno: billno,
            recordid: billid,
            isAutoPopulate: true
        }
        printRecordTemplate('print', paramObj, moduleid);
    } else if (templateCount > 1) { // If more than 1 templates available then show combo
        showTemplateListWin(templates, billno, billid, moduleid);
    } else {
        defaultHTMLPrint(moduleid, billno);
    }
}

function showTemplateListWin(templates, transactionno, recordid, moduleid) {
    // Window for selecting template from combo for printing
    var templatelist = [];
    for (var cnt = 0; cnt < templates.length; cnt++) {
        var temparr = [];
        temparr.push(templates[cnt].templateid);
        temparr.push(templates[cnt].templatename);
        templatelist.push(temparr);
    }
    // Store for template combo
    var templateStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data: templatelist
    });
    
    var templateListCombo = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectTemplate"),
        displayField: 'name',
        valueField: 'id',
        store: templateStore,
        labelWidth: 150,
        width: 300,
        mode: 'local',
        triggerAction: 'all',
        emptyText: WtfGlobal.getLocaleText("acc.field.SelectTemplate")//"--Select Template--",
    })
    
    // Window for template selection
    var selectTemplateWin = new Wtf.Window({
        title: WtfGlobal.getLocaleText("acc.field.SelectTemplate"),
        closable: true,
        autoHeight: true,
        bodyStyle: 'padding:5px 5px 5px',
        plain: true,
        modal: true,
        items: [templateListCombo],
        buttons: [
            {
                text: WtfGlobal.getLocaleText("acc.common.print"),
                scope: this,
                handler: function () {
                    var selectedtemplateId = templateListCombo.getValue();
                    if (selectedtemplateId == "") { // If no any template selected then show error message
                        WtfComMsgBox([0, WtfGlobal.getLocaleText("acc.field.PleaseselectTemplateName")], 1);
                        return false;
                    } else {
                        var paramObj = {
                            id: selectedtemplateId,
                            transactionno: transactionno,
                            recordid: recordid,
                            isAutoPopulate: true
                        };
                        printRecordTemplate('print', paramObj, moduleid);
                        selectTemplateWin.close();
                    }
                }
            },
            {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function () {
                    selectTemplateWin.close();
                }
            }
        ]
    });
    selectTemplateWin.show();
}

function printRecordTemplate(printflg, itemObj, moduleid) {
    var transactionno;
    var recordids;
    var isAutoPopulate = itemObj.isAutoPopulate != undefined ? itemObj.isAutoPopulate : false;
    if (isAutoPopulate) {
        transactionno = itemObj.transactionno;
        recordids = itemObj.recordid;
    }
    
    var mapForm = document.createElement("form");
    mapForm.target = "mywindow";
    mapForm.method = "post";
    
    var params = undefined;
    switch (moduleid) {
        case Wtf.Inventory_ModuleId:
            mapForm.action = "ACCExportPrintCMN/exportSingleStockRequestIssue.do";
            params = "myflag=order&transactiono=" + transactionno + "&moduleid=" + Wtf.Inventory_ModuleId + "&templateid=" + itemObj.id + "&recordids=" + recordids + "&filetype=" + printflg + "&isAutoPopulate=" + isAutoPopulate;
            break;
        case Wtf.Acc_Stock_Adjustment_ModuleId:
            mapForm.action = "ACCExportPrintCMN/exportSingleStockAdjustment.do";
            params = "myflag=order&transactiono=" + transactionno + "&moduleid=" + Wtf.Acc_Stock_Adjustment_ModuleId + "&templateid=" + itemObj.id + "&recordids=" + recordids + "&filetype=" + printflg;
            break;
    }
    
    var inputs = params.split('&');
    for (var i = 0; i < inputs.length; i++) {
        var KV_pair = inputs[i].split('=');
        var mapInput = document.createElement("input");
        mapInput.type = "text";
        mapInput.name = KV_pair[0];
        mapInput.value = KV_pair[1];
        mapForm.appendChild(mapInput);
    }
    // If is after saving stock issue then autopopulate print with Document Designer template in new window
    if (isAutoPopulate) {
        var htmlString = "";
        htmlString += "<div style='float: right; padding-top: 3px; padding-right: 5px;'>"
                + "<button id = 'print' title='Print Note' onclick='window.print();' style='color: rgb(8, 55, 114);' href='#'>Print</button>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>"
                + "<style>@media print {button#print{display:none;}}</style>";
        var disp_setting = "toolbar=yes,location=no,";
        disp_setting += "directories=yes,menubar=yes,";
        disp_setting += "scrollbars=yes,width=700, height=600, left=100, top=25";

        var docprint = window.open("", "mywindow", disp_setting);
        docprint.document.open();
        docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
        docprint.document.write('<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
        docprint.document.write('<head><title></title>');
        docprint.document.write('</head><body onLoad="self.print()"><center>');
        docprint.document.body.appendChild(mapForm);
        docprint.document.write(htmlString);
        docprint.document.write('</center></body></html>');
        mapForm.submit();
        docprint.document.close();
    } else { // normal Print Record(s) button print
        document.body.appendChild(mapForm);
        mapForm.submit();
        var myWindow = window.open("", "mywindow", "menubar=1,resizable=1,scrollbars=1");
        var div = myWindow.document.createElement("div");
        div.innerHTML = "Loading, Please Wait...";
        myWindow.document.body.appendChild(div);
    }
    mapForm.remove();
}

function defaultHTMLPrint(moduleid, billno) {
    switch (moduleid) {
        case Wtf.Inventory_ModuleId:
            printoutIssueNote(billno);
            break;
        case Wtf.Acc_Stock_Adjustment_ModuleId:
            printoutSA(billno);
            break;
    }
}
