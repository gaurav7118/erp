Wtf.account.FixedAssetDepreciation = function(config){
    Wtf.apply(this, config);
    Wtf.account.FixedAssetDepreciation.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.FixedAssetDepreciation, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.FixedAssetDepreciation.superclass.onRender.call(this, config);
        
        this.depreciationCalculationType = Wtf.account.companyAccountPref.depreciationCalculationType;
        this.depreciationCalculationBasedOn = Wtf.account.companyAccountPref.depreciationCalculationBasedOn;
        
        this.finanDate = undefined;
        
        // Create Combo Box to Select Multiple Assets
        this.assetsComboRec = Wtf.data.Record.create ([
        {
            name:'assetdetailId'
        },{
            name:'assetGroup'
        },{
            name:'assetGroupId'
        },{
            name:'assetId'
        }]);
        
        this.assetsComboStore = new Wtf.data.Store({
            url:"ACCAsset/getAssetDetails.do",
            baseParams:{
                isDepreciationReport:true
            },
            reader: new  Wtf.data.KwlJsonReader({
                totalProperty: 'totalCount',
                root: "data"
            },this.assetsComboRec)
        }); 
        
        
        this.assetsComboStore.on('beforeload', function(store){
            WtfGlobal.setAjaxTimeOut();
        }, this);          
        
        this.assetsComboStore.on('load', function(store){
            WtfGlobal.resetAjaxTimeOut();
            var storeNewRecord=new this.assetsComboRec({
                assetdetailId:'All',
                assetId:'All',
                assetGroup:''
            });
            store.insert( 0,storeNewRecord);
            if(store.getCount()>0){
                this.Assets.setValue(store.getAt(0).data.assetdetailId);
            }
        }, this);  
        
        this.assetsComboStore.load();
        
        this.AssetsComboconfig = {       
            store: this.assetsComboStore,
            valueField:'assetdetailId',
            hideLabel:false,
            displayField:'assetId',
            emptyText:WtfGlobal.getLocaleText("erp.SelectAssets"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };

        this.Assets = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            fieldLabel:"Assets" ,
            forceSelection:true,   
            extraFields:['assetGroup'],
            extraComparisionField:'assetGroup',// type ahead search on acccode as well.
            listWidth:250,
            width:200
        },this.AssetsComboconfig));
      

        // to select the Year and month from the drop down list
        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'monthid',
                type:'int'
            }, 'name'],
            data :[[0,"January"],[1,"February"],[2,"March"],[3,"April"],[4,"May"],[5,"June"],[6,"July"],[7,"August"],[8,"September"],[9,"October"],
            [10,"November"],[11,"December"]]
        });

        var data=WtfGlobal.getBookBeginningYear(true, true);
    
        this.yearStore= new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'int'
            }, 'yearid'],
            data :data
        });
     
        if(this.depreciationCalculationType==0){  // if Average method selected is Yearly 
            this.yearsComboconfig = {       
                store: this.yearStore,
                name:'startYear',
                displayField:'yearid',
                anchor:'95%',
                valueField:'yearid',
                emptyText:"Select Years",
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,
                triggerAction:'all',
                scope:this
            };
           
            this.selectYear = new Wtf.common.Select(Wtf.applyIf({
                multiSelect: true,
                fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"),
                forceSelection:true,   
                listWidth:250,
                width:90
            },this.yearsComboconfig));
            
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
                hidden: !this.depreciationCalculationType==0, // if Average method selected is not Yearly
                width:90,
                triggerAction: 'all',
                selectOnFocus:true
            });
            
            if(this.postOptions.getValue() == ""){
                this.postOptions.setValue(Wtf.postOptionStore.data.items[0].json[0]);
            }
        
            this.postOptions.on('select',function(){
                var month = this.grid.colModel.config[2];
                var year = this.grid.colModel.config[3];                
                if(this.postOptions.getValue()=="2"){
                    this.depreciationCalculationType = 2 ; //set Post method monthly
                    month.hidden = false;
                    year.hidden = true;
                }else{
                    this.depreciationCalculationType = 0; // set post method Yearly
                    month.hidden = true;
                    year.hidden = false;                
                }
                
                var monthVal = new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth();
                if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                    monthVal = new Date(Wtf.account.companyAccountPref.bbfrom).getMonth();
                }else{
                    monthVal = new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth();
                }                
                if(monthVal != 0){ //Financial year started other than January month
                    this.startMonth.setValue(this.monthStore.data.items[monthVal].json[0]);
                    this.endMonth.setValue(this.monthStore.data.items[monthVal-1].json[0]);
                }         
                this.gridStore.load();
                this.grid.getView().refresh(true);
                this.doLayout();
            },this);            
        
        }else{    
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
        }

        this.yearStore.on('load', function(store){
            if(store.getCount()>0){
                this.Assets.setValue(store.getAt(0).data.yearid);
                this.loaddata();
            }      
        }, this);   

        this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'startMonth',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            hidden:this.depreciationCalculationType==0, // if Average method selected is Yearly
            width:90,
            triggerAction: 'all',
            selectOnFocus:true
        });  

        this.endMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'endMonth',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            hidden:this.depreciationCalculationType==0, // if Average method selected is Yearly
            triggerAction: 'all',
            width:90,
            selectOnFocus:true
        }); 
    
        if(this.selectYear.getRawValue()==""){
            var temp=new Date();
            var currentyear=temp.getFullYear();
            this.selectYear.setValue(""+currentyear+"");
        }
        
        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.endMonth.getValue() == ""){
            var fyear = undefined;
            if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                fyear = new Date(Wtf.account.companyAccountPref.bbfrom).getFullYear();
            }else{
                fyear = new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear();
            }
            
            if(this.selectYear.getRawValue()==fyear){
                var month = undefined;
                if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                    month = new Date(Wtf.account.companyAccountPref.bbfrom).getMonth();
                }else{
                    month = new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth();
                }
                if(month != 0){
                    this.startMonth.setValue(this.monthStore.data.items[month].json[0]);
                    for(var monthNo = 0; monthNo < month; monthNo++){
                        var record = this.monthStore.getAt(0);
                        this.monthStore.remove(record);
                    }
                } else {
                    this.startMonth.setValue(this.monthStore.data.items[0].json[0]);  
                }
            }else{
                this.startMonth.setValue(this.monthStore.data.items[0].json[0]);
            }
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[0]); 
        }
        
        this.selectYear.on('select',function(){
            var fyear = undefined;
            if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                fyear=new Date(Wtf.account.companyAccountPref.bbfrom).getFullYear()
            }else{
                fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
            }
            if(this.selectYear.getRawValue()==fyear){
                var month = undefined;
                if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                    month = new Date(Wtf.account.companyAccountPref.bbfrom).getMonth();
                }else{
                    month = new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth();
                }
                if(month != 0){
                    this.startMonth.setValue(this.monthStore.data.items[month].json[0]);
                    for(var monthNo = 0; monthNo < month; monthNo++){
                        var record = this.monthStore.getAt(0);
                        this.monthStore.remove(record);
                    }
                }
            }else{
                var data = [[0,"January"],[1,"February"],[2,"March"],[3,"April"],[4,"May"],[5,"June"],[6,"July"],[7,"August"],[8,"September"],[9,"October"],
                [10,"November"],[11,"December"]];
                this.monthStore.loadData(data);
                this.startMonth.setValue(this.monthStore.data.items[0].json[0]);
            }
        },this);
        
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
                
        this.gridRec = Wtf.data.Record.create ([
        {
            name:'perioddepreciation'
        },{
            name:'accdepreciation'
        },{
            name:'netbookvalue'
        },{
            name:'netbookvalueInBase'
        },{
            name:'firstperiodamtInBase'
        },{
            name:'accdepreciationInBase'
        },{
            name:'currencysymbol'
        },{
            name:'currencyid'
        },{
            name:'depdetailid'
        },{
            name:'period'
        },{
            name:'firstperiodamt'
        },{
            name:'frommonth',
            type:'date'
        },{
            name:'isje'
        },{
            name:'isFuture'
        },{
            name:'tomonth',
            type:'date'
        },{
            name:'year',
            type:'date'
        },{
            name:'fromyear'
        },{
            name:'toyear'
        },{
            name:'assetId'
        },{
            name:'assetDetailsId'
        },{
            name:'assetGroupId'
        },{
            name:'isSelected'
        },{
            name:'status'
        },{
            name: 'isQuantityAvailableForDepreciation'
        },{
            name: 'differentPostOption'
        },{
            name:'jeid'
        },{
            name:'jeno'
        },{
            name:'jedate'
        }
        ]);

        this.msgLmt = 30;
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        }, this.gridRec);
                
        this.gridStore = new Wtf.data.GroupingStore({
            url: "ACCProductCMN/getAssetDepreciation.do",
            groupField:'assetId', 
            params:{
                assetdetailId:this.assetdetailIds,
                fromMonth:this.fromMonth,
                toMonth:this.toMonth,
                years : this.years,
                depreciationCalculationType:this.depreciationCalculationType,
                finanDate : this.finanDate,
                postOption : this.postOptions != undefined ? this.postOptions.getValue() : "0",
                assetGroupIds : this.assetGroupIds
            },
            sortInfo: {
                field: 'assetId',
                direction: "DESC"
            },
            reader: this.gridStoreReader
        });
                
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : 'Loading...'
        });

        this.gridStore.on('loadexception',function(){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);

        this.gridStore.on('beforeload',function(){
            var currentBaseParams = this.gridStore.baseParams;
            currentBaseParams.assetdetailId=this.Assets.getValue()==""?"All":this.Assets.getValue();
            currentBaseParams.assetGroupIds=this.assetGroupCombo.getValue()==""?"All":this.assetGroupCombo.getValue();
            currentBaseParams.fromMonth = this.startMonth.getValue();
            currentBaseParams.toMonth =this.endMonth.getValue();
            if(this.postOptions != undefined){
                if(this.postOptions.getValue() == 2){
                    var currentyear=this.selectYear.getValue(); 
                    if(this.selectYear.getValue().indexOf(""+(parseInt(currentyear)+1)) == -1){
                        this.selectYear.setValue(""+currentyear+","+(parseInt(currentyear)+1)+"");
                    }
                    if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                        this.finanDate =  WtfGlobal.convertToGenericStartDate(new Date(Wtf.account.companyAccountPref.bbfrom)); 
                    }else{
                        this.finanDate =  WtfGlobal.convertToGenericStartDate(new Date(Wtf.account.companyAccountPref.firstfyfrom)); 
                    }
                }
                currentBaseParams.postOption = this.postOptions != undefined ? this.postOptions.getValue() : "0";
            }
             
            currentBaseParams.years =this.selectYear.getValue(); 
            currentBaseParams.depreciationCalculationType=this.depreciationCalculationType;
            currentBaseParams.isGenerateAssetDepreciation =true; //This flag is used to java side calculating asset depreciation logic
            if(this.finanDate != undefined){
                currentBaseParams.finanDate = this.finanDate;
            }
            this.gridStore.baseParams=currentBaseParams; 
            WtfGlobal.setAjaxTimeOut();
            this.loadMask.show();
        },this);

        this.gridStore.on('load',function(){
            WtfGlobal.resetAjaxTimeOut();
            var arrdepreciated=[];
            this.arrRec = this.gridStore.getRange(0,this.gridStore.getCount()-1);
            for(var i=0;i<this.gridStore.getCount();i++){
                if(this.arrRec[i].data.isje){
                    arrdepreciated.push(this.arrRec[i]);
                }
            }
            WtfGlobal.highLightRowColor(this.grid,arrdepreciated,true,0,2);    
            this.loadMask.hide();
        },this);

       //create Grid
        
        this.createFADepreciationGrid();
        this.gridStore.load({
            params:{
                isFirstTimeLoad:Wtf.isFirstTimeLoad
            }
        });

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
            get:Wtf.autoNum.FixedAssetDepreciation
        });
                
        this.expButton.setParams({
            assetdetailId: this.Assets.getValue()==""?"All":this.Assets.getValue(),
            fromMonth : this.startMonth.getValue(),
            toMonth : this.endMonth.getValue(),
            years : this.selectYear.getValue(),   
            depreciationCalculationType : this.depreciationCalculationType,
            assetGroupIds : this.assetGroupIds
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
            get:Wtf.autoNum.FixedAssetDepreciation
        });
        
        this.printButton.setParams({
            assetdetailId: this.Assets.getValue()==""?"All":this.Assets.getValue(),
            fromMonth : this.startMonth.getValue(),
            toMonth : this.endMonth.getValue(),
            years : this.selectYear.getValue(),   
            depreciationCalculationType : this.depreciationCalculationType,
            assetGroupIds : this.assetGroupIds
        });
    
        this.gridStore.on('load',function(){
            if(this.gridStore.getCount()==0){
                if(this.expButton)this.expButton.disable();
                if(this.printButton)this.printButton.disable();
            }else{
                if(this.expButton)this.expButton.enable();
                if(this.printButton)this.printButton.enable();    
            }
            this.loadMask.hide();
        },this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.exportadep)) {  
            this.btnArr.push(this.expButton);
        }
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.printadep)) {  
            this.btnArr.push(this.printButton);
        }
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [ {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: [WtfGlobal.getLocaleText("acc.reval.year"),this.selectYear, 
                this.depreciationCalculationType==0?"":WtfGlobal.getLocaleText("erp.FromMonth"),this.startMonth,
                this.depreciationCalculationType==0?"":WtfGlobal.getLocaleText("erp.ToMonth"),this.endMonth,
                "-",WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),this.assetGroupCombo,
                "-",WtfGlobal.getLocaleText("erp.SelectAssets"),this.Assets,
                this.depreciationCalculationType==0 ? WtfGlobal.getLocaleText("acc.field.PostOpetions") : "" ,this.depreciationCalculationType==0 ? this.postOptions:"",
                {
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                    tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
                    iconCls:'accountingbase fetch',
                    scope:this,
                    handler:this.GenerateDepriciationData
                },this.btnArr,
                '<span class="highlightrecordsred" style="margin: 0px 10px;">&nbsp;&nbsp;&nbsp;&nbsp;</span><span id="wtf-gen1092">'+WtfGlobal.getLocaleText("erp.depriciationhadposted")+'</span></span>'],
                bbar: ''                                                   //Need to implement paging in future
            //                    this.pagingToolbar = new Wtf.PagingSearchToolbar({
            //                        pageSize: 30,
            //                        id: "pagingtoolbar" + this.id,
            //                        store: this.gridStore,
            //                        searchField: this.quickPanelSearch,
            //                        displayInfo: true,
            //                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            //                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
            //                })
            }]
        }); 
        this.add(this.leadpan);
    },
    
    createFADepreciationGrid:function(){
        this.btnArr = [];
        this.postDepreciation = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.fixedAssetList.post"),
            iconCls:getButtonIconCls(Wtf.etype.save),
            scope:this,
            handler:this.postHandler
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.postadep)) { 
            this.btnArr.push(this.postDepreciation);
        }
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            multiSelect:true
        });
        
        this.sm.on("beforerowselect",this.beforerowselectGrid.createDelegate(this),this);
        
        this.colModel = new Wtf.grid.ColumnModel([this.sm,{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.period"),
            dataIndex:'period',
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.field.Month"),
            dataIndex:'frommonth',
            renderer:WtfGlobal.onlyMonthDeletedRenderer,
            hidden:this.depreciationCalculationType==0,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.field.Year"),
            dataIndex:'fromyear',
            hidden:this.depreciationCalculationType!=0,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.je.jeNumber"),
            dataIndex:'jeno',
            renderer:WtfGlobal.linkDeletedRenderer,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.periodDep"),
            dataIndex:'firstperiodamt',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.accDep"),
            dataIndex:'accdepreciation',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.netBookVal"),
            dataIndex:'netbookvalue',
            align:'center',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("erp.Assets"),//'Asset Group',
            dataIndex:'assetId',
            //            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:75
        },{
            header: WtfGlobal.getLocaleText("acc.field.DepreciationStatus"),
            dataIndex: 'status',
            align: 'center',
            //            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:75    
        }]);
    
        this.colModel.defaultSortable = false;

        this.grid = new Wtf.grid.GridPanel({
            cm:this.colModel,
            store:this.gridStore,
            sm:this.sm,
            stripeRows :true,
            border:false,
            layout:'fit',
            view: new Wtf.grid.GroupingView({
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.asset.depreciation.alert.msg"))
            }),
            plugins :[Wtf.ux.grid.plugins.GroupCheckboxSelection]
        });
        this.grid.on('cellclick',this.onCellClick, this);
    },
    onCellClick:function(g,i,j,e){
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="jeno"){
            var accid = this.gridStore.getAt(i).data['jeid'];
            Wtf.onCellClick(accid,this.gridStore.getAt(i).data['jedate'],this.gridStore.getAt(i).data['jedate']);
        }
    },
    
    getDepreciationData : function(store,assetId){
        var selected=this.grid.getSelectionModel().getSelections();
        for(var i=0;i<selected.length;i++){
            selected[i].data.isSelected=true;
        }
        var arr=[];
        store.each(function(rec){
            if((!rec.data.isFuture && rec.data.isSelected))
                arr.push(store.indexOf(rec));
        },this);
        return WtfGlobal.getJSONArray(this.grid,true,arr);
    },
    
    postHandler:function(){
        if(!this.grid.getSelectionModel().hasSelection()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.178")],2);
            return;
        }    
        if(!(this.grid.getSelectionModel().getSelected().data['isQuantityAvailableForDepreciation'])){ // If selected ID is returned with purchase return or deliverd through through DO
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.asset.doesNotExists")],2);
            return;
        }
        if(this.grid.getSelectionModel().getSelections().length > Wtf.Max_Depreciation_Months){
            var msg = WtfGlobal.getLocaleText("acc.fixed.asset.depreciation.limit.msg");//"Sorry! you can post depreciation for maximum of 2000 months at a time.";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            return;
        }
        
        this.assetdetailIds = this.Assets.getValue();
        
        if(this.assetdetailIds=='') {
            this.assetdetailIds = "All";
        }
         var selectedAssetIds="";
         var selectedAssetIdsArray = [];
        if (this.grid.getSelectionModel().hasSelection()) {
            selectedAssetIdsArray = this.grid.getSelectionModel().getSelections();
            for (var cnt = 0; cnt < selectedAssetIdsArray.length; cnt++) {
                var n = selectedAssetIds.indexOf(selectedAssetIdsArray[cnt].data.assetDetailsId);
                if (n === -1) {
                    selectedAssetIds = selectedAssetIds + selectedAssetIdsArray[cnt].data.assetDetailsId + ",";
                }
            }
            if (selectedAssetIds != "" && selectedAssetIds.length>0) {
                selectedAssetIds = selectedAssetIds.substring(0, selectedAssetIds.length - 1);
            }
             
         }
        this.assetGroupIds = this.assetGroupCombo.getValue();
        var postOption = this.postOptions != undefined ? this.postOptions.getValue() : "0";
        var detail=this.getDepreciationData(this.gridStore);

        this.PostDateSettings = new Wtf.account.PostDateSettings({
            title:WtfGlobal.getLocaleText("acc.field.selectPostDate"),
            layout:'border',
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            height:120,
            assetdetailIds:this.assetdetailIds,
            selectedAssetIds:selectedAssetIds,
            detail: detail,
            postOption : postOption,
            gridStore:this.gridStore,
            width:320
        });
        this.PostDateSettings.show();   
    },
      
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
//    },
    
    beforerowselectGrid:function(sm1, rowIndex, a, rec){ 
        if(rec.data.isje || rec.data.isFuture || rec.data.differentPostOption) {
            return false;
        }
    },
    
    GenerateDepriciationData:function(){
        if(this.Assets.getRawValue().trim()==""||this.selectYear.getRawValue().trim()==""){
            WtfComMsgBox(107,0);
            return;
        }
        this.assetdetailIds = this.Assets.getValue();
        this.assetGroupIds = this.assetGroupCombo.getValue();
        this.fromMonth=this.startMonth.getValue();
        this.toMonth=this.endMonth.getValue();
        this.years=this.selectYear.getValue(); 
        this.gridStore.load({
            params:{
                assetdetailId:this.assetdetailIds,
                fromMonth:this.fromMonth,
                toMonth:this.toMonth,
                years : this.years,
                depreciationCalculationType:this.depreciationCalculationType,
                assetGroupIds : this.assetGroupIds
            }
        });
       
    }
});


