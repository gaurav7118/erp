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
Wtf.data.PagingMemoryProxy = function(data, config) {
    Wtf.data.PagingMemoryProxy.superclass.constructor.call(this);
    this.data = data;
    Wtf.apply(this, config);
};

Wtf.extend(Wtf.data.PagingMemoryProxy, Wtf.data.MemoryProxy, {
    customFilter: null,

    initComponent: function(config) {
             this.addEvents({
                "pagechange": true
            });
        },

    load : function(params, reader, callback, scope, arg) {
        params = params || {};
        var result;
        try {
            result = reader.readRecords(this.data);
        } catch(e) {
            this.fireEvent("loadexception", this, arg, null, e);
            callback.call(scope, null, arg, false);
            return;
        }

        // filtering
        if (this.customFilter!=null) {
            result.records = result.records.filter(this.customFilter);
            result.totalRecords = result.records.length;
        } else if (params.filter!==undefined) {
            result.records = result.records.filter(function(el){
                if (typeof(el)=="object"){
                    var att = params.filterCol || 0;
                    return String(el.data[att]).match(params.filter)?true:false;
                } else {
                    return String(el).match(params.filter)?true:false;
                }
            });
            result.totalRecords = result.records.length;
        }

        // sorting
        if (params.sort!==undefined) {
            // use integer as params.sort to specify column, since arrays are not named
            // params.sort=0; would also match a array without columns
            var dir = String(params.dir).toUpperCase() == "DESC" ? -1 : 1;
                var fn = function(r1, r2){
                return r1==r2 ? 0 : r1<r2 ? -1 : 1;
                };
            var st = reader.recordType.getField(params.sort).sortType;
            result.records.sort(function(a, b) {
                var v = 0;
                if (typeof(a)=="object"){
                    v = fn(st(a.data[params.sort]), st(b.data[params.sort])) * dir;
                } else {
                    v = fn(a, b) * dir;
                }
                if (v==0) {
                    v = (a.index < b.index ? -1 : 1);
                }
                return v;
            });
        }

        // paging (use undefined cause start can also be 0 (thus false))
        if (params.start!==undefined && params.limit!==undefined) {
            result.records = result.records.slice(params.start, params.start+params.limit);
        }
                callback.call(scope, result, arg, true);
                this.fireEvent("pagechange", this.id,params.start,params.limit);
    }
});

