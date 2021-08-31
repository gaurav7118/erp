/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.order = function (config){
    this.isTemplate=config.isTemplate==undefined?false:config.isTemplate;
    Wtf.apply(this,config);
    Wtf.order.superclass.constructor.call(this);
}

Wtf.extend(Wtf.order,Wtf.Panel,{
    onRender:function (config) {
        Wtf.order.superclass.onRender.call(this, config);
        this.getForm();
        this.getItemDetail();

        this.amainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.someForm,
            //                this.z

            this.ItemDetailGrid
            ],
            bbar:[this.submitBttn,'-',this.cancelBttn,'-',this.savencreateBttn,'-',this.singleRowPrint]
        });

        this.add(this.amainPanel);
       
        this.on("activate",function()
        {
            this.doLayout();
        },this);
        
        this.hideFormFields();
    },

    getForm:function (){
        this.storeRec = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },
        {
            name: 'description'
        },
        {
            name: 'abbr'
        },
        {
            name: 'fullname'
        }
        ]);

        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        this.Store = new Wtf.data.Store({
            url: 'INVStore/getStoreList.do',
            reader:this.storeReader
        });
        this.Store.load({
            params:{
                isActive : "true",
                byStoreManager:"true",
                isFromInvTransaction:true, //ERM-691 do not display repair/scrap stores in regular inventory transactions
                byStoreExecutive:"true"
            }
        });
        
        this.Store.on("load", function(ds, rec, o){
            if(rec.length > 0){
                if(this.FromStoreId != null ){
                    this.fromstoreCombo.setValue(this.FromStoreId, true); // for threshold
                }else{
                    this.fromstoreCombo.setValue(rec[0].data.store_id, true);
                }
                
            }
        }, this);
    
        this.fromstoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            store:this.Store,
            forceSelection:true,
            displayField:'fullname',
            valueField:'store_id',
            fieldLabel:WtfGlobal.getLocaleText("acc.stock.ForStore"),
            hiddenName:"fromstore",
            id:"fromstore" + this.id,
            allowBlank:false,
