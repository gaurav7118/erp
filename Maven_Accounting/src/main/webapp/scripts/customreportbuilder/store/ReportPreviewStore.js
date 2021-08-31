/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.store.ReportPreviewStore', {
    extend: 'Ext.data.Store',
    model: 'ReportBuilder.model.CommonModel',
    autoLoad: false,
    pageSize: reportPreviewPageSize,
    remoteFilter: true,
    proxy: {
        type: 'ajax',
        timeout : 900000,
        url: 'ACCCreateCustomReport/executeCustomReportPreview.do',
        actionMethods : getStoreActionMethods(),
        reader: {
            type: 'json',
            rootProperty: "data",
            keepRawData: true,
            totalProperty: 'totalCount'
        }
    }
});