Wtf.account.FADetails = function(config){
    /*
     * this.fromPO Flag comes true in case of linking, otherwise if Lease Sales order is being create [IN Normal Transactions While creating PO,SO,CQ,VQ this flag contains true value else in case of linking]
     */
    this.isDepartmentStoreloaded = false;
    this.isLocationStoreloaded = false;
    this.isAssetComboStoreloaded = false;
    this.isAssetComboExist = false;
    this.mainProductID=config.assetRec.data.productid;
    this.isBatchForProduct=config.assetRec.data.isBatchForProduct;
    this.isSerialForProduct=config.assetRec.data.isSerialForProduct;
    this.isLocationForProduct=config.assetRec.data.isLocationForProduct;
    this.isWarehouseForProduct=config.assetRec.data.isWarehouseForProduct;
    this.isRowForProduct=config.assetRec.data.isRowForProduct;
    this.isRackForProduct=config.assetRec.data.isRackForProduct;
    this.isBinForProduct=config.assetRec.data.isBinForProduct;
    this.isCustomer = (config.isCustomer)?config.isCustomer:false;
    this.isFixedAsset=(config.isFixedAsset)?config.isFixedAsset:false;
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset)?config.isLeaseFixedAsset:false;
    this.productComboStore=(this.isFixedAsset || this.isLeaseFixedAsset)?Wtf.FixedAssetStore:(this.isCustomer?Wtf.productStoreSales:Wtf.productStore);
    /*
     * this.isLinkedFromReplacementNumber will come true if lease sales order is being created by linking with Replacement number
     */
    this.isLinkedFromReplacementNumber = (config.isLinkedFromReplacementNumber)?config.isLinkedFromReplacementNumber:false;
    this.isLinkedFromCustomerQuotation = (config.isLinkedFromCustomerQuotation)?config.isLinkedFromCustomerQuotation:false;
    this.islinkedFromLeaseSo = (config.islinkedFromLeaseSo)?config.islinkedFromLeaseSo:false;
    this.isFromSalesOrder = (config.isFromSalesOrder)?config.isFromSalesOrder:false;
    this.fromPO = (config.fromPO)?config.fromPO:false;
    this.isEdit = (config.isEdit)?config.isEdit:false;
    this.isPILinkedInGR = (config.isPILinkedInGR)?config.isPILinkedInGR:false;
    this.isGRLinkedInPI = (config.isGRLinkedInPI) ? config.isGRLinkedInPI : false;
    this.isFromOrder = (config.isFromOrder)?config.isFromOrder:false;// From GR OR DO
    this.isFromSalesReturn = (config.isFromSalesReturn)?config.isFromSalesReturn:false;// From SR
    this.moduleid = config.moduleid;
//    this.isGRCreatedByLinkingWithPI = (config.isGRCreatedByLinkingWithPI)?config.isGRCreatedByLinkingWithPI:false;
//    
//    if(this.isGRCreatedByLinkingWithPI){// In case GR is Created by Linking wth Purchase Invoice, then only in edit case of this GR this Flag Will be True From File FixedAssetDeliveryOrder.js
//        this.fromPO=true;
//    }
    this.readOnly=config.readOnly;
    this.billDate=config.billDate;
    this.isbilldateChanged=config.isbilldateChanged != undefined ? config.isbilldateChanged : false;
    this.parentObj = config.parentObj;
    this.isInvoice = (config.isInvoice)?config.isInvoice:false;
    this.isFromOpeningForm = (config.isFromOpeningForm)?config.isFromOpeningForm:false;
    this.isQuotationFromPR = (config.isQuotationFromPR)? config.isQuotationFromPR : false;
    this.isPIFromPO = (config.isPIFromPO)? config.isPIFromPO : false;
    this.isPIFromVQ = (config.isPIFromVQ)? config.isPIFromVQ : false;
    this.isPOfromVQ = (config.isPOfromVQ)? config.isPOfromVQ : false;
    this.isFixedAssetPR = (config.isFixedAssetPR)? config.isFixedAssetPR : false;
    this.isFixedAssetSR = (config.isFixedAssetSR)? config.isFixedAssetSR : false;
    this.module=Wtf.Acc_FixedAssets_Details_ModuleId ;
    this.isFixedAssetGR=config.isFixedAssetGR;
    this.tagsFieldset = config.tagsFieldset != undefined ? config.tagsFieldset : undefined;
    this.globalColumnModelForReports=undefined;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.purDate=config.purDate;
    
      var btnArr=[];
      this.saveButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
        minWidth: 50,
        scope: this,
        hidden: this.readOnly,
        handler: this.saveData.createDelegate(this)
     });
     
     this.closeButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
        minWidth: 50,
        scope: this,
        handler: this.closeFADetails.createDelegate(this)
    });
    
    if (this.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId) {
            var extraConfig = {};
            extraConfig.url = "ACCProductCMN/importFixedAssetOpeningDocuments.do";
            extraConfig.isAssetsImportFromDoc = true;// This flag is used in JAVA side as well as JS side for fetching system column.
            var extraParams = "";
            this.moduleName = "Asset GoodsReceiptOrder"; //Need to reuse same system column's from default_header table.
            var importAssetDetailBtnArray = Wtf.importMenuArray(this, this.moduleName, config.parentGrid.getStore(), extraParams, extraConfig);
            this.importAssetDetailBtn = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.asset.import.assetdetail"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.asset.import.assetdetail"),
                iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import'),
                menu: importAssetDetailBtnArray,
                typeXLSFile: true
            });
            
         btnArr.push(this.importAssetDetailBtn);
       }
       btnArr.push(this.saveButton);
       btnArr.push(this.closeButton);
    
       Wtf.apply(this, {
            buttons: btnArr
        }, config);
    
    Wtf.account.FADetails.superclass.constructor.call(this, config);
    
}

Wtf.extend(Wtf.account.FADetails, Wtf.Window,{
    onRender:function(config){
        Wtf.account.FADetails.superclass.onRender.call(this,config);
        
        // create north template
        
        this.northPanel = this.createNorthTemplate();
        
        if(this.lineRec){
            this.selectedCurrencySymbol = this.lineRec.data['currencysymbol'];
        }
                
        // create Fixed Asset Details Grid
        
        this.createFixedAssetDetails();
        
        
        // add record at here only if asset combo store is not loading else it will be add on is load event
        if(!(this.isCustomer || ((this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder || this.isFixedAssetGR))))){
            this.addGridRec();
            this.setCustomFieldValues(this.FADetailsGrid,false);
        }
      
        this.add({
            region: 'north',
            height:100,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            items:[this.northPanel]
        }, this.centerPanel=new Wtf.Panel({
                border: false,
                region: 'center',
                id: 'centerpan'+this.id,
                autoScroll:true,
//                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'fit',
                items:[this.FADetailsGridContainerPanel]
            })
        );
        
    },
    
    createNorthTemplate:function(){
        
        var productCode = this.assetRec.get('pid');
        var description = this.assetRec.get('desc');
        var quantity = this.quantity;
        this.productId = this.assetRec.get('productid');
        var linkTemplate = "";
        
        if(this.isFromOpeningForm){
            
            var amount = this.rate;
            /* // ERP-16629: WDV field should be optional during asset creation
            var wdv = this.wdv; */
            
            linkTemplate = new Wtf.XTemplate(
                "<div> &nbsp;</div>",
                '<tpl>',
                "<div style='padding-left:30px;'><b>"+WtfGlobal.getLocaleText("erp.fixedasset.assetgroupid")+": "+productCode+"</b></div>"
                +"<div style='padding-left:30px;'><b>"+WtfGlobal.getLocaleText("acc.gridproduct.discription")+"&nbsp;&nbsp;&nbsp;&nbsp;: "+description+"</b></div>"
//                +"<div style='padding-left:30px;'><b>WDV &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: "+wdv+"</b></div>"
                +"<div style='padding-left:30px;'><b>"+WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Amount")+" &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: "+amount+"</b></div>"
                +"<div style='padding-left:30px;'><b>"+WtfGlobal.getLocaleText("acc.taskProgressGrid.materialConsumed.header5")+"    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: "+quantity+"</b></div>",
                '</tpl>'
                );
        }else{
            linkTemplate = new Wtf.XTemplate(
                "<div> &nbsp;</div>",
                '<tpl>',
                "<div style='padding-left:30px;'><b>"+WtfGlobal.getLocaleText("erp.fixedasset.assetgroupid")+" : "+productCode+"</b></div>"
                +"<div style='padding-left:30px;'><b>"+WtfGlobal.getLocaleText("acc.gridproduct.discription") +"&nbsp;&nbsp;&nbsp;&nbsp;: "+description+"</b></div>"
                +"<div style='padding-left:30px;'><b>"+WtfGlobal.getLocaleText("acc.taskProgressGrid.materialConsumed.header5")+"    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;: "+quantity+"</b></div>",
                '</tpl>'
                );
        }  
                    
        var tplPanel = new Wtf.Panel({
            border:false,
            html:linkTemplate.apply()
        }) 
        
        return tplPanel;
    },
        
    createFixedAssetDetails:function(){
        
        this.users= new Wtf.form.ComboBox({     
            triggerAction:'all',
            mode: 'local',
            selectOnFocus:true,
            valueField:'userid',
            displayField:'fname',
            store:Wtf.userds,
            typeAhead: true,
            forceSelection: true,
            name:'username',
            hiddenName:'username'     
        });
        
        // create location combo
        
        this.locationEditor = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
//            fieldLabel:"Location",
            valueField:'id',
            displayField:'name',
            store:Wtf.locationStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'location',
            hiddenName:'location'

        });
        
        // create department editor combo
        
