Ext.define("Ext.panel.Bar",{extend:"Ext.container.Container",vertical:false,_verticalSides:{left:1,right:1},initComponent:function(){var B=this,A=B.vertical;B.dock=B.dock||(A?"left":"top");B.layout=Ext.apply(A?{type:"vbox",align:"middle",alignRoundingMethod:"ceil"}:{type:"hbox",align:"middle",alignRoundingMethod:"floor"},B.layout);this.callParent()},onAdded:function(B,C,A){this.initOrientation();this.callParent([B,C,A])},onRemoved:function(A){this.removeClsWithUI(this.uiCls);this.callParent([A])},beforeRender:function(){var A=this;if(A.forceOrientation||!A.ownerCt){A.initOrientation()}A.callParent()},setDock:function(D){var C=this,B,A;if(D!==C.dock){Ext.suspendLayouts();C.clearOrientation();C.callParent([D]);C.initOrientation();A=C.vertical;B=C.layout;B.setVertical(A);B.setAlignRoundingMethod(A?"ceil":"floor");Ext.resumeLayouts(true)}},privates:{clearOrientation:function(){this.removeClsWithUI([this.vertical?"vertical":"horizontal",this.getDockName()])},getDockName:function(){return this.dock},initOrientation:function(){var C=this,B=C.dock,A=(C.vertical=(B?B in C._verticalSides:C.vertical));C.addClsWithUI([A?"vertical":"horizontal",C.getDockName()])}}})