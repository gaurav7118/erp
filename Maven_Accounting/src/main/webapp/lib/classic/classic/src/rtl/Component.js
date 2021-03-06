Ext.define("Ext.rtl.Component",{override:"Ext.Component",applyScrollable:function(B,C){var A=this.callParent([B,C]);if(A&&this.getInherited().rtl){A.setRtl(true)}return A},convertPositionSpec:function(A){if((Ext.rootInheritedState.rtl||false)!==(this.getInherited().rtl||false)){A=A.replace(/l/g,"tmp").replace(/r/g,"l").replace(/tmp/g,"r")}return A},getAnchorToXY:function(A,C,G,B){var I=document,F,H,E,D;if(A.dom===I.body||A.dom===I){H=Ext.rootInheritedState.rtl?A.rtlGetScroll():A.getScroll();E=H.left;D=H.top}else{F=A.getXY();E=G?0:F[0];D=G?0:F[1]}return A.calculateAnchorXY(C,E,D,B)},getBorderPadding:function(){var B=this.el.getBorderPadding(),A;if(this.isParentRtl()){A=B.xBegin;B.xBegin=B.xEnd;B.xEnd=A}return B},getLocalX:function(){return this.isLocalRtl()?this.el.rtlGetLocalX():this.el.getLocalX()},getLocalXY:function(){return this.isLocalRtl()?this.el.rtlGetLocalXY():this.el.getLocalXY()},unitizeBox:function(A){if(this.getInherited().rtl){return Ext.dom.Element.rtlUnitizeBox(A)}else{return this.callParent(arguments)}},initInheritedState:function(A){this.callParent(arguments);var B=this.rtl;if(B!==undefined){A.rtl=B}},isLocalRtl:function(){var B=this,C,A;if(B.floating){if(B._isOffsetParentRtl===undefined){A=this.el.dom.offsetParent||this.el.dom.parentNode;if(A){B._isOffsetParentRtl=Ext.fly(A,"_isLocalRtl").isStyle("direction","rtl")}}C=!!B._isOffsetParentRtl}else{C=this.isParentRtl()}return C},isParentRtl:function(){var C=this,B=C.getInherited(),A=false,D;if(B.hasOwnProperty("rtl")){D=B.rtl;delete B.rtl}if(B.rtl){A=true}if(D!==undefined){B.rtl=D}return A},setLocalX:function(A){return this.isLocalRtl()?this.el.rtlSetLocalX(A):this.el.setLocalX(A)},setLocalXY:function(A,B){return this.isLocalRtl()?this.el.rtlSetLocalXY(A,B):this.el.setLocalXY(A,B)},isOppositeRootDirection:function(){return !this.getInherited().rtl!==!Ext.rootInheritedState.rtl},privates:{initStyles:function(){if(this.getInherited().rtl){this.horizontalPosProp="right"}this.callParent(arguments)},parseBox:function(A){if(this.getInherited().rtl){return Ext.dom.Element.rtlParseBox(A)}else{return this.callParent(arguments)}}}},function(){Ext.onInternalReady(function(){if((Ext.fly(document.documentElement).isStyle("direction","rtl"))||(Ext.getBody().isStyle("direction","rtl"))){Ext.rootInheritedState.rtl=true}})})