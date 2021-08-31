///* 
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//

function getAccountingLockPeriodInfoGrid() {
    return new Wtf.AccountingLockingPeriodInfoWin();
}

Wtf.AccountingLockingPeriodInfoWin = function(config) {
    Wtf.apply(this, config);
    Wtf.AccountingLockingPeriodInfoWin.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.AccountingLockingPeriodInfoWin, Wtf.Panel, {
    initComponent: function() {
        Wtf.AccountingLockingPeriodInfoWin.superclass.initComponent.call(this);
        this.createLockInfoGridGrid();

    },
    onRender: function(config) {
        Wtf.AccountingLockingPeriodInfoWin.superclass.onRender.call(this, config);
        this.LockingPeriodStore.load();
        this.add(this.LockeingPeriodGrid);

    },

    createLockInfoGridGrid: function(){
        Wtf.initialAccountingPeriodLoading=true;
        this.LockingPeriodReader = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'periodname'},
        {name: 'periodclosed'},
        {name: 'aptransactions'},
        {name :'artransactions'},
        {name: 'allgltransactions'},
        {name: 'allownonglchangestransactions'},
        {name: 'level'},
        {name: 'startdate', type:'date', dateFormat: WtfGlobal.getOnlyDateFormat()},
        {name: 'enddate', type:'date', dateFormat: WtfGlobal.getOnlyDateFormat()},
        {name: 'leaf'},
        {name: 'periodtype'},
        {name: 'subperiodof'},
        ]);

        this.LockingPeriodStore = new Wtf.data.Store({
             url: 'accPeriodSettings/getAccountingPeriods.do',
            baseParams:{
                periodtype:Wtf.TaxAccountingPeriods.YEAR
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.LockingPeriodReader)
        });
        
        this.LockingPeriodStore.on("load",function(){
            if(Wtf.initialAccountingPeriodLoading){//Initial Loading--its true.For delete and create it is false
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
                header: WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.periodname'),
                dataIndex: 'periodname',
                value:'Fiscal Year 2016',//later on i have to remove this
//                renderer:this.formatAccountName,
                width: 70

            };
        this.columnModle.push(periodName);
                var startDate={
            header: WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.startdate'),
            dataIndex: 'startdate',
            align:'center',
            value:new Date(),
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            width: 70
        };
        
        this.columnModle.push(startDate);
        
        var endDate={
            header: WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.enddate'),
            dataIndex: 'enddate',
            align:'center',
            value:new Date(),
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            width: 70
        };
        
        this.columnModle.push(endDate);
        
         var checkListColumn={
                header: "CheckList",
                dataIndex: 'checklist',
                align:'left',
                 renderer: this.showCheckList.createDelegate(this),
                width: 20

            };
//        this.columnModle.push(checkListColumn);
        
        var PeriodClosed= new Wtf.CheckColumnComponent({   
                        dataIndex: 'periodclosed',
                        header:WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.periodclosed'),
                        width: 65,
                        align:'center',
                        id: 'periodclosed',
                        scope:this
                    });
                    
        this.pluginsArr.push(PeriodClosed);
        this.columnModle.push(PeriodClosed);
        
        var APTransactions= new Wtf.CheckColumnComponent({   
                        dataIndex: 'aptransactions',
                        header: "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.aptransactions.tooltip') + "\">" + WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.aptransactions') + "<div>",
                        width: 65,
                        align:'center',
                        id: 'aptransactions',
                        scope:this
                    });
                    
        this.pluginsArr.push(APTransactions); 
        this.columnModle.push(APTransactions);
        
         var ARTransactions= new Wtf.CheckColumnComponent({   
                        dataIndex: 'artransactions',
                        header: "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.artransactions.tooltip') + "\">" + WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.artransactions') + "<div>",
                        width: 65,
                        align:'center',
                        id: 'artransactions',
                        scope:this
                    });
                    
        this.pluginsArr.push(ARTransactions); 
        this.columnModle.push(ARTransactions);
        
         var AllGLTransactions= new Wtf.CheckColumnComponent({   
                        dataIndex: 'allgltransactions',
                        header: "<div  wtf:qtip=\"" + WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.allgltransactions.tooltip') + "\">" + WtfGlobal.getLocaleText('acc.accountingperiodgrid.header.allgltransactions') + "<div>",
                        width: 65,
                        align:'center',
                        id: 'allgltransactions',
                        scope:this
                    });
                    
        this.pluginsArr.push(AllGLTransactions); 
        this.columnModle.push(AllGLTransactions);
        
         var AllowNonGlChangesTransactions= new Wtf.CheckColumnComponent({   
                        dataIndex: 'allownonglchangestransactions',
                        header:'Allow Non-G/L Changes',
                        width: 65,
                        align:'center',
                        id: 'allownonglchangestransactions',
                        scope:this
                    });
                    
        var deletebtn= {
            header : WtfGlobal.getLocaleText("acc.common.delete"),
            dataIndex: '',
            sortable: true,
            renderer:function(value, css, record, row, column, store){
                return "<div class='delete pwnd delete-gridrow'  title="+WtfGlobal.getLocaleText("acc.field.DeleteAccountingPeriod")+"></div>";
            }
        }
        this.columnModle.push(deletebtn);           
                    
//        this.pluginsArr.push(AllowNonGlChangesTransactions); 
//        this.columnModle.push(AllowNonGlChangesTransactions);
        this.cm = new Wtf.grid.ColumnModel(this.columnModle);
                
    },
     showCheckList:function(){//ERP-8199 :
         return "<div class='pwnd checklistaccouningperiod' wtf:qtip=\"Click to add products\"></div>";
    },
    setGridColumnchecks:function(grid, rowIndex, columnIndex, e,record){
        var record = grid.getStore().getAt(rowIndex);  // Get the Record on which you clicked
        var periodclosedflag=!record.data['periodclosed'];
        record.set('periodclosed',periodclosedflag);//to check/uncheck checkbox on user click
        
        for(var i=columnIndex+1; i<grid.getColumnModel().config.length-1;i++){
            var fieldName = grid.getColumnModel().getDataIndex(i);
            if(periodclosedflag){
                record.set(fieldName,true);//to check/uncheck checkbox on user click
            }else{
                record.set(fieldName,false);//to check/uncheck checkbox on user click
            }
            
            record.commit(); // commit the changes made by user
        }
    },
    setPeriodClosedColumnchecked:function(grid, rowIndex, columnIndex, e,record){
        var columnflag=false;
        var record = grid.getStore().getAt(rowIndex);  // Get the Record on which you clicked
        for(var i=4; i<grid.getColumnModel().config.length-1;i++){
            var fieldName = grid.getColumnModel().getDataIndex(i);
            if(record.data[fieldName]==true){
                columnflag=true;
            }else{
                columnflag=false;
                break;
            }
        }
        if(columnflag){
            record.set('periodclosed', true);//to check/uncheck checkbox on user click
            record.commit(); // commit the changes made by user
        }else{
            record.set('periodclosed', false);//to check/uncheck checkbox on user click
            record.commit(); // commit the changes made by user
        }
    },
    handleCellClick: function (grid, rowIndex, columnIndex, e) {
        var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name of column on which you click ringh now
        if(fieldName == 'periodname' ||fieldName == 'startdate'||fieldName == 'enddate' ){
            return;
        }
        
        if(fieldName == 'periodclosed'){//Period Closed Column
            this.setGridColumnchecks(grid, rowIndex, columnIndex,e);
        }else if(fieldName == 'aptransactions'||fieldName == 'artransactions'||fieldName == 'allgltransactions'){//AP,AR and GL Transactions Column
            var record = grid.getStore().getAt(rowIndex);  // Get the Record on which you clicked 
            record.set(fieldName, !record.data[fieldName]);//to check/uncheck checkbox on user click
            record.commit();
            this.setPeriodClosedColumnchecked(grid, rowIndex, columnIndex,e);//Checking whether A/P Transactions,A/R Transactions and All G/L Transactions are all checked or not then set Period Closed check to true
        }
        
        if(e.getTarget("div[class='delete pwnd delete-gridrow']")) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedaccountingperiod"),function(btn){
                if(btn=="yes") {
                    var record = grid.getStore().getAt(rowIndex);  // Get the Record on which you clicked 
                    Wtf.Ajax.requestEx({
                        url: 'accPeriodSettings/deleteSelectedAccountingPeriod.do',
                        params: {
                            transactionid:record.data.id,
                            periodname:record.data.periodname //for entry in Audit Trial
                        }
                    },this,
                    function(resp){
                        if(resp.success == true) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),resp.msg ], 0);
                             Wtf.initialAccountingPeriodLoading=false;
                             this.LockingPeriodStore.load();
                        } else {
                            Wtf.initialAccountingPeriodLoading=false;
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"),resp.msg], 1);
                        }

                    },function(){
                        });

                }
            }, this);

        } 
    },
    saveGridData: function () {
        var savePeriodInfoGridJson = [];
        this.LockingPeriodStore.each(function (record) {
            var recdata = record.data;
            if (recdata.periodtype == Wtf.TaxAccountingPeriods.MONTHS) { //recdata.periodtype == Wtf.TaxAccountingPeriods.MONTHS because we are saving only on month level
                savePeriodInfoGridJson.push({
                    id: recdata.id,
                    periodclosed: recdata.periodclosed,
                    aptransactions: recdata.aptransactions,
                    artransactions: recdata.artransactions,
                    allgltransactions: recdata.allgltransactions
                });
                        }

            }, this);

        var filterJson = {
            root:savePeriodInfoGridJson
        } 
        var json=Wtf.encode(filterJson);
        
        return json;
    }
        
        
});

