Wtf.account.FixedAssetGroup = function(config){
    this.ExportButtonVersion='_v1';
    this.PrintButtonVersion='_v1';
    Wtf.apply(this, config);
    this.moduleid=config.moduleid;
    Wtf.account.FixedAssetGroup.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.FixedAssetGroup, Wtf.Panel,{  
    onRender: function(config){
        Wtf.account.FixedAssetGroup.superclass.onRender.call(this, config);
        
        //create Grid
            
        this.createFAGroupGrid();
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            autoScroll : true,
            items: [this.objsearchComponent,{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.assetGroupGrid],
                tbar: this.btnArr,
                bbar:   this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.gridStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({
                            id: "pPageSize_" + this.id
                        }),
                        items: [this.btnArrB]
                        
                })
            }]
        }); 
        this.add(this.leadpan);
    },
    
    openingHandler:function(){
        
        var recArray = this.assetGroupGrid.getSelectionModel().getSelections();
        if(this.assetGroupGrid.getSelectionModel().hasSelection()==false||this.assetGroupGrid.getSelectionModel().getCount()>1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        
        var rec = recArray[0];
        
        
        callFixedAssetOpeningWindow(rec);
    },
    
    createNewHandler : function(){
        createFixedAsset();
    },
    
    editHandler : function(){
        var recArray = this.assetGroupGrid.getSelectionModel().getSelections();
        if(this.assetGroupGrid.getSelectionModel().hasSelection()==false||this.assetGroupGrid.getSelectionModel().getCount()>1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        
        var rec = recArray[0];
        
        createFixedAsset(true,rec,rec.id);
    },
    
    deleteHandler:function(){
        
        var recArray = this.assetGroupGrid.getSelectionModel().getSelections();
        if(this.assetGroupGrid.getSelectionModel().hasSelection()==false||this.assetGroupGrid.getSelectionModel().getCount()<1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        
        this.arrRec=[];
        this.arrID=[];
        this.arrRec = this.sm.getSelections();
        for(var i=0;i<this.sm.getCount();i++){
            this.arrID.push(this.arrRec[i].data['productid']);
            
            if(this.gridStore.find('parentuuid', this.arrRec[i].data['productid']) != -1 ){
                var child = this.gridStore.getAt(this.gridStore.find('parentuuid',this.arrRec[i].data['productid']));
                this.arrID.push(child.data['productid']);
	            
                while(this.gridStore.find('parentuuid',child.data['productid']) != -1){
                    child = this.gridStore.getAt(this.gridStore.find('parentuuid',child.data['productid']));
                    this.arrID.push(child.data['productid']);
                }
            }
        } 
        WtfGlobal.highLightRowColor(this.assetGroupGrid,this.arrRec,true,0,2);
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.warning"),
            msg: WtfGlobal.getLocaleText("erp.fixedasset.deleteconfirmmsg"), //Are you sure you want to delete the selected Asset Group
            width: 560,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn!="ok"){
                    var num= (this.gridStore.indexOf(this.arrRec[0]))%2;
                    WtfGlobal.highLightRowColor(this.assetGroupGrid,this.arrRec,false,num,2);
                    return;
                }
                else {
                    this.deletionRequest(false);
                }
            }
        });
        
    },
    fetchStatement: function() {
        this.gridStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
   
    deletionRequest:function(unbuild){
        Wtf.Ajax.requestEx({
            url:"ACCProductCMN/deleteProducts.do",
            params: {
                mode:23,
                ids:this.arrID,
                unBuild:unbuild,
                isFixedAsset:true,
                isPermDel:true
            }
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    
    genSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
            var ind=this.gridStore.indexOf(this.arrRec[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.assetGroupGrid,this.arrRec[i],false,num,2,true);
        }
        if(response.success){
            WtfComMsgBox([WtfGlobal.getLocaleText("erp.fixedasset.fixedassetgroup"),response.msg],response.success*2+1);
            if(response.success){
                (function(){
//                    Wtf.uomStore.reload();
//                    Wtf.gridStore.reload();
//                    Wtf.productStoreSales.reload();
                    this.gridStore.reload();  
                    Wtf.FixedAssetStore.reload();                                //In Order to reload the Fixed asset Group Store ERP-9510
                }).defer(WtfGlobal.gridReloadDelay(),this);
            }
        }
        else{
            if(response.isused){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg]);
            }
//            else{
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.productList.gridProduct"),"No Products are available for syncing."],response.success*2+1);
//            }
        }     
    },
    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
            var ind=this.gridStore.indexOf(this.arrRec[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.assetGroupGrid,this.arrRec[i],false,num,2,true);
        }
        var msg=  WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    updateGrid: function () {
        //this.gridStore.reload();;
        if (this.gridStore.getRange().length > 0) {
            this.gridStore.reload();
        } else {
            this.gridStore.load({
                params: {
                    start: 0,
                    limit: this.pP.combo != undefined ? this.pP.combo.value : 30,
                    pagingFlag: true
                }
            });
        }
    },
    
    syncProducts:function(){
       var arrID=[];

        this.gridStore.filterBy(function(rec){
            if(rec.data.syncable)
                return true;
                else return false
        });
       this.arrRec = this.gridStore.getRange(0,this.gridStore.getCount()-1);
       for(var i=0;i<this.gridStore.getCount();i++)
            if(this.arrRec[i].data.syncable)
                arrID.push(this.arrRec[i].data['productid']);
        this.sm.clearSelections();
        WtfGlobal.highLightRowColor(this.assetGroupGrid,this.arrRec,true,0,2);
       if(this.arrRec.length==0){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.fixed.syncmsg.unavailable")],2);
           this.gridStore.clearFilter();
           return;
       }
       Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.confirm"),
           msg: WtfGlobal.getLocaleText("acc.fixed.syncmsg"),  //Shown Asset Group(s) will be synchronized with other application. Are you sure you want to synchronize the Asset Group(s)?
           width: 560,
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn!="ok"){this.gridStore.clearFilter();
                    var num= (this.gridStore.indexOf(this.arrRec[0]))%2;
                   WtfGlobal.highLightRowColor(this.assetGroupGrid,this.arrRec,false,num,2);
                     return;
                }
                else {
                        var URL="ACCCompanySetup/sendAccProducts.do";
                        WtfGlobal.setAjaxTimeOut();
                        Wtf.Ajax.requestEx({
    //                        url:Wtf.req.account+'CompanyManager.jsp',
                            url:URL,
                            params: {
                                ids:arrID,
                                isFixedAsset:true
                            }
                        },this,this.genSyncSuccessResponse,this.genSyncFailureResponse);
                }
        }});
    },
    
    callSubscriptionWin:function(){
       var m = Wtf.DomainPatt.exec(window.location);
       m="http://apps.deskera.com/"+m[0];
       var subscribePanel = new Wtf.FormPanel({
                    width:'80%',
                    method :'POST',
                    scope: this,
                    border:false,
                    fileUpload : true,
                    waitMsgTarget: true,
                    labelWidth: 70,
                    bodyStyle: 'font-size:10px;padding:10px;',
                    layout: 'form',
                    items:[{
                        border:false,
                        html:"<div style = 'font-size:12px; width:100%;height:100%;position:relative;float:left;'>"
                                +WtfGlobal.getLocaleText("acc.field.DatasyncingoperationbetweenCRMandAccountingcantbeperformed")
                                +WtfGlobal.getLocaleText("acc.field.InordertosubscribetoCRMclick")+"<a target='_blank' class='linkCls' href="+m+"> <b>" + WtfGlobal.getLocaleText("acc.field.Subscribe")+ "</b> </a>"+ WtfGlobal.getLocaleText("acc.field.Subscribe.else.click.cancel")
                                +"</div>"
                    }]
                },
                this);
                var impWin1 = new Wtf.Window({
                    resizable: false,
                    scope: this,
                    layout: 'border',
                    modal:true,
                    width: 380,
                    height: 220,
                    border : false,
                    iconCls: 'pwnd deskeralogoposition',
                    title: WtfGlobal.getLocaleText("acc.field.DataSyncing"),
                    items: [
                            {
                                region:'north',
                                height:70,
                                border : false,
                                bodyStyle : 'background:white;',
                                html: getTopHtml(WtfGlobal.getLocaleText("acc.setupWizard.dear")+ _fullName+",", "",null,true)
                            },{
                                region:'center',
                                layout:'fit',
                                border:false,
                                bodyStyle : 'background:white;',
                                items:[subscribePanel]
                            }
                    ],
                    buttons: [{
                        text:WtfGlobal.getLocaleText("acc.common.cancelBtn"), //'Cancel',
                        id:'canbttn1',
                        scope:this,
                        handler:function() {
                            impWin1.close();
                        }
                    }]
                },this);

                impWin1.show();
            },
    
    genSyncSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
             var ind=this.gridStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.assetGroupGrid,this.arrRec[i],false,num,2,true);
        }
        this.gridStore.clearFilter();
        if(!response.companyexist)
            this.callSubscriptionWin()
        else 
            if(response.success){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"),response.msg],response.success*2+1);

//            (function(){
//            }).defer(WtfGlobal.gridReloadDelay(),this);
            }

    },
    
    genSyncFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
             var ind=this.gridStore.indexOf(this.arrRec[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.assetGroupGrid,this.arrRec[i],false,num,2,true);
        }this.gridStore.clearFilter();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
   },

    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.gridStore.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
        }
    },

    createFAGroupGrid:function(){
        
        this.btnArr = [],this.btnArrEDSingleS=[],this.btnArrEDMultiS=[],this.btnArrB=[];
        
        this.btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.field.QuickSearchAssetGroupIdNameDesc"),  //'Quick Search by Asset Group Id, Name, Description',
            id:"quickSearch"+this.id,
            width: 200
        }));

        this.btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            tooltip :WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset Search Results',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        }));
        
        this.resetBttn.on('click', this.handleResetClick, this);
        
      
   this.btnArr.push({
        xtype: 'button',
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls: 'accountingbase fetch',
        tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
        scope: this,
        handler: this.fetchStatement
    });
        
        this.openingButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.fixed.asset.opening"),
            tooltip :WtfGlobal.getLocaleText("acc.fixed.asset.CreateOpeningRecord"),
            scope:this,
            disabled:true,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            handler:this.openingHandler
        });
        
        var importBtnArr = [];     
        this.moduleName = "Fixed Asset Group";
        var extraConfig = {isExcludeXLS: true};
        extraConfig.url= "ACCProductCMN/importFixedAssetGroups.do";
        var extraParams = "";
        var importAssetGroupsbtnArray = Wtf.importMenuArray(this, this.moduleName, this.productStore, extraParams, extraConfig);

        this.importAssetGroupsBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.importassetgroups"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.importassetgroups"),
            iconCls: (Wtf.isChrome?'pwnd importProductChrome':'pwnd importcsv'),
            menu: importAssetGroupsbtnArray
        });
        importBtnArr.push(this.importAssetGroupsBtn);
        this.btnArrB.push(importBtnArr);
        
        var importOpeningBtnArr = [];     
        this.moduleName = "Opening Fixed Asset Documents";
        var extraConfig = {isExcludeXLS: true};
        extraConfig.url= "ACCProductCMN/importFixedAssetOpeningDocuments.do";
        var extraParams = "";
        var importOpeningAssetbtnArray = Wtf.importMenuArray(this, this.moduleName, this.productStore, extraParams, extraConfig);

        this.importOpeningAssetBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.importopeningfixedassetdocuments"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.field.importopeningfixedassetdocuments"),
            iconCls: (Wtf.isChrome?'pwnd importProductChrome':'pwnd importcsv'),
            menu: importOpeningAssetbtnArray
        });
        if (this.importOpeningAssetBtn && !WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.openingbalance)) {
            importOpeningBtnArr.push(this.importOpeningAssetBtn);
            this.btnArrB.push(importOpeningBtnArr);
        }
        this.exportButton=new Wtf.exportButton({
                obj:this,
                id:this.id+'assetlistexport',
                tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
                params:{
                    name:WtfGlobal.getLocaleText("acc.AssetGroup.tabTitle"),
                    mode:22,
                    isFixedAsset:true
                },
                isAssetGroupExport:true,
                menuItem:{csv:true,pdf:true,rowPdf:false,xls:true},
                get:1110,
                filename: WtfGlobal.getLocaleText("acc.AssetGroup.tabTitle") + this.ExportButtonVersion,
                label:WtfGlobal.getLocaleText("acc.field.AssetGroup"),
                disabled :true
        });
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            params:{
                name:WtfGlobal.getLocaleText("acc.AssetGroup.tabTitle"),
                mode:22,
                isFixedAsset:true
            },
            menuItem:{print:true},
            filename: WtfGlobal.getLocaleText("acc.AssetGroup.tabTitle") + this.PrintButtonVersion,
            get:198,
            label:WtfGlobal.getLocaleText("acc.field.AssetGroup"),
            disabled :true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.createfa)) {
            this.createNewButton = new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.create.title"),
                scope:this,
                tooltip:WtfGlobal.getLocaleText("acc.create.title"),
                iconCls :getButtonIconCls(Wtf.etype.add),
                handler:this.createNewHandler.createDelegate(this)
            });
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.editfa)) {
            this.editButton = new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.edit"),//'Edit',
                scope:this,
                disabled:true,
                iconCls :getButtonIconCls(Wtf.etype.edit), 
                tooltip:WtfGlobal.getLocaleText("acc.common.edit"),
                handler:this.editHandler.createDelegate(this)
            });
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.deletefa)) {
            this.deleteButton = new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),//'Delete',
                scope:this,
                disabled:true,
                iconCls :getButtonIconCls(Wtf.etype.menudelete), 
                tooltip:WtfGlobal.getLocaleText("acc.fixed.asset.delete.selected"),//'Delete to selected record',
                handler:this.deleteHandler.createDelegate(this)
            });
        }
        if(this.isEntry) {//added because not to show tbar in case of entry while show tbar in case of reports
            if(this.createNewButton){
                this.btnArr.push(this.createNewButton);
            }
            if(this.editButton){
                this.btnArr.push(this.editButton);
                this.btnArrEDSingleS.push(this.btnArr.length-1);
            }
        }
      
            if(this.deleteButton){
                this.btnArr.push(this.deleteButton);
                this.btnArrEDMultiS.push(this.btnArr.length-1);
            }
             if(this.isEntry) {
            if(this.openingButton && !WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.openingbalance)){
                this.btnArr.push(this.openingButton);
                this.btnArrEDSingleS.push(this.btnArr.length-1);
            }
        this.btnArr.push(new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.productList.dataSync"),//'Data Sync',
            iconCls:getButtonIconCls(Wtf.etype.sync),
            menu : [{
                    text:WtfGlobal.getLocaleText("acc.field.DataSyncToCRM"),
                    scope:this,
                    tooltip:WtfGlobal.getLocaleText("acc.fixed.syncmsgTT"),  //Data Syncing operation enables you to sync the Asset Group List data from Accounting into CRM. Following fields from Accounting will be populated into CRM:<br>1. Asset Group Name <br>2. Description <br>3. Purchase Price <br>4. Sales Price
                    iconCls:getButtonIconCls(Wtf.etype.sync),                    
                    handler:this.syncProducts.createDelegate(this,[false])
            }]
        }))
        }
          this.btnArrB.push(this.exportButton);
          this.btnArrB.push(this.printButton);
        
        
        this.gridRec = Wtf.data.Record.create ([
            {name:'productid'},
            {name:'productname'},
            {name:'desc'},
            {name:'pid'},
            {name:'vendor'},
            {name:'producttype'},
            {name:'type'},
            {name:'initialsalesprice'},
            {name:'warrantyperiod'},
            {name:'uomid'},
            {name:'uomname'},
            {name:'parentuuid'},
            {name:'parentid'},
            {name:'parentname'},
            {name:'purchaseaccountid'},
            {name:'salesaccountid'},
            {name:'purchaseretaccountid'},
            {name:'salesretaccountid'},
            {name:'shelfLocationId'},
            {name:'reorderquantity'},
            {name:'quantity'},
            {name:'reorderlevel'},
            {name:'leadtime'},
            {name:'purchaseprice'},
            {name:'saleprice'},
            {name: 'leaf'},
            {name: 'warranty'},
            {name: 'syncable'},
            {name: 'qaenable'},
            {name: 'multiuom'},
            {name: 'blockLooseSell'},
            {name: 'level'},
            {name: 'initialquantity',mapping:'initialquantity'},
            {name: 'initialprice'},
            {name: 'ccountinterval'},
            {name: 'depreciationRate'},
            {name: 'depreciationMethod'},
            {name: 'depreciationCostLimit'},
            {name: 'depreciationGL'},
            {name: 'provisionGL'},
            {name: 'assetSaleGL'},
            {name:'depreciationGLAccount'},
            {name:'depreciationProvisionGLAccount'},
            {name: 'leasedQuantity'},
            {name:'lockquantity'},
            {name:'ccounttolerance'},
            {name:'isBatchForProduct'},
            {name:'sequenceformatid'},
            {name:'isSerialForProduct'},
            {name:'isRowForProduct'},
            {name:'isRackForProduct'},
            {name:'isBinForProduct'},
            {name:'isLocationForProduct'},
            {name:'isWarehouseForProduct'},
            {name:'customfield'},
            {name:'sellAssetGLAccount'},
            {name:'writeoffassetaccount'},
            {name:'landingcostcategoryid'},
            {name:'itctype'}
            
        ]);
        
        
        this.msgLmt = 30;
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        }, this.gridRec);
        
        this.gridStore = new Wtf.data.Store({
            url:"ACCProduct/getProducts.do",
            baseParams:{
                mode:22,
                isFixedAsset:true
            },
            reader: this.gridStoreReader
        });
        
        this.loadMask = new Wtf.LoadMask(document.body,{
                    msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        
        this.gridStore.on('loadexception',function(){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);
        
        this.gridStore.on('beforeload',function(s,o){
            if(!o.params)o.params={};
            o.params.ss = this.quickPanelSearch.getValue();
            this.loadMask.show();
        },this);
        
        this.gridStore.on('load',function(store){
            this.loadMask.hide();
            this.printButton.enable();
            this.exportButton.enable();
            this.quickPanelSearch.StorageChanged(store);
        },this);
        
        var colModelArray = GlobalColumnModelForReports[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.gridStore);  
//        this.gridStore.load({
//            params: {
//                start: 0,
//                limit: 30
//            }
//        });
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({
//            singleSelect:true
        });
        this.gridColumnModelArr=[];
        
        this.gridColumnModelArr.push(this.sm,{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.group"), //'Asset Group'
            dataIndex:'productname',
            pdfwidth:75
        },{
            header: WtfGlobal.getLocaleText("erp.fixedasset.assetgroupid"), //'Asset Group Id'
            dataIndex:'pid',
            align:'left',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridDescription"),//"Description",
            dataIndex:'desc',
            renderer : function(val) {
//                val = val.replace(/(<([^>]+)>)/ig,"");
                return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.productList.gridDescription")+"'>"+val+"</div>";
            },
            pdfwidth:75
        },{
            hidden:true,
            dataIndex:'productid'
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.type.group"), //"Asset Group Type"
            dataIndex:'type',
            hidden:true,
            pdfwidth:75
        },{
            header: WtfGlobal.getLocaleText("erp.fixedasset.depreciationrate"), //Depreciation Rate
            dataIndex:'depreciationRate',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridAvailableQty"),//"Available Quantity",
            dataIndex:"quantity",
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLeasedQty"),//"Available Quantity",
            dataIndex:"leasedQuantity",
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
         },{
            header:WtfGlobal.getLocaleText("acc.productList.gridLockQuantity"),//"Lock Quantity
            dataIndex:'lockquantity',
            align:'right',
            renderer:this.unitRenderer,
            pdfwidth:75
         });
     this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[this.moduleid],true);  
        //
        this.colModel = new Wtf.grid.ColumnModel(this.gridColumnModelArr);
        this.assetGroupGrid = new Wtf.grid.GridPanel({
            cm:this.colModel,
            store:this.gridStore,
            sm:this.sm,
            stripeRows :true,
            border:false,
            layout:'fit',
            forceFit:true,
            viewConfig:{
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")+"<br>"+WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))
//                forceFit:true
            }
        });
        
        this.assetGroupGrid.on("render",function(){
            this.assetGroupGrid.getView().applyEmptyText(); 
        },this);
        
        this.grid =this.assetGroupGrid;
        
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        this.btnArr.push(this.AdvanceSearchBtn);
        
//        var moduleidarr = Wtf.Acc_FixedAssets_AssetsGroups_ModuleId+','+Wtf.Acc_FixedAssets_Details_ModuleId;
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
            moduleid: this.moduleid,
//            moduleidarray: moduleidarr.split(','),
            advSearch: false,
            ignoreDefaultFields: true,
            globallevelfields: true,
            reportid: Wtf.autoNum.AssetGroupReport
        });
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        
        this.sm.on("selectionchange", function () {
            WtfGlobal.enableDisableBtnArr(this.btnArr, this.assetGroupGrid, this.btnArrEDSingleS, this.btnArrEDMultiS);
            var arr=this.assetGroupGrid.getSelectionModel().getSelections();
            if(arr.length==1){
                this.recid=this.assetGroupGrid.getSelectionModel().getSelections()[0].data.productid;
            }

        },this);
        
    },
    
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.gridStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: filterConjuctionCriteria,
            isFixedAsset:true
        }
        this.gridStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.gridStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            isFixedAsset:true
        }
        this.gridStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    }
});