//            id:"orderfromstore",
            checkField1:"ordertostore",
            width:200,
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
        this.moduleTemplateName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.campaigndetails.campaigntemplate.templatename"),
            name: 'moduletempname',
            id: "moduletempname" + this.id,
            width: 200,
            maxLength: 50,
            hidden:!this.isTemplate,
            scope: this,
            hideLabel : !this.isTemplate
        });
                
        this.documentNumber = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.DocumentNo"),
            name:'documentNo',
            maxLength:50,
            width : 200,
            hidden:this.isTemplate,
            hideLabel : this.isTemplate
        });
        this.sequenceFormatNO = new Wtf.SeqFormatCombo({
            seqNumberField : this.documentNumber,
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.sequenceformat")+"*",
            name:"seqFormat",
            id:"seqFormat" +this.id,
            moduleId:0,
            allowBlank:this.isTemplate?true:false,
            width : 200,
            hidden:this.isTemplate,
            hideLabel : this.isTemplate
        });

        this.SeqformatRec = Wtf.data.Record.create([
        {
            name:"seqFormatId"
        },{
            name:"formatedNumber"
        },{
            name:"isDefault"
        }]);

        this.SeqFormatstore = new Wtf.data.Store({
            baseParams: {
                action: 6,
                isActive:true,
                moduleId: this.moduleId
            },
            url:"INVSeq/getSeqFormats.do",
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },this.SeqformatRec)
        });

        this.SeqFormatstore.on('load',function(ds){
            for(var i=0;i<ds.data.length;i++){
                if(ds.data.items[i].data.isDefault==true){
                    this.sequenceFormatNO.setValue(ds.data.items[i].data.seqFormatId)

                }
            }
        },this)
        this.submitBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.je.ClicktoSave")
            },
            iconCls: getButtonIconCls(Wtf.etype.save),
            handler:function (){
                if(!this.documentNumber.getValue()&&this.sequenceFormatNO.getValue()=="NA"){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.stockrequest.PleaseentervalidDocumentNo")],3);
                    return;
                }
                this.saveOnlyFlag = true;
                this.SaveItem()
            },
            scope:this
        });
        this.cancelBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.invoiceList.bt.cancel"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.je.ClicktoCancel")
            },
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            scope:this,
            handler:function(){
                this.someForm.form.reset();
                this.ItemDetailGrid.EditorStore.removeAll();
                this.ItemDetailGrid.addRec();
                this.submitBttn.enable();
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
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.stockrequest.PleaseentervalidDocumentNo")],3);
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
        var colModArray = GlobalCustomTemplateList[Wtf.Acc_Stock_Request_ModuleId];
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

        this.transferNoteNO = new Wtf.form.TextField({
            fieldLabel:"Order No*",
            name:"trans note no",
            readOnly : true,
            width:200
        });
        this.dateField = new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.date"),
            format:"Y-m-d",
            name:"businessdate",
            id:"businessdate" +this.id,
            allowBlank:false,
            value:new Date(),
            width:200
        });

        this.MOUTextField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.je.MoD")+"*",
            readOnly : true,
            name:"trans mod",
            id:"trans mod" +this.id,
            width:200,
            value:_fullName
        });
        this.KeyField = new Wtf.form.NumberField({
            fieldLabel:"key",
            readOnly : true,
            name:"key",
            width:200,
            hidden:true,
            hideLabel:true
        });
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm2" + this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: Wtf.Acc_Stock_Request_ModuleId,
            isEdit: false
        });
        
        //        if(checktabperms(12, 1) == "edit"){
        //             this.MOUTextField.readOnly = false;
        //        }
        this.moduleTemplateSection();
        /**
         * Add form Field for template as well as Normal Document
         */
        var itemArr=[];
        itemArr.push(this.fromstoreCombo);
        if(!this.isTemplate){
            itemArr.push(this.templateModelCombo,this.sequenceFormatNO,this.documentNumber);
        }else{
            itemArr.push(this.moduleTemplateName)
        }
        itemArr.push(this.KeyField);
        this.someForm = new Wtf.form.FormPanel({
            region:"north",
            autoHeight:true,
            id:"northForm2" + this.id,
            url:"INVGoodsTransfer/addStockOrderRequest.do",
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
                        columnWidth:0.5,
                        border:false,
                        layout:'form',
                        bodyStyle:'padding:13px 13px 0px 13px',
                        labelWidth:120,
                        items:itemArr
                    },{
                        columnWidth:0.5,
                        border:false,
                        layout:'form',
                        bodyStyle:'padding:13px 13px 0px 13px',
                        labelWidth:90,
                        items:[
                                    
                        this.MOUTextField,
                        this.dateField,
                    

                        ]
                    }
                    ]
                },this.tagsFieldset
                ]
            }]       
        });
    },
    /**
     * Event on Template selection i.e. select value form combo box template
     */
        moduleTemplateSection:function(){
        this.moduleTemplateRecord = new Wtf.data.Record.create([
            {
                name: 'templateId'
            },
            {
                name: 'templateName'
            },
            {
                name: 'moduleRecordId'
            }
        ]);

        this.moduleTemplateStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.moduleTemplateRecord),
            url : "ACCCommon/getModuleTemplate.do",
            baseParams:{
                moduleId:Wtf.Acc_Stock_Request_ModuleId
            }
        });
        
        this.moduleTemplateStore.on('load', function(store){
            
             /*
             *Loads the default Template Id for the module  
             **/
            var defaultId=store.reader.jsonData.defaultId;
            if( this.templateModelCombo.getValue()== ""&&defaultId!=undefined&&!this.isCopyFromTemplate&&!this.isEdit&&!this.isView&&!this.isCopyInvoice&&!this.isTemplate){
                this.templateModelCombo.setValue(defaultId);
                this.templateId=defaultId;
                this.templateModelCombo.fireEvent('select',this);
                }
        },this);
        
        
        this.templateModelCombo= new Wtf.form.FnComboBox({
            fieldLabel:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.isExciseTab)? WtfGlobal.getLocaleText("acc.field.SelectExciseUnit")+"*":(this.isViewTemplate?WtfGlobal.getLocaleText("acc.designerTemplateName"): WtfGlobal.getLocaleText("acc.field.SelectTemplate")),
            id:"templateModelCombo"+this.heplmodeid+this.id,
            store: this.moduleTemplateStore,
            valueField:'templateId',
            displayField:'templateName',
            hideTrigger:this.isViewTemplate,
            hirarchical:true,
            emptyText:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.isExciseTab)? WtfGlobal.getLocaleText("acc.field.SelectExciseUnit"):WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            hidden:this.isTemplate ,
            hideLabel:this.isTemplate,
            forceSelection: true,
            selectOnFocus:true,
            addNoneRecord: true,
            allowBlank:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseTab)? false:true,
            width : 200,
            triggerAction:'all',
            scope:this,
            listeners:{
                'select':{
                    fn:function(){
                        if(this.templateModelCombo.getValue() != ""){
                            this.loadingMask = new Wtf.LoadMask(document.body,{
                                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
                            });
                            this.loadingMask.show();
                            var templateId = this.templateModelCombo.getValue();
                            var recNo = this.moduleTemplateStore.find('templateId', templateId);
                            var rec = this.moduleTemplateStore.getAt(recNo);
                            var moduleId = rec.get('moduleRecordId');
                            this.fillData(rec);
                            
                      }else{
                            this.ItemDetailGrid.EditorStore.removeAll();
                            this.ItemDetailGrid.addRec();
                        }
                    },
            scope:this            
                },
                 'beforeselect':{fn:function(){
                         this.ItemDetailGrid.itemEditorStore.load();
                     },scope:this
                 } 
                     
                 }
        });
        
        this.moduleTemplateName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.campaigndetails.campaigntemplate.templatename"),
            name: 'moduletempname',
            id:"moduletempname"+this.id,
            width : 200,
            maxLength:50,
            scope:this
        });
        
        this.createAsTransactionChk = new Wtf.form.Checkbox({
            fieldLabel : WtfGlobal.getLocaleText("acc.field.CreateTransactionAlso"),
            name:'createAsTransactionChkbox',
            cls : 'custcheckbox',
            width : 10
        });
        
        this.SelectedTemplateRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'gstIncluded'}
    ]);
    
    this.SelectedTemplateStoreUrl = "";
        this.SelectedTemplateStoreUrl= this.businessPerson=="Customer" ? "ACCSalesOrderCMN/getSalesOrdersMerged.do":"ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do"  
    
    this.SelectedTemplateStore = new Wtf.data.Store({
            url:this.SelectedTemplateStoreUrl,
            scope:this,
           baseParams:{
                archieve:0,
                deleted:false,
                nondeleted:false,
                cashonly:(this.cash == undefined)?false:this.cash,
                creditonly:false,
                consolidateFlag:false,
                companyids:companyids,
                enddate:'',
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                startdate:''
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SelectedTemplateRec)
        });
        
        this.SelectedTemplateStore.on('loadexception', function(){
           this.loadingMask.hide();
        },this);
        this.moduleTemplateStore.load();
        
    },
    hideFormFields: function() {
        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.stockRequest);
    },
    hideTransactionFormFields: function(array) {
        if (array) {
            for (var i = 0; i < array.length; i++) {
                var fieldArray = array[i];
                if (Wtf.getCmp(fieldArray.fieldId + this.id)) {
                    if (fieldArray.isHidden) {
                        Wtf.getCmp(fieldArray.fieldId + this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId + this.id).hidden = fieldArray.isHidden;
                    }
                    if (fieldArray.isReadOnly) {
                        Wtf.getCmp(fieldArray.fieldId + this.id).disabled = fieldArray.isReadOnly;
                    }
                    if (fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel != undefined) {
                        Wtf.getCmp(fieldArray.fieldId + this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        var fieldLabel = "";
                        if (fieldArray.fieldLabelText != "" && fieldArray.fieldLabelText != null && fieldArray.fieldLabelText != undefined) {
                            fieldLabel = fieldArray.fieldLabelText + " *";
                        } else {
                            fieldLabel = (Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel = fieldLabel;
                    } else {
                        if (fieldArray.fieldLabelText != "" && fieldArray.fieldLabelText != null && fieldArray.fieldLabelText != undefined) {
                            Wtf.getCmp(fieldArray.fieldId + this.id).fieldLabel = fieldArray.isManadatoryField ? fieldArray.fieldLabelText + " *" : fieldArray.fieldLabelText;
                        }
                    }
                }
            }
        }
    },
    getItemDetail:function (){
        this.ItemDetailGrid = new Wtf.goodsordergrid({
            layout:"fit",
            gridTitle:WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetails"),
            border:false,
            region:"center",
            prodIds: this.prodIds,
            parent:this,
            disabledClass:"newtripcmbss"
        });
    },
    fillRequestedItems: function(productIdArray){
        this.ItemDetailGrid.itemEditorStore.on('load', function(){
            var row = 0;
            for(var i= 0; i< productIdArray.length ; i++){
                var added = this.ItemDetailGrid.fillSelectedRecValue(row, productIdArray[i]);
                if(added){
                    row++;
                }
            }
        }, this)
    },
    printRecordTemplate:function(printflg,item){
        var params= "myflag=order&transactiono="+Wtf.OrderNoteNo+"&moduleid="+Wtf.Acc_Stock_Request_ModuleId+"&templateid="+item.id+"&recordids="+Wtf.recordbillid+"&filetype="+printflg;  
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
        document.body.appendChild(mapForm);
        mapForm.submit();
        var myWindow = window.open("", "mywindow","menubar=1,resizable=1,scrollbars=1");
        var div =  myWindow.document.createElement("div");
        div.innerHTML = "Loading, Please Wait...";
        myWindow.document.body.appendChild(div);
        mapForm.remove();
    },
    
    SaveItem:function (){
//        this.submitBttn.disable();
//        this.savencreateBttn.disable();
        var jsondata = "";
        if((this.fromstoreCombo.getValue() == "" || this.fromstoreCombo.getValue() == null)){
            WtfComMsgBox(["Info", "Please Select Store."], 0);
//            this.enableButtons();
            this.fromstoreCombo.markInvalid("This field is required");
            return;
        }
        if((this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
            WtfComMsgBox(["Info", "Please Select MoD for Goods Order"], 0);
//            this.enableButtons();
            this.MOUTextField.markInvalid("This field is required");
            return;
        }
        if(this.ItemDetailGrid.EditorStore.getCount() == 1){
            WtfComMsgBox(["Info", "Please add Item(s) to order"], 0);
//            this.enableButtons();
            return;
        }
//        var modRecs = this.ItemDetailGrid.EditorStore.getModifiedRecords();
//        if(modRecs.length == 0){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stockrequest.Pleaseenterquantityforatleastoneitem")], 0);
////            this.enableButtons();
//            return;
//        }
/*
 * Validate sequnce no for document only not for template 
 */
        if(this.sequenceFormatNO.getValue() == "NA" && this.documentNumber.getValue().trim() == "" && !this.isTemplate ){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stockrequest.PleaseentervalidDocumentNo")], 0);
            return;
        }
        var dataArr=new Array();
        var loadingMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        
        for(var i = 0; i<this.ItemDetailGrid.EditorStore.getCount()-1; i++){
             var rec  = this.ItemDetailGrid.EditorStore.getAt(i);
            if(this.validateRecord(rec)){
                var finalremark="";
                finalremark=  rec.data.remark;
                var jObject={};
            
                jObject.recordid =rec.data.recordid;
                jObject.productid = rec.data.productid ;
                jObject.productname = rec.data.productname ;
                jObject.pid = rec.data.pid ;
                jObject.quantity =rec.data.quantity;
                jObject.uomid =  rec.data.uomid;
                jObject.uomname =  rec.data.uomname;
                jObject.packaging =  rec.data.packaging;
                jObject.packagingid =  rec.data.packagingid;
                jObject.confactor =  rec.data.confactor;
                jObject.costcenter = rec.data.costcenter ;
                jObject.projectnumber=rec.data.projectnumber;
                jObject.shelflocation= rec.data.selfloactionName;
                jObject.warehouse= rec.data.warehouse;
                jObject.remark =finalremark;
                
                var linelevelcustomdata = Wtf.decode(WtfGlobal.getCustomColumnData(this.ItemDetailGrid.EditorStore.data.items[i].data, Wtf.Acc_Stock_Request_ModuleId).substring(13));
                if (linelevelcustomdata.length > 0)
                    jObject.linelevelcustomdata =linelevelcustomdata;
               
                dataArr.push(jObject);
               
            }else{
//                this.enableButtons();
                WtfComMsgBox(["Info", "Please enter valid data for Order "], 0);
                return;
            }
        }
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(!isValidCustomFields){
            return;
        }
        var custFieldArr=this.tagsFieldset.createFieldValuesArray();
        var dimencustomfield="";
        if (custFieldArr.length > 0)
            dimencustomfield = JSON.stringify(custFieldArr);
        var finalStr = JSON.stringify(dataArr);
        if(this.someForm.form.isValid() || this.isTemplate){
            this.submitBttn.disable();
            this.savencreateBttn.disable();
            loadingMask.show();
            this.someForm.form.submit({
                params:{
                    myflag:'order',
                    jsondata:finalStr,
                    partnerid: this.MOUTextField.getValue(),
                    sendToSuperVisor:true,
                    seqFormatId:this.sequenceFormatNO.getValue(),
                    documentNumber:this.documentNumber.getValue(),
                    customfield:dimencustomfield,
                    UomSchemaType:!Wtf.account.companyAccountPref.UomSchemaType,
                    istemplate : this.isTemplate?1:0,
                    templatename : this.moduleTemplateName.getValue()
                },
                scope:this,
                success:function (result,resp){
                    loadingMask.hide();
                    var retstatus = eval('('+resp.response.responseText+')');
                    var retmsg=retstatus.data.msg;
                    if(retstatus.data.success){
                        Wtf.MessageBox.show({
                            msg: retmsg,
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK,
                            title:"Success"
                        });
                        
                        if(retstatus.data.OrderNoteNo != "" && retstatus.data.OrderNoteNo != undefined){
                            printoutOrderNote(retstatus.data.OrderNoteNo);
                        }
                        Wtf.recordbillid=resp.result.data.billid;
                        Wtf.OrderNoteNo=resp.result.data.OrderNoteNo;
                        this.disableafterSaveButtons(this.saveOnlyFlag);
                        this.enableafterSaveButtons(this.saveOnlyFlag);
                        if(!this.saveOnlyFlag){
                            this.moduleTemplateStore.load();
                            this.someForm.form.reset();
                            this.ItemDetailGrid.EditorStore.removeAll();
                            this.ItemDetailGrid.addRec();
                        }
                        this.isAfterSaveNCreateNew=true;
                    } else if(retstatus.data.msg){
                        WtfComMsgBox(["Stock Request",retstatus.data.msg],retstatus.data.success*2+2);

                    }else {
                        Wtf.MessageBox.show({
                            msg:retmsg,
                            icon:Wtf.MessageBox.ERROR,
                            buttons:Wtf.MessageBox.OK,
                            title:"Error"
                        });
                    }
                    if(this.saveOnlyFlag && retstatus.data.success){
                        this.someForm.disable();
                        this.ItemDetailGrid.disable();
                    }else{
                        /**
                         * CODE COMMENTED BECAUSE if success is false then form should not be reset
                         */
//                        this.someForm.form.reset();
//                        this.ItemDetailGrid.EditorStore.removeAll();
//                        this.ItemDetailGrid.addRec();
//                        this.tagsFieldset.resetCustomComponents();
                        this.enableButtons();
                        this.Store.load({
                            params:{
                                isActive : "true",
                                byStoreManager:"true",
                                byStoreExecutive:"true"
                            }
                        });

                        this.SeqFormatstore.reload();
                    }
                },
                failure:function (result,resp){
                    loadingMask.hide();
                    var msg = result.data.msg;
                    this.enableButtons();
                    if(!msg){
                        msg = "Error while sending Item Order request";
                    }
                    Wtf.MessageBox.show({
                        msg:msg,
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK,
                        title:"Error"
                    });
                    this.someForm.form.reset();
                    this.ItemDetailGrid.EditorStore.removeAll();
                    this.ItemDetailGrid.addRec();
                    this.enableButtons();
                }
            });
        }else{
            this.submitBttn.enable();
        }
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
            this.cancelBttn.disable();
            this.savencreateBttn.disable();
        }else{//save and create new button
            this.singleRowPrint.disable();
        }
    },
    enableButtons:function(){
        this.submitBttn.enable();
        this.savencreateBttn.enable();
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
    /**
     * Populate details in form on template selection
     */
    fillData : function(rec){
      this.loadingMask.hide();
      this.ItemDetailGrid.EditorGrid.getStore().proxy.conn.url = "INVGoodsTransfer/getSingleStockRequestToLoad.do";
      this.ItemDetailGrid.loadGridStore(rec,this.id);
    }
});

//------------------------------------------Editor Grid Component---------------------------------------------------

Wtf.goodsordergrid = function (config){
    Wtf.apply(this,config);
    Wtf.goodsordergrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.goodsordergrid,Wtf.Panel,{
    initComponent:function (){
        Wtf.goodsordergrid.superclass.initComponent.call(this);
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
                html:"<div class='gridTitleClass' style='float:left;'>"+this.gridTitle+"</div><div style = 'float:right; font-size:9px;'> "+WtfGlobal.getLocaleText("acc.field.Note")+" : "+WtfGlobal.getLocaleText("acc.stock.templatenote")+"</div>"
            },
            this.EditorGrid
            ]
        });
        this.add(this.tmpPanel);
    },
    getEditorGrid:function (){
                
        this.costCenterRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"name"
        },
        {
            name:"ccid"
        },
        {
            name:"description"
        }
        ]);
        this.costCenterReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.costCenterRec);

        this.costCenterStore = new Wtf.data.Store({
            url:  'CostCenter/getCostCenter.do',
            reader:this.costCenterReader
        });
        this.costCenterStore.load();
        this.costCenterCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.costCenterStore,
            displayField:"ccid",
            valueField:"id",
            width:200
        });
        
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
        //        this.packagingStore.load();
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
            listWidth:50,
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
            name:"orderinguomid"
        },

        {
            name:"uomname"
        },
        {
            name:'orderinguomname'
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
            name:"reorderQuantity"
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
            name:"uomschematype"
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
                isWarehouseLocationSet:true,
                isInventoryForm:true
            },
            reader:this.itemEditorReader
        });

        this.itemEditorCombo=new Wtf.form.ExtFnComboBox({
            name:'itemdescription',
            store:this.itemEditorStore,    
            typeAhead: true,
            selectOnFocus:true,
            valueField:'id',
            displayField:'itemdescription',
            extraFields:[],
            listWidth:200,
            mode:'remote',
            hideTrigger:true,
            scope:this,
            triggerAction : 'all',
            editable : true,
            minChars : 1,
            forceSelection:true
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
                    isInventoryForm:true,
                    isWarehouseLocationSet:true
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
                            url: this.itemEditorStore.url,
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
            name:"ccpartnumber"
        },

        {
            name:"itemdescription"
        },
        
        {
            name:"packaging"
        },
        {
            name:"packagingid"
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
            name:"quantity"
        },

        {
            name:"uomid"
        },

        {
            name:"remark"
        },

        {
            name:"projectnumber"
        },
        {
            name:"costcenter"
        },
        {
            name:"avaquantity"
        },

        {
            name:"selfloactionName"
        },
        {
            name:"uomschematype"
        },{
            name:"productname"
        },{
            name:"productid"
        },{
            name:"uomname"
        },{
            name:"projectnumber"
        },{
            name:"remark"
        },{
            name:"pid"
        },{
            name:"fromstore"
        },{
            name:"date"
        }
        ]);

        this.EditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.EditorRec);

        this.EditorStore = new Wtf.data.Store({
            url:"jspfiles/inventory/store.jsp",
            pruneModifiedRecords:true,
            reader:this.EditorReader
        });
        
        this.EditorStore.on('load', function(){
            this.EditorGrid.getView().refresh();
        },this);
       
        WtfGlobal.updateStoreConfig(GlobalColumnModel[Wtf.Acc_Stock_Request_ModuleId], this.EditorStore);
        this.addRec();
    
        var cmWidth=165;
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
                header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
                width:65,
                align:'center',
                name:'srno',
                dataIndex:"",
                hidden:true,
