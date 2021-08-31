/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function importProductStock(){
    var extraParams = "";
    var extraConfig = {};
    //    extraConfig.url= "ACCProduct/importProductStockIn.do";
    extraConfig.url= "INVImport/importProductStockIn.do";
    Wtf.commonFileImportWindow(this, "Stock Movement List","",extraParams,extraConfig).show();
//    var importProductbtnArray = Wtf.importMenuArray(this, this.moduleName, this.productStore, extraParams, extraConfig);
}

Wtf.markout = function (config){
    this.viewRec=config.rec;
    this.isView=config.isView;
    this.tabtype=config.tabtype;
    Wtf.apply(this,config);
    Wtf.markout.superclass.constructor.call(this);
}

Wtf.extend(Wtf.markout,Wtf.Panel,{
    initComponent:function (){
        Wtf.markout.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.markout.superclass.onRender.call(this, config);
        this.draftId = "";
        this.getForm();
        this.getItemDetail();
        this.getsouthPanel();
        this.isActiveSAApprovalFlow();
        this.checkViewDraft=0;
        this.saveBut= new Wtf.Button({
            text:"Save in draft",
            tooltip: {
                text:"Save form in draft"
            },
            scope: this,
            handler:function(){
                var cnt= this.ItemDetailGrid.EditorStore.getModifiedRecords().length;
                if(cnt>0){
                    Wtf.MessageBox.confirm("Confirm","Are you sure you want to save as draft?", function(btn){
                        if(btn == 'yes') {
                            this.createSaveInDraftWin();
                        }else
                            return;
                
                    },this);
                }else
                    //  msgBoxShow(["Info", "Please fill the details"], 0);
                    return;
            }
        });
        
        /*Save Button*/
        this.onlySavingBttn=new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.je.ClicktoSave")
            },
            id: "save" +  this.id, 
            scope: this,
            iconCls: 'pwnd save',
            disabled:this.isView?true:false,
            handler: function(){
                var cnt= this.ItemDetailGrid.EditorStore.getModifiedRecords().length;
                var isInData=false;
                this.saveOnlyFlag = true;
                var approvalSerialData = [];
                if(cnt>0){
                    var editedRecords = this.ItemDetailGrid.EditorStore.getModifiedRecords(); 
                    for(var x=0;x<cnt;x++){  
                        var productid = editedRecords[x].get('productid');
                        var pid = editedRecords[x].get('pid');
                        var reason=editedRecords[x].get('reason').trim();
                        var costcenter=editedRecords[x].get('costcenter').trim(); 
                        var remark=editedRecords[x].get('remark').trim(); 
                        var quantity=editedRecords[x].get('quantity');
                        var defaultlocqty=editedRecords[x].get('defaultlocqty');
                        var deflocation=editedRecords[x].get('deflocation');
                        var adjustmentType=editedRecords[x].get('adjustmentType');
                        var stockDetails=editedRecords[x].get('stockDetails');
                        var isBatchForProduct=editedRecords[x].get('isBatchForProduct'); 
                        var isSerialForProduct=editedRecords[x].get('isSerialForProduct');
                        var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchForProduct && !isSerialForProduct;
                        var isProductIdEmpty= (productid == undefined || productid == "");
                        if(!isProductIdEmpty && quantity==0){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"), WtfGlobal.getLocaleText("acc.stockadjustment.Quantitycannotbe0")],3);
                            return;
                        }
                        if (!this.isJobWorkInReciever) {
                            if(!isProductIdEmpty &&  this.adjReasonCombo.getValue()==""||this.adjReasonCombo.getValue()==undefined){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"), WtfGlobal.getLocaleText("acc.stockadjustment.AdjustmentReasoncannotempty")],3);
                                return;
                            }
                        }
                        if (!isProductIdEmpty && (adjustmentType == undefined || adjustmentType == "")) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.JE.PleaseSelectAdjustmentType")], 0);
                            return false;
                        }
                        if(!isProductIdEmpty &&  this.memoTextArea.getValue().trim()==""||this.memoTextArea.getValue()==undefined){
                            WtfComMsgBox(["Alert", "Memo cannot empty."],3);
                            return;
                        }
                        if (!isProductIdEmpty &&  (stockDetails == "" || stockDetails == undefined) && (isBatchForProduct || isSerialForProduct)) {
                            WtfComMsgBox(["Alert", "Please select stock location detail for item."], 3);
                            return;
                        } else if (!isProductIdEmpty && (stockDetails == "" || stockDetails == undefined) && (!isBatchForProduct && !isSerialForProduct)) {
{
                            var detailArray = new Array();
                            var data = {};
                            if (deflocation != "" && deflocation != undefined) {
                                data.locationId = deflocation;
                                data.quantity = quantity;
                                data.rowId = "";
                                data.rackId = "";
                                data.binId = "";
                                data.batchName = "";
                                data.serialNames = "";
                                detailArray.push(data);

                                editedRecords[x].set("stockDetails", detailArray);
                                editedRecords[x].set("stockDetailQuantity",quantity);
                                stockDetails=detailArray;
                            } else {
                                WtfComMsgBox(["Alert", "Please select stock location detail for item."], 3);
                                return;
                            }
                        }
                        }
                        if (!isProductIdEmpty && adjustmentType != "Stock IN") {
                            if (x > 0) {
                                 var prodQtyBlocked = 0;
                                for (var prevRec = 0; prevRec < x; prevRec++) {
                                    var prevRow = editedRecords[prevRec];
                                    if (productid == prevRow.get('productid') && (prevRow.get('adjustmentType') == "Stock Out" || prevRow.get('adjustmentType') == "Stock Sales") && !isNegativeAllowed) {
                                        prodQtyBlocked += prevRow.get('stockDetailQuantity');                                    
                                    if (stockDetails != "" && stockDetails != undefined) {
                                        for (var stk = 0; stk < stockDetails.length; stk++) {
                                            var selectedStockLocationId = stockDetails[stk].locationId;
                                            var selectedStockQty = stockDetails[stk].quantity;
                                            if (deflocation == selectedStockLocationId && selectedStockQty + prodQtyBlocked > defaultlocqty) {
                                                WtfComMsgBox(["Alert", "Stock is not sufficient in the System for this Product to stock-out : " + pid + " at row No. <b>" + (x + 1) + " </b>"], 3);
                                                return;
                                            }
                                        }
                                    }
                                  }  
                                }
                            }
                            if (!isNegativeAllowed && quantity > 0 && !isBatchForProduct && !isSerialForProduct) {
                                if (stockDetails != "" && stockDetails != undefined) {
                                    for (var stk = 0; stk < stockDetails.length; stk++) {
                                        var selectedStockLocationId = stockDetails[stk].locationId;
                                        var selectedStockQty=stockDetails[stk].quantity;
                                        if (deflocation == selectedStockLocationId && selectedStockQty > defaultlocqty) {
                                            WtfComMsgBox(["Alert", "Stock is not sufficient in the System for this Product to stock-out : " + pid + " at row No. <b>" + (x + 1) + " </b>"], 3);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        var stockDetailQuantity=editedRecords[x].get('stockDetailQuantity');
                        if(!(stockDetailQuantity > 0 && stockDetailQuantity == quantity)){
                            if(((!isBatchForProduct&&!isSerialForProduct)&&quantity>defaultlocqty)||((isBatchForProduct||isSerialForProduct))){
                                WtfComMsgBox(["Alert", "Adjusted quantity and detail quantity is not matched for item "+pid],3);
                                return;
                            }else{
                                WtfComMsgBox(["Alert", "Adjusted quantity and detail quantity is not matched for item "+pid],3);
                                return;
                            }
                        }
                        
                        if(productid=="" && quantity > 0){ //reason==""
                            WtfComMsgBox(["Alert", "Please fill valid details."],3);
                            return;
                        }
                        if(!isProductIdEmpty &&  adjustmentType=="Stock IN"){
                            isInData=true;
                            if(editedRecords[x].get('isSerialForProduct')){
                                var productcode = editedRecords[x].get('pid');
                                for(var i= 0 ; i < stockDetails.length ; i++){
                                    var batchName = stockDetails[i].batchName;
                                    var serialnames = stockDetails[i].serialNames;
                                    var serialArr = serialnames.split(",");
                                    for(var s =0 ; s< serialArr.length ; s++){
                                        var approvalSerial = [];
                                        approvalSerial.push(x);
                                        approvalSerial.push(productcode);
                                        approvalSerial.push(batchName);
                                        approvalSerial.push(serialArr[s]);
                                        approvalSerialData.push(approvalSerial)
                                    }
                                    
                                }
                            }
                        }
                        
                    }
                }else{
                    //msgBoxShow(["Info", "Please fill the details"], 0);
                    return;
                }
                var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
                if(!isValidCustomFields){
                    return;
                }
                if(!this.Form.form.isValid()){
                    return;
                }
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.stockadjustment.Areyousureyouwanttosubmitrecords?"), function(btn){
                    if(btn == 'yes') {
                        if(Wtf.account.companyAccountPref.activateQAApprovalFlow && this.isActiveSAApproval && isInData){
                            Wtf.Msg.show({
                                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                                msg: WtfGlobal.getLocaleText("acc.stockadjustment.alert1"),
                                buttons: Wtf.Msg.YESNOCANCEL,
                                scope: this,
                                fn: function (btn) {
                                    if (btn == 'yes') {
                                        if (approvalSerialData.length > 0) {
                                            this.openSerialSelectionWindow(approvalSerialData)
                                        } else {
                                            this.Save(false, true);
                                        }
                                    } else if (btn == 'no') {
                                        this.Save(false, false);
                                    } else {
                                        return;
                                    }
                                },
                                icon: Wtf.MessageBox.QUESTION
                            });
//                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.stockadjustment.alert1"), function(btn){
//                                if(btn == 'yes') {
//                                    if(approvalSerialData.length > 0){
//                                        this.openSerialSelectionWindow(approvalSerialData)
//                                    }else{
//                                        this.Save(false,true);
//                                    }
//                                }else
//                                    this.Save(false,false);
//                            },this);
                        }else{
                            this.Save(false,false);
                        }
                    }else
                        return;
                },this);
            },
            scope:this
        });
        
        this.submitBut= new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),//Save and Create New
            tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            iconCls: 'pwnd save',
            disabled:this.isView?true:false,
            //iconCls:getButtonIconCls(Wtf.etype.add),
            handler:function (){
                var cnt= this.ItemDetailGrid.EditorStore.getModifiedRecords().length;
                var isInData=false;
                this.saveOnlyFlag = false;
                var approvalSerialData = [];
                if(cnt>0){
                    var editedRecords = this.ItemDetailGrid.EditorStore.getModifiedRecords(); 
                    for(var x=0;x<cnt;x++){ 
                        var productid = editedRecords[x].get('productid');
                        var pid = editedRecords[x].get('pid');
                        var reason=editedRecords[x].get('reason').trim();
                        var costcenter=editedRecords[x].get('costcenter').trim(); 
                        var remark=editedRecords[x].get('remark').trim(); 
                        var quantity=editedRecords[x].get('quantity');
                        var defaultlocqty=editedRecords[x].get('defaultlocqty');
                        var deflocation=editedRecords[x].get('deflocation');
                        var adjustmentType=editedRecords[x].get('adjustmentType');
                        var stockDetails=editedRecords[x].get('stockDetails');
                        var isRowForProduct=editedRecords[x].get('isRowForProduct');
                        var isRackForProduct=editedRecords[x].get('isRackForProduct');
                        var isBinForProduct=editedRecords[x].get('isBinForProduct');
                        var isBatchForProduct=editedRecords[x].get('isBatchForProduct');
                        var isSerialForProduct=editedRecords[x].get('isSerialForProduct');
                        var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchForProduct && !isSerialForProduct;
                        var isDefaultAllocationAllowed = !(isRowForProduct || isRackForProduct || isBinForProduct || isBatchForProduct || isSerialForProduct);
                        var isProductIdEmpty= (productid == undefined || productid == "");
                        
                        if(!isProductIdEmpty && quantity==0){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"), WtfGlobal.getLocaleText("acc.stockadjustment.Quantitycannotbe0")],3);
                            return;
                        }
                        if (!this.isJobWorkInReciever) {
                            if(!isProductIdEmpty &&  this.adjReasonCombo.getValue()==""||this.adjReasonCombo.getValue()==undefined){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"), WtfGlobal.getLocaleText("acc.stockadjustment.AdjustmentReasoncannotempty")],3);
                                return;
                            }
                        }
                        if (!isProductIdEmpty && adjustmentType == undefined || adjustmentType == "") {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.JE.PleaseSelectAdjustmentType")], 0);
                            return false;
                        }
                        if(!isProductIdEmpty &&  this.memoTextArea.getValue().trim()==""||this.memoTextArea.getValue()==undefined){
                            WtfComMsgBox(["Alert", "Memo cannot empty."],3);
                            return;
                        }
                        
                        if (!isProductIdEmpty && (stockDetails == "" || stockDetails == undefined) && (isBatchForProduct || isSerialForProduct)) {
                            WtfComMsgBox(["Alert", "Please select stock location detail for item."], 3);
                            return;
                        } else if (!isProductIdEmpty && (stockDetails == "" || stockDetails == undefined) && (!isBatchForProduct && !isSerialForProduct)) {
                            var detailArray = new Array();
                            var data = {};
                            if (deflocation != "" && deflocation != undefined) {
                                data.locationId = deflocation;
                                data.quantity = quantity;
                                data.rowId = "";
                                data.rackId = "";
                                data.binId = "";
                                data.batchName = "";
                                data.serialNames = "";
                                detailArray.push(data);

                                editedRecords[x].set("stockDetails", detailArray);
                                editedRecords[x].set("stockDetailQuantity", quantity);
                                stockDetails=detailArray;
                            } else {
                                WtfComMsgBox(["Alert", "Please select stock location detail for item."], 3);
                                return;
                            }
                        }
                        
                        if (!isProductIdEmpty && adjustmentType != "Stock IN") {
                            if (x > 0) {
                               var prodQtyBlocked = 0;
                                for (var prevRec = 0; prevRec < x; prevRec++) {
                                    var prevRow = editedRecords[prevRec];
                                    if (productid == prevRow.get('productid') && (prevRow.get('adjustmentType') == "Stock Out" || prevRow.get('adjustmentType') == "Stock Sales") && !isNegativeAllowed) {
                                        prodQtyBlocked += prevRow.get('stockDetailQuantity');
                                        if (stockDetails != "" && stockDetails != undefined) {
                                        for (var stk = 0; stk < stockDetails.length; stk++) {
                                            var selectedStockLocationId = stockDetails[stk].locationId;
                                            var selectedStockQty = stockDetails[stk].quantity;
                                            if (deflocation == selectedStockLocationId && selectedStockQty + prodQtyBlocked > defaultlocqty) {
                                                WtfComMsgBox(["Alert", "Stock is not sufficient in the System for this Product to stock-out : " + pid + " at row No. <b>" + (x + 1) + " </b>"], 3);
                                                return;
                                            }
                                        }
                                    }
                                    }
                                }
                            }
                            if (!isNegativeAllowed && quantity > 0 && !isBatchForProduct && !isSerialForProduct) {
                                if (stockDetails != "" && stockDetails != undefined) {
                                    for (var stk = 0; stk < stockDetails.length; stk++) {
                                        var selectedStockLocationId = stockDetails[stk].locationId;
                                        var selectedStockQty=stockDetails[stk].quantity;
                                        if (deflocation == selectedStockLocationId && selectedStockQty > defaultlocqty) {
                                            WtfComMsgBox(["Alert", "Stock is not sufficient in the System for this Product to stock-out : " + pid + " at row No. <b>" + (x + 1) + " </b>"], 3);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        var stockDetailQuantity=editedRecords[x].get('stockDetailQuantity');
                        if(!(stockDetailQuantity > 0 && stockDetailQuantity == quantity)){
                            if(((!isBatchForProduct&&!isSerialForProduct)&&quantity>defaultlocqty)||((isBatchForProduct||isSerialForProduct))){
                                WtfComMsgBox(["Alert", "Adjusted quantity and detail quantity is not matched for item "+pid],3);
                                return;
                            }
                        }
                        
                        
                        
                        if(productid=="" && quantity > 0){ //reason==""
                            WtfComMsgBox(["Alert", "Please fill valid details."],3);
                            return;
                        }
                        if(!isProductIdEmpty && adjustmentType=="Stock IN"){
                            isInData=true;
                            if(editedRecords[x].get('isSerialForProduct')){
                                var productcode = editedRecords[x].get('pid');
                                for(var i= 0 ; i < stockDetails.length ; i++){
                                    var batchName = stockDetails[i].batchName;
                                    var serialnames = stockDetails[i].serialNames;
                                    var serialArr = serialnames.split(",");
                                    for(var s =0 ; s< serialArr.length ; s++){
                                        var approvalSerial = [];
                                        approvalSerial.push(x);
                                        approvalSerial.push(productcode);
                                        approvalSerial.push(batchName);
                                        approvalSerial.push(serialArr[s]);
                                        approvalSerialData.push(approvalSerial)
                                    }
                                    
                                }
                            }
                        }
                        
                    }
                }else{
                    //msgBoxShow(["Info", "Please fill the details"], 0);
                    return;
                }
                var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
                if(!isValidCustomFields){
                    return;
                }
                if(!this.Form.form.isValid()){
                    return;
                }else{
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.stockadjustment.Areyousureyouwanttosubmitrecords?"), function(btn){
                        if(btn == 'yes') {
                            if(Wtf.account.companyAccountPref.activateQAApprovalFlow && this.isActiveSAApproval && isInData){
                                Wtf.Msg.show({
                                    title: WtfGlobal.getLocaleText("acc.common.confirm"),
                                    msg: WtfGlobal.getLocaleText("acc.stockadjustment.alert1"),
                                    buttons: Wtf.Msg.YESNOCANCEL,
                                    scope: this,
                                    fn: function (btn) {
                                        if (btn == 'yes') {
                                            if (approvalSerialData.length > 0) {
                                                this.openSerialSelectionWindow(approvalSerialData)
                                            } else {
                                                this.Save(false, true);
                                            }
                                        } else if (btn == 'no') {
                                            this.Save(false, false);
                                        } else {
                                            return;
                                        }
                                    },
                                    icon: Wtf.MessageBox.QUESTION
                                });
                                
//                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.stockadjustment.alert1"), function(btn){
//                                    if(btn == 'yes') {
//                                        if(approvalSerialData.length > 0){
//                                            this.openSerialSelectionWindow(approvalSerialData)
//                                        }else{
//                                            this.Save(false,true);
//                                        }
//                                    }else
//                                        this.Save(false,false);
//                                },this);
                            }else{
                                this.Save(false,false);
                            }
                        }else
                            return;
                    },this);
                }
            },
            scope:this
        });
        this.cancelBut= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"), //'cancel',,
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.je.ClicktoCancel")
            },
            scope:this,
            disabled:this.isView?true:false,
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            handler:function(){
                this.refreshMarkoutForm();
                Wtf.stockAdjustmentTempDataHolder = [];
                Wtf.stockAdjustmentProdBatchQtyMapArr= [];
            }
        });
        
        /*Print Single Record*/
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        var colModArray = [];
        if(this.isJobWorkInReciever){
            //get job work stock in module templates
            colModArray = GlobalCustomTemplateList[Wtf.autoNum.JobWorkStockInModuleID];
        } else{
            colModArray = GlobalCustomTemplateList[Wtf.Acc_Stock_Adjustment_ModuleId];
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
            disabled:this.isView?false:true,
            menu:this.printMenu
        });
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.Form,
            this.ItemDetailGrid,
            this.southSummaryPanel
            ],
            bbar:[{
                xtype:"button",
                text:"View draft",
                tooltip: {
                    text:"View list of saved form"
                },
                hidden:true,
                id:'viewmarkoutdrafts'+this.id,
                scope: this,
                handler:function(){
                    this.createDraftListWin();
                }
            },'-',this.onlySavingBttn,
            //"-", this.saveBut,"-",
            this.submitBut,"-",this.cancelBut,
            "-",{
                xtype:"button",
                text:"Print",
                id:'printform',
                iconCls:'pwnd Printicon caltb',
                hidden:true,
                scope: this,
                tooltip: {
                    text:"Click to Print list of items to count"
                },
                handler:function(){
                    var str = "jspfiles/inventory/printOut.jsp?store=" +this.fromstoreCombo.getValue();
                    str += "&date="+this.dateField.getRawValue();
                    str += "&mod="+this.MOUTextField.getValue();//Date.parseDate(this.countDate.getRawValue(), Wtf.get"Y-m-d"()).format(Wtf.getDateTimeFormat())
                    //                                    str += "&subcat="+this.subcategoryCmb.getValue();
                    str += "&flag=55";
                    //                                    str += "&typeFlag=1";
                    str+="&action="+this.type;
                //window.open(str, "mywindow","menubar=1,resizable=1,scrollbars=1");
                }
            },'-', this.singleRowPrint
            ]
        });

        this.add(this.mainPanel);
        //        Wtf.Ajax.requestEx({
        //            url:"jspfiles/inventory/store.jsp",
        //            params: {
        //                flag:19
        //            }
        //        }, this,
        //        function(response){
        //            var res=eval('('+response+')');
        //            //            this.storeTextField.setValue(res.data[0].storedescription);
        //            //            if(checktabperms(12, 1) != "edit")
        //            this.MOUTextField.setValue(res.data[0].username);
        //        // this.fromstoreCombo.setValue(res.data[0].storeid);
        //        //  this.loaditemcombo(res.data[0].analysiscode);
        //        },
        //        function(response){});
        //
        //        this.fromStore.on("load", function(ds, rec, o){
        //            if(rec.length > 0){
        //                this.fromstoreCombo.setValue(rec[0].data.id, true);
        //            //                              this.loaditemcombo(rec[0].data.code);
        //            }
        //            this.setBusinessDate();
        //
        //        }, this);
                
        
        this.on("activate",function()
        {
            //           this.ItemDetailGrid.itemEditorStore.load({
            //            params:{
            //                flag:22,
            //                action:this.type
            //            }
            //        });
        

            },this);
    },
    openSerialSelectionWindow: function(approvalSerialSimpleStoreData){
        this.winTitle=WtfGlobal.getLocaleText("acc.stockadjustment.Selectserialforapproval");
        this.winDescriptionTitle= "Select serial for approval";
        this.winDescription="Select serial to send for apprval" ;
        
        var gridStore = new Wtf.data.SimpleStore({
            fields:['recIndex', 'productcode', 'batch', 'serial' ]
        })

        //        if(approvalSerialSimpleStoreData != '' && approvalSerialSimpleStoreData != null && approvalSerialSimpleStoreData != undefined){
        //            var serialCmbData = [];
        //            for(var i=0 ; i<serialArr.length ; i++){
        //                serialCmbData.push([approvalSerialSimpleStoreData[i].productid,serialGridDataArr[i].productcode,serialGridDataArr[i].batch,serialGridDataArr[i].serial ])
        //            }
        gridStore.loadData(approvalSerialSimpleStoreData)
        //        }
        var sm = new Wtf.grid.CheckboxSelectionModel({
            width:25
        });
        var cm = new Wtf.grid.ColumnModel([
            sm,
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.inventorysetup.serial"),
                dataIndex:"serial"
            },{
                header:WtfGlobal.getLocaleText("acc.product.gridProduct"),
                dataIndex:"productcode"
            },{
                header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
                dataIndex:"batch"
            }]);
        
        this.serialGrid=new Wtf.grid.GridPanel({
            region: 'center',
            border: false,
            store: gridStore,
            cm: cm,
            sm:sm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true
            }
        })
        
        
        var serialSelectionWindow = new Wtf.Window({
            id:'approvalserialselectionwindowid',
            title : this.winTitle,
            modal : true,
            scope:this,
            iconCls : 'iconwin',
            minWidth:100,
            width : 300,
            height: 300,
            resizable :true,
            scrollable:true,
            buttonAlign : 'right',
            layout : 'border',
            items :[{
                region : 'center',
                border : false,
                //                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 0px 0px 0px;',
                layout : 'fit',
                items : [this.serialGrid]
            }],
            buttons :[{
                text : WtfGlobal.getLocaleText("acc.common.saveBtn"),
                iconCls: 'pwnd save',
                scope : this,
                handler: function(){  
                    var recs = this.serialGrid.getSelectionModel().getSelections();
                    var approvalSerialArr = {}
                    for(var i=0 ; i<recs.length ; i++){
                        var data = {};
                        var recIndex = recs[i].get('recIndex')
                        data.batch = recs[i].get('batch');
                        data.serial = recs[i].get('serial');
                        if(approvalSerialArr[recIndex] == undefined || approvalSerialArr[recIndex] == null){
                            approvalSerialArr[recIndex] = [];
                        }
                        approvalSerialArr[recIndex].push(data);
                    }
                    var ItemGridStore = this.ItemDetailGrid.EditorStore;
                    ItemGridStore.each(function(rec){
                        var recIndex = ItemGridStore.indexOf(rec);
                        var details = rec.get('stockDetails');
                        if(approvalSerialArr[recIndex] != undefined && approvalSerialArr[recIndex] != null){
                            var approvalSerials = approvalSerialArr[recIndex];
                            for(var i=0 ; i< details.length ; i++){
                                var detail = details[i];
                                for(var j=0 ; j<approvalSerials.length ; j++){
                                    if(detail.batchName == approvalSerials[j].batch){
                                        if(detail.approvalSerials == undefined ){
                                            detail.approvalSerials = approvalSerials[j].serial
                                        }else{
                                            detail.approvalSerials += ","+ approvalSerials[j].serial
                                        }
                                    }
                                }
                            }
                        }
                        
                    }, this);
                    
                    if(recs.length == 0){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.stockadjustment.alert2"), function(btn){
                            if(btn == 'yes') {  
                                Wtf.getCmp('approvalserialselectionwindowid').close();
                                this.Save(false,false);
                            }else if(btn == 'no') {
                            // keep serial window open.ie. no action to be performed on cancel
                            }
                        },this);
                    }else{
                        Wtf.getCmp('approvalserialselectionwindowid').close();
                        this.Save(false,true);
                    }
                    
                }
            },{
                text : WtfGlobal.getLocaleText("acc.field.Cancel"),
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                minWidth:75,
                scope:this,
                handler : function() {
                    Wtf.getCmp('approvalserialselectionwindowid').close();
                }
            }]
        }).show();  
    },
    refreshMarkoutForm:function(){
        this.ItemDetailGrid.EditorStore.removeAll();
        this.ItemDetailGrid.addRec();
        Wtf.stockAdjustmentTempDataHolder = [];
        Wtf.stockAdjustmentProdBatchQtyMapArr= [];
    },
    deleteSelectedRecord: function() {
        /*
         * Fetching Store
         */
        var store = this.ItemDetailGrid.EditorGrid.getStore();
        var rowindex = 0;
        var selectedCount = this.ItemDetailGrid.EditorGrid.selModel.getCount();
        var selections = this.ItemDetailGrid.EditorGrid.selModel.getSelections();
        var totalCount = store.getCount();
        /*
         * If total Count <= 1, then not allowing to delete.
         */
        if (totalCount <= 1) {
            WtfComMsgBox(["Alert", WtfGlobal.getLocaleText("acc.markout.lastpoductdelet.alert.msg")],3);
            return;
        } else {
            if (totalCount == selectedCount) {
                WtfComMsgBox(["Alert", WtfGlobal.getLocaleText("Can not delete all Products.")],3);
                return;
            }
            /*
             * Removing all records selected
             */
            for (rowindex = 0; rowindex < selectedCount; rowindex++) {
                var id = selections[rowindex].id;
                store.remove(store.getById(id));
                this.ItemDetailGrid.ArrangeNumberer(this.ItemDetailGrid.EditorGrid, rowindex);
            }
        }


    },
    
    setBusinessDate:function(){
    //        if(this.checkViewDraft==0){
    //            Wtf.Ajax.requestEx({
    //                url: "jspfiles/inventory/inventory.jsp",
    //                params: {
    //                    flag: 105,
    //                    storeid: this.fromstoreCombo.getValue()
    //                }
    //            },
    //            this,
    //            function(action, response){
    //                action = eval("("+ action + ")");
    //            //                    this.dateField.setValue(action.data);
    //            },
    //            function(){
    //                }
    //                );
    //        }
    },
     
    loaditemcombo:function(temp){
        //hardcode id is given
        if( temp == "482"){
            this.ItemDetailGrid.EditorStore.removeAll();
            this.ItemDetailGrid.EditorStore.load({
                params:{
                    flag:22,
                    action:this.type,
                    tech:"yes"
                }
            })
        } else{
            this.ItemDetailGrid.EditorStore.removeAll();
            this.ItemDetailGrid.EditorStore.load({
                params:{
                    flag:22,
                    action:this.type
                }
            })
        }
    },
    getForm:function (){
        

        this.SOStoreRec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'discount'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'duedate', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'creditoraccount'},
            {name:'personid'},
            {name:'shipping'},
            {name:'othercharges'},
            {name:'taxid'},
            {name:'productid'},
            {name:'discounttotal'},
            {name:'isAppliedForTax'},// in Malasian company if DO is applied for tax
            {name:'discountispertotal',type:'boolean'},
            {name:'currencyid'},
            {name:'currencysymbol'},
            {name:'amount'},
            {name:'amountinbase'},
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'lasteditedby'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'includeprotax',type:'boolean'},
            {name:'salesPerson'},
            {name:'islockQuantityflag'},
            {name:'agent'},
            {name:'termdetails'},
            {name:'LineTermdetails'},//Line Level Term Details
            {name:'shiplengthval'},
            {name:'gstIncluded'},
            {name:'quotationtype'},
            {name:'contract'},
            {name:'termid'},
            {name:'externalcurrencyrate'},//    ERP-9886
            {name:'customerporefno'},
            {name:'isexpenseinv'},
            {name: 'billingAddressType'},
            {name: 'billingAddress'},
            {name: 'billingCountry'},
            {name: 'billingState'},
            {name: 'billingPostal'},
            {name: 'billingEmail'},
            {name: 'billingFax'},
            {name: 'billingMobile'},
            {name: 'billingPhone'},
            {name: 'billingContactPerson'},
            {name: 'billingRecipientName'},
            {name: 'billingContactPersonNumber'},
            {name: 'billingContactPersonDesignation'},
            {name: 'billingWebsite'},
            {name: 'billingCounty'},
            {name: 'billingCity'},
            {name: 'shippingAddressType'},
            {name: 'shippingAddress'},
            {name: 'shippingCountry'},
            {name: 'shippingState'},
            {name: 'shippingCounty'},
            {name: 'shippingCity'},
            {name: 'shippingEmail'},
            {name: 'shippingFax'},
            {name: 'shippingMobile'},
            {name: 'shippingPhone'},
            {name: 'shippingPostal'},
            {name: 'shippingContactPersonNumber'},
            {name: 'shippingContactPersonDesignation'},
            {name: 'shippingWebsite'},
            {name: 'shippingRecipientName'},
            {name: 'shippingContactPerson'},
            {name: 'shippingRoute'},
            {name: 'vendcustShippingAddress'},
            {name: 'vendcustShippingCountry'},
            {name: 'vendcustShippingState'},
            {name: 'vendcustShippingCounty'},
            {name: 'vendcustShippingCity'},
            {name: 'vendcustShippingEmail'},
            {name: 'vendcustShippingFax'},
            {name: 'vendcustShippingMobile'},
            {name: 'vendcustShippingPhone'},
            {name: 'vendcustShippingPostal'},
            {name: 'vendcustShippingContactPersonNumber'},
            {name: 'vendcustShippingContactPersonDesignation'},
            {name: 'vendcustShippingWebsite'},
            {name: 'vendcustShippingContactPerson'},
            {name: 'vendcustShippingRecipientName'},
            {name: 'vendcustShippingAddressType'}
        ]);
        this.SOStore = new Wtf.data.Store({
            url:"ACCSalesOrderCMN/getSalesOrders.do",
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                /*
                 * to fetch Job Work Orders which have quantity remained to be adjusted i.e. in Open state
                 */
                isJobWorkInReciever:true,
                isJobWorkOrderReciever:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SOStoreRec)
        });
                
        this.storeRec = new Wtf.data.Record.create([
        {
            name:"store_id"
        },

        {
            name:"abbr"
        },

        {
            name:"fullname"
        },
        {
            name:"defaultlocationid"
        },
        {
            name:"defaultlocation"
        },
        {
            name:"storetypeid"          //ERM-691 allow only Stock Out for scrap store hence retreiving store type
        }
        ]);
        
        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        this.storejspurl = "store.jsp";
        this.storedisfield = "name";

        //        if(Wtf.realroles[0]==9){  //For FM MARKOUT AMEND PROCESS : show all T1 store
        //            this.storejspurl = "inventory.jsp";
        //            this.storedisfield = "description";
        //        }
        
        this.Name= new Wtf.form.ExtFnComboBox({
                fieldLabel:"<span wtf:qtip='"+  WtfGlobal.getLocaleText("acc.invoiceList.cust.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.cust") +"</span>",
                id:"customer"+this.id,
                store: Wtf.customerAccRemoteStore,
                valueField:'accid',
                name:'customer',
                displayField:'accname',
                minChars:1,
                extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                allowBlank:!this.isJobWorkInReciever,
                hirarchical:true,
                emptyText:WtfGlobal.getLocaleText("acc.inv.cus"),
                mode: 'remote',
                typeAhead: true,
                hidden:!this.isJobWorkInReciever,
                hideLabel:!this.isJobWorkInReciever,
                extraComparisionField:'acccode',
                typeAheadDelay:30000,
                forceSelection: true,
                selectOnFocus:true,
                isVendor:false,
                isCustomer:true,
                width : 200,
                triggerAction:'all',
                scope:this
            });  
            /*
             *sending isView's value in view case as store have to load the SO even if they are closed.
             */
            if (this.isView) {
                this.Name.store.load({
                    scope: this,
                    callback: function () {
                        this.Name.setValue(this.viewRec.data.customerid);
                    }
                });
            }
           
        
        this.fromStore = new Wtf.data.Store({
            url :"INVStore/getStoreList.do",
            reader:this.storeReader
        });
        this.fromStore.on("load", function(ds, rec, o){
            if(this.isView){
                this.fromstoreCombo.setValue(this.viewRec.data.store_id, true);
            }else
            if(rec.length > 0){
                this.fromstoreCombo.setValue(rec[0].data.store_id, true);
            }
        }, this);
        this.fromStore.load({
            params:{
                isActive : "true",
                byStoreManager:"true",
                byStoreExecutive:"true",
                isFromInvSATransaction:"true"
            }
        });
        
        //        if(Wtf.realroles[0]==9){  //For FM MARKOUT AMEND PROCESS : show all T1 store
        //            this.fromStore.load({
        //                params:{
        //                    flag:2,
        //                    type:"T1"
        //                }
        //            });
        //        }else{
        //            this.fromStore.load({
        //                params:{
        //                    flag:14,
        //                    action:'frm'
        //                }
        //            });
        //        }
        this.jobWorkOrderRecCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"remote",
            typeAhead:true,
            name:"jobworkorderno",
            forceSelection:true,
            store:this.SOStore,
            hideLabel:!this.isJobWorkInReciever,
            hidden:!this.isJobWorkInReciever,
            displayField:"billno",
            valueField:"billid",
            fieldLabel:WtfGlobal.getLocaleText("acc.jobWorkOrder.vendorjobworkorder"),
            allowBlank:!this.isJobWorkInReciever,
            width:200,
            disabled:true,
            parent:this
        });
        
        this.SOStore.on('beforeload',function(s,o){
            if(!o.params) {
                o.params={};
            }
            var currentBaseParams = this.SOStore.baseParams;
            if (this.isView) {
                currentBaseParams.id=this.viewRec.data.customerid;
                if(this.isJobWorkInReciever){
                    currentBaseParams.isViewJWSI=this.isView;
                }
            } else {
                currentBaseParams.id=this.Name.getValue();
            }
            this.SOStore.baseParams=currentBaseParams;        
        },this); 
        
        this.Name.on("change",function(){
            this.jobWorkOrderRecCombo.enable();
            this.jobWorkOrderRecCombo.reset();
            this.SOStore.load();
        },this);
        
        this.jobWorkOrderRecCombo.on("change",function(){
            this.loadSalesOrderProduct();
            this.challanNo.enable();
            this.challanNo.reset();
        },this);
        
        this.fromstoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.fromStore,
            displayField:"fullname",
            valueField:"store_id",
            fieldLabel:this.isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobworkin.create.Store"):WtfGlobal.getLocaleText("acc.je.FromStore*"),
            id: this.isJobWorkInReciever ? "jobworkfromstore" + this.id : "fromstore" + this.id,
            hiddenName:"fromstore",
            allowBlank:false,
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
        
        this.jobWorkOrderRecCombo.store.on('load',function() {
            if(this.isView) {
                this.jobWorkOrderRecCombo.setValue(this.viewRec.data.jobworkorderid, true);
            }
        },this);
        if (this.isView) {
            this.jobWorkOrderRecCombo.store.load();
        }
        
        this.fromstoreCombo.on("change",function(){
            this.parent.refreshMarkoutForm();
        });
       
        this.documentNumber = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.DocumentNo"),
            name:'documentNo',
            width : 200,
            maxLength:50,
            //hidden:this.isView?true:false,
            value:this.isView?this.viewRec.data.seqNumber:""
        });
        this.challanNo = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("Job Work Delivery Challan No.")+"*",
            name:'challanNo',
            id: this.isJobWorkInReciever ? 'SAchallonno' + this.id : 'SAchallonno1' + this.id,
            width : 200,
            hidden:!this.isJobWorkInReciever,
            hideLabel:!this.isJobWorkInReciever,
            disabled:true,
            allowBlank:!this.isJobWorkInReciever,
            value:this.isView?this.viewRec.data.stockDetails[0].batchName:""
            
        });
        this.challanNo.on("change",function(textField, newVal, oldVal){
            /*
             * Setting Batch Name (Challan No) for all the line items
             */
            /*
             * Fetching Store 
             */
            var storeid = this.fromstoreCombo.getValue();
            var idx = this.fromStore.find("store_id",storeid);
            var rec = this.fromStore.getAt(idx);
            /*
             * Fetching Location
             */
            this.defaultLocationId=rec.get("defaultlocationid");
            this.defaultLocationName=rec.get("defaultlocation");
            
            if(textField){
                if (textField.getValue()) {
                    /*
                    * For all Products at Line Level
                    */
                    for(var f=0;f<this.ItemDetailGrid.EditorStore.getCount();f++){
                        
                        /*
                         * Forming Stock Detail Array
                         */
                        var detailArray = new Array();
                        var data = {};

                        data.locationId = this.defaultLocationId;
                        data.quantity = this.ItemDetailGrid.EditorStore.getAt(f).get("quantity");
                        data.rowId = "";
                        data.rackId = "";
                        data.binId = "";
                        data.batchName = textField.getValue();
                        data.serialNames = "";
                        data.productid = this.ItemDetailGrid.EditorStore.getAt(f).get("productid");
                        data.storeid = storeid;
                        
                        /*
                         * Check For Duplicate challan No.
                         */
                        this.ItemDetailGrid.checkDuplicateChallanNo(data);
                        
                        /*
                         *  Setting details Array in grid rec.
                         */
                        detailArray.push(data);
                        this.ItemDetailGrid.EditorStore.getAt(f).set("stockDetails", detailArray);
                        this.ItemDetailGrid.EditorStore.getAt(f).set("stockDetailQuantity", this.ItemDetailGrid.EditorStore.getAt(f).get("quantity"));
                    }
                    
                }
            }
        },this);
        this.sequenceFormatNO = new Wtf.SeqFormatCombo({
            seqNumberField : this.documentNumber,
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.sequenceformat")+"*",
            name:"seqFormat",
            moduleId:3,
            isView:this.isView,
            allowBlank:false,
            hidden:this.isView?true:false,
            hideLabel:this.isView?true:false,
            width : 200
        });
        
        this.memoTextArea= new Wtf.form.TextArea({
            width:200,
            fieldLabel:WtfGlobal.getLocaleText("acc.repeated.Gridcol3")+"*",
            name:"memo",
            height:45,
            maxLength:2048,
            allowBlank:false,
            value:this.isView?this.viewRec.data.memo:""
        });
        
        this.adjReasonTextField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.stock.AdjustmentReason"),
            width:200
        });
         
        this.storeTextField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),
            readOnly : true,
            width:200
        });
        this.dateField = new Wtf.form.DateField({
            fieldLabel:this.isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobworkin.create.Date"):WtfGlobal.getLocaleText("acc.stock.BusinessDate"),
            name:"businessdate",
            format:"Y-m-d",
            allowBlank:false,
            value:this.isView?this.viewRec.data.date:new Date(),
            width:200
        });
        this.ReturntoVendor = new Wtf.form.Checkbox({
            fieldLabel:"Return to Vendor:",
            labelSeparator:"",
            cls : 'custcheckbox'//show checkbox at bottom
        });   
       
        this.MOUTextField = new Wtf.form.TextField({
            fieldLabel:this.isJobWorkInReciever?WtfGlobal.getLocaleText("acc.jobworkin.create.User")+"*":WtfGlobal.getLocaleText("acc.je.MoD")+"*",
            readOnly : true,
            value:this.isView?this.viewRec.data.createdBy:_fullName,
            width:200
        });
        
        this.MOUHiddenField = new Wtf.form.TextField({
            fieldLabel:"MoDvalue",
            hidden : true,
            hideLabel:true,
            value:loginid,
            width:200
        });
        
        this.SequenceNumber = new Wtf.form.TextField({
            fieldLabel:"Sequence Number*",
            allowBlank : false,
            width:200
        });
        this.reasonRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        },
        {
            name: "defaultMasterItem"
        }
        ]);
      
        this.reasonRecReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.reasonRec);
        
        this.reasonStore = new Wtf.data.Store({
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                groupid	: 31,
                mode	: 112
            },
           
            reader:this.reasonRecReader
        });
        
        this.reasonStore.load();
        this.deleteBtn = new Wtf.Button ({
            cls: 'setlocationwarehousebtn',
            disabled:true,
            text: WtfGlobal.getLocaleText("acc.common.deleteselected"),
            handler: this.deleteSelectedRecord.createDelegate(this)
        });
        this.deleteSelectedPanel = new Wtf.Panel({
            style: 'padding: 10px 10px 0;',
            border: false,
            autoScroll: true,
            hidden: !this.isJobWorkInReciever,
            items: [this.deleteBtn]
        });
       
        this.adjReasonCombo= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.stock.AdjustmentReason")+"*",
            hiddenName:'reason',
            store:this.reasonStore,
            id:this.isJobWorkInReciever?"stockAdjReasonJWIN":(this.isView?'stockadjustmentreasoncomboIdglobal'+this.id:'stockadjustmentreasoncomboIdglobal'),
            width:200,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            hideLabel:this.isJobWorkInReciever,
            hidden:this.isJobWorkInReciever,          
            triggerAction:'all',
            forceSelection:true,
            allowBlank:this.isJobWorkInReciever,
            addNewFn:this.addReason.createDelegate(this)
        });           
        this.reasonStore.on("load", function(ds, rec, o){
            if(this.isView){
                this.adjReasonCombo.setValue(this.viewRec.data.adjustmentreasonid, true);
            } 
        }, this);
        if (Wtf.account.companyAccountPref.activateWastageCalculation) {
            this.adjReasonCombo.on('beforeselect', function(combo, record, index) {
                this.adjReasonComboBeforeSelect = combo.getValue();
            }, this);
        
            this.adjReasonCombo.on('select', function(combo, record, index) {
                if (combo.getValue() === this.adjReasonComboBeforeSelect) {
                    return;
                }
                if (this.ItemDetailGrid.itemEditorStore) {
                    this.ItemDetailGrid.itemEditorStore.reload();
                }
                this.refreshMarkoutForm();
            }, this);
        }
        //for custom fields
        this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm2" + this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: this.isJobWorkInReciever?Wtf.MRP_Job_Work_IN:Wtf.Inventory_Stock_Adjustment_ModuleId,
            record:this.viewRec,            
            isEdit: false
        });
        
        this.dateField.on('change', function(){
            if (!this.isJobWorkInReciever) {
                if(this.ItemDetailGrid.EditorStore.getCount()>0){
                     this.refreshMarkoutForm();
                }
            }
             
        },this);
           
        //        
        //        this.ReturntoVendor.on('check', function(){
        //            if(this.ReturntoVendor.getValue()==true){
        //                this.ItemDetailGrid.EditorGrid.colModel.setHidden(9,false);
        //            }else{
        //                this.ItemDetailGrid.EditorGrid.colModel.setHidden(9,true);
        //            //EditorColumn
        //            }
        //        },this);
        this.Form = new Wtf.form.FormPanel({
            region:"north",
            autoHeight:true,
            id:"northForm2" + this.id,
            //            id:"form1"+this.id,
            url:"INVStockAdjustment/requestStockAdjustment.do",
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            //            layout:'column',
            border:false,
            defaults:{
                border:false
            },
            disabled:this.isView?true:false,
            disabledClass:"newtripcmbss",
            items:[{
                border:false,
                layout:'form',
                autoHeight:true,
                cls:"visibleDisabled",
                items:[{
                    layout:'column',
                    border:false,
                    defaults:{
                        border:false
                    },
                    items:[{
                        columnWidth:0.50,
                        border:false,
                        layout:'form',
                        labelWidth:120,
                        cls:"visibleDisabled",
                        items:[
                        this.fromstoreCombo,
                        this.sequenceFormatNO,
                        this.documentNumber,
                        this.memoTextArea ,
                        this.Name,
                        this.jobWorkOrderRecCombo,
                        this.deleteSelectedPanel
                        ]
                    },{
                        columnWidth:0.50,
                        border:false,
                        layout:'form',
                        labelWidth:120,
                        cls:"visibleDisabled",
                        items:[
                        this.MOUTextField,this.dateField ,this.MOUHiddenField,this.adjReasonCombo,this.challanNo
                        ]
            
                    }]
                },this.tagsFieldset
                
            
                ]
            }] 
        });
        
