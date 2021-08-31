/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



function getLockPeriodInfoGrid() {
    return new Wtf.LockingPeriodInfoWin();
}

Wtf.LockingPeriodInfoWin = function(config) {
    Wtf.apply(this, config);
    Wtf.LockingPeriodInfoWin.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.LockingPeriodInfoWin, Wtf.Panel, {
    initComponent: function() {
        Wtf.LockingPeriodInfoWin.superclass.initComponent.call(this);
        this.createLockInfoGridGrid();

    },
    onRender: function(config) {
        Wtf.LockingPeriodInfoWin.superclass.onRender.call(this, config);
        this.LockingPeriodStore.load();
        this.add(this.LockeingPeriodGrid);
    },

    createLockInfoGridGrid: function(){
        Wtf.initialTaxPeriodLoading=true;
        this.LockingPeriodReader = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'periodname'},
        {name: 'periodclosed'},
        {name: 'level'},
        {name: 'startdate', type:'date'},
        {name: 'enddate', type:'date'},
        {name: 'leaf'},
        {name: 'periodtype'},
        {name: 'subperiodof'},
        ]);

        this.LockingPeriodStore = new Wtf.data.Store({
             url: 'accPeriodSettings/getTaxPeriods.do',
            baseParams:{
                periodtype:Wtf.TaxAccountingPeriods.YEAR
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.LockingPeriodReader)
        });
        
        this.LockingPeriodStore.on("load",function(){
            if(Wtf.initialTaxPeriodLoading){//Initial Loading--its true.For delete and create it is false
                this.expandCollapseGrid(true);
            }
        }, this);
        
        this.createColumnModel();
        
        this.LockeingPeriodGrid = new Wtf.grid.HirarchicalGridPanel({
            store: this.LockingPeriodStore,
            stripeRows :true,
            hirarchyColNumber:0,
            cm: this.cm,
            //            sm:this.sm2,
            border: false,
            plugins:this.pluginsArr,
            height:710,
            autoWidth:true,
            layout:'fit',
            viewConfig: {
                forceFit: true
            },
            clicksToEdit: 1
        });
      
       this.LockeingPeriodGrid.on('cellclick', this.handleCellClick, this);        
    },   
    expandCollapseGrid : function(expandCollapse){
        if(expandCollapse){//Collapse
            for(var i=0; i< this.LockeingPeriodGrid.getStore().data.length; i++){
                var rec=this.LockeingPeriodGrid.getStore().data.items[i].data;
                if(rec.level >= 0){
                    this.LockeingPeriodGrid.collapseRow(this.LockeingPeriodGrid.getView().getRow(i));
                }
            }
        } else{//Expand
            for(var i=0; i< this.LockeingPeriodGrid.getStore().data.length; i++){
                this.LockeingPeriodGrid.expandRow(this.LockeingPeriodGrid.getView().getRow(i));
            }
        }
    },
    createColumnModel:function(){
        this.pluginsArr=[];
        this.columnModle=[]
    
        var periodName={
            header: "Period Name",
            dataIndex: 'periodname',
            value:'Fiscal Year 2016',//later on i have to remove this
            width: 70

        };
        this.columnModle.push(periodName);
        
        var startDate={
            header: "Start Date",
            dataIndex: 'startdate',
            align:'center',
            value:new Date(),
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            width: 70
        };
        
        this.columnModle.push(startDate);
        
        var endDate={
            header: "End Date",
            dataIndex: 'enddate',
            align:'center',
            value:new Date(),
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            width: 70
        };
        
        this.columnModle.push(endDate);
        
        var deletebtn= {
            header : WtfGlobal.getLocaleText("acc.common.delete"),
            dataIndex: '',
            sortable: true,
            renderer:function(value, css, record, row, column, store){
                    return "<div class='delete pwnd delete-gridrow'  title="+WtfGlobal.getLocaleText("acc.field.DeleteTaxPeriod")+"></div>";
            }
        }
        this.columnModle.push(deletebtn);
        this.cm = new Wtf.grid.ColumnModel(this.columnModle);
    },
    handleCellClick: function (grid, rowIndex, columnIndex, e) {
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name of column on which you click ringh now
        if(fieldName == 'periodname' ||fieldName == 'startdate'||fieldName == 'enddate'){
            return;
        }
        if(e.getTarget("div[class='delete pwnd delete-gridrow']")) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedtaxperiod"),function(btn){
             
                if(btn=="yes") {
                    var record = grid.getStore().getAt(rowIndex);  // Get the Record on which you clicked 
                    Wtf.Ajax.requestEx({
                        url: 'accPeriodSettings/deleteSelectedTaxPeriod.do',
                        params: {
                            transactionid:record.data.id,
                            periodname:record.data.periodname //for entry in Audit Trial
                        }
                    },this,
                    function(resp){
                        if(resp.success == true) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),resp.msg ], 0);
                            Wtf.initialTaxPeriodLoading=false;
                            this.LockingPeriodStore.load();
                        } else {
                            Wtf.initialTaxPeriodLoading=false;
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"),resp.msg], 1);
                        }

                    },function(){
                        });

                }
            }, this);
        } 
        
    }
});
