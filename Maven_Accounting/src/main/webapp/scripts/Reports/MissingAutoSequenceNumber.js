/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



function callMissingAutoSequenceNumberWindowDynamicLoad(){
    var MissingAutoSequenceNumPanel=Wtf.getCmp("MissingAutoSequenceNumberReport");
    if(MissingAutoSequenceNumPanel==null){

        MissingAutoSequenceNumPanel = new Wtf.MissingAutoSequenceNumReport({
            id : 'MissingAutoSequenceNumberReport',
            border : false,
            layout: 'fit',
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.MissingAutoNumber.tabTitle"),Wtf.TAB_TITLE_LENGTH) ,
            tabTip:WtfGlobal.getLocaleText("acc.MissingAutoNumber.tabTitleTT"),
            closable: true,
            iconCls:'accountingbase creditnote'
        });
        Wtf.getCmp('as').add(MissingAutoSequenceNumPanel);
    }
    Wtf.getCmp('as').setActiveTab(MissingAutoSequenceNumPanel);
    Wtf.getCmp('as').doLayout();

}

//***************************************************************************8
Wtf.MissingAutoSequenceNumReport = function(config) {
    Wtf.apply(this, config);
    this.msgLmt = 30;
    this.MissingSequenceNumRec = new Wtf.data.Record.create([{           
        name:'SequenceFormat'
    },{
        name:'MissingNumber'
    }]);
    this.MissingSequenceNumStore = new Wtf.data.GroupingStore({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.MissingSequenceNumRec),
        url : 'ACCCombineReports/getMissingAutoSequenceNumber.do',
        groupField :'SequenceFormat',
        sortInfo : {
            field : 'SequenceFormat',
            direction : 'ASC'
        },
         baseParams:{
            start:0,
            limit:5
          
        }
        
    });
    this.MissingSequenceNumStore.load({
        params: {
            start:0,
            limit:this.msgLmt
        }
    });
    
    this.MissingSequenceNumCm = new Wtf.grid.ColumnModel([
    {
        header: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        width: 200,
        sortable:true,
        dataIndex: 'SequenceFormat'
    }, {
        header: WtfGlobal.getLocaleText("acc.MissingAutoNumber.MissingNumber"),
        width: 200,
        dataIndex: 'MissingNumber'
    }]);
  
    var ModuleDetails=[
        ['4','Cash Sales','Invoice','invoiceNumber','autocashsales','invoice'],['8','Cash Purchase','GoodsReceipt','goodsReceiptNumber','autocashpurchase','goodsreceipt'],
        ['2','Customer Invoices','Invoice','invoiceNumber','autoinvoice','invoice'],           ['24','Journal Entry','JournalEntry','entryNumber','autojournalentry','journalentry'],
        ['32','Purchase Requisition','PurchaseRequisition','prNumber','autorequisition','purchaserequisition'], ['33','Request For Quotation','RequestForQuotation','rfqNumber','autorequestforquotation','requestforquotation'],
        ['20','Sales Order','SalesOrder','salesOrderNumber','autoso','salesorder'],          ['18','Purchase Order','PurchaseOrder','purchaseOrderNumber','autopo','purchaseorder'],
        ['12','Credit Note','CreditNote','creditNoteNumber','autocreditmemo','creditnote'],          ['10','Debit Note','DebitNote','debitNoteNumber','autodebitnote','debitnote'],
        ['6','Vendor Invoice','GoodsReceipt','goodsReceiptNumber','autogoodsreceipt','goodsreceipt'],    ['27','Delivery Order','DeliveryOrder','deliveryOrderNumber','autodo','deliveryorder'],
        ['23','Vendor Quotation','VendorQuotation','quotationNumber','autovenquotation','vendorquotation'], ['28','Goods Receipt Order','GoodsReceiptOrder','goodsReceiptOrderNumber','autogro','grorder'],
        ['22','Customer Quotation','Quotation','quotationNumber','autoquotation','quotation'],     ['14','Make Payment','Payment','paymentNumber','autopayment','payment'],
        ['29','Sales Return','SalesReturn','salesReturnNumber','autosr','salesreturn'],       ['31','Purchase Return','PurchaseReturn','purchaseReturnNumber',' autopr','purchasereturn'],
        ['16','Receive Payment','Receipt','receiptNumber','autoreceipt','receipt'],                    ['30','Product','Product','productid','autoproductid','product'],
        ['25','Customer','Customer','acccode','autocustomerid','customer'], ['26','Vendor','Vendor','acccode','autovendorid','vendor']
    ]
  
  
    this.ModuleStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'moduleid'
        },{           
            name:'modulename'
        },{           
            name:'pojoname'
        },{           
            name:'orderby'
        },{           
            name:'mode'
        },{           
            name:'tablename'
        }],
        data :ModuleDetails,
        sortInfo: {
            field: 'modulename',
            direction: "ASC"
        }
    });
  
    this.groupCombo = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName"),  
        store: this.ModuleStore,
        mode: 'local',
        triggerAction: 'all',
        editable: false,
        emptyText: WtfGlobal.getLocaleText("acc.field.SelectModule..."),
        allowBlank: false,
        width: 200,
        valueField: 'mode',
        displayField: 'modulename'
    });
    
    this.groupCombo.on('select', this.onGroupComboSelect, this);
    
    
    this.sequenceFormatStoreRec = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'value'
        },{
            name: 'oldflag'
    }]);
    this.sequenceFormatStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.sequenceFormatStoreRec),
        url : "ACCCompanyPref/getSequenceFormatStore.do",
        params: {
            mode:this.groupCombo.getValue().trim()
        }
    });
    
    this.SequenceFormatCombo = new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),  
        store: this.sequenceFormatStore,
        mode: 'local',
        triggerAction: 'all',
        editable: false,
        allowBlank: false,
        emptyText: WtfGlobal.getLocaleText("acc.field.SelectSequenceFromat"),
        width: 200,
        valueField:'id',
        displayField:'value'
    });

    this.sequenceFormatStore.on("load", function(combo) {     
       
        var index1 =this.sequenceFormatStore.find('oldflag','true');
        if(index1 != -1){
            var storerec=this.sequenceFormatStore.getAt(index1);                        
            this.sequenceFormatStore.remove(storerec);
        }
        var index =this.sequenceFormatStore.find('id','NA');
        if(index != -1){
            var storerec1=this.sequenceFormatStore.getAt(index);                        
            this.sequenceFormatStore.remove(storerec1);
        }
        var id="";
        for(var i=0;i<this.sequenceFormatStore.getCount();i++){
            var rec=this.sequenceFormatStore.getAt(i);
            id +=rec.data.id+",";
        }  
        var record = new this.sequenceFormatStoreRec({
            id: id,
            value: "All",
            oldflag: "false"
        });
        this.sequenceFormatStore.insert(0,record);
        this.SequenceFormatCombo.setValue(id);
      
    }, this);
         
    this.gridView = new Wtf.grid.GroupingView({
        forceFit:true,
        showGroupName: true,
        enableNoGroups:true, // REQUIRED!
        hideGroupedColumn: false,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    });
    var gridSummary = new Wtf.grid.GroupSummary({});
    this.MissingSequenceNumGrid = new Wtf.grid.GridPanel({
        store:  this.MissingSequenceNumStore,
        cm: this.MissingSequenceNumCm,
        viewConfig:this.gridView ,
        plugins: [gridSummary],
//        loadMask:true,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: this.msgLmt,
            id: "pagingtoolbar" + this.id,
            store: this.MissingSequenceNumStore,
            displayInfo: true,
            displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        })
    });
    if(this.groupCombo.getRawValue() != "" ){
        this.MissingSequenceNumStore.on('beforeload', function(){
            var currentBaseParams = this.MissingSequenceNumStore.baseParams;
            var index =this.ModuleStore.find('mode',this.groupCombo.getValue());
            if(index != -1){
                var storerec=this.ModuleStore.getAt(index);  
                currentBaseParams.moduleid= storerec.data.moduleid,
                currentBaseParams.modulename=storerec.data.modulename,
                currentBaseParams.pojoname =storerec.data.pojoname,
                currentBaseParams.tablename=storerec.data.tablename,
                currentBaseParams.orderby=storerec.data.orderby,
                currentBaseParams.sequenceFormat= this.SequenceFormatCombo?this.SequenceFormatCombo.getValue():"",
                this.MissingSequenceNumStore.baseParams = currentBaseParams;
            }
        }, this);
    }
   this.MissingSequenceNumStore.on('load',function(store,rec,option){
        if(rec.length==0){
            this.MissingSequenceNumGrid.getView().refresh(true); 
        }
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    //     this.reportGrid.getView().emptyText = "<div class='grid-empty-text'>Ja ghari</div>";
 
    this.FetchButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.agedPay.fetch"),
        tooltip: WtfGlobal.getLocaleText("acc.field.FetchMissingNumbers"),
        disabled: false,
        iconCls:'accountingbase fetch',
        scope: this,
        handler:this.FetchMissingNumber

    });
    Wtf.MissingAutoSequenceNumReport.superclass.constructor.call(this, {
        border: false,
        layout: 'border',
        items:[{
            id:'panelPer'+this.id,
            margins: '0 5 5 5',
            region: 'center',
            layout: 'fit',
            title: WtfGlobal.getLocaleText("acc.field.ListofMissingNumbers"),
            split: true,
            border: false,
            items: this.MissingSequenceNumGrid,
            tbar: [WtfGlobal.getLocaleText("acc.field.ModuleName1"),this.groupCombo,WtfGlobal.getLocaleText("acc.field.SequenceFormat1"),this.SequenceFormatCombo,this.FetchButton,this.resetBttn],
            bbar: []
        }]
    });
}

