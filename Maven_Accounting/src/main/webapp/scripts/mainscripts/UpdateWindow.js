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
Wtf.account.GridUpdateWindow = function(config){
    var btnArr=[];
    this.mode = config.mode;
    this.hidebbarinpayterms = (config.hidebbarinpayterms!=undefined?config.hidebbarinpayterms:false);
    this.levelSetting=false;
    /*
     * ERM-735 Associate default Payment Method to customer
     * This flag is used if paymethod is mapped to customer then dont allow to  delete that payment method
     */
    this.mapDefaultPmtMethod =CompanyPreferenceChecks.mapDefaultPaymentMethod();
    if(config.levelSetting){
        this.levelSetting=true;
    }
     if(config.mode==32) {
            this.uPermType=Wtf.UPerm.uom;
            this.permType=Wtf.Perm.uom;
        } else if(config.mode==34) {
            this.uPermType=Wtf.UPerm.tax;
            this.permType=Wtf.Perm.tax;
        } else if(config.mode==92) {
            this.uPermType=Wtf.UPerm.creditterm;
            this.permType=Wtf.Perm.creditterm;
        } else if(config.mode==52) {
            this.uPermType=Wtf.UPerm.paymentmethod;
            this.permType=Wtf.Perm.paymentmethod;
            this.accStore=config.accStore;
//        } else if(config.mode==26) {
        } else if(config.mode==82){
            this.uPermType=3;
            this.permType={edit:2, view:1};
        }else if(config.mode==97) {
            this.uPermType=3;
            this.permType={edit:2, view:1};
        } else if(config.mode==99) {
            this.uPermType=3;
            this.permType={edit:2, view:1};
        }else if(config.mode==100) {
            this.uPermType=3;
                this.permType={edit:2, view:1};
        }else if(config.mode==101){  // Window Populated For Manual JE Post Setting For Control Accounts
            this.uPermType=3;
            this.permType={
                edit:2, 
                view:1
            };     
        }else if(config.mode==102){  //    ERP-27117 :Provide Feature to Edit Excise Unit Window Permission 
             this.uPermType=2;
            this.permType={
                edit:2, 
                view:1
            };     
        }else if(config.mode==Wtf.DISCOUNT_MASTER_MODE){
            this.uPermType=3;
            this.permType={edit:2, view:1};
            this.discountAccountStore=config.discountAccountStore;
        }
        
        if(config.sync==true)  
        {
            if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.synctax)) {
            var synchbtnArray=[];
            synchbtnArray.push(this.SynchTransto=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.Synchto"), //Sync To CRM
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.common.Synchto.tootip"),  //You can Sync Tax(s) to CRM.
                hidden:!Wtf.isCRMSync,//hidden when crm not subscribed
                iconCls:getButtonIconCls(Wtf.etype.syncmenuItem),
                handler:function(){
                    if(!Wtf.account.companyAccountPref.activateCRMIntegration){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.crmnotacivatedalert")],2);
                    }else {
                        this.handleSynch([this.del=["Synchto"],false]); 
                    }                               
                }      
             }));


            synchbtnArray.push(this.SynchTransfrom=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.Synchfrom"),  //Sync From CRM
                scope: this,
                iconCls:getButtonIconCls(Wtf.etype.syncmenuItem),
                tooltip:WtfGlobal.getLocaleText("acc.common.Synchfrom.tootip"),  //You can Sync Tax(s) from CRM.
                hidden:!Wtf.isCRMSync,//hidden when crm not subscribed
                handler:function(){
                    if(!Wtf.account.companyAccountPref.activateCRMIntegration){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.crmnotacivatedalert")],2);
                    }else {
                        this.handleSynchRequest(this.del=["Synchfrom"]);
                    }                               
                }
            }));
            synchbtnArray.push(this.SynchTransToPOS = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.Synchtopos"), //Sync From CRM
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.syncmenuItem),
                tooltip: WtfGlobal.getLocaleText("acc.common.Synchtopos"), //You can Sync Tax(s) from CRM.
                handler: this.handleSynch.createDelegate(this, [this.del = ["Synchto"], true])
            }));
            if(synchbtnArray.length>0) {
                    btnArr.push(this.SynchMenu = new Wtf.Toolbar.Button({
                    text:WtfGlobal.getLocaleText("acc.common.Synch"),           //Sync
                    scope: this,
                    id: 'SyncButtons',
                    iconCls:getButtonIconCls(Wtf.etype.sync),
                    tooltip:WtfGlobal.getLocaleText("acc.field.Allowsyoutosyncthetaxswithotherapplication"), 
                    menu:synchbtnArray
                 }));
                }
            }
        }
        
        if (config.mode == 32 && !this.levelSetting) { // in case of uom add import button
            this.moduleName = "Unit of Measure";
            var extraConfig = {};
            extraConfig.url= "ACCUoM/importUnitOfMeasure.do";
            var extraParams = "";
            var importUOMBtnArray = Wtf.importMenuArray(this, this.moduleName, config.store, extraParams, extraConfig);
            
            this.importUOMBtn = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.import"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.import"),
                iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
                menu: importUOMBtnArray
            });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.importuom)) {
            btnArr.push(this.importUOMBtn);
        }
        }
         this.expButton=new Wtf.exportButton({
            obj:this,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),
            id:"exportTax",
            scope : this,
            menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
            get:Wtf.autoNum.TaxesReport,
            label:WtfGlobal.getLocaleText("acc.ccReport.tab3")
          }); 
          if (config.mode == 34&&!WtfGlobal.EnableDisable(this.uPermType, this.permType.exporttax)) {
             btnArr.push(this.expButton);
          }
        this.updateButton = new Wtf.Button({
                text: WtfGlobal.getLocaleText("acc.common.update"),  //'Update',
                minWidth: 75,
                scope: this,
                handler: this.levelSetting ? this.updateLevels.createDelegate(this) : this.addArr.createDelegate(this)
        });
        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {
            btnArr.push(this.updateButton);
        }
    btnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.close"),  //'Close',
        scope: this,
        handler: function(){
            this.close();
        }
    });
    Wtf.apply(this,{
        buttons: btnArr
    },config);
    Wtf.account.GridUpdateWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.GridUpdateWindow, Wtf.Window, {
    closable: true,
    draggable:false,
    addDeleteCol: true,
    rowDeletedIndexArr:null,
    rowIndexArr:null,
    modal: true,
    iconCls :getButtonIconCls(Wtf.etype.deskera),
    width: 750,
    record:null,
    height: 450,
    resizable: false,
    layout: 'border',
    buttonAlign: 'right',
    initComponent: function(config){
        Wtf.account.GridUpdateWindow.superclass.initComponent.call(this, config);
        if(this.levelSetting || this.mode==101){
            this.addDeleteCol=false
        }
        //ERM-971 Input tax credit check for landed cost feature to include/exclude taxes in landed cost based on this check
        if (this.istax && Wtf.account.companyAccountPref.isActiveLandingCostOfItem) {
            var isMalaysiaOrSingapore = (Wtf.account.companyAccountPref.countryid == Wtf.Country.SINGAPORE || Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA);
            this.isInputCreditColumn = new Wtf.grid.CheckColumn({
                header: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridlandedcostmsg") + "'>" + WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridlandedcost"), 
                dataIndex: 'isInputCreditForTax',
                hidden:!isMalaysiaOrSingapore,
                width: 100
            });
            this.cm.push(this.isInputCreditColumn);
            this.gridPlugins = [this.isInputCreditColumn];
        }
        if(this.addDeleteCol){
            this.cm.push({
                width:50,
                header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  //'Action',
                renderer:this.deleteRenderer.createDelegate(this)
            });
        }
    },

    onRender: function(config){
        this.rowDeletedIndexArr=[];
        this.rowIndexArr=[];
        Wtf.account.GridUpdateWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        if(config.mode==100) {
            this.addGridRec();
        }
        if(this.hidebbarinpayterms){
            this.pagingToolbar.hide();//hide paging tool bar in paymemt terms window
        }
        if(this.note==undefined || this.note==null){
            this.note="";
        }
        var msg='';
        msg = this.mode==101?'<div style="font-size:12px; text-align:left; font-weight:bold; margin-top:1%;">'+ WtfGlobal.getLocaleText("acc.field.Note") +' : </div>'+WtfGlobal.getLocaleText("acc.field.selectcatopostJEOrOtherTransactions"):WtfGlobal.getLocaleText("acc.rem.27")+" "+this.title+this.note;
        this.add({
            region: 'north',
            height: 90,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.title,msg,this.headerImage, true)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid

        });
        this.addEvents({
            'update':true
        });
        this.store=this.grid.getStore();
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('afteredit',this.addGridRec,this);
        this.grid.on('validateedit',this.checkDuplicate,this);
        this.grid.on('beforeedit',this.checkrecord,this);
        this.store.on('beforeload',function(){
            //this.accStore.reload();
            if(this.pP!=undefined&&this.pP.combo!=undefined){
                if(this.pP.combo.value=="All"){
                    var count = this.store.getTotalCount();
                    var rem = count % 5;
                    if(rem == 0){
                        count = count;
                    }else{
                        count = count + (5 - rem);
                    }
                    this.store.paramNames.limit = count;
                }
            }
        },this);
        this.store.on('load',this.addGridRec,this);
    },

    createDisplayGrid:function(){
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)){
            this.grid = new Wtf.grid.EditorGridPanel({
                plugins:this.gridPlugins,
                layout:'fit',
                clicksToEdit:1,
                store: this.store,
                cm: new Wtf.grid.ColumnModel(this.cm),
                border : false,
                loadMask : true,
                viewConfig: {
                    forceFit:true,
                    emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                },
                bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 15,
                    id: "pagingtoolbar" + this.id,
                    store: this.store,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                })
            });
        }else{
            this.grid = new Wtf.grid.GridPanel({
                plugins:this.gridPlugins,
                layout:'fit',
                clicksToEdit:1,
                store: this.store,
                cm: new Wtf.grid.ColumnModel(this.cm),
                border : false,
                loadMask : true,
                viewConfig: {
                    forceFit:true,
                    emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                },
                bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 15,
                    id: "pagingtoolbar" + this.id,
                    store: this.store,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                })
            });
        }
    },
    getdeletedArr:function(grid,index,rec){
        var store=grid.getStore();
        var fields=store.fields;
            var recarr=[];
            if(rec.data['taxid']!=""){
                for(var j=0;j<fields.length;j++){
                    var value=rec.data[fields.get(j).name];
                    switch(fields.get(j).type){
                        case "auto":value="'"+value+"'";break;
                        case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
                    }
                    recarr.push(fields.get(j).name+":"+value);
                }
                recarr.push("modified:"+rec.dirty);
                this.rowDeletedIndexArr.push("{"+recarr.join(",")+"}");
            }
},
    processRow:function(grid,rowindex,e){   
        
        if (e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid, 0, rowindex);
        }
        if (e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid, 1, rowindex);
        }
        
        if (e.getTarget(".delete-gridrow")) {
            var store = grid.getStore();
            var rec = store.getAt(rowindex);
            if (rec.data.isdefault == "true") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.paymentmethod.defaultmethod")], 2);
                return;
              } 
              if (rec.data.isMappedToCustomer == "true" && this.mapDefaultPmtMethod) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.paymentmethod.mappedmethod")], 2);
                return;
            }
            if (this.mode==32 && rec.data.uomname != undefined && rec.data.uomname != "" && rec.data.uomname == "N/A") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uom.deleteexcp")], 2);
                return;
            }
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function (btn) {    //For Tax Window  SDP-10875
                        if (btn != "yes") return;
                        this.getdeletedArr(grid, rowindex, rec);
                store.remove(store.getAt(rowindex));
                this.addGridRec();
            }, this);  
            } else if(e.getTarget(".delete-gridrow1")){
                  var store = grid.getStore();
                  var rec = store.getAt(rowindex);
                  var taxid = rec.get('taxid');    
                  var companyid = rec.get('companyid');  
            Wtf.Ajax.requestEx({
                url: 'ACCTax/checkTax.do',
                params: {
                    companyid: companyid,
                    id: taxid
                }
            }, this, function (response) {
                if (response.success) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function (btn) {
                        if (btn != "yes") return;
                        this.getdeletedArr(grid, rowindex, rec);
                store.remove(store.getAt(rowindex));
                this.addGridRec();
            }, this);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),Wtf.util.Format.ellipsis(response.msg,300)], 0);
        }
            });

        }
    },
    checkrecord:function(obj){
        if(this.istax){
            var idx = this.grid.getStore().find("taxid", obj.record.data["taxid"]);
            if(Wtf.account.companyAccountPref.countryid=='137'){// allow user to change account for tax for Malaysian companies
                if(idx>=0 && obj.field !="accountid" && obj.field!="taxcode"){
                    obj.cancel=true;
                }
            }else{
                if(idx>=0){
                    if(obj.field!="taxcode"){
                        obj.cancel=true;
                    }
                }
            }
        }
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            if(obj.record.data.levelName=='Warehouse'){
                obj.cancel=true;
            }
            if(obj.record.data.levelName=='Locations'){
                obj.cancel=true;
            }
        }
        if((obj.record.data.levelName=='Locations' && !Wtf.account.companyAccountPref.isLocationCompulsory) || (obj.record.data.levelName=='Warehouse' && !Wtf.account.companyAccountPref.isWarehouseCompulsory) || (obj.record.data.levelName=='Row' && !Wtf.account.companyAccountPref.isRowCompulsory) || (obj.record.data.levelName=='Rack' && !Wtf.account.companyAccountPref.isRackCompulsory) || (obj.record.data.levelName=='Bin' && !Wtf.account.companyAccountPref.isBinCompulsory)) {
            obj.cancel=true;  
            return;
        }
      
    },

     checkDuplicate:function(obj){
        if(this.istax &&obj.field=="taxname"){
           var FIND = obj.value;
            FIND =FIND.replace(/\s+/g, '');
            var index=this.grid.getStore().findBy( function(rec){
            var taxname=rec.data['taxname'].trim();
            taxname=taxname.replace(/\s+/g, '');
            if(taxname==FIND)
                return true;
            else
                return false
           })
           if(index>=0){
                obj.cancel=true;
           }
        }
        /*
         * Reverted below code due we have allowed duplicate tax code.
         * ERM-1216
         */
        
//        /*ERP-35905*/
//        if(this.istax &&obj.field=="taxcode"){
//            var FIND = obj.value;
//            FIND =FIND.replace(/\s+/g, '');
//            var index=this.grid.getStore().findBy( function(rec){
//                var taxname=rec.data['taxcode'].trim();
//                taxname=taxname.replace(/\s+/g, '');
//                if(taxname==FIND)
//                    return true;
//                else
//                    return false
//            })
//            if(index>=0){
//                obj.cancel=true;
//            }
//        }
        if(obj.field=="accountid"){
           var allowToChangeAccount=obj.record.data.isChangableAccount;
           var methodname=obj.record.data.methodname
           if(allowToChangeAccount===false){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.prList.accountchangedmsg1")+" <b>"+methodname+"</b>.<br>"+WtfGlobal.getLocaleText("acc.prList.accountchangedmsg2")], 2);
             return false              
           } else{
             return true  
           }
               
        }
        if (obj.field == "uomname" || obj.field == "uomtype") {
            var valid = true;
            if (obj.originalValue == "N/A" && obj.record.data.defaultunitofmeasure == Wtf.NA_UOM_DEFAULTMEASUREOFUOM_ID) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.uom.modifyexcp")], 2);
                valid = false
            }
            return valid;
        }
    },
    addArr:function(){
        var inValidRows = new Array();
        var cm = this.grid.getColumnModel();
        
        var editedarr=[];
         for(var i=0;i<this.store.getCount();i++){
             var   rec=this.store.getAt(i);
            if(rec.dirty){
                editedarr.push(i);

                for(var j=0;j<cm.getColumnCount();j++){
                    var editor = cm.getCellEditor(j,i);
                    var cellData = ""+rec.data[cm.getDataIndex(j)];
                    if(editor != undefined && editor.field.allowBlank !=undefined && !editor.field.allowBlank && cellData.trim().length == 0){
                        if(Wtf.Countryid!="105" && editor.field.hiddenName=="extrataxtypeid" ){
                            continue;
                        } else {
                            inValidRows.push(i+1);
                            break;
                        }
                    }
                }
            }else {
                editedarr.push(i);
            }
        }

        if(inValidRows.length>0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.tax.msg3")+" "+(inValidRows.join(","))], 2);
            return;
        }
        if(this.mode ==101){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.postmanualje.alert"), function(btn){
                if(btn!="yes"){
                    return;
                }else{
                    this.rowIndexArr=editedarr;
                    this.update(editedarr); 
                }
            }, this);
        }else{
            this.rowIndexArr=editedarr;
            this.update(editedarr);
        }
    },
    updateLevels:function(){
         var newLevelNm=[];
         var levelnm=[];
         var levelId=[];
         var isActivate=[];
         var parent=[];
         for(var i=0;i<this.store.getCount();i++){
             var rec=this.store.getAt(i);
             levelnm[i]=rec.data.levelName,
             newLevelNm[i]=rec.data.newLevelName;
             levelId[i]=i+1;
             isActivate[i]=rec.data.isActivate;
             parent[i]=rec.data.parent;
         }
          Wtf.Ajax.requestEx({
                    url:'ACCMaster/updateMasterSetting.do',
                    params: {
                         levelnm : levelnm,    
                         newLevelNm : newLevelNm,
                         levelId : levelId,
                         isActivate : isActivate,
                         parent : parent
                    }
                },this,function(response){
                    if(response.success){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.inventorysetup.updated.sucess")],0);
                        this.close();
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],0);
                    }
                });
    },
    
    handleSynch:function(rec,ispos){
        
        var taxrecord = new Wtf.data.Record.create([
        {
            name: 'taxid'
        },

        {
            name: 'taxname'
        },

        {
            name: 'taxdescription'
        },

        {
            name: 'percent',
            type:'float'
        },

        {
            name: 'taxcode'
        },

        {
            name: 'accountid'
        },

        {
            name: 'taxTypeName'
        },

        {
            name: 'accountname'
        },

        {
            name: 'applydate', 
            type:'date'
        }
        ]);

        var taxstore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },taxrecord),
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33,
                taxtypeid:this.isSales?'2':'1'
            }
        });
        taxstore.load();
        
        var arrID=[];
        taxstore.filterBy(function(rec){
            if(rec.data.syncable)
                return true;
            else return false
        })
        this.arrRec=taxstore.data;
        for(var i=0;i<taxstore.getCount();i++){
            if(this.arrRec[i].data.syncable)
                arrID.push(this.arrRec[i].data['taxid']);
        }
        WtfGlobal.highLightRowColor(this.taxgrid,taxstore,true,0,2);
        if(taxstore.length==0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.NOTaxesinaccounting")],2);
            this.arrRec.clearFilter();
            return;
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg:WtfGlobal.getLocaleText("acc.rem.ShownTaxeswillbe"),  //"Shown Taxes will be syncronized with other application. Are you sure you want to synchronize the Taxes ?",
            width: 560,
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn!="yes"){
                    return;
                }
                else {
                    var URL = "ACCTax/sendTax.do";
                    if (ispos) {
                        URL = "ACCTax/sendTaxToPOS.do";
                    }
                }                    
                WtfGlobal.setAjaxTimeOut();
                Wtf.Ajax.requestEx({
                    url:URL,
                    params: {
                        ids:this.arrRec
                    }
                },this,this.genSyncSuccessResponse,this.genSyncFailureResponse);
            }
        });
    },
    
    genSyncSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();        
        if(!response.companyexist){
           // this.callSubscriptionWin();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.invoice.Tax"),response.msg],response.success*2+1);
        }
        else if(response.success){
             this.accStore.reload();
            if (response.msg != "" && response.msg != undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.invoice.Tax"), response.msg], response.success * 2 + 1);
            } else {
                var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.invoice.Tax"), msg], response.success * 2 + 1);
            }

        }
    },
    genSyncFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();        
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    handleSynchRequest:function(){        
        var taxrecord = new Wtf.data.Record.create([
        {
            name: 'taxid'
        },

        {
            name: 'taxname'
        },

        {
            name: 'taxdescription'
        },

        {
            name: 'percent',
            type:'float'
        },

        {
            name: 'taxcode'
        },

        {
            name: 'accountid'
        },

        {
            name: 'taxTypeName'
        },

        {
            name: 'accountname'
        },

        {
            name: 'applydate', 
            type:'date'
        }
        ]);

        var taxstore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },taxrecord),
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33,
                taxtypeid:this.isSales?'2':'1'
            }
        });
        taxstore.load();
       
        var arrID=[];
        taxstore.filterBy(function(rec){
            if(rec.data.syncable)
                return true;
            else return false
        })
        this.arrRec=taxstore.data;
        for(var i=0;i<taxstore.getCount();i++){
            if(this.arrRec[i].data.syncable)
                arrID.push(this.arrRec[i].data['taxid']);
        }
        WtfGlobal.highLightRowColor(this.taxgrid,taxstore,true,0,2);
        if(taxstore.length==0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.NOTaxesinaccounting")],2);
            this.arrRec.clearFilter();
            return;
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg:WtfGlobal.getLocaleText("acc.rem.Areyousureyouwanttaxesfromotherapplication"),  
            width: 560,
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn!="yes"){
                    return;
                }
                else {
                    var URL="ACCTax/sendTaxRequest.do";
                }                    
                WtfGlobal.setAjaxTimeOut();
                Wtf.Ajax.requestEx({
                    url:URL,
                    params: {
                        ids:this.arrRec
                    }
                },this,this.genSyncSuccessResponse,this.genSyncFailureResponse);
            }
        });
    },
    
    addGridRec:function(e){ 
        var size=this.store.getCount();
        if(this.mode==101){  // Window Populated For Manual JE Post Setting For Control Accounts
            return;
        }
       if(this.mode==Wtf.DISCOUNT_MASTER_MODE){
           if(e != undefined && e.field=="discountvalue" && e.originalValue!=e.value){
                if (e.record.data.discounttype == 1 && e.value > 100) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Discountcannotbegreaterthan100")], 2);
                    e.record.data.discountvalue = e.originalValue != "" ? e.originalValue : 0;
                    e.grid.getView().refresh();
                }
           }
           if(e != undefined && e.field=="discounttype" && e.originalValue!=e.value){
                if (e.value == 1 && e.record.data.discountvalue > 100) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Discountcannotbegreaterthan100")], 2);
                    e.record.data.discountvalue = 0;
                    e.grid.getView().refresh();
                }
           }
       }
        if (this.mode == 92) {                  // 92 = Payment Term moduleid
            if (e != undefined && e.field == "applicabledays" && e.originalValue != e.value && e.record != undefined) {
                if (e.record.data.applicabledays > e.record.data.termdays) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.receiptpayment.applicabedayserrormsg")], 1);
                    e.record.data.applicabledays = e.originalValue;
                    e.grid.getView().refresh();
                }
            }
            if (e != undefined && e.field == "termdays" && e.originalValue != e.value && e.record != undefined && (e.record.data.applicabledays != "" || e.record.data.applicabledays != undefined)) {
                if (e.record.data.applicabledays > e.record.data.termdays) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.receiptpayment.termdayserrormsg")], 1);
                    e.record.data.termdays = e.originalValue;
                    e.grid.getView().refresh();
                }
            }
        }
        if(size>0){
            var lastRec=this.store.getAt(size-1);
            if(this.mode==97){
                if(e !== undefined && e.record !== undefined){
                    if(e.record.get('toDuration') !== ''){
                        if(e.record.get('fromDuration') >= e.record.get('toDuration')){
                            Wtf.MessageBox.show({
                                title: WtfGlobal.getLocaleText("acc.common.information"),
                                msg: WtfGlobal.getLocaleText("acc.duration.msg1"),
                                buttons: Wtf.MessageBox.OK,
                                icon: Wtf.MessageBox.INFO
                            });
                            e.record.set('toDuration', '');
                            return;
                        }
                    }
                }else{
                    if(lastRec.get('toDuration') !== ''){
                        if(lastRec.get('fromDuration') >= lastRec.get('toDuration')){
                            Wtf.MessageBox.show({
                                title: WtfGlobal.getLocaleText("acc.common.information"),
                                msg: WtfGlobal.getLocaleText("acc.duration.msg1"),
                                buttons: Wtf.MessageBox.OK,
                                icon: Wtf.MessageBox.INFO
                            });
                            lastRec.set('toDuration', '');
                            return;
                        }
                    }
                }
            }
            if(this.mode==52){
                if(e!=undefined && e.field!=undefined && e.field=="accountid")   {
                    var accountid=e.record.data.accountid;
                    var rec = WtfGlobal.searchRecord(this.accStore, accountid, 'accountid');
                    var accounttype=rec.data.accounttype;
                    if(accounttype==2){
                        e.record.set('detailtype', 0);  
                    }else if(accounttype==3){
                        e.record.set('detailtype', 2);  
                    }else{
                        e.record.set('detailtype', 1);  
                    }
                }
            }
            var cm=this.grid.getColumnModel();
            var count = cm.getColumnCount();
            for(var i=0;i<count-1;i++){
                if(lastRec.data[cm.getDataIndex(i)].length<=0 && cm.getDataIndex(i) != "srno"){
                    if((this.mode==34 && i==1) || (cm.getDataIndex(i)=="isdefaultcreditterm") || (cm.getDataIndex(i)=="taxdescription") || (cm.getDataIndex(i)=="discountdescription") || (cm.getDataIndex(i)=="discountname") || (cm.getDataIndex(i)=="applicabledays")){                    //Description column can be blank so ignoring it (Discount Name and Applicable days can be blank in Payment Term Window)
                        continue;                    
                    }else{
                        return;
                    }
                }
            }
        }
        var rec=this.record;
        rec = new rec({});
        rec.beginEdit();
        var fields=this.store.fields;
        for(var x=0;x<fields.length;x++){
            var value="";
            rec.set(fields.get(x).name, value);
        }
        /*
         *setting record's taxtypeid to default value 0 (i.e.Both) in case of tax
         **/
        rec.set("taxtypeid", 0);
        if (this.mode == 34 && Wtf.Countryid == Wtf.Country.MALAYSIA) {
            rec.set("activated", true);//Default value "Yes" to "Is Activated" in Tax Report.
        }
        rec.endEdit();
        rec.commit();
        this.store.add(rec);
    },

    deleteRenderer:function(v,m,rec){
        var flag=true;
        var cm=this.grid.getColumnModel();
        var count = cm.getColumnCount();
        for(var i=0;i<count-1;i++){
            if (rec.data[cm.getDataIndex(i)].length <= 0 && cm.getDataIndex(i) != "srno") {
                if(this.mode==34 && i==1 || (cm.getDataIndex(i)=="isdefaultcreditterm") || (cm.getDataIndex(i)=="taxdescription") || (cm.getDataIndex(i)=="discountdescription") || (cm.getDataIndex(i)=="discountname") || (cm.getDataIndex(i)=="applicabledays")){                  //Description column can be blank so ignoring it (Discount Name and Applicable days can be blank in Payment Term Window)
                    flag=true;                
                }else{
                    flag=false;
                    break;
                }
            }
        }
        if(flag){
            if(this.mode==34){      //For Tax Window  SDP-10875
                var deletegriclass=getButtonIconCls(Wtf.etype.deletegridrow1);
                return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow1)+"'></div>";
            }
            if(this.mode!=99||(this.mode==99&&rec.data.id>3)||rec.data.id==="")
            {
                var deletegriclass=getButtonIconCls(Wtf.etype.deletegridrow);
                return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
            }
        }
        return "";
    }, 

    update:function(arr){
        var rec;
        var recData="";
        var finalarray=[];
        if(this.mode==34 || this.mode ==101){
            for(var arrCount=0;arrCount<arr.length;arrCount++)
            {
                var fields=this.store.fields;            
                var recarr=[];
                var record=null;            
                record=this.store.getAt(arr[arrCount]);
                if(record!=undefined)
                {    
                    for(var j=0;j<fields.length;j++){
                        var value=record.data[fields.get(j).name];
                        if((j==1||j==3||j==4) && value==="")
                        {
                            recarr=[];
                            break;                                            
                        }
                        else
                        {    
                            switch(fields.get(j).type){
                                case "auto":
                                    if(value!=undefined){
                                    value=(value+"").trim();
                                }
                                value=encodeURI(value);
                                    value="\""+value+"\"";
                                    break;
                                case "date":
                                    value="'"+WtfGlobal.convertToGenericDate(value)+"'";
                                    break;
                            }
                            recarr.push(fields.get(j).name+":"+value);    
                        }
                    }
                }
                if(recarr.length>0)
                {
                    recarr.push("modified:"+record.dirty);                        
                    finalarray.push("{"+recarr.join(",")+"}");                
                }
            }
            recData="["+finalarray.join(',')+"]";                        
        }else{
            recData=this.getJSONArray(arr);
        }
            
        rec={
            data:recData,
            deleteddata:"["+this.rowDeletedIndexArr.join(',')+"]"
        };

        this.ajxUrl = Wtf.req.account+'CompanyManager.jsp';
        if(this.mode==32) {
            this.ajxUrl = "ACCUoM/saveUnitOfMeasure.do";
        } else if(this.mode==34) {
            this.ajxUrl = "ACCTax/saveTax.do";
        } else if(this.mode==92) {
            this.ajxUrl = "ACCTerm/saveTerm.do";
        } else if(this.mode==52) {
            this.ajxUrl = "ACCPaymentMethods/savePaymentMethod.do";
        } else if(this.mode==26) {
            this.ajxUrl = "ACCProduct/saveProductTypes.do";
        } else if(this.mode==82) {
            this.ajxUrl = "CostCenter/saveCostCenter.do";
        } else if(this.mode==97) {
            this.ajxUrl = "ACCInvoiceCMN/customizedAgedDuration.do";
        } else if(this.mode==99) {
            this.ajxUrl = "ACCCreditNote/saveNoteType.do";
        }else if(this.mode==100) {
            this.ajxUrl = "ACCMaster/savePackages.do";
        } else if(this.mode==101){   // Window Populated For Manual JE Post Setting For Control Accounts
             this.ajxUrl ="ACCCompanyPref/saveManualJePostSettingData.do";
        }else if(this.mode==102){  // ERP-27117 :Provide Feature to Edit Excise Unit Window Permission 
            this.ajxUrl ="ACCInvoice/saveOrUpdateModuleUnit.do";
        }else if(this.mode==Wtf.DISCOUNT_MASTER_MODE){ 
            this.ajxUrl ="AccDiscountController/saveDiscountMaster.do";
        }

       if(rec.deleteddata=="[]"&&rec.data=="[]"){
           if(arr!="")
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.tax.msg2")], 2);
           return;
       }
       else if(Wtf.account.companyAccountPref.countryid !='137' && this.istax&&rec.deleteddata=="[]"){
//             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.tax.msg1"),function(btn){
//                if(btn!="yes") { return; }
                this.updateButton.disable();
                rec.mode=this.mode;
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url : this.ajxUrl,
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
//            },this)
        }
        else{
            this.updateButton.disable();
            rec.mode=this.mode;
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url : this.ajxUrl,
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
        //this.close();
    },

    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.grid,false,arr);
    },
