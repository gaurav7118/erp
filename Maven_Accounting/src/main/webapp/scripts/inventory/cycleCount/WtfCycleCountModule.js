
function callCycleCountForm(){
    if(!Wtf.account.companyAccountPref.activateCycleCount) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),"You can not view cycle count form. Please activate Cycle Count from Company Preferences."],3);
        return;
    }
    var mainTabId = Wtf.getCmp("as");
    var cycleCountTab = Wtf.getCmp("CycleCountFormTabId");
    if(cycleCountTab == null){
        cycleCountTab = new Wtf.inventory.cycleCountPanel({
            layout:"fit",
            title:WtfGlobal.getLocaleText("acc.inventoryList.CycleCountForm"),
            iconCls: getButtonIconCls(Wtf.etype.addcyclecounttab),
            closable:true,
            border:false,
            id:"CycleCountFormTabId"
        });
        mainTabId.add(cycleCountTab);
    }
    mainTabId.setActiveTab(cycleCountTab);
    mainTabId.doLayout();
}
function callCycleCountReport(){
    if(!Wtf.account.companyAccountPref.activateCycleCount) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),"You can not view cycle count report. Please activate Cycle Count from Company Preferences."],3);
        return;
    }
    var mainTabId = Wtf.getCmp("as");
    var cycleCountTab = Wtf.getCmp("CycleCountReportTabId");
    if(cycleCountTab == null){
        cycleCountTab = new Wtf.CCReportTab({
            layout:"fit",
            title:WtfGlobal.getLocaleText("acc.productList.cycleCountReport"),
            iconCls: getButtonIconCls(Wtf.etype.cyclecountreporttab),
            closable:true,
            border:false,
            id:"CycleCountReportTabId"
        });
        mainTabId.add(cycleCountTab);
    }
    mainTabId.setActiveTab(cycleCountTab);
    mainTabId.doLayout();
}
function callCycleCountCalendar(){
    if(!Wtf.account.companyAccountPref.activateCycleCount) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.rem.cannotactivatecyclecountcalendar")],3);
        return;
    }
    var mainTabId = Wtf.getCmp("as");
    var cycleCountCalendarTab = Wtf.getCmp("CycleCountCalendarTabId");
    if(cycleCountCalendarTab == null) {
        cycleCountCalendarTab = new Wtf.cycleCountCalendarTab({
            title: WtfGlobal.getLocaleText("acc.inventoryList.CycleCountCalendar"),
            id: "CycleCountCalendarTabId",
            iconCls: getButtonIconCls(Wtf.etype.countcyclecounttab),
            closable: true,
            border: false,
            layout: "fit"
        });
        mainTabId.add(cycleCountCalendarTab);
    }
    mainTabId.setActiveTab(cycleCountCalendarTab);
    mainTabId.doLayout();
}

