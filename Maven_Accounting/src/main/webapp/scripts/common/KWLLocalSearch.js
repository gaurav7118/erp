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

Wtf.KWLLocalSearch = function(config){
    Wtf.apply(this,config);
    
    this.searchField = this.searchField!=undefined?this.searchField:"";
    this.anyMatch = this.anyMatch === true ? '' : '^';
    this.caseSensitive = this.caseSensitive === true ? '' : 'i';
    this.emptyText = this.emptyText==undefined?WtfGlobal.getLocaleText("acc.audittrail.searchBTN"):this.emptyText;
    this.grid = null;

    Wtf.KWLLocalSearch.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.KWLLocalSearch, Wtf.form.TextField, {
    onRender: function(ct, position){
        Wtf.KWLLocalSearch.superclass.onRender.call(this, ct, position);
        this.el.dom.onkeyup = this.onKeyUp.createDelegate(this);
    },
    onKeyUp: function(e){
        if(this.grid) {
            var view = this.grid.getView();
            var sm = this.grid.getSelectionModel();
            var store = this.grid.getStore();
            var len = store.getCount();

            var isSingleSelect = sm.singleSelect;
            if(isSingleSelect){
                sm.singleSelect = false; //Reset Single Select to highlight all matching rows [SK]
            }
            //Reset all
            sm.clearSelections();
            view.scrollToTop();

            var filterStr = this.getValue();
            if(filterStr!="") {
                var regEx = filterStr;
                //Build regex
                if(!regEx.exec){
                    regEx = String(regEx);
                    regEx = new RegExp(this.anyMatch + Wtf.escapeRe(regEx), this.caseSensitive);
                }

                var firstInstanceAt = -1;
                //Lookup and Highlight all mathched
                for(var i=0; i<len; i++){
                    var rec=store.getAt(i);
                    if(regEx.test(rec.data[this.searchField])) {
                        sm.selectRow(i,true);
                        if(firstInstanceAt==-1){
                            firstInstanceAt = i;
                        }
                    }
                }

                //Scroll down to first instance
                if(firstInstanceAt > -1){
                    var rowHeight = view.getRow(firstInstanceAt).scrollHeight;
                    view.scroller.dom.scrollTop = rowHeight*firstInstanceAt;
                }
            }
            sm.singleSelect = isSingleSelect;   //Set Original Single Select Value
        }
    },

    applyGrid: function(grid){
        this.grid = grid;
    }

});
Wtf.reg('KWLLocalSearch', Wtf.KWLLocalSearch);