//TODO    gentaxSuccessResponse:function(response){
//       var rec={
//            data:this.getJSONArray(this.rowIndexArr),
//            deleteddata:"["+this.rowDeletedIndexArr.join(',')+"]"
//            };
//        if(response.msg==true&& rec.deleteddata=="[]"){
//             Wtf.MessageBox.confirm("Save Data","Tax details are not editable. Do you wish to continue?",function(btn){
//                if(btn!="yes") { return; }
//            rec.mode=this.mode;
//            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
//                params: rec
//            },this,this.genSuccessResponse,this.genFailureResponse);
//        },this)
//        }
//        else{
//            rec.mode=this.mode;
//            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
//                params: rec
//            },this,this.genSuccessResponse,this.genFailureResponse);
//        }
//    },
//
//    gentaxFailureResponse:function(response){
//        var msg="Failed to make connection with Web Server";
//        if(response.msg)msg=response.msg;
//        WtfComMsgBox(['Alert',msg],2);
//
//    },
//
    genSuccessResponse:function(response){
        if(response.msg){
            WtfComMsgBox([this.title,response.msg],0);
        }
        this.updateButton.enable();
        if(response.success){
            this.rowDeletedIndexArr=[];
            this.fireEvent('update',this);
            this.store.reload();
            if(this.mode==32) {
                Wtf.uomStore.reload();
            } else if(this.mode==34) {
                Wtf.taxStore.reload();
            } else if(this.mode==92) {
                Wtf.termds.reload();
            } else if(this.mode==52) {
                //PaymentMethods
            } else if(this.mode==26) {
                Wtf.productTypeStore.reolad();
            } else if(this.mode==82) {
                if(Wtf.StoreMgr.containsKey("CostCenter")){Wtf.CostCenterStore.reload();}
                if(Wtf.StoreMgr.containsKey("FormCostCenter")){Wtf.FormCostCenterStore.reload();}
            }else if(this.mode == 97){
                if(this.comboStore){
                    this.comboStore.load();
                }
            }else if(this.mode==100) {
                Wtf.packageStore.reload();
            }else if(this.mode==102) {  // ERP-27117 :Load And Close the window
                Wtf.FormUnitStore.reload();
                this.close();
            }else if(this.mode==Wtf.DISCOUNT_MASTER_MODE) {  
                this.discountAccountStore.reload();
            }
            
            //this.close();
        } else{
           this.store.reload(); 
            if(this.mode==102) {  // ERP-27117 :Load And Close the window
                Wtf.FormUnitStore.reload();
                this.close();
            }
        }
    },

    genFailureResponse:function(response){
        this.updateButton.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
    
});  