Wtf.inventory.cycleCountPanel = function(config){
    Wtf.apply(this, config);
    Wtf.inventory.cycleCountPanel.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.inventory.cycleCountPanel, Wtf.Panel, {
    initComponent:function(){
        this.draftId = "";
        this.moduleid=Wtf.Acc_CycleCount_ModuleId;
        this.revertCCSubmit=0;
        this.frequency = "";
        var trackStoreLocation= true
        this.today = new Date();
        this.today.setHours(23);
        this.today.setMinutes(59);
        this.today.setSeconds(59);
        this.countDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.stock.BusinessDate")+"*",
            emptyText: WtfGlobal.getLocaleText("acc.stock.Selectadate"),
            width: 200,
            format: 'Y-m-d l',
            allowBlank: false,
            name: "countingdate1",
            value: new Date(),
            readOnly: true,
            maxValue: this.today
        });

        this.storeId = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.threshold.grid.storeid")+"*",
            emptyText: WtfGlobal.getLocaleText("acc.stock.Selectadate"),
            width: 200,
            allowBlank: false,
            name: "storeid"
        });
        this.documentNumber = new Wtf.form.TextField({
            fieldLabel:'Document No',
            name:'documentNo',
            maxLength:50,
            width : 200
        });
        this.sequenceFormatNO = new Wtf.SeqFormatCombo({
            seqNumberField : this.documentNumber,
            fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.sequenceformat"),
            name:"seqFormat",
            moduleId:4,
            allowBlank:false,
            width : 200
        });
        
        if(trackStoreLocation){
            
            this.locCmbRecord = new Wtf.data.Record.create([{
                name: 'id'
            },{
                name: 'name'
            }]);

            this.locCmbStore = new Wtf.data.Store({
                url:  'INVStore/getStoreLocations.do',
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                },this.locCmbRecord)
            });
	
        
            this.locCmb = new Wtf.form.ComboBox({
                fieldLabel : 'Location*',
                hiddenName : 'locationid',
                store : this.locCmbStore,
                typeAhead:true,
                displayField:'name',
                valueField:'id',
                mode: 'local',
                width : 200,
                triggerAction: 'all',
                emptyText:'Select location...',
                allowBlank:false
            });
        } 


        this.locCmbStore.on("beforeload", function(){
            this.locCmbStore.removeAll();
            this.locCmb.reset(); 
        }, this);
            
        this.storeCmbRecord = new Wtf.data.Record.create([{
            name: 'store_id'
        },{
            name: 'abbrev'
        },{
            name: 'fullname'
        }]);
        var globalRoleid=true;
        this.storeCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreListByUser.do',
            sortInfo: {
                field: 'description',
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            },
            baseParams:{
                isActive:true,
                excludeQARepair: true,
                isFromInvTransaction:true //ERM-691 do not show Scrap/Repair Stores in Inv transactions
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, this.storeCmbRecord)
        });
        this.storeCmbStore.load();

        this.storeCmb = new Wtf.form.ComboBox({
            fieldLabel : WtfGlobal.getLocaleText("acc.stock.Store*"),
            hiddenName : 'storeid',
            store : this.storeCmbStore,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            width : 200,
            triggerAction: 'all',
            emptyText:'Select count store...',
            allowBlank:false,
            forceSelection:true,
            typeAhead:true,
            listWidth:300,
            tpl: new Wtf.XTemplate(
                '<tpl for=".">',
                '<div wtf:qtip = "{[values.fullname]}" class="x-combo-list-item">',
                '<div>{fullname}</div>',
                '</div>',
                '</tpl>')
        });
        
        this.storeCmb.on("select",function(){
            if(this.storeCmb.getValue() !=null) {
                
                this.loadItemList();   
            } 
            
        },this);


        //        this.storeCmbStore.on("load", function(ds, rec, o){
        //            if(rec.length > 0) {
        //                this.storeCmb.setValue(rec[0].data.store_id);
        //            // this.setBusinessDate();
        //            }
        //        }, this);

        //        this.storeCmb.on("select", function(){
        //            //  this.setBusinessDate();
        //            }, this);

        this.itemlistRecord = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'code'
        },{
            name: 'frequency'
        },{
            name: 'subcategory'
        },{
            name: 'name'
        },{
            name: 'packaging'
        },{
            name: 'casinguom'
        },{
            name: 'inneruom'
        },{
            name: 'looseuom'
        },{
            name: 'casinguomval'
        },{
            name: 'inneruomval'
        },{
            name: 'looseuomval'
        },{
            name: 'casinguomcnt'
        },{
            name: 'variancealert'
        },{
            name: 'variance'
        },{
            name: 'inneruomcnt'
        },{
            name: 'looseuomcnt'
        },{
            name: 'expquantity'
        },{
            name: 'actualqty'
        },{
            name: 'reason'
        },{
            name: 'added',
            mapping: 'extraItem'
        },{
            name: 'threshold'
        },{
            name:'sysqty'
        },{
            name:'edited'
        },{
            name:"isRowForProduct"
        },{
            name:"isRackForProduct"
        },{
            name:"isBinForProduct"
        },{
            name:"isBatchForProduct"
        },{
            name:"isSerialForProduct"
        },{
            name:"isSkuForProduct"
        },{
            name:"stockDetails"
        },{
            name:"stockDetailQuantity"
        },{
            name:"draftDetail"
        },{
            name:"currentsysqty"
        },{
            name: 'customfield'
        }]);
    
   
        this.itemlistReader = new Wtf.data.KwlJsonReader({
            root: 'data'
        }, this.itemlistRecord);
        
        var grpView = new Wtf.grid.GroupingView({
            forceFit: true,
//            startCollapsed:true,
            showGroupName: true,
            enableGroupingMenu: false,
             hideGroupedColumn: true
        });

        this.itemlistStore = new Wtf.data.GroupingStore({
            sortInfo: {
                field: 'subcategory',
                direction: "ASC"
            },
            groupField:"added",
            url:  'INVCycleCount/getCCItemList.do',
            baseParams:{
                flag:3
            },
            reader: this.itemlistReader
        });

        this.MOUTextField = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.je.MoD")+"*",
            readOnly: true,
            name: "trans mod",
            value:loginname,
            width: 200
        });
        this.itemlistStore.on('loadexception',function(){
            this.el.unmask();
        },this)
        var colModelArray = [];
        colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.itemlistStore);
        this.itemlistStore.on("load",function(){
          
            if(this.itemlistStore.getCount()<=0){
                if(this.ccFrequencies){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stock.cyclecountfrequencyalert1")+" "+this.ccFrequencies+"." +WtfGlobal.getLocaleText("acc.stock.cyclecountfrequencyalert2")], 0);
                }else{
                    WtfComMsgBox(["Info", "For this date, cycle count frequency is not set."], 0);
                }
                
            }
        },this)
        this.sm = new Wtf.grid.RowSelectionModel({
            singleSelect:true
        });
        var customArr = [];
        customArr.push(          
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.het.18"),
                dataIndex: 'code',
                renderer: function(v){
                    return '<div wtf:qtip="'+v+'">'+v+'</div>';
                }
            },{
                header: WtfGlobal.getLocaleText("acc.contractDetails.ItemName"),
                dataIndex: 'name',
                renderer: function(v){
                    return '<div wtf:qtip="'+v+'">'+v+'</div>';
                }
            },{
                header: WtfGlobal.getLocaleText("acc.product.packaging"),
                dataIndex: 'packaging',
                hidden : Wtf.account.companyAccountPref.UomSchemaType?false:true,  // column is visible only when packaging schema is activated in sytem control
                renderer: function(v){
                    return '<div wtf:qtip="'+v+'">'+v+'</div>';
                }
            },{
                header: WtfGlobal.getLocaleText("acc.product.casingUOM"),
                dataIndex: 'casinguomcnt',
                width:70,
                align:'right',
                hidden : Wtf.account.companyAccountPref.UomSchemaType?false:true,  // column is visible only when packaging schema is activated in sytem control
                renderer: function(a, b, c, d, e, f){
                    if(c.data.casinguom != "-"){
                        b.attr = 'style="background-color:#eee;"';
                    }
                    return a + "    " + c.get("casinguom");
                },
                editor: this.cnum = new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
//                    listeners: {
//                        'focus': setZeroToBlank
//                    }
                })
            },{
                header: WtfGlobal.getLocaleText("acc.product.innerUOM"),
                dataIndex: 'inneruomcnt',
                width:70,
                align:'right',
                hidden : Wtf.account.companyAccountPref.UomSchemaType?false:true, // column is visible only when packaging schema is activated in sytem control
                renderer: function(a, b, c, d, e, f){
                    if(c.data.inneruom != "-"){
                        b.attr = 'style="background: #eee;"';
                    }
                    return a + "    " + c.get("inneruom");
                },
                editor: this.cnum = new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
//                    listeners: {
//                        'focus': setZeroToBlank
//                    }
                })
            },{
                header: WtfGlobal.getLocaleText("acc.stock.LooseUoM"),
                dataIndex: 'looseuomcnt',
                width:70,
                align:'right',
                editor: this.cnum = new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    decimalPrecision : Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
                    selectOnFocus: true
//                    listeners: {
//                        'focus': setZeroToBlank
//                    }
                }),
                renderer: function(a, b, c, d, e, f){
                    if(c.data.looseuom != "-"){
                        b.attr = 'style="background: #eee;"';
                    }
                    return a + "    " + c.get("looseuom");
                },
                
            },{
                header: WtfGlobal.getLocaleText("acc.field.ActualQuantity"),
                dataIndex: 'expquantity',
                width:70,
                scope: this,
                align:'right',
                renderer: this.getActualQty.createDelegate(this)
            },{
                header: WtfGlobal.getLocaleText("acc.stock.SystemQuantity"),
                dataIndex: 'sysqty',
                width:70,
                scope: this,
                align:'right',
                renderer: function(val, meta, rec){
                     var v = String(val);
                    var ps = v.split('.');
                    var sub = ps[1];
                    if (sub != undefined && sub.length > 0) {
                        val= (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    } else{
                    return val + ' ' + rec.get('looseuom');
                }
                    return val + ' ' + rec.get('looseuom');
                }
            },{
                header: '',
                dataIndex:"",
                renderer: this.serialRenderer.createDelegate(this),
                width:40  
            },{
                header: WtfGlobal.getLocaleText("acc.field.Variance"),
                dataIndex: 'variance',
                width:70
                ,
                renderer: function(a, b, c){
                    if(c.get('edited') === true){
                        if(c.get('actualqty') === parseFloat(getRoundofValue(c.data.sysqty))){
                            c.set('variance','No');
                            return "No";
                        } else if(c.get('actualqty') > c.get('sysqty') || c.get('actualqty') < c.get('sysqty') ) {
                            c.set('variance','Yes');
                            return "<span style='color:red'>Yes</span>";
                        } else {
                            return "Yes";
                        }
                    }
                }
            },
            {
                header: WtfGlobal.getLocaleText("acc.cc.5"),
                dataIndex: 'variance',
                align:'right',
                renderer: function(a, b, c){
                    if(c.get('edited') === true){
                    var val=c.get('actualqty')-c.get('sysqty');
                    var v = String(val);
                    var ps = v.split('.');
                    var sub = ps[1];
                    if (sub != undefined && sub.length > 0) {
                        val= (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) == "NaN") ? parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) : parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
                    } else{
                        return val + ' ' + c.get('looseuom');
                    }
                        return val + " " + c.get("looseuom");
                    }else{
                        return '';
                    }
                
                }
            },
            {
                header:WtfGlobal.getLocaleText("acc.dnList.reason"),
                dataIndex:"reason",
                //            width:200,
                editor: new Wtf.form.TextField({
                    selectOnFocus: true
                })
            },
            {
                header:WtfGlobal.getLocaleText("acc.stock.ExtraItem"),
                dataIndex:"added",
                renderer: function(v){
                    if(v == true){
                        return "Yes";
                    }else{
                        return "No";
                    }
                }

            //renderer: Wtf.ux.comboBoxRenderer(this.reasonCombo)
            });
        customArr = WtfGlobal.appendCustomColumn(customArr, GlobalColumnModel[this.moduleid], undefined, undefined, this.readOnly, this.isViewTemplate);
        this.itemlistCm = new Wtf.grid.ColumnModel(customArr);
        this.itemlistCm.defaultSortable = true;

        this.addItem = new Wtf.menu.Item({
            text: WtfGlobal.getLocaleText("acc.stock.AddExtraItem"),
            id : "additem" + this.id,
            allowDomMove: false,
            iconCls:getButtonIconCls(Wtf.etype.menuadd),
            scope: this,
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.stock.AddExtraItem"),
                text: WtfGlobal.getLocaleText("acc.stock.Toaddextraitemforcyclecount")
            },
            handler: function() {
                this.addWindow(1);
            }
        });
        this.deleteItem = new Wtf.menu.Item({
            text: WtfGlobal.getLocaleText("acc.stock.DeleteExtraItem"),
            id: "delitem" + this.id,
            allowDomMove: false,
            scope: this,
            //disabled: true,
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.stock.DeleteExtraItem"), 
                text: WtfGlobal.getLocaleText("acc.stock.DeleteExtraItemtooltip")
            },
            handler: function() {
                var rec = this.itemlistGrid.getSelectionModel().getSelected();

                if(!rec){
                    WtfComMsgBox(["Request", "Please select a record"], 1);
                    return;
                }
                if(!rec.data.added){
                    WtfComMsgBox(["Info", "Only extra added items can be removed"], 0);
                    return;
                }
                Wtf.MessageBox.confirm('Confirm', "Are you sure you want to delete record?", function(btn){
                    if (btn == 'yes') {
                        this.itemlistGrid.store.remove(rec);
                    // this.setTotalAmount();
                    }
                },this);

            }
        });
        this.edititem=new Wtf.menu.Item({
            text: "Edit Extra Item",
            id: "edititem" + this.id,
            allowDomMove: false,
            scope: this,
            iconCls:getButtonIconCls(Wtf.etype.menuedit),
            tooltip: {
                title: 'Edit Extra Item', 
                text: 'To Edit the extra item added.'
            },
            handler: function() {
                this.addWindow(2);
            }

        });

        this.gridwrapper = new Wtf.Panel({
            layout:'fit',
            border: false,
            items:this.itemlistGrid = new Wtf.grid.EditorGridPanel({
                store: this.itemlistStore,
                sm: this.sm,
                cm: this.itemlistCm,
                loadMask : true,
//                layout:'fit',
                viewConfig: {
                    forceFit: true
                },
                view: grpView,
                clicksToEdit: 1,
                bbar:[{
                    xtype:"button",
                    text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    iconCls:getButtonIconCls(Wtf.etype.save),
                    id: "confirmCCount",
                    //                    disabled: true,
                    scope: this,
                    tooltip: {
                        title: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                        text:WtfGlobal.getLocaleText("acc.stock.Savecyclecount")
                    },
                    handler:function(){
                        this.confirmItemList(false);
                    }
                },"-",{
                    xtype:"button",
                    text:WtfGlobal.getLocaleText("acc.stock.Viewdraft"),
                    iconCls:'pwnd view',
                    tooltip: {
                        title: WtfGlobal.getLocaleText("acc.wtfTrans.vvi"),
                        text:WtfGlobal.getLocaleText("acc.stock.Viewlistofsavedform")
                    },
                    id:'viewccdrafts',
                    scope: this,
                    handler:function(){
                        this.createDraftListWin();
                    }
                },"-",{
                    xtype:"button",
                    text:WtfGlobal.getLocaleText("acc.stock.Saveindraft"),
                    iconCls:getButtonIconCls(Wtf.etype.save),
                    tooltip: {
                        title: WtfGlobal.getLocaleText("acc.het.108"),
                        text:WtfGlobal.getLocaleText("acc.stock.Saveformindraft")
                    },
                    id:'saveccdrafts',
                    scope: this,
                    handler:function(){
                        
                        this.confirmItemList(true);
                    }
                },"-",{
                    xtype: "button",
                    text: WtfGlobal.getLocaleText("acc.stock.ExtraItem"),
                    iconCls: 'accountingbase product',//Change image
                    tooltip: {
                        title: WtfGlobal.getLocaleText("acc.stock.ExtraItem"),
                        text: WtfGlobal.getLocaleText("acc.stock.Tomanageextraitemforcyclecount")
                    },
                    id: 'addextraitems',
                    scope: this,
                    menu: [
                    this.addItem,
                    this.edititem,
                    this.deleteItem
                    ]
                 },"-",{
                     xtype:"button",
                    text:WtfGlobal.getLocaleText("acc.stock.Refresh"),
                    iconCls:getButtonIconCls(Wtf.etype.resetbutton),
                    scope: this,
                    tooltip: {
                        title: WtfGlobal.getLocaleText("acc.stock.Refresh"),
                        text:WtfGlobal.getLocaleText("acc.stock.RefreshForm")
                    },
                    handler:function(){
                        this.refreshForm();
                    }
                },{
                    text:WtfGlobal.getLocaleText("acc.common.print"),
                    iconCls: 'pwnd printButtonIcon',
                    handler:this.printBlankSheet,
                    scope:this
                }
                ]
            })
        });
        this.itemlistGrid.on("beforeedit", this.validateGridEdit, this);
        this.itemlistGrid.on("afteredit", this.gridAfterEdit, this);
        this.itemlistGrid.on("cellclick",this.cellClick,this);
        this.countDate.on("change", function(){
            this.getCCFrequenciesForBusinessDate(true);
        }, this);
        this.getCCFrequenciesForBusinessDate(false);
    },
    getCCFrequenciesForBusinessDate:function(isLoadItem, isDraft){
        Wtf.Ajax.requestEx({
            url: "INVCycleCount/getCycleCountFrequencyForDate.do",
            params: {
                countdate : this.countDate.getValue().format("Y-m-d")
            }
        }, this,
        function(action) {
            var dataArr=action.data;
            if(dataArr.length > 0){
                this.ccFrequencies = dataArr[0].frequency
            }
            if(isLoadItem){
                this.loadItemList(isDraft);
            }
        },
        function(){
            this.ccFrequencies = "";
            if(isLoadItem){
                this.loadItemList(isDraft);
            }
        }
        )
    },
    setBusinessDate:function(){
        Wtf.Ajax.requestEx({
            url: "INVCycleCount/validateCycleCountDate.do",
            params: {
                storeId: this.storeCmb.getValue(),
                countDate : this.countDate.getValue().format("Y-m-d")
            }
        }, this,
        function(action) {
           
            },
            function(){
            }
            )
    },
    getNewColumnModel:function (val){
        var newcm = [];
        var integrationFeatureFor=true
        if(val == "Update") {
            newcm = [new Wtf.grid.RowNumberer(),
            {
                header: "Item Code",
                dataIndex: 'code'
            },{
                header: "Item Name",
                dataIndex: 'description'
            },{
                header: "Micros Key",
                dataIndex: 'microskey2',
                hidden:true
            },{
                header:"Item Category",// (integrationFeatureFor === Wtf.IF.SUSHITEI)?"Item Category":"Order Category",
                dataIndex: 'subcategory',
                hidden:true
            },{
                header: "Packaging",
                dataIndex: 'packaging',
                width:70
            },{
                header: "Casing UoM",
                dataIndex: 'casinguomcnt',
                width:70,
                renderer: function(a, b, c, d, e, f){
                    if(c.data.casinguom != "-")
                        b.attr = 'style="background: #eee;"';
                    return a + "    " + c.get("casinguom");
                },
                editor: this.cnum = new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })
            },{
                header: "Inner UoM",
                dataIndex: 'inneruomcnt',
                width:70,
                renderer: function(a, b, c){
                    if(c.data.inneruom != "-")
                        b.attr = 'style="background: #eee;"';
                    return a + "    " + c.get("inneruom");
                },
                editor: new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })

            },{
                header: "Loose UoM",
                dataIndex: 'looseuomcnt',
                width:70,
                renderer: function(a, b, c){
                    if(c.data.looseuom != "-")
                        b.attr = 'style="background: #eee;"';
                    return a + "    " + c.get("looseuom");
                },
                editor: new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })
            },{
                header: "System Quantity",
                dataIndex: 'sysqty',
                width:70,
                scope: this,
                renderer: function(val, meta, rec){
                    return val + ' ' + rec.get('looseuom');
                }
            },{
                header: "Actual Quantity",
                sortable:true,
                dataIndex: 'expquantity',
                width:70,
                scope: this,
                renderer:this.getActualQty.createDelegate(this)
            },{
                header: "Variance",
                dataIndex: 'variance',
                width:70,
                renderer: function(a, b, c){
                    if(c.data.edited === true){
                        if(c.data.actualqty === c.data.sysqty ){
                            c.set('variance','No');
                            return "No";
                        } else if(c.data.actualqty > c.data.sysqty || c.data.actualqty < c.data.sysqty ) {
                            c.set('variance','Yes');
                            return "<span style='color:red'>Yes</span>";
                        } else {
                            return "Yes";
                        }
                    
                    }else{
                        c.set('variance','No');
                        return "No"
                    }
                }
            },
            {
                header: "Variance(Qty)",
                dataIndex: 'variance',
                align:'right',
                renderer: function(a, b, c){
                    return c.data.actualqty-c.data.sysqty + " " + c.get("looseuom");
                }
            },
            {
                header:"Reason",
                dataIndex:"reason",
                width:200,
                editor: new Wtf.form.TextField({
                    selectOnFocus: true
                })
            }
            ];
        } 

        else if(val == "Reset") {
            newcm = [new Wtf.grid.RowNumberer(),
            {
                header: "Item Code",
                dataIndex: 'code'
            },{
                header: "Item Description",
                dataIndex: 'description'
            },{
                header: "Micros Code",
                dataIndex: 'microskey1',
                hidden:true
            },{
                header:"Item Category",// (integrationFeatureFor === Wtf.IF.SUSHITEI)?"Item Category":"Order Category",
                dataIndex: 'subcategory',
                hidden:true
            },{
                header: "Packaging",
                dataIndex: 'packaging',
                width:70
            },{
                header: "Casing UoM",
                sortable:true,
                dataIndex: 'casinguomcnt',
                width:70,
                renderer: function(a, b, c, d, e, f){
                    if(c.data.casinguom != "-")
                        b.attr = 'style="background: #eee;"';
                    return a + "    " + c.get("casinguom");
                },
                editor: this.cnum = new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })
            },{
                header: "Inner UoM",
                dataIndex: 'inneruomcnt',
                width:70,
                renderer: function(a, b, c){
                    if(c.data.inneruom != "-")
                        b.attr = 'style="background: #eee;"';
                    return a + "    " + c.get("inneruom");
                },
                editor: new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })
            },{
                header: "Loose UoM",
                dataIndex: 'looseuomcnt',
                width:70,
                renderer: function(a, b, c){
                    if(c.data.looseuom != "-")
                        b.attr = 'style="background: #eee;"';
                    return a + "    " + c.get("looseuom");
                },
                editor: new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })
            },{
                header: '',
                dataIndex:"",
                renderer: this.serialRenderer.createDelegate(this),
                width:40  
            }];
        }
        this.itemlistCm.setConfig(newcm);
        this.itemlistCm.defaultSortable = true;
    },


    getQuantity: function(val) {
        var rec = this.itemlistStore.getAt(this.itemlistStore.getCount() -1);
        Wtf.Ajax.requestEx({
            url: 'jspfiles/inventory/store.jsp',
            method: 'POST',
            params: {
                flag: 7,
                storeid: this.storeCmb.getValue(),
                itemid: val
            }
        }, this,
        function(resp) {
            var exp = parseFloat(resp);
            rec.set('expquantity', exp);
            rec.set('variance', rec.data.actualqty - exp);
        }, function() {
            });
    },

    validateGridEdit : function(e) {
        var field = e.field;
        if(((field == 'casinguomcnt' || field == 'inneruomcnt'  || field == 'looseuomcnt' )
            && e.record.get((field).substr(0, (field).length - 3)) == '-')
        || (e.record.data.orderingcategoryname != '' && field == 'id')) {
            e.cancel = true;
        }
        this.row = e.row;
    // e.record.set("edited", true);
    //e.record.commit();
    //        if(e.grid.getStore().getCount() == (e.row + 1)) {
    //            this.addNewRow();
    //        }
    },
    
    gridAfterEdit : function(e) {
        if(e.record && e.record.get('currentsysqty') == 0 && e.record.get('actualqty') == 0){
            e.record.set("edited", false);
        }else{
            e.record.set("edited", true);
        }
    //e.record.commit();
    },

    addNewRow: function() {
        this.itemlistStore.add(new this.itemlistRecord({
            id: '',
            code: '',
            poscode: '',
            description: '',
            packaging: '',
            casinguom: '-',
            inneruom: '-',
            looseuom: '-',
            casinguomval: '',
            inneruomval: '',
            looseuomval: '',
            casinguomcnt: '',
            inneruomcnt: '',
            looseuomcnt: '',
            actualqty: '',
            expquantity: '',
            variance: '',
            variancealert: '',
            reason: '',
            frequency: '',
            threshold: ''
        }));
    },

    refreshForm: function(){
        //        this.getNewColumnModel("Reset");
        this.itemlistStore.commitChanges();
        this.itemlistStore.removeAll();
        this.itemlistStore.clearFilter();
        this.loadItemList();
    //        Wtf.getCmp("updatecyclecount").enable();
    //Wtf.getCmp("saveccdrafts").enable();
    //        Wtf.getCmp("confirmCCount").disable();
    //Wtf.getCmp("submitReasons").disable();
    },

    loadItemList: function(isDraft) {
        if(this.storeCmb.getValue() == "") {
            WtfComMsgBox(["Info", "Please select a store."], 0);
            return;
        }
        
        Wtf.Ajax.requestEx({
            url: "INVCycleCount/validateCycleCountDate.do",
            params: {
                storeId: this.storeCmb.getValue(),
                countDate : this.countDate.getValue().format("Y-m-d")
            }
        }, this,
        function(action) {
            var arr = action.data;
            if(arr[0].cycleCountDone){
                var ccDate = arr[0].cycleCountDate;
                this.itemlistStore.removeAll();
                WtfComMsgBox(["Info", "Cycle Count is already done for "+ccDate+", so you can not save cycle count details for any past date."], 2);
            }else{
                this.itemlistStore.load({
                    params: {
                        countdate: this.countDate.getValue().format("Y-m-d"),
                        storeid:this.storeCmb.getValue(),
                        isDraft : isDraft
                    },
                    callback: function(rec, options, success) {

                        if(!success) {
                            WtfComMsgBox(138, 1);
                        }
                    }
                });
           
                this.itemlistStore.on("load", this.itemListStoreload, this);
                this.itemlistStore.commitChanges();
            }
        },
        function(){
            WtfComMsgBox(["Error", "Some error occurred while processing your request."], 1);
        }
        )
        
        
    },
      
    
    itemListStoreload:function(store,recArr,option){
        if(this.itemlistStore.getCount() > 0){
            this.itemlistStore.each(function(rec){
                var draftDetail = rec.get('draftDetail');
                if(draftDetail){
                    rec.set('casinguomcnt', draftDetail.casinguomcnt);
                    rec.set('inneruomcnt', draftDetail.inneruomcnt);
                    rec.set('looseuomcnt', draftDetail.looseuomcnt);
                    rec.set('stockDetailQuantity', draftDetail.stockDetailQuantity);
                    rec.set('stockDetails', draftDetail.stockDetails);
                    rec.set('edited', true);
                }
            })
        }
            //            Wtf.getCmp("updatecyclecount").enable();
            if(recArr.length == option.params.limit) {
//                var params = option.params;
//                params.start = store.getCount();
//                params.limit = 100;
//                store.load({
//                    params: params,
//                    add: true
//                })
            } else {
            //this.addNewRow();
            }
    },

    getActualQty: function(a, b, c){
        var hasCount = false;
        var actualcnt = 0;
        var cival = c.get('inneruomval') ? c.get('inneruomval') : 1;
        var ilval = c.get('looseuomval') ? c.get('looseuomval') : 1;
        var clval = cival * ilval;
        
        if(c.get('casinguom') != "-" && c.get('casinguomcnt') !== "") {
            hasCount = true;
            actualcnt += clval * c.get('casinguomcnt');
        }
        if(c.get('inneruom') != "-" && c.get('inneruomcnt') !== "") {
            hasCount = true;
            actualcnt += ilval * c.get('inneruomcnt');     
        }
        if(c.get('looseuom') != "-" && c.get('looseuomcnt') !== "") {
            hasCount = true;
            actualcnt += c.get('looseuomcnt');
        }
        var qty = "";
        if(hasCount){
            qty = actualcnt;
        }
        c.data.actualqty = qty;
        return qty + " " + c.get('looseuom');
    },
    
    getCalculatedQty:function(value, count){
        var qty = 0;
        for(var i=1; i<value.length; i++){
            count[i] += count[i-1]*value[i];
            qty = count[i];
        }
        if(count.length == 1)
            qty += count[0];

        return qty;
    },
    
    confirmItemList: function(isDarft){
        if(!this.cycleCountForm.form.isValid()){
            this.loadMask1.hide();
            return;
        }
        if(this.MOUTextField.getValue()==""){
            this.loadMask1.hide();
            WtfComMsgBox(["Info", "Please fill MoD for cycle count"], 0);
            return;
        }
        if(!this.documentNumber.getValue() && this.sequenceFormatNO.getValue()=="NA"){
            WtfComMsgBox(["Alert", "Please enter valid Document No"],3);
            return;
        }
        this.confirmItemListCount = 0;
        this.confirmItemList1(isDarft);

    },
    confirmItemList1:function(isDraft){
        var modRecs = this.itemlistStore.getModifiedRecords();
        this.itemlistStore.each(function(rec) {
            if (rec.data != "") {
                rec.data[CUSTOM_FIELD_KEY] = Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
            }
        }, this);
        var editedIndex = this.itemlistStore.find("edited", true);
        //        var varianceIndex = this.itemlistStore.find("variance",'Yes');
      
        if(editedIndex == -1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"), WtfGlobal.getLocaleText("acc.stock.modify")], 0);
            return;
        }
        var invalidDetail = [];
        var negativeInvalidDetail = [];
        var srno = 1;
        var negativeInvalidsrno = 1;
        for(var i=0; i<modRecs.length ;i++){
            var rec = modRecs[i];
            var isNegativeAllowed = Wtf.account.companyAccountPref.isnegativestockforlocwar && !rec.get('isBatchForProduct') && !rec.get('isSerialForProduct') ;
            if(rec.get('actualqty') === ''){ 
                if(!(rec && rec.get('currentsysqty') == 0 && rec.get('actualqty') == 0)){
                    invalidDetail.push(srno +". "+ rec.get('code'));
                    srno++;
                }
            }else if(!isNegativeAllowed && rec.get('actualqty') + rec.get("currentsysqty") - rec.get('sysqty') < 0){
                negativeInvalidDetail.push(negativeInvalidsrno +". "+ rec.get('code'));
                negativeInvalidsrno++;
            } else if(rec.get('actualqty') !== rec.get('stockDetailQuantity')){
                if(!(rec && rec.get('currentsysqty') == 0 && rec.get('actualqty') == 0)){
                    invalidDetail.push(srno +". "+ rec.get('code'));
                    srno++;
                }
            }
        }
        if(!isDraft && negativeInvalidDetail.length > 0){
            var msg = "Following item(s) will cause negative stock, please provide valid stock.<br><b>" + WtfGlobal.replaceAll(negativeInvalidDetail.toString(), ",", "<br>")+" </b>";
            WtfComMsgBox(["Warning", msg], 2);
            return;
        }
        if(invalidDetail.length > 0){
            var msg = "Please fill the valid stock details for the following item(s) <br><b>" + WtfGlobal.replaceAll(invalidDetail.toString(), ",", "<br>")+" </b>";
            
            WtfComMsgBox(["Info", msg], 2);
            return;
        }
        Wtf.MessageBox.confirm('Confirm', "Are you sure you want to save Cycle Count details?", function(btn){
            if (btn == 'yes') {
                this.confirmItemList2(modRecs, true, isDraft, this.documentNumber.getValue());
            }
        }, this);
    },
    
    confirmItemList2:function(modRecs, checkEdit, isDraft, transactionNo){
        var jsonData = [];
        var startcnt = this.confirmItemListCount;
        this.confirmItemListCount = this.confirmItemListCount + 25;
        if(this.confirmItemListCount > modRecs.length ) {
            this.confirmItemListCount = modRecs.length ;
        }
        for(var cnt = startcnt; cnt < this.confirmItemListCount; cnt++) {
            var rec = modRecs[cnt];
            if(!checkEdit || (rec.get('edited')===true)){
                jsonData.push(this.getJsonFromRecord(rec));
            }
        }
        this.loadMask1 = new Wtf.LoadMask(this.gridwrapper.el.dom, {
            msg: "saving..."+(startcnt+1)+" to "+this.confirmItemListCount+"  of  "+modRecs.length
        });
        this.loadMask1.show();
        this.addItem.disable();
        this.deleteItem.disable();
        this.edititem.disable();
        var saveindraft=Wtf.getCmp('saveccdrafts');
        saveindraft.disable();
        Wtf.Ajax.timeout = 600000;
        Wtf.Ajax.requestEx({
            url: "INVCycleCount/addCycleCountRequest.do",
            scope:this,
            params: {
                //                flag: 6,
                currentDate: this.today.format("Y-m-d"),
                storeid:this.storeCmb.getValue(),
                //                locationid:this.locCmb.getValue(),
                jsondata: JSON.stringify(jsonData),
                //                isconfirm: true,
                seqFormatId:this.sequenceFormatNO.getValue(),
                seqno: transactionNo,
                sequenceDone:startcnt > 0,
                //                frequency: this.frequency,
                //                createdby: this.MOUTextField.getValue(),
                countingdate: this.countDate.getValue().format('Y-m-d'),
                removeDraft: false,
                isDraft: isDraft
                
            //                start: startcnt,
            //                totalcnt: this.itemlistStore.getCount(),
            //                revertCCSubmit: this.revertCCSubmit
            }
        }, this,
        function(action) {
//            this.itemlistStore.commitChanges();
            //            action = eval("("+ action + ")");
            var data=action.data;
            var issuccess = action.success
            if(issuccess){
                transactionNo = data.transactionNo
                if(this.confirmItemListCount < modRecs.length) {
                    //                if(startcnt==0){
                    //                    this.revertCCSubmit= parseInt(action.revertCC);//revertCCSubmit=1...cycle count already performed once.
                    //                }
                    this.confirmItemList2(modRecs, checkEdit, isDraft, transactionNo);
                } else {
                    Wtf.Ajax.timeout = 30000;
                    var msg = "Cycle Count ["+transactionNo+"] detail is saved successfully.";
                    if(isDraft){
                        msg = "Cycle Count detail is saved as draft successfully.";
                    }
                    WtfComMsgBox(["Success", msg], 0);
                    // if this.revertCCSubmit=1 means the previous cc submit records are reverted. hence refetch the variance frmo cc table.
                    //                if(this.revertCCSubmit==1){
                    //                    this.updateVarianceForRevert();
                    //                }

                    //Wtf.getCmp("saveccdrafts").disable();
//                    Wtf.getCmp("confirmCCount").disable();
                    //Wtf.getCmp("submitReasons").enable();
                    this.loadMask1.hide();
//                    this.itemlistStore.removeAll();                
                    this.refreshForm();
                    this.addItem.enable();
                    this.deleteItem.enable();
                    this.edititem.enable();
                    saveindraft.enable();

                }
            }else{
                var msg = action.msg;
                Wtf.Ajax.timeout = 30000;
                this.loadMask1.hide();
                WtfComMsgBox(["Failure", msg], 1);
                this.addItem.enable();
                this.deleteItem.enable();
                this.edititem.enable();
                saveindraft.enable();
            }
            
        },
        function(){
            Wtf.Ajax.timeout = 30000;
            this.loadMask1.hide();
            WtfComMsgBox(["Failure", "System request timed out while saving the cycle count"], 1);
            this.addItem.enable();
            this.deleteItem.enable();
            this.edititem.enable();
            saveindraft.enable();
        });
        
    },
    updateItemList: function(){
        if(!this.cycleCountForm.form.isValid()){
            this.loadMask1.hide();
            return;
        }
        var i=0;
        var a="";
        var first=0;
        for(i=0;i<this.itemlistStore.getCount();i++){
            var temp=this.itemlistStore.getAt(i);
            if(temp.data.added){
                if(first==1){
                    a+=",'"+temp.data.id+"'";
                }else{//means first record
                    a+="'"+temp.data.id+"'";
                    first=1;
                }

            }
        }
        //        if(this.frequency == ""){
        //            this.loadMask1.hide();
        //            WtfComMsgBox(["Info", "Frequency is not set for this date."], 0);
        //            return;
        //        }
        
            
        var store = this.itemlistStore;
        //        for(var i = 0; i <store.getCount(); i++) {
        //                
        //            var rec = store.getAt(i);
        //            //                        if(rec.get('edited')===true){
        //            //rec.set('expquantity', roundNumber(obj[i].qty,2));
        //            rec.set('sysqty',rec.data.sysqty);
        //            //rec.set('variance', (rec.data.expquantity - rec.data.sysqty));
        //            rec.set('variance', (rec.data.actualqty - rec.data.sysqty));
        //                
        //        }
        this.getNewColumnModel("Update");
        //         var store = this.itemlistStore;
        //         
        //        for(var i = 0; i <store.getCount(); i++) {
        //                
        //            var rec = store.getAt(i);
        //            //                        if(rec.get('edited')===true){
        //            //rec.set('expquantity', roundNumber(obj[i].qty,2));
        //            //rec.set('sysqty',rec.data.sysqty);
        //                
        //        }
        WtfComMsgBox(["Success"," Data updated successfully."], 0);
        // this.getNewColumnModel("Confirm");
        //        Wtf.getCmp("updatecyclecount").disable();
        this.loadMask1.hide();
        Wtf.getCmp("confirmCCount").enable();
        this.draftId = "";

    //        Wtf.Ajax.requestEx({
    //            url: 'jspfiles/inventory/store.jsp',
    //            method: 'POST',
    //            params: {
    //                flag: 10,
    //                frequency: this.frequency,
    //                store: this.storeCmb.getValue(),
    //                // location:(trackStoreLocation)?this.locCmb.getValue():'',
    //                array:a
    //            }
    //        }, this,
    //        function(resp) {
    //            var obj = Wtf.decode(resp).data;
    //            var len = obj.length;
    //            var store = this.itemlistStore;
    //            for(var i = 0; i < len; i++) {
    //                var index = store.find('id', obj[i].id);
    //                if(index != -1) {
    //                    var rec = store.getAt(index);
    //                    //                        if(rec.get('edited')===true){
    //                    rec.set('expquantity', roundNumber(obj[i].qty,2));
    //                    rec.set('sysqty', roundNumber(obj[i].sysqty,2));
    //                    rec.set('variance', roundNumber(rec.data.actualqty - obj[i].qty,2));
    //                //                        }
    //                }
    //            }
    //            this.getNewColumnModel("Update");
    //            WtfComMsgBox(["Success"," Data updated successfully."], 0);
    //            this.loadMask1.hide();
    //            Wtf.getCmp("confirmCCount").enable();
    //            this.draftId = "";
    //        }, function() {
    //            this.loadMask1.hide();
    //        });
    },
    submitReason: function(){
        if(!this.cycleCountForm.form.isValid()){
            this.loadMask1.hide();
            return;
        }
        if(this.MOUTextField.getValue()==""){
            WtfComMsgBox(["Info", "Please Select MoD for cycle count"], 0);
            return;
        }
        this.submitReasonCount = 0;
        this.submitReason1();
    },
    submitReason1:function(transactionNo){
        var remTrailingComma = false;
        var jsonData = "[";
        var startcnt = this.submitReasonCount;
        this.submitReasonCount = this.submitReasonCount + 25;
        if(this.submitReasonCount > this.itemlistStore.getCount()) {
            this.submitReasonCount = this.itemlistStore.getCount();
        }
        for(var len = startcnt;len < this.submitReasonCount; len++) {
            var rec = this.itemlistStore.getAt(len);
            if(rec.get("reason") != "") {
                rec.set("reason",rec.data.reason);
                remTrailingComma = true;
                jsonData += '{"id":"' + rec.data.id +
                '", "rs":"' + rec.data.reason + '"}' + ",";
            }
        }
        if(remTrailingComma)
            jsonData = jsonData.substr(0, jsonData.length - 1);
        jsonData += "]";
        this.loadMask1 = new Wtf.LoadMask(this.gridwrapper.el.dom, {
            msg: "Submitting..."+len+" of "+this.submitReasonCount
        });
        this.loadMask1.show();
        Wtf.Ajax.timeout = 600000;
        Wtf.Ajax.requestEx({
            url: "INVCycleCount/addCycleCountRequest.do",
            method: 'POST',
            scope:this,
            params: {
                flag: 6,
                storeid:this.storeCmb.getValue(),
                //location:(trackStoreLocation)?this.locCmb.getValue():'',
                jsondata: jsonData,
                isconfirm: false,
                isDraft: false,
                sequenceDone: startcnt > 0,
                seqno: transactionNo,
                frequency: this.frequency,
                draftid: this.draftId,
                createdby: this.MOUTextField.getValue(),
                countingdate: this.countDate.getRawValue()
            }
        }, this,
        function(resp) {
            var temp = eval("("+ resp + ")");
            var obj=temp.data;
            if(this.submitReasonCount < this.itemlistStore.getCount()) {
                this.submitReason1(obj.transactionNo);
            } else {
                Wtf.Ajax.timeout = 30000;
                WtfComMsgBox(["Success"," Reasons submitted successfully."], 0);
                //                Wtf.getCmp("updatecyclecount").disable();
                // Wtf.getCmp("saveccdrafts").disable();
                Wtf.getCmp("confirmCCount").disable();
                // Wtf.getCmp("submitReasons").disable();
                this.loadMask1.hide();
                this.refreshForm();
            //                Wtf.getCmp("as").remove(Wtf.getCmp("cycleCountParentTabb"));
            //                Wtf.getCmp("tabsbdashboard").setDisabled(false);
            }
        },
        function(){
            Wtf.Ajax.timeout = 30000;
            this.loadMask1.hide();
            WtfComMsgBox(["Failure", "Error occurred while submitting form"], 1);
        });
    },

    updateVarianceForRevert : function(){
        Wtf.Ajax.requestEx({
            url: 'jspfiles/inventory/store.jsp',
            method: 'POST',
            params: {
                flag: 110,
                //frequency: this.frequency,
                storeid: this.storeCmb.getValue(),
                businessdate:this.countDate.getRawValue()
            }
        }, this,
        function(resp) {
            var temp = eval("("+ resp + ")");
            var obj=temp.data;
            var len = obj.length;
            var store = this.itemlistStore;
            for(var i = 0; i < len; i++) {
                var index = store.find('id', obj[i].id);
                if(index != -1) {
                    var rec = store.getAt(index);
                    rec.set('expquantity', roundNumber([i].expectedqty,2));
                    rec.set('variance', roundNumber(obj[i].variance,2));
                }
            }
        }, function() {
            });

    },
    
    getJsonFromRecord : function(record) {
        return {
            id:record.get('id'),
            fq:record.get('frequency'),
            cc:record.get('casinguomcnt') != "" ? record.get('casinguomcnt'): 0,
            ic:record.get('inneruomcnt') != "" ? record.get('inneruomcnt'): 0,
            lc:record.get('looseuomcnt') != "" ? record.get('looseuomcnt'): 0,
            aq:record.get('actualqty') != "" ? record.get('actualqty'): 0,
            sq:record.get('sysqty'),
            rs:record.get('reason'),
            extraItem:record.get('added'),
            stockDetails: record.get('stockDetails'),
            customfield:record.get('customfield')
        };
    },

    getDraftJsonFromRecord : function(record) {
        return '{"id":"' + record.data.id +
        '", "fq":"' + record.data.frequency +
        '", "cc":"' + record.data.casinguomcnt +
        '", "ic":"' + record.data.inneruomcnt +
        '", "lc":"' + record.data.looseuomcnt +
        '", "new":"' + record.data.added + '"}';
    },

    onRender: function(config){
        Wtf.inventory.cycleCountPanel.superclass.onRender.call(this, config);
        this.add({
            border: false,
            layout : 'border',
            items :[{
                region:'center',
                autoScroll: true,
                layout:'border',
                border:false,
                bodyStyle : 'background:#f6f6f6;font-size:10px;',
                items:[{
                    border: false,
                    region:'north',
                    height:120,
                    items:[
                    this.cycleCountForm = new Wtf.form.FormPanel({
                        url: "jspfiles/inventory/inventory.jsp",
                        id: 'newCycleCountForm',
                        bodyStyle: "background-color:#F6F6F6; margin: 15px 5px 5px 10px;",
                        labelWidth:100,
                        border: false,
                        layout:"column",
                        items:[{
                            layout:"form",
                            labelWidth:130,
                            border: false,
                            columnWidth:.5,
                            items:[
                                this.storeCmb,
                                this.sequenceFormatNO,
                                this.documentNumber
                            ]
                        },{
                            layout:"form",
                            labelWidth:130,
                            border: false,
                            columnWidth:.5,
                            items: [
                                this.countDate,
                                this.MOUTextField
                            ]
                        }]
                    })
                
                    ]
                },
                {
                    region:'center',
                    border:false,
                    layout: 'fit',
                    items:[ this.gridwrapper]
                }
                ]
            }]
        })
    //        if(checktabperms(12, 1) != "edit") {
    //this.firemyajax();
    //        }
    },
    firemyajax:function(){
        Wtf.Ajax.requestEx({
            url: "jspfiles/inventory/store.jsp",
            params: {
                flag: 8
            }
        }, this,
        function(response){
            var res = eval('(' + response + ')');
            this.MOUTextField.setValue(res.data[0].username);
        },
        function(){
            });
    },

    createDraftListGrid: function(){
        this.draftsReader = new Wtf.data.Record.create([{
            name: 'businessDate'
        },{
            name: 'storeId'
        },{
            name: 'storeName'
        }
        ]);

        this.draftsStore = new Wtf.data.Store({
            url:  "INVCycleCount/getCCDraftList.do",
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.draftsReader)
        });
        this.draftsStore.load();

        this.draftsCm = new Wtf.grid.ColumnModel([{
            header: "Business Date",
            dataIndex: 'businessDate',
            sortable: true,
            renderer: Wtf.dateRenderer
        },{
            header: "Store Name",
            dataIndex: 'storeName'
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
            title : "Cycle Count Drafts",
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
                html : getTopHtml("Cycle Count Drafts","Select a draft from list to open",'images/createuser.png')
            },{
                region : 'center',
                border : false,
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
                handler: function(){
                    if(!this.draftGrid.getSelectionModel().getSelected()){
                        WtfComMsgBox(["Info", "Please select a draft to open"], 0);
                        return;
                    }
                    this.openSavedDraft();
                }
            },{
                text : 'Cancel',
                scope : this,
                minWidth:75,
                handler : function() {
                    this.saveDraftListWin.close();
                }
            }]
        });
        this.saveDraftListWin.show();
    },

    saveInDraft: function(){
        if(this.MOUTextField.getValue() == "") {
            var saveindraft=Wtf.getCmp('saveccdrafts');
            saveindraft.enable();
            WtfComMsgBox(["Info", "Please Select MoD for cycle count"], 0);
            return;
        }
        var remTrailingComma = false;
        var jsonData = "{'root': [";
        for(var cnt = 0; cnt < this.itemlistStore.getCount() ; cnt++) {
            var rec = this.itemlistStore.getAt(cnt);
            remTrailingComma = true;
            jsonData += this.getDraftJsonFromRecord(rec) + ",";
        }
        if(remTrailingComma) {
            jsonData = jsonData.substr(0, jsonData.length - 1);
        }
        jsonData += "]}";

        Wtf.Ajax.requestEx({
            url: "jspfiles/inventory/inventory.jsp",
            params: {
                flag: 8,
                jsondata: jsonData,
                countingdate: this.countDate.getRawValue(),
                storeid: this.storeCmb.getValue(),
                frequency: this.frequency,
                draftid: this.draftId
            }
        }, this,
        function(action) {
            action = eval("("+ action + ")");
            if(action.success) {
                WtfComMsgBox(["Success","Cycle count form saved successfully in draft."], 0);
            } else {
                WtfComMsgBox(["Failure", "Error occurred while saving form in draft"], 1);
            }
            var saveindraft=Wtf.getCmp('saveccdrafts');
                saveindraft.enable();
        },
        function() {
            var saveindraft=Wtf.getCmp('saveccdrafts');
            saveindraft.enable();
            WtfComMsgBox(["Failure", "System request was timed out while saving form in draft"], 1);
        });
    },

    openSavedDraft: function(){
        var rec = this.draftGrid.getSelectionModel().getSelected();
        this.storeCmb.setValue(rec.get('storeId'));
        this.countDate.setValue(new Date(rec.get('businessDate')));
        
        this.getCCFrequenciesForBusinessDate(true, true);
        
        this.saveDraftListWin.close();
        
//        Wtf.Ajax.requestEx({
//            method: 'POST',
//            url: "jspfiles/inventory/inventory.jsp",
//            params: {
//                flag: 10,
//                storeId: rec.get('storeId'),
//                businessDate : rec.get('businessDate')
//            }
//        }, this,
//        function(result){
//            result = eval( '(' + result + ')');
//            if(result.count != 0) {
//                var data = result.data[0];
//                
////                this.frequency = data.frequency;
//                this.added=data.added;
//                //                this.itemlistStore.load({
//                //                    params:{
//                //                        store: data.storeid,
//                //                        frequency: data.frequency,
//                //                        draftid: data.draftid,
//                //                        limit: 100,
//                //                        start: 0
//                //                    },
//                //                    scope: this
//                //                });
//                // this.itemlistStore.on("load", this.itemListStoreload, this);
//                //                    this.itemlistStore.on("loadexception", this.addNewRow, this);
//                this.draftId = data.draftid;
//            } else {
//                WtfComMsgBox(["Message", "No Data for the selected Cycle Count Draft"], 0);
//            }
//            this.saveDraftListWin.close();
//        }, function() {
//            WtfComMsgBox(["Error", "Problem occurred while displaying Cycle Count Draft"], 1);
//        });
    },
    addWindow: function(flag){//flag=1->Add,flag=2->Edit
        var rec1;
        if(flag==2){
            rec1 = this.itemlistGrid.getSelectionModel().getSelected();
            if(!rec1){
                WtfComMsgBox(["Request", "Please select a record"], 1);
                return;
            }
            if(!rec1.data.added){
                WtfComMsgBox(["Info", "Only Extra Items can be edited"], 0);
                return;
            }
        }

        //-----------item -------
        this.itemCodeStore = new Wtf.data.Store({
            url: 'INVCycleCount/getExtraItemList.do',
            baseParams: {
                flag: 6,
                storeid : this.storeCmb.getValue(),
                countdate: this.countDate.getValue().format('Y-m-d')
            },
            sortInfo: {
                field: 'description',
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            },
            autoLoad: true,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.itemlistRecord)
        });

        this.itemCodeCmb1=new Wtf.form.ExtFnComboBox({
                store:this.itemCodeStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus:true,
                fieldLabel:'Product*',
                valueField:'id',
                displayField:'name',
                extraFields:['code','name'],
                listWidth:300,
                extraComparisionField:'code',// type ahead search on acccode as well.
                lastQuery:'',
                //editable:false,
                scope:this,
                hirarchical:true,
                // addNewFn:this.openProductWindow.createDelegate(this),
                forceSelection:true
                   
            });
        this.itemCodeStore.on("load", function(ds, rec, o){//setting value while editing
            if(flag==2) {
                this.itemCodeCmb1.setValue(rec1.data.id);
            }
        }, this);

        var addItemWin = new Wtf.Window({
            title: flag==1?"Add Extra Item":"Edit Extra Item",
            modal: true,
            //labelWidth : 30,
            bodyStyle : 'font-size:10px;',
            minWidth:75,
            width : 400,
            height: 300,
            resizable :false,
            buttonAlign : 'right',
            layout : 'border',
            items: [{
                region: 'north',
                height: 75,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml("Manage Extra Item", "Select an item by its description to perform its cycle count.", 'images/createuser.png') //Change image
            },{
                region : 'center',
                border : false,
                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 20px 20px 20px;',
                layout : 'fit',
                items : [
                this.cycleCountForm1 = new Wtf.form.FormPanel({
                    border : false,
                    bodyStyle : 'background:transparent;',
                    items : [this.itemCodeCmb1]
                })
                ]
            }],
            buttons: [{
                text: 'Submit',
                scope: this,
                handler: function() {
                    var chooseitem=this.itemCodeCmb1.getValue();
                    //this.setTotalAmount();
                    //check newly selected item already exists
                    var existd=this.itemlistStore.find('id',chooseitem);
                    if(existd != -1) {
                        WtfComMsgBox(["Info", "Record already present"], 0);
                        this.itemCodeCmb1.setValue("");
                        return;
                    } else {
                        if(flag==2){//for edit delete previous entry
                            var rec = this.itemlistGrid.getSelectionModel().getSelected();
                            this.itemlistGrid.store.remove(rec);
                        }
                        var check=this.itemCodeStore.find('id',chooseitem);
                        if(check != -1) {
                            var trec = this.itemCodeStore.getAt(check).data;
                            this.itemlistStore.add(new this.itemlistRecord({
                                id: trec.id,
                                code: trec.code,
                                isRowForProduct:trec.isRowForProduct,
                                isRackForProduct:trec.isRackForProduct,
                                isBinForProduct:trec.isBinForProduct,
                                isBatchForProduct:trec.isBatchForProduct,
                                isSerialForProduct:trec.isSerialForProduct,
                                poscode: trec.wincorkey,
                                microskey2: trec.microskey2,
                                orderingcategoryname: trec.orderingcategoryname,
                                name: trec.name,
                                packaging: trec.packaging,
                                casinguom: trec.casinguom,
                                inneruom: trec.inneruom,
                                looseuom: trec.looseuom,
                                casinguomval: trec.casinguomval,
                                inneruomval: trec.inneruomval,
                                looseuomval: trec.looseuomval,
                                casinguomcnt: '',
                                inneruomcnt:'',
                                looseuomcnt: '',
                                actualqty: '',
                                sysqty: trec.sysqty,
                                variance: '',
                                variancealert: '',
                                reason: '',
                                frequency: trec.frequency,
                                threshold: trec.threshold,
                                added: true
                            }));
                            this.getQuantity(trec.id);
                            addItemWin.close();
                        }
                    }

                }
            },
            {
                text: 'Cancel',
                scope: this,
                handler : function() {
                    addItemWin.close();
                }
            }]
        });
        addItemWin.show();
    },
    serialRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.serial.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.serial.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    }
    ,
    cellClick :function(grid, rowIndex, columnIndex, e){
        
        var record = grid.getStore().getAt(rowIndex);  // Get the Record
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name

        
        if(!fieldName){
            var isSerialEnable = record.get("isSerialForProduct");
            var itemId=record.get("id");

            var actqty=record.get("actualqty");
            var fromStoreId = this.storeCmb.getValue();
            if(fromStoreId == undefined || fromStoreId == ""){
                WtfComMsgBox(["Warning", "Please select store for cycle count."],2);
                return false;
            }
            if(itemId=="" || itemId==undefined){
                WtfComMsgBox(["Warning", "Please select product first."],2);
                return false; 
            }
            if(actqty === ""){
                WtfComMsgBox(["Warning", "Please fill stock count details for packaging first."],2);
                return false;
            }else if(isSerialEnable && parseInt(actqty) != actqty) {
                WtfComMsgBox(["Warning", "Serial is enabled for this product so you can not give fractional quantity."],2);
                return false;
            }
            if(!(record && record.get('currentsysqty') == 0 && record.get('actualqty') == 0)){
                this.viewStockDetails(record);
            }
        }
    },
    viewStockDetails: function(record){
        var itemId=record.get("id");
        var itemCode=record.get("code");
        var quantity=record.get("actualqty");
        var systemQty=record.get("sysqty");
        var isRowEnable = record.get("isRowForProduct");
        var isRackEnable = record.get("isRackForProduct");
        var isBinEnable = record.get("isBinForProduct");
        var isBatchEnable = record.get("isBatchForProduct");
        var isSerialEnable = record.get("isSerialForProduct");
        var isSkuEnable = record.get("isSkuForProduct");
        //        var orderToStockUOMFactor=1;
        //        var orderingUomName=record.get("orderinguomname");
        var stockUOMName=record.get("looseuom");
        var fromStoreId = this.storeCmb.getValue();
        var fromStoreName=this.storeCmb.getRawValue();
        var countDate =this.countDate.getValue().format('Y-m-d');
        //        var toStoreId = this.parent.tostoreCombo.getValue();
        //        var toStoreName=this.parent.tostoreCombo.getRawValue();
        var maxQtyAllowed= quantity;
        
        var winTitle = "Stock Detail for Cycle Count";
        var winDetail = String.format('Select Stock details for cycle count  <br> <b>Product :</b> {0}<br> <b>Store :</b> {1}<br>  <b>Business Date :</b> {2}<br><b>Quantity :</b> {3} {4} ', itemCode, fromStoreName, countDate, quantity, stockUOMName);
        
        this.detailWin = new Wtf.CCStockDetailWin({
            WinTitle : winTitle,
            WinDetail: winDetail,
            MaxAllowedQuantity: maxQtyAllowed,
            SystemQuantity: systemQty,
            ProductId:itemId,
            FromStoreId: fromStoreId,
            isRowForProduct : isRowEnable,
            isRackForProduct : isRackEnable,
            isBinForProduct : isBinEnable,
            isBatchForProduct: isBatchEnable,
            isSerialForProduct : isSerialEnable,
            isSkuForProduct : isSkuEnable,
            GridStoreURL:"INVStockLevel/getStoreProductWiseAllAvailableStockDetailList.do",
            GridStoreExtraParams: {
                businessDate : countDate
            },
            StockDetailArray:record.get("stockDetails"),
            DataIndexMapping:{
                availableQty:"systemQty",
                quantity:"actualQty",
                availableSerials:"systemSerials",
                serials:"actualSerials",
                availableSerialsSku:"systemSerialsSku",
                serialsSku:"actualSerialsSku"
            },
            buttons:[{
                text:"Save",
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
                text:"Cancel",
                handler:function (){
                    this.detailWin.close();
                },
                scope:this
            }]
        })
        this.detailWin.show();
    },
    addLocationBatchSerialWindow : function (itemId,itemCode,isBatchForProduct,isSerialForProduct,quantityObj,rowIndex){
        
        this.fromStoreId = this.storeCmb.getValue();
        this.itemId=itemId;
        this.itemcode=itemCode;
        this.isBatchForProduct=isBatchForProduct;
        this.isSerialForProduct=isSerialForProduct;
        this.quantity=quantityObj.quantity;
        this.currentRowNo=rowIndex;
        
          
        this.locCmbRecord = new Wtf.data.Record.create([
        {
            name: 'id'
        },        

        {
            name: 'name'
        }]);

        this.locCmbStore = new Wtf.data.Store({
            url:  'INVStore/getStoreLocations.do',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.locCmbRecord)
        });
        
        this.locCmbStore.load({
            params:{
                storeid:this.fromStoreId
            }
        });
            
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
            emptyText:'Select location...'
        });
       
        this.quantityeditor=new Wtf.form.NumberField({
            scope: this,
            allowBlank:false,
            allowDecimals:true,
            decimalPrecision:4,//Wtf.companyPref.quantityDecimalPrecision,
            allowNegative:false
        })
        
        this.batchEditor = new Wtf.form.TextField({
            //allowBlank : false,
            width:200
        });
        
        this.serialEditor = new Wtf.form.TextField({
            //allowBlank : false,
            width:200
        });
        
        this.EditorColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:"Item Code",
                dataIndex:"itemcode"
            },
            {
                header:"Location",
                dataIndex:"locationid",
                hidden:false,
                editor:this.locCmb
            //                renderer:WtfGlobal.getComboRenderer(this.locCmb)
            },
            
            {
                header:"isBatchEnable",
                dataIndex:"isBatchForProduct",
                hidden:true
            },
            {
                header:"Batch",
                dataIndex:"batch",
                editor:this.batchEditor,
                hidden : (this.isBatchForProduct==true) ? false :true
            },
            {
                header:"isSerialEnable",
                dataIndex:"isSerialForProduct",
                hidden:true
            },
            {
                header:"Serial",
                dataIndex:"serial",
                editor:this.serialEditor,
                hidden : (this.isSerialForProduct==true) ? false :true
            },{
                header: "Packaging",
                dataIndex: 'parent.packaging',
                width:70
            },{
                header: "Casing UoM",
                sortable:true,
                dataIndex: 'casinguomcnt',
                width:70,
                renderer: function(a, b, c, d, e, f){
                    if(c.data.casinguom != "-")
                        b.css = "starbucksEditableCell";
                    return a + "    " + c.get("casinguom");
                },
                editor: this.cnum = new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })
            },{
                header: "Inner UoM",
                dataIndex: 'inneruomcnt',
                width:70,
                renderer: function(a, b, c){
                    if(c.data.inneruom != "-")
                        b.css = "starbucksEditableCell";
                    return a + "    " + c.get("inneruom");
                },
                editor: new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    selectOnFocus: true
                })
            },{
                header: "Loose UoM",
                dataIndex: 'looseuomcnt',
                width:70,
                renderer: function(a, b, c){
                    if(c.data.looseuom != "-")
                        b.css = "starbucksEditableCell";
                    return a + "    " + c.get("looseuom");
                },
                editor: new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative: false,
                    allowDecimals: true,
                    decimalPrecision : Wtf.QUANTITY_DIGIT_AFTER_DECIMAL,
                    selectOnFocus: true
                })
            },{
                header:"Adjusted Quantity",
                dataIndex:"quantity"
            //editor:this.quantityeditor
            }
            ]);
        
            
        this.locationGridStore = new Wtf.data.SimpleStore({
            fields:['itemid','itemcode','locationid','isBatchForProduct','isSerialForProduct',
            'batch','serial','quantity'],
            pruneModifiedRecords:true
        });
          
        this.locationGridStore.on('load', function(){
            this.locationSelectionGrid.getView().refresh();
        },this);
        
        
        ////////////////////////////////////////////////////////////////////////////////////////
        
        
        this.EditorRec = new Wtf.data.Record.create([
        {
            name:"itemid"
        },

        {
            name:"itemcode"
        },
        {
            name:"locationid"
        },
        {
            name:"isBatchForProduct"
        },

        {
            name:"isSerialForProduct"
        },

        {
            name:"batch"
        },
        {
            name:"serial"
        },
        {
            name:"quantity"
        }
        ]);

        this.EditorReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.EditorRec);

        this.locationGridStore = new Wtf.data.Store({
            url:"Json/demo.json",
            reader:this.EditorReader
        });
        
        for(var j=0;j<this.quantity;j++){
            this.addBlankRow();
        }
         
        //////////////////////////////////////////////////////////////////////////////////////
        
           
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
        
        this.locationSelectionGrid.on("beforeedit",this.gridbeforeEdit,this);
        this.locationSelectionGrid.on("afteredit",this.gridafterEdit,this);
        
        this.locationSelectionWindow = new Wtf.Window({
            title : "Select Location",
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
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml("Select Location,Batch,Serial","Please select location for following items",'images/accounting_image/add-Product.gif')/*upload52.gif')*/
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
    validateFilledData : function(){
        
        var recs=this.locationSelectionGrid.getStore().getModifiedRecords();
        var batch="";
        var isBatchForProduct=false;
        var isSerialForProduct=false;
        var quantity=0;
        var serial="";
        var locationid="";   
        
        if(recs.length > 0){
            
            for(var k=0;k<recs.length;k++){
            
                batch=recs[k].get("batch");
                isBatchForProduct=this.isBatchForProduct;
                isSerialForProduct=this.isSerialForProduct; 
                quantity=recs[k].get("quantity");
                serial=recs[k].get("serial");
                locationid=recs[k].get("locationid");
                    
                if(locationid == undefined || locationid == ""){
                    WtfComMsgBox(["Warning", "Please select Location."],0);
                    return false;
                }         
                    
                if(this.quantity>0){   // ie.case for +ve qty .
                    if(isSerialForProduct==true){
                        if(serial == undefined || serial == ""){
                            WtfComMsgBox(["Warning", "Please enter Serial."],0);
                            return false;
                        }
                    }
                    if(isBatchForProduct==true){
                        if(batch == undefined || batch == ""){
                            WtfComMsgBox(["Warning", "Please enter Batch."],0);
                            return false;
                        }
                    }
                }
                if(this.quantity<0){ // ie.case for -ve qty .
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
            }
            
            return true;
            
        }else{
            WtfComMsgBox(["Warning", "Please Select To Location."],0);
            return false;
        }
        
    },
    makeJSONData : function(){
        var recs=this.locationSelectionGrid.getStore().getModifiedRecords();
        var detailArray = new Array();
        
        if(recs.length > 0){
            var mergDataObject = {};
            var keyArray = [];
            for(var k=0;k<recs.length;k++){
                
                var batch=recs[k].get("batch");
                var locationid=recs[k].get("locationid");  
                var quantity=recs[k].get("quantity");
                var serial=recs[k].get("serial");
                
                if(mergDataObject[locationid+""+batch] != undefined){
                    var mergeddata = mergDataObject[locationid+""+batch]
                    mergeddata.quantity= mergeddata.quantity + quantity;
                    if(serial != undefined && serial != null && serial != ''){
                        mergeddata.serialNames= mergeddata.serialNames + ","+serial;
                    }
                } else{
                    var jsondata = {};
                    jsondata.locationId=locationid;
                    jsondata.batchName=batch;
                    jsondata.serialNames=serial;
                    jsondata.quantity=quantity;
                    mergDataObject[locationid+""+batch] = jsondata;
                    keyArray.push(locationid+""+batch)
                }
                
            }
            for(var i=0;i<keyArray.length;i++){
                
                var mergeData=mergDataObject[keyArray[i]];
                
                var data = {};
                data.locationId=mergeData.locationId;
                data.batchName=mergeData.batchName;
                data.serialNames=mergeData.serialNames;
                data.quantity=mergeData.quantity;
                detailArray.push(data);
            }
        }
        return detailArray;
    },
       
    gridbeforeEdit :function(e){
        
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
    },
    
    addBlankRow:function (){
       
        this.newRec = new this.EditorRec({
            itemid :this.itemId,
            itemcode:this.itemcode,
            locationid : "",
            isBatchForProduct:this.isBatchForProduct,
            isSerialForProduct:this.isSerialForProduct,
            batch: "",
            serial:"",
            quantity:1
        });
        
        // this.newRec.push([this.itemId,this.itemcode,"",this.isBatchForProduct,this.isSerialForProduct,"","",0]);
        this.locationGridStore.add(this.newRec); 
    // this.locationSelectionGrid.getView().refresh();
    },
    
    
    gridafterEdit :function(e){
        
        if(e.row == this.locationGridStore.getCount()-1){
        //  this.addBlankRow();
        }
        
    },
    showLocationBatchSerialSelectWindow : function (itemId,itemCode,isBatchForProduct,isSerialForProduct,quantity,rowIndex){
        
        this.fromStoreId = this.parent.fromstoreCombo.getValue();
        this.itemId=itemId;
        this.itemcode=itemCode;
        this.isBatchForProduct=isBatchForProduct;
        this.isSerialForProduct=isSerialForProduct;
        this.quantity=quantity;
        this.currentRowNo=rowIndex;
        
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
                header:"Item Code",
                dataIndex:"itemcode"
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
                header:"isBatchEnable",
                dataIndex:"isBatchForProduct",
                hidden:true
            },
            {
                header:"Batch",
                dataIndex:"batch",
                // editor:this.batchCmb,
                id:"batchColumn"
            //renderer:this.getComboRenderer(this.batchCmb)
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
                id:"serialColumn",
                renderer:this.getComboRenderer(this.serialCmb)
            },
            {
                header:"Available Quantity",
                dataIndex:"availableQty"
            },
            {
                header:"Quantity",
                dataIndex:"quantity",
                editor:this.quantityeditor
            }
            ]);
        
            
        this.locationGridStore = new Wtf.data.SimpleStore({
            fields:['itemid','itemcode','locationid','locationname','isBatchForProduct',
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
                WtfComMsgBox(["Error", "Error Occured while fetching data."],0);
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
        
        this.locationSelectionWindow = new Wtf.Window({
            title : "Select Location",
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
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : getTopHtml("Select Location,Batch,Serial","Please select location for following items",'images/accounting_image/add-Product.gif')/*upload52.gif')*/
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
                    productid :rec.data.itemid
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
            var totalIssueQty=this.quantity;
            var enteredTotalQty=0;
            
            for(var i=0; i < totalRec;i++){
                var currentRec=this.locationGridStore.getAt(i);
                enteredTotalQty += currentRec.get("quantity");
            }
            if(enteredTotalQty > totalIssueQty){
            //                WtfComMsgBox(["Warning", "Entered total quantity can not be greater than selected quantity."],0);
            //                var record=e.record;
            //                record.set("quantity",0);
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
                WtfComMsgBox(["Warning", "You can select maximum "+maxSerialSelectionAllowed+" Serials from list."],0);
                return false;
            }
        
        }
    },
    printBlankSheet: function() {
        if(this.itemlistStore.getCount() > 0){
            var url = "INVCycleCount/printBlankSheet.do?"
            + "storeid="+this.storeCmb.getValue()
            + "&countdate="+this.countDate.getValue().format("Y-m-d");
    
            Wtf.get('downloadframe').dom.src = url;
        }else{
            WtfComMsgBox(["Warning", "There is no any data to print."],0);
        }
        
        
    }
});


Wtf.cycleCountCalendarTab = function(config) {
    Wtf.apply(this, config);
    Wtf.cycleCountCalendarTab.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.cycleCountCalendarTab, Wtf.Panel, {
    initComponent: function(config) {
        Wtf.cycleCountCalendarTab.superclass.initComponent.call(this, config);
        this.calRec = Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'countdate'
        },{
            name: 'frequency'
        },{
            name: 'day'
        },{
            name:'frequencyid'
        }])
        
        this.calds = new Wtf.data.Store({
            url: 'INVCycleCount/getCycleCountCalendar.do',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.calRec)
        });
        
        this.ccfreqStore= new Wtf.data.SimpleStore({
            fields: ['frequencyid', 'name'],
            data : [['0','Daily'],['1','Weekly'],['2','Fortnightly'],['3','Monthly']]
        });
        this.CycleCountFrequencyCombo =new Wtf.common.Select({
            fieldLabel:"CycleCount Frequency" ,
            hiddenName:'ccfrequency',
            name:'ccfrequency',
            store:this.ccfreqStore,
            anchor:'70%',
            xtype:'select',
            valueField:'frequencyid',
            displayField:'name',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            mode: 'local',
            allowBlank:false,
            triggerAction:'all',
            typeAhead: true
        });

        var calcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header: WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup.date"),
                align:"left",
                dataIndex: 'countdate'
            },{
                header: WtfGlobal.getLocaleText("acc.recPattern.Monthly.Day"),
                dataIndex: 'day'
            },{
                header: WtfGlobal.getLocaleText("acc.report.annexure2A.Frequency"),
                dataIndex: 'frequencyid',
                editor: this.CycleCountFrequencyCombo,
                renderer: WtfGlobal.getSelectComboRenderer(this.CycleCountFrequencyCombo, 50)
            }
            ]);

        
        this.monthCal = new  Wtf.MonthField ({
            width: 150,
            value: new Date(),
            format: 'F Y'
        });
        this.calds.on("load", function(s){
            s.each(function(rec){
                if(rec.get('frequencyid') === ''){
                    rec.set('frequencyid', '0');
                }
            }, this)
        }, this)
        this.calds.load({
            params: {
                countmonth: this.monthCal.getValue().format('Y-m-d')
            }
        });
        this.monthCal.on('change', this.loadGrid, this);
        
        var tbarBtnArr = [WtfGlobal.getLocaleText("acc.rem.CycleCountMonth")+": ",this.monthCal];

        var submitCal = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.rem.SaveCalendarFrequencies"),
            iconCls :getButtonIconCls(Wtf.etype.save),
            tooltip: {
                title: WtfGlobal.getLocaleText("acc.rem.SubmitCycleCountCalendar"),
                text: WtfGlobal.getLocaleText("acc.rem.Clicktosubmittheselectedfrequency")
            },
            scope: this,
            handler: function(){
                //                if(this.calds.getModifiedRecords().length==0) {
                //                    WtfComMsgBox(["Cycle Count Calendar", "No changed records in Cycle Count Calendar."], 2);
                //                    return;
                //                }
                var jArr = [];
                for(var i=0;i<this.calds.getCount();i++){
                    var jObj={}
                    jObj.id=(this.calds.getAt(i)).get("id");
                    jObj.frequencyid=(this.calds.getAt(i)).get("frequencyid");
                    jObj.countdate=(this.calds.getAt(i)).get("countdate");
                  
                    jArr.push(jObj);
                }
                Wtf.Ajax.requestEx({
                    url:"INVCycleCount/updateCCCalendar.do",
                    params: {
                        calendarMonth: this.monthCal.getValue().format("Y-m-d"),
                        jsondata:JSON.stringify(jArr)
                    }
                },
                this,
                function(options,action) {
                    if( options.success) {
                        WtfComMsgBox(["Cycle Count Calendar", options.msg],0);
                        this.calds.reload();
                        this.calds.commitChanges();
                    } else {
                        WtfComMsgBox(["Cycle Count Calendar", "Error occured while submitting Cycle Count Calendar."], 1);
                    }
                },
                function(result, req){
                    WtfComMsgBox(["Cycle Count Calendar", "Error occured while submitting Cycle Count Calendar."], 1);
                });
            }
        });
        
        this.cycleCalGrid = new Wtf.grid.EditorGridPanel({
            ds: this.calds,
            cm: calcm,
            clicksToEdit: 1,
            tbar: tbarBtnArr,
            bbar: [submitCal],
            viewConfig: {
                autoFill: true
            }
        });
        this.add(this.cycleCalGrid);
    },

    loadGrid: function() {
        this.calds.load({
            params: {
                countmonth: this.monthCal.getValue().format('Y-m-d')
            },
            callback: function(rec, options, success) {
                if(!success) {
                    WtfComMsgBox(138, 1);
                }
            }
        });
    }
});

