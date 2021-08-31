this.natureofPurchase="";
Wtf.account.supplierExciseDetail = function (config){
    Wtf.apply(this, config);
    
    this.termRec =new Wtf.data.Record.create([
    {
        name: 'id'
    },{
        name:'invoiceName'
    },{
        name:'goodsreceiptid'
    },{
        name:'invoiceNameAndDate'
    },{
        name:'invoiceDate'
    },{
        name:'vendorid'
    },{
        name:'vendorName'
    },{
        name:'natureofpurchaseName'
    },{
        name:'natureofpurchaseId'
    },{
        name:'availableQuantity'
    },{
        name:'vendorNameAndnop'
    },{
        name:'actualQuantity'
    }
    ]);
    this.accRec = Wtf.data.Record.create ([
    {
        name:'invoiceName'
    },{
        name:'goodsreceiptid'
    },{
        name:'invoiceNameAndDate'
    },{
        name:'invoiceDate'
    },{
        name:'vendorid'
    },{
        name:'vendorName'
    },{
        name:'natureofpurchaseName'
    },{
        name:'natureofpurchaseId'
    },{
        name:'availableQuantity'
    },{
        name:'vendorNameAndnop'
    },{
        name:'actualQuantity'
    }
    ]);
    this.accStore = new Wtf.data.Store({
        url : "ACCGoodsReceiptCMN/goodsReceiptDetailsForSupplierExcise.do",
        baseParams:{
            companyid:companyid,
            productid:this.record.data.productid
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });
    this.accStore.load();
    

    this.cmbAccount=new Wtf.form.ExtFnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),
        name:'invoiceName',
        id:'invoiceName'+this.id,
        store:this.accStore,
        valueField:'invoiceNameAndDate',
        displayField:'invoiceNameAndDate',
        minChars:1,
        typeAheadDelay:30000,
        extraComparisionField:'acccode',
        width:200,
        listWidth:1000,
        hiddenName:'goodsreceiptid',
        allowBlank:false,
        forceSelection:true,
        extraFields:['vendorNameAndnop','availableQuantity'],
        triggerAction:'all',
        typeAhead: true,
        mode:'local'
    });
    this.utilizedQty=new Wtf.form.NumberField({
        name:'utilizedQty',
        hiddenName:'utilizedQty',
        id:'utilizedQty'+this.id,
        fieldLabel :WtfGlobal.getLocaleText("acc.invoice.grid.qtyUtilized"),//"Utilized Quantity",
        allowNegative: false,
        minValue:1,
        allowDecimals:false
    });

    var columnModelExcise = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer(),
        {  
            header:WtfGlobal.getLocaleText("acc.invoice.grid.supplierInvoiceAndDate"),//'Supplier Invoice <br>Number/Date',
            width: 150, 
            sortable: true,
            editor:this.cmbAccount,
            align:'center',
            dataIndex: 'invoiceNameAndDate'
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.grid.suppliernameAndNOP"),//"Supplier Name / <br>Nature of Purchase", 
            width: 150,  
            dataIndex: 'vendorNameAndnop',
            align:'right'
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.grid.qtyBRUtilized"),//"Quantity <br>Utilized", 
            width: 150,
            editor:!this.viewMode?this.utilizedQty:"",
            dataIndex: 'availableQuantity',
            align:'right'
        },{
            header:WtfGlobal.getLocaleText("acc.product.gridAction"),//"Action",
            align:'center',
            width: 80, 
            dataIndex: 'manufactureTermAmount',
            hidden:this.viewMode,
            renderer:function(v){
                return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
            }
        }
        ]);

    this.supplierExciseStore = new Wtf.data.Store({
        sortInfo: {
            field: 'term',
            direction: 'ASC'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.termRec)
    });
    this.loadSupplierDetails(this.record.data.supplierExciseDetails);
    this.summary = new Wtf.grid.GroupSummary();
    this.grid = new Wtf.grid.EditorGridPanel({
        store: this.supplierExciseStore,
        cm: columnModelExcise,
        stripeRows: true,
        border : false,
        layout:'fit',
        loadMask : true,
        bodyStyle: 'padding:0px',
        emptyText:WtfGlobal.getLocaleText("acc.common.norec"),
        height:300,
        viewConfig:{
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    });
    this.oldrecord=this.grid.on('beforeedit',this.beforeEditgrid,this);
    this.grid.on('afteredit',this.updateonrecordselect,this);
    this.grid.on('rowclick',this.handleRowClick,this);
    if(!this.viewMode){
        this.addBlankRow(this.supplierExciseStore);     
    }
    this.exciseSupplierDetails= new Wtf.Window({
        modal: true,
        closeAction: 'hide',
        closable: false,
        id:'exciseSupplierDetails'+this.id,
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
            html:getTopHtml("Supplier Details for : "+this.record.data.productname,"<i>"+((!Wtf.isEmpty(config.productDetails.data)&&!Wtf.isEmpty(config.productDetails.data.hsncode))?("(Tariff Classification: "+config.productDetails.data.hsncode+")"):""),'../../images/accounting_image/tax.gif',true)
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
            text: WtfGlobal.getLocaleText("acc.het.524"),//'Save',
            id:"supplierExciseDetails",
            disabled:true,
            hidden:this.viewMode,
            scope:this,
            handler: function()
            {
                var rec=this.record;
                this.saveSupplierExciseDetails(rec);
                this.exciseSupplierDetails.close();
            }
        },{
            text: this.viewMode?WtfGlobal.getLocaleText('acc.common.close'):WtfGlobal.getLocaleText('acc.field.Cancel'),//'Cancel',
            scope:this,
            handler: function()
            {
                this.exciseSupplierDetails.close();
            }
        }]
    });
    this.exciseSupplierDetails.show();
    
    this.cmbAccount.on('select',this.oncomboSelect,this);
    this.cmbAccount.on('beforeselect',this.oncomboBeforeSelect,this);
    this.accStore.on('load',this.comboafterLoad,this);
    Wtf.account.supplierExciseDetail.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.supplierExciseDetail,Wtf.Panel,{
    onRender:function (config){
        Wtf.account.supplierExciseDetail.superclass.onRender.call(this,config); 
    },
    addBlankRow:function(store){
        var Record = store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for(var j = 0;j < fl;j++){
            f = fi[j];
            if(f.name!='id') {
                blankObj[f.name]='';
                if(!Wtf.isEmpty(f.defValue))
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        if(this.symbol!=undefined)
            newrec.data.currencysymbol=this.symbol;
        store.add(newrec);
    },
    handleRowClick:function (grid,rowindex,e){
        var store=grid.getStore();
        var total=store.getCount();
        var record = store.getAt(rowindex);
        if(e.getTarget(".delete-gridrow")){
            var total=store.getCount();
            var record = store.getAt(rowindex);
            store.remove(record);
            if (rowindex == total - 1) {
                this.addBlankRow(store);
            }
            var countAv = 0;
            for(var o=0;o<store.getCount()-1;o++){
                countAv+= store.getAt(0).data.availableQuantity
                if(countAv==this.record.data.baseuomquantity){
                     Wtf.getCmp("supplierExciseDetails").setDisabled(false); 
                }
               Wtf.getCmp("supplierExciseDetails").setDisabled(true); 
            }
            if(store.getCount()<2){//all row deleted then reset natureofpurchase
                this.accStore.load({
                    params:{
                        filterdata:false,
                        invoice:this.record.data.rowid!=undefined?this.record.data.rowid:""
                    }
                });
            }else if(rowindex != total - 1){
                var find=false;
                for(var k=0;k<this.accStore.getCount();k++){
                    if(this.accStore.getAt(k).data.goodsreceiptid ==record.data.goodsreceiptid){
                        record.data.availableQuantity=this.accStore.getAt(k).data.availableQuantity+record.data.availableQuantity;
                        record.data.actualQuantity=record.data.availableQuantity;
                        this.accStore.remove(this.accStore.getAt(k));
                        find=true;
                    }
                }
                if(!find){
                    record.data.availableQuantity=record.data.actualQuantity;
                }
                this.accStore.add(record);  
            }
            grid.getView().refresh();
        }
    },
    updateonrecordselect:function (obj,grid,a){
        var store=this.supplierExciseStore;
        if(!Wtf.isEmpty(obj)){
            if(obj.field == "invoiceNameAndDate"){
                if(store.getAt(obj.row).data.invoiceNameAndDate!=""){
                    for(var l=0;l<=this.accStore.getCount();l++){ // it remove record from combo which is selected for grid.
                        if(store.getAt(obj.row).data.goodsreceiptid==this.accStore.getAt(l).data.goodsreceiptid){
                            this.accStore.remove(this.accStore.getAt(l));
                            break;
                        }
                    }
                    var q=0;
                    var p=[];
                    for(var h=0;h<this.accStore.getCount();h++){
                        if(this.accStore.data.items[h].data.natureofpurchaseId==store.getAt(obj.row).data.natureofpurchaseId){
                            p[q]=this.accStore.getAt(h);
                            q++;
                        }
                    }
                    this.accStore.removeAll();  
                    this.accStore.add(p); 
                
                    if(store.getCount()-1==obj.row){
                        this.addBlankRow(this.supplierExciseStore);
                    }
                }
            }else if(obj.field=="availableQuantity"){
                var actualqty=obj.record.data.actualQuantity
                for(var g=0;g<this.accStore.getCount();g++){
                    if(obj.record.data.goodsreceiptid==this.accStore.getAt(g).data.goodsreceiptid){
                        actualqty+=this.accStore.getAt(g).data.availableQuantity;
                    }
                }
                if(actualqty>=obj.record.data.availableQuantity){
                    var allQty=0;
                    var selectedOnly=0;
                    var gridstoreitems= store.data.items;
                    var requiredQty = this.record.data.baseuomquantity;
                    for(var i=0;i<gridstoreitems.length;i++){ // store data quantity
                        if(!Wtf.isEmpty(gridstoreitems[i].data.availableQuantity)){
                            allQty += gridstoreitems[i].data.availableQuantity;
                            selectedOnly += gridstoreitems[i].data.availableQuantity;
                        }
                    }
                    if(allQty>requiredQty){
                        WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),WtfGlobal.getLocaleText('acc.invoice.grid.quantitylargeravailable')], 2);//,//Selected quantity is exceed than available quantity
                        store.getAt(obj.row).data.availableQuantity = obj.originalValue;
                    }else if(allQty==requiredQty){
                        Wtf.getCmp("supplierExciseDetails").setDisabled(false);
//                        this.adjustStore(obj,this.accStore);
                    }else if(allQty<requiredQty){
//                        this.adjustStore(obj,this.accStore);
                        Wtf.getCmp("supplierExciseDetails").setDisabled(true);
                    }
                }else{
                    WtfComMsgBox(["Info","Utilized quantity must be equal or less than available quantity and available quantity is "+obj.record.data.actualQuantity+" "+this.record.data.baseuomname], 2);
                    store.getAt(obj.row).data.availableQuantity=obj.originalValue;
                }
            }
        }
        this.grid.getView().refresh();  
    },
    oncomboSelect:function (component,rec,a){
        var store=this.supplierExciseStore;
        if(!Wtf.isEmpty(rec) && !Wtf.isEmpty(rec.data) ){
            store.getAt(this.grid.selModel.getSelectedCell()[0]).data=rec.data; 
        }
    },
    oncomboBeforeSelect:function (component,rec,a,p){ // it check require quantity and available quantity.
        if(Wtf.isEmpty(component.lastSelectionText)){
            var gridstore=this.supplierExciseStore;
            var requiredQty = this.record.data.baseuomquantity;
            var gridstoreitems= gridstore.data.items
            var allQty=0;
            var selectedOnly=0;
            for(var i=0;i<gridstoreitems.length;i++){ // store data quantity
                if(i==this.grid.activeEditor.row && this.grid.activeEditor.record.data.natureofpurchaseId!=""){
                    continue;
                    
                }
                if(!Wtf.isEmpty(gridstoreitems[i].data.availableQuantity)){
                    allQty += gridstoreitems[i].data.availableQuantity;
                    selectedOnly += gridstoreitems[i].data.availableQuantity;
                }
            }
            if(!Wtf.isEmpty(rec) && !Wtf.isEmpty(rec.data) ){ //seleced record quantity
                allQty +=rec.data.availableQuantity;
            }
            
            if(allQty>requiredQty){
                if(requiredQty==selectedOnly){
                    WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),WtfGlobal.getLocaleText('acc.invoice.grid.quantitylargeravailable')], 2);
                    return false;
                }
                rec.data.availableQuantity=requiredQty-selectedOnly; // it change only available quentity to require quantity - suppose 10 is require and available is 20 so it select only 10.
                
                Wtf.getCmp("supplierExciseDetails").setDisabled(false);
                if(selectedOnly==requiredQty){
                    Wtf.getCmp("supplierExciseDetails").setDisabled(false);
                }
            }else if(allQty==requiredQty){
                Wtf.getCmp("supplierExciseDetails").setDisabled(false);
            }else if(allQty<requiredQty){
                Wtf.getCmp("supplierExciseDetails").setDisabled(true);
            }
        }
    },
    loadSupplierDetails:function (recs){
        var records = eval(recs);
        if(!Wtf.isEmpty(records)){
//            var q=0;
//            var p=[];
//            for(var h=0;h<this.accStore.getCount();h++){
//                if(this.accStore.data.items[h].data.natureofpurchaseId==records.getAt(0).data.natureofpurchaseId){
//                    p[q]=this.accStore.getAt(h);
//                    q++;
//                }
//            }
//            this.accStore.removeAll();  
//            this.accStore.add(p); 
            for (var x=0; x<records.length; x++) {
                var rec = new this.termRec(records[x]);            
                this.supplierExciseStore.add(rec);
            }
            if(!Wtf.isEmpty(this.supplierExciseStore.getAt(0))){ // it check any data found in grid - if "Yes" than it load combo with grid's "nature of payment""
                this.accStore.load({
                    params:{
                        natureofpurchase:this.supplierExciseStore.getAt(0).data.natureofpurchaseId    
                    }
                });
            }
        }
    },
    saveSupplierExciseDetails:function (rec){
        var json=[];
        for(var l=0;l<this.supplierExciseStore.getCount()-1;l++){
            json.push(this.supplierExciseStore.getAt(l).data);
        }
        this.record.set("supplierExciseDetails",JSON.stringify(json));
        
    },
