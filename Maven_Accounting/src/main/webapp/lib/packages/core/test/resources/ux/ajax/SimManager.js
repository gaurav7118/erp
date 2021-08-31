Ext.define("Ext.ux.ajax.SimManager",{singleton:true,requires:["Ext.data.Connection","Ext.ux.ajax.SimXhr","Ext.ux.ajax.Simlet","Ext.ux.ajax.JsonSimlet"],defaultType:"basic",delay:150,ready:false,constructor:function(){this.simlets={}},getSimlet:function(B){var C=this,A=B.indexOf("?");if(A<0){A=B.indexOf("#")}if(A>0){B=B.substring(0,A)}return C.simlets[B]||C.defaultSimlet},getXhr:function(E,B,A,C){var D=this.getSimlet(B);if(D){return D.openRequest(E,B,A,C)}return null},init:function(A){var B=this;Ext.apply(B,A);if(!B.ready){B.ready=true;if(!("defaultSimlet" in B)){B.defaultSimlet=new Ext.ux.ajax.Simlet({status:404,statusText:"Not Found"})}B._openRequest=Ext.data.Connection.prototype.openRequest;Ext.data.Connection.override({openRequest:function(D,C,E){var F=!D.nosim&&B.getXhr(C.method,C.url,D,E);if(!F){F=this.callParent(arguments)}return F}});if(Ext.data.JsonP){Ext.data.JsonP.self.override({createScript:function(F,G,E){var C=Ext.urlAppend(F,Ext.Object.toQueryString(G)),D=!E.nosim&&B.getXhr("GET",C,E,true);if(!D){D=this.callParent(arguments)}return D},loadScript:function(D){var C=D.script;if(C.simlet){C.jsonpCallback=D.params[D.callbackKey];C.send(null)}else{this.callParent(arguments)}}})}}return B},openRequest:function(D,A,C){var B={method:D,url:A};return this._openRequest.call(Ext.data.Connection.prototype,{},B,C)},register:function(C){var B=this;B.init();function A(D){var E=D;if(!E.isSimlet){E=Ext.create("simlet."+(E.stype||B.defaultType),D)}B.simlets[D.url]=E;E.manager=B}if(Ext.isArray(C)){Ext.each(C,A)}else{if(C.isSimlet||C.url){A(C)}else{Ext.Object.each(C,function(D,E){E.url=D;A(E)})}}return B}})