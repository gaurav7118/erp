/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.ProductdDetails = function(config) {
    Wtf.apply(this, config);
    this.msgLmt = 30;
    this.productid=config.productid;
    this.productname=config.productname;
    this.so=config.so;
    this.summary = new Wtf.grid.GridSummary({});

    this.ProductQuaRec = new Wtf.data.Record.create([{           
        name:'productid'
    },{           
        name:'productname'
    },{
        name:'quantity'

    },{
        name:'status'
    },
    {
        name:'sodate', 
        type:'date'
    },

    {
        name:'customername'
    },
    {
        name:'TotalCount'
    },
    {
        name:'sonumber'
    }]);
    this.ProductQuaStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.ProductQuaRec),
        url:"ACCProductCMN/getProductOutstandingQuantityDetails.do",
        baseParams:{
            productid: this.productid,
            productname: this.productname,
            SoPoFlag: this.so
            
        }
    });
    
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: true
    });
   
    this.repCm = new Wtf.grid.ColumnModel([
        this.sm,
        {
             header: WtfGlobal.getLocaleText("acc.saleByItem.gridProduct"),
            width: 70,
            dataIndex: 'productname'
            
        },{
            header:this.so?WtfGlobal.getLocaleText("acc.cust.name"):WtfGlobal.getLocaleText("acc.ven.name"),
            width: 100,
            align:'center',
            dataIndex: 'customername'
            
        },{
           header:this.so?WtfGlobal.getLocaleText("acc.MailWin.somsg7"):WtfGlobal.getLocaleText("acc.MailWin.pomsg7"),
            width: 100,
            align:'center',
            dataIndex: 'sonumber'
        }, {
            header:this.so?WtfGlobal.getLocaleText("acc.dimension.module.9")+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"):WtfGlobal.getLocaleText("acc.dimension.module.10")+" "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
            dataIndex:'sodate',
            align:'center',
            pdfwidth:80,
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            summaryRenderer:function(){
                return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.coa.total")+'</div>'
                }
        },
        {
            header : WtfGlobal.getLocaleText("acc.saleByItem.gridQty"),
            dataIndex: 'quantity',
            width:30,
            renderer:this.quantityRenderer,
            summaryType:'sum',
            summaryRenderer: this.showtotalquantity.createDelegate(this)

        }]);
    this.repCm.defaultSortable = true;

    this.ProductQuaGrid = new Wtf.grid.GridPanel({
        store:  this.ProductQuaStore,
        cm: this.repCm,
        selModel: this.sm,
        plugins:[this.summary],
        viewConfig: {
            emptyText:WtfGlobal.getLocaleText("acc.common.norec"),
            forceFit: true
        }
    });
    Wtf.account.ProductdDetails.superclass.constructor.call(this, {
        border: false,
        layout: 'border',
        items:[{
            id:'panelPer',
            margins: '0 5 5 5',
            region: 'center',
            layout: 'fit',
            split: true,
            border: false,
            items: this.ProductQuaGrid
        }]
    });
}

Wtf.extend(Wtf.account.ProductdDetails, Wtf.Panel, {
    onRender: function(config) {
        Wtf.account.ProductdDetails.superclass.onRender.call(this, config);
        this.ProductQuaStore.load({
            params: {               
                start: 0,
                limit: 30
            }
        });
      
    },
    showtotalquantity:function(val,m,rec){
        return WtfGlobal.summaryRenderer((parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL));
        },
    quantityRenderer:function(val,m,rec){
        return (parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)=="NaN")?parseFloat(0).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL):parseFloat(getRoundofValue(val)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
    }
   
});

