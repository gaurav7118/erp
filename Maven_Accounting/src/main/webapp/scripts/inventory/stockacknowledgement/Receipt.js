function showStockReceiptSIP(){

    var stockreceiptTab = Wtf.getCmp("stockreceipt1");
    if(stockreceiptTab==undefined){
        stockreceiptTab = new Wtf.Receipt({
            id:"stockreceipt1",
            title:"Stock Acknowledgement",
            closable:true,
            layout:"fit",
            showNewReceipts:true,
            showStoreMapWin:true
        });

        Wtf.getCmp("as").add(stockreceiptTab);
        Wtf.getCmp("as").doLayout();
    }
    Wtf.getCmp("as").setActiveTab(stockreceiptTab);
}

Wtf.Receipt = function (config){
    Wtf.apply(this,config);
    Wtf.Receipt.superclass.constructor.call(this);
}

Wtf.extend(Wtf.Receipt,Wtf.Panel,{
    initComponent:function (){
        if(this.showStoreMapWin == undefined){
            this.showStoreMapWin = false;
        }
        
        Wtf.Receipt.superclass.initComponent.call(this);
        /* This component is used for Stockreceipt as well as Autoshipment
         * Identified by this.rendermode i.e.  rendermode = autoshipment or rendermode = receipt (Default = receipt)
         */
        this.reportType = 1;//1->detailed view  2->summary
        this.status = 0;
        if(this.rendermode != "autoshipment"){
            this.rendermode = "receipt";
        }
        this.getForm();
        this.getItemDetail();

        var bbarArray = [];
        //     if(this.showNewReceipts){
        this.submitBut= new Wtf.Button({
            text:"Submit",
            iconCls:getButtonIconCls(Wtf.etype.save),
            handler:function (){
//                this.SaveItem()
            },
            scope:this
        });

        this.summaryBtn = new Wtf.Button({
            anchor : '90%',
            text: 'View Summary',
            //hidden:(parseInt(Wtf.realroles[0])==5 || parseInt(Wtf.realroles[0])==17 || parseInt(Wtf.realroles[0])==18)?true:false,
            scope:this,
            handler:function(){
                if(this.status==0) {
                    this.summaryBtn.setText("View Details");
                    this.status = 1;
                    this.submitBut.disable();
                    this.reportType = 2;
//                    this.searchGrid();
                } else {
                    this.status = 0;
                    this.reportType = 1;
                    this.submitBut.enable();
                    this.summaryBtn.setText("View Summary");
//                    this.searchGrid();
                }
            }
        });
        
        if(this.showStoreMapWin){
            bbarArray.push(this.submitBut);
        }
        
//        if((integrationFeatureFor == Wtf.IF.SUSHITEI) && !this.showNewReceipts){
        if(!this.showNewReceipts){
            bbarArray.remove(this.submitBut);
        }
            
        //        if(!this.showNewReceipts){
        //            bbarArray.push(this.summaryBtn);
        //        }
        //      }else{
        if(!this.showNewReceipts){
            this.exportBut= new Wtf.Button({
                text:'Export To CSV',
                id:this.id+"csv",
                handler:function(){
                    if(this.ItemDetailGrid.EditorStore.getTotalCount() != 0){
                        var rectype = (this.showNewReceipts)?0:1;
                        if(this.rendermode=="autoshipment"){
                            Wtf.get('downloadframe').dom.src = "ExportDataServlet.jsp?mode=7&reportname=Autoshipment"+
                            "&start=0&limit="+this.ItemDetailGrid.EditorStore.getTotalCount()+
                            "&pstoreid="+this.fromstoreCombo.getValue()+
                            "&visited="+rectype+
                            "&ondate="+this.dateField.getRawValue()+
                            "&todate="+this.dateField2.getRawValue()+
                            "&reportType=" +this.reportType;
                        }else{
                            Wtf.get('downloadframe').dom.src = "ExportDataServlet.jsp?mode=6&reportname=Receipt"+
                            "&start=0&limit="+this.ItemDetailGrid.EditorStore.getTotalCount()+
                            "&pstoreid="+this.fromstoreCombo.getValue()+
                            "&visited="+rectype+
                            "&ondate="+this.dateField.getRawValue()+
                            "&todate="+this.dateField2.getRawValue()+
                            "&reportType=" +this.reportType;
                        }
                    } else {
                        msgBoxShow(["Error", "No records to export"], 0,1);
                        return;
                    }
                },
                scope:this
            });
            bbarArray.push(this.exportBut);
        }

        //        this.importTitle = (this.rendermode=="autoshipment"?"Autoshipment":"Stock Receipt");
        //        this.ImportButton= new Wtf.Button({
        //            text: "Import "+this.importTitle,
        //            scope: this,
        //            tooltip: {title:"Import", text:"Import "+this.importTitle},
        //            handler: function(){
        //              this.importItemForm =  new Wtf.importForm({
        //                   title:"Import "+this.importTitle,
        //                   importFor: this.importTitle,
        //                   width:500,
        //                   modal:true,
        //                   height: this.rendermode=="autoshipment"?380:275,
        //                   layout:"fit"
        //               });
        //               this.importItemForm.on("importsuccess",function(){
        //                     Wtf.MessageBox.show({
        //                        title: this.importTitle+' Imported Successfully',
        //                        msg: 'Please check & acknowledge all records from '+this.importTitle+ " Acknowledgement",
        //                        buttons: Wtf.MessageBox.OK,
        //                        animEl: 'mb9',
        //                        icon: Wtf.MessageBox.INFO
        //                    });
        //                    return false;
        //               });
        //               this.importItemForm.show();
        //            },
        //            iconCls: "pwnd importicon"
        //        });
        //        bbarArray.push(this.ImportButton);

        this.mainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
            this.Form,
            this.ItemDetailGrid
            ],
            bbar:bbarArray
        });

        this.add(this.mainPanel);

        this.fromstoreCombo.on("select",this.filterGrid,this);
        this.dateField.on("change",this.filterGrid1,this);

    },
    filterGrid:function(){
        if(!this.dateField.isValid()){
            this.dateField.markInvalid();
            return false;
        }
        var storeid = this.fromstoreCombo.getValue();

        if(storeid.length==0){
            this.fromstoreCombo.markInvalid();
            return false;
        }
        this.ItemDetailGrid.EditorStore.rejectChanges();
//        this.setBusinessDate();
    },
    filterGrid1:function(){
        if(!this.dateField.isValid()){
            this.dateField.markInvalid();
            return false;
        }
        this.dateField2.setValue(this.dateField.getValue());
        var filterdate = this.dateField.getRawValue();
        var storeid = this.fromstoreCombo.getValue();

        if(storeid.length==0){
            this.fromstoreCombo.markInvalid();
            return false;
        }
        this.ItemDetailGrid.EditorStore.rejectChanges();
        if(this.showNewReceipts){
//            this.ItemDetailGrid.EditorStore.load({
//                params:{
//                    pstoreid: storeid,
//                    ondate : filterdate
//                }
//            });
        }

    },
    searchGrid: function() {
        if(!this.dateField.isValid()){
            this.dateField.markInvalid();
            return false;
        }
        var filterdate = this.dateField.getRawValue();
        var filterdate2 = this.dateField2.getRawValue();
        var storeid = this.fromstoreCombo.getValue();

        var action = getMultiMonthCheck(this.dateField, this.dateField2, 1);
        switch(action) {
            case 4:
                if(storeid.length == 0) {
                    this.fromstoreCombo.markInvalid();
                    return false;
                }
                this.ItemDetailGrid.EditorStore.rejectChanges();
//                this.ItemDetailGrid.EditorStore.load({
//                    params: {
//                        pstoreid: storeid,
//                        ondate: filterdate,
//                        todate: filterdate2,
//                        reportType: this.reportType
//                    }
//                });
                break;
            default:
                break;
        }
    },
    getForm:function (){
        this.storeRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        },

        {
            name:'storeid'
        },

        {
            name:'storecode'
        },

        {
            name:'store_id'
        },

        {
            name:"description"
        }
        ]);

        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        var jspurl = "store.jsp";
        var disfield = "name";

