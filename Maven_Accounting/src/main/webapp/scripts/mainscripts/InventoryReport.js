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
Wtf.account.InventoryReport = function(config){
    this.productID=config.productID;
    this.createStore();
    this.createColumnModel();
    this.createGrid();
//    this.resetBttn=new Wtf.Toolbar.Button({
//            text:'Reset',
//            tooltip :'Reset Search Results',
//            id: 'btnRec' + this.id,
//            scope: this,
//            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
//            disabled :false
//    });
//    this.resetBttn.on('click',this.handleResetClick,this);

    Wtf.apply(this,{
        layout : "fit",
        items:this.usergrid
//        tbar:[this.quickPanelSearch = new Wtf.KWLTagSearch({
//                emptyText:'Search by Product Name',
//                width: 200,
//                field: "productname"
//            }),
//            this.resetBttn]//,


//        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
//            pageSize: 15,
//            id: "pagingtoolbar" + this.id,
//            store: this.userds,
//            searchField: this.quickPanelSearch,
//            displayInfo:true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
//            emptyMsg: "No results to display",
//            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
//        })
    },config)
    Wtf.account.InventoryReport.superclass.constructor.call(this, config);
}
Wtf.extend( Wtf.account.InventoryReport, Wtf.Panel, {
//    handleResetClick:function(){
//        if(this.quickPanelSearch.getValue()){
//            this.quickPanelSearch.reset();
//              this.userds.load();
//        }
//    },
    createStore:function(){
        this.usersRec = new Wtf.data.Record.create([
            {name: 'carryin'},
            {name: 'date',type:'date'},
            {name: 'quantity'},
            {name: 'remquantity'},
            {name: 'rate'},
            {name: 'uom'},
            {name: 'amount'}
        ]);
        this.userds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.usersRec),
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCProduct/getInventory.do",
            baseParams:{
                mode:42,
                productid:this.productID
             }
        });
        this.userds.load({
            params:{
                start:0,
                limit:30
            }
        });
        WtfComMsgBox(29,4,true);
//        this.userds.on('datachanged', function() {
//            var p = this.pP.combo.value;
//            this.quickPanelSearch.setPage(p);
//         }, this);
         this.userds.on('load',this.storeloaded,this);
        },
    storeloaded:function(store){
         Wtf.MessageBox.hide();
//        this.quickPanelSearch.StorageChanged(store);
        if(this.userds.getCount()==0){
            this.usergrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.usergrid.getView().refresh();
        }
    },
    createColumnModel:function(){
        this.rowNo=new Wtf.grid.RowNumberer();//new Wtf.KWLRowNumberer();
        this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,{
            header: WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustment"),  //"Purchases/Sales",
            dataIndex: 'carryin',
            renderer:this.carryInRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.inventoryList.date"),  //"Date",
            dataIndex: 'date',
            align:'center',
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.inventoryList.qty"),  //"Quantity",
            renderer:this.unitRenderer,
            align:'right',
            dataIndex: 'quantity'
//        },{
//            header :'Rate',
//            dataIndex: 'rate',
//            renderer:WtfGlobal.currencyRenderer
//        },{
//            header :'Amount',
//            dataIndex: 'amount',
//            renderer:this.amountRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.inventoryList.remQty"),  //"Remaining Quantity",
            align:'right',
            renderer:this.unitRenderer,
            dataIndex: 'remquantity'
        }]);
    },
    createGrid:function(){
        this.usergrid = new Wtf.grid.GridPanel({
            stripeRows :true,
            store: this.userds,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }, 
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.userds,
                searchField: this.quickPanelSearch,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
           
            })
        })
     },
     amountRenderer:function(v,m,rec){
        return WtfGlobal.currencyRenderer(rec.data['quantity']*rec.data['rate']);
     },
     loadStore:function(){
        this.userds.load();
     },
    unitRenderer:function(value,metadata,record){
        var unit=record.data['uom'];
            value=parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+" "+unit;;
        return value;
    },
     carryInRenderer:function(val){
         if(val==true)
             return WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustment");
         else if(val==false)
             return WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustment");
         else 
             return WtfGlobal.getLocaleText("acc.inventoryList.StockAdjustment");
         return val;
     }
 }); 