//        if (this.isJobWorkInReciever) {
//            this.Form.items.items[0].items.items[0].items.items[0].add(this.jobWorkOrderRecCombo);
//            this.Form.doLayout();
//        }
    },
    getsouthPanel:function (){
        this.tplSummary=new Wtf.XTemplate("<div style='float:right;margin-right:15px'><span>Amount : </span><span><b>{total}</b> </span></div>");
        var totalamt=0;
        if(this.isView && this.tabtype==1){
            var price=this.viewRec.data.cost;
            var qty=this.viewRec.data.quantity;
            totalamt+=(price*qty);
            this.tplSummary=new Wtf.XTemplate("<div style='float:right;margin-right:15px'><span>Amount : </span><span><b>"+totalamt+"</b> </span></div>");
        }
        this.southSummaryPanel=new Wtf.Panel({
            region:"south",
            height:30,
            border:false,
            scope:this,
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            html:this.tplSummary.apply({
                total:"0.00"
            })
        });
    },
    loadSalesOrderProduct: function() {
        this.ItemDetailGrid.EditorGrid.getStore().proxy.conn.url = "ACCSalesOrderCMN/getSalesOrderRows.do";
        this.ItemDetailGrid.EditorGrid.getStore().load({
                params:{
                    bills:this.jobWorkOrderRecCombo.getValue(),
                    isForm:true,
                    isJobWorkInReciever:true
//                    productidstr:this.productidstr
                }
            });
            
            this.ItemDetailGrid.EditorGrid.getStore().on("load",function(e){
                if(this.isJobWorkInReciever) {
                    if (e != undefined && e != null && e.data != undefined && e.data != null) {
                        var recs = e.data.items;
                        var rec;
                        for (var i =0; i < recs.length; i++) {
                            rec = recs[i];
                            rec.set("adjustmentType","Stock IN");
                        }
                    }
                }
            },this);
        this.ItemDetailGrid.EditorGrid.getStore();    
        this.ItemDetailGrid.addRec();
        this.ItemDetailGrid.EditorGrid.getView().refresh();
    },
    addReason:function(){
        addMasterItemWindow('31'); // 31 is for stock adjustment reason
        var panel = Wtf.getCmp("masterconfigurationonly");
        panel.on("update",function(){
            this.reasonStore.reload();
        },this)
         
    },
     
    getItemDetail:function (){
        this.ItemDetailGrid = new Wtf.markoutGrid({
            layout:"fit",
            gridTitle:WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetails"),
            border:false,
            region:"center",
            isJobWorkInReciever:this.isJobWorkInReciever,
            type11:this.type,
            tabtype:this.tabtype,
            parent:this,
            rec:this.viewRec,
            isView:this.isView,
            height:270,
            disabledClass:"newtripcmbss"
        });
    },
    
    isActiveSAApprovalFlow: function() {
        this.isActiveSAApproval=false;
        Wtf.Ajax.requestEx({
            url: "INVConfig/getConfig.do",
            params: {
                cid: companyid
            }
        },this,
        function(request, response) {
            if (request.data.enableStockOutApprovalFlow) {
                this.isActiveSAApproval=request.data.enableStockOutApprovalFlow;
            }
        },
        function() {
            this.isActiveSAApproval=false;
        }
        );

    },
    printRecordTemplate:function(printflg,item){
        var params= "myflag=order&transactiono="+Wtf.OrderNoteNo+"&moduleid="+Wtf.Acc_Stock_Adjustment_ModuleId+"&templateid="+item.id+"&recordids="+Wtf.recordbillid+"&filetype="+printflg;  
        if(this.viewRec)
        {
            params= "myflag=order&transactiono="+this.viewRec.data.seqNumber+"&moduleid="+Wtf.Acc_Stock_Adjustment_ModuleId+"&templateid="+item.id+"&recordids="+this.viewRec.data.id+"&filetype="+printflg;        //Wtf.OrderNoteNo is null during view mode  
        }
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = "ACCExportPrintCMN/exportSingleStockAdjustment.do";
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
    
    enableafterSaveButtons:function(enableflag){
        if(enableflag){//save
            this.singleRowPrint.enable();  
        }else{
            this.submitBut.enable();
            this.onlySavingBttn.enable();
        }
    },
    disableafterSaveButtons:function(disableflag){
        if(disableflag){//save button
            this.submitBut.disable();
            this.onlySavingBttn.disable();
        }else{//save and create new button
            this.singleRowPrint.disable();
        }
    },
    enableButtons:function(){
        this.saveBut.enable();
        this.submitBut.enable();
    },
    Save:function (isDraft,QAApproval){
        var jsondata = "";
        if(!this.documentNumber.getValue()&&this.sequenceFormatNO.getValue()=="NA"){
            WtfComMsgBox(["Alert", "Please enter valid Document No "],3);
            return;
        }
        if(this.ItemDetailGrid.EditorStore.getCount() == 0){
            // msgBoxShow(["Info", "Please add Item/s to Stockout"], 0);
            return;
        }
        this.dataflag=false;
        var modRecs;
        if(isDraft || (this.ItemDetailGrid.EditorGrid.store.lastOptions != null && this.ItemDetailGrid.EditorGrid.store.lastOptions.params.flag==26))
            // modRecs= this.ItemDetailGrid.EditorStore.getRange( 0, this.ItemDetailGrid.EditorStore.getCount()-1);
            modRecs= this.ItemDetailGrid.EditorStore.getModifiedRecords();
        else{
            modRecs= this.ItemDetailGrid.EditorStore.getModifiedRecords();
        }
             
        
        var cnt = modRecs.length;
        //    if(cnt>0){
        var dataArr=new Array();
        for(var i = 0; i < cnt; i++) {
            var rec = modRecs[i];
            if(rec.get("quantity") == 0 && (rec.get("productid") == "" || rec.get("productid") == null || rec.get("productid") == undefined)){
                continue;
            }
            if(rec.get("quantity") > 0 && (rec.get("productid") == "" || rec.get("productid") == null || rec.get("productid")== undefined)){
                WtfComMsgBox(["Alert", "Please enter valid data for Item "],3);
                return;
            }
            var stockouttype;
            if(this.type=="markout") {
                stockouttype = "stockout";
                if(rec.get("quantity") == 0 && rec.get("productid") != "" && rec.get("productid") != null && rec.get("productid") != undefined){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"), WtfGlobal.getLocaleText("acc.stockadjustment.Quantitycannotbe0")],3);
                    return;
                }
            //                else if(rec.get("costcenter") == ""){
            //                    WtfComMsgBox(["Alert", "Please select a costcenter"],3);
            //                    return;
            //                }
            //                else if(rec.get("reason") == ""){
            //                    msgBoxShow(["Info", "Please enter a reason"], 0);
            //                    return;
            //                }
            //                else if(rec.get("remark") == ""){
            //                    WtfComMsgBox(["Alert", "Please enter a remark"],3);
            //                    return;
            //                }
            } else {
                stockouttype = this.type;
            }
            //            if( ((Wtf.realroles[0]==9 && rec.data.quantity != 0)
            //                || (Wtf.realroles[0]!=9))|| isDraft){

            var arr = Wtf.decode(WtfGlobal.getCustomColumnData(this.ItemDetailGrid.EditorStore.data.items[i].data, Wtf.Acc_Stock_Adjustment_ModuleId).substring(13));
            var linelevelcustomdata= "";
            if (arr.length > 0)
                linelevelcustomdata = arr;
            
            var jObject={};
            var jArray=[];
            jObject.recordid =rec.data.recordid;
            jObject.productid = rec.data.productid ;
            jObject.productname = rec.data.productname ;
            jObject.pid = rec.data.pid ;
            jObject.uomid =  rec.data.uomid;
            jObject.uomname =  rec.data.uomname;
            jObject.quantity =rec.data.adjustmentType == "Stock IN" ? rec.data.quantity : -(rec.data.quantity);
            jObject.adjustmentType=rec.data.adjustmentType;
            jObject.reason=rec.data.reason;
            jObject.markoutid =''+rec.data.markoutid;
            //jObject.category = ''+rec.data.category;
            jObject.remark = rec.data.remark ;
            //jObject.partnumber = rec.data.partnumber ;
            if(rec.data.adjustmentType == "Stock Out" || rec.data.adjustmentType == "Stock Sales"){
                jObject.purchaseprice = 0 ;
            }else{
                jObject.purchaseprice = rec.data.amount ;
            }
            jObject.markouttype = stockouttype;
            jObject.date =  this.dateField.getRawValue();
            jObject.costcenter = rec.data.costcenter ;
            
            //            jArray.push(rec.data.stockDetails);
            jObject.stockDetails=rec.data.stockDetails;
            jObject.linelevelcustomdata=linelevelcustomdata;
            //jObject.reason =NewlineRemove(rec.data.reason);
                
            dataArr.push(jObject);
            this.dataflag = true;
        //                
        //            } else{          // bcz if we put data as '0' then msg prompt was coming
        //                if(rec.data.quantity != 0 && !isDraft && this.ItemDetailGrid.EditorGrid.store.lastOptions.params.flag!=26){
        //                   // msgBoxShow(["Info", "Please enter valid data for " + stockouttype + "."], 0);
        //                    return;
        //                }
        //            }

        }
       
        this.isDraft = isDraft;
        var finalStr = JSON.stringify(dataArr);
      
        this.sendStockoutRequest(finalStr, isDraft,QAApproval);
    //        } 
    },
    
    sendStockoutRequest:function(finalStr, isDraft,QAApproval){
        //checkForm(this.Form);
        //alert(finalStr);
        //        this.flg=0;
        //        if(stocoutApproval&&!isDraft){
        //            this.flg=119;
        //        }else{
        //            this.flg=13;
        //        }
       
        if(this.Form.form.isValid()){
            
            var allowNegInv="";
            if(this.allowNegativeInventory != undefined || this.allowNegativeInventory != ""){
                allowNegInv=this.allowNegativeInventory;
            }
            this.allowNegativeInventory="";
            
            var paramsObj = null;
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            var dimencustomfield="";
            if (custFieldArr.length > 0)
                dimencustomfield = JSON.stringify(custFieldArr);
            if(!isDraft){
                paramsObj = {
                    //flag:this.flg,
                    jsondata:finalStr,
                    isdraft: isDraft,
                    mod:this.MOUTextField.getValue(),
                    modId:this.MOUHiddenField.getValue(),
                    //draftid: this.draftId,
                    qApproval:QAApproval,
                    seqFormatId:this.sequenceFormatNO.getValue(),
                    documentNumber:this.documentNumber.getValue(),
                    returntovendor:this.ReturntoVendor.getValue(),
                    adjustmentReason:this.adjReasonCombo.getValue(),
//                    challanno:this.challanno.getValue(),
                    jobworkorderno:this.jobWorkOrderRecCombo.getValue(),
                    customer:this.Name.getValue(),
                    customfield:dimencustomfield,
                    isJobWorkInReciever:this.isJobWorkInReciever,
                    //fromlocation:this.locCmb.getValue(),
                    allowNegativeInventory: allowNegInv
                // longbusinessdate:this.dateField.get()
                }
            }else{
                paramsObj = {
                    // flag:this.flg,
                    fromstore:this.fromstoreCombo.getValue(),
                    memo:this.memoTextArea.getValue(),
                    jsondata:finalStr,
                    isdraft: isDraft,
                    mod:this.MOUTextField.getValue(),
                    modId:this.MOUHiddenField.getValue(),
                    //drafttype: this.drafttype,
                    seqFormatId:this.sequenceFormatNO.getValue(),
                    documentNumber:this.documentNumber.getValue(),
                    returntovendor:this.ReturntoVendor.getValue(),
                    isJobWorkInReciever:this.isJobWorkInReciever,
                    customer:this.Name.getValue(),
                    customfield:dimencustomfield,
                    adjustmentReason:this.adjReasonCombo.getValue()
                // fromlocation:this.locCmb.getValue()
                //longbusinessdate:this.dateField.getTime()
                //draftinfo: draftInfo
                }
            }
				
            this.saveBut.disable();
            this.submitBut.disable();
            this.onlySavingBttn.disable();
            this.cancelBut.disable();
            this.loadMask = new Wtf.LoadMask(document.body, {
                msg: 'Saving...'
            });
            
            WtfGlobal.setAjaxTimeOut();
            this.loadMask.show();
            this.Form.form.submit({
                params:paramsObj,
                scope:this,
                success:function (result,resp){
                    this.loadMask.hide();
                    WtfGlobal.resetAjaxTimeOut();
                    var msg = ""
                    var isfailure=false;
                    var retstatus = eval('('+resp.response.responseText+')');
                    if(!this.isDraft){
                        if(this.type=="markout"){
                            //var seqno=retstatus.seqNo;
                            var title="Error";
                            if(retstatus.data.success){
                                title="Success";
                                msg=retstatus.data.msg;
                                
                                if (!this.saveOnlyFlag) { //Checked for save button
                                    this.ItemDetailGrid.EditorGrid.store.rejectChanges();
                                }
                                
                                Wtf.MessageBox.show({
                                    title: title,
                                    msg: msg,
                                    icon: Wtf.MessageBox.INFO,
                                    buttons: Wtf.MessageBox.OK,
                                    scope: this,
                                    fn: function () {
                                        if (retstatus.data.StockAdjustmentNo != "" && retstatus.data.StockAdjustmentNo != undefined) {
                                            var itemObj = {
                                                billid: retstatus.data.billid,
                                                billno: retstatus.data.StockAdjustmentNo
                                            }
                                            printDocumentAfterSave(Wtf.Acc_Stock_Adjustment_ModuleId, itemObj);
                                        }
                                    }
                                });
                                this.draftId = "";
                                
                                Wtf.stockAdjustmentTempDataHolder = [];
                                Wtf.stockAdjustmentProdBatchQtyMapArr= [];
                            }
                            else if(retstatus.data.success==false && (retstatus.data.currentInventoryLevel != undefined && retstatus.data.currentInventoryLevel != "")){
                
                                if(retstatus.data.currentInventoryLevel=="warn"){

                                    Wtf.MessageBox.confirm("Confirm",retstatus.data.msg, function(btn){
                                        if(btn == 'yes') {        
                                            this.allowNegativeInventory=true;
                                            this.sendStockoutRequest(finalStr, isDraft);
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
                 
                            }else if(retstatus.data.msg){
                                 isfailure=true;
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.up.44"),retstatus.data.msg],retstatus.data.success*2+2);

                            }else{
                                isfailure=true;
                                Wtf.MessageBox.show({
                                    msg: retstatus.data.msg,
                                    icon:Wtf.MessageBox.ERROR,
                                    buttons:Wtf.MessageBox.OK,
                                    title:"Error"
                                });
                            }
                        }

                        this.disableafterSaveButtons(this.saveOnlyFlag);
                        this.enableafterSaveButtons(this.saveOnlyFlag);
                        Wtf.recordbillid=resp.result.data.billid;
                        Wtf.OrderNoteNo=resp.result.data.StockAdjustmentNo;
                        if(isfailure){
                           this.enableButtons();
                           this.cancelBut.enable();
                           this.onlySavingBttn.enable();
                        }
                        if (retstatus.data.success) {
                            if (this.saveOnlyFlag) {//Disabling and enabling after saving
                                this.Form.disable();
                                this.ItemDetailGrid.disable();
                            }
                            else {
                                this.ItemDetailGrid.EditorStore.removeAll();
                                this.ItemDetailGrid.addRec();
                                this.enableButtons();
                                this.cancelBut.enable();
                                this.memoTextArea.setValue("");
                                this.adjReasonCombo.setValue("");
                                this.documentNumber.setValue("");
                                this.Name.setValue("");
                                this.jobWorkOrderRecCombo.setValue("");
                                this.jobWorkOrderRecCombo.disable();
                                this.challanNo.setValue("");
                                this.challanNo.disable();
                                
                                this.tagsFieldset.resetCustomComponents();
                            }
                        }
                    }
                },
                failure:function (result,resp){
                    this.loadMask.hide();
                    var retstatus = eval('('+resp.response.responseText+')');
                    var msg=retstatus.msg;
                    if(!msg || msg == ""){
                        msg = "Error while adding ";
                    }
                    WtfGlobal.resetAjaxTimeOut();
                    if(!this.isDraft){
                        msgBoxShow(["Error",msg],1)
                    }else{
                        msgBoxShow(["Error","Problem occurred while saving Stock Adjustment in draft"],1);
                    }
                    this.saveBut.enable();
                    this.submitBut.enable();
                    this.cancelBut.enable();
                }
            });
        }  
    },

    validateRecord: function(rec){
        if(rec.get("id") == "" || rec.get("id") == null || rec.get("quantity") == 0){
            //            msgBoxShow(["Info", "Please enter valid data for Item "], 0);
            return false;
        } else if((rec.get("quantity") == 0 && rec.get("costcenter") != "")) {  //reason also with cost center
            //            msgBoxShow(["Info", "Please enter valid data for Quantity "], 0);
            return false;
        }
        else if( rec.get("quantity") > 0 && this.type == "markout" && (rec.get("costcenter") == "" || rec.get("costcenter") == null)) { //reason also with cost center
            //            msgBoxShow(["Info", "Please enter reason for Stockout "], 0);
            return false;
        } else
            return true;
    },
    createDraftListGrid: function(){
        this.draftsReader = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        },

        {
            name: 'description'
        },

        {
            name: 'savingdate'
        },

        {
            name: 'store'
        },
        {
            name:'storeid'
        }
        ]);

        this.draftsStore = new Wtf.data.Store({
            url:  "jspfiles/inventory/inventory.jsp",
            baseParams:{
                flag:9,
                type:this.drafttype
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.draftsReader)
        });

        //        this.draftsStore.load();

        this.draftsCm = new Wtf.grid.ColumnModel(
            [{
                //            header: "Saved On",
                header: "Business Date",
                //dataIndex: 'savingdate',
                sortable: true
            },{
                header: "Store Name",
                dataIndex: 'store'
            // sortable: true
            },{
                header: "Store Id",
                dataIndex: 'storeid',
                hidden: true
            }
            ]);


        this.draftSm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });

        this.draftGrid = new Wtf.grid.GridPanel({
            store: this.draftsStore,
            cm: this.draftsCm,
            sm:this.draftSm,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: true
            }
        });
    },

    createDraftListWin: function(){

        this.createDraftListGrid();

        this.saveDraftListWin = new Wtf.Window({
            title : "Stock Adjustment Drafts",
            modal : true,
            iconCls : 'iconwin',
            minWidth:75,
            width : 500,
            height: 400,
            resizable :false,
            buttonAlign : 'right',
            layout : 'border',
            items :[{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml("Stock Adjustment Drafts","Select a draft from list to open",'images/createuser.png')
            },{
                region : 'center',
                border : false,
                //bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 20px 20px 20px;',
                layout : 'fit',
                items : [{
                    border : false,
                    bodyStyle : 'background:transparent;',
                    layout : "fit",
                    items : [this.draftGrid]
                }]
            }],
            buttons :[{
                text : 'Open',
                scope : this,
                //id:'frmChangePassSetBtn',
                handler: function(){
                    if(!this.draftGrid.getSelectionModel().getSelected()){
                        //msgBoxShow(["Info", "Please select a draft to open"], 0);
                        return;
                    }
                    this.checkViewDraft=1;
                    this.openSavedDraft();
                }
            },{
                text : 'Cancel',
                scope : this,
                minWidth:75,
                //id: 'frmChangePassCancelBtn',
                handler : function() {
                    this.saveDraftListWin.close();
                }
            }]
        });
        this.saveDraftListWin.show();
    },

    openSavedDraft: function(){
        this.draftId = this.draftGrid.getSelectionModel().getSelected().get("id");
        this.storeId = this.draftGrid.getSelectionModel().getSelected().get("storeid");
        this.fromstoreCombo.setValue(this.storeId);
        this.savingdate= this.draftGrid.getSelectionModel().getSelected().get("savingdate");
        this.refreshMarkoutForm();
            
        this.tempRecord = new Wtf.data.Record.create([
        {
            name:"recordid"
        },
        {
            name:"id"
        },

        {
            name:"itemcode"
        },

        {
            name:"itemdescription"
        },
        {
            name:"partnumber"
        },
        {
            name:"itemdescription"
        },

        {
            name:"uom"
        },

        {
            name:"quantity"
        },

        {
            name:"markouttype"
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
        },{
            name:"category"
        }
        ]);

            
        this.tempStore = new Wtf.data.Store({
            url:"jspfiles/inventory/store.jsp",
            baseParams: {
                flag:26,
                draftid:this.draftGrid.getSelectionModel().getSelected().get("id")
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.tempRecord)
        });
            
        this.tempStore.load();
        this.tempStore.on("load",function(store,options){
            for(var x=0;x<this.tempStore.getCount();x++){
                var myNewRecord = this.tempStore.getAt(x);  
                this.ItemDetailGrid.addRec();
                this.ItemDetailGrid.EditorStore.getAt(x).set("id",myNewRecord.get('id'));
                this.ItemDetailGrid.EditorStore.getAt(x).set("itemcode",myNewRecord.get("itemcode"));               
                this.ItemDetailGrid.EditorStore.getAt(x).set("itemdescription",myNewRecord.get("itemdescription"));
                this.ItemDetailGrid.EditorStore.getAt(x).set("uom",myNewRecord.get("uom"));
                this.ItemDetailGrid.EditorStore.getAt(x).set("partnumber",myNewRecord.get("partnumber"));
                this.ItemDetailGrid.EditorStore.getAt(x).set("quantity",myNewRecord.get("quantity"));
                this.ItemDetailGrid.EditorStore.getAt(x).set("reason",myNewRecord.get("reason"));
                this.ItemDetailGrid.EditorStore.getAt(x).set("costcenter",myNewRecord.get("costcenter"));
                this.ItemDetailGrid.EditorStore.getAt(x).set("remark",myNewRecord.get("remark"));
                this.ItemDetailGrid.EditorStore.getAt(x).set("amount",myNewRecord.get("amount"));
            }
        }, this);
        this.saveDraftListWin.close();
    },
         
    createSaveInDraftWin: function(){
        if(!this.Form.form.isValid()){
            return;
        }
        this.Save(true);
    }

});