//        if(!(Wtf.realroles[0]==5||Wtf.realroles[0]==17||Wtf.realroles[0]==18||Wtf.realroles[0]==6)){
//            jspurl = "inventory.jsp";
//            disfield = "description";
//        }
        this.Store = new Wtf.data.Store({
            url:"jspfiles/inventory/"+jspurl,
            reader:this.storeReader,
            sortInfo: {
                field: disfield,
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            }
        });

//        if(Wtf.realroles[0]==5||Wtf.realroles[0]==17||Wtf.realroles[0]==18||Wtf.realroles[0]==6){
//            this.Store.load({
//                params:{
//                    flag:14,
//                    action:'frm'
//                }
//            });
//        }else{
//            this.Store.load({
//                params:{
//                    flag:2,
//                    type:"T0T1"
//                }
//            });
//        }

        this.Store.on("load", function(ds, rec, o){
            if(rec.length > 0){
                this.fromstoreCombo.setValue(rec[0].data.id, true);
                this.filterGrid();
            }
        }, this);

        this.fromstoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.Store,
            displayField:disfield,
            valueField:"id",
            fieldLabel:"Store*",
            emptyText:"Select a store",
            hiddenName:"fromstore",
            width:200,
            listWidth:200
        });
        this.dateField = new Wtf.form.DateField({
            fieldLabel:this.showNewReceipts?"Date*":"From Date",
            format:"Y-m-d",
            name:"datedon",
            disableKeyFilter:true,
            minValue: Wtf.archivalDate,
            width:200,
            value:new Date()
        });
        this.formVendorRec = new Wtf.data.Record.create([
        {
            name: 'id', 
            type: 'string'
        },

        {
            name: 'name', 
            type: 'string'
        },

        {
            name: 'address', 
            type: 'string'
        },

        {
            name: 'shipterm', 
            type: 'string'
        },

        {
            name: 'payterm', 
            type: 'string'
        },

        {
            name: 'status'
        }
        ]);

        this.formVendorStore = new Wtf.data.Store({
            url: 'jspfiles/inventory/inventory.jsp?flag=152&companyid='+companyid,
            reader: new Wtf.data.KwlJsonReader({
                root:"data"
            }, this.formVendorRec),
            sortInfo:{
                field: "name", 
                direction: "ASC"
            }
        });

        this.formVendorCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.formVendorStore,
            displayField:'name',
            valueField:"id",
            fieldLabel:"Vendor",
            emptyText:"Select a vendor",
            width:200,
            listWidth:200,
            hidden : this.showNewReceipts?false:true,
            hideLabel:this.showNewReceipts?false:true,
            hiddenName:"vendor"
        });
        this.formVendorCombo.on('select', function(){
            this.ItemDetailGrid.EditorStore.baseParams.vendor = this.formVendorCombo.getValue();
//            this.filterGrid1();
        },this);
        this.formVendorStore.on('load', function(){
            this.newRec = new this.formVendorRec({
                id:"1",
                name:"All",
                address:"",
                shipterm:"",
                payterm:"",
                status:""
            });
            this.formVendorStore.add(this.newRec);
            this.formVendorCombo.setValue("1");
        },this);