// ********************************  component for Module Templates   ********************************* //

Wtf.account.moduleTemplatesWindow = function(config){
    
    this.SelectedTemplateStoreUrl = "";
    
    var btnArr=[{
                text:WtfGlobal.getLocaleText("acc.CLOSEBUTTON"),
                scope:this,
                handler:function(){
                    this.close();
                }
            }];
    Wtf.apply(this,{
        buttons: btnArr
    },config);
    Wtf.account.moduleTemplatesWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.moduleTemplatesWindow, Wtf.Window, {
    closable: true,
    draggable:false,
    title:WtfGlobal.getLocaleText("acc.field.Templates"),
    addDeleteCol: true,
    rowDeletedIndexArr:null,
    rowIndexArr:null,
    modal: true,
    iconCls :getButtonIconCls(Wtf.etype.deskera),
    width: 550,
    record:null,
    height: 350,
    resizable: false,
    layout: 'border',
    buttonAlign: 'right',
    initComponent: function(config){
        Wtf.account.moduleTemplatesWindow.superclass.initComponent.call(this, config);
    },
    onRender: function(config){
        Wtf.account.moduleTemplatesWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        this.add({
                    region: 'north',
                    height: 75,
                    border: false,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.field.Templates"),(this.mode == '101' ? WtfGlobal.getLocaleText("acc.field.ViewAndUpdate") : WtfGlobal.getLocaleText("acc.rem.27"))+" "+WtfGlobal.getLocaleText("acc.field.template"),"../../images/accounting_image/calendar.jpg", true)
                },{
                    region: 'center',
                    border: false,
                    baseCls:'bckgroundcolor',
                    layout: 'fit',
                    items:this.templateGrid
                });
    },
    createDisplayGrid:function(){
        this.templateRecord = new Wtf.data.Record.create([
            {
                name: 'templateId'
            },
            {
                name: 'templateName'
            },
            {
                name: 'moduleName'
            },
            {
                name:'moduleRecordId'
            },
            {
                name:'moduleId'
            },
            {
                name: 'deletionFlag'
            },
            {
                name: 'isdefaulttemplate'
            }
        ]);
        
    this.templateStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data", 
                totalProperty: "count"   //ERP-13664 [SJ]
            },this.templateRecord),
            url : "ACCCommon/getModuleTemplate.do"
    });
    this.pg = new Wtf.PagingSearchToolbar({      //ERP-13664 [SJ]
                id: 'pgTbarModule' + this.id,
                pageSize: 15,
                store: this.templateStore,
                displayInfo: true,
                displayMsg: WtfGlobal.getLocaleText("acc.field.Displayingrecords") +'{0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP3 = new Wtf.common.pPageSize({})
            });
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: true
    });
        
    this.templateCM = new Wtf.grid.ColumnModel([this.sm,{
            header: WtfGlobal.getLocaleText("acc.campaigndetails.campaigntemplate.templatename"),
            dataIndex: 'templateName'
        },{
            header: WtfGlobal.getLocaleText("acc.field.TemplateType"),
            dataIndex: 'moduleName'
        },
        {
            width:50,
            header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.isDefaultTemplate"),
            dataIndex: 'isdefaulttemplate',
            renderer:function(a,b,c,d){
                  return "<input type='checkbox'" + (a? "checked='checked'" : "") + ">";
            
          }
        },{
            width:50,
            header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  //'Action',
            dataIndex: 'deletionFlag',
            renderer:function(){
                return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>"
            }
            
        }
        
    ]);  
    
    var tbarArr = new Array();
        tbarArr.push({
            text : WtfGlobal.getLocaleText("acc.field.CreateNewTemplate"),
            scope:this,
            handler:this.newTemplate
        }, {
        text:WtfGlobal.getLocaleText("mrp.workorder.report.editworkorder")+" "+WtfGlobal.getLocaleText("acc.invoiceList.template"),
        scope:this,
        isEditTemplate:true,
        handler: this.viewTemplate.createDelegate(this,[false])
        },{
        text:WtfGlobal.getLocaleText("acc.field.ViewTemplate"),
        scope:this,
        handler: this.viewTemplate.createDelegate(this,[true])
    });
        
    this.templateStore.load({  //ERP-13664 [SJ]
            params:{start:0,
            limit:this.pP3.combo?this.pP3.combo.value:15
            }
        });    
    this.templateGrid = new Wtf.grid.GridPanel({
        store:this.templateStore,
        cm:this.templateCM,
        sm:this.sm,
        tbar : tbarArr,
        viewConfig:{
                forceFit:true
        },
        loadMask:true,
        bbar:this.pg,   //ERP-13664 [SJ]
        autoScroll:true
    });
    
    this.templateGrid.on('rowclick',function(grid,rowindex,e){ 
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
            var store=grid.getStore();
            var rec=store.getAt(rowindex);
            var tempId = rec.get('templateId');
            var moduleName = rec.get('moduleName');
            var templateName = rec.get('templateName');
            var billid = rec.get('moduleRecordId')!=undefined?rec.get('moduleRecordId'):"";
            var moduleId = rec.get('moduleId')!=undefined?rec.get('moduleId'):"";
            Wtf.Ajax.requestEx({
                    url : "AuthHandler/manageModuleTemplates.do",
                    params: {
                        templateId:tempId,
                        moduleName:moduleName,
                        templateName:templateName,
                        billid:billid,
                        moduleid:moduleId
                    }
                },this,function(response){
                    if(response.success){
                             WtfComMsgBox(["Alert",response.msg],2);
                        } 
                        if(!response.success){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.template.for.usedintransaction")],2);
                            store.reload();
                            store.refresh();
                            
                        }
                    
                }, function(){
                    
                })
                store.remove(store.getAt(rowindex));
            }, this);
            
        }
    },this);
    /*
     *Click handler for the IsDefaultTemplate checkbox 
     **/
   this.templateGrid.on('cellclick',function(grid, rowIndex, columnIndex, e) {
            /*
            *Check applied so that to check whether only Default Template cloumn cell is clicked 
            **/
            var fieldName = grid.getColumnModel().getDataIndex(columnIndex);
            if(fieldName=='isdefaulttemplate')          
            { 
                var store=grid.getStore();
                var rec=store.getAt(rowIndex);
                var templateId = rec.get('templateId');
                var moduleName = rec.get('moduleName');
                var templateName = rec.get('templateName');
                var moduleId=rec.get('moduleId');
                var isdefaulttemplate=!rec.get('isdefaulttemplate');
                var msg1="";
                var msg2="";
                if(!rec.get('isdefaulttemplate')){
                    msg1=WtfGlobal.getLocaleText("acc.nee.template.update.areyousure")+" <b>"+templateName+"</b> "+WtfGlobal.getLocaleText("acc.nee.template.update.asdefault");
                    msg2=templateName+" "+WtfGlobal.getLocaleText("acc.nee.template.updated.successfully")
                }else{
                    msg1=WtfGlobal.getLocaleText("acc.nee.template.update.areyousureyouwanttoremove")+" <b>"+templateName+"</b> "+WtfGlobal.getLocaleText("acc.nee.template.update.asdefault");
                    msg2=WtfGlobal.getLocaleText("acc.masterConfig.costCenter.DefaultTemplate")+" <b>"+templateName+"</b> "+WtfGlobal.getLocaleText("acc.nee.template.updated.emovedsuccessfully")
                }    
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText(msg1), function(btn){
                    if(btn!="yes") return;     
                    Wtf.Ajax.requestEx({
                        url: 'ACCCommon/setDefaultModuleTemplate.do',
                        params: {
                            templateId:templateId,
                            moduleName:moduleName,
                            templateName:templateName,
                            moduleId:moduleId,
                            companyid:companyid,
                            isdefaulttemplate:isdefaulttemplate
                        }
                    },this,function(response){
                 
                        if(response.success){
                            if(response.defaultTemplateAlreadySet) {
                                WtfComMsgBox(["Alert","<b>"+response.defaultTemplateName.toString()+"</b >"+WtfGlobal.getLocaleText("acc.nee.defaultTemplateAlreadySet")],2);                 
                                store.refresh();   
                            } else{
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Success"),msg2],0);
                                store.reload();
                                store.refresh();   
                            }
                        }
                         
                        if(!response.success){
                            WtfComMsgBox(["Error",WtfGlobal.getLocaleText("acc.nee.template.update.ErrorUpdating")],2);
                        }
                    },function(response){
                        
                        WtfComMsgBox(["Error",WtfGlobal.getLocaleText("acc.nee.template.update.ErrorUpdating")],2);                 
                           
                        store.refresh();  
                   
                    });
                });
                this.templateGrid.getView().refresh()
            }
           
        },this);
    },
      
    createTemplateHandler : function(){
        var comboCmp = Wtf.getCmp(this.id+"fieldtype_combo");
        if(comboCmp.getValue() != ""){
            this.templateTypeWin.close();
            this.close();
            if(comboCmp.getValue() == Wtf.Acc_Vendor_Invoice_ModuleId){
                callGoodsReceiptTemplate();
            }
//            else if(comboCmp.getValue() == Wtf.Acc_Vendor_BillingInvoice_ModuleId){
//                callBillingGoodsReceiptTemplate();
//            }
            else if(comboCmp.getValue() == Wtf.Acc_Invoice_ModuleId){
                callCustomerInvoiceTemplate();
            }
//            else if(comboCmp.getValue() == Wtf.Acc_BillingInvoice_ModuleId){
//                callBillingInvoiceTemplate();
//            }
            else if(comboCmp.getValue() == Wtf.Acc_Purchase_Order_ModuleId){
                callPurchaseOrderTemplate();
            }
//            else if(comboCmp.getValue() == Wtf.Acc_BillingPurchase_Order_ModuleId){
//                callBillingPurchaseOrderTemplate();
//            }
            else if(comboCmp.getValue() == Wtf.Acc_Sales_Order_ModuleId){
                callSalesOrderTemplate();
            }
//            else if(comboCmp.getValue() == Wtf.Acc_BillingSales_Order_ModuleId){
//                callBillingSalesOrderTemplate();
//            }
            else if(comboCmp.getValue() == Wtf.Acc_Cash_Purchase_ModuleId){
                callPurchaseReceiptTemplate();
            }
//            else if(comboCmp.getValue() == Wtf.Acc_BillingCash_Purchase_ModuleId){
//                callBillingPurchaseReceiptTemplate();
//            }
//            else if(comboCmp.getValue() == Wtf.Acc_Billing_Cash_Sales_ModuleId){
//                callBillingSalesReceiptTemplate();
//            }
            else if(comboCmp.getValue() == Wtf.Acc_Cash_Sales_ModuleId){
                callSalesReceiptTemplate();
            }else if(comboCmp.getValue() == Wtf.Acc_GENERAL_LEDGER_ModuleId){
                callJournalEntryTabTemplate(false,undefined,undefined,"1");
            }
            
            else if(comboCmp.getValue() == Wtf.Acc_Customer_Quotation_ModuleId){
                callQuotation(undefined, undefined, undefined, undefined, true);
            }else if(comboCmp.getValue() == Wtf.Acc_Stock_Request_ModuleId){
                callStockRequestTemplate();
            }
        }else{
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.information"),
                msg: WtfGlobal.getLocaleText("acc.field.PleaseSelectaTemplateTypeFirst"),
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO
           });
        }
    },
    
    viewTemplate:function(isView){
        /**
         * isTemplate for create template
         * isViewtemplate for view template
         * isEditTemplate for edit template
         */
        if(isView){
            this.isViewTemplate=true;
            this.isEditTemplate=false;
        }else{
            this.isEditTemplate=true;
            this.isViewTemplate=false;
        }
        var rec = this.templateGrid.getSelectionModel().getSelected();
        if(rec == undefined || rec == null){
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.information"),
                msg: WtfGlobal.getLocaleText("acc.field.PleaseSelectaTemplateFirst"),
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO
           });
            return;
        }
        var moduleRecId = rec.get('moduleRecordId');
        var moduleId = rec.get('moduleId');
        this.cash = (moduleId == Wtf.Acc_Cash_Sales_ModuleId || moduleId == Wtf.Acc_Cash_Purchase_ModuleId)? true : false;
        if(moduleId == Wtf.Acc_GENERAL_LEDGER_ModuleId){
            this.SelectedTemplateStoreUrl=  "ACCReports/getJournalEntry.do"; 
        }else{
            var isOrder = this.isOrder(moduleId);
            var isQuotation= this.isQuotation(moduleId);
            var isCustomer = this.isCustomer(moduleId);


            if(isOrder){
                this.SelectedTemplateStoreUrl= isCustomer ? "ACCSalesOrderCMN/getSalesOrdersMerged.do":"ACCPurchaseOrderCMN/getPurchaseOrdersMerged.do"  
            }else if(isQuotation){
                this.SelectedTemplateStoreUrl = isCustomer? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
            }else{
                this.SelectedTemplateStoreUrl= isCustomer ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
            }
       
        }
        
  if(moduleId == Wtf.Acc_GENERAL_LEDGER_ModuleId){
      this.SelectedTemplateRec = Wtf.data.Record.create ([
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'companyname'},
        {name:'companyid'},
        {name:'eliminateflag'},
        {name:'deleted'},
        {name:'entrydate',type:'date'},
        {name:'memo'},
        {name:'jeDetails'},
        {name:'transactionID'},
        {name:'billid'},
        {name:'noteid'},
        {name:'type'},
        {name:'transactionDetails'},
        {name:'costcenter'},
        {name:'currencyid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'childCount'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'parentje'},
        {name:'isreverseje',type:'boolean'},
        {name:'reversejeno'},
        {name:'withoutinventory'},
        {name:'revaluationid'},
        {name:'externalcurrencyrate'},
        {name:'typeValue'},
        {name:'partlyJeEntryWithCnDn'}
    ]);
  }else{ 
     this.SelectedTemplateRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'termdays'},
        {name:'termid'},
        {name:'termname'},
        {name:'incash'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'discountval'},
        {name:'crdraccid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'porefno'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'rowproductname'},
        {name:'rowquantity'},
        {name:'rowrate'},
        {name:'rowprdiscount'},
        {name:'rowprtaxpercent'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'salesPerson'},
        {name:'billingAddress'},
        {name:'billingCountry'},
        {name:'billingState'},
        {name:'billingPostal'},
        {name:'billingEmail'},
        {name:'billingFax'},
        {name:'billingMobile'},
        {name:'billingPhone'},
        {name:'billingContactPerson'},
        {name:'billingContactPersonNumber'},
        {name:'billingContactPersonDesignation'},
        {name:'billingWebsite'},
        {name:'billingCounty'},
        {name:'billingCity'},
        {name:'billingAddressType'},
        {name:'shippingAddress'},
        {name:'shippingCountry'},
        {name:'shippingState'},
        {name:'shippingCounty'},
        {name:'shippingCity'},
        {name:'shippingEmail'},
        {name:'shippingFax'},
        {name:'shippingMobile'},
        {name:'shippingPhone'},
        {name:'shippingPostal'},
        {name:'shippingContactPersonNumber'},
        {name:'shippingContactPersonDesignation'},
        {name:'shippingWebsite'},
        {name:'shippingContactPerson'},
        {name:'shippingAddressType'},
        {name:'includeprotax',type:'boolean'},
        {name:'termdetails'},
        {name:'shippingterm'},
        {name:'customerporefno'},
        {name:'shiplengthval'},
//        {name:'manufacturerType'},
        {name:'registrationType'},
        {name:'UnitName'},
        {name: 'ECCNo'},
        {name: 'populateproducttemplate'},
        {name: 'populatecustomertemplate'},
        {name: 'populateautodointemp'},
        {name: 'gstIncluded'},
        {name: 'agent'},
        {name: 'landedInvoiceID'},
        {name: 'tdsamount'},
        {name: 'tdsrate'},
        {name: 'TotalAdvanceTDSAdjustmentAmt'},
        {name: 'AdvancePaymentNumber'},
        {name: 'AdvancePaymentID'},
        {name: 'totalAmountWithTDS'},
        {name: 'landedInvoiceNumber'},
        {name: 'companyunitid'}, // Added record column for Excise Unit ID for INDIAN Subdomain
        {name: 'methodid'},
        {name: 'vendcustShippingAddressType'},
        {name: 'CustomerVendorTypeId'},
        {name: 'GSTINRegistrationTypeId'},
        {name: 'GSTINRegTypeDefaultMstrID'},
        {name: 'gstin'},
        {name: 'isapplytaxtoterms'}
        
    ]);
  } 
      
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
        
        this.SelectedTemplateStore.on('load', this.fillData,this);
        
        this.SelectedTemplateStore.load({
            params:{
                billid:moduleRecId,
                isForTemplate:true
            }
        });
    },
    
    newTemplate:function(){
        var dataArr = new Array();
        dataArr.push([Wtf.Acc_Invoice_ModuleId,'Sales Invoice'],[Wtf.Acc_Vendor_Invoice_ModuleId,'Purchase Invoice'],[Wtf.Acc_Purchase_Order_ModuleId,'Purchase Order'],[Wtf.Acc_Sales_Order_ModuleId,'Sales Order'],[Wtf.Acc_Cash_Purchase_ModuleId,'Cash Purchase'],[Wtf.Acc_Cash_Sales_ModuleId,'Cash Sales'],[Wtf.Acc_GENERAL_LEDGER_ModuleId,'Journal Entry'],[Wtf.Acc_Customer_Quotation_ModuleId,'Customer Quotation'],
        [Wtf.Acc_Stock_Request_ModuleId,'Stock Request']);
        
        this.templateTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :dataArr
        });
   
        var form_panel =   new Wtf.Panel({
            border:false,
            items:[{
                xtype:"form",
                border:false,
                id:"cstfrm"+this.id,
                autoScroll:true,
                labelWidth:150,
                items:[{
                        xtype:"combo",
                        fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectTemplateType"),
                        name:"fieldType",
                        id:this.id+"fieldtype_combo",
                        allowBlank : false,
                        store:this.templateTypeStore,
                        defaultValue:0,
                        emptyText:WtfGlobal.getLocaleText("acc.field.SelectTemplateType"),
                        displayField:"name",
                        valueField:'typeid',
                        mode:'local',
                        typeAhead:true,
                        triggerAction:'all',
                        width:150
                    }]
                }]
        });
        
       this.templateTypeWin =  new Wtf.Window({
                    title: WtfGlobal.getLocaleText("acc.field.TemplateType"),
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    width: 370,
                    height: 150,
                    bodyStyle:{
                        padding:'30px 0px 0px 20px',
                        background:'#f1f1f1'
                    },
                    resizable: false,
                    closable: true,
                    buttonAlign: 'right',
                    modal:true,
                    items:[form_panel],
                    buttons:[
                    {
                        text:WtfGlobal.getLocaleText("acc.field.Create"),
                        scope:this,
                        handler:this.createTemplateHandler
                    },
                    {
                        text:WtfGlobal.getLocaleText("acc.common.close"),
                        scope:this,
                        handler:function(){
                            this.templateTypeWin.close();
                        }
                    }
                ]
        });
        
        this.templateTypeWin.doLayout();
        this.templateTypeWin.show();
    },
    