Wtf.MonthPicker = Wtf.extend(Wtf.Component, {
    format : "M, Y",
    okText : "OK",
    cancelText : "Cancel",
    constrainToViewport : true,
    monthNames : Date.monthNames,
    value : 0,
    noPastYears : false, // only use the current year and future years
    useDayDate : null, // set to a number between 1-31 to use this day when creating the resulting date object (or null to use todays date or keep existing)

    initComponent: function(){
        Wtf.MonthPicker.superclass.initComponent.call(this);

        this.value = this.value ?
        this.value.clearTime() : new Date().clearTime();

        this.addEvents(
            'select'
            );

        if(this.handler){
            this.on("select", this.handler,  this.scope || this);
        }
    },

    focus : function(){
        if(this.el){
            this.update(this.activeDate);
        }
    },

    onRender : function(container, position){
        var m = [ '<div style="width: 175px; height:175px;"></div>' ]
        m[m.length] = '<div class="x-date-mp"></div>';

        var el = document.createElement("div");
        el.className = "x-date-picker";
        el.innerHTML = m.join("");

        container.dom.insertBefore(el, position);

        this.el = Wtf.get(el);
        this.monthPicker = this.el.down('div.x-date-mp');
        this.monthPicker.enableDisplayMode('block');

        this.el.unselectable();

        this.showMonthPicker();

        if(Wtf.isIE){
            this.el.repaint();
        }

        this.update(this.value);

    },

    createMonthPicker : function(){
        if(!this.monthPicker.dom.firstChild){
            var buf = ['<table border="0" cellspacing="0">'];
            for(var i = 0; i < 6; i++){
                buf.push(
                    '<tr><td class="x-date-mp-month"><a href="#">', this.monthNames[i].substr(0, 3), '</a></td>',
                    '<td class="x-date-mp-month x-date-mp-sep"><a href="#">', this.monthNames[i+6].substr(0, 3), '</a></td>',
                    i == 0 ?
                    '<td class="x-date-mp-ybtn" align="center"><a class="x-date-mp-prev"></a></td><td class="x-date-mp-ybtn" align="center"><a class="x-date-mp-next"></a></td></tr>' :
                    '<td class="x-date-mp-year"><a href="#"></a></td><td class="x-date-mp-year"><a href="#"></a></td></tr>'
                    );
            }
            buf.push(
                '<tr class="x-date-mp-btns"><td colspan="4"><button type="button" class="x-date-mp-ok">',
                this.okText,
                '</button><button type="button" class="x-date-mp-cancel">',
                this.cancelText,
                '</button></td></tr>',
                '</table>'
                );
            this.monthPicker.update(buf.join(''));
            this.monthPicker.on('click', this.onMonthClick, this);
            this.monthPicker.on('dblclick', this.onMonthDblClick, this);

            this.mpMonths = this.monthPicker.select('td.x-date-mp-month');
            this.mpYears = this.monthPicker.select('td.x-date-mp-year');

            this.mpMonths.each(function(m, a, i){
                i += 1;
                if((i%2) == 0){
                    m.dom.xmonth = 5 + Math.round(i * .5);
                }else{
                    m.dom.xmonth = Math.round((i-1) * .5);
                }
            });
        }
    },

    showMonthPicker : function(){
        this.createMonthPicker();
        var size = this.el.getSize();
        this.monthPicker.setSize(size);
        this.monthPicker.child('table').setSize(size);

        this.mpSelMonth = (this.activeDate || this.value).getMonth();
        this.updateMPMonth(this.mpSelMonth);
        this.mpSelYear = (this.activeDate || this.value).getFullYear();
        this.updateMPYear(this.mpSelYear);

        this.monthPicker.show();
    //this.monthPicker.slideIn('t', {duration:.2});
    },

    updateMPYear : function(y){

        if ( this.noPastYears ) {
            var minYear = new Date().getFullYear();
            if ( y < (minYear+4) ) {
                y = minYear+4;
            }
        }

        this.mpyear = y;
        var ys = this.mpYears.elements;
        for(var i = 1; i <= 10; i++){
            var td = ys[i-1], y2;
            if((i%2) == 0){
                y2 = y + Math.round(i * .5);
                td.firstChild.innerHTML = y2;
                td.xyear = y2;
            }else{
                y2 = y - (5-Math.round(i * .5));
                td.firstChild.innerHTML = y2;
                td.xyear = y2;
            }
            this.mpYears.item(i-1)[y2 == this.mpSelYear ? 'addClass' : 'removeClass']('x-date-mp-sel');
        }
    },

    updateMPMonth : function(sm){
        this.mpMonths.each(function(m, a, i){
            m[m.dom.xmonth == sm ? 'addClass' : 'removeClass']('x-date-mp-sel');
        });
    },

    selectMPMonth: function(m){

    },

    getAdjustedDate : function (year,month){
        return new Date(
            year,
            month,
            this.useDayDate ? // use a specific day date?
            (Math.min(this.useDayDate, (new Date(year, month, 1)).getDaysInMonth())) // yes, cap it to month max
            :
            (this.activeDate || this.value).getDate() // keep existing
            );
    },

    onMonthClick : function(e, t){
        e.stopEvent();
        var el = new Wtf.Element(t), pn;
        if(el.is('button.x-date-mp-cancel')){
            this.hideMonthPicker();
        //this.fireEvent("select", this, this.value);
        }
        else if(el.is('button.x-date-mp-ok')){
            this.update(this.getAdjustedDate(this.mpSelYear, this.mpSelMonth));
            //this.hideMonthPicker();
            this.fireEvent("select", this, this.value);
        }
        else if(pn = el.up('td.x-date-mp-month', 2)){
            this.mpMonths.removeClass('x-date-mp-sel');
            pn.addClass('x-date-mp-sel');
            this.mpSelMonth = pn.dom.xmonth;
        }
        else if(pn = el.up('td.x-date-mp-year', 2)){
            this.mpYears.removeClass('x-date-mp-sel');
            pn.addClass('x-date-mp-sel');
            this.mpSelYear = pn.dom.xyear;
        }
        else if(el.is('a.x-date-mp-prev')){
            this.updateMPYear(this.mpyear-10);
        }
        else if(el.is('a.x-date-mp-next')){
            this.updateMPYear(this.mpyear+10);
        }
    },

    onMonthDblClick : function(e, t){
        e.stopEvent();
        var el = new Wtf.Element(t), pn;
        if(pn = el.up('td.x-date-mp-month', 2)){
            this.update(this.getAdjustedDate(this.mpSelYear, pn.dom.xmonth));
            //this.hideMonthPicker();
            this.fireEvent("select", this, this.value);
        }
        else if(pn = el.up('td.x-date-mp-year', 2)){
            this.update(this.getAdjustedDate(pn.dom.xyear, this.mpSelMonth));
            //this.hideMonthPicker();
            this.fireEvent("select", this, this.value);
        }
    },

    hideMonthPicker : function(disableAnim){
        Wtf.menu.MenuMgr.hideAll();
    },


    showPrevMonth : function(e){
        this.update(this.activeDate.add("mo", -1));
    },


    showNextMonth : function(e){
        this.update(this.activeDate.add("mo", 1));
    },


    showPrevYear : function(){
        this.update(this.activeDate.add("y", -1));
    },


    showNextYear : function(){
        this.update(this.activeDate.add("y", 1));
    },

    update : function( date ) {
        this.activeDate = date;
        this.value = date;

        if(!this.internalRender){
            var main = this.el.dom.firstChild;
            var w = main.offsetWidth;
            this.el.setWidth(w + this.el.getBorderWidth("lr"));
            Wtf.fly(main).setWidth(w);
            this.internalRender = true;

            if(Wtf.isOpera && !this.secondPass){
                main.rows[0].cells[1].style.width = (w - (main.rows[0].cells[0].offsetWidth+main.rows[0].cells[2].offsetWidth)) + "px";
                this.secondPass = true;
                this.update.defer(10, this, [date]);
            }
        }
    },

    setValue : function( date ) {
        this.activeDate = date;
        this.value = date;
    }
});

