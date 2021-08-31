Wtf.account.supplierExciseGrid = function (config){
    Wtf.apply(this, config);
    this.parentScope=config.parentScope;
    this.gridRec =new Wtf.data.Record.create([
    {
        name:'productname'
    },{
        name:'invoiceNameAndDate'
    },{
        name:'availableQuantity'
    },{
        name:'vendorNameAndnop'
    }
    ]);

    var columnModelExcise = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer(),
        {  
            header:'Product Name',
            width: 150, 
            hidden:true,
            align:'center',
            dataIndex: 'productname'
        },{  
            header:'Supplier Invoice Number/Date',
            width: 150, 
            align:'center',
            dataIndex: 'invoiceNameAndDate'
        },{
            header:"Supplier Name / Nature of Purchase", 
            width: 150,  
            dataIndex: 'vendorNameAndnop',
            align:'right'
        },{
            header:"Quantity Utilized", 
            width: 150,
            dataIndex: 'availableQuantity',
            align:'right',
            summaryType:'sum',
            renderer:this.quantityRenderer,
            summaryRenderer:function(val){
                return "<b>Total : " +getRoundedAmountValue(decodeURI(val))+"</b>";
            }
        }
        ]);

    this.supplierExciseInvoiceStore = new Wtf.data.GroupingStore({
        sortInfo: {
            field: 'term',
            direction: 'ASC'
        },
        groupField : 'productname',
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.gridRec)
    });
    
    var data=this.loadSupplierInvoiceDetails();
    if(!data){
        return false;
    }
    this.gridView1 = new Wtf.grid.GroupingView({
        forceFit:true,
        showGroupName: true,
        enableNoGroups:true, // REQUIRED!
        hideGroupedColumn: false,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    
    var gridSummary = new Wtf.grid.GroupSummary({});               
    this.grid = new Wtf.grid.EditorGridPanel({
        store: this.supplierExciseInvoiceStore,
        cm: columnModelExcise,
        stripeRows: true,
        border : false,
        layout:'fit',
        loadMask : true,
        bodyStyle: 'padding:0px',
        emptyText:WtfGlobal.getLocaleText("acc.common.norec"),
        height:322,
        plugins: [gridSummary],
        viewConfig:this.gridView1
    });
    
    this.exciseSupplierDetailsGrid= new Wtf.Window({
        modal: true,
        closeAction: 'hide',
        closable: false,
        id:'exciseSupplierDetailsGrid',
        title: WtfGlobal.getLocaleText("acc.invoice.grid.supplierExciseDetails"),//"Supplier Excise Details",
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        buttonAlign: 'right',
        autoScroll:true,
        width: 800,
        height:500,               
        scope: this,
        items: [{
            region:"north",
            height:90,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml("Confirm Supplier Excise Details","Please confirm supplier details for each product",'../../images/accounting_image/tax.gif',true)
        },{
            region: "center",
            bodyStyle: 'padding:10px 10px 10px 10px;',
            baseCls:'bckgroundcolor',
            items:[
            this.grid
            ]
        }],
        buttons:
        [{
            text:"Confirm",//'Save',
            id:"supplierExciseDetails",
            scope:this,
            handler: function()
            {  
                this.exciseSupplierDetailsGrid.close(); 
                this.parentScope.checkMemo(this.param1,this.param2,this.param3);
            }
        },{
            text: this.viewMode?WtfGlobal.getLocaleText('acc.common.close'):WtfGlobal.getLocaleText('acc.field.Cancel'),//'Cancel',
            scope:this,
            handler: function()
            {
                this.parentScope.enableSaveButtons();
                this.exciseSupplierDetailsGrid.close();
            }
        }]
    });
    
    this.exciseSupplierDetailsGrid.show();
    
    this.grid.getView().refresh();
    
    Wtf.account.supplierExciseGrid.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.supplierExciseGrid,Wtf.Panel,{
    onRender:function (config){
        Wtf.account.supplierExciseGrid.superclass.onRender.call(this,config); 
    },
    loadSupplierInvoiceDetails:function (){
        var productgridStore=this.productGrid.getStore();
        if(productgridStore.getCount()-1>0){
            for (var x=0; x<productgridStore.getCount()-1; x++) {
                var record=productgridStore.getAt(x);
                var baseQuantity=record.data.baseuomquantity;
                var availableQuantity=0;
                var productName=record.data.productname;
                var supplierExciseDetails=eval(record.data.supplierExciseDetails);
                if(!Wtf.isEmpty(supplierExciseDetails) && supplierExciseDetails.length>0){
                    for (var y=0; y<supplierExciseDetails.length; y++) {   
                        var recs={
                            "productname":productName,
                            "invoiceNameAndDate":supplierExciseDetails[y].invoiceNameAndDate,
                            "vendorNameAndnop":supplierExciseDetails[y].vendorNameAndnop,
                            "availableQuantity":supplierExciseDetails[y].availableQuantity
                        };
                        availableQuantity+=supplierExciseDetails[y].availableQuantity;
                        if(y==supplierExciseDetails.length-1 && availableQuantity!=baseQuantity){
                            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),"In supplier excise details, total quantity of selected invoice should be equal to base quantity for product <b>"+productName+"</b>"], 2);
                            this.parentScope.enableSaveButtons();
                            return false;
                        }
                        this.supplierExciseInvoiceStore.add(new this.gridRec(recs));
                    }
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),"In supplier excise details, total quantity of selected invoice should be equal to base quantity for product <b>"+productName+"</b>"], 2);
                    this.parentScope.enableSaveButtons();
                    return false;
                }
            }
            return true;
        }
    }
});