fillData : function(store){
    var rec = store.getAt(0);
    this.openModuleTab(rec);
    this.close();
},

openModuleTab:function(formrec){
//     this.isViewTemplate = true;
    var copyInv = false;
    var rec = this.templateGrid.getSelectionModel().getSelected();
    var templateId = rec.get('templateId');
    var templateName=rec.get('templateName');
    var moduleId = rec.get('moduleId');
    
    var isOrder = this.isOrder(moduleId);
    var isQuotation= this.isQuotation(moduleId);
    if(moduleId == Wtf.Acc_GENERAL_LEDGER_ModuleId){
        callJournalEntryTabTemplate(true, formrec, formrec.get("billid"), "1",this.isViewTemplate,templateName,templateId,this.isEditTemplate)
    }else{
        WtfGlobal.openModuleTab(this, this.isCustomer(moduleId), isQuotation, isOrder, copyInv, templateId, formrec,this.isViewTemplate,this.isEditTemplate);
    }
},

isCustomer:function(moduleId){
    var isCustomer = false;
    
    if(moduleId == Wtf.Acc_Invoice_ModuleId || moduleId == Wtf.Acc_BillingInvoice_ModuleId || moduleId == Wtf.Acc_BillingSales_Order_ModuleId || moduleId == Wtf.Acc_Sales_Order_ModuleId || moduleId == Wtf.Acc_Cash_Sales_ModuleId || moduleId == Wtf.Acc_Billing_Cash_Sales_ModuleId || moduleId == Wtf.Acc_Customer_Quotation_ModuleId){
        isCustomer = true;
    }
    return isCustomer;
},

