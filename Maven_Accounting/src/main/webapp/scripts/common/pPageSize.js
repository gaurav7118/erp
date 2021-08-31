/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


Wtf.namespace('Wtf.common');
/**
 * @class Wtf.common.pPageSize
 * @extends Wtf.PagingToolbar
 * A combobox control that glues itself to a PagingToolbar's pageSize configuration property.
 * @constructor Create a new PageSize plugin.
 * @param {Object} config Configuration options
 */
Wtf.common.pPageSize = function(config){
    Wtf.apply(this, config);
};

Wtf.extend(Wtf.common.pPageSize, Wtf.util.Observable, {
    /**
     * @cfg {String} beforeText
     * Text to display before the comboBox
     */
    beforeText: 'Show',

    /**
     * @cfg {String} afterText
     * Text to display after the comboBox
     */
    afterText: 'items',

    /**
     * @cfg {Mixed} addBefore
     * Toolbar item(s) to add before the PageSizer
     */
    addBefore: '-',

    /**
     * @cfg {Mixed} addAfter
     * Toolbar item(s) to be added after the PageSizer
     */
    addAfter: null,

    /**
     * @cfg {Array} variations
     * Variations used for determining pageSize options
     */
    variations: [5, 10, 20, 50, 100],

    init: function(pagingToolbar){
        this.pagingToolbar = pagingToolbar;
        this.pagingToolbar.on('render', this.onRender, this);
    },

    //private
    addToStore: function(value){
        if (value > 0) {
            this.sizes.push([value]);
        }
    },

    //private
    updateStore: function(){
        var middleValue = this.pagingToolbar.pageSize, start;
        middleValue = (middleValue > 0) ? middleValue : 1;
        this.sizes = [];
        var v = this.variations;
        for (var i = 0, len = v.length; i < len; i++) {
            this.addToStore(middleValue - v[v.length - 1 - i]);
        }
        this.addToStore(middleValue);
        for (var i = 0, len = v.length; i < len; i++) {
            this.addToStore(middleValue + v[i]);
        }

        this.combo.store.loadData(this.sizes);
        this.combo.setValue(this.pagingToolbar.pageSize);
    },

    changePageSize: function(value){
        var pt = this.pagingToolbar;
        this.combo.collapse();
        value = parseInt(value) || parseInt(this.combo.getValue());
        value = (value > 0) ? value : 1;
        if (value < pt.pageSize) {
            pt.pageSize = value;
            var ap = Math.round(pt.cursor / value) + 1;
            var cursor = (ap - 1) * value;
            var store = pt.store;
            store.suspendEvents();
            for (var i = 0, len = cursor - pt.cursor; i < len; i++) {
                store.remove(store.getAt(0));
            }
            while (store.getCount() > value) {
                store.remove(store.getAt(store.getCount() - 1));
            }
            store.resumeEvents();
            store.fireEvent('datachanged', store);
            pt.cursor = cursor;
            var d = pt.getPageData();
            pt.afterTextEl.el.innerHTML = String.format(pt.afterPageText, d.pages);
            pt.field.dom.value = ap;
            pt.first.setDisabled(ap == 1);
            pt.prev.setDisabled(ap == 1);
            pt.next.setDisabled(ap == d.pages);
            pt.last.setDisabled(ap == d.pages);
            pt.updateInfo();
        }
        else {
            this.pagingToolbar.pageSize = value;
            this.pagingToolbar.doLoad(Math.floor(this.pagingToolbar.cursor / this.pagingToolbar.pageSize) * this.pagingToolbar.pageSize);
        }
        this.updateStore();
    },

    //private
    onRender: function(){
        var component = Wtf.form.ComboBox;
        this.combo = new component({
            store: new Wtf.data.SimpleStore({
                fields: ['pageSize'],
                data: []
            }),
            clearTrigger: false,
            displayField: 'pageSize',
            valueField: 'pageSize',
            editable: false,
            mode: 'local',
            triggerAction: 'all',
            width: 50
        });
        this.combo.on('select', this.changePageSize, this);
        this.updateStore();

        if (this.addBefore) {
            this.pagingToolbar.add(this.addBefore);
        }
        if (this.beforeText) {
            this.pagingToolbar.add(this.beforeText);
        }
        this.pagingToolbar.add(this.combo);
        if (this.afterText) {
            this.pagingToolbar.add(this.afterText);
        }
        if (this.addAfter) {
            this.pagingToolbar.add(this.addAfter);
        }
    }
})

Wtf.KWLRowNumberer = function(config){
    Wtf.apply(this, config);
    if(this.rowspan){
        this.renderer = this.renderer.createDelegate(this.pg);
    }
};

Wtf.KWLRowNumberer.prototype = {

    header: "",

    width: 23,

    sortable: false,


    fixed:true,
    dataIndex: '',
    id: 'numberer',
    rowspan: undefined,


    renderer : function(v, p, record, rowIndex, colIndex, store){
        if(this.rowspan){
            p.cellAttr = 'rowspan="'+this.rowspan+'"';
        }

        var i = store.lastOptions.params.start;

        if (isNaN(i)) {
            i = 0;
        }

        i = i + rowIndex + 1;

        return i;
    }
};