//Wtf.reg('monthpicker', Wtf.MonthPicker);

Wtf.MonthItem = function(config){
    Wtf.MonthItem.superclass.constructor.call(this, new Wtf.MonthPicker(config), config);

    this.picker = this.component;
    this.addEvents('select');

    this.picker.on("render", function(picker){
        picker.getEl().swallowEvent("click");
        picker.container.addClass("x-menu-date-item");
    });
    this.picker.on("select", this.onSelect, this);
};

Wtf.extend(Wtf.MonthItem, Wtf.menu.Adapter, {
    onSelect : function(picker, date){
        this.fireEvent("select", this, date, picker);
        Wtf.MonthItem.superclass.handleClick.call(this);
    }
});

Wtf.MonthMenu = function(config){
    Wtf.MonthMenu.superclass.constructor.call(this, config);
    this.plain = true;
    var mi = new Wtf.MonthItem(config);
    this.add(mi);

    this.picker = mi.picker;

    this.relayEvents(mi, ["select"]);
};

Wtf.extend(Wtf.MonthMenu, Wtf.menu.Menu, {
    cls:'x-date-menu'
});

Wtf.MonthField = function(config){
    Wtf.MonthField.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.MonthField, Wtf.form.DateField, {
    format: Wtf.MonthPicker.prototype.format,
    noPastYears: Wtf.MonthPicker.prototype.noPastYears,
    useDayDate: Wtf.MonthPicker.prototype.useDayDate,
    onTriggerClick : function(){
        if(this.disabled) {
            return;
        }
        if(this.menu == null) {
            this.menu = new Wtf.MonthMenu();
        }
        Wtf.apply(this.menu.picker, {
            format: this.format,
            noPastYears: this.noPastYears,
            useDayDate: this.useDayDate
        });
        this.menu.on(Wtf.apply({}, this.menuListeners, {
            scope: this
        }));
        this.menu.picker.setValue(this.getValue() || new Date());
        this.menu.show(this.el, "tl-bl?");
    },
    parseDate: function(value) {
        if(!value || value instanceof Date){
            return value;
        }
        var valArr = value.split(" ");
        if(valArr.length < 2) {
            return new Date(value);
        }
        var v = new Date(valArr[1], Date.getMonthNumber(valArr[0]), this.useDayDate || 1);
        return v;
    }
});

