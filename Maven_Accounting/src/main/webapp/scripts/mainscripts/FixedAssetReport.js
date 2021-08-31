Wtf.account.FixedAssetReport = function(config){
    this.isMaintenanceScheduler = (config.isMaintenanceScheduler)?config.isMaintenanceScheduler:false;
    this.isCreateSchedule = (config.isCreateSchedule)?config.isCreateSchedule:false;
    this.isUpdateSchedules = (config.isUpdateSchedules)?config.isUpdateSchedules:false;
    this.isDisposedAssetReport = (config.isDisposedAssetReport)?config.isDisposedAssetReport:false;//Disposed Assets report flag
    this.isFixedAssetDetailReport = (config.isFixedAssetDetailReport)?config.isFixedAssetDetailReport:false;//Fixed Assets all asset report flag
    this.depreciationCalculationType = Wtf.account.companyAccountPref.depreciationCalculationType;
    Wtf.apply(this, config);
    Wtf.account.FixedAssetReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.FixedAssetReport, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.FixedAssetReport.superclass.onRender.call(this, config);
        
        //create Grid
        
        this.createFAGrid();
        this.getMyConfig();
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent,{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.btnArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.gridStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                        items: this.tbarArr
                })
            }]
        }); 
        this.add(this.leadpan);
    },
    
    createFAGrid : function(){
        var dataArr = new Array();
        dataArr.push([0,WtfGlobal.getLocaleText("acc.rem.105")]);// 0 : All
        if(this.isDisposedAssetReport){
            dataArr.push([1,WtfGlobal.getLocaleText("acc.fixed.asset.manuallydisposed")]);// 1 : Manually Disposed 
            dataArr.push([2,WtfGlobal.getLocaleText("acc.fixed.asset.disposed")]);// 2 : Disposed throudh DI or DO
        }else{
            dataArr.push([3,WtfGlobal.getLocaleText("acc.fixed.asset.active")]);// 3 : Active assets
            dataArr.push([4,WtfGlobal.getLocaleText("acc.fixed.asset.disposed")]);// 4 : All Diposed Assets
        }
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :dataArr
        });    
        this.typeEditor = new Wtf.form.ComboBox({
            store: this.typeStore,
            name:'typeid',
            displayField:'name',
            id: 'typeEditor'+this.id,
            valueField:'typeid',
            mode: 'local',
            defaultValue:0,
            width:120,
            listWidth:150,
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true,
            value:0
        });
        this.typeEditor.on('select', this.typeEditorOnSelect.createDelegate(this), this);
        
        this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.getDates(true)
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.getDates(false)
        });
        
        this.checkDates();
        
        //Add Asset Group Combo to select different Group
        this.assetGroupStore = Wtf.FixedAssetStore;
        
        this.assetGroupComboConfig = {   
            emptyText:WtfGlobal.getLocaleText("acc.fxexposure.all"),
            name: 'productname',
            store:this.assetGroupStore,       
            typeAhead: true,
            selectOnFocus:true,
            valueField:'productid',
            displayField: 'productname',
            extraFields:['pid'],
            extraComparisionField:'pid',// type ahead search on acccode as well.
            lastQuery:'',
            scope:this,
            hirarchical:true,
            triggerAction: 'all'
        };
           
        this.assetGroupCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),
            forceSelection:true,   
            listWidth:300,
            width:180
        },this.assetGroupComboConfig));
        
        this.toolbarArr = [];
        this.toolbarArr.push(WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),this.assetGroupCombo);
        
        this.gridRec = Wtf.data.Record.create ([
            {name:'assetdetailId'},
            {name:'assetGroup'},
            {name:'assetDepreciationMethod'},
            {name:'assetGroupId'},
            {name:'assetId'},
            {name:'description'},
            {name:'installationDate',type:'date'},
            {name:'purchaseDate',type:'date'},
            {name:'assetValue'},
            {name:'currencycode'},
            {name:'assetvaluewithoutlandedcost'},
            {name:'salvageRate'},
            {name:'salvageValue'},
            {name:'assetNetBookValue'},
            {name:'accumulateddepreciationcost'},
            {name:'issummaryvalue'},
            {name:'deleted'},
            {name:'assetLife'},
            {name:'isDepreciable'},
            {name:'isLeased'},
            {name:'assetUser'},
            {name:'serialno'},
            {name:'location'},
            {name:'warehouse'},
            {name:'row'},
            {name:'rack'},
            {name:'bin'},
            {name:'department'},
            {name:'batchname'},
            {name:'expstart',type:'date'},
            {name:'expend',type:'date'},
            {name:'assetdepreciationschedule'},
            {name:'isdepreciationposted'},
            {name:'disposalDate',type:'date'},
            {name:'remainingLife'},
            {name:'salesproceed'},
            {name:'status'},
            {name: 'openingDepreciation'},
            {name: 'vendorname'},
            {name: 'purchaseinvno'},
            {name: 'disposalinvoiceno'},
            {name: 'disposalinvoicedate', type:'date'},
            {name: 'disposaljeid'},
            {name: 'disposaljeno'},
            {name: 'purchaseinvdate', type:'date'},
            {name: 'isLocationForProduct'},
            {name: 'isWarehouseForProduct'},
            {name: 'cost_openingbalance'},
            {name: 'additions'},
            {name: 'disposal'},
            {name: 'costclosingbal'},
            {name: 'openingdep'},
            {name: 'normalopeningdepreciation'},
            {name: 'yearval'},
            {name: 'depdisposal'},
            {name: 'depclosingbal'},
            {name: 'netbookvalue'},
            {name: 'gstcode'},
            {name: 'groupinfo'}
        ]);
        
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        },this.gridRec);
        
        this.gridStore = new Wtf.data.GroupingStore({
            url:"ACCAsset/getAssetDetails.do",
            groupField:'groupinfo', 
            reader:this.gridStoreReader,
            baseParams:{
                assetGroupIds : this.assetGroupCombo != undefined ? this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue() : "All",
                isDisposedAssetReport : this.isDisposedAssetReport,
                isFixedAssetDetailReport:this.isFixedAssetDetailReport,
                depreciationCalculationType: this.depreciationCalculationType,
                isCreateSchedule : this.isCreateSchedule,
                stdate:this.sdate,
                enddate:this.edate,
                type:this.typeEditor.getValue()
            },
            sortInfo: {
                field: 'assetGroup',
                direction: "DESC"
            }
        });
        
        var colModelArray = [];
        var colModelArrayReports = GlobalColumnModelForReports[Wtf.Acc_FixedAssets_Details_ModuleId];
        WtfGlobal.updateStoreConfig(colModelArrayReports, this.gridStore);
        colModelArray = GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId];
        if (colModelArray) {
            colModelArray.concat(GlobalColumnModelForProduct[Wtf.Acc_FixedAssets_Details_ModuleId]);
        }
        WtfGlobal.updateStoreConfig(colModelArray, this.gridStore);
        
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : 'Loading...'
        });
        
        this.gridStore.on('loadexception',function(){
            WtfGlobal.resetAjaxTimeOut();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);
        
       this.gridStore.on('beforeload',function(){
            WtfGlobal.setAjaxTimeOut();
            var currentBaseParams = this.gridStore.baseParams;
            currentBaseParams.assetGroupIds = this.assetGroupCombo != undefined ? this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue() : "All";
            currentBaseParams.stdate = this.sdate;
            currentBaseParams.enddate = this.edate;
            currentBaseParams.type = this.typeEditor.getValue();
            this.gridStore.baseParams=currentBaseParams; 
            this.loadMask.show();
        },this);
        
        this.gridStore.on('load',function(store){
            WtfGlobal.resetAjaxTimeOut();
            if(this.gridStore.getCount()==0){
                if(this.expButton)this.expButton.disable();
                if(this.printButton)this.printButton.disable();
            }else{
                if(this.expButton)this.expButton.enable();
                if(this.printButton)this.printButton.enable();    
            }
            var arrdepreciated=[];
            this.arrRec = this.gridStore.getRange(0,this.gridStore.getCount()-1);
            for(var i=0;i<this.gridStore.getCount();i++){
                if(this.arrRec[i].data.isdepreciationposted == "Yes"){
                    arrdepreciated.push(this.arrRec[i]);
                }
            }
            if(CompanyPreferenceChecks.highlightDepreciatedAssets()){
                WtfGlobal.highLightRowColor(this.grid,arrdepreciated,true,0,2);
            }
            this.loadMask.hide();
            this.quickPanelSearch.StorageChanged(store);
        },this);
        
//        this.gridStore.load({
//            params:{
//                start:0,
//                limit:30,
//                ss : this.quickPanelSearch != undefined ? this.quickPanelSearch.getValue() : "",
//                stdate:this.sdate,
//                enddate:this.edate,
//                type:this.typeEditor.getValue(),
//                assetGroupIds : this.assetGroupCombo != undefined ? this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue() : "All",
//                isDisposedAssetReport : this.isDisposedAssetReport,
//                isFixedAssetDetailReport:this.isFixedAssetDetailReport,
//                depreciationCalculationType: this.depreciationCalculationType,
//                isCreateSchedule : this.isCreateSchedule
//            }
//        });
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            multiSelect:true
        });
        
        this.sm.on("selectionchange",this.enableDisablePostUnpostButtons.createDelegate(this),this);
        
        var netbookvalueheader = "(" + WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header6") + ") - (" + WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header10") + ")";
        var FixedAssetDetailArr = [];
        FixedAssetDetailArr.push(this.sm,{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.id"),
            dataIndex:'assetId',
            renderer:WtfGlobal.deletedRenderer,
            width:120,
            pdfwidth:120,
            sortable:true,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.product.description"),
            dataIndex:'description',
            renderer:WtfGlobal.deletedRenderer,
            width:120,
            pdfwidth:120,
            align:'center'
        },{
            header: WtfGlobal.getLocaleText("acc.field.DisposalJENo"),
            dataIndex: "disposaljeno",
            width: 120,
            renderer:WtfGlobal.linkDeletedRenderer,
            pdfwidth:120,
            hidden: !this.isDisposedAssetReport,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            dataIndex:'location',
            //              renderer:WtfGlobal.deletedRenderer,
            renderer:this.emptyeDeletedRenderer,
            pdfwidth:120,
            width:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),  //warehouse
            dataIndex:'warehouse',
            //                renderer:WtfGlobal.deletedRenderer,
            renderer:this.emptyeDeletedRenderer,
            pdfwidth:120,
            width:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.row"),
            dataIndex:'row',
            renderer:this.emptyeDeletedRenderer,
            pdfwidth:120,
            width:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.rack"),
            dataIndex:'rack',
            renderer:this.emptyeDeletedRenderer,
            pdfwidth:120,
            width:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.bin"),
            dataIndex:'bin',
            renderer:this.emptyeDeletedRenderer,
            pdfwidth:120,
            width:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.field.department"),
            dataIndex:'department',
            renderer:this.emptyeDeletedRenderer,
            pdfwidth:120,
            width:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.inventorysetup.batch"),  //batcho
            dataIndex:'batchname',
            //                renderer:WtfGlobal.deletedRenderer,
            renderer:this.emptyeDeletedRenderer,
            pdfwidth:120,
            width:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.field.SerialNo"),  //serial no
            dataIndex:'serialno',
            //              renderer:WtfGlobal.deletedRenderer,
            renderer:this.emptyeDeletedRenderer,
            pdfwidth:120,
            width:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.field.WarrExp.FromDate"),  //warranty expiry from date
            dataIndex:'expstart', 
            pdfwidth:120,
            width:120,
            align:'center',
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden: this.isDisposedAssetReport
        },{
            header:WtfGlobal.getLocaleText("acc.field.WarrExp.EndDate"),  //warranty expiry end date
            dataIndex:'expend',
            pdfwidth:120,
            align:'center',
            width:120,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden: this.isDisposedAssetReport
        },{
            header:WtfGlobal.getLocaleText("acc.ven.name"),
            dataIndex:'vendorname',
            renderer:WtfGlobal.deletedRenderer,
            width:120,
            pdfwidth:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.prList.invNo"),
            dataIndex:'purchaseinvno',
            renderer:WtfGlobal.deletedRenderer,
            width:120,
            pdfwidth:120,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.het.912"), //Disposal Invoice No
            dataIndex:'disposalinvoiceno',
            renderer:WtfGlobal.deletedRenderer,
            width:120,
            pdfwidth:120,
            align:'center'
        },
        {
            header:WtfGlobal.getLocaleText("acc.het.913"),//'Disposal Invoice Date',
            dataIndex:'disposalinvoicedate',
            align:'center',
            width:120,
            pdfwidth:120,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        }
        ,{
            header:WtfGlobal.getLocaleText("acc.het.906"),//'Purchase Invoice date',
            dataIndex:'purchaseinvdate',
            align:'center',
            width:120,
            sortable:true,
            pdfwidth:120,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.PurchaseDate"),//'Installation Date',
            dataIndex:'purchaseDate',
            align:'center',
            width:120,
            pdfwidth:120,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.date.installation"),//'Installation Date',
            dataIndex:'installationDate',
            align:'center',
            width:120,
            sortable:true,
            pdfwidth:120,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridAssetGroup"),//'Asset Group',
            dataIndex:'groupinfo',
            pdfwidth:120,
            width:120,
            sortable:true,
            hidden:false
        },{
            header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
            dataIndex:'currencycode',
            hidden:true,
            pdfwidth:85
        });
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && GlobalColumnModelForLandedCostCategory && GlobalColumnModelForLandedCostCategory.columnModel) {
            var landedCostCM = GlobalColumnModelForLandedCostCategory.columnModel;
            for (var ccnt = 0; ccnt < landedCostCM.length; ccnt++) {
                FixedAssetDetailArr.push({
                    header: landedCostCM[ccnt].header,
                    dataIndex: landedCostCM[ccnt].dataIndex,
                    width: 150,
                    pdfwidth: 50,
                    renderer: eval('(' + landedCostCM[ccnt].renderer + ')')
                });
            }
            if (GlobalColumnModelForLandedCostCategory.rec) {
                this.updateStoreConfig(GlobalColumnModelForLandedCostCategory.rec, this.gridStore);
            }
        }
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.account.companyAccountPref.isActiveLandingCostOfItem) {
            FixedAssetDetailArr.push({
                header: "Asset Value Without Landed Cost",
                dataIndex: "assetvaluewithoutlandedcost",
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                align: 'center',
                width: 150,
                pdfwidth: 75
            });
        }
        FixedAssetDetailArr.push({
            header:WtfGlobal.getLocaleText("acc.assetValue"),//'Asset Value',
            dataIndex:'assetValue',
            pdfwidth:120,
            width:120,
            align:'center',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.rate.salvage"),//'Salvage Rate',
            dataIndex:'salvageRate',
            hidden:true,
            pdfwidth:120,
            width:120,
            align:'center',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.rate.salvagValue"),//'Salvage Value',
            dataIndex:'salvageValue',
            hidden:(this.isMaintenanceScheduler || this.isDisposedAssetReport),
            pdfwidth:120,
            width:120,
            align:'center',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.accDep"),//'Accumulated Depreciation',
            dataIndex:'accumulateddepreciationcost',
            pdfwidth:120,
            width:120,
            align:'center',
            hidden:!this.isDisposedAssetReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.nbValue"),//'Net Book Value',
            dataIndex:'assetNetBookValue',
            hidden:this.isMaintenanceScheduler,
            pdfwidth:120,
            width:120,
            align:'center',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.SalesProceed"),//'Sales Proceed',
            dataIndex:'salesproceed',
            hidden:!this.isDisposedAssetReport,
            pdfwidth:120,
            width:120,
            align:'center',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.life"),//'Asset Life(in Years)',
            dataIndex:'assetLife',
            pdfwidth:120,
            width:120,
            renderer:WtfGlobal.deletedRenderer,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.remaininglife"),//'Remaining Life(in Years)',
            dataIndex:'remainingLife',
            pdfwidth:120,
            width:120,
            renderer:function(v,m,rec){
                if(!v) {
                    return v;
                }
                v = parseFloat(getRoundedAmountValue(v)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                if(rec.data.deleted){
                    v='<del>'+v+'</del>';
                }                    
                return v;
            },
            align:'center',
            hidden:!this.isDisposedAssetReport
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.user"),//'Asset User',
            dataIndex:'assetUser',
            hidden:this.isMaintenanceScheduler,
            pdfwidth:120,
            width:120,
            renderer:WtfGlobal.deletedRenderer,
            align:'center'
        },
        {
            header:WtfGlobal.getLocaleText("acc.assetdepriciation.grid.OpeningDepreciation"),
            dataIndex:'normalopeningdepreciation',
            pdfwidth:120,
            width:100,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },
        {
            header:WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header3"),
            dataIndex:'cost_openingbalance',
            pdfwidth:120,
            width:150,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header4"),
            dataIndex:'additions',
            pdfwidth:120,
            width:100,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header5"),
            dataIndex:'disposal',
            pdfwidth:120,
            width:150,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header6"),
            dataIndex:'costclosingbal',
            pdfwidth:120,
            width:150,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header7"),
            dataIndex:'openingdep',
            pdfwidth:120,
            width:200,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header8"),
            dataIndex:'yearval',
            pdfwidth:120,
            width:180,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header9"),
            dataIndex:'depdisposal',
            pdfwidth:120,
            width:150,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.fixedassetsummeryreport.header10"),
            dataIndex:'depclosingbal',
            pdfwidth:120,
            width:180,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:"<span wtf:qtip='"+ netbookvalueheader +"'>"+ netbookvalueheader +"</span>",
            dataIndex:'netbookvalue',
            pdfwidth:200,
            width:200,
            hidden:!this.isFixedAssetDetailReport,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.isleased"),//Is Leased,
            dataIndex:'isLeased',
            pdfwidth:120,
            width:120,
            hidden:(this.isMaintenanceScheduler||this.isDisposedAssetReport),
            renderer:function(v,m,rec){
                //                    if(!v) return v;
                if(v) 
                    v = 'Yes';
                else 
                    v = 'No';
                if(rec.data.deleted)
                    v='<del>'+v+'</del>';
                return v;
            },
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.field.Assetdepreciationschedule"),//Asset depreciation schedule
            dataIndex:'assetdepreciationschedule',
            hidden:this.isMaintenanceScheduler,
            pdfwidth:120,
            width:120,
            renderer:WtfGlobal.deletedRenderer,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.manuallydisposaldate"),//'Disposal Date',
            dataIndex:'disposalDate',
            align:'center',
            width:120,
            pdfwidth:120,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            hidden:!this.isDisposedAssetReport
        },{
            header: WtfGlobal.getLocaleText("acc.masterConfig.24"), // "Depreciation posted/not posted"
            dataIndex: 'status',
            hidden: this.isMaintenanceScheduler,
            pdfwidth: 120,
            width: 120,
            renderer: WtfGlobal.deletedRenderer,
            align: 'center'
        }, {
            header:WtfGlobal.getLocaleText("acc.field.Depreciationpostednotposted"),// "Depreciation posted/not posted"
            dataIndex:'isdepreciationposted',
            hidden:(this.isMaintenanceScheduler || this.isDisposedAssetReport),
            pdfwidth:120,
            width:120,
            renderer:WtfGlobal.deletedRenderer,
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.field.GSTCodes"),
            dataIndex:'gstcode',
            renderer:WtfGlobal.deletedRenderer,
            width:120,
            pdfwidth:120,
            hidden:this.isDisposedAssetReport,
            align:'center'
        });

//        FixedAssetDetailArr = WtfGlobal.appendCustomColumn(FixedAssetDetailArr, GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId], undefined, undefined, this.readOnly);
        FixedAssetDetailArr = WtfGlobal.appendCustomColumn(FixedAssetDetailArr,GlobalColumnModelForReports[Wtf.Acc_FixedAssets_Details_ModuleId],true);
        FixedAssetDetailArr = WtfGlobal.appendCustomColumn(FixedAssetDetailArr,GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId],true,undefined,undefined,undefined,Wtf.Acc_FixedAssets_Details_ModuleId);
        var CustomtotalStoreCount = 0;
        var CustomloadedStoreCount = 0;

        for (var j = 0; j < FixedAssetDetailArr.length; j++) {
            if (FixedAssetDetailArr[j].dataIndex.indexOf('Custom_') != -1 && (FixedAssetDetailArr[j].fieldtype === 4 || FixedAssetDetailArr[j].fieldtype === 7)) {
                CustomtotalStoreCount++;
                FixedAssetDetailArr[j].editor.store.on('load', function() {
                    CustomloadedStoreCount++;
                    if (CustomtotalStoreCount === CustomloadedStoreCount && this.grid != undefined) {
                        this.grid.getView().refresh();
                    }
                }, this)
            }
        }
        this.colModel = new Wtf.grid.ColumnModel(FixedAssetDetailArr);
        
        this.grid = new Wtf.grid.GridPanel({
            stripeRows :true,
            store:this.gridStore,
            cm:this.colModel,
            sm:this.sm,
//            loadMask:true,
            border:false,
            layout:'fit',
            tbar:this.toolbarArr,
            view: new Wtf.grid.GroupingView({
                forceFit:false,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec') + "<br>" + WtfGlobal.getLocaleText('acc.common.norec.click.fetchbtn')))
            }),
            plugins :[Wtf.ux.grid.plugins.GroupCheckboxSelection]
        //            loadMask : true  comented because shown two times
        });      
        
        this.grid.on('cellclick',this.onCellClick, this);
        
        this.grid.on("render",function(){
            this.grid.getView().applyEmptyText(); 
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.grid.on('statesave', this.saveMyStateHandler, this);
            }, this);
        },this);
        
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        
        if(this.isDisposedAssetReport || this.isFixedAssetDetailReport){
            this.toolbarArr.push(this.AdvanceSearchBtn);
            this.toolbarArr.push("-",'<span class="highlightrecordsred" style="margin: 0px 10px;">&nbsp;&nbsp;&nbsp;&nbsp;</span><span id="wtf-gen1092">'+WtfGlobal.getLocaleText("erp.depriciationhadposted")+'</span></span>');
        }
        
        var moduleidArr = this.moduleid+','+Wtf.Acc_FixedAssets_AssetsGroups_ModuleId;
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
//            moduleid: this.moduleid,
            moduleidarray: moduleidArr.split(','),
            advSearch: false,
            ignoreDefaultFields: true,
            linelevelfields: true,
            reportid: this.isFixedAssetDetailReport ? Wtf.autoNum.FixedAssetReport : (this.isDisposedAssetReport ? Wtf.autoNum.DisposedAssetReport : Wtf.autoNum.FixedAssetReport)
        });
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        
        this.expButton=new Wtf.exportButton({
            obj:this,
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.agedPay.exportTT"),  //'Export report details',
            disabled :true,
            menuItem:{
                csv:true,
                pdf:true,
                rowPdf:false,
                xls:true
            },
            get:this.isDisposedAssetReport ? Wtf.autoNum.DisposedAssetsReport : 225
        })

        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            label:"Print",
            menuItem:{
                print:true
            },
            get:this.isDisposedAssetReport ? Wtf.autoNum.DisposedAssetsReport : 225
        });
    
        this.deleteButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),//'Delete',
            scope:this,
            disabled:true,
            hidden:!this.isMaintenanceSchedule,           //ERP-12642
            iconCls :getButtonIconCls(Wtf.etype.menudelete), 
            tooltip:WtfGlobal.getLocaleText("acc.fixed.asset.delete.selected"),//'Delete to selected record',
            handler:this.deleteHandler.createDelegate(this)
        });
        
        this.grid.getSelectionModel().on('selectionchange',this.enableDisableButtons,this);
        this.btnArr=[];       
        this.tbarArr=[];
        if((!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.deleteamaint)&&this.isMaintenanceScheduler)){
            this.btnArr.push(this.deleteButton);
        }
        this.MaintenanceScheduler = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.create.maintenance.schedulerbutt"),//Maintenance Scheduler
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.create.maintenance.schedulerbuttTT"),//View Maintenance Scheduler
            iconCls :getButtonIconCls(Wtf.etype.copy),
            hidden:this.isUpdateSchedules,
            handler:this.MaintenanceScheduler.createDelegate(this)
        });
                
        this.viewMaintenanceScheduler = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.asset.UpdateSchedules"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.asset.UpdateAssetMaintenanceSchedules"),
            iconCls:"accountingbase depreciation",
            hidden:this.isCreateSchedule,
            handler:this.viewMaintenanceSchedule.createDelegate(this)
        });

        this.postOpeningDepreciation = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.PostOpeningDepreciatio"),
            tooltip: WtfGlobal.getLocaleText("acc.field.PostOpeningDepreciatio"),
            scope: this,
            iconCls:getButtonIconCls(Wtf.etype.activate),
            width: 10,
            disabled: true,
            handler: this.postOpeningDepreciation
        });

        this.unPostOpeningDepreciation = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.UnpostOpeningDepreciation"),
            tooltip: WtfGlobal.getLocaleText("acc.field.UnpostOpeningDepreciation"),
            scope: this,
            disabled: true,
            iconCls:getButtonIconCls(Wtf.etype.deactivate),
            handler:this.unPostOpeningDepreciationForAssets
        });
        
        this.disposeAsset = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.DiposeAsset"),
            tooltip: WtfGlobal.getLocaleText("acc.field.DiposeAsset"),
            scope: this,
            disabled: true,
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.assetDisposalHandler
        });
        
        this.revertDisposedAsset = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.fixed.asset.RevertDisposedAsset"),
            tooltip: WtfGlobal.getLocaleText("acc.fixed.asset.RevertDisposedAsset")+"<br>"+WtfGlobal.getLocaleText("acc.fixed.asset.reversedisposedasset"),
            scope: this,
            disabled: true,
            iconCls:"accountingbase depreciation",
            handler:this.revertDisposedAssetHandler
        });
        
        this.deleteDisposedAsset = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.deletedisposedasset"),
            tooltip: WtfGlobal.getLocaleText("acc.field.deletedisposedasset")+"<br>"+WtfGlobal.getLocaleText("acc.fixed.asset.deletedisposedasset"),
            scope: this,
            disabled: true,
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.deleteDisposedAssetlHandler
        });
                
        this.transferAsset = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.transaferAsset"),
            tooltip: WtfGlobal.getLocaleText("acc.field.transaferAsset"),
            scope: this,
            disabled: true,
            iconCls:getButtonIconCls(Wtf.etype.edit),
            handler:this.assetTransferHandler
        });

        this.btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.field.QuickSearchAssetIdDesc"),  //'Quick Search by Asset Id, Description',
            id:"quickSearch"+this.id,
            width: 150
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
        
        if(this.isDisposedAssetReport || this.isFixedAssetDetailReport){
            this.btnArr.push('-',WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate);
        }
        
        this.btnArr.push("-",{
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
            tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
            iconCls:'accountingbase fetch',
            scope:this,
            handler:function(){
                if(!this.checkDates()){
                    return;
                }
                
                this.loadGridStore();
            }
        });
        
        if(this.isMaintenanceScheduler){// 
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.createamaint)){
                this.btnArr.push(this.MaintenanceScheduler);
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.updateams)){
                this.btnArr.push(this.viewMaintenanceScheduler);
            }
        }if(this.isDisposedAssetReport){
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.exportareport)){
                this.tbarArr.push(this.expButton);
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.printareport)){
                this.tbarArr.push(this.printButton);
            }
            this.btnArr.push(this.revertDisposedAsset);
            this.btnArr.push(this.deleteDisposedAsset);
            this.btnArr.push("->","&nbsp;View",this.typeEditor);
        } else {
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.exportareport)){
                this.tbarArr.push(this.expButton);   //ERP-9958
            }
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.printareport)){
                this.tbarArr.push(this.printButton);
            }
            if (!this.isMaintenanceScheduler) {
                this.manageOpeningDepreciation = [];
                // Add button to post opening Depreciation
                if (!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.genfadep)) {
                    this.manageOpeningDepreciation.push(this.postOpeningDepreciation);
                }
                // Add button to unpost opening Depreciation
                if (!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.unpostdep)) {
                    this.manageOpeningDepreciation.push(this.unPostOpeningDepreciation);
                }
                if (this.manageOpeningDepreciation.length > 0) {
                    this.btnArr.push({
                        text: WtfGlobal.getLocaleText("acc.fixed.asset.manageopeningdepreciation"),
                        tooltip: WtfGlobal.getLocaleText("acc.fixed.asset.manageopeningdepreciation"),
                        iconCls: getButtonIconCls(Wtf.etype.activatedeactivate),
                        menu: this.manageOpeningDepreciation
                    });
                }
            this.btnArr.push(this.disposeAsset);
            this.btnArr.push(this.transferAsset);
            }
          
           this.btnArr.push("->","&nbsp;View",this.typeEditor);
        }
    },
    
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    getMyConfig: function () {
        WtfGlobal.getGridConfig(this.grid, this.moduleid, false, true, false);
    },
    saveMyStateHandler: function (grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    }, 
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    updateStoreConfig: function (data, store) {
        for (var fieldcnt = 0; fieldcnt < data.length; fieldcnt++) {
            var fieldname = data[fieldcnt].dataIndex;
            var newField = new Wtf.data.Field({
                name: fieldname,
                type: 'auto'
            });
            store.fields.items.push(newField);
            store.fields.map[fieldname] = newField;
            store.fields.keys.push(fieldname);
        }
        store.reader = new Wtf.data.KwlJsonReader(store.reader.meta, store.fields.items);
    },
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.gridStore.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: filterConjuctionCriteria,
            stdate:this.sdate,
            enddate:this.edate,
            type:this.typeEditor.getValue(),
            assetGroupIds : this.assetGroupCombo != undefined ? this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue() : "All",
            isDisposedAssetReport : this.isDisposedAssetReport,
            isFixedAssetDetailReport:this.isFixedAssetDetailReport,
            depreciationCalculationType: this.depreciationCalculationType,
            isCreateSchedule : this.isCreateSchedule
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
            stdate:this.sdate,
            enddate:this.edate,
            type:this.typeEditor.getValue(),
            assetGroupIds : this.assetGroupCombo != undefined ? this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue() : "All",
            isDisposedAssetReport : this.isDisposedAssetReport,
            isFixedAssetDetailReport:this.isFixedAssetDetailReport,
            depreciationCalculationType: this.depreciationCalculationType,
            isCreateSchedule : this.isCreateSchedule
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
    },
       
    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
    checkDates: function(){
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();

        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return false;
        }

        this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return false;
        }
        return true;
    },
    
    typeEditorOnSelect: function(){
        this.loadGridStore();
    },
    
    loadGridStore: function(){
        this.gridStore.load({
            params:{
                start:0,
                limit:this.pP.combo!==undefined?this.pP.combo.value:30,
                stdate:this.sdate,
                enddate:this.edate,
                type:this.typeEditor.getValue(),
                ss : this.quickPanelSearch != undefined ? this.quickPanelSearch.getValue() : "",
                assetGroupIds : this.assetGroupCombo != undefined ? this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue() : "All",
                isDisposedAssetReport : this.isDisposedAssetReport,
                isFixedAssetDetailReport: this.isFixedAssetDetailReport,
                depreciationCalculationType: this.depreciationCalculationType
            }
        });  
    },
    
    deleteHandler:function(){   
        var selectedRecordArray = this.grid.getSelectionModel().getSelections();
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }

        this.assetId=[];
        this.arrRec = this.sm.getSelections();
        for(var i=0;i<selectedRecordArray.length;i++){
            this.assetId.push(this.arrRec[i].data['assetdetailId']);
        }
        WtfGlobal.highLightRowColor(this.grid,this.arrRec,true,0,2);
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.warning"),
            msg:'Are you sure you want to delete the selected Asset Details',// WtfGlobal.getLocaleText("erp.fixedasseAre you sure you want to delete the selected Asset Groupt.deleteconfirmmsg"), //Are you sure you want to delete the selected Asset Group
            width: 560,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn!="ok"){
                    var num= (this.gridStore.indexOf(this.arrRec[0]))%2;
                    WtfGlobal.highLightRowColor(this.grid,this.arrRec,false,num,2);
                    return;
                }
                else {
                    this.deletionRequest();
                }
            }
        });
    },
    
    deletionRequest:function(){
        Wtf.Ajax.requestEx({
            url: "ACCProduct/deleteAssetDetails.do",         
            params: {
                assetId:this.assetId
            }   
        },this,this.genSuccessResponseAsset,this.genFailureResponseAsset);
    },
    
    genSuccessResponseAsset : function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
            var ind=this.gridStore.indexOf(this.arrRec[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }
        if(response.success){
            WtfComMsgBox([WtfGlobal.getLocaleText("erp.fixedasset.fixedassetgroup"),response.msg],response.success*2+1);
            if(response.success){
                this.gridStore.reload();                
            }
        }else{
            if(response.isused){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg]);
            }

        }          
    },
    
    genFailureResponseAsset : function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.arrRec.length;i++){
            var ind=this.gridStore.indexOf(this.arrRec[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.arrRec[i],false,num,2,true);
        }
        var msg=  WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
        
    enableDisableButtons : function(){
        var recArray=this.grid.getSelectionModel().getSelections();
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
           
            if(this.MaintenanceScheduler)
                this.MaintenanceScheduler.disable();
            
            if(this.viewMaintenanceScheduler)
                this.viewMaintenanceScheduler.disable();           
            
            return;
        }else{
            var rec = recArray[0];
            if(this.deleteButton)
            this.deleteButton.enable();
            if(rec.get('deleted') || !rec.get('isDepreciable')){
                
                if(this.MaintenanceScheduler)
                    this.MaintenanceScheduler.disable();
                
                if(this.viewMaintenanceScheduler)
                    this.viewMaintenanceScheduler.disable();
            }else{
                
                if(this.MaintenanceScheduler)
                    this.MaintenanceScheduler.enable();
                
                if(this.viewMaintenanceScheduler)
                    this.viewMaintenanceScheduler.enable();                
            }
        }
    },
    
    emptyDateDeletedRenderer: function(v,m,rec) {
        var val = "N/A";
        if(v != null) {
            val = WtfGlobal.onlyDateDeletedRenderer(v,m,rec)
         } else {
             if(rec.data.deleted)
                val = '<del>'+"N/A"+'</del>';
            else
            val="N/A"
        }
        return val;  
    },
    
    emptyeDeletedRenderer: function(v,m,rec) {
        var val = "";
        if(v!="") {
            val = WtfGlobal.deletedRenderer(v,m,rec)
         } else {
             if(rec.data.deleted)
                val = '<del>'+" "+'</del>';
            else
            val=" "
        }
        return val;      
    },
       
    MaintenanceScheduler:function(){  
        var recArray=this.grid.getSelectionModel().getSelections();
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        
        var rec = recArray[0];        
        var gridId = rec.get('assetId');
        
        this.assetMaintenanceScheduleGrid = new Wtf.account.MaintenanceSchedulers({
            resizable:false,
            id:gridId+this.id,
            assetRec:rec
        });
        
        this.assetMaintenanceScheduleGrid.show();
    },    
    
    viewMaintenanceSchedule:function(){        
        var recArray=this.grid.getSelectionModel().getSelections();
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        
        var rec = recArray[0];        
        var id = rec.get('assetdetailId')+this.id;        
        callAssetMaintenanceScheduler(id,rec);        
    },

    postOpeningDepreciation: function () {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),(WtfGlobal.getLocaleText("acc.asset.confirmDepreciation")+WtfGlobal.getLocaleText("acc.field.PostOpeningDepreciatio")+"?"),function (btn)
        {
            if (btn == 'yes') {
                this.postOpeningDepreciation.disable();
                this.assetDetailIds = "";
                this.arrRec = this.sm.getSelections();

                for (var i = 0; i < this.arrRec.length; i++) {
                    /*if depreciation is already posted by asset then skip that asset id */
                    if (this.arrRec[i] && this.arrRec[i].data.isdepreciationposted == "Yes") {
                        continue;
                    }
                    this.assetDetailIds += "'" + this.arrRec[i].data['assetdetailId'] + "',";
                }
                if (CompanyPreferenceChecks.highlightDepreciatedAssets()) {
                    WtfGlobal.highLightRowColor(this.grid, this.arrRec, true, 0, 2);
                }
                WtfGlobal.setAjaxTimeOut();
                Wtf.Ajax.requestEx({
                    url: "ACCProductCMN/postOpeningDepreciationForAssets.do",
                    params: {
                        depreciationCalculationType: Wtf.account.companyAccountPref.depreciationCalculationType,
                        assetDetailIds: this.assetDetailIds
                    }
                }, this, function (req, res) {
                    this.postOpeningDepreciation.enable();
                    WtfGlobal.resetAjaxTimeOut();
                    var restext = req;
                    if (restext.success) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), restext.msg], 3);
                        getCompanyAccPref();
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), restext.msg], 1);
                    }
                    this.gridStore.load();
                }, function (response) {
                    this.postOpeningDepreciation.enable();
                    var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
                    if (response.msg) {
                        msg = response.msg;
                    }
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                });

            }
            else if (btn == 'no') 
            {
                return;
            }
        },this);

    },

    unPostOpeningDepreciationForAssets: function()
    {
   Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), (WtfGlobal.getLocaleText("acc.asset.confirmDepreciation")+WtfGlobal.getLocaleText("acc.fixedAssetList.UnpostDep")+"?"), function (btn)
        {
        if(btn=='yes')
        {
           // alert("Unpost");           
        this.unPostOpeningDepreciation.disable();
        this.assetDetailIds = "";
        this.arrRec = this.sm.getSelections();
        var isDisposed = false;
        for (var i = 0; i < this.arrRec.length; i++) 
            {
            if (this.arrRec[i].data.status == 'Manually Disposed' || this.arrRec[i].data.status == 'Disposed') 
            {
                isDisposed = true;
                break;
            }
            this.assetDetailIds += "'" + this.arrRec[i].data['assetdetailId'] + "',";
        }
        if (isDisposed) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.fixed.asset.unpost.alert")], 2);
            return;
        }
        if(CompanyPreferenceChecks.highlightDepreciatedAssets()){
            WtfGlobal.highLightRowColor(this.grid,this.arrRec,true,0,2);
        }
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url : "ACCProductCMN/unPostOpeningDepreciationForAssets.do",
            params:{
                depreciationCalculationType: Wtf.account.companyAccountPref.depreciationCalculationType,
                assetDetailIds: this.assetDetailIds
            }
        },this,function(req,res){
            this.unPostOpeningDepreciation.enable();
            var restext=req;
            if(restext.success){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),restext.msg],3);
                getCompanyAccPref();
            } else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);
            }
            this.gridStore.load();
        },function(response){
            this.unPostOpeningDepreciation.enable();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });    
    }
   else if(btn=='no')
        {
            return;
        }
    },this);
},
     
     // Function to handle the Asset Disposal, Firstly send request to pop-up disposal details by sending the disposal Date
    assetDisposalHandler:function(){
        this.assetDetailIds = "";
        this.arrRec = this.sm.getSelections();
        
        if(!this.grid.getSelectionModel().hasSelection()){ // If no records are selected 
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.178")],2);
            return;
        }else if(this.arrRec.length > 1){ // If multiple records are selected
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.selectsingleassetwarn")],2);
            return;            
        }
        
        for(var i=0;i < this.arrRec.length;i++){
            this.assetDetailIds += "'"+ this.arrRec[i].data['assetdetailId']+"',";
        }

        this.disposeDateSettings = new Wtf.account.DiposalDateSettings({
            title:WtfGlobal.getLocaleText("acc.field.selectDisposalDate"),
            layout:'border',
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            height:120,
            assetdetailIds:this.assetDetailIds,
            width:320
        });
        this.disposeDateSettings.show();   
    },        
  
    revertDisposedAssetHandler:function(){
        this.assetDetailIds = "";
        this.arrRec = this.sm.getSelections();
        
        if(!this.grid.getSelectionModel().hasSelection()){ // If no records are selected 
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.178")],2);
            return;
        }else if(this.arrRec.length > 1){ // If multiple records are selected
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.selectsingleassetwarn")],2);
            return;            
        }
        
        for(var i=0;i < this.arrRec.length;i++){
            this.assetDetailIds += "'"+ this.arrRec[i].data['assetdetailId']+"',";
        }
        
        this.revertDateSettings = new Wtf.account.DiposalDateSettings({
            title:WtfGlobal.getLocaleText("acc.fixed.asset.revertDate"),
                layout:'border',
                resizable:false,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                modal:true,
            height:120,
            assetdetailIds:this.assetDetailIds,
            width:320,
            isRevertDisposedAsset: true,
            disposaldate: this.arrRec[0].data['disposalDate']
            });
        this.revertDateSettings.show();   
    },

    deleteDisposedAssetlHandler:function(){
        Wtf.MessageBox.show({
            title:WtfGlobal.getLocaleText("acc.common.confirm"),
            msg:WtfGlobal.getLocaleText("acc.fixed.asset.deletedisposedasset.confirmmsg"),
            width:500,
            buttons: Wtf.MessageBox.YESNO,
            scope:this,
            icon: Wtf.MessageBox.INFO,
            fn: function(btn){
                if(btn =="yes") {
                    this.deleteDisposedAsset.disable();

        this.assetDetailIds = "";
        this.arrRec = this.sm.getSelections();
        
        if(!this.grid.getSelectionModel().hasSelection()){ // If no records are selected 
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.178")],2);
            return;
        }else if(this.arrRec.length > 1){ // If multiple records are selected
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.selectsingleassetwarn")],2);
            return;            
        }
        
        for(var i=0;i < this.arrRec.length;i++){
            this.assetDetailIds += "'"+ this.arrRec[i].data['assetdetailId']+"',";
        }
        
        var rec=[];
                    rec.assetDetailIds=this.assetDetailIds;
        Wtf.Ajax.timeout = WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
                        url: "ACCProductCMN/deleteDisposedAsset.do",
            params: rec
                    },this,this.genDeleteDisposedAssetSuccessResponse,this.genDeleteDisposedAssetFailureResponse);
                }else{
                    return;
                }
            }
        }, this);                
    }, 

    genDeleteDisposedAssetSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            Wtf.getCmp('disposedassetspanel').gridStore.reload();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
            return;
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        }
    },

    genDeleteDisposedAssetFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    // Function to handle Asset Transfer request
    assetTransferHandler:function(){
//        this.transferAsset.disable(); 
        this.assetDetailIds = "";
        this.arrRec = this.sm.getSelections();
        
        if(!this.grid.getSelectionModel().hasSelection()){ // If no records are selected 
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.178")],2);
            return;
        }else if(this.arrRec.length > 1){ // If multiple records are selected
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.selectsingleassetwarn")],2);
            return;            
        }
        
        this.isWarehouse=false;
        this.isLocation=false;
        for(var i=0;i < this.arrRec.length;i++){
            this.assetDetailIds += "'"+ this.arrRec[i].data['assetdetailId']+"',";
            this.isWarehouse = this.arrRec[i].data['isWarehouseForProduct'];
            this.isLocation = this.arrRec[i].data['isLocationForProduct'];
        }
        
        var rec=[];
        rec.assetDetailIds = this.assetDetailIds;
        Wtf.Ajax.timeout = WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url: "ACCProduct/getAssetTransferDetails.do",
            params: rec
        },this,this.genTransferSuccessResponse,this.genTransferFailureResponse);
    }, 

    genTransferSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            this.transferDetails = new Wtf.account.AssetTransferDetails({
                title:WtfGlobal.getLocaleText("acc.field.Assettransferdetails"),
                layout:'border',
                resizable:false,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                modal:true,
                height:600,
                assetdetailIds:this.assetdetailIds,
                assetDetailsArray:response.data,
                assetTransferHistoryArray:response.historydata,
                width:900,
                isWarehouse: this.isWarehouse,
                isLocation: this.isLocation
            });
            this.transferDetails.show(); 
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],1);
        }
    },

    genTransferFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.transferAsset.enable();
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    enableDisablePostUnpostButtons: function(){
        if (!this.isMaintenanceScheduler) {
            var arr = this.sm.getSelections();
            if (arr.length == 0) {     // If no records are selected then keep post/unpost buttons disabled       
                this.postOpeningDepreciation.disable();
                this.unPostOpeningDepreciation.disable();
                this.disposeAsset.disable();
                this.revertDisposedAsset.disable();
                this.deleteDisposedAsset.disable();
                this.transferAsset.disable();
            }
            
            // Allow Asset Disposal only for the Single Asset
            if(arr.length ==1){
                this.disposeAsset.enable();
                this.transferAsset.enable();
                if(arr[0].data.status=='Manually Disposed'){
                    this.revertDisposedAsset.enable();
                    this.deleteDisposedAsset.enable();
                }else{
                    this.revertDisposedAsset.disable();
                    this.deleteDisposedAsset.disable();
                }
            }else{
                this.disposeAsset.disable();
                this.revertDisposedAsset.disable();
                this.deleteDisposedAsset.disable();
                this.transferAsset.disable();
            }
            var onlyPosted=false;
            var onlyUnPosted=false;
            for (var i = 0; i < arr.length; i++) {
                /*Code for Enable/Disable post/unpost opening buttons*/
                if (arr[i].data.isdepreciationposted == "Yes") {
                    onlyPosted = true;
                } else if (arr[i].data.isdepreciationposted == "No") {
                    onlyUnPosted = true;
                }
                }
            if (onlyPosted && onlyUnPosted) {
                this.postOpeningDepreciation.enable();
                this.unPostOpeningDepreciation.enable();
            } else if (onlyPosted) {
                this.postOpeningDepreciation.disable();
                this.unPostOpeningDepreciation.enable();
            } else if (onlyUnPosted) {
                this.postOpeningDepreciation.enable();
                this.unPostOpeningDepreciation.disable();
            } else {
                this.postOpeningDepreciation.disable();
                this.unPostOpeningDepreciation.disable();

            }
        }
        
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
    
    onCellClick:function(g,i,j,e){
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="disposaljeno"){
            var accid = this.gridStore.getAt(i).data['disposaljeid'];
            Wtf.onCellClick(accid,this.gridStore.getAt(i).data['disposalDate'],this.gridStore.getAt(i).data['disposalDate']);
        }
    }
})



