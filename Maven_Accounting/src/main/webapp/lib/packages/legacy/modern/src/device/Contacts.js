Ext.define("Ext.device.Contacts",{singleton:true,requires:["Ext.device.Communicator","Ext.device.contacts.Cordova"],constructor:function(){var A=Ext.browser.is;if(A.WebView){if(A.Cordova){return Ext.create("Ext.device.contacts.Cordova")}}return Ext.create("Ext.device.contacts.Abstract")}})