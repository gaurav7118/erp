/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.store.ModuleCategoryStore', {
    extend: 'Ext.data.Store',
    fields: ['moduleCatId', 'moduleCatName'],
    //autoLoad: true,
    proxy: {
        type: 'ajax',
        timeout : 180000,
        url: 'ACCCreateCustomReport/getModulesCategories.do',
        actionMethods : getStoreActionMethods(),
        reader: {
            keepRawData: true,
            type: 'json',
            rootProperty: "data"
        }
    }
});