// New TabRport to Unpost the Depreciation
Wtf.account.FixedAssetDepreciationUnpost = function(config){
    Wtf.apply(this, config);    

    Wtf.account.FixedAssetDepreciationUnpost.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.FixedAssetDepreciationUnpost, Wtf.Panel,{
    onRender: function(config){
        Wtf.account.FixedAssetDepreciationUnpost.superclass.onRender.call(this, config);
        
        this.depreciationCalculationType = Wtf.account.companyAccountPref.depreciationCalculationType;
        this.depreciationCalculationBasedOn = Wtf.account.companyAccountPref.depreciationCalculationBasedOn;
        
        this.finanDate = undefined;
        
        this.assetsComboRec = Wtf.data.Record.create ([
        {
            name:'assetdetailId'
        },{
            name:'assetGroup'
        },{
            name:'assetGroupId'
        },{
            name:'assetId'
        }]);
        
        this.assetsComboStore = new Wtf.data.Store({
            url:"ACCAsset/getAssetDetails.do",
            baseParams:{
                isDepreciationReport:true
            },
            reader: new  Wtf.data.KwlJsonReader({
                totalProperty: 'totalCount',
                root: "data"
            },this.assetsComboRec)
        }); 
        
        this.assetsComboStore.on('beforeload', function(store){
            WtfGlobal.setAjaxTimeOut();
        }, this);  

        this.assetsComboStore.on('load', function(store){
            var storeNewRecord=new this.assetsComboRec({
                assetdetailId:'All',
                assetId:'All',
                assetGroup:''
            });
            store.insert( 0,storeNewRecord);
            if(store.getCount()>0){
                this.Assets.setValue(store.getAt(0).data.assetdetailId);
            }
        }, this);   
        
        this.assetsComboStore.load();
        
        this.AssetsComboconfig = {       
            store: this.assetsComboStore,
            valueField:'assetdetailId',
            hideLabel:false,
            displayField:'assetId',
            emptyText:WtfGlobal.getLocaleText("erp.SelectAssets"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };

        this.Assets = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            fieldLabel:"Assets" ,
            forceSelection:true,   
            extraFields:['assetGroup'],
            extraComparisionField:'assetGroup',// type ahead search on acccode as well.
            listWidth:250,
            width:240
        },this.AssetsComboconfig));
     
        // to select the Year and month from the drop down list
        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'monthid',
                type:'int'
            }, 'name'],
            data :[[0,"January"],[1,"February"],[2,"March"],[3,"April"],[4,"May"],[5,"June"],[6,"July"],[7,"August"],[8,"September"],[9,"October"],
            [10,"November"],[11,"December"]]
        });

        var data=WtfGlobal.getBookBeginningYear(true, true);
    
        this.yearStore= new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'int'
            }, 'yearid'],
            data :data
        });
    
        if(this.depreciationCalculationType==0){  // if Average method selected is Yearly
            this.yearsComboconfig = {       
                store: this.yearStore,
                name:'startYear',
                displayField:'yearid',
                anchor:'95%',
                valueField:'yearid',
                emptyText:"Select Years",
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,
                triggerAction:'all',
                scope:this
            };
           
            this.selectYear = new Wtf.common.Select(Wtf.applyIf({
                multiSelect: true,
                fieldLabel: WtfGlobal.getLocaleText("acc.accPref.year"),
                forceSelection:true,   
                listWidth:250,
                width:90
            },this.yearsComboconfig));
            
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
                hidden: !this.depreciationCalculationType==0, // if Average method selected is not Yearly
                width:90,
                triggerAction: 'all',
                selectOnFocus:true
            });
            
            if(this.postOptions.getValue() == ""){
                this.postOptions.setValue(Wtf.postOptionStore.data.items[0].json[0]);
            }
        
            this.postOptions.on('select',function(){
                var month = this.assetDepreciationGrid.colModel.config[2];
                var year = this.assetDepreciationGrid.colModel.config[3];                
                if(this.postOptions.getValue()=="2"){
                    this.depreciationCalculationType = 2; // set post method as monthly to show data monthly distributed
                    month.hidden = false;
                    year.hidden = true;
                }else{
                    this.depreciationCalculationType = 0; // set post Method as Yearly to show data Yearly
                    month.hidden = true;
                    year.hidden = false;                
                }
                
                var monthVal = new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth();
                if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                    monthVal = new Date(Wtf.account.companyAccountPref.bbfrom).getMonth();
                }else{
                    monthVal = new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth();
                }
                if(monthVal != 0){
                    if(this.startMonth != undefined && this.endMonth != undefined){
                        this.startMonth.setValue(this.monthStore.data.items[monthVal].json[0]);
                        this.endMonth.setValue(this.monthStore.data.items[monthVal-1].json[0]);
                    }
                }         
                this.gridStore.load();
                this.assetDepreciationGrid.getView().refresh(true);
                this.doLayout();
            },this); 
            
        }else{    
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
        }        

        this.yearStore.on('load', function(store){
            if(store.getCount()>0){
                this.Assets.setValue(store.getAt(0).data.yearid);
                this.loaddata();
            }      
        }, this);   

        this.startMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'startMonth',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            hidden:this.depreciationCalculationType==0, // if Average method selected is Yearly
            width:90,
            triggerAction: 'all',
            selectOnFocus:true
        });  

        this.endMonth = new Wtf.form.ComboBox({
            store: this.monthStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month"),  //'Month',
            name:'endMonth',
            displayField:'name',
            forceSelection: true,
            anchor:'95%',
            valueField:'monthid',
            mode: 'local',
            hidden:this.depreciationCalculationType==0, // if Average method selected is Yearly
            triggerAction: 'all',
            width:90,
            selectOnFocus:true
        }); 
        
        if(this.selectYear.getRawValue()==""){
            var temp=new Date();
            var currentyear=temp.getFullYear();
            this.selectYear.setValue(""+currentyear+"");
        }
 
        // get date from month & year drop-down lists
        if (this.startMonth.getValue() == "" || this.endMonth.getValue() == ""){
            var fyear = undefined;
            if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                fyear = new Date(Wtf.account.companyAccountPref.bbfrom).getFullYear();
            }else{
                fyear = new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear();
            }
            
            if(this.selectYear.getRawValue()==fyear){
                var month = undefined;
                if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                    month = new Date(Wtf.account.companyAccountPref.bbfrom).getMonth();
                }else{
                    month = new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth();
                }
                if(month != 0){
                    this.startMonth.setValue(this.monthStore.data.items[month].json[0]);
                    for(var monthNo = 0; monthNo < month; monthNo++){
                        var record = this.monthStore.getAt(0);
                        this.monthStore.remove(record);
                    }
                } else {
                    this.startMonth.setValue(this.monthStore.data.items[0].json[0]);  
                }
            }else{
                this.startMonth.setValue(this.monthStore.data.items[0].json[0]);
            }
            this.endMonth.setValue(this.monthStore.data.items[this.monthStore.data.items.length-1].json[0]); 
        }
        
        this.selectYear.on('select',function(){
            var fyear = undefined;
            if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                fyear=new Date(Wtf.account.companyAccountPref.bbfrom).getFullYear()
            }else{
                fyear=new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear()
            }
            if(this.selectYear.getRawValue()==fyear){
                var month = undefined;
                if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                    month = new Date(Wtf.account.companyAccountPref.bbfrom).getMonth();
                }else{
                    month = new Date(Wtf.account.companyAccountPref.firstfyfrom).getMonth();
                }
                if(month != 0){
                    this.startMonth.setValue(this.monthStore.data.items[month].json[0]);
                    for(var monthNo = 0; monthNo < month; monthNo++){
                        var record = this.monthStore.getAt(0);
                        this.monthStore.remove(record);
                    }
                }
            }else{
                var data = [[0,"January"],[1,"February"],[2,"March"],[3,"April"],[4,"May"],[5,"June"],[6,"July"],[7,"August"],[8,"September"],[9,"October"],
                [10,"November"],[11,"December"]];
                this.monthStore.loadData(data);
                this.startMonth.setValue(this.monthStore.data.items[0].json[0]);
            }
        },this);
 
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
            hirarchical:true
        };
           
        this.assetGroupCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),
            forceSelection:true,   
            listWidth:300,
            width:180
        },this.assetGroupComboConfig));
 
        // Create Grid Record
        this.gridRec = Wtf.data.Record.create ([
        {
            name:'perioddepreciation'
        },{
            name:'accdepreciation'
        },{
            name:'netbookvalue'
        },{
            name:'netbookvalueInBase'
        },{
            name:'firstperiodamtInBase'
        },{
            name:'accdepreciationInBase'
        },{
            name:'currencysymbol'
        },{
            name:'currencyid'
        },{
            name:'depdetailid'
        },{
            name:'period'
        },{
            name:'firstperiodamt'
        },{
            name:'frommonth',
            type:'date'
        },{
            name:'isje'
        },{
            name:'isFuture'
        },{
            name:'tomonth',
            type:'date'
        },{
            name:'year',
            type:'date'
        },{
            name:'fromyear'
        },{
            name:'toyear'
        },{
            name:'assetId'
        },{
            name:'assetDetailsId'
        },{
            name:'assetGroupId'
        },{
            name:'isSelected'
        },{
            name:'jeid'
        },{
            name:'jeno'
        },{
            name:'jedate'
        },{
            name:'disposed'
        }]);

        this.msgLmt = 30;
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        }, this.gridRec);
                
        this.gridStore = new Wtf.data.GroupingStore({
            url: "ACCProductCMN/getAssetDepreciation.do",
            groupField:'assetId', 
            params:{
                assetdetailId:"All",
                fromMonth:this.startMonth.getValue(),
                toMonth:this.endMonth.getValue(),
                years : this.selectYear.getValue(),
                depreciationCalculationType:this.depreciationCalculationType,
                isUnpost : true,
                finanDate : this.finanDate,
                assetGroupIds : this.assetGroupIds
            },
            sortInfo: {
                field: 'assetId',
                direction: "DESC"
            },
            reader: this.gridStoreReader
        });
                
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : 'Loading...'
        });

        this.gridStore.on('loadexception',function(){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);

        this.gridStore.on('beforeload',function(){        
            var currentBaseParams = this.gridStore.baseParams;
            currentBaseParams.assetdetailId = this.Assets.getValue()=="" ? "All" : this.Assets.getValue();
            currentBaseParams.assetGroupIds = this.assetGroupCombo.getValue() == "" ? "All" : this.assetGroupCombo.getValue();
            currentBaseParams.fromMonth = this.startMonth.getValue();
            currentBaseParams.toMonth =this.endMonth.getValue();
            if(this.postOptions != undefined){
                if(this.postOptions.getValue() == 2){
                    var currentyear=this.selectYear.getValue(); 
                    if(this.selectYear.getValue().indexOf(""+(parseInt(currentyear)+1)) == -1){
                        this.selectYear.setValue(""+currentyear+","+(parseInt(currentyear)+1)+"");
                    }
                    if(this.depreciationCalculationBasedOn == Wtf.DEPRECIATION_BASED_ON_BOOK_BEGINNING_DATE){
                        this.finanDate =  WtfGlobal.convertToGenericStartDate(new Date(Wtf.account.companyAccountPref.bbfrom)); 
                    }else{
                        this.finanDate =  WtfGlobal.convertToGenericStartDate(new Date(Wtf.account.companyAccountPref.firstfyfrom)); 
                    }
                }
            }             
            currentBaseParams.years =this.selectYear.getValue(); 
            currentBaseParams.depreciationCalculationType=this.depreciationCalculationType;
            currentBaseParams.isUnpost = true;
            currentBaseParams.isGenerateAssetDepreciation =true; //This flag is used to java side calculating asset depreciation logic
            if(this.finanDate != undefined){
                currentBaseParams.finanDate = this.finanDate;
            }
            this.gridStore.baseParams=currentBaseParams;  
            WtfGlobal.setAjaxTimeOut();
            this.loadMask.show();
        },this);

        this.gridStore.on('load',function(){
            WtfGlobal.resetAjaxTimeOut();
            var arrdepreciated=[];
            this.arrRec = this.gridStore.getRange(0,this.gridStore.getCount()-1);
            for(var i=0;i<this.gridStore.getCount();i++){
                if(this.arrRec[i].data.isje){
                    arrdepreciated.push(this.arrRec[i]);
                }
            }
            WtfGlobal.highLightRowColor(this.assetDepreciationGrid,arrdepreciated,true,0,2);    
            this.loadMask.hide();
        },this);       

        //create Grid
        
        this.createFADepreciationGrid();
        this.gridStore.load({
            params:{
                isFirstTimeLoad:Wtf.isFirstTimeLoad
            }
        });
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [ {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.assetDepreciationGrid],
                tbar: [ WtfGlobal.getLocaleText("acc.reval.year"),this.selectYear, 
                this.depreciationCalculationType==0?"":WtfGlobal.getLocaleText("erp.FromMonth"),this.startMonth,
                this.depreciationCalculationType==0?"":WtfGlobal.getLocaleText("erp.ToMonth"),this.endMonth,
                "-",WtfGlobal.getLocaleText("acc.filed.SelectAssetGroups"),this.assetGroupCombo,
                "-",WtfGlobal.getLocaleText("erp.SelectAssets"),this.Assets,
                this.depreciationCalculationType==0 ? "Post Options" : "" ,this.depreciationCalculationType==0 ? this.postOptions:"",
                {
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                    tooltip:WtfGlobal.getLocaleText("acc.common.fetchTT"),
                    iconCls:'accountingbase fetch',
                    scope:this,
                    handler:this.GenerateDepriciationData
                },this.btnArr,
                '<span class="highlightrecordsred" style="margin: 0px 10px;">&nbsp;&nbsp;&nbsp;&nbsp;</span><span id="wtf-gen1092">'+WtfGlobal.getLocaleText("erp.depriciationhadposted")+'</span></span>'],
                bbar: ''                                                   //Need to implement paging in future
            //                    this.pagingToolbar = new Wtf.PagingSearchToolbar({
            //                        pageSize: 30,
            //                        id: "pagingtoolbar" + this.id,
            //                        store: this.gridStore,
            //                        searchField: this.quickPanelSearch,
            //                        displayInfo: true,
            //                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            //                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
            //                })
            }]
        }); 
        this.add(this.leadpan);
    },
    
    createFADepreciationGrid:function(){
        this.btnArr = [];
        this.unpostDepreciation = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("erp.fixedAssetDepreciationUnpost"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            scope:this,
            handler:this.unPostHandler
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedassetnew, Wtf.Perm.fixedassetnew.unpost)) { 
            this.btnArr.push(this.unpostDepreciation);
        }
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            multiSelect:true
        });
        
        this.sm.on("selectionchange",this.resetDepreciatedRows.createDelegate(this),this);
        
        this.colModel = new Wtf.grid.ColumnModel([this.sm,{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.period"),
            dataIndex:'period',
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.field.Month"),
            dataIndex:'frommonth',
            renderer:WtfGlobal.onlyMonthDeletedRenderer,
            hidden:this.depreciationCalculationType==0,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.field.Year"),
            dataIndex:'fromyear',
            hidden:this.depreciationCalculationType!=0,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.je.jeNumber"),
            dataIndex:'jeno',
            renderer:WtfGlobal.linkDeletedRenderer,
            width: 100,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.periodDep"),
            dataIndex:'firstperiodamt',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.accDep"),
            dataIndex:'accdepreciation',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            align:'center',
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("acc.fixedAssetList.netBookVal"),
            dataIndex:'netbookvalue',
            align:'center',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
            pdfwidth:75
        },{
            header:WtfGlobal.getLocaleText("erp.Assets"),//'Asset Group',
            dataIndex:'assetId',
            hidden:true
        }]);
    
        this.colModel.defaultSortable = false;

        this.assetDepreciationGrid = new Wtf.grid.GridPanel({
            cm:this.colModel,
            store:this.gridStore,
            sm:this.sm,
            stripeRows :true,
            border:false,
            layout:'fit',
            view: new Wtf.grid.GroupingView({
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.asset.depreciation.alert.msg"))
            }),
            plugins :[Wtf.ux.grid.plugins.GroupCheckboxSelection]
        });
        
        this.assetDepreciationGrid.on('cellclick',this.onCellClick, this);
    },
    
    onCellClick:function(g,i,j,e){
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="jeno"){
            var accid = this.gridStore.getAt(i).data['jeid'];
            Wtf.onCellClick(accid,this.gridStore.getAt(i).data['jedate'],this.gridStore.getAt(i).data['jedate']);
        }
    },
    
    getDepreciationData : function(store,assetId){
        var selected=this.assetDepreciationGrid.getSelectionModel().getSelections();
        for(var i=0;i<selected.length;i++){
            selected[i].data.isSelected=true;
        }
        var arr=[];
        store.each(function(rec){
            if((!rec.data.isFuture && rec.data.isSelected))
                arr.push(store.indexOf(rec));
        },this);
        return WtfGlobal.getJSONArray(this.assetDepreciationGrid,true,arr);
    },

    unPostHandler:function(){
        if(!this.assetDepreciationGrid.getSelectionModel().hasSelection()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.NoDatatounpost")],2);
            return;
        }
         /*
          * "Sorry! you can un post post depreciation for maximum of 2000 months at a time.";
         */
        if(this.assetDepreciationGrid.getSelectionModel().getSelections().length > Wtf.Max_Depreciation_Months){
            var msg = WtfGlobal.getLocaleText("acc.fixed.asset.unpostdepreciation.limit.msg");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            return;
        }
        var rec=[];
        this.assetdetailIds =this.Assets.getValue();
        
        var selected=this.assetDepreciationGrid.getSelectionModel().getSelections();
        var isDisposed = false;
        for(var i=0;i<selected.length;i++){
            if(selected[i].data.disposed){
                isDisposed = true;
                break;
            }
        }
        if(isDisposed){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.fixed.asset.unpost.alert")],2);
            return;
        }
        
        var selectedAssetIds="";
        var selectedAssetIdsArray = [];
        if (this.assetDepreciationGrid.getSelectionModel().hasSelection()) {
            selectedAssetIdsArray = this.assetDepreciationGrid.getSelectionModel().getSelections();
            for (var cnt = 0; cnt < selectedAssetIdsArray.length; cnt++) {
                var n = selectedAssetIds.indexOf(selectedAssetIdsArray[cnt].data.assetDetailsId);
                if (n === -1) {
                    selectedAssetIds = selectedAssetIds + selectedAssetIdsArray[cnt].data.assetDetailsId + ",";
                }
            }
            if (selectedAssetIds !== "" && selectedAssetIds.length>0) {
                selectedAssetIds = selectedAssetIds.substring(0, selectedAssetIds.length - 1);
            }
             
         }
        
        var detail=this.getDepreciationData(this.gridStore);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("erp.fixedAssetDepreciationUnpost"),WtfGlobal.getLocaleText("erp.unpostderpeciationconfirm"),function(btn){
            if(btn!="yes") return;
            WtfComMsgBox(27,4,true);
            rec.assetdetailIds=this.assetdetailIds;
            rec.assetGroupIds=this.assetGroupIds;
            rec.selectedAssetIds = selectedAssetIds;
            rec.detail=detail;
            Wtf.Ajax.timeout = WtfGlobal.setAjaxTimeOut();
            Wtf.Ajax.requestEx({
                url: "ACCProductCMN/deleteAssetDepreciation.do",
                params: rec
            },this,this.genDepUnPostSuccessResponse,this.genDepFailureResponse);
        },this);
    },
    
    genDepUnPostSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.fixedAssetList.UnpostDep"),response.msg],response.success*2+1);
        if(response.success){
            this.gridStore.reload();
            getCompanyAccPref();
        }
    },
    
    genDepFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    resetDepreciatedRows:function(){
        var arr=this.sm.getSelections();
        for(var i=0;i<arr.length;i++){
            if(arr[i]&&arr[i].data.isFuture){
                this.sm.clearSelections();
            }
        }
    },

    GenerateDepriciationData:function(){
        if(this.Assets.getRawValue().trim()==""||this.selectYear.getRawValue().trim()==""){
            WtfComMsgBox(107,0);
            return;
        }
        this.assetGroupIds = this.assetGroupCombo.getValue();
        this.assetdetailIds =this.Assets.getValue();
        this.fromMonth=this.startMonth.getValue();
        this.toMonth=this.endMonth.getValue();
        this.years=this.selectYear.getValue(); 
        this.gridStore.load({
            params:{
                assetdetailId:this.assetdetailIds,
                fromMonth:this.fromMonth,
                toMonth:this.toMonth,
                years : this.years,
                depreciationCalculationType:this.depreciationCalculationType,
                assetGroupIds : this.assetGroupIds
            }
        });       
    }
});


