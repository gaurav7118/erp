/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define("ReportBuilder.extension.CustomListFilter", {
    extend : "Ext.grid.filters.filter.List",
    emptyText : ExtGlobal.getLocaleText("acc.field.Nodatatodisplay"),
    alias: 'grid.filter.customlist',
    type : 'customlist',
    createMenuItems : function (store) {
        console.log("In custom list filter");
        var me = this,
        menu = me.menu,
        len = store.getCount(),
        contains = Ext.Array.contains,
        listeners, itemDefaults, record, gid, idValue, idField, labelValue, labelField, i, item, processed;

        // B/c we're listening to datachanged event, we need to make sure there's a menu.
        if (len && menu) {
            listeners = {
                checkchange: me.onCheckChange,
                scope: me
            };

            itemDefaults = me.getItemDefaults();
            menu.suspendLayouts();
            menu.removeAll(true);
            gid = me.single ? Ext.id() : null;
            idField = me.idField;
            labelField = me.labelField;

            processed = [];

            for (i = 0; i < len; i++) {
                record = store.getAt(i);
                idValue = record.get(idField);
                labelValue = record.get(labelField);

                // Only allow unique values.
                if (labelValue == null || contains(processed, idValue)) {
                    continue;
                }

                processed.push(labelValue);

                // Note that the menu items will be set checked in filter#activate() if the value of the menu
                // item is in the cfg.value array.
                item = menu.add(Ext.apply({
                    text: labelValue,
                    group: gid,
                    value: idValue,
                    listeners: listeners
                }, itemDefaults));
            }

            menu.resumeLayouts(true);
        }else if(len === 0 && menu){
            menu.removeAll(true);     // If no data present in store then apply empty text.
            item = menu.add(Ext.apply({
                text : this.emptyText,
                iconCls : "nodata"
            }));
        }
    }
});