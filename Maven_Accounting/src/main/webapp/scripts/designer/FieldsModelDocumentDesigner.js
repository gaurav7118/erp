/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('FieldsModelDocumentDesigner', {
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
        },{
            name : 'isSummaryFormula',
            type: 'boolean'
        }
    ]
});
Ext.define('commonModel', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name : 'columnname'
        },
        {
            name : 'displayfield'
        },
        {
            name : 'hidecol',
            type: 'boolean'
        },
        {
            name : 'seq'
        },
        {
            name : 'fieldid'
        },
        {
            name : 'coltotal'
        },
        {
            name : 'colwidth'
        },
        {
            name : 'xtype'
        },
        {
            name : 'headerproperty'
        },
        {
            name : 'showtotal',
            type: 'boolean'
        },
        {
            name : 'recordcurrency'
        },
        {
            name : 'headercurrency'
        },
        {
            name : 'decimalpoint'
        },
        {
            name : 'commaamount',
            type: 'boolean'
        },
        {
            name : 'colno'
        },
        {
            name : 'isformula',
            type: 'boolean'
        },
        {
            name : 'formula'
        },
        {
            name : 'formulavalue'
        },
        {
            name : 'isSummaryFormula',
            type: 'boolean'
        }
    ]
});