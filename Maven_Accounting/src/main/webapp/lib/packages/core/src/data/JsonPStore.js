Ext.define("Ext.data.JsonPStore",{extend:"Ext.data.Store",alias:"store.jsonp",requires:["Ext.data.proxy.JsonP","Ext.data.reader.Json"],constructor:function(A){A=Ext.apply({proxy:{type:"jsonp",reader:"json"}},A);this.callParent([A])}})