//        this.formVendorStore.load();
        this.showAll = new Wtf.Button({
            text:"Show All Open PO",
            handler:this.showAllPO,
            scope:this,
            hidden : this.showNewReceipts?false:true
        });
        this.dateField2 = new Wtf.form.DateField({
            fieldLabel:"To Date*",
            format:"Y-m-d",
            hidden : this.showNewReceipts?true:false,
            hideLabel : this.showNewReceipts?true:false,
            name:"datedon",
            disableKeyFilter:true,
            minValue: Wtf.archivalDate,
            width:200
        });
        this.MOUTextField = new Wtf.form.TextField({
            fieldLabel:"MoD *",
            readOnly : true,
            hidden : this.showNewReceipts?false:true,
            hideLabel : this.showNewReceipts?false:true,
            name:"trans mod",
            allowBlank:false,
            width:218
        });
        this.search = new Wtf.Button({
            text: 'Search',
            hidden : this.showNewReceipts?true: false,
            iconCls : 'pwnd editicon',
            scope:this,
            handler:function(){
//                this.searchGrid();
            }
        });
        var tbarArray = new Array();
        tbarArray.push(this.fromstoreCombo);
        if(this.showNewReceipts){
            tbarArray.push(this.MOUTextField);
        }

        this.Form = new Wtf.form.FormPanel({
            region:"north",
            height:100,
            url:"jspfiles/inventory/itemHandler.jsp",
            bodyStyle:"background-color:#f1f1f1;padding:8px",
            layout:"column",

            items:[
            {
                layout:"form",
                labelWidth:130,
                border: false,
                columnWidth:.4,
                items:tbarArray
            //   items:[this.fromstoreCombo,this.MOUTextField]
            },  {
                layout:"form",
                labelWidth:130,
                border: false,
                columnWidth:.4,
                items: [this.dateField, this.dateField2, this.formVendorCombo, this.showAll]
            },{
                layout:"form",
                labelWidth:130,
                border: false,
                columnWidth:.2,
                items: [this.search]
            }]
        });
        //        if(checktabperms(12, 1) != "edit"){
//        this.firemyajax();
    //        }
    },
    firemyajax:function(){
        Wtf.Ajax.requestEx({
            url:"jspfiles/inventory/store.jsp",
            params:{
                flag: 8
            }
        }, this,
        function(response){
            var res=eval('('+response+')');
            this.MOUTextField.setValue(res.data[0].username);
        },
        function(response){})
    },
    showAllPO:function(){
        if(this.showAll.getText() === "Show All Open PO"){
            this.ItemDetailGrid.EditorStore.baseParams.showAll = true;
//            this.filterGrid1();
            this.showAll.setText("Apply Date Filter");
            this.dateField.disable();
        }else{
            this.ItemDetailGrid.EditorStore.baseParams.showAll = false;
//            this.filterGrid1();
            this.showAll.setText("Show All Open PO");
            this.dateField.enable();
        }
        
    },
    setBusinessDate:function(){
        Wtf.Ajax.requestEx({
            url: "jspfiles/inventory/inventory.jsp",
            params: {
                flag: 105,
                storeid: this.fromstoreCombo.getValue()
            }
        }, this,
        function(action) {
            action = eval("("+ action + ")");
            //                this.dateField.setValue(action.data);
            //                this.dateField2.setValue(action.data);
            this.ItemDetailGrid.EditorStore.load({
                params:{
                    pstoreid: this.fromstoreCombo.getValue(),
                    ondate : action.data
                }
            });
        }, function(){
        });
},

getItemDetail:function (){
    this.ItemDetailGrid = new Wtf.receiptGrid({
        layout:"fit",
        //            gridTitle:(this.rendermode=="autoshipment"?"Autoshipment":"Stock Receipt")+" Acknowledgment List",
        gridTitle:(this.rendermode=="autoshipment"?"Autoshipment":"Stock Receipt")+" Acknowledgment List",
        border:false,
        region:"center",
        storeStore:this.Store,
        showNewReceipts:this.showNewReceipts,
        rendermode:this.rendermode,
        height:330,
        showStoreMapWin:this.showStoreMapWin
    });
},
SaveItem:function (){
    if(!this.dateField.isValid()){
        this.dateField.markInvalid();
        return false;
    }
    var storeid = this.fromstoreCombo.getValue();
    if(storeid.length==0){
        this.fromstoreCombo.markInvalid();
        return false;
    }
    if(this.MOUTextField.getValue() == "" ){
        msgBoxShow(["Info", "Please fill MoD to update "+(this.rendermode=="autoshipment"?"Autoshipment":"Stock Receipt")], 0);
        this.MOUTextField.markInvalid();
        return false;
    }
    var jsondata = "";
    var mod = this.ItemDetailGrid.sm.getSelections();
    if(mod.length ==0){
        msgBoxShow(["Info", "Please select atleast one item to update "+(this.rendermode=="autoshipment"?"Autoshipment.":"Stock Receipt.")], 0);
        return;
    }
        
    if(this.ItemDetailGrid.EditorStore.getCount() == mod.length){
        if(true){//(integrationFeatureFor != Wtf.IF.SUSHITEI)){
            this.ItemDetailGrid.sm.selectRange(0, mod.length - 2);
            mod = this.ItemDetailGrid.sm.getSelections();
        }
    }
    var incidenttype="";
    var incidenttypevalue=0;
    var quality_incidenttype = "";
    var quality_incidenttypevalue = 0;
    if(this.ItemDetailGrid.EditorStore.getCount()==1 && this.rendermode != "autoshipment" && (integrationFeatureFor != Wtf.IF.SUSHITEI))
        return false;
    var totalIncidents = 0;
    for(var k=0; k<mod.length; k++){
        var rec = mod[k];
        var errormsg = "";
        if(rec.data.store_id==""){
            errormsg += " Store,";
        }
        if(rec.data.itemuuid==""){
            errormsg += " Item,";
        }
        if(rec.data.orderno==""){
            errormsg += " Order No,";
        }
        if(rec.data.fullfilledquantity==""){
            errormsg += " Delivered Quantity,";
        }
        if(rec.data.deliverydate==""){
            errormsg += " Delivery Date,";
        }
            
        if(errormsg.length > 0){
            var indx = (this.ItemDetailGrid.EditorStore.indexOf(rec))+1;
            msgBoxShow(["Error","Please fill"+errormsg.substring(0, errormsg.length-1)+" value for record at index "+indx],1);
            return;
        }else{
            if(rec.data.extra > 0 || rec.data.short1 > 0 || rec.data.qualityissue){ //Incidents
                incidenttype = "";
                incidenttypevalue = "";

                if(rec.data.extra > 0){
                    incidenttype = "Extra";
                    incidenttypevalue = rec.data.extra;
                }

                if(rec.data.short1 > 0){
                    incidenttype = "Short";
                    incidenttypevalue = rec.data.short1;
                }

                //                    if(incidenttypevalue!=""){
                //                        jsondata += this.createJson( rec,incidenttype, incidenttypevalue);
                //                    }


                if(rec.data.qualityissue > 0){
                    quality_incidenttype = "Quality Issue";
                    quality_incidenttypevalue = rec.data.qualityissue;
                    if(rec.data.issuetype==""){
                        msgBoxShow(["Alert", "Please select quality issue type for item "+rec.data.itemcode], 0);
                        return;
                    }
                }

                if(incidenttypevalue!="" || quality_incidenttype!="" ){
                    jsondata += this.createJson( rec,incidenttype, incidenttypevalue, quality_incidenttype, quality_incidenttypevalue);
                }
                incidenttype = "";
                incidenttypevalue = 0;
                quality_incidenttype = "";
                quality_incidenttypevalue = 0;
                totalIncidents++;
            }else{ // New receipt record
                incidenttype = "";
                incidenttypevalue = 0;
                quality_incidenttype = "";
                quality_incidenttypevalue = 0;
                jsondata += this.createJson( rec,incidenttype, incidenttypevalue, quality_incidenttype, quality_incidenttypevalue);
            }
        }
    }

    var trmLen = jsondata.length - 1;
    var finalStr = jsondata.substr(0,trmLen);

    if(this.Form.form.isValid()){
        var msg = "Are you sure you want to confirm "+(this.rendermode=="autoshipment"?"Autoshipment":"Stock Receipt");
        msg += totalIncidents<=0?"?":(", along with "+totalIncidents+ " incident"+(totalIncidents>1?"s":"")+"?");
        Wtf.MessageBox.confirm("Confirm", msg, function(btn){
            if(btn == 'yes') {
                this.ItemDetailGrid.EditorStore.commitChanges();
//                this.submitForm(finalStr);
            }else
                return;
        },this);
    }else{
        msgBoxShow(["Error", "Please fill header form."], 1);
    }

},

