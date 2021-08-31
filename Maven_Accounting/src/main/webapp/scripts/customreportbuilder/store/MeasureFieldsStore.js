/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.store.MeasureFieldsStore', {
    extend: 'Ext.data.Store',
    model: 'ReportBuilder.model.CommonModel',
    proxy: {
        type: 'ajax',
        timeout : 180000,
        url: '',
        actionMethods : getStoreActionMethods(),
        reader: {
            type: 'json',
            rootProperty: "data"
        }
    }
});