// Asset Disposal Components

//Componenet to take Disposal date
Wtf.account.DiposalDateSettings = function(config){
    this.assetdetailIds = config.assetdetailIds;
    this.isRevertDisposedAsset = (config.isRevertDisposedAsset) ? config.isRevertDisposedAsset : false;
    this.disposaldate = (config.disposaldate!=undefined && config.disposaldate!='') ? config.disposaldate : '';
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.submit"), // Submit
            minWidth: 50,
            scope: this,
            handler: this.submitForm.createDelegate(this)
        }),this.closeButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), // Cancel
            minWidth: 50,
            scope: this,
            handler: this.closeOpenWin.createDelegate(this)
        })]
    },config);
    
    Wtf.account.DiposalDateSettings.superclass.constructor.call(this, config); 
}

Wtf.extend(Wtf.account.DiposalDateSettings, Wtf.Window,{
    
    onRender:function(config){
        Wtf.account.DiposalDateSettings.superclass.onRender.call(this,config);              
        this.createDiposalDateSettings();    
        this.add({
            region: 'center',
            border: false,
            layout:'fit',
            baseCls:'bckgroundcolor',
            items:[this.disposalDateSettingsForm]
        });
    },
  
    createDiposalDateSettings:function(){        
        this.disposalDate = new Wtf.form.DateField({
            fieldLabel: this.isRevertDisposedAsset ? WtfGlobal.getLocaleText("acc.fixed.asset.revertDate") : WtfGlobal.getLocaleText("acc.field.selectDisposalDate"),//'Slect Disposal Date',
            name: 'disposaldate',
            id: "disposaldate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:false,
            minValue: this.isRevertDisposedAsset ? this.disposaldate : ''
        });
        if(this.isRevertDisposedAsset && this.disposaldate!=''){
            this.disposalDate.setValue(this.disposaldate);//Set asset disposal date
        }else{
            this.disposalDate.setValue(new Date());
        }
        
        this.disposalDateSettingsForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            autoHeight:true,
            anchor:'100%',
            labelWidth:150,
            bodyStyle:'margin:10px',
            items:[this.disposalDate]
        });        
    },    
  
    closeOpenWin:function(){
        this.close();
    },
    
    submitForm:function(){
        this.saveButton.disable();
        var rec=[];
        rec.assetDetailIds=this.assetdetailIds;
        if(this.disposalDate.getValue() == undefined || this.disposalDate.getValue() == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.DateWarnMsg")],2);
            this.saveButton.enable();
            return; 
        }
        rec.disposaldate=WtfGlobal.convertToGenericDate(this.disposalDate.getValue());
        Wtf.Ajax.timeout = WtfGlobal.setAjaxTimeOut();
        if(this.isRevertDisposedAsset){
            Wtf.Ajax.requestEx({
                url: "ACCProductCMN/revertDisposedAsset.do",
                params: rec
            },this,this.genRevertSuccessResponse,this.genDisposeFailureResponse);
        }else{
            Wtf.Ajax.requestEx({
                url: "ACCProductCMN/getAssetDisposalDetails.do",
                params: rec
            },this,this.genDisposeSuccessResponse,this.genDisposeFailureResponse);
        }
    },
          
    genDisposeSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            this.close();
            this.DisposalDetails = new Wtf.account.DisposalDetails({
                title:WtfGlobal.getLocaleText("acc.field.Disposaldetails"),
                layout:'border',
                resizable:false,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                modal:true,
                height:335,
                assetdetailIds:this.assetdetailIds,
                record:response.data[0],
                width:900
            });
            this.DisposalDetails.show(); 
        }else{
            this.close();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        }
    },
          
    genRevertSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            Wtf.getCmp('disposedassetspanel').gridStore.reload();
            this.close();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
            return;
        }else{
            this.close();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        }
    },

    genDisposeFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
})