//        this.detartmentRec = new Wtf.data.Record.create([
//            {name:"id"},
//            {name:"name"}
//        ]);
//        this.detartmentReader = new Wtf.data.KwlJsonReader({
//            root:"data"
//        },this.detartmentRec);
//
//        this.detartmentStore = new Wtf.data.Store({
//             url:"ACCMaster/getDepartments.do",
//            reader:this.detartmentReader
//        });
        
//        this.detartmentStore.on('load',function(){
//            if(!this.isCustomer && !((this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder)))){// should be vendor and should not be GRO Linked with VI
//                this.addGridRec();
//            }
//        },this);
        
//        this.detartmentStore.load();
        
        
        this.detartmentEditor = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
//            fieldLabel:"Location",
            valueField:'id',
            displayField:'name',
            store:Wtf.detartmentStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'detartment',
            hiddenName:'detartment'

        });
        this.machineRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.machineStore = new Wtf.data.Store({
            url: "ACCMachineMaster/getMachinesForCombo.do",
            baseParams: {
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.machineRec)
        });
        this.machineStore.load();
        this.machineEditor = new Wtf.form.ComboBox({
            triggerAction: 'all',
            mode: 'local',
            valueField: 'id',
            displayField: 'name',
            store: this.machineStore,
            anchor: '90%',
            typeAhead: true,
            forceSelection: true,
            name: 'machine',
            hiddenName: 'machine',
            selectOnFocus:true

        });
        this.rowno=new Wtf.grid.RowNumberer();
        
        if(this.isCustomer || ((this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder || this.isFixedAssetGR)))){// For CI/DO AND GR CREATION FROM LINKING
            this.assetComboRec = new Wtf.data.Record.create([
                {name: 'assetdetailId'},
                {name: 'assetGroup'},
                {name: 'location'},
                {name: 'department'},
                {name: 'machine'},
                {name: 'assetUser'},
                {name: 'cost'},
                {name: 'costInForeignCurrency'},
                {name: 'salvageValueInForeignCurrency'},
                {name: 'salvageValue'},
                /* // ERP-16629: WDV field should be optional during asset creation    
                {name: 'wdv'}, */
                {name: 'elapsedLife'},
                {name: 'nominalValue'},
                {name: 'assetDepreciationMethod'},
                {name: 'assetGroupId'},
                {name: 'assetId'},
                {name: 'assetName'},
                {name: 'assetdescription'},
                {name: 'sellAmount'},
                {name: 'purchaseDate', type:'date'},
                {name: 'installationDate',type:'date'},
                {name: 'salvageRate'},
                {name: 'accumulatedDepreciation'},
                {name: 'assetLife'},
                {name:'batchdetails'},
                {name:'isDepreciationPosted', type: 'boolean', defaultValue: false},
                {name: 'customfield'}
            ]);
            
            this.productId = this.assetRec.get('productid');

            this.assetComboStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    totalProperty: 'totalCount',
                    root: "data"
                },this.assetComboRec),
                url : "ACCProduct/getAssetDetailsForCombo.do",
                baseParams:{
                    excludeSoldAsset:true,
                    isLeaseFixedAsset:this.isLeaseFixedAsset,
                    productId:this.productId,
                    fromInvoice:this.isInvoice,
                    linkrowid:this.lineRec.get('rowid'),
                    billid:this.lineRec.get('billid'),
                    isFromSalesOrder:this.isFromSalesOrder,
                    isEdit:this.isEdit,
                    isCustomer:this.isCustomer,
                    moduleid:this.moduleid
                }
            });
            
            this.assetComboStore.on('beforeload',function(){
                if((this.fromPO || this.isEdit) && !(this.isLinkedFromReplacementNumber || this.isLinkedFromCustomerQuotation)){
                    //if(this.fromPO && this.isEdit){
                    if(this.fromPO){
                        this.assetComboStore.baseParams.invdetailId = this.lineRec.get('originalTransactionRowid');
                        this.assetComboStore.baseParams.parentInvdetailId = this.lineRec.get('savedrowid');
                    }else{
                        if(this.isCustomer){
                            this.assetComboStore.baseParams.invdetailId = this.lineRec.get('originalTransactionRowid');
                        }else{
                            this.assetComboStore.baseParams.invdetailId = this.lineRec.get('rowid');
                        }
                    }
                    
                    if(this.isLeaseFixedAsset && this.isFromOrder && !this.isFromSalesReturn){// if lease & from lease do
                        if(this.fromPO && !this.isEdit){// in case of partial linking of lease so to lease DO,  lease DO Should have only assets which are not linked to any other Lease DO
                            this.assetComboStore.baseParams.isLeasedDoCreated = false;
                        }
                    }
                    
                    if(this.isFromSalesReturn){
                        if(this.fromPO && !this.isEdit){// in case of partial linking of lease DO to lease SR,  lease SR Should have only assets which are not linked to any other Lease SR
                            this.assetComboStore.baseParams.isLeasedSRCreated = true;
                        }
                    }
                    
                    if(this.isLeaseFixedAsset && this.isInvoice){// if lease & from lease CI
                        if(this.fromPO && !this.isEdit){// in case of partial linking of lease DO to lease CI,  lease CI Should have only assets which are not linked to any other Lease CI
                            this.assetComboStore.baseParams.isLeasedCICreated = false;
                        }
                    }
                    
                    this.assetComboStore.baseParams.excludeSoldAsset = false;
                    if(this.isCustomer){
                        if(this.isEdit){
                            this.assetComboStore.baseParams.usedFlag = false;
                        }else{
                            this.assetComboStore.baseParams.usedFlag = true;
                        }
                    }
                    
                    if(!this.isEdit && this.moduleid==Wtf.Acc_FixedAssets_Sales_Return_ModuleId){   //refer ticket ERP-18633
                        this.assetComboStore.baseParams.usedFlag = false;
                        this.assetComboStore.baseParams.isSalesReturn = this.moduleid==Wtf.Acc_FixedAssets_Sales_Return_ModuleId ? true : false;
                    }
                    
                    if((this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder || this.isPILinkedInGR))){// if creating GR by Linking VI OR creating VI by linking GR
                        if(this.isFromOrder || this.isPILinkedInGR){ // if creating GR by Linking VI
                            if(this.isEdit)
                                this.assetComboStore.baseParams.invrecord = true;
                            else
                            this.assetComboStore.baseParams.invrecord = false;
                        }else{ // creating VI by linking GR
                            if(this.isEdit)
                                this.assetComboStore.baseParams.invrecord = false;
                            else
                                this.assetComboStore.baseParams.invrecord = true;
                        }
                        if(!this.isEdit)
                            this.assetComboStore.baseParams.isForGRO = true;
                    }
                    if((this.isEdit && (this.lineRec.get('savedrowid')==undefined ||this.lineRec.get('savedrowid')==""))){
                        this.assetComboStore.baseParams.isForGRO = true;
                        this.assetComboStore.baseParams.invrecord = true;
                        
                    }
                    if (this.isQuotationFromPR) { // if creating VQ by Linking PR
                        this.assetComboStore.baseParams.isQuotationFromPR = true;
                    }
                    
                    if (this.isPOfromVQ) { // if creating PO by Linking VQ
                        this.assetComboStore.baseParams.isPOfromVQ = true;
                    }
                    
                    if (this.isPIFromVQ && (this.lineRec.get('savedrowid')==undefined ||this.lineRec.get('savedrowid')=="")) { // if creating PI by Linking VQ
                        this.assetComboStore.baseParams.isPIFromVQ = true;
                    }
                    
                    if (this.isPIFromPO && (this.lineRec.get('savedrowid')==undefined ||this.lineRec.get('savedrowid')=="")) { // if creating PI by Linking PO
                        this.assetComboStore.baseParams.isPIFromPO = true;
                    }
                    
                    if ((this.isPIFromPO && (this.lineRec.get('savedrowid')!=undefined || this.lineRec.get('savedrowid')!="" || this.isEdit))) { // if creating PI by Linking PO
                        this.assetComboStore.baseParams.isPIFromPO = true;
                    }
                    
                    if (this.isFixedAssetPR) { // if creating Fixed Asset Purachase Return
                        this.assetComboStore.baseParams.isFixedAssetPR = true;
                    }
                    
                    if (this.isFixedAssetSR) { // if creating Fixed Asset Sales Return
                        this.assetComboStore.baseParams.isFixedAssetSR = true;
                    }
                }
            },this);
            
            this.assetComboStore.on('load',function(){
                if(this.isCustomer || ((this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder || this.isFixedAssetGR)))){
                    this.addGridRec();
                    this.setCustomFieldValues(this.FADetailsGrid,true);
                }
            },this);

            
            this.assetComboStore.load();
            if (this.productOptimizedFlag !== Wtf.Products_on_Submit) {
                this.productComboStore.load();
            }
            this.assetEditorCombo = new Wtf.form.ComboBox({     
                triggerAction:'all',
                mode: 'local',
                selectOnFocus:true,
                valueField:'assetdetailId',
                displayField:'assetId',
                store:this.assetComboStore,     
                typeAhead: true,
                forceSelection: true,
                name:'assetCombo',
                hiddenName:'assetCombo'     
            });
        }
        
        this.assetIdEditor = (this.isCustomer)?this.assetEditorCombo:new Wtf.form.TextField({
            validator:Wtf.ValidateAssetId
        });
        
        if((this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder || this.isFixedAssetGR))){
            this.assetIdEditor = this.assetEditorCombo;
        }
        
        var opening_date = WtfGlobal.getOpeningDocumentDate(true);
        
        this.purchaseDate = new Wtf.form.DateField({
            maxLength:250,
            format:WtfGlobal.getOnlyDateFormat(),
            xtype:'datefield'
        });            
        
        this.installationDate = new Wtf.form.DateField({
            maxLength:250,
            format:WtfGlobal.getOnlyDateFormat(),
            xtype:'datefield'
        });
        
        if(this.isFromOpeningForm){
         this.installationDate.maxValue = opening_date;   
         this.purchaseDate.maxValue = opening_date;
        }
        
        var FixedAssetDetailArr = [];
        FixedAssetDetailArr.push(this.rowno,{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.id"),
            dataIndex:'assetId',
            width:(this.isLeaseFixedAsset)?250:150,
            renderer:(this.isCustomer || (this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder || this.isFixedAssetGR)))?Wtf.comboBoxRenderer(this.assetEditorCombo):'',
            editor:(this.readOnly)?"": this.assetIdEditor
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.name"),
            dataIndex:'assetName',
            width:150,
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.field.description"),//"Description",
            dataIndex:"assetdescription",
            width:200,
            maxLength:1024,
            editor:(this.readOnly)?"":new Wtf.form.TextField({name:'assetdescription'}),
            renderer:function(val){
                return "<div wtf:qtip=\""+val+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.field.description")+"'>"+val+"</div>";
            }
        },{
            header:WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"),
            dataIndex:'location',
            width:(this.isLeaseFixedAsset)?150:100,
            align:'right',
            hidden:true,
            renderer:Wtf.comboBoxRenderer(this.locationEditor),
            editor:(this.readOnly)?"":this.locationEditor
        },{
            header:WtfGlobal.getLocaleText("acc.field.department"),
            dataIndex:'department',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset || !this.isShowDimension(),
            renderer:Wtf.comboBoxRenderer(this.detartmentEditor),
            editor:(this.readOnly)?"":this.detartmentEditor
        },{
            header:WtfGlobal.getLocaleText("acc.common.mrp.name"),
            dataIndex:'machine',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset || !Wtf.account.companyAccountPref.activateMRPManagementFlag, //SDP-7326-Nothing shows up in machine dropdown for chkl
            renderer:Wtf.comboBoxRenderer(this.machineEditor),
            editor:(this.readOnly)?"":this.machineEditor
        },{
            header:WtfGlobal.getLocaleText("acc.field.PersonUsingtheAsset"),
            dataIndex:'assetUser',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset,
            renderer:Wtf.comboBoxRenderer(this.users),
            editor:(this.readOnly)?"":this.users
        },{
            header:(this.selectedCurrencySymbol)?WtfGlobal.getLocaleText("acc.product.gridCost")+" "+' ('+this.selectedCurrencySymbol+')':WtfGlobal.getLocaleText("acc.product.gridCost"),
            dataIndex:'costInForeignCurrency',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset || this.isFromOrder || this.isCustomer,
            renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
            }) 
        },{
            header:WtfGlobal.getLocaleText("acc.field.CostInBaseCurrency"),
            dataIndex:'cost',
            hidden:this.isFromOrder || this.isFromOpeningForm || (this.isLeaseFixedAsset && this.isInvoice) || this.isCustomer,
            width:100,
            align:'right',
            renderer:WtfGlobal.globalCurrencySymbolforCredit
        },{
            header:WtfGlobal.getLocaleText("acc.field.SalvageRate"),
            dataIndex:'salvageRate',
            width:100,
            hidden:this.isFromOrder || this.isCustomer,
            align:'right',
//            hidden:this.isLeaseFixedAsset || this.isFromOrder,
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
                maxValue:100
            }) 
        },{
            header: WtfGlobal.getLocaleText("acc.fixed.asset.rate.salvagValue"),
            dataIndex:'salvageValueInForeignCurrency',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset || this.isFromOrder || this.isCustomer,
            renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
            }) 
        },{
            header:WtfGlobal.getLocaleText("acc.filed.SalvageValueInBaseCurrency"),
            dataIndex:'salvageValue',
            hidden:this.isFromOrder || this.isFromOpeningForm || this.isCustomer,
            width:100,
            align:'right',
            renderer:WtfGlobal.globalCurrencySymbolforCredit
        },{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.accDep"),
            dataIndex:'accumulatedDepreciation',
            width:100,
            hidden:true,
            align:'right',
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
            }) 
        },
        /* // ERP-16629: WDV field should be optional during asset creation
        {
            header:'WDV',
            dataIndex:'wdv',
            hidden:this.isInvoice || this.isFromOrder || this.isLeaseFixedAsset,
            width:100,
            align:'right',
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:2
            }) 
        }, */{
            header:WtfGlobal.getLocaleText("acc.asset.AssetLife(Years)"),
            dataIndex:'assetLife',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset || this.isFromOrder,
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
                minValue:0.01                    //Asset life should be greater than 0 so added this config.while dispose or posting depreciation-ERP-35462
            }) 
        },{
            header:WtfGlobal.getLocaleText("acc.asset.ElapsedLife(Years)"),
            dataIndex:'elapsedLife',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset,
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL
            }) 
        },{
            header:WtfGlobal.getLocaleText("acc.asset.NominalValue"),
            dataIndex:'nominalValue',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset,
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
                maxValue:100
            }) 
        },{
            header:this.isLeaseFixedAsset?((this.selectedCurrencySymbol)?WtfGlobal.getLocaleText("acc.fixed.asset.lease.amt")+" "+'('+this.selectedCurrencySymbol+')':WtfGlobal.getLocaleText("acc.fixed.asset.lease.amt")):((this.selectedCurrencySymbol)?WtfGlobal.getLocaleText("acc.fixed.asset.sale.amt")+" "+ '('+this.selectedCurrencySymbol+')':WtfGlobal.getLocaleText("acc.fixed.asset.sale.amt")),
            dataIndex:'sellAmount',
            hidden:!(this.isCustomer && (this.isInvoice || this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId)),
            width:(this.isLeaseFixedAsset)?250:100,
            align:'right',
            renderer:this.unitPriceRendererWithPermissionCheck.createDelegate(this),
            editor:(this.readOnly)?"":new Wtf.form.NumberField({
                allowNegative:false,
                decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,
                value:0
            }) 
        },{
            header: WtfGlobal.getLocaleText("acc.field.PurchaseDate"),
            dataIndex:'purchaseDate',
            width:(this.isLeaseFixedAsset || this.isFromOrder)?250:100,
            hidden:this.isFromOrder,
            align:'right',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            editor:(this.readOnly)?"":this.purchaseDate
        },{
            header:(Wtf.account.companyAccountPref.depreciationCalculationType == 0)?"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.ttp.assetInstallDateIsFisrtFinancialDate")+"'>"+WtfGlobal.getLocaleText("acc.machine.dateofinstallation")+"</span>":WtfGlobal.getLocaleText("acc.machine.dateofinstallation"),
            dataIndex:'installationDate',
            width:(this.isLeaseFixedAsset || this.isFromOrder)?250:100,
            hidden:this.isFromOrder,
            align:'right',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            disabledClass:"newtripcmbss",
            disabled: Wtf.account.companyAccountPref.depreciationCalculationType == 0,
            editor:(this.readOnly)?"":this.installationDate
        }, {
             header: '',
             align:'center',
             dataIndex:'serial',
             renderer: this.serialRenderer.createDelegate(this),
             hidden:((!Wtf.account.companyAccountPref.isBatchCompulsory && !Wtf.account.companyAccountPref.isSerialCompulsory && !Wtf.account.companyAccountPref.isLocationCompulsory && !Wtf.account.companyAccountPref.isWarehouseCompulsory) || ( this.isInvoice && !this.isFromSalesOrder)),
//             hidden:!(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory) || this.isInvoice,
             width:40
        });
        if (this.isShowDimension()) {
            
            var excludeLineItems = true;
            this.globalColumnModelForReports=GlobalColumnModelForReports;
            FixedAssetDetailArr = WtfGlobal.appendCustomColumn(FixedAssetDetailArr,this.globalColumnModelForReports[this.module],undefined,undefined,undefined,undefined,undefined,excludeLineItems);
            FixedAssetDetailArr = WtfGlobal.appendCustomColumn(FixedAssetDetailArr, GlobalColumnModel[this.module],undefined,undefined,undefined,undefined,this.module);
            var CustomtotalStoreCount = 0;
            var CustomloadedStoreCount = 0;

            for (var j = 0; j < FixedAssetDetailArr.length; j++) {
                if (FixedAssetDetailArr[j].dataIndex.indexOf('Custom_') != -1 && (FixedAssetDetailArr[j].fieldtype === 4 || FixedAssetDetailArr[j].fieldtype === 7)) {
                    CustomtotalStoreCount++;
                    FixedAssetDetailArr[j].editor.store.on('load', function() {
                        CustomloadedStoreCount++;
                        if (CustomtotalStoreCount === CustomloadedStoreCount && this.FADetailsGrid != undefined) {
                              this.setCustomFieldValues(this.FADetailsGrid,true);
                        }
                    }, this)
                }
            }
        }
        FixedAssetDetailArr.push({
            header:WtfGlobal.getLocaleText("mrp.workorder.report.delete"),
            dataIndex:"delete",
            align:'center',
            width:50,
            hidden:this.readOnly,
            renderer:function(){
                return "<div class='pwnd delete-gridrow' > </div>";
            }});
        this.FACM = new Wtf.grid.ColumnModel(FixedAssetDetailArr);
        
        this.FixedAssetDetailRec = new Wtf.data.Record.create([
            {
                name: 'assetId'
            },{
              name: 'assetdetailId'
            },{
                name: 'assetName'
            },{
                name: 'assetdescription'
            },{
                name: 'location'
            },{
                name: 'department'
            },{
                name: 'machine'
            },{
                name: 'assetUser'
            },{
                name: 'cost'
            },{
                name: 'costInForeignCurrency'
            },{
                name:'salvageRate'
            },{
                name:'salvageValue'
            },{
                name: 'salvageValueInForeignCurrency'
            },{
                name: 'accumulatedDepreciation'
            }, /* // ERP-16629: WDV field should be optional during asset creation
            {
                name: 'wdv'
            }, */
            {
                name: 'assetLife'
            },{
                name: 'elapsedLife'
            },{
                name: 'nominalValue'
            },{
                name: 'sellAmount',defValue:0
            },{
                name: 'installationDate', type:'date'
            },{
                name: 'purchaseDate', type: 'date'
            },{
                name:'serial'                
            },{
                name:'batchdetails'
            },{
                name: 'isDepreciationPosted', type: 'boolean', defaultValue: false
            },{
                name: 'customfield'
            },
            {name: 'currencysymbol',defValue:this.selectedCurrencySymbol},
            {name: 'currencyrate',defValue:1}
        ]);
        
        this.store = new Wtf.data.Store({
            url:'',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.FixedAssetDetailRec)
        });
        if (this.isShowDimension()) {
            var colModelArray = [];
            var colModelArrayReports = GlobalColumnModelForReports[this.module];
            WtfGlobal.updateStoreConfig(colModelArrayReports, this.store);
            colModelArray = GlobalColumnModel[this.module];
            if (colModelArray) {
                colModelArray.concat(GlobalColumnModelForProduct[this.module]);
            }
            WtfGlobal.updateStoreConfig(colModelArray, this.store);
        }
        
        this.FADetailsGrid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            layout:'fit',
            clicksToEdit:1,
            readOnly:this.readOnly,
            autoScroll:true,
