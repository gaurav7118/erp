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
Wtf.MyQuickSearch = function(config){
    Wtf.MyQuickSearch.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.MyQuickSearch, Wtf.form.TextField, {
    Store: null,
    StorageArray: null,
    initComponent: function(){
        Wtf.MyQuickSearch.superclass.initComponent.call(this);
        this.addEvents({
            'SearchComplete': true
        });
    },
    onRender: function(ct, position){
        Wtf.MyQuickSearch.superclass.onRender.call(this, ct, position);
        this.el.dom.onkeyup = this.onKeyUp.createDelegate(this);
    },
    onKeyUp: function(e){

//        if(this.Store)
//            {
//                this.Store.filter(this.field,this.getValue(),false,false);
//            }
    if(this.Store)
        {
            if (this.getValue() != "") {
                this.Store.removeAll();
                var i = 0;
                while (i < this.StorageArray.length) {
                    var str=new RegExp("^"+this.getValue()+".*$|\\s"+this.getValue()+".*$","i");

                    if (str.test(this.StorageArray[i].data[this.field])) {
                        this.Store.add(this.StorageArray[i]);
                    }
                    i++;
                }
            //dsSearch.add(this.Storage.getAt(this.Storage.find('Name',this.quickSearchTF.getValue())));
            }
            else {
                this.Store.removeAll();
                for (i = 0; i < this.StorageArray.length; i++) {
                    this.Store.insert(i, this.StorageArray[i]);
                }
            }
        }
        this.fireEvent('SearchComplete', this.Store);
    },
    StorageChanged: function(store){
        this.Store = store;
        this.StorageArray = this.Store.getRange();
    }
});
Wtf.reg('MyQuickSearch1', Wtf.MyQuickSearch);