//Componenet to show disposal Details
Wtf.account.DisposalDetails = function(config){
    this.assetdetailIds = config.assetdetailIds;
    this.record = config.record;
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.DiposeAsset"), // Submit
            minWidth: 50,
            scope: this,
            handler: this.submitForm.createDelegate(this)
        }),this.closeButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), // Cancel
            minWidth: 50,
            scope: this,
            handler: this.closeOpenWin.createDelegate(this)
        })]
    },config);
    
    Wtf.account.DisposalDetails.superclass.constructor.call(this, config); 
}

Wtf.extend(Wtf.account.DisposalDetails, Wtf.Window,{
    
    onRender:function(config){
        Wtf.account.DisposalDetails.superclass.onRender.call(this,config);              
                
        //============================================= For Disposal  Details Left Data View =======================================================
        
        var installationDate = '';
        if(this.record.installationDate!=undefined && this.record.installationDate!=''){
            installationDate = new Date(this.record.installationDate).format(WtfGlobal.getOnlyDateFormat());
        }
        var depreciationUptoDisposal = parseFloat(getRoundedAmountValue(0)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
        if(this.record.depreciationUptoDisposal!=undefined && this.record.depreciationUptoDisposal!=''){
            depreciationUptoDisposal = parseFloat(getRoundedAmountValue(this.record.depreciationUptoDisposal)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
        }

        this.disposalLeftStore = new Wtf.data.SimpleStore({
            fields: ['assetId','assetLifeInMonth','cost','installationDate','depreciationUptoDisposal'],
            data : [
            [this.record.assetId, this.record.assetLifeInMonth, this.record.cost, installationDate, depreciationUptoDisposal]
            ]          
        });

        var tpl = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:3%;">',
            '<tpl for=".">', 
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.fixed.asset.id")+' : </td><td class="leadDetailTD">{assetId}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.field.AssetLifeMonth")+' : </td><td class="leadDetailTD">{assetLifeInMonth}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.fixedAssetList.grid.assetName")+' : </td><td class="leadDetailTD">{cost}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.fixed.asset.date.installation")+' : </td><td class="leadDetailTD">{installationDate}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.depposteduptodisposal")+' : </td><td class="leadDetailTD">{depreciationUptoDisposal}</td></tr>',
            '</tpl></tpl></table>'
            );
        
        //============================================= For Disposal Details Right Data View =======================================================            
        var disposalDate = '';
        if(this.record.disposalDate!=undefined && this.record.disposalDate!=''){
            disposalDate = new Date(this.record.disposalDate).format(WtfGlobal.getOnlyDateFormat());
        }
        var profitloss = parseFloat(getRoundedAmountValue(0)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
        if(this.record.profitloss!=undefined && this.record.profitloss!=''){
            profitloss = parseFloat(getRoundedAmountValue(this.record.profitloss)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
        }
        
        this.disposalRightStore = new Wtf.data.SimpleStore({
            fields: ['assetLife','remainingLifeInMonth','disposalDate','profitloss'],
            data : [
            [this.record.assetLife,this.record.remainingLifeInMonth,disposalDate,profitloss]
            ]          
        });
        
        var tpl2 = new Wtf.XTemplate(
            '<table border="0" width="100%" style="padding-left:3%;padding-top:3%;">',
            '<tpl for=".">',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.fixed.asset.life")+' : </td><td class="leadDetailTD"> {assetLife}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.field.Assetremainlifemonth")+' : </td><td class="leadDetailTD">{remainingLifeInMonth}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+(this.record.iswriteoffaccount?WtfGlobal.getLocaleText("acc.writeoffondispose"):WtfGlobal.getLocaleText("acc.profitlossondispose"))+' : </td><td class="leadDetailTD">{profitloss}</td></tr>',
            '<tr><td class="leadDetailTD" style="color:#15428B;">'+WtfGlobal.getLocaleText("acc.field.DisposalDate")+' : </td><td class="leadDetailTD">{disposalDate}</td></tr>',
            '</tpl></table>'
            );
        
        //=============================================== For Defining All Data View ============================================================

        this.disposalLeftDataView = new Wtf.DataView({
            store:this.disposalLeftStore,
            tpl: tpl,
            autoHeight:true,
            multiSelect: true,
            autowidth:true,
            columnWidth:.50,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No Data to display'
        })
            
        this.disposalRightDataView = new Wtf.DataView({
            store:this.disposalRightStore,
            tpl: tpl2,
            autoHeight:true,
            autowidth:true,
            multiSelect: true,
            columnWidth:.50,
            overClass:'x-view-over',
            itemSelector:'div.thumb-wrap',
            emptyText: 'No Data to display'
        })          
       
        //=============================================== For Defining All Field Sets ============================================================     
        
        this.fs1=new Wtf.form.FieldSet({
            width:850,
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.field.Disposaldetails"), // 'Disposal Details',
            layout:'column',
            items:[
            this.disposalLeftDataView,
            this.disposalRightDataView
            ]
        });
        //=============================================== For Adding All Data View ============================================================       
        this.add({
            region: 'center',
            layout:"table",
            baseCls:'bckgroundcolor',
            layoutConfig: {
                columns: 1
            },
            autoWidth:true,
            autoScroll:true,
            bodyStyle:'overflow-y: scroll',
            items:[
            {
                colspan: 1,
                autoWidth:true,
                autoScroll:true,
                border:false,
                items:this.fs1,
                bodyStyle:"margin-left:20px;margin-right:20px;margin-top:30px;margin-bottom:20px;"
            }]
        });
    },
      
    submitForm:function(){
        this.saveButton.disable();
        this.record.assetDetailIds = this.assetdetailIds;
        Wtf.Ajax.timeout = WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url: "ACCProductCMN/saveAssetDisposalDetails.do",
            params: this.record
        },this,this.genDisposeSuccessResponse,this.genDisposeFailureResponse);
    },
          
    genDisposeSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.DiposeAsset"),response.msg],response.success*2+1);
        if(response.success){
            this.close();
        }else{
            this.close();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],1);
        }
    },

    genDisposeFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    closeOpenWin:function(){
        this.close();
    }
})