//------------------------------------------Editor Grid Component---------------------------------------------------

Wtf.markoutGrid = function (config){
    this.viewRec=config.rec;
    this.isView=config.isView;
    this.tabtype=config.tabtype;
    Wtf.apply(this,config);
    Wtf.markoutGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.markoutGrid,Wtf.Panel,{
    initComponent:function (){
        Wtf.markoutGrid.superclass.initComponent.call(this);
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
                html:"<div class='gridTitleClass'>"+this.gridTitle+"</div>"
            },
            this.EditorGrid
            ]
        });
        this.add(this.tmpPanel);
    },
    getEditorGrid:function (){
    
        this.itemEditorRec = new Wtf.data.Record.create([
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
            name:"uomid"
        },

        {
            name:"uomname"
        },

        {
            name:"purchaseprice"
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
            name:"parentproductname"
        },
        {
            name:"bomcode"
        },
        {
            name:"stockDetails"
        },
        {
            name:"hasAccess"
        }
        ]);
        this.itemEditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.itemEditorRec);
        
        this.reasonRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        }

        ]);
        this.reasonReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.reasonRec);

        this.reasonStore = new Wtf.data.Store({
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                groupid	: 31,
                mode	: 112
            },
            reader:this.reasonReader
        });
        
        this.reasonCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.reasonStore,
            fieldLabel:"Reason",
            displayField:"name",
            valueField:"id",
            allowBlank:false,
            editable:false,
            width:100,
            disabled:this.isView?true:false
        });
        
        /*
         * Creating Data Array for Adjustment Type Store
         * on checking if request is from job Work In
         */
        var data = [];
        data = [['Stock Sales'], ['Stock Out'], ['Stock IN']];
        if (this.isJobWorkInReciever) {
            data = [['Stock IN']];
        }
        
        this.AdjustmentTypeStore = new Wtf.data.SimpleStore({
            fields:['adjustmentType'],
            data:data,
            pruneModifiedRecords:true
        });
        
        this.AdjustmentTypeCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.AdjustmentTypeStore,
            displayField:"adjustmentType",
            valueField:"adjustmentType",
            allowBlank:false,
            editable:false,
            width:100,
            disabled:this.isView?true:false
        });
        this.AdjustmentTypeCombo.on("select", function () {
            /*Removed if block as only stock out adjustment is allowed by scrap store either qa flow is enable or disable*/
                var fromcombostore = this.parent.fromStore;
                var currentstoreid = this.parent.fromstoreCombo.getValue();
                var selectedrecord = WtfGlobal.searchRecord(fromcombostore, currentstoreid, 'store_id');
                if (selectedrecord !== undefined && selectedrecord.get('storetypeid') == 4 && this.AdjustmentTypeCombo.getValue() !== "Stock Out") {
                    WtfComMsgBox(["Warning", WtfGlobal.getLocaleText("acc.qaaproval.scrapstockoutonly")], 0); //WtfGlobal.getLocaleText("acc.product.uom.tt
                    this.AdjustmentTypeCombo.clearValue();
                    return;
                }
        }, this);
        
        
        this.EditorRec = new Wtf.data.Record.create([
        {
            name:"recordid"
        },
        {
            name:"id"
        },
        {
            name:"pid"
        },
        {
            name:"productid"
        },       
	{
            name:"productname"
        },
	{
            name:"uomid"
        },
	{
            name:"uomname"
        },
	{
            name:"purchaseprice"
        },
        {
            name : "desc"  
        },

        {
            name:"itemcode"
        },

        {
            name:"itemdescription"
        },
        {
            name:"partnumber"
        },
        {
            name:"itemdescription"
        },
        {
            name:"packaging"
        },

        {
            name:"uom"
        },

        {
            name:"quantity"
        },

        {
            name:"markouttype"
        },
        {
            name:"adjustmentType"
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
            name:"date"
        },{
            name:"category"
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
            name:"stockDetails"
        },
        {
            name:"stockDetailQuantity"
        },
        {
            name:"mfgdate"
        },
        {
            name:"expdate"
        },
        {
            name:"warrantyexpfromdate"
        },
        {
            name:"warrantyexptodate"
        },
        {
            name:"parentproductname"
        },
        {
            name:"bomcode"
        },
        {
            name:"defaultlocqty"
        },
        {
            name:"deflocation"
        },
        {
            name:"avlqty"
        },{
            name:'cost'
        }
        ]);

        this.EditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.EditorRec);

//        this.EditorStore = new Wtf.data.Store({
//            url:"jspfiles/inventory/store.jsp",
//            sortInfo:{
//                field: 'itemcode', 
//                direction: "DESC"
//            },
//            reader:this.EditorReader,
//            pruneModifiedRecords:true
//        });

        this.quantityTextField = new Wtf.form.NumberField({
            maxLength:10,
            allowBlank:false,
            decimalPrecision:4,
            disabled:this.isView?true:false,
            allowNegative: false,//(Wtf.realroles[0]==9)?true:false,    // For FM markout amend process
            renderer:function(val){
                if(!val)
                    return 0;
                return val;
            }
        });
        this.itemEditorRec = new Wtf.data.Record.create([
        {
            name:"recordid"
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
            name:"uomid"
        },

        {
            name:"uomname"
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
            name:"adjutmentType"
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
            name:"isBatchForProduct"
        },

        {
            name:"isSerialForProduct"
        },{
            name:"isSKUForProduct"
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
            name:'hasAccess'
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
        this.EditorStore = new Wtf.data.Store({
            url:"jspfiles/inventory/store.jsp",
            sortInfo:{
                field: 'itemcode', 
                direction: "DESC"
            },
            reader:this.EditorReader,
            pruneModifiedRecords:true
        });
        WtfGlobal.updateStoreConfig(GlobalColumnModel[Wtf.Acc_Stock_Adjustment_ModuleId], this.EditorStore);
        this.addRec();
        // this.itemEditorStore.on("load",this.addRec,this);
                
        this.EditorStore.on('load', function(){
            this.EditorGrid.getView().refresh();
        },this);

        this.itemEditorStore.on('beforeload',function(s,o) {
            var currentBaseParams = this.itemEditorStore.baseParams;
            currentBaseParams.excludeParent = true;
            this.reasonStore.reload();
            if (Wtf.account.companyAccountPref.activateWastageCalculation) {
                var adjReasonRecord = this.parent.reasonStore.getAt(this.parent.reasonStore.find('id',this.parent.adjReasonCombo.getValue()));
                currentBaseParams.isWastageApplicable = (adjReasonRecord && adjReasonRecord != null && adjReasonRecord.data.defaultMasterItem == Wtf.WASTAGE_ID) ? true : false;
            }
            
            this.itemEditorStore.baseParams = currentBaseParams;        
        },this);
        
        this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
        if(this.productOptimizedFlag== undefined || this.productOptimizedFlag==false){
            this.itemCodeCombo=new Wtf.form.ExtFnComboBox({
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
                forceSelection:true,
                disabled:this.isView?true:false,
                mode: 'remote'
                   
            });
            this.itemEditorStore.load();
        }
        else{
            if(this.productOptimizedFlag==Wtf.Products_on_type_ahead){
                var baseParams={
                    isStoreLocationEnable:true,
                    isInventoryForm:true,
                    excludeParent: true
                }
                this.itemCodeCombo =CommonERPComponent.createProductPagingComboBox(100,300,30,this,baseParams,false,true);
            }else{
                this.itemCodeCombo=new Wtf.form.ExtFnComboBox({
                    name:'itemcode',
                    store:this.itemEditorStore,    
                    typeAhead: true,
                    selectOnFocus:true,
                    valueField:'productid',
                    displayField:'pid',
                    extraFields:['productname'],
                    listWidth:300,
                    isProductCombo: true,
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
                    forceSelection:true,
                    disabled:this.isView?true:false
                });
            }
        }
        this.itemCodeCombo.on('beforeselect', function(combo, record, index) {
            return validateSelection(combo, record, index);
        }, this);
        
         /*
         *SDP-4553
         *Set Product Id When user scans barcode for product id field.
         *After scanning barcode by barcode reader press Enter or Tab button.
         *So we are handling specialkey event to set product id to product id combo.
         **/
        this.itemCodeCombo.on('specialkey', function(field , e) {
            if(e.keyCode == e.ENTER|| e.keyCode == e.TAB){
                if(field.getRawValue() !="" && field.getValue()==""){
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
                            setPIDForBarcode(this,dataObj,field,false,true);
                    
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
                                setPIDForBarcode(this,prorec,field,true,true);
                            }
                        }, function() {});
                    }
                }
            }
        },this);
        
        this.remarktextarea= new Wtf.form.TextArea({
//            regex:Wtf.validateAddress,
            maxLength:200,
            value : "",
            disabled:this.isView?true:false
        //            allowBlank : false
        });
        this.amounttextarea = new Wtf.form.NumberField({
            maxLength:10,
            allowBlank:false,
            decimalPrecision:4,
            disabled:this.isView?true:false,
            allowNegative: false,//(Wtf.realroles[0]==9)?true:false,    // For FM markout amend process
            renderer:function(val){
                if(!val)
                    return 0;
                return val;
            }
        });
        this.reasontextarea= new Wtf.form.TextArea({
            regex:Wtf.validateAddress,
            maxLength:200,
            value : "",
            disabled:this.isView?true:false,
            allowBlank : false
        });
        
        
        this.reasonRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        }
        ]);
      
        this.reasonRecReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.reasonRec);
        
        this.reasonStore = new Wtf.data.Store({
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                groupid	: 31,
                mode	: 112
            },
           
            reader:this.reasonRecReader
        });
        
        this.reasonStore.load();
        
        this.reasonCombo= new Wtf.form.FnComboBox({
            //fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.product.uom.tt")+"'>"+ WtfGlobal.getLocaleText("acc.product.uom")+"</span>",//WtfGlobal.getLocaleText("acc.product.uom"),//'Unit Of Measure*',
            hiddenName:'reason',
            store:this.reasonStore,
            id:'reasoncomboId'+this.id,
            anchor:'70%',
            //            allowBlank:false,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            triggerAction:'all',
            forceSelection:true,
            disabled:this.isView?true:false,
            addNewFn:this.addReason.createDelegate(this)
        });     
        if(Wtf.getCmp('stockadjustmentreasoncomboIdglobal')){
            Wtf.getCmp('stockadjustmentreasoncomboIdglobal').on('change',function(){
                this.reasonStore.reload();
                var EditorStore = this.EditorStore;
                this.reasonStore.on('load', function() {
                    for(var k=0;k<EditorStore.getCount()-1;k++){
                        if(EditorStore.getAt(k).get("reason") == ""){
                            EditorStore.getAt(k).set("reason",Wtf.getCmp('stockadjustmentreasoncomboIdglobal').getValue());
                        }
                    }
                }, this.reasonStore, {
                    single: true
                });
                
            },this)
        }
         
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
            disabled:this.isView?true:false,
            hirarchical:true
            
        }); 
        this.costCenterCombo.listWidth=250;
        
        this.quantityTextField.on("focus",this.setZeroToBlank,this);
        this.amounttextarea.on("focus",this.setZeroToBlank,this);
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            dataIndex:"selection",
            singleSelect:false
        });
        this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
        var columnArr = new Array();
        columnArr.push(new Wtf.grid.RowNumberer(),this.sm,
        {//ERP-12415 [SJ]
            header:WtfGlobal.getLocaleText("acc.common.add"),
            align:'center',
            width:30,
            dataIndex:"plusbtn",
            hidden:this.isJobWorkInReciever,
            renderer: this.addProductList.createDelegate(this)
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:65,
            align:'center',
            name:'srno',
            dataIndex:"seqno",
            hidden:true,
            renderer: WtfGlobal.itemSequenceRenderer
        },
        {
            header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"),
            // sortable:true,
            dataIndex:"pid",
            width:200,
            editor:this.isJobWorkInReciever?"":this.itemCodeCombo
        //  width:'10'
        },
        {
            header:WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"),
            // sortable:true,
            dataIndex:"productname",
            width:200,
            editor:this.itemEditorCombo/*,
//			renderer: Wtf.ux.comboBoxRenderer(this.itemEditorCombo)*/
        },
        {
            header:WtfGlobal.getLocaleText("acc.productList.gridProductDescription"),
            width:200,
            dataIndex:'desc'
        },
        {
            header:WtfGlobal.getLocaleText("acc.je.CoilcraftPartNo"),
            //sortable:true,
            dataIndex:"partnumber",
            //width:200,
            hidden: true//integrationFeatureFor == Wtf.IF.COILCRAFT ? false: true
        },
        
        //        {
        //            header:"Category",
        //            sortable:false,
        //            dataIndex:"category",
        //            width:200,
        //            hidden:false
        //        },
        {
            header:WtfGlobal.getLocaleText("acc.product.packaging"),
            dataIndex:"packaging",
            hidden:true
        },
        {
            header:WtfGlobal.getLocaleText("acc.invoice.gridUOM"),
            //sortable:true,
            dataIndex:"uomname"
        //   width:200
        //  width:'10'
        },
        /*
         * Added JW Assembly product and BOM in job Work stock in form. For show purpose only.
         */
        {
            header:WtfGlobal.getLocaleText("inv.stockAdj.grid.header.jobWorkAssembly"),
            //sortable:true,
            dataIndex:"parentproductname",
            hidden:!this.isJobWorkInReciever,
            width:200
        //  width:'10'
        },
        {
            header:WtfGlobal.getLocaleText("inv.stockAdj.grid.header.BOM"),
            //sortable:true,
            dataIndex:"bomcode",
            hidden:!this.isJobWorkInReciever
        //   width:200
        //  width:'10'
        },
        {
            header:WtfGlobal.getLocaleText("acc.field.CostCenter"),
            dataIndex:"costcenter",
            renderer:this.getComboRenderer(this.costCenterCombo),
            editor:this.costCenterCombo,
            hidden:this.isJobWorkInReciever
        },
        {
            header:WtfGlobal.getLocaleText("acc.stock.AdjustmentType"),
            sortable:false,
            dataIndex:"adjustmentType",
            width:200,
            editor:this.AdjustmentTypeCombo
            
        },
        {
            header:WtfGlobal.getLocaleText("acc.fixed.asset.quantity"),
            // sortable:true,
            dataIndex:"quantity",
            //width:200,
            align: 'right',
            editor:this.quantityTextField,
            renderer:this.quantityRenderer.createDelegate(this)
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
            dataIndex:"",
            //            align:'center',
            renderer: this.serialRenderer.createDelegate(this),
            //            // hidden:(!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory),
            hidden:this.isJobWorkInReciever,
            width:40
        },
        {
            header:WtfGlobal.getLocaleText("acc.dnList.reason"),
            sortable:false,
            dataIndex:"reason",
            width:200,
            hidden:false,
            renderer:(this.isView &&this.tabtype == 1)?'':Wtf.comboBoxRenderer(this.reasonCombo),
            editor:this.reasonCombo

        },
        {
            header:WtfGlobal.getLocaleText("acc.invoice.gridRemark"),
            // sortable:true,
            dataIndex:"remark",
            width:200,
            hidden:false,
            editor:this.remarktextarea
        },
        {
            header:WtfGlobal.getLocaleText("acc.stock.PerUnitPrice"),
            // sortable:true,
            dataIndex: this.isView ? "cost" : "amount",
            // width:200,
            //hidden:true,
            editor:this.amounttextarea,
            hidden:this.isJobWorkInReciever,
            renderer:this.quantityRenderer.createDelegate(this)
        });
        columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModel[this.isJobWorkInReciever?Wtf.MRP_Job_Work_IN:Wtf.Acc_Stock_Adjustment_ModuleId],undefined,undefined,this.isView);
        columnArr.push({
            header:WtfGlobal.getLocaleText("mrp.rejecteditems.report.actioncolumn.title"),
            align:'center',
            dataIndex: "lock",
            renderer: function(v,m,rec){
                return "<span class='pwnd delete-gridrow'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>";
            }
        });
     
        var CustomtotalStoreCount=0;
        var CustomloadedStoreCount=0;
        for (var custCount = 0; custCount < columnArr.length; custCount++) {
            if (columnArr[custCount].dataIndex.indexOf('Custom_') != -1 && (columnArr[custCount].fieldtype === 4 || columnArr[custCount].fieldtype === 7)) {
                CustomtotalStoreCount++;
                columnArr[custCount].editor.store.on('load', function () {
                    CustomloadedStoreCount++;
                    if (CustomtotalStoreCount === CustomloadedStoreCount && this.EditorGrid != undefined) {
                        if (this.viewRec != undefined && this.viewRec != null && this.viewRec.json != null && this.viewRec.json.transactionModule!=undefined && this.viewRec.json.transactionModule!=null && this.viewRec.json.transactionModule == "Stock Adjustment") {
                            this.setlinelevelcustomfields();
                            this.EditorGrid.getView().refresh();
                        }
                    }
                }, this)
            }
        }
