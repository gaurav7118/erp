Ext.define("Ext.device.geolocation.Simulator",{extend:"Ext.device.geolocation.Abstract",requires:["Ext.util.Geolocation"],getCurrentPosition:function(A){A=this.callParent([A]);Ext.apply(A,{autoUpdate:false,listeners:{scope:this,locationupdate:function(B){if(A.success){A.success.call(A.scope||this,B.position)}},locationerror:function(){if(A.failure){A.failure.call(A.scope||this)}}}});this.geolocation=Ext.create("Ext.util.Geolocation",A);this.geolocation.updateLocation();return A},watchPosition:function(A){A=this.callParent([A]);Ext.apply(A,{listeners:{scope:this,locationupdate:function(B){if(A.callback){A.callback.call(A.scope||this,B.position)}},locationerror:function(){if(A.failure){A.failure.call(A.scope||this)}}}});this.geolocation=Ext.create("Ext.util.Geolocation",A);return A},clearWatch:function(){if(this.geolocation){this.geolocation.destroy()}this.geolocation=null}})