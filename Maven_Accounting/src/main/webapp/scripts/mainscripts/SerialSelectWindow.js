/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
Wtf.account.SerialSelectWindow = function(config) {
    this.loadedStoreCount=config.loadedStoreCount;
    this.isEdit=config.isEdit;
    this.linkflag=config.linkflag;
    this.transactionid=config.transactionid;
    this.productid=config.productid;
    this.isDO=config.isDO;
    this.quantity=config.quantity;
    this.copyTrans=config.copyTrans;
    this.mainproduct=config.mainproduct;     //ERP-23242
    this.isUnbuildAssembly=config.isUnbuildAssembly; //ERP-23242
    this.subproduct=config.subproduct;                //ERP-23242
    this.refno=config.refno;
    this.customerID=config.customerID,
    this.isForconsignment=config.isForconsignment;
    this.billid=config.billid;
    this.documentid=config.documentid;
    this.isAutoFillBatchDetail=config.isAutoFillBatchDetail!=undefined? config.isAutoFillBatchDetail:false;
    this.featchIssuedSerial=config.featchIssuedSerial!=undefined? config.featchIssuedSerial:false;
    this.serialDataReturnTO=config.serialDataReturnTO;
    this.serialIdDataReturnTo=config.serialIdDataReturnTo;
    this.skuDataReturnTo=config.skuDataReturnTo;
    this.isConsignment=config.isConsignment;
    this.moduleid=config.moduleid;
    this.grid=config.grid;
    this.isBatchForProduct=config.isBatchForProduct;
    this.obj=config.obj;
    this.store=config.store;
    this.isLinkedFromPI=config.isLinkedFromPI,
    this.butnArr = new Array();
    this.isSubmitBtnClicked = false;
    this.docrowid=config.docrowid;
    this.linkedFrom=config.linkedFrom;
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope:this,
        handler: function() {
            if (this.selectedQuickSearch.getValue()) {
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.selectserial.clearquciksearch")],2);
                 return false;
            }else{
                this.selectedstore.clearFilter(); 
                if(this.selectedstore.getCount()<=1){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.batch.sr")],2);
                        return false;
                    }else{
//                        this.rec = this.sm2.getSelections();
                        if((this.selectedstore.getCount()-1)>this.quantity)
                        {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.please")+" "+this.quantity+" "+WtfGlobal.getLocaleText("acc.field.srnoq")],2);
                            return false;
                        }
                        
                        if(this.isAutoFillBatchDetail){
                            var selectedSerial="";
                            var selectedSerialId="";
                            var skus="";
                            var arr=[];
                            var inculelast=false;
                            this.selectedstore.each(function(rec){
                                if(rec.data.serialno!= undefined && rec.data.serialno != ""){
                                    selectedSerial = (selectedSerial.length > 0 ? selectedSerial+",":"")+rec.data.serialno;
                                    selectedSerialId = (selectedSerialId.length > 0 ? selectedSerialId+",":"")+rec.data.serialnoid;
                                    if(Wtf.account.companyAccountPref.SKUFieldParm ==true){
                                        skus = (skus.length > 0 ? skus+",":"")+rec.data.skufield;
                                    }
                                }
                                arr.push(this.selectedstore.indexOf(rec));
                            }, this);
                            var jarray=WtfGlobal.getJSONArray(this.selectedgrid,inculelast,arr); 
                            if(this.serialDataReturnTO != undefined){
                                this.store.data.items[this.obj.row].set(this.serialDataReturnTO, selectedSerial);
                            }
                            if(this.serialIdDataReturnTo != undefined){
                                this.store.data.items[this.obj.row].set(this.serialIdDataReturnTo, selectedSerialId);
                            }
                            if(this.skuDataReturnTo != undefined){
                                this.store.data.items[this.obj.row].set(this.skuDataReturnTo, skus);
                            }
                            if(this.store.data.items[this.obj.row].data.serialno !=undefined){
                                this.store.data.items[this.obj.row].data.serialno=selectedSerial;
                                this.store.data.items[this.obj.row].data.serialDetails=jarray;
                            }
                            if((this.moduleid==Wtf.Acc_Delivery_Order_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId || this.moduleid==Wtf.Acc_Cash_Sales_ModuleId)){
                            this.obj.record.serialno=selectedSerial;
                            this.obj.record.serialDetails=jarray;
                        }
                        }else{
                            for(var i=0;i<(this.selectedstore.getCount()-1);i++){
                             var rec=this.selectedstore.getAt(i);
                            
                            this.store.data.items[this.obj.row+i].data.serialno=rec.data.serialno
                            this.store.data.items[this.obj.row+i].data.serialnoid=rec.data.serialnoid
                            this.store.data.items[this.obj.row+i].data.expstart=rec.data.expstart
                            this.store.data.items[this.obj.row+i].data.expend=rec.data.expend
                            this.store.data.items[this.obj.row+i].data.skufield=rec.data.skufield
                            this.store.data.items[this.obj.row+i].data.purchaseserialid=rec.data.purchaseserialid
                            if(this.moduleid==51){
                              this.store.data.items[this.obj.row+i].data.reusablecount=rec.data.reusablecount
                             }
                            if(this.moduleid==27){
                              this.store.data.items[this.obj.row+i].data.stocktype=this.obj.record.data.stocktype
                             }
                            if(!this.isBatchForProduct){ 
                                this.store.data.items[this.obj.row+i].data.purchasebatchid=rec.data.purchasebatchid  //kept the purchase batch id in case of batch option is not selected 
                            }
//                            var rowObject = new Object();
//                            rowObject['srnoid'] =this.rec[i].data.serialnoid
//                            Wtf.dupsrno.push(rowObject);
                        }
                            
                        }
                        this.grid.getView().refresh();
                      
                    }
                    this.close();
            }
            
        }
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.SerialSelectWindow.superclass.constructor.call(this, config);  
}
Wtf.extend(Wtf.account.SerialSelectWindow, Wtf.Window, {
    height : 480,
    width : 900,
//    iconCls :getButtonIconCls(Wtf.etype.deskera),
    title : WtfGlobal.getLocaleText("acc.field.SelectSerialNo"),
    bodyStyle : 'padding:5px;background-color:#ffffff;',
    layout : 'border',
    resizable : true,
    onRender: function(config) {
      Wtf.account.SerialSelectWindow.superclass.onRender.call(this, config);
      this.createDisplayGrid();

      this.movetoright = document.createElement('img');
        this.movetoright.src = "../../images/arrowright.gif";
        this.movetoright.style.width = "24px";
        this.movetoright.style.height = "24px";
        this.movetoright.style.margin = "5px 0px 5px 0px";
        this.movetoright.onclick = this.movetorightclicked.createDelegate(this, []);
        this.movetoleft = document.createElement('img');
        this.movetoleft.src = "../../images/arrowleft.gif";
        this.movetoleft.style.width = "24px";
        this.movetoleft.style.height = "24px";
        this.movetoleft.style.margin = "5px 0px 5px 0px";
        this.movetoleft.onclick = this.movetoleftclicked.createDelegate(this, []);
        
       this.centerdiv = document.createElement("div");
        this.centerdiv.appendChild(this.movetoright);
        this.centerdiv.appendChild(this.movetoleft);
        this.centerdiv.style.padding = "135px 0px 135px 0px";
        
      this.NorthForm=new Wtf.form.FormPanel({
        region : 'center',
        //height:200,
        autoHeight:true,
        border:false,
        defaults:{border:false},
        split:true,
        layout:'form',
        baseCls:'northFormFormat',
        disabledClass:"newtripcmbss",
        hideMode:'display',
        id:'Northform',  //this.id+
        cls:"visibleDisabled",
        labelWidth:120,
//        disabled:this.readOnly,
        items:[{
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.48,
                items:[this.availablegrid]
           },{
                layout:'form',
                columnWidth:0.04,
                items:[{
                        region: 'center',
                        border: false,
                        contentEl: this.centerdiv
                }]
            },{
                layout:'form',
                columnWidth:0.48,
                items:[this.selectedgrid]
            }]
        }]
    
    });
    

    this.add(
    this.centerPanel = new Wtf.Panel({
        border: false,
        region: 'center',
        id: 'centerpan', // + this.id,
       // autoScroll: true,            
        bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
        baseCls: 'bckgroundcolor',
        layout: 'border',
        items:[this.NorthForm]  
    }));
        
    },
    AvaialblehandleResetClick: function() {
        if (this.availableQuickSearch.getValue()) {
             this.availablestore.clearFilter(); 
            this.availableQuickSearch.reset();
        }
    },
    selectedhandleResetClick: function() {
        if (this.selectedQuickSearch.getValue()) {
             this.selectedstore.clearFilter(); 
            this.selectedQuickSearch.reset();
        }
    },
    movetorightclicked: function(){
        var selected = this.sm1.getSelections();
        var recArr = [];
        for (var ctr = 0; ctr < selected.length; ctr++) {
            recArr[0] = selected[ctr];
            this.selectedstore.insert((this.selectedstore.getCount()-1),recArr);
            this.availablestore.remove(selected[ctr]);
            this.combostore.remove(selected[ctr]);
        }
        this.availablegrid.getView().refresh();
        this.selectedgrid.getView().refresh();
    },

    movetoleftclicked: function(){
        var selected = this.sm2.getSelections();
        if (selected.length > 0) {
            this.availablestore.add(selected);
            this.combostore.add(selected);
        }
        for (var ctr = 0; ctr < selected.length; ctr++) {
            this.selectedstore.remove(selected[ctr]);
        }
        this.availablegrid.getView().refresh();
        this.selectedgrid.getView().refresh();
    },
    createDisplayGrid: function() {
        
       this.availableRec = new Wtf.data.Record.create([
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
        },{
            name: 'reusablecount',defValue:0
    } 
        ]);
        
        this.ComboRec = new Wtf.data.Record.create([
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
        },{
            name: 'reusablecount',defValue:0
    } 
        ]);
        
        
        this.selectedRec = new Wtf.data.Record.create([
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
        },{
            name: 'reusablecount',defValue:0
      }  
        ]);
        
      var serialbatch=this.obj.record.data.purchasebatchid;
      var stocktype=this.obj.record.data.stocktype;
        var seriallocation=this.obj.record.data.location;
        var serialwarehouse=this.obj.record.data.warehouse;
        var serialrow=this.obj.record.data.row;
        var serialrack=this.obj.record.data.rack;
        var serialbin=this.obj.record.data.bin;
        var batchName=this.obj.record.data.batchName;
        var serialNames=this.obj.record.data.serialNames;
        for(var cnt=this.obj.row;cnt>=0;cnt--){
            var rowData=this.store.getAt(cnt);
            if((!rowData.data.isreadyonly || rowData.data.isreadyonly=="false") && rowData.data.isreadyonly!=undefined){
                serialbatch=rowData.data.purchasebatchid;
                seriallocation=rowData.data.location;
                serialwarehouse=rowData.data.warehouse;
                serialrow=rowData.data.row;
                serialrack=rowData.data.rack;
                serialbin=rowData.data.bin;
                break;
            }
        }
        
        if(!this.isBatchForProduct){
            serialbatch="";
        }
        var serialStoreUrl = "";
        if(this.featchIssuedSerial && this.featchIssuedSerialURL != undefined){
            serialStoreUrl=this.featchIssuedSerialURL    
        }else  if(this.isUnbuildAssembly){
            serialStoreUrl="ACCMaster/getUsedSerialsForAssembly.do"    //ERP-23242
        } else {
            serialStoreUrl="ACCMaster/getNewSerials.do"
        }
        var documentid="";
        if(this.moduleid==Wtf.Acc_ConsignmentDeliveryOrder_ModuleId){
            documentid=(this.isEdit || (this.linkflag))?this.documentid:"";
        }else{
            documentid=(this.isEdit || this.linkflag)?this.documentid:"";
        }
        
        this.availablestore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.availableRec),
            url:serialStoreUrl,
            baseParams:{
                //batch:obj.record.data.purchasebatchid
                mainproduct : this.mainproduct,     //ERP-23242
                isUnbuildAssembly : this.isUnbuildAssembly, //ERP-23242
                subproduct : this.productid,                //ERP-23242
                refno:this.refno,
                batch:serialbatch,
                transType:this.moduleid,
                location:seriallocation,
                warehouse:serialwarehouse,
                row:serialrow,
                rack:serialrack,
                bin:serialbin,
                linkflag:this.linkflag,
                documentid:(this.isEdit && (this.moduleid==Wtf.Acc_Cash_Sales_ModuleId || this.moduleid==Wtf.Acc_Invoice_ModuleId))? this.docrowid : documentid,
                transactionid:this.transactionid,
                productid:this.productid,
                isDO:this.isDO,
                customerID:this.customerID,
                isEdit:this.isEdit,
                copyTrans:this.copyTrans,
                isConsignment:this.isConsignment,
                isForconsignment:this.isForconsignment,
                billid:this.billid,
                serialNames:serialNames,
                isLinkedFromPI:this.isLinkedFromPI,
                batchName:batchName,
                stocktype:(!this.isConsignment&&this.moduleid==27)?stocktype:"",
                docrowid:this.docrowid,
                linkedFrom:this.linkedFrom
            }
        });
         this.selectedstore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.selectedRec)
            
        });
         this.combostore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.ComboRec)
            
        });
        
        this.availablestore.load();
        
        this.availablestore.on("load",function(){
            this.loadedStoreCount++;
            if(this.isfromProductAssembly || this.isUnbuildAssembly){ // ||this.isUnbuildAssembly
                 for(var q = 0; q<Wtf.unqsrno.length;q++){ 
                    for(var r=0;r < this.availablestore.data.items.length; r++){
                        var index=this.availablestore.findBy(function(rec){
                                if(rec.data.serialnoid == Wtf.unqsrno[q].srnoid)
                                    return true;
                        })
                        if(index != -1){
                            this.availablestore.remove(this.availablestore.getAt(index));
                        }
                    }
                }
            }

             for(var q = 0; q<Wtf.dupsrno.length;q++){ 
                for(var r=0;r < this.availablestore.data.items.length; r++){
                    var index=this.availablestore.findBy(function(rec){         //Do not check used Serial Nos in Unbuild Assembly Case
                            if((rec.data.serialnoid == Wtf.dupsrno[q].srnoid))  // && !this.isUnbuildAssembly
                                return true;
                    })
                    if(index != -1){ // && !this.isUnbuildAssembly - Do not check used Serial Nos in Unbuild Assembly Case
                        this.availablestore.remove(this.availablestore.getAt(index));
                    }
                    
                }
              }  
//           this.combostore.add(this.availablestore);
             this.availablestore.each(function(rec){
                    if(rec!= null){
                           this.combostore.add(rec);
                    }
              },this);
//            this.checkLoadMask();
        },this);
        this.availablestore.on("loadexception",function(){
            this.loadedStoreCount++;
//            this.checkLoadMask();
        },this);
        
      this.serialEditor=new Wtf.form.FnComboBox({
            name:'serialno',
            store:this.combostore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
//            typeAhead: true,
            selectOnFocus:true,
            valueField:'id',
//            hideTrigger:true,
            hirarchical:true,
            typeAheadDelay:30000,
            mode: 'local',
            triggerAction:'all',
            displayField:'serialno',
            scope:this,
            forceSelection:true
        });  
      this.sm1 = new Wtf.grid.CheckboxSelectionModel({
       });
        this.availableGridcm= new Wtf.grid.ColumnModel([this.sm1,new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),
            width:200,
            sortable:true,
            dataIndex:'serialno'
        },{
            header:'Exp.From Date',          //remove the ex date from  batch window
            dataIndex:'expstart',
            width:100,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:'Exp.End Date',
            dataIndex:'expend',
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
//            header: header:WtfGlobal.getLocaleText("acc.product.sku"),      
            header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
            sortable:true,
            dataIndex:'skufield'
        }]);
        
     
        this.sm1.on("beforerowselect", function (selectionModel, rowIndex, keepExisting, record) {
            if (this.moduleid == Wtf.Acc_ConsignmentSalesReturn_ModuleId || this.moduleid == Wtf.Acc_Sales_Return_ModuleId) {
                var productId = this.obj.record.data.productid;
                var batchName = this.obj.record.data.batchname;
                if (this.productid != undefined && this.productid != null && this.productid != "") {

                    Wtf.Ajax.requestEx({
                        url: "INVStockLevel/isSerialExists.do",
                        params: {
                            productid: productId,
                            batch: batchName,
                            serial: record.get("serialno"),
                            module:this.moduleid,
                            isEdit:this.isEdit,
                            documentid:(this.isEdit && this.moduleid == Wtf.Acc_Sales_Return_ModuleId)?this.doc_id : ""
                        }
                    },
                    this,
                            function (result, req) {

                                var msg = result.msg;
                                var title = "Error";
                                if (result.success) {
                                    var isSerialExists = result.data.isSerialPresent;
                                    if (isSerialExists == true) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.serial.selected_serial_already_in_system")], 2);
                                        selectionModel.deselectRow(rowIndex);
                                        return false;
                                    }
                                }
                                else if (result.success == false) {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Someerroroccoured")], 2);
                                    selectionModel.deselectRow(rowIndex);
                                    return false;
                                }
                            },
                            function (result, req) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Someerroroccoured")], 2);
                                selectionModel.deselectRow(rowIndex);
                                return false;
                            });
                }
            }
        }, this);
       
        this.sm2 = new Wtf.grid.CheckboxSelectionModel({
       });
         this.selectedGridcm= new Wtf.grid.ColumnModel([this.sm2,new Wtf.grid.RowNumberer(),{
            header:WtfGlobal.getLocaleText("acc.customerList.gridName"),
            width:200,
            dataIndex:'serialno',
            align:'left',
            renderer:this.getComboNameRenderer(this.serialEditor),
            editor:this.serialEditor
        },{
            header:'Exp.From Date',          //remove the ex date from  batch window
            dataIndex:'expstart',
            width:100,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:'Exp.End Date',
            dataIndex:'expend',
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
//            header: header:WtfGlobal.getLocaleText("acc.product.sku"),      
            header:(Wtf.account.companyAccountPref.SKUFieldParm)?(Wtf.account.companyAccountPref.SKUFieldRename!="" && Wtf.account.companyAccountPref.SKUFieldRename!= undefined)?Wtf.account.companyAccountPref.SKUFieldRename:WtfGlobal.getLocaleText("acc.product.sku"):WtfGlobal.getLocaleText("acc.product.sku"),
            hidden:!Wtf.account.companyAccountPref.SKUFieldParm,
            dataIndex:'skufield'
        }]);
    
        
         this.availableQuickSearch = new Wtf.KWLQuickSearchUseFilter({
            emptyText: WtfGlobal.getLocaleText("acc.selectserial.scanserial"),    //"Scan serials ..."
            width: 200,
            id : 'avialblesearch'+this.id,
            field: 'serialno'
        });
        this.selectedQuickSearch = new Wtf.KWLQuickSearchUseFilter({
            emptyText: WtfGlobal.getLocaleText("acc.selectserial.scanserial"),    //"Scan serials ..."
            width: 200,
            id : 'selectedsearch'+this.id,
            field: 'serialno'
        });
        
           
        this.avialbleResetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.avialbleResetBttn.on('click', this.AvaialblehandleResetClick ,this);
        this.selectedResetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), // 'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), // 'Allows you to add a new search term by clearing existing search terms.',
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.selectedResetBttn.on('click', this.selectedhandleResetClick, this);
        
        
        
        this.availablegrid = new Wtf.grid.GridPanel({
            title:WtfGlobal.getLocaleText("acc.selectserial.availableserial"),  //"Available Serials",                     
            height:350,
            width:'97%',
            store: this.availablestore,
            cm: this.availableGridcm,
            sm:this.sm1,
            tbar: [this.availableQuickSearch,this.avialbleResetBttn],
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        
        this.selectedgrid = new Wtf.grid.EditorGridPanel({
            clicksToEdit:1,
            stripeRows :true,
            title: WtfGlobal.getLocaleText("acc.selectserial.selectedserial"),                   //"Selected Serials ",       
            height:350,
            width:'97%',
            store: this.selectedstore,
            cm: this.selectedGridcm,
            sm:this.sm2,
            tbar: [this.selectedQuickSearch,this.selectedResetBttn],//, this.resetBttn
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        
        if(this.selectedstore.getCount()== 0){
                this.addBlankRow(this.selectedstore);
        }

         
         this.availableQuickSearch.StorageChanged(this.availablestore);
         this.selectedQuickSearch.StorageChanged(this.selectedstore);

         this.selectedgrid.on('afteredit',this.updateRow,this);
         this.sm2.on("beforerowselect", this.checkSelections, this);
    },
    checkSelections:function( scope, rowIndex, keepExisting, record){
        if(record.get("id")=="" ||record.get("id")==undefined){
            return false;
        }else{
            return true;
        }
       
    },
//    handleCellClick:function(grid, rowIndex, columnIndex, e) {
//        this.rowIndex = rowIndex;
//    },
    updateRow:function(obj){
        if(obj!=null){
            var rec=obj.record;
             if(obj.field=="serialno"){
                var record=WtfGlobal.searchRecord(this.availablestore, obj.value, 'id');
                rec.set("skufield",record.get("skufield"));
                rec.set("expend",record.get("expend"));
                rec.set("expstart",record.get("expstart"));
                rec.set("purchasebatchid",record.get("purchasebatchid"));
                rec.set("purchaseserialid",record.get("purchaseserialid"));
                rec.set("serialnoid",record.get("serialnoid"));
                rec.set("reusablecount",record.get("reusablecount"));
                this.addBlankRow(this.selectedstore);
                this.availablestore.remove(record);
                this.combostore.remove(record);
                  
                  
             }
        }
    },        
    getComboNameRenderer : function(combo){
        return function(value,metadata,record,row,col,store) {
            var idx = WtfGlobal.searchRecordIndex(combo.store,value,combo.valueField);
            var fieldIndex = "serialno";
            if(idx == -1) {
                if(record.data["serialno"] && record.data[fieldIndex].length>0) {
                    return record.data[fieldIndex];
                }
                else
                    return "";
            }
            var rec = combo.store.getAt(idx);
            var displayField = rec.get(combo.displayField);
            record.set("id", value);
            record.set("serialno", displayField);
            return displayField;
        }
    },

     addBlankRow:function(store){
        var Record = store.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if(f.name!='rowid') {
                blankObj[f.name]='';
                if(!Wtf.isEmpty(f.defValue))
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"? f.defValue.call() : f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        newrec.endEdit();
        newrec.commit();
        store.add(newrec);
//        this.getView().refresh();
    }
   
});


