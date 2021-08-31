/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.store.ReportListStore', {
    extend: 'Ext.data.Store',
    model: 'ReportBuilder.model.CommonModel',
    remoteFilter: true,
    pageSize: 25,
    proxy: {
        type: 'ajax',
        timeout : 180000,
        url: 'ACCCreateCustomReport/getCustomReportList.do',
        actionMethods : getStoreActionMethods(),
        reader: {
            type: 'json',
            rootProperty: "data",
            keepRawData: true,
            totalProperty: 'totalCount'
        }
    }
});