Ext.define("Ext.rtl.layout.ContextItem",{override:"Ext.layout.ContextItem",addPositionStyles:function(D,B){var A=B.x,E=B.y,C=0;if(A!==undefined){D[this.parent.target.getInherited().rtl?"right":"left"]=A+"px";++C}if(E!==undefined){D.top=E+"px";++C}return C}})