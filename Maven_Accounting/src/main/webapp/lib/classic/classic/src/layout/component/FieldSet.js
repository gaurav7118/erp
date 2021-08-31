Ext.define("Ext.layout.component.FieldSet",{extend:"Ext.layout.component.Body",alias:["layout.fieldset"],type:"fieldset",defaultCollapsedWidth:100,beforeLayoutCycle:function(A){if(A.target.collapsed){A.heightModel=this.sizeModels.shrinkWrap}},beginLayout:function(B){var A=this.owner.legend;this.callParent([B]);if(A){B.legendContext=B.context.getCmp(A)}},beginLayoutCycle:function(B){var C=B.target,A;this.callParent(arguments);if(C.collapsed){B.setContentHeight(0);B.restoreMinHeight=C.minHeight;delete C.minHeight;if(B.widthModel.shrinkWrap){A=this.lastComponentSize;B.setContentWidth((A&&A.contentWidth)||this.defaultCollapsedWidth)}}},finishedLayout:function(C){var A=this.owner,B=C.restoreMinHeight;this.callParent(arguments);if(B){A.minHeight=B}},calculateOwnerWidthFromContentWidth:function(C,A){var B=C.legendContext;if(B){A=Math.max(A,B.getProp("width"))}return this.callParent([C,A])},calculateOwnerHeightFromContentHeight:function(D,C){var A=D.getBorderInfo(),B=D.legendContext;return D.getProp("contentHeight")+D.getPaddingInfo().height+(Ext.isIE8?D.bodyContext.getPaddingInfo().top:0)+(B?B.getProp("height"):A.top)+A.bottom},publishInnerHeight:function(D,A){var C=D.legendContext,B=0;if(C){B=C.getProp("height")}if(B===undefined){this.done=false}else{this.callParent([D,A-B])}},getLayoutItems:function(){var A=this.owner.legend;return A?[A]:[]}})