// Fixed Asset Transfer Component 

Wtf.account.AssetTransferDetails = function(config){
    this.record = config.record;
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
            minWidth: 50,
            scope: this,
            hidden:this.readOnly,
            handler: this.saveData.createDelegate(this)
        }),this.closeButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
            minWidth: 50,
            scope: this,
            handler: this.closeFADetails.createDelegate(this)
        })]
    },config);
    
    Wtf.account.AssetTransferDetails.superclass.constructor.call(this, config);
    
}

Wtf.extend(Wtf.account.AssetTransferDetails, Wtf.Window,{
    onRender:function(config){
        Wtf.account.FADetails.superclass.onRender.call(this,config);
        
        if(this.lineRec){
            this.selectedCurrencySymbol = this.lineRec.data['currencysymbol'];
        }
                
        // create Fixed Asset Details Grid
        
        this.createFixedAssetDetails();
        
        // add record at here only if asset combo store is not loading else it will be add on is load event
        if(!(this.isCustomer || ((this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder))))){
            this.addGridRec();
        }
                 
        this.add({
            region: 'center',
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            items:[this.FATransferGrid]
        });
        
        this.add({
            region: 'south',
            height:400,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            items:[this.FATransferHistoryGrid]
        });

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
       
        this.detartmentEditor = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            store:Wtf.detartmentStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'detartment',
            hiddenName:'detartment'
        });
        
        this.transferDate = new Wtf.form.DateField({
            maxLength:250,
            format:WtfGlobal.getOnlyDateFormat(),
            xtype:'datefield'
        });
        
        this.rowno=new Wtf.grid.RowNumberer();
        
        var FixedAssetDetailArr = [];
        FixedAssetDetailArr.push(this.rowno,{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.id"),
            dataIndex:'assetId',
            width:150,
            renderer:(this.isCustomer || (this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder)))?Wtf.comboBoxRenderer(this.assetEditorCombo):'',
            editor:(this.readOnly)?"": this.assetIdEditor
        },{
            header: WtfGlobal.getLocaleText("acc.field.TransferDate"),
            dataIndex: 'transferDate',
            width: 100,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            disabledClass:"newtripcmbss",
            editor: this.transferDate            
        },{
            header:WtfGlobal.getLocaleText("acc.field.department"),
            dataIndex:'department',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset || !this.isShowDimension(),
            renderer:Wtf.comboBoxRenderer(this.detartmentEditor),
            editor:(this.readOnly)?"":this.detartmentEditor
        },{
            header:WtfGlobal.getLocaleText("acc.field.PersonUsingtheAsset"),
            dataIndex:'assetUser',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset,
            renderer:Wtf.comboBoxRenderer(this.users),
            editor:(this.readOnly)?"":this.users
        });
        
        // Put location and warehouse
        
        this.wareHouseRec = new Wtf.data.Record.create([
        {
            name:"id"
        },{
            name:"name"
        },{
            name: 'parentid'
        },{
            name: 'parentname'
        }
        ]);
        this.wareHouseReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.wareHouseRec);
        this.wareHouseStore = new Wtf.data.Store({
            url:"ACCMaster/getWarehouseItems.do",
            reader:this.wareHouseReader
        });
        this.wareHouseStore.load();

        this.wareHouseCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),
            valueField:'id',
            displayField:'name',
            store:this.wareHouseStore,
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            name:'warehouse',
            hiddenName:'warehouse',
            width: 250
        });

        this.wareHouseCombo.on('select',function(){
            this.locationStore.load({
                params:{
                    storeid:this.wareHouseCombo.getValue()
                }
            });
            this.locationCombo.enable();
//            this.setValuestoSelectedRecords(this.wareHouseCombo.getValue(),'warehouse');
        },this);

        this.locationRec = new Wtf.data.Record.create([
        {
            name:"id"
        },{
            name:"name"
        },{
            name: 'parentid'
        },{
            name: 'parentname'
        }
        ]);
        this.locationReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.locationRec);
        var locationStoreUrl="ACCMaster/getLocationItems.do"
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            locationStoreUrl="ACCMaster/getLocationItemsFromStore.do";
        }
        this.locationStore = new Wtf.data.Store({
            url:locationStoreUrl,
            reader:this.locationReader
        });
        this.locationStore.load();

        this.locationCombo = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            multiSelect:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.locationStore,
            typeAhead: true,
            forceSelection: true,
            hirarchical:true,
            name:'location',
            hiddenName:'location',
            width: 250
        });

        //this.locationCombo.on('select',function(){
