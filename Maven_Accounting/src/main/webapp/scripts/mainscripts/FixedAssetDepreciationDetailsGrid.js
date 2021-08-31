Wtf.account.FixedAssetDepreciationDetailsGrid = function(config){
    this.btnArr=[];
    this.arr = [];
    this.postUnPostVal=0; //Java side this variable value is used.
    this.moduleid=config.moduleid;
    Wtf.apply(this, config);
    Wtf.account.FixedAssetDepreciationDetailsGrid.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.FixedAssetDepreciationDetailsGrid, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.FixedAssetDepreciationDetailsGrid.superclass.onRender.call(this, config);
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:false
        });
        this.depreciationCalculationType = Wtf.account.companyAccountPref.depreciationCalculationType;
  
        var data=WtfGlobal.getBookBeginningYear(true, true);
    
        this.yearStore= new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'int'
            }, 'yearid'],
            data :data
        });

        this.selectYear = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'startYear',
            displayField:'yearid',
            anchor:'95%',
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            width:90,
            selectOnFocus:true
        });  
        
        this.startDateVal = WtfGlobal.getDates(true);
        this.endDateVal = WtfGlobal.getDates(false);
        if (Wtf.account.companyAccountPref.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE) {
            this.startDateVal = WtfGlobal.getBookBeginningDates(true);
            this.endDateVal = WtfGlobal.getBookBeginningDates(false);
        } 
        
        this.startDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            value:this.startDateVal,
            allowBlank: false,
            minValue:WtfGlobal.getOpeningDocumentDate(true),//Set From Date min value as after first financial year date.
            minText:WtfGlobal.getLocaleText({key:"acc.asset.depreciationdetail.fromDateToolTip",params: [this.startDateVal.format(WtfGlobal.getOnlyDateFormat())]}),
        });
        this.endDate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            name:'enddate',
            value:this.endDateVal,
            allowBlank: false
        });
        
        this.checkDates();
        if(this.selectYear.getRawValue()==""){
            var temp=new Date();
            var currentyear=temp.getFullYear();
            this.selectYear.setValue(""+currentyear+"");
        }
                
        this.btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.field.QuickSearchAssetIdDesc"),  //'Quick Search by Asset Id, Description',
            id:"quickSearch"+this.id,
            width: 200,
            renderer: this.fetchData
        }));
                        
        //        this.btnArr.push(WtfGlobal.getLocaleText("acc.reval.year"),this.selectYear);
        this.btnArr.push('-',WtfGlobal.getLocaleText("acc.common.from"),this.startDate,'-',WtfGlobal.getLocaleText("acc.common.to"),this.endDate);
        
        this.postedUnpostedStore = new Wtf.data.SimpleStore({
            fields: [{
                name: 'typeid', 
                type: 'int'
            }, 'name'],
            data: [
            [0, "All"],
            [1, "Posted"],
            [2, "Unposted"]
            ]
        });

        this.postedUnpostedCombo = new Wtf.form.ComboBox({//All/Balance Sheet/Profit & Loss
            store: this.postedUnpostedStore,
            name: 'typeid',
            displayField: 'name',
            id: 'typeid',
            valueField: 'typeid',
            mode: 'local',
            value: 0,
            width: 100,
            listWidth: 200,
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true
        });
        