isOrder:function(moduleId){
    var isOrder = false;
    
    if(moduleId == Wtf.Acc_Purchase_Order_ModuleId || moduleId == Wtf.Acc_BillingPurchase_Order_ModuleId || moduleId == Wtf.Acc_Sales_Order_ModuleId || moduleId == Wtf.Acc_BillingSales_Order_ModuleId){
        isOrder = true;
    }
    return isOrder;
},

isQuotation:function(moduleId){
    var isQuotation = false;
    
    if(moduleId == Wtf.Acc_Customer_Quotation_ModuleId || moduleId == Wtf.Acc_Vendor_Quotation_ModuleId){
        isQuotation = true;
    }
    return isQuotation;
}
    
})

Wtf.InvoiceTermWindow= function(config){
    var btnArr=[];
    btnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.close"),  //'Close',
        scope: this,
        handler: function(){
            this.close();
        }
    });
    Wtf.apply(this,{
        buttons: btnArr
    },config);
    Wtf.InvoiceTermWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.InvoiceTermWindow, Wtf.Window, {
    closable: true,
    draggable:false,
    modal: true,
//    title :this.isSales?'Sales Term':'Purchase Term',
    iconCls :getButtonIconCls(Wtf.etype.deskera),
    width: 650,
    record:null,
    height: 575,
    resizable: false,
    layout: 'border',
    buttonAlign: 'right',
    initComponent: function(config){
        Wtf.InvoiceTermWindow.superclass.initComponent.call(this, config);
    },

    onRender: function(config){
        this.rowDeletedIndexArr=[];
        this.rowIndexArr=[];
        Wtf.InvoiceTermWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.isSales?WtfGlobal.getLocaleText("acc.master.invoice.salesterm"):WtfGlobal.getLocaleText("acc.master.invoice.purchaseterm"),this.isSales?WtfGlobal.getLocaleText("acc.master.invoice.salesterm"):WtfGlobal.getLocaleText("acc.master.invoice.purchaseterm"),this.headerImage, true)
        },
        {region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.taxgrid ,
            margins: '0 5 5 5'
        },{
            region: 'south',
           margins: '0 5 5 5',
            border: false,
            baseCls:'bckgroundcolor',
            height:200,
            layout: 'fit',
            items:this.grid

        });
        this.addEvents({
            'update':true
        });
    },

    createDisplayGrid:function(){
      
        var taxrecord = new Wtf.data.Record.create([
           {name: 'taxid'},
           {name: 'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'taxTypeName'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'},
           {name: 'termname'},
           {name: 'termid'},
           {name: 'hasAccess'},
           {name:'extrataxtypeid'}
        ]);

        var taxstore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",        //ERP-13650 [SJ]
                totalProperty: "count"
            },taxrecord),
            //            url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCAccountCMN/getTax.do",
            baseParams:{
                mode:33,
                taxtypeid:this.isSales?'2':'1'
            }
        });
                
        this.taxpg = new Wtf.PagingSearchToolbar({ //ERP-13650 [SJ]
                id: 'pgTbarModule' + this.id,
                pageSize: 5,
                store: taxstore,
                displayInfo: true,
                displayMsg: WtfGlobal.getLocaleText("acc.field.Displayingrecords") +'{0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP3 = new Wtf.common.pPageSize({})
            });
        
        taxstore.load({   //ERP-13650 [SJ]
            params:{
                start:0,
                limit:this.pP3.combo?this.pP3.combo.getValue():5
            }
        });
        this.selectModel = new Wtf.grid.CheckboxSelectionModel();
        
       this.taxcm= [
               this.selectModel,
           {
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridName"),  //"Name",
            dataIndex: 'taxname'
        },
        {
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridDescription"),  //"Description",
            dataIndex: 'taxdescription',
            renderer : function(val,md,rec) {
                return "<div wtf:qtip=\""
                + val
                + "\" wtf:qtitle='"
                + "'>" + val + "</div>";
            }
        }
        ,{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridPercent"),  //"Percent",
            dataIndex: 'percent',
            renderer:function(val){
                if(typeof val != "number") return "";
                return val+'%';
            }
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridApplyDate"),  //"Apply Date",
            dataIndex: 'applydate',
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridTaxCode"),  //"Tax Code",
            dataIndex:'taxcode'
           
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridAccountName"),  //"Account Name",
            dataIndex: 'accountname'
           },
           {
            header:WtfGlobal.getLocaleText("acc.coa.gridAccType"),
            dataIndex: 'taxTypeName'
          
        },
        {
            header:WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridTaxtype"),
            dataIndex: 'extrataxtypeid',
            hidden:Wtf.Countryid=="105"?false:true
        },
        {
            header: WtfGlobal.getLocaleText("acc.field.GSTIncludingTerms"),
            dataIndex: 'termname',
            renderer : function(val,m,rec) {
                 val = val.replace(/(<([^>]+)>)/ig,"");
                 if(rec.data.deleted)
                 val='<del>'+val+'</del>';
                 return "<div wtf:qtip=\""+val+"\">"+val+"</div>";
            }

        }];
      this.taxgrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            store: taxstore,
            cm: new Wtf.grid.ColumnModel(this.taxcm),
            sm: this.selectModel,
            border: false,
            loadMask: true,
            tbar: [{
                    text: WtfGlobal.getLocaleText("acc.field.SetTerm"),
                    scope: this,
                    handler: function () {
                        this.setTermForTax()
                    },
                    iconCls: 'pwnd save'
                }],
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            },
            bbar:this.taxpg  //ERP-13650 [SJ]
        });
        this.cm=[{
            header: WtfGlobal.getLocaleText("acc.field.Term"),
            dataIndex: 'term'
        },{
            header: WtfGlobal.getLocaleText("acc.master.invoiceterm.glaccount"),
            dataIndex: 'glaccountname'
        },
        {
            header: WtfGlobal.getLocaleText("acc.coa.accCode"),  //"Account Name",
            dataIndex: 'accode'
           },
           {
            header: WtfGlobal.getLocaleText("acc.master.invoiceterm.formula"),
            dataIndex: 'formula'
        },{
            header: WtfGlobal.getLocaleText("acc.master.invoiceterm.calsign"),
            dataIndex: 'sign',
            renderer:function(val){
                if(val=='1')
                    return 'Plus(+)'
                else
                    return 'Minus(-)'
            }
        },{
            header: WtfGlobal.getLocaleText("acc.master.invoiceterm.category"),
            dataIndex: 'category',
            hidden : true,
            renderer:function(val){
                if(val=='1')
                    return 'GST'
                else
                    return 'None'
            }
        },{
            header: WtfGlobal.getLocaleText("acc.master.invoiceterm.inclusiveofgst"),
            dataIndex: 'inclusiveofgst',
            hidden : true,
            renderer:function(val){
                if(val=='1')
                    return 'Yes'
                else
                    return 'No'
            }
        },{
            header: WtfGlobal.getLocaleText("acc.master.invoiceterm.includeofprofitability"),
            dataIndex: 'includeprofitability',
            hidden : true,
            renderer:function(val){
                if(val=='1')
                    return 'Yes'
                else
                    return 'No'
            }
        },{
            header: WtfGlobal.getLocaleText("acc.master.invoiceterm.suppresszeroamount"),
            dataIndex: 'suppresszeroamount',
            hidden : true,
            renderer:function(val){
                if(val=='1')
                    return 'Yes'
                else
                    return 'No'
            }
        },{
            header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.TermSelGrid.includeInTDSCalculation") +"'>"+ WtfGlobal.getLocaleText("acc.TermSelGrid.includeInTDSCalculation") +"</div>" ,
            dataIndex: 'includeInTDSCalculation',
            width:30,
            hidden:(Wtf.isTDSApplicable && !this.isSales)?false :true,
            renderer: function (v, p) {
                p.css += ' x-grid3-check-col-td';
                return '<div class="x-grid3-check-col' + (v ? '-on' : '') + ' x-grid3-cc-' + this.id + '">&#160;</div>';
            }
        },{
                header: WtfGlobal.getLocaleText("acc.master.invoiceterm.isTermActive"),
                dataIndex: 'isTermActive',
                renderer: function (a, b, c, d) {
                    return "<input type='checkbox'" + (a ? "checked='checked'" : "") + ">";
                }
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
            align:'center',
            width:40,
            renderer: this.deleteRenderer.createDelegate(this)
        }];
        this.termRec =new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'term'},
        {name: 'glaccount'},
        {name:'glaccountname'},
        {name: 'accode'},
        {
            name: 'sign'
        },{
            name: 'category'
        }, {
            name: 'includegst'
        },{
            name: 'includeprofit'
        },{
            name: 'suppressamnt'
        },{
            name: 'formula'
        },{
            name:'isTermActive'
        },{
            name: 'istermused'
        },{name: 'includeInTDSCalculation'}
        ]);
        
        this.termStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.termRec),
            url: 'ACCAccount/getInvoiceTermsSales.do',
            baseParams:{
                mode:51,
                isSalesOrPurchase:this.isSales,
                showActiveDeactiveTerms:true         //showActiveDeactiveTerms is sent true in case of term master and edit transaction only to get both active and deactive terms also 
            }
        });
        this.termStore.load();
        
        this.grid = new Wtf.grid.GridPanel({
            layout:'fit',
            store: this.termStore,
            cm: new Wtf.grid.ColumnModel(this.cm),
            border : false,
            loadMask : true,
            tbar : [{
                text: WtfGlobal.getLocaleText("acc.rem.138"),
                scope: this,
                handler: function(){
                    this.createNewTerm()
                },
                iconCls: 'pwnd save'
            }],
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.grid.on('rowclick',this.handleRowClick,this);
        this.grid.on('cellclick',this.handleCellClick,this);
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handleCellClick:function(grid, rowIndex, columnIndex, e) {
        
        /*
            *Check applied so that to check whether only Activate Term cloumn cell is clicked 
            **/
            var fieldName = grid.getColumnModel().getDataIndex(columnIndex);
            if(fieldName=='isTermActive')          
            { 
                var store=grid.getStore();
                var rec=store.getAt(rowIndex);
                var termId = rec.get('id');
                var term = rec.get('term');
                var isTermActive=!rec.get('isTermActive');
                var msg1="";
                var msg2="";
                if(!rec.get('isTermActive')){
                    msg1=WtfGlobal.getLocaleText("acc.field.areyousure")+" <b>"+term+"</b> ";
                    msg2=" <b>"+term+"</b> "+WtfGlobal.getLocaleText("acc.field.termstatus.updated.successfully")
                }else{
                    msg1=WtfGlobal.getLocaleText("acc.field.termstatus.update.areYouSureYouWantToDeactivate")+" <b>"+term+"</b> ";
                    msg2=" <b>"+term+"</b> "+WtfGlobal.getLocaleText("acc.field.termstatus.updated.deactivatedSuccessfully")
                }    
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText(msg1), function(btn){
                    if(btn!="yes") return;     
                    Wtf.Ajax.requestEx({
                        url: 'ACCAccount/setInvoiceTermsSalesActive.do',
                        params: {
                        termId: termId,
                        term: term,
                        isTermActive: isTermActive,
                        companyid: companyid,
                        }
                    },this,function(response){
                 
                        if(response.success){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Success"), msg2], 0);
                            store.reload();
                            grid.getView().refresh();
                            this.taxgrid.store.reload();
                        }
                         
                        if(!response.success){
                            WtfComMsgBox(["Alert",response.msg],2);
                            store.reload();
                        }
                    },function(response){
                        
                        WtfComMsgBox(["Error",WtfGlobal.getLocaleText("acc.master.invoiceterm.update.ErrorUpdating")],2);                 
                           
                        store.refresh();  
                   
                    });
                }.createDelegate(this));
            }
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var rec = grid.getStore().getAt(rowindex);
                 Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                    url: 'ACCAccountCMN/deleteInvoiceSalesTerms.do',
                    params: {
                                termid : rec.data.id,
                                term : rec.data.term,
                                isSalesOrPurchase : rec.store.baseParams.isSalesOrPurchase
                            }
                },this,this.genTermDeleteSuccessResponse,this.genTermDeleteFailureResponse);
            }, this);
        }
    },
    
    genTermDeleteSuccessResponse:function(response){
        if(response.success){ 
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Success"),response.msg],0);
            this.termStore.reload();
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        }
    },

    genTermDeleteFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    getdeletedArr:function(grid,index,rec){
        var store=grid.getStore();
        var fields=store.fields;
        var recarr=[];
        if(rec.data['taxid']!=""){
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                switch(fields.get(j).type){
                    case "auto":
                        value="'"+value+"'";
                        break;
                    case "date":
                        value="'"+WtfGlobal.convertToGenericDate(value)+"'";
                        break;
                }
                recarr.push(fields.get(j).name+":"+value);
            }
            recarr.push("modified:"+rec.dirty);
            this.rowDeletedIndexArr.push("{"+recarr.join(",")+"}");
        }
    },
    
    processRow:function(grid,rowindex,e){        
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var rec=store.getAt(rowindex);
                this.getdeletedArr(grid,rowindex,rec);
                store.remove(store.getAt(rowindex));
                this.addGridRec();
            }, this);
        }
    },
    
    checkrecord:function(obj){
        if(this.istax){
            var idx = this.grid.getStore().find("taxid", obj.record.data["taxid"]);
            if(idx>=0)
                obj.cancel=true;
        }
    },

    checkDuplicate:function(obj){
        if(this.istax &&obj.field=="taxname"){
            var FIND = obj.value;
            FIND =FIND.replace(/\s+/g, '');
            var index=this.grid.getStore().findBy( function(rec){
                var taxname=rec.data['taxname'].trim();
                taxname=taxname.replace(/\s+/g, '');
                if(taxname==FIND)
                    return true;
                else
                    return false
            })
            if(index>=0){
                obj.cancel=true;
            }
        }
    },

    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.grid,false,arr);
    },
    genSuccessResponse:function(response){
        WtfComMsgBox([this.title,response.msg],0);
        if(response.success){    
            this.fireEvent('update',this);
            this.store.reload();
            if(this.mode==32) {
                Wtf.uomStore.reload();
            } else if(this.mode==34) {
                Wtf.taxStore.reload();
            } else if(this.mode==92) {
                Wtf.termds.reload();
            } else if(this.mode==52) {
            //PaymentMethods
            } else if(this.mode==26) {
                Wtf.productTypeStore.reolad();
            } else if(this.mode==82) {
                if(Wtf.StoreMgr.containsKey("CostCenter")){
                    Wtf.CostCenterStore.reload();
                }
                if(Wtf.StoreMgr.containsKey("FormCostCenter")){
                    Wtf.FormCostCenterStore.reload();
                }
            }else if(this.mode == 97){
                if(this.comboStore){
                    this.comboStore.load();
                }
            }
            this.close();
        }
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },
    
    createNewTerm : function(config) {
        this.term=new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Term"),
            name: 'term',
            scope:this,
//            anchor:'100%', 
            width:200,
            allowBlank:false
        });
        
        this.isTermActive = new Wtf.form.Checkbox({
            name:'isTermActive',
            hiddenName:"isTermActive",
            id:'isTermActive',
            fieldLabel:"<span wtf:qtip='" +WtfGlobal.getLocaleText("acc.master.invoiceterm.isTermActive.tooltip") +"'>"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.isTermActive")+"</span>",    
            itemCls:"chkboxalign",
            checked:true
        });
        this.accRec = Wtf.data.Record.create ([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'currencyid',mapping:'currencyid'},
            {name:'acccode'},
            {name:'groupname'}
        ]);

        this.accStore = new Wtf.data.Store({
            //        url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.cmbAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
            //                id:'accountIdForCombo'+config.id ,
            name:'accountid',
            store:this.accStore,
            valueField:'accountid',
            displayField:'accountname',
            mode: 'local',
            minChars:1,
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
//            anchor:'100%',
            width:200,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            hiddenName:'accountid',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectAccount"),
            allowBlank:false,
            forceSelection:true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            triggerAction:'all'
        });