//        this.EditorGrid.getView().refresh();
        this.EditorColumn = new Wtf.grid.ColumnModel(columnArr);
        /*,
        {
            header:"Reason",
            sortable:false,
            dataIndex:"reason",
            width:200,
            hidden:true,
            editor:this.reasonCombo
        }*/

        this.addBut = new Wtf.Toolbar.Button({
            text:"Add",
            iconCls:'pwnd addicon'
        });
        
        WtfGlobal.updateStoreConfig(GlobalColumnModel[Wtf.Acc_Stock_Adjustment_ModuleId], this.EditorStore);
        
        this.EditorGrid = new Wtf.grid.EditorGridPanel({
            cm:this.EditorColumn,
            enableHdMenu:false,
            region:"center",
            id:"editorgrid"+this.id,
            autoScroll:true,
            loadMask : true,
            sm:this.sm,
            store:this.EditorStore,
            forceFit:true, 
            viewConfig:{
                forceFit:false, 
                autoFill:true
            },
            border:true,
            clicksToEdit:1
        });
        
        this.EditorGrid.on("afteredit",this.loadProductStore,this);
        this.EditorGrid.on("cellclick",this.cellClick,this);
        this.EditorGrid.on('rowclick',this.handleRowClick,this);
        this.EditorGrid.on('populateDimensionValue',this.populateDimensionValueingrid,this);
        if(this.isView){
            this.fillGridValue();            
            this.EditorStore.commitChanges();
        }
    //  this.EditorGrid.getColumnModel().setHidden(6,true);
    },
    getComboRenderer : function(combo){
        return function(value) {
            var idx = combo.store.find(combo.valueField, value,0,false);
            if(idx == -1){
                idx = combo.store.findBy(function(r,id){
                    if(r.get(combo.displayField) == value){
                        return true;
                    }
                }, this,0);
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
    setlinelevelcustomfields:function(){
        var custArr = GlobalColumnModel[Wtf.Acc_Stock_Adjustment_ModuleId];
            for (var custCount = 0; custCount < custArr.length; custCount++) {
                if (this.viewRec.data[custArr[custCount].fieldname] != undefined) {
                    this.EditorStore.getAt(0).set(custArr[custCount].fieldname, this.viewRec.json[custArr[custCount].fieldname+"_Value"]);                    
                }else{
                    this.EditorStore.getAt(0).set(custArr[custCount].fieldname,"");
                }
            }
    },
    addProductList:function(){
        return "<div class='pwnd add-gridrow' wtf:qtip=\"Click to add products\"></div>";
    },
    showProductGrid : function() {//ERP-8199 :
        var adjReasonRecord = this.parent.reasonStore.getAt(this.parent.reasonStore.find('id',this.parent.adjReasonCombo.getValue()));
        this.productSelWin = new Wtf.account.ProductSelectionWindow({
            renderTo: document.body,
            height : 600,
            width : 700,
            title:'Product Selection Window',
            layout : 'fit',
            modal : true,
            resizable : true,
            id:this.id+'ProductSelectionWindow',
            moduleid:Wtf.Acc_Stock_Adjustment_ModuleId,
            modulename:"STOCK_ADJUSTMENT",
            isJobWorkInReciever:this.isJobWorkInReciever,
            invoiceGrid:this.EditorGrid,
            parentCmpID:this,
            isFromInventorySide:true,
            isCustomer : false,
            isStoreLocationEnable:true,
            warehouseId:this.parent.fromstoreCombo.getValue(),
            isWastageApplicable : (adjReasonRecord && adjReasonRecord != null && adjReasonRecord.data.defaultMasterItem == Wtf.WASTAGE_ID) ? true : false
        });
        this.productSelWin.show();
    },
    getCurrencyFormatWithoutSymbol: function(value, precisions) {
        var precisions=parseInt(precisions);
        if(isNaN(precisions)) precisions = 2;
        var precisionValue = Math.pow(10, precisions);
        var v=parseFloat(value);
        if(isNaN(v)) return "";
        v = (Math.round((v-0)*precisionValue))/precisionValue;
        v = v + this.getAppliedZeros(v, precisions);
        var ps = v.split('.');
        var whole = ps[0];
        var sub = ps[1]
        var r = /(\d+)(\d{3})/;
        while (r.test(whole)) {
            whole = whole.replace(r, '$1' + ',' + '$2');
        }
        if(!sub==""){
            v = whole +"."+ sub;
        }else{
            v=whole;
        }
        if(v.charAt(0) == '-'){
            v= '-'+ v.substr(1);
        }
        
        return v;
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
                    this.ArrangeNumberer(this.EditorGrid,rowIndexToCheck);
                    break;
                }
            }
        }
    },
    addReason:function(){
        addMasterItemWindow('31'); // 31 is for stock adjustment reason
        var panel = Wtf.getCmp("masterconfigurationonly");
        panel.on("update",function(){
            this.reasonStore.reload();
        },this)
         
    },
    ArrangeNumberer: function(grid, currentRow) {                // use currentRow as no. from which you want to change numbering
        var plannerView = grid.getView();                      // get Grid View
        var length = grid.getStore().getCount();              // get store count or no. of records upto which you want to change numberer
        for (var i = currentRow; i < length; i++)
            plannerView.getCell(i, 0).firstChild.innerHTML = i + 1;
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var total=store.getCount();
            if(rowindex==total-1 ){
                if (!this.isJobWorkInReciever) {
                    return;
                } else {
                    if (total == 1) {
                        return;
                    }
                }
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockadjustment.Areyousureyouwanttoremovethisitem"), function(btn){
                if(btn!="yes"){
                    return;
                }
                
                var rec = store.getAt(rowindex);
                var stockDetail = rec.get("stockDetails");
                var productId = rec.get("productid");
                if (stockDetail != "" && stockDetail != null && stockDetail != undefined) {
                    for (var x = 0; x < stockDetail.length; x++) {
                        var detailRec = stockDetail[x];
                        var batchName = detailRec.batchName;
                        var serailNames = detailRec.serialNames;
                        var qty = detailRec.quantity;
                        if (batchName != "" && batchName != undefined && Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch) {   //if product delete then delete batch also from temp holder
                            var curRec ="#" + batchName;
                            var index = Wtf.stockAdjustmentTempDataHolder.indexOf(curRec);
                            if (index != -1) {
                                Wtf.stockAdjustmentTempDataHolder.splice(index, 1);
                            }

                        }
                        if (serailNames != "" && serailNames != undefined) {
                            var serialsArr = serailNames.split(",");
                            for (var z = 0; z < serialsArr.length; z++) {
                                var curRec = productId + "#" + batchName + "#" + serialsArr[z];
                                var index = Wtf.stockAdjustmentTempDataHolder.indexOf(curRec);
                                if (index != -1) {
                                    Wtf.stockAdjustmentTempDataHolder.splice(index, 1);
                                }
                            }
                        }else {
                            if (Wtf.stockAdjustmentProdBatchQtyMapArr.length > 0) {
                                for (var s = 0; s < Wtf.stockAdjustmentProdBatchQtyMapArr.length; s++) {
                                    var recObj = Wtf.stockAdjustmentProdBatchQtyMapArr[s];
                                    if (recObj != "" && recObj != undefined) {
                                        if (recObj.productId == productId && recObj.fromLocationId == detailRec.locationId && recObj.batchName == batchName) {
                                            var arrQty = recObj.quantity;
                                            var filledQty = qty;
                                            recObj.quantity = Number(arrQty) - filledQty;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                store.remove(rec);
                this.ArrangeNumberer(grid, rowindex);
            //                this.fireEvent('datachanged',this);
            }, this);
        }else if (e.getTarget(".add-gridrow")) {//ERP-12415[SJ] 
            this.showProductGrid();
        }
        
        if (e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRow(grid, 0);
        }
        if (e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRow(grid, 1);
        }
    },
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(Wtf.Acc_Stock_Adjustment_ModuleId, rec, this.EditorGrid);
    },
    enableDisableButtons:function(){
        

        if(this.sm.getCount() >=1 ){
            this.parent.deleteBtn.enable();
        } else {
            this.parent.deleteBtn.disable(); 
        }
    },
    
    loadProductStore: function (e) {
            if (e.record.data != undefined && e.record.data != null){
                var rec = e.record.data;
                if (rec.pid != undefined && rec.pid != null && rec.pid != "") {
                    var productidarray = [];
                    productidarray.push(rec.pid);
                    this.itemEditorStore.load({
                        params:{
                            ids : productidarray
                        },
                        callback: this.fillGridValue.createDelegate(this, [e])
                    });
                }
            }
    },
        
    fillGridValue:function (e){
        var alreadyAdded = false;
        var i;
        if(this.isView && this.tabtype != 2){ //Stock Adjusment Detail
            
            this.EditorStore.getAt(0).set("productid",this.viewRec.data.productId);
            this.EditorStore.getAt(0).set("pid",this.viewRec.data.productCode); 
            this.EditorStore.getAt(0).set("quantity",this.viewRec.data.quantity);
            this.EditorStore.getAt(0).set("productname",this.viewRec.data.productName);
            this.EditorStore.getAt(0).set("desc",this.viewRec.data.productDescription);
            //                this.EditorStore.getAt(e.row).set("packaging",this.itemEditorStore.getAt(i).get("packaging"));
            this.EditorStore.getAt(0).set("uomid",this.viewRec.data.uomId);
            this.EditorStore.getAt(0).set("uomname",this.viewRec.data.uomName);
            //this.EditorStore.getAt(e.row).set("purchaseprice",this.itemEditorStore.getAt(i).get("purchaseprice"));
            this.EditorStore.getAt(0).set("amount",this.viewRec.data.cost);
            this.EditorStore.getAt(0).set("isBatchForProduct",this.viewRec.data.isBatchForProduct);
            this.EditorStore.getAt(0).set("isSerialForProduct",this.viewRec.data.isSerialForProduct);
            this.EditorStore.getAt(0).set("isSKUForProduct",this.viewRec.data.isSKUForProduct);
            this.EditorStore.getAt(0).set("isRowForProduct",this.viewRec.data.isRowForProduct);
            this.EditorStore.getAt(0).set("isRackForProduct",this.viewRec.data.isRackForProduct);
            this.EditorStore.getAt(0).set("isBinForProduct",this.viewRec.data.isBinForProduct);
            this.EditorStore.getAt(0).set("stockDetails",this.viewRec.data.stockDetails);
            //this.EditorStore.getAt(0).set("reason",this.viewRec.data.reasonid);
            this.EditorStore.getAt(0).set("remark",this.viewRec.data.remark.trim());
            this.EditorStore.getAt(0).set("adjustmentType",this.viewRec.data.adjustmentType);
            this.EditorStore.getAt(0).set("cost",this.viewRec.data.cost);
            this.EditorStore.getAt(0).set("costcenter",this.viewRec.data.costcenter);
            this.EditorStore.getAt(0).set("reason",this.viewRec.data.reason);
            this.setlinelevelcustomfields();  // Below commented code is move to common function setlinelevelcustomfields
//            var custArr = GlobalColumnModel[Wtf.Acc_Stock_Adjustment_ModuleId];
//            for (var custCount = 0; custCount < custArr.length; custCount++) {
//                if (this.viewRec.data[custArr[custCount].fieldname] != undefined) {
//                    this.EditorStore.getAt(0).set(custArr[custCount].fieldname, this.viewRec.json[custArr[custCount].fieldname+"_Value"]);
//                }else{
//                    this.EditorStore.getAt(0).set(custArr[custCount].fieldname,"");
//                }
//            }
        }else if(this.isView && this.tabtype==2){ // Stock Adjustment Summary
            
            this.expandRec = Wtf.data.Record.create ([
            {
                "name":"id"
            },
            {
                "name":"productCode"
            },
            {
                "name":"productid"
            },

            {
                "name":"productName"
            },
            {
                "name":"productDescription"
            },

            {
                "name":"quantity"
            },
            {
                "name":"packaging"
            },

            {
                "name":"uomName"
            },
        
            {
                "name":"markouttype"
            },

            {
                "name":"reason"
            },

            {
                "name":"costcenter"
            },

            {
                "name":"cost"
            },

            {
                "name":"amount"
            },
        
            {
                "name":"type"
            },
        
            {
                "name":"adjustmentType"
            },
            {
                "name":"remark"
            },

            {
                "name":"partnumber"
            },

            {
                "name":"seqNumber"
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
                "name":"reasonid"
            },
            {
                "name":"stockDetails"     //getting stock details during view mode
            }
            ]);
            this.expandStoreUrl = 'INVStockAdjustment/getStockAdjustmentRows.do';
    
            this.expandStore = new Wtf.data.Store({
                url:this.expandStoreUrl,
                baseParams:{
                    mode:14,
                    dtype : 'report', // Display type report/transaction, used for quotation
                    isNormalContract:this.isNormalContract,
                    transactionno:this.viewRec.data.seqNumber
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.expandRec)
            });
            
            this.expandStore.on('load',this.copyStore,this);
            WtfGlobal.updateStoreConfig(GlobalColumnModel[Wtf.Acc_Stock_Adjustment_ModuleId], this.expandStore);
            this.expandStore.load();
        //            this.EditorStore.proxy.conn.url = 'INVStockAdjustment/getStockAdjustmentRows.do';
        //            this.EditorStore.load({
        //                params: {
        //                        mode:14,
        //                        dtype : 'report', // Display type report/transaction, used for quotation
        //                        isNormalContract:this.isNormalContract,
        //                        transactionno:this.viewRec.data.seqNumber
        //                } 
        //            });
            
        }else{
            for(i=0;i<this.itemEditorStore.getCount();i++){
                if(this.itemEditorStore.getAt(i).get("productid") == e.value){
                    this.EditorStore.getAt(e.row).set("productid",e.value);
                    this.EditorStore.getAt(e.row).set("pid",this.itemEditorStore.getAt(i).get("pid")); 
                    //this.EditorStore.getAt(e.row).set("productid",this.itemEditorStore.getAt(i).get("productid"));
                    //this.EditorStore.getAt(e.row).set("quantity",this.itemEditorStore.getAt(i).get("quantity"));
                    this.EditorStore.getAt(e.row).set("quantity",0);
                    this.EditorStore.getAt(e.row).set("productname",this.itemEditorStore.getAt(i).get("productname"));
                    this.EditorStore.getAt(e.row).set("avlqty",this.itemEditorStore.getAt(i).get("quantity"));
                    this.EditorStore.getAt(e.row).set("desc",this.itemEditorStore.getAt(i).get("desc"));
                    this.EditorStore.getAt(e.row).set("packaging",this.itemEditorStore.getAt(i).get("packaging"));
                    this.EditorStore.getAt(e.row).set("uomid",this.itemEditorStore.getAt(i).get("uomid"));
                    this.EditorStore.getAt(e.row).set("uomname",this.itemEditorStore.getAt(i).get("uomname"));
                    this.EditorStore.getAt(e.row).set("partnumber",this.itemEditorStore.getAt(i).get("partnumber"));
                    var amountwithprecision=0//WtfGlobal.getCurrencyFormatWithoutSymbol(this.itemEditorStore.getAt(i).get("amount"),Wtf.companyPref.priceDecimalPrecision)
                    this.EditorStore.getAt(e.row).set("purchaseprice",this.itemEditorStore.getAt(i).get("purchaseprice"));
                    this.EditorStore.getAt(e.row).set("amount",this.itemEditorStore.getAt(i).get("purchaseprice"));
                    this.EditorStore.getAt(e.row).set("isBatchForProduct",this.itemEditorStore.getAt(i).get("isBatchForProduct"));
                    this.EditorStore.getAt(e.row).set("isSerialForProduct",this.itemEditorStore.getAt(i).get("isSerialForProduct"));
                    this.EditorStore.getAt(e.row).set("isSKUForProduct",this.itemEditorStore.getAt(i).get("isSKUForProduct"));
                    this.EditorStore.getAt(e.row).set("isRowForProduct",this.itemEditorStore.getAt(i).get("isRowForProduct"));
                    this.EditorStore.getAt(e.row).set("isRackForProduct",this.itemEditorStore.getAt(i).get("isRackForProduct"));
                    this.EditorStore.getAt(e.row).set("isBinForProduct",this.itemEditorStore.getAt(i).get("isBinForProduct"));
                    this.EditorStore.getAt(e.row).set("stockDetails",this.itemEditorStore.getAt(i).get("stockDetails"));
                    if(this.EditorStore.getAt(e.row).get("reason") == "" && Wtf.getCmp('stockadjustmentreasoncomboIdglobal')){
                        this.EditorStore.getAt(e.row).set("reason",Wtf.getCmp('stockadjustmentreasoncomboIdglobal').getValue());
                    }
                
                    //this.EditorStore.getAt(e.row).set("costcenter",this.itemEditorStore.getAt(i).get("costcenter")); ????
                    for(var k=0;k<this.EditorStore.getCount();k++){
                        if(this.EditorStore.getAt(k).get("reason") == "" && Wtf.getCmp('stockadjustmentreasoncomboIdglobal')){
                            this.EditorStore.getAt(k).set("reason",Wtf.getCmp('stockadjustmentreasoncomboIdglobal').getValue());
                        }
                    }
                    if(this.itemEditorStore.getAt(i).get("uomname") == ""){
                        this.EditorGrid.getView().getRow(e.row).style = "background-color :pink";
                    }
                    if(!(this.itemEditorStore.getAt(i).get("isBatchForProduct")&&this.itemEditorStore.getAt(i).get("isSerialForProduct"))){
                        this.getDefaultLocationQty(e,e.value);
                    }
                }
            }
            var fieldName = e.field; 
            
            if(e.column !=undefined){
                fieldName = e.grid.getColumnModel().getDataIndex(e.column); 
            }
            
            var record = e.grid.getStore().getAt(e.row);  // Get the Record
            if(record.data.productCode){
                record.set("pid",record.data.productCode);
            }
            if(fieldName=="quantity"){
                this.getDefaultLocationQty(e,record.get("productid"));
                
                /*
                 * Fetching Store combo
                 */
                var fromstorecombo = this.parent.fromstoreCombo;
                var fromstore;
                /*
                 * If Present
                 */
                if (fromstorecombo) {
                    fromstore = fromstorecombo.store;
                    /*
                     * Fetching Store and default Location
                     */                
                    var idx = fromstore.find("store_id",fromstorecombo.getValue());
                    var rec = fromstore.getAt(idx);
                    this.defaultLocationId=rec.get("defaultlocationid");
                    this.defaultLocationName=rec.get("defaultlocation");
                    
                    if(this.parent.challanNo && this.parent.challanNo.getValue()){
                        /*
                         * creating stock details Array
                         */
                        var detailArray = new Array();
                        var data = {};
                        data.locationId = this.defaultLocationId;
                        data.quantity = this.EditorStore.getAt(e.row).get("quantity");
                        data.batchQuantity = this.EditorStore.getAt(e.row).get("quantity");
                        data.rowId = "";
                        data.rackId = "";
                        data.binId = "";
                        data.batchName = this.parent.challanNo.getValue();
                        data.serialNames = "";
                        detailArray.push(data);
                        
                        /*
                         * setting details array in grid.
                         */
                        this.EditorStore.getAt(e.row).set("stockDetails", "");
                        this.EditorStore.getAt(e.row).set("stockDetails", detailArray);
                        this.EditorStore.getAt(e.row).set("stockDetailQuantity", "");
                        this.EditorStore.getAt(e.row).set("stockDetailQuantity", this.EditorStore.getAt(e.row).get("quantity"));
                    
            }
                }else{ // for normal stock Adjustment
//                    var stockDetails = this.EditorStore.getAt(e.row).get("stockDetails");
                    var adjustmentType = this.EditorStore.getAt(e.row).get("adjustmentType");
                    var quantity = this.EditorStore.getAt(e.row).get("quantity");
                    var defaultlocqty = this.EditorStore.getAt(e.row).get("defaultlocqty");
                    var deflocation = this.EditorStore.getAt(e.row).get("deflocation");
                    var isBatchForProduct = this.EditorStore.getAt(e.row).get("isBatchForProduct");
                    var isSerialForProduct = this.EditorStore.getAt(e.row).get("isSerialForProduct");
                    var isBinForProduct = this.EditorStore.getAt(e.row).get("isBinForProduct");
                    var isRackForProduct = this.EditorStore.getAt(e.row).get("isRackForProduct");
                    var isRowForProduct = this.EditorStore.getAt(e.row).get("isRowForProduct");
                    var isRRBBSAnyEnabled = (isSerialForProduct || isBatchForProduct || isRowForProduct || isRackForProduct || isBinForProduct);
                    if (!isRRBBSAnyEnabled && quantity > 0 && defaultlocqty > 0 && deflocation != undefined && deflocation != "") {
                        var isQtySufficient = (adjustmentType === "Stock IN" ? true : quantity <= defaultlocqty);
                        if (isQtySufficient) {
                            var detailArray = new Array();
                            var data = {};
                            data.locationId = deflocation;
                            data.quantity = quantity;
                            data.rowId = "";
                            data.rackId = "";
                            data.binId = "";
                            data.batchName = "";
                            data.serialNames = "";
                            detailArray.push(data);

                            this.EditorStore.getAt(e.row).set("stockDetails", "");
                            this.EditorStore.getAt(e.row).set("stockDetails", detailArray);
                            this.EditorStore.getAt(e.row).set("stockDetailQuantity", "");
                            this.EditorStore.getAt(e.row).set("stockDetailQuantity", quantity);
                        }
                    }
                }
            }
            if(fieldName=="quantity"||fieldName=="amount"||fieldName=="adjustmentType"){
                var cnt=this.EditorStore.getCount();
                var price=0;
                var qty=0;
                var totalamount=0;
                for(var i=0;i<cnt-1;i++){
                    if(this.EditorStore.getAt(i).get("adjustmentType")=="Stock IN"){
                        price=this.EditorStore.getAt(i).get("amount")!=undefined?this.EditorStore.getAt(i).get("amount"):0;
                        qty=this.EditorStore.getAt(i).get("quantity")!=undefined?this.EditorStore.getAt(i).get("quantity"):0;
                        totalamount+=(price*qty);
                    }
//                    var avlQty= this.EditorStore.getAt(i).get("avlqty")!=undefined?this.EditorStore.getAt(i).get("avlqty"):0;
//                    var entereQty= this.EditorStore.getAt(i).get("quantity")!=undefined?this.EditorStore.getAt(i).get("quantity"):0;
//                    if (avlQty!=undefined && avlQty<entereQty&&this.EditorStore.getAt(i).get("adjustmentType")!="Stock IN") {
//                        e.grid.getView().getRow(i).style = "background-color :pink";
//                }
            }
                 this.parent.tplSummary.overwrite(this.parent.southSummaryPanel.body,{total:isNaN(totalamount) ? "0.00" :parseFloat(getRoundofValue(totalamount)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)}); //ERP-38867
                }
            if(e.row == this.EditorStore.getCount()-1){
                if (!this.isJobWorkInReciever) {
                    this.addRec();
                }
            }
            var adjustmentType= record.get("adjustmentType");
            if(fieldName=="adjustmentType"){
                if(adjustmentType == "Stock Out" ||adjustmentType == "Stock Sales"){
                    this.EditorStore.getAt(e.row).set("amount","-");  
                }
//                else{
//                    if(this.itemEditorStore.getAt(e.row).get("purchaseprice")!=undefined&&this.itemEditorStore.getAt(e.row).get("purchaseprice")!=""){
//                        var prodId=this.EditorStore.getAt(e.row).get("productid");
//                        var idx1 = this.itemEditorStore.find("productid",prodId);
////                        this.EditorStore.getAt(e.row).set("amount",this.itemEditorStore.getAt(idx1).get("purchaseprice"));
//                    }
//                    
//                }
               
            }
            
            if((fieldName=="adjustmentType"&&adjustmentType == "Stock IN")||((fieldName=="pid")&&adjustmentType == "Stock IN")){
                this.getperunitPrice(e,this.EditorStore.getAt(e.row).get("productid"),this.parent.dateField.getRawValue());
            }
            if(fieldName=="quantity"||fieldName=="amount"||fieldName=="adjustmentType"){
                for(var i=0;i<cnt-1;i++){
                    var avlQty= this.EditorStore.getAt(i).get("avlqty")!=undefined?this.EditorStore.getAt(i).get("avlqty"):0;
                    var entereQty= this.EditorStore.getAt(i).get("quantity")!=undefined?this.EditorStore.getAt(i).get("quantity"):0;
                    if (avlQty<entereQty&&this.EditorStore.getAt(i).get("adjustmentType")!="Stock IN"&&this.EditorStore.getAt(i).get("adjustmentType")!=undefined&&this.EditorStore.getAt(i).get("adjustmentType")!="") {
                        e.grid.getView().getRow(i).style = "background-color :pink";
                    }
                }
            }
        }
    //        for(i=0;i<this.EditorStore.getCount();i++){
    //            if(i != e.row && this.EditorStore.getAt(i).get("productid") == e.value && this.EditorStore.getAt(i).get("productid") !=''){
    //                alreadyAdded = true;
    //                this.EditorStore.getAt(e.row).reject();
    //                break;
    //            }
    //        }
    //        if(alreadyAdded){
    //            WtfComMsgBox(["Failure", "Product is already added in grid."],3);
    //        }else 
    }, 
  
    copyStore: function() {
        if(this.expandStore.getCount() > 0) {
            this.EditorStore.removeAll();
            var total_amount=0;
            for(var i=0;i<this.expandStore.getCount();i++){
            total_amount +=Math.abs(this.expandStore.getAt(i).get("amount"));
           
                var record = this.EditorStore.reader.recordType, f = record.prototype.fields, fi = f.items, fl = f.length;
                var blankObj = {};
                for (var j = 0; j < fl; j++) {
                    f = fi[j];
                    if (f.name != 'id') {
                        blankObj[f.name] = '';
                    }
                }

                var rec = new this.EditorRec(blankObj);
                rec.beginEdit();
                rec.set("productid",this.expandStore.getAt(i).get("id"));
                rec.set("pid",this.expandStore.getAt(i).get("productCode")); 
                rec.set("quantity",this.expandStore.getAt(i).get("quantity"));
                rec.set("productname",this.expandStore.getAt(i).get("productName"));
                rec.set("desc",this.expandStore.getAt(i).get("productDescription"));
                rec.set("packaging",this.expandStore.getAt(i).get("packaging"));
                rec.set("uomname",this.expandStore.getAt(i).get("uomName"));
                rec.set("partnumber",this.expandStore.getAt(i).get("partnumber"));
                rec.set("purchaseprice",this.expandStore.getAt(i).get("purchaseprice"));
                rec.set("amount",this.expandStore.getAt(i).get("amount"));
                rec.set("cost",this.expandStore.getAt(i).get("cost"));
                rec.set("isBatchForProduct",this.expandStore.getAt(i).get("isBatchForProduct"));
                rec.set("isSerialForProduct",this.expandStore.getAt(i).get("isSerialForProduct"));
                rec.set("isSKUForProduct",this.expandStore.getAt(i).get("isSKUForProduct"));
                rec.set("isRowForProduct",this.expandStore.getAt(i).get("isRowForProduct"));
                rec.set("isRackForProduct",this.expandStore.getAt(i).get("isRackForProduct"));
                rec.set("isBinForProduct",this.expandStore.getAt(i).get("isBinForProduct"));
                rec.set("stockDetails",this.expandStore.getAt(i).get("stockDetails"));
                rec.set("reason",this.expandStore.getAt(i).get("reasonid"));
                rec.set("adjustmentType",this.expandStore.getAt(i).get("adjustmentType"));
                rec.set("remark",this.expandStore.getAt(i).get("remark"));
                rec.set("costcenter",this.expandStore.getAt(i).get("costcenter"));
                var custArr = GlobalColumnModel[Wtf.Acc_Stock_Adjustment_ModuleId];
                for (var custCount = 0; custCount < custArr.length; custCount++) {
                    if (this.expandStore.getAt(i).get(custArr[custCount].fieldname) != undefined) {
                        rec.set(custArr[custCount].fieldname, this.expandStore.getAt(i).get(custArr[custCount].fieldname));
                    }else{
                        rec.set(custArr[custCount].fieldname, "");    
                    }
                }
                rec.endEdit();
                rec.commit();
                this.EditorStore.add(rec);
            }
            
            this.parent.tplSummary.overwrite(this.parent.southSummaryPanel.body,{  //overwriting to show correct amount
                total: isNaN(total_amount) ? "0.00" : parseFloat(getRoundofValue(total_amount)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL) //ERP-38867
            });
            this.EditorGrid.getView().refresh();
        }
    },   
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    quantityRenderer:function(val,m,rec){
        return (this.isView ? Math.abs(val) : val);
    }
    ,
    cellClick :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isSKUEnable = record.get("isSKUForProduct");
        var isRowEnable = record.get("isRowForProduct");
        var isRackEnable = record.get("isRackForProduct");
        var isBinEnable = record.get("isBinForProduct");
        var itemId=record.get("productid");
        var itemCode=record.get("pid");
        var quantity=record.get("quantity");
        var UOMName=record.get("uomname");
        var adjustmentType=record.get("adjustmentType");
        var enteredQuantity=0;
        
        var fromStoreId = this.parent.fromstoreCombo.getValue();
        
        if((fieldName == "amount" )){
            if(adjustmentType == undefined || adjustmentType == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.JE.PleaseSelectAdjustmentType")],0);
                return false;
            }else{
                if(adjustmentType == "Stock Out" || adjustmentType == "Stock Sales"){
                    return false;
                }
            }  
        }
        if(fieldName == undefined || fieldName == "" && (fieldName != "plusbtn" && fieldName != "seqno")){
            if(fromStoreId == undefined || fromStoreId == ""){
                WtfComMsgBox(["Warning", "Please Select Store."],0);
                return false;
            }
            if(itemId=="" || itemId==undefined){
                if(!this.isView){
                    WtfComMsgBox(["Warning", "Please Select Product first."],0);
                    return false; 
                }
            }
            if(record.data.quantity==0){
                WtfComMsgBox(["Warning", "Please Fill quantity."],0);
                return false;
            }
            if(adjustmentType == undefined || adjustmentType == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.JE.PleaseSelectAdjustmentType")],0);
                return false;
            }else{
                if(adjustmentType == "Stock IN"){
                    enteredQuantity = quantity;
                }else if(adjustmentType == "Stock Out" || adjustmentType == "Stock Sales" || adjustmentType == "Stock OUT" ){
                    enteredQuantity = -(quantity);
                }
            }
            if(this.isView){
                this.addLocationBatchSerialWindow(itemId,itemCode,isBatchEnable,isSerialEnable,isRowEnable, isRackEnable, isBinEnable, enteredQuantity,UOMName,rowIndex,record,isSKUEnable,grid);
            }else{
                // if(record.data.quantity > 0){ // if +ve qty then add new batch and serials
                if(enteredQuantity > 0){ // if +ve qty then add new batch and serials
                    this.addLocationBatchSerialWindow(itemId,itemCode,isBatchEnable,isSerialEnable,isRowEnable, isRackEnable, isBinEnable, enteredQuantity,UOMName,rowIndex,record,isSKUEnable,grid);
                }
                //            if(record.data.quantity < 0){ // if -ve qty then select existing batch and serials
                if(enteredQuantity < 0){ // if -ve qty then select existing batch and serials
                    //                this.showLocationBatchSerialSelectWindow(itemId,itemCode,isBatchEnable,isSerialEnable,enteredQuantity,UOMName,rowIndex, record);
                    this.showStockDetailWindow(record);
                }
            }
        }
    },
    getDefaultLocationQty : function(e,pid){
         
        Wtf.Ajax.requestEx({
            url:"INVStockLevel/getStoreProductWiseDetailList.do",
            params: {
                storeId: this.parent.fromstoreCombo.getValue(),
                productId: pid,
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
    getperunitPrice : function(e,pid,bssDate){ 
      WtfGlobal.setAjaxTimeOut(); 
        Wtf.Ajax.requestEx({
            url:"INVGoodsTransfer/getPurchasePriceForSA.do",
            params: {
                storeId: this.parent.fromstoreCombo.getValue(),
                productId: pid,
                defaultloc:true,
                bssDate:bssDate
            }
        },this,
        function(res,action){
          WtfGlobal.resetAjaxTimeOut();
            if(res.success==true){
//                var result=eval('('+res.data+')');
//                var purchaseprice=result.data[0];
                this.EditorStore.getAt(e.row).set("amount",res.purchaseprice);
            }else{
                WtfComMsgBox(["Error", "Error occurred while fetching data."],0);
                return;
            }
        },
        function() {
           WtfGlobal.resetAjaxTimeOut();
            WtfComMsgBox(["Error", "Error occurred while processing"],1);
        }
        ); 
    },
    
   
     
    addLocationBatchSerialWindow : function (itemId,itemCode,isBatchForProduct,isSerialForProduct,isRowForProduct,isRackForProduct,isBinForProduct, quantity,UOMName,rowIndex,record,isSKUForProduct,grid){
       
        this.fromStoreId = this.parent.fromstoreCombo.getValue();
        this.fromStoreName=this.parent.fromstoreCombo.getRawValue();
        this.itemId=itemId;
        this.itemcode=itemCode;
        this.itemName=record.get("productname");
        this.isBatchForProduct=isBatchForProduct;
        this.isSerialForProduct=isSerialForProduct;
        this.isSKUForProduct=isSKUForProduct;
        this.isRowForProduct=isRowForProduct;
        this.isRackForProduct=isRackForProduct;
        this.isBinForProduct=isBinForProduct;
        this.quantity=quantity;
        this.currentRowNo=rowIndex;
        this.UOMName=UOMName;
        this.realquantity=quantity;
        this.defaultLocationId="";
        this.defaultLocationName="";
        this.StockDetailArray=record.get("stockDetails");
        var batchName = "";
        this.parentStore=grid.getStore();
//        var record = grid.getStore().getAt(rowIndex); 
          var count = 0;
        // Setting Batch name in Record
        
        if (this.StockDetailArray && this.StockDetailArray[0] && this.StockDetailArray[0].batchName) {
            batchName = this.StockDetailArray[0].batchName;
        }
        
        var idx = this.parent.fromStore.find("store_id",this.fromStoreId);
        if(idx != -1){
            var rec = this.parent.fromStore.getAt(idx);
            this.defaultLocationId=rec.get("defaultlocationid");
            this.defaultLocationName=rec.get("defaultlocation");
        }
        
        this.loadedCombo = [];
        this.locCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },        

        {
            name: 'name'
        },
        {
            name:'isdefault'
        }
        ]);

        this.locCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            baseParams:{isActive:true},   //ERP-40021 :To get only active Locations.
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
        
        this.locCmbStore.load({
            params:{
                storeid:this.fromStoreId
            }
        });
        this.locCmbStore.on("load",function(){
            this.loadedCombo.push("location");
            count++;
            if(this.loadedCombo.length == count){
                this.fillDetails();
            }
        },this);
        //         }
        //        this.locCmbStore.on("load",function(){
        //            var idx = this.locCmbStore.find("isdefault",true);
        //            if(idx != -1){
        //                var rec = this.locCmbStore.getAt(idx);
        //                this.defaultLocationId=rec.get("id");
        //                this.defaultLocationName=rec.get("name");
        //            }
        //        },this);
            
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
            disabled:this.isView?true:false,
            emptyText:'Select location...'
        });
       
        this.rowCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'name'
        },{
            name: 'parentid'
        }
        ]);
        
        this.rowCmbStore = new Wtf.data.Store({
            url:  'ACCMaster/getStoreMasters.do',
            baseParams:{
                transType:'row'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.rowCmbRecord)
        });
        
        this.rowCmbStore.on("load",function(){
            this.loadedCombo.push("row");
            count++;
            if(this.loadedCombo.length == count){
                this.fillDetails();
            }
        },this);
        if(isRowForProduct){
            this.rowCmbStore.load();            
        }
        
        this.rowCmb = new Wtf.form.ComboBox({
            store : this.rowCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            disabled:this.isView?true:false,
            emptyText:'Select Row...'
        });
        
        this.rackCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'name'
        },{
            name: 'parentid'
        }
        ]);

        this.rackCmbStore = new Wtf.data.Store({
            url:  'ACCMaster/getStoreMasters.do',
            baseParams:{
                transType:'rack'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.rackCmbRecord)
        });
        
        this.rackCmbStore.on("load",function(){
            this.loadedCombo.push("rack");
            count++;
            if(this.loadedCombo.length == count){
                this.fillDetails();
            }
        },this);
        if(isRackForProduct){
            this.rackCmbStore.load();            
        }
        
        this.rackCmb = new Wtf.form.ComboBox({
            store : this.rackCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            disabled:this.isView?true:false,
            emptyText:'Select rack...'
        });
        this.binCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },{
            name: 'name'
        },{
            name: 'parentid'
        }
        ]);

        this.binCmbStore = new Wtf.data.Store({
            url:  'ACCMaster/getStoreMasters.do',
            baseParams:{
                transType:'bin'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.binCmbRecord)
        });
        
        this.binCmbStore.on("load",function(){
            this.loadedCombo.push("bin");
            count++;
            if(this.loadedCombo.length == count){
                this.fillDetails();
            }
        },this);
        if(isBinForProduct){
            this.binCmbStore.load();            
        }
        
        this.binCmb = new Wtf.form.ComboBox({
            store : this.binCmbStore,
            typeAhead:true,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            disabled:this.isView?true:false,
            emptyText:'Select bin...'
        });
        
        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            disabled:this.isView?true:false,
            decimalPrecision:4,//Wtf.companyPref.quantityDecimalPrecision,
            allowNegative:false
        })
        this.batchQtyEditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            disabled:this.isView?true:false,
            decimalPrecision:4,//Wtf.companyPref.quantityDecimalPrecision,
            allowNegative:false
        })
        
        this.batchEditor = new Wtf.form.TextField({
            //allowBlank : false,
            width:200,
            disabled:this.isView?true:false
        });
        
        this.serialEditor = new Wtf.form.TextField({
            //allowBlank : false,
            width:200,
            disabled:this.isView?true:false
        });
        var defaultWidth = 150;
        this.EditorColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.add"),
                dataIndex:"locationid",
                width: defaultWidth,
                editor:this.locCmb,
                renderer:this.getComboRenderer(this.locCmb)
            },{
                header:WtfGlobal.getLocaleText("acc.inv.loclevel.3"),
                dataIndex:"rowId",
                width: defaultWidth,
                editor:this.rowCmb,
                renderer: this.getComboRenderer(this.rowCmb),
                hidden :!this.isRowForProduct
            },{
                header:WtfGlobal.getLocaleText("acc.inv.loclevel.4"),
                dataIndex:"rackId",
                width: defaultWidth,
                editor:this.rackCmb,
                renderer: this.getComboRenderer(this.rackCmb),
                hidden :!this.isRackForProduct
            },{
                header:WtfGlobal.getLocaleText("acc.inv.loclevel.5"),
                dataIndex:"binId",
                width: defaultWidth,
                editor:this.binCmb,
                renderer: this.getComboRenderer(this.binCmb),
                hidden : !this.isBinForProduct
            },
            {
                header:(Wtf.jobWorkInFlowFlag!= undefined && Wtf.jobWorkInFlowFlag==true && Wtf.isJobWorkInReciever==true )? "Challan No":WtfGlobal.getLocaleText("acc.inventorysetup.batch"),
                dataIndex:"batch",
                width: defaultWidth,
                editor:this.batchEditor,
                hidden : (this.isBatchForProduct==true) ? false :true
            },
            {
                header:WtfGlobal.getLocaleText("acc.fixed.asset.quantity"),
                dataIndex:"batchQuantity",
                width: defaultWidth,
                editor:this.batchQtyEditor,
                hidden : (this.isSerialForProduct==true || this.isBatchForProduct ==true) ? false :true
            },
            {
                header:WtfGlobal.getLocaleText("acc.inventorysetup.serial"),
                dataIndex:"serial",
                width: defaultWidth,
                editor:this.serialEditor,
                hidden : (this.isSerialForProduct==true) ? false :true
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.MfgDate"),
                dataIndex:"mfgdate",
                width: defaultWidth,
                renderer: function(v){
                    var date = v;
                    if(date != undefined && date != ""){
                        date = date.format("Y-m-d");
                    }
                    return date;
                },
                editor:new Wtf.form.DateField({
                    value:new Date(),
                    format:"Y-m-d"
                }),
                hidden : (this.isSerialForProduct==true) ? false :true
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.ExpDate"),
                dataIndex:"expdate",
                width: defaultWidth,
                renderer: function(v){
                    var date = v;
                    if(date != undefined && date != ""){
                        date = date.format("Y-m-d");
                    }
                    return date;
                },
                editor:new Wtf.form.DateField({
                    value:new Date(),
                    format:"Y-m-d"
                }),
                hidden : (this.isSerialForProduct==true || isBatchForProduct== true) ? false :true
            },
            {
                header:WtfGlobal.getLocaleText("acc.field.WarrExp.FromDate"),
                dataIndex:"warrantyexpfromdate",
                width: defaultWidth,
                renderer: function(v){
                    var date = v;
                    if(date != undefined && date != ""){
                        date = date.format("Y-m-d");
                    }
                    return date;
                },
                editor:new Wtf.form.DateField({
                    value:new Date(),
                    format:"Y-m-d"
                }),
                hidden : (this.isSerialForProduct==true) ? false :true
            },
            {
                header:WtfGlobal.getLocaleText("acc.stockadjustment.WarrantyExpToDate"),
                dataIndex:"warrantyexptodate",
                width: defaultWidth,
                renderer: function(v){
                    var date = v;
                    if(date != undefined && date != ""){
                        date = date.format("Y-m-d");
                    }
                    return date;
                },
                editor:new Wtf.form.DateField({
                    value:new Date(),
                    format:"Y-m-d"
                }),
                hidden : (this.isSerialForProduct==true) ? false :true
            },
            {
                header:WtfGlobal.getLocaleText("acc.stockadjustment.AdjustedQuantity"),
                dataIndex:"quantity",
                width: defaultWidth,
                hidden: this.isSerialForProduct,
                editor:(this.isBatchForProduct==true || this.isSerialForProduct==true) ? null :this.quantityeditor
            },
            {
                //            header: header:WtfGlobal.getLocaleText("acc.product.sku"),      
                header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
                hidden:!(Wtf.account.companyAccountPref.SKUFieldParm && this.isSKUForProduct),
                dataIndex:'skufield',
                editor:this.readOnly?"":new Wtf.form.TextField({
                    name:'skufield'
                }) 
            },   
            {
                header:WtfGlobal.getLocaleText("mrp.rejecteditems.report.actioncolumn.title"),
                align:'center',
                width: 50,
                dataIndex: "lock",
                renderer: function(v,m,rec){
                    return "<span class='pwnd delete-gridrow'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>";
                }
            }
            ]);
        
        var forceFit = false;
        var totalWidth = this.EditorColumn.getTotalWidth(false);
        if(totalWidth/defaultWidth < 9){
            forceFit = true;
        }    
        this.locationGridStore = new Wtf.data.SimpleStore({
            fields:['itemid','itemcode','locationid','rowId', 'rackId', 'binId','isBatchForProduct','isSerialForProduct',
            'batch','serial','quantity','mfgdate','expdate','warrantyexpfromdate','warrantyexptodate', 'batchQuantity','skufield','islocked','isSKUForProduct'],
            pruneModifiedRecords:true
        });
          
        this.locationGridStore.on('load', function(){
            this.locationSelectionGrid.getView().refresh();
        },this);
        
           
        this.locationSelectionGrid = new Wtf.grid.EditorGridPanel({
            cm:this.EditorColumn,
            region:"center",
            //id:"editorgrid2sd",
            autoScroll:true,
            sm: new Wtf.grid.RowSelectionModel({
                singleSelect: true
            }),
            store:this.locationGridStore,
            viewConfig:{
                forceFit:forceFit,
                emptyText:"No Data to Show."
            },
            clicksToEdit:1
        });
        
        this.locationSelectionGrid.on("beforeedit",this.gridbeforeEdit,this);
        this.locationSelectionGrid.on("afteredit",this.gridafterEdit,this);
        /*
         * Calling Function to Check Duplicate Batch Name 
         */ 
        this.locationSelectionGrid.on("afteredit",this.checkDuplicateBatchlName,this);
        this.locationSelectionGrid.on('rowclick',this.handleRowClick1,this);
        
        if(this.isSerialForProduct==true) {
            for(var j=0;j<this.quantity;j++){
                this.addBlankRow(1);
                this.setDefaultLocationInGrid(j);
                this.locationSelectionGrid.on("afteredit",this.gridafterEdit,this);
            }
        }else{
            this.addBlankRow(this.quantity,batchName);
             this.setDefaultLocationInGrid(0);
        //this.locationSelectionGrid.on("afteredit",this.gridafterEdit,this);
        }
        
        
        this.winTitle=WtfGlobal.getLocaleText("acc.stock.SelectQuantityLocation")+(this.isBatchForProduct ? ","+WtfGlobal.getLocaleText("acc.inventorysetup.batch") : "")+(this.isSerialForProduct ? ","+WtfGlobal.getLocaleText("acc.inventorysetup.serial"): "");
        this.winDescriptionTitle= this.winTitle+" "+WtfGlobal.getLocaleText("acc.stock.forfollowingitem")+"<br/>";
        this.winDescription="<b>"+WtfGlobal.getLocaleText("acc.product.threshold.grid.productID")+" : "+ "</b>"+this.itemcode+"<br/>"
        +"<b>"+WtfGlobal.getLocaleText("acc.field.ProductName")+" : "+ "</b>"+this.itemName+"<br/>"
        +"<b>"+WtfGlobal.getLocaleText("acc.stockadjustment.ForStore")+" :"+ "</b>"+this.fromStoreName+"<br/>"
        +"<b>"+WtfGlobal.getLocaleText("acc.fixed.asset.quantity")+" :"+ "</b>"+ this.quantity +" "+this.UOMName;
       
        
        
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
                height : 100,
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
                        region : 'north',
                        border : false,
                        height : 5
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
                text : WtfGlobal.getLocaleText("acc.het.305"),
                iconCls:'pwnd save',
                scope : this,
                disabled:this.isView?true:false,
                handler: function(){  
                    
                    var isValid=this.validateFilledData();
                    
                    if(isValid==true){
                        var jsonData=this.makeJSONData();
                        var rec=this.EditorStore.getAt(this.currentRowNo);
                        var prevStockDetails=rec.get("stockDetails");
                        if(prevStockDetails != undefined && prevStockDetails != "" ){
                            var productId=this.itemId;
                            for(var c=0; c < prevStockDetails.length; c++){
                                var prevRec=prevStockDetails[c];
                                var batchName=prevRec.batchName;
                                var serialName=prevRec.serialNames;
                                var uniqueRec=productId+"#"+batchName+"#"+serialName;
                                var index=Wtf.stockAdjustmentTempDataHolder.indexOf(uniqueRec);
                                
                                if(index != -1){
                                    Wtf.stockAdjustmentTempDataHolder.splice(index,1);
                                }
                            }
                        }
                        
                        rec.set("stockDetails","");
                        rec.set("stockDetails",jsonData);
                        rec.set("stockDetailQuantity",quantity);
                        
                        var totalRecLength=this.locationSelectionGrid.getStore().data.length;
                        if(totalRecLength > 0){
                            for(var i=0;i< totalRecLength; i++){
                                var rec=this.locationSelectionGrid.getStore().data.items[i];
                                var productId=this.itemId;
                                var batchName=rec.get("batch");
                                if(batchName != undefined &&  batchName !="" && Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch){
                                   Wtf.stockAdjustmentTempDataHolder.push("#"+batchName); 
                                }
                                var serialName=rec.get("serial");
                                if(serialName != undefined &&  serialName !=""){
                                    Wtf.stockAdjustmentTempDataHolder.push(productId+"#"+batchName+"#"+serialName);
                                }
                            }
                        }
                        
                        Wtf.getCmp('locationwindow').close();  
                    }
                }
            },{
                text :  WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope : this,
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                minWidth:75,
                handler : function() {
                    Wtf.getCmp('locationwindow').close();
                }
            }]
        });
        this.locationSelectionWindow.show();
        
        Wtf.getCmp("locationwindow").doLayout();
            
        Wtf.getCmp("locationwindow").on("close",function(){
            //this.showAddToItemMasterWin();
            },this);
            
           
    },
    checkDuplicateChallanNo:function(obj) {
        if (obj!=null) {
            Wtf.Ajax.requestEx({
                    url:"ACCMaster/getNewBatches.do",
                    params:{    
                        location:obj.locationId,
                        warehouse:obj.storeid,
                        row:(obj.rowId)?obj.rowId:"",
                        rack:(obj.rackId)?obj.rackId:"",
                        bin:(obj.binId)?obj.binId:"",
                        checkbatchname:obj.batchName,
                        productid:obj.productid
                
                    }
                },this,function(res,req){
                    if(res.data.length!=0){
                        WtfComMsgBox(112,2);
                        this.parent.challanNo.setValue("");
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.529")], 2);
                        obj.record.set("batch","");
                        return false;
                    } else {
                        return true;
                    }
                    
                },function(res,req){
                    obj.record.set("batch","");
                    this.parent.challanNo.setValue("");
                    return false;
                });
        }
    },
    checkDuplicateBatchlName:function(obj){   //Function to check duplicate Batch Name present in system[mayur p]
        if(obj!=null){
            
            if(obj.field=="batch" && this.isJobWorkInReciever){
                obj.record.set("batch", obj.record.get("batch"));
                var locName = obj.record.get("locationid");
                var locId = "";
                if (locName ) {
                    var locIndex =  this.locCmb.store.find("name",locName);
                    locId = this.locCmb.store.getAt(locIndex).get("id");
                }
                Wtf.Ajax.requestEx({
                    url:"ACCMaster/getNewBatches.do",
                    params:{    
                        location:locId,
                        warehouse:this.fromStoreId?this.fromStoreId:"",
                        row:(this.isRowForProduct)?obj.record.data.rowId:"",
                        rack:(this.isRackForProduct)?obj.record.data.rackId:"",
                        bin:(this.isBinForProduct)?obj.record.data.binId:"",
                        checkbatchname:obj.record.get("batch"),
                        productid:this.itemId
                
                    }
                },this,function(res,req){
                    if(res.data.length!=0){
                        WtfComMsgBox(112,2);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.529")], 2);
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
                 if(this.isEdit && this.isChangeInExistingData(obj)){
                      checkDuplicate=true;
                  }
                obj.record.set("serialno", obj.record.get("serialno").trim());
                if(this.productid != undefined && this.productid != null && this.productid != "" && batchName!="" && obj.record.get("serialno")!="" && checkDuplicate){
                    Wtf.Ajax.requestEx({
                        url:"ACCMaster/getNewSerials.do",
                        params:{    
                            checkbatchname:batchName,
                            checkserialname:obj.record.get("serialno"),
                            productid:this.productid
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
        }
    },
    fillDetails: function(){
        var cnt0=0;
        var tqty=0;
        if(this.StockDetailArray instanceof Array){
            var data = [];
            for(var i=0 ; i< this.StockDetailArray.length ; i++){
                var givenStockDetail = this.StockDetailArray[i];
                if(this.isSerialForProduct){
                    for(var k=0;k<givenStockDetail.quantity;k++){
                        if(this.quantity<=cnt0){
                            break;
                        }
                        var qty=0;
                        if(k==0){
                            qty=givenStockDetail.quantity;
                        }
                        var datarec = [null,null,null,null,null,null,null,null,null,null, 1,null,null,null,null,null,null,null]
                        data.push(datarec);
                        cnt0++;
                    }
                }else{
                    tqty=tqty+givenStockDetail.quantity;
                    var datarec = [null,null,null,null,null,null,null,null,null,null, 1,null,null,null,null,null,null,null]
                    data.push(datarec)
                    cnt0++;
                }
            }
            
            var noOfrows=this.quantity;
            var diff=0;
            diff=noOfrows-cnt0;
            if(this.isSerialForProduct){
                if(noOfrows>cnt0){
                    for(var r=0;r<diff;r++){
                        var datarec = [null,null,null,null,null,null,null,null,null,null, 1,null,null,null,null,null,null,null]
                        data.push(datarec);
                    }
                }
            }else if(diff>0&&this.quantity!=tqty){
                var datarec = [null,null,null,null,null,null,null,null,null,null, 1,null,null,null,null,null,null,null]
                data.push(datarec);
            }
            
            this.locationGridStore.loadData(data);
            this.locationSelectionGrid.getView().refresh();
            var rowQty = 0;
            var cnt=0;
            for(var i1=0 ; i1< this.StockDetailArray.length ; i1++){
                var givenStockDetail = this.StockDetailArray[i1];
           
                if(this.isBatchForProduct&&!this.isSerialForProduct){
                    var longDates =givenStockDetail.expLongTime?givenStockDetail.expLongTime.toString().split(','):"";
                    for(var k1=rowQty;k1<this.StockDetailArray.length;k1++){
                        givenStockDetail = this.StockDetailArray[k1];
                        if(this.quantity<=cnt){
                            break;
                        }
                        var nextRec = this.locationGridStore.getAt(k1);
                        var qty=0;
                        //                        if(k1==rowQty){
                        qty=givenStockDetail.quantity;
                        nextRec.set('batchQuantity', qty);
                        nextRec.set('isLocked', false);
                        //                        }else{
                        //                            nextRec.set('isLocked', true);
                        //                        }
                        nextRec.set('locationid',this.isView ? givenStockDetail.locationName : givenStockDetail.locationId);//location id display in some cases instead of name in batch serial window
                        this.getComboRenderer(this.locCmb);
                        nextRec.set('rowId', givenStockDetail.rowId);
                        nextRec.set('rackId', givenStockDetail.rackId);
                        nextRec.set('binId', givenStockDetail.binId);
                        nextRec.set('batch', givenStockDetail.batchName);
                        //                        nextRec.set('batch', givenStockDetail.batchName);
                        //                        var expdt=this.StockDetailArray.length==1?longDates[k1-rowQty]:givenStockDetail.expLongTime;
                        var expdt=longDates[k1-rowQty];
                        if(expdt!=""&&expdt!=undefined){
                            nextRec.set('expdate', new Date(expdt));
                        }else if(this.isView){
                            longDates =givenStockDetail.expLongTime?givenStockDetail.expLongTime.toString().split(','):"";
                            expdt=longDates[0];
                            if(expdt!=""&&expdt!=undefined)
                                {
                                    nextRec.set('expdate', new Date(expdt));
                                }
                        }
                        nextRec.set('quantity', 1);
                        cnt++;
                    }
                }else if(this.isSerialForProduct){
                    var rono=0;
                    var longDates1 =givenStockDetail.expLongTime != undefined?givenStockDetail.expLongTime.toString().split(','):"";
                    var psr = givenStockDetail.serialNames.split(',');
                    var pskur = givenStockDetail.skuFields != undefined?givenStockDetail.skuFields.split(','):",";
                    var rno=0;
                    var skuno=0;
                    for(var k1=rowQty;k1<psr.length+rowQty;k1++){
                        if(this.quantity<=cnt){
                            break;
                        }
                        var nextRec = this.locationGridStore.getAt(k1);
                        var srno=psr[rno++];
                        var skuvalue=pskur[skuno++];
                        qty=givenStockDetail.quantity;
                        if(k1==rowQty){
                            qty=givenStockDetail.quantity;
                            nextRec.set('batchQuantity', qty);
                            nextRec.set('isLocked', false);
                        }else{
                            nextRec.set('isLocked', true);
                        }
                        nextRec.set('locationid',this.isView ? givenStockDetail.locationName : givenStockDetail.locationId);  //location id display in some cases instead of name in batch serial window
                        this.getComboRenderer(this.locCmb);
                        nextRec.set('rowId', givenStockDetail.rowId);
                        nextRec.set('rackId', givenStockDetail.rackId);
                        nextRec.set('binId', givenStockDetail.binId);
                        nextRec.set('batch', givenStockDetail.batchName);
                        nextRec.set('serial', srno);
                        nextRec.set('skufield', skuvalue);
                        var expdt1=longDates1[k1-rowQty];
                        if(this.isSerialForProduct&&!this.isBatchForProduct){
                            expdt1=longDates1[rono++];
                        }
                        if(expdt1!=""&&expdt1!=undefined){
                            nextRec.set('expdate', new Date(expdt1));
                        }
                        nextRec.set('quantity', 1);
                        cnt++;
                    }
                }
                else{
                    if(this.quantity<=cnt){
                        break;
                    }
                   
                    for(var i=0 ; i< this.StockDetailArray.length ; i++){
                        var givenStockDetail = this.StockDetailArray[i];
                        var nextRec = this.locationGridStore.getAt(i);
                        qty=givenStockDetail.quantity;
                        nextRec.set('batchQuantity', qty);
                        nextRec.set('isLocked', false);
                        nextRec.set('locationid',this.isView ? givenStockDetail.locationName : givenStockDetail.locationId);
                        this.getComboRenderer(this.locCmb);
                        nextRec.set('rowId', givenStockDetail.rowId);
                        nextRec.set('rackId', givenStockDetail.rackId);
                        nextRec.set('binId', givenStockDetail.binId);
                        nextRec.set('batch','');
                        nextRec.set('serial', '');
                        nextRec.set('expdate', null);
                        nextRec.set('quantity', qty);
                    }
                }
                rowQty += Math.round(givenStockDetail.quantity);
            }
        }
        if(this.isView){
            this.locationGridStore.commitChanges();
        }
    },
    handleRowClick1:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var total=store.getCount();
            //            if(rowindex==total-1){
            //                return;
            //            }
            Wtf.MessageBox.confirm('Warning', 'Are you sure you want to remove this item?', function(btn){
                if(btn!="yes") return;
                store.remove(store.getAt(rowindex));
                this.ArrangeNumberer(grid, rowindex);
            //                this.fireEvent('datachanged',this);
            }, this);
        }
    },
    setDefaultLocationInGrid : function(rowIndex){
       
        var currentRec=this.locationGridStore.getAt(rowIndex);
        currentRec.set("locationid",this.defaultLocationName);
    },
    validateFilledData : function(){
        
        var recs=this.locationSelectionGrid.getStore().getRange();
        var batch="";
        var isBatchForProduct=false;
        var isSerialForProduct=false;
        var isSKUForProduct=false;
        var quantity=0;
        var serial="";
        var skuvl="";
        var locationid="";   
        var rowId="";   
        var rackId="";   
        var binId="";   
        var totalEnteredQty=0;
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                batch=recs[k].get("batch");
                isBatchForProduct=this.isBatchForProduct;
                isSerialForProduct=this.isSerialForProduct; 
                isSKUForProduct=this.isSKUForProduct; 
                quantity=(this.isBatchForProduct&&!this.isSerialForProduct)?recs[k].get("batchQuantity"):recs[k].get("quantity");
                serial=recs[k].get("serial");
                skuvl=recs[k].get("skufield");
                locationid=recs[k].get("locationid");
                rowId=recs[k].get("rowId");
                rackId=recs[k].get("rackId");
                binId=recs[k].get("binId");
                totalEnteredQty +=  quantity;
                    
                if(locationid == undefined || locationid == ""){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockadjustment.PleaseselectLocation")],0);
                    return false;
                }         
                if(this.isRowForProduct && !rowId){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockadjustment.PleaseselectRow")],0);
                    return false;
                }         
                if(this.isRackForProduct && !rackId){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockadjustment.PleaseselectRack")],0);
                    return false;
                }         
                if(this.isBinForProduct && !binId){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockadjustment.PleaseselectBin")],0);
                    return false;
                }         
                    
                if(this.realquantity > 0){   // ie.case for +ve qty .
                   
                    if(isSerialForProduct==true){
                        
                        var mfgdate=recs[k].get("mfgdate");
                        var expdate=recs[k].get("expdate"); 
                        var warrantyexpfromdate=recs[k].get("warrantyexpfromdate");
                        var warrantyexptodate=recs[k].get("warrantyexptodate");
                        
                        if(serial == undefined || serial == ""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.stockadjustment.PleaseenterSerial")],0);
                            return false;
                        }
//                        if((skuvl == undefined || skuvl == "") && Wtf.account.companyAccountPref.SKUFieldParm && isSKUForProduct){
//                            WtfComMsgBox(["Warning", "Please enter Asset Id value."],0);
//                            return false;
//                        }
                    //                        if((mfgdate == undefined || mfgdate == "") && this.realquantity > 0){
                    //                            WtfComMsgBox(["Warning", "Please enter Mfg. Date"],0);
                    //                            return false;
                    //                        }
                    //                        if((expdate == undefined || expdate == "") && this.realquantity > 0){ // for -ve qty will not throw an error
                    //                            WtfComMsgBox(["Warning", "Please enter Exp. Date"],0);
                    //                            return false;
                    //                        }
                    //                        if((warrantyexpfromdate == undefined || warrantyexpfromdate == "") && this.realquantity > 0){
                    //                            WtfComMsgBox(["Warning", "Please enter Warranty Exp. from Date."],0);
                    //                            return false;
                    //                        }
                    //                        if((warrantyexptodate == undefined || warrantyexptodate == "") && this.realquantity > 0){
                    //                            WtfComMsgBox(["Warning", "Please enter Warranty Exp. to Date."],0);
                    //                            return false;
                    //                        }
                       
                    }
                    if(isBatchForProduct==true){
                        if(batch == undefined || batch == ""){
                            WtfComMsgBox(["Warning", "Please enter Batch."],0);
                            return false;
                        }
                    //                        if(recs.length != this.quantity){ // in this case we can find modified record length so we can check if all data is filled or not
                    //                            WtfComMsgBox(["Alert", "Please fill all the data."],0);
                    //                            return false;
                    //                        }
                    }
                }
                
                if(this.realquantity < 0){ // ie.case for -ve qty .
                    if(isSerialForProduct==true){
                        
                        var serialsArr=[];
                        if(serial !=undefined && serial != ""){
                            serialsArr=serial.split(",");
                        }
                        if(serialsArr.length != quantity && quantity!=0){
                            WtfComMsgBox(["Warning", "Please Select "+quantity+" serials as per quantity."],0);
                            return false;
                        }
                        
                        if(recs.length != this.quantity && this.realquantity > 0){ // in this case we can find modified record length so we can check if all data is filled or not
                            WtfComMsgBox(["Alert", "Please fill all the data."],0);
                            return false;
                        }
                    }
                }
                if(this.realquantity < 0){ // ie.case for -ve qty .
                    if(isSKUForProduct==true && Wtf.account.companyAccountPref.SKUFieldParm){
                        
                        var skuArr=[];
                        if(skuvl !=undefined && skuvl != ""){
                            skuArr=skuvl.split(",");
                        }
                        if(skuArr.length != quantity && quantity!=0){
                            WtfComMsgBox(["Warning", "Please Select "+quantity+" sku field as per quantity."],0);
                            return false;
                        }
                    }
                }
            }
            if(totalEnteredQty != this.quantity){
                WtfComMsgBox(["Warning", "Please fill <b>"+this.quantity+" </b> Quantity."],0);
                return false;
            }
            return true;
            
        }else{
            WtfComMsgBox(["Warning", "Please Select To Location."],0);
            return false;
        }
        
    },
    makeJSONData : function(){
        var recs=this.locationSelectionGrid.getStore().getRange();
        var detailArray = new Array();
        // this for automatic assign in other transaction if rack row bin window only implemented in ILT
        
                    
        if(recs.length > 0){
            //            var rowId = "";
            //            if(this.rowCmb.store.getCount() > 0){
            //                var r = this.rowCmb.store.getAt(0);
            //                rowId = r.get("id");
            //            }
            //
            //               
            //            var rackId = "";
            //            if(this.rackCmb.store.getCount() > 0){
            //                var r = this.rackCmb.store.getAt(0);
            //                rackId = r.get("id");
            //            }
            //                  
            //                
            //            var binId = "";
            //            if(this.binCmb.store.getCount() > 0){
            //                var r = this.binCmb.store.getAt(0);
            //                binId = r.get("id");
            //            }
            
            var mergDataObject = {};
            var keyArray = [];
            for(var k=0;k<recs.length;k++){
                
                var batch=recs[k].get("batch");
                batch=batch!=null&&batch!=undefined?batch:"";
                var locationid=recs[k].get("locationid");  
                var rowId=recs[k].get("rowId");  
                var rackId=recs[k].get("rackId");  
                var binId=recs[k].get("binId");  
                var quantity=(this.isBatchForProduct&&!this.isSerialForProduct)?recs[k].get("batchQuantity"):recs[k].get("quantity");
                var serial=(recs[k].get("serial")!=undefined&&recs[k].get("serial")!=="")?recs[k].get("serial"):"";
                var skufield=(recs[k].get("skufield")!=undefined&&recs[k].get("skufield")!=="")?recs[k].get("skufield"):"";
                var mfgdate="";
                var expdate=""; 
                var expLongTime="";
                var warrantyexpfromdate="";
                var warrantyexptodate="";
                
                //  if id not set (ie. location name is set instead of location id then set its id in +ve quantity case) 
                if(locationid == this.defaultLocationName)  
                {
                    locationid = this.defaultLocationId;
                }     
                
                if(this.quantity>0 && (this.isSerialForProduct == true || this.isBatchForProduct) && this.realquantity > 0){
                    
                    if(recs[k].get("mfgdate") !="" && recs[k].get("mfgdate") != undefined){
                        mfgdate=recs[k].get("mfgdate").format("Y-m-d");
                    }
                    if(recs[k].get("expdate") !="" && recs[k].get("expdate") != undefined){
                        expdate=recs[k].get("expdate").format("Y-m-d");
                        expLongTime=recs[k].get("expdate");
                    }
                    
                    if(recs[k].get("warrantyexpfromdate") =="" || recs[k].get("warrantyexpfromdate")==undefined){
                    //                        recs[k].set("warrantyexpfromdate", Wtf.serverDate.clearTime(true));
                    //                        warrantyexpfromdate=Wtf.serverDate.clearTime(true).format("Y-m-d");
                    }else{
                        warrantyexpfromdate=recs[k].get("warrantyexpfromdate").format("Y-m-d");
                    }
                    
                    if(recs[k].get("warrantyexptodate") =="" || recs[k].get("warrantyexptodate")==undefined){
                    //                        recs[k].set("warrantyexptodate", Wtf.serverDate.clearTime(true));
                    //                        warrantyexptodate=Wtf.serverDate.clearTime(true).format("Y-m-d");
                        
                    //                        if(this.warrantyperiodsal=="" || this.warrantyperiodsal==undefined) {
                    //                            recs[k].set("warrantyexptodate", Wtf.serverDate.clearTime(true));
                    //                            warrantyexptodate=Wtf.serverDate.clearTime(true).format("Y-m-d");
                    //                        }else
                    //                        {
                    //                            warrantyexptodate = new Date(recs[k].get("warrantyexpfromdate")).add(Date.DAY,this.warrantyperiodsal);
                    //                            warrantyexptodate = warrantyexptodate.clearTime(true).format("Y-m-d");
                    //                            recs[k].set("warrantyexptodate",warrantyexptodate.clearTime(true));
                    //                        }
                    }else{
                        warrantyexptodate=recs[k].get("warrantyexptodate").format("Y-m-d");
                    }
                }
                
               
                if(quantity != 0){
                   
                    if(mergDataObject[locationid+""+batch+""+rowId+""+rackId+""+binId] != undefined){
                        
                        var mergeddata = mergDataObject[locationid+""+batch+""+rowId+""+rackId+""+binId]
                        mergeddata.quantity= mergeddata.quantity + quantity;
                        if(serial != undefined && serial != null && serial != ''){
                            mergeddata.serialNames= mergeddata.serialNames + ","+serial;
                        }
                        if(serial != undefined && serial != null && serial != ''&&skufield != undefined && skufield != null && skufield != ''){
                            mergeddata.skuFields= mergeddata.skuFields + ","+skufield;
                        }
                        
                        if(this.quantity>0 && (this.isSerialForProduct == true || this.isBatchForProduct)){
                            if(mfgdate != undefined && mfgdate != null){
                                mergeddata.mfgdate= mergeddata.mfgdate + ","+mfgdate;
                            }
                            if(expdate != undefined && expdate != null){
                                mergeddata.expdate= mergeddata.expdate + ","+expdate;
                            }
                            if(expdate != undefined && expdate != null){
                                mergeddata.expLongTime= mergeddata.expLongTime + ","+expLongTime;
                            }
                            if(warrantyexpfromdate != undefined && warrantyexpfromdate != null){
                                mergeddata.warrantyexpfromdate= mergeddata.warrantyexpfromdate + ","+warrantyexpfromdate;
                            }
                            if(warrantyexptodate != undefined && warrantyexptodate != null){
                                mergeddata.warrantyexptodate= mergeddata.warrantyexptodate + ","+warrantyexptodate;
                            }
                        }
                        
                    } else{
                        var jsondata = {};
                        jsondata.locationId=locationid;
                        jsondata.rowId=rowId;
                        jsondata.rackId=rackId;
                        jsondata.binId=binId;
                        jsondata.batchName=(batch!=null&&batch!=undefined)?batch:"";
                        jsondata.serialNames=serial;
                        jsondata.skuFields=skufield;
                        jsondata.quantity=quantity;
                        
                        if(this.quantity>0 && (this.isSerialForProduct == true || this.isBatchForProduct)){
                            jsondata.mfgdate=mfgdate;
                            jsondata.expdate=expdate;
                            jsondata.expLongTime=expLongTime;
                            jsondata.warrantyexpfromdate=warrantyexpfromdate;
                            jsondata.warrantyexptodate=warrantyexptodate; 
                        }
                        
                        mergDataObject[locationid+""+batch+""+rowId+""+rackId+""+binId] = jsondata;
                        keyArray.push(locationid+""+batch+""+rowId+""+rackId+""+binId)
                    }
                   
                }
                
                
            }
            for(var i=0;i<keyArray.length;i++){
                
                var mergeData=mergDataObject[keyArray[i]];
                
                var data = {};
                data.locationId=mergeData.locationId;
                data.rowId=mergeData.rowId;
                data.rackId=mergeData.rackId;
                data.binId=mergeData.binId;
                data.batchName=mergeData.batchName;
                data.serialNames=mergeData.serialNames;
                data.skuFields=mergeData.skuFields;
                data.quantity=mergeData.quantity;
                
                if(this.quantity>0 && (this.isSerialForProduct == true || this.isBatchForProduct)){
                    data.mfgdate=mergeData.mfgdate;
                    data.expdate=mergeData.expdate;
                    data.expLongTime=mergeData.expLongTime;
                    data.warrantyexpfromdate=mergeData.warrantyexpfromdate;
                    data.warrantyexptodate=mergeData.warrantyexptodate;
                }
                
                detailArray.push(data);
            }
        }
        return detailArray;
    },
       
    gridbeforeEdit :function(e){
        
        var rec=e.record;
        if(rec.get('isLocked') == true && (e.field == 'locationid' ||e.field == 'rowId' ||e.field == 'rackId' ||e.field == 'binId' || e.field == 'batch' || e.field == 'batchQuantity')){
            return false;
        }
        
        if(rec.get('isLocked') == true &&  e.field == 'expdate' && e.record.data.isBatchForProduct && !e.record.data.isSerialForProduct ){
            return false;
        }
        
        if(e.record.data.isBatchForProduct == false && (e.field =='batch')) {
            return false;
        }
        if(e.record.data.isSerialForProduct == false && (e.field =='serial')) {
            return false;
        }
        if(e.field =='serial' &&  (e.record.data.locationid == "" || e.record.data.locationid == undefined)) {
            WtfComMsgBox(["Warning", "Please select location first."],0);
            return false;
        }
        if(e.field =='serial' && e.record.data.isBatchForProduct == true && (e.record.data.batch == "" || e.record.data.batch == undefined || e.record.data.batch == null)){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.stockadjustment.PleaseenterBatchfirst")],0);
            return false;
        }
        if(e.field =='serial' &&  e.record.data.quantity==0) {
            WtfComMsgBox(["Warning", "Please Fill quantity first."],0);
            return false;
        }
    },
    addBlankRow:function (quantity,batch){
    
        var batchQuantity;
        if (true) {
            batchQuantity = quantity;
        }
        this.newRec = new this.EditorRec({
            itemid :this.itemId,
            itemcode:this.itemcode,
            locationid : "",
            rowId : "",
            rackId : "",
            binId : "",
            isBatchForProduct:this.isBatchForProduct,
            isSerialForProduct:this.isSerialForProduct,
            isRowForProduct:this.isRowForProduct,
            isRackForProduct:this.isRackForProduct,
            isBinForProduct:this.isBinForProduct,
            batch: batch || "",
            serial:"",
            skufield:"",
            quantity:quantity,
            mfgdate:"",
            expdate:"",
            batchQuantity:batchQuantity,
            warrantyexpfromdate:"",
            warrantyexptodate:""
        });
    
        // this.newRec.push([this.itemId,this.itemcode,"",this.isBatchForProduct,this.isSerialForProduct,"","",0]);
        this.locationGridStore.add(this.newRec); 
    // this.locationSelectionGrid.getView().refresh();
    },
    
    
    gridafterEdit :function(e){
        
        var rec= e.record;
        var rowIndex = e.row;
        var value = e.value;
        var grid = e.grid;
        var store = grid.getStore();
        
        var totalAllowQty=this.quantity;
        var enteredTotalQty=0;
        var modifiedRecs=this.locationGridStore.getModifiedRecords(); 
        
        this.getComboRenderer(this.locCmb);
        
        if(e.field=="batchQuantity"&&e.record.data.isSerialForProduct){
            if(this.quantity < e.value + e.row){
                WtfComMsgBox(["Warning", "You have allowed only "+( this.quantity - e.row)+" Quantity."],0);
                value = this.quantity - e.row
                rec.set('batchQuantity', value)
            }
            
            for(var i=1; i<value ; i++){
                var nextRec = store.getAt(rowIndex + i);
                nextRec.set('locationid', rec.get('locationid'));
                this.getComboRenderer(this.locCmb);
                nextRec.set('rowId', rec.get('rowId'));
                this.getComboRenderer(this.rowCmb);
                nextRec.set('rackId', rec.get('rackId'));
                this.getComboRenderer(this.rackCmb);
                nextRec.set('binId', rec.get('binId'));
                this.getComboRenderer(this.binCmb);
                nextRec.set('batch', rec.get('batch'));
                nextRec.set('expdate', rec.get('expdate'));
                nextRec.set('isLocked', true);
                nextRec.set('batchQuantity', '');
            }
            nextRec = store.getAt(rowIndex+value);
            if(nextRec != undefined){
                while(nextRec.get('isLocked') == true){
                    store.remove(nextRec);
                    this.ArrangeNumberer(grid, rowIndex+value);
                    nextRec = store.getAt(rowIndex+value);
                    this.addBlankRow(1);
                }
            }
            
        }
        if(e.field=="batch"){
            if(Wtf.account.companyAccountPref.columnPref.restrictDuplicateBatch && e.record.get('batch') != "" && e.record.get('batch') != undefined ){
                var rec=e.record;
                var batch=rec.get("batch");
                var curBatchSerial= batch;
                var batchSerialArr=new Array();
                var count=0;
                
                for(var x=0; x < modifiedRecs.length ; x++){
                    var btch=modifiedRecs[x].get("batch");
                    var srl=modifiedRecs[x].get("serial");
                    if(btch !="" && btch != undefined){
                        var key=btch;
                        batchSerialArr.push(key);
                    }
                    
                }
                
                for(var y=0; y < batchSerialArr.length ; y++){
                    if(curBatchSerial == batchSerialArr[y]){
                        count++;
                    }
                }
                if(count >1){
                    WtfComMsgBox(["Alert",WtfGlobal.getLocaleText("acc.batch.duplicatebatchname")],3);
                    rec.set("batch","");
                    return false;
                }
                
                if((batch == "" || batch == undefined || batch == null) && e.record.data.isBatchForProduct == true){
                    WtfComMsgBox(["Alert","Please enter Batch first."],3);
                    return false;
                }else{
                    
                    if(Wtf.stockAdjustmentTempDataHolder != undefined && Wtf.stockAdjustmentTempDataHolder.length >0){
                        var currentSerialRec="#"+batch;
                        if(Wtf.stockAdjustmentTempDataHolder.indexOf(currentSerialRec) != -1){
                            WtfComMsgBox(["Alert",WtfGlobal.getLocaleText("acc.batch.duplicatebatchname")],3);
                            rec.set("batch","");
                            return false;
                        } 
                    }
                    rec.set("batch","");
                    Wtf.Ajax.requestEx({
                        url:"INVStockLevel/isBatchExists.do",
                        params: {
                            productid:this.itemId,
                            batch : batch
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
                                WtfComMsgBox([title,"The batch you entered  : <b>"+batch+"</b> already exists."],0);
                                rec.set("batch","");
                                return false;
                            }else{
                                rec.set("batch",batch);
                            }
                                
                        }
                        else if(result.success==false){
                            title="Error";
                            WtfComMsgBox([title,"Some Error occurred."],0);
                            rec.set("batch","");
                            return false;
                        }
                    },
                    function(result, req){
                        WtfComMsgBox(["Failure", "Some Error occurred."],3);
                        rec.set("batch","");
                        return false;
                    });
                }    
            } else {
                var batchQty = rec.get('batchQuantity');
                if(batchQty != undefined){
                    for(var i=1; i<batchQty ; i++){
                        var nextRec = store.getAt(rowIndex + i);
                        nextRec.set('batch', value);
                    }
                }
            }
        }
        if(e.field=="expdate" && (e.record.data.isBatchForProduct || e.record.data.isSerialForProduct) ){
            var batchQty = rec.get('batchQuantity');
            if(batchQty != undefined){
                for(var i=1; i<batchQty ; i++){
                    var nextRec = store.getAt(rowIndex + i);
                    nextRec.set('expdate', value);
                }
            }
        }
        if(e.field=="locationid"){
            var batchQty = rec.get('batchQuantity');
            if(batchQty != undefined){
                for(var i=1; i<batchQty ; i++){
                    var nextRec = store.getAt(rowIndex + i);
                    nextRec.set('locationid', value);
                }
            }
            
        }
        if(e.field=="rowId"){
            var batchQty = rec.get('batchQuantity');
            if(batchQty != undefined){
                for(var i=1; i<batchQty ; i++){
                    var nextRec = store.getAt(rowIndex + i);
                    nextRec.set('rowId', value);
                }
            }
            
        }
        if(e.field=="rackId"){
            var batchQty = rec.get('batchQuantity');
            if(batchQty != undefined){
                for(var i=1; i<batchQty ; i++){
                    var nextRec = store.getAt(rowIndex + i);
                    nextRec.set('rackId', value);
                }
            }
            
        }
        if(e.field=="binId"){
            var batchQty = rec.get('batchQuantity');
            if(batchQty != undefined){
                for(var i=1; i<batchQty ; i++){
                    var nextRec = store.getAt(rowIndex + i);
                    nextRec.set('binId', value);
                }
            }
            
        }
        var totalRec=store.getCount();
        if(totalRec>0){
            for(var i=0; i < totalRec;i++){
                var currentRec=this.locationGridStore.getAt(i);
                enteredTotalQty += (e.record.data.isBatchForProduct&&! e.record.data.isSerialForProduct)?currentRec.get("batchQuantity"):currentRec.get("quantity");
            }
            if(e.field=="quantity" && e.record.get('quantity') > totalAllowQty){
                var rec=e.record;
                rec.set("quantity",(totalAllowQty-enteredTotalQty) > 0 ? (totalAllowQty-enteredTotalQty) : 0);
                return false;
            }
            if(e.field=="serial"  && e.record.get('serial') != "" && e.record.get('serial') != undefined){
                
                var comma = "," ;
                var rec=e.record;
                var batch=rec.get("batch");
                var serial=rec.get("serial");
                var locationid=rec.get("locationid");
                
                var curBatchSerial=(batch != undefined && batch != "") ? batch.concat(serial) :serial;
                var batchSerialArr=new Array();
                var count=0;
                
                if (serial.match(comma)) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batchserialwindow.enterSerialNameWithoutComma")], 0);
                    rec.set("serial", "");
                    return false;
                }
                
                for(var x=0; x < modifiedRecs.length ; x++){
                    var btch=modifiedRecs[x].get("batch");
                    var srl=modifiedRecs[x].get("serial");
                    if(srl !="" && srl != undefined){
                        var key=(btch != undefined && btch != "") ? btch.concat(srl) : srl;
                        batchSerialArr.push(key);
                    }
                    
                }
                
                for(var y=0; y < batchSerialArr.length ; y++){
                    if(curBatchSerial == batchSerialArr[y]){
                        count++;
                    }
                }
                if(count >1){
                    WtfComMsgBox(["Alert","Serial already added."],3);
                    rec.set("serial","");
                    return false;
                }
                
                if((batch == "" || batch == undefined || batch == null) && e.record.data.isBatchForProduct == true){
                    WtfComMsgBox(["Alert","Please enter Batch first."],3);
                    return false;
                }else{
                    
                    if(Wtf.stockAdjustmentTempDataHolder != undefined && Wtf.stockAdjustmentTempDataHolder.length >0){
                        var currentSerialRec=this.itemId+"#"+batch+"#"+serial;
                        if(Wtf.stockAdjustmentTempDataHolder.indexOf(currentSerialRec) != -1){
                            WtfComMsgBox(["Alert","Serial already added."],3);
                            rec.set("serial","");
                            return false;
                        } 
                    }
                    
                    Wtf.Ajax.requestEx({
                        url:"INVStockLevel/isSerialExists.do",
                        params: {
                            productid:this.itemId,
                            batch : batch,
                            serial :serial,
                            locationid:locationid,
                            storeid: this.fromStoreId
                        }
                    },
                    this,
                    function(result, req){

                        var msg=result.msg;
                        var title="Error";
                        if(result.success){
                                
                            var isSerialExists=result.data.isSerialPresent;
                            
                            if(isSerialExists == true){
                                title="Warning";
                                WtfComMsgBox([title,"The serial you entered  : <b>"+serial+"</b> already exists."],0);
                                rec.set("serial","");
                                return false;
                            }
                                                          
                        }
                        else if(result.success==false){
                            title="Error";
                            WtfComMsgBox([title,"Some Error occurred."],0);
                            rec.set("serial","");
                            return false;
                        }
                    },
                    function(result, req){
                        WtfComMsgBox(["Failure", "Some Error occurred."],3);
                        rec.set("serial","");
                        return false;
                    });
                }
            }
            
            if(this.isSKUForProduct && e.field=="skufield"  && e.record.get('skufield') != "" && e.record.get('skufield') != undefined){
                
                var rec=e.record;
                var batch=rec.get("batch");
                var skufield=rec.get("skufield");
                var SKUDisplayName=(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku");
                
//                var curBatchSKU=(batch != undefined && batch != "") ? batch.concat(skufield) :skufield;
                var curBatchSKU= skufield;
                var batchSKUArr=new Array();
                var count=0;
                
                for(var x=0; x < modifiedRecs.length ; x++){
                    var btch=modifiedRecs[x].get("batch");
                    var sku=modifiedRecs[x].get("skufield");
                    if(sku !="" && sku != undefined){
                        var key= sku ;
//                        var key=(btch != undefined && btch != "") ? btch.concat(sku) : sku ;
                        batchSKUArr.push(key);
                    }
                    
                }
                
                for(var y=0; y < batchSKUArr.length ; y++){
                    if(curBatchSKU == batchSKUArr[y]){
                        count++;
                    }
                }
                if(count >1){
                    WtfComMsgBox(["Alert",SKUDisplayName+" already added."],3);
                    rec.set("skufield","");
                    return false;
                }
                
              
                var totalRecLength = this.parentStore.data.length;
                if (totalRecLength > 0) {
                    for (var i = 0; i < totalRecLength; i++) {
                        var Mainrec = this.parentStore.data.items[i];
                        var stockDetails = Mainrec.get("stockDetails");
                        if(stockDetails!=undefined&&stockDetails!=""){
                            for (var x = 0; x < stockDetails.length; x++) {
                                var recc = stockDetails[x];
                                var skuArr = recc.skuFields.split(",");
                                for (var z = 0; z < skuArr.length; z++) {
                                    if(curBatchSKU== skuArr[z]){
                                        WtfComMsgBox(["Alert",curBatchSKU+" already added."],3);
                                        rec.set("skufield","");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
                
                if((batch == "" || batch == undefined || batch == null) && e.record.data.isBatchForProduct == true){
                    WtfComMsgBox(["Alert","Please enter Batch first."],3);
                    return false;
                }else{
                    
                    //                    if(Wtf.stockAdjustmentTempDataHolder != undefined && Wtf.stockAdjustmentTempDataHolder.length >0){
                    //                        var currentSerialRec=this.itemId+"#"+batch+"#"+skufield;
                    //                        if(Wtf.stockAdjustmentTempDataHolder.indexOf(currentSerialRec) != -1){
                    //                            WtfComMsgBox(["Alert","Serial already added."],3);
                    //                            rec.set("serial","");
                    //                            return false;
                    //                        } 
                    //                    }
                    
                    Wtf.Ajax.requestEx({
                        url:"INVStockLevel/isSKUExists.do",
                        params: {
                            productid:this.itemId,
                            batch : batch,
                            sku :skufield
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
                                WtfComMsgBox([title,"The "+SKUDisplayName+" you entered  : <b>"+skufield+"</b> already exists."],0);
                                rec.set("skufield","");
                                return false;
                            }
                                
                        }
                        else if(result.success==false){
                            title="Error";
                            WtfComMsgBox([title,"Some Error occurred."],0);
                            rec.set("skufield","");
                            return false;
                        }
                    },
                    function(result, req){
                        WtfComMsgBox(["Failure", "Some Error occurred."],3);
                        rec.set("skufield","");
                        return false;
                    });
                }
            }
            
        }    
        
            
        if(this.isSerialForProduct){
            var lastRec = store.getAt(totalRec - 1);
            if(e.field=="batchQuantity" && totalRec < totalAllowQty  && lastRec.get('locationid') != undefined){
            //                this.addBlankRow(1);
            }
        }else{
            if(enteredTotalQty < totalAllowQty  && e.row == this.locationGridStore.getCount()-1){
                this.addBlankRow(totalAllowQty-enteredTotalQty);
            }
        }
        
    },
    showStockDetailWindow : function (record){
        var itemId=record.get("productid");
        var itemCode=record.get("pid");
        var itemName=record.get("productname");
        var quantity=record.get("quantity");
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !isBatchEnable && !isSerialEnable;
        var isRackEnable = record.get("isRackForProduct");
        var isRowEnable = record.get("isRowForProduct");
        var isBinEnable = record.get("isBinForProduct");
        var isSKUEnable = record.get("isSKUForProduct");
        var transferToStockUOMFactor=1;
        var stockUOMName=record.get("uomname");
        var fromStoreId = this.parent.fromstoreCombo.getValue();
        var fromStoreName=this.parent.fromstoreCombo.getRawValue();
        var maxQtyAllowed=transferToStockUOMFactor * quantity;
        var adjustmentType=record.get("adjustmentType");
        
        var winTitle = WtfGlobal.getLocaleText("acc.stockrequest.StockDetailforStockTransfer");
        var winDetail = String.format('Select Stock details for stock transfer  <br> <b>Product ID : </b> {0}<br> <b>Product Name : </b> {6}<br> <b>Store :</b> {1}<br> <b>Quantity :</b> {2} {3} ( {4} {5} )', itemCode, fromStoreName, quantity, stockUOMName, maxQtyAllowed, stockUOMName, itemName);
        
        this.detailWin = new Wtf.StockTransferDetailWin({
            WinTitle : winTitle,
            WinDetail: winDetail,
            TotalTransferQuantity: maxQtyAllowed,
            ProductId:itemId,
            FromStoreId: fromStoreId,
            //            ToStoreId: fromStoreId,
            isBatchForProduct: isBatchEnable,
            isSerialForProduct : isSerialEnable,
            isRowForProduct: isRowEnable,
            isRackForProduct: isRackEnable,
            isBinForProduct: isBinEnable,
            isSKUForProduct : isSKUEnable,
            GridStoreURL: "INVStockLevel/getStoreProductWiseDetailList.do",
            StockDetailArray:record.get("stockDetails"),
            isNegativeAllowed: isNegativeAllowed,
            moduleid:Wtf.Acc_Stock_Adjustment_ModuleId,
            modulename:"STOCK_ADJUSTMENT",
            DataIndexMapping:{
                fromLocationId:"locationId",
                fromRowId:"rowId",
                fromRackId:"rackId",
                fromBinId:"binId",
                serials:"serialNames"
            },
            buttons:[{
                text:"Save",
                handler:function (){
                    if(this.detailWin.validateSelectedDetails()){
                        if (record.get("stockDetails") != "" && record.get("stockDetails") != undefined) { // previous record before edit
                            var productId = this.detailWin.ProductId;
                            for (var x = 0; x < record.get("stockDetails").length; x++) {
                                var recc = record.get("stockDetails")[x];
                                if (recc.serialNames != "" && recc.serialNames != undefined) {
                                    var serialsArr = recc.serialNames.split(",");
                                    for (var z = 0; z < serialsArr.length; z++) {
                                        var curRec = productId + "#" + recc.batchName + "#" + serialsArr[z];
                                        var index=Wtf.stockAdjustmentTempDataHolder.indexOf(curRec);
                                        if (index != -1) {
                                            Wtf.stockAdjustmentTempDataHolder.splice(index,1);
                                        }
                                    }
                                }else{
                                    if (Wtf.stockAdjustmentProdBatchQtyMapArr.length > 0) {
                                        for (var s = 0; s < Wtf.stockAdjustmentProdBatchQtyMapArr.length; s++) {
                                            var recObj = Wtf.stockAdjustmentProdBatchQtyMapArr[s];
                                            if (recObj != "" && recObj != undefined) {
                                                if (recObj.productId == productId && recObj.fromLocationId == recc.locationId && recObj.batchName == recc.batchName) {
                                                    var arrQty = recObj.quantity;
                                                    var filledQty = recc.quantity;
                                                    recObj.quantity = Number(arrQty) - Number(filledQty);
                                                }
                                            }
                                        }   
                                    }
                                }
                            }
                        }
                        var detailArray = this.detailWin.getSelectedDetails();
                        record.set("stockDetails","");
                        record.set("stockDetails",detailArray);
                        record.set("stockDetailQuantity",quantity);
                        var totalRecLength = this.detailWin.grid.getStore().data.length;
                        if (totalRecLength > 0) {
                            for (var i = 0; i < totalRecLength; i++) {
                                var rec = this.detailWin.grid.getStore().data.items[i];
                                var productId = this.detailWin.ProductId;
                                var batchName = rec.get("batchName");
                                if (rec.get("serials") != undefined && rec.get("serials") != "") {
                                    var serials = rec.get("serials").split(",");
                                    for (var x = 0; x < serials.length; x++) {
                                        var srl = serials[x];
                                        Wtf.stockAdjustmentTempDataHolder.push(productId + "#" + batchName + "#" + srl);
                                    }
                                } else if (rec.get("serials") == undefined || rec.get("serials") == "") {

                                    if (Wtf.stockAdjustmentProdBatchQtyMapArr.length > 0) {
                                        var tempMapArr=new Array();
                                        var isRecFound=false;
                                        for (var x = 0; x < Wtf.stockAdjustmentProdBatchQtyMapArr.length; x++) {
                                            var recObj = Wtf.stockAdjustmentProdBatchQtyMapArr[x];
                                            if (recObj != "" && recObj != undefined) {
                                                if (recObj.productId == productId && recObj.fromLocationId == rec.get("fromLocationId") && recObj.batchName == batchName) {
                                                    var arrQty = Number(recObj.quantity);
                                                    var filledQty = rec.get("quantity")=="" ? 0 : Number(rec.get("quantity"));
                                                    recObj.quantity = arrQty + filledQty;
                                                    isRecFound=true;
                                                    break;
                                                } else {
                                                    if (rec.get("quantity") != "" && rec.get("quantity") != "0") {
                                                        var data = {};
                                                        data.productId = productId;
                                                        data.fromLocationId = rec.get("fromLocationId");
                                                        data.batchName = rec.get("batchName");
                                                        data.quantity = Number(rec.get("quantity"));
                                                        var isDuplicateRecInTempArr=false;
                                                        for(var dd=0;dd<tempMapArr.length;dd++){
                                                            var ArrObj=tempMapArr[dd];
                                                            if(ArrObj.productId==data.productId && ArrObj.fromLocationId==data.fromLocationId && ArrObj.batchName==data.batchName){
                                                                isDuplicateRecInTempArr=true;
                                                                break;
                                                            }
                                                        }
                                                        if(!isDuplicateRecInTempArr){
                                                            tempMapArr.push(data);
                                                        }
                                                    }

                                                }
                                            }
                                        }
                                            
                                        if (!isRecFound) {
                                            if (tempMapArr.length > 0) {
                                                for (var y = 0; y < tempMapArr.length; y++) {
                                                    Wtf.stockAdjustmentProdBatchQtyMapArr.push(tempMapArr[y]);
                                                }
                                            }
                                        }
                                            
                                            
                                    }else {
                                        if (rec.get("quantity") != "") {
                                            var data = {};
                                            data.productId = productId;
                                            data.fromLocationId = rec.get("fromLocationId");
                                            data.batchName = rec.get("batchName");
                                            data.quantity = rec.get("quantity");
                                            Wtf.stockAdjustmentProdBatchQtyMapArr.push(data);
                                        }
                                    }
                                }
                            }
                        }
                        
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
    showLocationBatchSerialSelectWindow : function (itemId,itemCode,isBatchForProduct,isSerialForProduct,quantity,UOMName,rowIndex, record){
        
        this.fromStoreId = this.parent.fromstoreCombo.getValue();
        this.fromStoreName=this.parent.fromstoreCombo.getRawValue();
        this.itemId=itemId;
        this.itemcode=itemCode;
        this.isBatchForProduct=isBatchForProduct;
        this.isSerialForProduct=isSerialForProduct;
        this.quantity=Math.abs(quantity);
        this.realquantity=quantity;
        this.currentRowNo=rowIndex;
        this.UOMName=UOMName;
       
        this.serialCmbRecord = new Wtf.data.Record.create([
        {
            name: 'serialnoid'
        },        

        {
            name: 'serial'
        }]);
        var serialURL = 'INVStockLevel/getProductBatchWiseSerialList.do';
        if(record.get('adjustmentType') == "Stock Sales"){
            serialURL='INVStockLevel/getProductBatchWiseSerialList.do?checkQAReject=true';
        }
        this.serialCmbStore = new Wtf.data.Store({
            url: serialURL,
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
                header:"Location ID",
                dataIndex:"locationid",
                hidden:true
            },
            {
                header:"Location",
                dataIndex:"locationname"
            },
            {
                header:(Wtf.jobWorkInFlowFlag!= undefined && Wtf.jobWorkInFlowFlag==true && Wtf.isJobWorkInReciever==true )? "Challan No":"Batch",
                dataIndex:"batch",
                hidden : (this.isBatchForProduct==true) ? false :true,
                id:"batchColumn"
            //renderer:this.getComboRenderer(this.batchCmb)
            },
            {
                header:"Available Quantity",
                dataIndex:"availableQty"
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
                header:"Quantity*",
                dataIndex:"quantity",
                editor:this.quantityeditor
            },
            {
                header:"Serial*",
                dataIndex:"serial",
                editor:this.serialCmb,
                id:"serialColumn",
                hidden : (this.isSerialForProduct==true) ? false :true,
                renderer:this.getComboRenderer(this.serialCmb)
            },{
                //            header: header:WtfGlobal.getLocaleText("acc.product.sku"),      
                header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
                hidden:!(Wtf.account.companyAccountPref.SKUFieldParm && this.isSerialForProduct),
                editor:this.readOnly?"":new Wtf.form.TextField({
                    name:'skufield'
                }) 
            }
            ]);
        
            
        this.locationGridStore = new Wtf.data.SimpleStore({
            fields:['itemid','itemcode','locationid','locationname','isBatchForProduct',
            'isSerialForProduct','batch','serial','selectedSerials','quantity','availableQty','skufield','isSKUForProduct'],
            pruneModifiedRecords:true
        });
          
        this.locationGridStore.on('load', function(){
            this.locationSelectionGrid.getView().refresh();
        },this);
        
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        var callURL = "";
        var caseType="";
        
        if(this.isBatchForProduct == true && this.isSerialForProduct==true){
            if(record.get('adjustmentType') == "Stock Sales"){
                callURL="INVStockLevel/getStoreProductWiseLocationBatchList.do?checkQAReject=true";
            }else{
                callURL="INVStockLevel/getStoreProductWiseLocationBatchList.do";
            }
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
        
        if(callURL != ""){
            
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
 
                    // 'itemid','itemcode','locationid','locationname','isBatchForProduct',
                    //'isSerialForProduct','batch','serial','selectedSerials','quantity','availableQty'
 
                
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
                                res.data[i].batchName,"","",0,res.data[i].availableQty]);
                            
                           
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
                
        }
         
        ///////////////////////////////////////////////////////////////////////////////////////////////// 
        
      
        this.locationSelectionGrid = new Wtf.grid.EditorGridPanel({
            cm:this.EditorColumn,
            region:"center",
            //id:"editorgrid2sd",
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
        +"<b>For Store : </b>"+this.fromStoreName+"<br/>"
        +"<b>Quantity : </b>"+ this.quantity +" "+this.UOMName;
       
        
        
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
                height : 100,
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
                        region : 'north',
                        border : false,
                        height : 5
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
                iconCls:'pwnd ReasonSubmiticon caltb',
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
                iconCls:'pwnd rejecticon caltb',
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
                    storeid:this.fromStoreId,
                    locationid:rec.data.locationid
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
            
            var totalRec=this.locationGridStore.data.length;
            var totalIssueQty=this.quantity;
            var enteredTotalQty=0;
            
            for(var i=0; i < totalRec;i++){
                var currentRec=this.locationGridStore.getAt(i);
                enteredTotalQty += currentRec.get("quantity");
            }
            if(enteredTotalQty > totalIssueQty){
            //                var rec=e.record;
            //                rec.set("quantity",(totalIssueQty-enteredTotalQty) > 0 ? (totalIssueQty-enteredTotalQty) : 0);
            //                return false;
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
    }
});

//-----------------------------StockPending Grid---------------------------------------------------------------------------------

Wtf.markoutPendingGrid = function (config){
    Wtf.apply(this,config);
    Wtf.markoutPendingGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.markoutPendingGrid,Wtf.Panel,{
    initComponent:function (){
        Wtf.markoutPendingGrid.superclass.initComponent.call(this);
        this.getEditorGrid();
        this.tmpPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.StockPendingGrid
            ]
        });
        this.add(this.tmpPanel);
        this.on("activate",function(){
            this.loadgridstore(this.frmDate.getValue().format("Y-m-d"), this.toDate.getValue().format("Y-m-d"));
        },this);
    },
    
    getEditorGrid:function (){
    
        this.StockPendingRec = new Wtf.data.Record.create([
        {
            "name":"id"
        },

        {
            "name":"store_id"
        },

        {
            "name":"storeAbbr"
        },

        {
            "name":"storeDesc"
        },

        {
            "name":"productCode"
        },

        {
            "name":"productName"
        },

        {
            "name":"quantity"
        },

        {
            "name":"uomName"
        },
        {
            "name":"createdBy"
        },
        {
            "name":"markouttype"
        },

        {
            "name":"adjutmentType"
        },
        {
            "name":"reason"
        },

        {
            "name":"costcenter"
        },

        {
            "name":"cost"
        },

        {
            "name":"amount"
        },

        {
            "name":"date", 
            "type":"date", 
            "format":"Y-m-d"
        },

        {
            "name":"type"
        },

        {
            "name":"remark"
        },

        {
            "name":"partnumber"
        },

        {
            "name":"seqNumber"
        },
        {
            "name":"locationname"
        },
        {
            "name":"locationid"
        },
        {
            "name":"status"
        }
        ]);
        
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.todateVal=new Date().getLastDateOfMonth();
        //        for(var i=0;i<Wtf.closingDates.length-1;i++){
        //            var curtrec=Wtf.closingDates[i];
        //            var nextrec=Wtf.closingDates[i+1];
        //            var prevrec=Wtf.closingDates[i-1];
        //            //var crmonth = new Date(curtrec.date);
        //            //var nextmonth = new Date(nextrec.date);
        //            if(curtrec.date == this.fromdateVal.format('Y-m-d')){
        //                this.fromdateVal= Date.parseDate(prevrec.date, "Y-m-d").add(Date.DAY,1).format("Y-m-d");
        //                this.todateVal = curtrec.date;
        //                break;
        //            }else if((curtrec.date < this.fromdateVal.format('Y-m-d')) && (nextrec.date > this.fromdateVal.format('Y-m-d'))) {
        //                //            this.fromdateVal= new Date(curtrec.date).add(Date.DAY,1);
        //                //            this.todateVal = new Date(nextrec.date);
        //                this.fromdateVal= Date.parseDate(curtrec.date, "Y-m-d").add(Date.DAY,1).format("Y-m-d");
        //                this.todateVal = nextrec.date;
        //                break;
        //            }
        //        } 
        
        this.frmDate = new Wtf.form.DateField({
            emptyText:'From date...',
            readOnly:true,
            width : 100,
            value:this.fromdateVal,
            name : 'frmdate',
            //minValue: Wtf.archivalDate,
            format: "Y-m-d"//"Y-m-d"
        });
        this.toDate = new Wtf.form.DateField({
            emptyText:'To date...',
            readOnly:true,
            width : 100,
            name : 'todate',
            value:this.todateVal,
            //minValue: Wtf.archivalDate,
            format: "Y-m-d"//"Y-m-d"
        });
        var format = "Y-m-d";
        this.StockPendingReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.StockPendingRec);

        this.StockPendingStore = new Wtf.data.Store({

            url:"INVStockAdjustment/getPendingStockAdjustmentList.do",
            baseParams: {
                frmDate:this.frmDate.getValue().format(format),
                toDate:this.toDate.getValue().format(format)
            },
            reader:this.StockPendingReader

        });
        this.StockPendingStore.load();
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
     
        this.StockPendingColumn = new Wtf.grid.ColumnModel([  this.sm,new Wtf.grid.RowNumberer(),
           
        {
                header: "Stock Adjustment No",
                dataIndex: 'seqNumber',
                groupable: false,
                width:200
            },
            {
                header: "Store",
                dataIndex: 'storeAbbr',
                width:200
            },
            {
                header:"Location",
                sortable:false,
                dataIndex:"locationname",
                width:200
            },
            {
                header: "Created By",
                dataIndex: 'createdBy',
                width:200
            },
            {
                header: "Business Date",
                dataIndex: 'date',
                renderer: function(v){
                    var date = v;
                    if(date != undefined && date != ""){
                        date = date.format("Y-m-d");
                    }
                    return date;
                },
                width:200
            },
            {
                header: "Product ID",
                dataIndex: 'productCode',
                width:200
            },
            {
                header: "Product Name",
                dataIndex: 'productName',
                width:200
            },
            {
                header: "CoilCraft Part No",
                dataIndex: 'partnumber',
                groupable: false,
                sortable:false,
                hidden:true
            //hidden: true//integrationFeatureFor == Wtf.IF.COILCRAFT ? false: true
            },
            {
                header: "Quantity",
                dataIndex: 'quantity',
                sortable:false,
                align: 'right',
                summaryType: 'sum',
                renderer:function(val){
                    // return WtfGlobal.getCurrencyFormatWithoutSymbol(val, Wtf.companyPref.quantityDecimalPrecision);
                    return val;
                },
                width:200
            },
            {
                header:'Amount',
                dataIndex: 'cost',
                align: 'right',
                sortable:false,
                summaryType: 'sum',
                renderer:function(val){
                    // return WtfGlobal.getCurrencyFormatWithoutSymbol(val,2);
                    return val;
                },
                width:200
            },
            {
                header: "UoM",
                dataIndex: 'uomName',
                width:200
            },
            {
                header:"Adjustment Type",
                dataIndex:"adjustmentType",
                width:200
            
            },
            {
                header:"Reason",
                dataIndex:"reason",
                width:200,
                hidden:true
            
            },
            {
                header:"Cost Center",
                dataIndex:"costcenter",
                width:200,
                hidden:false
            },
            {
                header:"Remark",
                dataIndex:"remark",
                width:200,
                hidden:false
           
            },
            {
                header:"Status",
                dataIndex:"status",
                width:200,
                hidden:false,
                renderer:function(value){
                    if(value == "Aproved"){
                        return "<div style='color:green'>Aproved</div>";
                    } 
                      
                    else{
                        return "<div style='color:black'>Pending for Approval</div>";
                    } 
                   
                }
           
            }
  
            ]);
        
  
        this.dateField1 = new Wtf.form.DateField({
            fieldLabel:"Business Date",
            name:"businessdate",
            format:"Y-m-d",//"Y-m-d",
            allowBlank:false,
            value:new Date(),
            width:200
        });
        this.storeCmbRecord = new Wtf.data.Record.create([
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
        this.storeCmbStore = new Wtf.data.Store({
            url :"INVStore/getStoreList.do",
            baseParams:{
                isActive:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : 'Store*',
            hiddenName : 'store',
            store : this.storeCmbStore,
            editable: true,
            typeAhead: true,
            forceSelection: true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width: 150,
            triggerAction: 'all',
            emptyText:'Select store...',
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });
        this.storeCmbStore.load();
        this.storeCmbStore.on("load", function(ds, rec, o) {
            if(rec.length > 0) {
                var val = "";
                // if(!(Wtf.realroles[0] == 5 || Wtf.realroles[0] == 17 || Wtf.realroles[0] == 18)) {
                this.storeCmbStore.insert(0, new this.storeCmbRecord({
                    store_id: "",
                    fullname: "All"
                }));
                // } else {
                //     val = rec[0].data.id;
                // }
                this.storeCmbfilter.setValue(val, true);
                this.initloadgridstore(this.frmDate.getValue().format("Y-m-d"), this.toDate.getValue().format("Y-m-d"), val);
            }
        }, this);
        this.search = new Wtf.Button({
            anchor: '90%',
            text: 'Search',
            iconCls: 'pwnd editicon',
            scope: this,
            handler: function() {
                var monthlyreportcheck=false;
                var action = (monthlyreportcheck)? getMultiMonthCheck(this.frmDate, this.toDate, 0) : 4;
                switch(action) {
                    case 4:
                        this.status = this.status == 1 ? 0 : 1;
                        var format = "Y-m-d";
                        this.loadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format));
                        break;
                    case 6:
                        multiMonthConfirmBox(45, this);
                        break;
                    default:
                        break;
                }
            }
        });
        this.aprove = new Wtf.Button({
            anchor: '90%',
            text: 'Aprove',
            iconCls:'pwnd Editicon',
            scope: this,
            handler: function() {
                Wtf.MessageBox.confirm("Confirm","Are you sure you want to approve the selected records?", function(btn){
                    if(btn == 'yes') {
                        this.Aprove(false);
                    }else
                        return;
                },this);
            }
        });
        this.reject = new Wtf.Button({
            anchor: '90%',
            text: 'Reject',
            iconCls:'pwnd rejecticon caltb',
            scope: this,
            handler: function() {
                Wtf.MessageBox.confirm("Confirm","Are you sure you want to reject the selected records?", function(btn){
                    if(btn == 'yes') {
                        this.Reject(false);
                    }else
                        return;
                },this);
            }
        });
        var tbarArray= new Array();
        tbarArray.push("-", "From Date: ", this.frmDate, "-", "To Date: ", this.toDate,"-", "Store: ",this.storeCmbfilter, "-",
            this.search);
        var bbarArray= new Array();
        // if(checktabperms(9, 6) == "edit")
        {
            bbarArray.push(this.aprove);
            bbarArray.push(this.reject);
        }
        
        this.StockPendingGrid = new Wtf.KwlEditorGridPanel({
            sm:this.sm,
            cm:this.StockPendingColumn,
            region:"center",
            id:"stockpending"+this.id,
            store:this.StockPendingStore,
            displayInfo:true,
            viewConfig:{
                forceFit:true 
            },
            //  qsWidth:100,
            searchLabel:"Quick Search",
            searchLabelSeparator:":",
            searchEmptyText:"Search By Product ID,Product Name ",
            serverSideSearch:true,
            searchField:"productCode",
            loadMask : true,
            tbar:tbarArray,
            bbar:
            [bbarArray]

        });
    },
    loadgridstore:function(frm, to){
        
        this.StockPendingStore.baseParams = {
            frmDate: frm,
            toDate: to,
            storeid: this.storeCmbfilter.getValue()
        }
        this.StockPendingStore.load({
            params:{
                start: 0,
                ss:this.StockPendingGrid.quickSearchTF.getRawValue(),
                limit:30
            }
        });
    },
    initloadgridstore:function(frm, to, storeid) {
        this.StockPendingStore.baseParams = {
            frmDate: frm,
            toDate: to,
            storeid: storeid
        }
    },
    Aprove:function (isDraft){
        
        var str = "";
        var modRecs= this.sm.getSelections(); 
        //WtfComMsgBox(["Alert", "Quantity cannot be 0."],3);
        
        var cnt = modRecs.length;
        for(var i = 0; i < cnt; i++) {
            var rec = modRecs[i];
            
            if(i==0){
                str=rec.data.id;
            }else{
                str += ","+rec.data.id;
            }
            
        }
       
        var allowNegInv=false;
        if(this.allowNegativeInventory != undefined || this.allowNegativeInventory != ""){
            allowNegInv=this.allowNegativeInventory;
        }
        this.allowNegativeInventory="";
            
        Wtf.Ajax.timeout = 300000;
        Wtf.Ajax.requestEx({
            url:"INVStockAdjustment/approveStockAdjustment.do",
            params: {
                records:str,
                allowNegativeInventory : allowNegInv
            }
        },
        this,
        function(result, req){
            
            var msg = ""
            
            var title="Error";
            if(result.success){
                title="Success";
                msg=result.msg;
                                
                WtfComMsgBox([title,msg],0);
                this.draftId = "";
                var format = "Y-m-d";
                this.loadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format));
                                
            }
            else if(result.success==false && (result.currentInventoryLevel != undefined && result.currentInventoryLevel != "")){
                
                if(result.currentInventoryLevel=="warn"){

                    Wtf.MessageBox.confirm("Confirm",result.msg, function(btn){
                        if(btn == 'yes') {        
                            this.allowNegativeInventory=true;
                            this.Aprove(false);
                        }else if(btn == 'no') {
                            this.allowNegativeInventory=false;
                        }
                    },this);
                }
                    
                if(result.currentInventoryLevel=="block"){
                    Wtf.MessageBox.show({
                        msg: result.msg,
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK,
                        title:"Warning"
                    });
                }
                 
            }
                        
                     
                    
        },
        function(result, req){
            WtfComMsgBox(["Failure", "Error occurred while approving data."],3);
            var format = "Y-m-d";
            this.loadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format));
        });
    },

    Reject:function (isDraft){
       
        var str = "";
        var modRecs= this.sm.getSelections(); 
        
        var cnt = modRecs.length;
        for(var i = 0; i < cnt; i++) {
            var rec = modRecs[i];
            
            if(i==0){
                str=rec.data.id;
            }else{
                str += ","+rec.data.id;
            }
            
        }
       
       
        Wtf.Ajax.timeout = 300000;
        Wtf.Ajax.requestEx({
            url:"INVStockAdjustment/rejectStockAdjustment.do",
            params: {
                records:str
            }
        },
        this,
        function(result, req){
            
            var msg=result.msg;
            
            var title="Error";
            
            if(result.success){
                title="Success";
               
                WtfComMsgBox([title,msg],0);
                this.draftId = "";
                var format = "Y-m-d";
                this.loadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format));
                                
            }
            else if(result.success==false){
                title="Error";
                WtfComMsgBox([title,msg],0);
            }
                        
                     
                    
        },
        function(result, req){
            WtfComMsgBox(["Failure", "Error occurred while rejecting data."],3);
            var format = "Y-m-d";
            this.loadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format));
        });
       
    }
});

//_______________________________Rejected List______________________________________________________________

Wtf.markoutRejectedGrid = function (config){
    Wtf.apply(this,config);
    Wtf.markoutRejectedGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.markoutRejectedGrid,Wtf.Panel,{
    initComponent:function (){
        Wtf.markoutRejectedGrid.superclass.initComponent.call(this);
        this.getEditorGrid();
        this.tmpPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.StockRejectGrid
            ]
        });
        this.add(this.tmpPanel);
        this.on("activate",function(){
            this.loadgridstore(this.frmDate.getValue().format("Y-m-d"), this.toDate.getValue().format("Y-m-d"));
        },this);
    },
    
    getEditorGrid:function (){
        
        this.fromdateVal =new Date().getFirstDateOfMonth();
        this.todateVal=new Date().getLastDateOfMonth();
        //        for(var i=0;i<Wtf.closingDates.length-1;i++){
        //            var curtrec=Wtf.closingDates[i];
        //            var nextrec=Wtf.closingDates[i+1];
        //            var prevrec=Wtf.closingDates[i-1];
        //            if(curtrec.date == this.fromdateVal.format('Y-m-d')){
        //                this.fromdateVal= Date.parseDate(prevrec.date, "Y-m-d").add(Date.DAY,1).format("Y-m-d");
        //                this.todateVal = curtrec.date;
        //                break;
        //            }else if((curtrec.date < this.fromdateVal.format('Y-m-d')) && (nextrec.date > this.fromdateVal.format('Y-m-d'))) {
        //                this.fromdateVal= Date.parseDate(curtrec.date, "Y-m-d").add(Date.DAY,1).format("Y-m-d");
        //                this.todateVal = nextrec.date;
        //                break;
        //            }
        //        } 

        this.frmDate = new Wtf.form.DateField({
            emptyText:'From date...',
            readOnly:true,
            width : 100,
            value:this.fromdateVal,
            name : 'frmdate',
            //minValue: Wtf.archivalDate,
            format: "Y-m-d"//"Y-m-d"
        });
        this.toDate = new Wtf.form.DateField({
            emptyText:'To date...',
            readOnly:true,
            width : 100,
            name : 'todate',
            value:this.todateVal,
            //minValue: Wtf.archivalDate,
            format: "Y-m-d"//"Y-m-d"
        });
        this.dateField1 = new Wtf.form.DateField({
            fieldLabel:"Business Date",
            name:"businessdate",
            format:"Y-m-d",//"Y-m-d",
            allowBlank:false,
            value:new Date(),
            width:200
        });
    
        this.StockRejectRec  = new Wtf.data.Record.create([
        {
            "name":"id"
        },

        {
            "name":"store_id"
        },

        {
            "name":"storeAbbr"
        },

        {
            "name":"storeDesc"
        },

        {
            "name":"productCode"
        },

        {
            "name":"productName"
        },

        {
            "name":"quantity"
        },

        {
            "name":"uomName"
        },
        {
            "name":"createdBy"
        },
        {
            "name":"markouttype"
        },

        {
            "name":"reason"
        },

        {
            "name":"costcenter"
        },

        {
            "name":"cost"
        },

        {
            "name":"amount"
        },

        {
            "name":"date", 
            "type":"date", 
            "format":"Y-m-d"
        },

        {
            "name":"type"
        },

        {
            "name":"remark"
        },

        {
            "name":"partnumber"
        },

        {
            "name":"seqNumber"
        },
        {
            "name":"locationname"
        },
        {
            "name":"locationid"
        },
        {
            "name":"status"
        }
        ]);

        this.StockRejectReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.StockRejectRec );

        this.StockRejectStore = new Wtf.data.Store({

            url:"INVStockAdjustment/getRejectedStockAdjustmentList.do",
            baseParams: {
                frmDate:this.frmDate.getValue().format("Y-m-d"),
                toDate: this.toDate.getValue().format("Y-m-d")
            },
            reader:this.StockRejectReader

        });
        //        this.StockRejectStore.load();
        this.sm = new Wtf.grid.CheckboxSelectionModel({});
     
        this.StockRejectColumn = new Wtf.grid.ColumnModel([  this.sm,new Wtf.grid.RowNumberer(),
           
        {
                header: "Stock Adjustment No",
                dataIndex: 'seqNumber',
                groupable: false,
                width:200
            },
            {
                header: "Store",
                dataIndex: 'storeAbbr',
                width:200
            },
            {
                header:"Location",
                sortable:false,
                dataIndex:"locationname",
                width:200
            },
            {
                header: "Created By",
                dataIndex: 'createdBy',
                width:200
            },
            {
                header: "Business Date",
                dataIndex: 'date',
                renderer: function(v){
                    var date = v;
                    if(date != undefined && date != ""){
                        date = date.format("Y-m-d");
                    }
                    return date;
                },
                width:200
            },
            {
                header: "Product ID",
                dataIndex: 'productCode',
                width:200
            },
            {
                header: "Product Name",
                dataIndex: 'productName',
                width:200
            },
            {
                header: "CoilCraft Part No",
                dataIndex: 'partnumber',
                groupable: false,
                sortable:false,
                hidden:true
            //hidden: true//integrationFeatureFor == Wtf.IF.COILCRAFT ? false: true
            },
            {
                header: "Quantity",
                dataIndex: 'quantity',
                sortable:false,
                align: 'right',
                summaryType: 'sum',
                renderer:function(val){
                    // return WtfGlobal.getCurrencyFormatWithoutSymbol(val, Wtf.companyPref.quantityDecimalPrecision);
                    return val;
                },
                width:200
            },
            {
                header:'Amount',
                dataIndex: 'cost',
                align: 'right',
                sortable:false,
                summaryType: 'sum',
                renderer:function(val){
                    // return WtfGlobal.getCurrencyFormatWithoutSymbol(val,2);
                    return val;
                },
                width:200
            },
            {
                header: "UoM",
                dataIndex: 'uomName',
                width:200
            },
            {
                header:"Reason",
                dataIndex:"reason",
                width:200,
                hidden:true
            
            },
            {
                header:"Cost Center",
                dataIndex:"costcenter",
                width:200,
                hidden:false
            },
            {
                header:"Remark",
                dataIndex:"remark",
                width:200,
                hidden:false
           
            },
            {
                header:"Status",
                dataIndex:"status",
                width:200,
                hidden:false,
                renderer:function(value){
                    //if(value == "rejected"){
                    return "<div style='color:red'>Rejected</div>";
                // } 
                }
           
            }
  
            ]);
           
       
        this.storeCmbRecord = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },
        {
            name:"fullname"  
        },
        {
            name: 'description'
        },

        {
            name: 'analysiscode'
        },

        {
            name: 'abbrev'
        },

        {
            name: 'dmflag'
        }
        ]);
        this.storeCmbStore = new Wtf.data.Store({
            url :"INVStore/getStoreList.do",
            baseParams:{
                isActive:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.storeCmbRecord)
        });
        this.storeCmbfilter = new Wtf.form.ComboBox({
            fieldLabel : 'Store*',
            hiddenName : 'store',
            store : this.storeCmbStore,
            editable: true,
            typeAhead: true,
            forceSelection: true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width: 150,
            triggerAction: 'all',
            emptyText:'Select store...'
        });
        this.storeCmbStore.load();
        this.storeCmbStore.on("load", function(ds, rec, o) {
            if(rec.length > 0) {
                var val = "";
                // if(!(Wtf.realroles[0] == 5 || Wtf.realroles[0] == 17 || Wtf.realroles[0] == 18)) {
                this.storeCmbStore.insert(0, new this.storeCmbRecord({
                    store_id: "",
                    abbrev: "",
                    fullname: "All"
                }));
                //  } else {
                //      val = rec[0].data.id;
                //  }
                this.storeCmbfilter.setValue(val, true);
                this.initloadgridstore(this.frmDate.getValue().format("Y-m-d"), this.toDate.getValue().format("Y-m-d"), val);
            }
        }, this);
        this.search = new Wtf.Button({
            anchor: '90%',
            text: 'Search',
            iconCls: 'pwnd editicon',
            scope: this,
            handler: function() {
                var monthlyreportcheck=false;
                var action = (monthlyreportcheck)? getMultiMonthCheck(this.frmDate, this.toDate, 0) : 4;
                switch(action) {
                    case 4:
                        this.status = this.status == 1 ? 0 : 1;
                        var format = "Y-m-d";
                        this.loadgridstore(this.frmDate.getValue().format(format), this.toDate.getValue().format(format));
                        break;
                    case 6:
                        multiMonthConfirmBox(45, this);
                        break;
                    default:
                        break;
                }
            }
        });
        this.aprove = new Wtf.Button({
            anchor: '90%',
            text: 'Aprove',
            iconCls:'pwnd Editicon',
            scope: this,
            handler: function() {
                Wtf.MessageBox.confirm("Confirm","Are you sure you want to approve selected records?", function(btn){
                    if(btn == 'yes') {
                        this.Aprove(false);
                    }else
                        return;
                },this);
            }
        });
        this.reject = new Wtf.Button({
            anchor: '90%',
            text: 'Reject',
            iconCls:'pwnd Editicon',
            scope: this,
            handler: function() {
                Wtf.MessageBox.confirm("Confirm","Are you sure you want to reject selected records?", function(btn){
                    if(btn == 'yes') {
                        this.Reject(false);
                    }else
                        return;
                },this);
            }
        });
        
        var tbarArray= new Array();
        tbarArray.push("-", "From Date: ", this.frmDate, "-", "To Date: ", this.toDate,"-", "Store: ",this.storeCmbfilter, "-",
            this.search);
            
        this.pagingToolbar = new Wtf.PagingToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.StockRejectStore,
            displayInfo: true,
            emptyMsg: "No data to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            
            })
            
        });
       
        this.StockRejectGrid = new Wtf.KwlEditorGridPanel({
            sm:this.sm,
            cm:this.StockRejectColumn,
            region:"center",
            id:"editorgrid"+this.id,
            store:this.StockRejectStore,
            searchLabel:"Quick Search",
            searchLabelSeparator:":",
            searchEmptyText:"Search By Product ID,Product Name ",
            serverSideSearch:true,
            searchField:"productCode",
            viewConfig:{
                forceFit:true 
            },
            loadMask : true,
            tbar:tbarArray,
            bbar:this.pagingToolbar

        });
            
    },
    loadgridstore:function(frm, to){
        
        this.StockRejectStore.baseParams = {
            frmDate: frm,
            toDate: to,
            storeid: this.storeCmbfilter.getValue()
        }
        this.StockRejectStore.load({
            params:{
                start: 0,
                ss:this.StockRejectGrid.quickSearchTF.getRawValue(),
                limit:30
            }
        });
    },
    initloadgridstore:function(frm, to, storeid) {
        this.StockRejectStore.baseParams = {
            frmDate: frm,
            toDate: to,
            storeid: storeid
        }
    } 
});