Wtf.extend(Wtf.MissingAutoSequenceNumReport, Wtf.Panel, {
    onRender: function(config) {
        Wtf.MissingAutoSequenceNumReport.superclass.onRender.call(this, config);
//        this.MissingSequenceNumGrid.getView().emptyText="Test";
//        this.MissingSequenceNumGrid.getView().refresh();
    },
//    afterRender: function(config) {
//        Wtf.MissingAutoSequenceNumReport.superclass.afterRender.call(this, config);
//        this.MissingSequenceNumGrid.getView().emptyText="Test";
//        this.MissingSequenceNumGrid.getView().refresh();
//    },
    onGroupComboSelect: function() {
         
        this.sequenceFormatStore.load({
            params: {
                mode:this.groupCombo.getValue().trim()
            }
        });
        this.SequenceFormatCombo.setValue("All");
    },
    FetchMissingNumber: function() {
        if(this.groupCombo.getRawValue() != "" ){
        var index =this.ModuleStore.find('mode',this.groupCombo.getValue());
        if(index != -1){
            var storerec=this.ModuleStore.getAt(index);                        
                
            this.MissingSequenceNumStore.load({
                params: {
                    moduleid: storerec.data.moduleid,
                    modulename:storerec.data.modulename,
                    pojoname :storerec.data.pojoname,
                    tablename:storerec.data.tablename,
                    orderby:storerec.data.orderby,
                    sequenceFormat: this.SequenceFormatCombo.getValue()
                }
            });
        }
   }
   }
   ,
       handleResetClick:function(){
        this.SequenceFormatCombo.clearValue();
        this.groupCombo.clearValue();
        this.MissingSequenceNumStore.removeAll();
    }
});