createJson: function(rec, incidenttype, incidenttypevalue, quality_incidenttype, quality_incidenttypevalue){
    var jsondata ="";
    jsondata += "{\"itemcode\":\"" + rec.data.itemcode + "\",";
    jsondata += "\"storeid\":\"" + rec.data.storeid + "\",";
    jsondata += "\"itemuuid\":\"" + rec.data.itemuuid + "\",";
    jsondata += "\"orderno\":\"" + rec.data.orderno + "\",";
    jsondata += "\"store_id\":\"" + rec.data.store_id + "\",";
    jsondata += "\"fullfilledquantity\":\"" + rec.data.fullfilledquantity + "\",";
    jsondata += "\"deliverydate\":\"" + rec.data.deliverydate.dateFormat("Y-m-d") + "\",";
    jsondata += "\"stockreceiptid\":\"" + rec.data.stockreceiptid + "\",";
    jsondata += "\"autoshipmentid\":\"" + rec.data.autoshipmentid + "\",";
    jsondata += "\"issuetype\":\""+rec.data.issuetype+"\",";
    jsondata += "\"incidenttype\":\"" + incidenttype + "\",";
    jsondata += "\"quality_incidenttype\":\"" + quality_incidenttype + "\",";
    jsondata += "\"netReceivedQty\":\"" + rec.get("netReceivedQty") + "\",";
    jsondata += "\"quality_incidenttypevalue\":\"" + quality_incidenttypevalue + "\",";
    jsondata += "\"storecode\":\"" + rec.data.store_code + "\",";
    jsondata += "\"uomvalue\":\"" + rec.data.uomvalue + "\",";
    jsondata += "\"primaryuom\":\"" + rec.data.primaryuom + "\",";
    jsondata += "\"incidenttypevalue\":\"" + incidenttypevalue + "\"},";
    return jsondata;
},

submitForm : function(finalStr){
    WtfGlobal.setAjaxTimeOut();//set to 15min
    this.submitBut.disable();
    this.Form.form.submit({
        params:{
            flag:40,
            acknowledgby : this.MOUTextField.getValue(),
            jsondata:finalStr,
            addincidentfor:this.rendermode,
            ackform: this.showNewReceipts?true:false
        },
        scope:this,
        success:function (res){

            var msg = (this.rendermode=="autoshipment"?"Autoshipment":"Stock Receipt ")+" Updated successfully";
            //                if(res.transResult){
            //                    msg += " \n\n Some GL entries could not be updated. Please check GL Failure report for specific errors.";
            //                }
            msgBoxShow(["Status", msg], 0);
            this.ItemDetailGrid.EditorStore.reload();
            this.submitBut.enable();
            WtfGlobal.resetAjaxTimeOut();//Default 30secs
        },
        failure:function (){
            msgBoxShow(["Failure", "Error While Updating "+(this.rendermode=="autoshipment"?"Autoshipment":"Stock Receipt ")], 1);
            this.submitBut.enable();
            WtfGlobal.resetAjaxTimeOut();//Default 30secs
        }
    });
}

});


//------------------------------------------Editor Grid Component---------------------------------------------------

Wtf.receiptGrid = function (config){
    Wtf.apply(this,config);
    Wtf.receiptGrid.superclass.constructor.call(this);
}

