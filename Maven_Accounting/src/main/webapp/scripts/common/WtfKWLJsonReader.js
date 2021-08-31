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
Wtf.data.KwlDataReader = function(meta, recordType){
    
    this.meta = meta;
    this.recordType = recordType instanceof Array ? 
        Wtf.data.Record.create(recordType) : recordType;
};  
Wtf.data.KwlDataReader.prototype = {

};   
Wtf.extend(Wtf.data.KwlDataReader, Wtf.util.Observable); 

Wtf.data.KwlJsonReader = function(meta, recordType){
     meta = meta || {};
     Wtf.data.KwlJsonReader.superclass.constructor.call(this, meta, recordType);
      this.events = {
        aftereval : true
    };
     this.on("aftereval", this.jsonErrorResponseHandler);
    
 };
 Wtf.extend(Wtf.data.KwlJsonReader, Wtf.data.KwlDataReader, {
         read : function(response){
                var json = response.responseText;
                var o = eval("("+json+")");
                if (o && o.valid==false) {
                    signOut("timeout");
                }
                else{
					  o = o.data;
			    }
                if(!o) {
                    throw {message: "JsonReader.read: Json object not found"};
                }
                if(o.metaData){
                    delete this.ef;
                    this.meta = o.metaData;
                    this.recordType = Wtf.data.Record.create(o.metaData.fields);
                    this.onMetaChange(this.meta, this.recordType, o);
                }
                
                
                //this.fireEvent("aftereval", o, this, response);
                return this.readRecords(o);
            },
       
       onMetaChange : function(meta, recordType, o){

       },

     jsonErrorResponseHandler:function (json, reader, response) {
          if (json && !json.valid) {
		signOut("timeout");
          }
     },
    
    simpleAccess: function(obj, subsc) {
    	return obj[subsc];
    },

	
    getJsonAccessor: function(){
        var re = /[\[\.]/;
        return function(expr) {
            try {
                return(re.test(expr))
                    ? new Function("obj", "return obj." + expr)
                    : function(obj){
                        return obj[expr];
                    };
            } catch(e){}
            return Wtf.emptyFn;
        };
    }(),

    
    readRecords : function(o){
        
        this.jsonData = o;
        var s = this.meta, Record = this.recordType,
            f = Record.prototype.fields, fi = f.items, fl = f.length;


        if (!this.ef) {
            if(s.totalProperty) {
	            this.getTotal = this.getJsonAccessor(s.totalProperty);
	        }
	        if(s.successProperty) {
	            this.getSuccess = this.getJsonAccessor(s.successProperty);
	        }
	        this.getRoot = s.root ? this.getJsonAccessor(s.root) : function(p){return p;};
	        if (s.id) {
	        	var g = this.getJsonAccessor(s.id);
	        	this.getId = function(rec) {
	        		var r = g(rec);
		        	return (r === undefined || r === "") ? null : r;
	        	};
	        } else {
	        	this.getId = function(){return null;};
	        }
            this.ef = [];
            for(var i = 0; i < fl; i++){
                f = fi[i];
                var map = (f.mapping !== undefined && f.mapping !== null) ? f.mapping : f.name;
                this.ef[i] = this.getJsonAccessor(map);
            }
        }

    	var root = this.getRoot(o), c = root.length, totalRecords = c, success = true;
    	if(s.totalProperty){
            var v = parseInt(this.getTotal(o), 10);
            if(!isNaN(v)){
                totalRecords = v;
            }
        }
        if(s.successProperty){
            var v = this.getSuccess(o);
            if(v === false || v === 'false'){
                success = false;
            }
        }
        var records = [];
	    for(var i = 0; i < c; i++){
		    var n = root[i];
	        var values = {};
	        var id = this.getId(n);
	        for(var j = 0; j < fl; j++){
	            f = fi[j];
                var v = this.ef[j](n);
                values[f.name] = f.convert((v !== undefined) ? v : f.defaultValue);
	        }
	        var record = new Record(values, id);
	        record.json = n;
	        records[i] = record;
	    }
	    return {
	        success : success,
	        records : records,
	        totalRecords : totalRecords
	    };
    }
     });
