Ext.define("Ext.rtl.panel.Title",{override:"Ext.panel.Title",getIconRenderData:function(){var A=this,B=A.callParent(),C=A.ownerCt;if(C&&C.isParentRtl()){B.childElCls=" "+A._rtlCls}return B},privates:{_getVerticalAdjustDirection:function(){var A=this.ownerCt;return(A&&A.isParentRtl())?"right":"left"}}})