Wtf.extend(Wtf.receiptGrid,Wtf.Panel,{
    initComponent:function (){
        if(this.showStoreMapWin == undefined){
            this.showStoreMapWin = false;
        }
        Wtf.receiptGrid.superclass.initComponent.call(this);
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
        this.itemIssueRec = new Wtf.data.Record.create([{
            name:'id'
        },{
            name:'name'
        }]);
        this.itemIssueStore = new Wtf.data.Store({
            method:'POST',
            baseParams:{
                flag: 28,
                type:19
            },
            url:'jspfiles/inventory/itemHandler.jsp',
            sortInfo: {
                field: 'name',
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            },
            reader: new Wtf.data.KwlJsonReader({
                root : 'data'
            },this.itemIssueRec)
        });
        
        this.itemIssueStore.on('load', function(){
            var allrec = new this.itemIssueRec({
                id:"Add",
                name:"Add New Issue Type"
            });
            this.itemIssueStore.insert(0,allrec);
        },this);
        
//        this.itemIssueStore.load();
        
        this.itemIssueCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            store:this.itemIssueStore,
            displayField:"name",
            valueField:"id",
            width:200
        });
        
        this.itemIssueCombo.on('select', function(cmb, rec, index){
            if(index == 0){
                new Wtf.AddEditVendorMasterData({
                    layout:"fit",
                    title:'Add Master Data',
                    modal:true,
                    configid:19,
                    parentid:0,
                    width:400,
                    height:230,
                    store:this.itemIssueStore                    
                }).show();
                this.itemIssueCombo.setValue("");
            }
        }, this);
        
        this.vendorRec = new Wtf.data.Record.create([
        {
            name:'id'
        },

        {
            name:'name'
        }
        ]);
        this.vendorStore = new Wtf.data.Store({
            method:'POST',
            baseParams:{
                flag: 152,
                type:9,
                vendor:true,
                companyid:companyid
            },
            url:  'jspfiles/inventory/inventory.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root : 'data'
            },this.vendorRec),
            sortInfo: {
                field: 'name',
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            }
        });
//        this.vendorStore.load();
        
        this.vendorCombo = new Wtf.form.ComboBox({
            fieldLabel : 'Vendor',
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"vendor",
            name:"vendor",
            typeAhead: true,
            width:200,
            emptyText:'Select vendor',
            editable: true,
            store: this.vendorStore,
            displayField: 'name',
            valueField:'name',
            forceSelection : true
        });

        this.EditorRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"storeid"
        },

        {
            name:"store_id"
        },

        {
            name:"store_code"
        },

        {
            name:"autoshipmentid"
        },

        {
            name:"stockreceiptid"
        },

        {
            name:"itemcode"
        },

        {
            name:"itemuuid"
        },

        {
            name:"orderno"
        },

        {
            name:"deliverydate",
            type:"date",
            dateFormat:"Y-m-d"
        },

        {
            name:"vendor"
        },

        {
            name:"ordercategory"
        },

        {
            name:"orderedQty"
        },

        {
            name:"netReceivedQty"
        },

        {
            name:"categoty"
        },

        {
            name:"supplies"
        },

        {
            name:"source"
        },

        {
            name:"ownership"
        },

        {
            name:"description"
        },

        {
            name:"visited"
        },

        {
            name:"uom"
        },

        {
            name:"primaryuom"
        },

        {
            name:"uomvalue"
        },

        {
            name:"fullfilledquantity"
        },

        {
            name:"totalquantity"
        },

        {
            name:"extra"
        },

        {
            name:"short1"
        },

        {
            name:"qualityissue"
        },

        {
            name:"filedby"
        },

        {
            name:"issuetype"
        },

        {
            name:"purchasinguom"
        },

        {
            name:"purchasinguomvalue"
        }
        ]);

        this.EditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.EditorRec);

        this.EditorStore = new Wtf.data.GroupingStore({
            url:"jspfiles/inventory/inventory.jsp",
            baseParams: {
                visited : (this.showNewReceipts)?0:1,
                flag: (this.rendermode=="autoshipment")?70:30,
                showAll:false,
                vendor:"1",
                saved:(this.rendermode=="autoshipment")?true:false
            },
            reader:this.EditorReader,
            sortInfo: {
                field: 'orderno',
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            },
            groupField:"orderno"
        });
        this.storeCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.storeStore,
            displayField:'storeid',
            valueField:"storeid",
            fieldLabel:"Store*",
            emptyText:"Select a store",
            hiddenName:"fromstore",
            width:200,
            listWidth:200
        });
        
        this.itemEditorRec = new Wtf.data.Record.create([{
            name:"id"
        },{
            name:"itemdescription"
        },{
            name:"itemcode"
        },{
            name:'ordercategory'
        },{
            name:'orderinguom'
        },{
            name:'orderuom'
        },{
            name:'vendor'
        },{
            name:'primaryuom'
        },{
            name:'quantityForOne'
        },{
            name:'purchasinguom'
        },{
            name:'purchasinguomvalue'
        }]);
        this.itemEditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.itemEditorRec);

        this.itemEditorStore = new Wtf.data.Store({
            url:"jspfiles/inventory/store.jsp",
            sortInfo: {
                field: 'itemdescription',
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            },
            reader:this.itemEditorReader
        });