//                renderer: Wtf.applySequenceRenderer
                renderer: WtfGlobal.itemSequenceRenderer
            },
            {
                header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                //sortable:true,
                dataIndex:"pid",
                width:cmWidth,
//                renderer:this.getComboRenderer(this.itemCodeEditorCombo),
                editor:this.itemCodeEditorCombo
            },
            {
                header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
                // sortable:true,
                width:cmWidth,
                dataIndex:"productname"
            },
            {
                header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),
                width:cmWidth,
                dataIndex:"desc"
            },
            {
                header:WtfGlobal.getLocaleText("acc.je.CoilcraftPartNo"),
                // sortable:true,
                dataIndex:"partnumber",
                hidden: true//integrationFeatureFor == Wtf.IF.COILCRAFT ? false: true
            },
            {
                header:WtfGlobal.getLocaleText("acc.window.Warehouse"),
                dataIndex:"warehouse",
                hidden: true
            },
            {
                header:WtfGlobal.getLocaleText("acc.product.packaging"),
                dataIndex:"packaging",
                //                renderer:this.getComboRenderer(this.packagingCombo),
                //                editor:Wtf.account.companyAccountPref.UomSchemaType==false?this.packagingCombo:"",
                width:cmWidth,
                hidden:!Wtf.account.companyAccountPref.UomSchemaType?true:false
            },
            {
                header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
                dataIndex:"uomname",
                renderer:this.getComboRenderer(this.uomCombo),
                editor:this.uomCombo,
                width:cmWidth
            // sortable:true
            //editor:this.uomCombo,
            // renderer: Wtf.ux.comboBoxRenderer(this.uomCombo)
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),
                dataIndex:"confactor",
                hidden:Wtf.account.companyAccountPref.UomSchemaType?true:false,
                width:150,
                renderer:this.conversionFactorRenderer(this.itemEditorStore,"productid","uomname",this.EditorStore),
                editor:(Wtf.account.companyAccountPref.UomSchemaType===0) ?this.transBaseuomrate : ""     //Does allow to user to change conversion factor
            },
            {
                header:WtfGlobal.getLocaleText("acc.stock.AvailableQuantityinDefaultWarehouse"),
                dataIndex:"avaquantity",
                width:cmWidth
            //sortable:true,
            //                editor:new Wtf.form.NumberField({
            //                    scope: this,
            //                    allowBlank:false,
            //                    allowNegative:false,
            //                    allowDecimals:true,
            //                    decimalPrecision:4,
            //                    value:0,
            //                    listeners : {
            //                        'focus': setZeroToBlank
            //                    }
            //                })
            },{
                header:WtfGlobal.getLocaleText("acc.stock.RequestedQuantity"),
                dataIndex:"quantity",
                width:cmWidth,
                //sortable:true,
                editor:new Wtf.form.NumberField({
                    scope: this,
                    allowBlank:false,
                    allowNegative:false,
                    allowDecimals:true,
                    decimalPrecision:4,
                    value:0,
                    listeners : {
                        'focus': setZeroToBlank
                    }
                })
            },
            {
                header:WtfGlobal.getLocaleText("acc.common.costCenter"),
                dataIndex:"costcenter",
                width:cmWidth,
                hidden:true, // Refer SDP-1499
                renderer:this.getComboRenderer(this.costCenterCombo),
                editor:this.costCenterCombo
            },
            {
                header:WtfGlobal.getLocaleText("acc.stock.ProjectNo"),
                dataIndex:"projectnumber",
                editor:new Wtf.form.TextField({})
            },
            {
                header:WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
                dataIndex:"remark",
                width:cmWidth,
                // sortable:true,
                editor:new Wtf.form.TextArea({
                    regex:Wtf.validateAddress,
                    maxLength:200
                })
            });
            columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModel[Wtf.Acc_Stock_Request_ModuleId]);
            columnArr.push({
                header: WtfGlobal.getLocaleText("mrp.rejecteditems.report.actioncolumn.title"),
                align: 'center',
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
//            id:"editorgrid2sd",
            autoScroll:true,
            store:this.EditorStore,
            layout:'fit',
            forceFit:true,
            viewConfig:{
                forceFit:false,
                getRowClass: function(record, index) {
                    if(record.data.productid!="" && (record.data.orderinguomname=="" || record.data.warehouse == "")){
                        return "oc_numbercls";
                    }
                }
            //                emptyText:"No items are available for this category"
            },
            clicksToEdit:1
        });
        this.EditorGrid.on("render",this.hideShowCustomizeLineFields,this);
        this.EditorGrid.on('rowclick',this.handleRowClick,this);
        this.EditorGrid.on("afteredit",this.fillGridValue,this);
        this.EditorGrid.on("beforeedit",this.loadUom,this);
        this.EditorGrid.on("cellclick",this.cellClick,this);
        this.EditorGrid.on('populateDimensionValue',this.populateDimensionValueingrid,this);
    },
    hideShowCustomizeLineFields: function() {
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getCustomizedReportFields.do",
            params: {
                flag: 34,
                moduleid: Wtf.Acc_Stock_Request_ModuleId,
                reportId: 1,
                isFormField: true,
                isLineField: true
            }
        }, this, function(action, response) {
            if (action.success && action.data != undefined) {
                this.customizeData = action.data;
                var cm = this.EditorGrid.getColumnModel();
                for (var i = 0; i < action.data.length; i++) {
                    for (var j = 0; j < cm.config.length; j++) {
                        if (cm.config[j].dataIndex == action.data[i].fieldDataIndex) {
                            cm.setHidden(j, action.data[i].hidecol);
                            cm.setEditable(j, !action.data[i].isreadonlycol);
                            if (action.data[i].fieldlabeltext != null && action.data[i].fieldlabeltext != undefined && action.data[i].fieldlabeltext != "") {
                                cm.setColumnHeader(j, action.data[i].fieldlabeltext);
                            }
                        }
                    }
                }
                this.reconfigure(this.store, cm);
            }
        });
    },
    addProductList:function(){
        return "<div class='pwnd add-gridrow' wtf:qtip=\"Click to add products\"></div>";
    },
    /**
     * Load data at line as well as global level after Template selection
     */
    loadGridStore:function(rec,id){
        this.EditorStore.load({
            params:{
             templatename:rec.data.templateName
            }
        }); 
        this.EditorStore.on('load',function(store, recArr){ 
            /*
             * set global data
             */
            if(recArr.length>0){
                var record=recArr[0];
                if(record.data){
                    this.parent.fromstoreCombo.setValue(record.data.fromstore);
                                    if (record.data.date) {
                    var date = record.data.date;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        Wtf.getCmp("businessdate"+id).setValue(datenew);
                    }else{
                        Wtf.getCmp("businessdate"+id).setValue(new Date());
                    }
                }
                        
                }
                
            }
            var arrayOfRecords = [];    //taken array to push all records at a time.
            this.EditorStore.removeAll();
            for(var count=0;count<recArr.length;count++){
                var record=recArr[count];
                    arrayOfRecords.push(record);
            } 
            this.EditorStore.add(arrayOfRecords);
            this.addRec();   
           this.getView().refresh();
        },this);
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
            moduleid:Wtf.Acc_Stock_Request_ModuleId,
            modulename:"GOODS_REQUEST",
            invoiceGrid:this.EditorGrid,
            parentCmpID:this,
            isFromInventorySide:true,
            isCustomer : false,
            isStoreLocationEnable:true,
            warehouseId:this.parent.fromstoreCombo.getValue()
        });
        this.productSelWin.show();
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
        } else if (e.getTarget(".add-gridrow")) {//ERP-12415[SJ] 
            this.showProductGrid();
        }
        
        if (e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRow(grid, 0);
        }
        if (e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRow(grid, 1);
        }
    },
    cellClick :function(grid, rowIndex, columnIndex, e){
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        if(fieldName=="uomname"&& !record.get("uomschematype")&&!Wtf.account.companyAccountPref.UomSchemaType){
            return false;
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
                return "1 "+ uomname +" = "+ +value+" "+rec.data["stockuom"];
            }else{
                rec = store.getAt(idx);
                return "1 "+ uomname +" = "+ +value+" "+rec.data[displayField];
            }  
            
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
    
    setZeroToBlank : function(field){
        if(field.getValue()==0){
            this.quantityTextField.setValue("");
        }
    },
    
    NewlineRemove : function (str){
        if (str)
            return str.replace(/\n/g, ' ');
        else
            return str;
    },
    
    addRec: function () {
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
        if(e.field =='pid') {
            
            var index=this.itemEditorStore.find("productid",e.value);
            var warehouse=this.itemEditorStore.getAt(index).get("warehouse");
            if(warehouse != undefined && warehouse != "" ){
                var filterJson='[';
                filterJson+='{"warehouse":"'+warehouse+'","productid":"'+e.value+'"},';
                filterJson=filterJson.substring(0,filterJson.length-1);
                filterJson+="]";  

                Wtf.Ajax.requestEx({
                    url:"ACCInvoice/getBatchRemainingQuantity.do",
                    params: {
                         batchdetails: filterJson
                    }
                },
                this,
                function(action, response){ 
                    if(action.success == true){
                        var availQty=action.quantity;
                        this.itemEditorStore.getAt(index).set("avaquantity",availQty)
                        this.fillSelectedRecValue(e.row, e.value)
                        
                    }else{
                        WtfComMsgBox(["Error", "Some error has occurred."],0);
                        return false;
                    }
                    
                },
                function(){
                    WtfComMsgBox(["Error", "Some error has occurred."],0);
                    return false;
                
                
                });  
            }else{
                this.fillSelectedRecValue(e.row, e.value)
            }
        }else{
            this.fillSelectedRecValue(e.row, e.value)
        }
        
        if(e.field =='uomname'&&!Wtf.account.companyAccountPref.UomSchemaType) {
             
            this.getProductBaseUOMRate(this.EditorStore.getAt(e.row).get("productid"),this.EditorStore.getAt(e.row).get("uomname"),e.row);
        }
    
    },
    fillSelectedRecValue: function(row, value){
        var added = false;
        var alreadyAdded = false;
        var i;
        
        for(i=0;i<this.itemEditorStore.getCount();i++){
            if(this.itemEditorStore.getAt(i).get("productid") == value){
                this.EditorStore.getAt(row).set("productid",value);
                this.EditorStore.getAt(row).set("pid",this.itemEditorStore.getAt(i).get("pid")); 
                var reorderQty=this.itemEditorStore.getAt(i).get("reorderQuantity");
                //                this.EditorStore.getAt(row).set("quantity",(reorderQty !="" && reorderQty != undefined) ? reorderQty :this.itemEditorStore.getAt(i).get("quantity"));
//                this.EditorStore.getAt(row).set("avaquantity",(reorderQty !="" && reorderQty != undefined) ? reorderQty :this.itemEditorStore.getAt(i).get("avaquantity"));
                this.EditorStore.getAt(row).set("avaquantity", this.itemEditorStore.getAt(i).get("avaquantity"));
                this.EditorStore.getAt(row).set("productname",this.itemEditorStore.getAt(i).get("productname"));
                this.EditorStore.getAt(row).set("desc",this.itemEditorStore.getAt(i).get("desc"));
                this.EditorStore.getAt(row).set("packaging",this.itemEditorStore.getAt(i).get("packaging"));
                this.EditorStore.getAt(row).set("packagingid",this.itemEditorStore.getAt(i).get("packagingid"));
                this.EditorStore.getAt(row).set("uomid",this.itemEditorStore.getAt(i).get("orderinguomid"));
                this.EditorStore.getAt(row).set("stockuom",this.itemEditorStore.getAt(i).get("uomname"));
                if(!Wtf.account.companyAccountPref.UomSchemaType){
                    this.EditorStore.getAt(row).set("uomname",this.itemEditorStore.getAt(i).get("uomname"));
                }else{
                    this.EditorStore.getAt(row).set("uomname",this.itemEditorStore.getAt(i).get("orderinguomname"));
                }
                //                this.EditorStore.getAt(row).set("uomname",this.itemEditorStore.getAt(i).get("orderinguomname"));
                this.EditorStore.getAt(row).set("partnumber",this.itemEditorStore.getAt(i).get("partnumber"));
                this.EditorStore.getAt(row).set("costcenter",this.itemEditorStore.getAt(i).get("costcenter")); 
                this.EditorStore.getAt(row).set("warehouse",this.itemEditorStore.getAt(i).get("warehouse")); 
                this.EditorStore.getAt(row).set("uomschematype",this.itemEditorStore.getAt(i).get("uomschematype"));
                added = true;
                
                if(this.itemEditorStore.getAt(i).get("orderinguomname") == "" || this.itemEditorStore.getAt(i).get("warehouse") == "" ){
                //                    this.EditorGrid.getView().getRow(row).style = "background-color :pink";
                }
                //                this.loadPackagingStore(this.itemEditorStore.getAt(i).get("productid"));
                if(!Wtf.account.companyAccountPref.UomSchemaType){
                    this.getProductBaseUOMRate(value,this.itemEditorStore.getAt(i).get("uomid"),row);
                }
            }
        }
        for(i=0;i<this.EditorStore.getCount();i++){
            if(i != row && this.EditorStore.getAt(i).get("productid") == value && this.EditorStore.getAt(i).get("productid")){
                alreadyAdded = true;
                var blankrec = this.EditorStore.getAt(row);
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
        }else if(added && row == this.EditorStore.getCount()-1){
            this.addRec();
        }
        return added;
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
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(Wtf.Acc_Stock_Request_ModuleId, rec, this.EditorGrid);
    }
});