//            height:130,
//            autoHeight:true,
           autoWidth:true,
//            bodyStyle:'margin-top:15px',
            store: this.store,
            cm: this.FACM,
            border : false,
            loadMask : true,
            viewConfig: {
//                forceFit:true
            }
        });
           
        this.FADetailsGrid.on('rowclick',this.processFADetailsGridRow,this);
        this.FADetailsGrid.on('afteredit',this.updateRow,this);
        this.FADetailsGrid.on('validateedit',this.validateRow,this);
        this.FADetailsGrid.on('beforeedit',this.beforeEditHandler,this);
        
        
    this.FADetailsGridContainerPanel = new Wtf.Panel({
        layout: 'fit',
        width : 1000,
        autoScroll:true,
        border: false,
        items:[this.FADetailsGrid]
    });
    },
    
    beforeEditHandler:function(obj){
        if(this.isCustomer){
            if(obj.field != 'assetId' && obj.field != 'sellAmount'){
                obj.cancel = true;
            }
    }else if(!this.isCustomer && this.fromPO && this.isFromOrder){
        if(obj.field != 'assetId'){
            obj.cancel = true;
            return;
        }
    }
    if (obj.field.search('Custom_') >= 0) {
        if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId) {
            return false;
        }
    }
    },
    
validateRow:function(obj){
    if(obj!=null){
        if(obj.field=='assetId'){
            var isAssetAlreadySelected = false;            
            this.FADetailsGrid.getStore().each(function(recr){
                var assId = recr.get('assetId');
                if(assId.toUpperCase()==obj.value.toUpperCase() || assId.toLowerCase()==obj.value.toLowerCase() ){
                    isAssetAlreadySelected = true;
                    return false;
                }
            },this);              
            if(isAssetAlreadySelected){
                WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.asset.AssetIDalreadyentered")],0);
                obj.record.set('assetId','');
                return false;
            }
        }
            if (obj.field == 'machine') {
                var ismachineAlreadySelected = false;
                this.FADetailsGrid.getStore().each(function(recr) {
                    var assId = recr.get('machine');
                    if (assId== obj.value) {
                        ismachineAlreadySelected = true;
                        return false;
                    }
                }, this);
                if (ismachineAlreadySelected) {
                    WtfComMsgBox(['Information', 'Machine already entered.'], 0);
                    obj.record.set('machine', '');
                    return false;
                }
            }
    }
},
    
    updateRow:function(obj){
        if(obj!=null && (this.isCustomer  || this.fromPO)){
            var rec = obj.record;
            if(obj.field=='assetId'){
                var assetComboIndex = WtfGlobal.searchRecordIndex(this.assetComboStore, obj.value, 'assetdetailId');
                var assetrec = this.assetComboStore.getAt(assetComboIndex);
                obj.record.set('location',assetrec.get('location'));
                obj.record.set('assetName',assetrec.get('assetName'));
                obj.record.set('sellAmount',assetrec.get('sellAmount'));
                obj.record.set('department',assetrec.get('department'));
                obj.record.set('machine',assetrec.get('machine'));
                obj.record.set('assetdescription',assetrec.get('assetdescription'));
                obj.record.set('assetUser',assetrec.get('assetUser'));
                obj.record.set('cost',assetrec.get('cost'));
                // CALCULATE COST IN fOREIGN CURRENCY AS PER RATE AVAILABLE IN SALES INVOICE FORM AND SET IT IN 
                // costInForeignCurrency COLUMN VALUE PROFIT & LOSS WILL BE CALCULATED ACCORDING TO ITS VALUE
                
                var costInForeignCurrency = assetrec.get('cost');
                var salvageValueInForeignCurrency = assetrec.get('salvageValue');
                
//                if(this.lineRec){
//                     costInForeignCurrency = this.getAmountInForeignCurrency(costInForeignCurrency,this.lineRec);
//                     salvageValueInForeignCurrency = this.getAmountInForeignCurrency(salvageValueInForeignCurrency,this.lineRec);
//                }                           
                
                obj.record.set('costInForeignCurrency',costInForeignCurrency);
                obj.record.set('salvageRate',assetrec.get('salvageRate'));
                obj.record.set('salvageValue',assetrec.get('salvageValue'));
                obj.record.set('salvageValueInForeignCurrency',salvageValueInForeignCurrency);
//                obj.record.set('accumulatedDepreciation',assetrec.get('accumulatedDepreciation'));
                /* // ERP-16629: WDV field should be optional during asset creation
                obj.record.set('wdv',assetrec.get('wdv')); */
                obj.record.set('assetLife',assetrec.get('assetLife'));
                obj.record.set('elapsedLife',assetrec.get('elapsedLife'));
                obj.record.set('nominalValue',assetrec.get('nominalValue'));
                obj.record.set('purchaseDate', assetrec.get('purchaseDate'));
                obj.record.set('installationDate',assetrec.get('installationDate'));
                obj.record.set('batchdetails',assetrec.get('batchdetails'));
                obj.record.set('isDepreciationPosted',assetrec.get('isDepreciationPosted'));
                if (assetrec.json && this.isShowDimension()) {
                    for (var key in rec.data) {
                        if (key.indexOf('Custom') != -1) { // 'Custom' prefixed already used for custom fields/ dimensions
                            if (assetrec.json[key] != "null" && assetrec.json[key] != "" && assetrec.json[key] != "undefined" && assetrec.json[key] != "NaN" && assetrec.json[key] !=undefined) {
                                if(rec.data[key]!= null && rec.data[key] != ""  && rec.data[key] != "NaN" && rec.data[key] !=undefined){
                                    rec.set(key, rec.data[key]);
                                }else{
                                    rec.set(key, assetrec.json[key]);
                                }
                                
                            } else {
                                rec.set(key, "");
                            }

                        }
                    }
                }
            }
        }
        
        if(obj!=null){
            if (obj.field == 'assetId' && !(this.isCustomer  || this.fromPO)) {
                obj.record.set('isDepreciationPosted', false);
            }

            if(Wtf.account.companyAccountPref.depreciationCalculationType == 0){ // Yearly Depreciation
                var purchaseDate = obj.record.get('purchaseDate');
                var monthDateStr=purchaseDate.format('M d');

                if(Wtf.account.companyAccountPref.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                    if(Wtf.account.companyAccountPref.bbfrom){
                        monthDateStr=Wtf.account.companyAccountPref.bbfrom.format('M d');
                    }
                }else{
                    if(Wtf.account.companyAccountPref.fyfrom){
                        monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
                    }
                }
                var fd=new Date(monthDateStr+', '+purchaseDate.getFullYear());
                if(purchaseDate<fd){
                    fd=new Date(monthDateStr+', '+(purchaseDate.getFullYear()-1));
                }
                obj.record.set('installationDate',fd);
            }
            
            if(obj.field=='salvageRate'){
                
                if(obj.value>100){
                    WtfComMsgBox(['Information','Salvage Rate cannot be greater than 100'],0);
                    obj.record.set('salvageRate',0);
                    obj.record.set('salvageValue',0);
                    obj.record.set('salvageValueInForeignCurrency',0);
                    return;
                }
                if(obj.record.get('costInForeignCurrency') != ""){
                    var costInForeignCurrency = obj.record.get('costInForeignCurrency');
                    var salvageVal = costInForeignCurrency*obj.value/100;
                    obj.record.set('salvageValueInForeignCurrency',salvageVal);
                    var salvageValInBase = salvageVal;
                    if(this.lineRec){
                        salvageValInBase = this.getAmountInBase(salvageValInBase,this.lineRec);
                    }
                    
                    obj.record.set('salvageValue',salvageValInBase);
                }
            }else if(obj.field=='salvageValueInForeignCurrency'){
                var cst = 0;
                if(obj.record.get('costInForeignCurrency') != ""){
                    cst = obj.record.get('costInForeignCurrency');
                }
                if(obj.value>cst){
                    WtfComMsgBox(['Information','Salvage Value cannot be greater than Cost'],0);
                    obj.record.set('salvageRate',0);
                    obj.record.set('salvageValueInForeignCurrency',0);
                    obj.record.set('salvageValue',0);
                    return;
                }
                var salRate = 0;
                if(cst >0){
                   salvageValInBase = obj.value;
                   salRate = obj.value*100/cst; 
                   if(this.lineRec){
                        salvageValInBase = this.getAmountInBase(obj.value,this.lineRec);
                    }
                    obj.record.set('salvageValue',salvageValInBase);
                }
                obj.record.set('salvageRate',salRate);
            } else if(obj.field=='costInForeignCurrency' && obj.value != ''){
                var salvagRate = obj.record.get('salvageRate');
                
                // set cost in base currency after exchange
//                a a;
                var costInBase = obj.value;
                if(this.lineRec){
                    costInBase = this.getAmountInBase(obj.value,this.lineRec);
                }
                obj.record.set('cost',costInBase);
                var salvageValu = salvagRate*obj.value/100;
                
                obj.record.set('salvageValueInForeignCurrency',salvageValu);
                
                var salvageValuInBase = salvageValu;
                
                if(this.lineRec){
                    salvageValuInBase = this.getAmountInBase(salvageValu,this.lineRec);
                }
                
                obj.record.set('salvageValue',salvageValuInBase);
                
                var depRate = this.assetRec.get('depreciationRate');
                if(depRate>0){
                    var age = obj.value/(obj.value*depRate/100);
                    obj.record.set('assetLife',getRoundedAmountValue(age));
                }
            }
        }
    },
    
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },

