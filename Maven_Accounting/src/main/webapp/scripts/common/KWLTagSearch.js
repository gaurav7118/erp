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
Wtf.KWLTagSearch = function(config){
    Wtf.KWLTagSearch.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.KWLTagSearch, Wtf.form.TextField, {
    Store: null,
    emptyText  : WtfGlobal.getLocaleText("acc.audittrail.searchBTN"),
    StorageArray: null,
    limit: this.limit,
    listeners: {     //ERP-6081[SJ]
        render : function(c) {
            Wtf.QuickTips.register({
                target: c.getEl(),
                text: this.emptyText 
            });
        }
    },
    initComponent: function(){
        Wtf.KWLTagSearch.superclass.initComponent.call(this);
        this.addEvents({
            'SearchComplete': true
        });
    },
    timer:new Wtf.util.DelayedTask(this.callKeyUp),
    setPage: function(val) {
        this.limit = val;
    },
    onRender: function(ct, position){
        Wtf.KWLTagSearch.superclass.onRender.call(this, ct, position);
        this.el.dom.onkeyup = this.onKeyUp.createDelegate(this);
    },
    onKeyUp: function(e){
        if(this.Store) {
            if (this.getValue() != "") {
                this.timer.cancel();
                this.timer.delay(1000,this.callKeyUp,this);
            }
            else {
                this.Store.reload({
                    params: {
                        start: 0,
                        limit: this.limit,
                        ss: ""
                    }
                });
            }
            this.fireEvent('SearchComplete', this.Store);
        }
    },
    callKeyUp: function() {

     this.Store.reload({
          params: {
              start: 0,
              limit: this.limit,
              ss: this.getValue()
          }
      });
    },
    StorageChanged: function(store){
        this.Store = store;
        this.StorageArray = this.Store.getRange();
    }
});
Wtf.reg('KWLTagSearch', Wtf.KWLTagSearch);


Wtf.KWLQuickSearchUseFilter = function(config){
    Wtf.KWLQuickSearchUseFilter.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.KWLQuickSearchUseFilter, Wtf.form.TextField, {
    Store: null,
    initComponent: function(){
        Wtf.KWLQuickSearchUseFilter.superclass.initComponent.call(this);
    },
    onRender: function(ct, position){
        Wtf.KWLQuickSearchUseFilter.superclass.onRender.call(this, ct, position);
        this.el.dom.onkeyup = this.onKeyUp.createDelegate(this);
    },
    onKeyUp: function(e){
        this.Store.filter(this.field,this.getValue())
    },
    StorageChanged: function(store){
        this.Store = store;
        this.StorageArray = this.Store.getRange();
    }
});

/*
 * A GridPanel class with live search support.
 */