function printoutOrderNote(orderNoteNo){

    var URL="INVGoodsTransfer/getStockRequestDetailBySequenceNo.do"; 

    var printTitle = "Goods Order Note";
    var pcase = "Order";
    var printflg = "printorder";
    
    Wtf.Ajax.requestEx({
        url:URL,
        params: {
            sequenceNo : orderNoteNo,
            moduleName : "STOCK_REQUEST"
        }
    },
    this,
    function(result, req){

        var msg=result.msg;
        var title="Error";
        if(result.success){

            var rs=result.data;
            
            var cnt = rs.length;
           
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
            +  (printflg ==="printorder" || printflg === "printissueNote" || printflg === "interstore"? "" : "<b>Order ID : </b>" +  result.data[0].transfernoteno + "<br><br>") 
            + "<b>" + pcase+  " Note No : </b>" +  result.data[0].transfernoteno + "<br><br>"
            + (printflg ==="printorder" || printflg === "printissueNote" || printflg === "interstore" ? "<b>Date : </b>" : "<b>Shipping Date : </b>")+ (printflg === "interstore"? result.data[0].date :(printflg ==="printorder" || printflg === "printissueNote" || printflg === "interstore" ? result.data[0].date :new Date(result.data[0].issuedOn.replace(/[-]/gi, '/')).format('Y-m-d')))
            + "</span>"
            + "</div>"
            + "<div style='margin-top:140px;width: 95%;'>";
            
            if (printflg ==="printorder") {
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
            
            if (printflg ==="printorder") {
                htmlString += "<tr>"
                +"<th>S/N</th><th>Product ID</th>"
                +"<th>Product Name</th>"
                +"<th>Product Description</th>"
                //                    +(printflg === "printissueNote" || printflg === "interstore"?"<th> HS Code</th>":"")
                +"<th>Uom</th>"
                +"<th>Quantity</th>";
                htmlString += "<th>Remark</th>";
                /*
                 * Line Level Data Index
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
                
                if (printflg === "printorder") {
                    htmlString += "<tr align='center'><td>" + count + "&nbsp;</td><td>" + result.data[i].itemcode + "&nbsp;</td><td>" +result.data[i].itemname + "&nbsp;</td><td align='left'>"+result.data[i].itemdescription+"&nbsp;</td>"+(printflg === "printissueNote" || printflg === "interstore"?"<td>" + (result.data[i].hscode == undefined ? "": result.data[i].hscode) + "&nbsp;</td>":"")+"<td>" +result.data[i].name + "&nbsp;</td><td>" + result.data[i].quantity + "&nbsp;</td>"+
                    "<td>" + result.data[i].remark + "&nbsp;</td>";

                    /*
                     * Line Level Custom data
                     */
                    for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
                        if (linedata[lineFieldCount].header !== undefined && result.data[i][linedata[lineFieldCount].dataIndex] !== undefined) {
                            htmlString += "<td>" + result.data[i][linedata[lineFieldCount].dataIndex] + "</td>";
                        } else {
                            htmlString += "<td>" + "" + "</td>";
                        }
                    }
                    htmlString += "</tr>";
                    count++;
                }
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


function setPIDForBarcode (obj,prorec,field,isTypeAhead,isStockAdjustment){
    var rawValue = field.getRawValue();
    var count = obj.EditorStore.getCount();
    var rec = obj.EditorStore.getAt(count-1);
                            
    for (var key in prorec) {
        if (prorec.hasOwnProperty(key)) {
            rec.data[key] = prorec[key];
        }
    }
    rec.data.productid = prorec.productid;
    rec.data.productCode = rawValue;
    rec.data.quantity = "";
    var e = {
        grid:obj.EditorGrid,
        field : 'pid',
        value : prorec.productid,
        row: count-1,
        record : rec,
        cancel:false
    };
    if(!isStockAdjustment){
        obj.loadUom(e);
    }
    //        if(isTypeAhead){
    /*
             *In Case we are fetching data from backend so it takes time and afteredit event of grid is called before we get response from backend..
             *So manually firing afteredit event for product id.
             **/
    obj.EditorGrid.fireEvent('afteredit', e);
//        }
}
