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
Wtf.grid.RowExpander = function(config){
    Wtf.apply(this, config);

    this.addEvents({
        beforeexpand : true,
        expand: true,
        beforecollapse: true,
        collapse: true
    });

    Wtf.grid.RowExpander.superclass.constructor.call(this);

    if(this.tpl){
        if(typeof this.tpl == 'string'){
            this.tpl = new Wtf.Template(this.tpl);
        }
        this.tpl.compile();
    }

    this.state = {};
    this.bodyContent = {};
};

Wtf.extend(Wtf.grid.RowExpander, Wtf.util.Observable, {
    header: "",
    width: 20,
    sortable: false,
    fixed:true,
    dataIndex: '',
    id: 'expander',
    lazyRender : true,
    enableCaching: true,

    getRowClass : function(record, rowIndex, p, ds){
        p.cols = p.cols-1;
        var content = this.bodyContent[record.id];
        if(!content && !this.lazyRender){
            content = this.getBodyContent(record, rowIndex);
        }
        if(content){
            p.body = content;
        }
        return this.state[record.id] ? 'x-grid3-row-expanded' : 'x-grid3-row-collapsed';
    },

    init : function(grid){
        this.grid = grid;

        var view = grid.getView();
        view.getRowClass = this.getRowClass.createDelegate(this);

        view.enableRowBody = true;

        grid.on('render', function(){
            view.mainBody.on('mousedown', this.onMouseDown, this);
            
            if(view.lockedBody){
                view.lockedBody.on('mousedown', this.onMouseDown, this);
            }
        }, this);
    },

    getBodyContent : function(record, index){
        if(!this.enableCaching){
            return this.tpl.apply(record.data);
        }
        var content = this.bodyContent[record.id];
        if(!content){
            content = this.tpl.apply(record.data);
            this.bodyContent[record.id] = content;
        }
        return content;
    },

    onMouseDown : function(e, t){
        if(t.className == 'x-grid3-row-expander'){
            e.stopEvent();
            var row = e.getTarget('.x-grid3-row');
            this.toggleRow(row);
        }
    },

    renderer : function(v, p, record){
        p.cellAttr = 'rowspan="2"';
        return '<div class="x-grid3-row-expander">&#160;</div>';
    },

    beforeExpand : function(record, body, rowIndex){
        if(this.fireEvent('beforeexpand', this, record, body, rowIndex) !== false){
            if(this.tpl && this.lazyRender){
                body.innerHTML = this.getBodyContent(record, rowIndex);
            }
            return true;
        }else{
            return false;
        }
    },

    toggleRow : function(row){
        if(typeof row == 'number'){
            row = this.grid.view.getRow(row);
        }
        if(row) {
            this[Wtf.fly(row).hasClass('x-grid3-row-collapsed') ? 'expandRow' : 'collapseRow'](row);
        }
    },

    expandRow : function(row){
        var unlockRow  = row;
        var lockedRow  = row;
        if(typeof row == 'number'){
            unlockRow = this.grid.view.getRow(row);
            if(this.locked){
                lockedRow = this.grid.getView().getLockedRow(row);
            } 
        }else{
            if(this.locked){
                unlockRow = this.grid.getView().getRow(row.rowIndex);
            } 
        }
        var record = this.grid.store.getAt(unlockRow.rowIndex);
        
        var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', unlockRow);
        if(this.beforeExpand(record, body, unlockRow.rowIndex)){
            this.state[record.id] = true;
            Wtf.fly(unlockRow).replaceClass('x-grid3-row-collapsed', 'x-grid3-row-expanded');
            if(this.locked){
                Wtf.fly(lockedRow).replaceClass('x-grid3-row-collapsed', 'x-grid3-row-expanded');
            }
            this.fireEvent('expand', this, record, body, unlockRow.rowIndex);
        }
    },

    collapseRow : function(row){
        var unlockRow  = row;
        var lockedRow  = row;
        if(typeof row == 'number'){
            unlockRow = this.grid.view.getRow(row);
            if(this.locked){
                lockedRow = this.grid.getView().getLockedRow(row);
            } 
        }else{
            if(this.locked){
                unlockRow = this.grid.getView().getRow(row.rowIndex);
            } 
        }
        var record = this.grid.store.getAt(unlockRow.rowIndex);
        
        //        var unlockRow = row;
        //        if(this.locked){
        //            unlockRow = this.grid.getView().getRow(row.rowIndex);
        //        }
        
        var body = Wtf.fly(unlockRow).child('tr:nth(1) div.x-grid3-row-body', true);
        if(this.fireEvent('beforcollapse', this, record, body, unlockRow.rowIndex) !== false){
            this.state[record.id] = false;
            Wtf.fly(unlockRow).replaceClass('x-grid3-row-expanded', 'x-grid3-row-collapsed');
            
            if(this.locked){
                Wtf.fly(lockedRow).replaceClass('x-grid3-row-expanded', 'x-grid3-row-collapsed');
                lockedRow.style.height = unlockRow.offsetHeight-2 + "px";
            }
            this.fireEvent('collapse', this, record, body, unlockRow.rowIndex);
        }
    },
     collapseAll: function () {
         for (var i = 0; i < this.grid.getStore().getCount(); i++) {
             this.collapseRow(i);
         }
     },

     expandAll: function () {
         for (var i = 0; i < this.grid.getStore().getCount(); i++) {
            this.expandRow(i);
        }
     }
});
