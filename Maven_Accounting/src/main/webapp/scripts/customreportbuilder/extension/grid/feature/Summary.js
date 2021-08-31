/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.extension.grid.feature.Summary', {
    extend: 'Ext.grid.feature.Summary',
    alias: 'feature.reportsummary',
    grandSummaryNormalCls: "grand-summary-normal",
    grandSummaryLockedCls: "grand-summary-locked",
    grandSummaryTableCls: "grand-summary-table-view",
    
    init: function(grid) {
        //console.log('*** In init method of ReportBuilder.extension.grid.feature.Summary ***');
        var me = this,
            view = me.view,
            dock = me.dock;
        me.callParent(arguments);
        if (dock) {
            grid.addBodyCls(me.panelBodyCls + dock);
            grid.headerCt.on({
                add: me.onStoreUpdate,
                afterlayout: me.onStoreUpdate,
                scope: me
            });
            grid.on({
                beforerender: function() {
                    var tableCls = [
                            me.summaryTableCls
                        ];
                    if (view.columnLines) {
                        tableCls[tableCls.length] = view.ownerCt.colLinesCls;
                    }
                    me.summaryBar = grid.addDocked({
                        childEls: [
                            'innerCt',
                            'item'
                        ],
                        renderTpl: [
                            '<div id="{id}-innerCt" data-ref="innerCt" role="presentation">',
                            '<table id="{id}-item" data-ref="item" cellPadding="0" cellSpacing="0" class="' + tableCls.join(' ') + '">',
                            '<tr class="' + me.summaryRowCls + '"></tr>',
                            '</table>',
                            '</div>'
                        ],
                        scrollable: {
                            x: false,   
                            y: false
                        },
                        hidden: !me.showSummaryRow,
                        itemId: 'summaryBar',
                        cls: [
                            me.dockedSummaryCls,
                            me.dockedSummaryCls + '-' + dock
                        ],
                        xtype: 'component',
                        dock: dock,
                        weight: 10000000
                    })[0];
                },
                afterrender: function() {
//                    grid.getView().getScrollable().addPartner(me.summaryBar.getScrollable());
                    me.summaryBar.getScrollable().addPartner(grid.getView().getScrollable());
                    me.summaryBar.getScrollable().addPartner(grid.headerCt.getScrollable());
                    grid.getView().addCls(me.grandSummaryTableCls);
                    me.onStoreUpdate();
                },
                single: true
            });
            
            grid.ownerGrid.on({
                reconfigure: me.onReconfigure,
                scope: me
            });
            // Stretch the innerCt of the summary bar upon headerCt layout
            grid.headerCt.afterComponentLayout = Ext.Function.createSequence(grid.headerCt.afterComponentLayout, function() {
                var width = this.getTableWidth(),
                    innerCt = me.summaryBar.innerCt;
                me.summaryBar.item.setWidth(width);
                // "this" is the HeaderContainer. Its tooNarrow flag is set by its layout if the columns overflow.
                // Must not measure+set in after layout phase, this is a write phase.
                if (this.tooNarrow) {
                    width += Ext.getScrollbarSize().width;
                }
                innerCt.setWidth(width);
            });
        } else {
            if (grid.bufferedRenderer) {
                me.wrapsItem = true;
                view.addRowTpl(Ext.XTemplate.getTpl(me, 'fullSummaryTpl')).summaryFeature = me;
                view.on('refresh', me.onViewRefresh, me);
            } else {
                me.wrapsItem = false;
                me.view.addFooterFn(me.renderSummaryRow);
            }
        }
        grid.ownerGrid.on({
            beforereconfigure: me.onBeforeReconfigure,
            columnmove: me.onStoreUpdate,
            scope: me
        });
        me.bindStore(grid, grid.getStore());
    },
    onReconfigure: function(grid, store,columns,oldStore,oldColumns,eOpts) {
        var me=this;
        var isLocked = me.grid.isLocked;
        var customCls = isLocked ? me.grandSummaryLockedCls : me.grandSummaryNormalCls;
        if(columns.length > reportScrollMinColumn){
            me.summaryBar.addCls(customCls);
        }else{
            me.summaryBar.removeCls(customCls);
        }
    },
     createSummaryRecord: function(view) {
        var me = this,
            columns = view.headerCt.getGridColumns(),
            remoteRoot = me.remoteRoot,
            summaryRecord = me.summaryRecord,
            colCount = columns.length,
            i, column, dataIndex, summaryValue, modelData,defaultHeader,dataIndexForCal;
        if (!summaryRecord) {
            modelData = {
                id: view.id + '-summary-record'
            };
            summaryRecord = me.summaryRecord = new Ext.data.Model(modelData);
        }
        // Set the summary field values
        summaryRecord.beginEdit();
        if (remoteRoot) {
            summaryValue = me.generateSummaryData();
            if (summaryValue) {
                summaryRecord.set(summaryValue);
            }
        } else {
            for (i = 0; i < colCount; i++) {
                column = columns[i];
                defaultHeader = column.defaultHeader || "";
                // In summary records, if there's no dataIndex, then the value in regular rows must come from a renderer.
                // We set the data value in using the column ID.
                dataIndex = column.dataIndex || column.getItemId();
                // We need to capture this value because it could get overwritten when setting on the model if there
                // is a convert() method on the model.
                dataIndexForCal = dataIndex;
                if(defaultHeader == "Discount"){
                    dataIndexForCal = "flatdiscount";
                }
                summaryValue = me.getSummary(view.store, column.summaryType, dataIndexForCal);
                var newSummaryValue = 0;
                if(typeof summaryValue == "object"){
                    for(var key in summaryValue){
                        newSummaryValue = newSummaryValue + summaryValue[key];
                    }
                    summaryRecord.set(dataIndex, newSummaryValue);
                }else{
                    summaryRecord.set(dataIndex, summaryValue);
                }
                // Capture the columnId:value for the summaryRenderer in the summaryData object.
                me.setSummaryData(summaryRecord, column.getItemId(), summaryValue);
            }
        }
        summaryRecord.endEdit(true);
        // It's not dirty
        summaryRecord.commit(true);
        summaryRecord.isSummary = true;
        return summaryRecord;
    }
});