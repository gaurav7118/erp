Ext.define("Ext.rtl.layout.container.Absolute",{override:"Ext.layout.container.Absolute",adjustWidthAnchor:function(C,B){if(this.owner.getInherited().rtl){var D=this.targetPadding,A=B.getStyle("right");return C-A+D.right}else{return this.callParent(arguments)}}})