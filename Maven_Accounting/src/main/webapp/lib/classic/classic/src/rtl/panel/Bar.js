Ext.define("Ext.rtl.panel.Bar",{override:"Ext.panel.Bar",rtlPositions:{top:"top",right:"left",bottom:"bottom",left:"right"},_rtlRotationClasses:{1:Ext.baseCSSPrefix+"title-rotate-left",2:Ext.baseCSSPrefix+"title-rotate-right"},_rtlRotationAngles:{1:270,2:90},onAdded:function(B,D,A){var C=this;if(C.isParentRtl()){C._rotationClasses=C._rtlRotationClasses;C._rotationAngles=C._rtlRotationAngles}this.callParent([B,D,A])},privates:{getDockName:function(){var B=this,A=B.dock;return B.isParentRtl()?B.rtlPositions[A]:A}}})