//            this.setValuestoSelectedRecords(this.locationCombo.getValue(),'location');
        //},this);      
                
        FixedAssetDetailArr.push({
            header:"Warehouse",
            width:120,
            dataIndex:'warehouse',
            hidden: !(this.isWarehouse && this.isLocation),
            renderer:Wtf.comboBoxRenderer(this.wareHouseCombo),
            editor:this.readOnly?"":this.wareHouseCombo
        });

        FixedAssetDetailArr.push({
            header:"Location",
            width:120,
            dataIndex:'location',
            hidden: !(this.isWarehouse && this.isLocation),
            renderer:Wtf.comboBoxRenderer(this.locationCombo),
            editor:this.readOnly?"":this.locationCombo
        });
        
        // Put all Dimsnsions
        if (this.isShowDimension()) {
            FixedAssetDetailArr = WtfGlobal.appendCustomColumn(FixedAssetDetailArr, GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId], undefined, undefined, this.readOnly);
            var CustomtotalStoreCount = 0;
            var CustomloadedStoreCount = 0;

            for (var j = 0; j < FixedAssetDetailArr.length; j++) {
                if (FixedAssetDetailArr[j].dataIndex.indexOf('Custom_') != -1 && (FixedAssetDetailArr[j].fieldtype === 4 || FixedAssetDetailArr[j].fieldtype === 7)) {
                    CustomtotalStoreCount++;
                    FixedAssetDetailArr[j].editor.store.on('load', function() {
                        CustomloadedStoreCount++;
                        if (CustomtotalStoreCount === CustomloadedStoreCount && this.FATransferGrid != undefined) {
                            this.populateCustomFieldValue(this.FATransferGrid);
                            //                            this.disableCustomFieldOfGrid(this.FATransferGrid);                            
                            this.FATransferGrid.getView().refresh();
                        }
                    }, this)
                }
            }
        }
         
        // Asset Transfer History Grid          

        this.users1 = new Wtf.form.ComboBox({     
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
       
        this.detartmentEditor1 = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'name',
            store:Wtf.detartmentStore,
            anchor:'90%',
            typeAhead: true,
            forceSelection: true,
            name:'detartment',
            hiddenName:'detartment'
        });

        var FixedAssetDetailArr1 = [];
        FixedAssetDetailArr1.push(this.rowno,{
            header:WtfGlobal.getLocaleText("acc.fixed.asset.id"),
            dataIndex:'assetId',
            width:(this.isLeaseFixedAsset)?250:150,
            renderer:(this.isCustomer || (this.fromPO && !this.isCustomer && (this.isInvoice || this.isFromOrder)))?Wtf.comboBoxRenderer(this.assetEditorCombo):''
        },{
            header: WtfGlobal.getLocaleText("acc.field.TransferDate"),
            dataIndex: "transferDate",
            disabledClass:"newtripcmbss",
            disabled: true
        },{
            header:WtfGlobal.getLocaleText("acc.field.department"),
            dataIndex:'department',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset || !this.isShowDimension(),
            renderer:Wtf.comboBoxRenderer(this.detartmentEditor1)
        },{
            header:WtfGlobal.getLocaleText("acc.field.PersonUsingtheAsset"),
            dataIndex:'assetUser',
            width:100,
            align:'right',
            hidden:this.isLeaseFixedAsset,
            renderer:Wtf.comboBoxRenderer(this.users)
        });
        
        // Put location and warehouse
        
        this.wareHouseCombo1 = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),
            valueField:'id',
            displayField:'name',
            store:this.wareHouseStore,
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            name:'warehouse',
            hiddenName:'warehouse',
            width: 250
        });

        this.locationRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"name"
        },
        {
            name: 'parentid'
        },
        {
            name: 'parentname'
        }
        ]);

        this.locationCombo1 = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            multiSelect:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12"),
            valueField:'id',
            displayField:'name',
            lastQuery:'',
            store:this.locationStore,
            typeAhead: true,
            forceSelection: true,
            hirarchical:true,
            name:'location',
            hiddenName:'location',
            width: 250
        });

        FixedAssetDetailArr1.push({
            header:"Warehouse",
            width:120,
            dataIndex:'warehouse',
            hidden: !(this.isWarehouse && this.isLocation),
            renderer:Wtf.comboBoxRenderer(this.wareHouseCombo1)
        });

        FixedAssetDetailArr1.push({
            header:"Location",
            width:120,
            dataIndex:'location',
            hidden: !(this.isWarehouse && this.isLocation),
            renderer:Wtf.comboBoxRenderer(this.locationCombo1)
        });
        
        // Put all Dimsnsions
        if (this.isShowDimension()) {
            FixedAssetDetailArr1 = WtfGlobal.appendCustomColumn(FixedAssetDetailArr1, GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId], undefined, undefined, this.readOnly);
            var CustomtotalStoreCount1 = 0;
            var CustomloadedStoreCount1 = 0;

            for (var j = 0; j < FixedAssetDetailArr1.length; j++) {
                if (FixedAssetDetailArr1[j].dataIndex.indexOf('Custom_') != -1 && (FixedAssetDetailArr1[j].fieldtype === 4 || FixedAssetDetailArr1[j].fieldtype === 7)) {
                    CustomtotalStoreCount1++;
                    FixedAssetDetailArr1[j].editor.store.on('load', function() {
                        CustomloadedStoreCount1++;
                        if (CustomtotalStoreCount === CustomloadedStoreCount && this.FATransferHistoryGrid != undefined) {
                            this.populateCustomFieldValue(this.FATransferHistoryGrid);
                            this.disableCustomFieldOfGrid(this.FATransferHistoryGrid);
                            this.FATransferHistoryGrid.getView().refresh();
                        }
                    }, this)
                }
            }
        }
        
        // create Column Models
        this.FACM = new Wtf.grid.ColumnModel(FixedAssetDetailArr);
        this.FACM1 = new Wtf.grid.ColumnModel(FixedAssetDetailArr1);
        
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
            },{
                name: 'assetLife'
            },{
                name: 'elapsedLife'
            },{
                name: 'nominalValue'
            },{
                name: 'sellAmount',defValue:0
            },{
                name: 'installationDate'
            },{
                name: 'purchaseDate'
            },{
                name:'serial'                
            },{
                name:'batchdetails'
            },{
                name: 'customfield'
            },{
                name: 'location'
            },{
                name: 'warehouse'
            },{
                name: 'product'
            },{
                name: 'transferDate', type:'date'
            },{
                name: 'assetGroupId'
            }        
        ]);
        
        this.store = new Wtf.data.Store({
            url:'',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.FixedAssetDetailRec)
        });

        if (this.isShowDimension()) {
            var colModelArray = [];
            colModelArray = GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId];
            if (colModelArray) {
                colModelArray.concat(GlobalColumnModelForProduct[Wtf.Acc_FixedAssets_Details_ModuleId]);
            }
            WtfGlobal.updateStoreConfig(colModelArray, this.store);
        }
        
        var forceFitFlag = false;
        var IsWareHouseLocationActive = this.isWarehouse && this.isLocation;
        var customFieldLenth = GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId].length;
        
        if(IsWareHouseLocationActive && customFieldLenth <= 0){
            forceFitFlag = true;
        }else if(!IsWareHouseLocationActive && customFieldLenth <= 0){
            forceFitFlag = true;
        }
        
        this.FATransferGrid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            layout:'fit',
            clicksToEdit:1,
            height: 130,
            readOnly:this.readOnly,
            autoScroll:true,
            autoWidth:true,
            store: this.store,
            cm: this.FACM,
            tbar: ['<div style="font-size:15px; text-align:center;font-weight:bold;">  Fill Asset Transfer Details:<br></div>'],            
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit: forceFitFlag
            }
        });
   
        this.store1 = new Wtf.data.Store({
            url:'',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.FixedAssetDetailRec)
        });
   
        this.FATransferHistoryGrid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            layout:'fit',
            clicksToEdit:1,
            height: 400,
            readOnly:this.readOnly,
            autoScroll:true,
            autoWidth:true,
            store: this.store1,
            cm: this.FACM1,
            border : false,
            tbar: ['<div style="font-size:15px; text-align:center; font-weight:bold;"> Asset Transfer History:<br></div>'],                   
            loadMask : true,
            viewConfig: {
                forceFit: forceFitFlag,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
   
        this.FATransferGrid.on('afteredit',this.updateRow,this);
        this.FATransferGrid.on('beforeedit',this.beforeEditHandler,this);
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
    },
  
    updateRow:function(obj){
        if(obj!=null){
            var rec = obj.record;
            if(obj.field=='assetId'){
                var assetComboIndex = WtfGlobal.searchRecordIndex(this.assetComboStore, obj.value, 'assetdetailId');
                var assetrec = this.assetComboStore.getAt(assetComboIndex);
                obj.record.set('department',assetrec.get('department'));
                obj.record.set('assetUser',assetrec.get('assetUser'));
                obj.record.set('batchdetails',assetrec.get('batchdetails'));
                obj.record.set('isDepreciationPosted',assetrec.get('isDepreciationPosted'));
                if (assetrec.json && this.isShowDimension()) {
                    for (var key in rec.data) {
                        if (key.indexOf('Custom') != -1) { // 'Custom' prefixed already used for custom fields/ dimensions
                            if (assetrec.json[key] != "null" && assetrec.json[key] != "" && assetrec.json[key] != "undefined" && assetrec.json[key] != "NaN" && assetrec.json[key] !=undefined) {
                                rec.set(key, assetrec.json[key]);
                            }else {
                                rec.set(key, "");
                            }
                        }
                    }
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
    
    addGridRec:function(obj){
        var assetDetailsArrLength = 0;
        if(this.assetDetailsArray && this.assetDetailsArray !=''){
            this.assetDetailArr = eval('(' + this.assetDetailsArray + ')');
        }
        
        if(this.assetDetailArr && this.assetDetailArr.length>0){
            assetDetailsArrLength = this.assetDetailArr.length;
        }
        
        var iteratorLength = (this.quantity>assetDetailsArrLength)?this.quantity:assetDetailsArrLength;
        
        for(var i=0 ;i < iteratorLength ;i++){
            var rec= this.FixedAssetDetailRec;
            rec = new rec({});
            rec.beginEdit();
            var fields=this.store.fields;
            for(var x=0;x<fields.items.length;x++){
                
                if(this.assetDetailArr){
                    this.assetDetailRec = this.assetDetailArr[i];
                }
                
                var value="";                
                if(this.assetDetailRec){
                    value = this.assetDetailRec[fields.get(x).name];
                    if(!this.fromPO && (fields.get(x).name == 'assetId')){// not in linking case as record already comes decoded from java side
                        value = decodeURI(value);
                    }else if(fields.get(x).name == 'transferDate'){
                        value = new Date();
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
                            if (value != "" && value != undefined && value != "NaN" && value != "null") {
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
    
        // Put Asset Transfer History Data
    
        var assetTransferHistoryArrLength = 0;
        if(this.assetTransferHistoryArray && this.assetTransferHistoryArray !=''){
            this.assetTransferHistoryArr = eval('(' + this.assetTransferHistoryArray + ')');
        }
        
        if(this.assetTransferHistoryArr && this.assetTransferHistoryArr.length>0){
            assetTransferHistoryArrLength = this.assetTransferHistoryArr.length;
        }
        
        for(var i=0 ;i < assetTransferHistoryArrLength ;i++){
            var rec= this.FixedAssetDetailRec;
            rec = new rec({});
            rec.beginEdit();
            var fields=this.store1.fields;
            for(var x=0;x<fields.items.length;x++){
                
                if(this.assetDetailArr)
                    this.assetDetailRec = this.assetTransferHistoryArr[i];
                
                var value="";
                
                if(this.assetDetailRec){
                    value = this.assetDetailRec[fields.get(x).name];
                    if(!this.fromPO && (fields.get(x).name == 'assetId')){// not in linking case as record already comes decoded from java side
                        value = decodeURI(value);
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
                            if (value != "" && value != undefined && value != "NaN" && value != "null") {
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
            this.store1.add(rec);
        }
    },
    /**
     * Auto populate custom field value from Asset Group level to Asset Details level.
     */
    populateCustomFieldValue: function(grid) {
        var GlobalcolumnModel = GlobalColumnModel[this.moduleid];
        if (GlobalcolumnModel) {
            for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                var fieldname = GlobalcolumnModel[cnt].fieldname;
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

    /* * Returns value for drop down custom fields from group grid */    
    getValueForDimension: function(fieldName, value) {
        var grid = this.parentGrid;
        if (grid) {
            var array = grid.store.data.items;
            if (array.length > 1) {
                for (var i = 0; i < array.length - 1; i++) {
                    for (var k = 0; k < grid.colModel.config.length; k++) {
                        if (grid.colModel.config[k].editor && grid.colModel.config[k].editor.field.store && grid.colModel.config[k].dataIndex == fieldName) {
                            var store = grid.colModel.config[k].editor.field.store;
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

    /* * Disable custom fields at asset detail level */
    disableCustomFieldOfGrid: function(grid) {
        for (var k = 0; k < grid.colModel.config.length; k++) {
            if (grid.colModel.config[k].dataIndex.indexOf('Custom_') != -1) {
                grid.colModel.config[k].editor = '';
            }
        }
    },

    closeFADetails:function(){
        this.close();
    },

    saveData:function(){
        this.saveButton.disable();
        var arr = [];
        var returnFlag = false;
        if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){
            if((this.isBatchForProduct || this.isSerialForProduct ||this.isLocationForProduct || this.isWarehouseForProduct || this.isRowForProduct || this.isRackForProduct || this.isBinForProduct) && (this.isFromOrder || !this.isInvoice || this.isFromOpeningForm)){    //check wether batch and serial no detail entered or not
                var prodLength=this.FADetailsGrid.getStore().data.items.length
                for(var i=0;i<prodLength;i++){ 
                    var batchDetail = this.FADetailsGrid.getStore().data.items[i].data.batchdetails;
                    if(batchDetail == undefined || batchDetail == ""){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                        return;
                    }
                }
            }
        }
        this.store.each(function(record){
            if(record.get('assetId') == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.PleaseenterappropriatevalueFixedAssetId")],0);
                returnFlag = true;
                return;
            }
            arr.push(this.store.indexOf(record));
        },this);
        
        if(returnFlag){
            return;
        }
     
        if (this.isShowDimension()) {
            this.store.each(function(rec) {
                if (rec.data.assetDetails != "") {
                    rec.data[CUSTOM_FIELD_KEY] = Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, Wtf.Acc_FixedAssets_Details_ModuleId).substring(13));
                }
                arr.push(this.store.indexOf(rec));
            }, this);
        }
        this.assetDetails = this.getJSONArray(arr);
    
        var rec=[];
        rec.newAssetDetails = this.assetDetails;
        rec.oldAssetDetails = this.assetDetailsArray;
        Wtf.Ajax.timeout = WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url: "ACCProduct/saveAssetTransferDetails.do",
            params: rec
        },this,this.genTransferSaveSuccessResponse,this.genTransferSaveFailureResponse);    
    },

    genTransferSaveSuccessResponse: function(response){
        WtfGlobal.resetAjaxTimeOut();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.transaferAsset"),response.msg],response.success*2+1);
        if(response.success){
            this.close();
        }else{
            this.close();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],1);
        }
    },
    
    genTransferSaveFailureResponse: function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.close();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);    
    },

    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.FATransferGrid,true,arr);
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
