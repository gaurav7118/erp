Ext.define("Ext.device.Media",{singleton:true,requires:["Ext.device.Communicator","Ext.device.media.Cordova"],constructor:function(){var A=Ext.browser.is;if(A.WebView&&A.Cordova){return Ext.create("Ext.device.media.Cordova")}return Ext.create("Ext.device.media.Abstract")}})