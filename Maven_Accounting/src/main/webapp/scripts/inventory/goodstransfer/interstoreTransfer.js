Wtf.transfer = function (config){
    Wtf.apply(this,config);
    Wtf.transfer.superclass.constructor.call(this);
}

Wtf.extend(Wtf.transfer,Wtf.Panel,{
    initComponent:function (){
        Wtf.transfer.superclass.initComponent.call(this);
        this.getForm();
        this.getItemDetail( this.qarejected);
    
    
        /*Print Single Record*/
        this.isAutofill=false;
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        var colModArray = [];
        if(this.isJobWorkStockOut){
            //get job work out stock transfer module templates
            colModArray = GlobalCustomTemplateList[Wtf.autoNum.JobWorkOutStockTransferModuleID];
        } else{
            colModArray = GlobalCustomTemplateList[Wtf.Acc_InterStore_ModuleId];
        }
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
                Wtf.Msg.show({
                    title: WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg: WtfGlobal.getLocaleText("acc.IST.Warning"),
                    buttons: Wtf.Msg.YESNOCANCEL,
                    scope: this,
                    fn: function (btn) {
                        if (btn == 'yes') {
                            this.isAutofill = true;
                            this.fillAvailableQtyBeforeSaveItem()
                        } else if (btn == 'no') {
                            this.SaveItem();
                        } else {
                            return;
                        }
                    },
                    icon: Wtf.MessageBox.QUESTION
                });
                
//                Wtf.MessageBox.confirm("Confirm",WtfGlobal.getLocaleText("acc.IST.Warning"), function(btn){
//                    if(btn == 'yes') {     
//                        this.isAutofill=true;
//                        this.fillAvailableQtyBeforeSaveItem()
//                    }else if(btn == 'no') {
//                          return false;
//                       // this.SaveItem();
//                    }
//                },this);
            }
        });
        
        /*Save Button*/
        this.submitBttn=new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: {
                text:"Click to Save"
            },
            id:'interstoreTransferSubmit',
            iconCls:getButtonIconCls(Wtf.etype.save),
            handler:function (){
                if(this.documentNumber.getValue().trim() == "" && this.sequenceFormatNO.getValue()=="NA"){
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
                            this.fillAvailableQtyBeforeSaveItem()
                        } else if (btn == 'no') {
                            this.SaveItem();
                        } else {
                            return;
                        }
                    },
                    icon: Wtf.MessageBox.QUESTION
                });
                
