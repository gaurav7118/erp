Wtf.interLocationTransfer = function (config){
    Wtf.apply(this,config);
    Wtf.interLocationTransfer.superclass.constructor.call(this);
}

Wtf.extend(Wtf.interLocationTransfer,Wtf.Panel,{
    initComponent:function (){
        Wtf.interLocationTransfer.superclass.initComponent.call(this);
        this.getForm();
        this.getItemDetail();

        this.submitBttn=new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: {
                text:"Click to Save"
            },
            id:'interLocationTransferSubmit',
            iconCls:getButtonIconCls(Wtf.etype.save),
            handler:function (){
                if(this.documentNumber.getValue().trim() == "" && this.sequenceFormatNO.getValue()=="NA"){
                    WtfComMsgBox(["Alert", "Please enter valid Document No "],3);
                    return;
                }
                this.saveOnlyFlag = true;
                this.SaveItem();
            },
            scope:this
        });
          
        this.cancelBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),//Cancel Button
            tooltip: {
                text:"Click to Cancel"
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
                if(this.documentNumber.getValue().trim() == "" && this.sequenceFormatNO.getValue()=="NA"){
                    WtfComMsgBox(["Alert", "Please enter valid Document No "],3);
                    return;
                }
                this.saveOnlyFlag = false;
                this.SaveItem(); 
            }
        });

        /*Print Record Button*/
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        var colModArray = GlobalCustomTemplateList[Wtf.Acc_InterLocation_ModuleId];
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


        this.mainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.Form,
            this.ItemDetailGrid
            ],
            bbar:[this.submitBttn, "-",this.cancelBttn,"-", this.savencreateBttn,'-',this.singleRowPrint]
        });
        this.add(this.mainPanel);
    },
    getForm:function (){
        this.storeRec = new Wtf.data.Record.create([
        {
            name:"store_id"
        },

        {
            name:"fullname"
        }
        ]);

        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        this.fromStore = new Wtf.data.Store({
            url:"INVStore/getStoreList.do",
            reader:this.storeReader
        });
        this.fromStore.load({
            params:{
                isActive:true,
                byStoreManager:"true",
                isFromInvTransaction:true, //ERM-691 do not display repair/scrap stores in regular inventory transactions
                byStoreExecutive:"true"
            }
        });
        
        this.fromStore.on("load", function(ds, rec, o){
            if(rec.length > 0){
                this.fromstoreCombo.setValue(rec[0].data.store_id, true);
            }

        }, this);
        
        this.fromstoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.fromStore,
            displayField:"fullname",
            valueField:"store_id",
            fieldLabel:"From Store*",
            hiddenName:"fromstore",
            allowBlank:false,
            emptyText:'Select From Store...',
            width:150,
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
        });
        
        
        this.yesterdayVal = new Date();
        this.yesterdayVal.setDate(new Date().getDate() + 1);
        this.dateField = new Wtf.ExDateFieldQtip({
            fieldLabel:"Business Date*",
            format:"Y-m-d",
            name:"trans date",
            readOnly:true,
            allowBlank:false,
            width:150,
            value:new Date()
        });
        this.MOUTextField = new Wtf.form.TextField({
            fieldLabel:"MoD*",
            readOnly : true,
            name:"trans mod",
            value:_fullName,
            width:150
        });
        this.documentNumber = new Wtf.form.TextField({
            fieldLabel:'Document No',
            name:'documentNo',
            maxLength:50,
            width : 200
        });
        this.sequenceFormatNO = new Wtf.SeqFormatCombo({
            seqNumberField : this.documentNumber,
            fieldLabel:"Sequence Format *",
            name:"seqFormat",
            moduleId:5,
            allowBlank:false,
            width : 150
        });
        
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm2" + this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: Wtf.Acc_InterLocation_ModuleId,
            isEdit: false
        });
         this.memoTextArea= new Wtf.form.TextArea({
            width:200,
            fieldLabel:WtfGlobal.getLocaleText("acc.repeated.Gridcol3")+"/ " +WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
            name:"memo",
            height:45,
            maxLength:2048
        });
        this.Form = new Wtf.form.FormPanel({
            region:"north",
            autoHeight:true,
            id:"northForm2" + this.id,
            url:"INVGoodsTransfer/addInterLocationTransfer.do",
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            disabledClass:"newtripcmbss",
            items:[{
                border:false,
                layout:'form',
                cls:"visibleDisabled",
                items:[{
                    border:false,
                    columnWidth:1,
                    layout:'column',
                    cls:"visibleDisabled",
                    items:[{
                        columnWidth:0.50,
                        border:false,
                        layout:'form',
                        labelWidth:105,
                        items:[
                        this.fromstoreCombo,
                        this.sequenceFormatNO,
                        this.documentNumber,
                        this.memoTextArea,
                        ]
                    },{
                        columnWidth:0.50,
                        border:false,
                        layout:'form',
                        labelWidth:105,
                        items:[
                        this.MOUTextField,
                        this.dateField
                        ]
                    }
                    ]
                },this.tagsFieldset
                ]
            }]       
        });
    },
    printRecordTemplate:function(printflg,item){
        var params= "myflag=order&order&transactiono="+Wtf.OrderNoteNo+"&moduleid="+Wtf.Acc_InterLocation_ModuleId+"&templateid="+item.id+"&recordids="+Wtf.recordbillid+"&filetype="+printflg;  
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "ACCExportPrintCMN/exportSingleInterLocationStockTransfer.do";
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
        this.savencreateBttn.enable();
    },
    getItemDetail:function (){
        this.ItemDetailGrid = new Wtf.interlocationtransferGrid({
            layout:"fit",
            gridTitle:"Product Details",
            border:false,
            region:"center",
            height:300,
            parent:this,
            disabledClass:"newtripcmbss"
        });
    },
    SaveItem:function (){
//        Wtf.getCmp('interLocationTransferSubmit').disabled=true;
//        this.savencreateBttn.disable();
        var jsondata = "";
        if((this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
            WtfComMsgBox(["Info", "Please Select MoD for Goods Order"], 0);
//            Wtf.getCmp('interLocationTransferSubmit').disabled=false;
//            this.enableButtons();
            return;
        }
        if(this.ItemDetailGrid.EditorStore.getCount()-1 == 0){
            WtfComMsgBox(["Info", "Please add Item/s to transfer"], 0);
//            Wtf.getCmp('interLocationTransferSubmit').disabled=false;
//            this.enableButtons();
            return;
        }
        var jdataArray = [];
        for(var i=0;i<this.ItemDetailGrid.EditorStore.getCount()-1;i++){
            var rec = this.ItemDetailGrid.EditorStore.getAt(i);
            if(this.validateRecord(rec)){
                var value = [];
                var count = [];
                if(rec.get("uomtype") == "casing"){
                    value.push(rec.get("casinguomvalue"));
                    value.push(rec.get("inneruomvalue"));
                    value.push(rec.get("primaryuomvalue"));
                    count.push(rec.get("quantity"));
                    count.push(0);
                    count.push(0);
                }else if(rec.get("uomtype") == "inner"){
                    value.push(rec.get("inneruomvalue"));
                    value.push(rec.get("primaryuomvalue"));
                    count.push(rec.get("quantity"));
                    count.push(0);
                }else if(rec.get("uomtype") == "primary"){
                    value.push(rec.get("primaryuomvalue"));
                    count.push(rec.get("quantity"));
                }
                var jdata = {};
                jdata.itemid = rec.get("productid");
                jdata.quantity = rec.get("quantity");
                jdata.uom = rec.get("uomid");
                jdata.uomname = rec.get("uomname");
                jdata.packaging = rec.get("packagingid");
                jdata.confactor = rec.get("confactor");
                jdata.fromstore = this.fromstoreCombo.getValue();
                jdata.tostore = this.fromstoreCombo.getValue();
                jdata.costcenter = rec.get("costcenter");
                jdata.businessdate = this.dateField.getValue().format('Y-m-d');
                jdata.remark = this.NewlineRemove((rec.get("remark"))) ;
                jdata.stockDetails = rec.get("stockDetails");
                
                var arr = Wtf.decode(WtfGlobal.getCustomColumnData(this.ItemDetailGrid.EditorStore.data.items[i].data, Wtf.Acc_InterLocation_ModuleId).substring(13));
                if (arr.length > 0)
                    jdata.linelevelcustomdata = arr;
                jdataArray.push(jdata);
             
            }else{
//                Wtf.getCmp('interLocationTransferSubmit').disabled = false;
//                this.enableButtons();
                return;
            }
        }
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(!isValidCustomFields){
            return;
        }
        var finalStr = JSON.stringify(jdataArray);
        this.sendInterTransferRequest(finalStr);
   
    },
    
    sendInterTransferRequest: function(finalStr){
        if(this.Form.form.isValid()){
            var allowNegInv="";
            if(this.allowNegativeInventory != undefined || this.allowNegativeInventory != ""){
                allowNegInv=this.allowNegativeInventory;
            }
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            var dimencustomfield="";
            if (custFieldArr.length > 0)
            dimencustomfield = JSON.stringify(custFieldArr);
            Wtf.getCmp('interLocationTransferSubmit').disabled=true;
            this.savencreateBttn.disable();
            this.Form.form.submit({
                params:{
                    str:finalStr,
                    partnerid: this.MOUTextField.getValue(),
                    seqFormatId:this.sequenceFormatNO.getValue(),
                    documentNumber:this.documentNumber.getValue(),
                    allowNegativeInventory: allowNegInv,
                    memo:this.memoTextArea.getValue(),
                    customfield:dimencustomfield,
                    UomSchemaType:!Wtf.account.companyAccountPref.UomSchemaType
                },
                scope:this,
                success:function (result,resp){
                    var retstatus = eval('('+resp.response.responseText+')');
                    var msg = "";
                    var title="Error";
                    
                    if(retstatus.data.success){
                        title="Success";
                        msg=retstatus.data.msg;

                        WtfComMsgBox([title,msg],0);
                        this.Form.form.reset();
                        Wtf.recordbillid=resp.result.data.billid;
                        Wtf.OrderNoteNo=resp.result.data.Interlocationno;
                        this.disableafterSaveButtons(this.saveOnlyFlag);
                        this.enableafterSaveButtons(this.saveOnlyFlag);
                        
                        if(this.saveOnlyFlag && retstatus.data.success){//Disabling and enabling after saving for save Button
                            this.Form.disable();
                            this.ItemDetailGrid.disable();
                        }else{
                            this.ItemDetailGrid.EditorStore.removeAll();
                            this.ItemDetailGrid.addRec();
                            this.tagsFieldset.resetCustomComponents();
                        }
                    }
                    else if(retstatus.data.success==false && (retstatus.data.currentInventoryLevel != undefined && retstatus.data.currentInventoryLevel != "")){

                        if(retstatus.data.currentInventoryLevel=="warn"){

                            Wtf.MessageBox.confirm("Confirm",retstatus.data.msg, function(btn){
                                if(btn == 'yes') {        
                                    this.allowNegativeInventory=true;
                                    this.sendInterTransferRequest(finalStr);
                                }else if(btn == 'no') {
                                    this.allowNegativeInventory=false;
                                }
                            },this);
                        }
                        
                        if(retstatus.data.currentInventoryLevel=="block"){
                            var msg = retstatus.data.msg;
                            Wtf.MessageBox.show({
                                msg: msg,
                                icon:Wtf.MessageBox.INFO,
                                buttons:Wtf.MessageBox.OK,
                                title:"Warning"
                            });
                        }
                    }else if(retstatus.data.msg){
                                WtfComMsgBox(["Inter Location Transfer",retstatus.data.msg],retstatus.data.success*2+2);

                     }else{
                        Wtf.MessageBox.show({
                            msg: retstatus.data.msg,
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK,
                            title:"Warning"
                        }); 
                    }
                    
                    Wtf.getCmp('interLocationTransferSubmit').disabled=false;
                },
                failure:function (){
                    Wtf.MessageBox.show({
                        msg:"Error while sending Item transfer request",
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK,
                        title:"Error"
                    });
                    this.Form.form.reset();
                    this.ItemDetailGrid.EditorStore.removeAll();
                    this.ItemDetailGrid.addRec();
                    Wtf.getCmp('interLocationTransferSubmit').disabled=false;
                    this.enableButtons();
                }
            });
        }else{
            Wtf.getCmp('interLocationTransferSubmit').disabled=false;
            this.enableButtons();
        }
    },
    NewlineRemove : function(str){
        if (str)
            return str.replace(/\n/g, ' ');
        else
            return str;
    },

    validateRecord: function(rec){
        var msg="Please enter valid data for ";
        var status=false;
        if(rec.get("pid") == "" || rec.get("pid") == null) {
            msg += "Item ";
        } else if(rec.get("uomid") == "" || rec.get("uomid") == null) {
            msg += "UoM ";
        }else if( rec.get("quantity") == "" || rec.get("quantity") <= 0) {
            msg += "Quantity ";
        }else if( rec.get("stockDetailQuantity") !==  rec.get("quantity") ){
            msg = "Stock detail not match with transfer quantity for product: <b>"+rec.get('pid') +"</b>";
        }else {
            status = true;
        }

        if(!status){
            WtfComMsgBox(["Info", msg], 0);
        }
        return status;
    }
});