//        this.btnArr.push(this.postedUnpostedCombo);
        
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
            triggerAction: 'all',
            scope:this,
            hirarchical:true
        };
           
        this.assetGroupCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),
            forceSelection:true,   
            listWidth:300,
            width:180
        },this.assetGroupComboConfig));
            
        this.btnArr.push("-",WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),this.assetGroupCombo,"-");
        
        if(this.depreciationCalculationType == 0) {
            // Add Post Options Comnbo so that there will be two options to post depreciatio i.e. Yearly or Monthly
           
            this.postOptions = new Wtf.form.ComboBox({
                store: Wtf.postOptionStore,
                fieldLabel:WtfGlobal.getLocaleText("acc.field.PostOpetions"),  //Post Options
                name:'postoptionid',
                displayField:'name',
                forceSelection: true,
                anchor:'95%',
                valueField:'postoptionid',
                defaultValue: "1",
                mode: 'local',
                hidden: !this.depreciationCalculationType==0,
                width:90,
                triggerAction: 'all',
                selectOnFocus:true
            });
            
            if(this.postOptions.getValue() == ""){
                this.postOptions.setValue(Wtf.postOptionStore.data.items[0].json[0]);
            }
        this.resetBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
                tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
                id: 'btnRec',
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.resetbutton),
                disabled: false
            });
            this.resetBttn.on('click', this.handleResetClick, this);
            this.postOptions.on('select',function(){
                var month = this.grid.colModel.config[2];
                var year = this.grid.colModel.config[3];                
                if(this.postOptions.getValue()=="2"){
                    this.depreciationCalculationType = 2;
                    month.hidden = false;
                    year.hidden = true;
                }else{
                    this.depreciationCalculationType = 0;
                    month.hidden = true;
                    year.hidden = false;                
                }                       
                this.fetchData1();
                this.grid.getView().refresh(true);
                this.doLayout();
            },this);
            
            this.btnArr.push("Post Options ",this.postOptions);
        }
        
        this.btnArr.push({
            xtype:'button',
            text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',
            iconCls:'accountingbase fetch',
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.field.selectdifferentyesr"),  //select Different Years to View Depreciation Details Report,
            handler:this.fetchData1
        },{
            xtype:'button',
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
                tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
                id: 'btnRec',
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.resetbutton),
                handler:this.handleResetClick
        });
        this.gridStoreReader = new Wtf.data.KwlJsonReader2({
            totalProperty: 'totalcount',
            root: "data"
        });
        this.Store = new Wtf.data.Store({
            url:"ACCProductCMN/getAssetDepreciationDetails.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            })
        },this);
        
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
            get:Wtf.autoNum.AssetDepreciationReport
        });
        
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
            disabled :true,
            label:"Print",
            menuItem:{
                print:true
            },
            get:Wtf.autoNum.AssetDepreciationReport
        });
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.exportadepr)) {
            this.btnArr.push(this.expButton);
        }
        if (!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.printadepr)) {
            this.btnArr.push(this.printButton);
        }
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : 'Loading...'
        });
        this.Store.on('beforeload',function(s,o){
            WtfGlobal.setAjaxTimeOut();
            this.loadMask.show();
            if(!o.params)o.params={};
            var currentBaseParams = this.Store.baseParams;
            currentBaseParams.year = this.selectYear != undefined ? this.selectYear.getValue() : "",   
            currentBaseParams.depreciationCalculationType = this.depreciationCalculationType,  
            currentBaseParams.postedUnposted=this.postUnPostVal,
            currentBaseParams.assetGroupIds= this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue()
            currentBaseParams.ss=this.quickPanelSearch == undefined ? "" :this.quickPanelSearch.getValue(),
            currentBaseParams.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            currentBaseParams.isDepreciationDetailReport = true,
            currentBaseParams.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            this.Store.baseParams=currentBaseParams;
            var exportParams = {
                year: this.selectYear != undefined ? this.selectYear.getValue() : "",
                depreciationCalculationType: this.depreciationCalculationType,
                postedUnposted: this.postUnPostVal,
                isDepreciationDetailReport: true,
                stdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                start: 0,
                limit: 30,
                ss: this.quickPanelSearch == undefined ? "" : this.quickPanelSearch.getValue(),
                assetGroupIds: this.assetGroupCombo.getValue() == "" ? "All" : this.assetGroupCombo.getValue(),
                searchJson: this.searchJson
            };
            this.printButton.setParams(exportParams);
            this.expButton.setParams(exportParams);
        },this);
        
        this.Store.on('loadexception',function(){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);
        
        this.groupStore = new Wtf.data.GroupingStore({
            groupField: ['groupinfo']
        });
        this.Store.on("dataChanged", function(){
            this.loadData();
        }, this);
        this.Store.on('load',function(store){
            WtfGlobal.resetAjaxTimeOut();
            if(this.Store.getCount()==0){ // if there is no record to show then keep export buttons disable
                if(this.expButton)this.expButton.disable();
                if(this.printButton)this.printButton.disable();
            //                this.grid.getView().refresh(true);  
            }else{
                if(this.expButton)this.expButton.enable();
                if(this.printButton)this.printButton.enable();    
            }
            //            WtfGlobal.resetAjaxTimeOut();
            this.quickPanelSearch.StorageChanged(store);
            this.loadData();
            this.loadMask.hide();
        },this);
        //create Grid
    
        this.createFADepreciationDetailsGrid();
        this.grid.on("gridconfigloaded", function (grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.grid.on('statesave', this.saveMyStateHandler, this);
            }, this);
        }, this);

        this.expButton.setParams({
            year: this.selectYear != undefined ? this.selectYear.getValue() : "",
            depreciationCalculationType:this.depreciationCalculationType,
            postedUnposted: this.postUnPostVal,
            start:0,
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            limit:30,
            isDepreciationDetailReport : true,
            ss : this.quickPanelSearch == undefined ? "" :this.quickPanelSearch.getValue(),
            assetGroupIds : this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue()
        });
        
        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        this.btnArr.push(this.AdvanceSearchBtn);
        
        var moduleidarr = this.moduleid+','+Wtf.Acc_FixedAssets_AssetsGroups_ModuleId;
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.grid.colModel,
//            moduleid: this.moduleid,
            moduleidarray: moduleidarr.split(','),
            advSearch: false,
            ignoreDefaultFields: true,
            linelevelfields: true,
            reportid: Wtf.autoNum.AssetDepreciationReport
        });
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
        this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        
        this.customReportViewBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            scope: this,
            // hidden:!(this.isOrder && this.isCustomer),
            tooltip: WtfGlobal.getLocaleText("acc.field.CustomizeReportView"),
            handler: this.customizeView,
            iconCls: 'accountingbase fetch'
        
        });
        this.btnArr.push("->",this.customReportViewBtn); 
        
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
                    store: this.Store,
                    searchField: this.quickPanelSearch,
                    displayInfo: false,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id: "pPageSize_" + this.id
                    })
                })
            }]
        }); 
        this.add(this.leadpan);
    },handleResetClick:function(){
        this.startDate.setValue(this.startDateVal);
        this.endDate.setValue(this.endDateVal);
        this.assetGroupCombo.setValue("");
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
        }
        this.fetchData1();;
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
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: filterConjuctionCriteria,
            isFixedAsset:true
        }
        this.Store.load({
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
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleid,
            filterConjuctionCriteria: this.filterConjuctionCrit,
            isFixedAsset:true
        }
        this.Store.load({
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
    customizeView: function() {
        this.customizeViewWin = new Wtf.CustomizeReportView({
            title: WtfGlobal.getLocaleText("acc.field.CreateCustomizeView"),
            parentPanel: this,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            grid: this.grid,
            reportId: '121',
            modules:'121'
           
        });
        this.customizeViewWin.show();
        var arr = this.arr;
    },
    fetchData1:function(){
       if (!this.checkDates()) {
            return;
       }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value,
                stdate:this.sdate,
                enddate:this.edate
            }
        });

    },
    fetchData: function(){
        WtfComMsgBox(29,4,true); //Show loading mask
        if (!this.checkDates()) {
            return;
        }
        var params={
            //            year: this.selectYear != undefined ? this.selectYear.getValue() : "",
            depreciationCalculationType: this.depreciationCalculationType,
            postedUnposted: this.postUnPostVal,
            start:0,
            limit:30,
            ss : this.quickPanelSearch == undefined ? "" :this.quickPanelSearch.getValue(),
            assetGroupIds : this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue(),
            isDepreciationDetailReport : true,
            isCreateSchedule : this.isCreateSchedule,
            stdate:this.sdate,
            enddate:this.edate
        };
        
        this.ajxUrl = "ACCProductCMN/getAssetDepreciationDetails.do";
        
        this.expButton.setParams({
            //            year: this.selectYear != undefined ? this.selectYear.getValue() : "",
            isDepreciationDetailReport : true,
            stdate:this.sdate,
            enddate:this.edate,
            depreciationCalculationType:this.depreciationCalculationType,
            postedUnposted:this.postUnPostVal,
            start:0,
            limit:30,
            ss : this.quickPanelSearch == undefined ? "" :this.quickPanelSearch.getValue(),
            assetGroupIds : this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue()
        });
        
        this.printButton.setParams({
            year: this.selectYear != undefined ? this.selectYear.getValue() : "",
            depreciationCalculationType: this.depreciationCalculationType,
            postedUnposted: this.postUnPostVal,
            isDepreciationDetailReport : true,
            stdate:this.sdate,
            enddate:this.edate,
            start:0,
            limit:30,
            ss : this.quickPanelSearch == undefined ? "" :this.quickPanelSearch.getValue(),
            assetGroupIds : this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue()
        });
        
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:this.ajxUrl,
            params:params
        }, this, this.successCallback, this.failureCallback);
    },
    
    successCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        if(response.success){    
            this.grid.getStore().reader.read(response);
            var columns = [];
            Wtf.each(this.grid.getStore().reader.jsonData.columns, function(column){
                if(column.editor) {
                    var editor  = eval ("(" + column.editor + ")");
                    column.editor = editor;
                }
                if(column.dataIndex == "assetId"){
                    var renderer = WtfGlobal.assetIdRenderer;
                    column.renderer = renderer;
                }else if(column.align == "right"){
                    var renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
                    column.renderer = renderer;
                } 
                columns.push(column);
            });
            
            var colModelArray = [];
            colModelArray = GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId];
            if (colModelArray) {
                colModelArray.concat(GlobalColumnModelForProduct[Wtf.Acc_FixedAssets_Details_ModuleId]);
            }
            WtfGlobal.updateStoreConfigForDynamicColModel(colModelArray, this.grid.getStore());
            var colModelArrayReports = GlobalColumnModelForReports[Wtf.Acc_FixedAssets_Details_ModuleId];
            WtfGlobal.updateStoreConfig(colModelArrayReports, this.grid.getStore());
       
