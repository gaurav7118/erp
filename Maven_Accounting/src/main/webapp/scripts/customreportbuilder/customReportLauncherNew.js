/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.Loader.setConfig({
    enabled: true
});

Ext.Loader.setPath('ReportBuilder', '../../scripts/customreportbuilder');

Ext.require('ReportBuilder.view.ReportList');

Ext.onReady(function() {   // On ready Function. Loads on document loading
    
    Ext.tip.QuickTipManager.init();
    
    Ext.apply(Ext.tip.QuickTipManager.getQuickTip(), {
        dismissDelay: 0
    });
    
    var customReportList = Ext.create('Ext.tab.Panel', {
        fullscreen: true,
        requires: [
        'ReportBuilder.view.ReportList'
        ],
        id: 'mainTabPanel',
        defaults: {
            styleHtmlContent: true
        },
        items: [
        Ext.create('ReportBuilder.view.ReportList', {
            title:  ExtGlobal.getLocaleText("acc.common.CustomReports"),
            iconCls: 'pwnd report',
            tooltip: ExtGlobal.getLocaleText("acc.common.CustomReports"),
            layout: 'fit'
        })
        ]
    });
    
    var rootCenter = {
        xtype: "panel",
        id: "rootCenter",
        region: "center",
        layout: 'fit',
        height: '100%',
        autoHeight: true,
        autoScroll: true,
        border:false,
        items: [customReportList]
    };
    
    var topPanel = {
        xtype: "panel",
        region: "north",
        id: 'idtopPanel',
        border:false,
        bodyStyle:"background:transparent",
        width: "100%",
        contentEl:"header"
    };
    
    var viewport = Ext.create('Ext.Viewport', {
        id: 'reportViewport',
        layout: 'border',
        resizable: false,
        items: [rootCenter, topPanel],
        renderTo: Ext.getBody()
    });
});
