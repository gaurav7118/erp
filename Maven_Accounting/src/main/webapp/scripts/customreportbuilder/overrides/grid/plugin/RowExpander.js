/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.overrides.grid.plugin.RowExpander', {
    override: 'Ext.grid.plugin.RowExpander',
    addExpander: function(expanderGrid) {
//        console.log('*** In addExpander method of ReportBuilder.overrides.grid.plugin.RowExpander ***');
        var me = this;
        me.grid = expanderGrid;
        me.expanderColumn = expanderGrid.headerCt.insert(0, me.getHeaderConfig());
        //    expanderGrid.getSelectionModel().injectCheckbox = 1;
        if(expanderGrid.getSelectionModel().injectCheckbox == 0) expanderGrid.getSelectionModel().injectCheckbox = 1;   //Here is change.
        return me;
    },
    
     isCollapsed: function (rowIdx) {
         var me = this,
             rowNode = me.view.getNode(rowIdx),
             row = Ext.fly(rowNode, '_rowExpander');
         return row.hasCls(me.rowCollapsedCls)
     },


     collapse: function (rowIdx) {
         if (this.isCollapsed(rowIdx) == false) {
             this.toggleRow(rowIdx, this.grid.getStore().getAt(rowIdx));
         }
     },


     collapseAll: function () {
         for (var i = 0; i < this.grid.getStore().getCount(); i++) {
             this.collapse(i);
         }
     },


     expand: function (rowIdx) {
         if (this.isCollapsed(rowIdx) == true) {
             this.toggleRow(rowIdx, this.grid.getStore().getAt(rowIdx));
         }
     },


     expandAll: function () {
         for (var i = 0; i < this.grid.getStore().getCount(); i++) {
            this.expand(i);
        }
     }
});