//        this.itemEditorStore.load({
//            params:{
//                flag:27
//            }
//        });
        this.itemEditorCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.itemEditorStore,
            fieldLabel:"Item Type",
            displayField:"itemdescription",
            editable:true,
            valueField:"itemdescription",
            width:200
        });
        var first = 1;
        this.EditorStore.on("load",function(p){
            if(this.showNewReceipts && this.rendermode != "autoshipment"){
                if(true)//integrationFeatureFor !== Wtf.IF.SUSHITEI)
                    this.addRec();
            }
            this.addRec();
            if(false){//isIntegrateWithAccounting && integrationFeatureFor == Wtf.IF.POSLAVU && first == 1 && this.showStoreMapWin == true){
                first++;
                this.stockReceiptMappingWin(true);
            }
            
        },this);

        this.newCm = [];
        
        this.sm = new Wtf.grid.CheckboxSelectionModel();
        this.newCm.push(this.sm);
        this.newCm.push(new Wtf.grid.RowNumberer())
        if(true){//!isIntegrateWithAccounting || (isIntegrateWithAccounting && integrationFeatureFor == Wtf.IF.SUSHITEI)){
            this.newCm.push({
                header:"Store Code",
                //sortable:true,
                dataIndex:"storeid",
//                renderer:Wtf.ux.comboBoxRenderer(this.storeCombo),
                editor:(this.showNewReceipts)?this.storeCombo:""
            });
        } 
        this.newCm.push({
            header:"Item Description",
            //sortable:true,
            dataIndex:"description",
            renderer : function(a){
                return "<div wtf:qtip='"+a+"'>"+a+"</div>";
            },
            editor:(this.showNewReceipts)?this.itemEditorCombo:""
        },{
            header:"Item Code",
            //sortable:true,
            dataIndex:"itemcode"
        },{
            header:'Order No.',
            //sortable:true,
            dataIndex:"orderno",
            editor: (this.showNewReceipts)?new Wtf.form.TextField():""
        },{
            header:"Delivery Date",
           // sortable:true,
            dataIndex:"deliverydate"
//            renderer : Wtf.util.Format.dateRenderer(companyDateFormat),
//            editor: (this.showNewReceipts)?new Wtf.form.DateField({
//                format :companyDateFormat
//            }):""

        },{
            header:"Order Category",//(integrationFeatureFor === Wtf.IF.SUSHITEI)?"Item Category":"Order Category",
            //sortable:true,
            dataIndex:"ordercategory"
        },
        //        {
        //                header:"Category",
        //                sortable:true,
        //                hidden:true,
        //                dataIndex:"categoty"
        //        }
        //        ,{
        //                header:"Ownership",
        //                sortable:true,
        //                hidden:true,
        //                dataIndex:"ownership"
        //        },{
        //                header:"Source",
        //                sortable:true,
        //                hidden:true,
        //                dataIndex:"source"
        //        },{
        //                header:"Supplies",
        //                sortable:true,
        //                hidden:true,
        //                dataIndex:"supplies"
        //        },
        {
            header:"Vendor",
            //sortable:true,
            dataIndex:"vendor",
            editor: this.vendorCombo
//            renderer: WtfGlobal.getComboRenderer(this.vendorCombo)
        },{
            header:"Ordered Quantity",
            dataIndex:"orderedQty",
            align:"right",
            renderer:this.uomRenderer
        },{
            header:"Delivered Quantity",
            //sortable:true,
            align:"right",
            dataIndex:"fullfilledquantity",
            editor: (this.showNewReceipts)?new Wtf.form.NumberField():"",
            renderer:this.uomRenderer
        });

        if(true){//!isIntegrateWithAccounting  || (isIntegrateWithAccounting && integrationFeatureFor == Wtf.IF.SUSHITEI)){
            this.newCm.push({
                header:"Extra",
                sortable:false,
                dataIndex:"extra",
                align:"right",
                editor:(this.showNewReceipts)?new Wtf.form.NumberField():"",
                renderer: this.primaryUomRenderer
            },{
                header:"Short",
                sortable:false,
                dataIndex:"short1",
                align:"right",
                editor:(this.showNewReceipts)?new Wtf.form.NumberField():"",
                renderer: this.primaryUomRenderer
            },{
                header:"Quality Issue",
                sortable:false,
                dataIndex:"qualityissue",
                align:"right",
                editor:(this.showNewReceipts)?new Wtf.form.NumberField():"",
                renderer: this.primaryUomRenderer
            },{
                header:"Issue Type",
                sortable:false,
                width:200,
                dataIndex:"issuetype",
                editor:(this.showNewReceipts)?this.itemIssueCombo:""
//                renderer:Wtf.ux.comboBoxRenderer(this.itemIssueCombo)
            },{
                header:"Net Received Quantity",
                sortable:false,
                dataIndex:"netReceivedQty",
                align:"right",
                //editor:new Wtf.form.NumberField(),
                renderer: this.primaryUomRenderer
            });
        }
        

        if(!this.showNewReceipts) {
            this.newCm.push({
                header:"MoD",
                sortable:false,
                dataIndex:"filedby"
            });
        }

        this.EditorColumn = new Wtf.grid.ColumnModel(this.newCm);

        var tbarItems = new Array();
        if(false){//isIntegrateWithAccounting && integrationFeatureFor == Wtf.IF.POSLAVU && this.showStoreMapWin == true){
            tbarItems.push({
                text:"Accept Stock Items",
                tooltip:"Accept stock items for store",
                scope:this,
                handler: function(){
                    this.stockReceiptMappingWin(false);
                }
            })
        }
        
        this.grpView = new Wtf.grid.GroupingView({
            forceFit: true,
            showGroupName: true,
            enableGroupingMenu: true,
            hideGroupedColumn: true,
            emptyText:"No "+(this.showNewReceipts?"new":"acknowledged")+" "+(this.rendermode=="autoshipment"?"autoshipment records":"stock receipts")+" for selected store on selected date."
        });
        
        this.EditorGrid = new Wtf.grid.EditorGridPanel({
            cm:this.EditorColumn,
            sm:this.sm,
            region:"center",
            border:true,
            autoScroll:true,
            store:this.EditorStore,
            view:this.grpView,
            loadMask:true,
            clicksToEdit:1,
            tbar:tbarItems
        });
        if(true){//!(isIntegrateWithAccounting && integrationFeatureFor == Wtf.IF.POSLAVU && this.showStoreMapWin == true)){
            this.EditorGrid.on("render", function(){
                this.EditorGrid.getTopToolbar().hide();
            }, this);
            
        }
        
        
        this.EditorGrid.on('cellclick',this.callAfterEdit2,this);
        this.EditorGrid.on('validateedit',this.validateEdit,this);
        this.EditorGrid.on("afteredit",this.fillGridValue,this);
    },
    
    stockReceiptMappingWin: function(autoOpen){
        
        this.mappingStoreCombo = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.storeStore,
            displayField:'storeid',
            valueField:"id",
            fieldLabel:"Store*",
            emptyText:"Select a store",
            hiddenName:"fromstore",
            width:200,
            listWidth:200
        });
        this.storeStore.load();
        
        this.MappingSRStore = new Wtf.data.Store({
            url:"jspfiles/inventory/inventory.jsp",
            baseParams: {
                visited : 0,
                flag: 153
            },
            reader:this.EditorReader
        });
        this.MappingSRStore.on("load",function(store, records){
            if(records.length == 0){
                if(autoOpen){
                    return;
                }else{
                    msgBoxShow(["Alert", "No stock items available to accept."], 0);
                    return;
                }
            }
            this.mappingSRGrid.doLayout();
            this.mappingWin.show();
        },this);
        var mappingcm = [];
        var mappingsm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect : false
        });
        mappingcm.push(mappingsm,new Wtf.grid.RowNumberer(),
        {
            dataIndex:"stockreceiptid",
            hidden:true
        },{
            header:"Item Description",
            //sortable:true,
            dataIndex:"description",
            renderer : function(a){
                return "<div wtf:qtip='"+a+"'>"+a+"</div>";
            }
        },{
            header:"Item Code",
            //sortable:true,
            dataIndex:"itemcode"
        },{
            header:'Order No.',
            //sortable:true,
            dataIndex:"orderno"
        },{
            header:"Delivery Date",
            //sortable:true,
            dataIndex:"deliverydate"
//            renderer : Wtf.util.Format.dateRenderer(companyDateFormat)

        },{
            header:"Quantity",
            //sortable:true,
            align:"right",
            dataIndex:"fullfilledquantity",
            renderer:function(val,b,rec){
                return "<span>"+val+"&nbsp;"+rec.data.uomvalue+"</span>";
            }
        }
        );

        var mappingGridCM = new Wtf.grid.ColumnModel(mappingcm);

        this.mappingSRGrid = new Wtf.grid.GridPanel({
            cm:mappingGridCM,
            sm:mappingsm,
            border:false,
            height:257,
            autoScroll:true,
            store:this.MappingSRStore,
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:"No stock items available."
            }),
            loadMask:true,
            tbar:["Select Store :",this.mappingStoreCombo]
        });
        
        this.mappingWin = new Wtf.Window({
            title:"Accept Stock Items",
            width:650,
            height:403,
            closable:false,
            modal:true,
            border: false,
            layout : 'border',
            items :[{
                region: 'north',
                border:false,
                height:80,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                items:[{
                    xtype:"panel",
                    border:false,
                    height:70,
                    html:getwindowTopHtml("Accept Stock Items","Accept stock items for store","images/createuser.png")
                }]
            },{
                region: 'center',
                border:false,
                items:[this.mappingSRGrid]
            }],
            buttons:[{
                text:"Accept",
                scope:this,
                handler: function(){
                    
                    var rows = this.mappingSRGrid.getSelectionModel().getSelections();
                    var storeid = this.mappingStoreCombo.getValue();
                    if(storeid == ""){
                        msgBoxShow(["Alert", "Please select a store to map."], 0);
                        return;
                    }
                    if(rows.length == 0){
                        msgBoxShow(["Alert", "Please select at least one item to accept."], 0);
                        return;
                    }
                    var stockIds  = new Array();
                    for(var i=0 ; i<rows.length ; i++){
                        stockIds.push(rows[i].get("stockreceiptid"));
                    }
                    
                    var businessDate = rows[0].get("deliverydate");
                    var totalRows = this.mappingSRGrid.getStore().getTotalCount();
                    var reload = true;
                    if(totalRows == rows.length){
                        reload = false;
                    }
                    this.saveMappedStockReceipts(storeid, stockIds.toString(), businessDate.format('Y-m-d'), reload);
                    
                }
            },{
                text:"Skip",
                scope:this,
                handler: function(){
                    this.mappingWin.close();
                    this.mappingWin.destroy();
                }
            }]
        });
        
        this.mappingWin.on("destroy", function(p){
//            this.EditorStore.reload();
        },this);
        