Wtf.KWLHighlightSearchTag = Wtf.extend(Wtf.KWLTagSearch, {
    
    /*
     * @private
     * search value initialization
     */
    searchValue: null,
    
    /**
     * @private
     * The row indexes where matching strings are found. (used by previous and next buttons)
     */
    indexes: [],
    
    /**
     * @private
     * The row index of the first search, it could change if next or previous buttons are used.
     */
    currentIndex: null,
    
    /**
     * @private
     * The generated regular expression used for searching.
     */
    searchRegExp: null,
    
    /**
     * @private
     * Case sensitive mode.
     */
    caseSensitive: false,
    
    /**
     * @private
     * Regular expression mode.
     */
    regExpMode: false,
    
    /**
     * @cfg {String} matchCls
     * The matched string css class.
     */
    matchCls: 'x-livesearch-match',
    
    defaultStatusText: 'Nothing Found',
    
    // detects html tag
    tagsRe: /(&nbsp;|<([^>]+)>)/ig,
    
    // DEL ASCII code
    tagsProtect: '\x0f',
    
    // detects regexp reserved word
    regExpProtect: /\\|\/|\+|\\|\.|\[|\]|\{|\}|\?|\$|\*|\^|\|/gm,
    
    // Component initialization override: adds the top and bottom toolbars and setup headers renderer.
    initComponent: function() {
        var me = this;
        if(me.grid){
            me.gridView = me.grid.view;
            if(!me.Store){
                me.Store = me.grid.store;
            }
            me.Store.on("load",function(){
                me.onTextFieldChange();
            },me);
            
            me.gridView.findCell = me.findCell.createDelegate(me.gridView);
        }
        
        Wtf.KWLHighlightSearchTag.superclass.initComponent.apply(me, arguments);
    },
    findCell : function(el){
        if(!el){
            return false;
        }
        return this.fly(el).findParent(this.cellSelector, 4); //changed count from 3 to 4
    },
    
    /**
     * In normal mode it returns the value with protected regexp characters.
     * In regular expression mode it returns the raw value except if the regexp is invalid.
     * @return {String} The value to process or null if the textfield value is blank or invalid.
     * @private
     */
    getSearchValue: function() {
        var me = this,
        value = me.getValue();
            
        if (value === '') {
            return null;
        }
        if (!me.regExpMode) {
            value = value.replace(me.regExpProtect, function(m) {
                return '\\' + m;
            });
        } else {
            try {
                new RegExp(value);
            } catch (error) {
                //                me.statusBar.setStatus({
                //                    text: error.message,
                //                    iconCls: 'x-status-error'
                //                });
                return null;
            }
            // this is stupid
            if (value === '^' || value === '$') {
                return null;
            }
        }

        return value;
    },
    
    /**
     * Finds all strings that matches the searched value in each grid cells.
     * @private
     */
    onTextFieldChange: function() {
        var me = this,
        count = 0;

        me.gridView.refresh();
        // reset the statusbar
        me.searchValue = me.getSearchValue();
        me.indexes = [];
        me.currentIndex = null;
        try{

            if (me.searchValue !== null) {
                me.searchRegExp = new RegExp(me.searchValue, 'g' + (me.caseSensitive ? '' : 'i'));
             
                me.grid.store.each(function(record, idx) {
                    //                 var td = Wtf.fly(me.view.getNode(idx)).down('td'),
                    var td,cell, matches, cellHTML;
                    //                 var td = Wtf.DomQuery.select('td',Wtf.fly(me.view.getRow(idx)));
                    var tdArr = Wtf.DomQuery.select('td',me.gridView.getRow(idx));
                    for(var tdCount=0 ; tdCount<tdArr.length ; tdCount++) {
                        try{
                     
                            td = tdArr[tdCount];
                            cell = Wtf.DomQuery.selectNode("div[class^=x-grid3-cell-inner]",td);
                            if(cell){
                                matches = cell.innerHTML.match(me.tagsRe) || [];
                                cellHTML = cell.innerHTML.replace(me.tagsRe, me.tagsProtect);
                     
                                // populate indexes array, set currentIndex, and replace wrap matched string in a span
                                cellHTML = cellHTML.replace(me.searchRegExp, function(m) {
                                    count += 1;
                                    if (me.indexes.indexOf(idx) === -1) {
                                        me.indexes.push(idx);
                                    }
                                    if (me.currentIndex === null) {
                                        me.currentIndex = idx;
                                    }
                                    return '<span class="' + me.matchCls + '">' + m + '</span>';
                                });
                                // restore protected tags
                                Wtf.each(matches, function(match) {
                                    cellHTML = cellHTML.replace(me.tagsProtect, match); 
                                });
                                // update cell html
                                cell.innerHTML = cellHTML;
                            }
                        }catch(e){
                            clog(e);
                        }
                    }
                }, me);

                // results found
                if (me.currentIndex !== null) {
//                    me.grid.getSelectionModel().selectRow(me.currentIndex);
                //                 me.statusBar.setStatus({
                //                     text: count + ' matche(s) found.',
                //                     iconCls: 'x-status-valid'
                //                 });
                }
            }

            // no results found
            if (me.currentIndex === null) {
                //             me.getSelectionModel().deselectAll();
                me.grid.getSelectionModel().clearSelections();
            }

            // force textfield focus
            me.focus();
        }catch(e){
            clog(e);
        }
    },
    
    /**
     * Selects the previous row containing a match.
     * @private
     */   
    onPreviousClick: function() {
        var me = this,
        idx;
            
        if ((me.indexes !=undefined) && ((idx = me.indexes.indexOf(me.currentIndex)) !== -1)) {
            me.currentIndex = me.indexes[idx - 1] || me.indexes[me.indexes.length - 1];
            me.getSelectionModel().selectRow(me.currentIndex);
        }
    },
    
    /**
     * Selects the next row containing a match.
     * @private
     */    
    onNextClick: function() {
        var me = this,
        idx;
             
        if ((me.indexes !=undefined) && ((idx = me.indexes.indexOf(me.currentIndex)) !== -1)) {
            me.currentIndex = me.indexes[idx + 1] || me.indexes[0];
            me.getSelectionModel().selectRow(me.currentIndex);
        }
    },
    
    /**
     * Switch to case sensitive mode.
     * @private
     */    
    caseSensitiveToggle: function(checkbox, checked) {
        this.caseSensitive = checked;
        this.onTextFieldChange();
    },
    
    /**
     * Switch to regular expression mode
     * @private
     */
    regExpToggle: function(checkbox, checked) {
        this.regExpMode = checked;
        this.onTextFieldChange();
    }
});