//                Wtf.MessageBox.confirm("Confirm",WtfGlobal.getLocaleText("acc.IST.Warning"), function(btn){
//                    if(btn == 'yes') {     
//                        this.isAutofill=true;
//                        this.fillAvailableQtyBeforeSaveItem()
//                    }else if(btn == 'no') {
//                        return false;
////                        this.SaveItem();
//                    }
//                },this);
            },
            scope:this
        });
        
        /*Cancel Button*/
        this.cancelBttn=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            tooltip: {
                text:"Click to Cancel"
            },
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            scope:this,
            handler:function(){
                this.Form.form.reset();
                this.ItemDetailGrid.EditorStore.removeAll();
                this.ItemDetailGrid.addRec();
            //                    this.firemyajax();
            }
        });
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.Form,
            this.ItemDetailGrid
            ],
            bbar:[ this.submitBttn, "-",this.cancelBttn,'-',this.savencreateBttn,'-',this.singleRowPrint]
        });
        this.add(this.mainPanel);
    //        this.firemyajax();
    },

    firemyajax:function(){
        Wtf.Ajax.requestEx({
            url:"jspfiles/inventory/store.jsp",
            params:{
                flag:8
            }
        }, this,
        function(response){
            var res = eval('('+response+')');
            var rec = res.data[0];
            this.transferNoteNO.setValue(rec.transnoteno);
            //            if(checktabperms(12, 1) != "edit"){
            this.MOUTextField.setValue(rec.username);
            //            }
            this.KeyField.setValue(rec.key);
            if(this.fromStore.getCount() > 0) {
                this.Store.clearFilter();
                if(this.fromStore.find('store_id', rec.storeid) == -1) {
                    this.fromstoreCombo.setValue(this.fromStore.getAt(0).data.store_id);
                    this.Store.filterBy(function(record){
                        if(record.get("store_id")==this.fromStore.getAt(0).data.store_id){
                            return false;
                        }
                        return true;
                    },this);
                } else {
                    this.fromstoreCombo.setValue(rec.storeid);
                    this.Store.filterBy(function(record){
                        if(record.get("store_id")==rec.storeid){
                            return false;
                        }
                        return true;
                    },this);
                }
            }
            this.setBusinessDate();
        },
        function(){})
    },
    getForm:function (){
        this.Name = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.invoiceList.ven.tt") + "'>" + WtfGlobal.getLocaleText("acc.invoiceList.ven") + "</span>",
            id: "vendor" + this.id,
            store: Wtf.vendorAccRemoteStore,
            valueField: 'accid',
            name: 'vendor',
            displayField: 'accname',
            minChars: 1,
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 300 : 300,
            allowBlank: !this.isJobWorkStockOut,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.ven"),
            mode: 'remote',
            typeAhead: true,
            hidden: !this.isJobWorkStockOut,
            hideLabel: !this.isJobWorkStockOut,
            extraComparisionField: 'acccode',
            typeAheadDelay: 30000,
            forceSelection: true,
            selectOnFocus: true,
            isVendor: false,
            isCustomer: true,
            width: 150,
            triggerAction: 'all',
            scope: this
        });
        this.Name.on("change", function() {
            this.jobWorkOrderRecCombo.enable();
            this.jobWorkOrderRecCombo.reset();
            this.POStore.load({
                params:{
                    id:this.Name.getValue()
                }
            });
        }, this);
        this.POStoreRec = Wtf.data.Record.create([
            {name: 'billid'},
            {name: 'journalentryid'},
            {name: 'entryno'},
            {name: 'billto'},
            {name: 'discount'},
            {name: 'shipto'},
            {name: 'mode'},
            {name: 'billno'},
            {name: 'date', type: 'date'},
            {name: 'duedate', type: 'date'},
            {name: 'shipdate', type: 'date'},
            {name: 'personname'},
            {name: 'creditoraccount'},
            {name: 'personid'},
            {name: 'shipping'},
            {name: 'othercharges'},
            {name: 'taxid'},
            {name: 'productid'},
            {name: 'discounttotal'},
            {name: 'isAppliedForTax'}, // in Malasian company if DO is applied for tax
            {name: 'discountispertotal', type: 'boolean'},
            {name: 'currencyid'},
            {name: 'currencysymbol'},
            {name: 'amount'},
            {name: 'amountinbase'},
            {name: 'amountdue'},
            {name: 'costcenterid'},
            {name: 'lasteditedby'},
            {name: 'costcenterName'},
            {name: 'memo'},
            {name: 'shipvia'},
            {name: 'fob'},
            {name: 'includeprotax', type: 'boolean'},
            {name: 'salesPerson'},
            {name: 'islockQuantityflag'},
            {name: 'agent'},
            {name: 'termdetails'},
            {name: 'LineTermdetails'}, //Line Level Term Details
            {name: 'shiplengthval'},
            {name: 'gstIncluded'},
            {name: 'quotationtype'},
            {name: 'contract'},
            {name: 'termid'},
            {name: 'externalcurrencyrate'}, //    ERP-9886
            {name: 'customerporefno'},
            {name: 'isexpenseinv'},
        ]);
        this.POStore = new Wtf.data.Store({
            url: "ACCPurchaseOrderCMN/getPurchaseOrders.do",
            baseParams: {
                isJobWorkOrderReciever: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, this.POStoreRec)
        });
        this.jobWorkOrderRecCombo = new Wtf.form.ComboBox({
            triggerAction: "all",
            mode: "remote",
            typeAhead: true,
            forceSelection: true,
            store: this.POStore,
            hideLabel: !this.isJobWorkStockOut,
            hidden: !this.isJobWorkStockOut,
            displayField: "billno",
            valueField: "billid",
            fieldLabel: WtfGlobal.getLocaleText("acc.jobworkout.title"),
            allowBlank: !this.isJobWorkStockOut,
            width: 150,
            parent: this
        });
        this.challanNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.JobWorkOut.challanno") + "*",
            name: 'challanNo',
            id: 'PAchallonno',
            width: 150,
            hidden: !this.isJobWorkStockOut,
            hideLabel: !this.isJobWorkStockOut,
            disabled: false,
            allowBlank: !this.isJobWorkStockOut

        });
        this.jobWorkOrderRecCombo.on("change", function() {
            this.loadPurchaseOrderProduct();
            this.challanNo.enable();
            this.challanNo.reset();
        }, this);

        this.storeRec = new Wtf.data.Record.create([
        {
            name:"store_id"
        },

        {
            name:"fullname"
        },
        {
             name:"unitname"
        }
        ]);

        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        this.Store = new Wtf.data.Store({
            url:"INVStore/getStoreList.do",
            reader:this.storeReader
        });
        this.Store.on("load", function(ds, rec, o){
            if(rec.length > 0 && this.isJobWorkStockOut){
                 this.tostoreCombo.setValue(rec[0].data.store_id, true);
            }
        }, this);
        this.Store.load({
            params:{
                isActive:true,
                isFromInvTransaction:"true", //ERM-691 do not display Scrap/Repair Stores in regular Inventory Transactions
                isJobWorkStockOut:this.isJobWorkStockOut
            }
        });
        
        this.fromStore = new Wtf.data.Store({
            url:"INVStore/getStoreList.do",
            reader:this.storeReader
        });
        this.fromStore.load({
            params:{
                isActive:true,
                byStoreManager:"true",
                isFromInvTransaction:"true", //ERM-691 do not display Scrap/Repair Stores in regular Inventory Transactions
                byStoreExecutive:"true"
            //  action:"frm"
            }
        });
        this.fromStore.on("load", function(ds, rec, o){
            if(rec.length > 0  && this.qarejected != true){
                this.fromstoreCombo.setValue(rec[0].data.store_id, true);
          
            }

        }, this);
      
        this.fromstoreCombo = new Wtf.form.ExtFnComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.fromStore,
            displayField:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable && Wtf.exciseMultipleUnit)?"unitname":"fullname",
            valueField:"store_id",
            extraFields:Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?['fullname']:[],
            fieldLabel:WtfGlobal.getLocaleText("acc.je.FromStore*"),
            hiddenName:"fromstore",
            allowBlank:false,
            emptyText:WtfGlobal.getLocaleText("acc.je.SelectFromStore"),
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
        this.tostoreCombo = new Wtf.form.ExtFnComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            store:this.Store,
            forceSelection:true,
            displayField:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable && Wtf.exciseMultipleUnit)?"unitname":"fullname",
            extraFields:Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?['fullname']:[],
            valueField:"store_id",
            fieldLabel:WtfGlobal.getLocaleText("acc.je.ToStore*"),
            hiddenName:"tostore",
            emptyText:WtfGlobal.getLocaleText("acc.je.SelectToStore"),
            allowBlank:false,
            width:150
        });
        this.tostoreCombo.on("blur",this.tostoreComboBlur,this);
        this.fromstoreCombo.on("blur",this.fromstoreComboBlur,this);
        /*
          In case of indian compliance, User was unable to select warehouse in case of store transfer
          So to let user know that excise duty should be applied to warehouse provided a alert message 
        */
        this.tostoreCombo.on("select",function(combo,record){
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable && Wtf.exciseMultipleUnit){
                if(record && record.data && record.data.unitname){
                    return true;
                } else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.excise.unit.not.available")],2);
                }
            }
        });
        this.fromstoreCombo.on("select",function(combo,record){
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isExciseApplicable && Wtf.exciseMultipleUnit){
                if(record && record.data && record.data.unitname){
                    return true;
                } else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.excise.unit.not.available")],2);
                }
            }
        });
        
        this.transferNoteNO = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.stockrequest.TransferNo"),
            name:"trans note no",
            readOnly : true,
            width:150
        });
        this.yesterdayVal = new Date();
        this.yesterdayVal.setDate(new Date().getDate() + 1);
        this.dateField = new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.stock.BusinessDate")+"*",
            format:"Y-m-d",
            name:"trans date",
            readOnly:true,
            allowBlank:false,
            width:150,
            value:new Date()
        });
        this.MOUTextField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.je.MoD")+"*",
            readOnly : true,
            name:"trans mod",
            value:_fullName,
            width:150
        });
        this.documentNumber = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.DocumentNo"),
            name:'documentNo',
            maxLength:50,
            width : 150
        });
        this.sequenceFormatNO = new Wtf.SeqFormatCombo({
            seqNumberField : this.documentNumber,
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.sequenceformat")+"*",
            name:"seqFormat",
            moduleId:2,
            allowBlank:false,
            width : 150
        });
        this.KeyField = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.je.key"),
            readOnly : true,
            name:"key",
            width:150,
            hidden:true,
            hideLabel:true
        });
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm2" + this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: Wtf.Acc_InterStore_ModuleId,
            isEdit: false
        });
        
        this.memoTextArea= new Wtf.form.TextArea({
            width:150,
            fieldLabel:WtfGlobal.getLocaleText("acc.repeated.Gridcol3"),
            name:"memo",
            height:45,
            maxLength:2048,
//            allowBlank:false,
            value:this.isView?this.viewRec.data.memo:""
        });
        
        //        if(checktabperms(12, 1) == "edit"){
        //             this.MOUTextField.readOnly = false;
        //        }
        this.Form = new Wtf.form.FormPanel({
            region:"north",
            autoHeight:true,
            id:"northForm2" + this.id,
            url:"INVGoodsTransfer/addInterStoreTransferRequest.do",
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
                        columnWidth:0.33,
                        border:false,
                        layout:'form',
                        labelWidth:105,
                        items:[
                        this.fromstoreCombo,
                        this.sequenceFormatNO,
                        this.documentNumber,
                        this.memoTextArea 
                        ]
                    },{
                        columnWidth:0.33,
                        border:false,
                        layout:'form',
                        labelWidth:105,
                        items:[
                        this.tostoreCombo,
                        this.dateField,
                        this.challanNo
                        ]
                    },{
                        columnWidth:0.33,
                        border:false,
                        layout:'form',
                        labelWidth:105,
                        items:[
                        this.MOUTextField,
                        this.Name,
                        this.jobWorkOrderRecCombo
                        ]
                    },
                    {
                        columnWidth:0.25,
                        border:false,
                        layout:'form',
                        labelWidth:80,
                        items:[
                        this.KeyField
                        ]        
                           
                    }
                    ]
                },this.tagsFieldset
                ]
            }]       
        });
    },
    loadPurchaseOrderProduct: function() {
        this.ItemDetailGrid.EditorGrid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getPurchaseOrderRows.do";
        this.ItemDetailGrid.EditorGrid.getStore().load({
                params:{
                    bills:this.jobWorkOrderRecCombo.getValue(),
                    isForm:true,
                    isJobWorkStockOut:true,
                    storeId:this.fromstoreCombo.getValue()
                }
            });
        this.ItemDetailGrid.EditorGrid.getStore();    
//        this.ItemDetailGrid.addRec();
        this.ItemDetailGrid.EditorGrid.getView().refresh();  
    },
    tostoreComboBlur:function(){
        if(this.fromstoreCombo.getValue()==this.tostoreCombo.getValue()){
            this.tostoreCombo.reset();
        }
    },
    fromstoreComboBlur:function(){
        if(this.fromstoreCombo.getValue()==this.tostoreCombo.getValue()){
            this.fromstoreCombo.reset();
        }
    },
    getItemDetail:function ( qarejected){
        this.ItemDetailGrid = new Wtf.interstoretransferGrid({
            layout:"fit",
            gridTitle:WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetails"),
            border:false,
            region:"center",
            prodIds: this.prodIds,
            qarejected:qarejected,
            height:300,
            parent:this,
            disabledClass:"newtripcmbss",
            isJobWorkStockOut:this.isJobWorkStockOut
        });
    },
    printRecordTemplate:function(printflg,item){
        var params= "myflag=order&transactiono="+Wtf.OrderNoteNo+"&moduleid="+Wtf.Acc_InterStore_ModuleId+"&templateid="+item.id+"&recordids="+Wtf.recordbillid+"&filetype="+printflg;  
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "ACCExportPrintCMN/exportSingleInterStoreTransfer.do";
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
        div.innerHTML = WtfGlobal.getLocaleText("acc.field.LoadingMask"),
        myWindow.document.body.appendChild(div);
        mapForm.remove();
    },
    enableafterSaveButtons:function(enableflag){
        if(enableflag){//save
            this.singleRowPrint.enable();
        }else{
            this.savencreateBttn.enable();
            this.submitBttn.enable();
            this.cancelBttn.enable();
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
   fillAvailableQtyBeforeSaveItem:function(){
          var jsondata = "";
        //        if(Wtf.realroles[0] == 18 && (this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
        if((this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
            WtfComMsgBox(["Info", "Please Select MoD for Goods Order"], 0);
//            Wtf.getCmp('interstoreTransferSubmit').disabled=false;
            return;
        }
        var recCount = this.ItemDetailGrid.EditorStore.getCount();
        if(!this.qarejected){
            recCount -= 1;
        }
        
        if(recCount == 0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stockrequest.PleaseaddItemstotransfer")], 0);
//            Wtf.getCmp('interstoreTransferSubmit').disabled=false;
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
            var defaultlocqty=rec.get('defaultlocqty');
            var deflocation=rec.get('deflocation');
            var quantity=rec.get('quantity');
            
            var isRowForProduct=rec.get('isRowForProduct');
            var isRackForProduct=rec.get('isRackForProduct');
            var isBinForProduct=rec.get('isBinForProduct');
            var isBatchForProduct=rec.get('isBatchForProduct');
            var isSerialForProduct=rec.get('isSerialForProduct');
            var stockDetails=rec.get('stockDetails');
            var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchForProduct && !isSerialForProduct;
            var isDefaultAllocationAllowed = !(isRowForProduct || isRackForProduct || isBinForProduct || isBatchForProduct || isSerialForProduct);
            //         if(itemId=="" || itemId==undefined){
            //                WtfComMsgBox(["Warning", "Please Select Product first."],0);
            //                return false; 
            //            }
            
            if(rec.data.quantity > 0){
                if(stockDetails == "" || stockDetails == undefined){
                    if(isDefaultAllocationAllowed && (isNegativeAllowed || defaultlocqty>= quantity)){
                        if(deflocation == undefined || deflocation=="") {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("inventory.default.locationWarnMsg")],0);
                            return false;
                        } 
                        var data = {
                            locationId:deflocation,
                            quantity:quantity,
                            rowId:"",
                            rackId:"",
                            binId:"",
                            batchName:"",
                            serialNames:""
                        };
                        rec.set("stockDetailQuantity",quantity);
                        this.ItemDetailGrid.EditorStore.getAt(i).set("stockDetails",[data]);
                    }else{
                        WtfGlobal.highLightRowColor(this.ItemDetailGrid.EditorGrid,rec,true,0,2,true);
                    }
                }
            }
        }
         
        this.SaveItem();
       
//        }
//        var jsondata = "";
//        
//        //        if(Wtf.realroles[0] == 18 && (this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
//        if((this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
//            WtfComMsgBox(["Info", "Please Select MoD for Goods Order"], 0);
//            //            Wtf.getCmp('interstoreTransferSubmit').disabled=false;
//            return;
//        }
//        var recCount = this.ItemDetailGrid.EditorStore.getCount();
//        if(!this.qarejected){
//            recCount -= 1;
//        }
//        
//        if(recCount == 0){
//            WtfComMsgBox(["Info", "Please add Item/s to transfer"], 0);
//            //            Wtf.getCmp('interstoreTransferSubmit').disabled=false;
//            return;
//        }
//        
//       
//        
//               
//        Wtf.MessageBox.confirm("Confirm",WtfGlobal.getLocaleText("acc.IST.Warning"), function(btn){
//            if(btn == 'yes') {        
//                for(var i=0;i<recCount;i++){
//                    var record = this.ItemDetailGrid.EditorStore.getAt(i);
//                    this.isAllItemFilled=true;
//                    if(record.data.quantity > 0){
//                        var itemId=record.get("productid");
//                        var quantity=record.get("quantity");
//                        var transferToStockUOMFactor=Wtf.account.companyAccountPref.UomSchemaType?record.get("transferToStockUOMFactor"):record.get("confactor");
//                        var fromStoreId = this.fromstoreCombo.getValue();
//                        var toStoreId = this.tostoreCombo.getValue();
//                        var isBatchEnable = record.get("isBatchForProduct");
//                        var isSerialEnable = record.get("isSerialForProduct");
//                        if(fromStoreId == undefined || toStoreId==undefined || fromStoreId == "" || toStoreId==""){
//                            WtfComMsgBox(["Warning", "Please Select Store."],0);
//                            return false;
//                        }
//                        if(itemId=="" || itemId==undefined){
//                            WtfComMsgBox(["Warning", "Please Select Product first."],0);
//                            return false; 
//                        }
//                           
//                             
//                        if((isSerialEnable || isBatchEnable)){
//                            WtfGlobal.highLightRowColor(this.ItemDetailGrid.EditorGrid,record,true,0,2,true);
//                            continue; 
//                        }
//                                        
//                        Wtf.Ajax.requestEx(
//                        {
//                            url:  'INVStockLevel/getStoreProductWiseDetailList.do',
//                            params: {
//                                productId: itemId,
//                                defaultloc: true,
//                                storeId: fromStoreId
//                            }
//                        },
//                        this,
//                        function(action, response){
//                            if(action.success == true){
//                                var stockDetailArray = [];
//                                var availQty=action.data[0].availableQty;
//                                var defaultlocqty=action.data[0].defaultlocqty;
//                                var deflocation=action.data[0].deflocation;
//                                var maxAllowed= Math.floor(availQty/transferToStockUOMFactor);
//                                if(record.data.quantity <= defaultlocqty){
//                                    var stockDetail = {};
//                                                
//                                                    
//                                                
//                                    this.DataIndexMapping={
//                                        locationId:"locationId",
//                                        fromLocationName:"fromLocationName",
//                                        detailId:"detailId",
//                                        quantity:"quantity",
//                                        toLocationId:"toLocationId",
//                                        toLocationName:"toLocationName"
//                                    };
//            
//                                    stockDetail[this.DataIndexMapping.detailId] = "";
//                                    stockDetail[this.DataIndexMapping.locationId] = deflocation;
//                                    stockDetail[this.DataIndexMapping.fromLocationName] = action.data[0].fromLocationName;
//                                    stockDetail[this.DataIndexMapping.quantity] = record.data.quantity;
//                                    stockDetail[this.DataIndexMapping.toLocationId] = "";
//                                    stockDetail[this.DataIndexMapping.toLocationName] = "";
//                                    stockDetailArray.push(stockDetail);
//                                    record.set("stockDetails","");
//                                    record.set("stockDetails",stockDetailArray);
//                                    record.set("stockDetailQuantity",quantity);
//                                    if(i== recCount && this.isAllItemFilled){
//                                        this.SaveItem();
//                                    }
//                                }else{
//                                    this.isAllItemFilled=false;
//                                    WtfGlobal.highLightRowColor(this.ItemDetailGrid.EditorGrid,record,true,0,2,true);
//                                }
//                                                
//                            }else{
//                                this.isAllItemFilled=false;
//                                WtfComMsgBox(["Error", "Some error has occurred."],0);
//                                return false;
//                            }
//                                            
//                        },
//                        function(){
//                            this.isAllItemFilled=false;
//                            WtfComMsgBox(["Error", "Some error has occurred."],0);
//                            return false;
//                        }
//                        );
//                           
//                    }else{
//                        WtfComMsgBox(["Warning", "Please Fill quantity."],0);
//                        return false;
//                            
//                    }
//                }
//            } else if(btn == 'no') {
//                this.SaveItem();
//            }
//        },this);
//                
//                
//                
//            
//    //            else{
//    //                 WtfComMsgBox(["Warning", "Please Fill quantity."],0);
//    //                    return false;
//    //            }
//    //        }
    },
    SaveItem:function (){
//        Wtf.getCmp('interstoreTransferSubmit').disabled=true;
        var jsondata = "";
        var dataArr = new Array();
        //        if(Wtf.realroles[0] == 18 && (this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
        if((this.MOUTextField.getValue() == "" || this.MOUTextField.getValue() == null)){
            WtfComMsgBox(["Info", "Please Select MoD for Goods Order"], 0);
//            Wtf.getCmp('interstoreTransferSubmit').disabled=false;
            return;
        }
        var recCount = this.ItemDetailGrid.EditorStore.getCount();
        if(!this.qarejected){
            recCount -= 1;
        }
        
        if(recCount == 0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stockrequest.PleaseaddItemstotransfer")], 0);
//            Wtf.getCmp('interstoreTransferSubmit').disabled=false;
            return;
        }
        
        for(var i=0;i<recCount;i++){
            var rec = this.ItemDetailGrid.EditorStore.getAt(i);
            if(this.validateRecord(rec)){
                var value = [];
                var count = [];
                var finalremark;
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
                finalremark = this.NewlineRemove(rec.get("remark"));
                var jsondata={};
                jsondata.itemid=rec.get("productid");
                jsondata.quantity=rec.get("quantity");
                jsondata.uom=rec.get("uomid");
                jsondata.uomname=rec.get("uomname");
                jsondata.packaging=rec.get("packagingid");
                jsondata.confactor=rec.get("confactor");
                jsondata.fromstore=this.fromstoreCombo.getValue();
                jsondata.tostore=this.tostoreCombo.getValue();
                jsondata.costcenter=rec.get("costcenter");
                jsondata.moduletype=rec.get("moduletype");
                jsondata.qaapprovalid=rec.get("qaapprovalid");
                jsondata.qaapprovaldetailid=rec.get("qaapprovaldetailid");
                jsondata.businessdate=this.dateField.getValue().format('Y-m-d');
                jsondata.remark=this.NewlineRemove((rec.get("remark")));
                jsondata.stockDetails=rec.get("stockDetails");
                jsondata.podid=rec.get("podid");
                
                var linelevelcustomdata = Wtf.decode(WtfGlobal.getCustomColumnData(this.ItemDetailGrid.EditorStore.data.items[i].data, Wtf.Acc_InterStore_ModuleId).substring(13));
                if (linelevelcustomdata.length > 0)
                    jsondata.linelevelcustomdata = linelevelcustomdata;
               
                dataArr.push(jsondata);
                
            }else{
//                Wtf.getCmp('interstoreTransferSubmit').disabled = false;
                return;
            }
        }
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(!isValidCustomFields){
            return;
        }
        var finalStr = JSON.stringify(dataArr);
        this.sendInterTransferRequest(finalStr);
   
    },

    sendInterTransferRequest: function(finalStr){
        if(this.Form.form.isValid()){
            
            var loadingMask = new Wtf.LoadMask(document.body,{
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
            });
            var allowNegInv="";
            if(this.allowNegativeInventory != undefined || this.allowNegativeInventory != ""){
                allowNegInv=this.allowNegativeInventory;
            }
            loadingMask.show();
            
            //Disabling after saving
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            var dimencustomfield="";
            if (custFieldArr.length > 0)
            dimencustomfield = JSON.stringify(custFieldArr);
            this.cancelBttn.disable();
            this.savencreateBttn.disable();
            this.singleRowPrint.disable();
            this.submitBttn.disable();
            
            this.Form.form.submit({
                params:{
                    str:finalStr,
                    partnerid: this.MOUTextField.getValue(),
                    seqFormatId:this.sequenceFormatNO.getValue(),
                    documentNumber:this.documentNumber.getValue(),
                    memo:this.memoTextArea.getValue(),
                    allowNegativeInventory: allowNegInv,
                    isqarejected:this.qarejected,
                    customfield:dimencustomfield,
                    UomSchemaType:!Wtf.account.companyAccountPref.UomSchemaType,
                    isJobWorkStockOut:this.isJobWorkStockOut,
                    challanno:this.challanNo.getValue()
                },
                scope:this,
                success:function (result,resp){
                    loadingMask.hide();
                    var retstatus = eval('('+resp.response.responseText+')');
                    var msg = "";
                    var title="Error";
                    
                    if(retstatus.data.success){
                        title="Success";
                        msg=retstatus.data.msg;

                        WtfComMsgBox([title,msg],0);
                        
                        
                        if(retstatus.data.ISTNoteNo != "" && retstatus.data.ISTNoteNo != undefined){
                            printoutISTNote(retstatus.data.ISTNoteNo);
                        }
                        
                        Wtf.recordbillid=resp.result.data.billid;
                        Wtf.OrderNoteNo=resp.result.data.ISTNoteNo;
                        this.disableafterSaveButtons(this.saveOnlyFlag);
                        this.enableafterSaveButtons(this.saveOnlyFlag);
                        if(this.saveOnlyFlag && retstatus.data.success){
                            this.Form.disable();
                            this.ItemDetailGrid.disable();
                        }else{
                            this.Form.form.reset();
                            this.ItemDetailGrid.EditorStore.removeAll();
                            this.ItemDetailGrid.addRec();
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
                                    this.sendInterTransferRequest(finalStr);
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
                                WtfComMsgBox(["Inter Location Transfer",retstatus.data.msg],retstatus.data.success*2+2);
                                this.enableButtons();
                                this.cancelBttn.enable();

                    }else{
                        Wtf.MessageBox.show({
                            msg: retstatus.data.msg,
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK,
                            title:"Warning"
                        });
                        Wtf.getCmp('interstoreTransferSubmit').disabled=false;
                    }
                    
                   // Wtf.getCmp('interstoreTransferSubmit').disabled=false;
                },
                failure:function (){
                    loadingMask.hide();
                    Wtf.MessageBox.show({
                        msg:"Error while sending Item transfer request",
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK,
                        title:"Error"
                    });
                    this.Form.form.reset();
                    this.ItemDetailGrid.EditorStore.removeAll();
                    this.ItemDetailGrid.addRec();
                    this.enableButtons();
                    //                    this.firemyajax();
                    Wtf.getCmp('interstoreTransferSubmit').disabled=false;
                }
            });
        }else{
            Wtf.getCmp('interstoreTransferSubmit').disabled=false;
        }
    },
    NewlineRemove : function(str){
        // str = Wtf.util.Format.stripScripts(str);
        if (str)
            return str.replace(/\n/g, ' ');
        else
            return str;
    },

    validateRecord: function(rec){
        var msg=WtfGlobal.getLocaleText("acc.stockrequest.Pleaseentervaliddatafor");
        var status=true;
        if(!rec.get("pid")) {
            msg += " Item ";
            status=false;
        } else if(!rec.get("uomname")) {
            msg += " Transfer UoM ";
            status=false;
        } else if(!rec.get("quantity") || rec.get("quantity") <= 0) {
            msg += " Quantity ";
            status=false;
        } else if(!rec.get("stockDetailQuantity")|| rec.get("quantity") != rec.get("stockDetailQuantity")) {
            msg += " "+WtfGlobal.getLocaleText("acc.stockrequest.StockDetail");
            status=false;
        } 

        if(!status){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), msg], 2);
        }
        return status;
    },
    setBusinessDate:function(){
        Wtf.Ajax.requestEx({
            // url: "jspfiles/inventory/inventory.jsp",
            params: {
        //   flag: 105,
        // storeid: this.fromstoreCombo.getValue()
        }
        }, this,
        function(action){
            action = eval("("+ action + ")");
        //                this.dateField.setValue(action.data);
        },
        function(){
            });
    },
    fillRequestedItems: function(productDataArray){
        this.ItemDetailGrid.itemCodeEditorStore.on('load', function(){
            var row = 0;
            for(var i= 0; i< productDataArray.length ; i++){
                var productData = productDataArray[i];
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

Wtf.interstoretransferGrid = function (config){
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    Wtf.apply(this,config);
    Wtf.interstoretransferGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.interstoretransferGrid,Wtf.Panel,{
    initComponent:function (){
        Wtf.interstoretransferGrid.superclass.initComponent.call(this);
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
                html:"<div class='gridTitleClass' style='float:left;'>"+this.gridTitle+"</div><div style = 'float:right; font-size:9px;'> "+WtfGlobal.getLocaleText("acc.field.Note")+" : "+WtfGlobal.getLocaleText("acc.interstorestock.note")+"</div>"
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
            name:'transferinguomid'
        },
        
        {
            name:'stockuomname'
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
            "name":"transferToStockUOMFactor"
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
        }
        ]);
        this.itemEditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.itemEditorRec);

        this.itemCodeEditorStore = new Wtf.data.Store({
            baseParams: {
                isStoreLocationEnable: true,
                isInventoryForm:true
            },
            proxy: new Wtf.data.HttpProxy(new Wtf.data.Connection({
                url: "ACCProduct/getProductsForCombo.do",
                timeout: 300000
            })),
            reader: this.itemEditorReader
        });
        
        var loadingMask1 = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        this.itemCodeEditorStore.on('beforeload',function(){
            loadingMask1.show();
        },this)
        this.itemCodeEditorStore.on('load',function(){
            loadingMask1.hide();
        },this)

        this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
        if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==false){
            this.itemCodeEditorCombo=new Wtf.form.ExtFnComboBox({
                name:'itemcode',
                store:this.itemCodeEditorStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
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
            this.itemCodeEditorStore.load();
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
                    store:this.itemCodeEditorStore,    
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
                this.itemCodeEditorStore.load({
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
                        this.itemCodeEditorStore.load({
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
                     *In this case we will search pid in itemCodeEditorStore and set value accordingly. 
                     **/
                        var index = WtfGlobal.searchRecordIndex(this.itemCodeEditorStore,value,'pid');
                        if(index!=-1){
                            var prorec=this.itemCodeEditorStore.getAt(index); 
                            var dataObj = prorec.data;
                            setPIDForBarcode(this,dataObj,field,false);
                    
                        }
                    }else{
                        
                        /*
                     *This block will execute when Show product on type ahead is selected.
                     *In this case we will fetch data from backend.
                     **/
                        var params = JSON.clone(this.itemCodeEditorStore.baseParams);
                        params.query = field.getRawValue();
                        params.isForBarcode = true;
                        
                        Wtf.Ajax.requestEx({
                            url: this.itemCodeEditorStore.proxy.conn.url,
                            params:params
                        }, this, function(response) {
                            var prorec = response.data[0];
                            if(prorec){
                                var newrec = new this.itemCodeEditorStore.reader.recordType(prorec);
                                this.itemCodeEditorStore.add(newrec);
                                setPIDForBarcode(this,prorec,field,true);
                            }
                        }, function() {});
                    }
                }
            }
        },this);
        
        //        this.uomRec = new Wtf.data.Record.create([
        //        {
        //            name:"id"
        //        },
        //
        //        {
        //            name:"name"
        //        },
        //
        //        {
        //            name:"uomtype"
        //        },
        //
        //        {
        //            name:"uomval"
        //        }
        //        ]);
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
            "name":"transferToStockUOMFactor"
        },
        {
            name:"stockDetails"
        },
        {
            name:"stockDetailQuantity"
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
            name:"defaultlocqty"
        },
        {
            name:"deflocation"
        },{
            name:"podid"
        },{
            name:"itemdescription"
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
        
        WtfGlobal.updateStoreConfig(GlobalColumnModel[Wtf.Acc_InterStore_ModuleId], this.EditorStore);
        this.addRec();
        var cmWidth=175;
        var columnArr = new Array();
        this.EditorStore.on("load",this.addRec,this);
        columnArr.push(new Wtf.grid.RowNumberer(),{
                header:WtfGlobal.getLocaleText("acc.common.add"),
                align:'center',
                width:30,
                dataIndex:"plusbtn",
                renderer: this.addProductList.createDelegate(this)
            },
            {
                header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
                //sortable:true,
                dataIndex:"pid",
                width:cmWidth,
                editor:this.itemCodeEditorCombo
//                renderer:this.getComboRenderer(this.itemCodeEditorCombo)
            },
            {
                header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
                //sortable:true,
                dataIndex:"itemdescription",
                width:cmWidth
            //editor:this.itemEditorCombo
            },
            {
              header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),
              dataIndex:"desc",
              width:cmWidth
            },
            {
                header:WtfGlobal.getLocaleText("acc.je.CoilcraftPartNo"),
                //sortable:true,
                dataIndex:"partnumber",
                hidden: true//integrationFeatureFor == Wtf.IF.COILCRAFT ? false: true
            },{//to add the new column packaging 
                header:WtfGlobal.getLocaleText("acc.product.packaging"),
                dataIndex:"packaging",
                width:cmWidth,
                renderer:this.getComboRenderer(this.packagingCombo),
                editor:Wtf.account.companyAccountPref.UomSchemaType==false?this.packagingCombo:"",
                hidden:!Wtf.account.companyAccountPref.UomSchemaType?true:false
            // sortable:true
            },{
                header:WtfGlobal.getLocaleText("acc.product.transferUoM"),
                dataIndex:"uomname",
                renderer:this.getComboRenderer(this.uomCombo),
                editor:this.uomCombo,
                width:cmWidth
            // sortable:true
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridRateToBase"),
                dataIndex:"confactor",
                hidden:Wtf.account.companyAccountPref.UomSchemaType?true:false,
                width:150,
                renderer:this.conversionFactorRenderer(this.itemCodeEditorStore,"productid","uomname",this.EditorStore),
                editor:(Wtf.account.companyAccountPref.UomSchemaType===0) ?this.transBaseuomrate : ""     //Does allow to user to change conversion factor
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.CostCenter"),
                dataIndex:"costcenter",
                width:cmWidth,
                renderer:this.getComboRenderer(this.costCenterCombo),
                editor:this.costCenterCombo
            },{
                header:WtfGlobal.getLocaleText("acc.stockrequest.Quantity(inTransferUoM)"),
                dataIndex:"quantity",
                width:cmWidth,
                //sortable:true,
                editor:new Wtf.form.NumberField({
                    scope: this,
                    allowBlank:false,
                    allowNegative:false,
                    decimalPrecision:4,
                    value:0
                //                    listeners : {
                //                        'focus': setZeroToBlank
                //                    }
               
                }),
                renderer: function(val){
                    return val//WtfGlobal.getCurrencyFormatWithoutSymbol(val, Wtf.companyPref.quantityDecimalPrecision);
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
                hidden:this.qarejected,//(!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory),
                width:40
            },
            {
                header:WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
                dataIndex:"remark",
                width:cmWidth,
                //sortable:true,
                editor:new Wtf.form.TextArea({
                    maxLength:200,
                    regex:Wtf.validateAddress
                })
            });
        columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModel[Wtf.Acc_InterStore_ModuleId]);
        columnArr.push({
            header: WtfGlobal.getLocaleText("mrp.rejecteditems.report.actioncolumn.title"),
            align: 'center',
            dataIndex: "lock",
            width: 50,
            renderer: function (v, m, rec) {
                return "<span class='pwnd delete-gridrow'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>";
                }
            });
            
        this.EditorColumn = new Wtf.grid.ColumnModel(columnArr);
        this.addBut = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.add")
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

        this.EditorGrid.on("render", function(grid) {
            this.EditorGrid.getView().applyEmptyText();
             new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.EditorGrid.on('statesave', this.saveGridStateHandler, this);
                }, this); 
        },this);
        this.getGridConfig();
        var colModelArray;
        colModelArray = GlobalColumnModel[Wtf.Acc_InterStore_ModuleId];
        WtfGlobal.updateStoreConfig(colModelArray, this.EditorStore);
        
        this.EditorGrid.on('rowclick',this.handleRowClick,this);
        this.EditorGrid.on("afteredit",this.fillGridValue,this);
        this.EditorGrid.on("cellclick",this.cellClick,this);
        this.EditorGrid.on("beforeedit",this.loadUom,this);
        this.EditorGrid.on('populateDimensionValue',this.populateDimensionValueingrid,this);
    },
    addProductList:function(){
        return "<div class='pwnd add-gridrow' wtf:qtip=\"Click to add products\"></div>";
    },
    saveGridStateHandler: function(grid, state){//To save config details when we hide or show columns
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.InterStore_Form_Grid_Id, grid.gridConfigId, true);
    },
    getGridConfig: function(){//To load config details
        WtfGlobal.getGridConfig(this.EditorGrid, Wtf.InterStore_Form_Grid_Id , true, false);
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
            moduleid:Wtf.Acc_InterStore_ModuleId,
            modulename:"INTER_STORE_TRANSFER",
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
    fillSelectedRecValue: function(row, productData, isLastRec){
        var added = false;
        
        for(var i=0;i<this.itemCodeEditorStore.getCount();i++){
            if(this.itemCodeEditorStore.getAt(i).get("productid") == productData.productid){
                var rec = this.EditorStore.getAt(row);
                rec.set("productid", productData.productid);
                rec.set("quantity",(productData.quantity/this.itemCodeEditorStore.getAt(i).get("transferToStockUOMFactor")));
                rec.set("pid",this.itemCodeEditorStore.getAt(i).get("pid")); 
                rec.set("itemdescription",this.itemCodeEditorStore.getAt(i).get("productname"));
                rec.set("desc",this.itemCodeEditorStore.getAt(i).get("productname"));
                rec.set("packaging",this.itemCodeEditorStore.getAt(i).get("packaging"));
                rec.set("uomid",this.itemCodeEditorStore.getAt(i).get("uomid"));
                rec.set("uomname",this.itemCodeEditorStore.getAt(i).get("transferinguomname"));
                rec.set("partnumber",this.itemCodeEditorStore.getAt(i).get("partnumber"));
                rec.set("costcenter",this.itemCodeEditorStore.getAt(i).get("costcenter")); 
                rec.set("warehouse",this.itemCodeEditorStore.getAt(i).get("warehouse")); 
                rec.set("stockDetails",productData.productDetailDataArr);
                
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
        }else if(!isLastRec && added && row == this.EditorStore.getCount()-1){
            this.addRec();
        }
        return added;
    
    },
    isvalidedit:function(e){
        var rec = this.EditorStore.getAt(e.row);
        if(rec.get("transferinguomname") == "" && e.field != "pid"){
            return false;
        }
    },
    fillGridValue:function (e){
        for(var i=0;i<this.itemCodeEditorStore.getCount();i++){
            var itemRec = this.itemCodeEditorStore.getAt(i);
            if(itemRec.get("productid") == e.value){
                var rec = this.EditorStore.getAt(e.row);
                rec.set("productid",e.value);
                rec.set("pid",this.itemCodeEditorStore.getAt(i).get("pid"));
                rec.set("uomname",this.itemCodeEditorStore.getAt(i).get("transferinguomname"));
                rec.set("orderinguomname",this.itemCodeEditorStore.getAt(i).get("orderinguomname"));
                rec.set("transferinguomname",this.itemCodeEditorStore.getAt(i).get("transferinguomname"));
                rec.set("transferToStockUOMFactor",this.itemCodeEditorStore.getAt(i).get("transferToStockUOMFactor"));
                rec.set("stockuomname",this.itemCodeEditorStore.getAt(i).get("uomname"));
                rec.set("packaging",this.itemCodeEditorStore.getAt(i).get("packaging"));
                rec.set("packagingid",this.itemCodeEditorStore.getAt(i).get("packagingid"));
                rec.set("uomid",this.itemCodeEditorStore.getAt(i).get("uomid"));
                rec.set("primaryuomvalue",this.itemCodeEditorStore.getAt(i).get("primaryuomvalue"));
                rec.set("inneruomvalue",this.itemCodeEditorStore.getAt(i).get("inneruomvalue"));
                rec.set("casinguomvalue",this.itemCodeEditorStore.getAt(i).get("casinguomvalue"));
                rec.set("costcenter",this.itemCodeEditorStore.getAt(i).get("costcenter")); 
                rec.set("desc",this.itemCodeEditorStore.getAt(i).get("desc"));
                rec.set("itemdescription",this.itemCodeEditorStore.getAt(i).get("productname"));
                rec.set("data",this.itemCodeEditorStore.getAt(i).get("data"));
                rec.set("partnumber",this.itemCodeEditorStore.getAt(i).get("partnumber"));
                rec.set("isBatchForProduct",this.itemCodeEditorStore.getAt(i).get("isBatchForProduct"));
                rec.set("isSerialForProduct",this.itemCodeEditorStore.getAt(i).get("isSerialForProduct"));
                rec.set("isRowForProduct",this.itemCodeEditorStore.getAt(i).get("isRowForProduct"));
                rec.set("isRackForProduct",this.itemCodeEditorStore.getAt(i).get("isRackForProduct"));
                rec.set("isBinForProduct",this.itemCodeEditorStore.getAt(i).get("isBinForProduct"));
                rec.set("stockDetails",this.itemCodeEditorStore.getAt(i).get("stockDetails"));
                rec.set("uomschematype",this.itemCodeEditorStore.getAt(i).get("uomschematype"));
                rec.set("ismultipleuom",this.itemCodeEditorStore.getAt(i).get("ismultipleuom"));
                
                if(rec.get("transferinguomname") == "" ){
                    this.EditorGrid.getView().getRow(e.row).style = "background-color :pink";
                    this.addRec();
                    return false;
                }
                if(!(this.itemCodeEditorStore.getAt(i).get("isBatchForProduct")&&this.itemCodeEditorStore.getAt(i).get("isSerialForProduct"))){
                    this.getDefaultLocationQty(e);
                }
                if(!Wtf.account.companyAccountPref.UomSchemaType){
                    this.getProductBaseUOMRate(e.value,this.itemCodeEditorStore.getAt(i).get("uomid"),e.row);
                }else{
                    this.loadPackagingStore(this.itemCodeEditorStore.getAt(i).get("productid"));
                }
            }
        }
        var alreadyAdded = false;
        for(i=0;i<this.EditorStore.getCount();i++){
            var rec = this.EditorStore.getAt(e.row);
            if (e.field=="confactor" && rec.get("transferinguomname")==rec.get("stockuomname") && rec.get("confactor")!=1) {
                WtfComMsgBox(["Warning", "Conversion factor should be 1 as stock uom and transfer uom is same."], 0);
                rec.set("confactor", "");
                return false;
            }
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
    
    getDefaultLocationQty : function(e){
         
            Wtf.Ajax.requestEx({
            url:"INVStockLevel/getStoreProductWiseDetailList.do",
            params: {
                storeId: this.parent.fromstoreCombo.getValue(),
                productId: e.value,
                defaultloc:true
            }
        },this,
        function(res,action){
            if(res.success==true){
                var defaultlocqty=res.data[0].defaultlocqty;
                var deflocation=res.data[0].deflocation;
                this.EditorStore.getAt(e.row).set("defaultlocqty",defaultlocqty);
                this.EditorStore.getAt(e.row).set("deflocation",deflocation);
            }else{
                WtfComMsgBox(["Error", "Error occurred while fetching data."],0);
                return;
            }
                
        },
        function() {
            WtfComMsgBox(["Error", "Error occurred while processing"],1);
        }
        ); 
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
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(Wtf.Acc_InterStore_ModuleId, rec, this.EditorGrid);
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
        if (this.isJobWorkStockOut && this.isJobWorkStockOut == true && (fieldName == "plusbtn" || fieldName == "pid")) {
            /**
             * Restrict user to add product in case of Job work Out 
             */
            return false;
        }
        if(fieldName=="uomname"&& !record.get("ismultipleuom")&&!Wtf.account.companyAccountPref.UomSchemaType){
            return false;
        }
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchEnable && !isSerialEnable;
        var itemId=record.get("productid");
        var itemCode=record.get("pid");
        var quantity=record.get("quantity");
        var transferToStockUOMFactor=Wtf.account.companyAccountPref.UomSchemaType?record.get("transferToStockUOMFactor"):record.get("confactor");
        var transferUOMName=record.get("transferinguomname");
        var stockUOMName=record.get("stockuomname");
        var fromStoreId = this.parent.fromstoreCombo.getValue();
        var toStoreId = this.parent.tostoreCombo.getValue();
        
        if(fieldName == undefined){
            if(fromStoreId == undefined || toStoreId==undefined || fromStoreId == "" || toStoreId==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockrequest.PleaseSelectStore")],0);
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
                         
                        if(!isNegativeAllowed && availQty <= 0){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockrequest.Quantityisnotavailableforselecteditem")],0);
                            return false;
                        }
                        
                        if(isNegativeAllowed || availQty >= (quantity * transferToStockUOMFactor )){
                            //                            this.showLocationBatchSerialSelectWindow(itemId,itemCode,isBatchEnable,isSerialEnable,quantity,transferToStockUOMFactor,transferUOMName,stockUOMName,rowIndex);
                            this.showStockDetailWindow(record)
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
          
        } else if (e.getTarget(".add-gridrow")) {
            this.showProductGrid();
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
        var transferToStockUOMFactor=Wtf.account.companyAccountPref.UomSchemaType?record.get("transferToStockUOMFactor"):record.get("confactor");
        var transferUOMName=record.get("transferinguomname");
        var stockUOMName=record.get("stockuomname");
        var fromStoreId = this.parent.fromstoreCombo.getValue();
        var fromStoreName=this.parent.fromstoreCombo.getRawValue();
        var toStoreId = this.parent.tostoreCombo.getValue();
        var toStoreName=this.parent.tostoreCombo.getRawValue();
        var maxQtyAllowed=parseFloat(getRoundofValue(transferToStockUOMFactor * quantity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        
        var winTitle = WtfGlobal.getLocaleText("acc.stockrequest.StockDetailforStockTransfer");
        var winDetail = String.format( WtfGlobal.getLocaleText("acc.stockrequest.SelectStockdetailsforstocktransfer")+'<br> <b>'+WtfGlobal.getLocaleText("acc.product.gridProduct")+" :"+'</b> {0}<br> <b>'+WtfGlobal.getLocaleText("acc.stockrequest.IssuanceStore")+":"+'</b> {1}<br> <b>'+ WtfGlobal.getLocaleText("acc.stockrequest.CollectionStore")+":"+'</b> {2}<br> <b>'+WtfGlobal.getLocaleText("acc.fixed.asset.quantity")+" :"+'</b> {3} {4} ( {5} {6} )', itemCode, fromStoreName, toStoreName, quantity, transferUOMName, maxQtyAllowed, stockUOMName);
        this.detailWin = new Wtf.StockTransferDetailWin({
            WinTitle : winTitle,
            WinDetail: winDetail,
            TotalTransferQuantity: maxQtyAllowed,
            ProductId:itemId,
            FromStoreId: fromStoreId,
            isBatchForProduct: isBatchEnable,
            isSerialForProduct : isSerialEnable,
            isRowForProduct: isRowEnable,
            isRackForProduct: isRackEnable,
            isBinForProduct: isBinEnable,
            GridStoreURL:"INVStockLevel/getStoreProductWiseDetailList.do",
            StockDetailArray:record.get("stockDetails"),
            isNegativeAllowed: isNegativeAllowed,
            moduleid:Wtf.Acc_InterStore_ModuleId,
            modulename:"INTER_STORE_TRANSFER",
            DataIndexMapping:{
                fromLocationId:"locationId",
                fromRowId:"rowId",
                fromRackId:"rackId",
                fromBinId:"binId",
                serials:"serialNames"
            },
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
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
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                handler:function (){
                    this.detailWin.close();
                },
                scope:this
            }]
        })
        this.detailWin.show();
    },
    showLocationBatchSerialSelectWindow : function (itemId,itemCode,isBatchForProduct,isSerialForProduct,quantity,transferToStockUOMFactor,transferUOMName,stockUOMName,rowIndex){
        
        this.fromStoreId = this.parent.fromstoreCombo.getValue();
        this.toStoreId = this.parent.tostoreCombo.getValue();
        this.fromStoreName=this.parent.fromstoreCombo.getRawValue();
        this.toStoreName=this.parent.tostoreCombo.getRawValue();
        this.itemId=itemId;
        this.itemcode=itemCode;
        this.isBatchForProduct=isBatchForProduct;
        this.isSerialForProduct=isSerialForProduct;
        this.quantity=quantity;
        
        this.transferToStockUOMFactor=transferToStockUOMFactor;
        this.transferUOMName=transferUOMName;
        this.stockUOMName=stockUOMName;
        this.currentRowNo=rowIndex;
        this.maxQtyAllowed=transferToStockUOMFactor * quantity;
        
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
            },
            {
                header:"From Location",
                dataIndex:"fromlocationname",
                hidden:false
            },
            {
                header:"Batch",
                dataIndex:"batch",
                // editor:this.batchCmb,
                id:"batchColumn",
                hidden : (this.isBatchForProduct==true) ? false :true
            //renderer:this.getComboRenderer(this.batchCmb)
            },
            {
                header:"Available Quantity",
                dataIndex:"availableQty"
            },
            {
                header:"Quantity",
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
                header:"Serial",
                dataIndex:"serial",
                editor:this.serialCmb,
                hidden : (this.isSerialForProduct==true) ? false :true,
                renderer:this.getComboRenderer(this.serialCmb)
            }
            
            ]);
        
            
        this.locationGridStore = new Wtf.data.SimpleStore({
            fields:['itemid','itemcode','fromlocationid','fromlocationname','isBatchForProduct',
            'isSerialForProduct','batch','serial','selectedSerials','quantity','availableQty'],
            pruneModifiedRecords:true
        });
          
        this.locationGridStore.on('load', function(){
            this.locationSelectionGrid.getView().refresh();
        },this);
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        var callURL = "";
        var caseType="";
        
        if(this.isBatchForProduct == true && this.isSerialForProduct==true){
            callURL="INVStockLevel/getStoreProductWiseLocationBatchList.do";
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
 
                //     'itemid','itemcode','fromlocationid','fromlocationname','isBatchForProduct',
                //     'isSerialForProduct','batch','serial','selectedSerials','quantity','availableQty'
                        
                //dummy casetype 4 data : 
                // {"locationId":"ff80808149c1a2660149c2aa85e20006","locationName":"qwe","availableQty":100,"productId":"PR/ID000012"}

                //dummy casetype 1 data :
                // {"batchName":"mmob","locationId":"ff8080814a2841a9014a28ba32de000c","batchId":"afee1e00-62f5-44a8-950c-977a469ff77a",
                // "locationName":"123","availableQty":2,"productId":"402880094a7066db014a71c4f4cc0005"}                   
               
                if(caseType==1){
                    this.locationGridStore.removeAll();
                    for(var i=0;i<totalRec;i++){
                            
                        this.locationGridStoreArr.push([this.itemId,this.itemcode,res.data[i].locationId,
                            res.data[i].locationName,this.isBatchForProduct,this.isSerialForProduct,
                            res.data[i].batchName,"","",0,res.data[i].availableQty]);
                        
                    }
                    this.locationGridStore.loadData(this.locationGridStoreArr);
                }
                if(caseType==4){
                    this.locationGridStore.removeAll();
                    for(var i=0;i<totalRec;i++){
                            
                        this.locationGridStoreArr.push([this.itemId,this.itemcode,res.data[i].locationId,
                            res.data[i].locationName,this.isBatchForProduct,this.isSerialForProduct,
                            "","","",0,res.data[i].availableQty]);
                            
                           
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
        +"<b>Quantity : </b>"+ this.quantity +" "+this.transferUOMName+" ( "+ this.maxQtyAllowed+" "+ this.stockUOMName +" )<br/>" ;
        
        
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
                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 20px 20px 20px;',
                layout : 'fit',
                items : [{
                    border : false,
                    bodyStyle : 'background:transparent;',
                    layout : "border",
                    items : [
                    {
                        region : 'north',
                        border : false,
                        height : 35,
                        items  : this.toStoreCmb
                    },
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
                        //rec.set("quantity",this.quantity);
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
                    storeid: this.fromStoreId ,
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
            var enteredTotalQty=0;
            
            for(var i=0; i < totalRec;i++){
                var currentRec=this.locationGridStore.getAt(i);
                enteredTotalQty += currentRec.get("quantity");
            }
            if(enteredTotalQty > this.maxQtyAllowed){
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
    validateFilledData : function(){
        
        var recs=this.locationSelectionGrid.getStore().getModifiedRecords();
        var isSerialForProduct=false;
        var quantity=0;
        var serial="";
        var totalQty=0;
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                isSerialForProduct=this.isSerialForProduct; 
                quantity=recs[k].get("quantity");
                serial=recs[k].get("serial");
                totalQty += quantity;  
                
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
            if(totalQty != this.maxQtyAllowed){
                WtfComMsgBox(["Warning", "Please fill total <b>"+this.maxQtyAllowed+" </b> Quantity."],0);
                return false;
            }
            this.quantity=totalQty;
            return true;
        }else{
            WtfComMsgBox(["Warning", "Please enter quantity."],0);
            return false;
        }
        
    },
    makeJSONData : function(){
        var recs=this.locationSelectionGrid.getStore().getModifiedRecords();
        var jsondata = "";
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                var batch=recs[k].get("batch");
                var locationid=recs[k].get("fromlocationid");  
                var quantity=recs[k].get("quantity");
                var serial=recs[k].get("serial");
                
                jsondata += "{'locationId':'" + locationid  + "',";
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
        if(recs != undefined){
           
            var batch=recs.get("batch");
            var locationid=recs.get("fromlocationid");  
            var quantity=recs.get("quantity");
            var serial=recs.get("serial");
                
            var jsondata = {};
            jsondata.locationId=locationid;
            jsondata.batchName=batch;
            jsondata.serialNames=serial;
            jsondata.quantity=quantity;
            detailArray.push(jsondata)
           
        }
       
        return detailArray;
    }
});

function printoutISTNote(ISTNoteNo){

    var URL="INVGoodsTransfer/getInterStockTransferBySequenceNo.do"; 

    var printTitle = "Store Transfer Note";
    var pcase = "Transfer";
    var printflg = "interstore";
   
    
    Wtf.Ajax.requestEx({
        url:URL,
        params: {
            sequenceNo : ISTNoteNo
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
    
            +"<table border='0' width='95%'>"
            +"<tr><td style='width:53%'><b>&nbsp;</b>"+ "</td><td><b>Transfer Note No : </b>"+result.data[0].transfernoteno +"</td></tr>"
            +"<tr><td style='width:53%'><b>&nbsp;</b>"+"</td><td><b>Created By :</b> "+_fullName+"</td></tr>"
            +"<tr><td style='width:53%'><b>&nbsp;</b>"+ "</td><td><b>Date :</b> "+ result.data[0].date+"</td></tr>"
            +"</table>"
            +"<div style='margin-top:95px;width: 95%;'>";
           
            htmlString += "<span style='margin-left:1%; border-left:solid 1px black;border-bottom:solid 1px black;border-top:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: right;text-align: left;'><b>To : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" +result.data[0].tostorename + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" +result.data[0].tostoreadd
            + "</br></span>"
            + "<span style='border-bottom:solid 1px black;border-top:solid 1px black;border-left:solid 1px black; border-right:solid 1px black; width:45%; padding: 1%; float: left;text-align: left;'><b>From : </b></br>&nbsp;&nbsp;&nbsp;&nbsp;" + result.data[0].fromstorename + "</br>&nbsp;&nbsp;&nbsp;&nbsp;" + result.data[0].fromstoreadd
            + "</br></span>";

            htmlString += "</div><br/><br style='clear:both'/><br/>";
            var pgbrkstr1 = "<DIV style='page-break-after:always'></DIV>";
            
            if (i != 0) {
                htmlString += "<br/><br/></br>";
            }
            htmlString += "<center>";
            htmlString += "<table cellspacing=0 border=1 cellpadding=2 width='95%'>";
            if (printflg === "interstore") {
                htmlString += "<tr>"
                +"<th>S/N</th>"
                +"<th>Product ID</th>"
                +"<th>Product Name</th>"
                +"<th>Product Description</th>"
                //                    +(printflg === "printissueNote" || printflg === "interstore"?"<th> HS Code</th>":"")
                +"<th>UoM</th>"
                +"<th>Quantity</th>";
                // htmlString += (isLocationDataPresent == true ? "<th>Location</th>" : "" );
                htmlString += "<th>From Location</th>";
                //htmlString += (printflg === "interstore" ? "<th>To Location</th>" : "");
                htmlString += (isBatchDataPresent == true ? "<th>Batch No.</th>"  : "" );
                htmlString += (isSerialDataPresent == true ? "<th>Issued Serial No.</th>" : "" );
                //htmlString += (isSerialDataPresent == true &&  printflg === "interstore" ? "<th>Collected Serial No.</th>" : "" );
                htmlString += "<th>Remark</th>";
                
                var linedata = [];
                linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModel[Wtf.Acc_InterStore_ModuleId]);
                for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
                    if (linedata[lineFieldCount].header != undefined) {
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
                    +"<td align='center'>" +result.data[i].itemdescription + "&nbsp;</td>"
                    //                        +(printflg === "printissueNote" || printflg === "interstore"?"<td align='center'>" + (result.data[i].hscode == undefined ? "": result.data[i].hscode)+ "&nbsp;</td>":"")
                    +"<td align='center'>" +result.data[i].name + "&nbsp;</td>"
                    +"<td align=right>" + result.data[i].quantity + "&nbsp;</td>"
                    +"<td align='center'>" + fromLocationDtl + "</td>"+
                    // (printflg === "interstore" ? "<td align='center'>" + toLocationDtl + "</td>" : "" )+
                    (isBatchDataPresent == true ? "<td align='center'>" + batchDtl+ "</td>"  : "" )+
                    (isSerialDataPresent == true ? "<td align='center'>" + issuedSerialDtl + "</td>" : "" ) +
                    //(isSerialDataPresent == true &&  printflg === "interstore" ? "<td align='center'>" + collectedSerialDtl + "</td>" : "" ) +
            
                    "<td align='center'>" + result.data[i].remark + "&nbsp;</td>";

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
            //            if (i != cnt - 1) {
            //                htmlString += pgbrkstr1;
            //            }

            htmlString += 
            //        "<div>"
            //    + "<span style='width:270px; padding: 3%; float: left;text-align:left'><b>Prepared By </b><br></br>Sign:</br>Name:&nbsp;&nbsp;&nbsp;&nbsp;" +createdBy + "</br></br></span>"
            //    + "<span style='width:270px; padding: 3%; float: right;text-align:left'><b>Collected By </b></br></br>Sign:</br>Name:&nbsp;&nbsp;&nbsp;&nbsp;" + collectedBy + "</br></br></span>"
            //    + "</div><br style='clear:both'/>"
            "<div style='float: right; padding-top: 3px; padding-right: 5px;'>"
            + "<button id = 'print' title='Print Note' onclick='window.print();' style='color: rgb(8, 55, 114);visibility:hidden;display:none' href='#'>Print</button>"
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