//        this.MappingSRStore.load();
        function getwindowTopHtml(text, body,img){
            if(img===undefined) {
                img = 'images/createuser.png';
            }
            var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
            +"<div style='float:left;height:100%;width:auto;position:relative;'>"
            +"<img src = "+img+" style='width:40px;height:52px;pading:5px;margin:10px;'></img>"
            +"</div>"
            +"<div style='float:left;height:100%;width:75%;position:relative;'>"
            +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
            +"<div style='font-size:10px;float:left;margin:15px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>"
            +"</div>"
            +"</div>" ;
            return str;
        }
        
        
        
    },
    
    saveMappedStockReceipts : function(storeId, commaSeperatedStockIds, businessDate, reloadFlag){
        Wtf.Ajax.requestEx({
            url: "jspfiles/inventory/inventory.jsp",
            params: {
                flag: 154,
                storeid: storeId,
                businessDate: businessDate,//in yyyy-MM-dd format
                stockids: commaSeperatedStockIds
            }
        }, this,
        function(action) {
            action = eval("("+ action + ")");
            if(reloadFlag && reloadFlag == true){
                this.MappingSRStore.load();
            }else{
                this.mappingWin.close();
                this.mappingWin.destroy();
            }
        }, function(){
        });
},
    
primaryUomRenderer: function(val, b, rec) {
    if(rec.data.primaryuom == undefined) {
        return val;
    } else {
        return "<span>" + val + "&nbsp;" + rec.data.primaryuom + "</span>";
    }
},
uomRenderer: function(val, b, rec) {
    if(rec.data.primaryuom == undefined) {
        return val;
    } else {
        //            if(isIntegrateWithAccounting){
        return "<span>" + val + "&nbsp;" + rec.data.purchasinguomvalue + "</span>";
    //            }else{
    //                return "<span>" + val + "&nbsp;" + rec.data.uomvalue + "</span>";
    //            }
            
    }
},
fillGridValue:function (e){
    if(e.row == this.EditorStore.getCount()-1 &&  this.rendermode != "autoshipment" && this.showNewReceipts){
        if(true)//integrationFeatureFor !== Wtf.IF.SUSHITEI)
            this.addRec();
    }
    if(e.field=='description'){
        for(var i=0;i<this.itemEditorStore.getCount();i++){
            if(this.itemEditorStore.getAt(i).get("itemdescription") == e.value){
                this.EditorStore.getAt(e.row).set("ordercategory",this.itemEditorStore.getAt(i).get("ordercategory"));
                this.EditorStore.getAt(e.row).set("uom",this.itemEditorStore.getAt(i).get("orderinguom"));
                this.EditorStore.getAt(e.row).set("uomvalue",this.itemEditorStore.getAt(i).get("orderuom"));
                this.EditorStore.getAt(e.row).set("purchasinguom",this.itemEditorStore.getAt(i).get("purchasinguom"));
                this.EditorStore.getAt(e.row).set("purchasinguomvalue",this.itemEditorStore.getAt(i).get("purchasinguomvalue"));
                this.EditorStore.getAt(e.row).set("itemcode",this.itemEditorStore.getAt(i).get("itemcode"));
                this.EditorStore.getAt(e.row).set("primaryuom",this.itemEditorStore.getAt(i).get("primaryuom"));
                this.EditorStore.getAt(e.row).set("vendor",this.itemEditorStore.getAt(i).get("vendor"));
                this.EditorStore.getAt(e.row).set("itemuuid",this.itemEditorStore.getAt(i).get("id"));
            }
        }
    }
    if(e.field=='storeid'){
        for(var j=0; j < this.storeStore.getCount(); j++) {
            if(this.storeStore.getAt(j).get("storeid") == e.value){
                this.EditorStore.getAt(e.row).set("store_id",this.storeStore.getAt(j).get("store_id"));
                this.EditorStore.getAt(e.row).set("store_code",this.storeStore.getAt(j).get("storecode"));
            }
        }
    }
    if(e.field == "fullfilledquantity"){
        if(e.record.get("stockreceiptid")=="" && this.rendermode != "autoshipment") {
            for(var i=0;i<this.itemEditorStore.getCount();i++){
                if(this.itemEditorStore.getAt(i).get("id") == e.record.get("itemuuid")){
                    this.EditorStore.getAt(e.row).set("netReceivedQty", this.itemEditorStore.getAt(i).get("quantityForOne") * e.value);
                    this.EditorStore.getAt(e.row).set("totalquantity", this.itemEditorStore.getAt(i).get("quantityForOne") * e.value);
                }
            }
        }
    }
},
validateEdit: function(e) {
    var extravalue = 0;
    var shortvalue = 0;
    var qualityvalue = 0;
    var incidentcount = 0;
    var editedCount = 0;
    if(e.field == "qualityissue") {
        shortvalue = e.record.data.short1 == "" ? 0 : parseFloat(e.record.data.short1);
        qualityvalue = e.value== "" ? 0 : parseFloat(e.value);
        incidentcount = parseFloat(extravalue) + parseFloat(shortvalue) + parseFloat(qualityvalue);
        editedCount = (e.record.data.netReceivedQty + e.record.data.qualityissue) - qualityvalue;
        if(qualityvalue==0){
            e.record.data.issuetype='';
        }
        if(e.value < 0 || parseFloat(incidentcount) > parseFloat(e.record.data.totalquantity)) {
            return false;
        }else {
            e.record.set('netReceivedQty', editedCount);
            e.record.commit();
        }
    }
    else if(e.field == "extra"){
        shortvalue = e.record.data.short1 == "" ? 0 : parseFloat(e.record.data.short1);
        qualityvalue = e.value== "" ? 0 : parseFloat(e.value);
        incidentcount = parseFloat(extravalue) + parseFloat(shortvalue) + parseFloat(qualityvalue);
        if(qualityvalue==0){
            e.record.data.issuetype='';
        }
        editedCount = (e.record.data.netReceivedQty - e.record.data.extra) + e.value;
        if(e.value < 0 || parseFloat(incidentcount) > parseFloat(e.record.data.totalquantity)) {
            return false;
        } else {
            e.record.set('netReceivedQty', editedCount);
            e.record.commit();
        }
    }else if(e.field == "short1"){
        shortvalue = e.record.data.short1 == "" ? 0 : parseFloat(e.record.data.short1);
        qualityvalue = e.value== "" ? 0 : parseFloat(e.value);
        incidentcount = parseFloat(extravalue) + parseFloat(shortvalue) + parseFloat(qualityvalue);
        if(qualityvalue==0){
            e.record.data.issuetype='';
        }
        editedCount = (e.record.data.netReceivedQty + e.record.data.short1) - e.value;
        if(e.value < 0 || parseFloat(incidentcount) > parseFloat(e.record.data.totalquantity)) {
            return false;
        } else {
            e.record.set('netReceivedQty', editedCount);
            e.record.commit();
        }
    }
},