// Post Date Setting Window

Wtf.account.PostDateSettings = function(config){
    this.detail= config.detail;
    this.assetdetailIds = config.assetdetailIds;
    this.gridStore = config.gridStore; 
    this.postOption = config.postOption;
    this.assetGroupIds = config.assetGroupIds;
    this.selectedAssetIds = config.selectedAssetIds;
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.submit"),
            minWidth: 50,
            scope: this,
            handler: this.saveForm.createDelegate(this)
        }),this.closeButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
            minWidth: 50,
            scope: this,
            handler: this.closeOpenWin.createDelegate(this)
        })]
    },config);
    
    Wtf.account.PostDateSettings.superclass.constructor.call(this, config); 
}

Wtf.extend(Wtf.account.PostDateSettings, Wtf.Window,{
    
    onRender:function(config){
        Wtf.account.PostDateSettings.superclass.onRender.call(this,config);              
        this.createPostDateSettings();               
        this.add({
            region: 'center',
            border: false,
            layout:'fit',
            baseCls:'bckgroundcolor',
            items:[this.PostDateSettingsForm]
        });
    },
  
    createPostDateSettings:function(){        
        this.postDate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.selectPostDate"),//'select post date',
            name: 'postdate',
            id: "postdate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:false
        });
        this.postDate.setValue(new Date());
        //this.postDate.setValue(Wtf.serverDate);
        
        this.PostDateSettingsForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            autoHeight:true,
            anchor:'100%',
            labelWidth:150,
            bodyStyle:'margin:10px',
            items:[this.postDate]
        });        
    },    
  
    closeOpenWin:function(){
        this.close();
    },
    
    saveForm:function(){
        this.saveButton.disable();
        var rec=[];
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.rem.11"),function(btn){
            if (btn != "yes") {
                this.saveButton.enable();
                return;
            }
            WtfComMsgBox(27,4,true);
            rec.assetdetailIds=this.assetdetailIds;
            rec.detail=this.detail;
            rec.postdate=WtfGlobal.convertToGenericDate(this.postDate.getValue());
            rec.postOption = this.postOption;
            rec.assetGroupIds = this.assetGroupIds
            rec.selectedAssetIds = this.selectedAssetIds
            Wtf.Ajax.timeout = WtfGlobal.setAjaxTimeOut();
            Wtf.Ajax.requestEx({
                url: "ACCProductCMN/saveAssetDepreciation.do",
                params: rec
            },this,this.genDepSuccessResponse,this.genDepFailureResponse);
        },this);
    },
          
    genDepSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.fixedAssetList.postDep"),response.msg],response.success*2+1);
        if(response.success){
            this.gridStore.reload();
            this.close();
            getCompanyAccPref();
        }
    },

    genDepFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
})