//    adjustStore:function (obj,combostore){ // function adjust store record quantity when grid quantity is change
//        for(var u=0;u<this.accStore.getCount();u++){
//            if(obj.record.data.goodsreceiptid==this.accStore.getAt(u).data.goodsreceiptid){
//                var reccrd=this.accStore.getAt(u);
//                reccrd.data.availableQuantity=reccrd.data.availableQuantity+(obj.originalValue-obj.value);
//                reccrd.data.actualQuantity = reccrd.data.availableQuantity;
//                this.accStore.remove(this.accStore.getAt(u));
//                if(reccrd.data.availableQuantity>0){
//                    this.accStore.add(reccrd)
//                } // whole quentity is in grid so actual value== available value
//                    this.supplierExciseStore.getAt(obj.row).data.actualQuantity=obj.value;
////                }
//                break;
//            }
//        }
//    },
    comboafterLoad:function (){ // function remove data from combo which is already seleced in grid and adjust actual quantity.
        var reco=[];
        var p=0
        for(var j=0;j<this.accStore.getCount();j++){
            var idx = this.supplierExciseStore.find("goodsreceiptid",this.accStore.getAt(j).data.goodsreceiptid);            
            if(idx != -1){
                this.supplierExciseStore.getAt(idx).data.actualQuantity=this.supplierExciseStore.getAt(idx).data.actualQuantity+this.accStore.getAt(j).data.actualQuantity;
            }else{
                reco[p]=this.accStore.getAt(j);
                p++;
            }
        }
        if(reco!=[]){
            this.accStore.removeAll();
            this.accStore.add(reco);
            this.grid.getView().refresh();
        }
    },
    beforeEditgrid:function(row){
        if(!Wtf.isEmpty(row.record.data.goodsreceiptid) && row.field=="invoiceNameAndDate"){
            return false;
        }
    }
});
