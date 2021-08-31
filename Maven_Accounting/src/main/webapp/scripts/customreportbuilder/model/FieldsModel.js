/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.model.FieldsModel', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'id'
        },
        {
            name: 'mainTable'
        },
        {
            name: 'defaultHeader'
        },
        {
            name: 'dbcolumnname'
        },
        {
            name: 'reftablename'
        },
        {
            name: 'reftablefk'
        },
        {
            name: 'reftabledatacolumn'
        },
        {
            name: 'xtype'
        },
        {
            name: 'columntype'
        },
        {
            name: 'customfield'
        },
        {
            name: 'isgrouping',
            type: 'boolean'
        }, {
            name: 'showasrowexpander',
            type: 'boolean'
        },{
            name: 'isforformulabuilder',
            type: 'boolean'
        },{
            name:'expression'
        }
    ]
});