function printoutSA(stockAdjustmentNo){
    
    var URL="INVStockAdjustment/getStockAdjustmentDetailBySequenceNo.do"; 

    var printTitle = "Stock Adjustment Note";
    
    Wtf.Ajax.requestEx({
        url:URL,
        params: {
            sequenceNo : stockAdjustmentNo
        }
    },
    this,
    function(result, req){

        var msg=result.msg;
        var title="Error";
        if(result.success){
            var x=result.success;
            var rs=result.data;
            
            var cnt = rs.length;
            var isBatchDataPresent=false;
            var isSerialDataPresent=false;
            var isSKUDataPresent=false;
            var isLocationDataPresent=false;
            
            for(var i=0;i<cnt;i++){
                
                var saDetail=result.data[i].stockDetails;
                
                for(var sad=0; sad < saDetail.length; sad++){
                    
                    if(saDetail[sad].batchName != undefined && saDetail[sad].batchName != "" && isBatchDataPresent == false){
                        isBatchDataPresent=true;
                    }
                    
                    if(saDetail[sad].serialNames != undefined && saDetail[sad].serialNames != "" && isSerialDataPresent == false){
                        isSerialDataPresent=true;
                    }
                    
                    if(saDetail[sad].skuNames != undefined && saDetail[sad].skuNames != "" && isSKUDataPresent == false){
                        isSKUDataPresent=true;
                    }
                    
                    if(saDetail[sad].locationName != undefined && saDetail[sad].locationName != "" && isLocationDataPresent == false){
                        isLocationDataPresent=true;
                    }
                }
            
            }

            var htmlString = "<html>"
            + "<title>" + printTitle + "</title>"
            + "<head>"
            + "<STYLE TYPE='text/css'>"
            + "<!--"
//            + "TD{font-family: Calibri,Arial;}"
            + "--->"
            + "</STYLE>"
            + "</head>"
            + "<body style='font-size: 12px;'>"
            + "<table><thead><tr><th>"
            + "<h2 align = 'center' style='font-size:18px; font-family:sans-serif,Calibri; padding: 0% 2% 2% 2%;'> " + printTitle + " </h2>"
    
            +"<center><table cellspacing=0 border=0 cellpadding=2 width='100%' style='font-size:12px; table-layout:fixed;word-wrap:break-word;'>"
            +"<tr style='font-size:12px;'><td style='width:10%;font-size:12px;'><b>Store</b></td><td style='width:3%;font-size:12px;'><b> : </b></td> <td style='width:40%;font-size:12px;'>"+ result.data[0].storeAbbr +"</td><td style='width:10%;font-size:12px;'><b>Doc No.</b></td><td style='width:3%;font-size:12px;'><b> : </b></td> <td style='width:34%;font-size:12px;'>"+result.data[0].seqNumber+"</td></tr>"
            +"<tr style='font-size:12px;'><td style='width:10%;font-size:12px;'><b>Remarks</b></td><td style='width:3%;font-size:12px;'><b> : </b></td><td style='width:40%;font-size:12px;'>"+ result.data[0].memo +"</td><td style='width:10%;font-size:12px;'><b>Date </b></td><td style='width:3%;font-size:12px;'><b>: </b></td> <td style='width:34%;font-size:12px;'>"+result.data[0].date+"</td></tr>"
            +"<tr style='font-size:12px;'><td style='width:10%;font-size:12px;'><b> </b></td><td style='width:3%;font-size:12px;'><b> </b></td><td style='width:40%;font-size:12px;'><b> </b></td><td style='width:10%; font-size:12px;'><b>Created By</b></td> <td style='width:3%; font-size:12px;'><b>:</b></td> <td style='width:34%;font-size:12px;'> "+result.data[0].createdBy+"</td></tr>"
            +"<tr style='font-size:12px;'><td style='width:10%;font-size:12px;'><b> </b></td><td style='width:3%;font-size:12px;'><b> </b></td><td style='width:40%;font-size:12px;'><b> </b></td><td style='width:10%; font-size:12px;'><b>Purpose</b></td><td style='width:3%; font-size:12px;'><b> : </b></td><td style='width:34%; font-size:12px;'>"+result.data[0].adjustmentReason+"</td></tr>"
            +"</table></center>"
            +"</th></tr></thead><tbody><tr><td style='padding-top:1cm;'>"

            + "<div style='margin-top:0cm;width: 100%;'>";
                
//            htmlString += "<br/><br style='clear:both'/><br/>";
//            var pgbrkstr1 = "<DIV style='page-break-after:always'></DIV>";
            var pgbrkstr1 = "";

            if (i != 0) {
//                htmlString += "<br/><br/></br>";
            }
            htmlString += "<center>";
            htmlString += "<table  cellspacing=0 border=0 cellpadding=2 width='99%' border='0' style='word-wrap:break-word;table-layout: fixed; border-collapse: separate;'>";
//            htmlString += "<col width='3'><col width='8'><col width='17'><col width='5'><col width='8'><col width='8'><col width='12'><col width='7'><col width='8'><col width='8'><col width='5'><col width='11'> ";
            htmlString += "<thead><tr style='font-size:12px;'>"
            +"<th style='width: 3%; border-left:1px solid;border-bottom:1px solid;border-collapse: collapse !important; border-top:1px solid;' align='left'> </th>"
            +"<th style='width: 9%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Product ID</th>"
//            +"<th>Product Name</th>"
            +"<th style='width: 16%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Description</th>"
            +"<th style='width: 5%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Adj Type</th>"
            htmlString += "<th style='width: 5%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Qty</th>";
            htmlString += "<th style='width: 8%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Location</th>" ;
            htmlString += "<th style='width: 12%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Batch No. / Serial No.</th>";
//            htmlString += (isBatchDataPresent == true  && isSerialDataPresent==false ? "<th>Exp. Date</th>"  : "" );
//            htmlString += "<th></th>";
            htmlString += "<th style='width: 5%; border-bottom:1px solid; border-collapse: collapse !important;  border-top:1px solid;' align='left'>Sub Qty</th>";
            htmlString += "<th style='width: 10%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Asset #</th>";
            htmlString += "<th style='width: 8%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Exp. Date</th>";
            htmlString += "<th style='width: 5%; border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>CC</th>";
//            htmlString += "<th>Unit Price</th>";
            htmlString += "<th style='width: 19%; border-right:1px solid;border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>Remark</th>";
           
            
             var linedata = [];
            linedata = WtfGlobal.appendCustomColumn(linedata, GlobalColumnModel[Wtf.Inventory_Stock_Adjustment_ModuleId]);
            for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
                if (linedata[lineFieldCount].header != undefined && linedata[lineFieldCount].header != "") {
                    htmlString += "<th style='width: 20%; border-right:1px solid;border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>" + linedata[lineFieldCount].header + "</th>";
                }
            }
            var globaldata = [];
            globaldata = WtfGlobal.appendCustomColumn(globaldata, GlobalDimensionCustomFieldModel[Wtf.Inventory_Stock_Adjustment_ModuleId]);
            for (var lineFieldCount = 0; lineFieldCount < globaldata.length; lineFieldCount++) {
                if (globaldata[lineFieldCount].header != undefined && globaldata[lineFieldCount].header != "") {
                    htmlString += "<th style='width: 20%; border-right:1px solid;border-bottom:1px solid; border-collapse: collapse !important; border-top:1px solid;' align='left'>" + globaldata[lineFieldCount].header + "</th>";
                }
            }
                      
            
            htmlString+="</tr></thead><tbody>";
            var count=1;
            var totalStockIn = 0;
            var totalStockOut = 0;
            for(var i=0;i<cnt;i++){
                
                var saDetail=result.data[i].stockDetails;
                var qtyDtl="";
                var batchDtl="";
                var serialDtl="";
                var locationDtl="";
                var skuDtl="";
                var expdateDtl="";
                var costCenter="";
                var batchExpdateDtl="";
                var TransactionType=result.data[i].adjustmentType =="Stock IN"?"IN":"OUT";
                
                if(result.data[i].adjustmentType =="Stock IN"){
                    TransactionType="IN";
                }else if(result.data[i].adjustmentType =="Stock Out"){
                    TransactionType="OUT";
                }else{
                    TransactionType="SALES";
                }
                
                if(result.data[i].costcenter != undefined){
                    costCenter = result.data[i].costcenter;
                }
                
                for(var sad=0; sad < saDetail.length; sad++){
                    
                    qtyDtl += Math.abs(saDetail[sad].quantity) + " " + result.data[i].uomName ;
                    qtyDtl += (((sad != saDetail.length-1 && (saDetail[sad].quantity=="" || saDetail[sad].quantity=="")) || sad == saDetail.length-1) ? "" : "<hr noshade=\"noshade\" style=\"color:grey; \">");
                    
                    batchDtl += saDetail[sad].batchName ;
                    batchDtl += (((sad != saDetail.length-1 && (saDetail[sad].batchName=="" || saDetail[sad].batchName=="")) || sad == saDetail.length-1) ? "" : "<hr noshade=\"noshade\" style=\"color:grey; \">");
                    
//                    batchExpdateDtl += saDetail[sad].batchExpDate ;
//                    batchExpdateDtl += (((sad != saDetail.length-1 && saDetail[sad].batchExpDate=="") || sad == saDetail.length-1) ? "" : "<hr/>");
                    
                    serialDtl+= saDetail[sad].serialNames ;
                    serialDtl+= (((sad != saDetail.length-1 && (saDetail[sad].serialNames=="" || saDetail[sad].serialNames==undefined)) || sad == saDetail.length-1) ? "" : "<hr noshade=\"noshade\" style=\"color:grey; \">");
                    
                    locationDtl+= saDetail[sad].locationName ;
                    locationDtl+= (((sad != saDetail.length-1 && (saDetail[sad].locationName=="" || saDetail[sad].locationName==undefined)) || sad == saDetail.length-1) ? "" : "<hr noshade=\"noshade\" style=\"color:grey; \">");
                     
                    skuDtl+= saDetail[sad].skuNames ;
                    skuDtl+= (((sad != saDetail.length-1 && (saDetail[sad].skuNames=="" || saDetail[sad].skuNames==undefined)) || sad == saDetail.length-1) ? "" : "<hr noshade=\"noshade\" style=\"color:grey; \">");
                    
                    expdateDtl+= saDetail[sad].expiryDates!=undefined?saDetail[sad].expiryDates:"" ;
                    expdateDtl+= (((sad != saDetail.length-1 && (saDetail[sad].expiryDates=="" || saDetail[sad].expiryDates==undefined)) || sad == saDetail.length-1) ? "" : "<hr noshade=\"noshade\" style=\"color:grey; \">");
                }
                expdateDtl=(expdateDtl!=undefined&&expdateDtl!=""&&expdateDtl!="undefined")?expdateDtl:"";
        
                htmlString += "<tr style='font-size:12px;'>"+
                "<td style='border-left:1px solid;border-bottom:1px solid;border-collapse: collapse !important;' align='left'>" + count + "&nbsp;</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + result.data[i].productCode + "&nbsp;</td>"+
//                "<td align='center'>" + result.data[i].productName + "&nbsp;</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + result.data[i].productDesc + "&nbsp;</td>"+
                //                "<td align='center'>" + (result.data[i].hscode == undefined  ? "" : result.data[i].hscode) + "&nbsp;</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + TransactionType + "&nbsp;</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + Math.abs(result.data[i].quantity) +" "+result.data[i].uomName+"&nbsp;</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + locationDtl + "</td>"+
                "<td style='border-bottom:1px solid;border-collapse: collapse !important;' align='left'>" + batchDtl+serialDtl+"</td>"+
//                (isBatchDataPresent == true && isSerialDataPresent==false ? "<td align='center'>" + batchExpdateDtl+ "</td>"  : "" )+
//                "<td align='center'>" +  + "</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + qtyDtl+ "&nbsp;</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + skuDtl + "</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + expdateDtl + "</td>"+
                "<td style='border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + costCenter +"&nbsp;</td>"+
                
//                "<td align='center'>" + Math.abs(result.data[i].cost) +"&nbsp;</td>"+
                "<td style='border-right:1px solid;border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + result.data[i].remark +"&nbsp;</td>";
                
                for (var lineFieldCount = 0; lineFieldCount < linedata.length; lineFieldCount++) {
                    if (linedata[lineFieldCount].header != undefined && result.data[i][linedata[lineFieldCount].dataIndex] != undefined) {
                        htmlString += "<td style='border-right:1px solid; border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + result.data[i][linedata[lineFieldCount].dataIndex] + "</td>";
                    }else{
                        htmlString += "<td style='border-right:1px solid; border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + "" + "</td>";
                    }
                }
                 for (var lineFieldCount = 0; lineFieldCount < globaldata.length; lineFieldCount++) {
                    if (globaldata[lineFieldCount].header != undefined && result.data[i][globaldata[lineFieldCount].dataIndex] != undefined) {
                        htmlString += "<td style='border-right:1px solid; border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + result.data[i][globaldata[lineFieldCount].dataIndex] + "</td>";
                    }else{
                        htmlString += "<td style='border-right:1px solid; border-bottom:1px solid; border-collapse: collapse !important;' align='left'>" + "" + "</td>";
                    }
                }
                htmlString +="</tr>";
                count++;
                if(result.data[i].adjustmentType === "Stock IN"){
                    totalStockIn += Math.abs(result.data[i].quantity);
                }else if(result.data[i].adjustmentType === "Stock Out" || result.data[i].adjustmentType === "Stock Sales"){
                    totalStockOut += Math.abs(result.data[i].quantity);
                }
            }
            
            htmlString += "</tbody></table>";
            htmlString += "</center><br><br>";
            htmlString +="<center><table cellspacing=0 border=0 cellpadding=2 width='95%'>"
            +"<tr><td style='width:85%;text-align:right;font-size:12px;'><b>Total IN : </b></td><td style='width:15%;text-align:right;font-size:12px;'>"+getRoundofValueWithValues(totalStockIn,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</td></tr>"
            +"<tr><td style='width:85%;text-align:right;font-size:12px;'><b>Total OUT : </b></td><td style='width:15%;text-align:right;font-size:12px;'>"+getRoundofValueWithValues(totalStockOut,Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"</td></tr>"
            +"</table></center>";
         
            if (i != cnt - 1) {
                htmlString += pgbrkstr1;
            }

            htmlString += 
            //        "<div>"
            //    + "<span style='width:270px; padding: 3%; float: left;text-align:left'><b>Prepared By </b><br></br>Sign:</br>Name:&nbsp;&nbsp;&nbsp;&nbsp;" +createdBy + "</br></br></span>"
            //    + "<span style='width:270px; padding: 3%; float: right;text-align:left'><b>Collected By </b></br></br>Sign:</br>Name:&nbsp;&nbsp;&nbsp;&nbsp;" + collectedBy + "</br></br></span>"
            //    + "</div><br style='clear:both'/>"
            "<div style='float: right; padding-top: 3px; padding-right: 5px;'>"
//            + "<button id = 'print' title='Print Note' onclick='window.print();' style='color: rgb(8, 55, 114);' href='#'>Print</button>"
            + "</div>"
            + "</div></td></tr></tbody></table>"
            + "</body>"
            + "</html>";
            +"<style>@media print {button#print{display:none;}}</style>";
            var disp_setting="toolbar=yes,location=no,";
            disp_setting+="directories=yes,menubar=yes,";
            disp_setting+="scrollbars=yes,width=650, height=600, left=100, top=25";
            var content_value ="";
            var docprint=window.open("","",disp_setting);
            docprint.document.open();
            docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
            docprint.document.write('"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">');
            docprint.document.write('<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">');
            docprint.document.write('<head><title></title>');
            docprint.document.write('<style type="text/css">body{ margin:0px;');
            docprint.document.write('font-family:sans-serif,Calibri,Arial;color:#000;');
            docprint.document.write('font-family:sans-serif,Calibri,Arial;font-size:12px;} ');
            docprint.document.write('a{color:#000;text-decoration:none;} @page { margin-top:1cm; margin-bottom:1cm; margin-right:1cm; margin-left:1.5cm; } ');
            docprint.document.write('thead {display: table-header-group;} tfoot {display: table-footer-group;} tbody {display: table-row-group;} </style>');
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