//        this.accStore.load();
        this.accStore.load({params: {mode: 2 , ignoreAssets: true,ignorecustomers: true, ignorevendors: true}});

        this.formulaRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'term'
        }
        ]);
        this.formulaStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.formulaRec),
            url: 'ACCAccount/getInvoiceTermsSales.do',
            baseParams:{
                isSalesOrPurchase:this.isSales
        }
        });
        this.formulaStore.on("load",function(store, rec, options){
            var blankObj={};
            blankObj['id'] = 'Basic';
            blankObj['term'] = 'Basic';
            var newrec = new this.formulaRec(blankObj);
            this.formulaStore.insert(0,newrec);
        },this);
        
        this.formulaStore.load();
        
        this.FormulaComboconfig = {
            hiddenName:"formula",
            store: this.formulaStore,
            valueField:'id',
            displayField:'term',
            emptyText:WtfGlobal.getLocaleText("acc.field.Selectformula"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,                            
            allowBlank:false,
            triggerAction:'all',
            scope:this
        };

        this.formulaCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.master.invoiceterm.formula") ,
            forceSelection:true,
            width: 200
//            anchor:'100%'
        },this.FormulaComboconfig));
        
        this.formulaCombo.on('render',function(){             //to avoid editing combo value by user
        this.formulaCombo.setEditable(false); 
        },this);
        
        this.includeInTDSCalcheckbox = new Wtf.form.Checkbox({
            name:'includeInTDSCalculation',
            hiddenName:"includeInTDSCalculation",
            id:'includeInTDSCalculation',
            hidden:(Wtf.isTDSApplicable && !this.isSales)?false :true,
            hideLabel:(Wtf.isTDSApplicable && !this.isSales)?false :true,
            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TermSelGrid.includeInTDSCalculation") +"'>"+ WtfGlobal.getLocaleText("acc.TermSelGrid.includeInTDSCalculation")+"</span>",//Include In TDS Calculation. existing Product,            
            itemCls:"chkboxalign"
        });
        
            var array = [this.term, this.cmbAccount,this.formulaCombo,this.isTermActive,this.includeInTDSCalcheckbox,{
            xtype:'fieldset',
            autoHeight:true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.CalculateSign"),
                xtype : 'radiogroup',
                vertical: false,
                id:"_sign"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.field.Plus+"), 
                    name: 'sign', 
                    inputValue: '1', 
                    checked:true
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.field.Minus-"), 
                    name: 'sign', 
                    inputValue: '0'
                }
                ]    
            }]
        },{
            xtype:'fieldset',
            autoHeight:true,
            hidden : true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.cust.category"),
                xtype : 'radiogroup',
                vertical: false,
                id:"_category"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.rem.vrnet.196"), 
                    name: 'category', 
                    inputValue: '1'
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.rem.111"), 
                    name: 'category', 
                    inputValue: '0', 
                    checked:true
                }
                ]
                }
            ]
        },{
            xtype:'fieldset',
            autoHeight:true,
            hidden : true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.master.invoiceterm.inclusiveofgst"),
                xtype : 'radiogroup',
                vertical: true,
                id:"_includegst"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.yes"), 
                    name: 'includegst', 
                    inputValue: '1'
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.fxexposure.invno"), 
                    name: 'includegst', 
                    inputValue: '0', 
                    checked:true
                }
                ]    
            }]
        },{
            xtype:'fieldset',
            autoHeight:true,
            hidden : true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.InclusiveProfitability"),
                xtype : 'radiogroup',
                vertical: false,
                id:"_proft"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.yes"), 
                    name: 'proft', 
                    inputValue: '1'
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.no"), 
                    name: 'proft', 
                    inputValue: '0', 
                    checked:true
                }
                ]
            }]
        },{
            xtype:'fieldset',
            autoHeight:true,
            hidden : true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SuppressOfAmount"),
                xtype : 'radiogroup',
                vertical: false,
                id:"_suppressamount"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.yes"), 
                    name: 'suppressamount', 
                    inputValue: '1'
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.no"), 
                    name: 'suppressamount', 
                    inputValue: '0', 
                    checked:true
                }
                ]    
            }]
        }
        ];
        
        this.terminfo = new Wtf.form.FormPanel({
            url: 'ACCAccount/saveInvoiceTermsSales.do',
            region:'center',
            bodyStyle:"background: transparent;",
            border:false,
            style: "background: transparent;padding:10px;",
            items:array
        });
        this.createTermBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.field.CreateTerm"),
            scope: this,
            handler:this.saveTermForm.createDelegate(this)
        });
        this.createTemplateWin=new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.DefineTerm"),
            closable: true,
            modal: true,
            iconCls : getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            height: 300,
            autoScroll:true,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body,
            items:[{
                region: 'center',
                border: false,
                bodyStyle: 'background:#f1f1f1;font-size:10px;',
                autoScroll:true,
                items:this.terminfo
            }],
            buttons: [this.createTermBtn, {
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope:this,
                handler:function(){
                this.createTemplateWin.close();
                }
            }]
        });
        this.createTemplateWin.show();
    },
    setTermForTax: function () {
        var rec = this.taxgrid.getSelectionModel().getSelections();
        if(rec=="" || rec==undefined){
          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.SelectTax")],2)
          return;
        }
        //Not allowed user to set Term to deactivated tax.
        for (var count = 0; count < rec.length; count++) {
            if (rec[count].data && !rec[count].data.hasAccess) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.tax.deactivated.setToTerm.alert")], 2);//You cannot set term to deactivated tax(s). Please select activated tax(s).
                return;
            }
        }
        this.termRec = new Wtf.data.Record.create([
            {
                name: 'id'
            },
            {
                name: 'term'
            },
            {
                name: 'istermused'
            },
            {
                name:'hasAccess'
            }
        ]);
        this.termStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.termRec),
            url: 'ACCAccount/getInvoiceTermsSales.do',
            baseParams: {
                isSalesOrPurchase: this.isSales,
                showActiveDeactiveTerms:true     //showActiveDeactiveTerms is set false to get only active terms to map to tax 
            }
        });
        this.termStore.on("load", function(){
             var rec = this.taxgrid.getSelectionModel().getSelections();
            if (rec != "" && rec != undefined && rec.length==1) {
                var term = rec[0].data.termid;
                this.term.setValue(term);
            }
        }, this);
        this.termStore.load();
        this.MSComboconfig = {
            store: this.termStore,
            valueField: 'id',
            displayField: 'term',
            name: 'term',
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectTerm"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
          //  allowBlank: false,
            triggerAction: 'all',
            scope: this,
            anchor: '100%',
            isTerm:true
        };
        this.term = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Term"),
            // id:"poNumberID"+this.heplmodeid+this.id ,
            forceSelection: true,
            editable:false,
            width: 240
        }, this.MSComboconfig));
        this.term.on('beforeselect',function (combo, record, index) {
            return validateSelection(combo, record, index);
                    },this);
        this.term.on('focus',function(){
          this.term.setEditable(false);  
        },this);
        
        this.term.on('select', function(combo, rec, index) {
            if (rec.data.istermused) {
                combo.clearValue();
                this.term.list.hide();
                var msg=WtfGlobal.getLocaleText("coa.type.youcannotselect")+" <b>"+rec.data.term+"</b> term "+WtfGlobal.getLocaleText("acc.term.alreadyused");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            }

        }, this);
        //  var array = [this.term];
        this.termform = new Wtf.form.FormPanel({
            url: 'ACCAccountCMN/saveTermsForTax.do',
            region: 'center',
            bodyStyle: "background: transparent;",
            border: false,
            style: "background: transparent;padding:10px;",
            items: this.term
        });
        this.setTermBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.field.SaveTerm"),
            scope: this,
            handler: this.setTermForm.createDelegate(this)
        });
        this.setTermWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.SelectTerm"),
            closable: true,
            modal: true,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            height: 300,
            autoScroll: true,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body,
            items: [{
                    region: 'center',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    autoScroll: true,
                    items: this.termform
                }],
            buttons: [this.setTermBtn, {
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    scope: this,
                    handler: function () {
                        this.setTermWin.close();
                    }
                }]
        });
        this.setTermWin.show();

    },
    saveTermForm : function() {
        this.createTermBtn.disable();
        this.terminfo.getForm().submit({
            scope: this,
            params:{
            isSalesOrPurchase:this.isSales
        },
            success: function(result,action){
                this.createTermBtn.enable();
                 var resultObj = eval('('+action.response.responseText+')');
                 if(resultObj.data.success) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.field.Termaddedsuccessfully")],0);
                    this.createTemplateWin.close();
                    this.grid.store.load();
                 }else{
                    if(resultObj.data.msg)msg=resultObj.data.msg;
                       WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                 }
            },
            failure: function(frm, action){
                this.createTermBtn.enable();
                var resObj = eval( "(" + action.response.responseText + ")" );
            }
        });
    },
    setTermForm: function () {
        this.setTermBtn.disable();
        var taxId = [];
        var rec = this.taxgrid.getSelectionModel().getSelections();
        if (rec != "" && rec != undefined) {
            for (var i = 0; i < rec.length; i++) {
                taxId[i] = rec[i].data.taxid;
    }

        }
        var a = "[" + taxId.join(',') + "]";
        var b = this.term.getValue();
        var array = b.split(",");
        var b = "[" + array.join(',') + "]";
        this.termform.getForm().submit({
            scope: this,
            params: {
                isSalesOrPurchase: this.isSales,
                taxId: a,
                term: b
            },
            success: function (result, action) {
                this.setTermBtn.enable();
                var resultObj = eval('(' + action.response.responseText + ')');
                if (resultObj.data.success) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.Termaddedsuccessfully")], 0);
                    this.taxgrid.store.reload();
                    this.setTermWin.close();
                    // this.grid.store.load();
                } else {
                    if (resultObj.data.msg)
                        msg = resultObj.data.msg;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                }
            },
            failure: function (frm, action) {
                this.setTermBtn.enable();
                var resObj = eval("(" + action.response.responseText + ")");
            }
});  
    }
});  
