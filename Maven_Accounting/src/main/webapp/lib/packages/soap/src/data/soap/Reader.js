Ext.define("Ext.data.soap.Reader",{extend:"Ext.data.reader.Xml",alias:"reader.soap",getData:function(B){var C=B.documentElement,A=C.prefix;return Ext.DomQuery.selectNode(A+"|Body",B)}})