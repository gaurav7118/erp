/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.store.ReportStore', {
    extend: 'Ext.data.Store',
    model: 'ReportBuilder.model.CommonModel',
    autoLoad: false,
    pageSize: 25,
    remoteFilter: true,
    proxy: {
        type: 'ajax',
        timeout : 900000,
        url: 'ACCCreateCustomReport/executeCustomReport.do',
        actionMethods : getStoreActionMethods(),
        reader: {
            type: 'json',
            rootProperty: "data",
            keepRawData: true,
            totalProperty: 'totalCount'
        }
    }
});