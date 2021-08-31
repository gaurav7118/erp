/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.ResolveConflictLabourMachineReport = function(config) {
    Wtf.apply(this, config);
    Wtf.account.ResolveConflictLabourMachineReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.ResolveConflictLabourMachineReport, Wtf.Panel, {
    
    onRender: function(config) {
        /*
         * create panel to show grid
         */
        this.createPanel();
        this.add(this.conflictpan);
        /*
         * fetch data in report
         */
        this.fetchStatement();
        Wtf.account.ResolveConflictLabourMachineReport.superclass.onRender.call(this, config);
    },
    
    createPanel: function() {
        this.createWestPanel();
        this.createCenterPanel();

        this.conflictpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.westPanel, this.centerPanel]
        });
    },
    
    createWestPanel: function() {
        this.westStore = new Wtf.data.Store({
            url: "ACCLabourCMN/getResolveConflictResourcesColumnModel.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.westGrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.westStore,
            columns: [],
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.westStore.on('load', this.handleStoreOnLoad.createDelegate(this, [this.westStore, this.westGrid]), this);
        
        this.westPanel = new Wtf.Panel({
            title: WtfGlobal.getLocaleText("acc.resolveconflict.westpaneltitle"),
            tabTip: WtfGlobal.getLocaleText("acc.resolveconflict.westpaneltabtip"),
            region: "west",
            layout: 'fit',
            split:true,
            collapsible: true,
            width:300,
            autoScroll: true,
            border: true,
            bodyStyle: {"background-color": 'white'},
            items: [this.westGrid]
        });
    },
    
    createCenterPanel: function() {
        this.centerStore = new Wtf.data.Store({
            url: "ACCLabourCMN/getResolveConflictTasksColumnModel.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            })
        });
        this.centerGrid = new Wtf.grid.GridPanel({
            layout: 'fit',
            region: "center",
            store: this.westStore,
            columns: [],
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: false,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.centerStore.on('load', this.handleStoreOnLoad.createDelegate(this, [this.centerStore, this.centerGrid]), this);
        
        this.resolveBttn = new Wtf.Toolbar.Button({
            text: "Resolve"
        });
        this.centerPanel = new Wtf.Panel({
            title: WtfGlobal.getLocaleText("acc.resolveconflict.centerpaneltitle"),
            tabTip: WtfGlobal.getLocaleText("acc.resolveconflict.centerpaneltabtip"),
            region: "center",
            layout: 'fit',
            autoScroll: true,
            border: true,
            bodyStyle: {"background-color": 'white'},
            items: [this.centerGrid],
            tbar: [this.resolveBttn]
        });
    },
    
    fetchStatement: function() {
        this.westStore.load();
        this.centerStore.load();
    },
    
    handleStoreOnLoad: function(store, grid) {
        var columns = [];
        columns.push(new Wtf.grid.RowNumberer({
            width: 30
        }));
        Wtf.each(store.reader.jsonData.columns, function(column) {
            if (column.renderer) {
                column.renderer = eval('(' + column.renderer + ')');
            }
            columns.push(column);
        });
        grid.getColumnModel().setConfig(columns);
        grid.getView().refresh();

        if (store.getCount() < 1) {
            grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            grid.getView().refresh();
        }
    }
});