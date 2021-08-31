/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


function getJobWorkOrderGrid() {
    return new Wtf.JobWorkOrderGrid();
}

Wtf.JobWorkOrderGrid = function (config) {
    Wtf.apply(this, config);
    Wtf.JobWorkOrderGrid.superclass.constructor.call(this, config);
    /*
     * Create grid
     */
    
    this.createGrid();
};

Wtf.extend(Wtf.JobWorkOrderGrid, Wtf.Panel, {
     border: false,
    initComponent: function () {
        Wtf.JobWorkOrderGrid.superclass.initComponent.call(this);

    },
    onRender: function (config) {
        Wtf.JobWorkOrderGrid.superclass.onRender.call(this, config);
        this.add(this.JobworkGrid);
        this.Store.on('load', this.handleStoreOnLoad, this);
        this.Store.load();

    },
    createGrid: function () {
      this.Store = new Wtf.data.Store({
            url: "ACCJobWorkController/getJobWorks.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }),
             baseParams:{
                 workorderid:''
             }
        });
       
        this.sModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: false
        });
        this.srno = new Wtf.grid.RowNumberer();
       

        this.JobworkGrid = new Wtf.grid.GridPanel({
            layout:'fit',
            autoScroll: true,
            sm: this.sModel,
            store:  this.Store,
            columns: [],
             baseCls:'gridFormFormat',
            loadMask: true,
            stripeRows :true,
            height: 340,
            border: false,
            viewConfig: {
//                    forceFit:true,
                    emptyText:WtfGlobal.getLocaleText("acc.common.norec")
                }
        });
    },
     handleStoreOnLoad: function () {
        var columns = [];
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: true
        });
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));

        columns.push(this.sm);

        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
            if(column.dataIndex == "excisedutychargees"){
                column.renderer=WtfGlobal.currencyRenderer;
            }else if(column.dataIndex == "productquantity"){
                column.renderer = this.unitRenderer;
            }else{
            column.renderer = WtfGlobal.deletedRenderer;
        }
            columns.push(column);
        },this);
//        Wtf.each(this.Store.reader.jsonData.columns, function (column) {
//
//            columns.push(column);
//        });
        this.JobworkGrid.getColumnModel().setConfig(columns);
        this.JobworkGrid.getView().refresh();

        if (this.Store.getCount() < 1) {
            this.JobworkGrid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.JobworkGrid.getView().refresh();
        }
    }
});