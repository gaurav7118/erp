Wtf.account.TaxAdjustmentGrid = function(config){
    this.isCustomer = (config.isCustomer)?config.isCustomer:false;// if Output tax then isCustomer will be true else false
    Wtf.apply(this,{
        buttons:[this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeFAOpenWin.createDelegate(this)
            })]
    },config);
    
    Wtf.account.TaxAdjustmentGrid.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.TaxAdjustmentGrid, Wtf.Window,{
    onRender:function(config){
        Wtf.account.TaxAdjustmentGrid.superclass.onRender.call(this,config);
        var image="../../images/accounting_image/calendar.jpg";
        
        //create Grid
        
        this.createDocumentInfoGrid();
        
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(this.title,this.title+' List',image)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.documentGridContainerPanel]

        }
        );
    },
    
    createDocumentInfoGrid:function(){
        
        this.gridRec = new Wtf.data.Record.create([
            {name:'documentId'},
            {name:'documentNo'},
            {name:'documentDate',type:'date'},
            {name:'amount'},
            {name:'gstAmount'},
            {name:'tax'},
            {name:'taxName'},
            {name:'reason'},
            {name:'reasonName'}
        ]);
        
        //
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.gridRec);
        
        //
        var gridStoreUrl = "ACCInvoice/getTaxAdjustments.do";
        
        this.gridStore = new Wtf.data.Store({
            url:gridStoreUrl,
            baseParams:{
                isInputTax:!this.isCustomer
            },
            reader:this.gridStoreReader
        });
        
        //
        
        this.loadMask = new Wtf.LoadMask(document.body,{
                    msg : 'Loading...'
        });
        
        //
        this.gridStore.on('loadexception',function(){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);
        
        //
        this.gridStore.on('beforeload',function(){
            this.loadMask.show();
        },this);
        
        //
        
        this.gridStore.on('load',function(){
            this.loadMask.hide();
        },this);
        
        this.gridStore.load();
        
        //
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" Document No",
            width: 150,
            id:"quickSearch"+this.id,
            field: 'documentNo',
            Store:this.gridStore
        });
        
        //
        this.colModel = new Wtf.grid.ColumnModel([this.sm,{
                header:'Document No',
                dataIndex:'documentNo',
                align:'center'
            },{
                header:'Document Date',
                dataIndex:'documentDate',
                align:'center',
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:'Tax',
                dataIndex:'taxName',
                align:'center'
            },{
                header:'Amount',
                dataIndex:'amount',
                align:'center',
                renderer:WtfGlobal.currencyRendererSymbol
            },{
                header:'GST Amount',
                dataIndex:'gstAmount',
                align:'center',
                renderer:WtfGlobal.currencyRendererSymbol
            },{
                header:'Reason',
                dataIndex:'reasonName',
                align:'center'
        }]);
    
        //
        this.documentInfoGrid = new Wtf.grid.GridPanel({
            cm:this.colModel,
            store:this.gridStore,
            sm:this.sm,
            stripeRows :true,
            border:false,
            viewConfig:{
                emptyText:'<center>No Record To display.</center>',
                forceFit:true
            }
        });
        
        // Tbar Item Creation
    
        var buttonArr = [];
        this.createNewButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.create.title"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.create.title"),
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler:this.createNewHandler.createDelegate(this)
        });
        
        this.editButton = new Wtf.Toolbar.Button({
            text:'Edit',
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:'Edit to selected record',
            handler:this.editHandler.createDelegate(this)
        });
        
        this.deleteButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.DELETEBUTTON"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.deleteHandler.createDelegate(this)
        });
        
        this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            hidden:false,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        
        this.resetBttn.on('click',this.handleResetClick,this);
        
        
        buttonArr.push(this.quickPanelSearch,this.resetBttn,this.createNewButton,this.editButton,this.deleteButton);
        
         // creating container panel
    
        this.documentGridContainerPanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            items:[
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.documentInfoGrid],
                    tbar:buttonArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.gridStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                    })
                }
            ]
        });
        
    },
    
    createNewHandler:function(){
        
        if(this.isCustomer){
            if(Wtf.account.companyAccountPref.outputtaxadjustmentaccount == null || Wtf.account.companyAccountPref.outputtaxadjustmentaccount == undefined || Wtf.account.companyAccountPref.outputtaxadjustmentaccount == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),'Please set Output Tax Adjustment Account in Company Preferences'], 3);
                return;
            }
        }else{
            if(Wtf.account.companyAccountPref.inputtaxadjustmentaccount == null || Wtf.account.companyAccountPref.inputtaxadjustmentaccount == undefined || Wtf.account.companyAccountPref.inputtaxadjustmentaccount == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),'Please set Input Tax Adjustment Account in Company Preferences'], 3);
                return;
            }
        }
        
        
        var title = (this.isCustomer)?"Output Tax Adjustment":"Input Tax Adjustment";
    
        this.transactionForm = new Wtf.account.TaxAdjustment({
            title:title,
            layout:'border',
            id:'createTransactionFormId123',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            isCustomer:this.isCustomer,
            height:400,
            width:500,
            modal:true
        });
        this.transactionForm.on('datasaved',this.reloadStore,this)
        this.transactionForm.show();
    },
    
    deleteHandler : function(){
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),"Are you sure you want to delete the selected record?",function(btn){
            if(btn!="yes") {
                return;
            }
            var selectedRecordArray = this.documentInfoGrid.getSelectionModel().getSelections();
            if(selectedRecordArray.length>1 || selectedRecordArray.length<=0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
                return;
            }
            var record = "";
            if(selectedRecordArray.length == 1){
                record = selectedRecordArray[0];
            }

            var documentId = record.get('documentId');

            Wtf.Ajax.requestEx({
                url:"ACCInvoice/deleteTaxAdjustment.do",
                params: {
                    documentId:documentId
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    
    editHandler:function(){
        
        var selectedRecordArray = this.documentInfoGrid.getSelectionModel().getSelections();
        if(selectedRecordArray.length>1 || selectedRecordArray.length<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        var record = "";
        if(selectedRecordArray.length == 1){
            record = selectedRecordArray[0];
        }
        
        
        var title = (this.isCustomer)?"Edit Input Tax Adjustment":"Edit Output Tax Adjustment";
        this.transactionForm = new Wtf.account.TaxAdjustment({
            title:title,
            layout:'border',
            isEdit:true,
            id:'createTransactaionFormId123',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            isCustomer:this.isCustomer,
            record:record,
            height:400,
            width:500,
            modal:true
        });
        this.transactionForm.on('datasaved',this.reloadStore,this)
        this.transactionForm.show();
    },
    
    reloadStore:function(){
        this.gridStore.reload();
    },
    
    closeFAOpenWin:function(){
        this.close();
    },
    
    genSuccessResponse:function(response, request){
        if(response.success){
            WtfComMsgBox([this.title,response.msg],response.success*2+1);
            this.reloadStore();
        }else {
            WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
        }
    },

    genFailureResponse:function(response){
        //        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    loaddata:function(){
        this.gridStore.load({params: {ss: this.quickPanelSearch.getValue(), start: 0, limit: this.pP.combo.value,pagingFlag:true}});
    },
    
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loaddata();
            this.gridStore.on('load',this.storeloaded,this);
        }
    },
        
    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
    }
})