getAmountInBase: function(value,rec) {// same logic as of calAmountWithExchangeRate() in invoiceGrid.js
    var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
    var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
    var v;
    if(rate!=0.0)
        v=(parseFloat(value)/parseFloat(rate));
    else
        v=(parseFloat(value)/parseFloat(oldcurrencyrate));
    
    if(isNaN(v)) return value;
    return v;
},
    
getAmountInForeignCurrency: function(value,rec) {// same logic as of calAmountWithExchangeRate() in invoiceGrid.js
    var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
    var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
    var v;
    if(rate!=0.0)
        v=(parseFloat(value)*parseFloat(rate));
    else
        v=(parseFloat(value)*parseFloat(oldcurrencyrate));
    
    if(isNaN(v)) return value;
    return v;
},
    
    processFADetailsGridRow: function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);

                store.remove(store.getAt(rowindex));
            }, this);
          } else if(e.getTarget(".serialNo-gridrow")){  //add serial no optin in fixed asset grid
             var store=grid.getStore();
             var record = store.getAt(rowindex);
            var productid =  this.mainProductID;  //Assetgroup id
            
        var newassetid="";
            //in sales side or while linking we will have followining assetid which will ve populated as serial no
        if(this.isCustomer || ((this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder)))){           
            var assetrec=WtfGlobal.searchRecord(this.assetComboStore, record.data.assetId, 'assetdetailId');
            if(assetrec!=undefined && assetrec!=null && assetrec!=""){   
                newassetid=assetrec.data.assetId;
            }
        } 
        if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
           if(this.isLocationForProduct || this.isWarehouseForProduct || this.isBatchForProduct || this.isSerialForProduct || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct) {
                this.callSerialNoWindow(record,productid,newassetid);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isFixedAsset?WtfGlobal.getLocaleText("acc.batchserial.FunctinalityAsset"):WtfGlobal.getLocaleText("acc.batchserial.Functinality")],2);   //Batch and serial no details are not valid.
                return;
            }
        }

      }
},
       callSerialNoWindow:function(record,productid,newassetid){
            var productComboRecIndex = WtfGlobal.searchRecordIndex(this.productComboStore, productid, 'productid');
            //in case of 'Asset GR' record search in 'this.assetRec' which pass from 'Asset GR viwe window'.
            if (productComboRecIndex == -1 && this.isFixedAsset){
               var proRecord = this.assetRec;
            } else if (productComboRecIndex >= 0){
                var proRecord = this.productComboStore.getAt(productComboRecIndex);
            }
            
        this.batchDetailswin=new Wtf.account.SerialNoWindow({
            renderTo: document.body,
            title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
            productName:proRecord.data.productname,
            quantity:1,
            defaultLocation:proRecord.data.location,
            productid:proRecord.data.productid,
            isSales:this.isCustomer,
            isDO:this.isCustomer?true:false,
            defaultWarehouse:proRecord.data.warehouse,
            batchDetails:record.data.batchdetails,
            isBatchForProduct:this.isBatchForProduct,
            isSerialForProduct:this.isSerialForProduct,
            isLocationForProduct:this.isLocationForProduct,
            isWarehouseForProduct:this.isWarehouseForProduct,
            isRowForProduct:this.isRowForProduct,
            isRackForProduct:this.isRackForProduct,
            isBinForProduct:this.isBinForProduct,
            assetId:(this.isCustomer || (this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder)))?newassetid:record.data.assetId,
            warrantyperiod:proRecord.data.warrantyperiod,
            warrantyperiodsal:proRecord.data.warrantyperiodsal,  
            isFixedAsset:this.isFixedAsset,
            isLeaseFixedAsset:this.isLeaseFixedAsset,
            moduleid:this.moduleid,
            islinkedFromLeaseSo:this.islinkedFromLeaseSo,
            readOnly:this.moduleid == Wtf.Acc_FixedAssets_Purchase_Return_ModuleId ? true :this.readOnly,
            width:950,
            height:400,
            resizable : false,
            modal : true
        });
        this.batchDetailswin.on("beforeclose",function(){
            this.batchDetails=this.batchDetailswin.getBatchDetails();
             var isfromSubmit=this.batchDetailswin.isfromSubmit;
                if(isfromSubmit){  //as while clicking on the cancel icon it was adding the reocrd in batchdetail json 
                    record.set("batchdetails",this.batchDetails);
                }
             },this);
        this.batchDetailswin.show();
        
    },
    
    addGridRec:function(obj){
        var isAssetDetailAvailable = false;
        
        var assetDetailsArrLength = 0;
        if(this.assetDetailsArray && this.assetDetailsArray !=''){
            this.assetDetailArr = eval('(' + this.assetDetailsArray + ')');
        }
        
        if(this.assetDetailArr && this.assetDetailArr.langth>0){
            isAssetDetailAvailable = true;
            assetDetailsArrLength = this.assetDetailArr.langth;
        }
        
        var iteratorLength = (this.quantity>assetDetailsArrLength)?this.quantity:assetDetailsArrLength;
        
        for(var i=0;i<iteratorLength;i++){
            var rec= this.FixedAssetDetailRec;
            rec = new rec({});
            rec.beginEdit();
            var fields=this.store.fields;
            for(var x=0;x<fields.items.length;x++){
                
                if(this.assetDetailArr)
                    this.assetDetailRec = this.assetDetailArr[i];
                
                var value="";
                
                if(this.assetDetailRec){
                    value = this.assetDetailRec[fields.get(x).name];
                    if(fields.get(x).name == 'installationDate'){
                        value = new Date(value);
                        if(this.isbilldateChanged && Wtf.account.companyAccountPref.depreciationCalculationType != 0 && !(this.isPILinkedInGR || this.isGRLinkedInPI || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId) && (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId)){
                            value = this.billDate;
                        }
                    }
                    if(fields.get(x).name == 'purchaseDate'){
                        value = new Date(value);
                        if(this.isbilldateChanged && !(this.isPILinkedInGR || this.isGRLinkedInPI || this.moduleid == Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId) && (this.moduleid == Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId)){
                            value = this.billDate;
                        }
                    }
                    if(!this.fromPO && (fields.get(x).name == 'assetId')){// not in linking case as record already comes decoded from java side
                        value = decodeURI(value);
                    }
                    
                    if(fields.get(x).name == 'assetName' && value && value != ''){
                        value = decodeURI(value);
                    }
                    
                    if(fields.get(x).name == 'assetdescription' && value && value != ''){
                        value = decodeURI(value);
                    }
                    
                    if(this.isCustomer){
                        if(fields.get(x).name == 'costInForeignCurrency'){
                            if(this.lineRec){
                                var costInForeign = this.assetDetailRec['cost'];//this.getAmountInForeignCurrency(this.assetDetailRec['cost'],this.lineRec);
                                value = costInForeign;
                            }
                        }

                        if(fields.get(x).name == 'salvageValueInForeignCurrency'){
                            if(this.lineRec){
                                var salvageValueInForeign = this.getAmountInForeignCurrency(this.assetDetailRec['salvageValue'],this.lineRec);
                                value = salvageValueInForeign;
                            }
                        }
                    }else{
                        if(fields.get(x).name == 'cost'){
                            if(this.lineRec){
                                var costInBASE = this.getAmountInBase(this.assetDetailRec['costInForeignCurrency'],this.lineRec);
                                value = costInBASE;
                            }
                        }

                        if(fields.get(x).name == 'salvageValue'){
                            if(this.lineRec){
                                var salvageValueInBASE = this.getAmountInBase(this.assetDetailRec['salvageValueInForeignCurrency'],this.lineRec);
                                value = salvageValueInBASE;
                            }
                        }
                    }
                    
                }else{
                    if(fields.get(x).name == 'cost' || fields.get(x).name == 'costInForeignCurrency' || fields.get(x).name == 'salvageValue' || fields.get(x).name == 'salvageValueInForeignCurrency' || fields.get(x).name == 'salvageRate' || fields.get(x).name == 'accumulatedDepreciation' || fields.get(x).name == 'assetLife' || fields.get(x).name == 'elapsedLife' || fields.get(x).name == 'nominalValue' || fields.get(x).name == 'sellAmount'){
                        value = 0;
                    }else if(fields.get(x).name == 'installationDate'){
                        if(Wtf.account.companyAccountPref.depreciationCalculationType == 0){
                            value = rec.get('installationDate');
                        }else{
                            value = this.billDate != undefined ? this.billDate : new Date();
                        }
                    }else if(fields.get(x).name == 'purchaseDate'){           
                        value = this.billDate != undefined ? this.billDate : new Date();
                        this.populatePurchaseDateAndSetInstallationDate(value,rec);
                        }                        
                }
                if (value == undefined) {
                    value = "";
                }
                rec.set(fields.get(x).name, value);
            }
            if (this.isShowDimension()) {
                for (var key in this.assetDetailRec) {
                    if (key.indexOf('Custom') != -1 && this.assetDetailRec[key] != undefined) { // 'Custom' prefixed already used for custom fields/ dimensions
                        //  recObj[key] = record[key+"_Value"];
                        if (this.assetDetailRec[key] != "null" && this.assetDetailRec[key] != "" && this.assetDetailRec[key] != "undefined" && this.assetDetailRec[key] != "NaN") {
                            rec.set(key, this.assetDetailRec[key]);
                        } else {
                            rec.set(key, "");
                        }

                    }
                }
                if (this.assetDetailRec != "" && this.assetDetailRec != undefined) {
                    var data = this.assetDetailRec.customfield;
                    if (data != undefined & data != "") {
                        for (var j = 0; j < data.length; j++) {
                            var value = data[j].fieldname;
                            value = data[j][value];
                            value = data[j][value];
                            if (value != "" && value != "undefined" && value != "NaN" && value != "null") {
                                if (data[j].xtype == "3") {
                                    value = parseInt(value);
                                    value = new Date(value);
                                }
                                rec.set(data[j].fieldname, value);
                            } else {
                                rec.set(data[j].fieldname, "");
                            }
                        }
                    }
                }
            }
            rec.endEdit();
            rec.commit();
            this.store.add(rec);
        }
        if (this.store && this.store.getCount() > 1) {
            this.store.sort('assetName', 'ASC');
        }
        if(this.parentObj != undefined){
            this.parentObj.isbilldateChanged=false;
        }
    },
    
    populatePurchaseDateAndSetInstallationDate : function(value,rec){
        if(Wtf.account.companyAccountPref.depreciationCalculationType == 0){
            var purchaseDate = value;
            var monthDateStr=purchaseDate.format('M d');

            if(Wtf.account.companyAccountPref.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                if(Wtf.account.companyAccountPref.bbfrom){
                    monthDateStr=Wtf.account.companyAccountPref.bbfrom.format('M d');
                }
            }else{
                if(Wtf.account.companyAccountPref.fyfrom){
                    monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
                }
            }
            var fd=new Date(monthDateStr+', '+purchaseDate.getFullYear());
            if(purchaseDate<fd){
                fd=new Date(monthDateStr+', '+(purchaseDate.getFullYear()-1));
            }
            rec.set('installationDate',fd);
        }
    },
     unitPriceRendererWithPermissionCheck:function(v,m,rec){
        if (!isNaN(v)) {
            if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                return Wtf.UpriceAndAmountDisplayValue;
            } else {
                if (rec.data.prtaxid != "None") {
                    return WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec);
                } else {
                    if(this.parentObj!== null && this.parentObj.includingGST != undefined && this.parentObj.includingGST.checked == true){
                         return WtfGlobal.withCurrencyUnitPriceRenderer(rec.data.rateIncludingGst,m,rec);
                    }else{
                        return WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec);
                    }
                }
            }
        }
    },
    /**
     * Auto populate custom field value from Asset Group level to Asset Details level.
     */
    populateCustomFieldValue: function(grid) {
        var cnt = 0;
        if (this.tagsFieldset && this.tagsFieldset.dimensionFieldArray && this.tagsFieldset.dimensionFieldArrayValues) {
            var dimensionFieldArray = this.tagsFieldset.dimensionFieldArray;
            var dimensionFieldArrayValues = this.tagsFieldset.dimensionFieldArrayValues;
            for (cnt = 0; cnt < dimensionFieldArray.length; cnt++) {
                if(dimensionFieldArray[cnt].iscustomcolumn==false){
                    var dimensionfieldname = dimensionFieldArray[cnt].name;
                    
                    var fieldId = dimensionFieldArray[cnt].id
                    var dimensionvalue = Wtf.getCmp(fieldId).lastSelectionText;
                   
                    var dropDowntype = false;
                    if (dimensionFieldArrayValues[cnt].fieldtype == Wtf.CustomFieldType.SingleSelectDropdown || dimensionFieldArrayValues[cnt].fieldtype == Wtf.CustomFieldType.MultiSelectDropdown) {
                        dimensionvalue = this.getValueForGlobalDimension(dimensionfieldname, dimensionvalue);
                        dropDowntype = true;
                    }
                    
                    var dimensionarray = grid.store.data.items;
                    if (dimensionarray.length > 0) {
                        for (var i = 0; i < dimensionarray.length - 0; i++) {
                            for (var k = 0; k < grid.colModel.config.length; k++) {
                                if (grid.colModel.config[k].dataIndex == dimensionfieldname) {
                                    var gridRecord = grid.store.getAt(i);
                                    if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store && dropDowntype) {
                                        var store = grid.colModel.config[k].editor.field.store;
                                        var valArr = [];
                                        if(dimensionvalue!=undefined && dimensionvalue!=''){
                                            valArr = dimensionvalue.split(',');
                                        }
                                            
                                        var ComboValueID = "";
                                        for (var index = 0; index < valArr.length; index++) {
                                            var recCustomCombo = WtfGlobal.searchRecord(store, valArr[index], "name");
                                            if (recCustomCombo){
                                                ComboValueID += recCustomCombo.data.id + ',';
                                            }                                                
                                        }
                                        if (ComboValueID.length > 0)
                                            ComboValueID = ComboValueID.substring(0, ComboValueID.length - 1);
                                        gridRecord.set(dimensionfieldname, ComboValueID);
                                    } else {
                                        gridRecord.set(dimensionfieldname, dimensionvalue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        var GlobalcolumnModel = GlobalColumnModel[this.moduleid];
        if (GlobalcolumnModel) {
            for (cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
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
                                    if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store && dropDowntype) {
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
    setCustomFieldValues: function(grid,isAllFieldsLoaded) {
        if (isAllFieldsLoaded) {
            this.populateCustomFieldValue(grid);
            grid.getView().refresh();
        }
    },
    /*
     * Disable custom fields at asset detail level
     */
    disableCustomFieldOfGrid: function() {
        for (var k = 0; k < this.FADetailsGrid.colModel.config.length; k++) {
            if (this.FADetailsGrid.colModel.config[k].dataIndex.indexOf('Custom_') != -1) {
                this.FADetailsGrid.colModel.config[k].editor = '';
            }
        }
    },    
    
    getValueForGlobalDimension: function(fieldName, value) {
        var grid = this.FADetailsGrid;
        if (grid) {
            var array = grid.store.data.items;
            if (array.length > 0) {
                for (var i = 0; i < array.length; i++) {
                    for (var k = 0; k < grid.colModel.config.length; k++) {
                        if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store && grid.colModel.config[k].dataIndex == fieldName) {
                            var store = grid.colModel.config[k].editor.field.store;
//                            var gridRecord = grid.store.getAt(i);
                            var valArr = value.split(',');
                            var returnData = "";
                            for (var index = 0; index < valArr.length; index++) {
                                var recCustomCombo = WtfGlobal.searchRecord(store, valArr[index], "name");
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
    
    closeFADetails:function(){
        var arr = [];
        
//        this.store.each(function(record){
//            if(record.get('assetId') == ""){
//                WtfComMsgBox(['Infornation','Please Enter an appropriate value for Fixed Asset Id.'],0);
//                return;
//            }
//            arr.push(this.store.indexOf(record));
//        },this);
//        
//        if(arr.length != this.quantity){
//            WtfComMsgBox(['Infornation','Quantity entered by you is <b>'+this.quantity+'</b>. which does not match with asset entered.'],0);
//            return;
//        }
        
        this.isFromSaveButton = false;
        this.close();
    },
    saveData:function(){
        var arr = [];
        var profitLossAmtOnSelling=0;

        var returnFlag = false;
       if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){
      if((this.isBatchForProduct || this.isSerialForProduct ||this.isLocationForProduct || this.isWarehouseForProduct || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct) && (this.isFromOrder || !this.isInvoice || this.isFromOpeningForm)){    //check wether batch and serial no detail entered or not
            var prodLength=this.FADetailsGrid.getStore().data.items.length
            for(var i=0;i<prodLength;i++)
            { 
                var batchDetail = this.FADetailsGrid.getStore().data.items[i].data.batchdetails;
                if(batchDetail == undefined || batchDetail == ""){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                    return;
                }
                
                    if (this.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId) {
                        var rowObj = eval(batchDetail);
                        var assetID = this.FADetailsGrid.getStore().data.items[i].data.assetId;
                        if (rowObj) {
                            for (var cnt = 0; cnt < rowObj.length; cnt++) {

                                if (this.isLocationForProduct) {
                                    if (rowObj[cnt].location == "" || rowObj[cnt].location == undefined) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("Location should not be empty for Asset ID: " + assetID)], 2);
                                        return false;
                                    }
                                }

                                if (this.isWarehouseForProduct) {
                                    if (rowObj[cnt].warehouse == "" || rowObj[cnt].warehouse == undefined) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("Warehouse should not be empty for Asset ID: " + assetID)], 2);
                                        return false;
                                    }
                                }
                                if (this.isBatchForProduct) {
                                    if (rowObj[cnt].batch == "" || rowObj[cnt].batch == undefined) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("Batch name should not be empty for Asset ID: " + assetID)], 2);
                                        return false;
                                    }
                                }
                                if (this.isSerialForProduct) {
                                    if (rowObj[cnt].serialno == "" || rowObj[cnt].serialno == undefined) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("Serial No. should not be empty for Asset ID: " + assetID)], 2);
                                        return false;
                                    }
                                }

                                if (this.isRowForProduct) {
                                    if (rowObj[cnt].row == "" || rowObj[cnt].row == undefined) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("Row should not be empty for Asset ID: " + assetID)], 2);
                                        return false;
                                    }
                                }
                                if (this.isRackForProduct) {
                                    if (rowObj[cnt].rack == "" || rowObj[cnt].rack == undefined) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("Row should not be empty for Asset ID: " + assetID)], 2);
                                        return false;
                                    }
                                }
                                if (this.isBinForProduct) {
                                    if (rowObj[cnt].bin == "" || rowObj[cnt].bin == undefined) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("Bin should not be empty for Asset ID: " + assetID)], 2);
                                        return false;
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }
        this.store.each(function(record){
            if(record.get('assetId') == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.PleaseenterappropriatevalueFixedAssetId")],0);
                returnFlag = true;
                return;
            } else if((!this.moduleid==Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId) && (record.get('assetLife') == '' || record.get('assetLife') == 0 )){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.pleaseenterappropriatevalueforassetlife")],0);
                returnFlag = true;
                return; 
            }else if(record.get('purchaseDate') == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.PleaseenterappropriatevaluePurchaseDate")+' <br>Asset Id: <b>'+record.get('assetId')+'</b>'],0);
                returnFlag = true;
                return;
            }else if(record.get('installationDate') == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.PleaseenterappropriatevalueInstallationDate")+' <br>Asset Id: <b>'+record.get('assetId')+'</b>'],0);
                returnFlag = true;
                return;
            }else if(record.get('assetLife')<=0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.PleaseenterappropriatevalueofAssetLife")+' <br>Asset Id: <b>'+record.get('assetId')+'</b>'],0);
                returnFlag = true;
                return;
            }else{
//                record.set('wdv',(record.get('wdv')==undefined || record.get('wdv')=="")?0.00:record.get('wdv'));
                record.set('assetLife',(record.get('assetLife')==undefined ||record.get('assetLife')=="")?0.00:record.get('assetLife'));
                record.set('elapsedLife',(record.get('elapsedLife')==undefined||record.get('elapsedLife')=="")?0.00:record.get('elapsedLife'));
                record.set('nominalValue',(record.get('nominalValue')==undefined||record.get('nominalValue')=="")?0.00:record.get('nominalValue'));
                record.set('isDepreciationPosted', (record.get('isDepreciationPosted')==undefined || record.get('isDepreciationPosted')=="")? false : record.get('isDepreciationPosted'));
            }
                /**
                 * this calculation will be done on java while saving the document.
                 */
//            if(this.isCustomer && this.isInvoice){
//                var temporaryDiff = 0;
//                if(Math.abs(record.get('costInForeignCurrency') - record.get('sellAmount'))>Wtf.decimalLimiterValue){
//                    temporaryDiff = (record.get('sellAmount')-record.get('costInForeignCurrency'));
//                }
//                profitLossAmtOnSelling += temporaryDiff;
//            }
            arr.push(this.store.indexOf(record));
        },this);
        
        if(returnFlag){
            return;
        }
        
        if(arr.length != this.quantity){
            WtfComMsgBox(['Information','Quantity entered by you is <b>'+this.quantity+'</b>. which does not match with asset entered.'],0);
            return;
        }
        
        //check is there duplicate Asset ID in window
        if (this.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId) {
            var isDuplicate = false;
            var duplicateval = ", ";
            var count = this.store.data.items.length;

            for (var i = 0; i < count; i++) {
                var outerAssetID = this.store.getAt(i).data['assetId'];
                for (var j = i + 1; j < count; j++) {
                    var innerAssetID = this.store.getAt(j).data['assetId'];
                    if (outerAssetID == innerAssetID) {
                        isDuplicate = true;
                        if (duplicateval.indexOf(", " + innerAssetID + ",") == -1) {
                            duplicateval += innerAssetID + ", ";//Add duplicate Asset ID

                        }
                    }
                }
            }
            if (isDuplicate) {
                duplicateval = duplicateval.substring(2, (duplicateval.length - 2));
                WtfComMsgBox(['Information', 'Duplicate Asset Id(s)[<b>' + duplicateval + '</b>] are entered.'], 0);
                return;
            }
        }
        if (this.isShowDimension()) {
            this.store.each(function(rec) {
                if (rec.data.assetDetails != "") {
                    rec.data[CUSTOM_FIELD_KEY] = Wtf.decode(this.getCustomColumnData(rec.data, this.module).substring(13));
                }
                arr.push(this.store.indexOf(rec));
            }, this);
        }
        this.assetDetails = this.getJSONArray(arr);
        this.profitLossAmtOnSelling = profitLossAmtOnSelling;
        this.isFromSaveButton = true;
        if (this.lineRec != undefined && this.lineRec != "") {
            this.lineRec.data.profitLossAmt = this.profitLossAmtOnSelling;
            this.lineRec.data.assetDetails = this.assetDetails;
        }
        this.close();
    },
    getCustomColumnData:function(rData,moduleid){
        var jsondata = ",customfield:[]";
        var GlobalcolumnModel=GlobalColumnModel[moduleid];
        var GlobalColumnModelForReports=this.globalColumnModelForReports[moduleid];
        var isGlobalcolumnModel = false, isGlobalColumnModelForReports=false;
        
        if(GlobalcolumnModel || GlobalColumnModelForReports){
            jsondata =',customfield:[';
            
            if(GlobalcolumnModel){
                isGlobalcolumnModel = true;
                jsondata += '{';
                for(var cnt = 0;cnt<GlobalcolumnModel.length;cnt++){
                    var fieldname = GlobalcolumnModel[cnt].fieldname;
                    var refcolumn_number = GlobalcolumnModel[cnt].refcolumn_number;
                    var column_number = GlobalcolumnModel[cnt].column_number;
                    var fieldid = GlobalcolumnModel[cnt].fieldid;
                    var fieldtype = GlobalcolumnModel[cnt].fieldtype;
                    //              if(isMassordetails&&(rData[fieldname]=="" || rData[fieldname]==undefined)){
                    //                  continue;
                    //              }
                    if(cnt > 0){
                        jsondata +="},{";
                    }
                    var recData = "";
                    if(fieldname.indexOf('.')>=0){
                        recData = rData[fieldname.replace(".","")];
                    }else{
                        recData = rData[fieldname];
                    }
                    if(GlobalcolumnModel[cnt].fieldtype=="3" && !Wtf.isEmpty(recData)){
                        var daterec =recData;
                        if(recData!=undefined && recData!="" ){
                            daterec =new Date(recData).getTime();
                        }
                        jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": \""+daterec+"\",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
                    }else if(GlobalcolumnModel[cnt].fieldtype=="5"){  // Time Field
                        if(!Wtf.isEmpty(recData)){
                            recData =  recData;
                        }
                        jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": \""+recData+"\",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
                    }else{  
                        if(!Wtf.isEmpty(recData)){  //ERP-12328 [SJ]
                            recData =  recData;
                        }
                        else{
                            recData='';
                        }
                        jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": "+Wtf.util.JSON.encode(recData)+",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
                    }
                }
                jsondata +='},';
            }
            
            if(GlobalColumnModelForReports){
                isGlobalColumnModelForReports=true;
                jsondata +='{';
                for(var cnt = 0;cnt<GlobalColumnModelForReports.length;cnt++){
                    var fieldname = GlobalColumnModelForReports[cnt].fieldname;
                    var refcolumn_number = GlobalColumnModelForReports[cnt].refcolumn_number;
                    var column_number = GlobalColumnModelForReports[cnt].column_number;
                    var fieldid = GlobalColumnModelForReports[cnt].fieldid;
                    var fieldtype = GlobalColumnModelForReports[cnt].fieldtype;
                    //              if(isMassordetails&&(rData[fieldname]=="" || rData[fieldname]==undefined)){
                    //                  continue;
                    //              }
                    if(cnt > 0){
                        jsondata +="},{";
                    }
                    var recData = "";
                    if(fieldname.indexOf('.')>=0){
                        recData = rData[fieldname.replace(".","")];
                    }else{
                        recData = rData[fieldname];
                    }
                    if(GlobalColumnModelForReports[cnt].fieldtype=="3" && !Wtf.isEmpty(recData)){
                        var daterec =recData;
                        if(recData!=undefined && recData!="" ){
                            daterec =new Date(recData).getTime();
                        }
                        jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": \""+daterec+"\",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
                    }else if(GlobalColumnModelForReports[cnt].fieldtype=="5"){  // Time Field
                        if(!Wtf.isEmpty(recData)){
                            recData =  recData;
                        }
                        jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": \""+recData+"\",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
                    }else{  
                        if(!Wtf.isEmpty(recData)){  //ERP-12328 [SJ]
                            recData =  recData;
                        }
                        else{
                            recData='';
                        }
                        jsondata +="\"refcolumn_name\": \""+refcolumn_number+"\",\"fieldname\": \""+fieldname+"\",\""+column_number+"\": "+Wtf.util.JSON.encode(recData)+",\""+fieldname+"\": \""+column_number+"\",\"filedid\":\""+fieldid+"\",\"xtype\":\""+fieldtype+"\"";
                    }
                }
                jsondata +='},';
            }
            if(isGlobalcolumnModel || isGlobalColumnModelForReports){
                jsondata = jsondata.substr(0, jsondata.length-1);
            }
            
            jsondata +=']';
        }
        return jsondata;
    },
  
    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.FADetailsGrid,true,arr);
    },

    isShowDimension: function() {
        if (this.moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_RFQ_ModuleId
            || this.moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
            return false;
        } else {
            return true;
        }
            }
});