callAfterEdit2: function(e, row, column) {
    var cm = this.EditorGrid.getColumnModel();
    var rec = this.EditorStore.data.items[row];
    var field = cm.getDataIndex(column);
    if(field=="extra") {
        if(rec.get("short1")=="") {
            cm.setEditable(column,true);
        } else {
            cm.setEditable(column,false);
        }
    } else if(field=="short1") {
        if(rec.get("extra")=="") {
            cm.setEditable(column,true);
        } else {
            cm.setEditable(column,false);
        }
    } else if(field=="issuetype") {
        if(rec.get("qualityissue")!="") {
            cm.setEditable(column,true);
        } else {
            cm.setEditable(column,false);
        }
    } else if(field=="qualityissue") {
    } else {
        if(rec.get("stockreceiptid")=="" && this.rendermode != "autoshipment") {
            cm.setEditable(column,true);
        } else {
            cm.setEditable(column,false);
        }
    }
},
addRec:function (){
    this.newRec = new this.EditorRec({
        id:"",
        storeid:"",
        store_id:"",
        stockreceiptid:"",
        autoshipmentid:"",
        itemcode:"",
        itemuuid:"",
        orderno:"",
        deliverydate:"",
        vendor:"",
        ordercategory:"",
        categoty:"",
        supplies:"",
        source:"",
        ownership:"",
        description:"",
        visited:"",
        uom:"",
        uomvalue:"",
        purchasinguom:"",
        purchasinguomvalue:"",
        primaryuom:"",
        fullfilledquantity:"",
        extra:"",
        short1:"",
        qualityissue:"",
        issuetype:"",
        orderedQty:"",
        netReceivedQty:""            
    });
    this.EditorStore.add(this.newRec);
}
});