//------------------------------------------Editor Grid Component---------------------------------------------------

Wtf.interlocationtransferGrid = function (config){
    Wtf.apply(this,config);
    Wtf.interlocationtransferGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.interlocationtransferGrid,Wtf.Panel,{
    initComponent:function (){
        Wtf.interlocationtransferGrid.superclass.initComponent.call(this);
        this.getEditorGrid();
        this.tmpPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            {
                region:"north",
                height:30,
                border:false,
                bodyStyle:"background-color:#f1f1f1;padding:8px",
                html:"<div class='gridTitleClass'style='float:left;'>"+this.gridTitle+"</div><div style = 'float:right; font-size:9px;'> Note : Highlighted records are invalid because of transfering uom is not set. Remove them before submit.</div>"
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
        //            name:"ccid"
        //        },
        //        {
        //            name:"name"
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
        //        this.uomCombo = new Wtf.form.ComboBox({
        //            triggerAction:"all",
        //            mode:"local",
        //            typeAhead:true,
        //            forceSelection:true,
        //            store:(Wtf.account.companyAccountPref.UomSchemaType)?this.uomStore:Wtf.uomStore,
        //            displayField:Wtf.account.companyAccountPref.UomSchemaType?"name":"uomname",
        //            valueField:Wtf.account.companyAccountPref.UomSchemaType?"id":"uomid",
        //            width:200
        //        });
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
            listWidth:250,
            width:200,
            scope:this,
            isProductCombo: true,
            maxHeight:250,
            extraComparisionField:'ccid',// type ahead search on acccode as well.
            lastQuery:'',
            hirarchical:true
            
        }); 
        
        this.costCenterCombo.listWidth=250;
        
        this.itemCodeCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            store:this.itemTypeStore,
            fieldLabel:"Item Type",
            hiddenName:"type",
            width:200
        });
        this.itemEditorRec = new Wtf.data.Record.create([
        {
            name:"productid"
        },

        {
            name:"desc"
        },
        {
            name:"productname"
        },
        {
            name:"partnumber"
        },

        {
            name:"uomid"
        },

        {
            name:"packaging"
        },            
        {
            name:"packagingid"
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
            name:"costcenter"
        },
        {
            name:"data"
        },

        {
            name:"pid"
        },

        {
            name:"cost"
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
            name:"transferToStockUOMFactor"
        },
        {
            name:"stockDetails"
        },{
            name:"uomschematype"
        },
        {
            name:"ismultipleuom"
        },
        {
            name:"hasAccess"
        }
        ]);
        this.itemEditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.itemEditorRec);

        this.itemEditorStore = new Wtf.data.Store({
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{
                isStoreLocationEnable:true,
                isInventoryForm:true
            },
            reader:this.itemEditorReader
        });

        this.itemCodeEditorStore = new Wtf.data.Store({
            url:"ACCProduct/getProductsForCombo.do",
            baseParams:{
                isStoreLocationEnable:true,
                isInventoryForm:true
            },
            reader:this.itemEditorReader
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
                scope:this,
                hirarchical:true,
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
                    extraComparisionField:'pid',// type ahead search on acccode as well.
                    mode:'remote',
                    hideTrigger:true,
                    scope:this,
                    triggerAction : 'all',
                    editable : true,
                    minChars : 1,
                    hirarchical:true,
                    hideAddButton : true,//Added this Flag to hide AddNew  Button  
                    forceSelection:true
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
            name:"productid"
        },

        {
            name:"pid"
        },

        {
            name:"desc"
        },

        {
            name:"partnumber"
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
            name:"remark"
        },
        {
            name:"costcenter"
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
            name:"transferToStockUOMFactor"
        },
        {
            name:"stockDetails"
        },
        {
            name:"stockDetailQuantity"
        }, {
            name:"uomschematype"
        },
        {
            name:"ismultipleuom"
        },{
            name:"confactor"
        }
        ]);

        this.EditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.EditorRec);

        this.EditorStore = new Wtf.data.Store({
            url:"jspfiles/inventory/store.jsp?flag=52",
            reader:this.EditorReader
        });
        WtfGlobal.updateStoreConfig(GlobalColumnModel[Wtf.Acc_InterLocation_ModuleId], this.EditorStore);
        this.addRec();
        var cmWidth=175;
        this.EditorStore.on("load",this.addRec,this);
        var columnArr = [];
        columnArr.push(
            new Wtf.grid.RowNumberer(),
            {
                header:"Add",
                align:'center',
                width:30,
                dataIndex:"plusbtn",
                renderer: this.addProductList.createDelegate(this)
            },
            {
                header:"Product ID",
                dataIndex:"pid",
                width:cmWidth,
                editor:this.itemCodeEditorCombo
//                renderer:this.getComboRenderer(this.itemCodeEditorCombo)
            },
            {
                header:"Product Name",
                dataIndex:"itemdescription",
                width:cmWidth
            },
            {   
                header:'Product Description',
                dataIndex:'desc',
                hidden:false
            },
            {
                header:"CoilCraft Part No",
                dataIndex:"partnumber",
                hidden: true
            },{
                header:"UoM",
                dataIndex:"uomname",
                renderer:this.getComboRenderer(this.uomCombo),
                editor:this.uomCombo,
                width:cmWidth
            // sortable:true
            },{
                header:"Packaging",
                dataIndex:"packaging",
                width:cmWidth,
                hidden:!Wtf.account.companyAccountPref.UomSchemaType?true:false,
                renderer:this.getComboRenderer(this.packagingCombo),
                editor:Wtf.account.companyAccountPref.UomSchemaType==false?this.packagingCombo:""
            },{
                header:"Conversion Factor",
                dataIndex:"confactor",
                hidden:Wtf.account.companyAccountPref.UomSchemaType?true:false,
                width:150,
                renderer:this.conversionFactorRenderer(this.itemCodeEditorStore,"productid","uomname",this.EditorStore),
                editor:(Wtf.account.companyAccountPref.UomSchemaType===0) ?this.transBaseuomrate : ""     //Does allow to user to change conversion factor
            },
            {
                header:"Cost Center",
                dataIndex:"costcenter",
                width:cmWidth,
                renderer:this.getComboRenderer(this.costCenterCombo),
                editor:this.costCenterCombo
            },{
                header:"Quantity",
                dataIndex:"quantity",
                width:cmWidth,
                editor:new Wtf.form.NumberField({
                    scope: this,
                    allowBlank:false,
                    allowNegative:false,
                    decimalPrecision:4,
                    value:0
                }),
                renderer: function(val){
                    return val;
                }
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
                header: '',
                align:'center',
                renderer: this.serialRenderer.createDelegate(this),
                width:40
            },
            {
                header:"Remark",
                dataIndex:"remark",
                width:cmWidth,
                editor:new Wtf.form.TextArea({
                    maxLength:200,
                    regex:Wtf.validateAddress
                })
            });
        columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[Wtf.Acc_InterLocation_ModuleId]);
        columnArr.push({
            header: "Action",
            align: 'center',
            dataIndex: "lock",
            width:50,
            renderer: function(v,m,rec){
            return "<span class='pwnd delete-gridrow'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>";
            }
        });
        this.EditorColumn = new Wtf.grid.ColumnModel(columnArr);

        this.addBut = new Wtf.Toolbar.Button({
            text:"Add"
        });

        this.EditorGrid = new Wtf.grid.EditorGridPanel({
            cm:this.EditorColumn,
            region:"center",
            //            id:"editorgrid1",
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
        this.EditorGrid.on("beforeedit",this.loadUom,this);
        this.EditorGrid.on('populateDimensionValue',this.populateDimensionValueingrid,this);
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
            if(rowindex==total-1){
                return;
            }
            Wtf.MessageBox.confirm('Warning', 'Are you sure you want to remove this item?', function(btn){
                if(btn!="yes") return;
                store.remove(store.getAt(rowindex));
                this.ArrangeNumberer(rowindex);
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
    fillGridValue:function (e){
        for(var i=0;i<this.itemEditorStore.getCount();i++){
            var itemRec = this.itemEditorStore.getAt(i);
            if(itemRec.get("productid") == e.value){
                var rec = this.EditorStore.getAt(e.row);
                rec.set("productid",e.value);
                rec.set("pid",this.itemEditorStore.getAt(i).get("pid"));
                rec.set("uomname",this.itemEditorStore.getAt(i).get("transferinguomname"));
                rec.set("orderinguomname",this.itemEditorStore.getAt(i).get("orderinguomname"));
                rec.set("transferinguomname",this.itemEditorStore.getAt(i).get("transferinguomname"));
                rec.set("transferToStockUOMFactor",this.itemEditorStore.getAt(i).get("transferToStockUOMFactor"));
                rec.set("stockuomname",this.itemEditorStore.getAt(i).get("uomname"));
                rec.set("packaging",this.itemEditorStore.getAt(i).get("packaging"));
                rec.set("packagingid",this.itemEditorStore.getAt(i).get("packagingid"));
                rec.set("uomid",this.itemEditorStore.getAt(i).get("uomid"));
                rec.set("primaryuomvalue",this.itemEditorStore.getAt(i).get("primaryuomvalue"));
                rec.set("inneruomvalue",this.itemEditorStore.getAt(i).get("inneruomvalue"));
                rec.set("casinguomvalue",this.itemEditorStore.getAt(i).get("casinguomvalue"));
                rec.set("costcenter",this.itemEditorStore.getAt(i).get("costcenter")); 
                rec.set("desc",this.itemEditorStore.getAt(i).get("desc"));
                rec.set("itemdescription",this.itemEditorStore.getAt(i).get("productname"));
                rec.set("data",this.itemEditorStore.getAt(i).get("data"));
                rec.set("partnumber",this.itemEditorStore.getAt(i).get("partnumber"));
                rec.set("isBatchForProduct",this.itemEditorStore.getAt(i).get("isBatchForProduct"));
                rec.set("isSerialForProduct",this.itemEditorStore.getAt(i).get("isSerialForProduct"));
                rec.set("isRowForProduct",this.itemEditorStore.getAt(i).get("isRowForProduct"));
                rec.set("isRackForProduct",this.itemEditorStore.getAt(i).get("isRackForProduct"));
                rec.set("isBinForProduct",this.itemEditorStore.getAt(i).get("isBinForProduct"));
                rec.set("stockDetails",this.itemEditorStore.getAt(i).get("stockDetails"));
                rec.set("uomschematype",this.itemEditorStore.getAt(i).get("uomschematype"));
                rec.set("ismultipleuom",this.itemEditorStore.getAt(i).get("ismultipleuom"));
                
                if(rec.get("transferinguomname") == "" ){
                    this.EditorGrid.getView().getRow(e.row).style = "background-color :pink";
                }
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
                    rec1.set("transferToStockUOMFactor",uomStoreRec.get("factor"));
                    rec1.set("transferinguomname",uomStoreRec.get("name"));

                }
            }
        }else{
            var idx = this.uomCombo.store.find(this.uomCombo.valueField, e.value);
            if(idx != -1){
                var rec = this.uomCombo.store.getAt(idx);
                var valueStr = rec.get(this.uomCombo.displayField);
                var rec1 = this.EditorStore.getAt(e.row); 
                rec1.set("transferinguomname",valueStr);
            }
        }
        if(e.field =='uomname'&&!Wtf.account.companyAccountPref.UomSchemaType) {
             
            this.getProductBaseUOMRate(this.EditorStore.getAt(e.row).get("productid"),this.EditorStore.getAt(e.row).get("uomname"),e.row);
        }
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
            var idx = Wtf.uomStore.find("uomid", record.data["uomname"]);            
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
                return "1 "+ uomname +" = "+ +value+" "+rec.data["stockuomname"];
            }else{
                rec = store.getAt(idx);
                return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
            }  
            
        }
    },
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
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
    cellClick :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        if (fieldName == "uomname") {
            if (!record.get("ismultipleuom") && !Wtf.account.companyAccountPref.UomSchemaType) {
                return false;
            } else {
                if (record.get("productid") != undefined && record.get("productid") != "") {
                    if (!Wtf.account.companyAccountPref.UomSchemaType) {
                        this.getProductBaseUOMRate(e.value, record.get("uomid"), e.row);
                    } else {
                        this.loadPackagingStore(record.get("productid"));
                    }
                }
            }
        }
        var itemId=record.get("productid");
        var quantity=record.get("quantity");
        //        var transferToStockUOMFactor=record.get("transferToStockUOMFactor");
        var transferToStockUOMFactor=Wtf.account.companyAccountPref.UomSchemaType?record.get("transferToStockUOMFactor"):record.get("confactor");
        var fromStoreId = this.parent.fromstoreCombo.getValue();
        
        if(fieldName == undefined){
            if(fromStoreId == undefined ||  fromStoreId == "" ){
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
                        var maxAllowed= Math.floor(availQty/transferToStockUOMFactor);
                         
                        if(availQty <= 0){
                            WtfComMsgBox(["Warning", "Quantity is not available for selected item."],0);
                            return false;
                        }
                        
                        if(availQty >= (quantity * transferToStockUOMFactor )){
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
     showProductGrid : function() {//ERP-8199 :
       
        this.productSelWin = new Wtf.account.ProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 700,
            title:'Product Selection Window',
            layout : 'fit',
            modal : true,
            resizable : true,
            id:this.id+'ProductSelectionWindow',
            moduleid:Wtf.Acc_InterLocation_ModuleId,
            modulename:"INTER_LOCATION_TRANSFER",
            invoiceGrid:this.EditorGrid,
            parentCmpID:this,
            isFromInventorySide:true,
            isStoreLocationEnable:true,
            warehouseId:this.parent.fromstoreCombo.getValue(),
            isCustomer : false
        });
        this.productSelWin.show();
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
    loadPackagingStore:function(productid){
        this.packagingStore.load({
            params:{    
                productId:productid
            }
        },this);
    },
    loadUom:function(e){
        if(Wtf.account.companyAccountPref.UomSchemaType){
            var rec = this.EditorStore.getAt(e.row);
            if(rec.get("transferinguomname") == "" && e.field != "pid"){
                return false;
            }
            var rec=e.record;
            this.uomStore.load({
                params:{
                    packagingId:rec.data.packagingid
                }
            });
            
            this.packagingStore.load({
                params:{    
                    productId:rec.data.productid
                }
            },this);
        }else{
            this.uomStore.load({
                params:{
                    doNotShowNAUomName:true
                }
            });
        }
    },
    showStockDetailWindow : function (record){
        var itemId=record.get("productid");
        var itemCode=record.get("pid");
        var quantity=record.get("quantity");
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isRackEnable = record.get("isRackForProduct");
        var isRowEnable = record.get("isRowForProduct");
        var isBinEnable = record.get("isBinForProduct");
        //        var transferToStockUOMFactor=record.get("transferToStockUOMFactor");
        var transferToStockUOMFactor=Wtf.account.companyAccountPref.UomSchemaType?record.get("transferToStockUOMFactor"):record.get("confactor");
        var transferUOMName=record.get("transferinguomname");
        var stockUOMName=record.get("stockuomname");
        var fromStoreId = this.parent.fromstoreCombo.getValue();
        var fromStoreName=this.parent.fromstoreCombo.getRawValue();
        var maxQtyAllowed=transferToStockUOMFactor * quantity;
        
        var winTitle = "Stock Detail for Stock Transfer";
        var winDetail = String.format('Select Stock details for stock transfer  <br> <b>Product :</b> {0}<br> <b>Store :</b> {1}<br> <b>Quantity :</b> {2} {3} ( {4} {5} )', itemCode, fromStoreName, quantity, transferUOMName, maxQtyAllowed, stockUOMName);
        
        this.detailWin = new Wtf.StockTransferDetailWin({
            WinTitle : winTitle,
            WinDetail: winDetail,
            TotalTransferQuantity: maxQtyAllowed,
            ProductId:itemId,
            FromStoreId: fromStoreId,
            ToStoreId: fromStoreId,
            isBatchForProduct: isBatchEnable,
            isSerialForProduct : isSerialEnable,
            isRowForProduct: isRowEnable,
            isRackForProduct: isRackEnable,
            isBinForProduct: isBinEnable,
            GridStoreURL:"INVStockLevel/getStoreProductWiseDetailList.do",
            StockDetailArray:record.get("stockDetails"),
            moduleid:Wtf.Acc_InterLocation_ModuleId,
            modulename:"INTER_LOCATION_TRANSFER",
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
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(Wtf.Acc_InterLocation_ModuleId, rec, this.EditorGrid);
    }
    
});

// Inter Location Transfer Report  


Wtf.InterLocationReportTabPanel = function(config){
    Wtf.InterLocationReportTabPanel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.InterLocationReportTabPanel, Wtf.Panel, {
    initComponent: function() {
        Wtf.InterLocationReportTabPanel.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        var companyDateFormat='Y-m-d'
        Wtf.InterLocationReportTabPanel.superclass.onRender.call(this, config);
        this.dmflag = 0;
        
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.fromdateVal.setDate(new Date().getFirstDateOfMonth().getDate());
        this.frmDate = new Wtf.form.DateField({
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
        this.toDate = new Wtf.form.DateField({
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
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });
        this.storeCmbStore.load();
        this.resetBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.inventory.QAAproval.ResetFilter"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.stock.ClicktoResetFilter")
            },
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            scope:this,
            handler:function(){
                this.grid.quickSearchTF.setValue("");
                this.frmDate.setValue(WtfGlobal.getDates(true));
                this.toDate.setValue(WtfGlobal.getDates(false));
                this.storeCmbfilter.setValue(this.storeCmbfilter.store.data.items[0].data.store_id);
                this.initloadgridstore(this.frmDate.getValue().format('Y-m-d'), this.toDate.getValue().format('Y-m-d'),this.storeCmbfilter.getValue());
            }
        });
       
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
            "name":"packagingid"
        },
        {
            "name":"uomschematype"
        },
        {
            "name":"isBatchForProduct"
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
            "name":"isSerialForProduct"
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
            "name":"fromlocation"
        },
        {
            "name":"tolocation"
        },
        {
            "name":"hscode"
        },
        {
            "name":"memo"
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
                field: 'status',
                direction: "DESC"
            },
            groupField:"transfernoteno",
            url: 'INVGoodsTransfer/getInterLocationTransferList.do',//
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },
            this.record
            )
        
        });
        
        
        this.sm= new Wtf.grid.CheckboxSelectionModel({
            // singleSelect:true
            });
        
             
       
     
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
            '<tpl if="this.isTrue(parent.isSerialForProduct)==true">',  // serial 
            '<th><h2><b>Issued Serials</b></h2></th>',
            '<th><h2><b>Collected Serials</b></h2></th>',
            '</tpl>',
            '</tpl>',

            
            
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
            '<td ><p>{issuedSerials}</p></td>',
            '<td ><p>{collectedSerials}</p></td>',
            '</tpl>',
            
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
        
        
        var cmDefaultWidth = 100;
        var colArr = new Array();
        colArr.push(new Wtf.grid.RowNumberer(), //0
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
            header: WtfGlobal.getLocaleText("acc.stockavailability.TransferNoteNo"), //4
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
            header: WtfGlobal.getLocaleText("acc.nee.69"),   //5
            //sortable:true,
            dataIndex: 'createdby',
            width:cmDefaultWidth,
            pdfwidth:100
        },
        {
            header: WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),     //6
            //sortable:true,
            dataIndex: 'fromstorename',
            width:cmDefaultWidth,
            pdfwidth:100
        },
        {
                header: WtfGlobal.getLocaleText("acc.je.FromLocation"),     //6
                //sortable:true,
                dataIndex: 'fromlocation',
                width:cmDefaultWidth,
                pdfwidth:100
            },
            {
                header: WtfGlobal.getLocaleText("acc.je.ToLocation"),     //6
                //sortable:true,
                dataIndex: 'tolocation',
                width:cmDefaultWidth,
                pdfwidth:100
            },
        {
            header: WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup.date"),     //8
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
            header: WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
            //sortable:true,
            dataIndex: 'itemname',
            width:cmDefaultWidth,
            pdfwidth:100
        },
        {
            header: WtfGlobal.getLocaleText("acc.product.description"),
            //sortable:true,
            dataIndex: 'itemdescription',
            width:cmDefaultWidth,
            pdfwidth:100
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
            header: WtfGlobal.getLocaleText("acc.procduct.trans.header.5"),
            //sortable:true,
            dataIndex: 'quantity',
            width:cmDefaultWidth,
            hidden:false,
            pdfwidth:50
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
            header: WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
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
        });
            
        this.moduleid = Wtf.Acc_InterLocation_ModuleId;
        colArr = WtfGlobal.appendCustomColumn(colArr,GlobalColumnModelForReports[this.moduleid],true, true, true);
        var colModelArray = GlobalColumnModelForReports[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.ds);

        colArr = WtfGlobal.appendCustomColumn(colArr,GlobalColumnModel[this.moduleid]);
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.ds);
        
        this.cm = new Wtf.grid.ColumnModel(colArr); 
                
        this.exportBttn = new Wtf.exportButton({
            obj: this,
            //            id: 'stocktransferregisterexportid',
            tooltip: "Export Report", //"Export Report details.",  
            menuItem:{
                csv:true,
                pdf:true,
                xls:true
            },
            get:Wtf.autoNum.InterLocationTransferReport,
            label:"Export"
        })
        this.printBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.print"),
            scope: this,
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.inventory.stockrequest.grid.printTTip")
            },
            //            id:'interstorestockreportprint',
            iconCls:'pwnd printButtonIcon',
            hidden:false,
            disabled:true,
            handler: function(){
                var selected= this.sm.getSelections();
                var cnt = selected.length;
                if(cnt > 0){
                    printout("interstore", this.ds.query("transfernoteno", selected[0].data.transfernoteno));
           
                }else{
                    return;
                }
            // this.Print();
            }
            
        });

        /*Print Record Button*/
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        
        var colModArray=[];
        colModArray = GlobalCustomTemplateList[Wtf.Acc_InterLocation_ModuleId];
        
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
        tbarArray.push("-",WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.fromdate")+": ",this.frmDate,"-",WtfGlobal.getLocaleText("acc.mydocuments.quicksearch.mtytxt.todate")+": ",this.toDate, "-",WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+": ",this.storeCmbfilter, "-",this.search,"-",this.resetBtn);
       
        this.deleteButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("mrp.workorder.report.delete"),
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            id:"delete",
            handler:this.deleteHandler,
            scope:this
        });

       
        var bbarArray=new Array();
        //        if(this.type == 1){
        //            if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.acceptrejectistreq)) {
        //                bbarArray.push("-",this.editButton);
        //                bbarArray.push("-",this.deleteButton);
        //            }
        //            if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.printistreq)) {
        //                bbarArray.push("-",this.printBttn);
        //            }
        //        }
        //        if(this.type == 2){
        //            if(!WtfGlobal.EnableDisable(Wtf.UPerm.interstorestocktransfer, Wtf.Perm.interstorestocktransfer.exportisteq)) {
        if(Wtf.account.companyAccountPref.deleteTransaction){
            bbarArray.push("-",this.deleteButton);
        }
        bbarArray.push("-",this.exportBttn);
        bbarArray.push("-",this.singleRowPrint);
        //            }
        //        }
        

        /********************/
        this.grid=new Wtf.KwlEditorGridPanel({
            //            id:"inventEditorGridPanel"+this.id,
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
        
        Wtf.getCmp("paggintoolbar"+this.grid.id).on('beforerender',function(){
            Wtf.getCmp("paggintoolbar"+this.grid.id).pageSize=30
        },this);
        
        this.grid.on("beforeedit",function(){
            return false;
        },this);
        var arrId=new Array();
        arrId.push("delete");//"deleteIssueBtn" id of button
        
        this.ok=1;
        
        this.grid.on("validateedit",this.validateeditFunction,this);
        this.grid.on("statesave",this.statesaveFunction,this);
        this.sm.on("selectionchange",function(){//enabling print record button button
            var selected = this.sm.getSelections();
            if(selected.length>0){
                var selTransactionNo = selected[0].get("transfernoteno");
                var different=false;
                for(var i=0;i<selected.length;i++){                     
                    if(selected[i].get("transfernoteno") != selTransactionNo){
                        different = true;
                    }       
                }    
                if(different == true){
                    //                    this.printBttn.disable();
                    this.singleRowPrint.disable();
                }else{
                    //                    this.printBttn.enable();
                    this.singleRowPrint.enable();
                }
            
            //            this.singleRowPrint.enable();  
            }else{
                this.singleRowPrint.disable(); 
            }
        },this);
        //            var selected = this.sm.getSelections();
        //            if(selected.length>0){
        //                var test = 0;
        //                var selTransactionNo = selected[0].get("transfernoteno");
        //                var different=false;
        //                for(var i=0;i<selected.length;i++){                     
        //                    if(selected[i].get("closeornot") == true){
        //                        test = 1;
        //                    }    
        //                    if(selected[i].get("transfernoteno") != selTransactionNo){
        //                        different = true;
        //                    }       
        //                }
        //                //                 var test = selected.find("closeornot",true);
        //                if(test == 0){
        //                    this.editButton.enable();
        //                    this.deleteButton.enable();
        //                } else {
        //                    this.editButton.disable();
        //                    this.deleteButton.disable();
        //                }
        //                if(selected[0].get("remark")=="Stock Returned"&&selected[0].get("status")=="Returned"){
        //                    this.deleteButton.disable();
        //                }
        //                if(different == true){
        //                    this.printBttn.disable();
        //                }else{
        //                    this.printBttn.enable();
        //                }
        //            }else{
        //                this.printBttn.disable();  
        //            }
        //
        //        },this);
        
        this.add(this.grid);
        
        this.on("activate",function()
        {
            
            
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
            
                //                this.storeCmbfilter.fireEvent('select');
                    
                this.initloadgridstore(this.frmDate.getValue().format('Y-m-d'),this.toDate.getValue().format('Y-m-d'),this.storeCmbfilter.getValue());
            }, this);
                
                         
            
        //
        },this);
    

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
        var params= "myflag=order&order&transactiono="+transfernoteno+"&moduleid="+Wtf.Acc_InterLocation_ModuleId+"&templateid="+item.id+"&recordids="+recordbillid+"&filetype="+printflg;  
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "ACCExportPrintCMN/exportSingleInterLocationStockTransfer.do";
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
        div.innerHTML = "Loading, Please Wait...";
        myWindow.document.body.appendChild(div);
        mapForm.remove();
    },
    initloadgridstore:function(frm, to,storeid){

        this.ds.baseParams = {
            //flag: 24,
            //            type:this.type,
            frmDate:frm,
            toDate:to,
            storeid:storeid
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
        "&type="+this.type+
        "&reportname=" + this.title +
        "&exporttype=" + exportType +                                         
        "&frmDate=" + this.frmDate.getValue().format(Wtf.getDateFormat())+
        "&toDate=" + this.toDate.getValue().format(Wtf.getDateFormat()) +
        "&dmflag="+ this.dmflag+
        "&ss="+ this.grid.quickSearchTF.getValue();
                                         
        if(this.type==1 || this.type==3){
            url =  "ExportDataServlet.jsp?" +"&mode=" + reportid +
            "&colHeader=" + colHeader+
            //"&storeid=" + this.storeCmbfilter.getValue()+
            "&type="+this.type+
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
            //            if(rec.get('status') == "In Transit"){
            requestString += "<br><b>"+(i+1)+").</b> ";
            requestString += "Request No: <b>"+rec.get('transfernoteno')+"</b>, ";
            requestString += "Product: <b>"+rec.get('itemcode')+"</b> ";
        //            requestString += "Between Store: <b>"+rec.get('fromstorename')+"</b> to <b>"+rec.get('tostorename')+"</b>";
        //            }
        }
        
        var confirmMsg = "Are you sure want to delete following request(s)? <br> "+requestString;
        Wtf.MessageBox.confirm("Confirm",confirmMsg, function(btn){
            if(btn == 'yes') {
                WtfGlobal.setAjaxTimeOut();
                Wtf.Ajax.requestEx({
                    url: "INVGoodsTransfer/deleteInterLocationTransferRequest.do",
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
                    }else{
                        WtfComMsgBox(["Failure", result.msg], 1);
                    }
            
            
                }, function(){
                    WtfGlobal.resetAjaxTimeOut();
                    WtfComMsgBox(["Error", "Error occurred while processing your request "], 1);
                });
            }
        }, this);
    }
});