//            columns = WtfGlobal.appendCustomColumn(columns, GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId], undefined, undefined, this.readOnly);
            columns = WtfGlobal.appendCustomColumn(columns, GlobalColumnModel[Wtf.Acc_FixedAssets_Details_ModuleId],true,undefined,undefined,undefined,Wtf.Acc_FixedAssets_Details_ModuleId);
            columns = WtfGlobal.appendCustomColumn(columns, GlobalColumnModelForReports[Wtf.Acc_FixedAssets_Details_ModuleId],true);
            var CustomtotalStoreCount = 0;
            var CustomloadedStoreCount = 0;

            for (var j = 1; j < columns.length; j++) {
                if (columns[j].dataIndex.indexOf('Custom_') != -1 && (columns[j].fieldtype === 4 || columns[j].fieldtype === 7)) {
                    CustomtotalStoreCount++;
                    columns[j].editor.store.on('load', function() {
                        CustomloadedStoreCount++;
                        if (CustomtotalStoreCount === CustomloadedStoreCount && this.grid != undefined) {
                            this.grid.getView().refresh();
                        }
                    }, this)
                }
            }
               
            this.grid.getColumnModel().setConfig(columns);
            this.grid.getColumnModel().on("configchanged", function(){
                //                alert("configchanged");
                },this);
            this.doLayout();
            this.grid.store.loadData(response.data);
            this.grid.getView().refresh(true);
            if(this.Store.getCount()==0){
                if(this.expButton)this.expButton.disable();
                if(this.printButton)this.printButton.disable();
                this.grid.getView().refresh(true);  
            }else{
                if(this.expButton)this.expButton.enable();
                if(this.printButton)this.printButton.enable();    
            }
        }
    },

    failureCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
    },
    
    createFADepreciationDetailsGrid : function(){
        
        var colArr = [];
        this.cm = new Wtf.grid.ColumnModel(colArr);
        this.summary = new Wtf.grid.GroupSummary();
        var grpView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: true,
            enableGroupingMenu: true,
            hideGroupedColumn: false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.asset.depreciation.alert.msg"))
        });
    
        this.grid = new Wtf.grid.GridPanel({
            store: this.groupStore,
            height: 500,
            scope:this,
            columns: [{
                dataIndex: "assetGroup"
            }],
            border: false,
            loadMask: true,
            sm: this.sm,
            displayInfo: true,
            disabledClass: "newtripcmbss",
            //        layout: 'fit',
            autoScroll: true,
            view: grpView,
            plugins: [this.summary]
        });
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0,
                limit: 30,
                isFirstTimeLoad: Wtf.isFirstTimeLoad
            }
        });
    },
    loadData: function () {
        //             this.grid.getStore().reader.read(response);
            var columns = [];
            Wtf.each(this.Store.reader.jsonData.columns, function (column) {
                if (column.editor) {
                    var editor = eval("(" + column.editor + ")");
                    column.editor = editor;
                }
                if (column.dataIndex == "assetId") {
                    var renderer = WtfGlobal.assetIdRenderer;
                    column.renderer = renderer;
                } else if (column.align == "right") {
                    var renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
                    column.renderer = renderer;
                } else if (column.renderer != undefined) {
                    column.renderer = eval('(' + column.renderer + ')');
                }
                columns.push(column);
            });
            this.grid.getColumnModel().setConfig(columns);
            this.grid.getColumnModel().on("configchanged", function () {
                //                alert("configchanged");
            }, this);
            this.grid.getView().refresh();
        if (!this.isConfigLoaded) {
            this.getMyConfig();
            this.isConfigLoaded = true;
        }
        var Arr = [];
        Wtf.each(this.Store.reader.jsonData.metaData.fields, function (column) {
            Arr.push(column);
        });

        this.groupStore.removeAll();
        this.groupStore.fields = Arr;
        var array = [];
        this.groupStore.add(this.Store.getRange(0, (this.Store.data.items.length - 1)));
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
    
    getMyConfig: function () {
        WtfGlobal.getGridConfig(this.grid, this.moduleid, false, true, false);
    },
    saveMyStateHandler: function (grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    },
    checkDates: function(){
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        
        if(this.sDate=="" || this.eDate=="") {
            WtfComMsgBox(42,2);
            return false;
        }
        
        if (this.sDate < WtfGlobal.getOpeningDocumentDate(true)) {
              WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText({key: "acc.asset.depreciationdetail.fromDateToolTip", params: [WtfGlobal.getOpeningDocumentDate(true).format(WtfGlobal.getOnlyDateFormat())]})], 2);
            return false;
        }
        this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
            return false;
        }
        return true;
    }

//    getBookBeginningYear:function(isfirst){
//        var ffyear;
//        if(isfirst){
//            var cfYear=new Date(Wtf.account.companyAccountPref.fyfrom)
//            ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom)
//            ffyear=new Date( ffyear.getFullYear(),cfYear.getMonth(),cfYear.getDate()).clearTime()
//        }
//        else{
//            var fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
//            ffyear=new Date( fyear,this.fmonth.getValue(),this.fdays.getValue()).clearTime()
//        }
//        var data=[];
//        var newrec;
//        if(ffyear==null||ffyear=="NaN"){
//            ffyear=new Date(Wtf.account.companyAccountPref.fyfrom)
//        }
//        var year=ffyear.getFullYear();
//        var year1=year;
//        data.push([0,year1+5]);
//        data.push([1,year1+4]);
//        data.push([2,year1+3]);
//        data.push([3,year1+2]);
//        data.push([4,year1+1]);
//        data.push([5,year1]);
//        return data;
//    }
})